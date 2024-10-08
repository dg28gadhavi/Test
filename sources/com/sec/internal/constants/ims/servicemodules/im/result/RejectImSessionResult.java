package com.sec.internal.constants.ims.servicemodules.im.result;

import com.sec.internal.constants.ims.servicemodules.im.ImError;

public class RejectImSessionResult {
    public ImError mError;
    public Object mRawHandle;

    public RejectImSessionResult(Object obj, ImError imError) {
        this.mRawHandle = obj;
        this.mError = imError;
    }

    public String toString() {
        return "RejectImSessionResult [mRawHandle=" + this.mRawHandle + ", mError=" + this.mError + "]";
    }
}
