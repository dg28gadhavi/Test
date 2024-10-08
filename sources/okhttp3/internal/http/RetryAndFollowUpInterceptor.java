package okhttp3.internal.http;

import com.sec.internal.helper.HttpRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Regex;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.internal.connection.RealCall;
import org.jetbrains.annotations.NotNull;

/* compiled from: RetryAndFollowUpInterceptor.kt */
public final class RetryAndFollowUpInterceptor implements Interceptor {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    @NotNull
    private final OkHttpClient client;

    public RetryAndFollowUpInterceptor(@NotNull OkHttpClient okHttpClient) {
        Intrinsics.checkNotNullParameter(okHttpClient, "client");
        this.client = okHttpClient;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
        r0 = r0.newBuilder().priorResponse(r7.newBuilder().body((okhttp3.ResponseBody) null).build()).build();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0040, code lost:
        r7 = r0;
        r0 = r1.getInterceptorScopedExchange$okhttp();
        r6 = followUpRequest(r7, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0049, code lost:
        if (r6 != null) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x004b, code lost:
        if (r0 == null) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0051, code lost:
        if (r0.isDuplex$okhttp() == false) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0053, code lost:
        r1.timeoutEarlyExit();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0056, code lost:
        r1.exitNetworkInterceptorExchange$okhttp(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0059, code lost:
        return r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r0 = r6.body();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005e, code lost:
        if (r0 == null) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0064, code lost:
        if (r0.isOneShot() == false) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0066, code lost:
        r1.exitNetworkInterceptorExchange$okhttp(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0069, code lost:
        return r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r0 = r7.body();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006e, code lost:
        if (r0 != null) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0071, code lost:
        okhttp3.internal.Util.closeQuietly((java.io.Closeable) r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0074, code lost:
        r8 = r8 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0078, code lost:
        if (r8 > 20) goto L_0x007f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x008e, code lost:
        throw new java.net.ProtocolException(kotlin.jvm.internal.Intrinsics.stringPlus("Too many follow-up requests: ", java.lang.Integer.valueOf(r8)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0026, code lost:
        if (r7 == null) goto L_0x0040;
     */
    @org.jetbrains.annotations.NotNull
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public okhttp3.Response intercept(@org.jetbrains.annotations.NotNull okhttp3.Interceptor.Chain r11) throws java.io.IOException {
        /*
            r10 = this;
            java.lang.String r0 = "chain"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r11, r0)
            okhttp3.internal.http.RealInterceptorChain r11 = (okhttp3.internal.http.RealInterceptorChain) r11
            okhttp3.Request r0 = r11.getRequest$okhttp()
            okhttp3.internal.connection.RealCall r1 = r11.getCall$okhttp()
            java.util.List r2 = kotlin.collections.CollectionsKt__CollectionsKt.emptyList()
            r3 = 0
            r4 = 0
            r5 = 1
            r8 = r3
            r7 = r4
        L_0x0018:
            r6 = r5
        L_0x0019:
            r1.enterNetworkInterceptorExchange(r0, r6)
            boolean r6 = r1.isCanceled()     // Catch:{ all -> 0x00d1 }
            if (r6 != 0) goto L_0x00c9
            okhttp3.Response r0 = r11.proceed(r0)     // Catch:{ RouteException -> 0x00a7, IOException -> 0x008f }
            if (r7 == 0) goto L_0x0040
            okhttp3.Response$Builder r0 = r0.newBuilder()     // Catch:{ all -> 0x00d1 }
            okhttp3.Response$Builder r6 = r7.newBuilder()     // Catch:{ all -> 0x00d1 }
            okhttp3.Response$Builder r6 = r6.body(r4)     // Catch:{ all -> 0x00d1 }
            okhttp3.Response r6 = r6.build()     // Catch:{ all -> 0x00d1 }
            okhttp3.Response$Builder r0 = r0.priorResponse(r6)     // Catch:{ all -> 0x00d1 }
            okhttp3.Response r0 = r0.build()     // Catch:{ all -> 0x00d1 }
        L_0x0040:
            r7 = r0
            okhttp3.internal.connection.Exchange r0 = r1.getInterceptorScopedExchange$okhttp()     // Catch:{ all -> 0x00d1 }
            okhttp3.Request r6 = r10.followUpRequest(r7, r0)     // Catch:{ all -> 0x00d1 }
            if (r6 != 0) goto L_0x005a
            if (r0 == 0) goto L_0x0056
            boolean r10 = r0.isDuplex$okhttp()     // Catch:{ all -> 0x00d1 }
            if (r10 == 0) goto L_0x0056
            r1.timeoutEarlyExit()     // Catch:{ all -> 0x00d1 }
        L_0x0056:
            r1.exitNetworkInterceptorExchange$okhttp(r3)
            return r7
        L_0x005a:
            okhttp3.RequestBody r0 = r6.body()     // Catch:{ all -> 0x00d1 }
            if (r0 == 0) goto L_0x006a
            boolean r0 = r0.isOneShot()     // Catch:{ all -> 0x00d1 }
            if (r0 == 0) goto L_0x006a
            r1.exitNetworkInterceptorExchange$okhttp(r3)
            return r7
        L_0x006a:
            okhttp3.ResponseBody r0 = r7.body()     // Catch:{ all -> 0x00d1 }
            if (r0 != 0) goto L_0x0071
            goto L_0x0074
        L_0x0071:
            okhttp3.internal.Util.closeQuietly((java.io.Closeable) r0)     // Catch:{ all -> 0x00d1 }
        L_0x0074:
            int r8 = r8 + 1
            r0 = 20
            if (r8 > r0) goto L_0x007f
            r1.exitNetworkInterceptorExchange$okhttp(r5)
            r0 = r6
            goto L_0x0018
        L_0x007f:
            java.net.ProtocolException r10 = new java.net.ProtocolException     // Catch:{ all -> 0x00d1 }
            java.lang.String r11 = "Too many follow-up requests: "
            java.lang.Integer r0 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x00d1 }
            java.lang.String r11 = kotlin.jvm.internal.Intrinsics.stringPlus(r11, r0)     // Catch:{ all -> 0x00d1 }
            r10.<init>(r11)     // Catch:{ all -> 0x00d1 }
            throw r10     // Catch:{ all -> 0x00d1 }
        L_0x008f:
            r6 = move-exception
            boolean r9 = r6 instanceof okhttp3.internal.http2.ConnectionShutdownException     // Catch:{ all -> 0x00d1 }
            if (r9 != 0) goto L_0x0096
            r9 = r5
            goto L_0x0097
        L_0x0096:
            r9 = r3
        L_0x0097:
            boolean r9 = r10.recover(r6, r1, r0, r9)     // Catch:{ all -> 0x00d1 }
            if (r9 == 0) goto L_0x00a2
            java.util.List r2 = kotlin.collections.CollectionsKt___CollectionsKt.plus(r2, r6)     // Catch:{ all -> 0x00d1 }
            goto L_0x00ba
        L_0x00a2:
            java.lang.Throwable r10 = okhttp3.internal.Util.withSuppressed(r6, r2)     // Catch:{ all -> 0x00d1 }
            throw r10     // Catch:{ all -> 0x00d1 }
        L_0x00a7:
            r6 = move-exception
            java.io.IOException r9 = r6.getLastConnectException()     // Catch:{ all -> 0x00d1 }
            boolean r9 = r10.recover(r9, r1, r0, r3)     // Catch:{ all -> 0x00d1 }
            if (r9 == 0) goto L_0x00c0
            java.io.IOException r6 = r6.getFirstConnectException()     // Catch:{ all -> 0x00d1 }
            java.util.List r2 = kotlin.collections.CollectionsKt___CollectionsKt.plus(r2, r6)     // Catch:{ all -> 0x00d1 }
        L_0x00ba:
            r1.exitNetworkInterceptorExchange$okhttp(r5)
            r6 = r3
            goto L_0x0019
        L_0x00c0:
            java.io.IOException r10 = r6.getFirstConnectException()     // Catch:{ all -> 0x00d1 }
            java.lang.Throwable r10 = okhttp3.internal.Util.withSuppressed(r10, r2)     // Catch:{ all -> 0x00d1 }
            throw r10     // Catch:{ all -> 0x00d1 }
        L_0x00c9:
            java.io.IOException r10 = new java.io.IOException     // Catch:{ all -> 0x00d1 }
            java.lang.String r11 = "Canceled"
            r10.<init>(r11)     // Catch:{ all -> 0x00d1 }
            throw r10     // Catch:{ all -> 0x00d1 }
        L_0x00d1:
            r10 = move-exception
            r1.exitNetworkInterceptorExchange$okhttp(r5)
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept(okhttp3.Interceptor$Chain):okhttp3.Response");
    }

    private final boolean recover(IOException iOException, RealCall realCall, Request request, boolean z) {
        if (!this.client.retryOnConnectionFailure()) {
            return false;
        }
        if ((!z || !requestIsOneShot(iOException, request)) && isRecoverable(iOException, z) && realCall.retryAfterFailure()) {
            return true;
        }
        return false;
    }

    private final boolean requestIsOneShot(IOException iOException, Request request) {
        RequestBody body = request.body();
        return (body != null && body.isOneShot()) || (iOException instanceof FileNotFoundException);
    }

    private final boolean isRecoverable(IOException iOException, boolean z) {
        if (iOException instanceof ProtocolException) {
            return false;
        }
        if (iOException instanceof InterruptedIOException) {
            if (!(iOException instanceof SocketTimeoutException) || z) {
                return false;
            }
            return true;
        } else if ((!(iOException instanceof SSLHandshakeException) || !(iOException.getCause() instanceof CertificateException)) && !(iOException instanceof SSLPeerUnverifiedException)) {
            return true;
        } else {
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0005, code lost:
        r1 = r7.getConnection$okhttp();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final okhttp3.Request followUpRequest(okhttp3.Response r6, okhttp3.internal.connection.Exchange r7) throws java.io.IOException {
        /*
            r5 = this;
            r0 = 0
            if (r7 != 0) goto L_0x0005
        L_0x0003:
            r1 = r0
            goto L_0x0010
        L_0x0005:
            okhttp3.internal.connection.RealConnection r1 = r7.getConnection$okhttp()
            if (r1 != 0) goto L_0x000c
            goto L_0x0003
        L_0x000c:
            okhttp3.Route r1 = r1.route()
        L_0x0010:
            int r2 = r6.code()
            okhttp3.Request r3 = r6.request()
            java.lang.String r3 = r3.method()
            r4 = 307(0x133, float:4.3E-43)
            if (r2 == r4) goto L_0x00e0
            r4 = 308(0x134, float:4.32E-43)
            if (r2 == r4) goto L_0x00e0
            r4 = 401(0x191, float:5.62E-43)
            if (r2 == r4) goto L_0x00d5
            r4 = 421(0x1a5, float:5.9E-43)
            if (r2 == r4) goto L_0x00ae
            r7 = 503(0x1f7, float:7.05E-43)
            if (r2 == r7) goto L_0x0092
            r7 = 407(0x197, float:5.7E-43)
            if (r2 == r7) goto L_0x0070
            r7 = 408(0x198, float:5.72E-43)
            if (r2 == r7) goto L_0x003c
            switch(r2) {
                case 300: goto L_0x00e0;
                case 301: goto L_0x00e0;
                case 302: goto L_0x00e0;
                case 303: goto L_0x00e0;
                default: goto L_0x003b;
            }
        L_0x003b:
            return r0
        L_0x003c:
            okhttp3.OkHttpClient r1 = r5.client
            boolean r1 = r1.retryOnConnectionFailure()
            if (r1 != 0) goto L_0x0045
            return r0
        L_0x0045:
            okhttp3.Request r1 = r6.request()
            okhttp3.RequestBody r1 = r1.body()
            if (r1 == 0) goto L_0x0056
            boolean r1 = r1.isOneShot()
            if (r1 == 0) goto L_0x0056
            return r0
        L_0x0056:
            okhttp3.Response r1 = r6.priorResponse()
            if (r1 == 0) goto L_0x0063
            int r1 = r1.code()
            if (r1 != r7) goto L_0x0063
            return r0
        L_0x0063:
            r7 = 0
            int r5 = r5.retryAfter(r6, r7)
            if (r5 <= 0) goto L_0x006b
            return r0
        L_0x006b:
            okhttp3.Request r5 = r6.request()
            return r5
        L_0x0070:
            kotlin.jvm.internal.Intrinsics.checkNotNull(r1)
            java.net.Proxy r7 = r1.proxy()
            java.net.Proxy$Type r7 = r7.type()
            java.net.Proxy$Type r0 = java.net.Proxy.Type.HTTP
            if (r7 != r0) goto L_0x008a
            okhttp3.OkHttpClient r5 = r5.client
            okhttp3.Authenticator r5 = r5.proxyAuthenticator()
            okhttp3.Request r5 = r5.authenticate(r1, r6)
            return r5
        L_0x008a:
            java.net.ProtocolException r5 = new java.net.ProtocolException
            java.lang.String r6 = "Received HTTP_PROXY_AUTH (407) code while not using proxy"
            r5.<init>(r6)
            throw r5
        L_0x0092:
            okhttp3.Response r1 = r6.priorResponse()
            if (r1 == 0) goto L_0x009f
            int r1 = r1.code()
            if (r1 != r7) goto L_0x009f
            return r0
        L_0x009f:
            r7 = 2147483647(0x7fffffff, float:NaN)
            int r5 = r5.retryAfter(r6, r7)
            if (r5 != 0) goto L_0x00ad
            okhttp3.Request r5 = r6.request()
            return r5
        L_0x00ad:
            return r0
        L_0x00ae:
            okhttp3.Request r5 = r6.request()
            okhttp3.RequestBody r5 = r5.body()
            if (r5 == 0) goto L_0x00bf
            boolean r5 = r5.isOneShot()
            if (r5 == 0) goto L_0x00bf
            return r0
        L_0x00bf:
            if (r7 == 0) goto L_0x00d4
            boolean r5 = r7.isCoalescedConnection$okhttp()
            if (r5 != 0) goto L_0x00c8
            goto L_0x00d4
        L_0x00c8:
            okhttp3.internal.connection.RealConnection r5 = r7.getConnection$okhttp()
            r5.noCoalescedConnections$okhttp()
            okhttp3.Request r5 = r6.request()
            return r5
        L_0x00d4:
            return r0
        L_0x00d5:
            okhttp3.OkHttpClient r5 = r5.client
            okhttp3.Authenticator r5 = r5.authenticator()
            okhttp3.Request r5 = r5.authenticate(r1, r6)
            return r5
        L_0x00e0:
            okhttp3.Request r5 = r5.buildRedirectRequest(r6, r3)
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.http.RetryAndFollowUpInterceptor.followUpRequest(okhttp3.Response, okhttp3.internal.connection.Exchange):okhttp3.Request");
    }

    private final Request buildRedirectRequest(Response response, String str) {
        String header$default;
        HttpUrl resolve;
        RequestBody requestBody = null;
        if (!this.client.followRedirects() || (header$default = Response.header$default(response, "Location", (String) null, 2, (Object) null)) == null || (resolve = response.request().url().resolve(header$default)) == null) {
            return null;
        }
        if (!Intrinsics.areEqual(resolve.scheme(), response.request().url().scheme()) && !this.client.followSslRedirects()) {
            return null;
        }
        Request.Builder newBuilder = response.request().newBuilder();
        if (HttpMethod.permitsRequestBody(str)) {
            int code = response.code();
            HttpMethod httpMethod = HttpMethod.INSTANCE;
            boolean z = httpMethod.redirectsWithBody(str) || code == 308 || code == 307;
            if (!httpMethod.redirectsToGet(str) || code == 308 || code == 307) {
                if (z) {
                    requestBody = response.request().body();
                }
                newBuilder.method(str, requestBody);
            } else {
                newBuilder.method("GET", (RequestBody) null);
            }
            if (!z) {
                newBuilder.removeHeader(HttpRequest.HEADER_TRANSFER_ENCODING);
                newBuilder.removeHeader("Content-Length");
                newBuilder.removeHeader("Content-Type");
            }
        }
        if (!Util.canReuseConnectionFor(response.request().url(), resolve)) {
            newBuilder.removeHeader("Authorization");
        }
        return newBuilder.url(resolve).build();
    }

    private final int retryAfter(Response response, int i) {
        String header$default = Response.header$default(response, HttpRequest.HEADER_RETRY_AFTER, (String) null, 2, (Object) null);
        if (header$default == null) {
            return i;
        }
        if (!new Regex("\\d+").matches(header$default)) {
            return Integer.MAX_VALUE;
        }
        Integer valueOf = Integer.valueOf(header$default);
        Intrinsics.checkNotNullExpressionValue(valueOf, "valueOf(header)");
        return valueOf.intValue();
    }

    /* compiled from: RetryAndFollowUpInterceptor.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }
}
