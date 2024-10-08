package com.sec.internal.ims.servicemodules.volte2;

import android.telephony.BarringInfo;
import android.util.Log;

public class SsacInfo {
    private static String LOG_TAG = "SsacInfo";
    private boolean mKnownVideoBarringType;
    private boolean mKnownVoiceBarringType;
    private int mVideoFactor;
    private int mVideoTime;
    private int mVoiceFactor;
    private int mVoiceTime;

    SsacInfo(int i, int i2, boolean z, int i3, int i4, boolean z2) {
        this.mVoiceFactor = i;
        this.mVideoFactor = i3;
        this.mVoiceTime = i2;
        this.mVideoTime = i4;
        this.mKnownVoiceBarringType = z;
        this.mKnownVideoBarringType = z2;
    }

    SsacInfo(int i, int i2, int i3, int i4) {
        this(i, i2, true, i3, i4, true);
    }

    SsacInfo(BarringInfo barringInfo) {
        BarringInfo.BarringServiceInfo barringServiceInfo = barringInfo.getBarringServiceInfo(6);
        BarringInfo.BarringServiceInfo barringServiceInfo2 = barringInfo.getBarringServiceInfo(7);
        String str = LOG_TAG;
        Log.i(str, "voiceBarringInfo mConditionalBarringFactor:" + barringServiceInfo.getConditionalBarringFactor() + " mConditionalBarringTimeSeconds:" + barringServiceInfo.getConditionalBarringTimeSeconds());
        String str2 = LOG_TAG;
        Log.i(str2, "videoBarringInfo mConditionalBarringFactor:" + barringServiceInfo2.getConditionalBarringFactor() + " mConditionalBarringTimeSeconds:" + barringServiceInfo2.getConditionalBarringTimeSeconds());
        this.mVideoFactor = 100;
        this.mVoiceFactor = 100;
        boolean z = false;
        this.mVideoTime = 0;
        this.mVoiceTime = 0;
        int barringType = barringServiceInfo.getBarringType();
        int barringType2 = barringServiceInfo2.getBarringType();
        if (barringType == 1) {
            this.mVoiceFactor = barringServiceInfo.getConditionalBarringFactor();
            this.mVoiceTime = barringServiceInfo.getConditionalBarringTimeSeconds();
        }
        if (barringType2 == 1) {
            this.mVideoFactor = barringServiceInfo2.getConditionalBarringFactor();
            this.mVideoTime = barringServiceInfo2.getConditionalBarringTimeSeconds();
        }
        this.mKnownVoiceBarringType = barringType == 1 || barringType == 2;
        this.mKnownVideoBarringType = (barringType2 == 1 || barringType2 == 2) ? true : z;
    }

    public int getVoiceFactor() {
        return this.mVoiceFactor;
    }

    public void setVoiceFactor(int i) {
        this.mVoiceFactor = i;
    }

    public int getVideoFactor() {
        return this.mVideoFactor;
    }

    public void setVideoFactor(int i) {
        this.mVideoFactor = i;
    }

    public int getVoiceTime() {
        return this.mVoiceTime;
    }

    public void setVoiceTime(int i) {
        this.mVoiceTime = i;
    }

    public int getVideoTime() {
        return this.mVideoTime;
    }

    public void setVideoTime(int i) {
        this.mVideoTime = i;
    }

    public boolean isKnownVoiceBarringType() {
        return this.mKnownVoiceBarringType;
    }

    public void setKnownVoiceBarringType(boolean z) {
        this.mKnownVoiceBarringType = z;
    }

    public boolean isKnownVideoBarringType() {
        return this.mKnownVideoBarringType;
    }

    public void setKnownVideoBarringType(boolean z) {
        this.mKnownVideoBarringType = z;
    }
}
