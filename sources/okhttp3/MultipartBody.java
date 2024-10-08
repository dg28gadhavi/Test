package okhttp3;

import com.sec.internal.helper.httpclient.HttpPostBody;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.MediaType;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MultipartBody.kt */
public final class MultipartBody extends RequestBody {
    @NotNull
    public static final MediaType ALTERNATIVE;
    @NotNull
    private static final byte[] COLONSPACE = {58, 32};
    @NotNull
    private static final byte[] CRLF = {13, 10};
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    @NotNull
    private static final byte[] DASHDASH = {45, 45};
    @NotNull
    public static final MediaType DIGEST;
    @NotNull
    public static final MediaType FORM;
    @NotNull
    public static final MediaType MIXED;
    @NotNull
    public static final MediaType PARALLEL;
    @NotNull
    private final ByteString boundaryByteString;
    private long contentLength = -1;
    @NotNull
    private final MediaType contentType;
    @NotNull
    private final List<Part> parts;
    @NotNull
    private final MediaType type;

    public MultipartBody(@NotNull ByteString byteString, @NotNull MediaType mediaType, @NotNull List<Part> list) {
        Intrinsics.checkNotNullParameter(byteString, "boundaryByteString");
        Intrinsics.checkNotNullParameter(mediaType, "type");
        Intrinsics.checkNotNullParameter(list, "parts");
        this.boundaryByteString = byteString;
        this.type = mediaType;
        this.parts = list;
        MediaType.Companion companion = MediaType.Companion;
        this.contentType = companion.get(mediaType + "; boundary=" + boundary());
    }

    @NotNull
    public final String boundary() {
        return this.boundaryByteString.utf8();
    }

    @NotNull
    public MediaType contentType() {
        return this.contentType;
    }

    public long contentLength() throws IOException {
        long j = this.contentLength;
        if (j != -1) {
            return j;
        }
        long writeOrCountBytes = writeOrCountBytes((BufferedSink) null, true);
        this.contentLength = writeOrCountBytes;
        return writeOrCountBytes;
    }

