package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.UserConfiguration;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipErrorUscc;
import com.sec.internal.constants.ims.SipErrorVzw;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.servicemodules.volte2.data.IncomingCallEvent;
import com.sec.internal.ims.settings.GlobalSettingsManager;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;

public class ImsCallSipErrorFactory {
    private static final String LOG_TAG = "ImsCallSipErrorFactory";
    private IPdnController mPdnController;
    private final IRegistrationManager mRegMan;
    private ITelephonyManager mTelephonyManager;
    private IVolteServiceModuleInternal mVolteServiceModule;

    public ImsCallSipErrorFactory(IVolteServiceModuleInternal iVolteServiceModuleInternal, ITelephonyManager iTelephonyManager, IPdnController iPdnController, IRegistrationManager iRegistrationManager) {
        this.mVolteServiceModule = iVolteServiceModuleInternal;
        this.mTelephonyManager = iTelephonyManager;
        this.mPdnController = iPdnController;
        this.mRegMan = iRegistrationManager;
    }

    public SipError getSipErrorOnDialingState(int i, int i2, Mno mno) {
        SipError sipError;
        SipError sipError2 = SipErrorBase.TRYING;
        if (mno == Mno.VZW) {
            sipError = SipErrorVzw.BUSY_ESTABLISHING_ANOTHER_CALL;
        } else if (mno.isKor()) {
            sipError = SipErrorBase.BUSY_HERE;
        } else if (mno == Mno.USCC) {
            sipError = SipErrorUscc.BUSY_ESTABLISHING_ANOTHER_CALL;
        } else {
            sipError = SipErrorBase.BUSY_HERE;
        }
        return ((i != 0 || !ImsCallUtil.isCmcSecondaryType(i2)) && (!ImsCallUtil.isCmcSecondaryType(i) || i2 != 0)) ? sipError : SipErrorBase.OK;
    }

    public SipError getSipErrorIncomingCallWithVolteOffForVzw(IncomingCallEvent incomingCallEvent, ImsRegistration imsRegistration) {
        SipError sipError = SipErrorVzw.NOT_ACCEPTABLE_VOLTE_OFF;
        if (NetworkUtil.is3gppPsVoiceNetwork(this.mVolteServiceModule.getNetwork(imsRegistration.getPhoneId()).network) && this.mVolteServiceModule.getNetwork(imsRegistration.getPhoneId()).voiceOverPs != VoPsIndication.SUPPORTED) {
            return SipErrorVzw.NOT_ACCEPTABLE_NO_VOPS;
        }
        if (this.mVolteServiceModule.getNetwork(imsRegistration.getPhoneId()).network == 14) {
            return SipErrorVzw.NOT_ACCEPTABLE_ON_EHRPD;
        }
        if (this.mVolteServiceModule.isCallBarredBySSAC(imsRegistration.getPhoneId(), incomingCallEvent.getCallType())) {
            return SipErrorVzw.NOT_ACCEPTABLE_SSAC_ON;
        }
        if (this.mVolteServiceModule.acceptCallWhileSmsipRegistered(imsRegistration)) {
            return SipErrorBase.OK;
        }
        return (!this.mRegMan.isVoWiFiSupported(imsRegistration.getPhoneId()) || !this.mPdnController.isEpdgConnected(imsRegistration.getPhoneId()) || this.mVolteServiceModule.isVowifiEnabled(imsRegistration.getPhoneId())) ? sipError : SipErrorVzw.VOWIFI_OFF;
    }

