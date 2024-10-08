package okio;

import java.io.IOException;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: AsyncTimeout.kt */
public final class AsyncTimeout$source$1 implements Source {
    final /* synthetic */ Source $source;
    final /* synthetic */ AsyncTimeout this$0;

    AsyncTimeout$source$1(AsyncTimeout asyncTimeout, Source source) {
        this.this$0 = asyncTimeout;
        this.$source = source;
    }

    public long read(@NotNull Buffer buffer, long j) {
        Intrinsics.checkNotNullParameter(buffer, "sink");
        AsyncTimeout asyncTimeout = this.this$0;
        Source source = this.$source;
        asyncTimeout.enter();
        try {
            long read = source.read(buffer, j);
            if (!asyncTimeout.exit()) {
                return read;
            }
            throw asyncTimeout.access$newTimeoutException((IOException) null);
        } catch (IOException e) {
            e = e;
            if (asyncTimeout.exit()) {
                e = asyncTimeout.access$newTimeoutException(e);
            }
            throw e;
        } finally {
            boolean exit = asyncTimeout.exit();
        }
    }

    public void close() {
        AsyncTimeout asyncTimeout = this.this$0;
        Source source = this.$source;
        asyncTimeout.enter();
        try {
            source.close();
            Unit unit = Unit.INSTANCE;
            if (asyncTimeout.exit()) {
                throw asyncTimeout.access$newTimeoutException((IOException) null);
            }
        } catch (IOException e) {
            e = e;
            if (asyncTimeout.exit()) {
                e = asyncTimeout.access$newTimeoutException(e);
            }
            throw e;
        } finally {
            boolean exit = asyncTimeout.exit();
        }
    }

    @NotNull
    public AsyncTimeout timeout() {
        return this.this$0;
    }

    @NotNull
    public String toString() {
        return "AsyncTimeout.source(" + this.$source + ')';
    }
}
