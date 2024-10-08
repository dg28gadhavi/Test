package okhttp3;

import java.security.cert.Certificate;
import java.util.List;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: Handshake.kt */
final class Handshake$Companion$handshake$1 extends Lambda implements Function0<List<? extends Certificate>> {
    final /* synthetic */ List<Certificate> $peerCertificatesCopy;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    Handshake$Companion$handshake$1(List<? extends Certificate> list) {
        super(0);
        this.$peerCertificatesCopy = list;
    }

    @NotNull
    public final List<Certificate> invoke() {
        return this.$peerCertificatesCopy;
    }
}
