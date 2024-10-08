package com.sec.internal.ims.servicemodules.ss;

import android.database.Cursor;
import android.text.TextUtils;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;

public class ApnSettings {
    private static final String[] APN_PROJECTION = {"type", "name", NSDSContractExt.ProvisioningParametersColumns.APN, "proxy", "port"};
    private static final int COLUMN_PORT = 4;
    private static final int COLUMN_PROXY = 3;
    private static final int COLUMN_TYPE = 0;
    private static final String LOG_TAG = "ApnSettings";
    private final String mDebugText;
    private final String mProxyAddress;
    private final int mProxyPort;

    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        com.sec.internal.log.IMSLog.e(LOG_TAG, "Invalid port " + r12 + ", use 80");
        r12 = 80;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* JADX WARNING: Missing exception handler attribute for start block: B:14:0x007d */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.sec.internal.ims.servicemodules.ss.ApnSettings load(android.content.Context r10, java.lang.String r11, java.lang.String r12, int r13) {
        /*
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "Loading APN using name "
            r0.append(r1)
            r0.append(r11)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "ApnSettings"
            com.sec.internal.log.IMSLog.i(r1, r0)
            java.lang.String r11 = trimWithNullCheck(r11)
            boolean r0 = android.text.TextUtils.isEmpty(r11)
            r2 = 0
            if (r0 != 0) goto L_0x002a
            java.lang.String[] r11 = new java.lang.String[]{r11}
            java.lang.String r0 = "apn=?"
            r8 = r11
            r7 = r0
            goto L_0x002c
        L_0x002a:
            r7 = r2
            r8 = r7
        L_0x002c:
            android.content.ContentResolver r4 = r10.getContentResolver()
            android.net.Uri r11 = android.provider.Telephony.Carriers.CONTENT_URI
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r3 = "/subId/"
            r0.append(r3)
            r0.append(r13)
            java.lang.String r13 = r0.toString()
            android.net.Uri r5 = android.net.Uri.withAppendedPath(r11, r13)
            java.lang.String[] r6 = APN_PROJECTION
            r9 = 0
            r3 = r10
            android.database.Cursor r10 = android.database.sqlite.SqliteWrapper.query(r3, r4, r5, r6, r7, r8, r9)
            if (r10 == 0) goto L_0x00af
        L_0x0051:
            boolean r11 = r10.moveToNext()     // Catch:{ all -> 0x00a5 }
            if (r11 == 0) goto L_0x00af
            r11 = 0
            java.lang.String r11 = r10.getString(r11)     // Catch:{ all -> 0x00a5 }
            boolean r11 = isValidApnType(r11, r12)     // Catch:{ all -> 0x00a5 }
            if (r11 == 0) goto L_0x0051
            r11 = 3
            java.lang.String r11 = r10.getString(r11)     // Catch:{ all -> 0x00a5 }
            java.lang.String r11 = trimWithNullCheck(r11)     // Catch:{ all -> 0x00a5 }
            java.lang.String r11 = com.android.net.module.util.Inet4AddressUtils.trimAddressZeros(r11)     // Catch:{ all -> 0x00a5 }
            r12 = 4
            java.lang.String r12 = r10.getString(r12)     // Catch:{ all -> 0x00a5 }
            java.lang.String r12 = trimWithNullCheck(r12)     // Catch:{ all -> 0x00a5 }
            int r12 = java.lang.Integer.parseInt(r12)     // Catch:{ NumberFormatException -> 0x007d }
            goto L_0x0098
        L_0x007d:
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ all -> 0x00a5 }
            r13.<init>()     // Catch:{ all -> 0x00a5 }
            java.lang.String r0 = "Invalid port "
            r13.append(r0)     // Catch:{ all -> 0x00a5 }
            r13.append(r12)     // Catch:{ all -> 0x00a5 }
            java.lang.String r12 = ", use 80"
            r13.append(r12)     // Catch:{ all -> 0x00a5 }
            java.lang.String r12 = r13.toString()     // Catch:{ all -> 0x00a5 }
            com.sec.internal.log.IMSLog.e(r1, r12)     // Catch:{ all -> 0x00a5 }
            r12 = 80
        L_0x0098:
            com.sec.internal.ims.servicemodules.ss.ApnSettings r13 = new com.sec.internal.ims.servicemodules.ss.ApnSettings     // Catch:{ all -> 0x00a5 }
            java.lang.String r0 = getDebugText(r10)     // Catch:{ all -> 0x00a5 }
            r13.<init>(r11, r12, r0)     // Catch:{ all -> 0x00a5 }
            r10.close()
            return r13
        L_0x00a5:
            r11 = move-exception
            r10.close()     // Catch:{ all -> 0x00aa }
            goto L_0x00ae
        L_0x00aa:
            r10 = move-exception
            r11.addSuppressed(r10)
        L_0x00ae:
            throw r11
        L_0x00af:
            if (r10 == 0) goto L_0x00b4
            r10.close()
        L_0x00b4:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.ApnSettings.load(android.content.Context, java.lang.String, java.lang.String, int):com.sec.internal.ims.servicemodules.ss.ApnSettings");
    }

    private static String getDebugText(Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        sb.append("APN [");
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            String columnName = cursor.getColumnName(i);
            String string = cursor.getString(i);
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(columnName);
            sb.append('=');
            if (!TextUtils.isEmpty(string)) {
                sb.append(string);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String trimWithNullCheck(String str) {
        if (str != null) {
            return str.trim();
        }
        return null;
    }

    public ApnSettings(String str, int i, String str2) {
        this.mProxyAddress = str;
        this.mProxyPort = i;
        this.mDebugText = str2;
    }

    public String getProxyAddress() {
        return this.mProxyAddress;
    }

    public int getProxyPort() {
        return this.mProxyPort;
    }

    private static boolean isValidApnType(String str, String str2) {
        if (TextUtils.isEmpty(str)) {
            return true;
        }
        for (String trim : str.split(",")) {
            String trim2 = trim.trim();
            if (trim2.equals(str2) || trim2.equals("*")) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return this.mDebugText;
    }
}
