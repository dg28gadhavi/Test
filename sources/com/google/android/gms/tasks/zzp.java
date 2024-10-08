package com.google.android.gms.tasks;

import com.google.android.gms.common.internal.zzbq;
import java.util.concurrent.Executor;

final class zzp<TResult> extends Task<TResult> {
    private final Object mLock = new Object();
    private final zzn<TResult> zzlen = new zzn<>();
    private boolean zzleo;
    private TResult zzlep;
    private Exception zzleq;

    zzp() {
    }

    private final void zzbld() {
        zzbq.zza(this.zzleo, "Task is not yet complete");
    }

    private final void zzble() {
        zzbq.zza(!this.zzleo, "Task is already complete");
    }

    private final void zzblf() {
        synchronized (this.mLock) {
            if (this.zzleo) {
                this.zzlen.zzb(this);
            }
        }
    }

    public final Task<TResult> addOnFailureListener(Executor executor, OnFailureListener onFailureListener) {
        this.zzlen.zza(new zzg(executor, onFailureListener));
        zzblf();
        return this;
    }

    public final Task<TResult> addOnSuccessListener(Executor executor, OnSuccessListener<? super TResult> onSuccessListener) {
        this.zzlen.zza(new zzi(executor, onSuccessListener));
        zzblf();
        return this;
    }

    public final Exception getException() {
        Exception exc;
        synchronized (this.mLock) {
            exc = this.zzleq;
        }
        return exc;
    }

    public final TResult getResult() {
        TResult tresult;
        synchronized (this.mLock) {
            zzbld();
            if (this.zzleq == null) {
                tresult = this.zzlep;
            } else {
                throw new RuntimeExecutionException(this.zzleq);
            }
        }
        return tresult;
    }

    public final boolean isComplete() {
        boolean z;
        synchronized (this.mLock) {
            z = this.zzleo;
        }
        return z;
    }

    public final boolean isSuccessful() {
        boolean z;
        synchronized (this.mLock) {
            z = this.zzleo && this.zzleq == null;
        }
        return z;
    }

    public final void setException(Exception exc) {
        zzbq.checkNotNull(exc, "Exception must not be null");
        synchronized (this.mLock) {
            zzble();
            this.zzleo = true;
            this.zzleq = exc;
        }
        this.zzlen.zzb(this);
    }

    public final void setResult(TResult tresult) {
        synchronized (this.mLock) {
            zzble();
            this.zzleo = true;
            this.zzlep = tresult;
        }
        this.zzlen.zzb(this);
    }
}
