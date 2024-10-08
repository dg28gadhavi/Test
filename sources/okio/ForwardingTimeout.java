package okio;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ForwardingTimeout.kt */
public class ForwardingTimeout extends Timeout {
    @NotNull
    private Timeout delegate;

    @NotNull
    public final Timeout delegate() {
        return this.delegate;
    }

    public ForwardingTimeout(@NotNull Timeout timeout) {
        Intrinsics.checkNotNullParameter(timeout, "delegate");
        this.delegate = timeout;
    }

    @NotNull
    public final ForwardingTimeout setDelegate(@NotNull Timeout timeout) {
        Intrinsics.checkNotNullParameter(timeout, "delegate");
        this.delegate = timeout;
        return this;
    }

    @NotNull
    public Timeout timeout(long j, @NotNull TimeUnit timeUnit) {
        Intrinsics.checkNotNullParameter(timeUnit, "unit");
        return this.delegate.timeout(j, timeUnit);
    }

    public boolean hasDeadline() {
        return this.delegate.hasDeadline();
    }

    public long deadlineNanoTime() {
        return this.delegate.deadlineNanoTime();
    }

    @NotNull
    public Timeout deadlineNanoTime(long j) {
        return this.delegate.deadlineNanoTime(j);
    }

    @NotNull
    public Timeout clearTimeout() {
        return this.delegate.clearTimeout();
    }

    @NotNull
    public Timeout clearDeadline() {
        return this.delegate.clearDeadline();
    }

    public void throwIfReached() throws IOException {
        this.delegate.throwIfReached();
    }
}
