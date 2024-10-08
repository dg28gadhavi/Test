package com.sec.internal.ims.core.handler;

import android.net.Network;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.interfaces.ims.core.handler.IMediaServiceInterface;

public abstract class MediaHandler extends BaseHandler implements IMediaServiceInterface {
    protected final RegistrantList mMediaEventRegistrants = new RegistrantList();

    public void bindToNetwork(Network network) {
    }

    public void deinitSurface(boolean z) {
    }

    public void getCameraInfo(int i) {
    }

    public String getHwSupportedVideoCodecs(String str) {
        return str;
    }

    public void getMaxZoom() {
    }

    public void getZoom() {
    }

    public void holdVideo(int i, int i2) {
    }

    public void requestCallDataUsage() {
    }

    public void resetCameraId() {
    }

    public void restartEmoji(int i, int i2) {
    }

    public void resumeVideo(int i, int i2) {
    }

    public void sendGeneralBundleEvent(String str, Bundle bundle) {
    }

    public void sendGeneralEvent(int i, int i2, int i3, String str) {
    }

    public void sendRtpStatsToStack(IMSMediaEvent.AudioRtpStats audioRtpStats) {
    }

    public void sendStillImage(int i, boolean z, String str, String str2) {
    }

    public void setCamera(int i) {
    }

    public void setCameraEffect(int i) {
    }

    public void setDisplaySurface(int i, Object obj, int i2) {
    }

    public void setOrientation(int i) {
    }

    public void setPreviewResolution(int i, int i2) {
    }

    public void setPreviewSurface(int i, Object obj, int i2) {
    }

    public void setZoom(float f) {
    }

    public void startCamera(int i, int i2, int i3) {
    }

    public void startCamera(Surface surface) {
    }

    public void startEmoji(int i, int i2, String str) {
    }

    public int startLocalRingBackTone(int i, int i2, int i3) {
        return -1;
    }

    public void startRecord(int i, int i2, String str) {
    }

    public void startRender(boolean z) {
    }

    public void startVideoRenderer(Surface surface) {
    }

    public void stopCamera(int i) {
    }

    public void stopEmoji(int i, int i2) {
    }

    public int stopLocalRingBackTone() {
        return -1;
    }

    public void stopRecord(int i, int i2) {
    }

    public void stopVideoRenderer() {
    }

    public void swipeVideoSurface() {
    }

    public void switchCamera() {
    }

    protected MediaHandler(Looper looper) {
        super(looper);
    }

    public void registerForMediaEvent(Handler handler, int i, Object obj) {
        this.mMediaEventRegistrants.addUnique(handler, i, obj);
    }

    public void unregisterForMediaEvent(Handler handler) {
        this.mMediaEventRegistrants.remove(handler);
    }

    public void handleMessage(Message message) {
        int i = message.what;
        String str = this.LOG_TAG;
        Log.e(str, "Unknown event " + message.what);
    }
}
