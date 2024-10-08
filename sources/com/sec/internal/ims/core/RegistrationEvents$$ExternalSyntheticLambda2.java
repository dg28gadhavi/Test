package com.sec.internal.ims.core;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationEvents$$ExternalSyntheticLambda2 implements Predicate {
    public final boolean test(Object obj) {
        return ((Field) obj).getType().isAssignableFrom(Integer.TYPE);
    }
}
