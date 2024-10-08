package kotlin.ranges;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Ranges.kt */
class RangesKt__RangesKt {
    public static final void checkStepIsPositive(boolean z, @NotNull Number number) {
        Intrinsics.checkNotNullParameter(number, "step");
        if (!z) {
            throw new IllegalArgumentException("Step must be positive, was: " + number + '.');
        }
    }
}
