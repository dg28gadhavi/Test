package okhttp3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.internal.ProgressionUtilKt;
import kotlin.jvm.internal.ArrayIteratorKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.jvm.internal.markers.KMappedMarker;
import okhttp3.internal.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Headers.kt */
public final class Headers implements Iterable<Pair<? extends String, ? extends String>>, KMappedMarker {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    @NotNull
    private final String[] namesAndValues;

    public /* synthetic */ Headers(String[] strArr, DefaultConstructorMarker defaultConstructorMarker) {
        this(strArr);
    }

    private Headers(String[] strArr) {
        this.namesAndValues = strArr;
    }

    @Nullable
    public final String get(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "name");
        return Companion.get(this.namesAndValues, str);
    }

    public final int size() {
        return this.namesAndValues.length / 2;
    }

    @NotNull
    public final String name(int i) {
        return this.namesAndValues[i * 2];
    }

    @NotNull
    public final String value(int i) {
        return this.namesAndValues[(i * 2) + 1];
    }

    @NotNull
    public final Set<String> names() {
        TreeSet treeSet = new TreeSet(StringsKt__StringsJVMKt.getCASE_INSENSITIVE_ORDER(StringCompanionObject.INSTANCE));
        int size = size();
        for (int i = 0; i < size; i++) {
            treeSet.add(name(i));
        }
        Set<String> unmodifiableSet = Collections.unmodifiableSet(treeSet);
        Intrinsics.checkNotNullExpressionValue(unmodifiableSet, "unmodifiableSet(result)");
        return unmodifiableSet;
    }

    @NotNull
    public final List<String> values(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "name");
        int size = size();
        ArrayList arrayList = null;
        int i = 0;
        while (i < size) {
            int i2 = i + 1;
            if (StringsKt__StringsJVMKt.equals(str, name(i), true)) {
                if (arrayList == null) {
                    arrayList = new ArrayList(2);
                }
                arrayList.add(value(i));
            }
            i = i2;
        }
        if (arrayList == null) {
            return CollectionsKt__CollectionsKt.emptyList();
        }
        List<String> unmodifiableList = Collections.unmodifiableList(arrayList);
        Intrinsics.checkNotNullExpressionValue(unmodifiableList, "{\n      Collections.unmodifiableList(result)\n    }");
        return unmodifiableList;
    }

    @NotNull
    public Iterator<Pair<String, String>> iterator() {
        int size = size();
        Pair[] pairArr = new Pair[size];
        for (int i = 0; i < size; i++) {
            pairArr[i] = TuplesKt.to(name(i), value(i));
        }
        return ArrayIteratorKt.iterator(pairArr);
    }

    @NotNull
    public final Builder newBuilder() {
        Builder builder = new Builder();
        boolean unused = CollectionsKt__MutableCollectionsKt.addAll(builder.getNamesAndValues$okhttp(), (T[]) this.namesAndValues);
        return builder;
    }

    public boolean equals(@Nullable Object obj) {
        return (obj instanceof Headers) && Arrays.equals(this.namesAndValues, ((Headers) obj).namesAndValues);
    }

    public int hashCode() {
        return Arrays.hashCode(this.namesAndValues);
    }

    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int size = size();
        int i = 0;
        while (i < size) {
            int i2 = i + 1;
            String name = name(i);
            String value = value(i);
            sb.append(name);
            sb.append(": ");
            if (Util.isSensitiveHeader(name)) {
                value = "██";
            }
            sb.append(value);
            sb.append("\n");
            i = i2;
        }
        String sb2 = sb.toString();
        Intrinsics.checkNotNullExpressionValue(sb2, "StringBuilder().apply(builderAction).toString()");
        return sb2;
    }

    /* compiled from: Headers.kt */
    public static final class Builder {
        @NotNull
        private final List<String> namesAndValues = new ArrayList(20);

        @NotNull
        public final List<String> getNamesAndValues$okhttp() {
            return this.namesAndValues;
        }

        @NotNull
        public final Builder addLenient$okhttp(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "line");
            int indexOf$default = StringsKt__StringsKt.indexOf$default((CharSequence) str, ':', 1, false, 4, (Object) null);
            if (indexOf$default != -1) {
                String substring = str.substring(0, indexOf$default);
                Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
                String substring2 = str.substring(indexOf$default + 1);
                Intrinsics.checkNotNullExpressionValue(substring2, "this as java.lang.String).substring(startIndex)");
                addLenient$okhttp(substring, substring2);
            } else if (str.charAt(0) == ':') {
                String substring3 = str.substring(1);
                Intrinsics.checkNotNullExpressionValue(substring3, "this as java.lang.String).substring(startIndex)");
                addLenient$okhttp("", substring3);
            } else {
                addLenient$okhttp("", str);
            }
            return this;
        }

        @NotNull
        public final Builder add(@NotNull String str, @NotNull String str2) {
            Intrinsics.checkNotNullParameter(str, "name");
            Intrinsics.checkNotNullParameter(str2, "value");
            Companion companion = Headers.Companion;
            companion.checkName(str);
            companion.checkValue(str2, str);
            addLenient$okhttp(str, str2);
            return this;
        }

        @NotNull
        public final Builder addUnsafeNonAscii(@NotNull String str, @NotNull String str2) {
            Intrinsics.checkNotNullParameter(str, "name");
            Intrinsics.checkNotNullParameter(str2, "value");
            Headers.Companion.checkName(str);
            addLenient$okhttp(str, str2);
            return this;
        }

        @NotNull
        public final Builder addLenient$okhttp(@NotNull String str, @NotNull String str2) {
            Intrinsics.checkNotNullParameter(str, "name");
            Intrinsics.checkNotNullParameter(str2, "value");
            getNamesAndValues$okhttp().add(str);
            getNamesAndValues$okhttp().add(StringsKt__StringsKt.trim(str2).toString());
            return this;
        }

        @NotNull
        public final Builder removeAll(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "name");
            int i = 0;
            while (i < getNamesAndValues$okhttp().size()) {
                if (StringsKt__StringsJVMKt.equals(str, getNamesAndValues$okhttp().get(i), true)) {
                    getNamesAndValues$okhttp().remove(i);
                    getNamesAndValues$okhttp().remove(i);
                    i -= 2;
                }
                i += 2;
            }
            return this;
        }

        @NotNull
        public final Builder set(@NotNull String str, @NotNull String str2) {
            Intrinsics.checkNotNullParameter(str, "name");
            Intrinsics.checkNotNullParameter(str2, "value");
            Companion companion = Headers.Companion;
            companion.checkName(str);
            companion.checkValue(str2, str);
            removeAll(str);
            addLenient$okhttp(str, str2);
            return this;
        }

        @NotNull
        public final Headers build() {
            Object[] array = this.namesAndValues.toArray(new String[0]);
            if (array != null) {
                return new Headers((String[]) array, (DefaultConstructorMarker) null);
            }
            throw new NullPointerException("null cannot be cast to non-null type kotlin.Array<T of kotlin.collections.ArraysKt__ArraysJVMKt.toTypedArray>");
        }
    }

    /* compiled from: Headers.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        /* access modifiers changed from: private */
        public final String get(String[] strArr, String str) {
            int length = strArr.length - 2;
            int progressionLastElement = ProgressionUtilKt.getProgressionLastElement(length, 0, -2);
            if (progressionLastElement > length) {
                return null;
            }
            while (true) {
                int i = length - 2;
                if (StringsKt__StringsJVMKt.equals(str, strArr[length], true)) {
                    return strArr[length + 1];
                }
                if (length == progressionLastElement) {
                    return null;
                }
                length = i;
            }
        }

        @NotNull
        public final Headers of(@NotNull String... strArr) {
            Intrinsics.checkNotNullParameter(strArr, "namesAndValues");
            int i = 0;
            if (strArr.length % 2 == 0) {
                String[] strArr2 = (String[]) strArr.clone();
                int length = strArr2.length;
                int i2 = 0;
                while (i2 < length) {
                    int i3 = i2 + 1;
                    String str = strArr2[i2];
                    if (str != null) {
                        strArr2[i2] = StringsKt__StringsKt.trim(str).toString();
                        i2 = i3;
                    } else {
                        throw new IllegalArgumentException("Headers cannot be null".toString());
                    }
                }
                int progressionLastElement = ProgressionUtilKt.getProgressionLastElement(0, strArr2.length - 1, 2);
                if (progressionLastElement >= 0) {
                    while (true) {
                        int i4 = i + 2;
                        String str2 = strArr2[i];
                        String str3 = strArr2[i + 1];
                        checkName(str2);
                        checkValue(str3, str2);
                        if (i == progressionLastElement) {
                            break;
                        }
                        i = i4;
                    }
                }
                return new Headers(strArr2, (DefaultConstructorMarker) null);
            }
            throw new IllegalArgumentException("Expected alternating header names and values".toString());
        }

        /* access modifiers changed from: private */
        public final void checkName(String str) {
            if (str.length() > 0) {
                int length = str.length();
                int i = 0;
                while (i < length) {
                    int i2 = i + 1;
                    char charAt = str.charAt(i);
                    if ('!' <= charAt && charAt < 127) {
                        i = i2;
                    } else {
                        throw new IllegalArgumentException(Util.format("Unexpected char %#04x at %d in header name: %s", Integer.valueOf(charAt), Integer.valueOf(i), str).toString());
                    }
                }
                return;
            }
            throw new IllegalArgumentException("name is empty".toString());
        }

        /* access modifiers changed from: private */
        public final void checkValue(String str, String str2) {
            int length = str.length();
            int i = 0;
            while (i < length) {
                int i2 = i + 1;
                char charAt = str.charAt(i);
                boolean z = true;
                if (charAt != 9) {
                    if (!(' ' <= charAt && charAt < 127)) {
                        z = false;
                    }
                }
                if (!z) {
                    throw new IllegalArgumentException(Intrinsics.stringPlus(Util.format("Unexpected char %#04x at %d in %s value", Integer.valueOf(charAt), Integer.valueOf(i), str2), Util.isSensitiveHeader(str2) ? "" : Intrinsics.stringPlus(": ", str)).toString());
                }
                i = i2;
            }
        }
    }
}
