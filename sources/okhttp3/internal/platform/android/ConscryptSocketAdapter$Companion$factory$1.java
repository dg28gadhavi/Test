package okhttp3.internal.platform.android;

import javax.net.ssl.SSLSocket;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.platform.ConscryptPlatform;
import okhttp3.internal.platform.android.DeferredSocketAdapter;
import org.conscrypt.Conscrypt;
import org.jetbrains.annotations.NotNull;

/* compiled from: ConscryptSocketAdapter.kt */
public final class ConscryptSocketAdapter$Companion$factory$1 implements DeferredSocketAdapter.Factory {
    ConscryptSocketAdapter$Companion$factory$1() {
    }

    public boolean matchesSocket(@NotNull SSLSocket sSLSocket) {
        Intrinsics.checkNotNullParameter(sSLSocket, "sslSocket");
        return ConscryptPlatform.Companion.isSupported() && Conscrypt.isConscrypt(sSLSocket);
    }

    @NotNull
    public SocketAdapter create(@NotNull SSLSocket sSLSocket) {
        Intrinsics.checkNotNullParameter(sSLSocket, "sslSocket");
        return new ConscryptSocketAdapter();
    }
}
