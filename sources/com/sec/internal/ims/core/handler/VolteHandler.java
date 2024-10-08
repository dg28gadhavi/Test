package com.sec.internal.ims.core.handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.Dialog;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.ims.core.handler.secims.UserAgent;
import com.sec.internal.ims.servicemodules.volte2.data.CallSetupData;
import com.sec.internal.ims.servicemodules.volte2.data.ConfCallSetupData;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import java.util.HashMap;
import java.util.List;

public abstract class VolteHandler extends BaseHandler implements IVolteServiceInterface {
    public int addParticipantToNWayConferenceCall(int i, int i2) {
        return -1;
    }

    public int addParticipantToNWayConferenceCall(int i, String str) {
        return -1;
    }

    public int addUserForConferenceCall(int i, ConfCallSetupData confCallSetupData, boolean z) {
        return -1;
    }

    public int answerCallWithCallType(int i, int i2) {
        return -1;
    }

    public int answerCallWithCallType(int i, int i2, String str) {
        return -1;
    }

    public int answerCallWithCallType(int i, int i2, String str, String str2) {
        return 0;
    }

    public void clearAllCallInternal(int i) {
    }

    public int deleteTcpSocket(int i, int i2) {
        return -1;
    }

    public int endCall(int i, int i2, SipReason sipReason) {
        return -1;
    }

    public int handleDtmf(int i, int i2, int i3, int i4, Message message) {
        return -1;
    }

    public int holdCall(int i) {
        return -1;
    }

    public int makeCall(int i, CallSetupData callSetupData, HashMap<String, String> hashMap, int i2) {
        return -1;
    }

    public int modifyCallType(int i, int i2, int i3) {
        return -1;
    }

    public int proceedIncomingCall(int i, HashMap<String, String> hashMap, String str) {
        return -1;
    }

    public void registerForCallStateEvent(Handler handler, int i, Object obj) {
    }

    public void registerForCdpnInfoEvent(Handler handler, int i, Object obj) {
    }

    public void registerForCmcInfoEvent(Handler handler, int i, Object obj) {
    }

    public void registerForCurrentLocationDiscoveryDuringEmergencyCallEvent(Handler handler, int i, Object obj) {
    }

    public void registerForDedicatedBearerNotifyEvent(Handler handler, int i, Object obj) {
    }

    public void registerForDialogEvent(Handler handler, int i, Object obj) {
    }

    public void registerForDialogSubscribeStatus(Handler handler, int i, Object obj) {
    }

    public void registerForDtmfEvent(Handler handler, int i, Object obj) {
    }

    public void registerForIncomingCallEvent(Handler handler, int i, Object obj) {
    }

    public void registerForReferStatus(Handler handler, int i, Object obj) {
    }

    public void registerForRrcConnectionEvent(Handler handler, int i, Object obj) {
    }

    public void registerForRtpLossRateNoti(Handler handler, int i, Object obj) {
    }

    public void registerForSIPMSGEvent(Handler handler, int i, Object obj) {
    }

    public void registerForTextEvent(Handler handler, int i, Object obj) {
    }

    public void registerForUssdEvent(Handler handler, int i, Object obj) {
    }

    public void registerQuantumSecurityStatusEvent(Handler handler, int i, Object obj) {
    }

    public int rejectCall(int i, int i2, SipError sipError) {
        return -1;
    }

    public int rejectModifyCallType(int i, int i2) {
        return -1;
    }

    public int removeParticipantFromNWayConferenceCall(int i, int i2) {
        return -1;
    }

    public int removeParticipantFromNWayConferenceCall(int i, String str) {
        return -1;
    }

    public void replaceSipCallId(int i, String str) {
    }

    public void replaceUserAgent(int i, int i2) {
    }

    public int replyModifyCallType(int i, int i2, int i3, int i4) {
        return -1;
    }

    public int replyModifyCallType(int i, int i2, int i3, int i4, String str) {
        return -1;
    }

    public int replyWithIdc(int i, int i2, int i3, int i4, String str) {
        return -1;
    }

    public int resumeCall(int i) {
        return -1;
    }

    public void sendDtmfEvent(int i, String str) {
    }

    public int sendEmergencyLocationPublish(int i) {
        return -1;
    }

    public void sendNegotiatedLocalSdp(int i, String str) {
    }

