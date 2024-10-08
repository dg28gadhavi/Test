package okhttp3.internal.platform.android;

import java.util.List;
import javax.net.ssl.SSLSocket;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Protocol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: DeferredSocketAdapter.kt */
public final class DeferredSocketAdapter implements SocketAdapter {
    @Nullable
    private SocketAdapter delegate;
    @NotNull
    private final Factory socketAdapterFactory;

    /* compiled from: DeferredSocketAdapter.kt */
    public interface Factory {
        @NotNull
        SocketAdapter create(@NotNull SSLSocket sSLSocket);

        boolean matchesSocket(@NotNull SSLSocket sSLSocket);
    }

    public boolean isSupported() {
        return true;
    }

    public DeferredSocketAdapter(@NotNull Factory factory) {
        Intrinsics.checkNotNullParameter(factory, "socketAdapterFactory");
        this.socketAdapterFactory = factory;
    }

    public boolean matchesSocket(@NotNull SSLSocket sSLSocket) {
        Intrinsics.checkNotNullParameter(sSLSocket, "sslSocket");
        return this.socketAdapterFactory.matchesSocket(sSLSocket);
    }

    public void configureTlsExtensions(@NotNull SSLSocket sSLSocket, @Nullable String str, @NotNull List<? extends Protocol> list) {
        Intrinsics.checkNotNullParameter(sSLSocket, "sslSocket");
        Intrinsics.checkNotNullParameter(list, "protocols");
        SocketAdapter delegate2 = getDelegate(sSLSocket);
        if (delegate2 != null) {
            delegate2.configureTlsExtensions(sSLSocket, str, list);
        }
    }

    @Nullable
    public String getSelectedProtocol(@NotNull SSLSocket sSLSocket) {
        Intrinsics.checkNotNullParameter(sSLSocket, "sslSocket");
        SocketAdapter delegate2 = getDelegate(sSLSocket);
        if (delegate2 == null) {
            return null;
        }
        return delegate2.getSelectedProtocol(sSLSocket);
    }

    private final synchronized SocketAdapter getDelegate(SSLSocket sSLSocket) {
        if (this.delegate == null && this.socketAdapterFactory.matchesSocket(sSLSocket)) {
            this.delegate = this.socketAdapterFactory.create(sSLSocket);
        }
        return this.delegate;
    }
}
