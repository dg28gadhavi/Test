package com.sec.internal.constants.ims.servicemodules.im.result;

import com.sec.internal.constants.ims.servicemodules.im.ImError;

public class StopImSessionResult {
    public ImError mError;
    public Object mRawHandle;

    public StopImSessionResult(Object obj, ImError imError) {
        this.mRawHandle = obj;
        this.mError = imError;
    }

    public String toString() {
        return "StopImSessionResult [mRawHandle=" + this.mRawHandle + ", mError=" + this.mError + "]";
    }
}
