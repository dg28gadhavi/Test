package com.sec.internal.ims.servicemodules;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SemSystemProperties;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.feature.RcsFeature;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.samsung.android.feature.SemCscFeature;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.SecFeature;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.cmstore.CmsModule;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.gba.GbaService;
import com.sec.internal.ims.gba.GbaServiceModule;
import com.sec.internal.ims.mdmi.MdmiService;
import com.sec.internal.ims.mdmi.MdmiServiceModule;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.csh.ImageShareModule;
import com.sec.internal.ims.servicemodules.csh.VideoShareModule;
import com.sec.internal.ims.servicemodules.euc.EucModule;
import com.sec.internal.ims.servicemodules.gls.GlsModule;
import com.sec.internal.ims.servicemodules.im.ImModule;
import com.sec.internal.ims.servicemodules.openapi.ImsStatusService;
import com.sec.internal.ims.servicemodules.openapi.ImsStatusServiceModule;
import com.sec.internal.ims.servicemodules.openapi.OpenApiService;
import com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule;
import com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule;
import com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryService;
import com.sec.internal.ims.servicemodules.options.OptionsModule;
import com.sec.internal.ims.servicemodules.options.SemCapabilityDiscoveryService;
import com.sec.internal.ims.servicemodules.presence.PresenceModule;
import com.sec.internal.ims.servicemodules.quantumencryption.QuantumEncryptionService;
import com.sec.internal.ims.servicemodules.quantumencryption.QuantumEncryptionServiceModule;
import com.sec.internal.ims.servicemodules.session.SessionModule;
import com.sec.internal.ims.servicemodules.sms.SmsService;
import com.sec.internal.ims.servicemodules.sms.SmsServiceModule;
import com.sec.internal.ims.servicemodules.ss.UtService;
import com.sec.internal.ims.servicemodules.ss.UtServiceModule;
import com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager;
import com.sec.internal.ims.servicemodules.tapi.service.api.interfaces.ITapiServiceManager;
import com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal;
import com.sec.internal.ims.servicemodules.volte2.VolteService;
import com.sec.internal.ims.servicemodules.volte2.VolteServiceModule;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.cmstore.ICmsModule;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimEventListener;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.handler.IHandlerFactory;
import com.sec.internal.interfaces.ims.gba.IGbaServiceModule;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import com.sec.internal.interfaces.ims.servicemodules.csh.IImageShareModule;
import com.sec.internal.interfaces.ims.servicemodules.csh.IVideoShareModule;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucModule;
import com.sec.internal.interfaces.ims.servicemodules.gls.IGlsModule;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.openapi.IImsStatusServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.openapi.IOpenApiServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.options.IOptionsModule;
import com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule;
import com.sec.internal.interfaces.ims.servicemodules.quantumencryption.IQuantumEncryptionServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;
import com.sec.internal.interfaces.ims.servicemodules.sms.ISmsServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.ss.IUtServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceModuleManager extends Handler implements IServiceModuleManager {
    private static final int EVT_CONFIG_CHANGED = 2;
    private static final int EVT_IMS_SWITCH_UPDATED = 1;
    private static final int EVT_SIM_READY = 3;
    private static final String IMS_SETTINGS_UPDATED = "android.intent.action.IMS_SETTINGS_UPDATED";
    private static final String LOG_TAG = "ServiceModuleManager";
    private static Set<String> OBSERVE_DM_SET = new HashSet();
    private static Set<String> OBSERVE_PREFIX_DM_SET = new HashSet();
    private Map<Integer, Boolean> mAutoConfigCompletedList = new ConcurrentHashMap();
    private Map<String, Binder> mBinders = new HashMap();
    CapabilityDiscoveryModule mCapabilityDiscoveryModule;
    private final ReentrantLock mChangingServiceModulesStateLock = new ReentrantLock();
    CmsModule mCmsModule;
    private ContentObserver mConfigObserver = new ContentObserver(this) {
        public void onChange(boolean z, Uri uri) {
            int simSlotFromUri = UriUtil.getSimSlotFromUri(uri);
            Log.d(ServiceModuleManager.LOG_TAG, "onChange[" + simSlotFromUri + "]: config changed : " + uri.getLastPathSegment());
            if (!TextUtils.isEmpty(uri.getLastPathSegment())) {
                ServiceModuleManager.this.notifyConfigChanged(uri.getLastPathSegment(), simSlotFromUri);
            }
            ServiceModuleManager serviceModuleManager = ServiceModuleManager.this;
            serviceModuleManager.sendMessage(serviceModuleManager.obtainMessage(1, uri));
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    EucModule mEucModule;
    GbaServiceModule mGbaServiceModule;
    GlsModule mGlsModule;
    private final IHandlerFactory mHandlerFactory;
    ImModule mImModule;
    ImageShareModule mImageShareModule;
    private IImsFramework mImsFramework;
    ImsStatusServiceModule mImsStatusServiceModule;
    private Map<Integer, ContentValues> mLastImsServiceSwitches = new ConcurrentHashMap();
    private Looper mLooper;
    OpenApiServiceModule mOpenApiServiceModule;
    OptionsModule mOptionsModule;
    PresenceModule mPresenceModule;
    QuantumEncryptionServiceModule mQuantumEncryptionServiceModule;
    private final IRegistrationManager mRegMan;
    private Binder mSemBinder = null;
    private List<ServiceModuleBase> mServiceModules = new CopyOnWriteArrayList();
    SessionModule mSessionModule;
    private SimEventListener mSimEventListener = new SimEventListener();
    private List<ISimManager> mSimManagers;
    SmsServiceModule mSmsServiceModule;
    private boolean mStarted = false;
    TapiServiceManager mTapiServiceManager;
    UtServiceModule mUtServiceModule;
    VideoShareModule mVideoShareModule;
    VolteServiceModule mVolteServiceModule;

    static {
        OBSERVE_DM_SET.add("EAB_SETTING");
        OBSERVE_DM_SET.add("LVC_ENABLED");
        OBSERVE_DM_SET.add("VOLTE_ENABLED");
        OBSERVE_DM_SET.add("CAP_CACHE_EXP");
        OBSERVE_DM_SET.add("CAP_POLL_INTERVAL");
        OBSERVE_DM_SET.add("SRC_THROTTLE_PUBLISH");
        OBSERVE_DM_SET.add("SUBSCRIBE_MAX_ENTRY");
        OBSERVE_DM_SET.add("AVAIL_CACHE_EXP");
        OBSERVE_DM_SET.add("POLL_LIST_SUB_EXP");
        OBSERVE_DM_SET.add("PUBLISH_TIMER");
        OBSERVE_DM_SET.add("PUBLISH_TIMER_EXTEND");
        OBSERVE_DM_SET.add("PUBLISH_ERR_RETRY_TIMER");
        OBSERVE_DM_SET.add("CAP_DISCOVERY");
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_EAB_SETTING);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_LVC_ENABLED);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_CAP_CACHE_EXP);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_CAP_POLL_INTERVAL);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_SRC_THROTTLE_PUBLISH);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_SUBSCRIBE_MAX_ENTRY);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_AVAIL_CACHE_EXP);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_POLL_LIST_SUB_EXP);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_PUBLISH_TIMER);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_PUBLISH_TIMER_EXTEND);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_PUBLISH_ERR_RETRY_TIMER);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_CAP_DISCOVERY);
    }

    public ServiceModuleManager(Looper looper, Context context, IImsFramework iImsFramework, List<ISimManager> list, IRegistrationManager iRegistrationManager, IHandlerFactory iHandlerFactory) {
        super(looper);
        Log.d(LOG_TAG, "created");
        this.mContext = context;
        this.mSimManagers = list;
        this.mImsFramework = iImsFramework;
        this.mRegMan = iRegistrationManager;
        this.mHandlerFactory = iHandlerFactory;
    }

    public void initSequentially() {
        for (ISimManager next : this.mSimManagers) {
            if (next.isSimLoaded()) {
                int simSlotIndex = next.getSimSlotIndex();
                sendMessage(obtainMessage(3, simSlotIndex, 0, (Object) null));
                IMSLog.d(LOG_TAG, simSlotIndex, "SIM is ready subId:");
            }
            next.registerSimCardEventListener(this.mSimEventListener);
            this.mLastImsServiceSwitches.put(Integer.valueOf(next.getSimSlotIndex()), new ContentValues());
        }
        ContentResolver contentResolver = this.mContext.getContentResolver();
        for (String str : OBSERVE_DM_SET) {
            contentResolver.registerContentObserver(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/" + str), false, this.mConfigObserver);
        }
        for (String str2 : OBSERVE_PREFIX_DM_SET) {
            contentResolver.registerContentObserver(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/" + str2), false, this.mConfigObserver);
        }
        if (SimUtil.getPhoneCount() > 0) {
            Log.d(LOG_TAG, "Initializting ServiceModules.");
            createIMSServiceModules();
            startIMSServiceModules();
            return;
        }
        Log.d(LOG_TAG, "no phone skip Initializting ServiceModules.");
    }

    /* JADX WARNING: type inference failed for: r1v18, types: [com.sec.internal.ims.servicemodules.options.SemCapabilityDiscoveryService, android.os.Binder] */
    public void createIMSServiceModules() {
        this.mChangingServiceModulesStateLock.lock();
        try {
            Log.d(LOG_TAG, "createIMSServiceModules");
            HandlerThread handlerThread = new HandlerThread("ServiceModule");
            handlerThread.start();
            this.mLooper = handlerThread.getLooper();
            SmsServiceModule smsServiceModule = new SmsServiceModule(this.mLooper, this.mContext, this.mHandlerFactory.getSmsHandler());
            this.mSmsServiceModule = smsServiceModule;
            this.mServiceModules.add(smsServiceModule);
            this.mBinders.put("smsip", new SmsService(this.mSmsServiceModule));
            VolteServiceModule volteServiceModule = new VolteServiceModule(this.mLooper, this.mContext, this.mRegMan, this.mImsFramework.getPdnController(), this.mHandlerFactory.getVolteStackAdaptor(), this.mHandlerFactory.getMediaHandler(), this.mHandlerFactory.getOptionsHandler());
            this.mVolteServiceModule = volteServiceModule;
            this.mServiceModules.add(volteServiceModule);
            VolteService volteService = new VolteService(this.mVolteServiceModule);
            this.mBinders.put("mmtel", volteService);
            ImsStatusServiceModule imsStatusServiceModule = new ImsStatusServiceModule(this.mLooper, volteService);
            this.mImsStatusServiceModule = imsStatusServiceModule;
            this.mServiceModules.add(imsStatusServiceModule);
            this.mBinders.put("ImsStatus", new ImsStatusService(this.mImsStatusServiceModule));
            OpenApiServiceModule openApiServiceModule = new OpenApiServiceModule(this.mLooper, this.mContext, this.mHandlerFactory.getRawSipHandler());
            this.mOpenApiServiceModule = openApiServiceModule;
            this.mServiceModules.add(openApiServiceModule);
            this.mBinders.put("OpenApi", new OpenApiService(this.mOpenApiServiceModule));
            QuantumEncryptionServiceModule quantumEncryptionServiceModule = new QuantumEncryptionServiceModule(this.mLooper, this.mContext);
            this.mQuantumEncryptionServiceModule = quantumEncryptionServiceModule;
            this.mServiceModules.add(quantumEncryptionServiceModule);
            this.mBinders.put("quantum", new QuantumEncryptionService(this.mQuantumEncryptionServiceModule));
            UtServiceModule utServiceModule = new UtServiceModule(this.mLooper, this.mContext, this.mImsFramework);
            this.mUtServiceModule = utServiceModule;
            this.mServiceModules.add(utServiceModule);
            this.mBinders.put("ss", new UtService(this.mUtServiceModule));
            GbaServiceModule gbaServiceModule = new GbaServiceModule(this.mLooper, this.mContext, this.mImsFramework);
            this.mGbaServiceModule = gbaServiceModule;
            this.mServiceModules.add(gbaServiceModule);
            this.mBinders.put("GbaService", new GbaService(this.mGbaServiceModule));
            this.mBinders.put("options", new CapabilityDiscoveryService());
            this.mSemBinder = new SemCapabilityDiscoveryService();
            MdmiServiceModule mdmiServiceModule = new MdmiServiceModule(handlerThread.getLooper(), this.mContext);
            this.mServiceModules.add(mdmiServiceModule);
            this.mBinders.put("mdmi", new MdmiService(mdmiServiceModule));
            for (ServiceModuleBase init : this.mServiceModules) {
                init.init();
            }
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0048, code lost:
        if (r14.hasService(com.sec.internal.constants.ims.SipMsg.EVENT_PRESENCE) != false) goto L_0x004a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void createRcsServiceModulesAndStart(com.sec.ims.settings.ImsProfile r14, int r15) {
        /*
            r13 = this;
            java.lang.String r0 = "vs"
            java.lang.String r1 = "options"
            java.util.concurrent.CopyOnWriteArrayList r2 = new java.util.concurrent.CopyOnWriteArrayList
            r2.<init>()
            java.util.concurrent.locks.ReentrantLock r3 = r13.mChangingServiceModulesStateLock
            r3.lock()
            android.content.Context r3 = r13.mContext     // Catch:{ all -> 0x01f6 }
            java.lang.String r4 = "rcsswitch"
            int r3 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r3, (java.lang.String) r4, (int) r15)     // Catch:{ all -> 0x01f6 }
            r4 = 1
            if (r3 != r4) goto L_0x001c
            goto L_0x001d
        L_0x001c:
            r4 = 0
        L_0x001d:
            com.sec.internal.constants.Mno r3 = com.sec.internal.helper.SimUtil.getSimMno(r15)     // Catch:{ all -> 0x01f6 }
            if (r4 != 0) goto L_0x003b
            boolean r4 = r14.getSimMobility()     // Catch:{ all -> 0x01f6 }
            if (r4 == 0) goto L_0x01ed
            boolean r4 = com.sec.internal.helper.os.DeviceUtil.isTablet()     // Catch:{ all -> 0x01f6 }
            if (r4 != 0) goto L_0x01ed
            boolean r4 = com.sec.internal.helper.os.DeviceUtil.isUSOpenDevice()     // Catch:{ all -> 0x01f6 }
            if (r4 != 0) goto L_0x01ed
            boolean r4 = r3.isUSA()     // Catch:{ all -> 0x01f6 }
            if (r4 == 0) goto L_0x01ed
        L_0x003b:
            boolean r4 = r14.hasService(r1)     // Catch:{ all -> 0x01f6 }
            java.lang.String r5 = "presence"
            if (r4 != 0) goto L_0x004a
            boolean r4 = r14.hasService(r5)     // Catch:{ all -> 0x01f6 }
            if (r4 == 0) goto L_0x01ed
        L_0x004a:
            com.sec.internal.ims.servicemodules.im.ImModule r4 = r13.mImModule     // Catch:{ all -> 0x01f6 }
            if (r4 != 0) goto L_0x0069
            com.sec.internal.ims.servicemodules.im.ImModule r4 = new com.sec.internal.ims.servicemodules.im.ImModule     // Catch:{ all -> 0x01f6 }
            android.os.Looper r6 = r13.mLooper     // Catch:{ all -> 0x01f6 }
            android.content.Context r7 = r13.mContext     // Catch:{ all -> 0x01f6 }
            com.sec.internal.interfaces.ims.core.handler.IHandlerFactory r8 = r13.mHandlerFactory     // Catch:{ all -> 0x01f6 }
            com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface r8 = r8.getImHandler()     // Catch:{ all -> 0x01f6 }
            r4.<init>(r6, r7, r8)     // Catch:{ all -> 0x01f6 }
            r13.mImModule = r4     // Catch:{ all -> 0x01f6 }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r6 = r13.mServiceModules     // Catch:{ all -> 0x01f6 }
            r6.add(r4)     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.im.ImModule r4 = r13.mImModule     // Catch:{ all -> 0x01f6 }
            r2.add(r4)     // Catch:{ all -> 0x01f6 }
        L_0x0069:
            com.sec.internal.ims.servicemodules.session.SessionModule r4 = r13.mSessionModule     // Catch:{ all -> 0x01f6 }
            if (r4 != 0) goto L_0x0088
            com.sec.internal.ims.servicemodules.session.SessionModule r4 = new com.sec.internal.ims.servicemodules.session.SessionModule     // Catch:{ all -> 0x01f6 }
            android.os.Looper r6 = r13.mLooper     // Catch:{ all -> 0x01f6 }
            android.content.Context r7 = r13.mContext     // Catch:{ all -> 0x01f6 }
            com.sec.internal.interfaces.ims.core.handler.IHandlerFactory r8 = r13.mHandlerFactory     // Catch:{ all -> 0x01f6 }
            com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface r8 = r8.getImHandler()     // Catch:{ all -> 0x01f6 }
            r4.<init>(r6, r7, r8)     // Catch:{ all -> 0x01f6 }
            r13.mSessionModule = r4     // Catch:{ all -> 0x01f6 }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r6 = r13.mServiceModules     // Catch:{ all -> 0x01f6 }
            r6.add(r4)     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.session.SessionModule r4 = r13.mSessionModule     // Catch:{ all -> 0x01f6 }
            r2.add(r4)     // Catch:{ all -> 0x01f6 }
        L_0x0088:
            java.lang.String r4 = "gls"
            boolean r4 = r14.hasService(r4)     // Catch:{ all -> 0x01f6 }
            if (r4 != 0) goto L_0x0094
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.ATT     // Catch:{ all -> 0x01f6 }
            if (r3 != r4) goto L_0x00ad
        L_0x0094:
            com.sec.internal.ims.servicemodules.gls.GlsModule r3 = r13.mGlsModule     // Catch:{ all -> 0x01f6 }
            if (r3 != 0) goto L_0x00ad
            com.sec.internal.ims.servicemodules.gls.GlsModule r3 = new com.sec.internal.ims.servicemodules.gls.GlsModule     // Catch:{ all -> 0x01f6 }
            android.os.Looper r4 = r13.mLooper     // Catch:{ all -> 0x01f6 }
            android.content.Context r6 = r13.mContext     // Catch:{ all -> 0x01f6 }
            r3.<init>(r4, r6)     // Catch:{ all -> 0x01f6 }
            r13.mGlsModule = r3     // Catch:{ all -> 0x01f6 }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r4 = r13.mServiceModules     // Catch:{ all -> 0x01f6 }
            r4.add(r3)     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.gls.GlsModule r3 = r13.mGlsModule     // Catch:{ all -> 0x01f6 }
            r2.add(r3)     // Catch:{ all -> 0x01f6 }
        L_0x00ad:
            java.lang.String r3 = "euc"
            boolean r3 = r14.hasService(r3)     // Catch:{ all -> 0x01f6 }
            if (r3 == 0) goto L_0x00d4
            com.sec.internal.ims.servicemodules.euc.EucModule r3 = r13.mEucModule     // Catch:{ all -> 0x01f6 }
            if (r3 != 0) goto L_0x00d4
            com.sec.internal.ims.servicemodules.euc.EucModule r3 = new com.sec.internal.ims.servicemodules.euc.EucModule     // Catch:{ all -> 0x01f6 }
            android.os.Looper r4 = r13.mLooper     // Catch:{ all -> 0x01f6 }
            android.content.Context r6 = r13.mContext     // Catch:{ all -> 0x01f6 }
            com.sec.internal.interfaces.ims.core.handler.IHandlerFactory r7 = r13.mHandlerFactory     // Catch:{ all -> 0x01f6 }
            com.sec.internal.interfaces.ims.servicemodules.euc.IEucServiceInterface r7 = r7.getEucHandler()     // Catch:{ all -> 0x01f6 }
            r3.<init>(r4, r6, r7)     // Catch:{ all -> 0x01f6 }
            r13.mEucModule = r3     // Catch:{ all -> 0x01f6 }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r4 = r13.mServiceModules     // Catch:{ all -> 0x01f6 }
            r4.add(r3)     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.euc.EucModule r3 = r13.mEucModule     // Catch:{ all -> 0x01f6 }
            r2.add(r3)     // Catch:{ all -> 0x01f6 }
        L_0x00d4:
            java.lang.String r3 = "is"
            boolean r3 = r14.hasService(r3)     // Catch:{ all -> 0x01f6 }
            if (r3 == 0) goto L_0x00fb
            com.sec.internal.ims.servicemodules.csh.ImageShareModule r3 = r13.mImageShareModule     // Catch:{ all -> 0x01f6 }
            if (r3 != 0) goto L_0x00fb
            com.sec.internal.ims.servicemodules.csh.ImageShareModule r3 = new com.sec.internal.ims.servicemodules.csh.ImageShareModule     // Catch:{ all -> 0x01f6 }
            android.os.Looper r4 = r13.mLooper     // Catch:{ all -> 0x01f6 }
            android.content.Context r6 = r13.mContext     // Catch:{ all -> 0x01f6 }
            com.sec.internal.interfaces.ims.core.handler.IHandlerFactory r7 = r13.mHandlerFactory     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.csh.event.IIshServiceInterface r7 = r7.getIshHandler()     // Catch:{ all -> 0x01f6 }
            r3.<init>(r4, r6, r7)     // Catch:{ all -> 0x01f6 }
            r13.mImageShareModule = r3     // Catch:{ all -> 0x01f6 }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r4 = r13.mServiceModules     // Catch:{ all -> 0x01f6 }
            r4.add(r3)     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.csh.ImageShareModule r3 = r13.mImageShareModule     // Catch:{ all -> 0x01f6 }
            r2.add(r3)     // Catch:{ all -> 0x01f6 }
        L_0x00fb:
            boolean r14 = r14.hasService(r0)     // Catch:{ all -> 0x01f6 }
            if (r14 == 0) goto L_0x012c
            com.sec.internal.ims.servicemodules.csh.VideoShareModule r14 = r13.mVideoShareModule     // Catch:{ all -> 0x01f6 }
            if (r14 != 0) goto L_0x012c
            com.sec.internal.ims.servicemodules.csh.VideoShareModule r14 = new com.sec.internal.ims.servicemodules.csh.VideoShareModule     // Catch:{ all -> 0x01f6 }
            android.os.Looper r3 = r13.mLooper     // Catch:{ all -> 0x01f6 }
            android.content.Context r4 = r13.mContext     // Catch:{ all -> 0x01f6 }
            com.sec.internal.interfaces.ims.core.handler.IHandlerFactory r6 = r13.mHandlerFactory     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.csh.event.IvshServiceInterface r6 = r6.getVshHandler()     // Catch:{ all -> 0x01f6 }
            r14.<init>(r3, r4, r6)     // Catch:{ all -> 0x01f6 }
            r13.mVideoShareModule = r14     // Catch:{ all -> 0x01f6 }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r3 = r13.mServiceModules     // Catch:{ all -> 0x01f6 }
            r3.add(r14)     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.csh.VideoShareModule r14 = r13.mVideoShareModule     // Catch:{ all -> 0x01f6 }
            r2.add(r14)     // Catch:{ all -> 0x01f6 }
            java.util.Map<java.lang.String, android.os.Binder> r14 = r13.mBinders     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.csh.VshBinderFuntions r3 = new com.sec.internal.ims.servicemodules.csh.VshBinderFuntions     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.csh.VideoShareModule r4 = r13.mVideoShareModule     // Catch:{ all -> 0x01f6 }
            r3.<init>(r4)     // Catch:{ all -> 0x01f6 }
            r14.put(r0, r3)     // Catch:{ all -> 0x01f6 }
        L_0x012c:
            com.sec.internal.ims.servicemodules.options.OptionsModule r14 = r13.mOptionsModule     // Catch:{ all -> 0x01f6 }
            if (r14 != 0) goto L_0x0145
            com.sec.internal.ims.servicemodules.options.OptionsModule r14 = new com.sec.internal.ims.servicemodules.options.OptionsModule     // Catch:{ all -> 0x01f6 }
            android.os.Looper r0 = r13.mLooper     // Catch:{ all -> 0x01f6 }
            android.content.Context r3 = r13.mContext     // Catch:{ all -> 0x01f6 }
            r14.<init>(r0, r3)     // Catch:{ all -> 0x01f6 }
            r13.mOptionsModule = r14     // Catch:{ all -> 0x01f6 }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r0 = r13.mServiceModules     // Catch:{ all -> 0x01f6 }
            r0.add(r14)     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.options.OptionsModule r14 = r13.mOptionsModule     // Catch:{ all -> 0x01f6 }
            r2.add(r14)     // Catch:{ all -> 0x01f6 }
        L_0x0145:
            com.sec.internal.ims.servicemodules.presence.PresenceModule r14 = r13.mPresenceModule     // Catch:{ all -> 0x01f6 }
            if (r14 != 0) goto L_0x015e
            com.sec.internal.ims.servicemodules.presence.PresenceModule r14 = new com.sec.internal.ims.servicemodules.presence.PresenceModule     // Catch:{ all -> 0x01f6 }
            android.os.Looper r0 = r13.mLooper     // Catch:{ all -> 0x01f6 }
            android.content.Context r3 = r13.mContext     // Catch:{ all -> 0x01f6 }
            r14.<init>(r0, r3)     // Catch:{ all -> 0x01f6 }
            r13.mPresenceModule = r14     // Catch:{ all -> 0x01f6 }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r0 = r13.mServiceModules     // Catch:{ all -> 0x01f6 }
            r0.add(r14)     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.presence.PresenceModule r14 = r13.mPresenceModule     // Catch:{ all -> 0x01f6 }
            r2.add(r14)     // Catch:{ all -> 0x01f6 }
        L_0x015e:
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r14 = r13.mCapabilityDiscoveryModule     // Catch:{ all -> 0x01f6 }
            if (r14 != 0) goto L_0x01a4
            com.sec.internal.ims.servicemodules.options.OptionsModule r14 = r13.mOptionsModule     // Catch:{ all -> 0x01f6 }
            if (r14 == 0) goto L_0x01a4
            com.sec.internal.ims.servicemodules.presence.PresenceModule r14 = r13.mPresenceModule     // Catch:{ all -> 0x01f6 }
            if (r14 == 0) goto L_0x01a4
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r14 = new com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule     // Catch:{ all -> 0x01f6 }
            android.os.Looper r7 = r13.mLooper     // Catch:{ all -> 0x01f6 }
            android.content.Context r8 = r13.mContext     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.options.OptionsModule r9 = r13.mOptionsModule     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.presence.PresenceModule r10 = r13.mPresenceModule     // Catch:{ all -> 0x01f6 }
            com.sec.internal.interfaces.ims.core.IRegistrationManager r11 = r13.mRegMan     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.im.ImModule r12 = r13.mImModule     // Catch:{ all -> 0x01f6 }
            r6 = r14
            r6.<init>(r7, r8, r9, r10, r11, r12)     // Catch:{ all -> 0x01f6 }
            r13.mCapabilityDiscoveryModule = r14     // Catch:{ all -> 0x01f6 }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r0 = r13.mServiceModules     // Catch:{ all -> 0x01f6 }
            r0.add(r14)     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r14 = r13.mCapabilityDiscoveryModule     // Catch:{ all -> 0x01f6 }
            r2.add(r14)     // Catch:{ all -> 0x01f6 }
            java.util.Map<java.lang.String, android.os.Binder> r14 = r13.mBinders     // Catch:{ all -> 0x01f6 }
            java.lang.Object r14 = r14.get(r1)     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryService r14 = (com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryService) r14     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r0 = r13.mCapabilityDiscoveryModule     // Catch:{ all -> 0x01f6 }
            r14.setServiceModule(r0)     // Catch:{ all -> 0x01f6 }
            android.os.Binder r14 = r13.mSemBinder     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.options.SemCapabilityDiscoveryService r14 = (com.sec.internal.ims.servicemodules.options.SemCapabilityDiscoveryService) r14     // Catch:{ all -> 0x01f6 }
            java.util.Map<java.lang.String, android.os.Binder> r0 = r13.mBinders     // Catch:{ all -> 0x01f6 }
            java.lang.Object r0 = r0.get(r1)     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryService r0 = (com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryService) r0     // Catch:{ all -> 0x01f6 }
            r14.setServiceModule(r0)     // Catch:{ all -> 0x01f6 }
        L_0x01a4:
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r14 = r13.mCapabilityDiscoveryModule     // Catch:{ all -> 0x01f6 }
            if (r14 == 0) goto L_0x01b4
            java.util.Map<java.lang.String, android.os.Binder> r14 = r13.mBinders     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.presence.PresenceService r0 = new com.sec.internal.ims.servicemodules.presence.PresenceService     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r1 = r13.mCapabilityDiscoveryModule     // Catch:{ all -> 0x01f6 }
            r0.<init>(r1)     // Catch:{ all -> 0x01f6 }
            r14.put(r5, r0)     // Catch:{ all -> 0x01f6 }
        L_0x01b4:
            com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager r14 = r13.mTapiServiceManager     // Catch:{ all -> 0x01f6 }
            if (r14 != 0) goto L_0x01cd
            com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager r14 = new com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager     // Catch:{ all -> 0x01f6 }
            android.os.Looper r0 = r13.mLooper     // Catch:{ all -> 0x01f6 }
            android.content.Context r1 = r13.mContext     // Catch:{ all -> 0x01f6 }
            r14.<init>(r0, r1)     // Catch:{ all -> 0x01f6 }
            r13.mTapiServiceManager = r14     // Catch:{ all -> 0x01f6 }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r0 = r13.mServiceModules     // Catch:{ all -> 0x01f6 }
            r0.add(r14)     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager r14 = r13.mTapiServiceManager     // Catch:{ all -> 0x01f6 }
            r2.add(r14)     // Catch:{ all -> 0x01f6 }
        L_0x01cd:
            java.util.Iterator r14 = r2.iterator()     // Catch:{ all -> 0x01f6 }
        L_0x01d1:
            boolean r0 = r14.hasNext()     // Catch:{ all -> 0x01f6 }
            if (r0 == 0) goto L_0x01ed
            java.lang.Object r0 = r14.next()     // Catch:{ all -> 0x01f6 }
            com.sec.internal.ims.servicemodules.base.ServiceModuleBase r0 = (com.sec.internal.ims.servicemodules.base.ServiceModuleBase) r0     // Catch:{ all -> 0x01f6 }
            boolean r1 = r0.isReady()     // Catch:{ all -> 0x01f6 }
            if (r1 != 0) goto L_0x01d1
            boolean r1 = r0.isRunning()     // Catch:{ all -> 0x01f6 }
            if (r1 != 0) goto L_0x01d1
            r0.init()     // Catch:{ all -> 0x01f6 }
            goto L_0x01d1
        L_0x01ed:
            java.util.concurrent.locks.ReentrantLock r14 = r13.mChangingServiceModulesStateLock
            r14.unlock()
            r13.startRcsServiceModules(r2, r15)
            return
        L_0x01f6:
            r14 = move-exception
            java.util.concurrent.locks.ReentrantLock r13 = r13.mChangingServiceModulesStateLock
            r13.unlock()
            throw r14
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ServiceModuleManager.createRcsServiceModulesAndStart(com.sec.ims.settings.ImsProfile, int):void");
    }

    public void serviceStartDeterminer(List<ImsProfile> list, int i) {
        for (ImsProfile next : list) {
            if (ImsProfile.hasRcsService(next)) {
                createRcsServiceModulesAndStart(next, i);
            }
        }
        List<String> extendedServices = getExtendedServices(i);
        if (!extendedServices.isEmpty() && extendedServices.contains("cms") && !"AIO".equals(OmcCode.getNWCode(i))) {
            boolean z = false;
            if (!Mno.ATT.equals(SimUtil.getSimMno(i)) || (!TextUtils.isEmpty(SemCscFeature.getInstance().getString(SecFeature.CSC.TAG_CSCFEATURE_MESSAGE_CONFIGOPBACKUPSYNC)) && SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) <= 33)) {
                if (ImsRegistry.getBoolean(i, GlobalSettingsConstants.Registration.CMS_OPEN_DEVICE_VVM_ENABLED, false) && SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) >= 34) {
                    String str = Build.MODEL;
                    if (!str.contains("A156U1") && !str.contains("A256U1")) {
                        z = true;
                    }
                }
                IMSLog.i(LOG_TAG, i, "serviceStartDeterminer isOpenDeviceVVMEnabled: " + z);
                if (this.mCmsModule == null) {
                    CmsModule cmsModule = new CmsModule(this.mLooper, this.mContext);
                    this.mCmsModule = cmsModule;
                    this.mServiceModules.add(cmsModule);
                    if (isStartRequired(this.mCmsModule, i, (ISimManager) null) || z) {
                        this.mCmsModule.init();
                        this.mCmsModule.start();
                        return;
                    }
                    return;
                }
                return;
            }
            IMSLog.e(LOG_TAG, i, "AMBS has been disabled for this model");
        }
    }

    public boolean isLooperExist() {
        return this.mLooper != null;
    }

    private synchronized void startRcsServiceModules(List<ServiceModuleBase> list, int i) {
        for (ServiceModuleBase next : list) {
            if (isStartRequired(next, i, (ISimManager) null)) {
                next.start();
            }
        }
        this.mLastImsServiceSwitches.put(Integer.valueOf(i), DmConfigHelper.getImsSwitchValue(this.mContext, (String[]) null, i));
    }

    public void startIMSServiceModules() {
        this.mChangingServiceModulesStateLock.lock();
        try {
            Log.d(LOG_TAG, "startServiceModules");
            if (this.mStarted) {
                Log.d(LOG_TAG, "startServiceModules() - already started");
                return;
            }
            for (ISimManager next : this.mSimManagers) {
                int simSlotIndex = next.getSimSlotIndex();
                for (ServiceModuleBase next2 : this.mServiceModules) {
                    if (isStartRequired(next2, simSlotIndex, next)) {
                        next2.start();
                    }
                }
                this.mLastImsServiceSwitches.put(Integer.valueOf(simSlotIndex), DmConfigHelper.getImsSwitchValue(this.mContext, (String[]) null, simSlotIndex));
            }
            this.mStarted = true;
            this.mChangingServiceModulesStateLock.unlock();
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    public Binder getBinder(String str) {
        return getBinder(str, (String) null);
    }

    public synchronized Binder getBinder(String str, String str2) {
        if (str2 != null) {
            str = str + CmcConstants.E_NUM_SLOT_SPLIT + str2;
        }
        Log.d(LOG_TAG, "getBinder for " + str);
        return this.mBinders.get(str);
    }

    public Binder getSemBinder() {
        return this.mSemBinder;
    }

    public void notifyReRegistering(int i, Set<String> set) {
        this.mChangingServiceModulesStateLock.lock();
        try {
            IMSLog.d(LOG_TAG, i, "notify Ims Re-registration : " + set);
            for (ServiceModuleBase next : this.mServiceModules) {
                if (next.isRunning()) {
                    next.onReRegistering(i, set);
                }
            }
            updateCapabilities(i);
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    private boolean needRegistrationNotification(ServiceModuleBase serviceModuleBase, Set<String> set) {
        HashSet hashSet = new HashSet(Arrays.asList(serviceModuleBase.getServicesRequiring()));
        Log.d(LOG_TAG, "Service not matched. Not notified to " + serviceModuleBase.getName() + " " + hashSet);
        return !Collections.disjoint(set, hashSet);
    }

    public void notifyImsRegistration(ImsRegistration imsRegistration, boolean z, int i) {
        int phoneId = imsRegistration.getPhoneId();
        ImsProfile imsProfile = imsRegistration.getImsProfile();
        IMSLog.d(LOG_TAG, phoneId, "notifyImsRegistration: [" + imsProfile.getName() + "] registered: " + z + ", errorCode: " + i);
        ImsRegistration imsRegistration2 = new ImsRegistration(imsRegistration);
        IConfigModule configModule = this.mImsFramework.getConfigModule();
        if (configModule != null) {
            configModule.onRegistrationStatusChanged(z, i, imsRegistration);
        }
        if (!z) {
            this.mChangingServiceModulesStateLock.lock();
            try {
                for (ServiceModuleBase next : this.mServiceModules) {
                    if (next.isRunning()) {
                        if (needRegistrationNotification(next, imsRegistration2.getServices())) {
                            next.onDeregistered(imsRegistration2, i);
                        }
                    }
                }
            } finally {
                this.mChangingServiceModulesStateLock.unlock();
            }
        } else {
            Set allServiceSetFromAllNetwork = imsRegistration2.getImsProfile().getAllServiceSetFromAllNetwork();
            for (String remove : imsRegistration2.getServices()) {
                allServiceSetFromAllNetwork.remove(remove);
            }
            if (configModule != null && !configModule.isValidAcsVersion(phoneId)) {
                Log.d(LOG_TAG, "RCS disabled : remove rcs services from deregi list");
                for (String remove2 : ImsProfile.getRcsServiceList()) {
                    allServiceSetFromAllNetwork.remove(remove2);
                }
            }
            this.mChangingServiceModulesStateLock.lock();
            try {
                for (ServiceModuleBase next2 : this.mServiceModules) {
                    if (next2.isRunning()) {
                        if (needRegistrationNotification(next2, imsRegistration.getServices())) {
                            next2.onRegistered(imsRegistration2);
                        } else if (needRegistrationNotification(next2, allServiceSetFromAllNetwork)) {
                            next2.onDeregistered(imsRegistration2, i);
                        }
                    }
                }
            } finally {
                this.mChangingServiceModulesStateLock.unlock();
            }
        }
        if (((Boolean) Optional.ofNullable(this.mSimManagers.get(phoneId)).map(new ServiceModuleManager$$ExternalSyntheticLambda0()).map(new ServiceModuleManager$$ExternalSyntheticLambda1()).orElse(Boolean.FALSE)).booleanValue() && imsProfile.hasEmergencySupport()) {
            IVolteServiceModule volteServiceModule = this.mImsFramework.getServiceModuleManager().getVolteServiceModule();
            if (!volteServiceModule.isRunning()) {
                if (z) {
                    volteServiceModule.onRegistered(imsRegistration2);
                } else {
                    volteServiceModule.onDeregistered(imsRegistration2, i);
                }
            }
        }
        updateCapabilities(phoneId);
    }

    public void notifyDeregistering(ImsRegistration imsRegistration) {
        this.mChangingServiceModulesStateLock.lock();
        try {
            boolean z = false;
            for (ServiceModuleBase next : this.mServiceModules) {
                if (next.isRunning() && needRegistrationNotification(next, imsRegistration.getServices())) {
                    next.onDeregistering(imsRegistration);
                    z = true;
                }
            }
            if (z) {
                updateCapabilities(imsRegistration.getPhoneId());
            }
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    public void notifyRcsDeregistering(Set<String> set, ImsRegistration imsRegistration) {
        HashSet hashSet = new HashSet();
        boolean z = false;
        for (String str : ImsProfile.getRcsServiceList()) {
            if (set.contains(str)) {
                hashSet.add(str);
            }
        }
        this.mChangingServiceModulesStateLock.lock();
        try {
            for (ServiceModuleBase next : this.mServiceModules) {
                if (next.isRunning() && needRegistrationNotification(next, hashSet)) {
                    next.onDeregistering(imsRegistration);
                    z = true;
                }
            }
            if (z) {
                updateCapabilities(imsRegistration.getPhoneId());
            }
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    public void notifyConfigChanged(String str, int i) {
        Log.d(LOG_TAG, "notifyConfigChanged: dmUri " + str);
        this.mChangingServiceModulesStateLock.lock();
        try {
            for (ServiceModuleBase next : this.mServiceModules) {
                if (next.isRunning()) {
                    next.onImsConifgChanged(i, str);
                }
            }
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    /* JADX INFO: finally extract failed */
    public void notifyConfigured(boolean z, int i) {
        Log.d(LOG_TAG, "notifyConfigured: phoneId " + i);
        if (!z || (this.mAutoConfigCompletedList.containsKey(Integer.valueOf(i)) && this.mAutoConfigCompletedList.get(Integer.valueOf(i)).booleanValue())) {
            this.mChangingServiceModulesStateLock.lock();
            try {
                for (ServiceModuleBase next : this.mServiceModules) {
                    if (next.isRunning()) {
                        if (next != this.mCapabilityDiscoveryModule) {
                            next.onConfigured(i);
                        }
                    }
                }
                this.mChangingServiceModulesStateLock.unlock();
                CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscoveryModule;
                if (capabilityDiscoveryModule != null) {
                    if (capabilityDiscoveryModule.isRunning()) {
                        Log.d(LOG_TAG, "notifyConfigured: CDM is running");
                        this.mCapabilityDiscoveryModule.onConfigured(i);
                    } else {
                        Log.d(LOG_TAG, "notifyConfigured: CDM is not running, trigger tryRegister");
                        this.mImsFramework.getRegistrationManager().setOwnCapabilities(i, new Capabilities());
                    }
                }
                this.mImsFramework.getRegistrationManager().setRegiConfig(i);
            } catch (Throwable th) {
                this.mChangingServiceModulesStateLock.unlock();
                throw th;
            }
        }
    }

    public void notifySimChange(int i) {
        Log.d(LOG_TAG, "notifySimChange");
        Mno simMno = SimUtil.getSimMno(i);
        for (ServiceModuleBase next : this.mServiceModules) {
            if (next.isRunning() || (next == this.mCapabilityDiscoveryModule && ConfigUtil.isRcsEur(simMno))) {
                next.onSimChanged(i);
            }
        }
    }

    public void notifyNetworkChanged(NetworkEvent networkEvent, int i) {
        for (ServiceModuleBase next : this.mServiceModules) {
            if (next.isRunning()) {
                next.onNetworkChanged(new NetworkEvent(networkEvent), i);
            }
        }
        updateCapabilities(i);
    }

    public void handleIntent(Intent intent) {
        Log.d(LOG_TAG, "handleIntent:");
        for (ServiceModuleBase next : this.mServiceModules) {
            if (next.isRunning()) {
                next.handleIntent(intent);
            }
        }
    }

    public List<ServiceModuleBase> getAllServiceModules() {
        return Collections.unmodifiableList(this.mServiceModules);
    }

    public Handler getServiceModuleHandler(String str) {
        for (ServiceModuleBase next : this.mServiceModules) {
            if (next.getClass().getSimpleName().equals(str)) {
                return next;
            }
        }
        return null;
    }

    public IImModule getImModule() {
        return this.mImModule;
    }

    public IGlsModule getGlsModule() {
        return this.mGlsModule;
    }

    public IOptionsModule getOptionsModule() {
        return this.mOptionsModule;
    }

    public IPresenceModule getPresenceModule() {
        return this.mPresenceModule;
    }

    public ICapabilityDiscoveryModule getCapabilityDiscoveryModule() {
        return this.mCapabilityDiscoveryModule;
    }

    public IEucModule getEucModule() {
        return this.mEucModule;
    }

    public ISmsServiceModule getSmsServiceModule() {
        return this.mSmsServiceModule;
    }

    public ISessionModule getSessionModule() {
        return this.mSessionModule;
    }

    public ICmsModule getCmsModule() {
        return this.mCmsModule;
    }

    public IVolteServiceModule getVolteServiceModule() {
        return this.mVolteServiceModule;
    }

    public IImsStatusServiceModule getImsStatusServiceModule() {
        return this.mImsStatusServiceModule;
    }

    public IImageShareModule getImageShareModule() {
        return this.mImageShareModule;
    }

    public IVideoShareModule getVideoShareModule() {
        return this.mVideoShareModule;
    }

    public ITapiServiceManager getTapiServiceManager() {
        return this.mTapiServiceManager;
    }

    public IOpenApiServiceModule getOpenApiServiceModule() {
        return this.mOpenApiServiceModule;
    }

    public IUtServiceModule getUtServiceModule() {
        return this.mUtServiceModule;
    }

    public IGbaServiceModule getGbaServiceModule() {
        return this.mGbaServiceModule;
    }

    public IQuantumEncryptionServiceModule getQuantumEncryptionServiceModule() {
        return this.mQuantumEncryptionServiceModule;
    }

    public void dump() {
        for (ServiceModuleBase next : this.mServiceModules) {
            if (next.isRunning()) {
                next.dump();
            }
        }
    }

    public void notifyCallStateChanged(List<ICall> list, int i) {
        for (ServiceModuleBase onCallStateChanged : this.mServiceModules) {
            onCallStateChanged.onCallStateChanged(i, list);
        }
    }

    public void notifyAutoConfigDone(int i) {
        IMSLog.d(LOG_TAG, i, "notifyAutoConfigDone");
        this.mAutoConfigCompletedList.put(Integer.valueOf(i), Boolean.TRUE);
    }

    public void notifyOmadmVolteConfigDone(int i) {
        Log.d(LOG_TAG, "notifyOmadmVolteConfigDone()");
        Uri.Builder buildUpon = Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/").buildUpon();
        sendMessage(obtainMessage(1, buildUpon.fragment("simslot" + i).build()));
    }

    public void notifyImsSwitchUpdateToApp() {
        IntentUtil.sendBroadcast(this.mContext, new Intent(IMS_SETTINGS_UPDATED));
    }

    public void onImsSwitchUpdated(int i) {
        ContentValues contentValues;
        ContentValues contentValues2;
        Integer asInteger;
        int i2 = i;
        ContentValues imsSwitchValue = DmConfigHelper.getImsSwitchValue(this.mContext, (String[]) null, i2);
        this.mChangingServiceModulesStateLock.lock();
        try {
            IMSLog.d(LOG_TAG, i2, "onImsSwitchUpdated " + imsSwitchValue + ", old " + this.mLastImsServiceSwitches.get(Integer.valueOf(i)));
            boolean isCmcEnabled = this.mImsFramework.getCmcAccountManager().isCmcEnabled();
            Iterator<ServiceModuleBase> it = this.mServiceModules.iterator();
            while (true) {
                boolean z = true;
                if (!it.hasNext()) {
                    break;
                }
                ServiceModuleBase next = it.next();
                if (next.isRunning()) {
                    String[] servicesRequiring = next.getServicesRequiring();
                    int length = servicesRequiring.length;
                    boolean z2 = true;
                    boolean z3 = true;
                    int i3 = 0;
                    while (i3 < length) {
                        String str = servicesRequiring[i3];
                        Integer asInteger2 = imsSwitchValue.getAsInteger(str);
                        if (asInteger2 != null && asInteger2.intValue() == z && DmConfigHelper.readSwitch(this.mContext, str, z, i2)) {
                            z2 = false;
                            z3 = false;
                        }
                        if (next.getName().equals(IVolteServiceModuleInternal.NAME) && isCmcEnabled) {
                            Log.d(LOG_TAG, "onImsSwitchUpdated: CMC device: " + next.getName() + " module.");
                            z2 = false;
                            z3 = false;
                        }
                        for (ISimManager next2 : this.mSimManagers) {
                            boolean z4 = isCmcEnabled;
                            if (!(next2.getSimSlotIndex() == i2 || (contentValues2 = this.mLastImsServiceSwitches.get(Integer.valueOf(next2.getSimSlotIndex()))) == null || contentValues2.size() <= 0 || (asInteger = contentValues2.getAsInteger(str)) == null || asInteger.intValue() != 1)) {
                                Log.d(LOG_TAG, "onImsSwitchUpdated: opposite sim slot enabled " + next.getName() + " module.");
                                z3 = false;
                            }
                            isCmcEnabled = z4;
                        }
                        boolean z5 = isCmcEnabled;
                        i3++;
                        z = true;
                    }
                    boolean z6 = isCmcEnabled;
                    if (z2) {
                        Log.d(LOG_TAG, "onImsSwitchUpdated: Configuring " + next.getName() + " module.");
                        next.onConfigured(i2);
                    }
                    if (z3) {
                        Log.d(LOG_TAG, "onImsSwitchUpdated: Stopping " + next.getName() + " module.");
                        next.stop();
                    }
                    isCmcEnabled = z6;
                }
            }
            IRegistrationManager registrationManager = this.mImsFramework.getRegistrationManager();
            ArrayList<ServiceModuleBase> arrayList = new ArrayList<>();
            for (ServiceModuleBase next3 : this.mServiceModules) {
                for (String str2 : next3.getServicesRequiring()) {
                    Integer asInteger3 = imsSwitchValue.getAsInteger(str2);
                    if ((next3.isStopped() || next3.isReady()) && asInteger3 != null && asInteger3.intValue() == 1 && DmConfigHelper.readBool(this.mContext, str2, Boolean.TRUE, i2).booleanValue()) {
                        Log.d(LOG_TAG, "Starting " + next3.getName() + " module");
                        next3.start();
                        arrayList.add(next3);
                    }
                }
            }
            Integer asInteger4 = imsSwitchValue.getAsInteger(DeviceConfigManager.RCS);
            if ((this.mAutoConfigCompletedList.containsKey(Integer.valueOf(i)) && this.mAutoConfigCompletedList.get(Integer.valueOf(i)).booleanValue()) || !((asInteger4 == null || asInteger4.intValue() == 1) && i2 == SimUtil.getSimSlotPriority())) {
                for (ServiceModuleBase onConfigured : arrayList) {
                    onConfigured.onConfigured(i2);
                }
            }
            ContentValues contentValues3 = this.mLastImsServiceSwitches.get(Integer.valueOf(i));
            if (contentValues3 != null) {
                for (ServiceModuleBase next4 : this.mServiceModules) {
                    ArraySet arraySet = new ArraySet();
                    String[] servicesRequiring2 = next4.getServicesRequiring();
                    int length2 = servicesRequiring2.length;
                    int i4 = 0;
                    while (i4 < length2) {
                        String str3 = servicesRequiring2[i4];
                        Integer asInteger5 = imsSwitchValue.getAsInteger(str3);
                        Integer asInteger6 = contentValues3.getAsInteger(str3);
                        if (asInteger5 != null) {
                            if (asInteger6 != null) {
                                contentValues = contentValues3;
                                if ((asInteger5.intValue() == 1) != (asInteger6.intValue() == 1)) {
                                    arraySet.add(str3);
                                }
                                i4++;
                                contentValues3 = contentValues;
                            }
                        }
                        contentValues = contentValues3;
                        Log.d(LOG_TAG, "Unknown switch value : " + str3);
                        i4++;
                        contentValues3 = contentValues;
                    }
                    ContentValues contentValues4 = contentValues3;
                    if (!arraySet.isEmpty()) {
                        Log.d(LOG_TAG, "onImsSwitchUpdated: switchedServices " + arraySet);
                        next4.onServiceSwitched(i2, imsSwitchValue);
                    }
                    contentValues3 = contentValues4;
                }
            }
            this.mLastImsServiceSwitches.put(Integer.valueOf(i), imsSwitchValue);
            for (ServiceModuleBase serviceModuleBase : arrayList) {
                serviceModuleBase.onNetworkChanged(registrationManager.getNetworkEvent(i2), i2);
                for (ImsRegistration imsRegistration : registrationManager.getRegistrationInfo()) {
                    if (needRegistrationNotification(serviceModuleBase, imsRegistration.getServices())) {
                        serviceModuleBase.onRegistered(imsRegistration);
                    }
                }
            }
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    public void forceCallOnServiceSwitched(int i) {
        ContentValues imsSwitchValue = DmConfigHelper.getImsSwitchValue(this.mContext, (String[]) null, i);
        if (this.mLastImsServiceSwitches.get(Integer.valueOf(i)) != null) {
            for (ServiceModuleBase next : this.mServiceModules) {
                for (String str : next.getServicesRequiring()) {
                    if (imsSwitchValue.getAsInteger(str) == null) {
                        Log.d(LOG_TAG, "Unknown switch value : " + str);
                    } else {
                        next.onServiceSwitched(i, imsSwitchValue);
                    }
                }
            }
        }
    }

    private void onSimReady(int i) {
        IMSLog.d(LOG_TAG, i, "ServiceModuleManager : onSimReady");
        for (ServiceModuleBase next : this.mServiceModules) {
            if (next.isRunning()) {
                next.onSimReady(i);
            }
        }
    }

    public void handleMessage(Message message) {
        Log.d(LOG_TAG, "handleMessage: evt=" + message.what);
        int i = message.what;
        if (i == 1) {
            onImsSwitchUpdated(UriUtil.getSimSlotFromUri((Uri) message.obj));
        } else if (i == 2) {
            notifyConfigured(true, message.arg1);
        } else if (i == 3) {
            Log.d(LOG_TAG, "ON SIM READY");
            onSimReady(message.arg1);
        }
    }

    public void cleanUpModules() {
        this.mChangingServiceModulesStateLock.lock();
        try {
            for (ServiceModuleBase next : this.mServiceModules) {
                if (next.isRunning()) {
                    next.cleanUp();
                }
            }
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    public void updateCapabilities(int i) {
        MmTelFeature.MmTelCapabilities mmTelCapabilities = new MmTelFeature.MmTelCapabilities(0);
        RcsFeature.RcsImsCapabilities rcsImsCapabilities = new RcsFeature.RcsImsCapabilities(0);
        for (ServiceModuleBase next : this.mServiceModules) {
            if (next == this.mCapabilityDiscoveryModule) {
                rcsImsCapabilities.addCapabilities(next.queryCapabilityStatus(i).getMask());
            } else {
                mmTelCapabilities.addCapabilities(next.queryCapabilityStatus(i).getMask());
            }
        }
        Log.d(LOG_TAG, "updateCapabilities to mmTelcapabilities = " + mmTelCapabilities + ", rcsCapabilities = " + rcsImsCapabilities);
        SecImsNotifier.getInstance().updateMmTelCapabilities(i, mmTelCapabilities);
        SecImsNotifier.getInstance().updateRcsCapabilities(i, rcsImsCapabilities);
    }

    private class SimEventListener implements ISimEventListener {
        private SimEventListener() {
        }

        public void onReady(int i, boolean z) {
            int simState = TelephonyManagerWrapper.getInstance(ServiceModuleManager.this.mContext).getSimState(i);
            Log.d(ServiceModuleManager.LOG_TAG, "onReady: phoneId=" + i + " absent=" + z + "SIM state=" + simState);
            if (simState == 5) {
                ServiceModuleManager serviceModuleManager = ServiceModuleManager.this;
                serviceModuleManager.sendMessage(serviceModuleManager.obtainMessage(3, i, 0, (Object) null));
            }
        }
    }

    private List<String> getExtendedServices(int i) {
        String string = ImsRegistry.getString(i, GlobalSettingsConstants.Registration.EXTENDED_SERVICES, "");
        ArrayList arrayList = new ArrayList();
        if (string != null) {
            arrayList.addAll(Arrays.asList(string.split(",")));
        }
        return arrayList;
    }

    private boolean isStartRequired(ServiceModuleBase serviceModuleBase, int i, ISimManager iSimManager) {
        String[] servicesRequiring = serviceModuleBase.getServicesRequiring();
        int length = servicesRequiring.length;
        int i2 = 0;
        while (i2 < length) {
            String str = servicesRequiring[i2];
            boolean readSwitch = DmConfigHelper.readSwitch(this.mContext, str, true, i);
            if (str.equalsIgnoreCase("mmtel") && iSimManager != null && iSimManager.getSimMno() == Mno.SPRINT) {
                readSwitch |= DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_VWF_ENABLED, Boolean.FALSE, i).booleanValue();
            }
            if (DmConfigHelper.getImsSwitchValue(this.mContext, Arrays.asList(ImsProfile.getRcsServiceList()).contains(str) ? DeviceConfigManager.RCS_SWITCH : str, i) != 1 || !readSwitch || serviceModuleBase.isRunning()) {
                IMSLog.i(LOG_TAG, i, "isStartRequired: ImsSwitch not enabled for service: " + str + ", isDmOn: " + readSwitch);
                if (str.contains("mdmi")) {
                    return true;
                }
                i2++;
            } else {
                Log.d(LOG_TAG, "isStartRequired: start " + serviceModuleBase.getName() + " module");
                return true;
            }
        }
        return false;
    }

    private List<ServiceModuleBase> getRcsServiceModules(ImsProfile imsProfile, int i) {
        VideoShareModule videoShareModule;
        ImageShareModule imageShareModule;
        EucModule eucModule;
        GlsModule glsModule;
        IMSLog.i(LOG_TAG, i, "getRcsServiceModules is called");
        CopyOnWriteArrayList<ServiceModuleBase> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        this.mChangingServiceModulesStateLock.lock();
        try {
            boolean z = true;
            if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS_SWITCH, i) != 1) {
                z = false;
            }
            if (z && (imsProfile.hasService("options") || imsProfile.hasService(SipMsg.EVENT_PRESENCE))) {
                ImModule imModule = this.mImModule;
                if (imModule != null && this.mServiceModules.contains(imModule)) {
                    copyOnWriteArrayList.add(this.mImModule);
                }
                SessionModule sessionModule = this.mSessionModule;
                if (sessionModule != null && this.mServiceModules.contains(sessionModule)) {
                    copyOnWriteArrayList.add(this.mSessionModule);
                }
                if (imsProfile.hasService("gls") && (glsModule = this.mGlsModule) != null && this.mServiceModules.contains(glsModule)) {
                    copyOnWriteArrayList.add(this.mGlsModule);
                }
                if (imsProfile.hasService("euc") && (eucModule = this.mEucModule) != null && this.mServiceModules.contains(eucModule)) {
                    copyOnWriteArrayList.add(this.mEucModule);
                }
                if (imsProfile.hasService("is") && (imageShareModule = this.mImageShareModule) != null && this.mServiceModules.contains(imageShareModule)) {
                    copyOnWriteArrayList.add(this.mImageShareModule);
                }
                if (imsProfile.hasService("vs") && (videoShareModule = this.mVideoShareModule) != null && this.mServiceModules.contains(videoShareModule)) {
                    copyOnWriteArrayList.add(this.mVideoShareModule);
                }
                OptionsModule optionsModule = this.mOptionsModule;
                if (optionsModule != null && this.mServiceModules.contains(optionsModule)) {
                    copyOnWriteArrayList.add(this.mOptionsModule);
                }
                PresenceModule presenceModule = this.mPresenceModule;
                if (presenceModule != null && this.mServiceModules.contains(presenceModule)) {
                    copyOnWriteArrayList.add(this.mPresenceModule);
                }
                CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscoveryModule;
                if (capabilityDiscoveryModule != null && this.mServiceModules.contains(capabilityDiscoveryModule)) {
                    copyOnWriteArrayList.add(this.mCapabilityDiscoveryModule);
                }
                TapiServiceManager tapiServiceManager = this.mTapiServiceManager;
                if (tapiServiceManager != null && this.mServiceModules.contains(tapiServiceManager)) {
                    copyOnWriteArrayList.add(this.mTapiServiceManager);
                }
                for (ServiceModuleBase serviceModuleBase : copyOnWriteArrayList) {
                    if (!serviceModuleBase.isReady() && !serviceModuleBase.isRunning()) {
                        serviceModuleBase.init();
                    }
                }
            }
            return copyOnWriteArrayList;
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    public void checkRcsServiceModules(List<IRegisterTask> list, int i) {
        IMSLog.i(LOG_TAG, i, "checkRcsServiceModules is called");
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscoveryModule;
        if (capabilityDiscoveryModule != null && !capabilityDiscoveryModule.isRunning()) {
            for (IRegisterTask next : list) {
                if (ImsProfile.hasRcsService(next.getProfile())) {
                    startRcsServiceModules(getRcsServiceModules(next.getProfile(), i), i);
                }
            }
        }
        PresenceModule presenceModule = this.mPresenceModule;
        if (presenceModule != null && !presenceModule.isRunning()) {
            IMSLog.i(LOG_TAG, i, "PresenceModule is not running");
            if (isStartRequired(this.mPresenceModule, i, (ISimManager) null)) {
                Log.d(LOG_TAG, "isStartRequired: true ");
                this.mPresenceModule.start();
                this.mPresenceModule.onConfigured(i);
            }
        }
    }
}
