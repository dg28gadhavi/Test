package com.sec.internal.constants.ims;

import java.util.Map;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class DiagnosisConstants$$ExternalSyntheticLambda0 implements Predicate {
    public final /* synthetic */ String f$0;

    public /* synthetic */ DiagnosisConstants$$ExternalSyntheticLambda0(String str) {
        this.f$0 = str;
    }

    public final boolean test(Object obj) {
        return ((String) ((Map.Entry) obj).getKey()).equals(this.f$0);
    }
}
