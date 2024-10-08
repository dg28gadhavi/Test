package okhttp3.internal.authenticator;

import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.httpclient.HttpController;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.List;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Address;
import okhttp3.Authenticator;
import okhttp3.Challenge;
import okhttp3.Credentials;
import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: JavaNetAuthenticator.kt */
public final class JavaNetAuthenticator implements Authenticator {
    @NotNull
    private final Dns defaultDns;

    /* compiled from: JavaNetAuthenticator.kt */
    public /* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[Proxy.Type.values().length];
            iArr[Proxy.Type.DIRECT.ordinal()] = 1;
            $EnumSwitchMapping$0 = iArr;
        }
    }

    public JavaNetAuthenticator(@NotNull Dns dns) {
        Intrinsics.checkNotNullParameter(dns, "defaultDns");
        this.defaultDns = dns;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ JavaNetAuthenticator(Dns dns, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? Dns.SYSTEM : dns);
    }

    @Nullable
    public Request authenticate(@Nullable Route route, @NotNull Response response) throws IOException {
        Proxy proxy;
        Dns dns;
        PasswordAuthentication passwordAuthentication;
        Address address;
        Intrinsics.checkNotNullParameter(response, "response");
        List<Challenge> challenges = response.challenges();
        Request request = response.request();
        HttpUrl url = request.url();
        boolean z = response.code() == 407;
        if (route == null) {
            proxy = null;
        } else {
            proxy = route.proxy();
        }
        if (proxy == null) {
            proxy = Proxy.NO_PROXY;
        }
        for (Challenge next : challenges) {
            if (StringsKt__StringsJVMKt.equals(WwwAuthenticateHeader.HEADER_PARAM_BASIC_SCHEME, next.scheme(), true)) {
                if (route == null || (address = route.address()) == null) {
                    dns = null;
                } else {
                    dns = address.dns();
                }
                if (dns == null) {
                    dns = this.defaultDns;
                }
                if (z) {
                    SocketAddress address2 = proxy.address();
                    if (address2 != null) {
                        InetSocketAddress inetSocketAddress = (InetSocketAddress) address2;
                        String hostName = inetSocketAddress.getHostName();
                        Intrinsics.checkNotNullExpressionValue(proxy, "proxy");
                        passwordAuthentication = java.net.Authenticator.requestPasswordAuthentication(hostName, connectToInetAddress(proxy, url, dns), inetSocketAddress.getPort(), url.scheme(), next.realm(), next.scheme(), url.url(), Authenticator.RequestorType.PROXY);
                    } else {
                        throw new NullPointerException("null cannot be cast to non-null type java.net.InetSocketAddress");
                    }
                } else {
                    String host = url.host();
                    Intrinsics.checkNotNullExpressionValue(proxy, "proxy");
                    passwordAuthentication = java.net.Authenticator.requestPasswordAuthentication(host, connectToInetAddress(proxy, url, dns), url.port(), url.scheme(), next.realm(), next.scheme(), url.url(), Authenticator.RequestorType.SERVER);
                }
                if (passwordAuthentication != null) {
                    String str = z ? HttpController.HEADER_PROXY_AUTHORIZATION : "Authorization";
                    String userName = passwordAuthentication.getUserName();
                    Intrinsics.checkNotNullExpressionValue(userName, "auth.userName");
                    char[] password = passwordAuthentication.getPassword();
                    Intrinsics.checkNotNullExpressionValue(password, "auth.password");
                    return request.newBuilder().header(str, Credentials.basic(userName, new String(password), next.charset())).build();
                }
            }
        }
        return null;
    }

    private final InetAddress connectToInetAddress(Proxy proxy, HttpUrl httpUrl, Dns dns) throws IOException {
        Proxy.Type type = proxy.type();
        if ((type == null ? -1 : WhenMappings.$EnumSwitchMapping$0[type.ordinal()]) == 1) {
            return (InetAddress) CollectionsKt___CollectionsKt.first(dns.lookup(httpUrl.host()));
        }
        SocketAddress address = proxy.address();
        if (address != null) {
            InetAddress address2 = ((InetSocketAddress) address).getAddress();
            Intrinsics.checkNotNullExpressionValue(address2, "address() as InetSocketAddress).address");
            return address2;
        }
        throw new NullPointerException("null cannot be cast to non-null type java.net.InetSocketAddress");
    }
}
