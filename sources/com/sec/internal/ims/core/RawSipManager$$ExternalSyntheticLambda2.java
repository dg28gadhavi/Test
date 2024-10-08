package com.sec.internal.ims.core;

import com.sec.ims.ImsRegistration;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RawSipManager$$ExternalSyntheticLambda2 implements Predicate {
    public final /* synthetic */ int f$0;

    public /* synthetic */ RawSipManager$$ExternalSyntheticLambda2(int i) {
        this.f$0 = i;
    }

    public final boolean test(Object obj) {
        return RawSipManager.lambda$getRegId$0(this.f$0, (ImsRegistration) obj);
    }
}
