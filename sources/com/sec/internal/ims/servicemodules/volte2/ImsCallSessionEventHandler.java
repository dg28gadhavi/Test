package com.sec.internal.ims.servicemodules.volte2;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.os.EmcBsIndication;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.core.imsdc.IdcImsCallSessionData;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.volte2.data.ReferStatus;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;

public class ImsCallSessionEventHandler {
    private static final int DISH_POOR_VIDEO_TIMEOUT = 10000;
    private static final int TMO_POOR_VIDEO_TIMEOUT = 20000;
    /* access modifiers changed from: private */
    public String LOG_TAG = "ImsCallSessionEventHandler";
    private PreciseAlarmManager mAm;
    /* access modifiers changed from: private */
    public CallProfile mCallProfile;
    private IImsMediaController mMediaController;
    /* access modifiers changed from: private */
    public Mno mMno;
    /* access modifiers changed from: private */
    public IVolteServiceModuleInternal mModule = null;
    private Message mPoorVideoTimeoutMessage;
    /* access modifiers changed from: private */
    public ImsRegistration mRegistration = null;
    /* access modifiers changed from: private */
    public IRegistrationManager mRegistrationManager = null;
    /* access modifiers changed from: private */
    public ImsCallSession mSession = null;
    /* access modifiers changed from: private */
    public IVolteServiceInterface mVolteSvcIntf;
    /* access modifiers changed from: private */
    public CallStateMachine smCallStateMachine;

    public ImsCallSessionEventHandler(ImsCallSession imsCallSession, IVolteServiceModuleInternal iVolteServiceModuleInternal, ImsRegistration imsRegistration, IRegistrationManager iRegistrationManager, Mno mno, PreciseAlarmManager preciseAlarmManager, CallStateMachine callStateMachine, CallProfile callProfile, IVolteServiceInterface iVolteServiceInterface, IImsMediaController iImsMediaController) {
        char c = Mno.MVNO_DELIMITER;
        this.mPoorVideoTimeoutMessage = null;
        this.mSession = imsCallSession;
        this.mModule = iVolteServiceModuleInternal;
        this.mRegistration = imsRegistration;
        this.mRegistrationManager = iRegistrationManager;
        this.mMno = mno;
        this.mAm = preciseAlarmManager;
        this.smCallStateMachine = callStateMachine;
        this.mCallProfile = callProfile;
        this.mVolteSvcIntf = iVolteServiceInterface;
        this.mMediaController = iImsMediaController;
    }

