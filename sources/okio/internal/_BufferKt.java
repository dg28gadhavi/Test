package okio.internal;

import kotlin.jvm.internal.Intrinsics;
import okio.Buffer;
import okio._JvmPlatformKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: -Buffer.kt */
public final class _BufferKt {
    @NotNull
    private static final byte[] HEX_DIGIT_BYTES = _JvmPlatformKt.asUtf8ToByteArray("0123456789abcdef");

    @NotNull
    public static final byte[] getHEX_DIGIT_BYTES() {
        return HEX_DIGIT_BYTES;
    }

    @NotNull
    public static final String readUtf8Line(@NotNull Buffer buffer, long j) {
        Intrinsics.checkNotNullParameter(buffer, "<this>");
        if (j > 0) {
            long j2 = j - 1;
            if (buffer.getByte(j2) == ((byte) 13)) {
                String readUtf8 = buffer.readUtf8(j2);
                buffer.skip(2);
                return readUtf8;
            }
        }
        String readUtf82 = buffer.readUtf8(j);
        buffer.skip(1);
        return readUtf82;
    }
}
