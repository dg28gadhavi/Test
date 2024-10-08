package com.sec.internal.ims.entitlement.util;

import com.sec.internal.log.IMSLog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
    private static final String LOG_TAG = "DateUtil";
    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    public static Date parseIso8601Date(String str) {
        String str2 = LOG_TAG;
        IMSLog.i(str2, "parseIso8601Date: ISO8601 " + str);
        if (str == null) {
            IMSLog.e(str2, "parseIso8601Date: input is null");
            return null;
        }
        try {
            return fetchDateFormat().parse(str);
        } catch (ParseException e) {
            String str3 = LOG_TAG;
            IMSLog.s(str3, "parseIso8601Date: " + e.getMessage());
            return null;
        }
    }

    private static SimpleDateFormat fetchDateFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        simpleDateFormat.setTimeZone(UTC_TIME_ZONE);
        return simpleDateFormat;
    }
}
