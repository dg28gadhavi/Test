package com.sec.internal.helper;

import android.util.Log;

public class StrUtil {
    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String LOG_TAG = "StrUtil";

    public static String bytesToHexString(byte[] bArr) {
        return bytesToHexString(bArr, "");
    }

    public static String bytesToHexString(byte[] bArr, String str) {
        if (bArr != null) {
            StringBuilder sb = new StringBuilder(bArr.length << 1);
            for (byte b : bArr) {
                char[] cArr = DIGITS;
                sb.append(cArr[(b & 240) >>> 4]);
                sb.append(cArr[b & 15]);
                sb.append(str);
            }
            sb.delete(sb.length() - str.length(), sb.length());
            return sb.toString();
        }
        throw new IllegalArgumentException("bytes cannot be null");
    }

    public static byte[] hexStringToBytes(String str) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        if (length % 2 == 0) {
            byte[] bArr = new byte[(length / 2)];
            for (int i = 0; i < length; i += 2) {
                bArr[i / 2] = (byte) ((hexCharToInt(str.charAt(i)) << 4) | hexCharToInt(str.charAt(i + 1)));
            }
            return bArr;
        }
        throw new IllegalArgumentException("String length shall be even");
    }

    private static int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        char c2 = 'A';
        if (c < 'A' || c > 'F') {
            c2 = 'a';
            if (c < 'a' || c > 'f') {
                throw new RuntimeException("invalid hex char '" + c + "'");
            }
        }
        return (c - c2) + 10;
    }

    public static String convertByteToHexWithLength(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02X", new Object[]{Integer.valueOf(bArr.length)}));
        for (byte valueOf : bArr) {
            sb.append(String.format("%02X", new Object[]{Byte.valueOf(valueOf)}));
        }
        String sb2 = sb.toString();
        Log.d(LOG_TAG, "Byte to Hex: " + sb2);
        return sb2;
    }

    public static String convertHexToString(String str) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < str.length() - 1) {
            int i2 = i + 2;
            sb.append((char) Integer.parseInt(str.substring(i, i2), 16));
            i = i2;
        }
        return sb.toString();
    }
}
