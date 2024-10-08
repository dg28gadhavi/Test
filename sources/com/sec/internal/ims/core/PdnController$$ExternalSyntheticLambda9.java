package com.sec.internal.ims.core;

import com.sec.internal.interfaces.ims.core.PdnEventListener;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class PdnController$$ExternalSyntheticLambda9 implements Runnable {
    public final /* synthetic */ PdnEventListener f$0;
    public final /* synthetic */ int f$1;
    public final /* synthetic */ NetworkCallback f$2;

    public /* synthetic */ PdnController$$ExternalSyntheticLambda9(PdnEventListener pdnEventListener, int i, NetworkCallback networkCallback) {
        this.f$0 = pdnEventListener;
        this.f$1 = i;
        this.f$2 = networkCallback;
    }

    public final void run() {
        this.f$0.onConnected(this.f$1, this.f$2.mNetwork);
    }
}
