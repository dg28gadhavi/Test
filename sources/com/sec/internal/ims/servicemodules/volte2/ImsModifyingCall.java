package com.sec.internal.ims.servicemodules.volte2;

import android.os.Message;
import android.util.Log;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.ims.core.imsdc.IdcImsCallSessionData;
import com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra;

public class ImsModifyingCall extends CallState {
    ImsModifyingCall(CallStateMachine callStateMachine) {
        super(callStateMachine);
    }

    public void enter() {
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.callType = 0;
        callStateMachine.errorCode = -1;
        callStateMachine.errorMessage = "";
        CallProfile modifyRequestedProfile = this.mSession.getModifyRequestedProfile();
        if (modifyRequestedProfile != null && ImsCallUtil.isUpgradeCall(this.mSession.getCallProfile().getCallType(), modifyRequestedProfile.getCallType())) {
            this.mCsm.sendCmcPublishDialog();
        }
        Log.i(this.LOG_TAG, "Enter [ModifyingCall]");
        if (this.mSession.getIdcData() == null || this.mSession.getIdcData().getCurrentState() != IdcImsCallSessionData.State.MODIFYING) {
            this.mModule.onCallModifyRequested(this.mSession.getSessionId());
        } else {
            Log.i(this.LOG_TAG, "[IDC] Session Modifying.");
        }
    }

    public boolean processMessage(Message message) {
        String str = this.LOG_TAG;
        Log.i(str, "[ModifyingCall] processMessage " + message.what);
        int i = message.what;
        if (i != 3) {
            if (i == 4) {
                error_ModifyingCall(message);
            } else if (i == 51) {
                Log.i(this.LOG_TAG, "[ModifyingCall] Rejecting hold request while processing modify");
                this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
            } else if (i == 52) {
                this.mCsm.notifyOnError(1109, "Call switch failed", 10);
            } else if (i == 80) {
                Log.i(this.LOG_TAG, "[ModifyingCall] Hold video defered");
                this.mCsm.deferMessage(message);
            } else if (i != 81) {
                if (i != 84 && i != 85) {
                    switch (i) {
                        case 1:
                        case 100:
                        case 400:
                        case 5000:
                            break;
                        case 41:
                            this.mCsm.handleRemoteHeld(false);
                            break;
                        case 55:
                            switchRequest_ModifyingCall(message);
                            break;
                        case 62:
                            this.mCsm.handleRemoteHeld(true);
                            break;
                        case 64:
                            Log.i(this.LOG_TAG, "[ModifyingCall] SEND_TEXT defered");
                            this.mCsm.deferMessage(message);
                            break;
                        case 71:
                            Log.i(this.LOG_TAG, "[ModifyingCall] Rejecting resume request while processing modify");
                            this.mCsm.notifyOnError(1112, "Call resume failed");
                            break;
                        case 91:
                            modified_ModifyingCall(message);
                            break;
                        case 153:
                            idcModified_ModifyingCall(message);
                            break;
                        case 155:
                            idcError_ModifyingCall(message);
                            break;
                        case 502:
                            Log.i(this.LOG_TAG, "[ModifyingCall] Re-INVITE defered");
                            this.mCsm.deferMessage(message);
                            break;
                        default:
                            String str2 = this.LOG_TAG;
                            Log.e(str2, "[" + getName() + "] msg:" + message.what + " ignored !!!");
                            break;
                    }
                } else {
                    CallStateMachine callStateMachine = this.mCsm;
                    callStateMachine.transitionTo(callStateMachine.mInCall);
                }
            } else {
                Log.i(this.LOG_TAG, "[ModifyingCall] Resume video defered");
                CallStateMachine callStateMachine2 = this.mCsm;
                callStateMachine2.isDeferedVideoResume = true;
                callStateMachine2.deferMessage(message);
            }
            return true;
        }
        return false;
    }

