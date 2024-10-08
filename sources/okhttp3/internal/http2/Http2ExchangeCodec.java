package okhttp3.internal.http2;

import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.helper.httpclient.HttpController;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.internal.connection.RealConnection;
import okhttp3.internal.http.ExchangeCodec;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http.RealInterceptorChain;
import okhttp3.internal.http.RequestLine;
import okhttp3.internal.http.StatusLine;
import okio.Sink;
import okio.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Http2ExchangeCodec.kt */
public final class Http2ExchangeCodec implements ExchangeCodec {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    /* access modifiers changed from: private */
    @NotNull
    public static final List<String> HTTP_2_SKIPPED_REQUEST_HEADERS = Util.immutableListOf("connection", "host", "keep-alive", "proxy-connection", "te", "transfer-encoding", "encoding", "upgrade", ":method", ":path", ":scheme", ":authority");
    /* access modifiers changed from: private */
    @NotNull
    public static final List<String> HTTP_2_SKIPPED_RESPONSE_HEADERS = Util.immutableListOf("connection", "host", "keep-alive", "proxy-connection", "te", "transfer-encoding", "encoding", "upgrade");
    private volatile boolean canceled;
    @NotNull
    private final RealInterceptorChain chain;
    @NotNull
    private final RealConnection connection;
    @NotNull
    private final Http2Connection http2Connection;
    @NotNull
    private final Protocol protocol;
    @Nullable
    private volatile Http2Stream stream;

    public Http2ExchangeCodec(@NotNull OkHttpClient okHttpClient, @NotNull RealConnection realConnection, @NotNull RealInterceptorChain realInterceptorChain, @NotNull Http2Connection http2Connection2) {
        Intrinsics.checkNotNullParameter(okHttpClient, "client");
        Intrinsics.checkNotNullParameter(realConnection, "connection");
        Intrinsics.checkNotNullParameter(realInterceptorChain, "chain");
        Intrinsics.checkNotNullParameter(http2Connection2, "http2Connection");
        this.connection = realConnection;
        this.chain = realInterceptorChain;
        this.http2Connection = http2Connection2;
        List<Protocol> protocols = okHttpClient.protocols();
        Protocol protocol2 = Protocol.H2_PRIOR_KNOWLEDGE;
        this.protocol = !protocols.contains(protocol2) ? Protocol.HTTP_2 : protocol2;
    }

    @NotNull
    public RealConnection getConnection() {
        return this.connection;
    }

    @NotNull
    public Sink createRequestBody(@NotNull Request request, long j) {
        Intrinsics.checkNotNullParameter(request, "request");
        Http2Stream http2Stream = this.stream;
        Intrinsics.checkNotNull(http2Stream);
        return http2Stream.getSink();
    }

    public void writeRequestHeaders(@NotNull Request request) {
        Intrinsics.checkNotNullParameter(request, "request");
        if (this.stream == null) {
            this.stream = this.http2Connection.newStream(Companion.http2HeadersList(request), request.body() != null);
            if (!this.canceled) {
                Http2Stream http2Stream = this.stream;
                Intrinsics.checkNotNull(http2Stream);
                TimeUnit timeUnit = TimeUnit.MILLISECONDS;
                http2Stream.readTimeout().timeout((long) this.chain.getReadTimeoutMillis$okhttp(), timeUnit);
                Http2Stream http2Stream2 = this.stream;
                Intrinsics.checkNotNull(http2Stream2);
                http2Stream2.writeTimeout().timeout((long) this.chain.getWriteTimeoutMillis$okhttp(), timeUnit);
                return;
            }
            Http2Stream http2Stream3 = this.stream;
            Intrinsics.checkNotNull(http2Stream3);
            http2Stream3.closeLater(ErrorCode.CANCEL);
            throw new IOException("Canceled");
        }
    }

    public void flushRequest() {
        this.http2Connection.flush();
    }

    public void finishRequest() {
        Http2Stream http2Stream = this.stream;
        Intrinsics.checkNotNull(http2Stream);
        http2Stream.getSink().close();
    }

