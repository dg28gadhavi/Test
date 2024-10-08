package okhttp3.internal.platform.android;

import java.util.List;
import javax.net.ssl.SSLSocket;
import okhttp3.Protocol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: SocketAdapter.kt */
public interface SocketAdapter {
    void configureTlsExtensions(@NotNull SSLSocket sSLSocket, @Nullable String str, @NotNull List<? extends Protocol> list);

    @Nullable
    String getSelectedProtocol(@NotNull SSLSocket sSLSocket);

    boolean isSupported();

    boolean matchesSocket(@NotNull SSLSocket sSLSocket);
}