    public int sendReInvite(int i, SipReason sipReason) {
        return -1;
    }

    public int sendReInviteWithIdcExtra(int i, String str) {
        return -1;
    }

    public int sendText(int i, String str, int i2) {
        return -1;
    }

    public int sendTtyData(int i, byte[] bArr) {
        return -1;
    }

    public void setAutomaticMode(int i, boolean z) {
    }

    public void setOutOfService(boolean z, int i) {
    }

    public void setRttMode(int i, int i2) {
    }

    public int setTtyMode(int i, int i2, int i3) {
        return -1;
    }

    public void setTtyMode(String str) {
    }

    public void setVideoCrtAudio(int i, boolean z) {
    }

    public int startNWayConferenceCall(int i, ConfCallSetupData confCallSetupData) {
        return -1;
    }

    public void unregisterForCallStateEvent(Handler handler) {
    }

    public void unregisterForCdpnInfoEvent(Handler handler) {
    }

    public void unregisterForCmcInfoEvent(Handler handler) {
    }

    public void unregisterForCurrentLocationDiscoveryDuringEmergencyCallEvent(Handler handler) {
    }

    public void unregisterForDedicatedBearerNotifyEvent(Handler handler) {
    }

    public void unregisterForDialogEvent(Handler handler) {
    }

    public void unregisterForDialogSubscribeStatus(Handler handler) {
    }

    public void unregisterForDtmfEvent(Handler handler) {
    }

    public void unregisterForIncomingCallEvent(Handler handler) {
    }

    public void unregisterForReferStatus(Handler handler) {
    }

    public void unregisterForRrcConnectionEvent(Handler handler) {
    }

    public void unregisterForRtpLossRateNoti(Handler handler) {
    }

    public void unregisterForSIPMSGEvent(Handler handler) {
    }

    public void unregisterForTextEvent(Handler handler) {
    }

    public void unregisterForUssdEvent(Handler handler) {
    }

    public void unregisterQuantumSecurityStatusEvent(Handler handler) {
    }

    public void updateAudioInterface(int i, String str) {
    }

    public void updateAudioInterface(int i, String str, UserAgent userAgent) {
    }

    public void updateNrSaModeOnStart(int i) {
    }

    public void updateScreenOnOff(int i, int i2) {
    }

    public void updateXqEnable(int i, boolean z) {
    }

    protected VolteHandler(Looper looper) {
        super(looper);
    }

    public int transferCall(int i, String str) {
        Log.i(this.LOG_TAG, "transferCall: not implemented.");
        return -1;
    }

    public int cancelTransferCall(int i) {
        Log.i(this.LOG_TAG, "cancelTransferCall: not implemented.");
        return -1;
    }

    public int pullingCall(int i, String str, String str2, String str3, Dialog dialog, List<String> list) {
        Log.i(this.LOG_TAG, "pullingCall: not implemented.");
        return -1;
    }

    public int publishDialog(int i, String str, String str2, String str3, int i2, boolean z) {
        Log.i(this.LOG_TAG, "publishDialog: not implemented.");
        return -1;
    }

    public void handleMessage(Message message) {
        int i = message.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + message.what);
    }

    public int sendInfo(int i, int i2, String str, int i3) {
        Log.i(this.LOG_TAG, "sendInfo: not implemented.");
        return -1;
    }

    public int sendCmcInfo(int i, Bundle bundle) {
        Log.i(this.LOG_TAG, "sendCmcInfo: not implemented.");
        return -1;
    }

    public int sendVcsInfo(int i, Bundle bundle) {
        Log.i(this.LOG_TAG, "sendVcsInfo: not implemented.");
        return -1;
    }

    public int enableQuantumSecurityService(int i, boolean z) {
        Log.i(this.LOG_TAG, "enableQuantumSecurityService: not implemented.");
        return -1;
    }

    public int setQuantumSecurityInfo(int i, Bundle bundle) {
        Log.i(this.LOG_TAG, "setQuantumSecurityInfo: not implemented.");
        return -1;
    }

    public int startVideoEarlyMedia(int i) {
        Log.i(this.LOG_TAG, "startVideoEarlyMedia: not implemented.");
        return -1;
    }

    public int handleCmcCsfb(int i) {
        Log.i(this.LOG_TAG, "handleCmcCsfb: not implemented.");
        return -1;
    }
}
