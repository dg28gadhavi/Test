package okio;

import java.util.concurrent.TimeUnit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Timeout.kt */
public final class Timeout$Companion$NONE$1 extends Timeout {
    @NotNull
    public Timeout deadlineNanoTime(long j) {
        return this;
    }

    public void throwIfReached() {
    }

    @NotNull
    public Timeout timeout(long j, @NotNull TimeUnit timeUnit) {
        Intrinsics.checkNotNullParameter(timeUnit, "unit");
        return this;
    }

    Timeout$Companion$NONE$1() {
    }
}
