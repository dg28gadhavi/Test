package okio;

import kotlin.jvm.internal.Intrinsics;
import okio.Buffer;
import okio.internal._ByteStringKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: -Util.kt */
public final class _UtilKt {
    private static final int DEFAULT__ByteString_size = -1234567890;
    @NotNull
    private static final Buffer.UnsafeCursor DEFAULT__new_UnsafeCursor = new Buffer.UnsafeCursor();

    public static final int reverseBytes(int i) {
        return ((i & 255) << 24) | ((-16777216 & i) >>> 24) | ((16711680 & i) >>> 8) | ((65280 & i) << 8);
    }

    public static final short reverseBytes(short s) {
        short s2 = s & 65535;
        return (short) (((s2 & 255) << 8) | ((65280 & s2) >>> 8));
    }

    public static final void checkOffsetAndCount(long j, long j2, long j3) {
        if ((j2 | j3) < 0 || j2 > j || j - j2 < j3) {
            throw new ArrayIndexOutOfBoundsException("size=" + j + " offset=" + j2 + " byteCount=" + j3);
        }
    }

    public static final boolean arrayRangeEquals(@NotNull byte[] bArr, int i, @NotNull byte[] bArr2, int i2, int i3) {
        Intrinsics.checkNotNullParameter(bArr, "a");
        Intrinsics.checkNotNullParameter(bArr2, "b");
        if (i3 <= 0) {
            return true;
        }
        int i4 = 0;
        while (true) {
            int i5 = i4 + 1;
            if (bArr[i4 + i] != bArr2[i4 + i2]) {
                return false;
            }
            if (i5 >= i3) {
                return true;
            }
            i4 = i5;
        }
    }

    @NotNull
    public static final String toHexString(byte b) {
        return StringsKt__StringsJVMKt.concatToString(new char[]{_ByteStringKt.getHEX_DIGIT_CHARS()[(b >> 4) & 15], _ByteStringKt.getHEX_DIGIT_CHARS()[b & 15]});
    }

    @NotNull
    public static final String toHexString(int i) {
        if (i == 0) {
            return "0";
        }
        int i2 = 0;
        char[] cArr = {_ByteStringKt.getHEX_DIGIT_CHARS()[(i >> 28) & 15], _ByteStringKt.getHEX_DIGIT_CHARS()[(i >> 24) & 15], _ByteStringKt.getHEX_DIGIT_CHARS()[(i >> 20) & 15], _ByteStringKt.getHEX_DIGIT_CHARS()[(i >> 16) & 15], _ByteStringKt.getHEX_DIGIT_CHARS()[(i >> 12) & 15], _ByteStringKt.getHEX_DIGIT_CHARS()[(i >> 8) & 15], _ByteStringKt.getHEX_DIGIT_CHARS()[(i >> 4) & 15], _ByteStringKt.getHEX_DIGIT_CHARS()[i & 15]};
        while (i2 < 8 && cArr[i2] == '0') {
            i2++;
        }
        return StringsKt__StringsJVMKt.concatToString(cArr, i2, 8);
    }

    public static final int resolveDefaultParameter(@NotNull ByteString byteString, int i) {
        Intrinsics.checkNotNullParameter(byteString, "<this>");
        return i == DEFAULT__ByteString_size ? byteString.size() : i;
    }
}
