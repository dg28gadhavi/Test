package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.PowerManager;
import android.os.SemSystemProperties;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.Mno;

class KeepAliveSender {
    private static final int KEEPALIVE_INTERVAL = 2000;
    private static final int KEEPALIVE_INTERVAL_CMCC = 8000;
    private static final String PERSIST_VZW_KEEPALIVE = "persist.sys.ims.vzw.keepalive";
    private String LOG_TAG = KeepAliveSender.class.getSimpleName();
    private Context mContext = null;
    String mIpAddr;
    private volatile boolean mIsRunning = false;
    private final Object mLock = new Object();
    private Mno mMno = Mno.DEFAULT;
    int mPort;
    private ImsRegistration mRegistration = null;
    private Thread mTask = null;
    private PowerManager.WakeLock mWakeLock = null;

    public KeepAliveSender(Context context, ImsRegistration imsRegistration, String str, int i, Mno mno) {
        this.mContext = context;
        this.mRegistration = imsRegistration;
        this.mMno = mno;
        this.mIpAddr = str;
        this.mPort = i;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, this.LOG_TAG + "KeepAlive");
    }

    public void start() {
        Log.i(this.LOG_TAG, "KeepAliveSender: start: ");
        if (this.mTask != null) {
            Log.i(this.LOG_TAG, "KeepAliveSender: start() - already running.");
        } else if (SemSystemProperties.getBoolean("persist.sys.ims.blockvzwka", false)) {
            Log.i(this.LOG_TAG, "KeepAliveSender: blocked by system properties!");
        } else {
            PowerManager.WakeLock wakeLock = this.mWakeLock;
            if (wakeLock != null) {
                wakeLock.acquire();
                Log.i(this.LOG_TAG, "KeepAliveSender: acquire WakeLock");
            }
            this.mIsRunning = true;
            if (this.mMno == Mno.VZW) {
                SemSystemProperties.set(PERSIST_VZW_KEEPALIVE, "1");
            }
            Thread thread = new Thread(new KeepAliveSender$$ExternalSyntheticLambda0(this));
            this.mTask = thread;
            thread.start();
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x003c, code lost:
        if (r12.mMno.isOneOf(com.sec.internal.constants.Mno.VIVA_BAHRAIN, com.sec.internal.constants.Mno.ETISALAT_UAE) != false) goto L_0x003e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003e, code lost:
        if (r5 != false) goto L_0x0040;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0044, code lost:
        if (r12.mMno != com.sec.internal.constants.Mno.VZW) goto L_0x009e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0046, code lost:
        r5 = android.os.SemSystemProperties.get(PERSIST_VZW_KEEPALIVE, "").equals("1");
        android.util.Log.d(r12.LOG_TAG, "KeepAliveSender: isAllowedByProperty=" + r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x006a, code lost:
        if (r5 == false) goto L_0x00cf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x006c, code lost:
        android.util.Log.i(r12.LOG_TAG, "KeepAliveSender: send dummy.txt UDP to [" + r12.mIpAddr + "]:" + r12.mPort + " ...");
        r2.send(new java.net.DatagramPacket(r4, 4, r1, r12.mPort));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x009e, code lost:
        android.util.Log.i(r12.LOG_TAG, "KeepAliveSender: send dummy.txt UDP to [" + r12.mIpAddr + "]:" + r12.mPort + " ...");
        r2.send(new java.net.DatagramPacket(r4, 4, r1, r12.mPort));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00e4, code lost:
        if (r12.mMno.isOneOf(com.sec.internal.constants.Mno.CMCC, com.sec.internal.constants.Mno.VIVA_BAHRAIN, com.sec.internal.constants.Mno.ETISALAT_UAE) == false) goto L_0x00ec;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00e6, code lost:
        java.lang.Thread.sleep(8000);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00ec, code lost:
        java.lang.Thread.sleep(com.sec.internal.ims.servicemodules.ss.UtStateMachine.HTTP_READ_TIMEOUT_GCF);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00f1, code lost:
        r5 = r12.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00f3, code lost:
        monitor-enter(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00f6, code lost:
        if (r12.mIsRunning != false) goto L_0x00fa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00f8, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00fa, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x010c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$start$0() {
        /*
            r12 = this;
            r0 = 0
            r1 = 0
            java.net.DatagramSocket r2 = new java.net.DatagramSocket     // Catch:{ IOException | InterruptedException -> 0x0103 }
            r3 = 45016(0xafd8, float:6.3081E-41)
            r2.<init>(r3)     // Catch:{ IOException | InterruptedException -> 0x0103 }
            java.lang.String r1 = r12.mIpAddr     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.net.InetAddress r1 = java.net.InetAddress.getByName(r1)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r3 = 4
            byte[] r4 = new byte[r3]     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r4 = {13, 10, 13, 10} // fill-array     // Catch:{ IOException | InterruptedException -> 0x0101 }
            com.sec.ims.ImsRegistration r5 = r12.mRegistration     // Catch:{ IOException | InterruptedException -> 0x0101 }
            if (r5 == 0) goto L_0x0021
            android.net.Network r5 = r5.getNetwork()     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r5.bindSocket(r2)     // Catch:{ IOException | InterruptedException -> 0x0101 }
        L_0x0021:
            r5 = r0
        L_0x0022:
            com.sec.internal.constants.Mno r6 = r12.mMno     // Catch:{ IOException | InterruptedException -> 0x0101 }
            boolean r6 = r6.isChn()     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r7 = 2
            r8 = 1
            if (r6 != 0) goto L_0x003e
            com.sec.internal.constants.Mno r6 = r12.mMno     // Catch:{ IOException | InterruptedException -> 0x0101 }
            com.sec.internal.constants.Mno[] r9 = new com.sec.internal.constants.Mno[r7]     // Catch:{ IOException | InterruptedException -> 0x0101 }
            com.sec.internal.constants.Mno r10 = com.sec.internal.constants.Mno.VIVA_BAHRAIN     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r9[r0] = r10     // Catch:{ IOException | InterruptedException -> 0x0101 }
            com.sec.internal.constants.Mno r10 = com.sec.internal.constants.Mno.ETISALAT_UAE     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r9[r8] = r10     // Catch:{ IOException | InterruptedException -> 0x0101 }
            boolean r6 = r6.isOneOf(r9)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            if (r6 == 0) goto L_0x0040
        L_0x003e:
            if (r5 == 0) goto L_0x00cf
        L_0x0040:
            com.sec.internal.constants.Mno r5 = r12.mMno     // Catch:{ IOException | InterruptedException -> 0x0101 }
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.VZW     // Catch:{ IOException | InterruptedException -> 0x0101 }
            if (r5 != r6) goto L_0x009e
            java.lang.String r5 = "persist.sys.ims.vzw.keepalive"
            java.lang.String r6 = ""
            java.lang.String r5 = android.os.SemSystemProperties.get(r5, r6)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.String r6 = "1"
            boolean r5 = r5.equals(r6)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.String r6 = r12.LOG_TAG     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r9.<init>()     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.String r10 = "KeepAliveSender: isAllowedByProperty="
            r9.append(r10)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r9.append(r5)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.String r9 = r9.toString()     // Catch:{ IOException | InterruptedException -> 0x0101 }
            android.util.Log.d(r6, r9)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            if (r5 == 0) goto L_0x00cf
            java.lang.String r5 = r12.LOG_TAG     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r6.<init>()     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.String r9 = "KeepAliveSender: send dummy.txt UDP to ["
            r6.append(r9)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.String r9 = r12.mIpAddr     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r6.append(r9)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.String r9 = "]:"
            r6.append(r9)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            int r9 = r12.mPort     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r6.append(r9)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.String r9 = " ..."
            r6.append(r9)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.String r6 = r6.toString()     // Catch:{ IOException | InterruptedException -> 0x0101 }
            android.util.Log.i(r5, r6)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.net.DatagramPacket r5 = new java.net.DatagramPacket     // Catch:{ IOException | InterruptedException -> 0x0101 }
            int r6 = r12.mPort     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r5.<init>(r4, r3, r1, r6)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r2.send(r5)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            goto L_0x00cf
        L_0x009e:
            java.lang.String r5 = r12.LOG_TAG     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r6.<init>()     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.String r9 = "KeepAliveSender: send dummy.txt UDP to ["
            r6.append(r9)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.String r9 = r12.mIpAddr     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r6.append(r9)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.String r9 = "]:"
            r6.append(r9)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            int r9 = r12.mPort     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r6.append(r9)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.String r9 = " ..."
            r6.append(r9)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.lang.String r6 = r6.toString()     // Catch:{ IOException | InterruptedException -> 0x0101 }
            android.util.Log.i(r5, r6)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            java.net.DatagramPacket r5 = new java.net.DatagramPacket     // Catch:{ IOException | InterruptedException -> 0x0101 }
            int r6 = r12.mPort     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r5.<init>(r4, r3, r1, r6)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r2.send(r5)     // Catch:{ IOException | InterruptedException -> 0x0101 }
        L_0x00cf:
            com.sec.internal.constants.Mno r5 = r12.mMno     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r6 = 3
            com.sec.internal.constants.Mno[] r6 = new com.sec.internal.constants.Mno[r6]     // Catch:{ IOException | InterruptedException -> 0x0101 }
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.CMCC     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r6[r0] = r9     // Catch:{ IOException | InterruptedException -> 0x0101 }
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.VIVA_BAHRAIN     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r6[r8] = r9     // Catch:{ IOException | InterruptedException -> 0x0101 }
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.ETISALAT_UAE     // Catch:{ IOException | InterruptedException -> 0x0101 }
            r6[r7] = r9     // Catch:{ IOException | InterruptedException -> 0x0101 }
            boolean r5 = r5.isOneOf(r6)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            if (r5 == 0) goto L_0x00ec
            r5 = 8000(0x1f40, double:3.9525E-320)
            java.lang.Thread.sleep(r5)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            goto L_0x00f1
        L_0x00ec:
            r5 = 2000(0x7d0, double:9.88E-321)
            java.lang.Thread.sleep(r5)     // Catch:{ IOException | InterruptedException -> 0x0101 }
        L_0x00f1:
            java.lang.Object r5 = r12.mLock     // Catch:{ IOException | InterruptedException -> 0x0101 }
            monitor-enter(r5)     // Catch:{ IOException | InterruptedException -> 0x0101 }
            boolean r6 = r12.mIsRunning     // Catch:{ all -> 0x00fe }
            if (r6 != 0) goto L_0x00fa
            monitor-exit(r5)     // Catch:{ all -> 0x00fe }
            goto L_0x010a
        L_0x00fa:
            monitor-exit(r5)     // Catch:{ all -> 0x00fe }
            r5 = r8
            goto L_0x0022
        L_0x00fe:
            r1 = move-exception
            monitor-exit(r5)     // Catch:{ all -> 0x00fe }
            throw r1     // Catch:{ IOException | InterruptedException -> 0x0101 }
        L_0x0101:
            r1 = move-exception
            goto L_0x0107
        L_0x0103:
            r2 = move-exception
            r11 = r2
            r2 = r1
            r1 = r11
        L_0x0107:
            r1.printStackTrace()
        L_0x010a:
            if (r2 == 0) goto L_0x010f
            r2.close()
        L_0x010f:
            r12.mIsRunning = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.KeepAliveSender.lambda$start$0():void");
    }

    public void stop() {
        if (this.mTask != null) {
            PowerManager.WakeLock wakeLock = this.mWakeLock;
            if (wakeLock != null && wakeLock.isHeld()) {
                this.mWakeLock.release();
                Log.i(this.LOG_TAG, "KeepAliveSender: release WakeLock");
            }
            Log.i(this.LOG_TAG, "KeepAliveSender: stop");
            synchronized (this.mLock) {
                this.mIsRunning = false;
            }
            this.mTask.interrupt();
            try {
                this.mTask.join(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.mTask = null;
        }
    }
}
