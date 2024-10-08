package com.sec.internal.interfaces.ims.servicemodules.quantumencryption;

import android.content.Context;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;
import com.voltecrypt.service.SXICTQMVoLTECallBack;
import com.voltecrypt.service.SxHangUpEntity;
import com.voltecrypt.service.SxRequestAuthenticationEntity;
import com.voltecrypt.service.SxRequestPeerProfileEntity;
import com.voltecrypt.service.SxRequestQMKeyEntity;

public interface IQuantumEncryptionServiceModule extends IServiceModule {
    public static final int EVT_QUANTUM_SECURITY_NOTIFY_AUTH_STATUS = 1;
    public static final int EVT_QUANTUM_SECURITY_NOTIFY_LOGIN_RESULT = 2;
    public static final int EVT_QUANTUM_SECURITY_NOTIFY_PEER_PROFILE_STATUS = 3;
    public static final int EVT_QUANTUM_SECURITY_NOTIFY_QMKEY_STATUS = 4;
    public static final int EVT_QUANTUM_SECURITY_NOTIFY_VOLTE_STATUS = 5;

    Context getContext();

    boolean isSuccessAuthAndLogin();

    int notifyAuthenticationStatus(int i, String str, String str2);

    void notifyLoginResult(int i, String str);

    int notifyPeerProfileStatus(int i, String str, String str2, String str3);

    int notifyQMKeyStatus(int i, String str, String str2, byte[] bArr, String str3);

    int notifyVoLTEStatus(int i, String str);

    int onGetVoLTEStatus();

    int onGetVoLTEStatusComment();

    void onHangUp(SxHangUpEntity sxHangUpEntity);

    void onRequestAuthentication(SxRequestAuthenticationEntity sxRequestAuthenticationEntity);

    int onRequestPeerProfileStatus(SxRequestPeerProfileEntity sxRequestPeerProfileEntity);

    int onRequestQMKey(SxRequestQMKeyEntity sxRequestQMKeyEntity);

    int onRequestQMKeyWithDelay(SxRequestQMKeyEntity sxRequestQMKeyEntity, int i);

    int registerVoLTECallback(SXICTQMVoLTECallBack sXICTQMVoLTECallBack);

    void resetAuthStatus();
}
