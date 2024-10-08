package okhttp3.internal.http;

import kotlin.jvm.internal.Intrinsics;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: RealResponseBody.kt */
public final class RealResponseBody extends ResponseBody {
    private final long contentLength;
    @Nullable
    private final String contentTypeString;
    @NotNull
    private final BufferedSource source;

    public RealResponseBody(@Nullable String str, long j, @NotNull BufferedSource bufferedSource) {
        Intrinsics.checkNotNullParameter(bufferedSource, "source");
        this.contentTypeString = str;
        this.contentLength = j;
        this.source = bufferedSource;
    }

    public long contentLength() {
        return this.contentLength;
    }

    @NotNull
    public BufferedSource source() {
        return this.source;
    }
}
