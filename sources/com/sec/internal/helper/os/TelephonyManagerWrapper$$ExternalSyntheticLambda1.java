package com.sec.internal.helper.os;

import java.util.List;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class TelephonyManagerWrapper$$ExternalSyntheticLambda1 implements Consumer {
    public final /* synthetic */ List f$0;

    public /* synthetic */ TelephonyManagerWrapper$$ExternalSyntheticLambda1(List list) {
        this.f$0 = list;
    }

    public final void accept(Object obj) {
        this.f$0.set(this.f$0.indexOf((String) obj), "");
    }
}
