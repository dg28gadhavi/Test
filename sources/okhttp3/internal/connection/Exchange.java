package okhttp3.internal.connection;

import java.io.IOException;
import java.net.ProtocolException;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.EventListener;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.ExchangeCodec;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;
import okio.ForwardingSink;
import okio.ForwardingSource;
import okio.Okio;
import okio.Sink;
import okio.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Exchange.kt */
public final class Exchange {
    @NotNull
    private final RealCall call;
    @NotNull
    private final ExchangeCodec codec;
    @NotNull
    private final RealConnection connection;
    @NotNull
    private final EventListener eventListener;
    @NotNull
    private final ExchangeFinder finder;
    private boolean isDuplex;

    public Exchange(@NotNull RealCall realCall, @NotNull EventListener eventListener2, @NotNull ExchangeFinder exchangeFinder, @NotNull ExchangeCodec exchangeCodec) {
        Intrinsics.checkNotNullParameter(realCall, "call");
        Intrinsics.checkNotNullParameter(eventListener2, "eventListener");
        Intrinsics.checkNotNullParameter(exchangeFinder, "finder");
        Intrinsics.checkNotNullParameter(exchangeCodec, "codec");
        this.call = realCall;
        this.eventListener = eventListener2;
        this.finder = exchangeFinder;
        this.codec = exchangeCodec;
        this.connection = exchangeCodec.getConnection();
    }

    @NotNull
    public final RealCall getCall$okhttp() {
        return this.call;
    }

    @NotNull
    public final EventListener getEventListener$okhttp() {
        return this.eventListener;
    }

    @NotNull
    public final ExchangeFinder getFinder$okhttp() {
        return this.finder;
    }

    public final boolean isDuplex$okhttp() {
        return this.isDuplex;
    }

    @NotNull
    public final RealConnection getConnection$okhttp() {
        return this.connection;
    }

    public final boolean isCoalescedConnection$okhttp() {
        return !Intrinsics.areEqual(this.finder.getAddress$okhttp().url().host(), this.connection.route().address().url().host());
    }

    public final void writeRequestHeaders(@NotNull Request request) throws IOException {
        Intrinsics.checkNotNullParameter(request, "request");
        try {
            this.eventListener.requestHeadersStart(this.call);
            this.codec.writeRequestHeaders(request);
            this.eventListener.requestHeadersEnd(this.call, request);
        } catch (IOException e) {
            this.eventListener.requestFailed(this.call, e);
            trackFailure(e);
            throw e;
        }
    }

    @NotNull
    public final Sink createRequestBody(@NotNull Request request, boolean z) throws IOException {
        Intrinsics.checkNotNullParameter(request, "request");
        this.isDuplex = z;
        RequestBody body = request.body();
        Intrinsics.checkNotNull(body);
        long contentLength = body.contentLength();
        this.eventListener.requestBodyStart(this.call);
        return new RequestBodySink(this, this.codec.createRequestBody(request, contentLength), contentLength);
    }

    public final void flushRequest() throws IOException {
        try {
            this.codec.flushRequest();
        } catch (IOException e) {
            this.eventListener.requestFailed(this.call, e);
            trackFailure(e);
            throw e;
        }
    }

    public final void finishRequest() throws IOException {
        try {
            this.codec.finishRequest();
        } catch (IOException e) {
            this.eventListener.requestFailed(this.call, e);
            trackFailure(e);
            throw e;
        }
    }

    public final void responseHeadersStart() {
        this.eventListener.responseHeadersStart(this.call);
    }

    @Nullable
    public final Response.Builder readResponseHeaders(boolean z) throws IOException {
        try {
            Response.Builder readResponseHeaders = this.codec.readResponseHeaders(z);
            if (readResponseHeaders != null) {
                readResponseHeaders.initExchange$okhttp(this);
            }
            return readResponseHeaders;
        } catch (IOException e) {
            this.eventListener.responseFailed(this.call, e);
            trackFailure(e);
            throw e;
        }
    }

    public final void responseHeadersEnd(@NotNull Response response) {
        Intrinsics.checkNotNullParameter(response, "response");
        this.eventListener.responseHeadersEnd(this.call, response);
    }

    @NotNull
    public final ResponseBody openResponseBody(@NotNull Response response) throws IOException {
        Intrinsics.checkNotNullParameter(response, "response");
        try {
            String header$default = Response.header$default(response, "Content-Type", (String) null, 2, (Object) null);
            long reportedContentLength = this.codec.reportedContentLength(response);
            return new RealResponseBody(header$default, reportedContentLength, Okio.buffer((Source) new ResponseBodySource(this, this.codec.openResponseBodySource(response), reportedContentLength)));
        } catch (IOException e) {
            this.eventListener.responseFailed(this.call, e);
            trackFailure(e);
            throw e;
        }
    }

    public final void noNewExchangesOnConnection() {
        this.codec.getConnection().noNewExchanges$okhttp();
    }

    public final void cancel() {
        this.codec.cancel();
    }

