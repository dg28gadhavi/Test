package com.google.firebase.iid;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.util.Log;
import java.io.IOException;

final class zzac implements Runnable {
    private final zzw zzokq;
    private final long zzolp;
    private final PowerManager.WakeLock zzolq;
    private final FirebaseInstanceId zzolr;

    zzac(FirebaseInstanceId firebaseInstanceId, zzw zzw, long j) {
        this.zzolr = firebaseInstanceId;
        this.zzokq = zzw;
        this.zzolp = j;
        PowerManager.WakeLock newWakeLock = ((PowerManager) getContext().getSystemService("power")).newWakeLock(1, "fiid-sync");
        this.zzolq = newWakeLock;
        newWakeLock.setReferenceCounted(false);
    }

    private final boolean zzclt() {
        zzab zzclc = this.zzolr.zzclc();
        if (zzclc != null && !zzclc.zzru(this.zzokq.zzclm())) {
            return true;
        }
        try {
            String zzcld = this.zzolr.zzcld();
            if (zzcld == null) {
                Log.e("FirebaseInstanceId", "Token retrieval failed: null");
                return false;
            }
            if (Log.isLoggable("FirebaseInstanceId", 3)) {
                Log.d("FirebaseInstanceId", "Token successfully retrieved");
            }
            if (zzclc == null || !zzcld.equals(zzclc.zzlnm)) {
                Context context = getContext();
                Intent intent = new Intent("com.google.firebase.iid.TOKEN_REFRESH");
                Intent intent2 = new Intent("com.google.firebase.INSTANCE_ID_EVENT");
                intent2.setClass(context, FirebaseInstanceIdReceiver.class);
                intent2.putExtra("wrapped_intent", intent);
                context.sendBroadcast(intent2);
            }
            return true;
        } catch (IOException | SecurityException e) {
            String valueOf = String.valueOf(e.getMessage());
            Log.e("FirebaseInstanceId", valueOf.length() != 0 ? "Token retrieval failed: ".concat(valueOf) : new String("Token retrieval failed: "));
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001d, code lost:
        if (zzrv(r1) != false) goto L_0x0021;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final boolean zzclu() {
        /*
            r2 = this;
        L_0x0000:
            com.google.firebase.iid.FirebaseInstanceId r0 = r2.zzolr
            monitor-enter(r0)
            com.google.firebase.iid.zzaa r1 = com.google.firebase.iid.FirebaseInstanceId.zzcle()     // Catch:{ all -> 0x0029 }
            java.lang.String r1 = r1.zzcls()     // Catch:{ all -> 0x0029 }
            if (r1 != 0) goto L_0x0018
            java.lang.String r2 = "FirebaseInstanceId"
            java.lang.String r1 = "topic sync succeeded"
            android.util.Log.d(r2, r1)     // Catch:{ all -> 0x0029 }
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            r2 = 1
            return r2
        L_0x0018:
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            boolean r0 = r2.zzrv(r1)
            if (r0 != 0) goto L_0x0021
            r2 = 0
            return r2
        L_0x0021:
            com.google.firebase.iid.zzaa r0 = com.google.firebase.iid.FirebaseInstanceId.zzcle()
            r0.zzro(r1)
            goto L_0x0000
        L_0x0029:
            r2 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.iid.zzac.zzclu():boolean");
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0036 A[Catch:{ IOException -> 0x005a }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004b A[Catch:{ IOException -> 0x005a }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final boolean zzrv(java.lang.String r7) {
        /*
            r6 = this;
            java.lang.String r0 = "FirebaseInstanceId"
            java.lang.String r1 = "!"
            java.lang.String[] r7 = r7.split(r1)
            int r1 = r7.length
            r2 = 2
            r3 = 1
            if (r1 != r2) goto L_0x0079
            r1 = 0
            r2 = r7[r1]
            r7 = r7[r3]
            int r4 = r2.hashCode()     // Catch:{ IOException -> 0x005a }
            r5 = 83
            if (r4 == r5) goto L_0x0029
            r5 = 85
            if (r4 == r5) goto L_0x001f
            goto L_0x0033
        L_0x001f:
            java.lang.String r4 = "U"
            boolean r2 = r2.equals(r4)     // Catch:{ IOException -> 0x005a }
            if (r2 == 0) goto L_0x0033
            r2 = r3
            goto L_0x0034
        L_0x0029:
            java.lang.String r4 = "S"
            boolean r2 = r2.equals(r4)     // Catch:{ IOException -> 0x005a }
            if (r2 == 0) goto L_0x0033
            r2 = r1
            goto L_0x0034
        L_0x0033:
            r2 = -1
        L_0x0034:
            if (r2 == 0) goto L_0x004b
            if (r2 == r3) goto L_0x0039
            goto L_0x0079
        L_0x0039:
            com.google.firebase.iid.FirebaseInstanceId r6 = r6.zzolr     // Catch:{ IOException -> 0x005a }
            r6.zzrn(r7)     // Catch:{ IOException -> 0x005a }
            boolean r6 = com.google.firebase.iid.FirebaseInstanceId.zzclf()     // Catch:{ IOException -> 0x005a }
            if (r6 == 0) goto L_0x0079
            java.lang.String r6 = "unsubscribe operation succeeded"
        L_0x0047:
            android.util.Log.d(r0, r6)     // Catch:{ IOException -> 0x005a }
            goto L_0x0079
        L_0x004b:
            com.google.firebase.iid.FirebaseInstanceId r6 = r6.zzolr     // Catch:{ IOException -> 0x005a }
            r6.zzrm(r7)     // Catch:{ IOException -> 0x005a }
            boolean r6 = com.google.firebase.iid.FirebaseInstanceId.zzclf()     // Catch:{ IOException -> 0x005a }
            if (r6 == 0) goto L_0x0079
            java.lang.String r6 = "subscribe operation succeeded"
            goto L_0x0047
        L_0x005a:
            r6 = move-exception
            java.lang.String r6 = r6.getMessage()
            java.lang.String r6 = java.lang.String.valueOf(r6)
            int r7 = r6.length()
            java.lang.String r2 = "Topic sync failed: "
            if (r7 == 0) goto L_0x0070
            java.lang.String r6 = r2.concat(r6)
            goto L_0x0075
        L_0x0070:
            java.lang.String r6 = new java.lang.String
            r6.<init>(r2)
        L_0x0075:
            android.util.Log.e(r0, r6)
            return r1
        L_0x0079:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.iid.zzac.zzrv(java.lang.String):boolean");
    }

    /* access modifiers changed from: package-private */
    public final Context getContext() {
        return this.zzolr.getApp().getApplicationContext();
    }

    public final void run() {
        FirebaseInstanceId firebaseInstanceId;
        this.zzolq.acquire();
        try {
            boolean z = true;
            this.zzolr.zzcy(true);
            if (this.zzokq.zzcll() == 0) {
                z = false;
            }
            if (!z) {
                firebaseInstanceId = this.zzolr;
            } else {
                if (!zzclv()) {
                    new zzad(this).zzclw();
                } else if (!zzclt() || !zzclu()) {
                    this.zzolr.zzcd(this.zzolp);
                } else {
                    firebaseInstanceId = this.zzolr;
                }
            }
            firebaseInstanceId.zzcy(false);
        } finally {
            this.zzolq.release();
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean zzclv() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService("connectivity");
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
