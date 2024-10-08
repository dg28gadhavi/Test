package com.sec.internal.ims.cmstore.utils;

import android.content.Context;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.cmstore.PersistentHttp3CookieJar;

public class CmsHttpController extends HttpController {
    private String TAG = CmsHttpController.class.getSimpleName();
    public Context mContext;
    public PersistentHttp3CookieJar mCookieJar;

    public CmsHttpController(Context context, int i) {
        this.TAG += "[" + i + "]";
        this.mContext = context;
        this.mCookieJar = new PersistentHttp3CookieJar(context, i);
    }

    public PersistentHttp3CookieJar getCookieJar() {
        return this.mCookieJar;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00e8  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public okhttp3.OkHttpClient getOkHttpClient(com.sec.internal.helper.httpclient.HttpRequestParams r9) {
        /*
            r8 = this;
            java.lang.String r0 = r8.TAG
            java.lang.String r1 = "getOkHttpClient"
            com.sec.internal.log.IMSLog.i(r0, r1)
            okhttp3.OkHttpClient r0 = com.sec.internal.helper.httpclient.HttpController.sOkHttpClient
            okhttp3.OkHttpClient$Builder r0 = r0.newBuilder()
            long r1 = r9.getConnectionTimeout()
            r3 = 0
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            r5 = 2147483647(0x7fffffff, double:1.060997895E-314)
            if (r1 < 0) goto L_0x002b
            long r1 = r9.getConnectionTimeout()
            int r1 = (r1 > r5 ? 1 : (r1 == r5 ? 0 : -1))
            if (r1 > 0) goto L_0x002b
            long r1 = r9.getConnectionTimeout()
            java.util.concurrent.TimeUnit r7 = java.util.concurrent.TimeUnit.MILLISECONDS
            r0.connectTimeout(r1, r7)
        L_0x002b:
            long r1 = r9.getReadTimeout()
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r1 < 0) goto L_0x0044
            long r1 = r9.getReadTimeout()
            int r1 = (r1 > r5 ? 1 : (r1 == r5 ? 0 : -1))
            if (r1 > 0) goto L_0x0044
            long r1 = r9.getReadTimeout()
            java.util.concurrent.TimeUnit r7 = java.util.concurrent.TimeUnit.MILLISECONDS
            r0.readTimeout(r1, r7)
        L_0x0044:
            long r1 = r9.getWriteTimeout()
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r1 < 0) goto L_0x005d
            long r1 = r9.getWriteTimeout()
            int r1 = (r1 > r5 ? 1 : (r1 == r5 ? 0 : -1))
            if (r1 > 0) goto L_0x005d
            long r1 = r9.getWriteTimeout()
            java.util.concurrent.TimeUnit r3 = java.util.concurrent.TimeUnit.MILLISECONDS
            r0.writeTimeout(r1, r3)
        L_0x005d:
            okhttp3.Dns r1 = r9.getDns()
            if (r1 == 0) goto L_0x006a
            okhttp3.Dns r1 = r9.getDns()
            r0.dns(r1)
        L_0x006a:
            javax.net.SocketFactory r1 = r9.getSocketFactory()
            if (r1 == 0) goto L_0x0077
            javax.net.SocketFactory r1 = r9.getSocketFactory()
            r0.socketFactory(r1)
        L_0x0077:
            boolean r1 = r9.getFollowRedirects()
            r0.followRedirects(r1)
            boolean r1 = r9.getRetryOnConnectionFailure()
            r0.retryOnConnectionFailure(r1)
            javax.net.ssl.TrustManager[] r1 = com.sec.internal.helper.httpclient.HttpController.getTrustAllCertMangers()     // Catch:{ Exception -> 0x00f9 }
            r2 = 0
            r2 = r1[r2]     // Catch:{ Exception -> 0x00f9 }
            javax.net.ssl.X509TrustManager r2 = (javax.net.ssl.X509TrustManager) r2     // Catch:{ Exception -> 0x00f9 }
            java.lang.String r3 = r9.getUrl()     // Catch:{ Exception -> 0x00f9 }
            java.lang.String r4 = "https://wsg"
            boolean r3 = r3.startsWith(r4)     // Catch:{ Exception -> 0x00f9 }
            if (r3 != 0) goto L_0x00db
            boolean r3 = com.sec.internal.helper.httpclient.HttpController.mIsDebugHttps     // Catch:{ Exception -> 0x00f9 }
            if (r3 == 0) goto L_0x009f
            goto L_0x00db
        L_0x009f:
            boolean r3 = r9.getUseTls()     // Catch:{ Exception -> 0x00f9 }
            if (r3 == 0) goto L_0x00e2
            boolean r3 = r9.isReuseConnection()     // Catch:{ Exception -> 0x00f9 }
            if (r3 == 0) goto L_0x00c1
            javax.net.ssl.SSLSocketFactory r1 = com.sec.internal.helper.httpclient.HttpController.createSslSocketFactory(r1)     // Catch:{ Exception -> 0x00f9 }
            r0.sslSocketFactory(r1, r2)     // Catch:{ Exception -> 0x00f9 }
            java.util.List r1 = com.sec.internal.helper.httpclient.HttpController.getConnectionSpecs()     // Catch:{ Exception -> 0x00f9 }
            r0.connectionSpecs(r1)     // Catch:{ Exception -> 0x00f9 }
            javax.net.ssl.HostnameVerifier r1 = com.sec.internal.helper.httpclient.HttpController.getHostnameVerifier()     // Catch:{ Exception -> 0x00f9 }
            r0.hostnameVerifier(r1)     // Catch:{ Exception -> 0x00f9 }
            goto L_0x00e2
        L_0x00c1:
            javax.net.ssl.SSLSocketFactory r1 = com.sec.internal.helper.httpclient.HttpController.createSslSocketFactory(r1)     // Catch:{ Exception -> 0x00f9 }
            r0.sslSocketFactory(r1, r2)     // Catch:{ Exception -> 0x00f9 }
            okhttp3.ConnectionSpec r1 = com.sec.internal.helper.httpclient.HttpController.configConnectionSpec()     // Catch:{ Exception -> 0x00f9 }
            java.util.List r1 = java.util.Collections.singletonList(r1)     // Catch:{ Exception -> 0x00f9 }
            r0.connectionSpecs(r1)     // Catch:{ Exception -> 0x00f9 }
            javax.net.ssl.HostnameVerifier r1 = com.sec.internal.helper.httpclient.HttpController.createHostnameVerifier()     // Catch:{ Exception -> 0x00f9 }
            r0.hostnameVerifier(r1)     // Catch:{ Exception -> 0x00f9 }
            goto L_0x00e2
        L_0x00db:
            javax.net.ssl.SSLSocketFactory r1 = com.sec.internal.helper.httpclient.HttpController.createSslSocketFactory(r1)     // Catch:{ Exception -> 0x00f9 }
            r0.sslSocketFactory(r1, r2)     // Catch:{ Exception -> 0x00f9 }
        L_0x00e2:
            boolean r1 = r9.getUseProxy()
            if (r1 == 0) goto L_0x00ef
            java.net.Proxy r9 = r9.getProxy()
            r0.proxy(r9)
        L_0x00ef:
            com.sec.internal.ims.cmstore.PersistentHttp3CookieJar r8 = r8.mCookieJar
            r0.cookieJar(r8)
            okhttp3.OkHttpClient r8 = r0.build()
            return r8
        L_0x00f9:
            java.lang.String r8 = r8.TAG
            java.lang.String r9 = "Could not load keystore "
            com.sec.internal.log.IMSLog.d(r8, r9)
            okhttp3.OkHttpClient r8 = r0.build()
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.CmsHttpController.getOkHttpClient(com.sec.internal.helper.httpclient.HttpRequestParams):okhttp3.OkHttpClient");
    }
}
