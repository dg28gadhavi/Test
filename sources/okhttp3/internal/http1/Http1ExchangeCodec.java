package okhttp3.internal.http1;

import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.HttpRequest;
import java.io.EOFException;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.internal.connection.RealConnection;
import okhttp3.internal.http.ExchangeCodec;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http.RequestLine;
import okhttp3.internal.http.StatusLine;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingTimeout;
import okio.Sink;
import okio.Source;
import okio.Timeout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Http1ExchangeCodec.kt */
public final class Http1ExchangeCodec implements ExchangeCodec {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    /* access modifiers changed from: private */
    @Nullable
    public final OkHttpClient client;
    @NotNull
    private final RealConnection connection;
    /* access modifiers changed from: private */
    @NotNull
    public final HeadersReader headersReader;
    /* access modifiers changed from: private */
    @NotNull
    public final BufferedSink sink;
    /* access modifiers changed from: private */
    @NotNull
    public final BufferedSource source;
    /* access modifiers changed from: private */
    public int state;
    /* access modifiers changed from: private */
    @Nullable
    public Headers trailers;

    public Http1ExchangeCodec(@Nullable OkHttpClient okHttpClient, @NotNull RealConnection realConnection, @NotNull BufferedSource bufferedSource, @NotNull BufferedSink bufferedSink) {
        Intrinsics.checkNotNullParameter(realConnection, "connection");
        Intrinsics.checkNotNullParameter(bufferedSource, "source");
        Intrinsics.checkNotNullParameter(bufferedSink, "sink");
        this.client = okHttpClient;
        this.connection = realConnection;
        this.source = bufferedSource;
        this.sink = bufferedSink;
        this.headersReader = new HeadersReader(bufferedSource);
    }

    @NotNull
    public RealConnection getConnection() {
        return this.connection;
    }

    private final boolean isChunked(Response response) {
        return StringsKt__StringsJVMKt.equals("chunked", Response.header$default(response, HttpRequest.HEADER_TRANSFER_ENCODING, (String) null, 2, (Object) null), true);
    }

    private final boolean isChunked(Request request) {
        return StringsKt__StringsJVMKt.equals("chunked", request.header(HttpRequest.HEADER_TRANSFER_ENCODING), true);
    }

    @NotNull
    public Sink createRequestBody(@NotNull Request request, long j) {
        Intrinsics.checkNotNullParameter(request, "request");
        if (request.body() != null && request.body().isDuplex()) {
            throw new ProtocolException("Duplex connections are not supported for HTTP/1");
        } else if (isChunked(request)) {
            return newChunkedSink();
        } else {
            if (j != -1) {
                return newKnownLengthSink();
            }
            throw new IllegalStateException("Cannot stream a request body without chunked encoding or a known content length!");
        }
    }

    public void cancel() {
        getConnection().cancel();
    }

    public void writeRequestHeaders(@NotNull Request request) {
        Intrinsics.checkNotNullParameter(request, "request");
        RequestLine requestLine = RequestLine.INSTANCE;
        Proxy.Type type = getConnection().route().proxy().type();
        Intrinsics.checkNotNullExpressionValue(type, "connection.route().proxy.type()");
        writeRequest(request.headers(), requestLine.get(request, type));
    }

    public long reportedContentLength(@NotNull Response response) {
        Intrinsics.checkNotNullParameter(response, "response");
        if (!HttpHeaders.promisesBody(response)) {
            return 0;
        }
        if (isChunked(response)) {
            return -1;
        }
        return Util.headersContentLength(response);
    }

    @NotNull
    public Source openResponseBodySource(@NotNull Response response) {
        Intrinsics.checkNotNullParameter(response, "response");
        if (!HttpHeaders.promisesBody(response)) {
            return newFixedLengthSource(0);
        }
        if (isChunked(response)) {
            return newChunkedSource(response.request().url());
        }
        long headersContentLength = Util.headersContentLength(response);
        if (headersContentLength != -1) {
            return newFixedLengthSource(headersContentLength);
        }
        return newUnknownLengthSource();
    }

