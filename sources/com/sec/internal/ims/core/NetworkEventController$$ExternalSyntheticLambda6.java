package com.sec.internal.ims.core;

import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class NetworkEventController$$ExternalSyntheticLambda6 implements Consumer {
    public final /* synthetic */ NetworkEventController f$0;
    public final /* synthetic */ int f$1;
    public final /* synthetic */ int f$2;
    public final /* synthetic */ int f$3;

    public /* synthetic */ NetworkEventController$$ExternalSyntheticLambda6(NetworkEventController networkEventController, int i, int i2, int i3) {
        this.f$0 = networkEventController;
        this.f$1 = i;
        this.f$2 = i2;
        this.f$3 = i3;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$onPdnFailed$3(this.f$1, this.f$2, this.f$3, (RegisterTask) obj);
    }
}
