package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;

public class SipErrorSprint extends SipErrorBase {
    public SipError getFromRejectReason(int i) {
        if (i != 7) {
            return super.getFromRejectReason(i);
        }
        return SipErrorBase.NOT_ACCEPTABLE_HERE;
    }
}
