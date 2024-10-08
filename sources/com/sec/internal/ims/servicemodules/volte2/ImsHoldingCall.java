package com.sec.internal.ims.servicemodules.volte2;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;

public class ImsHoldingCall extends CallState {
    ImsHoldingCall(CallStateMachine callStateMachine) {
        super(callStateMachine);
    }

    public void enter() {
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.callType = 0;
        callStateMachine.errorCode = -1;
        callStateMachine.errorMessage = "";
        Log.i(this.LOG_TAG, "Enter [HoldingCall]");
        if (this.mSession.getUsingCamera()) {
            this.mSession.stopCamera();
        }
    }

    public boolean processMessage(Message message) {
        String str = this.LOG_TAG;
        Log.i(str, "[HoldingCall] processMessage " + message.what);
        int i = message.what;
        if (i != 1) {
            if (i == 41) {
                this.mCsm.handleRemoteHeld(false);
            } else if (i == 55) {
                return switchRequest_HoldingCall(message);
            } else {
                if (i == 91) {
                    modified_HoldingCall(message);
                } else if (!(i == 100 || i == 400)) {
                    if (i == 502) {
                        this.mCsm.mReinvite = true;
                        Log.i(this.LOG_TAG, "[HoldingCall] Re-INVITE defered");
                        this.mCsm.deferMessage(message);
                    } else if (i != 5000) {
                        if (i == 3) {
                            CallStateMachine callStateMachine = this.mCsm;
                            callStateMachine.errorCode = NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR;
                            callStateMachine.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
                            CallStateMachine callStateMachine2 = this.mCsm;
                            callStateMachine2.transitionTo(callStateMachine2.mEndingCall);
                            this.mCsm.sendMessage(3);
                        } else if (i == 4) {
                            error_HoldingCall(message);
                        } else if (i == 51) {
                            Log.i(this.LOG_TAG, "ignore hold request while processing hold");
                            this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
                        } else if (i != 52) {
                            switch (i) {
                                case 61:
                                    break;
                                case 62:
                                    this.mCsm.handleRemoteHeld(true);
                                    break;
                                case 63:
                                    if (this.mMno != Mno.TELSTRA) {
                                        this.mCsm.handleRemoteHeld(true);
                                        break;
                                    }
                                    break;
                                default:
                                    String str2 = this.LOG_TAG;
                                    Log.e(str2, "[" + getName() + "] msg:" + message.what + " ignored !!!");
                                    break;
                            }
                            CallStateMachine callStateMachine3 = this.mCsm;
                            callStateMachine3.transitionTo(callStateMachine3.mHeldCall);
                        } else {
                            update_HoldingCall(message);
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
        this.mCsm.mHoldingProfile = null;
    }

    private void error_HoldingCall(Message message) {
        SipError sipError = (SipError) message.obj;
        if (sipError.getCode() >= 5000) {
            Log.i(this.LOG_TAG, "[HoldingCall] big data code over 5000 means call ended");
            this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mEndingCall);
            this.mCsm.sendMessage(3);
        } else if (sipError.getCode() != 491 || this.mModule.getTotalCallCount(this.mSession.getPhoneId()) <= 1) {
            CallStateMachine callStateMachine2 = this.mCsm;
            if (callStateMachine2.mHoldBeforeTransfer) {
                callStateMachine2.notifyOnError(1119, "call transfer failed (" + message.arg1 + ")");
                this.mCsm.mHoldBeforeTransfer = false;
            } else {
                callStateMachine2.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
            }
            CallStateMachine callStateMachine3 = this.mCsm;
            callStateMachine3.transitionTo(callStateMachine3.mInCall);
        } else {
            this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed", 0);
            CallStateMachine callStateMachine4 = this.mCsm;
            callStateMachine4.transitionTo(callStateMachine4.mInCall);
        }
    }

    private void update_HoldingCall(Message message) {
        CallProfile parcelable = ((Bundle) message.obj).getParcelable("profile");
        if (parcelable != null && ImsCallUtil.isVideoCall(parcelable.getCallType())) {
            Log.i(this.LOG_TAG, "[HoldingCall] Holding request is ongoing return fail to UPDATE from APP");
            this.mCsm.mHoldingProfile = parcelable;
        }
        this.mCsm.notifyOnError(1109, "Call switch failed", 10);
    }

    private boolean switchRequest_HoldingCall(Message message) {
        Log.i(this.LOG_TAG, "Rejecting switch request - send 603 to remote party");
        if (this.mCsm.rejectModifyCallType(Id.REQUEST_UPDATE_TIME_IN_PLANI) >= 0) {
            return true;
        }
        this.mCsm.sendMessage(4, 0, -1, new SipError(1006, ""));
        return false;
    }

    private void modified_HoldingCall(Message message) {
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
}
