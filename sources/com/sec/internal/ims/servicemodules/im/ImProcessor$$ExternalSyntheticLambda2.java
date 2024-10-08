package com.sec.internal.ims.servicemodules.im;

import java.util.List;
import java.util.concurrent.Callable;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImProcessor$$ExternalSyntheticLambda2 implements Callable {
    public final /* synthetic */ ImProcessor f$0;
    public final /* synthetic */ List f$1;
    public final /* synthetic */ boolean f$2;

    public /* synthetic */ ImProcessor$$ExternalSyntheticLambda2(ImProcessor imProcessor, List list, boolean z) {
        this.f$0 = imProcessor;
        this.f$1 = list;
        this.f$2 = z;
    }

    public final Object call() {
        return this.f$0.lambda$deleteMessages$3(this.f$1, this.f$2);
    }
}