    public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
        Intrinsics.checkNotNullParameter(bufferedSink, "sink");
        writeOrCountBytes(bufferedSink, false);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v1, resolved type: okio.BufferedSink} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: okio.Buffer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: okio.Buffer} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v2, resolved type: okio.BufferedSink} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: okio.Buffer} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final long writeOrCountBytes(okio.BufferedSink r14, boolean r15) throws java.io.IOException {
        /*
            r13 = this;
            if (r15 == 0) goto L_0x0009
            okio.Buffer r14 = new okio.Buffer
            r14.<init>()
            r0 = r14
            goto L_0x000a
        L_0x0009:
            r0 = 0
        L_0x000a:
            java.util.List<okhttp3.MultipartBody$Part> r1 = r13.parts
            int r1 = r1.size()
            r2 = 0
            r3 = 0
            r5 = r2
        L_0x0014:
            if (r5 >= r1) goto L_0x00b0
            int r6 = r5 + 1
            java.util.List<okhttp3.MultipartBody$Part> r7 = r13.parts
            java.lang.Object r5 = r7.get(r5)
            okhttp3.MultipartBody$Part r5 = (okhttp3.MultipartBody.Part) r5
            okhttp3.Headers r7 = r5.headers()
            okhttp3.RequestBody r5 = r5.body()
            kotlin.jvm.internal.Intrinsics.checkNotNull(r14)
            byte[] r8 = DASHDASH
            r14.write((byte[]) r8)
            okio.ByteString r8 = r13.boundaryByteString
            r14.write((okio.ByteString) r8)
            byte[] r8 = CRLF
            r14.write((byte[]) r8)
            if (r7 == 0) goto L_0x0062
            int r8 = r7.size()
            r9 = r2
        L_0x0041:
            if (r9 >= r8) goto L_0x0062
            int r10 = r9 + 1
            java.lang.String r11 = r7.name(r9)
            okio.BufferedSink r11 = r14.writeUtf8(r11)
            byte[] r12 = COLONSPACE
            okio.BufferedSink r11 = r11.write((byte[]) r12)
            java.lang.String r9 = r7.value(r9)
            okio.BufferedSink r9 = r11.writeUtf8(r9)
            byte[] r11 = CRLF
            r9.write((byte[]) r11)
            r9 = r10
            goto L_0x0041
        L_0x0062:
            okhttp3.MediaType r7 = r5.contentType()
            if (r7 == 0) goto L_0x007b
            java.lang.String r8 = "Content-Type: "
            okio.BufferedSink r8 = r14.writeUtf8(r8)
            java.lang.String r7 = r7.toString()
            okio.BufferedSink r7 = r8.writeUtf8(r7)
            byte[] r8 = CRLF
            r7.write((byte[]) r8)
        L_0x007b:
            long r7 = r5.contentLength()
            r9 = -1
            int r11 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1))
            if (r11 == 0) goto L_0x0095
            java.lang.String r9 = "Content-Length: "
            okio.BufferedSink r9 = r14.writeUtf8(r9)
            okio.BufferedSink r9 = r9.writeDecimalLong(r7)
            byte[] r10 = CRLF
            r9.write((byte[]) r10)
            goto L_0x009e
        L_0x0095:
            if (r15 == 0) goto L_0x009e
            kotlin.jvm.internal.Intrinsics.checkNotNull(r0)
            r0.clear()
            return r9
        L_0x009e:
            byte[] r9 = CRLF
            r14.write((byte[]) r9)
            if (r15 == 0) goto L_0x00a7
            long r3 = r3 + r7
            goto L_0x00aa
        L_0x00a7:
            r5.writeTo(r14)
        L_0x00aa:
            r14.write((byte[]) r9)
            r5 = r6
            goto L_0x0014
        L_0x00b0:
            kotlin.jvm.internal.Intrinsics.checkNotNull(r14)
            byte[] r1 = DASHDASH
            r14.write((byte[]) r1)
            okio.ByteString r13 = r13.boundaryByteString
            r14.write((okio.ByteString) r13)
            r14.write((byte[]) r1)
            byte[] r13 = CRLF
            r14.write((byte[]) r13)
            if (r15 == 0) goto L_0x00d2
            kotlin.jvm.internal.Intrinsics.checkNotNull(r0)
            long r13 = r0.size()
            long r3 = r3 + r13
            r0.clear()
        L_0x00d2:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.MultipartBody.writeOrCountBytes(okio.BufferedSink, boolean):long");
    }

    /* compiled from: MultipartBody.kt */
    public static final class Part {
        @NotNull
        public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
        @NotNull
        private final RequestBody body;
        @Nullable
        private final Headers headers;

        public /* synthetic */ Part(Headers headers2, RequestBody requestBody, DefaultConstructorMarker defaultConstructorMarker) {
            this(headers2, requestBody);
        }

        private Part(Headers headers2, RequestBody requestBody) {
            this.headers = headers2;
            this.body = requestBody;
        }

        @Nullable
        public final Headers headers() {
            return this.headers;
        }

        @NotNull
        public final RequestBody body() {
            return this.body;
        }

        /* compiled from: MultipartBody.kt */
        public static final class Companion {
            public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
                this();
            }

            private Companion() {
            }

            @NotNull
            public final Part create(@Nullable Headers headers, @NotNull RequestBody requestBody) {
                String str;
                Intrinsics.checkNotNullParameter(requestBody, "body");
                boolean z = true;
                if ((headers == null ? null : headers.get("Content-Type")) == null) {
                    if (headers == null) {
                        str = null;
                    } else {
                        str = headers.get("Content-Length");
                    }
                    if (str != null) {
                        z = false;
                    }
                    if (z) {
                        return new Part(headers, requestBody, (DefaultConstructorMarker) null);
                    }
                    throw new IllegalArgumentException("Unexpected header: Content-Length".toString());
                }
                throw new IllegalArgumentException("Unexpected header: Content-Type".toString());
            }
        }
    }

    /* compiled from: MultipartBody.kt */
    public static final class Builder {
        @NotNull
        private final ByteString boundary;
        @NotNull
        private final List<Part> parts;
        @NotNull
        private MediaType type;

        public Builder() {
            this((String) null, 1, (DefaultConstructorMarker) null);
        }

        public Builder(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "boundary");
            this.boundary = ByteString.Companion.encodeUtf8(str);
            this.type = MultipartBody.MIXED;
            this.parts = new ArrayList();
        }

        /* JADX WARNING: Illegal instructions before constructor call */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public /* synthetic */ Builder(java.lang.String r1, int r2, kotlin.jvm.internal.DefaultConstructorMarker r3) {
            /*
                r0 = this;
                r2 = r2 & 1
                if (r2 == 0) goto L_0x0012
                java.util.UUID r1 = java.util.UUID.randomUUID()
                java.lang.String r1 = r1.toString()
                java.lang.String r2 = "randomUUID().toString()"
                kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r1, r2)
            L_0x0012:
                r0.<init>(r1)
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: okhttp3.MultipartBody.Builder.<init>(java.lang.String, int, kotlin.jvm.internal.DefaultConstructorMarker):void");
        }

        @NotNull
        public final Builder setType(@NotNull MediaType mediaType) {
            Intrinsics.checkNotNullParameter(mediaType, "type");
            if (Intrinsics.areEqual(mediaType.type(), "multipart")) {
                this.type = mediaType;
                return this;
            }
            throw new IllegalArgumentException(Intrinsics.stringPlus("multipart != ", mediaType).toString());
        }

        @NotNull
        public final Builder addPart(@Nullable Headers headers, @NotNull RequestBody requestBody) {
            Intrinsics.checkNotNullParameter(requestBody, "body");
            addPart(Part.Companion.create(headers, requestBody));
            return this;
        }

        @NotNull
        public final Builder addPart(@NotNull Part part) {
            Intrinsics.checkNotNullParameter(part, "part");
            this.parts.add(part);
            return this;
        }

        @NotNull
        public final MultipartBody build() {
            if (!this.parts.isEmpty()) {
                return new MultipartBody(this.boundary, this.type, Util.toImmutableList(this.parts));
            }
            throw new IllegalStateException("Multipart body must have at least one part.".toString());
        }
    }

    /* compiled from: MultipartBody.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }

    static {
        MediaType.Companion companion = MediaType.Companion;
        MIXED = companion.get("multipart/mixed");
        ALTERNATIVE = companion.get("multipart/alternative");
        DIGEST = companion.get("multipart/digest");
        PARALLEL = companion.get("multipart/parallel");
        FORM = companion.get(HttpPostBody.CONTENT_TYPE_MULTIPART_FORMDATA);
    }
}
