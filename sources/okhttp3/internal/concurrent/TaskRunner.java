package okhttp3.internal.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import kotlin.Unit;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.Util;
import org.jetbrains.annotations.NotNull;

/* compiled from: TaskRunner.kt */
public final class TaskRunner {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    @NotNull
    public static final TaskRunner INSTANCE = new TaskRunner(new RealBackend(Util.threadFactory(Intrinsics.stringPlus(Util.okHttpName, " TaskRunner"), true)));
    /* access modifiers changed from: private */
    @NotNull
    public static final Logger logger;
    @NotNull
    private final Backend backend;
    @NotNull
    private final List<TaskQueue> busyQueues = new ArrayList();
    private boolean coordinatorWaiting;
    private long coordinatorWakeUpAt;
    private int nextQueueName = 10000;
    @NotNull
    private final List<TaskQueue> readyQueues = new ArrayList();
    @NotNull
    private final Runnable runnable = new TaskRunner$runnable$1(this);

    /* compiled from: TaskRunner.kt */
    public interface Backend {
        void coordinatorNotify(@NotNull TaskRunner taskRunner);

        void coordinatorWait(@NotNull TaskRunner taskRunner, long j);

        void execute(@NotNull Runnable runnable);

        long nanoTime();
    }

    public TaskRunner(@NotNull Backend backend2) {
        Intrinsics.checkNotNullParameter(backend2, "backend");
        this.backend = backend2;
    }

    @NotNull
    public final Backend getBackend() {
        return this.backend;
    }

    @NotNull
    public final TaskQueue newQueue() {
        int i;
        synchronized (this) {
            i = this.nextQueueName;
            this.nextQueueName = i + 1;
        }
        return new TaskQueue(this, Intrinsics.stringPlus("Q", Integer.valueOf(i)));
    }

    public final void cancelAll() {
        int size = this.busyQueues.size() - 1;
        if (size >= 0) {
            while (true) {
                int i = size - 1;
                this.busyQueues.get(size).cancelAllAndDecide$okhttp();
                if (i < 0) {
                    break;
                }
                size = i;
            }
        }
        int size2 = this.readyQueues.size() - 1;
        if (size2 >= 0) {
            while (true) {
                int i2 = size2 - 1;
                TaskQueue taskQueue = this.readyQueues.get(size2);
                taskQueue.cancelAllAndDecide$okhttp();
                if (taskQueue.getFutureTasks$okhttp().isEmpty()) {
                    this.readyQueues.remove(size2);
                }
                if (i2 >= 0) {
                    size2 = i2;
                } else {
                    return;
                }
            }
        }
    }

    /* compiled from: TaskRunner.kt */
    public static final class RealBackend implements Backend {
        @NotNull
        private final ThreadPoolExecutor executor;

