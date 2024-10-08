package com.sec.internal.ims.imsservice;

import android.os.IBinder;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsServiceStub$$ExternalSyntheticLambda2 implements IBinder.DeathRecipient {
    public final /* synthetic */ ImsServiceStub f$0;

    public /* synthetic */ ImsServiceStub$$ExternalSyntheticLambda2(ImsServiceStub imsServiceStub) {
        this.f$0 = imsServiceStub;
    }

    public final void binderDied() {
        this.f$0.lambda$linkToPhoneDeath$14();
    }
}
