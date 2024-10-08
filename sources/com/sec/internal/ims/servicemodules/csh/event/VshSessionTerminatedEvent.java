package com.sec.internal.ims.servicemodules.csh.event;

public class VshSessionTerminatedEvent {
    public CshErrorReason mReason;
    public int mSessionId;

    public VshSessionTerminatedEvent(int i, CshErrorReason cshErrorReason) {
        this.mSessionId = i;
        this.mReason = cshErrorReason;
    }

    public String toString() {
        return "VshSessionTerminatedEvent [mSessionId=" + this.mSessionId + ", mReason=" + this.mReason + "]";
    }
}
