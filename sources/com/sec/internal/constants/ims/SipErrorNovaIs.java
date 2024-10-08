package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;

public class SipErrorNovaIs extends SipErrorBase {
    public SipError getFromRejectReason(int i) {
        if (i != 13) {
            return super.getFromRejectReason(i);
        }
        return SipErrorBase.REQUEST_TIMEOUT;
    }
}
