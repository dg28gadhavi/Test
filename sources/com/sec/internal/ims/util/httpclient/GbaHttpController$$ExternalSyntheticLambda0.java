package com.sec.internal.ims.util.httpclient;

import java.util.Map;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class GbaHttpController$$ExternalSyntheticLambda0 implements Predicate {
    public final /* synthetic */ int f$0;

    public /* synthetic */ GbaHttpController$$ExternalSyntheticLambda0(int i) {
        this.f$0 = i;
    }

    public final boolean test(Object obj) {
        return ((String) ((Map.Entry) obj).getKey()).contains("-subId" + this.f$0);
    }
}
