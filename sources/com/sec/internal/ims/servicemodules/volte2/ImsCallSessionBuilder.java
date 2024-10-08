package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;

class ImsCallSessionBuilder {
    private static final String LOG_TAG = "ImsCallSessionBuilder";
    ImsCallSessionManager mIcsm;
    private boolean mIsEmergency;
    private Mno mMno;
    private boolean mNeedToSetCallToPending;
    private IPdnController mPdnController;
    private int mPhoneId;
    private CallProfile mProfile;
    private ImsRegistration mRegInfo;
    private final IRegistrationManager mRegMan;
    private ImsCallSessionFactory mSessionFactory;
    private int mSubId;
    private ITelephonyManager mTelephonyManager;
    private IVolteServiceModuleInternal mVolteServiceModule;

    ImsCallSessionBuilder(ImsCallSessionManager imsCallSessionManager, IVolteServiceModuleInternal iVolteServiceModuleInternal, ITelephonyManager iTelephonyManager, IPdnController iPdnController, IRegistrationManager iRegistrationManager, Looper looper) {
        this.mIcsm = imsCallSessionManager;
        this.mVolteServiceModule = iVolteServiceModuleInternal;
        this.mTelephonyManager = iTelephonyManager;
        this.mPdnController = iPdnController;
        this.mSessionFactory = new ImsCallSessionFactory(iVolteServiceModuleInternal, looper);
        this.mRegMan = iRegistrationManager;
    }

    public ImsCallSession createSession(Context context, CallProfile callProfile, ImsRegistration imsRegistration) throws RemoteException {
        if (callProfile != null) {
            this.mProfile = callProfile;
            this.mRegInfo = imsRegistration;
            parseArguments();
            processNeedToSetCallToPending(context);
            processNetworkType();
            checkExistingCallSessions();
            processImpuAndCmc();
            ImsCallSession create = this.mSessionFactory.create(this.mProfile, this.mRegInfo, this.mNeedToSetCallToPending);
            if (create != null) {
                if (this.mRegInfo == null && this.mNeedToSetCallToPending && this.mProfile.getCmcType() == 0 && !this.mProfile.isForceCSFB()) {
                    setPendingOutgoingCall(create);
                } else if (this.mVolteServiceModule.getIsLteRetrying(this.mPhoneId)) {
                    if (imsRegistration == null || 13 != imsRegistration.getRegiRat()) {
                        create.mRegistration = null;
                        setPendingOutgoingCall(create);
                    } else {
                        this.mVolteServiceModule.setIsLteRetrying(this.mPhoneId, false);
                        Log.i(LOG_TAG, "[createSession] Lte Retrying");
                    }
                }
                this.mIcsm.addCallSession(create);
                return create;
            }
            Log.e(LOG_TAG, "createSession: session create fail");
            throw new RemoteException();
        }
        Log.e(LOG_TAG, "profile is null");
        throw new RemoteException("Null CallProfile.");
    }

    private void checkCanMakeCallSession() throws RemoteException {
        if (this.mRegInfo == null) {
            Log.e(LOG_TAG, "cannot make new call session. not registered");
            throw new RemoteException("Not registered.");
        } else if (this.mMno == Mno.VZW && !this.mVolteServiceModule.isVowifiEnabled(this.mPhoneId) && this.mPdnController.isEpdgConnected(this.mPhoneId) && this.mProfile.getCallType() == 1) {
            Log.e(LOG_TAG, "cannot make new call session. currently in Registering");
            throw new RemoteException("Registering.");
        }
    }

    private void checkExistingCallSessions() throws RemoteException {
        if (!this.mProfile.isConferenceCall()) {
            for (ImsCallSession next : this.mIcsm.getSessionMap().values()) {
                checkOngoingCallForForkedSession(next);
                if (this.mRegInfo != null && next != null && next.getRegistration() != null && this.mRegInfo.getHandle() != next.getRegistration().getHandle()) {
                    Log.i(LOG_TAG, "skip different based regi");
                } else if (!(next == null || next.getCallState() == CallConstants.STATE.HeldCall || !ImsCallUtil.isOngoingCallState(next.getCallState()))) {
                    handleCallSessionDuringCall(next);
                }
            }
        }
    }

