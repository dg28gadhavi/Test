package com.sec.internal.helper;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.helper.translate.FileExtensionTranslator;
import java.io.File;
import java.io.IOException;

public class FilePathGenerator {
    protected static final int FILE_RENAME_HASHVALUE_LEN = 5;
    private static final String LOG_TAG = "FilePathGenerator";
    protected static final String sReceivedFilesDir = (File.separatorChar + "Samsung Messages");
    protected static final String sThumbnailDir = (File.separatorChar + ".thumbnail");

    public static String generateUniqueFilePath(String str, String str2, int i) {
        String str3;
        String str4;
        if (TextUtils.isEmpty(str2)) {
            return null;
        }
        int lastIndexOf = str2.lastIndexOf(".");
        String substring = lastIndexOf >= 0 ? str2.substring(0, lastIndexOf) : str2;
        if (lastIndexOf >= 0) {
            str3 = "." + str2.substring(lastIndexOf + 1);
        } else {
            str3 = "";
        }
        if (substring.length() < i) {
            str4 = generateUniqueFileNameWithCountPostfix(str, substring, str3);
        } else {
            str4 = substring.substring(0, i - 5) + StringGenerator.generateString(5, 5) + str3;
        }
        String path = new File(str, str4).getPath();
        Log.d(LOG_TAG, "generateUniqueFilePath: " + path + ", fileFullName: " + str2 + ", fileNameWithoutExtension: " + substring + ", extensionWithDot: " + str3);
        return path;
    }

    public static String generateUniqueThumbnailPath(Context context, String str, String str2, String str3, int i) {
        String str4 = getIncomingFileDestinationDir(context, str3) + sThumbnailDir;
        File file = new File(str4);
        if (!file.exists() && !file.mkdirs()) {
            Log.e(LOG_TAG, "create Unique Thumbnail folder failure");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append(".");
        sb.append(FileExtensionTranslator.isTranslationDefined(str2) ? FileExtensionTranslator.translate(str2) : "jpg");
        sb.append(".tmp");
        sb.append(StringGenerator.generateString(3, 3));
        return generateUniqueFilePath(str4, sb.toString(), i);
    }

    public static String getIncomingFileDestinationDir(Context context, String str) {
        String path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath();
        if (TextUtils.isEmpty(str)) {
            return path;
        }
        File file = new File(str);
        if (file.exists() || file.mkdirs()) {
            createNomediaFile(str);
            return str;
        }
        Log.e(LOG_TAG, "can not create dir. Use default download directory.");
        return path;
    }

    public static String renameThumbnail(String str, String str2, String str3, int i) {
        if (str == null) {
            Log.d(LOG_TAG, "mThumbnailPath is null");
            return null;
        }
        File file = new File(str);
        StringBuilder sb = new StringBuilder();
        sb.append(".");
        sb.append(FileExtensionTranslator.isTranslationDefined(str2) ? FileExtensionTranslator.translate(str2) : "jpg");
        String sb2 = sb.toString();
        String parent = file.getParent();
        String generateUniqueFilePath = generateUniqueFilePath(parent, str3 + sb2, i);
        File file2 = new File(generateUniqueFilePath);
        String str4 = LOG_TAG;
        Log.d(str4, "old thumbnail path: " + str + ", new thumbnail path: " + generateUniqueFilePath);
        if (file.renameTo(file2)) {
            return generateUniqueFilePath;
        }
        Log.e(str4, "Thumbnail rename failure");
        return null;
    }

    public static String getFileDownloadPath(Context context, boolean z) {
        if (context == null) {
            return null;
        }
        File externalFilesDir = context.getExternalFilesDir((String) null);
        if (externalFilesDir == null) {
            Log.e(LOG_TAG, "Failed to get external files directory.");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(externalFilesDir.getAbsolutePath());
        sb.append(z ? "" : sReceivedFilesDir);
        return sb.toString();
    }

    private static String generateUniqueFileNameWithCountPostfix(String str, String str2, String str3) {
        StringBuilder sb = new StringBuilder();
        String str4 = str2 + str3;
        int i = 1;
        while (new File(str, str4).exists()) {
            sb.setLength(0);
            sb.append(str2);
            sb.append('(');
            sb.append(i);
            sb.append(')');
            sb.append(str3);
            i++;
            str4 = sb.toString();
        }
        return str4;
    }

    private static void createNomediaFile(String str) {
        File file = new File(str + File.separatorChar + ".nomedia");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    String str2 = LOG_TAG;
                    Log.e(str2, "makeDirectoryToCopyImage, created failed in: " + str);
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "makeDirectoryToCopyImage, cannot create .nomedia file");
                e.printStackTrace();
            }
        }
    }
}
