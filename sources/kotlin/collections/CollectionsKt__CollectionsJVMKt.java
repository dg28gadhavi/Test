package kotlin.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: CollectionsJVM.kt */
class CollectionsKt__CollectionsJVMKt {
    @NotNull
    public static <T> List<T> listOf(T t) {
        List<T> singletonList = Collections.singletonList(t);
        Intrinsics.checkNotNullExpressionValue(singletonList, "singletonList(element)");
        return singletonList;
    }

    @NotNull
    public static final <T> Object[] copyToArrayOfAny(@NotNull T[] tArr, boolean z) {
        Intrinsics.checkNotNullParameter(tArr, "<this>");
        Class<Object[]> cls = Object[].class;
        if (z && Intrinsics.areEqual(tArr.getClass(), cls)) {
            return tArr;
        }
        Object[] copyOf = Arrays.copyOf(tArr, tArr.length, cls);
        Intrinsics.checkNotNullExpressionValue(copyOf, "copyOf(this, this.size, Array<Any?>::class.java)");
        return copyOf;
    }
}
