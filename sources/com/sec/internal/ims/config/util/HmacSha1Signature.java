package com.sec.internal.ims.config.util;

import android.text.TextUtils;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.log.IMSLog;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacSha1Signature {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static String calculateRFC2104HMAC(byte[] bArr, byte[] bArr2) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(bArr2, HMAC_SHA1_ALGORITHM);
        Mac instance = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        instance.init(secretKeySpec);
        String bytesToHexString = StrUtil.bytesToHexString(instance.doFinal(bArr));
        IMSLog.s("StrUtil", "calculateRFC2104HMAC: " + bytesToHexString);
        if (TextUtils.isEmpty(bytesToHexString)) {
            return "";
        }
        return bytesToHexString.substring(0, 32);
    }
}
