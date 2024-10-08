package com.sec.internal.ims.servicemodules.volte2;

import com.sec.internal.helper.ImsCallUtil;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsCallSessionManager$$ExternalSyntheticLambda0 implements Predicate {
    public final boolean test(Object obj) {
        return ImsCallUtil.isVideoCall(((ImsCallSession) obj).getCallProfile().getCallType());
    }
}
