package com.sec.internal.ims.servicemodules.volte2;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;

public class ImsAlertingCall extends CallState {
    ImsAlertingCall(CallStateMachine callStateMachine) {
        super(callStateMachine);
    }

    public void enter() {
        KeepAliveSender keepAliveSender;
        this.mCsm.resetCallTypeAndErrorFlags();
        Log.i(this.LOG_TAG, "Enter [AlertingCall]");
        if (this.mMno == Mno.CMCC) {
            this.mSession.getCallProfile().setVCrtIsAlerting(true);
        }
        if ((this.mMno.isChn() || this.mMno.isOneOf(Mno.VIVA_BAHRAIN, Mno.ETISALAT_UAE)) && (keepAliveSender = this.mSession.mKaSender) != null) {
            keepAliveSender.start();
        }
        this.mCsm.isStartedCamera(this.mSession.getCallProfile().getCallType(), false);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0107, code lost:
        if (dbrLost_AlertingCall() != false) goto L_0x016f;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean processMessage(android.os.Message r10) {
        /*
            r9 = this;
            java.lang.String r0 = r9.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = " [AlertingCall] processMessage "
            r1.append(r2)
            int r2 = r10.what
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            int r0 = r10.what
            r1 = 0
            r2 = 1
            if (r0 == r2) goto L_0x0170
            r3 = 26
            if (r0 == r3) goto L_0x015f
            r3 = 52
            if (r0 == r3) goto L_0x015b
            r3 = 56
            if (r0 == r3) goto L_0x012f
            r3 = 64
            if (r0 == r3) goto L_0x0129
            r3 = 100
            if (r0 == r3) goto L_0x0170
            r3 = 204(0xcc, float:2.86E-43)
            if (r0 == r3) goto L_0x0117
            r3 = 400(0x190, float:5.6E-43)
            if (r0 == r3) goto L_0x0170
            r3 = 502(0x1f6, float:7.03E-43)
            if (r0 == r3) goto L_0x010a
            r3 = 5000(0x1388, float:7.006E-42)
            if (r0 == r3) goto L_0x0103
            r3 = 3
            if (r0 == r3) goto L_0x0170
            r4 = 4
            if (r0 == r4) goto L_0x00fe
            r4 = 41
            if (r0 == r4) goto L_0x00fa
            r4 = 42
            if (r0 == r4) goto L_0x00ec
            r3 = 93
            if (r0 == r3) goto L_0x0170
            r3 = 94
            if (r0 == r3) goto L_0x0170
            r1 = 209(0xd1, float:2.93E-43)
            if (r0 == r1) goto L_0x00de
            r1 = 210(0xd2, float:2.94E-43)
            if (r0 == r1) goto L_0x00de
            switch(r0) {
                case 31: goto L_0x00d4;
                case 32: goto L_0x00cf;
                case 33: goto L_0x00c6;
                case 34: goto L_0x00c1;
                case 35: goto L_0x0098;
                case 36: goto L_0x008e;
                default: goto L_0x0063;
            }
        L_0x0063:
            java.lang.String r0 = r9.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "["
            r1.append(r3)
            java.lang.String r9 = r9.getName()
            r1.append(r9)
            java.lang.String r9 = "] msg:"
            r1.append(r9)
            int r9 = r10.what
            r1.append(r9)
            java.lang.String r9 = " ignored !!!"
            r1.append(r9)
            java.lang.String r9 = r1.toString()
            android.util.Log.e(r0, r9)
            goto L_0x016f
        L_0x008e:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r10 = r9.mSession
            r10.setForwarded(r2)
            r9.forwarded_AlertingCall()
            goto L_0x016f
        L_0x0098:
            com.sec.internal.constants.Mno r10 = r9.mMno
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.TMOUS
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.DISH
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.CMCC
            com.sec.internal.constants.Mno[] r0 = new com.sec.internal.constants.Mno[]{r0, r1, r3}
            boolean r10 = r10.isOneOf(r0)
            if (r10 != 0) goto L_0x00b2
            com.sec.internal.constants.Mno r10 = r9.mMno
            boolean r10 = r10.isTmobile()
            if (r10 == 0) goto L_0x016f
        L_0x00b2:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r10 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r10 = r10.getCallProfile()
            int r10 = r10.getAudioEarlyMediaDir()
            r9.sessionProgress_AlertingCall(r10)
            goto L_0x016f
        L_0x00c1:
            r9.ringingBack_AlertingCall()
            goto L_0x016f
        L_0x00c6:
            java.lang.String r9 = r9.LOG_TAG
            java.lang.String r10 = "Ignore."
            android.util.Log.i(r9, r10)
            goto L_0x016f
        L_0x00cf:
            r9.earlymedia_AlertingCall(r10)
            goto L_0x016f
        L_0x00d4:
            java.lang.String r9 = r9.LOG_TAG
            java.lang.String r10 = "response from network by re-invite. do nothing."
            android.util.Log.i(r9, r10)
            goto L_0x016f
        L_0x00de:
            java.lang.String r0 = r9.LOG_TAG
            java.lang.String r1 = "[AlertingCall] deferMessage Downgrade Rtt to voice call"
            android.util.Log.i(r0, r1)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r9 = r9.mCsm
            r9.deferMessage(r10)
            goto L_0x016f
        L_0x00ec:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r10 = r9.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsEndingCall r0 = r10.mEndingCall
            r10.transitionTo(r0)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r9 = r9.mCsm
            r9.sendMessage((int) r3)
            goto L_0x016f
        L_0x00fa:
            r9.established_AlertingCall()
            goto L_0x016f
        L_0x00fe:
            boolean r9 = r9.error_AlertingCall(r10)
            return r9
        L_0x0103:
            boolean r10 = r9.dbrLost_AlertingCall()
            if (r10 == 0) goto L_0x0170
            goto L_0x016f
        L_0x010a:
            java.lang.String r0 = r9.LOG_TAG
            java.lang.String r1 = "[AlertingCall] Re-INVITE defered"
            android.util.Log.i(r0, r1)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r9 = r9.mCsm
            r9.deferMessage(r10)
            goto L_0x016f
        L_0x0117:
            java.lang.String r10 = r9.LOG_TAG
            java.lang.String r0 = "ringback timer expired."
            android.util.Log.i(r10, r0)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r9 = r9.mCsm
            r10 = 1802(0x70a, float:2.525E-42)
            java.lang.String r0 = "Ringback timer expired"
            r9.sendMessage(r2, r10, r1, r0)
            goto L_0x016f
        L_0x0129:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r9 = r9.mCsm
            r9.sendRTTtext(r10)
            goto L_0x016f
        L_0x012f:
            java.lang.Object r10 = r10.obj
            android.os.Bundle r10 = (android.os.Bundle) r10
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r3 = r9.mVolteSvcIntf
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r9 = r9.mSession
            int r4 = r9.getSessionId()
            java.lang.String r9 = "code"
            int r5 = r10.getInt(r9)
            java.lang.String r9 = "mode"
            int r6 = r10.getInt(r9)
            java.lang.String r9 = "operation"
            int r7 = r10.getInt(r9)
            java.lang.String r9 = "result"
            android.os.Parcelable r9 = r10.getParcelable(r9)
            r8 = r9
            android.os.Message r8 = (android.os.Message) r8
            r3.handleDtmf(r4, r5, r6, r7, r8)
            goto L_0x016f
        L_0x015b:
            r9.update_AlertingCall(r10)
            goto L_0x016f
        L_0x015f:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r10 = r9.mCsm
            r0 = 503(0x1f7, float:7.05E-43)
            java.lang.String r3 = "Session Progress Timeout"
            r10.notifyOnError(r0, r3, r1)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r9 = r9.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsEndingCall r10 = r9.mEndingCall
            r9.transitionTo(r10)
        L_0x016f:
            return r2
        L_0x0170:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r10 = r9.mCsm
            boolean r0 = r10.mIsMdmiEnabled
            if (r0 == 0) goto L_0x0189
            int r10 = r10.callType
            boolean r10 = com.sec.internal.helper.ImsCallUtil.isE911Call(r10)
            if (r10 == 0) goto L_0x0189
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r9 = r9.mCsm
            com.sec.internal.ims.mdmi.MdmiE911Listener r9 = r9.mMdmiE911Listener
            com.sec.internal.ims.mdmi.MdmiServiceModule$msgType r10 = com.sec.internal.ims.mdmi.MdmiServiceModule.msgType.SIP_CANCEL
            r2 = 0
            r9.notifySipMsg(r10, r2)
        L_0x0189:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsAlertingCall.processMessage(android.os.Message):boolean");
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
        KeepAliveSender keepAliveSender = this.mSession.mKaSender;
        if (keepAliveSender != null) {
            keepAliveSender.stop();
        }
        this.mCsm.stopRingTimer();
        if (this.mMno == Mno.CMCC) {
            this.mSession.getCallProfile().setVCrtIsAlerting(false);
        }
    }

    private void ringingBack_AlertingCall() {
        boolean z = this.mMno == Mno.RAKUTEN_JAPAN && this.mCsm.hasMessages(204);
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration != null && !z) {
            this.mCsm.startRingTimer(((long) imsRegistration.getImsProfile().getRingbackTimer()) * 1000);
        }
        this.mCsm.notifyOnRingingBack();
    }

    /* access modifiers changed from: protected */
    public void sessionProgress_AlertingCall(int i) {
        int beginBroadcast = this.mListeners.beginBroadcast();
        for (int i2 = 0; i2 < beginBroadcast; i2++) {
            try {
                this.mListeners.getBroadcastItem(i2).onSessionProgress(i);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    private void established_AlertingCall() {
        IRegistrationGovernor registrationGovernor;
        int i;
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.transitionTo(callStateMachine.mInCall);
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration != null && (registrationGovernor = this.mRegistrationManager.getRegistrationGovernor(imsRegistration.getHandle())) != null) {
            IRegistrationGovernor.CallEvent callEvent = IRegistrationGovernor.CallEvent.EVENT_CALL_ESTABLISHED;
            if (this.mSession.getCallProfile().isDowngradedVideoCall()) {
                i = 2;
            } else {
                i = this.mSession.getCallProfile().getCallType();
            }
            registrationGovernor.onCallStatus(callEvent, (SipError) null, i);
        }
    }

    private boolean dbrLost_AlertingCall() {
        Mno mno = this.mMno;
        if (mno != Mno.VIVA_KUWAIT && mno != Mno.TELEFONICA_GERMANY && mno != Mno.ETISALAT_UAE && mno != Mno.TELE2_SWE) {
            return false;
        }
        Log.e(this.LOG_TAG, "[AlertingCall] processMessage DBR LOST ignored!");
        return true;
    }

    private boolean error_AlertingCall(Message message) {
        this.mCsm.handleSPRoutgoingError(message);
        CallStateMachine callStateMachine = this.mCsm;
        if (!callStateMachine.mIsWPSCall) {
            return false;
        }
        callStateMachine.sendMessageDelayed(26, (long) UtStateMachine.HTTP_READ_TIMEOUT_GCF);
        return true;
    }

    private void earlymedia_AlertingCall(Message message) {
        String str = this.LOG_TAG;
        Log.i(str, "mSession.getCallProfile().isVideoCRBT: " + this.mSession.getCallProfile().isVideoCRBT());
        if (this.mRegistration != null && this.mSession.getCallProfile().isVideoCRBT()) {
            this.mVolteSvcIntf.startVideoEarlyMedia(this.mSession.getSessionId());
        }
        this.mCsm.notifyOnEarlyMediaStarted(message.arg1);
    }

    private void update_AlertingCall(Message message) {
        Bundle bundle = (Bundle) message.obj;
        CallProfile parcelable = bundle.getParcelable("profile");
        int srvccVersion = this.mModule.getSrvccVersion(this.mSession.getPhoneId());
        if (parcelable == null && srvccVersion != 0) {
            if (srvccVersion >= 10 || DeviceUtil.getGcfMode()) {
                Log.i(this.LOG_TAG, "MO aSRVCC supported");
                this.mVolteSvcIntf.sendReInvite(this.mSession.getSessionId(), new SipReason("SIP", bundle.getInt("cause"), bundle.getString("reasonText"), new String[0]));
            }
        }
    }

    private void forwarded_AlertingCall() {
        this.mCsm.stopRingTimer();
        if (!this.mMno.isKor()) {
            this.mCsm.notifyOnCallForwarded();
        }
    }
}
