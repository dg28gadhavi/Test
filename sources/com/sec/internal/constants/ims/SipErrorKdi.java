package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public class SipErrorKdi extends SipErrorBase {
    public static final SipError MULTIPARTY_CALL_IS_ESTABLISHED = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "Already On Two Calls");

    public SipErrorKdi() {
        this.mDefaultRejectReason = SipErrorBase.DECLINE;
    }

    public SipError getFromRejectReason(int i) {
        if (i == 3) {
            return SipErrorBase.DECLINE;
        }
        if (i != 13) {
            return super.getFromRejectReason(i);
        }
        return SipErrorBase.SERVER_INTERNAL_ERROR;
    }
}
