package com.sec.internal.ims.aec.workflow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.aec.persist.AECStorage;
import com.sec.internal.ims.aec.receiver.fcm.FcmIntentService;
import com.sec.internal.ims.aec.receiver.fcm.FcmNotification;
import com.sec.internal.ims.aec.util.CalcEapAka;
import com.sec.internal.ims.aec.util.DataConnectivity;
import com.sec.internal.ims.aec.util.HttpClient;
import com.sec.internal.ims.aec.util.HttpStore;
import com.sec.internal.ims.aec.util.NotificationUtil;
import com.sec.internal.ims.aec.util.PowerController;
import com.sec.internal.ims.aec.util.PsDataOffExempt;
import com.sec.internal.ims.aec.util.ValidityTimer;
import com.sec.internal.ims.fcm.interfaces.IFcmEventListener;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.interfaces.ims.aec.IWorkflowImpl;
import com.sec.internal.log.AECLog;
import java.util.LinkedList;
import java.util.Queue;

public abstract class WorkflowImpl extends Handler implements IWorkflowImpl {
    protected static final int TIMEOUT_PUSH_MSG = 30000;
    protected static final int TIMEOUT_WAKELOCK = 90000;
    protected final String LOG_TAG;
    protected AECStorage mAECJar;
    protected CalcEapAka mCalcEapAka;
    protected final Context mContext;
    protected SimpleEventLog mEventLog;
    protected IFcmEventListener mFcmEventListener = null;
    protected boolean mHasFcmEvent = false;
    protected HttpClient mHttpClient;
    protected HttpStore mHttpJar;
    protected boolean mIsEntitlementOngoing = false;
    protected boolean mIsReadyToNotifyApp = false;
    protected boolean mIsSharedAkaToken = false;
    protected boolean mIsValidEntitlement = false;
    protected final Handler mModuleHandler;
    protected Queue<String> mNotifJar = new LinkedList();
    protected NotifState mNotifState = NotifState.NOT_READY;
    protected NotificationUtil mNotifUtil;
    protected int mPhoneId = 0;
    protected PowerController mPowerCtrl;
    protected PsDataOffExempt mPsDataOffExempt;
    protected ValidityTimer mValidityTimer;

    protected enum NotifState {
        NOT_READY,
        IN_PROGRESS,
        READY
    }

    /* access modifiers changed from: package-private */
    public abstract void requestEntitlement(int i);

    WorkflowImpl(Context context, Looper looper, Handler handler, String str) {
        super(looper);
        this.mContext = context;
        this.mModuleHandler = handler;
        this.LOG_TAG = str;
    }

    public void dump() {
        this.mEventLog.dump();
    }