    public void flushRequest() {
        this.sink.flush();
    }

    public void finishRequest() {
        this.sink.flush();
    }

    public final void writeRequest(@NotNull Headers headers, @NotNull String str) {
        Intrinsics.checkNotNullParameter(headers, "headers");
        Intrinsics.checkNotNullParameter(str, "requestLine");
        int i = this.state;
        if (i == 0) {
            this.sink.writeUtf8(str).writeUtf8("\r\n");
            int size = headers.size();
            for (int i2 = 0; i2 < size; i2++) {
                this.sink.writeUtf8(headers.name(i2)).writeUtf8(": ").writeUtf8(headers.value(i2)).writeUtf8("\r\n");
            }
            this.sink.writeUtf8("\r\n");
            this.state = 1;
            return;
        }
        throw new IllegalStateException(Intrinsics.stringPlus("state: ", Integer.valueOf(i)).toString());
    }

    @Nullable
    public Response.Builder readResponseHeaders(boolean z) {
        int i = this.state;
        boolean z2 = true;
        if (!(i == 1 || i == 3)) {
            z2 = false;
        }
        if (z2) {
            try {
                StatusLine parse = StatusLine.Companion.parse(this.headersReader.readLine());
                Response.Builder headers = new Response.Builder().protocol(parse.protocol).code(parse.code).message(parse.message).headers(this.headersReader.readHeaders());
                if (z && parse.code == 100) {
                    return null;
                }
                if (parse.code == 100) {
                    this.state = 3;
                    return headers;
                }
                this.state = 4;
                return headers;
            } catch (EOFException e) {
                throw new IOException(Intrinsics.stringPlus("unexpected end of stream on ", getConnection().route().address().url().redact()), e);
            }
        } else {
            throw new IllegalStateException(Intrinsics.stringPlus("state: ", Integer.valueOf(i)).toString());
        }
    }

    private final Sink newChunkedSink() {
        int i = this.state;
        boolean z = true;
        if (i != 1) {
            z = false;
        }
        if (z) {
            this.state = 2;
            return new ChunkedSink(this);
        }
        throw new IllegalStateException(Intrinsics.stringPlus("state: ", Integer.valueOf(i)).toString());
    }

    private final Sink newKnownLengthSink() {
        int i = this.state;
        boolean z = true;
        if (i != 1) {
            z = false;
        }
        if (z) {
            this.state = 2;
            return new KnownLengthSink(this);
        }
        throw new IllegalStateException(Intrinsics.stringPlus("state: ", Integer.valueOf(i)).toString());
    }

    private final Source newFixedLengthSource(long j) {
        int i = this.state;
        if (i == 4) {
            this.state = 5;
            return new FixedLengthSource(this, j);
        }
        throw new IllegalStateException(Intrinsics.stringPlus("state: ", Integer.valueOf(i)).toString());
    }

    private final Source newChunkedSource(HttpUrl httpUrl) {
        int i = this.state;
        if (i == 4) {
            this.state = 5;
            return new ChunkedSource(this, httpUrl);
        }
        throw new IllegalStateException(Intrinsics.stringPlus("state: ", Integer.valueOf(i)).toString());
    }

    private final Source newUnknownLengthSource() {
        int i = this.state;
        if (i == 4) {
            this.state = 5;
            getConnection().noNewExchanges$okhttp();
            return new UnknownLengthSource(this);
        }
        throw new IllegalStateException(Intrinsics.stringPlus("state: ", Integer.valueOf(i)).toString());
    }

    /* access modifiers changed from: private */
    public final void detachTimeout(ForwardingTimeout forwardingTimeout) {
        Timeout delegate = forwardingTimeout.delegate();
        forwardingTimeout.setDelegate(Timeout.NONE);
        delegate.clearDeadline();
        delegate.clearTimeout();
    }

