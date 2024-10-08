package com.sec.internal.ims.servicemodules.volte2;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;

public class ImsHeldCall extends CallState {
    ImsHeldCall(CallStateMachine callStateMachine) {
        super(callStateMachine);
    }

    public void enter() {
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.callType = 0;
        callStateMachine.errorCode = -1;
        callStateMachine.errorMessage = "";
        if (callStateMachine.mHoldBeforeTransfer) {
            this.mModule.pushCallInternal();
        } else {
            callStateMachine.notifyOnHeld(true);
        }
        Log.i(this.LOG_TAG, "Enter [HeldCall]");
    }

    public boolean processMessage(Message message) {
        String str = this.LOG_TAG;
        Log.i(str, "[HeldCall] processMessage " + message.what);
        int i = message.what;
        if (!(i == 3 || i == 4)) {
            if (i == 51) {
                Log.i(this.LOG_TAG, "[HeldCall] already in HOLD");
                this.mCsm.notifyOnHeld(true);
            } else if (i != 52) {
                switch (i) {
                    case 1:
                    case 100:
                    case 400:
                    case 5000:
                        break;
                    case 41:
                    case 61:
                        this.mCsm.handleRemoteHeld(false);
                        break;
                    case 55:
                        return switchRequest_HeldCall(message);
                    case 59:
                        this.mCsm.transferCall((String) message.obj);
                        break;
                    case 63:
                        this.mCsm.handleRemoteHeld(true);
                        break;
                    case 71:
                        resume_HeldCall();
                        break;
                    case 75:
                        refuerStatus_HeldCall(message);
                        break;
                    case 91:
                        modified_HeldCall(message);
                        break;
                    case 202:
                        this.mCsm.sendMessage(71);
                        break;
                    case 502:
                        Log.i(this.LOG_TAG, "[HeldCall] ignore re-INVITE request");
                        break;
                    default:
                        String str2 = this.LOG_TAG;
                        Log.e(str2, "[" + getName() + "] msg:" + message.what + " ignored !!!");
                        break;
                }
            } else {
                update_HeldCall(message);
            }
            return true;
        }
        return false;
    }

    public void exit() {
        this.mCsm.stopRetriggerTimer();
        this.mCsm.setPreviousState(this);
        this.mCsm.mHeldProfile = null;
    }

    private void resume_HeldCall() {
        if (this.mVolteSvcIntf.resumeCall(this.mSession.getSessionId()) < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1006, ""));
            return;
        }
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.transitionTo(callStateMachine.mResumingCall);
    }

    private void refuerStatus_HeldCall(Message message) {
        CallStateMachine callStateMachine = this.mCsm;
        if (callStateMachine.mTransferRequested) {
            if (message.arg1 != 200) {
                callStateMachine.notifyOnError(1119, "call transfer failed (" + message.arg1 + ")");
                Iterator<ImsCallSession> it = this.mModule.getSessionList(this.mSession.mPhoneId).iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ImsCallSession next = it.next();
                    CallStateMachine callStateMachine2 = next.smCallStateMachine;
                    if (callStateMachine2.mHoldBeforeTransfer) {
                        callStateMachine2.sendMessage(71);
                        CallStateMachine callStateMachine3 = next.smCallStateMachine;
                        callStateMachine3.mHoldBeforeTransfer = false;
                        callStateMachine3.mTransferRequested = false;
                        break;
                    }
                }
            } else {
                callStateMachine.sendMessage(4, 0, -1, new SipError(1118, "call transfer success (" + message.arg1 + ")"));
            }
            CallStateMachine callStateMachine4 = this.mCsm;
            callStateMachine4.mHoldBeforeTransfer = false;
            callStateMachine4.mTransferRequested = false;
        }
    }

    private void update_HeldCall(Message message) {
        Bundle bundle = (Bundle) message.obj;
        CallProfile parcelable = bundle.getParcelable("profile");
        int srvccVersion = this.mModule.getSrvccVersion(this.mSession.getPhoneId());
        if (parcelable != null || srvccVersion == 0) {
            if (parcelable != null) {
                if (ImsCallUtil.isVideoCall(parcelable.getCallType())) {
                    Log.i(this.LOG_TAG, "[HeldCall] Held request is ongoing return fail to UPDATE from APP");
                    this.mCsm.mHeldProfile = parcelable;
                } else if (ImsCallUtil.isRttCall(parcelable.getCallType()) || ImsCallUtil.isRttCall(this.mSession.getCallProfile().getCallType())) {
                    this.mCsm.mHeldProfile = parcelable;
                }
            }
            this.mCsm.notifyOnError(1109, "Call switch failed", 10);
        } else if (srvccVersion >= 9 || DeviceUtil.getGcfMode()) {
            Log.i(this.LOG_TAG, "mid-call sRVCC supported [during held state]");
            int i = bundle.getInt("cause");
            if (i == 100) {
                Log.i(this.LOG_TAG, "SRVCC HO STARTED");
                IMSLog.c(LogClass.VOLTE_SRVCC_START, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
                this.mCsm.srvccStarted = true;
            } else if (i == 200) {
                Log.i(this.LOG_TAG, "SRVCC HO SUCCESS");
                IMSLog.c(LogClass.VOLTE_SRVCC_SUCCESS, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
                this.mCsm.srvccStarted = false;
            } else if (i == 487) {
                Log.i(this.LOG_TAG, "SRVCC HO FAILURE OR CANCELED");
                IMSLog.c(LogClass.VOLTE_SRVCC_FAIL, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
                this.mCsm.srvccStarted = false;
                this.mVolteSvcIntf.sendReInvite(this.mSession.getSessionId(), new SipReason("SIP", i, bundle.getString("reasonText"), new String[0]));
            }
        }
    }

    private boolean switchRequest_HeldCall(Message message) {
        Log.i(this.LOG_TAG, "Rejecting switch request - send 603 to remote party");
        if (this.mCsm.rejectModifyCallType(Id.REQUEST_UPDATE_TIME_IN_PLANI) >= 0) {
            return true;
        }
        this.mCsm.sendMessage(4, 0, -1, new SipError(1006, ""));
        return false;
    }

    private void modified_HeldCall(Message message) {
        int i = message.arg1;
        int i2 = message.arg2;
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
            if (ImsCallUtil.isRttCall(i) || ImsCallUtil.isRttCall(i2)) {
                this.mModule.onSendRttSessionModifyRequest(this.mSession.getCallId(), ImsCallUtil.isRttCall(i));
            }
        }
    }
}
