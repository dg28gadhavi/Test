package com.sec.internal.ims.core;

import com.sec.ims.ImsRegistration;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RawSipManager$$ExternalSyntheticLambda3 implements Predicate {
    public final /* synthetic */ RawSipManager f$0;

    public /* synthetic */ RawSipManager$$ExternalSyntheticLambda3(RawSipManager rawSipManager) {
        this.f$0 = rawSipManager;
    }

    public final boolean test(Object obj) {
        return this.f$0.isNonEmergency((ImsRegistration) obj);
    }
}