    public SipError getSipErrorForCheckRejectIncomingCall(Context context, ImsRegistration imsRegistration, int i) {
        int i2;
        SipError sipErrorForNoMmtel;
        ImsRegistration imsRegistration2 = imsRegistration;
        Mno fromName = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
        int phoneId = imsRegistration.getPhoneId();
        int cmcType = imsRegistration.getImsProfile().getCmcType();
        if (fromName == Mno.VZW && this.mVolteServiceModule.getNetwork(phoneId).network == 14 && this.mVolteServiceModule.isMmtelAcquiredEver() && !this.mPdnController.isEpdgConnected(phoneId)) {
            return SipErrorVzw.NOT_ACCEPTABLE_ON_EHRPD;
        }
        SipError sipError = SipErrorBase.TRYING;
        int i3 = i;
        if (!imsRegistration2.hasService("mmtel") && (sipErrorForNoMmtel = getSipErrorForNoMmtel(imsRegistration2, i3)) != SipErrorBase.OK) {
            return sipErrorForNoMmtel;
        }
        if (needRejectByTerminalSs(context, imsRegistration, i)) {
            return SipErrorBase.BUSY_HERE;
        }
        CallDetailInfo checkHasCallAndCallType = checkHasCallAndCallType(phoneId);
        SipError sipErrorAsSessionState = getSipErrorAsSessionState(phoneId, cmcType, checkHasCallAndCallType.getHasIncall().booleanValue(), checkHasCallAndCallType.getHasHoldCall().booleanValue(), i, fromName);
        if (this.mVolteServiceModule.getCmcServiceModule().isCmcRegExist(phoneId)) {
            i2 = this.mVolteServiceModule.getCmcServiceModule().getSessionCountByCmcType(phoneId, imsRegistration2);
        } else {
            i2 = this.mVolteServiceModule.getSessionCount(phoneId);
        }
        int i4 = i2;
        String str = LOG_TAG;
        Log.i(str, "checkRejectIncomingCall: numPsCall " + i4 + "," + checkHasCallAndCallType.toString() + ", error " + sipErrorAsSessionState);
        SipError sipErrorOnCsNetwork = getSipErrorOnCsNetwork(imsRegistration2, checkHasCallAndCallType.getHasIncall().booleanValue(), checkHasCallAndCallType.getHasHoldCall().booleanValue(), getSipErrorAsHasCall(imsRegistration, checkHasCallAndCallType.getHasIncall().booleanValue(), checkHasCallAndCallType.getHasHoldCall().booleanValue(), checkHasCallAndCallType.getHasConfCall().booleanValue(), checkHasCallAndCallType.getInCalltype(), i, i4, sipErrorAsSessionState));
        if (fromName == Mno.VZW) {
            return getSipErrorForVzw(imsRegistration, i, checkHasCallAndCallType.getIsModifyOngoing(), checkHasCallAndCallType.getHasTtyCall(), sipErrorOnCsNetwork);
        } else if (fromName == Mno.SPRINT && this.mVolteServiceModule.hasCsCall(phoneId)) {
            return SipErrorBase.NOT_ACCEPTABLE_HERE;
        } else {
            ImsProfile imsProfile = imsRegistration.getImsProfile();
            if (checkHasCallAndCallType.getIsModifyOngoing().booleanValue()) {
                return getSipErrorAsModifying(imsProfile);
            }
            if (!this.mVolteServiceModule.hasCsCall(phoneId) || !this.mPdnController.isEpdgConnected(phoneId) || imsProfile.getCmcType() != 0) {
                return sipErrorOnCsNetwork;
            }
            Log.i(str, "checkRejectIncomingCall: hasCsCall");
            if (fromName == Mno.RJIL || fromName == Mno.SINGTEL || fromName == Mno.FET || fromName == Mno.CHT) {
                return SipErrorBase.NOT_ACCEPTABLE_HERE;
            }
            return SipErrorBase.BUSY_HERE;
        }
    }

