package com.sec.sve;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Network;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.Surface;
import com.sec.sve.ISecVideoEngineService;

public class SecVideoEngineManager {
    /* access modifiers changed from: private */
    public final String LOG_TAG = SecVideoEngineManager.class.getSimpleName();
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(SecVideoEngineManager.this.LOG_TAG, "Connected");
            SecVideoEngineManager.this.mService = ISecVideoEngineService.Stub.asInterface(iBinder);
            SecVideoEngineManager.this.mListener.onConnected();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(SecVideoEngineManager.this.LOG_TAG, "Disconnected");
            SecVideoEngineManager.this.mService = null;
            SecVideoEngineManager.this.mListener.onDisconnected();
        }
    };
    private final Context mContext;
    /* access modifiers changed from: private */
    public final ConnectionListener mListener;
    /* access modifiers changed from: private */
    public ISecVideoEngineService mService;

    public interface ConnectionListener {
        void onConnected();

        void onDisconnected();
    }

    public SecVideoEngineManager(Context context, ConnectionListener connectionListener) {
        this.mContext = context;
        this.mListener = connectionListener;
    }

    public void connectService() {
        try {
            if (this.mService == null) {
                Intent intent = new Intent();
                intent.setClassName("com.sec.sve", "com.sec.sve.service.SecVideoEngineService");
                this.mContext.bindServiceAsUser(intent, this.mConnection, 1, UserHandle.CURRENT);
            }
        } catch (SecurityException unused) {
        }
    }

    public void disconnectService() {
        Context context = this.mContext;
        if (context != null) {
            context.unbindService(this.mConnection);
            this.mService = null;
        }
    }

    public void bindToNetwork(Network network) {
        if (this.mService == null) {
            Log.e(this.LOG_TAG, "SVE service is not ready!");
            return;
        }
        String str = this.LOG_TAG;
        Log.d(str, "bindToNetwork " + network);
        try {
            this.mService.bindToNetwork(network);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setPreviewSurface(int i, Surface surface, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.setPreviewSurface(i, surface, i2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setDisplaySurface(int i, Surface surface, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.setDisplaySurface(i, surface, i2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOrientation(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.setOrientation(i);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setZoom(float f) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.setZoom(f);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void switchCamera() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.switchCamera();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPreviewResolution(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.setPreviewResolution(i, i2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendStillImage(int i, boolean z, String str, String str2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.sendStillImage(i, z, str, str2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setCameraEffect(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.setCameraEffect(i);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void saeInitialize(int i, int i2, int i3) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.saeInitialize(i, i2, i3);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void saeTerminate() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.saeTerminate();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int saeSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetCodecInfo(i, str, i2, i3, i4, i5, i6, i7, z, i8, i9, i10, i11, i12, c, c2, c3, c4, c5, c6, i13, i14, str2, str3, str4, str5, str6, str7, str8, str9);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeCreateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6, String str3, boolean z, boolean z2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeCreateChannel(i, i2, str, i3, str2, i4, i5, i6, str3, z, z2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeStartChannel(int i, int i2, boolean z) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeStartChannel(i, i2, z);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeUpdateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeUpdateChannel(i, i2, str, i3, str2, i4, i5, i6);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeStopChannel(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeStopChannel(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeModifyChannel(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeModifyChannel(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeDeleteChannel(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeDeleteChannel(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeHandleDtmf(int i, int i2, int i3, int i4) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeHandleDtmf(i, i2, i3, i4);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeSetDtmfCodecInfo(int i, int i2, int i3, int i4, int i5) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetDtmfCodecInfo(i, i2, i3, i4, i5);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeEnableSRTP(i, i2, i3, bArr, i4);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeSetRtcpOnCall(int i, int i2, int i3) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetRtcpOnCall(i, i2, i3);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeSetRtpTimeout(int i, long j) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetRtpTimeout(i, j);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeSetRtcpTimeout(int i, long j) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetRtcpTimeout(i, j);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetRtcpXr(i, i2, i3, i4, i5, iArr);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public TimeInfo saeGetLastPlayedVoiceTime(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return null;
        }
        try {
            return iSecVideoEngineService.saeGetLastPlayedVoiceTime(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int saeSetVoicePlayDelay(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetVoicePlayDelay(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeSetTOS(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetTOS(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeGetVersion(byte[] bArr, int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeGetVersion(bArr, i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeGetAudioRxTrackId(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeGetAudioRxTrackId(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeSetAudioPath(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeSetAudioPath(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveCreateChannel() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveCreateChannel();
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveStartChannel(int i, int i2, int i3) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveStartChannel(i, i2, i3);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveStopChannel(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveStopChannel(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSetConnection(int i, String str, int i2, String str2, int i3, int i4, int i5, int i6, long j) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSetConnection(i, str, i2, str2, i3, i4, i5, i6, j);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSetCodecInfo(int i, int i2, int i3, int i4, int i5, int i6, String str, int i7, int i8, int i9, int i10, int i11, boolean z, int i12, boolean z2, int i13, int i14, int i15, int i16, int i17, byte[] bArr, byte[] bArr2, byte[] bArr3, int i18, int i19, int i20) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSetCodecInfo(i, i2, i3, i4, i5, i6, str, i7, i8, i9, i10, i11, z, i12, z2, i13, i14, i15, i16, i17, bArr, bArr2, bArr3, i18, i19, i20);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSetSRTPParams(int i, String str, byte[] bArr, int i2, int i3, int i4, int i5, String str2, byte[] bArr2, int i6, int i7, int i8, int i9) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSetSRTPParams(i, str, bArr, i2, i3, i4, i5, str2, bArr2, i6, i7, i8, i9);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSetSRTPParams(int i, int i2, int i3, int i4, char c, int i5, byte[] bArr, int i6, byte[] bArr2, int i7) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            Log.e(this.LOG_TAG, "SVE service is not ready!");
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSetGcmSrtpParams(i, i2, i3, i4, c, i5, bArr, i6, bArr2, i7);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSetMediaConfig(int i, boolean z, int i2, boolean z2, int i3, int i4, int i5) {
        return sveSetMediaConfig(i, z, i2, z2, i3, i4, i5, 2000);
    }

    public int sveSetMediaConfig(int i, boolean z, int i2, boolean z2, int i3, int i4, int i5, int i6) {
        if (this.mService == null) {
            return -1;
        }
        String str = this.LOG_TAG;
        Log.d(str, "sveSetMediaConfig keepAliveInterval " + i6);
        try {
            return this.mService.sveSetMediaConfig(i, z, i2, z2, i3, i4, i5, i6);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveStartCamera(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            Log.e(this.LOG_TAG, "SVE service is not ready!");
            return -1;
        }
        try {
            return iSecVideoEngineService.sveStartCamera(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveStopCamera() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveStopCamera();
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSetHeldInfo(int i, boolean z, boolean z2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSetHeldInfo(i, z, z2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public TimeInfo sveGetLastPlayedVideoTime(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return null;
        }
        try {
            return iSecVideoEngineService.sveGetLastPlayedVideoTime(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String sveGetCodecCapacity(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return null;
        }
        try {
            return iSecVideoEngineService.sveGetCodecCapacity(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int sveSetVideoPlayDelay(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSetVideoPlayDelay(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSetNetworkQoS(int i, int i2, int i3, int i4) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSetNetworkQoS(i, i2, i3, i4);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveSendGeneralEvent(int i, int i2, int i3, String str) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveSendGeneralEvent(i, i2, i3, str);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void sendGeneralBundleEvent(String str, Bundle bundle) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.sendGeneralBundleEvent(str, bundle);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public TimeInfo sveGetRtcpTimeInfo(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return null;
        }
        try {
            return iSecVideoEngineService.sveGetRtcpTimeInfo(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void registerForMediaEventListener(IImsMediaEventListener iImsMediaEventListener) {
        Log.d(this.LOG_TAG, "registerForMediaEventListener");
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.registerForMediaEventListener(iImsMediaEventListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void unregisterForMediaEventListener(IImsMediaEventListener iImsMediaEventListener) {
        Log.d(this.LOG_TAG, "unregisterForMediaEventListener");
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.unregisterForMediaEventListener(iImsMediaEventListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerForCmcEventListener(ICmcMediaEventListener iCmcMediaEventListener) {
        Log.d(this.LOG_TAG, "registerForCmcEventListener");
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.registerForCmcEventListener(iCmcMediaEventListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void unregisterForCmcEventListener(ICmcMediaEventListener iCmcMediaEventListener) {
        Log.d(this.LOG_TAG, "unregisterForCmcEventListener");
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.unregisterForCmcEventListener(iCmcMediaEventListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void steInitialize() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.steInitialize();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int steSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetCodecInfo(i, str, i2, i3, i4, i5, i6, i7, z, i8, i9, i10, i11, i12, c, c2, c3, c4, c5, c6, i13, i14, str2, str3, str4, str5, str6, str7, str8, str9);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steCreateChannel(int i, String str, int i2, String str2, int i3, int i4, int i5, String str3, boolean z, boolean z2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steCreateChannel(i, str, i2, str2, i3, i4, i5, str3, z, z2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steStartChannel(int i, int i2, boolean z) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steStartChannel(i, i2, z);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steUpdateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steUpdateChannel(i, i2, str, i3, str2, i4, i5, i6);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steStopChannel(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steStopChannel(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steModifyChannel(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steModifyChannel(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetRtcpXr(i, i2, i3, i4, i5, iArr);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSetRtpTimeout(int i, long j) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetRtpTimeout(i, j);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSetCallOptions(int i, boolean z) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetCallOptions(i, z);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSetNetId(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetNetId(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steDeleteChannel(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steDeleteChannel(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSendText(int i, String str, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSendText(i, str, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steEnableSRTP(i, i2, i3, bArr, i4);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSetRtcpOnCall(int i, int i2, int i3) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetRtcpOnCall(i, i2, i3);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSetRtcpTimeout(int i, long j) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetRtcpTimeout(i, j);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int steSetSessionId(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.steSetSessionId(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void sreInitialize() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.sreInitialize();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public String sreGetVersion() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return "";
        }
        try {
            return iSecVideoEngineService.sreGetVersion();
        } catch (RemoteException e) {
            e.printStackTrace();
            return "";
        }
    }

    public int sreSetMdmn(int i, boolean z) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetMdmn(i, z);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean sreGetMdmn(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return false;
        }
        try {
            return iSecVideoEngineService.sreGetMdmn(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int sreSetNetId(int i, long j) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetNetId(i, j);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreCreateStream(int i, int i2, int i3, String str, int i4, String str2, int i5, boolean z, boolean z2, int i6, int i7, String str3, boolean z3, boolean z4) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreCreateStream(i, i2, i3, str, i4, str2, i5, z, z2, i6, i7, str3, z3, z4);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreStartStream(int i, int i2, int i3) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreStartStream(i, i2, i3);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreDeleteStream(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreDeleteStream(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreUpdateStream(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreUpdateStream(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreCreateRelayChannel(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreCreateRelayChannel(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreDeleteRelayChannel(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreDeleteRelayChannel(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreStartRelayChannel(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreStartRelayChannel(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreStopRelayChannel(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreStopRelayChannel(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreHoldRelayChannel(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreHoldRelayChannel(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreResumeRelayChannel(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreResumeRelayChannel(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreUpdateRelayChannel(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreUpdateRelayChannel(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreSetConnection(int i, String str, int i2, String str2, int i3, int i4, int i5, int i6) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetConnection(i, str, i2, str2, i3, i4, i5, i6);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreEnableSRTP(i, i2, i3, bArr, i4);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreSetRtcpOnCall(int i, int i2, int i3, int i4, int i5) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetRtcpOnCall(i, i2, i3, i4, i5);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreSetRtpTimeout(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetRtpTimeout(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreSetRtcpTimeout(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetRtcpTimeout(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetRtcpXr(i, i2, i3, i4, i5, iArr);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, int i15) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetCodecInfo(i, str, i2, i3, i4, i5, i6, i7, z, i8, i9, i10, i11, i12, c, c2, c3, c4, c5, c6, i13, i14, str2, str3, str4, str5, str6, str7, str8, str9, i15);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreSetDtmfCodecInfo(int i, int i2, int i3, int i4, int i5, int i6) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreSetDtmfCodecInfo(i, i2, i3, i4, i5, i6);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreStartRecording(int i, int i2, int i3) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreStartRecording(i, i2, i3);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sreStopRecording(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sreStopRecording(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean isSupportingCameraMotor() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return false;
        }
        try {
            return iSecVideoEngineService.isSupportingCameraMotor();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int sveRecorderCreate(int i, String str, int i2, int i3, String str2, int i4) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveRecorderCreate(i, str, i2, i3, str2, i4);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveCmcRecorderCreate(int i, int i2, int i3, String str, int i4, int i5, long j, int i6, String str2, int i7, int i8, int i9, int i10, int i11, long j2, String str3) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveCmcRecorderCreate(i, i2, i3, str, i4, i5, j, i6, str2, i7, i8, i9, i10, i11, j2, str3);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveRecorderDelete(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveRecorderDelete(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveRecorderStart(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveRecorderStart(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveRecorderStop(int i, boolean z) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveRecorderStop(i, z);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeStartRecording(int i, int i2, int i3, boolean z) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeStartRecording(i, i2, i3, z);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int saeStopRecording(int i, boolean z) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.saeStopRecording(i, z);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveStartRecording(int i, int i2) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveStartRecording(i, i2);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int sveStopRecording(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            return -1;
        }
        try {
            return iSecVideoEngineService.sveStopRecording(i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void startEmoji(int i, String str) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.sveStartEmoji(i, str);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopEmoji(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.sveStopEmoji(i);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void restartEmoji(int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService != null) {
            try {
                iSecVideoEngineService.sveRestartEmoji(i);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int cpveStartInjection(String str, int i) {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            Log.e(this.LOG_TAG, "SVE service is not ready!");
            return -1;
        }
        try {
            return iSecVideoEngineService.cpveStartInjection(str, i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int cpveStopInjection() {
        ISecVideoEngineService iSecVideoEngineService = this.mService;
        if (iSecVideoEngineService == null) {
            Log.e(this.LOG_TAG, "SVE service is not ready!");
            return -1;
        }
        try {
            return iSecVideoEngineService.cpveStopInjection();
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
