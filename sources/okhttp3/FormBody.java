package okhttp3;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.HttpUrl;
import okhttp3.internal.Util;
import okio.Buffer;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: FormBody.kt */
public final class FormBody extends RequestBody {
    @NotNull
    private static final MediaType CONTENT_TYPE = MediaType.Companion.get("application/x-www-form-urlencoded");
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    @NotNull
    private final List<String> encodedNames;
    @NotNull
    private final List<String> encodedValues;

    public FormBody(@NotNull List<String> list, @NotNull List<String> list2) {
        Intrinsics.checkNotNullParameter(list, "encodedNames");
        Intrinsics.checkNotNullParameter(list2, "encodedValues");
        this.encodedNames = Util.toImmutableList(list);
        this.encodedValues = Util.toImmutableList(list2);
    }

    @NotNull
    public MediaType contentType() {
        return CONTENT_TYPE;
    }

    public long contentLength() {
        return writeOrCountBytes((BufferedSink) null, true);
    }

    public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
        Intrinsics.checkNotNullParameter(bufferedSink, "sink");
        writeOrCountBytes(bufferedSink, false);
    }

    private final long writeOrCountBytes(BufferedSink bufferedSink, boolean z) {
        Buffer buffer;
        if (z) {
            buffer = new Buffer();
        } else {
            Intrinsics.checkNotNull(bufferedSink);
            buffer = bufferedSink.getBuffer();
        }
        int size = this.encodedNames.size();
        int i = 0;
        while (i < size) {
            int i2 = i + 1;
            if (i > 0) {
                buffer.writeByte(38);
            }
            buffer.writeUtf8(this.encodedNames.get(i));
            buffer.writeByte(61);
            buffer.writeUtf8(this.encodedValues.get(i));
            i = i2;
        }
        if (!z) {
            return 0;
        }
        long size2 = buffer.size();
        buffer.clear();
        return size2;
    }

    /* compiled from: FormBody.kt */
    public static final class Builder {
        @Nullable
        private final Charset charset;
        @NotNull
        private final List<String> names;
        @NotNull
        private final List<String> values;

        public Builder() {
            this((Charset) null, 1, (DefaultConstructorMarker) null);
        }

        public Builder(@Nullable Charset charset2) {
            this.charset = charset2;
            this.names = new ArrayList();
            this.values = new ArrayList();
        }

        /* JADX INFO: this call moved to the top of the method (can break code semantics) */
        public /* synthetic */ Builder(Charset charset2, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this((i & 1) != 0 ? null : charset2);
        }

        @NotNull
        public final Builder add(@NotNull String str, @NotNull String str2) {
            String str3 = str;
            Intrinsics.checkNotNullParameter(str3, "name");
            Intrinsics.checkNotNullParameter(str2, "value");
            List<String> list = this.names;
            HttpUrl.Companion companion = HttpUrl.Companion;
            list.add(HttpUrl.Companion.canonicalize$okhttp$default(companion, str3, 0, 0, " \"':;<=>@[]^`{}|/\\?#&!$(),~", false, false, true, false, this.charset, 91, (Object) null));
            this.values.add(HttpUrl.Companion.canonicalize$okhttp$default(companion, str2, 0, 0, " \"':;<=>@[]^`{}|/\\?#&!$(),~", false, false, true, false, this.charset, 91, (Object) null));
            return this;
        }

        @NotNull
        public final FormBody build() {
            return new FormBody(this.names, this.values);
        }
    }

    /* compiled from: FormBody.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }
}
