package com.sec.internal.omanetapi.common.data;

import java.util.Arrays;

public class RequestError {
    public Link[] link;
    public PolicyException policyException;
    public ServiceException serviceException;

    public String toString() {
        return "RequestError{ link: " + Arrays.toString(this.link) + " serviceException: " + this.serviceException + " policyException: " + this.policyException + " }";
    }
}
