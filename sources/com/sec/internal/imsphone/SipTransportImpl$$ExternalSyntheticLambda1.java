package com.sec.internal.imsphone;

import com.sec.ims.ImsRegistration;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipTransportImpl$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ SipTransportImpl f$0;
    public final /* synthetic */ ImsRegistration f$1;
    public final /* synthetic */ boolean f$2;

    public /* synthetic */ SipTransportImpl$$ExternalSyntheticLambda1(SipTransportImpl sipTransportImpl, ImsRegistration imsRegistration, boolean z) {
        this.f$0 = sipTransportImpl;
        this.f$1 = imsRegistration;
        this.f$2 = z;
    }

    public final void run() {
        this.f$0.lambda$onRegistrationChanged$8(this.f$1, this.f$2);
    }
}
