package com.sec.internal.ims.cmstore.callHandling.successfullCall;

import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;

public class SuccessCallFlow {
    String mFlow;
    EnumProvision.ProvisionEventType mProvisionEventType;

    public SuccessCallFlow(String str, EnumProvision.ProvisionEventType provisionEventType) {
        this.mFlow = str;
        this.mProvisionEventType = provisionEventType;
    }
}
