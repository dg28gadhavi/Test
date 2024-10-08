package com.sec.internal.imsphone;

import com.sec.internal.constants.ims.SipMsg;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipTransportImpl$$ExternalSyntheticLambda5 implements Predicate {
    public final /* synthetic */ SipMsg f$0;

    public /* synthetic */ SipTransportImpl$$ExternalSyntheticLambda5(SipMsg sipMsg) {
        this.f$0 = sipMsg;
    }

    public final boolean test(Object obj) {
        return ((SipDelegateImpl) obj).isMatched(this.f$0);
    }
}
