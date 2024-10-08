package com.sec.internal.ims.core;

import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationManagerHandler$$ExternalSyntheticLambda11 implements Predicate {
    public final boolean test(Object obj) {
        return ((RegisterTask) obj).getProfile().hasService("mmtel");
    }
}
