package com.sec.internal.ims.core;

import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationManagerHandler$$ExternalSyntheticLambda9 implements Consumer {
    public final /* synthetic */ RegistrationManagerHandler f$0;

    public /* synthetic */ RegistrationManagerHandler$$ExternalSyntheticLambda9(RegistrationManagerHandler registrationManagerHandler) {
        this.f$0 = registrationManagerHandler;
    }

    public final void accept(Object obj) {
        this.f$0.onRcsAllowedChangedByMdm((RegisterTask) obj);
    }
}
