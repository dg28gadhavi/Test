package com.sec.internal.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.config.ConfigConstants;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public final class DmConfigHelper {
    private static final String LOG_TAG = "DmConfigHelper";
    private static Map<String, String> mServiceSwitchDmMap;

    static {
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
        mServiceSwitchDmMap = concurrentHashMap;
        concurrentHashMap.put("mmtel", ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED);
        mServiceSwitchDmMap.put("mmtel-video", ConfigConstants.ConfigPath.OMADM_LVC_ENABLED);
        mServiceSwitchDmMap.put(SipMsg.EVENT_PRESENCE, ConfigConstants.ConfigPath.OMADM_EAB_SETTING);
    }

    @Deprecated
    public static Boolean readBool(Context context, String str) {
        return readBool(context, str, Boolean.FALSE, 0);
    }

    public static Boolean readBool(Context context, String str, Boolean bool, int i) {
        String read = read(context, str, (String) null, i);
        return read != null ? Boolean.valueOf("1".equals(read)) : bool;
    }

    public static Long readLong(Context context, String str, Long l, int i) {
        String read = read(context, str, (String) null, i);
        if (!TextUtils.isEmpty(read)) {
            try {
                return Long.valueOf(Long.parseLong(read));
            } catch (NumberFormatException unused) {
            }
        }
        return l;
    }

    public static Integer readInt(Context context, String str, Integer num) {
        return readInt(context, str, num, 0);
    }

    public static Integer readInt(Context context, String str, Integer num, int i) {
        String read = read(context, str, (String) null, i);
        if (!TextUtils.isEmpty(read)) {
            try {
                return Integer.valueOf(Integer.parseInt(read));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return num;
    }

    public static String read(Context context, String str, String str2, int i) {
        Map<String, String> read = read(context, str, i);
        if (read == null) {
            return str2;
        }
        Locale locale = Locale.US;
        String str3 = read.get(str.toLowerCase(locale));
        if (TextUtils.isEmpty(str3)) {
            str3 = read.get(("omadm/./3GPP_IMS/" + str).toLowerCase(locale));
        }
        return !TextUtils.isEmpty(str3) ? str3 : str2;
    }

    public static Map<String, String> read(Context context, String str, int i) {
        TreeMap treeMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        Cursor query = context.getContentResolver().query(UriUtil.buildUri("content://com.samsung.rcs.dmconfigurationprovider/" + str, i), (String[]) null, (String) null, (String[]) null, (String) null);
        if (query == null) {
            if (query != null) {
                query.close();
            }
            return treeMap;
        }
        try {
            if (query.moveToFirst()) {
                do {
                    treeMap.put(query.getString(0), query.getString(1));
                } while (query.moveToNext());
            }
            query.close();
            return treeMap;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public static boolean readSwitch(Context context, String str, boolean z, int i) {
        String str2;
        String str3 = null;
        if (mServiceSwitchDmMap.containsKey(str)) {
            str2 = mServiceSwitchDmMap.get(str);
            str3 = read(context, str2, (String) null, i);
        } else {
            str2 = null;
        }
        if (str3 == null) {
            return z;
        }
        Log.d(LOG_TAG, "readBool(" + str + ") from " + str2 + ": [" + str3 + "]");
        return "1".equals(str3);
    }

    public static void setImsUserSetting(Context context, String str, int i, int i2) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", str);
        contentValues.put("value", Integer.valueOf(i));
        context.getContentResolver().update(UriUtil.buildUri("content://com.sec.ims.settings/imsusersetting", i2), contentValues, (String) null, (String[]) null);
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(2:10|11) */
    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        android.util.Log.d(LOG_TAG, "getImsUserSetting: false due to IllegalArgumentException");
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:10:0x004d */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:5:0x0024=Splitter:B:5:0x0024, B:17:0x006a=Splitter:B:17:0x006a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int getImsUserSetting(android.content.Context r8, java.lang.String r9, int r10) {
        /*
            android.content.ContentValues r0 = new android.content.ContentValues
            r0.<init>()
            java.lang.String r1 = "content://com.sec.ims.settings/imsusersetting"
            android.net.Uri r3 = com.sec.internal.helper.UriUtil.buildUri((java.lang.String) r1, (int) r10)
            android.content.ContentResolver r2 = r8.getContentResolver()
            java.lang.String[] r4 = new java.lang.String[]{r9}
            r5 = 0
            r6 = 0
            r7 = 0
            android.database.Cursor r8 = r2.query(r3, r4, r5, r6, r7)
            r1 = -1
            if (r8 == 0) goto L_0x006a
            int r2 = r8.getCount()     // Catch:{ all -> 0x0077 }
            if (r2 != 0) goto L_0x0024
            goto L_0x006a
        L_0x0024:
            boolean r10 = r8.moveToFirst()     // Catch:{ IllegalArgumentException -> 0x004d }
            if (r10 == 0) goto L_0x0054
        L_0x002a:
            java.lang.String r10 = "name"
            int r10 = r8.getColumnIndexOrThrow(r10)     // Catch:{ IllegalArgumentException -> 0x004d }
            java.lang.String r10 = r8.getString(r10)     // Catch:{ IllegalArgumentException -> 0x004d }
            java.lang.String r2 = "value"
            int r2 = r8.getColumnIndexOrThrow(r2)     // Catch:{ IllegalArgumentException -> 0x004d }
            int r2 = r8.getInt(r2)     // Catch:{ IllegalArgumentException -> 0x004d }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ IllegalArgumentException -> 0x004d }
            r0.put(r10, r2)     // Catch:{ IllegalArgumentException -> 0x004d }
            boolean r10 = r8.moveToNext()     // Catch:{ IllegalArgumentException -> 0x004d }
            if (r10 != 0) goto L_0x002a
            goto L_0x0054
        L_0x004d:
            java.lang.String r10 = LOG_TAG     // Catch:{ all -> 0x0077 }
            java.lang.String r2 = "getImsUserSetting: false due to IllegalArgumentException"
            android.util.Log.d(r10, r2)     // Catch:{ all -> 0x0077 }
        L_0x0054:
            r8.close()
            java.lang.Integer r8 = java.lang.Integer.valueOf(r1)
            java.lang.Integer r10 = r0.getAsInteger(r9)
            if (r10 == 0) goto L_0x0065
            java.lang.Integer r8 = r0.getAsInteger(r9)
        L_0x0065:
            int r8 = r8.intValue()
            return r8
        L_0x006a:
            java.lang.String r9 = LOG_TAG     // Catch:{ all -> 0x0077 }
            java.lang.String r0 = "getImsUserSetting: not found"
            com.sec.internal.log.IMSLog.d(r9, r10, r0)     // Catch:{ all -> 0x0077 }
            if (r8 == 0) goto L_0x0076
            r8.close()
        L_0x0076:
            return r1
        L_0x0077:
            r9 = move-exception
            if (r8 == 0) goto L_0x0082
            r8.close()     // Catch:{ all -> 0x007e }
            goto L_0x0082
        L_0x007e:
            r8 = move-exception
            r9.addSuppressed(r8)
        L_0x0082:
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.DmConfigHelper.getImsUserSetting(android.content.Context, java.lang.String, int):int");
    }

    public static void setImsSwitch(Context context, String str, boolean z, int i) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("service", str);
        contentValues.put("enabled", Boolean.valueOf(z));
        context.getContentResolver().update(UriUtil.buildUri("content://com.sec.ims.settings/imsswitch", i), contentValues, (String) null, (String[]) null);
    }

    public static boolean isImsSwitchEnabled(Context context, String str, int i) {
        return getImsSwitchValue(context, str, i) == 1;
    }

    public static int getImsSwitchValue(Context context, String str, int i) {
        ContentValues imsSwitchValue = getImsSwitchValue(context, new String[]{str}, i);
        if (imsSwitchValue == null || imsSwitchValue.size() == 0) {
            Log.d(LOG_TAG, "getImsSwitchValue: value is not exist.");
            return 0;
        }
        Integer num = 0;
        if (imsSwitchValue.getAsInteger(str) != null) {
            num = imsSwitchValue.getAsInteger(str);
        }
        return num.intValue();
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(2:10|11) */
    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        android.util.Log.d(LOG_TAG, "isServiceEnabled: false due to IllegalArgumentException");
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:10:0x0048 */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:5:0x0020=Splitter:B:5:0x0020, B:14:0x0053=Splitter:B:14:0x0053} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static android.content.ContentValues getImsSwitchValue(android.content.Context r8, java.lang.String[] r9, int r10) {
        /*
            android.content.ContentValues r0 = new android.content.ContentValues
            r0.<init>()
            java.lang.String r1 = "content://com.sec.ims.settings/imsswitch"
            android.net.Uri r3 = com.sec.internal.helper.UriUtil.buildUri((java.lang.String) r1, (int) r10)
            android.content.ContentResolver r2 = r8.getContentResolver()
            r5 = 0
            r6 = 0
            r7 = 0
            r4 = r9
            android.database.Cursor r8 = r2.query(r3, r4, r5, r6, r7)
            if (r8 == 0) goto L_0x0053
            int r9 = r8.getCount()     // Catch:{ all -> 0x0060 }
            if (r9 != 0) goto L_0x0020
            goto L_0x0053
        L_0x0020:
            boolean r9 = r8.moveToFirst()     // Catch:{ IllegalArgumentException -> 0x0048 }
            if (r9 == 0) goto L_0x004f
        L_0x0026:
            java.lang.String r9 = "name"
            int r9 = r8.getColumnIndexOrThrow(r9)     // Catch:{ IllegalArgumentException -> 0x0048 }
            java.lang.String r9 = r8.getString(r9)     // Catch:{ IllegalArgumentException -> 0x0048 }
            java.lang.String r10 = "enabled"
            int r10 = r8.getColumnIndexOrThrow(r10)     // Catch:{ IllegalArgumentException -> 0x0048 }
            int r10 = r8.getInt(r10)     // Catch:{ IllegalArgumentException -> 0x0048 }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)     // Catch:{ IllegalArgumentException -> 0x0048 }
            r0.put(r9, r10)     // Catch:{ IllegalArgumentException -> 0x0048 }
            boolean r9 = r8.moveToNext()     // Catch:{ IllegalArgumentException -> 0x0048 }
            if (r9 != 0) goto L_0x0026
            goto L_0x004f
        L_0x0048:
            java.lang.String r9 = LOG_TAG     // Catch:{ all -> 0x0060 }
            java.lang.String r10 = "isServiceEnabled: false due to IllegalArgumentException"
            android.util.Log.d(r9, r10)     // Catch:{ all -> 0x0060 }
        L_0x004f:
            r8.close()
            return r0
        L_0x0053:
            java.lang.String r9 = LOG_TAG     // Catch:{ all -> 0x0060 }
            java.lang.String r1 = "getImsSwitchValue: not found"
            com.sec.internal.log.IMSLog.d(r9, r10, r1)     // Catch:{ all -> 0x0060 }
            if (r8 == 0) goto L_0x005f
            r8.close()
        L_0x005f:
            return r0
        L_0x0060:
            r9 = move-exception
            if (r8 == 0) goto L_0x006b
            r8.close()     // Catch:{ all -> 0x0067 }
            goto L_0x006b
        L_0x0067:
            r8 = move-exception
            r9.addSuppressed(r8)
        L_0x006b:
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.DmConfigHelper.getImsSwitchValue(android.content.Context, java.lang.String[], int):android.content.ContentValues");
    }
}
