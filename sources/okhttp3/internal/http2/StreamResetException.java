package okhttp3.internal.http2;

import java.io.IOException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: StreamResetException.kt */
public final class StreamResetException extends IOException {
    @NotNull
    public final ErrorCode errorCode;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public StreamResetException(@NotNull ErrorCode errorCode2) {
        super(Intrinsics.stringPlus("stream was reset: ", errorCode2));
        Intrinsics.checkNotNullParameter(errorCode2, "errorCode");
        this.errorCode = errorCode2;
    }
}
