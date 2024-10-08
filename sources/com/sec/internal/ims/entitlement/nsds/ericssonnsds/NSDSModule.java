package com.sec.internal.ims.entitlement.nsds.ericssonnsds;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.IImsService;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.constants.ims.entitilement.EntitlementNamespaces;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.config.EntitlementConfigService;
import com.sec.internal.ims.entitlement.nsds.NSDSModuleBase;
import com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds.RegistrationIntentService;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.AkaTokenRetrievalFlow;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.PushTokenUpdateFlow;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.persist.PushTokenHelper;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.nsds.strategy.MnoNsdsStrategyCreator;
import com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.DeviceNameHelper;
import com.sec.internal.ims.entitlement.util.E911AidValidator;
import com.sec.internal.ims.entitlement.util.EntFeatureDetector;
import com.sec.internal.ims.entitlement.util.IntentScheduler;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.ims.entitlement.util.SimSwapNSDSConfigHelper;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.entitlement.nsds.IAkaTokenRetrievalFlow;
import com.sec.internal.interfaces.ims.entitlement.nsds.IEntitlementCheck;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISIMDeviceImplicitActivation;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISimSwapFlow;
import com.sec.internal.interfaces.ims.entitlement.nsds.SimSwapCompletedListener;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class NSDSModule extends NSDSModuleBase {
    protected static final String ACTION_CHECK_REG_STATE = "com.sec.vsim.ericssonnsds.CHECK_REG_STATE";
    protected static final String ACTION_E911_AID_EXP_CHECK_TIMEOUT = "com.sec.vsim.ericssonnsds.E911_AID_EXP_CHECK_TIMEOUT";
    protected static final String ACTION_REFRESH_DEVICE_INFO = "com.sec.vsim.ericssonnsds.ACTION_REFRESH_DEVICE_INFO";
    protected static final String ACTION_REFRESH_ENTITLEMENT_CHECK = "com.sec.vsim.ericssonnsds.ACTION_REFRESH_ENTITLEMENT_CHECK";
    protected static final String ACTION_REFRESH_TOKEN = "com.sec.vsim.ericssonnsds.REFRESH_TOKEN";
    private static final String ACTION_RETRY_ENTITLEMENT_CHECK = "com.sec.vsim.ericssonnsds.ACTION_RETRY_ENTITLEMENT_CHECK";
    protected static final String ACTION_SIM_DEVICE_ACTIVATION = "com.sec.vsim.ericssonnsds.ACTION_SIM_DEVICE_ACTIVATION";
    protected static final String ACTION_SVC_PROVISION_CHECK_RETRY_TIMEOUT = "com.sec.vsim.ericssonnsds.ACTION_SVC_PROVISION_CHECK_RETRY_TIMEOUT";
    protected static final String ACTION_SVC_PROVISION_CHECK_TIMEOUT = "com.sec.vsim.ericssonnsds.SVC_PROVISION_CHECK_TIMEOUT";
    private static final long INVALID_FINGERPRINT_EXPIRATION_TIME = 3600000;
    private static final int MAX_LENGTH_MSISDN = 11;
    private static final long REFRESH_TOKEN_WAIT_TIME = 120000;
    private static final long RETRY_INTERVAL = 30000;
    private static final long RETRY_INTERVAL_AUTO_ON = 280000;
    /* access modifiers changed from: private */
    public static Looper sServiceLooper;
    /* access modifiers changed from: private */
    public static final UriMatcher sUriMatcher;
    /* access modifiers changed from: private */
    public String LOG_TAG = NSDSModule.class.getSimpleName();
    protected AirplaneModeObserver mAirplaneModeObserver;
    private IAkaTokenRetrievalFlow mAkaTokenRetrievalFlow;
    /* access modifiers changed from: private */
    public BaseFlowImpl mBaseFlowImpl;
    protected ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z, Uri uri) {
            String r5 = NSDSModule.this.LOG_TAG;
            IMSLog.i(r5, "Uri changed:" + uri);
            IMnoNsdsStrategy mnoNsdsStrategy = NSDSModule.this.getMnoNsdsStrategy();
            if (mnoNsdsStrategy == null || uri == null) {
                IMSLog.e(NSDSModule.this.LOG_TAG, "Uri changed: null mnoStrategy or null url");
                return;
            }
            int match = NSDSModule.sUriMatcher.match(uri);
            if (match == 40) {
                NSDSModule.this.performProceduresOnConfigRefreshComplete();
            } else if (match != 71) {
                if (match == 78) {
                    NSDSModule.this.queueUpdateDeviceName();
                } else if (match == 79) {
                    NSDSModule.this.performOnDeviceReadyIf();
                }
            } else if (NSDSModule.this.isDeviceReady()) {
                NSDSModule.this.handleSimSwapEvent("SimSwapCache is ready");
            } else {
                IMSLog.i(NSDSModule.this.LOG_TAG, "SIM swap will be handled after device is ready");
                if (mnoNsdsStrategy.getSimSwapFlow(NSDSModule.sServiceLooper, NSDSModule.this.mContext, NSDSModule.this.mBaseFlowImpl, NSDSModule.this.mNSDSDatabaseHelper) != null) {
                    NSDSModule.this.mHandleSimSwapAfterDeviceIsReady = true;
                }
            }
            if (mnoNsdsStrategy.shouldChangedUriTriggerNsdsService(uri)) {
                NSDSModule.this.enableOrDisableNSDSService();
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    protected IEntitlementCheck mEntitlementCheckFlow;
    private SimpleEventLog mEventLog;
    protected boolean mHandleSimSwapAfterDeviceIsReady;
    protected IImsRegistrationListener.Stub mImsRegistratinListner = new IImsRegistrationListener.Stub() {
        public void onRegistered(ImsRegistration imsRegistration) {
            NSDSModule.this.editRegiListOnRegistered(imsRegistration);
            NSDSModule nSDSModule = NSDSModule.this;
            nSDSModule.sendMessage(nSDSModule.obtainMessage(0, imsRegistration));
        }

        public void onDeregistered(ImsRegistration imsRegistration, ImsRegistrationError imsRegistrationError) {
            NSDSModule.this.removeFromRegiListOnDeregistered(imsRegistration);
            NSDSModule nSDSModule = NSDSModule.this;
            nSDSModule.sendMessage(nSDSModule.obtainMessage(1, imsRegistrationError.getSipErrorCode(), 0, imsRegistration));
        }
    };
    protected IImsService mImsService;
    /* access modifiers changed from: private */
    public Date mInvalidFingerPrintDate = null;
    private AtomicBoolean mIsAfterApm = new AtomicBoolean(false);
    protected boolean mIsSimSupported = false;
    protected BroadcastReceiver mNSDSAppFlowReceiver = null;
    protected NSDSDatabaseHelper mNSDSDatabaseHelper;
    protected BroadcastReceiver mNSDSEventRequestReceiver = null;
    protected SharedPreferences.OnSharedPreferenceChangeListener mNSDSSharedPrefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            String r3 = NSDSModule.this.LOG_TAG;
            IMSLog.i(r3, "OnSharedPreferenceChangeListener: " + str + " changed.");
            if (str.contains(NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE)) {
                NSDSModule.this.queuePushTokenUpdateIf();
            } else if (str.contains(NSDSNamespaces.NSDSSharedPref.PREF_SENT_TOKEN_TO_SERVER)) {
                boolean isGcmTokenSentToServer = NSDSSharedPrefHelper.isGcmTokenSentToServer(NSDSModule.this.mContext, NSDSModule.this.mBaseFlowImpl.getDeviceId());
                String r4 = NSDSModule.this.LOG_TAG;
                IMSLog.i(r4, "isTokenSent: " + isGcmTokenSentToServer);
                if (isGcmTokenSentToServer) {
                    NSDSModule.this.updateGcmPushTokenInDb();
                    NSDSModule.this.queuePushTokenUpdateIf();
                    IntentScheduler.stopTimer(NSDSModule.this.mContext, NSDSModule.this.mSimManager.getSimSlotIndex(), NSDSModule.ACTION_REFRESH_TOKEN);
                    IMSLog.i(NSDSModule.this.LOG_TAG, "RefrehTokenTimer stopped");
                }
            } else if (str.contains(NSDSNamespaces.NSDSSharedPref.PREF_SVC_PROV_STATE)) {
                boolean isVoWifiServiceProvisioned = NSDSSharedPrefHelper.isVoWifiServiceProvisioned(NSDSModule.this.mContext, NSDSModule.this.mBaseFlowImpl.getDeviceId());
                String r42 = NSDSModule.this.LOG_TAG;
                IMSLog.i(r42, "isSvcProvCompleted: " + isVoWifiServiceProvisioned);
                if (isVoWifiServiceProvisioned) {
                    NSDSModule.this.scheduleServiceProvisionCheckTimer();
                } else {
                    IntentScheduler.stopTimer(NSDSModule.this.mContext, NSDSModule.this.mSimManager.getSimSlotIndex(), NSDSModule.ACTION_SVC_PROVISION_CHECK_TIMEOUT);
                }
            } else if (str.contains(NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS) && TextUtils.equals(NSDSSharedPrefHelper.get(NSDSModule.this.mContext, NSDSModule.this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS), NSDSNamespaces.VowifiAutoOnOperation.AUTOON_RETRY)) {
                IMSLog.i(NSDSModule.this.LOG_TAG, "[ATT_AutoOn] AUTOON_RETRY");
                NSDSModule nSDSModule = NSDSModule.this;
                nSDSModule.scheduleRetryWFCAutoOnTimer(nSDSModule.mWfcAutoOnRetryCount);
            }
        }
    };
    protected AtomicBoolean mOnSimSwapEvt = new AtomicBoolean(false);
    private List<Message> mPendindMsgsForSimSwapCompletion = new ArrayList();
    protected PushTokenUpdateFlow mPushTokenUpdateFlow;
    protected final ArrayList<ImsRegistration> mRegistrationList = new ArrayList<>();
    private ISIMDeviceImplicitActivation mSIMDeviceActivationFlow;
    /* access modifiers changed from: private */
    public ISimManager mSimManager;
    private ISimSwapFlow mSimSwapFlow;
    /* access modifiers changed from: private */
    public int mSvcProvCheckRetryCount = 0;
    /* access modifiers changed from: private */
    public int mWfcAutoOnRetryCount = 0;

    /* access modifiers changed from: private */
    public void updateGcmPushTokenInDb() {
        IMSLog.i(this.LOG_TAG, "updateGcmPushTokenInDb()");
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        String gcmSenderId = mnoNsdsStrategy != null ? mnoNsdsStrategy.getGcmSenderId(this.mBaseFlowImpl.getDeviceId(), this.mSimManager.getImsi()) : null;
        String pushToken = PushTokenHelper.getPushToken(this.mContext, this.mBaseFlowImpl.getDeviceId());
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.add("GCM Token ready: " + pushToken);
        this.mNSDSDatabaseHelper.insertOrUpdateGcmPushToken(gcmSenderId, pushToken, "managePushToken", this.mBaseFlowImpl.getDeviceId());
    }

    protected class AirplaneModeObserver extends ContentObserver {
        public AirplaneModeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean z) {
            int i = Settings.Global.getInt(NSDSModule.this.mContext.getContentResolver(), "airplane_mode_on", 1);
            String r0 = NSDSModule.this.LOG_TAG;
            IMSLog.i(r0, "AirpalneModeOn onChange: " + i);
            NSDSModule nSDSModule = NSDSModule.this;
            nSDSModule.sendMessage(nSDSModule.obtainMessage(18, Integer.valueOf(i)));
        }
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "nsds_configs", 40);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "sim_swap_nsds_configs", 71);
        uriMatcher.addURI("settings", "global/device_name", 78);
        uriMatcher.addURI("settings", "global/device_provisioned", 79);
    }

    public NSDSModule(Looper looper, Context context, ISimManager iSimManager) {
        super(looper);
        sServiceLooper = looper;
        this.mContext = context;
        this.mSimManager = iSimManager;
        this.mNSDSDatabaseHelper = new NSDSDatabaseHelper(this.mContext);
        SimpleEventLog simpleEventLog = new SimpleEventLog(this.mContext, this.LOG_TAG, 100);
        this.mEventLog = simpleEventLog;
        simpleEventLog.logAndAdd("Create " + this.LOG_TAG);
        this.LOG_TAG += "<" + this.mSimManager.getSimSlotIndex() + ">";
        initialize();
    }

    private void initialize() {
        initNsdsAppFlows();
        registerNsdsContentObserver();
        registerAirplaneModeObserver();
        registerNsdsEventQueueReceiver();
        registerNsdsAppFlowReceiver();
        connectImsService();
    }

    private void initSimSwapFlow() {
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy != null) {
            IMSLog.i(this.LOG_TAG, "initSimSwapFlow()");
            this.mSimSwapFlow = mnoNsdsStrategy.getSimSwapFlow(sServiceLooper, this.mContext, this.mBaseFlowImpl, this.mNSDSDatabaseHelper);
        }
    }

    private void initNsdsAppFlows() {
        this.mBaseFlowImpl = new BaseFlowImpl(sServiceLooper, this.mContext, this.mSimManager);
        this.mPushTokenUpdateFlow = new PushTokenUpdateFlow(sServiceLooper, this.mContext, this.mBaseFlowImpl, this.mNSDSDatabaseHelper);
    }

    public void registerImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) {
        IMSLog.i(this.LOG_TAG, "registerImsRegistrationListener");
        try {
            IImsService iImsService = this.mImsService;
            if (iImsService != null) {
                iImsService.registerImsRegistrationListener(iImsRegistrationListener);
            }
        } catch (RemoteException e) {
            String str = this.LOG_TAG;
            IMSLog.s(str, "registerImsRegistrationListener " + e.getMessage());
        }
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) {
        IMSLog.i(this.LOG_TAG, "unregisterImsRegistrationListener");
        try {
            IImsService iImsService = this.mImsService;
            if (iImsService != null) {
                iImsService.unregisterImsRegistrationListener(iImsRegistrationListener);
            }
        } catch (RemoteException e) {
            String str = this.LOG_TAG;
            IMSLog.s(str, "unregisterImsRegistrationListener " + e.getMessage());
        }
    }

    private void connectImsService() {
        IMSLog.i(this.LOG_TAG, "connectImsService");
        if (this.mImsService == null) {
            Intent intent = new Intent();
            intent.setClassName("com.sec.imsservice", "com.sec.internal.ims.imsservice.ImsService");
            ContextExt.bindServiceAsUser(this.mContext, intent, new ServiceConnection() {
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    IMSLog.i(NSDSModule.this.LOG_TAG, "Connected to ImsService.");
                    NSDSModule.this.mImsService = IImsService.Stub.asInterface(iBinder);
                    NSDSModule nSDSModule = NSDSModule.this;
                    nSDSModule.registerImsRegistrationListener(nSDSModule.mImsRegistratinListner);
                }

                public void onServiceDisconnected(ComponentName componentName) {
                    IMSLog.i(NSDSModule.this.LOG_TAG, "Disconnected from ImsService.");
                    NSDSModule nSDSModule = NSDSModule.this;
                    nSDSModule.unregisterImsRegistrationListener(nSDSModule.mImsRegistratinListner);
                    NSDSModule.this.mImsService = null;
                }
            }, 1, ContextExt.CURRENT_OR_SELF);
        }
    }

    /* access modifiers changed from: private */
    public void enableOrDisableNSDSService() {
        if (this.mOnSimSwapEvt.get()) {
            IMSLog.i(this.LOG_TAG, "add EVT_ENABLE_OR_DISABLE_SERVICE for sim swap complete:");
            this.mPendindMsgsForSimSwapCompletion.add(obtainMessage(42));
            return;
        }
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy == null || !mnoNsdsStrategy.isNsdsServiceEnabled()) {
            deactivateDeviceIfNsdsServiceDisabled();
        } else if (!NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "enableOrDisableNSDSService: activate SIM device");
            queueSimDeviceActivation(11, 0);
        }
    }

    private void registerNsdsAppFlowReceiver() {
        if (this.mNSDSAppFlowReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(NSDSNamespaces.NSDSActions.SIM_DEVICE_ACTIVATED);
            intentFilter.addAction(NSDSNamespaces.NSDSActions.SIM_DEVICE_DEACTIVATED);
            intentFilter.addAction(NSDSNamespaces.NSDSActions.DEVICE_CONFIG_UPDATED);
            intentFilter.addAction(NSDSNamespaces.NSDSActions.E911_AID_INFO_RECEIVED);
            intentFilter.addAction(NSDSNamespaces.NSDSActions.ENTITLEMENT_AND_LOCTC_CHECK_COMPLETED);
            intentFilter.addAction(NSDSNamespaces.NSDSActions.RECEIVED_GCM_EVENT_NOTIFICATION);
            AnonymousClass5 r1 = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String str;
                    String r8 = NSDSModule.this.LOG_TAG;
                    IMSLog.i(r8, "onReceive: app flow result arrived " + intent.getAction());
                    int intExtra = intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0);
                    if (intExtra != NSDSModule.this.mSimManager.getSimSlotIndex()) {
                        IMSLog.i(NSDSModule.this.LOG_TAG, "SlotId isn't matched about intent");
                        return;
                    }
                    NSDSModule.this.scheduleNsdsAppFlowRetryIf(intent);
                    if (NSDSNamespaces.NSDSActions.SIM_DEVICE_DEACTIVATED.equals(intent.getAction())) {
                        IntentScheduler.stopTimer(NSDSModule.this.mContext, NSDSModule.this.mSimManager.getSimSlotIndex(), NSDSModule.ACTION_CHECK_REG_STATE);
                        if (intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_DEACTIVATION_CAUSE, 0) == 1) {
                            NSDSModule.this.mInvalidFingerPrintDate = new Date(System.currentTimeMillis());
                        }
                    } else if (NSDSNamespaces.NSDSActions.E911_AID_INFO_RECEIVED.equals(intent.getAction())) {
                        NSDSModule.this.scheduleE911CheckTimer();
                        NSDSSharedPrefHelper.save(NSDSModule.this.mContext, NSDSModule.this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, "completed");
                    } else if (NSDSNamespaces.NSDSActions.ENTITLEMENT_AND_LOCTC_CHECK_COMPLETED.equals(intent.getAction())) {
                        NSDSModule.this.handleResultAfterEntitlementCheck(intent);
                    } else if (NSDSNamespaces.NSDSActions.RECEIVED_GCM_EVENT_NOTIFICATION.equals(intent.getAction())) {
                        ArrayList<String> stringArrayListExtra = intent.getStringArrayListExtra(NSDSNamespaces.NSDSExtras.EVENT_LIST);
                        String r4 = NSDSModule.this.LOG_TAG;
                        IMSLog.i(r4, "onReceive: entitlement push notification arrived " + stringArrayListExtra);
                        if (stringArrayListExtra != null) {
                            if (stringArrayListExtra.contains(NSDSNamespaces.NSDSGcmEventType.ENTMT_UPDATE)) {
                                NSDSModule.this.queueEntitlementCheck(9, 0);
                            } else if (stringArrayListExtra.contains(NSDSNamespaces.NSDSGcmEventType.E911_ADDR_UPDATE)) {
                                NSDSModule.this.queueEntitlementCheck(8, 0);
                            }
                        }
                    } else if (NSDSNamespaces.NSDSActions.DEVICE_CONFIG_UPDATED.equals(intent.getAction())) {
                        NSDSModule.this.handleResultAfterConfigRetrieval(intent);
                    }
                    if (EntFeatureDetector.checkWFCAutoOnEnabled(intExtra) && NSDSNamespaces.NSDSActions.SIM_DEVICE_ACTIVATED.equals(intent.getAction()) && (str = NSDSSharedPrefHelper.get(NSDSModule.this.mContext, NSDSModule.this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS)) != null && !"completed".equals(str)) {
                        IMSLog.i(NSDSModule.this.LOG_TAG, "[ATT_AutoOn] onReceive: start VoWIFI Toggle AutoOn");
                        NSDSModule.this.mWfcAutoOnRetryCount = intent.getIntExtra("retry_count", -1);
                        NSDSModule.this.handleVoWifToggleOnEvent();
                        NSDSModule nSDSModule = NSDSModule.this;
                        nSDSModule.scheduleRetryWFCAutoOnTimer(nSDSModule.mWfcAutoOnRetryCount);
                    }
                }
            };
            this.mNSDSAppFlowReceiver = r1;
            this.mContext.registerReceiver(r1, intentFilter);
        }
    }

    /* access modifiers changed from: private */
    public void handleResultAfterConfigRetrieval(Intent intent) {
        boolean booleanExtra = intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false);
        int intExtra = intent.getIntExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, 0);
        ArrayList<Integer> integerArrayListExtra = intent.getIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("handleResultAfterConfigRetrieval: " + booleanExtra + " deviceEventType " + intExtra + " errorCodes " + integerArrayListExtra);
        if (booleanExtra && integerArrayListExtra != null && !integerArrayListExtra.contains(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_CONNECTIVITY_NEW_CONFIG_UPDATED))) {
            performProceduresOnConfigRefreshComplete();
        }
    }

    private void registerNsdsEventQueueReceiver() {
        if (this.mNSDSEventRequestReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_REFRESH_TOKEN);
            intentFilter.addAction(ACTION_REFRESH_DEVICE_INFO);
            intentFilter.addAction(ACTION_SIM_DEVICE_ACTIVATION);
            intentFilter.addAction(ACTION_CHECK_REG_STATE);
            intentFilter.addAction(ACTION_E911_AID_EXP_CHECK_TIMEOUT);
            intentFilter.addAction(ACTION_SVC_PROVISION_CHECK_TIMEOUT);
            intentFilter.addAction(ACTION_SVC_PROVISION_CHECK_RETRY_TIMEOUT);
            intentFilter.addAction(ACTION_RETRY_ENTITLEMENT_CHECK);
            AnonymousClass6 r1 = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String r5 = NSDSModule.this.LOG_TAG;
                    IMSLog.i(r5, "onReceive: event has been requested " + intent.getAction());
                    int intExtra = intent.getIntExtra("retry_count", 0);
                    int intExtra2 = intent.getIntExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, -1);
                    if (intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0) != NSDSModule.this.mSimManager.getSimSlotIndex()) {
                        IMSLog.i(NSDSModule.this.LOG_TAG, "SlotId isn't matched about intent");
                    } else if (NSDSModule.ACTION_REFRESH_TOKEN.equals(intent.getAction())) {
                        NSDSModule.this.queueGcmTokenRetrieval();
                    } else if (NSDSModule.ACTION_REFRESH_DEVICE_INFO.equals(intent.getAction())) {
                        NSDSModule.this.queueRefreshDeviceAndServiceInfo(intExtra2, intExtra);
                    } else if (NSDSModule.ACTION_SIM_DEVICE_ACTIVATION.equals(intent.getAction())) {
                        NSDSModule.this.queueSimDeviceActivation(intExtra2, intExtra);
                    } else if (NSDSModule.ACTION_CHECK_REG_STATE.equals(intent.getAction())) {
                        NSDSModule nSDSModule = NSDSModule.this;
                        List<String> readyForUseMsisdns = nSDSModule.mNSDSDatabaseHelper.getReadyForUseMsisdns(nSDSModule.mBaseFlowImpl.getDeviceId());
                        String r6 = NSDSModule.this.LOG_TAG;
                        IMSLog.s(r6, "onReceive: ACTION_CHECK_REG_STATE timeout. prevRegMsisdns: " + readyForUseMsisdns);
                        NSDSModule.this.broadcastLinesReadyStatusUpdated(new ArrayList(readyForUseMsisdns), 0, 2);
                    } else if (NSDSModule.ACTION_E911_AID_EXP_CHECK_TIMEOUT.equals(intent.getAction())) {
                        NSDSModule.this.refreshEntitlementAndE911Info(6, 0);
                    } else if (NSDSModule.ACTION_SVC_PROVISION_CHECK_TIMEOUT.equals(intent.getAction())) {
                        NSDSModule.this.queueEntitlementCheck(4, 0);
                    } else if (NSDSModule.ACTION_SVC_PROVISION_CHECK_RETRY_TIMEOUT.equals(intent.getAction())) {
                        IMnoNsdsStrategy mnoNsdsStrategy = NSDSModule.this.getMnoNsdsStrategy();
                        if (mnoNsdsStrategy != null && !mnoNsdsStrategy.isNsdsUIAppSwitchOn(NSDSModule.this.mBaseFlowImpl.getDeviceId(), NSDSModule.this.mBaseFlowImpl.getSimManager().getSimSlotIndex())) {
                            NSDSModule nSDSModule2 = NSDSModule.this;
                            nSDSModule2.queueEntitlementCheck(10, nSDSModule2.mSvcProvCheckRetryCount);
                        }
                    } else if (NSDSModule.ACTION_RETRY_ENTITLEMENT_CHECK.equals(intent.getAction())) {
                        NSDSModule.this.queueEntitlementCheck(intExtra2, intExtra);
                    }
                }
            };
            this.mNSDSEventRequestReceiver = r1;
            this.mContext.registerReceiver(r1, intentFilter);
        }
    }

    private void initNsdsSharePref() {
        SharedPreferences sharedPref = NSDSSharedPrefHelper.getSharedPref(this.mContext, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0);
        if (sharedPref != null) {
            sharedPref.registerOnSharedPreferenceChangeListener(this.mNSDSSharedPrefChangeListener);
            clearActivationProgressState();
        }
    }

    private void registerNsdsContentObserver() {
        this.mContext.getContentResolver().registerContentObserver(NSDSContractExt.DeviceConfig.CONTENT_URI, false, this.mContentObserver);
        this.mContext.getContentResolver().registerContentObserver(NSDSContractExt.NsdsConfigs.CONTENT_URI, false, this.mContentObserver);
        this.mContext.getContentResolver().registerContentObserver(EntitlementConfigContract.DeviceConfig.CONTENT_URI, false, this.mContentObserver);
        this.mContext.getContentResolver().registerContentObserver(NSDSContractExt.SimSwapNsdsConfigs.CONTENT_URI, false, this.mContentObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(Extensions.Settings.Global.DEVICE_NAME), false, this.mContentObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(Extensions.Settings.Global.DEVICE_PROVISIONED), false, this.mContentObserver);
    }

    private boolean checkSimReady() {
        String str = this.LOG_TAG;
        IMSLog.i(str, "checkSimReady: " + this.mIsSimSupported);
        return this.mIsSimSupported;
    }

    private void onFlightMode(int i) {
        String str = this.LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("onFlightMode: ");
        sb.append(i == 1);
        IMSLog.i(str, sb.toString());
        if (i != 1) {
            this.mIsAfterApm.set(true);
            if (isDeviceReady()) {
                refreshEntitlementAndE911Info(6, 0);
                this.mIsAfterApm.set(false);
            }
        }
    }

    private void registerAirplaneModeObserver() {
        if (this.mAirplaneModeObserver == null) {
            this.mAirplaneModeObserver = new AirplaneModeObserver(new Handler(sServiceLooper));
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("airplane_mode_on"), false, this.mAirplaneModeObserver);
        }
    }

    /* access modifiers changed from: private */
    public void scheduleE911CheckTimer() {
        long j;
        IMSLog.i(this.LOG_TAG, "schedule E911 aid expiration check timer");
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy == null) {
            j = -1;
        } else {
            j = mnoNsdsStrategy.getEntitlementCheckExpirationTime();
        }
        long j2 = j;
        if (j2 > 0) {
            IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_E911_AID_EXP_CHECK_TIMEOUT, (Bundle) null, j2);
        }
    }

    /* access modifiers changed from: private */
    public void scheduleServiceProvisionCheckTimer() {
        long j;
        IMSLog.i(this.LOG_TAG, "schedule service provision check timer");
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy == null) {
            j = -1;
        } else {
            j = mnoNsdsStrategy.getEntitlementCheckExpirationTime() * 2;
        }
        long j2 = j;
        if (j2 > 0) {
            IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_SVC_PROVISION_CHECK_TIMEOUT, (Bundle) null, j2);
        }
    }

    private void scheduleServiceProvisionCheckRetryTimer() {
        long j;
        IMSLog.i(this.LOG_TAG, "schedule service provision check retry timer");
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy == null) {
            j = -1;
        } else {
            int i = this.mSvcProvCheckRetryCount + 1;
            this.mSvcProvCheckRetryCount = i;
            j = mnoNsdsStrategy.calEntitlementCheckExpRetryTime(i);
        }
        long j2 = j;
        if (j2 > 0) {
            IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_SVC_PROVISION_CHECK_RETRY_TIMEOUT, (Bundle) null, j2);
        } else {
            this.mSvcProvCheckRetryCount = 0;
        }
    }

    /* access modifiers changed from: private */
    public void scheduleRetryWFCAutoOnTimer(int i) {
        if (i <= 1) {
            IMSLog.i(this.LOG_TAG, "[ATT_AutoOn] scheduleRetryWFCAutoOnTimer start in about 5 minutes");
            Bundle bundle = new Bundle();
            bundle.putInt("retry_count", i);
            bundle.putInt(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, 1);
            IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_RETRY_ENTITLEMENT_CHECK, bundle, RETRY_INTERVAL_AUTO_ON);
        }
    }

    /* access modifiers changed from: private */
    public boolean isDeviceReady() {
        return NetworkUtil.isConnected(this.mContext);
    }

    private void clearActivationProgressState() {
        if (NSDSSharedPrefHelper.isDeviceInActivationProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "clearActivationProgressState: SIM device");
            NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE, NSDSNamespaces.NSDSDeviceState.DEACTIVATED);
        }
        if (NSDSSharedPrefHelper.isDeviceInEntitlementProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "clearActivationProgressState: Entitlement");
            NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE);
        }
    }

    private void scheduleGetGcmRegistrationTokenIfTokenNotSent() {
        boolean isGcmTokenSentToServer = NSDSSharedPrefHelper.isGcmTokenSentToServer(this.mContext, this.mBaseFlowImpl.getDeviceId());
        String str = this.LOG_TAG;
        IMSLog.i(str, "scheduleGetGcmRegistrationTokenIfTokenNotSent: isTokenSent:" + isGcmTokenSentToServer);
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (!isGcmTokenSentToServer && mnoNsdsStrategy != null && mnoNsdsStrategy.isGcmTokenRequired()) {
            new Thread(new NSDSModule$$ExternalSyntheticLambda0(this, mnoNsdsStrategy.getGcmSenderId(this.mBaseFlowImpl.getDeviceId(), this.mBaseFlowImpl.getSimManager().getImsi()))).start();
            IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_REFRESH_TOKEN, REFRESH_TOKEN_WAIT_TIME);
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$scheduleGetGcmRegistrationTokenIfTokenNotSent$0(String str) {
        getGcmTokenFromServer(str, "managePushToken");
    }

    /* access modifiers changed from: private */
    public void performProceduresOnConfigRefreshComplete() {
        if (this.mOnSimSwapEvt.get()) {
            IMSLog.i(this.LOG_TAG, "performProceduresOnConfigRefreshComplete: pending due to SIM swap");
            this.mPendindMsgsForSimSwapCompletion.add(obtainMessage(41));
            return;
        }
        IMSLog.i(this.LOG_TAG, "performProceduresOnConfigRefreshComplete");
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy == null || mnoNsdsStrategy.isNsdsUIAppSwitchOn(this.mBaseFlowImpl.getDeviceId(), this.mBaseFlowImpl.getSimManager().getSimSlotIndex())) {
            scheduleGetGcmRegistrationTokenIfTokenNotSent();
        } else {
            IMSLog.i(this.LOG_TAG, "performProceduresOnConfigRefreshComplete: NSDS switch off");
        }
    }

    private void deactivateDeviceIfNsdsServiceDisabled() {
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy != null && !mnoNsdsStrategy.isNsdsServiceEnabled() && NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "Deactivating device since nsds service is disabled");
            queueSimDeviceDeactivation(0);
        }
    }

    /* access modifiers changed from: private */
    public void handleSimSwapEvent(String str) {
        String str2 = this.LOG_TAG;
        IMSLog.i(str2, "handleSimSwapEvent:" + str);
        this.mHandleSimSwapAfterDeviceIsReady = false;
        stopForcedSimSwap();
        performSimSwapFlow();
    }

    public void initForDeviceReady() {
        initMnoBasedAppFlows();
        initNsdsSharePref();
    }

    public void onSimReady(boolean z) {
        String str = this.LOG_TAG;
        IMSLog.i(str, "onSimReady: isSwapped " + z);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.add("onSimReady: isSwapped " + z);
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy != null) {
            this.mIsSimSupported = mnoNsdsStrategy.isSimSupportedForNsds(this.mSimManager);
        }
        if (z) {
            unregisterLoginReceiversAndStopTimers();
            initSimSwapFlow();
            initNsdsAppFlows();
        }
        initMnoBasedAppFlows();
        initNsdsSharePref();
        this.mOnSimSwapEvt.set(z || NSDSSharedPrefHelper.isSimSwapPending(this.mContext, this.mBaseFlowImpl.getDeviceId()));
        if (this.mOnSimSwapEvt.get()) {
            scheduleForForcedSimSwapIf();
            this.mNSDSDatabaseHelper.copyConfigEntriesForSimSwap(this.mBaseFlowImpl.getDeviceId(), NSDSSharedPrefHelper.getPrefForSlot(this.mContext, this.mSimManager.getSimSlotIndex(), NSDSNamespaces.NSDSSharedPref.PREF_PREV_IMSI), this.mSimManager.getSimSlotIndex());
            clearEntitlementServerUrl();
            SimSwapNSDSConfigHelper.clear();
            MnoNsdsStrategyCreator.resetMnoStrategy();
            performProcsOnSimSwapCompleted();
        }
        performOnDeviceReadyIf();
    }

    private void clearEntitlementServerUrl() {
        NSDSSharedPrefHelper.clearEntitlementServerUrl(this.mContext, this.mBaseFlowImpl.getDeviceId());
    }

    private void initMnoBasedAppFlows() {
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy != null) {
            String str = this.LOG_TAG;
            IMSLog.i(str, "initMnoBasedAppFlows: " + mnoNsdsStrategy.getClass().getName());
            this.mBaseFlowImpl.clearDeferredMessages();
            this.mBaseFlowImpl.setSimAuthAppType(mnoNsdsStrategy.getSimAuthenticationType());
            this.mEntitlementCheckFlow = mnoNsdsStrategy.getEntitlementCheckImpl(sServiceLooper, this.mContext, this.mBaseFlowImpl, this.mNSDSDatabaseHelper, this);
            this.mSIMDeviceActivationFlow = mnoNsdsStrategy.getSimDeviceActivationImpl(sServiceLooper, this.mContext, this.mBaseFlowImpl, this.mNSDSDatabaseHelper);
            this.mAkaTokenRetrievalFlow = new AkaTokenRetrievalFlow(sServiceLooper, this.mContext, this.mBaseFlowImpl, this.mNSDSDatabaseHelper);
            return;
        }
        IMSLog.e(this.LOG_TAG, "initMnoBasedAppFlows: mnoStrategy is null");
    }

    private void performSimSwapFlow() {
        ISimSwapFlow iSimSwapFlow = this.mSimSwapFlow;
        if (iSimSwapFlow != null) {
            iSimSwapFlow.handleSimSwap(new SimSwapCompletedListener() {
                public void onSimSwapCompleted() {
                    IMSLog.i(NSDSModule.this.LOG_TAG, "performSimSwapHandling: onSimSwapCompleted");
                    NSDSSharedPrefHelper.clearSimSwapPending(NSDSModule.this.mContext, NSDSModule.this.mBaseFlowImpl.getDeviceId());
                    NSDSModule.this.mOnSimSwapEvt.set(false);
                    if (NSDSModule.this.getMnoNsdsStrategy() != null) {
                        IMSLog.i(NSDSModule.this.LOG_TAG, "performOnDeviceReadyIf after onSimSwapCompleted");
                        NSDSModule.this.performOnDeviceReadyIf();
                    }
                }
            });
        } else {
            IMSLog.e(this.LOG_TAG, "handleSimSwapEvent: flow not initiated, invalid request");
        }
    }

    private void performProcsOnSimSwapCompleted() {
        this.mOnSimSwapEvt.set(false);
        IMSLog.i(this.LOG_TAG, "performProcsOnSimSwapCompleted()");
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy != null && mnoNsdsStrategy.shouldIgnoreDeviceConfigValidity() && mnoNsdsStrategy.isNsdsServiceEnabled()) {
            scheduleGetGcmRegistrationTokenIfTokenNotSent();
        }
        if (!checkSimReady()) {
            IMSLog.i(this.LOG_TAG, "performProcsOnSimSwapCompleted: SIM not supported");
            handleSimNotSupported();
        } else if (!this.mPendindMsgsForSimSwapCompletion.isEmpty()) {
            for (Message sendMessage : this.mPendindMsgsForSimSwapCompletion) {
                sendMessage(sendMessage);
            }
        }
    }

    private void scheduleForForcedSimSwapIf() {
        IMSLog.i(this.LOG_TAG, "scheduleForForcedSimSwapIfCacheNotReady");
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        sendMessageDelayed(obtainMessage(40), mnoNsdsStrategy != null ? mnoNsdsStrategy.getWaitTimeForForcedSimSwap() : 0);
    }

    private void stopForcedSimSwap() {
        IMSLog.i(this.LOG_TAG, "stopped forced SimSwap handling");
        removeMessages(40);
    }

    public void onSimNotAvailable() {
        IMSLog.i(this.LOG_TAG, "onSimNotAvailable()");
        this.mEventLog.add("onSimNotAvailable()");
        IntentScheduler.stopAllTimers(this.mContext);
    }

    private void getGcmTokenFromServer(String str, String str2) {
        IMSLog.i(this.LOG_TAG, "getGcmRegistrationToken()");
        Intent intent = new Intent(this.mContext, RegistrationIntentService.class);
        intent.putExtra("gcm_sender_id", str);
        intent.putExtra(NSDSNamespaces.NSDSExtras.GCM_PROTOCOL_TO_SERVER, str2);
        intent.putExtra("device_id", this.mBaseFlowImpl.getDeviceId());
        this.mContext.startService(intent);
    }

    public void queueGcmTokenRetrieval() {
        sendEmptyMessage(43);
    }

    private void refreshDeviceConfigIf(int i) {
        if (!this.mNSDSDatabaseHelper.isDeviceConfigAvailable(this.mSimManager.getImsi())) {
            requestDeviceConfigRetrieval(14, i);
        } else if (!NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
            if (mnoNsdsStrategy != null && mnoNsdsStrategy.isNsdsServiceEnabled()) {
                sendMessage(obtainMessage(3, 11, 0));
            }
        } else {
            IMSLog.i(this.LOG_TAG, "refreshDeviceConfig: getConfigRefreshOnPowerUp");
            requestDeviceConfigRetrieval(15, i);
        }
    }

    public void activateSimDevice(int i, int i2) {
        if (!checkSimReady()) {
            notifySimErrorForDeviceActivation();
        } else if (this.mInvalidFingerPrintDate != null && new Date().getTime() - this.mInvalidFingerPrintDate.getTime() <= INVALID_FINGERPRINT_EXPIRATION_TIME) {
            IMSLog.i(this.LOG_TAG, "do not try it");
        } else if (!NSDSSharedPrefHelper.isDeviceInActivationProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "activateSimDevice: activate SIM device");
            queueSimDeviceActivation(i, i2);
        }
    }

    public void retrieveAkaToken(int i, int i2) {
        if (!checkSimReady()) {
            notifySimErrorForDeviceActivation();
        } else if (!NSDSSharedPrefHelper.isDeviceInActivationProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "retrieveAkaToken: retrieve aka token");
            queueRetrieveAkaToken(i, i2);
        }
    }

    public void deactivateSimDevice(int i) {
        queueSimDeviceDeactivation(i);
    }

    public void updateEntitlementUrl(String str) {
        String str2 = this.LOG_TAG;
        IMSLog.i(str2, "updateEntitlementUrl: url " + str);
        if (TextUtils.isEmpty(str)) {
            IMSLog.e(this.LOG_TAG, "updateEntitlementUrl: empty or null url");
            return;
        }
        this.mContext.getContentResolver().delete(EntitlementConfigContract.DeviceConfig.CONTENT_URI, (String) null, (String[]) null);
        this.mNSDSDatabaseHelper.deleteConfigAndResetDeviceAndAccountStatus(this.mBaseFlowImpl.getDeviceId(), this.mSimManager.getImsi(), this.mSimManager.getSimSlotIndex());
        NSDSSharedPrefHelper.setEntitlementServerUrl(this.mContext, this.mBaseFlowImpl.getDeviceId(), str);
    }

    private void queuePerformBootupProcedures() {
        removeMessages(45);
        sendEmptyMessageDelayed(45, 200);
    }

    public void queueRefreshDeviceConfig(int i) {
        if (!this.mNSDSDatabaseHelper.isDeviceConfigAvailable(this.mSimManager.getImsi()) || (!this.mOnSimSwapEvt.get() && NSDSConfigHelper.getConfigRefreshOnPowerUp(this.mContext, this.mSimManager.getImsi()))) {
            sendMessage(obtainMessage(14, Integer.valueOf(i)));
        }
    }

    /* access modifiers changed from: protected */
    public void queueEntitlementCheck(int i, int i2) {
        sendMessage(obtainMessage(15, i, i2));
    }

    /* access modifiers changed from: protected */
    public void queueEntitlementCheck(int i, int i2, long j) {
        sendMessageDelayed(obtainMessage(15, i, i2), j);
    }

    /* access modifiers changed from: protected */
    public void queueE911AddressUpdate(int i) {
        sendMessage(obtainMessage(19, Integer.valueOf(i)));
    }

    /* access modifiers changed from: protected */
    public void queueRemovePushToken(int i) {
        sendMessage(obtainMessage(17, Integer.valueOf(i)));
    }

    /* access modifiers changed from: private */
    public void queueSimDeviceActivation(int i, int i2) {
        sendMessage(obtainMessage(3, i, i2));
    }

    private void queueRetrieveAkaToken(int i, int i2) {
        sendMessage(obtainMessage(49, i, i2));
    }

    public void queueRefreshDeviceAndServiceInfo(int i, int i2) {
        sendMessage(obtainMessage(13, i, i2));
    }

    private void queueSimDeviceDeactivation(int i) {
        sendMessage(obtainMessage(4, 0, 0, Integer.valueOf(i)));
    }

    /* access modifiers changed from: private */
    public void queuePushTokenUpdateIf() {
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId()) && NSDSSharedPrefHelper.isGcmTokenSentToServer(this.mContext, this.mBaseFlowImpl.getDeviceId()) && mnoNsdsStrategy != null && !mnoNsdsStrategy.supportEntitlementCheck()) {
            queuePushTokenUpdateInEntitlementServer();
        }
    }

    public void queuePushTokenUpdateInEntitlementServer() {
        sendEmptyMessage(21);
    }

    /* access modifiers changed from: private */
    public void queueUpdateDeviceName() {
        sendMessage(obtainMessage(6, Boolean.FALSE));
    }

    /* access modifiers changed from: private */
    public void refreshEntitlementAndE911Info(int i, int i2) {
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy == null || !mnoNsdsStrategy.isNsdsUIAppSwitchOn(this.mBaseFlowImpl.getDeviceId(), this.mBaseFlowImpl.getSimManager().getSimSlotIndex()) || !NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.e(this.LOG_TAG, "refreshEntitlementAndE911Info: not ready to refresh");
            return;
        }
        if (E911AidValidator.validate(this.mNSDSDatabaseHelper.getNativeLineE911AidExp(this.mBaseFlowImpl.getDeviceId()))) {
            scheduleE911CheckTimer();
            if (i == 6) {
                IMSLog.i(this.LOG_TAG, "refreshEntitlementAndE911Info: still valid, no refresh");
                return;
            }
        }
        queueEntitlementCheck(i, i2);
    }

    private void refreshEntitlementAndE911InfoAutoOn(int i, int i2) {
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy == null || !mnoNsdsStrategy.isNsdsUIAppSwitchOn(this.mBaseFlowImpl.getDeviceId(), this.mBaseFlowImpl.getSimManager().getSimSlotIndex()) || !mnoNsdsStrategy.supportEntitlementCheck()) {
            IMSLog.e(this.LOG_TAG, "refreshEntitlementAndE911Info: not ready to refresh");
        } else {
            queueEntitlementCheck(i, i2);
        }
    }

    private void notifySimErrorForDeviceActivation() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.INVALID_SIM_STATUS));
        arrayList.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.FORCE_TOGGLE_OFF_ERROR_CODE));
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.SIM_DEVICE_ACTIVATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mSimManager.getSimSlotIndex());
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, arrayList);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    private void notifySimErrorForEntitlementAndLocTc() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.INVALID_SIM_STATUS));
        arrayList.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.FORCE_TOGGLE_OFF_ERROR_CODE));
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.ENTITLEMENT_AND_LOCTC_CHECK_COMPLETED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mSimManager.getSimSlotIndex());
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, arrayList);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    private void performSimDeviceImplicitActivation(int i, int i2) {
        IMSLog.i(this.LOG_TAG, "performSimDeviceImplicitActivation:");
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy != null && !mnoNsdsStrategy.isNsdsServiceEnabled()) {
            IMSLog.i(this.LOG_TAG, "NSDS is disabled, vail.");
        } else if (!NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "Device was not activated, activate it.");
            scheduleGetGcmRegistrationTokenIfTokenNotSent();
            ISIMDeviceImplicitActivation iSIMDeviceImplicitActivation = this.mSIMDeviceActivationFlow;
            if (iSIMDeviceImplicitActivation != null) {
                iSIMDeviceImplicitActivation.performSimDeviceImplicitActivation(i, i2);
            } else {
                IMSLog.e(this.LOG_TAG, "performSimDeviceImplicitActivation: flow not initiated, invalid request");
            }
        }
    }

    private void performAkaTokenRetrievalFlow(int i, int i2) {
        IMSLog.i(this.LOG_TAG, "performAkaTokenRetrievalFlow:");
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy != null && !mnoNsdsStrategy.isNsdsServiceEnabled()) {
            IMSLog.i(this.LOG_TAG, "NSDS is disabled, vail.");
        } else if (!NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "Device was not activated, retrieve only aka token");
            IAkaTokenRetrievalFlow iAkaTokenRetrievalFlow = this.mAkaTokenRetrievalFlow;
            if (iAkaTokenRetrievalFlow != null) {
                iAkaTokenRetrievalFlow.performAkaTokenRetrieval(i, i2);
            } else {
                IMSLog.e(this.LOG_TAG, "performAkaTokenRetrievalFlow: flow not initiated, invalid request");
            }
        }
    }

    private void performE911AddressUpdate(int i) {
        IEntitlementCheck iEntitlementCheck = this.mEntitlementCheckFlow;
        if (iEntitlementCheck != null) {
            iEntitlementCheck.performE911AddressUpdate(i);
        } else {
            IMSLog.e(this.LOG_TAG, "performE911AddressUpdate: flow not initiated, invalid request");
        }
    }

    private void performSimDeviceDeactivationFlow(int i) {
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy != null) {
            mnoNsdsStrategy.getSimDeviceDeactivationImpl(sServiceLooper, this.mContext, this.mBaseFlowImpl, this.mNSDSDatabaseHelper).deactivateDevice(i);
        } else {
            IMSLog.e(this.LOG_TAG, "performSimDeviceDeactivationFlow: mnoStrategy not initiated, invalid request");
        }
    }

    private void updatePushTokenInEntitlementServer() {
        this.mPushTokenUpdateFlow.updatePushToken();
    }

    private void performEntitlementCheck(int i, int i2) {
        if (checkEntitlementReadyStatus()) {
            IEntitlementCheck iEntitlementCheck = this.mEntitlementCheckFlow;
            if (iEntitlementCheck != null) {
                iEntitlementCheck.performEntitlementCheck(i, i2);
            } else {
                IMSLog.e(this.LOG_TAG, "performEntitlementCheck: flow not initiated, invalid request");
            }
        }
    }

    private boolean checkEntitlementReadyStatus() {
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy == null) {
            IMSLog.e(this.LOG_TAG, "checkEntitlementReadyStatus: mnoStrategy null");
            return false;
        } else if (!mnoNsdsStrategy.supportEntitlementCheck()) {
            String str = this.LOG_TAG;
            IMSLog.i(str, "checkEntitlementReadyStatus: entitlement not required for " + this.mSimManager.getSimOperator());
            return false;
        } else if (NSDSSharedPrefHelper.isDeviceInEntitlementProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(this.LOG_TAG, "checkEntitlementReadyStatus: entitlement in progress");
            return false;
        } else if (!mnoNsdsStrategy.isSIMDeviceActivationRequired()) {
            IMSLog.i(this.LOG_TAG, "checkEntitlementReadyStatus: device activation not required");
            return true;
        } else {
            if (!NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId()) && mnoNsdsStrategy.isNsdsUIAppSwitchOn(this.mBaseFlowImpl.getDeviceId(), this.mBaseFlowImpl.getSimManager().getSimSlotIndex())) {
                IMSLog.e(this.LOG_TAG, "checkEntitlementReadyStatus: device cannot be inactive !!");
                if (!NSDSSharedPrefHelper.isDeviceInActivationProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
                    queueSimDeviceActivation(11, 0);
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void performRemovePushToken(int i) {
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy != null && mnoNsdsStrategy.supportEntitlementCheck()) {
            IEntitlementCheck iEntitlementCheck = this.mEntitlementCheckFlow;
            if (iEntitlementCheck != null) {
                iEntitlementCheck.performRemovePushToken(i);
            } else {
                IMSLog.e(this.LOG_TAG, "performRemovePushToken: flow not initiated, invalid request");
            }
        }
    }

    public void updateE911Address() {
        IMSLog.i(this.LOG_TAG, "updateE911Address()");
        if (!checkSimReady()) {
            notifySimErrorForEntitlementAndLocTc();
        } else {
            queueE911AddressUpdate(5);
        }
    }

    public void handleVoWifToggleOnEvent() {
        IMSLog.i(this.LOG_TAG, "handleVoWifToggleOnEvent()");
        if (!checkSimReady()) {
            notifySimErrorForEntitlementAndLocTc();
        } else {
            queueEntitlementCheck(2, 0);
        }
    }

    public void handleVoWifToggleOffEvent() {
        IMSLog.i(this.LOG_TAG, "handleVoWifToggleOffEvent()");
        queueRemovePushToken(3);
    }

    private void requestDeviceConfigRetrieval(int i, int i2) {
        String str = this.LOG_TAG;
        IMSLog.i(str, "requestDeviceConfigRetrieval: eventType " + i + " retryCount " + i2);
        Intent intent = new Intent(this.mContext, EntitlementConfigService.class);
        intent.setAction(EntitlementNamespaces.EntitlementActions.ACTION_REFRESH_DEVICE_CONFIG);
        this.mContext.startService(intent);
    }

    /* access modifiers changed from: private */
    public void handleResultAfterEntitlementCheck(Intent intent) {
        int intExtra = intent.getIntExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, -1);
        boolean booleanExtra = intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false);
        ArrayList<Integer> integerArrayListExtra = intent.getIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("handleResultAfterEntitlementCheck: eventType " + intExtra + " success " + booleanExtra + " errors " + integerArrayListExtra);
        if (booleanExtra) {
            if (intExtra == 4 || intExtra == 1) {
                scheduleServiceProvisionCheckTimer();
            }
        } else if (intExtra != 6) {
            if (intExtra == 7) {
                IMSLog.i(this.LOG_TAG, "handleResultAfterEntitlementCheck: init retry count");
                this.mSvcProvCheckRetryCount = 0;
            } else if (intExtra != 10) {
                IMSLog.i(this.LOG_TAG, "handleResultAfterEntitlementCheck: no retry");
                return;
            }
            if (integerArrayListExtra != null && integerArrayListExtra.contains(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.SVC_PROVISION_PENDING_ERROR_CODE))) {
                scheduleServiceProvisionCheckRetryTimer();
            }
        } else {
            IMSLog.i(this.LOG_TAG, "handleResultAfterEntitlementCheck: init e911AID check timer");
            scheduleE911CheckTimer();
        }
    }

    private void handleSimNotSupported() {
        NSDSSharedPrefHelper.clearSimSwapPending(this.mContext, this.mBaseFlowImpl.getDeviceId());
        this.mOnSimSwapEvt.set(false);
        this.mNSDSDatabaseHelper.deleteNsdsConfigs(this.mSimManager.getImsi());
        NSDSConfigHelper.clear();
    }

    private void updateDeviceName() {
        Uri.Builder buildUpon = NSDSContractExt.Devices.buildUpdateDeviceNameUri((long) this.mNSDSDatabaseHelper.getDeviceId(this.mBaseFlowImpl.getDeviceId())).buildUpon();
        buildUpon.appendQueryParameter(NSDSContractExt.Devices.QUERY_PARAM_DEVICE_NAME, DeviceNameHelper.getDeviceName(this.mContext));
        this.mContext.getContentResolver().update(buildUpon.build(), new ContentValues(), (String) null, (String[]) null);
    }

    /* access modifiers changed from: protected */
    public IMnoNsdsStrategy getMnoNsdsStrategy() {
        MnoNsdsStrategyCreator instance = MnoNsdsStrategyCreator.getInstance(this.mContext, this.mSimManager.getSimSlotIndex());
        if (instance != null) {
            IMnoNsdsStrategy mnoStrategy = instance.getMnoStrategy();
            if (mnoStrategy == null) {
                return null;
            }
            String str = this.LOG_TAG;
            IMSLog.i(str, "getMnoNsdsStrategy: " + mnoStrategy.getClass().getName());
            return mnoStrategy;
        }
        IMSLog.i(this.LOG_TAG, "getMnoNsdsStrategy: mnoStrategyCreator is null");
        return null;
    }

    private void unregisterLoginReceiversAndStopTimers() {
        IntentScheduler.stopAllTimers(this.mContext);
    }

    /* access modifiers changed from: private */
    public void performOnDeviceReadyIf() {
        if (!checkSimReady()) {
            IMSLog.i(this.LOG_TAG, "SIM not supported...");
            handleSimNotSupported();
            return;
        }
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy == null) {
            IMSLog.e(this.LOG_TAG, "MNO Strategy has failed to be initiated...");
        } else if (!mnoNsdsStrategy.isDeviceProvisioned()) {
            IMSLog.i(this.LOG_TAG, "Waiting for OOBE setup complete...");
        } else if (!isDeviceReady()) {
            IMSLog.i(this.LOG_TAG, "Device is still waiting to be ready...");
        } else if (this.mHandleSimSwapAfterDeviceIsReady) {
            IMSLog.i(this.LOG_TAG, "handling case when simswap cache was ready but device was not ready at that time");
            handleSimSwapEvent("Device is Ready");
        } else {
            queuePerformBootupProcedures();
        }
    }

    public void onDeviceReady() {
        IMSLog.s(this.LOG_TAG, "onDeviceReady");
        DeviceIdHelper.getDeviceId(this.mContext, this.mSimManager.getSimSlotIndex());
        performOnDeviceReadyIf();
    }

    private void performBootupProcedures() {
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy != null) {
            for (Integer intValue : mnoNsdsStrategy.getOperationsForBootupInit(this.mBaseFlowImpl.getDeviceId(), this.mBaseFlowImpl.getSimManager().getSimSlotIndex())) {
                sendEmptyMessage(intValue.intValue());
            }
            return;
        }
        IMSLog.e(this.LOG_TAG, "performBootupProcedures: mnoStrategy cannot be null !!!");
    }

    private ImsRegistration getImsRegistration(int i, boolean z) {
        Iterator<ImsRegistration> it = this.mRegistrationList.iterator();
        while (it.hasNext()) {
            ImsRegistration next = it.next();
            if (next != null && next.getPhoneId() == i && next.getImsProfile().hasEmergencySupport() == z) {
                return next;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void editRegiListOnRegistered(ImsRegistration imsRegistration) {
        int i;
        ImsRegistration next;
        Iterator<ImsRegistration> it = this.mRegistrationList.iterator();
        while (true) {
            if (!it.hasNext()) {
                i = -1;
                break;
            }
            next = it.next();
            if (next.getHandle() == imsRegistration.getHandle() || (next.getPhoneId() == imsRegistration.getPhoneId() && TextUtils.equals(next.getImsProfile().getName(), imsRegistration.getImsProfile().getName()))) {
                i = this.mRegistrationList.indexOf(next);
            }
        }
        i = this.mRegistrationList.indexOf(next);
        if (i == -1) {
            this.mRegistrationList.add(imsRegistration);
        } else {
            this.mRegistrationList.set(i, imsRegistration);
        }
    }

    /* access modifiers changed from: private */
    public void removeFromRegiListOnDeregistered(ImsRegistration imsRegistration) {
        Iterator it = new ArrayList(this.mRegistrationList).iterator();
        while (it.hasNext()) {
            ImsRegistration imsRegistration2 = (ImsRegistration) it.next();
            if (imsRegistration2.getHandle() == imsRegistration.getHandle()) {
                this.mRegistrationList.remove(imsRegistration2);
            }
        }
    }

    private void onRegistration(ImsRegistration imsRegistration) {
        ImsProfile imsProfile = imsRegistration.getImsProfile();
        if (imsProfile == null || imsProfile.hasEmergencySupport()) {
            IMSLog.i(this.LOG_TAG, "onRegistration: emergency registration, skip");
            return;
        }
        String str = this.LOG_TAG;
        IMSLog.i(str, "onRegistration: " + imsRegistration.toString());
        IntentScheduler.stopTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_CHECK_REG_STATE);
        ArrayList<String> msisdnsFromImsRegistration = getMsisdnsFromImsRegistration(imsRegistration);
        List<String> readyForUseMsisdns = this.mNSDSDatabaseHelper.getReadyForUseMsisdns(this.mBaseFlowImpl.getDeviceId());
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "onRegistration: prevReadyForUseMsisdns " + readyForUseMsisdns);
        this.mNSDSDatabaseHelper.updateRegistationStatusForLines(msisdnsFromImsRegistration, 1, 0, 2);
        ArrayList arrayList = new ArrayList();
        for (String next : msisdnsFromImsRegistration) {
            if (!readyForUseMsisdns.contains(next)) {
                String str3 = this.LOG_TAG;
                IMSLog.s(str3, "onRegistration: add to added list:" + next);
                arrayList.add(next);
            }
        }
        if (arrayList.size() > 0) {
            broadcastLinesReadyStatusUpdated(arrayList, 0, 2);
        } else {
            markLineDeregisteredIfRemovedInRereg(msisdnsFromImsRegistration, readyForUseMsisdns);
        }
    }

    private void markLineDeregisteredIfRemovedInRereg(List<String> list, List<String> list2) {
        ArrayList arrayList = new ArrayList();
        for (String next : list2) {
            if (!list.contains(next)) {
                String str = this.LOG_TAG;
                IMSLog.s(str, "markLineDeregisteredIfRemovedInRereg: add to deleted list:" + next);
                arrayList.add(next);
            }
        }
        if (arrayList.size() > 0) {
            this.mNSDSDatabaseHelper.updateRegistationStatusForLines(arrayList, 0, 2, 0);
            broadcastLinesReadyStatusUpdated(arrayList, 2, 0);
        }
    }

    private ArrayList<String> getMsisdnsFromImsRegistration(ImsRegistration imsRegistration) {
        List<NameAddr> impuList = imsRegistration.getImpuList();
        ArrayList<String> arrayList = new ArrayList<>();
        for (NameAddr uri : impuList) {
            String msisdn = uri.getUri().getMsisdn();
            if (msisdn != null) {
                if (msisdn.startsWith("+")) {
                    msisdn = msisdn.substring(1);
                }
                if (msisdn.length() <= 11 && !arrayList.contains(msisdn)) {
                    arrayList.add(msisdn);
                }
            }
        }
        String str = this.LOG_TAG;
        IMSLog.s(str, "getMsisdnsFromImsRegistration:" + arrayList);
        return arrayList;
    }

    /* access modifiers changed from: private */
    public void broadcastLinesReadyStatusUpdated(ArrayList<String> arrayList, int i, int i2) {
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.LINES_READY_STATUS_UPDATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.FROM_REG_STATUS, i);
        intent.putExtra(NSDSNamespaces.NSDSExtras.TO_REG_STATUS, i2);
        intent.putStringArrayListExtra(NSDSNamespaces.NSDSExtras.MSISDN_LIST, arrayList);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    private void onDeregistration(ImsRegistration imsRegistration) {
        ImsRegistration imsRegistration2;
        ImsProfile imsProfile = imsRegistration.getImsProfile();
        if (imsProfile == null || imsProfile.hasEmergencySupport()) {
            IMSLog.i(this.LOG_TAG, "onDeregistration: emergency deregistration, skip");
            return;
        }
        String str = this.LOG_TAG;
        IMSLog.i(str, "onDeregistration: " + imsRegistration.toString());
        if (this.mRegistrationList.size() <= 0 || ((imsRegistration2 = getImsRegistration(this.mSimManager.getSimSlotIndex(), false)) != null && TextUtils.equals(imsRegistration.getImsProfile().getName(), imsRegistration2.getImsProfile().getName()))) {
            IntentScheduler.stopTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_CHECK_REG_STATE);
            ArrayList<String> msisdnsFromImsRegistration = getMsisdnsFromImsRegistration(imsRegistration);
            String str2 = this.LOG_TAG;
            IMSLog.s(str2, "onDeregistration: updated Msisdn list:" + msisdnsFromImsRegistration.toString());
            this.mNSDSDatabaseHelper.updateRegistationStatusForLines(2, 0);
            broadcastLinesReadyStatusUpdated(msisdnsFromImsRegistration, 2, 0);
            return;
        }
        IMSLog.i(this.LOG_TAG, "onDeregistration: abnormal deregistration, skip");
    }

    /* access modifiers changed from: private */
    public void scheduleNsdsAppFlowRetryIf(Intent intent) {
        ArrayList<Integer> integerArrayList;
        int intExtra = intent.getIntExtra("retry_count", -1);
        boolean z = false;
        int intExtra2 = intent.getIntExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, 0);
        int intExtra3 = intent.getIntExtra(NSDSNamespaces.NSDSExtras.ORIG_ERROR_CODE, -1);
        if (!intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false) && shouldRetry(intExtra3, intExtra, intExtra2)) {
            Bundle bundle = new Bundle();
            bundle.putInt("retry_count", intExtra + 1);
            bundle.putInt(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, intExtra2);
            if (NSDSNamespaces.NSDSActions.SIM_DEVICE_ACTIVATED.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                if (extras == null || (integerArrayList = extras.getIntegerArrayList(NSDSNamespaces.NSDSExtras.ERROR_CODES)) == null || !integerArrayList.contains(1400)) {
                    z = true;
                }
                IMSLog.i(this.LOG_TAG, "Retry sim device implicit activation:" + z);
                if (z) {
                    IntentScheduler.scheduleTimer(this.mContext, this.mSimManager.getSimSlotIndex(), ACTION_SIM_DEVICE_ACTIVATION, extras, 30000);
                }
            }
        }
    }

    private boolean shouldRetry(int i, int i2, int i3) {
        String str = this.LOG_TAG;
        IMSLog.i(str, "shouldRetry: errorCode " + i + " retryCount " + i2 + " deviceEventType " + i3);
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        if (mnoNsdsStrategy != null && mnoNsdsStrategy.requireRetryBootupProcedure()) {
            if (i2 > 4) {
                String str2 = this.LOG_TAG;
                IMSLog.i(str2, "shouldRetry: exceeded max retry " + i2);
                return false;
            } else if (i == -1 || i >= 1001) {
                String str3 = this.LOG_TAG;
                IMSLog.i(str3, "shouldRetry: NSDS error, retry " + i2);
                return true;
            } else if (i == 486 || i == 408 || i == 500 || i == 503 || i == 480) {
                String str4 = this.LOG_TAG;
                IMSLog.i(str4, "shouldRetry: HTTP error, retry " + i2);
                return true;
            }
        }
        return false;
    }

    public void dump() {
        String str = this.LOG_TAG;
        IMSLog.dump(str, "Dump of " + getClass().getSimpleName());
        IMSLog.increaseIndent(this.LOG_TAG);
        this.mEventLog.dump();
        this.mBaseFlowImpl.getNSDSClient().getResponseHandler().dump();
        IMSLog.decreaseIndent(this.LOG_TAG);
    }

    public void handleMessage(Message message) {
        String str = this.LOG_TAG;
        IMSLog.i(str, "handleMesasge: event " + message.what);
        int i = message.what;
        if (i == 0) {
            onRegistration((ImsRegistration) message.obj);
        } else if (i == 1) {
            onDeregistration((ImsRegistration) message.obj);
        } else if (i == 3) {
            performSimDeviceImplicitActivation(message.arg1, message.arg2);
        } else if (i == 4) {
            performSimDeviceDeactivationFlow(((Integer) message.obj).intValue());
        } else if (i == 6) {
            updateDeviceName();
        } else if (i == 49) {
            performAkaTokenRetrievalFlow(message.arg1, message.arg2);
        } else if (i == 51) {
            refreshEntitlementAndE911InfoAutoOn(1, 0);
        } else if (i == 14) {
            refreshDeviceConfigIf(((Integer) message.obj).intValue());
        } else if (i != 15) {
            switch (i) {
                case 17:
                    performRemovePushToken(((Integer) message.obj).intValue());
                    return;
                case 18:
                    onFlightMode(((Integer) message.obj).intValue());
                    return;
                case 19:
                    performE911AddressUpdate(((Integer) message.obj).intValue());
                    return;
                case 20:
                    break;
                case 21:
                    updatePushTokenInEntitlementServer();
                    return;
                default:
                    switch (i) {
                        case 40:
                            handleSimSwapEvent("Forced");
                            return;
                        case 41:
                            performProceduresOnConfigRefreshComplete();
                            return;
                        case 42:
                            enableOrDisableNSDSService();
                            return;
                        case 43:
                            scheduleGetGcmRegistrationTokenIfTokenNotSent();
                            return;
                        case 44:
                            refreshEntitlementAndE911Info(1, 0);
                            return;
                        case 45:
                            break;
                        default:
                            return;
                    }
            }
            performBootupProcedures();
        } else {
            performEntitlementCheck(message.arg1, message.arg2);
        }
    }
}
