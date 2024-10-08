package com.sec.internal.ims.cmstore.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ReflectionUtils;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.FilePathGenerator;
import com.sec.internal.helper.HashManager;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.StringGenerator;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.Link;
import com.sec.internal.omanetapi.nms.data.NmsEventList;
import com.sec.internal.omanetapi.nms.data.PayloadPartInfo;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

public class Util {
    protected static final int FILE_RENAME_HASHVALUE_LEN = 5;
    private static final String HTTP = "http:";
    private static final String HTTPS = "https:";
    private static final String LOG_TAG = Util.class.getSimpleName();
    protected static final int MAX_FILE_NAME_LEN = 50;
    private static final long MAX_NOT_SYNC_TIME = 259200000;
    protected static final String mMMSPartsDir = (File.separatorChar + "MMS_files");
    protected static final String mRCSFilesDir = (File.separatorChar + "RCS_files");

    protected static String getIncomingFileDestinationDir(Context context, boolean z) throws IOException {
        return getIncomingFileDestinationDir(context, z, 0);
    }

    protected static String getIncomingFileDestinationDir(Context context, boolean z, int i) throws IOException {
        String str;
        String str2;
        if (z) {
            str = mMMSPartsDir;
            if (i == 1) {
                str = str + "slot2";
            }
        } else {
            str = mRCSFilesDir;
            if (i == 1) {
                str = str + "slot2";
            }
        }
        if (context == null) {
            return null;
        }
        File externalFilesDir = context.getExternalFilesDir((String) null);
        String absolutePath = externalFilesDir != null ? externalFilesDir.getAbsolutePath() : null;
        if (absolutePath == null) {
            return null;
        }
        File file = new File(absolutePath + str);
        if (file.exists()) {
            str2 = absolutePath + str;
        } else if (file.mkdir()) {
            str2 = absolutePath + str;
        } else {
            Log.e(LOG_TAG, "can not create dir");
            return null;
        }
        if (file.exists()) {
            File file2 = new File(str2 + File.separatorChar + ".nomedia");
            if (!file2.exists()) {
                try {
                    boolean createNewFile = file2.createNewFile();
                    String str3 = LOG_TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("create .nomedia file in ");
                    sb.append(str2);
                    sb.append(" : ");
                    sb.append(createNewFile ? "successful" : "failed");
                    Log.d(str3, sb.toString());
                } catch (IOException e) {
                    Log.e(LOG_TAG, "makeDirectoryToCopyImage, cannot create .nomedia file");
                    e.printStackTrace();
                }
            }
        }
        return str2;
    }

    public static String generateUniqueFilePath(Context context, String str, boolean z) throws IOException {
        return generateUniqueFilePath(context, str, z, 0);
    }

    public static String generateUniqueFilePath(Context context, String str, boolean z, int i) throws IOException {
        if (!TextUtils.isEmpty(str)) {
            return FilePathGenerator.generateUniqueFilePath(getIncomingFileDestinationDir(context, z, i), str, 50);
        }
        throw new IOException();
    }

