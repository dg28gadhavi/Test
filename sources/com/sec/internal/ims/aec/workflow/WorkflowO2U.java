package com.sec.internal.ims.aec.workflow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.entitilement.data.ResponseServiceEntitlementStatus;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.aec.AECResult;
import com.sec.internal.ims.aec.persist.AECStorage;
import com.sec.internal.ims.aec.util.DataConnectivity;
import com.sec.internal.ims.aec.util.PsDataOffExempt;
import com.sec.internal.ims.aec.util.URLExtractor;
import com.sec.internal.ims.aec.util.ValidityTimer;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BulkEntitlementCheck;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.log.AECLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.net.SocketFactory;
import okhttp3.Dns;

public class WorkflowO2U extends WorkflowImpl {
    private static final String EXTRA_PHONE_ID = "phoneId";
    private static final int MAX_RETRY_COUNT = 4;
    private static final String MDSP30 = "3.0";
    private static final int RESPONSE_ENTITLEMENT_CHECK = 101;
    private static final String SERVICE_ENTITLEMENT_STATUS = "serviceEntitlementStatus";
    private static final String SERVICE_VOLTE = "VoLTE";
    private static final String SERVICE_VOWIFI = "VoWiFi";
    private BaseFlowImpl mBaseFlowImpl;
    private BulkEntitlementCheck mBulkEntitlementCheck;
    private int mRetryCount = 0;
    private boolean mReuseLastEntitlementResult = false;

    WorkflowO2U(Context context, Looper looper, Handler handler, String str) {
        super(context, looper, handler, str);
    }

