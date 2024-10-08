package okhttp3.internal.tls;

import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.ims.servicemodules.im.ImDBHelper;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.HostnamesKt;
import okhttp3.internal.Util;
import okio.Utf8;
import org.jetbrains.annotations.NotNull;

/* compiled from: OkHostnameVerifier.kt */
public final class OkHostnameVerifier implements HostnameVerifier {
    @NotNull
    public static final OkHostnameVerifier INSTANCE = new OkHostnameVerifier();

    private OkHostnameVerifier() {
    }

    public boolean verify(@NotNull String str, @NotNull SSLSession sSLSession) {
        Intrinsics.checkNotNullParameter(str, "host");
        Intrinsics.checkNotNullParameter(sSLSession, ImDBHelper.SESSION_TABLE);
        if (!isAscii(str)) {
            return false;
        }
        try {
            Certificate certificate = sSLSession.getPeerCertificates()[0];
            if (certificate != null) {
                return verify(str, (X509Certificate) certificate);
            }
            throw new NullPointerException("null cannot be cast to non-null type java.security.cert.X509Certificate");
        } catch (SSLException unused) {
            return false;
        }
    }

    public final boolean verify(@NotNull String str, @NotNull X509Certificate x509Certificate) {
        Intrinsics.checkNotNullParameter(str, "host");
        Intrinsics.checkNotNullParameter(x509Certificate, NSDSContractExt.ConnectivityParamsColumns.CERTIFICATE);
        if (Util.canParseAsIpAddress(str)) {
            return verifyIpAddress(str, x509Certificate);
        }
        return verifyHostname(str, x509Certificate);
    }

    private final boolean verifyIpAddress(String str, X509Certificate x509Certificate) {
        String canonicalHost = HostnamesKt.toCanonicalHost(str);
        List<String> subjectAltNames = getSubjectAltNames(x509Certificate, 7);
        if ((subjectAltNames instanceof Collection) && subjectAltNames.isEmpty()) {
            return false;
        }
        for (String canonicalHost2 : subjectAltNames) {
            if (Intrinsics.areEqual(canonicalHost, HostnamesKt.toCanonicalHost(canonicalHost2))) {
                return true;
            }
        }
        return false;
    }

    private final boolean verifyHostname(String str, X509Certificate x509Certificate) {
        String asciiToLowercase = asciiToLowercase(str);
        List<String> subjectAltNames = getSubjectAltNames(x509Certificate, 2);
        if ((subjectAltNames instanceof Collection) && subjectAltNames.isEmpty()) {
            return false;
        }
        for (String verifyHostname : subjectAltNames) {
            if (INSTANCE.verifyHostname(asciiToLowercase, verifyHostname)) {
                return true;
            }
        }
        return false;
    }

    private final String asciiToLowercase(String str) {
        if (!isAscii(str)) {
            return str;
        }
        Locale locale = Locale.US;
        Intrinsics.checkNotNullExpressionValue(locale, "US");
        String lowerCase = str.toLowerCase(locale);
        Intrinsics.checkNotNullExpressionValue(lowerCase, "this as java.lang.String).toLowerCase(locale)");
        return lowerCase;
    }

    private final boolean isAscii(String str) {
        return str.length() == ((int) Utf8.size$default(str, 0, 0, 3, (Object) null));
    }

    private final boolean verifyHostname(String str, String str2) {
        if (!(str == null || str.length() == 0) && !StringsKt__StringsJVMKt.startsWith$default(str, ".", false, 2, (Object) null) && !StringsKt__StringsJVMKt.endsWith$default(str, "..", false, 2, (Object) null)) {
            if (!(str2 == null || str2.length() == 0) && !StringsKt__StringsJVMKt.startsWith$default(str2, ".", false, 2, (Object) null) && !StringsKt__StringsJVMKt.endsWith$default(str2, "..", false, 2, (Object) null)) {
                if (!StringsKt__StringsJVMKt.endsWith$default(str, ".", false, 2, (Object) null)) {
                    str = Intrinsics.stringPlus(str, ".");
                }
                String str3 = str;
                if (!StringsKt__StringsJVMKt.endsWith$default(str2, ".", false, 2, (Object) null)) {
                    str2 = Intrinsics.stringPlus(str2, ".");
                }
                String asciiToLowercase = asciiToLowercase(str2);
                if (!StringsKt__StringsKt.contains$default((CharSequence) asciiToLowercase, (CharSequence) "*", false, 2, (Object) null)) {
                    return Intrinsics.areEqual(str3, asciiToLowercase);
                }
                if (!StringsKt__StringsJVMKt.startsWith$default(asciiToLowercase, "*.", false, 2, (Object) null) || StringsKt__StringsKt.indexOf$default((CharSequence) asciiToLowercase, '*', 1, false, 4, (Object) null) != -1 || str3.length() < asciiToLowercase.length() || Intrinsics.areEqual("*.", asciiToLowercase)) {
                    return false;
                }
                String substring = asciiToLowercase.substring(1);
                Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String).substring(startIndex)");
                if (!StringsKt__StringsJVMKt.endsWith$default(str3, substring, false, 2, (Object) null)) {
                    return false;
                }
                int length = str3.length() - substring.length();
                return length <= 0 || StringsKt__StringsKt.lastIndexOf$default((CharSequence) str3, '.', length + -1, false, 4, (Object) null) == -1;
            }
        }
        return false;
    }

    @NotNull
    public final List<String> allSubjectAltNames(@NotNull X509Certificate x509Certificate) {
        Intrinsics.checkNotNullParameter(x509Certificate, NSDSContractExt.ConnectivityParamsColumns.CERTIFICATE);
        return CollectionsKt___CollectionsKt.plus(getSubjectAltNames(x509Certificate, 7), getSubjectAltNames(x509Certificate, 2));
    }

    private final List<String> getSubjectAltNames(X509Certificate x509Certificate, int i) {
        try {
            Collection<List<?>> subjectAlternativeNames = x509Certificate.getSubjectAlternativeNames();
            if (subjectAlternativeNames == null) {
                return CollectionsKt__CollectionsKt.emptyList();
            }
            ArrayList arrayList = new ArrayList();
            for (List next : subjectAlternativeNames) {
                if (next != null) {
                    if (next.size() >= 2) {
                        if (Intrinsics.areEqual(next.get(0), Integer.valueOf(i))) {
                            Object obj = next.get(1);
                            if (obj != null) {
                                arrayList.add((String) obj);
                            }
                        }
                    }
                }
            }
            return arrayList;
        } catch (CertificateParsingException unused) {
            return CollectionsKt__CollectionsKt.emptyList();
        }
    }
}
