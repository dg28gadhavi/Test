package com.sec.internal.ims.core;

import com.sec.internal.constants.Mno;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationManagerHandler$$ExternalSyntheticLambda18 implements Runnable {
    public final /* synthetic */ RegistrationManagerHandler f$0;
    public final /* synthetic */ int f$1;
    public final /* synthetic */ Mno f$2;
    public final /* synthetic */ int f$3;

    public /* synthetic */ RegistrationManagerHandler$$ExternalSyntheticLambda18(RegistrationManagerHandler registrationManagerHandler, int i, Mno mno, int i2) {
        this.f$0 = registrationManagerHandler;
        this.f$1 = i;
        this.f$2 = mno;
        this.f$3 = i2;
    }

    public final void run() {
        this.f$0.lambda$notifySendDeRegisterRequested$15(this.f$1, this.f$2, this.f$3);
    }
}
