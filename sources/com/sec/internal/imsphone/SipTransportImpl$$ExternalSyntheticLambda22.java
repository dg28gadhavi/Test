package com.sec.internal.imsphone;

import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipTransportImpl$$ExternalSyntheticLambda22 implements Consumer {
    public final /* synthetic */ SipTransportImpl f$0;
    public final /* synthetic */ boolean f$1;

    public /* synthetic */ SipTransportImpl$$ExternalSyntheticLambda22(SipTransportImpl sipTransportImpl, boolean z) {
        this.f$0 = sipTransportImpl;
        this.f$1 = z;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$onRegistrationChanged$7(this.f$1, (SipDelegateImpl) obj);
    }
}
