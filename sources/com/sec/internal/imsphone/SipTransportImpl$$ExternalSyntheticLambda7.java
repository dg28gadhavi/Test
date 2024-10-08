package com.sec.internal.imsphone;

import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipTransportImpl$$ExternalSyntheticLambda7 implements Predicate {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ String f$1;

    public /* synthetic */ SipTransportImpl$$ExternalSyntheticLambda7(String str, String str2) {
        this.f$0 = str;
        this.f$1 = str2;
    }

    public final boolean test(Object obj) {
        return ((SipDelegateConfig) obj).isPaniChanged(this.f$0, this.f$1);
    }
}
