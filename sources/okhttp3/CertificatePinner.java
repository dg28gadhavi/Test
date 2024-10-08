package okhttp3;

import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.net.ssl.SSLPeerUnverifiedException;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.TypeIntrinsics;
import okhttp3.internal.tls.CertificateChainCleaner;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: CertificatePinner.kt */
public final class CertificatePinner {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    @NotNull
    public static final CertificatePinner DEFAULT = new Builder().build();
    @Nullable
    private final CertificateChainCleaner certificateChainCleaner;
    @NotNull
    private final Set<Pin> pins;

    public CertificatePinner(@NotNull Set<Pin> set, @Nullable CertificateChainCleaner certificateChainCleaner2) {
        Intrinsics.checkNotNullParameter(set, "pins");
        this.pins = set;
        this.certificateChainCleaner = certificateChainCleaner2;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ CertificatePinner(Set set, CertificateChainCleaner certificateChainCleaner2, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(set, (i & 2) != 0 ? null : certificateChainCleaner2);
    }

    @Nullable
    public final CertificateChainCleaner getCertificateChainCleaner$okhttp() {
        return this.certificateChainCleaner;
    }

    public final void check(@NotNull String str, @NotNull List<? extends Certificate> list) throws SSLPeerUnverifiedException {
        Intrinsics.checkNotNullParameter(str, "hostname");
        Intrinsics.checkNotNullParameter(list, "peerCertificates");
        check$okhttp(str, new CertificatePinner$check$1(this, list, str));
    }

    public final void check$okhttp(@NotNull String str, @NotNull Function0<? extends List<? extends X509Certificate>> function0) {
        Intrinsics.checkNotNullParameter(str, "hostname");
        Intrinsics.checkNotNullParameter(function0, "cleanedPeerCertificatesFn");
        List<Pin> findMatchingPins = findMatchingPins(str);
        if (!findMatchingPins.isEmpty()) {
            List<X509Certificate> list = (List) function0.invoke();
            for (X509Certificate x509Certificate : list) {
                Iterator<Pin> it = findMatchingPins.iterator();
                ByteString byteString = null;
                ByteString byteString2 = null;
                while (true) {
                    if (it.hasNext()) {
                        Pin next = it.next();
                        String hashAlgorithm = next.getHashAlgorithm();
                        if (Intrinsics.areEqual(hashAlgorithm, "sha256")) {
                            if (byteString == null) {
                                byteString = Companion.sha256Hash(x509Certificate);
                            }
                            if (Intrinsics.areEqual(next.getHash(), byteString)) {
                                return;
                            }
                        } else if (Intrinsics.areEqual(hashAlgorithm, "sha1")) {
                            if (byteString2 == null) {
                                byteString2 = Companion.sha1Hash(x509Certificate);
                            }
                            if (Intrinsics.areEqual(next.getHash(), byteString2)) {
                                return;
                            }
                        } else {
                            throw new AssertionError(Intrinsics.stringPlus("unsupported hashAlgorithm: ", next.getHashAlgorithm()));
                        }
                    }
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Certificate pinning failure!");
            sb.append("\n  Peer certificate chain:");
            for (X509Certificate x509Certificate2 : list) {
                sb.append("\n    ");
                sb.append(Companion.pin(x509Certificate2));
                sb.append(": ");
                sb.append(x509Certificate2.getSubjectDN().getName());
            }
            sb.append("\n  Pinned certificates for ");
            sb.append(str);
            sb.append(":");
            for (Pin append : findMatchingPins) {
                sb.append("\n    ");
                sb.append(append);
            }
            String sb2 = sb.toString();
            Intrinsics.checkNotNullExpressionValue(sb2, "StringBuilder().apply(builderAction).toString()");
            throw new SSLPeerUnverifiedException(sb2);
        }
    }

    @NotNull
    public final List<Pin> findMatchingPins(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "hostname");
        Set<Pin> set = this.pins;
        List<Pin> emptyList = CollectionsKt__CollectionsKt.emptyList();
        for (T next : set) {
            if (((Pin) next).matchesHostname(str)) {
                if (emptyList.isEmpty()) {
                    emptyList = new ArrayList<>();
                }
                TypeIntrinsics.asMutableList(emptyList).add(next);
            }
        }
        return emptyList;
    }

    @NotNull
    public final CertificatePinner withCertificateChainCleaner$okhttp(@NotNull CertificateChainCleaner certificateChainCleaner2) {
        Intrinsics.checkNotNullParameter(certificateChainCleaner2, "certificateChainCleaner");
        return Intrinsics.areEqual(this.certificateChainCleaner, certificateChainCleaner2) ? this : new CertificatePinner(this.pins, certificateChainCleaner2);
    }

    public boolean equals(@Nullable Object obj) {
        if (obj instanceof CertificatePinner) {
            CertificatePinner certificatePinner = (CertificatePinner) obj;
            return Intrinsics.areEqual(certificatePinner.pins, this.pins) && Intrinsics.areEqual(certificatePinner.certificateChainCleaner, this.certificateChainCleaner);
        }
    }

    public int hashCode() {
        int hashCode = (1517 + this.pins.hashCode()) * 41;
        CertificateChainCleaner certificateChainCleaner2 = this.certificateChainCleaner;
        return hashCode + (certificateChainCleaner2 != null ? certificateChainCleaner2.hashCode() : 0);
    }

    /* compiled from: CertificatePinner.kt */
    public static final class Pin {
        @NotNull
        private final ByteString hash;
        @NotNull
        private final String hashAlgorithm;
        @NotNull
        private final String pattern;

        @NotNull
        public final String getHashAlgorithm() {
            return this.hashAlgorithm;
        }

        @NotNull
        public final ByteString getHash() {
            return this.hash;
        }

        public final boolean matchesHostname(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "hostname");
            if (StringsKt__StringsJVMKt.startsWith$default(this.pattern, "**.", false, 2, (Object) null)) {
                int length = this.pattern.length() - 3;
                int length2 = str.length() - length;
                if (!StringsKt__StringsJVMKt.regionMatches$default(str, str.length() - length, this.pattern, 3, length, false, 16, (Object) null)) {
                    return false;
                }
                if (!(length2 == 0 || str.charAt(length2 - 1) == '.')) {
                    return false;
                }
            } else if (!StringsKt__StringsJVMKt.startsWith$default(this.pattern, "*.", false, 2, (Object) null)) {
                return Intrinsics.areEqual(str, this.pattern);
            } else {
                int length3 = this.pattern.length() - 1;
                int length4 = str.length() - length3;
                if (!StringsKt__StringsJVMKt.regionMatches$default(str, str.length() - length3, this.pattern, 1, length3, false, 16, (Object) null)) {
                    return false;
                }
                if (StringsKt__StringsKt.lastIndexOf$default((CharSequence) str, '.', length4 - 1, false, 4, (Object) null) != -1) {
                    return false;
                }
            }
            return true;
        }

        @NotNull
        public String toString() {
            return this.hashAlgorithm + '/' + this.hash.base64();
        }

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Pin)) {
                return false;
            }
            Pin pin = (Pin) obj;
            return Intrinsics.areEqual(this.pattern, pin.pattern) && Intrinsics.areEqual(this.hashAlgorithm, pin.hashAlgorithm) && Intrinsics.areEqual(this.hash, pin.hash);
        }

        public int hashCode() {
            return (((this.pattern.hashCode() * 31) + this.hashAlgorithm.hashCode()) * 31) + this.hash.hashCode();
        }
    }

    /* compiled from: CertificatePinner.kt */
    public static final class Builder {
        @NotNull
        private final List<Pin> pins = new ArrayList();

        @NotNull
        public final CertificatePinner build() {
            return new CertificatePinner(CollectionsKt___CollectionsKt.toSet(this.pins), (CertificateChainCleaner) null, 2, (DefaultConstructorMarker) null);
        }
    }

    /* compiled from: CertificatePinner.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @NotNull
        public final ByteString sha1Hash(@NotNull X509Certificate x509Certificate) {
            Intrinsics.checkNotNullParameter(x509Certificate, "<this>");
            ByteString.Companion companion = ByteString.Companion;
            byte[] encoded = x509Certificate.getPublicKey().getEncoded();
            Intrinsics.checkNotNullExpressionValue(encoded, "publicKey.encoded");
            return ByteString.Companion.of$default(companion, encoded, 0, 0, 3, (Object) null).sha1();
        }

        @NotNull
        public final ByteString sha256Hash(@NotNull X509Certificate x509Certificate) {
            Intrinsics.checkNotNullParameter(x509Certificate, "<this>");
            ByteString.Companion companion = ByteString.Companion;
            byte[] encoded = x509Certificate.getPublicKey().getEncoded();
            Intrinsics.checkNotNullExpressionValue(encoded, "publicKey.encoded");
            return ByteString.Companion.of$default(companion, encoded, 0, 0, 3, (Object) null).sha256();
        }

        @NotNull
        public final String pin(@NotNull Certificate certificate) {
            Intrinsics.checkNotNullParameter(certificate, NSDSContractExt.ConnectivityParamsColumns.CERTIFICATE);
            if (certificate instanceof X509Certificate) {
                return Intrinsics.stringPlus("sha256/", sha256Hash((X509Certificate) certificate).base64());
            }
            throw new IllegalArgumentException("Certificate pinning requires X509 certificates".toString());
        }
    }
}
