package com.sec.internal.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import org.apache.commons.codec.binary.Hex;

public class HashManager {
    private static final String ALGORITHM_MD5 = "MD5";
    public static final String HASH_CARRIERFEATURE = "carrierfeature";
    public static final String HASH_IMSUPDATE = "imsupdate";
    private static final String POSTFIX_FOR_MEMO = "_memo";
    private static final HashMap<Integer, HashManager> sInstances = new HashMap<>();
    private SharedPreferences mSharedPrefs = null;

    private HashManager(Context context, int i) {
        this.mSharedPrefs = ImsSharedPrefHelper.getSharedPref(i, context, ImsSharedPrefHelper.IMS_CONFIG, 0, false);
    }

    public static HashManager getInstance(Context context, int i) {
        HashMap<Integer, HashManager> hashMap = sInstances;
        synchronized (hashMap) {
            if (hashMap.containsKey(Integer.valueOf(i))) {
                HashManager hashManager = hashMap.get(Integer.valueOf(i));
                return hashManager;
            }
            hashMap.put(Integer.valueOf(i), new HashManager(context, i));
            return hashMap.get(Integer.valueOf(i));
        }
    }

    public static String generateMD5(String str) {
        try {
            MessageDigest instance = MessageDigest.getInstance(ALGORITHM_MD5);
            instance.update(str.getBytes("utf-8"));
            byte[] digest = instance.digest();
            StringBuffer stringBuffer = new StringBuffer();
            for (byte b : digest) {
                stringBuffer.append(Integer.toHexString((b & 255) + 256).substring(1));
            }
            return stringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
            return "";
        }
    }

    public static String generateHash(String str) throws NoSuchAlgorithmException {
        MessageDigest instance = MessageDigest.getInstance("SHA1");
        instance.reset();
        byte[] bArr = new byte[0];
        try {
            bArr = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        instance.update(bArr);
        byte[] digest = instance.digest();
        StringBuilder sb = new StringBuilder(digest.length);
        for (byte b : digest) {
            sb.append(Integer.toString((b & 255) + 256, 16).substring(1));
        }
        return sb.toString();
    }

    public String getHash(byte[] bArr) {
        byte[] calcMD5 = calcMD5(bArr);
        if (calcMD5 != null) {
            return new String(Hex.encodeHex(calcMD5));
        }
        return null;
    }

    public boolean isHashChanged(String str, String str2) {
        return !TextUtils.equals(str2, getOldHash(str));
    }

    private String getOldHash(String str) {
        return this.mSharedPrefs.getString(str, "");
    }

    public void saveHash(String str, String str2) {
        SharedPreferences.Editor edit = this.mSharedPrefs.edit();
        edit.putString(str, str2);
        edit.apply();
    }

    public void saveMemo(String str, String str2) {
        SharedPreferences.Editor edit = this.mSharedPrefs.edit();
        edit.putString(str + POSTFIX_FOR_MEMO, str2);
        edit.apply();
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
