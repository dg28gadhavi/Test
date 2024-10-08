package com.sec.internal.ims.core;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegistrationGovernorCan extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnCan";

    public RegistrationGovernorCan(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
    }

    public boolean isReadyToRegister(int i) {
        Mno mno;
        ImsRegistration imsRegistration = this.mRegMan.getRegistrationList().get(Integer.valueOf(this.mTask.getProfile().getId()));
        if (imsRegistration != null) {
            Set services = imsRegistration.getServices();
            Set<String> serviceForNetwork = this.mRegMan.getServiceForNetwork(this.mTask.getProfile(), i, false, this.mPhoneId);
            IMSLog.s(LOG_TAG, "getServiceForNetwork: services registered=" + services + " new=" + serviceForNetwork);
            Mno mno2 = this.mMno;
            if ((mno2 == Mno.BELL || mno2 == Mno.VTR) && serviceForNetwork != null && !serviceForNetwork.isEmpty() && !serviceForNetwork.equals(services) && this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0) {
                Log.i(LOG_TAG, "Call going on so registration blocked as per requirement for Bell");
                return false;
            }
        }
        if (this.mMno == Mno.SASKTEL && i == 13 && this.mPdnController.getVopsIndication(this.mPhoneId) != VoPsIndication.SUPPORTED && this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0) {
            Log.i(LOG_TAG, "isReadyToRegister: Sasktel if VOPS is not supported in LTE rat when call is ongoing");
            return false;
        } else if (this.mVsm != null && (((mno = this.mMno) == Mno.TELUS || mno == Mno.KOODO) && this.mTask.getProfile().hasEmergencySupport() && this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && this.mVsm.hasEmergencyCall(this.mPhoneId))) {
            return false;
        } else {
            if (this.mTask.getProfile().hasEmergencySupport() && this.mTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERING) {
                return true;
            }
            Mno mno3 = this.mMno;
            if (mno3 != Mno.BELL && mno3 != Mno.VTR && mno3 != Mno.SASKTEL && mno3 != Mno.WIND && mno3 != Mno.TELUS && this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0) {
                return false;
            }
            if (i == 18 && (!isWiFiSettingOn() || !VowifiConfig.isEnabled(this.mContext, this.mPhoneId))) {
                Log.i(LOG_TAG, "isReadyToRegister: Wifi Calling or Wifi turned off, RAT is not updated at framework side");
                return false;
            } else if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING) {
                return false;
            } else {
                if (!this.mTask.mIsUpdateRegistering) {
                    return true;
                }
                Log.i(LOG_TAG, "isReadyToRegister: Task State is UpdateRegistering");
                return false;
            }
        }
    }

    public SipError onSipError(String str, SipError sipError) {
        Mno mno;
        Log.e(LOG_TAG, "onSipError: service=" + str + " error=" + sipError);
        if ("mmtel".equals(str)) {
            if (SipErrorBase.SIP_INVITE_TIMEOUT.equals(sipError) || SipErrorBase.SIP_TIMEOUT.equals(sipError) || (((mno = this.mMno) == Mno.TELUS || mno == Mno.KOODO || mno == Mno.EASTLINK) && SipErrorBase.SERVER_TIMEOUT.equals(sipError))) {
                removeCurrentPcscfAndInitialRegister(true);
            }
        } else if (("im".equals(str) || "ft".equals(str)) && SipErrorBase.FORBIDDEN.equals(sipError)) {
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, true, this.mIsValid, "SIP ERROR[IM] : FORBIDDEN. DeRegister..");
        }
        return sipError;
    }

    public Set<String> filterService(Set<String> set, int i) {
        Mno mno;
        Set<String> filterService = super.filterService(set, i);
        boolean z = true;
        boolean z2 = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) == 1;
        if (!ImsUtil.isDualVideoCallAllowed(this.mPhoneId)) {
            removeService(filterService, "mmtel-video", "Non-ADS operator SIM");
        }
        if (this.mMno == Mno.BELL) {
            if (i == 18 || (NetworkUtil.isMobileDataOn(this.mContext) && !SlotBasedConfig.getInstance(this.mPhoneId).isDataUsageExceeded())) {
                boolean isNetworkRoaming = this.mTelephonyManager.isNetworkRoaming();
                if (ImsConstants.SystemSettings.DATA_ROAMING.get(this.mContext, ImsConstants.SystemSettings.DATA_ROAMING_UNKNOWN) != ImsConstants.SystemSettings.ROAMING_DATA_ENABLED) {
                    z = false;
                }
                Log.i(LOG_TAG, "isNetworkInRoaming " + isNetworkRoaming + " isDataRoamingOn " + z);
                if (isNetworkRoaming && !z) {
                    removeService(filterService, "ft_http", "DataRoaming Disabled");
                    removeService(filterService, "im", "DataRoaming Disabled");
                    removeService(filterService, "mmtel-video", "DataRoaming Disabled");
                }
            } else {
                Log.i(LOG_TAG, "Remove IM, FT, mmtel-video when Mobile Data off or limited");
                removeService(filterService, "ft_http", "MobileData unavailable");
                removeService(filterService, "im", "MobileData unavailable");
                removeService(filterService, "mmtel-video", "MobileData unavailable");
            }
        } else if (!(i == 18 || ((NetworkUtil.isMobileDataOn(this.mContext) && !SlotBasedConfig.getInstance(this.mPhoneId).isDataUsageExceeded()) || (mno = this.mMno) == Mno.ROGERS || mno == Mno.CHATR))) {
            removeService(filterService, "mmtel-video", "MobileData unavailable");
        }
        if (this.mMno == Mno.ROGERS && this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && ((this.mTelephonyManager.isNetworkRoaming() || "A4".equalsIgnoreCase(this.mTelephonyManager.getGroupIdLevel1(SimUtil.getSubId(this.mPhoneId)))) && NetworkUtil.is3gppLegacyNetwork(i))) {
            return new HashSet();
        }
        if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
            Context context = this.mContext;
            int i2 = this.mPhoneId;
            List<String> rcsEnabledServiceList = RcsConfigurationHelper.getRcsEnabledServiceList(context, i2, ConfigUtil.getRcsProfileWithFeature(context, i2, this.mTask.getProfile()));
            for (String str : ImsProfile.getRcsServiceList()) {
                if (!rcsEnabledServiceList.contains(str)) {
                    removeService(filterService, str, "Disable from ACS");
                }
            }
            if (!z2) {
                for (String removeService : ImsProfile.getChatServiceList()) {
                    removeService(filterService, removeService, "chatservice off");
                }
            }
        }
        return applyMmtelUserSettings(filterService, i);
    }

    /* access modifiers changed from: protected */
    public Set<String> applyVoPsPolicy(Set<String> set) {
        if (set == null) {
            return new HashSet();
        }
        if (this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
            removeService(set, "mmtel-video", "VoPS Off");
            removeService(set, "mmtel", "VoPS Off");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
            if (this.mMno == Mno.VTR) {
                removeService(set, "smsip", "VoPS Off");
            }
        }
        return set;
    }

    public void releaseThrottle(int i) {
        if (i == 1) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
        } else if (i == 4) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mCurImpu = 0;
        } else if (i == 6) {
            this.mRegiAt = 0;
            this.mIsPermanentPdnFailed = false;
        } else if (i == 9) {
            this.mIsPermanentPdnFailed = false;
        }
        if (!this.mIsPermanentStopped || !this.mIsPermanentPdnFailed) {
            Log.i(LOG_TAG, "releaseThrottle: case by " + i);
        }
    }

    public void onPdnRequestFailed(PdnFailReason pdnFailReason, int i) {
        super.onPdnRequestFailed(pdnFailReason, i);
        if (isMatchedPdnFailReason(pdnFailReason)) {
            stopConnectingPermanently();
        }
    }

    private void stopConnectingPermanently() {
        if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTING) {
            Log.i(LOG_TAG, "call pdn disconnect to clear off state.. : ");
            this.mIsPermanentPdnFailed = true;
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            this.mRegHandler.notifyPdnDisconnected(this.mTask);
        }
    }

    public void onEpdgIkeError() {
        stopConnectingPermanently();
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
        if (SipErrorBase.SipErrorType.ERROR_4XX.equals(sipError) || SipErrorBase.SipErrorType.ERROR_5XX.equals(sipError) || SipErrorBase.SipErrorType.ERROR_6XX.equals(sipError)) {
            if (SipErrorBase.isImsForbiddenError(sipError)) {
                handleForbiddenError(j);
                return;
            } else {
                this.mFailureCounter++;
                this.mCurPcscfIpIdx++;
            }
        } else if (SipErrorBase.SIP_TIMEOUT.equals(sipError)) {
            this.mFailureCounter++;
            this.mCurPcscfIpIdx++;
            handleTimeoutError(j);
        }
        handleRetryTimer(j);
    }

    /* access modifiers changed from: protected */
    public List<String> addIpv4Addr(List<String> list, List<String> list2, LinkPropertiesWrapper linkPropertiesWrapper) {
        if (linkPropertiesWrapper.hasIPv4Address() && (this.mMno == Mno.BELL || list2.isEmpty())) {
            Log.i(LOG_TAG, "ipv4 address found");
            for (String next : list) {
                if (NetworkUtil.isIPv4Address(next)) {
                    list2.add(next);
                }
            }
        }
        return list2;
    }

    public boolean isThrottled() {
        if (this.mIsPermanentStopped) {
            return true;
        }
        if ((!this.mIsPermanentPdnFailed || this.mTask.getProfile().getPdnType() != 11) && this.mRegiAt <= SystemClock.elapsedRealtime()) {
            return false;
        }
        return true;
    }
}
