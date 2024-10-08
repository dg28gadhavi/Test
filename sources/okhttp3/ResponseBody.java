package okhttp3;

import java.io.Closeable;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.Util;
import okio.Buffer;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ResponseBody.kt */
public abstract class ResponseBody implements Closeable {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);

    public abstract long contentLength();

    @NotNull
    public abstract BufferedSource source();

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004b, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004c, code lost:
        kotlin.io.CloseableKt.closeFinally(r5, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004f, code lost:
        throw r1;
     */
    @org.jetbrains.annotations.NotNull
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final byte[] bytes() throws java.io.IOException {
        /*
            r5 = this;
            long r0 = r5.contentLength()
            r2 = 2147483647(0x7fffffff, double:1.060997895E-314)
            int r2 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r2 > 0) goto L_0x0050
            okio.BufferedSource r5 = r5.source()
            byte[] r2 = r5.readByteArray()     // Catch:{ all -> 0x0049 }
            r3 = 0
            kotlin.io.CloseableKt.closeFinally(r5, r3)
            int r5 = r2.length
            r3 = -1
            int r3 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1))
            if (r3 == 0) goto L_0x0048
            long r3 = (long) r5
            int r3 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1))
            if (r3 != 0) goto L_0x0024
            goto L_0x0048
        L_0x0024:
            java.io.IOException r2 = new java.io.IOException
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "Content-Length ("
            r3.append(r4)
            r3.append(r0)
            java.lang.String r0 = ") and stream length ("
            r3.append(r0)
            r3.append(r5)
            java.lang.String r5 = ") disagree"
            r3.append(r5)
            java.lang.String r5 = r3.toString()
            r2.<init>(r5)
            throw r2
        L_0x0048:
            return r2
        L_0x0049:
            r0 = move-exception
            throw r0     // Catch:{ all -> 0x004b }
        L_0x004b:
            r1 = move-exception
            kotlin.io.CloseableKt.closeFinally(r5, r0)
            throw r1
        L_0x0050:
            java.io.IOException r5 = new java.io.IOException
            java.lang.String r2 = "Cannot buffer entire body for content length: "
            java.lang.Long r0 = java.lang.Long.valueOf(r0)
            java.lang.String r0 = kotlin.jvm.internal.Intrinsics.stringPlus(r2, r0)
            r5.<init>(r0)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.ResponseBody.bytes():byte[]");
    }

    public void close() {
        Util.closeQuietly((Closeable) source());
    }

    /* compiled from: ResponseBody.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public static /* synthetic */ ResponseBody create$default(Companion companion, byte[] bArr, MediaType mediaType, int i, Object obj) {
            if ((i & 1) != 0) {
                mediaType = null;
            }
            return companion.create(bArr, mediaType);
        }

        @NotNull
        public final ResponseBody create(@NotNull byte[] bArr, @Nullable MediaType mediaType) {
            Intrinsics.checkNotNullParameter(bArr, "<this>");
            return create(new Buffer().write(bArr), mediaType, (long) bArr.length);
        }

        @NotNull
        public final ResponseBody create(@NotNull BufferedSource bufferedSource, @Nullable MediaType mediaType, long j) {
            Intrinsics.checkNotNullParameter(bufferedSource, "<this>");
            return new ResponseBody$Companion$asResponseBody$1(mediaType, j, bufferedSource);
        }
    }
}
