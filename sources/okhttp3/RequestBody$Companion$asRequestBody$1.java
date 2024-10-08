package okhttp3;

import java.io.File;
import org.jetbrains.annotations.Nullable;

/* compiled from: RequestBody.kt */
public final class RequestBody$Companion$asRequestBody$1 extends RequestBody {
    final /* synthetic */ MediaType $contentType;
    final /* synthetic */ File $this_asRequestBody;

    RequestBody$Companion$asRequestBody$1(MediaType mediaType, File file) {
        this.$contentType = mediaType;
        this.$this_asRequestBody = file;
    }

    @Nullable
    public MediaType contentType() {
        return this.$contentType;
    }

    public long contentLength() {
        return this.$this_asRequestBody.length();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0016, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0017, code lost:
        kotlin.io.CloseableKt.closeFinally(r1, r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void writeTo(@org.jetbrains.annotations.NotNull okio.BufferedSink r2) {
        /*
            r1 = this;
            java.lang.String r0 = "sink"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r2, r0)
            java.io.File r1 = r1.$this_asRequestBody
            okio.Source r1 = okio.Okio.source((java.io.File) r1)
            r2.writeAll(r1)     // Catch:{ all -> 0x0014 }
            r2 = 0
            kotlin.io.CloseableKt.closeFinally(r1, r2)
            return
        L_0x0014:
            r2 = move-exception
            throw r2     // Catch:{ all -> 0x0016 }
        L_0x0016:
            r0 = move-exception
            kotlin.io.CloseableKt.closeFinally(r1, r2)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.RequestBody$Companion$asRequestBody$1.writeTo(okio.BufferedSink):void");
    }
}
