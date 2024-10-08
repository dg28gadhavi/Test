package com.sec.internal.ims.cmstore;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.ims.cmstore.servicecontainer.CentralMsgStoreInterface;
import com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

public class MStoreDebugTool {
    public static final boolean DEBUG_ON = false;
    public static final String DEFAULT_LAB_ENTITLEMENT = "https://eas3.msg.lab.t-mobile.com/generic_devices";
    public static final String DEFAULT_PRO_ENTITLEMENT = "https://eas3.msg.t-mobile.com/generic_devices";
    public static final String DEFAULT_STG_BSF = "https://bsf.sipgeo.t-mobile.com:443/";
    public static final String DEFAULT_STG_ENTITLEMENT = "https://easstg1.msg.t-mobile.com/generic_devices";
    public static final Uri ES_AUTHORITY_URI = Uri.parse("content://com.samsung.ims.entitlementconfig.provider");
    public static final String LOG_TAG = "MStoreDebugTool";
    public static int SIMTYPE;
    private static MStoreDebugTool sInstance;
    public String BSF_IP;
    /* access modifiers changed from: private */
    public CentralMsgStoreInterface mCentralMsgStoreWrapper;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public NetAPIWorkingStatusController mNetAPIWorkingController;
    BroadcastReceiver mVVMIntentReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Can't wrap try/catch for region: R(5:31|32|33|34|51) */
        /* JADX WARNING: Can't wrap try/catch for region: R(5:35|36|37|38|53) */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x00e2, code lost:
            if (r2.equals("downloadMessage") != false) goto L_0x00e6;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:50:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
            return;
         */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x0111 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x012c */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x00ea  */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x011a A[SYNTHETIC, Splitter:B:35:0x011a] */
        /* JADX WARNING: Unknown top exception splitter block from list: {B:33:0x0111=Splitter:B:33:0x0111, B:37:0x012c=Splitter:B:37:0x012c} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(android.content.Context r8, android.content.Intent r9) {
            /*
                r7 = this;
                java.lang.String r8 = com.sec.internal.ims.cmstore.MStoreDebugTool.LOG_TAG
                java.lang.StringBuilder r0 = new java.lang.StringBuilder
                r0.<init>()
                java.lang.String r1 = "mVVMIntentReceiver: onReceive "
                r0.append(r1)
                r0.append(r9)
                java.lang.String r0 = r0.toString()
                android.util.Log.d(r8, r0)
                com.sec.internal.ims.cmstore.MStoreDebugTool r0 = com.sec.internal.ims.cmstore.MStoreDebugTool.this
                com.sec.internal.ims.cmstore.NetAPIWorkingStatusController r0 = r0.mNetAPIWorkingController
                boolean r0 = r0.getCmsProfileEnabled()
                if (r0 == 0) goto L_0x0149
                java.lang.String r8 = r9.getAction()
                r8.hashCode()
                java.lang.String r0 = "com.shiqg.action.VVM"
                boolean r0 = r8.equals(r0)
                if (r0 != 0) goto L_0x013f
                java.lang.String r0 = "com.shiqg.action.TESTAPI"
                boolean r8 = r8.equals(r0)
                if (r8 != 0) goto L_0x003b
                goto L_0x014e
            L_0x003b:
                org.json.JSONArray r8 = new org.json.JSONArray
                r8.<init>()
                r0 = 0
                com.sec.internal.ims.cmstore.MessageStoreClient r1 = com.sec.internal.ims.cmstore.CloudMessageService.getClientByID(r0)
                com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r1 = r1.getPrerenceManager()
                java.lang.String r1 = r1.getUserTelCtn()
                com.sec.internal.ims.cmstore.MStoreDebugTool r2 = com.sec.internal.ims.cmstore.MStoreDebugTool.this
                android.content.Context r2 = r2.mContext
                java.lang.String r2 = com.sec.internal.ims.cmstore.utils.Util.getSimCountryCode(r2, r0)
                java.lang.String r1 = com.sec.internal.ims.cmstore.utils.Util.getMsisdn(r1, r2)
                java.io.File r2 = new java.io.File
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                r3.<init>()
                com.sec.internal.ims.cmstore.MStoreDebugTool r4 = com.sec.internal.ims.cmstore.MStoreDebugTool.this
                android.content.Context r4 = r4.mContext
                r5 = 0
                java.io.File r4 = r4.getExternalFilesDir(r5)
                r3.append(r4)
                java.lang.String r4 = "/TESTAPI/"
                r3.append(r4)
                java.lang.String r4 = "FileName"
                java.lang.String r4 = r9.getStringExtra(r4)
                r3.append(r4)
                java.lang.String r4 = ".json"
                r3.append(r4)
                java.lang.String r3 = r3.toString()
                r2.<init>(r3)
                boolean r3 = r2.exists()
                if (r3 == 0) goto L_0x014e
                org.json.JSONObject r3 = new org.json.JSONObject     // Catch:{ JSONException -> 0x0134 }
                com.sec.internal.ims.cmstore.MStoreDebugTool r4 = com.sec.internal.ims.cmstore.MStoreDebugTool.this     // Catch:{ JSONException -> 0x0134 }
                java.lang.String r2 = r4.getTextFromSD(r2)     // Catch:{ JSONException -> 0x0134 }
                r3.<init>(r2)     // Catch:{ JSONException -> 0x0134 }
                java.lang.String r2 = "API"
                java.lang.String r2 = r3.getString(r2)     // Catch:{ JSONException -> 0x0134 }
                java.lang.String r4 = "Content"
                org.json.JSONObject r3 = r3.getJSONObject(r4)     // Catch:{ JSONException -> 0x0134 }
                java.lang.String r4 = "preferred_line"
                r3.put(r4, r1)     // Catch:{ JSONException -> 0x0134 }
                r8.put(r3)     // Catch:{ JSONException -> 0x0134 }
                int r1 = r2.hashCode()     // Catch:{ JSONException -> 0x0134 }
                r4 = 1350018655(0x5077a65f, float:1.66195026E10)
                r5 = 2
                r6 = 1
                if (r1 == r4) goto L_0x00dc
                r0 = 1435736346(0x5593991a, float:2.02857219E13)
                if (r1 == r0) goto L_0x00d1
                r0 = 2046097094(0x79f4f6c6, float:1.589907E35)
                if (r1 == r0) goto L_0x00c6
                goto L_0x00e5
            L_0x00c6:
                java.lang.String r0 = "uploadMessage"
                boolean r0 = r2.equals(r0)     // Catch:{ JSONException -> 0x0134 }
                if (r0 == 0) goto L_0x00e5
                r0 = r6
                goto L_0x00e6
            L_0x00d1:
                java.lang.String r0 = "setupSIM"
                boolean r0 = r2.equals(r0)     // Catch:{ JSONException -> 0x0134 }
                if (r0 == 0) goto L_0x00e5
                r0 = r5
                goto L_0x00e6
            L_0x00dc:
                java.lang.String r1 = "downloadMessage"
                boolean r1 = r2.equals(r1)     // Catch:{ JSONException -> 0x0134 }
                if (r1 == 0) goto L_0x00e5
                goto L_0x00e6
            L_0x00e5:
                r0 = -1
            L_0x00e6:
                java.lang.String r1 = "VVMDATA"
                if (r0 == 0) goto L_0x011a
                if (r0 == r6) goto L_0x00ff
                if (r0 == r5) goto L_0x00ef
                goto L_0x014e
            L_0x00ef:
                com.sec.internal.ims.cmstore.MStoreDebugTool r7 = com.sec.internal.ims.cmstore.MStoreDebugTool.this     // Catch:{ JSONException -> 0x0134 }
                java.lang.String r8 = "Type"
                java.lang.String r8 = r9.getStringExtra(r8)     // Catch:{ JSONException -> 0x0134 }
                org.json.JSONObject r8 = r3.getJSONObject(r8)     // Catch:{ JSONException -> 0x0134 }
                r7.setupSim(r8)     // Catch:{ JSONException -> 0x0134 }
                goto L_0x014e
            L_0x00ff:
                com.sec.internal.ims.cmstore.MStoreDebugTool r7 = com.sec.internal.ims.cmstore.MStoreDebugTool.this     // Catch:{ RemoteException -> 0x0111 }
                com.sec.internal.ims.cmstore.servicecontainer.CentralMsgStoreInterface r7 = r7.mCentralMsgStoreWrapper     // Catch:{ RemoteException -> 0x0111 }
                com.sec.ims.ICentralMsgStoreService$Stub r7 = r7.getBinder()     // Catch:{ RemoteException -> 0x0111 }
                java.lang.String r8 = r8.toString()     // Catch:{ RemoteException -> 0x0111 }
                r7.uploadMessage(r1, r8)     // Catch:{ RemoteException -> 0x0111 }
                goto L_0x014e
            L_0x0111:
                java.lang.String r7 = com.sec.internal.ims.cmstore.MStoreDebugTool.LOG_TAG     // Catch:{ JSONException -> 0x0134 }
                java.lang.String r8 = "uploadMessage  error"
                android.util.Log.d(r7, r8)     // Catch:{ JSONException -> 0x0134 }
                goto L_0x014e
            L_0x011a:
                com.sec.internal.ims.cmstore.MStoreDebugTool r7 = com.sec.internal.ims.cmstore.MStoreDebugTool.this     // Catch:{ RemoteException -> 0x012c }
                com.sec.internal.ims.cmstore.servicecontainer.CentralMsgStoreInterface r7 = r7.mCentralMsgStoreWrapper     // Catch:{ RemoteException -> 0x012c }
                com.sec.ims.ICentralMsgStoreService$Stub r7 = r7.getBinder()     // Catch:{ RemoteException -> 0x012c }
                java.lang.String r8 = r8.toString()     // Catch:{ RemoteException -> 0x012c }
                r7.downloadMessage(r1, r8)     // Catch:{ RemoteException -> 0x012c }
                goto L_0x014e
            L_0x012c:
                java.lang.String r7 = com.sec.internal.ims.cmstore.MStoreDebugTool.LOG_TAG     // Catch:{ JSONException -> 0x0134 }
                java.lang.String r8 = "downloadMessage error"
                android.util.Log.d(r7, r8)     // Catch:{ JSONException -> 0x0134 }
                goto L_0x014e
            L_0x0134:
                r7 = move-exception
                java.lang.String r8 = com.sec.internal.ims.cmstore.MStoreDebugTool.LOG_TAG
                java.lang.String r7 = r7.getMessage()
                android.util.Log.e(r8, r7)
                goto L_0x014e
            L_0x013f:
                com.sec.internal.ims.cmstore.MStoreDebugTool r7 = com.sec.internal.ims.cmstore.MStoreDebugTool.this
                com.sec.internal.ims.cmstore.NetAPIWorkingStatusController r7 = r7.mNetAPIWorkingController
                r7.vvmNormalSyncRequest()
                goto L_0x014e
            L_0x0149:
                java.lang.String r7 = "cms profile is not enabled"
                android.util.Log.d(r8, r7)
            L_0x014e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.MStoreDebugTool.AnonymousClass1.onReceive(android.content.Context, android.content.Intent):void");
        }
    };

    public static String replaceDebugImpi(String str) {
        return null;
    }

    public void initDebugInfo() {
    }

    public String replaceDebugBsf(String str) {
        return str;
    }

    public MStoreDebugTool(Context context, NetAPIWorkingStatusController netAPIWorkingStatusController, CentralMsgStoreInterface centralMsgStoreInterface) {
        this.mContext = context;
        this.mNetAPIWorkingController = netAPIWorkingStatusController;
        this.mCentralMsgStoreWrapper = centralMsgStoreInterface;
    }

    public static MStoreDebugTool getInstance(Context context, NetAPIWorkingStatusController netAPIWorkingStatusController, CentralMsgStoreInterface centralMsgStoreInterface) {
        if (sInstance == null) {
            sInstance = new MStoreDebugTool(context, netAPIWorkingStatusController, centralMsgStoreInterface);
        }
        return sInstance;
    }

    public void registerVVMIntentReceiver() {
        Log.d(LOG_TAG, "registerVVMIntentReceiver");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.shiqg.action.VVM");
        intentFilter.addAction("com.shiqg.action.TESTAPI");
        this.mContext.registerReceiver(this.mVVMIntentReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    public String getTextFromSD(File file) {
        BufferedReader bufferedReader;
        String str = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    sb.append(readLine);
                }
                str = sb.toString();
                bufferedReader.close();
                fileInputStream.close();
            } catch (Throwable th) {
                fileInputStream.close();
                throw th;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
        return str;
        throw th;
    }

    private boolean updateEntitlementUrl(String str) {
        String str2 = LOG_TAG;
        Log.d(str2, "updateEntitlementUrl: " + str);
        if (TextUtils.isEmpty(str) || !isValidUrl(str)) {
            Log.e(str2, "updateEntitlementUrl: invalid url");
            return false;
        }
        Uri withAppendedPath = Uri.withAppendedPath(ES_AUTHORITY_URI, "config");
        this.mContext.getContentResolver().update(Uri.parse(Uri.withAppendedPath(withAppendedPath, "entitlement_url") + "?entitlement_url=" + str), new ContentValues(), (String) null, (String[]) null);
        Log.d(str2, "updateEntitlementUrl: update done!");
        return true;
    }

    private static boolean isValidUrl(String str) {
        return Patterns.WEB_URL.matcher(str.toLowerCase()).matches();
    }

    public String getEntitlementUrl(Context context) {
        return NSDSSharedPrefHelper.getEntitlementServerUrl(context, DeviceIdHelper.getDeviceId(context, 0), "http://ses.ericsson-magic.net:10080/generic_devices");
    }

    public void setupSim(JSONObject jSONObject) {
        try {
            String string = jSONObject.has(GlobalSettingsConstants.SS.BSF_IP) ? jSONObject.getString(GlobalSettingsConstants.SS.BSF_IP) : null;
            if (!TextUtils.isEmpty(string)) {
                this.BSF_IP = string;
            }
            if (jSONObject.has("entitlement_url")) {
                String string2 = jSONObject.getString("entitlement_url");
                String str = LOG_TAG;
                Log.d(str, "config entitlement_url: " + string2);
                if (!TextUtils.isEmpty(string2)) {
                    String entitlementUrl = getEntitlementUrl(this.mContext);
                    Log.d(str, "storeentitlement: " + entitlementUrl);
                    if (string2.equalsIgnoreCase(entitlementUrl)) {
                        Log.d(str, "same entitlement_url ");
                    } else {
                        updateEntitlementUrl(string2);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
