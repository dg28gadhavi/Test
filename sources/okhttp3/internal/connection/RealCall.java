package okhttp3.internal.connection;

import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Address;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.Dispatcher;
import okhttp3.EventListener;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.internal.http.RealInterceptorChain;
import okhttp3.internal.platform.Platform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: RealCall.kt */
public final class RealCall implements Call {
    @Nullable
    private Object callStackTrace;
    private volatile boolean canceled;
    @NotNull
    private final OkHttpClient client;
    @Nullable
    private RealConnection connection;
    @NotNull
    private final RealConnectionPool connectionPool;
    @Nullable
    private volatile RealConnection connectionToCancel;
    @NotNull
    private final EventListener eventListener;
    @Nullable
    private volatile Exchange exchange;
    @Nullable
    private ExchangeFinder exchangeFinder;
    @NotNull
    private final AtomicBoolean executed = new AtomicBoolean();
    private boolean expectMoreExchanges = true;
    private final boolean forWebSocket;
    @Nullable
    private Exchange interceptorScopedExchange;
    @NotNull
    private final Request originalRequest;
    private boolean requestBodyOpen;
    private boolean responseBodyOpen;
    /* access modifiers changed from: private */
    @NotNull
    public final RealCall$timeout$1 timeout;
    private boolean timeoutEarlyExit;

    public RealCall(@NotNull OkHttpClient okHttpClient, @NotNull Request request, boolean z) {
        Intrinsics.checkNotNullParameter(okHttpClient, "client");
        Intrinsics.checkNotNullParameter(request, "originalRequest");
        this.client = okHttpClient;
        this.originalRequest = request;
        this.forWebSocket = z;
        this.connectionPool = okHttpClient.connectionPool().getDelegate$okhttp();
        this.eventListener = okHttpClient.eventListenerFactory().create(this);
        RealCall$timeout$1 realCall$timeout$1 = new RealCall$timeout$1(this);
        realCall$timeout$1.timeout((long) getClient().callTimeoutMillis(), TimeUnit.MILLISECONDS);
        this.timeout = realCall$timeout$1;
    }

    @NotNull
    public final OkHttpClient getClient() {
        return this.client;
    }

    @NotNull
    public final Request getOriginalRequest() {
        return this.originalRequest;
    }

    public final boolean getForWebSocket() {
        return this.forWebSocket;
    }

    @NotNull
    public final EventListener getEventListener$okhttp() {
        return this.eventListener;
    }

    @Nullable
    public final RealConnection getConnection() {
        return this.connection;
    }

    @Nullable
    public final Exchange getInterceptorScopedExchange$okhttp() {
        return this.interceptorScopedExchange;
    }

    public final void setConnectionToCancel(@Nullable RealConnection realConnection) {
        this.connectionToCancel = realConnection;
    }

    @NotNull
    public RealCall clone() {
        return new RealCall(this.client, this.originalRequest, this.forWebSocket);
    }

    @NotNull
    public Request request() {
        return this.originalRequest;
    }

