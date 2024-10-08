package com.sec.internal.ims.settings;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.configuration.DATA;
import com.sec.ims.settings.NvConfiguration;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISequentialInitializable;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DmConfigModule extends Handler implements ISequentialInitializable {
    public static final String CONFIG_DM_PROVIDER = "content://com.samsung.rcs.dmconfigurationprovider/omadm/";
    public static final String DM_PATH = "omadm/";
    private static final int DM_SERVER_FETCH_FAIL_SIM0 = -2;
    private static final int DM_SERVER_FETCH_FAIL_SIM1 = -3;
    private static final int EVT_FINISH_DM_CONFIG = 1;
    private static final int EVT_FINISH_OMADM_PROV_UPDATE = 2;
    private static final String INTENT_ACTION_DM_VALUE_UPDATE = "com.samsung.ims.action.DM_UPDATE";
    public static final String INTERNAL_KEY_PROCESS_NAME = "INTERNAL_KEY_PROCESS_NAME";
    private static final String KOR_DM_PACKAGE_NAME = "com.ims.dm";
    private static final String LOG_TAG = "DmConfigModule";
    private static final int VZW_OMADM_PENDING_DELAY = 5000;
    /* access modifiers changed from: private */
    public Context mContext;
    private DmContentValues mDmContentValues;
    private SimpleEventLog mEventLog;
    protected IImsFramework mImsFramework = null;
    ContentObserver mMnoUpdateObserver = new ContentObserver(this) {
        public void onChange(boolean z) {
            Cursor query = DmConfigModule.this.mContext.getContentResolver().query(Uri.parse("content://com.sec.ims.settings/nvlist"), (String[]) null, (String) null, (String[]) null, (String) null);
            if (query != null) {
                try {
                    if (query.moveToFirst()) {
                        String string = query.getString(0);
                        if (string != null) {
                            String replace = string.replace("[", "").replace("]", "").replace(" ", "");
                            DmConfigModule.this.mNvList.clear();
                            DmConfigModule.this.mNvList.addAll(Arrays.asList(replace.split(",")));
                        } else {
                            Log.e(DmConfigModule.LOG_TAG, "nvList is null");
                        }
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            Log.d(DmConfigModule.LOG_TAG, "nv list reloaded:" + DmConfigModule.this.mNvList);
            if (query != null) {
                query.close();
                return;
            }
            return;
            throw th;
        }
    };
    protected ArrayList<String> mNvList = new ArrayList<>();
    private int mOmadmProvisioningTransactionId = -1;
    protected IRegistrationManager mRegMgr;
    protected BroadcastReceiver mVzwTestModeReceiver = null;

    public DmConfigModule(Context context, Looper looper, IImsFramework iImsFramework) {
        super(looper);
        this.mContext = context;
        this.mDmContentValues = new DmContentValues();
        this.mImsFramework = iImsFramework;
        this.mEventLog = new SimpleEventLog(this.mContext, LOG_TAG, 200);
    }

    public void initSequentially() {
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.sec.ims.settings/mno"), true, this.mMnoUpdateObserver);
        if (IMSLog.isEngMode()) {
            registerVzwTestReceiver();
        }
    }

    public void setRegistrationManager(IRegistrationManager iRegistrationManager) {
        this.mRegMgr = iRegistrationManager;
    }

    private void registerVzwTestReceiver() {
        Log.d(LOG_TAG, "registerVzwTestReceiver");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTION_DM_VALUE_UPDATE);
        AnonymousClass1 r1 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String stringExtra = intent.getStringExtra("ITEM");
                int intExtra = intent.getIntExtra("VALUE", -1);
                int intExtra2 = intent.getIntExtra(ImsConstants.Intents.EXTRA_REGI_PHONE_ID, 0);
                if (TextUtils.equals(stringExtra, "157") || TextUtils.equals(stringExtra, "106")) {
                    Log.d(DmConfigModule.LOG_TAG, "dmItem : " + stringExtra + ", value : " + intExtra);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(stringExtra, String.valueOf(intExtra));
                    DmConfigModule.this.updateConfigValues(contentValues, -1, intExtra2);
                    return;
                }
                Log.d(DmConfigModule.LOG_TAG, "This item is not allowed : " + stringExtra);
            }
        };
        this.mVzwTestModeReceiver = r1;
        this.mContext.registerReceiver(r1, intentFilter);
    }

    public int startDmConfig(int i) {
        if (this.mDmContentValues == null) {
            this.mDmContentValues = new DmContentValues();
        }
        int newTransactionId = this.mDmContentValues.getNewTransactionId();
        Log.d(LOG_TAG, "Start getting ims-config by OTA-DM with TransactionId " + newTransactionId + ", phoneId " + i);
        return newTransactionId;
    }

    public void finishDmConfig(int i, int i2) {
        Log.d(LOG_TAG, "finish getting ims-config by OTA-DM with transactionId " + i + ", phoneId " + i2);
        sendMessage(obtainMessage(1, i, i2, (Object) null));
    }

    public ContentValues getConfigValues(String[] strArr, int i) {
        int i2;
        int i3;
        String str;
        String str2;
        String str3;
        String str4;
        ISimManager simManagerFromSimSlot;
        String[] strArr2 = strArr;
        int i4 = i;
        ContentValues contentValues = new ContentValues();
        if (strArr2 == null || strArr2.length <= 0) {
            Log.e(LOG_TAG, "Error on fields");
            return contentValues;
        }
        String processNameById = PackageUtils.getProcessNameById(this.mContext, Binder.getCallingPid());
        Map<String, String> read = DmConfigHelper.read(this.mContext, "omadm/*", i4);
        Set<String> keySet = read.keySet();
        int length = strArr2.length;
        int i5 = 0;
        while (i5 < length) {
            String str5 = strArr2[i5];
            try {
                i3 = Integer.parseInt(str5);
                if (i3 < 0) {
                    i5++;
                    strArr2 = strArr;
                } else {
                    if (i3 >= 900) {
                        str2 = "";
                        str = str2;
                        i2 = 3;
                    } else {
                        i2 = ((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(i3)).getType();
                        str2 = ((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(i3)).getName();
                        str = ((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(i3)).getPathName();
                    }
                    if (i2 != 0) {
                        if (i2 != 1) {
                            if (i2 != 3) {
                                if (i2 == 4) {
                                    str3 = DmConfigHelper.getImsSwitchValue(this.mContext, str2, i4) == 1 ? "1" : "0";
                                } else if (i2 == 5) {
                                    str3 = RcsConfigurationHelper.readStringParamWithPath(this.mContext, str2);
                                }
                            } else if (Integer.parseInt("74") == i3) {
                                str3 = this.mImsFramework.getString(i4, "dm_app_id", ConfigConstants.PVALUE.APP_ID_1);
                            } else if (Integer.parseInt("75") == i3) {
                                str3 = this.mImsFramework.getString(i4, "dm_user_disp_name", ConfigConstants.PVALUE.APP_ID_1);
                            } else {
                                throw new IllegalArgumentException("Unsupported Segment : Global Type " + i3);
                            }
                            contentValues.put(str5, str3);
                            Log.d(LOG_TAG, "result (" + i3 + ") " + str2 + " [ " + str3 + " ]");
                            i5++;
                            strArr2 = strArr;
                        } else if (Integer.parseInt("91") == i3 && (simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i)) != null) {
                            str4 = simManagerFromSimSlot.getSimSerialNumber();
                        }
                        str3 = "";
                        contentValues.put(str5, str3);
                        Log.d(LOG_TAG, "result (" + i3 + ") " + str2 + " [ " + str3 + " ]");
                        i5++;
                        strArr2 = strArr;
                    } else if (this.mNvList.contains(str2)) {
                        str3 = NvConfiguration.get(this.mContext, str2, "", i4);
                        contentValues.put(str5, str3);
                        Log.d(LOG_TAG, "result (" + i3 + ") " + str2 + " [ " + str3 + " ]");
                        i5++;
                        strArr2 = strArr;
                    } else {
                        for (String next : keySet) {
                            if (str.equals(next)) {
                                str4 = read.get(next);
                                if (TextUtils.equals("VOICE_DOMAIN_PREF_EUTRAN", str5) && TextUtils.equals("com.ims.dm", processNameById)) {
                                    str4 = "-1";
                                }
                            }
                        }
                        str3 = "";
                        contentValues.put(str5, str3);
                        Log.d(LOG_TAG, "result (" + i3 + ") " + str2 + " [ " + str3 + " ]");
                        i5++;
                        strArr2 = strArr;
                    }
                    str3 = str4;
                    contentValues.put(str5, str3);
                    Log.d(LOG_TAG, "result (" + i3 + ") " + str2 + " [ " + str3 + " ]");
                    i5++;
                    strArr2 = strArr;
                }
            } catch (NumberFormatException unused) {
                Log.d(LOG_TAG, "get xNode " + str5);
                str = DM_PATH + str5;
                i3 = -1;
                str2 = str5;
                i2 = 0;
            }
        }
        return contentValues;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x00f4  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x010c  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0118  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean updateConfigValues(android.content.ContentValues r27, int r28, int r29) {
        /*
            r26 = this;
            r0 = r26
            r1 = r27
            r2 = r28
            r3 = r29
            java.lang.String r4 = " "
            java.lang.String r5 = "./3GPP_IMS/"
            java.lang.String r6 = ""
            android.content.Context r7 = r0.mContext
            int r8 = android.os.Binder.getCallingPid()
            java.lang.String r7 = com.sec.internal.helper.os.PackageUtils.getProcessNameById(r7, r8)
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "updateConfigValues<"
            r8.append(r9)
            r8.append(r3)
            java.lang.String r9 = ">: caller ["
            r8.append(r9)
            r8.append(r7)
            java.lang.String r9 = "] updateMap["
            r8.append(r9)
            r8.append(r1)
            java.lang.String r9 = "] transactionId ["
            r8.append(r9)
            r8.append(r2)
            java.lang.String r9 = "]"
            r8.append(r9)
            java.lang.String r8 = r8.toString()
            java.lang.String r10 = "DmConfigModule"
            android.util.Log.d(r10, r8)
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            android.content.ContentValues r11 = new android.content.ContentValues
            r11.<init>()
            android.content.ContentValues r12 = new android.content.ContentValues
            r12.<init>()
            android.content.ContentValues r13 = new android.content.ContentValues
            r13.<init>()
            java.lang.String[] r14 = com.sec.ims.configuration.DATA.DM_FIELD_INDEX.values()
            android.content.ContentValues r14 = r0.getConfigValues(r14, r3)
            android.content.Context r15 = r0.mContext
            java.lang.String r2 = "omadm/*"
            java.util.Map r2 = com.sec.internal.helper.DmConfigHelper.read(r15, r2, r3)
            java.util.Set r15 = r27.keySet()
            java.util.Iterator r15 = r15.iterator()
            r16 = r7
            r17 = 0
        L_0x007c:
            boolean r18 = r15.hasNext()
            java.lang.String r19 = "31"
            java.lang.String r20 = "94"
            java.lang.String r21 = "93"
            if (r18 == 0) goto L_0x0212
            java.lang.Object r18 = r15.next()
            r7 = r18
            java.lang.String r7 = (java.lang.String) r7
            java.lang.Object r18 = r14.get(r7)
            java.lang.String r18 = (java.lang.String) r18
            java.lang.Object r22 = r1.get(r7)
            r1 = r22
            java.lang.String r1 = (java.lang.String) r1
            r22 = r14
            java.util.List r14 = com.sec.ims.configuration.DATA.DM_FIELD_LIST     // Catch:{ NumberFormatException -> 0x00b3 }
            r23 = r15
            int r15 = java.lang.Integer.parseInt(r7)     // Catch:{ NumberFormatException -> 0x00b5 }
            java.lang.Object r14 = r14.get(r15)     // Catch:{ NumberFormatException -> 0x00b5 }
            com.sec.ims.configuration.DATA$DM_FIELD_INFO r14 = (com.sec.ims.configuration.DATA.DM_FIELD_INFO) r14     // Catch:{ NumberFormatException -> 0x00b5 }
            r24 = r2
            r25 = r11
            goto L_0x0108
        L_0x00b3:
            r23 = r15
        L_0x00b5:
            java.lang.Object r14 = r2.get(r7)
            r18 = r14
            java.lang.String r18 = (java.lang.String) r18
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            java.lang.String r15 = "update xNode "
            r14.append(r15)
            r14.append(r7)
            java.lang.String r15 = " ["
            r14.append(r15)
            r14.append(r1)
            r14.append(r9)
            java.lang.String r14 = r14.toString()
            android.util.Log.d(r10, r14)
            com.sec.ims.configuration.DATA$DM_FIELD_INFO r14 = new com.sec.ims.configuration.DATA$DM_FIELD_INFO
            java.lang.String r15 = r7.replace(r5, r6)
            r24 = r2
            r25 = r11
            r2 = -1
            r11 = 0
            r14.<init>(r2, r11, r15)
            java.lang.String r2 = "LBO_P-CSCF_Address"
            boolean r2 = r7.contains(r2)
            if (r2 == 0) goto L_0x0108
            java.util.Optional r2 = java.util.Optional.ofNullable(r1)
            com.sec.internal.ims.settings.DmConfigModule$$ExternalSyntheticLambda0 r11 = new com.sec.internal.ims.settings.DmConfigModule$$ExternalSyntheticLambda0
            r11.<init>()
            r2.ifPresent(r11)
            if (r1 == 0) goto L_0x0107
            java.lang.String r1 = r1.replace(r4, r6)
            goto L_0x0108
        L_0x0107:
            r1 = r6
        L_0x0108:
            r2 = r18
            if (r14 != 0) goto L_0x0118
            r1 = r27
            r14 = r22
            r15 = r23
            r2 = r24
            r11 = r25
            goto L_0x007c
        L_0x0118:
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r15 = "Idx ["
            r11.append(r15)
            r11.append(r7)
            java.lang.String r15 = "], Type ["
            r11.append(r15)
            int r15 = r14.getType()
            r11.append(r15)
            java.lang.String r15 = "], Val ["
            r11.append(r15)
            r11.append(r2)
            java.lang.String r15 = "] => ["
            r11.append(r15)
            r11.append(r1)
            r11.append(r9)
            java.lang.String r11 = r11.toString()
            android.util.Log.d(r10, r11)
            java.lang.String r11 = "10"
            boolean r11 = android.text.TextUtils.equals(r7, r11)
            if (r11 != 0) goto L_0x0167
            java.lang.String r11 = "72"
            boolean r11 = android.text.TextUtils.equals(r7, r11)
            if (r11 != 0) goto L_0x0167
            java.lang.String r11 = "116"
            boolean r11 = android.text.TextUtils.equals(r7, r11)
            if (r11 == 0) goto L_0x0164
            goto L_0x0167
        L_0x0164:
            r18 = r4
            goto L_0x019f
        L_0x0167:
            r8.append(r4)
            r8.append(r7)
            java.lang.String r11 = ":"
            r8.append(r11)
            r8.append(r2)
            java.lang.String r11 = ","
            r8.append(r11)
            r8.append(r1)
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            r18 = r4
            java.lang.String r4 = "OMADM update : "
            r15.append(r4)
            r15.append(r7)
            r15.append(r11)
            r15.append(r2)
            r15.append(r11)
            r15.append(r1)
            java.lang.String r4 = r15.toString()
            android.util.Log.d(r10, r4)
        L_0x019f:
            int r4 = r14.getType()
            if (r4 == 0) goto L_0x01c4
            r2 = 3
            if (r4 == r2) goto L_0x01bc
            r2 = 4
            if (r4 == r2) goto L_0x01ac
            goto L_0x01f3
        L_0x01ac:
            java.lang.String r2 = "1"
            boolean r1 = r2.equals(r1)
            android.content.Context r2 = r0.mContext
            java.lang.String r4 = r14.getName()
            com.sec.internal.helper.DmConfigHelper.setImsSwitch(r2, r4, r1, r3)
            goto L_0x01f3
        L_0x01bc:
            java.lang.String r2 = r14.getName()
            r13.put(r2, r1)
            goto L_0x01f3
        L_0x01c4:
            java.util.ArrayList<java.lang.String> r4 = r0.mNvList
            java.lang.String r11 = r14.getName()
            java.lang.String r11 = r11.replace(r5, r6)
            boolean r4 = r4.contains(r11)
            if (r4 == 0) goto L_0x01f6
            java.lang.String r2 = r14.getName()
            r12.put(r2, r1)
            int r1 = java.lang.Integer.parseInt(r7)
            int r2 = java.lang.Integer.parseInt(r21)
            if (r2 == r1) goto L_0x01f1
            int r2 = java.lang.Integer.parseInt(r20)
            if (r2 == r1) goto L_0x01f1
            int r2 = java.lang.Integer.parseInt(r19)
            if (r2 != r1) goto L_0x01f3
        L_0x01f1:
            r17 = 1
        L_0x01f3:
            r4 = r25
            goto L_0x0205
        L_0x01f6:
            boolean r2 = android.text.TextUtils.equals(r2, r1)
            if (r2 != 0) goto L_0x01f3
            java.lang.String r2 = r14.getName()
            r4 = r25
            r4.put(r2, r1)
        L_0x0205:
            r1 = r27
            r11 = r4
            r4 = r18
            r14 = r22
            r15 = r23
            r2 = r24
            goto L_0x007c
        L_0x0212:
            r4 = r11
            com.sec.internal.helper.SimpleEventLog r1 = r0.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r5 = "OMADM update :"
            r2.append(r5)
            r2.append(r8)
            java.lang.String r2 = r2.toString()
            r1.logAndAdd(r2)
            int r1 = r4.size()
            java.lang.String r2 = "INTERNAL_KEY_PROCESS_NAME"
            if (r1 <= 0) goto L_0x0237
            r1 = r16
            r4.put(r2, r1)
            goto L_0x0239
        L_0x0237:
            r1 = r16
        L_0x0239:
            int r5 = r12.size()
            if (r5 <= 0) goto L_0x0242
            r12.put(r2, r1)
        L_0x0242:
            if (r17 == 0) goto L_0x0326
            java.util.Set r1 = r12.keySet()
            int r2 = r0.mOmadmProvisioningTransactionId
            r4 = 2
            r5 = -1
            if (r2 == r5) goto L_0x02f2
            com.sec.internal.ims.settings.DmConfigModule$DmContentValues r5 = r0.mDmContentValues
            r5.addConfigData(r2, r4, r12)
            com.sec.internal.ims.settings.DmConfigModule$DmContentValues r2 = r0.mDmContentValues
            int r5 = r0.mOmadmProvisioningTransactionId
            android.content.ContentValues r2 = r2.getConfigData(r5, r4)
            if (r2 != 0) goto L_0x025f
            r5 = 1
            return r5
        L_0x025f:
            java.util.Iterator r5 = r1.iterator()
        L_0x0263:
            boolean r6 = r5.hasNext()
            if (r6 == 0) goto L_0x0295
            java.lang.Object r6 = r5.next()
            java.lang.String r6 = (java.lang.String) r6
            com.sec.internal.helper.SimpleEventLog r7 = r0.mEventLog
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r10 = "OMADM update "
            r8.append(r10)
            r8.append(r6)
            java.lang.String r10 = " = ["
            r8.append(r10)
            java.lang.Object r6 = r2.get(r6)
            r8.append(r6)
            r8.append(r9)
            java.lang.String r6 = r8.toString()
            r7.logAndAdd(r6)
            goto L_0x0263
        L_0x0295:
            java.util.List r5 = com.sec.ims.configuration.DATA.DM_FIELD_LIST
            int r6 = java.lang.Integer.parseInt(r21)
            java.lang.Object r5 = r5.get(r6)
            com.sec.ims.configuration.DATA$DM_FIELD_INFO r5 = (com.sec.ims.configuration.DATA.DM_FIELD_INFO) r5
            java.lang.String r5 = r5.getName()
            boolean r5 = r1.contains(r5)
            if (r5 == 0) goto L_0x0324
            java.util.List r5 = com.sec.ims.configuration.DATA.DM_FIELD_LIST
            int r6 = java.lang.Integer.parseInt(r20)
            java.lang.Object r5 = r5.get(r6)
            com.sec.ims.configuration.DATA$DM_FIELD_INFO r5 = (com.sec.ims.configuration.DATA.DM_FIELD_INFO) r5
            java.lang.String r5 = r5.getName()
            boolean r5 = r1.contains(r5)
            if (r5 == 0) goto L_0x0324
            java.util.List r5 = com.sec.ims.configuration.DATA.DM_FIELD_LIST
            int r6 = java.lang.Integer.parseInt(r19)
            java.lang.Object r5 = r5.get(r6)
            com.sec.ims.configuration.DATA$DM_FIELD_INFO r5 = (com.sec.ims.configuration.DATA.DM_FIELD_INFO) r5
            java.lang.String r5 = r5.getName()
            boolean r1 = r1.contains(r5)
            if (r1 == 0) goto L_0x0324
            java.lang.Integer r1 = java.lang.Integer.valueOf(r29)
            r0.removeMessages(r4, r1)
            com.sec.internal.ims.settings.DmConfigModule$DmContentValues r1 = r0.mDmContentValues
            int r5 = r0.mOmadmProvisioningTransactionId
            r1.removeConfigData(r5, r4)
            r1 = -1
            r0.mOmadmProvisioningTransactionId = r1
            android.net.Uri r1 = com.sec.ims.settings.NvConfiguration.URI
            android.net.Uri r1 = com.sec.internal.helper.UriUtil.buildUri((android.net.Uri) r1, (int) r3)
            r0.insertData(r1, r2)
            goto L_0x0324
        L_0x02f2:
            com.sec.internal.ims.settings.DmConfigModule$DmContentValues r1 = r0.mDmContentValues
            int r1 = r1.getNewTransactionId()
            r0.mOmadmProvisioningTransactionId = r1
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "OMADM update, created transaction : "
            r1.append(r2)
            int r2 = r0.mOmadmProvisioningTransactionId
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r10, r1)
            com.sec.internal.ims.settings.DmConfigModule$DmContentValues r1 = r0.mDmContentValues
            int r2 = r0.mOmadmProvisioningTransactionId
            r1.addConfigData(r2, r4, r12)
            java.lang.Integer r1 = java.lang.Integer.valueOf(r29)
            android.os.Message r1 = r0.obtainMessage(r4, r1)
            r2 = 5000(0x1388, double:2.4703E-320)
            r0.sendMessageDelayed(r1, r2)
        L_0x0324:
            r0 = 1
            return r0
        L_0x0326:
            r1 = r28
            if (r1 >= 0) goto L_0x034e
            java.lang.String r1 = "immediately write DM config"
            android.util.Log.d(r10, r1)
            int r1 = r4.size()
            if (r1 == 0) goto L_0x033e
            java.lang.String r1 = "content://com.samsung.rcs.dmconfigurationprovider/omadm/"
            android.net.Uri r1 = com.sec.internal.helper.UriUtil.buildUri((java.lang.String) r1, (int) r3)
            r0.insertData(r1, r4)
        L_0x033e:
            int r1 = r12.size()
            if (r1 == 0) goto L_0x0366
            android.net.Uri r1 = com.sec.ims.settings.NvConfiguration.URI
            android.net.Uri r1 = com.sec.internal.helper.UriUtil.buildUri((android.net.Uri) r1, (int) r3)
            r0.insertData(r1, r12)
            goto L_0x0366
        L_0x034e:
            int r2 = r4.size()
            if (r2 == 0) goto L_0x035a
            com.sec.internal.ims.settings.DmConfigModule$DmContentValues r2 = r0.mDmContentValues
            r3 = 0
            r2.addConfigData(r1, r3, r4)
        L_0x035a:
            int r2 = r12.size()
            if (r2 == 0) goto L_0x0366
            com.sec.internal.ims.settings.DmConfigModule$DmContentValues r2 = r0.mDmContentValues
            r3 = 1
            r2.addConfigData(r1, r3, r12)
        L_0x0366:
            int r1 = r13.size()
            if (r1 <= 0) goto L_0x0378
            android.content.Context r0 = r0.mContext
            android.content.ContentResolver r0 = r0.getContentResolver()
            android.net.Uri r1 = com.sec.internal.constants.ims.settings.GlobalSettingsConstants.CONTENT_URI
            r2 = 0
            r0.update(r1, r13, r2, r2)
        L_0x0378:
            r0 = 1
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.DmConfigModule.updateConfigValues(android.content.ContentValues, int, int):boolean");
    }

    private void insertData(Uri uri, ContentValues contentValues) {
        if (!contentValues.containsKey(INTERNAL_KEY_PROCESS_NAME)) {
            contentValues.put(INTERNAL_KEY_PROCESS_NAME, PackageUtils.getProcessNameById(this.mContext, Binder.getCallingPid()));
        }
        this.mContext.getContentResolver().insert(uri, contentValues);
    }

    public void handleMessage(Message message) {
        Log.d(LOG_TAG, "handleMessage: evt=" + message.what);
        int i = message.what;
        if (i == 1) {
            int i2 = message.arg2;
            if (this.mDmContentValues.allTransactionDone()) {
                this.mRegMgr.onDmConfigurationComplete(i2);
                return;
            }
            ContentValues configData = this.mDmContentValues.getConfigData(message.arg1, 0);
            if (configData == null) {
                Log.e(LOG_TAG, "no opt transactionId " + message.arg1);
            } else {
                this.mDmContentValues.removeConfigData(message.arg1, 0);
                insertData(UriUtil.buildUri(CONFIG_DM_PROVIDER, i2), configData);
            }
            ContentValues configData2 = this.mDmContentValues.getConfigData(message.arg1, 1);
            if (configData2 == null) {
                Log.e(LOG_TAG, "no nv transactionId " + message.arg1);
            } else {
                this.mDmContentValues.removeConfigData(message.arg1, 1);
                insertData(NvConfiguration.URI, configData2);
            }
            this.mRegMgr.onDmConfigurationComplete(i2);
            if (this.mDmContentValues.allTransactionDone()) {
                Log.d(LOG_TAG, "all config transaction done, " + message.arg1 + ", phoneId " + message.arg2);
                int i3 = message.arg1;
                if (i3 == -2 || i3 == -3) {
                    this.mEventLog.logAndAdd("socket timeout, don't destroy DmContentValues");
                } else {
                    this.mDmContentValues = new DmContentValues();
                }
            }
        } else if (i != 2) {
            Log.e(LOG_TAG, "unknown event");
        } else {
            ContentValues configData3 = this.mDmContentValues.getConfigData(this.mOmadmProvisioningTransactionId, 2);
            if (configData3 == null) {
                Log.e(LOG_TAG, "no pending transaction for : " + this.mOmadmProvisioningTransactionId);
                return;
            }
            Log.d(LOG_TAG, "EVT_FINISH_OMADM_PROV_UPDATE, completing transaction : " + this.mOmadmProvisioningTransactionId);
            this.mDmContentValues.removeConfigData(this.mOmadmProvisioningTransactionId, 2);
            this.mOmadmProvisioningTransactionId = -1;
            insertData(UriUtil.buildUri(NvConfiguration.URI, ((Integer) message.obj).intValue()), configData3);
        }
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of " + getClass().getSimpleName() + ":");
        this.mEventLog.dump();
    }

    static class DmContentValues {
        private static final String LOG_TAG = "DmContentValues";
        protected static final int NUM_OF_MAP = 3;
        protected static final int TYPE_CONFIG_DB = 0;
        protected static final int TYPE_NV = 1;
        protected static final int TYPE_OTA = 2;
        private static int mMaxTransactionId;
        private List<Map<Integer, ContentValues>> mTransactionMaps = new ArrayList();

        DmContentValues() {
            for (int i = 0; i < 3; i++) {
                this.mTransactionMaps.add(new HashMap());
            }
        }

        /* access modifiers changed from: protected */
        public int getNewTransactionId() {
            int i = mMaxTransactionId + 1;
            mMaxTransactionId = i;
            return i;
        }

        /* access modifiers changed from: protected */
        public void addConfigData(int i, int i2, ContentValues contentValues) {
            ContentValues contentValues2;
            if (this.mTransactionMaps.get(i2).containsKey(Integer.valueOf(i))) {
                contentValues2 = (ContentValues) this.mTransactionMaps.get(i2).get(Integer.valueOf(i));
            } else {
                Log.d(LOG_TAG, "no transaction with transactionId " + i + " create new transaction");
                this.mTransactionMaps.get(i2).put(Integer.valueOf(i), new ContentValues());
                contentValues2 = (ContentValues) this.mTransactionMaps.get(i2).get(Integer.valueOf(i));
                if (i > mMaxTransactionId) {
                    mMaxTransactionId = i;
                }
            }
            contentValues2.putAll(contentValues);
            this.mTransactionMaps.get(i2).put(Integer.valueOf(i), contentValues2);
        }

        /* access modifiers changed from: protected */
        public ContentValues getConfigData(int i, int i2) {
            if (this.mTransactionMaps.size() == 0 || this.mTransactionMaps.get(i2) == null || !this.mTransactionMaps.get(i2).containsKey(Integer.valueOf(i))) {
                return null;
            }
            return (ContentValues) this.mTransactionMaps.get(i2).get(Integer.valueOf(i));
        }

        /* access modifiers changed from: protected */
        public void removeConfigData(int i, int i2) {
            if (this.mTransactionMaps.get(i2).containsKey(Integer.valueOf(i))) {
                this.mTransactionMaps.get(i2).remove(Integer.valueOf(i));
            }
        }

        /* access modifiers changed from: protected */
        public boolean allTransactionDone() {
            for (int i = 0; i < 3; i++) {
                if (!this.mTransactionMaps.get(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
    }
}
