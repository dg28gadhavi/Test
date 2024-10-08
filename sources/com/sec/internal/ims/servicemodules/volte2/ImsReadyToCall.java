package com.sec.internal.ims.servicemodules.volte2;

import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.Dialog;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.UserConfiguration;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.imsdc.IdcImsCallSessionData;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.volte2.data.CallSetupData;
import com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;

public class ImsReadyToCall extends CallState {
    ImsReadyToCall(CallStateMachine callStateMachine) {
        super(callStateMachine);
    }

    public void enter() {
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.callType = 0;
        callStateMachine.errorCode = -1;
        callStateMachine.errorMessage = "";
        callStateMachine.isLocationAcquiringTriggered = false;
        callStateMachine.mRequestLocation = false;
        callStateMachine.mIsStartCameraSuccess = true;
        this.mSession.setIsEstablished(false);
        this.mCsm.mCallInitTime = SystemClock.elapsedRealtime();
        Log.i(this.LOG_TAG, "Enter [ReadyToCall]");
    }

    public boolean processMessage(Message message) {
        String str = this.LOG_TAG;
        Log.i(str, "[ReadyToCall] processMessage " + message.what);
        switch (message.what) {
            case 1:
            case 3:
            case 4:
            case CallStateMachine.ON_E911_PERM_FAIL /*308*/:
                Log.e(this.LOG_TAG, "[ReadyToCall] Call session got dropped early!");
                return false;
            case 11:
                start_ReadyToCall(message);
                return true;
            case 12:
                pulling_ReadyToCall(message);
                return true;
            case 13:
                locAcq_ReadyToCall();
                return true;
            case 15:
                setCallProfile_ReadyToCall((String) message.obj);
                return true;
            case 21:
                CallStateMachine callStateMachine = this.mCsm;
                callStateMachine.transitionTo(callStateMachine.mIncomingCall);
                return true;
            case 31:
                this.mCsm.removeMessages(208);
                return true;
            case 41:
                established_ReadyToCall();
                return true;
            case 52:
                Log.i(this.LOG_TAG, "[ReadyToCall] Postpone update request till established state");
                this.mCsm.deferMessage(message);
                return true;
            case 100:
                return false;
            case 208:
                tryingTimeout_ReadyToCall();
                return true;
            case 211:
                Log.i(this.LOG_TAG, "[ReadyToCall] registration is not available.");
                this.mCsm.notifyOnError(this.mMno.isKor() ? ImSessionEvent.CONFERENCE_INFO_UPDATED : 1601, "No registration.", 0);
                CallStateMachine callStateMachine2 = this.mCsm;
                callStateMachine2.transitionTo(callStateMachine2.mEndingCall);
                this.mCsm.sendMessageDelayed(3, 100);
                return true;
            case 303:
                if (this.mCsm.getPendingCall() && ImsCallUtil.isE911Call(this.mSession.getCallProfile().getCallType())) {
                    Log.e(this.LOG_TAG, "[ReadyToCall] pending Emergency call failed");
                    return false;
                }
            case 500:
                locAcqTimeout_ReadyToCall();
                return true;
            case 501:
                locAcqSuccess_ReadyToCall();
                return true;
            case 5000:
                dbrLost_ReadyToCall(message);
                return true;
        }
        String str2 = this.LOG_TAG;
        Log.e(str2, "[" + getName() + "] msg:" + message.what + " ignored !!!");
        return true;
    }

