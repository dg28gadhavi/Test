package kotlin.comparisons;

import java.util.Comparator;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Comparisons.kt */
final class NaturalOrderComparator implements Comparator<Comparable<? super Object>> {
    @NotNull
    public static final NaturalOrderComparator INSTANCE = new NaturalOrderComparator();

    private NaturalOrderComparator() {
    }

    public int compare(@NotNull Comparable<Object> comparable, @NotNull Comparable<Object> comparable2) {
        Intrinsics.checkNotNullParameter(comparable, "a");
        Intrinsics.checkNotNullParameter(comparable2, "b");
        return comparable.compareTo(comparable2);
    }

    @NotNull
    public final Comparator<Comparable<Object>> reversed() {
        return ReverseOrderComparator.INSTANCE;
    }
}
