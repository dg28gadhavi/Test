package okhttp3;

import java.net.InetSocketAddress;
import java.net.Proxy;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Route.kt */
public final class Route {
    @NotNull
    private final Address address;
    @NotNull
    private final Proxy proxy;
    @NotNull
    private final InetSocketAddress socketAddress;

    public Route(@NotNull Address address2, @NotNull Proxy proxy2, @NotNull InetSocketAddress inetSocketAddress) {
        Intrinsics.checkNotNullParameter(address2, "address");
        Intrinsics.checkNotNullParameter(proxy2, "proxy");
        Intrinsics.checkNotNullParameter(inetSocketAddress, "socketAddress");
        this.address = address2;
        this.proxy = proxy2;
        this.socketAddress = inetSocketAddress;
    }

    @NotNull
    public final Address address() {
        return this.address;
    }

    @NotNull
    public final Proxy proxy() {
        return this.proxy;
    }

    @NotNull
    public final InetSocketAddress socketAddress() {
        return this.socketAddress;
    }

    public final boolean requiresTunnel() {
        return this.address.sslSocketFactory() != null && this.proxy.type() == Proxy.Type.HTTP;
    }

    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Route) {
            Route route = (Route) obj;
            return Intrinsics.areEqual(route.address, this.address) && Intrinsics.areEqual(route.proxy, this.proxy) && Intrinsics.areEqual(route.socketAddress, this.socketAddress);
        }
    }

    public int hashCode() {
        return ((((527 + this.address.hashCode()) * 31) + this.proxy.hashCode()) * 31) + this.socketAddress.hashCode();
    }

    @NotNull
    public String toString() {
        return "Route{" + this.socketAddress + '}';
    }
}
