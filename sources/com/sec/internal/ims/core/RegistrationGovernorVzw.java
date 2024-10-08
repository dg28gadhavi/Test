package com.sec.internal.ims.core;

import android.content.Context;
import android.content.Intent;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.sec.epdg.EpdgManager;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.NvConfiguration;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipErrorVzw;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DmProfileLoader;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class RegistrationGovernorVzw extends RegistrationGovernorBase {
    protected static final long DEFAULT_RETRY_AFTER_MS = 30000;
    protected static final int DEFAULT_TIMS_TIMER_SEC = 120;
    protected static final int DELAY_FOR_CDMALESS_MODEL = 6;
    private static final int DELAY_FOR_CDMA_HANDOVER = 3;
    protected static final String INTENT_ACTION_TRIGGER_OMADM_TREE_SYNC = "com.samsung.sdm.START_DM_SYNC_SESSION";
    private static final String LOG_TAG = "RegiGvnVzw";
    protected static final long[] REG_RETRY_TIME_MS = {0, 30000, 30000, SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF, 120000, 480000, 900000};
    protected CallSnapshot mCallSnapshot = new CallSnapshot();
    protected boolean mDmLastEabEnabled = false;
    protected boolean mDmLastLvcEnabled = false;
    protected boolean mDmLastVceEnabled = false;
    protected boolean mDmLastVolteEnabled = false;
    protected int mDmTimsTimerInSec;
    protected boolean mDmVolteNodeUpdated = false;
    protected boolean mHasPendingDeregistration = false;
    protected boolean mHasPendingOmadmUpdate = false;
    protected boolean mHasPendingReregistration = false;
    protected boolean mIsInviteForbidden = false;
    protected boolean mOverrideEpdgCellularPref = false;
    protected int mPdnType;

    /* access modifiers changed from: package-private */
    public boolean isCallTypeVideo(int i) {
        return i == 2 || i == 4 || i == 3;
    }

    public RegistrationGovernorVzw(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        String str;
        this.mPdnType = registerTask.getProfile().getPdnType();
        this.mDmLastVolteEnabled = DmConfigHelper.readSwitch(this.mContext, "mmtel", true, this.mPhoneId);
        if ("LRA".equals(OmcCode.get()) && !ImsUtil.isCdmalessModel(this.mContext, this.mPhoneId)) {
            SimpleEventLog eventLog = registrationManagerInternal.getEventLog();
            int i = this.mPhoneId;
            eventLog.logAndAdd(i, "Sync SMS_OVER_IP based on VOLTE_ENABLED=0 ONLY FOR hVoLTE models" + this.mDmLastVolteEnabled);
            IMSLog.c(LogClass.LRA_OOB_SMSIP_OFF, this.mPhoneId + ",SMS_OVER_IP" + this.mDmLastVolteEnabled);
            NvConfiguration.setSmsIpNetworkIndi(this.mContext, this.mDmLastVolteEnabled, this.mPhoneId);
        }
        this.mDmLastLvcEnabled = DmConfigHelper.readSwitch(this.mContext, "mmtel-video", true, this.mPhoneId);
        this.mDmLastEabEnabled = DmConfigHelper.readSwitch(this.mContext, SipMsg.EVENT_PRESENCE, true, this.mPhoneId);
        this.mDmLastVceEnabled = readVCEConfigValue(context);
        this.mDmTimsTimerInSec = readDmTimsTimer(context);
        Log.i(LOG_TAG, "RegistrationGovernorVzw: mDmLastVceEnabled[" + this.mDmLastVceEnabled + "]");
        registerTask.getProfile().setVceConfigEnabled(this.mDmLastVceEnabled);
        try {
            if (this.mDmLastVolteEnabled) {
                ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 1);
            } else {
                ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 0);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if ("BAE1000000000000".equalsIgnoreCase(iTelephonyManager.getGroupIdLevel1())) {
            try {
                ImsConstants.SystemSettings.setVoiceCallType(context, 0, this.mPhoneId);
                IMSLog.c(LogClass.FKR_VOLTE_FORCED_ON, this.mPhoneId + ",FKR VLT ON");
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }
        } else if (ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId)) {
            forceTurnOnVoLteWhenMenuRemoved();
        }
        this.mPcoType = RegistrationGovernor.PcoType.PCO_DEFAULT;
        updateVolteState();
        StringBuilder sb = new StringBuilder();
        sb.append(this.mPhoneId);
        sb.append(",");
        String str2 = "1";
        sb.append(this.mDmLastVolteEnabled ? str2 : "0");
        sb.append(",");
        sb.append(this.mDmLastLvcEnabled ? str2 : "0");
        sb.append(",");
        if (this.mDmLastEabEnabled) {
            str = str2;
        } else {
            str = "0";
        }
        sb.append(str);
        sb.append(",");
        sb.append(!this.mDmLastVceEnabled ? "0" : str2);
        IMSLog.c(LogClass.VZW_OMADM_VALUES, sb.toString());
    }

    private boolean readVCEConfigValue(Context context) {
        String str = OmcCode.get();
        return ("VZW".equals(str) || "VPP".equals(str)) && NvConfiguration.get(context, "VCE_CONFIG", "0").equals("1");
    }

    private int readDmTimsTimer(Context context) {
        return DmConfigHelper.readInt(context, ConfigConstants.ConfigPath.OMADM_VZW_TIMS_TIMER, 120, this.mPhoneId).intValue();
    }

    /* access modifiers changed from: protected */
    public boolean checkEpdgEvent(int i) {
        if (i != 18 || this.mPdnController.isEpdgConnected(this.mPhoneId) || !this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask)) {
            return true;
        }
        this.mTask.setKeepPdn(true);
        Log.i(LOG_TAG, "EPDG is not actually connected");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus(int i) {
        int i2 = this.mRegMan.getNetworkEvent(this.mPhoneId).network;
        if (!isSVLTEDevice()) {
            if (this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0) {
                if (this.mTask.getRegistrationRat() == 13 && TelephonyManagerExt.getNetworkClass(i2) == 2 && this.mTelephonyManager.isNetworkRoaming()) {
                    int i3 = this.mPhoneId;
                    IMSLog.i(LOG_TAG, i3, "checkCallStatus: Keep going IMS deregistration. LTE -> RAT " + i);
                } else if (this.mTask.getRegistrationRat() == 20 && i == 13) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "checkCallStatus: EPSFB. Need re-registration.");
                } else if (!ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId) || this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "TelephonyCallStatus is not idle");
                    return false;
                } else {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "Call status is not idle but CDMA-less should allow this.");
                }
            }
            return true;
        } else if (this.mVsm.getSessionCount(this.mPhoneId) <= 0 || this.mVsm.hasEmergencyCall(this.mPhoneId)) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkNetworkEvent(int i) {
        if (!this.mHasPendingOmadmUpdate) {
            return true;
        }
        Log.i(LOG_TAG, "mHasPendingOmadmUpdate is enabled");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ONGOING_OTA.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkVowifiSetting(int i) {
        if (!this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONFIGURED) || i != 18 || this.mRegMan.isVoWiFiSupported(this.mPhoneId)) {
            return true;
        }
        Log.i(LOG_TAG, "VoWiFi feature is not enabled");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.CSC_DISABLED.getCode());
        return false;
    }

    public boolean isReadyToRegister(int i) {
        return checkEmergencyStatus() || (checkCbrsOffloadingStatus() && checkNetworkEvent(i) && checkCallStatus(i) && checkRegiStatus() && checkVowifiSetting(i) && checkEpdgEvent(i));
    }

    private boolean checkCbrsOffloadingStatus() {
        if (!SimUtil.isCctChaCbrsMsoSim(this.mPhoneId)) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "checkCbrsOffloadingStatus: REGISTRATION avoided for MSO eSIM");
        return false;
    }

    public void onRegistrationDone() {
        super.onRegistrationDone();
        RegisterTask registerTask = this.mTask;
        registerTask.mKeepPdn = false;
        stopTimsEstablishTimer(registerTask, RegistrationConstants.REASON_REGISTERED);
        if (!this.mCallSnapshot.isEmpty()) {
            onCallStatus(this.mCallSnapshot.mEvent, this.mCallSnapshot.mError, this.mCallSnapshot.mCallType);
            this.mCallSnapshot.clear();
        }
    }

    public boolean isThrottled() {
        if (!this.mDiscardCurrentNetwork) {
            return super.isThrottled();
        }
        Log.i(LOG_TAG, "Under discard current network. Do not try IMS registration.");
        return true;
    }

    public void onRegistrationTerminated(SipError sipError, long j, boolean z) {
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        eventLog.logAndAdd("onRegistrationTerminated: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j);
        if (!this.mCallSnapshot.isEmpty()) {
            Log.i(LOG_TAG, "handle call snapshot");
            onCallStatus(this.mCallSnapshot.mEvent, this.mCallSnapshot.mError, this.mCallSnapshot.mCallType);
            this.mCallSnapshot.clear();
        }
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        startRetryTimer(z ? SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF : 1000);
    }

    public void onRegistrationError(SipError sipError, long j, boolean z) {
        Log.e(LOG_TAG, "onRegistrationError: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j);
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        StringBuilder sb = new StringBuilder();
        sb.append("onRegistrationError :");
        sb.append(sipError);
        sb.append(", fail count : ");
        sb.append(this.mFailureCounter);
        eventLog.logAndAdd(sb.toString());
        if (!this.mCallSnapshot.isEmpty()) {
            Log.i(LOG_TAG, "handle call snapshot");
            onCallStatus(this.mCallSnapshot.mEvent, this.mCallSnapshot.mError, this.mCallSnapshot.mCallType);
            this.mCallSnapshot.clear();
        }
        if (j < 0) {
            j = 0;
        }
        if (!ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId) || getPcoType() != RegistrationGovernor.PcoType.PCO_SELF_ACTIVATION) {
            this.mFailureCounter++;
            this.mCurPcscfIpIdx = (this.mCurPcscfIpIdx + 1) % this.mNumOfPcscfIp;
            if (this.mTask.getProfile().hasEmergencySupport()) {
                if (SipErrorBase.SIP_TIMEOUT.equals(sipError) && this.mFailureCounter < 2) {
                    this.mRegHandler.sendTryRegister(this.mPhoneId);
                }
            } else if (SipErrorBase.USE_PROXY.equals(sipError)) {
                Log.e(LOG_TAG, "onRegistrationError: start from 1st P-CSCF.");
                this.mCurPcscfIpIdx = 0;
                startRetryTimer(j);
            } else if (SipErrorBase.BAD_REQUEST.equals(sipError) || SipErrorBase.PAYMENT_REQUIRED.equals(sipError)) {
                if (z) {
                    this.mIsPermanentStopped = true;
                } else if (this.mFailureCounter > 1) {
                    this.mIsPermanentStopped = true;
                } else {
                    if (j == 0) {
                        j = 30000;
                    }
                    startRetryTimer(j);
                }
            } else if (SipErrorBase.isImsForbiddenError(sipError) || SipErrorBase.NOT_FOUND.getCode() == sipError.getCode()) {
                int i = this.mCurPcscfIpIdx;
                if (i == 0 && this.mCurImpu == 1) {
                    this.mIsPermanentStopped = true;
                    this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                    Log.i(LOG_TAG, "onRegistrationError: Failed for all PCSCFs with IMSI_BASED");
                    return;
                }
                if (i == 0) {
                    this.mRegMan.getEventLog().logAndAdd("try regi with IMSI for next Registration");
                    this.mCurImpu = 1;
                }
                if (j == 0) {
                    j = 30000;
                }
                startRetryTimer(j);
            } else {
                if (this.mFailureCounter > 2 && this.mCurPcscfIpIdx == 0) {
                    Log.e(LOG_TAG, "onRegistrationError: all PCSCF failed to Regi");
                    if (!this.mRegMan.getCsfbSupported(this.mPhoneId) && ((ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId) || this.mTelephonyManager.isNetworkRoaming()) && this.mTimEshtablishTimeout != null)) {
                        Log.e(LOG_TAG, "onRegistrationError: Discard current N/W. CSFB is unavailable");
                        this.mDiscardCurrentNetwork = true;
                    }
                }
                if (z) {
                    this.mFailureCounter++;
                }
                if (j == 0) {
                    j = getWaitTime();
                }
                startRetryTimer(j);
            }
        } else {
            this.mRegMan.getEventLog().logAndAdd("RegiGvnVzw: Discard current network immediately when PCO=5");
            this.mDiscardCurrentNetwork = true;
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> applyVoPsPolicy(Set<String> set) {
        if (set == null) {
            return new HashSet();
        }
        return this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED ? applyVopsNotSupported(set) : set;
    }

    private Set<String> applyVopsNotSupported(Set<String> set) {
        if (this.mTask.getRegistrationRat() == 20) {
            this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "applyVopsNotSupported: VoPS Not supported but keep current registration.");
            return set;
        } else if (!ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId) || this.mTelephonyManager.isNetworkRoaming()) {
            removeService(set, "mmtel-video", "VoPS Off");
            removeService(set, "mmtel", "VoPS Off");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_VOPS_OFF.getCode());
            return set;
        } else {
            Log.i(LOG_TAG, "CDMALess and VoPS not Supported. Disable VZW LTE PLMN.");
            return new HashSet();
        }
    }

    public void onPublishError(SipError sipError) {
        if (sipError.equalsWithStrict(SipErrorBase.USER_NOT_REGISTERED) || sipError.equalsWithStrict(SipErrorBase.USER_NOT_REGISTERED2)) {
            this.mTask.setReason("Publish Error. ReRegister..");
            this.mRegMan.sendReRegister(this.mTask);
        } else if (ImsCallUtil.isImsOutageError(sipError)) {
            this.mTask.setDeregiReason(45);
            this.mRegMan.deregister(this.mTask, true, false, "Publish Error. DeRegister..");
        }
    }

    public void onSubscribeError(int i, SipError sipError) {
        Log.e(LOG_TAG, "onSubscribeError: state " + this.mTask.getState() + ", error " + sipError + ", event " + i);
        if (sipError.equalsWithStrict(SipErrorBase.USER_NOT_REGISTERED) || sipError.equalsWithStrict(SipErrorBase.USER_NOT_REGISTERED2)) {
            this.mTask.setReason("Subscribe Error. ReRegister..");
            this.mRegMan.sendReRegister(this.mTask);
        } else if (!SipErrorBase.isImsOutageError(sipError)) {
        } else {
            if (!this.mHasVoLteCall) {
                this.mTask.setDeregiReason(44);
                this.mRegMan.deregister(this.mTask, true, false, "Subscribe Error. DeRegister..");
                return;
            }
            this.mHasPendingDeregistration = true;
            if (this.mTask.getImsRegistration() != null) {
                this.mTask.getImsRegistration().setProhibited(true);
            }
        }
    }

    public void onTimsTimerExpired() {
        super.onTimsTimerExpired();
        boolean isNetworkRoaming = this.mTelephonyManager.isNetworkRoaming();
        boolean csfbSupported = this.mRegMan.getCsfbSupported(this.mPhoneId);
        int registrationRat = this.mTask.getRegistrationRat();
        if ((!isNetworkRoaming || !csfbSupported) && registrationRat != 18) {
            resetRetry();
            stopRetryTimer();
            this.mDiscardCurrentNetwork = true;
            return;
        }
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, this.mTask, String.format(Locale.US, "onTimsTimerExpired: Continue retrying. Roaming [%s], CSFB supported [%s], Regi RAT [%d]", new Object[]{Boolean.valueOf(isNetworkRoaming), Boolean.valueOf(csfbSupported), Integer.valueOf(registrationRat)}));
    }

    /* access modifiers changed from: package-private */
    public boolean needReRegiOnCallStatusChanged(int i, IRegistrationGovernor.CallEvent callEvent, int i2) {
        if (this.mTask.getRegistrationRat() == 18) {
            if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_ESTABLISHED) {
                if (this.mTask.getImsRegistration() != null && !this.mTask.getImsRegistration().hasService("mmtel")) {
                    if (isCallTypeVideo(i2) || getVoiceTechType(i) == 0) {
                        this.mOverrideEpdgCellularPref = true;
                        Log.i(LOG_TAG, "needReRegiOnCallStatusChanged, re-regi case");
                        return true;
                    }
                    Log.i(LOG_TAG, "needReRegiOnCallStatusChanged, VOWIFI or VoLTE disabled case, no re-regi");
                    return false;
                }
            } else if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END && this.mOverrideEpdgCellularPref) {
                IVolteServiceModule iVolteServiceModule = this.mVsm;
                if (iVolteServiceModule != null) {
                    EpdgManager epdgManager = iVolteServiceModule.getEpdgManager();
                    if (epdgManager == null) {
                        Log.i(LOG_TAG, "Can not find epdgManager");
                    } else if (epdgManager.isPossibleW2LHOAfterCallEndBySim(i)) {
                        Log.i(LOG_TAG, "W2L indication from EpdgManager will be coming.");
                        return false;
                    } else {
                        this.mOverrideEpdgCellularPref = false;
                        if (this.mTask.getRegistrationRat() == 18) {
                            if (!isVoiceOverWifiPreferred()) {
                                Log.i(LOG_TAG, "re-regi case");
                                return true;
                            }
                            Log.i(LOG_TAG, "VoWiFi pref. re-regi not required");
                            return false;
                        }
                    }
                } else {
                    Log.e(LOG_TAG, "VolteServiceModule is null");
                }
                if (this.mTelephonyManager.getNetworkType() != 13 || this.mPdnController.getVopsIndication(i) == VoPsIndication.NOT_SUPPORTED) {
                    Log.i(LOG_TAG, "needReRegiOnCallStatusChanged, re-regi case");
                    return true;
                }
                Log.i(LOG_TAG, "needReRegiOnCallStatusChanged, LTE HO case no need for re-regi");
                return false;
            }
        } else if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_ESTABLISHED) {
            this.mOverrideEpdgCellularPref = true;
        } else if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END) {
            this.mOverrideEpdgCellularPref = false;
        }
        return false;
    }

    public void onCallStatus(IRegistrationGovernor.CallEvent callEvent, SipError sipError, int i) {
        Log.i(LOG_TAG, "onCallStatus: event=" + callEvent + " error=" + sipError);
        IUserAgent userAgent = this.mTask.getUserAgent();
        if (userAgent == null || !userAgent.isRegistering()) {
            if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END) {
                if (this.mHasPendingDeregistration) {
                    this.mTask.setDeregiReason(47);
                    RegistrationManagerInternal registrationManagerInternal = this.mRegMan;
                    RegisterTask registerTask = this.mTask;
                    registrationManagerInternal.deregister(registerTask, true, registerTask.mKeepPdn, "onCallStatus: process pending deregistration.");
                    this.mHasPendingDeregistration = false;
                }
                if (this.mHasPendingReregistration) {
                    Log.i(LOG_TAG, "onCallStatus: process pending updateRegistration.");
                    this.mRegMan.addPendingUpdateRegistration(this.mTask, 0);
                    this.mHasPendingReregistration = false;
                }
                this.mHasVoLteCall = false;
            } else {
                super.onCallStatus(callEvent, sipError, i);
            }
            if (this.mRegMan.isVoWiFiSupported(this.mPhoneId) && needReRegiOnCallStatusChanged(this.mPhoneId, callEvent, i)) {
                this.mRegMan.sendReRegister(this.mTask);
                return;
            }
            return;
        }
        Log.i(LOG_TAG, "onCallStatus: defer call status event to registration done");
        this.mCallSnapshot.setCallSnapshot(callEvent, sipError, i);
    }

    private SipError onSipError_MmtelVoice(String str, SipError sipError) {
        boolean isCdmalessEnabled = ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId);
        if (SipErrorVzw.FORBIDDEN_ORIG_USER_NOT_REGISTERED.equals(sipError) || SipErrorVzw.FORBIDDEN_ORIG_USER_NOT_REGISTERED2.equals(sipError)) {
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, true, true, 0, "403 Forbidden");
            return sipError;
        } else if (!SipErrorVzw.FORBIDDEN_USER_NOT_AUTHORIZED_FOR_SERVICE.equals(sipError) || isCdmalessEnabled) {
            int i = 0;
            if (SipErrorBase.isImsOutageError(sipError)) {
                if (!this.mHasVoLteCall) {
                    if (!isCdmalessEnabled) {
                        i = 3;
                    }
                    RegisterTask registerTask = this.mTask;
                    registerTask.mKeepPdn = true;
                    registerTask.setDeregiReason(43);
                    RegistrationManagerInternal registrationManagerInternal = this.mRegMan;
                    RegisterTask registerTask2 = this.mTask;
                    registrationManagerInternal.deregister(registerTask2, true, registerTask2.mKeepPdn, i * 1000, "503 Service Unavailable: IMS Outage for voice service");
                    return new SipError(NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_NOT_REQUIRED, sipError.getReason());
                }
                RegisterTask registerTask3 = this.mTask;
                registerTask3.mKeepPdn = true;
                this.mHasPendingDeregistration = true;
                if (registerTask3.getImsRegistration() != null) {
                    this.mTask.getImsRegistration().setProhibited(true);
                }
                return SipErrorBase.FORBIDDEN;
            } else if (ImsCallUtil.isTimerVzwExpiredError(sipError) || 1702 == sipError.getCode() || 2507 == sipError.getCode()) {
                this.mTask.setDeregiReason(49);
                this.mRegMan.deregister(this.mTask, true, true, "vzw timer expired");
                return sipError;
            } else {
                SipError sipError2 = SipErrorBase.SIP_INVITE_TIMEOUT;
                if (sipError2.equals(sipError) || SipErrorBase.SIP_TIMEOUT.equals(sipError)) {
                    if (!this.mHasVoLteCall || (!isCdmalessEnabled && (!this.mTelephonyManager.isNetworkRoaming() || !sipError2.equals(sipError)))) {
                        removeCurrentPcscfAndInitialRegister(false);
                        return sipError;
                    }
                    this.mTask.mKeepPdn = true;
                    this.mHasPendingDeregistration = true;
                    return sipError;
                } else if (isCdmalessEnabled && SipErrorBase.PRECONDITION_FAILURE.equals(sipError)) {
                    this.mTask.setDeregiReason(43);
                    RegistrationManagerInternal registrationManagerInternal2 = this.mRegMan;
                    RegisterTask registerTask4 = this.mTask;
                    registrationManagerInternal2.deregister(registerTask4, true, registerTask4.mKeepPdn, 6000, "VoLTE call setup failure");
                    return sipError;
                } else if (1125 != sipError.getCode()) {
                    return super.onSipError(str, sipError);
                } else {
                    this.mTask.setDeregiReason(54);
                    this.mRegMan.deregister(this.mTask, true, true, "EPSFB");
                    return sipError;
                }
            }
        } else {
            this.mRegMan.getEventLog().logAndAdd("Volte service will be disable for 403 Forbidden");
            this.mRegMan.setInvite403DisableService(true, this.mPhoneId);
            Intent intent = new Intent(INTENT_ACTION_TRIGGER_OMADM_TREE_SYNC);
            intent.setPackage(ImsConstants.Packages.PACKAGE_SDM);
            this.mContext.sendBroadcast(intent);
            this.mRegMan.sendReRegister(this.mTask);
            return sipError;
        }
    }

    public SipError onSipError(String str, SipError sipError) {
        Log.e(LOG_TAG, "onSipError: service=" + str + " error=" + sipError);
        if ("mmtel".equals(str)) {
            return onSipError_MmtelVoice(str, sipError);
        }
        if ("smsip".equals(str)) {
            if (!SipErrorBase.isImsOutageError(sipError)) {
                return super.onSipError(str, sipError);
            }
            if (this.mHasVoLteCall) {
                return sipError;
            }
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, true, true, "503 Service Unavailable: IMS Outage for SMS service request");
            return sipError;
        } else if (!SipErrorBase.isImsOutageError(sipError)) {
            return super.onSipError(str, sipError);
        } else {
            if (!this.mHasVoLteCall) {
                this.mTask.setDeregiReason(43);
                this.mRegMan.deregister(this.mTask, true, false, "503 Service Unavailable: IMS Outage for Non-voice request");
                return sipError;
            }
            this.mHasPendingDeregistration = true;
            if (this.mTask.getImsRegistration() == null) {
                return sipError;
            }
            this.mTask.getImsRegistration().setProhibited(true);
            return sipError;
        }
    }

    /* access modifiers changed from: protected */
    public long getWaitTime() {
        int i = this.mFailureCounter;
        long[] jArr = REG_RETRY_TIME_MS;
        int min = Math.min(i, jArr.length - 1);
        return jArr[min] + (min == 3 ? ((long) ((int) (Math.random() * 15.0d))) * 1000 : 0);
    }

    public void checkProfileUpdateFromDM(boolean z) {
        Log.i(LOG_TAG, "checkProfileUpdateFromDM()");
        RegisterTask registerTask = this.mTask;
        registerTask.setProfile(DmProfileLoader.getProfile(this.mContext, registerTask.getProfile(), this.mPhoneId));
    }

    /* access modifiers changed from: package-private */
    public boolean isVoiceOverWifiEnabled() {
        boolean isEnabled = VowifiConfig.isEnabled(this.mContext, this.mPhoneId);
        boolean isNetworkRoaming = this.mTelephonyManager.isNetworkRoaming();
        if (isNetworkRoaming && isEnabled) {
            isEnabled = VowifiConfig.getRoamPrefMode(this.mContext, 0, this.mPhoneId) == 1;
        }
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "VoWiFi pref: " + isEnabled + ", isRoaming: " + isNetworkRoaming);
        return isEnabled;
    }

    /* access modifiers changed from: package-private */
    public boolean isVoiceOverWifiPreferred() {
        boolean isVoiceOverWifiEnabled = isVoiceOverWifiEnabled();
        boolean isCdmaAvailableForVoice = this.mRegMan.isCdmaAvailableForVoice(this.mPhoneId);
        Log.i(LOG_TAG, "isVoiceOverWifiPreferred: isVowifiPreferred [" + isVoiceOverWifiEnabled + "] isCdmaAvailableForVoice : [" + isCdmaAvailableForVoice + "]");
        if (this.mTelephonyManager.isNetworkRoaming()) {
            return isVoiceOverWifiEnabled;
        }
        return isVoiceOverWifiEnabled && !isCdmaAvailableForVoice;
    }

    public Set<String> filterService(Set<String> set, int i) {
        String str;
        int i2 = i;
        Set<String> filterService = super.filterService(set, i);
        if (filterService.isEmpty()) {
            return new HashSet();
        }
        boolean z = true;
        if (SemSystemProperties.getInt(ImsConstants.SystemProperties.IMS_TEST_MODE_PROP, 0) == 1) {
            Log.i(LOG_TAG, "by VZW IMS_TEST_MODE_PROP - remove all service");
            return new HashSet();
        }
        if (this.mTask.getProfile().hasEmergencySupport() && SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) < 29) {
            removeService(filterService, "smsip", "by unsupported E911 over SMS");
        }
        Set<String> applyMmtelUserSettings = applyMmtelUserSettings(filterService, i2);
        if (i2 == 13 && !this.mRegMan.getVolteAllowedWithDsac()) {
            removeService(applyMmtelUserSettings, "mmtel", "by DSAC feature");
            removeService(applyMmtelUserSettings, "mmtel-video", "by DSAC feature");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_DSAC.getCode());
        }
        boolean z2 = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.Registration.KEEP_MSISDN_VALIDATION, true);
        if (this.mCurImpu == 1 || (!this.mTelephonyManager.validateMsisdn(SimUtil.getSubId(this.mPhoneId)) && z2)) {
            if (ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId)) {
                removeService(applyMmtelUserSettings, "mmtel-video", "CDMALess IMSI based");
            } else {
                removeService(applyMmtelUserSettings, "mmtel-video", "by limited regi");
                removeService(applyMmtelUserSettings, "mmtel", "by limited regi");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_LIMITED_MODE.getCode());
            }
        }
        if (this.mRegMan.isInvite403DisabledService(this.mPhoneId)) {
            removeService(applyMmtelUserSettings, "mmtel-video", "Invite 403");
            removeService(applyMmtelUserSettings, "mmtel", "Invite 403");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_INVITE_403.getCode());
        }
        Set<String> applySSACPolicy = applySSACPolicy(i2, applyMmtelUserSettings);
        IVolteServiceModule iVolteServiceModule = this.mVsm;
        boolean z3 = iVolteServiceModule != null && iVolteServiceModule.getVideoCallCount(this.mPhoneId) > 0;
        SlotBasedConfig instance = SlotBasedConfig.getInstance(this.mPhoneId);
        boolean z4 = instance != null && instance.isDataUsageExceeded();
        boolean isNetworkRoaming = this.mTelephonyManager.isNetworkRoaming();
        boolean isMobileDataOn = NetworkUtil.isMobileDataOn(this.mContext);
        if (isNetworkRoaming && isMobileDataOn) {
            isMobileDataOn = Settings.Global.getInt(this.mContext.getContentResolver(), "data_roaming", 0) == 1;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "DataAllowed: romaing [" + isNetworkRoaming + "]: " + isMobileDataOn);
        if ((i2 != 18 && (!isMobileDataOn || z4)) || !this.mDmLastVolteEnabled || !this.mDmLastEabEnabled) {
            if (z3) {
                if (this.mTask.getImsRegistration() == null || this.mTask.getImsRegistration().hasService("mmtel-video")) {
                    Log.i(LOG_TAG, "by EAB false but activated VT call is exist.");
                    this.mHasPendingReregistration = true;
                }
            } else if (!applySSACPolicy.isEmpty()) {
                if (!this.mDmLastVolteEnabled || !this.mDmLastEabEnabled) {
                    str = "by DM : volte(" + this.mDmLastVolteEnabled + "), eab(" + this.mDmLastEabEnabled + ")";
                } else {
                    str = !isMobileDataOn ? "by mobile data off" : z4 ? "by data limit exceed" : "remove mmtel-video";
                }
                removeService(applySSACPolicy, "mmtel-video", str);
            }
        }
        if (!this.mDmLastVolteEnabled) {
            Log.i(LOG_TAG, "by volteEnabled false - presence");
            removeService(applySSACPolicy, SipMsg.EVENT_PRESENCE, "by volteEnabled false");
        }
        if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
            Context context = this.mContext;
            int i3 = this.mPhoneId;
            List<String> rcsEnabledServiceList = RcsConfigurationHelper.getRcsEnabledServiceList(context, i3, ConfigUtil.getRcsProfileWithFeature(context, i3, this.mTask.getProfile()));
            for (String str2 : ImsProfile.getRcsServiceList()) {
                if (!rcsEnabledServiceList.contains(str2)) {
                    removeService(applySSACPolicy, str2, "Disable from ACS");
                }
            }
            if (!rcsEnabledServiceList.contains("im")) {
                removeService(applySSACPolicy, ServiceConstants.SERVICE_CHATBOT_COMMUNICATION, "Chatbot disabled in autoconfig");
            }
            if (ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) != 1) {
                z = false;
            }
            if ((!ImsUtil.isSingleRegiAppConnected(this.mPhoneId) && !z) || (isNetworkRoaming && i2 != 18)) {
                for (String str3 : ImsProfile.getRcsServiceList()) {
                    if (!SipMsg.EVENT_PRESENCE.equals(str3)) {
                        removeService(applySSACPolicy, str3, "Roaming:" + isNetworkRoaming);
                    }
                }
            }
            if (!RcsUtils.DualRcs.isRegAllowed(this.mContext, this.mPhoneId)) {
                for (String str4 : ImsProfile.getRcsServiceList()) {
                    if (!SipMsg.EVENT_PRESENCE.equals(str4)) {
                        removeService(applySSACPolicy, str4, "No DualRcs");
                    }
                }
            }
        }
        if (isNetworkRoaming) {
            applySSACPolicy = applyCsfbSupported(applySSACPolicy);
        }
        if (!(instance == null || this.mTask.getProfile().getTtyType() == 2 || this.mTask.getProfile().getTtyType() == 4 || !instance.getTTYMode())) {
            removeService(applySSACPolicy, "mmtel-video", "TTY ON");
            removeService(applySSACPolicy, "mmtel", "TTY ON");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_CS_TTY.getCode());
        }
        return applySSACPolicy;
    }

    public void startOmadmProvisioningUpdate() {
        this.mHasPendingOmadmUpdate = true;
        setRadioPower(false);
        this.mRegHandler.sendFinishOmadmProvisioningUpdate(this.mTask, 10000);
    }

    public void finishOmadmProvisioningUpdate() {
        this.mHasPendingOmadmUpdate = false;
        setRadioPower(true);
    }

    public void resetRetry() {
        Log.i(LOG_TAG, "resetRetry()");
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        this.mRegiAt = 0;
        if (this.mPcoType != RegistrationGovernor.PcoType.PCO_SELF_ACTIVATION) {
            this.mCurImpu = 0;
        }
    }

    public void releaseThrottle(int i) {
        if (i == 4 || i == 1) {
            this.mIsPermanentStopped = false;
            this.mDiscardCurrentNetwork = false;
            this.mPcoType = RegistrationGovernor.PcoType.PCO_DEFAULT;
            resetRetry();
            stopRetryTimer();
        } else if (i == 5) {
            resetRetry();
            stopRetryTimer();
        } else if (i == 9 || i == 6) {
            this.mDiscardCurrentNetwork = false;
        }
        if (!this.mIsPermanentStopped) {
            Log.i(LOG_TAG, "releaseThrottle: case by " + i);
        }
    }

    public void onPdnRequestFailed(PdnFailReason pdnFailReason, int i) {
        super.onPdnRequestFailed(pdnFailReason, i);
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "onPdnRequestFailed: " + pdnFailReason + ", counter: " + this.mPdnRejectCounter);
        if (isRetryLongerThanTims(pdnFailReason)) {
            notifyImsNotAvailableByPdnReject();
        } else if (DeviceUtil.isTablet() || !NetworkUtil.is3gppPsVoiceNetwork(this.mTask.getRegistrationRat()) || ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId) || this.mTelephonyManager.isNetworkRoaming() || (DeviceUtil.isApAssistedMode() && i != 1)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onPdnRequestFailed: Do not notify");
        } else {
            if (PdnFailReason.PDN_THROTTLED != pdnFailReason) {
                int i3 = this.mPdnRejectCounter + 1;
                this.mPdnRejectCounter = i3;
                if (i3 < 2) {
                    return;
                }
            }
            notifyImsNotAvailableByPdnReject();
        }
    }

    private boolean isRetryLongerThanTims(PdnFailReason pdnFailReason) {
        if (this.mTimEshtablishTimeout == null) {
            return false;
        }
        if (this.mTask.getRegistrationRat() != 20 && PdnFailReason.PDN_THROTTLED != pdnFailReason) {
            return false;
        }
        long nextRetryTime = this.mTelephonyManager.getNextRetryTime();
        long timeout = this.mTimEshtablishTimeout.getTimeout();
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "isRetryLongerThanTims: Next retry time: " + nextRetryTime + ", TIMS timeout: " + timeout);
        if (nextRetryTime > timeout) {
            return true;
        }
        return false;
    }

    private void notifyImsNotAvailableByPdnReject() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "notifyImsNotAvailableByPdnReject");
        this.mRegMan.notifyImsNotAvailable(this.mTask, true);
        this.mPdnRejectCounter = 0;
    }

    public void onConfigUpdated() {
        boolean readSwitch = DmConfigHelper.readSwitch(this.mContext, "mmtel", true, this.mPhoneId);
        boolean readSwitch2 = DmConfigHelper.readSwitch(this.mContext, "mmtel-video", true, this.mPhoneId);
        boolean readSwitch3 = DmConfigHelper.readSwitch(this.mContext, SipMsg.EVENT_PRESENCE, true, this.mPhoneId);
        boolean readVCEConfigValue = readVCEConfigValue(this.mContext);
        int readDmTimsTimer = readDmTimsTimer(this.mContext);
        Log.i(LOG_TAG, "onConfigUpdated: VOLTE_ENABLED [" + this.mDmLastVolteEnabled + "] -> [" + readSwitch + "]");
        if (readSwitch != this.mDmLastVolteEnabled) {
            this.mDmLastVolteEnabled = readSwitch;
            if (readSwitch) {
                ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 1);
            } else {
                ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 0);
            }
            if ("LRA".equals(this.mMno.getMatchedSalesCode(OmcCode.get())) && !ImsUtil.isCdmalessModel(this.mContext, this.mPhoneId)) {
                NvConfiguration.setSmsIpNetworkIndi(this.mContext, this.mDmLastVolteEnabled, this.mPhoneId);
                SimpleEventLog eventLog = this.mRegMan.getEventLog();
                int i = this.mPhoneId;
                eventLog.logAndAdd(i, "onConfigUpdated: Sync SMS_OVER_IP based on VOLTE_ENABLED" + this.mDmLastVolteEnabled);
                IMSLog.c(LogClass.LRA_SMSIP_OFF_OMADM, this.mPhoneId + ",SMS_OVER_IP:" + this.mDmLastVolteEnabled);
            }
            this.mRegMan.setInvite403DisableService(false, this.mPhoneId);
            if (this.mTelephonyManager.getCallState() != 0) {
                this.mDmVolteNodeUpdated = true;
            } else if (this.mTask.getRegistrationRat() == 13 || this.mTask.getRegistrationRat() == 14) {
                Log.i(LOG_TAG, "onConfigUpdated, need network detach/reattach");
                this.mHasPendingOmadmUpdate = true;
                this.mRegHandler.sendOmadmProvisioningUpdateStarted(this.mTask);
            } else {
                this.mTask.setDeregiReason(29);
                this.mRegMan.deregister(this.mTask, false, false, "profile updated");
            }
        }
        Log.i(LOG_TAG, "onConfigUpdated: VCE_CONFIG [" + this.mDmLastVceEnabled + "] -> [" + readVCEConfigValue + "]");
        if (readVCEConfigValue != this.mDmLastVceEnabled) {
            this.mDmLastVceEnabled = readVCEConfigValue;
            this.mTask.getProfile().setVceConfigEnabled(this.mDmLastVceEnabled);
            this.mRegMan.updateVceConfig(this.mTask, this.mDmLastVceEnabled);
        }
        Log.i(LOG_TAG, "onConfigUpdated: VZW_TIMS_TIMER [" + this.mDmTimsTimerInSec + "] -> [" + readDmTimsTimer + "]");
        if (this.mDmTimsTimerInSec != readDmTimsTimer) {
            this.mDmTimsTimerInSec = readDmTimsTimer;
        }
        if (this.mDmVolteNodeUpdated || this.mHasPendingOmadmUpdate || readSwitch2 != this.mDmLastLvcEnabled || readSwitch3 != this.mDmLastEabEnabled) {
            this.mDmLastLvcEnabled = readSwitch2;
            this.mDmLastEabEnabled = readSwitch3;
            ImsRegistry.getServiceModuleManager().notifyOmadmVolteConfigDone(this.mPhoneId);
        }
    }

    public void onTelephonyCallStatusChanged(int i) {
        super.onTelephonyCallStatusChanged(i);
        if (this.mDmVolteNodeUpdated && i == 0) {
            this.mDmVolteNodeUpdated = false;
            if (this.mTask.getRegistrationRat() == 13 || this.mTask.getRegistrationRat() == 14) {
                this.mRegHandler.sendOmadmProvisioningUpdateStarted(this.mTask);
                return;
            }
            this.mTask.setDeregiReason(7);
            this.mRegMan.deregister(this.mTask, false, false, "call state changed");
        }
    }

    public void onDeregistrationDone(boolean z) {
        super.onDeregistrationDone(z);
        if (this.mTask.getPdnType() != 11) {
            return;
        }
        if (this.mTask.getDeregiReason() == 2) {
            startTimsTimer(RegistrationConstants.REASON_IMS_PDN_REQUEST);
        } else if (this.mTask.getDeregiReason() == 76) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Local deregi done by SSAC. Discard current network!");
            this.mDiscardCurrentNetwork = true;
        }
    }

    public void onPdnConnecting(int i) {
        toggleTimsTimerByPdnTransport(i);
    }

    public void startTimsTimer(String str) {
        if (this.mTelephonyManager.isNetworkRoaming() || ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId)) {
            int i = this.mDmTimsTimerInSec;
            if (i == 9999) {
                Log.i(LOG_TAG, "ignore Tims timer for labtest");
                return;
            }
            if (i < 0 || i > 255) {
                Log.i(LOG_TAG, "startTimsTimer; Invalid DM value [" + this.mDmTimsTimerInSec + "] for Tims Timer. Use default value[120].");
                this.mDmTimsTimerInSec = 120;
            }
            startTimsEstablishTimer(this.mTask, ((long) this.mDmTimsTimerInSec) * 1000, str);
            return;
        }
        Log.i(LOG_TAG, "ignore Tims timer for hVoLTE device in VZW NW");
    }

    public void stopTimsTimer(String str) {
        stopTimsEstablishTimer(this.mTask, str);
    }

    /* access modifiers changed from: package-private */
    public Set<String> applySSACPolicy(int i, Set<String> set) {
        if (set == null) {
            return new HashSet();
        }
        if (isFullBarring(i)) {
            if (ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId) && !this.mRegMan.getCsfbSupported(this.mPhoneId)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "applySSACPolicy: CSFB is not available. Discard PLMN.");
                return new HashSet();
            } else if (!isSVLTEDevice()) {
                removeService(set, "mmtel-video", "by SSAC");
                removeService(set, "mmtel", "by SSAC");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_SSAC_BARRING.getCode());
            }
        }
        return set;
    }

    /* access modifiers changed from: package-private */
    public boolean isFullBarring(int i) {
        return i == 13 && !SlotBasedConfig.getInstance(this.mPhoneId).isSsacEnabled();
    }

    /* access modifiers changed from: protected */
    public Set<String> applyCsfbSupported(Set<String> set) {
        return (set.contains("mmtel") || this.mRegMan.getCsfbSupported(this.mPhoneId)) ? set : new HashSet();
    }

    /* access modifiers changed from: package-private */
    public void setRadioPower(boolean z) {
        Log.i(LOG_TAG, "setRadioPower [" + z + "]");
        this.mTelephonyManager.setRadioPower(z);
    }

    private boolean isSVLTEDevice() {
        String str = SemSystemProperties.get("ro.ril.svlte1x");
        if (str == null || str.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(str);
    }

    public boolean onUpdatedPcoInfo(int i, int i2) {
        if (i != 64) {
            return false;
        }
        RegistrationGovernor.PcoType fromType = RegistrationGovernor.PcoType.fromType(i2);
        Log.i(LOG_TAG, "onUpdatedPcoInfo: PCO Type: " + fromType);
        setPcoType(fromType);
        if (fromType == RegistrationGovernor.PcoType.PCO_SELF_ACTIVATION && ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId)) {
            this.mCurImpu = 1;
            Log.i(LOG_TAG, "set PREFERED_IMPU as IMSI_BASED");
        }
        return true;
    }

    public void resetPcoType() {
        this.mPcoType = RegistrationGovernor.PcoType.PCO_DEFAULT;
    }

    /* access modifiers changed from: protected */
    public Set<String> applyMmtelUserSettings(Set<String> set, int i) {
        if (set == null) {
            return new HashSet();
        }
        if (!(ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId) || i == 18 || getVoiceTechType(this.mPhoneId) == 0)) {
            removeService(set, "mmtel", "by voice type cs");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_USER_SETTINGS_OFF.getCode());
        }
        if (!set.contains("mmtel")) {
            removeService(set, "mmtel-video", "by no mmtel");
        }
        if (this.mRegMan.isVoWiFiSupported(this.mPhoneId) && i == 18) {
            boolean isVoiceOverWifiPreferred = isVoiceOverWifiPreferred();
            if (!this.mOverrideEpdgCellularPref) {
                if (!VowifiConfig.isEnabled(this.mContext, this.mPhoneId)) {
                    removeService(set, "mmtel", "by VoWiFi settings");
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_USER_SETTINGS_OFF.getCode());
                }
                if (!(this.mTelephonyManager.getVoiceNetworkType() == 13 && this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.SUPPORTED) && !isVoiceOverWifiPreferred) {
                    removeService(set, "mmtel", "by VowifiPreferred");
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_USER_SETTINGS_OFF.getCode());
                }
            }
        }
        return set;
    }

    public boolean isLocationInfoLoaded(int i) {
        if (i != 18 || this.mTask.getProfile().getSupportedGeolocationPhase() < 2 || !this.mRegMan.isVoWiFiSupported(this.mPhoneId)) {
            return true;
        }
        IMSLog.e(LOG_TAG, this.mPhoneId, "update geo location");
        Optional.ofNullable(ImsRegistry.getGeolocationController()).ifPresent(new RegistrationGovernorVzw$$ExternalSyntheticLambda0(this));
        return true;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$isLocationInfoLoaded$0(IGeolocationController iGeolocationController) {
        iGeolocationController.startGeolocationUpdate(this.mPhoneId, false);
    }

    public boolean determineDeRegistration(int i, int i2) {
        if (i == 0) {
            triggerDeRegistration("no IMS service for network : " + i2, 4, NetworkUtil.is3gppLegacyNetwork(i2), false);
            return true;
        } else if (!isFullBarring(i2) || !ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId) || this.mRegMan.getCsfbSupported(this.mPhoneId)) {
            return false;
        } else {
            triggerDeRegistration("SSAC barred on PS only area", 76, true, true);
            return true;
        }
    }

    private void triggerDeRegistration(String str, int i, boolean z, boolean z2) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "isNeedToDeRegistration: " + str);
        this.mTask.setReason(str);
        this.mTask.setDeregiReason(i);
        this.mRegMan.tryDeregisterInternal(this.mTask, z, z2);
    }

    public boolean needPendingPdnConnected() {
        if (!ImsUtil.isCdmalessEnabled(this.mContext, this.mPhoneId) || this.mTask.getProfile().hasEmergencySupport()) {
            return false;
        }
        RegistrationGovernor.PcoType pcoType = this.mPcoType;
        if (pcoType == RegistrationGovernor.PcoType.PCO_DEFAULT) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onPdnConnected: Pending 1 sec! PCO not yet received.");
            RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
            registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(22, this.mTask), 1000);
            setPcoType(RegistrationGovernor.PcoType.PCO_AWAITING);
            return true;
        } else if (pcoType != RegistrationGovernor.PcoType.PCO_AWAITING) {
            return false;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onPdnConnected: 1 sec delay has expired!");
            setPcoType(RegistrationGovernor.PcoType.PCO_POSTPAY);
            return false;
        }
    }

    public String toString() {
        return "RegistrationGovernorVzw [mHasPendingDeregistration=" + this.mHasPendingDeregistration + ", mDmLastVolteEnabled=" + this.mDmLastVolteEnabled + ", mDmLastLvcEnabled=" + this.mDmLastLvcEnabled + ", mDmLastEabEnabled=" + this.mDmLastEabEnabled + ", mDmLastVceEnabled=" + this.mDmLastVceEnabled + ", mIsInviteForbidden=" + this.mIsInviteForbidden + ", mDmVolteNodeUpdated=" + this.mDmVolteNodeUpdated + ", mHasPendingOmadmUpdate=" + this.mHasPendingOmadmUpdate + ", mOverrideEpdgCellularPref=" + this.mOverrideEpdgCellularPref + ", mIsVolteAllowedWithDsac=" + this.mRegMan.getVolteAllowedWithDsac() + "]" + super.toString();
    }

    public void onVolteSettingChanged() {
        forceTurnOnVoLteWhenMenuRemoved();
        updateVolteState();
    }

    /* access modifiers changed from: protected */
    public void forceTurnOnVoLteWhenMenuRemoved() {
        if (this.mIsVoLteMenuRemoved && this.mPdnType == 11 && getVoiceTechType(this.mPhoneId) == 1) {
            this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, this.mTask, "forceTurnOnVoLteWhenMenuRemoved: No VoLTE UI exists. Force turn on!");
            forceTurnOnVoLte();
        }
    }

    public void onEpdgDisconnected() {
        if (this.mOverrideEpdgCellularPref) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onEpdgDisconnected: Release mOverrideEpdgCellularPref");
            this.mOverrideEpdgCellularPref = false;
        }
    }

    static class CallSnapshot {
        /* access modifiers changed from: private */
        public int mCallType;
        /* access modifiers changed from: private */
        public SipError mError;
        /* access modifiers changed from: private */
        public IRegistrationGovernor.CallEvent mEvent = IRegistrationGovernor.CallEvent.EVENT_CALL_UNKNOWN;

        public void setCallSnapshot(IRegistrationGovernor.CallEvent callEvent, SipError sipError, int i) {
            this.mEvent = callEvent;
            this.mError = sipError;
            this.mCallType = i;
        }

        public void clear() {
            this.mEvent = IRegistrationGovernor.CallEvent.EVENT_CALL_UNKNOWN;
        }

        public boolean isEmpty() {
            return this.mEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_UNKNOWN;
        }
    }
}
