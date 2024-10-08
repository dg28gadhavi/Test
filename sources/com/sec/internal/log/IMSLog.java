package com.sec.internal.log;

import android.content.Context;
import android.os.SemSystemProperties;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.ims.entitlement.util.EncryptionHelper;
import com.sec.internal.ims.settings.ImsAutoUpdate$$ExternalSyntheticLambda0;
import com.sec.internal.ims.settings.ImsAutoUpdate$$ExternalSyntheticLambda1;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class IMSLog {
    private static final boolean DEBUG_MODE;
    private static final boolean ENG_MODE = SemSystemProperties.get("ro.build.type", "user").equals("eng");
    private static final String EOL = System.getProperty("line.separator", "\n");
    private static final String INDENT = "  ";
    private static final int MAX_DUMP_LEN = 1024;
    private static String SALES_CODE = null;
    private static final boolean SHIP_BUILD = CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("ro.product_ship", ConfigConstants.VALUE.INFO_COMPLETED));
    private static final String TAG = "IMSLog";
    private static EncryptedLogger encryptedLogger = EncryptedLogger.getInstance();
    private static boolean mIsOtpAuthorized = false;
    private static boolean mIsUt = false;
    private static HashSet<String> mShowSLogInShipBuildSet;
    private static ByteBuffer sByteBuffer = null;
    private static long sDumpStartTime = 0;
    private static FileChannel sFileChannel = null;
    private static Map<String, String> sIndent = new ConcurrentHashMap();
    private static PrintWriter sPw = null;

    public enum LAZER_TYPE {
        CALL,
        REGI
    }

    static {
        boolean z = true;
        if (SemSystemProperties.getInt("ro.debuggable", 0) != 1) {
            z = false;
        }
        DEBUG_MODE = z;
        String str = SemSystemProperties.get(OmcCode.PERSIST_OMC_CODE_PROPERTY, "");
        SALES_CODE = str;
        if (TextUtils.isEmpty(str)) {
            SALES_CODE = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY, "");
        }
        HashSet<String> hashSet = new HashSet<>();
        mShowSLogInShipBuildSet = hashSet;
        hashSet.add("ATX");
        mShowSLogInShipBuildSet.add("OMX");
        mShowSLogInShipBuildSet.add("VDR");
        mShowSLogInShipBuildSet.add("VDP");
        mShowSLogInShipBuildSet.add("VOP");
    }

    public static String appendSessionIdToLogTag(String str, int i) {
        return str.split("\\(")[0] + "(" + i + ")";
    }

    private static String getImsDumpPath(Context context) {
        String str = (String) Optional.ofNullable((StorageManager) context.getSystemService(StorageManager.class)).map(new ImsAutoUpdate$$ExternalSyntheticLambda0()).map(new ImsAutoUpdate$$ExternalSyntheticLambda1()).orElse("");
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        try {
            File file = new File(str.concat("/log/ims_logs/"));
            String str2 = null;
            if (file.exists()) {
                long j = -1;
                for (File file2 : file.listFiles(new FileFilter() {
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                })) {
                    boolean z = false;
                    for (File file3 : file2.listFiles()) {
                        String name = file3.getName();
                        int lastIndexOf = name.lastIndexOf(".");
                        if (file3.isFile() && lastIndexOf > 0) {
                            if (file2.getName().endsWith(name.substring(0, lastIndexOf))) {
                                z = true;
                            }
                        }
                    }
                    if (z && j < file2.lastModified()) {
                        str2 = file2.getAbsolutePath();
                        j = file2.lastModified();
                    }
                }
            }
            return str2;
        } catch (NullPointerException | SecurityException e) {
            e.printStackTrace();
            return "";
        }
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(5:4|5|(5:7|8|(2:11|9)|19|12)|13|14) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0053 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void dumpToFile(java.lang.String r4) {
        /*
            java.nio.channels.FileChannel r0 = sFileChannel
            if (r0 == 0) goto L_0x0058
            java.lang.Class<com.sec.internal.log.IMSLog> r0 = com.sec.internal.log.IMSLog.class
            monitor-enter(r0)
            java.nio.channels.FileChannel r1 = sFileChannel     // Catch:{ all -> 0x0055 }
            if (r1 == 0) goto L_0x0053
            java.nio.charset.Charset r1 = java.nio.charset.StandardCharsets.UTF_8     // Catch:{ IOException | RuntimeException -> 0x0053 }
            byte[] r4 = r4.getBytes(r1)     // Catch:{ IOException | RuntimeException -> 0x0053 }
            r1 = 0
        L_0x0012:
            int r2 = r4.length     // Catch:{ IOException | RuntimeException -> 0x0053 }
            if (r1 >= r2) goto L_0x0035
            int r2 = r4.length     // Catch:{ IOException | RuntimeException -> 0x0053 }
            int r2 = r2 - r1
            r3 = 1024(0x400, float:1.435E-42)
            int r2 = java.lang.Math.min(r3, r2)     // Catch:{ IOException | RuntimeException -> 0x0053 }
            java.nio.ByteBuffer r3 = sByteBuffer     // Catch:{ IOException | RuntimeException -> 0x0053 }
            r3.put(r4, r1, r2)     // Catch:{ IOException | RuntimeException -> 0x0053 }
            int r1 = r1 + r2
            java.nio.ByteBuffer r2 = sByteBuffer     // Catch:{ IOException | RuntimeException -> 0x0053 }
            r2.flip()     // Catch:{ IOException | RuntimeException -> 0x0053 }
            java.nio.channels.FileChannel r2 = sFileChannel     // Catch:{ IOException | RuntimeException -> 0x0053 }
            java.nio.ByteBuffer r3 = sByteBuffer     // Catch:{ IOException | RuntimeException -> 0x0053 }
            r2.write(r3)     // Catch:{ IOException | RuntimeException -> 0x0053 }
            java.nio.ByteBuffer r2 = sByteBuffer     // Catch:{ IOException | RuntimeException -> 0x0053 }
            r2.clear()     // Catch:{ IOException | RuntimeException -> 0x0053 }
            goto L_0x0012
        L_0x0035:
            java.nio.ByteBuffer r4 = sByteBuffer     // Catch:{ IOException | RuntimeException -> 0x0053 }
            java.lang.String r1 = EOL     // Catch:{ IOException | RuntimeException -> 0x0053 }
            java.nio.charset.Charset r2 = java.nio.charset.StandardCharsets.UTF_8     // Catch:{ IOException | RuntimeException -> 0x0053 }
            byte[] r1 = r1.getBytes(r2)     // Catch:{ IOException | RuntimeException -> 0x0053 }
            r4.put(r1)     // Catch:{ IOException | RuntimeException -> 0x0053 }
            java.nio.ByteBuffer r4 = sByteBuffer     // Catch:{ IOException | RuntimeException -> 0x0053 }
            r4.flip()     // Catch:{ IOException | RuntimeException -> 0x0053 }
            java.nio.channels.FileChannel r4 = sFileChannel     // Catch:{ IOException | RuntimeException -> 0x0053 }
            java.nio.ByteBuffer r1 = sByteBuffer     // Catch:{ IOException | RuntimeException -> 0x0053 }
            r4.write(r1)     // Catch:{ IOException | RuntimeException -> 0x0053 }
            java.nio.ByteBuffer r4 = sByteBuffer     // Catch:{ IOException | RuntimeException -> 0x0053 }
            r4.clear()     // Catch:{ IOException | RuntimeException -> 0x0053 }
        L_0x0053:
            monitor-exit(r0)     // Catch:{ all -> 0x0055 }
            goto L_0x0058
        L_0x0055:
            r4 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0055 }
            throw r4
        L_0x0058:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.log.IMSLog.dumpToFile(java.lang.String):void");
    }

    public static void increaseIndent(String str) {
        if (!sIndent.containsKey(str)) {
            sIndent.put(str, "");
        }
        Map<String, String> map = sIndent;
        map.put(str, map.get(str).concat(INDENT));
    }

    public static void decreaseIndent(String str) {
        if (sIndent.containsKey(str)) {
            Map<String, String> map = sIndent;
            map.put(str, map.get(str).replaceFirst(INDENT, ""));
        }
    }

    public static void prepareDump(Context context, PrintWriter printWriter) {
        FileInputStream fileInputStream;
        if (sFileChannel == null) {
            synchronized (IMSLog.class) {
                if (sFileChannel == null) {
                    sPw = printWriter;
                    String imsDumpPath = getImsDumpPath(context);
                    if (!TextUtils.isEmpty(imsDumpPath)) {
                        try {
                            sFileChannel = new FileOutputStream(new File(imsDumpPath.concat("/ims_dump.log")), true).getChannel();
                            sByteBuffer = ByteBuffer.allocateDirect(1024).order(ByteOrder.nativeOrder());
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.US);
                            sDumpStartTime = SystemClock.elapsedRealtime();
                            String str = TAG;
                            dump(str, "dump started at " + simpleDateFormat.format(new Date()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            postDump(printWriter);
                        }
                    } else {
                        postDump(printWriter);
                    }
                }
            }
        }
        try {
            fileInputStream = new FileInputStream("/efs/sec_efs/.otp_auth");
            byte[] bArr = new byte[1024];
            if (fileInputStream.read(bArr) > 0) {
                mIsOtpAuthorized = CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(new String(bArr).trim());
            }
            fileInputStream.close();
            return;
        } catch (IOException e2) {
            String str2 = TAG;
            d(str2, "IOException:" + e2);
            return;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public static void postDump(PrintWriter printWriter) {
        if (sFileChannel != null) {
            synchronized (IMSLog.class) {
                if (sFileChannel != null) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.US);
                    String str = TAG;
                    dump(str, "dump finished at " + simpleDateFormat.format(new Date()));
                    if (sDumpStartTime > 0) {
                        dump(str, "elapsed time: " + (SystemClock.elapsedRealtime() - sDumpStartTime) + "msecs");
                    }
                    sDumpStartTime = 0;
                    try {
                        sFileChannel.close();
                    } catch (IOException e) {
                        try {
                            e.printStackTrace();
                        } catch (Throwable th) {
                            sFileChannel = null;
                            throw th;
                        }
                    }
                    sFileChannel = null;
                    if (printWriter != null) {
                        printWriter.close();
                    }
                    sPw = null;
                }
                ByteBuffer byteBuffer = sByteBuffer;
                if (byteBuffer != null) {
                    byteBuffer.clear();
                    sByteBuffer = null;
                }
            }
        }
        mIsOtpAuthorized = false;
    }

    public static void dump(String str, String str2) {
        dump(str, 0, str2, true);
    }

    public static void dump(String str, int i, String str2) {
        dump(str, i, str2, true);
    }

    public static void dump(String str, String str2, boolean z) {
        dump(str, 0, str2, z);
    }

    public static void dumpEncryptedACS(String str, String str2) {
        String str3 = sIndent.get(str);
        if (str3 != null) {
            str2 = str3 + str2;
        }
        String encryptAcs = EncryptionHelper.getInstance(ConfigUtil.TRANSFORMATION).encryptAcs(filterLog(str2, false));
        if (encryptAcs != null) {
            dumpToFile(encryptAcs);
            PrintWriter printWriter = sPw;
            if (printWriter != null) {
                printWriter.println(encryptAcs);
            }
        }
    }

    public static void dump(String str, int i, String str2, boolean z) {
        String str3 = sIndent.get(str);
        if (str3 != null) {
            str2 = str3 + str2;
        }
        String filterLog = filterLog(str2, z);
        dumpToFile(filterLog);
        PrintWriter printWriter = sPw;
        if (printWriter != null) {
            printWriter.println(filterLog);
        }
    }

    public static void d(String str, String str2) {
        Log.d(str, str2);
    }

    public static void d(String str, int i, String str2) {
        Log.d(str + "<" + i + ">", str2);
    }

    public static void d(String str, int i, IRegisterTask iRegisterTask, String str2) {
        Log.d(str + "<" + i + ">", "[" + iRegisterTask.getProfile().getName() + "|" + iRegisterTask.getState() + "] " + str2);
    }

    public static void i(String str, String str2) {
        Log.i(str, str2);
    }

    public static void i(String str, int i, String str2) {
        Log.i(str + "<" + i + ">", str2);
    }

    public static void i(String str, int i, IRegisterTask iRegisterTask, String str2) {
        Log.i(str + "<" + i + ">", "[" + iRegisterTask.getProfile().getName() + "|" + iRegisterTask.getState() + "] " + str2);
    }

    public static void e(String str, String str2) {
        Log.e(str, str2);
    }

    public static void e(String str, int i, String str2) {
        Log.e(str + "<" + i + ">", str2);
    }

    public static void e(String str, int i, IRegisterTask iRegisterTask, String str2) {
        Log.e(str + "<" + i + ">", "[" + iRegisterTask.getProfile().getName() + "|" + iRegisterTask.getState() + "] " + str2);
    }

    public static void s(String str, String str2) {
        if (!isShipBuild()) {
            Log.d(str, str2);
        }
    }

    public static void s(String str, int i, String str2) {
        if (!isShipBuild()) {
            Log.d(str + "<" + i + ">", str2);
        }
    }

    public static void s(String str, int i, IRegisterTask iRegisterTask, String str2) {
        if (!isShipBuild()) {
            Log.d(str + "<" + i + ">", "[" + iRegisterTask.getProfile().getName() + "|" + iRegisterTask.getState() + "] " + str2);
        }
    }

    public static void g(String str, String str2) {
        Log.i(str, str2);
    }

    public static String checker(Object obj) {
        return checker(obj, false);
    }

    public static String checker(Object obj, boolean z) {
        if (obj == null) {
            return null;
        }
        if (isShipBuild() && (!z || !mIsUt)) {
            return "xxxxx";
        }
        return "" + obj;
    }

    public static String numberChecker(String str) {
        return numberChecker(str, false);
    }

    public static String numberChecker(String str, boolean z) {
        if (str == null) {
            return null;
        }
        if (isShipBuild()) {
            return (!z || !mIsUt) ? str.replaceAll("\\d(?=\\d{3})", "*") : str;
        }
        return str;
    }

    public static String numberChecker(ImsUri imsUri) {
        if (imsUri == null) {
            return null;
        }
        return numberChecker(imsUri.toString());
    }

    public static String numberChecker(Collection<ImsUri> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        if (isShipBuild()) {
            StringBuilder sb = new StringBuilder();
            for (ImsUri imsUri : collection) {
                sb.append(numberChecker(imsUri.toString()));
                sb.append(", ");
            }
            return sb.toString();
        }
        return "" + collection;
    }

    public static boolean isShipBuild() {
        return SHIP_BUILD && !mShowSLogInShipBuildSet.contains(SALES_CODE);
    }

    public static boolean isEngMode() {
        return ENG_MODE;
    }

    public static void lazer(LAZER_TYPE lazer_type, String str) {
        if (lazer_type == LAZER_TYPE.CALL) {
            e("#IMSCALL", str);
        } else if (lazer_type == LAZER_TYPE.REGI) {
            e("#IMSREGI", str);
        }
    }

    public static void lazer(IRegisterTask iRegisterTask, String str) {
        e("#IMSREGI", "(" + iRegisterTask.getPhoneId() + ", " + iRegisterTask.getProfile().getName() + ") " + str);
    }

    private static String filterLog(String str, boolean z) {
        return !mIsOtpAuthorized && z && isShipBuild() ? str.replaceAll("\\d(?=\\d{3})", "*") : str;
    }

    public static void c(int i) {
        c(i, (String) null);
    }

    public static void c(int i, String str) {
        c(i, str, false);
    }

    public static void c(int i, String str, boolean z) {
        CriticalLogger.getInstance().write(i, str);
        if (z) {
            CriticalLogger.getInstance().flush();
        }
    }

    public static String vx(String str, String str2) {
        return x(str, str2, (Throwable) null, 2);
    }

    public static String dx(String str, String str2) {
        return x(str, str2, (Throwable) null, 3);
    }

    public static String ex(String str, String str2, Throwable th) {
        return x(str, str2, th, 6);
    }

    private static String x(String str, String str2, Throwable th, int i) {
        if (th != null) {
            StackTraceElement[] stackTrace = th.getStackTrace();
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(str2);
            if (th.getMessage() != null) {
                stringBuffer.append("\n");
                stringBuffer.append(th.getMessage());
            }
            if (stackTrace.length > 0) {
                stringBuffer.append("\n");
                for (StackTraceElement stackTraceElement : stackTrace) {
                    stringBuffer.append(stackTraceElement.toString());
                    stringBuffer.append("\n");
                }
                stringBuffer.append("\n");
            }
            str2 = stringBuffer.toString();
        }
        return encryptedLogger.doLog(str, str2, i);
    }

    public static void assertUnreachable(String str, String str2) {
        e(str, str2);
        if (!SHIP_BUILD) {
            throw new RuntimeException(str2);
        }
    }

    public static void assertFalse(String str, int i, String str2, boolean z) {
        if (z) {
            e(str, i, str2);
            if (!SHIP_BUILD) {
                throw new RuntimeException(str2);
            }
        }
    }

    public static void dumpSecretKey(String str) {
        String base64EncodedSecretKey = encryptedLogger.getBase64EncodedSecretKey();
        if (DEBUG_MODE) {
            base64EncodedSecretKey = base64EncodedSecretKey + "\n" + encryptedLogger._debug_GetSecretKeyInfo();
        }
        if (base64EncodedSecretKey != null) {
            dump(str, base64EncodedSecretKey, false);
        } else {
            dump(str, "Secret key is not ready");
        }
    }

    public static void setIsUt(boolean z) {
        mIsUt = z;
    }
}
