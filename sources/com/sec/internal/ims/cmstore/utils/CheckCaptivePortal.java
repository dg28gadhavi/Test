package com.sec.internal.ims.cmstore.utils;

import android.net.Network;
import android.util.Log;

public class CheckCaptivePortal {
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final int WALLED_GARDEN_RETRY_COUNT = 3;
    private static final int WALLED_GARDEN_RETRY_INTERVAL = 3000;
    private static final String WALLED_GARDEN_URL = "http://clients3.google.com/generate_204";

    public static boolean isGoodWifi(Network network) {
        for (int i = 0; i <= 3; i++) {
            if (!checkWifiWorksFineWithWalledGardenUrl(network)) {
                return false;
            }
            sleepHelper(3000);
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x004c  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0051 A[SYNTHETIC, Splitter:B:28:0x0051] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x005d  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0062 A[SYNTHETIC, Splitter:B:37:0x0062] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean checkWifiWorksFineWithWalledGardenUrl(android.net.Network r5) {
        /*
            r0 = 0
            r1 = 0
            java.net.URL r2 = new java.net.URL     // Catch:{ IOException -> 0x0045, all -> 0x0042 }
            java.lang.String r3 = "http://clients3.google.com/generate_204"
            r2.<init>(r3)     // Catch:{ IOException -> 0x0045, all -> 0x0042 }
            java.net.URLConnection r5 = r5.openConnection(r2)     // Catch:{ IOException -> 0x0045, all -> 0x0042 }
            java.net.HttpURLConnection r5 = (java.net.HttpURLConnection) r5     // Catch:{ IOException -> 0x0045, all -> 0x0042 }
            r5.setInstanceFollowRedirects(r0)     // Catch:{ IOException -> 0x003d, all -> 0x0038 }
            r2 = 10000(0x2710, float:1.4013E-41)
            r5.setConnectTimeout(r2)     // Catch:{ IOException -> 0x003d, all -> 0x0038 }
            r5.setReadTimeout(r2)     // Catch:{ IOException -> 0x003d, all -> 0x0038 }
            r5.setUseCaches(r0)     // Catch:{ IOException -> 0x003d, all -> 0x0038 }
            java.io.InputStream r1 = r5.getInputStream()     // Catch:{ IOException -> 0x003d, all -> 0x0038 }
            int r2 = r5.getResponseCode()     // Catch:{ IOException -> 0x003d, all -> 0x0038 }
            r3 = 204(0xcc, float:2.86E-43)
            if (r2 != r3) goto L_0x002a
            r0 = 1
        L_0x002a:
            r5.disconnect()
            if (r1 == 0) goto L_0x0037
            r1.close()     // Catch:{ IOException -> 0x0033 }
            goto L_0x0037
        L_0x0033:
            r5 = move-exception
            r5.printStackTrace()
        L_0x0037:
            return r0
        L_0x0038:
            r0 = move-exception
            r4 = r1
            r1 = r5
            r5 = r4
            goto L_0x005b
        L_0x003d:
            r2 = move-exception
            r4 = r1
            r1 = r5
            r5 = r4
            goto L_0x0047
        L_0x0042:
            r0 = move-exception
            r5 = r1
            goto L_0x005b
        L_0x0045:
            r2 = move-exception
            r5 = r1
        L_0x0047:
            r2.printStackTrace()     // Catch:{ all -> 0x005a }
            if (r1 == 0) goto L_0x004f
            r1.disconnect()
        L_0x004f:
            if (r5 == 0) goto L_0x0059
            r5.close()     // Catch:{ IOException -> 0x0055 }
            goto L_0x0059
        L_0x0055:
            r5 = move-exception
            r5.printStackTrace()
        L_0x0059:
            return r0
        L_0x005a:
            r0 = move-exception
        L_0x005b:
            if (r1 == 0) goto L_0x0060
            r1.disconnect()
        L_0x0060:
            if (r5 == 0) goto L_0x006a
            r5.close()     // Catch:{ IOException -> 0x0066 }
            goto L_0x006a
        L_0x0066:
            r5 = move-exception
            r5.printStackTrace()
        L_0x006a:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.CheckCaptivePortal.checkWifiWorksFineWithWalledGardenUrl(android.net.Network):boolean");
    }

    private static void sleepHelper(int i) {
        try {
            Thread.sleep((long) i);
        } catch (InterruptedException e) {
            Log.e("Utils", "sleepHelper", e);
        }
    }
}
