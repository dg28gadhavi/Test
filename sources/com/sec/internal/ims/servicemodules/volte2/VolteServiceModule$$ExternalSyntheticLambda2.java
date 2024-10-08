package com.sec.internal.ims.servicemodules.volte2;

import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class VolteServiceModule$$ExternalSyntheticLambda2 implements Runnable {
    public final /* synthetic */ VolteServiceModule f$0;
    public final /* synthetic */ CallStateEvent f$1;

    public /* synthetic */ VolteServiceModule$$ExternalSyntheticLambda2(VolteServiceModule volteServiceModule, CallStateEvent callStateEvent) {
        this.f$0 = volteServiceModule;
        this.f$1 = callStateEvent;
    }

    public final void run() {
        this.f$0.lambda$handleMessage$2(this.f$1);
    }
}
