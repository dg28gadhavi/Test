package okhttp3;

import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;

/* compiled from: ResponseBody.kt */
public final class ResponseBody$Companion$asResponseBody$1 extends ResponseBody {
    final /* synthetic */ long $contentLength;
    final /* synthetic */ MediaType $contentType;
    final /* synthetic */ BufferedSource $this_asResponseBody;

    ResponseBody$Companion$asResponseBody$1(MediaType mediaType, long j, BufferedSource bufferedSource) {
        this.$contentType = mediaType;
        this.$contentLength = j;
        this.$this_asResponseBody = bufferedSource;
    }

    public long contentLength() {
        return this.$contentLength;
    }

    @NotNull
    public BufferedSource source() {
        return this.$this_asResponseBody;
    }
}
