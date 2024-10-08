package com.sec.internal.ims.cmstore.params;

import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;

public class HttpResParamsWrapper {
    public IHttpAPICommonInterface mApi;
    public Object mBufDbParams;

    public HttpResParamsWrapper(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
        this.mApi = iHttpAPICommonInterface;
        this.mBufDbParams = obj;
    }
}
