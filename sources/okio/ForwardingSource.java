package okio;

import java.io.IOException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ForwardingSource.kt */
public abstract class ForwardingSource implements Source {
    @NotNull
    private final Source delegate;

    public ForwardingSource(@NotNull Source source) {
        Intrinsics.checkNotNullParameter(source, "delegate");
        this.delegate = source;
    }

    @NotNull
    public final Source delegate() {
        return this.delegate;
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
