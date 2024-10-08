package okhttp3;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/* compiled from: Call.kt */
public interface Call extends Cloneable {
    void enqueue(@NotNull Callback callback);

    @NotNull
    Response execute() throws IOException;

    @NotNull
    Request request();
}
