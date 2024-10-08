package okio.internal;

import kotlin.jvm.internal.Intrinsics;
import okio.Buffer;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;

/* compiled from: -ByteString.kt */
public final class _ByteStringKt {
    @NotNull
    private static final char[] HEX_DIGIT_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    @NotNull
    public static final char[] getHEX_DIGIT_CHARS() {
        return HEX_DIGIT_CHARS;
    }

    public static final void commonWrite(@NotNull ByteString byteString, @NotNull Buffer buffer, int i, int i2) {
        Intrinsics.checkNotNullParameter(byteString, "<this>");
        Intrinsics.checkNotNullParameter(buffer, "buffer");
        buffer.write(byteString.getData$okio(), i, i2);
    }

    /* access modifiers changed from: private */
    public static final int decodeHexDigit(char c) {
        boolean z = true;
        if ('0' <= c && c <= '9') {
            return c - '0';
        }
        char c2 = 'a';
        if (!('a' <= c && c <= 'f')) {
            c2 = 'A';
            if ('A' > c || c > 'F') {
                z = false;
            }
            if (!z) {
                throw new IllegalArgumentException(Intrinsics.stringPlus("Unexpected hex digit: ", Character.valueOf(c)));
            }
        }
        return (c - c2) + 10;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:251:0x0217 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0047 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:261:0x016c A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:263:0x0081 A[EDGE_INSN: B:263:0x0081->B:51:0x0081 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:275:0x00da A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static final int codePointIndexToCharIndex(byte[] r19, int r20) {
        /*
            r0 = r19
            r1 = r20
            int r2 = r0.length
            r4 = 0
            r5 = 0
            r6 = 0
        L_0x0008:
            if (r4 >= r2) goto L_0x022b
            byte r7 = r0[r4]
            r8 = 159(0x9f, float:2.23E-43)
            r9 = 127(0x7f, float:1.78E-43)
            r10 = 31
            r11 = 13
            r12 = 65533(0xfffd, float:9.1831E-41)
            r13 = 10
            r14 = 65536(0x10000, float:9.18355E-41)
            r16 = -1
            r17 = 1
            if (r7 < 0) goto L_0x008a
            int r18 = r6 + 1
            if (r6 != r1) goto L_0x0026
            return r5
        L_0x0026:
            if (r7 == r13) goto L_0x0045
            if (r7 == r11) goto L_0x0045
            if (r7 < 0) goto L_0x0031
            if (r7 > r10) goto L_0x0031
            r6 = r17
            goto L_0x0032
        L_0x0031:
            r6 = 0
        L_0x0032:
            if (r6 != 0) goto L_0x0041
            if (r9 > r7) goto L_0x003b
            if (r7 > r8) goto L_0x003b
            r6 = r17
            goto L_0x003c
        L_0x003b:
            r6 = 0
        L_0x003c:
            if (r6 == 0) goto L_0x003f
            goto L_0x0041
        L_0x003f:
            r6 = 0
            goto L_0x0043
        L_0x0041:
            r6 = r17
        L_0x0043:
            if (r6 != 0) goto L_0x0047
        L_0x0045:
            if (r7 != r12) goto L_0x0048
        L_0x0047:
            return r16
        L_0x0048:
            if (r7 >= r14) goto L_0x004d
            r6 = r17
            goto L_0x004e
        L_0x004d:
            r6 = 2
        L_0x004e:
            int r5 = r5 + r6
            int r4 = r4 + 1
        L_0x0051:
            r6 = r18
            if (r4 >= r2) goto L_0x0008
            byte r7 = r0[r4]
            if (r7 < 0) goto L_0x0008
            int r4 = r4 + 1
            int r18 = r6 + 1
            if (r6 != r1) goto L_0x0060
            return r5
        L_0x0060:
            if (r7 == r13) goto L_0x007f
            if (r7 == r11) goto L_0x007f
            if (r7 < 0) goto L_0x006b
            if (r7 > r10) goto L_0x006b
            r6 = r17
            goto L_0x006c
        L_0x006b:
            r6 = 0
        L_0x006c:
            if (r6 != 0) goto L_0x007b
            if (r9 > r7) goto L_0x0075
            if (r7 > r8) goto L_0x0075
            r6 = r17
            goto L_0x0076
        L_0x0075:
            r6 = 0
        L_0x0076:
            if (r6 == 0) goto L_0x0079
            goto L_0x007b
        L_0x0079:
            r6 = 0
            goto L_0x007d
        L_0x007b:
            r6 = r17
        L_0x007d:
            if (r6 != 0) goto L_0x0081
        L_0x007f:
            if (r7 != r12) goto L_0x0082
        L_0x0081:
            return r16
        L_0x0082:
            if (r7 >= r14) goto L_0x0087
            r6 = r17
            goto L_0x0088
        L_0x0087:
            r6 = 2
        L_0x0088:
            int r5 = r5 + r6
            goto L_0x0051
        L_0x008a:
            int r3 = r7 >> 5
            r15 = -2
            r14 = 128(0x80, float:1.794E-43)
            if (r3 != r15) goto L_0x00eb
            int r3 = r4 + 1
            if (r2 > r3) goto L_0x0099
            if (r6 != r1) goto L_0x0098
            return r5
        L_0x0098:
            return r16
        L_0x0099:
            byte r3 = r0[r3]
            r15 = r3 & 192(0xc0, float:2.69E-43)
            if (r15 != r14) goto L_0x00a2
            r15 = r17
            goto L_0x00a3
        L_0x00a2:
            r15 = 0
        L_0x00a3:
            if (r15 != 0) goto L_0x00a9
            if (r6 != r1) goto L_0x00a8
            return r5
        L_0x00a8:
            return r16
        L_0x00a9:
            r3 = r3 ^ 3968(0xf80, float:5.56E-42)
            int r7 = r7 << 6
            r3 = r3 ^ r7
            if (r3 >= r14) goto L_0x00b4
            if (r6 != r1) goto L_0x00b3
            return r5
        L_0x00b3:
            return r16
        L_0x00b4:
            int r7 = r6 + 1
            if (r6 != r1) goto L_0x00b9
            return r5
        L_0x00b9:
            if (r3 == r13) goto L_0x00d8
            if (r3 == r11) goto L_0x00d8
            if (r3 < 0) goto L_0x00c4
            if (r3 > r10) goto L_0x00c4
            r6 = r17
            goto L_0x00c5
        L_0x00c4:
            r6 = 0
        L_0x00c5:
            if (r6 != 0) goto L_0x00d4
            if (r9 > r3) goto L_0x00ce
            if (r3 > r8) goto L_0x00ce
            r6 = r17
            goto L_0x00cf
        L_0x00ce:
            r6 = 0
        L_0x00cf:
            if (r6 == 0) goto L_0x00d2
            goto L_0x00d4
        L_0x00d2:
            r6 = 0
            goto L_0x00d6
        L_0x00d4:
            r6 = r17
        L_0x00d6:
            if (r6 != 0) goto L_0x00da
        L_0x00d8:
            if (r3 != r12) goto L_0x00db
        L_0x00da:
            return r16
        L_0x00db:
            r6 = 65536(0x10000, float:9.18355E-41)
            if (r3 >= r6) goto L_0x00e2
            r15 = r17
            goto L_0x00e3
        L_0x00e2:
            r15 = 2
        L_0x00e3:
            int r5 = r5 + r15
            kotlin.Unit r3 = kotlin.Unit.INSTANCE
            int r4 = r4 + 2
        L_0x00e8:
            r6 = r7
            goto L_0x0008
        L_0x00eb:
            int r3 = r7 >> 4
            r12 = 57343(0xdfff, float:8.0355E-41)
            r8 = 55296(0xd800, float:7.7486E-41)
            if (r3 != r15) goto L_0x017c
            int r3 = r4 + 2
            if (r2 > r3) goto L_0x00fd
            if (r6 != r1) goto L_0x00fc
            return r5
        L_0x00fc:
            return r16
        L_0x00fd:
            int r15 = r4 + 1
            byte r15 = r0[r15]
            r9 = r15 & 192(0xc0, float:2.69E-43)
            if (r9 != r14) goto L_0x0108
            r9 = r17
            goto L_0x0109
        L_0x0108:
            r9 = 0
        L_0x0109:
            if (r9 != 0) goto L_0x010f
            if (r6 != r1) goto L_0x010e
            return r5
        L_0x010e:
            return r16
        L_0x010f:
            byte r3 = r0[r3]
            r9 = r3 & 192(0xc0, float:2.69E-43)
            if (r9 != r14) goto L_0x0118
            r9 = r17
            goto L_0x0119
        L_0x0118:
            r9 = 0
        L_0x0119:
            if (r9 != 0) goto L_0x011f
            if (r6 != r1) goto L_0x011e
            return r5
        L_0x011e:
            return r16
        L_0x011f:
            r9 = -123008(0xfffffffffffe1f80, float:NaN)
            r3 = r3 ^ r9
            int r9 = r15 << 6
            r3 = r3 ^ r9
            int r7 = r7 << 12
            r3 = r3 ^ r7
            r7 = 2048(0x800, float:2.87E-42)
            if (r3 >= r7) goto L_0x0131
            if (r6 != r1) goto L_0x0130
            return r5
        L_0x0130:
            return r16
        L_0x0131:
            if (r8 > r3) goto L_0x0138
            if (r3 > r12) goto L_0x0138
            r7 = r17
            goto L_0x0139
        L_0x0138:
            r7 = 0
        L_0x0139:
            if (r7 == 0) goto L_0x013f
            if (r6 != r1) goto L_0x013e
            return r5
        L_0x013e:
            return r16
        L_0x013f:
            int r7 = r6 + 1
            if (r6 != r1) goto L_0x0144
            return r5
        L_0x0144:
            if (r3 == r13) goto L_0x0167
            if (r3 == r11) goto L_0x0167
            if (r3 < 0) goto L_0x014f
            if (r3 > r10) goto L_0x014f
            r6 = r17
            goto L_0x0150
        L_0x014f:
            r6 = 0
        L_0x0150:
            if (r6 != 0) goto L_0x0163
            r6 = 127(0x7f, float:1.78E-43)
            if (r6 > r3) goto L_0x015d
            r6 = 159(0x9f, float:2.23E-43)
            if (r3 > r6) goto L_0x015d
            r6 = r17
            goto L_0x015e
        L_0x015d:
            r6 = 0
        L_0x015e:
            if (r6 == 0) goto L_0x0161
            goto L_0x0163
        L_0x0161:
            r6 = 0
            goto L_0x0165
        L_0x0163:
            r6 = r17
        L_0x0165:
            if (r6 != 0) goto L_0x016c
        L_0x0167:
            r6 = 65533(0xfffd, float:9.1831E-41)
            if (r3 != r6) goto L_0x016d
        L_0x016c:
            return r16
        L_0x016d:
            r6 = 65536(0x10000, float:9.18355E-41)
            if (r3 >= r6) goto L_0x0174
            r15 = r17
            goto L_0x0175
        L_0x0174:
            r15 = 2
        L_0x0175:
            int r5 = r5 + r15
            kotlin.Unit r3 = kotlin.Unit.INSTANCE
            int r4 = r4 + 3
            goto L_0x00e8
        L_0x017c:
            int r3 = r7 >> 3
            if (r3 != r15) goto L_0x0227
            int r3 = r4 + 3
            if (r2 > r3) goto L_0x0188
            if (r6 != r1) goto L_0x0187
            return r5
        L_0x0187:
            return r16
        L_0x0188:
            int r9 = r4 + 1
            byte r9 = r0[r9]
            r15 = r9 & 192(0xc0, float:2.69E-43)
            if (r15 != r14) goto L_0x0193
            r15 = r17
            goto L_0x0194
        L_0x0193:
            r15 = 0
        L_0x0194:
            if (r15 != 0) goto L_0x019a
            if (r6 != r1) goto L_0x0199
            return r5
        L_0x0199:
            return r16
        L_0x019a:
            int r15 = r4 + 2
            byte r15 = r0[r15]
            r10 = r15 & 192(0xc0, float:2.69E-43)
            if (r10 != r14) goto L_0x01a5
            r10 = r17
            goto L_0x01a6
        L_0x01a5:
            r10 = 0
        L_0x01a6:
            if (r10 != 0) goto L_0x01ac
            if (r6 != r1) goto L_0x01ab
            return r5
        L_0x01ab:
            return r16
        L_0x01ac:
            byte r3 = r0[r3]
            r10 = r3 & 192(0xc0, float:2.69E-43)
            if (r10 != r14) goto L_0x01b5
            r10 = r17
            goto L_0x01b6
        L_0x01b5:
            r10 = 0
        L_0x01b6:
            if (r10 != 0) goto L_0x01bc
            if (r6 != r1) goto L_0x01bb
            return r5
        L_0x01bb:
            return r16
        L_0x01bc:
            r10 = 3678080(0x381f80, float:5.154088E-39)
            r3 = r3 ^ r10
            int r10 = r15 << 6
            r3 = r3 ^ r10
            int r9 = r9 << 12
            r3 = r3 ^ r9
            int r7 = r7 << 18
            r3 = r3 ^ r7
            r7 = 1114111(0x10ffff, float:1.561202E-39)
            if (r3 <= r7) goto L_0x01d2
            if (r6 != r1) goto L_0x01d1
            return r5
        L_0x01d1:
            return r16
        L_0x01d2:
            if (r8 > r3) goto L_0x01d9
            if (r3 > r12) goto L_0x01d9
            r7 = r17
            goto L_0x01da
        L_0x01d9:
            r7 = 0
        L_0x01da:
            if (r7 == 0) goto L_0x01e0
            if (r6 != r1) goto L_0x01df
            return r5
        L_0x01df:
            return r16
        L_0x01e0:
            r7 = 65536(0x10000, float:9.18355E-41)
            if (r3 >= r7) goto L_0x01e8
            if (r6 != r1) goto L_0x01e7
            return r5
        L_0x01e7:
            return r16
        L_0x01e8:
            int r7 = r6 + 1
            if (r6 != r1) goto L_0x01ed
            return r5
        L_0x01ed:
            if (r3 == r13) goto L_0x0212
            if (r3 == r11) goto L_0x0212
            if (r3 < 0) goto L_0x01fa
            r6 = 31
            if (r3 > r6) goto L_0x01fa
            r6 = r17
            goto L_0x01fb
        L_0x01fa:
            r6 = 0
        L_0x01fb:
            if (r6 != 0) goto L_0x020e
            r6 = 127(0x7f, float:1.78E-43)
            if (r6 > r3) goto L_0x0208
            r6 = 159(0x9f, float:2.23E-43)
            if (r3 > r6) goto L_0x0208
            r6 = r17
            goto L_0x0209
        L_0x0208:
            r6 = 0
        L_0x0209:
            if (r6 == 0) goto L_0x020c
            goto L_0x020e
        L_0x020c:
            r6 = 0
            goto L_0x0210
        L_0x020e:
            r6 = r17
        L_0x0210:
            if (r6 != 0) goto L_0x0217
        L_0x0212:
            r6 = 65533(0xfffd, float:9.1831E-41)
            if (r3 != r6) goto L_0x0218
        L_0x0217:
            return r16
        L_0x0218:
            r6 = 65536(0x10000, float:9.18355E-41)
            if (r3 >= r6) goto L_0x021f
            r15 = r17
            goto L_0x0220
        L_0x021f:
            r15 = 2
        L_0x0220:
            int r5 = r5 + r15
            kotlin.Unit r3 = kotlin.Unit.INSTANCE
            int r4 = r4 + 4
            goto L_0x00e8
        L_0x0227:
            if (r6 != r1) goto L_0x022a
            return r5
        L_0x022a:
            return r16
        L_0x022b:
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: okio.internal._ByteStringKt.codePointIndexToCharIndex(byte[], int):int");
    }
}
