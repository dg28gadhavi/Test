package com.sec.internal.helper.httpclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompressionUtil {
    public static final int BUFFER_SIZE = 32;

    public static byte[] compress(String str) throws IOException {
        return compress(str.getBytes());
    }

    public static byte[] compress(byte[] bArr) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bArr.length);
        GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gZIPOutputStream.write(bArr);
        gZIPOutputStream.close();
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return byteArray;
    }

    /* JADX INFO: finally extract failed */
    public static String decompress(byte[] bArr) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        GZIPInputStream gZIPInputStream = new GZIPInputStream(byteArrayInputStream, 32);
        StringBuilder sb = new StringBuilder();
        byte[] bArr2 = new byte[32];
        while (true) {
            try {
                int read = gZIPInputStream.read(bArr2);
                if (read != -1) {
                    sb.append(new String(bArr2, 0, read));
                } else {
                    gZIPInputStream.close();
                    byteArrayInputStream.close();
                    return sb.toString();
                }
            } catch (Throwable th) {
                gZIPInputStream.close();
                throw th;
            }
        }
    }
}
