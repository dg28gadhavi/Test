package com.sec.internal.ims.core;

import android.content.Context;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.SemTelephonyAdapter;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RegistrationGovernorMeAfrica extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnMeAfr";
    protected List<String> mLastPcscfList = null;
    ScheduledExecutorService mVolteOffExecutor = Executors.newSingleThreadScheduledExecutor();

    public RegistrationGovernorMeAfrica(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        this.mHandlePcscfOnAlternativeCall = true;
        this.mNeedToCheckLocationSetting = false;
    }

    public void onRegistrationTerminated(SipError sipError, long j, boolean z) {
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        eventLog.logAndAdd("onRegistrationTerminated: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j);
        if (SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(sipError)) {
            onRegistrationError(sipError, j, z);
        } else {
            super.onRegistrationTerminated(sipError, j, z);
        }
    }

    public void onRegistrationError(SipError sipError, long j, boolean z) {
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (!SipErrorBase.FORBIDDEN.equals(sipError) || !this.mTask.isRcsOnly() || !this.mTask.getProfile().getNeedAutoconfig()) {
            if (j < 0) {
                j = 0;
            }
            this.mFailureCounter++;
            this.mCurPcscfIpIdx++;
            if (!this.mTask.getProfile().hasEmergencySupport() || !SipErrorBase.SIP_TIMEOUT.equals(sipError) || this.mTask.getProfile().getE911RegiTime() <= 0) {
                if (SipErrorBase.isImsForbiddenError(sipError) || (this.mMno != Mno.AVEA_TURKEY && SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(sipError))) {
                    handleForbiddenError(j);
                    if (this.mIsPermanentStopped && this.mTask.getProfile().getPdnType() == 11) {
                        this.mRegMan.notifyImsNotAvailable(this.mTask, true, true);
                    }
                } else if (SipErrorBase.SIP_TIMEOUT.equals(sipError)) {
                    handleTimeoutError(j);
                }
                if (!this.mIsPermanentStopped) {
                    handleRetryTimer(j);
                    return;
                }
                return;
            }
            handleTimeOutEmerRegiError();
            return;
        }
        this.mConfigModule.startAcs(this.mPhoneId);
    }

    public void updatePcscfIpList(List<String> list) {
        if (list == null) {
            Log.e("RegiGvnMeAfr[" + this.mPhoneId + "]", "updatePcscfIpList: null P-CSCF list!");
        } else if (!this.mTask.getProfile().getDelayPcscfChangeDuringCall() || this.mCallStatus == 0) {
            super.updatePcscfIpList(list);
        } else {
            this.mLastPcscfList = new ArrayList(list);
        }
    }

    public void onTelephonyCallStatusChanged(int i) {
        List<String> list;
        this.mCallStatus = i;
        if (this.mTask.getProfile().getDelayPcscfChangeDuringCall() && this.mCallStatus == 0 && (list = this.mLastPcscfList) != null && !list.isEmpty()) {
            updatePcscfIpList(this.mLastPcscfList);
            this.mLastPcscfList = null;
        }
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
        if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
            applyImsSwitch.addAll(servicesByReadSwitch((String[]) servicesByImsSwitch(ImsProfile.getRcsServiceList()).toArray(new String[0])));
        }
        if (applyImsSwitch.isEmpty()) {
            return applyImsSwitch;
        }
        if (i == 13 && this.mTask.getProfile().getPdnType() == 11) {
            applyImsSwitch = applyVoPsPolicy(applyImsSwitch);
            if (applyImsSwitch.isEmpty()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                return applyImsSwitch;
            }
        }
        if (1 == this.mTask.getProfile().getPdnType() && !VowifiConfig.isEnabled(this.mContext, this.mPhoneId) && i == 18) {
            removeService(hashSet2, "mmtel", "VoWiFi diabled");
            removeService(hashSet2, "smsip", "VoWiFi diabled");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_USER_SETTINGS_OFF.getCode());
        }
        if (hashSet2.contains("gls") && !isGlsEnabled()) {
            removeService(hashSet2, "gls", "GLS disabled");
        }
        if (hashSet2.contains("ec") && !isEcEnabled(this.mPhoneId)) {
            removeService(hashSet2, "ec", "EC disabled");
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
    public boolean checkCallStatus() {
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0 || this.mTask.isEpdgHandoverInProgress()) {
            return true;
        }
        if (!isSrvccCase()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: call state is not idle");
            return false;
        } else if (this.mTask.getProfile().getBlockDeregiOnSrvcc()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: Skip deregister SRVCC");
            return false;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: SRVCC case");
            return true;
        }
    }

    public boolean isReadyToRegister(int i) {
        return checkEmergencyStatus() || (checkCallStatus() && checkRegiStatus() && checkRoamingStatus(i) && checkNetworkEvent(i));
    }

    private boolean isGlsEnabled() {
        return RcsConfigurationHelper.readBoolParam(this.mContext, ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH).booleanValue();
    }

    public boolean isLocationInfoLoaded(int i) {
        if (this.mMno != Mno.CELLC_SOUTHAFRICA || this.mTask.getRegistrationRat() != 18 || this.mTask.getProfile().getSupportedGeolocationPhase() < 2 || !this.mRegMan.isVoWiFiSupported(this.mPhoneId)) {
            return true;
        }
        IMSLog.e(LOG_TAG, this.mPhoneId, "update geo location");
        Optional.ofNullable(ImsRegistry.getGeolocationController()).ifPresent(new RegistrationGovernorMeAfrica$$ExternalSyntheticLambda0(this));
        return true;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$isLocationInfoLoaded$0(IGeolocationController iGeolocationController) {
        iGeolocationController.startGeolocationUpdate(this.mPhoneId, false);
    }

    public void releaseThrottle(int i) {
        if (i == 1) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
        } else if (i == 4) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mCurImpu = 0;
        } else if (i == 7) {
            this.mIsPermanentStopped = false;
        } else if (i == 11) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
        }
        if (!this.mIsPermanentStopped || !this.mIsPermanentPdnFailed) {
            int i2 = this.mPhoneId;
            IMSLog.i(LOG_TAG, i2, "releaseThrottle: case by " + i);
        }
    }

    public void onPdnRequestFailed(PdnFailReason pdnFailReason, int i) {
        super.onPdnRequestFailed(pdnFailReason, i);
        if (isMatchedPdnFailReason(pdnFailReason)) {
            this.mRegMan.notifyImsNotAvailable(this.mTask, true, true);
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            this.mIsPermanentPdnFailed = true;
            return;
        }
        onPdnFailCounterInNr();
    }

    public void startTimsTimer(String str) {
        if (this.mTask.getPdnType() != 11) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "If not IMS PDN, no need to start TimsTimer");
            return;
        }
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "startTimsTimer : " + this.mTask.getProfile().getName() + "(" + this.mTask.getState() + ") Pdn(" + this.mTask.getPdnType() + "," + this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask) + ")");
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
            Optional.ofNullable(this.mDelayedVolteOffFuture).filter(new RegistrationGovernorMeAfrica$$ExternalSyntheticLambda1()).ifPresent(new RegistrationGovernorMeAfrica$$ExternalSyntheticLambda2());
            SemTelephonyAdapter.sendVolteState(this.mPhoneId, true);
            return;
        }
        long deregistrationTimeout = ((long) IRegistrationManager.getDeregistrationTimeout(this.mTask.getProfile(), this.mTask.getRegistrationRat())) * 2;
        int i2 = this.mPhoneId;
        RegisterTask registerTask2 = this.mTask;
        IMSLog.i(LOG_TAG, i2, registerTask2, "onVolteSettingChanged: Pending sendVolteState in " + deregistrationTimeout + "msec");
        this.mDelayedVolteOffFuture = this.mVolteOffExecutor.schedule(new RegistrationGovernorMeAfrica$$ExternalSyntheticLambda3(this), deregistrationTimeout, TimeUnit.MILLISECONDS);
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$onVolteSettingChanged$1(ScheduledFuture scheduledFuture) {
        return !scheduledFuture.isDone();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onVolteSettingChanged$3() {
        SemTelephonyAdapter.sendVolteState(this.mPhoneId, false);
    }
}
