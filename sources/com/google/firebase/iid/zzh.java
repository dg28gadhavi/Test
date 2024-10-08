package com.google.firebase.iid;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public final class zzh implements ServiceConnection {
    private final Context zzaiq;
    private final Intent zzimp;
    private final ScheduledExecutorService zzimq;
    private final Queue<zzd> zzimr;
    private boolean zzimt;
    private zzf zzokm;

    public zzh(Context context, String str) {
        this(context, str, new ScheduledThreadPoolExecutor(0));
    }

    private zzh(Context context, String str, ScheduledExecutorService scheduledExecutorService) {
        this.zzimr = new ArrayDeque();
        this.zzimt = false;
        Context applicationContext = context.getApplicationContext();
        this.zzaiq = applicationContext;
        this.zzimp = new Intent(str).setPackage(applicationContext.getPackageName());
        this.zzimq = scheduledExecutorService;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00b4, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final synchronized void zzawo() {
        /*
            r5 = this;
            monitor-enter(r5)
            java.lang.String r0 = "EnhancedIntentService"
            r1 = 3
            boolean r0 = android.util.Log.isLoggable(r0, r1)     // Catch:{ all -> 0x00b7 }
            if (r0 == 0) goto L_0x0011
            java.lang.String r0 = "EnhancedIntentService"
            java.lang.String r2 = "flush queue called"
            android.util.Log.d(r0, r2)     // Catch:{ all -> 0x00b7 }
        L_0x0011:
            java.util.Queue<com.google.firebase.iid.zzd> r0 = r5.zzimr     // Catch:{ all -> 0x00b7 }
            boolean r0 = r0.isEmpty()     // Catch:{ all -> 0x00b7 }
            if (r0 != 0) goto L_0x00b5
            java.lang.String r0 = "EnhancedIntentService"
            boolean r0 = android.util.Log.isLoggable(r0, r1)     // Catch:{ all -> 0x00b7 }
            if (r0 == 0) goto L_0x0028
            java.lang.String r0 = "EnhancedIntentService"
            java.lang.String r2 = "found intent to be delivered"
            android.util.Log.d(r0, r2)     // Catch:{ all -> 0x00b7 }
        L_0x0028:
            com.google.firebase.iid.zzf r0 = r5.zzokm     // Catch:{ all -> 0x00b7 }
            if (r0 == 0) goto L_0x004f
            boolean r0 = r0.isBinderAlive()     // Catch:{ all -> 0x00b7 }
            if (r0 == 0) goto L_0x004f
            java.lang.String r0 = "EnhancedIntentService"
            boolean r0 = android.util.Log.isLoggable(r0, r1)     // Catch:{ all -> 0x00b7 }
            if (r0 == 0) goto L_0x0041
            java.lang.String r0 = "EnhancedIntentService"
            java.lang.String r2 = "binder is alive, sending the intent."
            android.util.Log.d(r0, r2)     // Catch:{ all -> 0x00b7 }
        L_0x0041:
            java.util.Queue<com.google.firebase.iid.zzd> r0 = r5.zzimr     // Catch:{ all -> 0x00b7 }
            java.lang.Object r0 = r0.poll()     // Catch:{ all -> 0x00b7 }
            com.google.firebase.iid.zzd r0 = (com.google.firebase.iid.zzd) r0     // Catch:{ all -> 0x00b7 }
            com.google.firebase.iid.zzf r2 = r5.zzokm     // Catch:{ all -> 0x00b7 }
            r2.zza((com.google.firebase.iid.zzd) r0)     // Catch:{ all -> 0x00b7 }
            goto L_0x0011
        L_0x004f:
            java.lang.String r0 = "EnhancedIntentService"
            boolean r0 = android.util.Log.isLoggable(r0, r1)     // Catch:{ all -> 0x00b7 }
            r1 = 1
            if (r0 == 0) goto L_0x0077
            java.lang.String r0 = "EnhancedIntentService"
            boolean r2 = r5.zzimt     // Catch:{ all -> 0x00b7 }
            if (r2 != 0) goto L_0x0060
            r2 = r1
            goto L_0x0061
        L_0x0060:
            r2 = 0
        L_0x0061:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b7 }
            r4 = 39
            r3.<init>(r4)     // Catch:{ all -> 0x00b7 }
            java.lang.String r4 = "binder is dead. start connection? "
            r3.append(r4)     // Catch:{ all -> 0x00b7 }
            r3.append(r2)     // Catch:{ all -> 0x00b7 }
            java.lang.String r2 = r3.toString()     // Catch:{ all -> 0x00b7 }
            android.util.Log.d(r0, r2)     // Catch:{ all -> 0x00b7 }
        L_0x0077:
            boolean r0 = r5.zzimt     // Catch:{ all -> 0x00b7 }
            if (r0 != 0) goto L_0x00b3
            r5.zzimt = r1     // Catch:{ all -> 0x00b7 }
            com.google.android.gms.common.stats.zza r0 = com.google.android.gms.common.stats.zza.zzanm()     // Catch:{ SecurityException -> 0x0097 }
            android.content.Context r1 = r5.zzaiq     // Catch:{ SecurityException -> 0x0097 }
            android.content.Intent r2 = r5.zzimp     // Catch:{ SecurityException -> 0x0097 }
            r3 = 65
            boolean r0 = r0.zza(r1, r2, r5, r3)     // Catch:{ SecurityException -> 0x0097 }
            if (r0 == 0) goto L_0x008f
            monitor-exit(r5)
            return
        L_0x008f:
            java.lang.String r0 = "EnhancedIntentService"
            java.lang.String r1 = "binding to the service failed"
            android.util.Log.e(r0, r1)     // Catch:{ SecurityException -> 0x0097 }
            goto L_0x009f
        L_0x0097:
            r0 = move-exception
            java.lang.String r1 = "EnhancedIntentService"
            java.lang.String r2 = "Exception while binding the service"
            android.util.Log.e(r1, r2, r0)     // Catch:{ all -> 0x00b7 }
        L_0x009f:
            java.util.Queue<com.google.firebase.iid.zzd> r0 = r5.zzimr     // Catch:{ all -> 0x00b7 }
            boolean r0 = r0.isEmpty()     // Catch:{ all -> 0x00b7 }
            if (r0 != 0) goto L_0x00b3
            java.util.Queue<com.google.firebase.iid.zzd> r0 = r5.zzimr     // Catch:{ all -> 0x00b7 }
            java.lang.Object r0 = r0.poll()     // Catch:{ all -> 0x00b7 }
            com.google.firebase.iid.zzd r0 = (com.google.firebase.iid.zzd) r0     // Catch:{ all -> 0x00b7 }
            r0.finish()     // Catch:{ all -> 0x00b7 }
            goto L_0x009f
        L_0x00b3:
            monitor-exit(r5)
            return
        L_0x00b5:
            monitor-exit(r5)
            return
        L_0x00b7:
            r0 = move-exception
            monitor-exit(r5)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.iid.zzh.zzawo():void");
    }

    public final void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        synchronized (this) {
            this.zzimt = false;
            this.zzokm = (zzf) iBinder;
            if (Log.isLoggable("EnhancedIntentService", 3)) {
                String valueOf = String.valueOf(componentName);
                StringBuilder sb = new StringBuilder(valueOf.length() + 20);
                sb.append("onServiceConnected: ");
                sb.append(valueOf);
                Log.d("EnhancedIntentService", sb.toString());
            }
            zzawo();
        }
    }

    public final void onServiceDisconnected(ComponentName componentName) {
        if (Log.isLoggable("EnhancedIntentService", 3)) {
            String valueOf = String.valueOf(componentName);
            StringBuilder sb = new StringBuilder(valueOf.length() + 23);
            sb.append("onServiceDisconnected: ");
            sb.append(valueOf);
            Log.d("EnhancedIntentService", sb.toString());
        }
        zzawo();
    }

    public final synchronized void zza(Intent intent, BroadcastReceiver.PendingResult pendingResult) {
        if (Log.isLoggable("EnhancedIntentService", 3)) {
            Log.d("EnhancedIntentService", "new intent queued in the bind-strategy delivery");
        }
        this.zzimr.add(new zzd(intent, pendingResult, this.zzimq));
        zzawo();
    }
}