    /* access modifiers changed from: protected */
    public void setCallProfile_ReadyToCall(String str) {
        String str2;
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration != null && imsRegistration.getImsProfile() != null && this.mRegistration.getImsProfile().getCmcType() == 0 && !this.mMno.isJpn()) {
            String checkCallControl = this.mTelephonyManager.checkCallControl(str, this.mSession.getPhoneId());
            if (checkCallControl == null || ImsCallUtil.NOT_ALLOWED.equals(checkCallControl)) {
                if (this.mMno.isKor()) {
                    this.mCsm.sendMessage(4, 0, -1, new SipError(Id.REQUEST_VSH_ACCEPT_SESSION, "target is NotAllowed from STK"));
                    return;
                } else {
                    this.mCsm.sendMessage(4, 0, -1, new SipError(1004, "target is NotAllowed from STK"));
                    return;
                }
            } else if (!checkCallControl.equals(str)) {
                String str3 = this.LOG_TAG;
                Log.i(str3, "Change the target number by STK : " + IMSLog.checker(str) + " -> " + IMSLog.checker(checkCallControl));
                this.mSession.getCallProfile().setDialingNumber(checkCallControl);
                str = checkCallControl;
            }
        }
        ImsRegistration imsRegistration2 = this.mRegistration;
        if (imsRegistration2 == null || imsRegistration2.getImsProfile() == null || this.mRegistration.getImsProfile().getCmcType() <= 0) {
            str2 = null;
        } else {
            String deviceIdForCmc = getDeviceIdForCmc(str);
            str2 = deviceIdForCmc;
            str = setDialingNumberForCmc();
        }
        String handleCLI = handleCLI(str);
        if (TextUtils.isEmpty(this.mSession.getCallProfile().getDialingNumber())) {
            this.mSession.getCallProfile().setDialingNumber(handleCLI);
        }
        if (this.mSession.needUpdateEccUrn()) {
            this.mSession.getCallProfile().setUrn(this.mModule.updateEccUrn(this.mSession.getPhoneId(), this.mSession.getCallProfile().getDialingNumber()));
            String str4 = this.LOG_TAG;
            Log.i(str4, "UpdateECCUrn : " + this.mSession.getCallProfile().getUrn());
        }
        if (isSoftphoneE911(this.mSession.getCallProfile().getCallType())) {
            setNubmerForSoftphoneE911(handleCLI);
            this.mCsm.sendMessage(13);
        } else if (!isRequiredLocationE911(this.mSession.getCallProfile()) || (this.mMno.isOneOf(Mno.ATT, Mno.VZW) && this.mModule.isRoaming(this.mSession.getPhoneId()))) {
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.sendMessage(callStateMachine.obtainMessage(11, (Object) str2));
        } else {
            this.mCsm.sendMessage(13);
        }
    }

    private String handleCLI(String str) {
        if (this.mMno != Mno.TMOUS) {
            return str;
        }
        if (!str.startsWith("*67*") && str.startsWith("*67")) {
            this.mSession.getCallProfile().setCLI("*67");
            String substring = str.substring(3);
            String str2 = this.LOG_TAG;
            Log.i(str2, "handleCLI(): *67 Call : cli=*67 target=" + substring);
            return substring;
        } else if (!str.startsWith("*82")) {
            return str;
        } else {
            this.mSession.getCallProfile().setCLI("*82");
            String substring2 = str.substring(3);
            String str3 = this.LOG_TAG;
            Log.i(str3, "handleCLI(): *82 Call : cli=*82 target=" + substring2);
            return substring2;
        }
    }

    private String setDialingNumberForCmc() {
        String impi = this.mRegistration.getImpi();
        this.mSession.getCallProfile().setDialingNumber(impi);
        String str = this.LOG_TAG;
        Log.i(str, "this is CMC call / target=" + impi + " / Lettering=" + IMSLog.checker(this.mSession.getCallProfile().getLetteringText()));
        return impi;
    }

    private String getDeviceIdForCmc(String str) {
        if (this.mRegistration == null || !this.mSession.getCallProfile().isSamsungMdmnCall()) {
            return null;
        }
        if (ImsCallUtil.isCmcPrimaryType(this.mRegistration.getImsProfile().getCmcType())) {
            return this.mSession.getCallProfile().getDialingDevice();
        }
        this.mSession.getCallProfile().setLetteringText(str);
        return this.mRegistration.getImsProfile().getPriDeviceIdWithURN();
    }

    private void setNubmerForSoftphoneE911(String str) {
        this.mSession.getCallProfile().setCLI("*31#");
        if (str.length() <= 3) {
            return;
        }
        if (str.startsWith("*82") || (!str.startsWith("*67*") && str.startsWith("*67"))) {
            Log.i(this.LOG_TAG, "Remove CLI code for Softphone E911 call");
            this.mSession.getCallProfile().setDialingNumber(str.substring(3));
        }
    }

    private boolean isSoftphoneE911(int i) {
        ImsRegistration imsRegistration = this.mRegistration;
        return imsRegistration != null && imsRegistration.getImsProfile().isSoftphoneEnabled() && i == 13;
    }

    private void start_ReadyToCall(Message message) {
        if (handleNotREGStatus()) {
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.callType = callStateMachine.mSession.getCallProfile().getCallType();
            if (handleBreakBeforeToMakeCall(message)) {
                handleAutomaticMode();
                String dialingNumber = this.mSession.getCallProfile().getDialingNumber();
                ImsUri destUri = getDestUri(message, dialingNumber);
                if (destUri == null) {
                    Log.e(this.LOG_TAG, "dest Uri couldn't be null!!!!");
                    this.mCsm.sendMessage(4, 0, -1, new SipError(1004, "invalid remote uri"));
                    return;
                }
                CallSetupData callSetupData = getCallSetupData(dialingNumber, destUri);
                if (handleCallBarring() && handleSessionId(callSetupData)) {
                    CallStateMachine callStateMachine2 = this.mCsm;
                    callStateMachine2.transitionTo(callStateMachine2.mOutgoingCall);
                }
            }
        }
    }

    private ImsUri getDestUri(Message message, String str) {
        String str2;
        if (this.mSession.getCallProfile().getUrn() != null) {
            return ImsUri.parse(this.mSession.getCallProfile().getUrn());
        }
        if (this.mSession.getCallProfile().isSamsungMdmnCall()) {
            str2 = (String) message.obj;
            String str3 = this.LOG_TAG;
            Log.i(str3, "put deviceID as " + str2);
        } else {
            str2 = null;
        }
        if (isLGUspecificNumber(str)) {
            return this.mSession.buildUri(str.substring(0, str.length() - 1), str2, this.mCsm.callType);
        }
        return this.mSession.buildUri(str, str2, this.mCsm.callType);
    }

    private CallSetupData getCallSetupData(String str, ImsUri imsUri) {
        CallSetupData callSetupData = new CallSetupData(imsUri, str, this.mCsm.callType, this.mSession.getCallProfile().getCLI());
        String str2 = this.LOG_TAG;
        Log.i(str2, "CLI : " + this.mSession.getCallProfile().getCLI() + " LetteringText : " + IMSLog.checker(this.mSession.getCallProfile().getLetteringText()));
        if (ImsCallUtil.isCmcPrimaryType(this.mSession.getCmcType()) && !this.mSession.getCallProfile().getCLI().equals("#31#") && TextUtils.isEmpty(this.mSession.getCallProfile().getLetteringText())) {
            Log.i(this.LOG_TAG, "change cli to unknown");
            callSetupData.setCli(NSDSNamespaces.NSDSSimAuthType.UNKNOWN);
        }
        if (isLGUspecificNumber(str)) {
            callSetupData.setOriginatingUri(this.mSession.getSecondImpu(this.mRegistration));
        } else {
            callSetupData.setOriginatingUri(this.mSession.getOriginatingUri());
        }
        callSetupData.setLetteringText(this.mSession.getCallProfile().getLetteringText());
        callSetupData.setAlertInfo(this.mSession.getCallProfile().getAlertInfo());
        callSetupData.setLteEpsOnlyAttached(this.mModule.getLteEpsOnlyAttached(this.mSession.getPhoneId()));
        callSetupData.setP2p(this.mSession.getCallProfile().getP2p());
        callSetupData.setCmcBoundSessionId(this.mSession.getCallProfile().getCmcBoundSessionId());
        callSetupData.setComposerData(this.mSession.getCallProfile().getComposerData());
        if (isATTSoftPhoneCall() && this.mCsm.callType == 13) {
            callSetupData.setPEmergencyInfo(this.mSession.getPEmergencyInfo(this.mRegistrationManager.getImpi(this.mRegistration.getImsProfile(), this.mSession.getPhoneId()), (String) null));
        }
        if (this.mSession.getCmcType() == 2 && this.mSession.getCallProfile().getReplaceSipCallId() != null) {
            String str3 = this.LOG_TAG;
            Log.i(str3, "set replace call id " + this.mSession.getCallProfile().getReplaceSipCallId());
            callSetupData.setReplaceCallId(this.mSession.getCallProfile().getReplaceSipCallId());
            this.mCsm.mIsCmcHandover = true;
        }
        callSetupData.setCmcEdCallSlot(this.mSession.getCallProfile().getCmcEdCallSlot());
        if (this.mSession.getIdcData() != null && this.mModule.isSupportImsDataChannel(this.mSession.getPhoneId())) {
            Log.i(this.LOG_TAG, "[IDC] set bootstrap_sdp");
            this.mModule.getIdcServiceHelper().onImsOutgoingCallIdcEvent(this.mSession);
            callSetupData.setIdcExtra(IdcExtra.getBuilder().setSdp(this.mSession.getIdcData().getLocalBdcSdp()).build().encode());
            this.mSession.getIdcData().transitState(IdcImsCallSessionData.State.NEGOTIATING);
        }
        return callSetupData;
    }

    private void pulling_ReadyToCall(Message message) {
        String str;
        int i;
        if (this.mRegistration == null || this.mModule.isProhibited(this.mSession.getPhoneId())) {
            Log.e(this.LOG_TAG, "pulling: registration is not available.");
            this.mCsm.sendMessage(4, 0, -1, new SipError(1003, "No registration."));
            return;
        }
        Bundle bundle = (Bundle) message.obj;
        String string = bundle.getString("msisdn");
        Dialog parcelable = bundle.getParcelable("targetDialog");
        String targetUri = getTargetUri(string, parcelable);
        String str2 = this.LOG_TAG;
        Log.i(str2, "[ReadyToCall] pullingCall targetUri " + IMSLog.checker(targetUri));
        ImsUri originatingUri = this.mSession.getOriginatingUri();
        if (this.mSession.getCmcType() > 0) {
            parcelable.setMdmnExtNumber(parcelable.getSessionDescription());
        }
        if (!ImsCallUtil.isCmcPrimaryType(this.mSession.getCmcType())) {
            Log.i(this.LOG_TAG, "bindToNetwork for Pulling");
            this.mSession.mMediaController.bindToNetwork(this.mRegistration.getNetwork());
        }
        IVolteServiceInterface iVolteServiceInterface = this.mVolteSvcIntf;
        int handle = this.mRegistration.getHandle();
        if (originatingUri == null) {
            str = null;
        } else {
            str = originatingUri.toString();
        }
        int pullingCall = iVolteServiceInterface.pullingCall(handle, targetUri, string, str, parcelable, this.mSession.getCallProfile().getP2p());
        String str3 = this.LOG_TAG;
        Log.i(str3, "[ReadyToCall] pullingCall() returned session id " + pullingCall);
        if (pullingCall < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1002, "stack return -1"));
            return;
        }
        this.mSession.setSessionId(pullingCall);
        this.mCsm.isStartedCamera(parcelable.getCallType(), false);
        String remoteNumber = parcelable.getRemoteNumber();
        if (parcelable.getDirection() == 1) {
            i = 3;
            if (this.mMno == Mno.VZW) {
                remoteNumber = ImsCallUtil.getRemoteCallerId(new NameAddr("", parcelable.getRemoteUri()), this.mMno, Debug.isProductShip());
                if (!ImsRegistry.getPdnController().isVoiceRoaming(this.mSession.getPhoneId())) {
                    remoteNumber = ImsCallUtil.removeUriPlusPrefix(remoteNumber, Debug.isProductShip());
                }
            } else {
                remoteNumber = ImsCallUtil.getRemoteCallerId(new NameAddr(parcelable.getRemoteDispName(), parcelable.getRemoteUri()), this.mMno, Debug.isProductShip());
            }
        } else {
            i = 2;
        }
        String str4 = this.LOG_TAG;
        Log.i(str4, "remoteNumber : " + IMSLog.checker(remoteNumber));
        this.mSession.getCallProfile().setDowngradedVideoCall(parcelable.isVideoPortZero());
        this.mSession.getCallProfile().setDirection(i);
        this.mSession.getCallProfile().setDialogId(parcelable.getDialogId());
        handleCMCTransferCall(parcelable, remoteNumber);
        String str5 = this.LOG_TAG;
        Log.i(str5, "DialingNumber : " + IMSLog.checker(this.mSession.getCallProfile().getDialingNumber()));
        if (ImsCallUtil.isCmcSecondaryType(this.mSession.getCmcType())) {
            startCMC100Timer_ReadyToCall();
        }
    }

    private String getTargetUri(String str, Dialog dialog) {
        String str2;
        String encode = this.mSession.buildUri(str, (String) null, dialog.getCallType()).encode();
        if (!isATTSoftPhoneCall()) {
            return (this.mMno != Mno.VZW || this.mRegistration.getPreferredImpu() == null) ? encode : this.mRegistration.getPreferredImpu().toString();
        }
        String pullingServerUri = this.mRegistration.getImsProfile().getPullingServerUri();
        if (dialog.getLocalUri().contains("gr=") || !TextUtils.isEmpty(dialog.getLocalDispName())) {
            str2 = pullingServerUri.replace("[CALLTYPE]", "SP");
        } else {
            str2 = pullingServerUri.replace("[CALLTYPE]", ImsConstants.EmergencyRat.LTE);
        }
        if (dialog.isHeld()) {
            return str2.replace("[CALLSTATE]", "hold");
        }
        return str2.replace("[CALLSTATE]", SoftphoneNamespaces.SoftphoneCallHandling.ACTIVE);
    }

    private void established_ReadyToCall() {
        IRegistrationGovernor registrationGovernor;
        int i;
        if (this.mSession.getCallProfile().isPullCall()) {
            Log.i(this.LOG_TAG, "Pulling Call Establish");
            ImsRegistration imsRegistration = this.mRegistration;
            if (!(imsRegistration == null || (registrationGovernor = this.mRegistrationManager.getRegistrationGovernor(imsRegistration.getHandle())) == null)) {
                IRegistrationGovernor.CallEvent callEvent = IRegistrationGovernor.CallEvent.EVENT_CALL_ESTABLISHED;
                if (this.mSession.getCallProfile().isDowngradedVideoCall()) {
                    i = 2;
                } else {
                    i = this.mSession.getCallProfile().getCallType();
                }
                registrationGovernor.onCallStatus(callEvent, (SipError) null, i);
            }
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mInCall);
        }
    }

    private void dbrLost_ReadyToCall(Message message) {
        if (this.mSession.getCallProfile().getDirection() == 1) {
            this.mCsm.callType = this.mSession.getCallProfile().getCallType();
            if (message.arg1 != 1) {
                return;
            }
            if (this.mVolteSvcIntf.rejectCall(this.mSession.getSessionId(), this.mCsm.callType, SipErrorBase.PRECONDITION_FAILURE) < 0) {
                this.mCsm.sendMessage(4, 0, -1, new SipError(Id.REQUEST_SIP_DIALOG_OPEN, ""));
                return;
            }
            if (this.mModule.isNotifyRejectedCall(this.mSession.getPhoneId())) {
                this.mSession.getCallProfile().setRejectCause(1613);
            }
            this.mCsm.notifyOnEnded(Id.REQUEST_SIP_DIALOG_OPEN);
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mEndingCall);
        }
    }

    private void locAcq_ReadyToCall() {
        Log.i(this.LOG_TAG, "[ReadyToCall] Enable Location Provider & Request Location Acquiring");
        IGeolocationController geolocationController = ImsRegistry.getGeolocationController();
        if (geolocationController != null) {
            this.mCsm.mRequestLocation = geolocationController.startGeolocationUpdate(this.mSession.getPhoneId(), true);
            int locationAcquireTime = getLocationAcquireTime(this.mSession.getPhoneId(), this.mSession.getCallProfile());
            IMSLog.c(LogClass.VOLTE_GET_GEOLOCATION, this.mSession.getPhoneId() + "," + this.mSession.getSessionId() + "," + (this.mCsm.mRequestLocation ? 1 : 0) + "," + locationAcquireTime);
            CallStateMachine callStateMachine = this.mCsm;
            if (callStateMachine.mRequestLocation) {
                callStateMachine.sendMessageDelayed(500, (long) locationAcquireTime);
                this.mCsm.isLocationAcquiringTriggered = true;
                return;
            }
            ImsRegistration imsRegistration = this.mRegistration;
            if ((imsRegistration == null || !imsRegistration.getImsProfile().isSoftphoneEnabled()) && !isRequiredLocationE911(this.mSession.getCallProfile())) {
                this.mCsm.sendMessage(14);
            } else {
                this.mCsm.sendMessage(11);
            }
        }
    }

    private int getLocationAcquireTime(int i, CallProfile callProfile) {
        ImsProfile imsProfile;
        if (isATTSoftPhoneCall()) {
            imsProfile = this.mRegistrationManager.getImsProfile(i, ImsProfile.PROFILE_TYPE.VOLTE);
        } else {
            imsProfile = this.mRegistrationManager.getImsProfile(i, ImsProfile.PROFILE_TYPE.EMERGENCY);
        }
        if (imsProfile == null) {
            Log.i(this.LOG_TAG, "[ReadyToCall] imsProfile is null, use default");
        } else if (ImsConstants.EmergencyRat.IWLAN.equalsIgnoreCase(callProfile.getEmergencyRat())) {
            return imsProfile.getLocationAcquireFail();
        } else {
            if (ImsConstants.EmergencyRat.LTE.equalsIgnoreCase(callProfile.getEmergencyRat()) || ImsConstants.EmergencyRat.NR.equalsIgnoreCase(callProfile.getEmergencyRat())) {
                return imsProfile.getLocationAcquireFailVolte();
            }
        }
        return ImsCallUtil.DEFAULT_LOCATION_ACQUIRE_TIME;
    }

    private void locAcqSuccess_ReadyToCall() {
        if (this.mCsm.isLocationAcquiringTriggered) {
            Log.i(this.LOG_TAG, "[ReadyToCall] Location Acquiring Success -> Start Call");
            if (ImsRegistry.getGeolocationController() != null) {
                this.mCsm.removeMessages(500);
                this.mCsm.isLocationAcquiringTriggered = false;
            }
            IMSLog.c(LogClass.VOLTE_GEOLOCATION_SUCCESS, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
            ImsRegistration imsRegistration = this.mRegistration;
            if ((imsRegistration == null || !imsRegistration.getImsProfile().isSoftphoneEnabled()) && !isRequiredLocationE911(this.mSession.getCallProfile())) {
                this.mCsm.sendMessage(14);
            } else {
                this.mCsm.sendMessage(11);
            }
        }
    }

    private void locAcqTimeout_ReadyToCall() {
        if (this.mCsm.isLocationAcquiringTriggered) {
            Log.i(this.LOG_TAG, "[ReadyToCall] Location Acquiring Timeout & Get Last known Location -> Start Call");
            this.mCsm.isLocationAcquiringTriggered = false;
            IGeolocationController geolocationController = ImsRegistry.getGeolocationController();
            if (geolocationController != null) {
                geolocationController.updateGeolocationFromLastKnown(this.mSession.getPhoneId());
            }
            IMSLog.c(LogClass.VOLTE_GEOLOCATION_FAIL, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
            ImsRegistration imsRegistration = this.mRegistration;
            if ((imsRegistration == null || !imsRegistration.getImsProfile().isSoftphoneEnabled()) && !isRequiredLocationE911(this.mSession.getCallProfile())) {
                this.mCsm.sendMessage(14);
            } else {
                this.mCsm.sendMessage(11);
            }
        }
    }

    private void startCMC100Timer_ReadyToCall() {
        Log.i(this.LOG_TAG, "[ReadyToCall] Start 100 Trying Timer (5000 msec) for pulling.");
        this.mCsm.sendMessageDelayed(208, 5000);
    }

    private void tryingTimeout_ReadyToCall() {
        Log.i(this.LOG_TAG, "[ReadyToCall] 100 Trying Timeout - Call Terminate");
        this.mCsm.notifyOnError(503, "100 Trying Timeout", 0);
        this.mCsm.sendMessage(1, 17);
    }

    private void handleCMCTransferCall(Dialog dialog, String str) {
        if (this.mSession.getCmcType() > 0) {
            Log.i(this.LOG_TAG, "Update DialingNumber from sessionDescription for CMC call pull");
            this.mSession.getCallProfile().setDialingNumber(dialog.getSessionDescription());
            return;
        }
        this.mSession.getCallProfile().setDialingNumber(str);
    }

    private void handleAutomaticMode() {
        if (this.mModule.getAutomaticMode(this.mSession.getPhoneId())) {
            CallStateMachine callStateMachine = this.mCsm;
            int i = callStateMachine.callType;
            if (i == 1) {
                callStateMachine.callType = 14;
            } else if (i == 2) {
                if (!this.mMno.isOneOf(Mno.TMOUS, Mno.DISH, Mno.VZW, Mno.ATT)) {
                    this.mCsm.callType = 15;
                }
            } else if (i == 7) {
                callStateMachine.callType = 18;
            } else if (i == 8) {
                callStateMachine.callType = 19;
            } else if (i == 5) {
                callStateMachine.callType = 16;
            } else if (i == 6) {
                callStateMachine.callType = 17;
            }
            this.mSession.getCallProfile().setCallType(this.mCsm.callType);
            if (ImsCallUtil.isRttCall(this.mCsm.callType) && !ImsRegistry.getPdnController().isEpdgConnected(this.mSession.getPhoneId())) {
                ImsCallSession imsCallSession = this.mSession;
                imsCallSession.startRttDedicatedBearerTimer(this.mModule.getRttDbrTimer(imsCallSession.getPhoneId()));
            }
        }
    }

    private boolean handleBreakBeforeToMakeCall(Message message) {
        ImsRegistration imsRegistration;
        if (!ImsCallUtil.isCmcSecondaryType(this.mSession.getCmcType()) || this.mCsm.callType != 2) {
            boolean z = ImsRegistry.getBoolean(this.mSession.getPhoneId(), GlobalSettingsConstants.Call.SUPPORT_SSAC_NR, false);
            ImsRegistration imsRegistration2 = this.mRegistration;
            if (imsRegistration2 != null && ((imsRegistration2.getCurrentRat() == 13 || z) && this.mModule.isCallBarredBySSAC(this.mSession.getPhoneId(), this.mCsm.callType))) {
                Log.e(this.LOG_TAG, "start: call barred by ssac.");
                if (this.mMno == Mno.KDDI) {
                    this.mCsm.sendMessage(4, 0, -1, new SipError(2699, "Call Barred due to SSAC"));
                } else {
                    this.mCsm.sendMessage(4, 0, -1, new SipError(1116, "Call Barred due to SSAC"));
                }
                return false;
            } else if (this.mMno == Mno.ATT && this.mCsm.callType == 12 && (imsRegistration = this.mRegistration) != null && imsRegistration.getCurrentRat() == 13 && ImsRegistry.getPdnController().getVopsIndication(this.mSession.getPhoneId()) == VoPsIndication.NOT_SUPPORTED) {
                this.mCsm.sendMessage(4, 0, -1, new SipError(403, "VoPS is not supported"));
                return false;
            } else {
                ImsRegistration imsRegistration3 = this.mRegistration;
                if (imsRegistration3 == null || !this.mRegistrationManager.isSuspended(imsRegistration3.getHandle())) {
                    ImsRegistration imsRegistration4 = this.mRegistration;
                    if (imsRegistration4 == null || !this.mRegistrationManager.isSuspended(imsRegistration4.getHandle())) {
                        if (this.mRegistration != null && ImsCallUtil.isTtyCall(this.mCsm.callType) && (this.mRegistration.getImsProfile().getTtyType() == 1 || this.mRegistration.getImsProfile().getTtyType() == 3)) {
                            Log.i(this.LOG_TAG, "CS TTY Enable so do not allow outgoing IMS TTY call");
                            this.mCsm.callType = 1;
                        }
                        if (ImsCallUtil.isVideoCall(this.mCsm.callType) && !this.mModule.isCallServiceAvailable(this.mSession.getPhoneId(), "mmtel-video") && this.mCsm.callType != 8) {
                            Log.i(this.LOG_TAG, "Call Type change Video to Voice for no video feature tag");
                            this.mCsm.callType = 1;
                        }
                        return true;
                    }
                    ImsCallSession imsCallSession = this.mSession;
                    if (!imsCallSession.mHandOffTimedOut) {
                        imsCallSession.mHandOffTimedOut = true;
                        Log.e(this.LOG_TAG, "Wait 1.5 sec for the SUSPEND state to change");
                        this.mCsm.sendMessageDelayed(Message.obtain(message), 1500);
                    } else {
                        Log.e(this.LOG_TAG, "cannot make new call session. currently in suspend");
                        this.mCsm.sendMessage(4, 0, -1, new SipError(2511, "Suspended."));
                    }
                    return false;
                }
                Log.e(this.LOG_TAG, "cannot make new call session. currently in suspend");
                this.mCsm.sendMessage(4, 0, -1, new SipError(2511, "Suspended."));
                return false;
            }
        } else {
            Log.e(this.LOG_TAG, "start: SD is not supported VT.");
            this.mSession.getCallProfile().setDirection(0);
            this.mCsm.sendMessage(4, 0, -1, new SipError(AECNamespace.HttpResponseCode.UNSUPPORTED_MEDIA_TYPE, "SD_NOT_SUPPORTED_VT"));
            return false;
        }
    }

    private boolean isLGUspecificNumber(String str) {
        if (this.mMno != Mno.LGU || str.length() <= 1 || !str.endsWith("#")) {
            return false;
        }
        return true;
    }

    private boolean handleSessionId(CallSetupData callSetupData) {
        int i;
        ImsCallSession session;
        if (this.mRegistration != null) {
            if (!ImsCallUtil.isCmcPrimaryType(this.mSession.getCmcType())) {
                Log.i(this.LOG_TAG, "bindToNetwork for MO");
                this.mSession.mMediaController.bindToNetwork(this.mRegistration.getNetwork());
            }
            if (ImsCallUtil.isCmcPrimaryType(this.mSession.getCmcType())) {
                ICmcServiceHelperInternal cmcServiceHelper = this.mModule.getCmcServiceHelper();
                CallConstants.STATE state = CallConstants.STATE.InCall;
                if (!(cmcServiceHelper.getSessionByCmcTypeAndState(1, state) == null && this.mModule.getCmcServiceHelper().getSessionByCmcTypeAndState(3, state) == null && this.mModule.getCmcServiceHelper().getSessionByCmcTypeAndState(7, state) == null && this.mModule.getCmcServiceHelper().getSessionByCmcTypeAndState(5, state) == null)) {
                    HashMap hashMap = new HashMap();
                    hashMap.put("Pull-State", "disabled");
                    i = this.mVolteSvcIntf.makeCall(this.mRegistration.getHandle(), callSetupData, hashMap, this.mSession.getPhoneId());
                }
            }
            i = this.mVolteSvcIntf.makeCall(this.mRegistration.getHandle(), callSetupData, this.mSession.getCallProfile().getAdditionalSipHeaders(), this.mSession.getPhoneId());
        } else {
            i = -1;
        }
        Log.i(this.LOG_TAG, "[ReadyToCall] makeCall() returned session id " + i + ", p2p = " + this.mSession.getCallProfile().getP2p() + ", callSetupError = " + callSetupData.getCallSetupError());
        if (i < 0) {
            if (!this.mMno.isKor() || callSetupData.getCallSetupError() != 503) {
                this.mCsm.sendMessage(4, 0, -1, new SipError(1002, "stack return -1"));
            } else {
                this.mCsm.sendMessage(4, 0, -1, new SipError(NSDSNamespaces.NSDSDefinedResponseCode.SVC_NOT_PROVISIONED_ERROR_CODE, "call blocked by retry after"));
            }
            return false;
        }
        int cmcBoundSessionId = this.mSession.getCallProfile().getCmcBoundSessionId();
        if (cmcBoundSessionId > 0 && (session = this.mModule.getSession(cmcBoundSessionId)) != null) {
            session.getCallProfile().setCmcBoundSessionId(i);
            Log.i(this.LOG_TAG, "[ReadyToCall] updated boundSessionId : " + session.getCallProfile().getCmcBoundSessionId());
        }
        this.mSession.setSessionId(i);
        this.mSession.getCallProfile().setDirection(0);
        return true;
    }

    private boolean isATTSoftPhoneCall() {
        ImsRegistration imsRegistration = this.mRegistration;
        return (imsRegistration == null || imsRegistration.getImsProfile() == null || !this.mRegistration.getImsProfile().isSoftphoneEnabled()) ? false : true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x007c, code lost:
        if (isCsfbRequired(r6.mSession.getPhoneId()) != false) goto L_0x0065;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean handleNotREGStatus() {
        /*
            r6 = this;
            com.sec.ims.ImsRegistration r0 = r6.mRegistration
            r1 = 211(0xd3, float:2.96E-43)
            if (r0 == 0) goto L_0x0032
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r0 = r6.mModule
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r2 = r6.mSession
            int r2 = r2.getPhoneId()
            boolean r0 = r0.isProhibited(r2)
            if (r0 != 0) goto L_0x0032
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r0 = r6.mModule
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r2 = r6.mSession
            int r2 = r2.getPhoneId()
            boolean r0 = r0.getIsLteRetrying(r2)
            if (r0 == 0) goto L_0x0023
            goto L_0x0032
        L_0x0023:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r6.mCsm
            boolean r0 = r0.hasMessages(r1)
            if (r0 == 0) goto L_0x0030
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r6 = r6.mCsm
            r6.removeMessages(r1)
        L_0x0030:
            r6 = 1
            return r6
        L_0x0032:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r6.mCsm
            boolean r0 = r0.getPendingCall()
            r2 = 0
            if (r0 == 0) goto L_0x0110
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r0 = r6.mModule
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r3 = r6.mSession
            int r3 = r3.getPhoneId()
            boolean r0 = r0.getIsLteRetrying(r3)
            r3 = 10000(0x2710, float:1.4013E-41)
            if (r0 == 0) goto L_0x0053
            java.lang.String r0 = r6.LOG_TAG
            java.lang.String r4 = "[ReadyToCall] handleNotREGStatus Lte Retrying"
            android.util.Log.i(r0, r4)
            goto L_0x007f
        L_0x0053:
            com.sec.internal.constants.Mno r0 = r6.mMno
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.VZW
            if (r0 != r4) goto L_0x006c
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r6.mSession
            int r0 = r0.getPhoneId()
            boolean r0 = r6.isCsfbRequired(r0)
            if (r0 == 0) goto L_0x0067
        L_0x0065:
            r3 = r2
            goto L_0x007f
        L_0x0067:
            int r3 = r6.getTimsTimerVzw()
            goto L_0x007f
        L_0x006c:
            boolean r0 = r0.isKor()
            if (r0 == 0) goto L_0x007f
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r6.mSession
            int r0 = r0.getPhoneId()
            boolean r0 = r6.isCsfbRequired(r0)
            if (r0 == 0) goto L_0x007f
            goto L_0x0065
        L_0x007f:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r6.mSession
            com.sec.ims.volte2.data.CallProfile r0 = r0.getCallProfile()
            int r0 = r0.getCallType()
            boolean r0 = com.sec.internal.helper.ImsCallUtil.isE911Call(r0)
            if (r0 == 0) goto L_0x00ee
            com.sec.internal.interfaces.ims.core.IRegistrationManager r0 = r6.mRegistrationManager
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r1 = r6.mSession
            int r1 = r1.getPhoneId()
            com.sec.ims.settings.ImsProfile$PROFILE_TYPE r3 = com.sec.ims.settings.ImsProfile.PROFILE_TYPE.EMERGENCY
            com.sec.ims.settings.ImsProfile r0 = r0.getImsProfile(r1, r3)
            if (r0 == 0) goto L_0x00a8
            int r0 = r0.getLte911Fail()
            long r0 = (long) r0
            r3 = 1000(0x3e8, double:4.94E-321)
            long r0 = r0 * r3
            goto L_0x00aa
        L_0x00a8:
            r0 = 10000(0x2710, double:4.9407E-320)
        L_0x00aa:
            java.lang.String r3 = r6.LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "[ReadyToCall] start Tlte or TWlan-911fail"
            r4.append(r5)
            r4.append(r0)
            java.lang.String r5 = " millis."
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r3, r4)
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r4 = r6.mSession
            int r4 = r4.getPhoneId()
            r3.append(r4)
            java.lang.String r4 = ","
            r3.append(r4)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            r4 = 805306398(0x3000001e, float:4.6566295E-10)
            com.sec.internal.log.IMSLog.c(r4, r3)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r6 = r6.mCsm
            r3 = 303(0x12f, float:4.25E-43)
            r6.sendMessageDelayed((int) r3, (long) r0)
            goto L_0x016e
        L_0x00ee:
            java.lang.String r0 = r6.LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "IMS is not registered. Wait to "
            r4.append(r5)
            r4.append(r3)
            java.lang.String r5 = "msec"
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r0, r4)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r6 = r6.mCsm
            long r3 = (long) r3
            r6.sendMessageDelayed((int) r1, (long) r3)
            goto L_0x016e
        L_0x0110:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r6.mSession
            com.sec.ims.volte2.data.CallProfile r0 = r0.getCallProfile()
            boolean r0 = r0.isForceCSFB()
            r1 = -1
            r3 = 4
            if (r0 == 0) goto L_0x0135
            java.lang.String r0 = r6.LOG_TAG
            java.lang.String r4 = "start: Volte not registered. ForceCSFB"
            android.util.Log.e(r0, r4)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r6 = r6.mCsm
            com.sec.ims.util.SipError r0 = new com.sec.ims.util.SipError
            r4 = 6010(0x177a, float:8.422E-42)
            java.lang.String r5 = "VOLTE_NOT_REGISTERED"
            r0.<init>(r4, r5)
            r6.sendMessage(r3, r2, r1, r0)
            goto L_0x016e
        L_0x0135:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r6.mSession
            int r0 = r0.getCmcType()
            boolean r0 = com.sec.internal.helper.ImsCallUtil.isCmcSecondaryType(r0)
            if (r0 == 0) goto L_0x0158
            java.lang.String r0 = r6.LOG_TAG
            java.lang.String r4 = "start: SD registration is not available."
            android.util.Log.e(r0, r4)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r6 = r6.mCsm
            com.sec.ims.util.SipError r0 = new com.sec.ims.util.SipError
            r4 = 404(0x194, float:5.66E-43)
            java.lang.String r5 = "SD_NOT_REGISTERED"
            r0.<init>(r4, r5)
            r6.sendMessage(r3, r2, r1, r0)
            goto L_0x016e
        L_0x0158:
            java.lang.String r0 = r6.LOG_TAG
            java.lang.String r4 = "start: registration is not available."
            android.util.Log.e(r0, r4)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r6 = r6.mCsm
            com.sec.ims.util.SipError r0 = new com.sec.ims.util.SipError
            r4 = 1003(0x3eb, float:1.406E-42)
            java.lang.String r5 = "No registration."
            r0.<init>(r4, r5)
            r6.sendMessage(r3, r2, r1, r0)
        L_0x016e:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsReadyToCall.handleNotREGStatus():boolean");
    }

    private boolean isCsfbRequired(int i) {
        ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(i, ImsProfile.PROFILE_TYPE.VOLTE);
        if (imsProfile == null) {
            Log.e(this.LOG_TAG, "isCsfbRequired : profile null");
            return true;
        }
        IRegistrationGovernor registrationGovernorByProfileId = this.mRegistrationManager.getRegistrationGovernorByProfileId(imsProfile.getId(), i);
        if (registrationGovernorByProfileId == null) {
            Log.e(this.LOG_TAG, "isCsfbRequired : RegiGov null");
            return true;
        }
        IPdnController pdnController = ImsRegistry.getPdnController();
        if (pdnController == null) {
            Log.e(this.LOG_TAG, "isCsfbRequired : PdnController null");
            return true;
        }
        VoPsIndication vopsIndication = pdnController.getVopsIndication(i);
        IRegistrationGovernor.ThrottleState throttleState = registrationGovernorByProfileId.getThrottleState();
        boolean isSelfActivationRequired = this.mRegistrationManager.isSelfActivationRequired(i);
        int lastRegiErrorCode = this.mModule.getLastRegiErrorCode(i);
        String str = this.LOG_TAG;
        Log.e(str, "isCsfbRequired : VoPS[" + vopsIndication + "], ThrottleState[" + throttleState + "], PCO 5 [" + isSelfActivationRequired + "], Last error [" + lastRegiErrorCode + "]");
        if (vopsIndication == VoPsIndication.SUPPORTED && throttleState != IRegistrationGovernor.ThrottleState.PERMANENTLY_STOPPED && (isSelfActivationRequired || lastRegiErrorCode == SipErrorBase.FORBIDDEN.getCode() || lastRegiErrorCode == SipErrorBase.NOT_FOUND.getCode())) {
            Log.e(this.LOG_TAG, "isCsfbRequired : Wait for registration");
            return false;
        } else if (!this.mMno.isKor() || throttleState == IRegistrationGovernor.ThrottleState.PERMANENTLY_STOPPED) {
            Log.e(this.LOG_TAG, "isCsfbRequired : CSFB");
            return true;
        } else {
            Log.e(this.LOG_TAG, "isCsfbRequired : Wait for registration");
            return false;
        }
    }

    private int getTimsTimerVzw() {
        return DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_VZW_TIMS_TIMER, 120, this.mSession.getPhoneId()).intValue() * 1000;
    }

    private boolean handleCallBarring() {
        int i;
        int phoneId = this.mSession.getPhoneId();
        if (!this.mModule.isCallBarringByNetwork(phoneId)) {
            Log.i(this.LOG_TAG, "checkRejectOutgoingCall: Call barring");
            if (this.mCsm.callType == 2) {
                i = UserConfiguration.getUserConfig(this.mContext, phoneId, "ss_video_cb_pref", 0);
            } else {
                i = UserConfiguration.getUserConfig(this.mContext, phoneId, "ss_volte_cb_pref", 0);
            }
            if ((i & 1) == 1) {
                Log.i(this.LOG_TAG, "checkRejectOutgoingCall: Outgoing call is barried");
                this.mCsm.sendMessage(4, 0, -1, new SipError(405, "Call is Barred by terminal"));
                return false;
            }
        }
        return true;
    }

    private boolean isMatchPidfRatForEmergencyCall(CallProfile callProfile, ImsProfile imsProfile) {
        if (ImsConstants.EmergencyRat.IWLAN.equalsIgnoreCase(callProfile.getEmergencyRat()) && (imsProfile.getNeedPidfRat() & 1) == 1) {
            return true;
        }
        if ((ImsConstants.EmergencyRat.LTE.equalsIgnoreCase(callProfile.getEmergencyRat()) || ImsConstants.EmergencyRat.NR.equalsIgnoreCase(callProfile.getEmergencyRat())) && (imsProfile.getNeedPidfRat() & 2) == 2) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isRequiredLocationE911(CallProfile callProfile) {
        if (!ImsCallUtil.isE911Call(callProfile.getCallType())) {
            Log.i(this.LOG_TAG, "It's not emergency call");
            return false;
        }
        ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(callProfile.getPhoneId(), ImsProfile.PROFILE_TYPE.EMERGENCY);
        if (imsProfile == null) {
            Log.i(this.LOG_TAG, "profile is null");
            return false;
        } else if (imsProfile.getSupportedGeolocationPhase() < 2) {
            Log.i(this.LOG_TAG, "supported geolocation phase check fail");
            return false;
        } else {
            int requestLocationTiming = imsProfile.getRequestLocationTiming();
            String str = this.LOG_TAG;
            Log.i(str, "requestLocationTiming,=" + requestLocationTiming);
            if ((requestLocationTiming & 1) != 1) {
                String str2 = this.LOG_TAG;
                Log.i(str2, "requestLocationTiming,mismatch=" + requestLocationTiming);
                return false;
            } else if (isMatchPidfRatForEmergencyCall(callProfile, imsProfile)) {
                return true;
            } else {
                Log.i(this.LOG_TAG, "isMatchPidfRatForEmergencyCall fail");
                return false;
            }
        }
    }

    public void exit() {
        this.mCsm.removeMessages(208);
        this.mCsm.setPreviousState(this);
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.isLocationAcquiringTriggered = false;
        callStateMachine.mPreAlerting = false;
    }
}
