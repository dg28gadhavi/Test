package com.google.firebase.iid;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import com.google.android.gms.common.stats.zza;
import java.util.ArrayDeque;
import java.util.Queue;

final class zzm implements ServiceConnection {
    int state;
    final Messenger zzing;
    final Queue<zzt<?>> zzini;
    final SparseArray<zzt<?>> zzinj;
    zzr zzoky;
    final /* synthetic */ zzk zzokz;

    private zzm(zzk zzk) {
        this.zzokz = zzk;
        this.state = 0;
        this.zzing = new Messenger(new Handler(Looper.getMainLooper(), new zzn(this)));
        this.zzini = new ArrayDeque();
        this.zzinj = new SparseArray<>();
    }

    private final void zza(zzu zzu) {
        for (zzt zzb : this.zzini) {
            zzb.zzb(zzu);
        }
        this.zzini.clear();
        for (int i = 0; i < this.zzinj.size(); i++) {
            this.zzinj.valueAt(i).zzb(zzu);
        }
        this.zzinj.clear();
    }

    private final void zzawt() {
        this.zzokz.zzind.execute(new zzp(this));
    }

    public final synchronized void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (Log.isLoggable("MessengerIpcClient", 2)) {
            Log.v("MessengerIpcClient", "Service connected");
        }
        if (iBinder == null) {
            zzl(0, "Null service connection");
            return;
        }
        try {
            this.zzoky = new zzr(iBinder);
            this.state = 2;
            zzawt();
        } catch (RemoteException e) {
            zzl(0, e.getMessage());
        }
    }

    public final synchronized void onServiceDisconnected(ComponentName componentName) {
        if (Log.isLoggable("MessengerIpcClient", 2)) {
            Log.v("MessengerIpcClient", "Service disconnected");
        }
        zzl(2, "Service disconnected");
    }

    /* access modifiers changed from: package-private */
    public final synchronized void zzawu() {
        if (this.state == 2 && this.zzini.isEmpty() && this.zzinj.size() == 0) {
            if (Log.isLoggable("MessengerIpcClient", 2)) {
                Log.v("MessengerIpcClient", "Finished handling requests, unbinding");
            }
            this.state = 3;
            zza.zzanm();
            this.zzokz.zzaiq.unbindService(this);
        }
    }

    /* access modifiers changed from: package-private */
    public final synchronized void zzawv() {
        if (this.state == 1) {
            zzl(1, "Timed out while binding");
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002f, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0096, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final synchronized boolean zzb(com.google.firebase.iid.zzt r7) {
        /*
            r6 = this;
            monitor-enter(r6)
            int r0 = r6.state     // Catch:{ all -> 0x0097 }
            r1 = 2
            r2 = 0
            r3 = 1
            if (r0 == 0) goto L_0x0041
            if (r0 == r3) goto L_0x003a
            if (r0 == r1) goto L_0x0030
            r7 = 3
            if (r0 == r7) goto L_0x002e
            r7 = 4
            if (r0 != r7) goto L_0x0013
            goto L_0x002e
        L_0x0013:
            java.lang.IllegalStateException r7 = new java.lang.IllegalStateException     // Catch:{ all -> 0x0097 }
            int r0 = r6.state     // Catch:{ all -> 0x0097 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0097 }
            r2 = 26
            r1.<init>(r2)     // Catch:{ all -> 0x0097 }
            java.lang.String r2 = "Unknown state: "
            r1.append(r2)     // Catch:{ all -> 0x0097 }
            r1.append(r0)     // Catch:{ all -> 0x0097 }
            java.lang.String r0 = r1.toString()     // Catch:{ all -> 0x0097 }
            r7.<init>(r0)     // Catch:{ all -> 0x0097 }
            throw r7     // Catch:{ all -> 0x0097 }
        L_0x002e:
            monitor-exit(r6)
            return r2
        L_0x0030:
            java.util.Queue<com.google.firebase.iid.zzt<?>> r0 = r6.zzini     // Catch:{ all -> 0x0097 }
            r0.add(r7)     // Catch:{ all -> 0x0097 }
            r6.zzawt()     // Catch:{ all -> 0x0097 }
            monitor-exit(r6)
            return r3
        L_0x003a:
            java.util.Queue<com.google.firebase.iid.zzt<?>> r0 = r6.zzini     // Catch:{ all -> 0x0097 }
            r0.add(r7)     // Catch:{ all -> 0x0097 }
            monitor-exit(r6)
            return r3
        L_0x0041:
            java.util.Queue<com.google.firebase.iid.zzt<?>> r0 = r6.zzini     // Catch:{ all -> 0x0097 }
            r0.add(r7)     // Catch:{ all -> 0x0097 }
            int r7 = r6.state     // Catch:{ all -> 0x0097 }
            if (r7 != 0) goto L_0x004c
            r7 = r3
            goto L_0x004d
        L_0x004c:
            r7 = r2
        L_0x004d:
            com.google.android.gms.common.internal.zzbq.checkState(r7)     // Catch:{ all -> 0x0097 }
            java.lang.String r7 = "MessengerIpcClient"
            boolean r7 = android.util.Log.isLoggable(r7, r1)     // Catch:{ all -> 0x0097 }
            if (r7 == 0) goto L_0x005f
            java.lang.String r7 = "MessengerIpcClient"
            java.lang.String r0 = "Starting bind to GmsCore"
            android.util.Log.v(r7, r0)     // Catch:{ all -> 0x0097 }
        L_0x005f:
            r6.state = r3     // Catch:{ all -> 0x0097 }
            android.content.Intent r7 = new android.content.Intent     // Catch:{ all -> 0x0097 }
            java.lang.String r0 = "com.google.android.c2dm.intent.REGISTER"
            r7.<init>(r0)     // Catch:{ all -> 0x0097 }
            java.lang.String r0 = "com.google.android.gms"
            r7.setPackage(r0)     // Catch:{ all -> 0x0097 }
            com.google.android.gms.common.stats.zza r0 = com.google.android.gms.common.stats.zza.zzanm()     // Catch:{ all -> 0x0097 }
            com.google.firebase.iid.zzk r1 = r6.zzokz     // Catch:{ all -> 0x0097 }
            android.content.Context r1 = r1.zzaiq     // Catch:{ all -> 0x0097 }
            boolean r7 = r0.zza(r1, r7, r6, r3)     // Catch:{ all -> 0x0097 }
            if (r7 != 0) goto L_0x0083
            java.lang.String r7 = "Unable to bind to service"
            r6.zzl(r2, r7)     // Catch:{ all -> 0x0097 }
            goto L_0x0095
        L_0x0083:
            com.google.firebase.iid.zzk r7 = r6.zzokz     // Catch:{ all -> 0x0097 }
            java.util.concurrent.ScheduledExecutorService r7 = r7.zzind     // Catch:{ all -> 0x0097 }
            com.google.firebase.iid.zzo r0 = new com.google.firebase.iid.zzo     // Catch:{ all -> 0x0097 }
            r0.<init>(r6)     // Catch:{ all -> 0x0097 }
            java.util.concurrent.TimeUnit r1 = java.util.concurrent.TimeUnit.SECONDS     // Catch:{ all -> 0x0097 }
            r4 = 30
            r7.schedule(r0, r4, r1)     // Catch:{ all -> 0x0097 }
        L_0x0095:
            monitor-exit(r6)
            return r3
        L_0x0097:
            r7 = move-exception
            monitor-exit(r6)
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.iid.zzm.zzb(com.google.firebase.iid.zzt):boolean");
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0052, code lost:
        r4 = r5.getData();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x005e, code lost:
        if (r4.getBoolean("unsupported", false) == false) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0060, code lost:
        r1.zzb(new com.google.firebase.iid.zzu(4, "Not supported by GmsCore"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x006c, code lost:
        r1.zzx(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x006f, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean zzc(android.os.Message r5) {
        /*
            r4 = this;
            int r0 = r5.arg1
            java.lang.String r1 = "MessengerIpcClient"
            r2 = 3
            boolean r1 = android.util.Log.isLoggable(r1, r2)
            if (r1 == 0) goto L_0x0023
            java.lang.String r1 = "MessengerIpcClient"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r3 = 41
            r2.<init>(r3)
            java.lang.String r3 = "Received response to request: "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.d(r1, r2)
        L_0x0023:
            monitor-enter(r4)
            android.util.SparseArray<com.google.firebase.iid.zzt<?>> r1 = r4.zzinj     // Catch:{ all -> 0x0070 }
            java.lang.Object r1 = r1.get(r0)     // Catch:{ all -> 0x0070 }
            com.google.firebase.iid.zzt r1 = (com.google.firebase.iid.zzt) r1     // Catch:{ all -> 0x0070 }
            r2 = 1
            if (r1 != 0) goto L_0x0049
            java.lang.String r5 = "MessengerIpcClient"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0070 }
            r3 = 50
            r1.<init>(r3)     // Catch:{ all -> 0x0070 }
            java.lang.String r3 = "Received response for unknown request: "
            r1.append(r3)     // Catch:{ all -> 0x0070 }
            r1.append(r0)     // Catch:{ all -> 0x0070 }
            java.lang.String r0 = r1.toString()     // Catch:{ all -> 0x0070 }
            android.util.Log.w(r5, r0)     // Catch:{ all -> 0x0070 }
            monitor-exit(r4)     // Catch:{ all -> 0x0070 }
            return r2
        L_0x0049:
            android.util.SparseArray<com.google.firebase.iid.zzt<?>> r3 = r4.zzinj     // Catch:{ all -> 0x0070 }
            r3.remove(r0)     // Catch:{ all -> 0x0070 }
            r4.zzawu()     // Catch:{ all -> 0x0070 }
            monitor-exit(r4)     // Catch:{ all -> 0x0070 }
            android.os.Bundle r4 = r5.getData()
            java.lang.String r5 = "unsupported"
            r0 = 0
            boolean r5 = r4.getBoolean(r5, r0)
            if (r5 == 0) goto L_0x006c
            com.google.firebase.iid.zzu r4 = new com.google.firebase.iid.zzu
            r5 = 4
            java.lang.String r0 = "Not supported by GmsCore"
            r4.<init>(r5, r0)
            r1.zzb(r4)
            goto L_0x006f
        L_0x006c:
            r1.zzx(r4)
        L_0x006f:
            return r2
        L_0x0070:
            r5 = move-exception
            monitor-exit(r4)     // Catch:{ all -> 0x0070 }
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.iid.zzm.zzc(android.os.Message):boolean");
    }

    /* access modifiers changed from: package-private */
    public final synchronized void zzec(int i) {
        zzt zzt = this.zzinj.get(i);
        if (zzt != null) {
            StringBuilder sb = new StringBuilder(31);
            sb.append("Timing out request: ");
            sb.append(i);
            Log.w("MessengerIpcClient", sb.toString());
            this.zzinj.remove(i);
            zzt.zzb(new zzu(3, "Timed out waiting for response"));
            zzawu();
        }
    }

    /* access modifiers changed from: package-private */
    public final synchronized void zzl(int i, String str) {
        if (Log.isLoggable("MessengerIpcClient", 3)) {
            String valueOf = String.valueOf(str);
            Log.d("MessengerIpcClient", valueOf.length() != 0 ? "Disconnected: ".concat(valueOf) : new String("Disconnected: "));
        }
        int i2 = this.state;
        if (i2 == 0) {
            throw new IllegalStateException();
        } else if (i2 == 1 || i2 == 2) {
            if (Log.isLoggable("MessengerIpcClient", 2)) {
                Log.v("MessengerIpcClient", "Unbinding service");
            }
            this.state = 4;
            zza.zzanm();
            this.zzokz.zzaiq.unbindService(this);
            zza(new zzu(i, str));
        } else if (i2 == 3) {
            this.state = 4;
        } else if (i2 != 4) {
            int i3 = this.state;
            StringBuilder sb = new StringBuilder(26);
            sb.append("Unknown state: ");
            sb.append(i3);
            throw new IllegalStateException(sb.toString());
        }
    }
}
