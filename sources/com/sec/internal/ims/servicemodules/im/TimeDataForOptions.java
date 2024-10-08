package com.sec.internal.ims.servicemodules.im;

import com.sec.ims.util.ImsUri;

public class TimeDataForOptions {
    Long time;
    ImsUri uri;

    public TimeDataForOptions(ImsUri imsUri, Long l) {
        this.uri = imsUri;
        this.time = l;
    }

    public ImsUri getUri() {
        return this.uri;
    }

    public Long getTime() {
        return this.time;
    }
}
