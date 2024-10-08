package com.sec.internal.ims.config.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Fips186_2 {
    public static int fips186_2_prf2(byte[] bArr, byte[] bArr2) {
        BigInteger fromByteArray = fromByteArray(bArr);
        ByteBuffer wrap = ByteBuffer.wrap(bArr2);
        int length = bArr2.length / 40;
        SHA1 sha1 = new SHA1();
        BigInteger pow = new BigInteger("2").pow(bArr.length * 8);
        for (int i = 0; i < length; i++) {
            int i2 = 0;
            while (i2 < 2) {
                sha1.update(Arrays.copyOf(toByteArray(fromByteArray, 20), 64));
                ByteBuffer allocate = ByteBuffer.allocate(20);
                allocate.putInt(sha1.H0);
                allocate.putInt(sha1.H1);
                allocate.putInt(sha1.H2);
                allocate.putInt(sha1.H3);
                allocate.putInt(sha1.H4);
                BigInteger fromByteArray2 = fromByteArray(allocate.array());
                SHA1 sha12 = new SHA1();
                fromByteArray = fromByteArray.add(BigInteger.ONE).add(fromByteArray2).mod(pow);
                wrap.put(toByteArray(fromByteArray2, 20));
                i2++;
                sha1 = sha12;
            }
        }
        return 0;
    }

    static byte[] toByteArray(BigInteger bigInteger, int i) {
        byte[] byteArray = bigInteger.toByteArray();
        if (byteArray.length == i) {
            return byteArray;
        }
        if (byteArray.length > i) {
            return Arrays.copyOfRange(byteArray, byteArray.length - i, byteArray.length);
        }
        byte[] bArr = new byte[i];
        System.arraycopy(byteArray, 0, bArr, i - byteArray.length, byteArray.length);
        return bArr;
    }

    static BigInteger fromByteArray(byte[] bArr) {
        byte[] bArr2 = new byte[(bArr.length + 1)];
        System.arraycopy(bArr, 0, bArr2, 1, bArr.length);
        bArr2[0] = 0;
        return new BigInteger(bArr2);
    }
}
