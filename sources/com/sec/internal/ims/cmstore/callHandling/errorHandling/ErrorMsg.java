package com.sec.internal.ims.cmstore.callHandling.errorHandling;

public class ErrorMsg {
    ErrorType mType;
    public int mTypeResId;

    public ErrorMsg(ErrorType errorType, int i) {
        this.mType = errorType;
        this.mTypeResId = i;
    }

    public String toString() {
        return "ErrorMsg [mType=" + this.mType + ", mTypeResId=" + this.mTypeResId + "]";
    }
}
