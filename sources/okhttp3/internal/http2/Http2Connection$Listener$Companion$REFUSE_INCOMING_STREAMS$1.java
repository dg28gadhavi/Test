package okhttp3.internal.http2;

import java.io.IOException;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.http2.Http2Connection;
import org.jetbrains.annotations.NotNull;

/* compiled from: Http2Connection.kt */
public final class Http2Connection$Listener$Companion$REFUSE_INCOMING_STREAMS$1 extends Http2Connection.Listener {
    Http2Connection$Listener$Companion$REFUSE_INCOMING_STREAMS$1() {
    }

    public void onStream(@NotNull Http2Stream http2Stream) throws IOException {
        Intrinsics.checkNotNullParameter(http2Stream, "stream");
        http2Stream.close(ErrorCode.REFUSED_STREAM, (IOException) null);
    }
}
