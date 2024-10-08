package com.sec.internal.ims.core;

import com.sec.internal.constants.ims.core.RegistrationConstants;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationManagerHandler$$ExternalSyntheticLambda14 implements Predicate {
    public final boolean test(Object obj) {
        return ((RegisterTask) obj).isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING);
    }
}
