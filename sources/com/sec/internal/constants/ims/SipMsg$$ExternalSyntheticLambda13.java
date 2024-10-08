package com.sec.internal.constants.ims;

import com.sec.internal.ims.core.cmc.CmcConstants;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipMsg$$ExternalSyntheticLambda13 implements Function {
    public final Object apply(Object obj) {
        return ((String) obj).replace("nonce=", "").replace(CmcConstants.E_NUM_STR_QUOTE, "").trim();
    }
}
