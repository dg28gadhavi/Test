package com.sec.internal.ims.servicemodules.im;

import java.util.List;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImSessionProcessor$$ExternalSyntheticLambda12 implements Runnable {
    public final /* synthetic */ ImSessionProcessor f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ List f$2;

    public /* synthetic */ ImSessionProcessor$$ExternalSyntheticLambda12(ImSessionProcessor imSessionProcessor, String str, List list) {
        this.f$0 = imSessionProcessor;
        this.f$1 = str;
        this.f$2 = list;
    }

    public final void run() {
        this.f$0.lambda$addParticipants$4(this.f$1, this.f$2);
    }
}
