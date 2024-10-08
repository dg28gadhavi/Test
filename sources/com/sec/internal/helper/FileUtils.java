package com.sec.internal.helper;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.translate.ContentTypeTranslator;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.regex.Pattern;

public class FileUtils {
    private static final String FILE_PROVIDER_AUTHORITY = "com.sec.internal.ims.rcs.fileprovider";
    private static final String LOG_TAG = "FileUtils";
    private static final int MAX_FILE_NAME_LENGTH = 128;

    public static boolean copyFile(File file, File file2) {
        try {
            copyFileOrThrow(file, file2);
            return true;
        } catch (IOException unused) {
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x004e  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:31:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void copyFileOrThrow(java.io.File r9, java.io.File r10) throws java.io.IOException {
        /*
            boolean r0 = r10.exists()
            if (r0 != 0) goto L_0x0009
            r10.createNewFile()
        L_0x0009:
            r0 = 0
            java.io.FileInputStream r1 = new java.io.FileInputStream     // Catch:{ NullPointerException -> 0x003b, all -> 0x0038 }
            r1.<init>(r9)     // Catch:{ NullPointerException -> 0x003b, all -> 0x0038 }
            java.nio.channels.FileChannel r9 = r1.getChannel()     // Catch:{ NullPointerException -> 0x003b, all -> 0x0038 }
            java.io.FileOutputStream r1 = new java.io.FileOutputStream     // Catch:{ NullPointerException -> 0x0033, all -> 0x002e }
            r1.<init>(r10)     // Catch:{ NullPointerException -> 0x0033, all -> 0x002e }
            java.nio.channels.FileChannel r0 = r1.getChannel()     // Catch:{ NullPointerException -> 0x0033, all -> 0x002e }
            r4 = 0
            long r6 = r9.size()     // Catch:{ NullPointerException -> 0x0033, all -> 0x002e }
            r2 = r0
            r3 = r9
            r2.transferFrom(r3, r4, r6)     // Catch:{ NullPointerException -> 0x0033, all -> 0x002e }
            r9.close()
            r0.close()
            goto L_0x004a
        L_0x002e:
            r10 = move-exception
            r8 = r0
            r0 = r9
            r9 = r8
            goto L_0x004c
        L_0x0033:
            r10 = move-exception
            r8 = r0
            r0 = r9
            r9 = r8
            goto L_0x003d
        L_0x0038:
            r10 = move-exception
            r9 = r0
            goto L_0x004c
        L_0x003b:
            r10 = move-exception
            r9 = r0
        L_0x003d:
            r10.printStackTrace()     // Catch:{ all -> 0x004b }
            if (r0 == 0) goto L_0x0045
            r0.close()
        L_0x0045:
            if (r9 == 0) goto L_0x004a
            r9.close()
        L_0x004a:
            return
        L_0x004b:
            r10 = move-exception
        L_0x004c:
            if (r0 == 0) goto L_0x0051
            r0.close()
        L_0x0051:
            if (r9 == 0) goto L_0x0056
            r9.close()
        L_0x0056:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.FileUtils.copyFileOrThrow(java.io.File, java.io.File):void");
    }

    public static String copyFileToCacheFromUri(Context context, String str, Uri uri) {
        if (!(uri == null || uri.getScheme() == null || !uri.getScheme().equals("content"))) {
            File cacheDir = context.getCacheDir();
            if (cacheDir == null) {
                Log.e(LOG_TAG, "Unable to get Cache Dir!");
                return null;
            }
            try {
                String generateUniqueFilePath = FilePathGenerator.generateUniqueFilePath(cacheDir.getAbsolutePath(), str, 128);
                if (generateUniqueFilePath == null) {
                    Log.e(LOG_TAG, "Create internal path failed!!!");
                    return null;
                } else if (copyFile(context, uri, generateUniqueFilePath) > 0) {
                    return generateUniqueFilePath;
                } else {
                    return null;
                }
            } catch (NullPointerException | SecurityException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static long copyFile(Context context, Uri uri, String str) {
        if (uri == null) {
            return 0;
        }
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                String str2 = LOG_TAG;
                Log.e(str2, "URI open failed!!!! Uri = " + uri);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return 0;
            }
            String str3 = LOG_TAG;
            Log.i(str3, uri + " ==> " + str);
            long copy = Files.copy(inputStream, Paths.get(str, new String[0]), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
            try {
                inputStream.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            return copy;
        } catch (IOException e3) {
            String str4 = LOG_TAG;
            Log.e(str4, "File get from TP failed by " + e3);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            return 0;
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th;
        }
    }

    public static long copyFile(Context context, String str, Uri uri) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        OutputStream outputStream = null;
        try {
            outputStream = context.getContentResolver().openOutputStream(uri, "rw");
            if (outputStream == null) {
                String str2 = LOG_TAG;
                Log.e(str2, "URI open failed!!!! Uri = " + uri);
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return 0;
            }
            String str3 = LOG_TAG;
            Log.i(str3, str + " ==> " + uri);
            long copy = Files.copy(Paths.get(str, new String[0]), outputStream);
            try {
                outputStream.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            return copy;
        } catch (IOException e3) {
            e3.printStackTrace();
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            return 0;
        } catch (Throwable th) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th;
        }
    }

    public static boolean deleteDirectory(Path path) {
        if (path == null) {
            return false;
        }
        try {
            Files.walk(path, new FileVisitOption[0]).sorted(Comparator.reverseOrder()).map(new FileUtils$$ExternalSyntheticLambda0()).forEach(new FileUtils$$ExternalSyntheticLambda1());
            return true;
        } catch (IOException e) {
            String str = LOG_TAG;
            IMSLog.e(str, "deleteDirectory exception : " + e.getMessage());
            return false;
        }
    }

    public static Uri getUriForFile(Context context, File file) {
        if (!file.exists()) {
            return null;
        }
        return FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file);
    }

    public static boolean removeFile(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        File file = new File(str);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static long getSizeFromUri(Context context, Uri uri) {
        AssetFileDescriptor openAssetFileDescriptor;
        long j = -1;
        try {
            openAssetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            if (openAssetFileDescriptor != null) {
                j = openAssetFileDescriptor.getLength();
            }
            if (openAssetFileDescriptor != null) {
                openAssetFileDescriptor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return j;
        throw th;
    }

    public static boolean exists(Context context, Uri uri) {
        return getSizeFromUri(context, uri) != -1;
    }

    public static String getContentType(File file) {
        String name = file.getName();
        String contentTypeFromFileName = getContentTypeFromFileName(name);
        if (TextUtils.isEmpty(contentTypeFromFileName) && isMetaDataExtension(name.substring(name.lastIndexOf(".") + 1))) {
            contentTypeFromFileName = MetaDataUtil.getContentType(file);
        }
        return contentTypeFromFileName == null ? getUnkownContentType() : contentTypeFromFileName;
    }

    public static String getContentType(Context context, String str, Uri uri) {
        String contentTypeFromFileName = getContentTypeFromFileName(str);
        if (TextUtils.isEmpty(contentTypeFromFileName) && isMetaDataExtension(str.substring(str.lastIndexOf(".") + 1))) {
            contentTypeFromFileName = MetaDataUtil.getContentType(context, str, uri);
        }
        return contentTypeFromFileName == null ? getUnkownContentType() : contentTypeFromFileName;
    }

    private static String getUnkownContentType() {
        Log.i(LOG_TAG, "ContentTypeTranslator error: UNKNOWN TYPE");
        return HttpPostBody.CONTENT_TYPE_DEFAULT;
    }

    private static String getContentTypeFromFileName(String str) {
        String substring = str.substring(str.lastIndexOf(".") + 1);
        if (ContentTypeTranslator.isTranslationDefined(substring)) {
            return ContentTypeTranslator.translate(substring);
        }
        return null;
    }

    private static boolean isMetaDataExtension(String str) {
        return "3gp".equalsIgnoreCase(str) || "mp4".equalsIgnoreCase(str) || "heic".equalsIgnoreCase(str);
    }

    public static String deAccent(String str, boolean z) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        String replaceAll = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(Normalizer.normalize(str, Normalizer.Form.NFD)).replaceAll("");
        return z ? Normalizer.normalize(replaceAll, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "_").replaceAll("`", "_").replaceAll("'", "_") : replaceAll;
    }

    public static String getFileNameFromPath(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return str.substring(str.lastIndexOf("/") + 1);
    }
}