    private void checkOngoingCallForForkedSession(ImsCallSession imsCallSession) throws RemoteException {
        ImsRegistration imsRegistration = this.mRegInfo;
        if (imsRegistration != null && ImsCallUtil.isCmcPrimaryType(imsRegistration.getImsProfile().getCmcType()) && imsCallSession != null && imsCallSession.getRegistration() != null && this.mRegInfo.getPhoneId() != imsCallSession.getRegistration().getPhoneId() && !ImsRegistry.getCmcAccountManager().isSupportDualSimCMC() && imsCallSession.getCallState() != CallConstants.STATE.Idle) {
            String str = LOG_TAG;
            Log.e(str, "cannot make a forking session. ongoing call exists on the other sim. callId: " + imsCallSession.getCallId() + ", sessionId: " + imsCallSession.getSessionId() + ", callState: " + imsCallSession.getCallState());
            throw new RemoteException();
        }
    }

    private int getNetworkForCreateSession() {
        if (this.mIsEmergency) {
            ImsProfile imsProfile = this.mRegMan.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
            int e911PdnSelectionVowifi = imsProfile != null ? imsProfile.getE911PdnSelectionVowifi() : 0;
            String str = LOG_TAG;
            Log.i(str, "createSession: voiceNetwork = " + this.mTelephonyManager.getVoiceNetworkType(this.mSubId));
            if (e911PdnSelectionVowifi == 1 && this.mPdnController.isEpdgConnected(this.mPhoneId) && ImsConstants.EmergencyRat.IWLAN.equalsIgnoreCase(this.mProfile.getEmergencyRat())) {
                Log.i(str, "createSession: use IMS PDN for WiFi e911 for e911pdnpolicy(IMSPDN_IF_IPC_RAT_EPDG).");
                return 11;
            } else if (this.mMno.isKor() && !NetworkUtil.is3gppPsVoiceNetwork(this.mTelephonyManager.getVoiceNetworkType(this.mSubId)) && this.mTelephonyManager.getVoiceNetworkType(this.mSubId) != 0 && this.mProfile.getCallType() == 8) {
                Log.i(str, "createSession: use IMS PDN for KOR 3g psvt e911.");
                return 11;
            } else if (!this.mMno.isAus() || e911PdnSelectionVowifi != 1 || !ImsConstants.EmergencyRat.IWLAN.equalsIgnoreCase(this.mProfile.getEmergencyRat())) {
                return 15;
            } else {
                Log.i(str, "createSession: AU use IMS PDN for WiFi e911.");
                return 11;
            }
        } else {
            ImsRegistration imsRegistration = this.mRegInfo;
            if (imsRegistration != null) {
                return imsRegistration.getNetworkType();
            }
            if (this.mNeedToSetCallToPending) {
                return 11;
            }
            return -1;
        }
    }