    public static String getRandomFileName(String str) {
        if (TextUtils.isEmpty(str)) {
            return StringGenerator.generateString(5, 5);
        }
        return StringGenerator.generateString(5, 5) + "." + str;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0048, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004a, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r3.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x004e, code lost:
        if (r1 == null) goto L_0x0072;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0054, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r3.printStackTrace();
        android.util.Log.e(LOG_TAG, "saveFileToAppUri: Security Exception");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0060, code lost:
        if (r1 != null) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0063, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0064, code lost:
        r3.printStackTrace();
        android.util.Log.e(LOG_TAG, "saveInputStreamtoAppUri: Null Pointer Exception");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006f, code lost:
        if (r1 != null) goto L_0x0050;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0073, code lost:
        if (r1 != null) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0075, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0078, code lost:
        throw r3;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x003f */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0054 A[ExcHandler: SecurityException (r3v3 'e' java.lang.SecurityException A[CUSTOM_DECLARE]), PHI: r1 
      PHI: (r1v2 android.os.ParcelFileDescriptor) = (r1v0 android.os.ParcelFileDescriptor), (r1v6 android.os.ParcelFileDescriptor), (r1v6 android.os.ParcelFileDescriptor), (r1v6 android.os.ParcelFileDescriptor), (r1v6 android.os.ParcelFileDescriptor), (r1v6 android.os.ParcelFileDescriptor), (r1v6 android.os.ParcelFileDescriptor), (r1v6 android.os.ParcelFileDescriptor) binds: [B:1:0x0002, B:8:0x0021, B:23:0x003f, B:24:?, B:9:?, B:20:0x003b, B:12:0x002d, B:13:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:1:0x0002] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0063 A[Catch:{ NullPointerException -> 0x0063, SecurityException -> 0x0054, IOException -> 0x004a, all -> 0x0048 }, ExcHandler: NullPointerException (r3v1 'e' java.lang.NullPointerException A[CUSTOM_DECLARE, Catch:{  }]), PHI: r1 
      PHI: (r1v1 android.os.ParcelFileDescriptor) = (r1v0 android.os.ParcelFileDescriptor), (r1v6 android.os.ParcelFileDescriptor), (r1v6 android.os.ParcelFileDescriptor), (r1v6 android.os.ParcelFileDescriptor), (r1v6 android.os.ParcelFileDescriptor), (r1v6 android.os.ParcelFileDescriptor), (r1v6 android.os.ParcelFileDescriptor), (r1v6 android.os.ParcelFileDescriptor) binds: [B:1:0x0002, B:8:0x0021, B:23:0x003f, B:24:?, B:9:?, B:20:0x003b, B:12:0x002d, B:13:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:1:0x0002] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean saveFileToAppUri(android.content.Context r3, byte[] r4, java.lang.String r5) throws java.io.IOException {
        /*
            r0 = 0
            r1 = 0
            android.content.ContentResolver r3 = r3.getContentResolver()     // Catch:{ NullPointerException -> 0x0063, SecurityException -> 0x0054, IOException -> 0x004a }
            android.net.Uri r5 = android.net.Uri.parse(r5)     // Catch:{ NullPointerException -> 0x0063, SecurityException -> 0x0054, IOException -> 0x004a }
            java.lang.String r2 = "rwt"
            android.os.ParcelFileDescriptor r1 = r3.openFileDescriptor(r5, r2)     // Catch:{ NullPointerException -> 0x0063, SecurityException -> 0x0054, IOException -> 0x004a }
            if (r1 != 0) goto L_0x0021
            java.lang.String r3 = LOG_TAG     // Catch:{ NullPointerException -> 0x0063, SecurityException -> 0x0054, IOException -> 0x004a }
            java.lang.String r4 = "saveFileToAppUri fd is null"
            android.util.Log.e(r3, r4)     // Catch:{ NullPointerException -> 0x0063, SecurityException -> 0x0054, IOException -> 0x004a }
            if (r1 == 0) goto L_0x0020
            r1.close()
        L_0x0020:
            return r0
        L_0x0021:
            java.io.FileOutputStream r3 = new java.io.FileOutputStream     // Catch:{ IOException -> 0x003f, NullPointerException -> 0x0063, SecurityException -> 0x0054 }
            java.io.FileDescriptor r5 = r1.getFileDescriptor()     // Catch:{ IOException -> 0x003f, NullPointerException -> 0x0063, SecurityException -> 0x0054 }
            r3.<init>(r5)     // Catch:{ IOException -> 0x003f, NullPointerException -> 0x0063, SecurityException -> 0x0054 }
            r3.write(r4)     // Catch:{ all -> 0x0035 }
            r3.close()     // Catch:{ IOException -> 0x003f, NullPointerException -> 0x0063, SecurityException -> 0x0054 }
            r1.close()
            r3 = 1
            return r3
        L_0x0035:
            r4 = move-exception
            r3.close()     // Catch:{ all -> 0x003a }
            goto L_0x003e
        L_0x003a:
            r3 = move-exception
            r4.addSuppressed(r3)     // Catch:{ IOException -> 0x003f, NullPointerException -> 0x0063, SecurityException -> 0x0054 }
        L_0x003e:
            throw r4     // Catch:{ IOException -> 0x003f, NullPointerException -> 0x0063, SecurityException -> 0x0054 }
        L_0x003f:
            java.lang.String r3 = LOG_TAG     // Catch:{ NullPointerException -> 0x0063, SecurityException -> 0x0054, IOException -> 0x004a }
            java.lang.String r4 = "saveFileToAppUri: Error in getting Output Steam"
            android.util.Log.e(r3, r4)     // Catch:{ NullPointerException -> 0x0063, SecurityException -> 0x0054, IOException -> 0x004a }
            goto L_0x0050
        L_0x0048:
            r3 = move-exception
            goto L_0x0073
        L_0x004a:
            r3 = move-exception
            r3.printStackTrace()     // Catch:{ all -> 0x0048 }
            if (r1 == 0) goto L_0x0072
        L_0x0050:
            r1.close()
            goto L_0x0072
        L_0x0054:
            r3 = move-exception
            r3.printStackTrace()     // Catch:{ all -> 0x0048 }
            java.lang.String r3 = LOG_TAG     // Catch:{ all -> 0x0048 }
            java.lang.String r4 = "saveFileToAppUri: Security Exception"
            android.util.Log.e(r3, r4)     // Catch:{ all -> 0x0048 }
            if (r1 == 0) goto L_0x0072
            goto L_0x0050
        L_0x0063:
            r3 = move-exception
            r3.printStackTrace()     // Catch:{ all -> 0x0048 }
            java.lang.String r3 = LOG_TAG     // Catch:{ all -> 0x0048 }
            java.lang.String r4 = "saveInputStreamtoAppUri: Null Pointer Exception"
            android.util.Log.e(r3, r4)     // Catch:{ all -> 0x0048 }
            if (r1 == 0) goto L_0x0072
            goto L_0x0050
        L_0x0072:
            return r0
        L_0x0073:
            if (r1 == 0) goto L_0x0078
            r1.close()
        L_0x0078:
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.Util.saveFileToAppUri(android.content.Context, byte[], java.lang.String):boolean");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006f, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0071, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        r5.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0075, code lost:
        if (r2 != null) goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0078, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0079, code lost:
        r5.printStackTrace();
        android.util.Log.e(LOG_TAG, "saveInputStreamtoAppUri: Security Exception");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0084, code lost:
        if (r2 != null) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0087, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0088, code lost:
        r5.printStackTrace();
        android.util.Log.e(LOG_TAG, "saveInputStreamtoAppUri: Null Pointer Exception");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0093, code lost:
        if (r2 != null) goto L_0x006b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0097, code lost:
        if (r2 != null) goto L_0x0099;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0099, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x009c, code lost:
        throw r5;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0063 */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0078 A[Catch:{ NullPointerException -> 0x0087, SecurityException -> 0x0078, IOException -> 0x0071, all -> 0x006f }, ExcHandler: SecurityException (r5v3 'e' java.lang.SecurityException A[CUSTOM_DECLARE, Catch:{  }]), PHI: r0 r2 
      PHI: (r0v3 long) = (r0v0 long), (r0v0 long), (r0v6 long), (r0v6 long), (r0v0 long), (r0v0 long), (r0v7 long), (r0v7 long) binds: [B:1:0x0003, B:7:0x0024, B:25:0x0063, B:26:?, B:8:?, B:22:0x005f, B:16:0x0055, B:17:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r2v2 android.os.ParcelFileDescriptor) = (r2v0 android.os.ParcelFileDescriptor), (r2v6 android.os.ParcelFileDescriptor), (r2v6 android.os.ParcelFileDescriptor), (r2v6 android.os.ParcelFileDescriptor), (r2v6 android.os.ParcelFileDescriptor), (r2v6 android.os.ParcelFileDescriptor), (r2v6 android.os.ParcelFileDescriptor), (r2v6 android.os.ParcelFileDescriptor) binds: [B:1:0x0003, B:7:0x0024, B:25:0x0063, B:26:?, B:8:?, B:22:0x005f, B:16:0x0055, B:17:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:1:0x0003] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0087 A[Catch:{ NullPointerException -> 0x0087, SecurityException -> 0x0078, IOException -> 0x0071, all -> 0x006f }, ExcHandler: NullPointerException (r5v1 'e' java.lang.NullPointerException A[CUSTOM_DECLARE, Catch:{  }]), PHI: r0 r2 
      PHI: (r0v2 long) = (r0v0 long), (r0v0 long), (r0v6 long), (r0v6 long), (r0v0 long), (r0v0 long), (r0v7 long), (r0v7 long) binds: [B:1:0x0003, B:7:0x0024, B:25:0x0063, B:26:?, B:8:?, B:22:0x005f, B:16:0x0055, B:17:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r2v1 android.os.ParcelFileDescriptor) = (r2v0 android.os.ParcelFileDescriptor), (r2v6 android.os.ParcelFileDescriptor), (r2v6 android.os.ParcelFileDescriptor), (r2v6 android.os.ParcelFileDescriptor), (r2v6 android.os.ParcelFileDescriptor), (r2v6 android.os.ParcelFileDescriptor), (r2v6 android.os.ParcelFileDescriptor), (r2v6 android.os.ParcelFileDescriptor) binds: [B:1:0x0003, B:7:0x0024, B:25:0x0063, B:26:?, B:8:?, B:22:0x005f, B:16:0x0055, B:17:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:1:0x0003] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long saveInputStreamtoAppUri(android.content.Context r5, java.io.InputStream r6, java.lang.String r7) throws java.io.IOException {
        /*
            r0 = 0
            r2 = 0
            android.content.ContentResolver r5 = r5.getContentResolver()     // Catch:{ NullPointerException -> 0x0087, SecurityException -> 0x0078, IOException -> 0x0071 }
            android.net.Uri r7 = android.net.Uri.parse(r7)     // Catch:{ NullPointerException -> 0x0087, SecurityException -> 0x0078, IOException -> 0x0071 }
            java.lang.String r3 = "rwt"
            android.os.ParcelFileDescriptor r2 = r5.openFileDescriptor(r7, r3)     // Catch:{ NullPointerException -> 0x0087, SecurityException -> 0x0078, IOException -> 0x0071 }
            if (r2 != 0) goto L_0x0024
            java.lang.String r5 = LOG_TAG     // Catch:{ NullPointerException -> 0x0087, SecurityException -> 0x0078, IOException -> 0x0071 }
            java.lang.String r6 = "saveInputStreamtoAppUriFileToAppUri fd is null"
            android.util.Log.e(r5, r6)     // Catch:{ NullPointerException -> 0x0087, SecurityException -> 0x0078, IOException -> 0x0071 }
            if (r2 == 0) goto L_0x0021
            r2.close()
        L_0x0021:
            r5 = -1
            return r5
        L_0x0024:
            java.io.FileOutputStream r5 = new java.io.FileOutputStream     // Catch:{ IOException -> 0x0063, NullPointerException -> 0x0087, SecurityException -> 0x0078 }
            java.io.FileDescriptor r7 = r2.getFileDescriptor()     // Catch:{ IOException -> 0x0063, NullPointerException -> 0x0087, SecurityException -> 0x0078 }
            r5.<init>(r7)     // Catch:{ IOException -> 0x0063, NullPointerException -> 0x0087, SecurityException -> 0x0078 }
            r7 = 8192(0x2000, float:1.14794E-41)
            byte[] r7 = new byte[r7]     // Catch:{ all -> 0x0059 }
        L_0x0031:
            int r3 = r6.read(r7)     // Catch:{ all -> 0x0059 }
            if (r3 <= 0) goto L_0x003e
            r4 = 0
            r5.write(r7, r4, r3)     // Catch:{ all -> 0x0059 }
            long r3 = (long) r3     // Catch:{ all -> 0x0059 }
            long r0 = r0 + r3
            goto L_0x0031
        L_0x003e:
            java.lang.String r6 = LOG_TAG     // Catch:{ all -> 0x0059 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x0059 }
            r7.<init>()     // Catch:{ all -> 0x0059 }
            java.lang.String r3 = "saveInputStreamtoAppUri() file written "
            r7.append(r3)     // Catch:{ all -> 0x0059 }
            r7.append(r0)     // Catch:{ all -> 0x0059 }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x0059 }
            android.util.Log.i(r6, r7)     // Catch:{ all -> 0x0059 }
            r5.close()     // Catch:{ IOException -> 0x0063, NullPointerException -> 0x0087, SecurityException -> 0x0078 }
            goto L_0x006b
        L_0x0059:
            r6 = move-exception
            r5.close()     // Catch:{ all -> 0x005e }
            goto L_0x0062
        L_0x005e:
            r5 = move-exception
            r6.addSuppressed(r5)     // Catch:{ IOException -> 0x0063, NullPointerException -> 0x0087, SecurityException -> 0x0078 }
        L_0x0062:
            throw r6     // Catch:{ IOException -> 0x0063, NullPointerException -> 0x0087, SecurityException -> 0x0078 }
        L_0x0063:
            java.lang.String r5 = LOG_TAG     // Catch:{ NullPointerException -> 0x0087, SecurityException -> 0x0078, IOException -> 0x0071 }
            java.lang.String r6 = "saveInputSteamtoAppUri: error in getting OutputSteam"
            android.util.Log.e(r5, r6)     // Catch:{ NullPointerException -> 0x0087, SecurityException -> 0x0078, IOException -> 0x0071 }
        L_0x006b:
            r2.close()
            goto L_0x0096
        L_0x006f:
            r5 = move-exception
            goto L_0x0097
        L_0x0071:
            r5 = move-exception
            r5.printStackTrace()     // Catch:{ all -> 0x006f }
            if (r2 == 0) goto L_0x0096
            goto L_0x006b
        L_0x0078:
            r5 = move-exception
            r5.printStackTrace()     // Catch:{ all -> 0x006f }
            java.lang.String r5 = LOG_TAG     // Catch:{ all -> 0x006f }
            java.lang.String r6 = "saveInputStreamtoAppUri: Security Exception"
            android.util.Log.e(r5, r6)     // Catch:{ all -> 0x006f }
            if (r2 == 0) goto L_0x0096
            goto L_0x006b
        L_0x0087:
            r5 = move-exception
            r5.printStackTrace()     // Catch:{ all -> 0x006f }
            java.lang.String r5 = LOG_TAG     // Catch:{ all -> 0x006f }
            java.lang.String r6 = "saveInputStreamtoAppUri: Null Pointer Exception"
            android.util.Log.e(r5, r6)     // Catch:{ all -> 0x006f }
            if (r2 == 0) goto L_0x0096
            goto L_0x006b
        L_0x0096:
            return r0
        L_0x0097:
            if (r2 == 0) goto L_0x009c
            r2.close()
        L_0x009c:
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.Util.saveInputStreamtoAppUri(android.content.Context, java.io.InputStream, java.lang.String):long");
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0017  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void saveFiletoPath(byte[] r2, java.lang.String r3) throws java.io.IOException {
        /*
            if (r2 == 0) goto L_0x001b
            if (r3 == 0) goto L_0x001b
            r0 = 0
            java.io.FileOutputStream r1 = new java.io.FileOutputStream     // Catch:{ all -> 0x0014 }
            r1.<init>(r3)     // Catch:{ all -> 0x0014 }
            r1.write(r2)     // Catch:{ all -> 0x0011 }
            r1.close()
            goto L_0x001b
        L_0x0011:
            r2 = move-exception
            r0 = r1
            goto L_0x0015
        L_0x0014:
            r2 = move-exception
        L_0x0015:
            if (r0 == 0) goto L_0x001a
            r0.close()
        L_0x001a:
            throw r2
        L_0x001b:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.Util.saveFiletoPath(byte[], java.lang.String):void");
    }

    public static String convertStreamToString(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byte[] bArr = new byte[2000];
            while (true) {
                int read = inputStream.read(bArr);
                if (read <= 0) {
                    return new String(byteArrayOutputStream.toByteArray());
                }
                byteArrayOutputStream.write(bArr, 0, read);
            }
        } finally {
            byteArrayOutputStream.close();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0027  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long saveInputStreamtoPath(java.io.InputStream r6, java.lang.String r7) throws java.io.IOException {
        /*
            r0 = 0
            if (r6 == 0) goto L_0x002b
            if (r7 == 0) goto L_0x002b
            r2 = 0
            java.io.FileOutputStream r3 = new java.io.FileOutputStream     // Catch:{ all -> 0x0024 }
            r3.<init>(r7)     // Catch:{ all -> 0x0024 }
            r7 = 8192(0x2000, float:1.14794E-41)
            byte[] r7 = new byte[r7]     // Catch:{ all -> 0x0021 }
        L_0x0010:
            int r2 = r6.read(r7)     // Catch:{ all -> 0x0021 }
            if (r2 <= 0) goto L_0x001d
            r4 = 0
            r3.write(r7, r4, r2)     // Catch:{ all -> 0x0021 }
            long r4 = (long) r2
            long r0 = r0 + r4
            goto L_0x0010
        L_0x001d:
            r3.close()
            goto L_0x002b
        L_0x0021:
            r6 = move-exception
            r2 = r3
            goto L_0x0025
        L_0x0024:
            r6 = move-exception
        L_0x0025:
            if (r2 == 0) goto L_0x002a
            r2.close()
        L_0x002a:
            throw r6
        L_0x002b:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.Util.saveInputStreamtoPath(java.io.InputStream, java.lang.String):long");
    }

    public static long saveMimeBodyToPath(MimeMultipart mimeMultipart, String str) throws IOException, MessagingException {
        FileOutputStream fileOutputStream = new FileOutputStream(new File(str));
        try {
            mimeMultipart.writeTo(fileOutputStream);
        } catch (Throwable unused) {
        }
        fileOutputStream.close();
        return (long) mimeMultipart.getCount();
    }

    public static String decodeUrlFromServer(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            str = URLDecoder.decode(str, "UTF-8").replace(' ', '+');
        } catch (UnsupportedEncodingException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "decodeUrlFromServer: " + e.getMessage());
            e.printStackTrace();
        }
        String str3 = LOG_TAG;
        Log.d(str3, "decodeUrlFromServer to: " + str);
        return str;
    }

    public static String getFileNamefromContentType(String str) {
        String[] split;
        String[] split2;
        String str2 = "download";
        if (TextUtils.isEmpty(str) || (split = str.split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR)) == null || split.length < 2) {
            return str2;
        }
        String str3 = split[1];
        if (str3.contains(";") && (split2 = str3.split(";")) != null && split2.length > 1) {
            str3 = split2[0];
        }
        if (str3.contains(CmcConstants.E_NUM_STR_QUOTE)) {
            String[] split3 = str3.split(CmcConstants.E_NUM_STR_QUOTE);
            if (split3 != null && split3.length > 1) {
                str2 = split3[1];
            }
            str3 = str2;
        }
        String str4 = LOG_TAG;
        Log.d(str4, "getFileNamefromContentType: " + str + " to: " + str3);
        return str3;
    }

    public static boolean isPayloadExpired(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        try {
            String queryParameter = Uri.parse(str).getQueryParameter("e");
            if (TextUtils.isEmpty(queryParameter)) {
                return false;
            }
            long parseTimeStamp = parseTimeStamp(queryParameter);
            long currentTimeMillis = System.currentTimeMillis();
            String str2 = LOG_TAG;
            Log.i(str2, "isPayloadExpired payloadExpTime: " + parseTimeStamp + ", currTime: " + currentTimeMillis + ", expired: " + queryParameter);
            if (parseTimeStamp == -1 || parseTimeStamp > currentTimeMillis) {
                return false;
            }
            return true;
        } catch (UnsupportedOperationException e) {
            String str3 = LOG_TAG;
            Log.e(str3, "isPayloadExpired UnsupportedOperationException: " + e.getMessage());
            return false;
        }
    }

    public static String getFileNamefromContentDisposition(String str) {
        String[] split;
        String str2 = "";
        if (TextUtils.isEmpty(str) || (split = str.split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR)) == null || split.length < 2) {
            return str2;
        }
        String str3 = split[1];
        if (str3.contains(CmcConstants.E_NUM_STR_QUOTE)) {
            String[] split2 = str3.split(CmcConstants.E_NUM_STR_QUOTE);
            if (split2 != null && split2.length > 1) {
                str2 = split2[1];
            }
            str3 = str2;
        }
        String str4 = LOG_TAG;
        Log.d(str4, "getFileNamefromContentDisposition: " + str + " to: " + str3);
        return str3;
    }

    public static String findParaStr(String str, String str2) {
        String str3;
        Log.d(LOG_TAG, "findParaStr: " + str);
        if (!TextUtils.isEmpty(str) && !TextUtils.isEmpty(str2)) {
            String[] split = str.split(";");
            int length = split.length;
            int i = 0;
            while (i < length) {
                String[] split2 = split[i].split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                if (split2.length <= 1 || !split2[0].trim().equalsIgnoreCase(str2)) {
                    i++;
                } else {
                    String replaceAll = split2[1].replaceAll(CmcConstants.E_NUM_STR_QUOTE, "");
                    if (str.lastIndexOf(".") < 0) {
                        Log.d(LOG_TAG, "no extension, to: " + replaceAll);
                        return replaceAll;
                    }
                    String substring = str.substring(str.lastIndexOf(".") + 1);
                    if (substring.indexOf(CmcConstants.E_NUM_STR_QUOTE) > 0) {
                        str3 = substring.substring(0, substring.indexOf(CmcConstants.E_NUM_STR_QUOTE));
                    } else {
                        str3 = substring.substring(0);
                    }
                    if (!replaceAll.endsWith(str3)) {
                        replaceAll = "file." + str3;
                    }
                    Log.d(LOG_TAG, "findParaStr, value to: " + replaceAll);
                    return replaceAll;
                }
            }
            Log.d(LOG_TAG, "findParaStr, to: " + "");
        }
        return "";
    }

    public static String generateLocationWithEncoding(PayloadPartInfo payloadPartInfo) {
        return encodedToIso8859(generateLocation(payloadPartInfo));
    }

    public static String generateLocation(PayloadPartInfo payloadPartInfo) {
        String fileNamefromContentType;
        String str = LOG_TAG;
        Log.d(str, "contentType=" + payloadPartInfo.contentType + ", contentDisposition=" + payloadPartInfo.contentDisposition + ", contentLocation=" + payloadPartInfo.contentLocation + ", contentId=" + payloadPartInfo.contentId);
        if (!TextUtils.isEmpty(payloadPartInfo.contentType) && (fileNamefromContentType = getFileNamefromContentType(payloadPartInfo.contentType)) != null && !fileNamefromContentType.equals("download") && !fileNamefromContentType.equalsIgnoreCase("UTF-8")) {
            return fileNamefromContentType;
        }
        if (!TextUtils.isEmpty(payloadPartInfo.contentDisposition)) {
            String findParaStr = findParaStr(payloadPartInfo.contentDisposition, "filename");
            if (!TextUtils.isEmpty(findParaStr)) {
                return findParaStr;
            }
        }
        URI uri = payloadPartInfo.contentLocation;
        if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
            return payloadPartInfo.contentLocation.getPath();
        }
        if (!TextUtils.isEmpty(payloadPartInfo.contentId)) {
            return payloadPartInfo.contentId;
        }
        return getRandomFileName((String) null);
    }

    public static String encodedToIso8859(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        return new String(str.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
    }

    public static String generateUniqueFileName(PayloadPartInfo payloadPartInfo) {
        String str;
        String generateLocation = generateLocation(payloadPartInfo);
        try {
            generateLocation = URLDecoder.decode(generateLocation, "UTF-8");
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            Log.e(LOG_TAG, "generateUniqueFileName - " + e.getMessage());
        }
        String[] split = generateLocation.split("\\.");
        if (split == null || split.length < 2) {
            str = "";
        } else {
            str = split[split.length - 1];
            generateLocation = generateLocation.substring(0, (generateLocation.length() - str.length()) - 1);
        }
        String str2 = generateLocation + "_" + StringGenerator.generateString(5, 5);
        if (!TextUtils.isEmpty(str)) {
            str2 = str2 + "." + str;
        }
        Log.d(LOG_TAG, "generateUniqueFileName() final originalFileName: " + str2 + "extension: " + str);
        return str2;
    }

    public static ImsUri getNormalizedTelUri(String str, String str2) {
        String str3 = LOG_TAG;
        Log.i(str3, "getNormalizedTelUri: " + IMSLog.checker(str));
        if (str != null && !str.contains("#") && !str.contains("*") && !str.contains(",") && !str.contains("N")) {
            return UriUtil.parseNumber(str, str2);
        }
        return null;
    }

    public static String getPhoneNum(String str) {
        if (str == null) {
            return null;
        }
        int indexOf = str.indexOf(58);
        if (indexOf > 0) {
            str = str.substring(indexOf + 1, str.length());
        }
        int indexOf2 = str.indexOf(64);
        return indexOf2 > 0 ? str.substring(0, indexOf2) : str;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006b, code lost:
        r4 = com.sec.internal.helper.UriUtil.parseNumber(r0, r4);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getTelUri(java.lang.String r3, java.lang.String r4) {
        /*
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "getTelUri: "
            r1.append(r2)
            java.lang.String r2 = com.sec.internal.log.IMSLog.checker(r3)
            r1.append(r2)
            java.lang.String r2 = " countryCode:"
            r1.append(r2)
            r1.append(r4)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            r1 = 0
            if (r3 != 0) goto L_0x0026
            return r1
        L_0x0026:
            java.lang.String r2 = "tel:"
            boolean r2 = r3.startsWith(r2)
            if (r2 == 0) goto L_0x0030
            return r3
        L_0x0030:
            java.lang.String r2 = "#"
            boolean r2 = r3.contains(r2)
            if (r2 != 0) goto L_0x0077
            java.lang.String r2 = "*"
            boolean r2 = r3.contains(r2)
            if (r2 != 0) goto L_0x0077
            java.lang.String r2 = ","
            boolean r2 = r3.contains(r2)
            if (r2 == 0) goto L_0x0049
            goto L_0x0077
        L_0x0049:
            com.sec.ims.util.ImsUri r1 = com.sec.ims.util.ImsUri.parse(r3)
            if (r1 != 0) goto L_0x0060
            com.sec.ims.util.ImsUri r4 = getNormalizedTelUri(r3, r4)
            if (r4 != 0) goto L_0x005b
            java.lang.String r4 = "getTelUri: parsing fail, return original number"
            android.util.Log.d(r0, r4)
            return r3
        L_0x005b:
            java.lang.String r3 = r4.toString()
            return r3
        L_0x0060:
            java.lang.String r0 = com.sec.internal.helper.UriUtil.getMsisdnNumber(r1)
            boolean r1 = android.text.TextUtils.isEmpty(r0)
            if (r1 == 0) goto L_0x006b
            return r3
        L_0x006b:
            com.sec.ims.util.ImsUri r4 = com.sec.internal.helper.UriUtil.parseNumber(r0, r4)
            if (r4 != 0) goto L_0x0072
            goto L_0x0076
        L_0x0072:
            java.lang.String r3 = r4.toString()
        L_0x0076:
            return r3
        L_0x0077:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.Util.getTelUri(java.lang.String, java.lang.String):java.lang.String");
    }

    public static String getMsisdn(String str, String str2) {
        String str3 = LOG_TAG;
        Log.d(str3, "getMsisdn: " + IMSLog.checker(str));
        if (str == null || str.contains("#") || str.contains("*") || str.contains(",") || str.contains("N")) {
            return null;
        }
        ImsUri parse = ImsUri.parse(str);
        if (parse != null) {
            return parse.getMsisdn();
        }
        ImsUri normalizedTelUri = getNormalizedTelUri(str, str2);
        if (normalizedTelUri == null) {
            return str;
        }
        return normalizedTelUri.getMsisdn();
    }

    public static void deleteFilesinMmsBufferFolder(int i) {
        File file;
        String[] list;
        String str = LOG_TAG;
        Log.i(str, "deleteFilesinMmsBufferFolder sim_slot: " + i);
        if (i == 1) {
            file = new File(mMMSPartsDir + "slot2");
        } else {
            file = new File(mMMSPartsDir);
        }
        if (file.exists() && file.isDirectory() && (list = file.list()) != null) {
            for (int i2 = 0; i2 < list.length; i2++) {
                try {
                    new File(file, list[i2]).delete();
                } catch (SecurityException e) {
                    String str2 = LOG_TAG;
                    Log.e(str2, "deleteFilesinMmsBufferFolder: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getLineTelUriFromObjUrl(String str) {
        String decodeUrlFromServer = decodeUrlFromServer(str);
        String str2 = LOG_TAG;
        Log.d(str2, "getLineTelUriFromObjUrl: " + IMSLog.checker(decodeUrlFromServer));
        if (decodeUrlFromServer == null) {
            return str;
        }
        String[] split = decodeUrlFromServer.split("/");
        if (split == null) {
            return null;
        }
        for (int i = 0; i < split.length; i++) {
            if (split[i].contains("tel:+")) {
                return split[i];
            }
        }
        return null;
    }

    public static String extractObjIdFromResUrl(String str) {
        String decodeUrlFromServer = decodeUrlFromServer(str);
        if (decodeUrlFromServer == null) {
            return str;
        }
        String substring = decodeUrlFromServer.substring(decodeUrlFromServer.lastIndexOf(47) + 1);
        String str2 = LOG_TAG;
        Log.d(str2, "extractObjIdFromResUrl: " + IMSLog.checker(substring));
        return substring;
    }

    public static String generateHash() {
        try {
            return HashManager.generateHash(new Timestamp(new Date().getTime()).toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String replaceUrlPrefix(String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2) || str.startsWith(str2)) {
            return str;
        }
        if (str.startsWith(HTTPS)) {
            return str.replaceFirst(HTTPS, str2);
        }
        return str.startsWith(HTTP) ? str.replaceFirst(HTTP, str2) : str;
    }

    public static String replaceUrlPrefix(String str, MessageStoreClient messageStoreClient) {
        String str2 = messageStoreClient.getCloudMessageStrategyManager().getStrategy().getProtocol() + ":";
        Log.d(LOG_TAG, "replaceUrlPrefix with" + str2);
        return replaceUrlPrefix(str, str2);
    }

    public static String replaceHostOfURL(String str, String str2) {
        try {
            URL url = new URL(str2);
            return new URL(url.getProtocol(), str, url.getFile()).toString();
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String[] parseEmailOverSlm(ImsUri imsUri, String str) {
        if (imsUri != null && imsUri.getUser() != null && !couldBeEmailGateway(imsUri.getUser())) {
            return null;
        }
        String[] split = str.split("( /)|( )", 2);
        if (split.length < 2) {
            return null;
        }
        int length = str.length();
        int indexOf = str.indexOf(64);
        int lastIndexOf = str.lastIndexOf(64);
        int i = lastIndexOf + 1;
        int indexOf2 = str.indexOf(46, i);
        int lastIndexOf2 = str.lastIndexOf(46);
        if (indexOf <= 0 || indexOf != lastIndexOf || i >= indexOf2 || indexOf2 > lastIndexOf2 || lastIndexOf2 >= length - 1) {
            return null;
        }
        return split;
    }

    private static boolean couldBeEmailGateway(String str) {
        return str.length() <= 4;
    }

    public static boolean isSimExist(ISimManager iSimManager) {
        return (iSimManager == null || iSimManager.getSimState() == 1) ? false : true;
    }

    public static String getImei(MessageStoreClient messageStoreClient) {
        TelephonyManager telephonyManager = getTelephonyManager(messageStoreClient.getContext(), messageStoreClient.getClientID());
        if (telephonyManager == null) {
            return "";
        }
        try {
            return (String) ReflectionUtils.invoke2(TelephonyManager.class.getMethod("getImei", new Class[0]), telephonyManager, new Object[0]);
        } catch (IllegalStateException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean isWifiCallingEnabled(Context context) {
        if (Settings.Global.getInt(context.getContentResolver(), VowifiConfig.WIFI_CALL_ENABLE, 0) == 1 || Settings.System.getInt(context.getContentResolver(), "wifi_call_enable1", 0) == 1) {
            Log.d(LOG_TAG, "Wi-Fi Calling is Enabled");
            return true;
        }
        Log.d(LOG_TAG, "Wi-Fi Calling is Disabled");
        return false;
    }

    public static long parseTimeStamp(String str) {
        SimpleDateFormat[] simpleDateFormatArr = {new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault()), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()), new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.getDefault()), new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.getDefault())};
        int i = 0;
        while (i < 6) {
            SimpleDateFormat simpleDateFormat = simpleDateFormatArr[i];
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                Date parse = simpleDateFormat.parse(str);
                if (parse != null) {
                    return parse.getTime();
                }
                return System.currentTimeMillis();
            } catch (ParseException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "ParseException: " + e.getMessage());
                i++;
            }
        }
        return -1;
    }

    public static boolean isDownloadObject(String str, MessageStoreClient messageStoreClient, int i) {
        int i2 = i;
        if (!messageStoreClient.getCloudMessageStrategyManager().getStrategy().isSupportExpiredRule() || TextUtils.isEmpty(str)) {
            return false;
        }
        boolean isMcsSupported = CmsUtil.isMcsSupported(messageStoreClient.getContext(), messageStoreClient.getClientID());
        long parseTimeStamp = parseTimeStamp(str);
        if (isMcsSupported) {
            long currentTimeMillis = System.currentTimeMillis();
            long j = currentTimeMillis - parseTimeStamp;
            if (i2 == 1) {
                int cmsDataTtl = messageStoreClient.getPrerenceManager().getCmsDataTtl();
                String str2 = LOG_TAG;
                Log.i(str2, "msgRecTime " + parseTimeStamp + "  onLineTime " + currentTimeMillis + "  inter " + j + " ttl " + cmsDataTtl);
                if (!(currentTimeMillis == -1 || parseTimeStamp == -1 || j >= ((long) cmsDataTtl) * 1000)) {
                    Log.d(str2, "under TTL hours");
                    return true;
                }
            } else {
                long mmsRevokeTtlSecs = (long) messageStoreClient.getPrerenceManager().getMmsRevokeTtlSecs();
                if (i2 == 3) {
                    mmsRevokeTtlSecs = (long) messageStoreClient.getPrerenceManager().getSmsRevokeTtlSecs();
                }
                if (!(currentTimeMillis == -1 || parseTimeStamp == -1 || j <= mmsRevokeTtlSecs * 1000)) {
                    Log.d(LOG_TAG, "over the legacy timer ");
                    return true;
                }
            }
        } else {
            long networkAvailableTime = messageStoreClient.getPrerenceManager().getNetworkAvailableTime();
            long j2 = networkAvailableTime - parseTimeStamp;
            if (!(networkAvailableTime == -1 || parseTimeStamp == -1 || j2 <= MAX_NOT_SYNC_TIME)) {
                Log.d(LOG_TAG, "over 72 hours");
                return true;
            }
        }
        return false;
    }

    public static boolean isMatchedSubscriptionID(NmsEventList nmsEventList, MessageStoreClient messageStoreClient) {
        URL url;
        String oMASubscriptionResUrl = messageStoreClient.getPrerenceManager().getOMASubscriptionResUrl();
        boolean z = false;
        if (TextUtils.isEmpty(oMASubscriptionResUrl) || nmsEventList.link == null) {
            String str = LOG_TAG;
            Log.d(str, "isMatchedSubscriptionID " + false);
            return false;
        }
        String lastPathFromUrl = getLastPathFromUrl(oMASubscriptionResUrl);
        String str2 = LOG_TAG;
        Log.d(str2, "isMatchedSubscriptionID subscriptionID = " + lastPathFromUrl);
        Link[] linkArr = nmsEventList.link;
        int length = linkArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            Link link = linkArr[i];
            if ((PhoneConstants.SUBSCRIPTION_KEY.equalsIgnoreCase(link.rel) || "NmsSubscription".equalsIgnoreCase(link.rel)) && (url = link.href) != null && lastPathFromUrl.equalsIgnoreCase(getLastPathFromUrl(url.toString()))) {
                z = true;
                break;
            }
            i++;
        }
        String str3 = LOG_TAG;
        Log.d(str3, "isMatchedSubscriptionID " + z);
        return z;
    }

    public static String getLastPathFromUrl(String str) {
        String[] split = str.split("/");
        return split[split.length - 1];
    }

    public static boolean hasChannelExpired(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        long parseTimeStamp = parseTimeStamp(str);
        long currentTimeMillis = System.currentTimeMillis();
        String str2 = LOG_TAG;
        Log.i(str2, "notiTime:" + parseTimeStamp + " curr:" + currentTimeMillis);
        if (parseTimeStamp == -1 || currentTimeMillis >= parseTimeStamp) {
            return true;
        }
        return false;
    }

    public static String getChallengeFromHttpResponse(HttpResponseParams httpResponseParams) {
        String str;
        Iterator<Map.Entry<String, List<String>>> it = httpResponseParams.getHeaders().entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                str = null;
                break;
            }
            Map.Entry next = it.next();
            if ("WWW-Authenticate".equalsIgnoreCase((String) next.getKey())) {
                str = ((List) next.getValue()).toString();
                break;
            }
        }
        if (str == null) {
            return null;
        }
        if (str.startsWith("[") && str.endsWith("]")) {
            str = str.substring(1, str.length() - 1);
        }
        if (str.startsWith(WwwAuthenticateHeader.HEADER_PARAM_DIGEST_SCHEME) || str.startsWith("digest")) {
            return str.charAt(6) != ' ' ? new StringBuffer(str).insert(6, ' ').toString() : str;
        }
        if ((str.startsWith(WwwAuthenticateHeader.HEADER_PARAM_BASIC_SCHEME) || str.startsWith("basic")) && str.charAt(5) != ' ') {
            return new StringBuffer(str).insert(5, ' ').toString();
        }
        return str;
    }

    public static String replaceUriOfAuth(String str, String str2) {
        int indexOf;
        if (!TextUtils.isEmpty(str) && !TextUtils.isEmpty(str2)) {
            String[] split = str.split(",");
            for (String str3 : split) {
                if (str3 != null && str3.length() > 1 && (indexOf = str3.indexOf(61)) > 0) {
                    String substring = str3.substring(0, indexOf);
                    String substring2 = str3.substring(indexOf + 1);
                    if ("uri".equalsIgnoreCase(substring.trim())) {
                        if (substring2.startsWith(CmcConstants.E_NUM_STR_QUOTE) && !str2.startsWith(CmcConstants.E_NUM_STR_QUOTE)) {
                            str2 = CmcConstants.E_NUM_STR_QUOTE + str2 + CmcConstants.E_NUM_STR_QUOTE;
                        }
                        if (str2.equals(substring2)) {
                            return str;
                        }
                        return str.replace(str3, substring + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + str2);
                    }
                }
            }
        }
        return str;
    }

    public static String queryPathFromUrl(String str) {
        try {
            String queryParameter = Uri.parse(str).getQueryParameter("path");
            if (!TextUtils.isEmpty(queryParameter)) {
                str = queryParameter;
            }
            String path = new URI(str).getPath();
            String str2 = LOG_TAG;
            IMSLog.s(str2, "path = " + path);
            return path;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "/";
        }
    }

    public static boolean isColumnNotExists(SQLiteDatabase sQLiteDatabase, String str, String str2) {
        Cursor rawQuery;
        try {
            rawQuery = sQLiteDatabase.rawQuery("pragma table_info(" + str + ")", (String[]) null);
            if (rawQuery != null) {
                if (rawQuery.moveToFirst()) {
                    while (!str2.equalsIgnoreCase(rawQuery.getString(rawQuery.getColumnIndex("name")))) {
                        if (!rawQuery.moveToNext()) {
                        }
                    }
                    rawQuery.close();
                    return false;
                }
            }
            if (rawQuery == null) {
                return true;
            }
            rawQuery.close();
            return true;
        } catch (SQLException e) {
            String str3 = LOG_TAG;
            Log.e(str3, " exception:" + e.toString());
            return true;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public static TelephonyManager getTelephonyManager(Context context, int i) {
        return ((TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY)).createForSubscriptionId(SimUtil.getSubId(i));
    }

    public static String encodeRFC3986(String str) {
        try {
            return URLEncoder.encode(str, "utf-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            IMSLog.i(LOG_TAG, e.toString());
            e.printStackTrace();
            return str;
        }
    }

    public static String getSimCountryCode(Context context, int i) {
        return getTelephonyManager(context, i).getSimCountryIso().toUpperCase(Locale.ENGLISH);
    }

    private static String decode(String str) {
        return new String(Base64.getUrlDecoder().decode(str), StandardCharsets.UTF_8);
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x007f  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0086 A[RETURN] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long getIntegerPayloadFromToken(java.lang.String r11, java.lang.String r12) {
        /*
            java.lang.String r0 = "iat"
            java.lang.String r1 = "exp"
            boolean r2 = android.text.TextUtils.isEmpty(r11)     // Catch:{ JSONException -> 0x0087 }
            if (r2 != 0) goto L_0x0091
            java.lang.String r2 = "\\."
            java.lang.String[] r11 = r11.split(r2)     // Catch:{ JSONException -> 0x0087 }
            r2 = 1
            r11 = r11[r2]     // Catch:{ JSONException -> 0x0087 }
            org.json.JSONObject r3 = new org.json.JSONObject     // Catch:{ JSONException -> 0x0087 }
            java.lang.String r11 = decode(r11)     // Catch:{ JSONException -> 0x0087 }
            r3.<init>(r11)     // Catch:{ JSONException -> 0x0087 }
            long r4 = r3.getLong(r1)     // Catch:{ JSONException -> 0x0087 }
            long r6 = r3.getLong(r0)     // Catch:{ JSONException -> 0x0087 }
            long r8 = r4 - r6
            java.lang.String r11 = LOG_TAG     // Catch:{ JSONException -> 0x0087 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ JSONException -> 0x0087 }
            r3.<init>()     // Catch:{ JSONException -> 0x0087 }
            java.lang.String r10 = "exp: "
            r3.append(r10)     // Catch:{ JSONException -> 0x0087 }
            r3.append(r4)     // Catch:{ JSONException -> 0x0087 }
            java.lang.String r10 = ", iat: "
            r3.append(r10)     // Catch:{ JSONException -> 0x0087 }
            r3.append(r6)     // Catch:{ JSONException -> 0x0087 }
            java.lang.String r10 = ", validity: "
            r3.append(r10)     // Catch:{ JSONException -> 0x0087 }
            r3.append(r8)     // Catch:{ JSONException -> 0x0087 }
            java.lang.String r3 = r3.toString()     // Catch:{ JSONException -> 0x0087 }
            android.util.Log.d(r11, r3)     // Catch:{ JSONException -> 0x0087 }
            int r11 = r12.hashCode()     // Catch:{ JSONException -> 0x0087 }
            r3 = -1421265102(0xffffffffab493732, float:-7.1486144E-13)
            r10 = 2
            if (r11 == r3) goto L_0x0071
            r3 = 100893(0x18a1d, float:1.41381E-40)
            if (r11 == r3) goto L_0x0069
            r1 = 104028(0x1965c, float:1.45774E-40)
            if (r11 == r1) goto L_0x0061
            goto L_0x007c
        L_0x0061:
            boolean r11 = r12.equals(r0)     // Catch:{ JSONException -> 0x0087 }
            if (r11 == 0) goto L_0x007c
            r11 = r2
            goto L_0x007d
        L_0x0069:
            boolean r11 = r12.equals(r1)     // Catch:{ JSONException -> 0x0087 }
            if (r11 == 0) goto L_0x007c
            r11 = 0
            goto L_0x007d
        L_0x0071:
            java.lang.String r11 = "validity"
            boolean r11 = r12.equals(r11)     // Catch:{ JSONException -> 0x0087 }
            if (r11 == 0) goto L_0x007c
            r11 = r10
            goto L_0x007d
        L_0x007c:
            r11 = -1
        L_0x007d:
            if (r11 == 0) goto L_0x0086
            if (r11 == r2) goto L_0x0085
            if (r11 == r10) goto L_0x0084
            goto L_0x0091
        L_0x0084:
            return r8
        L_0x0085:
            return r6
        L_0x0086:
            return r4
        L_0x0087:
            r11 = move-exception
            java.lang.String r12 = LOG_TAG
            java.lang.String r11 = r11.getMessage()
            android.util.Log.e(r12, r11)
        L_0x0091:
            r11 = 0
            return r11
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.Util.getIntegerPayloadFromToken(java.lang.String, java.lang.String):long");
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x005f A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:17:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getStringPayloadFromToken(java.lang.String r4, java.lang.String r5) {
        /*
            java.lang.String r0 = "iss"
            boolean r1 = android.text.TextUtils.isEmpty(r4)     // Catch:{ Exception -> 0x0060 }
            if (r1 != 0) goto L_0x006a
            java.lang.String r1 = LOG_TAG     // Catch:{ Exception -> 0x0060 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0060 }
            r2.<init>()     // Catch:{ Exception -> 0x0060 }
            java.lang.String r3 = "token: "
            r2.append(r3)     // Catch:{ Exception -> 0x0060 }
            r2.append(r4)     // Catch:{ Exception -> 0x0060 }
            java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x0060 }
            android.util.Log.d(r1, r2)     // Catch:{ Exception -> 0x0060 }
            java.lang.String r2 = "\\."
            java.lang.String[] r4 = r4.split(r2)     // Catch:{ Exception -> 0x0060 }
            r2 = 1
            r4 = r4[r2]     // Catch:{ Exception -> 0x0060 }
            org.json.JSONObject r2 = new org.json.JSONObject     // Catch:{ Exception -> 0x0060 }
            java.lang.String r4 = decode(r4)     // Catch:{ Exception -> 0x0060 }
            r2.<init>(r4)     // Catch:{ Exception -> 0x0060 }
            java.lang.String r4 = r2.getString(r0)     // Catch:{ Exception -> 0x0060 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0060 }
            r2.<init>()     // Catch:{ Exception -> 0x0060 }
            java.lang.String r3 = "iss: "
            r2.append(r3)     // Catch:{ Exception -> 0x0060 }
            r2.append(r4)     // Catch:{ Exception -> 0x0060 }
            java.lang.String r2 = r2.toString()     // Catch:{ Exception -> 0x0060 }
            android.util.Log.d(r1, r2)     // Catch:{ Exception -> 0x0060 }
            int r1 = r5.hashCode()     // Catch:{ Exception -> 0x0060 }
            r2 = 104585(0x19889, float:1.46555E-40)
            if (r1 == r2) goto L_0x0053
            goto L_0x005b
        L_0x0053:
            boolean r5 = r5.equals(r0)     // Catch:{ Exception -> 0x0060 }
            if (r5 == 0) goto L_0x005b
            r5 = 0
            goto L_0x005c
        L_0x005b:
            r5 = -1
        L_0x005c:
            if (r5 == 0) goto L_0x005f
            goto L_0x006a
        L_0x005f:
            return r4
        L_0x0060:
            r4 = move-exception
            java.lang.String r5 = LOG_TAG
            java.lang.String r4 = r4.getMessage()
            android.util.Log.e(r5, r4)
        L_0x006a:
            java.lang.String r4 = ""
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.Util.getStringPayloadFromToken(java.lang.String, java.lang.String):java.lang.String");
    }

    public static boolean isRegistrationCodeInvalid(String str) {
        if (TextUtils.isEmpty(str)) {
            return true;
        }
        long integerPayloadFromToken = getIntegerPayloadFromToken(str, CloudMessageProviderContract.BufferDBMMSpdu.EXP) - (System.currentTimeMillis() / 1000);
        String str2 = LOG_TAG;
        Log.i(str2, "isRegistrationCodeInvalid: remainValidity = " + integerPayloadFromToken);
        if (integerPayloadFromToken <= 0) {
            return true;
        }
        return false;
    }

    public static String buildUploadURL(String str, String str2) {
        Uri.Builder buildUpon = Uri.parse(str).buildUpon();
        buildUpon.appendPath("oapi").appendPath(str2).appendPath("file");
        String uri = buildUpon.build().toString();
        String str3 = LOG_TAG;
        Log.i(str3, "Build Upload URL  : " + IMSLog.checker(uri));
        return uri;
    }

    public static String getReactionReferenceValue(int i) {
        String[] strArr = {"+U+1F44D", "+U+2764", "+U+1F44C", "+U+1F604", "+U+1F914", "+U+1F62D", "+U+1F44E"};
        if (i < 0 || i >= 7) {
            i = 0;
        }
        return strArr[i];
    }

    public static boolean isLocationPushContentType(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return str.toLowerCase().startsWith(MIMEContentType.LOCATION_PUSH);
    }

    public static boolean isBotMessageContentType(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        String lowerCase = str.toLowerCase();
        if (lowerCase.startsWith(MIMEContentType.BOT_MESSAGE) || lowerCase.startsWith(MIMEContentType.XBOT_MESSAGE) || lowerCase.startsWith(MIMEContentType.OPEN_RICH_CARD)) {
            return true;
        }
        return false;
    }

    public static boolean isBotResponseMessageContentType(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        String lowerCase = str.toLowerCase();
        if (lowerCase.startsWith(MIMEContentType.BOT_SUGGESTION_RESPONSE) || lowerCase.startsWith(MIMEContentType.BOT_SHARED_CLIENT_DATA)) {
            return true;
        }
        return false;
    }

    public static String decodeBase64(String str) {
        if (str != null) {
            try {
                return new String(android.util.Base64.decode(str, 2));
            } catch (IllegalArgumentException e) {
                Log.i(LOG_TAG, e.getMessage());
            }
        }
        return null;
    }
}