    public void initWorkflow(int i, String str, String str2) {
        this.mPhoneId = i;
        this.mAECJar = new AECStorage(this.mContext, i, str2);
        this.mCalcEapAka = new CalcEapAka(this.mPhoneId, str);
        this.mEventLog = new SimpleEventLog(this.mContext, i, this.LOG_TAG, 200);
        this.mHttpClient = new HttpClient(this.mPhoneId);
        this.mHttpJar = new HttpStore(this.mContext, this.mPhoneId);
        this.mNotifUtil = new NotificationUtil(this.mPhoneId, this.mAECJar.getAppId());
        this.mPowerCtrl = new PowerController(this.mContext, this.mPhoneId);
        this.mPsDataOffExempt = new PsDataOffExempt(this.mContext, this.mPhoneId, this);
        this.mValidityTimer = new ValidityTimer(this.mContext, this.mPhoneId, this);
        sendMessage(obtainMessage(1000, str));
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case 1000:
                onInitWorkFlow((String) message.obj);
                return;
            case 1001:
                onStartWorkFlow();
                return;
            case 1002:
                onStopWorkflow(message);
                return;
            case 1003:
                onCompletedWorkFlow();
                return;
            case 1004:
                onWaitEapAkaResp();
                return;
            case 1005:
                onCompletedEapChallengeResp(message);
                return;
            case 1006:
                onRequestFcmToken();
                return;
            case 1007:
                this.mPsDataOffExempt.requestNetwork();
                return;
            case 1008:
                performEntitlement((Object) null);
                return;
            case 1009:
                this.mPsDataOffExempt.unregisterNetworkCallback();
                return;
            case 1010:
                setValidEntitlement(false);
                performEntitlement((Object) null);
                return;
            case 1011:
                clearAkaToken();
                return;
            case 1012:
                onHandlePushNotification((String) message.obj);
                return;
            case 1014:
                onReceivedFcmNotification(message.arg1, (Bundle) message.obj);
                return;
            case 1015:
                onRefreshFcmToken(message.arg1);
                return;
            default:
                return;
        }
    }

    private void onInitWorkFlow(String str) {
        boolean z = false;
        if (str.equals(this.mAECJar.getImsi())) {
            AECLog.i(this.LOG_TAG, "identical sim, recover to the stored configuration", this.mPhoneId);
        } else {
            this.mAECJar.setDefaultValues("0");
            this.mAECJar.setHttpResponse(0);
            AECStorage aECStorage = this.mAECJar;
            aECStorage.setVoLteEntitlementStatus(aECStorage.getEntitlementForVoLte());
            AECStorage aECStorage2 = this.mAECJar;
            aECStorage2.setVoWiFiEntitlementStatus(aECStorage2.getEntitlementForVoWiFi());
            AECStorage aECStorage3 = this.mAECJar;
            aECStorage3.setSMSoIPEntitlementStatus(aECStorage3.getEntitlementForSMSoIp());
            AECLog.i(this.LOG_TAG, "sim swapped, revert to the default configuration", this.mPhoneId);
        }
        this.mAECJar.setImsi(str);
        this.mModuleHandler.sendMessage(obtainMessage(5, this.mPhoneId, this.mAECJar.getHttpResponse(), this.mAECJar.getStoredConfiguration()));
        if (!TextUtils.isEmpty(this.mAECJar.getNotifSenderId()) && "2".equals(this.mAECJar.getNotifAction())) {
            z = true;
        }
        this.mHasFcmEvent = z;
        if (z) {
            this.mFcmEventListener = new FcmNotification(this.mPhoneId, this);
            ImsRegistry.getFcmHandler().registerFcmEventListener(this.mFcmEventListener);
            AECLog.d(this.LOG_TAG, "registerFcmEventListener", this.mPhoneId);
        }
    }

    private void onStartWorkFlow() {
        if (NetworkUtil.isConnected(this.mContext) || this.mPsDataOffExempt.isAvailable()) {
            String str = this.LOG_TAG;
            AECLog.i(str, "onStartWorkFlow: " + this.mAECJar.getVersion(), this.mPhoneId);
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.add("onStartWorkFlow: " + this.mAECJar.getVersion());
            this.mIsEntitlementOngoing = true;
            this.mAECJar.setHttpResponse(0);
            requestEntitlement(this.mAECJar.getVersion());
        }
    }

    private void onStopWorkflow(Message message) {
        AECLog.i(this.LOG_TAG, "onStopWorkflow", this.mPhoneId);
        this.mIsEntitlementOngoing = false;
        this.mValidityTimer.stopTokenValidityTimer();
        this.mValidityTimer.stopVersionValidityTimer();
        sendEmptyMessage(1009);
        if (message != null) {
            this.mModuleHandler.sendMessage(obtainMessage(6, message.arg1, message.arg2));
        }
        if (!this.mNotifJar.isEmpty()) {
            AECLog.i(this.LOG_TAG, "notification jar is not empty, try entitlement", this.mPhoneId);
            handlePushNotification(this.mNotifJar.poll());
        }
    }

    private void onCompletedWorkFlow() {
        this.mIsEntitlementOngoing = false;
        this.mValidityTimer.stopVersionValidityTimer();
        this.mValidityTimer.stopTokenValidityTimer();
        sendEmptyMessage(1009);
        if (this.mAECJar.getVersion() > 0) {
            if (this.mAECJar.getHttpResponse() == 200) {
                setValidEntitlement(true);
                this.mValidityTimer.startVersionValidityTimer(this.mAECJar.getVersionValidity());
                this.mValidityTimer.startTokenValidityTimer(this.mAECJar.getTokenValidity());
            } else if (this.mAECJar.getHttpResponse() == 403) {
                this.mAECJar.setDefaultValues("0");
            }
        } else if (this.mAECJar.getVersion() == 0) {
            this.mAECJar.setDefaultValues("0");
        } else if (this.mAECJar.getVersion() < 0) {
            AECStorage aECStorage = this.mAECJar;
            aECStorage.setDefaultValues(Integer.toString(aECStorage.getVersion()));
        }
        Bundle storedConfiguration = this.mAECJar.getStoredConfiguration();
        String str = this.LOG_TAG;
        AECLog.i(str, "onCompletedWorkFlow: " + storedConfiguration.toString(), this.mPhoneId);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.add("onCompletedWorkFlow: " + storedConfiguration.toString());
        this.mModuleHandler.sendMessage(obtainMessage(5, this.mPhoneId, this.mAECJar.getHttpResponse(), storedConfiguration));
        if (!this.mNotifJar.isEmpty()) {
            AECLog.i(this.LOG_TAG, "notification jar is not empty, try entitlement", this.mPhoneId);
            handlePushNotification(this.mNotifJar.poll());
        }
    }

    private void onWaitEapAkaResp() {
        AECLog.i(this.LOG_TAG, "onWaitEapAkaResp", this.mPhoneId);
        this.mIsEntitlementOngoing = false;
    }

    private void onCompletedEapChallengeResp(Message message) {
        if (TextUtils.isEmpty((String) message.obj)) {
            AECLog.i(this.LOG_TAG, "onCompletedEapChallengeResp: no eap challenge response", this.mPhoneId);
            return;
        }
        String str = this.LOG_TAG;
        AECLog.i(str, "onCompletedEapChallengeResp: " + message.obj, this.mPhoneId);
        this.mHttpJar.setEapChallengeResp((String) message.obj);
        sendEmptyMessage(1001);
    }

    private void onRequestFcmToken() {
        NotifState notifState = NotifState.IN_PROGRESS;
        if (notifState != this.mNotifState) {
            AECLog.i(this.LOG_TAG, "onRequestFcmToken", this.mPhoneId);
            this.mNotifState = notifState;
            Intent intent = new Intent(this.mContext, FcmIntentService.class);
            intent.putExtra("phoneId", this.mPhoneId);
            intent.putExtra(AECNamespace.NotifExtras.SENDER_ID, this.mAECJar.getNotifSenderId());
            this.mContext.startService(intent);
        }
    }

    private void onHandlePushNotification(String str) {
        this.mNotifUtil.clearAppId();
        if (isEntitlementOngoing()) {
            this.mNotifJar.offer(str);
        } else if (!this.mAECJar.isSupportOnlyVoWiFibyUserAction() || this.mAECJar.getVersion() > 0) {
            handlePushNotification(str);
        } else {
            this.mEventLog.add("onHandlePushNotification : Don't try entitlement");
        }
    }

    private void handlePushNotification(String str) {
        String str2 = this.LOG_TAG;
        AECLog.i(str2, "handlePushNotification: " + str, this.mPhoneId);
        if (getEntitlementInitFromApp()) {
            setReadyToNotifyApp(true);
            this.mAECJar.setPushMsgStatus(true);
        }
        if (this.mAECJar.getVersion() < 0) {
            this.mAECJar.setVersion("0");
        }
        setValidEntitlement(false);
        performEntitlement(str);
    }

    private void onReceivedFcmNotification(int i, Bundle bundle) {
        if (this.mHasFcmEvent && this.mPhoneId == i) {
            String string = bundle.getString("from");
            String string2 = bundle.getString("app");
            String string3 = bundle.getString("timestamp");
            if (this.mAECJar.getNotifSenderId().equals(string) && !TextUtils.isEmpty(string2)) {
                if (this.mAECJar.getNotifIgnoreTimestamp() || this.mNotifUtil.validate(this.mAECJar.getTimeStamp(), string3)) {
                    removeMessages(1012);
                    int calcWaitTime = this.mNotifUtil.calcWaitTime(string3, string2);
                    this.mNotifUtil.updateAppId(string3, string2);
                    Message obtainMessage = obtainMessage();
                    obtainMessage.what = 1012;
                    obtainMessage.obj = this.mNotifUtil.getAppId();
                    sendMessageDelayed(obtainMessage, (long) calcWaitTime);
                    return;
                }
                AECLog.i(this.LOG_TAG, "discard incorrect notification", this.mPhoneId);
            }
        }
    }

    private void onRefreshFcmToken(int i) {
        int i2;
        if (this.mHasFcmEvent && (i2 = this.mPhoneId) == i) {
            AECLog.i(this.LOG_TAG, "onRefreshFcmToken", i2);
            sendEmptyMessage(1006);
        }
    }

    public void updateFcmToken(String str, String str2) {
        String str3 = this.LOG_TAG;
        AECLog.i(str3, "updateFcmToken: " + str2, this.mPhoneId);
        if (TextUtils.isEmpty(str)) {
            this.mNotifState = NotifState.NOT_READY;
            this.mAECJar.setNotifToken("");
            return;
        }
        this.mNotifState = NotifState.READY;
        String notifToken = this.mAECJar.getNotifToken();
        this.mAECJar.setNotifToken(str);
        if (!str.equals(notifToken) || !this.mIsValidEntitlement) {
            performEntitlement((Object) null);
        }
    }

    public void triggerAutoConfigForApp() {
        AECLog.i(this.LOG_TAG, "triggerAutoConfigForApp", this.mPhoneId);
        this.mAECJar.setHttpResponse(0);
        setValidEntitlement(false);
        setReadyToNotifyApp(true);
        performEntitlement((Object) null);
    }

    public void changeConnectivity() {
        if (!this.mPsDataOffExempt.isAvailable() && NetworkUtil.isConnected(this.mContext)) {
            performEntitlement((Object) null);
        }
    }

    public void performEntitlement(Object obj) {
        if (!this.mIsValidEntitlement && !isEntitlementOngoing()) {
            int httpResponse = this.mAECJar.getHttpResponse();
            if (httpResponse == 400 || httpResponse == 403 || httpResponse == 500) {
                String str = this.LOG_TAG;
                AECLog.i(str, "performEntitlement: stored response " + httpResponse, this.mPhoneId);
                sendMessage(obtainMessage(1002, this.mPhoneId, httpResponse));
            } else if (NetworkUtil.isConnected(this.mContext) || this.mPsDataOffExempt.isAvailable()) {
                HttpStore httpStore = this.mHttpJar;
                String str2 = (String) obj;
                if (TextUtils.isEmpty(str2)) {
                    str2 = this.mAECJar.getAppId();
                }
                httpStore.setAppId(str2);
                removeMessages(1001);
                if (!this.mHasFcmEvent || NotifState.NOT_READY != this.mNotifState) {
                    sendMessageDelayed(obtainMessage(1001), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                    return;
                }
                sendEmptyMessage(1006);
                sendMessageDelayed(obtainMessage(1001), 30000);
            } else {
                AECLog.i(this.LOG_TAG, "performEntitlement: data unavailable", this.mPhoneId);
                if (this.mAECJar.getPsDataOffExempt() && !DataConnectivity.isDataAvailable(this.mContext)) {
                    AECLog.i(this.LOG_TAG, "performEntitlement: 3GPP PS Data Off Exempt Services", this.mPhoneId);
                    sendMessageDelayed(obtainMessage(1007), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                }
            }
        }
    }

    public boolean isReadyToNotifyApp() {
        return this.mIsReadyToNotifyApp;
    }

    public void setReadyToNotifyApp(boolean z) {
        this.mIsReadyToNotifyApp = z;
    }

    public boolean isEntitlementOngoing() {
        return this.mIsEntitlementOngoing;
    }

    public void setValidEntitlement(boolean z) {
        this.mIsValidEntitlement = z;
    }

    public boolean isSharedAkaToken() {
        return this.mIsSharedAkaToken;
    }

    public void setSharedAkaToken(boolean z) {
        this.mIsSharedAkaToken = z;
    }

    public String getAkaToken() {
        return this.mAECJar.getAkaToken();
    }

    public void clearAkaToken() {
        this.mAECJar.setAkaToken("");
    }

    public void clearResource() {
        if (this.mFcmEventListener != null) {
            ImsRegistry.getFcmHandler().unregisterFcmEventListener(this.mFcmEventListener);
            AECLog.d(this.LOG_TAG, "unRegisterFcmEventListener", this.mPhoneId);
            this.mFcmEventListener = null;
            this.mNotifState = NotifState.NOT_READY;
        }
        sendEmptyMessage(1009);
        this.mHttpClient.closeURLConnection();
        this.mValidityTimer.stopVersionValidityTimer();
        this.mValidityTimer.stopTokenValidityTimer();
        this.mValidityTimer.unregisterReceiver();
        this.mPowerCtrl.release();
    }

    public void receivedSmsNotification(String str) {
        if (this.mAECJar.getVersion() < 0) {
            this.mAECJar.setVersion("0");
        }
        setValidEntitlement(false);
        performEntitlement(str);
    }

    public boolean getEntitlementForVoLte() {
        return this.mAECJar.getEntitlementForVoLte();
    }

    public boolean getEntitlementForVoWiFi() {
        return this.mAECJar.getEntitlementForVoWiFi();
    }

    public boolean getEntitlementForSMSoIp() {
        return this.mAECJar.getEntitlementForSMSoIp();
    }

    public boolean getEntitlementInitFromApp() {
        return this.mAECJar.getEntitlementInitFromApp();
    }

    public boolean getSMSoIpEntitlementStatus() {
        return this.mAECJar.getSMSoIPEntitlementStatus() == 1;
    }

    public boolean getVoLteEntitlementStatus() {
        return this.mAECJar.getVoLTEEntitlementStatus() == 1;
    }

    public boolean getVoWiFiEntitlementStatus() {
        return this.mAECJar.getVoWiFiActivationMode() == 3;
    }
}
