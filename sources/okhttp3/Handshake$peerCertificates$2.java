package okhttp3;

import java.security.cert.Certificate;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: Handshake.kt */
final class Handshake$peerCertificates$2 extends Lambda implements Function0<List<? extends Certificate>> {
    final /* synthetic */ Function0<List<Certificate>> $peerCertificatesFn;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    Handshake$peerCertificates$2(Function0<? extends List<? extends Certificate>> function0) {
        super(0);
        this.$peerCertificatesFn = function0;
    }

    @NotNull
    public final List<Certificate> invoke() {
        try {
            return this.$peerCertificatesFn.invoke();
        } catch (SSLPeerUnverifiedException unused) {
            return CollectionsKt__CollectionsKt.emptyList();
        }
    }
}