    public final void detachWithViolence() {
        this.codec.cancel();
        this.call.messageDone$okhttp(this, true, true, null);
    }

    private final void trackFailure(IOException iOException) {
        this.finder.trackFailure(iOException);
        this.codec.getConnection().trackFailure$okhttp(this.call, iOException);
    }

    public final <E extends IOException> E bodyComplete(long j, boolean z, boolean z2, E e) {
        if (e != null) {
            trackFailure(e);
        }
        if (z2) {
            if (e != null) {
                this.eventListener.requestFailed(this.call, e);
            } else {
                this.eventListener.requestBodyEnd(this.call, j);
            }
        }
        if (z) {
            if (e != null) {
                this.eventListener.responseFailed(this.call, e);
            } else {
                this.eventListener.responseBodyEnd(this.call, j);
            }
        }
        return this.call.messageDone$okhttp(this, z2, z, e);
    }

    public final void noRequestBody() {
        this.call.messageDone$okhttp(this, true, false, null);
    }

    /* compiled from: Exchange.kt */
    private final class RequestBodySink extends ForwardingSink {
        private long bytesReceived;
        private boolean closed;
        private boolean completed;
        private final long contentLength;
        final /* synthetic */ Exchange this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public RequestBodySink(@NotNull Exchange exchange, Sink sink, long j) {
            super(sink);
            Intrinsics.checkNotNullParameter(exchange, "this$0");
            Intrinsics.checkNotNullParameter(sink, "delegate");
            this.this$0 = exchange;
            this.contentLength = j;
        }

        public void write(@NotNull Buffer buffer, long j) throws IOException {
            Intrinsics.checkNotNullParameter(buffer, "source");
            if (!this.closed) {
                long j2 = this.contentLength;
                if (j2 == -1 || this.bytesReceived + j <= j2) {
                    try {
                        super.write(buffer, j);
                        this.bytesReceived += j;
                    } catch (IOException e) {
                        throw complete(e);
                    }
                } else {
                    throw new ProtocolException("expected " + this.contentLength + " bytes but received " + (this.bytesReceived + j));
                }
            } else {
                throw new IllegalStateException("closed".toString());
            }
        }

        public void flush() throws IOException {
            try {
                super.flush();
            } catch (IOException e) {
                throw complete(e);
            }
        }

        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                long j = this.contentLength;
                if (j == -1 || this.bytesReceived == j) {
                    try {
                        super.close();
                        complete((IOException) null);
                    } catch (IOException e) {
                        throw complete(e);
                    }
                } else {
                    throw new ProtocolException("unexpected end of stream");
                }
            }
        }

        private final <E extends IOException> E complete(E e) {
            if (this.completed) {
                return e;
            }
            this.completed = true;
            return this.this$0.bodyComplete(this.bytesReceived, false, true, e);
        }
    }

    /* compiled from: Exchange.kt */
    public final class ResponseBodySource extends ForwardingSource {
        private long bytesReceived;
        private boolean closed;
        private boolean completed;
        private final long contentLength;
        private boolean invokeStartEvent = true;
        final /* synthetic */ Exchange this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public ResponseBodySource(@NotNull Exchange exchange, Source source, long j) {
            super(source);
            Intrinsics.checkNotNullParameter(exchange, "this$0");
            Intrinsics.checkNotNullParameter(source, "delegate");
            this.this$0 = exchange;
            this.contentLength = j;
            if (j == 0) {
                complete((IOException) null);
            }
        }

        public long read(@NotNull Buffer buffer, long j) throws IOException {
            Intrinsics.checkNotNullParameter(buffer, "sink");
            if (!this.closed) {
                try {
                    long read = delegate().read(buffer, j);
                    if (this.invokeStartEvent) {
                        this.invokeStartEvent = false;
                        this.this$0.getEventListener$okhttp().responseBodyStart(this.this$0.getCall$okhttp());
                    }
                    if (read == -1) {
                        complete((IOException) null);
                        return -1;
                    }
                    long j2 = this.bytesReceived + read;
                    long j3 = this.contentLength;
                    if (j3 != -1) {
                        if (j2 > j3) {
                            throw new ProtocolException("expected " + this.contentLength + " bytes but received " + j2);
                        }
                    }
                    this.bytesReceived = j2;
                    if (j2 == j3) {
                        complete((IOException) null);
                    }
                    return read;
                } catch (IOException e) {
                    throw complete(e);
                }
            } else {
                throw new IllegalStateException("closed".toString());
            }
        }

        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                try {
                    super.close();
                    complete((IOException) null);
                } catch (IOException e) {
                    throw complete(e);
                }
            }
        }

        public final <E extends IOException> E complete(E e) {
            if (this.completed) {
                return e;
            }
            this.completed = true;
            if (e == null && this.invokeStartEvent) {
                this.invokeStartEvent = false;
                this.this$0.getEventListener$okhttp().responseBodyStart(this.this$0.getCall$okhttp());
            }
            return this.this$0.bodyComplete(this.bytesReceived, true, false, e);
        }
    }
}
