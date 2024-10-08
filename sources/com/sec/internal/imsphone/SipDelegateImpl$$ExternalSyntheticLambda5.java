package com.sec.internal.imsphone;

import java.util.Set;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipDelegateImpl$$ExternalSyntheticLambda5 implements Runnable {
    public final /* synthetic */ SipDelegateImpl f$0;
    public final /* synthetic */ Set f$1;

    public /* synthetic */ SipDelegateImpl$$ExternalSyntheticLambda5(SipDelegateImpl sipDelegateImpl, Set set) {
        this.f$0 = sipDelegateImpl;
        this.f$1 = set;
    }

    public final void run() {
        this.f$0.lambda$notifyRegistrationChanged$6(this.f$1);
    }
}
