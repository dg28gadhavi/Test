package com.sec.internal.ims.servicemodules.volte2.data;

public class DedicatedBearerEvent {
    private final int mBearerSessionId;
    private final int mBearerState;
    private final int mQci;

    public DedicatedBearerEvent(int i, int i2, int i3) {
        this.mBearerState = i;
        this.mQci = i2;
        this.mBearerSessionId = i3;
    }

    public int getBearerState() {
        return this.mBearerState;
    }

    public int getQci() {
        return this.mQci;
    }

    public int getBearerSessionId() {
        return this.mBearerSessionId;
    }
}
