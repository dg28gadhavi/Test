package kotlin.comparisons;

import java.util.Comparator;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Comparisons.kt */
final class ReverseOrderComparator implements Comparator<Comparable<? super Object>> {
    @NotNull
    public static final ReverseOrderComparator INSTANCE = new ReverseOrderComparator();

    private ReverseOrderComparator() {
    }

    public int compare(@NotNull Comparable<Object> comparable, @NotNull Comparable<Object> comparable2) {
        Intrinsics.checkNotNullParameter(comparable, "a");
        Intrinsics.checkNotNullParameter(comparable2, "b");
        return comparable2.compareTo(comparable);
    }

    @NotNull
    public final Comparator<Comparable<Object>> reversed() {
        return NaturalOrderComparator.INSTANCE;
    }
}