    public final void skipConnectBody(@NotNull Response response) {
        Intrinsics.checkNotNullParameter(response, "response");
        long headersContentLength = Util.headersContentLength(response);
        if (headersContentLength != -1) {
            Source newFixedLengthSource = newFixedLengthSource(headersContentLength);
            Util.skipAll(newFixedLengthSource, Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
            newFixedLengthSource.close();
        }
    }

    /* compiled from: Http1ExchangeCodec.kt */
    private final class KnownLengthSink implements Sink {
        private boolean closed;
        final /* synthetic */ Http1ExchangeCodec this$0;
        @NotNull
        private final ForwardingTimeout timeout;

        public KnownLengthSink(Http1ExchangeCodec http1ExchangeCodec) {
            Intrinsics.checkNotNullParameter(http1ExchangeCodec, "this$0");
            this.this$0 = http1ExchangeCodec;
            this.timeout = new ForwardingTimeout(http1ExchangeCodec.sink.timeout());
        }

        @NotNull
        public Timeout timeout() {
            return this.timeout;
        }

        public void write(@NotNull Buffer buffer, long j) {
            Intrinsics.checkNotNullParameter(buffer, "source");
            if (!this.closed) {
                Util.checkOffsetAndCount(buffer.size(), 0, j);
                this.this$0.sink.write(buffer, j);
                return;
            }
            throw new IllegalStateException("closed".toString());
        }

        public void flush() {
            if (!this.closed) {
                this.this$0.sink.flush();
            }
        }

        public void close() {
            if (!this.closed) {
                this.closed = true;
                this.this$0.detachTimeout(this.timeout);
                this.this$0.state = 3;
            }
        }
    }

    /* compiled from: Http1ExchangeCodec.kt */
    private final class ChunkedSink implements Sink {
        private boolean closed;
        final /* synthetic */ Http1ExchangeCodec this$0;
        @NotNull
        private final ForwardingTimeout timeout;

        public ChunkedSink(Http1ExchangeCodec http1ExchangeCodec) {
            Intrinsics.checkNotNullParameter(http1ExchangeCodec, "this$0");
            this.this$0 = http1ExchangeCodec;
            this.timeout = new ForwardingTimeout(http1ExchangeCodec.sink.timeout());
        }

        @NotNull
        public Timeout timeout() {
            return this.timeout;
        }

        public void write(@NotNull Buffer buffer, long j) {
            Intrinsics.checkNotNullParameter(buffer, "source");
            if (!(!this.closed)) {
                throw new IllegalStateException("closed".toString());
            } else if (j != 0) {
                this.this$0.sink.writeHexadecimalUnsignedLong(j);
                this.this$0.sink.writeUtf8("\r\n");
                this.this$0.sink.write(buffer, j);
                this.this$0.sink.writeUtf8("\r\n");
            }
        }

        public synchronized void flush() {
            if (!this.closed) {
                this.this$0.sink.flush();
            }
        }

        public synchronized void close() {
            if (!this.closed) {
                this.closed = true;
                this.this$0.sink.writeUtf8("0\r\n\r\n");
                this.this$0.detachTimeout(this.timeout);
                this.this$0.state = 3;
            }
        }
    }

    /* compiled from: Http1ExchangeCodec.kt */
    private abstract class AbstractSource implements Source {
        private boolean closed;
        final /* synthetic */ Http1ExchangeCodec this$0;
        @NotNull
        private final ForwardingTimeout timeout;

        public AbstractSource(Http1ExchangeCodec http1ExchangeCodec) {
            Intrinsics.checkNotNullParameter(http1ExchangeCodec, "this$0");
            this.this$0 = http1ExchangeCodec;
            this.timeout = new ForwardingTimeout(http1ExchangeCodec.source.timeout());
        }

        /* access modifiers changed from: protected */
        public final boolean getClosed() {
            return this.closed;
        }

        /* access modifiers changed from: protected */
        public final void setClosed(boolean z) {
            this.closed = z;
        }

        @NotNull
        public Timeout timeout() {
            return this.timeout;
        }

        public long read(@NotNull Buffer buffer, long j) {
            Intrinsics.checkNotNullParameter(buffer, "sink");
            try {
                return this.this$0.source.read(buffer, j);
            } catch (IOException e) {
                this.this$0.getConnection().noNewExchanges$okhttp();
                responseBodyComplete();
                throw e;
            }
        }

        public final void responseBodyComplete() {
            if (this.this$0.state != 6) {
                if (this.this$0.state == 5) {
                    this.this$0.detachTimeout(this.timeout);
                    this.this$0.state = 6;
                    return;
                }
                throw new IllegalStateException(Intrinsics.stringPlus("state: ", Integer.valueOf(this.this$0.state)));
            }
        }
    }

    /* compiled from: Http1ExchangeCodec.kt */
    private final class FixedLengthSource extends AbstractSource {
        private long bytesRemaining;
        final /* synthetic */ Http1ExchangeCodec this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public FixedLengthSource(Http1ExchangeCodec http1ExchangeCodec, long j) {
            super(http1ExchangeCodec);
            Intrinsics.checkNotNullParameter(http1ExchangeCodec, "this$0");
            this.this$0 = http1ExchangeCodec;
            this.bytesRemaining = j;
            if (j == 0) {
                responseBodyComplete();
            }
        }

        public long read(@NotNull Buffer buffer, long j) {
            Intrinsics.checkNotNullParameter(buffer, "sink");
            if (!(j >= 0)) {
                throw new IllegalArgumentException(Intrinsics.stringPlus("byteCount < 0: ", Long.valueOf(j)).toString());
            } else if (!getClosed()) {
                long j2 = this.bytesRemaining;
                if (j2 == 0) {
                    return -1;
                }
                long read = super.read(buffer, Math.min(j2, j));
                if (read != -1) {
                    long j3 = this.bytesRemaining - read;
                    this.bytesRemaining = j3;
                    if (j3 == 0) {
                        responseBodyComplete();
                    }
                    return read;
                }
                this.this$0.getConnection().noNewExchanges$okhttp();
                ProtocolException protocolException = new ProtocolException("unexpected end of stream");
                responseBodyComplete();
                throw protocolException;
            } else {
                throw new IllegalStateException("closed".toString());
            }
        }

        public void close() {
            if (!getClosed()) {
                if (this.bytesRemaining != 0 && !Util.discard(this, 100, TimeUnit.MILLISECONDS)) {
                    this.this$0.getConnection().noNewExchanges$okhttp();
                    responseBodyComplete();
                }
                setClosed(true);
            }
        }
    }

    /* compiled from: Http1ExchangeCodec.kt */
    private final class ChunkedSource extends AbstractSource {
        private long bytesRemainingInChunk = -1;
        private boolean hasMoreChunks = true;
        final /* synthetic */ Http1ExchangeCodec this$0;
        @NotNull
        private final HttpUrl url;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public ChunkedSource(@NotNull Http1ExchangeCodec http1ExchangeCodec, HttpUrl httpUrl) {
            super(http1ExchangeCodec);
            Intrinsics.checkNotNullParameter(http1ExchangeCodec, "this$0");
            Intrinsics.checkNotNullParameter(httpUrl, ImsConstants.FtDlParams.FT_DL_URL);
            this.this$0 = http1ExchangeCodec;
            this.url = httpUrl;
        }

        public long read(@NotNull Buffer buffer, long j) {
            Intrinsics.checkNotNullParameter(buffer, "sink");
            if (!(j >= 0)) {
                throw new IllegalArgumentException(Intrinsics.stringPlus("byteCount < 0: ", Long.valueOf(j)).toString());
            } else if (!(!getClosed())) {
                throw new IllegalStateException("closed".toString());
            } else if (!this.hasMoreChunks) {
                return -1;
            } else {
                long j2 = this.bytesRemainingInChunk;
                if (j2 == 0 || j2 == -1) {
                    readChunkSize();
                    if (!this.hasMoreChunks) {
                        return -1;
                    }
                }
                long read = super.read(buffer, Math.min(j, this.bytesRemainingInChunk));
                if (read != -1) {
                    this.bytesRemainingInChunk -= read;
                    return read;
                }
                this.this$0.getConnection().noNewExchanges$okhttp();
                ProtocolException protocolException = new ProtocolException("unexpected end of stream");
                responseBodyComplete();
                throw protocolException;
            }
        }

        private final void readChunkSize() {
            if (this.bytesRemainingInChunk != -1) {
                this.this$0.source.readUtf8LineStrict();
            }
            try {
                this.bytesRemainingInChunk = this.this$0.source.readHexadecimalUnsignedLong();
                String obj = StringsKt__StringsKt.trim(this.this$0.source.readUtf8LineStrict()).toString();
                if (this.bytesRemainingInChunk >= 0) {
                    if (!(obj.length() > 0) || StringsKt__StringsJVMKt.startsWith$default(obj, ";", false, 2, (Object) null)) {
                        if (this.bytesRemainingInChunk == 0) {
                            this.hasMoreChunks = false;
                            Http1ExchangeCodec http1ExchangeCodec = this.this$0;
                            http1ExchangeCodec.trailers = http1ExchangeCodec.headersReader.readHeaders();
                            OkHttpClient access$getClient$p = this.this$0.client;
                            Intrinsics.checkNotNull(access$getClient$p);
                            CookieJar cookieJar = access$getClient$p.cookieJar();
                            HttpUrl httpUrl = this.url;
                            Headers access$getTrailers$p = this.this$0.trailers;
                            Intrinsics.checkNotNull(access$getTrailers$p);
                            HttpHeaders.receiveHeaders(cookieJar, httpUrl, access$getTrailers$p);
                            responseBodyComplete();
                            return;
                        }
                        return;
                    }
                }
                throw new ProtocolException("expected chunk size and optional extensions but was \"" + this.bytesRemainingInChunk + obj + '\"');
            } catch (NumberFormatException e) {
                throw new ProtocolException(e.getMessage());
            }
        }

        public void close() {
            if (!getClosed()) {
                if (this.hasMoreChunks && !Util.discard(this, 100, TimeUnit.MILLISECONDS)) {
                    this.this$0.getConnection().noNewExchanges$okhttp();
                    responseBodyComplete();
                }
                setClosed(true);
            }
        }
    }

    /* compiled from: Http1ExchangeCodec.kt */
    private final class UnknownLengthSource extends AbstractSource {
        private boolean inputExhausted;
        final /* synthetic */ Http1ExchangeCodec this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public UnknownLengthSource(Http1ExchangeCodec http1ExchangeCodec) {
            super(http1ExchangeCodec);
            Intrinsics.checkNotNullParameter(http1ExchangeCodec, "this$0");
            this.this$0 = http1ExchangeCodec;
        }

        public long read(@NotNull Buffer buffer, long j) {
            Intrinsics.checkNotNullParameter(buffer, "sink");
            if (!(j >= 0)) {
                throw new IllegalArgumentException(Intrinsics.stringPlus("byteCount < 0: ", Long.valueOf(j)).toString());
            } else if (!(!getClosed())) {
                throw new IllegalStateException("closed".toString());
            } else if (this.inputExhausted) {
                return -1;
            } else {
                long read = super.read(buffer, j);
                if (read != -1) {
                    return read;
                }
                this.inputExhausted = true;
                responseBodyComplete();
                return -1;
            }
        }

        public void close() {
            if (!getClosed()) {
                if (!this.inputExhausted) {
                    responseBodyComplete();
                }
                setClosed(true);
            }
        }
    }

    /* compiled from: Http1ExchangeCodec.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }
}
