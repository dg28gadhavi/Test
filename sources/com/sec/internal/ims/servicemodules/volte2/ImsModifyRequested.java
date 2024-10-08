package com.sec.internal.ims.servicemodules.volte2;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.ims.core.imsdc.IdcImsCallSessionData;
import com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra;

public class ImsModifyRequested extends CallState {
    ImsModifyRequested(CallStateMachine callStateMachine) {
        super(callStateMachine);
    }

    public void enter() {
        CallProfile modifyRequestedProfile = this.mSession.getModifyRequestedProfile();
        if (modifyRequestedProfile != null && ImsCallUtil.isUpgradeCall(this.mSession.getCallProfile().getCallType(), modifyRequestedProfile.getCallType())) {
            this.mCsm.sendCmcPublishDialog();
        }
        Log.i(this.LOG_TAG, "Enter [ModifyRequested]");
    }

    public boolean processMessage(Message message) {
        String str = this.LOG_TAG;
        Log.i(str, "[ModifyRequested] processMessage " + message.what);
        int i = message.what;
        if (i == 3 || i == 4) {
            return false;
        }
        if (i == 22) {
            accept_ModifyRequested(message);
        } else if (i == 23) {
            reject_ModifyRequested(message);
        } else if (i == 51) {
            Log.i(this.LOG_TAG, "[ModifyRequested] Rejecting hold request while processing modify");
            this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
        } else if (i == 52) {
            update_ModifyRequested(message);
        } else if (i == 80) {
            Log.i(this.LOG_TAG, "[ModifyRequested] Hold video defered");
            this.mCsm.deferMessage(message);
        } else if (i != 81) {
            switch (i) {
                case 1:
                case 100:
                case 400:
                case 5000:
                    return false;
                case 55:
                    switchRequest_ModifyRequested(message);
                    break;
                case 62:
                    this.mCsm.handleRemoteHeld(true);
                    break;
                case 64:
                    Log.i(this.LOG_TAG, "[ModifyRequested] SEND_TEXT defered");
                    this.mCsm.deferMessage(message);
                    break;
                case 71:
                    Log.i(this.LOG_TAG, "[ModifyRequested] Rejecting resume request while processing modify");
                    this.mCsm.notifyOnError(1112, "Call resume failed");
                    break;
                case 91:
                    moidfied_ModifyRequested(message);
                    break;
                case 150:
                    accept_IdcModifyRequested((IdcExtra) message.obj);
                    break;
                case 153:
                    idcModified_ModifyRequested(message);
                    break;
                case 502:
                    Log.i(this.LOG_TAG, "[ModifyRequested] Re-INVITE defered");
                    this.mCsm.deferMessage(message);
                    break;
                default:
                    String str2 = this.LOG_TAG;
                    Log.e(str2, "[" + getName() + "] msg:" + message.what + " ignored !!!");
                    break;
            }
        } else {
            Log.i(this.LOG_TAG, "[ModifyRequested] Resume video defered");
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.isDeferedVideoResume = true;
            callStateMachine.deferMessage(message);
        }
        return true;
    }

    private void idcModified_ModifyRequested(Message message) {
        Log.i(this.LOG_TAG, "[IDC] idcModified_ModifyRequested");
        if (this.mSession.getIdcData() != null) {
            if (this.mSession.getIdcData().getCurrentState() != IdcImsCallSessionData.State.MODIFY_REQUESTED) {
                Log.i(this.LOG_TAG, "[IDC] Idc State isn't MODIFY_REQUESTED");
                return;
            }
            this.mSession.getIdcData().transitState(IdcImsCallSessionData.State.NEGOTIATED);
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mInCall);
        }
    }

    private void accept_IdcModifyRequested(IdcExtra idcExtra) {
        Log.i(this.LOG_TAG, "[IDC]accept_IdcModifyRequested");
        this.mCsm.modifyIdcReply(idcExtra);
    }

    private void accept_ModifyRequested(Message message) {
        CallProfile callProfile = (CallProfile) message.obj;
        if (this.mCsm.isChangedCallType(callProfile)) {
            this.mCsm.modifyCallType(callProfile, false);
        }
        this.mSession.mModifyRequestedProfile = callProfile;
    }

    private void reject_ModifyRequested(Message message) {
        if (this.mSession.getUsingCamera()) {
            ImsCallSession imsCallSession = this.mSession;
            imsCallSession.mLastUsedCamera = imsCallSession.mPrevUsedCamera;
        }
        if (this.mCsm.rejectModifyCallType(message.arg1) < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1006, ""));
            return;
        }
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.transitionTo(callStateMachine.mInCall);
    }

    private void moidfied_ModifyRequested(Message message) {
        int i = message.arg1;
        int i2 = message.arg2;
        CallProfile callProfile = new CallProfile();
        callProfile.setCallType(i);
        if (this.mMno != Mno.CMCC || i2 != i) {
            this.mCsm.onCallModified(callProfile);
        } else if (!ImsCallUtil.isTtyCall(i)) {
            IImsMediaController iImsMediaController = this.mMediaController;
            int sessionId = this.mSession.getSessionId();
            ImsCallSession imsCallSession = this.mSession;
            CallProfile callProfile2 = imsCallSession.mModifyRequestedProfile;
            if (callProfile2 == null) {
                callProfile2 = imsCallSession.getCallProfile();
            }
            iImsMediaController.receiveSessionModifyResponse(sessionId, 487, callProfile2, callProfile);
        }
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.transitionTo(callStateMachine.mInCall);
    }

    private void update_ModifyRequested(Message message) {
        CallProfile parcelable = ((Bundle) message.obj).getParcelable("profile");
        if (parcelable == null || !ImsCallUtil.isTtyCall(parcelable.getCallType())) {
            Log.i(this.LOG_TAG, "[ModifyRequested] Modify request from remote is ongoing return fail to UPDATE from APP");
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.mModifyingProfile = parcelable;
            callStateMachine.notifyOnError(1109, "Call switch failed", 10);
            return;
        }
        Log.i(this.LOG_TAG, "[ModifyRequested] defer setTty request.");
        this.mCsm.deferMessage(message);
    }

    private void switchRequest_ModifyRequested(Message message) {
        int i = message.arg1;
        int i2 = message.arg2;
        Mno mno = this.mMno;
        if ((mno == Mno.CTC || mno == Mno.CTCMO) && i2 == 2 && i == 3) {
            Log.i(this.LOG_TAG, "[ModifyRequested] CTC Bidirectional call switch defered");
            this.mCsm.deferMessage(message);
        }
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
        this.mSession.mModifyRequestedProfile = null;
        this.mCsm.mModifyingProfile = null;
    }
}
