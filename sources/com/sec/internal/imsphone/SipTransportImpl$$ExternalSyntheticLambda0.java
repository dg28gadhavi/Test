package com.sec.internal.imsphone;

import com.sec.internal.constants.ims.SipMsg;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipTransportImpl$$ExternalSyntheticLambda0 implements Runnable {
    public final /* synthetic */ SipTransportImpl f$0;
    public final /* synthetic */ SipMsg f$1;

    public /* synthetic */ SipTransportImpl$$ExternalSyntheticLambda0(SipTransportImpl sipTransportImpl, SipMsg sipMsg) {
        this.f$0 = sipTransportImpl;
        this.f$1 = sipMsg;
    }

    public final void run() {
        this.f$0.lambda$notifySipMessage$5(this.f$1);
    }
}
