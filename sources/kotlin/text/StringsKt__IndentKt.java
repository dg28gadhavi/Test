package kotlin.text;

import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Indent.kt */
class StringsKt__IndentKt extends StringsKt__AppendableKt {
    public static /* synthetic */ String trimMargin$default(String str, String str2, int i, Object obj) {
        if ((i & 1) != 0) {
            str2 = "|";
        }
        return trimMargin(str, str2);
    }

    @NotNull
    public static final String trimMargin(@NotNull String str, @NotNull String str2) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        Intrinsics.checkNotNullParameter(str2, "marginPrefix");
        return replaceIndentByMargin(str, "", str2);
    }

    @NotNull
    public static final String replaceIndentByMargin(@NotNull String str, @NotNull String str2, @NotNull String str3) {
        int i;
        String invoke;
        Intrinsics.checkNotNullParameter(str, "<this>");
        Intrinsics.checkNotNullParameter(str2, "newIndent");
        Intrinsics.checkNotNullParameter(str3, "marginPrefix");
        if (!StringsKt__StringsJVMKt.isBlank(str3)) {
            List<String> lines = StringsKt__StringsKt.lines(str);
            int length = str.length() + (str2.length() * lines.size());
            Function1<String, String> indentFunction$StringsKt__IndentKt = getIndentFunction$StringsKt__IndentKt(str2);
            int lastIndex = CollectionsKt__CollectionsKt.getLastIndex(lines);
            ArrayList arrayList = new ArrayList();
            int i2 = 0;
            for (T next : lines) {
                int i3 = i2 + 1;
                if (i2 < 0) {
                    CollectionsKt__CollectionsKt.throwIndexOverflow();
                }
                String str4 = (String) next;
                String str5 = null;
                if ((i2 == 0 || i2 == lastIndex) && StringsKt__StringsJVMKt.isBlank(str4)) {
                    str4 = null;
                } else {
                    int length2 = str4.length();
                    int i4 = 0;
                    while (true) {
                        if (i4 >= length2) {
                            i = -1;
                            break;
                        } else if (!CharsKt__CharJVMKt.isWhitespace(str4.charAt(i4))) {
                            i = i4;
                            break;
                        } else {
                            i4++;
                        }
                    }
                    if (i != -1) {
                        int i5 = i;
                        if (StringsKt__StringsJVMKt.startsWith$default(str4, str3, i, false, 4, (Object) null)) {
                            str5 = str4.substring(i5 + str3.length());
                            Intrinsics.checkNotNullExpressionValue(str5, "this as java.lang.String).substring(startIndex)");
                        }
                    }
                    if (!(str5 == null || (invoke = indentFunction$StringsKt__IndentKt.invoke(str5)) == null)) {
                        str4 = invoke;
                    }
                }
                if (str4 != null) {
                    arrayList.add(str4);
                }
                i2 = i3;
            }
            String sb = ((StringBuilder) CollectionsKt___CollectionsKt.joinTo$default(arrayList, new StringBuilder(length), "\n", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) null, 124, (Object) null)).toString();
            Intrinsics.checkNotNullExpressionValue(sb, "mapIndexedNotNull { inde…\"\\n\")\n        .toString()");
            return sb;
        }
        throw new IllegalArgumentException("marginPrefix must be non-blank string.".toString());
    }

    private static final Function1<String, String> getIndentFunction$StringsKt__IndentKt(String str) {
        if (str.length() == 0) {
            return StringsKt__IndentKt$getIndentFunction$1.INSTANCE;
        }
        return new StringsKt__IndentKt$getIndentFunction$2(str);
    }
}
