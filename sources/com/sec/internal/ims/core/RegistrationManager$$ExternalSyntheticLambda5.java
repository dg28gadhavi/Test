package com.sec.internal.ims.core;

import com.sec.ims.ImsRegistration;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationManager$$ExternalSyntheticLambda5 implements Predicate {
    public final /* synthetic */ int f$0;

    public /* synthetic */ RegistrationManager$$ExternalSyntheticLambda5(int i) {
        this.f$0 = i;
    }

    public final boolean test(Object obj) {
        return RegistrationManager.lambda$getPreferredImpuOnPdn$3(this.f$0, (ImsRegistration) obj);
    }
}
