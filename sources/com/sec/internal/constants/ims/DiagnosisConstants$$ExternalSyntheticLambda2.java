package com.sec.internal.constants.ims;

import java.util.Map;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class DiagnosisConstants$$ExternalSyntheticLambda2 implements Predicate {
    public final /* synthetic */ String f$0;

    public /* synthetic */ DiagnosisConstants$$ExternalSyntheticLambda2(String str) {
        this.f$0 = str;
    }

    public final boolean test(Object obj) {
        return this.f$0.contains((CharSequence) ((Map.Entry) obj).getKey());
    }
}
