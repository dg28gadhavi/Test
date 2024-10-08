package com.sec.internal.ims.core;

import java.util.Optional;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationManagerInternal$$ExternalSyntheticLambda4 implements Predicate {
    public final boolean test(Object obj) {
        return ((Boolean) Optional.ofNullable(((RegisterTask) obj).getImsRegistration()).map(new RegistrationManagerInternal$$ExternalSyntheticLambda2()).orElse(Boolean.FALSE)).booleanValue();
    }
}
