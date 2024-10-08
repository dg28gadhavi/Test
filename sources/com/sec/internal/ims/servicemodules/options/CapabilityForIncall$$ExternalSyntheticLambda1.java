package com.sec.internal.ims.servicemodules.options;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class CapabilityForIncall$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ CapabilityForIncall f$0;
    public final /* synthetic */ CopyOnWriteArrayList f$1;
    public final /* synthetic */ int f$2;
    public final /* synthetic */ Map f$3;

    public /* synthetic */ CapabilityForIncall$$ExternalSyntheticLambda1(CapabilityForIncall capabilityForIncall, CopyOnWriteArrayList copyOnWriteArrayList, int i, Map map) {
        this.f$0 = capabilityForIncall;
        this.f$1 = copyOnWriteArrayList;
        this.f$2 = i;
        this.f$3 = map;
    }

    public final void run() {
        this.f$0.lambda$processCallStateChanged$0(this.f$1, this.f$2, this.f$3);
    }
}
