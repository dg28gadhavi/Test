package com.sec.internal.constants.ims;

import com.sec.internal.constants.ims.SipMsg;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipMsg$HeaderName$$ExternalSyntheticLambda0 implements Predicate {
    public final /* synthetic */ SipMsg.HeaderName f$0;

    public /* synthetic */ SipMsg$HeaderName$$ExternalSyntheticLambda0(SipMsg.HeaderName headerName) {
        this.f$0 = headerName;
    }

    public final boolean test(Object obj) {
        return this.f$0.lambda$isOneOf$0((SipMsg.HeaderName) obj);
    }
}
