package com.sec.internal.helper.httpclient;

import java.security.cert.X509Certificate;
import okhttp3.internal.tls.OkHostnameVerifier;

public class OkHostnameVerifierWrapper {
    public static synchronized boolean verify(String str, X509Certificate x509Certificate) {
        boolean verify;
        synchronized (OkHostnameVerifierWrapper.class) {
            verify = OkHostnameVerifier.INSTANCE.verify(str, x509Certificate);
        }
        return verify;
    }
}
