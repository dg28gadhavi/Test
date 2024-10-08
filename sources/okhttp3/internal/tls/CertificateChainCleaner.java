package okhttp3.internal.tls;

import java.security.cert.Certificate;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.X509TrustManager;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.platform.Platform;
import org.jetbrains.annotations.NotNull;

/* compiled from: CertificateChainCleaner.kt */
public abstract class CertificateChainCleaner {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);

    @NotNull
    public abstract List<Certificate> clean(@NotNull List<? extends Certificate> list, @NotNull String str) throws SSLPeerUnverifiedException;

    /* compiled from: CertificateChainCleaner.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @NotNull
        public final CertificateChainCleaner get(@NotNull X509TrustManager x509TrustManager) {
            Intrinsics.checkNotNullParameter(x509TrustManager, "trustManager");
            return Platform.Companion.get().buildCertificateChainCleaner(x509TrustManager);
        }
    }
}