    @Nullable
    public Response.Builder readResponseHeaders(boolean z) {
        Http2Stream http2Stream = this.stream;
        Intrinsics.checkNotNull(http2Stream);
        Response.Builder readHttp2HeadersList = Companion.readHttp2HeadersList(http2Stream.takeHeaders(), this.protocol);
        if (!z || readHttp2HeadersList.getCode$okhttp() != 100) {
            return readHttp2HeadersList;
        }
        return null;
    }

    public long reportedContentLength(@NotNull Response response) {
        Intrinsics.checkNotNullParameter(response, "response");
        if (!HttpHeaders.promisesBody(response)) {
            return 0;
        }
        return Util.headersContentLength(response);
    }

    @NotNull
    public Source openResponseBodySource(@NotNull Response response) {
        Intrinsics.checkNotNullParameter(response, "response");
        Http2Stream http2Stream = this.stream;
        Intrinsics.checkNotNull(http2Stream);
        return http2Stream.getSource$okhttp();
    }

    public void cancel() {
        this.canceled = true;
        Http2Stream http2Stream = this.stream;
        if (http2Stream != null) {
            http2Stream.closeLater(ErrorCode.CANCEL);
        }
    }

    /* compiled from: Http2ExchangeCodec.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @NotNull
        public final List<Header> http2HeadersList(@NotNull Request request) {
            Intrinsics.checkNotNullParameter(request, "request");
            Headers headers = request.headers();
            ArrayList arrayList = new ArrayList(headers.size() + 4);
            arrayList.add(new Header(Header.TARGET_METHOD, request.method()));
            arrayList.add(new Header(Header.TARGET_PATH, RequestLine.INSTANCE.requestPath(request.url())));
            String header = request.header(HttpController.HEADER_HOST);
            if (header != null) {
                arrayList.add(new Header(Header.TARGET_AUTHORITY, header));
            }
            arrayList.add(new Header(Header.TARGET_SCHEME, request.url().scheme()));
            int size = headers.size();
            int i = 0;
            while (i < size) {
                int i2 = i + 1;
                String name = headers.name(i);
                Locale locale = Locale.US;
                Intrinsics.checkNotNullExpressionValue(locale, "US");
                String lowerCase = name.toLowerCase(locale);
                Intrinsics.checkNotNullExpressionValue(lowerCase, "this as java.lang.String).toLowerCase(locale)");
                if (!Http2ExchangeCodec.HTTP_2_SKIPPED_REQUEST_HEADERS.contains(lowerCase) || (Intrinsics.areEqual(lowerCase, "te") && Intrinsics.areEqual(headers.value(i), "trailers"))) {
                    arrayList.add(new Header(lowerCase, headers.value(i)));
                }
                i = i2;
            }
            return arrayList;
        }

        @NotNull
        public final Response.Builder readHttp2HeadersList(@NotNull Headers headers, @NotNull Protocol protocol) {
            Intrinsics.checkNotNullParameter(headers, "headerBlock");
            Intrinsics.checkNotNullParameter(protocol, CloudMessageProviderContract.BufferDBSMS.PROTOCOL);
            Headers.Builder builder = new Headers.Builder();
            int size = headers.size();
            StatusLine statusLine = null;
            int i = 0;
            while (i < size) {
                int i2 = i + 1;
                String name = headers.name(i);
                String value = headers.value(i);
                if (Intrinsics.areEqual(name, ":status")) {
                    statusLine = StatusLine.Companion.parse(Intrinsics.stringPlus("HTTP/1.1 ", value));
                } else if (!Http2ExchangeCodec.HTTP_2_SKIPPED_RESPONSE_HEADERS.contains(name)) {
                    builder.addLenient$okhttp(name, value);
                }
                i = i2;
            }
            if (statusLine != null) {
                return new Response.Builder().protocol(protocol).code(statusLine.code).message(statusLine.message).headers(builder.build());
            }
            throw new ProtocolException("Expected ':status' header not present");
        }
    }
}
