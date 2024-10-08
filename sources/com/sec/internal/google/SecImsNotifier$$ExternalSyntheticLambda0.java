package com.sec.internal.google;

import com.sec.ims.util.NameAddr;
import com.sec.internal.helper.UriUtil;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SecImsNotifier$$ExternalSyntheticLambda0 implements Predicate {
    public final boolean test(Object obj) {
        return UriUtil.hasMsisdnNumber(((NameAddr) obj).getUri());
    }
}
