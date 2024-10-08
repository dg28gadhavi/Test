package com.sec.internal.ims.entitlement.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class SimSwapNSDSConfigHelper extends NSDSConfigHelper {
    public static final String KEY_NATIVE_MSISDN = "NATIVE_MSISDN";
    private static final String LOG_TAG = "SimSwapNSDSConfigHelper";
    private static Map<String, String> sDataMap = new HashMap();

    public static synchronized String getConfigValue(Context context, String str) {
        String str2;
        synchronized (SimSwapNSDSConfigHelper.class) {
            if (sDataMap.isEmpty()) {
                Map<String, String> loadConfigFromDb = loadConfigFromDb(context);
                if (!loadConfigFromDb.isEmpty()) {
                    sDataMap.putAll(loadConfigFromDb);
                    addDerivedConfigToMap();
                }
            }
            str2 = sDataMap.get(str);
        }
        return str2;
    }

    public static synchronized void clear() {
        synchronized (SimSwapNSDSConfigHelper.class) {
            sDataMap.clear();
        }
    }

    private static void addDerivedConfigToMap() {
        String str = sDataMap.get(NSDSConfigHelper.KEY_URL_ENTITLEMENT_SERVER);
        if (str != null && !str.endsWith("generic_devices")) {
            Map<String, String> map = sDataMap;
            map.put(NSDSConfigHelper.KEY_URL_ENTITLEMENT_SERVER, str + "/generic_devices");
        }
    }

    public static Map<String, String> loadConfigFromDb(Context context) {
        Cursor query;
        HashMap hashMap = new HashMap();
        try {
            query = context.getContentResolver().query(NSDSContractExt.SimSwapNsdsConfigs.CONTENT_URI, new String[]{NSDSContractExt.NsdsConfigColumns.PNAME, NSDSContractExt.NsdsConfigColumns.PVALUE}, (String) null, (String[]) null, (String) null);
            if (query != null) {
                while (query.moveToNext()) {
                    String string = query.getString(0);
                    String string2 = query.getString(1);
                    hashMap.put(string, string2);
                    String str = LOG_TAG;
                    IMSLog.s(str, "Key:" + string + " Value:" + string2);
                }
            }
            if (query != null) {
                query.close();
            }
        } catch (SQLiteException e) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "!!!Could not load nsds config from db" + e.getMessage());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return hashMap;
        throw th;
    }
}
