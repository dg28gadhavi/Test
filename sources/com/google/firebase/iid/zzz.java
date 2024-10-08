package com.google.firebase.iid;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import com.sec.internal.imscr.LogClass;
import java.util.ArrayDeque;
import java.util.Queue;

public final class zzz {
    private static zzz zzolj;
    private final SimpleArrayMap<String, String> zzolk = new SimpleArrayMap<>();
    private Boolean zzoll = null;
    final Queue<Intent> zzolm = new ArrayDeque();
    private Queue<Intent> zzoln = new ArrayDeque();

    private zzz() {
    }

    public static PendingIntent zza(Context context, int i, Intent intent, int i2) {
        Intent intent2 = new Intent(context, FirebaseInstanceIdReceiver.class);
        intent2.setAction("com.google.firebase.MESSAGING_EVENT");
        intent2.putExtra("wrapped_intent", intent);
        return PendingIntent.getBroadcast(context, i, intent2, LogClass.IM_SWITCH_OFF);
    }

    public static synchronized zzz zzclq() {
        zzz zzz;
        synchronized (zzz.class) {
            if (zzolj == null) {
                zzolj = new zzz();
            }
            zzz = zzolj;
        }
        return zzz;
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
        	at java.util.ArrayList.rangeCheck(ArrayList.java:659)
        	at java.util.ArrayList.get(ArrayList.java:435)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:698)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:698)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00d4 A[Catch:{ SecurityException -> 0x012d, IllegalStateException -> 0x0109 }] */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00eb A[Catch:{ SecurityException -> 0x012d, IllegalStateException -> 0x0109 }] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00f0 A[Catch:{ SecurityException -> 0x012d, IllegalStateException -> 0x0109 }] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00fd A[Catch:{ SecurityException -> 0x012d, IllegalStateException -> 0x0109 }] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0107 A[RETURN] */
    private final int zze(android.content.Context r7, android.content.Intent r8) {
        /*
            r6 = this;
            android.support.v4.util.SimpleArrayMap<java.lang.String, java.lang.String> r0 = r6.zzolk
            monitor-enter(r0)
            android.support.v4.util.SimpleArrayMap<java.lang.String, java.lang.String> r1 = r6.zzolk     // Catch:{ all -> 0x0138 }
            java.lang.String r2 = r8.getAction()     // Catch:{ all -> 0x0138 }
            java.lang.Object r1 = r1.get(r2)     // Catch:{ all -> 0x0138 }
            java.lang.String r1 = (java.lang.String) r1     // Catch:{ all -> 0x0138 }
            monitor-exit(r0)     // Catch:{ all -> 0x0138 }
            r0 = 0
            if (r1 != 0) goto L_0x00a4
            android.content.pm.PackageManager r1 = r7.getPackageManager()
            android.content.pm.ResolveInfo r1 = r1.resolveService(r8, r0)
            if (r1 == 0) goto L_0x009c
            android.content.pm.ServiceInfo r1 = r1.serviceInfo
            if (r1 != 0) goto L_0x0023
            goto L_0x009c
        L_0x0023:
            java.lang.String r2 = r7.getPackageName()
            java.lang.String r3 = r1.packageName
            boolean r2 = r2.equals(r3)
            if (r2 == 0) goto L_0x0066
            java.lang.String r2 = r1.name
            if (r2 != 0) goto L_0x0034
            goto L_0x0066
        L_0x0034:
            java.lang.String r1 = "."
            boolean r1 = r2.startsWith(r1)
            if (r1 == 0) goto L_0x0054
            java.lang.String r1 = r7.getPackageName()
            java.lang.String r1 = java.lang.String.valueOf(r1)
            int r3 = r2.length()
            if (r3 == 0) goto L_0x004f
            java.lang.String r1 = r1.concat(r2)
            goto L_0x0055
        L_0x004f:
            java.lang.String r2 = new java.lang.String
            r2.<init>(r1)
        L_0x0054:
            r1 = r2
        L_0x0055:
            android.support.v4.util.SimpleArrayMap<java.lang.String, java.lang.String> r2 = r6.zzolk
            monitor-enter(r2)
            android.support.v4.util.SimpleArrayMap<java.lang.String, java.lang.String> r3 = r6.zzolk     // Catch:{ all -> 0x0063 }
            java.lang.String r4 = r8.getAction()     // Catch:{ all -> 0x0063 }
            r3.put(r4, r1)     // Catch:{ all -> 0x0063 }
            monitor-exit(r2)     // Catch:{ all -> 0x0063 }
            goto L_0x00a4
        L_0x0063:
            r6 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0063 }
            throw r6
        L_0x0066:
            java.lang.String r2 = "FirebaseInstanceId"
            java.lang.String r3 = r1.packageName
            java.lang.String r1 = r1.name
            java.lang.String r4 = java.lang.String.valueOf(r3)
            int r4 = r4.length()
            int r4 = r4 + 94
            java.lang.String r5 = java.lang.String.valueOf(r1)
            int r5 = r5.length()
            int r4 = r4 + r5
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>(r4)
            java.lang.String r4 = "Error resolving target intent service, skipping classname enforcement. Resolved service was: "
            r5.append(r4)
            r5.append(r3)
            java.lang.String r3 = "/"
            r5.append(r3)
            r5.append(r1)
            java.lang.String r1 = r5.toString()
            android.util.Log.e(r2, r1)
            goto L_0x00d0
        L_0x009c:
            java.lang.String r1 = "FirebaseInstanceId"
            java.lang.String r2 = "Failed to resolve target intent service, skipping classname enforcement"
            android.util.Log.e(r1, r2)
            goto L_0x00d0
        L_0x00a4:
            java.lang.String r2 = "FirebaseInstanceId"
            r3 = 3
            boolean r2 = android.util.Log.isLoggable(r2, r3)
            if (r2 == 0) goto L_0x00c9
            java.lang.String r2 = "FirebaseInstanceId"
            java.lang.String r3 = "Restricting intent to a specific service: "
            java.lang.String r4 = java.lang.String.valueOf(r1)
            int r5 = r4.length()
            if (r5 == 0) goto L_0x00c0
            java.lang.String r3 = r3.concat(r4)
            goto L_0x00c6
        L_0x00c0:
            java.lang.String r4 = new java.lang.String
            r4.<init>(r3)
            r3 = r4
        L_0x00c6:
            android.util.Log.d(r2, r3)
        L_0x00c9:
            java.lang.String r2 = r7.getPackageName()
            r8.setClassName(r2, r1)
        L_0x00d0:
            java.lang.Boolean r1 = r6.zzoll     // Catch:{ SecurityException -> 0x012d, IllegalStateException -> 0x0109 }
            if (r1 != 0) goto L_0x00e3
            java.lang.String r1 = "android.permission.WAKE_LOCK"
            int r1 = r7.checkCallingOrSelfPermission(r1)     // Catch:{ SecurityException -> 0x012d, IllegalStateException -> 0x0109 }
            if (r1 != 0) goto L_0x00dd
            r0 = 1
        L_0x00dd:
            java.lang.Boolean r0 = java.lang.Boolean.valueOf(r0)     // Catch:{ SecurityException -> 0x012d, IllegalStateException -> 0x0109 }
            r6.zzoll = r0     // Catch:{ SecurityException -> 0x012d, IllegalStateException -> 0x0109 }
        L_0x00e3:
            java.lang.Boolean r6 = r6.zzoll     // Catch:{ SecurityException -> 0x012d, IllegalStateException -> 0x0109 }
            boolean r6 = r6.booleanValue()     // Catch:{ SecurityException -> 0x012d, IllegalStateException -> 0x0109 }
            if (r6 == 0) goto L_0x00f0
            android.content.ComponentName r6 = android.support.v4.content.WakefulBroadcastReceiver.startWakefulService(r7, r8)     // Catch:{ SecurityException -> 0x012d, IllegalStateException -> 0x0109 }
            goto L_0x00fb
        L_0x00f0:
            android.content.ComponentName r6 = r7.startService(r8)     // Catch:{ SecurityException -> 0x012d, IllegalStateException -> 0x0109 }
            java.lang.String r7 = "FirebaseInstanceId"
            java.lang.String r8 = "Missing wake lock permission, service start may be delayed"
            android.util.Log.d(r7, r8)     // Catch:{ SecurityException -> 0x012d, IllegalStateException -> 0x0109 }
        L_0x00fb:
            if (r6 != 0) goto L_0x0107
            java.lang.String r6 = "FirebaseInstanceId"
            java.lang.String r7 = "Error while delivering the message: ServiceIntent not found."
            android.util.Log.e(r6, r7)     // Catch:{ SecurityException -> 0x012d, IllegalStateException -> 0x0109 }
            r6 = 404(0x194, float:5.66E-43)
            return r6
        L_0x0107:
            r6 = -1
            return r6
        L_0x0109:
            r6 = move-exception
            java.lang.String r7 = "FirebaseInstanceId"
            java.lang.String r6 = java.lang.String.valueOf(r6)
            int r8 = r6.length()
            int r8 = r8 + 45
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>(r8)
            java.lang.String r8 = "Failed to start service while in background: "
            r0.append(r8)
            r0.append(r6)
            java.lang.String r6 = r0.toString()
            android.util.Log.e(r7, r6)
            r6 = 402(0x192, float:5.63E-43)
            return r6
        L_0x012d:
            r6 = move-exception
            java.lang.String r7 = "FirebaseInstanceId"
            java.lang.String r8 = "Error while delivering the message to the serviceIntent"
            android.util.Log.e(r7, r8, r6)
            r6 = 401(0x191, float:5.62E-43)
            return r6
        L_0x0138:
            r6 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0138 }
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.iid.zzz.zze(android.content.Context, android.content.Intent):int");
    }

    public final int zzb(Context context, String str, Intent intent) {
        Queue<Intent> queue;
        str.hashCode();
        if (str.equals("com.google.firebase.INSTANCE_ID_EVENT")) {
            queue = this.zzolm;
        } else if (!str.equals("com.google.firebase.MESSAGING_EVENT")) {
            Log.w("FirebaseInstanceId", str.length() != 0 ? "Unknown service action: ".concat(str) : new String("Unknown service action: "));
            return 500;
        } else {
            queue = this.zzoln;
        }
        queue.offer(intent);
        Intent intent2 = new Intent(str);
        intent2.setPackage(context.getPackageName());
        return zze(context, intent2);
    }

    public final Intent zzclr() {
        return this.zzoln.poll();
    }
}
