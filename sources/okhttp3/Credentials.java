package okhttp3;

import java.nio.charset.Charset;
import kotlin.jvm.internal.Intrinsics;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;

/* compiled from: Credentials.kt */
public final class Credentials {
    @NotNull
    public static final Credentials INSTANCE = new Credentials();

    private Credentials() {
    }

    @NotNull
    public static final String basic(@NotNull String str, @NotNull String str2, @NotNull Charset charset) {
        Intrinsics.checkNotNullParameter(str, "username");
        Intrinsics.checkNotNullParameter(str2, "password");
        Intrinsics.checkNotNullParameter(charset, "charset");
        return Intrinsics.stringPlus("Basic ", ByteString.Companion.encodeString(str + ':' + str2, charset).base64());
    }
}
