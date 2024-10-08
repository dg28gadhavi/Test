package com.sec.internal.ims.core;

import com.sec.ims.ImsRegistration;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationManagerInternal$$ExternalSyntheticLambda2 implements Function {
    public final Object apply(Object obj) {
        return Boolean.valueOf(((ImsRegistration) obj).hasService("im"));
    }
}
