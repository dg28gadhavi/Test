package okhttp3;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/* compiled from: Interceptor.kt */
public interface Interceptor {

    /* compiled from: Interceptor.kt */
    public interface Chain {
        @NotNull
        Call call();

        @NotNull
        Response proceed(@NotNull Request request) throws IOException;

        @NotNull
        Request request();
    }

    @NotNull
    Response intercept(@NotNull Chain chain) throws IOException;
}
