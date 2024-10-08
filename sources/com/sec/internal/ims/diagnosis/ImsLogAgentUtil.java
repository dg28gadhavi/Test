package com.sec.internal.ims.diagnosis;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class ImsLogAgentUtil {
    private static final String AUTHORITY = "content://com.sec.imsservice.log";
    private static final String LOG_TAG = "ImsLogAgentUtil";
    private static final Map<Integer, ContentValues> sCommonHeader = new HashMap<Integer, ContentValues>() {
        {
            put(0, new ContentValues());
            put(1, new ContentValues());
        }
    };

    public static void updateCommonHeader(Context context, int i, String str, String str2, String str3) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DiagnosisConstants.COMMON_KEY_SIM_SLOT, Integer.valueOf(i));
        contentValues.put(DiagnosisConstants.COMMON_KEY_OMC_NW_CODE, str);
        contentValues.put(DiagnosisConstants.COMMON_KEY_MNO_NAME, str2);
        String replace = String.format("%-6s", new Object[]{str3}).replace(' ', '#');
        contentValues.put(DiagnosisConstants.COMMON_KEY_PLMN, replace);
        if (context != null) {
            Map<Integer, ContentValues> map = sCommonHeader;
            if (map.get(Integer.valueOf(i)) != null) {
                synchronized (map) {
                    SharedPreferences sharedPref = ImsSharedPrefHelper.getSharedPref(i, context, ImsSharedPrefHelper.PRE_COMMON_HEADER, 0, false);
                    String string = sharedPref.getString(DiagnosisConstants.COMMON_KEY_PLMN, "");
                    String string2 = sharedPref.getString(DiagnosisConstants.COMMON_KEY_MNO_NAME, "");
                    int i2 = sharedPref.getInt(DiagnosisConstants.COMMON_KEY_SPEC_REVISION, 1);
                    IMSLog.d(LOG_TAG, i, "setCommonHeader: oldMccMnc [" + string + "], oldMnoName [" + string2 + "], oldSREV [" + i2 + "] ==> newMccMnc [" + replace + "], newMnoName [" + str2 + "], newSREV [" + 28 + "]");
                    if (!TextUtils.equals(string, replace) || !TextUtils.equals(string2, str2) || 28 != i2) {
                        if (map.get(Integer.valueOf(i)).size() == 0) {
                            ImsSharedPrefHelper.getSharedPref(i, context, "DRPT", 0, false).edit().clear().apply();
                            ImsSharedPrefHelper.getSharedPref(i, context, DiagnosisConstants.FEATURE_DRCS, 0, false).edit().clear().apply();
                            IMSLog.i(LOG_TAG, i, "Discard stored DRPT/DRCS due to change of common header");
                        } else {
                            IMSLog.i(LOG_TAG, i, "SIM howswap; DRPT/DRCS might be sent already when ABSENT");
                        }
                    }
                    contentValues.put(DiagnosisConstants.COMMON_KEY_SPEC_REVISION, 28);
                    map.get(Integer.valueOf(i)).putAll(contentValues);
                    SharedPreferences.Editor edit = sharedPref.edit();
                    for (String next : contentValues.keySet()) {
                        Object obj = contentValues.get(next);
                        if (obj == null) {
                            Log.e(LOG_TAG, "setCommonHeader: [" + next + "] is null!");
                        } else if (obj instanceof Integer) {
                            edit.putInt(next, ((Integer) obj).intValue());
                        } else if (obj instanceof String) {
                            edit.putString(next, (String) obj);
                        } else if (obj instanceof Long) {
                            edit.putLong(next, ((Long) obj).longValue());
                        } else {
                            Log.e(LOG_TAG, "setCommonHeader: [" + next + "] has wrong data type!");
                        }
                    }
                    edit.apply();
                }
            }
        }
    }

    public static ContentValues getCommonHeader(Context context, int i) {
        ContentValues contentValues = sCommonHeader.get(Integer.valueOf(i));
        if (CollectionUtils.isNullOrEmpty(contentValues)) {
            return new ContentValues();
        }
        for (Map.Entry next : ImsSharedPrefHelper.getSharedPref(i, context, ImsSharedPrefHelper.PRE_COMMON_HEADER, 0, false).getAll().entrySet()) {
            String str = (String) next.getKey();
            Object value = next.getValue();
            if (value instanceof Integer) {
                contentValues.put(str, Integer.valueOf(((Integer) value).intValue()));
            } else if (value instanceof String) {
                contentValues.put(str, (String) value);
            } else if (value instanceof Long) {
                contentValues.put(str, Long.valueOf(((Long) value).longValue()));
            }
        }
        if (!contentValues.containsKey(DiagnosisConstants.COMMON_KEY_SPEC_REVISION)) {
            contentValues.put(DiagnosisConstants.COMMON_KEY_SPEC_REVISION, 28);
        }
        if (!contentValues.containsKey(DiagnosisConstants.COMMON_KEY_OMC_NW_CODE)) {
            contentValues.put(DiagnosisConstants.COMMON_KEY_OMC_NW_CODE, OmcCode.getNWCode(i));
        }
        if (!contentValues.containsKey(DiagnosisConstants.COMMON_KEY_SIM_SLOT)) {
            contentValues.put(DiagnosisConstants.COMMON_KEY_SIM_SLOT, Integer.valueOf(i));
        }
        return contentValues;
    }

    private static void passEventLog(Context context, String str) {
        try {
            context.getContentResolver().call(Uri.parse(AUTHORITY), DiagnosisConstants.CALL_METHOD_LOGANDADD, str, (Bundle) null);
        } catch (Exception e) {
            Log.e(LOG_TAG, "passEventLog: exception occurred: " + e);
        }
    }

    public static void sendLogToAgent(int i, Context context, String str, ContentValues contentValues) {
        Integer asInteger = contentValues.getAsInteger(DiagnosisConstants.KEY_SEND_MODE);
        if (asInteger == null || asInteger.intValue() != 0) {
            contentValues.put(DiagnosisConstants.KEY_SEND_MODE, 0);
        }
        contentValues.put(DiagnosisConstants.KEY_FEATURE, str);
        try {
            context.getContentResolver().insert(UriUtil.buildUri(AUTHORITY, i), contentValues);
        } catch (Exception e) {
            passEventLog(context, e.toString());
        }
    }

    public static void storeLogToAgent(int i, Context context, String str, ContentValues contentValues) {
        Integer asInteger = contentValues.getAsInteger(DiagnosisConstants.KEY_SEND_MODE);
        if (asInteger == null || asInteger.intValue() == 0) {
            contentValues.put(DiagnosisConstants.KEY_SEND_MODE, 1);
        }
        contentValues.put(DiagnosisConstants.KEY_FEATURE, str);
        try {
            context.getContentResolver().insert(UriUtil.buildUri(AUTHORITY, i), contentValues);
        } catch (Exception e) {
            passEventLog(context, e.toString());
        }
    }

    public static void requestToSendStoredLog(int i, Context context, String str) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.update(UriUtil.buildUri("content://com.sec.imsservice.log/send/" + str, i), (ContentValues) null, (String) null, (String[]) null);
        } catch (Exception e) {
            passEventLog(context, e.toString());
        }
    }
}
