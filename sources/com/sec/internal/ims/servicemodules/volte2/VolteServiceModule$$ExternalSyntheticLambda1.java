package com.sec.internal.ims.servicemodules.volte2;

import com.sec.internal.ims.servicemodules.volte2.data.IncomingCallEvent;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class VolteServiceModule$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ VolteServiceModule f$0;
    public final /* synthetic */ IncomingCallEvent f$1;

    public /* synthetic */ VolteServiceModule$$ExternalSyntheticLambda1(VolteServiceModule volteServiceModule, IncomingCallEvent incomingCallEvent) {
        this.f$0 = volteServiceModule;
        this.f$1 = incomingCallEvent;
    }

    public final void run() {
        this.f$0.lambda$handleMessage$1(this.f$1);
    }
}
