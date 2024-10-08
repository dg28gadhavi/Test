package com.sec.internal.ims.rcs;

import com.sec.internal.constants.ims.SipMsg;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RcsPolicyManager$$ExternalSyntheticLambda0 implements Predicate {
    public final boolean test(Object obj) {
        return ((String) obj).startsWith(SipMsg.FEATURE_TAG_CHATBOT_VER_PREFIX);
    }
}
