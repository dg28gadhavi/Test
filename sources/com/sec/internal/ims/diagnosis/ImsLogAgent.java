package com.sec.internal.ims.diagnosis;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SemHqmManager;
import android.os.SemSystemProperties;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.log.IMSLog;
import com.sec.internal.log.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import org.json.JSONObject;

public class ImsLogAgent extends ContentProvider {
    public static final String AUTHORITY = "com.sec.imsservice.log";
    private static final Object DMUI_LOCK = new Object();
    private static final String DRCS_KEY_RCS_USER_SETTING = "RUSS";
    private static final Object DRCS_LOCK = new Object();
    private static final Object DRPT_LOCK = new Object();
    private static final String INTENT_ACTION_BIG_DATA_INFO = "com.samsung.intent.action.BIG_DATA_INFO";
    private static final String INTENT_ACTION_DAILY_REPORT_EXPIRED = "com.sec.imsservice.ACTION_DAILY_REPORT_EXPIRED";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ImsLogAgent.class.getSimpleName();
    private static final int PERIOD_OF_DAILY_REPORT = 86400000;
    private static final Object PSCI_LOCK = new Object();
    private static final Object REGI_LOCK = new Object();
    private static final Object SIMI_LOCK = new Object();
    private static final Object UNKNOWN_LOCK = new Object();
    private static final int URI_TYPE_SEND_LOG = 1;
    private static final UriMatcher sUriMatcher;
    private Context mContext = null;
    private PendingIntent mDailyReportExpiry;
    private SimpleEventLog mEventLog = null;

    public int delete(Uri uri, String str, String[] strArr) {
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        return null;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI(AUTHORITY, "send/*", 1);
    }

