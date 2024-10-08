package com.sec.internal.ims.servicemodules.presence;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SemSystemProperties;
import android.telephony.ims.stub.RcsCapabilityExchangeImplBase;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.presence.ServiceTuple;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceNotifyInfo;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.constants.ims.servicemodules.presence.PublishResponse;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.StringGenerator;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.presence.PresenceSubscriptionController;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityEventListener;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityExchangeControl;
import com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PresenceModule extends ServiceModuleBase implements IPresenceModule, ICapabilityExchangeControl {
    private static final int DEFAULT_WAKE_LOCK_TIMEOUT = 5000;
    private static final String LOG_TAG = "PresenceModule";
    static final String NAME = PresenceModule.class.getSimpleName();
    ICapabilityDiscoveryModule mCapabilityDiscovery = null;
    Context mContext = null;
    private SimpleEventLog mEventLog;
    ICapabilityEventListener mListener = null;
    protected Handler mModuleHandler = null;
    private int mPhoneCount = 0;
    private PresenceCacheController mPresenceCacheController;
    private PhoneIdKeyMap<PresenceConfig> mPresenceConfig;
    private PhoneIdKeyMap<PresenceModuleInfo> mPresenceModuleInfo;
    private Map<Integer, Boolean> mPresenceRegiInfoUpdater = new HashMap();
    private PresenceSharedPrefHelper mPresenceSp;
    protected PresenceUpdate mPresenceUpdate;
    private final RegistrantList mPublishRegistrants = new RegistrantList();
    IPresenceStackInterface mService;
    private List<ServiceTuple> mServiceTupleList = new ArrayList();
    private Map<String, PendingIntent> mSubscribeRetryList = new HashMap();
    private PhoneIdKeyMap<UriGenerator> mUriGenerator;
    private Map<Integer, Set<ImsUri>> mUrisToSubscribe = new HashMap();
    PowerManager.WakeLock mWakeLock;

    public void handleIntent(Intent intent) {
    }

    public boolean sendOptionsRequest(ImsUri imsUri, boolean z, Set<String> set, int i) {
        return false;
    }

    static class PresenceModuleInfo {
        long mBackupPublishTimestamp = -1;
        PendingIntent mBadEventIntent;
        boolean mBadEventProgress = false;
        boolean mFirstPublish = true;
        long mLastBadEventTimestamp = -1;
        long mLastPublishTimestamp = -1;
        PresenceResponse.PresenceStatusCode mLastSubscribeStatusCode = PresenceResponse.PresenceStatusCode.NONE;
        boolean mLimitImmediateRetry;
        boolean mLimitReRegistration;
        Mno mMno = Mno.DEFAULT;
        PresenceResponse.PresenceFailureReason mOldPublishError;
        boolean mOwnInfoPublished;
        PresenceInfo mOwnPresenceInfo;
        boolean mParalysed;
        PendingIntent mPollingIntent;
        PresenceCache mPresenceCache;
        int mPublishExpBackOffRetryCount;
        int mPublishNoResponseCount;
        boolean mPublishNotFoundProgress = false;
        int mPublishNotProvisionedCount;
        int mPublishRequestTimeout;
        ImsRegistration mRegInfo;
        boolean mRequestPublishToAosp = false;
        PendingIntent mRetryPublishIntent;
        ISimManager mSimCardManager;
        boolean ongoingPublishErrRetry;

        PresenceModuleInfo() {
        }
    }

    public PresenceModule(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
        this.mEventLog = new SimpleEventLog(context, NAME, 20);
        this.mPhoneCount = SimManagerFactory.getAllSimManagers().size();
        this.mPresenceSp = new PresenceSharedPrefHelper(this.mContext, this);
        this.mPresenceCacheController = new PresenceCacheController(this);
        this.mPresenceUpdate = new PresenceUpdate(this);
        this.mPresenceModuleInfo = new PhoneIdKeyMap<>(this.mPhoneCount, new PresenceModuleInfo());
        this.mPresenceConfig = new PhoneIdKeyMap<>(this.mPhoneCount, null);
        this.mUriGenerator = new PhoneIdKeyMap<>(this.mPhoneCount, null);
        PresenceIntentReceiver presenceIntentReceiver = new PresenceIntentReceiver(this);
        this.mContext.registerReceiver(PresenceIntentReceiver.mIntentReceiver, presenceIntentReceiver.getIntentFilter());
        this.mContext.registerReceiver(PresenceIntentReceiver.mSubscribeRetryIntentReceiver, presenceIntentReceiver.getSubscribeRetryIntentFilter());
        for (int i = 0; i < this.mPhoneCount; i++) {
            PresenceModuleInfo presenceModuleInfo = new PresenceModuleInfo();
            presenceModuleInfo.mSimCardManager = SimManagerFactory.getSimManagerFromSimSlot(i);
            presenceModuleInfo.mOwnPresenceInfo = new PresenceInfo(i);
            presenceModuleInfo.mPresenceCache = new PresenceCache(this.mContext, i);
            this.mPresenceModuleInfo.put(i, presenceModuleInfo);
            this.mPresenceConfig.put(i, new PresenceConfig(this.mContext, i));
            this.mUrisToSubscribe.put(Integer.valueOf(i), new HashSet());
            this.mUriGenerator.put(i, UriGeneratorFactory.getInstance().get(i, UriGenerator.URIServiceType.RCS_URI));
        }
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        if (powerManager != null) {
            PowerManager.WakeLock newWakeLock = powerManager.newWakeLock(1, LOG_TAG);
            this.mWakeLock = newWakeLock;
            newWakeLock.setReferenceCounted(false);
        }
        Log.i(LOG_TAG, "created");
    }

    public String[] getServicesRequiring() {
        return new String[]{SipMsg.EVENT_PRESENCE};
    }

    public void onServiceSwitched(int i, ContentValues contentValues) {
        IMSLog.i(LOG_TAG, i, "onServiceSwitched:");
        updateFeatures(i);
    }

    public void init() {
        super.init();
        Log.i(LOG_TAG, "init");
        IPresenceStackInterface presenceHandler = ImsRegistry.getHandlerFactory().getPresenceHandler();
        this.mService = presenceHandler;
        presenceHandler.registerForPresenceInfo(this, 10, (Object) null);
        this.mService.registerForWatcherInfo(this, 12, (Object) null);
        this.mService.registerForPublishFailure(this, 2, (Object) null);
        this.mService.registerForPresenceNotifyInfo(this, 16, (Object) null);
        this.mService.registerForPresenceNotifyStatus(this, 17, (Object) null);
        this.mCapabilityDiscovery = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mPresenceModuleInfo.get(i).mLastPublishTimestamp = this.mPresenceSp.loadPublishTimestamp(i);
            this.mPresenceModuleInfo.get(i).mLastBadEventTimestamp = this.mPresenceSp.loadBadEventTimestamp(i);
        }
        HandlerThread handlerThread = new HandlerThread(LOG_TAG);
        handlerThread.start();
        this.mModuleHandler = new Handler(handlerThread.getLooper());
    }

    public void start() {
        super.start();
        Log.i(LOG_TAG, "start:");
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mPresenceModuleInfo.get(i).mPublishNotProvisionedCount = 0;
        }
    }

    public void stop() {
        super.stop();
        Log.i(LOG_TAG, "stop:");
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mPresenceModuleInfo.get(i).mOwnInfoPublished = false;
            this.mPresenceModuleInfo.get(i).mBackupPublishTimestamp = 0;
            this.mPresenceSp.savePublishTimestamp(0, i);
            stopPublishTimer(i);
            stopSubscribeRetryTimer(i);
            resetPublishErrorHandling(i);
            setParalysed(false, i);
            if (this.mPresenceModuleInfo.get(i).mRegInfo != null) {
                sendMessage(obtainMessage(3, Integer.valueOf(i)));
            }
        }
    }

    public void onConfigured(int i) {
        IMSLog.i(LOG_TAG, i, "onConfigured:");
        processConfigured(i);
    }

    private void processConfigured(int i) {
        this.mModuleHandler.post(new PresenceModule$$ExternalSyntheticLambda1(this, i));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$processConfigured$0(int i) {
        ImsProfile imsProfile = ImsRegistry.getRegistrationManager().getImsProfile(i, ImsProfile.PROFILE_TYPE.RCS);
        if (imsProfile == null || !imsProfile.hasService(SipMsg.EVENT_PRESENCE)) {
            IMSLog.i(LOG_TAG, i, "processConfigured: no Presence support.");
            return;
        }
        PresenceModuleInfo presenceModuleInfo = this.mPresenceModuleInfo.get(i);
        presenceModuleInfo.mMno = presenceModuleInfo.mSimCardManager.getSimMno();
        IMSLog.i(LOG_TAG, i, "onConfigured: mno = " + presenceModuleInfo.mMno);
        readConfig(i);
        updateFeatures(i);
        this.mPresenceSp.checkAndClearPresencePreferences(presenceModuleInfo.mSimCardManager.getImsi(), i);
    }

    private void updateFeatures(int i) {
        this.mEnabledFeatures[i] = 0;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, SipMsg.EVENT_PRESENCE, i) == 1 && this.mPresenceConfig.get(i).getDefaultDisc() != 2) {
            this.mEnabledFeatures[i] = (long) Capabilities.FEATURE_PRESENCE_DISCOVERY;
            if (this.mPresenceConfig.get(i).isSocialPresenceSupport()) {
                long[] jArr = this.mEnabledFeatures;
                jArr[i] = jArr[i] | ((long) Capabilities.FEATURE_SOCIAL_PRESENCE);
            }
        }
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        super.onRegistered(imsRegistration);
        Log.i(LOG_TAG, "onRegistered:");
        processRegistered(imsRegistration);
    }

    private void processRegistered(ImsRegistration imsRegistration) {
        this.mModuleHandler.post(new PresenceModule$$ExternalSyntheticLambda2(this, imsRegistration));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$processRegistered$1(ImsRegistration imsRegistration) {
        int phoneId = imsRegistration.getPhoneId();
        PresenceModuleInfo presenceModuleInfo = this.mPresenceModuleInfo.get(phoneId);
        presenceModuleInfo.mMno = presenceModuleInfo.mSimCardManager.getSimMno();
        IMSLog.i(LOG_TAG, phoneId, "processRegistered: mno = " + presenceModuleInfo.mMno);
        readConfig(phoneId);
        ImsProfile imsProfile = imsRegistration.getImsProfile();
        if (presenceModuleInfo.mRegInfo == null) {
            presenceModuleInfo.mOwnPresenceInfo.setPublishGzipEnabled(imsProfile.isPublishGzipEnabled());
        }
        presenceModuleInfo.mRegInfo = imsRegistration;
        this.mPresenceRegiInfoUpdater.put(Integer.valueOf(imsRegistration.getPhoneId()), Boolean.TRUE);
        IMSLog.i(LOG_TAG, phoneId, "processRegistered: profile " + imsProfile.getName());
        List impuList = imsRegistration.getImpuList();
        if (impuList == null || impuList.isEmpty()) {
            IMSLog.e(LOG_TAG, phoneId, "processRegistered: impus is empty !!!");
            return;
        }
        this.mUriGenerator.put(phoneId, UriGeneratorFactory.getInstance().get(((NameAddr) impuList.get(0)).getUri(), UriGenerator.URIServiceType.RCS_URI));
        if (RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.USE_SIPURI_FOR_URIGENERATOR)) {
            for (NameAddr nameAddr : imsRegistration.getImpuList()) {
                if (nameAddr.getUri().getUriType() == ImsUri.UriType.SIP_URI) {
                    this.mUriGenerator.put(phoneId, UriGeneratorFactory.getInstance().get(nameAddr.getUri(), UriGenerator.URIServiceType.RCS_URI));
                    return;
                }
            }
        }
    }

    public void onDeregistering(ImsRegistration imsRegistration) {
        Log.i(LOG_TAG, "onDeregistering:");
        processDeregistering(imsRegistration);
    }

    private void processDeregistering(ImsRegistration imsRegistration) {
        this.mModuleHandler.post(new PresenceModule$$ExternalSyntheticLambda4(this, imsRegistration));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$processDeregistering$2(ImsRegistration imsRegistration) {
        if (imsRegistration != null) {
            Log.i(LOG_TAG, "processDeregistering:");
            if (!imsRegistration.getImsProfile().hasEmergencySupport()) {
                removeMessages(1, Integer.valueOf(imsRegistration.getPhoneId()));
                removeMessages(15, Integer.valueOf(imsRegistration.getPhoneId()));
                if (isRunning()) {
                    unpublish(imsRegistration.getPhoneId());
                }
            }
        }
    }

    public void onDeregistered(ImsRegistration imsRegistration, int i) {
        super.onDeregistered(imsRegistration, i);
        Log.i(LOG_TAG, "onDeregistered:");
        processDeregistered(imsRegistration);
    }

    private void processDeregistered(ImsRegistration imsRegistration) {
        this.mModuleHandler.post(new PresenceModule$$ExternalSyntheticLambda0(this, imsRegistration));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$processDeregistered$3(ImsRegistration imsRegistration) {
        ImsProfile imsProfile = imsRegistration.getImsProfile();
        int phoneId = imsRegistration.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "processDeregistered: profile " + imsProfile.getName());
        removeMessages(1, Integer.valueOf(phoneId));
        removeMessages(10, Integer.valueOf(phoneId));
        removeMessages(12, Integer.valueOf(phoneId));
        removeMessages(11, Integer.valueOf(phoneId));
        removeMessages(9, Integer.valueOf(phoneId));
        removeMessages(16, Integer.valueOf(phoneId));
        removeMessages(17, Integer.valueOf(phoneId));
        removeMessages(15, Integer.valueOf(phoneId));
        setParalysed(false, phoneId);
        PresenceModuleInfo presenceModuleInfo = this.mPresenceModuleInfo.get(phoneId);
        presenceModuleInfo.mRegInfo = null;
        if (presenceModuleInfo.mMno == Mno.TMOUS) {
            presenceModuleInfo.mOwnPresenceInfo.setPublishGzipEnabled(imsProfile.isPublishGzipEnabled());
        }
        this.mPresenceRegiInfoUpdater.put(Integer.valueOf(imsRegistration.getPhoneId()), Boolean.FALSE);
        this.mUriGenerator.put(phoneId, null);
        this.mPresenceModuleInfo.get(phoneId).mOwnInfoPublished = false;
        resetPublishErrorHandling(phoneId);
        PresenceUtil.notifyPublishCommandError(phoneId, this.mContext, 9);
    }

    public void onSimChanged(int i) {
        IMSLog.i(LOG_TAG, i, "onSimChanged:");
        this.mPresenceCacheController.clearPresenceInfo(i);
        setBadEventProgress(false, i);
        this.mPresenceSp.saveBadEventTimestamp(0, i);
        setPublishNotFoundProgress(false, i);
    }

    /* access modifiers changed from: package-private */
    public void onDefaultSmsPackageChanged() {
        Log.i(LOG_TAG, "onDefaultSmsPackageChanged");
        for (int i = 0; i < this.mPhoneCount; i++) {
            if (!getBadEventProgress(i)) {
                setPublishNotFoundProgress(false, i);
            }
        }
    }

    public boolean isReadyToRequest(int i) {
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
        if (rcsStrategy != null) {
            return rcsStrategy.isPresenceReadyToRequest(this.mPresenceModuleInfo.get(i).mOwnInfoPublished, getParalysed(i));
        }
        IMSLog.i(LOG_TAG, i, "isReadyToRequest: mnoStrategy null");
        return false;
    }

    public void setOwnCapabilities(long j, int i) {
        String msisdn;
        if (isRunning()) {
            IMSLog.i(LOG_TAG, i, "setOwnCapabilities: features " + Capabilities.dumpFeature(j));
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.add(i, "OwnCapabilities - set, features = " + j);
            IMSLog.c(LogClass.PM_SET_OWNCAPA, i + ",SET:" + j);
            PresenceInfo presenceInfo = new PresenceInfo(i);
            IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
            if (rcsStrategy != null) {
                rcsStrategy.changeServiceDescription();
            }
            PresenceModuleInfo presenceModuleInfo = this.mPresenceModuleInfo.get(i);
            List<ServiceTuple> serviceTupleList = ServiceTuple.getServiceTupleList(j);
            if (presenceModuleInfo.mMno == Mno.TMOUS && ImsUtil.getComposerAuthValue(i, this.mContext) == 0) {
                IMSLog.d(LOG_TAG, i, "remove MmtelCallComposer tuple");
                serviceTupleList.removeIf(new PresenceModule$$ExternalSyntheticLambda3());
            }
            for (ServiceTuple serviceTuple : serviceTupleList) {
                IMSLog.i(LOG_TAG, i, "setOwnCapabilities: " + serviceTuple);
                ServiceTuple serviceTuple2 = presenceModuleInfo.mOwnPresenceInfo.getServiceTuple(serviceTuple.serviceId);
                if (serviceTuple2 != null) {
                    serviceTuple.tupleId = serviceTuple2.tupleId;
                } else if (presenceModuleInfo.mMno.isKor()) {
                    String loadRandomTupleId = this.mPresenceSp.loadRandomTupleId(serviceTuple.feature, i);
                    if (loadRandomTupleId != null) {
                        serviceTuple.tupleId = loadRandomTupleId;
                    } else {
                        String generateString = StringGenerator.generateString(5, 10);
                        serviceTuple.tupleId = generateString;
                        this.mPresenceSp.saveRandomTupleId(serviceTuple.feature, generateString, i);
                    }
                } else {
                    serviceTuple.tupleId = StringGenerator.generateString(5, 10);
                }
            }
            presenceInfo.setPhoneId(i);
            presenceInfo.addService(serviceTupleList);
            presenceInfo.setPublishGzipEnabled(presenceModuleInfo.mOwnPresenceInfo.getPublishGzipEnabled());
            presenceModuleInfo.mOwnPresenceInfo = presenceInfo;
            buildPresenceInfoForThirdParty(i);
            ImsRegistration imsRegistration = presenceModuleInfo.mRegInfo;
            if (imsRegistration != null) {
                ImsUri uri = ((NameAddr) imsRegistration.getImpuList().get(0)).getUri();
                if (presenceModuleInfo.mMno == Mno.ATT) {
                    Iterator it = presenceModuleInfo.mRegInfo.getImpuList().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        NameAddr nameAddr = (NameAddr) it.next();
                        if (nameAddr.getUri().getUriType() == ImsUri.UriType.TEL_URI && (msisdn = nameAddr.getUri().getMsisdn()) != null && !msisdn.equals(SimManagerFactory.getImsiFromPhoneId(i))) {
                            Log.i(LOG_TAG, "getPreferredImpu: Found MDN TEL URI");
                            uri = nameAddr.getUri();
                            break;
                        }
                    }
                }
                presenceModuleInfo.mOwnPresenceInfo.setUri(uri.toString());
                IMSLog.s(LOG_TAG, i, "setOwnCapabilities: uri" + presenceModuleInfo.mOwnPresenceInfo.getUri());
                if (getParalysed(i)) {
                    IMSLog.i(LOG_TAG, i, "setOwnCapabilities: paralysed");
                    return;
                }
                if (presenceModuleInfo.mRetryPublishIntent != null) {
                    if (!presenceModuleInfo.ongoingPublishErrRetry) {
                        IMSLog.i(LOG_TAG, i, "setOwnCapabilities: retry timer is running");
                        return;
                    }
                    this.mPresenceConfig.get(i).setPublishErrRetry((long) presenceModuleInfo.mRegInfo.getImsProfile().getPublishErrRetryTimer());
                    IMSLog.i(LOG_TAG, i, "initialize PublishErrRetry: " + this.mPresenceConfig.get(i).getPublishErrRetry());
                }
                if (presenceModuleInfo.mMno == Mno.VZW) {
                    sendMessageDelayed(obtainMessage(1, Integer.valueOf(i)), 500);
                } else {
                    sendMessage(obtainMessage(1, Integer.valueOf(i)));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$setOwnCapabilities$4(ServiceTuple serviceTuple) {
        return serviceTuple.description == "MmtelCallComposer";
    }

    public void registerCapabilityEventListener(ICapabilityEventListener iCapabilityEventListener) {
        this.mListener = iCapabilityEventListener;
    }

    public int requestCapabilityExchange(Set<ImsUri> set, CapabilityConstants.RequestType requestType, int i, int i2) {
        int i3;
        IMSLog.i(LOG_TAG, i, "requestCapabilityExchange: list requestType " + requestType);
        if (!isReadyToRequest(i)) {
            IMSLog.e(LOG_TAG, i, "requestCapabilityExchange: PUBLISH is not completed. bail.");
            return 0;
        } else if (!checkModuleReady(i)) {
            return 0;
        } else {
            Set set2 = this.mUrisToSubscribe.get(Integer.valueOf(i));
            synchronized (set2) {
                if (this.mPresenceConfig.get(i).getMaxUri() - set2.size() < set.size()) {
                    i3 = this.mPresenceConfig.get(i).getMaxUri() - set2.size();
                    Iterator<ImsUri> it = set.iterator();
                    for (int i4 = 0; i4 < i3 && it.hasNext(); i4++) {
                        set2.add(it.next());
                    }
                    set.removeAll(set2);
                } else {
                    set2.addAll(set);
                    i3 = set.size();
                    set.clear();
                }
                this.mPresenceCacheController.loadPresenceStorage(set2, i);
            }
            acquireWakeLock();
            sendMessage(obtainMessage(7, i, i2, requestType));
            return i3;
        }
    }

    public boolean requestCapabilityExchange(ImsUri imsUri, ICapabilityExchangeControl.ICapabilityExchangeCallback iCapabilityExchangeCallback, CapabilityConstants.RequestType requestType, boolean z, long j, int i, String str, int i2) {
        IMSLog.s(LOG_TAG, i, "requestCapabilityExchange: uri " + imsUri);
        IMSLog.i(LOG_TAG, i, "requestCapabilityExchange: requestType " + requestType + ", isAlwaysForce: " + z);
        if (!isReadyToRequest(i)) {
            IMSLog.e(LOG_TAG, i, "requestCapabilityExchange: PUBLISH is not completed. bail.");
            return false;
        }
        boolean isKor = this.mPresenceModuleInfo.get(i).mMno.isKor();
        if (this.mPresenceConfig.get(i).getRlsUri() == null || this.mPresenceConfig.get(i).getRlsUri().getScheme() == null || isKor) {
            sendMessage(obtainMessage(5, new PresenceSubscriptionController.SubscriptionRequest(imsUri, requestType, z, i, i2)));
        } else {
            IMSLog.i(LOG_TAG, i, "requestCapabilityExchange: adding uri to RCS list");
        }
        if (iCapabilityExchangeCallback == null) {
            return true;
        }
        iCapabilityExchangeCallback.onComplete((Capabilities) null);
        return true;
    }

    private void startPublishTimer(int i) {
        if (this.mPresenceModuleInfo.get(i).mPollingIntent != null) {
            IMSLog.e(LOG_TAG, i, "startPublishTimer: PublishTimer is already running. Stopping it.");
            stopPublishTimer(i);
        }
        long publishTimer = this.mPresenceConfig.get(i).getPublishTimer();
        if (PresenceUtil.getExtendedPublishTimerCond(i, this.mPresenceModuleInfo.get(i).mOwnPresenceInfo.getServiceList())) {
            publishTimer = this.mPresenceConfig.get(i).getPublishTimerExtended();
        }
        IMSLog.i(LOG_TAG, i, "startPublishTimer: PublishTimer " + publishTimer + " sec");
        Intent intent = new Intent("com.sec.internal.ims.servicemodules.presence.publish_timeout");
        intent.putExtra("sim_slot_id", i);
        intent.setPackage(this.mContext.getPackageName());
        this.mPresenceModuleInfo.get(i).mPollingIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
        AlarmTimer.start(this.mContext, this.mPresenceModuleInfo.get(i).mPollingIntent, publishTimer * 1000);
    }

    private void stopPublishTimer(int i) {
        PendingIntent pendingIntent = this.mPresenceModuleInfo.get(i).mPollingIntent;
        if (pendingIntent == null) {
            IMSLog.e(LOG_TAG, i, "stopPublishTimer: PublishTimer is not running.");
            return;
        }
        IMSLog.i(LOG_TAG, i, "stopPublishTimer:");
        AlarmTimer.stop(this.mContext, pendingIntent);
        this.mPresenceModuleInfo.get(i).mPollingIntent = null;
    }

    public PresenceInfo getOwnPresenceInfo(int i) {
        IMSLog.i(LOG_TAG, i, "getOwnPresenceInfo");
        return this.mPresenceModuleInfo.get(i).mOwnPresenceInfo;
    }

    public PresenceInfo getPresenceInfo(ImsUri imsUri, int i) {
        return this.mPresenceCacheController.getPresenceInfo(imsUri, i);
    }

    public PresenceInfo getPresenceInfoByContactId(String str, int i) {
        return this.mPresenceCacheController.getPresenceInfoByContactId(str, this.mCapabilityDiscovery.getPhonebook().getNumberlistByContactId(str), i);
    }

    public void subscribe(PresenceSubscriptionController.SubscriptionRequest subscriptionRequest, boolean z) {
        int i = subscriptionRequest.phoneId;
        IMSLog.s(LOG_TAG, i, "subscribe: uri " + subscriptionRequest.uri);
        IMSLog.i(LOG_TAG, i, "subscribe: request type " + subscriptionRequest.type);
        if (checkModuleReady(i)) {
            PresenceSubscription subscription = PresenceSubscriptionController.getSubscription(subscriptionRequest.uri, true, i);
            if (subscription == null) {
                subscription = new PresenceSubscription(StringIdGenerator.generateSubscriptionId());
                subscription.addUri(subscriptionRequest.uri);
                subscription.setRequestType(subscriptionRequest.type);
                subscription.setPhoneId(i);
                PresenceSubscriptionController.addSubscription(subscription);
            } else {
                CapabilityConstants.RequestType requestType = subscriptionRequest.type;
                if (!RcsPolicyManager.getRcsStrategy(i).isSubscribeThrottled(subscription, this.mPresenceConfig.get(i).getSourceThrottleSubscribe() * 1000, requestType == CapabilityConstants.RequestType.REQUEST_TYPE_NONE || requestType == CapabilityConstants.RequestType.REQUEST_TYPE_LAZY, subscriptionRequest.isAlwaysForce) || subscriptionRequest.type == CapabilityConstants.RequestType.REQUEST_TYPE_SR_API) {
                    subscription.updateState(0);
                    subscription.updateTimestamp();
                    subscription.setRequestType(subscriptionRequest.type);
                } else {
                    IMSLog.i(LOG_TAG, i, "subscribe: single fetch has been already sent");
                    IMSLog.s(LOG_TAG, i, "subscribe: throttled uri " + subscriptionRequest.uri);
                    return;
                }
            }
            PresenceSubscription presenceSubscription = subscription;
            long calSubscribeDelayTime = RcsPolicyManager.getRcsStrategy(i).calSubscribeDelayTime(presenceSubscription);
            if (calSubscribeDelayTime <= 0 || subscriptionRequest.type == CapabilityConstants.RequestType.REQUEST_TYPE_SR_API) {
                if (subscriptionRequest.type == CapabilityConstants.RequestType.REQUEST_TYPE_LAZY) {
                    PresenceSubscriptionController.addLazySubscription(this.mUriGenerator.get(i).normalize(subscriptionRequest.uri));
                }
                IMSLog.i(LOG_TAG, i, "subscribe internalRequestId : " + subscriptionRequest.internalRequestId);
                int i2 = subscriptionRequest.internalRequestId;
                if (i2 != 0) {
                    PresenceUtil.replaceSubscribeResponseCbSubsId(i2, presenceSubscription.getSubscriptionId());
                }
                int i3 = i;
                this.mService.subscribe(PresenceUtil.convertUriType(subscriptionRequest.uri, this.mPresenceConfig.get(i).useSipUri(), getPresenceInfo(subscriptionRequest.uri, i), this.mPresenceModuleInfo.get(i).mMno, this.mUriGenerator.get(i), i3), z, obtainMessage(6, presenceSubscription), presenceSubscription.getSubscriptionId(), i3);
                return;
            }
            IMSLog.i(LOG_TAG, i, "subscribe: delayed for " + calSubscribeDelayTime);
            presenceSubscription.updateState(5);
            sendMessageDelayed(obtainMessage(5, subscriptionRequest), calSubscribeDelayTime);
        }
    }

    private void subscribe(Set<ImsUri> set, boolean z, CapabilityConstants.RequestType requestType, int i, int i2) {
        Set<ImsUri> set2 = set;
        CapabilityConstants.RequestType requestType2 = requestType;
        int i3 = i;
        int i4 = i2;
        IMSLog.s(LOG_TAG, i3, "subscribe: uri list " + set2);
        IMSLog.i(LOG_TAG, i3, "subscribe: request type " + requestType2);
        if (requestType2 == CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC) {
            removeMessages(8);
            stopSubscribeRetryTimer(i3);
        }
        if (checkModuleReady(i3)) {
            PresenceModuleInfo presenceModuleInfo = this.mPresenceModuleInfo.get(i3);
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            for (ImsUri next : set) {
                if (PresenceSubscriptionController.hasSubscription(next)) {
                    IMSLog.i(LOG_TAG, i3, "subscribe: subscription has been already sent");
                    IMSLog.s(LOG_TAG, i3, "subscribe: subscribed uri " + next);
                    arrayList2.add(next);
                } else {
                    if (RcsPolicyManager.getRcsStrategy(i).boolSetting(RcsPolicySettings.RcsPolicy.LIST_SUB_URI_TRANSLATION)) {
                        next = PresenceUtil.convertUriType(next, this.mPresenceConfig.get(i3).useSipUri(), getPresenceInfo(next, i3), presenceModuleInfo.mMno, this.mUriGenerator.get(i3), i);
                    }
                    arrayList.add(next);
                }
            }
            if (arrayList2.size() > 0) {
                set2.removeAll(arrayList2);
            }
            if (arrayList.size() == 0) {
                IMSLog.i(LOG_TAG, i3, "subscribe: no URI to subscribe.");
                return;
            }
            PresenceSubscription presenceSubscription = new PresenceSubscription(StringIdGenerator.generateSubscriptionId());
            presenceSubscription.addUriAll(set2);
            presenceSubscription.setExpiry(PresenceUtil.getPollListSubExp(this.mContext, i3));
            presenceSubscription.setRequestType(requestType2);
            presenceSubscription.setSingleFetch(false);
            presenceSubscription.setPhoneId(i3);
            if (presenceModuleInfo.mMno == Mno.TMOUS) {
                presenceSubscription.addDropUriAll(set2);
            }
            set.clear();
            PresenceSubscriptionController.addSubscription(presenceSubscription);
            IMSLog.i(LOG_TAG, i3, "subscribe internalRequestId : " + i4);
            if (i4 != 0) {
                PresenceUtil.replaceSubscribeResponseCbSubsId(i4, presenceSubscription.getSubscriptionId());
            }
            ImsRegistration imsRegistration = presenceModuleInfo.mRegInfo;
            if (imsRegistration != null) {
                ImsProfile imsProfile = imsRegistration.getImsProfile();
                this.mService.subscribeList(arrayList, z, obtainMessage(6, presenceSubscription), presenceSubscription.getSubscriptionId(), imsProfile.isGzipEnabled(), presenceSubscription.getExpiry(), i);
            }
        }
    }

    public void addPublishResponseCallback(int i, RcsCapabilityExchangeImplBase.PublishResponseCallback publishResponseCallback) {
        PresenceUtil.addPublishResponseCallback(i, publishResponseCallback);
    }

    public void publish(PresenceInfo presenceInfo, int i) {
        publish(presenceInfo, i, (String) null);
    }

    /* access modifiers changed from: package-private */
    public void publish(PresenceInfo presenceInfo, int i, String str) {
        PresenceInfo presenceInfo2 = presenceInfo;
        int i2 = i;
        String str2 = str;
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
        if (!isRunning() || rcsStrategy == null || this.mPresenceModuleInfo.get(i2).mRegInfo == null) {
            IMSLog.i(LOG_TAG, i2, "publish: not ready to publish");
            if (this.mPresenceModuleInfo.get(i2).mRetryPublishIntent != null) {
                stopRetryPublishTimer(i2);
            }
            PresenceUtil.notifyPublishCommandError(i2, this.mContext, 9);
            return;
        }
        IMSLog.s(LOG_TAG, i2, "publish: " + presenceInfo2);
        removeMessages(1, Integer.valueOf(i));
        if (!TextUtils.isEmpty(str)) {
            removeMessages(15, Integer.valueOf(i));
        }
        stopPublishTimer(i2);
        stopRetryPublishTimer(i2);
        long calThrottledPublishRetryDelayTime = rcsStrategy.calThrottledPublishRetryDelayTime(this.mPresenceModuleInfo.get(i2).mLastPublishTimestamp, this.mPresenceConfig.get(i2).getSourceThrottlePublish());
        if (calThrottledPublishRetryDelayTime <= 0) {
            Date date = new Date();
            if (this.mPresenceModuleInfo.get(i2).mFirstPublish && this.mPresenceConfig.get(i2).getBadEventExpiry() != 0) {
                long badEventExpiry = (this.mPresenceModuleInfo.get(i2).mLastBadEventTimestamp + (this.mPresenceConfig.get(i2).getBadEventExpiry() * 1000)) - date.getTime();
                if (badEventExpiry > 0) {
                    IMSLog.i(LOG_TAG, i2, "publish: restart BadEventTimer");
                    startBadEventTimer(badEventExpiry, false, i2);
                }
            }
            long isTdelay = rcsStrategy.isTdelay(this.mPresenceConfig.get(i2).getTdelayPublish());
            if (isTdelay != 0) {
                IMSLog.i(LOG_TAG, i2, "publish: retry after " + isTdelay + "ms");
                if (!TextUtils.isEmpty(str)) {
                    sendMessageDelayed(obtainMessage(15, i2, 0, str2), isTdelay);
                } else {
                    sendMessageDelayed(obtainMessage(1, Integer.valueOf(i)), isTdelay);
                }
            } else if (!RcsUtils.isImsSingleRegiRequired(this.mContext, i2) || !ConfigUtil.isGoogDmaPackageInuse(this.mContext, i2) || !RcsUtils.isSrRcsPresenceEnabled(this.mContext, i2) || !TextUtils.isEmpty(str)) {
                if (this.mPresenceSp.checkIfValidEtag(i2)) {
                    IMSLog.i(LOG_TAG, i2, "valid etag, setting to " + this.mPresenceSp.getPublishETag(i2));
                    presenceInfo2.setEtag(this.mPresenceSp.getPublishETag(i2));
                } else {
                    IMSLog.i(LOG_TAG, i2, "not valid etag");
                    presenceInfo2.setEtag((String) null);
                }
                if (PresenceUtil.getExtendedPublishTimerCond(i2, this.mPresenceModuleInfo.get(i2).mOwnPresenceInfo.getServiceList())) {
                    this.mPresenceModuleInfo.get(i2).mOwnPresenceInfo.setExtendedTimerFlag(true);
                    presenceInfo2.setExpireTime(Math.max(presenceInfo.getMinExpires(), this.mPresenceConfig.get(i2).getPublishTimerExtended()));
                    presenceInfo2.setExtendedTimerFlag(true);
                } else {
                    this.mPresenceModuleInfo.get(i2).mOwnPresenceInfo.setExtendedTimerFlag(false);
                    presenceInfo2.setExpireTime(Math.max(presenceInfo.getMinExpires(), this.mPresenceConfig.get(i2).getPublishTimer()));
                    presenceInfo2.setExtendedTimerFlag(false);
                }
                if (!TextUtils.isEmpty(str)) {
                    IMSLog.i(LOG_TAG, i2, "publish: set pidfXml");
                    IMSLog.s(LOG_TAG, i2, "publish: pidfXml from AOSP = " + str2);
                    presenceInfo2.setPidf(str2);
                }
                ServiceTuple.getServiceTuple((long) Capabilities.FEATURE_CHAT_CPM);
                if (!this.mPresenceModuleInfo.get(i2).mMno.isKor() || this.mPresenceSp.loadDisplayText(i2) == null) {
                    IMSLog.i(LOG_TAG, i2, "not valid displaytext");
                } else {
                    IMSLog.i(LOG_TAG, i2, "displaytext exist");
                    ServiceTuple.setDisplayText((long) Capabilities.FEATURE_CHAT_CPM, this.mPresenceSp.loadDisplayText(i2));
                }
                acquireWakeLock();
                setServiceVersion(i2);
                this.mService.publish(presenceInfo2, obtainMessage(2, presenceInfo2), i2);
                if (this.mPresenceModuleInfo.get(i2).mFirstPublish) {
                    this.mPresenceModuleInfo.get(i2).mFirstPublish = false;
                }
                this.mEventLog.add(i2, "Publish - sent");
                IMSLog.c(LogClass.PM_PUB, i2 + ",PUB-SENT");
            } else if (!this.mPresenceModuleInfo.get(i2).mOwnInfoPublished || !this.mPresenceModuleInfo.get(i2).mRequestPublishToAosp) {
                IMSLog.i(LOG_TAG, i2, "publish: call onRequestPublishCapabilities, return");
                this.mEventLog.add(i2, "Publish - call onRequestPublishCapabilities");
                this.mCapabilityDiscovery.removePublishedServiceList(i2);
                SecImsNotifier.getInstance().onRequestPublishCapabilities(i2, 1);
                this.mPresenceModuleInfo.get(i2).mRequestPublishToAosp = true;
            } else {
                IMSLog.i(LOG_TAG, i2, "publish: already published, return");
            }
        } else if (!TextUtils.isEmpty(str)) {
            sendMessageDelayed(obtainMessage(15, i2, 0, str2), calThrottledPublishRetryDelayTime);
        } else {
            sendMessageDelayed(obtainMessage(1, Integer.valueOf(i)), calThrottledPublishRetryDelayTime);
        }
    }

    public void unpublish(int i) {
        IMSLog.i(LOG_TAG, i, "unpublish: ");
        stopPublishTimer(i);
        stopRetryPublishTimer(i);
        removeMessages(3, Integer.valueOf(i));
        ImsRegistration imsRegistration = this.mPresenceModuleInfo.get(i).mRegInfo;
        PresenceModuleInfo presenceModuleInfo = this.mPresenceModuleInfo.get(i);
        if (imsRegistration != null && !PresenceUtil.isRegProhibited(imsRegistration, i)) {
            this.mService.unpublish(i);
        }
        if (presenceModuleInfo.mMno.isKor()) {
            IMSLog.i(LOG_TAG, i, "unpublish: remain etag for Kor");
            long j = presenceModuleInfo.mLastPublishTimestamp;
            if (j > 0) {
                presenceModuleInfo.mBackupPublishTimestamp = j;
            }
        } else if (presenceModuleInfo.mMno != Mno.ATT) {
            this.mPresenceSp.resetPublishEtag(i);
        } else if (imsRegistration != null) {
            if (!ImsRegistry.getRegistrationManager().isPdnConnected(imsRegistration.getImsProfile(), i)) {
                IMSLog.i(LOG_TAG, i, "unpublish: PDN already disconnected");
                long j2 = presenceModuleInfo.mLastPublishTimestamp;
                if (j2 > 0) {
                    presenceModuleInfo.mBackupPublishTimestamp = j2;
                }
            } else {
                this.mPresenceSp.resetPublishEtag(i);
            }
        }
        if (presenceModuleInfo.mOwnInfoPublished) {
            this.mEventLog.add(i, "UnPublish");
            IMSLog.c(LogClass.PM_UNPUB, i + ",UNPUB");
        }
        presenceModuleInfo.mOwnInfoPublished = false;
        if (!presenceModuleInfo.mMno.isKor()) {
            this.mPresenceSp.savePublishTimestamp(0, i);
        }
        SecImsNotifier.getInstance().onUnPublish(i);
    }

    /* access modifiers changed from: package-private */
    public PresenceModuleInfo getPresenceModuleInfo(int i) {
        return this.mPresenceModuleInfo.get(i);
    }

    public PresenceConfig getPresenceConfig(int i) {
        return this.mPresenceConfig.get(i);
    }

    /* access modifiers changed from: package-private */
    public UriGenerator getUriGenerator(int i) {
        return this.mUriGenerator.get(i);
    }

    public void setParalysed(boolean z, int i) {
        IMSLog.i(LOG_TAG, i, "mParalysed: " + z);
        this.mPresenceModuleInfo.get(i).mParalysed = z;
    }

    public boolean getParalysed(int i) {
        return this.mPresenceModuleInfo.get(i).mParalysed;
    }

    public int getPublishTimer(int i) {
        int publishTimerValue = isProvisionedValueAvailable(i) ? (int) getPublishTimerValue(i) : 0;
        IMSLog.i(LOG_TAG, i, "getPublishTimer: " + publishTimerValue);
        return publishTimerValue;
    }

    public int getPublishExpiry(int i) {
        int max = isProvisionedValueAvailable(i) ? (int) Math.max(getOwnPresenceInfo(i).getMinExpires(), getPublishTimerValue(i)) : 0;
        IMSLog.i(LOG_TAG, i, "getPublishExpiry: " + max);
        return max;
    }

    public int getPublishSourceThrottle(int i) {
        int sourceThrottlePublish = isProvisionedValueAvailable(i) ? ((int) getPresenceConfig(i).getSourceThrottlePublish()) * 1000 : 0;
        IMSLog.i(LOG_TAG, i, "getPublishSourceThrottle: " + sourceThrottlePublish);
        return sourceThrottlePublish;
    }

    public int getListSubMaxUri(int i) {
        int maxUri = isProvisionedValueAvailable(i) ? getPresenceConfig(i).getMaxUri() : 0;
        IMSLog.i(LOG_TAG, i, "getListSubMaxUri: " + maxUri);
        return maxUri;
    }

    public int getListSubExpiry(int i) {
        int pollListSubExp = isProvisionedValueAvailable(i) ? PresenceUtil.getPollListSubExp(this.mContext, i) : 0;
        IMSLog.i(LOG_TAG, i, "getListSubExpiry: " + pollListSubExp);
        return pollListSubExp;
    }

    public void setDisplayText(int i, String str) {
        IMSLog.i(LOG_TAG, i, "setDisplayText: " + IMSLog.checker(str));
        if (!str.equals(this.mPresenceSp.loadDisplayText(i))) {
            this.mPresenceSp.saveDisplayText(str, i);
            sendMessageDelayed(obtainMessage(1, Integer.valueOf(i)), 100);
            return;
        }
        IMSLog.i(LOG_TAG, i, "skip setDisplayText");
    }

    public int isListSubGzipEnabled(int i) {
        int i2 = 0;
        if (isProvisionedValueAvailable(i) && getPresenceConfig(i).isGzipEnabled()) {
            i2 = 1;
        }
        IMSLog.i(LOG_TAG, i, "isListSubGzipEnabled: " + i2);
        return i2;
    }

    public boolean isOwnPresenceInfoHasTuple(int i, long j) {
        return getOwnPresenceInfo(i).getServiceTuple(ServiceTuple.getServiceTuple(j).serviceId) != null;
    }

    private long getPublishTimerValue(int i) {
        return PresenceUtil.getExtendedPublishTimerCond(i, getOwnPresenceInfo(i).getServiceList()) ? getPresenceConfig(i).getPublishTimerExtended() : getPresenceConfig(i).getPublishTimer();
    }

    private boolean isProvisionedValueAvailable(int i) {
        boolean z = RcsUtils.isImsSingleRegiRequired(this.mContext, i) && RcsUtils.isSrRcsPresenceEnabled(this.mContext, i) && isRunning() && RcsPolicyManager.getRcsStrategy(i) != null && getOwnPresenceInfo(i) != null && getPresenceConfig(i) != null;
        IMSLog.i(LOG_TAG, i, "isProvisionedValueAvailable: " + z);
        return z;
    }

    private void notifyProvisionedValue(int i) {
        if (isProvisionedValueAvailable(i)) {
            IMSLog.i(LOG_TAG, i, "notifyProvisionedValue");
            SecImsNotifier.getInstance().notifyProvisionedIntValueChanged(i, 15, (int) getPublishTimerValue(i));
            SecImsNotifier.getInstance().notifyProvisionedIntValueChanged(i, 16, (int) Math.max(getOwnPresenceInfo(i).getMinExpires(), getPublishTimerValue(i)));
            SecImsNotifier.getInstance().notifyProvisionedIntValueChanged(i, 21, ((int) getPresenceConfig(i).getSourceThrottlePublish()) * 1000);
            SecImsNotifier.getInstance().notifyProvisionedIntValueChanged(i, 22, getPresenceConfig(i).getMaxUri());
            SecImsNotifier.getInstance().notifyProvisionedIntValueChanged(i, 23, PresenceUtil.getPollListSubExp(this.mContext, i));
            SecImsNotifier.getInstance().notifyProvisionedIntValueChanged(i, 24, getPresenceConfig(i).isGzipEnabled() ? 1 : 0);
        }
    }

    /* access modifiers changed from: package-private */
    public void onNewNotifyInfo(PresenceNotifyInfo presenceNotifyInfo, int i) {
        IMSLog.i(LOG_TAG, i, "onNewNotifyInfo:");
        if (checkModuleReady(i) && presenceNotifyInfo != null && RcsUtils.isImsSingleRegiRequired(this.mContext, i)) {
            PresenceSubscription subscription = PresenceSubscriptionController.getSubscription(presenceNotifyInfo.getSubscriptionId(), i);
            if (subscription == null) {
                IMSLog.e(LOG_TAG, i, "onNewNotifyInfo: no subscription");
                return;
            }
            PresenceUtil.onSubscribeNotifyCapabilitiesUpdate(presenceNotifyInfo.getSubscriptionId(), this.mContext, i, presenceNotifyInfo.getPidfXmls());
            if (!subscription.isSingleFetch() && presenceNotifyInfo.getUriTerminatedReason() != null && presenceNotifyInfo.getUriTerminatedReason().size() > 0) {
                PresenceUtil.onSubscribeResourceTerminated(presenceNotifyInfo.getSubscriptionId(), this.mContext, i, presenceNotifyInfo.getUriTerminatedReason());
            }
            if ("terminated".equals(presenceNotifyInfo.getSubscriptionState())) {
                PresenceUtil.onSubscribeTerminated(presenceNotifyInfo.getSubscriptionId(), this.mContext, i, presenceNotifyInfo.getSubscriptionStateReason(), 0);
            }
            if (subscription.isSingleFetch()) {
                PresenceUtil.removeSubscribeResponseCallback(presenceNotifyInfo.getSubscriptionId());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onNewNotifyStatus(PresenceResponse presenceResponse, int i) {
        IMSLog.i(LOG_TAG, "onNewNotifyStatus:");
        if (checkModuleReady(i) && presenceResponse != null && RcsUtils.isImsSingleRegiRequired(this.mContext, i)) {
            PresenceSubscription subscription = PresenceSubscriptionController.getSubscription(presenceResponse.getSubscribeId(), i);
            if (subscription == null) {
                IMSLog.e(LOG_TAG, i, "onNewNotifyStatus: no subscription");
                return;
            }
            PresenceUtil.onSubscribeTerminated(presenceResponse.getSubscribeId(), this.mContext, i, presenceResponse.getSubscribeTerminatedReason(), 0);
            if (subscription.isSingleFetch()) {
                PresenceUtil.removeSubscribeResponseCallback(presenceResponse.getSubscribeId());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updatePresenceDatabase(List<ImsUri> list, PresenceInfo presenceInfo, int i) {
        this.mPresenceCacheController.updatePresenceDatabase(list, presenceInfo, this.mCapabilityDiscovery, this.mUriGenerator.get(i), i);
    }

    /* access modifiers changed from: package-private */
    public void onPublishComplete(PresenceResponse presenceResponse, int i) {
        boolean z;
        ICapabilityEventListener iCapabilityEventListener;
        if (presenceResponse == null) {
            IMSLog.i(LOG_TAG, i, "onPublishComplete: response is null");
            return;
        }
        IMSLog.i(LOG_TAG, i, "onPublishComplete: success " + presenceResponse.isSuccess());
        this.mEventLog.add(i, "Publish - completed, response = " + presenceResponse.getSipError());
        IMSLog.c(LogClass.PM_ONPUB_COMP, i + "," + presenceResponse.getSipError());
        PresenceModuleInfo presenceModuleInfo = this.mPresenceModuleInfo.get(i);
        clearWakeLock();
        if (presenceResponse.isSuccess()) {
            presenceModuleInfo.mOwnInfoPublished = true;
            stopBadEventTimer(i);
            setParalysed(false, i);
            resetPublishErrorHandling(i);
            if (presenceResponse instanceof PublishResponse) {
                PublishResponse publishResponse = (PublishResponse) presenceResponse;
                IMSLog.i(LOG_TAG, i, "getEtag:" + publishResponse.getEtag() + " getExpiresTimer:" + publishResponse.getExpiresTimer());
                this.mPresenceSp.savePublishETag(publishResponse.getEtag(), publishResponse.getExpiresTimer(), i);
                z = publishResponse.isRefresh();
                this.mPresenceSp.savePublishTimestamp(System.currentTimeMillis(), i);
                IMSLog.i(LOG_TAG, i, "onPublishComplete(), isRefresh : " + z);
            } else {
                z = false;
            }
            presenceModuleInfo.mPublishNotProvisionedCount = 0;
            presenceModuleInfo.mPublishExpBackOffRetryCount = 0;
            presenceModuleInfo.mPublishRequestTimeout = 0;
            presenceModuleInfo.mPublishNoResponseCount = 0;
            if (presenceModuleInfo.mMno == Mno.VZW) {
                this.mPublishRegistrants.notifyResult(Boolean.FALSE);
                ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 1);
            }
            if (RcsPolicyManager.getRcsStrategy(i).needUnpublish(i)) {
                sendMessage(obtainMessage(3, Integer.valueOf(i)));
            } else {
                if (!z && (iCapabilityEventListener = this.mListener) != null) {
                    iCapabilityEventListener.onMediaReady(presenceResponse.isSuccess(), true, i);
                }
                if (!PresenceSubscriptionController.getPendingSubscription().isEmpty()) {
                    IMSLog.i(LOG_TAG, i, "onPublishComplete, pending subscription");
                    for (PresenceSubscription obtainMessage : PresenceSubscriptionController.getPendingSubscription()) {
                        sendMessage(obtainMessage(8, obtainMessage));
                    }
                    PresenceSubscriptionController.clearPendingSubscription();
                }
            }
            if (!ImsProfile.isRcsUpProfile(this.mPresenceConfig.get(i).getRcsProfile()) || presenceModuleInfo.mMno == Mno.VZW) {
                IMSLog.i(LOG_TAG, i, "onPublishComplete,start PublishTimer: " + this.mPresenceConfig.get(i).getPublishTimer());
                startPublishTimer(i);
            }
        } else if (presenceResponse instanceof PublishResponse) {
            onPublishFailed((PublishResponse) presenceResponse, i);
        }
        if (this.mListener != null) {
            onEABPublishComplete(presenceResponse);
        }
        if (presenceResponse.getSipError() == 708) {
            PresenceUtil.notifyPublishCommandError(i, this.mContext, 4);
        } else {
            PresenceUtil.notifyPublishNetworkResponse(i, this.mContext, presenceResponse.getSipError(), presenceResponse.getErrorDescription());
        }
        if ((presenceResponse instanceof PublishResponse) && ((PublishResponse) presenceResponse).isRefresh()) {
            SecImsNotifier.getInstance().onPublishUpdated(i, presenceResponse.getSipError(), presenceResponse.getErrorDescription(), 0, (String) null);
        }
        ContentValues contentValues = new ContentValues();
        if (presenceResponse.isSuccess()) {
            contentValues.put(DiagnosisConstants.DRCS_KEY_RCPC, 1);
        } else {
            contentValues.put(DiagnosisConstants.DRCS_KEY_RCPF, 1);
        }
        contentValues.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
        ImsLogAgentUtil.storeLogToAgent(i, this.mContext, DiagnosisConstants.FEATURE_DRCS, contentValues);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0103, code lost:
        if (onPublishRequireFull(r1, r4, r11) == false) goto L_0x0138;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onPublishFailed(com.sec.internal.constants.ims.servicemodules.presence.PublishResponse r10, int r11) {
        /*
            r9 = this;
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r0 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r11)
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo> r1 = r9.mPresenceModuleInfo
            java.lang.Object r1 = r1.get(r11)
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r1 = (com.sec.internal.ims.servicemodules.presence.PresenceModule.PresenceModuleInfo) r1
            r2 = 0
            r1.mOwnInfoPublished = r2
            java.lang.String r3 = "PresenceModule"
            if (r0 != 0) goto L_0x0019
            java.lang.String r9 = "onPublishFailed: mnoStrategy is null."
            com.sec.internal.log.IMSLog.e(r3, r11, r9)
            return
        L_0x0019:
            com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceFailureReason r4 = r10.getReason()
            r5 = 1
            com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r0 = r0.handlePresenceFailure(r4, r5)
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "onPublishFailed - statusCode: "
            r6.append(r7)
            r6.append(r0)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.i(r3, r11, r6)
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            r6.append(r11)
            java.lang.String r7 = ","
            r6.append(r7)
            int r8 = r0.ordinal()
            r6.append(r8)
            r6.append(r7)
            int r8 = r4.ordinal()
            r6.append(r8)
            r6.append(r7)
            int r8 = r10.getSipError()
            r6.append(r8)
            r6.append(r7)
            java.lang.String r7 = r10.getErrorDescription()
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            r7 = 302579716(0x12090004, float:4.3229597E-28)
            com.sec.internal.log.IMSLog.c(r7, r6)
            com.sec.internal.ims.servicemodules.presence.PresenceModule$PresenceModuleInfo r1 = r9.initPublishFailedInfos(r1, r0)
            com.sec.internal.constants.Mno r6 = r1.mMno
            boolean r6 = r6.isKor()
            if (r6 == 0) goto L_0x0088
            com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceFailureReason r6 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceFailureReason.CONDITIONAL_REQUEST_FAILED
            if (r4 == r6) goto L_0x0088
            java.lang.String r6 = "onPublishFailed - remain etag for Kor"
            com.sec.internal.log.IMSLog.i(r3, r11, r6)
            goto L_0x008d
        L_0x0088:
            com.sec.internal.ims.servicemodules.presence.PresenceSharedPrefHelper r6 = r9.mPresenceSp
            r6.resetPublishEtag(r11)
        L_0x008d:
            com.sec.internal.constants.Mno r6 = r1.mMno
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.TMOUS
            if (r6 != r7) goto L_0x0098
            com.sec.ims.presence.PresenceInfo r6 = r1.mOwnPresenceInfo
            r6.setPublishGzipEnabled(r2)
        L_0x0098:
            int[] r2 = com.sec.internal.ims.servicemodules.presence.PresenceModule.AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode
            int r6 = r0.ordinal()
            r2 = r2[r6]
            r6 = 1000(0x3e8, double:4.94E-321)
            switch(r2) {
                case 1: goto L_0x0135;
                case 2: goto L_0x012f;
                case 3: goto L_0x0126;
                case 4: goto L_0x0122;
                case 5: goto L_0x00ff;
                case 6: goto L_0x0106;
                case 7: goto L_0x00eb;
                case 8: goto L_0x00e3;
                case 9: goto L_0x00db;
                case 10: goto L_0x00d2;
                case 11: goto L_0x00bc;
                case 12: goto L_0x00bc;
                case 13: goto L_0x00ae;
                case 14: goto L_0x00a7;
                default: goto L_0x00a5;
            }
        L_0x00a5:
            goto L_0x0138
        L_0x00a7:
            java.lang.String r2 = "onPublishFailed: need to perform IMS re-registration"
            com.sec.internal.log.IMSLog.e(r3, r11, r2)
            goto L_0x0138
        L_0x00ae:
            java.lang.String r2 = "onPublishFailed: PRESENCE_REQUIRE_RETRY_PUBLISH_AFTER"
            com.sec.internal.log.IMSLog.e(r3, r11, r2)
            long r2 = r10.getRetryAfter()
            r9.onPublishRetryAfter(r1, r2, r11)
            goto L_0x0138
        L_0x00bc:
            java.lang.String r2 = "onPublishFailed: PRESENCE_REQUIRE_RETRY_PUBLISH"
            com.sec.internal.log.IMSLog.e(r3, r11, r2)
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceConfig> r2 = r9.mPresenceConfig
            java.lang.Object r2 = r2.get(r11)
            com.sec.internal.ims.servicemodules.presence.PresenceConfig r2 = (com.sec.internal.ims.servicemodules.presence.PresenceConfig) r2
            long r2 = r2.getPublishTimer()
            long r2 = r2 * r6
            r9.startRetryPublishTimer(r2, r11)
            goto L_0x0138
        L_0x00d2:
            java.lang.String r2 = "onPublishFailed: vzw default case... "
            com.sec.internal.log.IMSLog.e(r3, r11, r2)
            r9.onPublishDisableMode(r11)
            goto L_0x0138
        L_0x00db:
            boolean r2 = r10.isRefresh()
            r9.onPublishNoResponse(r1, r2, r11)
            goto L_0x0138
        L_0x00e3:
            long r2 = r10.getRetryAfter()
            r9.onPublishRetryExpBackoff(r1, r2, r11)
            goto L_0x0138
        L_0x00eb:
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.ims.servicemodules.presence.PresenceConfig> r2 = r9.mPresenceConfig
            java.lang.Object r2 = r2.get(r11)
            com.sec.internal.ims.servicemodules.presence.PresenceConfig r2 = (com.sec.internal.ims.servicemodules.presence.PresenceConfig) r2
            long r2 = r2.getBadEventExpiry()
            long r2 = r2 * r6
            r9.startBadEventTimer(r2, r5, r11)
            r9.setParalysed(r5, r11)
            goto L_0x0138
        L_0x00ff:
            boolean r2 = r9.onPublishRequireFull(r1, r4, r11)
            if (r2 != 0) goto L_0x0106
            goto L_0x0138
        L_0x0106:
            int r2 = r10.getRetryTime()
            if (r2 <= 0) goto L_0x0116
            com.sec.ims.presence.PresenceInfo r2 = r1.mOwnPresenceInfo
            int r3 = r10.getRetryTime()
            long r3 = (long) r3
            r2.setMinExpires(r3)
        L_0x0116:
            java.lang.Integer r2 = java.lang.Integer.valueOf(r11)
            android.os.Message r2 = r9.obtainMessage(r5, r2)
            r9.sendMessage(r2)
            goto L_0x0138
        L_0x0122:
            r9.onPublishRequestTimeout(r1, r11)
            goto L_0x0138
        L_0x0126:
            java.lang.String r2 = "onPublishFailed: PRESENCE_NOT_FOUND"
            com.sec.internal.log.IMSLog.e(r3, r11, r2)
            r9.setParalysed(r5, r11)
            goto L_0x0138
        L_0x012f:
            java.lang.String r2 = "onPublishFailed: PRESENCE_AT_NOT_REGISTERED"
            com.sec.internal.log.IMSLog.e(r3, r11, r2)
            goto L_0x0138
        L_0x0135:
            r9.onPublishNotProvisioned(r1, r11)
        L_0x0138:
            r9.notifyPublishError(r1, r0, r10, r11)
            android.content.Context r9 = r9.mContext
            int r0 = r10.getSipError()
            java.lang.String r10 = r10.getErrorDescription()
            com.sec.internal.ims.servicemodules.presence.PresenceUtil.sendRCSPPubInfoToHQM(r9, r0, r10, r11)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.presence.PresenceModule.onPublishFailed(com.sec.internal.constants.ims.servicemodules.presence.PublishResponse, int):void");
    }

    /* renamed from: com.sec.internal.ims.servicemodules.presence.PresenceModule$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode;

        /* JADX WARNING: Can't wrap try/catch for region: R(32:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|32) */
        /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0084 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0090 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x009c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x00a8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode[] r0 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode = r0
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_AT_NOT_PROVISIONED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_AT_NOT_REGISTERED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_NOT_FOUND     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_REQUEST_TIMEOUT     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_INTERVAL_TOO_SHORT     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_AT_BAD_EVENT     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_RETRY_EXP_BACKOFF     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_NO_RESPONSE     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_DISABLE_MODE     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_NO_SUBSCRIBE     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH_AFTER     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_RE_REGISTRATION     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse$PresenceStatusCode r1 = com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse.PresenceStatusCode.PRESENCE_FORBIDDEN     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.presence.PresenceModule.AnonymousClass1.<clinit>():void");
        }
    }

    private PresenceModuleInfo initPublishFailedInfos(PresenceModuleInfo presenceModuleInfo, PresenceResponse.PresenceStatusCode presenceStatusCode) {
        if (presenceStatusCode != PresenceResponse.PresenceStatusCode.PRESENCE_RETRY_EXP_BACKOFF) {
            presenceModuleInfo.mPublishExpBackOffRetryCount = 0;
        }
        if (presenceStatusCode != PresenceResponse.PresenceStatusCode.PRESENCE_REQUEST_TIMEOUT) {
            presenceModuleInfo.mPublishRequestTimeout = 0;
        }
        if (presenceStatusCode != PresenceResponse.PresenceStatusCode.PRESENCE_NO_RESPONSE) {
            presenceModuleInfo.mPublishNoResponseCount = 0;
        }
        return presenceModuleInfo;
    }

    private void onPublishRequestTimeout(PresenceModuleInfo presenceModuleInfo, int i) {
        presenceModuleInfo.mPublishRequestTimeout++;
        IMSLog.e(LOG_TAG, i, "onPublishRequestTimeout: PRESENCE_REQUEST_TIMEOUT count = " + presenceModuleInfo.mPublishRequestTimeout);
        long publishExpBackOffRetryTime = PresenceUtil.getPublishExpBackOffRetryTime(i, presenceModuleInfo.mPublishRequestTimeout);
        if (RcsPolicyManager.getRcsStrategy(i).needUnpublish(i)) {
            sendMessage(obtainMessage(3, Integer.valueOf(i)));
        } else if (publishExpBackOffRetryTime != 0) {
            startRetryPublishTimer(publishExpBackOffRetryTime * 1000, i);
        } else {
            IMSLog.e(LOG_TAG, i, "onPublishRequestTimeout: starting error retry ... ");
            if (this.mPresenceConfig.get(i).getPublishErrRetry() != 0) {
                startRetryPublishTimer(this.mPresenceConfig.get(i).getPublishErrRetry() * 1000, i);
                this.mPresenceConfig.get(i).setPublishErrRetry(0);
            }
        }
    }

    private void onPublishNoResponse(PresenceModuleInfo presenceModuleInfo, boolean z, int i) {
        if (!z) {
            presenceModuleInfo.mPublishNoResponseCount++;
            IMSLog.e(LOG_TAG, i, "onPublishNoResponse: count = " + presenceModuleInfo.mPublishNoResponseCount + ", isSVLTE: " + SemSystemProperties.getBoolean("ro.ril.svlte1x", false));
            long publishExpBackOffRetryTime = PresenceUtil.getPublishExpBackOffRetryTime(i, presenceModuleInfo.mPublishNoResponseCount);
            if (publishExpBackOffRetryTime != 0) {
                startRetryPublishTimer(publishExpBackOffRetryTime * 1000, i);
            } else {
                IMSLog.e(LOG_TAG, i, "onPublishNoResponse: retry time end for NoResponse... ");
            }
        }
    }

    private boolean onPublishRequireFull(PresenceModuleInfo presenceModuleInfo, PresenceResponse.PresenceFailureReason presenceFailureReason, int i) {
        Mno mno = presenceModuleInfo.mMno;
        if (mno == Mno.TMOUS || mno.isKor()) {
            IMSLog.i(LOG_TAG, i, "onPublishRequireFull: oldError = " + presenceModuleInfo.mOldPublishError + ", newError = " + presenceFailureReason);
            if (!presenceModuleInfo.mLimitImmediateRetry || presenceFailureReason == null || !presenceFailureReason.equals(presenceModuleInfo.mOldPublishError)) {
                presenceModuleInfo.mLimitImmediateRetry = true;
                presenceModuleInfo.mOldPublishError = presenceFailureReason;
            } else {
                IMSLog.i(LOG_TAG, i, "onPublishRequireFull: retry after publish timer expires");
                startRetryPublishTimer(this.mPresenceConfig.get(i).getPublishTimer() * 1000, i);
                return false;
            }
        }
        return true;
    }

    private void onPublishRetryAfter(PresenceModuleInfo presenceModuleInfo, long j, int i) {
        if (j > 0) {
            IMSLog.e(LOG_TAG, i, "onPublishRetryAfter: retry publish after " + j);
            startRetryPublishTimer(j * 1000, i);
        } else if (presenceModuleInfo.mMno == Mno.TMOUS) {
            startRetryPublishTimer((long) (((double) (this.mPresenceConfig.get(i).getPublishTimer() * 1000)) * 0.85d), i);
        } else {
            startRetryPublishTimer(this.mPresenceConfig.get(i).getPublishTimer() * 1000, i);
        }
    }

    private void onPublishRetryExpBackoff(PresenceModuleInfo presenceModuleInfo, long j, int i) {
        if (!presenceModuleInfo.mMno.isKor() || j <= 0) {
            presenceModuleInfo.mPublishExpBackOffRetryCount++;
            IMSLog.i(LOG_TAG, i, "onPublishRetryExpBackoff: count = " + presenceModuleInfo.mPublishExpBackOffRetryCount);
            long publishExpBackOffRetryTime = PresenceUtil.getPublishExpBackOffRetryTime(i, presenceModuleInfo.mPublishExpBackOffRetryCount);
            if (RcsPolicyManager.getRcsStrategy(i).needUnpublish(i)) {
                sendMessage(obtainMessage(3, Integer.valueOf(i)));
            } else if (publishExpBackOffRetryTime != 0) {
                startRetryPublishTimer(publishExpBackOffRetryTime * 1000, i);
                setPublishNotFoundProgress(true, i);
            } else if (presenceModuleInfo.mMno == Mno.ATT) {
                this.mEventLog.logAndAdd(i, "onPublishRetryExpBackoff: no more retry");
                setPublishNotFoundProgress(true, i);
            } else {
                IMSLog.e(LOG_TAG, i, "onPublishRetryExpBackoff: starting error retry ... ");
                if (this.mPresenceConfig.get(i).getPublishErrRetry() != 0) {
                    startRetryPublishTimer(this.mPresenceConfig.get(i).getPublishErrRetry() * 1000, i);
                    this.mPresenceConfig.get(i).setPublishErrRetry(0);
                }
            }
        } else {
            IMSLog.e(LOG_TAG, i, "onPublishRetryExpBackoff: Use retryAfter, Retry publish after " + j);
            startRetryPublishTimer(j * 1000, i);
            presenceModuleInfo.mPublishExpBackOffRetryCount = 0;
        }
    }

    private void onPublishNotProvisioned(PresenceModuleInfo presenceModuleInfo, int i) {
        presenceModuleInfo.mPublishNotProvisionedCount++;
        IMSLog.e(LOG_TAG, i, "onPublishNotProvisioned: NOT_PROVISIONED count = " + presenceModuleInfo.mPublishNotProvisionedCount);
        if (presenceModuleInfo.mMno == Mno.VZW) {
            this.mCapabilityDiscovery.clearCapabilitiesCache(i);
            PresenceUtil.triggerOmadmTreeSync(this.mContext, i);
            setParalysed(true, i);
            presenceModuleInfo.mPublishNotProvisionedCount = 0;
        }
    }

    private void onPublishDisableMode(int i) {
        if (RcsPolicyManager.getRcsStrategy(i).needUnpublish(i)) {
            sendMessage(obtainMessage(3, Integer.valueOf(i)));
        }
    }

    private void notifyPublishError(PresenceModuleInfo presenceModuleInfo, PresenceResponse.PresenceStatusCode presenceStatusCode, PublishResponse publishResponse, int i) {
        IRegistrationGovernor registrationGovernor;
        if (presenceModuleInfo.mRegInfo != null && (registrationGovernor = ImsRegistry.getRegistrationManager().getRegistrationGovernor(presenceModuleInfo.mRegInfo.getHandle())) != null) {
            if (presenceModuleInfo.mMno != Mno.TMOUS || presenceStatusCode != PresenceResponse.PresenceStatusCode.PRESENCE_RE_REGISTRATION) {
                registrationGovernor.onPublishError(new SipError(publishResponse.getSipError(), publishResponse.getErrorDescription()));
            } else if (!presenceModuleInfo.mLimitReRegistration) {
                registrationGovernor.onPublishError(SipErrorBase.FORBIDDEN);
                presenceModuleInfo.mLimitReRegistration = true;
            } else {
                IMSLog.i(LOG_TAG, i, "notifyPublishError: maintain last IMS registration");
                presenceModuleInfo.mLimitReRegistration = false;
            }
        }
    }

    private void startBadEventTimer(long j, boolean z, int i) {
        IMSLog.i(LOG_TAG, i, "startBadEventTimer: millis " + j);
        if (getBadEventProgress(i)) {
            IMSLog.i(LOG_TAG, i, "startBadEventTimer: BadEvent in progress");
            return;
        }
        if (this.mPresenceModuleInfo.get(i).mBadEventIntent != null) {
            stopBadEventTimer(i);
        }
        if (j > 0) {
            Intent intent = new Intent("com.sec.internal.ims.servicemodules.presence.bad_event_timeout");
            intent.putExtra("sim_slot_id", i);
            intent.addFlags(LogClass.SIM_EVENT);
            intent.setPackage(this.mContext.getPackageName());
            this.mPresenceModuleInfo.get(i).mBadEventIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
            AlarmTimer.start(this.mContext, this.mPresenceModuleInfo.get(i).mBadEventIntent, j);
            setBadEventProgress(true, i);
            if (z) {
                this.mPresenceSp.saveBadEventTimestamp(new Date().getTime(), i);
            }
        }
    }

    private void stopBadEventTimer(int i) {
        removeMessages(14, Integer.valueOf(i));
        if (this.mPresenceModuleInfo.get(i).mBadEventIntent == null) {
            IMSLog.e(LOG_TAG, i, "stopBadEventTimer: BadEventExitTimer is not running.");
            return;
        }
        IMSLog.i(LOG_TAG, i, "stopBadEventTimer");
        AlarmTimer.stop(this.mContext, this.mPresenceModuleInfo.get(i).mBadEventIntent);
        this.mPresenceModuleInfo.get(i).mBadEventIntent = null;
        setBadEventProgress(false, i);
        this.mPresenceSp.saveBadEventTimestamp(0, i);
    }

    /* access modifiers changed from: package-private */
    public void onBadEventTimeout(int i) {
        IMSLog.i(LOG_TAG, i, "onBadEventTimeout: ");
        if (this.mPresenceModuleInfo.get(i).mBadEventIntent != null) {
            stopBadEventTimer(i);
            setParalysed(false, i);
            sendMessage(obtainMessage(1, Integer.valueOf(i)));
        }
    }

    /* access modifiers changed from: package-private */
    public void onRetryPublishTimeout(int i) {
        IMSLog.i(LOG_TAG, i, "onRetryPublishTimeout");
        if (this.mPresenceModuleInfo.get(i).mRetryPublishIntent != null) {
            stopRetryPublishTimer(i);
            if (this.mPresenceModuleInfo.get(i).mMno == Mno.ATT) {
                setParalysed(false, i);
            }
            sendMessage(obtainMessage(1, Integer.valueOf(i)));
        }
    }

    private void startRetryPublishTimer(long j, int i) {
        IMSLog.i(LOG_TAG, i, "startRetryPublishTimer: millis " + j);
        stopPublishTimer(i);
        if (this.mPresenceModuleInfo.get(i).mRetryPublishIntent != null) {
            stopRetryPublishTimer(i);
        }
        if (j > 0) {
            this.mPresenceModuleInfo.get(i).ongoingPublishErrRetry = j == this.mPresenceConfig.get(i).getPublishErrRetry() * 1000;
            Intent intent = new Intent("com.sec.internal.ims.servicemodules.presence.retry_publish");
            intent.putExtra("sim_slot_id", i);
            intent.setPackage(this.mContext.getPackageName());
            this.mPresenceModuleInfo.get(i).mRetryPublishIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
            AlarmTimer.start(this.mContext, this.mPresenceModuleInfo.get(i).mRetryPublishIntent, j);
            if (!this.mPresenceModuleInfo.get(i).mMno.isKor()) {
                this.mPresenceSp.savePublishTimestamp(0, i);
            }
        }
    }

    private void stopRetryPublishTimer(int i) {
        if (this.mPresenceModuleInfo.get(i).mRetryPublishIntent == null) {
            IMSLog.e(LOG_TAG, i, "stopRetryPublishTimer: mRetryPublishIntent is null.");
            return;
        }
        IMSLog.i(LOG_TAG, i, "stopRetryPublishTimer");
        AlarmTimer.stop(this.mContext, this.mPresenceModuleInfo.get(i).mRetryPublishIntent);
        this.mPresenceModuleInfo.get(i).mRetryPublishIntent = null;
    }

    private void startSubscribeRetryTimer(long j, String str, int i) {
        Log.i(LOG_TAG, "startSubscribeRetryTimer: millis " + j + ", subscriptionId " + str);
        PendingIntent pendingIntent = this.mSubscribeRetryList.get(str);
        if (pendingIntent != null) {
            AlarmTimer.stop(this.mContext, pendingIntent);
            this.mSubscribeRetryList.remove(str);
        }
        Intent intent = new Intent("com.sec.internal.ims.servicemodules.presence.retry_subscribe");
        Uri parse = Uri.parse("urn:subscriptionid:" + str);
        intent.setPackage(this.mContext.getPackageName());
        intent.setData(parse);
        intent.putExtra("KEY_SUBSCRIPTION_ID", str);
        intent.putExtra("KEY_PHONE_ID", i);
        PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
        AlarmTimer.start(this.mContext, broadcast, j);
        this.mSubscribeRetryList.put(str, broadcast);
    }

    private void stopSubscribeRetryTimer(int i) {
        Iterator<String> it = this.mSubscribeRetryList.keySet().iterator();
        while (it.hasNext()) {
            String next = it.next();
            PresenceSubscription subscription = PresenceSubscriptionController.getSubscription(next, i);
            if (subscription != null && subscription.getPhoneId() == i) {
                AlarmTimer.stop(this.mContext, this.mSubscribeRetryList.get(next));
                subscription.updateState(4);
                it.remove();
            }
        }
        IMSLog.i(LOG_TAG, i, "stopSubscribeRetryTimer");
    }

    /* access modifiers changed from: package-private */
    public void onPeriodicPublish(int i) {
        IMSLog.e(LOG_TAG, i, "onPeriodicPublish:");
        publish(this.mPresenceModuleInfo.get(i).mOwnPresenceInfo, i);
        startPublishTimer(i);
    }

    private void acquireWakeLock() {
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null) {
            synchronized (wakeLock) {
                this.mWakeLock.acquire(5000);
                removeMessages(13);
                sendEmptyMessageDelayed(13, 5000);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean clearWakeLock() {
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock == null) {
            return false;
        }
        synchronized (wakeLock) {
            if (!this.mWakeLock.isHeld()) {
                return false;
            }
            this.mWakeLock.release();
            removeMessages(13);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void onSubscribeRequested(PresenceSubscriptionController.SubscriptionRequest subscriptionRequest) {
        subscribe(subscriptionRequest, this.mPresenceConfig.get(subscriptionRequest.phoneId).useAnonymousFetch());
    }

    /* access modifiers changed from: package-private */
    public void onSubscribeComplete(PresenceSubscription presenceSubscription, PresenceResponse presenceResponse) {
        int phoneId = presenceSubscription.getPhoneId();
        IMSLog.s(LOG_TAG, phoneId, "onSubscribeComplete: Uri " + presenceSubscription.getUriList() + " success " + presenceResponse.isSuccess());
        clearWakeLock();
        if (RcsPolicyManager.getRcsStrategy(phoneId) == null) {
            IMSLog.e(LOG_TAG, phoneId, "onSubscribeComplete: mnoStrategy is null.");
            return;
        }
        PresenceUtil.onSubscribeNetworkResponse(presenceSubscription.getSubscriptionId(), this.mContext, phoneId, presenceResponse.getSipError(), presenceResponse.getErrorDescription());
        if (!presenceResponse.isSuccess()) {
            presenceSubscription.updateState(6);
            onSubscribeFailed(presenceSubscription, presenceResponse);
        } else {
            int expiry = presenceSubscription.getExpiry();
            if (expiry > 0) {
                presenceSubscription.updateState(1);
                if (presenceSubscription.getRequestType() == CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC && this.mListener != null) {
                    IMSLog.i(LOG_TAG, phoneId, "onSubscribeComplete: recover polling");
                    this.mListener.onPollingRequested(true, phoneId);
                }
                if (!presenceSubscription.isSingleFetch() && this.mPresenceModuleInfo.get(phoneId).mMno == Mno.TMOUS) {
                    IMSLog.i(LOG_TAG, phoneId, "onSubscribeComplete: subscription will be terminated after " + expiry);
                    sendMessageDelayed(obtainMessage(9, presenceSubscription), ((long) (expiry + 1)) * 1000);
                }
            } else {
                presenceSubscription.updateState(4);
                if (this.mPresenceModuleInfo.get(phoneId).mMno == Mno.ATT) {
                    resetSubscribeRetryCount(presenceSubscription, phoneId);
                }
            }
        }
        ContentValues contentValues = new ContentValues();
        if (presenceResponse.isSuccess() || (!presenceResponse.isSuccess() && (presenceResponse.getSipError() == 403 || presenceResponse.getSipError() == 404))) {
            contentValues.put("RCSC", 1);
        } else {
            contentValues.put(DiagnosisConstants.DRCS_KEY_RCSF, 1);
        }
        contentValues.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
        ImsLogAgentUtil.storeLogToAgent(phoneId, this.mContext, DiagnosisConstants.FEATURE_DRCS, contentValues);
        PresenceSubscriptionController.cleanExpiredSubscription();
    }

    private void onSubscribeFailed(PresenceSubscription presenceSubscription, PresenceResponse presenceResponse) {
        IRegistrationGovernor registrationGovernor;
        ICapabilityEventListener iCapabilityEventListener;
        int phoneId = presenceSubscription.getPhoneId();
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
        if (rcsStrategy == null) {
            IMSLog.e(LOG_TAG, phoneId, "onSubscribeFailed: mnoStrategy is null.");
            return;
        }
        PresenceResponse.PresenceStatusCode handlePresenceFailure = rcsStrategy.handlePresenceFailure(presenceResponse.getReason(), false);
        this.mPresenceModuleInfo.get(phoneId).mLastSubscribeStatusCode = handlePresenceFailure;
        IMSLog.i(LOG_TAG, phoneId, "onSubscribeFailed - statusCode: " + handlePresenceFailure);
        PresenceInfo presenceInfo = new PresenceInfo(presenceSubscription.getSubscriptionId(), phoneId);
        presenceInfo.addService(new ServiceTuple((long) Capabilities.FEATURE_NOT_UPDATED, (String) null, (String) null));
        presenceInfo.setFetchState(false);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$presence$PresenceResponse$PresenceStatusCode[handlePresenceFailure.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    if (i != 4) {
                        if (i != 6) {
                            if (i != 15) {
                                switch (i) {
                                    case 8:
                                        break;
                                    case 9:
                                        onSubscribeNoResponse(presenceSubscription);
                                        break;
                                    case 10:
                                        PresenceUtil.removeSubscribeResponseCallback(presenceSubscription.getSubscriptionId());
                                        break;
                                    case 11:
                                        break;
                                }
                            }
                        } else {
                            if (presenceSubscription.getRequestType() == CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC && (iCapabilityEventListener = this.mListener) != null) {
                                iCapabilityEventListener.onPollingRequested(false, phoneId);
                            }
                            presenceSubscription.updateState(5);
                            presenceSubscription.setExpiry(presenceResponse.getRetryTime());
                            sendMessage(obtainMessage(8, presenceSubscription));
                        }
                    }
                    presenceSubscription.retrySubscription();
                    handleExpBackOffRetry(presenceSubscription);
                }
                presenceInfo.clearService();
                IMSLog.i(LOG_TAG, phoneId, "onSubscribeFailed - PRESENCE_NO_SUBSCRIBE: code : " + presenceResponse.getSipError() + " errorReason : " + presenceResponse.getErrorDescription());
                if (presenceResponse.getSipError() != 404 || !"isbot".equals(presenceResponse.getErrorDescription())) {
                    presenceInfo.addService(new ServiceTuple((long) Capabilities.FEATURE_NON_RCS_USER, (String) null, (String) null));
                } else {
                    presenceInfo.addService(new ServiceTuple(Capabilities.FEATURE_CHATBOT_ROLE, (String) null, (String) null));
                }
                PresenceUtil.removeSubscribeResponseCallback(presenceSubscription.getSubscriptionId());
            } else {
                PresenceSubscriptionController.addPendingSubscription(presenceSubscription);
            }
            if (this.mPresenceModuleInfo.get(phoneId).mMno == Mno.ATT && handlePresenceFailure != PresenceResponse.PresenceStatusCode.PRESENCE_NO_RESPONSE) {
                resetSubscribeRetryCount(presenceSubscription, phoneId);
            }
            if (!(this.mPresenceModuleInfo.get(phoneId).mRegInfo == null || (registrationGovernor = ImsRegistry.getRegistrationManager().getRegistrationGovernor(this.mPresenceModuleInfo.get(phoneId).mRegInfo.getHandle())) == null)) {
                registrationGovernor.onSubscribeError(1, new SipError(presenceResponse.getSipError(), presenceResponse.getErrorDescription()));
            }
            this.mPresenceUpdate.onNewPresenceInformation(presenceInfo, phoneId, presenceSubscription);
            PresenceUtil.sendRCSPSubInfoToHQM(this.mContext, presenceResponse.getSipError(), phoneId);
            IMSLog.c(LogClass.PM_ONSUB_FAIL, phoneId + "," + handlePresenceFailure + "," + presenceResponse.getSipError());
        } else if (this.mPresenceModuleInfo.get(phoneId).mMno == Mno.VZW) {
            this.mCapabilityDiscovery.clearCapabilitiesCache(phoneId);
            PresenceUtil.triggerOmadmTreeSync(this.mContext, phoneId);
            setParalysed(true, phoneId);
            IMSLog.i(LOG_TAG, phoneId, "trigger OMA sync for 403 not provisioned");
            PresenceUtil.removeSubscribeResponseCallback(presenceSubscription.getSubscriptionId());
            resetSubscribeRetryCount(presenceSubscription, phoneId);
            registrationGovernor.onSubscribeError(1, new SipError(presenceResponse.getSipError(), presenceResponse.getErrorDescription()));
            this.mPresenceUpdate.onNewPresenceInformation(presenceInfo, phoneId, presenceSubscription);
            PresenceUtil.sendRCSPSubInfoToHQM(this.mContext, presenceResponse.getSipError(), phoneId);
            IMSLog.c(LogClass.PM_ONSUB_FAIL, phoneId + "," + handlePresenceFailure + "," + presenceResponse.getSipError());
        }
        if (this.mPresenceModuleInfo.get(phoneId).mMno == Mno.VZW) {
            PresenceUtil.triggerOmadmTreeSync(this.mContext, phoneId);
        }
        IMSLog.i(LOG_TAG, phoneId, "onSubscribeFailed: for 403 forbidden response");
        PresenceUtil.removeSubscribeResponseCallback(presenceSubscription.getSubscriptionId());
        resetSubscribeRetryCount(presenceSubscription, phoneId);
        registrationGovernor.onSubscribeError(1, new SipError(presenceResponse.getSipError(), presenceResponse.getErrorDescription()));
        this.mPresenceUpdate.onNewPresenceInformation(presenceInfo, phoneId, presenceSubscription);
        PresenceUtil.sendRCSPSubInfoToHQM(this.mContext, presenceResponse.getSipError(), phoneId);
        IMSLog.c(LogClass.PM_ONSUB_FAIL, phoneId + "," + handlePresenceFailure + "," + presenceResponse.getSipError());
    }

    private void handleExpBackOffRetry(PresenceSubscription presenceSubscription) {
        long j;
        int phoneId = presenceSubscription.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "handleExpBackOffRetry: EXP_BACKOFF_RETRY count = " + presenceSubscription.getRetryCount());
        if (presenceSubscription.getRequestType() == CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC) {
            if (this.mListener != null && presenceSubscription.getRetryCount() == 1) {
                IMSLog.i(LOG_TAG, phoneId, "handleExpBackOffRetry: notifying polling failure");
                this.mListener.onPollingRequested(false, phoneId);
            }
            j = PresenceUtil.getListSubscribeExpBackOffRetryTime(phoneId, presenceSubscription.getRetryCount());
        } else {
            j = presenceSubscription.getRequestType() == CapabilityConstants.RequestType.REQUEST_TYPE_CONTACT_CHANGE ? PresenceUtil.getSubscribeExpBackOffRetryTime(phoneId, presenceSubscription.getRetryCount()) : 0;
        }
        if (j != 0) {
            presenceSubscription.updateState(5);
            startSubscribeRetryTimer(j * 1000, presenceSubscription.getSubscriptionId(), phoneId);
            return;
        }
        presenceSubscription.updateState(4);
    }

    /* access modifiers changed from: package-private */
    public void onSubscribeListRequested(CapabilityConstants.RequestType requestType, int i, int i2) {
        Set set = this.mUrisToSubscribe.get(Integer.valueOf(i));
        synchronized (set) {
            subscribe(set, true, requestType, i, i2);
        }
    }

    /* access modifiers changed from: package-private */
    public void onSubscribeRetry(PresenceSubscription presenceSubscription) {
        int phoneId = presenceSubscription.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "onSubscribeRetry");
        presenceSubscription.updateTimestamp();
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(presenceSubscription.getUriList());
        if (arrayList.size() > 1) {
            ImsProfile imsProfile = this.mPresenceModuleInfo.get(phoneId).mRegInfo.getImsProfile();
            IPresenceStackInterface iPresenceStackInterface = this.mService;
            IPresenceStackInterface iPresenceStackInterface2 = iPresenceStackInterface;
            iPresenceStackInterface2.subscribeList(arrayList, true, obtainMessage(6, presenceSubscription), presenceSubscription.getSubscriptionId(), imsProfile.isGzipEnabled(), presenceSubscription.getExpiry(), phoneId);
            return;
        }
        this.mService.subscribe((ImsUri) arrayList.get(0), true, obtainMessage(6, presenceSubscription), presenceSubscription.getSubscriptionId(), phoneId);
    }

    private void onSubscribeNoResponse(PresenceSubscription presenceSubscription) {
        int phoneId = presenceSubscription.getPhoneId();
        if (this.mPresenceModuleInfo.get(phoneId).mMno == Mno.ATT && presenceSubscription.isSingleFetch()) {
            if (presenceSubscription.getRetryCount() == 0) {
                presenceSubscription.updateState(5);
                presenceSubscription.retrySubscription();
                IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
                long calThrottledPublishRetryDelayTime = rcsStrategy != null ? rcsStrategy.calThrottledPublishRetryDelayTime(presenceSubscription.getTimestamp().getTime(), this.mPresenceConfig.get(phoneId).getSourceThrottlePublish()) : 0;
                IMSLog.i(LOG_TAG, phoneId, "onSubscribeNoResponse: retry in " + calThrottledPublishRetryDelayTime);
                if (calThrottledPublishRetryDelayTime > 0) {
                    sendMessageDelayed(obtainMessage(8, presenceSubscription), calThrottledPublishRetryDelayTime);
                } else {
                    sendMessage(obtainMessage(8, presenceSubscription));
                }
            } else {
                IMSLog.i(LOG_TAG, phoneId, "onSubscribeNoResponse: no more retry");
                presenceSubscription.updateState(4);
                presenceSubscription.setRetryCount(0);
            }
        }
    }

    private void resetSubscribeRetryCount(PresenceSubscription presenceSubscription, int i) {
        if (presenceSubscription.isSingleFetch() && presenceSubscription.getRetryCount() > 0) {
            IMSLog.i(LOG_TAG, i, "resetSubscribeRetryCount");
            presenceSubscription.setRetryCount(0);
        }
    }

    public void readConfig(int i) {
        if (this.mPresenceConfig.get(i) == null) {
            IMSLog.e(LOG_TAG, i, "readConfig: not ready");
            return;
        }
        IMSLog.i(LOG_TAG, i, "readConfig");
        this.mPresenceConfig.get(i).load();
        this.mPresenceModuleInfo.get(i).mOwnPresenceInfo.setExpireTime(this.mPresenceConfig.get(i).getRetryPublishTimer());
        notifyProvisionedValue(i);
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
        Log.i(LOG_TAG, "handleMessage: msg " + message.what);
        if (!PresenceEvent.handleEvent(message, this, this.mPresenceUpdate, activeDataPhoneId)) {
            Log.e(LOG_TAG, "handleMessage: unknown event " + message.what);
        }
    }

    private void setServiceVersion(int i) {
        HashMap hashMap = new HashMap();
        ServiceTuple serviceTuple = ServiceTuple.getServiceTuple(Capabilities.FEATURE_CHATBOT_EXTENDED_MSG);
        hashMap.put("xbotmessage", serviceTuple.version);
        this.mService.updateServiceVersion(i, hashMap);
        IMSLog.i(LOG_TAG, i, "setServiceVersion: xbotmessage " + serviceTuple.version);
    }

    /* access modifiers changed from: package-private */
    public boolean checkModuleReady(int i) {
        if (!isRunning()) {
            IMSLog.e(LOG_TAG, i, "checkModuleReady: module not running");
            return false;
        } else if (!isReadyToRequest(i)) {
            IMSLog.e(LOG_TAG, i, "checkModuleReady: not ready to request");
            return false;
        } else if (RcsPolicyManager.getRcsStrategy(i) == null) {
            IMSLog.e(LOG_TAG, i, "checkModuleReady: mnoStrategy is null.");
            return false;
        } else if (this.mUriGenerator.get(i) != null) {
            return true;
        } else {
            IMSLog.e(LOG_TAG, i, "checkModuleReady: mUriGenerator is null");
            return false;
        }
    }

    private void onEABPublishComplete(PresenceResponse presenceResponse) {
        this.mListener.onCapabilityAndAvailabilityPublished(presenceResponse.getSipError(), presenceResponse.getPhoneId());
    }

    public void registerService(String str, String str2, int i) {
        IMSLog.i(LOG_TAG, i, "registerService: [" + str + ":" + str2 + "]");
        ServiceTuple serviceTuple = ServiceTuple.getServiceTuple(str, str2, (String[]) null);
        if (serviceTuple != null) {
            IMSLog.i(LOG_TAG, i, "registerService: valid service tuple");
            if (!this.mPresenceModuleInfo.get(i).mOwnInfoPublished) {
                this.mServiceTupleList.add(serviceTuple);
                return;
            }
            synchronized (this.mPresenceModuleInfo.get(i).mOwnPresenceInfo) {
                this.mPresenceModuleInfo.get(i).mOwnPresenceInfo.addService(serviceTuple);
            }
            removeMessages(1, Integer.valueOf(i));
            sendMessage(obtainMessage(1, Integer.valueOf(i)));
            return;
        }
        IMSLog.i(LOG_TAG, i, "advertise: not a valid service tuple, do nothing..");
    }

    public void deRegisterService(List<String> list, int i) {
        IMSLog.i(LOG_TAG, i, "deRegisterService: serviceIdList = " + list);
        boolean z = false;
        for (String split : list) {
            String[] split2 = split.split("#");
            ServiceTuple serviceTuple = ServiceTuple.getServiceTuple(split2[0], split2[1], (String[]) null);
            if (serviceTuple != null) {
                synchronized (this.mPresenceModuleInfo.get(i).mOwnPresenceInfo) {
                    this.mPresenceModuleInfo.get(i).mOwnPresenceInfo.removeService(serviceTuple);
                }
                z = true;
            } else {
                IMSLog.e(LOG_TAG, i, "deRegisterService: not a valid service tuple");
            }
        }
        if (z) {
            removeMessages(1, Integer.valueOf(i));
            sendMessage(obtainMessage(1, Integer.valueOf(i)));
        }
    }

    public void loadThirdPartyServiceTuples(List<ServiceTuple> list) {
        Log.i(LOG_TAG, "loadThirdPartyServiceTuples");
        for (ServiceTuple next : list) {
            synchronized (this.mServiceTupleList) {
                this.mServiceTupleList.add(next);
            }
        }
    }

    private void buildPresenceInfoForThirdParty(int i) {
        IMSLog.i(LOG_TAG, i, "buildPresenceInfoForThirdParty");
        synchronized (this.mServiceTupleList) {
            if (!this.mServiceTupleList.isEmpty()) {
                for (ServiceTuple addService : this.mServiceTupleList) {
                    this.mPresenceModuleInfo.get(i).mOwnPresenceInfo.addService(addService);
                }
            }
        }
    }

    private void setBadEventProgress(boolean z, int i) {
        IMSLog.i(LOG_TAG, i, "setBadEventProgress: " + z);
        this.mPresenceModuleInfo.get(i).mBadEventProgress = z;
    }

    public boolean getBadEventProgress(int i) {
        return this.mPresenceModuleInfo.get(i).mBadEventProgress;
    }

    public boolean isPublishNotFoundProgress(int i) {
        return this.mPresenceModuleInfo.get(i).mPublishNotFoundProgress;
    }

    private void setPublishNotFoundProgress(boolean z, int i) {
        if (this.mPresenceModuleInfo.get(i).mMno == Mno.ATT) {
            IMSLog.i(LOG_TAG, i, "setPublishNotFoundProgress: " + z);
            if (!z) {
                this.mPresenceModuleInfo.get(i).mPublishExpBackOffRetryCount = 0;
            }
            this.mPresenceModuleInfo.get(i).mPublishNotFoundProgress = z;
            setParalysed(z, i);
        }
    }

    public boolean isOwnCapPublished() {
        return this.mPresenceModuleInfo.get(SimUtil.getActiveDataPhoneId()).mOwnInfoPublished;
    }

    /* access modifiers changed from: package-private */
    public void onSubscriptionTerminated(PresenceSubscription presenceSubscription) {
        if (presenceSubscription == null) {
            Log.e(LOG_TAG, "onSubscriptionTerminated: subscription is null");
            return;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(presenceSubscription.getDropUris());
        if (arrayList.size() > 0) {
            Log.i(LOG_TAG, "onSubscriptionTerminated: update capabilities for dropped " + arrayList.size() + " uris");
            ICapabilityEventListener iCapabilityEventListener = this.mListener;
            if (iCapabilityEventListener != null) {
                iCapabilityEventListener.onCapabilityUpdate(arrayList, (long) Capabilities.FEATURE_NOT_UPDATED, CapabilityConstants.CapExResult.SUCCESS, (String) null, presenceSubscription.getPhoneId());
            }
        }
    }

    public long getSupportFeature(int i) {
        return this.mEnabledFeatures[i];
    }

    public void reset(int i) {
        this.mPresenceSp.savePublishTimestamp(0, i);
        stopPublishTimer(i);
        stopBadEventTimer(i);
        stopSubscribeRetryTimer(i);
        this.mPresenceSp.resetPublishEtag(i);
    }

    public void removePresenceCache(List<ImsUri> list, int i) {
        this.mPresenceCacheController.removePresenceCache(list, i);
    }

    private void resetPublishErrorHandling(int i) {
        PresenceModuleInfo presenceModuleInfo = this.mPresenceModuleInfo.get(i);
        presenceModuleInfo.mLimitReRegistration = false;
        presenceModuleInfo.mLimitImmediateRetry = false;
        presenceModuleInfo.mOldPublishError = null;
        setPublishNotFoundProgress(false, i);
    }

    public boolean getRegiInfoUpdater(int i) {
        return this.mPresenceRegiInfoUpdater.getOrDefault(Integer.valueOf(i), Boolean.FALSE).booleanValue();
    }

    public void setRegiInfoUpdater(int i, boolean z) {
        this.mPresenceRegiInfoUpdater.put(Integer.valueOf(i), Boolean.valueOf(z));
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(LOG_TAG);
        IMSLog.dump(LOG_TAG, "Publish History: ");
        this.mEventLog.dump();
        for (PresenceConfig next : this.mPresenceConfig.values()) {
            if (next != null) {
                IMSLog.dump(LOG_TAG, next.toString());
            }
        }
        IMSLog.decreaseIndent(LOG_TAG);
    }
}
