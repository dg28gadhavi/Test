package com.sec.internal.ims.servicemodules.im;

import java.util.Map;
import java.util.concurrent.Callable;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImProcessor$$ExternalSyntheticLambda7 implements Callable {
    public final /* synthetic */ ImProcessor f$0;
    public final /* synthetic */ Map f$1;
    public final /* synthetic */ boolean f$2;
    public final /* synthetic */ String f$3;

    public /* synthetic */ ImProcessor$$ExternalSyntheticLambda7(ImProcessor imProcessor, Map map, boolean z, String str) {
        this.f$0 = imProcessor;
        this.f$1 = map;
        this.f$2 = z;
        this.f$3 = str;
    }

    public final Object call() {
        return this.f$0.lambda$deleteMessagesByImdnId$4(this.f$1, this.f$2, this.f$3);
    }
}
