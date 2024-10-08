package com.sec.internal.ims.core;

import android.app.NotificationManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.TelephonyCallback;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.os.SecFeature;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.ServiceStateWrapper;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public class ImsIconManager {
    private static final String CMC_SD_ICON = "stat_sys_phone_call_skt";
    private static final String[] CROSS_SIM_ICON_NAME = {"stat_sys_cross_sim_calling1_spr", "stat_sys_cross_sim_calling2_spr"};
    public static final String DEFAULT_VOLTE_REGI_ICON_ID = "stat_notify_volte_service_avaliable";
    private static final String DUAL_IMS_NO_CTC_VOLTE_ICON_NAME = "stat_sys_phone_no_volte_chn_hd";
    private static final String INTENT_ACTION_CONFIGURATION_CHANGED = "android.intent.action.CONFIGURATION_CHANGED";
    private static final String INTENT_ACTION_SILENT_REDIAL = "com.samsung.intent.action.PHONE_NEED_SILENT_REDIAL";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "ImsIconManager";
    private static final int NOTIFICATION_BUILDER_ID = -26247;
    private static final String NO_CTC_VOLTE_ICON_NAME = "stat_sys_phone_no_volte_chn_ctc";
    private static final String PRIMARY_CHANNEL = "imsicon_channel";
    private static final String RCS_ICON_DESCRIPTION = "RCS";
    protected static final String RCS_ICON_NAME = "stat_notify_rcs_service_avaliable";
    protected static final String RCS_ICON_NAME_CHN = "stat_notify_rcs";
    protected static final String[] RCS_ICON_NAME_DUAL = {"stat_notify_rcs_service_avaliable_1", "stat_notify_rcs_service_avaliable_2", "stat_notify_rcs_service_avaliable_dual"};
    protected static final String RCS_ICON_SLOT = "com.samsung.rcs";
    protected static final String VOLTE_ICON_SLOT_HEAD = "ims_volte";
    private static final String VoLTE_ICON_WFC_WARNING_NAME = "stat_notify_wfc_warning";
    static boolean[] mShowVoWIFILabel = {false, false, false};
    static String[] mVowifiOperatorLabel = {"", ""};
    static int[] mVowifiOperatorLabelOngoing = {0, 0};
    static String[] mWifiSubTextOnLockScreen = {"", ""};
    protected String VOLTE_ICON_SLOT = "";
    ConnectivityManager mConnectivityManager;
    Context mContext;
    protected boolean mCurrentInRoaming;
    protected int mCurrentNetworkType;
    int mCurrentPhoneState;
    protected int mCurrentServiceState;
    protected int mCurrentVoiceRatType;
    int mDisplayDensity = -1;
    boolean mForceRefreshIcon = false;
    final BroadcastReceiver mIconBroadcastReceiver;
    protected boolean mIsDebuggable = Build.IS_DEBUGGABLE;
    boolean mIsDuringEmergencyCall;
    boolean mIsFirstVoLTEIconShown = false;
    boolean mIsSilentRedialInProgress;
    boolean mIsVonrEnabled;
    IconVisibility mLastRcsVisiblity = IconVisibility.HIDE;
    int mLastVoLTEResourceId = -1;
    IconVisibility mLastVoLTEVisiblity = IconVisibility.UNKNOWN;
    Mno mMno = Mno.DEFAULT;
    NotificationManager mNotificationManager;
    String mOmcCode;
    String mPackageName;
    PdnController mPdnController;
    int mPhoneId = 0;
    IRegistrationManager mRegistrationManager;
    TelephonyCallback mTelephonyCallback;
    ITelephonyManager mTelephonyManager;
    boolean mUseDualVolteIcon = false;
    final ContentObserver mVolteNotiObserver;
    private final ContentObserver simSwitchChangeObserver;

    public enum Icon {
        VOLTE,
        VOWIFI,
        VO5G
    }

    public enum IconVisibility {
        UNKNOWN,
        SHOW,
        HIDE
    }

    private class TelephonyCallbackImpl extends TelephonyCallback implements TelephonyCallback.ServiceStateListener, TelephonyCallback.CallStateListener {
        private int mPhoneId;

        private TelephonyCallbackImpl(int i) {
            this.mPhoneId = i;
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            String r0 = ImsIconManager.LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.d(r0, i, "onServiceStateChanged: " + serviceState);
            if (isUpdateRequires(serviceState)) {
                IMSLog.i(ImsIconManager.LOG_TAG, this.mPhoneId, "updateRegistrationIcon on RAT change");
                ImsIconManager.this.updateRegistrationIcon();
            }
        }

        private boolean isUpdateRequires(ServiceState serviceState) {
            ServiceStateWrapper serviceStateWrapper = new ServiceStateWrapper(serviceState);
            ImsIconManager imsIconManager = ImsIconManager.this;
            int i = imsIconManager.mCurrentNetworkType;
            int i2 = imsIconManager.mCurrentServiceState;
            int i3 = imsIconManager.mCurrentVoiceRatType;
            boolean z = imsIconManager.mCurrentInRoaming;
            imsIconManager.setCurrentNetworkType(serviceStateWrapper.getDataNetworkType());
            ImsIconManager.this.setCurrentServiceState(serviceStateWrapper.getDataRegState());
            ImsIconManager.this.setCurrentVoiceRatType(serviceStateWrapper.getVoiceNetworkType());
            ImsIconManager.this.setCurrentRoamingState(serviceStateWrapper.getVoiceRoaming());
            if (!ImsIconManager.this.mMno.isChn() && !ImsIconManager.this.mMno.isHkMo() && !ImsIconManager.this.mMno.isTw() && !ConfigUtil.isRcsEur(ImsIconManager.this.mMno) && !ImsIconManager.this.mMno.isOce() && !ImsIconManager.this.mMno.isLatin() && !ImsIconManager.this.mMno.isATTMexico() && !OmcCode.isKOROmcCode()) {
                return false;
            }
            boolean z2 = ImsIconManager.this.mMno.isOneOf(Mno.CTC, Mno.CTCMO) && z != ImsIconManager.this.mCurrentInRoaming;
            boolean z3 = (i2 != 0 && serviceStateWrapper.getDataRegState() == 0) || (i2 == 0 && serviceStateWrapper.getDataRegState() != 0);
            boolean z4 = ImsIconManager.this.mMno.isOneOf(Mno.CTC, Mno.CTCMO) && i3 != ImsIconManager.this.mCurrentVoiceRatType;
            if (z3 || isNWTypeChangedUpdateRequires(i) || z2 || z4 || isSeparatedVo5gIcon(i, i3)) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean isNWTypeChangedUpdateRequires(int i) {
            return isImsIconSupportedNW(i) != isImsIconSupportedNW(ImsIconManager.this.mCurrentNetworkType);
        }

        /* access modifiers changed from: package-private */
        public boolean isImsIconSupportedNW(int i) {
            return NetworkUtil.is3gppPsVoiceNetwork(i) || i == 18;
        }

        /* access modifiers changed from: package-private */
        public boolean isSeparatedVo5gIcon(int i, int i2) {
            if (!ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.Registration.SEPARATE_VO5G_ICON, false)) {
                return false;
            }
            if (i == 18) {
                i = i2;
            }
            ImsIconManager imsIconManager = ImsIconManager.this;
            int i3 = imsIconManager.mCurrentNetworkType;
            if (i3 == 18) {
                i3 = imsIconManager.mCurrentVoiceRatType;
            }
            String r6 = ImsIconManager.LOG_TAG;
            int i4 = this.mPhoneId;
            IMSLog.i(r6, i4, "isSeparatedVo5gIcon oldCellularNetworkType :" + i + " , newCellularNetworkType :" + i3);
            if (i == i3 || !NetworkUtil.is3gppPsVoiceNetwork(i3)) {
                return false;
            }
            return true;
        }

        public void onCallStateChanged(int i) {
            if (i != ImsIconManager.this.mCurrentPhoneState) {
                String r0 = ImsIconManager.LOG_TAG;
                int i2 = this.mPhoneId;
                IMSLog.i(r0, i2, "call state is changed to [" + i + "]");
                ImsIconManager.this.mCurrentPhoneState = i;
                if (i == 0 && (OmcCode.isKOROmcCode() || ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.Call.HIDE_VOWIFI_ICON_WHEN_CS_CALL, false))) {
                    ImsIconManager imsIconManager = ImsIconManager.this;
                    imsIconManager.mIsSilentRedialInProgress = false;
                    imsIconManager.updateRegistrationIcon();
                }
                if (i == 0 && ImsIconManager.this.getDuringEmergencyCall()) {
                    ImsIconManager.this.setDuringEmergencyCall(false);
                }
            }
        }
    }

    public ImsIconManager(Context context, IRegistrationManager iRegistrationManager, PdnController pdnController, Mno mno, int i) {
        AnonymousClass1 r0 = new ContentObserver(new Handler()) {
            public void onChange(boolean z, Uri uri) {
                if (uri != null) {
                    String r0 = ImsIconManager.LOG_TAG;
                    int i = ImsIconManager.this.mPhoneId;
                    IMSLog.i(r0, i, "onChange() " + uri.getLastPathSegment() + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + SimUtil.isSimActive(ImsIconManager.this.mContext, "phone1_on".equals(uri.getLastPathSegment()) ^ true ? 1 : 0));
                    ImsIconManager.this.updateRegistrationIcon();
                }
            }
        };
        this.simSwitchChangeObserver = r0;
        AnonymousClass2 r1 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                DisplayMetrics displayMetrics;
                int i;
                String action = intent.getAction();
                String r0 = ImsIconManager.LOG_TAG;
                int i2 = ImsIconManager.this.mPhoneId;
                IMSLog.d(r0, i2, "Received intent: " + action + " extra: " + intent.getExtras());
                if (action.equals(ImsIconManager.INTENT_ACTION_SILENT_REDIAL)) {
                    ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(ImsIconManager.this.mPhoneId);
                    ImsIconManager imsIconManager = ImsIconManager.this;
                    if ((imsIconManager.mMno == Mno.SKT || ImsRegistry.getBoolean(imsIconManager.mPhoneId, GlobalSettingsConstants.Call.HIDE_VOWIFI_ICON_WHEN_CS_CALL, false)) && simManagerFromSimSlot != null && simManagerFromSimSlot.isSimAvailable()) {
                        IMSLog.i(ImsIconManager.LOG_TAG, ImsIconManager.this.mPhoneId, "Silent Redial Enabled");
                        if (SimUtil.getPhoneCount() > 1) {
                            int intExtra = intent.getIntExtra("SLOTID", -1);
                            ImsIconManager imsIconManager2 = ImsIconManager.this;
                            if (imsIconManager2.mPhoneId == intExtra) {
                                imsIconManager2.mIsSilentRedialInProgress = true;
                                imsIconManager2.updateRegistrationIcon();
                                return;
                            }
                            return;
                        }
                        ImsIconManager imsIconManager3 = ImsIconManager.this;
                        imsIconManager3.mIsSilentRedialInProgress = true;
                        imsIconManager3.updateRegistrationIcon();
                    }
                } else if (action.equals(ImsIconManager.INTENT_ACTION_CONFIGURATION_CHANGED) && (displayMetrics = ImsIconManager.this.mContext.getResources().getDisplayMetrics()) != null && ImsIconManager.this.mDisplayDensity != (i = displayMetrics.densityDpi)) {
                    IMSLog.i(ImsIconManager.LOG_TAG, ImsIconManager.this.mPhoneId, "config is changed. update icon");
                    ImsIconManager imsIconManager4 = ImsIconManager.this;
                    imsIconManager4.mForceRefreshIcon = true;
                    imsIconManager4.updateRegistrationIcon();
                    ImsIconManager imsIconManager5 = ImsIconManager.this;
                    imsIconManager5.mDisplayDensity = i;
                    imsIconManager5.mForceRefreshIcon = false;
                }
            }
        };
        this.mIconBroadcastReceiver = r1;
        AnonymousClass3 r2 = new ContentObserver(new Handler(Looper.myLooper())) {
            public void onChange(boolean z) {
                IMSLog.i(ImsIconManager.LOG_TAG, ImsIconManager.this.mPhoneId, "call settins is changed. update icon");
                ImsIconManager.this.updateRegistrationIcon();
            }
        };
        this.mVolteNotiObserver = r2;
        this.mContext = context;
        this.mPackageName = context.getPackageName();
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(this.mContext);
        this.mRegistrationManager = iRegistrationManager;
        this.mPdnController = pdnController;
        this.mOmcCode = OmcCode.get();
        this.mUseDualVolteIcon = showDualVolteIcon();
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mPhoneId = i;
        this.mTelephonyCallback = new TelephonyCallbackImpl(i);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("volte_noti_settings"), true, r2);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTION_SILENT_REDIAL);
        intentFilter.addAction(INTENT_ACTION_CONFIGURATION_CHANGED);
        this.mContext.registerReceiver(r1, intentFilter);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("phone1_on"), true, r0);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("phone2_on"), true, r0);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("esim_phone_on_1"), true, r0);
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        initConfiguration(mno, i);
    }

    public void initConfiguration(Mno mno, int i) {
        this.mMno = (Mno) Optional.ofNullable(mno).orElse(Mno.DEFAULT);
        this.mPhoneId = i;
        boolean z = false;
        this.mIsSilentRedialInProgress = false;
        this.mIsDuringEmergencyCall = false;
        if (ImsRegistry.getBoolean(i, GlobalSettingsConstants.Registration.SEPARATE_VO5G_ICON, false) && this.mTelephonyManager.semIsVoNrEnabled(i)) {
            z = true;
        }
        this.mIsVonrEnabled = z;
        registerPhoneStateListener();
        clearIcon(i);
        this.VOLTE_ICON_SLOT = getVolteIconSlot();
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "initConfiguration: " + this.VOLTE_ICON_SLOT);
    }

    /* access modifiers changed from: package-private */
    public String getVolteIconSlot() {
        int intValue = ((Integer) SimManagerFactory.getAllSimManagers().stream().map(new ImsIconManager$$ExternalSyntheticLambda0()).reduce(new ImsIconManager$$ExternalSyntheticLambda1()).orElse(0)).intValue();
        int activeSimCount = SimUtil.getActiveSimCount(this.mContext);
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getVolteIconSlot: eSIM Count: " + intValue + ", active SIM count: " + activeSimCount);
        if (intValue == 1 && activeSimCount == 1) {
            return VOLTE_ICON_SLOT_HEAD;
        }
        String str2 = "";
        if (intValue == 1 && activeSimCount == 2) {
            boolean booleanValue = ((Boolean) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId)).map(new ImsIconManager$$ExternalSyntheticLambda2()).orElse(Boolean.FALSE)).booleanValue();
            StringBuilder sb = new StringBuilder();
            sb.append(VOLTE_ICON_SLOT_HEAD);
            if (booleanValue) {
                str2 = "2";
            }
            sb.append(str2);
            return sb.toString();
        } else if (intValue == 2) {
            int subId = SimUtil.getSubId(this.mPhoneId);
            int subId2 = SimUtil.getSubId(SimUtil.getOppositeSimSlot(this.mPhoneId));
            StringBuilder sb2 = new StringBuilder();
            sb2.append(VOLTE_ICON_SLOT_HEAD);
            if (subId >= subId2) {
                str2 = "2";
            }
            sb2.append(str2);
            return sb2.toString();
        } else {
            StringBuilder sb3 = new StringBuilder();
            sb3.append(VOLTE_ICON_SLOT_HEAD);
            if (this.mPhoneId != 0) {
                str2 = "2";
            }
            sb3.append(str2);
            return sb3.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public void clearIcon(int i) {
        if (!needShowRcsIcon(i)) {
            setIconVisibility(RCS_ICON_SLOT, IconVisibility.HIDE);
        }
        if (!needShowNoCTCVoLTEIcon() && !TextUtils.isEmpty(this.VOLTE_ICON_SLOT)) {
            this.mLastVoLTEResourceId = -1;
            setIconVisibility(this.VOLTE_ICON_SLOT, IconVisibility.HIDE);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isServiceAvailable(String str) {
        Mno mno;
        if (!"ATT".equals(this.mOmcCode) && !"APP".equals(this.mOmcCode)) {
            Mno mno2 = this.mMno;
            if (mno2 != Mno.BOG && mno2 != Mno.ORANGE && mno2 != Mno.ORANGE_POLAND && mno2 != Mno.DIGI && mno2 != Mno.TELECOM_ITALY && mno2 != Mno.VODAFONE && !mno2.isTmobile() && (mno = this.mMno) != Mno.TELEKOM_ALBANIA && mno != Mno.VODAFONE_NEWZEALAND && mno != Mno.WINDTRE) {
                return true;
            }
            int currentNetworkByPhoneId = this.mRegistrationManager.getCurrentNetworkByPhoneId(this.mPhoneId);
            if (!NetworkUtil.is3gppPsVoiceNetwork(currentNetworkByPhoneId) && (currentNetworkByPhoneId != 18 || !this.mPdnController.isEpdgConnected(this.mPhoneId))) {
                return false;
            }
            if ("mmtel".equals(str) || "mmtel-video".equals(str)) {
                return true;
            }
            return false;
        } else if (SimUtil.isSoftphoneEnabled()) {
            return true;
        } else {
            int currentNetworkByPhoneId2 = this.mRegistrationManager.getCurrentNetworkByPhoneId(this.mPhoneId);
            if (NetworkUtil.is3gppPsVoiceNetwork(currentNetworkByPhoneId2) || currentNetworkByPhoneId2 == 18) {
                return true;
            }
            if ("mmtel".equals(str) || "mmtel-video".equals(str)) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean needShowNoCTCVoLTEIcon() {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        boolean z = false;
        if (simManagerFromSimSlot != null && this.mTelephonyManager.getRilSimOperator(this.mPhoneId).contains("CTC") && (this.mUseDualVolteIcon || this.mPhoneId == SimUtil.getActiveDataPhoneId())) {
            int voiceCallType = ImsConstants.SystemSettings.getVoiceCallType(this.mContext, -1, this.mPhoneId);
            int i = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0);
            if (voiceCallType == 0 && NetworkUtil.is3gppPsVoiceNetwork(this.mCurrentNetworkType) && i == 0 && simManagerFromSimSlot.isSimLoaded() && this.mTelephonyManager.getCurrentPhoneTypeForSlot(this.mPhoneId) != 2 && this.mCurrentVoiceRatType != 7 && !this.mCurrentInRoaming) {
                z = true;
            }
        }
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "needShowNoCTCVoLTEIcon: " + z);
        return z;
    }

    static class IconVisiblities {
        boolean mShowRcsIcon;
        boolean mShowVoWiFiIcon;
        boolean mShowVolteIcon;

        IconVisiblities() {
        }

        IconVisiblities(boolean z, boolean z2, boolean z3) {
            this.mShowVolteIcon = z;
            this.mShowVoWiFiIcon = z2;
            this.mShowRcsIcon = z3;
        }

        /* access modifiers changed from: package-private */
        public void setShowVolteIcon(boolean z) {
            this.mShowVolteIcon = z;
        }

        /* access modifiers changed from: package-private */
        public boolean isShowVolteIcon() {
            return this.mShowVolteIcon;
        }

        /* access modifiers changed from: package-private */
        public void setShowVoWiFiIcon(boolean z) {
            this.mShowVoWiFiIcon = z;
        }

        /* access modifiers changed from: package-private */
        public boolean isShowVowiFiIcon() {
            return this.mShowVoWiFiIcon;
        }

        /* access modifiers changed from: package-private */
        public void setShowRcsIcon(boolean z) {
            this.mShowRcsIcon = z;
        }

        /* access modifiers changed from: package-private */
        public boolean isShowRcsIcon() {
            return this.mShowRcsIcon;
        }
    }

    static class RegistrationStatus {
        boolean mCmcRegistered;
        boolean mCrossSimRegistered;
        boolean mIsRcsNetworkSuspended;
        boolean mRcsRegistered;
        boolean mVolteRegistered;
        boolean mVowifiRegistered;

        RegistrationStatus() {
        }

        RegistrationStatus(boolean z, boolean z2, boolean z3) {
            this.mVolteRegistered = z;
            this.mRcsRegistered = z2;
            this.mVowifiRegistered = z3;
        }

        /* access modifiers changed from: package-private */
        public void setVolteRegistered(boolean z) {
            this.mVolteRegistered = z;
        }

        /* access modifiers changed from: package-private */
        public boolean isVolteRegistered() {
            return this.mVolteRegistered;
        }

        /* access modifiers changed from: package-private */
        public void setCrossSimRegistered(boolean z) {
            this.mCrossSimRegistered = z;
        }

        /* access modifiers changed from: package-private */
        public boolean isCrossSimRegistered() {
            return this.mCrossSimRegistered;
        }

        /* access modifiers changed from: package-private */
        public void setRcsRegistered(boolean z) {
            this.mRcsRegistered = z;
        }

        /* access modifiers changed from: package-private */
        public boolean isRcsRegistered() {
            return this.mRcsRegistered;
        }

        public void setRcsNetworkSuspended(boolean z) {
            this.mIsRcsNetworkSuspended = z;
        }

        public boolean isRcsNetworkSuspended() {
            return this.mIsRcsNetworkSuspended;
        }

        /* access modifiers changed from: package-private */
        public void setVowifiRegistered(boolean z) {
            this.mVowifiRegistered = z;
        }

        /* access modifiers changed from: package-private */
        public boolean isVowifiRegistered() {
            return this.mVowifiRegistered;
        }

        /* access modifiers changed from: package-private */
        public void setCmcRegistered(boolean z) {
            this.mCmcRegistered = z;
        }

        /* access modifiers changed from: package-private */
        public boolean isCmcRegistered() {
            return this.mCmcRegistered;
        }

        /* access modifiers changed from: package-private */
        public boolean isAllRegistered() {
            return (isVolteRegistered() || isVowifiRegistered()) && isRcsRegistered();
        }
    }

    public void updateRegistrationIcon() {
        this.mUseDualVolteIcon = showDualVolteIcon();
        boolean z = false;
        int voiceCallType = ImsConstants.SystemSettings.getVoiceCallType(this.mContext, 0, this.mPhoneId);
        IconVisiblities updateShowIconSettings = updateShowIconSettings(voiceCallType);
        ImsRegistration[] registrationInfoByPhoneId = this.mRegistrationManager.getRegistrationInfoByPhoneId(this.mPhoneId);
        RegistrationStatus updateRegistrationStatus = updateRegistrationStatus(registrationInfoByPhoneId, voiceCallType);
        IMSLog.i(LOG_TAG, this.mPhoneId, "updateRegistrationIcon: VoLTE [show: " + updateShowIconSettings.isShowVolteIcon() + ", regi: " + updateRegistrationStatus.isVolteRegistered() + "] VoWiFi [show: " + updateShowIconSettings.isShowVowiFiIcon() + ", regi: " + updateRegistrationStatus.isVowifiRegistered() + "] RCS [show: " + updateShowIconSettings.isShowRcsIcon() + ", regi: " + updateRegistrationStatus.isRcsRegistered() + "] CROSS SIM [regi: " + updateRegistrationStatus.isCrossSimRegistered() + "] (RcsNetworkSuspended: " + updateRegistrationStatus.isRcsNetworkSuspended() + ") (VoNREnabled: " + this.mIsVonrEnabled + ")");
        if (updateRegistrationStatus.isCmcRegistered() && registrationInfoByPhoneId != null && registrationInfoByPhoneId.length == 1) {
            z = true;
        }
        updateVolteIcon(updateShowIconSettings, updateRegistrationStatus, z);
        updateRcsIcon(updateShowIconSettings, updateRegistrationStatus);
        updateVoWifiLabel(updateShowIconSettings, updateRegistrationStatus);
    }

    /* access modifiers changed from: package-private */
    public IconVisiblities updateShowIconSettings(int i) {
        IconVisiblities iconVisiblities = new IconVisiblities();
        iconVisiblities.setShowVolteIcon(true);
        iconVisiblities.setShowVoWiFiIcon(ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.Registration.SHOW_VOWIFI_REGI_ICON, false));
        iconVisiblities.setShowRcsIcon(true);
        boolean z = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.Registration.SHOW_VOLTE_REGI_ICON, false);
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) >= 32 && ((simManagerFromSimSlot != null && "GenericIR92_US:Cellcom".equals(simManagerFromSimSlot.getSimMnoName())) || this.mMno.isOneOf(Mno.DPAC, Mno.GTA, Mno.ITE, Mno.SPRINT, Mno.ASTCA_US))) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Volte RegistrationIcon: need to turn off");
            z = false;
        }
        int i2 = ImsRegistry.getInt(this.mPhoneId, GlobalSettingsConstants.Registration.REMOVE_ICON_NOSVC, 0);
        if (!this.mIsDebuggable) {
            if (!z) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "Volte/RCS RegistrationIcon: turned off.");
                iconVisiblities.setShowVolteIcon(false);
            }
            if (!ConfigUtil.isRcsChn(this.mMno)) {
                iconVisiblities.setShowRcsIcon(false);
            }
        }
        if (ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.Call.HIDE_VOWIFI_ICON_WHEN_CS_CALL, false) && iconVisiblities.isShowVowiFiIcon() && this.mIsSilentRedialInProgress) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "VoWIFI Special Req.: Hide vowifi icon when CSFB");
            iconVisiblities.setShowVoWiFiIcon(false);
        }
        if ("DCM".equals(this.mOmcCode) && this.mPdnController.getVopsIndication(this.mPhoneId) == VoPsIndication.NOT_SUPPORTED) {
            iconVisiblities.setShowVolteIcon(false);
        }
        if (this.mMno.isKor()) {
            if (OmcCode.isKOROmcCode()) {
                iconVisiblities.setShowVolteIcon(checkKORVolteIcon());
                return iconVisiblities;
            } else if (i != 0) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "KOR requirement");
                iconVisiblities.setShowVolteIcon(false);
            }
        }
        if (i2 == 1 && (this.mCurrentServiceState != 0 || (!NetworkUtil.is3gppPsVoiceNetwork(this.mCurrentNetworkType) && this.mCurrentNetworkType != 18))) {
            iconVisiblities.setShowVolteIcon(false);
        }
        return iconVisiblities;
    }

    /* access modifiers changed from: package-private */
    public RegistrationStatus updateRegistrationStatus(ImsRegistration[] imsRegistrationArr, int i) {
        RegistrationStatus registrationStatus = new RegistrationStatus();
        if (OmcCode.isKOROmcCode() && this.mMno == Mno.KT) {
            int subId = SimUtil.getSubId(this.mPhoneId);
            if (subId < 0) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "updateRegistrationStatus : subId is invalid");
                return registrationStatus;
            } else if (this.mTelephonyManager.getServiceStateForSubscriber(subId) == 0) {
                registrationStatus.setVolteRegistered(true);
            }
        }
        if (imsRegistrationArr == null) {
            return registrationStatus;
        }
        boolean z = false;
        for (ImsRegistration imsRegistration : imsRegistrationArr) {
            if (isVoImsRegistered(imsRegistration)) {
                boolean isVoWiFiConnected = isVoWiFiConnected(imsRegistration);
                boolean isCrossSimConnected = isCrossSimConnected(imsRegistration);
                registrationStatus.setVolteRegistered(!isVoWiFiConnected && !isCrossSimConnected);
                registrationStatus.setVowifiRegistered(isVoWiFiConnected);
                registrationStatus.setCrossSimRegistered(isCrossSimConnected);
            }
            if (imsRegistration.getImsProfile().getCmcType() == 2 || imsRegistration.getImsProfile().getCmcType() == 4 || imsRegistration.getImsProfile().getCmcType() == 8) {
                registrationStatus.setCmcRegistered(true);
            }
            if (imsRegistration.hasRcsService()) {
                boolean z2 = !isSuspend(imsRegistration.getNetwork());
                registrationStatus.setRcsNetworkSuspended(!z2);
                if (ConfigUtil.isRcsChn(this.mMno)) {
                    z2 = z2 && isInSvcAndOtherSimIdle();
                }
                registrationStatus.setRcsRegistered(z2);
            }
            if (registrationStatus.isAllRegistered()) {
                break;
            }
        }
        if (getDuringEmergencyCall() && registrationStatus.isVowifiRegistered()) {
            Mno mno = this.mMno;
            if (mno == Mno.APT) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "APT special requirement");
                if (i == 0) {
                    z = true;
                }
                registrationStatus.setVolteRegistered(z);
                registrationStatus.setVowifiRegistered(!z);
            } else if (mno == Mno.VODAFONE_AUSTRALIA) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "Vodafone AUS special requirement");
                registrationStatus.setVowifiRegistered(false);
            }
        }
        return registrationStatus;
    }

    private boolean isSuspend(Network network) {
        return Optional.ofNullable(this.mConnectivityManager.getNetworkCapabilities(network)).filter(new ImsIconManager$$ExternalSyntheticLambda3()).isPresent();
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$isSuspend$1(NetworkCapabilities networkCapabilities) {
        return !networkCapabilities.hasCapability(21);
    }

    /* access modifiers changed from: package-private */
    public boolean isVoImsRegistered(ImsRegistration imsRegistration) {
        return hasVolteService(imsRegistration) && !imsRegistration.getImsProfile().hasEmergencySupport() && imsRegistration.getImsProfile().getCmcType() == 0 && (isServiceAvailable("mmtel") || isServiceAvailable("mmtel-video"));
    }

    /* access modifiers changed from: package-private */
    public boolean isVoWiFiConnected(ImsRegistration imsRegistration) {
        int currentNetwork = this.mRegistrationManager.getCurrentNetwork(imsRegistration.getHandle());
        int regiRat = imsRegistration.getRegiRat();
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getRegiRat [" + regiRat + "], getCurrentNetwork [" + currentNetwork + "]");
        if (currentNetwork != 18 || !this.mPdnController.isEpdgConnected(this.mPhoneId) || this.mPdnController.getEpdgPhysicalInterface(this.mPhoneId) != 1) {
            return false;
        }
        if (this.mMno != Mno.CHT || regiRat == 18) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isCrossSimConnected(ImsRegistration imsRegistration) {
        return this.mRegistrationManager.getCurrentNetwork(imsRegistration.getHandle()) == 18 && this.mPdnController.isEpdgConnected(this.mPhoneId) && this.mPdnController.getEpdgPhysicalInterface(this.mPhoneId) == 2;
    }

    /* access modifiers changed from: package-private */
    public boolean needDisplayVo5gIcon() {
        if ((this.mMno != Mno.RJIL || this.mIsVonrEnabled) && this.mCurrentNetworkType == 20 && ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.Registration.SEPARATE_VO5G_ICON, false)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00e5  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0112  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateVolteIcon(com.sec.internal.ims.core.ImsIconManager.IconVisiblities r7, com.sec.internal.ims.core.ImsIconManager.RegistrationStatus r8, boolean r9) {
        /*
            r6 = this;
            java.lang.String r0 = r6.VOLTE_ICON_SLOT
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 == 0) goto L_0x0009
            return
        L_0x0009:
            boolean r0 = r6.mUseDualVolteIcon
            r1 = 2131165184(0x7f070000, float:1.7944578E38)
            r2 = 2131165185(0x7f070001, float:1.794458E38)
            r3 = 0
            r4 = 1
            if (r0 == 0) goto L_0x008b
            boolean r0 = r8.isVowifiRegistered()
            if (r0 == 0) goto L_0x0035
            boolean r0 = r7.isShowVowiFiIcon()
            if (r0 == 0) goto L_0x0035
            com.sec.internal.ims.core.ImsIconManager$Icon r0 = com.sec.internal.ims.core.ImsIconManager.Icon.VOWIFI
            java.lang.String r3 = r6.getDualIMSIconName(r0)
            android.content.Context r0 = r6.mContext
            android.content.res.Resources r0 = r0.getResources()
            java.lang.String r0 = r0.getString(r2)
        L_0x0030:
            r5 = r3
            r3 = r0
            r0 = r5
            goto L_0x00e3
        L_0x0035:
            boolean r0 = r8.isVolteRegistered()
            if (r0 == 0) goto L_0x005b
            boolean r0 = r7.isShowVolteIcon()
            if (r0 == 0) goto L_0x005b
            boolean r0 = r6.needDisplayVo5gIcon()
            if (r0 == 0) goto L_0x004a
            com.sec.internal.ims.core.ImsIconManager$Icon r0 = com.sec.internal.ims.core.ImsIconManager.Icon.VO5G
            goto L_0x004c
        L_0x004a:
            com.sec.internal.ims.core.ImsIconManager$Icon r0 = com.sec.internal.ims.core.ImsIconManager.Icon.VOLTE
        L_0x004c:
            java.lang.String r3 = r6.getDualIMSIconName(r0)
            android.content.Context r0 = r6.mContext
            android.content.res.Resources r0 = r0.getResources()
            java.lang.String r0 = r0.getString(r1)
            goto L_0x0030
        L_0x005b:
            boolean r0 = r7.isShowVolteIcon()
            if (r0 == 0) goto L_0x0080
            boolean r0 = r6.needShowNoCTCVoLTEIcon()
            if (r0 == 0) goto L_0x0080
            r8.setVolteRegistered(r4)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "stat_sys_phone_no_volte_chn_hd"
            r0.append(r1)
            int r1 = r6.mPhoneId
            int r1 = r1 + r4
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            goto L_0x00e3
        L_0x0080:
            boolean r0 = r8.isCrossSimRegistered()
            if (r0 == 0) goto L_0x00e2
            java.lang.String r0 = r6.getCrossSimIconName()
            goto L_0x00e3
        L_0x008b:
            boolean r0 = r8.isVolteRegistered()
            if (r0 == 0) goto L_0x00b3
            boolean r0 = r7.isShowVolteIcon()
            if (r0 == 0) goto L_0x00b3
            boolean r0 = r6.needDisplayVo5gIcon()
            if (r0 == 0) goto L_0x00a2
            java.lang.String r0 = r6.getVo5gIconName()
            goto L_0x00a6
        L_0x00a2:
            java.lang.String r0 = r6.getVolteIconName()
        L_0x00a6:
            r3 = r0
            android.content.Context r0 = r6.mContext
            android.content.res.Resources r0 = r0.getResources()
            java.lang.String r0 = r0.getString(r1)
            goto L_0x0030
        L_0x00b3:
            boolean r0 = r8.isVowifiRegistered()
            if (r0 == 0) goto L_0x00cf
            boolean r0 = r7.isShowVowiFiIcon()
            if (r0 == 0) goto L_0x00cf
            java.lang.String r3 = r6.getVowifiIconName()
            android.content.Context r0 = r6.mContext
            android.content.res.Resources r0 = r0.getResources()
            java.lang.String r0 = r0.getString(r2)
            goto L_0x0030
        L_0x00cf:
            boolean r0 = r7.isShowVolteIcon()
            if (r0 == 0) goto L_0x00e2
            boolean r0 = r6.needShowNoCTCVoLTEIcon()
            if (r0 == 0) goto L_0x00e2
            r8.setVolteRegistered(r4)
            java.lang.String r0 = "stat_sys_phone_no_volte_chn_ctc"
            goto L_0x00e3
        L_0x00e2:
            r0 = r3
        L_0x00e3:
            if (r3 != 0) goto L_0x00e7
            java.lang.String r3 = ""
        L_0x00e7:
            boolean r1 = r6.mIsDebuggable
            if (r1 == 0) goto L_0x00f3
            if (r9 == 0) goto L_0x00f3
            r8.setVolteRegistered(r4)
            java.lang.String r0 = "stat_sys_phone_call_skt"
        L_0x00f3:
            java.lang.String r9 = LOG_TAG
            int r1 = r6.mPhoneId
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "updateVolteIcon: iconNametoSet="
            r2.append(r4)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r9, r1, r2)
            boolean r9 = android.text.TextUtils.isEmpty(r0)
            if (r9 != 0) goto L_0x0117
            java.lang.String r9 = r6.VOLTE_ICON_SLOT
            r6.setIconSlot(r9, r0, r3)
        L_0x0117:
            java.lang.String r9 = r6.VOLTE_ICON_SLOT
            com.sec.internal.ims.core.ImsIconManager$IconVisibility r7 = r6.getVolteIconVisibility(r7, r8)
            r6.setIconVisibility(r9, r7)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.ImsIconManager.updateVolteIcon(com.sec.internal.ims.core.ImsIconManager$IconVisiblities, com.sec.internal.ims.core.ImsIconManager$RegistrationStatus, boolean):void");
    }

    /* access modifiers changed from: package-private */
    public IconVisibility getVolteIconVisibility(IconVisiblities iconVisiblities, RegistrationStatus registrationStatus) {
        return ((!iconVisiblities.isShowVolteIcon() || ((!OmcCode.isKOROmcCode() || !this.mMno.isKor()) && !registrationStatus.isVolteRegistered())) && (!iconVisiblities.isShowVowiFiIcon() || !registrationStatus.isVowifiRegistered()) && !registrationStatus.isCrossSimRegistered()) ? IconVisibility.HIDE : IconVisibility.SHOW;
    }

    /* access modifiers changed from: package-private */
    public IconVisibility getRcsIconVisibility(IconVisiblities iconVisiblities, RegistrationStatus registrationStatus) {
        return (!iconVisiblities.isShowRcsIcon() || !registrationStatus.isRcsRegistered()) ? IconVisibility.HIDE : IconVisibility.SHOW;
    }

    /* access modifiers changed from: package-private */
    public void updateRcsIcon(IconVisiblities iconVisiblities, RegistrationStatus registrationStatus) {
        IconVisibility iconVisibility;
        char c = 1;
        boolean z = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.SHOW_REGI_ICON, true);
        IMSLog.i(LOG_TAG, this.mPhoneId, "isRcsIconVisible: " + z);
        if (!z) {
            iconVisiblities.setShowRcsIcon(false);
        }
        IconVisibility rcsIconVisibility = getRcsIconVisibility(iconVisiblities, registrationStatus);
        if (ConfigUtil.isRcsChn(this.mMno)) {
            if (rcsIconVisibility == IconVisibility.SHOW) {
                setIconSlot(RCS_ICON_SLOT, RCS_ICON_NAME_CHN, (String) null);
            }
        } else if (this.mIsDebuggable) {
            RcsUtils.DualRcs.refreshDualRcsReg(this.mContext);
            if (this.mPhoneId != 0) {
                c = 0;
            }
            if (!this.mMno.isEur() || !RcsUtils.DualRcs.isDualRcsSettings()) {
                iconVisibility = IconVisibility.SHOW;
                if (rcsIconVisibility == iconVisibility) {
                    setIconSlot(RCS_ICON_SLOT, RCS_ICON_NAME, (String) null);
                } else if (this.mPhoneId != SimUtil.getActiveDataPhoneId() && isCounterSlotRcsTransferable()) {
                    setIconSlot(RCS_ICON_SLOT, RCS_ICON_NAME, (String) null);
                }
            } else {
                iconVisibility = IconVisibility.SHOW;
                if (rcsIconVisibility == iconVisibility) {
                    if (isCounterSlotRcsTransferable()) {
                        setIconSlot(RCS_ICON_SLOT, RCS_ICON_NAME_DUAL[2], (String) null);
                    } else {
                        setIconSlot(RCS_ICON_SLOT, RCS_ICON_NAME_DUAL[this.mPhoneId], (String) null);
                    }
                } else if (isCounterSlotRcsTransferable()) {
                    setIconSlot(RCS_ICON_SLOT, RCS_ICON_NAME_DUAL[c], (String) null);
                }
            }
            rcsIconVisibility = iconVisibility;
        }
        setIconVisibility(RCS_ICON_SLOT, rcsIconVisibility);
    }

    /* access modifiers changed from: package-private */
    public void updateVoWifiLabel(IconVisiblities iconVisiblities, RegistrationStatus registrationStatus) {
        String string = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.Registration.VOWIFI_OPERATOR_LABEL, "");
        if (iconVisiblities.isShowVowiFiIcon() && !TextUtils.isEmpty(string)) {
            fillWifiLabel();
            boolean checkSameVoWIFILabel = checkSameVoWIFILabel();
            int oppositeSimSlot = SimUtil.getOppositeSimSlot(this.mPhoneId);
            if (checkSameVoWIFILabel) {
                showWifiRegistrationStateQuickPanel(-1, isVoWiFiRegistered(oppositeSimSlot) || registrationStatus.isVowifiRegistered());
            } else {
                showWifiRegistrationStateQuickPanel(this.mPhoneId, registrationStatus.isVowifiRegistered());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasVolteService(ImsRegistration imsRegistration) {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (this.mMno == Mno.SPRINT || (simManagerFromSimSlot != null && "GenericIR92_US:Cellcom".equals(simManagerFromSimSlot.getSimMnoName()))) {
            return imsRegistration.hasService("mmtel") || imsRegistration.hasService("mmtel-video");
        }
        return imsRegistration.hasVolteService();
    }

    /* access modifiers changed from: package-private */
    public void fillWifiLabel() {
        int[] iArr = mVowifiOperatorLabelOngoing;
        int i = this.mPhoneId;
        iArr[i] = ImsRegistry.getInt(i, GlobalSettingsConstants.Registration.VOWIFI_OPERATOR_LABEL_ONGOING, 0);
        String[] strArr = mWifiSubTextOnLockScreen;
        int i2 = this.mPhoneId;
        strArr[i2] = ImsRegistry.getString(i2, GlobalSettingsConstants.Registration.VOWIFI_SUBTEXT_ON_LOCKSCREEN, "");
        String[] strArr2 = mVowifiOperatorLabel;
        int i3 = this.mPhoneId;
        strArr2[i3] = ImsRegistry.getString(i3, GlobalSettingsConstants.Registration.VOWIFI_OPERATOR_LABEL, "");
        int oppositeSimSlot = SimUtil.getOppositeSimSlot(this.mPhoneId);
        mVowifiOperatorLabelOngoing[oppositeSimSlot] = ImsRegistry.getInt(oppositeSimSlot, GlobalSettingsConstants.Registration.VOWIFI_OPERATOR_LABEL_ONGOING, 0);
        mWifiSubTextOnLockScreen[oppositeSimSlot] = ImsRegistry.getString(oppositeSimSlot, GlobalSettingsConstants.Registration.VOWIFI_SUBTEXT_ON_LOCKSCREEN, "");
        mVowifiOperatorLabel[oppositeSimSlot] = ImsRegistry.getString(oppositeSimSlot, GlobalSettingsConstants.Registration.VOWIFI_OPERATOR_LABEL, "");
    }

    /* access modifiers changed from: package-private */
    public boolean checkSameVoWIFILabel() {
        int oppositeSimSlot = SimUtil.getOppositeSimSlot(this.mPhoneId);
        int[] iArr = mVowifiOperatorLabelOngoing;
        int i = this.mPhoneId;
        if (iArr[i] == iArr[oppositeSimSlot]) {
            String[] strArr = mWifiSubTextOnLockScreen;
            if (TextUtils.equals(strArr[i], strArr[oppositeSimSlot])) {
                String[] strArr2 = mVowifiOperatorLabel;
                if (TextUtils.equals(strArr2[this.mPhoneId], strArr2[oppositeSimSlot])) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isVoWiFiRegistered(int i) {
        ImsRegistration[] registrationInfoByPhoneId = this.mRegistrationManager.getRegistrationInfoByPhoneId(i);
        if (registrationInfoByPhoneId != null) {
            int length = registrationInfoByPhoneId.length;
            int i2 = 0;
            while (i2 < length) {
                ImsRegistration imsRegistration = registrationInfoByPhoneId[i2];
                if (!imsRegistration.hasVolteService() || imsRegistration.getImsProfile().hasEmergencySupport() || ((!isServiceAvailable("mmtel") && !isServiceAvailable("mmtel-video")) || this.mRegistrationManager.getCurrentNetworkByPhoneId(i) != 18 || this.mPdnController.getEpdgPhysicalInterface(i) != 1 || !this.mPdnController.isEpdgConnected(i))) {
                    i2++;
                } else {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "isVoWIFIRegistered");
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isCounterSlotRcsTransferable() {
        boolean z = true;
        ImsRegistration[] registrationInfoByPhoneId = this.mRegistrationManager.getRegistrationInfoByPhoneId(this.mPhoneId == 0 ? 1 : 0);
        boolean z2 = registrationInfoByPhoneId != null && Arrays.stream(registrationInfoByPhoneId).anyMatch(new ImsIconManager$$ExternalSyntheticLambda4(this));
        if (!ConfigUtil.isRcsChn(this.mMno)) {
            return z2;
        }
        if (!z2 || !isInSvcAndOtherSimIdle()) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ boolean lambda$isCounterSlotRcsTransferable$2(ImsRegistration imsRegistration) {
        return imsRegistration.hasRcsService() && !isSuspend(imsRegistration.getNetwork());
    }

    /* access modifiers changed from: package-private */
    public boolean isInSvcAndOtherSimIdle() {
        boolean z = this.mCurrentServiceState == 0;
        boolean z2 = !isOtherSimInCallStatus();
        IMSLog.i(LOG_TAG, String.format(Locale.US, "isInSvcAndOtherSimIdle: In SVC %s, Other SIM Idle %s", new Object[]{Boolean.valueOf(z), Boolean.valueOf(z2)}));
        if (!z || !z2) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isOtherSimInCallStatus() {
        if (SimUtil.isDSDACapableDevice()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isOtherSimInCallStatus: DSDA not need to check");
            return false;
        }
        int telephonyCallStatus = this.mRegistrationManager.getTelephonyCallStatus(this.mPhoneId == 0 ? 1 : 0);
        if (telephonyCallStatus == 2 || telephonyCallStatus == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00fe, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void showWifiRegistrationStateQuickPanel(int r9, boolean r10) {
        /*
            r8 = this;
            monitor-enter(r8)
            boolean[] r0 = mShowVoWIFILabel     // Catch:{ all -> 0x00ff }
            int r1 = r9 + 1
            boolean r2 = r0[r1]     // Catch:{ all -> 0x00ff }
            if (r2 != r10) goto L_0x0030
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x00ff }
            int r1 = r8.mPhoneId     // Catch:{ all -> 0x00ff }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x00ff }
            r2.<init>()     // Catch:{ all -> 0x00ff }
            java.lang.String r3 = "no need to update mShowVoWIFILabel["
            r2.append(r3)     // Catch:{ all -> 0x00ff }
            r2.append(r9)     // Catch:{ all -> 0x00ff }
            java.lang.String r9 = "]  aready ["
            r2.append(r9)     // Catch:{ all -> 0x00ff }
            r2.append(r10)     // Catch:{ all -> 0x00ff }
            java.lang.String r9 = "]"
            r2.append(r9)     // Catch:{ all -> 0x00ff }
            java.lang.String r9 = r2.toString()     // Catch:{ all -> 0x00ff }
            com.sec.internal.log.IMSLog.i(r0, r1, r9)     // Catch:{ all -> 0x00ff }
            monitor-exit(r8)
            return
        L_0x0030:
            r2 = -1
            if (r9 != r2) goto L_0x0036
            java.lang.String r3 = "imsicon_channel_both"
            goto L_0x003d
        L_0x0036:
            if (r9 != 0) goto L_0x003b
            java.lang.String r3 = "imsicon_channel_0"
            goto L_0x003d
        L_0x003b:
            java.lang.String r3 = "imsicon_channel_1"
        L_0x003d:
            r4 = -26247(0xffffffffffff9979, float:NaN)
            r5 = 0
            if (r9 == r2) goto L_0x004d
            boolean r0 = r0[r5]     // Catch:{ all -> 0x00ff }
            if (r0 == 0) goto L_0x004d
            android.app.NotificationManager r0 = r8.mNotificationManager     // Catch:{ all -> 0x00ff }
            java.lang.String r2 = "imsicon_channel_both"
            r0.cancel(r2, r4)     // Catch:{ all -> 0x00ff }
        L_0x004d:
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x00ff }
            int r2 = r8.mPhoneId     // Catch:{ all -> 0x00ff }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x00ff }
            r6.<init>()     // Catch:{ all -> 0x00ff }
            java.lang.String r7 = "show notification VoWiFi tag["
            r6.append(r7)     // Catch:{ all -> 0x00ff }
            r6.append(r9)     // Catch:{ all -> 0x00ff }
            java.lang.String r7 = "] in quick panel ["
            r6.append(r7)     // Catch:{ all -> 0x00ff }
            r6.append(r10)     // Catch:{ all -> 0x00ff }
            java.lang.String r7 = "]"
            r6.append(r7)     // Catch:{ all -> 0x00ff }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x00ff }
            com.sec.internal.log.IMSLog.i(r0, r2, r6)     // Catch:{ all -> 0x00ff }
            boolean[] r0 = mShowVoWIFILabel     // Catch:{ all -> 0x00ff }
            r0[r1] = r10     // Catch:{ all -> 0x00ff }
            r0 = 1
            if (r9 > 0) goto L_0x007c
            r9 = r5
            goto L_0x007d
        L_0x007c:
            r9 = r0
        L_0x007d:
            android.app.NotificationChannel r1 = new android.app.NotificationChannel     // Catch:{ all -> 0x00ff }
            java.lang.String r2 = "imsicon_channel"
            r6 = 2
            r1.<init>(r2, r3, r6)     // Catch:{ all -> 0x00ff }
            r1.setLockscreenVisibility(r5)     // Catch:{ all -> 0x00ff }
            android.app.NotificationManager r2 = r8.mNotificationManager     // Catch:{ all -> 0x00ff }
            r2.createNotificationChannel(r1)     // Catch:{ all -> 0x00ff }
            if (r10 == 0) goto L_0x00f8
            android.app.Notification$Builder r10 = new android.app.Notification$Builder     // Catch:{ all -> 0x00ff }
            android.content.Context r1 = r8.mContext     // Catch:{ all -> 0x00ff }
            java.lang.String r2 = "imsicon_channel"
            r10.<init>(r1, r2)     // Catch:{ all -> 0x00ff }
            java.lang.String r1 = "drawable"
            java.lang.String r2 = "stat_notify_wfc_warning"
            int r1 = r8.getResourceIdByName(r1, r2)     // Catch:{ all -> 0x00ff }
            r10.setSmallIcon(r1)     // Catch:{ all -> 0x00ff }
            java.lang.String[] r1 = mVowifiOperatorLabel     // Catch:{ all -> 0x00ff }
            r1 = r1[r9]     // Catch:{ all -> 0x00ff }
            r10.setContentTitle(r1)     // Catch:{ all -> 0x00ff }
            r1 = 0
            android.app.Notification$Builder r1 = r10.setWhen(r1)     // Catch:{ all -> 0x00ff }
            r1.setShowWhen(r5)     // Catch:{ all -> 0x00ff }
            r10.setAutoCancel(r5)     // Catch:{ all -> 0x00ff }
            java.lang.String[] r1 = mWifiSubTextOnLockScreen     // Catch:{ all -> 0x00ff }
            r1 = r1[r9]     // Catch:{ all -> 0x00ff }
            boolean r1 = android.text.TextUtils.isEmpty(r1)     // Catch:{ all -> 0x00ff }
            if (r1 != 0) goto L_0x00e5
            java.lang.String r1 = "string"
            java.lang.String[] r2 = mWifiSubTextOnLockScreen     // Catch:{ all -> 0x00ff }
            r2 = r2[r9]     // Catch:{ all -> 0x00ff }
            int r1 = r8.getResourceIdByName(r1, r2)     // Catch:{ all -> 0x00ff }
            android.content.Context r2 = r8.mContext     // Catch:{ all -> 0x00ff }
            android.content.res.Resources r2 = r2.getResources()     // Catch:{ all -> 0x00ff }
            java.lang.String r1 = r2.getString(r1)     // Catch:{ all -> 0x00ff }
            r10.setContentText(r1)     // Catch:{ all -> 0x00ff }
            android.app.Notification$BigTextStyle r2 = new android.app.Notification$BigTextStyle     // Catch:{ all -> 0x00ff }
            r2.<init>()     // Catch:{ all -> 0x00ff }
            android.app.Notification$BigTextStyle r1 = r2.bigText(r1)     // Catch:{ all -> 0x00ff }
            r10.setStyle(r1)     // Catch:{ all -> 0x00ff }
        L_0x00e5:
            int[] r1 = mVowifiOperatorLabelOngoing     // Catch:{ all -> 0x00ff }
            r9 = r1[r9]     // Catch:{ all -> 0x00ff }
            if (r9 != r0) goto L_0x00ee
            r10.setOngoing(r0)     // Catch:{ all -> 0x00ff }
        L_0x00ee:
            android.app.Notification r9 = r10.build()     // Catch:{ all -> 0x00ff }
            android.app.NotificationManager r10 = r8.mNotificationManager     // Catch:{ all -> 0x00ff }
            r10.notify(r3, r4, r9)     // Catch:{ all -> 0x00ff }
            goto L_0x00fd
        L_0x00f8:
            android.app.NotificationManager r9 = r8.mNotificationManager     // Catch:{ all -> 0x00ff }
            r9.cancel(r3, r4)     // Catch:{ all -> 0x00ff }
        L_0x00fd:
            monitor-exit(r8)
            return
        L_0x00ff:
            r9 = move-exception
            monitor-exit(r8)
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.ImsIconManager.showWifiRegistrationStateQuickPanel(int, boolean):void");
    }

    /* access modifiers changed from: protected */
    public void setIconSlot(String str, String str2, String str3) {
        int resourceIdByName = getResourceIdByName("drawable", str2);
        boolean z = true;
        if (this.VOLTE_ICON_SLOT.equalsIgnoreCase(str)) {
            if (this.mLastVoLTEResourceId != resourceIdByName) {
                this.mLastVoLTEResourceId = resourceIdByName;
            } else {
                z = false;
            }
        } else if (RCS_ICON_SLOT.equalsIgnoreCase(str)) {
            str3 = RCS_ICON_DESCRIPTION;
        } else {
            IMSLog.e(LOG_TAG, this.mPhoneId, "Wrong slot name: " + str);
            return;
        }
        if (z || this.mForceRefreshIcon) {
            try {
                ((StatusBarManager) this.mContext.getSystemService("statusbar")).setIcon(str, resourceIdByName, 0, str3);
                IMSLog.i(LOG_TAG, this.mPhoneId, "setIconSlot: " + str2 + " (id: " + resourceIdByName + ")");
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setIconVisibility(String str, IconVisibility iconVisibility) {
        boolean z;
        boolean z2 = false;
        if (this.VOLTE_ICON_SLOT.equalsIgnoreCase(str)) {
            if (this.mLastVoLTEVisiblity != iconVisibility) {
                this.mLastVoLTEVisiblity = iconVisibility;
                z = true;
            } else {
                z = false;
            }
        } else if (RCS_ICON_SLOT.equalsIgnoreCase(str)) {
            if (this.mLastRcsVisiblity != iconVisibility) {
                this.mLastRcsVisiblity = iconVisibility;
                z = true;
            } else {
                z = false;
            }
            if (!z && iconVisibility != IconVisibility.SHOW && this.mForceRefreshIcon) {
                IMSLog.e(LOG_TAG, this.mPhoneId, "RCS not registered on this SIM. Skip refresh.");
                return;
            }
        } else {
            IMSLog.e(LOG_TAG, this.mPhoneId, "Wrong slot name: " + str);
            return;
        }
        if (z || this.mForceRefreshIcon) {
            try {
                StatusBarManager statusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
                IconVisibility iconVisibility2 = IconVisibility.SHOW;
                if (iconVisibility == iconVisibility2) {
                    z2 = true;
                }
                statusBarManager.setIconVisibility(str, z2);
                if (this.VOLTE_ICON_SLOT.equalsIgnoreCase(str)) {
                    if (!this.mIsFirstVoLTEIconShown && iconVisibility == iconVisibility2) {
                        this.mIsFirstVoLTEIconShown = true;
                        IMSLog.e(LOG_TAG, this.mPhoneId, "!@Boot: " + "setIconVisibility: " + str + ": [" + iconVisibility + "]");
                    }
                }
                IMSLog.i(LOG_TAG, this.mPhoneId, "setIconVisibility: " + str + ": [" + iconVisibility + "]");
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getResourceIdByName(String str, String str2) {
        return this.mContext.getResources().getIdentifier(str2, str, this.mPackageName);
    }

    /* access modifiers changed from: package-private */
    public boolean checkKORVolteIcon() {
        boolean z;
        ImsRegistration[] registrationInfoByPhoneId = this.mRegistrationManager.getRegistrationInfoByPhoneId(this.mPhoneId);
        if (registrationInfoByPhoneId != null) {
            z = false;
            for (ImsRegistration imsRegistration : registrationInfoByPhoneId) {
                if (imsRegistration.hasService("mmtel") && imsRegistration.getImsProfile().getCmcType() == 0 && NetworkUtil.is3gppPsVoiceNetwork(this.mCurrentNetworkType)) {
                    z = true;
                }
            }
        } else {
            z = false;
        }
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (simManagerFromSimSlot == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIcon : SimManager is null");
            return false;
        }
        int simState = this.mTelephonyManager.getSimState(simManagerFromSimSlot.getSimSlotIndex());
        if (simState == 0 || simState == 1) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIcon : SIM state is unknown or absent");
            return false;
        } else if (this.mCurrentNetworkType == 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIcon : network is unknown.");
            return false;
        } else if (!"oversea".equals(this.mTelephonyManager.semGetTelephonyProperty(this.mPhoneId, ImsConstants.SystemProperties.CURRENT_PLMN, ""))) {
            return checkKORVolteIconOperatorSpecifics(z);
        } else {
            if (this.mMno == Mno.LGU) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIcon : on roaming. Hide VoLTE icon");
                return false;
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIcon : on roaming. Volte featuremask = " + z);
            return z;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkKORVolteIconOperatorSpecifics(boolean z) {
        int i;
        boolean z2;
        int subId = SimUtil.getSubId(this.mPhoneId);
        if (subId < 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIconOperatorSpecifics : subId is invalid, return false");
            return false;
        }
        int serviceStateForSubscriber = this.mTelephonyManager.getServiceStateForSubscriber(subId);
        if (!OmcCode.isKTTOmcCode() || this.mMno != Mno.KT) {
            Mno mno = this.mMno;
            if (mno == Mno.LGU) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIconOperatorSpecifics : SIM card is LGT, return false");
                return false;
            } else if (!mno.isKor()) {
                return false;
            } else {
                if (this.mMno == Mno.SKT) {
                    z2 = this.mIsSilentRedialInProgress;
                    if (OmcCode.isSKTOmcCode()) {
                        try {
                            i = Settings.System.getInt(this.mContext.getContentResolver(), "volte_noti_settings");
                        } catch (Settings.SettingNotFoundException unused) {
                            IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIconOperatorSpecifics : volte_noti_settings is not exists");
                            i = 0;
                        }
                        IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIconOperatorSpecifics : volte_noti_settings = " + i + ", isVolteFeatureEnabled = " + z + ", isHide = " + z2 + ", ServiceState = " + serviceStateForSubscriber);
                        if (i == 1 || !z || z2 || serviceStateForSubscriber != 0) {
                            return false;
                        }
                        return true;
                    }
                } else {
                    z2 = false;
                }
                i = 1;
                IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIconOperatorSpecifics : volte_noti_settings = " + i + ", isVolteFeatureEnabled = " + z + ", isHide = " + z2 + ", ServiceState = " + serviceStateForSubscriber);
                return i == 1 ? false : false;
            }
        } else {
            int i2 = -1;
            int voiceCallType = ImsConstants.SystemSettings.getVoiceCallType(this.mContext, -1, this.mPhoneId);
            if (voiceCallType != -1) {
                i2 = voiceCallType;
            } else if (Extensions.UserHandle.myUserId() != 0) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIconOperatorSpecifics : Settings not found, return VOLTE_PREFERRED");
                i2 = 0;
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIconOperatorSpecifics : Settings not found");
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIconOperatorSpecifics : KT device and KT sim, ServiceState = " + serviceStateForSubscriber + ", voicecall_type = " + i2);
            if (serviceStateForSubscriber != 0) {
                return false;
            }
            if (i2 == 0 || i2 == 2) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public String getCrossSimIconName() {
        String[] strArr = CROSS_SIM_ICON_NAME;
        int i = this.mPhoneId;
        String str = strArr[i];
        String str2 = LOG_TAG;
        IMSLog.i(str2, i, "getCrossSimIconName() - " + str);
        return str;
    }

    /* access modifiers changed from: package-private */
    public String getVowifiIconName() {
        return ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.Registration.VOWIFI_ICON, "");
    }

    /* access modifiers changed from: package-private */
    public String getVo5gIconName() {
        return ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.Registration.VO5G_ICON, "");
    }

    /* access modifiers changed from: package-private */
    public String getVolteIconName() {
        if (!OmcCode.isKOROmcCode() || !this.mMno.isKor()) {
            String string = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.Registration.VOLTE_ICON, "");
            return !TextUtils.isEmpty(string) ? string : DEFAULT_VOLTE_REGI_ICON_ID;
        } else if (OmcCode.isKorOpenOmcCode()) {
            return "stat_sys_phone_call";
        } else {
            if (OmcCode.isSKTOmcCode()) {
                return CMC_SD_ICON;
            }
            return OmcCode.isKTTOmcCode() ? "stat_sys_phone_call_kt" : "stat_sys_phone_call_lgt";
        }
    }

    /* access modifiers changed from: package-private */
    public String getDualIMSIconName(Icon icon) {
        String str;
        int i = this.VOLTE_ICON_SLOT.equals(VOLTE_ICON_SLOT_HEAD) ? 1 : 2;
        if (!this.mUseDualVolteIcon) {
            return DEFAULT_VOLTE_REGI_ICON_ID;
        }
        int i2 = AnonymousClass4.$SwitchMap$com$sec$internal$ims$core$ImsIconManager$Icon[icon.ordinal()];
        String str2 = "";
        if (i2 == 1) {
            str = GlobalSettingsConstants.Registration.VOLTE_ICON + i;
        } else if (i2 == 2) {
            str = GlobalSettingsConstants.Registration.VOWIFI_ICON + i;
        } else if (i2 != 3) {
            str = str2;
        } else {
            str = GlobalSettingsConstants.Registration.VO5G_ICON + i;
        }
        if (!str.isEmpty()) {
            str2 = ImsRegistry.getString(this.mPhoneId, str, str2);
        }
        if (!OmcCode.isKOROmcCode() || !this.mMno.isKor()) {
            return str2;
        }
        if (OmcCode.isKorOpenOmcCode()) {
            return "stat_sys_phone_call";
        }
        if (OmcCode.isSKTOmcCode()) {
            return CMC_SD_ICON;
        }
        return OmcCode.isKTTOmcCode() ? "stat_sys_phone_call_kt" : "stat_sys_phone_call_lgt";
    }

    /* renamed from: com.sec.internal.ims.core.ImsIconManager$4  reason: invalid class name */
    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$core$ImsIconManager$Icon;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.sec.internal.ims.core.ImsIconManager$Icon[] r0 = com.sec.internal.ims.core.ImsIconManager.Icon.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$core$ImsIconManager$Icon = r0
                com.sec.internal.ims.core.ImsIconManager$Icon r1 = com.sec.internal.ims.core.ImsIconManager.Icon.VOLTE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$ImsIconManager$Icon     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.core.ImsIconManager$Icon r1 = com.sec.internal.ims.core.ImsIconManager.Icon.VOWIFI     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$core$ImsIconManager$Icon     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.core.ImsIconManager$Icon r1 = com.sec.internal.ims.core.ImsIconManager.Icon.VO5G     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.ImsIconManager.AnonymousClass4.<clinit>():void");
        }
    }

    public void registerPhoneStateListener() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "registerPhoneStateListener:");
        if (SimUtil.getSubId(this.mPhoneId) >= 0) {
            this.mTelephonyManager.registerTelephonyCallbackForSlot(this.mPhoneId, this.mContext.getMainExecutor(), this.mTelephonyCallback);
        }
    }

    public void setCurrentNetworkType(int i) {
        this.mCurrentNetworkType = i;
    }

    public void setCurrentServiceState(int i) {
        this.mCurrentServiceState = i;
    }

    public void setCurrentVoiceRatType(int i) {
        this.mCurrentVoiceRatType = i;
    }

    public void setCurrentRoamingState(boolean z) {
        this.mCurrentInRoaming = z;
    }

    /* access modifiers changed from: package-private */
    public boolean showDualVolteIcon() {
        boolean isDualIMS = SimUtil.isDualIMS();
        int activeSimCount = SimUtil.getActiveSimCount(this.mContext);
        boolean equals = TextUtils.equals("tsds2", SemSystemProperties.get("persist.ril.esim.slotswitch", ""));
        boolean z = SemFloatingFeature.getInstance().getBoolean(SecFeature.FLOATING.SUPPORT_EMBEDDED_SIM);
        boolean z2 = SemCscFeature.getInstance().getBoolean(SecFeature.CSC.TAG_CSCFEATURE_RIL_SUPPORTESIM, false);
        boolean hasSystemFeature = this.mContext.getApplicationContext().getPackageManager().hasSystemFeature("android.hardware.telephony.euicc");
        String str = LOG_TAG;
        IMSLog.i(str, "supportDualVolte: " + isDualIMS + ", configESimSlotSwitch: " + equals + ", ESim Features - floating: " + z + ", csc: " + z2 + ", eUicc: " + hasSystemFeature + ", activeSimCount: " + activeSimCount);
        if (!isDualIMS || activeSimCount < 2) {
            return false;
        }
        if (!z || equals || z2 || hasSystemFeature) {
            return true;
        }
        return false;
    }

    public void setDuringEmergencyCall(boolean z) {
        if (this.mMno.isOneOf(Mno.VODAFONE_AUSTRALIA, Mno.APT)) {
            this.mIsDuringEmergencyCall = z;
            updateRegistrationIcon();
        }
    }

    public void setVo5gIcon(int i) {
        boolean z = true;
        if (i != 1) {
            z = false;
        }
        this.mIsVonrEnabled = z;
        updateRegistrationIcon();
    }

    public boolean getDuringEmergencyCall() {
        return this.mIsDuringEmergencyCall;
    }

    /* access modifiers changed from: package-private */
    public boolean needShowRcsIcon(int i) {
        if (i == SimUtil.getActiveDataPhoneId() || !isCounterSlotRcsTransferable()) {
            return false;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "needShowRcsIcon: true");
        return true;
    }
}
