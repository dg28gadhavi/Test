package okhttp3.internal.platform.android;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.platform.Platform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: StandardAndroidSocketAdapter.kt */
public final class StandardAndroidSocketAdapter extends AndroidSocketAdapter {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    @NotNull
    private final Class<?> paramClass;
    @NotNull
    private final Class<? super SSLSocketFactory> sslSocketFactoryClass;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public StandardAndroidSocketAdapter(@NotNull Class<? super SSLSocket> cls, @NotNull Class<? super SSLSocketFactory> cls2, @NotNull Class<?> cls3) {
        super(cls);
        Intrinsics.checkNotNullParameter(cls, "sslSocketClass");
        Intrinsics.checkNotNullParameter(cls2, "sslSocketFactoryClass");
        Intrinsics.checkNotNullParameter(cls3, "paramClass");
        this.sslSocketFactoryClass = cls2;
        this.paramClass = cls3;
    }

    /* compiled from: StandardAndroidSocketAdapter.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public static /* synthetic */ SocketAdapter buildIfSupported$default(Companion companion, String str, int i, Object obj) {
            if ((i & 1) != 0) {
                str = "com.android.org.conscrypt";
            }
            return companion.buildIfSupported(str);
        }

        @Nullable
        public final SocketAdapter buildIfSupported(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "packageName");
            try {
                Class<?> cls = Class.forName(Intrinsics.stringPlus(str, ".OpenSSLSocketImpl"));
                Class<?> cls2 = Class.forName(Intrinsics.stringPlus(str, ".OpenSSLSocketFactoryImpl"));
                Class<?> cls3 = Class.forName(Intrinsics.stringPlus(str, ".SSLParametersImpl"));
                Intrinsics.checkNotNullExpressionValue(cls3, "paramsClass");
                return new StandardAndroidSocketAdapter(cls, cls2, cls3);
            } catch (Exception e) {
                Platform.Companion.get().log("unable to load android socket classes", 5, e);
                return null;
            }
        }
    }
}
