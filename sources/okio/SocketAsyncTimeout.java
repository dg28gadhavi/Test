package okio;

import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: JvmOkio.kt */
final class SocketAsyncTimeout extends AsyncTimeout {
    @NotNull
    private final Socket socket;

    public SocketAsyncTimeout(@NotNull Socket socket2) {
        Intrinsics.checkNotNullParameter(socket2, "socket");
        this.socket = socket2;
    }

    /* access modifiers changed from: protected */
    @NotNull
    public IOException newTimeoutException(@Nullable IOException iOException) {
        SocketTimeoutException socketTimeoutException = new SocketTimeoutException(EucTestIntent.Extras.TIMEOUT);
        if (iOException != null) {
            socketTimeoutException.initCause(iOException);
        }
        return socketTimeoutException;
    }

    /* access modifiers changed from: protected */
    public void timedOut() {
        try {
            this.socket.close();
        } catch (Exception e) {
            Okio__JvmOkioKt.logger.log(Level.WARNING, Intrinsics.stringPlus("Failed to close timed out socket ", this.socket), e);
        } catch (AssertionError e2) {
            if (Okio.isAndroidGetsocknameError(e2)) {
                Okio__JvmOkioKt.logger.log(Level.WARNING, Intrinsics.stringPlus("Failed to close timed out socket ", this.socket), e2);
                return;
            }
            throw e2;
        }
    }
}
