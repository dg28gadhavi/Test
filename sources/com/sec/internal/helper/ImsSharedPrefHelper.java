package com.sec.internal.helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ImsSharedPrefHelper {
    public static final String CARRIER_ID = "carrierId";
    public static final String CSC_INFO_PREF = "CSC_INFO_PREF";
    public static final String DEBUG_CONFIG = "Debug_config";
    public static final String DRPT = "DRPT";
    public static final String GLOBAL_GC_SETTINGS = "globalgcsettings";
    public static final String GLOBAL_SETTINGS = "globalsettings";
    public static final String IMS_CONFIG = "imsconfig";
    public static final String IMS_FEATURE = "imsfeature";
    public static final String IMS_PROFILE = "imsprofile";
    public static final String IMS_SWITCH = "imsswitch";
    public static final String IMS_USER_DATA = "ims_user_data";
    public static final String LAST_ACCESSED_COUNTRY_ISO = "last_accessed_country_iso";
    private static final String LOG_TAG = "ImsSharedPrefHelper";
    public static final String PREF = "pref";
    public static final String PRE_COMMON_HEADER = "previous_common_header";
    public static final String PROFILE = "profile";
    public static final String SAVED_IDC_PROCESS_MODE = "saved_idc_appdata_process_mode";
    public static final String SAVED_IMPU = "saved_impu";
    public static final String USER_CONFIG = "user_config";
    public static final String VALID_RCS_CONFIG = "validrcsconfig";
    private static final List<String> migrationListForCe = Arrays.asList(new String[]{IMS_USER_DATA, "profile", PREF, SAVED_IMPU});
    private static final List<String> saveWithPhoneIdList = Arrays.asList(new String[]{USER_CONFIG, IMS_USER_DATA, CSC_INFO_PREF, GLOBAL_SETTINGS, IMS_FEATURE, IMS_PROFILE, "imsswitch"});

    public static SharedPreferences getSharedPref(int i, Context context, String str, int i2, boolean z) {
        if (context == null || TextUtils.isEmpty(str)) {
            return null;
        }
        if ((z || DeviceUtil.isUserUnlocked(context)) && migrationListForCe.contains(str)) {
            String str2 = LOG_TAG;
            IMSLog.d(str2, i, "getSharedPref from CE : " + str);
            Context createCredentialProtectedStorageContext = context.createCredentialProtectedStorageContext();
            if (i < 0) {
                return createCredentialProtectedStorageContext.getSharedPreferences(str, i2);
            }
            return createCredentialProtectedStorageContext.getSharedPreferences(str + "_" + i, i2);
        } else if (i < 0) {
            return context.getSharedPreferences(str, i2);
        } else {
            return context.getSharedPreferences(str + "_" + i, i2);
        }
    }

    private static Optional<SharedPreferences> getSpAsOptional(int i, Context context, String str) {
        return Optional.ofNullable(getSharedPref(i, context, str, 0, false));
    }

    public static void save(int i, Context context, String str, String str2, String str3) {
        getSpAsOptional(i, context, str).ifPresent(new ImsSharedPrefHelper$$ExternalSyntheticLambda8(str2, str3));
    }

    public static void save(int i, Context context, String str, String str2, boolean z) {
        getSpAsOptional(i, context, str).ifPresent(new ImsSharedPrefHelper$$ExternalSyntheticLambda3(str2, z));
    }

    public static void save(int i, Context context, String str, String str2, int i2) {
        getSpAsOptional(i, context, str).ifPresent(new ImsSharedPrefHelper$$ExternalSyntheticLambda11(str2, i2));
    }

    public static void save(int i, Context context, String str, String str2, long j) {
        getSpAsOptional(i, context, str).ifPresent(new ImsSharedPrefHelper$$ExternalSyntheticLambda6(str2, j));
    }

    public static void save(int i, Context context, String str, String str2, Set<String> set) {
        getSpAsOptional(i, context, str).ifPresent(new ImsSharedPrefHelper$$ExternalSyntheticLambda13(str2, set));
    }

    public static String getString(int i, Context context, String str, String str2, String str3) {
        try {
            return (String) getSpAsOptional(i, context, str).map(new ImsSharedPrefHelper$$ExternalSyntheticLambda9(str2, str3)).orElse(str3);
        } catch (ClassCastException e) {
            if (str3.matches("-?\\d+")) {
                int parseInt = Integer.parseInt(str3);
                String valueOf = String.valueOf(getSpAsOptional(i, context, str).map(new ImsSharedPrefHelper$$ExternalSyntheticLambda10(str2, parseInt)).orElse(Integer.valueOf(parseInt)));
                String str4 = LOG_TAG;
                IMSLog.i(str4, i, "getString: ClassCastException but okay with getInt() for " + str2);
                return valueOf;
            }
            e.printStackTrace();
            return str3;
        }
    }

    public static Set<String> getStringSet(int i, Context context, String str, String str2, Set<String> set) {
        return (Set) getSpAsOptional(i, context, str).map(new ImsSharedPrefHelper$$ExternalSyntheticLambda7(str2, set)).orElse(set);
    }

    public static boolean getBoolean(int i, Context context, String str, String str2, boolean z) {
        return ((Boolean) getSpAsOptional(i, context, str).map(new ImsSharedPrefHelper$$ExternalSyntheticLambda12(str2, z)).orElse(Boolean.valueOf(z))).booleanValue();
    }

    public static int getInt(int i, Context context, String str, String str2, int i2) {
        return ((Integer) getSpAsOptional(i, context, str).map(new ImsSharedPrefHelper$$ExternalSyntheticLambda5(str2, i2)).orElse(Integer.valueOf(i2))).intValue();
    }

    public static long getLong(int i, Context context, String str, String str2, long j) {
        return ((Long) getSpAsOptional(i, context, str).map(new ImsSharedPrefHelper$$ExternalSyntheticLambda0(str2, j)).orElse(Long.valueOf(j))).longValue();
    }

    public static void remove(int i, Context context, String str, String str2) {
        getSpAsOptional(i, context, str).ifPresent(new ImsSharedPrefHelper$$ExternalSyntheticLambda14(str2));
    }

    public static void clear(int i, Context context, String str) {
        getSpAsOptional(i, context, str).ifPresent(new ImsSharedPrefHelper$$ExternalSyntheticLambda2());
    }

    public static Map<String, String> getStringArray(int i, Context context, String str, String[] strArr) {
        ArrayMap arrayMap = new ArrayMap(strArr.length);
        getSpAsOptional(i, context, str).ifPresent(new ImsSharedPrefHelper$$ExternalSyntheticLambda1(strArr, arrayMap));
        return arrayMap;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ void lambda$getStringArray$13(String[] strArr, Map map, SharedPreferences sharedPreferences) {
        for (String str : strArr) {
            map.put(str, sharedPreferences.getString(str, ""));
        }
    }

    public static void put(int i, Context context, String str, ContentValues contentValues) {
        getSpAsOptional(i, context, str).ifPresent(new ImsSharedPrefHelper$$ExternalSyntheticLambda4(contentValues));
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ void lambda$put$14(ContentValues contentValues, SharedPreferences sharedPreferences) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        for (String next : contentValues.keySet()) {
            edit.putString(next, contentValues.getAsString(next));
        }
        edit.apply();
    }

    public static void migrateToCeStorage(Context context) {
        String str = LOG_TAG;
        IMSLog.d(str, "migrate shared preferences to CE storage");
        if (context == null) {
            IMSLog.d(str, "context is null ");
            return;
        }
        int phoneCount = ((TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY)).getPhoneCount();
        Context createCredentialProtectedStorageContext = context.createCredentialProtectedStorageContext();
        for (String next : migrationListForCe) {
            if (saveWithPhoneIdList.contains(next)) {
                for (int i = 0; i < phoneCount; i++) {
                    if (!createCredentialProtectedStorageContext.moveSharedPreferencesFrom(context, next + "_" + i)) {
                        IMSLog.e(LOG_TAG, "Failed to move shared preferences.");
                        return;
                    }
                    if (!context.deleteSharedPreferences(next + "_" + i)) {
                        IMSLog.e(LOG_TAG, "Failed delete shared preferences on DE.");
                        return;
                    }
                }
                continue;
            } else if (!createCredentialProtectedStorageContext.moveSharedPreferencesFrom(context, next)) {
                IMSLog.e(LOG_TAG, "Failed to move shared preferences.");
                return;
            } else if (!context.deleteSharedPreferences(next)) {
                IMSLog.e(LOG_TAG, "Failed delete shared preferences on DE.");
                return;
            }
        }
    }
}
