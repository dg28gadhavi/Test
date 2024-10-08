package com.sec.internal.ims.servicemodules.volte2;

import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.State;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;

public class ImsEndingCall extends CallState {
    ImsEndingCall(CallStateMachine callStateMachine) {
        super(callStateMachine);
    }

    public void enter() {
        this.mCsm.mCallTerminateTime = SystemClock.elapsedRealtime();
        this.mCsm.callType = 0;
        String str = this.LOG_TAG;
        Log.i(str, "Enter [EndingCall], errorCode=" + this.mCsm.errorCode);
        if (this.mCsm.needToLogForATTGate(this.mSession.getCallProfile().getCallType())) {
            IMSLog.g("GATE", "<GATE-M>DISCONNECT_VIDEO_CALL</GATE-M>");
        }
        CallStateMachine callStateMachine = this.mCsm;
        if (callStateMachine.errorCode == -1) {
            callStateMachine.errorCode = 200;
            callStateMachine.errorMessage = "";
        }
        callStateMachine.stopNetworkStatsOnPorts();
        this.mMediaController.stopEmoji(this.mSession.getSessionId());
        if (this.mSession.getUsingCamera() || (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType()) && this.mMno.isChn())) {
            if (!this.mMno.isKor() && this.mMno != Mno.DOCOMO) {
                Log.i(this.LOG_TAG, "stopCamera in EndingCall state");
                this.mSession.stopCamera();
            }
            this.mMediaController.resetCameraId();
        }
        this.mMediaController.unregisterForMediaEvent(this.mSession);
        ImsRegistration imsRegistration = this.mRegistration;
        if ((imsRegistration != null && imsRegistration.getImsProfile().isSoftphoneEnabled() && this.mSession.getCallProfile().getCallType() == 13) || this.mSession.getCallProfile().getCallType() == 7 || this.mSession.getCallProfile().getCallType() == 8) {
            Log.i(this.LOG_TAG, "[EndingCall] E911 Call end - restore User location settings");
            IGeolocationController geolocationController = ImsRegistry.getGeolocationController();
            if (geolocationController != null && this.mCsm.mRequestLocation) {
                geolocationController.stopGeolocationUpdate();
                this.mCsm.mRequestLocation = false;
            }
            if (!this.mModule.getSessionByState(this.mSession.getPhoneId(), CallConstants.STATE.HeldCall).isEmpty() && this.mRegistration != null && !ImsCallUtil.isCmcPrimaryType(this.mSession.getCmcType())) {
                Log.i(this.LOG_TAG, "bindToNetwork for Normal call");
                this.mMediaController.bindToNetwork(this.mRegistration.getNetwork());
            }
        }
        ImsRegistry.getImsDiagMonitor().notifyCallStatus(this.mSession.getSessionId(), "CALL_ENDED", this.mSession.getCallProfile().getCallType(), this.mSession.getCallProfile().getMediaProfile().getAudioCodec().toString());
        if ((this.mSession.getCallProfile().getRejectCause() != 0 || (this.mMno.isChn() && (isEmergencyCallAndNotRegistered() || this.mSession.getSessionId() == -1))) && (!this.mMno.isKor() || (this.mRegistration == null && !ImsCallUtil.isE911Call(this.mSession.getCallProfile().getCallType())))) {
            this.mCsm.sendMessage(2);
        } else {
            Log.i(this.LOG_TAG, "[EndingCall] start EndCall timer (5 sec).");
            this.mCsm.sendMessageDelayed(2, 5000);
        }
        if (this.mSession.getCmcType() == 2) {
            this.mModule.getCmcServiceHelper().stopCmcHandoverTimer(this.mRegistration);
        }
    }

    public boolean processMessage(Message message) {
        String str = this.LOG_TAG;
        Log.i(str, "[EndingCall] processMessage " + message.what);
        int i = message.what;
        if (i == 1) {
            terminate_EndingCall(message);
        } else if (i == 2) {
            terminated_EndingCall(message);
        } else if (i == 3) {
            ended_EndingCall(message);
        } else if (i == 4) {
            error_EndingCall(message);
        } else if (i != 55) {
            String str2 = this.LOG_TAG;
            Log.e(str2, getName() + " msg:" + message.what + " ignored !!!");
        } else {
            switchRequest_EndingCall(message);
        }
        return true;
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
    }

    private void terminate_EndingCall(Message message) {
        int i = message.arg1;
        if (i < 0) {
            i = 5;
        }
        if (i == 5) {
            this.mSession.setEndType(1);
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.errorCode = 200;
            callStateMachine.notifyOnEnded(200);
        }
    }

    private void ended_EndingCall(Message message) {
        handleCallLoggingOnEndingCall();
        CallStateMachine callStateMachine = this.mCsm;
        if (callStateMachine.mCameraUsedAtOtherApp) {
            callStateMachine.mCameraUsedAtOtherApp = false;
            callStateMachine.sendMessageDelayed(3, 500);
            return;
        }
        callStateMachine.srvccStarted = false;
        if (callStateMachine.mConfCallAdded) {
            Log.i(this.LOG_TAG, "Call end by Join to conference session");
            this.mCsm.mConfCallAdded = false;
            this.mSession.setEndType(1);
            this.mSession.setEndReason(7);
        } else if (callStateMachine.mRetryInprogress) {
            Log.i(this.LOG_TAG, "ImsTelePhonyService is handling retry!!");
            this.mCsm.mRetryInprogress = false;
        } else {
            if ((this.mMno.isChn() || this.mMno.isKor() || this.mMno.isJpn()) && this.mModule.getSessionCount() == 2) {
                ImsCallSession foregroundSession = this.mModule.getForegroundSession();
                if (foregroundSession != null) {
                    CallProfile callProfile = foregroundSession.getCallProfile();
                    String str = this.LOG_TAG;
                    Log.i(str, "setRemoteVideoCapa() : " + callProfile.getModifyHeader());
                    if (CloudMessageProviderContract.JsonData.TRUE.equals(callProfile.getModifyHeader())) {
                        foregroundSession.getCallProfile().setRemoteVideoCapa(true);
                    } else {
                        foregroundSession.getCallProfile().setRemoteVideoCapa(false);
                    }
                    foregroundSession.forceNotifyCurrentCodec();
                } else {
                    Log.i(this.LOG_TAG, "getForegroundSessionn is NULL");
                }
            }
            onErrorCode(this.mCsm.errorCode);
            CallStateMachine callStateMachine2 = this.mCsm;
            if (callStateMachine2.mIsCmcHandover) {
                Log.i(this.LOG_TAG, "do not notifyOnEnded because it is created for cmc handover");
                ImsCallSession sessionBySipCallId = this.mModule.getSessionBySipCallId(this.mSession.getCallProfile().getReplaceSipCallId());
                if (sessionBySipCallId != null) {
                    sessionBySipCallId.replaceSipCallId(this.mSession.getCallProfile().getSipCallId());
                } else {
                    Log.i(this.LOG_TAG, "replace session is null");
                }
            } else {
                callStateMachine2.notifyOnEnded(callStateMachine2.errorCode);
            }
        }
        this.mModule.onCallEnded(this.mSession.getPhoneId(), this.mSession.getSessionId(), message.arg2);
        this.mCsm.removeMessages(2);
        KeepAliveSender keepAliveSender = this.mSession.mKaSender;
        if (keepAliveSender != null) {
            keepAliveSender.stop();
        }
        this.mCsm.quit();
    }

    private void switchRequest_EndingCall(Message message) {
        Log.i(this.LOG_TAG, "Rejecting switch request - send 603 to remote party");
        if (this.mCsm.rejectModifyCallType(Id.REQUEST_UPDATE_TIME_IN_PLANI) < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1006, ""));
        }
    }

    private void terminated_EndingCall(Message message) {
        handleCallLoggingOnEndingCall();
        CallStateMachine callStateMachine = this.mCsm;
        if (callStateMachine.mConfCallAdded) {
            Log.i(this.LOG_TAG, "Call end by Join to conference session");
            this.mCsm.mConfCallAdded = false;
            this.mSession.setEndType(1);
            this.mSession.setEndReason(7);
        } else if (callStateMachine.mRetryInprogress) {
            Log.i(this.LOG_TAG, "ImsTelePhonyService is handling retry!!");
            this.mCsm.mRetryInprogress = false;
        } else {
            onErrorCode(callStateMachine.errorCode);
            CallStateMachine callStateMachine2 = this.mCsm;
            callStateMachine2.notifyOnEnded(callStateMachine2.errorCode);
        }
        CallStateMachine callStateMachine3 = this.mCsm;
        callStateMachine3.sipReason = callStateMachine3.getSipReasonFromUserReason(5);
        Mno mno = this.mMno;
        if (mno == Mno.DOCOMO || mno == Mno.KDDI) {
            CallStateMachine callStateMachine4 = this.mCsm;
            if (callStateMachine4.errorCode == 709) {
                callStateMachine4.sipReason = callStateMachine4.getSipReasonFromUserReason(25);
            }
        }
        IVolteServiceInterface iVolteServiceInterface = this.mVolteSvcIntf;
        int sessionId = this.mSession.getSessionId();
        CallStateMachine callStateMachine5 = this.mCsm;
        if (iVolteServiceInterface.endCall(sessionId, callStateMachine5.callType, callStateMachine5.sipReason) < 0) {
            Log.i(this.LOG_TAG, "[EndingCall] endCall failed but call terminated");
        }
        this.mModule.onCallEnded(this.mSession.getPhoneId(), this.mSession.getSessionId(), message.arg2);
        Log.i(this.LOG_TAG, "[EndingCall] timeout. force to exit.");
        KeepAliveSender keepAliveSender = this.mSession.mKaSender;
        if (keepAliveSender != null) {
            keepAliveSender.stop();
        }
        this.mCsm.quit();
    }

    private void error_EndingCall(Message message) {
        SipError sipError = (SipError) message.obj;
        String str = this.LOG_TAG;
        Log.i(str, "[EndingCall] err: " + sipError.getCode() + ", errorCode: " + this.mCsm.errorCode);
        CallStateMachine callStateMachine = this.mCsm;
        if (callStateMachine.errorCode != 2414) {
            callStateMachine.errorCode = sipError.getCode();
        }
        if (this.mMno == Mno.KDDI && this.mModule.getSessionCount(this.mSession.getPhoneId()) > 1) {
            CallStateMachine callStateMachine2 = this.mCsm;
            if (callStateMachine2.errorCode == 709) {
                callStateMachine2.notifyOnError(503, "Session Progress Timeout", 0);
            }
        }
    }

    private void onErrorCode(int i) {
        if (this.mCsm.mIsBigDataEndReason) {
            if (this.mSession.getDRBLost() && i >= 5000) {
                i = i >= 6000 ? i + 200 : i + Id.REQUEST_SIP_DIALOG_SEND_SIP;
            }
            this.mSession.setDRBLost(false);
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.notifyOnError(i, callStateMachine.errorMessage);
        }
        if ((this.mCsm.mIsBigDataEndReason || i >= 5000) && !this.mModule.isCsfbErrorCode(this.mSession.getPhoneId(), this.mSession.getCallProfile(), new SipError(i, this.mCsm.errorMessage))) {
            this.mModule.sendQualityStatisticsEvent();
        }
    }

    /* access modifiers changed from: protected */
    public void handleCallLoggingOnEndingCall() {
        this.mCsm.mCallEndTime = SystemClock.elapsedRealtime();
        this.mSession.mDiagnosisController.sendPSCallInfo();
        this.mSession.mDiagnosisController.sendPSDailyInfo();
        CallStateMachine callStateMachine = this.mCsm;
        if (callStateMachine.lazerErrorCode == -1) {
            callStateMachine.lazerErrorCode = callStateMachine.errorCode;
        }
        if (TextUtils.isEmpty(callStateMachine.lazerErrorMessage)) {
            CallStateMachine callStateMachine2 = this.mCsm;
            callStateMachine2.lazerErrorMessage = callStateMachine2.errorMessage;
        }
        if (this.mSession.getCallProfile().hasCSFBError()) {
            IMSLog.LAZER_TYPE lazer_type = IMSLog.LAZER_TYPE.CALL;
            IMSLog.lazer(lazer_type, this.mSession.getCallId() + " - RETRY OVER CS");
        } else if (!this.mSession.mDiagnosisController.isCallDrop(this.mCsm.errorCode)) {
            IMSLog.LAZER_TYPE lazer_type2 = IMSLog.LAZER_TYPE.CALL;
            IMSLog.lazer(lazer_type2, this.mSession.getCallId() + " - END");
        } else {
            State previousState = this.mCsm.getPreviousState();
            CallStateMachine callStateMachine3 = this.mCsm;
            if (previousState != callStateMachine3.mOutgoingCall) {
                State previousState2 = callStateMachine3.getPreviousState();
                CallStateMachine callStateMachine4 = this.mCsm;
                if (previousState2 != callStateMachine4.mAlertingCall && (callStateMachine4.getPreviousState() != this.mCsm.mReadyToCall || !this.mSession.getCallProfile().isMOCall())) {
                    State previousState3 = this.mCsm.getPreviousState();
                    CallStateMachine callStateMachine5 = this.mCsm;
                    if (previousState3 == callStateMachine5.mIncomingCall || (callStateMachine5.getPreviousState() == this.mCsm.mReadyToCall && this.mSession.getCallProfile().isMTCall())) {
                        IMSLog.LAZER_TYPE lazer_type3 = IMSLog.LAZER_TYPE.CALL;
                        IMSLog.lazer(lazer_type3, this.mSession.getCallId() + " - RECEIVE FAIL");
                    } else {
                        IMSLog.LAZER_TYPE lazer_type4 = IMSLog.LAZER_TYPE.CALL;
                        IMSLog.lazer(lazer_type4, this.mSession.getCallId() + " - DROP");
                    }
                }
            }
            IMSLog.LAZER_TYPE lazer_type5 = IMSLog.LAZER_TYPE.CALL;
            IMSLog.lazer(lazer_type5, this.mSession.getCallId() + " - OUTGOING FAIL");
        }
        IMSLog.LAZER_TYPE lazer_type6 = IMSLog.LAZER_TYPE.CALL;
        IMSLog.lazer(lazer_type6, this.mSession.getCallId() + " - SIP REASON : " + this.mSession.getErrorMessage() + "(" + this.mSession.getErrorCode() + ")");
        IMSLog.lazer(lazer_type6, this.mSession.getCallId() + " - INTERNAL REASON : " + this.mCsm.lazerErrorMessage + "(" + this.mCsm.lazerErrorCode + ")");
        if (this.mSession.getEndType() == 3) {
            IMSLog.lazer(lazer_type6, this.mSession.getCallId() + " - LOCAL RELEASE");
        }
    }

    private boolean isEmergencyCallAndNotRegistered() {
        return this.mSession.getCallProfile().getNetworkType() == 15 && !this.mModule.isEmergencyRegistered(this.mSession.getPhoneId());
    }
}
