package com.sec.internal.ims.servicemodules.volte2.idc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.imsdcservice.IImsDataChannelCallback;
import com.samsung.android.imsdcservice.IImsDataChannelServiceController;
import com.sec.ims.ImsRegistration;
import com.sec.internal.ims.core.imsdc.IdcImsCallSessionData;
import com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal;
import com.sec.internal.ims.servicemodules.volte2.ImsCallSession;
import com.sec.internal.ims.servicemodules.volte2.ImsCallSessionManager;
import com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IIdcServiceHelper;

public class IdcServiceHelper extends Handler implements IIdcServiceHelper {
    private static final int EVT_TRY_RECONNECT = 1;
    private static final String LOG_TAG = "[IDC][FW]IdcServiceHelper";
    private static final String SERVICE_CLASS_NAME = "com.samsung.android.imsdcservice.SecImsDataChannelService";
    private static final String SERVICE_PACKAGE_NAME = "com.samsung.android.imsdcservice";
    private static IImsDataChannelCallback mImsDataChannelCallback;
    protected static IImsDataChannelServiceController mServiceBinder;
    protected final Context mContext;
    protected ImsCallSessionManager mImsCallSessionManager;
    protected IVolteServiceModuleInternal mModule;
    ServiceConnection mServiceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            IdcServiceHelper.mServiceBinder = IImsDataChannelServiceController.Stub.asInterface(iBinder);
            IdcServiceHelper.this.onConnected();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(IdcServiceHelper.LOG_TAG, "onServiceDisconnected : " + componentName);
            IdcServiceHelper.mServiceBinder = null;
            IdcServiceHelper.this.mState = DcMgrState.IDLE;
            IdcServiceHelper.this.sendEmptyMessageDelayed(1, 1000);
        }
    };
    /* access modifiers changed from: private */
    public DcMgrState mState = DcMgrState.IDLE;

    private enum DcMgrState {
        IDLE,
        SERVICE_CONNECTING,
        SERVICE_CONNECTED,
        REQUESTING
    }

    public void finishIDCApp() {
    }

    public void notifyCallEstablished(int i) {
    }

    private class ImsDataChannelCallback extends IImsDataChannelCallback.Stub {
        private ImsDataChannelCallback() {
        }

        public void sendSdpOffer(int i, String str, int i2, String str2) throws RemoteException {
            Log.i(IdcServiceHelper.LOG_TAG, "sendSdpOffer reqId : " + i + " telecomCallId : " + str + " sdpOffer : " + str2);
            ImsCallSession sessionByTelecomCallId = IdcServiceHelper.this.mImsCallSessionManager.getSessionByTelecomCallId(str);
            if (sessionByTelecomCallId != null) {
                sessionByTelecomCallId.reInviteIdc(i, str2);
            }
        }

        public void sendSdpAnswer(String str, int i, String str2) throws RemoteException {
            Log.i(IdcServiceHelper.LOG_TAG, "sendSdpAnswer telecomCallId : " + str + " sdpAnswer : " + str2);
            ImsCallSession sessionByTelecomCallId = IdcServiceHelper.this.mImsCallSessionManager.getSessionByTelecomCallId(str);
            if (sessionByTelecomCallId != null) {
                sessionByTelecomCallId.acceptIdc(str2);
            }
        }

        public void sendCallEnd(String str) throws RemoteException {
            Log.i(IdcServiceHelper.LOG_TAG, "sendCallEnd telecomCallId : " + str);
            ImsCallSession sessionByTelecomCallId = IdcServiceHelper.this.mImsCallSessionManager.getSessionByTelecomCallId(str);
            if (sessionByTelecomCallId != null) {
                try {
                    sessionByTelecomCallId.terminate(5);
                } catch (RemoteException unused) {
                    Log.i(IdcServiceHelper.LOG_TAG, "RemoteException on termiate callsession with id : ");
                }
            }
        }

        public void sendNegotiatedLocalSdp(String str, String str2) throws RemoteException {
            Log.i(IdcServiceHelper.LOG_TAG, "sendNegotiatedLocalSdp telecomCallId : " + str + " negotiatedLocalSdp : " + str2);
            ImsCallSession sessionByTelecomCallId = IdcServiceHelper.this.mImsCallSessionManager.getSessionByTelecomCallId(str);
            if (TextUtils.isEmpty(str2)) {
                str2 = IdcImsCallSessionData.NO_DATA;
            }
            if (sessionByTelecomCallId != null) {
                sessionByTelecomCallId.sendNegotiatedLocalSdp(str2);
            }
        }
    }

    public IdcServiceHelper(IVolteServiceModuleInternal iVolteServiceModuleInternal, Looper looper, Context context, ImsCallSessionManager imsCallSessionManager) {
        super(looper);
        this.mContext = context;
        this.mModule = iVolteServiceModuleInternal;
        this.mImsCallSessionManager = imsCallSessionManager;
    }

    public void init() {
        this.mState = DcMgrState.IDLE;
    }

    private void connectToDcManager() {
        Log.i(LOG_TAG, "DcManager state : " + this.mState);
        if (this.mState == DcMgrState.IDLE) {
            Log.i(LOG_TAG, "connectToDcManager");
            Intent intent = new Intent();
            intent.setClassName(SERVICE_PACKAGE_NAME, SERVICE_CLASS_NAME);
            this.mContext.bindService(intent, this.mServiceConn, 1);
            this.mState = DcMgrState.SERVICE_CONNECTING;
        }
    }

    /* access modifiers changed from: private */
    public void onConnected() {
        Log.i(LOG_TAG, "onConnected()");
        if (mServiceBinder != null) {
            this.mState = DcMgrState.SERVICE_CONNECTED;
            ImsDataChannelCallback imsDataChannelCallback = new ImsDataChannelCallback();
            mImsDataChannelCallback = imsDataChannelCallback;
            try {
                mServiceBinder.setImsDataChannelCallback(imsDataChannelCallback, (String) null);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setBootstrapRemoteAnswerSdp(String str, IdcExtra idcExtra) {
        Log.i(LOG_TAG, "setBootstrapRemoteAnswerSdp telecomCallId : " + str + " remoteAnswerSdp : " + idcExtra.getString(IdcExtra.Key.SDP));
        if (isBinderReady()) {
            String processingRFC8841v5 = processingRFC8841v5(idcExtra.getString(IdcExtra.Key.SDP));
            try {
                Log.i(LOG_TAG, "modifiedSDP : " + processingRFC8841v5);
                mServiceBinder.setBootstrapRemoteAnswerSdp(str, processingRFC8841v5);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void receiveSdpOffer(int i, IdcExtra idcExtra) {
        String telecomCallIdBySessionId = this.mImsCallSessionManager.getTelecomCallIdBySessionId(i);
        Log.i(LOG_TAG, "receiveSdpOffer telecomCallId : " + telecomCallIdBySessionId);
        try {
            mServiceBinder.receiveSdpOffer(telecomCallIdBySessionId, -1, idcExtra.getString(IdcExtra.Key.SDP), "");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void receiveSdpAnswer(int i, IdcExtra idcExtra) {
        String telecomCallIdBySessionId = this.mImsCallSessionManager.getTelecomCallIdBySessionId(i);
        Log.i(LOG_TAG, "receiveSdpAnswer telecomCallId : " + telecomCallIdBySessionId);
        try {
            mServiceBinder.receiveSdpAnswer(idcExtra.getInt(IdcExtra.Key.REQ_ID), telecomCallIdBySessionId, -1, idcExtra.getString(IdcExtra.Key.SDP));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void notifyErrorToSdpOffer(ImsCallSession imsCallSession, IdcExtra idcExtra) {
        int phoneId = imsCallSession.getPhoneId();
        String telecomCallIdBySessionId = this.mImsCallSessionManager.getTelecomCallIdBySessionId(imsCallSession.getSessionId());
        Log.i(LOG_TAG, "receiveSdpAnswer telecomCallId : " + telecomCallIdBySessionId + " reqId : " + idcExtra.getInt(IdcExtra.Key.REQ_ID));
        try {
            mServiceBinder.notifyErrorToSdpOffer(idcExtra.getInt(IdcExtra.Key.REQ_ID), phoneId, telecomCallIdBySessionId, idcExtra.getInt(IdcExtra.Key.SHOULD_RETRY) > 0);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleMessage(Message message) {
        if (message.what == 1) {
            connectToDcManager();
        }
    }

    public void createBootstrapDataChannel(int i) {
        Log.i(LOG_TAG, "createBootstrapDataChannel slotId : " + i);
        if (isBinderReady()) {
            try {
                mServiceBinder.createBootstrapDataChannel(i);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public String getBootstrapLocalSdp(int i) {
        Log.i(LOG_TAG, "getBootstrapLocalSdp slotId : " + i);
        if (!isBinderReady()) {
            return "";
        }
        try {
            return mServiceBinder.getBootstrapLocalSdp(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getNegotiatedLocalSdp(String str) {
        Log.i(LOG_TAG, "getNegotiatedLocalSdp telecomCallId : " + str);
        if (!isBinderReady()) {
            return "";
        }
        try {
            return mServiceBinder.getNegotiatedLocalSdp(str);
        } catch (RemoteException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void setTelecomCallId(String str, String str2) {
        Log.i(LOG_TAG, "setTelecomCallId tlsId : " + str + " telecomCallId : " + str2);
        if (isBinderReady()) {
            try {
                mServiceBinder.setTelecomCallId(str, str2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setBootstrapRemoteOfferSdp(int i, IdcExtra idcExtra) {
        if (isBinderReady()) {
            try {
                String processingRFC8841v5 = processingRFC8841v5(idcExtra.getString(IdcExtra.Key.SDP));
                Log.i(LOG_TAG, "modifiedSDP : " + processingRFC8841v5);
                mServiceBinder.setBootstrapRemoteOfferSdp(i, processingRFC8841v5);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void onImsOutgoingCallIdcEvent(ImsCallSession imsCallSession) {
        Log.i(LOG_TAG, "onImsOutgoingCallIdcEvent");
        createBootstrapDataChannel(imsCallSession.getPhoneId());
        String bootstrapLocalSdp = getBootstrapLocalSdp(imsCallSession.getPhoneId());
        Log.i(LOG_TAG, "localSdp : " + bootstrapLocalSdp);
        imsCallSession.getIdcData().setLocalBdcSdp(bootstrapLocalSdp);
        setLocalBdcTlsId(imsCallSession, bootstrapLocalSdp);
        if (!imsCallSession.getIdcData().getIsNotifiedTelecomCallId()) {
            setTelecomCallId(imsCallSession.getIdcData().getLocalBdcTlsId(), imsCallSession.getIdcData().getTelecomCallId());
            imsCallSession.getIdcData().setIsNotifiedTelecomCallId(true);
        }
    }

    public void onImsIncomingCallIdcEvent(ImsCallSession imsCallSession, IdcExtra idcExtra) {
        Log.i(LOG_TAG, "onImsIncomingCallIdcEvent");
        createBootstrapDataChannel(imsCallSession.getPhoneId());
        setBootstrapRemoteOfferSdp(imsCallSession.getPhoneId(), idcExtra);
        String bootstrapLocalSdp = getBootstrapLocalSdp(imsCallSession.getPhoneId());
        imsCallSession.getIdcData().setLocalBdcSdp(bootstrapLocalSdp);
        setLocalBdcTlsId(imsCallSession, bootstrapLocalSdp);
    }

    public void notifyCallEnded(int i, int i2) {
        Log.i(LOG_TAG, "notifyCallEnded slotId : " + i + " sessionId : " + i2);
        if (isBinderReady()) {
            try {
                String telecomCallIdBySessionId = this.mImsCallSessionManager.getTelecomCallIdBySessionId(i2);
                Log.i(LOG_TAG, "notifyCallEnded telecomCallId : " + telecomCallIdBySessionId);
                mServiceBinder.endImsDataChannel(i, telecomCallIdBySessionId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: protected */
    public String processingRFC8841v5(String str) {
        if (TextUtils.isEmpty(str)) {
            Log.i(LOG_TAG, "processingRFC8841v5: sdp is null or empty");
            return str;
        }
        boolean contains = str.contains("UDP/DTLS/SCTP webrtc-datachannel");
        if (contains) {
            Log.i(LOG_TAG, "processingRFC8841v5: isRFC8841currentVersion? " + contains);
            return str;
        }
        Log.i(LOG_TAG, "practice processingRFC8841v5()");
        StringBuffer stringBuffer = new StringBuffer();
        for (String str2 : str.split("\\r?\\n")) {
            if (str2.contains(" DTLS/SCTP ")) {
                String replace = str2.replace("DTLS/SCTP", "UDP/DTLS/SCTP webrtc-datachannel");
                stringBuffer.append(replace.substring(0, replace.indexOf("datachannel") + 11));
                stringBuffer.append("\r\n");
            } else if (str2.contains("sctpmap")) {
                String replace2 = str2.replace("sctpmap", "sctp-port");
                stringBuffer.append(replace2.substring(0, replace2.indexOf("webrtc") - 1));
                stringBuffer.append("\r\n");
            } else {
                stringBuffer.append(str2);
                stringBuffer.append("\r\n");
            }
        }
        return stringBuffer.toString();
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        if ((imsRegistration != null && this.mModule.isSupportImsDataChannel(imsRegistration.getPhoneId())) && !isBinderReady()) {
            connectToDcManager();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isBinderReady() {
        if (this.mState == DcMgrState.SERVICE_CONNECTED && mServiceBinder != null) {
            return true;
        }
        Log.i(LOG_TAG, "Binder not ready mState: " + this.mState);
        return false;
    }

    private void setLocalBdcTlsId(ImsCallSession imsCallSession, String str) {
        if (!TextUtils.isEmpty(str)) {
            for (String str2 : str.split("\r\n")) {
                if (str2.contains("a=tls-id:")) {
                    String substring = str2.substring(9);
                    imsCallSession.getIdcData().setLocalBdcTlsId(substring);
                    Log.i(LOG_TAG, "setLocalBdcTlsId: " + substring);
                    return;
                }
            }
        }
    }
}
