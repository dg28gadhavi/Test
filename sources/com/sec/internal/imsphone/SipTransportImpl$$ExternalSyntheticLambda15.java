package com.sec.internal.imsphone;

import com.sec.ims.ImsRegistration;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipTransportImpl$$ExternalSyntheticLambda15 implements Runnable {
    public final /* synthetic */ SipTransportImpl f$0;
    public final /* synthetic */ ImsRegistration f$1;

    public /* synthetic */ SipTransportImpl$$ExternalSyntheticLambda15(SipTransportImpl sipTransportImpl, ImsRegistration imsRegistration) {
        this.f$0 = sipTransportImpl;
        this.f$1 = imsRegistration;
    }

    public final void run() {
        this.f$0.lambda$notifyDeRegistering$15(this.f$1);
    }
}
