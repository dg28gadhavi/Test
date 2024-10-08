package com.sec.internal.ims.servicemodules.presence;

import com.sec.ims.ImsRegistration;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class PresenceModule$$ExternalSyntheticLambda2 implements Runnable {
    public final /* synthetic */ PresenceModule f$0;
    public final /* synthetic */ ImsRegistration f$1;

    public /* synthetic */ PresenceModule$$ExternalSyntheticLambda2(PresenceModule presenceModule, ImsRegistration imsRegistration) {
        this.f$0 = presenceModule;
        this.f$1 = imsRegistration;
    }

    public final void run() {
        this.f$0.lambda$processRegistered$1(this.f$1);
    }
}
