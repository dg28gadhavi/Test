package com.sec.internal.ims.config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.config.params.ACSConfig;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.interfaces.ims.config.IWorkflow;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ConfigModule extends Handler implements IConfigModule {
    private static final String AUTOCONF_TAG = "Autoconf";
    static final int AUTO_CONFIG_IMS_PDN = 1;
    static final int ERROR_WORKFLOW_IS_NULL = 708;
    static final int LOCAL_CONFIG_VERS = 59;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ConfigModule.class.getSimpleName();
    int m403ForbiddenCounter = 0;
    List<String> mAcsEncrNeededParams = Arrays.asList(new String[]{ConfigConstants.ConfigTable.CAPDISCOVERY_ALLOWED_PREFIXES.toLowerCase(), ConfigConstants.ConfigTable.CAPDISCOVERY_CAPINFO_EXPIRY.toLowerCase(), ConfigConstants.ConfigTable.CAPDISCOVERY_DEFAULT_DISC.toLowerCase(), ConfigConstants.ConfigTable.CAPDISCOVERY_DISABLE_INITIAL_SCAN.toLowerCase(), ConfigConstants.ConfigTable.CAPDISCOVERY_NON_RCS_CAPINFO_EXPIRY.toLowerCase(), ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_PERIOD.toLowerCase(), ConfigConstants.ConfigTable.IM_IM_MSG_TECH.toLowerCase(), ConfigConstants.ConfigTable.SERVICES_CHAT_AUTH.toLowerCase(), ConfigConstants.ConfigTable.SERVICES_GROUP_CHAT_AUTH.toLowerCase(), ConfigConstants.ConfigTable.SERVICES_FT_AUTH.toLowerCase(), ConfigConstants.ConfigTable.SERVICES_SLM_AUTH.toLowerCase(), ConfigConstants.ConfigTable.UX_CANCEL_MESSAGE_UX.toLowerCase(), "validity".toLowerCase(), "version".toLowerCase(), ConfigConstants.ConfigTable.SERVICES_RCS_DISABLED_STATE.toLowerCase(), ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH.toLowerCase(), ConfigConstants.ConfigTable.PRESENCE_THROTTLE_PUBLISH.toLowerCase(), ConfigConstants.ConfigTable.PRESENCE_MAX_SUBSCRIPTION_LIST.toLowerCase()});
    int mCallState = 0;
    PhoneIdKeyMap<Boolean> mClearTokenNeededList;
    ConfigComplete mConfigComplete;
    ConfigTrigger mConfigTrigger;
    /* access modifiers changed from: private */
    public final Context mContext;
    protected SimpleEventLog mEventLog;
    private String mIidToken = null;
    IntentReceiver mIntentReceiver;
    boolean mIsConfigModuleBootUp = false;
    boolean mIsMessagingReady = false;
    boolean mIsRcsEnabled = false;
    private SparseArray<IAutoConfigurationListener> mListener = new SparseArray<>();
    private boolean mMobileNetwork = false;
    private String mMsisdnNumber = null;
    boolean mNeedRetryOverNetwork = false;
    boolean mNeedRetryOverWifi = false;
    private PhoneIdKeyMap<HashMap<Integer, ConnectivityManager.NetworkCallback>> mNetworkListeners;
    PhoneIdKeyMap<HashMap<Integer, Network>> mNetworkLists;
    SparseArray<Message> mOnCompleteList = new SparseArray<>();
    boolean mPendingAutoComplete = false;
    boolean mPendingAutoConfig = false;
    boolean mPendingDeregi = false;
    PhoneIdKeyMap<Boolean> mReadyNetwork;
    private int mRetryCount = 1;
    IRegistrationManager mRm;
    PhoneIdKeyMap<Boolean> mSimRefreshReceivedList;
    private String mVerificationCode = null;
    boolean mWifiNetwork = false;
    WorkFlowController mWorkFlowController;
    SparseArray<HandlerThread> mWorkflowThreadList = new SparseArray<>();

    public Handler getHandler() {
        return this;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ConfigModule(Looper looper, Context context, IRegistrationManager iRegistrationManager) {
        super(looper);
        Context context2 = context;
        this.mContext = context2;
        this.mRm = iRegistrationManager;
        this.mEventLog = new SimpleEventLog(context2, "Autoconfig", 100);
    }

    public void initSequentially() {
        this.mIntentReceiver = new IntentReceiver();
        for (ISimManager iSimManager : SimManagerFactory.getAllSimManagers()) {
            if (iSimManager.getSimMno() == Mno.KT) {
                this.mIntentReceiver.addActionAirplaneMode();
            }
            iSimManager.registerForSimRefresh(this, 12, (Object) null);
            iSimManager.registerForSimRemoved(this, 12, (Object) null);
            iSimManager.registerForSimReady(this, 11, (Object) null);
        }
        Context context = this.mContext;
        IntentReceiver intentReceiver = this.mIntentReceiver;
        context.registerReceiver(intentReceiver, intentReceiver.getIntentFilter());
        int phoneCount = SimUtil.getPhoneCount();
        if (phoneCount > 1) {
            Log.d(LOG_TAG, " Registering for ADS");
            SimManagerFactory.registerForADSChange(this, 10, (Object) null);
        }
        this.mNetworkListeners = new PhoneIdKeyMap<>(phoneCount, null);
        this.mNetworkLists = new PhoneIdKeyMap<>(phoneCount, null);
        for (int i = 0; i < phoneCount; i++) {
            this.mNetworkLists.put(i, new HashMap());
            this.mNetworkListeners.put(i, new HashMap());
        }
        Boolean bool = Boolean.FALSE;
        this.mReadyNetwork = new PhoneIdKeyMap<>(phoneCount, bool);
        this.mSimRefreshReceivedList = new PhoneIdKeyMap<>(phoneCount, bool);
        this.mClearTokenNeededList = new PhoneIdKeyMap<>(phoneCount, bool);
        this.mWorkFlowController = new WorkFlowController(this.mContext);
        this.mConfigTrigger = new ConfigTrigger(this.mContext, this.mRm, this, this.mEventLog);
        this.mConfigComplete = new ConfigComplete(this.mContext, this.mRm, this, this.mEventLog);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:174:0x043a, code lost:
        setAcsTryReason(r2, com.sec.internal.constants.ims.DiagnosisConstants.RCSA_ATRE.PUSH_SMS);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:233:0x0660, code lost:
        r0.mConfigTrigger.setReadyStartForceCmd(r2, true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:234:0x0665, code lost:
        r0.mConfigTrigger.setReadyStartCmdList(r2, true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:235:0x066a, code lost:
        if (r3 != null) goto L_0x06b2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:236:0x066c, code lost:
        r1 = LOG_TAG;
        com.sec.internal.log.IMSLog.i(r1, "workflow is null");
        r3 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:237:0x0678, code lost:
        if (r3 == null) goto L_0x06aa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:239:0x067e, code lost:
        if (r3.hasNoSim() != false) goto L_0x06aa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:241:0x0688, code lost:
        if (android.text.TextUtils.isEmpty(r3.getSimMnoName()) == false) goto L_0x0691;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:243:0x068e, code lost:
        if (r3.hasVsim() != false) goto L_0x0691;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:244:0x0691, code lost:
        com.sec.internal.log.IMSLog.i(r1, "try init workflow and start again");
        sendMessage(obtainMessage(0, r2, 0, (java.lang.Object) null));
        sendMessage(obtainMessage(2, r2, 0, (java.lang.Object) null));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:245:0x06aa, code lost:
        com.sec.internal.log.IMSLog.i(r1, "sim is not ready, start config finished");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:246:0x06b2, code lost:
        r1 = LOG_TAG;
        com.sec.internal.log.IMSLog.i(r1, r2, "HANDLE_AUTO_CONFIG_START:");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:247:0x06bd, code lost:
        if (isGcEnabledChange(r2) == false) goto L_0x06e4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:249:0x06c5, code lost:
        if (com.sec.internal.ims.util.ConfigUtil.getGlobalGcEnabled(r0.mContext, r2) != false) goto L_0x06d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:251:0x06cb, code lost:
        if (com.sec.internal.constants.ims.ImsConstants.RCS_AS.JIBE.equals(r8) == false) goto L_0x06d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:252:0x06cd, code lost:
        com.sec.internal.log.IMSLog.i(r1, r2, "Change the GC RCS policy, set enableRcs");
        r3.setEnableRcsByMigration();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:253:0x06d6, code lost:
        com.sec.internal.log.IMSLog.i(r1, r2, "Change the GC RCS policy, clear AutoConfig Storage");
        r3.clearAutoConfigStorage(com.sec.internal.constants.ims.DiagnosisConstants.RCSA_TDRE.GCPOLICY_CHANGE);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:254:0x06e0, code lost:
        setIsGcEnabledChange(false, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:256:0x06e8, code lost:
        if (r3.checkNetworkConnectivity() != false) goto L_0x06f9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:257:0x06ea, code lost:
        r0.mConfigTrigger.tryAutoConfig(r3, r2, r0.mWorkFlowController.isSimInfochanged(r2, r12), r0.mMobileNetwork);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:258:0x06f9, code lost:
        if (r12 == false) goto L_0x070b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:259:0x06fb, code lost:
        com.sec.internal.log.IMSLog.i(r1, r2, "need CurConfig");
        r3.startCurConfig();
        r0.mConfigTrigger.setReadyStartCmdList(r2, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:261:0x0716, code lost:
        if (com.sec.internal.ims.registry.ImsRegistry.getInt(r2, com.sec.internal.constants.ims.settings.GlobalSettingsConstants.RCS.AUTO_CONFIG_PDN, 0) != 1) goto L_0x0734;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:263:0x0729, code lost:
        if (r0.mNetworkLists.get(r2).containsKey(2) == false) goto L_0x07cb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:264:0x072b, code lost:
        r0.mReadyNetwork.put(r2, java.lang.Boolean.TRUE);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:266:0x0738, code lost:
        if (isMobileDataOn() == false) goto L_0x076d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:268:0x073e, code lost:
        if (isRoamingMobileDataOn(r2) != false) goto L_0x0741;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:269:0x0741, code lost:
        if (r9 == null) goto L_0x07cb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:271:0x0747, code lost:
        if (r9.boolSetting(com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicy.PS_ONLY_NETWORK) == false) goto L_0x07cb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:273:0x074d, code lost:
        if (isMobileDataOn() == false) goto L_0x07cb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:275:0x0753, code lost:
        if (isWifiSwitchOn() == false) goto L_0x07cb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:276:0x0755, code lost:
        com.sec.internal.log.IMSLog.i(r1, r2, "Mobile Data ON & WIFI ON case for PS only network.");
        r5 = r0.mWorkFlowController.getCurrentRcsConfigVersion(r2);
        sendMessage(obtainMessage(3, r5, r5, java.lang.Integer.valueOf(r2)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:277:0x076d, code lost:
        com.sec.internal.log.IMSLog.i(r1, r2, "Mobile Data is off or roaming data off in roaming area");
        r5 = r0.mWorkFlowController.getCurrentRcsConfigVersion(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:278:0x077f, code lost:
        if (r7.contains("wifi") == false) goto L_0x0837;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:280:0x0785, code lost:
        if (isWifiSwitchOn() != false) goto L_0x0789;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:281:0x0789, code lost:
        com.sec.internal.log.IMSLog.i(r1, r2, "Mobile Data is off but WiFi is on");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:282:0x0792, code lost:
        if (com.sec.internal.ims.util.ConfigUtil.isRcsChn(r6) == false) goto L_0x0797;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:283:0x0794, code lost:
        r0.mMobileNetwork = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:284:0x0797, code lost:
        if (r9 == null) goto L_0x07b1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:286:0x079d, code lost:
        if (r9.boolSetting(com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicy.PS_ONLY_NETWORK) == false) goto L_0x07b1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:287:0x079f, code lost:
        com.sec.internal.log.IMSLog.i(r1, r2, "WiFi is on. Register to VOLTE to receive OTP message for PS only network");
        sendMessage(obtainMessage(3, r5, r5, java.lang.Integer.valueOf(r2)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:288:0x07b1, code lost:
        com.sec.internal.log.IMSLog.i(r1, r2, "Mobile Data is off but WiFi is on. So wait 20 seconds.");
        removeMessages(3, java.lang.Integer.valueOf(r2));
        sendMessageDelayed(obtainMessage(3, r5, r5, java.lang.Integer.valueOf(r2)), 20000);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:289:0x07cb, code lost:
        com.sec.internal.log.IMSLog.i(r1, r2, "Auto Config Start: ReadyNetwork = " + r0.mReadyNetwork.get(r2) + ", Start command = " + r0.mConfigTrigger.getReadyStartCmdList(r2));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:290:0x07ff, code lost:
        if (r0.mReadyNetwork.get(r2).booleanValue() == false) goto L_0x0811;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:291:0x0801, code lost:
        r0.mConfigTrigger.tryAutoConfig(r3, r2, r0.mWorkFlowController.isSimInfochanged(r2, false), r0.mMobileNetwork);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:292:0x0811, code lost:
        if (r9 == null) goto L_0x0819;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:294:0x0817, code lost:
        if (r9.boolSetting(com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicy.PS_ONLY_NETWORK) != false) goto L_0x0823;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:296:0x081b, code lost:
        if (r6 != com.sec.internal.constants.Mno.BELL) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:298:0x0821, code lost:
        if (getAvailableNetwork(r2) != null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:299:0x0823, code lost:
        com.sec.internal.log.IMSLog.i(r1, r2, "No conditions satisfied to start Auto Config, proceed to VOLTE REG");
        sendMessage(obtainMessage(3, 0, 0, java.lang.Integer.valueOf(r2)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:300:0x0837, code lost:
        com.sec.internal.log.IMSLog.i(r1, r2, "Both Mobile Data and WiFi are off, skip autoconfig");
        sendMessage(obtainMessage(3, r5, r5, java.lang.Integer.valueOf(r2)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:388:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:389:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:390:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:391:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:392:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:393:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:394:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:395:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:396:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(android.os.Message r17) {
        /*
            r16 = this;
            r0 = r16
            r1 = r17
            super.handleMessage(r17)
            int r2 = r1.arg1
            int r3 = r1.what
            java.lang.String r4 = "phoneId"
            r5 = 3
            if (r3 == r5) goto L_0x002b
            r6 = 17
            if (r3 == r6) goto L_0x002b
            r6 = 4
            if (r3 == r6) goto L_0x002b
            r6 = 21
            if (r3 != r6) goto L_0x001c
            goto L_0x002b
        L_0x001c:
            r6 = 13
            if (r3 != r6) goto L_0x0033
            java.lang.Object r3 = r1.obj
            if (r3 == 0) goto L_0x0033
            android.os.Bundle r3 = (android.os.Bundle) r3
            int r2 = r3.getInt(r4)
            goto L_0x0033
        L_0x002b:
            java.lang.Object r2 = r1.obj
            java.lang.Integer r2 = (java.lang.Integer) r2
            int r2 = r2.intValue()
        L_0x0033:
            com.sec.internal.ims.config.WorkFlowController r3 = r0.mWorkFlowController
            com.sec.internal.interfaces.ims.config.IWorkflow r3 = r3.getWorkflow(r2)
            com.sec.internal.constants.Mno r6 = com.sec.internal.helper.SimUtil.getSimMno(r2)
            java.lang.String r7 = com.sec.internal.ims.util.ConfigUtil.getNetworkType(r2)
            java.lang.String r8 = com.sec.internal.ims.util.ConfigUtil.getAcsServerType(r2)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r9 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r2)
            r10 = 1
            if (r9 == 0) goto L_0x0054
            boolean r12 = r9.isRemoteConfigNeeded(r2)
            if (r12 == 0) goto L_0x0054
            r12 = r10
            goto L_0x0055
        L_0x0054:
            r12 = 0
        L_0x0055:
            java.lang.String r13 = LOG_TAG
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            java.lang.String r15 = "handleMessage: msg: "
            r14.append(r15)
            int r15 = r1.what
            r14.append(r15)
            java.lang.String r14 = r14.toString()
            com.sec.internal.log.IMSLog.i(r13, r2, r14)
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            java.lang.String r15 = "rcsNetworkType: "
            r14.append(r15)
            r14.append(r7)
            java.lang.String r15 = " rcsAs: "
            r14.append(r15)
            r14.append(r8)
            java.lang.String r15 = " isRemoteConfigNeeded: "
            r14.append(r15)
            r14.append(r12)
            java.lang.String r14 = r14.toString()
            com.sec.internal.log.IMSLog.i(r13, r2, r14)
            int r14 = r1.what
            java.lang.String r15 = "jibe"
            java.lang.String r5 = "lastError"
            r11 = 2
            switch(r14) {
                case 0: goto L_0x084a;
                case 1: goto L_0x0660;
                case 2: goto L_0x0665;
                case 3: goto L_0x0441;
                case 4: goto L_0x03f7;
                case 5: goto L_0x03e8;
                case 6: goto L_0x03d9;
                case 7: goto L_0x03d0;
                case 8: goto L_0x038a;
                case 9: goto L_0x037c;
                case 10: goto L_0x030d;
                case 11: goto L_0x02fc;
                case 12: goto L_0x02dc;
                case 13: goto L_0x01ff;
                case 14: goto L_0x01f6;
                case 15: goto L_0x0197;
                case 16: goto L_0x018d;
                case 17: goto L_0x0181;
                case 18: goto L_0x0177;
                case 19: goto L_0x0111;
                case 20: goto L_0x0108;
                case 21: goto L_0x043a;
                case 22: goto L_0x00ff;
                case 23: goto L_0x00f6;
                case 24: goto L_0x00dc;
                case 25: goto L_0x00b4;
                case 26: goto L_0x00ad;
                case 27: goto L_0x00a4;
                default: goto L_0x009c;
            }
        L_0x009c:
            java.lang.String r0 = "unknown message"
            com.sec.internal.log.IMSLog.i(r13, r2, r0)
            goto L_0x0922
        L_0x00a4:
            if (r3 == 0) goto L_0x0922
            java.lang.String r0 = r0.mIidToken
            r3.sendIidToken(r0)
            goto L_0x0922
        L_0x00ad:
            com.sec.internal.ims.config.ConfigTrigger r0 = r0.mConfigTrigger
            r0.sendRcsAutoconfigStart(r2)
            goto L_0x0922
        L_0x00b4:
            com.sec.internal.helper.PhoneIdKeyMap<java.util.HashMap<java.lang.Integer, android.net.Network>> r3 = r0.mNetworkLists
            java.lang.Object r3 = r3.get(r2)
            java.util.HashMap r3 = (java.util.HashMap) r3
            int r1 = r1.arg2
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r3.remove(r1)
            if (r9 == 0) goto L_0x00d7
            java.lang.String r1 = "dual_simhandling"
            boolean r1 = r9.boolSetting(r1)
            if (r1 == 0) goto L_0x00d7
            java.lang.String r1 = "Clear workflow"
            com.sec.internal.log.IMSLog.i(r13, r2, r1)
            r0.clearWorkFlow(r2)
        L_0x00d7:
            r0.processConnectionChange(r2, r12)
            goto L_0x0922
        L_0x00dc:
            com.sec.internal.helper.PhoneIdKeyMap<java.util.HashMap<java.lang.Integer, android.net.Network>> r3 = r0.mNetworkLists
            java.lang.Object r3 = r3.get(r2)
            java.util.HashMap r3 = (java.util.HashMap) r3
            int r4 = r1.arg2
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            java.lang.Object r1 = r1.obj
            android.net.Network r1 = (android.net.Network) r1
            r3.put(r4, r1)
            r0.processConnectionChange(r2, r12)
            goto L_0x0922
        L_0x00f6:
            r0.mIsConfigModuleBootUp = r10
            com.sec.internal.ims.config.WorkFlowController r0 = r0.mWorkFlowController
            r0.onBootCompleted()
            goto L_0x0922
        L_0x00ff:
            if (r3 == 0) goto L_0x0922
            com.sec.internal.constants.ims.DiagnosisConstants$RCSA_TDRE r0 = com.sec.internal.constants.ims.DiagnosisConstants.RCSA_TDRE.FORBIDDEN_ERROR
            r3.clearAutoConfigStorage(r0)
            goto L_0x0922
        L_0x0108:
            if (r3 == 0) goto L_0x0922
            java.lang.String r0 = r0.mMsisdnNumber
            r3.sendMsisdnNumber(r0)
            goto L_0x0922
        L_0x0111:
            java.lang.String r1 = "HANDLE_AUTO_CONFIG_RESTART:"
            com.sec.internal.log.IMSLog.i(r13, r2, r1)
            com.sec.internal.ims.config.ConfigTrigger r1 = r0.mConfigTrigger
            r1.setReadyStartCmdList(r2, r10)
            if (r3 != 0) goto L_0x0132
            java.lang.String r1 = "workflow is null. skip autoconfig"
            com.sec.internal.log.IMSLog.i(r13, r2, r1)
            java.lang.Integer r1 = java.lang.Integer.valueOf(r2)
            r2 = 0
            r3 = 3
            android.os.Message r1 = r0.obtainMessage(r3, r2, r2, r1)
            r0.sendMessage(r1)
            goto L_0x0922
        L_0x0132:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r4 = "ReadyNetwork: "
            r1.append(r4)
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r4 = r0.mReadyNetwork
            java.lang.Object r4 = r4.get(r2)
            r1.append(r4)
            java.lang.String r4 = ", Start command: "
            r1.append(r4)
            com.sec.internal.ims.config.ConfigTrigger r4 = r0.mConfigTrigger
            boolean r4 = r4.getReadyStartCmdList(r2)
            r1.append(r4)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.i(r13, r2, r1)
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r1 = r0.mReadyNetwork
            java.lang.Object r1 = r1.get(r2)
            java.lang.Boolean r1 = (java.lang.Boolean) r1
            boolean r1 = r1.booleanValue()
            if (r1 == 0) goto L_0x0922
            com.sec.internal.ims.config.ConfigTrigger r1 = r0.mConfigTrigger
            com.sec.internal.ims.config.WorkFlowController r4 = r0.mWorkFlowController
            boolean r4 = r4.isSimInfochanged(r2, r12)
            boolean r0 = r0.mMobileNetwork
            r1.tryAutoConfig(r3, r2, r4, r0)
            goto L_0x0922
        L_0x0177:
            if (r3 == 0) goto L_0x017c
            r3.onDefaultSmsPackageChanged()
        L_0x017c:
            r0.notifyDefaultSmsChanged(r2)
            goto L_0x0922
        L_0x0181:
            java.lang.String r1 = "HANDLE_AUTO_CONFIG_START_WITH_SUITABLE_NETWORK retrigger ACS with best network"
            com.sec.internal.log.IMSLog.i(r13, r2, r1)
            com.sec.internal.ims.config.ConfigTrigger r0 = r0.mConfigTrigger
            r0.setReadyStartCmdList(r2, r10)
            goto L_0x0922
        L_0x018d:
            r0.init(r2)
            if (r3 == 0) goto L_0x0922
            r3.handleMSISDNDialog()
            goto L_0x0922
        L_0x0197:
            int r2 = r1.arg1
            java.lang.Object r1 = r1.obj
            if (r1 == 0) goto L_0x01a4
            java.lang.Boolean r1 = (java.lang.Boolean) r1
            boolean r1 = r1.booleanValue()
            goto L_0x01a5
        L_0x01a4:
            r1 = r10
        L_0x01a5:
            com.sec.internal.ims.config.params.ACSConfig r3 = r0.getAcsConfig(r2)
            r3.resetAcsSettings()
            com.sec.internal.interfaces.ims.core.IRegistrationManager r3 = r0.mRm
            java.util.List r3 = r3.getPendingRegistration(r2)
            if (r3 == 0) goto L_0x0922
            com.sec.internal.ims.config.params.ACSConfig r4 = r0.getAcsConfig(r2)
            boolean r4 = r4.isRcsDormantMode()
            if (r4 == 0) goto L_0x01d7
            android.content.Context r4 = r0.mContext
            android.content.res.Resources r5 = r4.getResources()
            r6 = 2131165237(0x7f070035, float:1.7944685E38)
            java.lang.String r5 = r5.getString(r6)
            android.widget.Toast r4 = android.widget.Toast.makeText(r4, r5, r10)
            r4.show()
            r0.triggerAutoConfig(r1, r2, r3)
            goto L_0x0922
        L_0x01d7:
            boolean r4 = r6.isKor()
            if (r4 != 0) goto L_0x01f1
            boolean r4 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((com.sec.internal.constants.Mno) r6)
            if (r4 != 0) goto L_0x01f1
            boolean r4 = r15.equals(r8)
            if (r4 == 0) goto L_0x0922
            com.sec.internal.interfaces.ims.core.IRegistrationManager r4 = r0.mRm
            boolean r4 = com.sec.internal.ims.util.ConfigUtil.hasChatbotService(r2, r4)
            if (r4 == 0) goto L_0x0922
        L_0x01f1:
            r0.triggerAutoConfig(r1, r2, r3)
            goto L_0x0922
        L_0x01f6:
            int r2 = r1.arg1
            int r1 = r1.arg2
            r0.onTelephonyCallStatusChanged(r2, r1)
            goto L_0x0922
        L_0x01ff:
            java.lang.Object r1 = r1.obj
            if (r1 == 0) goto L_0x020a
            android.os.Bundle r1 = (android.os.Bundle) r1
            int r1 = r1.getInt(r5)
            goto L_0x020b
        L_0x020a:
            r1 = -1
        L_0x020b:
            boolean r3 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((com.sec.internal.constants.Mno) r6)
            if (r3 != 0) goto L_0x022b
            boolean r3 = com.sec.internal.ims.util.ConfigUtil.isRcsChn(r6)
            if (r3 != 0) goto L_0x022b
            int r3 = r0.mCallState
            if (r3 == 0) goto L_0x022b
            java.lang.String r3 = "Pending Autoconfig comlete event on active call"
            com.sec.internal.log.IMSLog.i(r13, r2, r3)
            r0.mPendingAutoComplete = r10
            com.sec.internal.ims.config.params.ACSConfig r0 = r0.getAcsConfig(r2)
            r0.setAcsLastError(r1)
            goto L_0x0922
        L_0x022b:
            com.sec.internal.interfaces.ims.core.IRegistrationManager r3 = r0.mRm
            java.util.List r3 = r3.getPendingRegistration(r2)
            if (r3 == 0) goto L_0x0922
            android.content.Context r4 = r0.mContext
            boolean r4 = com.sec.internal.ims.rcs.util.RcsUtils.DualRcs.isRegAllowed(r4, r2)
            if (r4 == 0) goto L_0x02b5
            com.sec.internal.ims.config.ConfigComplete r4 = r0.mConfigComplete
            int r5 = r0.m403ForbiddenCounter
            r4.setStateforACSComplete(r1, r2, r3, r5)
            com.sec.internal.ims.config.ConfigComplete r4 = r0.mConfigComplete
            com.sec.internal.ims.config.WorkFlowController r5 = r0.mWorkFlowController
            com.sec.internal.interfaces.ims.config.IWorkflow r5 = r5.getWorkflow(r2)
            r4.handleAutoconfigurationComplete(r2, r3, r1, r5)
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.KT
            if (r6 != r3) goto L_0x0922
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "mNeedRetryOverWifi = "
            r3.append(r4)
            boolean r4 = r0.mNeedRetryOverWifi
            r3.append(r4)
            java.lang.String r4 = ", mWifiNetwork = "
            r3.append(r4)
            boolean r4 = r0.mWifiNetwork
            r3.append(r4)
            java.lang.String r4 = ", lastErrorCode = "
            r3.append(r4)
            r3.append(r1)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.i(r13, r2, r3)
            boolean r3 = r0.mNeedRetryOverWifi
            if (r3 == 0) goto L_0x0922
            if (r1 == 0) goto L_0x02a2
            r3 = 800(0x320, float:1.121E-42)
            if (r1 == r3) goto L_0x02a2
            r3 = 801(0x321, float:1.122E-42)
            if (r1 == r3) goto L_0x02a2
            r3 = 802(0x322, float:1.124E-42)
            if (r1 == r3) goto L_0x02a2
            r3 = 803(0x323, float:1.125E-42)
            if (r1 == r3) goto L_0x02a2
            r3 = 804(0x324, float:1.127E-42)
            if (r1 == r3) goto L_0x02a2
            r3 = 805(0x325, float:1.128E-42)
            if (r1 != r3) goto L_0x0298
            goto L_0x02a2
        L_0x0298:
            java.lang.String r1 = "clear mNeedRetryOverWifi to false"
            com.sec.internal.log.IMSLog.i(r13, r2, r1)
            r1 = 0
            r0.mNeedRetryOverWifi = r1
            goto L_0x0922
        L_0x02a2:
            boolean r1 = r0.mWifiNetwork
            if (r1 == 0) goto L_0x0922
            java.lang.String r1 = "reset AcsSettings for KT over Wifi"
            com.sec.internal.log.IMSLog.i(r13, r2, r1)
            com.sec.internal.ims.config.params.ACSConfig r0 = r0.getAcsConfig(r2)
            r0.resetAcsSettings()
            goto L_0x0922
        L_0x02b5:
            java.util.Iterator r0 = r3.iterator()
        L_0x02b9:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x02d3
            java.lang.Object r1 = r0.next()
            com.sec.internal.interfaces.ims.core.IRegisterTask r1 = (com.sec.internal.interfaces.ims.core.IRegisterTask) r1
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r3 = r1.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r4 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONFIGURING
            if (r3 != r4) goto L_0x02b9
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r3 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.IDLE
            r1.setState(r3)
            goto L_0x02b9
        L_0x02d3:
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "ADS set to other SIM"
            com.sec.internal.log.IMSLog.i(r0, r2, r1)
            goto L_0x0922
        L_0x02dc:
            java.lang.Object r1 = r1.obj
            com.sec.internal.helper.AsyncResult r1 = (com.sec.internal.helper.AsyncResult) r1
            java.lang.Object r1 = r1.result
            java.lang.Integer r1 = (java.lang.Integer) r1
            int r1 = r1.intValue()
            r0.onSimRefresh(r1)
            boolean r1 = r6.isKor()
            if (r1 == 0) goto L_0x0922
            java.lang.String r1 = "sim state changed, reset to MSISDN_FROM_PAU"
            com.sec.internal.log.IMSLog.i(r13, r2, r1)
            r0.resetMsisdnFromPau(r2)
            goto L_0x0922
        L_0x02fc:
            java.lang.Object r1 = r1.obj
            com.sec.internal.helper.AsyncResult r1 = (com.sec.internal.helper.AsyncResult) r1
            java.lang.Object r1 = r1.result
            java.lang.Integer r1 = (java.lang.Integer) r1
            int r1 = r1.intValue()
            r0.onSimReady(r1, r12)
            goto L_0x0922
        L_0x030d:
            r0.getAvailableNetwork(r2)
            com.sec.internal.ims.config.ConfigTrigger r1 = r0.mConfigTrigger
            r3 = 0
            r1.setReadyStartCmdList(r2, r3)
            com.sec.internal.interfaces.ims.core.IRegistrationManager r1 = r0.mRm
            java.util.List r1 = r1.getPendingRegistration(r2)
            if (r1 == 0) goto L_0x034a
            java.util.Iterator r1 = r1.iterator()
        L_0x0322:
            boolean r3 = r1.hasNext()
            if (r3 == 0) goto L_0x034a
            java.lang.Object r3 = r1.next()
            com.sec.internal.interfaces.ims.core.IRegisterTask r3 = (com.sec.internal.interfaces.ims.core.IRegisterTask) r3
            boolean r4 = r3.isRcsOnly()
            if (r4 == 0) goto L_0x0322
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r4 = r3.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONFIGURING
            if (r4 != r5) goto L_0x0322
            java.lang.String r4 = LOG_TAG
            java.lang.String r5 = "task is set as IDLE because of ads change."
            com.sec.internal.log.IMSLog.i(r4, r2, r5)
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r4 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.IDLE
            r3.setState(r4)
            goto L_0x0322
        L_0x034a:
            boolean r1 = r0.isValidAcsVersion(r2)
            if (r1 == 0) goto L_0x035a
            com.sec.internal.ims.config.params.ACSConfig r1 = r0.getAcsConfig(r2)
            boolean r1 = r1.isAcsCompleted()
            if (r1 != 0) goto L_0x0370
        L_0x035a:
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.BELL
            com.sec.internal.constants.Mno[] r1 = new com.sec.internal.constants.Mno[]{r1}
            boolean r1 = r6.isOneOf(r1)
            if (r1 == 0) goto L_0x0370
            android.content.Context r1 = r0.mContext
            boolean r1 = com.sec.internal.ims.rcs.util.RcsUtils.UiUtils.isRcsEnabledinSettings(r1, r2)
            if (r1 == 0) goto L_0x0370
            r0.mNeedRetryOverNetwork = r10
        L_0x0370:
            r0.onADSChanged(r2)
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "Network configs are reset"
            com.sec.internal.log.IMSLog.i(r0, r2, r1)
            goto L_0x0922
        L_0x037c:
            r0.setDualSimRcsAutoConfig(r10)
            r1 = 0
            r3 = 0
            android.os.Message r1 = r0.obtainMessage(r11, r2, r3, r1)
            r0.sendMessage(r1)
            goto L_0x0922
        L_0x038a:
            if (r3 == 0) goto L_0x0922
            java.lang.String r1 = r0.getRcsProfile(r2)
            boolean r1 = com.sec.ims.settings.ImsProfile.isRcsUpProfile(r1)
            if (r1 == 0) goto L_0x03c9
            boolean r1 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((com.sec.internal.constants.Mno) r6)
            if (r1 != 0) goto L_0x03a2
            boolean r1 = com.sec.internal.ims.util.ConfigUtil.isRcsChn(r6)
            if (r1 == 0) goto L_0x03c9
        L_0x03a2:
            boolean r1 = r0.mPendingAutoConfig
            if (r1 != 0) goto L_0x03b1
            boolean r1 = r0.mIsRcsEnabled
            if (r1 != 0) goto L_0x03ac
            r0.mPendingAutoConfig = r10
        L_0x03ac:
            r3.changeOpMode(r1)
            goto L_0x0922
        L_0x03b1:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "pending AutoConfig mIsRcsEnabled: "
            r1.append(r3)
            boolean r0 = r0.mIsRcsEnabled
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            com.sec.internal.log.IMSLog.i(r13, r2, r0)
            goto L_0x0922
        L_0x03c9:
            boolean r0 = r0.mIsRcsEnabled
            r3.changeOpMode(r0)
            goto L_0x0922
        L_0x03d0:
            if (r3 == 0) goto L_0x0922
            java.lang.String r0 = r0.mVerificationCode
            r3.sendVerificationCode(r0)
            goto L_0x0922
        L_0x03d9:
            if (r3 == 0) goto L_0x0922
            android.util.SparseArray<com.sec.ims.IAutoConfigurationListener> r0 = r0.mListener
            java.lang.Object r0 = r0.get(r2)
            com.sec.ims.IAutoConfigurationListener r0 = (com.sec.ims.IAutoConfigurationListener) r0
            r3.unregisterAutoConfigurationListener(r0)
            goto L_0x0922
        L_0x03e8:
            if (r3 == 0) goto L_0x0922
            android.util.SparseArray<com.sec.ims.IAutoConfigurationListener> r0 = r0.mListener
            java.lang.Object r0 = r0.get(r2)
            com.sec.ims.IAutoConfigurationListener r0 = (com.sec.ims.IAutoConfigurationListener) r0
            r3.registerAutoConfigurationListener(r0)
            goto L_0x0922
        L_0x03f7:
            com.sec.internal.ims.config.ConfigTrigger r1 = r0.mConfigTrigger
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.MTS_RUSSIA
            if (r6 == r4) goto L_0x03ff
            r4 = r10
            goto L_0x0400
        L_0x03ff:
            r4 = 0
        L_0x0400:
            r1.setNeedResetConfig(r4)
            com.sec.internal.interfaces.ims.core.IRegistrationManager r1 = r0.mRm
            java.util.List r1 = r1.getPendingRegistration(r2)
            if (r1 == 0) goto L_0x0433
            java.util.Iterator r1 = r1.iterator()
        L_0x040f:
            boolean r4 = r1.hasNext()
            if (r4 == 0) goto L_0x0433
            java.lang.Object r4 = r1.next()
            com.sec.internal.interfaces.ims.core.IRegisterTask r4 = (com.sec.internal.interfaces.ims.core.IRegisterTask) r4
            boolean r5 = r4.isRcsOnly()
            if (r5 == 0) goto L_0x040f
            com.sec.ims.settings.ImsProfile r4 = r4.getProfile()
            boolean r4 = r4.getNeedAutoconfig()
            if (r4 == 0) goto L_0x040f
            com.sec.internal.interfaces.ims.core.IRegistrationManager r4 = r0.mRm
            r5 = 143(0x8f, float:2.0E-43)
            r4.sendDeregister((int) r5, (int) r2)
            goto L_0x040f
        L_0x0433:
            com.sec.internal.ims.config.params.ACSConfig r1 = r0.getAcsConfig(r2)
            r1.setIsTriggeredByNrcr(r10)
        L_0x043a:
            com.sec.internal.constants.ims.DiagnosisConstants$RCSA_ATRE r1 = com.sec.internal.constants.ims.DiagnosisConstants.RCSA_ATRE.PUSH_SMS
            r0.setAcsTryReason(r2, r1)
            goto L_0x0660
        L_0x0441:
            java.lang.String r7 = "HANDLE_AUTO_CONFIG_COMPLETE:"
            com.sec.internal.log.IMSLog.i(r13, r2, r7)
            com.sec.internal.ims.config.ConfigTrigger r7 = r0.mConfigTrigger
            r7.resetReAutoConfigOption(r2)
            int r7 = r1.arg1
            int r1 = r1.arg2
            if (r3 != 0) goto L_0x0454
            r3 = 708(0x2c4, float:9.92E-43)
            goto L_0x0458
        L_0x0454:
            int r3 = r3.getLastErrorCode()
        L_0x0458:
            com.sec.internal.interfaces.ims.core.ISimManager r14 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r2)
            com.sec.internal.helper.SimpleEventLog r15 = r0.mEventLog
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r8 = "Autoconfig complete: old version = "
            r11.append(r8)
            r11.append(r7)
            java.lang.String r8 = ", new version = "
            r11.append(r8)
            r11.append(r1)
            java.lang.String r8 = ", last errorcode = "
            r11.append(r8)
            r11.append(r3)
            java.lang.String r8 = r11.toString()
            r15.logAndAdd(r2, r8)
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r2)
            java.lang.String r11 = ",OV:"
            r8.append(r11)
            r8.append(r7)
            java.lang.String r11 = ",NV:"
            r8.append(r11)
            r8.append(r1)
            java.lang.String r11 = ",LEC:"
            r8.append(r11)
            r8.append(r3)
            java.lang.String r8 = r8.toString()
            r11 = 318767110(0x13000006, float:1.6155883E-27)
            com.sec.internal.log.IMSLog.c(r11, r8)
            if (r12 == 0) goto L_0x04b7
            if (r1 <= 0) goto L_0x04b7
            r8 = 59
            if (r1 != r8) goto L_0x04b5
            goto L_0x04b7
        L_0x04b5:
            r8 = 0
            goto L_0x04b8
        L_0x04b7:
            r8 = r10
        L_0x04b8:
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r15 = "localConfigUsedState: "
            r11.append(r15)
            r11.append(r8)
            java.lang.String r11 = r11.toString()
            com.sec.internal.log.IMSLog.i(r13, r2, r11)
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            r11.append(r2)
            java.lang.String r15 = ",LCUS:"
            r11.append(r15)
            r11.append(r8)
            java.lang.String r11 = r11.toString()
            r15 = 318767116(0x1300000c, float:1.6155894E-27)
            com.sec.internal.log.IMSLog.c(r15, r11)
            if (r9 == 0) goto L_0x04eb
            r9.updateLocalConfigUsedState(r8)
        L_0x04eb:
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "AcsTryReason: "
            r8.append(r9)
            com.sec.internal.constants.ims.DiagnosisConstants$RCSA_ATRE r9 = r0.getAcsTryReason(r2)
            java.lang.String r9 = r9.toString()
            r8.append(r9)
            java.lang.String r8 = r8.toString()
            com.sec.internal.log.IMSLog.i(r13, r2, r8)
            r8 = 987(0x3db, float:1.383E-42)
            if (r3 == r8) goto L_0x053c
            com.sec.internal.ims.config.ConfigComplete r8 = r0.mConfigComplete
            r8.sendRCSAInfoToHQM(r1, r3, r2)
            android.content.ContentValues r8 = new android.content.ContentValues
            r8.<init>()
            r9 = 200(0xc8, float:2.8E-43)
            if (r3 != r9) goto L_0x0523
            java.lang.String r9 = "RACC"
            java.lang.Integer r11 = java.lang.Integer.valueOf(r10)
            r8.put(r9, r11)
            goto L_0x052c
        L_0x0523:
            java.lang.String r9 = "RACF"
            java.lang.Integer r11 = java.lang.Integer.valueOf(r10)
            r8.put(r9, r11)
        L_0x052c:
            java.lang.String r9 = "overwrite_mode"
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)
            r8.put(r9, r10)
            android.content.Context r9 = r0.mContext
            java.lang.String r10 = "DRCS"
            com.sec.internal.ims.diagnosis.ImsLogAgentUtil.storeLogToAgent(r2, r9, r10, r8)
        L_0x053c:
            r8 = 800(0x320, float:1.121E-42)
            if (r3 != r8) goto L_0x056d
            java.lang.String r1 = "SSL Handshake failed"
            com.sec.internal.log.IMSLog.i(r13, r2, r1)
            r0.startAcsWithDelay(r2)
            android.util.SparseArray<android.os.Message> r1 = r0.mOnCompleteList
            java.lang.Object r1 = r1.get(r2)
            android.os.Message r1 = (android.os.Message) r1
            if (r1 == 0) goto L_0x0922
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.RJIL
            if (r6 != r7) goto L_0x0922
            android.os.Bundle r6 = new android.os.Bundle
            r6.<init>()
            r6.putInt(r4, r2)
            r6.putInt(r5, r3)
            r1.obj = r6
            r1.sendToTarget()
            android.util.SparseArray<android.os.Message> r0 = r0.mOnCompleteList
            r0.remove(r2)
            goto L_0x0922
        L_0x056d:
            if (r14 == 0) goto L_0x0580
            boolean r8 = r14.isSimAvailable()
            if (r8 != 0) goto L_0x0580
            r8 = 708(0x2c4, float:9.92E-43)
            if (r3 != r8) goto L_0x0580
            java.lang.String r0 = "autoconfiguration failed because sim is unavailable."
            com.sec.internal.log.IMSLog.i(r13, r2, r0)
            goto L_0x0922
        L_0x0580:
            android.util.SparseArray<android.os.Message> r8 = r0.mOnCompleteList
            java.lang.Object r8 = r8.get(r2)
            android.os.Message r8 = (android.os.Message) r8
            if (r8 == 0) goto L_0x05b5
            java.lang.String r7 = "send complete message"
            com.sec.internal.log.IMSLog.i(r13, r2, r7)
            if (r1 == 0) goto L_0x059e
            boolean r1 = com.sec.internal.ims.util.ConfigUtil.isRcsChn(r6)
            if (r1 == 0) goto L_0x059e
            com.sec.internal.ims.config.ConfigTrigger r1 = r0.mConfigTrigger
            r6 = 0
            r1.setReadyStartCmdList(r2, r6)
        L_0x059e:
            android.os.Bundle r1 = new android.os.Bundle
            r1.<init>()
            r1.putInt(r4, r2)
            r1.putInt(r5, r3)
            r8.obj = r1
            r8.sendToTarget()
            android.util.SparseArray<android.os.Message> r1 = r0.mOnCompleteList
            r1.remove(r2)
            goto L_0x0634
        L_0x05b5:
            if (r12 == 0) goto L_0x05d7
            java.lang.String r1 = "complete autoconfiguration and send EVENT_AUTOCONFIGURATION_COMPLETE msg"
            com.sec.internal.log.IMSLog.i(r13, r2, r1)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r2)
            java.lang.String r4 = ",CONINFO:CHA"
            r1.append(r4)
            java.lang.String r1 = r1.toString()
            r4 = 318767117(0x1300000d, float:1.6155896E-27)
            com.sec.internal.log.IMSLog.c(r4, r1)
            r0.onNewRcsConfigurationAvailable(r2, r3)
            goto L_0x0634
        L_0x05d7:
            boolean r4 = r6.isKor()
            if (r4 != 0) goto L_0x0613
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.ATT
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.TMOUS
            com.sec.internal.constants.Mno[] r4 = new com.sec.internal.constants.Mno[]{r4, r5}
            boolean r4 = r6.isOneOf(r4)
            if (r4 != 0) goto L_0x0613
            boolean r4 = com.sec.internal.ims.util.ConfigUtil.isRcsChn(r6)
            if (r4 != 0) goto L_0x0613
            if (r7 != r1) goto L_0x0613
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r2)
            java.lang.String r3 = ",CONINFO:NONCHA"
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            r3 = 318767118(0x1300000e, float:1.6155898E-27)
            com.sec.internal.log.IMSLog.c(r3, r1)
            com.sec.internal.helper.SimpleEventLog r1 = r0.mEventLog
            java.lang.String r3 = "same version. no event"
            r1.logAndAdd(r2, r3)
            goto L_0x0634
        L_0x0613:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r2)
            java.lang.String r4 = ",NEEDRECON"
            r1.append(r4)
            java.lang.String r1 = r1.toString()
            r4 = 318767119(0x1300000f, float:1.61559E-27)
            com.sec.internal.log.IMSLog.c(r4, r1)
            com.sec.internal.helper.SimpleEventLog r1 = r0.mEventLog
            java.lang.String r4 = "no exist complete message. send EVENT_RECONFIGURATION"
            r1.logAndAdd(r2, r4)
            r0.onNewRcsConfigurationAvailable(r2, r3)
        L_0x0634:
            boolean r1 = r0.mPendingAutoConfig
            if (r1 == 0) goto L_0x0922
            r1 = 0
            r0.mPendingAutoConfig = r1
            boolean r3 = r0.mIsRcsEnabled
            if (r3 == 0) goto L_0x0922
            com.sec.internal.ims.config.params.ACSConfig r3 = r0.getAcsConfig(r2)
            r3.resetAcsSettings()
            r3 = 8
            r0.removeMessages(r3)
            r4 = 0
            android.os.Message r3 = r0.obtainMessage(r3, r2, r1, r4)
            r0.sendMessage(r3)
            r3 = 2
            r0.removeMessages(r3)
            android.os.Message r1 = r0.obtainMessage(r3, r2, r1, r4)
            r0.sendMessage(r1)
            goto L_0x0922
        L_0x0660:
            com.sec.internal.ims.config.ConfigTrigger r1 = r0.mConfigTrigger
            r1.setReadyStartForceCmd(r2, r10)
        L_0x0665:
            com.sec.internal.ims.config.ConfigTrigger r1 = r0.mConfigTrigger
            r1.setReadyStartCmdList(r2, r10)
            if (r3 != 0) goto L_0x06b2
            java.lang.String r1 = LOG_TAG
            java.lang.String r3 = "workflow is null"
            com.sec.internal.log.IMSLog.i(r1, r3)
            com.sec.internal.interfaces.ims.core.ISimManager r3 = com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSimSlot(r2)
            if (r3 == 0) goto L_0x06aa
            boolean r4 = r3.hasNoSim()
            if (r4 != 0) goto L_0x06aa
            java.lang.String r4 = r3.getSimMnoName()
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 == 0) goto L_0x0691
            boolean r3 = r3.hasVsim()
            if (r3 != 0) goto L_0x0691
            goto L_0x06aa
        L_0x0691:
            java.lang.String r3 = "try init workflow and start again"
            com.sec.internal.log.IMSLog.i(r1, r3)
            r1 = 0
            r3 = 0
            android.os.Message r4 = r0.obtainMessage(r3, r2, r3, r1)
            r0.sendMessage(r4)
            r4 = 2
            android.os.Message r1 = r0.obtainMessage(r4, r2, r3, r1)
            r0.sendMessage(r1)
            goto L_0x0922
        L_0x06aa:
            java.lang.String r0 = "sim is not ready, start config finished"
            com.sec.internal.log.IMSLog.i(r1, r0)
            goto L_0x0922
        L_0x06b2:
            java.lang.String r1 = LOG_TAG
            java.lang.String r4 = "HANDLE_AUTO_CONFIG_START:"
            com.sec.internal.log.IMSLog.i(r1, r2, r4)
            boolean r4 = r0.isGcEnabledChange(r2)
            if (r4 == 0) goto L_0x06e4
            android.content.Context r4 = r0.mContext
            boolean r4 = com.sec.internal.ims.util.ConfigUtil.getGlobalGcEnabled(r4, r2)
            if (r4 != 0) goto L_0x06d6
            boolean r4 = r15.equals(r8)
            if (r4 == 0) goto L_0x06d6
            java.lang.String r4 = "Change the GC RCS policy, set enableRcs"
            com.sec.internal.log.IMSLog.i(r1, r2, r4)
            r3.setEnableRcsByMigration()
            goto L_0x06e0
        L_0x06d6:
            java.lang.String r4 = "Change the GC RCS policy, clear AutoConfig Storage"
            com.sec.internal.log.IMSLog.i(r1, r2, r4)
            com.sec.internal.constants.ims.DiagnosisConstants$RCSA_TDRE r4 = com.sec.internal.constants.ims.DiagnosisConstants.RCSA_TDRE.GCPOLICY_CHANGE
            r3.clearAutoConfigStorage(r4)
        L_0x06e0:
            r4 = 0
            r0.setIsGcEnabledChange(r4, r2)
        L_0x06e4:
            boolean r4 = r3.checkNetworkConnectivity()
            if (r4 != 0) goto L_0x06f9
            com.sec.internal.ims.config.ConfigTrigger r1 = r0.mConfigTrigger
            com.sec.internal.ims.config.WorkFlowController r4 = r0.mWorkFlowController
            boolean r4 = r4.isSimInfochanged(r2, r12)
            boolean r0 = r0.mMobileNetwork
            r1.tryAutoConfig(r3, r2, r4, r0)
            goto L_0x0922
        L_0x06f9:
            if (r12 == 0) goto L_0x070b
            java.lang.String r4 = "need CurConfig"
            com.sec.internal.log.IMSLog.i(r1, r2, r4)
            r3.startCurConfig()
            com.sec.internal.ims.config.ConfigTrigger r0 = r0.mConfigTrigger
            r4 = 0
            r0.setReadyStartCmdList(r2, r4)
            goto L_0x0922
        L_0x070b:
            r4 = 0
            java.lang.String r5 = "rcs_auto_config_pdn"
            int r5 = com.sec.internal.ims.registry.ImsRegistry.getInt(r2, r5, r4)
            java.lang.String r4 = "ps_only_network"
            if (r5 != r10) goto L_0x0734
            com.sec.internal.helper.PhoneIdKeyMap<java.util.HashMap<java.lang.Integer, android.net.Network>> r5 = r0.mNetworkLists
            java.lang.Object r5 = r5.get(r2)
            java.util.HashMap r5 = (java.util.HashMap) r5
            r7 = 2
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            boolean r5 = r5.containsKey(r7)
            if (r5 == 0) goto L_0x07cb
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r5 = r0.mReadyNetwork
            java.lang.Boolean r7 = java.lang.Boolean.TRUE
            r5.put(r2, r7)
            goto L_0x07cb
        L_0x0734:
            boolean r5 = r16.isMobileDataOn()
            if (r5 == 0) goto L_0x076d
            boolean r5 = r0.isRoamingMobileDataOn(r2)
            if (r5 != 0) goto L_0x0741
            goto L_0x076d
        L_0x0741:
            if (r9 == 0) goto L_0x07cb
            boolean r5 = r9.boolSetting(r4)
            if (r5 == 0) goto L_0x07cb
            boolean r5 = r16.isMobileDataOn()
            if (r5 == 0) goto L_0x07cb
            boolean r5 = r16.isWifiSwitchOn()
            if (r5 == 0) goto L_0x07cb
            java.lang.String r5 = "Mobile Data ON & WIFI ON case for PS only network."
            com.sec.internal.log.IMSLog.i(r1, r2, r5)
            com.sec.internal.ims.config.WorkFlowController r5 = r0.mWorkFlowController
            int r5 = r5.getCurrentRcsConfigVersion(r2)
            java.lang.Integer r7 = java.lang.Integer.valueOf(r2)
            r8 = 3
            android.os.Message r5 = r0.obtainMessage(r8, r5, r5, r7)
            r0.sendMessage(r5)
            goto L_0x07cb
        L_0x076d:
            java.lang.String r5 = "Mobile Data is off or roaming data off in roaming area"
            com.sec.internal.log.IMSLog.i(r1, r2, r5)
            com.sec.internal.ims.config.WorkFlowController r5 = r0.mWorkFlowController
            int r5 = r5.getCurrentRcsConfigVersion(r2)
            java.lang.String r8 = "wifi"
            boolean r7 = r7.contains(r8)
            if (r7 == 0) goto L_0x0837
            boolean r7 = r16.isWifiSwitchOn()
            if (r7 != 0) goto L_0x0789
            goto L_0x0837
        L_0x0789:
            java.lang.String r7 = "Mobile Data is off but WiFi is on"
            com.sec.internal.log.IMSLog.i(r1, r2, r7)
            boolean r7 = com.sec.internal.ims.util.ConfigUtil.isRcsChn(r6)
            if (r7 == 0) goto L_0x0797
            r7 = 0
            r0.mMobileNetwork = r7
        L_0x0797:
            if (r9 == 0) goto L_0x07b1
            boolean r7 = r9.boolSetting(r4)
            if (r7 == 0) goto L_0x07b1
            java.lang.String r7 = "WiFi is on. Register to VOLTE to receive OTP message for PS only network"
            com.sec.internal.log.IMSLog.i(r1, r2, r7)
            java.lang.Integer r7 = java.lang.Integer.valueOf(r2)
            r8 = 3
            android.os.Message r5 = r0.obtainMessage(r8, r5, r5, r7)
            r0.sendMessage(r5)
            goto L_0x07cb
        L_0x07b1:
            r8 = 3
            java.lang.String r7 = "Mobile Data is off but WiFi is on. So wait 20 seconds."
            com.sec.internal.log.IMSLog.i(r1, r2, r7)
            java.lang.Integer r7 = java.lang.Integer.valueOf(r2)
            r0.removeMessages(r8, r7)
            java.lang.Integer r7 = java.lang.Integer.valueOf(r2)
            android.os.Message r5 = r0.obtainMessage(r8, r5, r5, r7)
            r7 = 20000(0x4e20, double:9.8813E-320)
            r0.sendMessageDelayed(r5, r7)
        L_0x07cb:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r7 = "Auto Config Start: ReadyNetwork = "
            r5.append(r7)
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r7 = r0.mReadyNetwork
            java.lang.Object r7 = r7.get(r2)
            r5.append(r7)
            java.lang.String r7 = ", Start command = "
            r5.append(r7)
            com.sec.internal.ims.config.ConfigTrigger r7 = r0.mConfigTrigger
            boolean r7 = r7.getReadyStartCmdList(r2)
            r5.append(r7)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.i(r1, r2, r5)
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r5 = r0.mReadyNetwork
            java.lang.Object r5 = r5.get(r2)
            java.lang.Boolean r5 = (java.lang.Boolean) r5
            boolean r5 = r5.booleanValue()
            if (r5 == 0) goto L_0x0811
            com.sec.internal.ims.config.ConfigTrigger r1 = r0.mConfigTrigger
            com.sec.internal.ims.config.WorkFlowController r4 = r0.mWorkFlowController
            r5 = 0
            boolean r4 = r4.isSimInfochanged(r2, r5)
            boolean r0 = r0.mMobileNetwork
            r1.tryAutoConfig(r3, r2, r4, r0)
            goto L_0x0922
        L_0x0811:
            if (r9 == 0) goto L_0x0819
            boolean r3 = r9.boolSetting(r4)
            if (r3 != 0) goto L_0x0823
        L_0x0819:
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.BELL
            if (r6 != r3) goto L_0x0922
            android.util.Pair r3 = r0.getAvailableNetwork(r2)
            if (r3 != 0) goto L_0x0922
        L_0x0823:
            java.lang.String r3 = "No conditions satisfied to start Auto Config, proceed to VOLTE REG"
            com.sec.internal.log.IMSLog.i(r1, r2, r3)
            java.lang.Integer r1 = java.lang.Integer.valueOf(r2)
            r2 = 0
            r3 = 3
            android.os.Message r1 = r0.obtainMessage(r3, r2, r2, r1)
            r0.sendMessage(r1)
            goto L_0x0922
        L_0x0837:
            r3 = 3
            java.lang.String r4 = "Both Mobile Data and WiFi are off, skip autoconfig"
            com.sec.internal.log.IMSLog.i(r1, r2, r4)
            java.lang.Integer r1 = java.lang.Integer.valueOf(r2)
            android.os.Message r1 = r0.obtainMessage(r3, r5, r5, r1)
            r0.sendMessage(r1)
            goto L_0x0922
        L_0x084a:
            if (r3 != 0) goto L_0x0919
            java.lang.String r1 = "HANDLE_AUTO_CONFIG_INIT:"
            com.sec.internal.log.IMSLog.i(r13, r2, r1)
            boolean r1 = r0.rcsProfileInit(r2)
            if (r1 != 0) goto L_0x085e
            java.lang.String r0 = "SIM is not ready. skip init workflow"
            com.sec.internal.log.IMSLog.i(r13, r2, r0)
            goto L_0x0922
        L_0x085e:
            android.os.HandlerThread r1 = new android.os.HandlerThread
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "workflowThread_"
            r3.append(r4)
            r3.append(r2)
            java.lang.String r3 = r3.toString()
            r1.<init>(r3)
            r1.start()
            android.os.Looper r3 = r1.getLooper()
            android.content.Context r4 = r0.mContext
            com.sec.internal.interfaces.ims.core.IRegistrationManager r5 = r0.mRm
            boolean r5 = com.sec.internal.ims.util.ConfigUtil.hasChatbotService(r2, r5)
            com.sec.internal.interfaces.ims.config.IWorkflow r3 = com.sec.internal.ims.config.CustomizationManager.getConfigManager(r3, r4, r0, r2, r5)
            r0.clearWorkFlowThread(r2)
            android.util.SparseArray<android.os.HandlerThread> r4 = r0.mWorkflowThreadList
            r4.put(r2, r1)
            if (r3 != 0) goto L_0x08ab
            java.lang.String r1 = "workflow is null. skip init workflow, regard old version and new version as 0"
            com.sec.internal.log.IMSLog.i(r13, r2, r1)
            com.sec.internal.ims.config.WorkFlowController r1 = r0.mWorkFlowController
            r1.removeWorkFlow(r2)
            java.lang.Integer r1 = java.lang.Integer.valueOf(r2)
            r2 = 0
            r3 = 3
            android.os.Message r1 = r0.obtainMessage(r3, r2, r2, r1)
            r0.sendMessage(r1)
            goto L_0x0922
        L_0x08ab:
            com.sec.internal.ims.config.WorkFlowController r1 = r0.mWorkFlowController
            r1.initWorkflow(r2, r3)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r2)
            java.lang.String r4 = ",WF:CR"
            r1.append(r4)
            java.lang.String r1 = r1.toString()
            r4 = 318767115(0x1300000b, float:1.6155893E-27)
            com.sec.internal.log.IMSLog.c(r4, r1)
            if (r12 == 0) goto L_0x08e8
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r1 = r0.mSimRefreshReceivedList
            java.lang.Object r1 = r1.get(r2)
            java.lang.Boolean r1 = (java.lang.Boolean) r1
            boolean r1 = r1.booleanValue()
            if (r1 == 0) goto L_0x08e8
            java.lang.String r1 = "clear config info because of sim refresh"
            com.sec.internal.log.IMSLog.i(r13, r2, r1)
            com.sec.internal.constants.ims.DiagnosisConstants$RCSA_TDRE r1 = com.sec.internal.constants.ims.DiagnosisConstants.RCSA_TDRE.SIM_REFRESH
            r3.clearAutoConfigStorage(r1)
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r1 = r0.mSimRefreshReceivedList
            java.lang.Boolean r4 = java.lang.Boolean.FALSE
            r1.put(r2, r4)
        L_0x08e8:
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r1 = r0.mClearTokenNeededList
            java.lang.Object r1 = r1.get(r2)
            java.lang.Boolean r1 = (java.lang.Boolean) r1
            boolean r1 = r1.booleanValue()
            if (r1 == 0) goto L_0x0922
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r1 = r0.mSimRefreshReceivedList
            java.lang.Object r1 = r1.get(r2)
            java.lang.Boolean r1 = (java.lang.Boolean) r1
            boolean r1 = r1.booleanValue()
            if (r1 == 0) goto L_0x0922
            java.lang.String r1 = "clear old token because of sim refresh"
            com.sec.internal.log.IMSLog.i(r13, r2, r1)
            com.sec.internal.constants.ims.DiagnosisConstants$RCSA_TDRE r1 = com.sec.internal.constants.ims.DiagnosisConstants.RCSA_TDRE.SIM_REFRESH
            r3.clearToken(r1)
            r3.removeValidToken()
            com.sec.internal.helper.PhoneIdKeyMap<java.lang.Boolean> r0 = r0.mClearTokenNeededList
            java.lang.Boolean r1 = java.lang.Boolean.FALSE
            r0.put(r2, r1)
            goto L_0x0922
        L_0x0919:
            java.lang.String r0 = "re-init Workflow if needed."
            com.sec.internal.log.IMSLog.i(r13, r2, r0)
            r3.reInitIfNeeded()
        L_0x0922:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.ConfigModule.handleMessage(android.os.Message):void");
    }

    /* access modifiers changed from: protected */
    public void onSimRefresh(int i) {
        IMSLog.i(LOG_TAG, i, "onSimRefresh:");
        this.mConfigTrigger.setReadyStartCmdList(i, false);
        if (getAcsConfig(i) != null) {
            getAcsConfig(i).clear();
        }
        this.mReadyNetwork.put(i, Boolean.FALSE);
        deregisterNetworkCallback(i);
        this.mSimRefreshReceivedList.put(i, Boolean.TRUE);
        this.mWorkFlowController.onSimRefresh(i);
    }

    /* access modifiers changed from: protected */
    public void onTelephonyCallStatusChanged(int i, int i2) {
        this.mCallState = i2;
        String str = LOG_TAG;
        IMSLog.i(str, i, "onTelephonyCallStatusChanged: " + this.mCallState);
        if (this.mCallState == 0) {
            boolean z = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, i) == 1;
            if (this.mPendingAutoComplete) {
                this.mPendingAutoComplete = false;
                Bundle bundle = new Bundle();
                bundle.putInt("lastError", getAcsConfig(i).getAcsLastError());
                sendMessage(obtainMessage(13, bundle));
            } else if (this.mPendingDeregi) {
                this.mPendingDeregi = false;
                List<IRegisterTask> pendingRegistration = this.mRm.getPendingRegistration(i);
                if (pendingRegistration != null) {
                    for (IRegisterTask next : pendingRegistration) {
                        if (next.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED) && next.getPdnType() != 15) {
                            next.setDeregiReason(36);
                            this.mRm.deregister(next, false, true, "MsgApp is changed");
                        } else if (z) {
                            this.mRm.requestTryRegister(next.getPhoneId());
                        }
                    }
                }
            }
        }
    }

    public ACSConfig getAcsConfig(int i) {
        return this.mWorkFlowController.getAcsConfig(i);
    }

    public void setRegisterFromApp(boolean z, int i) {
        this.mConfigTrigger.setRegisterFromApp(z, i);
    }

    public void updateTelephonyCallStatus(int i, int i2) {
        String str = LOG_TAG;
        IMSLog.i(str, i, "updateTelephonyCallStatus: " + i2);
        sendMessage(obtainMessage(14, i, i2, (Object) null));
    }

    public void onNewRcsConfigurationAvailable(int i, int i2) {
        Bundle bundle = new Bundle();
        bundle.putInt("phoneId", i);
        bundle.putInt("lastError", i2);
        sendMessage(obtainMessage(13, bundle));
    }

    public boolean isWaitAutoconfig(IRegisterTask iRegisterTask) {
        return this.mConfigTrigger.isWaitAutoconfig(iRegisterTask);
    }

    public void triggerAutoConfig(boolean z, int i, List<IRegisterTask> list) {
        if (this.mConfigTrigger.triggerAutoConfig(z, i, list)) {
            sendMessageDelayed(obtainMessage(18, i, 0, (Object) null), 1000);
        }
    }

    public Message obtainConfigMessage(int i, Bundle bundle) {
        return obtainMessage(i, bundle);
    }

    public void sendConfigMessage(int i, int i2) {
        sendMessage(obtainMessage(i, i2, 0, (Object) null));
    }

    public void sendConfigMessageDelayed(int i, int i2, int i3) {
        sendMessageDelayed(obtainMessage(i, i2, 0, (Object) null), (long) i3);
    }

    public void sendConfigMessageDelayed(int i, int i2, Object obj, int i3) {
        sendMessageDelayed(obtainMessage(i, i2, 0, obj), (long) i3);
    }

    public void startAutoConfig(boolean z, Message message, int i) {
        if (message == null) {
            Bundle bundle = new Bundle();
            bundle.putInt("phoneId", i);
            message = obtainMessage(13, bundle);
        }
        this.mOnCompleteList.put(i, message);
        this.mConfigTrigger.startAutoConfig(z, message, i);
    }

    public void startAutoConfigDualsim(int i, Message message) {
        this.mOnCompleteList.put(i, message);
        this.mConfigTrigger.startAutoConfigDualsim(i, message);
    }

    public void registerAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener, int i) {
        this.mListener.put(i, iAutoConfigurationListener);
        this.mConfigTrigger.startConfig(5, (Message) null, i);
    }

    public void unregisterAutoConfigurationListener(IAutoConfigurationListener iAutoConfigurationListener, int i) {
        this.mListener.put(i, iAutoConfigurationListener);
        this.mConfigTrigger.startConfig(6, (Message) null, i);
    }

    public void sendVerificationCode(String str, int i) {
        this.mVerificationCode = str;
        this.mConfigTrigger.startConfig(7, (Message) null, i);
    }

    public void sendMsisdnNumber(String str, int i) {
        this.mMsisdnNumber = str;
        this.mConfigTrigger.startConfig(20, (Message) null, i);
    }

    public void sendIidToken(String str, int i) {
        this.mIidToken = str;
        this.mConfigTrigger.startConfig(27, (Message) null, i);
    }

    public void changeOpMode(boolean z, int i, int i2) {
        this.mIsRcsEnabled = z;
        String str = LOG_TAG;
        IMSLog.i(str, i, "changeOpMode: mIsRcsEnabled: " + this.mIsRcsEnabled);
        IMSLog.c(LogClass.CM_OP_MODE, i + ",RCSE:" + this.mIsRcsEnabled);
        Mno simMno = SimUtil.getSimMno(i);
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
        if (rcsStrategy == null || !rcsStrategy.isRemoteConfigNeeded(i)) {
            if (ImsProfile.isRcsUpProfile(getRcsProfile(i)) || ConfigUtil.isRcsChn(simMno)) {
                getAcsConfig(i).resetAcsSettings();
                if (!z) {
                    String acsServerType = ConfigUtil.getAcsServerType(i);
                    IStorageAdapter storage = getStorage(i);
                    if (!ImsConstants.RCS_AS.JIBE.equals(acsServerType) || (isValidConfigDb(i) && storage != null && !TextUtils.isEmpty(storage.read("root/token/token")))) {
                        IMSLog.i(str, i, "force autoconfig for supporting up profile");
                        this.mConfigTrigger.startConfig(8, (Message) null, i);
                        startAutoConfig(true, (Message) null, i);
                        return;
                    }
                    IMSLog.i(str, i, "not to trigger a config because of invalid config");
                    return;
                }
            }
            this.mConfigTrigger.startConfig(8, (Message) null, i);
            IMSLog.i(str, i, "tcPopupUserAccept: " + i2);
            if (i2 == 0 && z) {
                getAcsConfig(i).resetAcsSettings();
                IMSLog.i(str, i, "force autoconfig in case tcPopupUserAccept is zero");
                startAutoConfig(true, (Message) null, i);
                return;
            }
            return;
        }
        IMSLog.i(str, i, "changeOpMode: it is not supported");
    }

    public String getRcsProfile(int i) {
        return this.mWorkFlowController.getRcsProfile(i);
    }

    public String getRcsConfigMark(int i) {
        Mno simMno = SimUtil.getSimMno(i);
        String str = "";
        if (simMno == Mno.DEFAULT) {
            IMSLog.i(LOG_TAG, i, "getRcsConfigMark: no SIM loaded");
            return str;
        }
        List<ImsProfile> profileListWithMnoName = ImsProfileLoaderInternal.getProfileListWithMnoName(this.mContext, simMno.getName(), i);
        if (profileListWithMnoName != null && !profileListWithMnoName.isEmpty()) {
            Iterator<ImsProfile> it = profileListWithMnoName.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ImsProfile next = it.next();
                str = next.getRcsConfigMark();
                if (next.getEnableStatus() == 2 && !TextUtils.isEmpty(str)) {
                    String str2 = LOG_TAG;
                    IMSLog.i(str2, i, "getRcsConfigMark: " + str);
                    break;
                }
            }
        }
        return str;
    }

    public boolean isValidAcsVersion(int i) {
        return this.mConfigTrigger.isValidAcsVersion(i);
    }

    public Integer getRcsConfVersion(int i) {
        return RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId("version", i), (Integer) null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0067 A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:15:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isValidConfigDb(int r10) {
        /*
            r9 = this;
            java.lang.Integer r0 = r9.getRcsConfVersion(r10)
            r1 = 0
            if (r0 == 0) goto L_0x0068
            java.lang.Integer r0 = r9.getRcsConfVersion(r10)
            int r0 = r0.intValue()
            if (r0 >= 0) goto L_0x0012
            goto L_0x0068
        L_0x0012:
            java.util.Date r0 = new java.util.Date
            r0.<init>()
            android.content.Context r9 = r9.mContext
            java.lang.String r2 = "info/next_autoconfig_time"
            java.lang.String r2 = com.sec.internal.ims.util.ImsUtil.getPathWithPhoneId(r2, r10)
            java.lang.String r9 = com.sec.internal.helper.RcsConfigurationHelper.readStringParamWithPath(r9, r2)
            boolean r2 = android.text.TextUtils.isEmpty(r9)
            r3 = 0
            if (r2 != 0) goto L_0x0046
            long r5 = java.lang.Long.parseLong(r9)     // Catch:{ NumberFormatException -> 0x0030 }
            goto L_0x0047
        L_0x0030:
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Invalid next autoconfig time: "
            r5.append(r6)
            r5.append(r9)
            java.lang.String r9 = r5.toString()
            com.sec.internal.log.IMSLog.i(r2, r10, r9)
        L_0x0046:
            r5 = r3
        L_0x0047:
            long r7 = r0.getTime()
            long r5 = r5 - r7
            java.lang.String r9 = LOG_TAG
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "remainingValidity: "
            r0.append(r2)
            r0.append(r5)
            java.lang.String r0 = r0.toString()
            com.sec.internal.log.IMSLog.i(r9, r10, r0)
            int r9 = (r5 > r3 ? 1 : (r5 == r3 ? 0 : -1))
            if (r9 <= 0) goto L_0x0068
            r1 = 1
        L_0x0068:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.ConfigModule.isValidConfigDb(int):boolean");
    }

    /* access modifiers changed from: package-private */
    public boolean rcsProfileInit(int i) {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot == null || simManagerFromSimSlot.hasNoSim()) {
            this.mEventLog.logAndAdd(i, "rcsProfileInit: no SIM loaded");
            IMSLog.c(LogClass.CM_NO_SIM_LOADED, i + ",NOSL");
            return false;
        }
        Integer rcsConfVersion = getRcsConfVersion(simManagerFromSimSlot.getSimSlotIndex());
        String str = LOG_TAG;
        IMSLog.i(str, i, "rcsProfileInit: ConfigDBVer = " + rcsConfVersion);
        if (rcsConfVersion != null) {
            getAcsConfig(simManagerFromSimSlot.getSimSlotIndex()).setAcsVersion(rcsConfVersion.intValue());
        }
        String simMnoName = simManagerFromSimSlot.getSimMnoName();
        if (!TextUtils.isEmpty(simMnoName) || simManagerFromSimSlot.hasVsim()) {
            String rcsProfileLoaderInternalWithFeature = ConfigUtil.getRcsProfileLoaderInternalWithFeature(this.mContext, simMnoName, i);
            this.mWorkFlowController.putRcsProfile(i, rcsProfileLoaderInternalWithFeature);
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd(i, "Autoconfig init: mnoName = " + simMnoName + ", rcsProfile = " + rcsProfileLoaderInternalWithFeature);
            IMSLog.c(LogClass.CM_RCS_PROFILE, i + "," + simMnoName + "," + rcsProfileLoaderInternalWithFeature);
            return true;
        }
        this.mEventLog.logAndAdd(i, "rcsProfileInit: mnoName is not valid");
        IMSLog.c(LogClass.CM_INVALID_MNONAME, i + ",INVMNO");
        return false;
    }

    public boolean updateMobileNetworkforDualRcs(int i) {
        if (RcsUtils.DualRcs.isDualRcsReg() && SimUtil.getActiveDataPhoneId() != i) {
            IMSLog.i(LOG_TAG, i, "tryAutoConfig: getActiveDataPhoneId() != phoneId ->mobileNetwork = false");
            this.mMobileNetwork = false;
        }
        return this.mMobileNetwork;
    }

    /* access modifiers changed from: package-private */
    public void init(int i) {
        sendMessage(obtainMessage(0, i, 0, (Object) null));
    }

    /* access modifiers changed from: package-private */
    public void onSimReady(int i, boolean z) {
        int i2 = i;
        boolean z2 = z;
        String str = LOG_TAG;
        IMSLog.i(str, i2, "onSimReady:");
        deregisterNetworkCallback(i);
        registerNetworkCallback(i);
        boolean isSimInfochanged = this.mWorkFlowController.isSimInfochanged(i2, z2);
        boolean isValidAcsVersion = isValidAcsVersion(i);
        IWorkflow workflow = this.mWorkFlowController.getWorkflow(i2);
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        Mno simMno = SimUtil.getSimMno(i);
        int i3 = ImsRegistry.getInt(i2, GlobalSettingsConstants.RCS.RCS_DEFAULT_ENABLED, -1);
        IMSLog.i(str, i2, "isRcsAvailable: " + isValidAcsVersion + " isChanged: " + isSimInfochanged + " isRemoteConfigNeeded: " + z2 + " isSimRefreshReceived: " + this.mSimRefreshReceivedList.get(i2) + " rcsDefaultEnabled: " + i3);
        StringBuilder sb = new StringBuilder();
        sb.append(i2);
        sb.append(",RCSE:");
        sb.append(isValidAcsVersion);
        sb.append(",SIM:");
        sb.append(isSimInfochanged);
        IMSLog.c(LogClass.CM_SIM_READY, sb.toString());
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i2, "isRcsEnabled: " + isValidAcsVersion + " isChanged: " + isSimInfochanged);
        String nWCode = OmcCode.getNWCode(i);
        String string = ImsSharedPrefHelper.getString(i2, this.mContext, IConfigModule.PREF_OMCNW_CODE, IConfigModule.KEY_OMCNW_CODE, "");
        boolean equalsIgnoreCase = nWCode.equalsIgnoreCase(string) ^ true;
        ImsSharedPrefHelper.save(i2, this.mContext, IConfigModule.PREF_OMCNW_CODE, IConfigModule.KEY_OMCNW_CODE, nWCode);
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd(i2, "onSimReady: OMCNW_CODE: " + string + " => " + nWCode);
        boolean equals = ImsConstants.RCS_AS.JIBE.equals(ConfigUtil.getAcsServerType(i));
        if (workflow != null || !isValidAcsVersion || ((!z2 && (!isSimInfochanged || !equals)) || (!this.mSimRefreshReceivedList.get(i2).booleanValue() && !equalsIgnoreCase))) {
            if (isSimInfochanged) {
                setAcsTryReason(i2, DiagnosisConstants.RCSA_ATRE.SIM_SWAP);
                if (simMno.isKor()) {
                    IMSLog.i(str, i2, "changed sim info, reset to MSISDN_FROM_PAU");
                    resetMsisdnFromPau(i);
                }
            }
            this.mSimRefreshReceivedList.put(i2, Boolean.FALSE);
            if (workflow == null) {
                if ((simMno.isKor() || simMno.isEur() || simMno.isChn()) && simManagerFromSimSlot != null && !simManagerFromSimSlot.hasNoSim()) {
                    IMSLog.i(str, i2, "init workflow");
                    IMSLog.c(LogClass.CM_INIT_WORKFLOW, i2 + ",WF:INIT");
                    sendMessage(obtainMessage(0, i2, 0, (Object) null));
                }
                if (RcsUtils.DualRcs.isDualRcsReg()) {
                    updateDualRcsNetwork(i);
                }
            } else if (isSimInfochanged && isValidAcsVersion) {
                IMSLog.i(str, i2, "reinit workflow");
                IMSLog.c(LogClass.CM_REINIT_WORKFLOW, i2 + ",WF:REINIT");
                if (equals) {
                    workflow.clearAutoConfigStorage(DiagnosisConstants.RCSA_TDRE.SIM_CHANGED);
                    IMSLog.i(str, i2, "setting for starting auto config by Message app is clear");
                    ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, -1, i2);
                } else if (simMno.isOneOf(Mno.TMOUS, Mno.DISH)) {
                    getAcsConfig(i).setAcsCompleteStatus(false);
                } else if (z2) {
                    IMSLog.i(str, i2, "sim info is changed and reset acsSettings");
                    IMSLog.c(LogClass.CM_SIMINFO_CHANGED, i2 + ",SIMINFO:CHA,RACS");
                    ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, i3, i2);
                    getAcsConfig(i).resetAcsSettings();
                    this.mConfigTrigger.setReadyStartForceCmd(i2, true);
                    this.mSimRefreshReceivedList.put(i2, Boolean.TRUE);
                }
                workflow.cleanup();
                this.mConfigTrigger.setReadyStartCmdList(i2, false);
                this.mWorkFlowController.removeWorkFlow(i2);
                clearWorkFlowThread(i);
                IMSLog.i(str, i2, "clear WorkFlow/WorkFlowThread and send init msg");
                sendMessage(obtainMessage(0, i2, 0, (Object) null));
            }
        } else {
            IMSLog.i(str, i2, "sim info is refreshed and reset acsSettings");
            IMSLog.c(LogClass.CM_SIMINFO_REFRESHED, i2 + ",SIMINFO:REF,RACS");
            this.mEventLog.logAndAdd(i2, "SIM info is refreshed or OMCNW_CODE changed. Reset ACS settings");
            if (!equals) {
                ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, i3, i2);
            } else {
                this.mClearTokenNeededList.put(i2, Boolean.TRUE);
            }
            setAcsTryReason(i2, DiagnosisConstants.RCSA_ATRE.SIM_SWAP);
            getAcsConfig(i).resetAcsSettings();
            this.mConfigTrigger.setReadyStartForceCmd(i2, true);
            this.mSimRefreshReceivedList.put(i2, Boolean.TRUE);
            clearWorkFlowThread(i);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateDualRcsNetwork(int i) {
        Network availableNetworkForNetworkType = getAvailableNetworkForNetworkType(i == 0 ? 1 : 0, 1);
        if (availableNetworkForNetworkType != null && SimUtil.getActiveDataPhoneId() != i) {
            sendMessage(obtainMessage(24, i, 1, availableNetworkForNetworkType));
            IMSLog.d(LOG_TAG, i, "updateDualRcsNetwork : ");
        }
    }

    /* access modifiers changed from: package-private */
    public void clearWorkFlow(int i) {
        IWorkflow workflow = this.mWorkFlowController.getWorkflow(i);
        HandlerThread handlerThread = this.mWorkflowThreadList.get(i);
        if (workflow != null && handlerThread != null && workflow.isConfigOngoing()) {
            String str = LOG_TAG;
            IMSLog.i(str, i, "clearWorkFlow started");
            workflow.stopWorkFlow();
            handlerThread.interrupt();
            this.mConfigTrigger.setReadyStartCmdList(i, true);
            IMSLog.i(str, i, "clearWorkFlow done");
        }
    }

    /* access modifiers changed from: package-private */
    public void clearWorkFlowThread(int i) {
        HandlerThread handlerThread = this.mWorkflowThreadList.get(i);
        if (handlerThread == null) {
            IMSLog.i(LOG_TAG, i, "clearWorkFlowThread: workflowThread is null");
            return;
        }
        IMSLog.i(LOG_TAG, i, "clearWorkFlowThread: started");
        handlerThread.interrupt();
        try {
            handlerThread.join(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            this.mWorkflowThreadList.remove(i);
            throw th;
        }
        this.mWorkflowThreadList.remove(i);
        IMSLog.i(LOG_TAG, i, "clearWorkFlowThread: done");
    }

    /* access modifiers changed from: package-private */
    public boolean isMobileDataOn() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), Extensions.Settings.Global.MOBILE_DATA, 1) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isRoamingMobileDataOn(int i) {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        boolean z = false;
        if (simManagerFromSimSlot == null) {
            return false;
        }
        int subscriptionId = simManagerFromSimSlot.getSubscriptionId();
        if (!TelephonyManagerWrapper.getInstance(this.mContext).isNetworkRoaming(subscriptionId)) {
            IMSLog.i(LOG_TAG, i, "is in Home Network");
            return true;
        }
        ImsConstants.SystemSettings.SettingsItem settingsItem = ImsConstants.SystemSettings.DATA_ROAMING;
        if (settingsItem.getbySubId(this.mContext, ImsConstants.SystemSettings.DATA_ROAMING_UNKNOWN, subscriptionId) == ImsConstants.SystemSettings.ROAMING_DATA_ENABLED || settingsItem.get(this.mContext, ImsConstants.SystemSettings.DATA_ROAMING_UNKNOWN) == ImsConstants.SystemSettings.ROAMING_DATA_ENABLED) {
            z = true;
        }
        IMSLog.i(LOG_TAG, i, "Roaming && isDataRoamingOn = " + z);
        return z;
    }

    private boolean isWifiSwitchOn() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    public void setAcsTryReason(int i, DiagnosisConstants.RCSA_ATRE rcsa_atre) {
        this.mConfigTrigger.setAcsTryReason(i, rcsa_atre);
    }

    public DiagnosisConstants.RCSA_ATRE getAcsTryReason(int i) {
        return this.mConfigTrigger.getAcsTryReason(i);
    }

    public void resetAcsTryReason(int i) {
        this.mConfigTrigger.resetAcsTryReason(i);
    }

    public void setTokenDeletedReason(int i, DiagnosisConstants.RCSA_TDRE rcsa_tdre) {
        this.mConfigTrigger.setTokenDeletedReason(i, rcsa_tdre);
    }

    public DiagnosisConstants.RCSA_TDRE getTokenDeletedReason(int i) {
        return this.mConfigTrigger.getTokenDeletedReason(i);
    }

    public void resetTokenDeletedReason(int i) {
        this.mConfigTrigger.resetTokenDeletedReason(i);
    }

    public void onNewRcsConfigurationNeeded(String str, String str2, Message message) throws NullPointerException {
        Preconditions.checkNotNull(str);
        Preconditions.checkNotNull(str2);
        Preconditions.checkNotNull(message);
        IRegistrationManager registrationManager = ImsRegistry.getRegistrationManager();
        IUserAgent userAgentByImsi = registrationManager != null ? registrationManager.getUserAgentByImsi(str2, str) : null;
        if (userAgentByImsi != null) {
            startAcs(userAgentByImsi.getPhoneId());
            message.arg1 = 1;
        } else {
            message.arg1 = 0;
        }
        message.sendToTarget();
    }

    public void startAcs(int i) {
        sendMessage(obtainMessage(15, i, -1));
    }

    /* access modifiers changed from: package-private */
    public void startAcsWithDelay(int i) {
        int i2;
        Integer rcsConfVersion = getRcsConfVersion(i);
        if (rcsConfVersion != null && rcsConfVersion.intValue() == 0 && (i2 = this.mRetryCount) > 0) {
            this.mRetryCount = i2 - 1;
            IMSLog.i(LOG_TAG, i, "SSL Handshake failed. delay 5 minutes");
            sendMessageDelayed(obtainMessage(15, i, -1), 300000);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateMsisdn(ImsRegistration imsRegistration) {
        if (SimUtil.getSimMno(imsRegistration.getPhoneId()).isKor() && TextUtils.isEmpty(TelephonyManagerWrapper.getInstance(this.mContext).getMsisdn(imsRegistration.getSubscriptionId())) && imsRegistration.hasVolteService() && !imsRegistration.getImsProfile().hasEmergencySupport() && imsRegistration.getImsProfile().getCmcType() == 0) {
            IMSLog.i(LOG_TAG, imsRegistration.getPhoneId(), "MSISDN is null, SP needs to be set to PAU");
            setMsisdnFromPau(imsRegistration);
        }
    }

    public void onRegistrationStatusChanged(boolean z, int i, ImsRegistration imsRegistration) {
        int phoneId = imsRegistration.getPhoneId();
        if (z) {
            updateMsisdn(imsRegistration);
        }
        ImsProfile imsProfile = imsRegistration.getImsProfile();
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "onRegistrationStatusChanged: [" + imsProfile.getName() + "] registered[" + z + "], response [" + i + "], 403Forbidden Count [" + this.m403ForbiddenCounter + "]");
        StringBuilder sb = new StringBuilder();
        sb.append(phoneId);
        sb.append(",EC:");
        sb.append(i);
        sb.append(",CNT:");
        sb.append(this.m403ForbiddenCounter);
        IMSLog.c(LogClass.CM_REGI_ERROR, sb.toString());
        Mno fromName = Mno.fromName(imsProfile.getMnoName());
        if (z) {
            if (fromName.isKor() && imsRegistration.hasVolteService()) {
                IMSLog.i(str, phoneId, "VoLTE regi. is done. It's time for RCS registration!");
                List<IRegisterTask> pendingRegistration = this.mRm.getPendingRegistration(phoneId);
                if (pendingRegistration != null) {
                    for (IRegisterTask tryAutoconfiguration : pendingRegistration) {
                        tryAutoconfiguration(tryAutoconfiguration);
                    }
                }
            }
            if (imsRegistration.hasRcsService()) {
                this.m403ForbiddenCounter = 0;
            }
        } else if (!ImsConstants.RCS_AS.JIBE.equals(ConfigUtil.getAcsServerType(phoneId)) || !ConfigUtil.isRcsOnly(imsProfile)) {
            if (ConfigUtil.isRcsEur(fromName) && i == SipErrorBase.UNAUTHORIZED.getCode()) {
                this.mWorkFlowController.deleteConfiguration(imsRegistration.getPhoneId(), DiagnosisConstants.RCSA_TDRE.SIPERROR_UNAUTHORIZED);
            }
        } else if (i == SipErrorBase.FORBIDDEN.getCode()) {
            int i2 = this.m403ForbiddenCounter + 1;
            this.m403ForbiddenCounter = i2;
            if (i2 >= 2) {
                IMSLog.i(str, phoneId, "Two consecutive 403 errors. Permanently prohibited.");
                this.m403ForbiddenCounter = 0;
                return;
            }
            IMSLog.i(str, phoneId, "403 error. Restart ACS");
            startAcs(phoneId);
        }
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(str);
        IMSLog.dump(str, "Autoconfig History:");
        this.mEventLog.dump();
        if (IMSLog.isShipBuild()) {
            IMSLog.dump(str, "Dump of ACS Encr : ");
            dumpEncrAcsDb(Uri.parse(ConfigConstants.CONTENT_URI + "*"));
        } else if (this.mContext == null) {
            IMSLog.dump(AUTOCONF_TAG, "Dump of configuration db: mContext is null!", true);
        } else if (SimUtil.isMultiSimSupported()) {
            IMSLog.dump(AUTOCONF_TAG, "Dump of configuration db for simslot0:", true);
            StringBuilder sb = new StringBuilder();
            Uri uri = ConfigConstants.CONTENT_URI;
            sb.append(uri);
            sb.append("*#simslot0");
            dumpAutoConfDb(Uri.parse(sb.toString()));
            IMSLog.dump(AUTOCONF_TAG, "Dump of configuration db for simslot1:", true);
            dumpAutoConfDb(Uri.parse(uri + "*#simslot1"));
        } else {
            IMSLog.dump(AUTOCONF_TAG, "Dump of configuration db:", true);
            dumpAutoConfDb(Uri.parse(ConfigConstants.CONTENT_URI + "*"));
        }
        IMSLog.decreaseIndent(str);
        this.mWorkFlowController.dump();
    }

    private void dumpEncrAcsDb(Uri uri) {
        Cursor query;
        try {
            query = this.mContext.getContentResolver().query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
            StringBuilder sb = new StringBuilder();
            if (query != null) {
                query.moveToFirst();
                for (int i = 0; i < query.getColumnCount(); i++) {
                    if (Arrays.stream(query.getColumnName(i).split("/")).anyMatch(new ConfigModule$$ExternalSyntheticLambda0(this))) {
                        sb.append(" ");
                        sb.append(query.getColumnName(i));
                        sb.append(": ");
                        sb.append(query.getString(i));
                        sb.append("\n");
                    }
                    if (query.getColumnCount() < 1) {
                        query.close();
                        return;
                    }
                }
            }
            if (sb.length() > 0) {
                IMSLog.dumpEncryptedACS(AUTOCONF_TAG, sb.toString());
            }
            if (query != null) {
                query.close();
                return;
            }
            return;
        } catch (SQLiteException | SecurityException unused) {
            IMSLog.dump(AUTOCONF_TAG, "  Skip dump encr auto conf db", true);
            return;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ boolean lambda$dumpEncrAcsDb$0(String str) {
        return this.mAcsEncrNeededParams.contains(str);
    }

    private void dumpAutoConfDb(Uri uri) {
        Cursor query;
        try {
            query = this.mContext.getContentResolver().query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
            if (query != null) {
                query.moveToFirst();
                for (int i = 0; i < query.getColumnCount(); i++) {
                    IMSLog.dump(AUTOCONF_TAG, "  " + query.getColumnName(i) + ": " + query.getString(i), true);
                }
                if (query.getColumnCount() < 1) {
                    IMSLog.dump(AUTOCONF_TAG, "  DB is empty", true);
                }
            } else {
                IMSLog.dump(AUTOCONF_TAG, "  DB is not available", true);
            }
            if (query != null) {
                query.close();
                return;
            }
            return;
        } catch (SQLiteException | SecurityException unused) {
            IMSLog.dump(AUTOCONF_TAG, "  Skip dump auto conf db", true);
            return;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    /* access modifiers changed from: package-private */
    public boolean checkMsisdnSkipCount(int i, boolean z) {
        Mno simMno = SimUtil.getSimMno(i);
        String acsServerType = ConfigUtil.getAcsServerType(i);
        if (z || simMno == Mno.SPRINT || ImsConstants.RCS_AS.JIBE.equals(acsServerType)) {
            IMSLog.i(LOG_TAG, i, "no need to check MsisdnSkipCount");
            return false;
        }
        int msisdnSkipCount = this.mWorkFlowController.getMsisdnSkipCount(i);
        String str = LOG_TAG;
        IMSLog.i(str, i, "MsisdnSkipCount : " + msisdnSkipCount + ", MobileNetwork : " + this.mMobileNetwork);
        if (msisdnSkipCount != 3 || !this.mMobileNetwork) {
            return false;
        }
        return true;
    }

    public void showMSIDSNDialog() {
        sendEmptyMessage(16);
    }

    public void notifyDefaultSmsChanged(int i) {
        IMSLog.i(LOG_TAG, i, "notifyDefaultSmsChanged:");
        List<IRegisterTask> pendingRegistration = this.mRm.getPendingRegistration(i);
        if (pendingRegistration != null) {
            processChatPolicyforSMSAppChange(checkChatPolicyforSMSAppChange(i), i, pendingRegistration);
        }
    }

    private int checkChatPolicyforSMSAppChange(int i) {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        String acsServerType = ConfigUtil.getAcsServerType(i);
        setAcsTryReason(i, DiagnosisConstants.RCSA_ATRE.CHANGE_MSG_APP);
        int i2 = 0;
        int i3 = ImsRegistry.getInt(i, GlobalSettingsConstants.RCS.SUPPORT_CHAT_ON_DEFAULT_MMS_APP, 0);
        boolean z = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, i) == -1;
        if (simManagerFromSimSlot == null || !simManagerFromSimSlot.isSimAvailable() || !ImsConstants.RCS_AS.JIBE.equals(acsServerType) || !ConfigUtil.isRcsEur(i) || !z) {
            i2 = i3;
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i, "notifyDefaultSmsChanged - SupportChat Type : " + i2);
        return i2;
    }

    /* access modifiers changed from: package-private */
    public void processChatPolicyforSMSAppChange(int i, int i2, List<IRegisterTask> list) {
        boolean z = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, i2) == 1;
        IMSLog.c(LogClass.CM_DEFAULT_SMS_CHANGED, i2 + "," + z + "," + i);
        if (i == 1) {
            this.mRm.cancelUpdateSipDelegateRegistration(i2);
            this.mRm.updateChatService(i2, 2);
        } else if (i == 2) {
            for (IRegisterTask next : list) {
                if (next.isRcsOnly()) {
                    if (z) {
                        IMSLog.i(LOG_TAG, i2, "notifyDefaultSmsChanged - setStateforACS");
                        getAcsConfig(i2).resetAcsSettings();
                        triggerAutoConfig(false, i2, list);
                    } else if (next.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                        next.setDeregiReason(36);
                        this.mRm.deregister(next, false, true, "MsgApp is changed");
                    }
                }
            }
        } else if (i != 3) {
            if (i == 4) {
                removeMessages(15);
                startAcsWithDelay(i2);
            } else if (i == 5) {
                for (IRegisterTask profile : list) {
                    if (profile.getProfile().getNeedAutoconfig()) {
                        getAcsConfig(i2).resetAcsSettings();
                        triggerAutoConfig(false, i2, list);
                    }
                }
            }
        } else if (this.mCallState != 0) {
            this.mPendingDeregi = true;
            IMSLog.i(LOG_TAG, i2, "Pending deregistration on active call when MsgApp is changed");
        } else {
            for (IRegisterTask next2 : list) {
                if (next2.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED) && next2.getPdnType() != 15) {
                    this.mRm.cancelUpdateSipDelegateRegistration(i2);
                    next2.setDeregiReason(36);
                    this.mRm.deregister(next2, false, true, "MsgApp is changed");
                } else if (z) {
                    this.mRm.requestTryRegister(next2.getPhoneId());
                }
            }
        }
    }

    public void setDualSimRcsAutoConfig(boolean z) {
        this.mConfigTrigger.setDualSimRcsAutoConfig(z);
    }

    public boolean tryAutoconfiguration(IRegisterTask iRegisterTask) {
        return this.mConfigTrigger.tryAutoconfiguration(iRegisterTask);
    }

    public boolean isRcsEnabled(int i) {
        return DmConfigHelper.isImsSwitchEnabled(this.mContext, DeviceConfigManager.RCS, i);
    }

    public boolean isConfigModuleBootUp() {
        return this.mIsConfigModuleBootUp;
    }

    public boolean isMessagingReady() {
        return this.mIsMessagingReady;
    }

    public void onDefaultSmsPackageChanged() {
        Log.i(LOG_TAG, "onDefaultSmsPackageChanged");
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            sendMessage(obtainMessage(18, i, 0, (Object) null));
        }
    }

    class IntentReceiver extends BroadcastReceiver {
        private static final String ACTION_AIRPLANE_MODE = "android.intent.action.AIRPLANE_MODE";
        private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
        private static final String IMS_SERVICE_UP_RESPONSE = "com.samsung.android.messaging.IMS_SERVICE_UP_RESPONSE";
        private IntentFilter mIntentFilter;

        public IntentReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            this.mIntentFilter = intentFilter;
            intentFilter.addAction(ACTION_BOOT_COMPLETED);
            this.mIntentFilter.addAction(IMS_SERVICE_UP_RESPONSE);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {
                    ConfigModule.this.sendEmptyMessage(23);
                } else if (intent.getAction().equals(IMS_SERVICE_UP_RESPONSE)) {
                    IMSLog.i(ConfigModule.LOG_TAG, "onReceive: IMS_SERVICE_UP_RESPONSE");
                    ConfigModule.this.mIsMessagingReady = true;
                } else if (intent.getAction().equals("android.intent.action.AIRPLANE_MODE")) {
                    for (ISimManager iSimManager : SimManagerFactory.getAllSimManagers()) {
                        if (iSimManager != null && iSimManager.getSimMno() == Mno.KT) {
                            if (ImsConstants.SystemSettings.AIRPLANE_MODE.get(ConfigModule.this.mContext, 0) != ImsConstants.SystemSettings.AIRPLANE_MODE_ON) {
                                ConfigModule.this.mNeedRetryOverWifi = false;
                            } else if (ConfigModule.this.mWorkFlowController.getCurrentRcsConfigVersion(iSimManager.getSimSlotIndex()) > 0) {
                                ConfigModule.this.mNeedRetryOverWifi = true;
                            }
                        }
                    }
                }
            }
        }

        public void addActionAirplaneMode() {
            this.mIntentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        }

        public IntentFilter getIntentFilter() {
            return this.mIntentFilter;
        }
    }

    public void setMsisdnFromPau(ImsRegistration imsRegistration) {
        String ownNumber = imsRegistration.getOwnNumber();
        if (ownNumber != null) {
            if (ownNumber.startsWith("0")) {
                ownNumber = "+82" + ownNumber.substring(1);
            }
            String str = "IMSI_" + SimManagerFactory.getImsiFromPhoneId(imsRegistration.getPhoneId());
            if (!ownNumber.equals(ImsSharedPrefHelper.getString(imsRegistration.getPhoneId(), this.mContext, IConfigModule.MSISDN_FROM_PAU, str, ""))) {
                this.mEventLog.logAndAdd(imsRegistration.getPhoneId(), "setMsisdnFromPau: " + IMSLog.checker(ownNumber));
                IMSLog.c(LogClass.CM_SET_SP_PAU, imsRegistration.getPhoneId() + "SET_SP_PAU");
                ImsSharedPrefHelper.save(imsRegistration.getPhoneId(), this.mContext, IConfigModule.MSISDN_FROM_PAU, str, ownNumber);
            }
            this.mRm.requestTryRegister(imsRegistration.getPhoneId());
        }
    }

    private void resetMsisdnFromPau(int i) {
        IMSLog.c(LogClass.CM_RES_SP_PAU, i + "RES_SP_PAU");
        this.mEventLog.logAndAdd(i, "reset to MSISDN_FROM_PAU");
        ImsSharedPrefHelper.save(i, this.mContext, IConfigModule.MSISDN_FROM_PAU, "IMSI_" + SimManagerFactory.getImsiFromPhoneId(i), "");
    }

    public void resetReadyStateCommand(int i) {
        this.mConfigTrigger.setReadyStartCmdList(i, true);
    }

    /* access modifiers changed from: package-private */
    public void createNetworkListener(final int i, final int i2) {
        String str = LOG_TAG;
        IMSLog.i(str, i, "createNetworkListener: " + i2);
        this.mNetworkListeners.get(i).put(Integer.valueOf(i2), new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                String r0 = ConfigModule.LOG_TAG;
                int i = i;
                IMSLog.i(r0, i, "onAvailable : " + network + " networkType: " + i2);
                if (!RcsUtils.DualRcs.isDualRcsReg() || i2 != 1) {
                    ConfigModule configModule = ConfigModule.this;
                    configModule.sendMessage(configModule.obtainMessage(24, i, i2, network));
                    return;
                }
                for (int i2 = 0; i2 < SimUtil.getPhoneCount(); i2++) {
                    ConfigModule configModule2 = ConfigModule.this;
                    configModule2.sendMessage(configModule2.obtainMessage(24, i2, i2, network));
                }
            }

            public void onLost(Network network) {
                String r0 = ConfigModule.LOG_TAG;
                int i = i;
                IMSLog.i(r0, i, "onLost : " + network + " networkType: " + i2);
                if (!RcsUtils.DualRcs.isDualRcsReg() || i2 != 1) {
                    ConfigModule configModule = ConfigModule.this;
                    configModule.sendMessage(configModule.obtainMessage(25, i, i2));
                    return;
                }
                for (int i2 = 0; i2 < SimUtil.getPhoneCount(); i2++) {
                    ConfigModule configModule2 = ConfigModule.this;
                    configModule2.sendMessage(configModule2.obtainMessage(25, i2, i2));
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void registerNetworkCallback(int r13) {
        /*
            r12 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "registerNetworkCallback"
            com.sec.internal.log.IMSLog.i(r0, r13, r1)
            java.lang.String r0 = com.sec.internal.ims.util.ConfigUtil.getNetworkType(r13)
            android.content.Context r1 = r12.mContext
            java.lang.String r2 = "connectivity"
            java.lang.Object r1 = r1.getSystemService(r2)
            android.net.ConnectivityManager r1 = (android.net.ConnectivityManager) r1
            int r2 = com.sec.internal.helper.SimUtil.getSubId(r13)
            r3 = -1
            if (r2 != r3) goto L_0x001e
            return
        L_0x001e:
            java.lang.String r4 = ","
            java.lang.String[] r0 = r0.split(r4)
            int r4 = r0.length
            r5 = 0
            r6 = r5
        L_0x0027:
            if (r6 >= r4) goto L_0x00cd
            r7 = r0[r6]
            boolean r8 = android.text.TextUtils.isEmpty(r7)
            r9 = 1
            if (r8 == 0) goto L_0x0037
            r12.registerNetworkCallbackForNetwork(r13, r9)
            goto L_0x00c9
        L_0x0037:
            android.net.NetworkRequest$Builder r8 = new android.net.NetworkRequest$Builder
            r8.<init>()
            r7.hashCode()
            int r10 = r7.hashCode()
            r11 = 2
            switch(r10) {
                case 104399: goto L_0x0060;
                case 3649301: goto L_0x0054;
                case 570410817: goto L_0x0049;
                default: goto L_0x0047;
            }
        L_0x0047:
            r7 = r3
            goto L_0x006a
        L_0x0049:
            java.lang.String r10 = "internet"
            boolean r7 = r7.equals(r10)
            if (r7 != 0) goto L_0x0052
            goto L_0x0047
        L_0x0052:
            r7 = r11
            goto L_0x006a
        L_0x0054:
            java.lang.String r10 = "wifi"
            boolean r7 = r7.equals(r10)
            if (r7 != 0) goto L_0x005e
            goto L_0x0047
        L_0x005e:
            r7 = r9
            goto L_0x006a
        L_0x0060:
            java.lang.String r10 = "ims"
            boolean r7 = r7.equals(r10)
            if (r7 != 0) goto L_0x0069
            goto L_0x0047
        L_0x0069:
            r7 = r5
        L_0x006a:
            r10 = 12
            switch(r7) {
                case 0: goto L_0x008a;
                case 1: goto L_0x0081;
                case 2: goto L_0x0071;
                default: goto L_0x006f;
            }
        L_0x006f:
            r9 = r5
            goto L_0x009b
        L_0x0071:
            android.net.NetworkRequest$Builder r7 = r8.addTransportType(r5)
            android.net.NetworkRequest$Builder r7 = r7.addCapability(r10)
            java.lang.String r10 = java.lang.Integer.toString(r2)
            r7.setNetworkSpecifier(r10)
            goto L_0x009b
        L_0x0081:
            android.net.NetworkRequest$Builder r7 = r8.addTransportType(r9)
            r7.addCapability(r10)
            r9 = 3
            goto L_0x009b
        L_0x008a:
            android.net.NetworkRequest$Builder r7 = r8.addTransportType(r5)
            r9 = 4
            android.net.NetworkRequest$Builder r7 = r7.addCapability(r9)
            java.lang.String r9 = java.lang.Integer.toString(r2)
            r7.setNetworkSpecifier(r9)
            r9 = r11
        L_0x009b:
            com.sec.internal.helper.PhoneIdKeyMap<java.util.HashMap<java.lang.Integer, android.net.ConnectivityManager$NetworkCallback>> r7 = r12.mNetworkListeners
            java.lang.Object r7 = r7.get(r13)
            java.util.HashMap r7 = (java.util.HashMap) r7
            java.lang.Integer r10 = java.lang.Integer.valueOf(r9)
            boolean r7 = r7.containsKey(r10)
            if (r7 != 0) goto L_0x00c9
            r12.createNetworkListener(r13, r9)
            android.net.NetworkRequest r7 = r8.build()
            com.sec.internal.helper.PhoneIdKeyMap<java.util.HashMap<java.lang.Integer, android.net.ConnectivityManager$NetworkCallback>> r8 = r12.mNetworkListeners
            java.lang.Object r8 = r8.get(r13)
            java.util.HashMap r8 = (java.util.HashMap) r8
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)
            java.lang.Object r8 = r8.get(r9)
            android.net.ConnectivityManager$NetworkCallback r8 = (android.net.ConnectivityManager.NetworkCallback) r8
            r1.registerNetworkCallback(r7, r8)
        L_0x00c9:
            int r6 = r6 + 1
            goto L_0x0027
        L_0x00cd:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.ConfigModule.registerNetworkCallback(int):void");
    }

    /* access modifiers changed from: package-private */
    public void registerNetworkCallbackForNetwork(int i, int i2) {
        int i3 = 1;
        if (SimUtil.getPhoneCount() > 1) {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            if (i != 0) {
                i3 = 0;
            }
            if (SimUtil.getSubId(i3) != -1) {
                int subId = SimUtil.getSubId(i);
                if (RcsUtils.DualRcs.dualRcsPolicyCase(this.mContext, i3) && !this.mNetworkListeners.get(i).containsKey(Integer.valueOf(i2))) {
                    NetworkRequest.Builder builder = new NetworkRequest.Builder();
                    builder.addTransportType(0).addCapability(12).setNetworkSpecifier(Integer.toString(subId));
                    createNetworkListener(i, i2);
                    connectivityManager.registerNetworkCallback(builder.build(), (ConnectivityManager.NetworkCallback) this.mNetworkListeners.get(i).get(Integer.valueOf(i2)));
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void deregisterNetworkCallback(int i) {
        IMSLog.i(LOG_TAG, i, "deregisterNetworkCallback");
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        for (ConnectivityManager.NetworkCallback unregisterNetworkCallback : this.mNetworkListeners.get(i).values()) {
            connectivityManager.unregisterNetworkCallback(unregisterNetworkCallback);
        }
        this.mNetworkListeners.get(i).clear();
        this.mNetworkLists.get(i).clear();
    }

    /* access modifiers changed from: package-private */
    public void processConnectionChange(int i, boolean z) {
        if (getAvailableNetwork(i) == null) {
            IMSLog.i(LOG_TAG, i, "No Available network");
        } else if (this.mConfigTrigger.getReadyStartCmdListIndexOfKey(i) >= 0) {
            if (this.mConfigTrigger.getReadyStartCmdList(i) || checkMsisdnSkipCount(i, z) || this.mNeedRetryOverNetwork) {
                String str = LOG_TAG;
                IMSLog.i(str, i, "network is ready for phoneId: " + i);
                this.mNeedRetryOverNetwork = false;
                IMSLog.i(str, i, "resend HANDLE_AUTO_CONFIG_START");
                sendMessage(obtainMessage(2, i, 0, (Object) null));
            }
        }
    }

    private void onADSChanged(int i) {
        IMSLog.i(LOG_TAG, i, "onADSChanged");
        for (int i2 = 0; i2 < SimUtil.getPhoneCount(); i2++) {
            IWorkflow workflow = this.mWorkFlowController.getWorkflow(i2);
            if (workflow != null) {
                workflow.onADSChanged();
            }
        }
    }

    public Pair<Network, Integer> getAvailableNetwork(int i) {
        this.mMobileNetwork = false;
        this.mWifiNetwork = false;
        this.mReadyNetwork.put(i, Boolean.TRUE);
        if (this.mNetworkLists.get(i).containsKey(1)) {
            this.mMobileNetwork = true;
            return Pair.create((Network) this.mNetworkLists.get(i).get(1), 1);
        } else if (this.mNetworkLists.get(i).containsKey(2)) {
            this.mMobileNetwork = true;
            return Pair.create((Network) this.mNetworkLists.get(i).get(2), 2);
        } else if (this.mNetworkLists.get(i).containsKey(3)) {
            this.mWifiNetwork = true;
            return Pair.create((Network) this.mNetworkLists.get(i).get(3), 3);
        } else {
            this.mReadyNetwork.put(i, Boolean.FALSE);
            return null;
        }
    }

    public Network getAvailableNetworkForNetworkType(int i, int i2) {
        HashMap hashMap = this.mNetworkLists.get(i);
        if (hashMap == null || !hashMap.containsKey(Integer.valueOf(i2))) {
            return null;
        }
        return (Network) hashMap.get(Integer.valueOf(i2));
    }

    /* access modifiers changed from: package-private */
    public boolean isGcEnabledChange(int i) {
        boolean z = ImsSharedPrefHelper.getBoolean(i, this.mContext, "imsswitch", "isGcEnabledChange", false);
        String str = LOG_TAG;
        Log.i(str, "isGcEnabledChange: " + z);
        return z;
    }

    private void setIsGcEnabledChange(boolean z, int i) {
        String str = LOG_TAG;
        IMSLog.i(str, i, "setIsGcEnabledChange: " + z);
        ImsSharedPrefHelper.save(i, this.mContext, "imsswitch", "isGcEnabledChange", false);
    }

    public IStorageAdapter getStorage(int i) {
        return this.mWorkFlowController.getStorage(i);
    }

    public void setRcsClientConfiguration(int i, String str, String str2, String str3, String str4, String str5) {
        this.mConfigTrigger.setRcsClientConfiguration(i, this.mWorkFlowController.getWorkflow(i), str, str2, str3, str4, str5);
    }

    public void triggerAutoConfiguration(int i) {
        this.mConfigTrigger.triggerAutoConfiguration(i);
    }
}
