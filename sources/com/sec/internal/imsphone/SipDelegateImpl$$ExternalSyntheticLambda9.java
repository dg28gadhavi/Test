package com.sec.internal.imsphone;

import com.sec.internal.constants.ims.SipMsg;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipDelegateImpl$$ExternalSyntheticLambda9 implements Predicate {
    public final /* synthetic */ SipMsg f$0;

    public /* synthetic */ SipDelegateImpl$$ExternalSyntheticLambda9(SipMsg sipMsg) {
        this.f$0 = sipMsg;
    }

    public final boolean test(Object obj) {
        return this.f$0.isFeatureTagMatched((String) obj);
    }
}
