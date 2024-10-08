package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;

public class SipErrorMdmn extends SipErrorBase {
    public SipErrorMdmn() {
        this.mDefaultRejectReason = SipErrorBase.DECLINE;
    }

    public SipError getFromRejectReason(int i) {
        if (i == 3) {
            return SipErrorBase.DECLINE;
        }
        if (i == 13) {
            return SipErrorBase.NOT_ACCEPTABLE_GLOBALLY;
        }
        if (i != 15) {
            return super.getFromRejectReason(i);
        }
        return SipErrorBase.E911_NOT_ALLOWED_ON_SD;
    }
}
