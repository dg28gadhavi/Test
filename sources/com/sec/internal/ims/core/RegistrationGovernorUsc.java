package com.sec.internal.ims.core;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.NvConfiguration;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ImsAutoUpdate;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RegistrationGovernorUsc extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnUsc";

    public RegistrationGovernorUsc(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        int phoneId = this.mTask.getPhoneId();
        int voiceDomainPrefEutran = getVoiceDomainPrefEutran();
        boolean z = false;
        if (voiceDomainPrefEutran >= 1 && voiceDomainPrefEutran <= 4) {
            String str = NvConfiguration.get(this.mContext, DeviceConfigManager.NV_INIT_DONE, "1");
            ISimManager simManager = this.mRegMan.getSimManager(phoneId);
            ContentValues mnoInfo = simManager != null ? simManager.getMnoInfo() : new ContentValues();
            IMSLog.i(LOG_TAG, phoneId, "NV version [" + str + "], DM [" + voiceDomainPrefEutran + "]");
            String eutranPrefFromImsUpdate = CollectionUtils.getIntValue(mnoInfo, ISimManager.KEY_IMSSWITCH_TYPE, 0) == 4 ? getEutranPrefFromImsUpdate() : "";
            if (!"1".equals(str) || TextUtils.isEmpty(eutranPrefFromImsUpdate) || Integer.parseInt(eutranPrefFromImsUpdate) != 3) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, String.valueOf(voiceDomainPrefEutran));
                this.mContext.getContentResolver().update(UriUtil.buildUri(GlobalSettingsConstants.CONTENT_URI.toString(), this.mPhoneId), contentValues, (String) null, (String[]) null);
                this.mRegMan.getEventLog().logAndAdd("GvnUsc: Restoring VOICE_DOMAIN_PREF_EUTRAN from DM.");
                IMSLog.c(LogClass.USC_LOAD_EUTRAN, this.mTask.getPhoneId() + ",EUTRAN:" + voiceDomainPrefEutran);
            } else {
                this.mRegMan.getEventLog().logAndAdd(phoneId, "RegiGvnUsc: SET EUTRAN 3 BY FORCE!");
                NvConfiguration.set(this.mContext, DeviceConfigManager.NV_INIT_DONE, "2", this.mPhoneId);
                NvConfiguration.set(this.mContext, "VOICE_DOMAIN_PREF_EUTRAN", String.valueOf(3), this.mPhoneId);
            }
        }
        z = DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel-video", this.mTask.getPhoneId()) == 1 ? true : z;
        String str2 = NvConfiguration.get(this.mContext, "LVC_ENABLED", "0");
        if (z && "0".equals(str2)) {
            NvConfiguration.set(this.mContext, "LVC_ENABLED", "1", this.mPhoneId);
        }
    }

    private String getEutranPrefFromImsUpdate() {
        int phoneId = this.mTask.getPhoneId();
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        String simMnoName = simManagerFromSimSlot != null ? simManagerFromSimSlot.getSimMnoName() : Mno.DEFAULT.getName();
        String globalSettingsSpecificParam = ImsAutoUpdate.getInstance(this.mContext, phoneId).getGlobalSettingsSpecificParam(1, simMnoName, GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN);
        IMSLog.i(LOG_TAG, phoneId, "getEutranPrefFromImsUpdate for " + simMnoName + " : " + globalSettingsSpecificParam);
        return (String) Optional.ofNullable(globalSettingsSpecificParam).orElse("");
    }

    public void onPdnRequestFailed(PdnFailReason pdnFailReason, int i) {
        super.onPdnRequestFailed(pdnFailReason, i);
        if (isMatchedPdnFailReason(pdnFailReason) && this.mTask.getRegistrationRat() == 13) {
            Log.i(LOG_TAG, "send ImsNotAvailable");
            this.mRegMan.notifyImsNotAvailable(this.mTask, true);
        }
    }

    public void onRegistrationError(SipError sipError, long j, boolean z) {
        Log.e(LOG_TAG, "onRegistrationError: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        StringBuilder sb = new StringBuilder();
        sb.append("onRegistrationError : ");
        sb.append(sipError);
        sb.append(", fail count : ");
        sb.append(this.mFailureCounter);
        eventLog.logAndAdd(sb.toString());
        if (j < 0) {
            j = 0;
        }
        if (!z) {
            this.mCurPcscfIpIdx++;
        }
        this.mFailureCounter++;
        if (SipErrorBase.isImsForbiddenError(sipError)) {
            handleForbiddenError(j);
            return;
        }
        if (SipErrorBase.SIP_TIMEOUT.equals(sipError)) {
            handleTimeoutError(j);
        }
        handleRetryTimer(j);
    }

    public SipError onSipError(String str, SipError sipError) {
        Log.e(LOG_TAG, "onSipError: service=" + str + " error=" + sipError);
        this.mIsValid = this.mNumOfPcscfIp > 0;
        if (!"mmtel".equals(str)) {
            return super.onSipError(str, sipError);
        }
        this.mTask.setReason("SIP ERROR[MMTEL] : Deregister..");
        if (SipErrorBase.ALTERNATIVE_SERVICE.equals(sipError) || ((!SipErrorBase.SESSION_INTERVAL_TOO_SMALL.equals(sipError) && !SipErrorBase.INTERVAL_TOO_BRIEF.equals(sipError) && !SipErrorBase.ANONYMITY_DISALLOWED.equals(sipError) && !SipErrorBase.BUSY_HERE.equals(sipError) && !SipErrorBase.REQUEST_TERMINATED.equals(sipError) && SipErrorBase.SipErrorType.ERROR_4XX.equals(sipError)) || SipErrorBase.SipErrorType.ERROR_5XX.equals(sipError) || SipErrorBase.SipErrorType.ERROR_6XX.equals(sipError))) {
            this.mTask.setDeregiReason(43);
            RegistrationManagerInternal registrationManagerInternal = this.mRegMan;
            RegisterTask registerTask = this.mTask;
            registrationManagerInternal.deregister(registerTask, true, true, "Deregister due to " + sipError);
        }
        return sipError;
    }

    public Set<String> filterService(Set<String> set, int i) {
        Set<String> filterService = super.filterService(set, i);
        if (getVoiceTechType() == 0 || i == 18) {
            if (RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_IR94_VIDEO_AUTH, this.mPhoneId), -1).intValue() != 1) {
                removeService(filterService, "mmtel-video", "ir94VideoAuth off");
            }
            return filterService;
        }
        Log.i(LOG_TAG, "Volte : OFF, RAT : " + this.mTask.getRegistrationRat());
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return new HashSet();
    }

    /* access modifiers changed from: protected */
    public boolean checkEutranSetting(int i) {
        if (i == 18) {
            return true;
        }
        int voiceDomainPrefEutran = getVoiceDomainPrefEutran();
        Log.i(LOG_TAG, "voiceDomainPrefEutran : " + voiceDomainPrefEutran);
        if (voiceDomainPrefEutran == 3) {
            return true;
        }
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.DM_EUTRAN_OFF.getCode());
        return false;
    }

    public boolean isReadyToRegister(int i) {
        return checkEmergencyStatus() || (checkRegiStatus() && checkEpdgEvent(i) && checkCallStatus(i) && checkEutranSetting(i) && checkRcsEvent(i)) || checkMdmnProfile();
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus(int i) {
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0) {
            return true;
        }
        if (this.mTask.getRegistrationRat() == 20 && i == 13) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "checkCallStatus: EPSFB. USC Needs re-registration.");
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: call state is not idle");
        return false;
    }

    public void onConfigUpdated() {
        int voiceDomainPrefEutran = getVoiceDomainPrefEutran();
        Log.i(LOG_TAG, "onConfigUpdated : voiceDomainPrefEutran : " + voiceDomainPrefEutran);
        IMSLog.c(LogClass.USC_UPDATE_EUTRAN, this.mTask.getPhoneId() + ",UPD EUTRAN:" + voiceDomainPrefEutran);
        ContentValues contentValues = new ContentValues();
        contentValues.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, String.valueOf(voiceDomainPrefEutran));
        this.mContext.getContentResolver().update(UriUtil.buildUri(GlobalSettingsConstants.CONTENT_URI.toString(), this.mPhoneId), contentValues, (String) null, (String[]) null);
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        eventLog.logAndAdd("GvnUsc: onConfigUpdated(): Update to GlobalSettings voice_domain_pref_eutran [" + voiceDomainPrefEutran + "]");
        if (voiceDomainPrefEutran != 3) {
            Log.i(LOG_TAG, "volte had disabled by DM");
            this.mTask.setDeregiReason(73);
            this.mRegMan.deregister(this.mTask, false, false, "volte had disabled by DM");
        }
    }

    /* access modifiers changed from: protected */
    public int getVoiceDomainPrefEutran() {
        return DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_VOICE_DOMAIN_PREF_EUTRAN, 0, this.mPhoneId).intValue();
    }
}
