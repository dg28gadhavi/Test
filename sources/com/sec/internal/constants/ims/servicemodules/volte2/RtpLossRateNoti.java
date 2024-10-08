package com.sec.internal.constants.ims.servicemodules.volte2;

public class RtpLossRateNoti {
    int mInterval;
    float mJitter;
    float mLossRate;
    int mNotification;

    public RtpLossRateNoti(int i, float f, float f2, int i2) {
        this.mInterval = i;
        this.mLossRate = f;
        this.mJitter = f2;
        this.mNotification = i2;
    }

    public int getInterval() {
        return this.mInterval;
    }

    public float getLossRate() {
        return this.mLossRate;
    }

    public float getJitter() {
        return this.mJitter;
    }

    public int getNotification() {
        return this.mNotification;
    }
}
