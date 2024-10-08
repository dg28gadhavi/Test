package com.sec.internal.imsphone;

import com.sec.internal.imsphone.RegistrationTracker;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipTransportImpl$$ExternalSyntheticLambda10 implements Consumer {
    public final /* synthetic */ SipTransportImpl f$0;
    public final /* synthetic */ SipDelegateImpl f$1;

    public /* synthetic */ SipTransportImpl$$ExternalSyntheticLambda10(SipTransportImpl sipTransportImpl, SipDelegateImpl sipDelegateImpl) {
        this.f$0 = sipTransportImpl;
        this.f$1 = sipDelegateImpl;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$createSipDelegate$0(this.f$1, (RegistrationTracker.RegisteredToken) obj);
    }
}
