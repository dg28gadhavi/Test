package com.sec.internal.ims.core.handler.secims;

import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class UserAgent$$ExternalSyntheticLambda0 implements Predicate {
    public final /* synthetic */ ImsUri.UriType f$0;

    public /* synthetic */ UserAgent$$ExternalSyntheticLambda0(ImsUri.UriType uriType) {
        this.f$0 = uriType;
    }

    public final boolean test(Object obj) {
        return UserAgent.lambda$getFirstImpuByUriType$0(this.f$0, (NameAddr) obj);
    }
}
