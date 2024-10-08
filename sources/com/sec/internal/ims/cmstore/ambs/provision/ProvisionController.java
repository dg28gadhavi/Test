package com.sec.internal.ims.cmstore.ambs.provision;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.ims.settings.RcsConfigurationReader;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.ReqConstant;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqRetireSession;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqToken;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqZCode;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestAccountEligibility;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestCreateAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestDeleteAccount;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestHUIToken;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestPat;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestTC;
import com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils;
import com.sec.internal.ims.cmstore.ambs.receiver.DataSMSReceiver;
import com.sec.internal.ims.cmstore.ambs.receiver.SmsReceiver;
import com.sec.internal.ims.cmstore.callHandling.errorHandling.ErrorRuleHandling;
import com.sec.internal.ims.cmstore.callHandling.successfullCall.SuccessfulCallHandling;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.FailedAPICallResponseParam;
import com.sec.internal.ims.cmstore.params.SuccessfulAPICallResponseParam;
import com.sec.internal.ims.cmstore.params.UIEventParam;
import com.sec.internal.ims.cmstore.receiver.NetworkChangeReceiver;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener;
import com.sec.internal.log.IMSLog;
import com.sec.sve.generalevent.VcidEvent;
import java.util.regex.Pattern;

public class ProvisionController extends Handler implements IAPICallFlowListener, IControllerCommonInterface {
    protected static final int EVENT_PAUSE = 6;
    protected static final int EVENT_PAUSE_SERVICE = 8;
    protected static final int EVENT_PROVISIONAPI_FAIL = 4;
    protected static final int EVENT_PROVISIONAPI_SUCCESS = 3;
    protected static final int EVENT_PROVISION_API = 1;
    protected static final int EVENT_RESUME = 5;
    protected static final int EVENT_STOP = 7;
    protected static final int EVENT_UI_ACTIONS = 2;
    private final long DELAY = 10000;
    private final long INTERNAL_WAITING = 5000;
    public String TAG = ProvisionController.class.getSimpleName();
    private final AmbsPhoneStateListener mAmbsPhoneStateListener;
    private final Context mContext;
    private DataSMSReceiver mDataSmsReceiver;
    private BroadcastReceiver mFactoryResetReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String str = ProvisionController.this.TAG;
            Log.i(str, "Factory Reset Added received intent : " + intent.getAction());
            String action = intent.getAction();
            action.hashCode();
            if (action.equals(ImsConstants.Intents.ACTION_FACTORY_RESET)) {
                ProvisionController.this.factoryReset();
            }
        }
    };
    private boolean mHasUserDeleteAccount = false;
    private boolean mHasUserOptedIn = false;
    private final ICloudMessageManagerHelper mICloudMessageManagerHelper;
    public IRetryStackAdapterHelper mIRetryStackAdapterHelper;
    private final IWorkingStatusProvisionListener mIWorkingStatusProvisionListener;
    private boolean mIfSteadyState = false;
    private boolean mIsInternalRestart = false;
    private int mLastSavedMessageIdAfterStop = -1;
    private ATTConstants.AttAmbsUIScreenNames mLastScreenUserStopBackup;
    private ATTConstants.AttAmbsUIScreenNames mLastUIScreen;
    private NetworkChangeReceiver mNetworkChangeReceiver;
    private int mNewUserOptInCase;
    private boolean mPaused = false;
    private SmsReceiver mSmsReceiver;
    private MessageStoreClient mStoreClient;
    private final IUIEventCallback mUIInterface;

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam, SyncMsgType syncMsgType, int i) {
    }

    public void onFailedEvent(int i, Object obj) {
    }

    public void onFixedFlowWithMessage(Message message) {
    }

    public void onMoveOnToNext(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i, Object obj) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface iHttpAPICommonInterface, int i, Object obj) {
    }

    public void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener) {
    }

    public boolean updateMessage(Message message) {
        return false;
    }

    public ProvisionController(IWorkingStatusProvisionListener iWorkingStatusProvisionListener, Looper looper, MessageStoreClient messageStoreClient, IUIEventCallback iUIEventCallback, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper);
        this.mStoreClient = messageStoreClient;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mIWorkingStatusProvisionListener = iWorkingStatusProvisionListener;
        this.mUIInterface = iUIEventCallback;
        Context context = messageStoreClient.getContext();
        this.mContext = context;
        this.mAmbsPhoneStateListener = new AmbsPhoneStateListener(this.mStoreClient.getClientID(), this, context);
        this.mIRetryStackAdapterHelper = iRetryStackAdapterHelper;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        this.mLastSavedMessageIdAfterStop = -1;
        this.mPaused = false;
        initPrefenceValues();
        registerFactoryResetReceiver();
        if (this.mStoreClient.getPrerenceManager().getAMBSPauseService()) {
            registerDataSmsReceiver();
        }
    }

    private void initPrefenceValues() {
        this.mNewUserOptInCase = this.mStoreClient.getPrerenceManager().getNewUserOptInCase();
        this.mIfSteadyState = this.mStoreClient.getPrerenceManager().ifSteadyState();
        this.mHasUserOptedIn = this.mStoreClient.getPrerenceManager().hasUserOptedIn();
        this.mLastUIScreen = ATTConstants.AttAmbsUIScreenNames.valueOf(this.mStoreClient.getPrerenceManager().getLastScreen());
        this.mLastScreenUserStopBackup = ATTConstants.AttAmbsUIScreenNames.valueOf(this.mStoreClient.getPrerenceManager().getLastScreenUserStopBackup());
        this.mHasUserDeleteAccount = this.mStoreClient.getPrerenceManager().hasUserDeleteAccount();
    }

    private void readNcNmsHost() {
        readNcHost();
        readNmsHost();
    }

    private static boolean isBase64(String str) {
        return Pattern.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$", str);
    }

    /* access modifiers changed from: private */
    public boolean readNcHost() {
        String string = new RcsConfigurationReader(this.mContext).getString("root/application/1/serviceproviderext/nc_url");
        String str = this.TAG;
        Log.d(str, "readNcHost() nc=" + string);
        if (TextUtils.isEmpty(string)) {
            return false;
        }
        String trim = string.trim();
        if (isBase64(trim)) {
            try {
                trim = new String(Base64.decode(trim, 0));
            } catch (IllegalArgumentException unused) {
                Log.e(this.TAG, "Failed to decrypt the NC");
            }
        }
        String ncHost = this.mStoreClient.getPrerenceManager().getNcHost();
        String str2 = this.TAG;
        Log.d(str2, "oldnc=" + ncHost + " nc=" + trim);
        if (trim.equals(ncHost)) {
            return false;
        }
        this.mStoreClient.getPrerenceManager().saveNcHost(trim);
        return true;
    }

    /* access modifiers changed from: private */
    public boolean readNmsHost() {
        String string = new RcsConfigurationReader(this.mContext).getString("root/application/1/serviceproviderext/nms_url");
        String str = this.TAG;
        Log.d(str, "readNmsHost() nms=" + string);
        if (TextUtils.isEmpty(string)) {
            return false;
        }
        try {
            string = new String(Base64.decode(string, 0));
        } catch (IllegalArgumentException unused) {
            Log.e(this.TAG, "Failed to decrypt the NMS");
        }
        this.mStoreClient.getPrerenceManager().saveAcsNmsHost(string);
        String nmsHost = this.mStoreClient.getPrerenceManager().getNmsHost();
        String str2 = this.TAG;
        Log.d(str2, "oldNms=" + nmsHost + " nms=" + string);
        if (TextUtils.isEmpty(nmsHost)) {
            return true;
        }
        return false;
    }

    private void registerConfigurationObserver() {
        this.mContext.getContentResolver().registerContentObserver(RcsConfigurationReader.AUTO_CONFIGURATION_URI, true, new ContentObserver(new Handler()) {
            public void onChange(boolean z, Uri uri) {
                super.onChange(z, uri);
                String str = ProvisionController.this.TAG;
                Log.d(str, "changed in DB. uri=" + IMSLog.checker(uri));
                if (uri.toString().contains("root/application/1/serviceproviderext/nc_url")) {
                    if (ProvisionController.this.readNcHost()) {
                        Log.d(ProvisionController.this.TAG, "nc host changed, send REQ_SESSION_GEN event");
                    }
                } else if (uri.toString().contains("root/application/1/serviceproviderext/nms_url") && ProvisionController.this.readNmsHost()) {
                    Log.d(ProvisionController.this.TAG, "nms host changed, send REQ_SESSION_GEN event");
                }
            }
        });
    }

    private void registerNetworkChangeReceiver() {
        Log.d(this.TAG, "registerNetworkChangeReceiver");
        if (this.mNetworkChangeReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            intentFilter.setPriority(Integer.MAX_VALUE);
            NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver(this.mIWorkingStatusProvisionListener);
            this.mNetworkChangeReceiver = networkChangeReceiver;
            this.mContext.registerReceiver(networkChangeReceiver, intentFilter);
        }
    }

    private void registerSmsReceiver() {
        IntentFilter intentFilter = new IntentFilter(com.sec.internal.ims.servicemodules.im.SmsReceiver.SMS_RECEIVED);
        intentFilter.setPriority(Integer.MAX_VALUE);
        if (this.mSmsReceiver == null) {
            SmsReceiver smsReceiver = new SmsReceiver(this, this.mStoreClient);
            this.mSmsReceiver = smsReceiver;
            this.mContext.registerReceiver(smsReceiver, intentFilter);
        }
        Log.d(this.TAG, "registerSmsReceiver");
    }

    private void unregisterSmsReceiver() {
        SmsReceiver smsReceiver = this.mSmsReceiver;
        if (smsReceiver != null) {
            this.mContext.unregisterReceiver(smsReceiver);
            this.mSmsReceiver = null;
        }
    }

    private void registerFactoryResetReceiver() {
        Log.i(this.TAG, "Updated with FactoryReset Receiver");
        if (this.mFactoryResetReceiver == null) {
            Log.i(this.TAG, "NULL Receiver");
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImsConstants.Intents.ACTION_SOFT_RESET);
        intentFilter.addAction(ImsConstants.Intents.ACTION_RESET_NETWORK_SETTINGS);
        intentFilter.addAction(ImsConstants.Intents.ACTION_FACTORY_RESET);
        this.mContext.registerReceiver(this.mFactoryResetReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    public void factoryReset() {
        Log.i(this.TAG, "Factory reset");
        this.mStoreClient.getPrerenceManager().saveAMBSStopService(false);
        this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.RestartMenu_Enable_PrmptMsg15.getId(), IUIEventCallback.NON_POP_UP, 0);
        this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg9.getId(), IUIEventCallback.NON_POP_UP, 0);
        IMSLog.i(this.TAG, "factoryReset Done");
    }

    private void registerDataSmsReceiver() {
        Log.d(this.TAG, "registerDataSmsReceiver");
        IntentFilter intentFilter = new IntentFilter(AECNamespace.Action.RECEIVED_SMS_NOTIFICATION);
        intentFilter.addDataAuthority("*", ATTGlobalVariables.ATT_DATA_MESSAGE_PORT);
        intentFilter.addDataScheme("sms");
        if (this.mDataSmsReceiver == null) {
            DataSMSReceiver dataSMSReceiver = new DataSMSReceiver(this, this.mStoreClient, this.mIWorkingStatusProvisionListener);
            this.mDataSmsReceiver = dataSMSReceiver;
            this.mContext.registerReceiver(dataSMSReceiver, intentFilter);
        }
        if (ATTGlobalVariables.supportSignedBinary() && this.mStoreClient.getPrerenceManager().isDebugEnable()) {
            this.mContext.registerReceiver(this.mDataSmsReceiver, new IntentFilter("android.test.ambsphasev.SIGNEDBINARYSMS"));
        }
    }

    private void unregisterDataSmsReceiver() {
        Log.d(this.TAG, "unregisterDataSmsReceiver");
        DataSMSReceiver dataSMSReceiver = this.mDataSmsReceiver;
        if (dataSMSReceiver != null) {
            this.mContext.unregisterReceiver(dataSMSReceiver);
            this.mDataSmsReceiver = null;
        }
    }

    private void startPhoneStateListener() {
        this.mAmbsPhoneStateListener.startListen();
    }

    private void stopPhoneStateListener() {
        this.mAmbsPhoneStateListener.stopListen();
    }

    public boolean onUIButtonProceed(int i, String str) {
        String str2 = this.TAG;
        Log.d(str2, "message: " + str);
        sendMessage(obtainMessage(2, new UIEventParam(ATTConstants.AttAmbsUIScreenNames.valueOf(i), str)));
        return true;
    }

    private boolean isAMBSActive() {
        boolean z = !this.mStoreClient.getPrerenceManager().getAMBSStopService() && !this.mStoreClient.getPrerenceManager().getAMBSPauseService();
        String str = this.TAG;
        Log.i(str, "isAMBSActive: " + z);
        return z;
    }

    public void handleMessage(Message message) {
        int i;
        super.handleMessage(message);
        String str = this.TAG;
        Log.i(str, "message: " + message.what);
        logCurrentWorkingStatus();
        if (isAMBSActive() || (i = message.what) == 7 || i == 8) {
            switch (message.what) {
                case 1:
                    EnumProvision.ProvisionEventType provisionEventType = (EnumProvision.ProvisionEventType) message.obj;
                    if (provisionEventType != null) {
                        handleProvisionEvent(provisionEventType);
                        return;
                    }
                    return;
                case 2:
                    UIEventParam uIEventParam = (UIEventParam) message.obj;
                    if (uIEventParam != null) {
                        handleUIEvent(uIEventParam.mUIScreen, uIEventParam.mMessage);
                        return;
                    }
                    return;
                case 3:
                    SuccessfulAPICallResponseParam successfulAPICallResponseParam = (SuccessfulAPICallResponseParam) message.obj;
                    if (successfulAPICallResponseParam != null) {
                        onProvisionAPISuccess(successfulAPICallResponseParam);
                        return;
                    }
                    return;
                case 4:
                    FailedAPICallResponseParam failedAPICallResponseParam = (FailedAPICallResponseParam) message.obj;
                    if (failedAPICallResponseParam != null) {
                        onProvisionAPIFail(failedAPICallResponseParam);
                        return;
                    }
                    return;
                case 5:
                    if (this.mPaused) {
                        this.mPaused = false;
                        int i2 = this.mLastSavedMessageIdAfterStop;
                        if (i2 != -1) {
                            sendMessage(obtainMessage(1, EnumProvision.ProvisionEventType.valueOf(i2)));
                            this.mLastSavedMessageIdAfterStop = -1;
                            Log.i(this.TAG, "resume successfully");
                            return;
                        }
                        Log.i(this.TAG, "no saved event");
                        return;
                    }
                    return;
                case 6:
                    this.mPaused = true;
                    return;
                case 7:
                    this.mPaused = true;
                    this.mLastSavedMessageIdAfterStop = -1;
                    this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.AMBS_SERVICE_DISABLE.getId(), IUIEventCallback.NON_POP_UP, 0);
                    return;
                case 8:
                    this.mPaused = true;
                    this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.RestartMenu_Disable_PrmptMsg16.getId(), IUIEventCallback.NON_POP_UP, 0);
                    this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
                    return;
                default:
                    return;
            }
        }
    }

    private void handleUIEvent(ATTConstants.AttAmbsUIScreenNames attAmbsUIScreenNames, String str) {
        String str2 = this.TAG;
        Log.i(str2, "handleUIEvent: " + attAmbsUIScreenNames + " messge: " + str);
        boolean supportSignedBinary = ATTGlobalVariables.supportSignedBinary();
        if (attAmbsUIScreenNames == null) {
            Log.d(this.TAG, "screenName is null");
            return;
        }
        switch (AnonymousClass3.$SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames[attAmbsUIScreenNames.ordinal()]) {
            case 1:
                if (!supportSignedBinary) {
                    unregisterDataSmsReceiver();
                }
                saveUserOptedIn(true);
                saveLastScreen(attAmbsUIScreenNames.getId());
                this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg11.getId(), IUIEventCallback.NON_POP_UP, 0);
                int i = this.mNewUserOptInCase;
                String str3 = this.TAG;
                Log.d(str3, "newUserOptInCase: " + i);
                if (i == EnumProvision.NewUserOptInCase.ERR.getId()) {
                    if (!TextUtils.isEmpty(this.mStoreClient.getPrerenceManager().getAtsToken())) {
                        update(EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId());
                    } else {
                        update(EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.getId());
                    }
                    saveNewUserOptInCase(EnumProvision.NewUserOptInCase.DEFAULT.getId());
                    return;
                } else if (i == EnumProvision.NewUserOptInCase.DELETE.getId()) {
                    saveNewUserOptInCase(EnumProvision.NewUserOptInCase.DEFAULT.getId());
                    update(EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.getId());
                    return;
                } else {
                    saveNewUserOptInCase(EnumProvision.NewUserOptInCase.DEFAULT.getId());
                    update(EnumProvision.ProvisionEventType.REQ_GET_TC.getId());
                    return;
                }
            case 2:
            case 3:
                if (!supportSignedBinary) {
                    unregisterDataSmsReceiver();
                }
                saveUserOptedIn(true);
                saveLastScreenUserStopBackup(attAmbsUIScreenNames.getId());
                saveLastScreen(attAmbsUIScreenNames.getId());
                this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
                update(EnumProvision.ProvisionEventType.REQ_HUI_TOKEN.getId());
                return;
            case 4:
                this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
                update(EnumProvision.ProvisionEventType.REQ_DELETE_ACCOUNT.getId());
                return;
            case 5:
                saveLastScreen(attAmbsUIScreenNames.getId());
                this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
                if (this.mStoreClient.getPrerenceManager().isLastAPIRequestCreateAccount()) {
                    Log.d(this.TAG, "HUIToken 6014 case");
                    this.mStoreClient.getRetryStackAdapter().clearRetryHistory();
                    update(EnumProvision.ProvisionEventType.REQ_HUI_TOKEN.getId());
                    return;
                }
                IHttpAPICommonInterface lastFailedRequest = this.mStoreClient.getRetryStackAdapter().getLastFailedRequest();
                if (lastFailedRequest != null) {
                    Log.d(this.TAG, "SteadyStateError - retry api");
                    this.mStoreClient.getRetryStackAdapter().retryApi(lastFailedRequest, this, this.mICloudMessageManagerHelper, this.mIRetryStackAdapterHelper);
                    notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.SteadyState_PrmptMsg5.getId(), 0);
                    return;
                }
                Log.d(this.TAG, "last api is null");
                notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.SteadyState_PrmptMsg5.getId(), 0);
                this.mStoreClient.getRetryStackAdapter().clearRetryHistory();
                return;
            case 6:
                saveLastScreen(attAmbsUIScreenNames.getId());
                this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
                IHttpAPICommonInterface lastFailedRequest2 = this.mStoreClient.getRetryStackAdapter().getLastFailedRequest();
                if (lastFailedRequest2 != null) {
                    Log.d(this.TAG, "SteadyStateError - retry api");
                    this.mStoreClient.getRetryStackAdapter().retryApi(lastFailedRequest2, this, this.mICloudMessageManagerHelper, this.mIRetryStackAdapterHelper);
                    notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.SteadyState_PrmptMsg5.getId(), 0);
                    return;
                }
                Log.d(this.TAG, "retry stack is empty");
                update(EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId());
                this.mStoreClient.getRetryStackAdapter().clearRetryHistory();
                return;
            case 7:
                this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg11.getId(), IUIEventCallback.NON_POP_UP, 0);
                this.mStoreClient.getPrerenceManager().increaseUserInputNumberCount();
                saveUserOptedIn(true);
                if (!TextUtils.isEmpty(str)) {
                    this.mStoreClient.getPrerenceManager().saveUserCtn(str, true);
                    onFixedFlow(EnumProvision.ProvisionEventType.CHECK_PHONE_STATE.getId());
                    return;
                }
                Log.e(this.TAG, "phone number null");
                this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg9.getId(), IUIEventCallback.NON_POP_UP, 0);
                update(EnumProvision.ProvisionEventType.REQ_INPUT_CTN.getId());
                return;
            case 8:
                ATTConstants.AttAmbsUIScreenNames attAmbsUIScreenNames2 = this.mLastScreenUserStopBackup;
                if (attAmbsUIScreenNames2 != null) {
                    notifyMsgAppNonPopup(attAmbsUIScreenNames2.getId(), 0);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void handleProvisionEvent(EnumProvision.ProvisionEventType provisionEventType) {
        Log.i(this.TAG, "handleProvisionEvent: " + provisionEventType + " mHasUserOptedIn:" + this.mHasUserOptedIn + " mIfSteadyState:" + this.mIfSteadyState + " isAMBSActive: " + isAMBSActive());
        if (provisionEventType.getId() == EnumProvision.ProvisionEventType.REQ_DELETE_ACCOUNT.getId() || provisionEventType.getId() == EnumProvision.ProvisionEventType.RESTART_SERVICE.getId() || provisionEventType.getId() == EnumProvision.ProvisionEventType.INTERNAL_RESTART.getId() || this.mHasUserDeleteAccount || !this.mPaused) {
            boolean z = true;
            switch (AnonymousClass3.$SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType[provisionEventType.ordinal()]) {
                case 1:
                    registerNetworkChangeReceiver();
                    registerDataSmsReceiver();
                    if (ATTGlobalVariables.isGcmReplacePolling()) {
                        registerConfigurationObserver();
                    }
                    if (CmsUtil.isSimOrCtnChanged(this.mStoreClient) || ProvisionHelper.isOOBE(this.mStoreClient) || TextUtils.isEmpty(this.mStoreClient.getPrerenceManager().getUserCtn())) {
                        Log.i(this.TAG, "isSimOrCtnChanged || OOBE || empty CTN");
                        startOOBE();
                        this.mStoreClient.getPrerenceManager().saveAppVer(ATTGlobalVariables.VERSION_NAME);
                        return;
                    }
                    this.mStoreClient.getPrerenceManager().saveAppVer(ATTGlobalVariables.VERSION_NAME);
                    if (this.mStoreClient.getPrerenceManager().hasShownPopupOptIn() && !this.mHasUserOptedIn) {
                        Log.i(this.TAG, "has shown popup before, will not bother user and server, non_popup screen : " + this.mLastUIScreen);
                        ATTConstants.AttAmbsUIScreenNames attAmbsUIScreenNames = this.mLastUIScreen;
                        if (attAmbsUIScreenNames != null) {
                            notifyMsgAppNonPopup(attAmbsUIScreenNames.getId(), 0);
                            return;
                        } else {
                            notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                            return;
                        }
                    } else if (this.mStoreClient.getRetryStackAdapter().isRetryTimesFinished(this.mICloudMessageManagerHelper)) {
                        Log.i(this.TAG, "isRetryTimesFinished");
                        ATTConstants.AttAmbsUIScreenNames attAmbsUIScreenNames2 = this.mLastUIScreen;
                        if (attAmbsUIScreenNames2 != null) {
                            notifyMsgAppNonPopup(attAmbsUIScreenNames2.getId(), 0);
                            return;
                        }
                        return;
                    } else {
                        IHttpAPICommonInterface lastFailedRequest = this.mStoreClient.getRetryStackAdapter().getLastFailedRequest();
                        if (lastFailedRequest != null) {
                            Log.i(this.TAG, "retryLastApi");
                            if (lastFailedRequest instanceof ReqZCode) {
                                Log.d(this.TAG, "in order to Auth Z code, register sms receiver");
                                registerSmsReceiver();
                            }
                            this.mStoreClient.getRetryStackAdapter().retryLastApi(this, this.mICloudMessageManagerHelper, this.mIRetryStackAdapterHelper);
                            ATTConstants.AttAmbsUIScreenNames attAmbsUIScreenNames3 = this.mLastUIScreen;
                            if (attAmbsUIScreenNames3 != null) {
                                notifyMsgAppNonPopup(attAmbsUIScreenNames3.getId(), 0);
                                return;
                            }
                            return;
                        } else if (TextUtils.isEmpty(this.mStoreClient.getPrerenceManager().getAtsToken())) {
                            update(EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.getId());
                            return;
                        } else if (TextUtils.isEmpty(this.mStoreClient.getPrerenceManager().getValidPAT())) {
                            this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
                            update(EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId());
                            return;
                        } else if (!TextUtils.isEmpty(this.mStoreClient.getPrerenceManager().getValidPAT())) {
                            Log.i(this.TAG, "PAT VALID");
                            onProvisionReady();
                            return;
                        } else {
                            return;
                        }
                    }
                case 2:
                    ProvisionHelper.readAndSaveSimInformation(this.mStoreClient);
                    if (TextUtils.isEmpty(this.mStoreClient.getPrerenceManager().getUserCtn())) {
                        Log.d(this.TAG, "empty CTN");
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), ATTConstants.AttAmbsUIScreenNames.MsisdnEntry_ErrMsg6.getId());
                        return;
                    }
                    Log.d(this.TAG, "CTN was successfully read");
                    update(EnumProvision.ProvisionEventType.CHECK_PHONE_STATE.getId());
                    return;
                case 3:
                    startPhoneStateListener();
                    return;
                case 4:
                    EnumProvision.ProvisionEventType provisionEventType2 = EnumProvision.ProvisionEventType.EVENT_AUTH_ZCODE_TIMEOUT;
                    removeMessages(1, EnumProvision.ProvisionEventType.valueOf(provisionEventType2.getId()));
                    String userCtn = this.mStoreClient.getPrerenceManager().getUserCtn();
                    String convertPhoneNumberToUserAct = AmbsUtils.convertPhoneNumberToUserAct(this.mStoreClient.getSimManager().getMsisdn());
                    if (TextUtils.isEmpty(userCtn)) {
                        Log.i(this.TAG, "empty CTN, phone number:" + IMSLog.checker(convertPhoneNumberToUserAct));
                        if (TextUtils.isEmpty(convertPhoneNumberToUserAct)) {
                            convertPhoneNumberToUserAct = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getNativeLine();
                            Log.i(this.TAG, "Phone number from DB == " + IMSLog.checker(convertPhoneNumberToUserAct));
                        }
                        this.mStoreClient.getPrerenceManager().saveUserCtn(convertPhoneNumberToUserAct, false);
                    } else if (!TextUtils.isEmpty(convertPhoneNumberToUserAct) && !userCtn.equals(convertPhoneNumberToUserAct)) {
                        Log.i(this.TAG, "Phone number was changed!!");
                        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().needToHandleSimSwap()) {
                            this.mIWorkingStatusProvisionListener.onRestartService();
                            return;
                        }
                        return;
                    }
                    registerSmsReceiver();
                    updateDelay(provisionEventType2.getId(), 900000);
                    this.mStoreClient.getHttpController().execute(new ReqZCode(this, this.mStoreClient, this.mICloudMessageManagerHelper));
                    return;
                case 5:
                    removeMessages(1, EnumProvision.ProvisionEventType.valueOf(EnumProvision.ProvisionEventType.EVENT_AUTH_ZCODE_TIMEOUT.getId()));
                    unregisterSmsReceiver();
                    stopPhoneStateListener();
                    this.mStoreClient.getHttpController().execute(new ReqToken(this, this.mStoreClient, this.mICloudMessageManagerHelper));
                    return;
                case 6:
                    if (ATTGlobalVariables.isGcmReplacePolling()) {
                        readNcNmsHost();
                        initSharedPreference();
                    }
                    this.mIWorkingStatusProvisionListener.onChannelStateReset();
                    this.mStoreClient.getHttpController().execute(new ReqSession(this, this.mStoreClient, this.mIRetryStackAdapterHelper, this.mICloudMessageManagerHelper));
                    return;
                case 7:
                    this.mStoreClient.getHttpController().execute(new RequestAccount(this, this.mStoreClient));
                    return;
                case 8:
                    this.mStoreClient.getHttpController().execute(new RequestAccountEligibility(this, this.mStoreClient, this.mICloudMessageManagerHelper));
                    return;
                case 9:
                    this.mStoreClient.getHttpController().execute(new RequestTC(this, this.mStoreClient, this.mICloudMessageManagerHelper));
                    return;
                case 10:
                    this.mStoreClient.getHttpController().execute(new RequestCreateAccount(this, this.mStoreClient, this.mICloudMessageManagerHelper));
                    return;
                case 11:
                    saveUserDeleteAccount(true);
                    this.mIWorkingStatusProvisionListener.onUserDeleteAccount(true);
                    this.mStoreClient.getHttpController().execute(new RequestDeleteAccount(this, this.mStoreClient, this.mICloudMessageManagerHelper));
                    return;
                case 12:
                    if (!this.mIfSteadyState) {
                        saveIfSteadyState(true);
                    }
                    this.mStoreClient.getHttpController().execute(new RequestHUIToken(this, this.mStoreClient, this.mICloudMessageManagerHelper));
                    return;
                case 13:
                    this.mStoreClient.getHttpController().execute(new RequestPat(this, this.mStoreClient, this.mICloudMessageManagerHelper));
                    return;
                case 15:
                    this.mStoreClient.getHttpController().execute(new ReqRetireSession(this, this.mStoreClient, this.mICloudMessageManagerHelper));
                    onProvisionReady();
                    return;
                case 16:
                    boolean z2 = this.mHasUserOptedIn;
                    if (!z2 && !this.mIfSteadyState) {
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    } else if (!z2 || this.mIfSteadyState) {
                        this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
                        return;
                    } else {
                        this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.EligibilityError_ErrMsg1.getId(), IUIEventCallback.POP_UP, 0);
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    }
                case 17:
                    if (this.mStoreClient.getPrerenceManager().getIsUserInputCtn()) {
                        update(EnumProvision.ProvisionEventType.REQ_INPUT_CTN.getId());
                        return;
                    }
                    ProvisionHelper.readAndSaveSimInformation(this.mStoreClient);
                    if (this.mStoreClient.getPrerenceManager().isZCodeMax2Tries()) {
                        Log.d(this.TAG, "No more chance. Show error screen");
                        update(EnumProvision.ProvisionEventType.AUTH_ERR.getId());
                        this.mStoreClient.getPrerenceManager().removeZCodeCounter();
                        return;
                    }
                    this.mStoreClient.getPrerenceManager().increazeZCodeCounter();
                    update(EnumProvision.ProvisionEventType.REQ_AUTH_ZCODE.getId());
                    return;
                case 18:
                    if (this.mStoreClient.getPrerenceManager().getIsUserInputCtn()) {
                        this.mStoreClient.getPrerenceManager().clearInvalidUserCtn();
                    }
                    if (this.mStoreClient.getPrerenceManager().isNoMoreChanceUserInputNumber()) {
                        Log.d(this.TAG, "No more chance. Show error screen");
                        update(EnumProvision.ProvisionEventType.AUTH_ERR.getId());
                        return;
                    }
                    Log.d(this.TAG, "user still has a chance to input the number");
                    notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.MsisdnEntry_ErrMsg6.getId(), 0);
                    return;
                case 19:
                    boolean z3 = this.mHasUserOptedIn;
                    if (!z3 && !this.mIfSteadyState) {
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    } else if (!z3 || this.mIfSteadyState) {
                        this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
                        return;
                    } else {
                        this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.ProvisioningBlockedError_ErrMsg8.getId(), IUIEventCallback.POP_UP, 0);
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    }
                case 20:
                    if (!this.mHasUserOptedIn) {
                        if (!this.mIfSteadyState) {
                            saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                            notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                            return;
                        }
                        handleProvisionErr();
                        return;
                    } else if (!this.mIfSteadyState) {
                        this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.ProvisioningError_ErrMsg4.getId(), IUIEventCallback.POP_UP, 0);
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    } else {
                        handleProvisionErr();
                        return;
                    }
                case 21:
                    if (this.mStoreClient.getPrerenceManager().isNoMoreChanceUserInputNumber()) {
                        Log.d(this.TAG, "max 2 tries reached");
                        this.mStoreClient.getPrerenceManager().removeUserInputNumberCount();
                        this.mStoreClient.getRetryStackAdapter().clearRetryHistory();
                        this.mStoreClient.getPrerenceManager().saveUserCtn("", false);
                    } else {
                        z = false;
                    }
                    boolean z4 = this.mHasUserOptedIn;
                    if (!z4 && !this.mIfSteadyState) {
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    } else if (!z4 || this.mIfSteadyState) {
                        removeMessages(EnumProvision.ProvisionEventType.EVENT_AUTH_ZCODE_TIMEOUT.getId());
                        this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
                        return;
                    } else {
                        this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.AuthenticationError_ErrMsg2.getId(), IUIEventCallback.POP_UP, 0);
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        if (z) {
                            notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), ATTConstants.AttAmbsUIScreenNames.MsisdnEntry_ErrMsg6.getId());
                            return;
                        } else {
                            notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                            return;
                        }
                    }
                case 22:
                    if (this.mStoreClient.getPrerenceManager().isHUI6014Err()) {
                        this.mStoreClient.getPrerenceManager().saveIfHUI6014Err(false);
                    }
                    boolean z5 = this.mHasUserOptedIn;
                    if (!z5 && !this.mIfSteadyState) {
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    } else if (!z5 || this.mIfSteadyState) {
                        this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg5.getId(), IUIEventCallback.POP_UP, 0);
                        return;
                    } else {
                        this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg5.getId(), IUIEventCallback.POP_UP, 0);
                        saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        return;
                    }
                case 23:
                    if (this.mHasUserDeleteAccount) {
                        saveUserDeleteAccount(false);
                        this.mIWorkingStatusProvisionListener.onUserDeleteAccount(false);
                    }
                    this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.StopBackupError_ErrMsg10.getId(), IUIEventCallback.POP_UP, 0);
                    notifyMsgAppNonPopup(this.mLastScreenUserStopBackup.getId(), 0);
                    return;
                case 24:
                    String userCtn2 = this.mStoreClient.getPrerenceManager().getUserCtn();
                    String simImsi = this.mStoreClient.getPrerenceManager().getSimImsi();
                    boolean hasUserOptedIn = this.mStoreClient.getPrerenceManager().hasUserOptedIn();
                    boolean isUserInputCtn = this.mStoreClient.getPrerenceManager().getIsUserInputCtn();
                    this.mIWorkingStatusProvisionListener.onCloudSyncWorkingStopped();
                    this.mIWorkingStatusProvisionListener.onUserDeleteAccount(false);
                    stopProvisioningAPIs();
                    this.mStoreClient.getPrerenceManager().saveIfHasShownPopupOptIn(true);
                    this.mStoreClient.getPrerenceManager().saveSimImsi(simImsi);
                    this.mStoreClient.getPrerenceManager().saveUserCtn(userCtn2, isUserInputCtn);
                    saveUserOptedIn(false);
                    this.mStoreClient.getRetryStackAdapter().clearRetryHistory();
                    this.mLastSavedMessageIdAfterStop = -1;
                    saveNewUserOptInCase(EnumProvision.NewUserOptInCase.DELETE.getId());
                    notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                    initPrefenceValues();
                    if (hasUserOptedIn) {
                        registerDataSmsReceiver();
                        return;
                    }
                    return;
                case 25:
                    this.mIsInternalRestart = true;
                    if (this.mHasUserOptedIn) {
                        saveUserOptedIn(true);
                    }
                    this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.RestartMenu_Enable_PrmptMsg15.getId(), IUIEventCallback.NON_POP_UP, 0);
                    this.mPaused = false;
                    break;
                case 26:
                    break;
                case 27:
                    updateDelay(EnumProvision.ProvisionEventType.REQ_HUI_TOKEN.getId(), 10000);
                    return;
                case 28:
                    if (this.mStoreClient.getPrerenceManager().getIsUserInputCtn()) {
                        Log.d(this.TAG, "Wrong CTN, clear user input");
                        this.mStoreClient.getPrerenceManager().clearInvalidUserCtn();
                    }
                    if (!this.mStoreClient.getPrerenceManager().isZCodeMax2Tries()) {
                        Log.d(this.TAG, "isZCodeMax2Tries: false");
                        this.mStoreClient.getPrerenceManager().increazeZCodeCounter();
                        update(EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.getId());
                    } else {
                        Log.d(this.TAG, "isZCodeMax2Tries: true, mHasUserOptedIn:" + this.mHasUserOptedIn);
                        this.mStoreClient.getPrerenceManager().removeZCodeCounter();
                        if (this.mHasUserOptedIn) {
                            this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.AuthenticationError_ErrMsg2.getId(), IUIEventCallback.POP_UP, 0);
                            saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                            notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        } else {
                            saveNewUserOptInCase(EnumProvision.NewUserOptInCase.ERR.getId());
                            notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                        }
                    }
                    unregisterSmsReceiver();
                    stopPhoneStateListener();
                    return;
                case 29:
                    onMailBoxMigrationReset();
                    return;
                default:
                    return;
            }
            stopProvisioningAPIs();
            Log.i(this.TAG, "Provisioning Api's");
            if (!this.mIsInternalRestart) {
                saveUserOptedIn(false);
            }
            this.mLastSavedMessageIdAfterStop = -1;
            registerNetworkChangeReceiver();
            registerDataSmsReceiver();
            update(EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.ordinal());
            this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
            initPrefenceValues();
            return;
        }
        this.mLastSavedMessageIdAfterStop = provisionEventType.getId();
        Log.i(this.TAG, "handleMessage stop! Pending Message is " + provisionEventType);
    }

    /* renamed from: com.sec.internal.ims.cmstore.ambs.provision.ProvisionController$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType;

        /* JADX WARNING: Can't wrap try/catch for region: R(77:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|(2:13|14)|15|(2:17|18)|19|21|22|23|(2:25|26)|27|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|(2:71|72)|73|75|76|77|78|79|80|81|82|83|84|85|86|87|88|89|90|92) */
        /* JADX WARNING: Can't wrap try/catch for region: R(78:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|(2:13|14)|15|17|18|19|21|22|23|(2:25|26)|27|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|(2:71|72)|73|75|76|77|78|79|80|81|82|83|84|85|86|87|88|89|90|92) */
        /* JADX WARNING: Can't wrap try/catch for region: R(79:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|13|14|15|17|18|19|21|22|23|(2:25|26)|27|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|(2:71|72)|73|75|76|77|78|79|80|81|82|83|84|85|86|87|88|89|90|92) */
        /* JADX WARNING: Can't wrap try/catch for region: R(80:0|(2:1|2)|3|(2:5|6)|7|9|10|11|13|14|15|17|18|19|21|22|23|(2:25|26)|27|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|(2:71|72)|73|75|76|77|78|79|80|81|82|83|84|85|86|87|88|89|90|92) */
        /* JADX WARNING: Can't wrap try/catch for region: R(82:0|(2:1|2)|3|5|6|7|9|10|11|13|14|15|17|18|19|21|22|23|(2:25|26)|27|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|75|76|77|78|79|80|81|82|83|84|85|86|87|88|89|90|92) */
        /* JADX WARNING: Can't wrap try/catch for region: R(84:0|1|2|3|5|6|7|9|10|11|13|14|15|17|18|19|21|22|23|25|26|27|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|75|76|77|78|79|80|81|82|83|84|85|86|87|88|89|90|92) */
        /* JADX WARNING: Code restructure failed: missing block: B:93:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x0084 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x0090 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:41:0x009c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:43:0x00a8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:45:0x00b4 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:47:0x00c0 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:49:0x00cc */
        /* JADX WARNING: Missing exception handler attribute for start block: B:51:0x00d8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:53:0x00e4 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:55:0x00f0 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:57:0x00fc */
        /* JADX WARNING: Missing exception handler attribute for start block: B:59:0x0108 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:61:0x0114 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:63:0x0120 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:65:0x012c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:67:0x0138 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:69:0x0144 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:71:0x0150 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:77:0x016d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:79:0x0177 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:81:0x0181 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:83:0x018b */
        /* JADX WARNING: Missing exception handler attribute for start block: B:85:0x0195 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:87:0x019f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:89:0x01a9 */
        static {
            /*
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType[] r0 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType = r0
                r1 = 1
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r2 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.CHK_INITIAL_STATE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r3 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r4 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.CHECK_PHONE_STATE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                r3 = 4
                int[] r4 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r5 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.REQ_AUTH_ZCODE     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                r4 = 5
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r6 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.REQ_ATS_TOKEN     // Catch:{ NoSuchFieldError -> 0x003e }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r5[r6] = r4     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                r5 = 6
                int[] r6 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r7 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.REQ_SESSION_GEN     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r7 = r7.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r6[r7] = r5     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                r6 = 7
                int[] r7 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r8 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.REQ_SERVICE_ACCOUNT     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r8 = r8.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r7[r8] = r6     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                r7 = 8
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.REQ_ACCOUNT_ELIGIBILITY     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r8[r9] = r7     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.REQ_GET_TC     // Catch:{ NoSuchFieldError -> 0x006c }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r10 = 9
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.REQ_CREATE_ACCOUNT     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r10 = 10
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.REQ_DELETE_ACCOUNT     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r10 = 11
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.REQ_HUI_TOKEN     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r10 = 12
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.REQ_PAT     // Catch:{ NoSuchFieldError -> 0x009c }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r10 = 13
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.REQ_RETIRE_SESSION     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r10 = 14
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.READY_PAT     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r10 = 15
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x00c0 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.ACCOUNT_NOT_ELIGIBLE     // Catch:{ NoSuchFieldError -> 0x00c0 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c0 }
                r10 = 16
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x00c0 }
            L_0x00c0:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x00cc }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.ZCODE_ERROR_201     // Catch:{ NoSuchFieldError -> 0x00cc }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cc }
                r10 = 17
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x00cc }
            L_0x00cc:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.REQ_INPUT_CTN     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r10 = 18
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x00e4 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.CPS_PROVISION_SHUTDOWN     // Catch:{ NoSuchFieldError -> 0x00e4 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e4 }
                r10 = 19
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x00e4 }
            L_0x00e4:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x00f0 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.PROVISION_ERR     // Catch:{ NoSuchFieldError -> 0x00f0 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x00f0 }
                r10 = 20
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x00f0 }
            L_0x00f0:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x00fc }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.AUTH_ERR     // Catch:{ NoSuchFieldError -> 0x00fc }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x00fc }
                r10 = 21
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x00fc }
            L_0x00fc:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x0108 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.ACCESS_ERR     // Catch:{ NoSuchFieldError -> 0x0108 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x0108 }
                r10 = 22
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x0108 }
            L_0x0108:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x0114 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.STOP_BACKUP_ERR     // Catch:{ NoSuchFieldError -> 0x0114 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x0114 }
                r10 = 23
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x0114 }
            L_0x0114:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x0120 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.DELETE_ACCOUNT_SUCCESS     // Catch:{ NoSuchFieldError -> 0x0120 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x0120 }
                r10 = 24
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x0120 }
            L_0x0120:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x012c }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.INTERNAL_RESTART     // Catch:{ NoSuchFieldError -> 0x012c }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x012c }
                r10 = 25
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x012c }
            L_0x012c:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x0138 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.RESTART_SERVICE     // Catch:{ NoSuchFieldError -> 0x0138 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x0138 }
                r10 = 26
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x0138 }
            L_0x0138:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x0144 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.LAST_RETRY_CREATE_ACCOUNT     // Catch:{ NoSuchFieldError -> 0x0144 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x0144 }
                r10 = 27
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x0144 }
            L_0x0144:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x0150 }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.EVENT_AUTH_ZCODE_TIMEOUT     // Catch:{ NoSuchFieldError -> 0x0150 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x0150 }
                r10 = 28
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x0150 }
            L_0x0150:
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$cmstore$enumprovision$EnumProvision$ProvisionEventType     // Catch:{ NoSuchFieldError -> 0x015c }
                com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision$ProvisionEventType r9 = com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision.ProvisionEventType.MAILBOX_MIGRATION_RESET     // Catch:{ NoSuchFieldError -> 0x015c }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x015c }
                r10 = 29
                r8[r9] = r10     // Catch:{ NoSuchFieldError -> 0x015c }
            L_0x015c:
                com.sec.internal.constants.ims.cmstore.ATTConstants$AttAmbsUIScreenNames[] r8 = com.sec.internal.constants.ims.cmstore.ATTConstants.AttAmbsUIScreenNames.values()
                int r8 = r8.length
                int[] r8 = new int[r8]
                $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames = r8
                com.sec.internal.constants.ims.cmstore.ATTConstants$AttAmbsUIScreenNames r9 = com.sec.internal.constants.ims.cmstore.ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1     // Catch:{ NoSuchFieldError -> 0x016d }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x016d }
                r8[r9] = r1     // Catch:{ NoSuchFieldError -> 0x016d }
            L_0x016d:
                int[] r1 = $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames     // Catch:{ NoSuchFieldError -> 0x0177 }
                com.sec.internal.constants.ims.cmstore.ATTConstants$AttAmbsUIScreenNames r8 = com.sec.internal.constants.ims.cmstore.ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWithTerms_PrmptMsg3     // Catch:{ NoSuchFieldError -> 0x0177 }
                int r8 = r8.ordinal()     // Catch:{ NoSuchFieldError -> 0x0177 }
                r1[r8] = r0     // Catch:{ NoSuchFieldError -> 0x0177 }
            L_0x0177:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames     // Catch:{ NoSuchFieldError -> 0x0181 }
                com.sec.internal.constants.ims.cmstore.ATTConstants$AttAmbsUIScreenNames r1 = com.sec.internal.constants.ims.cmstore.ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWoTerms_PrmpMsg4     // Catch:{ NoSuchFieldError -> 0x0181 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0181 }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0181 }
            L_0x0181:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames     // Catch:{ NoSuchFieldError -> 0x018b }
                com.sec.internal.constants.ims.cmstore.ATTConstants$AttAmbsUIScreenNames r1 = com.sec.internal.constants.ims.cmstore.ATTConstants.AttAmbsUIScreenNames.StopBackup_PrmptMsg13     // Catch:{ NoSuchFieldError -> 0x018b }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x018b }
                r0[r1] = r3     // Catch:{ NoSuchFieldError -> 0x018b }
            L_0x018b:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames     // Catch:{ NoSuchFieldError -> 0x0195 }
                com.sec.internal.constants.ims.cmstore.ATTConstants$AttAmbsUIScreenNames r1 = com.sec.internal.constants.ims.cmstore.ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg5     // Catch:{ NoSuchFieldError -> 0x0195 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0195 }
                r0[r1] = r4     // Catch:{ NoSuchFieldError -> 0x0195 }
            L_0x0195:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames     // Catch:{ NoSuchFieldError -> 0x019f }
                com.sec.internal.constants.ims.cmstore.ATTConstants$AttAmbsUIScreenNames r1 = com.sec.internal.constants.ims.cmstore.ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7     // Catch:{ NoSuchFieldError -> 0x019f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x019f }
                r0[r1] = r5     // Catch:{ NoSuchFieldError -> 0x019f }
            L_0x019f:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames     // Catch:{ NoSuchFieldError -> 0x01a9 }
                com.sec.internal.constants.ims.cmstore.ATTConstants$AttAmbsUIScreenNames r1 = com.sec.internal.constants.ims.cmstore.ATTConstants.AttAmbsUIScreenNames.MsisdnEntry_ErrMsg6     // Catch:{ NoSuchFieldError -> 0x01a9 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x01a9 }
                r0[r1] = r6     // Catch:{ NoSuchFieldError -> 0x01a9 }
            L_0x01a9:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$ATTConstants$AttAmbsUIScreenNames     // Catch:{ NoSuchFieldError -> 0x01b3 }
                com.sec.internal.constants.ims.cmstore.ATTConstants$AttAmbsUIScreenNames r1 = com.sec.internal.constants.ims.cmstore.ATTConstants.AttAmbsUIScreenNames.StopBackupError_ErrMsg10     // Catch:{ NoSuchFieldError -> 0x01b3 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x01b3 }
                r0[r1] = r7     // Catch:{ NoSuchFieldError -> 0x01b3 }
            L_0x01b3:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.ambs.provision.ProvisionController.AnonymousClass3.<clinit>():void");
        }
    }

    private void startOOBE() {
        this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
        this.mStoreClient.getHttpController().getCookieJar().removeAll();
        this.mStoreClient.getPrerenceManager().clearAll();
        this.mIWorkingStatusProvisionListener.onCleanBufferDbRequired();
        initPrefenceValues();
        update(EnumProvision.ProvisionEventType.CHK_PHONE_ACCOUNT.getId());
    }

    private void onProvisionReady() {
        Log.i(this.TAG, "onProvisionReady");
        saveIfSteadyState(true);
        notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.SteadyState_PrmptMsg5.getId(), 0);
        this.mIWorkingStatusProvisionListener.onProvisionSuccess();
        if (!ATTGlobalVariables.supportSignedBinary()) {
            unregisterDataSmsReceiver();
        }
    }

    private void onProvisionAPISuccess(SuccessfulAPICallResponseParam successfulAPICallResponseParam) {
        String str = this.TAG;
        Log.i(str, "onProvisionAPISuccess: " + successfulAPICallResponseParam);
        handlerUIonSuccessProvisionAPI(successfulAPICallResponseParam);
        String str2 = successfulAPICallResponseParam.mCallFlow;
        if (str2 != null) {
            SuccessfulCallHandling.callHandling(this.mStoreClient, this, successfulAPICallResponseParam.mRequest, str2, this.mIRetryStackAdapterHelper, this.mICloudMessageManagerHelper);
        } else {
            SuccessfulCallHandling.callHandling(this.mStoreClient, this, successfulAPICallResponseParam.mRequest, this.mIRetryStackAdapterHelper, this.mICloudMessageManagerHelper);
        }
        if (this.mIsInternalRestart) {
            this.mIsInternalRestart = false;
        }
    }

    private void handleInternalRestart(SuccessfulAPICallResponseParam successfulAPICallResponseParam) {
        Log.i(this.TAG, "handleInternalRestart, restart without optin ui");
        this.mIsInternalRestart = false;
        if (!this.mHasUserOptedIn && !this.mIfSteadyState) {
            String str = this.TAG;
            Log.d(str, "handlerUIonSuccessProvisionAPI: User has NOT opted in: isOOBE?: " + ProvisionHelper.isOOBE(this.mStoreClient) + " hasUserOptedIn: " + this.mHasUserOptedIn);
            if (ReqConstant.HAPPY_PATH_REQACCOUNT_GET_TC.equalsIgnoreCase(successfulAPICallResponseParam.mCallFlow)) {
                sendMessage(obtainMessage(2, new UIEventParam(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1, (String) null)));
            } else if (ReqConstant.HAPPY_PATH_REQACCOUNT_EXIST_USER.equalsIgnoreCase(successfulAPICallResponseParam.mCallFlow)) {
                sendMessage(obtainMessage(2, new UIEventParam(ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWoTerms_PrmpMsg4, (String) null)));
            } else {
                Log.i(this.TAG, "illegal returned callflow name");
            }
        }
    }

    private void handlerUIonSuccessProvisionAPI(SuccessfulAPICallResponseParam successfulAPICallResponseParam) {
        String str = this.TAG;
        Log.i(str, "handlerUIonSuccessProvisionAPI: " + successfulAPICallResponseParam);
        if (RequestAccount.class.getSimpleName().equals(successfulAPICallResponseParam.getApiName())) {
            if (this.mIsInternalRestart) {
                handleInternalRestart(successfulAPICallResponseParam);
            }
            if (!this.mHasUserOptedIn && !this.mIfSteadyState) {
                String str2 = this.TAG;
                Log.d(str2, "handlerUIonSuccessProvisionAPI: User has NOT opted in: isOOBE?: " + ProvisionHelper.isOOBE(this.mStoreClient) + " hasUserOptedIn: " + this.mHasUserOptedIn);
                if (ReqConstant.HAPPY_PATH_REQACCOUNT_GET_TC.equalsIgnoreCase(successfulAPICallResponseParam.mCallFlow)) {
                    ATTConstants.AttAmbsUIScreenNames attAmbsUIScreenNames = ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1;
                    saveLastScreenUserStopBackup(attAmbsUIScreenNames.getId());
                    displayOptIn(attAmbsUIScreenNames.getId());
                } else if (ReqConstant.HAPPY_PATH_REQACCOUNT_EXIST_USER.equalsIgnoreCase(successfulAPICallResponseParam.mCallFlow)) {
                    ATTConstants.AttAmbsUIScreenNames attAmbsUIScreenNames2 = ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWoTerms_PrmpMsg4;
                    saveLastScreenUserStopBackup(attAmbsUIScreenNames2.getId());
                    displayOptIn(attAmbsUIScreenNames2.getId());
                } else if (ReqConstant.HAPPY_PATH_BINARY_SMS_PROVISIONED.equalsIgnoreCase(successfulAPICallResponseParam.mCallFlow)) {
                    ATTConstants.AttAmbsUIScreenNames attAmbsUIScreenNames3 = ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWoTerms_PrmpMsg4;
                    saveLastScreenUserStopBackup(attAmbsUIScreenNames3.getId());
                    this.mStoreClient.getPrerenceManager().saveIfHasShownPopupOptIn(false);
                    displayOptIn(attAmbsUIScreenNames3.getId());
                } else if (ReqConstant.HAPPY_PATH_REQACCOUNT_GET_TBS_TC.equalsIgnoreCase(successfulAPICallResponseParam.mCallFlow)) {
                    this.mStoreClient.getPrerenceManager().saveUserTbsRquired(true);
                    ATTConstants.AttAmbsUIScreenNames attAmbsUIScreenNames4 = ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWithTerms_PrmptMsg3;
                    saveLastScreenUserStopBackup(attAmbsUIScreenNames4.getId());
                    displayOptIn(attAmbsUIScreenNames4.getId());
                } else {
                    Log.i(this.TAG, "illegal returned callflow name");
                }
            } else if (ReqConstant.HAPPY_PATH_REQACCOUNT_EXIST_USER.equalsIgnoreCase(successfulAPICallResponseParam.mCallFlow)) {
                Log.d(this.TAG, "handlerUIonSuccessProvisionAPI: HAPPY_PATH_REQ_ACCOUNT_EXIST_USER");
                update(EnumProvision.ProvisionEventType.REQ_HUI_TOKEN.getId());
            } else if (ReqConstant.HAPPY_PATH_REQACCOUNT_GET_TC.equalsIgnoreCase(successfulAPICallResponseParam.mCallFlow)) {
                Log.d(this.TAG, "handlerUIonSuccessProvisionAPI: NEW_USER");
                if (this.mStoreClient.getPrerenceManager().isHUI6014Err()) {
                    Log.d(this.TAG, "handlerUIonSuccessProvisionAPI: SOC removal");
                    this.mIWorkingStatusProvisionListener.onCloudSyncWorkingStopped();
                    saveNewUserOptInCase(EnumProvision.NewUserOptInCase.DELETE.getId());
                    notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.NewUserOptIn_PrmptMsg1.getId(), 0);
                } else {
                    update(EnumProvision.ProvisionEventType.REQ_GET_TC.getId());
                }
            } else {
                Log.d(this.TAG, "handlerUIonSuccessProvisionAPI: TBS_TC");
                update(EnumProvision.ProvisionEventType.REQ_HUI_TOKEN.getId());
            }
        }
        if (RequestHUIToken.class.getSimpleName().equals(successfulAPICallResponseParam.getApiName())) {
            Log.i(this.TAG, "handlerUIonSuccessProvisionAPI: RequestHUIToken API success");
            if (this.mIfSteadyState && !this.mHasUserDeleteAccount) {
                notifyMsgAppNonPopup(ATTConstants.AttAmbsUIScreenNames.SteadyState_PrmptMsg5.getId(), 0);
            }
        }
    }

    private void displayOptIn(int i) {
        boolean hasShownPopupOptIn = this.mStoreClient.getPrerenceManager().hasShownPopupOptIn();
        String str = this.TAG;
        Log.d(str, "displayOptIn: hasShownPopUpOptIn? : " + hasShownPopupOptIn + " mHasUserOptedIn:" + this.mHasUserOptedIn);
        if (!hasShownPopupOptIn) {
            if (ProvisionHelper.isOOBE(this.mStoreClient) || !this.mHasUserOptedIn) {
                this.mUIInterface.notifyAppUIScreen(i, IUIEventCallback.POP_UP, 0);
                this.mStoreClient.getPrerenceManager().saveIfHasShownPopupOptIn(true);
                notifyMsgAppNonPopup(i, 0);
                return;
            }
            Log.d(this.TAG, "handlerUIonSuccessProvisionAPI: !isOOBE && UserHasOptedIn - impossible here");
        } else if (ProvisionHelper.isOOBE(this.mStoreClient) || !this.mHasUserOptedIn) {
            notifyMsgAppNonPopup(i, 0);
        } else {
            Log.d(this.TAG, "handlerUIonSuccessProvisionAPI: !OOBE && UserOptedIn");
        }
    }

    private void onProvisionAPIFail(FailedAPICallResponseParam failedAPICallResponseParam) {
        String str = this.TAG;
        Log.i(str, "onProvisionAPIFail: " + failedAPICallResponseParam);
        handlerUIonFailedProvisionAPI(failedAPICallResponseParam);
        String str2 = failedAPICallResponseParam.mErrorCode;
        if (str2 != null) {
            ErrorRuleHandling.handleErrorCode(this.mStoreClient, this, failedAPICallResponseParam.mRequest, str2, this.mIRetryStackAdapterHelper, this.mICloudMessageManagerHelper);
            return;
        }
        ErrorRuleHandling.handleErrorCode(this.mStoreClient, this, failedAPICallResponseParam.mRequest, this.mIRetryStackAdapterHelper, this.mICloudMessageManagerHelper);
    }

    private void handlerUIonFailedProvisionAPI(FailedAPICallResponseParam failedAPICallResponseParam) {
        String str = this.TAG;
        Log.i(str, "handlerUIonFailedProvisionAPI: all failed APIs should go here. param: " + failedAPICallResponseParam);
    }

    private void notifyMsgAppNonPopup(int i, int i2) {
        String str = this.TAG;
        Log.d(str, "screen to display: " + i);
        if (i == ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWithTerms_PrmptMsg3.getId() || i == ATTConstants.AttAmbsUIScreenNames.ExistingUserOptInWoTerms_PrmpMsg4.getId() || i == ATTConstants.AttAmbsUIScreenNames.SteadyState_PrmptMsg5.getId()) {
            saveLastScreenUserStopBackup(i);
        }
        saveLastScreen(i);
        this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg9.getId(), IUIEventCallback.NON_POP_UP, 0);
        if (i2 > 0) {
            this.mUIInterface.notifyAppUIScreen(i, IUIEventCallback.NON_POP_UP, i2);
        } else {
            this.mUIInterface.notifyAppUIScreen(i, IUIEventCallback.NON_POP_UP, 0);
        }
    }

    private void stopProvisioningAPIs() {
        Log.d(this.TAG, "stopProvisioningAPIs");
        for (int i = 1; i <= 4; i++) {
            removeMessages(i);
        }
    }

    private void saveNewUserOptInCase(int i) {
        this.mStoreClient.getPrerenceManager().saveNewUserOptInCase(i);
        this.mNewUserOptInCase = i;
    }

    private void saveUserOptedIn(boolean z) {
        this.mStoreClient.getPrerenceManager().saveUserOptedIn(z);
        this.mHasUserOptedIn = z;
    }

    private void saveIfSteadyState(boolean z) {
        this.mStoreClient.getPrerenceManager().saveIfSteadyState(z);
        this.mIfSteadyState = z;
    }

    private void saveLastScreen(int i) {
        this.mStoreClient.getPrerenceManager().saveLastScreen(i);
        this.mLastUIScreen = ATTConstants.AttAmbsUIScreenNames.valueOf(i);
    }

    private void saveLastScreenUserStopBackup(int i) {
        this.mStoreClient.getPrerenceManager().saveLastScreenUserStopBackup(i);
        this.mLastScreenUserStopBackup = ATTConstants.AttAmbsUIScreenNames.valueOf(i);
    }

    private void saveUserDeleteAccount(boolean z) {
        this.mStoreClient.getPrerenceManager().saveUserDeleteAccount(z);
        this.mHasUserDeleteAccount = z;
    }

    private void logCurrentWorkingStatus() {
        String str = this.TAG;
        Log.d(str, "logCurrentWorkingStatus: [mLastSavedMessageIdAfterStop: " + this.mLastSavedMessageIdAfterStop + " mPaused: " + this.mPaused + " mNewUserOptInCase: " + this.mNewUserOptInCase + " mIfSteadyState: " + this.mIfSteadyState + " mHasUserOptedIn: " + this.mHasUserOptedIn + " mLastUIScreen: " + this.mLastUIScreen + " mLastScreenUserStopBackup: " + this.mLastScreenUserStopBackup + " mHasUserDeleteAccount: " + this.mHasUserDeleteAccount + "]");
    }

    public void onGoToEvent(int i, Object obj) {
        EnumProvision.ProvisionEventType valueOf = EnumProvision.ProvisionEventType.valueOf(i);
        String str = this.TAG;
        Log.i(str, "onGoToEvent: " + valueOf);
        sendMessage(obtainMessage(1, valueOf));
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
        sendMessage(obtainMessage(3, new SuccessfulAPICallResponseParam(iHttpAPICommonInterface, str)));
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
        sendMessage(obtainMessage(3, new SuccessfulAPICallResponseParam(iHttpAPICommonInterface, (String) null)));
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
        sendMessage(obtainMessage(4, new FailedAPICallResponseParam(iHttpAPICommonInterface, str)));
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam) {
        sendMessage(obtainMessage(4, new FailedAPICallResponseParam(iHttpAPICommonInterface, (String) null)));
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
        sendMessage(obtainMessage(4, new FailedAPICallResponseParam(iHttpAPICommonInterface, (String) null)));
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i) {
        ErrorRuleHandling.handleErrorHeader(this.mStoreClient, this, iHttpAPICommonInterface, str, i, this.mIRetryStackAdapterHelper, this.mICloudMessageManagerHelper);
    }

    public void onFixedFlow(int i) {
        EnumProvision.ProvisionEventType valueOf = EnumProvision.ProvisionEventType.valueOf(i);
        String str = this.TAG;
        Log.i(str, "onFixedFlow: " + valueOf);
        sendMessage(obtainMessage(1, valueOf));
    }

    public void start() {
        Log.i(this.TAG, VcidEvent.BUNDLE_VALUE_ACTION_START);
        updateDelay(EnumProvision.ProvisionEventType.CHK_INITIAL_STATE.getId(), 5000);
    }

    public void resume() {
        Log.i(this.TAG, "resume");
        sendMessage(obtainMessage(5));
    }

    public void pause() {
        Log.i(this.TAG, "pause");
        sendMessage(obtainMessage(6));
    }

    public void pauseService() {
        Log.i(this.TAG, "pauseService");
        sendMessage(obtainMessage(8));
    }

    public void stop() {
        Log.i(this.TAG, "stopService");
        sendMessage(obtainMessage(7));
    }

    public boolean update(int i) {
        EnumProvision.ProvisionEventType valueOf = EnumProvision.ProvisionEventType.valueOf(i);
        String str = this.TAG;
        Log.i(str, "update: " + valueOf);
        return sendMessage(obtainMessage(1, valueOf));
    }

    public boolean updateDelay(int i, long j) {
        EnumProvision.ProvisionEventType valueOf = EnumProvision.ProvisionEventType.valueOf(i);
        String str = this.TAG;
        Log.i(str, "update with " + valueOf + " delayed " + j);
        return sendMessageDelayed(obtainMessage(1, valueOf), j);
    }

    public boolean updateDelayRetry(int i, long j) {
        this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg11.getId(), IUIEventCallback.NON_POP_UP, 0);
        EnumProvision.ProvisionEventType valueOf = EnumProvision.ProvisionEventType.valueOf(i);
        String str = this.TAG;
        Log.i(str, "update with " + valueOf + " delayed retry " + j);
        return sendMessageDelayed(obtainMessage(1, valueOf), j);
    }

    public void onOmaFailExceedMaxCount() {
        IUIEventCallback iUIEventCallback = this.mUIInterface;
        ATTConstants.AttAmbsUIScreenNames attAmbsUIScreenNames = ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7;
        iUIEventCallback.notifyAppUIScreen(attAmbsUIScreenNames.getId(), IUIEventCallback.POP_UP, 0);
        this.mUIInterface.showInitsyncIndicator(false);
        notifyMsgAppNonPopup(attAmbsUIScreenNames.getId(), 0);
        saveLastScreen(attAmbsUIScreenNames.getId());
    }

    private void initSharedPreference() {
        CloudMessagePreferenceManager prerenceManager = this.mStoreClient.getPrerenceManager();
        prerenceManager.saveOMAChannelResURL("");
        prerenceManager.saveOMAChannelURL("");
        prerenceManager.saveOMACallBackURL("");
        prerenceManager.saveOMAChannelCreateTime(0);
        prerenceManager.saveOMAChannelLifeTime(0);
        prerenceManager.clearOMASubscriptionChannelDuration();
        prerenceManager.clearOMASubscriptionTime();
    }

    public void onMailBoxMigrationReset() {
        Log.i(this.TAG, "onMailBoxMigrationReset.");
        this.mIWorkingStatusProvisionListener.onMailBoxMigrationReset();
    }

    private void handleProvisionErr() {
        String str = this.TAG;
        Log.d(str, "handleProvisionErr, TBS Case:" + this.mStoreClient.getPrerenceManager().getUserTbs());
        if (!this.mStoreClient.getPrerenceManager().getUserTbs()) {
            this.mUIInterface.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
        } else {
            this.mStoreClient.getPrerenceManager().saveUserTbsRquired(false);
        }
    }

    public void resetDataReceiver() {
        Log.d(this.TAG, "reset DataSmsReceiver ");
        unregisterDataSmsReceiver();
        registerDataSmsReceiver();
    }
}
