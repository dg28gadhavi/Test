package org.xbill.DNS.utils;

import java.io.ByteArrayOutputStream;

public class base64 {
    public static String toString(byte[] bArr) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (int i = 0; i < (bArr.length + 2) / 3; i++) {
            short[] sArr = new short[3];
            short[] sArr2 = new short[4];
            for (int i2 = 0; i2 < 3; i2++) {
                int i3 = (i * 3) + i2;
                if (i3 < bArr.length) {
                    sArr[i2] = (short) (bArr[i3] & 255);
                } else {
                    sArr[i2] = -1;
                }
            }
            sArr2[0] = (short) (sArr[0] >> 2);
            short s = sArr[1];
            if (s == -1) {
                sArr2[1] = (short) ((sArr[0] & 3) << 4);
            } else {
                sArr2[1] = (short) (((sArr[0] & 3) << 4) + (s >> 4));
            }
            short s2 = sArr[1];
            if (s2 == -1) {
                sArr2[3] = 64;
                sArr2[2] = 64;
            } else {
                short s3 = sArr[2];
                if (s3 == -1) {
                    sArr2[2] = (short) ((s2 & 15) << 2);
                    sArr2[3] = 64;
                } else {
                    sArr2[2] = (short) (((s2 & 15) << 2) + (s3 >> 6));
                    sArr2[3] = (short) (sArr[2] & 63);
                }
            }
            for (int i4 = 0; i4 < 4; i4++) {
                byteArrayOutputStream.write("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".charAt(sArr2[i4]));
            }
        }
        return new String(byteArrayOutputStream.toByteArray());
    }

    public static String formatString(byte[] bArr, int i, String str, boolean z) {
        String base64 = toString(bArr);
        StringBuffer stringBuffer = new StringBuffer();
        int i2 = 0;
        while (i2 < base64.length()) {
            stringBuffer.append(str);
            int i3 = i2 + i;
            if (i3 >= base64.length()) {
                stringBuffer.append(base64.substring(i2));
                if (z) {
                    stringBuffer.append(" )");
                }
            } else {
                stringBuffer.append(base64.substring(i2, i3));
                stringBuffer.append("\n");
            }
            i2 = i3;
        }
        return stringBuffer.toString();
    }
}
