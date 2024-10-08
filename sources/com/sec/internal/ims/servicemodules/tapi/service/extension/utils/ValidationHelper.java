package com.sec.internal.ims.servicemodules.tapi.service.extension.utils;

import android.content.pm.Signature;
import android.util.Base64;
import com.sec.internal.helper.header.AuthenticationHeaders;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;

public class ValidationHelper {
    public static boolean isTapiAuthorisationSupports() {
        return false;
    }

    private static char[] byte2hex(byte b) {
        return new char[]{"0123456789ABCDEF".charAt((b & 240) >> 4), "0123456789ABCDEF".charAt(b & 15)};
    }

    public static String hash(byte[] bArr) {
        try {
            return new String(Base64.encode(MessageDigest.getInstance(Constants.DIGEST_ALGORITHM_SHA224).digest(bArr), 2)).replace('+', '-').replace('/', '_').replace(AuthenticationHeaders.HEADER_PRARAM_SPERATOR, "");
        } catch (NoSuchAlgorithmException unused) {
            return null;
        }
    }

    public static String getFingerPrint(Signature signature) {
        X509Certificate x509Certificate;
        try {
            x509Certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(signature.toByteArray()));
        } catch (CertificateException e) {
            e.printStackTrace();
            x509Certificate = null;
        }
        Objects.requireNonNull(x509Certificate);
        X509Certificate x509Certificate2 = x509Certificate;
        return loadFingerprint(x509Certificate);
    }

    private static String loadFingerprint(X509Certificate x509Certificate) {
        try {
            byte[] bArr = new byte[0];
            try {
                bArr = MessageDigest.getInstance(Constants.DIGEST_ALGORITHM_SHA1).digest(x509Certificate.getEncoded());
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
            }
            StringBuilder sb = new StringBuilder();
            int length = bArr.length - 1;
            for (int i = 0; i < length; i++) {
                sb.append(byte2hex(bArr[i]));
                sb.append(':');
            }
            sb.append(byte2hex(bArr[length]));
            return sb.toString();
        } catch (NoSuchAlgorithmException unused) {
            return null;
        }
    }

    public static boolean isContained(String[] strArr, String str) {
        for (String equalsIgnoreCase : strArr) {
            if (equalsIgnoreCase.equalsIgnoreCase(str)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkKeyLength(PublicKey publicKey) {
        return (publicKey instanceof RSAPublicKey) && ((RSAPublicKey) publicKey).getModulus().bitLength() >= 2048;
    }

    public static String encrypt(String str) {
        return Base64.encodeToString(str.getBytes(), 0);
    }

    public static String decrypt(String str) {
        return new String(Base64.decode(str, 0));
    }
}
