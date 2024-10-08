package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;

public class SipErrorDcm extends SipErrorBase {
    public SipError getFromRejectReason(int i) {
        if (i < 0) {
            return SipErrorBase.FORBIDDEN;
        }
        if (i == 3 || i == 12 || i == 14) {
            return SipErrorBase.FORBIDDEN;
        }
        return super.getFromRejectReason(i);
    }
}
