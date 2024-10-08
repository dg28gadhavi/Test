package com.sec.internal.ims.cmstore.callHandling.errorHandling;

public class ErrorRule {
    String mErrorCode;
    ErrorMsg mErrorMsg;
    int mFailEvent;
    RetryAttribute mRetryAttr;
    int mRetryEvent;

    public enum RetryAttribute {
        RETRY_ALLOW,
        RETRY_FORBIDDEN,
        RETRY_USE_HEADER_VALUE
    }

    public ErrorRule(String str, RetryAttribute retryAttribute, int i, int i2, ErrorMsg errorMsg) {
        this.mErrorCode = str;
        this.mRetryAttr = retryAttribute;
        this.mRetryEvent = i;
        this.mFailEvent = i2;
        this.mErrorMsg = errorMsg;
    }

    public String toString() {
        return "ErrorRule [mErrorCode=" + this.mErrorCode + ", mRetryAttr=" + this.mRetryAttr + ", mRetryEvent=" + this.mRetryEvent + ", mFailEvent=" + this.mFailEvent + ", mErrorMsg=" + this.mErrorMsg + "]";
    }
}
