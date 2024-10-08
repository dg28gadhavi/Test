package okhttp3.internal.http2;

import com.sec.internal.helper.os.IntentUtil;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$IntRef;
import kotlin.jvm.internal.Ref$ObjectRef;
import okhttp3.internal.Util;
import okhttp3.internal.concurrent.TaskQueue;
import okhttp3.internal.concurrent.TaskQueue$execute$1;
import okhttp3.internal.concurrent.TaskRunner;
import okhttp3.internal.http2.Http2Reader;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Http2Connection.kt */
public final class Http2Connection implements Closeable {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    /* access modifiers changed from: private */
    @NotNull
    public static final Settings DEFAULT_SETTINGS;
    /* access modifiers changed from: private */
    public long awaitPongsReceived;
    private final boolean client;
    @NotNull
    private final String connectionName;
    /* access modifiers changed from: private */
    @NotNull
    public final Set<Integer> currentPushRequests;
    private long degradedPingsSent;
    private long degradedPongDeadlineNs;
    /* access modifiers changed from: private */
    public long degradedPongsReceived;
    /* access modifiers changed from: private */
    public long intervalPingsSent;
    /* access modifiers changed from: private */
    public long intervalPongsReceived;
    /* access modifiers changed from: private */
    public boolean isShutdown;
    private int lastGoodStreamId;
    @NotNull
    private final Listener listener;
    private int nextStreamId;
    @NotNull
    private final Settings okHttpSettings;
    @NotNull
    private Settings peerSettings;
    /* access modifiers changed from: private */
    @NotNull
    public final PushObserver pushObserver;
    @NotNull
    private final TaskQueue pushQueue;
    private long readBytesAcknowledged;
    private long readBytesTotal;
    @NotNull
    private final ReaderRunnable readerRunnable;
    /* access modifiers changed from: private */
    @NotNull
    public final TaskQueue settingsListenerQueue;
    @NotNull
    private final Socket socket;
    @NotNull
    private final Map<Integer, Http2Stream> streams = new LinkedHashMap();
    /* access modifiers changed from: private */
    @NotNull
    public final TaskRunner taskRunner;
    /* access modifiers changed from: private */
    public long writeBytesMaximum;
    private long writeBytesTotal;
    @NotNull
    private final Http2Writer writer;
    /* access modifiers changed from: private */
    @NotNull
    public final TaskQueue writerQueue;

    public final boolean pushedStream$okhttp(int i) {
        return i != 0 && (i & 1) == 0;
    }

