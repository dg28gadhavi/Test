package okio;

import java.io.IOException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ForwardingSink.kt */
public abstract class ForwardingSink implements Sink {
    @NotNull
    private final Sink delegate;

    public ForwardingSink(@NotNull Sink sink) {
        Intrinsics.checkNotNullParameter(sink, "delegate");
        this.delegate = sink;
    }

    public void write(@NotNull Buffer buffer, long j) throws IOException {
        Intrinsics.checkNotNullParameter(buffer, "source");
        this.delegate.write(buffer, j);
    }

    public void flush() throws IOException {
        this.delegate.flush();
    }

    @NotNull
    public Timeout timeout() {
        return this.delegate.timeout();
    }

    public void close() throws IOException {
        this.delegate.close();
    }

    @NotNull
    public String toString() {
        return getClass().getSimpleName() + '(' + this.delegate + ')';
    }
}
