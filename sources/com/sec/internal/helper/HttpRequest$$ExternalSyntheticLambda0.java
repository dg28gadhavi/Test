package com.sec.internal.helper;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class HttpRequest$$ExternalSyntheticLambda0 implements HostnameVerifier {
    public final boolean verify(String str, SSLSession sSLSession) {
        return HttpRequest.lambda$trustAllHosts$0(str, sSLSession);
    }
}
