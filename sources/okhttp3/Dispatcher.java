package okhttp3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.Util;
import okhttp3.internal.connection.RealCall;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Dispatcher.kt */
public final class Dispatcher {
    @Nullable
    private ExecutorService executorServiceOrNull;
    @Nullable
    private Runnable idleCallback;
    private int maxRequests = 64;
    private int maxRequestsPerHost = 5;
    @NotNull
    private final ArrayDeque<RealCall.AsyncCall> readyAsyncCalls = new ArrayDeque<>();
    @NotNull
    private final ArrayDeque<RealCall.AsyncCall> runningAsyncCalls = new ArrayDeque<>();
    @NotNull
    private final ArrayDeque<RealCall> runningSyncCalls = new ArrayDeque<>();

    public final synchronized int getMaxRequests() {
        return this.maxRequests;
    }

    public final synchronized int getMaxRequestsPerHost() {
        return this.maxRequestsPerHost;
    }

    @Nullable
    public final synchronized Runnable getIdleCallback() {
        return this.idleCallback;
    }

    @NotNull
    public final synchronized ExecutorService executorService() {
        ExecutorService executorService;
        if (this.executorServiceOrNull == null) {
            this.executorServiceOrNull = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory(Intrinsics.stringPlus(Util.okHttpName, " Dispatcher"), false));
        }
        executorService = this.executorServiceOrNull;
        Intrinsics.checkNotNull(executorService);
        return executorService;
    }

    public final void enqueue$okhttp(@NotNull RealCall.AsyncCall asyncCall) {
        RealCall.AsyncCall findExistingCallWithHost;
        Intrinsics.checkNotNullParameter(asyncCall, "call");
        synchronized (this) {
            this.readyAsyncCalls.add(asyncCall);
            if (!asyncCall.getCall().getForWebSocket() && (findExistingCallWithHost = findExistingCallWithHost(asyncCall.getHost())) != null) {
                asyncCall.reuseCallsPerHostFrom(findExistingCallWithHost);
            }
            Unit unit = Unit.INSTANCE;
        }
        promoteAndExecute();
    }

    private final RealCall.AsyncCall findExistingCallWithHost(String str) {
        Iterator<RealCall.AsyncCall> it = this.runningAsyncCalls.iterator();
        while (it.hasNext()) {
            RealCall.AsyncCall next = it.next();
            if (Intrinsics.areEqual(next.getHost(), str)) {
                return next;
            }
        }
        Iterator<RealCall.AsyncCall> it2 = this.readyAsyncCalls.iterator();
        while (it2.hasNext()) {
            RealCall.AsyncCall next2 = it2.next();
            if (Intrinsics.areEqual(next2.getHost(), str)) {
                return next2;
            }
        }
        return null;
    }

    public final synchronized void executed$okhttp(@NotNull RealCall realCall) {
        Intrinsics.checkNotNullParameter(realCall, "call");
        this.runningSyncCalls.add(realCall);
    }

    public final void finished$okhttp(@NotNull RealCall.AsyncCall asyncCall) {
        Intrinsics.checkNotNullParameter(asyncCall, "call");
        asyncCall.getCallsPerHost().decrementAndGet();
        finished(this.runningAsyncCalls, asyncCall);
    }

    public final void finished$okhttp(@NotNull RealCall realCall) {
        Intrinsics.checkNotNullParameter(realCall, "call");
        finished(this.runningSyncCalls, realCall);
    }

    private final <T> void finished(Deque<T> deque, T t) {
        Runnable idleCallback2;
        synchronized (this) {
            if (deque.remove(t)) {
                idleCallback2 = getIdleCallback();
                Unit unit = Unit.INSTANCE;
            } else {
                throw new AssertionError("Call wasn't in-flight!");
            }
        }
        if (!promoteAndExecute() && idleCallback2 != null) {
            idleCallback2.run();
        }
    }

    public final synchronized int runningCallsCount() {
        return this.runningAsyncCalls.size() + this.runningSyncCalls.size();
    }

    private final boolean promoteAndExecute() {
        int i;
        boolean z;
        if (!Util.assertionsEnabled || !Thread.holdsLock(this)) {
            ArrayList arrayList = new ArrayList();
            synchronized (this) {
                Iterator<RealCall.AsyncCall> it = this.readyAsyncCalls.iterator();
                Intrinsics.checkNotNullExpressionValue(it, "readyAsyncCalls.iterator()");
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    RealCall.AsyncCall next = it.next();
                    if (this.runningAsyncCalls.size() >= getMaxRequests()) {
                        break;
                    } else if (next.getCallsPerHost().get() < getMaxRequestsPerHost()) {
                        it.remove();
                        next.getCallsPerHost().incrementAndGet();
                        Intrinsics.checkNotNullExpressionValue(next, "asyncCall");
                        arrayList.add(next);
                        this.runningAsyncCalls.add(next);
                    }
                }
                z = runningCallsCount() > 0;
                Unit unit = Unit.INSTANCE;
            }
            int size = arrayList.size();
            for (i = 0; i < size; i++) {
                ((RealCall.AsyncCall) arrayList.get(i)).executeOn(executorService());
            }
            return z;
        }
        throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST NOT hold lock on " + this);
    }
}
