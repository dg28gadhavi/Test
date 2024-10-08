package com.sec.internal.constants.ims;

import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipMsg$$ExternalSyntheticLambda15 implements Predicate {
    public final boolean test(Object obj) {
        return ((String) obj).matches("^.*sip:.+@.+$");
    }
}
