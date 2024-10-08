package org.xbill.DNS;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

final class FormattedTime {
    private static NumberFormat w2;
    private static NumberFormat w4;

    static {
        DecimalFormat decimalFormat = new DecimalFormat();
        w2 = decimalFormat;
        decimalFormat.setMinimumIntegerDigits(2);
        DecimalFormat decimalFormat2 = new DecimalFormat();
        w4 = decimalFormat2;
        decimalFormat2.setMinimumIntegerDigits(4);
        w4.setGroupingUsed(false);
    }

    public static String format(Date date) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        StringBuffer stringBuffer = new StringBuffer();
        gregorianCalendar.setTime(date);
        stringBuffer.append(w4.format((long) gregorianCalendar.get(1)));
        stringBuffer.append(w2.format((long) (gregorianCalendar.get(2) + 1)));
        stringBuffer.append(w2.format((long) gregorianCalendar.get(5)));
        stringBuffer.append(w2.format((long) gregorianCalendar.get(11)));
        stringBuffer.append(w2.format((long) gregorianCalendar.get(12)));
        stringBuffer.append(w2.format((long) gregorianCalendar.get(13)));
        return stringBuffer.toString();
    }
}
