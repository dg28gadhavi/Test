package com.sec.internal.ims.core;

import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class NetworkEventController$$ExternalSyntheticLambda2 implements Consumer {
    public final /* synthetic */ int f$0;

    public /* synthetic */ NetworkEventController$$ExternalSyntheticLambda2(int i) {
        this.f$0 = i;
    }

    public final void accept(Object obj) {
        ((IRegistrationGovernor) obj).onPdnConnecting(this.f$0);
    }
}