    public Http2Connection(@NotNull Builder builder) {
        Intrinsics.checkNotNullParameter(builder, "builder");
        boolean client$okhttp = builder.getClient$okhttp();
        this.client = client$okhttp;
        this.listener = builder.getListener$okhttp();
        String connectionName$okhttp = builder.getConnectionName$okhttp();
        this.connectionName = connectionName$okhttp;
        this.nextStreamId = builder.getClient$okhttp() ? 3 : 2;
        TaskRunner taskRunner$okhttp = builder.getTaskRunner$okhttp();
        this.taskRunner = taskRunner$okhttp;
        TaskQueue newQueue = taskRunner$okhttp.newQueue();
        this.writerQueue = newQueue;
        this.pushQueue = taskRunner$okhttp.newQueue();
        this.settingsListenerQueue = taskRunner$okhttp.newQueue();
        this.pushObserver = builder.getPushObserver$okhttp();
        Settings settings = new Settings();
        if (builder.getClient$okhttp()) {
            settings.set(7, IntentUtil.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        }
        this.okHttpSettings = settings;
        Settings settings2 = DEFAULT_SETTINGS;
        this.peerSettings = settings2;
        this.writeBytesMaximum = (long) settings2.getInitialWindowSize();
        this.socket = builder.getSocket$okhttp();
        this.writer = new Http2Writer(builder.getSink$okhttp(), client$okhttp);
        this.readerRunnable = new ReaderRunnable(this, new Http2Reader(builder.getSource$okhttp(), client$okhttp));
        this.currentPushRequests = new LinkedHashSet();
        if (builder.getPingIntervalMillis$okhttp() != 0) {
            long nanos = TimeUnit.MILLISECONDS.toNanos((long) builder.getPingIntervalMillis$okhttp());
            newQueue.schedule(new Http2Connection$special$$inlined$schedule$1(Intrinsics.stringPlus(connectionName$okhttp, " ping"), this, nanos), nanos);
        }
    }

    public final boolean getClient$okhttp() {
        return this.client;
    }

    @NotNull
    public final Listener getListener$okhttp() {
        return this.listener;
    }

    @NotNull
    public final Map<Integer, Http2Stream> getStreams$okhttp() {
        return this.streams;
    }

    @NotNull
    public final String getConnectionName$okhttp() {
        return this.connectionName;
    }

    public final int getLastGoodStreamId$okhttp() {
        return this.lastGoodStreamId;
    }

    public final void setLastGoodStreamId$okhttp(int i) {
        this.lastGoodStreamId = i;
    }

    public final int getNextStreamId$okhttp() {
        return this.nextStreamId;
    }

    public final void setNextStreamId$okhttp(int i) {
        this.nextStreamId = i;
    }

    @NotNull
    public final Settings getOkHttpSettings() {
        return this.okHttpSettings;
    }

    @NotNull
    public final Settings getPeerSettings() {
        return this.peerSettings;
    }

    public final void setPeerSettings(@NotNull Settings settings) {
        Intrinsics.checkNotNullParameter(settings, "<set-?>");
        this.peerSettings = settings;
    }

    public final long getWriteBytesTotal() {
        return this.writeBytesTotal;
    }

    public final long getWriteBytesMaximum() {
        return this.writeBytesMaximum;
    }

    @NotNull
    public final Socket getSocket$okhttp() {
        return this.socket;
    }

    @NotNull
    public final Http2Writer getWriter() {
        return this.writer;
    }

    @Nullable
    public final synchronized Http2Stream getStream(int i) {
        return this.streams.get(Integer.valueOf(i));
    }

    @Nullable
    public final synchronized Http2Stream removeStream$okhttp(int i) {
        Http2Stream remove;
        remove = this.streams.remove(Integer.valueOf(i));
        notifyAll();
        return remove;
    }

    public final synchronized void updateConnectionFlowControl$okhttp(long j) {
        long j2 = this.readBytesTotal + j;
        this.readBytesTotal = j2;
        long j3 = j2 - this.readBytesAcknowledged;
        if (j3 >= ((long) (this.okHttpSettings.getInitialWindowSize() / 2))) {
            writeWindowUpdateLater$okhttp(0, j3);
            this.readBytesAcknowledged += j3;
        }
    }

    @NotNull
    public final Http2Stream newStream(@NotNull List<Header> list, boolean z) throws IOException {
        Intrinsics.checkNotNullParameter(list, "requestHeaders");
        return newStream(0, list, z);
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0055  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final okhttp3.internal.http2.Http2Stream newStream(int r11, java.util.List<okhttp3.internal.http2.Header> r12, boolean r13) throws java.io.IOException {
        /*
            r10 = this;
            r6 = r13 ^ 1
            r4 = 0
            okhttp3.internal.http2.Http2Writer r7 = r10.writer
            monitor-enter(r7)
            monitor-enter(r10)     // Catch:{ all -> 0x0099 }
            int r0 = r10.getNextStreamId$okhttp()     // Catch:{ all -> 0x0096 }
            r1 = 1073741823(0x3fffffff, float:1.9999999)
            if (r0 <= r1) goto L_0x0015
            okhttp3.internal.http2.ErrorCode r0 = okhttp3.internal.http2.ErrorCode.REFUSED_STREAM     // Catch:{ all -> 0x0096 }
            r10.shutdown(r0)     // Catch:{ all -> 0x0096 }
        L_0x0015:
            boolean r0 = r10.isShutdown     // Catch:{ all -> 0x0096 }
            if (r0 != 0) goto L_0x0090
            int r8 = r10.getNextStreamId$okhttp()     // Catch:{ all -> 0x0096 }
            int r0 = r10.getNextStreamId$okhttp()     // Catch:{ all -> 0x0096 }
            int r0 = r0 + 2
            r10.setNextStreamId$okhttp(r0)     // Catch:{ all -> 0x0096 }
            okhttp3.internal.http2.Http2Stream r9 = new okhttp3.internal.http2.Http2Stream     // Catch:{ all -> 0x0096 }
            r5 = 0
            r0 = r9
            r1 = r8
            r2 = r10
            r3 = r6
            r0.<init>(r1, r2, r3, r4, r5)     // Catch:{ all -> 0x0096 }
            r0 = 1
            if (r13 == 0) goto L_0x004e
            long r1 = r10.getWriteBytesTotal()     // Catch:{ all -> 0x0096 }
            long r3 = r10.getWriteBytesMaximum()     // Catch:{ all -> 0x0096 }
            int r13 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r13 >= 0) goto L_0x004e
            long r1 = r9.getWriteBytesTotal()     // Catch:{ all -> 0x0096 }
            long r3 = r9.getWriteBytesMaximum()     // Catch:{ all -> 0x0096 }
            int r13 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r13 < 0) goto L_0x004c
            goto L_0x004e
        L_0x004c:
            r13 = 0
            goto L_0x004f
        L_0x004e:
            r13 = r0
        L_0x004f:
            boolean r1 = r9.isOpen()     // Catch:{ all -> 0x0096 }
            if (r1 == 0) goto L_0x0060
            java.util.Map r1 = r10.getStreams$okhttp()     // Catch:{ all -> 0x0096 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x0096 }
            r1.put(r2, r9)     // Catch:{ all -> 0x0096 }
        L_0x0060:
            kotlin.Unit r1 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x0096 }
            monitor-exit(r10)     // Catch:{ all -> 0x0099 }
            if (r11 != 0) goto L_0x006d
            okhttp3.internal.http2.Http2Writer r11 = r10.getWriter()     // Catch:{ all -> 0x0099 }
            r11.headers(r6, r8, r12)     // Catch:{ all -> 0x0099 }
            goto L_0x007b
        L_0x006d:
            boolean r1 = r10.getClient$okhttp()     // Catch:{ all -> 0x0099 }
            r0 = r0 ^ r1
            if (r0 == 0) goto L_0x0084
            okhttp3.internal.http2.Http2Writer r0 = r10.getWriter()     // Catch:{ all -> 0x0099 }
            r0.pushPromise(r11, r8, r12)     // Catch:{ all -> 0x0099 }
        L_0x007b:
            monitor-exit(r7)
            if (r13 == 0) goto L_0x0083
            okhttp3.internal.http2.Http2Writer r10 = r10.writer
            r10.flush()
        L_0x0083:
            return r9
        L_0x0084:
            java.lang.String r10 = "client streams shouldn't have associated stream IDs"
            java.lang.IllegalArgumentException r11 = new java.lang.IllegalArgumentException     // Catch:{ all -> 0x0099 }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x0099 }
            r11.<init>(r10)     // Catch:{ all -> 0x0099 }
            throw r11     // Catch:{ all -> 0x0099 }
        L_0x0090:
            okhttp3.internal.http2.ConnectionShutdownException r11 = new okhttp3.internal.http2.ConnectionShutdownException     // Catch:{ all -> 0x0096 }
            r11.<init>()     // Catch:{ all -> 0x0096 }
            throw r11     // Catch:{ all -> 0x0096 }
        L_0x0096:
            r11 = move-exception
            monitor-exit(r10)     // Catch:{ all -> 0x0099 }
            throw r11     // Catch:{ all -> 0x0099 }
        L_0x0099:
            r10 = move-exception
            monitor-exit(r7)
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.http2.Http2Connection.newStream(int, java.util.List, boolean):okhttp3.internal.http2.Http2Stream");
    }

    public final void writeHeaders$okhttp(int i, boolean z, @NotNull List<Header> list) throws IOException {
        Intrinsics.checkNotNullParameter(list, "alternating");
        this.writer.headers(z, i, list);
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(3:26|27|28) */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        r2 = java.lang.Math.min((int) java.lang.Math.min(r12, getWriteBytesMaximum() - getWriteBytesTotal()), getWriter().maxDataLength());
        r6 = (long) r2;
        r8.writeBytesTotal = getWriteBytesTotal() + r6;
        r4 = kotlin.Unit.INSTANCE;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        java.lang.Thread.currentThread().interrupt();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x007c, code lost:
        throw new java.io.InterruptedIOException();
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:26:0x0070 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void writeData(int r9, boolean r10, @org.jetbrains.annotations.Nullable okio.Buffer r11, long r12) throws java.io.IOException {
        /*
            r8 = this;
            r0 = 0
            int r2 = (r12 > r0 ? 1 : (r12 == r0 ? 0 : -1))
            r3 = 0
            if (r2 != 0) goto L_0x000d
            okhttp3.internal.http2.Http2Writer r8 = r8.writer
            r8.data(r10, r9, r11, r3)
            return
        L_0x000d:
            int r2 = (r12 > r0 ? 1 : (r12 == r0 ? 0 : -1))
            if (r2 <= 0) goto L_0x007f
            monitor-enter(r8)
        L_0x0012:
            long r4 = r8.getWriteBytesTotal()     // Catch:{ InterruptedException -> 0x0070 }
            long r6 = r8.getWriteBytesMaximum()     // Catch:{ InterruptedException -> 0x0070 }
            int r2 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r2 < 0) goto L_0x0039
            java.util.Map r2 = r8.getStreams$okhttp()     // Catch:{ InterruptedException -> 0x0070 }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r9)     // Catch:{ InterruptedException -> 0x0070 }
            boolean r2 = r2.containsKey(r4)     // Catch:{ InterruptedException -> 0x0070 }
            if (r2 == 0) goto L_0x0030
            r8.wait()     // Catch:{ InterruptedException -> 0x0070 }
            goto L_0x0012
        L_0x0030:
            java.io.IOException r9 = new java.io.IOException     // Catch:{ InterruptedException -> 0x0070 }
            java.lang.String r10 = "stream closed"
            r9.<init>(r10)     // Catch:{ InterruptedException -> 0x0070 }
            throw r9     // Catch:{ InterruptedException -> 0x0070 }
        L_0x0039:
            long r4 = r8.getWriteBytesMaximum()     // Catch:{ all -> 0x006e }
            long r6 = r8.getWriteBytesTotal()     // Catch:{ all -> 0x006e }
            long r4 = r4 - r6
            long r4 = java.lang.Math.min(r12, r4)     // Catch:{ all -> 0x006e }
            int r2 = (int) r4     // Catch:{ all -> 0x006e }
            okhttp3.internal.http2.Http2Writer r4 = r8.getWriter()     // Catch:{ all -> 0x006e }
            int r4 = r4.maxDataLength()     // Catch:{ all -> 0x006e }
            int r2 = java.lang.Math.min(r2, r4)     // Catch:{ all -> 0x006e }
            long r4 = r8.getWriteBytesTotal()     // Catch:{ all -> 0x006e }
            long r6 = (long) r2     // Catch:{ all -> 0x006e }
            long r4 = r4 + r6
            r8.writeBytesTotal = r4     // Catch:{ all -> 0x006e }
            kotlin.Unit r4 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x006e }
            monitor-exit(r8)
            long r12 = r12 - r6
            okhttp3.internal.http2.Http2Writer r4 = r8.writer
            if (r10 == 0) goto L_0x0069
            int r5 = (r12 > r0 ? 1 : (r12 == r0 ? 0 : -1))
            if (r5 != 0) goto L_0x0069
            r5 = 1
            goto L_0x006a
        L_0x0069:
            r5 = r3
        L_0x006a:
            r4.data(r5, r9, r11, r2)
            goto L_0x000d
        L_0x006e:
            r9 = move-exception
            goto L_0x007d
        L_0x0070:
            java.lang.Thread r9 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x006e }
            r9.interrupt()     // Catch:{ all -> 0x006e }
            java.io.InterruptedIOException r9 = new java.io.InterruptedIOException     // Catch:{ all -> 0x006e }
            r9.<init>()     // Catch:{ all -> 0x006e }
            throw r9     // Catch:{ all -> 0x006e }
        L_0x007d:
            monitor-exit(r8)
            throw r9
        L_0x007f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.http2.Http2Connection.writeData(int, boolean, okio.Buffer, long):void");
    }

    public final void writeSynResetLater$okhttp(int i, @NotNull ErrorCode errorCode) {
        Intrinsics.checkNotNullParameter(errorCode, "errorCode");
        TaskQueue taskQueue = this.writerQueue;
        taskQueue.schedule(new Http2Connection$writeSynResetLater$$inlined$execute$default$1(this.connectionName + '[' + i + "] writeSynReset", true, this, i, errorCode), 0);
    }

    public final void writeSynReset$okhttp(int i, @NotNull ErrorCode errorCode) throws IOException {
        Intrinsics.checkNotNullParameter(errorCode, "statusCode");
        this.writer.rstStream(i, errorCode);
    }

    public final void writeWindowUpdateLater$okhttp(int i, long j) {
        TaskQueue taskQueue = this.writerQueue;
        taskQueue.schedule(new Http2Connection$writeWindowUpdateLater$$inlined$execute$default$1(this.connectionName + '[' + i + "] windowUpdate", true, this, i, j), 0);
    }

    public final void writePing(boolean z, int i, int i2) {
        try {
            this.writer.ping(z, i, i2);
        } catch (IOException e) {
            failConnection(e);
        }
    }

    public final void flush() throws IOException {
        this.writer.flush();
    }

    public final void shutdown(@NotNull ErrorCode errorCode) throws IOException {
        Intrinsics.checkNotNullParameter(errorCode, "statusCode");
        synchronized (this.writer) {
            Ref$IntRef ref$IntRef = new Ref$IntRef();
            synchronized (this) {
                if (!this.isShutdown) {
                    this.isShutdown = true;
                    ref$IntRef.element = getLastGoodStreamId$okhttp();
                    Unit unit = Unit.INSTANCE;
                    getWriter().goAway(ref$IntRef.element, errorCode, Util.EMPTY_BYTE_ARRAY);
                }
            }
        }
    }

    public void close() {
        close$okhttp(ErrorCode.NO_ERROR, ErrorCode.CANCEL, (IOException) null);
    }

    /* access modifiers changed from: private */
    public final void failConnection(IOException iOException) {
        ErrorCode errorCode = ErrorCode.PROTOCOL_ERROR;
        close$okhttp(errorCode, errorCode, iOException);
    }

    public static /* synthetic */ void start$default(Http2Connection http2Connection, boolean z, TaskRunner taskRunner2, int i, Object obj) throws IOException {
        if ((i & 1) != 0) {
            z = true;
        }
        if ((i & 2) != 0) {
            taskRunner2 = TaskRunner.INSTANCE;
        }
        http2Connection.start(z, taskRunner2);
    }

    public final void start(boolean z, @NotNull TaskRunner taskRunner2) throws IOException {
        Intrinsics.checkNotNullParameter(taskRunner2, "taskRunner");
        if (z) {
            this.writer.connectionPreface();
            this.writer.settings(this.okHttpSettings);
            int initialWindowSize = this.okHttpSettings.getInitialWindowSize();
            if (initialWindowSize != 65535) {
                this.writer.windowUpdate(0, (long) (initialWindowSize - 65535));
            }
        }
        taskRunner2.newQueue().schedule(new TaskQueue$execute$1(this.connectionName, true, this.readerRunnable), 0);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0019, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final synchronized boolean isHealthy(long r7) {
        /*
            r6 = this;
            monitor-enter(r6)
            boolean r0 = r6.isShutdown     // Catch:{ all -> 0x001b }
            r1 = 0
            if (r0 == 0) goto L_0x0008
            monitor-exit(r6)
            return r1
        L_0x0008:
            long r2 = r6.degradedPongsReceived     // Catch:{ all -> 0x001b }
            long r4 = r6.degradedPingsSent     // Catch:{ all -> 0x001b }
            int r0 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r0 >= 0) goto L_0x0018
            long r2 = r6.degradedPongDeadlineNs     // Catch:{ all -> 0x001b }
            int r7 = (r7 > r2 ? 1 : (r7 == r2 ? 0 : -1))
            if (r7 < 0) goto L_0x0018
            monitor-exit(r6)
            return r1
        L_0x0018:
            monitor-exit(r6)
            r6 = 1
            return r6
        L_0x001b:
            r7 = move-exception
            monitor-exit(r6)
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.http2.Http2Connection.isHealthy(long):boolean");
    }

    public final void sendDegradedPingLater$okhttp() {
        synchronized (this) {
            long j = this.degradedPongsReceived;
            long j2 = this.degradedPingsSent;
            if (j >= j2) {
                this.degradedPingsSent = j2 + 1;
                this.degradedPongDeadlineNs = System.nanoTime() + ((long) 1000000000);
                Unit unit = Unit.INSTANCE;
                this.writerQueue.schedule(new Http2Connection$sendDegradedPingLater$$inlined$execute$default$1(Intrinsics.stringPlus(this.connectionName, " ping"), true, this), 0);
            }
        }
    }

    /* compiled from: Http2Connection.kt */
    public static final class Builder {
        private boolean client;
        public String connectionName;
        @NotNull
        private Listener listener = Listener.REFUSE_INCOMING_STREAMS;
        private int pingIntervalMillis;
        @NotNull
        private PushObserver pushObserver = PushObserver.CANCEL;
        public BufferedSink sink;
        public Socket socket;
        public BufferedSource source;
        @NotNull
        private final TaskRunner taskRunner;

        public Builder(boolean z, @NotNull TaskRunner taskRunner2) {
            Intrinsics.checkNotNullParameter(taskRunner2, "taskRunner");
            this.client = z;
            this.taskRunner = taskRunner2;
        }

        public final boolean getClient$okhttp() {
            return this.client;
        }

        @NotNull
        public final TaskRunner getTaskRunner$okhttp() {
            return this.taskRunner;
        }

        @NotNull
        public final Socket getSocket$okhttp() {
            Socket socket2 = this.socket;
            if (socket2 != null) {
                return socket2;
            }
            Intrinsics.throwUninitializedPropertyAccessException("socket");
            return null;
        }

        public final void setSocket$okhttp(@NotNull Socket socket2) {
            Intrinsics.checkNotNullParameter(socket2, "<set-?>");
            this.socket = socket2;
        }

        @NotNull
        public final String getConnectionName$okhttp() {
            String str = this.connectionName;
            if (str != null) {
                return str;
            }
            Intrinsics.throwUninitializedPropertyAccessException("connectionName");
            return null;
        }

        public final void setConnectionName$okhttp(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "<set-?>");
            this.connectionName = str;
        }

        @NotNull
        public final BufferedSource getSource$okhttp() {
            BufferedSource bufferedSource = this.source;
            if (bufferedSource != null) {
                return bufferedSource;
            }
            Intrinsics.throwUninitializedPropertyAccessException("source");
            return null;
        }

        public final void setSource$okhttp(@NotNull BufferedSource bufferedSource) {
            Intrinsics.checkNotNullParameter(bufferedSource, "<set-?>");
            this.source = bufferedSource;
        }

        @NotNull
        public final BufferedSink getSink$okhttp() {
            BufferedSink bufferedSink = this.sink;
            if (bufferedSink != null) {
                return bufferedSink;
            }
            Intrinsics.throwUninitializedPropertyAccessException("sink");
            return null;
        }

        public final void setSink$okhttp(@NotNull BufferedSink bufferedSink) {
            Intrinsics.checkNotNullParameter(bufferedSink, "<set-?>");
            this.sink = bufferedSink;
        }

        @NotNull
        public final Listener getListener$okhttp() {
            return this.listener;
        }

        public final void setListener$okhttp(@NotNull Listener listener2) {
            Intrinsics.checkNotNullParameter(listener2, "<set-?>");
            this.listener = listener2;
        }

        @NotNull
        public final PushObserver getPushObserver$okhttp() {
            return this.pushObserver;
        }

        public final int getPingIntervalMillis$okhttp() {
            return this.pingIntervalMillis;
        }

        public final void setPingIntervalMillis$okhttp(int i) {
            this.pingIntervalMillis = i;
        }

        @NotNull
        public final Builder socket(@NotNull Socket socket2, @NotNull String str, @NotNull BufferedSource bufferedSource, @NotNull BufferedSink bufferedSink) throws IOException {
            String str2;
            Intrinsics.checkNotNullParameter(socket2, "socket");
            Intrinsics.checkNotNullParameter(str, "peerName");
            Intrinsics.checkNotNullParameter(bufferedSource, "source");
            Intrinsics.checkNotNullParameter(bufferedSink, "sink");
            setSocket$okhttp(socket2);
            if (getClient$okhttp()) {
                str2 = Util.okHttpName + ' ' + str;
            } else {
                str2 = Intrinsics.stringPlus("MockWebServer ", str);
            }
            setConnectionName$okhttp(str2);
            setSource$okhttp(bufferedSource);
            setSink$okhttp(bufferedSink);
            return this;
        }

        @NotNull
        public final Builder listener(@NotNull Listener listener2) {
            Intrinsics.checkNotNullParameter(listener2, "listener");
            setListener$okhttp(listener2);
            return this;
        }

        @NotNull
        public final Builder pingIntervalMillis(int i) {
            setPingIntervalMillis$okhttp(i);
            return this;
        }

        @NotNull
        public final Http2Connection build() {
            return new Http2Connection(this);
        }
    }

    /* compiled from: Http2Connection.kt */
    public final class ReaderRunnable implements Http2Reader.Handler, Function0<Unit> {
        @NotNull
        private final Http2Reader reader;
        final /* synthetic */ Http2Connection this$0;

        public void ackSettings() {
        }

        public void priority(int i, int i2, int i3, boolean z) {
        }

        public ReaderRunnable(@NotNull Http2Connection http2Connection, Http2Reader http2Reader) {
            Intrinsics.checkNotNullParameter(http2Connection, "this$0");
            Intrinsics.checkNotNullParameter(http2Reader, "reader");
            this.this$0 = http2Connection;
            this.reader = http2Reader;
        }

        public void invoke() {
            ErrorCode errorCode;
            ErrorCode errorCode2 = ErrorCode.INTERNAL_ERROR;
            e = null;
            try {
                this.reader.readConnectionPreface(this);
                while (this.reader.nextFrame(false, this)) {
                }
                errorCode = ErrorCode.NO_ERROR;
                try {
                    this.this$0.close$okhttp(errorCode, ErrorCode.CANCEL, (IOException) null);
                } catch (IOException e) {
                    e = e;
                    try {
                        ErrorCode errorCode3 = ErrorCode.PROTOCOL_ERROR;
                        this.this$0.close$okhttp(errorCode3, errorCode3, e);
                        Util.closeQuietly((Closeable) this.reader);
                    } catch (Throwable th) {
                        th = th;
                        this.this$0.close$okhttp(errorCode, errorCode2, e);
                        Util.closeQuietly((Closeable) this.reader);
                        throw th;
                    }
                }
            } catch (IOException e2) {
                e = e2;
                errorCode = errorCode2;
                ErrorCode errorCode32 = ErrorCode.PROTOCOL_ERROR;
                this.this$0.close$okhttp(errorCode32, errorCode32, e);
                Util.closeQuietly((Closeable) this.reader);
            } catch (Throwable th2) {
                th = th2;
                errorCode = errorCode2;
                this.this$0.close$okhttp(errorCode, errorCode2, e);
                Util.closeQuietly((Closeable) this.reader);
                throw th;
            }
            Util.closeQuietly((Closeable) this.reader);
        }

        public void data(boolean z, int i, @NotNull BufferedSource bufferedSource, int i2) throws IOException {
            Intrinsics.checkNotNullParameter(bufferedSource, "source");
            if (this.this$0.pushedStream$okhttp(i)) {
                this.this$0.pushDataLater$okhttp(i, bufferedSource, i2, z);
                return;
            }
            Http2Stream stream = this.this$0.getStream(i);
            if (stream == null) {
                this.this$0.writeSynResetLater$okhttp(i, ErrorCode.PROTOCOL_ERROR);
                long j = (long) i2;
                this.this$0.updateConnectionFlowControl$okhttp(j);
                bufferedSource.skip(j);
                return;
            }
            stream.receiveData(bufferedSource, i2);
            if (z) {
                stream.receiveHeaders(Util.EMPTY_HEADERS, true);
            }
        }

        public void headers(boolean z, int i, int i2, @NotNull List<Header> list) {
            Intrinsics.checkNotNullParameter(list, "headerBlock");
            if (this.this$0.pushedStream$okhttp(i)) {
                this.this$0.pushHeadersLater$okhttp(i, list, z);
                return;
            }
            Http2Connection http2Connection = this.this$0;
            synchronized (http2Connection) {
                Http2Stream stream = http2Connection.getStream(i);
                if (stream != null) {
                    Unit unit = Unit.INSTANCE;
                    stream.receiveHeaders(Util.toHeaders(list), z);
                } else if (!http2Connection.isShutdown) {
                    if (i > http2Connection.getLastGoodStreamId$okhttp()) {
                        if (i % 2 != http2Connection.getNextStreamId$okhttp() % 2) {
                            Http2Stream http2Stream = new Http2Stream(i, http2Connection, false, z, Util.toHeaders(list));
                            http2Connection.setLastGoodStreamId$okhttp(i);
                            http2Connection.getStreams$okhttp().put(Integer.valueOf(i), http2Stream);
                            TaskQueue newQueue = http2Connection.taskRunner.newQueue();
                            newQueue.schedule(new Http2Connection$ReaderRunnable$headers$lambda2$$inlined$execute$default$1(http2Connection.getConnectionName$okhttp() + '[' + i + "] onStream", true, http2Connection, http2Stream), 0);
                        }
                    }
                }
            }
        }

        public void rstStream(int i, @NotNull ErrorCode errorCode) {
            Intrinsics.checkNotNullParameter(errorCode, "errorCode");
            if (this.this$0.pushedStream$okhttp(i)) {
                this.this$0.pushResetLater$okhttp(i, errorCode);
                return;
            }
            Http2Stream removeStream$okhttp = this.this$0.removeStream$okhttp(i);
            if (removeStream$okhttp != null) {
                removeStream$okhttp.receiveRstStream(errorCode);
            }
        }

        public void settings(boolean z, @NotNull Settings settings) {
            Intrinsics.checkNotNullParameter(settings, "settings");
            this.this$0.writerQueue.schedule(new Http2Connection$ReaderRunnable$settings$$inlined$execute$default$1(Intrinsics.stringPlus(this.this$0.getConnectionName$okhttp(), " applyAndAckSettings"), true, this, z, settings), 0);
        }

        public final void applyAndAckSettings(boolean z, @NotNull T t) {
            long initialWindowSize;
            int i;
            Http2Stream[] http2StreamArr;
            Intrinsics.checkNotNullParameter(t, "settings");
            Ref$ObjectRef ref$ObjectRef = new Ref$ObjectRef();
            Http2Writer writer = this.this$0.getWriter();
            Http2Connection http2Connection = this.this$0;
            synchronized (writer) {
                synchronized (http2Connection) {
                    Settings peerSettings = http2Connection.getPeerSettings();
                    if (!z) {
                        T settings = new Settings();
                        settings.merge(peerSettings);
                        settings.merge(t);
                        t = settings;
                    }
                    ref$ObjectRef.element = t;
                    initialWindowSize = ((long) t.getInitialWindowSize()) - ((long) peerSettings.getInitialWindowSize());
                    i = 0;
                    if (initialWindowSize != 0) {
                        if (!http2Connection.getStreams$okhttp().isEmpty()) {
                            Object[] array = http2Connection.getStreams$okhttp().values().toArray(new Http2Stream[0]);
                            if (array != null) {
                                http2StreamArr = (Http2Stream[]) array;
                                http2Connection.setPeerSettings((Settings) ref$ObjectRef.element);
                                http2Connection.settingsListenerQueue.schedule(new Http2Connection$ReaderRunnable$applyAndAckSettings$lambda7$lambda6$$inlined$execute$default$1(Intrinsics.stringPlus(http2Connection.getConnectionName$okhttp(), " onSettings"), true, http2Connection, ref$ObjectRef), 0);
                                Unit unit = Unit.INSTANCE;
                            } else {
                                throw new NullPointerException("null cannot be cast to non-null type kotlin.Array<T of kotlin.collections.ArraysKt__ArraysJVMKt.toTypedArray>");
                            }
                        }
                    }
                    http2StreamArr = null;
                    http2Connection.setPeerSettings((Settings) ref$ObjectRef.element);
                    http2Connection.settingsListenerQueue.schedule(new Http2Connection$ReaderRunnable$applyAndAckSettings$lambda7$lambda6$$inlined$execute$default$1(Intrinsics.stringPlus(http2Connection.getConnectionName$okhttp(), " onSettings"), true, http2Connection, ref$ObjectRef), 0);
                    Unit unit2 = Unit.INSTANCE;
                }
                try {
                    http2Connection.getWriter().applyAndAckSettings((Settings) ref$ObjectRef.element);
                } catch (IOException e) {
                    http2Connection.failConnection(e);
                }
                Unit unit3 = Unit.INSTANCE;
            }
            if (http2StreamArr != null) {
                int length = http2StreamArr.length;
                while (i < length) {
                    Http2Stream http2Stream = http2StreamArr[i];
                    i++;
                    synchronized (http2Stream) {
                        http2Stream.addBytesToWriteWindow(initialWindowSize);
                        Unit unit4 = Unit.INSTANCE;
                    }
                }
            }
        }

        public void ping(boolean z, int i, int i2) {
            if (z) {
                Http2Connection http2Connection = this.this$0;
                synchronized (http2Connection) {
                    if (i == 1) {
                        http2Connection.intervalPongsReceived = http2Connection.intervalPongsReceived + 1;
                    } else if (i != 2) {
                        if (i == 3) {
                            http2Connection.awaitPongsReceived = http2Connection.awaitPongsReceived + 1;
                            http2Connection.notifyAll();
                        }
                        Unit unit = Unit.INSTANCE;
                    } else {
                        http2Connection.degradedPongsReceived = http2Connection.degradedPongsReceived + 1;
                    }
                }
                return;
            }
            this.this$0.writerQueue.schedule(new Http2Connection$ReaderRunnable$ping$$inlined$execute$default$1(Intrinsics.stringPlus(this.this$0.getConnectionName$okhttp(), " ping"), true, this.this$0, i, i2), 0);
        }

        public void goAway(int i, @NotNull ErrorCode errorCode, @NotNull ByteString byteString) {
            int i2;
            Object[] array;
            Intrinsics.checkNotNullParameter(errorCode, "errorCode");
            Intrinsics.checkNotNullParameter(byteString, "debugData");
            byteString.size();
            Http2Connection http2Connection = this.this$0;
            synchronized (http2Connection) {
                i2 = 0;
                array = http2Connection.getStreams$okhttp().values().toArray(new Http2Stream[0]);
                if (array != null) {
                    http2Connection.isShutdown = true;
                    Unit unit = Unit.INSTANCE;
                } else {
                    throw new NullPointerException("null cannot be cast to non-null type kotlin.Array<T of kotlin.collections.ArraysKt__ArraysJVMKt.toTypedArray>");
                }
            }
            Http2Stream[] http2StreamArr = (Http2Stream[]) array;
            int length = http2StreamArr.length;
            while (i2 < length) {
                Http2Stream http2Stream = http2StreamArr[i2];
                i2++;
                if (http2Stream.getId() > i && http2Stream.isLocallyInitiated()) {
                    http2Stream.receiveRstStream(ErrorCode.REFUSED_STREAM);
                    this.this$0.removeStream$okhttp(http2Stream.getId());
                }
            }
        }

        public void windowUpdate(int i, long j) {
            if (i == 0) {
                Http2Connection http2Connection = this.this$0;
                synchronized (http2Connection) {
                    http2Connection.writeBytesMaximum = http2Connection.getWriteBytesMaximum() + j;
                    http2Connection.notifyAll();
                    Unit unit = Unit.INSTANCE;
                }
                return;
            }
            Http2Stream stream = this.this$0.getStream(i);
            if (stream != null) {
                synchronized (stream) {
                    stream.addBytesToWriteWindow(j);
                    Unit unit2 = Unit.INSTANCE;
                }
            }
        }

        public void pushPromise(int i, int i2, @NotNull List<Header> list) {
            Intrinsics.checkNotNullParameter(list, "requestHeaders");
            this.this$0.pushRequestLater$okhttp(i2, list);
        }
    }

    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:32:0x0089 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void close$okhttp(@org.jetbrains.annotations.NotNull okhttp3.internal.http2.ErrorCode r4, @org.jetbrains.annotations.NotNull okhttp3.internal.http2.ErrorCode r5, @org.jetbrains.annotations.Nullable java.io.IOException r6) {
        /*
            r3 = this;
            java.lang.String r0 = "connectionCode"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r4, r0)
            java.lang.String r0 = "streamCode"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r5, r0)
            boolean r0 = okhttp3.internal.Util.assertionsEnabled
            if (r0 == 0) goto L_0x003d
            boolean r0 = java.lang.Thread.holdsLock(r3)
            if (r0 != 0) goto L_0x0016
            goto L_0x003d
        L_0x0016:
            java.lang.AssertionError r4 = new java.lang.AssertionError
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Thread "
            r5.append(r6)
            java.lang.Thread r6 = java.lang.Thread.currentThread()
            java.lang.String r6 = r6.getName()
            r5.append(r6)
            java.lang.String r6 = " MUST NOT hold lock on "
            r5.append(r6)
            r5.append(r3)
            java.lang.String r3 = r5.toString()
            r4.<init>(r3)
            throw r4
        L_0x003d:
            r3.shutdown(r4)     // Catch:{ IOException -> 0x0040 }
        L_0x0040:
            monitor-enter(r3)
            java.util.Map r4 = r3.getStreams$okhttp()     // Catch:{ all -> 0x00a0 }
            boolean r4 = r4.isEmpty()     // Catch:{ all -> 0x00a0 }
            r4 = r4 ^ 1
            r0 = 0
            if (r4 == 0) goto L_0x006e
            java.util.Map r4 = r3.getStreams$okhttp()     // Catch:{ all -> 0x00a0 }
            java.util.Collection r4 = r4.values()     // Catch:{ all -> 0x00a0 }
            okhttp3.internal.http2.Http2Stream[] r1 = new okhttp3.internal.http2.Http2Stream[r0]     // Catch:{ all -> 0x00a0 }
            java.lang.Object[] r4 = r4.toArray(r1)     // Catch:{ all -> 0x00a0 }
            if (r4 == 0) goto L_0x0066
            java.util.Map r1 = r3.getStreams$okhttp()     // Catch:{ all -> 0x00a0 }
            r1.clear()     // Catch:{ all -> 0x00a0 }
            goto L_0x006f
        L_0x0066:
            java.lang.NullPointerException r4 = new java.lang.NullPointerException     // Catch:{ all -> 0x00a0 }
            java.lang.String r5 = "null cannot be cast to non-null type kotlin.Array<T of kotlin.collections.ArraysKt__ArraysJVMKt.toTypedArray>"
            r4.<init>(r5)     // Catch:{ all -> 0x00a0 }
            throw r4     // Catch:{ all -> 0x00a0 }
        L_0x006e:
            r4 = 0
        L_0x006f:
            kotlin.Unit r1 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x00a0 }
            monitor-exit(r3)
            okhttp3.internal.http2.Http2Stream[] r4 = (okhttp3.internal.http2.Http2Stream[]) r4
            if (r4 != 0) goto L_0x0077
            goto L_0x0082
        L_0x0077:
            int r1 = r4.length
        L_0x0078:
            if (r0 >= r1) goto L_0x0082
            r2 = r4[r0]
            r2.close(r5, r6)     // Catch:{ IOException -> 0x007f }
        L_0x007f:
            int r0 = r0 + 1
            goto L_0x0078
        L_0x0082:
            okhttp3.internal.http2.Http2Writer r4 = r3.getWriter()     // Catch:{ IOException -> 0x0089 }
            r4.close()     // Catch:{ IOException -> 0x0089 }
        L_0x0089:
            java.net.Socket r4 = r3.getSocket$okhttp()     // Catch:{ IOException -> 0x0090 }
            r4.close()     // Catch:{ IOException -> 0x0090 }
        L_0x0090:
            okhttp3.internal.concurrent.TaskQueue r4 = r3.writerQueue
            r4.shutdown()
            okhttp3.internal.concurrent.TaskQueue r4 = r3.pushQueue
            r4.shutdown()
            okhttp3.internal.concurrent.TaskQueue r3 = r3.settingsListenerQueue
            r3.shutdown()
            return
        L_0x00a0:
            r4 = move-exception
            monitor-exit(r3)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.http2.Http2Connection.close$okhttp(okhttp3.internal.http2.ErrorCode, okhttp3.internal.http2.ErrorCode, java.io.IOException):void");
    }

    public final void pushRequestLater$okhttp(int i, @NotNull List<Header> list) {
        Intrinsics.checkNotNullParameter(list, "requestHeaders");
        synchronized (this) {
            if (this.currentPushRequests.contains(Integer.valueOf(i))) {
                writeSynResetLater$okhttp(i, ErrorCode.PROTOCOL_ERROR);
                return;
            }
            this.currentPushRequests.add(Integer.valueOf(i));
            TaskQueue taskQueue = this.pushQueue;
            taskQueue.schedule(new Http2Connection$pushRequestLater$$inlined$execute$default$1(this.connectionName + '[' + i + "] onRequest", true, this, i, list), 0);
        }
    }

    public final void pushHeadersLater$okhttp(int i, @NotNull List<Header> list, boolean z) {
        Intrinsics.checkNotNullParameter(list, "requestHeaders");
        TaskQueue taskQueue = this.pushQueue;
        taskQueue.schedule(new Http2Connection$pushHeadersLater$$inlined$execute$default$1(this.connectionName + '[' + i + "] onHeaders", true, this, i, list, z), 0);
    }

    public final void pushDataLater$okhttp(int i, @NotNull BufferedSource bufferedSource, int i2, boolean z) throws IOException {
        Intrinsics.checkNotNullParameter(bufferedSource, "source");
        Buffer buffer = new Buffer();
        long j = (long) i2;
        bufferedSource.require(j);
        bufferedSource.read(buffer, j);
        TaskQueue taskQueue = this.pushQueue;
        taskQueue.schedule(new Http2Connection$pushDataLater$$inlined$execute$default$1(this.connectionName + '[' + i + "] onData", true, this, i, buffer, i2, z), 0);
    }

    public final void pushResetLater$okhttp(int i, @NotNull ErrorCode errorCode) {
        Intrinsics.checkNotNullParameter(errorCode, "errorCode");
        TaskQueue taskQueue = this.pushQueue;
        taskQueue.schedule(new Http2Connection$pushResetLater$$inlined$execute$default$1(this.connectionName + '[' + i + "] onReset", true, this, i, errorCode), 0);
    }

    /* compiled from: Http2Connection.kt */
    public static abstract class Listener {
        @NotNull
        public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
        @NotNull
        public static final Listener REFUSE_INCOMING_STREAMS = new Http2Connection$Listener$Companion$REFUSE_INCOMING_STREAMS$1();

        public void onSettings(@NotNull Http2Connection http2Connection, @NotNull Settings settings) {
            Intrinsics.checkNotNullParameter(http2Connection, "connection");
            Intrinsics.checkNotNullParameter(settings, "settings");
        }

        public abstract void onStream(@NotNull Http2Stream http2Stream) throws IOException;

        /* compiled from: Http2Connection.kt */
        public static final class Companion {
            public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
                this();
            }

            private Companion() {
            }
        }
    }

    /* compiled from: Http2Connection.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @NotNull
        public final Settings getDEFAULT_SETTINGS() {
            return Http2Connection.DEFAULT_SETTINGS;
        }
    }

    static {
        Settings settings = new Settings();
        settings.set(7, 65535);
        settings.set(5, 16384);
        DEFAULT_SETTINGS = settings;
    }
}
