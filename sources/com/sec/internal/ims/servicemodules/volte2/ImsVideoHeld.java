package com.sec.internal.ims.servicemodules.volte2;

import android.os.Message;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;

public class ImsVideoHeld extends CallState {
    ImsVideoHeld(CallStateMachine callStateMachine) {
        super(callStateMachine);
    }

    public void enter() {
        if (!this.mModule.getSessionByState(this.mSession.getPhoneId(), CallConstants.STATE.IncomingCall).isEmpty() && this.mSession.getCallProfile().getCallType() != 4) {
            this.mSession.stopCamera();
        }
        this.mMediaController.setVideoPause(this.mSession.getSessionId(), true);
        Log.i(this.LOG_TAG, "Enter [VideoHeld]");
    }

    public boolean processMessage(Message message) {
        String str = this.LOG_TAG;
        Log.i(str, "[VideoHeld] processMessage " + message.what);
        int i = message.what;
        if (i == 80) {
            Log.i(this.LOG_TAG, "[VideoHeld] already held. ignore.");
            return true;
        } else if (i != 81) {
            return false;
        } else {
            if (this.mMno != Mno.VZW) {
                return true;
            }
            int determineCamera = this.mCsm.determineCamera(this.mSession.getCallProfile().getCallType(), false);
            if (determineCamera >= 0) {
                this.mSession.startCamera(determineCamera);
            }
            this.mCsm.isDeferedVideoResume = false;
            this.mMediaController.resumeVideo(this.mSession.getSessionId());
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mResumingVideo);
            return true;
        }
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
    }
}
