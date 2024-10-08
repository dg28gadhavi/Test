package javax.mail.internet;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class MailDateFormat extends SimpleDateFormat {
    private static Calendar cal = new GregorianCalendar(tz);
    static boolean debug = false;
    private static final long serialVersionUID = -8148227605210628779L;
    private static TimeZone tz = TimeZone.getTimeZone("GMT");

    public MailDateFormat() {
        super("EEE, d MMM yyyy HH:mm:ss 'XXXXX' (z)", Locale.US);
    }

    public StringBuffer format(Date date, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        int i;
        int length = stringBuffer.length();
        super.format(date, stringBuffer, fieldPosition);
        int i2 = length + 25;
        while (stringBuffer.charAt(i2) != 'X') {
            i2++;
        }
        this.calendar.clear();
        this.calendar.setTime(date);
        int i3 = this.calendar.get(15) + this.calendar.get(16);
        if (i3 < 0) {
            i = i2 + 1;
            stringBuffer.setCharAt(i2, '-');
            i3 = -i3;
        } else {
            i = i2 + 1;
            stringBuffer.setCharAt(i2, '+');
        }
        int i4 = (i3 / 60) / 1000;
        int i5 = i4 / 60;
        int i6 = i4 % 60;
        int i7 = i + 1;
        stringBuffer.setCharAt(i, Character.forDigit(i5 / 10, 10));
        int i8 = i7 + 1;
        stringBuffer.setCharAt(i7, Character.forDigit(i5 % 10, 10));
        stringBuffer.setCharAt(i8, Character.forDigit(i6 / 10, 10));
        stringBuffer.setCharAt(i8 + 1, Character.forDigit(i6 % 10, 10));
        return stringBuffer;
    }

    public Date parse(String str, ParsePosition parsePosition) {
        return parseDate(str.toCharArray(), parsePosition, isLenient());
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(3:22|23|(1:25)) */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0060, code lost:
        if (debug != false) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0062, code lost:
        r6 = java.lang.System.out;
        r6.println("No timezone? : '" + new java.lang.String(r12) + "'");
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:22:0x005e */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.util.Date parseDate(char[] r12, java.text.ParsePosition r13, boolean r14) {
        /*
            java.lang.String r0 = "'"
            javax.mail.internet.MailDateParser r1 = new javax.mail.internet.MailDateParser     // Catch:{ Exception -> 0x008c }
            r1.<init>(r12)     // Catch:{ Exception -> 0x008c }
            r1.skipUntilNumber()     // Catch:{ Exception -> 0x008c }
            int r4 = r1.parseNumber()     // Catch:{ Exception -> 0x008c }
            r2 = 45
            boolean r3 = r1.skipIfChar(r2)     // Catch:{ Exception -> 0x008c }
            if (r3 != 0) goto L_0x0019
            r1.skipWhiteSpace()     // Catch:{ Exception -> 0x008c }
        L_0x0019:
            int r3 = r1.parseMonth()     // Catch:{ Exception -> 0x008c }
            boolean r2 = r1.skipIfChar(r2)     // Catch:{ Exception -> 0x008c }
            if (r2 != 0) goto L_0x0026
            r1.skipWhiteSpace()     // Catch:{ Exception -> 0x008c }
        L_0x0026:
            int r2 = r1.parseNumber()     // Catch:{ Exception -> 0x008c }
            r5 = 50
            if (r2 >= r5) goto L_0x0031
            int r2 = r2 + 2000
            goto L_0x0037
        L_0x0031:
            r5 = 100
            if (r2 >= r5) goto L_0x0037
            int r2 = r2 + 1900
        L_0x0037:
            r1.skipWhiteSpace()     // Catch:{ Exception -> 0x008c }
            int r5 = r1.parseNumber()     // Catch:{ Exception -> 0x008c }
            r6 = 58
            r1.skipChar(r6)     // Catch:{ Exception -> 0x008c }
            int r7 = r1.parseNumber()     // Catch:{ Exception -> 0x008c }
            boolean r6 = r1.skipIfChar(r6)     // Catch:{ Exception -> 0x008c }
            r8 = 0
            if (r6 == 0) goto L_0x0054
            int r6 = r1.parseNumber()     // Catch:{ Exception -> 0x008c }
            r9 = r6
            goto L_0x0055
        L_0x0054:
            r9 = r8
        L_0x0055:
            r1.skipWhiteSpace()     // Catch:{ ParseException -> 0x005e }
            int r6 = r1.parseTimeZone()     // Catch:{ ParseException -> 0x005e }
            r8 = r6
            goto L_0x007d
        L_0x005e:
            boolean r6 = debug     // Catch:{ Exception -> 0x008c }
            if (r6 == 0) goto L_0x007d
            java.io.PrintStream r6 = java.lang.System.out     // Catch:{ Exception -> 0x008c }
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x008c }
            java.lang.String r11 = "No timezone? : '"
            r10.<init>(r11)     // Catch:{ Exception -> 0x008c }
            java.lang.String r11 = new java.lang.String     // Catch:{ Exception -> 0x008c }
            r11.<init>(r12)     // Catch:{ Exception -> 0x008c }
            r10.append(r11)     // Catch:{ Exception -> 0x008c }
            r10.append(r0)     // Catch:{ Exception -> 0x008c }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x008c }
            r6.println(r10)     // Catch:{ Exception -> 0x008c }
        L_0x007d:
            int r1 = r1.getIndex()     // Catch:{ Exception -> 0x008c }
            r13.setIndex(r1)     // Catch:{ Exception -> 0x008c }
            r6 = r7
            r7 = r9
            r9 = r14
            java.util.Date r12 = ourUTC(r2, r3, r4, r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x008c }
            return r12
        L_0x008c:
            r14 = move-exception
            boolean r1 = debug
            if (r1 == 0) goto L_0x00af
            java.io.PrintStream r1 = java.lang.System.out
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            java.lang.String r3 = "Bad date: '"
            r2.<init>(r3)
            java.lang.String r3 = new java.lang.String
            r3.<init>(r12)
            r2.append(r3)
            r2.append(r0)
            java.lang.String r12 = r2.toString()
            r1.println(r12)
            r14.printStackTrace()
        L_0x00af:
            r12 = 1
            r13.setIndex(r12)
            r12 = 0
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MailDateFormat.parseDate(char[], java.text.ParsePosition, boolean):java.util.Date");
    }

    private static synchronized Date ourUTC(int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean z) {
        Date time;
        synchronized (MailDateFormat.class) {
            cal.clear();
            cal.setLenient(z);
            cal.set(1, i);
            cal.set(2, i2);
            cal.set(5, i3);
            cal.set(11, i4);
            cal.set(12, i5 + i7);
            cal.set(13, i6);
            time = cal.getTime();
        }
        return time;
    }

    public void setCalendar(Calendar calendar) {
        throw new RuntimeException("Method setCalendar() shouldn't be called");
    }

    public void setNumberFormat(NumberFormat numberFormat) {
        throw new RuntimeException("Method setNumberFormat() shouldn't be called");
    }
}
