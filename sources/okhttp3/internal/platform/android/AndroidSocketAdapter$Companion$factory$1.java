package okhttp3.internal.platform.android;

import javax.net.ssl.SSLSocket;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.platform.android.DeferredSocketAdapter;
import org.jetbrains.annotations.NotNull;

/* compiled from: AndroidSocketAdapter.kt */
public final class AndroidSocketAdapter$Companion$factory$1 implements DeferredSocketAdapter.Factory {
    final /* synthetic */ String $packageName;

    AndroidSocketAdapter$Companion$factory$1(String str) {
        this.$packageName = str;
    }

    public boolean matchesSocket(@NotNull SSLSocket sSLSocket) {
        Intrinsics.checkNotNullParameter(sSLSocket, "sslSocket");
        String name = sSLSocket.getClass().getName();
        Intrinsics.checkNotNullExpressionValue(name, "sslSocket.javaClass.name");
        return StringsKt__StringsJVMKt.startsWith$default(name, Intrinsics.stringPlus(this.$packageName, "."), false, 2, (Object) null);
    }

    @NotNull
    public SocketAdapter create(@NotNull SSLSocket sSLSocket) {
        Intrinsics.checkNotNullParameter(sSLSocket, "sslSocket");
        return AndroidSocketAdapter.Companion.build(sSLSocket.getClass());
    }
}
