package okhttp3.internal.connection;

import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.core.RegistrationEvents;
import java.io.IOException;
import java.lang.ref.Reference;
import java.net.ConnectException;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLPeerUnverifiedException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Address;
import okhttp3.Call;
import okhttp3.CertificatePinner;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.internal.Util;
import okhttp3.internal.concurrent.TaskRunner;
import okhttp3.internal.http.ExchangeCodec;
import okhttp3.internal.http.RealInterceptorChain;
import okhttp3.internal.http1.Http1ExchangeCodec;
import okhttp3.internal.http2.ConnectionShutdownException;
import okhttp3.internal.http2.ErrorCode;
import okhttp3.internal.http2.Http2Connection;
import okhttp3.internal.http2.Http2ExchangeCodec;
import okhttp3.internal.http2.Http2Stream;
import okhttp3.internal.http2.Settings;
import okhttp3.internal.http2.StreamResetException;
import okhttp3.internal.platform.Platform;
import okhttp3.internal.tls.OkHostnameVerifier;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: RealConnection.kt */
public final class RealConnection extends Http2Connection.Listener implements Connection {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    private int allocationLimit = 1;
    @NotNull
    private final List<Reference<RealCall>> calls = new ArrayList();
    @NotNull
    private final RealConnectionPool connectionPool;
    /* access modifiers changed from: private */
    @Nullable
    public Handshake handshake;
    @Nullable
    private Http2Connection http2Connection;
    private long idleAtNs = Long.MAX_VALUE;
    private boolean noCoalescedConnections;
    private boolean noNewExchanges;
    @Nullable
    private Protocol protocol;
    @Nullable
    private Socket rawSocket;
    private int refusedStreamCount;
    @NotNull
    private final Route route;
    private int routeFailureCount;
    @Nullable
    private BufferedSink sink;
    @Nullable
    private Socket socket;
    @Nullable
    private BufferedSource source;
    private int successCount;

