package kotlin.collections;

import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Sets.kt */
class SetsKt__SetsKt extends SetsKt__SetsJVMKt {
    @NotNull
    public static final <T> Set<T> emptySet() {
        return EmptySet.INSTANCE;
    }

    @NotNull
    public static final <T> Set<T> optimizeReadOnlySet(@NotNull Set<? extends T> set) {
        Intrinsics.checkNotNullParameter(set, "<this>");
        int size = set.size();
        if (size == 0) {
            return emptySet();
        }
        if (size != 1) {
            return set;
        }
        return SetsKt__SetsJVMKt.setOf(set.iterator().next());
    }
}
