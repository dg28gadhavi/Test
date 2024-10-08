package okhttp3;

import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;
import java.util.Objects;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.HttpUrl;
import okhttp3.internal.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Address.kt */
public final class Address {
    @Nullable
    private final CertificatePinner certificatePinner;
    @NotNull
    private final List<ConnectionSpec> connectionSpecs;
    @NotNull
    private final Dns dns;
    @Nullable
    private final HostnameVerifier hostnameVerifier;
    @NotNull
    private final List<Protocol> protocols;
    @Nullable
    private final Proxy proxy;
    @NotNull
    private final Authenticator proxyAuthenticator;
    @NotNull
    private final ProxySelector proxySelector;
    @NotNull
    private final SocketFactory socketFactory;
    @Nullable
    private final SSLSocketFactory sslSocketFactory;
    @NotNull
    private final HttpUrl url;

    public Address(@NotNull String str, int i, @NotNull Dns dns2, @NotNull SocketFactory socketFactory2, @Nullable SSLSocketFactory sSLSocketFactory, @Nullable HostnameVerifier hostnameVerifier2, @Nullable CertificatePinner certificatePinner2, @NotNull Authenticator authenticator, @Nullable Proxy proxy2, @NotNull List<? extends Protocol> list, @NotNull List<ConnectionSpec> list2, @NotNull ProxySelector proxySelector2) {
        Intrinsics.checkNotNullParameter(str, "uriHost");
        Intrinsics.checkNotNullParameter(dns2, "dns");
        Intrinsics.checkNotNullParameter(socketFactory2, "socketFactory");
        Intrinsics.checkNotNullParameter(authenticator, "proxyAuthenticator");
        Intrinsics.checkNotNullParameter(list, "protocols");
        Intrinsics.checkNotNullParameter(list2, "connectionSpecs");
        Intrinsics.checkNotNullParameter(proxySelector2, "proxySelector");
        this.dns = dns2;
        this.socketFactory = socketFactory2;
        this.sslSocketFactory = sSLSocketFactory;
        this.hostnameVerifier = hostnameVerifier2;
        this.certificatePinner = certificatePinner2;
        this.proxyAuthenticator = authenticator;
        this.proxy = proxy2;
        this.proxySelector = proxySelector2;
        this.url = new HttpUrl.Builder().scheme(sSLSocketFactory != null ? OMAGlobalVariables.HTTPS : OMAGlobalVariables.HTTP).host(str).port(i).build();
        this.protocols = Util.toImmutableList(list);
        this.connectionSpecs = Util.toImmutableList(list2);
    }

    @NotNull
    public final Dns dns() {
        return this.dns;
    }

    @NotNull
    public final SocketFactory socketFactory() {
        return this.socketFactory;
    }

    @Nullable
    public final SSLSocketFactory sslSocketFactory() {
        return this.sslSocketFactory;
    }

    @Nullable
    public final HostnameVerifier hostnameVerifier() {
        return this.hostnameVerifier;
    }

    @Nullable
    public final CertificatePinner certificatePinner() {
        return this.certificatePinner;
    }

    @NotNull
    public final Authenticator proxyAuthenticator() {
        return this.proxyAuthenticator;
    }

    @Nullable
    public final Proxy proxy() {
        return this.proxy;
    }

    @NotNull
    public final ProxySelector proxySelector() {
        return this.proxySelector;
    }

    @NotNull
    public final HttpUrl url() {
        return this.url;
    }

    @NotNull
    public final List<Protocol> protocols() {
        return this.protocols;
    }

    @NotNull
    public final List<ConnectionSpec> connectionSpecs() {
        return this.connectionSpecs;
    }

    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Address) {
            Address address = (Address) obj;
            return Intrinsics.areEqual(this.url, address.url) && equalsNonHost$okhttp(address);
        }
    }

    public int hashCode() {
        return ((((((((((((((((((527 + this.url.hashCode()) * 31) + this.dns.hashCode()) * 31) + this.proxyAuthenticator.hashCode()) * 31) + this.protocols.hashCode()) * 31) + this.connectionSpecs.hashCode()) * 31) + this.proxySelector.hashCode()) * 31) + Objects.hashCode(this.proxy)) * 31) + Objects.hashCode(this.sslSocketFactory)) * 31) + Objects.hashCode(this.hostnameVerifier)) * 31) + Objects.hashCode(this.certificatePinner);
    }

    public final boolean equalsNonHost$okhttp(@NotNull Address address) {
        Intrinsics.checkNotNullParameter(address, "that");
        return Intrinsics.areEqual(this.dns, address.dns) && Intrinsics.areEqual(this.proxyAuthenticator, address.proxyAuthenticator) && Intrinsics.areEqual(this.protocols, address.protocols) && Intrinsics.areEqual(this.connectionSpecs, address.connectionSpecs) && Intrinsics.areEqual(this.proxySelector, address.proxySelector) && Intrinsics.areEqual(this.proxy, address.proxy) && Intrinsics.areEqual(this.sslSocketFactory, address.sslSocketFactory) && Intrinsics.areEqual(this.hostnameVerifier, address.hostnameVerifier) && Intrinsics.areEqual(this.certificatePinner, address.certificatePinner) && this.url.port() == address.url.port();
    }

    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Address{");
        sb.append(this.url.host());
        sb.append(':');
        sb.append(this.url.port());
        sb.append(", ");
        Proxy proxy2 = this.proxy;
        sb.append(proxy2 != null ? Intrinsics.stringPlus("proxy=", proxy2) : Intrinsics.stringPlus("proxySelector=", this.proxySelector));
        sb.append('}');
        return sb.toString();
    }
}
