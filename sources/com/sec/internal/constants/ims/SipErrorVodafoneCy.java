package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;

public class SipErrorVodafoneCy extends SipErrorBase {
    public SipError getFromRejectReason(int i) {
        if (i != 3) {
            return super.getFromRejectReason(i);
        }
        return SipErrorBase.DECLINE;
    }
}
