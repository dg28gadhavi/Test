package com.google.android.gms.tasks;

import java.util.ArrayDeque;
import java.util.Queue;

final class zzn<TResult> {
    private final Object mLock = new Object();
    private Queue<zzm<TResult>> zzlej;
    private boolean zzlek;

    zzn() {
    }

    public final void zza(zzm<TResult> zzm) {
        synchronized (this.mLock) {
            if (this.zzlej == null) {
                this.zzlej = new ArrayDeque();
            }
            this.zzlej.add(zzm);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0010, code lost:
        r1 = r2.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0012, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r0 = r2.zzlej.poll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001b, code lost:
        if (r0 != null) goto L_0x0022;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001d, code lost:
        r2.zzlek = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0020, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0021, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0022, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0023, code lost:
        r0.onComplete(r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void zzb(com.google.android.gms.tasks.Task<TResult> r3) {
        /*
            r2 = this;
            java.lang.Object r0 = r2.mLock
            monitor-enter(r0)
            java.util.Queue<com.google.android.gms.tasks.zzm<TResult>> r1 = r2.zzlej     // Catch:{ all -> 0x002c }
            if (r1 == 0) goto L_0x002a
            boolean r1 = r2.zzlek     // Catch:{ all -> 0x002c }
            if (r1 == 0) goto L_0x000c
            goto L_0x002a
        L_0x000c:
            r1 = 1
            r2.zzlek = r1     // Catch:{ all -> 0x002c }
            monitor-exit(r0)     // Catch:{ all -> 0x002c }
        L_0x0010:
            java.lang.Object r1 = r2.mLock
            monitor-enter(r1)
            java.util.Queue<com.google.android.gms.tasks.zzm<TResult>> r0 = r2.zzlej     // Catch:{ all -> 0x0027 }
            java.lang.Object r0 = r0.poll()     // Catch:{ all -> 0x0027 }
            com.google.android.gms.tasks.zzm r0 = (com.google.android.gms.tasks.zzm) r0     // Catch:{ all -> 0x0027 }
            if (r0 != 0) goto L_0x0022
            r3 = 0
            r2.zzlek = r3     // Catch:{ all -> 0x0027 }
            monitor-exit(r1)     // Catch:{ all -> 0x0027 }
            return
        L_0x0022:
            monitor-exit(r1)     // Catch:{ all -> 0x0027 }
            r0.onComplete(r3)
            goto L_0x0010
        L_0x0027:
            r2 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0027 }
            throw r2
        L_0x002a:
            monitor-exit(r0)     // Catch:{ all -> 0x002c }
            return
        L_0x002c:
            r2 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x002c }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.tasks.zzn.zzb(com.google.android.gms.tasks.Task):void");
    }
}
