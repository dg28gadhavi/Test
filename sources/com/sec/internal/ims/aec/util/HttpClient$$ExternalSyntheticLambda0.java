package com.sec.internal.ims.aec.util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class HttpClient$$ExternalSyntheticLambda0 implements HostnameVerifier {
    public final /* synthetic */ HttpClient f$0;

    public /* synthetic */ HttpClient$$ExternalSyntheticLambda0(HttpClient httpClient) {
        this.f$0 = httpClient;
    }

    public final boolean verify(String str, SSLSession sSLSession) {
        return this.f$0.lambda$openURLConnection$0(str, sSLSession);
    }
}