    public boolean onCreate() {
        Context context = getContext();
        this.mContext = context;
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 200);
        scheduleDailyReport();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTION_BIG_DATA_INFO);
        intentFilter.addAction(INTENT_ACTION_DAILY_REPORT_EXPIRED);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String r3 = ImsLogAgent.LOG_TAG;
                Log.d(r3, "onReceive: " + intent.getAction());
                String action = intent.getAction();
                action.hashCode();
                if (action.equals(ImsLogAgent.INTENT_ACTION_DAILY_REPORT_EXPIRED)) {
                    ImsLogAgent.this.onDailyReport();
                } else if (action.equals(ImsLogAgent.INTENT_ACTION_BIG_DATA_INFO)) {
                    ImsLogAgent.this.onCsCallInfoReceived(intent.getIntExtra("simslot", 0), intent.getIntExtra(DiagnosisConstants.KEY_FEATURE, 0), intent.getStringExtra("bigdata_info"));
                }
            }
        }, intentFilter);
        return true;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        if (sUriMatcher.match(uri) != 1) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("update: Invalid uri [" + uri + "]");
            return 0;
        }
        String lastPathSegment = uri.getLastPathSegment();
        int simSlotFromUri = UriUtil.getSimSlotFromUri(uri);
        if (!"DRPT".equalsIgnoreCase(lastPathSegment) && !DiagnosisConstants.FEATURE_DRCS.equalsIgnoreCase(lastPathSegment)) {
            return sendStoredLog(simSlotFromUri, lastPathSegment);
        }
        onDailyReport();
        return 1;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        if (uri == null || contentValues == null) {
            Log.e(LOG_TAG, "insert: parameter Uri or ContentValues has unexpected null value");
            return null;
        }
        int simSlotFromUri = UriUtil.getSimSlotFromUri(uri);
        String asString = contentValues.getAsString(DiagnosisConstants.KEY_FEATURE);
        Integer asInteger = contentValues.getAsInteger(DiagnosisConstants.KEY_SEND_MODE);
        Integer asInteger2 = contentValues.getAsInteger(DiagnosisConstants.KEY_OVERWRITE_MODE);
        if (asInteger2 == null) {
            asInteger2 = 0;
        }
        contentValues.remove(DiagnosisConstants.KEY_FEATURE);
        contentValues.remove(DiagnosisConstants.KEY_SEND_MODE);
        contentValues.remove(DiagnosisConstants.KEY_OVERWRITE_MODE);
        if (asInteger == null || asInteger.intValue() == 0) {
            sendLogs(simSlotFromUri, asString, contentValues);
        } else if (asInteger.intValue() == 1) {
            storeLogs(simSlotFromUri, asString, contentValues, asInteger2.intValue());
        }
        return uri;
    }

    public Bundle call(String str, String str2, Bundle bundle) {
        if (!DiagnosisConstants.CALL_METHOD_LOGANDADD.equals(str) || TextUtils.isEmpty(str2)) {
            return null;
        }
        this.mEventLog.logAndAdd(str2);
        return null;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        this.mEventLog.dump(new IndentingPrintWriter(printWriter, "  "));
    }

    /* access modifiers changed from: private */
    public void onDailyReport() {
        Log.d(LOG_TAG, "onDailyReport");
        PendingIntent pendingIntent = this.mDailyReportExpiry;
        if (pendingIntent != null) {
            AlarmTimer.stop(this.mContext, pendingIntent);
            this.mDailyReportExpiry = null;
        }
        ImsSharedPrefHelper.remove(0, this.mContext, "DRPT", DiagnosisConstants.KEY_NEXT_DRPT_SCHEDULE);
        try {
            sendStoredLog(0, "DRPT");
            sendStoredLog(0, DiagnosisConstants.FEATURE_DRCS);
            if (ImsLogAgentUtil.getCommonHeader(this.mContext, 1).size() > 0) {
                sendStoredLog(1, "DRPT");
                sendStoredLog(1, DiagnosisConstants.FEATURE_DRCS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("sendLogToAgent: Exception - " + e.getMessage());
        } catch (Throwable th) {
            scheduleDailyReport();
            throw th;
        }
        scheduleDailyReport();
    }

    private int getPeriodForDailyReport() {
        int i = SemSystemProperties.getInt("persist.ims.debug.period.dr", PERIOD_OF_DAILY_REPORT);
        return i <= 0 ? PERIOD_OF_DAILY_REPORT : i;
    }

    private synchronized String getFeatureName(String str) {
        if (str.equalsIgnoreCase(DiagnosisConstants.FEATURE_REGI)) {
            return DiagnosisConstants.FEATURE_REGI;
        }
        if (str.equalsIgnoreCase(DiagnosisConstants.FEATURE_PSCI)) {
            return DiagnosisConstants.FEATURE_PSCI;
        }
        if (str.equalsIgnoreCase(DiagnosisConstants.FEATURE_SIMI)) {
            return DiagnosisConstants.FEATURE_SIMI;
        }
        if (str.equalsIgnoreCase(DiagnosisConstants.FEATURE_DMUI)) {
            return DiagnosisConstants.FEATURE_DMUI;
        }
        if (str.equalsIgnoreCase("DRPT")) {
            return "DRPT";
        }
        if (str.equalsIgnoreCase(DiagnosisConstants.FEATURE_DRCS)) {
            return DiagnosisConstants.FEATURE_DRCS;
        }
        return "UNKNOWN";
    }

    private synchronized Object getFeatureLock(String str) {
        if (str.equalsIgnoreCase(DiagnosisConstants.FEATURE_REGI)) {
            return REGI_LOCK;
        } else if (str.equalsIgnoreCase(DiagnosisConstants.FEATURE_PSCI)) {
            return PSCI_LOCK;
        } else if (str.equalsIgnoreCase(DiagnosisConstants.FEATURE_SIMI)) {
            return SIMI_LOCK;
        } else if (str.equalsIgnoreCase(DiagnosisConstants.FEATURE_DMUI)) {
            return DMUI_LOCK;
        } else if (str.equalsIgnoreCase("DRPT")) {
            return DRPT_LOCK;
        } else if (str.equalsIgnoreCase(DiagnosisConstants.FEATURE_DRCS)) {
            return DRCS_LOCK;
        } else {
            return UNKNOWN_LOCK;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x00fb  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean sendLogs(int r8, java.lang.String r9, android.content.ContentValues r10) {
        /*
            r7 = this;
            java.lang.Object r0 = r7.getFeatureLock(r9)
            monitor-enter(r0)
            java.lang.String r1 = LOG_TAG     // Catch:{ all -> 0x010d }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x010d }
            r2.<init>()     // Catch:{ all -> 0x010d }
            java.lang.String r3 = "sendLogs: feature ["
            r2.append(r3)     // Catch:{ all -> 0x010d }
            r2.append(r9)     // Catch:{ all -> 0x010d }
            java.lang.String r3 = "]"
            r2.append(r3)     // Catch:{ all -> 0x010d }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x010d }
            android.util.Log.d(r1, r2)     // Catch:{ all -> 0x010d }
            boolean r1 = com.sec.internal.helper.CollectionUtils.isNullOrEmpty((android.content.ContentValues) r10)     // Catch:{ all -> 0x010d }
            if (r1 == 0) goto L_0x0046
            com.sec.internal.helper.SimpleEventLog r7 = r7.mEventLog     // Catch:{ all -> 0x010d }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x010d }
            r8.<init>()     // Catch:{ all -> 0x010d }
            java.lang.String r10 = "sendLogs: ["
            r8.append(r10)     // Catch:{ all -> 0x010d }
            r8.append(r9)     // Catch:{ all -> 0x010d }
            java.lang.String r9 = "] is null or empty!"
            r8.append(r9)     // Catch:{ all -> 0x010d }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x010d }
            r7.logAndAdd(r8)     // Catch:{ all -> 0x010d }
            monitor-exit(r0)     // Catch:{ all -> 0x010d }
            r7 = 0
            return r7
        L_0x0046:
            r1 = 0
            org.json.JSONObject r2 = new org.json.JSONObject     // Catch:{ JSONException -> 0x00b3 }
            r2.<init>()     // Catch:{ JSONException -> 0x00b3 }
            android.content.Context r3 = r7.mContext     // Catch:{ JSONException -> 0x00b1 }
            android.content.ContentValues r8 = com.sec.internal.ims.diagnosis.ImsLogAgentUtil.getCommonHeader(r3, r8)     // Catch:{ JSONException -> 0x00b1 }
            java.util.Set r3 = r8.keySet()     // Catch:{ JSONException -> 0x00b1 }
            java.util.Iterator r3 = r3.iterator()     // Catch:{ JSONException -> 0x00b1 }
        L_0x005a:
            boolean r4 = r3.hasNext()     // Catch:{ JSONException -> 0x00b1 }
            if (r4 == 0) goto L_0x0072
            java.lang.Object r4 = r3.next()     // Catch:{ JSONException -> 0x00b1 }
            java.lang.String r4 = (java.lang.String) r4     // Catch:{ JSONException -> 0x00b1 }
            java.lang.Object r5 = r8.get(r4)     // Catch:{ JSONException -> 0x00b1 }
            java.lang.String r5 = java.lang.String.valueOf(r5)     // Catch:{ JSONException -> 0x00b1 }
            r2.put(r4, r5)     // Catch:{ JSONException -> 0x00b1 }
            goto L_0x005a
        L_0x0072:
            java.util.Set r8 = r10.keySet()     // Catch:{ JSONException -> 0x00b1 }
            java.util.Iterator r8 = r8.iterator()     // Catch:{ JSONException -> 0x00b1 }
        L_0x007a:
            boolean r3 = r8.hasNext()     // Catch:{ JSONException -> 0x00b1 }
            if (r3 == 0) goto L_0x00d0
            java.lang.Object r3 = r8.next()     // Catch:{ JSONException -> 0x00b1 }
            java.lang.String r3 = (java.lang.String) r3     // Catch:{ JSONException -> 0x00b1 }
            java.lang.Object r4 = r10.get(r3)     // Catch:{ JSONException -> 0x00b1 }
            if (r4 != 0) goto L_0x00a9
            com.sec.internal.helper.SimpleEventLog r4 = r7.mEventLog     // Catch:{ JSONException -> 0x00b1 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ JSONException -> 0x00b1 }
            r5.<init>()     // Catch:{ JSONException -> 0x00b1 }
            java.lang.String r6 = "sendLogs: ["
            r5.append(r6)     // Catch:{ JSONException -> 0x00b1 }
            r5.append(r3)     // Catch:{ JSONException -> 0x00b1 }
            java.lang.String r3 = "] is null!"
            r5.append(r3)     // Catch:{ JSONException -> 0x00b1 }
            java.lang.String r3 = r5.toString()     // Catch:{ JSONException -> 0x00b1 }
            r4.logAndAdd(r3)     // Catch:{ JSONException -> 0x00b1 }
            goto L_0x007a
        L_0x00a9:
            java.lang.String r4 = java.lang.String.valueOf(r4)     // Catch:{ JSONException -> 0x00b1 }
            r2.put(r3, r4)     // Catch:{ JSONException -> 0x00b1 }
            goto L_0x007a
        L_0x00b1:
            r8 = move-exception
            goto L_0x00b5
        L_0x00b3:
            r8 = move-exception
            r2 = r1
        L_0x00b5:
            java.lang.String r10 = LOG_TAG     // Catch:{ all -> 0x010d }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x010d }
            r3.<init>()     // Catch:{ all -> 0x010d }
            java.lang.String r4 = "sendLogs: JSONException! "
            r3.append(r4)     // Catch:{ all -> 0x010d }
            java.lang.String r8 = r8.getMessage()     // Catch:{ all -> 0x010d }
            r3.append(r8)     // Catch:{ all -> 0x010d }
            java.lang.String r8 = r3.toString()     // Catch:{ all -> 0x010d }
            android.util.Log.e(r10, r8)     // Catch:{ all -> 0x010d }
        L_0x00d0:
            java.lang.String r8 = r7.normalizeLog(r2)     // Catch:{ all -> 0x010d }
            java.lang.String r10 = LOG_TAG     // Catch:{ all -> 0x010d }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x010d }
            r2.<init>()     // Catch:{ all -> 0x010d }
            java.lang.String r3 = "sendLogs: send ["
            r2.append(r3)     // Catch:{ all -> 0x010d }
            r2.append(r8)     // Catch:{ all -> 0x010d }
            java.lang.String r3 = "]"
            r2.append(r3)     // Catch:{ all -> 0x010d }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x010d }
            com.sec.internal.log.IMSLog.s(r10, r2)     // Catch:{ all -> 0x010d }
            r7.sendLogToHqmManager(r9, r8)     // Catch:{ all -> 0x010d }
            java.lang.String r8 = "DRPT"
            boolean r8 = r9.equalsIgnoreCase(r8)     // Catch:{ all -> 0x010d }
            if (r8 == 0) goto L_0x010a
            android.content.Context r7 = r7.mContext     // Catch:{ all -> 0x010d }
            android.content.ContentResolver r7 = r7.getContentResolver()     // Catch:{ all -> 0x010d }
            java.lang.String r8 = "content://com.sec.imsservice.log/log/drpt"
            android.net.Uri r8 = android.net.Uri.parse(r8)     // Catch:{ all -> 0x010d }
            r7.notifyChange(r8, r1)     // Catch:{ all -> 0x010d }
        L_0x010a:
            monitor-exit(r0)     // Catch:{ all -> 0x010d }
            r7 = 1
            return r7
        L_0x010d:
            r7 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x010d }
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.diagnosis.ImsLogAgent.sendLogs(int, java.lang.String, android.content.ContentValues):boolean");
    }

    /* access modifiers changed from: protected */
    @SuppressLint({"WrongConstant"})
    public boolean sendLogToHqmManager(String str, String str2) {
        SemHqmManager semHqmManager = (SemHqmManager) this.mContext.getSystemService("HqmManagerService");
        if (semHqmManager == null) {
            return false;
        }
        return semHqmManager.sendHWParamToHQM(0, DiagnosisConstants.COMPONENT_ID, str, "sm", "0.0", ImsConstants.RCS_AS.SEC, "", str2, "");
    }

    private int sendStoredLog(int i, String str) {
        String featureName = getFeatureName(str);
        int i2 = 0;
        if (featureName.equals("UNKNOWN")) {
            this.mEventLog.logAndAdd("sendStoredLog: Invalid feature [" + featureName + "]");
            return 0;
        }
        IMSLog.d(LOG_TAG, i, "sendStoredLog: feature [" + featureName + "]");
        synchronized (getFeatureLock(featureName)) {
            SharedPreferences sharedPref = ImsSharedPrefHelper.getSharedPref(i, this.mContext, featureName, 0, false);
            ContentValues contentValues = new ContentValues();
            for (Map.Entry next : sharedPref.getAll().entrySet()) {
                String str2 = (String) next.getKey();
                Object value = next.getValue();
                if (value instanceof Integer) {
                    contentValues.put(str2, Integer.valueOf(((Integer) value).intValue()));
                } else if (value instanceof String) {
                    contentValues.put(str2, (String) value);
                } else if (value instanceof Long) {
                    contentValues.put(str2, Long.valueOf(((Long) value).longValue()));
                } else {
                    this.mEventLog.logAndAdd(i, "sendStoredLog: [" + str2 + "] has wrong data type!");
                }
            }
            if (CollectionUtils.isNullOrEmpty(contentValues)) {
                this.mEventLog.logAndAdd(i, "sendStoredLog: [" + featureName + "] is null or empty");
                return 0;
            }
            if (featureName.equalsIgnoreCase("DRPT")) {
                contentValues.put(DiagnosisConstants.COMMON_KEY_VOLTE_SETTINGS, Integer.valueOf(ImsConstants.SystemSettings.getVoiceCallType(this.mContext, -1, i) == 0 ? 1 : 0));
                contentValues.put(DiagnosisConstants.COMMON_KEY_VIDEO_SETTINGS, Integer.valueOf(ImsConstants.SystemSettings.getVideoCallType(this.mContext, -1, i) == 0 ? 1 : 0));
                contentValues.put(DiagnosisConstants.DRPT_KEY_VOWIFI_ENABLE_SETTINGS, Integer.valueOf(VowifiConfig.isEnabled(this.mContext, i) ? 1 : 0));
                if (VowifiConfig.isCrossSimSettingEnabled(this.mContext, i)) {
                    i2 = 1;
                }
                contentValues.put(DiagnosisConstants.DRPT_KEY_CROSS_SIM_ENABLE_SETTINGS, Integer.valueOf(i2));
                contentValues.put(DiagnosisConstants.DRPT_KEY_VOWIFI_PREF_SETTINGS, Integer.valueOf(VowifiConfig.getPrefMode(this.mContext, 1, i)));
                contentValues.remove(DiagnosisConstants.KEY_NEXT_DRPT_SCHEDULE);
            } else if (featureName.equalsIgnoreCase(DiagnosisConstants.FEATURE_DRCS)) {
                boolean isSmsAppDefault = isSmsAppDefault();
                if (!isSmsAppDefault) {
                    i2 = 1;
                }
                contentValues.put("CMAS", Integer.valueOf(i2));
                if (!isSmsAppDefault) {
                    String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this.mContext);
                    if (!TextUtils.isEmpty(defaultSmsPackage)) {
                        contentValues.put("CMDA", defaultSmsPackage);
                    }
                }
                String str3 = SemSystemProperties.get("persist.ril.config.defaultmsgapp");
                if (TextUtils.isEmpty(str3)) {
                    str3 = "NA";
                }
                contentValues.put("IMDA", str3);
                contentValues.put(DRCS_KEY_RCS_USER_SETTING, Integer.valueOf(ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, i)));
            }
            IMSLog.d(LOG_TAG, i, "sendStoredLog: send logs of [" + featureName + "]");
            sendLogs(i, featureName, contentValues);
            sharedPref.edit().clear().apply();
            return 1;
        }
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(3:38|39|53) */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        r5 = r11.mEventLog;
        r5.logAndAdd("storeLogs: ClassCastException! key: [" + r4 + "]!");
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:38:0x00cf */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean storeLogs(int r12, java.lang.String r13, android.content.ContentValues r14, int r15) {
        /*
            r11 = this;
            java.lang.Object r0 = r11.getFeatureLock(r13)
            monitor-enter(r0)
            boolean r1 = com.sec.internal.helper.CollectionUtils.isNullOrEmpty((android.content.ContentValues) r14)     // Catch:{ all -> 0x010e }
            r2 = 0
            if (r1 == 0) goto L_0x002a
            com.sec.internal.helper.SimpleEventLog r11 = r11.mEventLog     // Catch:{ all -> 0x010e }
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ all -> 0x010e }
            r12.<init>()     // Catch:{ all -> 0x010e }
            java.lang.String r14 = "storeLogs: ["
            r12.append(r14)     // Catch:{ all -> 0x010e }
            r12.append(r13)     // Catch:{ all -> 0x010e }
            java.lang.String r13 = "] is null or empty"
            r12.append(r13)     // Catch:{ all -> 0x010e }
            java.lang.String r12 = r12.toString()     // Catch:{ all -> 0x010e }
            r11.logAndAdd(r12)     // Catch:{ all -> 0x010e }
            monitor-exit(r0)     // Catch:{ all -> 0x010e }
            return r2
        L_0x002a:
            android.content.Context r1 = r11.mContext     // Catch:{ all -> 0x010e }
            android.content.SharedPreferences r12 = com.sec.internal.helper.ImsSharedPrefHelper.getSharedPref(r12, r1, r13, r2, r2)     // Catch:{ all -> 0x010e }
            android.content.SharedPreferences$Editor r1 = r12.edit()     // Catch:{ all -> 0x010e }
            java.util.Set r3 = r14.keySet()     // Catch:{ all -> 0x010e }
            java.util.Iterator r3 = r3.iterator()     // Catch:{ all -> 0x010e }
        L_0x003c:
            boolean r4 = r3.hasNext()     // Catch:{ all -> 0x010e }
            r5 = 1
            if (r4 == 0) goto L_0x00ed
            java.lang.Object r4 = r3.next()     // Catch:{ all -> 0x010e }
            java.lang.String r4 = (java.lang.String) r4     // Catch:{ all -> 0x010e }
            java.lang.Object r6 = r14.get(r4)     // Catch:{ all -> 0x010e }
            if (r6 != 0) goto L_0x006c
            com.sec.internal.helper.SimpleEventLog r5 = r11.mEventLog     // Catch:{ all -> 0x010e }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x010e }
            r6.<init>()     // Catch:{ all -> 0x010e }
            java.lang.String r7 = "storeLogs: ["
            r6.append(r7)     // Catch:{ all -> 0x010e }
            r6.append(r4)     // Catch:{ all -> 0x010e }
            java.lang.String r4 = "] is null!"
            r6.append(r4)     // Catch:{ all -> 0x010e }
            java.lang.String r4 = r6.toString()     // Catch:{ all -> 0x010e }
            r5.logAndAdd(r4)     // Catch:{ all -> 0x010e }
            goto L_0x003c
        L_0x006c:
            boolean r7 = r6 instanceof java.lang.Integer     // Catch:{ ClassCastException -> 0x00cf }
            r8 = 2
            if (r7 == 0) goto L_0x0088
            java.lang.Integer r6 = (java.lang.Integer) r6     // Catch:{ ClassCastException -> 0x00cf }
            int r6 = r6.intValue()     // Catch:{ ClassCastException -> 0x00cf }
            int r7 = r12.getInt(r4, r2)     // Catch:{ ClassCastException -> 0x00cf }
            if (r15 != r5) goto L_0x007f
            int r6 = r6 + r7
            goto L_0x0084
        L_0x007f:
            if (r15 != r8) goto L_0x0084
            if (r6 > r7) goto L_0x0084
            r6 = r7
        L_0x0084:
            r1.putInt(r4, r6)     // Catch:{ ClassCastException -> 0x00cf }
            goto L_0x003c
        L_0x0088:
            boolean r7 = r6 instanceof java.lang.Long     // Catch:{ ClassCastException -> 0x00cf }
            if (r7 == 0) goto L_0x00a7
            java.lang.Long r6 = (java.lang.Long) r6     // Catch:{ ClassCastException -> 0x00cf }
            long r6 = r6.longValue()     // Catch:{ ClassCastException -> 0x00cf }
            r9 = 0
            long r9 = r12.getLong(r4, r9)     // Catch:{ ClassCastException -> 0x00cf }
            if (r15 != r5) goto L_0x009c
            long r6 = r6 + r9
            goto L_0x00a3
        L_0x009c:
            if (r15 != r8) goto L_0x00a3
            int r5 = (r6 > r9 ? 1 : (r6 == r9 ? 0 : -1))
            if (r5 > 0) goto L_0x00a3
            r6 = r9
        L_0x00a3:
            r1.putLong(r4, r6)     // Catch:{ ClassCastException -> 0x00cf }
            goto L_0x003c
        L_0x00a7:
            boolean r5 = r6 instanceof java.lang.String     // Catch:{ ClassCastException -> 0x00cf }
            if (r5 == 0) goto L_0x00b1
            java.lang.String r6 = (java.lang.String) r6     // Catch:{ ClassCastException -> 0x00cf }
            r1.putString(r4, r6)     // Catch:{ ClassCastException -> 0x00cf }
            goto L_0x003c
        L_0x00b1:
            com.sec.internal.helper.SimpleEventLog r5 = r11.mEventLog     // Catch:{ ClassCastException -> 0x00cf }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ ClassCastException -> 0x00cf }
            r6.<init>()     // Catch:{ ClassCastException -> 0x00cf }
            java.lang.String r7 = "storeLogs: ["
            r6.append(r7)     // Catch:{ ClassCastException -> 0x00cf }
            r6.append(r4)     // Catch:{ ClassCastException -> 0x00cf }
            java.lang.String r7 = "] has wrong data type!"
            r6.append(r7)     // Catch:{ ClassCastException -> 0x00cf }
            java.lang.String r6 = r6.toString()     // Catch:{ ClassCastException -> 0x00cf }
            r5.logAndAdd(r6)     // Catch:{ ClassCastException -> 0x00cf }
            goto L_0x003c
        L_0x00cf:
            com.sec.internal.helper.SimpleEventLog r5 = r11.mEventLog     // Catch:{ all -> 0x010e }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x010e }
            r6.<init>()     // Catch:{ all -> 0x010e }
            java.lang.String r7 = "storeLogs: ClassCastException! key: ["
            r6.append(r7)     // Catch:{ all -> 0x010e }
            r6.append(r4)     // Catch:{ all -> 0x010e }
            java.lang.String r4 = "]!"
            r6.append(r4)     // Catch:{ all -> 0x010e }
            java.lang.String r4 = r6.toString()     // Catch:{ all -> 0x010e }
            r5.logAndAdd(r4)     // Catch:{ all -> 0x010e }
            goto L_0x003c
        L_0x00ed:
            java.lang.String r11 = LOG_TAG     // Catch:{ all -> 0x010e }
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ all -> 0x010e }
            r12.<init>()     // Catch:{ all -> 0x010e }
            java.lang.String r14 = "storeLogs: feature ["
            r12.append(r14)     // Catch:{ all -> 0x010e }
            r12.append(r13)     // Catch:{ all -> 0x010e }
            java.lang.String r13 = "]"
            r12.append(r13)     // Catch:{ all -> 0x010e }
            java.lang.String r12 = r12.toString()     // Catch:{ all -> 0x010e }
            android.util.Log.d(r11, r12)     // Catch:{ all -> 0x010e }
            r1.apply()     // Catch:{ all -> 0x010e }
            monitor-exit(r0)     // Catch:{ all -> 0x010e }
            return r5
        L_0x010e:
            r11 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x010e }
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.diagnosis.ImsLogAgent.storeLogs(int, java.lang.String, android.content.ContentValues, int):boolean");
    }

    private void scheduleDailyReport() {
        if (this.mDailyReportExpiry == null) {
            SharedPreferences sharedPref = ImsSharedPrefHelper.getSharedPref(0, this.mContext, "DRPT", 0, false);
            long j = 0;
            long j2 = sharedPref.getLong(DiagnosisConstants.KEY_NEXT_DRPT_SCHEDULE, 0);
            long currentTimeMillis = System.currentTimeMillis();
            int i = (j2 > 0 ? 1 : (j2 == 0 ? 0 : -1));
            if (i <= 0 || j2 > currentTimeMillis) {
                if (i == 0) {
                    j2 = currentTimeMillis + ((long) getPeriodForDailyReport());
                }
                j = j2 - currentTimeMillis;
                sharedPref.edit().putLong(DiagnosisConstants.KEY_NEXT_DRPT_SCHEDULE, j2).apply();
            } else {
                Log.d(LOG_TAG, "scheduleDailyReport: DRPT timer is expired. Sending it now.");
            }
            String str = LOG_TAG;
            Log.d(str, "scheduleDailyReport: delay [" + j + "] scheduled time [" + new Date(j2) + "]");
            Intent intent = new Intent(INTENT_ACTION_DAILY_REPORT_EXPIRED);
            intent.setPackage(this.mContext.getPackageName());
            PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
            this.mDailyReportExpiry = broadcast;
            AlarmTimer.start(this.mContext, broadcast, j, false);
        }
    }

    private String normalizeLog(JSONObject jSONObject) {
        return jSONObject.toString().replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("\\s+", "^");
    }

    /* access modifiers changed from: private */
    public void onCsCallInfoReceived(int i, int i2, String str) {
        if (i2 != 0 && i2 != 1) {
            Log.d(LOG_TAG, "onCsCallInfoReceived : ignore except CEND/DROP! received: " + i2);
        } else if (!TextUtils.isEmpty(str)) {
            String replace = str.replace(CmcConstants.E_NUM_STR_QUOTE, "");
            String str2 = LOG_TAG;
            Log.d(str2, "onCsCallInfoReceived: remove quotes [" + replace + "]");
            String[] split = replace.split(",");
            if (split.length < 1) {
                Log.d(str2, "onCsCallInfoReceived: No data");
                return;
            }
            int i3 = -1;
            int i4 = 0;
            int i5 = -1;
            int i6 = -1;
            while (i4 < split.length) {
                try {
                    String[] split2 = split[i4].split(":");
                    if (split2 != null && split2.length > 1) {
                        if (split[i4].contains("Ctyp")) {
                            i3 = Integer.parseInt(split2[1].trim());
                        } else if (split[i4].contains("Csta")) {
                            i5 = Integer.parseInt(split2[1].trim());
                        } else if (split[i4].contains("Etyp")) {
                            i6 = Integer.parseInt(split2[1].trim());
                        }
                    }
                    i4++;
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "onCsCallInfoReceived: NumberFormatException! " + e.getMessage());
                    return;
                }
            }
            if (i3 >= 1 && i3 <= 3) {
                ContentValues contentValues = new ContentValues();
                ContentValues contentValues2 = new ContentValues();
                if (i2 == 1) {
                    contentValues2.put(DiagnosisConstants.PSCI_KEY_CALL_BEARER, Integer.valueOf(DiagnosisConstants.CALL_BEARER.CS.getValue()));
                    if (i3 == 3) {
                        contentValues2.put("TYPE", 7);
                    } else {
                        contentValues2.put("TYPE", Integer.valueOf(i3));
                    }
                    if (i5 == 1) {
                        contentValues2.put(DiagnosisConstants.PSCI_KEY_CALL_STATE, 3);
                        contentValues.put(DiagnosisConstants.DRPT_KEY_CSCALL_OUTGOING_FAIL, 1);
                    } else if (i5 == 2) {
                        contentValues2.put(DiagnosisConstants.PSCI_KEY_CALL_STATE, 2);
                        contentValues.put(DiagnosisConstants.DRPT_KEY_CSCALL_INCOMING_FAIL, 1);
                    } else {
                        contentValues2.put(DiagnosisConstants.PSCI_KEY_CALL_STATE, 5);
                    }
                    contentValues2.put(DiagnosisConstants.PSCI_KEY_FAIL_CODE, Integer.valueOf(i6));
                    storeLogs(i, DiagnosisConstants.FEATURE_PSCI, contentValues2, 0);
                    ImsLogAgentUtil.requestToSendStoredLog(i, this.mContext, DiagnosisConstants.FEATURE_PSCI);
                    Log.d(LOG_TAG, "onCsCallInfoReceived: send PSCI: " + contentValues2);
                    contentValues.put(DiagnosisConstants.DRPT_KEY_CSCALL_END_FAIL_COUNT, 1);
                }
                contentValues.put(DiagnosisConstants.DRPT_KEY_CSCALL_END_TOTAL_COUNT, 1);
                Log.d(LOG_TAG, "onCsCallInfoReceived: storeLogs: " + contentValues);
                storeLogs(i, "DRPT", contentValues, 1);
            }
        }
    }

    private boolean isSmsAppDefault() {
        String str;
        Log.d(LOG_TAG, "get default sms app.");
        try {
            str = Telephony.Sms.getDefaultSmsPackage(this.mContext);
        } catch (Exception e) {
            String str2 = LOG_TAG;
            Log.e(str2, "Failed to getDefaultSmsPackage: " + e);
            str = null;
        }
        if (str == null) {
            Log.e(LOG_TAG, "default sms app is null");
            return false;
        }
        String msgAppPkgName = PackageUtils.getMsgAppPkgName(this.mContext);
        boolean equals = TextUtils.equals(str, msgAppPkgName);
        String str3 = LOG_TAG;
        Log.d(str3, "default sms app:" + str + " samsungPackage:" + msgAppPkgName);
        StringBuilder sb = new StringBuilder();
        sb.append("isDefaultMessageAppInUse : ");
        sb.append(equals);
        Log.d(str3, sb.toString());
        return equals;
    }
}
