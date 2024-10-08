package com.sec.internal.ims.core;

import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationManagerHandler$$ExternalSyntheticLambda1 implements Predicate {
    public final /* synthetic */ int f$0;
    public final /* synthetic */ int f$1;

    public /* synthetic */ RegistrationManagerHandler$$ExternalSyntheticLambda1(int i, int i2) {
        this.f$0 = i;
        this.f$1 = i2;
    }

    public final boolean test(Object obj) {
        return ((RegisterTask) obj).getGovernor().onUpdatedPcoInfo(this.f$0, this.f$1);
    }
}
