package com.sec.internal.imsphone;

import com.sec.ims.ImsRegistration;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipTransportImpl$$ExternalSyntheticLambda14 implements Consumer {
    public final /* synthetic */ ImsRegistration f$0;

    public /* synthetic */ SipTransportImpl$$ExternalSyntheticLambda14(ImsRegistration imsRegistration) {
        this.f$0 = imsRegistration;
    }

    public final void accept(Object obj) {
        ((SipDelegateImpl) obj).notifyDeRegistering(this.f$0.getDeregiReason());
    }
}
