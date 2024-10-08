package kotlin.text;

import com.sec.internal.imscr.LogClass;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: StringNumberConversions.kt */
class StringsKt__StringNumberConversionsKt extends StringsKt__StringNumberConversionsJVMKt {
    @Nullable
    public static Integer toIntOrNull(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        return toIntOrNull(str, 10);
    }

    @Nullable
    public static final Integer toIntOrNull(@NotNull String str, int i) {
        int i2;
        boolean z;
        int i3;
        Intrinsics.checkNotNullParameter(str, "<this>");
        int unused = CharsKt__CharJVMKt.checkRadix(i);
        int length = str.length();
        if (length == 0) {
            return null;
        }
        int i4 = 0;
        char charAt = str.charAt(0);
        int compare = Intrinsics.compare(charAt, 48);
        int i5 = LogClass.QEC_REGISTER_VOLTE_CALLBACK;
        if (compare < 0) {
            i2 = 1;
            if (length == 1) {
                return null;
            }
            if (charAt == '-') {
                i5 = Integer.MIN_VALUE;
                z = true;
            } else if (charAt != '+') {
                return null;
            } else {
                z = false;
            }
        } else {
            z = false;
            i2 = 0;
        }
        int i6 = -59652323;
        while (i2 < length) {
            int digitOf = CharsKt__CharJVMKt.digitOf(str.charAt(i2), i);
            if (digitOf < 0) {
                return null;
            }
            if ((i4 < i6 && (i6 != -59652323 || i4 < (i6 = i5 / i))) || (i3 = i4 * i) < i5 + digitOf) {
                return null;
            }
            i4 = i3 - digitOf;
            i2++;
        }
        return z ? Integer.valueOf(i4) : Integer.valueOf(-i4);
    }
}
