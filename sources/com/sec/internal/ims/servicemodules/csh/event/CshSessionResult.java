package com.sec.internal.ims.servicemodules.csh.event;

public class CshSessionResult {
    public CshErrorReason mReason;
    public int mSessionNumber;

    public CshSessionResult(int i, CshErrorReason cshErrorReason) {
        this.mSessionNumber = i;
        this.mReason = cshErrorReason;
    }
}
