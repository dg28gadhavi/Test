package com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSErrorTranslator;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.LineDetail;
import com.sec.internal.constants.ims.entitilement.data.ResponseGetMSISDN;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity;
import com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.SIMDeviceActivation;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.persist.PushTokenHelper;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.IntentScheduler;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISIMDeviceImplicitActivation;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;

public class SIMDeviceImplicitActivation extends NSDSAppFlowBase implements ISIMDeviceImplicitActivation {
    private static final String ACTION_WAIT_GCM_TOKEN = "com.sec.vsim.ericssonnsds.WAIT_GCM_TOKEN";
    private static final long GCM_TOKEN_WAIT_TIME = 10000;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = SIMDeviceImplicitActivation.class.getSimpleName();
    private static final int START_SIM_ACTIVATION = 1;
    private BroadcastReceiver mGcmTokenIntentReceiver = null;
    private SharedPreferences.OnSharedPreferenceChangeListener mNSDSSharedPrefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            if (str.contains(NSDSNamespaces.NSDSSharedPref.PREF_SENT_TOKEN_TO_SERVER)) {
                SIMDeviceImplicitActivation sIMDeviceImplicitActivation = SIMDeviceImplicitActivation.this;
                boolean isGcmTokenSentToServer = NSDSSharedPrefHelper.isGcmTokenSentToServer(sIMDeviceImplicitActivation.mContext, sIMDeviceImplicitActivation.mBaseFlowImpl.getDeviceId());
                String r4 = SIMDeviceImplicitActivation.LOG_TAG;
                IMSLog.i(r4, "PREF_SENT_TOKEN_TO_SERVER: " + isGcmTokenSentToServer);
                SIMDeviceImplicitActivation.this.resumeSimDeviceActivation();
            }
        }
    };
    protected LineDetail mNativeLineDetail = new LineDetail();

    /* access modifiers changed from: private */
    public void resumeSimDeviceActivation() {
        IMSLog.i(LOG_TAG, "resumeSimDeviceActivation()");
        stopGcmTokenWaitTimer();
        unregisterNsdsSharePrefChangeListener();
        NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE);
        sendEmptyMessage(1);
    }

    private void registerNsdsSharePrefChangeListener() {
        SharedPreferences sharedPref = NSDSSharedPrefHelper.getSharedPref(this.mContext, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0);
        if (sharedPref != null) {
            sharedPref.registerOnSharedPreferenceChangeListener(this.mNSDSSharedPrefChangeListener);
        }
    }

    private void unregisterNsdsSharePrefChangeListener() {
        SharedPreferences sharedPref = NSDSSharedPrefHelper.getSharedPref(this.mContext, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0);
        if (sharedPref != null) {
            sharedPref.unregisterOnSharedPreferenceChangeListener(this.mNSDSSharedPrefChangeListener);
        }
    }

    private void startGcmTokenWaitTimer() {
        IMSLog.i(LOG_TAG, "startGcmTokenWaitTimer()");
        stopGcmTokenWaitTimer();
        AnonymousClass2 r0 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String r3 = SIMDeviceImplicitActivation.LOG_TAG;
                IMSLog.i(r3, "onReceive: timer expired for " + intent.getAction());
                if (SIMDeviceImplicitActivation.ACTION_WAIT_GCM_TOKEN.equals(intent.getAction())) {
                    SIMDeviceImplicitActivation.this.resumeSimDeviceActivation();
                }
            }
        };
        this.mGcmTokenIntentReceiver = r0;
        this.mContext.registerReceiver(r0, new IntentFilter(ACTION_WAIT_GCM_TOKEN));
        IntentScheduler.scheduleTimer(this.mContext, this.mBaseFlowImpl.getSimManager().getSimSlotIndex(), ACTION_WAIT_GCM_TOKEN, 10000);
    }

    private void stopGcmTokenWaitTimer() {
        if (this.mGcmTokenIntentReceiver != null) {
            IMSLog.i(LOG_TAG, "stopGcmTokenWaitTimer()");
            IntentScheduler.stopTimer(this.mContext, this.mBaseFlowImpl.getSimManager().getSimSlotIndex(), ACTION_WAIT_GCM_TOKEN);
            this.mContext.unregisterReceiver(this.mGcmTokenIntentReceiver);
            this.mGcmTokenIntentReceiver = null;
        }
    }

    public SIMDeviceImplicitActivation(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper) {
        super(looper, context, baseFlowImpl, nSDSDatabaseHelper);
    }

    /* access modifiers changed from: protected */
    public NSDSAppFlowBase.NSDSResponseStatus handleSimDeviceActivationResponse(Bundle bundle) {
        int i;
        int i2;
        int i3;
        int i4;
        int httpErrRespCode = getHttpErrRespCode(bundle);
        String str = LOG_TAG;
        IMSLog.i(str, "handleSimDeviceActivationResponse: errorResponseCode: " + httpErrRespCode);
        NSDSAppFlowBase.NSDSResponseStatus nSDSResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(httpErrRespCode, NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY, -1);
        if (bundle != null && httpErrRespCode <= 0) {
            ResponseManageConnectivity responseManageConnectivity = (ResponseManageConnectivity) bundle.getParcelable(NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY);
            ResponseManagePushToken responseManagePushToken = (ResponseManagePushToken) bundle.getParcelable("managePushToken");
            ResponseGetMSISDN responseGetMSISDN = (ResponseGetMSISDN) bundle.getParcelable(NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN);
            handleResponsePushToken(responseManagePushToken);
            handleResponseGetMsisdn(responseGetMSISDN);
            if (responseManageConnectivity == null || (i4 = responseManageConnectivity.responseCode) != 1000 || responseManagePushToken == null || responseManagePushToken.responseCode != 1000 || responseGetMSISDN == null || responseGetMSISDN.responseCode != 1000) {
                if (responseGetMSISDN != null && (i3 = responseGetMSISDN.responseCode) != 1000) {
                    nSDSResponseStatus.methodName = NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN;
                    nSDSResponseStatus.responseCode = i3;
                } else if (responseManageConnectivity != null && (i2 = responseManageConnectivity.responseCode) != 1000) {
                    nSDSResponseStatus.methodName = NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY;
                    nSDSResponseStatus.failedOperation = 0;
                    nSDSResponseStatus.responseCode = i2;
                } else if (!(responseManagePushToken == null || (i = responseManagePushToken.responseCode) == 1000)) {
                    nSDSResponseStatus.methodName = "managePushToken";
                    nSDSResponseStatus.responseCode = i;
                }
                IMSLog.e(str, "SIMDevice activation failed:");
            } else {
                nSDSResponseStatus.responseCode = i4;
            }
            if (this.mNativeLineDetail.lineId <= 0) {
                nSDSResponseStatus.responseCode = -1;
                IMSLog.e(str, "handleSimDeviceActivationResponse: native line insert/update failed");
            }
        }
        return nSDSResponseStatus;
    }

    /* access modifiers changed from: protected */
    public void handleResponseGetMsisdn(ResponseGetMSISDN responseGetMSISDN) {
        if (responseGetMSISDN != null) {
            String str = LOG_TAG;
            IMSLog.s(str, "responseGetMsisdn : messageId:" + responseGetMSISDN.messageId + "responseCode:" + responseGetMSISDN.responseCode + "msisdn:" + responseGetMSISDN.msisdn + "service_fingerprint:" + responseGetMSISDN.serviceFingerprint);
            if (responseGetMSISDN.responseCode == 1000 && responseGetMSISDN.msisdn != null && responseGetMSISDN.serviceFingerprint != null) {
                this.mNativeLineDetail.lineId = this.mNSDSDatabaseHelper.insertOrUpdateNativeLine(0, this.mBaseFlowImpl.getDeviceId(), responseGetMSISDN);
                LineDetail lineDetail = this.mNativeLineDetail;
                lineDetail.msisdn = responseGetMSISDN.msisdn;
                lineDetail.serviceFingerPrint = responseGetMSISDN.serviceFingerprint;
                return;
            }
            return;
        }
        IMSLog.e(LOG_TAG, "ResponseGetMSISDN is NULL");
    }

    /* access modifiers changed from: protected */
    public void handleResponsePushToken(ResponseManagePushToken responseManagePushToken) {
        if (responseManagePushToken != null) {
            String str = LOG_TAG;
            IMSLog.i(str, "responsePushToken : messageId:" + responseManagePushToken.messageId + "responseCode:" + responseManagePushToken.responseCode);
            return;
        }
        IMSLog.e(LOG_TAG, "ResponseManagePushToken is NULL");
    }

    public void performSimDeviceImplicitActivation(int i, int i2) {
        String str = LOG_TAG;
        IMSLog.i(str, "performSimDeviceImplicitActivation: eventType-" + i);
        this.mDeviceEventType = i;
        this.mRetryCount = i2;
        performNextOperationIf(-1, new NSDSAppFlowBase.NSDSResponseStatus(-1, (String) null, -1), (Bundle) null);
    }

    /* access modifiers changed from: protected */
    public void queueOperation(int i, Bundle bundle) {
        int i2 = 1;
        if (i != 1) {
            IMSLog.i(LOG_TAG, "queueOperation: did not match any nsds base operations");
            i2 = -1;
        }
        if (i2 != -1) {
            Message obtainMessage = obtainMessage(i2);
            obtainMessage.setData(bundle);
            sendMessage(obtainMessage);
        }
    }

    private void startSimDeviceActivation() {
        String str = LOG_TAG;
        IMSLog.i(str, "startSimDeviceActivation:");
        if (NSDSSharedPrefHelper.isDeviceInActivationProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(str, "startSimDeviceActivation: activation in progress. do not do any thing");
            return;
        }
        NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE, NSDSNamespaces.NSDSDeviceState.ACTIVATION_IN_PROGRESS);
        if (!NSDSSharedPrefHelper.isGcmTokenSentToServer(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.e(str, "startSimDeviceActivation: GCM token not yet created");
            registerNsdsSharePrefChangeListener();
            startGcmTokenWaitTimer();
            return;
        }
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        new SIMDeviceActivation(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0").activateSIMDevice((String) null, PushTokenHelper.getPushToken(this.mContext, this.mBaseFlowImpl.getDeviceId()), mnoNsdsStrategy != null ? mnoNsdsStrategy.getRetryInterval() : 0);
    }

    /* access modifiers changed from: protected */
    public void updateDeviceState(boolean z) {
        String str = LOG_TAG;
        IMSLog.i(str, "updateDeviceState: flow " + z);
        if (z) {
            NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE, NSDSNamespaces.NSDSDeviceState.ACTIVATED);
            return;
        }
        NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE, NSDSNamespaces.NSDSDeviceState.DEACTIVATED);
        NSDSSharedPrefHelper.removePrefForSlot(this.mContext, this.mBaseFlowImpl.getSimManager().getSimSlotIndex(), NSDSNamespaces.NSDSSharedPref.PREF_IMSI_EAP);
    }

    private int translateErrorCode(boolean z, String str, int i, int i2) {
        if (z || str == null || i2 == -1) {
            return -1;
        }
        return NSDSErrorTranslator.translate(str, i, i2);
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        IMSLog.i(str, "msg:" + message.what);
        int i = message.what;
        if (i == 1) {
            startSimDeviceActivation();
        } else if (i == 103) {
            performNextOperationIf(1, handleSimDeviceActivationResponse(message.getData()), (Bundle) null);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean z, String str, int i, int i2) {
        String str2 = LOG_TAG;
        IMSLog.i(str2, "notifyNSDSFlowResponse: success " + z);
        updateDeviceState(z);
        ArrayList arrayList = new ArrayList();
        arrayList.add(Integer.valueOf(translateErrorCode(z, str, i, i2)));
        int simSlotIndex = this.mBaseFlowImpl.getSimManager().getSimSlotIndex();
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.SIM_DEVICE_ACTIVATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, simSlotIndex);
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, z);
        intent.putExtra("retry_count", this.mRetryCount);
        intent.putExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, this.mDeviceEventType);
        intent.putExtra(NSDSNamespaces.NSDSExtras.ORIG_ERROR_CODE, i2);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, arrayList);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        notifyCallbackForNsdsEvent(0, simSlotIndex);
    }
}
