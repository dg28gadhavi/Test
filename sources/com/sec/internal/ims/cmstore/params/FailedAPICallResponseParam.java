package com.sec.internal.ims.cmstore.params;

import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;

public class FailedAPICallResponseParam {
    private String mApiName = null;
    public String mErrorCode;
    public IHttpAPICommonInterface mRequest;

    public FailedAPICallResponseParam(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
        this.mRequest = iHttpAPICommonInterface;
        if (iHttpAPICommonInterface != null) {
            this.mApiName = iHttpAPICommonInterface.getClass().getSimpleName();
        }
        this.mErrorCode = str;
    }

    public String toString() {
        return "FailedAPICallResponseParam [mApiName=" + this.mApiName + ", mErrorCode=" + this.mErrorCode + "]";
    }
}
