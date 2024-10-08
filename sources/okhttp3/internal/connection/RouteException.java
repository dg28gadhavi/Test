package okhttp3.internal.connection;

import java.io.IOException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: RouteException.kt */
public final class RouteException extends RuntimeException {
    @NotNull
    private final IOException firstConnectException;
    @NotNull
    private IOException lastConnectException;

    @NotNull
    public final IOException getFirstConnectException() {
        return this.firstConnectException;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RouteException(@NotNull IOException iOException) {
        super(iOException);
        Intrinsics.checkNotNullParameter(iOException, "firstConnectException");
        this.firstConnectException = iOException;
        this.lastConnectException = iOException;
    }

    @NotNull
    public final IOException getLastConnectException() {
        return this.lastConnectException;
    }

    public final void addConnectException(@NotNull IOException iOException) {
        Intrinsics.checkNotNullParameter(iOException, "e");
        ExceptionsKt__ExceptionsKt.addSuppressed(this.firstConnectException, iOException);
        this.lastConnectException = iOException;
    }
}
