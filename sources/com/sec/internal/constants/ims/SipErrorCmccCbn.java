package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;

public class SipErrorCmccCbn extends SipErrorBase {
    public SipErrorCmccCbn() {
        this.mDefaultRejectReason = SipErrorBase.DECLINE;
    }

    public SipError getFromRejectReason(int i) {
        if (i == 3) {
            return SipErrorBase.DECLINE;
        }
        if (i != 13) {
            return super.getFromRejectReason(i);
        }
        return SipErrorBase.BUSY_HERE;
    }
}
