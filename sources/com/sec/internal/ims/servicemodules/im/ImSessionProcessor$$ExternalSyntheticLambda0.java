package com.sec.internal.ims.servicemodules.im;

import java.util.List;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImSessionProcessor$$ExternalSyntheticLambda0 implements Runnable {
    public final /* synthetic */ ImSessionProcessor f$0;
    public final /* synthetic */ List f$1;
    public final /* synthetic */ boolean f$2;
    public final /* synthetic */ boolean f$3;

    public /* synthetic */ ImSessionProcessor$$ExternalSyntheticLambda0(ImSessionProcessor imSessionProcessor, List list, boolean z, boolean z2) {
        this.f$0 = imSessionProcessor;
        this.f$1 = list;
        this.f$2 = z;
        this.f$3 = z2;
    }

    public final void run() {
        this.f$0.lambda$closeChat$19(this.f$1, this.f$2, this.f$3);
    }
}
