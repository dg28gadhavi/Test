package com.sec.internal.ims.servicemodules.im;

import com.sec.internal.constants.ims.servicemodules.im.event.ImComposingEvent;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImSessionProcessor$$ExternalSyntheticLambda4 implements Runnable {
    public final /* synthetic */ ImSessionProcessor f$0;
    public final /* synthetic */ ImComposingEvent f$1;

    public /* synthetic */ ImSessionProcessor$$ExternalSyntheticLambda4(ImSessionProcessor imSessionProcessor, ImComposingEvent imComposingEvent) {
        this.f$0 = imSessionProcessor;
        this.f$1 = imComposingEvent;
    }

    public final void run() {
        this.f$0.lambda$onComposingNotificationReceived$1(this.f$1);
    }
}