    /* access modifiers changed from: protected */
    public void onImsCallEventHandler(CallStateEvent callStateEvent) {
        if (callStateEvent.getSessionID() == this.mSession.getSessionId()) {
            String str = this.LOG_TAG;
            Log.i(str, "onImsCallEventHandler, " + callStateEvent);
            ImsCallEventHandler imsCallEventHandler = new ImsCallEventHandler(callStateEvent);
            switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[callStateEvent.getState().ordinal()]) {
                case 1:
                    imsCallEventHandler.handleRingingBack();
                    return;
                case 2:
                    imsCallEventHandler.handleCalling();
                    return;
                case 3:
                    this.smCallStateMachine.sendMessage(31);
                    return;
                case 4:
                    imsCallEventHandler.handleEstablished();
                    return;
                case 5:
                    imsCallEventHandler.handleRefreshFail();
                    return;
                case 6:
                    if (!this.mMno.isChn() || this.mSession.getCallState() != CallConstants.STATE.IncomingCall) {
                        this.smCallStateMachine.mConfCallAdded = true;
                        return;
                    }
                    return;
                case 7:
                    imsCallEventHandler.handleModified();
                    return;
                case 8:
                    imsCallEventHandler.handleHeldLocal();
                    return;
                case 9:
                    imsCallEventHandler.handleHeldRemote();
                    return;
                case 10:
                    imsCallEventHandler.handleHeldBoth();
                    return;
                case 11:
                    imsCallEventHandler.handleEnded();
                    return;
                case 12:
                    imsCallEventHandler.handleModifyRequested();
                    return;
                case 13:
                    imsCallEventHandler.handleEarlyMediaStart();
                    return;
                case 14:
                    imsCallEventHandler.handleError();
                    return;
                case 15:
                    this.mSession.updateCallProfile(callStateEvent.getParams());
                    this.smCallStateMachine.sendMessage(35);
                    return;
                case 16:
                    this.mSession.updateCallProfile(callStateEvent.getParams());
                    this.smCallStateMachine.sendMessage(36);
                    return;
                case 17:
                    imsCallEventHandler.handleExtendToConference();
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onReferStatus(AsyncResult asyncResult) {
        ReferStatus referStatus = (ReferStatus) asyncResult.result;
        if (((ImsCallSession) asyncResult.userObj).mSessionId == referStatus.mSessionId) {
            String str = this.LOG_TAG;
            Log.i(str, "onReferStatus: respCode=" + referStatus.mRespCode);
            int i = referStatus.mRespCode;
            if (i >= 200) {
                this.smCallStateMachine.sendMessage(75, i, -1);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onCurrentLocationDiscoveryDuringEmergencyCall(int i, AsyncResult asyncResult) {
        int intValue = ((Integer) asyncResult.result).intValue();
        if (i != intValue) {
            String str = this.LOG_TAG;
            Log.i(str, "onCurrentLocationDiscoveryDuringEmergencyCall : session is different. sessionId=" + i + ", infoSessionId=" + intValue);
            return;
        }
        this.smCallStateMachine.removeMessages(13);
        if (this.smCallStateMachine.getState() == CallConstants.STATE.InCall) {
            this.smCallStateMachine.sendMessage(13);
        }
    }

    /* access modifiers changed from: protected */
    public void onImsMediaEvent(IMSMediaEvent iMSMediaEvent) {
        int callType = this.mCallProfile.getCallType();
        if (iMSMediaEvent.getSessionID() == this.mSession.getSessionId() || (this.mMno == Mno.SKT && callType == 6)) {
            String str = this.LOG_TAG;
            Log.i(str, "onImsMediaEvent: " + iMSMediaEvent.getState() + " phoneId: " + iMSMediaEvent.getPhoneId());
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[iMSMediaEvent.getState().ordinal()];
            if (i == 1) {
                onVideoHeld();
            } else if (i != 2) {
                switch (i) {
                    case 5:
                        onVideoRtpTimeout(true);
                        break;
                    case 6:
                        onVideoRtpTimeout(false);
                        break;
                    case 7:
                        onVideoQuality(false);
                        break;
                    case 8:
                    case 9:
                    case 10:
                        onVideoQuality(true);
                        break;
                    case 11:
                        this.smCallStateMachine.sendMessage(84);
                        break;
                    case 12:
                        this.smCallStateMachine.sendMessage(85);
                        break;
                    case 13:
                        this.smCallStateMachine.sendMessage(207);
                        break;
                    case 14:
                        this.smCallStateMachine.sendMessage(700, 1);
                        break;
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                        this.smCallStateMachine.sendMessage(700, 0);
                        break;
                    case 19:
                        if (this.mCallProfile.getRecordState() == 1) {
                            this.smCallStateMachine.sendMessage(700, 0);
                            this.mMediaController.stopRecord();
                            break;
                        }
                        break;
                }
            } else {
                onVideoResumed();
            }
            if (iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.VIDEO_HELD || iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.VIDEO_RESUMED || iMSMediaEvent.getState() == IMSMediaEvent.MEDIA_STATE.VIDEO_AVAILABLE) {
                this.mModule.notifyImsCallEventForVideo(this.mSession, iMSMediaEvent);
            }
        }
    }

    private void onVideoHeld() {
        this.mCallProfile.getMediaProfile().setVideoPause(true);
        this.smCallStateMachine.sendMessage(82);
        if (this.mMno.isOneOf(Mno.TMOUS, Mno.DISH)) {
            stopPoorVideoTimer();
        }
    }

    private class ImsCallEventHandler {
        final CallStateEvent mEvent;

        private ImsCallEventHandler(CallStateEvent callStateEvent) {
            this.mEvent = callStateEvent;
        }

        /* access modifiers changed from: private */
        public void handleRingingBack() {
            ImsCallSessionEventHandler.this.mSession.updateCallProfile(this.mEvent.getParams());
            if ((ImsCallSessionEventHandler.this.mSession.getVideoCrbtSupportType() & 1) == 1) {
                if (this.mEvent.getParams().getVideoCrbtType() == 0) {
                    ImsCallSessionEventHandler.this.mCallProfile.setVideoCRBT(false);
                    ImsCallSessionEventHandler.this.mCallProfile.setDtmfEvent("");
                }
                String r0 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(r0, "isVideoCRBT : " + ImsCallSessionEventHandler.this.mCallProfile.isVideoCRBT());
            }
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(34);
        }

        /* access modifiers changed from: private */
        public void handleCalling() {
            ImsCallSession sessionBySipCallId;
            ImsCallSessionEventHandler.this.mSession.updateCallProfile(this.mEvent.getParams());
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(33);
            if (ImsCallSessionEventHandler.this.mCallProfile.getReplaceSipCallId() != null && (sessionBySipCallId = ImsCallSessionEventHandler.this.mModule.getSessionBySipCallId(ImsCallSessionEventHandler.this.mCallProfile.getReplaceSipCallId())) != null) {
                String r1 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(r1, "replace UserAgent. replaceSessionId " + sessionBySipCallId.getSessionId() + " newSessionId " + ImsCallSessionEventHandler.this.mSession.mSessionId);
                ImsCallSessionEventHandler.this.mVolteSvcIntf.replaceUserAgent(sessionBySipCallId.getSessionId(), ImsCallSessionEventHandler.this.mSession.mSessionId);
            }
        }

        /* access modifiers changed from: private */
        public void handleEstablished() {
            String audioCodecType = ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().getAudioCodec().toString();
            boolean z = this.mEvent.getParams().getAudioRxTrackId() != ImsCallSessionEventHandler.this.mCallProfile.getAudioRxTrackId();
            ImsCallSessionEventHandler.this.mSession.updateCallProfile(this.mEvent.getParams());
            if (ImsCallSessionEventHandler.this.mRegistration != null && ImsCallSessionEventHandler.this.mRegistration.getImsProfile() != null && ImsCallSessionEventHandler.this.mRegistration.getImsProfile().getNotifyCodecOnEstablished() && !audioCodecType.equals(ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().getAudioCodec().toString()) && ImsCallSessionEventHandler.this.mSession.getCallState() == CallConstants.STATE.InCall) {
                String r0 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(r0, "forceNotifyCurrentCodec, Codec =" + this.mEvent.getParams().getAudioCodec() + ", HdIcon: " + ImsCallSessionEventHandler.this.mCallProfile.getHDIcon());
                ImsCallSessionEventHandler.this.mSession.forceNotifyCurrentCodec();
            }
            if (z && ImsCallSessionEventHandler.this.mSession.getCallState() == CallConstants.STATE.InCall) {
                ImsCallSessionEventHandler.this.mSession.forceNotifyCurrentCodec();
                Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "notified audiorxtrackid");
            }
            if (ImsCallUtil.isVideoCall(ImsCallSessionEventHandler.this.mCallProfile.getCallType()) && !ImsCallUtil.isVideoCall(this.mEvent.getCallType())) {
                ImsCallSessionEventHandler.this.mCallProfile.setDowngradedVideoCall(true);
                ImsCallSessionEventHandler.this.mCallProfile.setDowngradedAtEstablish(true);
                ImsCallSessionEventHandler.this.mSession.setUserCameraOff(false);
                int notifyCallDowngraded = (ImsCallSessionEventHandler.this.mRegistration == null || ImsCallSessionEventHandler.this.mRegistration.getImsProfile() == null) ? 0 : ImsCallSessionEventHandler.this.mRegistration.getImsProfile().getNotifyCallDowngraded();
                if ((ImsCallSessionEventHandler.this.mMno.isChn() || notifyCallDowngraded == 1 || ((ImsCallSessionEventHandler.this.mMno.isOneOf(Mno.TMOUS, Mno.DISH) || notifyCallDowngraded == 2) && !this.mEvent.getRemoteVideoCapa())) && ImsCallSessionEventHandler.this.mCallProfile.isMOCall()) {
                    ImsCallSessionEventHandler.this.mSession.notifyCallDowngraded();
                }
            }
            if (ImsCallSessionEventHandler.this.mRegistration == null || !ImsCallSessionEventHandler.this.mRegistration.getImsProfile().isSoftphoneEnabled() || ImsCallSessionEventHandler.this.mCallProfile.getCallType() != 13) {
                ImsCallSessionEventHandler.this.mCallProfile.setCallType(this.mEvent.getCallType());
                if (ImsCallUtil.isRttCall(this.mEvent.getCallType())) {
                    ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().setRttMode(1);
                } else {
                    ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().setRttMode(0);
                }
            } else {
                String r02 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(r02, "ATT Softphone : not change FROM  callType = " + ImsCallSessionEventHandler.this.mCallProfile.getCallType() + "TO  calltype =" + this.mEvent.getCallType());
            }
            if (ImsCallUtil.isCmcPrimaryType(ImsCallSessionEventHandler.this.mSession.getCmcType()) && !TextUtils.isEmpty(this.mEvent.getCmcDeviceId())) {
                ImsCallSessionEventHandler.this.mCallProfile.setCmcDeviceId(this.mEvent.getCmcDeviceId());
            }
            if (ImsCallUtil.isCmcSecondaryType(ImsCallSessionEventHandler.this.mSession.getCmcType()) && !TextUtils.isEmpty(this.mEvent.getCmcCallTime())) {
                ImsCallSessionEventHandler.this.mCallProfile.setCmcCallTime(this.mEvent.getCmcCallTime());
            }
            ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(this.mEvent.getRemoteVideoCapa());
            ImsCallSessionEventHandler.this.smCallStateMachine.setVideoRtpPort(this.mEvent.getParams().getLocalVideoRTPPort(), this.mEvent.getParams().getLocalVideoRTCPPort(), this.mEvent.getParams().getRemoteVideoRTPPort(), this.mEvent.getParams().getRemoteVideoRTCPPort());
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(41, this.mEvent.getParams().getIndicationFlag());
        }

        /* access modifiers changed from: private */
        public void handleRefreshFail() {
            IRegistrationGovernor registrationGovernor;
            SipError errorCode = this.mEvent.getErrorCode();
            String r1 = ImsCallSessionEventHandler.this.LOG_TAG;
            Log.i(r1, "REFRESHFAIL " + errorCode.toString());
            if (ImsCallSessionEventHandler.this.mRegistration != null && (registrationGovernor = ImsCallSessionEventHandler.this.mRegistrationManager.getRegistrationGovernor(ImsCallSessionEventHandler.this.mRegistration.getHandle())) != null) {
                registrationGovernor.onSipError("mmtel", errorCode);
            }
        }

        /* access modifiers changed from: private */
        public void handleModified() {
            if (ImsCallSessionEventHandler.this.mSession.getIdcData() == null || !(ImsCallSessionEventHandler.this.mSession.getIdcData().getCurrentState() == IdcImsCallSessionData.State.MODIFYING || ImsCallSessionEventHandler.this.mSession.getIdcData().getCurrentState() == IdcImsCallSessionData.State.MODIFY_REQUESTED)) {
                ImsCallSessionEventHandler.this.mSession.updateCallProfile(this.mEvent.getParams());
                if (ImsCallSessionEventHandler.this.mSession.mModifyRequestedProfile == null) {
                    Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "unexpected ImsCallEvent");
                } else if (this.mEvent.getErrorCode() == null || this.mEvent.getErrorCode().equals(SipErrorBase.OK)) {
                    String r0 = ImsCallSessionEventHandler.this.LOG_TAG;
                    Log.i(r0, "Change calltype from " + this.mEvent.getCallType() + " to " + ImsCallSessionEventHandler.this.mSession.mModifyRequestedProfile.getCallType());
                    this.mEvent.setCallType(ImsCallSessionEventHandler.this.mSession.mModifyRequestedProfile.getCallType());
                }
                ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(this.mEvent.getRemoteVideoCapa());
                if (ImsCallUtil.isVideoCall(ImsCallSessionEventHandler.this.mCallProfile.getCallType()) && !ImsCallUtil.isVideoCall(this.mEvent.getCallType())) {
                    Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "Call is downgrade");
                    ImsCallSessionEventHandler.this.mSession.stopCamera();
                    ImsCallSessionEventHandler.this.mCallProfile.setDowngradedVideoCall(true);
                    ImsCallSessionEventHandler.this.mSession.setUserCameraOff(false);
                } else if (!ImsCallUtil.isVideoCall(ImsCallSessionEventHandler.this.mCallProfile.getCallType()) && ImsCallUtil.isVideoCall(this.mEvent.getCallType())) {
                    Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "Call is upgrade");
                    ImsCallSessionEventHandler.this.mCallProfile.setDowngradedVideoCall(false);
                }
                ImsCallSessionEventHandler.this.mCallProfile.setDowngradedAtEstablish(false);
                int callType = ImsCallSessionEventHandler.this.mCallProfile.getCallType();
                ImsCallSessionEventHandler.this.mCallProfile.setCallType(this.mEvent.getCallType());
                ImsCallSessionEventHandler.this.smCallStateMachine.setVideoRtpPort(this.mEvent.getParams().getLocalVideoRTPPort(), this.mEvent.getParams().getLocalVideoRTCPPort(), this.mEvent.getParams().getRemoteVideoRTPPort(), this.mEvent.getParams().getRemoteVideoRTCPPort());
                ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(91, this.mEvent.getCallType(), callType);
                return;
            }
            Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "[IDC] Transaction Handling");
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(153, (Object) this.mEvent.getIdcExtra());
        }

        /* access modifiers changed from: private */
        public void handleHeldLocal() {
            String audioCodecType = ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().getAudioCodec().toString();
            ImsCallSessionEventHandler.this.mSession.updateCallProfile(this.mEvent.getParams());
            if (ImsCallSessionEventHandler.this.mMno.isChn() || ImsCallSessionEventHandler.this.mMno.isHkMo() || ImsCallSessionEventHandler.this.mMno.isKor() || ImsCallSessionEventHandler.this.mMno.isJpn() || ImsCallSessionEventHandler.this.mMno == Mno.RJIL || ImsCallSessionEventHandler.this.mMno == Mno.TELSTRA) {
                ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(this.mEvent.getRemoteVideoCapa());
            }
            if ((ImsCallSessionEventHandler.this.mMno == Mno.DOCOMO || ImsCallSessionEventHandler.this.mMno == Mno.TWM) && !audioCodecType.equals(ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().getAudioCodec().toString())) {
                String r0 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(r0, "forceNotifyCurrentCodec, Codec =" + this.mEvent.getParams().getAudioCodec() + ", HdIcon: " + ImsCallSessionEventHandler.this.mCallProfile.getHDIcon());
                ImsCallSessionEventHandler.this.mSession.forceNotifyCurrentCodec();
            }
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(61);
        }

        /* access modifiers changed from: private */
        public void handleHeldRemote() {
            if (ImsCallSessionEventHandler.this.mMno.isChn() || ImsCallSessionEventHandler.this.mMno.isHkMo() || ImsCallSessionEventHandler.this.mMno.isKor() || ImsCallSessionEventHandler.this.mMno.isJpn() || ImsCallSessionEventHandler.this.mMno == Mno.ATT || ImsCallSessionEventHandler.this.mMno == Mno.RJIL || ImsCallSessionEventHandler.this.mMno == Mno.TELSTRA) {
                ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(this.mEvent.getRemoteVideoCapa());
            }
            if (ImsCallSessionEventHandler.this.mMno == Mno.MOVISTAR_PERU || ImsCallSessionEventHandler.this.mMno == Mno.TWM) {
                String audioCodecType = ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().getAudioCodec().toString();
                ImsCallSessionEventHandler.this.mSession.updateCallProfile(this.mEvent.getParams());
                if (!audioCodecType.equals(ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().getAudioCodec().toString())) {
                    String r0 = ImsCallSessionEventHandler.this.LOG_TAG;
                    Log.i(r0, "forceNotifyCurrentCodec, Codec =" + this.mEvent.getParams().getAudioCodec() + ", HdIcon: " + ImsCallSessionEventHandler.this.mCallProfile.getHDIcon());
                    ImsCallSessionEventHandler.this.mSession.forceNotifyCurrentCodec();
                }
            }
            ImsCallSessionEventHandler.this.mSession.mOldLocalHoldTone = ImsCallSessionEventHandler.this.mSession.mLocalHoldTone;
            ImsCallSessionEventHandler.this.mSession.mLocalHoldTone = this.mEvent.getParams().getLocalHoldTone();
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(62);
        }

        /* access modifiers changed from: private */
        public void handleHeldBoth() {
            if (ImsCallSessionEventHandler.this.mMno.isChn() || ImsCallSessionEventHandler.this.mMno.isHkMo() || ImsCallSessionEventHandler.this.mMno.isKor() || ImsCallSessionEventHandler.this.mMno.isJpn() || ImsCallSessionEventHandler.this.mMno == Mno.RJIL || ImsCallSessionEventHandler.this.mMno == Mno.TELSTRA) {
                ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(this.mEvent.getRemoteVideoCapa());
            }
            ImsCallSessionEventHandler.this.mSession.mOldLocalHoldTone = ImsCallSessionEventHandler.this.mSession.mLocalHoldTone;
            ImsCallSessionEventHandler.this.mSession.mLocalHoldTone = this.mEvent.getParams().getLocalHoldTone();
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(63);
        }

        /* access modifiers changed from: private */
        public void handleEnded() {
            int videoCrbtSupportType = ImsCallSessionEventHandler.this.mSession.getVideoCrbtSupportType();
            if ((videoCrbtSupportType & 1) == 1 || (videoCrbtSupportType & 2) == 2) {
                ImsCallSessionEventHandler.this.mCallProfile.setVideoCRBT(false);
                ImsCallSessionEventHandler.this.mCallProfile.setDtmfEvent("");
                String r0 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(r0, "isVideoCRBT : " + ImsCallSessionEventHandler.this.mCallProfile.isVideoCRBT());
            }
            SipError errorCode = this.mEvent.getErrorCode();
            if (errorCode == null) {
                ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(3);
                return;
            }
            if (!ImsCallUtil.isCmcSecondaryType(ImsCallSessionEventHandler.this.mSession.getCmcType()) || !"MDMN_PULL_BY_PRIMARY".equals(errorCode.getReason())) {
                ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(3, errorCode.getCode(), -1, errorCode.getReason());
            } else {
                ImsCallSessionEventHandler.this.mCallProfile.setCmcDeviceId(ImsRegistry.getCmcAccountManager().getCurrentLineOwnerDeviceId());
                ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(3, errorCode.getCode(), 6007, errorCode.getReason());
            }
            if (ImsCallUtil.isCmcSecondaryType(ImsCallSessionEventHandler.this.mSession.getCmcType()) && "MDMN_PULL_BY_SECONDARY".equals(errorCode.getReason())) {
                ImsCallSessionEventHandler.this.mCallProfile.setCmcDeviceId(this.mEvent.getCmcDeviceId());
            }
        }

        /* access modifiers changed from: private */
        public void handleModifyRequested() {
            int callType = this.mEvent.getCallType();
            boolean isSdToSdPull = this.mEvent.getIsSdToSdPull();
            if (!this.mEvent.getIdcExtra().isEmpty()) {
                Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "[IDC] Transaction Handling");
                ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(154, (Object) this.mEvent.getIdcExtra());
            } else if (!ImsCallSessionEventHandler.this.mCallProfile.hasRemoteVideoCapa() && ImsCallSessionEventHandler.this.mModule.isCallServiceAvailable(ImsCallSessionEventHandler.this.mSession.mPhoneId, "mmtel-video") && ImsCallUtil.isVideoCall(callType)) {
                ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(true);
                ImsCallSessionEventHandler.this.mSession.forceNotifyCurrentCodec();
                ImsCallSessionEventHandler.this.smCallStateMachine.sendMessageDelayed(55, callType, 0, (Object) null, 100);
            } else if (!ImsCallUtil.isCmcPrimaryType(ImsCallSessionEventHandler.this.mSession.getCmcType()) || !isSdToSdPull) {
                ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(55, callType, 0, (Object) null);
            } else {
                modifyCallTypeForPull();
            }
        }

