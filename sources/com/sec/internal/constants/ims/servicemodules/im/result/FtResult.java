package com.sec.internal.constants.ims.servicemodules.im.result;

import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;

public class FtResult extends Result {
    public Object mRawHandle;
    public int mRetryTimer;

    public FtResult(ImError imError, Result.Type type, Object obj) {
        super(imError, type);
        this.mRawHandle = obj;
        this.mRetryTimer = 0;
    }

    public FtResult(Result result, Object obj) {
        this(result, obj, 0);
    }

    public FtResult(Result result, Object obj, int i) {
        super(result.getImError(), result.getType(), result.getSipResponse(), result.getMsrpResponse(), result.getWarningHdr(), result.getReasonHdr());
        this.mRawHandle = obj;
        this.mRetryTimer = i;
    }

    public String toString() {
        return "FtResult [" + super.toString() + ", mRawHandle=" + this.mRawHandle + ", mRetryTimer=" + this.mRetryTimer + "]";
    }
}
