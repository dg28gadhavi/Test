package com.sec.internal.ims.core;

import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationManagerHandler$$ExternalSyntheticLambda10 implements Consumer {
    public final /* synthetic */ byte[] f$0;

    public /* synthetic */ RegistrationManagerHandler$$ExternalSyntheticLambda10(byte[] bArr) {
        this.f$0 = bArr;
    }

    public final void accept(Object obj) {
        ((RegisterTask) obj).getGovernor().onWfcProfileChanged(this.f$0);
    }
}
