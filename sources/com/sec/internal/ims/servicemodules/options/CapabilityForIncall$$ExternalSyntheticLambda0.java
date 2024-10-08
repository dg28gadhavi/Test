package com.sec.internal.ims.servicemodules.options;

import java.util.concurrent.CopyOnWriteArrayList;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class CapabilityForIncall$$ExternalSyntheticLambda0 implements Runnable {
    public final /* synthetic */ CapabilityForIncall f$0;
    public final /* synthetic */ CopyOnWriteArrayList f$1;
    public final /* synthetic */ int f$2;

    public /* synthetic */ CapabilityForIncall$$ExternalSyntheticLambda0(CapabilityForIncall capabilityForIncall, CopyOnWriteArrayList copyOnWriteArrayList, int i) {
        this.f$0 = capabilityForIncall;
        this.f$1 = copyOnWriteArrayList;
        this.f$2 = i;
    }

    public final void run() {
        this.f$0.lambda$processCallStateChangedOnDeregi$1(this.f$1, this.f$2);
    }
}
