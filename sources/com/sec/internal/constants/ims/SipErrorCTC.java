package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public class SipErrorCTC extends SipErrorBase {
    public static final SipError CALL_REJECTED_BY_USER = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "Call Rejected by User");

    public SipError getFromRejectReason(int i) {
        if (i == 3) {
            return CALL_REJECTED_BY_USER;
        }
        if (i != 13) {
            return super.getFromRejectReason(i);
        }
        return SipErrorBase.REQUEST_TIMEOUT;
    }
}
