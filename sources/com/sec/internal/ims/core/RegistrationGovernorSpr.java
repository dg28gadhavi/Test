package com.sec.internal.ims.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.NvConfiguration;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorSpr extends RegistrationGovernorBase {
    private static final String ACTION_LOCATION_TIMEOUT = "com.sec.sprint.wfc.LOCATION_TIMEOUT";
    private static final int DELAY_FOR_CDMA_HANDOVER = 3;
    private static final String INTENT_VOWIFI_HARDRESET = "com.sec.sprint.wfc.HRADRESET_SUCCESS";
    private static final String LOG_TAG = "RegiGvnSpr";
    private final int LOCATION_REQUEST_TIMEOUT = 45000;
    protected IGeolocationController mGeolocationCon;
    protected BroadcastReceiver mIntentReceiverSPR = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(RegistrationGovernorSpr.LOG_TAG, "intent = " + intent.getAction());
            if (RegistrationGovernorSpr.INTENT_VOWIFI_HARDRESET.equals(intent.getAction())) {
                RegistrationGovernorSpr registrationGovernorSpr = RegistrationGovernorSpr.this;
                registrationGovernorSpr.mIsPermanentStopped = false;
                registrationGovernorSpr.resetRetry();
                RegistrationGovernorSpr.this.stopRetryTimer();
                RegistrationGovernorSpr registrationGovernorSpr2 = RegistrationGovernorSpr.this;
                registrationGovernorSpr2.mRegHandler.sendTryRegister(registrationGovernorSpr2.mPhoneId);
            }
        }
    };
    protected Message mLocationTimeoutMessage = null;
    protected int mPrevVolteUIDefault = -1;

    public RegistrationGovernorSpr(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_VOWIFI_HARDRESET);
        this.mContext.registerReceiver(this.mIntentReceiverSPR, intentFilter);
        this.mGeolocationCon = ImsRegistry.getGeolocationController();
        onConfigUpdated();
    }

    public void onPdnRequestFailed(PdnFailReason pdnFailReason, int i) {
        super.onPdnRequestFailed(pdnFailReason, i);
        if (this.mTask.getRegistrationRat() == 13) {
            Log.i(LOG_TAG, "send ImsNotAvailable");
            this.mRegMan.notifyImsNotAvailable(this.mTask, true);
        }
    }

    public void unRegisterIntentReceiver() {
        Log.i(LOG_TAG, "Un-register intent receiver(s)");
        try {
            this.mContext.unregisterReceiver(this.mIntentReceiverSPR);
        } catch (IllegalArgumentException unused) {
            Log.e(LOG_TAG, "unRegisterIntentReceiver: Receiver not registered!");
        }
    }

    public Set<String> filterService(Set<String> set, int i) {
        Set<String> filterService = super.filterService(set, i);
        if (!TextUtils.equals(NvConfiguration.get(this.mContext, "sms_over_ip_network_indication", ""), "1")) {
            removeService(filterService, "smsip", "DM off.");
        }
        if (i != 18 && !NetworkUtil.isMobileDataOn(this.mContext)) {
            Log.i(LOG_TAG, "filterService: Mobile data OFF!");
            if (this.mTask.getProfile().hasService("im") || this.mTask.getProfile().hasService("ft")) {
                return new HashSet();
            }
        }
        Set<String> applyMmtelUserSettings = applyMmtelUserSettings(filterService, i);
        if (!RcsUtils.DualRcs.isRegAllowed(this.mContext, this.mPhoneId)) {
            for (String removeService : ImsProfile.getRcsServiceList()) {
                removeService(applyMmtelUserSettings, removeService, "No DualRcs");
            }
        }
        return applyMmtelUserSettings;
    }

    /* access modifiers changed from: protected */
    public boolean isVoWiFiPrefered(boolean z) {
        if (!z) {
            return VowifiConfig.getPrefMode(this.mContext, 1, this.mPhoneId) == 1;
        }
        if (VowifiConfig.getRoamPrefMode(this.mContext, 1, this.mPhoneId) == 1) {
            return true;
        }
        return false;
    }

    public SipError onSipError(String str, SipError sipError) {
        Log.e(LOG_TAG, "onSipError: service=" + str + " error=" + sipError);
        if ("mmtel".equals(str)) {
            int code = sipError.getCode();
            if (code == 486 || code == 487 || code == 408) {
                return sipError;
            }
            if (code >= 400 && code <= 699) {
                this.mTask.setDeregiReason(43);
                RegistrationManagerInternal registrationManagerInternal = this.mRegMan;
                RegisterTask registerTask = this.mTask;
                registrationManagerInternal.deregister(registerTask, false, true, 3000, code + " error");
            } else if (SipErrorBase.SIP_INVITE_TIMEOUT.equals(sipError)) {
                this.mTask.setDeregiReason(43);
                this.mRegMan.deregister(this.mTask, false, true, 3000, "invite timeout");
            }
        } else if (("im".equals(str) || "ft".equals(str)) && SipErrorBase.FORBIDDEN.equals(sipError)) {
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, true, this.mIsValid, "SIP ERROR[IM] : FORBIDDEN. DeRegister..");
        }
        return sipError;
    }

    /* access modifiers changed from: protected */
    public boolean checkRoamingStatus(int i) {
        if (i == 18 || !this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming || allowRoaming()) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: IMS roaming is not allowed.");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ROAMING_NOT_SUPPORTED.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (this.mTask.getProfile().getPdnType() == 15 || this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0) {
            return true;
        }
        if (this.mTask.isRcsOnly()) {
            IVolteServiceModule iVolteServiceModule = this.mVsm;
            if (iVolteServiceModule == null || !iVolteServiceModule.hasCsCall(this.mPhoneId)) {
                return true;
            }
            Log.i(LOG_TAG, "isReadyToRegister: TelephonyCallStatus is not idle (CS call)");
            return false;
        }
        Log.i(LOG_TAG, "isReadyToRegister: TelephonyCallStatus is not idle");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkRcsEvent(int i) {
        if (this.mTask.isRcsOnly()) {
            if (RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId("version", this.mPhoneId), -1).intValue() <= 0 && ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) == -1) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: User don't try RCS service yet");
                return false;
            } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) != 1) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: Default MSG app isn't used for RCS");
                return false;
            }
        }
        return true;
    }

    public boolean isReadyToRegister(int i) {
        return checkEmergencyStatus() || (checkRegiStatus() && checkRoamingStatus(i) && checkCallStatus() && checkRcsEvent(i));
    }

    public void onTelephonyCallStatusChanged(int i) {
        ImsRegistration imsRegistration = this.mTask.getImsRegistration();
        Log.i(LOG_TAG, "onTelephonyCallStatusChanged: callState = " + i);
        if (i == 0 && this.mTask.getRegistrationRat() != 18 && imsRegistration != null && imsRegistration.hasService("mmtel")) {
            this.mRegMan.addPendingUpdateRegistration(this.mTask, 0);
        }
        IVolteServiceModule iVolteServiceModule = this.mVsm;
        if (iVolteServiceModule != null && iVolteServiceModule.hasCsCall(this.mPhoneId) && imsRegistration != null && this.mTask.isRcsOnly() && this.mTask.getRegistrationRat() != 18 && this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
            Log.i(LOG_TAG, "CS call. Trigger deregister for RCS");
            this.mTask.setDeregiReason(7);
            this.mRegMan.deregister(this.mTask, false, true, 0, "CS call. Trigger deregister for RCS");
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> applyVoPsPolicy(Set<String> set) {
        if (set == null) {
            return new HashSet();
        }
        int callState = this.mTelephonyManager.getCallState();
        Log.i(LOG_TAG, "applyVoPsPolicy: call state = " + callState);
        if (set.contains("mmtel") && callState == 0 && this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
            removeService(set, "mmtel", "applyVoPsPolicy");
        }
        return set;
    }

    public boolean isLocationInfoLoaded(int i) {
        Log.i(LOG_TAG, "isLocationInfoLoaded: rat = " + i);
        if (this.mTask.getProfile().getSupportedGeolocationPhase() == 0 || i != 18) {
            return true;
        }
        IGeolocationController iGeolocationController = this.mGeolocationCon;
        if (iGeolocationController != null) {
            if (iGeolocationController.isCountryCodeLoaded(this.mPhoneId)) {
                Log.i(LOG_TAG, "isLocationInfoLoaded: country code loaded");
                stopLocTimeoutTimer();
                return true;
            } else if (this.mGeolocationCon.isLocationServiceEnabled()) {
                Log.i(LOG_TAG, "isLocationInfoLoaded: request location info");
                this.mGeolocationCon.startGeolocationUpdate(this.mPhoneId, false);
                startLocTimeoutTimer();
            } else {
                notifyLocationTimeout();
            }
        }
        return false;
    }

    public void notifyLocationTimeout() {
        Log.i(LOG_TAG, "notifyLocationTimeout:");
        stopLocTimeoutTimer();
        Intent intent = new Intent();
        intent.setAction(ACTION_LOCATION_TIMEOUT);
        this.mContext.sendBroadcast(intent);
    }

    /* access modifiers changed from: protected */
    public void startLocTimeoutTimer() {
        if (this.mLocationTimeoutMessage != null) {
            Log.i(LOG_TAG, "startLocTimeoutTimer: timer already running");
            return;
        }
        Log.i(LOG_TAG, "startLocTimeoutTimer: timer 45000ms");
        this.mLocationTimeoutMessage = this.mRegHandler.startLocationRequestTimer(this.mTask, 45000);
    }

    /* access modifiers changed from: protected */
    public void stopLocTimeoutTimer() {
        Log.i(LOG_TAG, "stopLocTimeoutTimer:");
        Message message = this.mLocationTimeoutMessage;
        if (message != null) {
            this.mRegHandler.stopTimer(message);
            this.mLocationTimeoutMessage = null;
        }
    }

    public void requestLocation(int i) {
        Log.i(LOG_TAG, "requestLocation:");
        this.mGeolocationCon.startGeolocationUpdate(i, false);
    }

    public void onConfigUpdated() {
        int i = this.mTask != null ? this.mPhoneId : 0;
        int intValue = DmConfigHelper.readInt(ImsRegistry.getContext(), ConfigConstants.ConfigPath.OMADM_SPR_VOLTE_UI_DEFAULT, -1, i).intValue();
        Log.i(LOG_TAG, "onConfigUpdated: currentVolteUIDefault = " + intValue + ", prevVolteUIDefault = " + this.mPrevVolteUIDefault);
        if (intValue != -1 && intValue != this.mPrevVolteUIDefault) {
            this.mPrevVolteUIDefault = intValue;
            if (intValue == 2) {
                IMSLog.c(LogClass.SPR_DM_VOLTE_FORCED_ON, i + ",DM UPD:VLT FORCED ON");
                ImsConstants.SystemSettings.setVoiceCallType(this.mContext, 0, i);
                ImsConstants.SystemSettings.setVoiceCallTypeUserAction(this.mContext, 2, i);
                return;
            }
            boolean isUserToggledVoiceCallType = ImsConstants.SystemSettings.isUserToggledVoiceCallType(this.mContext, this.mPhoneId);
            int i2 = this.mPhoneId;
            IMSLog.i(LOG_TAG, i2, "isVoltePrefChangedFromApp: Changed by user [" + isUserToggledVoiceCallType + "]");
            if (!isUserToggledVoiceCallType) {
                ImsConstants.SystemSettings.setVoiceCallType(this.mContext, intValue == 0 ? 1 : 0, i);
                ImsConstants.SystemSettings.setVoiceCallTypeUserAction(this.mContext, 0, i);
            } else {
                Log.i(LOG_TAG, "onConfigUpdated: user pref already changed from app");
            }
            StringBuilder sb = new StringBuilder();
            sb.append(i);
            sb.append(",DM UPD:");
            sb.append(isUserToggledVoiceCallType ? "1" : "0");
            sb.append(",");
            sb.append(intValue);
            IMSLog.c(LogClass.SPR_DM_VOLTE_DEFAULT_UPDATE, sb.toString());
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> applyMmtelUserSettings(Set<String> set, int i) {
        if (set == null) {
            return new HashSet();
        }
        if (i != 18) {
            if (getVoiceTechType() != 0) {
                removeService(set, "mmtel", "VoLTE OFF");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_USER_SETTINGS_OFF.getCode());
            }
        } else if (this.mTask.getProfile().getPdnType() == 11) {
            if (!VowifiConfig.isEnabled(this.mContext, this.mPhoneId)) {
                Log.i(LOG_TAG, "filterService: remove [ALL] by Wi-Fi Calling OFF");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
                return new HashSet();
            }
            if (DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel", this.mPhoneId) == 1 && !set.contains("mmtel")) {
                Log.i(LOG_TAG, "filterService: add [mmtel] by Wi-Fi Calling ON");
                set.add("mmtel");
            }
            if (this.mTelephonyManager.getCallState() == 0 && !isVoWiFiPrefered(this.mTelephonyManager.isNetworkRoaming())) {
                NetworkEvent networkEvent = this.mRegMan.getNetworkEvent(this.mPhoneId);
                int i2 = networkEvent.voiceNetwork;
                boolean z = true ^ networkEvent.csOutOfService;
                boolean z2 = networkEvent.isPsOnlyReg;
                int i3 = this.mPhoneId;
                IMSLog.i(LOG_TAG, i3, "filterService: voiceRat [" + i2 + "] voiceInSvc [" + z + "] PsOnlyReg [" + z2 + "]");
                if (z && i2 != 0 && (i2 != 13 || !z2)) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "filterService: remove [mmtel] due to cellular pref. mode");
                    removeService(set, "mmtel", "VoWiFi cellular pref. mode");
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_USER_SETTINGS_OFF.getCode());
                }
            }
        }
        return set;
    }
}
