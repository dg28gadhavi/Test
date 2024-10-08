package com.sec.internal.ims.core;

import android.content.Context;
import android.os.SemSystemProperties;
import android.os.SystemClock;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorDcm extends RegistrationGovernorBase {
    private static final String KEY_PROP_DEFAULT_NETWORK = "ro.telephony.default_network";
    private static final String LOG_TAG = "RegiGvnDcm";
    private final boolean is5GCapable;

    private byte[] buildVolteOnOffOemHookCmd(boolean z) {
        return new byte[]{9, 5, 0, 6, 7, z ? (byte) 1 : 0};
    }

    public RegistrationGovernorDcm(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        this.is5GCapable = SemSystemProperties.getInt(KEY_PROP_DEFAULT_NETWORK, 26) >= 23;
    }

    /* access modifiers changed from: protected */
    public void handleTimeoutError(long j) {
        if (this.mCurPcscfIpIdx == this.mNumOfPcscfIp && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            resetPcscfList();
            stopTimsTimer(RegistrationConstants.REASON_TIMS_REFRESHING);
        }
    }

    public void onRegistrationTerminated(SipError sipError, long j, boolean z) {
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        eventLog.logAndAdd("onRegistrationTerminated: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j);
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        startRetryTimer(1000);
    }

    public void onRegistrationDone() {
        super.onRegistrationDone();
        stopTimsTimer(RegistrationConstants.REASON_REGISTERED);
    }

    public void onRegistrationError(SipError sipError, long j, boolean z) {
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (j < 0) {
            j = 0;
        }
        this.mFailureCounter++;
        this.mCurPcscfIpIdx++;
        if (SipErrorBase.isImsForbiddenError(sipError)) {
            StringBuilder sb = new StringBuilder();
            sb.append("onRegistrationError: SIP_403 error triggers DCM timer ");
            j = 240000;
            sb.append(240000);
            Log.e(LOG_TAG, sb.toString());
        } else if (SipErrorBase.SIP_TIMEOUT.equals(sipError) || SipErrorBase.USE_PROXY.equals(sipError) || SipErrorBase.SERVER_TIMEOUT.equals(sipError)) {
            handleTimeoutError(j);
        } else if (SipErrorBase.SERVICE_UNAVAILABLE.equals(sipError)) {
            if (j > 0) {
                Log.i(LOG_TAG, "onRegistrationError: block Ps e911 for " + j + "ms");
                this.mPse911Prohibited = true;
            }
            removeCurrentPcscfAndInitialRegister(true);
        }
        if (this.mCurPcscfIpIdx >= this.mNumOfPcscfIp) {
            this.mCurPcscfIpIdx = 0;
        }
        if (j == 0) {
            j = getWaitTime();
        }
        if (j > 0) {
            startRetryTimer(j);
        } else {
            this.mRegHandler.sendTryRegister(this.mPhoneId, 1000);
        }
    }

    public void onDeregistrationDone(boolean z) {
        if (z) {
            RegisterTask registerTask = this.mTask;
            if (!registerTask.mKeepPdn && getVoiceTechType(registerTask.getPhoneId()) == 1 && this.mTask.getPdnType() == 11) {
                this.mRegHandler.notifyVolteSettingOff(this.mTask, 1000);
            }
        }
    }

    public SipError onSipError(String str, SipError sipError) {
        Log.i(LOG_TAG, "onSipError: service=" + str + " error=" + sipError);
        if (SipErrorBase.SERVER_TIMEOUT.equals(sipError)) {
            removeCurrentPcscfAndInitialRegister(true);
        } else if (SipErrorBase.REQUEST_TIMEOUT.equals(sipError)) {
            if (this.mCurPcscfIpIdx == this.mNumOfPcscfIp && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                resetPcscfList();
            }
        } else if (!"smsip".equals(str)) {
            return super.onSipError(str, sipError);
        } else {
            Log.i(LOG_TAG, "onSipError SMS caught is : service=" + str + " error=" + sipError);
            if (sipError.getCode() == 408 || sipError.getCode() == 708 || sipError.getCode() == 504) {
                Log.i(LOG_TAG, "SMS error : mCurPcscfIpIdx=" + this.mCurPcscfIpIdx + " mNumOfPcscfIp=" + this.mNumOfPcscfIp);
                if (this.mCurPcscfIpIdx == this.mNumOfPcscfIp) {
                    Log.i(LOG_TAG, "SMS Error caught state = " + this.mTask.getState());
                    if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                        Log.i(LOG_TAG, "SMSError stop pdn called : service=" + str);
                        this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                        resetPcscfList();
                    }
                } else {
                    Log.i(LOG_TAG, "SMS Error trying on next pcscf is");
                    removeCurrentPcscfAndInitialRegister(true);
                }
            }
        }
        return sipError;
    }

    public boolean isThrottled() {
        if (this.mIsPermanentStopped || this.mRegiAt > SystemClock.elapsedRealtime()) {
            return true;
        }
        if (isPse911Prohibited()) {
            Log.i(LOG_TAG, "release blocking Ps e911 as throttling is expired for 503 error");
            this.mPse911Prohibited = false;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public Set<String> applyVoPsPolicy(Set<String> set) {
        if (set == null) {
            return new HashSet();
        }
        if (this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs != VoPsIndication.NOT_SUPPORTED) {
            return set;
        }
        Log.i(LOG_TAG, "applyVoPsPolicy: not support VoPS, filtering all services.");
        return new HashSet();
    }

    public void onVolteSettingChanged() {
        boolean z = getVoiceTechType() == 0;
        Log.i(LOG_TAG, "onVolteSettingChanged: " + z);
        if (z) {
            if (this.mRegHandler.hasVolteSettingOffEvent()) {
                this.mRegHandler.removeVolteSettingOffEvent();
            }
            notifyVoLteOnOffToRil(z);
        } else if (!this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
            notifyVoLteOnOffToRil(z);
        }
    }

    public void notifyVoLteOnOffToRil(boolean z) {
        Log.i(LOG_TAG, "notifyVoLteOnOffToRil: " + z);
        sendRawRequestToTelephony(this.mContext, buildVolteOnOffOemHookCmd(z));
    }

    public Set<String> filterService(Set<String> set, int i) {
        Set hashSet = new HashSet();
        HashSet hashSet2 = new HashSet(set);
        if (isImsDisabled()) {
            return new HashSet();
        }
        boolean z = true;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) != 1) {
            z = false;
        }
        if (z) {
            hashSet.addAll(servicesByImsSwitch(ImsProfile.getVoLteServiceList()));
            if (!hashSet.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
            }
        }
        if (i == 13 && this.mTask.getProfile().getPdnType() == 11) {
            hashSet = applyVoPsPolicy(hashSet);
            if (hashSet.isEmpty()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                return hashSet;
            }
        }
        if (!isVideoCallEnabled()) {
            removeService(hashSet2, "mmtel-video", "VideoCall disable.");
        }
        if (hashSet2.size() > 0) {
            hashSet2.retainAll(hashSet);
        }
        return hashSet2;
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int i) {
        if (i == 18 || getVoiceTechType(this.mPhoneId) == 0) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: volte disabled");
        if (this.is5GCapable && OmcCode.isDCMOmcCode() && i != 0 && !this.mTelephonyManager.isNetworkRoaming(SimUtil.getSubId(this.mPhoneId))) {
            Log.i(LOG_TAG, "isReadyToRegister: volte disabled, but force enable it on domestic n/w");
            ImsConstants.SystemSettings.setVoiceCallType(this.mContext, 0, this.mPhoneId);
        }
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return false;
    }

    public boolean determineDeRegistration(int i, int i2) {
        if (i != 0 || this.mTelephonyManager.getCallState() == 0) {
            return super.determineDeRegistration(i, i2);
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isNeedToDeRegistration: Block deregister for VoLte task during emergency call.");
        return true;
    }

    /* access modifiers changed from: protected */
    public int getVoiceTechType() {
        forceTurnOnVoLteWhenMenuRemoved();
        return super.getVoiceTechType();
    }
}
