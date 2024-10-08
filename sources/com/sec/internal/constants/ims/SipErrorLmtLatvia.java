package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public class SipErrorLmtLatvia extends SipErrorBase {
    public static final SipError CALL_REJECTED_BY_NOANSWER = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "No Answer");

    public SipError getFromRejectReason(int i) {
        if (i == 3) {
            return SipErrorBase.DECLINE;
        }
        if (i != 13) {
            return super.getFromRejectReason(i);
        }
        return CALL_REJECTED_BY_NOANSWER;
    }
}
