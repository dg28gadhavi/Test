package okhttp3.internal.http;

import java.io.IOException;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.connection.RealConnection;
import okio.Sink;
import okio.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ExchangeCodec.kt */
public interface ExchangeCodec {
    void cancel();

    @NotNull
    Sink createRequestBody(@NotNull Request request, long j) throws IOException;

    void finishRequest() throws IOException;

    void flushRequest() throws IOException;

    @NotNull
    RealConnection getConnection();

    @NotNull
    Source openResponseBodySource(@NotNull Response response) throws IOException;

    @Nullable
    Response.Builder readResponseHeaders(boolean z) throws IOException;

    long reportedContentLength(@NotNull Response response) throws IOException;

    void writeRequestHeaders(@NotNull Request request) throws IOException;
}
