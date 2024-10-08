package com.sec.internal.constants.ims;

import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipErrorTmoUs$$ExternalSyntheticLambda0 implements Predicate {
    public final boolean test(Object obj) {
        return ((String) obj).matches(".+cause\\s*=\\s*200.+");
    }
}
