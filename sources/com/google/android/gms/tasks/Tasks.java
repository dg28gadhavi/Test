package com.google.android.gms.tasks;

import com.google.android.gms.common.internal.zzbq;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Tasks {

    static final class zza implements zzb {
        private final CountDownLatch zzapc;

        private zza() {
            this.zzapc = new CountDownLatch(1);
        }

        /* synthetic */ zza(zzq zzq) {
            this();
        }

        public final void await() throws InterruptedException {
            this.zzapc.await();
        }

        public final boolean await(long j, TimeUnit timeUnit) throws InterruptedException {
            return this.zzapc.await(j, timeUnit);
        }

        public final void onFailure(Exception exc) {
            this.zzapc.countDown();
        }

        public final void onSuccess(Object obj) {
            this.zzapc.countDown();
        }
    }

    interface zzb extends OnFailureListener, OnSuccessListener<Object> {
    }

    public static <TResult> TResult await(Task<TResult> task) throws ExecutionException, InterruptedException {
        zzbq.zzgw("Must not be called on the main application thread");
        zzbq.checkNotNull(task, "Task must not be null");
        if (task.isComplete()) {
            return zzc(task);
        }
        zza zza2 = new zza((zzq) null);
        zza(task, zza2);
        zza2.await();
        return zzc(task);
    }

    public static <TResult> TResult await(Task<TResult> task, long j, TimeUnit timeUnit) throws ExecutionException, InterruptedException, TimeoutException {
        zzbq.zzgw("Must not be called on the main application thread");
        zzbq.checkNotNull(task, "Task must not be null");
        zzbq.checkNotNull(timeUnit, "TimeUnit must not be null");
        if (task.isComplete()) {
            return zzc(task);
        }
        zza zza2 = new zza((zzq) null);
        zza(task, zza2);
        if (zza2.await(j, timeUnit)) {
            return zzc(task);
        }
        throw new TimeoutException("Timed out waiting for Task");
    }

    private static void zza(Task<?> task, zzb zzb2) {
        Executor executor = TaskExecutors.zzlem;
        task.addOnSuccessListener(executor, zzb2);
        task.addOnFailureListener(executor, zzb2);
    }

    private static <TResult> TResult zzc(Task<TResult> task) throws ExecutionException {
        if (task.isSuccessful()) {
            return task.getResult();
        }
        throw new ExecutionException(task.getException());
    }
}
