package com.sec.sve;

import android.net.Network;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;

public interface ISecVideoEngineService extends IInterface {
    public static final String DESCRIPTOR = "com.sec.sve.ISecVideoEngineService";

    public static class Default implements ISecVideoEngineService {
        public IBinder asBinder() {
            return null;
        }

        public void bindToNetwork(Network network) throws RemoteException {
        }

        public int cpveStartInjection(String str, int i) throws RemoteException {
            return 0;
        }

        public int cpveStopInjection() throws RemoteException {
            return 0;
        }

        public boolean isSupportingCameraMotor() throws RemoteException {
            return false;
        }

        public void onDestroy() throws RemoteException {
        }

        public void registerForCmcEventListener(ICmcMediaEventListener iCmcMediaEventListener) throws RemoteException {
        }

        public void registerForMediaEventListener(IImsMediaEventListener iImsMediaEventListener) throws RemoteException {
        }

        public int saeCreateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6, String str3, boolean z, boolean z2) throws RemoteException {
            return 0;
        }

        public int saeDeleteChannel(int i) throws RemoteException {
            return 0;
        }

        public int saeEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) throws RemoteException {
            return 0;
        }

        public int saeGetAudioRxTrackId(int i) throws RemoteException {
            return 0;
        }

        public TimeInfo saeGetLastPlayedVoiceTime(int i) throws RemoteException {
            return null;
        }

        public int saeGetVersion(byte[] bArr, int i) throws RemoteException {
            return 0;
        }

        public int saeHandleDtmf(int i, int i2, int i3, int i4) throws RemoteException {
            return 0;
        }

        public void saeInitialize(int i, int i2, int i3) throws RemoteException {
        }

        public int saeModifyChannel(int i, int i2) throws RemoteException {
            return 0;
        }

        public int saeSetAudioPath(int i, int i2) throws RemoteException {
            return 0;
        }

        public int saeSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) throws RemoteException {
            return 0;
        }

        public int saeSetDtmfCodecInfo(int i, int i2, int i3, int i4, int i5) throws RemoteException {
            return 0;
        }

        public int saeSetRtcpOnCall(int i, int i2, int i3) throws RemoteException {
            return 0;
        }

        public int saeSetRtcpTimeout(int i, long j) throws RemoteException {
            return 0;
        }

        public int saeSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) throws RemoteException {
            return 0;
        }

        public int saeSetRtpTimeout(int i, long j) throws RemoteException {
            return 0;
        }

        public int saeSetTOS(int i, int i2) throws RemoteException {
            return 0;
        }

        public int saeSetVoicePlayDelay(int i, int i2) throws RemoteException {
            return 0;
        }

        public int saeStartChannel(int i, int i2, boolean z) throws RemoteException {
            return 0;
        }

        public int saeStartRecording(int i, int i2, int i3, boolean z) throws RemoteException {
            return 0;
        }

        public int saeStopChannel(int i) throws RemoteException {
            return 0;
        }

        public int saeStopRecording(int i, boolean z) throws RemoteException {
            return 0;
        }

        public void saeTerminate() throws RemoteException {
        }

        public int saeUpdateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6) throws RemoteException {
            return 0;
        }

        public void sendGeneralBundleEvent(String str, Bundle bundle) throws RemoteException {
        }

        public void sendStillImage(int i, boolean z, String str, String str2) throws RemoteException {
        }

        public void setCameraEffect(int i) throws RemoteException {
        }

        public void setDisplaySurface(int i, Surface surface, int i2) throws RemoteException {
        }

        public void setOrientation(int i) throws RemoteException {
        }

        public void setPreviewResolution(int i, int i2) throws RemoteException {
        }

        public void setPreviewSurface(int i, Surface surface, int i2) throws RemoteException {
        }

        public void setZoom(float f) throws RemoteException {
        }

        public int sreCreateRelayChannel(int i, int i2) throws RemoteException {
            return 0;
        }

        public int sreCreateStream(int i, int i2, int i3, String str, int i4, String str2, int i5, boolean z, boolean z2, int i6, int i7, String str3, boolean z3, boolean z4) throws RemoteException {
            return 0;
        }

        public int sreDeleteRelayChannel(int i) throws RemoteException {
            return 0;
        }

        public int sreDeleteStream(int i) throws RemoteException {
            return 0;
        }

        public int sreEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) throws RemoteException {
            return 0;
        }

        public boolean sreGetMdmn(int i) throws RemoteException {
            return false;
        }

        public String sreGetVersion() throws RemoteException {
            return null;
        }

        public int sreHoldRelayChannel(int i) throws RemoteException {
            return 0;
        }

        public void sreInitialize() throws RemoteException {
        }

        public int sreResumeRelayChannel(int i) throws RemoteException {
            return 0;
        }

        public int sreSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, int i15) throws RemoteException {
            return 0;
        }

        public int sreSetConnection(int i, String str, int i2, String str2, int i3, int i4, int i5, int i6) throws RemoteException {
            return 0;
        }

        public int sreSetDtmfCodecInfo(int i, int i2, int i3, int i4, int i5, int i6) throws RemoteException {
            return 0;
        }

        public int sreSetMdmn(int i, boolean z) throws RemoteException {
            return 0;
        }

        public int sreSetNetId(int i, long j) throws RemoteException {
            return 0;
        }

        public int sreSetRtcpOnCall(int i, int i2, int i3, int i4, int i5) throws RemoteException {
            return 0;
        }

        public int sreSetRtcpTimeout(int i, int i2) throws RemoteException {
            return 0;
        }

        public int sreSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) throws RemoteException {
            return 0;
        }

        public int sreSetRtpTimeout(int i, int i2) throws RemoteException {
            return 0;
        }

        public int sreStartRecording(int i, int i2, int i3) throws RemoteException {
            return 0;
        }

        public int sreStartRelayChannel(int i, int i2) throws RemoteException {
            return 0;
        }

        public int sreStartStream(int i, int i2, int i3) throws RemoteException {
            return 0;
        }

        public int sreStopRecording(int i, int i2) throws RemoteException {
            return 0;
        }

        public int sreStopRelayChannel(int i) throws RemoteException {
            return 0;
        }

        public int sreUpdateRelayChannel(int i, int i2) throws RemoteException {
            return 0;
        }

        public int sreUpdateStream(int i) throws RemoteException {
            return 0;
        }

        public int steCreateChannel(int i, String str, int i2, String str2, int i3, int i4, int i5, String str3, boolean z, boolean z2) throws RemoteException {
            return 0;
        }

        public int steDeleteChannel(int i) throws RemoteException {
            return 0;
        }

        public int steEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) throws RemoteException {
            return 0;
        }

        public void steInitialize() throws RemoteException {
        }

        public int steModifyChannel(int i, int i2) throws RemoteException {
            return 0;
        }

        public int steSendText(int i, String str, int i2) throws RemoteException {
            return 0;
        }

        public int steSetCallOptions(int i, boolean z) throws RemoteException {
            return 0;
        }

        public int steSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) throws RemoteException {
            return 0;
        }

        public int steSetNetId(int i, int i2) throws RemoteException {
            return 0;
        }

        public int steSetRtcpOnCall(int i, int i2, int i3) throws RemoteException {
            return 0;
        }

        public int steSetRtcpTimeout(int i, long j) throws RemoteException {
            return 0;
        }

        public int steSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) throws RemoteException {
            return 0;
        }

        public int steSetRtpTimeout(int i, long j) throws RemoteException {
            return 0;
        }

        public int steSetSessionId(int i, int i2) throws RemoteException {
            return 0;
        }

        public int steStartChannel(int i, int i2, boolean z) throws RemoteException {
            return 0;
        }

        public int steStopChannel(int i) throws RemoteException {
            return 0;
        }

        public int steUpdateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6) throws RemoteException {
            return 0;
        }

        public int sveCmcRecorderCreate(int i, int i2, int i3, String str, int i4, int i5, long j, int i6, String str2, int i7, int i8, int i9, int i10, int i11, long j2, String str3) throws RemoteException {
            return 0;
        }

        public int sveCreateChannel() throws RemoteException {
            return 0;
        }

        public String sveGetCodecCapacity(int i) throws RemoteException {
            return null;
        }

        public TimeInfo sveGetLastPlayedVideoTime(int i) throws RemoteException {
            return null;
        }

        public TimeInfo sveGetRtcpTimeInfo(int i) throws RemoteException {
            return null;
        }

        public int sveRecorderCreate(int i, String str, int i2, int i3, String str2, int i4) throws RemoteException {
            return 0;
        }

        public int sveRecorderDelete(int i) throws RemoteException {
            return 0;
        }

        public int sveRecorderStart(int i) throws RemoteException {
            return 0;
        }

        public int sveRecorderStop(int i, boolean z) throws RemoteException {
            return 0;
        }

        public void sveRestartEmoji(int i) throws RemoteException {
        }

        public int sveSendGeneralEvent(int i, int i2, int i3, String str) throws RemoteException {
            return 0;
        }

        public int sveSetCodecInfo(int i, int i2, int i3, int i4, int i5, int i6, String str, int i7, int i8, int i9, int i10, int i11, boolean z, int i12, boolean z2, int i13, int i14, int i15, int i16, int i17, byte[] bArr, byte[] bArr2, byte[] bArr3, int i18, int i19, int i20) throws RemoteException {
            return 0;
        }

        public int sveSetConnection(int i, String str, int i2, String str2, int i3, int i4, int i5, int i6, long j) throws RemoteException {
            return 0;
        }

        public int sveSetGcmSrtpParams(int i, int i2, int i3, int i4, char c, int i5, byte[] bArr, int i6, byte[] bArr2, int i7) throws RemoteException {
            return 0;
        }

        public int sveSetHeldInfo(int i, boolean z, boolean z2) throws RemoteException {
            return 0;
        }

        public int sveSetMediaConfig(int i, boolean z, int i2, boolean z2, int i3, int i4, int i5, int i6) throws RemoteException {
            return 0;
        }

        public int sveSetNetworkQoS(int i, int i2, int i3, int i4) throws RemoteException {
            return 0;
        }

        public int sveSetSRTPParams(int i, String str, byte[] bArr, int i2, int i3, int i4, int i5, String str2, byte[] bArr2, int i6, int i7, int i8, int i9) throws RemoteException {
            return 0;
        }

        public int sveSetVideoPlayDelay(int i, int i2) throws RemoteException {
            return 0;
        }

        public int sveStartCamera(int i, int i2) throws RemoteException {
            return 0;
        }

        public int sveStartChannel(int i, int i2, int i3) throws RemoteException {
            return 0;
        }

        public void sveStartEmoji(int i, String str) throws RemoteException {
        }

        public int sveStartRecording(int i, int i2) throws RemoteException {
            return 0;
        }

        public int sveStopCamera() throws RemoteException {
            return 0;
        }

        public int sveStopChannel(int i) throws RemoteException {
            return 0;
        }

        public void sveStopEmoji(int i) throws RemoteException {
        }

        public int sveStopRecording(int i) throws RemoteException {
            return 0;
        }

        public void switchCamera() throws RemoteException {
        }

        public void unregisterForCmcEventListener(ICmcMediaEventListener iCmcMediaEventListener) throws RemoteException {
        }

        public void unregisterForMediaEventListener(IImsMediaEventListener iImsMediaEventListener) throws RemoteException {
        }
    }

    void bindToNetwork(Network network) throws RemoteException;

    int cpveStartInjection(String str, int i) throws RemoteException;

    int cpveStopInjection() throws RemoteException;

    boolean isSupportingCameraMotor() throws RemoteException;

    void onDestroy() throws RemoteException;

    void registerForCmcEventListener(ICmcMediaEventListener iCmcMediaEventListener) throws RemoteException;

    void registerForMediaEventListener(IImsMediaEventListener iImsMediaEventListener) throws RemoteException;

    int saeCreateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6, String str3, boolean z, boolean z2) throws RemoteException;

    int saeDeleteChannel(int i) throws RemoteException;

    int saeEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) throws RemoteException;

    int saeGetAudioRxTrackId(int i) throws RemoteException;

    TimeInfo saeGetLastPlayedVoiceTime(int i) throws RemoteException;

    int saeGetVersion(byte[] bArr, int i) throws RemoteException;

    int saeHandleDtmf(int i, int i2, int i3, int i4) throws RemoteException;

    void saeInitialize(int i, int i2, int i3) throws RemoteException;

    int saeModifyChannel(int i, int i2) throws RemoteException;

    int saeSetAudioPath(int i, int i2) throws RemoteException;

    int saeSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) throws RemoteException;

    int saeSetDtmfCodecInfo(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    int saeSetRtcpOnCall(int i, int i2, int i3) throws RemoteException;

    int saeSetRtcpTimeout(int i, long j) throws RemoteException;

    int saeSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) throws RemoteException;

    int saeSetRtpTimeout(int i, long j) throws RemoteException;

    int saeSetTOS(int i, int i2) throws RemoteException;

    int saeSetVoicePlayDelay(int i, int i2) throws RemoteException;

    int saeStartChannel(int i, int i2, boolean z) throws RemoteException;

    int saeStartRecording(int i, int i2, int i3, boolean z) throws RemoteException;

    int saeStopChannel(int i) throws RemoteException;

    int saeStopRecording(int i, boolean z) throws RemoteException;

    void saeTerminate() throws RemoteException;

    int saeUpdateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6) throws RemoteException;

    void sendGeneralBundleEvent(String str, Bundle bundle) throws RemoteException;

    void sendStillImage(int i, boolean z, String str, String str2) throws RemoteException;

    void setCameraEffect(int i) throws RemoteException;

    void setDisplaySurface(int i, Surface surface, int i2) throws RemoteException;

    void setOrientation(int i) throws RemoteException;

    void setPreviewResolution(int i, int i2) throws RemoteException;

    void setPreviewSurface(int i, Surface surface, int i2) throws RemoteException;

    void setZoom(float f) throws RemoteException;

    int sreCreateRelayChannel(int i, int i2) throws RemoteException;

    int sreCreateStream(int i, int i2, int i3, String str, int i4, String str2, int i5, boolean z, boolean z2, int i6, int i7, String str3, boolean z3, boolean z4) throws RemoteException;

    int sreDeleteRelayChannel(int i) throws RemoteException;

    int sreDeleteStream(int i) throws RemoteException;

    int sreEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) throws RemoteException;

    boolean sreGetMdmn(int i) throws RemoteException;

    String sreGetVersion() throws RemoteException;

    int sreHoldRelayChannel(int i) throws RemoteException;

    void sreInitialize() throws RemoteException;

    int sreResumeRelayChannel(int i) throws RemoteException;

    int sreSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, int i15) throws RemoteException;

    int sreSetConnection(int i, String str, int i2, String str2, int i3, int i4, int i5, int i6) throws RemoteException;

    int sreSetDtmfCodecInfo(int i, int i2, int i3, int i4, int i5, int i6) throws RemoteException;

    int sreSetMdmn(int i, boolean z) throws RemoteException;

    int sreSetNetId(int i, long j) throws RemoteException;

    int sreSetRtcpOnCall(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    int sreSetRtcpTimeout(int i, int i2) throws RemoteException;

    int sreSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) throws RemoteException;

    int sreSetRtpTimeout(int i, int i2) throws RemoteException;

    int sreStartRecording(int i, int i2, int i3) throws RemoteException;

    int sreStartRelayChannel(int i, int i2) throws RemoteException;

    int sreStartStream(int i, int i2, int i3) throws RemoteException;

    int sreStopRecording(int i, int i2) throws RemoteException;

    int sreStopRelayChannel(int i) throws RemoteException;

    int sreUpdateRelayChannel(int i, int i2) throws RemoteException;

    int sreUpdateStream(int i) throws RemoteException;

    int steCreateChannel(int i, String str, int i2, String str2, int i3, int i4, int i5, String str3, boolean z, boolean z2) throws RemoteException;

    int steDeleteChannel(int i) throws RemoteException;

    int steEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) throws RemoteException;

    void steInitialize() throws RemoteException;

    int steModifyChannel(int i, int i2) throws RemoteException;

    int steSendText(int i, String str, int i2) throws RemoteException;

    int steSetCallOptions(int i, boolean z) throws RemoteException;

    int steSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) throws RemoteException;

    int steSetNetId(int i, int i2) throws RemoteException;

    int steSetRtcpOnCall(int i, int i2, int i3) throws RemoteException;

    int steSetRtcpTimeout(int i, long j) throws RemoteException;

    int steSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) throws RemoteException;

    int steSetRtpTimeout(int i, long j) throws RemoteException;

    int steSetSessionId(int i, int i2) throws RemoteException;

    int steStartChannel(int i, int i2, boolean z) throws RemoteException;

    int steStopChannel(int i) throws RemoteException;

    int steUpdateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6) throws RemoteException;

    int sveCmcRecorderCreate(int i, int i2, int i3, String str, int i4, int i5, long j, int i6, String str2, int i7, int i8, int i9, int i10, int i11, long j2, String str3) throws RemoteException;

    int sveCreateChannel() throws RemoteException;

    String sveGetCodecCapacity(int i) throws RemoteException;

    TimeInfo sveGetLastPlayedVideoTime(int i) throws RemoteException;

    TimeInfo sveGetRtcpTimeInfo(int i) throws RemoteException;

    int sveRecorderCreate(int i, String str, int i2, int i3, String str2, int i4) throws RemoteException;

    int sveRecorderDelete(int i) throws RemoteException;

    int sveRecorderStart(int i) throws RemoteException;

    int sveRecorderStop(int i, boolean z) throws RemoteException;

    void sveRestartEmoji(int i) throws RemoteException;

    int sveSendGeneralEvent(int i, int i2, int i3, String str) throws RemoteException;

    int sveSetCodecInfo(int i, int i2, int i3, int i4, int i5, int i6, String str, int i7, int i8, int i9, int i10, int i11, boolean z, int i12, boolean z2, int i13, int i14, int i15, int i16, int i17, byte[] bArr, byte[] bArr2, byte[] bArr3, int i18, int i19, int i20) throws RemoteException;

    int sveSetConnection(int i, String str, int i2, String str2, int i3, int i4, int i5, int i6, long j) throws RemoteException;

    int sveSetGcmSrtpParams(int i, int i2, int i3, int i4, char c, int i5, byte[] bArr, int i6, byte[] bArr2, int i7) throws RemoteException;

    int sveSetHeldInfo(int i, boolean z, boolean z2) throws RemoteException;

    int sveSetMediaConfig(int i, boolean z, int i2, boolean z2, int i3, int i4, int i5, int i6) throws RemoteException;

    int sveSetNetworkQoS(int i, int i2, int i3, int i4) throws RemoteException;

    int sveSetSRTPParams(int i, String str, byte[] bArr, int i2, int i3, int i4, int i5, String str2, byte[] bArr2, int i6, int i7, int i8, int i9) throws RemoteException;

    int sveSetVideoPlayDelay(int i, int i2) throws RemoteException;

    int sveStartCamera(int i, int i2) throws RemoteException;

    int sveStartChannel(int i, int i2, int i3) throws RemoteException;

    void sveStartEmoji(int i, String str) throws RemoteException;

    int sveStartRecording(int i, int i2) throws RemoteException;

    int sveStopCamera() throws RemoteException;

    int sveStopChannel(int i) throws RemoteException;

    void sveStopEmoji(int i) throws RemoteException;

    int sveStopRecording(int i) throws RemoteException;

    void switchCamera() throws RemoteException;

    void unregisterForCmcEventListener(ICmcMediaEventListener iCmcMediaEventListener) throws RemoteException;

    void unregisterForMediaEventListener(IImsMediaEventListener iImsMediaEventListener) throws RemoteException;

    public static abstract class Stub extends Binder implements ISecVideoEngineService {
        static final int TRANSACTION_bindToNetwork = 10;
        static final int TRANSACTION_cpveStartInjection = 107;
        static final int TRANSACTION_cpveStopInjection = 108;
        static final int TRANSACTION_isSupportingCameraMotor = 97;
        static final int TRANSACTION_onDestroy = 1;
        static final int TRANSACTION_registerForCmcEventListener = 111;
        static final int TRANSACTION_registerForMediaEventListener = 109;
        static final int TRANSACTION_saeCreateChannel = 15;
        static final int TRANSACTION_saeDeleteChannel = 20;
        static final int TRANSACTION_saeEnableSRTP = 23;
        static final int TRANSACTION_saeGetAudioRxTrackId = 32;
        static final int TRANSACTION_saeGetLastPlayedVoiceTime = 28;
        static final int TRANSACTION_saeGetVersion = 31;
        static final int TRANSACTION_saeHandleDtmf = 21;
        static final int TRANSACTION_saeInitialize = 12;
        static final int TRANSACTION_saeModifyChannel = 19;
        static final int TRANSACTION_saeSetAudioPath = 33;
        static final int TRANSACTION_saeSetCodecInfo = 14;
        static final int TRANSACTION_saeSetDtmfCodecInfo = 22;
        static final int TRANSACTION_saeSetRtcpOnCall = 24;
        static final int TRANSACTION_saeSetRtcpTimeout = 26;
        static final int TRANSACTION_saeSetRtcpXr = 27;
        static final int TRANSACTION_saeSetRtpTimeout = 25;
        static final int TRANSACTION_saeSetTOS = 30;
        static final int TRANSACTION_saeSetVoicePlayDelay = 29;
        static final int TRANSACTION_saeStartChannel = 16;
        static final int TRANSACTION_saeStartRecording = 103;
        static final int TRANSACTION_saeStopChannel = 18;
        static final int TRANSACTION_saeStopRecording = 104;
        static final int TRANSACTION_saeTerminate = 13;
        static final int TRANSACTION_saeUpdateChannel = 17;
        static final int TRANSACTION_sendGeneralBundleEvent = 11;
        static final int TRANSACTION_sendStillImage = 7;
        static final int TRANSACTION_setCameraEffect = 8;
        static final int TRANSACTION_setDisplaySurface = 3;
        static final int TRANSACTION_setOrientation = 4;
        static final int TRANSACTION_setPreviewResolution = 9;
        static final int TRANSACTION_setPreviewSurface = 2;
        static final int TRANSACTION_setZoom = 5;
        static final int TRANSACTION_sreCreateRelayChannel = 80;
        static final int TRANSACTION_sreCreateStream = 76;
        static final int TRANSACTION_sreDeleteRelayChannel = 81;
        static final int TRANSACTION_sreDeleteStream = 78;
        static final int TRANSACTION_sreEnableSRTP = 88;
        static final int TRANSACTION_sreGetMdmn = 74;
        static final int TRANSACTION_sreGetVersion = 72;
        static final int TRANSACTION_sreHoldRelayChannel = 84;
        static final int TRANSACTION_sreInitialize = 71;
        static final int TRANSACTION_sreResumeRelayChannel = 85;
        static final int TRANSACTION_sreSetCodecInfo = 93;
        static final int TRANSACTION_sreSetConnection = 87;
        static final int TRANSACTION_sreSetDtmfCodecInfo = 94;
        static final int TRANSACTION_sreSetMdmn = 73;
        static final int TRANSACTION_sreSetNetId = 75;
        static final int TRANSACTION_sreSetRtcpOnCall = 89;
        static final int TRANSACTION_sreSetRtcpTimeout = 91;
        static final int TRANSACTION_sreSetRtcpXr = 92;
        static final int TRANSACTION_sreSetRtpTimeout = 90;
        static final int TRANSACTION_sreStartRecording = 95;
        static final int TRANSACTION_sreStartRelayChannel = 82;
        static final int TRANSACTION_sreStartStream = 77;
        static final int TRANSACTION_sreStopRecording = 96;
        static final int TRANSACTION_sreStopRelayChannel = 83;
        static final int TRANSACTION_sreUpdateRelayChannel = 86;
        static final int TRANSACTION_sreUpdateStream = 79;
        static final int TRANSACTION_steCreateChannel = 56;
        static final int TRANSACTION_steDeleteChannel = 61;
        static final int TRANSACTION_steEnableSRTP = 63;
        static final int TRANSACTION_steInitialize = 54;
        static final int TRANSACTION_steModifyChannel = 60;
        static final int TRANSACTION_steSendText = 62;
        static final int TRANSACTION_steSetCallOptions = 68;
        static final int TRANSACTION_steSetCodecInfo = 55;
        static final int TRANSACTION_steSetNetId = 69;
        static final int TRANSACTION_steSetRtcpOnCall = 64;
        static final int TRANSACTION_steSetRtcpTimeout = 66;
        static final int TRANSACTION_steSetRtcpXr = 67;
        static final int TRANSACTION_steSetRtpTimeout = 65;
        static final int TRANSACTION_steSetSessionId = 70;
        static final int TRANSACTION_steStartChannel = 57;
        static final int TRANSACTION_steStopChannel = 59;
        static final int TRANSACTION_steUpdateChannel = 58;
        static final int TRANSACTION_sveCmcRecorderCreate = 99;
        static final int TRANSACTION_sveCreateChannel = 34;
        static final int TRANSACTION_sveGetCodecCapacity = 53;
        static final int TRANSACTION_sveGetLastPlayedVideoTime = 48;
        static final int TRANSACTION_sveGetRtcpTimeInfo = 52;
        static final int TRANSACTION_sveRecorderCreate = 98;
        static final int TRANSACTION_sveRecorderDelete = 100;
        static final int TRANSACTION_sveRecorderStart = 101;
        static final int TRANSACTION_sveRecorderStop = 102;
        static final int TRANSACTION_sveRestartEmoji = 46;
        static final int TRANSACTION_sveSendGeneralEvent = 51;
        static final int TRANSACTION_sveSetCodecInfo = 38;
        static final int TRANSACTION_sveSetConnection = 37;
        static final int TRANSACTION_sveSetGcmSrtpParams = 40;
        static final int TRANSACTION_sveSetHeldInfo = 47;
        static final int TRANSACTION_sveSetMediaConfig = 41;
        static final int TRANSACTION_sveSetNetworkQoS = 50;
        static final int TRANSACTION_sveSetSRTPParams = 39;
        static final int TRANSACTION_sveSetVideoPlayDelay = 49;
        static final int TRANSACTION_sveStartCamera = 42;
        static final int TRANSACTION_sveStartChannel = 35;
        static final int TRANSACTION_sveStartEmoji = 44;
        static final int TRANSACTION_sveStartRecording = 105;
        static final int TRANSACTION_sveStopCamera = 43;
        static final int TRANSACTION_sveStopChannel = 36;
        static final int TRANSACTION_sveStopEmoji = 45;
        static final int TRANSACTION_sveStopRecording = 106;
        static final int TRANSACTION_switchCamera = 6;
        static final int TRANSACTION_unregisterForCmcEventListener = 112;
        static final int TRANSACTION_unregisterForMediaEventListener = 110;

        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, ISecVideoEngineService.DESCRIPTOR);
        }

        public static ISecVideoEngineService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(ISecVideoEngineService.DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof ISecVideoEngineService)) {
                return new Proxy(iBinder);
            }
            return (ISecVideoEngineService) queryLocalInterface;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:128:?, code lost:
            return r13;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:129:?, code lost:
            return r14;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:95:0x0ae2, code lost:
            return true;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onTransact(int r33, android.os.Parcel r34, android.os.Parcel r35, int r36) throws android.os.RemoteException {
            /*
                r32 = this;
                r0 = r32
                r1 = r33
                r15 = r34
                r14 = r35
                java.lang.String r2 = "com.sec.sve.ISecVideoEngineService"
                r13 = 1
                if (r1 < r13) goto L_0x0015
                r3 = 16777215(0xffffff, float:2.3509886E-38)
                if (r1 > r3) goto L_0x0015
                r15.enforceInterface(r2)
            L_0x0015:
                r3 = 1598968902(0x5f4e5446, float:1.4867585E19)
                if (r1 == r3) goto L_0x0e7a
                switch(r1) {
                    case 1: goto L_0x0e6f;
                    case 2: goto L_0x0e50;
                    case 3: goto L_0x0e31;
                    case 4: goto L_0x0e1e;
                    case 5: goto L_0x0e0b;
                    case 6: goto L_0x0dff;
                    case 7: goto L_0x0ddf;
                    case 8: goto L_0x0dcb;
                    case 9: goto L_0x0db3;
                    case 10: goto L_0x0d9b;
                    case 11: goto L_0x0d7e;
                    case 12: goto L_0x0d62;
                    case 13: goto L_0x0d57;
                    case 14: goto L_0x0cb6;
                    case 15: goto L_0x0c75;
                    case 16: goto L_0x0c59;
                    case 17: goto L_0x0c27;
                    case 18: goto L_0x0c12;
                    case 19: goto L_0x0bf9;
                    case 20: goto L_0x0be4;
                    case 21: goto L_0x0bc3;
                    case 22: goto L_0x0b9c;
                    case 23: goto L_0x0b75;
                    case 24: goto L_0x0b58;
                    case 25: goto L_0x0b3f;
                    case 26: goto L_0x0b26;
                    case 27: goto L_0x0afb;
                    case 28: goto L_0x0ae6;
                    case 29: goto L_0x0acc;
                    case 30: goto L_0x0ab5;
                    case 31: goto L_0x0a9e;
                    case 32: goto L_0x0a8b;
                    case 33: goto L_0x0a74;
                    case 34: goto L_0x0a67;
                    case 35: goto L_0x0a4b;
                    case 36: goto L_0x0a37;
                    case 37: goto L_0x0a01;
                    case 38: goto L_0x097e;
                    case 39: goto L_0x0934;
                    case 40: goto L_0x08f6;
                    case 41: goto L_0x08c4;
                    case 42: goto L_0x08ac;
                    case 43: goto L_0x089e;
                    case 44: goto L_0x0889;
                    case 45: goto L_0x0878;
                    case 46: goto L_0x0867;
                    case 47: goto L_0x084a;
                    case 48: goto L_0x0835;
                    case 49: goto L_0x081c;
                    case 50: goto L_0x07fb;
                    case 51: goto L_0x07da;
                    case 52: goto L_0x07c4;
                    case 53: goto L_0x07ae;
                    case 54: goto L_0x07a5;
                    case 55: goto L_0x0706;
                    case 56: goto L_0x06cb;
                    case 57: goto L_0x06ae;
                    case 58: goto L_0x067b;
                    case 59: goto L_0x0666;
                    case 60: goto L_0x064d;
                    case 61: goto L_0x0638;
                    case 62: goto L_0x061b;
                    case 63: goto L_0x05f4;
                    case 64: goto L_0x05d7;
                    case 65: goto L_0x05be;
                    case 66: goto L_0x05a5;
                    case 67: goto L_0x057a;
                    case 68: goto L_0x0561;
                    case 69: goto L_0x0548;
                    case 70: goto L_0x052f;
                    case 71: goto L_0x0525;
                    case 72: goto L_0x0517;
                    case 73: goto L_0x04fe;
                    case 74: goto L_0x04e9;
                    case 75: goto L_0x04d0;
                    case 76: goto L_0x0483;
                    case 77: goto L_0x0466;
                    case 78: goto L_0x0451;
                    case 79: goto L_0x043c;
                    case 80: goto L_0x0423;
                    case 81: goto L_0x040e;
                    case 82: goto L_0x03f5;
                    case 83: goto L_0x03e0;
                    case 84: goto L_0x03cb;
                    case 85: goto L_0x03b6;
                    case 86: goto L_0x039d;
                    case 87: goto L_0x036a;
                    case 88: goto L_0x0343;
                    case 89: goto L_0x031c;
                    case 90: goto L_0x0303;
                    case 91: goto L_0x02e9;
                    case 92: goto L_0x02bf;
                    case 93: goto L_0x021f;
                    case 94: goto L_0x01f5;
                    case 95: goto L_0x01d9;
                    case 96: goto L_0x01c1;
                    case 97: goto L_0x01b4;
                    case 98: goto L_0x018a;
                    case 99: goto L_0x0131;
                    case 100: goto L_0x011e;
                    case 101: goto L_0x010b;
                    case 102: goto L_0x00f4;
                    case 103: goto L_0x00d5;
                    case 104: goto L_0x00be;
                    case 105: goto L_0x00a7;
                    case 106: goto L_0x0094;
                    case 107: goto L_0x007d;
                    case 108: goto L_0x0071;
                    case 109: goto L_0x005e;
                    case 110: goto L_0x004b;
                    case 111: goto L_0x0038;
                    case 112: goto L_0x0025;
                    default: goto L_0x001d;
                }
            L_0x001d:
                r4 = r0
                r3 = r14
                r2 = r15
                boolean r0 = super.onTransact(r33, r34, r35, r36)
                return r0
            L_0x0025:
                android.os.IBinder r1 = r34.readStrongBinder()
                com.sec.sve.ICmcMediaEventListener r1 = com.sec.sve.ICmcMediaEventListener.Stub.asInterface(r1)
                r34.enforceNoDataAvail()
                r0.unregisterForCmcEventListener(r1)
                r35.writeNoException()
                goto L_0x0930
            L_0x0038:
                android.os.IBinder r1 = r34.readStrongBinder()
                com.sec.sve.ICmcMediaEventListener r1 = com.sec.sve.ICmcMediaEventListener.Stub.asInterface(r1)
                r34.enforceNoDataAvail()
                r0.registerForCmcEventListener(r1)
                r35.writeNoException()
                goto L_0x0930
            L_0x004b:
                android.os.IBinder r1 = r34.readStrongBinder()
                com.sec.sve.IImsMediaEventListener r1 = com.sec.sve.IImsMediaEventListener.Stub.asInterface(r1)
                r34.enforceNoDataAvail()
                r0.unregisterForMediaEventListener(r1)
                r35.writeNoException()
                goto L_0x0930
            L_0x005e:
                android.os.IBinder r1 = r34.readStrongBinder()
                com.sec.sve.IImsMediaEventListener r1 = com.sec.sve.IImsMediaEventListener.Stub.asInterface(r1)
                r34.enforceNoDataAvail()
                r0.registerForMediaEventListener(r1)
                r35.writeNoException()
                goto L_0x0930
            L_0x0071:
                int r0 = r32.cpveStopInjection()
                r35.writeNoException()
                r14.writeInt(r0)
                goto L_0x0930
            L_0x007d:
                java.lang.String r1 = r34.readString()
                int r2 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.cpveStartInjection(r1, r2)
                r35.writeNoException()
                r14.writeInt(r0)
                goto L_0x0930
            L_0x0094:
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.sveStopRecording(r1)
                r35.writeNoException()
                r14.writeInt(r0)
                goto L_0x0930
            L_0x00a7:
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.sveStartRecording(r1, r2)
                r35.writeNoException()
                r14.writeInt(r0)
                goto L_0x0930
            L_0x00be:
                int r1 = r34.readInt()
                boolean r2 = r34.readBoolean()
                r34.enforceNoDataAvail()
                int r0 = r0.saeStopRecording(r1, r2)
                r35.writeNoException()
                r14.writeInt(r0)
                goto L_0x0930
            L_0x00d5:
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                boolean r4 = r34.readBoolean()
                r34.enforceNoDataAvail()
                int r0 = r0.saeStartRecording(r1, r2, r3, r4)
                r35.writeNoException()
                r14.writeInt(r0)
                goto L_0x0930
            L_0x00f4:
                int r1 = r34.readInt()
                boolean r2 = r34.readBoolean()
                r34.enforceNoDataAvail()
                int r0 = r0.sveRecorderStop(r1, r2)
                r35.writeNoException()
                r14.writeInt(r0)
                goto L_0x0930
            L_0x010b:
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.sveRecorderStart(r1)
                r35.writeNoException()
                r14.writeInt(r0)
                goto L_0x0930
            L_0x011e:
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.sveRecorderDelete(r1)
                r35.writeNoException()
                r14.writeInt(r0)
                goto L_0x0930
            L_0x0131:
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                java.lang.String r4 = r34.readString()
                int r5 = r34.readInt()
                int r6 = r34.readInt()
                long r7 = r34.readLong()
                int r9 = r34.readInt()
                java.lang.String r10 = r34.readString()
                int r11 = r34.readInt()
                int r12 = r34.readInt()
                int r16 = r34.readInt()
                r13 = r16
                int r16 = r34.readInt()
                r14 = r16
                int r16 = r34.readInt()
                r15 = r16
                long r16 = r34.readLong()
                java.lang.String r18 = r34.readString()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.sveCmcRecorderCreate(r1, r2, r3, r4, r5, r6, r7, r9, r10, r11, r12, r13, r14, r15, r16, r18)
                r35.writeNoException()
                r15 = r35
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x018a:
                r15 = r14
                int r1 = r34.readInt()
                java.lang.String r2 = r34.readString()
                int r3 = r34.readInt()
                int r4 = r34.readInt()
                java.lang.String r5 = r34.readString()
                int r6 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.sveRecorderCreate(r1, r2, r3, r4, r5, r6)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x01b4:
                r15 = r14
                boolean r0 = r32.isSupportingCameraMotor()
                r35.writeNoException()
                r15.writeBoolean(r0)
                goto L_0x0ae2
            L_0x01c1:
                r15 = r14
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.sreStopRecording(r1, r2)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x01d9:
                r15 = r14
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.sreStartRecording(r1, r2, r3)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x01f5:
                r15 = r14
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                int r4 = r34.readInt()
                int r5 = r34.readInt()
                int r6 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.sreSetDtmfCodecInfo(r1, r2, r3, r4, r5, r6)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x021f:
                r15 = r14
                int r1 = r34.readInt()
                java.lang.String r2 = r34.readString()
                int r3 = r34.readInt()
                int r4 = r34.readInt()
                int r5 = r34.readInt()
                int r6 = r34.readInt()
                int r7 = r34.readInt()
                int r8 = r34.readInt()
                boolean r9 = r34.readBoolean()
                int r10 = r34.readInt()
                int r11 = r34.readInt()
                int r12 = r34.readInt()
                int r13 = r34.readInt()
                int r14 = r34.readInt()
                int r15 = r34.readInt()
                char r15 = (char) r15
                int r0 = r34.readInt()
                char r0 = (char) r0
                r16 = r0
                int r0 = r34.readInt()
                char r0 = (char) r0
                r17 = r0
                int r0 = r34.readInt()
                char r0 = (char) r0
                r18 = r0
                int r0 = r34.readInt()
                char r0 = (char) r0
                r19 = r0
                int r0 = r34.readInt()
                char r0 = (char) r0
                r20 = r0
                int r21 = r34.readInt()
                int r22 = r34.readInt()
                java.lang.String r23 = r34.readString()
                java.lang.String r24 = r34.readString()
                java.lang.String r25 = r34.readString()
                java.lang.String r26 = r34.readString()
                java.lang.String r27 = r34.readString()
                java.lang.String r28 = r34.readString()
                java.lang.String r29 = r34.readString()
                java.lang.String r30 = r34.readString()
                int r31 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.sreSetCodecInfo(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24, r25, r26, r27, r28, r29, r30, r31)
                r35.writeNoException()
                r15 = r35
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x02bf:
                r15 = r14
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                int r4 = r34.readInt()
                int r5 = r34.readInt()
                int[] r6 = r34.createIntArray()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.sreSetRtcpXr(r1, r2, r3, r4, r5, r6)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x02e9:
                r15 = r14
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                r14 = r32
                int r0 = r14.sreSetRtcpTimeout(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0303:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.sreSetRtpTimeout(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x031c:
                r15 = r14
                r14 = r0
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                int r4 = r34.readInt()
                int r5 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.sreSetRtcpOnCall(r1, r2, r3, r4, r5)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0343:
                r15 = r14
                r14 = r0
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                byte[] r4 = r34.createByteArray()
                int r5 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.sreEnableSRTP(r1, r2, r3, r4, r5)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x036a:
                r15 = r14
                r14 = r0
                int r1 = r34.readInt()
                java.lang.String r2 = r34.readString()
                int r3 = r34.readInt()
                java.lang.String r4 = r34.readString()
                int r5 = r34.readInt()
                int r6 = r34.readInt()
                int r7 = r34.readInt()
                int r8 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.sreSetConnection(r1, r2, r3, r4, r5, r6, r7, r8)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x039d:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.sreUpdateRelayChannel(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x03b6:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.sreResumeRelayChannel(r0)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x03cb:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.sreHoldRelayChannel(r0)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x03e0:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.sreStopRelayChannel(r0)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x03f5:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.sreStartRelayChannel(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x040e:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.sreDeleteRelayChannel(r0)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0423:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.sreCreateRelayChannel(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x043c:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.sreUpdateStream(r0)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0451:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.sreDeleteStream(r0)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0466:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.sreStartStream(r0, r1, r2)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0483:
                r15 = r14
                r14 = r0
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                java.lang.String r4 = r34.readString()
                int r5 = r34.readInt()
                java.lang.String r6 = r34.readString()
                int r7 = r34.readInt()
                boolean r8 = r34.readBoolean()
                boolean r9 = r34.readBoolean()
                int r10 = r34.readInt()
                int r11 = r34.readInt()
                java.lang.String r12 = r34.readString()
                boolean r13 = r34.readBoolean()
                boolean r16 = r34.readBoolean()
                r34.enforceNoDataAvail()
                r0 = r32
                r14 = r16
                int r0 = r0.sreCreateStream(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x04d0:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                long r1 = r34.readLong()
                r34.enforceNoDataAvail()
                int r0 = r14.sreSetNetId(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x04e9:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                boolean r0 = r14.sreGetMdmn(r0)
                r35.writeNoException()
                r15.writeBoolean(r0)
                goto L_0x0ae2
            L_0x04fe:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                boolean r1 = r34.readBoolean()
                r34.enforceNoDataAvail()
                int r0 = r14.sreSetMdmn(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0517:
                r15 = r14
                r14 = r0
                java.lang.String r0 = r32.sreGetVersion()
                r35.writeNoException()
                r15.writeString(r0)
                goto L_0x0ae2
            L_0x0525:
                r15 = r14
                r14 = r0
                r32.sreInitialize()
                r35.writeNoException()
                goto L_0x0ae2
            L_0x052f:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.steSetSessionId(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0548:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.steSetNetId(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0561:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                boolean r1 = r34.readBoolean()
                r34.enforceNoDataAvail()
                int r0 = r14.steSetCallOptions(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x057a:
                r15 = r14
                r14 = r0
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                int r4 = r34.readInt()
                int r5 = r34.readInt()
                int[] r6 = r34.createIntArray()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.steSetRtcpXr(r1, r2, r3, r4, r5, r6)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x05a5:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                long r1 = r34.readLong()
                r34.enforceNoDataAvail()
                int r0 = r14.steSetRtcpTimeout(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x05be:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                long r1 = r34.readLong()
                r34.enforceNoDataAvail()
                int r0 = r14.steSetRtpTimeout(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x05d7:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.steSetRtcpOnCall(r0, r1, r2)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x05f4:
                r15 = r14
                r14 = r0
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                byte[] r4 = r34.createByteArray()
                int r5 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.steEnableSRTP(r1, r2, r3, r4, r5)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x061b:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                java.lang.String r1 = r34.readString()
                int r2 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.steSendText(r0, r1, r2)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0638:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.steDeleteChannel(r0)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x064d:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.steModifyChannel(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0666:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.steStopChannel(r0)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x067b:
                r15 = r14
                r14 = r0
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                java.lang.String r3 = r34.readString()
                int r4 = r34.readInt()
                java.lang.String r5 = r34.readString()
                int r6 = r34.readInt()
                int r7 = r34.readInt()
                int r8 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.steUpdateChannel(r1, r2, r3, r4, r5, r6, r7, r8)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x06ae:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                boolean r2 = r34.readBoolean()
                r34.enforceNoDataAvail()
                int r0 = r14.steStartChannel(r0, r1, r2)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x06cb:
                r15 = r14
                r14 = r0
                int r1 = r34.readInt()
                java.lang.String r2 = r34.readString()
                int r3 = r34.readInt()
                java.lang.String r4 = r34.readString()
                int r5 = r34.readInt()
                int r6 = r34.readInt()
                int r7 = r34.readInt()
                java.lang.String r8 = r34.readString()
                boolean r9 = r34.readBoolean()
                boolean r10 = r34.readBoolean()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.steCreateChannel(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0706:
                r15 = r14
                r14 = r0
                int r1 = r34.readInt()
                java.lang.String r2 = r34.readString()
                int r3 = r34.readInt()
                int r4 = r34.readInt()
                int r5 = r34.readInt()
                int r6 = r34.readInt()
                int r7 = r34.readInt()
                int r8 = r34.readInt()
                boolean r9 = r34.readBoolean()
                int r10 = r34.readInt()
                int r11 = r34.readInt()
                int r12 = r34.readInt()
                int r13 = r34.readInt()
                int r0 = r34.readInt()
                r14 = r0
                int r0 = r34.readInt()
                char r0 = (char) r0
                r15 = r0
                int r0 = r34.readInt()
                char r0 = (char) r0
                r16 = r0
                int r0 = r34.readInt()
                char r0 = (char) r0
                r17 = r0
                int r0 = r34.readInt()
                char r0 = (char) r0
                r18 = r0
                int r0 = r34.readInt()
                char r0 = (char) r0
                r19 = r0
                int r0 = r34.readInt()
                char r0 = (char) r0
                r20 = r0
                int r21 = r34.readInt()
                int r22 = r34.readInt()
                java.lang.String r23 = r34.readString()
                java.lang.String r24 = r34.readString()
                java.lang.String r25 = r34.readString()
                java.lang.String r26 = r34.readString()
                java.lang.String r27 = r34.readString()
                java.lang.String r28 = r34.readString()
                java.lang.String r29 = r34.readString()
                java.lang.String r30 = r34.readString()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.steSetCodecInfo(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24, r25, r26, r27, r28, r29, r30)
                r35.writeNoException()
                r15 = r35
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x07a5:
                r15 = r14
                r32.steInitialize()
                r35.writeNoException()
                goto L_0x0ae2
            L_0x07ae:
                r15 = r14
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                r14 = r32
                java.lang.String r0 = r14.sveGetCodecCapacity(r0)
                r35.writeNoException()
                r15.writeString(r0)
                goto L_0x0ae2
            L_0x07c4:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                com.sec.sve.TimeInfo r0 = r14.sveGetRtcpTimeInfo(r0)
                r35.writeNoException()
                r13 = 1
                r15.writeTypedObject(r0, r13)
                goto L_0x0930
            L_0x07da:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                java.lang.String r3 = r34.readString()
                r34.enforceNoDataAvail()
                int r0 = r14.sveSendGeneralEvent(r0, r1, r2, r3)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0930
            L_0x07fb:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.sveSetNetworkQoS(r0, r1, r2, r3)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0930
            L_0x081c:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.sveSetVideoPlayDelay(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0930
            L_0x0835:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                com.sec.sve.TimeInfo r0 = r14.sveGetLastPlayedVideoTime(r0)
                r35.writeNoException()
                r15.writeTypedObject(r0, r13)
                goto L_0x0930
            L_0x084a:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                boolean r1 = r34.readBoolean()
                boolean r2 = r34.readBoolean()
                r34.enforceNoDataAvail()
                int r0 = r14.sveSetHeldInfo(r0, r1, r2)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0930
            L_0x0867:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                r14.sveRestartEmoji(r0)
                r35.writeNoException()
                goto L_0x0930
            L_0x0878:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                r14.sveStopEmoji(r0)
                r35.writeNoException()
                goto L_0x0930
            L_0x0889:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                java.lang.String r1 = r34.readString()
                r34.enforceNoDataAvail()
                r14.sveStartEmoji(r0, r1)
                r35.writeNoException()
                goto L_0x0930
            L_0x089e:
                r15 = r14
                r14 = r0
                int r0 = r32.sveStopCamera()
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0930
            L_0x08ac:
                r15 = r14
                r14 = r0
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r14.sveStartCamera(r0, r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0930
            L_0x08c4:
                r15 = r14
                r14 = r0
                int r1 = r34.readInt()
                boolean r2 = r34.readBoolean()
                int r3 = r34.readInt()
                boolean r4 = r34.readBoolean()
                int r5 = r34.readInt()
                int r6 = r34.readInt()
                int r7 = r34.readInt()
                int r8 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.sveSetMediaConfig(r1, r2, r3, r4, r5, r6, r7, r8)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0930
            L_0x08f6:
                r15 = r14
                r14 = r0
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                int r4 = r34.readInt()
                int r0 = r34.readInt()
                char r5 = (char) r0
                int r6 = r34.readInt()
                byte[] r7 = r34.createByteArray()
                int r8 = r34.readInt()
                byte[] r9 = r34.createByteArray()
                int r10 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.sveSetGcmSrtpParams(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
                r35.writeNoException()
                r15.writeInt(r0)
            L_0x0930:
                r31 = r13
                goto L_0x0e79
            L_0x0934:
                r15 = r14
                r14 = r0
                int r1 = r34.readInt()
                java.lang.String r2 = r34.readString()
                byte[] r3 = r34.createByteArray()
                int r4 = r34.readInt()
                int r5 = r34.readInt()
                int r6 = r34.readInt()
                int r7 = r34.readInt()
                java.lang.String r8 = r34.readString()
                byte[] r9 = r34.createByteArray()
                int r10 = r34.readInt()
                int r11 = r34.readInt()
                int r12 = r34.readInt()
                int r16 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                r14 = r13
                r13 = r16
                int r0 = r0.sveSetSRTPParams(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0cb2
            L_0x097e:
                r15 = r14
                r14 = r0
                r0 = r13
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                int r4 = r34.readInt()
                int r5 = r34.readInt()
                int r6 = r34.readInt()
                java.lang.String r7 = r34.readString()
                int r8 = r34.readInt()
                int r9 = r34.readInt()
                int r10 = r34.readInt()
                int r11 = r34.readInt()
                int r12 = r34.readInt()
                boolean r13 = r34.readBoolean()
                int r16 = r34.readInt()
                r0 = r14
                r14 = r16
                boolean r16 = r34.readBoolean()
                r15 = r16
                int r16 = r34.readInt()
                int r17 = r34.readInt()
                int r18 = r34.readInt()
                int r19 = r34.readInt()
                int r20 = r34.readInt()
                byte[] r21 = r34.createByteArray()
                byte[] r22 = r34.createByteArray()
                byte[] r23 = r34.createByteArray()
                int r24 = r34.readInt()
                int r25 = r34.readInt()
                int r26 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.sveSetCodecInfo(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24, r25, r26)
                r35.writeNoException()
                r15 = r35
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0a01:
                r15 = r14
                int r1 = r34.readInt()
                java.lang.String r2 = r34.readString()
                int r3 = r34.readInt()
                java.lang.String r4 = r34.readString()
                int r5 = r34.readInt()
                int r6 = r34.readInt()
                int r7 = r34.readInt()
                int r8 = r34.readInt()
                long r9 = r34.readLong()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.sveSetConnection(r1, r2, r3, r4, r5, r6, r7, r8, r9)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0a37:
                r15 = r14
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.sveStopChannel(r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0a4b:
                r15 = r14
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.sveStartChannel(r1, r2, r3)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0a67:
                r15 = r14
                int r0 = r32.sveCreateChannel()
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0a74:
                r15 = r14
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.saeSetAudioPath(r1, r2)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0a8b:
                r15 = r14
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.saeGetAudioRxTrackId(r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0a9e:
                r15 = r14
                byte[] r1 = r34.createByteArray()
                int r2 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.saeGetVersion(r1, r2)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0ab5:
                r15 = r14
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.saeSetTOS(r1, r2)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0ae2
            L_0x0acc:
                r15 = r14
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.saeSetVoicePlayDelay(r1, r2)
                r35.writeNoException()
                r15.writeInt(r0)
            L_0x0ae2:
                r31 = 1
                goto L_0x0e79
            L_0x0ae6:
                r15 = r14
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                com.sec.sve.TimeInfo r0 = r0.saeGetLastPlayedVoiceTime(r1)
                r35.writeNoException()
                r14 = 1
                r15.writeTypedObject(r0, r14)
                goto L_0x0cb2
            L_0x0afb:
                r15 = r14
                r14 = r13
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                int r4 = r34.readInt()
                int r5 = r34.readInt()
                int[] r6 = r34.createIntArray()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.saeSetRtcpXr(r1, r2, r3, r4, r5, r6)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0cb2
            L_0x0b26:
                r15 = r14
                r14 = r13
                int r1 = r34.readInt()
                long r2 = r34.readLong()
                r34.enforceNoDataAvail()
                int r0 = r0.saeSetRtcpTimeout(r1, r2)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0cb2
            L_0x0b3f:
                r15 = r14
                r14 = r13
                int r1 = r34.readInt()
                long r2 = r34.readLong()
                r34.enforceNoDataAvail()
                int r0 = r0.saeSetRtpTimeout(r1, r2)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0cb2
            L_0x0b58:
                r15 = r14
                r14 = r13
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.saeSetRtcpOnCall(r1, r2, r3)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0cb2
            L_0x0b75:
                r15 = r14
                r14 = r13
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                byte[] r4 = r34.createByteArray()
                int r5 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.saeEnableSRTP(r1, r2, r3, r4, r5)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0cb2
            L_0x0b9c:
                r15 = r14
                r14 = r13
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                int r4 = r34.readInt()
                int r5 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.saeSetDtmfCodecInfo(r1, r2, r3, r4, r5)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0cb2
            L_0x0bc3:
                r15 = r14
                r14 = r13
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                int r3 = r34.readInt()
                int r4 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.saeHandleDtmf(r1, r2, r3, r4)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0cb2
            L_0x0be4:
                r15 = r14
                r14 = r13
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.saeDeleteChannel(r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0cb2
            L_0x0bf9:
                r15 = r14
                r14 = r13
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.saeModifyChannel(r1, r2)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0cb2
            L_0x0c12:
                r15 = r14
                r14 = r13
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                int r0 = r0.saeStopChannel(r1)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0cb2
            L_0x0c27:
                r15 = r14
                r14 = r13
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                java.lang.String r3 = r34.readString()
                int r4 = r34.readInt()
                java.lang.String r5 = r34.readString()
                int r6 = r34.readInt()
                int r7 = r34.readInt()
                int r8 = r34.readInt()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.saeUpdateChannel(r1, r2, r3, r4, r5, r6, r7, r8)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0cb2
            L_0x0c59:
                r15 = r14
                r14 = r13
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                boolean r3 = r34.readBoolean()
                r34.enforceNoDataAvail()
                int r0 = r0.saeStartChannel(r1, r2, r3)
                r35.writeNoException()
                r15.writeInt(r0)
                goto L_0x0cb2
            L_0x0c75:
                r15 = r14
                r14 = r13
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                java.lang.String r3 = r34.readString()
                int r4 = r34.readInt()
                java.lang.String r5 = r34.readString()
                int r6 = r34.readInt()
                int r7 = r34.readInt()
                int r8 = r34.readInt()
                java.lang.String r9 = r34.readString()
                boolean r10 = r34.readBoolean()
                boolean r11 = r34.readBoolean()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.saeCreateChannel(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
                r35.writeNoException()
                r15.writeInt(r0)
            L_0x0cb2:
                r31 = r14
                goto L_0x0e79
            L_0x0cb6:
                r15 = r14
                r14 = r13
                int r1 = r34.readInt()
                java.lang.String r2 = r34.readString()
                int r3 = r34.readInt()
                int r4 = r34.readInt()
                int r5 = r34.readInt()
                int r6 = r34.readInt()
                int r7 = r34.readInt()
                int r8 = r34.readInt()
                boolean r9 = r34.readBoolean()
                int r10 = r34.readInt()
                int r11 = r34.readInt()
                int r12 = r34.readInt()
                int r13 = r34.readInt()
                int r16 = r34.readInt()
                r31 = r14
                r14 = r16
                int r15 = r34.readInt()
                char r15 = (char) r15
                int r0 = r34.readInt()
                char r0 = (char) r0
                r16 = r0
                int r0 = r34.readInt()
                char r0 = (char) r0
                r17 = r0
                int r0 = r34.readInt()
                char r0 = (char) r0
                r18 = r0
                int r0 = r34.readInt()
                char r0 = (char) r0
                r19 = r0
                int r0 = r34.readInt()
                char r0 = (char) r0
                r20 = r0
                int r21 = r34.readInt()
                int r22 = r34.readInt()
                java.lang.String r23 = r34.readString()
                java.lang.String r24 = r34.readString()
                java.lang.String r25 = r34.readString()
                java.lang.String r26 = r34.readString()
                java.lang.String r27 = r34.readString()
                java.lang.String r28 = r34.readString()
                java.lang.String r29 = r34.readString()
                java.lang.String r30 = r34.readString()
                r34.enforceNoDataAvail()
                r0 = r32
                int r0 = r0.saeSetCodecInfo(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24, r25, r26, r27, r28, r29, r30)
                r35.writeNoException()
                r3 = r35
                r3.writeInt(r0)
                goto L_0x0e79
            L_0x0d57:
                r31 = r13
                r3 = r14
                r32.saeTerminate()
                r35.writeNoException()
                goto L_0x0e79
            L_0x0d62:
                r31 = r13
                r3 = r14
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                int r2 = r34.readInt()
                r34.enforceNoDataAvail()
                r4 = r32
                r4.saeInitialize(r0, r1, r2)
                r35.writeNoException()
                goto L_0x0e79
            L_0x0d7e:
                r4 = r0
                r31 = r13
                r3 = r14
                java.lang.String r0 = r34.readString()
                android.os.Parcelable$Creator r1 = android.os.Bundle.CREATOR
                r2 = r34
                java.lang.Object r1 = r2.readTypedObject(r1)
                android.os.Bundle r1 = (android.os.Bundle) r1
                r34.enforceNoDataAvail()
                r4.sendGeneralBundleEvent(r0, r1)
                r35.writeNoException()
                goto L_0x0e79
            L_0x0d9b:
                r4 = r0
                r31 = r13
                r3 = r14
                r2 = r15
                android.os.Parcelable$Creator r0 = android.net.Network.CREATOR
                java.lang.Object r0 = r2.readTypedObject(r0)
                android.net.Network r0 = (android.net.Network) r0
                r34.enforceNoDataAvail()
                r4.bindToNetwork(r0)
                r35.writeNoException()
                goto L_0x0e79
            L_0x0db3:
                r4 = r0
                r31 = r13
                r3 = r14
                r2 = r15
                int r0 = r34.readInt()
                int r1 = r34.readInt()
                r34.enforceNoDataAvail()
                r4.setPreviewResolution(r0, r1)
                r35.writeNoException()
                goto L_0x0e79
            L_0x0dcb:
                r4 = r0
                r31 = r13
                r3 = r14
                r2 = r15
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                r4.setCameraEffect(r0)
                r35.writeNoException()
                goto L_0x0e79
            L_0x0ddf:
                r4 = r0
                r31 = r13
                r3 = r14
                r2 = r15
                int r0 = r34.readInt()
                boolean r1 = r34.readBoolean()
                java.lang.String r5 = r34.readString()
                java.lang.String r6 = r34.readString()
                r34.enforceNoDataAvail()
                r4.sendStillImage(r0, r1, r5, r6)
                r35.writeNoException()
                goto L_0x0e79
            L_0x0dff:
                r4 = r0
                r31 = r13
                r3 = r14
                r32.switchCamera()
                r35.writeNoException()
                goto L_0x0e79
            L_0x0e0b:
                r4 = r0
                r31 = r13
                r3 = r14
                r2 = r15
                float r0 = r34.readFloat()
                r34.enforceNoDataAvail()
                r4.setZoom(r0)
                r35.writeNoException()
                goto L_0x0e79
            L_0x0e1e:
                r4 = r0
                r31 = r13
                r3 = r14
                r2 = r15
                int r0 = r34.readInt()
                r34.enforceNoDataAvail()
                r4.setOrientation(r0)
                r35.writeNoException()
                goto L_0x0e79
            L_0x0e31:
                r4 = r0
                r31 = r13
                r3 = r14
                r2 = r15
                int r0 = r34.readInt()
                android.os.Parcelable$Creator r1 = android.view.Surface.CREATOR
                java.lang.Object r1 = r2.readTypedObject(r1)
                android.view.Surface r1 = (android.view.Surface) r1
                int r5 = r34.readInt()
                r34.enforceNoDataAvail()
                r4.setDisplaySurface(r0, r1, r5)
                r35.writeNoException()
                goto L_0x0e79
            L_0x0e50:
                r4 = r0
                r31 = r13
                r3 = r14
                r2 = r15
                int r0 = r34.readInt()
                android.os.Parcelable$Creator r1 = android.view.Surface.CREATOR
                java.lang.Object r1 = r2.readTypedObject(r1)
                android.view.Surface r1 = (android.view.Surface) r1
                int r5 = r34.readInt()
                r34.enforceNoDataAvail()
                r4.setPreviewSurface(r0, r1, r5)
                r35.writeNoException()
                goto L_0x0e79
            L_0x0e6f:
                r4 = r0
                r31 = r13
                r3 = r14
                r32.onDestroy()
                r35.writeNoException()
            L_0x0e79:
                return r31
            L_0x0e7a:
                r31 = r13
                r3 = r14
                r3.writeString(r2)
                return r31
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.sve.ISecVideoEngineService.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }

        private static class Proxy implements ISecVideoEngineService {
            private IBinder mRemote;

            public String getInterfaceDescriptor() {
                return ISecVideoEngineService.DESCRIPTOR;
            }

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void onDestroy() throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void setPreviewSurface(int i, Surface surface, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeTypedObject(surface, 0);
                    obtain.writeInt(i2);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void setDisplaySurface(int i, Surface surface, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeTypedObject(surface, 0);
                    obtain.writeInt(i2);
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void setOrientation(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void setZoom(float f) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeFloat(f);
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void switchCamera() throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void sendStillImage(int i, boolean z, String str, String str2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeBoolean(z);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    this.mRemote.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void setCameraEffect(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(8, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void setPreviewResolution(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(9, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void bindToNetwork(Network network) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeTypedObject(network, 0);
                    this.mRemote.transact(10, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void sendGeneralBundleEvent(String str, Bundle bundle) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeTypedObject(bundle, 0);
                    this.mRemote.transact(11, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void saeInitialize(int i, int i2, int i3) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    this.mRemote.transact(12, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void saeTerminate() throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    this.mRemote.transact(13, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    int i15 = i;
                    obtain.writeInt(i);
                    String str10 = str;
                    obtain.writeString(str);
                    int i16 = i2;
                    obtain.writeInt(i2);
                    int i17 = i3;
                    obtain.writeInt(i3);
                    int i18 = i4;
                    obtain.writeInt(i4);
                    int i19 = i5;
                    obtain.writeInt(i5);
                    int i20 = i6;
                    obtain.writeInt(i6);
                    int i21 = i7;
                    obtain.writeInt(i7);
                    boolean z2 = z;
                    obtain.writeBoolean(z);
                    int i22 = i8;
                    obtain.writeInt(i8);
                    obtain.writeInt(i9);
                    obtain.writeInt(i10);
                    obtain.writeInt(i11);
                    obtain.writeInt(i12);
                    obtain.writeInt(c);
                    obtain.writeInt(c2);
                    obtain.writeInt(c3);
                    obtain.writeInt(c4);
                    obtain.writeInt(c5);
                    obtain.writeInt(c6);
                    obtain.writeInt(i13);
                    obtain.writeInt(i14);
                    obtain.writeString(str2);
                    obtain.writeString(str3);
                    obtain.writeString(str4);
                    obtain.writeString(str5);
                    obtain.writeString(str6);
                    obtain.writeString(str7);
                    obtain.writeString(str8);
                    obtain.writeString(str9);
                    this.mRemote.transact(14, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeCreateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6, String str3, boolean z, boolean z2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeString(str);
                    obtain.writeInt(i3);
                    obtain.writeString(str2);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    obtain.writeInt(i6);
                    obtain.writeString(str3);
                    obtain.writeBoolean(z);
                    obtain.writeBoolean(z2);
                    this.mRemote.transact(15, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeStartChannel(int i, int i2, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeBoolean(z);
                    this.mRemote.transact(16, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeUpdateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeString(str);
                    obtain.writeInt(i3);
                    obtain.writeString(str2);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    obtain.writeInt(i6);
                    this.mRemote.transact(17, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeStopChannel(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(18, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeModifyChannel(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(19, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeDeleteChannel(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(20, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeHandleDtmf(int i, int i2, int i3, int i4) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    this.mRemote.transact(21, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeSetDtmfCodecInfo(int i, int i2, int i3, int i4, int i5) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    this.mRemote.transact(22, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeByteArray(bArr);
                    obtain.writeInt(i4);
                    this.mRemote.transact(23, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeSetRtcpOnCall(int i, int i2, int i3) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    this.mRemote.transact(24, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeSetRtpTimeout(int i, long j) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeLong(j);
                    this.mRemote.transact(25, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeSetRtcpTimeout(int i, long j) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeLong(j);
                    this.mRemote.transact(26, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    obtain.writeIntArray(iArr);
                    this.mRemote.transact(27, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public TimeInfo saeGetLastPlayedVoiceTime(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(28, obtain, obtain2, 0);
                    obtain2.readException();
                    return (TimeInfo) obtain2.readTypedObject(TimeInfo.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeSetVoicePlayDelay(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(29, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeSetTOS(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(30, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeGetVersion(byte[] bArr, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeByteArray(bArr);
                    obtain.writeInt(i);
                    this.mRemote.transact(31, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeGetAudioRxTrackId(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(32, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeSetAudioPath(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(33, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveCreateChannel() throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    this.mRemote.transact(34, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveStartChannel(int i, int i2, int i3) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    this.mRemote.transact(35, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveStopChannel(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(36, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveSetConnection(int i, String str, int i2, String str2, int i3, int i4, int i5, int i6, long j) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    obtain.writeInt(i2);
                    obtain.writeString(str2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    obtain.writeInt(i6);
                    obtain.writeLong(j);
                    this.mRemote.transact(37, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveSetCodecInfo(int i, int i2, int i3, int i4, int i5, int i6, String str, int i7, int i8, int i9, int i10, int i11, boolean z, int i12, boolean z2, int i13, int i14, int i15, int i16, int i17, byte[] bArr, byte[] bArr2, byte[] bArr3, int i18, int i19, int i20) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    int i21 = i;
                    obtain.writeInt(i);
                    int i22 = i2;
                    obtain.writeInt(i2);
                    int i23 = i3;
                    obtain.writeInt(i3);
                    int i24 = i4;
                    obtain.writeInt(i4);
                    int i25 = i5;
                    obtain.writeInt(i5);
                    int i26 = i6;
                    obtain.writeInt(i6);
                    String str2 = str;
                    obtain.writeString(str);
                    int i27 = i7;
                    obtain.writeInt(i7);
                    int i28 = i8;
                    obtain.writeInt(i8);
                    int i29 = i9;
                    obtain.writeInt(i9);
                    obtain.writeInt(i10);
                    obtain.writeInt(i11);
                    obtain.writeBoolean(z);
                    obtain.writeInt(i12);
                    obtain.writeBoolean(z2);
                    obtain.writeInt(i13);
                    obtain.writeInt(i14);
                    obtain.writeInt(i15);
                    obtain.writeInt(i16);
                    obtain.writeInt(i17);
                    obtain.writeByteArray(bArr);
                    obtain.writeByteArray(bArr2);
                    obtain.writeByteArray(bArr3);
                    obtain.writeInt(i18);
                    obtain.writeInt(i19);
                    obtain.writeInt(i20);
                    this.mRemote.transact(38, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveSetSRTPParams(int i, String str, byte[] bArr, int i2, int i3, int i4, int i5, String str2, byte[] bArr2, int i6, int i7, int i8, int i9) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    int i10 = i;
                    obtain.writeInt(i);
                    String str3 = str;
                    obtain.writeString(str);
                    byte[] bArr3 = bArr;
                    obtain.writeByteArray(bArr);
                    int i11 = i2;
                    obtain.writeInt(i2);
                    int i12 = i3;
                    obtain.writeInt(i3);
                    int i13 = i4;
                    obtain.writeInt(i4);
                    int i14 = i5;
                    obtain.writeInt(i5);
                    String str4 = str2;
                    obtain.writeString(str2);
                    byte[] bArr4 = bArr2;
                    obtain.writeByteArray(bArr2);
                    int i15 = i6;
                    obtain.writeInt(i6);
                    obtain.writeInt(i7);
                    obtain.writeInt(i8);
                    obtain.writeInt(i9);
                    this.mRemote.transact(39, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveSetGcmSrtpParams(int i, int i2, int i3, int i4, char c, int i5, byte[] bArr, int i6, byte[] bArr2, int i7) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(c);
                    obtain.writeInt(i5);
                    obtain.writeByteArray(bArr);
                    obtain.writeInt(i6);
                    obtain.writeByteArray(bArr2);
                    obtain.writeInt(i7);
                    this.mRemote.transact(40, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveSetMediaConfig(int i, boolean z, int i2, boolean z2, int i3, int i4, int i5, int i6) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeBoolean(z);
                    obtain.writeInt(i2);
                    obtain.writeBoolean(z2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    obtain.writeInt(i6);
                    this.mRemote.transact(41, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveStartCamera(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(42, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveStopCamera() throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    this.mRemote.transact(43, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void sveStartEmoji(int i, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    this.mRemote.transact(44, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void sveStopEmoji(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(45, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void sveRestartEmoji(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(46, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveSetHeldInfo(int i, boolean z, boolean z2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeBoolean(z);
                    obtain.writeBoolean(z2);
                    this.mRemote.transact(47, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public TimeInfo sveGetLastPlayedVideoTime(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(48, obtain, obtain2, 0);
                    obtain2.readException();
                    return (TimeInfo) obtain2.readTypedObject(TimeInfo.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveSetVideoPlayDelay(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(49, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveSetNetworkQoS(int i, int i2, int i3, int i4) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    this.mRemote.transact(50, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveSendGeneralEvent(int i, int i2, int i3, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeString(str);
                    this.mRemote.transact(51, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public TimeInfo sveGetRtcpTimeInfo(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(52, obtain, obtain2, 0);
                    obtain2.readException();
                    return (TimeInfo) obtain2.readTypedObject(TimeInfo.CREATOR);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public String sveGetCodecCapacity(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(53, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readString();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void steInitialize() throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    this.mRemote.transact(54, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    int i15 = i;
                    obtain.writeInt(i);
                    String str10 = str;
                    obtain.writeString(str);
                    int i16 = i2;
                    obtain.writeInt(i2);
                    int i17 = i3;
                    obtain.writeInt(i3);
                    int i18 = i4;
                    obtain.writeInt(i4);
                    int i19 = i5;
                    obtain.writeInt(i5);
                    int i20 = i6;
                    obtain.writeInt(i6);
                    int i21 = i7;
                    obtain.writeInt(i7);
                    boolean z2 = z;
                    obtain.writeBoolean(z);
                    int i22 = i8;
                    obtain.writeInt(i8);
                    obtain.writeInt(i9);
                    obtain.writeInt(i10);
                    obtain.writeInt(i11);
                    obtain.writeInt(i12);
                    obtain.writeInt(c);
                    obtain.writeInt(c2);
                    obtain.writeInt(c3);
                    obtain.writeInt(c4);
                    obtain.writeInt(c5);
                    obtain.writeInt(c6);
                    obtain.writeInt(i13);
                    obtain.writeInt(i14);
                    obtain.writeString(str2);
                    obtain.writeString(str3);
                    obtain.writeString(str4);
                    obtain.writeString(str5);
                    obtain.writeString(str6);
                    obtain.writeString(str7);
                    obtain.writeString(str8);
                    obtain.writeString(str9);
                    this.mRemote.transact(55, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steCreateChannel(int i, String str, int i2, String str2, int i3, int i4, int i5, String str3, boolean z, boolean z2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    obtain.writeInt(i2);
                    obtain.writeString(str2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    obtain.writeString(str3);
                    obtain.writeBoolean(z);
                    obtain.writeBoolean(z2);
                    this.mRemote.transact(56, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steStartChannel(int i, int i2, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeBoolean(z);
                    this.mRemote.transact(57, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steUpdateChannel(int i, int i2, String str, int i3, String str2, int i4, int i5, int i6) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeString(str);
                    obtain.writeInt(i3);
                    obtain.writeString(str2);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    obtain.writeInt(i6);
                    this.mRemote.transact(58, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steStopChannel(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(59, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steModifyChannel(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(60, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steDeleteChannel(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(61, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steSendText(int i, String str, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    obtain.writeInt(i2);
                    this.mRemote.transact(62, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeByteArray(bArr);
                    obtain.writeInt(i4);
                    this.mRemote.transact(63, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steSetRtcpOnCall(int i, int i2, int i3) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    this.mRemote.transact(64, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steSetRtpTimeout(int i, long j) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeLong(j);
                    this.mRemote.transact(65, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steSetRtcpTimeout(int i, long j) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeLong(j);
                    this.mRemote.transact(66, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    obtain.writeIntArray(iArr);
                    this.mRemote.transact(67, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steSetCallOptions(int i, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeBoolean(z);
                    this.mRemote.transact(68, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steSetNetId(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(69, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int steSetSessionId(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(70, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void sreInitialize() throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    this.mRemote.transact(71, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public String sreGetVersion() throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    this.mRemote.transact(72, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readString();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreSetMdmn(int i, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeBoolean(z);
                    this.mRemote.transact(73, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public boolean sreGetMdmn(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(74, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readBoolean();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreSetNetId(int i, long j) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeLong(j);
                    this.mRemote.transact(75, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreCreateStream(int i, int i2, int i3, String str, int i4, String str2, int i5, boolean z, boolean z2, int i6, int i7, String str3, boolean z3, boolean z4) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    int i8 = i;
                    obtain.writeInt(i);
                    int i9 = i2;
                    obtain.writeInt(i2);
                    int i10 = i3;
                    obtain.writeInt(i3);
                    String str4 = str;
                    obtain.writeString(str);
                    int i11 = i4;
                    obtain.writeInt(i4);
                    String str5 = str2;
                    obtain.writeString(str2);
                    int i12 = i5;
                    obtain.writeInt(i5);
                    boolean z5 = z;
                    obtain.writeBoolean(z);
                    boolean z6 = z2;
                    obtain.writeBoolean(z2);
                    int i13 = i6;
                    obtain.writeInt(i6);
                    obtain.writeInt(i7);
                    obtain.writeString(str3);
                    obtain.writeBoolean(z3);
                    obtain.writeBoolean(z4);
                    this.mRemote.transact(76, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreStartStream(int i, int i2, int i3) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    this.mRemote.transact(77, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreDeleteStream(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(78, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreUpdateStream(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(79, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreCreateRelayChannel(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(80, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreDeleteRelayChannel(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(81, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreStartRelayChannel(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(82, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreStopRelayChannel(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(83, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreHoldRelayChannel(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(84, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreResumeRelayChannel(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(85, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreUpdateRelayChannel(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(86, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreSetConnection(int i, String str, int i2, String str2, int i3, int i4, int i5, int i6) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    obtain.writeInt(i2);
                    obtain.writeString(str2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    obtain.writeInt(i6);
                    this.mRemote.transact(87, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreEnableSRTP(int i, int i2, int i3, byte[] bArr, int i4) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeByteArray(bArr);
                    obtain.writeInt(i4);
                    this.mRemote.transact(88, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreSetRtcpOnCall(int i, int i2, int i3, int i4, int i5) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    this.mRemote.transact(89, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreSetRtpTimeout(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(90, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreSetRtcpTimeout(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(91, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreSetRtcpXr(int i, int i2, int i3, int i4, int i5, int[] iArr) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    obtain.writeIntArray(iArr);
                    this.mRemote.transact(92, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreSetCodecInfo(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int i9, int i10, int i11, int i12, char c, char c2, char c3, char c4, char c5, char c6, int i13, int i14, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, int i15) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    int i16 = i;
                    obtain.writeInt(i);
                    String str10 = str;
                    obtain.writeString(str);
                    int i17 = i2;
                    obtain.writeInt(i2);
                    int i18 = i3;
                    obtain.writeInt(i3);
                    int i19 = i4;
                    obtain.writeInt(i4);
                    int i20 = i5;
                    obtain.writeInt(i5);
                    int i21 = i6;
                    obtain.writeInt(i6);
                    int i22 = i7;
                    obtain.writeInt(i7);
                    boolean z2 = z;
                    obtain.writeBoolean(z);
                    int i23 = i8;
                    obtain.writeInt(i8);
                    obtain.writeInt(i9);
                    obtain.writeInt(i10);
                    obtain.writeInt(i11);
                    obtain.writeInt(i12);
                    obtain.writeInt(c);
                    obtain.writeInt(c2);
                    obtain.writeInt(c3);
                    obtain.writeInt(c4);
                    obtain.writeInt(c5);
                    obtain.writeInt(c6);
                    obtain.writeInt(i13);
                    obtain.writeInt(i14);
                    obtain.writeString(str2);
                    obtain.writeString(str3);
                    obtain.writeString(str4);
                    obtain.writeString(str5);
                    obtain.writeString(str6);
                    obtain.writeString(str7);
                    obtain.writeString(str8);
                    obtain.writeString(str9);
                    obtain.writeInt(i15);
                    this.mRemote.transact(93, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreSetDtmfCodecInfo(int i, int i2, int i3, int i4, int i5, int i6) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    obtain.writeInt(i6);
                    this.mRemote.transact(94, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreStartRecording(int i, int i2, int i3) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    this.mRemote.transact(95, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sreStopRecording(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(96, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public boolean isSupportingCameraMotor() throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    this.mRemote.transact(97, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readBoolean();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveRecorderCreate(int i, String str, int i2, int i3, String str2, int i4) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeString(str2);
                    obtain.writeInt(i4);
                    this.mRemote.transact(98, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveCmcRecorderCreate(int i, int i2, int i3, String str, int i4, int i5, long j, int i6, String str2, int i7, int i8, int i9, int i10, int i11, long j2, String str3) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    int i12 = i;
                    obtain.writeInt(i);
                    int i13 = i2;
                    obtain.writeInt(i2);
                    int i14 = i3;
                    obtain.writeInt(i3);
                    String str4 = str;
                    obtain.writeString(str);
                    int i15 = i4;
                    obtain.writeInt(i4);
                    int i16 = i5;
                    obtain.writeInt(i5);
                    long j3 = j;
                    obtain.writeLong(j);
                    int i17 = i6;
                    obtain.writeInt(i6);
                    String str5 = str2;
                    obtain.writeString(str2);
                    obtain.writeInt(i7);
                    obtain.writeInt(i8);
                    obtain.writeInt(i9);
                    obtain.writeInt(i10);
                    obtain.writeInt(i11);
                    obtain.writeLong(j2);
                    obtain.writeString(str3);
                    this.mRemote.transact(99, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveRecorderDelete(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(100, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveRecorderStart(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(101, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveRecorderStop(int i, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeBoolean(z);
                    this.mRemote.transact(102, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeStartRecording(int i, int i2, int i3, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeBoolean(z);
                    this.mRemote.transact(103, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int saeStopRecording(int i, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeBoolean(z);
                    this.mRemote.transact(104, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveStartRecording(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(105, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int sveStopRecording(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(106, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int cpveStartInjection(String str, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    this.mRemote.transact(107, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int cpveStopInjection() throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    this.mRemote.transact(108, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void registerForMediaEventListener(IImsMediaEventListener iImsMediaEventListener) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeStrongInterface(iImsMediaEventListener);
                    this.mRemote.transact(109, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void unregisterForMediaEventListener(IImsMediaEventListener iImsMediaEventListener) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeStrongInterface(iImsMediaEventListener);
                    this.mRemote.transact(110, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void registerForCmcEventListener(ICmcMediaEventListener iCmcMediaEventListener) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeStrongInterface(iCmcMediaEventListener);
                    this.mRemote.transact(111, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void unregisterForCmcEventListener(ICmcMediaEventListener iCmcMediaEventListener) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ISecVideoEngineService.DESCRIPTOR);
                    obtain.writeStrongInterface(iCmcMediaEventListener);
                    this.mRemote.transact(112, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
