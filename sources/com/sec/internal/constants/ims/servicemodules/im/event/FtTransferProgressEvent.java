package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.internal.constants.ims.servicemodules.im.result.Result;

public class FtTransferProgressEvent {
    public int mId;
    public Object mRawHandle;
    public Result mReason;
    public State mState;
    public long mTotal;
    public long mTransferred;

    public enum State {
        TRANSFERRING,
        INTERRUPTED,
        CANCELED,
        COMPLETED
    }

    public FtTransferProgressEvent(Object obj, int i, long j, long j2, State state, Result result) {
        this.mRawHandle = obj;
        this.mId = i;
        this.mTotal = j;
        this.mTransferred = j2;
        this.mState = state;
        this.mReason = result;
    }

    public String toString() {
        return "FtTransferProgressEvent [mRawHandle=" + this.mRawHandle + ", mId=" + this.mId + ", mTotal=" + this.mTotal + ", mTransferred=" + this.mTransferred + ", mState=" + this.mState + ", mReason=" + this.mReason + "]";
    }
}