    public SipError getSipErrorOnCsNetwork(ImsRegistration imsRegistration, boolean z, boolean z2, SipError sipError) {
        Mno fromName = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
        int phoneId = imsRegistration.getPhoneId();
        int subscriptionId = imsRegistration.getSubscriptionId();
        String str = LOG_TAG;
        Log.i(str, "getSipErrorOnCsNetwork: " + this.mVolteServiceModule.getNetwork(phoneId).network + ", " + this.mTelephonyManager.getVoiceNetworkType(subscriptionId));
        boolean z3 = (!NetworkUtil.is3gppPsVoiceNetwork(this.mVolteServiceModule.getNetwork(phoneId).network) && !NetworkUtil.is3gppPsVoiceNetwork(this.mTelephonyManager.getVoiceNetworkType(subscriptionId))) || this.mVolteServiceModule.getNetwork(phoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED;
        if ((fromName == Mno.ATT || fromName == Mno.TMOBILE) && !imsRegistration.getImsProfile().isSoftphoneEnabled() && !z && !z2 && !this.mPdnController.isEpdgConnected(phoneId) && z3) {
            return SipErrorBase.NOT_ACCEPTABLE_HERE;
        }
        if (!fromName.isVodafone() || this.mPdnController.isEpdgConnected(phoneId) || !z3) {
            return sipError;
        }
        Log.i(str, "for VODAFONE getSipErrorOnCsNetwork");
        return SipErrorBase.NOT_ACCEPTABLE_HERE;
    }

    public SipError getSipErrorForVzw(ImsRegistration imsRegistration, int i, Boolean bool, Boolean bool2, SipError sipError) {
        int phoneId = imsRegistration.getPhoneId();
        int subscriptionId = imsRegistration.getSubscriptionId();
        if (bool.booleanValue()) {
            sipError = SipErrorVzw.VIDEO_UPGRADE_REQUEST_IN_PROGRESS;
        } else if (i == 2) {
            if ((this.mVolteServiceModule.getTtyMode(phoneId) != Extensions.TelecomManager.TTY_MODE_OFF && this.mVolteServiceModule.getTtyMode(phoneId) != Extensions.TelecomManager.RTT_MODE) || bool2.booleanValue()) {
                Log.i(LOG_TAG, "checkRejectIncomingCall: VT not allowed during TTY is on.");
                sipError = SipErrorVzw.TTY_ON;
            } else if (!this.mRegMan.isVoWiFiSupported(phoneId) && this.mTelephonyManager.getDataNetworkType(subscriptionId) == 18) {
                sipError = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "");
            }
        } else if (i == 1 || i == 14) {
            if (!NetworkUtil.is3gppPsVoiceNetwork(this.mVolteServiceModule.getDataAccessNetwork(phoneId)) || this.mVolteServiceModule.getNetwork(phoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
                if (this.mRegMan.isVoWiFiSupported(phoneId)) {
                    if (this.mPdnController.isEpdgConnected(phoneId) && !this.mVolteServiceModule.isVowifiEnabled(phoneId)) {
                        sipError = SipErrorVzw.VOWIFI_OFF;
                    }
                } else if (this.mTelephonyManager.getDataNetworkType(subscriptionId) == 18) {
                    sipError = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "");
                }
            } else if (!this.mVolteServiceModule.isVowifiEnabled(phoneId) && this.mPdnController.isEpdgConnected(phoneId) && !imsRegistration.hasService("mmtel")) {
                sipError = SipErrorVzw.VOWIFI_OFF;
            }
        }
        if (!this.mVolteServiceModule.hasCsCall(phoneId)) {
            return sipError;
        }
        Log.i(LOG_TAG, "checkRejectIncomingCall: hasCsCall");
        return SipErrorVzw.NOT_ACCEPTABLE_ACTIVE_1X_CALL;
    }

