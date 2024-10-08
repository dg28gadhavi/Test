package org.xbill.DNS.utils;

import java.io.ByteArrayOutputStream;

public class base32 {
    private String alphabet;
    private boolean lowercase;
    private boolean padding;

    private static int blockLenToPadding(int i) {
        if (i == 1) {
            return 6;
        }
        if (i == 2) {
            return 4;
        }
        if (i == 3) {
            return 3;
        }
        if (i != 4) {
            return i != 5 ? -1 : 0;
        }
        return 1;
    }

    public base32(String str, boolean z, boolean z2) {
        this.alphabet = str;
        this.padding = z;
        this.lowercase = z2;
    }

    public String toString(byte[] bArr) {
        int i;
        byte[] bArr2 = bArr;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (int i2 = 0; i2 < (bArr2.length + 4) / 5; i2++) {
            short[] sArr = new short[5];
            int[] iArr = new int[8];
            int i3 = 5;
            for (int i4 = 0; i4 < 5; i4++) {
                int i5 = (i2 * 5) + i4;
                if (i5 < bArr2.length) {
                    sArr[i4] = (short) (bArr2[i5] & 255);
                } else {
                    sArr[i4] = 0;
                    i3--;
                }
            }
            int blockLenToPadding = blockLenToPadding(i3);
            short s = sArr[0];
            iArr[0] = (byte) ((s >> 3) & 31);
            short s2 = sArr[1];
            iArr[1] = (byte) (((s & 7) << 2) | ((s2 >> 6) & 3));
            iArr[2] = (byte) ((s2 >> 1) & 31);
            short s3 = sArr[2];
            iArr[3] = (byte) (((s2 & 1) << 4) | ((s3 >> 4) & 15));
            short s4 = sArr[3];
            iArr[4] = (byte) (((s3 & 15) << 1) | (1 & (s4 >> 7)));
            iArr[5] = (byte) ((s4 >> 2) & 31);
            short s5 = sArr[4];
            iArr[6] = (byte) (((s5 >> 5) & 7) | ((s4 & 3) << 3));
            iArr[7] = (byte) (s5 & 31);
            int i6 = 0;
            while (true) {
                i = 8 - blockLenToPadding;
                if (i6 >= i) {
                    break;
                }
                char charAt = this.alphabet.charAt(iArr[i6]);
                if (this.lowercase) {
                    charAt = Character.toLowerCase(charAt);
                }
                byteArrayOutputStream.write(charAt);
                i6++;
            }
            if (this.padding) {
                while (i < 8) {
                    byteArrayOutputStream.write(61);
                    i++;
                }
            }
        }
        return new String(byteArrayOutputStream.toByteArray());
    }
}
