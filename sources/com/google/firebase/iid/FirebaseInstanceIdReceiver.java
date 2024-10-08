package com.google.firebase.iid;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.content.WakefulBroadcastReceiver;

public final class FirebaseInstanceIdReceiver extends WakefulBroadcastReceiver {
    private static zzh zzoku;
    private static zzh zzokv;

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0055  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:30:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void zza(android.content.Context r6, android.content.Intent r7, java.lang.String r8) {
        /*
            r5 = this;
            r0 = 0
            r7.setComponent(r0)
            java.lang.String r1 = r6.getPackageName()
            r7.setPackage(r1)
            java.lang.String r1 = "gcm.rawData64"
            java.lang.String r2 = r7.getStringExtra(r1)
            r3 = 0
            if (r2 == 0) goto L_0x0021
            java.lang.String r4 = "rawData"
            byte[] r2 = android.util.Base64.decode(r2, r3)
            r7.putExtra(r4, r2)
            r7.removeExtra(r1)
        L_0x0021:
            java.lang.String r1 = "from"
            java.lang.String r1 = r7.getStringExtra(r1)
            java.lang.String r2 = "google.com/iid"
            boolean r1 = r2.equals(r1)
            java.lang.String r2 = "com.google.firebase.INSTANCE_ID_EVENT"
            if (r1 != 0) goto L_0x0051
            boolean r1 = r2.equals(r8)
            if (r1 == 0) goto L_0x0038
            goto L_0x0051
        L_0x0038:
            java.lang.String r1 = "com.google.android.c2dm.intent.RECEIVE"
            boolean r1 = r1.equals(r8)
            java.lang.String r2 = "com.google.firebase.MESSAGING_EVENT"
            if (r1 != 0) goto L_0x0051
            boolean r8 = r2.equals(r8)
            if (r8 == 0) goto L_0x0049
            goto L_0x0051
        L_0x0049:
            java.lang.String r8 = "FirebaseInstanceId"
            java.lang.String r1 = "Unexpected intent"
            android.util.Log.d(r8, r1)
            goto L_0x0052
        L_0x0051:
            r0 = r2
        L_0x0052:
            r8 = -1
            if (r0 == 0) goto L_0x0086
            boolean r1 = com.google.android.gms.common.util.zzs.isAtLeastO()
            if (r1 == 0) goto L_0x0066
            android.content.pm.ApplicationInfo r1 = r6.getApplicationInfo()
            int r1 = r1.targetSdkVersion
            r2 = 26
            if (r1 < r2) goto L_0x0066
            r3 = 1
        L_0x0066:
            if (r3 == 0) goto L_0x007d
            boolean r1 = r5.isOrderedBroadcast()
            if (r1 == 0) goto L_0x0071
            r5.setResultCode(r8)
        L_0x0071:
            com.google.firebase.iid.zzh r6 = zzai(r6, r0)
            android.content.BroadcastReceiver$PendingResult r0 = r5.goAsync()
            r6.zza(r7, r0)
            goto L_0x0086
        L_0x007d:
            com.google.firebase.iid.zzz r8 = com.google.firebase.iid.zzz.zzclq()
            int r6 = r8.zzb(r6, r0, r7)
            r8 = r6
        L_0x0086:
            boolean r6 = r5.isOrderedBroadcast()
            if (r6 == 0) goto L_0x008f
            r5.setResultCode(r8)
        L_0x008f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.iid.FirebaseInstanceIdReceiver.zza(android.content.Context, android.content.Intent, java.lang.String):void");
    }

    private static synchronized zzh zzai(Context context, String str) {
        synchronized (FirebaseInstanceIdReceiver.class) {
            if ("com.google.firebase.MESSAGING_EVENT".equals(str)) {
                if (zzokv == null) {
                    zzokv = new zzh(context, str);
                }
                zzh zzh = zzokv;
                return zzh;
            }
            if (zzoku == null) {
                zzoku = new zzh(context, str);
            }
            zzh zzh2 = zzoku;
            return zzh2;
        }
    }

    public final void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Parcelable parcelableExtra = intent.getParcelableExtra("wrapped_intent");
            if (parcelableExtra instanceof Intent) {
                zza(context, (Intent) parcelableExtra, intent.getAction());
            } else {
                zza(context, intent, intent.getAction());
            }
        }
    }
}
