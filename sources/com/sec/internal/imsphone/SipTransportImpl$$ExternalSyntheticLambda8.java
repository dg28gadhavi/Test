package com.sec.internal.imsphone;

import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipTransportImpl$$ExternalSyntheticLambda8 implements Consumer {
    public final /* synthetic */ SipTransportImpl f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ String f$2;

    public /* synthetic */ SipTransportImpl$$ExternalSyntheticLambda8(SipTransportImpl sipTransportImpl, String str, String str2) {
        this.f$0 = sipTransportImpl;
        this.f$1 = str;
        this.f$2 = str2;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$onPaniUpdated$20(this.f$1, this.f$2, (SipDelegateConfig) obj);
    }
}
