package com.sec.internal.ims.core;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.SemTelephonyAdapter;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.ss.IUtServiceModule;
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

public class RegistrationGovernorOce extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnOce";
    private IGeolocationController mGeolocationCon = ImsRegistry.getGeolocationController();
    ScheduledExecutorService mVolteOffExecutor = Executors.newSingleThreadScheduledExecutor();

    public boolean isLocationInfoLoaded(int i) {
        return true;
    }

    public RegistrationGovernorOce(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        this.mNeedToCheckSrvcc = true;
        this.mHandlePcscfOnAlternativeCall = true;
        this.mNeedToCheckLocationSetting = false;
    }

    public boolean isThrottled() {
        return this.mIsPermanentStopped || (this.mIsPermanentPdnFailed && this.mTask.getProfile().getPdnType() == 11) || this.mRegiAt > SystemClock.elapsedRealtime();
    }

    public void releaseThrottle(int i) {
        IUtServiceModule utServiceModule;
        if (i == 1) {
            if (this.mTask.isRcsOnly()) {
                this.mTask.setDeregiReason(23);
                this.mRegMan.deregister(this.mTask, false, false, "flight mode enabled");
            }
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mNonVoLTESimByPdnFail = false;
        } else if (i == 4) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mCurImpu = 0;
            this.mNonVoLTESimByPdnFail = false;
        } else if (i == 6) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mCurImpu = 0;
            this.mNonVoLTESimByPdnFail = false;
        } else if (i == 11) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mNonVoLTESimByPdnFail = false;
        }
        if (!this.mIsPermanentStopped || !this.mIsPermanentPdnFailed) {
            Log.i("RegiGvnOce[" + this.mPhoneId + "]", "releaseThrottle: case by " + i);
            if (this.mMno == Mno.TELSTRA && (utServiceModule = ImsRegistry.getServiceModuleManager().getUtServiceModule()) != null) {
                utServiceModule.enableUt(this.mPhoneId, true);
            }
        }
    }

    public void onPdnRequestFailed(PdnFailReason pdnFailReason, int i) {
        IUtServiceModule utServiceModule;
        super.onPdnRequestFailed(pdnFailReason, i);
        if (isMatchedPdnFailReason(pdnFailReason)) {
            this.mRegMan.notifyImsNotAvailable(this.mTask, true, true);
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            this.mIsPermanentPdnFailed = true;
            this.mNonVoLTESimByPdnFail = true;
            if (this.mMno == Mno.TELSTRA && (utServiceModule = ImsRegistry.getServiceModuleManager().getUtServiceModule()) != null) {
                utServiceModule.enableUt(this.mPhoneId, false);
                return;
            }
            return;
        }
        onPdnFailCounterInNr();
    }

    private boolean checkSimState() {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (simManagerFromSimSlot == null || simManagerFromSimSlot.isSimLoaded()) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: Sim hasn't loaded yet");
        return false;
    }

    private boolean checkAvailableRat(int i) {
        if (NetworkUtil.is3gppPsVoiceNetwork(i) || i == 18 || this.mTask.getProfile().getPdnType() != 11 || this.mMno != Mno.VODAFONE_NEWZEALAND) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: No PS Voice capable RAT");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.DATA_RAT_IS_NOT_PS_VOICE.getCode());
        this.mTask.setRegistrationRat(i);
        if (this.mTask.getImsRegistration() == null) {
            return false;
        }
        this.mTask.getImsRegistration().setCurrentRat(i);
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0 || this.mTask.isEpdgHandoverInProgress()) {
            return true;
        }
        if (isSrvccCase()) {
            if (this.mTask.getProfile().getBlockDeregiOnSrvcc()) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: Skip deregister SRVCC");
                return false;
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: SRVCC case");
            return true;
        } else if (this.mVsm.hasPendingCall(this.mPhoneId)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: wait Ims PDN registration");
            return true;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: call state is not idle");
            return false;
        }
    }

    public boolean isReadyToRegister(int i) {
        return checkEmergencyStatus() || (checkRegiStatus() && checkSimState() && checkRoamingStatus(i) && checkAvailableRat(i) && checkCallStatus() && checkWFCsettings(i) && checkNetworkEvent(i));
    }

    public Set<String> filterService(Set<String> set, int i) {
        if (isImsDisabled()) {
            return new HashSet();
        }
        HashSet hashSet = new HashSet();
        Set<String> hashSet2 = new HashSet<>(set);
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) == 1) {
            hashSet.addAll(servicesByImsSwitch(ImsProfile.getVoLteServiceList()));
            if (!hashSet.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
            }
        }
        Set<String> applyImsSwitch = applyImsSwitch(hashSet, i);
        if (!NetworkUtil.isMobileDataOn(this.mContext) && i != 18) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Mobile off!");
            for (String str : ImsProfile.getRcsServiceList()) {
                if (DmConfigHelper.getImsSwitchValue(this.mContext, str, this.mPhoneId) == 1 && DmConfigHelper.readBool(this.mContext, str, Boolean.TRUE, this.mPhoneId).booleanValue()) {
                    removeService(hashSet2, str, "MobileOff");
                }
            }
        }
        if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
            applyImsSwitch.addAll(servicesByReadSwitch((String[]) servicesByImsSwitch(ImsProfile.getRcsServiceList()).toArray(new String[0])));
        }
        if (applyImsSwitch.isEmpty()) {
            return applyImsSwitch;
        }
        if (NetworkUtil.is3gppPsVoiceNetwork(i) && this.mTask.getProfile().getPdnType() == 11) {
            applyImsSwitch = applyVoPsPolicy(applyImsSwitch);
            if (applyImsSwitch.isEmpty()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                return applyImsSwitch;
            }
        }
        Mno mno = this.mMno;
        if ((mno == Mno.TELSTRA || mno == Mno.VODAFONE_AUSTRALIA) && !isVideoCallEnabled()) {
            removeService(hashSet2, "mmtel-video", "TELSTRA/VODAFONE VideoCall disabled");
        }
        if (this.mTask.getProfile().getPdnType() == 11) {
            hashSet2 = applyMmtelUserSettings(hashSet2, i);
        }
        if (!this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming || this.mTask.getProfile().getPdnType() != 11 || this.mTask.getProfile().isAllowedOnRoaming() || i == 18) {
            if (!hashSet2.isEmpty()) {
                hashSet2.retainAll(applyImsSwitch);
            }
            return hashSet2;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "filterEnabledCoreService: Roaming not support.");
        return new HashSet();
    }

    /* access modifiers changed from: protected */
    public void handleForbiddenError(long j) {
        IUtServiceModule utServiceModule;
        String regRetryPcscfPolicyOn403 = this.mTask.getProfile().getRegRetryPcscfPolicyOn403();
        if (RegistrationGovernor.RETRY_TO_NEXT_PCSCF.equals(regRetryPcscfPolicyOn403)) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: Retry to next PCSCF address in case 403 Forbidden");
        } else if (RegistrationGovernor.RETRY_TO_SAME_PCSCF.equals(regRetryPcscfPolicyOn403)) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: Retry to same PCSCF address in case 403 Forbidden");
            this.mCurPcscfIpIdx--;
        } else {
            if (this.mMno == Mno.TELSTRA && this.mTask.getProfile().getPdnType() == 11 && (utServiceModule = ImsRegistry.getServiceModuleManager().getUtServiceModule()) != null) {
                utServiceModule.enableUt(this.mPhoneId, false);
            }
            IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: Permanently prohibited.");
            this.mIsPermanentStopped = true;
            if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                resetPcscfList();
            }
        }
        if (this.mIsPermanentStopped && this.mTask.getProfile().getPdnType() == 11) {
            this.mRegMan.notifyImsNotAvailable(this.mTask, true, true);
        }
    }

    public boolean determineDeRegistration(int i, int i2) {
        if (i != 0 || this.mMno != Mno.VODAFONE_AUSTRALIA || this.mTelephonyManager.getCallState() == 0) {
            return super.determineDeRegistration(i, i2);
        }
        this.mTask.setDeregiReason(4);
        this.mRegMan.deregister(this.mTask, false, false, 6000, "Vodafone AU: delay 6s to deregister");
        return true;
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

    public void onRegistrationDone() {
        super.onRegistrationDone();
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
            Optional.ofNullable(this.mDelayedVolteOffFuture).filter(new RegistrationGovernorOce$$ExternalSyntheticLambda0()).ifPresent(new RegistrationGovernorOce$$ExternalSyntheticLambda1());
            SemTelephonyAdapter.sendVolteState(this.mPhoneId, true);
            return;
        }
        long deregistrationTimeout = ((long) IRegistrationManager.getDeregistrationTimeout(this.mTask.getProfile(), this.mTask.getRegistrationRat())) * 2;
        int i2 = this.mPhoneId;
        RegisterTask registerTask2 = this.mTask;
        IMSLog.i(LOG_TAG, i2, registerTask2, "onVolteSettingChanged: Pending sendVolteState in " + deregistrationTimeout + "msec");
        this.mDelayedVolteOffFuture = this.mVolteOffExecutor.schedule(new RegistrationGovernorOce$$ExternalSyntheticLambda2(this), deregistrationTimeout, TimeUnit.MILLISECONDS);
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
