package okhttp3.internal.tls;

import java.security.cert.X509Certificate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: TrustRootIndex.kt */
public interface TrustRootIndex {
    @Nullable
    X509Certificate findByIssuerAndSignature(@NotNull X509Certificate x509Certificate);
}
