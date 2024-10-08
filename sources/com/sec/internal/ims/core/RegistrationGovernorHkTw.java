package com.sec.internal.ims.core;

import android.content.Context;
import android.os.SystemClock;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegistrationGovernorHkTw extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnHkTw";

    public RegistrationGovernorHkTw(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
        this.mNeedToCheckLocationSetting = false;
    }

    public Set<String> filterService(Set<String> set, int i) {
        if (isImsDisabled()) {
            return new HashSet();
        }
        HashSet hashSet = new HashSet(set);
        Set hashSet2 = new HashSet();
        boolean z = false;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) == 1) {
            Set<String> servicesByImsSwitch = servicesByImsSwitch(ImsProfile.getVoLteServiceList());
            hashSet2.addAll(servicesByReadSwitch((String[]) servicesByImsSwitch.toArray(new String[servicesByImsSwitch.size()])));
            if (!hashSet2.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
            }
        }
        if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
            Set<String> servicesByImsSwitch2 = servicesByImsSwitch(ImsProfile.getRcsServiceList());
            hashSet2.addAll(servicesByReadSwitch((String[]) servicesByImsSwitch2.toArray(new String[servicesByImsSwitch2.size()])));
        }
        if (hashSet2.isEmpty()) {
            return hashSet2;
        }
        if ((i == 13 || i == 20) && this.mTask.getProfile().getPdnType() == 11) {
            hashSet2 = applyVoPsPolicy(hashSet2);
            if (hashSet2.isEmpty()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                return hashSet2;
            } else if (this.mMno == Mno.CHT) {
                if (ImsConstants.SystemSettings.DATA_ROAMING.get(this.mContext, ImsConstants.SystemSettings.DATA_ROAMING_UNKNOWN) == ImsConstants.SystemSettings.ROAMING_DATA_ENABLED) {
                    z = true;
                }
                if (!z && this.mRegMan.getNetworkEvent(this.mTask.getPhoneId()).isDataRoaming) {
                    IMSLog.d(LOG_TAG, this.mPhoneId, "Data roaming OFF remove VoLTE service - isDataRoaming:" + this.mRegMan.getNetworkEvent(this.mTask.getPhoneId()).isDataRoaming);
                    String str = "Data roaming : OFF," + this.mTask.getRegistrationRat();
                    removeService(hashSet2, "mmtel-video", str);
                    removeService(hashSet2, "mmtel", str);
                    removeService(hashSet2, "smsip", str);
                }
            }
        }
        if (!hashSet.isEmpty()) {
            hashSet.retainAll(hashSet2);
        }
        return applyMmtelUserSettings(hashSet, i);
    }

    /* access modifiers changed from: protected */
    public boolean checkNetworkEvent(int i) {
        int networkClass = TelephonyManagerExt.getNetworkClass(this.mRegMan.getNetworkEvent(this.mPhoneId).network);
        if (i != 0 || this.mTask.getProfile().getPdnType() != 11 || networkClass == 1 || networkClass == 2) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: W2L NW unknown moment");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int i) {
        if (i == 18 || getVoiceTechType() == 0) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: volte disabled");
        this.mRegMan.resetNotifiedImsNotAvailable(this.mPhoneId);
        return true;
    }

    public boolean isReadyToRegister(int i) {
        return checkEmergencyStatus() || (checkRegiStatus() && checkNetworkEvent(i) && checkRoamingStatus(i) && checkVolteSetting(i) && checkWFCsettings(i));
    }

    public boolean isLocationInfoLoaded(int i) {
        return !this.mRegMan.getNetworkEvent(this.mPhoneId).isEpdgConnected || super.isLocationInfoLoaded(i);
    }

    public void releaseThrottle(int i) {
        if (i == 1) {
            this.mIsPermanentStopped = false;
        } else if (i == 6) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mNonVoLTESimByPdnFail = false;
        } else if (i == 4) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mCurImpu = 0;
            this.mNonVoLTESimByPdnFail = false;
        }
        if (!this.mIsPermanentStopped || !this.mIsPermanentPdnFailed) {
            int i2 = this.mPhoneId;
            IMSLog.i(LOG_TAG, i2, "releaseThrottle: case by " + i);
        }
    }

    public boolean isThrottled() {
        return this.mIsPermanentStopped || (this.mIsPermanentPdnFailed && this.mTask.getProfile().getPdnType() == 11) || this.mRegiAt > SystemClock.elapsedRealtime();
    }

    public void onPdnRequestFailed(PdnFailReason pdnFailReason, int i) {
        if (this.mRegMan.getCurrentNetworkByPhoneId(this.mPhoneId) != 13) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onPdnRequestFailed ignore in non LTE");
            return;
        }
        super.onPdnRequestFailed(pdnFailReason, i);
        if (isMatchedPdnFailReason(pdnFailReason)) {
            this.mIsPermanentPdnFailed = true;
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            this.mNonVoLTESimByPdnFail = true;
        }
    }

    public boolean determineDeRegistration(int i, int i2) {
        if (i != 0 || this.mMno != Mno.TWM || this.mTelephonyManager.getCallState() == 0) {
            return super.determineDeRegistration(i, i2);
        }
        this.mTask.setDeregiReason(4);
        this.mRegMan.deregister(this.mTask, false, false, 6000, "TWM: delay 6s to deregister");
        return true;
    }

    public boolean onUpdateGeolocation(LocationInfo locationInfo) {
        if (!this.mMno.isOneOf(Mno.HK3, Mno.SMARTONE, Mno.CMHK, Mno.CSL, Mno.PCCW)) {
            return false;
        }
        updateGeolocation(locationInfo.mCountry);
        return false;
    }

    public void onTelephonyCallStatusChanged(int i) {
        IVolteServiceModule iVolteServiceModule;
        super.onTelephonyCallStatusChanged(i);
        if (this.mMno == Mno.SMARTONE && i == 1 && (iVolteServiceModule = this.mVsm) != null && iVolteServiceModule.hasCsCall(this.mPhoneId) && this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && this.mTask.getPdnType() == 11) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onTelephonyCallStatusChanged: deregister due to incoming cs call");
            this.mTask.setDeregiReason(7);
            this.mRegMan.deregister(this.mTask, true, false, "incoming cs call");
        }
    }

    /* access modifiers changed from: protected */
    public List<String> addIpv4Addr(List<String> list, List<String> list2, LinkPropertiesWrapper linkPropertiesWrapper) {
        if (linkPropertiesWrapper.hasIPv4Address()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "ipv4 address found");
            for (String next : list) {
                if (NetworkUtil.isIPv4Address(next)) {
                    list2.add(next);
                }
            }
        }
        return list2;
    }

    /* access modifiers changed from: protected */
    public int getVoiceTechType() {
        forceTurnOnVoLteWhenMenuRemoved();
        return super.getVoiceTechType();
    }
}
