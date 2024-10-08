package com.sec.internal.ims.servicemodules.im;

import java.util.List;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImSessionProcessor$$ExternalSyntheticLambda20 implements Runnable {
    public final /* synthetic */ ImSessionProcessor f$0;
    public final /* synthetic */ List f$1;
    public final /* synthetic */ int f$2;
    public final /* synthetic */ String f$3;

    public /* synthetic */ ImSessionProcessor$$ExternalSyntheticLambda20(ImSessionProcessor imSessionProcessor, List list, int i, String str) {
        this.f$0 = imSessionProcessor;
        this.f$1 = list;
        this.f$2 = i;
        this.f$3 = str;
    }

    public final void run() {
        this.f$0.lambda$processRejoinGCSession$20(this.f$1, this.f$2, this.f$3);
    }
}
