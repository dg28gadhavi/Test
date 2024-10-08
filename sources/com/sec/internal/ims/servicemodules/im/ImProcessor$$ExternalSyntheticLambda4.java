package com.sec.internal.ims.servicemodules.im;

import java.util.List;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImProcessor$$ExternalSyntheticLambda4 implements Runnable {
    public final /* synthetic */ ImProcessor f$0;
    public final /* synthetic */ int f$1;
    public final /* synthetic */ ImSession f$2;
    public final /* synthetic */ List f$3;

    public /* synthetic */ ImProcessor$$ExternalSyntheticLambda4(ImProcessor imProcessor, int i, ImSession imSession, List list) {
        this.f$0 = imProcessor;
        this.f$1 = i;
        this.f$2 = imSession;
        this.f$3 = list;
    }

    public final void run() {
        this.f$0.lambda$onProcessPendingMessages$7(this.f$1, this.f$2, this.f$3);
    }
}
