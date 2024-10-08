package com.sec.internal.ims.servicemodules.volte2;

import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsCallSessionManager$$ExternalSyntheticLambda4 implements Predicate {
    public final boolean test(Object obj) {
        return ((ImsCallSession) obj).getCallProfile().isDowngradedVideoCall();
    }
}
