package okhttp3.internal.platform.android;

import javax.net.ssl.SSLSocket;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.platform.BouncyCastlePlatform;
import okhttp3.internal.platform.android.DeferredSocketAdapter;
import org.bouncycastle.jsse.BCSSLSocket;
import org.jetbrains.annotations.NotNull;

/* compiled from: BouncyCastleSocketAdapter.kt */
public final class BouncyCastleSocketAdapter$Companion$factory$1 implements DeferredSocketAdapter.Factory {
    BouncyCastleSocketAdapter$Companion$factory$1() {
    }

    public boolean matchesSocket(@NotNull SSLSocket sSLSocket) {
        Intrinsics.checkNotNullParameter(sSLSocket, "sslSocket");
        return BouncyCastlePlatform.Companion.isSupported() && (sSLSocket instanceof BCSSLSocket);
    }

    @NotNull
    public SocketAdapter create(@NotNull SSLSocket sSLSocket) {
        Intrinsics.checkNotNullParameter(sSLSocket, "sslSocket");
        return new BouncyCastleSocketAdapter();
    }
}
