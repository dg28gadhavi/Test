package okhttp3.internal.connection;

import java.lang.ref.Reference;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import kotlin.Unit;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Address;
import okhttp3.Route;
import okhttp3.internal.Util;
import okhttp3.internal.concurrent.TaskQueue;
import okhttp3.internal.concurrent.TaskRunner;
import okhttp3.internal.connection.RealCall;
import okhttp3.internal.platform.Platform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: RealConnectionPool.kt */
public final class RealConnectionPool {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    @NotNull
    private final TaskQueue cleanupQueue;
    @NotNull
    private final RealConnectionPool$cleanupTask$1 cleanupTask = new RealConnectionPool$cleanupTask$1(this, Intrinsics.stringPlus(Util.okHttpName, " ConnectionPool"));
    @NotNull
    private final ConcurrentLinkedQueue<RealConnection> connections = new ConcurrentLinkedQueue<>();
    private final long keepAliveDurationNs;
    private final int maxIdleConnections;

    public RealConnectionPool(@NotNull TaskRunner taskRunner, int i, long j, @NotNull TimeUnit timeUnit) {
        Intrinsics.checkNotNullParameter(taskRunner, "taskRunner");
        Intrinsics.checkNotNullParameter(timeUnit, "timeUnit");
        this.maxIdleConnections = i;
        this.keepAliveDurationNs = timeUnit.toNanos(j);
        this.cleanupQueue = taskRunner.newQueue();
        if (!(j > 0)) {
            throw new IllegalArgumentException(Intrinsics.stringPlus("keepAliveDuration <= 0: ", Long.valueOf(j)).toString());
        }
    }

