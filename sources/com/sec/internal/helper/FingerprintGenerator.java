package com.sec.internal.helper;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class FingerprintGenerator {
    private static final String LOG_TAG = "FingerprintGenerator";

    public static String generateFromFile(File file, String str) {
        FileInputStream fileInputStream;
        if (file != null && file.isFile()) {
            try {
                fileInputStream = new FileInputStream(file);
                MessageDigest instance = MessageDigest.getInstance(str);
                byte[] bArr = new byte[1024];
                while (true) {
                    int read = fileInputStream.read(bArr, 0, 1024);
                    if (read != -1) {
                        instance.update(bArr, 0, read);
                    } else {
                        String upperCase = StrUtil.bytesToHexString(instance.digest(), ":").toUpperCase();
                        fileInputStream.close();
                        return upperCase;
                    }
                }
            } catch (Exception e) {
                String str2 = LOG_TAG;
                Log.e(str2, "Unable to generate fingerprint by " + e);
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        return null;
        throw th;
    }
}