        /* access modifiers changed from: private */
        public void handleEarlyMediaStart() {
            boolean z;
            ImsCallSessionEventHandler.this.mSession.updateCallProfile(this.mEvent.getParams());
            String dtmfEvent = this.mEvent.getParams().getDtmfEvent();
            ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(this.mEvent.getRemoteVideoCapa());
            if (this.mEvent.getParams().getVideoCrbtType() > 0) {
                z = true;
                ImsCallSessionEventHandler.this.mCallProfile.setVideoCrbtValid(true);
            } else {
                z = false;
            }
            ImsCallSessionEventHandler.this.mCallProfile.setVideoCRBT(z);
            ImsCallSessionEventHandler.this.mCallProfile.setDtmfEvent(dtmfEvent);
            String r1 = ImsCallSessionEventHandler.this.LOG_TAG;
            Log.i(r1, "isVideoCRBT : " + ImsCallSessionEventHandler.this.mCallProfile.isVideoCRBT() + ", dtmfEvent : " + dtmfEvent);
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(32, this.mEvent.getErrorCode().getCode());
        }

        /* access modifiers changed from: private */
        public void handleError() {
            IRegistrationGovernor.CallEvent callEvent;
            IRegistrationGovernor registrationGovernor;
            if (ImsCallSessionEventHandler.this.mSession.getIdcData() == null || ImsCallSessionEventHandler.this.mSession.getIdcData().getCurrentState() != IdcImsCallSessionData.State.MODIFYING) {
                SipError errorCode = this.mEvent.getErrorCode();
                int retryAfter = this.mEvent.getRetryAfter();
                if (this.mEvent.getAlternativeService() == CallStateEvent.ALTERNATIVE_SERVICE.INITIAL_REGISTRATION) {
                    callEvent = IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_INITIAL_REGI;
                } else if (this.mEvent.getAlternativeService() == CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY_REGISTRATION) {
                    callEvent = IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_EMERGENCY_REGI;
                } else {
                    callEvent = this.mEvent.getAlternativeService() == CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY ? IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_EMERGENCY : null;
                }
                int videoCrbtSupportType = ImsCallSessionEventHandler.this.mSession.getVideoCrbtSupportType();
                if ((videoCrbtSupportType & 1) == 1 || (videoCrbtSupportType & 2) == 2) {
                    ImsCallSessionEventHandler.this.mCallProfile.setVideoCRBT(false);
                    ImsCallSessionEventHandler.this.mCallProfile.setDtmfEvent("");
                    String r3 = ImsCallSessionEventHandler.this.LOG_TAG;
                    Log.i(r3, "isVideoCRBT : " + ImsCallSessionEventHandler.this.mCallProfile.isVideoCRBT());
                }
                if (!(ImsCallSessionEventHandler.this.mRegistration == null || (registrationGovernor = ImsCallSessionEventHandler.this.mRegistrationManager.getRegistrationGovernor(ImsCallSessionEventHandler.this.mRegistration.getHandle())) == null)) {
                    errorCode = callEvent != null ? handleErrorOnCallEvent(errorCode, callEvent, registrationGovernor) : handleErrorOnNullEvent(errorCode, registrationGovernor);
                }
                handleErrorOnNullRegistration(errorCode, callEvent);
                if (ImsCallUtil.isCmcPrimaryType(ImsCallSessionEventHandler.this.mSession.getCmcType()) && this.mEvent.getCmcDeviceId() != null && !this.mEvent.getCmcDeviceId().isEmpty()) {
                    ImsCallSessionEventHandler.this.mCallProfile.setCmcDeviceId(this.mEvent.getCmcDeviceId());
                }
                ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(4, retryAfter, -1, errorCode);
                return;
            }
            Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "[IDC] Transaction Handling");
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(155, (Object) this.mEvent.getIdcExtra());
        }

        private SipError handleErrorOnCallEvent(SipError sipError, IRegistrationGovernor.CallEvent callEvent, IRegistrationGovernor iRegistrationGovernor) {
            if (ImsCallSessionEventHandler.this.mMno == Mno.CMCC) {
                iRegistrationGovernor.onCallStatus(callEvent, sipError, ImsCallSessionEventHandler.this.mCallProfile.getCallType());
                SipError sipError2 = SipErrorBase.ALTERNATIVE_SERVICE;
                if (!sipError2.equals(sipError)) {
                    return sipError;
                }
                String alternativeServiceType = this.mEvent.getAlternativeServiceType();
                String alternativeServiceReason = this.mEvent.getAlternativeServiceReason();
                String alternativeServiceUrn = this.mEvent.getAlternativeServiceUrn();
                String r2 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(r2, "handleErrorOnCallEvent: type : " + alternativeServiceType + ", reason : " + alternativeServiceReason + ", serviceUrn : " + alternativeServiceUrn);
                String r6 = ImsCallSessionEventHandler.this.LOG_TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("handleErrorOnCallEvent: phoenId : ");
                sb.append(ImsCallSessionEventHandler.this.mSession.mPhoneId);
                sb.append(", callEvent : ");
                sb.append(callEvent);
                Log.i(r6, sb.toString());
                if (ImsRegistry.getPdnController().getEmcBsIndication(ImsCallSessionEventHandler.this.mSession.mPhoneId) == EmcBsIndication.SUPPORTED && callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_EMERGENCY_REGI) {
                    SipError sipError3 = SipErrorBase.ALTERNATIVE_SERVICE_EMERGENCY;
                    if (TextUtils.isEmpty(alternativeServiceUrn)) {
                        sipError3.setReason(ImsCallUtil.ECC_SERVICE_URN_DEFAULT);
                        return sipError3;
                    }
                    sipError3.setReason(alternativeServiceUrn);
                    return sipError3;
                } else if (TextUtils.isEmpty(alternativeServiceUrn)) {
                    sipError2.setReason("");
                    return sipError2;
                } else if (ImsCallUtil.convertUrnToEccCat(alternativeServiceUrn) == 254) {
                    sipError2.setReason(alternativeServiceUrn);
                    return sipError2;
                } else {
                    SipError sipError4 = SipErrorBase.ALTERNATIVE_SERVICE_EMERGENCY_CSFB;
                    sipError4.setReason(alternativeServiceUrn);
                    return sipError4;
                }
            } else {
                iRegistrationGovernor.onCallStatus(callEvent, sipError, ImsCallSessionEventHandler.this.mCallProfile.getCallType());
                return ImsCallUtil.onConvertSipErrorReason(this.mEvent);
            }
        }

        private SipError handleErrorOnNullEvent(SipError sipError, IRegistrationGovernor iRegistrationGovernor) {
            if (ImsCallSessionEventHandler.this.mMno == Mno.CMCC) {
                return iRegistrationGovernor.onSipError("mmtel", sipError);
            }
            if ((ImsCallSessionEventHandler.this.smCallStateMachine.mReinvite || ImsCallSessionEventHandler.this.smCallStateMachine.mConfCallAdded) && ImsCallSessionEventHandler.this.mMno == Mno.KDDI) {
                Log.e(ImsCallSessionEventHandler.this.LOG_TAG, "Don't send Register for reINVITE's transaction timeout");
                return sipError;
            } else if (ImsCallSessionEventHandler.this.mMno == Mno.USCC && ((ImsCallSessionEventHandler.this.mSession.getCallState() == CallConstants.STATE.AlertingCall || ImsCallSessionEventHandler.this.mSession.getCallState() == CallConstants.STATE.EndingCall) && sipError.getCode() == 408)) {
                Log.e(ImsCallSessionEventHandler.this.LOG_TAG, "USCC - Don't re-REGISTER for 408 if it is received after 180");
                return sipError;
            } else if (ImsCallSessionEventHandler.this.mMno != Mno.SPRINT || ImsCallSessionEventHandler.this.mSession.getCallState() != CallConstants.STATE.ModifyingCall) {
                return iRegistrationGovernor.onSipError("mmtel", sipError);
            } else {
                Log.e(ImsCallSessionEventHandler.this.LOG_TAG, "Don't deregister for Re-Invite failures");
                return sipError;
            }
        }

        private void handleErrorOnNullRegistration(SipError sipError, IRegistrationGovernor.CallEvent callEvent) {
            int retryAfter = this.mEvent.getRetryAfter();
            if (ImsCallSessionEventHandler.this.mMno != Mno.CMCC) {
                if (ImsCallSessionEventHandler.this.mMno == Mno.KDDI && retryAfter > 0) {
                    String r1 = ImsCallSessionEventHandler.this.LOG_TAG;
                    Log.e(r1, "KDDI : INVITE retry should happen after " + retryAfter + " seconds");
                    ImsCallSessionEventHandler.this.smCallStateMachine.setRetryInprogress(true);
                }
                if (callEvent != null && !DeviceUtil.getGcfMode()) {
                    handleErrorSetCodeReason(sipError, callEvent);
                }
            }
        }

        private void handleErrorSetCodeReason(SipError sipError, IRegistrationGovernor.CallEvent callEvent) {
            if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_EMERGENCY_REGI) {
                if (ImsCallSessionEventHandler.this.mMno == Mno.STARHUB || ImsCallSessionEventHandler.this.mMno == Mno.CU) {
                    sipError.setCode(380);
                } else if (ImsCallSessionEventHandler.this.mMno != Mno.SPRINT || !this.mEvent.getAlternativeServiceReason().equals("VoIP emergency not available!")) {
                    sipError.setCode(381);
                } else {
                    sipError.setCode(382);
                }
                if (ImsCallSessionEventHandler.this.mMno == Mno.DOCOMO || ImsCallSessionEventHandler.this.mMno == Mno.KDDI || ImsCallSessionEventHandler.this.mMno.isEur() || ImsCallSessionEventHandler.this.mMno == Mno.MOBILEONE) {
                    Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "need to carry service urn info for e911");
                    sipError.setReason(this.mEvent.getAlternativeServiceUrn());
                } else {
                    sipError.setReason("");
                }
                String r4 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(r4, "convert error " + sipError.getCode() + " " + sipError.getReason());
            } else if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_EMERGENCY) {
                if (ImsCallSessionEventHandler.this.mRegistration != null && ImsCallSessionEventHandler.this.mRegistration.getImsProfile().getEcallCsfbWithoutActionTag() && !TextUtils.isEmpty(this.mEvent.getAlternativeServiceUrn())) {
                    sipError.setCode(381);
                    sipError.setReason(this.mEvent.getAlternativeServiceUrn());
                    String r42 = ImsCallSessionEventHandler.this.LOG_TAG;
                    Log.i(r42, "convert error " + sipError.getCode() + " " + sipError.getReason());
                }
            }
        }

        /* access modifiers changed from: private */
        public void handleExtendToConference() {
            SipError errorCode = this.mEvent.getErrorCode();
            if (ImsCallSessionEventHandler.this.mMno.isKor()) {
                ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(this.mEvent.getRemoteVideoCapa());
            }
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(74, errorCode.getCode(), this.mEvent.getCallType());
        }

        private void modifyCallTypeForPull() {
            Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "modifyCallType for SD to SD pull");
            int callType = ImsCallSessionEventHandler.this.mCallProfile.getCallType();
            int cmcBoundSessionId = ImsCallSessionEventHandler.this.mCallProfile.getCmcBoundSessionId();
            ImsCallSessionEventHandler.this.mVolteSvcIntf.replyModifyCallType(ImsCallSessionEventHandler.this.mSession.getSessionId(), callType, callType, callType, ImsCallSessionEventHandler.this.smCallStateMachine.calculateCmcCallTime(cmcBoundSessionId > 0 ? ImsCallSessionEventHandler.this.mModule.getSession(cmcBoundSessionId) : null, (String) null));
        }
    }

    private void onVideoResumed() {
        this.mCallProfile.getMediaProfile().setVideoPause(false);
        this.smCallStateMachine.sendMessage(83);
    }

    private void onVideoRtpTimeout(boolean z) {
        if (this.mMno.isOneOf(Mno.TMOUS, Mno.DISH, Mno.ATT, Mno.DIGI, Mno.RJIL)) {
            this.smCallStateMachine.sendMessage(206);
        }
        if (this.mMno.isOneOf(Mno.CTC, Mno.CTCMO) && z) {
            this.smCallStateMachine.sendMessage(206);
        }
    }

    private void onVideoQuality(boolean z) {
        if (!this.mMno.isOneOf(Mno.TMOUS, Mno.DISH)) {
            return;
        }
        if (z) {
            stopPoorVideoTimer();
        } else if (this.mMno == Mno.TMOUS) {
            startPoorVideoTimer(20000);
        } else {
            startPoorVideoTimer(10000);
        }
    }

    private void startPoorVideoTimer(long j) {
        String str = this.LOG_TAG;
        Log.i(str, "startPoorVideoTimer: " + j);
        stopPoorVideoTimer();
        this.mPoorVideoTimeoutMessage = this.smCallStateMachine.obtainMessage(205);
        this.mAm.sendMessageDelayed(getClass().getSimpleName(), this.mPoorVideoTimeoutMessage, j);
    }

    private void stopPoorVideoTimer() {
        if (this.mPoorVideoTimeoutMessage != null) {
            Log.i(this.LOG_TAG, "stopPoorVidoeTimer");
            this.mAm.removeMessage(this.mPoorVideoTimeoutMessage);
            this.mPoorVideoTimeoutMessage = null;
        }
    }

    /* access modifiers changed from: protected */
    public void onUssdEvent(UssdEvent ussdEvent) {
        if (ussdEvent.getSessionID() == this.mSession.getSessionId()) {
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$UssdEvent$USSD_STATE[ussdEvent.getState().ordinal()];
            if (i == 1) {
                this.smCallStateMachine.sendMessage(93, (Object) ussdEvent.getErrorCode());
            } else if (i == 2) {
                Bundle bundle = new Bundle();
                bundle.putInt("status", ussdEvent.getStatus());
                bundle.putInt("dcs", ussdEvent.getDCS());
                bundle.putByteArray("data", ussdEvent.getData());
                this.smCallStateMachine.sendMessage(94, (Object) bundle);
            } else if (i == 3) {
                this.smCallStateMachine.sendMessage(4, -1, -1, ussdEvent.getErrorCode());
            }
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.volte2.ImsCallSessionEventHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$UssdEvent$USSD_STATE;

        /* JADX WARNING: Can't wrap try/catch for region: R(78:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|13|14|15|16|(2:17|18)|19|(2:21|22)|23|(2:25|26)|27|(2:29|30)|31|(2:33|34)|35|(2:37|38)|39|(2:41|42)|43|(2:45|46)|47|(2:49|50)|51|(2:53|54)|55|(2:57|58)|59|(2:61|62)|63|(2:65|66)|67|(2:69|70)|71|73|74|75|76|(2:77|78)|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|(3:113|114|116)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(80:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|13|14|15|16|(2:17|18)|19|(2:21|22)|23|(2:25|26)|27|(2:29|30)|31|(2:33|34)|35|37|38|39|(2:41|42)|43|(2:45|46)|47|(2:49|50)|51|(2:53|54)|55|(2:57|58)|59|(2:61|62)|63|(2:65|66)|67|(2:69|70)|71|73|74|75|76|77|78|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|(3:113|114|116)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(81:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|13|14|15|16|(2:17|18)|19|(2:21|22)|23|(2:25|26)|27|(2:29|30)|31|(2:33|34)|35|37|38|39|(2:41|42)|43|(2:45|46)|47|(2:49|50)|51|(2:53|54)|55|57|58|59|(2:61|62)|63|(2:65|66)|67|(2:69|70)|71|73|74|75|76|77|78|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|(3:113|114|116)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(83:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|13|14|15|16|(2:17|18)|19|(2:21|22)|23|(2:25|26)|27|(2:29|30)|31|(2:33|34)|35|37|38|39|(2:41|42)|43|(2:45|46)|47|(2:49|50)|51|(2:53|54)|55|57|58|59|(2:61|62)|63|(2:65|66)|67|(2:69|70)|71|73|74|75|76|77|78|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(84:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|13|14|15|16|(2:17|18)|19|(2:21|22)|23|(2:25|26)|27|(2:29|30)|31|33|34|35|37|38|39|(2:41|42)|43|(2:45|46)|47|(2:49|50)|51|(2:53|54)|55|57|58|59|(2:61|62)|63|(2:65|66)|67|(2:69|70)|71|73|74|75|76|77|78|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(85:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|13|14|15|16|(2:17|18)|19|(2:21|22)|23|(2:25|26)|27|(2:29|30)|31|33|34|35|37|38|39|(2:41|42)|43|(2:45|46)|47|(2:49|50)|51|53|54|55|57|58|59|(2:61|62)|63|(2:65|66)|67|(2:69|70)|71|73|74|75|76|77|78|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(86:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|13|14|15|16|(2:17|18)|19|(2:21|22)|23|(2:25|26)|27|29|30|31|33|34|35|37|38|39|(2:41|42)|43|(2:45|46)|47|(2:49|50)|51|53|54|55|57|58|59|(2:61|62)|63|(2:65|66)|67|(2:69|70)|71|73|74|75|76|77|78|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(88:0|(2:1|2)|3|(2:5|6)|7|9|10|11|13|14|15|16|(2:17|18)|19|(2:21|22)|23|(2:25|26)|27|29|30|31|33|34|35|37|38|39|(2:41|42)|43|(2:45|46)|47|49|50|51|53|54|55|57|58|59|(2:61|62)|63|(2:65|66)|67|(2:69|70)|71|73|74|75|76|77|78|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(89:0|(2:1|2)|3|(2:5|6)|7|9|10|11|13|14|15|16|(2:17|18)|19|(2:21|22)|23|(2:25|26)|27|29|30|31|33|34|35|37|38|39|(2:41|42)|43|(2:45|46)|47|49|50|51|53|54|55|57|58|59|(2:61|62)|63|(2:65|66)|67|69|70|71|73|74|75|76|77|78|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(90:0|(2:1|2)|3|(2:5|6)|7|9|10|11|13|14|15|16|(2:17|18)|19|(2:21|22)|23|25|26|27|29|30|31|33|34|35|37|38|39|(2:41|42)|43|(2:45|46)|47|49|50|51|53|54|55|57|58|59|(2:61|62)|63|(2:65|66)|67|69|70|71|73|74|75|76|77|78|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(92:0|(2:1|2)|3|5|6|7|9|10|11|13|14|15|16|(2:17|18)|19|(2:21|22)|23|25|26|27|29|30|31|33|34|35|37|38|39|(2:41|42)|43|45|46|47|49|50|51|53|54|55|57|58|59|(2:61|62)|63|(2:65|66)|67|69|70|71|73|74|75|76|77|78|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(94:0|(2:1|2)|3|5|6|7|9|10|11|13|14|15|16|(2:17|18)|19|21|22|23|25|26|27|29|30|31|33|34|35|37|38|39|(2:41|42)|43|45|46|47|49|50|51|53|54|55|57|58|59|(2:61|62)|63|65|66|67|69|70|71|73|74|75|76|77|78|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(95:0|1|2|3|5|6|7|9|10|11|13|14|15|16|(2:17|18)|19|21|22|23|25|26|27|29|30|31|33|34|35|37|38|39|(2:41|42)|43|45|46|47|49|50|51|53|54|55|57|58|59|(2:61|62)|63|65|66|67|69|70|71|73|74|75|76|77|78|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Can't wrap try/catch for region: R(98:0|1|2|3|5|6|7|9|10|11|13|14|15|16|17|18|19|21|22|23|25|26|27|29|30|31|33|34|35|37|38|39|41|42|43|45|46|47|49|50|51|53|54|55|57|58|59|61|62|63|65|66|67|69|70|71|73|74|75|76|77|78|79|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|101|102|103|104|105|106|107|108|109|110|111|112|113|114|116) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:101:0x0174 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:103:0x017e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:105:0x0188 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:107:0x0192 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:109:0x019c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:111:0x01a6 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:113:0x01b2 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0039 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0043 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:75:0x00f1 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:77:0x00fd */
        /* JADX WARNING: Missing exception handler attribute for start block: B:83:0x011a */
        /* JADX WARNING: Missing exception handler attribute for start block: B:85:0x0124 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:87:0x012e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:89:0x0138 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:91:0x0142 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:93:0x014c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:95:0x0156 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:97:0x0160 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:99:0x016a */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent$USSD_STATE[] r0 = com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent.USSD_STATE.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$UssdEvent$USSD_STATE = r0
                r1 = 1
                com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent$USSD_STATE r2 = com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent.USSD_STATE.USSD_RESPONSE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$UssdEvent$USSD_STATE     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent$USSD_STATE r3 = com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent.USSD_STATE.USSD_INDICATION     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$UssdEvent$USSD_STATE     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent$USSD_STATE r4 = com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent.USSD_STATE.USSD_ERROR     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE[] r3 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.values()
                int r3 = r3.length
                int[] r3 = new int[r3]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE = r3
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r4 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_HELD     // Catch:{ NoSuchFieldError -> 0x0039 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0039 }
                r3[r4] = r1     // Catch:{ NoSuchFieldError -> 0x0039 }
            L_0x0039:
                int[] r3 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0043 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r4 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_RESUMED     // Catch:{ NoSuchFieldError -> 0x0043 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0043 }
                r3[r4] = r0     // Catch:{ NoSuchFieldError -> 0x0043 }
            L_0x0043:
                int[] r3 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x004d }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r4 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.CAMERA_FIRST_FRAME_READY     // Catch:{ NoSuchFieldError -> 0x004d }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x004d }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x004d }
            L_0x004d:
                r3 = 4
                int[] r4 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0058 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r5 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_AVAILABLE     // Catch:{ NoSuchFieldError -> 0x0058 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0058 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x0058 }
            L_0x0058:
                r4 = 5
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0063 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r6 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_RTP_TIMEOUT     // Catch:{ NoSuchFieldError -> 0x0063 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0063 }
                r5[r6] = r4     // Catch:{ NoSuchFieldError -> 0x0063 }
            L_0x0063:
                r5 = 6
                int[] r6 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x006e }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r7 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_RTCP_TIMEOUT     // Catch:{ NoSuchFieldError -> 0x006e }
                int r7 = r7.ordinal()     // Catch:{ NoSuchFieldError -> 0x006e }
                r6[r7] = r5     // Catch:{ NoSuchFieldError -> 0x006e }
            L_0x006e:
                r6 = 7
                int[] r7 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0079 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r8 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_VERYPOOR_QUALITY     // Catch:{ NoSuchFieldError -> 0x0079 }
                int r8 = r8.ordinal()     // Catch:{ NoSuchFieldError -> 0x0079 }
                r7[r8] = r6     // Catch:{ NoSuchFieldError -> 0x0079 }
            L_0x0079:
                r7 = 8
                int[] r8 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0085 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r9 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_FAIR_QUALITY     // Catch:{ NoSuchFieldError -> 0x0085 }
                int r9 = r9.ordinal()     // Catch:{ NoSuchFieldError -> 0x0085 }
                r8[r9] = r7     // Catch:{ NoSuchFieldError -> 0x0085 }
            L_0x0085:
                r8 = 9
                int[] r9 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0091 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r10 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_GOOD_QUALITY     // Catch:{ NoSuchFieldError -> 0x0091 }
                int r10 = r10.ordinal()     // Catch:{ NoSuchFieldError -> 0x0091 }
                r9[r10] = r8     // Catch:{ NoSuchFieldError -> 0x0091 }
            L_0x0091:
                r9 = 10
                int[] r10 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x009d }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r11 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_POOR_QUALITY     // Catch:{ NoSuchFieldError -> 0x009d }
                int r11 = r11.ordinal()     // Catch:{ NoSuchFieldError -> 0x009d }
                r10[r11] = r9     // Catch:{ NoSuchFieldError -> 0x009d }
            L_0x009d:
                r10 = 11
                int[] r11 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00a9 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r12 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_HOLD_FAILED     // Catch:{ NoSuchFieldError -> 0x00a9 }
                int r12 = r12.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a9 }
                r11[r12] = r10     // Catch:{ NoSuchFieldError -> 0x00a9 }
            L_0x00a9:
                r11 = 12
                int[] r12 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00b5 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r13 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.VIDEO_RESUME_FAILED     // Catch:{ NoSuchFieldError -> 0x00b5 }
                int r13 = r13.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b5 }
                r12[r13] = r11     // Catch:{ NoSuchFieldError -> 0x00b5 }
            L_0x00b5:
                r12 = 13
                int[] r13 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00c1 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r14 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.CAMERA_START_FAIL     // Catch:{ NoSuchFieldError -> 0x00c1 }
                int r14 = r14.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c1 }
                r13[r14] = r12     // Catch:{ NoSuchFieldError -> 0x00c1 }
            L_0x00c1:
                r13 = 14
                int[] r14 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00cd }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r15 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.RECORD_START_SUCCESS     // Catch:{ NoSuchFieldError -> 0x00cd }
                int r15 = r15.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cd }
                r14[r15] = r13     // Catch:{ NoSuchFieldError -> 0x00cd }
            L_0x00cd:
                r14 = 15
                int[] r15 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00d9 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r16 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE     // Catch:{ NoSuchFieldError -> 0x00d9 }
                int r16 = r16.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d9 }
                r15[r16] = r14     // Catch:{ NoSuchFieldError -> 0x00d9 }
            L_0x00d9:
                r15 = 16
                int[] r16 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00e5 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r17 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE_NO_SPACE     // Catch:{ NoSuchFieldError -> 0x00e5 }
                int r17 = r17.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e5 }
                r16[r17] = r15     // Catch:{ NoSuchFieldError -> 0x00e5 }
            L_0x00e5:
                r16 = 17
                int[] r17 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00f1 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r18 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.RECORD_STOP_SUCCESS     // Catch:{ NoSuchFieldError -> 0x00f1 }
                int r18 = r18.ordinal()     // Catch:{ NoSuchFieldError -> 0x00f1 }
                r17[r18] = r16     // Catch:{ NoSuchFieldError -> 0x00f1 }
            L_0x00f1:
                int[] r17 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x00fd }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r18 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.RECORD_STOP_FAILURE     // Catch:{ NoSuchFieldError -> 0x00fd }
                int r18 = r18.ordinal()     // Catch:{ NoSuchFieldError -> 0x00fd }
                r19 = 18
                r17[r18] = r19     // Catch:{ NoSuchFieldError -> 0x00fd }
            L_0x00fd:
                int[] r17 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE     // Catch:{ NoSuchFieldError -> 0x0109 }
                com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent$MEDIA_STATE r18 = com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent.MEDIA_STATE.RECORD_STOP_NO_SPACE     // Catch:{ NoSuchFieldError -> 0x0109 }
                int r18 = r18.ordinal()     // Catch:{ NoSuchFieldError -> 0x0109 }
                r19 = 19
                r17[r18] = r19     // Catch:{ NoSuchFieldError -> 0x0109 }
            L_0x0109:
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE[] r15 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.values()
                int r15 = r15.length
                int[] r15 = new int[r15]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE = r15
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r18 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.RINGING_BACK     // Catch:{ NoSuchFieldError -> 0x011a }
                int r18 = r18.ordinal()     // Catch:{ NoSuchFieldError -> 0x011a }
                r15[r18] = r1     // Catch:{ NoSuchFieldError -> 0x011a }
            L_0x011a:
                int[] r1 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0124 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r15 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CALLING     // Catch:{ NoSuchFieldError -> 0x0124 }
                int r15 = r15.ordinal()     // Catch:{ NoSuchFieldError -> 0x0124 }
                r1[r15] = r0     // Catch:{ NoSuchFieldError -> 0x0124 }
            L_0x0124:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x012e }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.TRYING     // Catch:{ NoSuchFieldError -> 0x012e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x012e }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x012e }
            L_0x012e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0138 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.ESTABLISHED     // Catch:{ NoSuchFieldError -> 0x0138 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0138 }
                r0[r1] = r3     // Catch:{ NoSuchFieldError -> 0x0138 }
            L_0x0138:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0142 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.REFRESHFAIL     // Catch:{ NoSuchFieldError -> 0x0142 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0142 }
                r0[r1] = r4     // Catch:{ NoSuchFieldError -> 0x0142 }
            L_0x0142:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x014c }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.CONFERENCE_ADDED     // Catch:{ NoSuchFieldError -> 0x014c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x014c }
                r0[r1] = r5     // Catch:{ NoSuchFieldError -> 0x014c }
            L_0x014c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0156 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.MODIFIED     // Catch:{ NoSuchFieldError -> 0x0156 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0156 }
                r0[r1] = r6     // Catch:{ NoSuchFieldError -> 0x0156 }
            L_0x0156:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0160 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_LOCAL     // Catch:{ NoSuchFieldError -> 0x0160 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0160 }
                r0[r1] = r7     // Catch:{ NoSuchFieldError -> 0x0160 }
            L_0x0160:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x016a }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_REMOTE     // Catch:{ NoSuchFieldError -> 0x016a }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x016a }
                r0[r1] = r8     // Catch:{ NoSuchFieldError -> 0x016a }
            L_0x016a:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0174 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.HELD_BOTH     // Catch:{ NoSuchFieldError -> 0x0174 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0174 }
                r0[r1] = r9     // Catch:{ NoSuchFieldError -> 0x0174 }
            L_0x0174:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x017e }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.ENDED     // Catch:{ NoSuchFieldError -> 0x017e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x017e }
                r0[r1] = r10     // Catch:{ NoSuchFieldError -> 0x017e }
            L_0x017e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0188 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.MODIFY_REQUESTED     // Catch:{ NoSuchFieldError -> 0x0188 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0188 }
                r0[r1] = r11     // Catch:{ NoSuchFieldError -> 0x0188 }
            L_0x0188:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x0192 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.EARLY_MEDIA_START     // Catch:{ NoSuchFieldError -> 0x0192 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0192 }
                r0[r1] = r12     // Catch:{ NoSuchFieldError -> 0x0192 }
            L_0x0192:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x019c }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.ERROR     // Catch:{ NoSuchFieldError -> 0x019c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x019c }
                r0[r1] = r13     // Catch:{ NoSuchFieldError -> 0x019c }
            L_0x019c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x01a6 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.SESSIONPROGRESS     // Catch:{ NoSuchFieldError -> 0x01a6 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x01a6 }
                r0[r1] = r14     // Catch:{ NoSuchFieldError -> 0x01a6 }
            L_0x01a6:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x01b2 }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.FORWARDED     // Catch:{ NoSuchFieldError -> 0x01b2 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x01b2 }
                r2 = 16
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x01b2 }
            L_0x01b2:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE     // Catch:{ NoSuchFieldError -> 0x01bc }
                com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent$CALL_STATE r1 = com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent.CALL_STATE.EXTEND_TO_CONFERENCE     // Catch:{ NoSuchFieldError -> 0x01bc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x01bc }
                r0[r1] = r16     // Catch:{ NoSuchFieldError -> 0x01bc }
            L_0x01bc:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsCallSessionEventHandler.AnonymousClass1.<clinit>():void");
        }
    }
}