    public void initWorkflow(int i, String str, String str2) {
        this.mPhoneId = i;
        this.mAECJar = new AECStorage(this.mContext, i, str2);
        this.mEventLog = new SimpleEventLog(this.mContext, i, this.LOG_TAG, 200);
        this.mPsDataOffExempt = new PsDataOffExempt(this.mContext, this.mPhoneId, this);
        this.mValidityTimer = new ValidityTimer(this.mContext, this.mPhoneId, this);
        this.mBaseFlowImpl = new BaseFlowImpl(getLooper(), this.mContext, this.mPhoneId);
        this.mBulkEntitlementCheck = new BulkEntitlementCheck(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), MDSP30);
        sendMessage(obtainMessage(1000, str));
    }

    public void clearResource() {
        sendEmptyMessage(1009);
        this.mValidityTimer.stopPollIntervalTimer();
    }

    public boolean getVoWiFiEntitlementStatus() {
        return this.mAECJar.getVoWiFiEntitlementStatus() == 1;
    }

    public void handleMessage(Message message) {
        int i = message.what;
        if (i == 101) {
            onHandleEntitlementResponse(message.getData());
        } else if (i == 1013) {
            this.mIsValidEntitlement = false;
            requestEntitlement(this.mAECJar.getVersion());
        } else if (i == 1000) {
            onInitWorkFlow((String) message.obj);
        } else if (i != 1001) {
            switch (i) {
                case 1007:
                    this.mPsDataOffExempt.requestNetwork();
                    return;
                case 1008:
                    onNetworkCallbackAvailable();
                    return;
                case 1009:
                    onUnregisterNetworkCallback();
                    return;
                default:
                    return;
            }
        } else {
            requestEntitlement(this.mAECJar.getVersion());
        }
    }

    private void onInitWorkFlow(String str) {
        if (str.equals(this.mAECJar.getImsi())) {
            AECLog.i(this.LOG_TAG, "identical sim, recover to the stored configuration", this.mPhoneId);
        } else {
            this.mAECJar.setDefaultValues("0");
            AECStorage aECStorage = this.mAECJar;
            aECStorage.setVoLteEntitlementStatus(aECStorage.getEntitlementForVoLte());
            AECStorage aECStorage2 = this.mAECJar;
            aECStorage2.setVoWiFiEntitlementStatus(aECStorage2.getEntitlementForVoWiFi());
            AECStorage aECStorage3 = this.mAECJar;
            aECStorage3.setSMSoIPEntitlementStatus(aECStorage3.getEntitlementForSMSoIp());
            AECLog.i(this.LOG_TAG, "sim swapped, revert to the default configuration", this.mPhoneId);
        }
        this.mAECJar.setImsi(str);
    }

    public void performEntitlement(Object obj) {
        if (NetworkUtil.isConnected(this.mContext) || this.mPsDataOffExempt.isAvailable()) {
            removeMessages(1001);
            sendMessageDelayed(obtainMessage(1001), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
            return;
        }
        AECLog.i(this.LOG_TAG, "performEntitlement: data unavailable", this.mPhoneId);
        if (this.mAECJar.getPsDataOffExempt() && !DataConnectivity.isDataAvailable(this.mContext)) {
            AECLog.i(this.LOG_TAG, "performEntitlement: 3GPP PS Data Off Exempt Services", this.mPhoneId);
            sendMessageDelayed(obtainMessage(1007), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
        }
    }

    /* access modifiers changed from: protected */
    public void requestEntitlement(int i) {
        if (!this.mIsValidEntitlement && !this.mIsEntitlementOngoing) {
            this.mIsEntitlementOngoing = true;
            String str = this.LOG_TAG;
            AECLog.i(str, "requestEntitlement " + this.mRetryCount + " times", this.mPhoneId);
            ArrayList arrayList = new ArrayList(Arrays.asList(new String[]{"VoLTE", SERVICE_VOWIFI}));
            this.mBaseFlowImpl.getNSDSClient().setRequestUrl(URLExtractor.getUrl(this.mContext, this.mPhoneId, this.mAECJar.getEntitlementDomain(), this.mAECJar.getEntitlementPort(), this.mAECJar.getEntitlementPath(), this.mAECJar.getDomainFromImpi()));
            this.mBulkEntitlementCheck.checkBulkEntitlement(arrayList, true);
        }
    }

    private void onNetworkCallbackAvailable() {
        this.mBaseFlowImpl.getNSDSClient().setNetwork(this.mPsDataOffExempt.getDns(), this.mPsDataOffExempt.getSocketFactory());
        requestEntitlement(this.mAECJar.getVersion());
    }

    private void onUnregisterNetworkCallback() {
        this.mBaseFlowImpl.getNSDSClient().setNetwork((Dns) null, (SocketFactory) null);
        this.mPsDataOffExempt.unregisterNetworkCallback();
    }

    private static class NSDSResponse {
        public boolean entitlementStatus_VoLTE = false;
        public boolean entitlementStatus_VoWiFi = false;
        public int pollInterval = 24;
        public int responseCode = -1;
        public int responseCode_VoLTE = -1;
        public int responseCode_VoWiFi = -1;

        public String printLog() {
            return "responseCode: " + this.responseCode + ", pollInterval: " + this.pollInterval + ", responseCode_VoLTE: " + this.responseCode_VoLTE + ", entitlementStatus_VoLTE: " + this.entitlementStatus_VoLTE + ", responseCode_VoWiFi: " + this.responseCode_VoWiFi + ", entitlementStatus_VoWiFi: " + this.entitlementStatus_VoWiFi;
        }
    }

    private void onHandleEntitlementResponse(Bundle bundle) {
        handleEntitlementResponse(setEntitlementStatus((ResponseServiceEntitlementStatus) bundle.getParcelable("serviceEntitlementStatus")));
        this.mIsEntitlementOngoing = false;
    }

    private <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
        return iterable == null ? Collections.emptyList() : iterable;
    }

    private NSDSResponse setEntitlementStatus(ResponseServiceEntitlementStatus responseServiceEntitlementStatus) {
        NSDSResponse nSDSResponse = new NSDSResponse();
        if (responseServiceEntitlementStatus != null) {
            int i = responseServiceEntitlementStatus.responseCode;
            nSDSResponse.responseCode = i;
            nSDSResponse.responseCode_VoLTE = i;
            nSDSResponse.responseCode_VoWiFi = i;
            if (i == 1000) {
                for (T t : emptyIfNull(responseServiceEntitlementStatus.serviceEntitlementList)) {
                    boolean z = true;
                    if ("VoLTE".equalsIgnoreCase(t.serviceName)) {
                        int i2 = t.entitlementStatus;
                        nSDSResponse.responseCode_VoLTE = i2;
                        if (i2 != 1000) {
                            z = false;
                        }
                        nSDSResponse.entitlementStatus_VoLTE = z;
                    } else if (SERVICE_VOWIFI.equalsIgnoreCase(t.serviceName)) {
                        int i3 = t.entitlementStatus;
                        nSDSResponse.responseCode_VoWiFi = i3;
                        if (i3 != 1000) {
                            z = false;
                        }
                        nSDSResponse.entitlementStatus_VoWiFi = z;
                    }
                }
            }
            Integer num = responseServiceEntitlementStatus.pollInterval;
            if (num != null) {
                nSDSResponse.pollInterval = num.intValue();
            }
        }
        this.mEventLog.logAndAdd(this.mPhoneId, "setEntitlementStatus: " + nSDSResponse.printLog());
        return nSDSResponse;
    }

    private void handleServiceNotEntitled(int i) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i2 = this.mPhoneId;
        simpleEventLog.logAndAdd(i2, "handleServiceNotEntitled: " + i);
        this.mAECJar.setVoLteEntitlementStatus(false);
        this.mAECJar.setVoWiFiEntitlementStatus(false);
        this.mValidityTimer.stopPollIntervalTimer();
        this.mIsValidEntitlement = true;
        AECResult.handleUtSwitch(this.mPhoneId, false);
        AECResult.sendDeRegister(this.mContext, this.mPhoneId);
        sendBroadcastCompletedEntitlement();
    }

    private void handleEntitlementResponse(NSDSResponse nSDSResponse) {
        int i = nSDSResponse.responseCode;
        if (1048 == i || 1048 == nSDSResponse.responseCode_VoLTE) {
            handleServiceNotEntitled(i);
        } else if (checkRetry(i)) {
            this.mRetryCount++;
            if (-1 == nSDSResponse.responseCode) {
                AECLog.i(this.LOG_TAG, "need to try again entitlement check after 5 seconds", this.mPhoneId);
                sendMessageDelayed(obtainMessage(1001), 5000);
                return;
            }
            AECLog.i(this.LOG_TAG, "need to try again entitlement check after 30 seconds", this.mPhoneId);
            sendMessageDelayed(obtainMessage(1001), 30000);
        } else {
            if (this.mReuseLastEntitlementResult) {
                this.mReuseLastEntitlementResult = false;
                startPollIntervalTimer(86400);
                this.mEventLog.logAndAdd(this.mPhoneId, "all tries failed, retry entitlement check after 1 day");
            } else {
                this.mAECJar.setVoLteEntitlementStatus(nSDSResponse.entitlementStatus_VoLTE);
                this.mAECJar.setVoWiFiEntitlementStatus(nSDSResponse.entitlementStatus_VoWiFi);
                startPollIntervalTimer(nSDSResponse.pollInterval * 60 * 60);
                sendBroadcastCompletedEntitlement();
                if (nSDSResponse.entitlementStatus_VoLTE || nSDSResponse.entitlementStatus_VoWiFi) {
                    AECResult.handleUtSwitch(this.mPhoneId, true);
                    AECResult.sendTryRegister(this.mContext, this.mPhoneId);
                } else {
                    AECResult.sendDeRegister(this.mContext, this.mPhoneId);
                }
            }
            sendEmptyMessage(1009);
        }
    }

    private boolean checkRetry(int i) {
        if (this.mRetryCount > 4) {
            AECLog.i(this.LOG_TAG, "checkRetry: exceeded max try count", this.mPhoneId);
            this.mReuseLastEntitlementResult = true;
            this.mRetryCount = 0;
            return false;
        } else if (i == -1 || i >= 1001) {
            String str = this.LOG_TAG;
            AECLog.i(str, "checkRetry: NSDS error: " + i, this.mPhoneId);
            return true;
        } else if (i != 486 && i != 408 && i != 500 && i != 503 && i != 480) {
            return false;
        } else {
            String str2 = this.LOG_TAG;
            AECLog.i(str2, "checkRetry: HTTP error:" + i, this.mPhoneId);
            return true;
        }
    }

    private void startPollIntervalTimer(int i) {
        this.mIsValidEntitlement = true;
        this.mValidityTimer.startPollIntervalTimer(i);
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i2 = this.mPhoneId;
        simpleEventLog.logAndAdd(i2, "entitlement check will be performed after " + i + " sec");
    }

    private void sendBroadcastCompletedEntitlement() {
        Arrays.asList(new String[]{AECNamespace.Packages.UNIFIED_WFC, "com.sec.epdg", this.mContext.getPackageName()}).forEach(new WorkflowO2U$$ExternalSyntheticLambda0(this));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$sendBroadcastCompletedEntitlement$0(String str) {
        Intent intent = new Intent(AECNamespace.Action.COMPLETED_ENTITLEMENT);
        intent.putExtra("phoneId", this.mPhoneId);
        intent.setPackage(str);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }
}
