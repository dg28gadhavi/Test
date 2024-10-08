package com.sec.internal.ims.core;

import com.sec.internal.helper.os.CellIdentityWrapper;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class PdnController$$ExternalSyntheticLambda2 implements Predicate {
    public final /* synthetic */ int f$0;

    public /* synthetic */ PdnController$$ExternalSyntheticLambda2(int i) {
        this.f$0 = i;
    }

    public final boolean test(Object obj) {
        return ((CellIdentityWrapper) obj).isMatched(this.f$0);
    }
}