    /* compiled from: RealConnection.kt */
    public /* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[Proxy.Type.values().length];
            iArr[Proxy.Type.DIRECT.ordinal()] = 1;
            iArr[Proxy.Type.HTTP.ordinal()] = 2;
            $EnumSwitchMapping$0 = iArr;
        }
    }

    public RealConnection(@NotNull RealConnectionPool realConnectionPool, @NotNull Route route2) {
        Intrinsics.checkNotNullParameter(realConnectionPool, "connectionPool");
        Intrinsics.checkNotNullParameter(route2, "route");
        this.connectionPool = realConnectionPool;
        this.route = route2;
    }

    public final boolean getNoNewExchanges() {
        return this.noNewExchanges;
    }

    public final void setNoNewExchanges(boolean z) {
        this.noNewExchanges = z;
    }

    public final int getRouteFailureCount$okhttp() {
        return this.routeFailureCount;
    }

    @NotNull
    public final List<Reference<RealCall>> getCalls() {
        return this.calls;
    }

    public final long getIdleAtNs$okhttp() {
        return this.idleAtNs;
    }

    public final void setIdleAtNs$okhttp(long j) {
        this.idleAtNs = j;
    }

    public final boolean isMultiplexed$okhttp() {
        return this.http2Connection != null;
    }

    public final synchronized void noNewExchanges$okhttp() {
        this.noNewExchanges = true;
    }

    public final synchronized void noCoalescedConnections$okhttp() {
        this.noCoalescedConnections = true;
    }

    public final synchronized void incrementSuccessCount$okhttp() {
        this.successCount++;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x00a2 A[Catch:{ IOException -> 0x00fd }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00bb  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00e5  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00f2  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0109  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0111  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x013b  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0141  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void connect(int r17, int r18, int r19, int r20, boolean r21, @org.jetbrains.annotations.NotNull okhttp3.Call r22, @org.jetbrains.annotations.NotNull okhttp3.EventListener r23) {
        /*
            r16 = this;
            r7 = r16
            r8 = r22
            r9 = r23
            java.lang.String r0 = "call"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r8, r0)
            java.lang.String r0 = "eventListener"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r9, r0)
            okhttp3.Protocol r0 = r7.protocol
            r10 = 1
            if (r0 != 0) goto L_0x0017
            r0 = r10
            goto L_0x0018
        L_0x0017:
            r0 = 0
        L_0x0018:
            if (r0 == 0) goto L_0x015c
            okhttp3.Route r0 = r7.route
            okhttp3.Address r0 = r0.address()
            java.util.List r0 = r0.connectionSpecs()
            okhttp3.internal.connection.ConnectionSpecSelector r11 = new okhttp3.internal.connection.ConnectionSpecSelector
            r11.<init>(r0)
            okhttp3.Route r1 = r7.route
            okhttp3.Address r1 = r1.address()
            javax.net.ssl.SSLSocketFactory r1 = r1.sslSocketFactory()
            if (r1 != 0) goto L_0x0086
            okhttp3.ConnectionSpec r1 = okhttp3.ConnectionSpec.CLEARTEXT
            boolean r0 = r0.contains(r1)
            if (r0 == 0) goto L_0x0079
            okhttp3.Route r0 = r7.route
            okhttp3.Address r0 = r0.address()
            okhttp3.HttpUrl r0 = r0.url()
            java.lang.String r0 = r0.host()
            okhttp3.internal.platform.Platform$Companion r1 = okhttp3.internal.platform.Platform.Companion
            okhttp3.internal.platform.Platform r1 = r1.get()
            boolean r1 = r1.isCleartextTrafficPermitted(r0)
            if (r1 == 0) goto L_0x0058
            goto L_0x0098
        L_0x0058:
            okhttp3.internal.connection.RouteException r1 = new okhttp3.internal.connection.RouteException
            java.net.UnknownServiceException r2 = new java.net.UnknownServiceException
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "CLEARTEXT communication to "
            r3.append(r4)
            r3.append(r0)
            java.lang.String r0 = " not permitted by network security policy"
            r3.append(r0)
            java.lang.String r0 = r3.toString()
            r2.<init>(r0)
            r1.<init>(r2)
            throw r1
        L_0x0079:
            okhttp3.internal.connection.RouteException r0 = new okhttp3.internal.connection.RouteException
            java.net.UnknownServiceException r1 = new java.net.UnknownServiceException
            java.lang.String r2 = "CLEARTEXT communication not enabled for client"
            r1.<init>(r2)
            r0.<init>(r1)
            throw r0
        L_0x0086:
            okhttp3.Route r0 = r7.route
            okhttp3.Address r0 = r0.address()
            java.util.List r0 = r0.protocols()
            okhttp3.Protocol r1 = okhttp3.Protocol.H2_PRIOR_KNOWLEDGE
            boolean r0 = r0.contains(r1)
            if (r0 != 0) goto L_0x014f
        L_0x0098:
            r12 = 0
            r13 = r12
        L_0x009a:
            okhttp3.Route r0 = r7.route     // Catch:{ IOException -> 0x00fd }
            boolean r0 = r0.requiresTunnel()     // Catch:{ IOException -> 0x00fd }
            if (r0 == 0) goto L_0x00bb
            r1 = r16
            r2 = r17
            r3 = r18
            r4 = r19
            r5 = r22
            r6 = r23
            r1.connectTunnel(r2, r3, r4, r5, r6)     // Catch:{ IOException -> 0x00fd }
            java.net.Socket r0 = r7.rawSocket     // Catch:{ IOException -> 0x00fd }
            if (r0 != 0) goto L_0x00b6
            goto L_0x00d8
        L_0x00b6:
            r14 = r17
            r15 = r18
            goto L_0x00c2
        L_0x00bb:
            r14 = r17
            r15 = r18
            r7.connectSocket(r14, r15, r8, r9)     // Catch:{ IOException -> 0x00fb }
        L_0x00c2:
            r6 = r20
            r7.establishProtocol(r11, r6, r8, r9)     // Catch:{ IOException -> 0x00f9 }
            okhttp3.Route r0 = r7.route     // Catch:{ IOException -> 0x00f9 }
            java.net.InetSocketAddress r0 = r0.socketAddress()     // Catch:{ IOException -> 0x00f9 }
            okhttp3.Route r1 = r7.route     // Catch:{ IOException -> 0x00f9 }
            java.net.Proxy r1 = r1.proxy()     // Catch:{ IOException -> 0x00f9 }
            okhttp3.Protocol r2 = r7.protocol     // Catch:{ IOException -> 0x00f9 }
            r9.connectEnd(r8, r0, r1, r2)     // Catch:{ IOException -> 0x00f9 }
        L_0x00d8:
            okhttp3.Route r0 = r7.route
            boolean r0 = r0.requiresTunnel()
            if (r0 == 0) goto L_0x00f2
            java.net.Socket r0 = r7.rawSocket
            if (r0 == 0) goto L_0x00e5
            goto L_0x00f2
        L_0x00e5:
            okhttp3.internal.connection.RouteException r0 = new okhttp3.internal.connection.RouteException
            java.net.ProtocolException r1 = new java.net.ProtocolException
            java.lang.String r2 = "Too many tunnel connections attempted: 21"
            r1.<init>(r2)
            r0.<init>(r1)
            throw r0
        L_0x00f2:
            long r0 = java.lang.System.nanoTime()
            r7.idleAtNs = r0
            return
        L_0x00f9:
            r0 = move-exception
            goto L_0x0104
        L_0x00fb:
            r0 = move-exception
            goto L_0x0102
        L_0x00fd:
            r0 = move-exception
            r14 = r17
            r15 = r18
        L_0x0102:
            r6 = r20
        L_0x0104:
            java.net.Socket r1 = r7.socket
            if (r1 != 0) goto L_0x0109
            goto L_0x010c
        L_0x0109:
            okhttp3.internal.Util.closeQuietly((java.net.Socket) r1)
        L_0x010c:
            java.net.Socket r1 = r7.rawSocket
            if (r1 != 0) goto L_0x0111
            goto L_0x0114
        L_0x0111:
            okhttp3.internal.Util.closeQuietly((java.net.Socket) r1)
        L_0x0114:
            r7.socket = r12
            r7.rawSocket = r12
            r7.source = r12
            r7.sink = r12
            r7.handshake = r12
            r7.protocol = r12
            r7.http2Connection = r12
            r7.allocationLimit = r10
            okhttp3.Route r1 = r7.route
            java.net.InetSocketAddress r3 = r1.socketAddress()
            okhttp3.Route r1 = r7.route
            java.net.Proxy r4 = r1.proxy()
            r5 = 0
            r1 = r23
            r2 = r22
            r6 = r0
            r1.connectFailed(r2, r3, r4, r5, r6)
            if (r13 != 0) goto L_0x0141
            okhttp3.internal.connection.RouteException r13 = new okhttp3.internal.connection.RouteException
            r13.<init>(r0)
            goto L_0x0144
        L_0x0141:
            r13.addConnectException(r0)
        L_0x0144:
            if (r21 == 0) goto L_0x014e
            boolean r0 = r11.connectionFailed(r0)
            if (r0 == 0) goto L_0x014e
            goto L_0x009a
        L_0x014e:
            throw r13
        L_0x014f:
            okhttp3.internal.connection.RouteException r0 = new okhttp3.internal.connection.RouteException
            java.net.UnknownServiceException r1 = new java.net.UnknownServiceException
            java.lang.String r2 = "H2_PRIOR_KNOWLEDGE cannot be used with HTTPS"
            r1.<init>(r2)
            r0.<init>(r1)
            throw r0
        L_0x015c:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
            java.lang.String r1 = "already connected"
            java.lang.String r1 = r1.toString()
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.connection.RealConnection.connect(int, int, int, int, boolean, okhttp3.Call, okhttp3.EventListener):void");
    }

    private final void connectTunnel(int i, int i2, int i3, Call call, EventListener eventListener) throws IOException {
        Request createTunnelRequest = createTunnelRequest();
        HttpUrl url = createTunnelRequest.url();
        int i4 = 0;
        while (i4 < 21) {
            i4++;
            connectSocket(i, i2, call, eventListener);
            createTunnelRequest = createTunnel(i2, i3, createTunnelRequest, url);
            if (createTunnelRequest != null) {
                Socket socket2 = this.rawSocket;
                if (socket2 != null) {
                    Util.closeQuietly(socket2);
                }
                this.rawSocket = null;
                this.sink = null;
                this.source = null;
                eventListener.connectEnd(call, this.route.socketAddress(), this.route.proxy(), (Protocol) null);
            } else {
                return;
            }
        }
    }

    private final void connectSocket(int i, int i2, Call call, EventListener eventListener) throws IOException {
        Socket socket2;
        Proxy proxy = this.route.proxy();
        Address address = this.route.address();
        Proxy.Type type = proxy.type();
        int i3 = type == null ? -1 : WhenMappings.$EnumSwitchMapping$0[type.ordinal()];
        if (i3 == 1 || i3 == 2) {
            socket2 = address.socketFactory().createSocket();
            Intrinsics.checkNotNull(socket2);
        } else {
            socket2 = new Socket(proxy);
        }
        this.rawSocket = socket2;
        eventListener.connectStart(call, this.route.socketAddress(), proxy);
        socket2.setSoTimeout(i2);
        try {
            Platform.Companion.get().connectSocket(socket2, this.route.socketAddress(), i);
            try {
                this.source = Okio.buffer(Okio.source(socket2));
                this.sink = Okio.buffer(Okio.sink(socket2));
            } catch (NullPointerException e) {
                if (Intrinsics.areEqual(e.getMessage(), "throw with null exception")) {
                    throw new IOException(e);
                }
            }
        } catch (ConnectException e2) {
            ConnectException connectException = new ConnectException(Intrinsics.stringPlus("Failed to connect to ", this.route.socketAddress()));
            connectException.initCause(e2);
            throw connectException;
        }
    }

    private final void establishProtocol(ConnectionSpecSelector connectionSpecSelector, int i, Call call, EventListener eventListener) throws IOException {
        if (this.route.address().sslSocketFactory() == null) {
            List<Protocol> protocols = this.route.address().protocols();
            Protocol protocol2 = Protocol.H2_PRIOR_KNOWLEDGE;
            if (protocols.contains(protocol2)) {
                this.socket = this.rawSocket;
                this.protocol = protocol2;
                startHttp2(i);
                return;
            }
            this.socket = this.rawSocket;
            this.protocol = Protocol.HTTP_1_1;
            return;
        }
        eventListener.secureConnectStart(call);
        connectTls(connectionSpecSelector);
        eventListener.secureConnectEnd(call, this.handshake);
        if (this.protocol == Protocol.HTTP_2) {
            startHttp2(i);
        }
    }

    private final void startHttp2(int i) throws IOException {
        Socket socket2 = this.socket;
        Intrinsics.checkNotNull(socket2);
        BufferedSource bufferedSource = this.source;
        Intrinsics.checkNotNull(bufferedSource);
        BufferedSink bufferedSink = this.sink;
        Intrinsics.checkNotNull(bufferedSink);
        socket2.setSoTimeout(0);
        Http2Connection build = new Http2Connection.Builder(true, TaskRunner.INSTANCE).socket(socket2, this.route.address().url().host(), bufferedSource, bufferedSink).listener(this).pingIntervalMillis(i).build();
        this.http2Connection = build;
        this.allocationLimit = Http2Connection.Companion.getDEFAULT_SETTINGS().getMaxConcurrentStreams();
        Http2Connection.start$default(build, false, (TaskRunner) null, 3, (Object) null);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v1, resolved type: javax.net.ssl.SSLSocket} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v3, resolved type: java.lang.String} */
    /* JADX WARNING: type inference failed for: r2v0 */
    /* JADX WARNING: type inference failed for: r2v2 */
    /* JADX WARNING: type inference failed for: r2v6 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0174  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0180  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void connectTls(okhttp3.internal.connection.ConnectionSpecSelector r11) throws java.io.IOException {
        /*
            r10 = this;
            okhttp3.Route r0 = r10.route
            okhttp3.Address r0 = r0.address()
            javax.net.ssl.SSLSocketFactory r1 = r0.sslSocketFactory()
            r2 = 0
            kotlin.jvm.internal.Intrinsics.checkNotNull(r1)     // Catch:{ all -> 0x0171 }
            java.net.Socket r3 = r10.rawSocket     // Catch:{ all -> 0x0171 }
            okhttp3.HttpUrl r4 = r0.url()     // Catch:{ all -> 0x0171 }
            java.lang.String r4 = r4.host()     // Catch:{ all -> 0x0171 }
            okhttp3.HttpUrl r5 = r0.url()     // Catch:{ all -> 0x0171 }
            int r5 = r5.port()     // Catch:{ all -> 0x0171 }
            r6 = 1
            java.net.Socket r1 = r1.createSocket(r3, r4, r5, r6)     // Catch:{ all -> 0x0171 }
            if (r1 == 0) goto L_0x0169
            javax.net.ssl.SSLSocket r1 = (javax.net.ssl.SSLSocket) r1     // Catch:{ all -> 0x0171 }
            okhttp3.ConnectionSpec r11 = r11.configureSecureSocket(r1)     // Catch:{ all -> 0x0166 }
            boolean r3 = r11.supportsTlsExtensions()     // Catch:{ all -> 0x0166 }
            if (r3 == 0) goto L_0x0048
            okhttp3.internal.platform.Platform$Companion r3 = okhttp3.internal.platform.Platform.Companion     // Catch:{ all -> 0x0166 }
            okhttp3.internal.platform.Platform r3 = r3.get()     // Catch:{ all -> 0x0166 }
            okhttp3.HttpUrl r4 = r0.url()     // Catch:{ all -> 0x0166 }
            java.lang.String r4 = r4.host()     // Catch:{ all -> 0x0166 }
            java.util.List r5 = r0.protocols()     // Catch:{ all -> 0x0166 }
            r3.configureTlsExtensions(r1, r4, r5)     // Catch:{ all -> 0x0166 }
        L_0x0048:
            r1.startHandshake()     // Catch:{ all -> 0x0166 }
            javax.net.ssl.SSLSession r3 = r1.getSession()     // Catch:{ all -> 0x0166 }
            okhttp3.Handshake$Companion r4 = okhttp3.Handshake.Companion     // Catch:{ all -> 0x0166 }
            java.lang.String r5 = "sslSocketSession"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r3, r5)     // Catch:{ all -> 0x0166 }
            okhttp3.Handshake r4 = r4.get(r3)     // Catch:{ all -> 0x0166 }
            javax.net.ssl.HostnameVerifier r5 = r0.hostnameVerifier()     // Catch:{ all -> 0x0166 }
            kotlin.jvm.internal.Intrinsics.checkNotNull(r5)     // Catch:{ all -> 0x0166 }
            okhttp3.HttpUrl r7 = r0.url()     // Catch:{ all -> 0x0166 }
            java.lang.String r7 = r7.host()     // Catch:{ all -> 0x0166 }
            boolean r3 = r5.verify(r7, r3)     // Catch:{ all -> 0x0166 }
            if (r3 != 0) goto L_0x00fa
            java.util.List r10 = r4.peerCertificates()     // Catch:{ all -> 0x0166 }
            boolean r11 = r10.isEmpty()     // Catch:{ all -> 0x0166 }
            r11 = r11 ^ r6
            if (r11 == 0) goto L_0x00d6
            r11 = 0
            java.lang.Object r10 = r10.get(r11)     // Catch:{ all -> 0x0166 }
            java.security.cert.X509Certificate r10 = (java.security.cert.X509Certificate) r10     // Catch:{ all -> 0x0166 }
            javax.net.ssl.SSLPeerUnverifiedException r11 = new javax.net.ssl.SSLPeerUnverifiedException     // Catch:{ all -> 0x0166 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0166 }
            r3.<init>()     // Catch:{ all -> 0x0166 }
            java.lang.String r4 = "\n              |Hostname "
            r3.append(r4)     // Catch:{ all -> 0x0166 }
            okhttp3.HttpUrl r0 = r0.url()     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = r0.host()     // Catch:{ all -> 0x0166 }
            r3.append(r0)     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = " not verified:\n              |    certificate: "
            r3.append(r0)     // Catch:{ all -> 0x0166 }
            okhttp3.CertificatePinner$Companion r0 = okhttp3.CertificatePinner.Companion     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = r0.pin(r10)     // Catch:{ all -> 0x0166 }
            r3.append(r0)     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = "\n              |    DN: "
            r3.append(r0)     // Catch:{ all -> 0x0166 }
            java.security.Principal r0 = r10.getSubjectDN()     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = r0.getName()     // Catch:{ all -> 0x0166 }
            r3.append(r0)     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = "\n              |    subjectAltNames: "
            r3.append(r0)     // Catch:{ all -> 0x0166 }
            okhttp3.internal.tls.OkHostnameVerifier r0 = okhttp3.internal.tls.OkHostnameVerifier.INSTANCE     // Catch:{ all -> 0x0166 }
            java.util.List r10 = r0.allSubjectAltNames(r10)     // Catch:{ all -> 0x0166 }
            r3.append(r10)     // Catch:{ all -> 0x0166 }
            java.lang.String r10 = "\n              "
            r3.append(r10)     // Catch:{ all -> 0x0166 }
            java.lang.String r10 = r3.toString()     // Catch:{ all -> 0x0166 }
            java.lang.String r10 = kotlin.text.StringsKt__IndentKt.trimMargin$default(r10, r2, r6, r2)     // Catch:{ all -> 0x0166 }
            r11.<init>(r10)     // Catch:{ all -> 0x0166 }
            throw r11     // Catch:{ all -> 0x0166 }
        L_0x00d6:
            javax.net.ssl.SSLPeerUnverifiedException r10 = new javax.net.ssl.SSLPeerUnverifiedException     // Catch:{ all -> 0x0166 }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x0166 }
            r11.<init>()     // Catch:{ all -> 0x0166 }
            java.lang.String r2 = "Hostname "
            r11.append(r2)     // Catch:{ all -> 0x0166 }
            okhttp3.HttpUrl r0 = r0.url()     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = r0.host()     // Catch:{ all -> 0x0166 }
            r11.append(r0)     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = " not verified (no certificates)"
            r11.append(r0)     // Catch:{ all -> 0x0166 }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x0166 }
            r10.<init>(r11)     // Catch:{ all -> 0x0166 }
            throw r10     // Catch:{ all -> 0x0166 }
        L_0x00fa:
            okhttp3.CertificatePinner r3 = r0.certificatePinner()     // Catch:{ all -> 0x0166 }
            kotlin.jvm.internal.Intrinsics.checkNotNull(r3)     // Catch:{ all -> 0x0166 }
            okhttp3.Handshake r5 = new okhttp3.Handshake     // Catch:{ all -> 0x0166 }
            okhttp3.TlsVersion r6 = r4.tlsVersion()     // Catch:{ all -> 0x0166 }
            okhttp3.CipherSuite r7 = r4.cipherSuite()     // Catch:{ all -> 0x0166 }
            java.util.List r8 = r4.localCertificates()     // Catch:{ all -> 0x0166 }
            okhttp3.internal.connection.RealConnection$connectTls$1 r9 = new okhttp3.internal.connection.RealConnection$connectTls$1     // Catch:{ all -> 0x0166 }
            r9.<init>(r3, r4, r0)     // Catch:{ all -> 0x0166 }
            r5.<init>(r6, r7, r8, r9)     // Catch:{ all -> 0x0166 }
            r10.handshake = r5     // Catch:{ all -> 0x0166 }
            okhttp3.HttpUrl r0 = r0.url()     // Catch:{ all -> 0x0166 }
            java.lang.String r0 = r0.host()     // Catch:{ all -> 0x0166 }
            okhttp3.internal.connection.RealConnection$connectTls$2 r4 = new okhttp3.internal.connection.RealConnection$connectTls$2     // Catch:{ all -> 0x0166 }
            r4.<init>(r10)     // Catch:{ all -> 0x0166 }
            r3.check$okhttp(r0, r4)     // Catch:{ all -> 0x0166 }
            boolean r11 = r11.supportsTlsExtensions()     // Catch:{ all -> 0x0166 }
            if (r11 == 0) goto L_0x0139
            okhttp3.internal.platform.Platform$Companion r11 = okhttp3.internal.platform.Platform.Companion     // Catch:{ all -> 0x0166 }
            okhttp3.internal.platform.Platform r11 = r11.get()     // Catch:{ all -> 0x0166 }
            java.lang.String r2 = r11.getSelectedProtocol(r1)     // Catch:{ all -> 0x0166 }
        L_0x0139:
            r10.socket = r1     // Catch:{ all -> 0x0166 }
            okio.Source r11 = okio.Okio.source((java.net.Socket) r1)     // Catch:{ all -> 0x0166 }
            okio.BufferedSource r11 = okio.Okio.buffer((okio.Source) r11)     // Catch:{ all -> 0x0166 }
            r10.source = r11     // Catch:{ all -> 0x0166 }
            okio.Sink r11 = okio.Okio.sink(r1)     // Catch:{ all -> 0x0166 }
            okio.BufferedSink r11 = okio.Okio.buffer((okio.Sink) r11)     // Catch:{ all -> 0x0166 }
            r10.sink = r11     // Catch:{ all -> 0x0166 }
            if (r2 == 0) goto L_0x0158
            okhttp3.Protocol$Companion r11 = okhttp3.Protocol.Companion     // Catch:{ all -> 0x0166 }
            okhttp3.Protocol r11 = r11.get(r2)     // Catch:{ all -> 0x0166 }
            goto L_0x015a
        L_0x0158:
            okhttp3.Protocol r11 = okhttp3.Protocol.HTTP_1_1     // Catch:{ all -> 0x0166 }
        L_0x015a:
            r10.protocol = r11     // Catch:{ all -> 0x0166 }
            okhttp3.internal.platform.Platform$Companion r10 = okhttp3.internal.platform.Platform.Companion
            okhttp3.internal.platform.Platform r10 = r10.get()
            r10.afterHandshake(r1)
            return
        L_0x0166:
            r10 = move-exception
            r2 = r1
            goto L_0x0172
        L_0x0169:
            java.lang.NullPointerException r10 = new java.lang.NullPointerException     // Catch:{ all -> 0x0171 }
            java.lang.String r11 = "null cannot be cast to non-null type javax.net.ssl.SSLSocket"
            r10.<init>(r11)     // Catch:{ all -> 0x0171 }
            throw r10     // Catch:{ all -> 0x0171 }
        L_0x0171:
            r10 = move-exception
        L_0x0172:
            if (r2 == 0) goto L_0x017d
            okhttp3.internal.platform.Platform$Companion r11 = okhttp3.internal.platform.Platform.Companion
            okhttp3.internal.platform.Platform r11 = r11.get()
            r11.afterHandshake(r2)
        L_0x017d:
            if (r2 != 0) goto L_0x0180
            goto L_0x0183
        L_0x0180:
            okhttp3.internal.Util.closeQuietly((java.net.Socket) r2)
        L_0x0183:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.connection.RealConnection.connectTls(okhttp3.internal.connection.ConnectionSpecSelector):void");
    }

    private final Request createTunnel(int i, int i2, Request request, HttpUrl httpUrl) throws IOException {
        String str = "CONNECT " + Util.toHostHeader(httpUrl, true) + " HTTP/1.1";
        while (true) {
            BufferedSource bufferedSource = this.source;
            Intrinsics.checkNotNull(bufferedSource);
            BufferedSink bufferedSink = this.sink;
            Intrinsics.checkNotNull(bufferedSink);
            Http1ExchangeCodec http1ExchangeCodec = new Http1ExchangeCodec((OkHttpClient) null, this, bufferedSource, bufferedSink);
            TimeUnit timeUnit = TimeUnit.MILLISECONDS;
            bufferedSource.timeout().timeout((long) i, timeUnit);
            bufferedSink.timeout().timeout((long) i2, timeUnit);
            http1ExchangeCodec.writeRequest(request.headers(), str);
            http1ExchangeCodec.finishRequest();
            Response.Builder readResponseHeaders = http1ExchangeCodec.readResponseHeaders(false);
            Intrinsics.checkNotNull(readResponseHeaders);
            Response build = readResponseHeaders.request(request).build();
            http1ExchangeCodec.skipConnectBody(build);
            int code = build.code();
            if (code != 200) {
                if (code == 407) {
                    Request authenticate = this.route.address().proxyAuthenticator().authenticate(this.route, build);
                    if (authenticate == null) {
                        throw new IOException("Failed to authenticate with proxy");
                    } else if (StringsKt__StringsJVMKt.equals("close", Response.header$default(build, "Connection", (String) null, 2, (Object) null), true)) {
                        return authenticate;
                    } else {
                        request = authenticate;
                    }
                } else {
                    throw new IOException(Intrinsics.stringPlus("Unexpected response code for CONNECT: ", Integer.valueOf(build.code())));
                }
            } else if (bufferedSource.getBuffer().exhausted() && bufferedSink.getBuffer().exhausted()) {
                return null;
            } else {
                throw new IOException("TLS tunnel buffered too many bytes!");
            }
        }
    }

    private final Request createTunnelRequest() throws IOException {
        Request build = new Request.Builder().url(this.route.address().url()).method("CONNECT", (RequestBody) null).header(HttpController.HEADER_HOST, Util.toHostHeader(this.route.address().url(), true)).header("Proxy-Connection", "Keep-Alive").header("User-Agent", "okhttp/4.10.0").build();
        Request authenticate = this.route.address().proxyAuthenticator().authenticate(this.route, new Response.Builder().request(build).protocol(Protocol.HTTP_1_1).code(RegistrationEvents.EVENT_CHECK_UNPROCESSED_OMADM_CONFIG).message("Preemptive Authenticate").body(Util.EMPTY_RESPONSE).sentRequestAtMillis(-1).receivedResponseAtMillis(-1).header("Proxy-Authenticate", "OkHttp-Preemptive").build());
        return authenticate == null ? build : authenticate;
    }

    private final boolean certificateSupportHost(HttpUrl httpUrl, Handshake handshake2) {
        List<Certificate> peerCertificates = handshake2.peerCertificates();
        return (peerCertificates.isEmpty() ^ true) && OkHostnameVerifier.INSTANCE.verify(httpUrl.host(), (X509Certificate) peerCertificates.get(0));
    }

    @NotNull
    public final ExchangeCodec newCodec$okhttp(@NotNull OkHttpClient okHttpClient, @NotNull RealInterceptorChain realInterceptorChain) throws SocketException {
        Intrinsics.checkNotNullParameter(okHttpClient, "client");
        Intrinsics.checkNotNullParameter(realInterceptorChain, "chain");
        Socket socket2 = this.socket;
        Intrinsics.checkNotNull(socket2);
        BufferedSource bufferedSource = this.source;
        Intrinsics.checkNotNull(bufferedSource);
        BufferedSink bufferedSink = this.sink;
        Intrinsics.checkNotNull(bufferedSink);
        Http2Connection http2Connection2 = this.http2Connection;
        if (http2Connection2 != null) {
            return new Http2ExchangeCodec(okHttpClient, this, realInterceptorChain, http2Connection2);
        }
        socket2.setSoTimeout(realInterceptorChain.readTimeoutMillis());
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        bufferedSource.timeout().timeout((long) realInterceptorChain.getReadTimeoutMillis$okhttp(), timeUnit);
        bufferedSink.timeout().timeout((long) realInterceptorChain.getWriteTimeoutMillis$okhttp(), timeUnit);
        return new Http1ExchangeCodec(okHttpClient, this, bufferedSource, bufferedSink);
    }

    private final boolean supportsUrl(HttpUrl httpUrl) {
        Handshake handshake2;
        if (!Util.assertionsEnabled || Thread.holdsLock(this)) {
            HttpUrl url = this.route.address().url();
            if (httpUrl.port() != url.port()) {
                return false;
            }
            if (Intrinsics.areEqual(httpUrl.host(), url.host())) {
                return true;
            }
            if (this.noCoalescedConnections || (handshake2 = this.handshake) == null) {
                return false;
            }
            Intrinsics.checkNotNull(handshake2);
            if (certificateSupportHost(httpUrl, handshake2)) {
                return true;
            }
            return false;
        }
        throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST hold lock on " + this);
    }

    public final boolean isEligible$okhttp(@NotNull Address address, @Nullable List<Route> list) {
        Intrinsics.checkNotNullParameter(address, "address");
        if (Util.assertionsEnabled && !Thread.holdsLock(this)) {
            throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST hold lock on " + this);
        } else if (this.calls.size() >= this.allocationLimit || this.noNewExchanges || !this.route.address().equalsNonHost$okhttp(address)) {
            return false;
        } else {
            if (Intrinsics.areEqual(address.url().host(), route().address().url().host())) {
                return true;
            }
            if (this.http2Connection == null || list == null || !routeMatchesAny(list) || address.hostnameVerifier() != OkHostnameVerifier.INSTANCE || !supportsUrl(address.url())) {
                return false;
            }
            try {
                CertificatePinner certificatePinner = address.certificatePinner();
                Intrinsics.checkNotNull(certificatePinner);
                String host = address.url().host();
                Handshake handshake2 = handshake();
                Intrinsics.checkNotNull(handshake2);
                certificatePinner.check(host, handshake2.peerCertificates());
                return true;
            } catch (SSLPeerUnverifiedException unused) {
                return false;
            }
        }
    }

    public final boolean isHealthy(boolean z) {
        long idleAtNs$okhttp;
        if (!Util.assertionsEnabled || !Thread.holdsLock(this)) {
            long nanoTime = System.nanoTime();
            Socket socket2 = this.rawSocket;
            Intrinsics.checkNotNull(socket2);
            Socket socket3 = this.socket;
            Intrinsics.checkNotNull(socket3);
            BufferedSource bufferedSource = this.source;
            Intrinsics.checkNotNull(bufferedSource);
            if (socket2.isClosed() || socket3.isClosed() || socket3.isInputShutdown() || socket3.isOutputShutdown()) {
                return false;
            }
            Http2Connection http2Connection2 = this.http2Connection;
            if (http2Connection2 != null) {
                return http2Connection2.isHealthy(nanoTime);
            }
            synchronized (this) {
                idleAtNs$okhttp = nanoTime - getIdleAtNs$okhttp();
            }
            if (idleAtNs$okhttp < 10000000000L || !z) {
                return true;
            }
            return Util.isHealthy(socket3, bufferedSource);
        }
        throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST NOT hold lock on " + this);
    }

    @NotNull
    public Route route() {
        return this.route;
    }

    public final void cancel() {
        Socket socket2 = this.rawSocket;
        if (socket2 != null) {
            Util.closeQuietly(socket2);
        }
    }

    @NotNull
    public Socket socket() {
        Socket socket2 = this.socket;
        Intrinsics.checkNotNull(socket2);
        return socket2;
    }

    public void onStream(@NotNull Http2Stream http2Stream) throws IOException {
        Intrinsics.checkNotNullParameter(http2Stream, "stream");
        http2Stream.close(ErrorCode.REFUSED_STREAM, (IOException) null);
    }

    public synchronized void onSettings(@NotNull Http2Connection http2Connection2, @NotNull Settings settings) {
        Intrinsics.checkNotNullParameter(http2Connection2, "connection");
        Intrinsics.checkNotNullParameter(settings, "settings");
        this.allocationLimit = settings.getMaxConcurrentStreams();
    }

    @Nullable
    public Handshake handshake() {
        return this.handshake;
    }

    public final void connectFailed$okhttp(@NotNull OkHttpClient okHttpClient, @NotNull Route route2, @NotNull IOException iOException) {
        Intrinsics.checkNotNullParameter(okHttpClient, "client");
        Intrinsics.checkNotNullParameter(route2, "failedRoute");
        Intrinsics.checkNotNullParameter(iOException, "failure");
        if (route2.proxy().type() != Proxy.Type.DIRECT) {
            Address address = route2.address();
            address.proxySelector().connectFailed(address.url().uri(), route2.proxy().address(), iOException);
        }
        okHttpClient.getRouteDatabase().failed(route2);
    }

    public final synchronized void trackFailure$okhttp(@NotNull RealCall realCall, @Nullable IOException iOException) {
        Intrinsics.checkNotNullParameter(realCall, "call");
        if (iOException instanceof StreamResetException) {
            if (((StreamResetException) iOException).errorCode == ErrorCode.REFUSED_STREAM) {
                int i = this.refusedStreamCount + 1;
                this.refusedStreamCount = i;
                if (i > 1) {
                    this.noNewExchanges = true;
                    this.routeFailureCount++;
                }
            } else if (((StreamResetException) iOException).errorCode != ErrorCode.CANCEL || !realCall.isCanceled()) {
                this.noNewExchanges = true;
                this.routeFailureCount++;
            }
        } else if (!isMultiplexed$okhttp() || (iOException instanceof ConnectionShutdownException)) {
            this.noNewExchanges = true;
            if (this.successCount == 0) {
                if (iOException != null) {
                    connectFailed$okhttp(realCall.getClient(), this.route, iOException);
                }
                this.routeFailureCount++;
            }
        }
    }

    @NotNull
    public String toString() {
        Object cipherSuite;
        StringBuilder sb = new StringBuilder();
        sb.append("Connection{");
        sb.append(this.route.address().url().host());
        sb.append(':');
        sb.append(this.route.address().url().port());
        sb.append(", proxy=");
        sb.append(this.route.proxy());
        sb.append(" hostAddress=");
        sb.append(this.route.socketAddress());
        sb.append(" cipherSuite=");
        Handshake handshake2 = this.handshake;
        Object obj = MessageContextValues.none;
        if (!(handshake2 == null || (cipherSuite = handshake2.cipherSuite()) == null)) {
            obj = cipherSuite;
        }
        sb.append(obj);
        sb.append(" protocol=");
        sb.append(this.protocol);
        sb.append('}');
        return sb.toString();
    }

    /* compiled from: RealConnection.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }

    private final boolean routeMatchesAny(List<Route> list) {
        boolean z;
        if ((list instanceof Collection) && list.isEmpty()) {
            return false;
        }
        for (Route route2 : list) {
            if (route2.proxy().type() == Proxy.Type.DIRECT && this.route.proxy().type() == Proxy.Type.DIRECT && Intrinsics.areEqual(this.route.socketAddress(), route2.socketAddress())) {
                z = true;
                continue;
            } else {
                z = false;
                continue;
            }
            if (z) {
                return true;
            }
        }
        return false;
    }
}
