package com.sec.internal.helper.httpclient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class HttpController$$ExternalSyntheticLambda0 implements HostnameVerifier {
    public final boolean verify(String str, SSLSession sSLSession) {
        return HttpController.lambda$createHostnameVerifier$0(str, sSLSession);
    }
}
