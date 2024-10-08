package com.sec.internal.helper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

public class DigestCalculator {
    private static final String ALGORITHM_AKAV1_MD5 = "AKAv1-MD5";
    private static final String ALGORITHM_MD5 = "MD5";
    private static final String QOP_AUTH_INT = "auth-int";
    private static final String SEPARATOR = ":";
    private String algorithm;
    private String cnonce;
    private String digestUri;
    private String httpMethod;
    private String nonce;
    private String nonceCount;
    private byte[] passwd;
    private String qop;
    private String realm;
    private String userName;

    public DigestCalculator(String str, String str2, String str3, String str4, String str5, String str6, String str7, byte[] bArr, String str8, String str9, byte[] bArr2) {
        this.userName = str;
        this.algorithm = str2;
        this.cnonce = str3;
        this.nonce = str4;
        this.nonceCount = str5;
        this.qop = str6;
        this.realm = str7;
        this.passwd = bArr;
        this.httpMethod = str8;
        this.digestUri = str9;
    }

    public String calculateDigest() {
        if (isInputDataValid()) {
            return calculateAuthDigest();
        }
        return null;
    }

    private boolean isInputDataValid() {
        return (this.httpMethod == null || this.algorithm == null || this.cnonce == null || this.qop == null || this.nonce == null || this.nonceCount == null || this.passwd == null || this.realm == null || this.userName == null || this.digestUri == null) ? false : true;
    }

    private String calculateAuthDigest() {
        String calcDigestHA1 = calcDigestHA1();
        String data = getData();
        return new String(Hex.encodeHex(calcMD5((calcDigestHA1 + SEPARATOR + data).getBytes())));
    }

    private String getData() {
        String calcDigestHA2 = calcDigestHA2();
        return this.nonce + SEPARATOR + this.nonceCount + SEPARATOR + this.cnonce + SEPARATOR + this.qop + SEPARATOR + calcDigestHA2;
    }

    private String calcDigestHA2() {
        String str;
        if (this.qop.equalsIgnoreCase(QOP_AUTH_INT)) {
            str = this.httpMethod + SEPARATOR + this.digestUri + SEPARATOR + new String(Hex.encodeHex(calcMD5("".getBytes())));
        } else {
            str = this.httpMethod + SEPARATOR + this.digestUri;
        }
        return new String(Hex.encodeHex(calcMD5(str.getBytes())));
    }

    private String calcDigestHA1() {
        if (!this.algorithm.equalsIgnoreCase(ALGORITHM_MD5) && !this.algorithm.equalsIgnoreCase(ALGORITHM_AKAV1_MD5)) {
            return null;
        }
        byte[] bytes = (this.userName + SEPARATOR + this.realm + SEPARATOR).getBytes();
        byte[] bArr = new byte[(bytes.length + this.passwd.length)];
        System.arraycopy(bytes, 0, bArr, 0, bytes.length);
        byte[] bArr2 = this.passwd;
        System.arraycopy(bArr2, 0, bArr, bytes.length, bArr2.length);
        return new String(Hex.encodeHex(calcMD5(bArr)));
    }

    private byte[] calcMD5(byte[] bArr) {
        try {
            MessageDigest instance = MessageDigest.getInstance(ALGORITHM_MD5);
            instance.reset();
            instance.update(bArr);
            return instance.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
