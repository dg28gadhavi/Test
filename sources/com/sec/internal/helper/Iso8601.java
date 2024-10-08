package com.sec.internal.helper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Iso8601 {
    private static final List<DateFormat> ALL_FORMATS;
    private static final DateFormat DATE_FORMAT;
    private static final DateFormat MILLISECONDS_FORMAT;
    private static final DateFormat MILLISECONDS_FORMAT_GMT;
    private static final DateFormat MINUTES_FORMAT;
    private static final DateFormat MINUTES_FORMAT_GMT;
    private static final DateFormat MONTH_FORMAT;
    private static final DateFormat SECONDS_FORMAT;
    private static final DateFormat SECONDS_FORMAT_GMT;
    private static final DateFormat YEAR_FORMAT;

    static {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        YEAR_FORMAT = simpleDateFormat;
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        MONTH_FORMAT = simpleDateFormat2;
        SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DATE_FORMAT = simpleDateFormat3;
        SimpleDateFormat simpleDateFormat4 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.getDefault());
        MINUTES_FORMAT_GMT = simpleDateFormat4;
        SimpleDateFormat simpleDateFormat5 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.getDefault());
        MINUTES_FORMAT = simpleDateFormat5;
        SimpleDateFormat simpleDateFormat6 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        SECONDS_FORMAT_GMT = simpleDateFormat6;
        SimpleDateFormat simpleDateFormat7 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
        SECONDS_FORMAT = simpleDateFormat7;
        SimpleDateFormat simpleDateFormat8 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        MILLISECONDS_FORMAT_GMT = simpleDateFormat8;
        SimpleDateFormat simpleDateFormat9 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        MILLISECONDS_FORMAT = simpleDateFormat9;
        ArrayList<DateFormat> arrayList = new ArrayList<>();
        ALL_FORMATS = arrayList;
        arrayList.add(simpleDateFormat7);
        arrayList.add(simpleDateFormat6);
        arrayList.add(simpleDateFormat8);
        arrayList.add(simpleDateFormat9);
        arrayList.add(simpleDateFormat4);
        arrayList.add(simpleDateFormat5);
        arrayList.add(simpleDateFormat3);
        arrayList.add(simpleDateFormat2);
        arrayList.add(simpleDateFormat);
        for (DateFormat timeZone : arrayList) {
            timeZone.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
    }

    public static synchronized String format(Date date) throws NullPointerException, IllegalArgumentException {
        String format;
        synchronized (Iso8601.class) {
            format = SECONDS_FORMAT_GMT.format(date);
        }
        return format;
    }

    public static synchronized String formatMillis(Date date) throws NullPointerException, IllegalArgumentException {
        String format;
        synchronized (Iso8601.class) {
            format = MILLISECONDS_FORMAT_GMT.format(date);
        }
        return format;
    }

    public static synchronized Date parse(String str) throws NullPointerException, ParseException {
        Date parse;
        synchronized (Iso8601.class) {
            int max = Math.max(str.lastIndexOf(45), str.lastIndexOf(43));
            int lastIndexOf = str.lastIndexOf(84);
            int lastIndexOf2 = str.lastIndexOf(58);
            if (max > -1 && lastIndexOf2 > max && max > lastIndexOf) {
                str = str.substring(0, lastIndexOf2) + str.substring(lastIndexOf2 + 1, str.length());
            }
            for (DateFormat parse2 : ALL_FORMATS) {
                try {
                    parse = parse2.parse(str);
                } catch (ParseException unused) {
                }
            }
            throw new ParseException(String.format("unsupported format for date %s", new Object[]{str}), 0);
        }
        return parse;
    }
}
