package okio;

import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;
import kotlin.Unit;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: AsyncTimeout.kt */
public class AsyncTimeout extends Timeout {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    /* access modifiers changed from: private */
    public static final long IDLE_TIMEOUT_MILLIS;
    /* access modifiers changed from: private */
    public static final long IDLE_TIMEOUT_NANOS;
    /* access modifiers changed from: private */
    @Nullable
    public static AsyncTimeout head;
    /* access modifiers changed from: private */
    public boolean inQueue;
    /* access modifiers changed from: private */
    @Nullable
    public AsyncTimeout next;
    /* access modifiers changed from: private */
    public long timeoutAt;

    /* access modifiers changed from: protected */
    public void timedOut() {
    }

    /* compiled from: AsyncTimeout.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        /* access modifiers changed from: private */
        public final boolean cancelScheduledTimeout(AsyncTimeout asyncTimeout) {
            synchronized (AsyncTimeout.class) {
                if (!asyncTimeout.inQueue) {
                    return false;
                }
                asyncTimeout.inQueue = false;
                for (AsyncTimeout access$getHead$cp = AsyncTimeout.head; access$getHead$cp != null; access$getHead$cp = access$getHead$cp.next) {
                    if (access$getHead$cp.next == asyncTimeout) {
                        access$getHead$cp.next = asyncTimeout.next;
                        asyncTimeout.next = null;
                        return false;
                    }
                }
                return true;
            }
        }

        /* access modifiers changed from: private */
        public final void scheduleTimeout(AsyncTimeout asyncTimeout, long j, boolean z) {
            synchronized (AsyncTimeout.class) {
                if (!asyncTimeout.inQueue) {
                    asyncTimeout.inQueue = true;
                    if (AsyncTimeout.head == null) {
                        AsyncTimeout.head = new AsyncTimeout();
                        new Watchdog().start();
                    }
                    long nanoTime = System.nanoTime();
                    int i = (j > 0 ? 1 : (j == 0 ? 0 : -1));
                    if (i != 0 && z) {
                        asyncTimeout.timeoutAt = Math.min(j, asyncTimeout.deadlineNanoTime() - nanoTime) + nanoTime;
                    } else if (i != 0) {
                        asyncTimeout.timeoutAt = j + nanoTime;
                    } else if (z) {
                        asyncTimeout.timeoutAt = asyncTimeout.deadlineNanoTime();
                    } else {
                        throw new AssertionError();
                    }
                    long access$remainingNanos = asyncTimeout.remainingNanos(nanoTime);
                    AsyncTimeout access$getHead$cp = AsyncTimeout.head;
                    Intrinsics.checkNotNull(access$getHead$cp);
                    while (true) {
                        if (access$getHead$cp.next == null) {
                            break;
                        }
                        AsyncTimeout access$getNext$p = access$getHead$cp.next;
                        Intrinsics.checkNotNull(access$getNext$p);
                        if (access$remainingNanos < access$getNext$p.remainingNanos(nanoTime)) {
                            break;
                        }
                        access$getHead$cp = access$getHead$cp.next;
                        Intrinsics.checkNotNull(access$getHead$cp);
                    }
                    asyncTimeout.next = access$getHead$cp.next;
                    access$getHead$cp.next = asyncTimeout;
                    if (access$getHead$cp == AsyncTimeout.head) {
                        AsyncTimeout.class.notify();
                    }
                    Unit unit = Unit.INSTANCE;
                } else {
                    throw new IllegalStateException("Unbalanced enter/exit".toString());
                }
            }
        }

        private Companion() {
        }

