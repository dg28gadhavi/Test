package com.sec.internal.ims.util;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class CscParser {
    private static final String COUNTRY_ISO_PATH = "CustomerData.GeneralInfo.CountryISO";
    private static final String CSC_EDITION_PATH = "CustomerData.GeneralInfo.CSCEdition";
    private static final String CSC_SW_CONFIG_FILE_PATH = "/system/SW_Configuration.xml";
    private static final String CUSTOMER_CSC_FILE_NAME = "/customer.xml";
    private static final String CUSTOMER_CSC_FILE_PATH = "/system/csc";
    private static final String IMS_PATH = "CustomerData.Settings.IMSSetting.NbSetting";
    private static final String KEY_CSC_EDITION = "csc.key.edition";
    private static final String KEY_CSC_SALES_CODE = "csc.key.salescode";
    private static final String KEY_CSC_VERSION = "csc.key.version";
    private static final String KEY_OMC_VERSION = "omc.key.version";
    private static final String LOG_TAG = "CscParser";
    private static final String NETWORK_INFO_PATH = "CustomerData.GeneralInfo.NetworkInfo";
    private static final String OMC_INFO_FILE_NAME = "/omc.info";
    private static final String OMC_INFO_VERSION = "version";
    private static final String SALES_CODE_PATH = "CustomerData.GeneralInfo.SalesCode";
    private static final String SW_CONFIG_CSCNAME = "CSCName";
    private static final String SW_CONFIG_CSCVERSION = "CSCVersion";
    private static boolean[] sCscChangeChecked = {false, false};

    private static FileInputStream getCscFile(int i) {
        String str;
        String omcNwPath = OmcCode.getOmcNwPath(i);
        if (!TextUtils.isEmpty(omcNwPath)) {
            str = omcNwPath + CUSTOMER_CSC_FILE_NAME;
        } else {
            str = "/system/csc/customer.xml";
        }
        try {
            return new FileInputStream(new File(str));
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, e.getClass().getSimpleName() + "!! " + e.getMessage());
            return null;
        }
    }

    private static XmlPullParser getCscCustomerParser(FileInputStream fileInputStream) {
        if (fileInputStream == null) {
            Log.d(LOG_TAG, "no csc file");
            return null;
        }
        try {
            XmlPullParserFactory newInstance = XmlPullParserFactory.newInstance();
            newInstance.setNamespaceAware(true);
            XmlPullParser newPullParser = newInstance.newPullParser();
            newPullParser.setInput(fileInputStream, (String) null);
            return newPullParser;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            closeFileInputStream(fileInputStream);
            return null;
        }
    }

    private static void closeFileInputStream(FileInputStream fileInputStream) {
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static String getCscEdition(int i) {
        return getFieldFromCsc(i, CSC_EDITION_PATH);
    }

    /* JADX WARNING: type inference failed for: r0v5 */
    /* JADX WARNING: type inference failed for: r0v8, types: [java.io.FileInputStream] */
    /* JADX WARNING: type inference failed for: r0v10 */
    /* JADX WARNING: type inference failed for: r0v12, types: [java.lang.CharSequence] */
    /* JADX WARNING: type inference failed for: r0v32 */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00b0, code lost:
        r9 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00b1, code lost:
        r0 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00b3, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00b4, code lost:
        r1 = null;
        r3 = r2;
        r2 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00b0 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:11:0x0049] */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00c4 A[SYNTHETIC, Splitter:B:57:0x00c4] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00d6  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x00e8 A[SYNTHETIC, Splitter:B:68:0x00e8] */
    /* JADX WARNING: Removed duplicated region for block: B:79:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static java.lang.String getCscVersion(java.lang.String r9) {
        /*
            boolean r0 = com.sec.internal.helper.OmcCode.isOmcModel()
            if (r0 == 0) goto L_0x0020
            java.lang.String r0 = "persist.sys.omc_root"
            java.lang.String r1 = "/odm/omc"
            java.lang.String r0 = android.os.SemSystemProperties.get(r0, r1)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r0)
            java.lang.String r0 = "/SW_Configuration.xml"
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            goto L_0x0022
        L_0x0020:
            java.lang.String r0 = "/system/SW_Configuration.xml"
        L_0x0022:
            java.io.File r1 = new java.io.File
            r1.<init>(r0)
            boolean r0 = r1.exists()
            java.lang.String r2 = "CscParser"
            if (r0 == 0) goto L_0x00ec
            boolean r0 = r1.canRead()
            if (r0 != 0) goto L_0x0037
            goto L_0x00ec
        L_0x0037:
            r0 = 0
            org.xmlpull.v1.XmlPullParserFactory r3 = org.xmlpull.v1.XmlPullParserFactory.newInstance()     // Catch:{ IOException | XmlPullParserException -> 0x00bb }
            r4 = 1
            r3.setNamespaceAware(r4)     // Catch:{ IOException | XmlPullParserException -> 0x00bb }
            org.xmlpull.v1.XmlPullParser r3 = r3.newPullParser()     // Catch:{ IOException | XmlPullParserException -> 0x00bb }
            java.io.FileInputStream r5 = new java.io.FileInputStream     // Catch:{ IOException | XmlPullParserException -> 0x00bb }
            r5.<init>(r1)     // Catch:{ IOException | XmlPullParserException -> 0x00bb }
            r3.setInput(r5, r0)     // Catch:{ IOException | XmlPullParserException -> 0x00b3, all -> 0x00b0 }
            r1 = r0
            r6 = r1
        L_0x004e:
            int r7 = r3.getEventType()     // Catch:{ IOException | XmlPullParserException -> 0x00ab, all -> 0x00b0 }
            if (r7 == r4) goto L_0x00a7
            r8 = 2
            if (r7 == r8) goto L_0x008d
            r8 = 4
            if (r7 == r8) goto L_0x005b
            goto L_0x0091
        L_0x005b:
            java.lang.String r7 = r3.getText()     // Catch:{ IOException | XmlPullParserException -> 0x00ab, all -> 0x00b0 }
            boolean r8 = android.text.TextUtils.isEmpty(r7)     // Catch:{ IOException | XmlPullParserException -> 0x00ab, all -> 0x00b0 }
            if (r8 != 0) goto L_0x008a
            boolean r8 = android.text.TextUtils.isEmpty(r0)     // Catch:{ IOException | XmlPullParserException -> 0x00ab, all -> 0x00b0 }
            if (r8 == 0) goto L_0x0078
            java.lang.String r8 = "CSCName"
            boolean r8 = r8.equals(r6)     // Catch:{ IOException | XmlPullParserException -> 0x00ab, all -> 0x00b0 }
            if (r8 == 0) goto L_0x0078
            java.lang.String r0 = r7.trim()     // Catch:{ IOException | XmlPullParserException -> 0x00ab, all -> 0x00b0 }
            goto L_0x008a
        L_0x0078:
            boolean r8 = android.text.TextUtils.isEmpty(r1)     // Catch:{ IOException | XmlPullParserException -> 0x00ab, all -> 0x00b0 }
            if (r8 == 0) goto L_0x008a
            java.lang.String r8 = "CSCVersion"
            boolean r6 = r8.equals(r6)     // Catch:{ IOException | XmlPullParserException -> 0x00ab, all -> 0x00b0 }
            if (r6 == 0) goto L_0x008a
            java.lang.String r1 = r7.trim()     // Catch:{ IOException | XmlPullParserException -> 0x00ab, all -> 0x00b0 }
        L_0x008a:
            java.lang.String r6 = ""
            goto L_0x0091
        L_0x008d:
            java.lang.String r6 = r3.getName()     // Catch:{ IOException | XmlPullParserException -> 0x00ab, all -> 0x00b0 }
        L_0x0091:
            boolean r7 = android.text.TextUtils.isEmpty(r0)     // Catch:{ IOException | XmlPullParserException -> 0x00ab, all -> 0x00b0 }
            if (r7 != 0) goto L_0x00a3
            boolean r7 = android.text.TextUtils.isEmpty(r1)     // Catch:{ IOException | XmlPullParserException -> 0x00ab, all -> 0x00b0 }
            if (r7 != 0) goto L_0x00a3
            java.lang.String r3 = "Found Name and Version"
            android.util.Log.d(r2, r3)     // Catch:{ IOException | XmlPullParserException -> 0x00ab, all -> 0x00b0 }
            goto L_0x00a7
        L_0x00a3:
            r3.next()     // Catch:{ IOException | XmlPullParserException -> 0x00ab, all -> 0x00b0 }
            goto L_0x004e
        L_0x00a7:
            r5.close()     // Catch:{ IOException -> 0x00c9 }
            goto L_0x00c9
        L_0x00ab:
            r2 = move-exception
            r3 = r2
            r2 = r1
            r1 = r0
            goto L_0x00b7
        L_0x00b0:
            r9 = move-exception
            r0 = r5
            goto L_0x00e6
        L_0x00b3:
            r2 = move-exception
            r1 = r0
            r3 = r2
            r2 = r1
        L_0x00b7:
            r0 = r5
            goto L_0x00bf
        L_0x00b9:
            r9 = move-exception
            goto L_0x00e6
        L_0x00bb:
            r2 = move-exception
            r1 = r0
            r3 = r2
            r2 = r1
        L_0x00bf:
            r3.printStackTrace()     // Catch:{ all -> 0x00b9 }
            if (r0 == 0) goto L_0x00c7
            r0.close()     // Catch:{ IOException -> 0x00c7 }
        L_0x00c7:
            r0 = r1
            r1 = r2
        L_0x00c9:
            boolean r2 = android.text.TextUtils.isEmpty(r0)
            if (r2 != 0) goto L_0x00e5
            boolean r2 = android.text.TextUtils.isEmpty(r1)
            if (r2 == 0) goto L_0x00d6
            goto L_0x00e5
        L_0x00d6:
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            r9.append(r0)
            r9.append(r1)
            java.lang.String r9 = r9.toString()
        L_0x00e5:
            return r9
        L_0x00e6:
            if (r0 == 0) goto L_0x00eb
            r0.close()     // Catch:{ IOException -> 0x00eb }
        L_0x00eb:
            throw r9
        L_0x00ec:
            java.lang.String r0 = "Can't read CSC Version"
            android.util.Log.e(r2, r0)
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.CscParser.getCscVersion(java.lang.String):java.lang.String");
    }

    /* JADX WARNING: type inference failed for: r8v7 */
    /* JADX WARNING: type inference failed for: r8v10, types: [java.io.FileInputStream] */
    /* JADX WARNING: type inference failed for: r8v12 */
    /* JADX WARNING: type inference failed for: r8v14, types: [java.lang.CharSequence] */
    /* JADX WARNING: type inference failed for: r8v29 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x009e A[SYNTHETIC, Splitter:B:44:0x009e] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00a8 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00a9 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00ac A[SYNTHETIC, Splitter:B:54:0x00ac] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static java.lang.String getOmcInfoVersion(java.lang.String r7, int r8) {
        /*
            boolean r0 = com.sec.internal.helper.OmcCode.isOmcModel()
            if (r0 != 0) goto L_0x0007
            return r7
        L_0x0007:
            java.lang.String r8 = com.sec.internal.helper.OmcCode.getOmcNwPath(r8)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r8)
            java.lang.String r8 = "/omc.info"
            r0.append(r8)
            java.lang.String r8 = r0.toString()
            java.io.File r0 = new java.io.File
            r0.<init>(r8)
            boolean r8 = r0.exists()
            java.lang.String r1 = "CscParser"
            if (r8 == 0) goto L_0x00b0
            boolean r8 = r0.canRead()
            if (r8 != 0) goto L_0x0031
            goto L_0x00b0
        L_0x0031:
            r8 = 0
            org.xmlpull.v1.XmlPullParserFactory r2 = org.xmlpull.v1.XmlPullParserFactory.newInstance()     // Catch:{ IOException | XmlPullParserException -> 0x0096 }
            r3 = 1
            r2.setNamespaceAware(r3)     // Catch:{ IOException | XmlPullParserException -> 0x0096 }
            org.xmlpull.v1.XmlPullParser r2 = r2.newPullParser()     // Catch:{ IOException | XmlPullParserException -> 0x0096 }
            java.io.FileInputStream r4 = new java.io.FileInputStream     // Catch:{ IOException | XmlPullParserException -> 0x0096 }
            r4.<init>(r0)     // Catch:{ IOException | XmlPullParserException -> 0x0096 }
            r2.setInput(r4, r8)     // Catch:{ IOException | XmlPullParserException -> 0x008f, all -> 0x008c }
            r0 = r8
        L_0x0047:
            int r5 = r2.getEventType()     // Catch:{ IOException | XmlPullParserException -> 0x008f, all -> 0x008c }
            if (r5 == r3) goto L_0x0088
            r6 = 2
            if (r5 == r6) goto L_0x0074
            r6 = 4
            if (r5 == r6) goto L_0x0054
            goto L_0x0078
        L_0x0054:
            java.lang.String r5 = r2.getText()     // Catch:{ IOException | XmlPullParserException -> 0x008f, all -> 0x008c }
            boolean r6 = android.text.TextUtils.isEmpty(r5)     // Catch:{ IOException | XmlPullParserException -> 0x008f, all -> 0x008c }
            if (r6 != 0) goto L_0x0071
            boolean r6 = android.text.TextUtils.isEmpty(r8)     // Catch:{ IOException | XmlPullParserException -> 0x008f, all -> 0x008c }
            if (r6 == 0) goto L_0x0071
            java.lang.String r6 = "version"
            boolean r0 = r6.equals(r0)     // Catch:{ IOException | XmlPullParserException -> 0x008f, all -> 0x008c }
            if (r0 == 0) goto L_0x0071
            java.lang.String r8 = r5.trim()     // Catch:{ IOException | XmlPullParserException -> 0x008f, all -> 0x008c }
        L_0x0071:
            java.lang.String r0 = ""
            goto L_0x0078
        L_0x0074:
            java.lang.String r0 = r2.getName()     // Catch:{ IOException | XmlPullParserException -> 0x008f, all -> 0x008c }
        L_0x0078:
            boolean r5 = android.text.TextUtils.isEmpty(r8)     // Catch:{ IOException | XmlPullParserException -> 0x008f, all -> 0x008c }
            if (r5 != 0) goto L_0x0084
            java.lang.String r0 = "Found OMC Version"
            android.util.Log.d(r1, r0)     // Catch:{ IOException | XmlPullParserException -> 0x008f, all -> 0x008c }
            goto L_0x0088
        L_0x0084:
            r2.next()     // Catch:{ IOException | XmlPullParserException -> 0x008f, all -> 0x008c }
            goto L_0x0047
        L_0x0088:
            r4.close()     // Catch:{ IOException -> 0x00a2 }
            goto L_0x00a2
        L_0x008c:
            r7 = move-exception
            r8 = r4
            goto L_0x00aa
        L_0x008f:
            r0 = move-exception
            r1 = r0
            r0 = r8
            r8 = r4
            goto L_0x0099
        L_0x0094:
            r7 = move-exception
            goto L_0x00aa
        L_0x0096:
            r0 = move-exception
            r1 = r0
            r0 = r8
        L_0x0099:
            r1.printStackTrace()     // Catch:{ all -> 0x0094 }
            if (r8 == 0) goto L_0x00a1
            r8.close()     // Catch:{ IOException -> 0x00a1 }
        L_0x00a1:
            r8 = r0
        L_0x00a2:
            boolean r0 = android.text.TextUtils.isEmpty(r8)
            if (r0 == 0) goto L_0x00a9
            return r7
        L_0x00a9:
            return r8
        L_0x00aa:
            if (r8 == 0) goto L_0x00af
            r8.close()     // Catch:{ IOException -> 0x00af }
        L_0x00af:
            throw r7
        L_0x00b0:
            java.lang.String r8 = "Can't read OMC Version"
            android.util.Log.e(r1, r8)
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.CscParser.getOmcInfoVersion(java.lang.String, int):java.lang.String");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x010a, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized boolean isCscChanged(android.content.Context r13, int r14) {
        /*
            java.lang.Class<com.sec.internal.ims.util.CscParser> r0 = com.sec.internal.ims.util.CscParser.class
            monitor-enter(r0)
            r1 = 0
            if (r13 == 0) goto L_0x0109
            if (r14 < 0) goto L_0x0109
            boolean[] r2 = sCscChangeChecked     // Catch:{ all -> 0x0106 }
            int r3 = r2.length     // Catch:{ all -> 0x0106 }
            if (r14 < r3) goto L_0x000f
            goto L_0x0109
        L_0x000f:
            boolean r2 = r2[r14]     // Catch:{ all -> 0x0106 }
            if (r2 == 0) goto L_0x0015
            monitor-exit(r0)
            return r1
        L_0x0015:
            java.lang.String r2 = "CSC_INFO_PREF"
            android.content.SharedPreferences r13 = com.sec.internal.helper.ImsSharedPrefHelper.getSharedPref(r14, r13, r2, r1, r1)     // Catch:{ all -> 0x0106 }
            java.lang.String r2 = "csc.key.edition"
            java.lang.String r3 = "unknown"
            java.lang.String r2 = r13.getString(r2, r3)     // Catch:{ all -> 0x0106 }
            java.lang.String r3 = "csc.key.version"
            java.lang.String r4 = "unknown"
            java.lang.String r3 = r13.getString(r3, r4)     // Catch:{ all -> 0x0106 }
            java.lang.String r4 = "csc.key.salescode"
            java.lang.String r5 = "unknown"
            java.lang.String r4 = r13.getString(r4, r5)     // Catch:{ all -> 0x0106 }
            java.lang.String r5 = "omc.key.version"
            java.lang.String r6 = "unknown"
            java.lang.String r5 = r13.getString(r5, r6)     // Catch:{ all -> 0x0106 }
            java.lang.String r6 = getCscEdition(r14)     // Catch:{ all -> 0x0106 }
            java.lang.String r7 = getCscVersion(r3)     // Catch:{ all -> 0x0106 }
            java.lang.String r8 = getCscSalesCode(r14)     // Catch:{ all -> 0x0106 }
            java.lang.String r9 = getOmcInfoVersion(r5, r14)     // Catch:{ all -> 0x0106 }
            java.lang.String r10 = "CscParser"
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x0106 }
            r11.<init>()     // Catch:{ all -> 0x0106 }
            java.lang.String r12 = "old edition : "
            r11.append(r12)     // Catch:{ all -> 0x0106 }
            r11.append(r2)     // Catch:{ all -> 0x0106 }
            java.lang.String r12 = " new edition : "
            r11.append(r12)     // Catch:{ all -> 0x0106 }
            r11.append(r6)     // Catch:{ all -> 0x0106 }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x0106 }
            android.util.Log.d(r10, r11)     // Catch:{ all -> 0x0106 }
            java.lang.String r10 = "CscParser"
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x0106 }
            r11.<init>()     // Catch:{ all -> 0x0106 }
            java.lang.String r12 = "old csc version : "
            r11.append(r12)     // Catch:{ all -> 0x0106 }
            r11.append(r3)     // Catch:{ all -> 0x0106 }
            java.lang.String r12 = " new csc version : "
            r11.append(r12)     // Catch:{ all -> 0x0106 }
            r11.append(r7)     // Catch:{ all -> 0x0106 }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x0106 }
            android.util.Log.d(r10, r11)     // Catch:{ all -> 0x0106 }
            java.lang.String r10 = "CscParser"
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x0106 }
            r11.<init>()     // Catch:{ all -> 0x0106 }
            java.lang.String r12 = "old salescode : "
            r11.append(r12)     // Catch:{ all -> 0x0106 }
            r11.append(r4)     // Catch:{ all -> 0x0106 }
            java.lang.String r12 = " new salescode : "
            r11.append(r12)     // Catch:{ all -> 0x0106 }
            r11.append(r8)     // Catch:{ all -> 0x0106 }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x0106 }
            android.util.Log.d(r10, r11)     // Catch:{ all -> 0x0106 }
            java.lang.String r10 = "CscParser"
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x0106 }
            r11.<init>()     // Catch:{ all -> 0x0106 }
            java.lang.String r12 = "old omc version : "
            r11.append(r12)     // Catch:{ all -> 0x0106 }
            r11.append(r5)     // Catch:{ all -> 0x0106 }
            java.lang.String r12 = " new omc version : "
            r11.append(r12)     // Catch:{ all -> 0x0106 }
            r11.append(r9)     // Catch:{ all -> 0x0106 }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x0106 }
            android.util.Log.d(r10, r11)     // Catch:{ all -> 0x0106 }
            boolean[] r10 = sCscChangeChecked     // Catch:{ all -> 0x0106 }
            r11 = 1
            r10[r14] = r11     // Catch:{ all -> 0x0106 }
            boolean r14 = android.text.TextUtils.equals(r2, r6)     // Catch:{ all -> 0x0106 }
            if (r14 == 0) goto L_0x00e6
            boolean r14 = android.text.TextUtils.equals(r3, r7)     // Catch:{ all -> 0x0106 }
            if (r14 == 0) goto L_0x00e6
            boolean r14 = android.text.TextUtils.equals(r4, r8)     // Catch:{ all -> 0x0106 }
            if (r14 == 0) goto L_0x00e6
            boolean r14 = android.text.TextUtils.equals(r5, r9)     // Catch:{ all -> 0x0106 }
            if (r14 == 0) goto L_0x00e6
            monitor-exit(r0)
            return r1
        L_0x00e6:
            android.content.SharedPreferences$Editor r13 = r13.edit()     // Catch:{ all -> 0x0106 }
            r13.clear()     // Catch:{ all -> 0x0106 }
            java.lang.String r14 = "csc.key.edition"
            r13.putString(r14, r6)     // Catch:{ all -> 0x0106 }
            java.lang.String r14 = "csc.key.version"
            r13.putString(r14, r7)     // Catch:{ all -> 0x0106 }
            java.lang.String r14 = "csc.key.salescode"
            r13.putString(r14, r8)     // Catch:{ all -> 0x0106 }
            java.lang.String r14 = "omc.key.version"
            r13.putString(r14, r9)     // Catch:{ all -> 0x0106 }
            r13.apply()     // Catch:{ all -> 0x0106 }
            monitor-exit(r0)
            return r11
        L_0x0106:
            r13 = move-exception
            monitor-exit(r0)
            throw r13
        L_0x0109:
            monitor-exit(r0)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.CscParser.isCscChanged(android.content.Context, int):boolean");
    }

    private static String getFieldFromCsc(int i, String str) {
        FileInputStream cscFile = getCscFile(i);
        XmlPullParser cscCustomerParser = getCscCustomerParser(cscFile);
        if (cscCustomerParser == null) {
            Log.e(LOG_TAG, "XmlPullParser is null");
            closeFileInputStream(cscFile);
            return null;
        } else if (!XmlUtils.search(cscCustomerParser, str)) {
            Log.e(LOG_TAG, "can not find " + str);
            closeFileInputStream(cscFile);
            return null;
        } else {
            while (cscCustomerParser.next() != 4) {
                try {
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    closeFileInputStream(cscFile);
                }
            }
            return cscCustomerParser.getText();
        }
    }

    static String getCscSalesCode(int i) {
        return getFieldFromCsc(i, SALES_CODE_PATH);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:169:0x02d5, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x014a, code lost:
        r1 = "";
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x02d5 A[ExcHandler: IOException | XmlPullParserException (e java.lang.Throwable), PHI: r18 
      PHI: (r18v10 java.io.FileInputStream) = (r18v11 java.io.FileInputStream), (r18v11 java.io.FileInputStream), (r18v11 java.io.FileInputStream), (r18v11 java.io.FileInputStream), (r18v12 java.io.FileInputStream) binds: [B:81:0x014e, B:120:0x01e6, B:71:0x012e, B:76:0x013d, B:23:0x0077] A[DONT_GENERATE, DONT_INLINE], Splitter:B:23:0x0077] */
    /* JADX WARNING: Removed duplicated region for block: B:186:0x0300 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0301 A[RETURN] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.util.List<java.lang.String> getNetworkNames(java.lang.String r19, java.lang.String r20, java.lang.String r21, java.lang.String r22, java.lang.String r23, int r24, boolean r25) {
        /*
            r0 = r20
            java.io.FileInputStream r1 = getCscFile(r24)
            org.xmlpull.v1.XmlPullParser r2 = getCscCustomerParser(r1)
            java.lang.String r3 = "CscParser"
            r4 = 0
            if (r2 != 0) goto L_0x0018
            java.lang.String r0 = "XmlPullParser is null"
            android.util.Log.e(r3, r0)
            closeFileInputStream(r1)
            return r4
        L_0x0018:
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>()
            java.util.ArrayList r6 = new java.util.ArrayList
            r6.<init>()
            java.lang.String r7 = "CustomerData.GeneralInfo.NetworkInfo"
            boolean r7 = com.sec.internal.helper.XmlUtils.search(r2, r7)
            if (r7 != 0) goto L_0x0033
            java.lang.String r0 = "can not find CustomerData.GeneralInfo.NetworkInfo"
            android.util.Log.e(r3, r0)
            closeFileInputStream(r1)
            return r4
        L_0x0033:
            java.lang.String r3 = ""
            if (r0 == 0) goto L_0x004b
            int r7 = r20.length()
            int r8 = r19.length()
            if (r7 > r8) goto L_0x0042
            goto L_0x004b
        L_0x0042:
            int r7 = r19.length()
            java.lang.String r0 = r0.substring(r7)
            goto L_0x004c
        L_0x004b:
            r0 = r3
        L_0x004c:
            r7 = 1
            r9 = r22
            r10 = r23
            r20 = r3
            r15 = r20
            r16 = r15
            r17 = r16
            r11 = r4
            r12 = r11
            r13 = r12
            r14 = 0
            r4 = r21
        L_0x005f:
            int r8 = r2.next()     // Catch:{ IOException | XmlPullParserException -> 0x02ed, all -> 0x02e9 }
            if (r8 == r7) goto L_0x02e6
            r7 = 2
            if (r8 != r7) goto L_0x011b
            java.lang.String r7 = r2.getName()     // Catch:{ IOException | XmlPullParserException -> 0x02ed, all -> 0x02e9 }
            java.lang.String r8 = "MCCMNC"
            boolean r8 = r8.equalsIgnoreCase(r7)     // Catch:{ IOException | XmlPullParserException -> 0x02ed, all -> 0x02e9 }
            r18 = r1
            r1 = 4
            if (r8 == 0) goto L_0x0084
        L_0x0077:
            int r7 = r2.next()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 == r1) goto L_0x007e
            goto L_0x0077
        L_0x007e:
            java.lang.String r11 = r2.getText()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            goto L_0x02e1
        L_0x0084:
            java.lang.String r8 = "SPCode"
            boolean r8 = r8.equalsIgnoreCase(r7)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r8 == 0) goto L_0x009f
        L_0x008c:
            int r7 = r2.next()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 == r1) goto L_0x0093
            goto L_0x008c
        L_0x0093:
            java.lang.String r1 = r2.getText()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            java.lang.String r1 = r1.toUpperCase()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            r20 = r1
            goto L_0x02e1
        L_0x009f:
            java.lang.String r8 = "CodeType"
            boolean r8 = r8.equalsIgnoreCase(r7)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r8 == 0) goto L_0x00bf
        L_0x00a7:
            int r7 = r2.next()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 == r1) goto L_0x00ae
            goto L_0x00a7
        L_0x00ae:
            java.lang.String r1 = "HEX"
            java.lang.String r7 = r2.getText()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            boolean r1 = r1.equalsIgnoreCase(r7)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r1 == 0) goto L_0x02d7
            r1 = r18
            r7 = 1
            r14 = 1
            goto L_0x005f
        L_0x00bf:
            java.lang.String r8 = "SubsetCode"
            boolean r8 = r8.equalsIgnoreCase(r7)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r8 == 0) goto L_0x00d8
        L_0x00c7:
            int r7 = r2.next()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 == r1) goto L_0x00ce
            goto L_0x00c7
        L_0x00ce:
            java.lang.String r1 = r2.getText()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            java.lang.String r15 = r1.toUpperCase()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            goto L_0x02e1
        L_0x00d8:
            java.lang.String r8 = "Gid2"
            boolean r8 = r8.equalsIgnoreCase(r7)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r8 == 0) goto L_0x00f1
        L_0x00e0:
            int r7 = r2.next()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 == r1) goto L_0x00e7
            goto L_0x00e0
        L_0x00e7:
            java.lang.String r1 = r2.getText()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            java.lang.String r16 = r1.toUpperCase()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            goto L_0x02e1
        L_0x00f1:
            java.lang.String r8 = "Spname"
            boolean r8 = r8.equalsIgnoreCase(r7)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r8 == 0) goto L_0x0106
        L_0x00f9:
            int r7 = r2.next()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 == r1) goto L_0x0100
            goto L_0x00f9
        L_0x0100:
            java.lang.String r17 = r2.getText()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            goto L_0x02e1
        L_0x0106:
            java.lang.String r8 = "NetworkName"
            boolean r7 = r8.equalsIgnoreCase(r7)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 == 0) goto L_0x02d7
        L_0x010e:
            int r7 = r2.next()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 == r1) goto L_0x0115
            goto L_0x010e
        L_0x0115:
            java.lang.String r12 = r2.getText()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            goto L_0x02e1
        L_0x011b:
            r18 = r1
            r1 = 3
            if (r8 != r1) goto L_0x02d7
            java.lang.String r1 = "NetworkInfo"
            java.lang.String r7 = r2.getName()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            boolean r1 = r1.equalsIgnoreCase(r7)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r1 == 0) goto L_0x02c2
            if (r14 != 0) goto L_0x014c
            int r1 = java.lang.Integer.parseInt(r15)     // Catch:{ NumberFormatException -> 0x013c, IOException | XmlPullParserException -> 0x02d5 }
            java.lang.String r1 = java.lang.Integer.toHexString(r1)     // Catch:{ NumberFormatException -> 0x013c, IOException | XmlPullParserException -> 0x02d5 }
            java.lang.String r1 = r1.toUpperCase()     // Catch:{ NumberFormatException -> 0x013c, IOException | XmlPullParserException -> 0x02d5 }
            r15 = r1
            goto L_0x013d
        L_0x013c:
            r15 = r3
        L_0x013d:
            int r1 = java.lang.Integer.parseInt(r16)     // Catch:{ NumberFormatException -> 0x014a, IOException | XmlPullParserException -> 0x02d5 }
            java.lang.String r1 = java.lang.Integer.toHexString(r1)     // Catch:{ NumberFormatException -> 0x014a, IOException | XmlPullParserException -> 0x02d5 }
            java.lang.String r16 = r1.toUpperCase()     // Catch:{ NumberFormatException -> 0x014a, IOException | XmlPullParserException -> 0x02d5 }
            goto L_0x014c
        L_0x014a:
            r1 = r3
            goto L_0x014e
        L_0x014c:
            r1 = r16
        L_0x014e:
            boolean r7 = android.text.TextUtils.isEmpty(r12)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 != 0) goto L_0x02b7
            boolean r7 = android.text.TextUtils.isEmpty(r11)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 == 0) goto L_0x015c
            goto L_0x02b7
        L_0x015c:
            java.lang.String r7 = "00101"
            boolean r7 = r7.equals(r11)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 != 0) goto L_0x02b7
            java.lang.String r7 = "001001"
            boolean r7 = r7.equals(r11)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 != 0) goto L_0x02b7
            java.lang.String r7 = "001010"
            boolean r7 = r7.equals(r11)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 != 0) goto L_0x02b7
            java.lang.String r7 = "00101f"
            boolean r7 = r7.equals(r11)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 != 0) goto L_0x02b7
            java.lang.String r7 = "99999"
            boolean r7 = r7.equals(r11)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r7 == 0) goto L_0x0186
            goto L_0x02b7
        L_0x0186:
            r7 = r19
            boolean r8 = r7.equalsIgnoreCase(r11)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r8 == 0) goto L_0x02a1
            if (r25 == 0) goto L_0x0195
            r5.add(r12)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            goto L_0x02a1
        L_0x0195:
            boolean r8 = android.text.TextUtils.isEmpty(r20)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r8 != 0) goto L_0x01d8
            boolean r8 = android.text.TextUtils.isEmpty(r0)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r8 != 0) goto L_0x01d8
            if (r13 != 0) goto L_0x01bb
            com.sec.internal.constants.Mno$Country r8 = com.sec.internal.constants.Mno.Country.CANADA     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            java.lang.String r8 = r8.getCountryIso()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            java.lang.String r11 = "CustomerData.GeneralInfo.CountryISO"
            r14 = r24
            java.lang.String r11 = getFieldFromCsc(r14, r11)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            boolean r8 = r8.equalsIgnoreCase(r11)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            java.lang.Boolean r8 = java.lang.Boolean.valueOf(r8)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            r13 = r8
            goto L_0x01bd
        L_0x01bb:
            r14 = r24
        L_0x01bd:
            boolean r8 = r13.booleanValue()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r8 == 0) goto L_0x01c7
            r8 = r20
            r11 = 1
            goto L_0x01ca
        L_0x01c7:
            r8 = r20
            r11 = 0
        L_0x01ca:
            boolean r11 = r0.startsWith(r8, r11)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r11 == 0) goto L_0x01dc
            r5.clear()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            r5.add(r12)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            goto L_0x02f3
        L_0x01d8:
            r8 = r20
            r14 = r24
        L_0x01dc:
            boolean r11 = android.text.TextUtils.isEmpty(r15)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            r20 = r0
            java.lang.String r0 = "^0+(?!$)"
            if (r11 != 0) goto L_0x021c
            boolean r11 = android.text.TextUtils.isEmpty(r4)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r11 != 0) goto L_0x021c
            java.lang.String r15 = r15.replaceFirst(r0, r3)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            java.lang.String r4 = r4.replaceFirst(r0, r3)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            boolean r11 = android.text.TextUtils.isEmpty(r15)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r11 != 0) goto L_0x0218
            boolean r11 = android.text.TextUtils.isEmpty(r4)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r11 != 0) goto L_0x0218
            java.lang.String r11 = r4.toUpperCase()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            r21 = r4
            java.lang.String r4 = r15.toUpperCase()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            boolean r4 = r11.startsWith(r4)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r4 == 0) goto L_0x021a
            r5.clear()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            r5.add(r12)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            goto L_0x02f3
        L_0x0218:
            r21 = r4
        L_0x021a:
            r4 = r21
        L_0x021c:
            boolean r11 = android.text.TextUtils.isEmpty(r1)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r11 != 0) goto L_0x0252
            boolean r11 = android.text.TextUtils.isEmpty(r9)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r11 != 0) goto L_0x0252
            java.lang.String r1 = r1.replaceFirst(r0, r3)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            java.lang.String r9 = r9.replaceFirst(r0, r3)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            boolean r0 = android.text.TextUtils.isEmpty(r1)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r0 != 0) goto L_0x0252
            boolean r0 = android.text.TextUtils.isEmpty(r9)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r0 != 0) goto L_0x0252
            java.lang.String r0 = r9.toUpperCase()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            java.lang.String r11 = r1.toUpperCase()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            boolean r0 = r0.startsWith(r11)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r0 == 0) goto L_0x0252
            r5.clear()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            r5.add(r12)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            goto L_0x02f3
        L_0x0252:
            boolean r0 = android.text.TextUtils.isEmpty(r17)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r0 != 0) goto L_0x0282
            boolean r0 = android.text.TextUtils.isEmpty(r10)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r0 != 0) goto L_0x0282
            java.lang.String r0 = r17.trim()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            java.lang.String r10 = r10.trim()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            boolean r11 = android.text.TextUtils.isEmpty(r0)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r11 != 0) goto L_0x0280
            boolean r11 = android.text.TextUtils.isEmpty(r10)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r11 != 0) goto L_0x0280
            boolean r11 = r10.equalsIgnoreCase(r0)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r11 == 0) goto L_0x0280
            r5.clear()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            r5.add(r12)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            goto L_0x02f3
        L_0x0280:
            r17 = r0
        L_0x0282:
            boolean r0 = android.text.TextUtils.isEmpty(r8)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r0 == 0) goto L_0x02a5
            boolean r0 = android.text.TextUtils.isEmpty(r15)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r0 == 0) goto L_0x02a5
            boolean r0 = android.text.TextUtils.isEmpty(r1)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r0 == 0) goto L_0x02a5
            r5.add(r12)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            boolean r0 = android.text.TextUtils.isEmpty(r17)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r0 == 0) goto L_0x02a5
            r6.add(r12)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            goto L_0x02a5
        L_0x02a1:
            r14 = r24
            r20 = r0
        L_0x02a5:
            r0 = r20
            r20 = r3
            r11 = r20
            r12 = r11
            r15 = r12
            r16 = r15
            r17 = r16
            r1 = r18
            r7 = 1
            r14 = 0
            goto L_0x005f
        L_0x02b7:
            r7 = r19
            r8 = r20
            r20 = r0
            r0 = r20
            r16 = r1
            goto L_0x02df
        L_0x02c2:
            r7 = r19
            r8 = r20
            r20 = r0
            java.lang.String r0 = "GeneralInfo"
            java.lang.String r1 = r2.getName()     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            boolean r0 = r0.equalsIgnoreCase(r1)     // Catch:{ IOException | XmlPullParserException -> 0x02d5 }
            if (r0 == 0) goto L_0x02dd
            goto L_0x02f3
        L_0x02d5:
            r0 = move-exception
            goto L_0x02f0
        L_0x02d7:
            r7 = r19
            r8 = r20
            r20 = r0
        L_0x02dd:
            r0 = r20
        L_0x02df:
            r20 = r8
        L_0x02e1:
            r1 = r18
            r7 = 1
            goto L_0x005f
        L_0x02e6:
            r18 = r1
            goto L_0x02f3
        L_0x02e9:
            r0 = move-exception
            r18 = r1
            goto L_0x0303
        L_0x02ed:
            r0 = move-exception
            r18 = r1
        L_0x02f0:
            r0.printStackTrace()     // Catch:{ all -> 0x0302 }
        L_0x02f3:
            closeFileInputStream(r18)
            if (r25 == 0) goto L_0x02f9
            return r5
        L_0x02f9:
            int r0 = r5.size()
            r1 = 1
            if (r0 <= r1) goto L_0x0301
            return r6
        L_0x0301:
            return r5
        L_0x0302:
            r0 = move-exception
        L_0x0303:
            closeFileInputStream(r18)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.CscParser.getNetworkNames(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, boolean):java.util.List");
    }

    public static synchronized ContentValues getCscImsSetting(String str, int i) {
        ContentValues cscImsSetting;
        synchronized (CscParser.class) {
            cscImsSetting = getCscImsSetting(getNetworkNames(str, "", "", "", "", i, true), i);
        }
        return cscImsSetting;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        closeFileInputStream(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00a0, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00a9, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized android.content.ContentValues getCscImsSetting(java.util.List<java.lang.String> r5, int r6) {
        /*
            java.lang.Class<com.sec.internal.ims.util.CscParser> r0 = com.sec.internal.ims.util.CscParser.class
            monitor-enter(r0)
            r1 = 0
            if (r5 == 0) goto L_0x00a8
            int r2 = r5.size()     // Catch:{ all -> 0x00a5 }
            if (r2 != 0) goto L_0x000e
            goto L_0x00a8
        L_0x000e:
            java.io.FileInputStream r6 = getCscFile(r6)     // Catch:{ all -> 0x00a5 }
            org.xmlpull.v1.XmlPullParser r2 = getCscCustomerParser(r6)     // Catch:{ all -> 0x00a5 }
            if (r2 != 0) goto L_0x0024
            java.lang.String r5 = "CscParser"
            java.lang.String r2 = "XmlPullParser is null"
            android.util.Log.e(r5, r2)     // Catch:{ all -> 0x00a5 }
            closeFileInputStream(r6)     // Catch:{ all -> 0x00a5 }
            monitor-exit(r0)
            return r1
        L_0x0024:
            java.lang.String r3 = "CustomerData.Settings.IMSSetting.NbSetting"
            boolean r3 = com.sec.internal.helper.XmlUtils.search(r2, r3)     // Catch:{ all -> 0x00a5 }
            if (r3 != 0) goto L_0x0038
            java.lang.String r5 = "CscParser"
            java.lang.String r2 = "can not find CustomerData.Settings.IMSSetting.NbSetting"
            android.util.Log.e(r5, r2)     // Catch:{ all -> 0x00a5 }
            closeFileInputStream(r6)     // Catch:{ all -> 0x00a5 }
            monitor-exit(r0)
            return r1
        L_0x0038:
            int r3 = r2.getEventType()     // Catch:{ IOException | XmlPullParserException -> 0x0083 }
            r4 = 1
            if (r3 == r4) goto L_0x007d
            r4 = 2
            if (r3 != r4) goto L_0x0079
            java.lang.String r3 = "Setting"
            java.lang.String r4 = r2.getName()     // Catch:{ IOException | XmlPullParserException -> 0x0083 }
            boolean r3 = r3.equalsIgnoreCase(r4)     // Catch:{ IOException | XmlPullParserException -> 0x0083 }
            if (r3 == 0) goto L_0x0079
            android.content.ContentValues r3 = getSetting(r2)     // Catch:{ IOException | XmlPullParserException -> 0x0083 }
            java.lang.String r4 = "NetworkName"
            java.lang.String r4 = r3.getAsString(r4)     // Catch:{ IOException | XmlPullParserException -> 0x0083 }
            boolean r4 = r5.contains(r4)     // Catch:{ IOException | XmlPullParserException -> 0x0083 }
            if (r4 == 0) goto L_0x0079
            java.lang.String r5 = "CscParser"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ IOException | XmlPullParserException -> 0x0083 }
            r2.<init>()     // Catch:{ IOException | XmlPullParserException -> 0x0083 }
            java.lang.String r4 = "csc ims setting: "
            r2.append(r4)     // Catch:{ IOException | XmlPullParserException -> 0x0083 }
            r2.append(r3)     // Catch:{ IOException | XmlPullParserException -> 0x0083 }
            java.lang.String r2 = r2.toString()     // Catch:{ IOException | XmlPullParserException -> 0x0083 }
            android.util.Log.d(r5, r2)     // Catch:{ IOException | XmlPullParserException -> 0x0083 }
            closeFileInputStream(r6)     // Catch:{ all -> 0x00a5 }
            monitor-exit(r0)
            return r3
        L_0x0079:
            r2.next()     // Catch:{ IOException | XmlPullParserException -> 0x0083 }
            goto L_0x0038
        L_0x007d:
            closeFileInputStream(r6)     // Catch:{ all -> 0x00a5 }
            goto L_0x009f
        L_0x0081:
            r5 = move-exception
            goto L_0x00a1
        L_0x0083:
            r5 = move-exception
            java.lang.String r2 = "CscParser"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0081 }
            r3.<init>()     // Catch:{ all -> 0x0081 }
            java.lang.String r4 = "getCscImsSetting: "
            r3.append(r4)     // Catch:{ all -> 0x0081 }
            java.lang.String r5 = r5.getMessage()     // Catch:{ all -> 0x0081 }
            r3.append(r5)     // Catch:{ all -> 0x0081 }
            java.lang.String r5 = r3.toString()     // Catch:{ all -> 0x0081 }
            android.util.Log.e(r2, r5)     // Catch:{ all -> 0x0081 }
            goto L_0x007d
        L_0x009f:
            monitor-exit(r0)
            return r1
        L_0x00a1:
            closeFileInputStream(r6)     // Catch:{ all -> 0x00a5 }
            throw r5     // Catch:{ all -> 0x00a5 }
        L_0x00a5:
            r5 = move-exception
            monitor-exit(r0)
            throw r5
        L_0x00a8:
            monitor-exit(r0)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.CscParser.getCscImsSetting(java.util.List, int):android.content.ContentValues");
    }

    private static ContentValues getSetting(XmlPullParser xmlPullParser) {
        ContentValues contentValues = new ContentValues();
        String str = null;
        while (true) {
            try {
                int eventType = xmlPullParser.getEventType();
                if (eventType == 1) {
                    break;
                }
                if (eventType == 2) {
                    str = xmlPullParser.getName();
                } else if (eventType == 3) {
                    if ("Setting".equalsIgnoreCase(xmlPullParser.getName())) {
                        break;
                    }
                } else if (eventType == 4) {
                    String text = xmlPullParser.getText();
                    if (!TextUtils.isEmpty(text) && text.trim().length() > 0) {
                        contentValues.put(str, text);
                    }
                }
                xmlPullParser.next();
            } catch (IOException | XmlPullParserException e) {
                Log.e(LOG_TAG, "getSetting: " + e.getMessage());
            }
        }
        return contentValues;
    }
}
