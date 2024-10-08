package com.sec.internal.ims.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationEvents$$ExternalSyntheticLambda1 implements Predicate {
    public final boolean test(Object obj) {
        return Modifier.isFinal(((Field) obj).getModifiers());
    }
}
