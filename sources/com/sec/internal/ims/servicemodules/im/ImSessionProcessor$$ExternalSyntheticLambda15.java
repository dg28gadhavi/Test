package com.sec.internal.ims.servicemodules.im;

import com.sec.internal.constants.ims.servicemodules.im.event.ImdnNotificationEvent;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImSessionProcessor$$ExternalSyntheticLambda15 implements Runnable {
    public final /* synthetic */ ImSessionProcessor f$0;
    public final /* synthetic */ ImdnNotificationEvent f$1;

    public /* synthetic */ ImSessionProcessor$$ExternalSyntheticLambda15(ImSessionProcessor imSessionProcessor, ImdnNotificationEvent imdnNotificationEvent) {
        this.f$0 = imSessionProcessor;
        this.f$1 = imdnNotificationEvent;
    }

    public final void run() {
        this.f$0.lambda$onImdnNotificationReceived$0(this.f$1);
    }
}
