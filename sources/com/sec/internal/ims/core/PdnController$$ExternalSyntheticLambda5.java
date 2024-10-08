package com.sec.internal.ims.core;

import com.sec.internal.interfaces.ims.core.PdnEventListener;
import java.util.List;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class PdnController$$ExternalSyntheticLambda5 implements Consumer {
    public final /* synthetic */ List f$0;

    public /* synthetic */ PdnController$$ExternalSyntheticLambda5(List list) {
        this.f$0 = list;
    }

    public final void accept(Object obj) {
        ((PdnEventListener) obj).onPcscfRestorationNotified(11, this.f$0);
    }
}
