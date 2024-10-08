package com.sec.internal.ims.servicemodules.im;

import java.util.List;
import java.util.concurrent.Callable;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImSessionProcessor$$ExternalSyntheticLambda1 implements Callable {
    public final /* synthetic */ ImSessionProcessor f$0;
    public final /* synthetic */ List f$1;
    public final /* synthetic */ boolean f$2;

    public /* synthetic */ ImSessionProcessor$$ExternalSyntheticLambda1(ImSessionProcessor imSessionProcessor, List list, boolean z) {
        this.f$0 = imSessionProcessor;
        this.f$1 = list;
        this.f$2 = z;
    }

    public final Object call() {
        return this.f$0.lambda$deleteChats$10(this.f$1, this.f$2);
    }
}
