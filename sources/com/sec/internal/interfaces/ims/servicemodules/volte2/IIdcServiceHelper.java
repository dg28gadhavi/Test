package com.sec.internal.interfaces.ims.servicemodules.volte2;

import com.sec.internal.ims.servicemodules.volte2.ImsCallSession;
import com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra;

public interface IIdcServiceHelper {
    void createBootstrapDataChannel(int i);

    void finishIDCApp();

    String getBootstrapLocalSdp(int i);

    String getNegotiatedLocalSdp(String str);

    void notifyCallEnded(int i, int i2);

    void notifyCallEstablished(int i);

    void notifyErrorToSdpOffer(ImsCallSession imsCallSession, IdcExtra idcExtra);

    void onImsIncomingCallIdcEvent(ImsCallSession imsCallSession, IdcExtra idcExtra);

    void onImsOutgoingCallIdcEvent(ImsCallSession imsCallSession);

    void receiveSdpAnswer(int i, IdcExtra idcExtra);

    void receiveSdpOffer(int i, IdcExtra idcExtra);

    void setBootstrapRemoteAnswerSdp(String str, IdcExtra idcExtra);

    void setBootstrapRemoteOfferSdp(int i, IdcExtra idcExtra);

    void setTelecomCallId(String str, String str2);
}
