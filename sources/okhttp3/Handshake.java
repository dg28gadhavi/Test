package okhttp3;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import kotlin.Lazy;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Handshake.kt */
public final class Handshake {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    @NotNull
    private final CipherSuite cipherSuite;
    @NotNull
    private final List<Certificate> localCertificates;
    @NotNull
    private final Lazy peerCertificates$delegate;
    @NotNull
    private final TlsVersion tlsVersion;

    public Handshake(@NotNull TlsVersion tlsVersion2, @NotNull CipherSuite cipherSuite2, @NotNull List<? extends Certificate> list, @NotNull Function0<? extends List<? extends Certificate>> function0) {
        Intrinsics.checkNotNullParameter(tlsVersion2, "tlsVersion");
        Intrinsics.checkNotNullParameter(cipherSuite2, "cipherSuite");
        Intrinsics.checkNotNullParameter(list, "localCertificates");
        Intrinsics.checkNotNullParameter(function0, "peerCertificatesFn");
        this.tlsVersion = tlsVersion2;
        this.cipherSuite = cipherSuite2;
        this.localCertificates = list;
        this.peerCertificates$delegate = LazyKt__LazyJVMKt.lazy(new Handshake$peerCertificates$2(function0));
    }

    @NotNull
    public final TlsVersion tlsVersion() {
        return this.tlsVersion;
    }

    @NotNull
    public final CipherSuite cipherSuite() {
        return this.cipherSuite;
    }

    @NotNull
    public final List<Certificate> localCertificates() {
        return this.localCertificates;
    }

    @NotNull
    public final List<Certificate> peerCertificates() {
        return (List) this.peerCertificates$delegate.getValue();
    }

    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Handshake) {
            Handshake handshake = (Handshake) obj;
            return handshake.tlsVersion == this.tlsVersion && Intrinsics.areEqual(handshake.cipherSuite, this.cipherSuite) && Intrinsics.areEqual(handshake.peerCertificates(), peerCertificates()) && Intrinsics.areEqual(handshake.localCertificates, this.localCertificates);
        }
    }

    public int hashCode() {
        return ((((((527 + this.tlsVersion.hashCode()) * 31) + this.cipherSuite.hashCode()) * 31) + peerCertificates().hashCode()) * 31) + this.localCertificates.hashCode();
    }

    @NotNull
    public String toString() {
        List<Certificate> peerCertificates = peerCertificates();
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(peerCertificates, 10));
        for (Certificate name : peerCertificates) {
            arrayList.add(getName(name));
        }
        String obj = arrayList.toString();
        StringBuilder sb = new StringBuilder();
        sb.append("Handshake{tlsVersion=");
        sb.append(this.tlsVersion);
        sb.append(" cipherSuite=");
        sb.append(this.cipherSuite);
        sb.append(" peerCertificates=");
        sb.append(obj);
        sb.append(" localCertificates=");
        List<Certificate> list = this.localCertificates;
        ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        for (Certificate name2 : list) {
            arrayList2.add(getName(name2));
        }
        sb.append(arrayList2);
        sb.append('}');
        return sb.toString();
    }

    private final String getName(Certificate certificate) {
        if (certificate instanceof X509Certificate) {
            return ((X509Certificate) certificate).getSubjectDN().toString();
        }
        String type = certificate.getType();
        Intrinsics.checkNotNullExpressionValue(type, "type");
        return type;
    }

    /* compiled from: Handshake.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @NotNull
        public final Handshake get(@NotNull SSLSession sSLSession) throws IOException {
            List<Certificate> list;
            Intrinsics.checkNotNullParameter(sSLSession, "<this>");
            String cipherSuite = sSLSession.getCipherSuite();
            if (cipherSuite != null) {
                if (!(Intrinsics.areEqual(cipherSuite, "TLS_NULL_WITH_NULL_NULL") ? true : Intrinsics.areEqual(cipherSuite, "SSL_NULL_WITH_NULL_NULL"))) {
                    CipherSuite forJavaName = CipherSuite.Companion.forJavaName(cipherSuite);
                    String protocol = sSLSession.getProtocol();
                    if (protocol == null) {
                        throw new IllegalStateException("tlsVersion == null".toString());
                    } else if (!Intrinsics.areEqual("NONE", protocol)) {
                        TlsVersion forJavaName2 = TlsVersion.Companion.forJavaName(protocol);
                        try {
                            list = toImmutableList(sSLSession.getPeerCertificates());
                        } catch (SSLPeerUnverifiedException unused) {
                            list = CollectionsKt__CollectionsKt.emptyList();
                        }
                        return new Handshake(forJavaName2, forJavaName, toImmutableList(sSLSession.getLocalCertificates()), new Handshake$Companion$handshake$1(list));
                    } else {
                        throw new IOException("tlsVersion == NONE");
                    }
                } else {
                    throw new IOException(Intrinsics.stringPlus("cipherSuite == ", cipherSuite));
                }
            } else {
                throw new IllegalStateException("cipherSuite == null".toString());
            }
        }

        private final List<Certificate> toImmutableList(Certificate[] certificateArr) {
            if (certificateArr != null) {
                return Util.immutableListOf(Arrays.copyOf(certificateArr, certificateArr.length));
            }
            return CollectionsKt__CollectionsKt.emptyList();
        }
    }
}
