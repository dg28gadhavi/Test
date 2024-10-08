package com.sec.internal.constants.ims;

import java.util.Arrays;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipMsg$$ExternalSyntheticLambda1 implements Function {
    public final Object apply(Object obj) {
        return Arrays.stream(((String) obj).split(SipMsg.REGEXP_SEMI_NOT_IN_DQUOTE));
    }
}
