package com.sec.internal.helper;

import com.sec.internal.helper.header.AuthenticationHeaders;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsCallUtil$$ExternalSyntheticLambda1 implements Function {
    public final Object apply(Object obj) {
        return ((String) obj).substring(((String) obj).indexOf(AuthenticationHeaders.HEADER_PRARAM_SPERATOR) + 1);
    }
}
