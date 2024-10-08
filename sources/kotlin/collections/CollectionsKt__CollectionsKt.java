package kotlin.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Collections.kt */
class CollectionsKt__CollectionsKt extends CollectionsKt__CollectionsJVMKt {
    @NotNull
    public static final <T> Collection<T> asCollection(@NotNull T[] tArr) {
        Intrinsics.checkNotNullParameter(tArr, "<this>");
        return new ArrayAsCollection(tArr, false);
    }

    @NotNull
    public static <T> List<T> emptyList() {
        return EmptyList.INSTANCE;
    }

    @NotNull
    public static <T> List<T> listOf(@NotNull T... tArr) {
        Intrinsics.checkNotNullParameter(tArr, "elements");
        return tArr.length > 0 ? ArraysKt___ArraysJvmKt.asList(tArr) : emptyList();
    }

    @NotNull
    public static <T> List<T> mutableListOf(@NotNull T... tArr) {
        Intrinsics.checkNotNullParameter(tArr, "elements");
        return tArr.length == 0 ? new ArrayList() : new ArrayList(new ArrayAsCollection(tArr, true));
    }

    @NotNull
    public static <T> List<T> listOfNotNull(@NotNull T... tArr) {
        Intrinsics.checkNotNullParameter(tArr, "elements");
        return ArraysKt___ArraysKt.filterNotNull(tArr);
    }

    public static <T> int getLastIndex(@NotNull List<? extends T> list) {
        Intrinsics.checkNotNullParameter(list, "<this>");
        return list.size() - 1;
    }

    @NotNull
    public static <T> List<T> optimizeReadOnlyList(@NotNull List<? extends T> list) {
        Intrinsics.checkNotNullParameter(list, "<this>");
        int size = list.size();
        if (size == 0) {
            return emptyList();
        }
        if (size != 1) {
            return list;
        }
        return CollectionsKt__CollectionsJVMKt.listOf(list.get(0));
    }

    public static /* synthetic */ int binarySearch$default(List list, Comparable comparable, int i, int i2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            i = 0;
        }
        if ((i3 & 4) != 0) {
            i2 = list.size();
        }
        return binarySearch(list, comparable, i, i2);
    }

    public static final <T extends Comparable<? super T>> int binarySearch(@NotNull List<? extends T> list, @Nullable T t, int i, int i2) {
        Intrinsics.checkNotNullParameter(list, "<this>");
        rangeCheck$CollectionsKt__CollectionsKt(list.size(), i, i2);
        int i3 = i2 - 1;
        while (i <= i3) {
            int i4 = (i + i3) >>> 1;
            int compareValues = ComparisonsKt__ComparisonsKt.compareValues((Comparable) list.get(i4), t);
            if (compareValues < 0) {
                i = i4 + 1;
            } else if (compareValues <= 0) {
                return i4;
            } else {
                i3 = i4 - 1;
            }
        }
        return -(i + 1);
    }

    private static final void rangeCheck$CollectionsKt__CollectionsKt(int i, int i2, int i3) {
        if (i2 > i3) {
            throw new IllegalArgumentException("fromIndex (" + i2 + ") is greater than toIndex (" + i3 + ").");
        } else if (i2 < 0) {
            throw new IndexOutOfBoundsException("fromIndex (" + i2 + ") is less than zero.");
        } else if (i3 > i) {
            throw new IndexOutOfBoundsException("toIndex (" + i3 + ") is greater than size (" + i + ").");
        }
    }

    public static void throwIndexOverflow() {
        throw new ArithmeticException("Index overflow has happened.");
    }
}