    public void cancel() {
        if (!this.canceled) {
            this.canceled = true;
            Exchange exchange2 = this.exchange;
            if (exchange2 != null) {
                exchange2.cancel();
            }
            RealConnection realConnection = this.connectionToCancel;
            if (realConnection != null) {
                realConnection.cancel();
            }
            this.eventListener.canceled(this);
        }
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    @NotNull
    public Response execute() {
        if (this.executed.compareAndSet(false, true)) {
            this.timeout.enter();
            callStart();
            try {
                this.client.dispatcher().executed$okhttp(this);
                return getResponseWithInterceptorChain$okhttp();
            } finally {
                this.client.dispatcher().finished$okhttp(this);
            }
        } else {
            throw new IllegalStateException("Already Executed".toString());
        }
    }

    public void enqueue(@NotNull Callback callback) {
        Intrinsics.checkNotNullParameter(callback, "responseCallback");
        if (this.executed.compareAndSet(false, true)) {
            callStart();
            this.client.dispatcher().enqueue$okhttp(new AsyncCall(this, callback));
            return;
        }
        throw new IllegalStateException("Already Executed".toString());
    }

    private final void callStart() {
        this.callStackTrace = Platform.Companion.get().getStackTraceForCloseable("response.body().close()");
        this.eventListener.callStart(this);
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x00a2  */
    @org.jetbrains.annotations.NotNull
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final okhttp3.Response getResponseWithInterceptorChain$okhttp() throws java.io.IOException {
        /*
            r12 = this;
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            okhttp3.OkHttpClient r0 = r12.client
            java.util.List r0 = r0.interceptors()
            boolean unused = kotlin.collections.CollectionsKt__MutableCollectionsKt.addAll(r2, r0)
            okhttp3.internal.http.RetryAndFollowUpInterceptor r0 = new okhttp3.internal.http.RetryAndFollowUpInterceptor
            okhttp3.OkHttpClient r1 = r12.client
            r0.<init>(r1)
            r2.add(r0)
            okhttp3.internal.http.BridgeInterceptor r0 = new okhttp3.internal.http.BridgeInterceptor
            okhttp3.OkHttpClient r1 = r12.client
            okhttp3.CookieJar r1 = r1.cookieJar()
            r0.<init>(r1)
            r2.add(r0)
            okhttp3.internal.cache.CacheInterceptor r0 = new okhttp3.internal.cache.CacheInterceptor
            okhttp3.OkHttpClient r1 = r12.client
            r1.cache()
            r9 = 0
            r0.<init>(r9)
            r2.add(r0)
            okhttp3.internal.connection.ConnectInterceptor r0 = okhttp3.internal.connection.ConnectInterceptor.INSTANCE
            r2.add(r0)
            boolean r0 = r12.forWebSocket
            if (r0 != 0) goto L_0x0046
            okhttp3.OkHttpClient r0 = r12.client
            java.util.List r0 = r0.networkInterceptors()
            boolean unused = kotlin.collections.CollectionsKt__MutableCollectionsKt.addAll(r2, r0)
        L_0x0046:
            okhttp3.internal.http.CallServerInterceptor r0 = new okhttp3.internal.http.CallServerInterceptor
            boolean r1 = r12.forWebSocket
            r0.<init>(r1)
            r2.add(r0)
            okhttp3.internal.http.RealInterceptorChain r10 = new okhttp3.internal.http.RealInterceptorChain
            r3 = 0
            r4 = 0
            okhttp3.Request r5 = r12.originalRequest
            okhttp3.OkHttpClient r0 = r12.client
            int r6 = r0.connectTimeoutMillis()
            okhttp3.OkHttpClient r0 = r12.client
            int r7 = r0.readTimeoutMillis()
            okhttp3.OkHttpClient r0 = r12.client
            int r8 = r0.writeTimeoutMillis()
            r0 = r10
            r1 = r12
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8)
            r0 = 0
            okhttp3.Request r1 = r12.originalRequest     // Catch:{ IOException -> 0x008b, all -> 0x0089 }
            okhttp3.Response r1 = r10.proceed(r1)     // Catch:{ IOException -> 0x008b, all -> 0x0089 }
            boolean r2 = r12.isCanceled()     // Catch:{ IOException -> 0x008b, all -> 0x0089 }
            if (r2 != 0) goto L_0x007e
            r12.noMoreExchanges$okhttp(r9)
            return r1
        L_0x007e:
            okhttp3.internal.Util.closeQuietly((java.io.Closeable) r1)     // Catch:{ IOException -> 0x008b, all -> 0x0089 }
            java.io.IOException r1 = new java.io.IOException     // Catch:{ IOException -> 0x008b, all -> 0x0089 }
            java.lang.String r2 = "Canceled"
            r1.<init>(r2)     // Catch:{ IOException -> 0x008b, all -> 0x0089 }
            throw r1     // Catch:{ IOException -> 0x008b, all -> 0x0089 }
        L_0x0089:
            r1 = move-exception
            goto L_0x00a0
        L_0x008b:
            r0 = move-exception
            r1 = 1
            java.io.IOException r0 = r12.noMoreExchanges$okhttp(r0)     // Catch:{ all -> 0x009c }
            if (r0 != 0) goto L_0x009b
            java.lang.NullPointerException r0 = new java.lang.NullPointerException     // Catch:{ all -> 0x009c }
            java.lang.String r2 = "null cannot be cast to non-null type kotlin.Throwable"
            r0.<init>(r2)     // Catch:{ all -> 0x009c }
            throw r0     // Catch:{ all -> 0x009c }
        L_0x009b:
            throw r0     // Catch:{ all -> 0x009c }
        L_0x009c:
            r0 = move-exception
            r11 = r1
            r1 = r0
            r0 = r11
        L_0x00a0:
            if (r0 != 0) goto L_0x00a5
            r12.noMoreExchanges$okhttp(r9)
        L_0x00a5:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp():okhttp3.Response");
    }

    public final void enterNetworkInterceptorExchange(@NotNull Request request, boolean z) {
        Intrinsics.checkNotNullParameter(request, "request");
        if (this.interceptorScopedExchange == null) {
            synchronized (this) {
                if (!(!this.responseBodyOpen)) {
                    throw new IllegalStateException("cannot make a new request because the previous response is still open: please call response.close()".toString());
                } else if (!this.requestBodyOpen) {
                    Unit unit = Unit.INSTANCE;
                } else {
                    throw new IllegalStateException("Check failed.".toString());
                }
            }
            if (z) {
                this.exchangeFinder = new ExchangeFinder(this.connectionPool, createAddress(request.url()), this, this.eventListener);
                return;
            }
            return;
        }
        throw new IllegalStateException("Check failed.".toString());
    }

    @NotNull
    public final Exchange initExchange$okhttp(@NotNull RealInterceptorChain realInterceptorChain) {
        Intrinsics.checkNotNullParameter(realInterceptorChain, "chain");
        synchronized (this) {
            if (!this.expectMoreExchanges) {
                throw new IllegalStateException("released".toString());
            } else if (!(!this.responseBodyOpen)) {
                throw new IllegalStateException("Check failed.".toString());
            } else if (!this.requestBodyOpen) {
                Unit unit = Unit.INSTANCE;
            } else {
                throw new IllegalStateException("Check failed.".toString());
            }
        }
        ExchangeFinder exchangeFinder2 = this.exchangeFinder;
        Intrinsics.checkNotNull(exchangeFinder2);
        Exchange exchange2 = new Exchange(this, this.eventListener, exchangeFinder2, exchangeFinder2.find(this.client, realInterceptorChain));
        this.interceptorScopedExchange = exchange2;
        this.exchange = exchange2;
        synchronized (this) {
            this.requestBodyOpen = true;
            this.responseBodyOpen = true;
        }
        if (!this.canceled) {
            return exchange2;
        }
        throw new IOException("Canceled");
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0021 A[Catch:{ all -> 0x0017 }] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0025 A[Catch:{ all -> 0x0017 }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0030 A[Catch:{ all -> 0x0017 }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0032 A[Catch:{ all -> 0x0017 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final <E extends java.io.IOException> E messageDone$okhttp(@org.jetbrains.annotations.NotNull okhttp3.internal.connection.Exchange r2, boolean r3, boolean r4, E r5) {
        /*
            r1 = this;
            java.lang.String r0 = "exchange"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r2, r0)
            okhttp3.internal.connection.Exchange r0 = r1.exchange
            boolean r2 = kotlin.jvm.internal.Intrinsics.areEqual(r2, r0)
            if (r2 != 0) goto L_0x000e
            return r5
        L_0x000e:
            monitor-enter(r1)
            r2 = 0
            if (r3 == 0) goto L_0x0019
            boolean r0 = r1.requestBodyOpen     // Catch:{ all -> 0x0017 }
            if (r0 != 0) goto L_0x001f
            goto L_0x0019
        L_0x0017:
            r2 = move-exception
            goto L_0x005a
        L_0x0019:
            if (r4 == 0) goto L_0x0041
            boolean r0 = r1.responseBodyOpen     // Catch:{ all -> 0x0017 }
            if (r0 == 0) goto L_0x0041
        L_0x001f:
            if (r3 == 0) goto L_0x0023
            r1.requestBodyOpen = r2     // Catch:{ all -> 0x0017 }
        L_0x0023:
            if (r4 == 0) goto L_0x0027
            r1.responseBodyOpen = r2     // Catch:{ all -> 0x0017 }
        L_0x0027:
            boolean r3 = r1.requestBodyOpen     // Catch:{ all -> 0x0017 }
            r4 = 1
            if (r3 != 0) goto L_0x0032
            boolean r0 = r1.responseBodyOpen     // Catch:{ all -> 0x0017 }
            if (r0 != 0) goto L_0x0032
            r0 = r4
            goto L_0x0033
        L_0x0032:
            r0 = r2
        L_0x0033:
            if (r3 != 0) goto L_0x003e
            boolean r3 = r1.responseBodyOpen     // Catch:{ all -> 0x0017 }
            if (r3 != 0) goto L_0x003e
            boolean r3 = r1.expectMoreExchanges     // Catch:{ all -> 0x0017 }
            if (r3 != 0) goto L_0x003e
            r2 = r4
        L_0x003e:
            r3 = r2
            r2 = r0
            goto L_0x0042
        L_0x0041:
            r3 = r2
        L_0x0042:
            kotlin.Unit r4 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x0017 }
            monitor-exit(r1)
            if (r2 == 0) goto L_0x0052
            r2 = 0
            r1.exchange = r2
            okhttp3.internal.connection.RealConnection r2 = r1.connection
            if (r2 != 0) goto L_0x004f
            goto L_0x0052
        L_0x004f:
            r2.incrementSuccessCount$okhttp()
        L_0x0052:
            if (r3 == 0) goto L_0x0059
            java.io.IOException r1 = r1.callDone(r5)
            return r1
        L_0x0059:
            return r5
        L_0x005a:
            monitor-exit(r1)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.connection.RealCall.messageDone$okhttp(okhttp3.internal.connection.Exchange, boolean, boolean, java.io.IOException):java.io.IOException");
    }

    @Nullable
    public final IOException noMoreExchanges$okhttp(@Nullable IOException iOException) {
        boolean z;
        synchronized (this) {
            z = false;
            if (this.expectMoreExchanges) {
                this.expectMoreExchanges = false;
                if (!this.requestBodyOpen && !this.responseBodyOpen) {
                    z = true;
                }
            }
            Unit unit = Unit.INSTANCE;
        }
        if (z) {
            return callDone(iOException);
        }
        return iOException;
    }

    @Nullable
    public final Socket releaseConnectionNoEvents$okhttp() {
        RealConnection realConnection = this.connection;
        Intrinsics.checkNotNull(realConnection);
        if (!Util.assertionsEnabled || Thread.holdsLock(realConnection)) {
            List<Reference<RealCall>> calls = realConnection.getCalls();
            Iterator<Reference<RealCall>> it = calls.iterator();
            boolean z = false;
            int i = 0;
            while (true) {
                if (!it.hasNext()) {
                    i = -1;
                    break;
                } else if (Intrinsics.areEqual(it.next().get(), this)) {
                    break;
                } else {
                    i++;
                }
            }
            if (i != -1) {
                z = true;
            }
            if (z) {
                calls.remove(i);
                this.connection = null;
                if (calls.isEmpty()) {
                    realConnection.setIdleAtNs$okhttp(System.nanoTime());
                    if (this.connectionPool.connectionBecameIdle(realConnection)) {
                        return realConnection.socket();
                    }
                }
                return null;
            }
            throw new IllegalStateException("Check failed.".toString());
        }
        throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST hold lock on " + realConnection);
    }

    private final <E extends IOException> E timeoutExit(E e) {
        if (this.timeoutEarlyExit || !this.timeout.exit()) {
            return e;
        }
        E interruptedIOException = new InterruptedIOException(EucTestIntent.Extras.TIMEOUT);
        if (e != null) {
            interruptedIOException.initCause(e);
        }
        return interruptedIOException;
    }

    public final void timeoutEarlyExit() {
        if (!this.timeoutEarlyExit) {
            this.timeoutEarlyExit = true;
            this.timeout.exit();
            return;
        }
        throw new IllegalStateException("Check failed.".toString());
    }

    public final void exitNetworkInterceptorExchange$okhttp(boolean z) {
        Exchange exchange2;
        synchronized (this) {
            if (this.expectMoreExchanges) {
                Unit unit = Unit.INSTANCE;
            } else {
                throw new IllegalStateException("released".toString());
            }
        }
        if (z && (exchange2 = this.exchange) != null) {
            exchange2.detachWithViolence();
        }
        this.interceptorScopedExchange = null;
    }

    private final Address createAddress(HttpUrl httpUrl) {
        CertificatePinner certificatePinner;
        HostnameVerifier hostnameVerifier;
        SSLSocketFactory sSLSocketFactory;
        if (httpUrl.isHttps()) {
            sSLSocketFactory = this.client.sslSocketFactory();
            hostnameVerifier = this.client.hostnameVerifier();
            certificatePinner = this.client.certificatePinner();
        } else {
            sSLSocketFactory = null;
            hostnameVerifier = null;
            certificatePinner = null;
        }
        return new Address(httpUrl.host(), httpUrl.port(), this.client.dns(), this.client.socketFactory(), sSLSocketFactory, hostnameVerifier, certificatePinner, this.client.proxyAuthenticator(), this.client.proxy(), this.client.protocols(), this.client.connectionSpecs(), this.client.proxySelector());
    }

    public final boolean retryAfterFailure() {
        ExchangeFinder exchangeFinder2 = this.exchangeFinder;
        Intrinsics.checkNotNull(exchangeFinder2);
        return exchangeFinder2.retryAfterFailure();
    }

    /* access modifiers changed from: private */
    public final String toLoggableString() {
        StringBuilder sb = new StringBuilder();
        sb.append(isCanceled() ? "canceled " : "");
        sb.append(this.forWebSocket ? "web socket" : "call");
        sb.append(" to ");
        sb.append(redactedUrl$okhttp());
        return sb.toString();
    }

    @NotNull
    public final String redactedUrl$okhttp() {
        return this.originalRequest.url().redact();
    }

    /* compiled from: RealCall.kt */
    public final class AsyncCall implements Runnable {
        @NotNull
        private volatile AtomicInteger callsPerHost = new AtomicInteger(0);
        @NotNull
        private final Callback responseCallback;
        final /* synthetic */ RealCall this$0;

        public AsyncCall(@NotNull RealCall realCall, Callback callback) {
            Intrinsics.checkNotNullParameter(realCall, "this$0");
            Intrinsics.checkNotNullParameter(callback, "responseCallback");
            this.this$0 = realCall;
            this.responseCallback = callback;
        }

        @NotNull
        public final AtomicInteger getCallsPerHost() {
            return this.callsPerHost;
        }

        public final void reuseCallsPerHostFrom(@NotNull AsyncCall asyncCall) {
            Intrinsics.checkNotNullParameter(asyncCall, "other");
            this.callsPerHost = asyncCall.callsPerHost;
        }

        @NotNull
        public final String getHost() {
            return this.this$0.getOriginalRequest().url().host();
        }

        @NotNull
        public final RealCall getCall() {
            return this.this$0;
        }

        public final void executeOn(@NotNull ExecutorService executorService) {
            Intrinsics.checkNotNullParameter(executorService, "executorService");
            Dispatcher dispatcher = this.this$0.getClient().dispatcher();
            if (!Util.assertionsEnabled || !Thread.holdsLock(dispatcher)) {
                try {
                    executorService.execute(this);
                } catch (RejectedExecutionException e) {
                    InterruptedIOException interruptedIOException = new InterruptedIOException("executor rejected");
                    interruptedIOException.initCause(e);
                    this.this$0.noMoreExchanges$okhttp(interruptedIOException);
                    this.responseCallback.onFailure(this.this$0, interruptedIOException);
                    this.this$0.getClient().dispatcher().finished$okhttp(this);
                } catch (Throwable th) {
                    this.this$0.getClient().dispatcher().finished$okhttp(this);
                    throw th;
                }
            } else {
                throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST NOT hold lock on " + dispatcher);
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:19:0x0044 A[Catch:{ all -> 0x0058, all -> 0x0093 }] */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x0060 A[Catch:{ all -> 0x0058, all -> 0x0093 }] */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x0075 A[Catch:{ all -> 0x0058, all -> 0x0093 }] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r8 = this;
                okhttp3.internal.connection.RealCall r0 = r8.this$0
                java.lang.String r0 = r0.redactedUrl$okhttp()
                java.lang.String r1 = "OkHttp "
                java.lang.String r0 = kotlin.jvm.internal.Intrinsics.stringPlus(r1, r0)
                okhttp3.internal.connection.RealCall r1 = r8.this$0
                java.lang.Thread r2 = java.lang.Thread.currentThread()
                java.lang.String r3 = r2.getName()
                r2.setName(r0)
                okhttp3.internal.connection.RealCall$timeout$1 r0 = r1.timeout     // Catch:{ all -> 0x0093 }
                r0.enter()     // Catch:{ all -> 0x0093 }
                r0 = 0
                okhttp3.Response r0 = r1.getResponseWithInterceptorChain$okhttp()     // Catch:{ IOException -> 0x005a, all -> 0x003b }
                r4 = 1
                okhttp3.Callback r5 = r8.responseCallback     // Catch:{ IOException -> 0x0039, all -> 0x0037 }
                r5.onResponse(r1, r0)     // Catch:{ IOException -> 0x0039, all -> 0x0037 }
                okhttp3.OkHttpClient r0 = r1.getClient()     // Catch:{ all -> 0x0093 }
                okhttp3.Dispatcher r0 = r0.dispatcher()     // Catch:{ all -> 0x0093 }
            L_0x0033:
                r0.finished$okhttp((okhttp3.internal.connection.RealCall.AsyncCall) r8)     // Catch:{ all -> 0x0093 }
                goto L_0x0083
            L_0x0037:
                r0 = move-exception
                goto L_0x003f
            L_0x0039:
                r0 = move-exception
                goto L_0x005e
            L_0x003b:
                r4 = move-exception
                r7 = r4
                r4 = r0
                r0 = r7
            L_0x003f:
                r1.cancel()     // Catch:{ all -> 0x0058 }
                if (r4 != 0) goto L_0x0057
                java.io.IOException r4 = new java.io.IOException     // Catch:{ all -> 0x0058 }
                java.lang.String r5 = "canceled due to "
                java.lang.String r5 = kotlin.jvm.internal.Intrinsics.stringPlus(r5, r0)     // Catch:{ all -> 0x0058 }
                r4.<init>(r5)     // Catch:{ all -> 0x0058 }
                kotlin.ExceptionsKt__ExceptionsKt.addSuppressed(r4, r0)     // Catch:{ all -> 0x0058 }
                okhttp3.Callback r5 = r8.responseCallback     // Catch:{ all -> 0x0058 }
                r5.onFailure(r1, r4)     // Catch:{ all -> 0x0058 }
            L_0x0057:
                throw r0     // Catch:{ all -> 0x0058 }
            L_0x0058:
                r0 = move-exception
                goto L_0x0087
            L_0x005a:
                r4 = move-exception
                r7 = r4
                r4 = r0
                r0 = r7
            L_0x005e:
                if (r4 == 0) goto L_0x0075
                okhttp3.internal.platform.Platform$Companion r4 = okhttp3.internal.platform.Platform.Companion     // Catch:{ all -> 0x0058 }
                okhttp3.internal.platform.Platform r4 = r4.get()     // Catch:{ all -> 0x0058 }
                java.lang.String r5 = "Callback failure for "
                java.lang.String r6 = r1.toLoggableString()     // Catch:{ all -> 0x0058 }
                java.lang.String r5 = kotlin.jvm.internal.Intrinsics.stringPlus(r5, r6)     // Catch:{ all -> 0x0058 }
                r6 = 4
                r4.log(r5, r6, r0)     // Catch:{ all -> 0x0058 }
                goto L_0x007a
            L_0x0075:
                okhttp3.Callback r4 = r8.responseCallback     // Catch:{ all -> 0x0058 }
                r4.onFailure(r1, r0)     // Catch:{ all -> 0x0058 }
            L_0x007a:
                okhttp3.OkHttpClient r0 = r1.getClient()     // Catch:{ all -> 0x0093 }
                okhttp3.Dispatcher r0 = r0.dispatcher()     // Catch:{ all -> 0x0093 }
                goto L_0x0033
            L_0x0083:
                r2.setName(r3)
                return
            L_0x0087:
                okhttp3.OkHttpClient r1 = r1.getClient()     // Catch:{ all -> 0x0093 }
                okhttp3.Dispatcher r1 = r1.dispatcher()     // Catch:{ all -> 0x0093 }
                r1.finished$okhttp((okhttp3.internal.connection.RealCall.AsyncCall) r8)     // Catch:{ all -> 0x0093 }
                throw r0     // Catch:{ all -> 0x0093 }
            L_0x0093:
                r8 = move-exception
                r2.setName(r3)
                throw r8
            */
            throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.connection.RealCall.AsyncCall.run():void");
        }
    }

    /* compiled from: RealCall.kt */
    public static final class CallReference extends WeakReference<RealCall> {
        @Nullable
        private final Object callStackTrace;

        @Nullable
        public final Object getCallStackTrace() {
            return this.callStackTrace;
        }

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public CallReference(@NotNull RealCall realCall, @Nullable Object obj) {
            super(realCall);
            Intrinsics.checkNotNullParameter(realCall, "referent");
            this.callStackTrace = obj;
        }
    }

    public final void acquireConnectionNoEvents(@NotNull RealConnection realConnection) {
        Intrinsics.checkNotNullParameter(realConnection, "connection");
        if (!Util.assertionsEnabled || Thread.holdsLock(realConnection)) {
            if (this.connection == null) {
                this.connection = realConnection;
                realConnection.getCalls().add(new CallReference(this, this.callStackTrace));
                return;
            }
            throw new IllegalStateException("Check failed.".toString());
        }
        throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST hold lock on " + realConnection);
    }

    private final <E extends IOException> E callDone(E e) {
        Socket releaseConnectionNoEvents$okhttp;
        boolean z = Util.assertionsEnabled;
        if (!z || !Thread.holdsLock(this)) {
            RealConnection realConnection = this.connection;
            if (realConnection != null) {
                if (!z || !Thread.holdsLock(realConnection)) {
                    synchronized (realConnection) {
                        releaseConnectionNoEvents$okhttp = releaseConnectionNoEvents$okhttp();
                    }
                    if (this.connection == null) {
                        if (releaseConnectionNoEvents$okhttp != null) {
                            Util.closeQuietly(releaseConnectionNoEvents$okhttp);
                        }
                        this.eventListener.connectionReleased(this, realConnection);
                    } else {
                        if (!(releaseConnectionNoEvents$okhttp == null)) {
                            throw new IllegalStateException("Check failed.".toString());
                        }
                    }
                } else {
                    throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST NOT hold lock on " + realConnection);
                }
            }
            E timeoutExit = timeoutExit(e);
            if (e != null) {
                EventListener eventListener2 = this.eventListener;
                Intrinsics.checkNotNull(timeoutExit);
                eventListener2.callFailed(this, timeoutExit);
            } else {
                this.eventListener.callEnd(this);
            }
            return timeoutExit;
        }
        throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST NOT hold lock on " + this);
    }
}
