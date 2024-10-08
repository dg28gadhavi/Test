package com.sec.internal.ims.cmstore;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.TelephonyCallback;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.ims.cmstore.adapters.DeviceConfigAdapter;
import com.sec.internal.ims.cmstore.ambs.CmsServiceModuleManager;
import com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils;
import com.sec.internal.ims.cmstore.ambs.provision.ProvisionController;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.helper.SyncParam;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.VvmHandler;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateLargeDataPolling;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.ParamNetAPIStatusControl;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.CheckCaptivePortal;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.gba.GbaServiceModule;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.ILineStatusChangeCallBack;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NetAPIWorkingStatusController extends Handler implements IWorkingStatusProvisionListener, IDeviceDataChangeListener, ILineStatusChangeCallBack {
    public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    public static final String AUTO_DOWNLOAD_SIM_0 = "auto_download_sim0";
    public static final String AUTO_DOWNLOAD_SIM_1 = "auto_download_sim1";
    protected static final int EVENT_AIRPLANEMODE_CHANGED = 8;
    private static final int EVENT_CHANGE_MSG_APP_WORKING_STATUS = 3;
    private static final int EVENT_CHANGE_OMANETAPI_WORKING_STATUS = 4;
    protected static final int EVENT_CMS_DEREGISTRATION_COMPLETED = 13;
    protected static final int EVENT_CMS_REGISTRATION_COMPLETED = 12;
    private static final int EVENT_DELETE_ACCOUNT = 7;
    private static final int EVENT_ENABLE_GBA_MODULE = 11;
    protected static final int EVENT_MESSAGE_APP_CHANGED = 1;
    protected static final int EVENT_NETWORK_CHANGE_DETECTED = 2;
    protected static final int EVENT_RECEIVE_FCM_PUSH_NOTIFICATION = 17;
    protected static final int EVENT_RECEIVE_FCM_REGISTRATION_TOKEN = 15;
    protected static final int EVENT_RECEIVE_TOKEN_VALIDITY_TIMEOUT = 18;
    protected static final int EVENT_REFRESH_FCM_REGISTRATION_TOKEN = 16;
    private static final int EVENT_REGISTER_PHONE_LISTENER = 9;
    protected static final int EVENT_REQUEST_FCM_REGISTRATION_TOKEN = 14;
    private static final int EVENT_SIM_STATE_CHANGED = 10;
    private static final int EVENT_STOP_ALL_TASK = 6;
    private final String LOG_TAG_CN = NetAPIWorkingStatusController.class.toString();
    public String TAG = NetAPIWorkingStatusController.class.getSimpleName();
    protected AutoDownloadContentObserver mAutoDownloadContentObserver = null;
    private IUIEventCallback mCallbackMsgApp;
    private CloudMessageManagerHelper mCloudMessageManagerHelper;
    protected Context mContext;
    private DeviceConfigAdapter mDeviceConfigAdapter;
    private GbaServiceModule mGbaServiceModule = null;
    protected boolean mHasNotifiedBufferDBProvisionSuccess = false;
    private IRetryStackAdapterHelper mIRetryStackAdapterHelper;
    private IImsFramework mImsFramework;
    protected boolean mIsAirPlaneModeOn = false;
    private boolean mIsAmbsServiceStop = false;
    protected boolean mIsCMNWorkingStarted = false;
    protected boolean mIsCmsProfileEnabled = false;
    protected boolean mIsDefaultMsgAppNative = true;
    private boolean mIsMsgAppForeground = false;
    protected boolean mIsNetworkValid = true;
    protected boolean mIsOMAAPIRunning = false;
    private boolean mIsProvisionSuccess = false;
    private boolean mIsServicePaused = false;
    private boolean mIsUserDeleteAccount = false;
    /* access modifiers changed from: private */
    public boolean mIsUsingMobileHipri = false;
    protected boolean mIsWifiConnected = false;
    private LineManager mLineManager;
    private MobileNetowrkCallBack mMobileNetworkCallback = null;
    protected OMANetAPIHandler mNetAPIHandler;
    private final ConnectivityManager.NetworkCallback mNetworkStateListener = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            String str = NetAPIWorkingStatusController.this.TAG;
            Log.i(str, "onAvailable " + network);
            NetAPIWorkingStatusController netAPIWorkingStatusController = NetAPIWorkingStatusController.this;
            netAPIWorkingStatusController.sendMessage(netAPIWorkingStatusController.obtainMessage(2));
        }

        public void onLost(Network network) {
            String str = NetAPIWorkingStatusController.this.TAG;
            Log.i(str, "onLost + " + network);
            NetAPIWorkingStatusController netAPIWorkingStatusController = NetAPIWorkingStatusController.this;
            netAPIWorkingStatusController.sendMessage(netAPIWorkingStatusController.obtainMessage(2));
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            String str = NetAPIWorkingStatusController.this.TAG;
            Log.i(str, "onCapabilitiesChanged" + networkCapabilities);
            NetAPIWorkingStatusController netAPIWorkingStatusController = NetAPIWorkingStatusController.this;
            netAPIWorkingStatusController.sendMessage(netAPIWorkingStatusController.obtainMessage(2));
        }
    };
    private ProvisionController mProvisionControl;
    private boolean mPushNotiPaused = false;
    protected MessageStoreClient mStoreClient;
    private TelephonyCallback mTelephonyCallback = null;
    private VvmHandler mVvmHandler;
    final ConnectivityManager.NetworkCallback mWifiStateListener = new ConnectivityManager.NetworkCallback() {
        public void onLost(Network network) {
            IMSLog.i(NetAPIWorkingStatusController.this.TAG, "onLost wifi");
            NetAPIWorkingStatusController.this.mIsWifiConnected = false;
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            Log.d(NetAPIWorkingStatusController.this.TAG, "onCapabilitiesChanged");
            if (networkCapabilities != null && networkCapabilities.hasCapability(12) && networkCapabilities.hasCapability(16) && networkCapabilities.hasTransport(1)) {
                NetAPIWorkingStatusController netAPIWorkingStatusController = NetAPIWorkingStatusController.this;
                netAPIWorkingStatusController.mIsWifiConnected = true;
                String str = netAPIWorkingStatusController.TAG;
                Log.i(str, "onCapabilitiesChanged mIsWifiConnected: " + NetAPIWorkingStatusController.this.mIsWifiConnected);
            }
        }
    };
    protected final RegistrantList mWorkingStatus = new RegistrantList();

    public void onChannelLifetimeUpdateComplete() {
    }

    public void onDeviceFlagUpdateSchedulerStarted() {
    }

    public void onStartFcmRetry() {
    }

    public void resetMcsRestartReceiver() {
    }

    public void registerForUpdateFromCloud(Handler handler, int i, Object obj) {
        this.mNetAPIHandler.registerForUpdateFromCloud(handler, i, obj);
        this.mVvmHandler.registerForUpdateFromCloud(handler, i, obj);
    }

    public void registerForUpdateOfWorkingStatus(Handler handler, int i, Object obj) {
        this.mWorkingStatus.add(new Registrant(handler, i, obj));
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public NetAPIWorkingStatusController(Looper looper, MessageStoreClient messageStoreClient, IUIEventCallback iUIEventCallback, IRetryStackAdapterHelper iRetryStackAdapterHelper, IImsFramework iImsFramework, GbaServiceModule gbaServiceModule) {
        super(looper);
        this.mStoreClient = messageStoreClient;
        this.mContext = messageStoreClient.getContext();
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mImsFramework = iImsFramework;
        this.mCallbackMsgApp = iUIEventCallback;
        this.mIRetryStackAdapterHelper = iRetryStackAdapterHelper;
        CloudMessageManagerHelper cloudMessageManagerHelper = new CloudMessageManagerHelper();
        this.mCloudMessageManagerHelper = cloudMessageManagerHelper;
        this.mGbaServiceModule = gbaServiceModule;
        this.mProvisionControl = new ProvisionController(this, looper, messageStoreClient, this.mCallbackMsgApp, this.mIRetryStackAdapterHelper, cloudMessageManagerHelper);
        LineManager lineManager = new LineManager(this);
        this.mLineManager = lineManager;
        OMANetAPIHandler oMANetAPIHandler = new OMANetAPIHandler(looper, this.mStoreClient, this, this.mCallbackMsgApp, lineManager, this.mCloudMessageManagerHelper);
        this.mNetAPIHandler = oMANetAPIHandler;
        Looper looper2 = looper;
        this.mVvmHandler = new VvmHandler(looper, this.mStoreClient, oMANetAPIHandler, this.mCloudMessageManagerHelper);
        this.mDeviceConfigAdapter = new DeviceConfigAdapter(messageStoreClient, this.mCloudMessageManagerHelper);
        this.mIsUserDeleteAccount = this.mStoreClient.getPrerenceManager().hasUserDeleteAccount();
        this.mIsAmbsServiceStop = this.mStoreClient.getPrerenceManager().getAMBSStopService();
        this.mIsServicePaused = this.mStoreClient.getPrerenceManager().getAMBSPauseService();
        registerDefaultSmsPackageChangeReceiver(this.mContext);
        registerAirplaneMode(this.mContext);
    }

    public NetAPIWorkingStatusController(Looper looper, MessageStoreClient messageStoreClient, IUIEventCallback iUIEventCallback) {
        super(looper);
        this.mStoreClient = messageStoreClient;
        this.mContext = messageStoreClient.getContext();
        this.mCallbackMsgApp = iUIEventCallback;
        this.mLineManager = new LineManager(this);
        CloudMessageManagerHelper cloudMessageManagerHelper = new CloudMessageManagerHelper();
        this.mCloudMessageManagerHelper = cloudMessageManagerHelper;
        this.mNetAPIHandler = new OMANetAPIHandler(looper, this.mStoreClient, this, this.mCallbackMsgApp, this.mLineManager, cloudMessageManagerHelper);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
    }

    public void resetAdapter(IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        this.mIRetryStackAdapterHelper = iRetryStackAdapterHelper;
        this.mProvisionControl.mIRetryStackAdapterHelper = iRetryStackAdapterHelper;
        this.mNetAPIHandler.resetChannelScheduler();
    }

    public void init() {
        initDeviceID();
        this.mStoreClient.getCloudMessageStrategyManager().createStrategy();
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isGbaSupported()) {
            sendMessage(obtainMessage(11));
            registerWifiStateListener();
            unregisterAutoDownloadSettingsObserver();
            registerAutoDownloadSettingsObserver();
            changeAndSaveAutoDownloadSettings();
        }
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isSupportExpiredRule()) {
            sendMessage(obtainMessage(9));
        }
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isProvisionRequired()) {
            startProvsioningApi();
        } else {
            initSimInfo();
            setVVMSyncState(false);
        }
        setConfigParam();
        registerNetworkStateListener();
        setNetworkCapabilities();
    }

    private void setNetworkCapabilities() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if (networkCapabilities != null && networkCapabilities.hasCapability(12) && networkCapabilities.hasCapability(16)) {
            if (networkCapabilities.hasTransport(1)) {
                this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setProtocol(OMAGlobalVariables.HTTPS);
            } else if (networkCapabilities.hasTransport(0)) {
                this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setProtocol(OMAGlobalVariables.HTTP);
            }
        }
    }

    private void setConfigParam() {
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isGbaSupported() && this.mStoreClient.getRetryMapAdapter() != null) {
            this.mStoreClient.getRetryMapAdapter().clearRetryHistory();
        }
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isDeviceConfigUsed()) {
            boolean z = this.mDeviceConfigAdapter.getDeviceConfig() != null;
            if (z) {
                this.mDeviceConfigAdapter.parseDeviceConfig();
            }
            this.mDeviceConfigAdapter.registerDeviceConfigUpdatedReceiver(this.mContext);
            String str = this.TAG;
            Log.d(str, "device config exists: " + z);
            this.mVvmHandler.resetDateFormat();
        }
    }

    private void initSimInfo() {
        String str;
        if (CmsUtil.isSimChanged(this.mStoreClient)) {
            this.mStoreClient.getPrerenceManager().clearAll();
            resetServiceState();
        }
        String convertPhoneNumberToUserAct = AmbsUtils.convertPhoneNumberToUserAct(this.mStoreClient.getSimManager().getMsisdn());
        String imsi = this.mStoreClient.getSimManager().getImsi();
        if (TextUtils.isEmpty(convertPhoneNumberToUserAct)) {
            convertPhoneNumberToUserAct = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getNativeLine();
            str = "from DB == ";
        } else {
            str = "== ";
        }
        String str2 = this.TAG;
        Log.i(str2, "Phone number " + str + IMSLog.checker(convertPhoneNumberToUserAct) + ", Provision not required");
        this.mStoreClient.getPrerenceManager().saveSimImsi(imsi);
        this.mStoreClient.getPrerenceManager().saveUserCtn(convertPhoneNumberToUserAct, false);
    }

    /* access modifiers changed from: protected */
    public void registerDefaultSmsPackageChangeReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED_INTERNAL");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    Log.w(NetAPIWorkingStatusController.this.TAG, "registerDefaultSmsPackageChangeReceiver, onReceive: intent is null.");
                    return;
                }
                String action = intent.getAction();
                String str = NetAPIWorkingStatusController.this.TAG;
                Log.d(str, "registerDefaultSmsPackageChangeReceiver, onReceive: anction = " + action);
                if ("android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED_INTERNAL".equals(action)) {
                    NetAPIWorkingStatusController.this.sendEmptyMessage(1);
                }
            }
        }, intentFilter);
    }

    /* access modifiers changed from: protected */
    public void registerAirplaneMode(Context context) {
        boolean z = Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
        this.mIsAirPlaneModeOn = z;
        if (z) {
            this.mIsNetworkValid = false;
        } else {
            this.mIsNetworkValid = true;
        }
        logCurrentWorkingStatus();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImsConstants.Intents.ACTION_AIRPLANE_MODE);
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String str = NetAPIWorkingStatusController.this.TAG;
                Log.d(str, "registerAirplaneMode, BroadcastReceiver, action: " + action);
                if (ImsConstants.Intents.ACTION_AIRPLANE_MODE.equals(action)) {
                    NetAPIWorkingStatusController netAPIWorkingStatusController = NetAPIWorkingStatusController.this;
                    netAPIWorkingStatusController.sendMessage(netAPIWorkingStatusController.obtainMessage(8));
                } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                    NetAPIWorkingStatusController netAPIWorkingStatusController2 = NetAPIWorkingStatusController.this;
                    netAPIWorkingStatusController2.sendMessage(netAPIWorkingStatusController2.obtainMessage(10));
                }
            }
        }, intentFilter);
    }

    private void registerPhoneStateListener(Context context) {
        Log.d(this.TAG, "registerPhoneStateListener");
        this.mTelephonyCallback = new TelephonyServiceCallback();
        Util.getTelephonyManager(context, this.mStoreClient.getClientID()).registerTelephonyCallback(context.getMainExecutor(), this.mTelephonyCallback);
    }

    public class TelephonyServiceCallback extends TelephonyCallback implements TelephonyCallback.ServiceStateListener {
        public TelephonyServiceCallback() {
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            String str = NetAPIWorkingStatusController.this.TAG;
            Log.i(str, "onServiceStateChanged " + serviceState.getState());
            if (serviceState.getState() == 0 || Util.isWifiCallingEnabled(NetAPIWorkingStatusController.this.mContext)) {
                NetAPIWorkingStatusController.this.mStoreClient.getPrerenceManager().saveNetworkAvailableTime(System.currentTimeMillis());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleEventMessageAppChanged() {
        logCurrentWorkingStatus();
        if (!this.mIsCmsProfileEnabled) {
            Log.d(this.TAG, "handleEventMessageAppChanged: not enabled");
            return;
        }
        this.mIsDefaultMsgAppNative = CmsUtil.isDefaultMessageAppInUse(this.mContext);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isGoForwardSyncSupported()) {
            if (!this.mIsDefaultMsgAppNative) {
                Log.d(this.TAG, "handleEventMessageAppChanged: native message app not default");
                this.mStoreClient.getPrerenceManager().saveNativeMsgAppIsDefault(false);
                setOMANetAPIWorkingStatus(false);
                this.mNetAPIHandler.deleteNotificationSubscriptionResource();
                pauseProvsioningApi();
                return;
            }
            Log.d(this.TAG, "handleEventMessageAppChanged native message app default");
            this.mStoreClient.getPrerenceManager().saveNativeMsgAppIsDefault(true);
            resumeProvsioningApi();
            Log.i(this.TAG, "notify buffer DB");
            this.mWorkingStatus.notifyRegistrants(new AsyncResult((Object) null, IWorkingStatusProvisionListener.WorkingStatus.DEFAULT_MSGAPP_CHGTO_NATIVE, (Throwable) null));
            if (shouldEnableOMANetAPIWorking()) {
                Log.d(this.TAG, "handleEventMessageAppChanged: default msg app, resume cms api working");
                setOMANetAPIWorkingStatus(true);
            }
        }
    }

    public boolean isNativeMsgAppDefault() {
        return this.mIsDefaultMsgAppNative;
    }

    /* JADX WARNING: Removed duplicated region for block: B:60:0x0198  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x019a  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x01a1  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x01f6 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0251  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0263  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(android.os.Message r10) {
        /*
            r9 = this;
            super.handleMessage(r10)
            int r0 = r10.what
            r9.removeMessages(r0)
            java.lang.String r0 = r9.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "message: "
            r1.append(r2)
            int r2 = r10.what
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            int r0 = r10.what
            java.lang.String r1 = "airplane_mode_on"
            r2 = 1
            r3 = 0
            switch(r0) {
                case 1: goto L_0x0267;
                case 2: goto L_0x0134;
                case 3: goto L_0x0113;
                case 4: goto L_0x00eb;
                case 5: goto L_0x0029;
                case 6: goto L_0x00e6;
                case 7: goto L_0x00c5;
                case 8: goto L_0x008a;
                case 9: goto L_0x0083;
                case 10: goto L_0x0030;
                case 11: goto L_0x002b;
                default: goto L_0x0029;
            }
        L_0x0029:
            goto L_0x026a
        L_0x002b:
            r9.enableGbaModule()
            goto L_0x026a
        L_0x0030:
            android.content.Context r10 = r9.mContext
            com.sec.internal.ims.cmstore.MessageStoreClient r0 = r9.mStoreClient
            int r0 = r0.getClientID()
            android.telephony.TelephonyManager r10 = com.sec.internal.ims.cmstore.utils.Util.getTelephonyManager(r10, r0)
            int r10 = r10.getSimState()
            java.lang.String r0 = r9.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "sim state changed, state: "
            r1.append(r2)
            r1.append(r10)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            android.content.Context r0 = r9.mContext
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = r9.mStoreClient
            int r1 = r1.getClientID()
            com.sec.internal.ims.cmstore.utils.Util.getTelephonyManager(r0, r1)
            r0 = 5
            if (r0 != r10) goto L_0x026a
            com.sec.internal.ims.cmstore.MessageStoreClient r10 = r9.mStoreClient
            boolean r10 = com.sec.internal.ims.cmstore.utils.CmsUtil.isSimChanged(r10)
            com.sec.internal.ims.cmstore.MessageStoreClient r0 = r9.mStoreClient
            boolean r1 = r9.mIsCmsProfileEnabled
            boolean r0 = com.sec.internal.ims.cmstore.utils.CmsUtil.isCtnChangedByNetwork(r0, r1)
            if (r10 != 0) goto L_0x026a
            if (r0 == 0) goto L_0x026a
            java.lang.String r10 = r9.TAG
            java.lang.String r0 = "ctn changed, restart service"
            android.util.Log.d(r10, r0)
            r9.onRestartService()
            goto L_0x026a
        L_0x0083:
            android.content.Context r10 = r9.mContext
            r9.registerPhoneStateListener(r10)
            goto L_0x026a
        L_0x008a:
            com.sec.internal.ims.cmstore.MessageStoreClient r10 = r9.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r10 = r10.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r10 = r10.getStrategy()
            boolean r10 = r10.isAirplaneModeChangeHandled()
            if (r10 == 0) goto L_0x026a
            android.content.Context r10 = r9.mContext
            android.content.ContentResolver r10 = r10.getContentResolver()
            int r10 = android.provider.Settings.System.getInt(r10, r1, r3)
            if (r10 == 0) goto L_0x00a7
            goto L_0x00a8
        L_0x00a7:
            r2 = r3
        L_0x00a8:
            r9.mIsAirPlaneModeOn = r2
            if (r2 == 0) goto L_0x00b6
            r9.mIsWifiConnected = r3
            r9.setNetworkStatus(r3)
            r9.setOMANetAPIWorkingStatus(r3)
            goto L_0x026a
        L_0x00b6:
            com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler r10 = r9.mNetAPIHandler
            r10.resetChannelState()
            r10 = 2
            android.os.Message r10 = r9.obtainMessage(r10)
            r9.sendMessage(r10)
            goto L_0x026a
        L_0x00c5:
            java.lang.Object r10 = r10.obj
            java.lang.Boolean r10 = (java.lang.Boolean) r10
            boolean r10 = r10.booleanValue()
            r9.mIsUserDeleteAccount = r10
            r9.logCurrentWorkingStatus()
            boolean r10 = r9.mIsUserDeleteAccount
            if (r10 == 0) goto L_0x00db
            r9.stopCMNWorking()
            goto L_0x026a
        L_0x00db:
            boolean r10 = r9.shouldEnableOMANetAPIWorking()
            if (r10 == 0) goto L_0x026a
            r9.setOMANetAPIWorkingStatus(r2)
            goto L_0x026a
        L_0x00e6:
            r9.stopCMNWorking()
            goto L_0x026a
        L_0x00eb:
            java.lang.Object r10 = r10.obj
            java.lang.Boolean r10 = (java.lang.Boolean) r10
            boolean r10 = r10.booleanValue()
            boolean r0 = r9.isCmsProfileActive()
            if (r0 != 0) goto L_0x0103
            if (r10 == 0) goto L_0x0103
            java.lang.String r9 = r9.TAG
            java.lang.String r10 = "isCmsProfileActive: false"
            android.util.Log.d(r9, r10)
            return
        L_0x0103:
            if (r10 == 0) goto L_0x010c
            r9.mIsOMAAPIRunning = r2
            r9.resumeCMNWorking()
            goto L_0x026a
        L_0x010c:
            r9.mIsOMAAPIRunning = r3
            r9.pauseCMNWorking()
            goto L_0x026a
        L_0x0113:
            r9.logCurrentWorkingStatus()
            java.lang.Object r10 = r10.obj
            java.lang.Boolean r10 = (java.lang.Boolean) r10
            boolean r10 = r10.booleanValue()
            r9.mIsMsgAppForeground = r10
            if (r10 != 0) goto L_0x0126
            r9.setOMANetAPIWorkingStatus(r3)
            goto L_0x0129
        L_0x0126:
            r9.setNotiPauseState(r3)
        L_0x0129:
            boolean r10 = r9.shouldEnableOMANetAPIWorking()
            if (r10 == 0) goto L_0x026a
            r9.setOMANetAPIWorkingStatus(r2)
            goto L_0x026a
        L_0x0134:
            android.content.Context r10 = r9.mContext
            java.lang.String r0 = "connectivity"
            java.lang.Object r10 = r10.getSystemService(r0)
            android.net.ConnectivityManager r10 = (android.net.ConnectivityManager) r10
            android.net.Network r0 = r10.getActiveNetwork()
            if (r0 != 0) goto L_0x014c
            java.lang.String r9 = r9.TAG
            java.lang.String r10 = "network is null"
            com.sec.internal.log.IMSLog.e(r9, r10)
            return
        L_0x014c:
            android.net.NetworkCapabilities r10 = r10.getNetworkCapabilities(r0)
            java.lang.String r4 = r9.TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "NetworkCapabilities: "
            r5.append(r6)
            r5.append(r10)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.i(r4, r5)
            if (r10 == 0) goto L_0x018a
            r4 = 12
            boolean r4 = r10.hasCapability(r4)
            if (r4 == 0) goto L_0x018a
            r4 = 16
            boolean r4 = r10.hasCapability(r4)
            if (r4 == 0) goto L_0x018a
            boolean r4 = r10.hasTransport(r3)
            if (r4 == 0) goto L_0x0181
            r4 = r2
            r10 = r3
            goto L_0x018c
        L_0x0181:
            boolean r10 = r10.hasTransport(r2)
            if (r10 == 0) goto L_0x018a
            r10 = r2
            r4 = r3
            goto L_0x018c
        L_0x018a:
            r10 = r3
            r4 = r10
        L_0x018c:
            android.content.Context r5 = r9.mContext
            android.content.ContentResolver r5 = r5.getContentResolver()
            int r1 = android.provider.Settings.System.getInt(r5, r1, r3)
            if (r1 == 0) goto L_0x019a
            r1 = r2
            goto L_0x019b
        L_0x019a:
            r1 = r3
        L_0x019b:
            r9.mIsAirPlaneModeOn = r1
            java.lang.String r5 = "is using MOBILE_HIPRI, will change to default network"
            if (r10 == 0) goto L_0x01f6
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = r9.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r1 = r1.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r1 = r1.getStrategy()
            java.lang.String r6 = "https"
            r1.setProtocol(r6)
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = r9.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r1 = r1.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r1 = r1.getStrategy()
            boolean r1 = r1.isCaptivePortalCheckSupported()
            java.lang.String r6 = r9.TAG
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "WiFi connected, needToCheckCaptive value: "
            r7.append(r8)
            r7.append(r1)
            java.lang.String r7 = r7.toString()
            android.util.Log.d(r6, r7)
            if (r1 == 0) goto L_0x021c
            boolean r0 = r9.checkingWifiGoodOrNot(r0)
            if (r0 == 0) goto L_0x01eb
            boolean r0 = r9.mIsUsingMobileHipri
            if (r0 == 0) goto L_0x01e8
            java.lang.String r0 = r9.TAG
            android.util.Log.d(r0, r5)
            r9.stopMobileHipri()
        L_0x01e8:
            java.lang.String r0 = "Good Wifi"
            goto L_0x01f0
        L_0x01eb:
            r9.startMobileHipri()
            java.lang.String r0 = "Bad Wifi"
        L_0x01f0:
            java.lang.String r1 = r9.TAG
            android.util.Log.d(r1, r0)
            goto L_0x021c
        L_0x01f6:
            if (r4 == 0) goto L_0x021c
            if (r1 != 0) goto L_0x021c
            com.sec.internal.ims.cmstore.MessageStoreClient r0 = r9.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r0.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
            java.lang.String r1 = "http"
            r0.setProtocol(r1)
            java.lang.String r0 = r9.TAG
            java.lang.String r1 = "WiFi not connected, but Mobile is connected"
            android.util.Log.d(r0, r1)
            boolean r0 = r9.mIsUsingMobileHipri
            if (r0 == 0) goto L_0x021c
            java.lang.String r0 = r9.TAG
            android.util.Log.d(r0, r5)
            r9.stopMobileHipri()
        L_0x021c:
            if (r4 != 0) goto L_0x0223
            if (r10 == 0) goto L_0x0221
            goto L_0x0223
        L_0x0221:
            r10 = r3
            goto L_0x0224
        L_0x0223:
            r10 = r2
        L_0x0224:
            java.lang.String r0 = r9.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r4 = "Network available: "
            r1.append(r4)
            r1.append(r10)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
            boolean r0 = r9.mIsNetworkValid
            if (r0 == 0) goto L_0x024c
            if (r10 != 0) goto L_0x024c
            java.lang.String r0 = r9.TAG
            java.lang.String r1 = "no available network, reset channel state."
            android.util.Log.d(r0, r1)
            com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler r0 = r9.mNetAPIHandler
            r0.resetChannelState()
        L_0x024c:
            r9.setNetworkStatus(r10)
            if (r10 == 0) goto L_0x0263
            boolean r10 = r9.shouldEnableOMANetAPIWorking()
            if (r10 == 0) goto L_0x026a
            java.lang.String r10 = r9.TAG
            java.lang.String r0 = "shouldEnableOMANetAPIWorking: true"
            android.util.Log.d(r10, r0)
            r9.setOMANetAPIWorkingStatus(r2)
            goto L_0x026a
        L_0x0263:
            r9.setOMANetAPIWorkingStatus(r3)
            goto L_0x026a
        L_0x0267:
            r9.handleEventMessageAppChanged()
        L_0x026a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.NetAPIWorkingStatusController.handleMessage(android.os.Message):void");
    }

    private void setNotiPauseState(boolean z) {
        String str = this.TAG;
        Log.i(str, "setNotiPauseState, currenty Paused:" + this.mPushNotiPaused + " newState:" + z);
        this.mPushNotiPaused = z;
    }

    /* access modifiers changed from: protected */
    public boolean shouldEnableOMANetAPIWorking() {
        return this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldEnableNetAPIWorking(this.mIsNetworkValid, this.mIsDefaultMsgAppNative, this.mIsUserDeleteAccount, this.mIsProvisionSuccess, this.mIsServicePaused);
    }

    /* access modifiers changed from: protected */
    public void pauseCMNWorking() {
        Log.d(this.TAG, "pause cloud message NetAPI");
        this.mNetAPIHandler.pausewithStatusParam(new ParamNetAPIStatusControl(this.mIsMsgAppForeground, this.mIsNetworkValid, this.mIsOMAAPIRunning, this.mIsDefaultMsgAppNative, this.mIsUserDeleteAccount, this.mIsProvisionSuccess, this.mIsServicePaused));
    }

    /* access modifiers changed from: protected */
    public void stopCMNWorking() {
        Log.d(this.TAG, "stop cloud message NetAPI");
        this.mIsCMNWorkingStarted = false;
        this.mIsProvisionSuccess = false;
        this.mNetAPIHandler.stop();
    }

    /* access modifiers changed from: protected */
    public void startCMNWorking() {
        Log.d(this.TAG, "start cloud message NetAPI");
        this.mNetAPIHandler.start();
    }

    private void startCMNWorkingResetBox() {
        Log.d(this.TAG, "start cloud message NetAPI: resetBox");
        this.mNetAPIHandler.start_resetBox();
    }

    /* access modifiers changed from: protected */
    public void resumeCMNWorking() {
        Log.d(this.TAG, "resume cloud message NetAPI");
        this.mNetAPIHandler.resumewithStatusParam(new ParamNetAPIStatusControl(this.mIsMsgAppForeground, this.mIsNetworkValid, this.mIsOMAAPIRunning, this.mIsDefaultMsgAppNative, this.mIsUserDeleteAccount, this.mIsProvisionSuccess, this.mIsServicePaused));
    }

    private void startProvsioningApi() {
        this.mProvisionControl.start();
    }

    private void pauseProvsioningApi() {
        Log.d(this.TAG, "pauseProvisioningApi");
        this.mProvisionControl.pause();
    }

    private void resumeProvsioningApi() {
        Log.d(this.TAG, "resumeProvisioningApi");
        if (isCmsProfileActive() && this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isProvisionRequired()) {
            this.mProvisionControl.resume();
        }
    }

    public void setMsgAppForegroundStatus(boolean z) {
        sendMessage(obtainMessage(3, Boolean.valueOf(z)));
    }

    public void setOMANetAPIWorkingStatus(boolean z) {
        sendMessage(obtainMessage(4, Boolean.valueOf(z)));
    }

    /* access modifiers changed from: protected */
    public void setNetworkStatus(boolean z) {
        if (this.mIsCmsProfileEnabled || !z) {
            this.mIsNetworkValid = z;
            if (!z || !this.mIsDefaultMsgAppNative) {
                pauseProvsioningApi();
            } else {
                resumeProvsioningApi();
            }
            if (this.mIsNetworkValid && !this.mIsCMNWorkingStarted && this.mIsProvisionSuccess && isCmsProfileActive()) {
                this.mIsCMNWorkingStarted = true;
                startCMNWorking();
                return;
            }
            return;
        }
        Log.d(this.TAG, "mIsCmsProfileEnabled: false");
    }

    public void sendDeviceUpdate(BufferDBChangeParamList bufferDBChangeParamList) {
        String str = this.TAG;
        Log.d(str, "sendDeviceUpdate: " + bufferDBChangeParamList);
        if (bufferDBChangeParamList != null && bufferDBChangeParamList.mChangelst.size() > 0 && this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isValidOMARequestUrl()) {
            BufferDBChangeParam bufferDBChangeParam = bufferDBChangeParamList.mChangelst.get(0);
            String str2 = this.TAG;
            Log.i(str2, "sendDeviceUpdate: mIsAdhocV2t: " + bufferDBChangeParam.mIsAdhocV2t);
            int i = bufferDBChangeParam.mDBIndex;
            if (i != 19 && i != 18 && i != 20 && i != 17) {
                this.mNetAPIHandler.sendUpdate(bufferDBChangeParamList);
            } else if ((i != 18 || !CloudMessageBufferDBConstants.ActionStatusFlag.Delete.equals(bufferDBChangeParam.mAction)) && (bufferDBChangeParam.mDBIndex != 17 || bufferDBChangeParam.mIsAdhocV2t)) {
                this.mVvmHandler.sendVvmUpdate(bufferDBChangeParamList);
            } else {
                this.mNetAPIHandler.sendUpdate(bufferDBChangeParamList);
            }
        }
    }

    public void onProvisionSuccess() {
        this.mIsProvisionSuccess = true;
        logCurrentWorkingStatus();
        if (shouldEnableOMANetAPIWorking()) {
            setOMANetAPIWorkingStatus(true);
        }
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isProvisionRequired()) {
            if (this.mIsNetworkValid && !this.mIsCMNWorkingStarted) {
                this.mIsCMNWorkingStarted = true;
                startCMNWorking();
            }
            startInitSync();
        }
    }

    public void onCleanBufferDbRequired() {
        Log.i(this.TAG, "onCleanBufferDbRequired");
        this.mWorkingStatus.notifyRegistrants(new AsyncResult((Object) null, IWorkingStatusProvisionListener.WorkingStatus.BUFFERDB_CLEAN, (Throwable) null));
    }

    public void onCmsRegistrationCompletedEvent() {
        Log.i(this.TAG, "onCmsRegistrationCompleted");
        this.mWorkingStatus.notifyRegistrants(new AsyncResult((Object) null, IWorkingStatusProvisionListener.WorkingStatus.UPDATE_CMS_CONFIG, (Throwable) null));
    }

    public void onInitialDBSyncCompleted() {
        this.mProvisionControl.update(EnumProvision.ProvisionEventType.REQ_CREATE_ACCOUNT.getId());
    }

    public void onInitialDBCopyDone() {
        Log.i(this.TAG, "onInitialDBCopyDone");
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isMultiLineSupported()) {
            this.mLineManager.initLineStatus();
        } else {
            this.mLineManager.addLine(this.mStoreClient.getPrerenceManager().getUserTelCtn());
        }
        logCurrentWorkingStatus();
        if (this.mIsNetworkValid && !this.mIsCMNWorkingStarted && this.mIsProvisionSuccess) {
            this.mIsCMNWorkingStarted = true;
            startCMNWorking();
        }
    }

    public void onMailBoxResetBufferDbDone() {
        Log.i(this.TAG, "onMailBoxResetBufferDbDone");
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isMultiLineSupported()) {
            this.mLineManager.initLineStatus();
        } else {
            this.mLineManager.addLine(this.mStoreClient.getPrerenceManager().getUserTelCtn());
        }
        this.mNetAPIHandler.deleteNotificationSubscriptionResource();
        logCurrentWorkingStatus();
        if (this.mIsNetworkValid && this.mIsProvisionSuccess) {
            this.mIsCMNWorkingStarted = true;
            startCMNWorkingResetBox();
        }
        if (shouldEnableOMANetAPIWorking()) {
            setOMANetAPIWorkingStatus(true);
        }
    }

    public boolean onUIButtonProceed(int i, String str) {
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isUIButtonUsed()) {
            Log.d(this.TAG, "UI button is enabled");
            return this.mProvisionControl.onUIButtonProceed(i, str);
        }
        Log.d(this.TAG, "UI button call is disabled");
        return false;
    }

    public void sendAppSync(SyncParam syncParam, boolean z) {
        if (z) {
            this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMPendingRequestCounts(true);
        }
        String str = this.TAG;
        Log.i(str, "sendAppSync: " + syncParam + " isFullSync: " + z);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isValidOMARequestUrl()) {
            this.mNetAPIHandler.sendAppSync(syncParam, z);
        }
    }

    public void stopAppSync(SyncParam syncParam) {
        String str = this.TAG;
        Log.i(str, "sendAppSync: " + syncParam);
        this.mNetAPIHandler.stopAppSync(syncParam);
    }

    public void sendDeviceInitialSyncDownload(BufferDBChangeParamList bufferDBChangeParamList) {
        String str = this.TAG;
        Log.i(str, "sendDeviceInitialSyncDownload: " + bufferDBChangeParamList);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isValidOMARequestUrl()) {
            this.mNetAPIHandler.sendInitialSyncDownload(bufferDBChangeParamList);
        }
    }

    public void sendGetVVMQuotaInfo(BufferDBChangeParamList bufferDBChangeParamList) {
        String str = this.TAG;
        Log.i(str, "sendGetVVMQuotaInfo : " + bufferDBChangeParamList);
        this.mVvmHandler.getVvmQuota(bufferDBChangeParamList);
    }

    public void onWipeOutResetSyncHandler() {
        Log.i(this.TAG, "onWipeOutResetSyncHandler");
        this.mIsCMNWorkingStarted = false;
        this.mNetAPIHandler.onWipeOutResetSyncHandler();
    }

    public void sendDeviceNormalSyncDownload(BufferDBChangeParamList bufferDBChangeParamList) {
        String str = this.TAG;
        Log.i(str, "sendDeviceNormalDownload: " + bufferDBChangeParamList);
        BufferDBChangeParamList bufferDBChangeParamList2 = new BufferDBChangeParamList();
        BufferDBChangeParamList bufferDBChangeParamList3 = new BufferDBChangeParamList();
        if (bufferDBChangeParamList != null && this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isValidOMARequestUrl()) {
            Iterator<BufferDBChangeParam> it = bufferDBChangeParamList.mChangelst.iterator();
            while (it.hasNext()) {
                BufferDBChangeParam next = it.next();
                if (next.mDBIndex == 20) {
                    bufferDBChangeParamList2.mChangelst.add(next);
                } else {
                    bufferDBChangeParamList3.mChangelst.add(next);
                }
            }
        }
        if (bufferDBChangeParamList3.mChangelst.size() > 0) {
            this.mNetAPIHandler.sendNormalSyncDownload(bufferDBChangeParamList);
        }
        if (bufferDBChangeParamList2.mChangelst.size() > 0) {
            this.mVvmHandler.sendVvmUpdate(bufferDBChangeParamList2);
        }
    }

    public void sendDeviceUpload(BufferDBChangeParamList bufferDBChangeParamList) {
        String str = this.TAG;
        Log.i(str, "sendDeviceUpload: " + bufferDBChangeParamList);
        if (bufferDBChangeParamList != null && this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isValidOMARequestUrl()) {
            this.mNetAPIHandler.sendUpload(bufferDBChangeParamList);
        }
    }

    public void onOmaProvisionFailed(ParamOMAresponseforBufDB paramOMAresponseforBufDB, long j) {
        String str = this.TAG;
        Log.d(str, "onOmaProvisionFailed: " + paramOMAresponseforBufDB);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isTokenRequestedFromProvision()) {
            this.mIsProvisionSuccess = false;
            setOMANetAPIWorkingStatus(false);
            String str2 = this.TAG;
            Log.d(str2, "REQ_SESSION_GEN will be triggered in " + (j / 1000) + " seconds");
            this.mCallbackMsgApp.notifyAppUIScreen(ATTConstants.AttAmbsUIScreenNames.Settings_PrmptMsg10.getId(), IUIEventCallback.NON_POP_UP, 0);
            this.mProvisionControl.updateDelay(EnumProvision.ProvisionEventType.REQ_SESSION_GEN.getId(), j);
        } else if (paramOMAresponseforBufDB != null) {
            String msisdn = Util.getMsisdn(paramOMAresponseforBufDB.getLine(), Util.getSimCountryCode(this.mContext, this.mStoreClient.getClientID()));
            if (!TextUtils.isEmpty(msisdn) && msisdn.length() > 1) {
                String substring = msisdn.substring(1);
                String str3 = this.TAG;
                IMSLog.s(str3, "line: " + substring);
                this.mContext.getContentResolver().update(NSDSContractExt.Lines.buildRefreshSitUri(substring), new ContentValues(), (String) null, (String[]) null);
            } else {
                return;
            }
        } else {
            return;
        }
        Class<? extends IHttpAPICommonInterface> lastFailedApi = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getLastFailedApi();
        if (lastFailedApi != null && CloudMessageCreateLargeDataPolling.class.getSimpleName().equalsIgnoreCase(lastFailedApi.getSimpleName()) && !this.mIsMsgAppForeground) {
            Log.i(this.TAG, "LargeDataPolling failed while app was in background. Stop all futher pushed notification");
            setNotiPauseState(true);
        }
    }

    public void onCloudSyncWorkingStopped() {
        clearData();
        stopCMNWorking();
    }

    public void onUserDeleteAccount(boolean z) {
        String str = this.TAG;
        Log.d(str, "onUserDeleteAccount: " + z);
        sendMessage(obtainMessage(7, Boolean.valueOf(z)));
    }

    public void onRestartService() {
        onRestartService(true);
    }

    public void onRestartService(boolean z) {
        if (this.mIsAmbsServiceStop) {
            Log.e(this.TAG, "AMBS service is suspended, do not process Restart Service");
            return;
        }
        String str = this.TAG;
        Log.i(str, "Entry restartService: userOptin " + z);
        setOMANetAPIWorkingStatus(false);
        clearData();
        stopCMNWorking();
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isGbaSupported()) {
            initSimInfo();
            setConfigParam();
        }
        if (!z) {
            Log.i(this.TAG, "Internal Restart case");
            if (this.mIsServicePaused) {
                this.mIsServicePaused = false;
                this.mStoreClient.getPrerenceManager().saveAMBSPauseService(false);
            }
            setCmsProfileEnabled(true);
            this.mProvisionControl.update(EnumProvision.ProvisionEventType.INTERNAL_RESTART.getId());
        } else {
            Log.i(this.TAG, "Restart case optin");
            this.mProvisionControl.update(EnumProvision.ProvisionEventType.RESTART_SERVICE.getId());
        }
        this.mWorkingStatus.notifyRegistrants(new AsyncResult((Object) null, IWorkingStatusProvisionListener.WorkingStatus.RESTART_SERVICE, (Throwable) null));
        initDeviceID();
    }

    public void onEsimHotswap() {
        int simState = Util.getTelephonyManager(this.mContext, this.mStoreClient.getClientID()).getSimState();
        String str = this.TAG;
        Log.i(str, "onEsimHotswap sim state: " + simState);
        Util.getTelephonyManager(this.mContext, this.mStoreClient.getClientID());
        if (5 != simState) {
            Log.i(this.TAG, "SIM not yet loaded, skip esim hotswap processing ");
            return;
        }
        clearData();
        stopCMNWorking();
        initDeviceID();
        this.mStoreClient.getCloudMessageStrategyManager().createStrategy();
        sendMessage(obtainMessage(11));
        changeAndSaveAutoDownloadSettings();
        initSimInfo();
        setVVMSyncState(false);
        setConfigParam();
        setNetworkCapabilities();
    }

    public void pauseService() {
        Log.i(this.TAG, "Entry pauseService");
        this.mIsServicePaused = true;
        setOMANetAPIWorkingStatus(false);
        this.mStoreClient.getPrerenceManager().saveAMBSPauseService(true);
        this.mProvisionControl.pauseService();
    }

    public void stopService() {
        this.mProvisionControl.stop();
        this.mIsServicePaused = true;
        stopCMNWorking();
        this.mStoreClient.getPrerenceManager().saveAMBSStopService(true);
        this.mIsAmbsServiceStop = true;
    }

    public void onChannelStateReset() {
        Log.d(this.TAG, "onChannelStateReset");
        this.mNetAPIHandler.resetChannelState();
    }

    /* access modifiers changed from: protected */
    public void clearData() {
        this.mStoreClient.getPrerenceManager().clearAll();
        resetServiceState();
        this.mStoreClient.getHttpController().getCookieJar().removeAll();
        if (this.mStoreClient.getRetryStackAdapter() != null) {
            this.mStoreClient.getRetryStackAdapter().clearRetryHistory();
        }
        if (this.mStoreClient.getRetryMapAdapter() != null) {
            this.mStoreClient.getRetryMapAdapter().clearRetryHistory();
        }
        onCleanBufferDbRequired();
        this.mStoreClient.getCloudMessageStrategyManager().getStrategy().clearOmaRetryData();
        this.mHasNotifiedBufferDBProvisionSuccess = false;
    }

    public void onNetworkChangeDetected() {
        Log.d(this.TAG, "onNetworkChangeDetected");
        boolean isCaptivePortalCheckSupported = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isCaptivePortalCheckSupported();
        if (!this.mIsCmsProfileEnabled || !isCaptivePortalCheckSupported) {
            String str = this.TAG;
            Log.d(str, "onNetworkChangeDetected: CmsProfileEnabled: " + this.mIsCmsProfileEnabled + " or captive portal:" + isCaptivePortalCheckSupported);
            return;
        }
        sendEmptyMessage(2);
    }

    public void setCmsProfileEnabled(boolean z) {
        String str = this.LOG_TAG_CN;
        int clientID = this.mStoreClient.getClientID();
        EventLogHelper.infoLogAndAdd(str, clientID, "setCmsProfileEnabled: mIsCmsProfileEnabled " + this.mIsCmsProfileEnabled + " Value :" + z);
        if (!this.mIsCmsProfileEnabled || !z) {
            this.mIsCmsProfileEnabled = z;
            if (z) {
                onNetworkChangeDetected();
                init();
                this.mIsDefaultMsgAppNative = CmsUtil.isDefaultMessageAppInUse(this.mContext);
                this.mStoreClient.getPrerenceManager().saveNativeMsgAppIsDefault(this.mIsDefaultMsgAppNative);
                if (!this.mIsDefaultMsgAppNative) {
                    Log.d(this.TAG, "setCmsProfileEnabled: non-default app: pause provisioning");
                    pauseProvsioningApi();
                    return;
                }
                return;
            }
            unregisterNetworkStateListener();
            stopCMNWorking();
            pauseProvsioningApi();
        }
    }

    public void setImpuFromImsRegistration(String str) {
        String str2 = this.TAG;
        Log.d(str2, "setImpuFromImsRegistration: " + IMSLog.checker(str) + ", shouldPersistImsRegNum value: " + this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldPersistImsRegNum());
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldPersistImsRegNum() && str != null && str.length() >= 10 && str.length() <= 12) {
            if (str.length() >= 11) {
                str = str.substring(str.length() - 10, str.length());
            }
            this.mStoreClient.getPrerenceManager().saveUserCtn(str, false);
        }
    }

    public boolean getCmsProfileEnabled() {
        String str = this.TAG;
        Log.i(str, "getCmsProfileEnabled mIsCmsProfileEnabled: " + this.mIsCmsProfileEnabled);
        return this.mIsCmsProfileEnabled;
    }

    public boolean isCmsProfileActive() {
        return this.mIsCmsProfileEnabled && !this.mIsAmbsServiceStop && !this.mIsServicePaused && this.mStoreClient.getPrerenceManager().getMcsUser() != 0;
    }

    public boolean getCmsIsAccountServiceStop() {
        return this.mIsAmbsServiceStop;
    }

    public boolean getCmsIsAccountServicePause() {
        return this.mIsServicePaused;
    }

    public void resetServiceState() {
        this.mIsAmbsServiceStop = this.mStoreClient.getPrerenceManager().getAMBSStopService();
        this.mIsServicePaused = this.mStoreClient.getPrerenceManager().getAMBSPauseService();
    }

    private void logCurrentWorkingStatus() {
        String str = this.TAG;
        Log.i(str, "logCurrentWorkingStatus:  mIsUsingMobileHipri: " + this.mIsUsingMobileHipri + " mIsAmbsRunning: " + this.mIsOMAAPIRunning + " mIsMsgAppForeground: " + this.mIsMsgAppForeground + " mIsNetworkValid: " + this.mIsNetworkValid + " mIsCmsProfileEnabled: " + this.mIsCmsProfileEnabled + " mIsDefaultMsgAppNative: " + this.mIsDefaultMsgAppNative + " mIsUserDeleteAccount: " + this.mIsUserDeleteAccount + " mIsAirPlaneModeOn: " + this.mIsAirPlaneModeOn + " mIsCMNWorkingStarted: " + this.mIsCMNWorkingStarted + " mIsProvisionSuccess: " + this.mIsProvisionSuccess + " mHasNotifiedBufferDBProvisionSuccess: " + this.mHasNotifiedBufferDBProvisionSuccess + " mIsAmbsServiceStop: " + this.mIsAmbsServiceStop + " mIsServicePaused: " + this.mIsServicePaused);
    }

    public List<String> notifyLoadLineStatus() {
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isMultiLineSupported()) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(this.mStoreClient.getPrerenceManager().getUserCtn());
            return arrayList;
        }
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(this.mStoreClient.getPrerenceManager().getUserCtn());
        return arrayList2;
    }

    public void onDeviceSITRefreshed(String str) {
        this.mNetAPIHandler.onLineSITRefreshed(Util.getTelUri(str, Util.getSimCountryCode(this.mContext, this.mStoreClient.getClientID())));
    }

    public void onOmaFailExceedMaxCount() {
        Log.d(this.TAG, "onOmaFailExceedMaxCount");
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isTokenRequestedFromProvision()) {
            this.mIsProvisionSuccess = false;
            setOMANetAPIWorkingStatus(false);
            this.mProvisionControl.onOmaFailExceedMaxCount();
        }
    }

    /* access modifiers changed from: private */
    public boolean bindToNetwork(Network network) {
        if (network == null) {
            Log.d(this.TAG, "bind current process to default network type");
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        boolean bindProcessToNetwork = connectivityManager.bindProcessToNetwork(network);
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork != null) {
            Log.d(this.TAG, activeNetwork.toString());
        }
        return bindProcessToNetwork;
    }

    private boolean checkingWifiGoodOrNot(Network network) {
        if (network != null) {
            return CheckCaptivePortal.isGoodWifi(network);
        }
        Log.d(this.TAG, "Wifi network instance is null");
        return false;
    }

    private void stopMobileHipri() {
        String str;
        if (this.mIsUsingMobileHipri && this.mMobileNetworkCallback != null) {
            ((ConnectivityManager) this.mContext.getSystemService("connectivity")).unregisterNetworkCallback(this.mMobileNetworkCallback);
            this.mMobileNetworkCallback = null;
            Log.d(this.TAG, "Mobile network callback unregistered");
        }
        if (bindToNetwork((Network) null)) {
            this.mIsUsingMobileHipri = false;
            str = "successfully";
        } else {
            str = "failed";
        }
        String str2 = this.TAG;
        Log.d(str2, "stopMobileHipri, bind to default network " + str);
    }

    private void startMobileHipri() {
        Log.v(this.TAG, "startMobileHipri");
        if (this.mIsUsingMobileHipri) {
            Log.d(this.TAG, "mobile network is in using");
        } else if (this.mMobileNetworkCallback == null) {
            Log.d(this.TAG, "register mobile network callback");
            MobileNetowrkCallBack mobileNetowrkCallBack = new MobileNetowrkCallBack();
            this.mMobileNetworkCallback = mobileNetowrkCallBack;
            registerNetworkCallBack(0, mobileNetowrkCallBack);
        }
    }

    private void registerNetworkCallBack(int i, ConnectivityManager.NetworkCallback networkCallback) {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(12);
        builder.addTransportType(i);
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).requestNetwork(builder.build(), networkCallback);
    }

    class MobileNetowrkCallBack extends ConnectivityManager.NetworkCallback {
        public MobileNetowrkCallBack() {
        }

        public void onAvailable(Network network) {
            Log.i(NetAPIWorkingStatusController.this.TAG, "mobile network on available");
            if (NetAPIWorkingStatusController.this.bindToNetwork(network)) {
                Log.d(NetAPIWorkingStatusController.this.TAG, "bind to MOBILE_HIPRI successfully");
                NetAPIWorkingStatusController.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setProtocol(OMAGlobalVariables.HTTP);
                NetAPIWorkingStatusController.this.mIsUsingMobileHipri = true;
                NetAPIWorkingStatusController.this.setNetworkStatus(true);
                if (NetAPIWorkingStatusController.this.shouldEnableOMANetAPIWorking()) {
                    Log.d(NetAPIWorkingStatusController.this.TAG, "shouldEnableOMANetAPIWorking: true");
                    NetAPIWorkingStatusController.this.setOMANetAPIWorkingStatus(true);
                    return;
                }
                return;
            }
            Log.d(NetAPIWorkingStatusController.this.TAG, "bind to MOBILE_HIPRI failed");
        }

        public void onLost(Network network) {
            Log.i(NetAPIWorkingStatusController.this.TAG, "mobile network on lost");
        }
    }

    /* access modifiers changed from: protected */
    public void registerNetworkStateListener() {
        Log.i(this.TAG, "registerNetworkStateListener");
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(12);
        builder.addTransportType(0);
        NetworkRequest build = builder.build();
        try {
            ((ConnectivityManager) this.mContext.getSystemService("connectivity")).registerNetworkCallback(build, this.mNetworkStateListener);
        } catch (RuntimeException e) {
            Log.e(this.TAG, e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterNetworkStateListener() {
        Log.i(this.TAG, "unregisterNetworkStateListener");
        try {
            ((ConnectivityManager) this.mContext.getSystemService("connectivity")).unregisterNetworkCallback(this.mNetworkStateListener);
        } catch (RuntimeException e) {
            Log.e(this.TAG, e.getMessage());
        }
    }

    public void hideIndicator() {
        Log.i(this.TAG, "hideIndicator()");
        this.mCallbackMsgApp.showInitsyncIndicator(false);
    }

    public void updateSubscriptionChannel() {
        Log.i(this.TAG, "updateSubscriptionChannel()");
        this.mNetAPIHandler.updateSubscriptionChannel();
    }

    public void updateDelayedSubscriptionChannel() {
        Log.i(this.TAG, "updateDelayedSubscriptionChannel()");
        this.mNetAPIHandler.updateDelayedSubscriptionChannel();
    }

    public void removeUpdateSubscriptionChannelEvent() {
        Log.i(this.TAG, "removeUpdateSubscriptionChannelEvent()");
        this.mNetAPIHandler.removeUpdateSubscriptionChannelEvent();
    }

    public void handleLargeDataPolling() {
        this.mNetAPIHandler.handleLargeDataPolling();
    }

    public boolean isPushNotiProcessPaused() {
        return this.mPushNotiPaused;
    }

    public void onMailBoxMigrationReset() {
        this.mWorkingStatus.notifyRegistrants(new AsyncResult((Object) null, IWorkingStatusProvisionListener.WorkingStatus.MAILBOX_MIGRATION_RESET, (Throwable) null));
    }

    public void onVVMNormalSyncComplete(boolean z) {
        this.mVvmHandler.setSyncState(z);
    }

    /* access modifiers changed from: protected */
    public void initDeviceID() {
        this.mStoreClient.getPrerenceManager().saveDeviceId(Util.getImei(this.mStoreClient));
    }

    public void vvmNormalSyncRequest() {
        boolean vVMAutoDownloadSetting = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getVVMAutoDownloadSetting();
        String str = this.TAG;
        Log.i(str, "vvmNormalSyncRequest() autoDownload: " + vVMAutoDownloadSetting + ", mIsWifiConnected: " + this.mIsWifiConnected);
        if (this.mIsWifiConnected || vVMAutoDownloadSetting) {
            this.mVvmHandler.normalSyncRequest();
        }
    }

    private void enableGbaModule() {
        CmsServiceModuleManager.getInstance(this.mImsFramework, this.mGbaServiceModule);
    }

    public void setVVMSyncState(boolean z) {
        this.mVvmHandler.setSyncState(z);
    }

    public boolean isNormalVVMSyncing() {
        return this.mVvmHandler.getSyncState();
    }

    public void resetDataReceiver() {
        this.mProvisionControl.resetDataReceiver();
    }

    public void notifyWorkingStatus(AsyncResult asyncResult) {
        this.mWorkingStatus.notifyRegistrants(asyncResult);
    }

    public void startInitSync() {
        String str = this.TAG;
        IMSLog.i(str, "startInitSync already notified:" + this.mHasNotifiedBufferDBProvisionSuccess);
        if (!this.mHasNotifiedBufferDBProvisionSuccess) {
            this.mHasNotifiedBufferDBProvisionSuccess = true;
            notifyWorkingStatus(new AsyncResult((Object) null, IWorkingStatusProvisionListener.WorkingStatus.PROVISION_SUCCESS, (Throwable) null));
        }
    }

    private void registerWifiStateListener() {
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).registerNetworkCallback(new NetworkRequest.Builder().addTransportType(1).addCapability(12).build(), this.mWifiStateListener);
    }

    private boolean getAutoDownloadSettings() {
        try {
            int i = Settings.System.getInt(this.mContext.getContentResolver(), this.mStoreClient.getClientID() == 1 ? AUTO_DOWNLOAD_SIM_1 : AUTO_DOWNLOAD_SIM_0);
            String str = this.TAG;
            Log.i(str, "getAutoDownloadSettings autoDownload: " + i);
            if (i == 1) {
                return true;
            }
            return false;
        } catch (Settings.SettingNotFoundException e) {
            String str2 = this.TAG;
            Log.i(str2, "getAutoDownloadSettings SettingNotFoundException: " + e.getMessage());
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void changeAndSaveAutoDownloadSettings() {
        this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMAutoDownloadSetting(getAutoDownloadSettings());
    }

    private class AutoDownloadContentObserver extends ContentObserver {
        public AutoDownloadContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean z, Uri uri) {
            String str = NetAPIWorkingStatusController.this.TAG;
            IMSLog.i(str, "AutoDownloadContentObserver - onChange() with " + uri);
            NetAPIWorkingStatusController.this.changeAndSaveAutoDownloadSettings();
        }
    }

    private void registerAutoDownloadSettingsObserver() {
        if (this.mAutoDownloadContentObserver == null) {
            this.mAutoDownloadContentObserver = new AutoDownloadContentObserver(this);
        }
        if (this.mStoreClient.getClientID() == 0) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(AUTO_DOWNLOAD_SIM_0), true, this.mAutoDownloadContentObserver);
        } else {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(AUTO_DOWNLOAD_SIM_1), true, this.mAutoDownloadContentObserver);
        }
    }

    private void unregisterAutoDownloadSettingsObserver() {
        if (this.mAutoDownloadContentObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mAutoDownloadContentObserver);
            this.mAutoDownloadContentObserver = null;
        }
    }
}
