package com.sec.internal.ims.util;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import java.io.File;

public class StorageEnvironment {
    private static String LOG_TAG = "StorageEnvironment";

    public static String generateStorePath(String str) {
        String name = new File(getDefaultStoreDirectory(1), str).getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            lastIndexOf = name.length();
        }
        String substring = name.substring(0, lastIndexOf);
        String substring2 = name.substring(lastIndexOf);
        int i = 1;
        while (new File(getDefaultStoreDirectory(1), str).exists()) {
            str = substring + "(" + i + ")" + substring2;
            i++;
        }
        return new File(getDefaultStoreDirectory(1), str).getAbsolutePath();
    }

    public static boolean isSdCardStateFine(long j) {
        return getSdCardFreeSpace(getExternalStorageDirectoryCreateIfNotExists(Environment.DIRECTORY_PICTURES)) > j;
    }

    private static String getDefaultStoreDirectory(int i) {
        String str = Environment.DIRECTORY_PICTURES;
        if (i != 1) {
            if (i != 2) {
                str = Environment.DIRECTORY_DOWNLOADS;
            } else {
                str = Environment.DIRECTORY_MOVIES;
            }
        }
        return getExternalStorageDirectoryCreateIfNotExists(str);
    }

    private static String getExternalStorageDirectoryCreateIfNotExists(String str) {
        File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(str);
        if (externalStoragePublicDirectory.mkdirs() || externalStoragePublicDirectory.isDirectory()) {
            return externalStoragePublicDirectory.getPath();
        }
        String str2 = LOG_TAG;
        Log.d(str2, "Environment " + str + " Error");
        return null;
    }

    private static long getSdCardFreeSpace(String str) {
        if (str == null) {
            Log.e(LOG_TAG, "path == null");
            return -1;
        } else if (new File(str).exists()) {
            return new StatFs(str).getAvailableBytes();
        } else {
            String str2 = LOG_TAG;
            Log.e(str2, "path doesn't exist: '" + str + "'");
            return -1;
        }
    }
}