    private void idcModified_ModifyingCall(Message message) {
        Log.i(this.LOG_TAG, "[IDC] idcModified_ModifyingCall");
        if (this.mSession.getIdcData() != null) {
            if (this.mSession.getIdcData().getCurrentState() != IdcImsCallSessionData.State.MODIFYING) {
                Log.i(this.LOG_TAG, "[IDC] Idc State isn't MODIFYING");
                return;
            }
            this.mModule.getIdcServiceHelper().receiveSdpAnswer(this.mSession.getSessionId(), (IdcExtra) message.obj);
            this.mSession.getIdcData().transitState(IdcImsCallSessionData.State.NEGOTIATED);
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mInCall);
        }
    }

    private void idcError_ModifyingCall(Message message) {
        Log.i(this.LOG_TAG, "[IDC] idcError_ModifyingCall");
        if (this.mSession.getIdcData() != null) {
            if (this.mSession.getIdcData().getCurrentState() != IdcImsCallSessionData.State.MODIFYING) {
                Log.i(this.LOG_TAG, "[IDC] Idc State isn't MODIFYING");
                return;
            }
            this.mModule.getIdcServiceHelper().notifyErrorToSdpOffer(this.mSession, (IdcExtra) message.obj);
            this.mSession.getIdcData().transitState(IdcImsCallSessionData.State.NEGOTIATED);
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mInCall);
        }
    }

    private void modified_ModifyingCall(Message message) {
        int i = message.arg1;
        int i2 = message.arg2;
        Log.i(this.LOG_TAG, "modifiedCallType " + i + ", orgCallType " + i2);
        boolean z = false;
        if (i == i2) {
            Log.e(this.LOG_TAG, "Modify requested but callType hasn't changed");
            if (!ImsCallUtil.isRttCall(this.mSession.mModifyRequestedProfile.getCallType())) {
                this.mCsm.notifyOnError(1110, "Call switch rejected");
            } else {
                this.mModule.onSendRttSessionModifyResponse(this.mSession.getCallId(), !ImsCallUtil.isRttCall(i2), false);
            }
            if (this.mSession.mModifyRequestedProfile.getCallType() == 3) {
                ImsCallSession imsCallSession = this.mSession;
                if (imsCallSession.mPrevUsedCamera == -1 && imsCallSession.mLastUsedCamera == 0) {
                    imsCallSession.mLastUsedCamera = -1;
                }
            }
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mInCall);
            return;
        }
        if (i == 9 || ImsCallUtil.isRttCall(i)) {
            CallStateMachine callStateMachine2 = this.mCsm;
            if (!callStateMachine2.isRequestTtyFull) {
                Log.i(this.LOG_TAG, "TTY/RTT FULL defered");
                this.mCsm.deferMessage(message);
                return;
            }
            callStateMachine2.isRequestTtyFull = false;
        }
        if (i != i2 && (ImsCallUtil.isRttCall(i) || ImsCallUtil.isRttCall(i2))) {
            IVolteServiceModuleInternal iVolteServiceModuleInternal = this.mModule;
            int callId = this.mSession.getCallId();
            if (!ImsCallUtil.isRttCall(i2) && ImsCallUtil.isRttCall(i)) {
                z = true;
            }
            iVolteServiceModuleInternal.onSendRttSessionModifyResponse(callId, z, true);
        }
        CallProfile callProfile = new CallProfile();
        callProfile.setCallType(i);
        this.mCsm.onCallModified(callProfile);
        CallStateMachine callStateMachine3 = this.mCsm;
        callStateMachine3.transitionTo(callStateMachine3.mInCall);
    }

    private void switchRequest_ModifyingCall(Message message) {
        CallProfile callProfile = this.mSession.mModifyRequestedProfile;
        if (callProfile == null || callProfile.getCallType() != message.arg1) {
            Log.i(this.LOG_TAG, "[ModifyingCall] Rejecting switch request while processing modify");
            if (this.mCsm.rejectModifyCallType(491) < 0) {
                this.mCsm.sendMessage(4, 0, -1, new SipError(1006, ""));
                return;
            }
            return;
        }
        this.mCsm.modifyCallType(this.mSession.mModifyRequestedProfile, false);
        Log.i(this.LOG_TAG, "[ModifyingCall] accept a call modification in progress of resolving race condition");
    }

    private void error_ModifyingCall(Message message) {
        SipError sipError = (SipError) message.obj;
        if (sipError.getCode() >= 5000) {
            Log.i(this.LOG_TAG, "[ModifyingCall] big data code over 5000 means call ended");
            this.mCsm.notifyOnError(1109, "Call switch failed");
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mEndingCall);
            this.mCsm.sendMessage(3);
            return;
        }
        if (sipError.getCode() == 603) {
            this.mCsm.notifyOnError(1110, "Call switch rejected");
        } else {
            this.mCsm.notifyOnError(1109, "Call switch failed");
        }
        CallStateMachine callStateMachine2 = this.mCsm;
        callStateMachine2.transitionTo(callStateMachine2.mInCall);
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
        this.mSession.mModifyRequestedProfile = null;
    }
}
