package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.sec.internal.constants.ims.DiagnosisConstants;

public final class RegiType {
    public static final int DE_REGI = 1;
    public static final int PCSCF_GONE_DE_REGI = 2;
    public static final int REGI = 0;
    public static final String[] names = {DiagnosisConstants.FEATURE_REGI, "DE_REGI", "PCSCF_GONE_DE_REGI"};

    private RegiType() {
    }

    public static String name(int i) {
        return names[i];
    }
}
