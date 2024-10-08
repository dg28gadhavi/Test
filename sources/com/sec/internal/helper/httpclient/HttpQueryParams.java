package com.sec.internal.helper.httpclient;

import java.util.LinkedHashMap;

public class HttpQueryParams {
    private LinkedHashMap<String, String> mParams;
    private boolean mParamsEncoded;

    public HttpQueryParams(LinkedHashMap<String, String> linkedHashMap, boolean z) {
        new LinkedHashMap();
        this.mParams = linkedHashMap;
        this.mParamsEncoded = z;
    }

    public LinkedHashMap<String, String> getParams() {
        return this.mParams;
    }

    public boolean isEncoded() {
        return this.mParamsEncoded;
    }

    public String toString() {
        return this.mParams.toString();
    }
}
