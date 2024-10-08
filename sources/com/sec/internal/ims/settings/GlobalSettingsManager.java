package com.sec.internal.ims.settings;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import java.util.HashMap;

public class GlobalSettingsManager {
    private static final HashMap<Integer, GlobalSettingsManager> sInstances = new HashMap<>();
    private GlobalSettingsRepo mGlobalSettingsRepo;

    private GlobalSettingsManager(Context context, int i) {
        this.mGlobalSettingsRepo = new GlobalSettingsRepo(context, i);
    }

    public static GlobalSettingsManager getInstance(Context context, int i) {
        HashMap<Integer, GlobalSettingsManager> hashMap = sInstances;
        synchronized (hashMap) {
            if (hashMap.containsKey(Integer.valueOf(i))) {
                GlobalSettingsManager globalSettingsManager = hashMap.get(Integer.valueOf(i));
                return globalSettingsManager;
            }
            hashMap.put(Integer.valueOf(i), new GlobalSettingsManager(context, i));
            return hashMap.get(Integer.valueOf(i));
        }
    }

    public synchronized GlobalSettingsRepo getGlobalSettings() {
        return this.mGlobalSettingsRepo;
    }

    public boolean getBoolean(String str, boolean z) {
        Cursor query = this.mGlobalSettingsRepo.query(new String[]{str}, (String) null, (String[]) null);
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    boolean z2 = false;
                    String string = query.getString(0);
                    if (!TextUtils.isEmpty(string)) {
                        if (CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(string) || "1".equalsIgnoreCase(string)) {
                            z2 = true;
                        }
                        query.close();
                        return z2;
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (query != null) {
            query.close();
        }
        return z;
        throw th;
    }

    public String getString(String str, String str2) {
        Cursor query = this.mGlobalSettingsRepo.query(new String[]{str}, (String) null, (String[]) null);
        if (query != null) {
            try {
                if (query.moveToFirst() && query.getString(0) != null) {
                    str2 = query.getString(0);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (query != null) {
            query.close();
        }
        return str2;
        throw th;
    }

    public String[] getStringArray(String str, String[] strArr) {
        Cursor query = this.mGlobalSettingsRepo.query(new String[]{str}, (String) null, (String[]) null);
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    String string = query.getString(0);
                    if (!TextUtils.isEmpty(string)) {
                        String[] split = string.replaceAll("\\[|\\]|\"", "").trim().split(",");
                        query.close();
                        return split;
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (query != null) {
            query.close();
        }
        return strArr;
        throw th;
    }

    public int getInt(String str, int i) {
        Cursor query = this.mGlobalSettingsRepo.query(new String[]{str}, (String) null, (String[]) null);
        if (query != null) {
            try {
                if (query.moveToFirst() && !TextUtils.isEmpty(query.getString(0))) {
                    i = Integer.parseInt(query.getString(0));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                query.close();
                return i;
            } catch (Throwable th) {
                try {
                    query.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        }
        if (query != null) {
            query.close();
        }
        return i;
    }
}