        public RealBackend(@NotNull ThreadFactory threadFactory) {
            Intrinsics.checkNotNullParameter(threadFactory, "threadFactory");
            this.executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), threadFactory);
        }

        public long nanoTime() {
            return System.nanoTime();
        }

        public void coordinatorWait(@NotNull TaskRunner taskRunner, long j) throws InterruptedException {
            Intrinsics.checkNotNullParameter(taskRunner, "taskRunner");
            long j2 = j / 1000000;
            long j3 = j - (1000000 * j2);
            if (j2 > 0 || j > 0) {
                taskRunner.wait(j2, (int) j3);
            }
        }

        public void execute(@NotNull Runnable runnable) {
            Intrinsics.checkNotNullParameter(runnable, "runnable");
            this.executor.execute(runnable);
        }

        public void coordinatorNotify(@NotNull TaskRunner taskRunner) {
            Intrinsics.checkNotNullParameter(taskRunner, "taskRunner");
            taskRunner.notify();
        }
    }

    /* compiled from: TaskRunner.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @NotNull
        public final Logger getLogger() {
            return TaskRunner.logger;
        }
    }

    static {
        Logger logger2 = Logger.getLogger(TaskRunner.class.getName());
        Intrinsics.checkNotNullExpressionValue(logger2, "getLogger(TaskRunner::class.java.name)");
        logger = logger2;
    }

    private final void afterRun(Task task, long j) {
        if (!Util.assertionsEnabled || Thread.holdsLock(this)) {
            TaskQueue queue$okhttp = task.getQueue$okhttp();
            Intrinsics.checkNotNull(queue$okhttp);
            if (queue$okhttp.getActiveTask$okhttp() == task) {
                boolean cancelActiveTask$okhttp = queue$okhttp.getCancelActiveTask$okhttp();
                queue$okhttp.setCancelActiveTask$okhttp(false);
                queue$okhttp.setActiveTask$okhttp((Task) null);
                this.busyQueues.remove(queue$okhttp);
                if (j != -1 && !cancelActiveTask$okhttp && !queue$okhttp.getShutdown$okhttp()) {
                    queue$okhttp.scheduleAndDecide$okhttp(task, j, true);
                }
                if (!queue$okhttp.getFutureTasks$okhttp().isEmpty()) {
                    this.readyQueues.add(queue$okhttp);
                    return;
                }
                return;
            }
            throw new IllegalStateException("Check failed.".toString());
        }
        throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST hold lock on " + this);
    }

    private final void beforeRun(Task task) {
        if (!Util.assertionsEnabled || Thread.holdsLock(this)) {
            task.setNextExecuteNanoTime$okhttp(-1);
            TaskQueue queue$okhttp = task.getQueue$okhttp();
            Intrinsics.checkNotNull(queue$okhttp);
            queue$okhttp.getFutureTasks$okhttp().remove(task);
            this.readyQueues.remove(queue$okhttp);
            queue$okhttp.setActiveTask$okhttp(task);
            this.busyQueues.add(queue$okhttp);
            return;
        }
        throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST hold lock on " + this);
    }

    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x00be */
    @org.jetbrains.annotations.Nullable
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final okhttp3.internal.concurrent.Task awaitTaskToRun() {
        /*
            r14 = this;
            boolean r0 = okhttp3.internal.Util.assertionsEnabled
            if (r0 == 0) goto L_0x0032
            boolean r0 = java.lang.Thread.holdsLock(r14)
            if (r0 == 0) goto L_0x000b
            goto L_0x0032
        L_0x000b:
            java.lang.AssertionError r0 = new java.lang.AssertionError
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "Thread "
            r1.append(r2)
            java.lang.Thread r2 = java.lang.Thread.currentThread()
            java.lang.String r2 = r2.getName()
            r1.append(r2)
            java.lang.String r2 = " MUST hold lock on "
            r1.append(r2)
            r1.append(r14)
            java.lang.String r14 = r1.toString()
            r0.<init>(r14)
            throw r0
        L_0x0032:
            java.util.List<okhttp3.internal.concurrent.TaskQueue> r0 = r14.readyQueues
            boolean r0 = r0.isEmpty()
            r1 = 0
            if (r0 == 0) goto L_0x003c
            return r1
        L_0x003c:
            okhttp3.internal.concurrent.TaskRunner$Backend r0 = r14.backend
            long r2 = r0.nanoTime()
            java.util.List<okhttp3.internal.concurrent.TaskQueue> r0 = r14.readyQueues
            java.util.Iterator r0 = r0.iterator()
            r4 = 9223372036854775807(0x7fffffffffffffff, double:NaN)
            r6 = r1
        L_0x004e:
            boolean r7 = r0.hasNext()
            r8 = 1
            r9 = 0
            if (r7 == 0) goto L_0x0080
            java.lang.Object r7 = r0.next()
            okhttp3.internal.concurrent.TaskQueue r7 = (okhttp3.internal.concurrent.TaskQueue) r7
            java.util.List r7 = r7.getFutureTasks$okhttp()
            java.lang.Object r7 = r7.get(r9)
            okhttp3.internal.concurrent.Task r7 = (okhttp3.internal.concurrent.Task) r7
            long r10 = r7.getNextExecuteNanoTime$okhttp()
            long r10 = r10 - r2
            r12 = 0
            long r10 = java.lang.Math.max(r12, r10)
            int r12 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1))
            if (r12 <= 0) goto L_0x007a
            long r4 = java.lang.Math.min(r10, r4)
            goto L_0x004e
        L_0x007a:
            if (r6 == 0) goto L_0x007e
            r0 = r8
            goto L_0x0081
        L_0x007e:
            r6 = r7
            goto L_0x004e
        L_0x0080:
            r0 = r9
        L_0x0081:
            if (r6 == 0) goto L_0x009d
            r14.beforeRun(r6)
            if (r0 != 0) goto L_0x0095
            boolean r0 = r14.coordinatorWaiting
            if (r0 != 0) goto L_0x009c
            java.util.List<okhttp3.internal.concurrent.TaskQueue> r0 = r14.readyQueues
            boolean r0 = r0.isEmpty()
            r0 = r0 ^ r8
            if (r0 == 0) goto L_0x009c
        L_0x0095:
            okhttp3.internal.concurrent.TaskRunner$Backend r0 = r14.backend
            java.lang.Runnable r14 = r14.runnable
            r0.execute(r14)
        L_0x009c:
            return r6
        L_0x009d:
            boolean r0 = r14.coordinatorWaiting
            if (r0 == 0) goto L_0x00ae
            long r6 = r14.coordinatorWakeUpAt
            long r6 = r6 - r2
            int r0 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r0 >= 0) goto L_0x00ad
            okhttp3.internal.concurrent.TaskRunner$Backend r0 = r14.backend
            r0.coordinatorNotify(r14)
        L_0x00ad:
            return r1
        L_0x00ae:
            r14.coordinatorWaiting = r8
            long r2 = r2 + r4
            r14.coordinatorWakeUpAt = r2
            okhttp3.internal.concurrent.TaskRunner$Backend r0 = r14.backend     // Catch:{ InterruptedException -> 0x00be }
            r0.coordinatorWait(r14, r4)     // Catch:{ InterruptedException -> 0x00be }
        L_0x00b8:
            r14.coordinatorWaiting = r9
            goto L_0x0032
        L_0x00bc:
            r0 = move-exception
            goto L_0x00c2
        L_0x00be:
            r14.cancelAll()     // Catch:{ all -> 0x00bc }
            goto L_0x00b8
        L_0x00c2:
            r14.coordinatorWaiting = r9
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.concurrent.TaskRunner.awaitTaskToRun():okhttp3.internal.concurrent.Task");
    }

    public final void kickCoordinator$okhttp(@NotNull TaskQueue taskQueue) {
        Intrinsics.checkNotNullParameter(taskQueue, "taskQueue");
        if (!Util.assertionsEnabled || Thread.holdsLock(this)) {
            if (taskQueue.getActiveTask$okhttp() == null) {
                if (!taskQueue.getFutureTasks$okhttp().isEmpty()) {
                    Util.addIfAbsent(this.readyQueues, taskQueue);
                } else {
                    this.readyQueues.remove(taskQueue);
                }
            }
            if (this.coordinatorWaiting) {
                this.backend.coordinatorNotify(this);
            } else {
                this.backend.execute(this.runnable);
            }
        } else {
            throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST hold lock on " + this);
        }
    }

    /* access modifiers changed from: private */
    public final void runTask(Task task) {
        if (!Util.assertionsEnabled || !Thread.holdsLock(this)) {
            Thread currentThread = Thread.currentThread();
            String name = currentThread.getName();
            currentThread.setName(task.getName());
            try {
                long runOnce = task.runOnce();
                synchronized (this) {
                    afterRun(task, runOnce);
                    Unit unit = Unit.INSTANCE;
                }
                currentThread.setName(name);
            } catch (Throwable th) {
                synchronized (this) {
                    afterRun(task, -1);
                    Unit unit2 = Unit.INSTANCE;
                    currentThread.setName(name);
                    throw th;
                }
            }
        } else {
            throw new AssertionError("Thread " + Thread.currentThread().getName() + " MUST NOT hold lock on " + this);
        }
    }
}
