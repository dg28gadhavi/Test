package com.sec.internal.ims.util;

import android.telephony.PhoneNumberUtils;

public class PhoneUtils {
    private static String COUNTRY_AREA_CODE = "0";
    private static String COUNTRY_CODE = "+1";

    public static synchronized void initialize() {
        synchronized (PhoneUtils.class) {
            if (RcsSettingsUtils.getInstance() != null) {
                COUNTRY_CODE = RcsSettingsUtils.getInstance().getCountryCode();
                COUNTRY_AREA_CODE = RcsSettingsUtils.getInstance().getCountryAreaCode();
            }
        }
    }

    public static String formatNumberToInternational(String str) {
        if (str == null) {
            return null;
        }
        String stripSeparators = PhoneNumberUtils.stripSeparators(str.trim());
        if (stripSeparators.startsWith("00" + COUNTRY_CODE.substring(1))) {
            return COUNTRY_CODE + stripSeparators.substring(4);
        }
        String str2 = COUNTRY_AREA_CODE;
        if (str2 != null && str2.length() > 0 && stripSeparators.startsWith(COUNTRY_AREA_CODE)) {
            return COUNTRY_CODE + stripSeparators.substring(COUNTRY_AREA_CODE.length());
        } else if (stripSeparators.startsWith("+")) {
            return stripSeparators;
        } else {
            return COUNTRY_CODE + stripSeparators;
        }
    }

    public static String extractNumberFromUri(String str) {
        if (str == null || "".equals(str)) {
            return null;
        }
        try {
            int indexOf = str.indexOf("<");
            if (indexOf != -1) {
                str = str.substring(indexOf + 1, str.indexOf(">", indexOf));
            }
            int indexOf2 = str.indexOf("tel:");
            if (indexOf2 != -1) {
                str = str.substring(indexOf2 + 4);
            }
            int indexOf3 = str.indexOf("sip:");
            if (indexOf3 != -1) {
                str = str.substring(indexOf3 + 4, str.indexOf("@", indexOf3));
            }
            int indexOf4 = str.indexOf(";");
            if (indexOf4 != -1) {
                str = str.substring(0, indexOf4);
            }
            return formatNumberToInternational(str);
        } catch (IndexOutOfBoundsException unused) {
            return "";
        }
    }
}
