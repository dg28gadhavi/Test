package com.sec.internal.ims.core;

import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationGovernorBase$$ExternalSyntheticLambda11 implements Predicate {
    public final boolean test(Object obj) {
        return ((IRegisterTask) obj).isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.REGISTERING);
    }
}
