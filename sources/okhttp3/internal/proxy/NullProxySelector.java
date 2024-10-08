package okhttp3.internal.proxy;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NullProxySelector.kt */
public final class NullProxySelector extends ProxySelector {
    @NotNull
    public static final NullProxySelector INSTANCE = new NullProxySelector();

    public void connectFailed(@Nullable URI uri, @Nullable SocketAddress socketAddress, @Nullable IOException iOException) {
    }

    private NullProxySelector() {
    }

    @NotNull
    public List<Proxy> select(@Nullable URI uri) {
        if (uri != null) {
            return CollectionsKt__CollectionsJVMKt.listOf(Proxy.NO_PROXY);
        }
        throw new IllegalArgumentException("uri must not be null".toString());
    }
}
