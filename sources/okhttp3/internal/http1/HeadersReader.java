package okhttp3.internal.http1;

import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Headers;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;

/* compiled from: HeadersReader.kt */
public final class HeadersReader {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    private long headerLimit = 262144;
    @NotNull
    private final BufferedSource source;

    public HeadersReader(@NotNull BufferedSource bufferedSource) {
        Intrinsics.checkNotNullParameter(bufferedSource, "source");
        this.source = bufferedSource;
    }

    @NotNull
    public final String readLine() {
        String readUtf8LineStrict = this.source.readUtf8LineStrict(this.headerLimit);
        this.headerLimit -= (long) readUtf8LineStrict.length();
        return readUtf8LineStrict;
    }

    @NotNull
    public final Headers readHeaders() {
        Headers.Builder builder = new Headers.Builder();
        while (true) {
            String readLine = readLine();
            if (readLine.length() == 0) {
                return builder.build();
            }
            builder.addLenient$okhttp(readLine);
        }
    }

    /* compiled from: HeadersReader.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }
}
