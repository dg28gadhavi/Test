package com.sec.internal.ims.gba.params;

public enum CipherSuite {
    TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256(new byte[]{-64, 43}),
    TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256(new byte[]{-64, 47}),
    TLS_DHE_RSA_WITH_AES_128_GCM_SHA256(new byte[]{0, -98}),
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA(new byte[]{-64, 10}),
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA(new byte[]{-64, 9}),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA(new byte[]{-64, 19}),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA(new byte[]{-64, 20}),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA(new byte[]{0, 51}),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA(new byte[]{0, 57}),
    TLS_RSA_WITH_AES_128_GCM_SHA256(new byte[]{0, -100}),
    TLS_RSA_WITH_AES_128_CBC_SHA(new byte[]{0, 47}),
    TLS_RSA_WITH_AES_256_CBC_SHA(new byte[]{0, 53}),
    TLS_RSA_WITH_3DES_EDE_CBC_SHA(new byte[]{0, 10}),
    TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384(new byte[]{-64, 48}),
    TLS_AES_256_GCM_SHA384(new byte[]{19, 2}),
    TLS_AES_128_GCM_SHA256(new byte[]{19, 1}),
    TLS_CHACHA20_POLY1305_SHA256(new byte[]{19, 3}),
    TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384(new byte[]{-64, 44}),
    TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256(new byte[]{-52, -87}),
    TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256(new byte[]{-52, -88}),
    TLS_RSA_WITH_AES_256_GCM_SHA384(new byte[]{0, -99}),
    DEFAULT(new byte[]{0, 47});
    
    private byte[] mType;

    private CipherSuite(byte[] bArr) {
        this.mType = bArr;
    }

    public static byte[] forData(String str) {
        try {
            return valueOf(str).mType;
        } catch (IllegalArgumentException unused) {
            return DEFAULT.mType;
        }
    }
}
