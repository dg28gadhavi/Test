package com.sec.internal.ims.core;

import com.sec.internal.ims.core.SlotBasedConfig;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationGovernorBase$$ExternalSyntheticLambda17 implements Function {
    public final Object apply(Object obj) {
        return ((SlotBasedConfig.RegisterTaskList) obj).stream().filter(new RegistrationGovernorBase$$ExternalSyntheticLambda8()).filter(new RegistrationGovernorBase$$ExternalSyntheticLambda9()).filter(new RegistrationGovernorBase$$ExternalSyntheticLambda10()).findAny();
    }
}
