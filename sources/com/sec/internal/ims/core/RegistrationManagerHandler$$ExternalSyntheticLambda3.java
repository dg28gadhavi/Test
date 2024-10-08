package com.sec.internal.ims.core;

import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationManagerHandler$$ExternalSyntheticLambda3 implements Consumer {
    public final /* synthetic */ int f$0;

    public /* synthetic */ RegistrationManagerHandler$$ExternalSyntheticLambda3(int i) {
        this.f$0 = i;
    }

    public final void accept(Object obj) {
        ((RegisterTask) obj).getGovernor().onTelephonyCallStatusChanged(this.f$0);
    }
}
