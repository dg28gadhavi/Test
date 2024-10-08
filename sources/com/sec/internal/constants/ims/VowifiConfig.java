package com.sec.internal.constants.ims;

import android.content.Context;
import android.provider.Settings;
import com.sec.internal.constants.ims.ImsConstants;

public class VowifiConfig {
    public static final String AUTO_DATA_SWITCH = "data_preferred_mode_during_calling";
    public static final int UNKNOWN = -1;
    public static final String WIFI_CALL_ENABLE = "wifi_call_enable";
    public static final String WIFI_CALL_PREFERRED = "wifi_call_preferred";
    public static final String WIFI_CALL_WHEN_ROAMING = "wifi_call_when_roaming";

    public static final class HOME_PREF {
        public static final int CELLULAR = 2;
        public static final int NEVER_USE_CS = 3;
        public static final int WIFI = 1;
    }

    public static final class ROAM_PREF {
        public static final int CELLULAR = 0;
        public static final int WIFI = 1;
    }

    public static final class STATUS {
        public static final int OFF = 0;
        public static final int ON = 1;
    }

    public static boolean isEnabled(Context context, int i) {
        return ImsConstants.SystemSettings.getWiFiCallEnabled(context, 0, i) == 1;
    }

    public static boolean isCrossSimSettingEnabled(Context context, int i) {
        return isEnabled(context, i) && Settings.Secure.getInt(context.getContentResolver(), AUTO_DATA_SWITCH, 0) == 1;
    }

    public static int getPrefMode(Context context, int i, int i2) {
        return ImsConstants.SystemSettings.getWiFiCallPreferred(context, i, i2);
    }

    @Deprecated
    public static int getPrefMode(Context context, int i) {
        return getPrefMode(context, i, ImsConstants.Phone.SLOT_1);
    }

    public static int getRoamPrefMode(Context context, int i, int i2) {
        return ImsConstants.SystemSettings.getWiFiCallWhenRoaming(context, i, i2);
    }

    public static void setEnabled(Context context, int i, int i2) {
        ImsConstants.SystemSettings.setWiFiCallEnabled(context, i2, i);
    }

    public static void setPrefMode(Context context, int i, int i2) {
        ImsConstants.SystemSettings.setWiFiCallPreferred(context, i2, i);
    }

    public static void setRoamPrefMode(Context context, int i, int i2) {
        ImsConstants.SystemSettings.setWiFiCallWhenRoaming(context, i2, i);
    }
}
