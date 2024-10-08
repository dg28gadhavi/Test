package okhttp3;

import kotlin.jvm.internal.Intrinsics;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: RequestBody.kt */
public final class RequestBody$Companion$toRequestBody$2 extends RequestBody {
    final /* synthetic */ int $byteCount;
    final /* synthetic */ MediaType $contentType;
    final /* synthetic */ int $offset;
    final /* synthetic */ byte[] $this_toRequestBody;

    RequestBody$Companion$toRequestBody$2(MediaType mediaType, int i, byte[] bArr, int i2) {
        this.$contentType = mediaType;
        this.$byteCount = i;
        this.$this_toRequestBody = bArr;
        this.$offset = i2;
    }

    @Nullable
    public MediaType contentType() {
        return this.$contentType;
    }

    public long contentLength() {
        return (long) this.$byteCount;
    }

    public void writeTo(@NotNull BufferedSink bufferedSink) {
        Intrinsics.checkNotNullParameter(bufferedSink, "sink");
        bufferedSink.write(this.$this_toRequestBody, this.$offset, this.$byteCount);
    }
}
