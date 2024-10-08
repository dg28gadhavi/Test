package com.sec.internal.constants.ims;

import java.util.Arrays;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipMsg$$ExternalSyntheticLambda14 implements Function {
    public final /* synthetic */ String f$0;

    public /* synthetic */ SipMsg$$ExternalSyntheticLambda14(String str) {
        this.f$0 = str;
    }

    public final Object apply(Object obj) {
        return Boolean.valueOf(Arrays.stream((String[]) obj).anyMatch(new SipMsg$$ExternalSyntheticLambda20(this.f$0)));
    }
}
