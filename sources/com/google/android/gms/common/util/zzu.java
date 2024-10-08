package com.google.android.gms.common.util;

import android.os.Process;

public final class zzu {
    private static String zzglf;
    private static final int zzglg = Process.myPid();

    public static String zzany() {
        if (zzglf == null) {
            zzglf = zzci(zzglg);
        }
        return zzglf;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: java.lang.String} */
    /* JADX WARNING: type inference failed for: r0v0 */
    /* JADX WARNING: type inference failed for: r0v2, types: [java.io.Closeable] */
    /* JADX WARNING: type inference failed for: r0v3 */
    /* JADX WARNING: type inference failed for: r0v5 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.lang.String zzci(int r6) {
        /*
            r0 = 0
            if (r6 > 0) goto L_0x0004
            return r0
        L_0x0004:
            android.os.StrictMode$ThreadPolicy r1 = android.os.StrictMode.allowThreadDiskReads()     // Catch:{ IOException -> 0x0043, all -> 0x003e }
            java.io.BufferedReader r2 = new java.io.BufferedReader     // Catch:{ all -> 0x0039 }
            java.io.FileReader r3 = new java.io.FileReader     // Catch:{ all -> 0x0039 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0039 }
            r5 = 25
            r4.<init>(r5)     // Catch:{ all -> 0x0039 }
            java.lang.String r5 = "/proc/"
            r4.append(r5)     // Catch:{ all -> 0x0039 }
            r4.append(r6)     // Catch:{ all -> 0x0039 }
            java.lang.String r6 = "/cmdline"
            r4.append(r6)     // Catch:{ all -> 0x0039 }
            java.lang.String r6 = r4.toString()     // Catch:{ all -> 0x0039 }
            r3.<init>(r6)     // Catch:{ all -> 0x0039 }
            r2.<init>(r3)     // Catch:{ all -> 0x0039 }
            android.os.StrictMode.setThreadPolicy(r1)     // Catch:{ IOException -> 0x0044, all -> 0x0036 }
            java.lang.String r6 = r2.readLine()     // Catch:{ IOException -> 0x0044, all -> 0x0036 }
            java.lang.String r0 = r6.trim()     // Catch:{ IOException -> 0x0044, all -> 0x0036 }
            goto L_0x0044
        L_0x0036:
            r6 = move-exception
            r0 = r2
            goto L_0x003f
        L_0x0039:
            r6 = move-exception
            android.os.StrictMode.setThreadPolicy(r1)     // Catch:{ IOException -> 0x0043, all -> 0x003e }
            throw r6     // Catch:{ IOException -> 0x0043, all -> 0x003e }
        L_0x003e:
            r6 = move-exception
        L_0x003f:
            com.google.android.gms.common.util.zzp.closeQuietly(r0)
            throw r6
        L_0x0043:
            r2 = r0
        L_0x0044:
            com.google.android.gms.common.util.zzp.closeQuietly(r2)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.common.util.zzu.zzci(int):java.lang.String");
    }
}
