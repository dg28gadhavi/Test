package com.sec.internal.constants.ims;

import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipMsg$$ExternalSyntheticLambda16 implements Function {
    public final Object apply(Object obj) {
        return ((String) obj).replaceAll("^.*sip:", "").replaceAll("@.+$", "");
    }
}
