package com.sec.internal.constants.ims;

import com.sec.internal.constants.ims.SipMsg;
import java.util.List;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipMsg$$ExternalSyntheticLambda11 implements Function {
    public final /* synthetic */ SipMsg.HeaderName f$0;

    public /* synthetic */ SipMsg$$ExternalSyntheticLambda11(SipMsg.HeaderName headerName) {
        this.f$0 = headerName;
    }

    public final Object apply(Object obj) {
        return SipMsg.lambda$getHeader$5(this.f$0, (List) obj);
    }
}