        @Nullable
        public final AsyncTimeout awaitTimeout$okio() throws InterruptedException {
            AsyncTimeout access$getHead$cp = AsyncTimeout.head;
            Intrinsics.checkNotNull(access$getHead$cp);
            AsyncTimeout access$getNext$p = access$getHead$cp.next;
            Class<AsyncTimeout> cls = AsyncTimeout.class;
            if (access$getNext$p == null) {
                long nanoTime = System.nanoTime();
                cls.wait(AsyncTimeout.IDLE_TIMEOUT_MILLIS);
                AsyncTimeout access$getHead$cp2 = AsyncTimeout.head;
                Intrinsics.checkNotNull(access$getHead$cp2);
                if (access$getHead$cp2.next != null || System.nanoTime() - nanoTime < AsyncTimeout.IDLE_TIMEOUT_NANOS) {
                    return null;
                }
                return AsyncTimeout.head;
            }
            long access$remainingNanos = access$getNext$p.remainingNanos(System.nanoTime());
            if (access$remainingNanos > 0) {
                long j = access$remainingNanos / 1000000;
                cls.wait(j, (int) (access$remainingNanos - (1000000 * j)));
                return null;
            }
            AsyncTimeout access$getHead$cp3 = AsyncTimeout.head;
            Intrinsics.checkNotNull(access$getHead$cp3);
            access$getHead$cp3.next = access$getNext$p.next;
            access$getNext$p.next = null;
            return access$getNext$p;
        }
    }

    public final void enter() {
        long timeoutNanos = timeoutNanos();
        boolean hasDeadline = hasDeadline();
        if (timeoutNanos != 0 || hasDeadline) {
            Companion.scheduleTimeout(this, timeoutNanos, hasDeadline);
        }
    }

    public final boolean exit() {
        return Companion.cancelScheduledTimeout(this);
    }

    /* access modifiers changed from: private */
    public final long remainingNanos(long j) {
        return this.timeoutAt - j;
    }

    @NotNull
    public final Sink sink(@NotNull Sink sink) {
        Intrinsics.checkNotNullParameter(sink, "sink");
        return new AsyncTimeout$sink$1(this, sink);
    }

    @NotNull
    public final Source source(@NotNull Source source) {
        Intrinsics.checkNotNullParameter(source, "source");
        return new AsyncTimeout$source$1(this, source);
    }

    @NotNull
    public final IOException access$newTimeoutException(@Nullable IOException iOException) {
        return newTimeoutException(iOException);
    }

    /* access modifiers changed from: protected */
    @NotNull
    public IOException newTimeoutException(@Nullable IOException iOException) {
        InterruptedIOException interruptedIOException = new InterruptedIOException(EucTestIntent.Extras.TIMEOUT);
        if (iOException != null) {
            interruptedIOException.initCause(iOException);
        }
        return interruptedIOException;
    }

    /* compiled from: AsyncTimeout.kt */
    private static final class Watchdog extends Thread {
        public Watchdog() {
            super("Okio Watchdog");
            setDaemon(true);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0018, code lost:
            if (r0 != null) goto L_0x001b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x001b, code lost:
            r0.timedOut();
         */
        /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r2 = this;
            L_0x0000:
                java.lang.Class<okio.AsyncTimeout> r2 = okio.AsyncTimeout.class
                monitor-enter(r2)     // Catch:{ InterruptedException -> 0x0000 }
                okio.AsyncTimeout$Companion r0 = okio.AsyncTimeout.Companion     // Catch:{ all -> 0x001f }
                okio.AsyncTimeout r0 = r0.awaitTimeout$okio()     // Catch:{ all -> 0x001f }
                okio.AsyncTimeout r1 = okio.AsyncTimeout.head     // Catch:{ all -> 0x001f }
                if (r0 != r1) goto L_0x0015
                r0 = 0
                okio.AsyncTimeout.head = r0     // Catch:{ all -> 0x001f }
                monitor-exit(r2)     // Catch:{ InterruptedException -> 0x0000 }
                return
            L_0x0015:
                kotlin.Unit r1 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x001f }
                monitor-exit(r2)     // Catch:{ InterruptedException -> 0x0000 }
                if (r0 != 0) goto L_0x001b
                goto L_0x0000
            L_0x001b:
                r0.timedOut()     // Catch:{ InterruptedException -> 0x0000 }
                goto L_0x0000
            L_0x001f:
                r0 = move-exception
                monitor-exit(r2)     // Catch:{ InterruptedException -> 0x0000 }
                throw r0     // Catch:{ InterruptedException -> 0x0000 }
            */
            throw new UnsupportedOperationException("Method not decompiled: okio.AsyncTimeout.Watchdog.run():void");
        }
    }

    static {
        long millis = TimeUnit.SECONDS.toMillis(60);
        IDLE_TIMEOUT_MILLIS = millis;
        IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(millis);
    }
}
