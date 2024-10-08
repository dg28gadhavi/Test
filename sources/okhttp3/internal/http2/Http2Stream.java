package okhttp3.internal.http2;

import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import kotlin.Unit;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Headers;
import okhttp3.internal.Util;
import okio.AsyncTimeout;
import okio.Buffer;
import okio.BufferedSource;
import okio.Sink;
import okio.Source;
import okio.Timeout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Http2Stream.kt */
public final class Http2Stream {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    @NotNull
    private final Http2Connection connection;
    @Nullable
    private ErrorCode errorCode;
    @Nullable
    private IOException errorException;
    private boolean hasResponseHeaders;
    @NotNull
    private final ArrayDeque<Headers> headersQueue;
    private final int id;
    private long readBytesAcknowledged;
    private long readBytesTotal;
    @NotNull
    private final StreamTimeout readTimeout = new StreamTimeout(this);
    @NotNull
    private final FramingSink sink;
    @NotNull
    private final FramingSource source;
    private long writeBytesMaximum;
    private long writeBytesTotal;
    @NotNull
    private final StreamTimeout writeTimeout = new StreamTimeout(this);

    public Http2Stream(int i, @NotNull Http2Connection http2Connection, boolean z, boolean z2, @Nullable Headers headers) {
        Intrinsics.checkNotNullParameter(http2Connection, "connection");
        this.id = i;
        this.connection = http2Connection;
        this.writeBytesMaximum = (long) http2Connection.getPeerSettings().getInitialWindowSize();
        ArrayDeque<Headers> arrayDeque = new ArrayDeque<>();
        this.headersQueue = arrayDeque;
        this.source = new FramingSource(this, (long) http2Connection.getOkHttpSettings().getInitialWindowSize(), z2);
        this.sink = new FramingSink(this, z);
        if (headers != null) {
            if (!isLocallyInitiated()) {
                arrayDeque.add(headers);
                return;
            }
            throw new IllegalStateException("locally-initiated streams shouldn't have headers yet".toString());
        } else if (!isLocallyInitiated()) {
            throw new IllegalStateException("remotely-initiated streams should have headers".toString());
        }
    }

    public final int getId() {
        return this.id;
    }

    @NotNull
    public final Http2Connection getConnection() {
        return this.connection;
    }

    public final long getReadBytesTotal() {
        return this.readBytesTotal;
    }

    public final void setReadBytesTotal$okhttp(long j) {
        this.readBytesTotal = j;
    }

    public final long getReadBytesAcknowledged() {
        return this.readBytesAcknowledged;
    }

    public final void setReadBytesAcknowledged$okhttp(long j) {
        this.readBytesAcknowledged = j;
    }

    public final long getWriteBytesTotal() {
        return this.writeBytesTotal;
    }

    public final void setWriteBytesTotal$okhttp(long j) {
        this.writeBytesTotal = j;
    }

    public final long getWriteBytesMaximum() {
        return this.writeBytesMaximum;
    }

    @NotNull
    public final FramingSource getSource$okhttp() {
        return this.source;
    }

    @NotNull
    public final FramingSink getSink$okhttp() {
        return this.sink;
    }

    @NotNull
    public final StreamTimeout getReadTimeout$okhttp() {
        return this.readTimeout;
    }

    @NotNull
    public final StreamTimeout getWriteTimeout$okhttp() {
        return this.writeTimeout;
    }

    @Nullable
    public final synchronized ErrorCode getErrorCode$okhttp() {
        return this.errorCode;
    }

    public final void setErrorCode$okhttp(@Nullable ErrorCode errorCode2) {
        this.errorCode = errorCode2;
    }

    @Nullable
    public final IOException getErrorException$okhttp() {
        return this.errorException;
    }

