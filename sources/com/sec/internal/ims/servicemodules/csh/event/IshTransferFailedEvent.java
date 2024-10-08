package com.sec.internal.ims.servicemodules.csh.event;

public class IshTransferFailedEvent {
    public CshErrorReason mReason;
    public int mSessionId;

    public IshTransferFailedEvent(int i, CshErrorReason cshErrorReason) {
        this.mSessionId = i;
        this.mReason = cshErrorReason;
    }
}
