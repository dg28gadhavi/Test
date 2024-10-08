package com.sec.internal.ims.core;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.emergencymode.SemEmergencyManager;
import com.sec.ims.extensions.SemEmergencyConstantsExt;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DelayedMessage;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.CellIdentityWrapper;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.helper.os.SystemUtil;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.SemTelephonyAdapter;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

public class RegistrationGovernorBase extends RegistrationGovernor {
    protected static final long DEFAULT_TIMS_ESTABLISH_TIMER_MS = 120000;
    protected static final int DELAY_RESTORE_SETTING_TIMER = 1000;
    private static final String LOG_TAG = "RegiGvnBase";
    protected IConfigModule mConfigModule;
    protected Context mContext;
    protected ScheduledFuture<?> mDelayedVolteOffFuture;
    protected final boolean mIsVoLteMenuRemoved;
    protected Mno mMno;
    protected BroadcastReceiver mPackageDataClearedIntentReceiver;
    protected PdnController mPdnController;
    protected RegistrationManagerHandler mRegHandler;
    protected RegistrationManagerInternal mRegMan;
    protected String mSamsungMsgPackage = "";
    protected RegisterTask mTask;
    protected ITelephonyManager mTelephonyManager = null;
    protected final BroadcastReceiver mUpsmEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(RegistrationGovernorBase.LOG_TAG, "Received UpsmEvent: " + intent.getAction() + " extra: " + intent.getExtras());
            RegistrationGovernorBase.this.onUltraPowerSavingModeChanged();
        }
    };
    protected IVolteServiceModule mVsm = null;

    public RegistrationGovernorBase(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        this.mContext = context;
        this.mRegMan = registrationManagerInternal;
        this.mPdnController = pdnController;
        this.mTask = registerTask;
        this.mPhoneId = registerTask.getPhoneId();
        final ImsProfile profile = registerTask.getProfile();
        this.mMno = Mno.fromName(profile.getMnoName());
        this.mRegBaseTimeMs = ((long) profile.getRegRetryBaseTime()) * 1000;
        this.mRegMaxTimeMs = ((long) profile.getRegRetryMaxTime()) * 1000;
        this.mTelephonyManager = iTelephonyManager;
        this.mVsm = iVolteServiceModule;
        this.mConfigModule = iConfigModule;
        if (this.mRegMan != null) {
            this.mRegHandler = registrationManagerInternal.getRegistrationManagerHandler();
        }
        this.mSamsungMsgPackage = PackageUtils.getMsgAppPkgName(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        intentFilter.addDataScheme("package");
        this.mPackageDataClearedIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Uri data = intent.getData();
                int i = RegistrationGovernorBase.this.mPhoneId;
                IMSLog.s(RegistrationGovernorBase.LOG_TAG, i, "onReceive:" + intent.getAction() + " mTask:" + profile.getName() + "(" + RegistrationGovernorBase.this.mTask.getState() + ")");
                if ("android.intent.action.PACKAGE_DATA_CLEARED".equals(action) && data != null) {
                    RegistrationGovernorBase.this.onPackageDataCleared(data);
                }
            }
        };
        this.mIsVoLteMenuRemoved = DeviceUtil.removeVolteMenuWithSimMobility(this.mPhoneId);
        this.mContext.registerReceiver(this.mPackageDataClearedIntentReceiver, intentFilter);
    }

    public void clear() {
        this.mContext.unregisterReceiver(this.mPackageDataClearedIntentReceiver);
    }

    public void onPackageDataCleared(Uri uri) {
        this.mRegMan.getEventLog().logAndAdd("onReceive: ACTION_PACKAGE_DATA_CLEARED is received");
        String schemeSpecificPart = uri.getSchemeSpecificPart();
        int i = this.mPhoneId;
        IMSLog.s(LOG_TAG, i, "Intent received is packageName: " + schemeSpecificPart + ", mSamsungMsgPackage: " + this.mSamsungMsgPackage);
        if (!TextUtils.isEmpty(schemeSpecificPart) && !TextUtils.isEmpty(this.mSamsungMsgPackage) && TextUtils.equals(schemeSpecificPart, this.mSamsungMsgPackage)) {
            String acsServerType = ConfigUtil.getAcsServerType(this.mTask.getPhoneId());
            if (((this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING || this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) && this.mMno == Mno.ATT) || this.mMno == Mno.VZW || (this.mTask.isRcsOnly() && ImsConstants.RCS_AS.JIBE.equalsIgnoreCase(acsServerType))) {
                new Handler().postDelayed(new RegistrationGovernorBase$$ExternalSyntheticLambda14(this), 1000);
            } else if (this.mMno.isKor() && this.mTask.isRcsOnly()) {
                setBotAgreementToFile(0);
                this.mRegHandler.notifyChatbotAgreementChanged(this.mTask.getPhoneId());
                DmConfigHelper.setImsUserSetting(this.mContext, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName(), ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mTask.getPhoneId()), this.mTask.getPhoneId());
            } else if (this.mTask.isRcsOnly() && ConfigUtil.isRcsChn(this.mMno)) {
                int phoneCount = SimUtil.getPhoneCount();
                int i2 = this.mPhoneId;
                IMSLog.s(LOG_TAG, i2, "Turn off RCS for block data connection alert. phoneCount: " + phoneCount);
                if (phoneCount == 1) {
                    ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, 0, this.mTask.getPhoneId());
                } else if (phoneCount == 2) {
                    ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, 0, 0);
                    ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, 0, 1);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onPackageDataCleared$0() {
        Context context = this.mContext;
        ImsConstants.SystemSettings.setRcsUserSetting(context, DmConfigHelper.getImsUserSetting(context, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName(), this.mTask.getPhoneId()), this.mTask.getPhoneId());
    }

    /* access modifiers changed from: protected */
    public void setBotAgreementToFile(int i) {
        IMSLog.s(LOG_TAG, this.mPhoneId, "setBotAgreementToFile : " + i);
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        String str = "IMSI_";
        if (simManagerFromSimSlot != null) {
            str = str + simManagerFromSimSlot.getImsi();
        }
        ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, "bot_agreement_from_app", str, i == 1 ? "1" : "0");
    }

    public void onRegistrationTerminated(SipError sipError, long j, boolean z) {
        this.mRegMan.getEventLog().logAndAdd("onRegistrationTerminated: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j);
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        if (j < 0) {
            j = 0;
        }
        if (SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(sipError)) {
            j = 1000;
        } else if (SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(sipError)) {
            j = 128000;
        }
        this.mThrottleReason = 0;
        startRetryTimer(j);
    }

    /* access modifiers changed from: protected */
    public void handleForbiddenError(long j) {
        String regRetryPcscfPolicyOn403 = this.mTask.getProfile().getRegRetryPcscfPolicyOn403();
        if (RegistrationGovernor.RETRY_TO_NEXT_PCSCF.equals(regRetryPcscfPolicyOn403)) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: Retry to next PCSCF address in case 403 Forbidden");
        } else if (RegistrationGovernor.RETRY_TO_SAME_PCSCF.equals(regRetryPcscfPolicyOn403)) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: Retry to same PCSCF address in case 403 Forbidden");
            this.mCurPcscfIpIdx--;
        } else {
            Log.e(LOG_TAG, "onRegistrationError: Permanently prohibited.");
            this.mIsPermanentStopped = true;
            if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                resetPcscfList();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleTimeoutError(long j) {
        if (this.mCurPcscfIpIdx == this.mNumOfPcscfIp && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            resetPcscfList();
        }
    }

    /* access modifiers changed from: protected */
    public void handlePcscfError() {
        this.mTask.setKeepPdn(false);
        startRetryTimer(getWaitTime());
        this.mThrottleReason = 0;
    }

    /* access modifiers changed from: protected */
    public void handleRetryTimer(long j) {
        if (this.mCurPcscfIpIdx >= this.mNumOfPcscfIp) {
            this.mCurPcscfIpIdx = 0;
        }
        if (j == 0) {
            j = getWaitTime();
        }
        startRetryTimer(j);
        this.mThrottleReason = 0;
    }

    public void onRegistrationError(SipError sipError, long j, boolean z) {
        this.mRegMan.getEventLog().logAndAdd("onRegistrationError: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (j < 0) {
            j = 0;
        }
        this.mFailureCounter++;
        this.mCurPcscfIpIdx++;
        if (!this.mTask.getProfile().hasEmergencySupport() || !SipErrorBase.SIP_TIMEOUT.equals(sipError) || this.mTask.getProfile().getE911RegiTime() <= 0) {
            if (SipErrorBase.isImsForbiddenError(sipError)) {
                handleForbiddenError(j);
                if (this.mIsPermanentStopped) {
                    return;
                }
            } else if (SipErrorBase.SIP_TIMEOUT.equals(sipError)) {
                handleTimeoutError(j);
            } else if (SipErrorBase.EMPTY_PCSCF.equals(sipError)) {
                handlePcscfError();
                return;
            } else if (SipErrorBase.SERVICE_UNAVAILABLE.equals(sipError) && j != 0 && j < ((long) this.mTask.getProfile().getTimerF())) {
                this.mCurPcscfIpIdx--;
            }
            handleRetryTimer(j);
            return;
        }
        handleTimeOutEmerRegiError();
    }

    public void onRegistrationDone() {
        int i = this.mPhoneId;
        IMSLog.s(LOG_TAG, i, "onRegistrationDone: state " + this.mTask.getState());
        this.mFailureCounter = 0;
        this.mRegiAt = 0;
        this.mThrottleReason = 0;
        stopRetryTimer();
    }

    public void onTimsTimerExpired() {
        stopTimsEstablishTimer(this.mTask, RegistrationConstants.REASON_TIMS_EXPIRED);
        this.mTask.setNotAvailableReason(1);
        this.mRegMan.notifyImsNotAvailable(this.mTask, true);
    }

    /* access modifiers changed from: protected */
    public void removeCurrentPcscfAndInitialRegister(boolean z) {
        String currentPcscfIp = getCurrentPcscfIp();
        this.mPcscfIpList.remove(currentPcscfIp);
        this.mNumOfPcscfIp--;
        updatePcscfIpList(this.mPcscfIpList, z);
        IMSLog.s(LOG_TAG, this.mPhoneId, "removeCurrentPcscfAndInitialRegister(): curPcscfIp " + currentPcscfIp + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mCurPcscfIpIndex " + this.mCurPcscfIpIdx + " mPcscfIpList " + this.mPcscfIpList);
    }

    /* access modifiers changed from: protected */
    public void handleAlternativeCallState() {
        if (this.mHandlePcscfOnAlternativeCall) {
            int i = this.mCurPcscfIpIdx + 1;
            this.mCurPcscfIpIdx = i;
            if (i >= this.mNumOfPcscfIp) {
                this.mCurPcscfIpIdx = 0;
            }
        }
        this.mTask.setDeregiReason(7);
        this.mRegMan.deregister(this.mTask, true, true, "call state changed");
    }

    public void onCallStatus(IRegistrationGovernor.CallEvent callEvent, SipError sipError, int i) {
        Log.i(LOG_TAG, "onCallStatus: event=" + callEvent + " error=" + sipError);
        if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_ESTABLISHED) {
            this.mHasVoLteCall = true;
        } else if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END) {
            this.mHasVoLteCall = false;
        } else if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_INITIAL_REGI) {
            handleAlternativeCallState();
        }
    }

    public SipError onSipError(String str, SipError sipError) {
        Log.i(LOG_TAG, "onSipError: service=" + str + " error=" + sipError);
        if ("mmtel".equals(str) && (SipErrorBase.SIP_INVITE_TIMEOUT.equals(sipError) || SipErrorBase.SIP_TIMEOUT.equals(sipError))) {
            removeCurrentPcscfAndInitialRegister(true);
        }
        return sipError;
    }

    public int getFailureType() {
        if (this.mDiscardCurrentNetwork) {
            return 32;
        }
        return this.mIsPermanentStopped ? 33 : 16;
    }

    /* access modifiers changed from: protected */
    public void removeService(Set<String> set, String str, String str2) {
        if (set.remove(str)) {
            int i = this.mPhoneId;
            IMSLog.s(LOG_TAG, i, "remove service: " + str + "(" + str2 + ")");
            this.mTask.addFilteredReason(str, str2);
        }
    }

    public Set<String> applyDataSimPolicyForCrossSim(Set<String> set, int i) {
        if (set.isEmpty()) {
            return set;
        }
        Set<String> blockedServicesForCrossSim = SlotBasedConfig.getInstance(i == 0 ? 1 : 0).getBlockedServicesForCrossSim();
        if (blockedServicesForCrossSim.contains("all")) {
            set.clear();
        } else if (!blockedServicesForCrossSim.isEmpty()) {
            set.removeAll(blockedServicesForCrossSim);
        }
        return set;
    }

    public Set<String> applyCrossSimPolicy(Set<String> set, int i) {
        HashSet hashSet = new HashSet(set);
        if (TelephonyManagerExt.getNetworkClass(this.mRegMan.getNetworkEvent(i == 0 ? 1 : 0).network) != 1) {
            return applyDataSimPolicyForCrossSim(hashSet, i);
        }
        HashSet hashSet2 = new HashSet();
        hashSet2.add("smsip");
        hashSet.retainAll(hashSet2);
        return hashSet;
    }

    public Set<String> applyPsDataOffExempt(Set<String> set, int i) {
        if (set == null) {
            return new HashSet();
        }
        if (i != 18 && !NetworkUtil.isMobileDataOn(this.mContext)) {
            if (!this.mTask.getProfile().isMmtelVoiceExempt()) {
                removeService(set, "mmtel", "MMTEL Voice PS Data Off Exempt");
            }
            if (!this.mTask.getProfile().isMmtelVoiceExempt() || !this.mTask.getProfile().isMmtelVideoExempt()) {
                removeService(set, "mmtel-video", "MMTEL Video PS Data Off Exempt");
            }
            if (!this.mTask.getProfile().isSmsIpExempt()) {
                removeService(set, "smsip", "SMS over IP PS Data Off Exempt");
            }
        }
        return set;
    }

    public Set<String> filterService(Set<String> set, int i) {
        Set<String> hashSet = new HashSet<>();
        HashSet hashSet2 = new HashSet(set);
        int i2 = 0;
        boolean z = true;
        boolean z2 = DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) == 1;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) != 1) {
            z = false;
        }
        if (isImsDisabled()) {
            return new HashSet();
        }
        if ((this.mTask.getProfile().getPdnType() == -1 || this.mTask.getProfile().getPdnType() == 0) && !NetworkUtil.isMobileDataOn(this.mContext) && i != 18) {
            Log.i(LOG_TAG, "filterService: Mobile data off");
            return new HashSet();
        }
        if (z2) {
            Set<String> servicesByImsSwitch = servicesByImsSwitch(ImsProfile.getVoLteServiceList());
            if (!servicesByImsSwitch.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
            }
            hashSet.addAll(servicesByReadSwitch((String[]) servicesByImsSwitch.toArray(new String[0])));
            if (servicesByImsSwitch.contains("mmtel") && !hashSet.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_DM_OFF.getCode());
            }
        }
        if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
            hashSet.addAll(servicesByReadSwitch((String[]) servicesByImsSwitch(ImsProfile.getRcsServiceList()).toArray(new String[0])));
        }
        if (NetworkUtil.is3gppPsVoiceNetwork(i) && this.mTask.getProfile().getPdnType() == 11) {
            hashSet = applyVoPsPolicy(hashSet);
            if (hashSet.isEmpty()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                return hashSet;
            }
        }
        if (!isVideoCallEnabled()) {
            removeService(hashSet2, "mmtel-video", "Videocall disabled.");
        }
        if (this.mMno.isUSA() && !ImsUtil.isDualVideoCallAllowed(this.mPhoneId)) {
            removeService(hashSet2, "mmtel-video", "Non-DDS operator SIM");
        }
        if (this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming && this.mTask.getProfile().getPdnType() == 11 && i != 18 && allowRoaming() && this.mTask.getProfile().getMediaTypeRestrictionPolicy().equalsIgnoreCase("Voice_Only")) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Support Voice Only when roaming. just filtering mmtel-video.");
            removeService(hashSet2, "mmtel-video", "voice only when roaming");
        }
        if (!z) {
            if (ImsUtil.isSingleRegiAppConnected(this.mPhoneId)) {
                Arrays.stream(ImsProfile.getRcsServiceList()).filter(new RegistrationGovernorBase$$ExternalSyntheticLambda0(SecImsNotifier.getInstance().getSipDelegateServiceList(this.mPhoneId))).forEach(new RegistrationGovernorBase$$ExternalSyntheticLambda1(this, hashSet2));
            } else if (this.mTask.isRcsOnly()) {
                String[] rcsServiceList = ImsProfile.getRcsServiceList();
                int length = rcsServiceList.length;
                while (i2 < length) {
                    removeService(hashSet2, rcsServiceList[i2], "DefaultAppInUse is false");
                    i2++;
                }
            } else {
                String[] chatServiceList = ImsProfile.getChatServiceList();
                int length2 = chatServiceList.length;
                while (i2 < length2) {
                    removeService(hashSet2, chatServiceList[i2], "DefaultAppInUse is false");
                    i2++;
                }
            }
        }
        if (this.mTask.getProfile().getPdnType() == 11) {
            hashSet = applyPsDataOffExempt(hashSet, i);
        }
        if (!hashSet2.isEmpty()) {
            hashSet2.retainAll(hashSet);
        }
        return hashSet2;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$filterService$1(List list, String str) {
        return !list.contains(str);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$filterService$2(Set set, String str) {
        removeService(set, str, "Disable from singleregi");
    }

    /* access modifiers changed from: protected */
    public boolean isImsDisabled() {
        boolean z = true;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, this.mPhoneId) == 1) {
            z = false;
        }
        if (z) {
            Log.i(LOG_TAG, "filterService: IMS is disabled.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF.getCode());
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public Set<String> servicesByImsSwitch(String[] strArr) {
        HashSet hashSet = new HashSet();
        ContentValues imsSwitchValue = DmConfigHelper.getImsSwitchValue(this.mContext, strArr, this.mPhoneId);
        for (String str : strArr) {
            if (imsSwitchValue.getAsInteger(str) != null && imsSwitchValue.getAsInteger(str).intValue() == 1) {
                hashSet.add(str);
            }
        }
        return hashSet;
    }

    /* access modifiers changed from: protected */
    public Set<String> servicesByReadSwitch(String[] strArr) {
        HashSet hashSet = new HashSet();
        for (String str : strArr) {
            if (DmConfigHelper.readSwitch(this.mContext, str, true, this.mPhoneId)) {
                hashSet.add(str);
            }
        }
        return hashSet;
    }

    /* access modifiers changed from: protected */
    public Set<String> applyVoPsPolicy(Set<String> set) {
        if (set == null) {
            return new HashSet();
        }
        if (this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
            if (this.mTask.getProfile().getSmsoipUsagePolicy().equalsIgnoreCase("Irrespective_of_voice")) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "SMSoIP can be used even if VoPS not supported. just filtering mmtel, mmtel-video.");
                removeService(set, "mmtel-video", "VoPS Off");
                removeService(set, "mmtel", "VoPS Off");
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "by VoPS policy: remove all service");
                return new HashSet();
            }
        }
        return set;
    }

    /* access modifiers changed from: protected */
    public List<String> addIpv6Addr(List<String> list, List<String> list2, LinkPropertiesWrapper linkPropertiesWrapper) {
        if (linkPropertiesWrapper.hasGlobalIPv6Address() || linkPropertiesWrapper.hasIPv6DefaultRoute()) {
            addIpv6Addr(list, list2);
        }
        return list2;
    }

    private void addIpv6Addr(List<String> list, List<String> list2) {
        for (String next : list) {
            if (NetworkUtil.isIPv6Address(next)) {
                list2.add(next);
            }
        }
    }

    /* access modifiers changed from: protected */
    public List<String> addIpv4Addr(List<String> list, List<String> list2, LinkPropertiesWrapper linkPropertiesWrapper) {
        if (list2.isEmpty() && linkPropertiesWrapper.hasIPv4Address()) {
            Log.i(LOG_TAG, "ipv4 address found");
            addIpv4Addr(list, list2);
        }
        return list2;
    }

    private void addIpv4Addr(List<String> list, List<String> list2) {
        for (String next : list) {
            if (NetworkUtil.isIPv4Address(next)) {
                list2.add(next);
            }
        }
    }

    public List<String> checkValidPcscfIp(List<String> list) {
        ArrayList arrayList = new ArrayList();
        LinkPropertiesWrapper linkProperties = this.mPdnController.getLinkProperties(this.mTask);
        if (list == null || list.isEmpty() || linkProperties == null) {
            return arrayList;
        }
        List<String> addIpv4Addr = addIpv4Addr(list, addIpv6Addr(list, arrayList, linkProperties), linkProperties);
        Log.i(LOG_TAG, "ValidPcscfIp: " + addIpv4Addr);
        return addIpv4Addr;
    }

    public List<String> checkValidPcscfIpForPcscfRestoration(List<String> list) {
        ArrayList arrayList = new ArrayList();
        if (list != null && !list.isEmpty()) {
            addIpv6Addr(list, arrayList);
            if (arrayList.isEmpty()) {
                addIpv4Addr(list, arrayList);
            }
            Log.i(LOG_TAG, "ValidPcscfIp: " + arrayList);
        }
        return arrayList;
    }

    public void updatePcscfIpList(List<String> list) {
        updatePcscfIpList(list, false);
    }

    /* access modifiers changed from: protected */
    public void updatePcscfIpList(List<String> list, boolean z) {
        if (list == null) {
            Log.e(LOG_TAG, "updatePcscfIpList: null P-CSCF list!");
            return;
        }
        String currentPcscfIp = getCurrentPcscfIp();
        int size = list.size();
        this.mNumOfPcscfIp = size;
        this.mPcscfIpList = list;
        this.mIsValid = size > 0;
        int indexOf = list.indexOf(currentPcscfIp);
        if (indexOf >= 0) {
            RegisterTask registerTask = this.mTask;
            RegistrationConstants.RegisterTaskState registerTaskState = RegistrationConstants.RegisterTaskState.REGISTERING;
            RegistrationConstants.RegisterTaskState registerTaskState2 = RegistrationConstants.RegisterTaskState.REGISTERED;
            if (registerTask.isOneOf(registerTaskState, registerTaskState2)) {
                Log.i(LOG_TAG, "updatePcscfIpList: keeping " + currentPcscfIp + " as current forceInitialRegi=" + z + " mMoveToNextPcscfAfterTimerB=" + this.mMoveToNextPcscfAfterTimerB);
                this.mCurPcscfIpIdx = indexOf;
                if (z) {
                    if (this.mMno.isKor()) {
                        if (this.mMoveToNextPcscfAfterTimerB) {
                            this.mFailureCounter = 0;
                            this.mCurImpu = 0;
                            this.mRegiAt = 0;
                            this.mThrottleReason = 0;
                        } else {
                            resetRetry();
                        }
                        this.mMoveToNextPcscfAfterTimerB = false;
                    }
                    if (this.mTask.isOneOf(registerTaskState, registerTaskState2)) {
                        this.mTask.setDeregiReason(8);
                        this.mRegMan.deregister(this.mTask, true, this.mIsValid, "pcscf updated");
                        return;
                    }
                    return;
                } else if (this.mMno == Mno.VZW) {
                    this.mRegMan.sendReRegister(this.mTask);
                    return;
                } else {
                    return;
                }
            }
        }
        Log.i(LOG_TAG, "updatePcscfIpList: whole new set of PCSCFs (" + this.mTask.getState() + ")");
        if (this.mMno.isTmobile() || this.mMno.isOrangeGPG()) {
            this.mCurPcscfIpIdx = 0;
            this.mCurImpu = 0;
            this.mRegiAt = 0;
            this.mThrottleReason = 0;
        } else {
            resetRetry();
        }
        if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
            this.mTask.setDeregiReason(8);
            this.mRegMan.deregister(this.mTask, true, this.mIsValid, "pcscf updated");
        }
    }

    public String getCurrentPcscfIp() {
        if (CollectionUtils.isNullOrEmpty((Collection<?>) this.mPcscfIpList)) {
            Log.e(LOG_TAG, "getNextPcscf: empty P-CSCF list.");
            return "";
        }
        List<String> list = this.mPcscfIpList;
        String str = list.get(this.mCurPcscfIpIdx % list.size());
        Log.i(LOG_TAG, "getCurrentPcscfIp: " + str);
        return str;
    }

    public void resetRetry() {
        Log.i(LOG_TAG, "resetRetry()");
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        this.mCurImpu = 0;
        this.mRegiAt = 0;
        this.mThrottleReason = 0;
        resetIPSecAllow();
    }

    /* access modifiers changed from: protected */
    public long getWaitTime() {
        long pow = this.mRegBaseTimeMs * ((long) Math.pow(2.0d, (double) (this.mFailureCounter - 1)));
        if (pow < 0) {
            return this.mRegMaxTimeMs;
        }
        return Math.min(this.mRegMaxTimeMs, pow);
    }

    public long getNextRetryMillis() {
        if (this.mIsPermanentStopped || this.mIsPermanentPdnFailed) {
            return -1;
        }
        return Math.max(0, this.mRegiAt - SystemClock.elapsedRealtime());
    }

    /* access modifiers changed from: protected */
    public void startTimsEstablishTimer(RegisterTask registerTask, long j, String str) {
        if (registerTask.getProfile().hasEmergencySupport()) {
            Log.i(LOG_TAG, "Emergecy Task doens't required Tims timer.");
        } else if (!registerTask.isRcsOnly() || !registerTask.getMno().isKor()) {
            if (this.mTimEshtablishTimeout != null) {
                Log.i(LOG_TAG, "Tims is running. don't need to start new timer.");
                return;
            }
            Log.i(LOG_TAG, "startTimsEstablishTimer: millis = " + j + ", reason = [" + str + "]");
            this.mTimEshtablishTimeoutReason = str;
            this.mTimEshtablishTimeout = this.mRegHandler.startTimsEshtablishTimer(registerTask, j);
        } else if (this.mTimEshtablishTimeoutRCS != null) {
            Log.i(LOG_TAG, "TimsRCS is running. don't need to start new timer.");
        } else {
            Log.i(LOG_TAG, "start TimsRCS timer; millis = " + j + ", reason = [" + str + "]");
            this.mTimEshtablishTimeoutRCS = this.mRegHandler.startTimsEshtablishTimer(registerTask, j);
        }
    }

    /* access modifiers changed from: protected */
    public void stopTimsEstablishTimer(RegisterTask registerTask, String str) {
        if (!registerTask.isRcsOnly() || !registerTask.getMno().isKor()) {
            Log.i(LOG_TAG, "stop Tims timer by " + str);
            DelayedMessage delayedMessage = this.mTimEshtablishTimeout;
            if (delayedMessage != null) {
                this.mRegHandler.stopTimer(delayedMessage);
                this.mTimEshtablishTimeout = null;
                this.mTimEshtablishTimeoutReason = null;
                return;
            }
            return;
        }
        Log.i(LOG_TAG, "stop TimsRCS timer by " + str);
        DelayedMessage delayedMessage2 = this.mTimEshtablishTimeoutRCS;
        if (delayedMessage2 != null) {
            this.mRegHandler.stopTimer(delayedMessage2);
            this.mTimEshtablishTimeoutRCS = null;
        }
    }

    /* access modifiers changed from: protected */
    public void toggleTimsTimerByPdnTransport(int i) {
        if (i == 1) {
            IMSLog.i(LOG_TAG, this.mPhoneId, this.mTask, "onPdnConnecting: IMS PDN connecting on cellular. Start TIMS timer");
            startTimsTimer(RegistrationConstants.REASON_IMS_PDN_REQUEST);
        } else if (i == 2) {
            IMSLog.i(LOG_TAG, this.mPhoneId, this.mTask, "onPdnConnecting: IMS PDN connecting on ePDG. Stop TIMS timer");
            stopTimsTimer(RegistrationConstants.REASON_EPDG_CONNECTING);
        }
    }

    /* access modifiers changed from: protected */
    public void startRetryTimer(long j) {
        this.mRegiAt = SystemClock.elapsedRealtime() + j;
        stopRetryTimer();
        Log.i(LOG_TAG, "startRetryTimer: millis " + j);
        this.mRetryTimeout = this.mRegHandler.startRegistrationTimer(this.mTask, j);
    }

    /* access modifiers changed from: protected */
    public void stopRetryTimer() {
        if (this.mRetryTimeout != null) {
            Log.i(LOG_TAG, "stopRetryTimer; what = " + this.mRetryTimeout.what);
            this.mRegHandler.stopTimer(this.mRetryTimeout);
            this.mRetryTimeout = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkRoamingStatus(int i) {
        if (i == 18 || !this.mTelephonyManager.isNetworkRoaming(SimUtil.getSubId(this.mPhoneId)) || allowRoaming()) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: roaming is not allowed.");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ROAMING_NOT_SUPPORTED.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: call state is not idle");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkEmergencyStatus() {
        return this.mTask.getProfile().hasEmergencySupport() && this.mTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERING;
    }

    /* access modifiers changed from: protected */
    public boolean checkRegiStatus() {
        RegistrationConstants.RegisterTaskState state = this.mTask.getState();
        boolean isUpdateRegistering = this.mTask.isUpdateRegistering();
        Log.i(LOG_TAG, "checkRegiStatus: getState()=" + state + " mIsUpdateRegistering=" + isUpdateRegistering);
        return state != RegistrationConstants.RegisterTaskState.REGISTERING && !isUpdateRegistering;
    }

    /* access modifiers changed from: protected */
    public boolean checkNetworkEvent(int i) {
        if (!this.mRegHandler.hasNetworModeChangeEvent() || !this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: networkModeChangeTimer Running.");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ONGOING_NW_MODE_CHANGE.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkWFCsettings(int i) {
        if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONFIGURED) && i == 18 && this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && this.mPdnController.getEpdgPhysicalInterface(this.mPhoneId) == 1) {
            int wifiStatus = DeviceUtil.getWifiStatus(this.mContext, 0);
            boolean isEnabled = VowifiConfig.isEnabled(this.mContext, this.mPhoneId);
            if (wifiStatus == 0 || !isEnabled) {
                Log.i("RegiGvnBase[" + this.mPhoneId + "]", "VoWiFi menu is not enabled or WIFI is not enabled");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkDelayedStopPdnEvent() {
        if (!this.mRegHandler.hasDelayedStopPdnEvent() || !this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
            return true;
        }
        Log.i(LOG_TAG, "stopPdn would be called soon. Skip IMS registration");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkMdmnProfile() {
        return this.mTask.getProfile().isSamsungMdmnEnabled();
    }

    public boolean isReadyToRegister(int i) {
        if (checkEmergencyStatus() || checkMdmnProfile()) {
            return true;
        }
        if (!checkRegiStatus() || !checkEpdgEvent(i) || !checkCallStatus() || !checkRoamingStatus(i) || !checkVolteSetting(i) || !checkNetworkEvent(i) || !checkDelayedStopPdnEvent() || !checkRcsEvent(i)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int i) {
        if (this.mTask.isRcsOnly() || i == 18 || getVoiceTechType(this.mPhoneId) == 0) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: volte disabled");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return false;
    }

    public void releaseThrottle(int i) {
        if (i == 1 || i == 7) {
            this.mIsPermanentStopped = false;
        } else if (i == 4) {
            this.mIsPermanentStopped = false;
            this.mCurImpu = 0;
        }
        if (!this.mIsPermanentStopped) {
            Log.i(LOG_TAG, "releaseThrottle: case by " + i);
        }
    }

    /* access modifiers changed from: protected */
    public int getVoiceTechType() {
        int voiceCallType = ImsConstants.SystemSettings.getVoiceCallType(this.mContext, 0, this.mPhoneId);
        StringBuilder sb = new StringBuilder();
        sb.append("getVoiceTechType: ");
        sb.append(voiceCallType == 0 ? "VOLTE" : "CS");
        Log.i(LOG_TAG, sb.toString());
        return voiceCallType;
    }

    /* access modifiers changed from: protected */
    public int getVoiceTechType(int i) {
        int voiceCallType = ImsConstants.SystemSettings.getVoiceCallType(this.mContext, 0, i);
        StringBuilder sb = new StringBuilder();
        sb.append("getVoiceTechType: ");
        sb.append(voiceCallType == 0 ? "VOLTE" : "CS");
        Log.i(LOG_TAG, sb.toString());
        return voiceCallType;
    }

    /* access modifiers changed from: protected */
    public boolean isVideoCallEnabled() {
        int videoCallType = ImsConstants.SystemSettings.getVideoCallType(this.mContext, -1, this.mPhoneId);
        StringBuilder sb = new StringBuilder();
        sb.append("isVideoCallEnabled: ");
        sb.append(videoCallType == 0 ? "Enable" : "Disable");
        Log.i(LOG_TAG, sb.toString());
        return videoCallType == 0;
    }

    public boolean allowRoaming() {
        if (!this.mTask.getProfile().hasEmergencySupport()) {
            return this.mTask.getProfile().isAllowedOnRoaming();
        }
        Log.i(LOG_TAG, "allowRoaming: Emergency profile. Return true.");
        return true;
    }

    public boolean isLocationInfoLoaded(int i) {
        ImsProfile profile = this.mTask.getProfile();
        if (profile.getSupportedGeolocationPhase() == 0 || i != 18 || profile.hasEmergencySupport()) {
            return true;
        }
        IGeolocationController geolocationController = ImsRegistry.getGeolocationController();
        if (geolocationController != null) {
            if (this.mNeedToCheckLocationSetting && !geolocationController.isLocationServiceEnabled()) {
                Log.i(LOG_TAG, "locationService is disabled");
                return false;
            } else if (geolocationController.isCountryCodeLoaded(this.mPhoneId)) {
                return true;
            } else {
                geolocationController.startGeolocationUpdate(this.mPhoneId, false);
            }
        }
        return profile.isAllowedRegiWhenLocationUnavailable();
    }

    /* access modifiers changed from: protected */
    public boolean isDeregisterWithRATNeeded() {
        int currentNetworkByPhoneId = this.mRegMan.getCurrentNetworkByPhoneId(this.mPhoneId);
        boolean z = !NetworkUtil.is3gppPsVoiceNetwork(currentNetworkByPhoneId) && currentNetworkByPhoneId != 18;
        Log.i(LOG_TAG, "isDeregisterWithRATNeeded [" + z + "]");
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean isDeregisterWithVoPSNeeded() {
        boolean is3gppPsVoiceNetwork = this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED ? NetworkUtil.is3gppPsVoiceNetwork(this.mRegMan.getCurrentNetworkByPhoneId(this.mPhoneId)) : false;
        Log.i(LOG_TAG, "isDeregisterWithVoPSNeeded [" + is3gppPsVoiceNetwork + "]");
        return is3gppPsVoiceNetwork;
    }

    /* access modifiers changed from: protected */
    public void setDelayedDeregisterTimerRunning(boolean z) {
        Log.i(LOG_TAG, "setDelayedDeregisterTimerRunning [" + z + "]");
        this.mDelayedDeregisterTimerRunning = z;
        this.mRegMan.setDelayedDeregisterTimerRunning(this.mTask, z);
    }

    public void runDelayedDeregister() {
        if (isDelayedDeregisterTimerRunning()) {
            Log.i(LOG_TAG, "runDelayedDeregister : delete DelayedDeregisterTimer. mState [" + this.mTask.getState() + "]");
            setDelayedDeregisterTimerRunning(false);
            if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                this.mRegMan.addPendingUpdateRegistration(this.mTask, 0);
            } else if (this.mTask.getState() != RegistrationConstants.RegisterTaskState.CONNECTED) {
                this.mRegHandler.sendTryRegister(this.mTask.getPhoneId());
            } else if (isDeregisterWithVoPSNeeded() || isDeregisterWithRATNeeded()) {
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            } else {
                this.mRegHandler.sendTryRegister(this.mTask.getPhoneId());
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isDelayedDeregisterTimerRunningWithCallStatus() {
        if (this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0 || getCallStatus() == 0 || (!isDeregisterWithVoPSNeeded() && !isDeregisterWithRATNeeded() && !this.mRegMan.getNetworkEvent(this.mPhoneId).outOfService)) {
            Log.i("RegiGvnBase[" + this.mPhoneId + "]", "isDelayedDeregisterTimerRunning [" + this.mDelayedDeregisterTimerRunning + "]");
            return this.mDelayedDeregisterTimerRunning;
        }
        Log.i("RegiGvnBase[" + this.mPhoneId + "]", "isDelayedDeregisterTimerRunning: Timer will start soon. return true.");
        return true;
    }

    public void onTelephonyCallStatusChanged(int i) {
        setCallStatus(i);
    }

    public void onPdnRequestFailed(PdnFailReason pdnFailReason, int i) {
        this.mHasPdnFailure = true;
    }

    public String toString() {
        return "RegiGvnBase [mMno=" + this.mMno + ", mFailureCounter=" + this.mFailureCounter + ", mIsPermanentStopped=" + this.mIsPermanentStopped + ", mCurPcscfIpIdx=" + this.mCurPcscfIpIdx + ", mNumOfPcscfIp=" + this.mNumOfPcscfIp + ", mCurImpu=" + this.mCurImpu + ", mPcscfIpList=" + this.mPcscfIpList + ", mIsValid=" + this.mIsValid + ", mIPsecAllow=" + this.mIPsecAllow + ", mMoveToNextPcscfAfterTimerB=" + this.mMoveToNextPcscfAfterTimerB + ", mRegiAt=" + this.mRegiAt + ", mHasVoLteCall=" + this.mHasVoLteCall + ", mRegBaseTimeMs=" + this.mRegBaseTimeMs + ", mRegMaxTimeMs=" + this.mRegMaxTimeMs + "]";
    }

    public boolean isWiFiSettingOn() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager == null) {
            return false;
        }
        boolean isWifiEnabled = wifiManager.isWifiEnabled();
        Log.i(LOG_TAG, "WifiManager.isWifiEnabled() : " + isWifiEnabled);
        return isWifiEnabled;
    }

    public boolean isSrvccCase() {
        int i = this.mRegMan.getNetworkEvent(this.mPhoneId).network;
        if (this.mNeedToCheckSrvcc && this.mTask.getRegistrationRat() == 13 && (TelephonyManagerExt.getNetworkClass(i) == 1 || TelephonyManagerExt.getNetworkClass(i) == 2)) {
            return true;
        }
        return false;
    }

    public boolean isEcEnabled(int i) {
        return RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_POST_CALL_AUTH, i)).booleanValue() || RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_SHARED_SKETCH_AUTH, i)).booleanValue() || RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_SHARED_MAP_AUTH, i)).booleanValue();
    }

    /* access modifiers changed from: protected */
    public Set<String> applyImsSwitch(Set<String> set, int i) {
        if (set == null) {
            return new HashSet();
        }
        ISimManager simManager = this.mRegMan.getSimManager(this.mPhoneId);
        if (simManager == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "applyImsSwitch: sm is null!! retrun empty set");
            return new HashSet();
        }
        boolean isLabSimCard = simManager.isLabSimCard();
        if (!"GCF".equalsIgnoreCase(OmcCode.get()) && !isLabSimCard && this.mTask.getProfile().getPdnType() == 11) {
            ContentValues mnoInfo = simManager.getMnoInfo();
            boolean booleanValue = CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE, false);
            boolean booleanValue2 = CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI, false);
            boolean booleanValue3 = CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_SMS_IP, false);
            boolean booleanValue4 = CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VIDEO_CALL, false);
            if (!booleanValue && i != 18) {
                removeService(set, "mmtel", "VoLTE MPS false");
                removeService(set, "mmtel-video", "VoLTE MPS false");
                removeService(set, "smsip", "VoLTE MPS false");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_MPS_DISABLED.getCode());
            }
            if (!booleanValue2 && i == 18) {
                removeService(set, "mmtel", "Vowifi MPS false");
                removeService(set, "mmtel-video", "Vowifi MPS false");
                removeService(set, "smsip", "Vowifi MPS false");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_MPS_DISABLED.getCode());
            }
            if (!booleanValue3) {
                removeService(set, "smsip", "SMSIP MPS false");
            }
            if (!booleanValue4) {
                removeService(set, "mmtel-video", "Enable ViLTE MPS false");
            }
        }
        return set;
    }

    public boolean isNoNextPcscf() {
        boolean z = true;
        if (this.mCurPcscfIpIdx + 1 < this.mNumOfPcscfIp) {
            z = false;
        }
        IMSLog.i(LOG_TAG, "isNoNextPcscf = " + z);
        return z;
    }

    public int getNumOfPcscfIp() {
        return this.mNumOfPcscfIp;
    }

    public void increasePcscfIdx() {
        int i = this.mNumOfPcscfIp;
        if (i > 0) {
            this.mCurPcscfIpIdx = (this.mCurPcscfIpIdx + 1) % i;
        }
        int phoneId = this.mTask.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "increasePcscfIdx: now [" + this.mCurPcscfIpIdx + "]");
    }

    public boolean isMatchedPdnFailReason(PdnFailReason pdnFailReason) {
        return !TextUtils.isEmpty(getMatchedPdnFailReasonFromGlobalSettings(pdnFailReason));
    }

    public String getMatchedPdnFailReasonFromGlobalSettings(PdnFailReason pdnFailReason) {
        String[] stringArray = ImsRegistry.getStringArray(this.mTask.getPhoneId(), GlobalSettingsConstants.Registration.PDN_FAIL_REASON_LIST, new String[0]);
        IMSLog.i(LOG_TAG, "getMatchedPdnFailReason: reasons: " + Arrays.toString(stringArray));
        return (String) Arrays.stream(stringArray).map(new RegistrationGovernorBase$$ExternalSyntheticLambda2()).map(new RegistrationGovernorBase$$ExternalSyntheticLambda3()).filter(new RegistrationGovernorBase$$ExternalSyntheticLambda4(pdnFailReason)).findAny().orElse("");
    }

    /* access modifiers changed from: protected */
    public void sendRawRequestToTelephony(Context context, byte[] bArr) {
        this.mTelephonyManager.sendRawRequestToTelephony(context, bArr);
    }

    public RegistrationConstants.RegisterTaskState getState() {
        return this.mTask.getState();
    }

    /* access modifiers changed from: protected */
    public void setUpsmEventReceiver() {
        Log.i(LOG_TAG, "setUpsmEventReceiver.");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.samsung.intent.action.EMERGENCY_STATE_CHANGED");
        intentFilter.addAction(SemEmergencyConstantsExt.EMERGENCY_CHECK_ABNORMAL_STATE);
        intentFilter.addAction("com.samsung.intent.action.EMERGENCY_START_SERVICE_BY_ORDER");
        this.mContext.registerReceiver(this.mUpsmEventReceiver, intentFilter);
    }

    public void unRegisterIntentReceiver() {
        Log.i(LOG_TAG, "Un-register Intent receiver(s)");
        try {
            this.mContext.unregisterReceiver(this.mUpsmEventReceiver);
            this.mContext.unregisterReceiver(this.mPackageDataClearedIntentReceiver);
        } catch (IllegalArgumentException unused) {
            Log.e(LOG_TAG, "unRegisterIntentReceiver: Receiver not registered!");
        }
    }

    /* access modifiers changed from: protected */
    public int onUltraPowerSavingModeChanged() {
        SemEmergencyManager instance = SemEmergencyManager.getInstance(this.mContext);
        if (instance == null) {
            Log.e(LOG_TAG, "onUltraPowerSavingModeChanged: SemEmergencyManager is null!");
            return -1;
        }
        boolean isEmergencyMode = SemEmergencyManager.isEmergencyMode(this.mContext);
        boolean checkUltraPowerSavingMode = SystemUtil.checkUltraPowerSavingMode(instance);
        Log.i(LOG_TAG, "onUltraPowerSavingModeChanged: emergency=" + isEmergencyMode + ", UPSM=" + checkUltraPowerSavingMode);
        if (!isEmergencyMode || !checkUltraPowerSavingMode) {
            if (this.mUpsmEnabled) {
                Log.i(LOG_TAG, "EM is disabled");
                this.mUpsmEnabled = false;
                if (isThrottled()) {
                    releaseThrottle(0);
                }
                return 1;
            }
        } else if (this.mUpsmEnabled) {
            Log.i(LOG_TAG, "EM is already enabled, so skip.");
        } else {
            Log.i(LOG_TAG, "EM is enabled");
            this.mUpsmEnabled = true;
            if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
                return 0;
            }
        }
        return -1;
    }

    public boolean checkEmergencyInProgress() {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(this.mPhoneId);
        if (pendingRegistrationInternal == null) {
            return false;
        }
        Iterator it = pendingRegistrationInternal.iterator();
        while (it.hasNext()) {
            RegisterTask registerTask = (RegisterTask) it.next();
            if (registerTask.getProfile().hasEmergencySupport() && registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.DEREGISTERING)) {
                return true;
            }
        }
        return false;
    }

    public boolean isReadyToDualRegister(boolean z) {
        if (SimConstants.DSDS_DI.equals(SimUtil.getConfigDualIMS()) || z) {
            int oppositeSimSlot = SimUtil.getOppositeSimSlot(this.mPhoneId);
            if (isW2lInProgress(oppositeSimSlot)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : false, other slot is re-registering for W2L handover");
                return false;
            } else if ((isRegistering(oppositeSimSlot) && checkOwnStateIfNeeded()) || ((isImsPdnConnecting(oppositeSimSlot) && isOwnSlotNotConnected()) || isInCall(oppositeSimSlot))) {
                return isReadyToDualRegisterOnOtherSlotBusy(oppositeSimSlot, z);
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : true");
                return true;
            }
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : true - Non DSDS_DI");
            return true;
        }
    }

    private boolean isW2lInProgress(int i) {
        if (!(!this.mRegMan.getNetworkEvent(i).isEpdgConnected) || !Optional.ofNullable(RegistrationUtils.getPendingRegistrationInternal(i)).flatMap(new RegistrationGovernorBase$$ExternalSyntheticLambda17()).isPresent()) {
            return false;
        }
        return true;
    }

    private boolean isInCall(int i) {
        int callState = this.mTelephonyManager.getCallState(i);
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "isReadyToDualRegister: slot " + i + "'s call state: " + callState);
        return callState != 0;
    }

    private boolean isRegistering(int i) {
        return ((Boolean) Optional.ofNullable(RegistrationUtils.getPendingRegistrationInternal(i)).flatMap(new RegistrationGovernorBase$$ExternalSyntheticLambda21()).map(new RegistrationGovernorBase$$ExternalSyntheticLambda22(this, i)).orElse(Boolean.FALSE)).booleanValue();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$isRegistering$10(int i, RegisterTask registerTask) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "isReadyToDualRegister: slot " + i + " is registering");
        return Boolean.TRUE;
    }

    private boolean checkOwnStateIfNeeded() {
        Mno mno = this.mTask.getMno();
        if (!mno.isIndia() && !mno.isVietnam()) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : Own slot is REGISTERING or CONNECTED and other slot is REGISTERING");
        return this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.DEREGISTERING, RegistrationConstants.RegisterTaskState.CONNECTED);
    }

    private boolean isImsPdnConnecting(int i) {
        return isInServiceOnCellular(i) && hasImsConnectingTask(i);
    }

    private boolean isInServiceOnCellular(int i) {
        NetworkEvent networkEvent = this.mRegMan.getNetworkEvent(i);
        return !networkEvent.outOfService && networkEvent.network != 18;
    }

    private boolean hasImsConnectingTask(int i) {
        return ((Boolean) Optional.ofNullable(RegistrationUtils.getPendingRegistrationInternal(i)).flatMap(new RegistrationGovernorBase$$ExternalSyntheticLambda15()).map(new RegistrationGovernorBase$$ExternalSyntheticLambda16(this, i)).orElse(Boolean.FALSE)).booleanValue();
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$hasImsConnectingTask$12(RegisterTask registerTask) {
        return registerTask.getPdnType() == 11;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$hasImsConnectingTask$13(RegisterTask registerTask) {
        return !registerTask.getGovernor().hasPdnFailure();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$hasImsConnectingTask$15(int i, RegisterTask registerTask) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "isReadyToDualRegister: slot " + i + " is connecting");
        return Boolean.TRUE;
    }

    private boolean isOwnSlotNotConnected() {
        return this.mTask.getProfile().getPdnType() == 11 && this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONNECTING);
    }

    private boolean isReadyToDualRegisterOnOtherSlotBusy(int i, boolean z) {
        int subId = SimUtil.getSubId(i);
        if (this.mPdnController.isEpdgConnected(i) && this.mTelephonyManager.getDataNetworkType(subId) == 18) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : true - other slot is EPDG Call or Registering");
            return true;
        } else if (this.mTask.getProfile().hasEmergencySupport()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : true - The slot will make E911 call");
            return true;
        } else if (this.mRegMan.findBestNetwork(this.mPhoneId, this.mTask.getProfile(), this) == 18 && !z) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : true - This slot is going to register VoWifi");
            return true;
        } else if (this.mTask.isRcsOnly()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : true - This slot is going to register RCS only profile");
            return true;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : false");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> applyMmtelUserSettings(Set<String> set, int i) {
        if (set == null) {
            return new HashSet();
        }
        if (getVoiceTechType() == 0 || this.mTask.getRegistrationRat() == 18) {
            return set;
        }
        Log.i(LOG_TAG, "by VoLTE OFF, remove all service, RAT :" + this.mTask.getRegistrationRat());
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return new HashSet();
    }

    public String getUpdateRegiPendingReason(int i, NetworkEvent networkEvent, boolean z, boolean z2) {
        int pdnType = this.mTask.getProfile().getPdnType();
        boolean z3 = pdnType == 11;
        if (!z2 && !isReadyToRegister(i)) {
            return "Governor is not ready";
        }
        if (!z2 && z3 && isCellIdentityUnavailable(networkEvent.network)) {
            return "CellInfo is not yet updated";
        }
        if (!isSrvccCase() && this.mTask.isSuspended()) {
            return "suspended and not SRVCC";
        }
        if (!networkEvent.outOfService) {
            return (!z2 || !this.mTask.mIsUpdateRegistering) ? "" : "Immediate update registration triggered but it's under update registering";
        }
        if (z3 || pdnType == 15) {
            return "OOS";
        }
        if (!this.mPdnController.isWifiConnected()) {
            return "OOS and WiFi is not connected";
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCellIdentityUnavailable(int i) {
        if (i == 18) {
            return false;
        }
        CellIdentityWrapper currentCellIdentity = this.mPdnController.getCurrentCellIdentity(this.mPhoneId, i);
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "isCellIdentityUnavailable: rat " + i + " -> " + currentCellIdentity);
        if (currentCellIdentity == CellIdentityWrapper.DEFAULT) {
            return true;
        }
        return false;
    }

    public boolean determineDeRegistration(int i, int i2) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "isNeedToDeRegistration:");
        boolean z = this.mTelephonyManager.getCallState() != 0;
        if (i == 0) {
            int i3 = this.mPhoneId;
            IMSLog.i(LOG_TAG, i3, "isNeedToDeRegistration: no IMS service for network " + i2 + ". Deregister.");
            RegisterTask registerTask = this.mTask;
            registerTask.setReason("no IMS service for network : " + i2);
            this.mTask.setDeregiReason(4);
            this.mRegMan.tryDeregisterInternal(this.mTask, false, false);
            return true;
        } else if (RegistrationUtils.supportCsTty(this.mTask) && SlotBasedConfig.getInstance(this.mPhoneId).getTTYMode() && !z) {
            this.mTask.setReason("TTY enabled");
            this.mTask.setDeregiReason(75);
            this.mRegMan.tryDeregisterInternal(this.mTask, false, false);
            return true;
        } else if (!ConfigUtil.isRcsEur(this.mMno) || !this.mTask.isRcsOnly() || this.mTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERED || i != 18 || this.mTask.getRegistrationRat() == 18) {
            return false;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "determineDeRegistration:  WiFi is connected.");
            this.mTask.setDeregiReason(4);
            this.mRegMan.tryDeregisterInternal(this.mTask, true, false);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean updateGeolocation(String str) {
        boolean z;
        if (!TextUtils.isEmpty(str) && !str.equalsIgnoreCase(this.mCountry)) {
            z = true;
            if (this.mTask.getProfile().getSupportedGeolocationPhase() >= 1) {
                if (isThrottled()) {
                    releaseThrottle(6);
                }
                int phoneId = this.mTask.getPhoneId();
                NetworkEvent networkEvent = this.mRegMan.getNetworkEvent(phoneId);
                if ((!TextUtils.isEmpty(str) && !str.equalsIgnoreCase(this.mCountry)) || (TextUtils.isEmpty(str) && !TextUtils.isEmpty(this.mCountry))) {
                    if (this.mTask.getProfile().getPdnType() == 11 && this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && !this.mTask.mIsUpdateRegistering && networkEvent.isEpdgConnected) {
                        IMSLog.i(LOG_TAG, phoneId, "updateRegistration as Country Code change");
                        this.mRegMan.updatePani(phoneId);
                        this.mTask.setReason("update location");
                        this.mRegMan.updateRegistration(this.mTask, RegistrationConstants.UpdateRegiReason.GEOLOCATION_CHANGED_FORCED, false);
                    }
                    this.mCountry = str;
                }
                return z;
            }
        }
        z = false;
        int phoneId2 = this.mTask.getPhoneId();
        NetworkEvent networkEvent2 = this.mRegMan.getNetworkEvent(phoneId2);
        IMSLog.i(LOG_TAG, phoneId2, "updateRegistration as Country Code change");
        this.mRegMan.updatePani(phoneId2);
        this.mTask.setReason("update location");
        this.mRegMan.updateRegistration(this.mTask, RegistrationConstants.UpdateRegiReason.GEOLOCATION_CHANGED_FORCED, false);
        this.mCountry = str;
        return z;
    }

    public RegisterTask onManualDeregister(boolean z) {
        int phoneId = this.mTask.getPhoneId();
        ImsProfile profile = this.mTask.getProfile();
        boolean z2 = true;
        if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
            boolean hasEmergencySupport = profile.hasEmergencySupport();
            IMSLog.i(LOG_TAG, phoneId, "onManualDeregister: emergency: " + hasEmergencySupport + ", explicit dereg: " + z);
            this.mTask.setReason("manual deregi");
            this.mTask.setDeregiReason(22);
            RegistrationManagerInternal registrationManagerInternal = this.mRegMan;
            RegisterTask registerTask = this.mTask;
            if (z && !hasEmergencySupport) {
                z2 = false;
            }
            registrationManagerInternal.tryDeregisterInternal(registerTask, z2, false);
            return null;
        }
        RegisterTask registerTask2 = this.mTask;
        RegistrationConstants.RegisterTaskState registerTaskState = RegistrationConstants.RegisterTaskState.CONNECTING;
        RegistrationConstants.RegisterTaskState registerTaskState2 = RegistrationConstants.RegisterTaskState.CONNECTED;
        if (registerTask2.isOneOf(registerTaskState, registerTaskState2)) {
            IMSLog.i(LOG_TAG, phoneId, "onManualDeregister: disconnecting PDN network " + this.mTask.getPdnType());
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            if (profile.hasEmergencySupport() || RegistrationUtils.isCmcProfile(profile)) {
                return this.mTask;
            }
            return null;
        } else if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.EMERGENCY) || (this.mTask.getState() == RegistrationConstants.RegisterTaskState.DEREGISTERING && this.mTask.getUserAgent() == null)) {
            IMSLog.i(LOG_TAG, phoneId, "onManualDeregister: disconnect Emergency PDN.");
            this.mTask.setReason("manual deregi(EPDN)");
            this.mTask.setDeregiReason(30);
            RegistrationConstants.RegisterTaskState state = this.mTask.getState();
            RegistrationConstants.RegisterTaskState registerTaskState3 = RegistrationConstants.RegisterTaskState.DEREGISTERING;
            if (state != registerTaskState3) {
                this.mRegMan.tryDeregisterInternal(this.mTask, true, false);
            }
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            if (this.mTask.getState() != registerTaskState3) {
                return this.mTask;
            }
            if (this.mTask.needKeepEmergencyTask()) {
                this.mTask.keepEmergencyTask(false);
                return null;
            } else if (!RegistrationUtils.isCmcProfile(this.mTask.getProfile())) {
                return this.mTask;
            } else {
                return null;
            }
        } else if (!this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONFIGURING, RegistrationConstants.RegisterTaskState.CONFIGURED, registerTaskState2) || !SlotBasedConfig.getInstance(phoneId).getExtendedProfiles().containsKey(Integer.valueOf(profile.getId()))) {
            return null;
        } else {
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            return this.mTask;
        }
    }

    public boolean hasEmergencyTaskInPriority(List<? extends IRegisterTask> list) {
        return (this.mMno.isCanada() || this.mMno.isOneOf(Mno.OPTUS, Mno.TELSTRA, Mno.TELIA_NORWAY, Mno.EE, Mno.EE_ESN, Mno.CTC, Mno.CTCMO, Mno.CHT)) && list.stream().filter(new RegistrationGovernorBase$$ExternalSyntheticLambda11()).map(new RegistrationGovernorBase$$ExternalSyntheticLambda12()).anyMatch(new RegistrationGovernorBase$$ExternalSyntheticLambda13());
    }

    public boolean needPendingPdnConnected() {
        ImsProfile profile = this.mTask.getProfile();
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(this.mPhoneId);
        if (pendingRegistrationInternal == null || profile.hasEmergencySupport() || !hasEmergencyTaskInPriority(pendingRegistrationInternal)) {
            return false;
        }
        RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
        registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(22, this.mTask), 500);
        Log.i(LOG_TAG, "onPdnConnected: delay " + profile.getName() + " due to priority of Emergency.");
        return true;
    }

    /* access modifiers changed from: protected */
    public void updateVolteState() {
        String matchedSalesCode = this.mMno.getMatchedSalesCode(OmcCode.getNWCode(this.mPhoneId));
        if (("ACG".equals(matchedSalesCode) || "LRA".equals(matchedSalesCode)) && SimUtil.isDualIMS()) {
            int voiceTechType = getVoiceTechType(this.mPhoneId);
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "updateVolteState for ACG/LRA DSDS : voiceTech : " + voiceTechType);
            SemTelephonyAdapter.sendVolteState(this.mPhoneId, voiceTechType == 0);
        }
    }

    /* access modifiers changed from: protected */
    public void handleTimeOutEmerRegiError() {
        boolean isPdnConnected = this.mRegMan.isPdnConnected(this.mTask.getProfile(), this.mPhoneId);
        if (getFailureCount() >= getNumOfEmerPcscfIp() || !isPdnConnected) {
            IUserAgent userAgent = this.mTask.getUserAgent();
            if (userAgent != null) {
                userAgent.notifyE911RegistrationFailed();
            }
            if (!isPdnConnected) {
                RegistrationUtils.sendEmergencyRegistrationFailed(this.mTask);
                resetRetry();
                return;
            }
            return;
        }
        this.mRegHandler.requestTryEmergencyRegister(this.mTask);
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$checkUnProcessedVoLTEState$17(ScheduledFuture scheduledFuture) {
        return !scheduledFuture.isDone();
    }

    public void checkUnProcessedVoLTEState() {
        Optional.ofNullable(this.mDelayedVolteOffFuture).filter(new RegistrationGovernorBase$$ExternalSyntheticLambda18()).ifPresent(new RegistrationGovernorBase$$ExternalSyntheticLambda19(this));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$checkUnProcessedVoLTEState$18(ScheduledFuture scheduledFuture) {
        IMSLog.i(LOG_TAG, this.mPhoneId, this.mTask, "checkUnProcessedVoLTEState: Send pending VoLTE state now");
        scheduledFuture.cancel(false);
        SemTelephonyAdapter.sendVolteState(this.mPhoneId, false);
    }

    /* access modifiers changed from: protected */
    public void onPdnFailCounterInNr() {
        if (this.mRegMan.getCurrentNetworkByPhoneId(this.mPhoneId) == 20 && this.mTask.getPdnType() == 11) {
            int i = this.mPdnRejectCounter + 1;
            this.mPdnRejectCounter = i;
            if (i >= 3) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "notifyImsNotAvailable");
                this.mRegMan.notifyImsNotAvailable(this.mTask, true);
                this.mPdnRejectCounter = 0;
            }
        }
    }

    public int getNumOfEmerPcscfIp() {
        return this.mNumOfPcscfIp;
    }

    public void onAdsChanged(int i) {
        updateVolteState();
        if (this.mMno.isUSA()) {
            this.mRegMan.updateRegistration(this.mTask, RegistrationConstants.UpdateRegiReason.ADS_CHANGED);
        }
    }

    /* access modifiers changed from: protected */
    public void forceTurnOnVoLte() {
        if (ImsConstants.SystemSettings.getVoiceCallType(this.mContext, -1, this.mPhoneId) != 0) {
            Log.i(LOG_TAG, "forceTurnOnVoLte : voicecall_type is not 0, force enable");
            ImsConstants.SystemSettings.setVoiceCallType(this.mContext, 0, this.mPhoneId);
        }
    }

    /* access modifiers changed from: protected */
    public void forceTurnOnVoLteWhenMenuRemoved() {
        if (this.mIsVoLteMenuRemoved) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "forceTurnOnVoLteWhenMenuRemoved: ");
            forceTurnOnVoLte();
        }
    }
}
