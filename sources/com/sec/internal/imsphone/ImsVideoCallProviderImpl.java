package com.sec.internal.imsphone;

import android.net.Uri;
import android.os.RemoteException;
import android.telecom.VideoProfile;
import android.util.Log;
import android.view.Surface;
import com.android.ims.internal.IImsVideoCallCallback;
import com.android.ims.internal.IImsVideoCallProvider;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.IImsMediaCallProvider;
import com.sec.ims.volte2.IVideoServiceEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class ImsVideoCallProviderImpl extends IImsVideoCallProvider.Stub {
    protected static final int EMOJI_START_FAILURE = 1201;
    protected static final int EMOJI_START_SUCCESS = 1200;
    protected static final int EMOJI_STOP_FAILURE = 1203;
    protected static final int EMOJI_STOP_SUCCESS = 1202;
    private static final String LOG_TAG = "ImsVTProviderImpl";
    protected static final int NOTIFY_DOWNGRADED = 1001;
    protected static final int NOTIFY_VIDEO_RESUMED = 1000;
    protected static final int RECORD_START_FAILURE = 1101;
    protected static final int RECORD_START_FAILURE_NO_SPACE = 1110;
    protected static final int RECORD_START_SUCCESS = 1100;
    protected static final int RECORD_STOP_FAILURE = 1103;
    protected static final int RECORD_STOP_NO_SPACE = 1111;
    protected static final int RECORD_STOP_SUCCESS = 1102;
    private boolean mIsDummyCamera = false;
    /* access modifiers changed from: private */
    public boolean mIsVideoPause = false;
    protected IImsMediaCallProvider mMediaController = null;
    /* access modifiers changed from: private */
    public long mModifyRequestTime = 0;
    protected List<IVideoServiceEventListener> mRelay = null;
    protected IImsCallSession mSession;

    private int convertQualityFromVideoProfile(int i) {
        if (i == 1) {
            return 15;
        }
        if (i != 2) {
            return i != 3 ? 13 : 12;
        }
        return 13;
    }

    public ImsVideoCallProviderImpl(IImsCallSession iImsCallSession) {
        this.mSession = iImsCallSession;
        this.mRelay = new ArrayList();
        try {
            this.mMediaController = this.mSession.getMediaCallProvider();
        } catch (RemoteException | NullPointerException unused) {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0042, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setCallback(com.android.ims.internal.IImsVideoCallCallback r3) throws android.os.RemoteException {
        /*
            r2 = this;
            monitor-enter(r2)
            com.sec.ims.volte2.IImsCallSession r0 = r2.mSession     // Catch:{ all -> 0x0043 }
            if (r0 == 0) goto L_0x0041
            com.sec.ims.volte2.IImsMediaCallProvider r0 = r2.mMediaController     // Catch:{ all -> 0x0043 }
            if (r0 != 0) goto L_0x000a
            goto L_0x0041
        L_0x000a:
            if (r3 != 0) goto L_0x0030
            java.util.List<com.sec.ims.volte2.IVideoServiceEventListener> r3 = r2.mRelay     // Catch:{ all -> 0x0043 }
            java.util.Iterator r3 = r3.iterator()     // Catch:{ all -> 0x0043 }
        L_0x0012:
            boolean r0 = r3.hasNext()     // Catch:{ all -> 0x0043 }
            if (r0 == 0) goto L_0x0024
            java.lang.Object r0 = r3.next()     // Catch:{ all -> 0x0043 }
            com.sec.ims.volte2.IVideoServiceEventListener r0 = (com.sec.ims.volte2.IVideoServiceEventListener) r0     // Catch:{ all -> 0x0043 }
            com.sec.ims.volte2.IImsMediaCallProvider r1 = r2.mMediaController     // Catch:{ all -> 0x0043 }
            r1.unregisterForVideoServiceEvent(r0)     // Catch:{ all -> 0x0043 }
            goto L_0x0012
        L_0x0024:
            java.util.List<com.sec.ims.volte2.IVideoServiceEventListener> r3 = r2.mRelay     // Catch:{ all -> 0x0043 }
            r3.clear()     // Catch:{ all -> 0x0043 }
            r3 = 0
            r2.mSession = r3     // Catch:{ all -> 0x0043 }
            r2.mMediaController = r3     // Catch:{ all -> 0x0043 }
            monitor-exit(r2)
            return
        L_0x0030:
            com.sec.internal.imsphone.ImsVideoCallProviderImpl$ImsVideoCallEventListener r0 = new com.sec.internal.imsphone.ImsVideoCallProviderImpl$ImsVideoCallEventListener     // Catch:{ all -> 0x0043 }
            r0.<init>(r3)     // Catch:{ all -> 0x0043 }
            java.util.List<com.sec.ims.volte2.IVideoServiceEventListener> r3 = r2.mRelay     // Catch:{ all -> 0x0043 }
            r3.add(r0)     // Catch:{ all -> 0x0043 }
            com.sec.ims.volte2.IImsMediaCallProvider r3 = r2.mMediaController     // Catch:{ all -> 0x0043 }
            r3.registerForVideoServiceEvent(r0)     // Catch:{ all -> 0x0043 }
            monitor-exit(r2)
            return
        L_0x0041:
            monitor-exit(r2)
            return
        L_0x0043:
            r3 = move-exception
            monitor-exit(r2)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.imsphone.ImsVideoCallProviderImpl.setCallback(com.android.ims.internal.IImsVideoCallCallback):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x006f, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00d7, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCamera(java.lang.String r4, int r5) throws android.os.RemoteException {
        /*
            r3 = this;
            monitor-enter(r3)
            com.sec.ims.volte2.IImsCallSession r5 = r3.mSession     // Catch:{ all -> 0x00d8 }
            if (r5 == 0) goto L_0x00d6
            com.sec.ims.volte2.IImsMediaCallProvider r5 = r3.mMediaController     // Catch:{ all -> 0x00d8 }
            if (r5 != 0) goto L_0x000b
            goto L_0x00d6
        L_0x000b:
            java.lang.String r5 = "ImsVTProviderImpl"
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ all -> 0x00d8 }
            r0.<init>()     // Catch:{ all -> 0x00d8 }
            java.lang.String r1 = "setCamera("
            r0.append(r1)     // Catch:{ all -> 0x00d8 }
            com.sec.ims.volte2.IImsCallSession r1 = r3.mSession     // Catch:{ all -> 0x00d8 }
            int r1 = r1.getSessionId()     // Catch:{ all -> 0x00d8 }
            r0.append(r1)     // Catch:{ all -> 0x00d8 }
            java.lang.String r1 = "): cameraId "
            r0.append(r1)     // Catch:{ all -> 0x00d8 }
            r0.append(r4)     // Catch:{ all -> 0x00d8 }
            java.lang.String r0 = r0.toString()     // Catch:{ all -> 0x00d8 }
            android.util.Log.i(r5, r0)     // Catch:{ all -> 0x00d8 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00d8 }
            r5.<init>()     // Catch:{ all -> 0x00d8 }
            com.sec.ims.volte2.IImsCallSession r0 = r3.mSession     // Catch:{ all -> 0x00d8 }
            int r0 = r0.getPhoneId()     // Catch:{ all -> 0x00d8 }
            r5.append(r0)     // Catch:{ all -> 0x00d8 }
            java.lang.String r0 = ","
            r5.append(r0)     // Catch:{ all -> 0x00d8 }
            com.sec.ims.volte2.IImsCallSession r0 = r3.mSession     // Catch:{ all -> 0x00d8 }
            int r0 = r0.getSessionId()     // Catch:{ all -> 0x00d8 }
            r5.append(r0)     // Catch:{ all -> 0x00d8 }
            java.lang.String r0 = ","
            r5.append(r0)     // Catch:{ all -> 0x00d8 }
            r5.append(r4)     // Catch:{ all -> 0x00d8 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x00d8 }
            r0 = 805306405(0x30000025, float:4.6566334E-10)
            com.sec.internal.log.IMSLog.c(r0, r5)     // Catch:{ all -> 0x00d8 }
            if (r4 != 0) goto L_0x0070
            com.sec.ims.volte2.IImsCallSession r4 = r3.mSession     // Catch:{ all -> 0x00d8 }
            boolean r5 = r3.mIsDummyCamera     // Catch:{ all -> 0x00d8 }
            r4.stopCameraForProvider(r5)     // Catch:{ all -> 0x00d8 }
            boolean r4 = r3.mIsDummyCamera     // Catch:{ all -> 0x00d8 }
            if (r4 == 0) goto L_0x006e
            r4 = 0
            r3.mIsDummyCamera = r4     // Catch:{ all -> 0x00d8 }
        L_0x006e:
            monitor-exit(r3)     // Catch:{ all -> 0x00d8 }
            return
        L_0x0070:
            boolean r5 = r3.setCameraAdditionService(r4)     // Catch:{ all -> 0x00d8 }
            if (r5 == 0) goto L_0x0078
            monitor-exit(r3)     // Catch:{ all -> 0x00d8 }
            return
        L_0x0078:
            com.sec.ims.volte2.IImsMediaCallProvider r5 = r3.mMediaController     // Catch:{ all -> 0x00d8 }
            int r5 = r5.getDefaultCameraId()     // Catch:{ all -> 0x00d8 }
            int r4 = java.lang.Integer.parseInt(r4)     // Catch:{ all -> 0x00d8 }
            com.sec.ims.volte2.IImsCallSession r0 = r3.mSession     // Catch:{ all -> 0x00d8 }
            int r0 = r0.getSessionId()     // Catch:{ all -> 0x00d8 }
            r1 = 1
            r2 = -1
            if (r4 != r2) goto L_0x0094
            r3.mIsDummyCamera = r1     // Catch:{ all -> 0x00d8 }
            com.sec.ims.volte2.IImsCallSession r5 = r3.mSession     // Catch:{ all -> 0x00d8 }
            r5.startCameraForProvider(r4)     // Catch:{ all -> 0x00d8 }
            goto L_0x00d1
        L_0x0094:
            if (r5 == r4) goto L_0x00a6
            if (r5 == r2) goto L_0x00a6
            com.sec.ims.volte2.IImsCallSession r5 = r3.mSession     // Catch:{ all -> 0x00d8 }
            boolean r5 = r5.getUsingCamera()     // Catch:{ all -> 0x00d8 }
            if (r5 == 0) goto L_0x00a6
            com.sec.ims.volte2.IImsMediaCallProvider r4 = r3.mMediaController     // Catch:{ all -> 0x00d8 }
            r4.switchCamera()     // Catch:{ all -> 0x00d8 }
            goto L_0x00d1
        L_0x00a6:
            com.sec.ims.volte2.IImsCallSession r5 = r3.mSession     // Catch:{ all -> 0x00d8 }
            boolean r5 = r5.getUsingCamera()     // Catch:{ all -> 0x00d8 }
            if (r5 == 0) goto L_0x00c4
            java.util.List<com.sec.ims.volte2.IVideoServiceEventListener> r5 = r3.mRelay     // Catch:{ all -> 0x00d8 }
            java.util.Iterator r5 = r5.iterator()     // Catch:{ all -> 0x00d8 }
        L_0x00b4:
            boolean r2 = r5.hasNext()     // Catch:{ all -> 0x00d8 }
            if (r2 == 0) goto L_0x00c4
            java.lang.Object r2 = r5.next()     // Catch:{ all -> 0x00d8 }
            com.sec.ims.volte2.IVideoServiceEventListener r2 = (com.sec.ims.volte2.IVideoServiceEventListener) r2     // Catch:{ all -> 0x00d8 }
            r2.onCameraState(r0, r1)     // Catch:{ all -> 0x00d8 }
            goto L_0x00b4
        L_0x00c4:
            com.sec.ims.volte2.IImsCallSession r5 = r3.mSession     // Catch:{ all -> 0x00d8 }
            int r5 = r5.getSessionId()     // Catch:{ all -> 0x00d8 }
            if (r5 <= 0) goto L_0x00d1
            com.sec.ims.volte2.IImsCallSession r5 = r3.mSession     // Catch:{ all -> 0x00d8 }
            r5.startCameraForProvider(r4)     // Catch:{ all -> 0x00d8 }
        L_0x00d1:
            r3.notifyLocalVideoSize(r0)     // Catch:{ all -> 0x00d8 }
            monitor-exit(r3)     // Catch:{ all -> 0x00d8 }
            return
        L_0x00d6:
            monitor-exit(r3)     // Catch:{ all -> 0x00d8 }
            return
        L_0x00d8:
            r4 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x00d8 }
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.imsphone.ImsVideoCallProviderImpl.setCamera(java.lang.String, int):void");
    }

    private void notifyLocalVideoSize(int i) throws RemoteException {
        int width = this.mSession.getCallProfile().getMediaProfile().getWidth();
        int height = this.mSession.getCallProfile().getMediaProfile().getHeight();
        String videoSize = this.mSession.getCallProfile().getMediaProfile().getVideoSize();
        if (width == 0 && height == 0) {
            if (this.mSession.getCallProfile().getCallType() == 8) {
                width = NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE;
                height = 640;
            } else {
                Log.i(LOG_TAG, "Use updateCallProfile notification");
                return;
            }
        }
        Log.i(LOG_TAG, "Notify Local video width : " + width + " height : " + height + " videoSize : " + videoSize);
        for (IVideoServiceEventListener next : this.mRelay) {
            if (!this.mSession.getCallProfile().isVideoCRBT()) {
                if (!videoSize.contains("LAND") || videoSize.contains("QCIF")) {
                    next.changeCameraCapabilities(i, width, height);
                } else {
                    next.changeCameraCapabilities(i, height, width);
                }
            }
        }
    }

    private boolean setCameraAdditionService(String str) throws RemoteException {
        if (str.contains("effect,")) {
            try {
                this.mMediaController.setCameraEffect(Integer.parseInt(str.substring(7)));
            } catch (NumberFormatException unused) {
                Log.e(LOG_TAG, "Invalid effect Id : " + str);
            }
            return true;
        } else if (str.contains("startRecord,")) {
            this.mMediaController.startRecord(str.substring(12));
            return true;
        } else if (str.contains("stopRecord")) {
            this.mMediaController.stopRecord();
            return true;
        } else if (str.contains("filter,0")) {
            this.mMediaController.stopEmoji(this.mSession.getSessionId());
            return true;
        } else if (str.contains("filter")) {
            this.mMediaController.startEmoji(str);
            return true;
        } else if (str.contains("StartScreenSharing")) {
            this.mMediaController.sendGeneralEvent(100, this.mSession.getSessionId(), 1, "");
            return true;
        } else if (!str.contains("StopScreenSharing")) {
            return false;
        } else {
            this.mMediaController.sendGeneralEvent(100, this.mSession.getSessionId(), 0, "");
            return true;
        }
    }

    public void setPreviewSurface(Surface surface) throws RemoteException {
        synchronized (this) {
            if (!(this.mSession == null || this.mMediaController == null)) {
                Log.i(LOG_TAG, "setPreviewSurface(" + this.mSession.getSessionId() + "): " + surface);
                StringBuilder sb = new StringBuilder();
                sb.append(this.mSession.getPhoneId());
                sb.append(",");
                sb.append(this.mSession.getSessionId());
                sb.append(",");
                sb.append(surface == null ? "0" : "1");
                IMSLog.c(LogClass.VOLTE_SET_PREVIEW_SURFACE, sb.toString());
                this.mMediaController.setPreviewSurface(this.mSession.getSessionId(), surface);
            }
        }
    }

    public void setDisplaySurface(Surface surface) throws RemoteException {
        synchronized (this) {
            if (!(this.mSession == null || this.mMediaController == null)) {
                Log.i(LOG_TAG, "setDisplaySurface(" + this.mSession.getSessionId() + "): " + surface);
                StringBuilder sb = new StringBuilder();
                sb.append(this.mSession.getPhoneId());
                sb.append(",");
                sb.append(this.mSession.getSessionId());
                sb.append(",");
                sb.append(surface == null ? "0" : "1");
                IMSLog.c(LogClass.VOLTE_SET_DISPLAY_SURFACE, sb.toString());
                this.mMediaController.setDisplaySurface(this.mSession.getSessionId(), surface);
            }
        }
    }

    public void setDeviceOrientation(int i) throws RemoteException {
        synchronized (this) {
            if (!(this.mSession == null || this.mMediaController == null)) {
                Log.i(LOG_TAG, "setDeviceOrientation(" + this.mSession.getSessionId() + "): rotation " + i);
                IMSLog.c(LogClass.VOLTE_SET_ORIENTATION, this.mSession.getPhoneId() + "," + this.mSession.getSessionId() + "," + i);
                this.mMediaController.setDeviceOrientation(i);
            }
        }
    }

    public void setZoom(float f) throws RemoteException {
        IImsMediaCallProvider iImsMediaCallProvider;
        synchronized (this) {
            if (!(this.mSession == null || (iImsMediaCallProvider = this.mMediaController) == null)) {
                iImsMediaCallProvider.setZoom(f);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00f1, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendSessionModifyRequest(android.telecom.VideoProfile r6, android.telecom.VideoProfile r7) {
        /*
            r5 = this;
            monitor-enter(r5)
            if (r7 == 0) goto L_0x00f0
            com.sec.ims.volte2.IImsCallSession r0 = r5.mSession     // Catch:{ all -> 0x00f2 }
            if (r0 != 0) goto L_0x0009
            goto L_0x00f0
        L_0x0009:
            java.lang.String r0 = "ImsVTProviderImpl"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x00f2 }
            r1.<init>()     // Catch:{ all -> 0x00f2 }
            java.lang.String r2 = "sendSessionModifyRequest from "
            r1.append(r2)     // Catch:{ all -> 0x00f2 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x00f2 }
            r1.append(r6)     // Catch:{ all -> 0x00f2 }
            java.lang.String r6 = " to "
            r1.append(r6)     // Catch:{ all -> 0x00f2 }
            java.lang.String r6 = r7.toString()     // Catch:{ all -> 0x00f2 }
            r1.append(r6)     // Catch:{ all -> 0x00f2 }
            java.lang.String r6 = r1.toString()     // Catch:{ all -> 0x00f2 }
            android.util.Log.i(r0, r6)     // Catch:{ all -> 0x00f2 }
            com.sec.ims.volte2.data.CallProfile r6 = new com.sec.ims.volte2.data.CallProfile     // Catch:{ all -> 0x00f2 }
            r6.<init>()     // Catch:{ all -> 0x00f2 }
            r0 = 0
            r6.setCallType(r0)     // Catch:{ all -> 0x00f2 }
            int r1 = r7.getVideoState()     // Catch:{ all -> 0x00f2 }
            boolean r1 = android.telecom.VideoProfile.isAudioOnly(r1)     // Catch:{ all -> 0x00f2 }
            r2 = 1
            if (r1 == 0) goto L_0x0048
            r6.setCallType(r2)     // Catch:{ all -> 0x00f2 }
            goto L_0x0074
        L_0x0048:
            int r1 = r7.getVideoState()     // Catch:{ all -> 0x00f2 }
            boolean r1 = android.telecom.VideoProfile.isBidirectional(r1)     // Catch:{ all -> 0x00f2 }
            if (r1 == 0) goto L_0x0057
            r1 = 2
            r6.setCallType(r1)     // Catch:{ all -> 0x00f2 }
            goto L_0x0074
        L_0x0057:
            int r1 = r7.getVideoState()     // Catch:{ all -> 0x00f2 }
            boolean r1 = android.telecom.VideoProfile.isTransmissionEnabled(r1)     // Catch:{ all -> 0x00f2 }
            if (r1 == 0) goto L_0x0066
            r1 = 3
            r6.setCallType(r1)     // Catch:{ all -> 0x00f2 }
            goto L_0x0074
        L_0x0066:
            int r1 = r7.getVideoState()     // Catch:{ all -> 0x00f2 }
            boolean r1 = android.telecom.VideoProfile.isReceptionEnabled(r1)     // Catch:{ all -> 0x00f2 }
            if (r1 == 0) goto L_0x0074
            r1 = 4
            r6.setCallType(r1)     // Catch:{ all -> 0x00f2 }
        L_0x0074:
            int r1 = r6.getCallType()     // Catch:{ all -> 0x00f2 }
            if (r1 != 0) goto L_0x007c
            monitor-exit(r5)     // Catch:{ all -> 0x00f2 }
            return
        L_0x007c:
            com.sec.ims.volte2.data.MediaProfile r1 = r6.getMediaProfile()     // Catch:{ all -> 0x00f2 }
            int r3 = r7.getQuality()     // Catch:{ all -> 0x00f2 }
            int r3 = r5.convertQualityFromVideoProfile(r3)     // Catch:{ all -> 0x00f2 }
            r1.setVideoQuality(r3)     // Catch:{ all -> 0x00f2 }
            r5.mIsDummyCamera = r0     // Catch:{ all -> 0x00f2 }
            long r3 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x00f2 }
            r5.mModifyRequestTime = r3     // Catch:{ all -> 0x00f2 }
            com.sec.ims.volte2.IImsCallSession r1 = r5.mSession     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            com.sec.ims.volte2.data.CallProfile r1 = r1.getCallProfile()     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            int r1 = r1.getCallType()     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            int r3 = r6.getCallType()     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            if (r1 == r3) goto L_0x00ab
            com.sec.ims.volte2.IImsCallSession r7 = r5.mSession     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            java.lang.String r1 = ""
            r7.update(r6, r0, r1)     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            goto L_0x00ee
        L_0x00ab:
            int r6 = r7.getVideoState()     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            boolean r6 = android.telecom.VideoProfile.isPaused(r6)     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            if (r6 == 0) goto L_0x00bd
            r5.mIsVideoPause = r2     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            com.sec.ims.volte2.IImsCallSession r6 = r5.mSession     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            r6.holdVideo()     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            goto L_0x00ee
        L_0x00bd:
            int r6 = r7.getVideoState()     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            boolean r6 = android.telecom.VideoProfile.isPaused(r6)     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            if (r6 != 0) goto L_0x00ee
            boolean r6 = r5.mIsVideoPause     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            if (r6 == 0) goto L_0x00ee
            r5.mIsVideoPause = r0     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            com.sec.ims.volte2.IImsCallSession r6 = r5.mSession     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            r6.resumeVideo()     // Catch:{ RemoteException | NullPointerException -> 0x00d3 }
            goto L_0x00ee
        L_0x00d3:
            r6 = move-exception
            java.lang.String r7 = "ImsVTProviderImpl"
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ all -> 0x00f2 }
            r0.<init>()     // Catch:{ all -> 0x00f2 }
            java.lang.String r1 = "Couldn't notify due to "
            r0.append(r1)     // Catch:{ all -> 0x00f2 }
            java.lang.String r6 = r6.getMessage()     // Catch:{ all -> 0x00f2 }
            r0.append(r6)     // Catch:{ all -> 0x00f2 }
            java.lang.String r6 = r0.toString()     // Catch:{ all -> 0x00f2 }
            android.util.Log.e(r7, r6)     // Catch:{ all -> 0x00f2 }
        L_0x00ee:
            monitor-exit(r5)     // Catch:{ all -> 0x00f2 }
            return
        L_0x00f0:
            monitor-exit(r5)     // Catch:{ all -> 0x00f2 }
            return
        L_0x00f2:
            r6 = move-exception
            monitor-exit(r5)     // Catch:{ all -> 0x00f2 }
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.imsphone.ImsVideoCallProviderImpl.sendSessionModifyRequest(android.telecom.VideoProfile, android.telecom.VideoProfile):void");
    }

    public void sendSessionModifyResponse(VideoProfile videoProfile) throws RemoteException {
        if (videoProfile != null && this.mSession != null) {
            Log.i(LOG_TAG, "sendSessionModifyResponse " + videoProfile.toString());
            CallProfile callProfile = new CallProfile();
            callProfile.setCallType(0);
            if (VideoProfile.isAudioOnly(videoProfile.getVideoState())) {
                if (this.mSession.getCallProfile().getCallType() == 5) {
                    callProfile.setCallType(5);
                } else {
                    callProfile.setCallType(1);
                }
                if (this.mIsVideoPause) {
                    this.mIsVideoPause = false;
                }
            } else if (VideoProfile.isBidirectional(videoProfile.getVideoState())) {
                callProfile.setCallType(2);
            } else if (VideoProfile.isTransmissionEnabled(videoProfile.getVideoState())) {
                callProfile.setCallType(3);
            } else if (VideoProfile.isReceptionEnabled(videoProfile.getVideoState())) {
                callProfile.setCallType(4);
            }
            if (callProfile.getCallType() != 0) {
                callProfile.getMediaProfile().setVideoQuality(convertQualityFromVideoProfile(videoProfile.getQuality()));
                try {
                    if (this.mSession.getCallProfile().getCallType() == callProfile.getCallType()) {
                        this.mSession.reject(0);
                    } else {
                        this.mSession.accept(callProfile);
                    }
                } catch (RemoteException unused) {
                }
            }
        }
    }

    public void requestCameraCapabilities() throws RemoteException {
        int sessionId = this.mSession.getSessionId();
        int width = this.mSession.getCallProfile().getMediaProfile().getWidth();
        int height = this.mSession.getCallProfile().getMediaProfile().getHeight();
        String videoSize = this.mSession.getCallProfile().getMediaProfile().getVideoSize();
        Log.i(LOG_TAG, "requestCameraCapabilities() width : " + width + " height : " + height);
        for (IVideoServiceEventListener next : this.mRelay) {
            if (!videoSize.contains("LAND") || videoSize.contains("QCIF")) {
                next.changeCameraCapabilities(sessionId, width, height);
            } else {
                next.changeCameraCapabilities(sessionId, height, width);
            }
        }
    }

    public void requestCallDataUsage() throws RemoteException {
        IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession != null) {
            iImsCallSession.requestCallDataUsage();
        }
    }

    public void setPauseImage(Uri uri) throws RemoteException {
        if (this.mSession != null && this.mMediaController != null) {
            IMSLog.c(LogClass.VOLTE_SET_PAUSE_IMAGE, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
            if (uri != null) {
                String uri2 = uri.toString();
                Log.d(LOG_TAG, "sendStillImage filePath " + uri2);
                this.mMediaController.sendStillImage(this.mSession.getSessionId(), uri2, 256, "VGA", 0);
                return;
            }
            if (!this.mSession.getUsingCamera() && this.mSession.getSessionId() > 0) {
                this.mSession.startCameraForProvider(-1);
            }
            this.mMediaController.sendLiveVideo(this.mSession.getSessionId());
        }
    }

    private class ImsVideoCallEventListener extends IVideoServiceEventListener.Stub {
        private IImsVideoCallCallback mCallback;

        private int convertQualityToVideoProfile(int i) {
            if (i == 12) {
                return 3;
            }
            if (i != 13) {
                return i != 15 ? 4 : 1;
            }
            return 2;
        }

        private int convertStateToVideoProfile(int i) {
            if (i != 1) {
                if (i == 2) {
                    return 3;
                }
                if (i == 3) {
                    return 1;
                }
                if (i == 4) {
                    return 2;
                }
            }
            return 0;
        }

        public void onVideoOrientChanged(int i) throws RemoteException {
        }

        public ImsVideoCallEventListener(IImsVideoCallCallback iImsVideoCallCallback) {
            this.mCallback = iImsVideoCallCallback;
        }

        public IImsCallSession getSession() {
            return ImsVideoCallProviderImpl.this.mSession;
        }

        public void receiveSessionModifyRequest(int i, CallProfile callProfile) throws RemoteException {
            if (!checkInvalidStatus(i)) {
                Log.i(ImsVideoCallProviderImpl.LOG_TAG, "receiveSessionModifyRequest()");
                VideoProfile convertCallProfileToVideoProfile = convertCallProfileToVideoProfile(callProfile);
                if (convertCallProfileToVideoProfile != null) {
                    try {
                        this.mCallback.receiveSessionModifyRequest(convertCallProfileToVideoProfile);
                    } catch (RemoteException unused) {
                    }
                }
            }
        }

        public void receiveSessionModifyResponse(int i, int i2, CallProfile callProfile, CallProfile callProfile2) throws RemoteException {
            if (!checkInvalidStatus(i)) {
                Log.i(ImsVideoCallProviderImpl.LOG_TAG, "receiveSessionModifyResponse()");
                VideoProfile convertCallProfileToVideoProfile = convertCallProfileToVideoProfile(callProfile);
                VideoProfile convertCallProfileToVideoProfile2 = convertCallProfileToVideoProfile(callProfile2);
                int i3 = 0;
                if (i2 == 200) {
                    if (convertCallProfileToVideoProfile != null && convertCallProfileToVideoProfile2 != null) {
                        if (callProfile2.getCallType() == 1) {
                            ImsVideoCallProviderImpl.this.mIsVideoPause = false;
                        }
                        IMSLog.i(ImsVideoCallProviderImpl.LOG_TAG, "#IMSCALL - Call Modify KPI - Success : " + (((double) (System.currentTimeMillis() - ImsVideoCallProviderImpl.this.mModifyRequestTime)) / 1000.0d));
                        i3 = 1;
                    } else {
                        return;
                    }
                } else if (i2 == ImsVideoCallProviderImpl.RECORD_START_FAILURE_NO_SPACE) {
                    IMSLog.i(ImsVideoCallProviderImpl.LOG_TAG, "#IMSCALL - Call Modify KPI - Rejected : " + (((double) (System.currentTimeMillis() - ImsVideoCallProviderImpl.this.mModifyRequestTime)) / 1000.0d));
                    i3 = 5;
                } else if (i2 == 1109 || i2 == 487) {
                    IMSLog.i(ImsVideoCallProviderImpl.LOG_TAG, "#IMSCALL - Call Modify KPI - Failure : " + (((double) (System.currentTimeMillis() - ImsVideoCallProviderImpl.this.mModifyRequestTime)) / 1000.0d));
                    i3 = 2;
                }
                try {
                    this.mCallback.receiveSessionModifyResponse(i3, convertCallProfileToVideoProfile, convertCallProfileToVideoProfile2);
                } catch (RemoteException unused) {
                }
            }
        }

        public void onCameraState(int i, int i2) throws RemoteException {
            if (!checkInvalidStatus(i)) {
                Log.i(ImsVideoCallProviderImpl.LOG_TAG, "onCameraState() " + i2);
                IMSLog.c(LogClass.VOLTE_CHANGE_CAMERA_STATE, ImsVideoCallProviderImpl.this.mSession.getPhoneId() + "," + i + "," + i2);
                switch (i2) {
                    case 0:
                        this.mCallback.handleCallSessionEvent(3);
                        return;
                    case 1:
                    case 5:
                        this.mCallback.handleCallSessionEvent(6);
                        return;
                    case 2:
                    case 4:
                    case 6:
                    case 7:
                        this.mCallback.handleCallSessionEvent(5);
                        return;
                    case 3:
                        try {
                            this.mCallback.handleCallSessionEvent(4);
                            return;
                        } catch (RemoteException unused) {
                            return;
                        }
                    default:
                        return;
                }
            }
        }

        public void onVideoState(int i, int i2) throws RemoteException {
            if (!checkInvalidStatus(i)) {
                Log.i(ImsVideoCallProviderImpl.LOG_TAG, "onVideoState() " + i2);
                IMSLog.c(LogClass.VOLTE_CHANGE_VIDEO_STATE, ImsVideoCallProviderImpl.this.mSession.getPhoneId() + "," + i + "," + i2);
                if (i2 == 0) {
                    this.mCallback.handleCallSessionEvent(2);
                } else if (i2 == 1) {
                    this.mCallback.handleCallSessionEvent(1);
                } else if (i2 == 2) {
                    ImsVideoCallProviderImpl.this.mIsVideoPause = false;
                    this.mCallback.handleCallSessionEvent(1000);
                } else if (i2 == 3) {
                    try {
                        this.mCallback.handleCallSessionEvent(1001);
                    } catch (RemoteException unused) {
                    }
                }
            }
        }

        public void onVideoQualityChanged(int i, int i2) throws RemoteException {
            if (!checkInvalidStatus(i)) {
                Log.i(ImsVideoCallProviderImpl.LOG_TAG, "onVideoQualityChanged() " + i2);
                if (i2 == 0) {
                    this.mCallback.changeVideoQuality(3);
                } else if (i2 == 1) {
                    this.mCallback.changeVideoQuality(2);
                } else if (i2 == 2) {
                    try {
                        this.mCallback.changeVideoQuality(1);
                    } catch (RemoteException unused) {
                    }
                }
            }
        }

        public void onChangePeerDimension(int i, int i2, int i3) throws RemoteException {
            if (!checkInvalidStatus(i)) {
                Log.i(ImsVideoCallProviderImpl.LOG_TAG, "onChangePeerDimension() width : " + i2 + " height : " + i3);
                try {
                    this.mCallback.changePeerDimensions(i2, i3);
                } catch (RemoteException unused) {
                }
            }
        }

        public void setVideoPause(int i, boolean z) throws RemoteException {
            ImsVideoCallProviderImpl imsVideoCallProviderImpl = ImsVideoCallProviderImpl.this;
            IImsCallSession iImsCallSession = imsVideoCallProviderImpl.mSession;
            if (iImsCallSession != null && imsVideoCallProviderImpl.mMediaController != null && i == iImsCallSession.getSessionId()) {
                ImsVideoCallProviderImpl.this.mIsVideoPause = z;
            }
        }

        public void changeCameraCapabilities(int i, int i2, int i3) throws RemoteException {
            if (!checkInvalidStatus(i)) {
                Log.i(ImsVideoCallProviderImpl.LOG_TAG, "changeCameraCapabilities() width : " + i2 + " height : " + i3);
                try {
                    this.mCallback.changeCameraCapabilities(new VideoProfile.CameraCapabilities(i2, i3));
                } catch (RemoteException unused) {
                }
            }
        }

        public void onRecordState(int i, int i2) throws RemoteException {
            ImsVideoCallProviderImpl imsVideoCallProviderImpl = ImsVideoCallProviderImpl.this;
            if (imsVideoCallProviderImpl.mSession != null && imsVideoCallProviderImpl.mMediaController != null && this.mCallback != null) {
                Log.i(ImsVideoCallProviderImpl.LOG_TAG, "onRecordState() " + i2);
                if (i2 == 0) {
                    this.mCallback.handleCallSessionEvent(1100);
                } else if (i2 == 1) {
                    this.mCallback.handleCallSessionEvent(1101);
                } else if (i2 == 2) {
                    this.mCallback.handleCallSessionEvent(ImsVideoCallProviderImpl.RECORD_START_FAILURE_NO_SPACE);
                } else if (i2 == 3) {
                    this.mCallback.handleCallSessionEvent(1102);
                } else if (i2 == 4) {
                    this.mCallback.handleCallSessionEvent(1103);
                } else if (i2 == 5) {
                    try {
                        this.mCallback.handleCallSessionEvent(1111);
                    } catch (RemoteException unused) {
                    }
                }
            }
        }

        public void onEmojiState(int i, int i2) throws RemoteException {
            ImsVideoCallProviderImpl imsVideoCallProviderImpl = ImsVideoCallProviderImpl.this;
            if (imsVideoCallProviderImpl.mSession != null && imsVideoCallProviderImpl.mMediaController != null && this.mCallback != null) {
                Log.i(ImsVideoCallProviderImpl.LOG_TAG, "onEmojiState() " + i2);
                if (i2 == 0) {
                    this.mCallback.handleCallSessionEvent(1200);
                } else if (i2 == 1) {
                    this.mCallback.handleCallSessionEvent(1201);
                } else if (i2 == 2) {
                    this.mCallback.handleCallSessionEvent(ImsVideoCallProviderImpl.EMOJI_STOP_SUCCESS);
                } else if (i2 == 3) {
                    try {
                        this.mCallback.handleCallSessionEvent(ImsVideoCallProviderImpl.EMOJI_STOP_FAILURE);
                    } catch (RemoteException unused) {
                    }
                }
            }
        }

        public void onChangeCallDataUsage(int i, long j) throws RemoteException {
            if (!checkInvalidStatus(i)) {
                try {
                    this.mCallback.changeCallDataUsage(j);
                } catch (RemoteException unused) {
                }
            }
        }

        private boolean checkInvalidStatus(int i) throws RemoteException {
            ImsVideoCallProviderImpl imsVideoCallProviderImpl = ImsVideoCallProviderImpl.this;
            IImsCallSession iImsCallSession = imsVideoCallProviderImpl.mSession;
            return iImsCallSession == null || imsVideoCallProviderImpl.mMediaController == null || i != iImsCallSession.getSessionId() || this.mCallback == null;
        }

        private VideoProfile convertCallProfileToVideoProfile(CallProfile callProfile) {
            if (callProfile == null) {
                return null;
            }
            int convertStateToVideoProfile = convertStateToVideoProfile(callProfile.getCallType());
            if (callProfile.getMediaProfile().getVideoPause()) {
                convertStateToVideoProfile |= 4;
            }
            int convertQualityToVideoProfile = convertQualityToVideoProfile(callProfile.getMediaProfile().getVideoQuality());
            if (callProfile.getCallType() == 0) {
                return null;
            }
            return new VideoProfile(convertStateToVideoProfile, convertQualityToVideoProfile);
        }
    }
}