    private void handleCallSessionDuringCall(ImsCallSession imsCallSession) throws RemoteException {
        if (this.mIsEmergency && this.mMno == Mno.VZW) {
            try {
                Log.i(LOG_TAG, "release active call before E911 dialing");
                if (imsCallSession.getCallState() == CallConstants.STATE.IncomingCall) {
                    imsCallSession.reject(2);
                } else {
                    imsCallSession.terminate(5, true);
                }
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "createSession: ", e);
            }
        } else if (!isAllowUssdDuringCall(this.mMno) || this.mProfile.getCallType() != 12) {
            ImsRegistration imsRegistration = this.mRegInfo;
            if (imsRegistration == null || !ImsCallUtil.isCmcPrimaryType(imsRegistration.getImsProfile().getCmcType())) {
                String str = LOG_TAG;
                Log.e(str, "cannot make new call session. another call already exist callId: " + imsCallSession.getCallId() + ", sessionId: " + imsCallSession.getSessionId() + ", callState: " + imsCallSession.getCallState());
                throw new RemoteException();
            }
            Log.e(LOG_TAG, "allow CMC 2ndCall in PD");
        } else {
            Log.e(LOG_TAG, "Operator allow USSD during call");
        }
    }

    private void parseArguments() {
        int phoneId = this.mProfile.getPhoneId();
        this.mPhoneId = phoneId;
        ImsRegistration imsRegistration = this.mRegInfo;
        if (imsRegistration == null) {
            this.mMno = SimUtil.getSimMno(phoneId);
        } else {
            this.mMno = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
        }
        this.mSubId = SimUtil.getSubId(this.mPhoneId);
        this.mIsEmergency = this.mProfile.getCallType() == 7 || this.mProfile.getCallType() == 8;
        this.mNeedToSetCallToPending = this.mMno.isKor();
    }

    private void processImpuAndCmc() {
        ImsUri activeImpu = this.mVolteServiceModule.getActiveImpu();
        if (TextUtils.isEmpty(this.mProfile.getLineMsisdn()) && activeImpu != null) {
            String str = LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("createSession: originating from ");
            sb.append(IMSLog.checker(activeImpu + ""));
            Log.i(str, sb.toString());
            this.mProfile.setLineMsisdn(UriUtil.getMsisdnNumber(activeImpu));
            this.mProfile.setOriginatingUri(activeImpu);
        }
        ImsRegistration imsRegistration = this.mRegInfo;
        if (imsRegistration != null && imsRegistration.getImsProfile().getCmcType() > 0) {
            this.mVolteServiceModule.updateCmcP2pList(this.mRegInfo, this.mProfile);
        }
    }

    private void processNeedToSetCallToPending(Context context) {
        if (this.mMno != Mno.VZW || !ImsUtil.isCdmalessEnabled(context, this.mPhoneId) || this.mRegInfo != null || this.mIsEmergency) {
            if (!this.mProfile.isForceCSFB()) {
                if (this.mProfile.getCmcType() == 2) {
                    ImsRegistration imsRegistration = this.mRegInfo;
                    if (imsRegistration != null && imsRegistration.getImsProfile().getCmcType() == 2) {
                        return;
                    }
                } else {
                    return;
                }
            }
            Log.i(LOG_TAG, "set needToPendingCall to true when SD or VoLTE is not registered");
            this.mNeedToSetCallToPending = true;
            this.mRegInfo = null;
            return;
        }
        Log.e(LOG_TAG, "createSession: Cdmaless needToPendingCall");
        this.mNeedToSetCallToPending = true;
    }

    private void processNetworkType() throws RemoteException {
        this.mProfile.setNetworkType(getNetworkForCreateSession());
        if (this.mRegInfo == null && this.mIsEmergency && this.mProfile.getNetworkType() == 11) {
            Log.i(LOG_TAG, "Need to pending E911 call over VoWifi using IMS PDN.");
            this.mNeedToSetCallToPending = true;
        }
        if (!this.mNeedToSetCallToPending) {
            if (!this.mIsEmergency) {
                checkCanMakeCallSession();
            }
            if (this.mProfile.getNetworkType() != 15 && this.mRegInfo == null) {
                Log.e(LOG_TAG, "cannot make new call session. not registered");
                throw new RemoteException("Not registered.");
            }
        }
    }

    private void setPendingOutgoingCall(ImsCallSession imsCallSession) {
        Log.i(LOG_TAG, "try to regi for pending outgoing call session");
        imsCallSession.setPendingCall(true);
        this.mIcsm.setPendingOutgoingCall(imsCallSession);
    }

    private boolean isAllowUssdDuringCall(Mno mno) {
        boolean z = mno.isSwa() && mno != Mno.MOBITEL_LK;
        boolean z2 = (!mno.isMea() || mno == Mno.MTN_IRAN || mno == Mno.OOREDOO_QATAR) ? false : true;
        if (mno.isOneOf(Mno.ATT, Mno.TMOUS, Mno.DISH) || mno.isEur() || mno.isSea() || z || z2 || mno.isOce() || mno.isJpn()) {
            return true;
        }
        return false;
    }
}
