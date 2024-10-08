package com.sec.internal.ims.core;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.android.internal.telephony.TelephonyFeatures;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.SemTelephonyAdapter;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class RegistrationGovernorCtc extends RegistrationGovernorBase {
    private static final int DEFAULT_TIMS_TIMER = 730;
    private static final String LOG_TAG = "RegiGvnCtc";
    protected Set<String> mInvalidPcscfIp = new TreeSet();
    private boolean mPendingCtcVolteOff = false;
    private boolean mPendingCtcVolteOn = false;
    protected List<String> mRcsPcscfList;
    protected final long[] mRegRetryTime_MS = {0, 30000, SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF, 120000, 240000, 480000};

    /* access modifiers changed from: protected */
    public boolean checkDelayedStopPdnEvent() {
        return true;
    }

    public RegistrationGovernorCtc(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        updateEutranValues();
        updateCTCVolteState();
        this.mHandlePcscfOnAlternativeCall = true;
        this.mRcsPcscfList = new ArrayList();
    }

    public void releaseThrottle(int i) {
        if (i == 1 || i == 4) {
            this.mIsPermanentStopped = false;
            this.mRegiAt = 0;
            stopRetryTimer();
        } else if (i == 7) {
            this.mIsPermanentStopped = false;
        }
        if (!this.mIsPermanentStopped) {
            int i2 = this.mPhoneId;
            IMSLog.i(LOG_TAG, i2, "releaseThrottle: case by " + i);
        }
    }

    public void onCallStatus(IRegistrationGovernor.CallEvent callEvent, SipError sipError, int i) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "onCallStatus: event=" + callEvent + " error=" + sipError);
        if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END) {
            this.mHasVoLteCall = false;
            if (isDeregisterWithVoPSNeeded()) {
                this.mTask.setDeregiReason(72);
                this.mRegMan.deregister(this.mTask, false, false, "SERVICE NOT AVAILABLE");
                return;
            }
            return;
        }
        super.onCallStatus(callEvent, sipError, i);
    }

    public void checkAcsPcscfListChange() {
        if (this.mTask.isRcsOnly()) {
            ArrayList arrayList = new ArrayList();
            String readStringParam = RcsConfigurationHelper.readStringParam(this.mContext, "address", (String) null);
            if (readStringParam == null) {
                IMSLog.i(LOG_TAG, "checkAcsPcscfIpListChange : lboPcscfAddress is null");
                return;
            }
            arrayList.add(readStringParam);
            IMSLog.i(LOG_TAG, "checkAcsPcscfIpListChange : previous pcscf = " + this.mRcsPcscfList + ", new pcscf = " + arrayList);
            if (!arrayList.equals(this.mRcsPcscfList)) {
                if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    this.mTask.setDeregiReason(8);
                    this.mRegMan.deregister(this.mTask, true, false, "pcscf updated");
                }
                resetPcscfList();
                ArrayList arrayList2 = new ArrayList();
                this.mRcsPcscfList = arrayList2;
                arrayList2.add(readStringParam);
                IMSLog.i(LOG_TAG, "checkAcsPcscfIpListChange : resetPcscfList");
            }
        }
    }

    public Set<String> filterService(Set<String> set, int i) {
        Set hashSet = new HashSet();
        HashSet hashSet2 = new HashSet(set);
        if (isImsDisabled()) {
            return new HashSet();
        }
        Set<String> applyMmtelUserSettings = applyMmtelUserSettings(hashSet2, i);
        boolean z = true;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) != 1) {
            z = false;
        }
        if (z) {
            Set<String> servicesByImsSwitch = servicesByImsSwitch(ImsProfile.getVoLteServiceList());
            hashSet.addAll(servicesByReadSwitch((String[]) servicesByImsSwitch.toArray(new String[servicesByImsSwitch.size()])));
            if (!hashSet.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
            }
        }
        if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
            Set<String> servicesByImsSwitch2 = servicesByImsSwitch(ImsProfile.getRcsServiceList());
            hashSet.addAll(servicesByReadSwitch((String[]) servicesByImsSwitch2.toArray(new String[servicesByImsSwitch2.size()])));
        }
        if ((i == 13 || i == 20) && this.mTask.getProfile().getPdnType() == 11) {
            hashSet = applyVoPsPolicy(hashSet);
            if (hashSet.isEmpty()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                return hashSet;
            }
        }
        if (!applyMmtelUserSettings.isEmpty()) {
            applyMmtelUserSettings.retainAll(hashSet);
        }
        return applyMmtelUserSettings;
    }

    public SipError onSipError(String str, SipError sipError) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "onSipError: service=" + str + " error=" + sipError);
        if (!"smsip".equals(str) || !checkCallStatus()) {
            return super.onSipError(str, sipError);
        }
        if (sipError.getCode() == 408 || sipError.getCode() == 708) {
            int i2 = this.mPhoneId;
            IMSLog.i(LOG_TAG, i2, "SMS error : mCurPcscfIpIdx=" + this.mCurPcscfIpIdx + " mNumOfPcscfIp=" + this.mNumOfPcscfIp);
            removeCurrentPcscfAndInitialRegister(true);
        }
        return sipError;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        IVolteServiceModule iVolteServiceModule;
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
            return true;
        }
        int i = this.mPhoneId;
        if (this.mRegMan.getTelephonyCallStatus(i) != 0 || ((iVolteServiceModule = this.mVsm) != null && iVolteServiceModule.getSessionCount(i) > 0 && !this.mVsm.hasEmergencyCall(i) && this.mVsm.hasActiveCall(i))) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: call state is not idle");
            return false;
        } else if (!SimConstants.DSDS_DI.equals(SimUtil.getConfigDualIMS()) || this.mRegMan.getTelephonyCallStatus(SimUtil.getOppositeSimSlot(i)) == 0) {
            return true;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: another slot's call state is not idle");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkRcsEvent(int i) {
        int i2 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i2, "checkRcsEvent: pdn = " + this.mTask.getProfile().getPdn() + ", state = " + this.mTask.getState());
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && this.mTask.isRcsOnly()) {
            if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) != 1) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: Default MSG app isn't used for RCS");
                return false;
            } else if (!(this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED || !this.mPdnController.isWifiConnected() || i == 18)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: The RCS rat is not wifi, when wifi is connected.");
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int i) {
        if (i == 18 || getVoiceTechType(this.mPhoneId) == 0) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: volte disabled");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public long getWaitTime() {
        int min = Math.min(this.mFailureCounter, this.mRegRetryTime_MS.length - 1);
        return this.mRegRetryTime_MS[min] + (min == 3 ? ((long) ImsUtil.getRandom().nextInt(15)) * 1000 : 0);
    }

    /* access modifiers changed from: protected */
    public boolean isInvalidPcscfIp(String str) {
        if (!TextUtils.isEmpty(str) && CollectionUtils.isNullOrEmpty((Collection<?>) this.mInvalidPcscfIp)) {
            return false;
        }
        for (String equalsIgnoreCase : this.mInvalidPcscfIp) {
            if (str.equalsIgnoreCase(equalsIgnoreCase)) {
                return true;
            }
        }
        return false;
    }

    public List<String> checkValidPcscfIp(List<String> list) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "checkValidPcscfIp");
        if (!this.mTask.isRcsOnly()) {
            return super.checkValidPcscfIp(list);
        }
        List<String> arrayList = new ArrayList<>();
        LinkPropertiesWrapper linkProperties = this.mPdnController.getLinkProperties(this.mTask);
        if (!(list == null || list.isEmpty() || linkProperties == null)) {
            arrayList = addIpv6Addr(list, arrayList, linkProperties);
            if (CollectionUtils.isNullOrEmpty((Collection<?>) arrayList)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "validPcscfIp ipv6 is null");
                arrayList = addIpv4Addr(list, arrayList, linkProperties);
            } else if (isInvalidPcscfIp(arrayList.get(0))) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "validPcscfIp ipv6 is not valid");
                String str = arrayList.get(0);
                arrayList.clear();
                arrayList = addIpv4Addr(list, arrayList, linkProperties);
                if (CollectionUtils.isNullOrEmpty((Collection<?>) arrayList) || isInvalidPcscfIp(arrayList.get(0))) {
                    arrayList.clear();
                    arrayList.add(str);
                    IMSLog.i(LOG_TAG, this.mPhoneId, "validPcscfIp ipv6 and ipv4 all is not valid, so select ipv6");
                } else {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "validPcscfIp ipv4 is valid");
                }
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "validPcscfIp ipv6 is valid");
            }
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "ValidPcscfIp: " + arrayList);
        }
        return arrayList;
    }

    public void onRegistrationError(SipError sipError, long j, boolean z) {
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        eventLog.logAndAdd("onRegistrationError: state " + this.mTask.getState() + " error " + sipError + " retryAfterMs " + j + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (this.mTask.isRcsOnly()) {
            String currentPcscfIp = getCurrentPcscfIp();
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "onRegistrationError: " + currentPcscfIp);
            if (!TextUtils.isEmpty(currentPcscfIp)) {
                this.mInvalidPcscfIp.add(currentPcscfIp);
            }
            super.onRegistrationError(sipError, j, z);
            return;
        }
        super.onRegistrationError(sipError, j, z);
    }

    public void onRegistrationDone() {
        super.onRegistrationDone();
        stopTimsTimer(RegistrationConstants.REASON_REGISTERED);
    }

    public void onDeregistrationDone(boolean z) {
        if (z && !this.mTask.mKeepPdn && getVoiceTechType(this.mPhoneId) == 1 && this.mTask.getPdnType() == 11) {
            this.mRegHandler.notifyVolteSettingOff(this.mTask, 1000);
        }
        if (TelephonyFeatures.isChnGlobalModel(this.mPhoneId) && this.mTask.getPdnType() == 11 && this.mPendingCtcVolteOff) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "update volte off state to CP after IMS deregistered.");
            this.mPendingCtcVolteOff = false;
            SemTelephonyAdapter.sendVolteState(this.mPhoneId, false);
            if (this.mPendingCtcVolteOn) {
                this.mPendingCtcVolteOn = false;
                SemTelephonyAdapter.sendVolteState(this.mPhoneId, true);
            }
        }
    }

    public void onPdnRequestFailed(PdnFailReason pdnFailReason, int i) {
        super.onPdnRequestFailed(pdnFailReason, i);
        if (!isMatchedPdnFailReason(pdnFailReason)) {
            onPdnFailCounterInNr();
        }
    }

    public void notifyVoLteOnOffToRil(boolean z) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "notifyVoLteOnOffToRil: " + z);
        ContentValues contentValues = new ContentValues();
        if (z) {
            contentValues.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 3);
        } else {
            contentValues.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 1);
        }
        Uri.Builder buildUpon = Uri.parse("content://com.sec.ims.settings/global").buildUpon();
        this.mContext.getContentResolver().update(buildUpon.fragment("simslot" + this.mPhoneId).build(), contentValues, (String) null, (String[]) null);
    }

    public void onVolteSettingChanged() {
        updateEutranValues();
        updateCTCVolteState();
    }

    public void startTimsTimer(String str) {
        startTimsEstablishTimer(this.mTask, (long) 730000, str);
    }

    public void stopTimsTimer(String str) {
        stopTimsEstablishTimer(this.mTask, str);
    }

    private void updateEutranValues() {
        if (this.mTask.getProfile().hasService("mmtel")) {
            int voiceTechType = getVoiceTechType(this.mPhoneId);
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "updateEutranValues : voiceTech : " + voiceTechType);
            if (voiceTechType == 0) {
                this.mRegHandler.removeVolteSettingOffEvent();
                notifyVoLteOnOffToRil(true);
            } else if (this.mTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERED && this.mTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERING) {
                notifyVoLteOnOffToRil(false);
            }
        }
    }

    private void updateCTCVolteState() {
        if (TelephonyFeatures.isChnGlobalModel(this.mPhoneId) && this.mTask.getProfile().hasService("mmtel")) {
            int voiceTechType = getVoiceTechType(this.mPhoneId);
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "updateCTCVolteState : voiceTech : " + voiceTechType);
            if (voiceTechType == 0) {
                if (this.mPendingCtcVolteOff) {
                    this.mPendingCtcVolteOn = true;
                } else {
                    SemTelephonyAdapter.sendVolteState(this.mPhoneId, true);
                }
            } else if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                this.mPendingCtcVolteOn = false;
                this.mPendingCtcVolteOff = true;
            } else {
                SemTelephonyAdapter.sendVolteState(this.mPhoneId, false);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getVoiceTechType() {
        forceTurnOnVoLteWhenMenuRemoved();
        return super.getVoiceTechType();
    }
}
