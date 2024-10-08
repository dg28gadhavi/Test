package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.content.SharedPreferences;
import com.sec.internal.ims.rcs.util.RcsUtils;

public class ImUserPreference {
    private static final String FT_AUTO_ACCEPT_SIM1 = "FT_AUTO_ACCEPT";
    private static final String FT_AUTO_ACCEPT_SIM2 = "FT_AUTO_ACCEPT_SIM2";
    private static final String SHARED_PREFS_NAME = "im_user_prefs";
    private static final String USER_ALIAS = "USER_ALIAS";
    private static final String USER_ALIAS_SIM1 = "USER_ALIAS_SIM1";
    private static final String USER_ALIAS_SIM2 = "USER_ALIAS_SIM2";
    private static ImUserPreference sInstance;

    private String getFtAutAcceptPrefByPhoneId(int i) {
        return i == 1 ? FT_AUTO_ACCEPT_SIM2 : FT_AUTO_ACCEPT_SIM1;
    }

    private String getUserAliasPrefByPhoneId(int i) {
        return i == 1 ? USER_ALIAS_SIM2 : USER_ALIAS_SIM1;
    }

    private ImUserPreference() {
    }

    public static ImUserPreference getInstance() {
        if (sInstance == null) {
            sInstance = new ImUserPreference();
        }
        return sInstance;
    }

    public void setFtAutAccept(Context context, int i, int i2) {
        if (RcsUtils.DualRcs.isDualRcsSettings()) {
            persist(context, getFtAutAcceptPrefByPhoneId(i), i2);
        } else {
            persist(context, getFtAutAcceptPrefByPhoneId(0), i2);
        }
    }

    public int getFtAutAccept(Context context) {
        return getInt(context, getFtAutAcceptPrefByPhoneId(0), -1);
    }

    public int getFtAutAccept(Context context, int i) {
        if (RcsUtils.DualRcs.isDualRcsSettings()) {
            return getInt(context, getFtAutAcceptPrefByPhoneId(i), -1);
        }
        return getInt(context, getFtAutAcceptPrefByPhoneId(0), -1);
    }

    public void setUserAlias(Context context, int i, String str) {
        persist(context, getUserAliasPrefByPhoneId(i), str);
    }

    public String getUserAlias(Context context, int i) {
        String string = getString(context, USER_ALIAS, "");
        String string2 = getString(context, USER_ALIAS_SIM1, "");
        String string3 = getString(context, USER_ALIAS_SIM2, "");
        if (!string.isEmpty()) {
            if (string2.isEmpty() && string3.isEmpty()) {
                persist(context, USER_ALIAS_SIM1, string);
                persist(context, USER_ALIAS_SIM2, string);
            }
            persist(context, USER_ALIAS, "");
        }
        return getString(context, getUserAliasPrefByPhoneId(i), "");
    }

    private void persist(Context context, String str, int i) {
        SharedPreferences.Editor edit = context.getSharedPreferences(SHARED_PREFS_NAME, 0).edit();
        edit.putInt(str, i);
        edit.apply();
    }

    private void persist(Context context, String str, String str2) {
        SharedPreferences.Editor edit = context.getSharedPreferences(SHARED_PREFS_NAME, 0).edit();
        edit.putString(str, str2);
        edit.apply();
    }

    private int getInt(Context context, String str, int i) {
        return context.getSharedPreferences(SHARED_PREFS_NAME, 0).getInt(str, i);
    }

    private String getString(Context context, String str, String str2) {
        return context.getSharedPreferences(SHARED_PREFS_NAME, 0).getString(str, str2);
    }
}
