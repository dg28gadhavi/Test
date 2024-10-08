package com.sec.internal.ims.servicemodules.csh.event;

public class VshSessionEstablishedEvent {
    public VshResolution mResolution;
    public int mSessionId;

    public VshSessionEstablishedEvent(int i, VshResolution vshResolution) {
        this.mSessionId = i;
        this.mResolution = vshResolution;
    }

    public String toString() {
        return "VshSessionEstablishedEvent [mSessionId=" + this.mSessionId + ", mResolution=" + this.mResolution + "]";
    }
}
