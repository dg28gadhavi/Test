package com.sec.internal.imsphone;

import android.telephony.ims.stub.SipDelegate;
import java.util.Objects;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipTransportImpl$$ExternalSyntheticLambda12 implements Predicate {
    public final /* synthetic */ SipDelegate f$0;

    public /* synthetic */ SipTransportImpl$$ExternalSyntheticLambda12(SipDelegate sipDelegate) {
        this.f$0 = sipDelegate;
    }

    public final boolean test(Object obj) {
        return Objects.equals(this.f$0, (SipDelegateImpl) obj);
    }
}
