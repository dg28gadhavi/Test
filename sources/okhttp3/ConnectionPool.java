package okhttp3;

import java.util.concurrent.TimeUnit;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.concurrent.TaskRunner;
import okhttp3.internal.connection.RealConnectionPool;
import org.jetbrains.annotations.NotNull;

/* compiled from: ConnectionPool.kt */
public final class ConnectionPool {
    @NotNull
    private final RealConnectionPool delegate;

    public ConnectionPool(@NotNull RealConnectionPool realConnectionPool) {
        Intrinsics.checkNotNullParameter(realConnectionPool, "delegate");
        this.delegate = realConnectionPool;
    }

    @NotNull
    public final RealConnectionPool getDelegate$okhttp() {
        return this.delegate;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ConnectionPool(int i, long j, @NotNull TimeUnit timeUnit) {
        this(new RealConnectionPool(TaskRunner.INSTANCE, i, j, timeUnit));
        Intrinsics.checkNotNullParameter(timeUnit, "timeUnit");
    }

    public ConnectionPool() {
        this(5, 5, TimeUnit.MINUTES);
    }
}
