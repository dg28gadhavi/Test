package okhttp3;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Dns.kt */
public interface Dns {
    @NotNull
    public static final Companion Companion = Companion.$$INSTANCE;
    @NotNull
    public static final Dns SYSTEM = new Companion.DnsSystem();

    @NotNull
    List<InetAddress> lookup(@NotNull String str) throws UnknownHostException;

    /* compiled from: Dns.kt */
    public static final class Companion {
        static final /* synthetic */ Companion $$INSTANCE = new Companion();

        private Companion() {
        }

        /* compiled from: Dns.kt */
        private static final class DnsSystem implements Dns {
            @NotNull
            public List<InetAddress> lookup(@NotNull String str) {
                Intrinsics.checkNotNullParameter(str, "hostname");
                try {
                    InetAddress[] allByName = InetAddress.getAllByName(str);
                    Intrinsics.checkNotNullExpressionValue(allByName, "getAllByName(hostname)");
                    return ArraysKt___ArraysKt.toList(allByName);
                } catch (NullPointerException e) {
                    UnknownHostException unknownHostException = new UnknownHostException(Intrinsics.stringPlus("Broken system behaviour for dns lookup of ", str));
                    unknownHostException.initCause(e);
                    throw unknownHostException;
                }
            }
        }
    }
}
