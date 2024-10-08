package com.sec.internal.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class RetryTimerUtil {
    private static final List<String> HTTP_DATE_FORMATS;
    static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";
    static final String PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";
    static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    static {
        ArrayList arrayList = new ArrayList();
        HTTP_DATE_FORMATS = arrayList;
        arrayList.add(PATTERN_RFC1123);
        arrayList.add(PATTERN_RFC1036);
        arrayList.add(PATTERN_ASCTIME);
    }

    public static int getRetryAfter(String str) {
        if (str == null) {
            return -1;
        }
        try {
            if (!str.isEmpty()) {
                return Integer.parseInt(str);
            }
            return -1;
        } catch (NumberFormatException unused) {
            for (String next : HTTP_DATE_FORMATS) {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(next);
                    if (next.equals(PATTERN_ASCTIME)) {
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    }
                    return (int) ((simpleDateFormat.parse(str).getTime() - new Date().getTime()) / 1000);
                } catch (ParseException unused2) {
                }
            }
            return -1;
        }
    }
}
