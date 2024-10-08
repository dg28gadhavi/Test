package com.sec.internal.ims.servicemodules.volte2;

import android.os.Message;
import android.util.Log;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;

public class ImsResumingCall extends CallState {
    ImsResumingCall(CallStateMachine callStateMachine) {
        super(callStateMachine);
    }

    public void enter() {
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.callType = 0;
        callStateMachine.errorCode = -1;
        callStateMachine.errorMessage = "";
        Log.i(this.LOG_TAG, "Enter [ResumingCall]");
    }

    public boolean processMessage(Message message) {
        String str = this.LOG_TAG;
        Log.i(str, "[ResumingCall] processMessage " + message.what);
        int i = message.what;
        if (i != 1) {
            if (i == 41) {
                established_ResumingCall();
            } else if (i == 55) {
                switchRequest_ResumingCall();
            } else if (i == 71) {
                Log.i(this.LOG_TAG, "[ResumingCall] ignore resume request while processing resume");
                this.mCsm.notifyOnError(1112, "Call resume failed");
            } else if (i == 91) {
                modified_ResumingCall(message);
            } else if (!(i == 100 || i == 400)) {
                if (i == 502) {
                    Log.i(this.LOG_TAG, "[ResumingCall] Re-INVITE defered");
                    this.mCsm.deferMessage(message);
                } else if (i != 5000) {
                    if (i == 3) {
                        CallStateMachine callStateMachine = this.mCsm;
                        callStateMachine.errorCode = 1112;
                        callStateMachine.notifyOnError(1112, "Call resume failed");
                        CallStateMachine callStateMachine2 = this.mCsm;
                        callStateMachine2.transitionTo(callStateMachine2.mEndingCall);
                        this.mCsm.sendMessage(3);
                    } else if (i == 4) {
                        error_ResumingCall(message);
                    } else if (i == 51) {
                        Log.i(this.LOG_TAG, "[ResumingCall] Rejecting hold request while processing modify");
                        this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
                    } else if (i != 52) {
                        switch (i) {
                            case 61:
                                this.mCsm.handleRemoteHeld(false);
                                break;
                            case 62:
                                heldRemote_ResumingCall();
                                break;
                            case 63:
                                this.mCsm.handleRemoteHeld(true);
                                break;
                            default:
                                String str2 = this.LOG_TAG;
                                Log.e(str2, "[" + getName() + "] msg:" + message.what + " ignored !!!");
                                break;
                        }
                    } else {
                        this.mCsm.notifyOnError(1109, "Call switch failed", 10);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void modified_ResumingCall(Message message) {
        int i = message.arg1;
        this.mCsm.notifyOnModified(i);
        if (!ImsCallUtil.isTtyCall(i)) {
            CallProfile callProfile = new CallProfile();
            callProfile.setCallType(i);
            callProfile.getMediaProfile().setVideoQuality(this.mSession.getCallProfile().getMediaProfile().getVideoQuality());
            IImsMediaController iImsMediaController = this.mMediaController;
            int sessionId = this.mSession.getSessionId();
            ImsCallSession imsCallSession = this.mSession;
            CallProfile callProfile2 = imsCallSession.mModifyRequestedProfile;
            if (callProfile2 == null) {
                callProfile2 = imsCallSession.getCallProfile();
            }
            iImsMediaController.receiveSessionModifyResponse(sessionId, 200, callProfile2, callProfile);
        }
    }

    private void heldRemote_ResumingCall() {
        if (this.mMno == Mno.TELSTRA) {
            this.mCsm.mRemoteHeld = false;
        }
        this.mCsm.notifyOnResumed(true);
        this.mCsm.handleRemoteHeld(true);
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.transitionTo(callStateMachine.mInCall);
    }

    private void established_ResumingCall() {
        this.mCsm.sendCmcPublishDialog();
        this.mCsm.notifyOnResumed(true);
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.transitionTo(callStateMachine.mInCall);
    }

    private void switchRequest_ResumingCall() {
        Log.i(this.LOG_TAG, "Rejecting switch request - send 603 to remote party");
        if (this.mCsm.rejectModifyCallType(Id.REQUEST_UPDATE_TIME_IN_PLANI) < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1006, ""));
        }
    }

    private void error_ResumingCall(Message message) {
        if (((SipError) message.obj).getCode() >= 5000) {
            Log.i(this.LOG_TAG, "[ResumingCall] big data code over 5000 means call ended");
            this.mCsm.notifyOnError(1112, "Call resume failed");
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mEndingCall);
            this.mCsm.sendMessage(3);
            return;
        }
        this.mCsm.notifyOnError(1112, "Call resume failed");
        int totalCallCount = this.mModule.getTotalCallCount(this.mSession.getPhoneId());
        int i = this.mSession.mResumeCallRetriggerTimer;
        if (i != 0 && totalCallCount == 1) {
            this.mCsm.startRetriggerTimer((long) i);
        }
        CallStateMachine callStateMachine2 = this.mCsm;
        callStateMachine2.transitionTo(callStateMachine2.mHeldCall);
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
    }
}
