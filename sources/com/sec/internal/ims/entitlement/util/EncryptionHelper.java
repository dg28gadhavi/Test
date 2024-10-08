package com.sec.internal.ims.entitlement.util;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.util.Base64;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.ims.settings.SettingsProvider;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.log.IMSLog;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionHelper {
    private static final String LOG_TAG = DateUtil.class.getSimpleName();
    private static Map<String, EncryptionHelper> mEncryptionHelpers = new HashMap();
    private Cipher mCipher;

    public static synchronized EncryptionHelper getInstance(String str) {
        EncryptionHelper encryptionHelper;
        synchronized (EncryptionHelper.class) {
            if (mEncryptionHelpers.get(str) == null) {
                mEncryptionHelpers.put(str, new EncryptionHelper(str));
            }
            encryptionHelper = mEncryptionHelpers.get(str);
        }
        return encryptionHelper;
    }

    private EncryptionHelper(String str) {
        try {
            this.mCipher = Cipher.getInstance(str);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "exception " + e.getMessage());
        }
    }

    @SuppressLint({"TrulyRandom"})
    public static SecretKey generateKey(String str) throws NoSuchAlgorithmException {
        KeyGenerator instance = KeyGenerator.getInstance(str);
        instance.init(256, ImsUtil.getRandom());
        return instance.generateKey();
    }

    public static SecretKey getSecretKey(Cursor cursor) {
        String string = cursor.getString(cursor.getColumnIndex(SoftphoneContract.AccountColumns.SECRET_KEY));
        if (string == null) {
            return null;
        }
        byte[] decode = Base64.decode(string, 0);
        return new SecretKeySpec(decode, 0, decode.length, SoftphoneNamespaces.SoftphoneSettings.ENCRYPTION_ALGORITHM);
    }

    public String encrypt(String str, SecretKey secretKey) {
        try {
            this.mCipher.init(1, secretKey);
            return new String(Base64.encode(this.mCipher.doFinal(str.getBytes(StandardCharsets.UTF_8)), 0), StandardCharsets.UTF_8);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "exception " + e.getMessage());
            return null;
        }
    }

    public String encryptAcs(String str) {
        String secretKey = SettingsProvider.getSecretKey();
        if (secretKey != null) {
            String substring = secretKey.substring(0, 16);
            try {
                this.mCipher.init(1, new SecretKeySpec(secretKey.getBytes(), ConfigUtil.TRANSFORMATION), new IvParameterSpec(substring.getBytes()));
                return new String(Base64.encode(this.mCipher.doFinal(str.getBytes(StandardCharsets.UTF_8)), 0), StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String decrypt(String str, SecretKey secretKey) {
        if (!(str == null || secretKey == null)) {
            try {
                this.mCipher.init(2, secretKey);
                return new String(this.mCipher.doFinal(Base64.decode(str, 0)), StandardCharsets.UTF_8);
            } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                String str2 = LOG_TAG;
                IMSLog.s(str2, "exception " + e.getMessage());
            }
        }
        return null;
    }
}