    public final void setErrorException$okhttp(@Nullable IOException iOException) {
        this.errorException = iOException;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002f, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final synchronized boolean isOpen() {
        /*
            r2 = this;
            monitor-enter(r2)
            okhttp3.internal.http2.ErrorCode r0 = r2.errorCode     // Catch:{ all -> 0x0031 }
            r1 = 0
            if (r0 == 0) goto L_0x0008
            monitor-exit(r2)
            return r1
        L_0x0008:
            okhttp3.internal.http2.Http2Stream$FramingSource r0 = r2.source     // Catch:{ all -> 0x0031 }
            boolean r0 = r0.getFinished$okhttp()     // Catch:{ all -> 0x0031 }
            if (r0 != 0) goto L_0x0018
            okhttp3.internal.http2.Http2Stream$FramingSource r0 = r2.source     // Catch:{ all -> 0x0031 }
            boolean r0 = r0.getClosed$okhttp()     // Catch:{ all -> 0x0031 }
            if (r0 == 0) goto L_0x002e
        L_0x0018:
            okhttp3.internal.http2.Http2Stream$FramingSink r0 = r2.sink     // Catch:{ all -> 0x0031 }
            boolean r0 = r0.getFinished()     // Catch:{ all -> 0x0031 }
            if (r0 != 0) goto L_0x0028
            okhttp3.internal.http2.Http2Stream$FramingSink r0 = r2.sink     // Catch:{ all -> 0x0031 }
            boolean r0 = r0.getClosed()     // Catch:{ all -> 0x0031 }
            if (r0 == 0) goto L_0x002e
        L_0x0028:
            boolean r0 = r2.hasResponseHeaders     // Catch:{ all -> 0x0031 }
            if (r0 == 0) goto L_0x002e
            monitor-exit(r2)
            return r1
        L_0x002e:
            monitor-exit(r2)
            r2 = 1
            return r2
        L_0x0031:
            r0 = move-exception
            monitor-exit(r2)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.http2.Http2Stream.isOpen():boolean");
    }

    public final boolean isLocallyInitiated() {
        if (this.connection.getClient$okhttp() == ((this.id & 1) == 1)) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0043, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0044, code lost:
        r2.readTimeout.exitAndThrowIfTimedOut();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0049, code lost:
        throw r0;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    @org.jetbrains.annotations.NotNull
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final synchronized okhttp3.Headers takeHeaders() throws java.io.IOException {
        /*
            r2 = this;
            monitor-enter(r2)
            okhttp3.internal.http2.Http2Stream$StreamTimeout r0 = r2.readTimeout     // Catch:{ all -> 0x004a }
            r0.enter()     // Catch:{ all -> 0x004a }
        L_0x0006:
            java.util.ArrayDeque<okhttp3.Headers> r0 = r2.headersQueue     // Catch:{ all -> 0x0043 }
            boolean r0 = r0.isEmpty()     // Catch:{ all -> 0x0043 }
            if (r0 == 0) goto L_0x0016
            okhttp3.internal.http2.ErrorCode r0 = r2.errorCode     // Catch:{ all -> 0x0043 }
            if (r0 != 0) goto L_0x0016
            r2.waitForIo$okhttp()     // Catch:{ all -> 0x0043 }
            goto L_0x0006
        L_0x0016:
            okhttp3.internal.http2.Http2Stream$StreamTimeout r0 = r2.readTimeout     // Catch:{ all -> 0x004a }
            r0.exitAndThrowIfTimedOut()     // Catch:{ all -> 0x004a }
            java.util.ArrayDeque<okhttp3.Headers> r0 = r2.headersQueue     // Catch:{ all -> 0x004a }
            boolean r0 = r0.isEmpty()     // Catch:{ all -> 0x004a }
            r0 = r0 ^ 1
            if (r0 == 0) goto L_0x0034
            java.util.ArrayDeque<okhttp3.Headers> r0 = r2.headersQueue     // Catch:{ all -> 0x004a }
            java.lang.Object r0 = r0.removeFirst()     // Catch:{ all -> 0x004a }
            java.lang.String r1 = "headersQueue.removeFirst()"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r0, r1)     // Catch:{ all -> 0x004a }
            okhttp3.Headers r0 = (okhttp3.Headers) r0     // Catch:{ all -> 0x004a }
            monitor-exit(r2)
            return r0
        L_0x0034:
            java.io.IOException r0 = r2.errorException     // Catch:{ all -> 0x004a }
            if (r0 != 0) goto L_0x0042
            okhttp3.internal.http2.StreamResetException r0 = new okhttp3.internal.http2.StreamResetException     // Catch:{ all -> 0x004a }
            okhttp3.internal.http2.ErrorCode r1 = r2.errorCode     // Catch:{ all -> 0x004a }
            kotlin.jvm.internal.Intrinsics.checkNotNull(r1)     // Catch:{ all -> 0x004a }
            r0.<init>(r1)     // Catch:{ all -> 0x004a }
        L_0x0042:
            throw r0     // Catch:{ all -> 0x004a }
        L_0x0043:
            r0 = move-exception
            okhttp3.internal.http2.Http2Stream$StreamTimeout r1 = r2.readTimeout     // Catch:{ all -> 0x004a }
            r1.exitAndThrowIfTimedOut()     // Catch:{ all -> 0x004a }
            throw r0     // Catch:{ all -> 0x004a }
        L_0x004a:
            r0 = move-exception
            monitor-exit(r2)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.http2.Http2Stream.takeHeaders():okhttp3.Headers");
    }

    @NotNull
    public final Timeout readTimeout() {
        return this.readTimeout;
    }

    @NotNull
    public final Timeout writeTimeout() {
        return this.writeTimeout;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0011  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0017  */
    @org.jetbrains.annotations.NotNull
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final okio.Sink getSink() {
        /*
            r2 = this;
            monitor-enter(r2)
            boolean r0 = r2.hasResponseHeaders     // Catch:{ all -> 0x0024 }
            if (r0 != 0) goto L_0x000e
            boolean r0 = r2.isLocallyInitiated()     // Catch:{ all -> 0x0024 }
            if (r0 == 0) goto L_0x000c
            goto L_0x000e
        L_0x000c:
            r0 = 0
            goto L_0x000f
        L_0x000e:
            r0 = 1
        L_0x000f:
            if (r0 == 0) goto L_0x0017
            kotlin.Unit r0 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x0024 }
            monitor-exit(r2)
            okhttp3.internal.http2.Http2Stream$FramingSink r2 = r2.sink
            return r2
        L_0x0017:
            java.lang.String r0 = "reply before requesting the sink"
            java.lang.IllegalStateException r1 = new java.lang.IllegalStateException     // Catch:{ all -> 0x0024 }
            java.lang.String r0 = r0.toString()     // Catch:{ all -> 0x0024 }
            r1.<init>(r0)     // Catch:{ all -> 0x0024 }
            throw r1     // Catch:{ all -> 0x0024 }
        L_0x0024:
            r0 = move-exception
            monitor-exit(r2)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.http2.Http2Stream.getSink():okio.Sink");
    }

    public final void close(@NotNull ErrorCode errorCode2, @Nullable IOException iOException) throws IOException {
        Intrinsics.checkNotNullParameter(errorCode2, "rstStatusCode");
        if (closeInternal(errorCode2, iOException)) {
            this.connection.writeSynReset$okhttp(this.id, errorCode2);
        }
    }

    public final void closeLater(@NotNull ErrorCode errorCode2) {
        Intrinsics.checkNotNullParameter(errorCode2, "errorCode");
        if (closeInternal(errorCode2, (IOException) null)) {
            this.connection.writeSynResetLater$okhttp(this.id, errorCode2);
        }
    }

    public final synchronized void receiveRstStream(@NotNull ErrorCode errorCode2) {
        Intrinsics.checkNotNullParameter(errorCode2, "errorCode");
        if (this.errorCode == null) {
            this.errorCode = errorCode2;
            notifyAll();
        }
    }

    /* compiled from: Http2Stream.kt */
    public final class FramingSource implements Source {
        private boolean closed;
        private boolean finished;
        private final long maxByteCount;
        @NotNull
        private final Buffer readBuffer = new Buffer();
        @NotNull
        private final Buffer receiveBuffer = new Buffer();
        final /* synthetic */ Http2Stream this$0;
        @Nullable
        private Headers trailers;

        public FramingSource(Http2Stream http2Stream, long j, boolean z) {
            Intrinsics.checkNotNullParameter(http2Stream, "this$0");
            this.this$0 = http2Stream;
            this.maxByteCount = j;
            this.finished = z;
        }

        public final boolean getFinished$okhttp() {
            return this.finished;
        }

        public final void setFinished$okhttp(boolean z) {
            this.finished = z;
        }

        @NotNull
        public final Buffer getReceiveBuffer() {
            return this.receiveBuffer;
        }

        @NotNull
        public final Buffer getReadBuffer() {
            return this.readBuffer;
        }

        public final void setTrailers(@Nullable Headers headers) {
            this.trailers = headers;
        }

        public final boolean getClosed$okhttp() {
            return this.closed;
        }

        public final void setClosed$okhttp(boolean z) {
            this.closed = z;
        }

        /* JADX INFO: finally extract failed */
        public long read(@NotNull Buffer buffer, long j) throws IOException {
            Throwable th;
            long j2;
            boolean z;
            Buffer buffer2 = buffer;
            long j3 = j;
            Intrinsics.checkNotNullParameter(buffer2, "sink");
            long j4 = 0;
            if (j3 >= 0) {
                while (true) {
                    Http2Stream http2Stream = this.this$0;
                    synchronized (http2Stream) {
                        http2Stream.getReadTimeout$okhttp().enter();
                        try {
                            if (http2Stream.getErrorCode$okhttp() != null) {
                                th = http2Stream.getErrorException$okhttp();
                                if (th == null) {
                                    ErrorCode errorCode$okhttp = http2Stream.getErrorCode$okhttp();
                                    Intrinsics.checkNotNull(errorCode$okhttp);
                                    th = new StreamResetException(errorCode$okhttp);
                                }
                            } else {
                                th = null;
                            }
                            if (!getClosed$okhttp()) {
                                if (getReadBuffer().size() > j4) {
                                    j2 = getReadBuffer().read(buffer2, Math.min(j3, getReadBuffer().size()));
                                    http2Stream.setReadBytesTotal$okhttp(http2Stream.getReadBytesTotal() + j2);
                                    long readBytesTotal = http2Stream.getReadBytesTotal() - http2Stream.getReadBytesAcknowledged();
                                    if (th == null && readBytesTotal >= ((long) (http2Stream.getConnection().getOkHttpSettings().getInitialWindowSize() / 2))) {
                                        http2Stream.getConnection().writeWindowUpdateLater$okhttp(http2Stream.getId(), readBytesTotal);
                                        http2Stream.setReadBytesAcknowledged$okhttp(http2Stream.getReadBytesTotal());
                                    }
                                } else if (getFinished$okhttp() || th != null) {
                                    j2 = -1;
                                } else {
                                    http2Stream.waitForIo$okhttp();
                                    j2 = -1;
                                    z = true;
                                    http2Stream.getReadTimeout$okhttp().exitAndThrowIfTimedOut();
                                    Unit unit = Unit.INSTANCE;
                                }
                                z = false;
                                http2Stream.getReadTimeout$okhttp().exitAndThrowIfTimedOut();
                                Unit unit2 = Unit.INSTANCE;
                            } else {
                                throw new IOException("stream closed");
                            }
                        } catch (Throwable th2) {
                            http2Stream.getReadTimeout$okhttp().exitAndThrowIfTimedOut();
                            throw th2;
                        }
                    }
                    if (z) {
                        j4 = 0;
                    } else if (j2 != -1) {
                        updateConnectionFlowControl(j2);
                        return j2;
                    } else if (th == null) {
                        return -1;
                    } else {
                        throw th;
                    }
                }
            } else {
                throw new IllegalArgumentException(Intrinsics.stringPlus("byteCount < 0: ", Long.valueOf(j)).toString());
            }
        }

        private final void updateConnectionFlowControl(long j) {
            Http2Stream http2Stream = this.this$0;
            if (!Util.assertionsEnabled || !Thread.holdsLock(http2Stream)) {
                this.this$0.getConnection().updateConnectionFlowControl$okhttp(j);
                return;
            }
            throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST NOT hold lock on " + http2Stream);
        }

        public final void receive$okhttp(@NotNull BufferedSource bufferedSource, long j) throws IOException {
            boolean finished$okhttp;
            boolean z;
            boolean z2;
            long j2;
            Intrinsics.checkNotNullParameter(bufferedSource, "source");
            Http2Stream http2Stream = this.this$0;
            if (!Util.assertionsEnabled || !Thread.holdsLock(http2Stream)) {
                while (j > 0) {
                    synchronized (this.this$0) {
                        finished$okhttp = getFinished$okhttp();
                        z = true;
                        z2 = getReadBuffer().size() + j > this.maxByteCount;
                        Unit unit = Unit.INSTANCE;
                    }
                    if (z2) {
                        bufferedSource.skip(j);
                        this.this$0.closeLater(ErrorCode.FLOW_CONTROL_ERROR);
                        return;
                    } else if (finished$okhttp) {
                        bufferedSource.skip(j);
                        return;
                    } else {
                        long read = bufferedSource.read(this.receiveBuffer, j);
                        if (read != -1) {
                            j -= read;
                            Http2Stream http2Stream2 = this.this$0;
                            synchronized (http2Stream2) {
                                if (getClosed$okhttp()) {
                                    j2 = getReceiveBuffer().size();
                                    getReceiveBuffer().clear();
                                } else {
                                    if (getReadBuffer().size() != 0) {
                                        z = false;
                                    }
                                    getReadBuffer().writeAll(getReceiveBuffer());
                                    if (z) {
                                        http2Stream2.notifyAll();
                                    }
                                    j2 = 0;
                                }
                            }
                            if (j2 > 0) {
                                updateConnectionFlowControl(j2);
                            }
                        } else {
                            throw new EOFException();
                        }
                    }
                }
                return;
            }
            throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST NOT hold lock on " + http2Stream);
        }

        @NotNull
        public Timeout timeout() {
            return this.this$0.getReadTimeout$okhttp();
        }

        public void close() throws IOException {
            long size;
            Http2Stream http2Stream = this.this$0;
            synchronized (http2Stream) {
                setClosed$okhttp(true);
                size = getReadBuffer().size();
                getReadBuffer().clear();
                http2Stream.notifyAll();
                Unit unit = Unit.INSTANCE;
            }
            if (size > 0) {
                updateConnectionFlowControl(size);
            }
            this.this$0.cancelStreamIfNecessary$okhttp();
        }
    }

    /* compiled from: Http2Stream.kt */
    public final class FramingSink implements Sink {
        private boolean closed;
        private boolean finished;
        @NotNull
        private final Buffer sendBuffer = new Buffer();
        final /* synthetic */ Http2Stream this$0;
        @Nullable
        private Headers trailers;

        public FramingSink(Http2Stream http2Stream, boolean z) {
            Intrinsics.checkNotNullParameter(http2Stream, "this$0");
            this.this$0 = http2Stream;
            this.finished = z;
        }

        public final boolean getFinished() {
            return this.finished;
        }

        public final boolean getClosed() {
            return this.closed;
        }

        public final void setClosed(boolean z) {
            this.closed = z;
        }

        public void write(@NotNull Buffer buffer, long j) throws IOException {
            Intrinsics.checkNotNullParameter(buffer, "source");
            Http2Stream http2Stream = this.this$0;
            if (!Util.assertionsEnabled || !Thread.holdsLock(http2Stream)) {
                this.sendBuffer.write(buffer, j);
                while (this.sendBuffer.size() >= 16384) {
                    emitFrame(false);
                }
                return;
            }
            throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST NOT hold lock on " + http2Stream);
        }

        /* JADX INFO: finally extract failed */
        private final void emitFrame(boolean z) throws IOException {
            long min;
            boolean z2;
            Http2Stream http2Stream = this.this$0;
            synchronized (http2Stream) {
                http2Stream.getWriteTimeout$okhttp().enter();
                while (http2Stream.getWriteBytesTotal() >= http2Stream.getWriteBytesMaximum() && !getFinished() && !getClosed() && http2Stream.getErrorCode$okhttp() == null) {
                    try {
                        http2Stream.waitForIo$okhttp();
                    } catch (Throwable th) {
                        http2Stream.getWriteTimeout$okhttp().exitAndThrowIfTimedOut();
                        throw th;
                    }
                }
                http2Stream.getWriteTimeout$okhttp().exitAndThrowIfTimedOut();
                http2Stream.checkOutNotClosed$okhttp();
                min = Math.min(http2Stream.getWriteBytesMaximum() - http2Stream.getWriteBytesTotal(), this.sendBuffer.size());
                http2Stream.setWriteBytesTotal$okhttp(http2Stream.getWriteBytesTotal() + min);
                z2 = z && min == this.sendBuffer.size();
                Unit unit = Unit.INSTANCE;
            }
            this.this$0.getWriteTimeout$okhttp().enter();
            try {
                this.this$0.getConnection().writeData(this.this$0.getId(), z2, this.sendBuffer, min);
            } finally {
                this.this$0.getWriteTimeout$okhttp().exitAndThrowIfTimedOut();
            }
        }

        public void flush() throws IOException {
            Http2Stream http2Stream = this.this$0;
            if (!Util.assertionsEnabled || !Thread.holdsLock(http2Stream)) {
                Http2Stream http2Stream2 = this.this$0;
                synchronized (http2Stream2) {
                    http2Stream2.checkOutNotClosed$okhttp();
                    Unit unit = Unit.INSTANCE;
                }
                while (this.sendBuffer.size() > 0) {
                    emitFrame(false);
                    this.this$0.getConnection().flush();
                }
                return;
            }
            throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST NOT hold lock on " + http2Stream);
        }

        @NotNull
        public Timeout timeout() {
            return this.this$0.getWriteTimeout$okhttp();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0055, code lost:
            if (r10.this$0.getSink$okhttp().finished != false) goto L_0x00bb;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x0061, code lost:
            if (r10.sendBuffer.size() <= 0) goto L_0x0065;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0063, code lost:
            r0 = true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x0065, code lost:
            r0 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x0068, code lost:
            if (r10.trailers == null) goto L_0x006c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x006a, code lost:
            r4 = true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x006c, code lost:
            r4 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x006d, code lost:
            if (r4 == false) goto L_0x0096;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0077, code lost:
            if (r10.sendBuffer.size() <= 0) goto L_0x007d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0079, code lost:
            emitFrame(false);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x007d, code lost:
            r0 = r10.this$0.getConnection();
            r2 = r10.this$0.getId();
            r4 = r10.trailers;
            kotlin.jvm.internal.Intrinsics.checkNotNull(r4);
            r0.writeHeaders$okhttp(r2, r1, okhttp3.internal.Util.toHeaderList(r4));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x0096, code lost:
            if (r0 == false) goto L_0x00a6;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a0, code lost:
            if (r10.sendBuffer.size() <= 0) goto L_0x00bb;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a2, code lost:
            emitFrame(true);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a6, code lost:
            if (r1 == false) goto L_0x00bb;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a8, code lost:
            r10.this$0.getConnection().writeData(r10.this$0.getId(), true, (okio.Buffer) null, 0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x00bb, code lost:
            r0 = r10.this$0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x00bd, code lost:
            monitor-enter(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
            setClosed(true);
            r1 = kotlin.Unit.INSTANCE;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:0x00c3, code lost:
            monitor-exit(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x00c4, code lost:
            r10.this$0.getConnection().flush();
            r10.this$0.cancelStreamIfNecessary$okhttp();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:47:0x00d2, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void close() throws java.io.IOException {
            /*
                r10 = this;
                okhttp3.internal.http2.Http2Stream r0 = r10.this$0
                boolean r1 = okhttp3.internal.Util.assertionsEnabled
                if (r1 == 0) goto L_0x0034
                boolean r1 = java.lang.Thread.holdsLock(r0)
                if (r1 != 0) goto L_0x000d
                goto L_0x0034
            L_0x000d:
                java.lang.AssertionError r10 = new java.lang.AssertionError
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "Thread "
                r1.append(r2)
                java.lang.Thread r2 = java.lang.Thread.currentThread()
                java.lang.String r2 = r2.getName()
                r1.append(r2)
                java.lang.String r2 = " MUST NOT hold lock on "
                r1.append(r2)
                r1.append(r0)
                java.lang.String r0 = r1.toString()
                r10.<init>(r0)
                throw r10
            L_0x0034:
                okhttp3.internal.http2.Http2Stream r0 = r10.this$0
                monitor-enter(r0)
                boolean r1 = r10.getClosed()     // Catch:{ all -> 0x00d6 }
                if (r1 == 0) goto L_0x003f
                monitor-exit(r0)
                return
            L_0x003f:
                okhttp3.internal.http2.ErrorCode r1 = r0.getErrorCode$okhttp()     // Catch:{ all -> 0x00d6 }
                r2 = 0
                r3 = 1
                if (r1 != 0) goto L_0x0049
                r1 = r3
                goto L_0x004a
            L_0x0049:
                r1 = r2
            L_0x004a:
                kotlin.Unit r4 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x00d6 }
                monitor-exit(r0)
                okhttp3.internal.http2.Http2Stream r0 = r10.this$0
                okhttp3.internal.http2.Http2Stream$FramingSink r0 = r0.getSink$okhttp()
                boolean r0 = r0.finished
                if (r0 != 0) goto L_0x00bb
                okio.Buffer r0 = r10.sendBuffer
                long r4 = r0.size()
                r6 = 0
                int r0 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
                if (r0 <= 0) goto L_0x0065
                r0 = r3
                goto L_0x0066
            L_0x0065:
                r0 = r2
            L_0x0066:
                okhttp3.Headers r4 = r10.trailers
                if (r4 == 0) goto L_0x006c
                r4 = r3
                goto L_0x006d
            L_0x006c:
                r4 = r2
            L_0x006d:
                if (r4 == 0) goto L_0x0096
            L_0x006f:
                okio.Buffer r0 = r10.sendBuffer
                long r4 = r0.size()
                int r0 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
                if (r0 <= 0) goto L_0x007d
                r10.emitFrame(r2)
                goto L_0x006f
            L_0x007d:
                okhttp3.internal.http2.Http2Stream r0 = r10.this$0
                okhttp3.internal.http2.Http2Connection r0 = r0.getConnection()
                okhttp3.internal.http2.Http2Stream r2 = r10.this$0
                int r2 = r2.getId()
                okhttp3.Headers r4 = r10.trailers
                kotlin.jvm.internal.Intrinsics.checkNotNull(r4)
                java.util.List r4 = okhttp3.internal.Util.toHeaderList(r4)
                r0.writeHeaders$okhttp(r2, r1, r4)
                goto L_0x00bb
            L_0x0096:
                if (r0 == 0) goto L_0x00a6
            L_0x0098:
                okio.Buffer r0 = r10.sendBuffer
                long r0 = r0.size()
                int r0 = (r0 > r6 ? 1 : (r0 == r6 ? 0 : -1))
                if (r0 <= 0) goto L_0x00bb
                r10.emitFrame(r3)
                goto L_0x0098
            L_0x00a6:
                if (r1 == 0) goto L_0x00bb
                okhttp3.internal.http2.Http2Stream r0 = r10.this$0
                okhttp3.internal.http2.Http2Connection r4 = r0.getConnection()
                okhttp3.internal.http2.Http2Stream r0 = r10.this$0
                int r5 = r0.getId()
                r6 = 1
                r7 = 0
                r8 = 0
                r4.writeData(r5, r6, r7, r8)
            L_0x00bb:
                okhttp3.internal.http2.Http2Stream r0 = r10.this$0
                monitor-enter(r0)
                r10.setClosed(r3)     // Catch:{ all -> 0x00d3 }
                kotlin.Unit r1 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x00d3 }
                monitor-exit(r0)
                okhttp3.internal.http2.Http2Stream r0 = r10.this$0
                okhttp3.internal.http2.Http2Connection r0 = r0.getConnection()
                r0.flush()
                okhttp3.internal.http2.Http2Stream r10 = r10.this$0
                r10.cancelStreamIfNecessary$okhttp()
                return
            L_0x00d3:
                r10 = move-exception
                monitor-exit(r0)
                throw r10
            L_0x00d6:
                r10 = move-exception
                monitor-exit(r0)
                throw r10
            */
            throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.http2.Http2Stream.FramingSink.close():void");
        }
    }

    public final void waitForIo$okhttp() throws InterruptedIOException {
        try {
            wait();
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException();
        }
    }

    private final boolean closeInternal(ErrorCode errorCode2, IOException iOException) {
        if (!Util.assertionsEnabled || !Thread.holdsLock(this)) {
            synchronized (this) {
                if (getErrorCode$okhttp() != null) {
                    return false;
                }
                if (getSource$okhttp().getFinished$okhttp() && getSink$okhttp().getFinished()) {
                    return false;
                }
                setErrorCode$okhttp(errorCode2);
                setErrorException$okhttp(iOException);
                notifyAll();
                Unit unit = Unit.INSTANCE;
                this.connection.removeStream$okhttp(this.id);
                return true;
            }
        }
        throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST NOT hold lock on " + this);
    }

    public final void cancelStreamIfNecessary$okhttp() throws IOException {
        boolean z;
        boolean isOpen;
        if (!Util.assertionsEnabled || !Thread.holdsLock(this)) {
            synchronized (this) {
                z = !getSource$okhttp().getFinished$okhttp() && getSource$okhttp().getClosed$okhttp() && (getSink$okhttp().getFinished() || getSink$okhttp().getClosed());
                isOpen = isOpen();
                Unit unit = Unit.INSTANCE;
            }
            if (z) {
                close(ErrorCode.CANCEL, (IOException) null);
            } else if (!isOpen) {
                this.connection.removeStream$okhttp(this.id);
            }
        } else {
            throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST NOT hold lock on " + this);
        }
    }

    public final void receiveData(@NotNull BufferedSource bufferedSource, int i) throws IOException {
        Intrinsics.checkNotNullParameter(bufferedSource, "source");
        if (!Util.assertionsEnabled || !Thread.holdsLock(this)) {
            this.source.receive$okhttp(bufferedSource, (long) i);
            return;
        }
        throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST NOT hold lock on " + this);
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0051  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void receiveHeaders(@org.jetbrains.annotations.NotNull okhttp3.Headers r3, boolean r4) {
        /*
            r2 = this;
            java.lang.String r0 = "headers"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r3, r0)
            boolean r0 = okhttp3.internal.Util.assertionsEnabled
            if (r0 == 0) goto L_0x0037
            boolean r0 = java.lang.Thread.holdsLock(r2)
            if (r0 != 0) goto L_0x0010
            goto L_0x0037
        L_0x0010:
            java.lang.AssertionError r3 = new java.lang.AssertionError
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r0 = "Thread "
            r4.append(r0)
            java.lang.Thread r0 = java.lang.Thread.currentThread()
            java.lang.String r0 = r0.getName()
            r4.append(r0)
            java.lang.String r0 = " MUST NOT hold lock on "
            r4.append(r0)
            r4.append(r2)
            java.lang.String r2 = r4.toString()
            r3.<init>(r2)
            throw r3
        L_0x0037:
            monitor-enter(r2)
            boolean r0 = r2.hasResponseHeaders     // Catch:{ all -> 0x006c }
            r1 = 1
            if (r0 == 0) goto L_0x0048
            if (r4 != 0) goto L_0x0040
            goto L_0x0048
        L_0x0040:
            okhttp3.internal.http2.Http2Stream$FramingSource r0 = r2.getSource$okhttp()     // Catch:{ all -> 0x006c }
            r0.setTrailers(r3)     // Catch:{ all -> 0x006c }
            goto L_0x004f
        L_0x0048:
            r2.hasResponseHeaders = r1     // Catch:{ all -> 0x006c }
            java.util.ArrayDeque<okhttp3.Headers> r0 = r2.headersQueue     // Catch:{ all -> 0x006c }
            r0.add(r3)     // Catch:{ all -> 0x006c }
        L_0x004f:
            if (r4 == 0) goto L_0x0058
            okhttp3.internal.http2.Http2Stream$FramingSource r3 = r2.getSource$okhttp()     // Catch:{ all -> 0x006c }
            r3.setFinished$okhttp(r1)     // Catch:{ all -> 0x006c }
        L_0x0058:
            boolean r3 = r2.isOpen()     // Catch:{ all -> 0x006c }
            r2.notifyAll()     // Catch:{ all -> 0x006c }
            kotlin.Unit r4 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x006c }
            monitor-exit(r2)
            if (r3 != 0) goto L_0x006b
            okhttp3.internal.http2.Http2Connection r3 = r2.connection
            int r2 = r2.id
            r3.removeStream$okhttp(r2)
        L_0x006b:
            return
        L_0x006c:
            r3 = move-exception
            monitor-exit(r2)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.http2.Http2Stream.receiveHeaders(okhttp3.Headers, boolean):void");
    }

    /* compiled from: Http2Stream.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }

    public final void addBytesToWriteWindow(long j) {
        this.writeBytesMaximum += j;
        if (j > 0) {
            notifyAll();
        }
    }

    public final void checkOutNotClosed$okhttp() throws IOException {
        if (this.sink.getClosed()) {
            throw new IOException("stream closed");
        } else if (this.sink.getFinished()) {
            throw new IOException("stream finished");
        } else if (this.errorCode != null) {
            Throwable th = this.errorException;
            if (th == null) {
                ErrorCode errorCode2 = this.errorCode;
                Intrinsics.checkNotNull(errorCode2);
                th = new StreamResetException(errorCode2);
            }
            throw th;
        }
    }

    /* compiled from: Http2Stream.kt */
    public final class StreamTimeout extends AsyncTimeout {
        final /* synthetic */ Http2Stream this$0;

        public StreamTimeout(Http2Stream http2Stream) {
            Intrinsics.checkNotNullParameter(http2Stream, "this$0");
            this.this$0 = http2Stream;
        }

        /* access modifiers changed from: protected */
        public void timedOut() {
            this.this$0.closeLater(ErrorCode.CANCEL);
            this.this$0.getConnection().sendDegradedPingLater$okhttp();
        }

        /* access modifiers changed from: protected */
        @NotNull
        public IOException newTimeoutException(@Nullable IOException iOException) {
            SocketTimeoutException socketTimeoutException = new SocketTimeoutException(EucTestIntent.Extras.TIMEOUT);
            if (iOException != null) {
                socketTimeoutException.initCause(iOException);
            }
            return socketTimeoutException;
        }

        public final void exitAndThrowIfTimedOut() throws IOException {
            if (exit()) {
                throw newTimeoutException((IOException) null);
            }
        }
    }
}
