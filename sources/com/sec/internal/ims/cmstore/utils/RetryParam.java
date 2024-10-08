package com.sec.internal.ims.cmstore.utils;

import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;

public class RetryParam {
    private static final String LOG_TAG = "RetryParam";
    private int errorCode;
    private long last_executed = System.currentTimeMillis();
    private IHttpAPICommonInterface mrequest;
    private int retryCount;

    public RetryParam(IHttpAPICommonInterface iHttpAPICommonInterface, int i, int i2) {
        this.retryCount = i;
        this.mrequest = iHttpAPICommonInterface;
        this.errorCode = i2;
    }

    public void setRetryCount(int i) {
        this.retryCount = i;
    }

    public int getRetryCount() {
        return this.retryCount;
    }

    public void setErrorCode(int i) {
        this.errorCode = i;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public IHttpAPICommonInterface getMrequest() {
        return this.mrequest;
    }

    public void setLastExecuted() {
        this.last_executed = System.currentTimeMillis();
    }

    public long getLastExecuted() {
        return this.last_executed;
    }

    public String toString() {
        return "Retry count :" + this.retryCount + " mRequest :" + this.mrequest + " last_executed " + this.last_executed;
    }
}
