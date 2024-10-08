package org.xbill.DNS;

public final class TTL {
    static void check(long j) {
        if (j < 0 || j > 2147483647L) {
            throw new InvalidTTLException(j);
        }
    }

    public static String format(long j) {
        check(j);
        StringBuffer stringBuffer = new StringBuffer();
        long j2 = j % 60;
        long j3 = j / 60;
        long j4 = j3 % 60;
        long j5 = j3 / 60;
        long j6 = j5 % 24;
        long j7 = j5 / 24;
        long j8 = j7 % 7;
        long j9 = j7 / 7;
        int i = (j9 > 0 ? 1 : (j9 == 0 ? 0 : -1));
        if (i > 0) {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append(j9);
            stringBuffer2.append("W");
            stringBuffer.append(stringBuffer2.toString());
        }
        int i2 = (j8 > 0 ? 1 : (j8 == 0 ? 0 : -1));
        if (i2 > 0) {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append(j8);
            stringBuffer3.append("D");
            stringBuffer.append(stringBuffer3.toString());
        }
        int i3 = (j6 > 0 ? 1 : (j6 == 0 ? 0 : -1));
        if (i3 > 0) {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append(j6);
            stringBuffer4.append("H");
            stringBuffer.append(stringBuffer4.toString());
        }
        int i4 = (j4 > 0 ? 1 : (j4 == 0 ? 0 : -1));
        if (i4 > 0) {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append(j4);
            stringBuffer5.append("M");
            stringBuffer.append(stringBuffer5.toString());
        }
        if (j2 > 0 || (i == 0 && i2 == 0 && i3 == 0 && i4 == 0)) {
            StringBuffer stringBuffer6 = new StringBuffer();
            stringBuffer6.append(j2);
            stringBuffer6.append("S");
            stringBuffer.append(stringBuffer6.toString());
        }
        return stringBuffer.toString();
    }
}
