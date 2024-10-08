package okhttp3;

import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.EventListener;
import okhttp3.internal.Util;
import okhttp3.internal.connection.RealCall;
import okhttp3.internal.connection.RouteDatabase;
import okhttp3.internal.platform.Platform;
import okhttp3.internal.proxy.NullProxySelector;
import okhttp3.internal.tls.CertificateChainCleaner;
import okhttp3.internal.tls.OkHostnameVerifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: OkHttpClient.kt */
public class OkHttpClient implements Cloneable {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    /* access modifiers changed from: private */
    @NotNull
    public static final List<ConnectionSpec> DEFAULT_CONNECTION_SPECS = Util.immutableListOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT);
    /* access modifiers changed from: private */
    @NotNull
    public static final List<Protocol> DEFAULT_PROTOCOLS = Util.immutableListOf(Protocol.HTTP_2, Protocol.HTTP_1_1);
    @NotNull
    private final Authenticator authenticator;
    private final int callTimeoutMillis;
    @Nullable
    private final CertificateChainCleaner certificateChainCleaner;
    @NotNull
    private final CertificatePinner certificatePinner;
    private final int connectTimeoutMillis;
    @NotNull
    private final ConnectionPool connectionPool;
    @NotNull
    private final List<ConnectionSpec> connectionSpecs;
    @NotNull
    private final CookieJar cookieJar;
    @NotNull
    private final Dispatcher dispatcher;
    @NotNull
    private final Dns dns;
    @NotNull
    private final EventListener.Factory eventListenerFactory;
    private final boolean followRedirects;
    private final boolean followSslRedirects;
    @NotNull
    private final HostnameVerifier hostnameVerifier;
    @NotNull
    private final List<Interceptor> interceptors;
    private final long minWebSocketMessageToCompress;
    @NotNull
    private final List<Interceptor> networkInterceptors;
    private final int pingIntervalMillis;
    @NotNull
    private final List<Protocol> protocols;
    @Nullable
    private final Proxy proxy;
    @NotNull
    private final Authenticator proxyAuthenticator;
    @NotNull
    private final ProxySelector proxySelector;
    private final int readTimeoutMillis;
    private final boolean retryOnConnectionFailure;
    @NotNull
    private final RouteDatabase routeDatabase;
    @NotNull
    private final SocketFactory socketFactory;
    /* access modifiers changed from: private */
    @Nullable
    public final SSLSocketFactory sslSocketFactoryOrNull;
    private final int writeTimeoutMillis;
    @Nullable
    private final X509TrustManager x509TrustManager;

    @Nullable
    public final Cache cache() {
        return null;
    }

    public OkHttpClient(@NotNull Builder builder) {
        ProxySelector proxySelector2;
        Intrinsics.checkNotNullParameter(builder, "builder");
        this.dispatcher = builder.getDispatcher$okhttp();
        this.connectionPool = builder.getConnectionPool$okhttp();
        this.interceptors = Util.toImmutableList(builder.getInterceptors$okhttp());
        this.networkInterceptors = Util.toImmutableList(builder.getNetworkInterceptors$okhttp());
        this.eventListenerFactory = builder.getEventListenerFactory$okhttp();
        this.retryOnConnectionFailure = builder.getRetryOnConnectionFailure$okhttp();
        this.authenticator = builder.getAuthenticator$okhttp();
        this.followRedirects = builder.getFollowRedirects$okhttp();
        this.followSslRedirects = builder.getFollowSslRedirects$okhttp();
        this.cookieJar = builder.getCookieJar$okhttp();
        builder.getCache$okhttp();
        this.dns = builder.getDns$okhttp();
        this.proxy = builder.getProxy$okhttp();
        if (builder.getProxy$okhttp() != null) {
            proxySelector2 = NullProxySelector.INSTANCE;
        } else {
            proxySelector2 = builder.getProxySelector$okhttp();
            proxySelector2 = proxySelector2 == null ? ProxySelector.getDefault() : proxySelector2;
            if (proxySelector2 == null) {
                proxySelector2 = NullProxySelector.INSTANCE;
            }
        }
        this.proxySelector = proxySelector2;
        this.proxyAuthenticator = builder.getProxyAuthenticator$okhttp();
        this.socketFactory = builder.getSocketFactory$okhttp();
        List<ConnectionSpec> connectionSpecs$okhttp = builder.getConnectionSpecs$okhttp();
        this.connectionSpecs = connectionSpecs$okhttp;
        this.protocols = builder.getProtocols$okhttp();
        this.hostnameVerifier = builder.getHostnameVerifier$okhttp();
        this.callTimeoutMillis = builder.getCallTimeout$okhttp();
        this.connectTimeoutMillis = builder.getConnectTimeout$okhttp();
        this.readTimeoutMillis = builder.getReadTimeout$okhttp();
        this.writeTimeoutMillis = builder.getWriteTimeout$okhttp();
        this.pingIntervalMillis = builder.getPingInterval$okhttp();
        this.minWebSocketMessageToCompress = builder.getMinWebSocketMessageToCompress$okhttp();
        RouteDatabase routeDatabase$okhttp = builder.getRouteDatabase$okhttp();
        this.routeDatabase = routeDatabase$okhttp == null ? new RouteDatabase() : routeDatabase$okhttp;
        boolean z = true;
        if (!(connectionSpecs$okhttp instanceof Collection) || !connectionSpecs$okhttp.isEmpty()) {
            Iterator<T> it = connectionSpecs$okhttp.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (((ConnectionSpec) it.next()).isTls()) {
                        z = false;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        if (z) {
            this.sslSocketFactoryOrNull = null;
            this.certificateChainCleaner = null;
            this.x509TrustManager = null;
            this.certificatePinner = CertificatePinner.DEFAULT;
        } else if (builder.getSslSocketFactoryOrNull$okhttp() != null) {
            this.sslSocketFactoryOrNull = builder.getSslSocketFactoryOrNull$okhttp();
            CertificateChainCleaner certificateChainCleaner$okhttp = builder.getCertificateChainCleaner$okhttp();
            Intrinsics.checkNotNull(certificateChainCleaner$okhttp);
            this.certificateChainCleaner = certificateChainCleaner$okhttp;
            X509TrustManager x509TrustManagerOrNull$okhttp = builder.getX509TrustManagerOrNull$okhttp();
            Intrinsics.checkNotNull(x509TrustManagerOrNull$okhttp);
            this.x509TrustManager = x509TrustManagerOrNull$okhttp;
            CertificatePinner certificatePinner$okhttp = builder.getCertificatePinner$okhttp();
            Intrinsics.checkNotNull(certificateChainCleaner$okhttp);
            this.certificatePinner = certificatePinner$okhttp.withCertificateChainCleaner$okhttp(certificateChainCleaner$okhttp);
        } else {
            Platform.Companion companion = Platform.Companion;
            X509TrustManager platformTrustManager = companion.get().platformTrustManager();
            this.x509TrustManager = platformTrustManager;
            Platform platform = companion.get();
            Intrinsics.checkNotNull(platformTrustManager);
            this.sslSocketFactoryOrNull = platform.newSslSocketFactory(platformTrustManager);
            CertificateChainCleaner.Companion companion2 = CertificateChainCleaner.Companion;
            Intrinsics.checkNotNull(platformTrustManager);
            CertificateChainCleaner certificateChainCleaner2 = companion2.get(platformTrustManager);
            this.certificateChainCleaner = certificateChainCleaner2;
            CertificatePinner certificatePinner$okhttp2 = builder.getCertificatePinner$okhttp();
            Intrinsics.checkNotNull(certificateChainCleaner2);
            this.certificatePinner = certificatePinner$okhttp2.withCertificateChainCleaner$okhttp(certificateChainCleaner2);
        }
        verifyClientState();
    }

    @NotNull
    public Object clone() {
        return super.clone();
    }

    @NotNull
    public final Dispatcher dispatcher() {
        return this.dispatcher;
    }

    @NotNull
    public final ConnectionPool connectionPool() {
        return this.connectionPool;
    }

    @NotNull
    public final List<Interceptor> interceptors() {
        return this.interceptors;
    }

    @NotNull
    public final List<Interceptor> networkInterceptors() {
        return this.networkInterceptors;
    }

    @NotNull
    public final EventListener.Factory eventListenerFactory() {
        return this.eventListenerFactory;
    }

    public final boolean retryOnConnectionFailure() {
        return this.retryOnConnectionFailure;
    }

    @NotNull
    public final Authenticator authenticator() {
        return this.authenticator;
    }

    public final boolean followRedirects() {
        return this.followRedirects;
    }

    public final boolean followSslRedirects() {
        return this.followSslRedirects;
    }

    @NotNull
    public final CookieJar cookieJar() {
        return this.cookieJar;
    }

    @NotNull
    public final Dns dns() {
        return this.dns;
    }

    @Nullable
    public final Proxy proxy() {
        return this.proxy;
    }

    @NotNull
    public final ProxySelector proxySelector() {
        return this.proxySelector;
    }

    @NotNull
    public final Authenticator proxyAuthenticator() {
        return this.proxyAuthenticator;
    }

    @NotNull
    public final SocketFactory socketFactory() {
        return this.socketFactory;
    }

    @NotNull
    public final SSLSocketFactory sslSocketFactory() {
        SSLSocketFactory sSLSocketFactory = this.sslSocketFactoryOrNull;
        if (sSLSocketFactory != null) {
            return sSLSocketFactory;
        }
        throw new IllegalStateException("CLEARTEXT-only client");
    }

    @Nullable
    public final X509TrustManager x509TrustManager() {
        return this.x509TrustManager;
    }

    @NotNull
    public final List<ConnectionSpec> connectionSpecs() {
        return this.connectionSpecs;
    }

    @NotNull
    public final List<Protocol> protocols() {
        return this.protocols;
    }

    @NotNull
    public final HostnameVerifier hostnameVerifier() {
        return this.hostnameVerifier;
    }

    @NotNull
    public final CertificatePinner certificatePinner() {
        return this.certificatePinner;
    }

    @Nullable
    public final CertificateChainCleaner certificateChainCleaner() {
        return this.certificateChainCleaner;
    }

    public final int callTimeoutMillis() {
        return this.callTimeoutMillis;
    }

    public final int connectTimeoutMillis() {
        return this.connectTimeoutMillis;
    }

    public final int readTimeoutMillis() {
        return this.readTimeoutMillis;
    }

    public final int writeTimeoutMillis() {
        return this.writeTimeoutMillis;
    }

    public final int pingIntervalMillis() {
        return this.pingIntervalMillis;
    }

    public final long minWebSocketMessageToCompress() {
        return this.minWebSocketMessageToCompress;
    }

    @NotNull
    public final RouteDatabase getRouteDatabase() {
        return this.routeDatabase;
    }

    public OkHttpClient() {
        this(new Builder());
    }

    private final void verifyClientState() {
        boolean z;
        boolean z2 = true;
        if (!(!this.interceptors.contains((Object) null))) {
            throw new IllegalStateException(Intrinsics.stringPlus("Null interceptor: ", interceptors()).toString());
        } else if (!this.networkInterceptors.contains((Object) null)) {
            List<ConnectionSpec> list = this.connectionSpecs;
            if (!(list instanceof Collection) || !list.isEmpty()) {
                Iterator<T> it = list.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (((ConnectionSpec) it.next()).isTls()) {
                            z = false;
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            z = true;
            if (z) {
                if (this.sslSocketFactoryOrNull == null) {
                    if (this.certificateChainCleaner == null) {
                        if (this.x509TrustManager != null) {
                            z2 = false;
                        }
                        if (!z2) {
                            throw new IllegalStateException("Check failed.".toString());
                        } else if (!Intrinsics.areEqual(this.certificatePinner, CertificatePinner.DEFAULT)) {
                            throw new IllegalStateException("Check failed.".toString());
                        }
                    } else {
                        throw new IllegalStateException("Check failed.".toString());
                    }
                } else {
                    throw new IllegalStateException("Check failed.".toString());
                }
            } else if (this.sslSocketFactoryOrNull == null) {
                throw new IllegalStateException("sslSocketFactory == null".toString());
            } else if (this.certificateChainCleaner == null) {
                throw new IllegalStateException("certificateChainCleaner == null".toString());
            } else if (this.x509TrustManager == null) {
                throw new IllegalStateException("x509TrustManager == null".toString());
            }
        } else {
            throw new IllegalStateException(Intrinsics.stringPlus("Null network interceptor: ", networkInterceptors()).toString());
        }
    }

    @NotNull
    public Call newCall(@NotNull Request request) {
        Intrinsics.checkNotNullParameter(request, "request");
        return new RealCall(this, request, false);
    }

    @NotNull
    public Builder newBuilder() {
        return new Builder(this);
    }

    /* compiled from: OkHttpClient.kt */
    public static final class Builder {
        @NotNull
        private Authenticator authenticator;
        private int callTimeout;
        @Nullable
        private CertificateChainCleaner certificateChainCleaner;
        @NotNull
        private CertificatePinner certificatePinner;
        private int connectTimeout;
        @NotNull
        private ConnectionPool connectionPool;
        @NotNull
        private List<ConnectionSpec> connectionSpecs;
        @NotNull
        private CookieJar cookieJar;
        @NotNull
        private Dispatcher dispatcher;
        @NotNull
        private Dns dns;
        @NotNull
        private EventListener.Factory eventListenerFactory;
        private boolean followRedirects;
        private boolean followSslRedirects;
        @NotNull
        private HostnameVerifier hostnameVerifier;
        @NotNull
        private final List<Interceptor> interceptors;
        private long minWebSocketMessageToCompress;
        @NotNull
        private final List<Interceptor> networkInterceptors;
        private int pingInterval;
        @NotNull
        private List<? extends Protocol> protocols;
        @Nullable
        private Proxy proxy;
        @NotNull
        private Authenticator proxyAuthenticator;
        @Nullable
        private ProxySelector proxySelector;
        private int readTimeout;
        private boolean retryOnConnectionFailure;
        @Nullable
        private RouteDatabase routeDatabase;
        @NotNull
        private SocketFactory socketFactory;
        @Nullable
        private SSLSocketFactory sslSocketFactoryOrNull;
        private int writeTimeout;
        @Nullable
        private X509TrustManager x509TrustManagerOrNull;

        @Nullable
        public final Cache getCache$okhttp() {
            return null;
        }

        public Builder() {
            this.dispatcher = new Dispatcher();
            this.connectionPool = new ConnectionPool();
            this.interceptors = new ArrayList();
            this.networkInterceptors = new ArrayList();
            this.eventListenerFactory = Util.asFactory(EventListener.NONE);
            this.retryOnConnectionFailure = true;
            Authenticator authenticator2 = Authenticator.NONE;
            this.authenticator = authenticator2;
            this.followRedirects = true;
            this.followSslRedirects = true;
            this.cookieJar = CookieJar.NO_COOKIES;
            this.dns = Dns.SYSTEM;
            this.proxyAuthenticator = authenticator2;
            SocketFactory socketFactory2 = SocketFactory.getDefault();
            Intrinsics.checkNotNullExpressionValue(socketFactory2, "getDefault()");
            this.socketFactory = socketFactory2;
            Companion companion = OkHttpClient.Companion;
            this.connectionSpecs = companion.getDEFAULT_CONNECTION_SPECS$okhttp();
            this.protocols = companion.getDEFAULT_PROTOCOLS$okhttp();
            this.hostnameVerifier = OkHostnameVerifier.INSTANCE;
            this.certificatePinner = CertificatePinner.DEFAULT;
            this.connectTimeout = 10000;
            this.readTimeout = 10000;
            this.writeTimeout = 10000;
            this.minWebSocketMessageToCompress = 1024;
        }

        @NotNull
        public final Dispatcher getDispatcher$okhttp() {
            return this.dispatcher;
        }

        @NotNull
        public final ConnectionPool getConnectionPool$okhttp() {
            return this.connectionPool;
        }

        @NotNull
        public final List<Interceptor> getInterceptors$okhttp() {
            return this.interceptors;
        }

        @NotNull
        public final List<Interceptor> getNetworkInterceptors$okhttp() {
            return this.networkInterceptors;
        }

        @NotNull
        public final EventListener.Factory getEventListenerFactory$okhttp() {
            return this.eventListenerFactory;
        }

        public final boolean getRetryOnConnectionFailure$okhttp() {
            return this.retryOnConnectionFailure;
        }

        public final void setRetryOnConnectionFailure$okhttp(boolean z) {
            this.retryOnConnectionFailure = z;
        }

        @NotNull
        public final Authenticator getAuthenticator$okhttp() {
            return this.authenticator;
        }

        public final boolean getFollowRedirects$okhttp() {
            return this.followRedirects;
        }

        public final void setFollowRedirects$okhttp(boolean z) {
            this.followRedirects = z;
        }

        public final boolean getFollowSslRedirects$okhttp() {
            return this.followSslRedirects;
        }

        @NotNull
        public final CookieJar getCookieJar$okhttp() {
            return this.cookieJar;
        }

        public final void setCookieJar$okhttp(@NotNull CookieJar cookieJar2) {
            Intrinsics.checkNotNullParameter(cookieJar2, "<set-?>");
            this.cookieJar = cookieJar2;
        }

        @NotNull
        public final Dns getDns$okhttp() {
            return this.dns;
        }

        public final void setDns$okhttp(@NotNull Dns dns2) {
            Intrinsics.checkNotNullParameter(dns2, "<set-?>");
            this.dns = dns2;
        }

        @Nullable
        public final Proxy getProxy$okhttp() {
            return this.proxy;
        }

        public final void setProxy$okhttp(@Nullable Proxy proxy2) {
            this.proxy = proxy2;
        }

        @Nullable
        public final ProxySelector getProxySelector$okhttp() {
            return this.proxySelector;
        }

        @NotNull
        public final Authenticator getProxyAuthenticator$okhttp() {
            return this.proxyAuthenticator;
        }

        @NotNull
        public final SocketFactory getSocketFactory$okhttp() {
            return this.socketFactory;
        }

        public final void setSocketFactory$okhttp(@NotNull SocketFactory socketFactory2) {
            Intrinsics.checkNotNullParameter(socketFactory2, "<set-?>");
            this.socketFactory = socketFactory2;
        }

        @Nullable
        public final SSLSocketFactory getSslSocketFactoryOrNull$okhttp() {
            return this.sslSocketFactoryOrNull;
        }

        public final void setSslSocketFactoryOrNull$okhttp(@Nullable SSLSocketFactory sSLSocketFactory) {
            this.sslSocketFactoryOrNull = sSLSocketFactory;
        }

        @Nullable
        public final X509TrustManager getX509TrustManagerOrNull$okhttp() {
            return this.x509TrustManagerOrNull;
        }

        public final void setX509TrustManagerOrNull$okhttp(@Nullable X509TrustManager x509TrustManager) {
            this.x509TrustManagerOrNull = x509TrustManager;
        }

        @NotNull
        public final List<ConnectionSpec> getConnectionSpecs$okhttp() {
            return this.connectionSpecs;
        }

        public final void setConnectionSpecs$okhttp(@NotNull List<ConnectionSpec> list) {
            Intrinsics.checkNotNullParameter(list, "<set-?>");
            this.connectionSpecs = list;
        }

        @NotNull
        public final List<Protocol> getProtocols$okhttp() {
            return this.protocols;
        }

        @NotNull
        public final HostnameVerifier getHostnameVerifier$okhttp() {
            return this.hostnameVerifier;
        }

        public final void setHostnameVerifier$okhttp(@NotNull HostnameVerifier hostnameVerifier2) {
            Intrinsics.checkNotNullParameter(hostnameVerifier2, "<set-?>");
            this.hostnameVerifier = hostnameVerifier2;
        }

        @NotNull
        public final CertificatePinner getCertificatePinner$okhttp() {
            return this.certificatePinner;
        }

        @Nullable
        public final CertificateChainCleaner getCertificateChainCleaner$okhttp() {
            return this.certificateChainCleaner;
        }

        public final void setCertificateChainCleaner$okhttp(@Nullable CertificateChainCleaner certificateChainCleaner2) {
            this.certificateChainCleaner = certificateChainCleaner2;
        }

        public final int getCallTimeout$okhttp() {
            return this.callTimeout;
        }

        public final int getConnectTimeout$okhttp() {
            return this.connectTimeout;
        }

        public final void setConnectTimeout$okhttp(int i) {
            this.connectTimeout = i;
        }

        public final int getReadTimeout$okhttp() {
            return this.readTimeout;
        }

        public final void setReadTimeout$okhttp(int i) {
            this.readTimeout = i;
        }

        public final int getWriteTimeout$okhttp() {
            return this.writeTimeout;
        }

        public final void setWriteTimeout$okhttp(int i) {
            this.writeTimeout = i;
        }

        public final int getPingInterval$okhttp() {
            return this.pingInterval;
        }

        public final long getMinWebSocketMessageToCompress$okhttp() {
            return this.minWebSocketMessageToCompress;
        }

        @Nullable
        public final RouteDatabase getRouteDatabase$okhttp() {
            return this.routeDatabase;
        }

        public final void setRouteDatabase$okhttp(@Nullable RouteDatabase routeDatabase2) {
            this.routeDatabase = routeDatabase2;
        }

        /* JADX INFO: this call moved to the top of the method (can break code semantics) */
        public Builder(@NotNull OkHttpClient okHttpClient) {
            this();
            Intrinsics.checkNotNullParameter(okHttpClient, "okHttpClient");
            this.dispatcher = okHttpClient.dispatcher();
            this.connectionPool = okHttpClient.connectionPool();
            boolean unused = CollectionsKt__MutableCollectionsKt.addAll(this.interceptors, okHttpClient.interceptors());
            boolean unused2 = CollectionsKt__MutableCollectionsKt.addAll(this.networkInterceptors, okHttpClient.networkInterceptors());
            this.eventListenerFactory = okHttpClient.eventListenerFactory();
            this.retryOnConnectionFailure = okHttpClient.retryOnConnectionFailure();
            this.authenticator = okHttpClient.authenticator();
            this.followRedirects = okHttpClient.followRedirects();
            this.followSslRedirects = okHttpClient.followSslRedirects();
            this.cookieJar = okHttpClient.cookieJar();
            okHttpClient.cache();
            this.dns = okHttpClient.dns();
            this.proxy = okHttpClient.proxy();
            this.proxySelector = okHttpClient.proxySelector();
            this.proxyAuthenticator = okHttpClient.proxyAuthenticator();
            this.socketFactory = okHttpClient.socketFactory();
            this.sslSocketFactoryOrNull = okHttpClient.sslSocketFactoryOrNull;
            this.x509TrustManagerOrNull = okHttpClient.x509TrustManager();
            this.connectionSpecs = okHttpClient.connectionSpecs();
            this.protocols = okHttpClient.protocols();
            this.hostnameVerifier = okHttpClient.hostnameVerifier();
            this.certificatePinner = okHttpClient.certificatePinner();
            this.certificateChainCleaner = okHttpClient.certificateChainCleaner();
            this.callTimeout = okHttpClient.callTimeoutMillis();
            this.connectTimeout = okHttpClient.connectTimeoutMillis();
            this.readTimeout = okHttpClient.readTimeoutMillis();
            this.writeTimeout = okHttpClient.writeTimeoutMillis();
            this.pingInterval = okHttpClient.pingIntervalMillis();
            this.minWebSocketMessageToCompress = okHttpClient.minWebSocketMessageToCompress();
            this.routeDatabase = okHttpClient.getRouteDatabase();
        }

        @NotNull
        public final Builder retryOnConnectionFailure(boolean z) {
            setRetryOnConnectionFailure$okhttp(z);
            return this;
        }

        @NotNull
        public final Builder followRedirects(boolean z) {
            setFollowRedirects$okhttp(z);
            return this;
        }

        @NotNull
        public final Builder cookieJar(@NotNull CookieJar cookieJar2) {
            Intrinsics.checkNotNullParameter(cookieJar2, "cookieJar");
            setCookieJar$okhttp(cookieJar2);
            return this;
        }

        @NotNull
        public final Builder dns(@NotNull Dns dns2) {
            Intrinsics.checkNotNullParameter(dns2, "dns");
            if (!Intrinsics.areEqual(dns2, getDns$okhttp())) {
                setRouteDatabase$okhttp((RouteDatabase) null);
            }
            setDns$okhttp(dns2);
            return this;
        }

        @NotNull
        public final Builder proxy(@Nullable Proxy proxy2) {
            if (!Intrinsics.areEqual(proxy2, getProxy$okhttp())) {
                setRouteDatabase$okhttp((RouteDatabase) null);
            }
            setProxy$okhttp(proxy2);
            return this;
        }

        @NotNull
        public final Builder socketFactory(@NotNull SocketFactory socketFactory2) {
            Intrinsics.checkNotNullParameter(socketFactory2, "socketFactory");
            if (!(socketFactory2 instanceof SSLSocketFactory)) {
                if (!Intrinsics.areEqual(socketFactory2, getSocketFactory$okhttp())) {
                    setRouteDatabase$okhttp((RouteDatabase) null);
                }
                setSocketFactory$okhttp(socketFactory2);
                return this;
            }
            throw new IllegalArgumentException("socketFactory instanceof SSLSocketFactory".toString());
        }

        @NotNull
        public final Builder sslSocketFactory(@NotNull SSLSocketFactory sSLSocketFactory, @NotNull X509TrustManager x509TrustManager) {
            Intrinsics.checkNotNullParameter(sSLSocketFactory, "sslSocketFactory");
            Intrinsics.checkNotNullParameter(x509TrustManager, "trustManager");
            if (!Intrinsics.areEqual(sSLSocketFactory, getSslSocketFactoryOrNull$okhttp()) || !Intrinsics.areEqual(x509TrustManager, getX509TrustManagerOrNull$okhttp())) {
                setRouteDatabase$okhttp((RouteDatabase) null);
            }
            setSslSocketFactoryOrNull$okhttp(sSLSocketFactory);
            setCertificateChainCleaner$okhttp(CertificateChainCleaner.Companion.get(x509TrustManager));
            setX509TrustManagerOrNull$okhttp(x509TrustManager);
            return this;
        }

        @NotNull
        public final Builder connectionSpecs(@NotNull List<ConnectionSpec> list) {
            Intrinsics.checkNotNullParameter(list, "connectionSpecs");
            if (!Intrinsics.areEqual(list, getConnectionSpecs$okhttp())) {
                setRouteDatabase$okhttp((RouteDatabase) null);
            }
            setConnectionSpecs$okhttp(Util.toImmutableList(list));
            return this;
        }

        @NotNull
        public final Builder hostnameVerifier(@NotNull HostnameVerifier hostnameVerifier2) {
            Intrinsics.checkNotNullParameter(hostnameVerifier2, "hostnameVerifier");
            if (!Intrinsics.areEqual(hostnameVerifier2, getHostnameVerifier$okhttp())) {
                setRouteDatabase$okhttp((RouteDatabase) null);
            }
            setHostnameVerifier$okhttp(hostnameVerifier2);
            return this;
        }

        @NotNull
        public final Builder connectTimeout(long j, @NotNull TimeUnit timeUnit) {
            Intrinsics.checkNotNullParameter(timeUnit, "unit");
            setConnectTimeout$okhttp(Util.checkDuration(EucTestIntent.Extras.TIMEOUT, j, timeUnit));
            return this;
        }

        @NotNull
        public final Builder readTimeout(long j, @NotNull TimeUnit timeUnit) {
            Intrinsics.checkNotNullParameter(timeUnit, "unit");
            setReadTimeout$okhttp(Util.checkDuration(EucTestIntent.Extras.TIMEOUT, j, timeUnit));
            return this;
        }

        @NotNull
        public final Builder writeTimeout(long j, @NotNull TimeUnit timeUnit) {
            Intrinsics.checkNotNullParameter(timeUnit, "unit");
            setWriteTimeout$okhttp(Util.checkDuration(EucTestIntent.Extras.TIMEOUT, j, timeUnit));
            return this;
        }

        @NotNull
        public final OkHttpClient build() {
            return new OkHttpClient(this);
        }
    }

    /* compiled from: OkHttpClient.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @NotNull
        public final List<Protocol> getDEFAULT_PROTOCOLS$okhttp() {
            return OkHttpClient.DEFAULT_PROTOCOLS;
        }

        @NotNull
        public final List<ConnectionSpec> getDEFAULT_CONNECTION_SPECS$okhttp() {
            return OkHttpClient.DEFAULT_CONNECTION_SPECS;
        }
    }
}
