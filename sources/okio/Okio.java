package okio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import org.jetbrains.annotations.NotNull;

public final class Okio {
    @NotNull
    public static final BufferedSink buffer(@NotNull Sink sink) {
        return Okio__OkioKt.buffer(sink);
    }

    @NotNull
    public static final BufferedSource buffer(@NotNull Source source) {
        return Okio__OkioKt.buffer(source);
    }

    public static final boolean isAndroidGetsocknameError(@NotNull AssertionError assertionError) {
        return Okio__JvmOkioKt.isAndroidGetsocknameError(assertionError);
    }

    @NotNull
    public static final Sink sink(@NotNull Socket socket) throws IOException {
        return Okio__JvmOkioKt.sink(socket);
    }

    @NotNull
    public static final Source source(@NotNull File file) throws FileNotFoundException {
        return Okio__JvmOkioKt.source(file);
    }

    @NotNull
    public static final Source source(@NotNull InputStream inputStream) {
        return Okio__JvmOkioKt.source(inputStream);
    }

    @NotNull
    public static final Source source(@NotNull Socket socket) throws IOException {
        return Okio__JvmOkioKt.source(socket);
    }
}
