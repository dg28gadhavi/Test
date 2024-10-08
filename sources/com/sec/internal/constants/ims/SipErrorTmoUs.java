package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import java.util.Optional;
import java.util.function.Predicate;

public class SipErrorTmoUs extends SipErrorBase {
    private static final Predicate<String> CAUSE_IS_200 = new SipErrorTmoUs$$ExternalSyntheticLambda0();
    private static final Predicate<String> TEXT_CONTAINS_LOCATION = new SipErrorTmoUs$$ExternalSyntheticLambda1();
    public static final SipError USER_NOT_REGISTERED_NR_NOWARNING = new SipError(403, "Forbidden - No Warning");
    public static final SipError VERSION_NOT_SUPPORTED = new SipError(Id.REQUEST_IM_SEND_COMPOSING_STATUS, "SIP Version Not Supported");

    public boolean requireSmsCsfb() {
        return false;
    }

    public static boolean isCountryBlockingForbidden(SipError sipError) {
        return sipError.getCode() == 403 && Optional.ofNullable(sipError.getReasonHeader()).filter(CAUSE_IS_200.and(TEXT_CONTAINS_LOCATION)).isPresent();
    }

    public boolean requireVoLteCsfb() {
        return equals(SipErrorBase.ALTERNATIVE_SERVICE) || equals(SipErrorBase.BAD_REQUEST) || equals(SipErrorBase.UNAUTHORIZED) || equals(SipErrorBase.FORBIDDEN) || equals(SipErrorBase.METHOD_NOT_ALLOWED) || equals(SipErrorBase.NOT_ACCEPTABLE);
    }
}