    public SipError getSipErrorAsModifying(ImsProfile imsProfile) {
        CallProfile callProfile;
        String str = LOG_TAG;
        Log.i(str, "checkRejectIncomingCall: Reject call while Call modifying");
        SipError sipError = SipErrorBase.BUSY_HERE;
        if (!ImsCallUtil.isCmcPrimaryType(imsProfile.getCmcType())) {
            return sipError;
        }
        try {
            ImsCallSession sessionByCmcType = this.mVolteServiceModule.getCmcServiceModule().getSessionByCmcType(0);
            if (sessionByCmcType == null || sessionByCmcType.getCallState() != CallConstants.STATE.ModifyRequested || (callProfile = sessionByCmcType.getCallProfile()) == null || callProfile.getCallType() != 1) {
                return sipError;
            }
            Log.i(str, "checkRejectIncomingCall: Reject upgrade call for pulling by SD");
            sessionByCmcType.reject(3);
            return SipErrorBase.OK;
        } catch (RemoteException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "checkRejectIncomingCall: " + e.getMessage());
            return sipError;
        }
    }

    public SipError getSipErrorAsHasCall(ImsRegistration imsRegistration, boolean z, boolean z2, boolean z3, int i, int i2, int i3, SipError sipError) {
        Mno fromName = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
        if ((!z || !z2) && i3 < 2) {
            if (z) {
                String str = LOG_TAG;
                Log.i(str, "checkRejectIncomingCall: hasInCallType: " + i + " callType: " + i2);
                if (fromName.isKor() && !imsRegistration.getImsProfile().getName().contains("PS-LTE") && (i == 2 || i2 == 2)) {
                    sipError = SipErrorBase.BUSY_HERE;
                }
                if (fromName.isChn() && (ImsCallUtil.isVideoCall(i) || ImsCallUtil.isVideoCall(i2))) {
                    sipError = SipErrorBase.BUSY_HERE;
                }
            }
        } else if (fromName == Mno.VZW) {
            if (this.mVolteServiceModule.isEnableCallWaitingRule()) {
                sipError = SipErrorVzw.BUSY_ALREADY_IN_TWO_CALLS;
            }
        } else if (fromName.isChn() || fromName.isOneOf(Mno.ATT, Mno.VODAFONE_AUSTRALIA, Mno.DOCOMO, Mno.TMOUS, Mno.DISH, Mno.VODAFONE_IRELAND)) {
            Log.i(LOG_TAG, "checkRejectIncomingCall: 3rd incoming call handling in OneHold and OneActive");
        } else {
            sipError = SipErrorBase.BUSY_HERE;
        }
        if (fromName != Mno.KDDI || !z3) {
            return sipError;
        }
        SipError sipError2 = SipErrorBase.BUSY_HERE;
        String str2 = LOG_TAG;
        Log.i(str2, "checkRejectIncomingCall: error " + sipError2);
        return sipError2;
    }

    /* access modifiers changed from: protected */
    public SipError getSipErrorIncomingCallWithVolteOff(Context context, IncomingCallEvent incomingCallEvent, boolean z, ImsRegistration imsRegistration) {
        Mno fromName = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
        SipError sipError = SipErrorBase.SERVICE_UNAVAILABLE;
        if (fromName != Mno.VZW) {
            return fromName == Mno.ATT ? SipErrorBase.NOT_ACCEPTABLE_HERE : sipError;
        }
        if (z) {
            return getSipErrorForCheckRejectIncomingCall(context, imsRegistration, incomingCallEvent.getCallType());
        }
        return getSipErrorIncomingCallWithVolteOffForVzw(incomingCallEvent, imsRegistration);
    }

    public SipError getSipErrorForBarring(Context context, int i, int i2) {
        int i3;
        String str = LOG_TAG;
        Log.i(str, "checkRejectIncomingCall: Call barring");
        if (i2 == 2) {
            i3 = UserConfiguration.getUserConfig(context, i, "ss_video_cb_pref", 0);
        } else {
            i3 = UserConfiguration.getUserConfig(context, i, "ss_volte_cb_pref", 0);
        }
        if ((i3 & 8) == 8) {
            Log.i(str, "checkRejectIncomingCall: Incoming call is barried");
            return SipErrorBase.BUSY_HERE;
        } else if ((i3 & 10) != 10 || !this.mTelephonyManager.isNetworkRoaming()) {
            return SipErrorBase.OK;
        } else {
            Log.i(str, "checkRejectIncomingCall: Incoming call is barried in roaming condition");
            return SipErrorBase.BUSY_HERE;
        }
    }

    public SipError getSipErrorForNoMmtel(ImsRegistration imsRegistration, int i) {
        int phoneId = imsRegistration.getPhoneId();
        Mno fromName = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
        String str = LOG_TAG;
        Log.i(str, "checkRejectIncomingCall: no mmtel registration.");
        SipError sipError = SipErrorBase.OK;
        if (fromName == Mno.VZW) {
            if (imsRegistration.hasService("mmtel-video")) {
                return sipError;
            }
            Log.i(str, "checkRejectIncomingCall: no mmtel or mmtel-video registered.");
            if (NetworkUtil.is3gppPsVoiceNetwork(this.mVolteServiceModule.getNetwork(phoneId).network) && this.mVolteServiceModule.getNetwork(phoneId).voiceOverPs != VoPsIndication.SUPPORTED) {
                return SipErrorVzw.NOT_ACCEPTABLE_NO_VOPS;
            }
            if (this.mVolteServiceModule.getNetwork(phoneId).network == 14) {
                return SipErrorVzw.NOT_ACCEPTABLE_ON_EHRPD;
            }
            if (this.mVolteServiceModule.isCallBarredBySSAC(phoneId, i)) {
                return SipErrorVzw.NOT_ACCEPTABLE_SSAC_ON;
            }
            if (this.mVolteServiceModule.acceptCallWhileSmsipRegistered(imsRegistration)) {
                return sipError;
            }
            if (!this.mRegMan.isVoWiFiSupported(phoneId) || !this.mPdnController.isEpdgConnected(phoneId) || this.mVolteServiceModule.isVowifiEnabled(phoneId)) {
                return SipErrorVzw.NOT_ACCEPTABLE_VOLTE_OFF;
            }
            return SipErrorVzw.VOWIFI_OFF;
        } else if (fromName != Mno.SKT && fromName != Mno.KT) {
            return SipErrorBase.NOT_ACCEPTABLE_HERE;
        } else {
            if (imsRegistration.hasService("mmtel-video")) {
                return sipError;
            }
            Log.i(str, "checkRejectIncomingCall: no mmtel or mmtel-video registered.");
            return SipErrorBase.NOT_ACCEPTABLE_HERE;
        }
    }

    public SipError getSipErrorAsSessionState(int i, int i2, boolean z, boolean z2, int i3, Mno mno) {
        SipError sipError = SipErrorBase.OK;
        synchronized (this.mVolteServiceModule.getSessionList(i)) {
            for (ImsCallSession next : this.mVolteServiceModule.getSessionList(i)) {
                if (next != null && i2 == 0 && ImsCallUtil.isCmcPrimaryType(next.getCmcType()) && (next.getCallState() == CallConstants.STATE.IncomingCall || next.getPreAlerting())) {
                    String str = LOG_TAG;
                    Log.i(str, "checkRejectIncomingCall: found incoming PD session " + next.mSessionId);
                    SipError sipErrorOnDialingState = getSipErrorOnDialingState(i2, next.getCmcType(), mno);
                    return sipErrorOnDialingState;
                } else if (next != null) {
                    if (!ImsCallUtil.isCmcPrimaryType(next.getCmcType())) {
                        if (next.getPhoneId() != i) {
                            continue;
                        } else {
                            CallConstants.STATE callState = next.getCallState();
                            if (!(callState == CallConstants.STATE.Idle || callState == CallConstants.STATE.ReadyToCall)) {
                                if (!ImsCallUtil.isDialingCallState(callState)) {
                                    if (z2 || z) {
                                        sipError = getSipErrorDuringCall(i3, next.getCallProfile().getCallType(), mno);
                                    }
                                }
                            }
                            String str2 = LOG_TAG;
                            Log.i(str2, "checkRejectIncomingCall: found dialing session " + next.mSessionId);
                            SipError sipErrorOnDialingState2 = getSipErrorOnDialingState(i2, next.mCmcType, mno);
                            return sipErrorOnDialingState2;
                        }
                    }
                }
            }
            return sipError;
        }
    }

    public SipError getSipErrorDuringCall(int i, int i2, Mno mno) {
        SipError sipError = SipErrorBase.OK;
        if (mno == Mno.VIVA_KUWAIT && i2 == 2 && i == 2) {
            return SipErrorBase.BUSY_HERE;
        }
        return ((mno == Mno.ZAIN_KUWAIT || mno == Mno.OOREDOO_KUWAIT) && i2 == 2) ? SipErrorBase.BUSY_HERE : sipError;
    }

    private boolean needRejectByTerminalSs(Context context, ImsRegistration imsRegistration, int i) {
        int phoneId = imsRegistration.getPhoneId();
        if (!GlobalSettingsManager.getInstance(context, phoneId).getBoolean(GlobalSettingsConstants.SS.CALLBARRING_BY_NETWORK, false) && getSipErrorForBarring(context, phoneId, i) == SipErrorBase.BUSY_HERE) {
            return true;
        }
        if (!GlobalSettingsManager.getInstance(context, phoneId).getBoolean(GlobalSettingsConstants.SS.CALLWAITING_BY_NETWORK, true) && imsRegistration.getImsProfile().getCmcType() == 0) {
            boolean userConfig = UserConfiguration.getUserConfig(context, phoneId, "enable_call_wait", true);
            int sessionCountByCmcType = this.mVolteServiceModule.getCmcServiceHelper().getSessionCountByCmcType(phoneId, 0);
            if (sessionCountByCmcType >= 1 && !userConfig) {
                String str = LOG_TAG;
                Log.i(str, "needRejectByTerminalSs: Terminal CW : " + userConfig + " callCount : " + sessionCountByCmcType + " reject call");
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public CallDetailInfo checkHasCallAndCallType(int i) {
        CallDetailInfo callDetailInfo = new CallDetailInfo();
        synchronized (this.mVolteServiceModule.getSessionList()) {
            for (ImsCallSession next : this.mVolteServiceModule.getSessionList()) {
                if (next == null || !ImsCallUtil.isCmcPrimaryType(next.getCmcType())) {
                    if (next != null && next.getPhoneId() == i) {
                        CallConstants.STATE callState = next.getCallState();
                        if (callState == CallConstants.STATE.InCall) {
                            callDetailInfo.setHasIncall(Boolean.TRUE);
                            callDetailInfo.setInCalltype(next.getCallProfile().getCallType());
                        } else if (callState == CallConstants.STATE.HeldCall || next.isRemoteHeld()) {
                            callDetailInfo.setHasHoldCall(Boolean.TRUE);
                        }
                        if (next.getCallProfile().isConferenceCall()) {
                            callDetailInfo.setHasConfCall(Boolean.TRUE);
                        }
                        if (next.getCallProfile().getCallType() == 9) {
                            callDetailInfo.setHasTtyCall(Boolean.TRUE);
                        }
                        if (callState == CallConstants.STATE.ModifyingCall || callState == CallConstants.STATE.ModifyRequested || callState == CallConstants.STATE.HoldingVideo || callState == CallConstants.STATE.ResumingVideo) {
                            callDetailInfo.setIsModifyOngoing(Boolean.TRUE);
                        }
                    }
                }
            }
        }
        return callDetailInfo;
    }

    protected class CallDetailInfo {
        Boolean hasConfCall;
        Boolean hasHoldCall;
        Boolean hasIncall;
        Boolean hasTtyCall;
        int inCalltype = 0;
        Boolean isModifyOngoing;

        public CallDetailInfo() {
            Boolean bool = Boolean.FALSE;
            this.isModifyOngoing = bool;
            this.hasTtyCall = bool;
            this.hasConfCall = bool;
            this.hasHoldCall = bool;
            this.hasIncall = bool;
        }

        public Boolean getHasIncall() {
            return this.hasIncall;
        }

        public void setHasIncall(Boolean bool) {
            this.hasIncall = bool;
        }

        public Boolean getHasHoldCall() {
            return this.hasHoldCall;
        }

        public void setHasHoldCall(Boolean bool) {
            this.hasHoldCall = bool;
        }

        public Boolean getHasConfCall() {
            return this.hasConfCall;
        }

        public void setHasConfCall(Boolean bool) {
            this.hasConfCall = bool;
        }

        public Boolean getHasTtyCall() {
            return this.hasTtyCall;
        }

        public void setHasTtyCall(Boolean bool) {
            this.hasTtyCall = bool;
        }

        public Boolean getIsModifyOngoing() {
            return this.isModifyOngoing;
        }

        public void setIsModifyOngoing(Boolean bool) {
            this.isModifyOngoing = bool;
        }

        public int getInCalltype() {
            return this.inCalltype;
        }

        public void setInCalltype(int i) {
            this.inCalltype = i;
        }

        public String toString() {
            return "CallDetailInfo [hasIncall=" + this.hasIncall + ", hasHoldCall=" + this.hasHoldCall + ", hasConfCall=" + this.hasConfCall + ", hasTtyCall=" + this.hasTtyCall + ", isModifyOngoing=" + this.isModifyOngoing + ", inCalltype=" + this.inCalltype + "]";
        }
    }
}
