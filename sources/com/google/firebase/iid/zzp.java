package com.google.firebase.iid;

final /* synthetic */ class zzp implements Runnable {
    private final zzm zzola;

    zzp(zzm zzm) {
        this.zzola = zzm;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0040, code lost:
        if (android.util.Log.isLoggable("MessengerIpcClient", 3) == false) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0042, code lost:
        r3 = java.lang.String.valueOf(r0);
        r5 = new java.lang.StringBuilder(r3.length() + 8);
        r5.append("Sending ");
        r5.append(r3);
        android.util.Log.d("MessengerIpcClient", r5.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0062, code lost:
        r2 = r7.zzokz.zzaiq;
        r3 = r7.zzing;
        r4 = android.os.Message.obtain();
        r4.what = r0.what;
        r4.arg1 = r0.zzino;
        r4.replyTo = r3;
        r3 = new android.os.Bundle();
        r3.putBoolean("oneWay", r0.zzaww());
        r3.putString("pkg", r2.getPackageName());
        r3.putBundle("data", r0.zzinp);
        r4.setData(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r7.zzoky.send(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00a0, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00a1, code lost:
        r7.zzl(2, r0.getMessage());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void run() {
        /*
            r7 = this;
            com.google.firebase.iid.zzm r7 = r7.zzola
        L_0x0002:
            monitor-enter(r7)
            int r0 = r7.state     // Catch:{ all -> 0x00aa }
            r1 = 2
            if (r0 == r1) goto L_0x000a
            monitor-exit(r7)     // Catch:{ all -> 0x00aa }
            return
        L_0x000a:
            java.util.Queue<com.google.firebase.iid.zzt<?>> r0 = r7.zzini     // Catch:{ all -> 0x00aa }
            boolean r0 = r0.isEmpty()     // Catch:{ all -> 0x00aa }
            if (r0 == 0) goto L_0x0017
            r7.zzawu()     // Catch:{ all -> 0x00aa }
            monitor-exit(r7)     // Catch:{ all -> 0x00aa }
            return
        L_0x0017:
            java.util.Queue<com.google.firebase.iid.zzt<?>> r0 = r7.zzini     // Catch:{ all -> 0x00aa }
            java.lang.Object r0 = r0.poll()     // Catch:{ all -> 0x00aa }
            com.google.firebase.iid.zzt r0 = (com.google.firebase.iid.zzt) r0     // Catch:{ all -> 0x00aa }
            android.util.SparseArray<com.google.firebase.iid.zzt<?>> r2 = r7.zzinj     // Catch:{ all -> 0x00aa }
            int r3 = r0.zzino     // Catch:{ all -> 0x00aa }
            r2.put(r3, r0)     // Catch:{ all -> 0x00aa }
            com.google.firebase.iid.zzk r2 = r7.zzokz     // Catch:{ all -> 0x00aa }
            java.util.concurrent.ScheduledExecutorService r2 = r2.zzind     // Catch:{ all -> 0x00aa }
            com.google.firebase.iid.zzq r3 = new com.google.firebase.iid.zzq     // Catch:{ all -> 0x00aa }
            r3.<init>(r7, r0)     // Catch:{ all -> 0x00aa }
            java.util.concurrent.TimeUnit r4 = java.util.concurrent.TimeUnit.SECONDS     // Catch:{ all -> 0x00aa }
            r5 = 30
            r2.schedule(r3, r5, r4)     // Catch:{ all -> 0x00aa }
            monitor-exit(r7)     // Catch:{ all -> 0x00aa }
            java.lang.String r2 = "MessengerIpcClient"
            r3 = 3
            boolean r2 = android.util.Log.isLoggable(r2, r3)
            if (r2 == 0) goto L_0x0062
            java.lang.String r2 = "MessengerIpcClient"
            java.lang.String r3 = java.lang.String.valueOf(r0)
            int r4 = r3.length()
            int r4 = r4 + 8
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>(r4)
            java.lang.String r4 = "Sending "
            r5.append(r4)
            r5.append(r3)
            java.lang.String r3 = r5.toString()
            android.util.Log.d(r2, r3)
        L_0x0062:
            com.google.firebase.iid.zzk r2 = r7.zzokz
            android.content.Context r2 = r2.zzaiq
            android.os.Messenger r3 = r7.zzing
            android.os.Message r4 = android.os.Message.obtain()
            int r5 = r0.what
            r4.what = r5
            int r5 = r0.zzino
            r4.arg1 = r5
            r4.replyTo = r3
            android.os.Bundle r3 = new android.os.Bundle
            r3.<init>()
            java.lang.String r5 = "oneWay"
            boolean r6 = r0.zzaww()
            r3.putBoolean(r5, r6)
            java.lang.String r5 = "pkg"
            java.lang.String r2 = r2.getPackageName()
            r3.putString(r5, r2)
            java.lang.String r2 = "data"
            android.os.Bundle r0 = r0.zzinp
            r3.putBundle(r2, r0)
            r4.setData(r3)
            com.google.firebase.iid.zzr r0 = r7.zzoky     // Catch:{ RemoteException -> 0x00a0 }
            r0.send(r4)     // Catch:{ RemoteException -> 0x00a0 }
            goto L_0x0002
        L_0x00a0:
            r0 = move-exception
            java.lang.String r0 = r0.getMessage()
            r7.zzl(r1, r0)
            goto L_0x0002
        L_0x00aa:
            r0 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x00aa }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.iid.zzp.run():void");
    }
}
