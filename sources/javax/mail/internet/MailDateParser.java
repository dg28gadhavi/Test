package javax.mail.internet;

import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import java.text.ParseException;

/* compiled from: MailDateFormat */
class MailDateParser {
    int index = 0;
    char[] orig;

    public MailDateParser(char[] cArr) {
        this.orig = cArr;
    }

    public void skipUntilNumber() throws ParseException {
        while (true) {
            try {
                char[] cArr = this.orig;
                int i = this.index;
                switch (cArr[i]) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        return;
                    default:
                        this.index = i + 1;
                }
            } catch (ArrayIndexOutOfBoundsException unused) {
                throw new ParseException("No Number Found", this.index);
            }
        }
    }

    public void skipWhiteSpace() {
        int length = this.orig.length;
        while (true) {
            int i = this.index;
            if (i < length) {
                char c = this.orig[i];
                if (c == 9 || c == 10 || c == 13 || c == ' ') {
                    this.index = i + 1;
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    public void skipChar(char c) throws ParseException {
        int i = this.index;
        char[] cArr = this.orig;
        if (i >= cArr.length) {
            throw new ParseException("No more characters", this.index);
        } else if (cArr[i] == c) {
            this.index = i + 1;
        } else {
            throw new ParseException("Wrong char", this.index);
        }
    }

    public boolean skipIfChar(char c) throws ParseException {
        int i = this.index;
        char[] cArr = this.orig;
        if (i >= cArr.length) {
            throw new ParseException("No more characters", this.index);
        } else if (cArr[i] != c) {
            return false;
        } else {
            this.index = i + 1;
            return true;
        }
    }

    public int parseNumber() throws ParseException {
        int length = this.orig.length;
        boolean z = false;
        int i = 0;
        while (true) {
            int i2 = this.index;
            if (i2 < length) {
                switch (this.orig[i2]) {
                    case '0':
                        i *= 10;
                        break;
                    case '1':
                        i = (i * 10) + 1;
                        break;
                    case '2':
                        i = (i * 10) + 2;
                        break;
                    case '3':
                        i = (i * 10) + 3;
                        break;
                    case '4':
                        i = (i * 10) + 4;
                        break;
                    case '5':
                        i = (i * 10) + 5;
                        break;
                    case '6':
                        i = (i * 10) + 6;
                        break;
                    case '7':
                        i = (i * 10) + 7;
                        break;
                    case '8':
                        i = (i * 10) + 8;
                        break;
                    case '9':
                        i = (i * 10) + 9;
                        break;
                    default:
                        if (z) {
                            return i;
                        }
                        throw new ParseException("No Number found", this.index);
                }
                this.index = i2 + 1;
                z = true;
            } else if (z) {
                return i;
            } else {
                throw new ParseException("No Number found", this.index);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:103:?, code lost:
        return 9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:?, code lost:
        return 9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:?, code lost:
        return 10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:?, code lost:
        return 10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:?, code lost:
        return 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:?, code lost:
        return 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:?, code lost:
        return 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:?, code lost:
        return 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004e, code lost:
        r2 = r3 + 1;
        r0.index = r2;
        r3 = r1[r3];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0054, code lost:
        if (r3 == 'C') goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0056, code lost:
        if (r3 != 'c') goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0058, code lost:
        r0.index = r2 + 1;
        r1 = r1[r2];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0060, code lost:
        if (r1 == 'T') goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0064, code lost:
        if (r1 != 't') goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0069, code lost:
        r2 = r3 + 1;
        r0.index = r2;
        r3 = r1[r3];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0071, code lost:
        if (r3 == 'O') goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0075, code lost:
        if (r3 != 'o') goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0077, code lost:
        r0.index = r2 + 1;
        r1 = r1[r2];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x007f, code lost:
        if (r1 == 'V') goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0083, code lost:
        if (r1 != 'v') goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0088, code lost:
        r2 = r3 + 1;
        r0.index = r2;
        r3 = r1[r3];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x008e, code lost:
        if (r3 == 'A') goto L_0x0092;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0090, code lost:
        if (r3 != 'a') goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0092, code lost:
        r0.index = r2 + 1;
        r1 = r1[r2];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0098, code lost:
        if (r1 == 'R') goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x009a, code lost:
        if (r1 != 'r') goto L_0x009d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x009f, code lost:
        if (r1 == 'Y') goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00a3, code lost:
        if (r1 != 'y') goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00d2, code lost:
        if (r3 == 'u') goto L_0x00d4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int parseMonth() throws java.text.ParseException {
        /*
            r16 = this;
            r0 = r16
            char[] r1 = r0.orig     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            int r2 = r0.index     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            int r3 = r2 + 1
            r0.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r2 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            r4 = 114(0x72, float:1.6E-43)
            r5 = 82
            r6 = 112(0x70, float:1.57E-43)
            r8 = 85
            r9 = 80
            r10 = 65
            if (r2 == r10) goto L_0x0128
            r11 = 68
            r12 = 99
            r13 = 67
            r14 = 101(0x65, float:1.42E-43)
            r15 = 69
            if (r2 == r11) goto L_0x0111
            r11 = 70
            if (r2 == r11) goto L_0x00f7
            r11 = 74
            r7 = 97
            if (r2 == r11) goto L_0x00c0
            r11 = 83
            if (r2 == r11) goto L_0x00a9
            if (r2 == r7) goto L_0x0128
            r11 = 100
            if (r2 == r11) goto L_0x0111
            r11 = 102(0x66, float:1.43E-43)
            if (r2 == r11) goto L_0x00f7
            r11 = 106(0x6a, float:1.49E-43)
            if (r2 == r11) goto L_0x00c0
            r8 = 115(0x73, float:1.61E-43)
            if (r2 == r8) goto L_0x00a9
            switch(r2) {
                case 77: goto L_0x0088;
                case 78: goto L_0x0069;
                case 79: goto L_0x004e;
                default: goto L_0x0049;
            }     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
        L_0x0049:
            switch(r2) {
                case 109: goto L_0x0088;
                case 110: goto L_0x0069;
                case 111: goto L_0x004e;
                default: goto L_0x004c;
            }     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
        L_0x004c:
            goto L_0x0155
        L_0x004e:
            int r2 = r3 + 1
            r0.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r3 = r1[r3]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            if (r3 == r13) goto L_0x0058
            if (r3 != r12) goto L_0x0155
        L_0x0058:
            int r3 = r2 + 1
            r0.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            r2 = 84
            if (r1 == r2) goto L_0x0066
            r2 = 116(0x74, float:1.63E-43)
            if (r1 != r2) goto L_0x0155
        L_0x0066:
            r0 = 9
            return r0
        L_0x0069:
            int r2 = r3 + 1
            r0.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r3 = r1[r3]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            r4 = 79
            if (r3 == r4) goto L_0x0077
            r4 = 111(0x6f, float:1.56E-43)
            if (r3 != r4) goto L_0x0155
        L_0x0077:
            int r3 = r2 + 1
            r0.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            r2 = 86
            if (r1 == r2) goto L_0x0085
            r2 = 118(0x76, float:1.65E-43)
            if (r1 != r2) goto L_0x0155
        L_0x0085:
            r0 = 10
            return r0
        L_0x0088:
            int r2 = r3 + 1
            r0.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r3 = r1[r3]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            if (r3 == r10) goto L_0x0092
            if (r3 != r7) goto L_0x0155
        L_0x0092:
            int r3 = r2 + 1
            r0.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            if (r1 == r5) goto L_0x00a7
            if (r1 != r4) goto L_0x009d
            goto L_0x00a7
        L_0x009d:
            r2 = 89
            if (r1 == r2) goto L_0x00a5
            r2 = 121(0x79, float:1.7E-43)
            if (r1 != r2) goto L_0x0155
        L_0x00a5:
            r0 = 4
            return r0
        L_0x00a7:
            r0 = 2
            return r0
        L_0x00a9:
            int r2 = r3 + 1
            r0.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r3 = r1[r3]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            if (r3 == r15) goto L_0x00b3
            if (r3 != r14) goto L_0x0155
        L_0x00b3:
            int r3 = r2 + 1
            r0.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            if (r1 == r9) goto L_0x00bd
            if (r1 != r6) goto L_0x0155
        L_0x00bd:
            r0 = 8
            return r0
        L_0x00c0:
            int r2 = r3 + 1
            r0.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r3 = r1[r3]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            r4 = 110(0x6e, float:1.54E-43)
            r5 = 78
            if (r3 == r10) goto L_0x00eb
            if (r3 == r8) goto L_0x00d4
            if (r3 == r7) goto L_0x00eb
            r6 = 117(0x75, float:1.64E-43)
            if (r3 != r6) goto L_0x0155
        L_0x00d4:
            int r3 = r2 + 1
            r0.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            if (r1 == r5) goto L_0x00e9
            if (r1 != r4) goto L_0x00df
            goto L_0x00e9
        L_0x00df:
            r2 = 76
            if (r1 == r2) goto L_0x00e7
            r2 = 108(0x6c, float:1.51E-43)
            if (r1 != r2) goto L_0x0155
        L_0x00e7:
            r0 = 6
            return r0
        L_0x00e9:
            r0 = 5
            return r0
        L_0x00eb:
            int r3 = r2 + 1
            r0.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            if (r1 == r5) goto L_0x00f5
            if (r1 != r4) goto L_0x0155
        L_0x00f5:
            r0 = 0
            return r0
        L_0x00f7:
            int r2 = r3 + 1
            r0.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r3 = r1[r3]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            if (r3 == r15) goto L_0x0101
            if (r3 != r14) goto L_0x0155
        L_0x0101:
            int r3 = r2 + 1
            r0.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            r2 = 66
            if (r1 == r2) goto L_0x010f
            r2 = 98
            if (r1 != r2) goto L_0x0155
        L_0x010f:
            r0 = 1
            return r0
        L_0x0111:
            int r2 = r3 + 1
            r0.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r3 = r1[r3]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            if (r3 == r15) goto L_0x011b
            if (r3 != r14) goto L_0x0155
        L_0x011b:
            int r3 = r2 + 1
            r0.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            if (r1 == r13) goto L_0x0125
            if (r1 != r12) goto L_0x0155
        L_0x0125:
            r0 = 11
            return r0
        L_0x0128:
            int r2 = r3 + 1
            r0.index = r2     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r3 = r1[r3]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            if (r3 == r9) goto L_0x0149
            if (r3 != r6) goto L_0x0133
            goto L_0x0149
        L_0x0133:
            if (r3 == r8) goto L_0x0139
            r4 = 117(0x75, float:1.64E-43)
            if (r3 != r4) goto L_0x0155
        L_0x0139:
            int r3 = r2 + 1
            r0.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            r2 = 71
            if (r1 == r2) goto L_0x0147
            r2 = 103(0x67, float:1.44E-43)
            if (r1 != r2) goto L_0x0155
        L_0x0147:
            r0 = 7
            return r0
        L_0x0149:
            int r3 = r2 + 1
            r0.index = r3     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            char r1 = r1[r2]     // Catch:{ ArrayIndexOutOfBoundsException -> 0x0155 }
            if (r1 == r5) goto L_0x0153
            if (r1 != r4) goto L_0x0155
        L_0x0153:
            r0 = 3
            return r0
        L_0x0155:
            java.text.ParseException r1 = new java.text.ParseException
            java.lang.String r2 = "Bad Month"
            int r0 = r0.index
            r1.<init>(r2, r0)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MailDateParser.parseMonth():int");
    }

    public int parseTimeZone() throws ParseException {
        int i = this.index;
        char[] cArr = this.orig;
        if (i < cArr.length) {
            char c = cArr[i];
            if (c == '+' || c == '-') {
                return parseNumericTimeZone();
            }
            return parseAlphaTimeZone();
        }
        throw new ParseException("No more characters", this.index);
    }

    public int parseNumericTimeZone() throws ParseException {
        boolean z;
        char[] cArr = this.orig;
        int i = this.index;
        this.index = i + 1;
        char c = cArr[i];
        if (c == '+') {
            z = true;
        } else if (c == '-') {
            z = false;
        } else {
            throw new ParseException("Bad Numeric TimeZone", this.index);
        }
        int parseNumber = parseNumber();
        int i2 = ((parseNumber / 100) * 60) + (parseNumber % 100);
        return z ? -i2 : i2;
    }

    public int parseAlphaTimeZone() throws ParseException {
        try {
            char[] cArr = this.orig;
            int i = this.index;
            int i2 = i + 1;
            this.index = i2;
            boolean z = true;
            int i3 = 0;
            switch (cArr[i]) {
                case 'C':
                case 'c':
                    i3 = 360;
                    break;
                case 'E':
                case 'e':
                    i3 = 300;
                    break;
                case 'G':
                case 'g':
                    int i4 = i2 + 1;
                    this.index = i4;
                    char c = cArr[i2];
                    if (c == 'M' || c == 'm') {
                        this.index = i4 + 1;
                        char c2 = cArr[i4];
                        if (c2 != 'T') {
                            if (c2 == 't') {
                                break;
                            }
                        }
                    }
                    throw new ParseException("Bad Alpha TimeZone", this.index);
                case 'M':
                case 'm':
                    i3 = 420;
                    break;
                case 'P':
                case 'p':
                    i3 = NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE;
                    break;
                case 'U':
                case 'u':
                    this.index = i2 + 1;
                    char c3 = cArr[i2];
                    if (c3 != 'T') {
                        if (c3 == 't') {
                            break;
                        } else {
                            throw new ParseException("Bad Alpha TimeZone", this.index);
                        }
                    }
                    break;
                default:
                    throw new ParseException("Bad Alpha TimeZone", this.index);
            }
            z = false;
            if (!z) {
                return i3;
            }
            int i5 = this.index;
            int i6 = i5 + 1;
            this.index = i6;
            char c4 = cArr[i5];
            if (c4 == 'S' || c4 == 's') {
                this.index = i6 + 1;
                char c5 = cArr[i6];
                if (c5 == 'T' || c5 == 't') {
                    return i3;
                }
                throw new ParseException("Bad Alpha TimeZone", this.index);
            } else if (c4 != 'D' && c4 != 'd') {
                return i3;
            } else {
                this.index = i6 + 1;
                char c6 = cArr[i6];
                if (c6 == 'T' || c6 != 't') {
                    return i3 - 60;
                }
                throw new ParseException("Bad Alpha TimeZone", this.index);
            }
        } catch (ArrayIndexOutOfBoundsException unused) {
            throw new ParseException("Bad Alpha TimeZone", this.index);
        }
    }

    /* access modifiers changed from: package-private */
    public int getIndex() {
        return this.index;
    }
}