    public final boolean callAcquirePooledConnection(@NotNull Address address, @NotNull RealCall realCall, @Nullable List<Route> list, boolean z) {
        Intrinsics.checkNotNullParameter(address, "address");
        Intrinsics.checkNotNullParameter(realCall, "call");
        Iterator<RealConnection> it = this.connections.iterator();
        while (it.hasNext()) {
            RealConnection next = it.next();
            Intrinsics.checkNotNullExpressionValue(next, "connection");
            synchronized (next) {
                if (z) {
                    if (!next.isMultiplexed$okhttp()) {
                        Unit unit = Unit.INSTANCE;
                    }
                }
                if (next.isEligible$okhttp(address, list)) {
                    realCall.acquireConnectionNoEvents(next);
                    return true;
                }
                Unit unit2 = Unit.INSTANCE;
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:44:0x007a, code lost:
        okhttp3.internal.Util.closeQuietly(r3.socket());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0087, code lost:
        if (r10.connections.isEmpty() == false) goto L_0x008e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0089, code lost:
        r10.cleanupQueue.cancelAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x008e, code lost:
        return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final long cleanup(long r11) {
        /*
            r10 = this;
            java.util.concurrent.ConcurrentLinkedQueue<okhttp3.internal.connection.RealConnection> r0 = r10.connections
            java.util.Iterator r0 = r0.iterator()
            r1 = 0
            r2 = 0
            r3 = -9223372036854775808
            r4 = r3
            r3 = r2
            r2 = r1
        L_0x000d:
            boolean r6 = r0.hasNext()
            if (r6 == 0) goto L_0x003d
            java.lang.Object r6 = r0.next()
            okhttp3.internal.connection.RealConnection r6 = (okhttp3.internal.connection.RealConnection) r6
            java.lang.String r7 = "connection"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r6, r7)
            monitor-enter(r6)
            int r7 = r10.pruneAndGetAllocationCount(r6, r11)     // Catch:{ all -> 0x003a }
            if (r7 <= 0) goto L_0x0028
            int r2 = r2 + 1
            goto L_0x0038
        L_0x0028:
            int r1 = r1 + 1
            long r7 = r6.getIdleAtNs$okhttp()     // Catch:{ all -> 0x003a }
            long r7 = r11 - r7
            int r9 = (r7 > r4 ? 1 : (r7 == r4 ? 0 : -1))
            if (r9 <= 0) goto L_0x0036
            r3 = r6
            r4 = r7
        L_0x0036:
            kotlin.Unit r7 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x003a }
        L_0x0038:
            monitor-exit(r6)
            goto L_0x000d
        L_0x003a:
            r10 = move-exception
            monitor-exit(r6)
            throw r10
        L_0x003d:
            long r6 = r10.keepAliveDurationNs
            int r0 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r0 >= 0) goto L_0x0052
            int r0 = r10.maxIdleConnections
            if (r1 <= r0) goto L_0x0048
            goto L_0x0052
        L_0x0048:
            if (r1 <= 0) goto L_0x004c
            long r6 = r6 - r4
            return r6
        L_0x004c:
            if (r2 <= 0) goto L_0x004f
            return r6
        L_0x004f:
            r10 = -1
            return r10
        L_0x0052:
            kotlin.jvm.internal.Intrinsics.checkNotNull(r3)
            monitor-enter(r3)
            java.util.List r0 = r3.getCalls()     // Catch:{ all -> 0x008f }
            boolean r0 = r0.isEmpty()     // Catch:{ all -> 0x008f }
            r1 = 1
            r0 = r0 ^ r1
            r6 = 0
            if (r0 == 0) goto L_0x0066
            monitor-exit(r3)
            return r6
        L_0x0066:
            long r8 = r3.getIdleAtNs$okhttp()     // Catch:{ all -> 0x008f }
            long r8 = r8 + r4
            int r11 = (r8 > r11 ? 1 : (r8 == r11 ? 0 : -1))
            if (r11 == 0) goto L_0x0071
            monitor-exit(r3)
            return r6
        L_0x0071:
            r3.setNoNewExchanges(r1)     // Catch:{ all -> 0x008f }
            java.util.concurrent.ConcurrentLinkedQueue<okhttp3.internal.connection.RealConnection> r11 = r10.connections     // Catch:{ all -> 0x008f }
            r11.remove(r3)     // Catch:{ all -> 0x008f }
            monitor-exit(r3)
            java.net.Socket r11 = r3.socket()
            okhttp3.internal.Util.closeQuietly((java.net.Socket) r11)
            java.util.concurrent.ConcurrentLinkedQueue<okhttp3.internal.connection.RealConnection> r11 = r10.connections
            boolean r11 = r11.isEmpty()
            if (r11 == 0) goto L_0x008e
            okhttp3.internal.concurrent.TaskQueue r10 = r10.cleanupQueue
            r10.cancelAll()
        L_0x008e:
            return r6
        L_0x008f:
            r10 = move-exception
            monitor-exit(r3)
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.connection.RealConnectionPool.cleanup(long):long");
    }

    /* compiled from: RealConnectionPool.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }

    private final int pruneAndGetAllocationCount(RealConnection realConnection, long j) {
        if (!Util.assertionsEnabled || Thread.holdsLock(realConnection)) {
            List<Reference<RealCall>> calls = realConnection.getCalls();
            int i = 0;
            while (i < calls.size()) {
                Reference reference = calls.get(i);
                if (reference.get() != null) {
                    i++;
                } else {
                    Platform.Companion.get().logCloseableLeak("A connection to " + realConnection.route().address().url() + " was leaked. Did you forget to close a response body?", ((RealCall.CallReference) reference).getCallStackTrace());
                    calls.remove(i);
                    realConnection.setNoNewExchanges(true);
                    if (calls.isEmpty()) {
                        realConnection.setIdleAtNs$okhttp(j - this.keepAliveDurationNs);
                        return 0;
                    }
                }
            }
            return calls.size();
        }
        throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST hold lock on " + realConnection);
    }

    public final boolean connectionBecameIdle(@NotNull RealConnection realConnection) {
        Intrinsics.checkNotNullParameter(realConnection, "connection");
        if (Util.assertionsEnabled && !Thread.holdsLock(realConnection)) {
            throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST hold lock on " + realConnection);
        } else if (realConnection.getNoNewExchanges() || this.maxIdleConnections == 0) {
            realConnection.setNoNewExchanges(true);
            this.connections.remove(realConnection);
            if (this.connections.isEmpty()) {
                this.cleanupQueue.cancelAll();
            }
            return true;
        } else {
            TaskQueue.schedule$default(this.cleanupQueue, this.cleanupTask, 0, 2, (Object) null);
            return false;
        }
    }

    public final void put(@NotNull RealConnection realConnection) {
        Intrinsics.checkNotNullParameter(realConnection, "connection");
        if (!Util.assertionsEnabled || Thread.holdsLock(realConnection)) {
            this.connections.add(realConnection);
            TaskQueue.schedule$default(this.cleanupQueue, this.cleanupTask, 0, 2, (Object) null);
            return;
        }
        throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST hold lock on " + realConnection);
    }
}
