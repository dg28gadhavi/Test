package com.sec.internal.ims.servicemodules.options;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsException;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.stub.RcsCapabilityExchangeImplBase;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.options.ICapabilityServiceEventListener;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.presence.ServiceTuple;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.options.ContactCache;
import com.sec.internal.ims.servicemodules.presence.PresenceModule;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimEventListener;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityExchangeControl;
import com.sec.internal.log.IMSLog;
import com.sec.sve.generalevent.VcidEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class CapabilityDiscoveryModule extends ServiceModuleBase implements ICapabilityDiscoveryModule {
    private static final int CONTACT_CHANGED_DELAY = 500;
    private static final int EVT_EXCHANGE_CAPABILITIES_FOR_VSH_DELAY = 500;
    private static final long LAST_SEEN_ACTIVE = 0;
    private static final long LAST_SEEN_UNKNOWN = -1;
    private static final String LOG_TAG = "CapabilityDiscModule";
    private static final String NAME = CapabilityDiscoveryModule.class.getSimpleName();
    private static final int SET_OWN_CAPABILITIES_DELAY = 500;
    private static final int SET_OWN_CAPABILITIES_DELAY_ON_REG = 100;
    private PhoneIdKeyMap<Boolean> forcePollingGuard;
    PhoneIdKeyMap<Boolean> isOfflineAddedContact;
    private ImsUri mActiveCallRemoteUri;
    /* access modifiers changed from: private */
    public int mAvailablePhoneId;
    protected Handler mBackgroundHandler;
    protected String[] mCallNumber;
    final Map<Integer, CapabilitiesCache> mCapabilitiesMapList = new HashMap();
    CapabilityEventListener mCapabilityEventListener;
    protected CapabilityExchange mCapabilityExchange;
    protected CapabilityForIncall mCapabilityForIncall;
    protected boolean mCapabilityModuleOn = true;
    protected CapabilityQuery mCapabilityQuery;
    protected CapabilityRegistration mCapabilityRegistration;
    CapabilityServiceEventListener mCapabilityServiceEventListener;
    protected CapabilityUpdate mCapabilityUpdate;
    protected CapabilityUtil mCapabilityUtil;
    protected PhoneIdKeyMap<CapabilityConfig> mConfigs;
    ContactCache mContactList;
    private final ContactCache.ContactEventListener mContactListener = new CapabilityDiscoveryModule$$ExternalSyntheticLambda0(this);
    Context mContext;
    PhoneIdKeyMap<ICapabilityExchangeControl> mControl;
    private SimpleEventLog mEventLog;
    private Map<Integer, Boolean> mHasVideoOwn = new HashMap();
    private IImModule mImModule;
    protected Map<Integer, ImsRegistration> mImsRegInfoList = new HashMap();
    protected PhoneIdKeyMap<Boolean> mInitialQuery;
    private Map<Integer, Boolean> mIsConfigured;
    private Map<Integer, Boolean> mIsConfiguredOnCapability;
    /* access modifiers changed from: private */
    public boolean mIsInCall;
    private Map<Integer, CapabilityConstants.CapExResult> mLastCapExResult;
    protected PhoneIdKeyMap<Long> mLastListSubscribeStamp;
    private PhoneIdKeyMap<Long> mLastPollTimestamp;
    private Map<Integer, Integer> mNetworkClass;
    private Map<Integer, NetworkEvent> mNetworkEvent;
    private Map<Integer, Integer> mNetworkType;
    protected Map<Integer, Long> mOldFeature;
    private PhoneIdKeyMap<HashMap<ImsUri, List<RcsCapabilityExchangeImplBase.OptionsResponseCallback>>> mOptionsCallbacks;
    OptionsModule mOptionsModule;
    protected Map<Integer, Boolean> mOptionsSwitchOnList = new HashMap();
    /* access modifiers changed from: private */
    public Map<Integer, Capabilities> mOwnList = new HashMap();
    protected PhoneIdKeyMap<PendingIntent> mPartialPollingIntent;
    protected PhoneIdKeyMap<List<Date>> mPollingHistory;
    protected PhoneIdKeyMap<PendingIntent> mPollingIntent;
    PresenceModule mPresenceModule;
    protected Map<Integer, Boolean> mPresenceSwitchOnList = new HashMap();
    protected Map<Integer, Set<String>> mPublishedServiceList = new HashMap();
    private Map<Integer, String> mRcsProfile = new HashMap();
    IRegistrationManager mRegMan;
    protected int mRetrySyncContactCount;
    private final ServiceAvailabilityEventListenerWrapper mServiceAvailabilityEventListenerWrapper;
    List<ServiceTuple> mServiceTupleList = new ArrayList();
    protected SimEventListener mSimEventListener = null;
    protected TelephonyCallbackForCapability mTelephonyCallback;
    private final TelephonyManager mTelephonyManager;
    protected PhoneIdKeyMap<PendingIntent> mThrottledIntent;
    private UriGenerator mUriGenerator = null;
    Map<Integer, Set<ImsUri>> mUrisToRequestList = new HashMap();
    private PhoneIdKeyMap<Long> mUserLastActive;

    public void handleIntent(Intent intent) {
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        removeMessages(2);
        sendMessageDelayed(obtainMessage(2), 500);
    }

    public CapabilityDiscoveryModule(Looper looper, Context context, OptionsModule optionsModule, PresenceModule presenceModule, IRegistrationManager iRegistrationManager, IImModule iImModule) {
        super(looper);
        boolean z = false;
        this.mIsInCall = false;
        this.mIsConfigured = new HashMap();
        this.mIsConfiguredOnCapability = new HashMap();
        this.mCallNumber = new String[]{null, null};
        this.mLastCapExResult = new HashMap();
        this.mBackgroundHandler = null;
        this.mActiveCallRemoteUri = null;
        this.mNetworkEvent = new HashMap();
        this.mNetworkClass = new HashMap();
        this.mNetworkType = new HashMap();
        this.mAvailablePhoneId = 0;
        this.mOldFeature = new HashMap();
        this.mRetrySyncContactCount = 0;
        this.mContext = context;
        this.mRegMan = iRegistrationManager;
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 1000);
        int phoneCount = SimUtil.getPhoneCount();
        for (int i = 0; i < phoneCount; i++) {
            this.mUrisToRequestList.put(Integer.valueOf(i), new HashSet());
            Map<Integer, Boolean> map = this.mOptionsSwitchOnList;
            Integer valueOf = Integer.valueOf(i);
            Boolean bool = Boolean.TRUE;
            map.put(valueOf, bool);
            this.mPresenceSwitchOnList.put(Integer.valueOf(i), bool);
            this.mOwnList.put(Integer.valueOf(i), new Capabilities());
            this.mRcsProfile.put(Integer.valueOf(i), "");
            Map<Integer, Boolean> map2 = this.mHasVideoOwn;
            Integer valueOf2 = Integer.valueOf(i);
            Boolean bool2 = Boolean.FALSE;
            map2.put(valueOf2, bool2);
            this.mIsConfigured.put(Integer.valueOf(i), bool2);
            this.mIsConfiguredOnCapability.put(Integer.valueOf(i), bool2);
            this.mLastCapExResult.put(Integer.valueOf(i), CapabilityConstants.CapExResult.SUCCESS);
            this.mOldFeature.put(Integer.valueOf(i), Long.valueOf((long) Capabilities.FEATURE_NOT_UPDATED));
            this.mNetworkEvent.put(Integer.valueOf(i), (Object) null);
            this.mNetworkClass.put(Integer.valueOf(i), 0);
            this.mNetworkType.put(Integer.valueOf(i), 0);
        }
        initContactCache(phoneCount);
        this.mOptionsModule = optionsModule;
        this.mPresenceModule = presenceModule;
        this.mCapabilityEventListener = new CapabilityEventListener(this, this.mContext);
        this.mServiceAvailabilityEventListenerWrapper = new ServiceAvailabilityEventListenerWrapper(this);
        this.mCapabilityServiceEventListener = new CapabilityServiceEventListener();
        this.mImModule = iImModule;
        if (!IMSLog.isShipBuild()) {
            Preconditions.checkState(this.mImModule != null ? true : z, "Shall not happen! Wrong order of modules instantiation in ServiceModuleManager");
        } else if (this.mImModule == null) {
            Log.e(LOG_TAG, "Shall not happen! Wrong order of modules instantiation in ServiceModuleManager");
        }
        CapabilityIntentReceiver capabilityIntentReceiver = new CapabilityIntentReceiver(this);
        this.mContext.registerReceiver(capabilityIntentReceiver, capabilityIntentReceiver.getIntentFilter());
        this.mTelephonyCallback = new TelephonyCallbackForCapability();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        if (this.mUriGenerator == null) {
            UriGenerator uriGenerator = UriGeneratorFactory.getInstance().get(UriGenerator.URIServiceType.RCS_URI);
            this.mUriGenerator = uriGenerator;
            this.mContactList.setUriGenerator(uriGenerator);
        }
        if (phoneCount > 1) {
            SimManagerFactory.registerForADSChange(this, 15, (Object) null);
        }
        this.mConfigs = new PhoneIdKeyMap<>(phoneCount, null);
        this.mControl = new PhoneIdKeyMap<>(phoneCount, null);
        this.mUserLastActive = new PhoneIdKeyMap<>(phoneCount, -1L);
        this.mInitialQuery = new PhoneIdKeyMap<>(phoneCount, Boolean.TRUE);
        this.mPollingHistory = new PhoneIdKeyMap<>(phoneCount, new ArrayList());
        this.mLastPollTimestamp = new PhoneIdKeyMap<>(phoneCount, -1L);
        this.mLastListSubscribeStamp = new PhoneIdKeyMap<>(phoneCount, -1L);
        Boolean bool3 = Boolean.FALSE;
        this.isOfflineAddedContact = new PhoneIdKeyMap<>(phoneCount, bool3);
        this.forcePollingGuard = new PhoneIdKeyMap<>(phoneCount, bool3);
        this.mPollingIntent = new PhoneIdKeyMap<>(phoneCount, null);
        this.mThrottledIntent = new PhoneIdKeyMap<>(phoneCount, null);
        this.mOptionsCallbacks = new PhoneIdKeyMap<>(phoneCount, null);
        this.mPartialPollingIntent = new PhoneIdKeyMap<>(phoneCount, null);
        this.mCapabilityUtil = new CapabilityUtil(this, this.mEventLog);
        this.mCapabilityForIncall = new CapabilityForIncall(this, this.mCapabilityUtil, iRegistrationManager);
        CapabilityExchange capabilityExchange = new CapabilityExchange(this, this.mCapabilityUtil, this.mEventLog);
        this.mCapabilityExchange = capabilityExchange;
        this.mCapabilityQuery = new CapabilityQuery(this, this.mCapabilityUtil, capabilityExchange);
        this.mCapabilityUpdate = new CapabilityUpdate(this, this.mCapabilityUtil, iRegistrationManager, this.mEventLog);
        this.mCapabilityRegistration = new CapabilityRegistration(this, this.mCapabilityUtil, iRegistrationManager);
        IMSLog.i(LOG_TAG, "created");
    }

    public String getName() {
        return NAME;
    }

    public String[] getServicesRequiring() {
        return new String[]{"options", SipMsg.EVENT_PRESENCE, "lastseen"};
    }

    public void onServiceSwitched(int i, ContentValues contentValues) {
        this.mCapabilityUtil.onServiceSwitched(i, contentValues, this.mPresenceSwitchOnList, this.mOptionsSwitchOnList, this.mCapabilityModuleOn);
    }

    public void init() {
        IMSLog.i(LOG_TAG, "init");
        super.init();
        HandlerThread handlerThread = new HandlerThread("BackgroundHandler", 10);
        handlerThread.start();
        this.mBackgroundHandler = new Handler(handlerThread.getLooper());
        loadCapabilityStorage();
        this.mCapabilityUtil.migrateSharedprefWithPhoneId(this.mContext);
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            this.mLastPollTimestamp.put(i, Long.valueOf(loadPollTimestamp(i)));
        }
    }

    public void onConfigured(int i) {
        IMSLog.i(LOG_TAG, i, "onConfigured");
        processConfigured(i);
    }

    /* access modifiers changed from: package-private */
    public void processConfigured(int i) {
        post(new CapabilityDiscoveryModule$$ExternalSyntheticLambda5(this, i));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$processConfigured$1(int i) {
        int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
        this.mAvailablePhoneId = activeDataPhoneId;
        if (activeDataPhoneId == -1) {
            this.mAvailablePhoneId = 0;
        }
        if (!RcsUtils.DualRcs.isRegAllowed(this.mContext, i)) {
            IMSLog.i(LOG_TAG, i, "omit not data sim onConfigured!");
            return;
        }
        try {
            this.mConfigs.put(i, new CapabilityConfig(this.mContext, i));
            loadConfig(i);
            if (!this.mContactList.isReady(i) && this.mConfigs.get(i) != null && !this.mConfigs.get(i).isDisableInitialScan()) {
                IMSLog.i(LOG_TAG, i, "onConfigured: start ContactCache");
                if (!RcsUtils.DualRcs.isRegAllowed(this.mContext, i)) {
                    IMSLog.i(LOG_TAG, i, "onConfigured: ignore to start ContactCache because of opposite sim");
                } else if (this.mCapabilityUtil.isCheckRcsSwitch(this.mContext)) {
                    syncContact();
                }
            }
            if (this.mConfigs.get(i) == null || !this.mConfigs.get(i).usePresence()) {
                this.mControl.put(i, this.mOptionsModule);
            } else {
                this.mControl.put(i, this.mPresenceModule);
                Capabilities capabilities = this.mOwnList.get(Integer.valueOf(i));
                capabilities.addFeature((long) Capabilities.FEATURE_PRESENCE_DISCOVERY);
                this.mOwnList.put(Integer.valueOf(i), capabilities);
            }
            this.mRcsProfile.put(Integer.valueOf(i), this.mConfigs.get(i) != null ? this.mConfigs.get(i).getRcsProfile() : "");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        loadThirdPartyServiceTuples(i);
        onImsSettingsUpdate(i);
        sendMessage(obtainMessage(9, i, 0, (Object) null));
        sendMessage(obtainMessage(52, i, 0));
    }

    /* access modifiers changed from: package-private */
    public void loadConfig(int i) {
        if (this.mConfigs.get(i) == null) {
            IMSLog.s(LOG_TAG, i, "Config not ready");
            return;
        }
        this.mConfigs.get(i).load();
        notifyProvisionedValue(i);
    }

    public void start() {
        IMSLog.i(LOG_TAG, VcidEvent.BUNDLE_VALUE_ACTION_START);
        if (this.mCapabilityUtil.isCheckRcsSwitch(this.mContext)) {
            super.start();
            this.mContactList.registerListener(this.mContactListener);
            this.mContactList.start();
            this.mOptionsModule.registerCapabilityEventListener(this.mCapabilityEventListener);
            this.mPresenceModule.registerCapabilityEventListener(this.mCapabilityEventListener);
            registerSimCardEventListener();
            updateMsgAppInfo(true);
            this.mTelephonyManager.registerTelephonyCallback(this.mContext.getMainExecutor(), this.mTelephonyCallback);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateMsgAppInfo(boolean z) {
        try {
            String msgAppPkgName = PackageUtils.getMsgAppPkgName(this.mContext);
            ApplicationInfo applicationInfo = this.mContext.getPackageManager().getApplicationInfo(msgAppPkgName, 128);
            String string = applicationInfo.metaData.getString("Xbot.Version");
            if (string == null) {
                IMSLog.i(LOG_TAG, "Xbot.Version" + " is null");
                string = Float.toString(applicationInfo.metaData.getFloat("Xbot.Version"));
            }
            ServiceTuple serviceTuple = ServiceTuple.getServiceTuple(Capabilities.FEATURE_CHATBOT_EXTENDED_MSG);
            ServiceTuple serviceTuple2 = ServiceTuple.getServiceTuple(Capabilities.FEATURE_CHATBOT_CHAT_SESSION);
            IMSLog.i(LOG_TAG, "updateMsgAppInfo: msgAppPkgName:" + msgAppPkgName + "cur:" + serviceTuple + ", new:" + string);
            if (!TextUtils.equals(string, serviceTuple.version)) {
                ServiceTuple.setServiceVersion(Capabilities.FEATURE_CHATBOT_EXTENDED_MSG, string);
                if (!z) {
                    for (Map.Entry next : this.mImsRegInfoList.entrySet()) {
                        int intValue = ((Integer) next.getKey()).intValue();
                        if (((ImsRegistration) next.getValue()).hasRcsService()) {
                            PresenceInfo ownPresenceInfo = this.mPresenceModule.getOwnPresenceInfo(intValue);
                            if (ownPresenceInfo.getServiceTuple(serviceTuple2.serviceId) == null) {
                                IMSLog.i(LOG_TAG, intValue, "updateMsgAppInfo: chatbot not registered");
                            } else if (ownPresenceInfo.getServiceTuple(serviceTuple.serviceId) == null || serviceTuple.version.equals("0.0")) {
                                this.mOldFeature.put(Integer.valueOf(intValue), Long.valueOf(this.mOwnList.get(Integer.valueOf(intValue)).getFeature()));
                                IMSLog.i(LOG_TAG, intValue, "updateMsgAppInfo: update REGISTER");
                                this.mImModule.updateExtendedBotMsgFeature(intValue);
                                this.mRegMan.sendReRegister(intValue, ((ImsRegistration) next.getValue()).getNetworkType());
                            } else {
                                IMSLog.i(LOG_TAG, intValue, "updateMsgAppInfo: re PUBLISH");
                                ownPresenceInfo.removeService(serviceTuple);
                                serviceTuple.version = string;
                                ownPresenceInfo.addService(serviceTuple);
                                this.mPresenceModule.removeMessages(1, Integer.valueOf(intValue));
                                PresenceModule presenceModule = this.mPresenceModule;
                                presenceModule.sendMessage(presenceModule.obtainMessage(1, Integer.valueOf(intValue)));
                            }
                        }
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException unused) {
            Log.e(LOG_TAG, "error retrieving msgapp(" + "" + ") details");
        }
    }

    public void onPackageUpdated(String str) {
        if (TextUtils.isEmpty(str)) {
            IMSLog.i(LOG_TAG, "onPackageUpdated: invalid packageName");
        } else {
            sendEmptyMessage(40);
        }
    }

    public void onNetworkChanged(NetworkEvent networkEvent, int i) {
        this.mCapabilityUtil.onNetworkChanged(this.mContext, networkEvent, i, this.mAvailablePhoneId, this.mImsRegInfoList, this.mNetworkEvent.get(Integer.valueOf(i)));
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        super.onRegistered(imsRegistration);
        this.mCapabilityRegistration.onRegistered(this.mContext, imsRegistration, this.mImsRegInfoList, this.mLastCapExResult.get(Integer.valueOf(imsRegistration.getPhoneId())), this.mOldFeature.get(Integer.valueOf(imsRegistration.getPhoneId())).longValue());
    }

    public void stop() {
        super.stop();
        processStop();
    }

    private void processStop() {
        post(new CapabilityDiscoveryModule$$ExternalSyntheticLambda1(this));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$processStop$2() {
        Log.i(LOG_TAG, "processStop");
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            stopPollingTimer(i);
            if (this.mCapabilityUtil.isCapabilityDiscoveryDisabled(this.mContext, i)) {
                savePollTimestamp(LAST_SEEN_ACTIVE, i);
            }
        }
        this.mImsRegInfoList.clear();
        this.mPublishedServiceList.clear();
        for (Map.Entry<Integer, Capabilities> key : this.mOwnList.entrySet()) {
            Integer num = (Integer) key.getKey();
            Capabilities capabilities = this.mOwnList.get(num);
            if (capabilities.isAvailable()) {
                capabilities.setAvailiable(false);
                this.mOwnList.put(num, capabilities);
                notifyOwnCapabilitiesChanged(num.intValue());
            }
        }
        this.mContactList.stop();
        this.mContactList.unregisterListener(this.mContactListener);
        deregisterSimCardEventListener();
        this.mTelephonyManager.unregisterTelephonyCallback(this.mTelephonyCallback);
    }

    public void onDeregistered(ImsRegistration imsRegistration, int i) {
        super.onDeregistered(imsRegistration, i);
        this.mCapabilityRegistration.onDeregistered(imsRegistration, this.mImsRegInfoList);
    }

    public void onDeregistering(ImsRegistration imsRegistration) {
        super.onDeregistering(imsRegistration);
        this.mCapabilityRegistration.onDeregistering(imsRegistration, this.mImsRegInfoList);
    }

    public void onSimChanged(int i) {
        IMSLog.i(LOG_TAG, i, "onSimChanged: clear cache and init poll timer");
        clearCapabilitiesCache(i);
        this.mInitialQuery.put(i, Boolean.TRUE);
        if (!isRunning()) {
            IMSLog.i(LOG_TAG, i, "onSimChanged: isRunning() is false.");
        } else if (this.mControl.get(i) != null && this.mControl.get(i).isReadyToRequest(i)) {
            sendMessage(obtainMessage(3, Integer.valueOf(i)));
        }
    }

    public void onCallStateChanged(int i, List<ICall> list) {
        if (this.mImsRegInfoList.containsKey(Integer.valueOf(i))) {
            this.mCapabilityForIncall.processCallStateChanged(i, new CopyOnWriteArrayList(list), this.mImsRegInfoList);
        } else {
            this.mCapabilityForIncall.processCallStateChangedOnDeregi(i, new CopyOnWriteArrayList(list));
        }
    }

    public void setCallNumber(int i, String str) {
        this.mCallNumber[i] = str;
    }

    /* access modifiers changed from: package-private */
    public void onOwnCapabilitiesChanged(int i) {
        this.mCapabilityUpdate.onOwnCapabilitiesChanged(i);
    }

    public void updateOwnCapabilities(int i) {
        this.mCapabilityUpdate.updateOwnCapabilities(this.mContext, this.mImsRegInfoList, i, this.mIsConfiguredOnCapability.get(Integer.valueOf(i)).booleanValue(), this.mNetworkType.get(Integer.valueOf(i)).intValue());
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        Log.i(LOG_TAG, "handleMessage: evt " + message.what);
        if (!isRunning() && message.what != 15) {
            Log.i(LOG_TAG, "CapabilityDiscoveryModule disabled.");
        } else if (!CapabilityEvent.handleEvent(message, this, this.mCapabilityUtil, this.mServiceAvailabilityEventListenerWrapper)) {
            Log.e(LOG_TAG, "handleMessage: unknown event " + message.what);
        }
    }

    public void handleModuleChannelRequest(Message message) {
        int i = message.what;
        if (i == 8001) {
            enableFeature(((Long) message.obj).longValue(), false);
            sendModuleResponse(message, 1, (Object) null);
        } else if (i == 8002) {
            disableFeature(((Long) message.obj).longValue(), false);
            sendModuleResponse(message, 1, (Object) null);
        }
    }

    public void onImsConifgChanged(int i, String str) {
        IMSLog.i(LOG_TAG, i, "onChange: config changed : " + str);
        removeMessages(7, Integer.valueOf(i));
        sendMessageDelayed(obtainMessage(7, Integer.valueOf(i)), 600);
    }

    public void registerListener(ICapabilityServiceEventListener iCapabilityServiceEventListener, int i) {
        this.mCapabilityServiceEventListener.registerListener(iCapabilityServiceEventListener, i);
    }

    /* access modifiers changed from: package-private */
    public void unregisterListener(ICapabilityServiceEventListener iCapabilityServiceEventListener, int i) {
        this.mCapabilityServiceEventListener.unregisterListener(iCapabilityServiceEventListener, i);
    }

    /* access modifiers changed from: package-private */
    public void enableFeature(long j, boolean z) {
        Log.i(LOG_TAG, "enableFeature: forced " + z + " feature " + Capabilities.dumpFeature(j));
        for (Map.Entry<Integer, Capabilities> key : this.mOwnList.entrySet()) {
            int intValue = ((Integer) key.getKey()).intValue();
            Capabilities capabilities = this.mOwnList.get(Integer.valueOf(intValue));
            capabilities.addFeature(j);
            this.mOwnList.put(Integer.valueOf(intValue), capabilities);
            if (isRunning() && z) {
                removeMessages(5, Integer.valueOf(intValue));
                sendMessageDelayed(obtainMessage(5, 1, 0, Integer.valueOf(intValue)), 500);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void disableFeature(long j, boolean z) {
        Log.i(LOG_TAG, "disableFeature: forced " + z + " feature " + Capabilities.dumpFeature(j));
        for (Map.Entry<Integer, Capabilities> key : this.mOwnList.entrySet()) {
            int intValue = ((Integer) key.getKey()).intValue();
            Capabilities capabilities = this.mOwnList.get(Integer.valueOf(intValue));
            capabilities.removeFeature(j);
            this.mOwnList.put(Integer.valueOf(intValue), capabilities);
            if (isRunning() && z) {
                removeMessages(5, Integer.valueOf(intValue));
                sendMessageDelayed(obtainMessage(5, 1, 0, Integer.valueOf(intValue)), 500);
            }
        }
    }

    public Capabilities getCapabilities(int i, int i2) {
        return this.mCapabilityQuery.getCapabilities(i, i2);
    }

    public Capabilities getCapabilities(String str, CapabilityRefreshType capabilityRefreshType, boolean z, int i) {
        return this.mCapabilityQuery.getCapabilities(str, capabilityRefreshType, z, i, this.mRcsProfile.get(Integer.valueOf(i)));
    }

    public Capabilities getCapabilities(String str, long j, int i) {
        return this.mCapabilityQuery.getCapabilities(str, j, i, this.mRcsProfile.get(Integer.valueOf(i)));
    }

    public Capabilities getCapabilities(ImsUri imsUri, long j, int i) {
        return this.mCapabilityQuery.getCapabilities(imsUri, j, i, this.mRcsProfile.get(Integer.valueOf(i)));
    }

    public Capabilities[] getCapabilities(List<ImsUri> list, CapabilityRefreshType capabilityRefreshType, long j, int i) {
        return this.mCapabilityQuery.getCapabilities(list, capabilityRefreshType, j, i, this.mRcsProfile.get(Integer.valueOf(i)), (RcsCapabilityExchangeImplBase.SubscribeResponseCallback) null);
    }

    public Capabilities[] getCapabilities(List<ImsUri> list, CapabilityRefreshType capabilityRefreshType, long j, int i, RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback) {
        return this.mCapabilityQuery.getCapabilities(list, capabilityRefreshType, j, i, this.mRcsProfile.get(Integer.valueOf(i)), subscribeResponseCallback);
    }

    public Capabilities getCapabilities(ImsUri imsUri, CapabilityRefreshType capabilityRefreshType, int i) {
        return this.mCapabilityQuery.getCapabilities(imsUri, capabilityRefreshType, i, this.mRcsProfile.get(Integer.valueOf(i)));
    }

    public Capabilities[] getCapabilitiesByContactId(String str, CapabilityRefreshType capabilityRefreshType, int i) {
        return this.mCapabilityQuery.getCapabilitiesByContactId(str, capabilityRefreshType, i, this.mRcsProfile.get(Integer.valueOf(i)));
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x00a4  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00c8  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void publishCapabilities(java.lang.String r7, android.telephony.ims.stub.RcsCapabilityExchangeImplBase.PublishResponseCallback r8, int r9) {
        /*
            r6 = this;
            java.lang.String r0 = "publishCapabilities"
            java.lang.String r1 = "CapabilityDiscModule"
            com.sec.internal.log.IMSLog.i(r1, r9, r0)
            android.content.Context r0 = r6.mContext
            boolean r0 = com.sec.internal.ims.rcs.util.RcsUtils.isImsSingleRegiRequired(r0, r9)
            r2 = 9
            r3 = -1
            if (r0 == 0) goto L_0x009b
            android.content.Context r0 = r6.mContext
            boolean r0 = com.sec.internal.ims.util.ConfigUtil.isGoogDmaPackageInuse(r0, r9)
            if (r0 != 0) goto L_0x001d
            goto L_0x009b
        L_0x001d:
            com.sec.internal.ims.servicemodules.options.CapabilityUtil r0 = r6.mCapabilityUtil
            boolean r0 = r0.checkModuleReady(r9)
            if (r0 == 0) goto L_0x0094
            com.sec.internal.ims.servicemodules.options.CapabilityUtil r0 = r6.mCapabilityUtil
            android.content.Context r4 = r6.mContext
            boolean r0 = r0.isCheckRcsSwitch(r4)
            if (r0 != 0) goto L_0x0030
            goto L_0x0094
        L_0x0030:
            java.util.Map<java.lang.Integer, com.sec.ims.ImsRegistration> r0 = r6.mImsRegInfoList
            java.lang.Integer r4 = java.lang.Integer.valueOf(r9)
            boolean r0 = r0.containsKey(r4)
            if (r0 != 0) goto L_0x0043
            java.lang.String r0 = "publishCapabilities: do not publish, already deregistered"
            com.sec.internal.log.IMSLog.i(r1, r9, r0)
            goto L_0x00a1
        L_0x0043:
            java.util.Map<java.lang.Integer, java.util.Set<java.lang.String>> r0 = r6.mPublishedServiceList
            java.lang.Integer r4 = java.lang.Integer.valueOf(r9)
            boolean r0 = r0.containsKey(r4)
            if (r0 == 0) goto L_0x0092
            java.util.Map<java.lang.Integer, java.util.Set<java.lang.String>> r0 = r6.mPublishedServiceList
            java.lang.Integer r4 = java.lang.Integer.valueOf(r9)
            java.lang.Object r0 = r0.get(r4)
            java.util.Set r0 = (java.util.Set) r0
            java.util.Map<java.lang.Integer, com.sec.ims.ImsRegistration> r4 = r6.mImsRegInfoList
            java.lang.Integer r5 = java.lang.Integer.valueOf(r9)
            java.lang.Object r4 = r4.get(r5)
            com.sec.ims.ImsRegistration r4 = (com.sec.ims.ImsRegistration) r4
            java.util.Set r4 = r4.getServices()
            boolean r0 = r0.equals(r4)
            if (r0 == 0) goto L_0x0092
            com.sec.internal.constants.Mno r0 = com.sec.internal.helper.SimUtil.getSimMno(r9)
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.TMOUS
            if (r0 != r4) goto L_0x0089
            android.content.Context r0 = r6.mContext
            com.sec.internal.interfaces.ims.core.IRegistrationManager r4 = r6.mRegMan
            com.sec.ims.settings.ImsProfile$PROFILE_TYPE r5 = com.sec.ims.settings.ImsProfile.PROFILE_TYPE.VOLTE
            com.sec.ims.settings.ImsProfile r4 = r4.getImsProfile(r9, r5)
            boolean r0 = com.sec.internal.ims.util.ImsUtil.needForceRegiOrPublishForMmtelCallComposer(r0, r4, r9)
            if (r0 != 0) goto L_0x0092
        L_0x0089:
            java.lang.String r0 = "publishCapabilities: do not publish, service list is same"
            com.sec.internal.log.IMSLog.i(r1, r9, r0)
            r0 = 10
            goto L_0x00a2
        L_0x0092:
            r0 = r3
            goto L_0x00a2
        L_0x0094:
            java.lang.String r0 = "publishCapabilities: do not publish, not ready"
            com.sec.internal.log.IMSLog.i(r1, r9, r0)
            goto L_0x00a1
        L_0x009b:
            java.lang.String r0 = "publishCapabilities: do not publish, single regi is not available"
            com.sec.internal.log.IMSLog.i(r1, r9, r0)
        L_0x00a1:
            r0 = r2
        L_0x00a2:
            if (r0 == r3) goto L_0x00c8
            if (r0 != r2) goto L_0x00a9
            r6.removePublishedServiceList(r9)
        L_0x00a9:
            r8.onCommandError(r0)     // Catch:{ ImsException -> 0x00ad }
            goto L_0x00c7
        L_0x00ad:
            r6 = move-exception
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "publishCapabilities: failed: "
            r7.append(r8)
            java.lang.String r6 = r6.getMessage()
            r7.append(r6)
            java.lang.String r6 = r7.toString()
            com.sec.internal.log.IMSLog.e(r1, r9, r6)
        L_0x00c7:
            return
        L_0x00c8:
            java.util.Map<java.lang.Integer, com.sec.ims.ImsRegistration> r0 = r6.mImsRegInfoList
            java.lang.Integer r1 = java.lang.Integer.valueOf(r9)
            boolean r0 = r0.containsKey(r1)
            if (r0 == 0) goto L_0x00e7
            java.util.Map<java.lang.Integer, com.sec.ims.ImsRegistration> r0 = r6.mImsRegInfoList
            java.lang.Integer r1 = java.lang.Integer.valueOf(r9)
            java.lang.Object r0 = r0.get(r1)
            com.sec.ims.ImsRegistration r0 = (com.sec.ims.ImsRegistration) r0
            java.util.Set r0 = r0.getServices()
            r6.setPublishedServiceList(r9, r0)
        L_0x00e7:
            com.sec.internal.ims.servicemodules.presence.PresenceModule r0 = r6.mPresenceModule
            r0.addPublishResponseCallback(r9, r8)
            com.sec.internal.ims.servicemodules.presence.PresenceModule r6 = r6.mPresenceModule
            r8 = 15
            r0 = 0
            android.os.Message r7 = r6.obtainMessage(r8, r9, r0, r7)
            r6.sendMessage(r7)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule.publishCapabilities(java.lang.String, android.telephony.ims.stub.RcsCapabilityExchangeImplBase$PublishResponseCallback, int):void");
    }

    public void subscribeForCapabilities(Collection<Uri> collection, RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback, int i) {
        IMSLog.i(LOG_TAG, i, "subscribeForCapabilities");
        ArrayList arrayList = new ArrayList();
        for (Uri valueOf : collection) {
            arrayList.add(ImsUri.parse(String.valueOf(valueOf)));
        }
        getCapabilities(arrayList, CapabilityRefreshType.ALWAYS_FORCE_REFRESH, Capabilities.FEATURE_IM_SERVICE, i, subscribeResponseCallback);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v1, resolved type: java.util.List} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendOptionsCapabilityRequest(android.net.Uri r9, java.util.Set<java.lang.String> r10, android.telephony.ims.stub.RcsCapabilityExchangeImplBase.OptionsResponseCallback r11, int r12) {
        /*
            r8 = this;
            java.lang.String r0 = "CapabilityDiscModule"
            java.lang.String r1 = "sendOptionsCapabilityRequest"
            com.sec.internal.log.IMSLog.i(r0, r12, r1)
            if (r9 == 0) goto L_0x0089
            java.lang.String r9 = java.lang.String.valueOf(r9)
            com.sec.ims.util.ImsUri r9 = com.sec.ims.util.ImsUri.parse(r9)
            if (r9 == 0) goto L_0x0089
            com.sec.internal.helper.PhoneIdKeyMap<java.util.HashMap<com.sec.ims.util.ImsUri, java.util.List<android.telephony.ims.stub.RcsCapabilityExchangeImplBase$OptionsResponseCallback>>> r6 = r8.mOptionsCallbacks
            monitor-enter(r6)
            com.sec.internal.helper.PhoneIdKeyMap<java.util.HashMap<com.sec.ims.util.ImsUri, java.util.List<android.telephony.ims.stub.RcsCapabilityExchangeImplBase$OptionsResponseCallback>>> r0 = r8.mOptionsCallbacks     // Catch:{ all -> 0x0086 }
            java.lang.Object r0 = r0.get(r12)     // Catch:{ all -> 0x0086 }
            java.util.HashMap r0 = (java.util.HashMap) r0     // Catch:{ all -> 0x0086 }
            if (r0 != 0) goto L_0x002b
            java.util.HashMap r0 = new java.util.HashMap     // Catch:{ all -> 0x0086 }
            r0.<init>()     // Catch:{ all -> 0x0086 }
            com.sec.internal.helper.PhoneIdKeyMap<java.util.HashMap<com.sec.ims.util.ImsUri, java.util.List<android.telephony.ims.stub.RcsCapabilityExchangeImplBase$OptionsResponseCallback>>> r1 = r8.mOptionsCallbacks     // Catch:{ all -> 0x0086 }
            r1.put(r12, r0)     // Catch:{ all -> 0x0086 }
        L_0x002b:
            boolean r1 = r0.containsKey(r9)     // Catch:{ all -> 0x0086 }
            if (r1 != 0) goto L_0x0050
            java.util.ArrayList r7 = new java.util.ArrayList     // Catch:{ all -> 0x0086 }
            r7.<init>()     // Catch:{ all -> 0x0086 }
            r7.add(r11)     // Catch:{ all -> 0x0086 }
            r0.put(r9, r7)     // Catch:{ all -> 0x0086 }
            com.sec.internal.ims.servicemodules.options.CapabilityExchange r0 = r8.mCapabilityExchange     // Catch:{ all -> 0x0086 }
            com.sec.internal.interfaces.ims.core.IRegistrationManager r4 = r8.mRegMan     // Catch:{ all -> 0x0086 }
            java.util.Map<java.lang.Integer, com.sec.ims.ImsRegistration> r5 = r8.mImsRegInfoList     // Catch:{ all -> 0x0086 }
            r1 = r9
            r2 = r12
            r3 = r10
            boolean r8 = r0.sendOptionsRequest(r1, r2, r3, r4, r5)     // Catch:{ all -> 0x0086 }
            if (r8 != 0) goto L_0x005d
            r8 = 1
            com.sec.internal.ims.servicemodules.options.CapabilityUtil.reportErrorToApp(r11, r8)     // Catch:{ all -> 0x0086 }
            goto L_0x005d
        L_0x0050:
            java.lang.Object r8 = r0.get(r9)     // Catch:{ all -> 0x0086 }
            r7 = r8
            java.util.List r7 = (java.util.List) r7     // Catch:{ all -> 0x0086 }
            r7.add(r11)     // Catch:{ all -> 0x0086 }
            r0.put(r9, r7)     // Catch:{ all -> 0x0086 }
        L_0x005d:
            java.lang.String r8 = "CapabilityDiscModule"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x0086 }
            r10.<init>()     // Catch:{ all -> 0x0086 }
            java.lang.String r11 = "sendOptionsCapabilityRequest uri: "
            r10.append(r11)     // Catch:{ all -> 0x0086 }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x0086 }
            r10.append(r9)     // Catch:{ all -> 0x0086 }
            java.lang.String r9 = " registered callbacks: "
            r10.append(r9)     // Catch:{ all -> 0x0086 }
            int r9 = r7.size()     // Catch:{ all -> 0x0086 }
            r10.append(r9)     // Catch:{ all -> 0x0086 }
            java.lang.String r9 = r10.toString()     // Catch:{ all -> 0x0086 }
            com.sec.internal.log.IMSLog.s(r8, r12, r9)     // Catch:{ all -> 0x0086 }
            monitor-exit(r6)     // Catch:{ all -> 0x0086 }
            goto L_0x008d
        L_0x0086:
            r8 = move-exception
            monitor-exit(r6)     // Catch:{ all -> 0x0086 }
            throw r8
        L_0x0089:
            r8 = 2
            com.sec.internal.ims.servicemodules.options.CapabilityUtil.reportErrorToApp(r11, r8)
        L_0x008d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule.sendOptionsCapabilityRequest(android.net.Uri, java.util.Set, android.telephony.ims.stub.RcsCapabilityExchangeImplBase$OptionsResponseCallback, int):void");
    }

    public void exchangeCapabilities(String str, long j, int i, String str2) {
        this.mCapabilityExchange.exchangeCapabilities(this.mImsRegInfoList, this.mRegMan, str, j, i, str2, this.mCallNumber[i]);
    }

    public void exchangeCapabilitiesForVSH(int i, boolean z) {
        this.mCapabilityForIncall.exchangeCapabilitiesForVSH(i, z, this.mImsRegInfoList);
    }

    /* access modifiers changed from: package-private */
    public void onAdsChanged() {
        int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
        if (adsChangedCheckRcsSwitch(activeDataPhoneId) && SimUtil.isDualIMS() && RcsUtils.DualRcs.isRegAllowed(this.mContext, activeDataPhoneId)) {
            IMSLog.i(LOG_TAG, activeDataPhoneId, "onAdsChanged: trigger syncContact");
            syncContact();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean adsChangedCheckRcsSwitch(int i) {
        if (this.mActiveDataPhoneId == i) {
            Log.i(LOG_TAG, "Current default phoneId = " + this.mActiveDataPhoneId);
            return true;
        }
        this.mActiveDataPhoneId = i;
        this.mAvailablePhoneId = i;
        if (i == -1) {
            this.mAvailablePhoneId = 0;
        }
        if (!this.mCapabilityUtil.isCheckRcsSwitch(this.mContext)) {
            stop();
            return false;
        }
        if (isReady()) {
            start();
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setUserActive(boolean z, int i) {
        Log.i(LOG_TAG, "IPC successful user activity" + z);
        if (z) {
            this.mUserLastActive.put(i, Long.valueOf(LAST_SEEN_ACTIVE));
        } else {
            this.mUserLastActive.put(i, Long.valueOf(System.currentTimeMillis()));
        }
        saveUserLastActiveTimeStamp(System.currentTimeMillis(), i);
        Log.i(LOG_TAG, "IPC successful user activity: " + this.mUserLastActive);
    }

    /* access modifiers changed from: package-private */
    public void addFakeCapabilityInfo(List<ImsUri> list, boolean z, int i) {
        if (list != null) {
            IMSLog.s(LOG_TAG, "addFakeCapabilityInfo: uri " + list.toString());
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("URIS", new ArrayList(list));
            bundle.putLong("FEATURES", z ? Capabilities.FEATURE_ALL : (long) Capabilities.FEATURE_OFFLINE_RCS_USER);
            bundle.putInt("PHONEID", i);
            sendMessage(obtainMessage(4, CapabilityConstants.CapExResult.SUCCESS.ordinal(), -1, bundle));
        }
    }

    public void clearCapabilitiesCache(int i) {
        IMSLog.i(LOG_TAG, i, "clearCapabilitiesCache");
        synchronized (this.mCapabilitiesMapList) {
            this.mCapabilitiesMapList.get(Integer.valueOf(i)).clear();
        }
        savePollTimestamp(LAST_SEEN_ACTIVE, i);
        this.mContactList.resetRefreshTime(i);
    }

    public void changeParalysed(boolean z, int i) {
        this.mCapabilityUtil.changeParalysed(z, i);
    }

    public ImsUri getNetworkPreferredUri(ImsUri imsUri) {
        return this.mCapabilityUtil.getNetworkPreferredUri(imsUri);
    }

    public int isCapDiscEnabled(int i) {
        int i2 = 0;
        if (isCapConfigAvailable(i) && this.mConfigs.get(i).getDefaultDisc() != 2) {
            i2 = 1;
        }
        IMSLog.i(LOG_TAG, i, "isCapDiscEnabled: " + i2);
        return i2;
    }

    public int getCapCacheExpiry(int i) {
        int capCacheExpiry = isCapConfigAvailable(i) ? (int) this.mConfigs.get(i).getCapCacheExpiry() : 0;
        IMSLog.i(LOG_TAG, i, "getCapCacheExpiry: " + capCacheExpiry);
        return capCacheExpiry;
    }

    public int getCapInfoExpiry(int i) {
        int capInfoExpiry = isCapConfigAvailable(i) ? this.mConfigs.get(i).getCapInfoExpiry() : 0;
        IMSLog.i(LOG_TAG, i, "getCapInfoExpiry: " + capInfoExpiry);
        return capInfoExpiry;
    }

    public int getServiceAvailabilityInfoExpiry(int i) {
        int serviceAvailabilityInfoExpiry = isCapConfigAvailable(i) ? this.mConfigs.get(i).getServiceAvailabilityInfoExpiry() : 0;
        IMSLog.i(LOG_TAG, i, "getServiceAvailabilityInfoExpiry: " + serviceAvailabilityInfoExpiry);
        return serviceAvailabilityInfoExpiry;
    }

    public int getCapPollInterval(int i) {
        int pollingPeriod = isCapConfigAvailable(i) ? this.mConfigs.get(i).getPollingPeriod() : 0;
        IMSLog.i(LOG_TAG, i, "getCapPollInterval: " + pollingPeriod);
        return pollingPeriod;
    }

    private boolean isCapConfigAvailable(int i) {
        boolean z = RcsUtils.isImsSingleRegiRequired(this.mContext, i) && this.mCapabilityUtil.checkModuleReady(i) && this.mCapabilityUtil.isCheckRcsSwitch(this.mContext) && this.mConfigs.get(i) != null;
        IMSLog.i(LOG_TAG, i, "isCapConfigAvailable: " + z);
        return z;
    }

    private void notifyProvisionedValue(int i) {
        if (isCapConfigAvailable(i)) {
            IMSLog.i(LOG_TAG, i, "notify provisioned value");
            SecImsNotifier.getInstance().notifyProvisionedIntValueChanged(i, 17, this.mConfigs.get(i).getDefaultDisc() != 2 ? 1 : 0);
            SecImsNotifier.getInstance().notifyProvisionedIntValueChanged(i, 18, this.mConfigs.get(i).getCapInfoExpiry());
            SecImsNotifier.getInstance().notifyProvisionedIntValueChanged(i, 19, this.mConfigs.get(i).getServiceAvailabilityInfoExpiry());
            SecImsNotifier.getInstance().notifyProvisionedIntValueChanged(i, 20, this.mConfigs.get(i).getPollingPeriod());
            if (SimUtil.getSimMno(i) == Mno.VZW && !ImsConstants.RCS_AS.JIBE.equals(ConfigUtil.getAcsServerType(i)) && this.mConfigs.get(i).getDefaultDisc() != 2) {
                SecImsNotifier.getInstance().notifyProvisionedIntValueChanged(i, 25, DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_EAB_SETTING, Boolean.FALSE, i).booleanValue() ? 1 : 0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Capabilities[] getAllCapabilities(int i) {
        return this.mCapabilityQuery.getAllCapabilities(i);
    }

    public Capabilities getOwnCapabilitiesBase(int i) {
        return this.mCapabilityQuery.getOwnCapabilitiesBase(i, this.mOwnList.get(Integer.valueOf(i)));
    }

    public Capabilities getOwnCapabilities() {
        return getOwnCapabilities(this.mActiveDataPhoneId);
    }

    public Capabilities getOwnCapabilities(int i) {
        return this.mCapabilityQuery.getOwnCapabilities(i, this.mAvailablePhoneId, this.mImsRegInfoList, this.mRegMan, this.mNetworkType.get(Integer.valueOf(i)).intValue(), this.mIsInCall, this.mCallNumber[i], this.mOwnList.get(Integer.valueOf(i)));
    }

    public void setOwnCapabilities(int i, boolean z) {
        this.mCapabilityUpdate.setOwnCapabilities(i, z, this.mImsRegInfoList, this.mNetworkType.get(Integer.valueOf(i)).intValue(), this.mIsInCall, this.mCallNumber[i]);
    }

    /* access modifiers changed from: package-private */
    public void prepareResponse(List<ImsUri> list, long j, String str, int i, String str2) {
        this.mCapabilityUpdate.prepareResponse(this.mContext, list, j, str, i, str2, this.mImsRegInfoList, this.mNetworkType.get(Integer.valueOf(i)).intValue(), this.mCallNumber[i]);
    }

    public boolean hasVideoOwnCapability(int i) {
        return this.mHasVideoOwn.get(Integer.valueOf(i)).booleanValue();
    }

    public ContactCache getPhonebook() {
        return this.mContactList;
    }

    public CapabilitiesCache getCapabilitiesCache() {
        return getCapabilitiesCache(this.mAvailablePhoneId);
    }

    public CapabilitiesCache getCapabilitiesCache(int i) {
        CapabilitiesCache capabilitiesCache;
        synchronized (this.mCapabilitiesMapList) {
            capabilitiesCache = this.mCapabilitiesMapList.get(Integer.valueOf(i));
        }
        return capabilitiesCache;
    }

    /* access modifiers changed from: package-private */
    public CapabilityConfig getCapabilityConfig(int i) {
        return this.mConfigs.get(i);
    }

    /* access modifiers changed from: package-private */
    public ICapabilityExchangeControl getCapabilityControl(int i) {
        return this.mControl.get(i);
    }

    /* access modifiers changed from: package-private */
    public void putCapabilityControlForOptionsModule(int i, OptionsModule optionsModule) {
        this.mControl.put(i, optionsModule);
    }

    public UriGenerator getUriGenerator() {
        return this.mUriGenerator;
    }

    public PresenceModule getPresenceModule() {
        return this.mPresenceModule;
    }

    /* access modifiers changed from: package-private */
    public void onContactChanged(boolean z) {
        CapabilityUpdate capabilityUpdate = this.mCapabilityUpdate;
        int i = this.mAvailablePhoneId;
        capabilityUpdate.onContactChanged(z, i, this.isOfflineAddedContact.get(i).booleanValue(), this.mLastListSubscribeStamp.get(this.mAvailablePhoneId).longValue());
    }

    public int requestCapabilityExchange(Set<ImsUri> set, CapabilityConstants.RequestType requestType, int i, int i2) {
        return this.mCapabilityExchange.requestCapabilityExchange(set, requestType, i, i2);
    }

    public boolean requestCapabilityExchange(ImsUri imsUri, CapabilityConstants.RequestType requestType, boolean z, int i, int i2) {
        CapabilityExchange capabilityExchange = this.mCapabilityExchange;
        IRegistrationManager iRegistrationManager = this.mRegMan;
        Map<Integer, ImsRegistration> map = this.mImsRegInfoList;
        String str = this.mCallNumber[i];
        int intValue = this.mNetworkType.get(Integer.valueOf(i)).intValue();
        return capabilityExchange.requestCapabilityExchange(imsUri, requestType, z, i, this.mOwnList.get(Integer.valueOf(i)), iRegistrationManager, map, str, intValue, i2);
    }

    /* access modifiers changed from: package-private */
    public boolean updatePollList(ImsUri imsUri, boolean z, int i) {
        return this.mCapabilityExchange.updatePollList(this.mUrisToRequestList.get(Integer.valueOf(i)), imsUri, z, i);
    }

    /* access modifiers changed from: package-private */
    public boolean isPollingInProgress(int i) {
        return this.mCapabilityUpdate.isPollingInProgress(i, this.mPollingHistory.get(i));
    }

    /* access modifiers changed from: package-private */
    public void requestInitialCapabilitiesQuery(int i) {
        this.mCapabilityExchange.requestInitialCapabilitiesQuery(i, this.mInitialQuery.get(i).booleanValue(), this.mLastPollTimestamp.get(i).longValue());
    }

    /* access modifiers changed from: package-private */
    public void startPoll(int i) {
        long longValue = (this.mLastPollTimestamp.get(i).longValue() + (((long) this.mConfigs.get(i).getPollingPeriod()) * 1000)) - new Date().getTime();
        if (longValue > LAST_SEEN_ACTIVE) {
            sendMessage(obtainMessage(1, Integer.valueOf(i)));
            startPollingTimer(longValue, i);
            return;
        }
        sendMessage(obtainMessage(18, 0, 0, Integer.valueOf(i)));
    }

    public void poll(boolean z, boolean z2, int i) {
        this.mCapabilityExchange.poll(this.mContext, z, z2, i, this.mImsRegInfoList, this.mPollingHistory.get(i));
    }

    public boolean setLegacyLatching(ImsUri imsUri, boolean z, int i) {
        return this.mCapabilityUpdate.setLegacyLatching(this.mContext, imsUri, z, i);
    }

    /* access modifiers changed from: package-private */
    public void onUpdateCapabilities(List<ImsUri> list, long j, CapabilityConstants.CapExResult capExResult, String str, int i, List<ImsUri> list2, int i2, boolean z, String str2) {
        int i3 = i2;
        this.mCapabilityUpdate.onUpdateCapabilities(list, j, capExResult, str, i, list2, i2, z, str2, this.mCallNumber[i3]);
        if (j != ((long) Capabilities.FEATURE_OFFLINE_RCS_USER)) {
            for (ImsUri removeUriFromLatchingList : list) {
                if (this.mImModule.getLatchingProcessor().removeUriFromLatchingList(removeUriFromLatchingList, i3)) {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyOwnCapabilitiesChanged(int i) {
        this.mBackgroundHandler.post(new CapabilityDiscoveryModule$$ExternalSyntheticLambda2(this, i));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifyOwnCapabilitiesChanged$3(int i) {
        IMSLog.i(LOG_TAG, i, "notifyOwnCapabilitiesChanged:");
        this.mCapabilityServiceEventListener.notifyOwnCapabilitiesChanged(i);
    }

    /* access modifiers changed from: package-private */
    public void notifyCapabilitiesChanged(List<ImsUri> list, Capabilities capabilities, int i) {
        this.mBackgroundHandler.post(new CapabilityDiscoveryModule$$ExternalSyntheticLambda3(this, i, list, capabilities));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifyCapabilitiesChanged$4(int i, List list, Capabilities capabilities) {
        IMSLog.i(LOG_TAG, i, "notifyCapabilitiesChanged:");
        String str = this.mCallNumber[i];
        if (str != null) {
            this.mActiveCallRemoteUri = UriUtil.parseNumber(str);
        }
        this.mCapabilityServiceEventListener.notifyCapabilitiesChanged(list, capabilities, this.mActiveCallRemoteUri, i);
    }

    /* access modifiers changed from: package-private */
    public void startPollingTimer(int i) {
        long pollingPeriod = ((long) this.mConfigs.get(i).getPollingPeriod()) * 1000;
        if (RcsPolicyManager.getRcsStrategy(i).boolSetting(RcsPolicySettings.RcsPolicy.USE_RAND_DELAY_PERIODIC_POLL)) {
            pollingPeriod = this.mCapabilityUtil.getRandomizedDelayForPeriodicPolling(i, pollingPeriod);
        }
        if (pollingPeriod != LAST_SEEN_ACTIVE) {
            startPollingTimer(pollingPeriod, i);
        }
    }

    /* access modifiers changed from: package-private */
    public void startPollingTimer(long j, int i) {
        if (this.mPollingIntent.get(i) != null) {
            stopPollingTimer(i);
        }
        IMSLog.i(LOG_TAG, i, "startPollingTimer: millis " + j);
        Intent intent = new Intent("com.sec.internal.ims.servicemodules.options.poll_timeout");
        intent.setPackage(this.mContext.getPackageName());
        this.mPollingIntent.put(i, PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432));
        AlarmTimer.start(this.mContext, this.mPollingIntent.get(i), j);
    }

    /* access modifiers changed from: package-private */
    public void stopPollingTimer(int i) {
        IMSLog.i(LOG_TAG, i, "stopPollingTimer");
        if (this.mPollingIntent.get(i) != null) {
            AlarmTimer.stop(this.mContext, this.mPollingIntent.get(i));
            this.mPollingIntent.put(i, null);
        }
    }

    /* access modifiers changed from: package-private */
    public long loadPollTimestamp(int i) {
        Context context = this.mContext;
        long j = context.getSharedPreferences("capdiscovery_" + i, 0).getLong("pollTimestamp", LAST_SEEN_ACTIVE);
        if (j <= new Date().getTime()) {
            return j;
        }
        IMSLog.i(LOG_TAG, i, "loadPollTimestamp: abnormal case, clear lastPollTime " + j + " to 0");
        savePollTimestamp(LAST_SEEN_ACTIVE, i);
        return LAST_SEEN_ACTIVE;
    }

    /* access modifiers changed from: package-private */
    public void savePollTimestamp(long j, int i) {
        Context context = this.mContext;
        SharedPreferences sharedPreferences = context.getSharedPreferences("capdiscovery_" + i, 0);
        this.mLastPollTimestamp.put(i, Long.valueOf(j));
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putLong("pollTimestamp", j);
        edit.apply();
    }

    /* access modifiers changed from: package-private */
    public void startPartialPollingTimer(long j, boolean z, int i) {
        if (this.mPartialPollingIntent.get(i) != null) {
            stopPartialPollingTimer(i);
        }
        IMSLog.i(LOG_TAG, i, "startPartialPollingTimer: millis " + j);
        Intent intent = new Intent("com.sec.internal.ims.servicemodules.options.poll_partial");
        intent.putExtra("force", z);
        intent.setPackage(this.mContext.getPackageName());
        this.mPartialPollingIntent.put(i, PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432));
        AlarmTimer.start(this.mContext, this.mPartialPollingIntent.get(i), j);
    }

    /* access modifiers changed from: package-private */
    public void stopPartialPollingTimer(int i) {
        if (this.mPartialPollingIntent.get(i) != null) {
            IMSLog.i(LOG_TAG, i, "stopPartialPollingTimer");
            AlarmTimer.stop(this.mContext, this.mPartialPollingIntent.get(i));
            this.mPartialPollingIntent.put(i, null);
        }
    }

    private void saveUserLastActiveTimeStamp(long j, int i) {
        Context context = this.mContext;
        SharedPreferences sharedPreferences = context.getSharedPreferences("capdiscovery_" + i, 0);
        IMSLog.i(LOG_TAG, i, "save last seen active");
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putLong("lastseenactive_" + SimManagerFactory.getImsiFromPhoneId(i), j);
        edit.apply();
    }

    /* access modifiers changed from: package-private */
    public void onImsSettingsUpdate(int i) {
        this.mCapabilityUtil.onImsSettingsUpdate(this.mContext, i);
    }

    /* access modifiers changed from: package-private */
    public void syncContact() {
        Log.i(LOG_TAG, "syncContact:");
        ISimManager simManager = SimManagerFactory.getSimManager();
        if (simManager == null || !simManager.isSimLoaded()) {
            Log.i(LOG_TAG, "syncContact: sim is not loaded.");
            this.mContactList.setIsBlockedContactChange(true);
            return;
        }
        _syncContact(simManager.getSimMno());
    }

    /* access modifiers changed from: package-private */
    public void _syncContact(Mno mno) {
        if (mno.isChn()) {
            Log.i(LOG_TAG, "Chn syncContact Block.");
            return;
        }
        CapabilityUpdate capabilityUpdate = this.mCapabilityUpdate;
        if (capabilityUpdate != null) {
            capabilityUpdate._syncContact(mno);
        }
    }

    public boolean isConfigured(int i) {
        return this.mIsConfigured.get(Integer.valueOf(i)).booleanValue();
    }

    /* access modifiers changed from: package-private */
    public boolean isOwnInfoPublished() {
        boolean isOwnCapPublished = this.mPresenceModule.isOwnCapPublished();
        Log.i(LOG_TAG, "isOwnInfoPublished: " + isOwnCapPublished);
        return isOwnCapPublished;
    }

    /* access modifiers changed from: package-private */
    public void registerService(String str, String str2) {
        Log.i(LOG_TAG, "registerService: called for vzw api layer");
        if (this.mControl.get(this.mActiveDataPhoneId) == null) {
            Log.i(LOG_TAG, "registerService: adding service tuple to list");
            ServiceTuple serviceTuple = ServiceTuple.getServiceTuple(str, str2, (String[]) null);
            synchronized (this.mServiceTupleList) {
                this.mServiceTupleList.add(serviceTuple);
            }
        } else if (this.mControl.get(this.mActiveDataPhoneId) == this.mPresenceModule) {
            Log.i(LOG_TAG, "registerService: calling presence module api");
            this.mControl.get(this.mActiveDataPhoneId).registerService(str, str2, this.mActiveDataPhoneId);
        }
    }

    /* access modifiers changed from: package-private */
    public void deRegisterService(List<String> list) {
        Log.i(LOG_TAG, "deRegisterService: called for vzw api layer");
        if (this.mControl.get(this.mActiveDataPhoneId) != null && this.mControl.get(this.mActiveDataPhoneId) == this.mPresenceModule) {
            this.mControl.get(this.mActiveDataPhoneId).deRegisterService(list, this.mActiveDataPhoneId);
        }
    }

    /* access modifiers changed from: package-private */
    public void loadThirdPartyServiceTuples(int i) {
        PresenceModule presenceModule;
        Log.i(LOG_TAG, "loadThirdPartyServiceTuples");
        if (this.mControl.get(i) != null && this.mControl.get(i) == (presenceModule = this.mPresenceModule)) {
            presenceModule.loadThirdPartyServiceTuples(this.mServiceTupleList);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyEABServiceAdvertiseResult(int i, int i2) {
        Log.i(LOG_TAG, "notifyEABServiceAdvertiseResult: error[" + i + "]");
        this.mCapabilityServiceEventListener.notifyEABServiceAdvertiseResult(i, i2);
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(LOG_TAG);
        this.mContactList.dump();
        this.mEventLog.dump();
        for (CapabilityConfig next : this.mConfigs.values()) {
            if (next != null) {
                IMSLog.dump(LOG_TAG, next.toString());
            }
        }
        try {
            getCapabilitiesCache().dump();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        IMSLog.decreaseIndent(LOG_TAG);
    }

    class TelephonyCallbackForCapability extends TelephonyCallback implements TelephonyCallback.CallStateListener, TelephonyCallback.DataConnectionStateListener {
        int mNetworkType = 0;

        TelephonyCallbackForCapability() {
        }

        public void onCallStateChanged(int i) {
            Log.i(CapabilityDiscoveryModule.LOG_TAG, "onCallStateChanged: " + i);
            if (!CapabilityDiscoveryModule.this.mIsInCall && i != 0) {
                CapabilityDiscoveryModule.this.mIsInCall = true;
                if (hasCshFeature(CapabilityDiscoveryModule.this.mActiveDataPhoneId)) {
                    fetchCapabilities(CapabilityDiscoveryModule.this.mActiveDataPhoneId);
                }
            } else if (CapabilityDiscoveryModule.this.mIsInCall && i == 0) {
                CapabilityDiscoveryModule.this.mIsInCall = false;
                if (hasCshFeature(CapabilityDiscoveryModule.this.mActiveDataPhoneId)) {
                    fetchCapabilities(CapabilityDiscoveryModule.this.mActiveDataPhoneId);
                }
            }
        }

        public void onDataConnectionStateChanged(int i, int i2) {
            CapabilityDiscoveryModule.this.post(new CapabilityDiscoveryModule$TelephonyCallbackForCapability$$ExternalSyntheticLambda0(this, i2, i));
        }

        /* access modifiers changed from: private */
        public /* synthetic */ void lambda$onDataConnectionStateChanged$0(int i, int i2) {
            if (i != this.mNetworkType) {
                this.mNetworkType = i;
                if (hasCshFeature(CapabilityDiscoveryModule.this.mActiveDataPhoneId) && CapabilityDiscoveryModule.this.mIsInCall) {
                    Log.i(CapabilityDiscoveryModule.LOG_TAG, "onDataConnectionStateChanged(): state=" + i2 + ", networkType=" + i);
                    fetchCapabilities(CapabilityDiscoveryModule.this.mActiveDataPhoneId);
                }
            }
        }

        private boolean hasCshFeature(int i) {
            return ((Capabilities) CapabilityDiscoveryModule.this.mOwnList.get(Integer.valueOf(i))).hasFeature(Capabilities.FEATURE_ISH) || ((Capabilities) CapabilityDiscoveryModule.this.mOwnList.get(Integer.valueOf(i))).hasFeature(Capabilities.FEATURE_VSH);
        }

        /* access modifiers changed from: package-private */
        public void fetchCapabilities(int i) {
            CapabilityDiscoveryModule.this.post(new CapabilityDiscoveryModule$TelephonyCallbackForCapability$$ExternalSyntheticLambda1(this, i));
        }

        /* access modifiers changed from: private */
        public /* synthetic */ void lambda$fetchCapabilities$1(int i) {
            CapabilityDiscoveryModule.this.updateOwnCapabilities(i);
            CapabilityDiscoveryModule.this.setOwnCapabilities(i, true);
        }
    }

    class SimEventListener implements ISimEventListener {
        SimEventListener() {
        }

        public void onReady(int i, boolean z) {
            String str;
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
            Mno simMno = simManagerFromSimSlot == null ? Mno.DEFAULT : simManagerFromSimSlot.getSimMno();
            if (simManagerFromSimSlot == null) {
                str = "";
            } else {
                str = simManagerFromSimSlot.getImsi();
            }
            Context context = CapabilityDiscoveryModule.this.mContext;
            SharedPreferences sharedPreferences = context.getSharedPreferences("capdiscovery_" + i, 0);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            if (!TextUtils.equals(sharedPreferences.getString("imsi", ""), str)) {
                Log.i(CapabilityDiscoveryModule.LOG_TAG, "imsi changed");
                CapabilityDiscoveryModule.this.clearCapabilitiesCache(i);
                edit.putString("imsi", str);
            }
            IMSLog.s(CapabilityDiscoveryModule.LOG_TAG, i, "SimEventListener,onReady,EVT_SYNC_CONTACT");
            edit.apply();
            CapabilityDiscoveryModule.this.mAvailablePhoneId = SimUtil.getActiveDataPhoneId();
            if (CapabilityDiscoveryModule.this.mAvailablePhoneId == -1) {
                CapabilityDiscoveryModule.this.mAvailablePhoneId = 0;
            }
            if (!RcsUtils.DualRcs.isDualRcsReg() || CapabilityDiscoveryModule.this.mAvailablePhoneId == i) {
                CapabilityDiscoveryModule capabilityDiscoveryModule = CapabilityDiscoveryModule.this;
                capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(10, simMno), RegistrationGovernor.RETRY_AFTER_PDNLOST_MS);
                return;
            }
            IMSLog.s(CapabilityDiscoveryModule.LOG_TAG, i, "SimEventListener : contact sync of opposite sim is blocked.");
        }
    }

    public void exchangeCapabilitiesForVSHOnRegi(boolean z, int i) {
        sendMessageDelayed(obtainMessage(14, i, 0, Boolean.valueOf(z)), 500);
    }

    /* access modifiers changed from: package-private */
    public void triggerCapexForIncallRegiDeregi(int i, ImsRegistration imsRegistration) {
        this.mCapabilityForIncall.triggerCapexForIncallRegiDeregi(i, imsRegistration);
    }

    public void updateCapabilities(int i) {
        IMSLog.i(LOG_TAG, i, "updateCapabilities");
        getServiceModuleManager().updateCapabilities(i);
    }

    public ImsFeature.Capabilities queryCapabilityStatus(int i) {
        IMSLog.i(LOG_TAG, i, "queryCapabilityStatus");
        ImsFeature.Capabilities capabilities = new ImsFeature.Capabilities();
        if (isUceServiceAvailable(i, "options")) {
            capabilities.addCapabilities(1);
        }
        if (isUceServiceAvailable(i, SipMsg.EVENT_PRESENCE)) {
            capabilities.addCapabilities(2);
        }
        return capabilities;
    }

    public boolean isUceServiceAvailable(int i, String str) {
        if (!this.mCapabilityUtil.checkModuleReady(i) || !this.mCapabilityUtil.isCheckRcsSwitch(this.mContext)) {
            IMSLog.i(LOG_TAG, i, "isUceServiceAvailable: not ready");
            return false;
        }
        ImsRegistration imsRegistration = getImsRegistration(i);
        if (imsRegistration != null) {
            return imsRegistration.hasService(str);
        }
        IMSLog.i(LOG_TAG, i, "isUceServiceAvailable: regiInfo is null");
        return false;
    }

    /* access modifiers changed from: package-private */
    public void onBootCompleted() {
        if (this.mCapabilityUtil.isPhoneLockState(this.mContext)) {
            Log.i(LOG_TAG, "onBootCompleted : not required sync contact");
            return;
        }
        ISimManager simManager = SimManagerFactory.getSimManager();
        if (simManager == null || !simManager.getSimMno().isChn()) {
            Log.i(LOG_TAG, "onBootCompleted: try sync contact");
            this.mRetrySyncContactCount = 0;
            sendMessage(obtainMessage(13));
            return;
        }
        Log.i(LOG_TAG, "Chn syncContact Block.");
    }

    public void onDefaultSmsPackageChanged() {
        Log.i(LOG_TAG, "onDefaultSmsPackageChanged");
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            ICapabilityExchangeControl iCapabilityExchangeControl = this.mControl.get(i);
            PresenceModule presenceModule = this.mPresenceModule;
            if (iCapabilityExchangeControl == presenceModule) {
                presenceModule.sendMessage(presenceModule.obtainMessage(19));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onRetrySyncContact() {
        this.mCapabilityUpdate.onRetrySyncContact(this.mRetrySyncContactCount);
    }

    /* access modifiers changed from: package-private */
    public void handleDelayedSetOwnCapabilities(int i) {
        Log.i(LOG_TAG, "handleMessage: EVT_DELAYED_SET_OWN_CAPABILITIES");
        if (this.mPresenceModule.getRegiInfoUpdater(i)) {
            Log.i(LOG_TAG, "EVT_SET_OWN_CAPABILITIES : setting own capabilities");
            removeMessages(5, Integer.valueOf(i));
            removeMessages(53, Integer.valueOf(i));
            sendMessage(obtainMessage(5, 0, 0, Integer.valueOf(i)));
            this.mPresenceModule.setRegiInfoUpdater(i, false);
            return;
        }
        Log.i(LOG_TAG, "EVT_DELAYED_SET_OWN_CAPABILITIES : Delayed for a while");
        removeMessages(53, Integer.valueOf(i));
        sendMessageDelayed(obtainMessage(53, 0, 0, Integer.valueOf(i)), 100);
    }

    /* access modifiers changed from: package-private */
    public void deleteNonRcsDataFromContactDB(int i) {
        this.mEventLog.logAndAdd(i, "deleteNonRcsDataFromContactDB");
        getCapabilitiesCache(i).deleteNonRcsDataFromContactDB();
    }

    private void initContactCache(int i) {
        synchronized (this.mCapabilitiesMapList) {
            for (int i2 = 0; i2 < i; i2++) {
                this.mCapabilitiesMapList.put(Integer.valueOf(i2), new CapabilitiesCache(this.mContext, i2));
            }
            this.mContactList = new ContactCache(this.mContext, this.mCapabilitiesMapList);
        }
    }

    private void loadCapabilityStorage() {
        synchronized (this.mCapabilitiesMapList) {
            for (Map.Entry<Integer, CapabilitiesCache> key : this.mCapabilitiesMapList.entrySet()) {
                this.mCapabilitiesMapList.get((Integer) key.getKey()).loadCapabilityStorage();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void registerSimCardEventListener() {
        if (this.mSimEventListener == null) {
            this.mSimEventListener = new SimEventListener();
            Log.d("??", "wtf 1");
            if (RcsUtils.DualRcs.isDualRcsReg()) {
                for (ISimManager registerSimCardEventListener : SimManagerFactory.getAllSimManagers()) {
                    registerSimCardEventListener.registerSimCardEventListener(this.mSimEventListener);
                }
                return;
            }
            SimManagerFactory.getSimManager().registerSimCardEventListener(this.mSimEventListener);
        }
    }

    /* access modifiers changed from: package-private */
    public void deregisterSimCardEventListener() {
        if (this.mSimEventListener != null) {
            if (RcsUtils.DualRcs.isDualRcsReg()) {
                for (ISimManager deRegisterSimCardEventListener : SimManagerFactory.getAllSimManagers()) {
                    deRegisterSimCardEventListener.deRegisterSimCardEventListener(this.mSimEventListener);
                }
            } else {
                SimManagerFactory.getSimManager().deRegisterSimCardEventListener(this.mSimEventListener);
            }
            this.mSimEventListener = null;
        }
    }

    public boolean checkSenderCapability(ImsUri imsUri) {
        Capabilities capabilities = (Capabilities) Optional.ofNullable(getCapabilitiesCache()).map(new CapabilityDiscoveryModule$$ExternalSyntheticLambda4(imsUri)).orElse((Object) null);
        return (capabilities == null || capabilities.getFeature() == ((long) Capabilities.FEATURE_NON_RCS_USER)) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public void notifyOptionsResponseToAOSP(ImsUri imsUri, int i, String str, List<String> list, int i2) {
        IMSLog.s(LOG_TAG, "notifyOptionsResponseToAOSP: " + imsUri + " respCode: " + i + " reason: " + str + " caps: " + list);
        synchronized (this.mOptionsCallbacks) {
            HashMap hashMap = this.mOptionsCallbacks.get(i2);
            if (hashMap != null && hashMap.containsKey(imsUri)) {
                List<RcsCapabilityExchangeImplBase.OptionsResponseCallback> list2 = (List) hashMap.get(imsUri);
                IMSLog.s(LOG_TAG, "notifyOptionsResponseToAOSP: callbacks size" + list2.size());
                for (RcsCapabilityExchangeImplBase.OptionsResponseCallback onNetworkResponse : list2) {
                    try {
                        onNetworkResponse.onNetworkResponse(i, str, list);
                    } catch (ImsException e) {
                        IMSLog.e(LOG_TAG, "ImsException" + e.getMessage());
                    }
                }
                hashMap.remove(imsUri);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setIsOfflineAddedContact(boolean z, int i) {
        this.isOfflineAddedContact.put(i, Boolean.valueOf(z));
    }

    /* access modifiers changed from: package-private */
    public void setNetworkEvent(NetworkEvent networkEvent, int i) {
        this.mNetworkEvent.put(Integer.valueOf(i), networkEvent);
    }

    /* access modifiers changed from: package-private */
    public void setNetworkClass(int i, int i2) {
        this.mNetworkClass.put(Integer.valueOf(i2), Integer.valueOf(i));
    }

    /* access modifiers changed from: package-private */
    public void setNetworkType(int i, int i2) {
        this.mNetworkType.put(Integer.valueOf(i2), Integer.valueOf(i));
    }

    /* access modifiers changed from: package-private */
    public void setForcePollingGuard(boolean z, int i) {
        this.forcePollingGuard.put(i, Boolean.valueOf(z));
    }

    /* access modifiers changed from: package-private */
    public void addPollingHistory(Date date, int i) {
        List list = this.mPollingHistory.get(i);
        list.add(date);
        this.mPollingHistory.put(i, list);
    }

    /* access modifiers changed from: package-private */
    public void setLastListSubscribeStamp(long j, int i) {
        this.mLastListSubscribeStamp.put(i, Long.valueOf(j));
    }

    /* access modifiers changed from: package-private */
    public void setInitialQuery(boolean z, int i) {
        this.mInitialQuery.put(i, Boolean.valueOf(z));
    }

    /* access modifiers changed from: package-private */
    public Map<Integer, Capabilities> getOwnList() {
        return this.mOwnList;
    }

    /* access modifiers changed from: package-private */
    public void putOwnList(int i, Capabilities capabilities) {
        this.mOwnList.put(Integer.valueOf(i), capabilities);
    }

    /* access modifiers changed from: package-private */
    public void setIsConfigured(boolean z, int i) {
        this.mIsConfigured.put(Integer.valueOf(i), Boolean.valueOf(z));
    }

    /* access modifiers changed from: package-private */
    public void setIsConfiguredOnCapability(boolean z, int i) {
        this.mIsConfiguredOnCapability.put(Integer.valueOf(i), Boolean.valueOf(z));
    }

    /* access modifiers changed from: package-private */
    public void putUrisToRequestList(int i, Set<ImsUri> set) {
        this.mUrisToRequestList.put(Integer.valueOf(i), set);
    }

    /* access modifiers changed from: package-private */
    public Map<Integer, Set<ImsUri>> getUrisToRequest() {
        return this.mUrisToRequestList;
    }

    /* access modifiers changed from: package-private */
    public OptionsModule getOptionsModule() {
        return this.mOptionsModule;
    }

    /* access modifiers changed from: package-private */
    public boolean getForcePollingGuard(int i) {
        return this.forcePollingGuard.get(i).booleanValue();
    }

    /* access modifiers changed from: package-private */
    public void setHasVideoOwnCapability(boolean z, int i) {
        this.mHasVideoOwn.put(Integer.valueOf(i), Boolean.valueOf(z));
    }

    /* access modifiers changed from: package-private */
    public PendingIntent getPollingIntent(int i) {
        return this.mPollingIntent.get(i);
    }

    /* access modifiers changed from: package-private */
    public PendingIntent getThrottledIntent(int i) {
        return this.mThrottledIntent.get(i);
    }

    /* access modifiers changed from: package-private */
    public void setThrottledIntent(PendingIntent pendingIntent, int i) {
        this.mThrottledIntent.put(i, pendingIntent);
    }

    /* access modifiers changed from: package-private */
    public void setLastCapExResult(CapabilityConstants.CapExResult capExResult, int i) {
        this.mLastCapExResult.put(Integer.valueOf(i), capExResult);
    }

    /* access modifiers changed from: package-private */
    public void setUriGenerator(UriGenerator uriGenerator) {
        this.mUriGenerator = uriGenerator;
    }

    /* access modifiers changed from: package-private */
    public void setOldFeature(long j, int i) {
        this.mOldFeature.put(Integer.valueOf(i), Long.valueOf(j));
    }

    /* access modifiers changed from: package-private */
    public int getActiveDataPhoneId() {
        return this.mActiveDataPhoneId;
    }

    /* access modifiers changed from: package-private */
    public void setAvailablePhoneId(int i) {
        this.mAvailablePhoneId = i;
    }

    /* access modifiers changed from: package-private */
    public long getUserLastActive(int i) {
        return this.mUserLastActive.get(i).longValue();
    }

    /* access modifiers changed from: package-private */
    public void putUserLastActive(int i, long j) {
        this.mUserLastActive.put(i, Long.valueOf(j));
    }

    /* access modifiers changed from: package-private */
    public void removeImsRegInfoList(int i) {
        this.mImsRegInfoList.remove(Integer.valueOf(i));
    }

    /* access modifiers changed from: package-private */
    public void setImsRegInfoList(int i, ImsRegistration imsRegistration) {
        this.mImsRegInfoList.put(Integer.valueOf(i), imsRegistration);
    }

    /* access modifiers changed from: package-private */
    public void setRetrySyncContactCount(int i) {
        this.mRetrySyncContactCount = i;
    }

    /* access modifiers changed from: package-private */
    public void settOptionsSwitch(int i, boolean z) {
        this.mOptionsSwitchOnList.put(Integer.valueOf(i), Boolean.valueOf(z));
    }

    /* access modifiers changed from: package-private */
    public void setPresenceSwitch(int i, boolean z) {
        this.mPresenceSwitchOnList.put(Integer.valueOf(i), Boolean.valueOf(z));
    }

    /* access modifiers changed from: package-private */
    public void setCapabilityModuleOn(boolean z) {
        this.mCapabilityModuleOn = z;
    }

    public void removePublishedServiceList(int i) {
        IMSLog.i(LOG_TAG, i, "removePublishedServiceList");
        this.mPublishedServiceList.remove(Integer.valueOf(i));
    }

    public void setPublishedServiceList(int i, Set<String> set) {
        IMSLog.i(LOG_TAG, i, "setPublishedServiceList: " + set);
        this.mPublishedServiceList.put(Integer.valueOf(i), set);
    }

    /* access modifiers changed from: package-private */
    public IImModule getImModule() {
        return this.mImModule;
    }
}
