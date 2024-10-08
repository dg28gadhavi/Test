package com.sec.internal.ims.servicemodules.im;

import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImSessionProcessor$$ExternalSyntheticLambda7 implements Runnable {
    public final /* synthetic */ ImSessionProcessor f$0;
    public final /* synthetic */ ImIncomingSessionEvent f$1;

    public /* synthetic */ ImSessionProcessor$$ExternalSyntheticLambda7(ImSessionProcessor imSessionProcessor, ImIncomingSessionEvent imIncomingSessionEvent) {
        this.f$0 = imSessionProcessor;
        this.f$1 = imIncomingSessionEvent;
    }

    public final void run() {
        this.f$0.lambda$onIncomingSessionReceived$21(this.f$1);
    }
}
