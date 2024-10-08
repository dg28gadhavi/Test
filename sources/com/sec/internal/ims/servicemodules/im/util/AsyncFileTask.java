package com.sec.internal.ims.servicemodules.im.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import java.util.ArrayDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AsyncFileTask<Result> {
    private static final int FTHTTP_POOL_SIZE = 3;
    private static final int KEEP_ALIVE = 1;
    private static final int MAXIMUM_POOL_SIZE = 3;
    public static final Executor SERIAL_EXECUTOR = new SerialExecutor();
    public static final Executor THREAD_POOL_EXECUTOR;
    public static final Executor THREAD_THUMBNAIL_POOL_EXECUTOR;
    private static final ThreadFactory sThreadFactory;
    private FutureTask<Result> mFuture;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private final AtomicBoolean mIsCancelled = new AtomicBoolean();
    private State mState = State.NOT_STARTED;
    private final AtomicBoolean mTaskInvoked = new AtomicBoolean();
    private Callable<Result> mWorker;

    public enum State {
        NOT_STARTED,
        STARTED,
        FINISHED
    }

    /* access modifiers changed from: protected */
    public abstract Result doInBackground() throws Exception;

    static {
        AnonymousClass1 r8 = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, "BaseTask #" + this.mCount.getAndIncrement());
            }
        };
        sThreadFactory = r8;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        AnonymousClass1 r7 = r8;
        THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(3, 3, 1, timeUnit, new LinkedBlockingQueue(128), r7);
        THREAD_THUMBNAIL_POOL_EXECUTOR = new ThreadPoolExecutor(3, 3, 1, timeUnit, new LinkedBlockingQueue(128), r7);
    }

    protected AsyncFileTask(Looper looper) {
        this.mHandler = new Handler(looper);
        this.mWorker = new AsyncFileTask$$ExternalSyntheticLambda0(this);
        this.mFuture = new FutureTask<Result>(this.mWorker) {
            /* access modifiers changed from: protected */
            public void done() {
                super.done();
                AsyncFileTask.this.mHandler.post(new AsyncFileTask$2$$ExternalSyntheticLambda0(this));
            }

            /* access modifiers changed from: private */
            public /* synthetic */ void lambda$done$0() {
                try {
                    AsyncFileTask.this.postIfNotInvoked(get());
                } catch (InterruptedException | CancellationException | ExecutionException unused) {
                    AsyncFileTask.this.postIfNotInvoked(null);
                }
            }
        };
    }

    /* access modifiers changed from: private */
    public /* synthetic */ Object lambda$new$0() throws Exception {
        this.mTaskInvoked.set(true);
        try {
            Process.setThreadPriority(10);
            Object doInBackground = doInBackground();
            handleResult(doInBackground);
            return doInBackground;
        } catch (Throwable th) {
            handleResult((Object) null);
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public void postIfNotInvoked(Result result) {
        if (!this.mTaskInvoked.get()) {
            handleResult(result);
        }
    }

    private void handleResult(Result result) {
        this.mHandler.post(new AsyncFileTask$$ExternalSyntheticLambda1(this, result));
    }

    public void execute(Executor executor) {
        onPreExecute();
        executor.execute(this.mFuture);
    }

    /* access modifiers changed from: protected */
    public void onPreExecute() {
        this.mState = State.STARTED;
    }

    /* access modifiers changed from: protected */
    /* renamed from: onPostExecute */
    public void lambda$handleResult$1(Result result) {
        this.mState = State.FINISHED;
    }

    public final void cancel(boolean z) {
        this.mIsCancelled.set(true);
        this.mFuture.cancel(z);
    }

    /* access modifiers changed from: protected */
    public final boolean isCancelled() {
        return this.mIsCancelled.get();
    }

    public State getState() {
        return this.mState;
    }

    private static class SerialExecutor implements Executor {
        private Runnable mActive;
        private final ArrayDeque<Runnable> mTasks;

        private SerialExecutor() {
            this.mTasks = new ArrayDeque<>();
        }

        public synchronized void execute(Runnable runnable) {
            this.mTasks.offer(new AsyncFileTask$SerialExecutor$$ExternalSyntheticLambda0(this, runnable));
            if (this.mActive == null) {
                scheduleNext();
            }
        }

        /* access modifiers changed from: private */
        public /* synthetic */ void lambda$execute$0(Runnable runnable) {
            try {
                runnable.run();
            } finally {
                scheduleNext();
            }
        }

        /* access modifiers changed from: protected */
        public synchronized void scheduleNext() {
            Runnable poll = this.mTasks.poll();
            this.mActive = poll;
            if (poll != null) {
                AsyncFileTask.THREAD_POOL_EXECUTOR.execute(poll);
            }
        }
    }
}
