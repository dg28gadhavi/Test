package com.sec.internal.interfaces.ims.servicemodules.volte2;

import android.os.Message;
import android.os.RemoteException;
import com.android.internal.telephony.Call;
import com.samsung.android.ims.cmc.ISemCmcRecordingListener;
import com.samsung.android.ims.cmc.SemCmcRecordingInfo;
import com.sec.epdg.EpdgManager;
import com.sec.ims.DialogEvent;
import com.sec.ims.IDialogEventListener;
import com.sec.ims.IRttEventListener;
import com.sec.ims.cmc.CmcCallInfo;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.ImsCallInfo;
import com.sec.internal.ims.servicemodules.volte2.ImsCallSessionManager;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;

public interface IVolteServiceModule extends IServiceModule {
    void consultativeTransferCall(int i, int i2);

    IImsCallSession createSession(CallProfile callProfile) throws RemoteException;

    IImsCallSession createSession(CallProfile callProfile, int i) throws RemoteException;

    int getActiveDataPhoneId();

    CmcCallInfo getCmcCallInfo();

    ICmcServiceHelper getCmcServiceHelper();

    int getDowngradedCallCount(int i);

    int getE911CallCount(int i);

    int getEpdgCallCount(int i);

    EpdgManager getEpdgManager();

    int getEpsFbCallCount(int i);

    boolean getExtMoCall();

    IImsCallSession getForegroundSession();

    IIdcServiceHelper getIdcServiceHelper();

    ImsCallInfo[] getImsCallInfos(int i);

    ImsCallSessionManager getImsCallSessionManager();

    int getIncomingSessionPhoneIdForCmc();

    DialogEvent getLastDialogEvent(int i);

    ImsUri getNormalizedUri(int i, String str);

    int getNrSaCallCount(int i);

    int getParticipantIdForMerge(int i, int i2);

    IImsCallSession getPendingSession(String str);

    int getRttMode();

    int getRttMode(int i);

    IImsCallSession getSessionByCallId(int i);

    int getSessionCount();

    int getSessionCount(int i);

    int getSignalLevel();

    int getTotalCallCount(int i);

    String getTrn(String str, String str2);

    int getTtyMode();

    int getTtyMode(int i);

    int getVideoCallCount(int i);

    int getVoWIFIEmergencyCallRat(int i);

    boolean hasActiveCall(int i);

    boolean hasCsCall(int i);

    boolean hasEmergencyCall(int i);

    boolean hasPendingCall(int i);

    boolean hasQecInCall();

    boolean isNotifyRejectedCall(int i);

    boolean isQSSSuccessAuthAndLogin(int i);

    boolean isRttCall(int i);

    boolean isVolteRetryRequired(int i, int i2, SipError sipError);

    boolean isVolteRetryRequired(int i, int i2, SipError sipError, int i3);

    boolean isVolteServiceStatus(int i);

    boolean isVolteSupportECT();

    boolean isVolteSupportECT(int i);

    void notifyEpsFallbackResult(int i, int i2);

    void onSrvccStateChange(int i, Call.SrvccState srvccState);

    void onUpdateGeolocation();

    void onVoWiFiSwitched(int i);

    void pushCall(int i, String str);

    void registerCmcRecordingListener(int i, ISemCmcRecordingListener iSemCmcRecordingListener);

    void registerDialogEventListener(int i, IDialogEventListener iDialogEventListener);

    void registerRttEventListener(int i, IRttEventListener iRttEventListener);

    void sendCmcRecordingEvent(int i, int i2, SemCmcRecordingInfo semCmcRecordingInfo);

    void sendDtmfEvent(int i, String str);

    void sendHandOffEvent(int i, int i2, int i3, int i4, long j);

    void sendRttMessage(String str);

    int sendRttSessionModifyRequest(int i, boolean z);

    void sendRttSessionModifyResponse(int i, boolean z);

    void setActiveImpu(int i, String str);

    void setAutomaticMode(int i, boolean z);

    void setDelayedDeregisterTimerRunning(int i, boolean z);

    void setOutOfService(boolean z, int i);

    void setRttMode(int i);

    void setRttMode(int i, int i2);

    void setTtyMode(int i, int i2);

    void setUiTTYMode(int i, int i2, Message message);

    void setVideoCrtAudio(int i, boolean z);

    int startLocalRingBackTone(int i, int i2, int i3);

    int stopLocalRingBackTone();

    void transferCall(String str, String str2) throws RemoteException;

    void unregisterCmcRecordingListener(int i, ISemCmcRecordingListener iSemCmcRecordingListener);

    void unregisterDialogEventListener(int i, IDialogEventListener iDialogEventListener);

    void unregisterRttEventListener(int i, IRttEventListener iRttEventListener);

    void updateAudioInterface(int i, int i2);

    void updateSSACInfo(int i, int i2, int i3, int i4, int i5);
}
