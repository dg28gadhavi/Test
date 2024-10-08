package com.sec.internal.ims.imsservice;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.TelephonyFrameworkInitializer;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.ims.cmc.ISemCmcRecordingListener;
import com.samsung.android.ims.cmc.SemCmcRecordingInfo;
import com.sec.ims.DialogEvent;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.ims.ICentralMsgStoreService;
import com.sec.ims.IDialogEventListener;
import com.sec.ims.IEpdgListener;
import com.sec.ims.IImsDmConfigListener;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.IImsService;
import com.sec.ims.IRttEventListener;
import com.sec.ims.ISimMobilityStatusListener;
import com.sec.ims.ImsEventListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.cmc.CmcCallInfo;
import com.sec.ims.configuration.DATA;
import com.sec.ims.extensions.ReflectionUtils;
import com.sec.ims.ft.IImsOngoingFtEventListener;
import com.sec.ims.im.IImSessionListener;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.NvConfiguration;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.helper.os.SystemWrapper;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.ImsFramework;
import com.sec.internal.ims.aec.AECModule;
import com.sec.internal.ims.cmstore.CloudMessageService$$ExternalSyntheticLambda0;
import com.sec.internal.ims.config.ConfigModule;
import com.sec.internal.ims.core.GeolocationController;
import com.sec.internal.ims.core.GeolocationController$$ExternalSyntheticLambda0;
import com.sec.internal.ims.core.NtpTimeController;
import com.sec.internal.ims.core.PdnController;
import com.sec.internal.ims.core.RawSipManager;
import com.sec.internal.ims.core.RegistrationManagerBase;
import com.sec.internal.ims.core.WfcEpdgManager;
import com.sec.internal.ims.core.cmc.CmcAccountManager;
import com.sec.internal.ims.core.handler.HandlerFactory;
import com.sec.internal.ims.core.iil.IilManager;
import com.sec.internal.ims.core.imslogger.ImsDiagnosticMonitorNotificationManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.fcm.FcmHandler;
import com.sec.internal.ims.fcm.interfaces.IFcmHandler;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.ServiceModuleManager;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.servicemodules.tapi.service.extension.ServiceExtensionManager;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.ValidationHelper;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.DmConfigModule;
import com.sec.internal.ims.settings.GlobalSettingsManager;
import com.sec.internal.ims.settings.ImsAutoUpdate;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.ims.settings.ImsSimMobilityUpdate;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.imsphone.cmc.CmcConnectivityController;
import com.sec.internal.imsphone.cmc.ICmcConnectivityController;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.aec.IAECModule;
import com.sec.internal.interfaces.ims.cmstore.ICmsModule;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.INtpTimeController;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRawSipSender;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISequentialInitializable;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IWfcEpdgManager;
import com.sec.internal.interfaces.ims.core.handler.IHandlerFactory;
import com.sec.internal.interfaces.ims.core.iil.IIilManager;
import com.sec.internal.interfaces.ims.core.imslogger.IImsDiagMonitor;
import com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.sms.ISmsServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.CriticalLogger;
import com.sec.internal.log.IMSLog;
import com.sec.internal.log.IMSLogTimer;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ImsServiceStub extends IImsService.Stub implements IImsFramework {
    private static final int LISTENER_DEFAULT_INDEX = 100;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ImsServiceStub.class.getSimpleName();
    private static final String PERMISSION = "com.sec.imsservice.PERMISSION";
    private static final String SOFT_RESET_PERMISSION = "com.sec.android.settings.permission.SOFT_RESET";
    private static final String TC_POPUP_USER_ACCEPT = "info/tc_popup_user_accept";
    private static boolean mIsExplicitGcCalled = false;
    /* access modifiers changed from: private */
    public static boolean mIsImsAvailable = false;
    private static final AtomicInteger mListenerIndex = new AtomicInteger(0);
    /* access modifiers changed from: private */
    public static boolean mUserUnlocked = false;
    private static ImsServiceStub sInstance = null;
    private IAECModule mAECModule = null;
    private CallStateTracker mCallStateTracker = null;
    private CmcAccountManager mCmcAccountManager = null;
    private CmcConnectivityController mCmcConnectivityController = null;
    /* access modifiers changed from: private */
    public ConfigModule mConfigModule = null;
    /* access modifiers changed from: private */
    public Context mContext;
    private Handler mCoreHandler;
    private final HandlerThread mCoreThread;
    private BroadcastReceiver mDefaultSmsPackageChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String str;
            if (intent != null && "android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED_INTERNAL".equals(intent.getAction())) {
                try {
                    str = Telephony.Sms.getDefaultSmsPackage(ImsServiceStub.this.mContext);
                } catch (Exception e) {
                    String r4 = ImsServiceStub.LOG_TAG;
                    IMSLog.e(r4, "Failed to get currentPackage: " + e);
                    str = null;
                }
                String r42 = ImsServiceStub.LOG_TAG;
                IMSLog.d(r42, "onChange: MessageApplication is changed : " + str);
                if (str != null) {
                    IImModule imModule = ImsServiceStub.this.mServiceModuleManager.getImModule();
                    if (imModule != null) {
                        imModule.handleEventDefaultAppChanged();
                    }
                    if (ImsServiceStub.this.mConfigModule != null) {
                        ImsServiceStub.this.mConfigModule.onDefaultSmsPackageChanged();
                    }
                    ISmsServiceModule smsServiceModule = ImsServiceStub.this.mServiceModuleManager.getSmsServiceModule();
                    if (smsServiceModule != null) {
                        smsServiceModule.handleEventDefaultAppChanged();
                    }
                    ICmsModule cmsModule = ImsServiceStub.this.mServiceModuleManager.getCmsModule();
                    if (cmsModule != null) {
                        cmsModule.handleEventDefaultAppChanged();
                    }
                    ICapabilityDiscoveryModule capabilityDiscoveryModule = ImsServiceStub.this.mServiceModuleManager.getCapabilityDiscoveryModule();
                    if (capabilityDiscoveryModule != null) {
                        capabilityDiscoveryModule.onDefaultSmsPackageChanged();
                    }
                }
            }
        }
    };
    private DmConfigModule mDmConfigModule = null;
    private ExecutorService mDumpExecutor;
    private SimpleEventLog mEventLog;
    private FcmHandler mFcmHandler = null;
    private GeolocationController mGeolocationController = null;
    private HandlerFactory mHandlerFactory = null;
    private List<IIilManager> mIilManagers = new ArrayList();
    private ImsDiagnosticMonitorNotificationManager mImsDiagMonitor = null;
    /* access modifiers changed from: private */
    public final Map<String, CallBack<? extends IInterface>> mListenerTokenMap = new ConcurrentHashMap();
    private NtpTimeController mNtpTimeController = null;
    private PdnController mPdnController = null;
    private RawSipManager mRawSipManager;
    private RcsPolicyManager mRcsPolicyManager = null;
    /* access modifiers changed from: private */
    public RegistrationManagerBase mRegistrationManager = null;
    private List<ISequentialInitializable> mSequentialInitializer = new ArrayList();
    private ServiceExtensionManager mServiceExtensionManager = null;
    /* access modifiers changed from: private */
    public ServiceModuleManager mServiceModuleManager = null;
    private ISimManager mSimManager = null;
    private List<ISimManager> mSimManagers = new ArrayList();
    private BroadcastReceiver mUserUnlockReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                IMSLog.i(ImsServiceStub.LOG_TAG, "ACTION_USER_UNLOCKED received");
                ImsServiceStub.mUserUnlocked = true;
                if (ImsServiceStub.this.mRegistrationManager != null && ImsServiceStub.mIsImsAvailable) {
                    ImsServiceStub.explicitGC();
                    ImsServiceStub.this.mRegistrationManager.bootCompleted();
                }
                if (SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) >= 28) {
                    ImsSharedPrefHelper.migrateToCeStorage(context);
                }
                IntentUtil.sendBroadcast(context, new Intent(NSDSNamespaces.NSDSActions.DEVICE_READY_AFTER_BOOTUP));
            }
        }
    };
    /* access modifiers changed from: private */
    public WfcEpdgManager mWfcEpdgManager = null;

    public void registerCallback(ImsEventListener imsEventListener, String str) {
    }

    public String registerCmsRegistrationListenerByPhoneId(ICentralMsgStoreService iCentralMsgStoreService, int i) throws RemoteException {
        return null;
    }

    public void unregisterCallback(ImsEventListener imsEventListener) {
    }

    public void unregisterCmsRegistrationListenerByPhoneId(String str, int i) throws RemoteException {
    }

    protected ImsServiceStub(Context context) {
        this.mContext = context;
        this.mCoreThread = new HandlerThread(getClass().getSimpleName());
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 300);
        new ImsFramework(this);
        checkUt(context);
    }

    public static ImsServiceStub getInstance() {
        while (getInstanceInternal() == null) {
            IMSLog.e(LOG_TAG, "instance is null...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return getInstanceInternal();
    }

    protected static <T extends IInterface> String getTokenOfListener(T t) {
        AtomicInteger atomicInteger = mListenerIndex;
        atomicInteger.compareAndSet(Integer.MAX_VALUE, 100);
        StringBuilder sb = new StringBuilder();
        sb.append(t == null ? "null" : Integer.valueOf(t.hashCode()));
        sb.append("$");
        sb.append(atomicInteger.incrementAndGet());
        return sb.toString();
    }

    private static synchronized ImsServiceStub getInstanceInternal() {
        ImsServiceStub imsServiceStub;
        synchronized (ImsServiceStub.class) {
            imsServiceStub = sInstance;
        }
        return imsServiceStub;
    }

    public static synchronized ImsServiceStub makeImsService(Context context) {
        synchronized (ImsServiceStub.class) {
            if (sInstance != null) {
                IMSLog.d(LOG_TAG, "Already created.");
                ImsServiceStub imsServiceStub = sInstance;
                return imsServiceStub;
            }
            String str = LOG_TAG;
            IMSLog.i(str, "Creating IMSService");
            IMSLogTimer.setLatchStartTime(-1);
            ImsServiceStub imsServiceStub2 = new ImsServiceStub(context);
            sInstance = imsServiceStub2;
            imsServiceStub2.createModules();
            sInstance.init();
            IMSLog.i(str, "Done.");
            IMSLog.c(LogClass.GEN_IMS_SERVICE_CREATED, "PID:" + Process.myPid());
            ImsServiceStub imsServiceStub3 = sInstance;
            return imsServiceStub3;
        }
    }

    /* access modifiers changed from: private */
    public static void explicitGC() {
        if (mIsExplicitGcCalled) {
            return;
        }
        if (!Debug.isProductShip() || mUserUnlocked) {
            new Thread(new ImsServiceStub$$ExternalSyntheticLambda6()).start();
            mIsExplicitGcCalled = true;
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ void lambda$explicitGC$0() {
        IMSLog.i(LOG_TAG, "Call explicit GC");
        SystemWrapper.explicitGc();
    }

    public static boolean isImsAvailable() {
        return mIsImsAvailable;
    }

    private static void checkUt(Context context) {
        try {
            if (context.getPackageManager().getPackageUid("com.salab.issuetracker", 0) == 1000) {
                IMSLog.i(LOG_TAG, "issueTracker found should be UT device");
                IMSLog.setIsUt(true);
            }
        } catch (PackageManager.NameNotFoundException unused) {
            IMSLog.i(LOG_TAG, "issueTracker not found");
        }
    }

    public void registerDefaultSmsPackageChangeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED_INTERNAL");
        this.mContext.registerReceiver(this.mDefaultSmsPackageChangeReceiver, intentFilter);
    }

    public void registerUserUnlockReceiver() {
        this.mContext.registerReceiver(this.mUserUnlockReceiver, new IntentFilter("android.intent.action.USER_UNLOCKED"));
    }

    private void registerFactoryResetReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImsConstants.Intents.ACTION_SOFT_RESET);
        intentFilter.addAction(ImsConstants.Intents.ACTION_RESET_NETWORK_SETTINGS);
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String r3 = ImsServiceStub.LOG_TAG;
                IMSLog.d(r3, "received intent : " + intent.getAction());
                String action = intent.getAction();
                action.hashCode();
                if (action.equals(ImsConstants.Intents.ACTION_SOFT_RESET)) {
                    for (int i = 0; i < ImsServiceStub.this.getPhoneCount(); i++) {
                        ImsServiceStub.this.factoryReset(i);
                    }
                } else if (action.equals(ImsConstants.Intents.ACTION_RESET_NETWORK_SETTINGS) && intent.hasExtra(ImsConstants.Intents.EXTRA_RESET_NETWORK_SUBID)) {
                    ImsServiceStub.this.factoryReset(SimManagerFactory.getSlotId(intent.getIntExtra(ImsConstants.Intents.EXTRA_RESET_NETWORK_SUBID, -1)));
                }
                if (ImsServiceStub.this.mWfcEpdgManager != null) {
                    ImsServiceStub.this.mWfcEpdgManager.onResetSetting(intent);
                }
            }
        }, UserHandle.ALL, intentFilter, SOFT_RESET_PERMISSION, (Handler) null);
    }

    private void registerPackageManagerReceiver() {
        IMSLog.d(LOG_TAG, "registerPackageMgrListener");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter.addDataSchemeSpecificPart(ImsConstants.Packages.PACKAGE_SIMMOBILITY_KIT, 0);
        intentFilter.addDataSchemeSpecificPart(ImsConstants.Packages.PACKAGE_SEC_MSG, 0);
        intentFilter.addDataScheme("package");
        String smkVersion = getSmkVersion();
        if (smkVersion != null) {
            writeSmkVerData(smkVersion);
        }
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String str = "";
                if (intent.getData() != null) {
                    str = intent.getData().toString().replace("package:", str);
                }
                String action = intent.getAction();
                String r5 = ImsServiceStub.LOG_TAG;
                IMSLog.d(r5, "packageStatus : " + action + ", packageName : " + str);
                if (TextUtils.equals(ImsConstants.Packages.PACKAGE_SIMMOBILITY_KIT, str)) {
                    action.hashCode();
                    if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                        String r4 = ImsServiceStub.this.getSmkVersion();
                        if (!ImsServiceStub.this.isPreloadedSmk(r4)) {
                            ImsServiceStub.this.startDeviceConfigService();
                        }
                        ImsServiceStub.this.writeSmkVerData(r4);
                    }
                } else if (TextUtils.equals(ImsConstants.Packages.PACKAGE_SEC_MSG, str)) {
                    action.hashCode();
                    if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                        ICapabilityDiscoveryModule capabilityDiscoveryModule = ImsServiceStub.this.mServiceModuleManager.getCapabilityDiscoveryModule();
                        if (capabilityDiscoveryModule == null || !capabilityDiscoveryModule.isRunning()) {
                            IMSLog.d(ImsServiceStub.LOG_TAG, "registerPackageManagerReceiver:CapaModule not available");
                            return;
                        }
                        IMSLog.d(ImsServiceStub.LOG_TAG, "registerPackageManagerReceiver: notify to CapaModule");
                        capabilityDiscoveryModule.onPackageUpdated(str);
                    }
                }
            }
        }, intentFilter);
    }

    /* access modifiers changed from: private */
    public void writeSmkVerData(String str) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DiagnosisConstants.KEY_SEND_MODE, 1);
        contentValues.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 0);
        contentValues.put(DiagnosisConstants.DRPT_KEY_SMK_VERSION, str);
        ImsLogAgentUtil.storeLogToAgent(SimUtil.getActiveDataPhoneId(), this.mContext, "DRPT", contentValues);
    }

    /* access modifiers changed from: private */
    public String getSmkVersion() {
        String str = null;
        try {
            str = this.mContext.getPackageManager().getPackageInfo(ImsConstants.Packages.PACKAGE_SIMMOBILITY_KIT, 0).versionName;
            String str2 = LOG_TAG;
            IMSLog.d(str2, "Get SMK version Success : " + str);
            return str;
        } catch (Exception unused) {
            IMSLog.e(LOG_TAG, "fail to get versionName");
            return str;
        }
    }

    /* access modifiers changed from: private */
    public boolean isPreloadedSmk(String str) {
        return str == null || str.equals(ImsConstants.Packages.SMK_PRELOADED_VERSION);
    }

    /* access modifiers changed from: private */
    public void startDeviceConfigService() {
        this.mEventLog.logAndAdd("call SMK start");
        Intent intent = new Intent();
        intent.setClassName(ImsConstants.Packages.PACKAGE_SIMMOBILITY_KIT, ImsConstants.Packages.CLASS_SIMMOBILITY_KIT_UPDATE);
        this.mContext.startForegroundService(intent);
    }

    public void handleIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            this.mServiceModuleManager.handleIntent(intent);
        }
    }

    private void createModules() {
        int i;
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.i(str, "createModules started");
        this.mCoreThread.start();
        Looper looper = this.mCoreThread.getLooper();
        this.mCoreHandler = new Handler(looper);
        IMSLog.i(str, "Creating SimManagers.");
        SimManagerFactory.createInstance(looper, this.mContext);
        this.mSimManager = SimManagerFactory.getSimManager();
        this.mSimManagers.clear();
        this.mSimManagers.addAll(SimManagerFactory.getAllSimManagers());
        this.mSequentialInitializer.addAll(this.mSimManagers);
        if (!DeviceUtil.isTablet() || !DeviceUtil.isWifiOnlyModel()) {
            i = TelephonyManagerWrapper.getInstance(this.mContext).getPhoneCount();
        } else {
            i = 0;
        }
        IMSLog.i(str, "Creating IIilManager: count: " + i);
        for (int i2 = 0; i2 < i; i2++) {
            this.mIilManagers.add(i2, new IilManager(this.mContext, i2, this));
        }
        String str2 = LOG_TAG;
        IMSLog.i(str2, "Creating WfcEpdgManager.");
        WfcEpdgManager wfcEpdgManager = new WfcEpdgManager(this.mContext, looper, this);
        this.mWfcEpdgManager = wfcEpdgManager;
        this.mSequentialInitializer.add(wfcEpdgManager);
        IMSLog.i(str2, "Creating PdnController.");
        PdnController pdnController = new PdnController(this.mContext, looper, this);
        this.mPdnController = pdnController;
        this.mSequentialInitializer.add(pdnController);
        IMSLog.i(str2, "Creating DmConfigModule.");
        DmConfigModule dmConfigModule = new DmConfigModule(this.mContext, looper, this);
        this.mDmConfigModule = dmConfigModule;
        this.mSequentialInitializer.add(dmConfigModule);
        IMSLog.i(str2, "Creating CmcAccountManager.");
        CmcAccountManager cmcAccountManager = new CmcAccountManager(this.mContext, looper);
        this.mCmcAccountManager = cmcAccountManager;
        this.mSequentialInitializer.add(cmcAccountManager);
        IMSLog.i(str2, "Creating RcsPolicyManager.");
        RcsPolicyManager rcsPolicyManager = new RcsPolicyManager(looper, this.mContext, this.mSimManagers);
        this.mRcsPolicyManager = rcsPolicyManager;
        this.mSequentialInitializer.add(rcsPolicyManager);
        IMSLog.i(str2, "Creating RegistrationManager.");
        Context context2 = this.mContext;
        RegistrationManagerBase registrationManagerBase = new RegistrationManagerBase(looper, this, context2, this.mPdnController, this.mSimManagers, TelephonyManagerWrapper.getInstance(context2), this.mCmcAccountManager, this.mRcsPolicyManager);
        this.mRegistrationManager = registrationManagerBase;
        this.mSequentialInitializer.add(registrationManagerBase);
        IMSLog.i(str2, "Creating ConfigModule.");
        ConfigModule configModule = new ConfigModule(looper, this.mContext, this.mRegistrationManager);
        this.mConfigModule = configModule;
        this.mSequentialInitializer.add(configModule);
        IMSLog.i(str2, "Creating HandlerFactory.");
        HandlerFactory createStackHandler = HandlerFactory.createStackHandler(looper, this.mContext, this);
        this.mHandlerFactory = createStackHandler;
        this.mSequentialInitializer.add(createStackHandler);
        IMSLog.i(str2, "Creating ServiceModuleManager.");
        ServiceModuleManager serviceModuleManager = new ServiceModuleManager(looper, this.mContext, this, this.mSimManagers, this.mRegistrationManager, this.mHandlerFactory);
        this.mServiceModuleManager = serviceModuleManager;
        this.mSequentialInitializer.add(serviceModuleManager);
        IMSLog.i(str2, "Creating AECModule.");
        AECModule aECModule = new AECModule(looper, this.mContext);
        this.mAECModule = aECModule;
        this.mSequentialInitializer.add(aECModule);
        IMSLog.i(str2, "Creating GeolocationController.");
        GeolocationController geolocationController = new GeolocationController(this.mContext, looper, this.mRegistrationManager);
        this.mGeolocationController = geolocationController;
        this.mSequentialInitializer.add(geolocationController);
        CallStateTracker callStateTracker = new CallStateTracker(this.mContext, this.mCoreHandler, this.mServiceModuleManager);
        this.mCallStateTracker = callStateTracker;
        this.mSequentialInitializer.add(callStateTracker);
        IMSLog.i(str2, "Creating ImsDiagnosticMonitorNotificationManager.");
        ImsDiagnosticMonitorNotificationManager imsDiagnosticMonitorNotificationManager = new ImsDiagnosticMonitorNotificationManager(this.mContext, looper);
        this.mImsDiagMonitor = imsDiagnosticMonitorNotificationManager;
        this.mSequentialInitializer.add(imsDiagnosticMonitorNotificationManager);
        IMSLog.i(str2, "Creating NtpTimeController.");
        NtpTimeController ntpTimeController = new NtpTimeController(this.mContext, looper);
        this.mNtpTimeController = ntpTimeController;
        this.mSequentialInitializer.add(ntpTimeController);
        this.mRawSipManager = new RawSipManager(this.mContext);
        this.mRegistrationManager.setConfigModule(this.mConfigModule);
        this.mRegistrationManager.setGeolocationController(this.mGeolocationController);
        this.mRegistrationManager.setStackInterface(this.mHandlerFactory.getRegistrationStackAdaptor());
        this.mRcsPolicyManager.setRegistrationManager(this.mRegistrationManager);
        this.mDmConfigModule.setRegistrationManager(this.mRegistrationManager);
        this.mCmcConnectivityController = new CmcConnectivityController(looper, getRegistrationManager());
    }

    private void init() {
        IMSLog.i(LOG_TAG, "init started");
        this.mSequentialInitializer.forEach(new CloudMessageService$$ExternalSyntheticLambda0());
        this.mSequentialInitializer.clear();
        this.mRegistrationManager.setVolteServiceModule(this.mServiceModuleManager.getVolteServiceModule());
        SimManagerFactory.initInstances();
        this.mRawSipManager.init(this.mHandlerFactory.getRawSipHandler());
        registerFactoryResetReceiver();
        if (ValidationHelper.isTapiAuthorisationSupports()) {
            ServiceExtensionManager instance = ServiceExtensionManager.getInstance(this.mContext);
            this.mServiceExtensionManager = instance;
            instance.start();
        }
        try {
            if (Build.VERSION.SEM_INT >= 2716) {
                SemImsServiceStub.makeSemImsService(this.mContext);
            }
        } catch (NoSuchFieldError e) {
            IMSLog.e(LOG_TAG, e.toString());
        }
        registerDefaultSmsPackageChangeReceiver();
        registerPackageManagerReceiver();
        registerUserUnlockReceiver();
        linkToPhoneDeath();
        checkGrantAppOpsPermission();
    }

    public String registerSimMobilityStatusListener(ISimMobilityStatusListener iSimMobilityStatusListener, boolean z, int i) {
        if (isPermissionGranted()) {
            String str = LOG_TAG;
            IMSLog.d(str, i, "registerSimMobilityStatusListener: broadcast = " + z);
            if (i == -1) {
                IMSLog.d(str, "Requested registerSimMobilityStatusListener without phoneId. register it by all phoneId.");
                this.mSimManagers.forEach(new ImsServiceStub$$ExternalSyntheticLambda0(this, iSimMobilityStatusListener));
            } else {
                this.mRegistrationManager.registerSimMobilityStatusListener(iSimMobilityStatusListener, z, i);
            }
            String tokenOfListener = getTokenOfListener(iSimMobilityStatusListener);
            this.mListenerTokenMap.put(tokenOfListener, new CallBack(iSimMobilityStatusListener, tokenOfListener));
            return tokenOfListener;
        }
        throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$registerSimMobilityStatusListener$1(ISimMobilityStatusListener iSimMobilityStatusListener, ISimManager iSimManager) {
        this.mRegistrationManager.registerSimMobilityStatusListener(iSimMobilityStatusListener, iSimManager.getSimSlotIndex());
    }

    public boolean isSimMobilityActivated(int i) {
        return ImsUtil.isSimMobilityActivated(i);
    }

    public boolean isSimMobilityActivatedForRcs(int i) {
        return ImsUtil.isSimMobilityActivatedForRcs(i) || ImsUtil.isSimMobilityActivatedForAmRcs(this.mContext, i);
    }

    private boolean hasVoImsFeature(String str, int i, int i2) {
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i2);
        if (simManagerFromSimSlot == null) {
            IMSLog.d(LOG_TAG, i2, "hasVolteFeature - no simMgr");
            return true;
        }
        if ("mmtel".equalsIgnoreCase(str)) {
            z = true;
            z3 = false;
            z2 = false;
        } else if ("smsip".equalsIgnoreCase(str)) {
            z3 = true;
            z = false;
            z2 = false;
        } else if ("mmtel-video".equalsIgnoreCase(str)) {
            z2 = true;
            z = false;
            z3 = false;
        } else {
            IMSLog.d(LOG_TAG, i2, "no VoLTE feature, no need to check mnoInfo");
            return true;
        }
        if (i == 18) {
            z4 = true;
            z = false;
        } else {
            z4 = false;
        }
        ContentValues mnoInfo = simManagerFromSimSlot.getMnoInfo();
        if (mnoInfo.size() == 0) {
            IMSLog.d(LOG_TAG, i2, "hasVoImsFeature - mnoInfo's size is 0");
            return false;
        }
        int intValue = CollectionUtils.getIntValue(mnoInfo, ISimManager.KEY_IMSSWITCH_TYPE, -1);
        if (intValue == -1) {
            IMSLog.d(LOG_TAG, i2, "hasVoImsFeature - imsswitchType not exist");
            return false;
        } else if (intValue == 0 || intValue == 2) {
            IMSLog.d(LOG_TAG, i2, "hasVoImsFeature - No SIM or GCF or LABSIM or Softphone or Default ImsSwitch");
            return true;
        } else if (!CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, false)) {
            IMSLog.d(LOG_TAG, i2, "hasVoImsFeature - " + ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS + " false");
            return false;
        } else if (!z4 || CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI, false)) {
            if (z && !CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE, false)) {
                IMSLog.d(LOG_TAG, i2, "hasVoImsFeature - " + ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE + " false");
            } else if (z3 && !CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_SMS_IP, false)) {
                IMSLog.d(LOG_TAG, i2, "hasVoImsFeature - " + ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_SMS_IP + " false");
            } else if (!z2 || CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VIDEO_CALL, false)) {
                return true;
            } else {
                IMSLog.d(LOG_TAG, i2, "hasVoImsFeature - " + ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VIDEO_CALL + " false");
            }
            return false;
        } else {
            IMSLog.d(LOG_TAG, i2, "hasVoImsFeature - " + ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI + " false");
            return false;
        }
    }

    private void changeOpModeByRcsSwtich(boolean z, boolean z2, int i) {
        int i2;
        if (z != z2) {
            String readStringParamWithPath = RcsConfigurationHelper.readStringParamWithPath(this.mContext, ImsUtil.getPathWithPhoneId("info/tc_popup_user_accept", i));
            if (readStringParamWithPath != null) {
                try {
                    i2 = Integer.parseInt(readStringParamWithPath);
                } catch (NumberFormatException unused) {
                    IMSLog.e(LOG_TAG, i, "Error while parsing integer in getIntValue() - NumberFormatException");
                }
                this.mConfigModule.changeOpMode(z2, i, i2);
            }
            i2 = -1;
            this.mConfigModule.changeOpMode(z2, i, i2);
        }
    }

    private void enableRcsMainSwitchByPhoneId(boolean z, int i) {
        boolean z2 = true;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, i) != 1) {
            z2 = false;
        }
        String str = LOG_TAG;
        IMSLog.d(str, i, "enableRcsMainSwitchByPhoneId: oldValue: " + z2 + ", newValue: " + z);
        changeOpModeByRcsSwtich(z2, z, i);
        if (SimUtil.getSimMno(i) != Mno.SKT || z) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                DmConfigHelper.setImsSwitch(this.mContext, DeviceConfigManager.RCS, z, i);
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        } else {
            IMSLog.d(str, i, "enableRcs: Ignore RCS disable for SKT until server responds");
        }
    }

    private void dump(PrintWriter printWriter) {
        CriticalLogger.getInstance().flush();
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission("android.permission.DUMP", "Permission Denial: can't dump ims from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        IMSLog.prepareDump(this.mContext, printWriter);
        if (this.mDumpExecutor == null) {
            this.mDumpExecutor = Executors.newSingleThreadExecutor();
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        StringBuilder sb = new StringBuilder();
        sb.append("SimMobility Feature ");
        sb.append(SimUtil.isSimMobilityFeatureEnabled() ? "Enabled" : "Disabled");
        simpleEventLog.add(sb.toString());
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        Objects.requireNonNull(simpleEventLog2);
        ImsServiceStub$$ExternalSyntheticLambda11 imsServiceStub$$ExternalSyntheticLambda11 = new ImsServiceStub$$ExternalSyntheticLambda11(simpleEventLog2);
        ImsServiceStub$$ExternalSyntheticLambda18 imsServiceStub$$ExternalSyntheticLambda18 = new ImsServiceStub$$ExternalSyntheticLambda18();
        RegistrationManagerBase registrationManagerBase = this.mRegistrationManager;
        Objects.requireNonNull(registrationManagerBase);
        ImsServiceStub$$ExternalSyntheticLambda19 imsServiceStub$$ExternalSyntheticLambda19 = new ImsServiceStub$$ExternalSyntheticLambda19(registrationManagerBase);
        PdnController pdnController = this.mPdnController;
        Objects.requireNonNull(pdnController);
        ImsServiceStub$$ExternalSyntheticLambda20 imsServiceStub$$ExternalSyntheticLambda20 = new ImsServiceStub$$ExternalSyntheticLambda20(pdnController);
        ConfigModule configModule = this.mConfigModule;
        Objects.requireNonNull(configModule);
        ImsServiceStub$$ExternalSyntheticLambda21 imsServiceStub$$ExternalSyntheticLambda21 = new ImsServiceStub$$ExternalSyntheticLambda21(configModule);
        DmConfigModule dmConfigModule = this.mDmConfigModule;
        Objects.requireNonNull(dmConfigModule);
        ImsServiceStub$$ExternalSyntheticLambda22 imsServiceStub$$ExternalSyntheticLambda22 = new ImsServiceStub$$ExternalSyntheticLambda22(dmConfigModule);
        IAECModule iAECModule = this.mAECModule;
        Objects.requireNonNull(iAECModule);
        ImsServiceStub$$ExternalSyntheticLambda23 imsServiceStub$$ExternalSyntheticLambda23 = new ImsServiceStub$$ExternalSyntheticLambda23(iAECModule);
        CmcAccountManager cmcAccountManager = this.mCmcAccountManager;
        Objects.requireNonNull(cmcAccountManager);
        ImsServiceStub$$ExternalSyntheticLambda24 imsServiceStub$$ExternalSyntheticLambda24 = new ImsServiceStub$$ExternalSyntheticLambda24(cmcAccountManager);
        WfcEpdgManager wfcEpdgManager = this.mWfcEpdgManager;
        Objects.requireNonNull(wfcEpdgManager);
        ImsServiceStub$$ExternalSyntheticLambda25 imsServiceStub$$ExternalSyntheticLambda25 = new ImsServiceStub$$ExternalSyntheticLambda25(wfcEpdgManager);
        GeolocationController geolocationController = this.mGeolocationController;
        Objects.requireNonNull(geolocationController);
        ImsServiceStub$$ExternalSyntheticLambda26 imsServiceStub$$ExternalSyntheticLambda26 = new ImsServiceStub$$ExternalSyntheticLambda26(geolocationController);
        PreciseAlarmManager instance = PreciseAlarmManager.getInstance(this.mContext);
        Objects.requireNonNull(instance);
        ImsServiceStub$$ExternalSyntheticLambda12 imsServiceStub$$ExternalSyntheticLambda12 = new ImsServiceStub$$ExternalSyntheticLambda12(instance);
        ImsAutoUpdate instance2 = ImsAutoUpdate.getInstance(this.mContext, 0);
        Objects.requireNonNull(instance2);
        ImsServiceStub$$ExternalSyntheticLambda13 imsServiceStub$$ExternalSyntheticLambda13 = new ImsServiceStub$$ExternalSyntheticLambda13(instance2);
        ImsAutoUpdate instance3 = ImsAutoUpdate.getInstance(this.mContext, 1);
        Objects.requireNonNull(instance3);
        ImsServiceStub$$ExternalSyntheticLambda13 imsServiceStub$$ExternalSyntheticLambda132 = new ImsServiceStub$$ExternalSyntheticLambda13(instance3);
        ImsSimMobilityUpdate instance4 = ImsSimMobilityUpdate.getInstance(this.mContext);
        Objects.requireNonNull(instance4);
        Stream.of(new Runnable[]{imsServiceStub$$ExternalSyntheticLambda11, imsServiceStub$$ExternalSyntheticLambda18, imsServiceStub$$ExternalSyntheticLambda19, imsServiceStub$$ExternalSyntheticLambda20, imsServiceStub$$ExternalSyntheticLambda21, imsServiceStub$$ExternalSyntheticLambda22, imsServiceStub$$ExternalSyntheticLambda23, imsServiceStub$$ExternalSyntheticLambda24, imsServiceStub$$ExternalSyntheticLambda25, imsServiceStub$$ExternalSyntheticLambda26, imsServiceStub$$ExternalSyntheticLambda12, imsServiceStub$$ExternalSyntheticLambda13, imsServiceStub$$ExternalSyntheticLambda132, new ImsServiceStub$$ExternalSyntheticLambda14(instance4)}).forEach(new ImsServiceStub$$ExternalSyntheticLambda15(this));
        ServiceModuleManager serviceModuleManager = this.mServiceModuleManager;
        Objects.requireNonNull(serviceModuleManager);
        dump(new ImsServiceStub$$ExternalSyntheticLambda16(serviceModuleManager), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
        dump(new ImsServiceStub$$ExternalSyntheticLambda17(this), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
        IMSLog.postDump(printWriter);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$dump$2(Runnable runnable) {
        dump(runnable, 1000);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$dump$3() {
        this.mContext.getContentResolver().call(Uri.parse(ImsConstants.Uris.CONFIG_URI), "dump", (String) null, (Bundle) null);
    }

    private void dump(Runnable runnable, long j) {
        Future<?> future = null;
        try {
            future = this.mDumpExecutor.submit(runnable);
            future.get(j, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | RuntimeException | ExecutionException | TimeoutException e) {
            Optional.ofNullable(future).ifPresent(new ImsServiceStub$$ExternalSyntheticLambda28());
            String str = "dump: Exception occurs! " + e;
            String str2 = LOG_TAG;
            IMSLog.e(str2, str);
            IMSLog.dump(str2, str);
            e.printStackTrace();
        }
    }

    private boolean isPermissionGranted() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE") == 0 || this.mContext.checkCallingOrSelfPermission(PERMISSION) == 0;
    }

    /* access modifiers changed from: private */
    public void factoryReset(int i) {
        if (i < 0 || i >= getPhoneCount()) {
            IMSLog.e(LOG_TAG, i, "factoryReset : invalid phoneId");
            return;
        }
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot == null || simManagerFromSimSlot.hasNoSim()) {
            IMSLog.e(LOG_TAG, i, "factoryReset : skip reset due to no SIM");
            return;
        }
        String str = LOG_TAG;
        IMSLog.i(str, i, "factoryReset");
        boolean z = getBoolean(i, GlobalSettingsConstants.Registration.VOLTE_DOMESTIC_DEFAULT_ENABLED, true);
        boolean z2 = getBoolean(i, GlobalSettingsConstants.Registration.VIDEO_DEFAULT_ENABLED, true);
        IMSLog.d(str, i, "reset to default] Volte : " + z + ", Video : " + z2);
        ImsConstants.SystemSettings.setVoiceCallType(this.mContext, z ^ true ? 1 : 0, i);
        ImsConstants.SystemSettings.setVideoCallType(this.mContext, z2 ^ true ? 1 : 0, i);
    }

    public int getPhoneCount() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return SimUtil.getPhoneCount();
    }

    public void setIsimLoaded() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mSimManager.setIsimLoaded();
    }

    public void setSimRefreshed() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mSimManager.setSimRefreshed();
    }

    public int setActiveImpu(int i, String str, String str2) throws RemoteException {
        Context context = this.mContext;
        String str3 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str3);
        IMSLog.d(str3, i, "setActiveImpu: impu " + str + " service " + str2 + " to phoneId" + i);
        this.mServiceModuleManager.getVolteServiceModule().setActiveImpu(i, str);
        return 0;
    }

    public int setActiveMsisdn(int i, String str, String str2) throws RemoteException {
        Context context = this.mContext;
        String str3 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str3);
        IMSLog.d(str3, i, "setActiveMsisdn: msisdn " + IMSLog.checker(str) + " service " + str2);
        if (TextUtils.isEmpty(str2)) {
            return -1;
        }
        if (TextUtils.isEmpty(str)) {
            IMSLog.d(str3, i, "setActiveMsisdn: unset activeMsisdn.");
            return setActiveImpu(i, (String) null, str2);
        }
        ImsUri normalizedUri = this.mServiceModuleManager.getVolteServiceModule().getNormalizedUri(i, str);
        if (normalizedUri != null) {
            return setActiveImpu(i, normalizedUri.toString(), str2);
        }
        IMSLog.e(str3, i, "setActiveMsisdn: not found!");
        return -2;
    }

    public void sendVerificationCode(String str, int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mConfigModule.sendVerificationCode(str, i);
    }

    public void sendMsisdnNumber(String str, int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mConfigModule.sendMsisdnNumber(str, i);
    }

    public void sendIidToken(String str, int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mConfigModule.sendIidToken(str, i);
    }

    public int getNetworkType(int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        int currentNetwork = this.mRegistrationManager.getCurrentNetwork(i);
        if (currentNetwork < 1 || currentNetwork > 17) {
            return currentNetwork == 18 ? 2 : 0;
        }
        return 1;
    }

    public String getAvailableNetworkType(String str) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mRegistrationManager.getAvailableNetworkType(str);
    }

    public String registerImSessionListener(IImSessionListener iImSessionListener) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, "registerImSessionListener:");
        IImModule imModule = this.mServiceModuleManager.getImModule();
        if (imModule != null) {
            imModule.registerImSessionListener(iImSessionListener);
        }
        String tokenOfListener = getTokenOfListener(iImSessionListener);
        this.mListenerTokenMap.put(tokenOfListener, new CallBack(iImSessionListener, tokenOfListener));
        return tokenOfListener;
    }

    public String registerImSessionListenerByPhoneId(IImSessionListener iImSessionListener, int i) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, i, "registerImSessionListenerByPhoneId");
        IImModule imModule = this.mServiceModuleManager.getImModule();
        if (imModule != null) {
            imModule.registerImSessionListenerByPhoneId(iImSessionListener, i);
        }
        String tokenOfListener = getTokenOfListener(iImSessionListener);
        this.mListenerTokenMap.put(tokenOfListener, new CallBack(iImSessionListener, tokenOfListener));
        return tokenOfListener;
    }

    public void unregisterImSessionListener(String str) {
        IImModule imModule;
        Context context = this.mContext;
        String str2 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str2);
        if (!TextUtils.isEmpty(str)) {
            IMSLog.d(str2, "unregisterImSessionListener:");
            IImSessionListener removeCallback = removeCallback(str);
            if (removeCallback != null && (imModule = this.mServiceModuleManager.getImModule()) != null) {
                imModule.unregisterImSessionListener(removeCallback);
            }
        }
    }

    public void unregisterImSessionListenerByPhoneId(String str, int i) {
        IImModule imModule;
        Context context = this.mContext;
        String str2 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str2);
        if (!TextUtils.isEmpty(str)) {
            IMSLog.d(str2, i, "unregisterImSessionListenerByPhoneId");
            IImSessionListener removeCallback = removeCallback(str);
            if (removeCallback != null && (imModule = this.mServiceModuleManager.getImModule()) != null) {
                imModule.unregisterImSessionListenerByPhoneId(removeCallback, i);
            }
        }
    }

    public String registerImsOngoingFtListener(IImsOngoingFtEventListener iImsOngoingFtEventListener) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, "registerImsOngoingFtListener");
        String tokenOfListener = getTokenOfListener(iImsOngoingFtEventListener);
        this.mListenerTokenMap.put(tokenOfListener, new CallBack(iImsOngoingFtEventListener, tokenOfListener));
        IImModule imModule = this.mServiceModuleManager.getImModule();
        if (imModule != null) {
            imModule.registerImsOngoingFtListener(iImsOngoingFtEventListener);
        }
        return tokenOfListener;
    }

    public String registerImsOngoingFtListenerByPhoneId(IImsOngoingFtEventListener iImsOngoingFtEventListener, int i) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, i, "registerImsOngoingFtListenerByPhoneId");
        IImModule imModule = this.mServiceModuleManager.getImModule();
        String tokenOfListener = getTokenOfListener(iImsOngoingFtEventListener);
        this.mListenerTokenMap.put(tokenOfListener, new CallBack(iImsOngoingFtEventListener, tokenOfListener));
        if (imModule != null) {
            imModule.registerImsOngoingFtListenerByPhoneId(iImsOngoingFtEventListener, i);
        }
        return tokenOfListener;
    }

    public void unregisterImsOngoingFtListener(String str) {
        IImModule imModule;
        Context context = this.mContext;
        String str2 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str2);
        if (!TextUtils.isEmpty(str)) {
            IMSLog.d(str2, "unregisterImsOngoingFtListener");
            IImsOngoingFtEventListener removeCallback = removeCallback(str);
            if (removeCallback != null && (imModule = this.mServiceModuleManager.getImModule()) != null) {
                imModule.unregisterImsOngoingListener(removeCallback);
            }
        }
    }

    public void unregisterImsOngoingFtListenerByPhoneId(String str, int i) {
        Context context = this.mContext;
        String str2 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str2);
        if (!TextUtils.isEmpty(str)) {
            IMSLog.d(str2, i, "unregisterImsOngoingFtListenerByPhoneId");
            IImModule imModule = this.mServiceModuleManager.getImModule();
            IImsOngoingFtEventListener removeCallback = removeCallback(str);
            if (removeCallback != null && imModule != null) {
                imModule.unregisterImsOngoingListenerByPhoneId(removeCallback, i);
            }
        }
    }

    public String registerAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener, int i) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, i, "registerAutoConfigurationListener");
        this.mConfigModule.registerAutoConfigurationListener(iAutoConfigurationListener, i);
        String tokenOfListener = getTokenOfListener(iAutoConfigurationListener);
        this.mListenerTokenMap.put(tokenOfListener, new CallBack(iAutoConfigurationListener, tokenOfListener));
        return tokenOfListener;
    }

    public void unregisterAutoConfigurationListener(String str, int i) {
        if (!TextUtils.isEmpty(str)) {
            Context context = this.mContext;
            String str2 = LOG_TAG;
            context.enforceCallingOrSelfPermission(PERMISSION, str2);
            IMSLog.d(str2, i, "unregisterAutoConfigurationListener");
            IAutoConfigurationListener removeCallback = removeCallback(str);
            if (removeCallback != null) {
                this.mConfigModule.unregisterAutoConfigurationListener(removeCallback, i);
            }
        }
    }

    public String registerSimMobilityStatusListenerByPhoneId(ISimMobilityStatusListener iSimMobilityStatusListener, int i) {
        if (isPermissionGranted()) {
            IMSLog.d(LOG_TAG, i, "registerSimMobilityStatusListenerByPhoneId");
            return registerSimMobilityStatusListener(iSimMobilityStatusListener, true, i);
        }
        throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
    }

    public void unregisterSimMobilityStatusListenerByPhoneId(String str, int i) {
        if (!isPermissionGranted()) {
            throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
        } else if (!TextUtils.isEmpty(str)) {
            IMSLog.d(LOG_TAG, i, "unregisterSimMobilityStatusListenerByPhoneId");
            ISimMobilityStatusListener removeCallback = removeCallback(str);
            if (removeCallback == null) {
                return;
            }
            if (i == -1) {
                this.mSimManagers.forEach(new ImsServiceStub$$ExternalSyntheticLambda8(this, removeCallback));
            } else {
                this.mRegistrationManager.unregisterSimMobilityStatusListener(removeCallback, i);
            }
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$unregisterSimMobilityStatusListenerByPhoneId$5(ISimMobilityStatusListener iSimMobilityStatusListener, ISimManager iSimManager) {
        this.mRegistrationManager.unregisterSimMobilityStatusListener(iSimMobilityStatusListener, iSimManager.getSimSlotIndex());
    }

    public boolean isRegistered() throws RemoteException {
        if (isPermissionGranted()) {
            RegistrationManagerBase registrationManagerBase = this.mRegistrationManager;
            return (registrationManagerBase == null || registrationManagerBase.getRegistrationInfo() == null || this.mRegistrationManager.getRegistrationInfo().length <= 0) ? false : true;
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public ImsRegistration[] getRegistrationInfo() throws RemoteException {
        if (isPermissionGranted()) {
            return this.mRegistrationManager.getRegistrationInfo();
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public ImsRegistration[] getRegistrationInfoByPhoneId(int i) {
        if (isPermissionGranted()) {
            return this.mRegistrationManager.getRegistrationInfoByPhoneId(i);
        }
        throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
    }

    public ImsRegistration getRegistrationInfoByServiceType(String str, int i) {
        if (isPermissionGranted()) {
            return this.mRegistrationManager.getRegistrationInfoByServiceType(str, i);
        }
        throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
    }

    public ImsProfile[] getCurrentProfile() {
        return getCurrentProfileForSlot(SimUtil.getActiveDataPhoneId());
    }

    public ImsProfile[] getCurrentProfileForSlot(int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mRegistrationManager.getProfileList(i);
    }

    public String getRcsProfileType(int i) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        String str2 = (String) Arrays.stream(this.mRegistrationManager.getProfileList(i)).filter(new GeolocationController$$ExternalSyntheticLambda0()).map(new ImsServiceStub$$ExternalSyntheticLambda9(this, i)).filter(new ImsServiceStub$$ExternalSyntheticLambda10()).findFirst().orElse("");
        IMSLog.d(str, i, "getRcsProfileType: rcsProfile = " + str2);
        return str2;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ String lambda$getRcsProfileType$6(int i, ImsProfile imsProfile) {
        return ConfigUtil.getRcsProfileWithFeature(this.mContext, i, imsProfile);
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$getRcsProfileType$7(String str) {
        return !TextUtils.isEmpty(str);
    }

    public int registerAdhocProfile(ImsProfile imsProfile) {
        return registerAdhocProfileByPhoneId(imsProfile, SimUtil.getActiveDataPhoneId());
    }

    public int registerAdhocProfileByPhoneId(ImsProfile imsProfile, int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mRegistrationManager.registerProfile(imsProfile, i);
    }

    public void deregisterAdhocProfile(int i) throws RemoteException {
        deregisterAdhocProfileByPhoneId(i, SimUtil.getActiveDataPhoneId());
    }

    public void deregisterAdhocProfileByPhoneId(int i, int i2) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mRegistrationManager.deregisterProfile(i, i2);
    }

    public void registerProfile(List list) {
        registerProfileByPhoneId(list, SimUtil.getActiveDataPhoneId());
    }

    public void registerProfileByPhoneId(List list, int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mRegistrationManager.registerProfile((List<Integer>) list, i);
    }

    public void deregisterProfile(List list, boolean z) {
        deregisterProfileByPhoneId(list, z, SimUtil.getActiveDataPhoneId());
    }

    public void deregisterProfileByPhoneId(List list, boolean z, int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mRegistrationManager.deregisterProfile((List<Integer>) list, z, i);
    }

    public void sendTryRegister() {
        sendTryRegisterByPhoneId(SimUtil.getActiveDataPhoneId());
    }

    public void sendTryRegisterByPhoneId(int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mConfigModule.setRegisterFromApp(true, i);
    }

    public void sendTryRegisterCms(int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mConfigModule.setRegisterFromApp(true, i);
    }

    public void forcedUpdateRegistration(ImsProfile imsProfile) {
        forcedUpdateRegistrationByPhoneId(imsProfile, SimUtil.getActiveDataPhoneId());
    }

    public void forcedUpdateRegistrationByPhoneId(ImsProfile imsProfile, int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mRegistrationManager.forcedUpdateRegistration(imsProfile, i);
    }

    public void sendDeregister(int i, int i2) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mRegistrationManager.sendDeregister(i, i2);
    }

    public void suspendRegister(boolean z, int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mRegistrationManager.suspendRegister(z, i);
    }

    public int updateRegistration(ImsProfile imsProfile, int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mRegistrationManager.updateRegistration(imsProfile, i);
    }

    public boolean isQSSSuccessAuthAndLogin(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModuleManager.getVolteServiceModule().isQSSSuccessAuthAndLogin(i);
    }

    public void setEmergencyPdnInfo(String str, String[] strArr, String str2, int i) {
        Context context = this.mContext;
        String str3 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str3);
        IMSLog.d(str3, i, "ePDN setup failure was changed to onPreciseDataConnectionStateChanged");
    }

    public String registerEpdgListener(IEpdgListener iEpdgListener) {
        String str = LOG_TAG;
        IMSLog.d(str, "registerEpdgListener");
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, str);
        WfcEpdgManager wfcEpdgManager = this.mWfcEpdgManager;
        if (wfcEpdgManager != null) {
            wfcEpdgManager.registerEpdgHandoverListener(iEpdgListener);
        }
        String tokenOfListener = getTokenOfListener(iEpdgListener);
        this.mListenerTokenMap.put(tokenOfListener, new CallBack(iEpdgListener, tokenOfListener));
        return tokenOfListener;
    }

    public void unRegisterEpdgListener(String str) {
        IEpdgListener removeCallback;
        WfcEpdgManager wfcEpdgManager;
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        if (!TextUtils.isEmpty(str) && (removeCallback = removeCallback(str)) != null && (wfcEpdgManager = this.mWfcEpdgManager) != null) {
            wfcEpdgManager.unRegisterEpdgHandoverListener(removeCallback);
        }
    }

    public void registerImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) {
        if (isPermissionGranted()) {
            IMSLog.d(LOG_TAG, "Requested registerListener without phoneId. register it by all phoneId.");
            this.mSimManagers.forEach(new ImsServiceStub$$ExternalSyntheticLambda4(this, iImsRegistrationListener));
            return;
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$registerImsRegistrationListener$8(IImsRegistrationListener iImsRegistrationListener, ISimManager iSimManager) {
        this.mRegistrationManager.registerListener(iImsRegistrationListener, iSimManager.getSimSlotIndex());
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException {
        if (isPermissionGranted()) {
            IMSLog.d(LOG_TAG, "Requested unregisterListener without phoneId. unregister it by all phoneId.");
            this.mSimManagers.forEach(new ImsServiceStub$$ExternalSyntheticLambda3(this, iImsRegistrationListener));
            return;
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$unregisterImsRegistrationListener$9(IImsRegistrationListener iImsRegistrationListener, ISimManager iSimManager) {
        this.mRegistrationManager.unregisterListener(iImsRegistrationListener, iSimManager.getSimSlotIndex());
    }

    public String registerImsRegistrationListenerForSlot(IImsRegistrationListener iImsRegistrationListener, int i) {
        if (isPermissionGranted()) {
            IMSLog.d(LOG_TAG, i, "registerImsRegistrationListenerForSlot");
            return registerImsRegistrationListener(iImsRegistrationListener, true, i);
        }
        throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
    }

    public void unregisterImsRegistrationListenerForSlot(String str, int i) {
        if (!isPermissionGranted()) {
            throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
        } else if (!TextUtils.isEmpty(str)) {
            String str2 = LOG_TAG;
            IMSLog.d(str2, i, "unregisterImsRegistrationListenerForSlot");
            IImsRegistrationListener removeCallback = removeCallback(str);
            if (removeCallback == null) {
                return;
            }
            if (i == -1) {
                IMSLog.d(str2, "Requested unRegisterListener without phoneId. register it by all phoneId.");
                this.mSimManagers.forEach(new ImsServiceStub$$ExternalSyntheticLambda27(this, removeCallback));
                return;
            }
            this.mRegistrationManager.unregisterListener(removeCallback, i);
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$unregisterImsRegistrationListenerForSlot$10(IImsRegistrationListener iImsRegistrationListener, ISimManager iSimManager) {
        this.mRegistrationManager.unregisterListener(iImsRegistrationListener, iSimManager.getSimSlotIndex());
    }

    public String registerCmcRegistrationListenerForSlot(IImsRegistrationListener iImsRegistrationListener, int i) {
        if (isPermissionGranted()) {
            IMSLog.d(LOG_TAG, i, "registerCmcRegistrationListenerForSlot");
            this.mRegistrationManager.registerCmcRegiListener(iImsRegistrationListener, i);
            String tokenOfListener = getTokenOfListener(iImsRegistrationListener);
            this.mListenerTokenMap.put(tokenOfListener, new CallBack(iImsRegistrationListener, tokenOfListener));
            return tokenOfListener;
        }
        throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
    }

    public void unregisterCmcRegistrationListenerForSlot(String str, int i) {
        if (!isPermissionGranted()) {
            throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
        } else if (!TextUtils.isEmpty(str)) {
            IMSLog.d(LOG_TAG, i, "unregisterCmcRegistrationListenerForSlot");
            IImsRegistrationListener removeCallback = removeCallback(str);
            if (removeCallback != null) {
                this.mRegistrationManager.unregisterCmcRegiListener(removeCallback, i);
            }
        }
    }

    public void registerDialogEventListener(int i, IDialogEventListener iDialogEventListener) throws RemoteException {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, i, "registerDialogEventListener");
        this.mServiceModuleManager.getVolteServiceModule().registerDialogEventListener(i, iDialogEventListener);
    }

    public void unregisterDialogEventListener(int i, IDialogEventListener iDialogEventListener) throws RemoteException {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, i, "unregisterDialogEventListener");
        this.mServiceModuleManager.getVolteServiceModule().unregisterDialogEventListener(i, iDialogEventListener);
    }

    public String registerDialogEventListenerByToken(int i, IDialogEventListener iDialogEventListener) throws RemoteException {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, i, "registerDialogEventListener");
        this.mServiceModuleManager.getVolteServiceModule().registerDialogEventListener(i, iDialogEventListener);
        String tokenOfListener = getTokenOfListener(iDialogEventListener);
        this.mListenerTokenMap.put(tokenOfListener, new CallBack(iDialogEventListener, tokenOfListener));
        return tokenOfListener;
    }

    public void unregisterDialogEventListenerByToken(int i, String str) throws RemoteException {
        Context context = this.mContext;
        String str2 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str2);
        if (!TextUtils.isEmpty(str)) {
            IMSLog.d(str2, i, "unregisterDialogEventListener");
            IDialogEventListener removeCallback = removeCallback(str);
            if (removeCallback != null) {
                this.mServiceModuleManager.getVolteServiceModule().unregisterDialogEventListener(i, removeCallback);
            }
        }
    }

    public DialogEvent getLastDialogEvent(int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModuleManager.getVolteServiceModule().getLastDialogEvent(i);
    }

    public int getMasterValue(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return 0;
    }

    public String getMasterStringValue(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return "";
    }

    public void setProvisionedValue(int i, int i2) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
    }

    public void setProvisionedStringValue(int i, String str) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
    }

    public boolean isImsEnabled() {
        return isImsEnabledByPhoneId(0);
    }

    public boolean isImsEnabledByPhoneId(int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, i) == 1;
    }

    public boolean isVoLteEnabled() {
        return isVoLteEnabledByPhoneId(0);
    }

    public boolean isVoLteEnabledByPhoneId(int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return DmConfigHelper.getImsSwitchValue(this.mContext, "volte", i) == 1;
    }

    public boolean isVolteEnabledFromNetwork(int i) {
        if (isPermissionGranted()) {
            return this.mServiceModuleManager.getVolteServiceModule().isVolteServiceStatus(i);
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public boolean isVolteSupportECT() {
        if (isPermissionGranted()) {
            return this.mServiceModuleManager.getVolteServiceModule().isVolteSupportECT();
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public boolean isVolteSupportEctByPhoneId(int i) {
        if (isPermissionGranted()) {
            return this.mServiceModuleManager.getVolteServiceModule().isVolteSupportECT(i);
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public boolean isRcsEnabled() {
        return isRcsEnabledByPhoneId(SimUtil.getActiveDataPhoneId());
    }

    public boolean isServiceEnabled(String str) {
        return isServiceEnabledByPhoneId(str, 0);
    }

    public boolean hasCrossSimImsService(int i) {
        Set hashSet = new HashSet();
        ImsProfile[] currentProfileForSlot = getCurrentProfileForSlot(i);
        int length = currentProfileForSlot.length;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            }
            ImsProfile imsProfile = currentProfileForSlot[i2];
            if (imsProfile == null || imsProfile.getPdnType() != 11) {
                i2++;
            } else {
                hashSet = imsProfile.getServiceSet(18);
                IRegistrationGovernor registrationGovernorByProfileId = this.mRegistrationManager.getRegistrationGovernorByProfileId(imsProfile.getId(), i);
                if (registrationGovernorByProfileId != null) {
                    hashSet = registrationGovernorByProfileId.applyDataSimPolicyForCrossSim(hashSet, i);
                }
            }
        }
        hashSet.removeIf(new ImsServiceStub$$ExternalSyntheticLambda5(this, i));
        return !hashSet.isEmpty();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ boolean lambda$hasCrossSimImsService$11(int i, String str) {
        return !isServiceAvailable(str, 18, i);
    }

    public boolean isCrossSimCallingSupportedByPhoneId(int i) {
        if (!isPermissionGranted()) {
            throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
        } else if (!isCrossSimCallingSupported()) {
            return false;
        } else {
            int i2 = i + 1;
            return (Settings.System.getInt(this.mContext.getContentResolver(), "cross_sim_menu_enable", 0) & i2) == i2;
        }
    }

    public boolean isCrossSimCallingSupported() {
        if (!isPermissionGranted()) {
            throw new SecurityException(LOG_TAG + " Permission denied");
        } else if (TelephonyManagerWrapper.getInstance(this.mContext).getPhoneCount() <= 1 || Settings.System.getInt(this.mContext.getContentResolver(), "cross_sim_menu_enable", -1) <= 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isCrossSimCallingRegistered(int i) {
        ImsRegistration[] registrationInfoByPhoneId = getRegistrationInfoByPhoneId(i);
        if (registrationInfoByPhoneId == null) {
            return false;
        }
        int length = registrationInfoByPhoneId.length;
        int i2 = 0;
        while (i2 < length) {
            ImsRegistration imsRegistration = registrationInfoByPhoneId[i2];
            if (imsRegistration == null || imsRegistration.getImsProfile().getPdnType() != 11 || !imsRegistration.getEpdgStatus() || !imsRegistration.isEpdgOverCellularData() || imsRegistration.getRegiRat() != 18) {
                i2++;
            } else {
                IMSLog.i(LOG_TAG, i, "isCrossSimCallingRegistered true");
                return true;
            }
        }
        IMSLog.i(LOG_TAG, i, "isCrossSimCallingRegistered false");
        return false;
    }

    public boolean isServiceAvailable(String str, int i, int i2) {
        Context context = this.mContext;
        String str2 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str2);
        if (!hasVoImsFeature(str, i, i2)) {
            IMSLog.i(str2, i2, "isServiceAvailable: VoImsFeature : (" + str + ") is not supported");
            return false;
        }
        if (ImsProfile.isRcsService(str) && (OmcCode.isSKTOmcCode() || OmcCode.isKTTOmcCode() || OmcCode.isLGTOmcCode())) {
            boolean z = false;
            for (ImsProfile imsProfile : getCurrentProfileForSlot(i2)) {
                if (imsProfile != null && ImsProfile.hasRcsService(imsProfile)) {
                    z = imsProfile.getSupportRcsAcrossSalesCode();
                }
            }
            String nWCode = OmcCode.getNWCode(i2);
            Mno simMno = SimUtil.getSimMno(i2);
            if (!nWCode.isEmpty() && !simMno.equalsWithSalesCode(simMno, nWCode) && !z) {
                IMSLog.i(LOG_TAG, i2, "isServiceAvailable: not matched with SIM :" + str);
                return false;
            }
        }
        boolean anyMatch = Arrays.stream(getCurrentProfileForSlot(i2)).anyMatch(new ImsServiceStub$$ExternalSyntheticLambda7(str, i));
        boolean isServiceEnabledByPhoneId = isServiceEnabledByPhoneId(str, i2);
        IMSLog.i(LOG_TAG, i2, "isServiceAvailable: " + str + ", rat: " + i + ", profileFind:" + anyMatch + ", Enabled:" + isServiceEnabledByPhoneId);
        if (!anyMatch || !isServiceEnabledByPhoneId) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$isServiceAvailable$12(String str, int i, ImsProfile imsProfile) {
        return imsProfile != null && !imsProfile.hasEmergencySupport() && imsProfile.hasService(str, i);
    }

    public boolean isServiceEnabledByPhoneId(String str, int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            int imsSwitchValue = DmConfigHelper.getImsSwitchValue(this.mContext, str, i);
            boolean z = true;
            if (imsSwitchValue != 1) {
                z = false;
            }
            return z;
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    public boolean hasVoLteSim() {
        return hasVoLteSimByPhoneId(SimUtil.getActiveDataPhoneId());
    }

    public boolean hasVoLteSimByPhoneId(int i) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        RegistrationManagerBase registrationManagerBase = this.mRegistrationManager;
        if (registrationManagerBase != null) {
            return registrationManagerBase.hasVoLteSim(i);
        }
        IMSLog.d(str, i, "hasVoLteSimByPhoneId - no mRegistrationManager");
        return true;
    }

    public void enableService(String str, boolean z) {
        enableServiceByPhoneId(str, z, 0);
    }

    public void enableServiceByPhoneId(String str, boolean z, int i) {
        Context context = this.mContext;
        String str2 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str2);
        String processNameById = PackageUtils.getProcessNameById(getContext(), Binder.getCallingPid());
        if ("com.samsung.advp.imssettings".equalsIgnoreCase(processNameById) || "com.android.phone".equals(processNameById)) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                int i2 = 0;
                if (!TextUtils.equals(str, ImsConstants.SystemSettings.VOLTE_SLOT1.getName())) {
                    if (!TextUtils.equals(str, ImsConstants.SystemSettings.VILTE_SLOT1.getName())) {
                        if (TextUtils.equals(str, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName())) {
                            Context context2 = this.mContext;
                            if (z) {
                                i2 = 1;
                            }
                            DmConfigHelper.setImsUserSetting(context2, str, i2, i);
                        } else {
                            DmConfigHelper.setImsSwitch(this.mContext, str, z, i);
                        }
                    }
                }
                Context context3 = this.mContext;
                if (!z) {
                    i2 = 1;
                }
                DmConfigHelper.setImsUserSetting(context3, str, i2, i);
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        } else {
            IMSLog.d(str2, i, "deprecated] enableService is called by " + processNameById);
        }
    }

    public void enableVoLte(boolean z) {
        enableVoLteByPhoneId(z, 0);
    }

    public void enableVoLteByPhoneId(boolean z, int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        DmConfigHelper.setImsSwitch(this.mContext, "volte", z, i);
    }

    public void enableRcs(boolean z) {
        enableRcsByPhoneId(z, 0);
    }

    public void enableRcsByPhoneId(boolean z, int i) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        if (z && !ConfigUtil.checkMdmRcsStatus(this.mContext, i)) {
            IMSLog.e(str, i, "RCS isn't allowed by MDM. Don't enable RCS");
        } else if ("com.samsung.advp.imssettings".equalsIgnoreCase(PackageUtils.getProcessNameById(getContext(), Binder.getCallingPid()))) {
            IMSLog.d(str, i, "Called by ImsSettings app. Change main switch value.");
            enableRcsMainSwitchByPhoneId(z, i);
        } else {
            boolean z2 = true;
            if (ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, 1, i) != 1) {
                z2 = false;
            }
            IMSLog.i(str, i, "enableRcs: oldValue: " + z2 + ", newValue: " + z);
            changeOpModeByRcsSwtich(z2, z, i);
            if (SimUtil.getSimMno(i) != Mno.SKT || z) {
                ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, z ? 1 : 0, i);
            } else {
                IMSLog.d(str, i, "enableRcs: Ignore RCS disable for SKT until server responds");
            }
        }
    }

    public int[] getCallCount(int i) {
        if (isPermissionGranted()) {
            return new int[]{this.mServiceModuleManager.getVolteServiceModule().getTotalCallCount(i), this.mServiceModuleManager.getVolteServiceModule().getVideoCallCount(i), this.mServiceModuleManager.getVolteServiceModule().getDowngradedCallCount(i), this.mServiceModuleManager.getVolteServiceModule().getE911CallCount(i)};
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public int getEpsFbCallCount(int i) {
        if (isPermissionGranted()) {
            return this.mServiceModuleManager.getVolteServiceModule().getEpsFbCallCount(i);
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public int getNrSaCallCount(int i) {
        if (isPermissionGranted()) {
            return this.mServiceModuleManager.getVolteServiceModule().getNrSaCallCount(i);
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public boolean isForbidden() {
        if (isPermissionGranted()) {
            return this.mRegistrationManager.isInvite403DisabledService(SimUtil.getActiveDataPhoneId());
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public boolean isForbiddenByPhoneId(int i) {
        if (isPermissionGranted()) {
            return this.mRegistrationManager.isInvite403DisabledService(i);
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public void transferCall(String str, String str2) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModuleManager.getVolteServiceModule().transferCall(str, str2);
    }

    public int startLocalRingBackTone(int i, int i2, int i3) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IVolteServiceModule volteServiceModule = this.mServiceModuleManager.getVolteServiceModule();
        if (volteServiceModule != null) {
            return volteServiceModule.startLocalRingBackTone(i, i2, i3);
        }
        IMSLog.e(str, "VolteServiceModule is not ready");
        return -1;
    }

    public int stopLocalRingBackTone() {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IVolteServiceModule volteServiceModule = this.mServiceModuleManager.getVolteServiceModule();
        if (volteServiceModule != null) {
            return volteServiceModule.stopLocalRingBackTone();
        }
        IMSLog.e(str, "VolteServiceModule is not ready");
        return -1;
    }

    public void changeAudioPath(int i) {
        changeAudioPathForSlot(0, i);
    }

    public void changeAudioPathForSlot(int i, int i2) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IVolteServiceModule volteServiceModule = this.mServiceModuleManager.getVolteServiceModule();
        if (volteServiceModule == null) {
            IMSLog.e(str, i, "VolteServiceModule is not ready");
        } else {
            volteServiceModule.updateAudioInterface(i, i2);
        }
    }

    public boolean setVideocallType(int i) {
        if (isPermissionGranted()) {
            ImsConstants.SystemSettings.VILTE_SLOT1.set(this.mContext, i);
            return true;
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public int getVideocallType() {
        if (isPermissionGranted()) {
            return ImsConstants.SystemSettings.VILTE_SLOT1.get(this.mContext, -1);
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public void registerDmValueListener(IImsDmConfigListener iImsDmConfigListener) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, "registerDmValueListener:");
        this.mRegistrationManager.registerDmListener(iImsDmConfigListener);
    }

    public void unregisterDmValueListener(IImsDmConfigListener iImsDmConfigListener) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, "unregisterDmValueListener:");
        this.mRegistrationManager.unregisterDmListener(iImsDmConfigListener);
    }

    public ContentValues getConfigValues(String[] strArr, int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            return this.mDmConfigModule.getConfigValues(strArr, i);
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    public boolean updateConfigValues(ContentValues contentValues, int i, int i2) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mDmConfigModule.updateConfigValues(contentValues, i, i2);
    }

    public int startDmConfig(int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mRegistrationManager.sendDmState(i, true);
        return this.mDmConfigModule.startDmConfig(i);
    }

    public void finishDmConfig(int i, int i2) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mRegistrationManager.sendDmState(i2, false);
        this.mDmConfigModule.finishDmConfig(i, i2);
    }

    public boolean isRttCall(int i) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, "isRttCall");
        return this.mServiceModuleManager.getVolteServiceModule().isRttCall(i);
    }

    public void setAutomaticMode(int i, boolean z) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, i, "setAutomaticMode, mode=" + z);
        this.mServiceModuleManager.getVolteServiceModule().setAutomaticMode(i, z);
    }

    public void setRttMode(int i, int i2) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, i, "setRttMode, mode=" + i2);
        this.mServiceModuleManager.getVolteServiceModule().setRttMode(i, i2);
    }

    public int getRttMode(int i) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, i, "getRttMode");
        return this.mServiceModuleManager.getVolteServiceModule().getRttMode();
    }

    public void sendRttMessage(String str) {
        Context context = this.mContext;
        String str2 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str2);
        IMSLog.d(str2, "sendRttMessage, mode=" + str);
        this.mServiceModuleManager.getVolteServiceModule().sendRttMessage(str);
    }

    public void sendRttSessionModifyResponse(int i, boolean z) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, "sendRttSessionModifyResponse, accept=" + z);
        this.mServiceModuleManager.getVolteServiceModule().sendRttSessionModifyResponse(i, z);
    }

    public void sendRttSessionModifyRequest(int i, boolean z) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, "sendRttSessionModifyRequest");
        this.mServiceModuleManager.getVolteServiceModule().sendRttSessionModifyRequest(i, z);
    }

    public String registerRttEventListener(int i, IRttEventListener iRttEventListener) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, i, "registerRttEventListener");
        this.mServiceModuleManager.getVolteServiceModule().registerRttEventListener(i, iRttEventListener);
        String tokenOfListener = getTokenOfListener(iRttEventListener);
        this.mListenerTokenMap.put(tokenOfListener, new CallBack(iRttEventListener, tokenOfListener));
        return tokenOfListener;
    }

    public void unregisterRttEventListener(int i, String str) {
        Context context = this.mContext;
        String str2 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str2);
        if (!TextUtils.isEmpty(str)) {
            IMSLog.d(str2, i, "unregisterRttEventListener");
            IRttEventListener removeCallback = removeCallback(str);
            if (removeCallback != null) {
                this.mServiceModuleManager.getVolteServiceModule().unregisterRttEventListener(i, removeCallback);
            }
        }
    }

    public void triggerAutoConfigurationForApp(int i) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        IAECModule iAECModule = this.mAECModule;
        if (iAECModule != null) {
            iAECModule.triggerAutoConfigForApp(i);
        }
    }

    public String getGlobalSettingsValueToString(String str, int i, String str2) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return getString(i, str, str2);
    }

    public int getGlobalSettingsValueToInteger(String str, int i, int i2) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return getInt(i, str, i2);
    }

    public boolean getGlobalSettingsValueToBoolean(String str, int i, boolean z) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return getBoolean(i, str, z);
    }

    public int getInt(int i, String str, int i2) {
        return GlobalSettingsManager.getInstance(this.mContext, i).getInt(str, i2);
    }

    public boolean getBoolean(int i, String str, boolean z) {
        return GlobalSettingsManager.getInstance(this.mContext, i).getBoolean(str, z);
    }

    public String getString(int i, String str, String str2) {
        return GlobalSettingsManager.getInstance(this.mContext, i).getString(str, str2);
    }

    public String[] getStringArray(int i, String str, String[] strArr) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return GlobalSettingsManager.getInstance(this.mContext, i).getStringArray(str, strArr);
    }

    public void dump() {
        dump((PrintWriter) null);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        dump(printWriter);
    }

    public IPdnController getPdnController() {
        return this.mPdnController;
    }

    public ICmcAccountManager getCmcAccountManager() {
        return this.mCmcAccountManager;
    }

    public IRcsPolicyManager getRcsPolicyManager() {
        return this.mRcsPolicyManager;
    }

    public IServiceModuleManager getServiceModuleManager() {
        return this.mServiceModuleManager;
    }

    public IRegistrationManager getRegistrationManager() {
        return this.mRegistrationManager;
    }

    public IConfigModule getConfigModule() {
        return this.mConfigModule;
    }

    public IHandlerFactory getHandlerFactory() {
        return this.mHandlerFactory;
    }

    public IAECModule getAECModule() {
        return this.mAECModule;
    }

    public ICmcConnectivityController getCmcConnectivityController() {
        return this.mCmcConnectivityController;
    }

    public IGeolocationController getGeolocationController() {
        return this.mGeolocationController;
    }

    public INtpTimeController getNtpTimeController() {
        return this.mNtpTimeController;
    }

    public IImsDiagMonitor getImsDiagMonitor() {
        return this.mImsDiagMonitor;
    }

    public IFcmHandler getFcmHandler() {
        if (this.mFcmHandler == null) {
            this.mFcmHandler = new FcmHandler(this.mContext);
        }
        return this.mFcmHandler;
    }

    public IIilManager getIilManager(int i) {
        if (this.mIilManagers.size() == 0) {
            return null;
        }
        return this.mIilManagers.get(i);
    }

    public List<ServiceModuleBase> getAllServiceModules() {
        return this.mServiceModuleManager.getAllServiceModules();
    }

    public IWfcEpdgManager getWfcEpdgManager() {
        return this.mWfcEpdgManager;
    }

    public IRawSipSender getRawSipSender() {
        return this.mRawSipManager;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void startAutoConfig(boolean z, Message message) {
        this.mConfigModule.startAutoConfig(z, message, SimUtil.getActiveDataPhoneId());
    }

    public String registerImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener, boolean z, int i) {
        if (isPermissionGranted()) {
            String str = LOG_TAG;
            IMSLog.d(str, i, "registerImsRegistrationListener: broadcast = " + z);
            if (i == -1) {
                IMSLog.d(str, "Requested registerListener without phoneId. register it by all phoneId.");
                this.mSimManagers.forEach(new ImsServiceStub$$ExternalSyntheticLambda1(this, iImsRegistrationListener));
            } else {
                this.mRegistrationManager.registerListener(iImsRegistrationListener, z, i);
            }
            String tokenOfListener = getTokenOfListener(iImsRegistrationListener);
            this.mListenerTokenMap.put(tokenOfListener, new CallBack(iImsRegistrationListener, tokenOfListener));
            return tokenOfListener;
        }
        throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$registerImsRegistrationListener$13(IImsRegistrationListener iImsRegistrationListener, ISimManager iSimManager) {
        this.mRegistrationManager.registerListener(iImsRegistrationListener, iSimManager.getSimSlotIndex());
    }

    public boolean isRcsEnabledByPhoneId(int i) {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, i, "isRcsEnabled:");
        return this.mConfigModule.isValidAcsVersion(i);
    }

    public Binder getBinder(String str) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModuleManager.getBinder(str);
    }

    public Binder getBinder(String str, String str2) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModuleManager.getBinder(str, str2);
    }

    public Binder getSemBinder() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModuleManager.getSemBinder();
    }

    public boolean isDefaultDmValue(String str, int i) {
        Context context = this.mContext;
        String str2 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str2);
        if (ConfigConstants.ATCMD.OMADM_VALUE.equalsIgnoreCase(str)) {
            ContentValues configValues = this.mDmConfigModule.getConfigValues(new String[]{"93", "94", "31"}, i);
            boolean equalsIgnoreCase = "1".equalsIgnoreCase(configValues.getAsString("93"));
            boolean equalsIgnoreCase2 = "1".equalsIgnoreCase(configValues.getAsString("94"));
            boolean equalsIgnoreCase3 = "1".equalsIgnoreCase(configValues.getAsString("31"));
            IMSLog.d(str2, i, "OMADM Default Value [VoLTE : " + equalsIgnoreCase + ", LVC : " + equalsIgnoreCase2 + ", EAB : " + equalsIgnoreCase3 + "]");
            if (!equalsIgnoreCase || !equalsIgnoreCase2 || !equalsIgnoreCase3) {
                return false;
            }
            return true;
        } else if (ConfigConstants.ATCMD.SMS_SETTING.equalsIgnoreCase(str)) {
            String asString = this.mDmConfigModule.getConfigValues(new String[]{"9"}, i).getAsString("9");
            IMSLog.d(str2, i, "SMS Setting Default Value : " + asString);
            return "3GPP2".equalsIgnoreCase(asString);
        } else {
            IMSLog.e(str2, i, str + " is wrong value on isDefaultDmValue");
            return false;
        }
    }

    public boolean setDefaultDmValue(String str, int i) {
        Context context = this.mContext;
        String str2 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str2);
        if (ConfigConstants.ATCMD.OMADM_VALUE.equalsIgnoreCase(str)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(Integer.parseInt("93"))).getName(), "1");
            contentValues.put(((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(Integer.parseInt("94"))).getName(), "1");
            contentValues.put(((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(Integer.parseInt("31"))).getName(), "1");
            this.mContext.getContentResolver().insert(NvConfiguration.URI, contentValues);
            return isDefaultDmValue(str, i);
        }
        IMSLog.e(str2, i, str + " is wrong value on setDefaultDmValue");
        return false;
    }

    public void notifyImsReady(boolean z, int i) {
        Intent intent = new Intent();
        intent.setAction(z ? ImsConstants.Intents.ACTION_SERVICE_UP : ImsConstants.Intents.ACTION_SERVICE_DOWN);
        intent.putExtra(ImsConstants.Intents.EXTRA_ANDORID_PHONE_ID, i);
        intent.putExtra(ImsConstants.Intents.EXTRA_SIMMOBILITY, ImsUtil.isSimMobilityActivated(i));
        intent.addFlags(LogClass.SIM_EVENT);
        IntentUtil.sendBroadcast(this.mContext, intent);
        mIsImsAvailable = true;
        if (this.mIilManagers.size() > 0) {
            this.mIilManagers.get(i).notifyImsReady(z);
        }
        explicitGC();
    }

    private void linkToPhoneDeath() {
        IBinder tryGet = TelephonyFrameworkInitializer.getTelephonyServiceManager().getPhoneSubServiceRegisterer().tryGet();
        if (tryGet != null) {
            try {
                this.mEventLog.logAndAdd("Link to Phone Binder Death");
                tryGet.linkToDeath(new ImsServiceStub$$ExternalSyntheticLambda2(this), 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$linkToPhoneDeath$14() {
        this.mEventLog.logAndAdd("Phone Crashed. Cleanup IMS");
        this.mRegistrationManager.sendDeregister(6);
        getServiceModuleManager().cleanUpModules();
        this.mEventLog.logAndAdd("Restart service");
        IMSLog.c(LogClass.GEN_PHONE_BINDER_DIED, (String) null, true);
        SystemWrapper.exit(0);
    }

    public void sendCmcRecordingEvent(int i, int i2, SemCmcRecordingInfo semCmcRecordingInfo) {
        IVolteServiceModule volteServiceModule = this.mServiceModuleManager.getVolteServiceModule();
        if (volteServiceModule != null) {
            volteServiceModule.sendCmcRecordingEvent(i, i2, semCmcRecordingInfo);
        }
    }

    public CmcCallInfo getCmcCallInfo() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        IVolteServiceModule volteServiceModule = this.mServiceModuleManager.getVolteServiceModule();
        if (volteServiceModule != null) {
            return volteServiceModule.getCmcCallInfo();
        }
        return null;
    }

    public void registerCmcRecordingListener(int i, ISemCmcRecordingListener iSemCmcRecordingListener) {
        IMSLog.d(LOG_TAG, i, "registerCmcRecordingListener");
        IVolteServiceModule volteServiceModule = this.mServiceModuleManager.getVolteServiceModule();
        if (volteServiceModule != null) {
            volteServiceModule.registerCmcRecordingListener(i, iSemCmcRecordingListener);
        }
    }

    public boolean isCmcEmergencyCallSupported(int i) throws RemoteException {
        Context context = this.mContext;
        String str = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str);
        IMSLog.d(str, "isCmcEmergencyCallSupported");
        if (getCmcAccountManager() == null) {
            return false;
        }
        return getCmcAccountManager().isEmergencyCallSupported();
    }

    public boolean isCmcEmergencyNumber(String str, int i) {
        Context context = this.mContext;
        String str2 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str2);
        IMSLog.d(str2, "isCmcEmergencyNumber");
        if (getCmcAccountManager() == null) {
            return false;
        }
        return getCmcAccountManager().isEmergencyNumber(str, i);
    }

    public boolean isCmcPotentialEmergencyNumber(String str, int i) {
        Context context = this.mContext;
        String str2 = LOG_TAG;
        context.enforceCallingOrSelfPermission(PERMISSION, str2);
        IMSLog.d(str2, "isCmcPotentialEmergencyNumber");
        if (getCmcAccountManager() == null) {
            return false;
        }
        return getCmcAccountManager().isPotentialEmergencyNumber(str, i);
    }

    public boolean isSupportVoWiFiDisable5GSA(int i) {
        if (isPermissionGranted()) {
            IMSLog.d(LOG_TAG, i, "isSupportVoWiFiDisable5GSA");
            RegistrationManagerBase registrationManagerBase = this.mRegistrationManager;
            if (registrationManagerBase == null) {
                return false;
            }
            return registrationManagerBase.isSupportVoWiFiDisable5GSA(i);
        }
        throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
    }

    public void setCrossSimPermanentBlocked(int i, boolean z) {
        if (isPermissionGranted()) {
            WfcEpdgManager wfcEpdgManager = this.mWfcEpdgManager;
            if (wfcEpdgManager != null) {
                wfcEpdgManager.setCrossSimPermanentBlocked(i, z);
                return;
            }
            return;
        }
        throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
    }

    public boolean isCrossSimPermanentBlocked(int i) {
        if (isPermissionGranted()) {
            WfcEpdgManager wfcEpdgManager = this.mWfcEpdgManager;
            if (wfcEpdgManager != null) {
                return wfcEpdgManager.isCrossSimPermanentBlocked(i);
            }
            return false;
        }
        throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
    }

    public void setNrInterworkingMode(int i, int i2) {
        if (isPermissionGranted()) {
            WfcEpdgManager wfcEpdgManager = this.mWfcEpdgManager;
            if (wfcEpdgManager != null) {
                wfcEpdgManager.setNrInterworkingMode(i, i2);
                return;
            }
            return;
        }
        throw new SecurityException(LOG_TAG + "[" + i + "] Permission denied");
    }

    private <T extends IInterface> T removeCallback(String str) {
        CallBack remove = this.mListenerTokenMap.remove(str);
        if (remove == null) {
            return null;
        }
        remove.reset();
        try {
            return remove.mListener;
        } catch (ClassCastException e) {
            String str2 = LOG_TAG;
            IMSLog.e(str2, "Unable to removeCallback by " + e);
            return null;
        }
    }

    public static class BootCompleteReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                IMSLog.i(ImsServiceStub.LOG_TAG, "ACTION_BOOT_COMPLETED received");
                int phoneCount = SimUtil.getPhoneCount();
                for (int i = 0; i < phoneCount; i++) {
                    DmConfigHelper.getImsSwitchValue(context, DeviceConfigManager.RCS, i);
                }
                ImsServiceStub.getInstance().getCmcConnectivityController().startNsdBind();
                ImsServiceStub.getInstance().checkGrantAppOpsPermission();
                if (ImsServiceStub.getInstance().getCmcConnectivityController().isEnabledWifiDirectFeature()) {
                    ImsServiceStub.getInstance().getCmcConnectivityController().startP2pBind();
                }
            }
        }
    }

    private final class CallBack<E extends IInterface> implements IBinder.DeathRecipient {
        final E mListener;
        final String mToken;

        CallBack(E e, String str) {
            this.mListener = e;
            this.mToken = str;
            try {
                e.asBinder().linkToDeath(this, 0);
            } catch (RemoteException unused) {
            }
        }

        public void binderDied() {
            reset();
            ImsServiceStub.this.mListenerTokenMap.remove(this.mToken);
        }

        /* access modifiers changed from: protected */
        public void reset() {
            try {
                this.mListener.asBinder().unlinkToDeath(this, 0);
            } catch (NoSuchElementException unused) {
            }
        }
    }

    public void checkGrantAppOpsPermission() {
        Class<String> cls = String.class;
        try {
            Class<?> cls2 = Class.forName("android.app.AppOpsManager");
            AppOpsManager appOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
            Class cls3 = Integer.TYPE;
            if (((Integer) ReflectionUtils.invoke2(cls2.getMethod("semCheckOpWriteSms", new Class[]{cls3, cls}), appOpsManager, new Object[]{Integer.valueOf(Process.myUid()), "com.sec.imsservice"})).intValue() == 0) {
                Log.d(LOG_TAG, "checkGrantAppOpsPermission already allowed");
                return;
            }
            ReflectionUtils.invoke(cls2.getMethod("semSetModeWriteSms", new Class[]{cls3, cls, cls3}), appOpsManager, new Object[]{Integer.valueOf(Process.myUid()), "com.sec.imsservice", 0});
        } catch (ClassNotFoundException | IllegalStateException | NoSuchMethodException | SecurityException e) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("checkGrantAppOpsPermission exception." + e);
        }
    }
}
