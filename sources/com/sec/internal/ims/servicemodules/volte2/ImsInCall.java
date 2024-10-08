package com.sec.internal.ims.servicemodules.volte2;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.sec.epdg.EpdgManager;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.CmcInfoEvent;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.State;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.imsdc.IdcImsCallSessionData;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.servicemodules.volte2.data.ConfCallSetupData;
import com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class ImsInCall extends CallState {
    ImsInCall(CallStateMachine callStateMachine) {
        super(callStateMachine);
    }

    public void enter() {
        this.mCsm.resetCallTypeAndErrorFlags();
        enter_InCall();
        if (!ImsCallUtil.isRttCall(this.mSession.getCallProfile().getCallType()) && this.mSession.getDedicatedBearerState(99) != 3) {
            Log.i(this.LOG_TAG, "[InCall] mRttBearerState initialzed to BEARER_STATE_CLOSED");
            this.mSession.setDedicatedBearerState(99, 3);
        }
        if (!checkVideo_InCall()) {
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.mPreAlerting = false;
            callStateMachine.mIsWPSCall = false;
            callStateMachine.mCameraUsedAtOtherApp = false;
            this.mSession.setIsEstablished(true);
            StringBuilder sb = new StringBuilder();
            CallStateMachine callStateMachine2 = this.mCsm;
            sb.append(callStateMachine2.mCallTypeHistory);
            sb.append(",");
            sb.append(this.mSession.getCallProfile().getCallType());
            callStateMachine2.mCallTypeHistory = sb.toString();
            Log.i(this.LOG_TAG, "Enter [InCall]");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:56:0x01d5, code lost:
        if (r9.mCsm.mIsMdmiEnabled == false) goto L_0x01f2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x01e5, code lost:
        if (com.sec.internal.helper.ImsCallUtil.isE911Call(r9.mSession.getCallProfile().getCallType()) == false) goto L_0x01f2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x01e7, code lost:
        r9.mCsm.mMdmiE911Listener.notifySipMsg(com.sec.internal.ims.mdmi.MdmiServiceModule.msgType.SIP_BYE, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x01f2, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x01f3, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean processMessage(android.os.Message r10) {
        /*
            r9 = this;
            java.lang.String r0 = r9.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "[InCall] processMessage "
            r1.append(r2)
            int r2 = r10.what
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            int r0 = r10.what
            r1 = 0
            r2 = 1
            switch(r0) {
                case 1: goto L_0x01d1;
                case 3: goto L_0x01d1;
                case 4: goto L_0x01d1;
                case 13: goto L_0x01cd;
                case 16: goto L_0x01c1;
                case 25: goto L_0x01bd;
                case 41: goto L_0x01b9;
                case 51: goto L_0x01b5;
                case 52: goto L_0x01b0;
                case 55: goto L_0x01ab;
                case 56: goto L_0x017f;
                case 59: goto L_0x0174;
                case 60: goto L_0x016f;
                case 62: goto L_0x0168;
                case 64: goto L_0x0161;
                case 71: goto L_0x0153;
                case 73: goto L_0x0146;
                case 74: goto L_0x0141;
                case 75: goto L_0x013c;
                case 80: goto L_0x0137;
                case 81: goto L_0x0132;
                case 82: goto L_0x011a;
                case 83: goto L_0x00fb;
                case 86: goto L_0x00eb;
                case 87: goto L_0x00db;
                case 91: goto L_0x00d6;
                case 93: goto L_0x01d1;
                case 94: goto L_0x01d1;
                case 100: goto L_0x01d1;
                case 101: goto L_0x00d1;
                case 151: goto L_0x00c8;
                case 152: goto L_0x00bf;
                case 154: goto L_0x00ba;
                case 205: goto L_0x00b5;
                case 206: goto L_0x00b5;
                case 207: goto L_0x00b0;
                case 209: goto L_0x00ab;
                case 210: goto L_0x00ab;
                case 302: goto L_0x00a2;
                case 400: goto L_0x01d1;
                case 500: goto L_0x009d;
                case 501: goto L_0x0098;
                case 502: goto L_0x0093;
                case 600: goto L_0x008e;
                case 700: goto L_0x0087;
                case 5000: goto L_0x007c;
                case 5001: goto L_0x004a;
                default: goto L_0x001f;
            }
        L_0x001f:
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
            goto L_0x01f3
        L_0x004a:
            java.lang.String r10 = r9.LOG_TAG
            java.lang.String r0 = "DELAYED_EPSFB_CHECK_TIMING"
            android.util.Log.i(r10, r0)
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r10 = r9.mModule
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r9.mSession
            int r0 = r0.mPhoneId
            com.sec.internal.constants.ims.os.NetworkEvent r10 = r10.getNetwork(r0)
            if (r10 == 0) goto L_0x006f
            int r10 = r10.network
            r0 = 20
            if (r10 == r0) goto L_0x006f
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r10 = r9.mSession
            boolean r10 = r10.isEpdgCall()
            if (r10 != 0) goto L_0x006f
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r10 = r9.mSession
            r10.mEpsFallback = r2
        L_0x006f:
            com.sec.internal.interfaces.ims.core.IRegistrationManager r10 = r9.mRegistrationManager
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r9 = r9.mSession
            int r9 = r9.getPhoneId()
            r10.updateEpsFbInImsCall(r9)
            goto L_0x01f3
        L_0x007c:
            int r10 = r9.dbrLost_InCall(r10)
            r0 = -1
            if (r10 == r0) goto L_0x01d1
            if (r10 != r2) goto L_0x0086
            r1 = r2
        L_0x0086:
            return r1
        L_0x0087:
            int r10 = r10.arg1
            r9.notifyRecordState(r10)
            goto L_0x01f3
        L_0x008e:
            r9.enter()
            goto L_0x01f3
        L_0x0093:
            r9.reInvite_InCall()
            goto L_0x01f3
        L_0x0098:
            r9.locAcqSuccess_InCall(r10)
            goto L_0x01f3
        L_0x009d:
            r9.locAcqTimeout_InCall(r10)
            goto L_0x01f3
        L_0x00a2:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r10 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r10 = r10.mModifyRequestedProfile
            boolean r9 = r9.handleUpdate(r10)
            return r9
        L_0x00ab:
            r9.rttDBRLost_InCall()
            goto L_0x01f3
        L_0x00b0:
            r9.camStartFailed_InCall()
            goto L_0x01f3
        L_0x00b5:
            r9.videoRTPTImer_InCall()
            goto L_0x01f3
        L_0x00ba:
            boolean r9 = r9.idcSwitchRequest_InCall(r10)
            return r9
        L_0x00bf:
            java.lang.Object r10 = r10.obj
            com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra r10 = (com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra) r10
            r9.end_Idc(r10)
            goto L_0x01f3
        L_0x00c8:
            java.lang.Object r10 = r10.obj
            com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra r10 = (com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra) r10
            r9.sendReInvite_Idc(r10)
            goto L_0x01f3
        L_0x00d1:
            r9.sendInfo_InCall(r10)
            goto L_0x01f3
        L_0x00d6:
            r9.modified_InCall(r10)
            goto L_0x01f3
        L_0x00db:
            java.lang.String r0 = r9.LOG_TAG
            java.lang.String r1 = "[InCall] Receive CMC INFO EVENT."
            android.util.Log.i(r0, r1)
            java.lang.Object r10 = r10.obj
            com.sec.internal.constants.ims.servicemodules.volte2.CmcInfoEvent r10 = (com.sec.internal.constants.ims.servicemodules.volte2.CmcInfoEvent) r10
            r9.notifyCmcInfoEvent(r10)
            goto L_0x01f3
        L_0x00eb:
            java.lang.String r0 = r9.LOG_TAG
            java.lang.String r1 = "[InCall] Receive CMC DTMF EVENT."
            android.util.Log.i(r0, r1)
            int r10 = r10.arg1
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r9 = r9.mCsm
            r9.notifyCmcDtmfEvent(r10)
            goto L_0x01f3
        L_0x00fb:
            java.lang.String r10 = r9.LOG_TAG
            java.lang.String r0 = "[InCall] Video resumed."
            android.util.Log.i(r10, r0)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r10 = r9.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r0 = r0.getCallProfile()
            int r0 = r0.getCallType()
            r10.notifyOnModified(r0)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r9 = r9.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsInCall r10 = r9.mInCall
            r9.transitionTo(r10)
            goto L_0x01f3
        L_0x011a:
            java.lang.String r10 = r9.LOG_TAG
            java.lang.String r0 = "[InCall] Video held."
            android.util.Log.i(r10, r0)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r10 = r9.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r9 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r9 = r9.getCallProfile()
            int r9 = r9.getCallType()
            r10.notifyOnModified(r9)
            goto L_0x01f3
        L_0x0132:
            r9.resumeVideo_InCall()
            goto L_0x01f3
        L_0x0137:
            r9.holdVideo_InCall()
            goto L_0x01f3
        L_0x013c:
            r9.referStatus_InCall(r10)
            goto L_0x01f3
        L_0x0141:
            r9.extendToConf_InCall(r10)
            goto L_0x01f3
        L_0x0146:
            java.lang.Object r10 = r10.obj
            java.lang.String[] r10 = (java.lang.String[]) r10
            java.util.List r10 = java.util.Arrays.asList(r10)
            r9.extendToConference(r10)
            goto L_0x01f3
        L_0x0153:
            java.lang.String r10 = r9.LOG_TAG
            java.lang.String r0 = "[InCall] already in InCall"
            android.util.Log.i(r10, r0)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r9 = r9.mCsm
            r9.notifyOnResumed(r2)
            goto L_0x01f3
        L_0x0161:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r9 = r9.mCsm
            r9.sendRTTtext(r10)
            goto L_0x01f3
        L_0x0168:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r9 = r9.mCsm
            r9.handleRemoteHeld(r2)
            goto L_0x01f3
        L_0x016f:
            r9.cancelTransfer_InCall()
            goto L_0x01f3
        L_0x0174:
            java.lang.Object r10 = r10.obj
            java.lang.String r10 = (java.lang.String) r10
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r9 = r9.mCsm
            r9.transferCall(r10)
            goto L_0x01f3
        L_0x017f:
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
            goto L_0x01f3
        L_0x01ab:
            boolean r9 = r9.switchRequest_InCall(r10)
            return r9
        L_0x01b0:
            boolean r9 = r9.update_InCall(r10)
            return r9
        L_0x01b5:
            r9.hold_InCall()
            goto L_0x01f3
        L_0x01b9:
            r9.established_InCall()
            goto L_0x01f3
        L_0x01bd:
            r9.checkVideoDBR_InCall()
            goto L_0x01f3
        L_0x01c1:
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r10 = r9.mVolteSvcIntf
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r9 = r9.mSession
            int r9 = r9.getSessionId()
            r10.sendEmergencyLocationPublish(r9)
            goto L_0x01f3
        L_0x01cd:
            r9.locAcq_InCall(r10)
            goto L_0x01f3
        L_0x01d1:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r10 = r9.mCsm
            boolean r10 = r10.mIsMdmiEnabled
            if (r10 == 0) goto L_0x01f2
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r10 = r9.mSession
            com.sec.ims.volte2.data.CallProfile r10 = r10.getCallProfile()
            int r10 = r10.getCallType()
            boolean r10 = com.sec.internal.helper.ImsCallUtil.isE911Call(r10)
            if (r10 == 0) goto L_0x01f2
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r9 = r9.mCsm
            com.sec.internal.ims.mdmi.MdmiE911Listener r9 = r9.mMdmiE911Listener
            com.sec.internal.ims.mdmi.MdmiServiceModule$msgType r10 = com.sec.internal.ims.mdmi.MdmiServiceModule.msgType.SIP_BYE
            r2 = 0
            r9.notifySipMsg(r10, r2)
        L_0x01f2:
            return r1
        L_0x01f3:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsInCall.processMessage(android.os.Message):boolean");
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
    }

    private void enter_InCall() {
        if (this.mCsm.hasMessages(303)) {
            this.mCsm.removeMessages(303);
        }
        KeepAliveSender keepAliveSender = this.mSession.mKaSender;
        if (keepAliveSender != null) {
            keepAliveSender.stop();
        }
        handleCallEstablished();
        handleCMCPublishDialog();
        handleEPSFB();
    }

    private void handleEPSFB() {
        ImsCallSession imsCallSession = this.mSession;
        if (imsCallSession.mIsNrSaMode && !imsCallSession.isEpdgCall()) {
            NetworkEvent network = this.mModule.getNetwork(this.mSession.mPhoneId);
            if (!(network == null || network.network == 20)) {
                this.mSession.mEpsFallback = true;
            }
            if (this.mMno != Mno.VODAFONE_AUSTRALIA || this.mSession.mEpsFallback) {
                this.mRegistrationManager.updateEpsFbInImsCall(this.mSession.getPhoneId());
            } else {
                this.mCsm.sendMessageDelayed((int) CallStateMachine.DELAYED_EPSFB_CHECK_TIMING, (long) UtStateMachine.HTTP_READ_TIMEOUT_GCF);
            }
        }
    }

    private void handleCMCPublishDialog() {
        State previousState = this.mCsm.getPreviousState();
        CallStateMachine callStateMachine = this.mCsm;
        if ((previousState == callStateMachine.mModifyRequested || callStateMachine.getPreviousState() == this.mCsm.mModifyingCall) && this.mSession.getCallProfile().getCallType() == 1) {
            this.mCsm.sendCmcPublishDialog();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x003f, code lost:
        if (r5.mSession.getCallProfile().isPullCall() != true) goto L_0x00da;
     */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x00e6  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0102  */
    /* JADX WARNING: Removed duplicated region for block: B:28:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCallEstablished() {
        /*
            r5 = this;
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r5.mSession
            com.sec.ims.volte2.data.CallProfile r0 = r0.getCallProfile()
            r1 = 0
            r0.setVideoCRBT(r1)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r5.mCsm
            com.sec.internal.helper.State r0 = r0.getPreviousState()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r1 = r5.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsOutgoingCall r2 = r1.mOutgoingCall
            if (r0 == r2) goto L_0x0041
            com.sec.internal.helper.State r0 = r1.getPreviousState()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r1 = r5.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsIncomingCall r2 = r1.mIncomingCall
            if (r0 == r2) goto L_0x0041
            com.sec.internal.helper.State r0 = r1.getPreviousState()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r1 = r5.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsAlertingCall r2 = r1.mAlertingCall
            if (r0 == r2) goto L_0x0041
            com.sec.internal.helper.State r0 = r1.getPreviousState()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r1 = r5.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsReadyToCall r1 = r1.mReadyToCall
            if (r0 != r1) goto L_0x00da
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r5.mSession
            com.sec.ims.volte2.data.CallProfile r0 = r0.getCallProfile()
            boolean r0 = r0.isPullCall()
            r1 = 1
            if (r0 != r1) goto L_0x00da
        L_0x0041:
            java.lang.String r0 = r5.LOG_TAG
            java.lang.String r1 = "[InCall] Notify on Established"
            android.util.Log.i(r0, r1)
            long r0 = java.lang.System.currentTimeMillis()
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r2 = r5.mSession
            long r2 = r2.mStartTime
            long r0 = r0 - r2
            double r0 = (double) r0
            r2 = 4652007308841189376(0x408f400000000000, double:1000.0)
            double r0 = r0 / r2
            com.sec.internal.log.IMSLog$LAZER_TYPE r2 = com.sec.internal.log.IMSLog.LAZER_TYPE.CALL
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r4 = r5.mSession
            int r4 = r4.getCallId()
            r3.append(r4)
            java.lang.String r4 = " - CONNECTED - Call Setup Time = "
            r3.append(r4)
            r3.append(r0)
            java.lang.String r0 = r3.toString()
            com.sec.internal.log.IMSLog.lazer((com.sec.internal.log.IMSLog.LAZER_TYPE) r2, (java.lang.String) r0)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r5.mCsm
            r0.notifyOnEstablished()
            com.sec.internal.constants.Mno r0 = r5.mMno
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.TMOUS
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.DISH
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.VZW
            com.sec.internal.constants.Mno[] r1 = new com.sec.internal.constants.Mno[]{r1, r2, r3}
            boolean r0 = r0.isOneOf(r1)
            if (r0 == 0) goto L_0x0093
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r5.mCsm
            r0.forceNotifyCurrentCodec()
        L_0x0093:
            r5.handleSetVideoQuality()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r5.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r1 = r5.mSession
            com.sec.ims.volte2.data.CallProfile r1 = r1.getCallProfile()
            int r1 = r1.getCallType()
            boolean r0 = r0.needToLogForATTGate(r1)
            if (r0 == 0) goto L_0x00af
            java.lang.String r0 = "GATE"
            java.lang.String r1 = "<GATE-M>VIDEO_CALL_CONNECTED</GATE-M>"
            com.sec.internal.log.IMSLog.g(r0, r1)
        L_0x00af:
            com.sec.internal.interfaces.ims.core.imslogger.IImsDiagMonitor r0 = com.sec.internal.ims.registry.ImsRegistry.getImsDiagMonitor()
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r1 = r5.mSession
            int r1 = r1.getSessionId()
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r2 = r5.mSession
            com.sec.ims.volte2.data.CallProfile r2 = r2.getCallProfile()
            int r2 = r2.getCallType()
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r3 = r5.mSession
            com.sec.ims.volte2.data.CallProfile r3 = r3.getCallProfile()
            com.sec.ims.volte2.data.MediaProfile r3 = r3.getMediaProfile()
            com.sec.ims.volte2.data.VolteConstants$AudioCodecType r3 = r3.getAudioCodec()
            java.lang.String r3 = r3.toString()
            java.lang.String r4 = "CALL_ESTABLISHED"
            r0.notifyCallStatus(r1, r4, r2, r3)
        L_0x00da:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r5.mCsm
            com.sec.internal.helper.State r0 = r0.getPreviousState()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r1 = r5.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsOutgoingCall r2 = r1.mOutgoingCall
            if (r0 == r2) goto L_0x00fa
            com.sec.internal.helper.State r0 = r1.getPreviousState()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r1 = r5.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsIncomingCall r2 = r1.mIncomingCall
            if (r0 == r2) goto L_0x00fa
            com.sec.internal.helper.State r0 = r1.getPreviousState()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r1 = r5.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsAlertingCall r1 = r1.mAlertingCall
            if (r0 != r1) goto L_0x0124
        L_0x00fa:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r5.mSession
            int r0 = r0.getCmcType()
            if (r0 != 0) goto L_0x0124
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r5.mCsm
            long r1 = java.lang.System.currentTimeMillis()
            r0.mCmcCallEstablishTime = r1
            java.lang.String r0 = r5.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "[InCall] VoLTE callEstablishTime : "
            r1.append(r2)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r5 = r5.mCsm
            long r2 = r5.mCmcCallEstablishTime
            r1.append(r2)
            java.lang.String r5 = r1.toString()
            android.util.Log.i(r0, r5)
        L_0x0124:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsInCall.handleCallEstablished():void");
    }

    private boolean handleUpdate(CallProfile callProfile) {
        boolean z = false;
        if (callProfile == null) {
            return false;
        }
        if (this.mCsm.isChangedCallType(callProfile) && this.mCsm.modifyCallType(callProfile, true)) {
            z = true;
        }
        if (z) {
            this.mSession.mModifyRequestedProfile = callProfile;
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mModifyingCall);
        } else {
            this.mSession.mModifyRequestedProfile = null;
        }
        return true;
    }

    private void handleSetVideoQuality() {
        if (this.mMno != Mno.RJIL) {
            return;
        }
        if ("HD720" == this.mSession.getCallProfile().getMediaProfile().getVideoSize() || "HD720LAND" == this.mSession.getCallProfile().getMediaProfile().getVideoSize()) {
            this.mSession.getCallProfile().getMediaProfile().setVideoQuality(16);
        } else if ("VGA" == this.mSession.getCallProfile().getMediaProfile().getVideoSize() || "VGALAND" == this.mSession.getCallProfile().getMediaProfile().getVideoSize()) {
            this.mSession.getCallProfile().getMediaProfile().setVideoQuality(15);
        } else if ("QVGA" == this.mSession.getCallProfile().getMediaProfile().getVideoSize() || "QVGALAND" == this.mSession.getCallProfile().getMediaProfile().getVideoSize()) {
            this.mSession.getCallProfile().getMediaProfile().setVideoQuality(13);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x005d, code lost:
        if (r3.getPreviousState() != r8.mCsm.mAlertingCall) goto L_0x0065;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkVideo_InCall() {
        /*
            r8 = this;
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r8.mSession
            com.sec.ims.volte2.data.CallProfile r0 = r0.getCallProfile()
            int r0 = r0.getCallType()
            boolean r0 = com.sec.internal.helper.ImsCallUtil.isVideoCall(r0)
            if (r0 == 0) goto L_0x0015
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r8.mCsm
            r0.startNetworkStatsOnPorts()
        L_0x0015:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r8.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r1 = r8.mSession
            com.sec.ims.volte2.data.CallProfile r1 = r1.getCallProfile()
            int r1 = r1.getCallType()
            r2 = 0
            boolean r0 = r0.isStartedCamera(r1, r2)
            r1 = 1
            if (r0 == 0) goto L_0x002e
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r8.mCsm
            r0.mIsStartCameraSuccess = r1
            goto L_0x0065
        L_0x002e:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r8.mSession
            boolean r0 = r0.getUsingCamera()
            if (r0 == 0) goto L_0x0065
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r8.mCsm
            r0.mIsCheckVideoDBR = r2
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r8.mSession
            r0.stopCamera()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r8.mCsm
            com.sec.internal.helper.State r0 = r0.getPreviousState()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r3 = r8.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsOutgoingCall r4 = r3.mOutgoingCall
            if (r0 == r4) goto L_0x005f
            com.sec.internal.helper.State r0 = r3.getPreviousState()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r3 = r8.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsIncomingCall r4 = r3.mIncomingCall
            if (r0 == r4) goto L_0x005f
            com.sec.internal.helper.State r0 = r3.getPreviousState()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r3 = r8.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsAlertingCall r3 = r3.mAlertingCall
            if (r0 != r3) goto L_0x0065
        L_0x005f:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r8.mSession
            int r3 = r0.mPrevUsedCamera
            r0.mLastUsedCamera = r3
        L_0x0065:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r8.mCsm
            boolean r0 = r0.mIsStartCameraSuccess
            if (r0 != 0) goto L_0x008d
            com.sec.internal.constants.Mno r0 = r8.mMno
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.DOCOMO
            if (r0 == r3) goto L_0x008d
            boolean r0 = r0.isKor()
            if (r0 != 0) goto L_0x008d
            boolean r0 = r8.downgradeVideoToVoiceRequest()
            if (r0 == 0) goto L_0x0089
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r8.mSession
            r0.notifyCallDowngraded()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r8.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsModifyingCall r3 = r0.mModifyingCall
            r0.transitionTo(r3)
        L_0x0089:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r8.mCsm
            r0.mIsStartCameraSuccess = r1
        L_0x008d:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r8.mCsm
            boolean r0 = r0.mIsCheckVideoDBR
            if (r0 != 0) goto L_0x00e6
            com.sec.internal.constants.Mno r0 = r8.mMno
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.TMOUS
            if (r0 != r3) goto L_0x00e6
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r8.mSession
            com.sec.ims.volte2.data.CallProfile r0 = r0.getCallProfile()
            int r0 = r0.getCallType()
            boolean r0 = com.sec.internal.helper.ImsCallUtil.isVideoCall(r0)
            if (r0 == 0) goto L_0x00e6
            com.sec.internal.interfaces.ims.core.IPdnController r0 = com.sec.internal.ims.registry.ImsRegistry.getPdnController()
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r3 = r8.mSession
            int r3 = r3.getPhoneId()
            boolean r0 = r0.isEpdgConnected(r3)
            if (r0 != 0) goto L_0x00e6
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r8.mSession
            boolean r3 = r0.mIsNrSaMode
            if (r3 != 0) goto L_0x00e6
            com.sec.ims.volte2.data.CallProfile r0 = r0.getCallProfile()
            boolean r0 = r0.isMOCall()
            if (r0 == 0) goto L_0x00cc
            r0 = 1500(0x5dc, float:2.102E-42)
            goto L_0x00ce
        L_0x00cc:
            r0 = 500(0x1f4, float:7.0E-43)
        L_0x00ce:
            java.security.SecureRandom r2 = com.sec.internal.ims.util.ImsUtil.getRandom()
            r3 = 1000(0x3e8, float:1.401E-42)
            int r2 = r2.nextInt(r3)
            int r0 = r0 + r2
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r2 = r8.mCsm
            r2.mIsCheckVideoDBR = r1
            r3 = 25
            r4 = 0
            r5 = -1
            long r6 = (long) r0
            r2.sendMessageDelayed(r3, r4, r5, r6)
            return r1
        L_0x00e6:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r8.mSession
            com.sec.ims.volte2.data.CallProfile r0 = r0.getCallProfile()
            int r0 = r0.getCallType()
            boolean r0 = com.sec.internal.helper.ImsCallUtil.isVideoCall(r0)
            if (r0 == 0) goto L_0x0138
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r0 = r8.mModule
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r3 = r8.mSession
            int r3 = r3.getPhoneId()
            java.lang.String r4 = "mmtel-video"
            boolean r0 = r0.isCallServiceAvailable(r3, r4)
            if (r0 != 0) goto L_0x0138
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r8.mSession
            com.sec.ims.volte2.data.CallProfile r0 = r0.getCallProfile()
            int r0 = r0.getCallType()
            r3 = 8
            if (r0 == r3) goto L_0x0138
            java.lang.String r0 = r8.LOG_TAG
            java.lang.String r3 = "[InCall] ForceDowngrade trigger due to MMTEL-VIDEO was not exist case"
            android.util.Log.i(r0, r3)
            com.sec.ims.volte2.data.CallProfile r0 = new com.sec.ims.volte2.data.CallProfile
            r0.<init>()
            r0.setCallType(r1)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r3 = r8.mCsm
            boolean r0 = r3.modifyCallType(r0, r1)
            if (r0 == 0) goto L_0x0138
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r8.mSession
            r0.notifyCallDowngraded()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r8 = r8.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsModifyingCall r0 = r8.mModifyingCall
            r8.transitionTo(r0)
            return r1
        L_0x0138:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsInCall.checkVideo_InCall():boolean");
    }

    /* access modifiers changed from: protected */
    public boolean downgradeVideoToVoiceRequest() {
        if (this.mSession.getCallProfile().getCallType() != 2 && this.mSession.getCallProfile().getCallType() != 3) {
            return false;
        }
        Log.i(this.LOG_TAG, "[InCall] downgradeVideoToVoiceRequest() trigger downgrade");
        CallProfile callProfile = new CallProfile();
        callProfile.setCallType(1);
        this.mSession.mModifyRequestedProfile = callProfile;
        return this.mCsm.modifyCallType(callProfile, true);
    }

    private void hold_InCall() {
        if (this.mRegistration == null || this.mModule.isProhibited(this.mSession.getPhoneId())) {
            this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
        } else if (this.mVolteSvcIntf.holdCall(this.mSession.getSessionId()) < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1006, "remote exception"));
        } else {
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mHoldingCall);
        }
    }

    private void established_InCall() {
        if (this.mMno != Mno.STARHUB) {
            this.mCsm.handleRemoteHeld(false);
        }
    }

    private boolean update_InCall(Message message) {
        if (!this.mModule.isProhibited(this.mSession.getPhoneId())) {
            return handleUpdate(((Bundle) message.obj).getParcelable("profile"));
        }
        this.mCsm.notifyOnError(1109, "Call switch failed");
        return true;
    }

    private void videoRTPTImer_InCall() {
        Log.i(this.LOG_TAG, "[InCall] Downgrade Video Quality due to Poor Video Quality/RTP Timeout");
        IMSLog.c(LogClass.VOLTE_VIDEO_RTP_TIMEOUT, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
        this.mCsm.mVideoRTPtimeout = true;
        if (handleVideoDowngradeRequest()) {
            this.mSession.notifyCallDowngraded();
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mModifyingCall);
        }
    }

    private void rttDBRLost_InCall() {
        Log.i(this.LOG_TAG, "[InCall] Downgrade voice call due to Rtt DBR Timeout/Lost");
        if (handleRttDowngradeRequest()) {
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mModifyingCall);
        }
    }

    /* access modifiers changed from: protected */
    public boolean handleRttDowngradeRequest() {
        EpdgManager epdgManager;
        String str = this.LOG_TAG;
        Log.i(str, "[InCall] handleRttDowngradeRequest: " + this.mCsm.getCurrentState().getName());
        this.mSession.setRttDedicatedBearerTimeoutMessage((Message) null);
        IRegistrationManager iRegistrationManager = this.mRegistrationManager;
        if (iRegistrationManager != null && iRegistrationManager.isVoWiFiSupported(this.mSession.getPhoneId()) && (epdgManager = this.mModule.getEpdgManager()) != null && epdgManager.isDuringHandoverForIMS()) {
            String str2 = this.LOG_TAG;
            Log.i(str2, "handleRttDowngradeRequest: ignore RTT Dedicated Bearer Lost due to EPDG for mno:" + this.mMno);
            this.mSession.stopRttDedicatedBearerTimer();
            this.mSession.setDedicatedBearerState(99, 3);
            return false;
        } else if (!ImsCallUtil.isRttCall(this.mSession.getCallProfile().getCallType()) || this.mCsm.mRemoteHeld) {
            return false;
        } else {
            Log.i(this.LOG_TAG, "handleRttDowngradeRequest: trigger downgrade");
            CallProfile callProfile = new CallProfile();
            callProfile.setCallType(1);
            this.mSession.mModifyRequestedProfile = callProfile;
            return this.mCsm.modifyCallType(callProfile, true);
        }
    }

    private boolean switchRequest_InCall(Message message) {
        this.mSession.mModifyRequestedProfile = new CallProfile();
        this.mSession.mModifyRequestedProfile.setCallType(message.arg1);
        this.mSession.mModifyRequestedProfile.getMediaProfile().setVideoQuality(this.mSession.getCallProfile().getMediaProfile().getVideoQuality());
        if (this.mModule.hasRingingCall()) {
            Log.i(this.LOG_TAG, "[InCall] Rejecting switch request - send 603 to remote party has Incoming call on other session");
            if (this.mCsm.rejectModifyCallType(Id.REQUEST_UPDATE_TIME_IN_PLANI) >= 0) {
                return true;
            }
            this.mCsm.sendMessage(4, 0, -1, new SipError(1006, ""));
            return false;
        }
        int determineCamera = this.mCsm.determineCamera(this.mSession.mModifyRequestedProfile.getCallType(), true);
        if (!this.mSession.getUsingCamera() && determineCamera >= 0) {
            this.mSession.startCamera(determineCamera);
        }
        if (!ImsCallUtil.isTtyCall(this.mSession.mModifyRequestedProfile.getCallType()) && !ImsCallUtil.isRttCall(this.mSession.getCallProfile().getCallType()) && !ImsCallUtil.isRttCall(this.mSession.mModifyRequestedProfile.getCallType())) {
            this.mMediaController.receiveSessionModifyRequest(this.mSession.getSessionId(), this.mSession.mModifyRequestedProfile);
        }
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.transitionTo(callStateMachine.mModifyRequested);
        if (this.mSession.mModifyRequestedProfile.getCallType() == 9) {
            this.mCsm.sendMessage(22, (Object) this.mSession.mModifyRequestedProfile);
        } else if (ImsCallUtil.isRttCall(this.mSession.mModifyRequestedProfile.getCallType()) || ImsCallUtil.isRttCall(this.mSession.getCallProfile().getCallType())) {
            this.mModule.onSendRttSessionModifyRequest(this.mSession.getCallId(), ImsCallUtil.isRttCall(this.mSession.mModifyRequestedProfile.getCallType()));
        } else {
            notifyOnSessionUpdateRequested(message.arg1, (byte[]) message.obj);
        }
        return true;
    }

    private void holdVideo_InCall() {
        if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType()) && this.mMno == Mno.VZW) {
            if (this.mCsm.isDeferedVideoResume) {
                Log.i(this.LOG_TAG, "[InCall] video resume defered. ignore video hold");
                this.mCsm.isDeferedVideoResume = false;
                return;
            }
            this.mMediaController.holdVideo(this.mSession.getSessionId());
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mHoldingVideo);
        }
    }

    private void resumeVideo_InCall() {
        if (this.mMno == Mno.VZW && ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
            this.mCsm.isDeferedVideoResume = false;
            this.mMediaController.resumeVideo(this.mSession.getSessionId());
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mResumingVideo);
        }
    }

    private void extendToConference(List<String> list) {
        ArrayList arrayList = new ArrayList();
        int callType = this.mSession.getCallProfile().getCallType();
        for (int i = 0; i < list.size(); i++) {
            if (this.mMno != Mno.LGU || list.get(i) == null || !list.get(i).equals(this.mSession.getCallProfile().getDialingNumber())) {
                arrayList.add(this.mSession.buildUri(list.get(i), (String) null, callType).toString());
            }
        }
        if (this.mRegistration == null || arrayList.size() <= 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1005, "Not enough participant."));
            return;
        }
        ImsProfile imsProfile = this.mRegistration.getImsProfile();
        ConfCallSetupData confCallSetupData = new ConfCallSetupData(this.mSession.getConferenceUri(imsProfile), arrayList, callType);
        confCallSetupData.enableSubscription(this.mSession.getConfSubscribeEnabled(imsProfile));
        confCallSetupData.setSubscribeDialogType(this.mSession.getConfSubscribeDialogType(imsProfile));
        confCallSetupData.setReferUriType(this.mSession.getConfReferUriType(imsProfile));
        confCallSetupData.setRemoveReferUriType(this.mSession.getConfRemoveReferUriType(imsProfile));
        confCallSetupData.setReferUriAsserted(this.mSession.getConfReferUriAsserted(imsProfile));
        confCallSetupData.setOriginatingUri(this.mSession.getOriginatingUri());
        confCallSetupData.setUseAnonymousUpdate(this.mSession.getConfUseAnonymousUpdate(imsProfile));
        confCallSetupData.setSupportPrematureEnd(this.mSession.getConfSupportPrematureEnd(imsProfile));
        int addUserForConferenceCall = this.mVolteSvcIntf.addUserForConferenceCall(this.mSession.getSessionId(), confCallSetupData, true);
        Log.i(this.LOG_TAG, "[InCall] extendToConference() returned session id " + addUserForConferenceCall);
        if (addUserForConferenceCall < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1002, "stack return -1"));
        }
    }

    private void extendToConf_InCall(Message message) {
        int callType = this.mSession.getCallProfile().getCallType();
        if (callType != message.arg2) {
            String str = this.LOG_TAG;
            Log.i(str, "[InCall] callType " + callType + " to callType " + message.arg2);
            this.mSession.getCallProfile().setCallType(message.arg2);
            this.mSession.getCallProfile().setConferenceCall(2);
        }
        this.mCsm.notifyOnModified(message.arg2);
    }

    private void cancelTransfer_InCall() {
        if (this.mCsm.mTransferRequested) {
            Log.i(this.LOG_TAG, "[InCall] cancel call transfer");
            this.mCsm.notifyOnError(1119, "cancel call transfer");
            if (this.mVolteSvcIntf.cancelTransferCall(this.mSession.getSessionId()) < 0) {
                this.mCsm.notifyOnError(1121, "cancel call transfer fail", 0);
            }
            this.mCsm.notifyOnError(1120, "cancel call transfer success", 0);
            this.mCsm.mTransferRequested = false;
            return;
        }
        Log.e(this.LOG_TAG, "[InCall] call transfer is not requested, so ignore cancel transfer");
        this.mCsm.notifyOnError(1121, "cancel call transfer fail", 0);
    }

    private void referStatus_InCall(Message message) {
        CallStateMachine callStateMachine = this.mCsm;
        if (callStateMachine.mTransferRequested) {
            if (message.arg1 == 200) {
                callStateMachine.notifyOnError(1118, "call transfer success (" + message.arg1 + ")");
            } else {
                callStateMachine.notifyOnError(1119, "call transfer failed (" + message.arg1 + ")");
            }
            CallStateMachine callStateMachine2 = this.mCsm;
            callStateMachine2.mHoldBeforeTransfer = false;
            callStateMachine2.mTransferRequested = false;
        }
    }

    private void modified_InCall(Message message) {
        int i = message.arg1;
        int i2 = message.arg2;
        String str = this.LOG_TAG;
        Log.i(str, "[InCall] modifiedCallType " + i + ", orgCallType " + i2);
        if (i != i2 && (ImsCallUtil.isRttCall(i) || ImsCallUtil.isRttCall(i2))) {
            this.mModule.onSendRttSessionModifyResponse(this.mSession.getCallId(), !ImsCallUtil.isRttCall(i2) && ImsCallUtil.isRttCall(i), true);
        }
        String isFocus = this.mSession.getCallProfile().getIsFocus();
        Mno mno = Mno.ZAIN_KSA;
        Mno mno2 = this.mMno;
        if ((mno == mno2 || Mno.AIRTEL == mno2 || Mno.MTN_SOUTHAFRICA == mno2) && "1".equals(isFocus)) {
            this.mCsm.notifyOnResumed(false);
        } else {
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
        this.mCsm.isStartedCamera(this.mSession.getCallProfile().getCallType(), false);
    }

    private int dbrLost_InCall(Message message) {
        if (message.arg1 == 2) {
            Mno mno = this.mMno;
            if (mno != Mno.CTC && mno != Mno.CU && mno != Mno.CTCMO) {
                CallProfile callProfile = this.mSession.getCallProfile();
                callProfile.setCallType(1);
                return handleUpdate(callProfile) ? 1 : 0;
            } else if (mno == Mno.CU && this.mSession.getDedicatedBearerState(2) != 3 && this.mSession.getCallProfile().getCallType() == 4) {
                return -1;
            } else {
                Log.i(this.LOG_TAG, "[InCall] Downgrade Call due to Video Dedicated Bearer lost");
                if (handleVideoDowngradeRequest()) {
                    this.mSession.notifyCallDowngraded();
                    CallStateMachine callStateMachine = this.mCsm;
                    callStateMachine.transitionTo(callStateMachine.mModifyingCall);
                    return 1;
                }
            }
        }
        return -1;
    }

    private void camStartFailed_InCall() {
        Mno mno = this.mMno;
        if (mno != Mno.DOCOMO && !mno.isKor()) {
            if (downgradeVideoToVoiceRequest()) {
                Log.i(this.LOG_TAG, "[InCall] Downgrade Call due to StartCamera failed");
                IMSLog.c(LogClass.VOLTE_START_CAMERA_FAIL, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
                this.mSession.notifyCallDowngraded();
                CallStateMachine callStateMachine = this.mCsm;
                callStateMachine.transitionTo(callStateMachine.mModifyingCall);
            }
            this.mCsm.mIsStartCameraSuccess = true;
        }
    }

    private void reInvite_InCall() {
        this.mCsm.callType = this.mSession.getCallProfile().getCallType();
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.mReinvite = true;
        if ((!ImsCallUtil.isVideoCall(callStateMachine.callType) || this.mMno != Mno.ATT) && !this.mCsm.mRemoteHeld) {
            Log.i(this.LOG_TAG, "[InCall] send H/O Re-INVITE");
            this.mVolteSvcIntf.sendReInvite(this.mSession.getSessionId(), new SipReason("SIP", 0, "", new String[0]));
            return;
        }
        String str = this.LOG_TAG;
        Log.i(str, "[InCall] calltype=" + this.mCsm.callType + ", ignore re-INVITE");
    }

    private void sendInfo_InCall(Message message) {
        Log.i(this.LOG_TAG, "[InCall] sendInfo");
        this.mCsm.callType = this.mSession.getCallProfile().getCallType();
        Bundle bundle = (Bundle) message.obj;
        String string = bundle.getString(McsConstants.BundleData.INFO);
        int i = bundle.getInt("type");
        String str = this.LOG_TAG;
        Log.i(str, "info callType= %d" + this.mCsm.callType + ", request=%s" + string + ", ussdType=%d" + i);
        this.mVolteSvcIntf.sendInfo(this.mSession.getSessionId(), this.mCsm.callType, string, i);
    }

    private void checkVideoDBR_InCall() {
        if (this.mSession.getDedicatedBearerState(2) == 3 && this.mSession.getDedicatedBearerState(8) == 3) {
            Log.i(this.LOG_TAG, "[InCall] Downgrade Call due to Video DBR is not opened");
            if (handleVideoDowngradeRequest()) {
                this.mSession.notifyCallDowngraded();
                CallStateMachine callStateMachine = this.mCsm;
                callStateMachine.transitionTo(callStateMachine.mModifyingCall);
            }
        }
    }

    private boolean handleVideoDowngradeRequest() {
        String str = this.LOG_TAG;
        Log.i(str, "[InCall] handleVideoDowngradeRequest: " + this.mCsm.getCurrentState().getName());
        if ((this.mSession.getCallProfile().getCallType() != 2 && this.mSession.getCallProfile().getCallType() != 4) || this.mCsm.mRemoteHeld) {
            return false;
        }
        Log.i(this.LOG_TAG, "handleVideoDowngradeRequest: trigger downgrade");
        CallProfile callProfile = new CallProfile();
        callProfile.setCallType(1);
        return this.mCsm.modifyCallType(callProfile, true);
    }

    private void notifyOnSessionUpdateRequested(int i, byte[] bArr) {
        int beginBroadcast = this.mListeners.beginBroadcast();
        for (int i2 = 0; i2 < beginBroadcast; i2++) {
            try {
                this.mListeners.getBroadcastItem(i2).onSessionUpdateRequested(i, bArr);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void notifyCmcInfoEvent(CmcInfoEvent cmcInfoEvent) {
        int beginBroadcast = this.mListeners.beginBroadcast();
        String str = this.LOG_TAG;
        Log.i(str, "notifyCmcDtmfEvent: " + cmcInfoEvent.getExternalCallId() + ", recordEvent : " + cmcInfoEvent.getRecordEvent());
        for (int i = 0; i < beginBroadcast; i++) {
            IImsCallSessionEventListener broadcastItem = this.mListeners.getBroadcastItem(i);
            try {
                this.mSession.mCallProfile.setCmcRecordEvent(cmcInfoEvent.getRecordEvent());
                broadcastItem.onProfileUpdated(this.mSession.mCallProfile.getMediaProfile(), this.mSession.mCallProfile.getMediaProfile());
                this.mSession.mCallProfile.setCmcRecordEvent(-1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    private void notifyRecordState(int i) {
        String str = this.LOG_TAG;
        Log.i(str, "[InCall] notifyRecordState: " + i);
        this.mSession.mCallProfile.setRecordState(i);
        int beginBroadcast = this.mListeners.beginBroadcast();
        for (int i2 = 0; i2 < beginBroadcast; i2++) {
            try {
                this.mListeners.getBroadcastItem(i2).onProfileUpdated(this.mSession.mCallProfile.getMediaProfile(), this.mSession.mCallProfile.getMediaProfile());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    private void locAcq_InCall(Message message) {
        Log.i(this.LOG_TAG, "[InCall] Request Location Acquiring");
        IGeolocationController geolocationController = ImsRegistry.getGeolocationController();
        if (geolocationController != null) {
            this.mCsm.mRequestLocation = geolocationController.startGeolocationUpdate(this.mSession.getPhoneId(), true);
            int locationAcquireTime = getLocationAcquireTime(this.mSession.getPhoneId());
            IMSLog.c(LogClass.VOLTE_GET_GEOLOCATION, this.mSession.getPhoneId() + "," + this.mSession.getSessionId() + "," + (this.mCsm.mRequestLocation ? 1 : 0) + "," + locationAcquireTime);
            CallStateMachine callStateMachine = this.mCsm;
            if (callStateMachine.mRequestLocation) {
                callStateMachine.sendMessageDelayed(500, (long) locationAcquireTime);
                this.mCsm.isLocationAcquiringTriggered = true;
            }
        }
    }

    private int getLocationAcquireTime(int i) {
        ImsProfile imsProfile = this.mRegistrationManager.getImsProfile(i, ImsProfile.PROFILE_TYPE.EMERGENCY);
        if (imsProfile != null) {
            return imsProfile.getLocationAcquireFailIncall();
        }
        Log.i(this.LOG_TAG, "[ReadyToCall] imsProfile is null, use default");
        return ImsCallUtil.DEFAULT_LOCATION_ACQUIRE_TIME;
    }

    private void locAcqSuccess_InCall(Message message) {
        if (this.mCsm.isLocationAcquiringTriggered) {
            Log.i(this.LOG_TAG, "[InCall] Location Acquiring Success -> Send PUBLISH");
            if (ImsRegistry.getGeolocationController() != null) {
                this.mCsm.removeMessages(500);
                this.mCsm.isLocationAcquiringTriggered = false;
            }
            IMSLog.c(LogClass.VOLTE_GEOLOCATION_SUCCESS, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
            this.mCsm.sendMessage(16);
        }
    }

    private void locAcqTimeout_InCall(Message message) {
        if (this.mCsm.isLocationAcquiringTriggered) {
            Log.i(this.LOG_TAG, "[InCall] Location Acquiring Timeout & Get Last known Location -> Start PUBLISH");
            this.mCsm.isLocationAcquiringTriggered = false;
            IGeolocationController geolocationController = ImsRegistry.getGeolocationController();
            if (geolocationController != null) {
                geolocationController.updateGeolocationFromLastKnown(this.mSession.getPhoneId());
            }
            IMSLog.c(LogClass.VOLTE_GEOLOCATION_FAIL, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
            this.mCsm.sendMessage(16);
        }
    }

    private void sendReInvite_Idc(IdcExtra idcExtra) {
        Log.i(this.LOG_TAG, "[IDC][InCall] send IDC-ADC Offer ReInvite");
        if (this.mSession.getIdcData() == null) {
            Log.i(this.LOG_TAG, "[IDC][InCall] fail because mSession.getIdcData() null");
        } else if (this.mCsm.modifyIdcRequest(idcExtra)) {
            this.mSession.getIdcData().transitState(IdcImsCallSessionData.State.MODIFYING);
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mModifyingCall);
        }
    }

    private void end_Idc(IdcExtra idcExtra) {
        Log.i(this.LOG_TAG, "[InCall] send IDC-ADC end ReInvite");
        if (this.mSession.getIdcData() == null) {
            Log.i(this.LOG_TAG, "[IDC][InCall] fail because mSession.getIdcData() null");
        } else if (this.mCsm.modifyIdcRequest(idcExtra)) {
            this.mSession.getIdcData().transitState(IdcImsCallSessionData.State.MODIFYING);
            CallStateMachine callStateMachine = this.mCsm;
            callStateMachine.transitionTo(callStateMachine.mModifyingCall);
        }
    }

    private boolean idcSwitchRequest_InCall(Message message) {
        if (this.mSession.getIdcData() == null) {
            return false;
        }
        this.mModule.hasRingingCall();
        this.mModule.getIdcServiceHelper().receiveSdpOffer(this.mSession.getSessionId(), (IdcExtra) message.obj);
        this.mSession.getIdcData().transitState(IdcImsCallSessionData.State.MODIFY_REQUESTED);
        CallStateMachine callStateMachine = this.mCsm;
        callStateMachine.transitionTo(callStateMachine.mModifyRequested);
        return true;
    }
}
