package com.sec.internal.constants.ims;

import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipMsg$$ExternalSyntheticLambda25 implements Predicate {
    public final /* synthetic */ String f$0;

    public /* synthetic */ SipMsg$$ExternalSyntheticLambda25(String str) {
        this.f$0 = str;
    }

    public final boolean test(Object obj) {
        return ((String) obj).startsWith(this.f$0);
    }
}
