package okhttp3;

import java.io.IOException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.authenticator.JavaNetAuthenticator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Authenticator.kt */
public interface Authenticator {
    @NotNull
    public static final Companion Companion = Companion.$$INSTANCE;
    @NotNull
    public static final Authenticator JAVA_NET_AUTHENTICATOR = new JavaNetAuthenticator((Dns) null, 1, (DefaultConstructorMarker) null);
    @NotNull
    public static final Authenticator NONE = new Companion.AuthenticatorNone();

    @Nullable
    Request authenticate(@Nullable Route route, @NotNull Response response) throws IOException;

    /* compiled from: Authenticator.kt */
    public static final class Companion {
        static final /* synthetic */ Companion $$INSTANCE = new Companion();

        /* compiled from: Authenticator.kt */
        private static final class AuthenticatorNone implements Authenticator {
            @Nullable
            public Request authenticate(@Nullable Route route, @NotNull Response response) {
                Intrinsics.checkNotNullParameter(response, "response");
                return null;
            }
        }

        private Companion() {
        }
    }
}
