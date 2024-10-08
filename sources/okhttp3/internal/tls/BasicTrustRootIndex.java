package okhttp3.internal.tls;

import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: BasicTrustRootIndex.kt */
public final class BasicTrustRootIndex implements TrustRootIndex {
    @NotNull
    private final Map<X500Principal, Set<X509Certificate>> subjectToCaCerts;

    public BasicTrustRootIndex(@NotNull X509Certificate... x509CertificateArr) {
        Intrinsics.checkNotNullParameter(x509CertificateArr, "caCerts");
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        int length = x509CertificateArr.length;
        int i = 0;
        while (i < length) {
            X509Certificate x509Certificate = x509CertificateArr[i];
            i++;
            X500Principal subjectX500Principal = x509Certificate.getSubjectX500Principal();
            Intrinsics.checkNotNullExpressionValue(subjectX500Principal, "caCert.subjectX500Principal");
            Object obj = linkedHashMap.get(subjectX500Principal);
            if (obj == null) {
                obj = new LinkedHashSet();
                linkedHashMap.put(subjectX500Principal, obj);
            }
            ((Set) obj).add(x509Certificate);
        }
        this.subjectToCaCerts = linkedHashMap;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: java.security.cert.X509Certificate} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: java.security.cert.X509Certificate} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: java.security.cert.X509Certificate} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: java.security.cert.X509Certificate} */
    /* JADX WARNING: Multi-variable type inference failed */
    @org.jetbrains.annotations.Nullable
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.security.cert.X509Certificate findByIssuerAndSignature(@org.jetbrains.annotations.NotNull java.security.cert.X509Certificate r4) {
        /*
            r3 = this;
            java.lang.String r0 = "cert"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r4, r0)
            javax.security.auth.x500.X500Principal r0 = r4.getIssuerX500Principal()
            java.util.Map<javax.security.auth.x500.X500Principal, java.util.Set<java.security.cert.X509Certificate>> r3 = r3.subjectToCaCerts
            java.lang.Object r3 = r3.get(r0)
            java.util.Set r3 = (java.util.Set) r3
            r0 = 0
            if (r3 != 0) goto L_0x0015
            return r0
        L_0x0015:
            java.util.Iterator r3 = r3.iterator()
        L_0x0019:
            boolean r1 = r3.hasNext()
            if (r1 == 0) goto L_0x0033
            java.lang.Object r1 = r3.next()
            r2 = r1
            java.security.cert.X509Certificate r2 = (java.security.cert.X509Certificate) r2
            java.security.PublicKey r2 = r2.getPublicKey()     // Catch:{ Exception -> 0x002f }
            r4.verify(r2)     // Catch:{ Exception -> 0x002f }
            r2 = 1
            goto L_0x0030
        L_0x002f:
            r2 = 0
        L_0x0030:
            if (r2 == 0) goto L_0x0019
            r0 = r1
        L_0x0033:
            java.security.cert.X509Certificate r0 = (java.security.cert.X509Certificate) r0
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.tls.BasicTrustRootIndex.findByIssuerAndSignature(java.security.cert.X509Certificate):java.security.cert.X509Certificate");
    }

    public boolean equals(@Nullable Object obj) {
        return obj == this || ((obj instanceof BasicTrustRootIndex) && Intrinsics.areEqual(((BasicTrustRootIndex) obj).subjectToCaCerts, this.subjectToCaCerts));
    }

    public int hashCode() {
        return this.subjectToCaCerts.hashCode();
    }
}
