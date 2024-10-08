package com.sec.internal.ims.core;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.SemTelephonyAdapter;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RegistrationGovernorSea extends RegistrationGovernorBase {
    private static final long DELAYED_DEREGISTER_TIMER_MS = 10000;
    private static final String LOG_TAG = "RegiGvnSea";
    protected int mCurPcscf503FailCounter = 0;
    protected PdnFailReason mLastPdnFailedReason = PdnFailReason.NOT_DEFINED;
    ScheduledExecutorService mVolteOffExecutor = Executors.newSingleThreadScheduledExecutor();

    public RegistrationGovernorSea(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        this.mNeedToCheckSrvcc = true;
        this.mHandlePcscfOnAlternativeCall = true;
        this.mNeedToCheckLocationSetting = false;
    }

    public boolean isThrottled() {
        if (this.mIsPermanentStopped || this.mRegiAt > SystemClock.elapsedRealtime()) {
            return true;
        }
        if (!this.mIsPermanentPdnFailed || this.mTask.getProfile().getPdnType() != 11) {
            return false;
        }
        if (this.mMno == Mno.SMART_PH && PdnFailReason.INSUFFICIENT_RESOURCES == this.mLastPdnFailedReason && this.mRegMan.getCurrentNetworkByPhoneId(this.mTask.getPhoneId()) == 18) {
            return false;
        }
        return true;
    }

    public void releaseThrottle(int i) {
        if (i == 1) {
            if (isDelayedDeregisterTimerRunning()) {
                Log.i("RegiGvnSea[" + this.mTask.getPhoneId() + "]", "releaseThrottle: delete DelayedDeregisterTimer on fligt mode");
                setDelayedDeregisterTimerRunning(false);
            }
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mLastPdnFailedReason = PdnFailReason.NOT_DEFINED;
            this.mNonVoLTESimByPdnFail = false;
        } else if (i == 4) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mLastPdnFailedReason = PdnFailReason.NOT_DEFINED;
            this.mCurImpu = 0;
            this.mNonVoLTESimByPdnFail = false;
        } else if (i == 11) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mLastPdnFailedReason = PdnFailReason.NOT_DEFINED;
            this.mNonVoLTESimByPdnFail = false;
        }
        if (!this.mIsPermanentStopped || !this.mIsPermanentPdnFailed) {
            Log.i("RegiGvnSea[" + this.mTask.getPhoneId() + "]", "releaseThrottle: case by " + i);
        }
    }

    public void onPdnRequestFailed(PdnFailReason pdnFailReason, int i) {
        boolean z;
        super.onPdnRequestFailed(pdnFailReason, i);
        this.mLastPdnFailedReason = pdnFailReason;
        String matchedPdnFailReasonFromGlobalSettings = getMatchedPdnFailReasonFromGlobalSettings(pdnFailReason);
        if (!TextUtils.isEmpty(matchedPdnFailReasonFromGlobalSettings)) {
            setRetryTimeOnPdnFail(matchedPdnFailReasonFromGlobalSettings.indexOf(":") != -1 ? Long.parseLong(matchedPdnFailReasonFromGlobalSettings.substring(matchedPdnFailReasonFromGlobalSettings.indexOf(":") + 1, matchedPdnFailReasonFromGlobalSettings.length())) : -1);
            z = true;
        } else {
            z = false;
        }
        if (z) {
            this.mIsPermanentPdnFailed = true;
            this.mRegMan.notifyImsNotAvailable(this.mTask, true, true);
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            if (this.mMno == Mno.SMART_PH && pdnFailReason == PdnFailReason.INSUFFICIENT_RESOURCES) {
                Log.i("RegiGvnSea[" + this.mTask.getPhoneId() + "]", "SMART_PH not enable Volte on all sites");
                return;
            }
            this.mNonVoLTESimByPdnFail = true;
            return;
        }
        onPdnFailCounterInNr();
    }

    public Set<String> filterService(Set<String> set, int i) {
        if (isImsDisabled()) {
            return new HashSet();
        }
        HashSet hashSet = new HashSet();
        HashSet hashSet2 = new HashSet(set);
        boolean z = false;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mTask.getPhoneId()) == 1) {
            hashSet.addAll(servicesByImsSwitch(ImsProfile.getVoLteServiceList()));
            if (!hashSet.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
            }
        }
        Set<String> applyImsSwitch = applyImsSwitch(hashSet, i);
        if (this.mConfigModule.isValidAcsVersion(this.mTask.getPhoneId())) {
            Set<String> servicesByImsSwitch = servicesByImsSwitch(ImsProfile.getRcsServiceList());
            applyImsSwitch.addAll(servicesByReadSwitch((String[]) servicesByImsSwitch.toArray(new String[servicesByImsSwitch.size()])));
        }
        if (applyImsSwitch.isEmpty()) {
            return applyImsSwitch;
        }
        Mno mno = this.mMno;
        if (mno != Mno.TRUEMOVE && mno != Mno.AIS && NetworkUtil.is3gppPsVoiceNetwork(i) && this.mTask.getProfile().getPdnType() == 11) {
            applyImsSwitch = applyVoPsPolicy(applyImsSwitch);
            if (applyImsSwitch.isEmpty()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                return applyImsSwitch;
            }
        }
        if (this.mMno.isOneOf(Mno.CELCOM, Mno.UMOBILE, Mno.IMAGINE_BN, Mno.PROGRESIF_BN, Mno.UNN_BN, Mno.DST_BN) && NetworkUtil.is3gppPsVoiceNetwork(i) && this.mTask.getProfile().getPdnType() == 11) {
            if (ImsConstants.SystemSettings.DATA_ROAMING.get(this.mContext, ImsConstants.SystemSettings.DATA_ROAMING_UNKNOWN) == ImsConstants.SystemSettings.ROAMING_DATA_ENABLED) {
                z = true;
            }
            if (!z && this.mRegMan.getNetworkEvent(this.mTask.getPhoneId()).isDataRoaming) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "Data roaming OFF remove VoLTE service");
                String str = "Data roaming : OFF," + this.mTask.getRegistrationRat();
                removeService(applyImsSwitch, "mmtel-video", str);
                removeService(applyImsSwitch, "mmtel", str);
                removeService(applyImsSwitch, "smsip", str);
            }
        }
        if (!this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming || this.mTask.getProfile().getPdnType() != 11 || this.mTask.getProfile().isAllowedOnRoaming() || i == 18) {
            if (this.mTask.getProfile().getPdnType() == 11) {
                applyImsSwitch = applyMmtelUserSettings(applyImsSwitch, i);
            }
            if (!hashSet2.isEmpty()) {
                hashSet2.retainAll(applyImsSwitch);
            }
            return hashSet2;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "filterEnabledCoreService: Roaming not support.");
        return new HashSet();
    }

    /* access modifiers changed from: protected */
    public boolean checkRoamingStatus(int i) {
        int phoneId = this.mTask.getPhoneId();
        if ((!this.mRegMan.getNetworkEvent(phoneId).isDataRoaming && !this.mRegMan.getNetworkEvent(phoneId).isVoiceRoaming) || allowRoaming()) {
            return true;
        }
        if (i == 18 && this.mTask.getProfile().getPdnType() == 11) {
            IMSLog.i(LOG_TAG, phoneId, "VoWIFI skip for roaming check");
            return true;
        }
        IMSLog.i(LOG_TAG, phoneId, "isReadyToRegister: roaming is not allowed.");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ROAMING_NOT_SUPPORTED.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        int phoneId = this.mTask.getPhoneId();
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || this.mRegMan.getTelephonyCallStatus(phoneId) == 0 || this.mTask.isEpdgHandoverInProgress()) {
            return true;
        }
        if (!isSrvccCase()) {
            IMSLog.i(LOG_TAG, phoneId, "isReadyToRegister: call state is not idle");
            return false;
        } else if (this.mTask.getProfile().getBlockDeregiOnSrvcc()) {
            Log.i("RegiGvnSea[" + this.mTask.getPhoneId() + "]", "isReadyToRegister: Skip deregister SRVCC");
            return false;
        } else {
            IMSLog.i(LOG_TAG, phoneId, "isReadyToRegister: SRVCC case");
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkDeregisterTimer() {
        if (!this.mTask.getProfile().getBlockDeregiOnSrvcc() || !isDelayedDeregisterTimerRunning()) {
            return true;
        }
        Log.i("RegiGvnSea[" + this.mTask.getPhoneId() + "]", "isReadyToRegister: DelayedDeregisterTimer Running.");
        if (isDeregisterWithVoPSNeeded() || isDeregisterWithRATNeeded() || this.mRegMan.getNetworkEvent(this.mTask.getPhoneId()).outOfService) {
            return false;
        }
        Log.i("RegiGvnSea[" + this.mTask.getPhoneId() + "]", "isReadyToRegister: LTE attached. Delete DelayedDeregisterTimer.");
        this.mRegMan.onDelayedDeregister(this.mTask);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkNetworkEvent(int i) {
        int phoneId = this.mTask.getPhoneId();
        if (this.mMno == Mno.DIGI && !NetworkUtil.is3gppPsVoiceNetwork(i) && i != 18 && this.mTask.getProfile().getPdnType() == 11) {
            IMSLog.i(LOG_TAG, phoneId, "isReadyToRegister:  No PS Voice capable RAT");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.DATA_RAT_IS_NOT_PS_VOICE.getCode());
            this.mTask.setRegistrationRat(i);
            if (this.mTask.getImsRegistration() != null) {
                this.mTask.getImsRegistration().setCurrentRat(i);
            }
            return false;
        } else if (!this.mRegHandler.hasNetworModeChangeEvent() || !this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
            return true;
        } else {
            IMSLog.i(LOG_TAG, phoneId, "isReadyToRegister: networkModeChangeTimer Running.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ONGOING_NW_MODE_CHANGE.getCode());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void handleForbiddenError(long j) {
        super.handleForbiddenError(j);
        if (this.mIsPermanentStopped && this.mTask.getProfile().getPdnType() == 11) {
            this.mRegMan.notifyImsNotAvailable(this.mTask, true, true);
        }
    }

    public boolean isReadyToRegister(int i) {
        return checkEmergencyStatus() || (checkCallStatus() && checkRegiStatus() && checkRoamingStatus(i) && checkNetworkEvent(i) && checkWFCsettings(i) && checkDeregisterTimer()) || checkMdmnProfile();
    }

    public boolean isLocationInfoLoaded(int i) {
        if (this.mMno != Mno.GLOBE_PH) {
            return true;
        }
        return super.isLocationInfoLoaded(i);
    }

    public void onTelephonyCallStatusChanged(int i) {
        setCallStatus(i);
        if (this.mTask.getProfile().getBlockDeregiOnSrvcc() && getCallStatus() == 0) {
            int phoneId = this.mTask.getPhoneId();
            if (!this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                return;
            }
            if (isDeregisterWithVoPSNeeded() || isDeregisterWithRATNeeded() || this.mRegMan.getNetworkEvent(phoneId).outOfService) {
                IMSLog.i(LOG_TAG, phoneId, "onTelephonyCallStatusChanged: delayedDeregisterTimer 10000 milliseconds start");
                setDelayedDeregisterTimerRunning(true);
                this.mRegMan.sendDeregister((IRegisterTask) this.mTask, 10000);
            }
        }
    }

    public boolean isDelayedDeregisterTimerRunning() {
        return isDelayedDeregisterTimerRunningWithCallStatus();
    }

    public boolean isIPSecAllow() {
        if (!this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming || this.mMno != Mno.MOBILEONE) {
            return this.mIPsecAllow;
        }
        return false;
    }

    public void startTimsTimer(String str) {
        if (this.mTask.getPdnType() != 11) {
            Log.i(LOG_TAG, "If not IMS PDN, no need to start TimsTimer");
            return;
        }
        Log.i(LOG_TAG, "startTimsTimer : " + this.mTask.getProfile().getName() + "(" + this.mTask.getState() + ") Pdn(" + this.mTask.getPdnType() + "," + this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask) + ")");
        long j = 120000;
        if (!RegistrationConstants.REASON_IMS_PDN_REQUEST.equals(str)) {
            if (this.mTimEshtablishTimeout != null && RegistrationConstants.REASON_IMS_PDN_REQUEST.equals(this.mTimEshtablishTimeoutReason)) {
                stopTimsTimer(RegistrationConstants.REASON_IMS_PDN_REQUEST);
            }
            if (!CollectionUtils.isNullOrEmpty((Collection<?>) this.mPcscfIpList)) {
                j = ((long) this.mPcscfIpList.size()) * ((long) this.mTask.getProfile().getTimerF());
            }
        }
        startTimsEstablishTimer(this.mTask, j, str);
    }

    public void stopTimsTimer(String str) {
        stopTimsEstablishTimer(this.mTask, str);
    }

    public void onRegistrationError(SipError sipError, long j, boolean z) {
        this.mRegMan.getEventLog().logAndAdd("onRegistrationError: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mCurPcscfFailedCounter " + this.mCurPcscf503FailCounter + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (!this.mMno.isBrunei()) {
            super.onRegistrationError(sipError, j, z);
        } else if (!SipErrorBase.SERVICE_UNAVAILABLE.equals(sipError)) {
            this.mCurPcscf503FailCounter = 0;
            super.onRegistrationError(sipError, j, z);
        } else {
            this.mFailureCounter++;
            this.mCurPcscfIpIdx++;
            this.mCurPcscf503FailCounter++;
            if (j == 0 || j >= ((long) this.mTask.getProfile().getTimerF()) || this.mCurPcscf503FailCounter >= 3) {
                this.mCurPcscf503FailCounter = 0;
            } else {
                this.mCurPcscfIpIdx--;
            }
            handleRetryTimer(j);
        }
    }

    public void onRegistrationDone() {
        super.onRegistrationDone();
        this.mCurPcscf503FailCounter = 0;
        stopTimsTimer(RegistrationConstants.REASON_REGISTERED);
    }

    public void onVolteSettingChanged() {
        if (this.mTask.isRcsOnly()) {
            IMSLog.e(LOG_TAG, this.mPhoneId, this.mTask, "onVolteSettingChanged: Ignore");
            return;
        }
        boolean z = getVoiceTechType(this.mPhoneId) == 0;
        int i = this.mPhoneId;
        RegisterTask registerTask = this.mTask;
        IMSLog.i(LOG_TAG, i, registerTask, "onVolteSettingChanged: " + z);
        if (z) {
            Optional.ofNullable(this.mDelayedVolteOffFuture).filter(new RegistrationGovernorSea$$ExternalSyntheticLambda0()).ifPresent(new RegistrationGovernorSea$$ExternalSyntheticLambda1());
            SemTelephonyAdapter.sendVolteState(this.mPhoneId, true);
            return;
        }
        long deregistrationTimeout = ((long) IRegistrationManager.getDeregistrationTimeout(this.mTask.getProfile(), this.mTask.getRegistrationRat())) * 2;
        int i2 = this.mPhoneId;
        RegisterTask registerTask2 = this.mTask;
        IMSLog.i(LOG_TAG, i2, registerTask2, "onVolteSettingChanged: Pending sendVolteState in " + deregistrationTimeout + "msec");
        this.mDelayedVolteOffFuture = this.mVolteOffExecutor.schedule(new RegistrationGovernorSea$$ExternalSyntheticLambda2(this), deregistrationTimeout, TimeUnit.MILLISECONDS);
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$onVolteSettingChanged$0(ScheduledFuture scheduledFuture) {
        return !scheduledFuture.isDone();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onVolteSettingChanged$2() {
        SemTelephonyAdapter.sendVolteState(this.mPhoneId, false);
    }

    /* access modifiers changed from: protected */
    public int getVoiceTechType() {
        forceTurnOnVoLteWhenMenuRemoved();
        return super.getVoiceTechType();
    }
}
