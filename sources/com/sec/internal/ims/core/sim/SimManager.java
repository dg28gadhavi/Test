package com.sec.internal.ims.core.sim;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SemSystemProperties;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.IccCardConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.ImsAutoUpdate;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.ims.util.CscParser;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimEventListener;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class SimManager extends Handler implements ISimManager {
    protected static final int EVENT_ADS_CHANGED = 6;
    protected static final int EVENT_IMSSWITCH_UPDATED = 7;
    protected static final int EVENT_LOAD_MNOMAP = 8;
    protected static final int EVENT_SIM_REFRESH = 3;
    protected static final int EVENT_SIM_STATE_CHANGED = 1;
    protected static final int EVENT_SOFTPHONE_AUTH_FAILED = 5;
    protected static final int EVENT_UICC_CHANGED = 2;
    private static final String LOG_TAG = "SimManager";
    private static final String SMF_MNONAME_PROP = "sys.smf.mnoname";
    protected static final String SOFTPHONE_OPERATOR_CODE = "310999";
    static final Uri URI_UPDATE_GLOBAL = Uri.parse("content://com.sec.ims.settings/global");
    static final Uri URI_UPDATE_MNO = Uri.parse("content://com.sec.ims.settings/mno");
    private static final String sInteractAcrossUsersFullPermission = "android.permission.INTERACT_ACROSS_USERS_FULL";
    String OMCNW_CODE;
    String OMC_CODE;
    final BroadcastReceiver mAkaEventReceiver;
    Context mContext;
    Mno mDevMno;
    private final List<ISimEventListener> mEventListeners;
    SimpleEventLog mEventLog;
    final BroadcastReceiver mGtsAppInstallReceiver;
    private String mHighestPriorityEhplmn = "";
    protected ContentObserver mImsServiceSwitchObserver;
    private String mImsi = "";
    String mImsiFromImpi;
    boolean mIsCrashSimEvent = false;
    private boolean mIsGtsAppInstalled;
    boolean mIsGuestMode = false;
    private boolean mIsOutBoundSIM;
    private boolean mIsRefresh = false;
    protected boolean mIsimLoaded;
    boolean mLabSimCard;
    private String mLastImsi = null;
    MnoInfoStorage mMnoInfoStorage;
    MnoMap mMnoMap;
    Mno mNetMno;
    String mOperatorFromImpi;
    SimDataAdaptor mSimDataAdaptor = null;
    BroadcastReceiver mSimIntentReceiver;
    private String mSimMnoName;
    protected final RegistrantList mSimReadyRegistrants;
    protected final RegistrantList mSimRefreshRegistrants;
    protected final RegistrantList mSimRemovedRegistrants;
    /* access modifiers changed from: private */
    public int mSimSlot = 0;
    SimConstants.SIM_STATE mSimState;
    protected RegistrantList mSimStateChangedRegistrants;
    SimConstants.SIM_STATE mSimStatePrev;
    SimConstants.SoftphoneAccount mSoftphoneAccount;
    int mSubscriptionId = -1;
    ITelephonyManager mTelephonyManager;
    protected final RegistrantList mUiccChangedRegistrants;
    protected boolean notifySimReadyAlreadyDone = false;

    public SimManager(Looper looper, Context context, int i, SubscriptionInfo subscriptionInfo, ITelephonyManager iTelephonyManager) {
        super(looper);
        Mno mno = Mno.DEFAULT;
        this.mDevMno = mno;
        this.mNetMno = mno;
        this.mSimMnoName = "";
        this.mLabSimCard = false;
        this.mMnoMap = null;
        SimConstants.SIM_STATE sim_state = SimConstants.SIM_STATE.UNKNOWN;
        this.mSimStatePrev = sim_state;
        this.mSimState = sim_state;
        this.mIsimLoaded = false;
        this.mIsOutBoundSIM = false;
        this.mIsGtsAppInstalled = false;
        this.mSimReadyRegistrants = new RegistrantList();
        this.mUiccChangedRegistrants = new RegistrantList();
        this.mSimRefreshRegistrants = new RegistrantList();
        this.mSimRemovedRegistrants = new RegistrantList();
        this.mSimStateChangedRegistrants = new RegistrantList();
        this.mImsServiceSwitchObserver = null;
        this.mEventListeners = new ArrayList();
        this.mSimIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int intExtra = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, -1);
                int intExtra2 = intent.getIntExtra(PhoneConstants.PHONE_KEY, 0);
                int r2 = SimManager.this.mSimSlot;
                IMSLog.i(SimManager.LOG_TAG, r2, "SimIntentReceiver: received action " + action + " subId=" + intExtra + " mSubId=" + SimManager.this.mSubscriptionId);
                if (ImsConstants.Intents.ACTION_SIM_ICCID_CHANGED.equals(action)) {
                    SimManager.this.sendEmptyMessage(2);
                } else if (intExtra2 != SimManager.this.mSimSlot) {
                    int r7 = SimManager.this.mSimSlot;
                    IMSLog.i(SimManager.LOG_TAG, r7, "phoneId mismatch : " + action + ", " + intExtra2 + ", " + SimManager.this.mSimSlot);
                } else {
                    if (SimManager.this.mSubscriptionId < 0 && intExtra != Integer.MAX_VALUE) {
                        if (SimUtil.isMultiSimSupported() && !ImsConstants.Intents.ACTION_SIM_REFRESH.equals(action) && !"android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                            ImsConstants.Intents.ACTION_SIM_ISIM_LOADED.equals(action);
                        }
                        SimManager.this.mSubscriptionId = intExtra;
                    }
                    if ("android.intent.action.SIM_STATE_CHANGED".equals(action) || ImsConstants.Intents.ACTION_SIM_REFRESH_FAIL_RECOVERY.equals(action)) {
                        SimManager simManager = SimManager.this;
                        simManager.sendMessage(simManager.obtainMessage(1, intent.getStringExtra("ss")));
                    } else if (ImsConstants.Intents.ACTION_SIM_ISIM_LOADED.equals(action) && SimManagerUtils.isISimAppPresent(SimManager.this.mSimSlot, SimManager.this.mTelephonyManager)) {
                        SimManager simManager2 = SimManager.this;
                        simManager2.sendMessage(simManager2.obtainMessage(1, IccCardConstants.INTENT_VALUE_ICC_ISIM_LOADED));
                    } else if (ImsConstants.Intents.ACTION_SIM_REFRESH.equals(action) && !SimManager.this.hasVsim()) {
                        SimManager.this.sendEmptyMessage(3);
                    }
                }
            }
        };
        this.mGtsAppInstallReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (TextUtils.equals(intent.getData().getSchemeSpecificPart(), "com.google.android.gts.telephony")) {
                    action.hashCode();
                    if (!action.equals("android.intent.action.PACKAGE_REMOVED")) {
                        if (action.equals("android.intent.action.PACKAGE_ADDED") && !SimManager.this.getGtsAppInstalled()) {
                            Log.w(SimManager.LOG_TAG, "ADD GTS package, SendMessage SIM LOAD again");
                            SimManager.this.setGtsAppInstalled(true);
                            SimManager simManager = SimManager.this;
                            simManager.mSimState = SimConstants.SIM_STATE.UNKNOWN;
                            simManager.sendMessage(simManager.obtainMessage(1, IccCardConstants.INTENT_VALUE_ICC_LOADED));
                        }
                    } else if (SimManager.this.getGtsAppInstalled()) {
                        Log.w(SimManager.LOG_TAG, "Remove GTS package, SendMessage SIM LOAD again");
                        SimManager.this.setGtsAppInstalled(false);
                        SimManager simManager2 = SimManager.this;
                        simManager2.mSimState = SimConstants.SIM_STATE.UNKNOWN;
                        simManager2.sendMessage(simManager2.obtainMessage(1, IccCardConstants.INTENT_VALUE_ICC_LOADED));
                    }
                }
            }
        };
        this.mAkaEventReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int r0 = SimManager.this.mSimSlot;
                IMSLog.i(SimManager.LOG_TAG, r0, "Intent received : " + action);
                int r02 = SimManager.this.mSimSlot;
                IMSLog.i(SimManager.LOG_TAG, r02, "id : " + intent.getIntExtra("id", -1));
                SimManager simManager = SimManager.this;
                if (simManager.mSoftphoneAccount == null) {
                    IMSLog.e(SimManager.LOG_TAG, simManager.mSimSlot, "mSoftphoneAccount is null, so skip handling");
                    return;
                }
                if ("com.sec.imsservice.AKA_CHALLENGE_COMPLETE".equals(action)) {
                    int intExtra = intent.getIntExtra("id", -1);
                    SimManager simManager2 = SimManager.this;
                    if (intExtra == simManager2.mSoftphoneAccount.mId) {
                        simManager2.onSoftphoneAuthDone(intent.getStringExtra("result"));
                        return;
                    }
                }
                if ("com.sec.imsservice.AKA_CHALLENGE_FAILED".equals(action)) {
                    SimManager.this.onSoftphoneAuthDone("");
                }
            }
        };
        this.mEventLog = new SimpleEventLog(context, i, LOG_TAG, 300);
        this.mContext = context;
        this.mSimSlot = i;
        this.mTelephonyManager = iTelephonyManager;
        this.mMnoInfoStorage = new MnoInfoStorage();
        int i2 = this.mSimSlot;
        IMSLog.i(LOG_TAG, i2, "subId: " + this.mSubscriptionId + ", info: " + subscriptionInfo);
        if (subscriptionInfo != null) {
            this.mSubscriptionId = subscriptionInfo.getSubscriptionId();
            setSubscriptionInfo(subscriptionInfo);
        }
        String str = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY, NSDSNamespaces.NSDSSimAuthType.UNKNOWN);
        this.OMC_CODE = str;
        if (!NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equals(str)) {
            this.mDevMno = Mno.fromSalesCode(this.OMC_CODE);
        }
        String nWCode = OmcCode.getNWCode(this.mSimSlot);
        this.OMCNW_CODE = nWCode;
        this.mNetMno = Mno.fromSalesCode(nWCode);
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i3 = this.mSimSlot;
        simpleEventLog.logAndAdd(i3, "OMC_CODE(create): " + this.OMC_CODE + ", mDevMno: " + this.mDevMno.toString());
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        int i4 = this.mSimSlot;
        simpleEventLog2.logAndAdd(i4, "OMCNW_CODE(create): " + this.OMCNW_CODE + ", mNetMno: " + this.mNetMno.toString());
        setSimMno(this.mNetMno, false);
        this.mImsServiceSwitchObserver = new ImsServiceSwitchObserver(this);
        this.mContext.getContentResolver().registerContentObserver(ImsConstants.SystemSettings.IMS_SWITCHES.getUri(), false, this.mImsServiceSwitchObserver);
    }

    class ImsServiceSwitchObserver extends ContentObserver {
        public ImsServiceSwitchObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean z, Uri uri) {
            IMSLog.i(SimManager.LOG_TAG, SimManager.this.mSimSlot, "ImsServiceSwitch updated.");
            if (uri != null) {
                int simSlotFromUri = UriUtil.getSimSlotFromUri(uri);
                if (simSlotFromUri != SimManager.this.mSimSlot) {
                    IMSLog.i(SimManager.LOG_TAG, SimManager.this.mSimSlot, "phoneId mismatch, No need to update");
                    return;
                }
                SimManager simManager = SimManager.this;
                simManager.sendMessage(simManager.obtainMessage(7, Integer.valueOf(simSlotFromUri)));
            }
        }
    }

    public void onImsSwitchUpdated(int i) {
        SharedPreferences sharedPref = ImsSharedPrefHelper.getSharedPref(i, this.mContext, "imsswitch", 0, false);
        ContentValues contentValues = new ContentValues();
        Arrays.asList(new String[]{ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_SMS_IP, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VIDEO_CALL, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE}).forEach(new SimManager$$ExternalSyntheticLambda0(contentValues, sharedPref));
        this.mMnoInfoStorage.update(contentValues);
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i2 = this.mSimSlot;
        simpleEventLog.logAndAdd(i2, this.mSimState + ", " + this.mSimMnoName + ", onImsSwitchUpdated : " + contentValues);
    }

    /* access modifiers changed from: package-private */
    public void updateGlobalSetting(int i) {
        int i2 = this.mMnoInfoStorage.getInt(ISimManager.KEY_IMSSWITCH_TYPE, 0);
        if (i2 != 0 && i2 != 2) {
            boolean z = this.mMnoInfoStorage.getBoolean(ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, false);
            boolean z2 = this.mMnoInfoStorage.getBoolean(ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE, false);
            boolean z3 = this.mMnoInfoStorage.getBoolean(ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI, false);
            if (!z || (!z2 && !z3)) {
                IMSLog.i(LOG_TAG, i, "updateGlobalSetting: enableIms or enableServiceVolte, enableServiceVowifi : disable");
                ContentValues contentValues = new ContentValues();
                if (getDevMno().isAus()) {
                    contentValues.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 3);
                    contentValues.put(GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN, "PS");
                } else {
                    contentValues.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 1);
                    contentValues.put(GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN, "CS");
                }
                contentValues.put(GlobalSettingsConstants.SS.DOMAIN, "CS_ALWAYS");
                contentValues.put(GlobalSettingsConstants.Call.USSD_DOMAIN, "CS");
                Uri.Builder buildUpon = URI_UPDATE_GLOBAL.buildUpon();
                this.mContext.getContentResolver().update(buildUpon.fragment("simslot" + i).build(), contentValues, (String) null, (String[]) null);
            }
        }
    }

    public void initSequentially() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.setPriority(1000);
        this.mContext.registerReceiver(this.mSimIntentReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(ImsConstants.Intents.ACTION_SIM_ICCID_CHANGED);
        intentFilter2.addAction(ImsConstants.Intents.ACTION_SIM_REFRESH);
        intentFilter2.addAction(ImsConstants.Intents.ACTION_SIM_ISIM_LOADED);
        intentFilter2.addAction(ImsConstants.Intents.ACTION_SIM_REFRESH_FAIL_RECOVERY);
        this.mContext.registerReceiver(this.mSimIntentReceiver, intentFilter2);
        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter3.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter3.addDataScheme("package");
        this.mContext.registerReceiver(this.mGtsAppInstallReceiver, intentFilter3);
        if (this.mContext.checkSelfPermission(sInteractAcrossUsersFullPermission) == 0) {
            IntentFilter intentFilter4 = new IntentFilter();
            intentFilter4.addAction("android.intent.action.USER_BACKGROUND");
            intentFilter4.addAction("android.intent.action.USER_FOREGROUND");
            IntentFilter intentFilter5 = new IntentFilter();
            intentFilter5.addAction("com.sec.imsservice.AKA_CHALLENGE_COMPLETE");
            intentFilter5.addAction("com.sec.imsservice.AKA_CHALLENGE_FAILED");
            ContextExt.registerReceiverAsUser(this.mContext.getApplicationContext(), this.mAkaEventReceiver, ContextExt.ALL, intentFilter5, (String) null, (Handler) null);
        }
        IMSLog.e(LOG_TAG, this.mSimSlot, "init mno map");
        sendEmptyMessage(8);
    }

    static class AuthRequest {
        Message response;

        AuthRequest() {
        }
    }

    public void registerForSimReady(Handler handler, int i, Object obj) {
        IMSLog.i(LOG_TAG, this.mSimSlot, "Register for sim ready");
        Registrant registrant = new Registrant(handler, i, obj);
        this.mSimReadyRegistrants.add(registrant);
        if (!this.notifySimReadyAlreadyDone) {
            return;
        }
        if (this.mSimState != SimConstants.SIM_STATE.UNKNOWN || SimManagerUtils.needImsUpOnUnknownState(this.mContext, this.mSimSlot)) {
            registrant.notifyResult(Integer.valueOf(this.mSimSlot));
        }
    }

    public void deregisterForSimReady(Handler handler) {
        this.mSimReadyRegistrants.remove(handler);
    }

    /* access modifiers changed from: package-private */
    public void notifySimReady(String str) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i = this.mSimSlot;
        simpleEventLog.logAndAdd(i, "notifySimReady: state [" + this.mSimState + "]");
        StringBuilder sb = new StringBuilder();
        sb.append(this.mSimSlot);
        sb.append(",NOTI SIM EVT");
        IMSLog.c(LogClass.SIM_NOTIFY_EVENT, sb.toString());
        boolean z = true;
        this.notifySimReadyAlreadyDone = true;
        Intent intent = new Intent(ImsConstants.Intents.ACTION_IMS_ON_SIMLOADED);
        intent.addFlags(32);
        IMSLog.i(LOG_TAG, this.mSimSlot, "send ACTION_IMS_ON_SIMLOADED");
        IntentUtil.sendBroadcast(this.mContext, intent);
        SimConstants.SIM_STATE sim_state = this.mSimState;
        SimConstants.SIM_STATE sim_state2 = SimConstants.SIM_STATE.LOADED;
        if (sim_state == sim_state2) {
            z = false;
        }
        if (z || this.mSimStatePrev != sim_state2) {
            this.mSimReadyRegistrants.notifyResult(Integer.valueOf(this.mSimSlot));
        } else {
            SimDataAdaptor simDataAdaptor = this.mSimDataAdaptor;
            if (simDataAdaptor != null && simDataAdaptor.needHandleLoadedAgain(str)) {
                int i2 = this.mSimSlot;
                IMSLog.i(LOG_TAG, i2, "SIM READY by needHandleLoadedAgain: " + str);
                this.mSimReadyRegistrants.notifyResult(Integer.valueOf(this.mSimSlot));
            }
        }
        synchronized (this.mEventListeners) {
            for (ISimEventListener onReady : this.mEventListeners) {
                onReady.onReady(this.mSimSlot, z);
            }
        }
    }

    public void registerForUiccChanged(Handler handler, int i, Object obj) {
        this.mUiccChangedRegistrants.add(new Registrant(handler, i, obj));
    }

    /* access modifiers changed from: package-private */
    public void notifyUiccChanged() {
        this.mUiccChangedRegistrants.notifyRegistrants();
    }

    public void registerForSimRefresh(Handler handler, int i, Object obj) {
        this.mSimRefreshRegistrants.add(new Registrant(handler, i, obj));
    }

    public void deregisterForSimRefresh(Handler handler) {
        this.mSimRefreshRegistrants.remove(handler);
    }

    private void notifySimRefresh() {
        this.mSimRefreshRegistrants.notifyResult(Integer.valueOf(this.mSimSlot));
    }

    public void registerForSimRemoved(Handler handler, int i, Object obj) {
        this.mSimRemovedRegistrants.add(new Registrant(handler, i, obj));
    }

    public void deregisterForSimRemoved(Handler handler) {
        this.mSimRemovedRegistrants.remove(handler);
    }

    private void notifySimRemoved() {
        this.mSimRemovedRegistrants.notifyResult(Integer.valueOf(this.mSimSlot));
    }

    public void registerForSimStateChanged(Handler handler, int i, Object obj) {
        this.mSimStateChangedRegistrants.add(new Registrant(handler, i, obj));
    }

    public void deregisterForSimStateChanged(Handler handler) {
        this.mSimStateChangedRegistrants.remove(handler);
    }

    /* access modifiers changed from: package-private */
    public void notifySimStateChanged() {
        this.mSimStateChangedRegistrants.notifyResult(Integer.valueOf(this.mSimSlot));
    }

    public boolean isSimAvailable() {
        int i = this.mSimSlot;
        IMSLog.i(LOG_TAG, i, "mSimState:" + this.mSimState + ", mIsimLoaded:" + this.mIsimLoaded + ", hasIsim():" + hasIsim());
        return this.mSimState == SimConstants.SIM_STATE.LOADED && (this.mIsimLoaded || !hasIsim());
    }

    public boolean hasNoSim() {
        return this.mSimState != SimConstants.SIM_STATE.LOADED;
    }

    public void setIsimLoaded() {
        onSimStateChange(IccCardConstants.INTENT_VALUE_ICC_ISIM_LOADED);
    }

    /* access modifiers changed from: package-private */
    public boolean isISimAppLoaded() {
        int i = this.mSimSlot;
        StringBuilder sb = new StringBuilder();
        sb.append("isISimAppLoaded : simstate ");
        sb.append(getSimState());
        sb.append(", subscriptionId ");
        sb.append(getSubscriptionId());
        sb.append(", isISimAppPresent ");
        sb.append(SimManagerUtils.isISimAppPresent(this.mSimSlot, this.mTelephonyManager));
        sb.append(", getboolean ");
        sb.append(!ImsRegistry.getBoolean(this.mSimSlot, GlobalSettingsConstants.Registration.BLOCK_REGI_ON_INVALID_ISIM, true));
        sb.append(", isISimDataValid() ");
        sb.append(isISimDataValid());
        IMSLog.d(LOG_TAG, i, sb.toString());
        if (getSimState() != 5 || getSubscriptionId() < 0 || !SimManagerUtils.isISimAppPresent(this.mSimSlot, this.mTelephonyManager)) {
            return false;
        }
        if (!ImsRegistry.getBoolean(this.mSimSlot, GlobalSettingsConstants.Registration.BLOCK_REGI_ON_INVALID_ISIM, true) || isISimDataValid()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean checkOutBoundSIM() {
        if (hasNoSim()) {
            IMSLog.i(LOG_TAG, this.mSimSlot, "isOutboundSim, SIM not ready");
            return false;
        } else if (isLabSimCard() || DeviceUtil.getGcfMode()) {
            IMSLog.i(LOG_TAG, this.mSimSlot, "isOutboundSim, GCF mode, LabSim card/ Test Bed SIM inserted.");
            return false;
        } else if (DeviceUtil.isUnifiedSalesCodeInTSS()) {
            return !DeviceUtil.includedSimByTSS(this.mSimMnoName);
        } else {
            return CollectionUtils.isNullOrEmpty((Collection<?>) getNetworkNames());
        }
    }

    public List<String> getNetworkNames() {
        String str;
        String str2;
        String groupIdLevel1 = this.mTelephonyManager.getGroupIdLevel1(getSubscriptionId());
        Mno simMno = getSimMno();
        boolean z = (TextUtils.isEmpty(groupIdLevel1) || groupIdLevel1.toUpperCase().startsWith("FF")) && simMno.isUSA();
        if (simMno == Mno.RJIL) {
            str2 = getSimOperatorFromImpi();
            str = getImsiFromImpi();
        } else {
            str2 = getSimOperator();
            str = getImsi();
        }
        int subId = SimUtil.getSubId(this.mSimSlot);
        return CscParser.getNetworkNames(str2, str, groupIdLevel1, this.mTelephonyManager.getGid2(subId), this.mTelephonyManager.getSimOperatorName(subId), this.mSimSlot, z);
    }

    public boolean hasIsim() {
        Mno simMno = getSimMno();
        String rilSimOperator = this.mTelephonyManager.getRilSimOperator(this.mSimSlot);
        String str = SemSystemProperties.get("ro.boot.hardware", "");
        boolean z = false;
        if (simMno == Mno.SKT && (("SKCTN".equals(rilSimOperator) || "SKCTD".equals(rilSimOperator)) && OmcCode.isKOROmcCode() && (str.contains("qcom") || str.contains("mt")))) {
            IMSLog.i(LOG_TAG, this.mSimSlot, "hasIsim: watch data SIM. treat it as USIM(by SKT operator)");
            return false;
        } else if (simMno == Mno.SAFARICOM_KENYA) {
            IMSLog.i(LOG_TAG, this.mSimSlot, "hasIsim safariCom_kenya : false");
            return false;
        } else {
            boolean isISimAppPresent = SimManagerUtils.isISimAppPresent(this.mSimSlot, this.mTelephonyManager);
            int i = this.mSimSlot;
            IMSLog.i(LOG_TAG, i, "hasIsim: [" + isISimAppPresent + "]");
            if (!ImsRegistry.getBoolean(this.mSimSlot, GlobalSettingsConstants.Registration.USE_USIM_ON_INVALID_ISIM, false) && !isEsim()) {
                return isISimAppPresent;
            }
            if (isISimAppPresent && (!this.mIsimLoaded || isISimDataValid())) {
                z = true;
            }
            return z;
        }
    }

    public boolean hasVsim() {
        return SimUtil.isSoftphoneEnabled();
    }

    public void setSimRefreshed() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "setSimRefreshed:");
    }

    public String getSimCountryIso() {
        return this.mTelephonyManager.getSimCountryIsoForSubId(SimUtil.getSubId(this.mSimSlot));
    }

    public String getSimOperator() {
        String mockOperatorCode = Mno.getMockOperatorCode();
        if (!TextUtils.isEmpty(mockOperatorCode)) {
            return mockOperatorCode;
        }
        if (SimUtil.isSoftphoneEnabled()) {
            return SOFTPHONE_OPERATOR_CODE;
        }
        String simOperator = this.mTelephonyManager.getSimOperator(getSubscriptionId());
        int i = this.mSimSlot;
        IMSLog.i(LOG_TAG, i, "getSimOperator: value [" + simOperator + "]");
        return simOperator;
    }

    public String getSimOperatorFromImpi() {
        if (TextUtils.isEmpty(this.mOperatorFromImpi)) {
            return getSimOperator();
        }
        return this.mOperatorFromImpi;
    }

    public boolean isLabSimCard() {
        int i = this.mSimSlot;
        IMSLog.i(LOG_TAG, i, "isLabSimCard: state [" + this.mSimState + "] isLabSim [" + this.mLabSimCard + "]");
        return this.mSimState == SimConstants.SIM_STATE.LOADED && this.mLabSimCard;
    }

    public boolean isOutBoundSIM() {
        int i = this.mSimSlot;
        IMSLog.i(LOG_TAG, i, "isOutBoundSIM: state [" + this.mSimState + "] isOutBoundSIM [" + this.mIsOutBoundSIM + "]");
        return this.mSimState == SimConstants.SIM_STATE.LOADED && this.mIsOutBoundSIM;
    }

    public boolean isGBASupported() {
        if (!hasIsim()) {
            return false;
        }
        boolean isGbaSupported = this.mTelephonyManager.isGbaSupported(getSubscriptionId());
        int i = this.mSimSlot;
        IMSLog.i(LOG_TAG, i, "isGbaSupported [" + isGbaSupported + "]");
        return isGbaSupported;
    }

    public boolean isISimDataValid() {
        return getISimDataValidity() == 0;
    }

    /* access modifiers changed from: package-private */
    public int getISimDataValidity() {
        SimDataAdaptor simDataAdaptor;
        String isimImpi = this.mTelephonyManager.getIsimImpi(getSubscriptionId());
        String isimDomain = this.mTelephonyManager.getIsimDomain(getSubscriptionId());
        String[] isimImpu = this.mTelephonyManager.getIsimImpu(getSubscriptionId());
        int i = 0;
        if (CollectionUtils.isNullOrEmpty((Object[]) isimImpu) || (simDataAdaptor = this.mSimDataAdaptor) == null) {
            SimConstants.ISIM_VALIDITY isim_validity = SimConstants.ISIM_VALIDITY.IMPU_NOT_EXISTS;
            i = 0 | isim_validity.getValue();
            int i2 = this.mSimSlot;
            IMSLog.e(LOG_TAG, i2, "isIsimDataValid: " + isim_validity);
        } else if (!isValidImpu(simDataAdaptor.getImpuFromList(Arrays.asList(isimImpu)))) {
            SimConstants.ISIM_VALIDITY isim_validity2 = SimConstants.ISIM_VALIDITY.IMPU_INVALID;
            i = 0 | isim_validity2.getValue();
            int i3 = this.mSimSlot;
            IMSLog.e(LOG_TAG, i3, "isIsimDataValid: " + isim_validity2);
        }
        if (TextUtils.isEmpty(isimImpi)) {
            SimConstants.ISIM_VALIDITY isim_validity3 = SimConstants.ISIM_VALIDITY.IMPI_NOT_EXIST;
            i |= isim_validity3.getValue();
            int i4 = this.mSimSlot;
            IMSLog.e(LOG_TAG, i4, "isIsimDataValid: " + isim_validity3);
        }
        if (!TextUtils.isEmpty(isimDomain)) {
            return i;
        }
        if (getSimMno() != Mno.TMOUS || this.mHighestPriorityEhplmn.isEmpty()) {
            SimConstants.ISIM_VALIDITY isim_validity4 = SimConstants.ISIM_VALIDITY.HOME_DOMAIN_NOT_EXIST;
            int value = i | isim_validity4.getValue();
            int i5 = this.mSimSlot;
            IMSLog.e(LOG_TAG, i5, "isIsimDataValid: " + isim_validity4);
            return value;
        }
        this.mEventLog.logAndAdd(this.mSimSlot, "Allow empty EF_HOMEDOMAIN only when the EHPLMN is available");
        return i;
    }

    public static boolean isValidImpu(String str) {
        ImsUri parse = ImsUri.parse(str);
        if (parse != null && parse.getUriType() == ImsUri.UriType.SIP_URI) {
            return true;
        }
        IMSLog.s(LOG_TAG, "invalid impu : " + str);
        return false;
    }

    public String getIsimAuthentication(String str) {
        int i;
        if (hasIsim()) {
            i = 5;
        } else {
            i = isSimLoaded() ? 2 : 0;
        }
        return getIsimAuthentication(str, i);
    }

    private boolean isValidAkaResponse(String str) {
        if (TextUtils.equals(str, "2wQAAAAAAAA=")) {
            IMSLog.c(LogClass.SIM_AKA_RESPONSE, this.mSimSlot + ", failed to challenge");
            return false;
        } else if (!TextUtils.isEmpty(str) && !TextUtils.equals(str, "null")) {
            return true;
        } else {
            IMSLog.c(LogClass.SIM_AKA_RESPONSE, this.mSimSlot + ", empty response");
            return false;
        }
    }

    public String getIsimAuthentication(String str, int i) {
        if (i == 0 || str == null || str.length() % 2 != 0) {
            IMSLog.e(LOG_TAG, this.mSimSlot, "Wrong parameter - AppType : " + i + " nonce : " + str);
            return null;
        }
        IMSLog.i(LOG_TAG, this.mSimSlot, " getIsimAuthentication calling - AppType : " + i);
        byte[] bArr = new byte[(str.length() / 2)];
        int i2 = 0;
        int i3 = 0;
        while (i2 < str.length()) {
            int i4 = i2 + 2;
            bArr[i3] = (byte) (Integer.parseInt(str.substring(i2, i4), 16) & 255);
            i3++;
            i2 = i4;
        }
        IMSLog.c(LogClass.SIM_AKA_REQUEST, this.mSimSlot + ",REQ ISIM AUTH");
        String iccAuthentication = this.mTelephonyManager.getIccAuthentication(getSubscriptionId(), i, 129, Base64.encodeToString(bArr, 2));
        IMSLog.i(LOG_TAG, this.mSimSlot, "result: " + iccAuthentication);
        if ((getSimMno().isKor() || getSimMno().isChn() || getSimMno().isLatin() || getSimMno().isATTMexico()) && !isValidAkaResponse(iccAuthentication)) {
            this.mEventLog.logAndAdd(this.mSimSlot, "getIsimAuthentication result:" + iccAuthentication);
            return "mGI=";
        } else if (TextUtils.isEmpty(iccAuthentication) || TextUtils.equals(iccAuthentication, "null")) {
            IMSLog.e(LOG_TAG, this.mSimSlot, "getIccAuthentication failed");
            if (ImsRegistry.getRegistrationManager() != null) {
                ImsRegistry.getRegistrationManager().updateEmergencyTaskByAuthFailure(this.mSimSlot);
            }
            return null;
        } else {
            IMSLog.c(LogClass.SIM_AKA_RESPONSE, this.mSimSlot + ",LEN:" + iccAuthentication.length());
            try {
                byte[] decode = Base64.decode(iccAuthentication, 2);
                StringBuilder sb = new StringBuilder(decode.length * 2);
                IMSLog.i(LOG_TAG, this.mSimSlot, "resultBytes.length: " + decode.length);
                for (int i5 = 0; i5 < decode.length; i5++) {
                    sb.append("0123456789abcdef".charAt((decode[i5] >> 4) & 15));
                    sb.append("0123456789abcdef".charAt(decode[i5] & 15));
                }
                String sb2 = sb.toString();
                IMSLog.s(LOG_TAG, "decoded result : " + sb2);
                return sb2;
            } catch (Exception e) {
                IMSLog.e(LOG_TAG, this.mSimSlot, "Failed to decode the AKA RESPONSE - retry as MAC ERROR" + e.getMessage());
                return "9862";
            }
        }
    }

    public void requestIsimAuthentication(String str, int i, Message message) {
        String isimAuthentication = getIsimAuthentication(str, i);
        if (isimAuthentication != null) {
            message.obj = new String(isimAuthentication.getBytes());
            message.sendToTarget();
        }
    }

    public void requestIsimAuthentication(String str, Message message) {
        String isimAuthentication = getIsimAuthentication(str);
        if (isimAuthentication != null) {
            message.obj = new String(isimAuthentication.getBytes());
            message.sendToTarget();
        }
    }

    public void requestSoftphoneAuthentication(String str, String str2, Message message, int i) {
        this.mSoftphoneAccount = new SimConstants.SoftphoneAccount(str, i, str2, message);
        int i2 = this.mSimSlot;
        IMSLog.i(LOG_TAG, i2, "requestSoftphoneAuthentication, id = " + i);
        IMSLog.c(LogClass.SIM_SOFTPHONE_AUTH_REQUEST, this.mSimSlot + ",REQ AUTH");
        Intent intent = new Intent("com.sec.imsservice.REQUEST_AKA_CHALLENGE");
        intent.setPackage(this.mContext.getPackageName());
        intent.putExtra(WwwAuthenticateHeader.HEADER_PARAM_NONCE, str);
        intent.putExtra("impi", str2);
        intent.putExtra("id", i);
        ContextExt.sendBroadcastAsUser(this.mContext, intent, ContextExt.ALL);
    }

    /* access modifiers changed from: package-private */
    public void onSoftphoneAuthDone(String str) {
        IMSLog.s(LOG_TAG, "aka result : " + str);
        StringBuilder sb = new StringBuilder();
        sb.append(this.mSimSlot);
        sb.append(",LEN:");
        sb.append(str != null ? Integer.valueOf(str.length()) : "null");
        IMSLog.c(LogClass.SIM_SOFTPHONE_AUTH_RESPONSE, sb.toString());
        if (!TextUtils.isEmpty(str)) {
            Message message = this.mSoftphoneAccount.mResponse;
            if (message != null) {
                message.obj = new String(str.getBytes());
                this.mSoftphoneAccount.mResponse.sendToTarget();
                return;
            }
            return;
        }
        IMSLog.e(LOG_TAG, this.mSimSlot, "aka failed");
        sendEmptyMessage(5);
    }

    /* access modifiers changed from: package-private */
    public void onSoftphoneAuthFailed() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "onSoftphoneAuthFailed");
        Message message = this.mSoftphoneAccount.mResponse;
        message.what = 46;
        message.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public boolean updateSimState(SimConstants.SIM_STATE sim_state) {
        SimConstants.SIM_STATE sim_state2 = this.mSimState;
        if (sim_state2 == sim_state) {
            return false;
        }
        this.mSimStatePrev = sim_state2;
        this.mSimState = sim_state;
        if (sim_state == SimConstants.SIM_STATE.LOADED) {
            return true;
        }
        this.mIsOutBoundSIM = false;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isValidOperator(String str) {
        return !TextUtils.isEmpty(str) && str.length() >= 5;
    }

    /* access modifiers changed from: package-private */
    public boolean isValidImsi(String str, String str2) {
        return str2 != null && str2.length() > str.length();
    }

    /* access modifiers changed from: package-private */
    public boolean useImsSwitch() {
        return getSimMno() != Mno.GCF && !"GCF".equals(this.OMC_CODE) && !"SUP".equals(this.OMC_CODE) && !this.mLabSimCard;
    }

    /* access modifiers changed from: protected */
    public void onSimStateChange(String str) {
        boolean z;
        String simOperator = getSimOperator();
        boolean isMultiSimSupported = SimUtil.isMultiSimSupported();
        int i = this.mSimSlot;
        IMSLog.i(LOG_TAG, i, "onSimStateChange: [" + this.mSimState + " -> " + str + "], operator: [" + simOperator + "]");
        StringBuilder sb = new StringBuilder();
        sb.append(this.mSimSlot);
        sb.append(",,EVT:");
        sb.append(str);
        IMSLog.c(LogClass.SIM_EVENT, sb.toString());
        if (hasVsim()) {
            handleVsim(simOperator, str);
            return;
        }
        String str2 = "";
        if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(str)) {
            str2 = ImsRegistry.getString(this.mSimSlot, "mnoname", str2);
            z = handle_Loaded(simOperator);
        } else if (IccCardConstants.DELAYED_ISIM_LOAD.equals(str)) {
            z = handle_Delayed_IsimLoaded();
        } else if (IccCardConstants.INTENT_VALUE_ICC_ISIM_LOADED.equals(str)) {
            z = handle_IsimLoaded();
        } else if (IccCardConstants.INTENT_VALUE_ICC_NOT_READY.equals(str) || "UNKNOWN".equals(str)) {
            handle_NotReadyUnknown(simOperator, str);
            return;
        } else if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(str)) {
            handle_absent(simOperator, isMultiSimSupported);
            return;
        } else if (IccCardConstants.INTENT_VALUE_ICC_LOCKED.equals(str)) {
            handldle_Locked(simOperator);
            return;
        } else {
            z = false;
        }
        if (z) {
            handleSimStateChanged(str2, simOperator);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleVsim(String str, String str2) {
        if (this.mSimDataAdaptor == null) {
            this.mSimDataAdaptor = SimDataAdaptor.getSimDataAdaptor(this);
            IMSLog.i(LOG_TAG, this.mSimSlot, "Enable virtual SIM");
            updateSimState(SimConstants.SIM_STATE.LOADED);
            this.mIsimLoaded = true;
            this.mEventLog.add("VSIM LOADED");
            notifySimReady(str);
        } else if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(str2)) {
            handleSubscriptionId();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean handleSubscriptionId() {
        SubscriptionManager subscriptionManager = (SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service");
        if (subscriptionManager == null) {
            IMSLog.e(LOG_TAG, this.mSimSlot, "SubscriptionManager is null, should not happen");
            return false;
        }
        SubscriptionInfo activeSubscriptionInfoForSimSlotIndex = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(this.mSimSlot);
        if (activeSubscriptionInfoForSimSlotIndex == null) {
            IMSLog.e(LOG_TAG, this.mSimSlot, "onSimStateChange:[LOADED] subInfo is not created yet. retry in 1 sec.");
            IMSLog.c(LogClass.SIM_NO_SUBINFO, this.mSimSlot + ",NO SUBINFO");
            if (!hasVsim()) {
                this.mSimState = SimConstants.SIM_STATE.UNKNOWN;
            }
            sendMessageDelayed(obtainMessage(1, IccCardConstants.INTENT_VALUE_ICC_LOADED), 1000);
            return false;
        }
        SimManagerFactory.notifySubscriptionIdChanged(activeSubscriptionInfoForSimSlotIndex);
        setSubscriptionInfo(activeSubscriptionInfoForSimSlotIndex);
        return true;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x04ba  */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x04c3  */
    @android.annotation.SuppressLint({"MissingPermission"})
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean handle_Loaded(java.lang.String r21) {
        /*
            r20 = this;
            r0 = r20
            r1 = r21
            com.sec.internal.helper.SimpleEventLog r2 = r0.mEventLog
            int r3 = r0.mSimSlot
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "LOADED : "
            r4.append(r5)
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r5 = r0.mSimState
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            r2.logAndAdd(r3, r4)
            r2 = 1
            java.lang.String r3 = "LOADED"
            r0.removeMessages(r2, r3)
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r4 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.LOADED
            boolean r4 = r0.updateSimState(r4)
            if (r4 != 0) goto L_0x0045
            boolean r5 = r20.hasIsim()
            if (r5 != 0) goto L_0x0045
            java.lang.String r5 = r0.mLastImsi
            com.sec.internal.helper.os.ITelephonyManager r6 = r0.mTelephonyManager
            int r7 = r20.getSubscriptionId()
            java.lang.String r6 = r6.getSubscriberId(r7)
            boolean r5 = android.text.TextUtils.equals(r5, r6)
            if (r5 != 0) goto L_0x0045
            r4 = r2
        L_0x0045:
            com.sec.internal.ims.core.sim.SimDataAdaptor r5 = r0.mSimDataAdaptor
            if (r5 == 0) goto L_0x0050
            boolean r5 = r5.needHandleLoadedAgain(r1)
            if (r5 == 0) goto L_0x0050
            r4 = r2
        L_0x0050:
            if (r4 == 0) goto L_0x04cd
            boolean r5 = r20.isValidOperator(r21)
            r6 = 1000(0x3e8, double:4.94E-321)
            java.lang.String r8 = "SimManager"
            r9 = 0
            if (r5 != 0) goto L_0x008c
            int r4 = r0.mSimSlot
            java.lang.String r5 = "onSimStateChange: [LOADED] but operator is invalid. retry in 1 sec."
            com.sec.internal.log.IMSLog.e(r8, r4, r5)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            int r5 = r0.mSimSlot
            r4.append(r5)
            java.lang.String r5 = ",INVLD OP:"
            r4.append(r5)
            r4.append(r1)
            java.lang.String r1 = r4.toString()
            r4 = 268435457(0x10000001, float:2.5243552E-29)
            com.sec.internal.log.IMSLog.c(r4, r1)
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r1 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.UNKNOWN
            r0.mSimState = r1
            android.os.Message r1 = r0.obtainMessage(r2, r3)
            r0.sendMessageDelayed(r1, r6)
            return r9
        L_0x008c:
            boolean r5 = r20.handleSubscriptionId()
            if (r5 != 0) goto L_0x0093
            return r9
        L_0x0093:
            com.sec.internal.helper.os.ITelephonyManager r5 = r0.mTelephonyManager
            int r10 = r20.getSubscriptionId()
            java.lang.String r5 = r5.getSubscriberId(r10)
            com.sec.internal.helper.os.ITelephonyManager r10 = r0.mTelephonyManager
            int r11 = r20.getSubscriptionId()
            java.lang.String r10 = r10.getIsimImpi(r11)
            com.sec.internal.helper.os.ITelephonyManager r11 = r0.mTelephonyManager
            int r12 = r20.getSubscriptionId()
            java.lang.String r11 = r11.getGroupIdLevel1(r12)
            com.sec.internal.helper.os.ITelephonyManager r12 = r0.mTelephonyManager
            int r13 = r20.getSubscriptionId()
            java.lang.String r12 = r12.getSimOperatorName(r13)
            com.sec.internal.helper.os.ITelephonyManager r13 = r0.mTelephonyManager
            int r14 = r20.getSubscriptionId()
            java.lang.String r15 = r13.getGid2(r14)
            com.sec.internal.helper.SimpleEventLog r13 = r0.mEventLog
            int r14 = r0.mSimSlot
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r6 = "imsi:"
            r9.append(r6)
            java.lang.String r6 = com.sec.internal.log.IMSLog.checker(r5)
            r9.append(r6)
            java.lang.String r6 = " gid1:"
            r9.append(r6)
            r9.append(r11)
            java.lang.String r6 = " gid2:"
            r9.append(r6)
            r9.append(r15)
            java.lang.String r6 = " impi:"
            r9.append(r6)
            java.lang.String r6 = com.sec.internal.log.IMSLog.checker(r10)
            r9.append(r6)
            java.lang.String r6 = " spname:"
            r9.append(r6)
            r9.append(r12)
            java.lang.String r6 = r9.toString()
            r13.logAndAdd(r14, r6)
            boolean r6 = r0.isValidImsi(r1, r5)
            if (r6 != 0) goto L_0x014a
            int r1 = r0.mSimSlot
            java.lang.String r4 = "onSimStateChange: [LOADED] but imsi is invalid. retry in 1 sec."
            com.sec.internal.log.IMSLog.e(r8, r1, r4)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            int r4 = r0.mSimSlot
            r1.append(r4)
            java.lang.String r4 = ",INVLD IMSI,"
            r1.append(r4)
            if (r5 == 0) goto L_0x012c
            int r4 = r5.length()
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            goto L_0x012e
        L_0x012c:
            java.lang.String r4 = "null"
        L_0x012e:
            r1.append(r4)
            java.lang.String r1 = r1.toString()
            r4 = 268435459(0x10000003, float:2.5243558E-29)
            com.sec.internal.log.IMSLog.c(r4, r1)
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r1 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.UNKNOWN
            r0.mSimState = r1
            android.os.Message r1 = r0.obtainMessage(r2, r3)
            r2 = 1000(0x3e8, double:4.94E-321)
            r0.sendMessageDelayed(r1, r2)
            r0 = 0
            return r0
        L_0x014a:
            java.lang.String r3 = "ro.csc.sales_code"
            java.lang.String r6 = "unknown"
            java.lang.String r3 = android.os.SemSystemProperties.get(r3, r6)
            r0.OMC_CODE = r3
            boolean r3 = r6.equals(r3)
            if (r3 != 0) goto L_0x0164
            java.lang.String r3 = r0.OMC_CODE
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.fromSalesCode(r3)
            r0.mDevMno = r3
        L_0x0164:
            com.sec.internal.helper.SimpleEventLog r3 = r0.mEventLog
            int r6 = r0.mSimSlot
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r9 = "OMC_CODE(loaded): "
            r7.append(r9)
            java.lang.String r9 = r0.OMC_CODE
            r7.append(r9)
            java.lang.String r9 = ", mDevMno: "
            r7.append(r9)
            com.sec.internal.constants.Mno r9 = r0.mDevMno
            java.lang.String r9 = r9.toString()
            r7.append(r9)
            java.lang.String r7 = r7.toString()
            r3.logAndAdd(r6, r7)
            int r3 = r0.mSimSlot
            java.lang.String r3 = com.sec.internal.helper.OmcCode.getNWCode(r3)
            r0.OMCNW_CODE = r3
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.fromSalesCode(r3)
            r0.mNetMno = r3
            com.sec.internal.helper.SimpleEventLog r3 = r0.mEventLog
            int r6 = r0.mSimSlot
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r9 = "OMCNW_CODE(loaded): "
            r7.append(r9)
            java.lang.String r9 = r0.OMCNW_CODE
            r7.append(r9)
            java.lang.String r9 = ", mNetMno: "
            r7.append(r9)
            com.sec.internal.constants.Mno r9 = r0.mNetMno
            java.lang.String r9 = r9.toString()
            r7.append(r9)
            java.lang.String r7 = r7.toString()
            r3.logAndAdd(r6, r7)
            com.sec.internal.helper.os.ImsCscFeature r3 = com.sec.internal.helper.os.ImsCscFeature.getInstance()
            int r6 = r0.mSimSlot
            r3.clear(r6)
            java.lang.String r3 = "00101"
            boolean r3 = android.text.TextUtils.equals(r3, r1)
            if (r3 == 0) goto L_0x01e7
            java.lang.String r3 = "CPW"
            java.lang.String r6 = r0.OMCNW_CODE
            boolean r3 = android.text.TextUtils.equals(r3, r6)
            if (r3 == 0) goto L_0x01e7
            int r3 = r0.mSimSlot
            java.lang.String r6 = "(CPW) & 00101 sim card, Enable GCF mode"
            com.sec.internal.log.IMSLog.i(r8, r3, r6)
            com.sec.internal.helper.os.DeviceUtil.setGcfMode(r2)
        L_0x01e7:
            java.lang.String r3 = ""
            r0.mImsiFromImpi = r3
            boolean r3 = com.sec.internal.helper.os.DeviceUtil.getGcfMode()
            if (r3 == 0) goto L_0x01fb
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.GCF
            r0.setSimMno(r3, r2)
            r6 = 0
            r19 = 0
            goto L_0x034d
        L_0x01fb:
            com.sec.internal.ims.core.sim.MnoMap r3 = r0.mMnoMap
            if (r3 != 0) goto L_0x0213
            com.sec.internal.helper.SimpleEventLog r3 = r0.mEventLog
            int r6 = r0.mSimSlot
            java.lang.String r7 = "mnomap is empty"
            r3.logAndAdd(r6, r7)
            com.sec.internal.ims.core.sim.MnoMap r3 = new com.sec.internal.ims.core.sim.MnoMap
            android.content.Context r6 = r0.mContext
            int r7 = r0.mSimSlot
            r3.<init>(r6, r7)
            r0.mMnoMap = r3
        L_0x0213:
            if (r10 == 0) goto L_0x027f
            boolean r3 = r10.startsWith(r1)
            if (r3 != 0) goto L_0x027f
            java.lang.String r3 = com.sec.internal.ims.core.sim.SimManagerUtils.extractMnoFromImpi(r10)
            boolean r6 = r10.startsWith(r5)
            if (r6 != 0) goto L_0x0234
            com.sec.internal.helper.os.ITelephonyManager r6 = r0.mTelephonyManager
            int r7 = r20.getSubscriptionId()
            java.lang.String r6 = r6.getSubscriberId(r7)
            java.lang.String r6 = com.sec.internal.ims.core.sim.SimManagerUtils.extractImsiFromImpi(r10, r6)
            goto L_0x0235
        L_0x0234:
            r6 = r5
        L_0x0235:
            com.sec.internal.ims.core.sim.MnoMap r13 = r0.mMnoMap
            r14 = r3
            r7 = r15
            r15 = r6
            r16 = r11
            r17 = r7
            r18 = r12
            java.lang.String r9 = r13.getMnoName(r14, r15, r16, r17, r18)
            java.lang.String r10 = r0.getMnoNameWithoutGcExtension(r9)
            com.sec.internal.constants.Mno r10 = com.sec.internal.constants.Mno.fromName(r10)
            com.sec.internal.helper.SimpleEventLog r13 = r0.mEventLog
            int r14 = r0.mSimSlot
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            java.lang.String r2 = "MnoNameFromImpi: "
            r15.append(r2)
            r15.append(r9)
            java.lang.String r2 = ", SIM Mno: "
            r15.append(r2)
            r15.append(r10)
            java.lang.String r2 = r15.toString()
            r13.logAndAdd(r14, r2)
            boolean r2 = r10.isRjil()
            if (r2 != 0) goto L_0x0278
            boolean r2 = r10.isDish()
            if (r2 == 0) goto L_0x0280
        L_0x0278:
            r0.mOperatorFromImpi = r3
            r0.mImsiFromImpi = r6
            r1 = r3
            r5 = r6
            goto L_0x0280
        L_0x027f:
            r7 = r15
        L_0x0280:
            com.sec.internal.ims.core.sim.MnoMap r13 = r0.mMnoMap
            r14 = r1
            r15 = r5
            r16 = r11
            r17 = r7
            r18 = r12
            java.lang.String r2 = r13.getMnoName(r14, r15, r16, r17, r18)
            com.sec.internal.ims.core.sim.MnoMap r3 = r0.mMnoMap
            com.sec.internal.helper.os.ITelephonyManager r6 = r0.mTelephonyManager
            int r7 = r20.getSubscriptionId()
            java.lang.String r6 = r6.getSimSerialNumber(r7)
            com.sec.internal.helper.os.ITelephonyManager r7 = r0.mTelephonyManager
            int r9 = r0.mSimSlot
            java.lang.String r7 = r7.getRilSimOperator(r9)
            java.lang.String r2 = r3.changeMnoNameByIccid(r2, r1, r6, r7)
            com.sec.internal.ims.core.sim.MnoMap r3 = r0.mMnoMap
            boolean r3 = r3.isGcBlockListContains(r1)
            if (r3 != 0) goto L_0x02b6
            boolean r3 = r0.isMnoHasGcBlockExtension(r2)
            if (r3 != 0) goto L_0x02b6
            r3 = 1
            goto L_0x02b7
        L_0x02b6:
            r3 = 0
        L_0x02b7:
            int r6 = r0.mSimSlot
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r9 = "isGlobalGcEnabled: "
            r7.append(r9)
            r7.append(r3)
            java.lang.String r7 = r7.toString()
            com.sec.internal.log.IMSLog.i(r8, r6, r7)
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.DEFAULT
            java.lang.String r6 = r6.getName()
            boolean r6 = android.text.TextUtils.equals(r2, r6)
            if (r6 == 0) goto L_0x02da
            r3 = 0
        L_0x02da:
            java.lang.String r2 = r0.getMnoNameWithoutGcExtension(r2)
            r0.mSimMnoName = r2
            java.lang.String r6 = "LABSIM"
            boolean r2 = r6.equalsIgnoreCase(r2)
            if (r2 == 0) goto L_0x02f2
            com.sec.internal.constants.Mno r2 = r0.mNetMno
            r3 = 1
            r0.setSimMno(r2, r3)
            r0.mLabSimCard = r3
            r3 = 0
            goto L_0x02fe
        L_0x02f2:
            java.lang.String r2 = r0.mSimMnoName
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.fromName(r2)
            r6 = 0
            r0.setSimMno(r2, r6)
            r0.mLabSimCard = r6
        L_0x02fe:
            com.sec.internal.constants.Mno r2 = r20.getSimMno()
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.DEFAULT
            if (r2 != r6) goto L_0x0323
            java.lang.String r2 = "SUP"
            java.lang.String r6 = r0.OMC_CODE
            boolean r2 = r2.equalsIgnoreCase(r6)
            if (r2 != 0) goto L_0x0316
            boolean r2 = r20.getGtsAppInstalled()
            if (r2 == 0) goto L_0x0323
        L_0x0316:
            int r2 = r0.mSimSlot
            java.lang.String r6 = "With SUP CSC or GtsAppInstalled, use GCF profile for GTS testing."
            com.sec.internal.log.IMSLog.i(r8, r2, r6)
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.GCF
            r6 = 1
            r0.setSimMno(r2, r6)
        L_0x0323:
            com.sec.internal.constants.Mno r2 = r20.getSimMno()
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.DEFAULT
            if (r2 != r6) goto L_0x034a
            java.lang.String r2 = "DEFAULT"
            java.lang.String r6 = r0.mSimMnoName
            boolean r2 = r2.equalsIgnoreCase(r6)
            if (r2 != 0) goto L_0x034a
            com.sec.internal.helper.SimpleEventLog r2 = r0.mEventLog
            int r6 = r0.mSimSlot
            java.lang.String r7 = "handle_Loaded: Mno.GENERIC Update Name, Country, Region"
            r2.logAndAdd(r6, r7)
            java.lang.String r2 = r0.mSimMnoName
            com.sec.internal.constants.Mno.updateGenerictMno(r2)
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.GENERIC
            r6 = 0
            r0.setSimMno(r2, r6)
            goto L_0x034b
        L_0x034a:
            r6 = 0
        L_0x034b:
            r19 = r3
        L_0x034d:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "sys.smf.mnoname"
            r2.append(r3)
            int r3 = r0.mSimSlot
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r7 = r0.mSimMnoName
            r3.append(r7)
            java.lang.String r7 = "|LOADED"
            r3.append(r7)
            java.lang.String r3 = r3.toString()
            android.os.SemSystemProperties.set(r2, r3)
            com.sec.internal.helper.SimpleEventLog r2 = r0.mEventLog
            int r3 = r0.mSimSlot
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r9 = "SIM PLMN: "
            r7.append(r9)
            r7.append(r1)
            java.lang.String r9 = ", mSimMno: "
            r7.append(r9)
            com.sec.internal.constants.Mno r9 = r20.getSimMno()
            java.lang.String r9 = r9.toString()
            r7.append(r9)
            java.lang.String r9 = "("
            r7.append(r9)
            java.lang.String r9 = r0.mSimMnoName
            r7.append(r9)
            java.lang.String r9 = ")"
            r7.append(r9)
            java.lang.String r7 = r7.toString()
            r2.logAndAdd(r3, r7)
            com.sec.internal.ims.core.sim.SimDataAdaptor r2 = com.sec.internal.ims.core.sim.SimDataAdaptor.getSimDataAdaptor(r20)
            r0.mSimDataAdaptor = r2
            com.sec.internal.ims.core.sim.MnoInfoStorage r2 = r0.mMnoInfoStorage
            r2.init()
            android.content.ContentValues r2 = new android.content.ContentValues
            r2.<init>()
            java.lang.String r3 = "hassim"
            java.lang.Boolean r7 = java.lang.Boolean.TRUE
            r2.put(r3, r7)
            java.lang.String r3 = "globalgcenabled"
            java.lang.Boolean r7 = java.lang.Boolean.valueOf(r19)
            r2.put(r3, r7)
            com.sec.internal.constants.Mno r3 = r20.getSimMno()
            java.lang.String r3 = r3.getName()
            java.lang.String r7 = "mnoname"
            r2.put(r7, r3)
            java.lang.String r3 = r0.mSimMnoName
            java.lang.String r3 = com.sec.internal.ims.core.sim.SimManagerUtils.getMvnoName(r3)
            java.lang.String r7 = "mvnoname"
            r2.put(r7, r3)
            java.lang.String r3 = "imsi"
            r2.put(r3, r5)
            android.content.Context r3 = r0.mContext
            int r5 = r0.mSimSlot
            java.lang.String r7 = r0.OMCNW_CODE
            java.lang.String r9 = r0.mSimMnoName
            com.sec.internal.ims.diagnosis.ImsLogAgentUtil.updateCommonHeader(r3, r5, r7, r9, r1)
            boolean r1 = r20.checkOutBoundSIM()
            r0.mIsOutBoundSIM = r1
            android.content.Context r1 = r0.mContext
            java.lang.String r3 = r0.mSimMnoName
            int r5 = r0.mSimSlot
            java.util.List r1 = com.sec.internal.ims.settings.ImsProfileLoaderInternal.getProfileListWithMnoName(r1, r3, r5)
            int r3 = r0.getSimMobilityType(r1)
            com.sec.internal.constants.Mno r5 = r20.getSimMno()
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.GENERIC
            r9 = 4
            if (r5 != r7) goto L_0x0417
        L_0x0414:
            r6 = r9
            goto L_0x04ac
        L_0x0417:
            boolean r5 = com.sec.internal.helper.OmcCode.isKDIMhs()
            r7 = 5
            if (r5 != 0) goto L_0x04a2
            int r5 = r0.mSimSlot
            boolean r5 = com.sec.internal.helper.SimUtil.isCctChaCbrsMsoSim(r5)
            if (r5 == 0) goto L_0x0428
            goto L_0x04a2
        L_0x0428:
            if (r3 <= 0) goto L_0x0463
            java.lang.String r5 = "simMoType"
            java.lang.Integer r6 = java.lang.Integer.valueOf(r3)
            r2.put(r5, r6)
            r5 = 3
            r6 = 1
            if (r3 == r6) goto L_0x043a
            if (r3 != r5) goto L_0x044c
        L_0x043a:
            int r6 = r0.mSimSlot
            java.lang.String r7 = "isSimMobilityForVoLTE true"
            com.sec.internal.log.IMSLog.i(r8, r6, r7)
            android.content.Context r6 = r0.mContext
            int r7 = r0.mSimSlot
            android.content.ContentValues r6 = com.sec.internal.ims.settings.ImsServiceSwitch.getSimMobilityImsSwitchSetting(r6, r7)
            r2.putAll(r6)
        L_0x044c:
            r6 = 2
            if (r3 == r6) goto L_0x0451
            if (r3 != r5) goto L_0x0461
        L_0x0451:
            int r3 = r0.mSimSlot
            java.lang.String r6 = "isSimMobilityForRcs true"
            com.sec.internal.log.IMSLog.i(r8, r3, r6)
            int r3 = r0.mSimSlot
            android.content.ContentValues r1 = com.sec.internal.ims.core.sim.SimManagerUtils.getSimMobilityRcsSettings(r3, r1)
            r2.putAll(r1)
        L_0x0461:
            r6 = r5
            goto L_0x04ac
        L_0x0463:
            int r1 = r0.mSimSlot
            java.lang.String r1 = com.sec.internal.helper.OmcCode.getNWCode(r1)
            java.lang.String r3 = "XAS"
            boolean r1 = r3.equals(r1)
            if (r1 == 0) goto L_0x0480
            int r1 = r0.mSimSlot
            java.lang.String r3 = "for XAS use internal IMSSetting"
            com.sec.internal.log.IMSLog.i(r8, r1, r3)
            android.content.ContentValues r1 = com.sec.internal.ims.settings.ImsServiceSwitch.getXasImsSwitchSetting()
            r2.putAll(r1)
            goto L_0x0414
        L_0x0480:
            boolean r1 = r20.useImsSwitch()
            if (r1 == 0) goto L_0x04ac
            boolean r1 = r0.mIsOutBoundSIM
            if (r1 == 0) goto L_0x0414
            boolean r1 = r20.getGtsAppInstalled()
            if (r1 == 0) goto L_0x0498
            int r1 = r0.mSimSlot
            java.lang.String r3 = "GTS installed"
            com.sec.internal.log.IMSLog.i(r8, r1, r3)
            goto L_0x04ac
        L_0x0498:
            com.sec.internal.helper.SimpleEventLog r1 = r0.mEventLog
            int r3 = r0.mSimSlot
            java.lang.String r5 = "Turned off all switches for OutBoundSIM && not SimMo"
            r1.logAndAdd(r3, r5)
            goto L_0x04ab
        L_0x04a2:
            com.sec.internal.helper.SimpleEventLog r1 = r0.mEventLog
            int r3 = r0.mSimSlot
            java.lang.String r5 = "Turning off all switches for Non-Ims SIM"
            r1.logAndAdd(r3, r5)
        L_0x04ab:
            r6 = r7
        L_0x04ac:
            java.lang.String r1 = "imsSwitchType"
            java.lang.Integer r3 = java.lang.Integer.valueOf(r6)
            r2.put(r1, r3)
            r0.updateMno(r2)
            if (r6 != r9) goto L_0x04bf
            int r1 = r0.mSimSlot
            r0.onImsSwitchUpdated(r1)
        L_0x04bf:
            boolean r1 = r0.mIsCrashSimEvent
            if (r1 == 0) goto L_0x04cd
            int r0 = r0.mSimSlot
            java.lang.String r1 = "handle_Loaded: need to update ADS again when imsservice restarted"
            com.sec.internal.log.IMSLog.i(r8, r0, r1)
            com.sec.internal.ims.core.sim.SimManagerFactory.updateAdsSlot()
        L_0x04cd:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.sim.SimManager.handle_Loaded(java.lang.String):boolean");
    }

    /* access modifiers changed from: package-private */
    public boolean handle_Delayed_IsimLoaded() {
        if (this.mIsimLoaded || this.mSimState != SimConstants.SIM_STATE.LOADED) {
            return false;
        }
        this.mEventLog.logAndAdd(this.mSimSlot, IccCardConstants.INTENT_VALUE_ICC_ISIM_LOADED);
        this.mIsimLoaded = true;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean handle_IsimLoaded() {
        SimConstants.SIM_STATE sim_state;
        this.mEventLog.logAndAdd(this.mSimSlot, IccCardConstants.INTENT_VALUE_ICC_ISIM_LOADED);
        boolean z = !this.mIsimLoaded;
        if (this.mSimState == SimConstants.SIM_STATE.INVALID_ISIM && this.mSimStatePrev == (sim_state = SimConstants.SIM_STATE.LOADED)) {
            updateSimState(sim_state);
            z = true;
        }
        if (this.mSimState == SimConstants.SIM_STATE.LOADED && getSimMno() == Mno.BELL) {
            IMSLog.i(LOG_TAG, this.mSimSlot, "fix for exceptional case : LOADED notified before ISIM_LOADED");
            z = true;
        }
        this.mIsimLoaded = true;
        return z;
    }

    /* access modifiers changed from: package-private */
    public void handle_NotReadyUnknown(String str, String str2) {
        removeMessages(1, IccCardConstants.INTENT_VALUE_ICC_LOADED);
        if (this.mSimState == SimConstants.SIM_STATE.LOADED || this.mIsRefresh) {
            onSimNotReady();
        } else if ("UNKNOWN".equals(str2) && SimManagerUtils.needImsUpOnUnknownState(this.mContext, this.mSimSlot)) {
            String str3 = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY, NSDSNamespaces.NSDSSimAuthType.UNKNOWN);
            this.OMC_CODE = str3;
            Mno mno = Mno.DEFAULT;
            if (!NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equals(str3)) {
                mno = Mno.fromSalesCode(this.OMC_CODE);
            }
            this.mDevMno = mno;
            this.mEventLog.logAndAdd(this.mSimSlot, "SIM UNKNOWN");
            SimpleEventLog simpleEventLog = this.mEventLog;
            int i = this.mSimSlot;
            simpleEventLog.logAndAdd(i, "OMC_CODE(unknown): " + this.OMC_CODE + ", mDevMno: " + this.mDevMno.toString());
            setSimMno(this.mDevMno, true);
            StringBuilder sb = new StringBuilder();
            sb.append(SMF_MNONAME_PROP);
            sb.append(this.mSimSlot);
            SemSystemProperties.set(sb.toString(), this.mSimMnoName);
            this.mMnoInfoStorage.init();
            ContentValues contentValues = new ContentValues();
            contentValues.put(ISimManager.KEY_HAS_SIM, Boolean.FALSE);
            contentValues.put("mnoname", this.mDevMno.getName());
            contentValues.put(ISimManager.KEY_MVNO_NAME, SimManagerUtils.getMvnoName(this.mSimMnoName));
            contentValues.put(ISimManager.KEY_IMSSWITCH_TYPE, 0);
            updateMno(contentValues);
            notifySimReady(str);
        }
    }

    /* access modifiers changed from: package-private */
    public void handle_absent(String str, boolean z) {
        boolean updateSimState = updateSimState(SimConstants.SIM_STATE.ABSENT);
        ImsLogAgentUtil.requestToSendStoredLog(this.mSimSlot, this.mContext, "DRPT");
        removeMessages(1, IccCardConstants.INTENT_VALUE_ICC_LOADED);
        ImsAutoUpdate.getInstance(this.mContext, this.mSimSlot).resetCarrierFeatureHash();
        SimConstants.SIM_STATE sim_state = this.mSimStatePrev;
        if (sim_state == SimConstants.SIM_STATE.LOADED || sim_state == SimConstants.SIM_STATE.LOCKED) {
            this.mEventLog.logAndAdd(this.mSimSlot, "SIM REMOVED");
            onSimRemoved();
            String string = this.mMnoInfoStorage.getString("mnoname");
            this.mMnoInfoStorage.init();
            ContentValues contentValues = new ContentValues();
            contentValues.clear();
            contentValues.put(ISimManager.KEY_HAS_SIM, Boolean.FALSE);
            contentValues.put("mnoname", string);
            contentValues.put(ISimManager.KEY_MVNO_NAME, this.mMnoInfoStorage.getString(ISimManager.KEY_MVNO_NAME));
            contentValues.put(ISimManager.KEY_IMSSWITCH_TYPE, 0);
            updateMno(contentValues);
            return;
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(this.mSimSlot + "SIM ABSENT");
        this.mIsimLoaded = false;
        this.mSimDataAdaptor = SimDataAdaptor.getSimDataAdaptor(this);
        if (updateSimState) {
            notifySimReady(str);
        }
        String str2 = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY, NSDSNamespaces.NSDSSimAuthType.UNKNOWN);
        this.OMC_CODE = str2;
        if (!NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equals(str2)) {
            this.mDevMno = Mno.fromSalesCode(this.OMC_CODE);
        }
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        int i = this.mSimSlot;
        simpleEventLog2.logAndAdd(i, "OMC_CODE(absent): " + this.OMC_CODE + ", mDevMno: " + this.mDevMno.toString());
        String nWCode = OmcCode.getNWCode(this.mSimSlot);
        this.OMCNW_CODE = nWCode;
        this.mNetMno = Mno.fromSalesCode(nWCode);
        SimpleEventLog simpleEventLog3 = this.mEventLog;
        int i2 = this.mSimSlot;
        simpleEventLog3.logAndAdd(i2, " OMCNW_CODE(absent): " + this.OMCNW_CODE + ", mNetMno: " + this.mNetMno.toString());
        setSimMno(this.mNetMno, true);
        StringBuilder sb = new StringBuilder();
        sb.append(SMF_MNONAME_PROP);
        sb.append(this.mSimSlot);
        String sb2 = sb.toString();
        SemSystemProperties.set(sb2, this.mSimMnoName + "|ABSENT");
        this.mMnoInfoStorage.init();
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(ISimManager.KEY_HAS_SIM, Boolean.FALSE);
        contentValues2.put("mnoname", this.mNetMno.getName());
        contentValues2.put(ISimManager.KEY_MVNO_NAME, SimManagerUtils.getMvnoName(this.mSimMnoName));
        contentValues2.put(ISimManager.KEY_IMSSWITCH_TYPE, 0);
        if (getSimMno() == Mno.RJIL) {
            int activeDataPhoneIdFromTelephony = SimUtil.getActiveDataPhoneIdFromTelephony();
            if (!z || activeDataPhoneIdFromTelephony == this.mSimSlot) {
                updateMno(contentValues2);
            }
        } else if (SimUtil.isDualIMS() || this.mTelephonyManager.getSimState() == 1) {
            updateMno(contentValues2);
        }
    }

    /* access modifiers changed from: package-private */
    public void handldle_Locked(String str) {
        boolean updateSimState = updateSimState(SimConstants.SIM_STATE.LOCKED);
        String str2 = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY, NSDSNamespaces.NSDSSimAuthType.UNKNOWN);
        this.OMC_CODE = str2;
        if (!NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equals(str2)) {
            this.mDevMno = Mno.fromSalesCode(this.OMC_CODE);
        }
        this.mEventLog.logAndAdd(this.mSimSlot, "SIM LOCKED");
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i = this.mSimSlot;
        simpleEventLog.logAndAdd(i, "OMC_CODE(locked): " + this.OMC_CODE + ", mDevMno: " + this.mDevMno.toString());
        setSimMno(this.mDevMno, true);
        StringBuilder sb = new StringBuilder();
        sb.append(SMF_MNONAME_PROP);
        sb.append(this.mSimSlot);
        SemSystemProperties.set(sb.toString(), this.mSimMnoName);
        this.mMnoInfoStorage.init();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ISimManager.KEY_HAS_SIM, Boolean.FALSE);
        contentValues.put("mnoname", this.mDevMno.getName());
        contentValues.put(ISimManager.KEY_MVNO_NAME, SimManagerUtils.getMvnoName(this.mSimMnoName));
        contentValues.put(ISimManager.KEY_IMSSWITCH_TYPE, 0);
        updateMno(contentValues);
        if (!this.mSimStatePrev.isOneOf(SimConstants.SIM_STATE.LOADED, SimConstants.SIM_STATE.ABSENT) && updateSimState) {
            notifySimReady(str);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleSimStateChanged(String str, String str2) {
        boolean z;
        int iSimDataValidity;
        if (isSimAvailable()) {
            IMSLog.i(LOG_TAG, this.mSimSlot, "handleSimChange: SIM is ready.");
            if (getSimMno().isRjil()) {
                this.mLastImsi = getImsiFromImpi();
            } else {
                this.mLastImsi = this.mTelephonyManager.getSubscriberId(getSubscriptionId());
            }
            ContentValues contentValues = new ContentValues();
            String groupIdLevel1 = this.mTelephonyManager.getGroupIdLevel1(getSubscriptionId());
            contentValues.put(DiagnosisConstants.SIMI_KEY_EVENT_TYPE, Integer.valueOf(DiagnosisConstants.getEventType(this.mSimStatePrev, this.mIsRefresh, TextUtils.equals(this.mSimMnoName, str))));
            contentValues.put(DiagnosisConstants.SIMI_KEY_SUBSCRIPTION_ID, Integer.valueOf(Math.max(getSubscriptionId(), 0)));
            if (!TextUtils.isEmpty(groupIdLevel1)) {
                contentValues.put(DiagnosisConstants.SIMI_KEY_GID1, groupIdLevel1.substring(0, Math.min(16, groupIdLevel1.length())));
            }
            contentValues.put(DiagnosisConstants.SIMI_KEY_ISIM_EXISTS, Integer.valueOf(this.mIsimLoaded ? 1 : 0));
            Context context = this.mContext;
            ImsConstants.SystemSettings.SettingsItem settingsItem = ImsConstants.SystemSettings.VOLTE_SLOT1;
            contentValues.put(DiagnosisConstants.COMMON_KEY_VOLTE_SETTINGS, Integer.valueOf(DmConfigHelper.getImsUserSetting(context, settingsItem.getName(), this.mSimSlot)));
            contentValues.put(DiagnosisConstants.COMMON_KEY_VIDEO_SETTINGS, Integer.valueOf(DmConfigHelper.getImsUserSetting(this.mContext, settingsItem.getName(), this.mSimSlot)));
            int value = (getSimMno() != Mno.TMOUS || isGBASupported()) ? 0 : SimConstants.SIM_VALIDITY.GBA_NOT_SUPPORTED.getValue() | 0;
            SimDataAdaptor simDataAdaptor = this.mSimDataAdaptor;
            if (simDataAdaptor == null || simDataAdaptor.hasValidMsisdn()) {
                z = true;
            } else {
                value |= SimConstants.SIM_VALIDITY.MSISDN_INVALID.getValue();
                IMSLog.e(LOG_TAG, this.mSimSlot, "Invalid MSISDN");
                z = false;
            }
            if (value > 0) {
                contentValues.put(DiagnosisConstants.SIMI_KEY_SIM_VALIDITY, DiagnosisConstants.intToHexStr(value));
            }
            if (this.mIsimLoaded && (iSimDataValidity = getISimDataValidity()) > 0) {
                contentValues.put(DiagnosisConstants.SIMI_KEY_ISIM_VALIDITY, DiagnosisConstants.intToHexStr(iSimDataValidity));
                if (ImsRegistry.getBoolean(this.mSimSlot, GlobalSettingsConstants.Registration.BLOCK_REGI_ON_INVALID_ISIM, true) && !isEsim()) {
                    IMSLog.e(LOG_TAG, this.mSimSlot, "onSimStateChange: invalid ISIM!");
                    updateSimState(SimConstants.SIM_STATE.INVALID_ISIM);
                    this.mEventLog.logAndAdd(this.mSimSlot, "INVALID_FIELD");
                    IMSLog.c(LogClass.SIM_INVALID_ISIM, this.mSimSlot + ",INVLD ISIM," + iSimDataValidity);
                }
            }
            ImsLogAgentUtil.sendLogToAgent(this.mSimSlot, this.mContext, DiagnosisConstants.FEATURE_SIMI, contentValues);
            this.mIsRefresh = false;
            this.mIsCrashSimEvent = false;
            if (z) {
                notifySimReady(str2);
            }
        } else if (this.mSimState == SimConstants.SIM_STATE.LOADED && isISimAppLoaded()) {
            if (this.mIsCrashSimEvent) {
                IMSLog.d(LOG_TAG, this.mSimSlot, "send simstate, isim loaded");
                this.mIsCrashSimEvent = false;
                sendMessage(obtainMessage(1, IccCardConstants.INTENT_VALUE_ICC_ISIM_LOADED));
                return;
            }
            sendMessageDelayed(obtainMessage(1, IccCardConstants.DELAYED_ISIM_LOAD), 10000);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0058, code lost:
        if (r1.equals(com.sec.internal.constants.ims.os.IccCardConstants.INTENT_VALUE_ICC_LOADED) == false) goto L_0x0031;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void initializeSimState() {
        /*
            r6 = this;
            r0 = 0
            r6.mIsCrashSimEvent = r0
            int r1 = r6.mSimSlot
            com.sec.internal.helper.os.ITelephonyManager r2 = r6.mTelephonyManager
            java.lang.String r1 = com.sec.internal.ims.core.sim.SimManagerUtils.readSimStateProperty(r1, r2)
            int r2 = r6.mSimSlot
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "initializeSimState (gsm.sim.state) : =  "
            r3.append(r4)
            r3.append(r1)
            java.lang.String r3 = r3.toString()
            java.lang.String r4 = "SimManager"
            com.sec.internal.log.IMSLog.i(r4, r2, r3)
            r1.hashCode()
            int r2 = r1.hashCode()
            java.lang.String r3 = "LOADED"
            r4 = 1
            r5 = -1
            switch(r2) {
                case -2044189691: goto L_0x0054;
                case -2044123382: goto L_0x0049;
                case 433141802: goto L_0x003e;
                case 1924388665: goto L_0x0033;
                default: goto L_0x0031;
            }
        L_0x0031:
            r0 = r5
            goto L_0x005b
        L_0x0033:
            java.lang.String r0 = "ABSENT"
            boolean r0 = r1.equals(r0)
            if (r0 != 0) goto L_0x003c
            goto L_0x0031
        L_0x003c:
            r0 = 3
            goto L_0x005b
        L_0x003e:
            java.lang.String r0 = "UNKNOWN"
            boolean r0 = r1.equals(r0)
            if (r0 != 0) goto L_0x0047
            goto L_0x0031
        L_0x0047:
            r0 = 2
            goto L_0x005b
        L_0x0049:
            java.lang.String r0 = "LOCKED"
            boolean r0 = r1.equals(r0)
            if (r0 != 0) goto L_0x0052
            goto L_0x0031
        L_0x0052:
            r0 = r4
            goto L_0x005b
        L_0x0054:
            boolean r2 = r1.equals(r3)
            if (r2 != 0) goto L_0x005b
            goto L_0x0031
        L_0x005b:
            switch(r0) {
                case 0: goto L_0x0067;
                case 1: goto L_0x005f;
                case 2: goto L_0x005f;
                case 3: goto L_0x005f;
                default: goto L_0x005e;
            }
        L_0x005e:
            goto L_0x0070
        L_0x005f:
            android.os.Message r0 = r6.obtainMessage(r4, r1)
            r6.sendMessage(r0)
            goto L_0x0070
        L_0x0067:
            r6.mIsCrashSimEvent = r4
            android.os.Message r0 = r6.obtainMessage(r4, r3)
            r6.sendMessage(r0)
        L_0x0070:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.sim.SimManager.initializeSimState():void");
    }

    /* access modifiers changed from: package-private */
    public void setSimMno(Mno mno, boolean z) {
        SimUtil.setSimMno(this.mSimSlot, mno);
        if (z) {
            this.mSimMnoName = mno.getName();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateMno(ContentValues contentValues) {
        IMSLog.i(LOG_TAG, this.mSimSlot, "updateMno:");
        contentValues.put("phoneId", Integer.valueOf(this.mSimSlot));
        if (TextUtils.isEmpty(contentValues.getAsString("imsi"))) {
            contentValues.put("imsi", "");
        }
        int intValue = ((Integer) Optional.ofNullable(contentValues.getAsInteger(ISimManager.KEY_IMSSWITCH_TYPE)).orElse(-1)).intValue();
        this.mMnoInfoStorage.update(contentValues);
        IMSLog.c(LogClass.SIM_UPDATE_MNO, this.mSimSlot + "," + this.mSimState + "," + this.mSimMnoName + "," + intValue);
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i = this.mSimSlot;
        simpleEventLog.logAndAdd(i, this.mSimState + ", " + this.mSimMnoName + ", " + this.mMnoInfoStorage);
        if (intValue != 0) {
            IMSLog.c(LogClass.SIM_MNO_INFO, this.mSimSlot + "," + SimManagerUtils.convertMnoInfoToString(this.mMnoInfoStorage.getAll()));
        }
        notifyMnoChanged();
    }

    /* access modifiers changed from: package-private */
    public void notifyMnoChanged() {
        Uri.Builder buildUpon = URI_UPDATE_MNO.buildUpon();
        Uri build = buildUpon.fragment("simslot" + this.mSimSlot).build();
        int i = this.mSimSlot;
        IMSLog.i(LOG_TAG, i, "notifyMnoChanged [" + build + "]");
        this.mContext.getContentResolver().update(build, this.mMnoInfoStorage.getAll(), (String) null, (String[]) null);
    }

    /* access modifiers changed from: package-private */
    public void onSimRefresh() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "onSimRefresh");
        IMSLog.c(LogClass.SIM_REFRESH, this.mSimSlot + ",SIM REFRESH");
        this.mEventLog.logAndAdd(this.mSimSlot, "onSimRefresh");
        updateSimState(SimConstants.SIM_STATE.UNKNOWN);
        this.mIsimLoaded = false;
        this.notifySimReadyAlreadyDone = false;
        this.mSubscriptionId = -1;
        if (!this.mIsRefresh) {
            this.mIsRefresh = true;
            notifySimRefresh();
        }
        this.mTelephonyManager.clearCache();
    }

    /* access modifiers changed from: protected */
    public void onSimRemoved() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "onSimRemoved:");
        this.mIsimLoaded = false;
        this.notifySimReadyAlreadyDone = false;
        this.mSubscriptionId = -1;
        notifySimRemoved();
        this.mTelephonyManager.clearCache();
    }

    /* access modifiers changed from: protected */
    public void onSimNotReady() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "onSimNotReady");
        this.mEventLog.logAndAdd(this.mSimSlot, "onSimNotReady");
        updateSimState(SimConstants.SIM_STATE.UNKNOWN);
        this.mIsimLoaded = false;
        this.notifySimReadyAlreadyDone = false;
        this.mSubscriptionId = -1;
        notifySimRemoved();
        this.mTelephonyManager.clearCache();
    }

    public void handleMessage(Message message) {
        int i = this.mSimSlot;
        IMSLog.i(LOG_TAG, i, "handleMessage: what " + message.what);
        switch (message.what) {
            case 1:
                onSimStateChange((String) message.obj);
                notifySimStateChanged();
                return;
            case 2:
                notifyUiccChanged();
                return;
            case 3:
                onSimRefresh();
                return;
            case 5:
                onSoftphoneAuthFailed();
                return;
            case 6:
                onADSChanged(message.arg1);
                return;
            case 7:
                onImsSwitchUpdated(((Integer) message.obj).intValue());
                updateGlobalSetting(((Integer) message.obj).intValue());
                return;
            case 8:
                if (this.mMnoMap == null) {
                    this.mMnoMap = new MnoMap(this.mContext, this.mSimSlot);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void notifyADSChanged(int i) {
        sendMessage(obtainMessage(6, i, 0));
    }

    /* access modifiers changed from: package-private */
    public void onADSChanged(int i) {
        if (!hasVsim() && i == this.mSimSlot && this.mMnoInfoStorage.size() > 0) {
            SimConstants.SIM_STATE sim_state = this.mSimState;
            SimConstants.SIM_STATE sim_state2 = SimConstants.SIM_STATE.LOADED;
            if (sim_state == sim_state2 || (sim_state == SimConstants.SIM_STATE.INVALID_ISIM && this.mSimStatePrev == sim_state2)) {
                notifyMnoChanged();
            }
        }
    }

    public int getSimSlotCount() {
        return this.mTelephonyManager.getPhoneCount();
    }

    public int getSubscriptionId() {
        if (this.mSubscriptionId < 0) {
            this.mSubscriptionId = SimUtil.getSubId(this.mSimSlot);
        }
        return this.mSubscriptionId;
    }

    public int getSimSlotIndex() {
        return this.mSimSlot;
    }

    public String getHighestPriorityEhplmn() {
        return this.mHighestPriorityEhplmn;
    }

    public synchronized void setSubscriptionInfo(SubscriptionInfo subscriptionInfo) {
        if (!hasVsim()) {
            int i = this.mSimSlot;
            IMSLog.i(LOG_TAG, i, "setSubscriptionInfo : mSubscriptionId : " + this.mSubscriptionId + " => " + subscriptionInfo.getSubscriptionId() + " mSimSlot : " + this.mSimSlot + " => " + subscriptionInfo.getSimSlotIndex());
            this.mSubscriptionId = subscriptionInfo.getSubscriptionId();
            this.mSimSlot = subscriptionInfo.getSimSlotIndex();
            this.mHighestPriorityEhplmn = SimManagerUtils.getEhplmn(subscriptionInfo);
            int i2 = this.mSimSlot;
            StringBuilder sb = new StringBuilder();
            sb.append("Stored EHPLMN [");
            sb.append(this.mHighestPriorityEhplmn);
            sb.append("]");
            IMSLog.i(LOG_TAG, i2, sb.toString());
        }
    }

    public boolean isSimLoaded() {
        return this.mSimState == SimConstants.SIM_STATE.LOADED;
    }

    public String getMsisdn() {
        return this.mTelephonyManager.getMsisdn(getSubscriptionId());
    }

    public String getLine1Number() {
        return this.mTelephonyManager.getLine1Number();
    }

    public String getLine1Number(int i) {
        return this.mTelephonyManager.getLine1Number(i);
    }

    public String getImsi() {
        String subscriberId = this.mTelephonyManager.getSubscriberId(getSubscriptionId());
        if (!TextUtils.isEmpty(subscriberId)) {
            this.mImsi = subscriberId;
        }
        return this.mImsi;
    }

    public String getGid1() {
        return this.mTelephonyManager.getGroupIdLevel1(getSubscriptionId());
    }

    public String getImsiFromImpi() {
        if (TextUtils.isEmpty(this.mImsiFromImpi)) {
            return getImsi();
        }
        return this.mImsiFromImpi;
    }

    public void registerSimCardEventListener(ISimEventListener iSimEventListener) {
        SimConstants.SIM_STATE sim_state;
        synchronized (this.mEventListeners) {
            this.mEventListeners.add(iSimEventListener);
        }
        if (this.notifySimReadyAlreadyDone && (sim_state = this.mSimState) != SimConstants.SIM_STATE.UNKNOWN) {
            iSimEventListener.onReady(this.mSimSlot, sim_state != SimConstants.SIM_STATE.LOADED);
        }
    }

    public void deRegisterSimCardEventListener(ISimEventListener iSimEventListener) {
        synchronized (this.mEventListeners) {
            this.mEventListeners.remove(iSimEventListener);
        }
    }

    public String getImpi() {
        return this.mTelephonyManager.getIsimImpi(getSubscriptionId());
    }

    public String getSimSerialNumber() {
        return this.mTelephonyManager.getSimSerialNumber();
    }

    public int getSimState() {
        if (this.mTelephonyManager == null) {
            return 0;
        }
        if (getSimSlotCount() == 1) {
            return this.mTelephonyManager.getSimState();
        }
        return this.mTelephonyManager.getSimState(getSimSlotIndex());
    }

    public String getDerivedImpuFromMsisdn() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "getDerivedImpuFromMsisdn:");
        Mno simMno = getSimMno();
        String msisdn = getMsisdn();
        if (TextUtils.isEmpty(msisdn)) {
            IMSLog.e(LOG_TAG, this.mSimSlot, "getDerivedImpuFromMsisdn: msisdn is not found");
            return null;
        }
        int[] parseMccMnc = SimManagerUtils.parseMccMnc(this.mSimSlot, getSimOperator());
        if (parseMccMnc == null) {
            int i = this.mSimSlot;
            IMSLog.e(LOG_TAG, i, "getDerivedImpi: operator is invalid. operator=" + getSimOperator());
            return "111@example.com";
        } else if (simMno == Mno.BELL) {
            return String.format(Locale.US, "sip:%s@ims.bell.ca", new Object[]{msisdn});
        } else {
            if (simMno != Mno.LGU) {
                return String.format(Locale.US, "sip:%s@ims.mnc%03d.mcc%03d.3gppnetwork.org", new Object[]{msisdn, Integer.valueOf(parseMccMnc[1]), Integer.valueOf(parseMccMnc[0])});
            }
            if (msisdn.startsWith("+82")) {
                msisdn = msisdn.replace("+82", "0");
            }
            return String.format(Locale.US, "sip:%s@lte-lguplus.co.kr", new Object[]{msisdn});
        }
    }

    public String getDerivedImpi() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "getDerivedImpi:");
        Mno simMno = getSimMno();
        String subscriberId = this.mTelephonyManager.getSubscriberId(getSubscriptionId());
        if (subscriberId == null || subscriberId.isEmpty()) {
            IMSLog.e(LOG_TAG, this.mSimSlot, "getDerivedImpi: IMSI is not found. Using [sip:111@example.com]");
            return "111@example.com";
        }
        String simOperator = getSimOperator();
        if (!SimManagerUtils.isValidSimOperator(this.mSimSlot, simOperator)) {
            IMSLog.e(LOG_TAG, this.mSimSlot, "getDerivedImpi: operator is invalid");
            return null;
        }
        int parseInt = Integer.parseInt(simOperator.substring(0, 3));
        int parseInt2 = Integer.parseInt(simOperator.substring(3));
        if (simMno == Mno.LGU) {
            return String.format(Locale.US, "%s@lte-lguplus.co.kr", new Object[]{subscriberId});
        }
        if (simMno == Mno.TWM) {
            return String.format(Locale.US, "%s@ims.taiwanmobile.com", new Object[]{subscriberId});
        }
        return String.format(Locale.US, "%s@ims.mnc%03d.mcc%03d.3gppnetwork.org", new Object[]{subscriberId, Integer.valueOf(parseInt2), Integer.valueOf(parseInt)});
    }

    public String getDerivedImpu() {
        IMSLog.i(LOG_TAG, this.mSimSlot, "getDerivedImpu:");
        Mno simMno = getSimMno();
        String subscriberId = this.mTelephonyManager.getSubscriberId(getSubscriptionId());
        if (subscriberId == null || subscriberId.isEmpty()) {
            IMSLog.e(LOG_TAG, this.mSimSlot, "getDerivedImpu: IMSI is not found.");
            return null;
        }
        int[] parseMccMnc = SimManagerUtils.parseMccMnc(this.mSimSlot, getSimOperator());
        if (parseMccMnc == null) {
            return null;
        }
        if (simMno == Mno.TWM) {
            return String.format(Locale.US, "sip:%s@ims.taiwanmobile.com", new Object[]{subscriberId});
        }
        return String.format(Locale.US, "sip:%s@ims.mnc%03d.mcc%03d.3gppnetwork.org", new Object[]{subscriberId, Integer.valueOf(parseMccMnc[1]), Integer.valueOf(parseMccMnc[0])});
    }

    public List<String> getEfImpuList() {
        ArrayList arrayList = new ArrayList();
        String[] isimImpu = this.mTelephonyManager.getIsimImpu(getSubscriptionId());
        if (isimImpu == null) {
            return arrayList;
        }
        for (String str : isimImpu) {
            if (!(str == null || str.length() == 0)) {
                arrayList.add(str);
            }
        }
        return arrayList;
    }

    public String getImpuFromSim() {
        Mno simMno = getSimMno();
        if (!hasIsim()) {
            return simMno == Mno.LGU ? getDerivedImpuFromMsisdn() : getDerivedImpu();
        }
        String impuFromList = this.mSimDataAdaptor.getImpuFromList(getEfImpuList());
        if (impuFromList != null) {
            return impuFromList;
        }
        return simMno == Mno.LGU ? getDerivedImpuFromMsisdn() : getDerivedImpu();
    }

    public String getImpuFromIsim(int i) {
        String[] isimImpu = this.mTelephonyManager.getIsimImpu(getSubscriptionId());
        if (isimImpu == null || isimImpu.length < i - 1) {
            return null;
        }
        return isimImpu[i];
    }

    public String getEmergencyImpu() {
        if (this.mSimDataAdaptor == null) {
            this.mSimDataAdaptor = SimDataAdaptor.getSimDataAdaptor(this);
        }
        String emergencyImpu = this.mSimDataAdaptor.getEmergencyImpu(getEfImpuList());
        Mno simMno = getSimMno();
        if (emergencyImpu != null) {
            return emergencyImpu;
        }
        if (!hasNoSim()) {
            if (simMno == Mno.BELL) {
                String derivedImpuFromMsisdn = getDerivedImpuFromMsisdn();
                if (derivedImpuFromMsisdn != null) {
                    return derivedImpuFromMsisdn;
                }
            } else if (simMno != Mno.USCC) {
                return getDerivedImpu();
            }
        }
        return "sip:anonymous@anonymous.invalid";
    }

    public Mno getDevMno() {
        return this.mDevMno;
    }

    public Mno getNetMno() {
        return this.mNetMno;
    }

    public Mno getSimMno() {
        return SimUtil.getSimMno(this.mSimSlot);
    }

    public Mno getMnoFromNetworkPlmn(String str) {
        return Mno.fromName((String) this.mMnoMap.getMnoNamesFromNetworkPlmn(str).stream().findFirst().orElse("DEFAULT"));
    }

    public String getSimMnoName() {
        return this.mSimMnoName;
    }

    public void dump() {
        int i = this.mSimSlot;
        IMSLog.dump(LOG_TAG, i, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(LOG_TAG);
        int i2 = this.mSimSlot;
        IMSLog.dump(LOG_TAG, i2, "subID: " + this.mSubscriptionId);
        int i3 = this.mSimSlot;
        IMSLog.dump(LOG_TAG, i3, "mSimStatePrev: " + this.mSimStatePrev);
        int i4 = this.mSimSlot;
        IMSLog.dump(LOG_TAG, i4, "mSimState: " + this.mSimState);
        int i5 = this.mSimSlot;
        IMSLog.dump(LOG_TAG, i5, "mIsimLoaded: " + this.mIsimLoaded);
        int i6 = this.mSimSlot;
        IMSLog.dump(LOG_TAG, i6, "mIsOutBound: " + this.mIsOutBoundSIM);
        if (this.mSimDataAdaptor != null) {
            int i7 = this.mSimSlot;
            IMSLog.dump(LOG_TAG, i7, "mSimDataAdaptor : " + this.mSimDataAdaptor.getClass().getSimpleName());
        }
        if (this.mTelephonyManager != null) {
            if (!IMSLog.isShipBuild()) {
                int i8 = this.mSimSlot;
                IMSLog.dump(LOG_TAG, i8, "impi: " + this.mTelephonyManager.getIsimImpi(this.mSubscriptionId));
                int i9 = this.mSimSlot;
                IMSLog.dump(LOG_TAG, i9, "msisdn: " + this.mTelephonyManager.getMsisdn(getSubscriptionId()));
                int i10 = this.mSimSlot;
                IMSLog.dump(LOG_TAG, i10, "homedomainName: " + this.mTelephonyManager.getIsimDomain(this.mSubscriptionId));
                int i11 = this.mSimSlot;
                IMSLog.dump(LOG_TAG, i11, "impuFromSim[]: " + Arrays.toString(this.mTelephonyManager.getIsimImpu(this.mSubscriptionId)));
            }
            int i12 = this.mSimSlot;
            IMSLog.dump(LOG_TAG, i12, "operator: " + this.mTelephonyManager.getSimOperator(this.mSubscriptionId));
        }
        IMSLog.decreaseIndent(LOG_TAG);
        this.mEventLog.dump();
        this.mMnoMap.dump();
    }

    public SimpleEventLog getSimpleEventLog() {
        return this.mEventLog;
    }

    public ContentValues getMnoInfo() {
        return new ContentValues(this.mMnoInfoStorage.getAll());
    }

    /* access modifiers changed from: protected */
    public void setGtsAppInstalled(boolean z) {
        this.mIsGtsAppInstalled = z;
    }

    /* access modifiers changed from: protected */
    public boolean getGtsAppInstalled() {
        return this.mIsGtsAppInstalled;
    }

    /* access modifiers changed from: package-private */
    public String getMnoNameWithoutGcExtension(String str) {
        int indexOf = str.indexOf(Mno.GC_DELIMITER);
        return indexOf != -1 ? str.substring(0, indexOf) : str;
    }

    /* access modifiers changed from: package-private */
    public boolean isMnoHasGcBlockExtension(String str) {
        return str.toUpperCase().endsWith(Mno.GC_BLOCK_EXTENSION);
    }

    public boolean isEsim() {
        return "1".equals(SemSystemProperties.get("ril.simslottype" + (this.mSimSlot + 1), "0"));
    }

    /* access modifiers changed from: package-private */
    public int getSimMobilityType(List<ImsProfile> list) {
        boolean z = false;
        boolean z2 = false;
        for (ImsProfile next : list) {
            if (next.getSimMobility()) {
                z = true;
            }
            if (next.getSimMobilityForRcs()) {
                z2 = !DeviceUtil.isTablet() || !SimUtil.getMno(this.mSimSlot).isEmeasewaoce();
            }
        }
        if (z && z2) {
            return 3;
        }
        if (z) {
            return 1;
        }
        if (z2) {
            return 2;
        }
        return 0;
    }
}
