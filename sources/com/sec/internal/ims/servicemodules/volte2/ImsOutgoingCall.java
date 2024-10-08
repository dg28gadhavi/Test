package com.sec.internal.ims.servicemodules.volte2;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.os.SystemClock;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.mdmi.MdmiServiceModule;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;

public class ImsOutgoingCall extends CallState {
    ImsOutgoingCall(CallStateMachine callStateMachine) {
        super(callStateMachine);
    }

    public void enter() {
        Mno mno;
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.errorCode = -1;
        callStateMachine.errorMessage = "";
        callStateMachine.mTryingReceived = false;
        callStateMachine.callType = this.mSession.getCallProfile().getCallType();
        startTimer_OutgoingCall();
        start100Timer_OutgoingCall();
        Log.i(this.LOG_TAG, "Enter [OutgoingCall]");
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration != null && imsRegistration.getImsProfile() != null) {
            if (!(this.mRegistration.getImsProfile().getUsePrecondition() != 0) || ((mno = this.mMno) != Mno.ATT && !mno.isOneOf(Mno.TMOUS, Mno.DISH))) {
                CallStateMachine callStateMachine2 = this.mCsm;
                int determineCamera = callStateMachine2.determineCamera(callStateMachine2.callType, false);
                if (determineCamera >= 0) {
                    this.mSession.startCamera(determineCamera);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:64:0x017f, code lost:
        if (terminate_OutgoingCall(r6) != false) goto L_0x0188;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0186, code lost:
        if (dbrLost_OutgoingCall(r6) != false) goto L_0x0188;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean processMessage(android.os.Message r6) {
        /*
            r5 = this;
            java.lang.String r0 = r5.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "[OutgoingCall] processMessage "
            r1.append(r2)
            int r2 = r6.what
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            int r0 = r6.what
            r1 = 1
            if (r0 == r1) goto L_0x017b
            r2 = 26
            r3 = 0
            if (r0 == r2) goto L_0x016a
            r2 = 41
            if (r0 == r2) goto L_0x0162
            r2 = 52
            if (r0 == r2) goto L_0x015e
            r2 = 203(0xcb, float:2.84E-43)
            if (r0 == r2) goto L_0x015a
            r2 = 301(0x12d, float:4.22E-43)
            if (r0 == r2) goto L_0x0156
            r2 = 303(0x12f, float:4.25E-43)
            if (r0 == r2) goto L_0x0189
            r2 = 502(0x1f6, float:7.03E-43)
            if (r0 == r2) goto L_0x0145
            r2 = 1000(0x3e8, float:1.401E-42)
            if (r0 == r2) goto L_0x0141
            r2 = 5000(0x1388, float:7.006E-42)
            if (r0 == r2) goto L_0x0182
            r2 = 3
            if (r0 == r2) goto L_0x0189
            r2 = 4
            if (r0 == r2) goto L_0x013c
            r2 = 93
            if (r0 == r2) goto L_0x0189
            r2 = 94
            if (r0 == r2) goto L_0x0189
            r2 = 100
            if (r0 == r2) goto L_0x0189
            r2 = 101(0x65, float:1.42E-43)
            if (r0 == r2) goto L_0x00dc
            r2 = 306(0x132, float:4.29E-43)
            if (r0 == r2) goto L_0x0189
            r2 = 307(0x133, float:4.3E-43)
            if (r0 == r2) goto L_0x0189
            r2 = 400(0x190, float:5.6E-43)
            if (r0 == r2) goto L_0x00db
            r2 = 401(0x191, float:5.62E-43)
            if (r0 == r2) goto L_0x00d6
            switch(r0) {
                case 31: goto L_0x00d1;
                case 32: goto L_0x00cc;
                case 33: goto L_0x00c7;
                case 34: goto L_0x00c2;
                case 35: goto L_0x00bd;
                case 36: goto L_0x00ac;
                default: goto L_0x006b;
            }
        L_0x006b:
            switch(r0) {
                case 208: goto L_0x00a7;
                case 209: goto L_0x0099;
                case 210: goto L_0x0099;
                default: goto L_0x006e;
            }
        L_0x006e:
            java.lang.String r0 = r5.LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "["
            r2.append(r3)
            java.lang.String r5 = r5.getName()
            r2.append(r5)
            java.lang.String r5 = "] msg:"
            r2.append(r5)
            int r5 = r6.what
            r2.append(r5)
            java.lang.String r5 = " ignored !!!"
            r2.append(r5)
            java.lang.String r5 = r2.toString()
            android.util.Log.e(r0, r5)
            goto L_0x0188
        L_0x0099:
            java.lang.String r0 = r5.LOG_TAG
            java.lang.String r2 = "[OutgoingCall] deferMessage Downgrade Rtt to voice call"
            android.util.Log.i(r0, r2)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r5 = r5.mCsm
            r5.deferMessage(r6)
            goto L_0x0188
        L_0x00a7:
            r5.tryingTimeout_OutgoingCall()
            goto L_0x0188
        L_0x00ac:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r6 = r5.mSession
            r6.setForwarded(r1)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r6 = r5.mCsm
            r6.stopRingTimer()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r5 = r5.mCsm
            r5.notifyOnCallForwarded()
            goto L_0x0188
        L_0x00bd:
            r5.sessionProgress_OutgoingCall()
            goto L_0x0188
        L_0x00c2:
            r5.ringingBack_OutgoingCall()
            goto L_0x0188
        L_0x00c7:
            r5.notifyOnCalling()
            goto L_0x0188
        L_0x00cc:
            r5.earlymedia_OutgoingCall(r6)
            goto L_0x0188
        L_0x00d1:
            r5.tyring_OutgoingCall()
            goto L_0x0188
        L_0x00d6:
            r5.rrcReleased_OutgoingCall()
            goto L_0x0188
        L_0x00db:
            return r3
        L_0x00dc:
            java.lang.String r0 = r5.LOG_TAG
            java.lang.String r2 = "[OutgoingCall] sendInfo"
            android.util.Log.i(r0, r2)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r5.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r2 = r5.mSession
            com.sec.ims.volte2.data.CallProfile r2 = r2.getCallProfile()
            int r2 = r2.getCallType()
            r0.callType = r2
            java.lang.Object r6 = r6.obj
            android.os.Bundle r6 = (android.os.Bundle) r6
            java.lang.String r0 = "info"
            java.lang.String r0 = r6.getString(r0)
            java.lang.String r2 = "type"
            int r6 = r6.getInt(r2)
            java.lang.String r2 = r5.LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "info callType= %d"
            r3.append(r4)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r4 = r5.mCsm
            int r4 = r4.callType
            r3.append(r4)
            java.lang.String r4 = ", request=%s"
            r3.append(r4)
            r3.append(r0)
            java.lang.String r4 = ", ussdType=%d"
            r3.append(r4)
            r3.append(r6)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r2, r3)
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r2 = r5.mVolteSvcIntf
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r3 = r5.mSession
            int r3 = r3.getSessionId()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r5 = r5.mCsm
            int r5 = r5.callType
            r2.sendInfo(r3, r5, r0, r6)
            goto L_0x0188
        L_0x013c:
            boolean r5 = r5.error_OutgoingCall(r6)
            return r5
        L_0x0141:
            r5.epsFallbackResult_OutgoingCall(r6)
            goto L_0x0188
        L_0x0145:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r5.mCsm
            r0.mReinvite = r1
            java.lang.String r0 = r5.LOG_TAG
            java.lang.String r2 = "[OutgoingCall] Re-INVITE defered"
            android.util.Log.i(r0, r2)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r5 = r5.mCsm
            r5.deferMessage(r6)
            goto L_0x0188
        L_0x0156:
            r5.timerVZWExpired_OutgoingCall()
            goto L_0x0188
        L_0x015a:
            r5.sessionProgressTimeout_OutgoingCall()
            goto L_0x0188
        L_0x015e:
            r5.update_OutgoingCall(r6)
            goto L_0x0188
        L_0x0162:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r5 = r5.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsInCall r6 = r5.mInCall
            r5.transitionTo(r6)
            goto L_0x0188
        L_0x016a:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r6 = r5.mCsm
            r0 = 503(0x1f7, float:7.05E-43)
            java.lang.String r2 = "Session Progress Timeout"
            r6.notifyOnError(r0, r2, r3)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r5 = r5.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsEndingCall r6 = r5.mEndingCall
            r5.transitionTo(r6)
            goto L_0x0188
        L_0x017b:
            boolean r0 = r5.terminate_OutgoingCall(r6)
            if (r0 == 0) goto L_0x0182
            goto L_0x0188
        L_0x0182:
            boolean r0 = r5.dbrLost_OutgoingCall(r6)
            if (r0 == 0) goto L_0x0189
        L_0x0188:
            return r1
        L_0x0189:
            boolean r5 = r5.endOrFail_OutgoingCall(r6)
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsOutgoingCall.processMessage(android.os.Message):boolean");
    }

    public void exit() {
        this.mCsm.removeMessages(203);
        this.mCsm.removeMessages(208);
        this.mCsm.removeMessages(301);
        this.mCsm.setPreviousState(this);
    }

    private void startTimer_OutgoingCall() {
        String dialingNumber = this.mSession.getCallProfile().getDialingNumber();
        if (isNeedToStartVZWTimer()) {
            String str = this.LOG_TAG;
            Log.i(str, "[OutgoingCall] start Timer_VZW " + getTimerVzw() + " msec.");
            this.mCsm.sendMessageDelayed(301, (long) getTimerVzw());
            return;
        }
        Mno mno = this.mMno;
        if (mno == Mno.KDDI && this.mCsm.callType != 12) {
            Log.i(this.LOG_TAG, "[OutgoingCall] Start Session Progress Timer (10 sec) + (300ms) to avoid conflict with Timer B");
            this.mCsm.sendMessageDelayed(203, 10300);
        } else if (mno == Mno.ELISA_EE && this.mCsm.callType != 12) {
            Log.i(this.LOG_TAG, "[OutgoingCall] Start Session Progress Timer (15 sec).");
            this.mCsm.sendMessageDelayed(203, 15000);
        } else if (mno == Mno.ATT) {
            handleStartATTTimer(dialingNumber);
        } else if ((mno == Mno.EE || mno == Mno.EE_ESN || mno == Mno.BTOP) && this.mCsm.callType != 12) {
            Log.i(this.LOG_TAG, "[OutgoingCall] Start EE-UK Session Progress Timer (20 sec).");
            this.mCsm.sendMessageDelayed(203, 19500);
        } else if (ImsCallUtil.isCmcSecondaryType(this.mSession.getCmcType())) {
            Log.i(this.LOG_TAG, "[OutgoingCall] Start Session Progress Timer for SD (12 sec).");
            this.mCsm.sendMessageDelayed(203, 12000);
        }
    }

    private int getTimerVzw() {
        return DmConfigHelper.readInt(this.mContext, "timer_vzw", 6, this.mSession.getPhoneId()).intValue() * 1000;
    }

    private void start100Timer_OutgoingCall() {
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration != null && imsRegistration.getImsProfile() != null && this.mRegistration.getImsProfile().get100tryingTimer() > 0 && this.mCsm.callType != 12) {
            int i = this.mRegistration.getImsProfile().get100tryingTimer();
            if (!ImsCallUtil.isE911Call(this.mCsm.callType)) {
                if (this.mMno == Mno.USCC && this.mModule.getSessionCount(this.mSession.getPhoneId()) == 1) {
                    Log.i(this.LOG_TAG, "[OutgoingCall] USCC G30 Timer (12 sec)");
                    this.mCsm.sendMessageDelayed(208, 12000);
                } else if (this.mMno != Mno.SFR || this.mSession.isEpdgCall()) {
                    String str = this.LOG_TAG;
                    Log.i(str, "[OutgoingCall] Start 100 Trying Timer (" + i + " msec).");
                    this.mCsm.sendMessageDelayed(208, (long) i);
                } else {
                    Log.i(this.LOG_TAG, "[OutgoingCall] Skip 100 Trying Timer ()");
                }
            } else if (this.mMno.isOneOf(Mno.TMOUS, Mno.DISH)) {
                Log.i(this.LOG_TAG, "[OutgoingCall] Start 100 Trying Timer (5000 msec).");
                this.mCsm.sendMessageDelayed(208, 5000);
            }
        }
    }

    private void tyring_OutgoingCall() {
        ImsRegistration imsRegistration;
        KeepAliveSender keepAliveSender;
        this.mCsm.mTryingReceived = true;
        notifyOnTrying();
        if (!this.mMno.isChn() && !this.mMno.isOneOf(Mno.VIVA_BAHRAIN, Mno.ETISALAT_UAE) && (keepAliveSender = this.mSession.mKaSender) != null) {
            keepAliveSender.start();
        }
        this.mCsm.removeMessages(208);
        this.mCsm.removeMessages(301);
        Mno mno = this.mMno;
        if (mno == Mno.VZW || mno == Mno.RJIL) {
            this.mCsm.sendMessageDelayed(203, 180000);
        }
        Mno mno2 = this.mMno;
        if ((mno2 == Mno.CTC || mno2 == Mno.CTCMO) && (imsRegistration = this.mRegistration) != null && imsRegistration.getImsProfile() != null) {
            this.mCsm.sendMessageDelayed(203, (long) this.mRegistration.getImsProfile().getTimerB());
        }
    }

    private void earlymedia_OutgoingCall(Message message) {
        ImsRegistration imsRegistration;
        int ringbackTimer;
        String str = this.LOG_TAG;
        Log.i(str, "mSession.getCallProfile().isVideoCRBT: " + this.mSession.getCallProfile().isVideoCRBT());
        if (this.mRegistration != null && this.mSession.getCallProfile().isVideoCRBT()) {
            this.mVolteSvcIntf.startVideoEarlyMedia(this.mSession.getSessionId());
        }
        if (this.mMno == Mno.RAKUTEN_JAPAN && (imsRegistration = this.mRegistration) != null && (ringbackTimer = imsRegistration.getImsProfile().getRingbackTimer()) > 0) {
            this.mCsm.startRingTimer(((long) ringbackTimer) * 1000);
        }
        this.mCsm.notifyOnEarlyMediaStarted(message.arg1);
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.transitionTo(callStateMachine.mAlertingCall);
    }

    private void ringingBack_OutgoingCall() {
        int ringbackTimer;
        this.mCsm.notifyOnRingingBack();
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.transitionTo(callStateMachine.mAlertingCall);
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration != null && (ringbackTimer = imsRegistration.getImsProfile().getRingbackTimer()) > 0) {
            this.mCsm.startRingTimer(((long) ringbackTimer) * 1000);
        }
        this.mCsm.mCallRingingTime = SystemClock.elapsedRealtime();
    }

    private void sessionProgress_OutgoingCall() {
        this.mCsm.removeMessages(203);
        this.mCsm.removeMessages(208);
        this.mCsm.removeMessages(301);
        KeepAliveSender keepAliveSender = this.mSession.mKaSender;
        if (keepAliveSender != null) {
            keepAliveSender.stop();
        }
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.isStartedCamera(callStateMachine.callType, false);
        if (this.mMno == Mno.CMCC) {
            int beginBroadcast = this.mListeners.beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                try {
                    this.mListeners.getBroadcastItem(i).onSessionProgress(this.mSession.getCallProfile().getAudioEarlyMediaDir());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mListeners.finishBroadcast();
        }
    }

    private boolean terminate_OutgoingCall(Message message) {
        CallStateMachine callStateMachine = this.mCsm;
        if (!callStateMachine.mTryingReceived && message.arg1 == 4 && !callStateMachine.srvccStarted) {
            Log.i(this.LOG_TAG, "Network Handover on dialing (before get 100 TRYING)");
            if (this.mMno != Mno.VZW || !ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
                this.mCsm.notifyOnEnded(1117);
            } else {
                this.mCsm.notifyOnEnded(1107);
            }
        }
        boolean z = this.mCsm.mTryingReceived;
        if (z || !(message.arg1 == 14 || message.arg2 == 3)) {
            if (!z && message.arg1 == 13) {
                Log.i(this.LOG_TAG, "PS Barred. notify error call barred by network!");
                this.mCsm.notifyOnError(2801, "ps Barred");
                CallStateMachine callStateMachine2 = this.mCsm;
                callStateMachine2.transitionTo(callStateMachine2.mEndingCall);
            }
            if (message.arg1 == 23) {
                Log.i(this.LOG_TAG, "RRC Reject. notify error rrc reject by network!");
                this.mCsm.notifyOnError(2801, "rrc reject");
                CallStateMachine callStateMachine3 = this.mCsm;
                callStateMachine3.transitionTo(callStateMachine3.mEndingCall);
            }
            if (this.mMno == Mno.VZW && message.arg1 == 28) {
                Log.i(this.LOG_TAG, "INVITE SIP Timeout!");
                this.mCsm.notifyOnError(NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_SUCCESS_CODE, "timer vzw expired");
                CallStateMachine callStateMachine4 = this.mCsm;
                callStateMachine4.transitionTo(callStateMachine4.mEndingCall);
            }
            Mno mno = this.mMno;
            if ((mno != Mno.KDDI && mno != Mno.DOCOMO) || message.arg1 != 25) {
                return false;
            }
            Log.i(this.LOG_TAG, "on terminate out of service.");
            this.mCsm.notifyOnError(1114, "CALL_INVITE_TIMEOUT");
            CallStateMachine callStateMachine5 = this.mCsm;
            callStateMachine5.transitionTo(callStateMachine5.mEndingCall);
            return true;
        }
        Log.i(this.LOG_TAG, "Deregistered. notify error 1701 for CSFB");
        this.mCsm.notifyOnError(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_DEVICE_STATUS, "deregistered");
        CallStateMachine callStateMachine6 = this.mCsm;
        callStateMachine6.transitionTo(callStateMachine6.mEndingCall);
        return true;
    }

    private boolean dbrLost_OutgoingCall(Message message) {
        if (this.mMno != Mno.PLAY || message.what != 5000 || this.mSession.getDedicatedBearerState(message.arg1) == 3) {
            return false;
        }
        Log.i(this.LOG_TAG, "dedicated bearer was re-established, the call is not terminated");
        return true;
    }

    private boolean endOrFail_OutgoingCall(Message message) {
        ImsRegistration imsRegistration;
        CallStateMachine callStateMachine = this.mCsm;
        if (callStateMachine.mIsMdmiEnabled && ImsCallUtil.isE911Call(callStateMachine.callType)) {
            this.mCsm.mMdmiE911Listener.notifySipMsg(MdmiServiceModule.msgType.SIP_CANCEL, 0);
        }
        if ((this.mMno.isOneOf(Mno.TMOUS, Mno.DISH) || this.mMno == Mno.SPRINT) && message.what == 100) {
            Log.i(this.LOG_TAG, "[OutgoingCall] Skip FORCE_NOTIFY_CURRENT_CODEC");
            return true;
        } else if (this.mCsm.mNeedToWaitEndcall) {
            Log.i(this.LOG_TAG, "[OutgoingCall] need to Wait Endcall");
            this.mCsm.mNeedToWaitEndcall = false;
            return true;
        } else {
            String str = this.LOG_TAG;
            Log.i(str, "[OutgoingCall] endOrFail_OutgoingCall, what=" + message.what + ", getIsLteRetrying=" + this.mCsm.mModule.getIsLteRetrying(this.mSession.getPhoneId()) + ",mSession.getErrorMessage()=" + this.mSession.getErrorMessage());
            if (this.mMno != Mno.VZW || message.what != 1 || !this.mCsm.mModule.getIsLteRetrying(this.mSession.getPhoneId()) || (imsRegistration = this.mRegistration) == null || imsRegistration.getRegiRat() != 20 || this.mCsm.sipReason == null || !"TIMER VZW EXPIRED".equals(this.mSession.getErrorMessage())) {
                return false;
            }
            Log.i(this.LOG_TAG, "[OutgoingCall] timerVZWExpired_OutgoingCall ==> Deregister on NR");
            IRegistrationGovernor registrationGovernor = this.mRegistrationManager.getRegistrationGovernor(this.mRegistration.getHandle());
            if (registrationGovernor != null) {
                registrationGovernor.onSipError(ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType()) ? "mmtel-video" : "mmtel", new SipError(1125));
            }
            return true;
        }
    }

    private boolean error_OutgoingCall(Message message) {
        Log.e(this.LOG_TAG, "[OutgoingCall] on error.");
        if (!this.mCsm.handleSPRoutgoingError(message)) {
            return false;
        }
        SipError sipError = (SipError) message.obj;
        if ("LTE Retry in UAC Barred".equals(sipError.getReason())) {
            Log.e(this.LOG_TAG, "[OutgoingCall] skip error by UAC.");
            this.mCsm.notifyOnError(sipError.getCode(), sipError.getReason());
            return true;
        }
        Mno mno = this.mMno;
        if ((mno == Mno.KDDI || mno == Mno.DOCOMO) && ((SipError) message.obj).getCode() == 709) {
            Log.i(this.LOG_TAG, "on error 709.");
            this.mCsm.sendMessage(1, 25);
            return true;
        }
        String str = "";
        if (this.mMno == Mno.BELL) {
            SipError sipError2 = (SipError) message.obj;
            boolean isCsfbErrorCode = this.mModule.isCsfbErrorCode(this.mSession.getPhoneId(), this.mSession.getCallProfile(), new SipError(sipError2.getCode(), sipError2.getReason() == null ? str : sipError2.getReason()));
            String str2 = this.LOG_TAG;
            Log.e(str2, "On error delayed for 300ms, needDelayed : " + isCsfbErrorCode + " ,mOnErrorDelayed : " + this.mCsm.mOnErrorDelayed);
            CallStateMachine callStateMachine = this.mCsm;
            if (callStateMachine.mOnErrorDelayed || !isCsfbErrorCode) {
                callStateMachine.mOnErrorDelayed = false;
            } else {
                callStateMachine.mOnErrorDelayed = true;
                callStateMachine.sendMessageDelayed(Message.obtain(message), 300);
                return true;
            }
        }
        CallStateMachine callStateMachine2 = this.mCsm;
        if (callStateMachine2.mIsWPSCall) {
            callStateMachine2.sendMessageDelayed(26, (long) UtStateMachine.HTTP_READ_TIMEOUT_GCF);
            this.mModule.releaseSessionByState(this.mSession.getPhoneId(), CallConstants.STATE.HeldCall);
            return true;
        }
        if (this.mMno == Mno.CMCC) {
            Log.i(this.LOG_TAG, "[OutgoingCall] check delay!");
            SipError sipError3 = (SipError) message.obj;
            int code = sipError3.getCode();
            if (sipError3.getReason() != null) {
                str = sipError3.getReason();
            }
            boolean z = (code == 380 || code == 382) && this.mModule.isCsfbErrorCode(this.mSession.getPhoneId(), this.mSession.getCallProfile(), new SipError(code, str));
            String str3 = this.LOG_TAG;
            Log.i(str3, "needEndHeldCall : " + z + ", mOnErrorDelayed : " + this.mCsm.mOnErrorDelayed);
            if (!this.mSession.getCallProfile().isConferenceCall() && z && !this.mCsm.mOnErrorDelayed) {
                for (ImsCallSession next : this.mModule.getSessionList(this.mSession.getPhoneId())) {
                    String str4 = this.LOG_TAG;
                    Log.i(str4, "phoneId[" + this.mSession.getPhoneId() + "] session Id : " + next.getSessionId() + ", state : " + next.getCallState());
                    if (next.getSessionId() != this.mSession.getSessionId() && next.getCallState() == CallConstants.STATE.HeldCall) {
                        this.mCsm.sipReason = new SipReason("SIP", 0, "User triggered", true, new String[0]);
                        this.mVolteSvcIntf.endCall(next.getSessionId(), next.getCallProfile().getCallType(), this.mCsm.sipReason);
                        this.mCsm.mOnErrorDelayed = true;
                    }
                }
                if (this.mCsm.mOnErrorDelayed) {
                    Log.i(this.LOG_TAG, "error notify delayed!");
                    this.mCsm.sendMessageDelayed(Message.obtain(message), 200);
                    return true;
                }
            }
        }
        return false;
    }

    private void sessionProgressTimeout_OutgoingCall() {
        ImsRegistration imsRegistration;
        IRegistrationGovernor registrationGovernor;
        Log.i(this.LOG_TAG, "[OutgoingCall] SessionProgress Timeout - Call Terminate/CSFB");
        Mno mno = this.mMno;
        if (mno == Mno.VZW || mno == Mno.CTC || mno == Mno.CTCMO) {
            CallStateMachine callStateMachine = this.mCsm;
            SipError sipError = SipErrorBase.REQUEST_TIMEOUT;
            callStateMachine.notifyOnError(sipError.getCode(), sipError.getReason(), 0);
        } else if (mno == Mno.KDDI && this.mModule.getSessionCount(this.mSession.getPhoneId()) > 1) {
            this.mCsm.errorCode = 503;
        } else if (ImsCallUtil.isCmcSecondaryType(this.mSession.getCmcType())) {
            this.mCsm.notifyOnError(NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE, "REJECT_REASON_PD_UNREACHABLE", 0);
        } else {
            CallStateMachine callStateMachine2 = this.mCsm;
            if (callStateMachine2.mIsWPSCall) {
                callStateMachine2.mNeedToWaitEndcall = true;
                Log.i(this.LOG_TAG, "[OutgoingCall] CANCEL now CSFB after 2s");
                CallStateMachine callStateMachine3 = this.mCsm;
                callStateMachine3.sipReason = callStateMachine3.getSipReasonFromUserReason(17);
                IVolteServiceInterface iVolteServiceInterface = this.mVolteSvcIntf;
                int sessionId = this.mSession.getSessionId();
                CallStateMachine callStateMachine4 = this.mCsm;
                iVolteServiceInterface.endCall(sessionId, callStateMachine4.callType, callStateMachine4.sipReason);
                this.mCsm.sendMessageDelayed(26, (long) UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                this.mModule.releaseSessionByState(this.mSession.getPhoneId(), CallConstants.STATE.HeldCall);
                return;
            }
            callStateMachine2.notifyOnError(503, "Session Progress Timeout", 0);
            if (this.mMno == Mno.EE && !ImsCallUtil.isE911Call(this.mSession.mCallProfile.getCallType()) && (imsRegistration = this.mRegistration) != null && (registrationGovernor = this.mRegistrationManager.getRegistrationGovernor(imsRegistration.getHandle())) != null) {
                registrationGovernor.onSipError("mmtel", SipErrorBase.SIP_INVITE_TIMEOUT);
            }
        }
        this.mCsm.sendMessage(1, 17);
    }

    private void tryingTimeout_OutgoingCall() {
        if (this.mMno == Mno.TMOUS) {
            Log.i(this.LOG_TAG, "[OutgoingCall] TMOUS, 100 Trying Timeout");
            if (!ImsCallUtil.isE911Call(this.mSession.mCallProfile.getCallType())) {
                this.mCsm.errorCode = 28;
                return;
            } else if (!SemSystemProperties.get("ro.boot.hardware", "").contains("qcom")) {
                this.mCsm.errorCode = 503;
            }
        }
        Log.i(this.LOG_TAG, "[OutgoingCall] 100 Trying Timeout - Call Terminate/CSFB");
        this.mCsm.notifyOnError(503, "100 Trying Timeout", 0);
        if (this.mMno.isChn()) {
            Log.i(this.LOG_TAG, "Force to change END_REASON to terminate client socket with RST");
            this.mCsm.sendMessage(1, 8);
        } else {
            this.mCsm.sendMessage(1, 17);
        }
        if (this.mMno == Mno.USCC && this.mRegistration != null) {
            Log.i(this.LOG_TAG, "[OutgoingCall] USCC 12 sec 100 Trying Timer expired.");
            IRegistrationGovernor registrationGovernor = this.mRegistrationManager.getRegistrationGovernor(this.mRegistration.getHandle());
            if (registrationGovernor != null) {
                registrationGovernor.onSipError(ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType()) ? "mmtel-video" : "mmtel", new SipError(503));
            }
        }
    }

    private void epsFallbackResult_OutgoingCall(Message message) {
        int beginBroadcast = this.mListeners.beginBroadcast();
        String str = this.LOG_TAG;
        Log.i(str, "epsFallbackResult_OutgoingCall: " + message.arg1);
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onRetryingVoLteOrCsCall(message.arg1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    private void timerVZWExpired_OutgoingCall() {
        Log.i(this.LOG_TAG, "[OutgoingCall] TimerVzw expired.");
        if (ImsConstants.SystemSettings.AIRPLANE_MODE.get(this.mContext, 0) == ImsConstants.SystemSettings.AIRPLANE_MODE_ON) {
            Log.i(this.LOG_TAG, "[OutgoingCall] But AirplainModeOn, cannot fallback to 1x");
            this.mCsm.sendMessage(1, NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_SUCCESS_CODE, 0, "Timer_VZW expired");
            return;
        }
        this.mCsm.sipReason = new SipReason("", 0, "TIMER VZW EXPIRED", true, new String[0]);
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration != null) {
            if (!imsRegistration.getImsProfile().getNeedVoLteRetryInNr() || this.mRegistration.getRegiRat() != 20 || this.mModule.isRoaming(this.mSession.getPhoneId())) {
                IRegistrationGovernor registrationGovernor = this.mRegistrationManager.getRegistrationGovernor(this.mRegistration.getHandle());
                if (registrationGovernor != null) {
                    registrationGovernor.onSipError(ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType()) ? "mmtel-video" : "mmtel", new SipError(NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_SUCCESS_CODE));
                }
            } else {
                SecImsNotifier.getInstance().onTriggerEpsFallback(this.mSession.getPhoneId(), 2);
                return;
            }
        }
        this.mCsm.sendMessage(4, 0, -1, new SipError(NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_SUCCESS_CODE, "Timer_VZW expired"));
        this.mVolteSvcIntf.endCall(this.mSession.getSessionId(), this.mSession.getCallProfile().getCallType(), this.mCsm.sipReason);
    }

    private void rrcReleased_OutgoingCall() {
        IRegistrationGovernor registrationGovernor;
        Log.i(this.LOG_TAG, "[OutgoingCall] RRC connection released.");
        if (this.mMno != Mno.VZW || (!this.mTelephonyManager.isNetworkRoaming() && !ImsUtil.isCdmalessEnabled(this.mContext, this.mSession.getPhoneId()))) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_SVC_INST_ID, "RRC connection released"));
        } else {
            Log.i(this.LOG_TAG, "Socket close with NO_LINGER in case RRC Non-Depriorization Reject in MO case");
            this.mCsm.sendMessage(1, 23);
        }
        if (this.mMno == Mno.DOCOMO) {
            this.mVolteSvcIntf.deleteTcpSocket(this.mSession.getSessionId(), this.mSession.getCallProfile().getCallType());
        }
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration != null && (registrationGovernor = this.mRegistrationManager.getRegistrationGovernor(imsRegistration.getHandle())) != null) {
            registrationGovernor.onSipError(ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType()) ? "mmtel-video" : "mmtel", new SipError(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_SVC_INST_ID));
        }
    }

    private void update_OutgoingCall(Message message) {
        Bundle bundle = (Bundle) message.obj;
        CallProfile parcelable = bundle.getParcelable("profile");
        Log.i(this.LOG_TAG, "Received srvcc H/O event");
        int srvccVersion = this.mModule.getSrvccVersion(this.mSession.getPhoneId());
        if (parcelable == null && srvccVersion != 0) {
            if (srvccVersion >= 10 || DeviceUtil.getGcfMode()) {
                Log.i(this.LOG_TAG, "MO bsrvcc support");
                this.mVolteSvcIntf.sendReInvite(this.mSession.getSessionId(), new SipReason("SIP", bundle.getInt("cause"), bundle.getString("reasonText"), new String[0]));
            }
        }
    }

    private boolean isNeedToStartVZWTimer() {
        return this.mMno == Mno.VZW && !ImsCallUtil.isVideoCall(this.mCsm.callType) && !ImsRegistry.getPdnController().isEpdgConnected(this.mSession.getPhoneId()) && this.mModule.getSessionCount(this.mSession.getPhoneId()) == 1;
    }

    private void handleStartATTTimer(String str) {
        ImsRegistration imsRegistration;
        CallStateMachine callStateMachine = this.mCsm;
        if (callStateMachine.needToLogForATTGate(callStateMachine.callType)) {
            IMSLog.g("GATE", "<GATE-M>MO_VIDEO_CALL</GATE-M>");
        } else if (ImsCallUtil.isE911Call(this.mCsm.callType) && (imsRegistration = this.mRegistration) != null && imsRegistration.getImsProfile() != null && this.mRegistration.getImsProfile().isSoftphoneEnabled()) {
            this.mCsm.sendMessageDelayed(203, 12000);
        }
    }

    private void notifyOnCalling() {
        int beginBroadcast = this.mListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onCalling();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    private void notifyOnTrying() {
        int beginBroadcast = this.mListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onTrying();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }
}
