package com.sec.internal.ims.util.httpclient;

import com.sec.internal.helper.httpclient.HttpRequestParams;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class GbaHttpController$3$$ExternalSyntheticLambda1 implements Function {
    public final /* synthetic */ HttpRequestParams f$0;

    public /* synthetic */ GbaHttpController$3$$ExternalSyntheticLambda1(HttpRequestParams httpRequestParams) {
        this.f$0 = httpRequestParams;
    }

    public final Object apply(Object obj) {
        return Boolean.valueOf(this.f$0.getUrl().contains((String) obj));
    }
}
