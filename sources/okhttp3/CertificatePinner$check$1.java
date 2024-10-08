package okhttp3;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;
import okhttp3.internal.tls.CertificateChainCleaner;
import org.jetbrains.annotations.NotNull;

/* compiled from: CertificatePinner.kt */
final class CertificatePinner$check$1 extends Lambda implements Function0<List<? extends X509Certificate>> {
    final /* synthetic */ String $hostname;
    final /* synthetic */ List<Certificate> $peerCertificates;
    final /* synthetic */ CertificatePinner this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    CertificatePinner$check$1(CertificatePinner certificatePinner, List<? extends Certificate> list, String str) {
        super(0);
        this.this$0 = certificatePinner;
        this.$peerCertificates = list;
        this.$hostname = str;
    }

    @NotNull
    public final List<X509Certificate> invoke() {
        CertificateChainCleaner certificateChainCleaner$okhttp = this.this$0.getCertificateChainCleaner$okhttp();
        List<Certificate> clean = certificateChainCleaner$okhttp == null ? null : certificateChainCleaner$okhttp.clean(this.$peerCertificates, this.$hostname);
        if (clean == null) {
            clean = this.$peerCertificates;
        }
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(clean, 10));
        for (Certificate certificate : clean) {
            arrayList.add((X509Certificate) certificate);
        }
        return arrayList;
    }
}
