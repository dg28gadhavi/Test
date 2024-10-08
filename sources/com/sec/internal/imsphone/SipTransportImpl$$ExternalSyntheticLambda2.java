package com.sec.internal.imsphone;

import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipTransportImpl$$ExternalSyntheticLambda2 implements Consumer {
    public final /* synthetic */ SipDelegateConfig f$0;

    public /* synthetic */ SipTransportImpl$$ExternalSyntheticLambda2(SipDelegateConfig sipDelegateConfig) {
        this.f$0 = sipDelegateConfig;
    }

    public final void accept(Object obj) {
        ((SipDelegateImpl) obj).notifyConfigurationChanged(this.f$0);
    }
}
