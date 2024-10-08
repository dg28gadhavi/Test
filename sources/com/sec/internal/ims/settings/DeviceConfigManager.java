package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.telephony.ITelephony;
import com.sec.ims.configuration.DATA;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.ImsSettings;
import com.sec.ims.settings.NvConfiguration;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.XmlUtils;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.config.adapters.StorageAdapter;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.settings.SettingsProviderUtility;
import com.sec.internal.ims.util.CscParser;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class DeviceConfigManager {
    private static final String CONFIG_URI = "content://com.samsung.rcs.dmconfigurationprovider/";
    public static final String DEFAULTMSGAPPINUSE = "defaultmsgappinuse";
    private static final String DEFAULT_DATABASE_NAME = "DEFAULT";
    private static final String DEFAULT_DMCONFIG_NAME = "default";
    public static final String IMS = "ims";
    private static final String IMS_TEST_MODE = "IMS_TEST_MODE";
    private static final Object LOCK = new Object();
    public static final String LOG_TAG = "DeviceConfigManager";
    public static final String NV_INIT_DONE = "nv_init_done";
    public static final String NV_VERSION_DEFAULT = "1";
    public static final String NV_VERSION_USC_NR_OOB = "2";
    private static final String OMADM_DB_NAME_PREFIX = "OMADM_";
    public static final String OMADM_PREFIX = "omadm/./3GPP_IMS/";
    private static final String OMC_CODE_PROPERTY = "ro.csc.sales_code";
    public static final String RCS = "rcs";
    public static final String RCS_SWITCH = "rcsswitch";
    public static final String VOLTE = "volte";
    private Context mContext;
    private DebugConfigStorage mDebugConfigStorage;
    protected IStorageAdapter mDmStorage = null;
    private SimpleEventLog mEventLog;
    private ImsServiceSwitch mImsServiceSwitch;
    private SimConstants.SIM_STATE mLastSimState = SimConstants.SIM_STATE.UNKNOWN;
    private Mno mMno = Mno.DEFAULT;
    private String mMvnoName = "";
    private ArrayList<String> mNvList = new ArrayList<>();
    private NvStorage mNvStorage = null;
    private int mPhoneId = 0;
    private ImsProfileCache mProfileCache;
    private SmsSetting mSmsSetting;
    private UserConfigStorage mUserConfigStorage;

    public DeviceConfigManager(Context context, int i) {
        boolean z = false;
        this.mContext = context;
        this.mPhoneId = i;
        this.mEventLog = new SimpleEventLog(context, i, LOG_TAG, 300);
        this.mMno = Mno.DEFAULT;
        String previousMno = GlobalSettingsManager.getInstance(this.mContext, i).getGlobalSettings().getPreviousMno();
        if ("".equals(previousMno)) {
            this.mMno = Mno.fromSalesCode(OmcCode.getNWCode(i));
        } else {
            this.mMno = Mno.fromName(previousMno);
        }
        Mno mno = this.mMno;
        if (mno == Mno.GCF && !TextUtils.equals(mno.getName().toUpperCase(), previousMno.toUpperCase())) {
            z = true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "needToRefreshGcf : " + z);
        ImsProfileCache imsProfileCache = new ImsProfileCache(this.mContext, this.mMno.getName(), i);
        this.mProfileCache = imsProfileCache;
        imsProfileCache.load(z);
        this.mSmsSetting = new SmsSetting(this.mContext, this.mPhoneId);
        updateNvList();
        if (!this.mNvList.isEmpty()) {
            this.mNvStorage = new NvStorage(this.mContext, this.mMno.getMatchedNetworkCode(OmcCode.getNWCode(this.mPhoneId)), this.mPhoneId);
        }
        this.mDmStorage = new StorageAdapter();
        this.mUserConfigStorage = new UserConfigStorage(this.mContext, previousMno, i);
        this.mDebugConfigStorage = new DebugConfigStorage(this.mContext);
        this.mImsServiceSwitch = new ImsServiceSwitch(this.mContext, this.mPhoneId);
        if (SettingsProviderUtility.getDbCreatState(this.mContext) == SettingsProviderUtility.DB_CREAT_STATE.DB_CREATING_FAIL && restoreDefaultImsProfile()) {
            SettingsProviderUtility.setDbCreated(this.mContext, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void readInitialConfigFromXml(XmlPullParser xmlPullParser, List<String> list, SparseArray<String> sparseArray, ContentValues contentValues, Map<String, String> map) {
        boolean containsKey = contentValues.containsKey("omadm/./3GPP_IMS/nv_init_done");
        int i = -1;
        while (true) {
            try {
                int next = xmlPullParser.next();
                if (next == 1) {
                    return;
                }
                if (next == 2 && ImsConstants.Intents.EXTRA_UPDATED_ITEM.equalsIgnoreCase(xmlPullParser.getName())) {
                    i = Integer.parseInt(xmlPullParser.getAttributeValue(0));
                } else if (next == 3) {
                    if ("configuration".equalsIgnoreCase(xmlPullParser.getName())) {
                        return;
                    }
                } else if (next == 4 && xmlPullParser.getText().trim().length() > 0) {
                    String replace = ((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(i)).getName().replace("./3GPP_IMS/", "");
                    if (this.mNvList.contains(replace) && containsKey) {
                        if (contentValues.containsKey("omadm/./3GPP_IMS/" + replace)) {
                        }
                    }
                    if (!map.containsKey("omadm/./3GPP_IMS/" + replace)) {
                        String text = xmlPullParser.getText();
                        sparseArray.put(i, text);
                        SimpleEventLog simpleEventLog = this.mEventLog;
                        int i2 = this.mPhoneId;
                        simpleEventLog.logAndAdd(i2, "Found new item. Read from " + list + " - " + replace + " = [" + text + "]");
                    }
                }
            } catch (IOException | NumberFormatException | XmlPullParserException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public GlobalSettingsRepo getGlobalSettingsRepo() {
        return GlobalSettingsManager.getInstance(this.mContext, this.mPhoneId).getGlobalSettings();
    }

    /* access modifiers changed from: package-private */
    public ImsProfileCache getProfileCache() {
        return this.mProfileCache;
    }

    /* access modifiers changed from: package-private */
    public SmsSetting getSmsSetting() {
        return this.mSmsSetting;
    }

    /* access modifiers changed from: package-private */
    public UserConfigStorage getUserConfigStorage() {
        return this.mUserConfigStorage;
    }

    /* access modifiers changed from: package-private */
    public DebugConfigStorage getDebugConfigStorage() {
        return this.mDebugConfigStorage;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<String> getNvList() {
        return this.mNvList;
    }

    private void updateNvList() {
        this.mNvList.clear();
        ArrayList<String> nvList = getNvList(this.mMno.getMatchedNetworkCode(OmcCode.getNWCode(this.mPhoneId)));
        this.mNvList = nvList;
        if (!nvList.isEmpty()) {
            this.mNvList.add(NV_INIT_DONE);
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i = this.mPhoneId;
        simpleEventLog.logAndAdd(i, "updateNvList(" + this.mMno.getMatchedNetworkCode(OmcCode.getNWCode(this.mPhoneId)) + ") : nv list : " + Arrays.toString(this.mNvList.toArray()));
    }

    /* JADX WARNING: Removed duplicated region for block: B:72:0x01cd A[Catch:{ all -> 0x01bf, all -> 0x01c4, all -> 0x0092, all -> 0x0097 }] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x01f2 A[Catch:{ all -> 0x01bf, all -> 0x01c4, all -> 0x0092, all -> 0x0097 }] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x01f3 A[Catch:{ all -> 0x01bf, all -> 0x01c4, all -> 0x0092, all -> 0x0097 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean initStorage() {
        /*
            r13 = this;
            java.lang.Object r0 = LOCK
            monitor-enter(r0)
            com.sec.internal.interfaces.ims.config.IStorageAdapter r1 = r13.mDmStorage     // Catch:{ all -> 0x0222 }
            int r1 = r1.getState()     // Catch:{ all -> 0x0222 }
            r2 = 1
            if (r1 == r2) goto L_0x0220
            com.sec.internal.constants.Mno r1 = r13.mMno     // Catch:{ all -> 0x0222 }
            java.lang.String r3 = com.sec.internal.helper.OmcCode.get()     // Catch:{ all -> 0x0222 }
            java.lang.String r1 = r1.getMatchedSalesCode(r3)     // Catch:{ all -> 0x0222 }
            java.lang.String r3 = r13.getDbTableName(r1)     // Catch:{ all -> 0x0222 }
            com.sec.internal.helper.SimpleEventLog r4 = r13.mEventLog     // Catch:{ all -> 0x0222 }
            int r5 = r13.mPhoneId     // Catch:{ all -> 0x0222 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0222 }
            r6.<init>()     // Catch:{ all -> 0x0222 }
            java.lang.String r7 = "DM CONFIG DB : "
            r6.append(r7)     // Catch:{ all -> 0x0222 }
            r6.append(r3)     // Catch:{ all -> 0x0222 }
            java.lang.String r7 = ", Mno : "
            r6.append(r7)     // Catch:{ all -> 0x0222 }
            r6.append(r1)     // Catch:{ all -> 0x0222 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0222 }
            r4.logAndAdd(r5, r6)     // Catch:{ all -> 0x0222 }
            com.sec.internal.interfaces.ims.config.IStorageAdapter r4 = r13.mDmStorage     // Catch:{ all -> 0x0222 }
            android.content.Context r5 = r13.mContext     // Catch:{ all -> 0x0222 }
            int r6 = r13.mPhoneId     // Catch:{ all -> 0x0222 }
            r4.open(r5, r3, r6)     // Catch:{ all -> 0x0222 }
            r13.updateNvList()     // Catch:{ all -> 0x0222 }
            android.content.ContentValues r3 = new android.content.ContentValues     // Catch:{ all -> 0x0222 }
            r3.<init>()     // Catch:{ all -> 0x0222 }
            java.util.ArrayList<java.lang.String> r4 = r13.mNvList     // Catch:{ all -> 0x0222 }
            boolean r4 = r4.isEmpty()     // Catch:{ all -> 0x0222 }
            r5 = 0
            r6 = 0
            if (r4 != 0) goto L_0x00a1
            com.sec.internal.ims.settings.NvStorage r4 = r13.mNvStorage     // Catch:{ all -> 0x0222 }
            if (r4 != 0) goto L_0x0070
            com.sec.internal.ims.settings.NvStorage r4 = new com.sec.internal.ims.settings.NvStorage     // Catch:{ all -> 0x0222 }
            android.content.Context r7 = r13.mContext     // Catch:{ all -> 0x0222 }
            com.sec.internal.constants.Mno r8 = r13.mMno     // Catch:{ all -> 0x0222 }
            int r9 = r13.mPhoneId     // Catch:{ all -> 0x0222 }
            java.lang.String r9 = com.sec.internal.helper.OmcCode.getNWCode(r9)     // Catch:{ all -> 0x0222 }
            java.lang.String r8 = r8.getMatchedNetworkCode(r9)     // Catch:{ all -> 0x0222 }
            int r9 = r13.mPhoneId     // Catch:{ all -> 0x0222 }
            r4.<init>(r7, r8, r9)     // Catch:{ all -> 0x0222 }
            r13.mNvStorage = r4     // Catch:{ all -> 0x0222 }
        L_0x0070:
            com.sec.internal.ims.settings.NvStorage r4 = r13.mNvStorage     // Catch:{ all -> 0x0222 }
            java.lang.String r7 = "omadm"
            android.database.Cursor r4 = r4.query(r7, r5)     // Catch:{ all -> 0x0222 }
            if (r4 == 0) goto L_0x009c
            boolean r7 = r4.moveToFirst()     // Catch:{ all -> 0x0092 }
            if (r7 == 0) goto L_0x009c
        L_0x0080:
            java.lang.String r7 = r4.getString(r6)     // Catch:{ all -> 0x0092 }
            java.lang.String r8 = r4.getString(r2)     // Catch:{ all -> 0x0092 }
            r3.put(r7, r8)     // Catch:{ all -> 0x0092 }
            boolean r7 = r4.moveToNext()     // Catch:{ all -> 0x0092 }
            if (r7 != 0) goto L_0x0080
            goto L_0x009c
        L_0x0092:
            r13 = move-exception
            r4.close()     // Catch:{ all -> 0x0097 }
            goto L_0x009b
        L_0x0097:
            r1 = move-exception
            r13.addSuppressed(r1)     // Catch:{ all -> 0x0222 }
        L_0x009b:
            throw r13     // Catch:{ all -> 0x0222 }
        L_0x009c:
            if (r4 == 0) goto L_0x00a1
            r4.close()     // Catch:{ all -> 0x0222 }
        L_0x00a1:
            android.util.ArrayMap r4 = new android.util.ArrayMap     // Catch:{ all -> 0x0222 }
            r4.<init>()     // Catch:{ all -> 0x0222 }
            com.sec.internal.interfaces.ims.config.IStorageAdapter r7 = r13.mDmStorage     // Catch:{ all -> 0x0222 }
            java.lang.String r8 = "omadm/*"
            java.util.Map r7 = r7.readAll(r8)     // Catch:{ all -> 0x0222 }
            java.util.Optional r7 = java.util.Optional.ofNullable(r7)     // Catch:{ all -> 0x0222 }
            com.sec.internal.ims.settings.DeviceConfigManager$$ExternalSyntheticLambda0 r8 = new com.sec.internal.ims.settings.DeviceConfigManager$$ExternalSyntheticLambda0     // Catch:{ all -> 0x0222 }
            r8.<init>(r4)     // Catch:{ all -> 0x0222 }
            r7.ifPresent(r8)     // Catch:{ all -> 0x0222 }
            android.util.SparseArray r4 = r13.getDefaultDmConfig(r1, r3, r4)     // Catch:{ all -> 0x0222 }
            r7 = 2
            r8 = 3
            if (r4 == 0) goto L_0x0163
            int r9 = r4.size()     // Catch:{ all -> 0x0222 }
            if (r9 <= 0) goto L_0x0163
            java.lang.String r9 = "LRA"
            boolean r1 = r9.equals(r1)     // Catch:{ all -> 0x0222 }
            if (r1 == 0) goto L_0x0143
            android.content.Context r1 = r13.mContext     // Catch:{ all -> 0x0222 }
            int r9 = r13.mPhoneId     // Catch:{ all -> 0x0222 }
            boolean r1 = com.sec.internal.ims.util.ImsUtil.isCdmalessModel(r1, r9)     // Catch:{ all -> 0x0222 }
            if (r1 != 0) goto L_0x0143
            r1 = 4
            java.lang.Integer[] r1 = new java.lang.Integer[r1]     // Catch:{ all -> 0x0222 }
            java.lang.String r9 = "93"
            int r9 = java.lang.Integer.parseInt(r9)     // Catch:{ all -> 0x0222 }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x0222 }
            r1[r6] = r9     // Catch:{ all -> 0x0222 }
            java.lang.String r9 = "94"
            int r9 = java.lang.Integer.parseInt(r9)     // Catch:{ all -> 0x0222 }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x0222 }
            r1[r2] = r9     // Catch:{ all -> 0x0222 }
            java.lang.String r9 = "31"
            int r9 = java.lang.Integer.parseInt(r9)     // Catch:{ all -> 0x0222 }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x0222 }
            r1[r7] = r9     // Catch:{ all -> 0x0222 }
            java.lang.String r9 = "133"
            int r9 = java.lang.Integer.parseInt(r9)     // Catch:{ all -> 0x0222 }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x0222 }
            r1[r8] = r9     // Catch:{ all -> 0x0222 }
            java.util.List r1 = java.util.Arrays.asList(r1)     // Catch:{ all -> 0x0222 }
            java.util.Iterator r1 = r1.iterator()     // Catch:{ all -> 0x0222 }
        L_0x0115:
            boolean r9 = r1.hasNext()     // Catch:{ all -> 0x0222 }
            if (r9 == 0) goto L_0x0143
            java.lang.Object r9 = r1.next()     // Catch:{ all -> 0x0222 }
            java.lang.Integer r9 = (java.lang.Integer) r9     // Catch:{ all -> 0x0222 }
            int r10 = r9.intValue()     // Catch:{ all -> 0x0222 }
            java.lang.Object r10 = r4.get(r10)     // Catch:{ all -> 0x0222 }
            java.lang.String r10 = (java.lang.String) r10     // Catch:{ all -> 0x0222 }
            java.util.Optional r10 = java.util.Optional.ofNullable(r10)     // Catch:{ all -> 0x0222 }
            java.lang.String r11 = "1"
            com.sec.internal.ims.settings.DeviceConfigManager$$ExternalSyntheticLambda1 r12 = new com.sec.internal.ims.settings.DeviceConfigManager$$ExternalSyntheticLambda1     // Catch:{ all -> 0x0222 }
            r12.<init>(r11)     // Catch:{ all -> 0x0222 }
            java.util.Optional r10 = r10.filter(r12)     // Catch:{ all -> 0x0222 }
            com.sec.internal.ims.settings.DeviceConfigManager$$ExternalSyntheticLambda2 r11 = new com.sec.internal.ims.settings.DeviceConfigManager$$ExternalSyntheticLambda2     // Catch:{ all -> 0x0222 }
            r11.<init>(r13, r9, r4)     // Catch:{ all -> 0x0222 }
            r10.ifPresent(r11)     // Catch:{ all -> 0x0222 }
            goto L_0x0115
        L_0x0143:
            java.util.ArrayList<java.lang.String> r1 = r13.mNvList     // Catch:{ all -> 0x0222 }
            r13.initDmConfig(r4, r1)     // Catch:{ all -> 0x0222 }
            java.util.ArrayList<java.lang.String> r1 = r13.mNvList     // Catch:{ all -> 0x0222 }
            boolean r1 = r1.isEmpty()     // Catch:{ all -> 0x0222 }
            if (r1 != 0) goto L_0x0163
            java.lang.String r1 = "omadm/./3GPP_IMS/nv_init_done"
            boolean r1 = r3.containsKey(r1)     // Catch:{ all -> 0x0222 }
            if (r1 != 0) goto L_0x0163
            android.content.Context r1 = r13.mContext     // Catch:{ all -> 0x0222 }
            java.lang.String r3 = "nv_init_done"
            java.lang.String r4 = "1"
            int r9 = r13.mPhoneId     // Catch:{ all -> 0x0222 }
            com.sec.ims.settings.NvConfiguration.set(r1, r3, r4, r9)     // Catch:{ all -> 0x0222 }
        L_0x0163:
            java.lang.String[] r1 = new java.lang.String[r8]     // Catch:{ all -> 0x0222 }
            java.lang.String r3 = "omadm/./3GPP_IMS/VOLTE_ENABLED"
            r1[r6] = r3     // Catch:{ all -> 0x0222 }
            java.lang.String r3 = "omadm/./3GPP_IMS/EAB_SETTING"
            r1[r2] = r3     // Catch:{ all -> 0x0222 }
            java.lang.String r3 = "omadm/./3GPP_IMS/POLL_LIST_SUB_EXP"
            r1[r7] = r3     // Catch:{ all -> 0x0222 }
            android.database.Cursor r1 = r13.readMultipleDm(r1, r5, r5)     // Catch:{ all -> 0x0222 }
            r3 = -1
            if (r1 == 0) goto L_0x01c9
            int r4 = r1.getCount()     // Catch:{ all -> 0x01bf }
            if (r4 <= 0) goto L_0x01c9
            boolean r4 = r1.moveToFirst()     // Catch:{ all -> 0x01bf }
            if (r4 == 0) goto L_0x01c9
            r4 = r3
            r5 = r4
        L_0x0186:
            java.lang.String r7 = r1.getString(r6)     // Catch:{ all -> 0x01bf }
            java.lang.String r8 = r1.getString(r2)     // Catch:{ all -> 0x01bf }
            java.lang.String r9 = "omadm/./3GPP_IMS/VOLTE_ENABLED"
            boolean r9 = r9.equalsIgnoreCase(r7)     // Catch:{ all -> 0x01bf }
            if (r9 == 0) goto L_0x019d
            java.lang.String r3 = "1"
            boolean r3 = r8.equalsIgnoreCase(r3)     // Catch:{ all -> 0x01bf }
            goto L_0x01b8
        L_0x019d:
            java.lang.String r9 = "omadm/./3GPP_IMS/EAB_SETTING"
            boolean r9 = r9.equalsIgnoreCase(r7)     // Catch:{ all -> 0x01bf }
            if (r9 == 0) goto L_0x01ac
            java.lang.String r4 = "1"
            boolean r4 = r8.equalsIgnoreCase(r4)     // Catch:{ all -> 0x01bf }
            goto L_0x01b8
        L_0x01ac:
            java.lang.String r9 = "omadm/./3GPP_IMS/POLL_LIST_SUB_EXP"
            boolean r7 = r9.equalsIgnoreCase(r7)     // Catch:{ all -> 0x01bf }
            if (r7 == 0) goto L_0x01b8
            int r5 = java.lang.Integer.parseInt(r8)     // Catch:{ all -> 0x01bf }
        L_0x01b8:
            boolean r7 = r1.moveToNext()     // Catch:{ all -> 0x01bf }
            if (r7 != 0) goto L_0x0186
            goto L_0x01cb
        L_0x01bf:
            r13 = move-exception
            r1.close()     // Catch:{ all -> 0x01c4 }
            goto L_0x01c8
        L_0x01c4:
            r1 = move-exception
            r13.addSuppressed(r1)     // Catch:{ all -> 0x0222 }
        L_0x01c8:
            throw r13     // Catch:{ all -> 0x0222 }
        L_0x01c9:
            r4 = r3
            r5 = r4
        L_0x01cb:
            if (r1 == 0) goto L_0x01d0
            r1.close()     // Catch:{ all -> 0x0222 }
        L_0x01d0:
            com.sec.internal.constants.Mno r1 = r13.mMno     // Catch:{ all -> 0x0222 }
            boolean r1 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((com.sec.internal.constants.Mno) r1)     // Catch:{ all -> 0x0222 }
            if (r1 != 0) goto L_0x0216
            com.sec.internal.constants.Mno r1 = r13.mMno     // Catch:{ all -> 0x0222 }
            boolean r1 = r1.isOce()     // Catch:{ all -> 0x0222 }
            if (r1 != 0) goto L_0x0216
            com.sec.internal.constants.Mno r1 = r13.mMno     // Catch:{ all -> 0x0222 }
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.ROGERS     // Catch:{ all -> 0x0222 }
            if (r1 == r6) goto L_0x0216
            boolean r1 = r1.isLatin()     // Catch:{ all -> 0x0222 }
            if (r1 == 0) goto L_0x01f3
            com.sec.internal.constants.Mno r1 = r13.mMno     // Catch:{ all -> 0x0222 }
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.TCE     // Catch:{ all -> 0x0222 }
            if (r1 == r6) goto L_0x01f3
            goto L_0x0216
        L_0x01f3:
            com.sec.internal.constants.Mno r1 = r13.mMno     // Catch:{ all -> 0x0222 }
            boolean r1 = r1.isKor()     // Catch:{ all -> 0x0222 }
            if (r1 != 0) goto L_0x0201
            com.sec.internal.constants.Mno r1 = r13.mMno     // Catch:{ all -> 0x0222 }
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.BELL     // Catch:{ all -> 0x0222 }
            if (r1 != r3) goto L_0x0220
        L_0x0201:
            com.sec.internal.constants.Mno r1 = r13.mMno     // Catch:{ all -> 0x0222 }
            boolean r1 = r1.isKor()     // Catch:{ all -> 0x0222 }
            if (r1 == 0) goto L_0x0210
            r1 = 30
            if (r5 == r1) goto L_0x0210
            r13.changePollListSubExp(r1)     // Catch:{ all -> 0x0222 }
        L_0x0210:
            if (r4 == r2) goto L_0x0220
            r13.initEabFeature()     // Catch:{ all -> 0x0222 }
            goto L_0x0220
        L_0x0216:
            if (r3 == r2) goto L_0x021b
            r13.initVoLTEFeature()     // Catch:{ all -> 0x0222 }
        L_0x021b:
            if (r4 == 0) goto L_0x0220
            r13.disableEabFeature()     // Catch:{ all -> 0x0222 }
        L_0x0220:
            monitor-exit(r0)     // Catch:{ all -> 0x0222 }
            return r2
        L_0x0222:
            r13 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0222 }
            throw r13
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.DeviceConfigManager.initStorage():boolean");
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$initStorage$0(Integer num, SparseArray sparseArray, String str) {
        this.mEventLog.logAndAdd(this.mPhoneId, String.format(Locale.US, "initStorage: %s = [0] by default for LRA hVoLTE!", new Object[]{((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(num.intValue())).getName().replace("./3GPP_IMS/", "")}));
        sparseArray.put(num.intValue(), "0");
    }

    private Cursor readAllOfDm(Uri uri) {
        Cursor query;
        String[] strArr = new String[2];
        Map<String, String> readAll = this.mDmStorage.readAll(uri.toString().replaceFirst(CONFIG_URI, ""));
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"PATH", "VALUE"});
        if (readAll != null) {
            try {
                for (Map.Entry next : readAll.entrySet()) {
                    if (!this.mNvList.contains(((String) next.getKey()).replace("omadm/./3GPP_IMS/", ""))) {
                        strArr[0] = (String) next.getKey();
                        strArr[1] = (String) next.getValue();
                        matrixCursor.addRow(strArr);
                    }
                }
            } catch (Throwable unused) {
            }
        } else {
            IMSLog.e(LOG_TAG, this.mPhoneId, "readData is null");
        }
        if (!this.mNvList.isEmpty()) {
            query = this.mNvStorage.query(NvStorage.ID_OMADM, (String[]) null);
            if (query != null) {
                if (query.moveToFirst()) {
                    do {
                        if (this.mNvList.contains(query.getString(0).replace("omadm/./3GPP_IMS/", ""))) {
                            strArr[0] = query.getString(0);
                            strArr[1] = query.getString(1);
                            matrixCursor.addRow(strArr);
                        }
                    } while (query.moveToNext());
                }
            }
            if (query != null) {
                query.close();
            }
        }
        return matrixCursor;
        throw th;
    }

    private Cursor readMultipleDm(String[] strArr, String str, String[] strArr2) {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (String str2 : strArr) {
            if (this.mNvList.contains(str2)) {
                arrayList2.add(str2);
            } else {
                if (!str2.contains("omadm/./3GPP_IMS/")) {
                    str2 = "omadm/./3GPP_IMS/" + str2;
                }
                arrayList.add(str2);
            }
        }
        if (arrayList.size() > 0 && arrayList2.size() > 0) {
            return new MergeCursor(new Cursor[]{this.mDmStorage.query((String[]) arrayList.toArray(new String[0])), this.mNvStorage.query(NvStorage.ID_OMADM, strArr)});
        }
        if (arrayList.size() > 0) {
            return this.mDmStorage.query((String[]) arrayList.toArray(new String[0]));
        }
        if (arrayList2.size() > 0) {
            return this.mNvStorage.query(NvStorage.ID_OMADM, strArr);
        }
        return null;
    }

    private Cursor readSingleDm(Uri uri, String str, String[] strArr) {
        String lastPathSegment = uri.getLastPathSegment();
        if (this.mNvList.contains(lastPathSegment)) {
            IMSLog.d(LOG_TAG, this.mPhoneId, "read from NV");
            return this.mNvStorage.query(NvStorage.ID_OMADM, new String[]{lastPathSegment});
        }
        String uri2 = uri.toString();
        int i = this.mPhoneId;
        IMSLog.d(LOG_TAG, i, "read from DB : " + uri2);
        String read = this.mDmStorage.read(uri2.replaceFirst(CONFIG_URI, ""));
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"PATH", "VALUE"});
        matrixCursor.addRow(new String[]{uri2.replace(CONFIG_URI, "").toLowerCase(Locale.US), read});
        return matrixCursor;
    }

    public int deleteDm(Uri uri) {
        synchronized (LOCK) {
            if (!initStorage()) {
                return 0;
            }
            String uri2 = uri.toString();
            int i = this.mPhoneId;
            IMSLog.d(LOG_TAG, i, "delete uri:" + IMSLog.checker(uri2));
            String replace = uri2.replace(CONFIG_URI, "").replace(NvStorage.ID_OMADM, "");
            if (this.mNvList.contains(replace)) {
                this.mNvStorage.delete(replace);
            }
            if (uri2.matches("^content://com.samsung.rcs.dmconfigurationprovider/[\\.\\w-_/]*")) {
                int delete = this.mDmStorage.delete(uri2.replaceFirst(CONFIG_URI, ""));
                this.mContext.getContentResolver().notifyChange(UriUtil.buildUri(CONFIG_URI, this.mPhoneId), (ContentObserver) null);
                return delete;
            }
            throw new IllegalArgumentException(uri2 + " is not a correct DmConfigurationProvider Uri");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:0x011a, code lost:
        return r11;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.net.Uri insertDm(android.net.Uri r11, android.content.ContentValues r12) {
        /*
            r10 = this;
            java.lang.Object r0 = LOCK
            monitor-enter(r0)
            boolean r1 = r10.initStorage()     // Catch:{ all -> 0x011b }
            if (r1 != 0) goto L_0x000c
            monitor-exit(r0)     // Catch:{ all -> 0x011b }
            r10 = 0
            return r10
        L_0x000c:
            android.content.ContentValues r1 = new android.content.ContentValues     // Catch:{ all -> 0x011b }
            r1.<init>()     // Catch:{ all -> 0x011b }
            java.util.HashMap r2 = new java.util.HashMap     // Catch:{ all -> 0x011b }
            r2.<init>()     // Catch:{ all -> 0x011b }
            java.util.Set r12 = r12.valueSet()     // Catch:{ all -> 0x011b }
            java.util.Iterator r12 = r12.iterator()     // Catch:{ all -> 0x011b }
        L_0x001e:
            boolean r3 = r12.hasNext()     // Catch:{ all -> 0x011b }
            if (r3 == 0) goto L_0x0101
            java.lang.Object r3 = r12.next()     // Catch:{ all -> 0x011b }
            java.util.Map$Entry r3 = (java.util.Map.Entry) r3     // Catch:{ all -> 0x011b }
            java.lang.Object r4 = r3.getValue()     // Catch:{ all -> 0x011b }
            boolean r4 = r4 instanceof java.lang.String     // Catch:{ all -> 0x011b }
            if (r4 == 0) goto L_0x001e
            java.lang.Object r4 = r3.getKey()     // Catch:{ all -> 0x011b }
            java.lang.String r4 = (java.lang.String) r4     // Catch:{ all -> 0x011b }
            java.lang.String r5 = "/"
            int r5 = r4.lastIndexOf(r5)     // Catch:{ all -> 0x011b }
            int r6 = r4.length()     // Catch:{ all -> 0x011b }
            if (r5 != r6) goto L_0x004f
            int r5 = r4.length()     // Catch:{ all -> 0x011b }
            int r5 = r5 + -1
            r6 = 0
            java.lang.String r4 = r4.substring(r6, r5)     // Catch:{ all -> 0x011b }
        L_0x004f:
            java.lang.String r5 = "/"
            int r5 = r4.lastIndexOf(r5)     // Catch:{ all -> 0x011b }
            if (r5 < 0) goto L_0x0064
            java.lang.String r5 = "/"
            int r5 = r4.lastIndexOf(r5)     // Catch:{ all -> 0x011b }
            int r5 = r5 + 1
            java.lang.String r5 = r4.substring(r5)     // Catch:{ all -> 0x011b }
            goto L_0x0065
        L_0x0064:
            r5 = r4
        L_0x0065:
            java.lang.String r6 = "DeviceConfigManager"
            int r7 = r10.mPhoneId     // Catch:{ all -> 0x011b }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x011b }
            r8.<init>()     // Catch:{ all -> 0x011b }
            java.lang.String r9 = "dmItem : "
            r8.append(r9)     // Catch:{ all -> 0x011b }
            r8.append(r5)     // Catch:{ all -> 0x011b }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x011b }
            com.sec.internal.log.IMSLog.d(r6, r7, r8)     // Catch:{ all -> 0x011b }
            java.util.ArrayList<java.lang.String> r6 = r10.mNvList     // Catch:{ all -> 0x011b }
            boolean r6 = r6.contains(r5)     // Catch:{ all -> 0x011b }
            if (r6 == 0) goto L_0x008f
            java.lang.Object r3 = r3.getValue()     // Catch:{ all -> 0x011b }
            java.lang.String r3 = (java.lang.String) r3     // Catch:{ all -> 0x011b }
            r1.put(r5, r3)     // Catch:{ all -> 0x011b }
            goto L_0x001e
        L_0x008f:
            java.lang.String r6 = "omadm/./3GPP_IMS/"
            boolean r6 = r4.startsWith(r6)     // Catch:{ all -> 0x011b }
            if (r6 != 0) goto L_0x00c2
            java.lang.String r6 = "./3GPP_IMS/"
            boolean r6 = r4.startsWith(r6)     // Catch:{ all -> 0x011b }
            if (r6 == 0) goto L_0x00b1
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x011b }
            r5.<init>()     // Catch:{ all -> 0x011b }
            java.lang.String r6 = "omadm/"
            r5.append(r6)     // Catch:{ all -> 0x011b }
            r5.append(r4)     // Catch:{ all -> 0x011b }
            java.lang.String r4 = r5.toString()     // Catch:{ all -> 0x011b }
            goto L_0x00c2
        L_0x00b1:
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x011b }
            r4.<init>()     // Catch:{ all -> 0x011b }
            java.lang.String r6 = "omadm/./3GPP_IMS/"
            r4.append(r6)     // Catch:{ all -> 0x011b }
            r4.append(r5)     // Catch:{ all -> 0x011b }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x011b }
        L_0x00c2:
            java.lang.Object r5 = r3.getValue()     // Catch:{ all -> 0x011b }
            java.lang.String r5 = (java.lang.String) r5     // Catch:{ all -> 0x011b }
            r2.put(r4, r5)     // Catch:{ all -> 0x011b }
            java.lang.Object r4 = r3.getKey()     // Catch:{ all -> 0x011b }
            java.lang.String r4 = (java.lang.String) r4     // Catch:{ all -> 0x011b }
            java.lang.String r5 = "IMS_TEST_MODE"
            boolean r4 = r4.contains(r5)     // Catch:{ all -> 0x011b }
            if (r4 == 0) goto L_0x001e
            java.lang.String r4 = "persist.sys.ims_test_mode"
            java.lang.Object r5 = r3.getValue()     // Catch:{ all -> 0x011b }
            java.lang.String r5 = (java.lang.String) r5     // Catch:{ all -> 0x011b }
            android.os.SemSystemProperties.set(r4, r5)     // Catch:{ all -> 0x011b }
            com.sec.internal.constants.Mno r4 = r10.mMno     // Catch:{ all -> 0x011b }
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.VZW     // Catch:{ all -> 0x011b }
            if (r4 == r5) goto L_0x00ee
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.GCF     // Catch:{ all -> 0x011b }
            if (r4 != r5) goto L_0x001e
        L_0x00ee:
            java.lang.Object r3 = r3.getValue()     // Catch:{ all -> 0x011b }
            java.lang.String r3 = (java.lang.String) r3     // Catch:{ all -> 0x011b }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ all -> 0x011b }
            int r3 = r3.intValue()     // Catch:{ all -> 0x011b }
            r10.sendRawRequest(r3)     // Catch:{ all -> 0x011b }
            goto L_0x001e
        L_0x0101:
            int r12 = r1.size()     // Catch:{ all -> 0x011b }
            if (r12 <= 0) goto L_0x010e
            com.sec.internal.ims.settings.NvStorage r12 = r10.mNvStorage     // Catch:{ all -> 0x011b }
            java.lang.String r3 = "omadm"
            r12.insert(r3, r1)     // Catch:{ all -> 0x011b }
        L_0x010e:
            int r12 = r2.size()     // Catch:{ all -> 0x011b }
            if (r12 <= 0) goto L_0x0119
            com.sec.internal.interfaces.ims.config.IStorageAdapter r10 = r10.mDmStorage     // Catch:{ all -> 0x011b }
            r10.writeAll(r2)     // Catch:{ all -> 0x011b }
        L_0x0119:
            monitor-exit(r0)     // Catch:{ all -> 0x011b }
            return r11
        L_0x011b:
            r10 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x011b }
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.DeviceConfigManager.insertDm(android.net.Uri, android.content.ContentValues):android.net.Uri");
    }

    public int updateDm(Uri uri, ContentValues contentValues) {
        insertDm(uri, contentValues);
        return contentValues.size();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0036, code lost:
        return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.database.Cursor queryDm(android.net.Uri r3, java.lang.String[] r4, java.lang.String r5, java.lang.String[] r6, boolean r7) {
        /*
            r2 = this;
            java.lang.Object r0 = LOCK
            monitor-enter(r0)
            boolean r1 = r2.initStorage()     // Catch:{ all -> 0x0037 }
            if (r1 != 0) goto L_0x0023
            android.database.MatrixCursor r2 = new android.database.MatrixCursor     // Catch:{ all -> 0x0037 }
            r3 = 1
            java.lang.String[] r4 = new java.lang.String[r3]     // Catch:{ all -> 0x0037 }
            java.lang.String r5 = "NODATA"
            r6 = 0
            r4[r6] = r5     // Catch:{ all -> 0x0037 }
            r2.<init>(r4)     // Catch:{ all -> 0x0037 }
            java.lang.String[] r3 = new java.lang.String[r3]     // Catch:{ all -> 0x0021 }
            java.lang.String r4 = "NODATA"
            r3[r6] = r4     // Catch:{ all -> 0x0021 }
            r2.addRow(r3)     // Catch:{ all -> 0x0021 }
            monitor-exit(r0)     // Catch:{ all -> 0x0037 }
            return r2
        L_0x0021:
            monitor-exit(r0)     // Catch:{ all -> 0x0037 }
            return r2
        L_0x0023:
            if (r7 == 0) goto L_0x002a
            android.database.Cursor r2 = r2.readAllOfDm(r3)     // Catch:{ all -> 0x0037 }
            goto L_0x0035
        L_0x002a:
            if (r4 == 0) goto L_0x0031
            android.database.Cursor r2 = r2.readMultipleDm(r4, r5, r6)     // Catch:{ all -> 0x0037 }
            goto L_0x0035
        L_0x0031:
            android.database.Cursor r2 = r2.readSingleDm(r3, r5, r6)     // Catch:{ all -> 0x0037 }
        L_0x0035:
            monitor-exit(r0)     // Catch:{ all -> 0x0037 }
            return r2
        L_0x0037:
            r2 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0037 }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.DeviceConfigManager.queryDm(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], boolean):android.database.Cursor");
    }

    private void sendRawRequest(int i) {
        ITelephony asInterface = ITelephony.Stub.asInterface(ServiceManager.getService(PhoneConstants.PHONE_KEY));
        if (asInterface != null) {
            byte[] bArr = new byte[5];
            byte[] bArr2 = new byte[4];
            int i2 = 0;
            bArr[0] = 9;
            bArr[1] = 15;
            bArr[2] = 0;
            bArr[3] = 5;
            if (i == 1) {
                i2 = 1;
            }
            bArr[4] = (byte) i2;
            try {
                asInterface.invokeOemRilRequestRaw(bArr, bArr2);
                Log.d(LOG_TAG, "set testmode as " + i);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initVoLTEFeature() {
        String str = SemSystemProperties.get(Mno.MOCK_MNO_PROPERTY, "");
        if (TextUtils.isEmpty(str)) {
            str = ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY)).getSimOperator();
        }
        ContentValues cscImsSetting = CscParser.getCscImsSetting(str, this.mPhoneId);
        if (cscImsSetting != null && cscImsSetting.size() > 0) {
            boolean booleanValue = CollectionUtils.getBooleanValue(cscImsSetting, ImsConstants.CscParserConstants.ENABLE_VOLTE, false);
            boolean booleanValue2 = CollectionUtils.getBooleanValue(cscImsSetting, ImsConstants.CscParserConstants.SUPPORT_VOWIFI, false);
            if (booleanValue || booleanValue2) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("VOLTE_ENABLED", "1");
                insertDm(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/VOLTE_ENABLED"), contentValues);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initSmsOverImsFeature() {
        boolean z = GlobalSettingsManager.getInstance(this.mContext, this.mPhoneId).getBoolean(GlobalSettingsConstants.Registration.SMS_OVER_IP_INDICATION, false);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("[" + this.mPhoneId + "] initSmsOverImsFeature: isSmsOverIpNetworkIndication: " + z);
        NvConfiguration.setSmsIpNetworkIndi(this.mContext, z, this.mPhoneId);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0066  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0069  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void initIPsecFeature() {
        /*
            r4 = this;
            android.content.Context r0 = r4.mContext
            int r1 = r4.mPhoneId
            java.util.List r0 = com.sec.internal.ims.settings.ImsProfileLoaderInternal.getProfileList(r0, r1)
            if (r0 == 0) goto L_0x007a
            int r1 = r0.size()
            if (r1 != 0) goto L_0x0011
            goto L_0x007a
        L_0x0011:
            int r1 = r0.size()
            if (r1 <= 0) goto L_0x003e
            java.util.Iterator r0 = r0.iterator()
        L_0x001b:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x003e
            java.lang.Object r1 = r0.next()
            com.sec.ims.settings.ImsProfile r1 = (com.sec.ims.settings.ImsProfile) r1
            if (r1 == 0) goto L_0x001b
            java.lang.String r2 = "mmtel"
            boolean r2 = r1.hasService(r2)
            if (r2 != 0) goto L_0x0039
            java.lang.String r2 = "mmtel-video"
            boolean r2 = r1.hasService(r2)
            if (r2 == 0) goto L_0x001b
        L_0x0039:
            boolean r0 = r1.isIpSecEnabled()
            goto L_0x003f
        L_0x003e:
            r0 = 0
        L_0x003f:
            com.sec.internal.helper.SimpleEventLog r1 = r4.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "["
            r2.append(r3)
            int r3 = r4.mPhoneId
            r2.append(r3)
            java.lang.String r3 = "] initIPsecFeature: isIPsecEnabled: "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            r1.logAndAdd(r2)
            android.content.ContentValues r1 = new android.content.ContentValues
            r1.<init>()
            if (r0 == 0) goto L_0x0069
            java.lang.String r0 = "1"
            goto L_0x006b
        L_0x0069:
            java.lang.String r0 = "0"
        L_0x006b:
            java.lang.String r2 = "IPSEC_ENABLED"
            r1.put(r2, r0)
            java.lang.String r0 = "content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/IPSEC_ENABLED"
            android.net.Uri r0 = android.net.Uri.parse(r0)
            r4.insertDm(r0, r1)
            return
        L_0x007a:
            int r4 = r4.mPhoneId
            java.lang.String r0 = "initIPsecFeature: profileList null "
            java.lang.String r1 = "DeviceConfigManager"
            com.sec.internal.log.IMSLog.e(r1, r4, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.DeviceConfigManager.initIPsecFeature():void");
    }

    /* access modifiers changed from: protected */
    public void initH265Hd720Payload() {
        int i;
        ImsProfile next;
        List<ImsProfile> profileList = ImsProfileLoaderInternal.getProfileList(this.mContext, this.mPhoneId);
        if (profileList == null || profileList.size() == 0) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "initH265Hd720Payload: profileList null ");
            return;
        }
        if (profileList.size() > 0) {
            Iterator<ImsProfile> it = profileList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                next = it.next();
                if (next == null || (!next.hasService("mmtel") && !next.hasService("mmtel-video"))) {
                }
            }
            i = next.getH265Hd720pPayload();
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("[" + this.mPhoneId + "] initH265Hd720Payload: h265_hd720_payload: " + i);
            ContentValues contentValues = new ContentValues();
            contentValues.put("H265_720P", Integer.toString(i));
            insertDm(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/H265_720P"), contentValues);
        }
        i = 112;
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("[" + this.mPhoneId + "] initH265Hd720Payload: h265_hd720_payload: " + i);
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put("H265_720P", Integer.toString(i));
        insertDm(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/H265_720P"), contentValues2);
    }

    /* access modifiers changed from: protected */
    public void initEabFeature() {
        ContentValues cscImsSetting;
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (simManagerFromSimSlot != null && (cscImsSetting = CscParser.getCscImsSetting(simManagerFromSimSlot.getNetworkNames(), this.mPhoneId)) != null && cscImsSetting.size() > 0) {
            boolean booleanValue = CollectionUtils.getBooleanValue(cscImsSetting, ImsConstants.CscParserConstants.ENABLE_RCS, false);
            boolean booleanValue2 = CollectionUtils.getBooleanValue(cscImsSetting, ImsConstants.CscParserConstants.ENABLE_RCS_CHAT_SERVICE, false);
            if (booleanValue || booleanValue2) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("EAB_SETTING", "1");
                insertDm(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/EAB_SETTING"), contentValues);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void disableEabFeature() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("EAB_SETTING", "0");
        insertDm(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/EAB_SETTING"), contentValues);
    }

    /* access modifiers changed from: protected */
    public void changePollListSubExp(int i) {
        ContentValues cscImsSetting;
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (simManagerFromSimSlot != null && (cscImsSetting = CscParser.getCscImsSetting(simManagerFromSimSlot.getNetworkNames(), this.mPhoneId)) != null && cscImsSetting.size() > 0) {
            boolean booleanValue = CollectionUtils.getBooleanValue(cscImsSetting, ImsConstants.CscParserConstants.ENABLE_RCS, false);
            boolean booleanValue2 = CollectionUtils.getBooleanValue(cscImsSetting, ImsConstants.CscParserConstants.ENABLE_RCS_CHAT_SERVICE, false);
            if (booleanValue || booleanValue2) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("POLL_LIST_SUB_EXP", String.valueOf(i));
                insertDm(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/omadm/./3GPP_IMS/POLL_LIST_SUB_EXP"), contentValues);
            }
        }
    }

    private String getDbTableName(String str) {
        if (!TextUtils.isEmpty(str)) {
            Mno mno = this.mMno;
            if (mno == Mno.SPRINT) {
                str = mno.getAllSalesCodes()[0];
            }
        } else {
            str = DEFAULT_DATABASE_NAME;
        }
        return OMADM_DB_NAME_PREFIX + str + "_" + this.mPhoneId;
    }

    /* access modifiers changed from: protected */
    public void initDmConfig(SparseArray<String> sparseArray, ArrayList<String> arrayList) {
        HashMap hashMap = new HashMap();
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < sparseArray.size(); i++) {
            String replace = ((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(sparseArray.keyAt(i))).getName().replace("./3GPP_IMS/", "");
            String valueAt = sparseArray.valueAt(i);
            if (arrayList.contains(replace)) {
                this.mEventLog.logAndAdd(this.mPhoneId, "initDmConfig : Put into NV : " + replace + ", " + valueAt);
                contentValues.put(replace, valueAt);
            } else {
                if (!replace.contains("omadm/./3GPP_IMS/")) {
                    replace = "omadm/./3GPP_IMS/" + replace;
                }
                this.mEventLog.logAndAdd(this.mPhoneId, "initDmConfig : Put into DB : " + replace + ", " + valueAt);
                hashMap.put(replace, valueAt);
            }
        }
        if (hashMap.size() > 0) {
            this.mDmStorage.writeAll(hashMap);
        }
        if (contentValues.size() > 0) {
            this.mNvStorage.insert(NvStorage.ID_OMADM, contentValues);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:91:?, code lost:
        r14 = r13.mMno;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x0277, code lost:
        if (r14 == com.sec.internal.constants.Mno.VZW) goto L_0x027d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x027b, code lost:
        if (r14 != com.sec.internal.constants.Mno.GCF) goto L_0x02ac;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x027d, code lost:
        r14 = android.os.SemSystemProperties.getInt(com.sec.internal.constants.ims.ImsConstants.SystemProperties.IMS_TEST_MODE_PROP, 0);
        sendRawRequest(r14);
        r13.mEventLog.logAndAdd("simSlot[" + r13.mPhoneId + "] updateMno: send IMS_TESTMODE(" + r14 + ")");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x02ad, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void updateMno(android.content.ContentValues r14) {
        /*
            r13 = this;
            monitor-enter(r13)
            com.sec.internal.helper.SimpleEventLog r0 = r13.mEventLog     // Catch:{ all -> 0x02b1 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x02b1 }
            r1.<init>()     // Catch:{ all -> 0x02b1 }
            java.lang.String r2 = "simSlot["
            r1.append(r2)     // Catch:{ all -> 0x02b1 }
            int r2 = r13.mPhoneId     // Catch:{ all -> 0x02b1 }
            r1.append(r2)     // Catch:{ all -> 0x02b1 }
            java.lang.String r2 = "] updateMno"
            r1.append(r2)     // Catch:{ all -> 0x02b1 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x02b1 }
            r0.logAndAdd(r1)     // Catch:{ all -> 0x02b1 }
            java.lang.Object r0 = LOCK     // Catch:{ all -> 0x02b1 }
            monitor-enter(r0)     // Catch:{ all -> 0x02b1 }
            java.lang.String r1 = "mnoname"
            java.lang.String r1 = r14.getAsString(r1)     // Catch:{ all -> 0x02ae }
            java.lang.String r2 = "hassim"
            java.lang.Boolean r2 = r14.getAsBoolean(r2)     // Catch:{ all -> 0x02ae }
            java.util.Optional r2 = java.util.Optional.ofNullable(r2)     // Catch:{ all -> 0x02ae }
            java.lang.Boolean r3 = java.lang.Boolean.FALSE     // Catch:{ all -> 0x02ae }
            java.lang.Object r2 = r2.orElse(r3)     // Catch:{ all -> 0x02ae }
            java.lang.Boolean r2 = (java.lang.Boolean) r2     // Catch:{ all -> 0x02ae }
            boolean r2 = r2.booleanValue()     // Catch:{ all -> 0x02ae }
            com.sec.internal.constants.Mno r4 = r13.mMno     // Catch:{ all -> 0x02ae }
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.fromName(r1)     // Catch:{ all -> 0x02ae }
            r13.mMno = r5     // Catch:{ all -> 0x02ae }
            java.lang.String r5 = "mvnoname"
            java.lang.String r5 = r14.getAsString(r5)     // Catch:{ all -> 0x02ae }
            r13.mMvnoName = r5     // Catch:{ all -> 0x02ae }
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r5 = r13.mLastSimState     // Catch:{ all -> 0x02ae }
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r6 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.UNKNOWN     // Catch:{ all -> 0x02ae }
            if (r5 == r6) goto L_0x0074
            if (r2 != 0) goto L_0x0074
            com.sec.internal.constants.Mno r5 = r13.mMno     // Catch:{ all -> 0x02ae }
            boolean r5 = r5.isHkMo()     // Catch:{ all -> 0x02ae }
            if (r5 != 0) goto L_0x0074
            com.sec.internal.constants.Mno r5 = r13.mMno     // Catch:{ all -> 0x02ae }
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.CTCMO     // Catch:{ all -> 0x02ae }
            if (r5 == r6) goto L_0x0074
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r14 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.ABSENT     // Catch:{ all -> 0x02ae }
            r13.mLastSimState = r14     // Catch:{ all -> 0x02ae }
            java.lang.String r14 = "DeviceConfigManager"
            int r1 = r13.mPhoneId     // Catch:{ all -> 0x02ae }
            java.lang.String r2 = "Skip updating config modules when SIM ejected"
            com.sec.internal.log.IMSLog.i(r14, r1, r2)     // Catch:{ all -> 0x02ae }
            monitor-exit(r0)     // Catch:{ all -> 0x02ae }
            monitor-exit(r13)
            return
        L_0x0074:
            if (r2 == 0) goto L_0x0079
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r5 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.LOADED     // Catch:{ all -> 0x02ae }
            goto L_0x007b
        L_0x0079:
            com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r5 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.ABSENT     // Catch:{ all -> 0x02ae }
        L_0x007b:
            r13.mLastSimState = r5     // Catch:{ all -> 0x02ae }
            com.sec.internal.ims.settings.ImsProfileCache r5 = r13.mProfileCache     // Catch:{ all -> 0x02ae }
            r5.updateMno(r14)     // Catch:{ all -> 0x02ae }
            com.sec.internal.ims.settings.UserConfigStorage r5 = r13.mUserConfigStorage     // Catch:{ all -> 0x02ae }
            r5.reset(r1)     // Catch:{ all -> 0x02ae }
            android.content.Context r1 = r13.mContext     // Catch:{ all -> 0x02ae }
            int r5 = r13.mPhoneId     // Catch:{ all -> 0x02ae }
            com.sec.internal.ims.settings.GlobalSettingsManager r1 = com.sec.internal.ims.settings.GlobalSettingsManager.getInstance(r1, r5)     // Catch:{ all -> 0x02ae }
            com.sec.internal.ims.settings.GlobalSettingsRepo r1 = r1.getGlobalSettings()     // Catch:{ all -> 0x02ae }
            boolean r5 = r1.updateMno(r14)     // Catch:{ all -> 0x02ae }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x02ae }
            r6.<init>()     // Catch:{ all -> 0x02ae }
            int r7 = r13.mPhoneId     // Catch:{ all -> 0x02ae }
            r6.append(r7)     // Catch:{ all -> 0x02ae }
            java.lang.String r7 = ",UPD MNO:"
            r6.append(r7)     // Catch:{ all -> 0x02ae }
            r6.append(r5)     // Catch:{ all -> 0x02ae }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x02ae }
            r7 = 268500992(0x10010000, float:2.5440764E-29)
            com.sec.internal.log.IMSLog.c(r7, r6)     // Catch:{ all -> 0x02ae }
            r6 = 0
            r7 = 0
            r8 = 1
            if (r5 == 0) goto L_0x016e
            com.sec.internal.interfaces.ims.config.IStorageAdapter r9 = r13.mDmStorage     // Catch:{ all -> 0x02ae }
            if (r9 == 0) goto L_0x00bf
            r9.close()     // Catch:{ all -> 0x02ae }
            goto L_0x00c6
        L_0x00bf:
            com.sec.internal.ims.config.adapters.StorageAdapter r9 = new com.sec.internal.ims.config.adapters.StorageAdapter     // Catch:{ all -> 0x02ae }
            r9.<init>()     // Catch:{ all -> 0x02ae }
            r13.mDmStorage = r9     // Catch:{ all -> 0x02ae }
        L_0x00c6:
            com.sec.internal.ims.settings.NvStorage r9 = r13.mNvStorage     // Catch:{ all -> 0x02ae }
            if (r9 == 0) goto L_0x00cf
            r9.close()     // Catch:{ all -> 0x02ae }
            r13.mNvStorage = r6     // Catch:{ all -> 0x02ae }
        L_0x00cf:
            r13.initStorage()     // Catch:{ all -> 0x02ae }
            com.sec.internal.ims.settings.ImsServiceSwitch r9 = r13.mImsServiceSwitch     // Catch:{ all -> 0x02ae }
            r9.unregisterObserver()     // Catch:{ all -> 0x02ae }
            com.sec.internal.ims.settings.ImsServiceSwitch r9 = r13.mImsServiceSwitch     // Catch:{ all -> 0x02ae }
            r9.updateServiceSwitch(r14)     // Catch:{ all -> 0x02ae }
            com.sec.internal.ims.settings.ImsServiceSwitch r9 = r13.mImsServiceSwitch     // Catch:{ all -> 0x02ae }
            java.lang.String r10 = "enableServiceVolte"
            boolean r9 = r9.isImsSwitchEnabled(r10)     // Catch:{ all -> 0x02ae }
            java.lang.String r10 = "imsSwitchType"
            java.lang.Integer r10 = r14.getAsInteger(r10)     // Catch:{ all -> 0x02ae }
            java.util.Optional r10 = java.util.Optional.ofNullable(r10)     // Catch:{ all -> 0x02ae }
            java.lang.Integer r11 = java.lang.Integer.valueOf(r7)     // Catch:{ all -> 0x02ae }
            java.lang.Object r10 = r10.orElse(r11)     // Catch:{ all -> 0x02ae }
            java.lang.Integer r10 = (java.lang.Integer) r10     // Catch:{ all -> 0x02ae }
            int r10 = r10.intValue()     // Catch:{ all -> 0x02ae }
            r11 = 4
            if (r10 == r11) goto L_0x0105
            r11 = 3
            if (r10 == r11) goto L_0x0105
            r11 = 5
            if (r10 != r11) goto L_0x0157
        L_0x0105:
            r10 = -1
            if (r2 == 0) goto L_0x0116
            android.content.Context r11 = r13.mContext     // Catch:{ all -> 0x02ae }
            int r12 = r13.mPhoneId     // Catch:{ all -> 0x02ae }
            int r11 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.getVoiceCallType(r11, r10, r12)     // Catch:{ all -> 0x02ae }
            if (r11 == r10) goto L_0x0116
            if (r9 != 0) goto L_0x0116
            r11 = r8
            goto L_0x0117
        L_0x0116:
            r11 = r7
        L_0x0117:
            if (r2 == 0) goto L_0x012f
            android.content.Context r2 = r13.mContext     // Catch:{ all -> 0x02ae }
            int r12 = r13.mPhoneId     // Catch:{ all -> 0x02ae }
            int r2 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.getVideoCallType(r2, r10, r12)     // Catch:{ all -> 0x02ae }
            if (r2 == r10) goto L_0x012f
            com.sec.internal.ims.settings.ImsServiceSwitch r2 = r13.mImsServiceSwitch     // Catch:{ all -> 0x02ae }
            java.lang.String r10 = "enableServiceVilte"
            boolean r2 = r2.isImsSwitchEnabled(r10)     // Catch:{ all -> 0x02ae }
            if (r2 != 0) goto L_0x012f
            r2 = r8
            goto L_0x0130
        L_0x012f:
            r2 = r7
        L_0x0130:
            r1.resetUserSettingAsDefault(r11, r2, r7)     // Catch:{ all -> 0x02ae }
            if (r9 != 0) goto L_0x0157
            com.sec.internal.ims.settings.ImsServiceSwitch r2 = r13.mImsServiceSwitch     // Catch:{ all -> 0x02ae }
            java.lang.String r9 = "enableServiceSmsip"
            boolean r2 = r2.isImsSwitchEnabled(r9)     // Catch:{ all -> 0x02ae }
            if (r2 != 0) goto L_0x0157
            com.sec.internal.ims.settings.ImsServiceSwitch r2 = r13.mImsServiceSwitch     // Catch:{ all -> 0x02ae }
            java.lang.String r9 = "enableServiceVowifi"
            boolean r2 = r2.isImsSwitchEnabled(r9)     // Catch:{ all -> 0x02ae }
            if (r2 != 0) goto L_0x0157
            android.content.ContentValues r2 = new android.content.ContentValues     // Catch:{ all -> 0x02ae }
            r2.<init>()     // Catch:{ all -> 0x02ae }
            java.lang.String r9 = "show_regi_info_in_sec_settings"
            r2.put(r9, r3)     // Catch:{ all -> 0x02ae }
            r1.update(r2)     // Catch:{ all -> 0x02ae }
        L_0x0157:
            com.sec.internal.constants.Mno r1 = r13.mMno     // Catch:{ all -> 0x02ae }
            boolean r1 = r1.isKor()     // Catch:{ all -> 0x02ae }
            if (r1 == 0) goto L_0x016e
            r13.initSmsOverImsFeature()     // Catch:{ all -> 0x02ae }
            r13.initIPsecFeature()     // Catch:{ all -> 0x02ae }
            com.sec.internal.constants.Mno r1 = r13.mMno     // Catch:{ all -> 0x02ae }
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.KT     // Catch:{ all -> 0x02ae }
            if (r1 != r2) goto L_0x016e
            r13.initH265Hd720Payload()     // Catch:{ all -> 0x02ae }
        L_0x016e:
            android.content.Context r1 = r13.mContext     // Catch:{ all -> 0x02ae }
            int r2 = r13.mPhoneId     // Catch:{ all -> 0x02ae }
            com.sec.internal.ims.settings.GlobalSettingsManager r1 = com.sec.internal.ims.settings.GlobalSettingsManager.getInstance(r1, r2)     // Catch:{ all -> 0x02ae }
            java.lang.String r2 = "ss_callwaiting_by_network"
            boolean r1 = r1.getBoolean(r2, r8)     // Catch:{ all -> 0x02ae }
            com.sec.internal.constants.Mno r2 = r13.mMno     // Catch:{ all -> 0x02ae }
            boolean r2 = r2.isChn()     // Catch:{ all -> 0x02ae }
            if (r2 == 0) goto L_0x01cb
            com.sec.internal.constants.Mno r2 = r13.mMno     // Catch:{ all -> 0x02ae }
            boolean r2 = r4.equals(r2)     // Catch:{ all -> 0x02ae }
            if (r2 != 0) goto L_0x01cb
            if (r1 != 0) goto L_0x01cb
            android.content.Context r1 = r13.mContext     // Catch:{ all -> 0x02ae }
            android.content.ContentResolver r1 = r1.getContentResolver()     // Catch:{ all -> 0x02ae }
            int r2 = r13.mPhoneId     // Catch:{ all -> 0x02ae }
            if (r2 == r8) goto L_0x019d
            java.lang.String r2 = "volte_call_waiting"
            goto L_0x01a0
        L_0x019d:
            java.lang.String r2 = "volte_call_waiting_slot2"
        L_0x01a0:
            int r1 = android.provider.Settings.System.getInt(r1, r2, r8)     // Catch:{ all -> 0x02ae }
            if (r1 != r8) goto L_0x01a7
            goto L_0x01a8
        L_0x01a7:
            r8 = r7
        L_0x01a8:
            if (r8 != 0) goto L_0x01cb
            android.content.Context r1 = r13.mContext     // Catch:{ all -> 0x02ae }
            int r2 = r13.mPhoneId     // Catch:{ all -> 0x02ae }
            java.lang.String r3 = "enable_call_wait"
            com.sec.ims.settings.UserConfiguration.setUserConfig(r1, r2, r3, r8)     // Catch:{ all -> 0x02ae }
            java.lang.String r1 = "DeviceConfigManager"
            int r2 = r13.mPhoneId     // Catch:{ all -> 0x02ae }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x02ae }
            r3.<init>()     // Catch:{ all -> 0x02ae }
            java.lang.String r4 = "TerminalBasedCallWaiting should follow DB, update to : "
            r3.append(r4)     // Catch:{ all -> 0x02ae }
            r3.append(r8)     // Catch:{ all -> 0x02ae }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x02ae }
            com.sec.internal.log.IMSLog.d(r1, r2, r3)     // Catch:{ all -> 0x02ae }
        L_0x01cb:
            int r1 = r13.mPhoneId     // Catch:{ all -> 0x02ae }
            boolean r1 = com.sec.internal.ims.rcs.RcsPolicyManager.loadRcsSettings(r1, r5)     // Catch:{ all -> 0x02ae }
            if (r5 != 0) goto L_0x01d5
            if (r1 == 0) goto L_0x0218
        L_0x01d5:
            com.sec.internal.helper.SimpleEventLog r2 = r13.mEventLog     // Catch:{ all -> 0x02ae }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x02ae }
            r3.<init>()     // Catch:{ all -> 0x02ae }
            java.lang.String r4 = "simSlot["
            r3.append(r4)     // Catch:{ all -> 0x02ae }
            int r4 = r13.mPhoneId     // Catch:{ all -> 0x02ae }
            r3.append(r4)     // Catch:{ all -> 0x02ae }
            java.lang.String r4 = "] updateMno: notifyUpdated: GlobalSettings("
            r3.append(r4)     // Catch:{ all -> 0x02ae }
            r3.append(r5)     // Catch:{ all -> 0x02ae }
            java.lang.String r4 = "), RcsPolicy("
            r3.append(r4)     // Catch:{ all -> 0x02ae }
            r3.append(r1)     // Catch:{ all -> 0x02ae }
            java.lang.String r1 = ")"
            r3.append(r1)     // Catch:{ all -> 0x02ae }
            java.lang.String r1 = r3.toString()     // Catch:{ all -> 0x02ae }
            r2.logAndAdd(r1)     // Catch:{ all -> 0x02ae }
            android.content.Context r1 = r13.mContext     // Catch:{ all -> 0x02ae }
            android.content.ContentResolver r1 = r1.getContentResolver()     // Catch:{ all -> 0x02ae }
            android.net.Uri r2 = com.sec.internal.constants.ims.settings.GlobalSettingsConstants.CONTENT_URI     // Catch:{ all -> 0x02ae }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x02ae }
            int r3 = r13.mPhoneId     // Catch:{ all -> 0x02ae }
            android.net.Uri r2 = com.sec.internal.helper.UriUtil.buildUri((java.lang.String) r2, (int) r3)     // Catch:{ all -> 0x02ae }
            r1.notifyChange(r2, r6)     // Catch:{ all -> 0x02ae }
        L_0x0218:
            com.sec.internal.ims.settings.SmsSetting r1 = r13.mSmsSetting     // Catch:{ all -> 0x02ae }
            boolean r14 = r1.updateMno(r14, r5)     // Catch:{ all -> 0x02ae }
            if (r14 == 0) goto L_0x0235
            android.content.Context r14 = r13.mContext     // Catch:{ all -> 0x02ae }
            android.content.ContentResolver r14 = r14.getContentResolver()     // Catch:{ all -> 0x02ae }
            android.net.Uri r1 = com.sec.internal.constants.ims.ImsConstants.Uris.SMS_SETTING     // Catch:{ all -> 0x02ae }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x02ae }
            int r2 = r13.mPhoneId     // Catch:{ all -> 0x02ae }
            android.net.Uri r1 = com.sec.internal.helper.UriUtil.buildUri((java.lang.String) r1, (int) r2)     // Catch:{ all -> 0x02ae }
            r14.notifyChange(r1, r6)     // Catch:{ all -> 0x02ae }
        L_0x0235:
            com.sec.internal.constants.Mno r14 = r13.mMno     // Catch:{ all -> 0x02ae }
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.GCF     // Catch:{ all -> 0x02ae }
            if (r14 != r1) goto L_0x0272
            java.lang.String r14 = r13.getGcfInitRat()     // Catch:{ all -> 0x02ae }
            boolean r14 = android.text.TextUtils.isEmpty(r14)     // Catch:{ all -> 0x02ae }
            if (r14 == 0) goto L_0x0272
            java.lang.StringBuilder r14 = new java.lang.StringBuilder     // Catch:{ all -> 0x02ae }
            r14.<init>()     // Catch:{ all -> 0x02ae }
            java.lang.String r1 = "DeviceConfigManager["
            r14.append(r1)     // Catch:{ all -> 0x02ae }
            int r1 = r13.mPhoneId     // Catch:{ all -> 0x02ae }
            r14.append(r1)     // Catch:{ all -> 0x02ae }
            java.lang.String r1 = "]"
            r14.append(r1)     // Catch:{ all -> 0x02ae }
            java.lang.String r14 = r14.toString()     // Catch:{ all -> 0x02ae }
            java.lang.String r1 = "init rat : nr,lte,wifi"
            android.util.Log.d(r14, r1)     // Catch:{ all -> 0x02ae }
            android.content.ContentValues r14 = new android.content.ContentValues     // Catch:{ all -> 0x02ae }
            r14.<init>()     // Catch:{ all -> 0x02ae }
            java.lang.String r1 = "rat"
            java.lang.String r2 = "nr,lte,wifi"
            r14.put(r1, r2)     // Catch:{ all -> 0x02ae }
            r13.updateGcfInitRat(r14)     // Catch:{ all -> 0x02ae }
        L_0x0272:
            monitor-exit(r0)     // Catch:{ all -> 0x02ae }
            com.sec.internal.constants.Mno r14 = r13.mMno     // Catch:{ all -> 0x02b1 }
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.VZW     // Catch:{ all -> 0x02b1 }
            if (r14 == r0) goto L_0x027d
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.GCF     // Catch:{ all -> 0x02b1 }
            if (r14 != r0) goto L_0x02ac
        L_0x027d:
            java.lang.String r14 = "persist.sys.ims_test_mode"
            int r14 = android.os.SemSystemProperties.getInt(r14, r7)     // Catch:{ all -> 0x02b1 }
            r13.sendRawRequest(r14)     // Catch:{ all -> 0x02b1 }
            com.sec.internal.helper.SimpleEventLog r0 = r13.mEventLog     // Catch:{ all -> 0x02b1 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x02b1 }
            r1.<init>()     // Catch:{ all -> 0x02b1 }
            java.lang.String r2 = "simSlot["
            r1.append(r2)     // Catch:{ all -> 0x02b1 }
            int r2 = r13.mPhoneId     // Catch:{ all -> 0x02b1 }
            r1.append(r2)     // Catch:{ all -> 0x02b1 }
            java.lang.String r2 = "] updateMno: send IMS_TESTMODE("
            r1.append(r2)     // Catch:{ all -> 0x02b1 }
            r1.append(r14)     // Catch:{ all -> 0x02b1 }
            java.lang.String r14 = ")"
            r1.append(r14)     // Catch:{ all -> 0x02b1 }
            java.lang.String r14 = r1.toString()     // Catch:{ all -> 0x02b1 }
            r0.logAndAdd(r14)     // Catch:{ all -> 0x02b1 }
        L_0x02ac:
            monitor-exit(r13)
            return
        L_0x02ae:
            r14 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x02ae }
            throw r14     // Catch:{ all -> 0x02b1 }
        L_0x02b1:
            r14 = move-exception
            monitor-exit(r13)
            throw r14
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.DeviceConfigManager.updateMno(android.content.ContentValues):void");
    }

    public synchronized String getMnoName() {
        String str;
        synchronized (LOCK) {
            str = "";
            if (this.mLastSimState != SimConstants.SIM_STATE.UNKNOWN) {
                str = this.mMno.getName();
            }
        }
        return str;
    }

    public synchronized String getMvnoName() {
        String str;
        synchronized (LOCK) {
            str = "";
            if (this.mLastSimState != SimConstants.SIM_STATE.UNKNOWN) {
                str = this.mMvnoName;
            }
        }
        return str;
    }

    public synchronized boolean getHasSim() {
        boolean z;
        synchronized (LOCK) {
            z = this.mLastSimState == SimConstants.SIM_STATE.LOADED;
        }
        return z;
    }

    public boolean restoreDefaultImsProfile() {
        this.mProfileCache.resetToDefault();
        return true;
    }

    public void updateGcfConfig(ContentValues contentValues) {
        if (contentValues != null && contentValues.size() != 0) {
            Boolean asBoolean = contentValues.getAsBoolean("GCF_CONFIG_ENABLE");
            if (asBoolean == null) {
                IMSLog.d(LOG_TAG, this.mPhoneId, "GCF_CONFIG_ENABLE is null");
            } else {
                DeviceUtil.setGcfMode(asBoolean.booleanValue());
            }
        }
    }

    public void updateDnsBlock(ContentValues contentValues) {
        if (contentValues != null) {
            Boolean asBoolean = contentValues.getAsBoolean("DNS_BLOCK_ENABLE");
            if (asBoolean != null) {
                SemSystemProperties.set("net.tether.always", asBoolean.booleanValue() ? "1" : "");
            } else {
                IMSLog.d(LOG_TAG, this.mPhoneId, "DNS_BLOCK_ENABLE is null");
            }
        }
    }

    public Cursor queryGcfConfig() {
        Boolean valueOf = Boolean.valueOf(DeviceUtil.getGcfMode());
        MatrixCursor matrixCursor = new MatrixCursor(ImsSettings.ImsServiceSwitchTable.PROJECTION);
        matrixCursor.addRow(new Object[]{"GCF_CONFIG_ENABLE", String.valueOf(valueOf)});
        return matrixCursor;
    }

    public void setImsUserSetting(String str, int i) {
        if (!TextUtils.isEmpty(str)) {
            Mno simMno = SimUtil.getSimMno(this.mPhoneId);
            if (str.startsWith(ImsConstants.SystemSettings.VOLTE_SLOT1.getName())) {
                this.mImsServiceSwitch.setVoiceCallType(simMno.getName(), i);
            } else if (str.startsWith(ImsConstants.SystemSettings.VILTE_SLOT1.getName())) {
                this.mImsServiceSwitch.setVideoCallType(simMno.getName(), i);
            } else if (str.startsWith(ImsConstants.SystemSettings.RCS_USER_SETTING1.getName())) {
                this.mImsServiceSwitch.setRcsUserSetting(i);
            }
        }
    }

    public void enableImsSwitch(String str, boolean z) {
        if (!TextUtils.isEmpty(str)) {
            if ("volte".equalsIgnoreCase(str)) {
                this.mImsServiceSwitch.enableVoLte(z);
            } else if (RCS.equalsIgnoreCase(str)) {
                this.mImsServiceSwitch.enableRcs(z);
            } else {
                this.mImsServiceSwitch.enable(str, z);
            }
        }
    }

    public void resetImsSwitch() {
        this.mImsServiceSwitch.doInit();
    }

    public void updateImsSwitchByDynamicUpdate() {
        if (DeviceUtil.getGcfMode() || "GCF".equalsIgnoreCase(OmcCode.get())) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Skip updateSwitchByDynamicUpdate during GCF");
        } else {
            this.mImsServiceSwitch.updateSwitchByDynamicUpdate();
        }
    }

    public Cursor queryImsUserSetting(String[] strArr) {
        MatrixCursor matrixCursor = new MatrixCursor(ImsSettings.ImsUserSettingTable.PROJECTION);
        String name = SimUtil.getSimMno(this.mPhoneId).getName();
        if (strArr != null) {
            for (String str : strArr) {
                IMSLog.d(LOG_TAG, this.mPhoneId, "queryImsUserSetting: name " + str);
                if (ImsConstants.SystemSettings.VOLTE_SLOT1.getName().equalsIgnoreCase(str)) {
                    matrixCursor.addRow(new Object[]{str, Integer.valueOf(this.mImsServiceSwitch.getVoiceCallType(name))});
                } else if (ImsConstants.SystemSettings.VILTE_SLOT1.getName().equalsIgnoreCase(str)) {
                    matrixCursor.addRow(new Object[]{str, Integer.valueOf(this.mImsServiceSwitch.getVideoCallType(name))});
                } else if (ImsConstants.SystemSettings.RCS_USER_SETTING1.getName().equalsIgnoreCase(str) && SimUtil.getSimMno(this.mPhoneId) != Mno.DEFAULT) {
                    matrixCursor.addRow(new Object[]{str, Integer.valueOf(this.mImsServiceSwitch.getRcsUserSetting())});
                }
            }
        }
        return matrixCursor;
    }

    public Cursor queryImsSwitch(String[] strArr) {
        MatrixCursor matrixCursor = new MatrixCursor(ImsSettings.ImsServiceSwitchTable.PROJECTION);
        if (strArr != null) {
            for (String str : strArr) {
                IMSLog.d(LOG_TAG, this.mPhoneId, "queryImsSwitch: name " + str);
                if ("volte".equalsIgnoreCase(str)) {
                    matrixCursor.addRow(new Object[]{str, Integer.valueOf(this.mImsServiceSwitch.isVoLteEnabled() ? 1 : 0)});
                } else if (RCS_SWITCH.equalsIgnoreCase(str)) {
                    matrixCursor.addRow(new Object[]{str, Integer.valueOf(this.mImsServiceSwitch.isRcsSwitchEnabled() ? 1 : 0)});
                } else if (RCS.equalsIgnoreCase(str)) {
                    matrixCursor.addRow(new Object[]{str, Integer.valueOf(this.mImsServiceSwitch.isRcsEnabled() ? 1 : 0)});
                } else if (IMS.equalsIgnoreCase(str)) {
                    matrixCursor.addRow(new Object[]{str, Integer.valueOf(this.mImsServiceSwitch.isImsEnabled() ? 1 : 0)});
                } else if (DEFAULTMSGAPPINUSE.equalsIgnoreCase(str)) {
                    matrixCursor.addRow(new Object[]{str, Integer.valueOf(this.mImsServiceSwitch.isDefaultMessageAppInUse() ? 1 : 0)});
                } else {
                    matrixCursor.addRow(new Object[]{str, Integer.valueOf(this.mImsServiceSwitch.isEnabled(str) ? 1 : 0)});
                }
            }
        }
        return matrixCursor;
    }

    public void updateProvisioningProperty(ContentValues contentValues) {
        Boolean asBoolean = contentValues.getAsBoolean("status");
        Log.d("DeviceConfigManager[" + this.mPhoneId + "]", "updateProvisioningProperty : " + asBoolean);
        if (asBoolean == null) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "status is null.");
        } else if (asBoolean.booleanValue()) {
            ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 1);
        } else {
            ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 0);
            VowifiConfig.setEnabled(this.mContext, 0, this.mPhoneId);
        }
    }

    public void updateWificallingProperty(ContentValues contentValues) {
        Boolean asBoolean = contentValues.getAsBoolean("status");
        if (asBoolean == null) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "status is null.");
        } else if (asBoolean.booleanValue()) {
            ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 1);
            VowifiConfig.setEnabled(this.mContext, 1, this.mPhoneId);
        } else {
            VowifiConfig.setEnabled(this.mContext, 0, this.mPhoneId);
        }
    }

    public void updateGcfInitRat(ContentValues contentValues) {
        if (contentValues != null && contentValues.size() != 0) {
            String asString = contentValues.getAsString("rat");
            if (TextUtils.isEmpty(asString)) {
                Log.d(LOG_TAG, "updateGcfInitRat is empty");
                asString = "";
            }
            SharedPreferences.Editor edit = ImsSharedPrefHelper.getSharedPref(-1, this.mContext, "gcf_init_rat", 0, false).edit();
            edit.putString("rat", asString);
            edit.apply();
        }
    }

    public void updateDtLocUserConsent(ContentValues contentValues) {
        if (contentValues != null && contentValues.size() != 0) {
            Integer asInteger = contentValues.getAsInteger("dtlocation");
            int intValue = asInteger != null ? asInteger.intValue() : -1;
            SharedPreferences.Editor edit = ImsSharedPrefHelper.getSharedPref(-1, this.mContext, "dtlocuserconsent", 0, false).edit();
            edit.putInt("dtlocation", intValue);
            edit.apply();
        }
    }

    public String getGcfInitRat() {
        Cursor query;
        String str = "";
        try {
            query = this.mContext.getContentResolver().query(Uri.parse("content://com.sec.ims.settings/gcfinitrat"), (String[]) null, (String) null, (String[]) null, (String) null);
            if (query != null) {
                if (query.moveToFirst()) {
                    str = query.getString(query.getColumnIndex("rat"));
                }
            }
            if (query != null) {
                query.close();
            }
        } catch (Exception unused) {
            Log.e(LOG_TAG, "failed to get getGcfInitialRegistrationRat");
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return str;
        throw th;
    }

    private XmlPullParser getDmConfigXmlParser() {
        return this.mContext.getResources().getXml(this.mContext.getResources().getIdentifier("dmconfig", MIMEContentType.XML, this.mContext.getPackageName()));
    }

    public ArrayList<String> getNvList(String str) {
        XmlPullParser dmConfigXmlParser = getDmConfigXmlParser();
        if (dmConfigXmlParser == null) {
            Log.e(LOG_TAG, "can not find matched dmConfig.xml");
            return new ArrayList<>();
        }
        try {
            XmlUtils.beginDocument(dmConfigXmlParser, "configurations");
            for (int eventType = dmConfigXmlParser.getEventType(); eventType != 1; eventType = dmConfigXmlParser.next()) {
                if (eventType == 2 && "configuration".equalsIgnoreCase(dmConfigXmlParser.getName())) {
                    if (matchConfigName(str, dmConfigXmlParser.getAttributeValue(0))) {
                        return parseNvList(dmConfigXmlParser);
                    }
                    XmlUtils.skipCurrentTag(dmConfigXmlParser);
                }
            }
            return new ArrayList<>();
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private ArrayList<String> parseNvList(XmlPullParser xmlPullParser) {
        ArrayList<String> arrayList = new ArrayList<>();
        while (true) {
            try {
                int next = xmlPullParser.next();
                if (next == 1) {
                    break;
                } else if (next != 2 || !ImsConstants.Intents.EXTRA_UPDATED_ITEM.equalsIgnoreCase(xmlPullParser.getName())) {
                    if (next == 3 && "configuration".equalsIgnoreCase(xmlPullParser.getName())) {
                        break;
                    }
                } else {
                    String attributeValue = xmlPullParser.getAttributeValue(0);
                    String attributeValue2 = xmlPullParser.getAttributeValue((String) null, "type");
                    if (!TextUtils.isEmpty(attributeValue2) && TextUtils.equals(attributeValue2.toUpperCase(), "NV")) {
                        arrayList.add(((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(Integer.parseInt(attributeValue))).getName().replace("./3GPP_IMS/", ""));
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public SparseArray<String> getDefaultDmConfig(String str, ContentValues contentValues, Map<String, String> map) {
        XmlPullParser dmConfigXmlParser = getDmConfigXmlParser();
        if (dmConfigXmlParser == null) {
            Log.e(LOG_TAG, "can not find matched dmConfig.xml");
            return null;
        }
        if (TextUtils.isEmpty(str)) {
            str = DEFAULT_DMCONFIG_NAME;
        }
        SparseArray<String> sparseArray = new SparseArray<>();
        try {
            XmlUtils.beginDocument(dmConfigXmlParser, "configurations");
            for (int eventType = dmConfigXmlParser.getEventType(); eventType != 1; eventType = dmConfigXmlParser.next()) {
                if (eventType == 2 && "configuration".equalsIgnoreCase(dmConfigXmlParser.getName())) {
                    List asList = Arrays.asList(dmConfigXmlParser.getAttributeValue(0).split(","));
                    if (asList.contains(DEFAULT_DMCONFIG_NAME)) {
                        readInitialConfigFromXml(dmConfigXmlParser, asList, sparseArray, contentValues, map);
                    } else if (asList.contains(str)) {
                        readInitialConfigFromXml(dmConfigXmlParser, asList, sparseArray, contentValues, map);
                        return sparseArray;
                    } else {
                        XmlUtils.skipCurrentTag(dmConfigXmlParser);
                    }
                }
            }
            return sparseArray;
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean matchConfigName(String str, String str2) {
        Log.d(LOG_TAG, "Configname : " + str2 + " name : " + str);
        String[] split = str2.split(",");
        int length = split.length;
        for (int i = 0; i < length; i++) {
            if (split[i].equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    public void dump() {
        synchronized (LOCK) {
            IMSLog.dump(LOG_TAG, "Dump of DeviceConfigManager:");
            this.mEventLog.dump();
            this.mProfileCache.dump();
            this.mSmsSetting.dump();
            NvStorage nvStorage = this.mNvStorage;
            if (nvStorage != null) {
                nvStorage.dump();
            }
            this.mImsServiceSwitch.dump();
            getGlobalSettingsRepo().dump();
            this.mUserConfigStorage.dump();
        }
    }
}
