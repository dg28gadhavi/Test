package com.sec.internal.ims.servicemodules.im;

import java.util.HashSet;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImCache$$ExternalSyntheticLambda1 implements Consumer {
    public final /* synthetic */ HashSet f$0;

    public /* synthetic */ ImCache$$ExternalSyntheticLambda1(HashSet hashSet) {
        this.f$0 = hashSet;
    }

    public final void accept(Object obj) {
        this.f$0.add((MessageBase) obj);
    }
}
