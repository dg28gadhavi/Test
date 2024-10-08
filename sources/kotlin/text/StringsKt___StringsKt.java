package kotlin.text;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: _Strings.kt */
class StringsKt___StringsKt extends StringsKt___StringsJvmKt {
    @NotNull
    public static String take(@NotNull String str, int i) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        if (i >= 0) {
            String substring = str.substring(0, RangesKt___RangesKt.coerceAtMost(i, str.length()));
            Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
            return substring;
        }
        throw new IllegalArgumentException(("Requested character count " + i + " is less than zero.").toString());
    }
}
