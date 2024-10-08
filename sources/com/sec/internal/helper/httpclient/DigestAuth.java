package com.sec.internal.helper.httpclient;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.log.IMSLog;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class DigestAuth {
    private static final String AKAV1_MD5 = "AKAv1-MD5";
    private static final String AKAV2_MD5 = "AKAv2-MD5";
    private static final String AKAV2_PASSWORD_KEY = "http-digest-akav2-password";
    private static final String AUTH = "auth";
    private static final String AUTH_INT = "auth-int";
    private static final char[] HEXADECIMAL = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String HMACMD5 = "HmacMD5";
    private static final String LOG_TAG = "DigestAuth";
    private static final String MD5 = "MD5";
    private static final String MD5_SESSION = "MD5-sess";
    private static final String md5 = "md5";
    private Algo mAlgorithm;
    private String mCnonce;
    private String mDigestURI;
    private String mEntity;
    private String mMethod;
    private int mNC = 0;
    private String mNonce;
    private String mPassword;
    private String mQOP;
    private String mRealm;
    private String mUsername;

    public enum Algo {
        UNKNOWN,
        MD5,
        MD5_SESSION,
        AKAV1_MD5,
        AKAV2_MD5,
        md5;

        public static Algo getAlgoType(String str) {
            if (TextUtils.isEmpty(str)) {
                return UNKNOWN;
            }
            str.hashCode();
            char c = 65535;
            switch (str.hashCode()) {
                case -1459419359:
                    if (str.equals(DigestAuth.MD5_SESSION)) {
                        c = 0;
                        break;
                    }
                    break;
                case 76158:
                    if (str.equals(DigestAuth.MD5)) {
                        c = 1;
                        break;
                    }
                    break;
                case 107902:
                    if (str.equals(DigestAuth.md5)) {
                        c = 2;
                        break;
                    }
                    break;
                case 1324439363:
                    if (str.equals(DigestAuth.AKAV1_MD5)) {
                        c = 3;
                        break;
                    }
                    break;
                case 1325362884:
                    if (str.equals(DigestAuth.AKAV2_MD5)) {
                        c = 4;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    return MD5_SESSION;
                case 1:
                    return MD5;
                case 2:
                    return md5;
                case 3:
                    return AKAV1_MD5;
                case 4:
                    return AKAV2_MD5;
                default:
                    return UNKNOWN;
            }
        }

        public String toString() {
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$helper$httpclient$DigestAuth$Algo[ordinal()];
            if (i == 1) {
                return DigestAuth.MD5;
            }
            if (i == 2) {
                return DigestAuth.md5;
            }
            if (i == 3) {
                return DigestAuth.MD5_SESSION;
            }
            if (i != 4) {
                return i != 5 ? "" : DigestAuth.AKAV2_MD5;
            }
            return DigestAuth.AKAV1_MD5;
        }
    }

    /* renamed from: com.sec.internal.helper.httpclient.DigestAuth$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$helper$httpclient$DigestAuth$Algo;

        /* JADX WARNING: Can't wrap try/catch for region: R(12:0|1|2|3|4|5|6|7|8|9|10|12) */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.helper.httpclient.DigestAuth$Algo[] r0 = com.sec.internal.helper.httpclient.DigestAuth.Algo.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$helper$httpclient$DigestAuth$Algo = r0
                com.sec.internal.helper.httpclient.DigestAuth$Algo r1 = com.sec.internal.helper.httpclient.DigestAuth.Algo.MD5     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$helper$httpclient$DigestAuth$Algo     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.helper.httpclient.DigestAuth$Algo r1 = com.sec.internal.helper.httpclient.DigestAuth.Algo.md5     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$helper$httpclient$DigestAuth$Algo     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.helper.httpclient.DigestAuth$Algo r1 = com.sec.internal.helper.httpclient.DigestAuth.Algo.MD5_SESSION     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$helper$httpclient$DigestAuth$Algo     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.helper.httpclient.DigestAuth$Algo r1 = com.sec.internal.helper.httpclient.DigestAuth.Algo.AKAV1_MD5     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$helper$httpclient$DigestAuth$Algo     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.helper.httpclient.DigestAuth$Algo r1 = com.sec.internal.helper.httpclient.DigestAuth.Algo.AKAV2_MD5     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.httpclient.DigestAuth.AnonymousClass1.<clinit>():void");
        }
    }

    public DigestAuth() {
    }

    public DigestAuth(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8) {
        this.mUsername = str;
        this.mPassword = str2;
        this.mRealm = str3;
        this.mNonce = str4;
        this.mMethod = str5;
        this.mDigestURI = str6;
        this.mAlgorithm = Algo.getAlgoType(str7);
        this.mQOP = str8;
        this.mEntity = "";
    }

    public void setDigestAuth(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8) {
        this.mUsername = str;
        this.mPassword = str2;
        this.mRealm = str3;
        this.mNonce = str4;
        this.mMethod = str5;
        this.mDigestURI = str6;
        this.mAlgorithm = Algo.getAlgoType(str7);
        this.mQOP = str8;
        this.mEntity = "";
    }

    public void setDigestAuth(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) {
        this.mUsername = str;
        this.mPassword = str2;
        this.mRealm = str3;
        this.mNonce = str4;
        this.mMethod = str5;
        this.mDigestURI = str6;
        this.mAlgorithm = Algo.getAlgoType(str7);
        this.mQOP = str8;
        this.mEntity = str9;
    }

    public void setBody(String str) {
        this.mEntity = str;
    }

    public void setDigestURI(String str) {
        this.mDigestURI = str;
    }

    public static String createCnonce() {
        byte[] bArr = new byte[8];
        ImsUtil.getRandom().nextBytes(bArr);
        return encode(bArr);
    }

    public static String encode(byte[] bArr) {
        int length = bArr.length;
        char[] cArr = new char[(length * 2)];
        for (int i = 0; i < length; i++) {
            byte b = bArr[i];
            int i2 = i * 2;
            char[] cArr2 = HEXADECIMAL;
            cArr[i2] = cArr2[(b & 240) >> 4];
            cArr[i2 + 1] = cArr2[b & 15];
        }
        return new String(cArr);
    }

    public String getNC() {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format("%08x", new Object[]{Integer.valueOf(this.mNC)});
        formatter.close();
        String str = LOG_TAG;
        IMSLog.d(str, "getNC(): " + sb.toString());
        return sb.toString();
    }

    public String getCnonce() {
        if (AUTH.equalsIgnoreCase(this.mQOP) || AUTH_INT.equalsIgnoreCase(this.mQOP)) {
            return this.mCnonce;
        }
        String str = LOG_TAG;
        IMSLog.d(str, "not auth: " + this.mQOP);
        return "";
    }

    public String getUsername() {
        return this.mUsername;
    }

    public String getRealm() {
        return this.mRealm;
    }

    public String getNonce() {
        return this.mNonce;
    }

    public String getQop() {
        return this.mQOP;
    }

    public String getAlgorithm() {
        return this.mAlgorithm.toString();
    }

    public String getDigestUri() {
        return this.mDigestURI;
    }

    public String getResp() {
        if (this.mAlgorithm == Algo.AKAV2_MD5) {
            this.mPassword = calculatePasswordForAkav2();
        }
        return calcResponseForMD5();
    }

    private String calcResponseForMD5() {
        try {
            MessageDigest instance = MessageDigest.getInstance(MD5);
            this.mNC++;
            this.mCnonce = createCnonce();
            StringBuilder sb = new StringBuilder();
            sb.append(getHexHA1(instance));
            sb.append(":");
            sb.append(this.mNonce);
            sb.append(":");
            if (AUTH.equalsIgnoreCase(this.mQOP) || AUTH_INT.equalsIgnoreCase(this.mQOP)) {
                sb.append(getNC());
                sb.append(":");
                sb.append(this.mCnonce);
                sb.append(":");
                sb.append(this.mQOP);
                sb.append(":");
            }
            sb.append(getHexHA2(instance));
            String encode = encode(instance.digest(sb.toString().getBytes()));
            IMSLog.d(LOG_TAG, "calcResponseForMD5(): contents: " + sb.toString() + ", HEX RESP: " + encode);
            return encode;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getHexHA1(MessageDigest messageDigest) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mUsername);
        sb.append(":");
        sb.append(this.mRealm);
        sb.append(":");
        sb.append(this.mPassword);
        if (this.mAlgorithm == Algo.MD5_SESSION) {
            sb.append(":");
            sb.append(this.mNonce);
            sb.append(":");
            sb.append(this.mCnonce);
        }
        String encode = encode(messageDigest.digest(sb.toString().getBytes(Charset.forName("CP1252"))));
        String str = LOG_TAG;
        IMSLog.d(str, "getHexHA1(): contents: " + sb.toString() + ", HEX HA1: " + encode);
        return encode;
    }

    private String getHexHA2(MessageDigest messageDigest) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mMethod);
        sb.append(":");
        sb.append(this.mDigestURI);
        if (AUTH_INT.equalsIgnoreCase(this.mQOP)) {
            sb.append(":");
            sb.append(getEntityHash(messageDigest));
        }
        String encode = encode(messageDigest.digest(sb.toString().getBytes()));
        String str = LOG_TAG;
        IMSLog.d(str, "getHexHA2(): : contents: " + sb.toString() + ", HEX HA2: " + encode);
        return encode;
    }

    private String getEntityHash(MessageDigest messageDigest) {
        String encode = encode(messageDigest.digest(this.mEntity.getBytes()));
        String str = LOG_TAG;
        IMSLog.d(str, "getEntityHash(): contents: " + this.mEntity + ", HEX entityHash: " + encode);
        return encode;
    }

    private String calculatePasswordForAkav2() {
        try {
            return encode(hmacMD5(AKAV2_PASSWORD_KEY.getBytes(), this.mPassword.getBytes()));
        } catch (Exception unused) {
            Log.e(LOG_TAG, "Hmac encryption failed");
            return "";
        }
    }

    private byte[] hmacMD5(byte[] bArr, byte[] bArr2) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac instance = Mac.getInstance(HMACMD5);
        instance.init(new SecretKeySpec(bArr, HMACMD5));
        return instance.doFinal(bArr2);
    }
}
