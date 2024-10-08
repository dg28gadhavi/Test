package com.sec.internal.ims.settings;

import android.content.Context;
import android.os.Environment;
import android.os.SemSystemProperties;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.samsung.android.feature.SemCarrierFeature;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.SecFeature;
import com.sec.internal.helper.HashManager;
import com.sec.internal.helper.JsonUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.SystemWrapper;
import com.sec.internal.ims.settings.SmsSetting;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

public class ImsAutoUpdate {
    public static final String GLOBALSETTINGS_UPDATE = "globalsettings_update";
    public static final String IMSPROFILE_UPDATE = "imsprofile_update";
    private static final String IMSSWITCH_UPDATE = "imsswitch_update";
    protected static final String IMSUPDATE_JSON_FILE = "imsupdate.json";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "ImsAutoUpdate";
    private static final String MNOMAP_UPDATE = "mnomap_update";
    protected static final String MNONAME = "mnoname";
    private static char MVNO_DELIMITER = ':';
    protected static final String NAME = "name";
    private static final String PROVIDERSETTINGS_UPDATE = "providersettings_update";
    private static final String RCSRPOLICY_UPDATE = "rcspolicy_update";
    public static final int RESOURCE_CARRIER_FEATURE = 4;
    public static final int RESOURCE_DOWNLOAD = 0;
    public static final int RESOURCE_IMSUPDATE = 1;
    private static final String SMS_SETTINGS_UPDATE = "sms_settings_update";
    public static final String TAG_DEFAULT_RCS_POLICY = "default_rcs_policy";
    public static final String TAG_DEFAULT_UP_POLICY = "default_up_policy";
    public static final String TAG_GC_BLOCK_MCC_LIST = "gc_block_mcc_list";
    public static final String TAG_GLOBALSETTING = "globalsetting";
    public static final String TAG_GLOBALSETTINGS_DEFAULT = "defaultsetting";
    public static final String TAG_GLOBALSETTINGS_NOHIT = "nohitsetting";
    public static final String TAG_IMSSWITCH = "imsswitch";
    public static final String TAG_MNOMAP_ADD = "add_mnomap";
    public static final String TAG_MNOMAP_REMOVE = "remove_mnomap";
    public static final String TAG_POLICY_NAME = "policy_name";
    public static final String TAG_PROFILE = "profile";
    public static final String TAG_RCS_POLICY = "rcs_policy";
    public static final String TAG_REPLACE_GC_BLOCK_MCC_LIST = "replace_gc_block_mcc_list";
    protected static final String UPDATE_FILE_PATH_CSC = "/system/csc";
    /* access modifiers changed from: private */
    public static StorageManager mStorage = null;
    private static JsonArray mUpdateArrays = null;
    private static final HashMap<Integer, ImsAutoUpdate> sInstances = new HashMap<>();
    private JsonElement mCarrierUpdate;
    private Context mContext;
    private String mCurrentCarrierFeatureHash;
    private String mCurrentHash;
    protected SimpleEventLog mEventLog;
    private boolean mHashChanged;
    private HashManager mHashManager;
    private ImsSimMobilityUpdate mImsMobilityUpdate;
    private boolean mImsSetupMode;
    private boolean mLoaded = false;
    private String mNote;
    /* access modifiers changed from: private */
    public int mPhoneId;
    private boolean mShipBuild;
    private handleSmkConfig mSmkConfig;
    /* access modifiers changed from: private */
    public StorageEventListener mStorageListener;
    private JsonElement mUpdate;
    private boolean mUpdatedGlobalSettings;
    private boolean mUpdatedImsProfile;
    private boolean mUpdatedImsSwitch;

    protected ImsAutoUpdate(Context context, int i) {
        JsonNull jsonNull = JsonNull.INSTANCE;
        this.mUpdate = jsonNull;
        this.mCarrierUpdate = jsonNull;
        this.mCurrentHash = "";
        this.mCurrentCarrierFeatureHash = "";
        this.mHashChanged = false;
        this.mUpdatedImsProfile = false;
        this.mUpdatedGlobalSettings = false;
        this.mUpdatedImsSwitch = false;
        this.mShipBuild = SemSystemProperties.get("ro.product_ship", ConfigConstants.VALUE.INFO_COMPLETED).equals(CloudMessageProviderContract.JsonData.TRUE);
        this.mImsSetupMode = SemSystemProperties.get(ImsConstants.SystemProperties.IMSSETUP_MODE, ConfigConstants.VALUE.INFO_COMPLETED).equals(CloudMessageProviderContract.JsonData.TRUE);
        this.mNote = "";
        this.mStorageListener = new StorageEventListener() {
            public void onStorageStateChanged(String str, String str2, String str3) {
                if (str2.equals("checking") && str3.equals("mounted")) {
                    IMSLog.i(ImsAutoUpdate.LOG_TAG, ImsAutoUpdate.this.mPhoneId, "onStorageStateChanged : checking -> mounted");
                    ImsAutoUpdate.mStorage.unregisterListener(ImsAutoUpdate.this.mStorageListener);
                    if (!TextUtils.isEmpty(ImsAutoUpdate.this.getExternalStorageImsUpdatePath())) {
                        IMSLog.i(ImsAutoUpdate.LOG_TAG, ImsAutoUpdate.this.mPhoneId, "ImsSetup mode. imsservice restart");
                        SemSystemProperties.set(ImsConstants.SystemProperties.IMSSETUP_MODE, CloudMessageProviderContract.JsonData.TRUE);
                        SystemWrapper.exit(0);
                    }
                }
            }
        };
        this.mHashManager = HashManager.getInstance(context, i);
        this.mImsMobilityUpdate = ImsSimMobilityUpdate.getInstance(context);
        handleSmkConfig handlesmkconfig = new handleSmkConfig(context);
        this.mSmkConfig = handlesmkconfig;
        handlesmkconfig.load();
        this.mContext = context;
        this.mPhoneId = i;
        this.mEventLog = new SimpleEventLog(context, i, LOG_TAG, 200);
        if (!this.mShipBuild && !this.mImsSetupMode && mStorage == null) {
            StorageManager storageManager = (StorageManager) this.mContext.getSystemService(StorageManager.class);
            mStorage = storageManager;
            storageManager.registerListener(this.mStorageListener);
        }
    }

    public static ImsAutoUpdate getInstance(Context context, int i) {
        HashMap<Integer, ImsAutoUpdate> hashMap = sInstances;
        synchronized (hashMap) {
            if (hashMap.containsKey(Integer.valueOf(i))) {
                ImsAutoUpdate imsAutoUpdate = hashMap.get(Integer.valueOf(i));
                return imsAutoUpdate;
            }
            hashMap.put(Integer.valueOf(i), new ImsAutoUpdate(context, i));
            hashMap.get(Integer.valueOf(i)).checkLoaded();
            ImsAutoUpdate imsAutoUpdate2 = hashMap.get(Integer.valueOf(i));
            return imsAutoUpdate2;
        }
    }

    public boolean checkLoaded() {
        if (!this.mLoaded) {
            this.mLoaded = (loadImsAutoUpdate() && !this.mUpdate.isJsonNull()) || getSmkConfig() != null;
        }
        return this.mLoaded;
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(15:6|7|8|9|(1:22)(5:13|(1:17)|18|19|20)|23|24|(1:26)|27|28|29|30|31|32|33) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x00e6 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean loadImsAutoUpdate() {
        /*
            r11 = this;
            java.lang.String r0 = r11.getUpdateFilePath()
            java.lang.String r1 = LOG_TAG
            int r2 = r11.mPhoneId
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "Use imsupdate file on "
            r3.append(r4)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.d(r1, r2, r3)
            java.io.File r2 = new java.io.File
            r2.<init>(r0)
            boolean r0 = r2.exists()
            java.lang.String r3 = "]"
            r4 = 0
            if (r0 == 0) goto L_0x0110
            long r5 = r2.length()
            r7 = 0
            int r0 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r0 <= 0) goto L_0x0110
            java.io.FileInputStream r0 = new java.io.FileInputStream     // Catch:{ JsonParseException | IOException -> 0x0102 }
            java.lang.String r5 = r2.getAbsolutePath()     // Catch:{ JsonParseException | IOException -> 0x0102 }
            r0.<init>(r5)     // Catch:{ JsonParseException | IOException -> 0x0102 }
            com.google.gson.stream.JsonReader r5 = new com.google.gson.stream.JsonReader     // Catch:{ all -> 0x00f8 }
            java.io.BufferedReader r6 = new java.io.BufferedReader     // Catch:{ all -> 0x00f8 }
            java.io.InputStreamReader r9 = new java.io.InputStreamReader     // Catch:{ all -> 0x00f8 }
            r9.<init>(r0)     // Catch:{ all -> 0x00f8 }
            r6.<init>(r9)     // Catch:{ all -> 0x00f8 }
            r5.<init>(r6)     // Catch:{ all -> 0x00f8 }
            com.google.gson.JsonParser r6 = new com.google.gson.JsonParser     // Catch:{ all -> 0x00ee }
            r6.<init>()     // Catch:{ all -> 0x00ee }
            com.google.gson.JsonElement r6 = r6.parse(r5)     // Catch:{ all -> 0x00ee }
            r11.mUpdate = r6     // Catch:{ all -> 0x00ee }
            boolean r6 = r6.isJsonNull()     // Catch:{ all -> 0x00ee }
            if (r6 != 0) goto L_0x00ae
            com.google.gson.JsonElement r6 = r11.mUpdate     // Catch:{ all -> 0x00ee }
            boolean r6 = r6.isJsonObject()     // Catch:{ all -> 0x00ee }
            if (r6 == 0) goto L_0x00ae
            com.google.gson.JsonElement r6 = r11.mUpdate     // Catch:{ all -> 0x00ee }
            com.google.gson.JsonObject r6 = r6.getAsJsonObject()     // Catch:{ all -> 0x00ee }
            java.lang.String r9 = "note"
            com.google.gson.JsonElement r6 = r6.get(r9)     // Catch:{ all -> 0x00ee }
            if (r6 == 0) goto L_0x0097
            boolean r9 = r6.isJsonNull()     // Catch:{ all -> 0x00ee }
            if (r9 != 0) goto L_0x0097
            java.lang.String r6 = r6.getAsString()     // Catch:{ all -> 0x00ee }
            r11.mNote = r6     // Catch:{ all -> 0x00ee }
            int r6 = r11.mPhoneId     // Catch:{ all -> 0x00ee }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ all -> 0x00ee }
            r9.<init>()     // Catch:{ all -> 0x00ee }
            java.lang.String r10 = "imsupdate is ready : "
            r9.append(r10)     // Catch:{ all -> 0x00ee }
            java.lang.String r10 = r11.mNote     // Catch:{ all -> 0x00ee }
            r9.append(r10)     // Catch:{ all -> 0x00ee }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x00ee }
            com.sec.internal.log.IMSLog.d(r1, r6, r9)     // Catch:{ all -> 0x00ee }
        L_0x0097:
            com.google.gson.JsonElement r1 = r11.mUpdate     // Catch:{ NullPointerException -> 0x00ac }
            com.google.gson.JsonObject r1 = r1.getAsJsonObject()     // Catch:{ NullPointerException -> 0x00ac }
            java.lang.String r6 = "imsprofile_update"
            com.google.gson.JsonObject r1 = r1.getAsJsonObject(r6)     // Catch:{ NullPointerException -> 0x00ac }
            java.lang.String r6 = "profile"
            com.google.gson.JsonArray r1 = r1.getAsJsonArray(r6)     // Catch:{ NullPointerException -> 0x00ac }
            mUpdateArrays = r1     // Catch:{ NullPointerException -> 0x00ac }
        L_0x00ac:
            r1 = 1
            goto L_0x00af
        L_0x00ae:
            r1 = r4
        L_0x00af:
            long r9 = r2.length()     // Catch:{ all -> 0x00ee }
            int r2 = (int) r9     // Catch:{ all -> 0x00ee }
            byte[] r2 = new byte[r2]     // Catch:{ all -> 0x00ee }
            java.nio.channels.FileChannel r6 = r0.getChannel()     // Catch:{ all -> 0x00ee }
            r6.position(r7)     // Catch:{ all -> 0x00ee }
            int r6 = r0.read(r2)     // Catch:{ all -> 0x00ee }
            if (r6 > 0) goto L_0x00de
            java.lang.String r7 = LOG_TAG     // Catch:{ all -> 0x00ee }
            int r8 = r11.mPhoneId     // Catch:{ all -> 0x00ee }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ all -> 0x00ee }
            r9.<init>()     // Catch:{ all -> 0x00ee }
            java.lang.String r10 = "Failed to read imsupdate.json! Got ["
            r9.append(r10)     // Catch:{ all -> 0x00ee }
            r9.append(r6)     // Catch:{ all -> 0x00ee }
            r9.append(r3)     // Catch:{ all -> 0x00ee }
            java.lang.String r6 = r9.toString()     // Catch:{ all -> 0x00ee }
            com.sec.internal.log.IMSLog.e(r7, r8, r6)     // Catch:{ all -> 0x00ee }
        L_0x00de:
            com.sec.internal.helper.HashManager r6 = r11.mHashManager     // Catch:{ Exception -> 0x00e6 }
            java.lang.String r2 = r6.getHash(r2)     // Catch:{ Exception -> 0x00e6 }
            r11.mCurrentHash = r2     // Catch:{ Exception -> 0x00e6 }
        L_0x00e6:
            r5.close()     // Catch:{ all -> 0x00f8 }
            r0.close()     // Catch:{ JsonParseException | IOException -> 0x0102 }
            r4 = r1
            goto L_0x011b
        L_0x00ee:
            r1 = move-exception
            r5.close()     // Catch:{ all -> 0x00f3 }
            goto L_0x00f7
        L_0x00f3:
            r2 = move-exception
            r1.addSuppressed(r2)     // Catch:{ all -> 0x00f8 }
        L_0x00f7:
            throw r1     // Catch:{ all -> 0x00f8 }
        L_0x00f8:
            r1 = move-exception
            r0.close()     // Catch:{ all -> 0x00fd }
            goto L_0x0101
        L_0x00fd:
            r0 = move-exception
            r1.addSuppressed(r0)     // Catch:{ JsonParseException | IOException -> 0x0102 }
        L_0x0101:
            throw r1     // Catch:{ JsonParseException | IOException -> 0x0102 }
        L_0x0102:
            r0 = move-exception
            r0.printStackTrace()
            java.lang.String r0 = LOG_TAG
            int r1 = r11.mPhoneId
            java.lang.String r2 = "imsupdate.json parsing fail."
            com.sec.internal.log.IMSLog.e(r0, r1, r2)
            goto L_0x011b
        L_0x0110:
            int r0 = r11.mPhoneId
            java.lang.String r2 = "imsupdate.json not found."
            com.sec.internal.log.IMSLog.e(r1, r0, r2)
            java.lang.String r0 = ""
            r11.mCurrentHash = r0
        L_0x011b:
            com.sec.internal.helper.HashManager r0 = r11.mHashManager
            java.lang.String r1 = "imsupdate"
            java.lang.String r2 = r11.mCurrentHash
            boolean r0 = r0.isHashChanged(r1, r2)
            r11.mHashChanged = r0
            java.lang.String r0 = LOG_TAG
            int r1 = r11.mPhoneId
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r5 = "loadImsAutoUpdate: hash changed ["
            r2.append(r5)
            boolean r11 = r11.mHashChanged
            r2.append(r11)
            r2.append(r3)
            java.lang.String r11 = r2.toString()
            com.sec.internal.log.IMSLog.d(r0, r1, r11)
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.ImsAutoUpdate.loadImsAutoUpdate():boolean");
    }

    public boolean isCarrierFeatureChanged(int i) {
        String string = SemCarrierFeature.getInstance().getString(i, SecFeature.CARRIER.IMSUPDATE, "", false);
        try {
            if (string.isEmpty()) {
                this.mCurrentCarrierFeatureHash = "";
            } else {
                this.mCurrentCarrierFeatureHash = this.mHashManager.getHash(string.getBytes());
            }
        } catch (Exception unused) {
        }
        return this.mHashManager.isHashChanged(HashManager.HASH_CARRIERFEATURE, this.mCurrentCarrierFeatureHash);
    }

    public void resetCarrierFeatureHash() {
        IMSLog.d(LOG_TAG, this.mPhoneId, "reset carrier config hash");
        this.mHashManager.saveHash(HashManager.HASH_CARRIERFEATURE, "");
    }

    public boolean loadCarrierFeature() {
        int carrierId = SemCarrierFeature.getInstance().getCarrierId(this.mPhoneId, false);
        int i = SemSystemProperties.getInt(ImsConstants.SystemProperties.CARRIERFEATURE_FORCE_USE, -1);
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.d(str, i2, "loadCarrierFeature  carrierId : " + carrierId + " forceProp : " + i);
        if (carrierId == -1 && i == -1) {
            return false;
        }
        try {
            JsonParser jsonParser = new JsonParser();
            String string = SemCarrierFeature.getInstance().getString(this.mPhoneId, SecFeature.CARRIER.IMSUPDATE, "", false);
            if (TextUtils.isEmpty(string)) {
                IMSLog.e(str, this.mPhoneId, "carrierfeature was not found.");
                return false;
            }
            String hash = this.mHashManager.getHash(string.getBytes());
            this.mCurrentCarrierFeatureHash = hash;
            this.mHashManager.saveHash(HashManager.HASH_CARRIERFEATURE, hash);
            JsonElement parse = jsonParser.parse(string);
            if (!JsonUtil.isValidJsonElement(parse)) {
                return false;
            }
            int i3 = this.mPhoneId;
            IMSLog.d(str, i3, "Successfully get carrier feature : " + parse.toString());
            this.mCarrierUpdate = parse;
            return true;
        } catch (Exception e) {
            String str2 = LOG_TAG;
            int i4 = this.mPhoneId;
            IMSLog.e(str2, i4, "Problem on Carrier feature : " + e.getMessage());
            return false;
        }
    }

    public String getExternalStorageImsUpdatePath() {
        String str;
        boolean equals = Environment.getExternalStorageState().equals("mounted");
        if (this.mShipBuild || !equals) {
            return "";
        }
        try {
            str = (String) Optional.ofNullable((StorageManager) this.mContext.getSystemService(StorageManager.class)).map(new ImsAutoUpdate$$ExternalSyntheticLambda0()).map(new ImsAutoUpdate$$ExternalSyntheticLambda1()).orElse("");
        } catch (ArrayIndexOutOfBoundsException e) {
            String str2 = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str2, i, "getExternalStorageImsUpdatePath() " + e.toString());
            str = "";
        }
        String str3 = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str3, i2, "getExternalStorageImsUpdatePath() path=" + str);
        File file = new File(str, IMSUPDATE_JSON_FILE);
        if (file.exists()) {
            return file.getPath();
        }
        return "";
    }

    public void saveHash() {
        this.mHashManager.saveHash(HashManager.HASH_IMSUPDATE, this.mCurrentHash);
        this.mHashManager.saveMemo(HashManager.HASH_IMSUPDATE, this.mNote);
        this.mHashChanged = false;
    }

    /* access modifiers changed from: protected */
    public String getUpdateFilePath() {
        String externalStorageImsUpdatePath = getExternalStorageImsUpdatePath();
        if (!TextUtils.isEmpty(externalStorageImsUpdatePath)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "ImsSetup. getUpdateFilePath() path=" + externalStorageImsUpdatePath);
            return externalStorageImsUpdatePath;
        } else if (!OmcCode.isOmcModel()) {
            return "/system/csc/imsupdate.json";
        } else {
            String omcNwPath = OmcCode.getOmcNwPath(this.mPhoneId);
            if (omcNwPath.contains(UPDATE_FILE_PATH_CSC)) {
                return "/system/csc/imsupdate.json";
            }
            String str = omcNwPath + "/" + IMSUPDATE_JSON_FILE;
            if (new File(str).exists()) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "getUpdateFilePath() omcPath : " + omcNwPath);
                return str;
            }
            String etcPath = OmcCode.getEtcPath();
            String substring = etcPath.substring(0, etcPath.length() - 3);
            String nWCode = OmcCode.getNWCode(this.mPhoneId);
            String str2 = substring + nWCode + "/" + IMSUPDATE_JSON_FILE;
            IMSLog.i(LOG_TAG, this.mPhoneId, " getUpdateFilePath() etcPath : " + etcPath + " / nwCode : " + nWCode);
            return str2;
        }
    }

    public boolean isUpdateNeeded() {
        String str = LOG_TAG;
        IMSLog.i(str, "checkLoaded : " + checkLoaded());
        return this.mHashChanged || this.mSmkConfig.hasNewSmkConfig();
    }

    public JsonElement selectResource(int i) {
        if (i == 0 && this.mSmkConfig != null) {
            return getSmkConfig() == null ? JsonNull.INSTANCE : getSmkConfig();
        }
        if (i == 1) {
            return this.mUpdate;
        }
        if (i == 4) {
            return this.mCarrierUpdate;
        }
        return JsonNull.INSTANCE;
    }

    private String sourceToString(int i) {
        if (i == 0) {
            return "SMK";
        }
        if (i == 1) {
            return "IMSUPDATE";
        }
        if (i == 4) {
            return "CARRIER_FEATURE";
        }
        return "UNKNOWN update source " + i;
    }

    public JsonElement getImsProfileUpdate(int i, String str) {
        JsonElement selectResource = selectResource(i);
        try {
            if (selectResource.getAsJsonObject().has(IMSPROFILE_UPDATE)) {
                JsonObject asJsonObject = selectResource.getAsJsonObject().getAsJsonObject(IMSPROFILE_UPDATE);
                if (JsonUtil.isValidJsonElement(asJsonObject) && asJsonObject.has(str)) {
                    return asJsonObject.get(str);
                }
            }
        } catch (IllegalStateException unused) {
        }
        return JsonNull.INSTANCE;
    }

    public JsonElement getImsSwitches(int i, String str) {
        JsonElement selectResource = selectResource(i);
        try {
            if (selectResource.getAsJsonObject().has(IMSSWITCH_UPDATE)) {
                JsonObject asJsonObject = selectResource.getAsJsonObject().getAsJsonObject(IMSSWITCH_UPDATE);
                if (JsonUtil.isValidJsonElement(asJsonObject) && asJsonObject.has(str)) {
                    return asJsonObject.get(str);
                }
            }
        } catch (IllegalStateException unused) {
        }
        return JsonNull.INSTANCE;
    }

    public JsonElement getGlobalSettings(int i, String str) {
        JsonElement selectResource = selectResource(i);
        try {
            if (selectResource.getAsJsonObject().has(GLOBALSETTINGS_UPDATE)) {
                JsonObject asJsonObject = selectResource.getAsJsonObject().getAsJsonObject(GLOBALSETTINGS_UPDATE);
                if (JsonUtil.isValidJsonElement(asJsonObject) && asJsonObject.has(str)) {
                    return asJsonObject.get(str);
                }
            }
        } catch (IllegalStateException unused) {
        }
        return JsonNull.INSTANCE;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0012, code lost:
        r5 = r5.getAsJsonArray();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getGlobalSettingsSpecificParam(int r5, java.lang.String r6, java.lang.String r7) {
        /*
            r4 = this;
            java.lang.String r0 = "globalsetting"
            com.google.gson.JsonElement r5 = r4.getGlobalSettings(r5, r0)
            boolean r0 = r5.isJsonNull()
            if (r0 != 0) goto L_0x004c
            boolean r0 = r5.isJsonArray()
            if (r0 == 0) goto L_0x004c
            com.google.gson.JsonArray r5 = r5.getAsJsonArray()
            int r0 = getIndexWithMnoname(r5, r6)
            r1 = -1
            if (r0 == r1) goto L_0x004c
            java.lang.String r1 = LOG_TAG
            int r4 = r4.mPhoneId
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Found Globalsetting for : "
            r2.append(r3)
            r2.append(r6)
            java.lang.String r6 = r2.toString()
            com.sec.internal.log.IMSLog.d(r1, r4, r6)
            com.google.gson.JsonElement r4 = r5.get(r0)
            com.google.gson.JsonObject r4 = r4.getAsJsonObject()
            boolean r5 = r4.has(r7)
            if (r5 == 0) goto L_0x004c
            com.google.gson.JsonElement r4 = r4.get(r7)
            java.lang.String r4 = r4.getAsString()
            return r4
        L_0x004c:
            r4 = 0
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.ImsAutoUpdate.getGlobalSettingsSpecificParam(int, java.lang.String, java.lang.String):java.lang.String");
    }

    public boolean getDefaultGlobalSettingsFromImsUpdate(String str) {
        JsonElement globalSettings = getGlobalSettings(1, TAG_GLOBALSETTINGS_DEFAULT);
        if (!JsonUtil.isValidJsonElement(globalSettings)) {
            return false;
        }
        JsonObject asJsonObject = globalSettings.getAsJsonObject();
        if (!JsonUtil.isValidJsonElement(asJsonObject) || !asJsonObject.has(str) || !asJsonObject.get(str).isJsonPrimitive()) {
            return false;
        }
        return asJsonObject.get(str).getAsBoolean();
    }

    public JsonElement getMnomap(int i, String str) {
        JsonElement selectResource = selectResource(i);
        try {
            if (selectResource.getAsJsonObject().has(MNOMAP_UPDATE)) {
                JsonObject asJsonObject = selectResource.getAsJsonObject().getAsJsonObject(MNOMAP_UPDATE);
                if (JsonUtil.isValidJsonElement(asJsonObject) && asJsonObject.has(str)) {
                    if ("[]".equals(asJsonObject.get(str).toString())) {
                        return JsonNull.INSTANCE;
                    }
                    return asJsonObject.get(str);
                }
            }
        } catch (IllegalStateException unused) {
        }
        return JsonNull.INSTANCE;
    }

    public JsonElement getProviderSettings(int i, String str) {
        JsonElement selectResource = selectResource(i);
        try {
            if (selectResource.getAsJsonObject().has(PROVIDERSETTINGS_UPDATE)) {
                JsonObject asJsonObject = selectResource.getAsJsonObject().getAsJsonObject(PROVIDERSETTINGS_UPDATE);
                if (JsonUtil.isValidJsonElement(asJsonObject) && asJsonObject.has(str)) {
                    if ("[]".equals(asJsonObject.get(str).toString())) {
                        return JsonNull.INSTANCE;
                    }
                    return asJsonObject.get(str);
                }
            }
        } catch (IllegalStateException unused) {
        }
        return JsonNull.INSTANCE;
    }

    public JsonElement applyImsProfileUpdate(JsonArray jsonArray, String str) {
        JsonElement applyImsProfileUpdate = applyImsProfileUpdate(applyImsProfileUpdate(applyImsProfileUpdate(jsonArray, 0, str).getAsJsonArray(), 1, str).getAsJsonArray(), 4, str);
        this.mUpdatedImsProfile = true;
        if ((this.mUpdatedGlobalSettings || this.mUpdatedImsSwitch) && this.mHashChanged) {
            saveHash();
        }
        return applyImsProfileUpdate;
    }

    public JsonElement applyImsProfileUpdate(JsonArray jsonArray, int i, String str) {
        ImsAutoUpdate imsAutoUpdate = this;
        JsonArray jsonArray2 = jsonArray;
        int i2 = i;
        String str2 = str;
        JsonElement imsProfileUpdate = imsAutoUpdate.getImsProfileUpdate(i2, "profile");
        if (!jsonArray.isJsonNull() && !imsProfileUpdate.isJsonNull() && imsProfileUpdate.isJsonArray()) {
            jsonArray2 = (JsonArray) JsonUtil.deepCopy(jsonArray2, JsonArray.class);
            boolean needCheckMvno = needCheckMvno(jsonArray2, str2);
            String str3 = LOG_TAG;
            int i3 = imsAutoUpdate.mPhoneId;
            IMSLog.d(str3, i3, "applyImsProfileUpdate fullNameCheck : " + needCheckMvno);
            Iterator it = imsProfileUpdate.getAsJsonArray().iterator();
            while (it.hasNext()) {
                JsonElement jsonElement = (JsonElement) it.next();
                JsonObject asJsonObject = jsonElement.getAsJsonObject();
                if (asJsonObject.has("name") && asJsonObject.has("mnoname")) {
                    String asString = asJsonObject.get("name").getAsString();
                    String asString2 = asJsonObject.get("mnoname").getAsString();
                    if (TextUtils.isEmpty(str) || isMatchedMnoName(needCheckMvno, asString2, str2) || (asJsonObject.get("mdmn_type") != null && asJsonObject.get("mdmn_type").getAsString().equals(str2))) {
                        int indexWithNames = getIndexWithNames(jsonArray2, asString, asString2);
                        if (indexWithNames == -1) {
                            jsonArray2.add(jsonElement);
                            SimpleEventLog simpleEventLog = imsAutoUpdate.mEventLog;
                            simpleEventLog.logAndAdd("add imsprofile by resource: " + i2 + " => " + asString);
                        } else {
                            JsonElement remove = asJsonObject.remove("mnoname");
                            JsonElement jsonElement2 = jsonArray2.get(indexWithNames);
                            SimpleEventLog simpleEventLog2 = imsAutoUpdate.mEventLog;
                            simpleEventLog2.logAndAdd("update imsprofile by resource: " + i2 + " => " + asString);
                            JsonElement merge = JsonUtil.merge(jsonElement2, jsonElement);
                            if (!merge.isJsonNull()) {
                                jsonArray2.set(indexWithNames, merge);
                            }
                            if (remove != null && !remove.isJsonNull()) {
                                asJsonObject.add("mnoname", remove);
                            }
                        }
                    }
                }
                imsAutoUpdate = this;
            }
        }
        return jsonArray2;
    }

    public JsonElement getUpdatedGlobalSetting(JsonElement jsonElement) {
        JsonElement applyGlobalSettingUpdate = applyGlobalSettingUpdate(applyGlobalSettingUpdate(applyGlobalSettingUpdate(jsonElement, 0), 1), 4);
        this.mUpdatedGlobalSettings = true;
        if (this.mUpdatedImsProfile && this.mHashChanged) {
            saveHash();
        }
        return applyGlobalSettingUpdate;
    }

    public JsonElement applyGlobalSettingUpdate(JsonElement jsonElement, int i) {
        JsonElement jsonElement2;
        int indexWithMnoname;
        if (!JsonUtil.isValidJsonElement(jsonElement)) {
            IMSLog.d(LOG_TAG, this.mPhoneId, "Not a valid GlobalElement.");
            return jsonElement;
        }
        String asString = jsonElement.getAsJsonObject().get("mnoname").getAsString();
        JsonElement globalSettings = getGlobalSettings(i, TAG_GLOBALSETTING);
        if (!JsonUtil.isValidJsonElement(globalSettings) || (indexWithMnoname = getIndexWithMnoname(globalSettings.getAsJsonArray(), asString)) == -1) {
            jsonElement2 = jsonElement;
        } else {
            JsonElement jsonElement3 = globalSettings.getAsJsonArray().get(indexWithMnoname);
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("update globalsettings by resource: " + i + " => " + jsonElement3);
            jsonElement2 = JsonUtil.merge(jsonElement, jsonElement3);
        }
        return jsonElement2 != JsonNull.INSTANCE ? jsonElement2 : jsonElement;
    }

    public JsonElement getUpdatedImsSwitch(JsonElement jsonElement) {
        JsonElement applyImsSwitchUpdate = applyImsSwitchUpdate(applyImsSwitchUpdate(applyImsSwitchUpdate(jsonElement, 0), 1), 4);
        this.mUpdatedImsSwitch = true;
        if (this.mUpdatedImsProfile && this.mHashChanged) {
            saveHash();
        }
        return applyImsSwitchUpdate;
    }

    public boolean isMatchedImsSwitch(int i, String str, String str2) {
        JsonElement imsSwitches = getImsSwitches(i, "imsswitch");
        if (!TextUtils.isEmpty(str2)) {
            str = str + MVNO_DELIMITER + str2;
        }
        String str3 = LOG_TAG;
        IMSLog.d(str3, this.mPhoneId, "isMatchedImsSwitch source : " + sourceToString(i) + " for : " + str);
        if (!JsonUtil.isValidJsonElement(imsSwitches) || getIndexWithMnoname(imsSwitches.getAsJsonArray(), str) == -1) {
            return false;
        }
        Log.d(str3, "isMatchedImsSwitch for : " + str);
        return true;
    }

    public JsonElement applyImsSwitchUpdate(JsonElement jsonElement, int i) {
        JsonElement jsonElement2;
        int indexWithMnoname;
        if (!JsonUtil.isValidJsonElement(jsonElement)) {
            IMSLog.d(LOG_TAG, this.mPhoneId, "Not a valid imsswitchElement.");
            return jsonElement;
        }
        String asString = jsonElement.getAsJsonObject().get("mnoname").getAsString();
        JsonElement imsSwitches = getImsSwitches(i, "imsswitch");
        if (!JsonUtil.isValidJsonElement(imsSwitches) || (indexWithMnoname = getIndexWithMnoname(imsSwitches.getAsJsonArray(), asString)) == -1) {
            jsonElement2 = jsonElement;
        } else {
            JsonElement jsonElement3 = imsSwitches.getAsJsonArray().get(indexWithMnoname);
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("update imsswitch by resource: " + i + " => " + jsonElement3);
            jsonElement2 = JsonUtil.merge(jsonElement, jsonElement3);
        }
        return jsonElement2 != JsonNull.INSTANCE ? jsonElement2 : jsonElement;
    }

    public JsonElement applyNohitSettingUpdate(JsonElement jsonElement) {
        JsonElement globalSettings = getGlobalSettings(0, TAG_GLOBALSETTINGS_NOHIT);
        JsonElement globalSettings2 = getGlobalSettings(1, TAG_GLOBALSETTINGS_NOHIT);
        JsonElement globalSettings3 = getGlobalSettings(4, TAG_GLOBALSETTINGS_NOHIT);
        if (JsonUtil.isValidJsonElement(globalSettings)) {
            JsonElement merge = JsonUtil.merge(jsonElement, globalSettings);
            if (JsonUtil.isValidJsonElement(merge)) {
                jsonElement = merge;
            }
        }
        if (JsonUtil.isValidJsonElement(globalSettings2)) {
            JsonElement merge2 = JsonUtil.merge(jsonElement, globalSettings2);
            if (JsonUtil.isValidJsonElement(merge2)) {
                jsonElement = merge2;
            }
        }
        if (!JsonUtil.isValidJsonElement(globalSettings3)) {
            return jsonElement;
        }
        JsonElement merge3 = JsonUtil.merge(jsonElement, globalSettings3);
        return JsonUtil.isValidJsonElement(merge3) ? merge3 : jsonElement;
    }

    public JsonElement applyDefaultSettingUpdate(JsonElement jsonElement) {
        JsonElement globalSettings = getGlobalSettings(0, TAG_GLOBALSETTINGS_DEFAULT);
        JsonElement globalSettings2 = getGlobalSettings(1, TAG_GLOBALSETTINGS_DEFAULT);
        JsonElement globalSettings3 = getGlobalSettings(4, TAG_GLOBALSETTINGS_DEFAULT);
        if (JsonUtil.isValidJsonElement(globalSettings)) {
            JsonElement merge = JsonUtil.merge(jsonElement, globalSettings);
            if (JsonUtil.isValidJsonElement(merge)) {
                jsonElement = merge;
            }
        }
        if (JsonUtil.isValidJsonElement(globalSettings2)) {
            JsonElement merge2 = JsonUtil.merge(jsonElement, globalSettings2);
            if (JsonUtil.isValidJsonElement(merge2)) {
                jsonElement = merge2;
            }
        }
        if (!JsonUtil.isValidJsonElement(globalSettings3)) {
            return jsonElement;
        }
        JsonElement merge3 = JsonUtil.merge(jsonElement, globalSettings3);
        return JsonUtil.isValidJsonElement(merge3) ? merge3 : jsonElement;
    }

    public JsonElement getRcsDefaultPolicyUpdate(JsonElement jsonElement, boolean z) {
        return applyRcsDefaultPolicyUpdate(applyRcsDefaultPolicyUpdate(applyRcsDefaultPolicyUpdate(jsonElement, 0, z), 1, z), 4, z);
    }

    private JsonElement applyRcsDefaultPolicyUpdate(JsonElement jsonElement, int i, boolean z) {
        String str = z ? TAG_DEFAULT_UP_POLICY : TAG_DEFAULT_RCS_POLICY;
        JsonElement selectResource = selectResource(i);
        JsonElement jsonElement2 = JsonNull.INSTANCE;
        try {
            if (selectResource.getAsJsonObject().has(RCSRPOLICY_UPDATE)) {
                JsonObject asJsonObject = selectResource.getAsJsonObject().getAsJsonObject(RCSRPOLICY_UPDATE);
                if (JsonUtil.isValidJsonElement(asJsonObject) && asJsonObject.has(str)) {
                    jsonElement2 = asJsonObject.get(str);
                }
            }
        } catch (IllegalStateException unused) {
        }
        if (!JsonUtil.isValidJsonElement(jsonElement2)) {
            return jsonElement;
        }
        JsonElement merge = JsonUtil.merge(jsonElement, jsonElement2);
        return JsonUtil.isValidJsonElement(merge) ? merge : jsonElement;
    }

    public JsonElement getRcsPolicyUpdate(JsonElement jsonElement, String str) {
        if (!TextUtils.isEmpty(str) && !jsonElement.isJsonNull()) {
            return applyRcsPolicySettingUpdate(applyRcsPolicySettingUpdate(applyRcsPolicySettingUpdate(jsonElement, 0, str), 1, str), 4, str);
        }
        IMSLog.e(LOG_TAG, this.mPhoneId, "policyName is not valid or policy is JsonNull");
        return jsonElement;
    }

    private JsonElement applyRcsPolicySettingUpdate(JsonElement jsonElement, int i, String str) {
        JsonElement selectResource = selectResource(i);
        JsonElement jsonElement2 = JsonNull.INSTANCE;
        try {
            if (selectResource.getAsJsonObject().has(RCSRPOLICY_UPDATE)) {
                JsonObject asJsonObject = selectResource.getAsJsonObject().getAsJsonObject(RCSRPOLICY_UPDATE);
                if (JsonUtil.isValidJsonElement(asJsonObject) && asJsonObject.has(TAG_RCS_POLICY)) {
                    Iterator it = asJsonObject.getAsJsonArray(TAG_RCS_POLICY).iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        JsonElement jsonElement3 = (JsonElement) it.next();
                        JsonObject asJsonObject2 = jsonElement3.getAsJsonObject();
                        if (asJsonObject2.has(TAG_POLICY_NAME) && TextUtils.equals(asJsonObject2.get(TAG_POLICY_NAME).getAsString(), str)) {
                            jsonElement2 = jsonElement3;
                            break;
                        }
                    }
                }
            }
            if (!JsonUtil.isValidJsonElement(jsonElement2)) {
                return jsonElement;
            }
            JsonElement merge = JsonUtil.merge(jsonElement, jsonElement2);
            return JsonUtil.isValidJsonElement(merge) ? merge : jsonElement;
        } catch (IllegalStateException unused) {
        }
    }

    /* access modifiers changed from: package-private */
    public JsonElement getUpdatedSmsSetting(JsonElement jsonElement, String str) {
        JsonArray asJsonArray;
        int indexWithMnoname;
        int[] iArr = {0, 1};
        JsonElement jsonElement2 = JsonNull.INSTANCE;
        String asString = jsonElement.getAsJsonObject().get("mnoname").getAsString();
        for (int i = 0; i < 2; i++) {
            try {
                JsonObject asJsonObject = selectResource(iArr[i]).getAsJsonObject().getAsJsonObject(SMS_SETTINGS_UPDATE);
                if (JsonUtil.isValidJsonElement(asJsonObject)) {
                    if (SmsSetting.Properties.DEFAULT_SETTING.equalsIgnoreCase(str)) {
                        jsonElement2 = JsonUtil.merge(jsonElement2, asJsonObject.get(str));
                    } else if ("mnoname".equalsIgnoreCase(str) && (indexWithMnoname = getIndexWithMnoname(asJsonArray, asString)) != -1) {
                        jsonElement2 = JsonUtil.merge(jsonElement2, (asJsonArray = asJsonObject.getAsJsonArray(SmsSetting.Properties.SMS_SETTINGS)).get(indexWithMnoname));
                    }
                }
            } catch (ArrayIndexOutOfBoundsException | IllegalStateException unused) {
            }
        }
        JsonElement merge = JsonUtil.merge(jsonElement, jsonElement2);
        return JsonUtil.isValidJsonElement(merge) ? merge : jsonElement;
    }

    public static int getIndexWithMnoname(JsonArray jsonArray, String str) {
        if (jsonArray != null && !jsonArray.isJsonNull() && jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                try {
                    JsonObject asJsonObject = jsonArray.get(i).getAsJsonObject();
                    JsonElement jsonElement = asJsonObject.get("mnoname");
                    if (jsonElement != null && !asJsonObject.isJsonNull() && jsonElement.getAsString().equalsIgnoreCase(str)) {
                        return i;
                    }
                } catch (ClassCastException | IllegalStateException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
        String str2 = LOG_TAG;
        Log.e(str2, "no matched element with mnoname " + str);
        return -1;
    }

    private static boolean needCheckMvno(JsonArray jsonArray, String str) {
        if (jsonArray.isJsonNull() || jsonArray.size() <= 0) {
            return false;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            try {
                JsonElement jsonElement = jsonArray.get(i).getAsJsonObject().get("mnoname");
                if (jsonElement.getAsString().startsWith(str.split(":")[0]) && jsonElement.getAsString().indexOf(Mno.MVNO_DELIMITER) != -1) {
                    return true;
                }
            } catch (ClassCastException | IllegalStateException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean isMatchedMnoName(boolean z, String str, String str2) {
        if (!z || !str.equalsIgnoreCase(str2)) {
            return !z && str.startsWith(str2.split(":")[0]);
        }
        return true;
    }

    private static int getIndexWithNames(JsonArray jsonArray, String str, String str2) {
        if (!jsonArray.isJsonNull() && jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                try {
                    JsonObject asJsonObject = jsonArray.get(i).getAsJsonObject();
                    JsonElement jsonElement = asJsonObject.get("name");
                    JsonElement jsonElement2 = asJsonObject.get("mnoname");
                    if (jsonElement != null && jsonElement2 != null && !asJsonObject.isJsonNull() && jsonElement.getAsString().equalsIgnoreCase(str) && jsonElement2.getAsString().equalsIgnoreCase(str2)) {
                        return i;
                    }
                } catch (ClassCastException | IllegalStateException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
        String str3 = LOG_TAG;
        Log.e(str3, "no matched element with name " + str + "and mnoname " + str2);
        return -1;
    }

    public void updateSmkConfig(String str) {
        if (!TextUtils.isEmpty(str)) {
            this.mSmkConfig.saveSmkConfig(new JsonParser().parse(str).getAsJsonObject());
        }
    }

    public void clearSmkConfig() {
        this.mSmkConfig.clearSmkConfig();
    }

    public JsonObject getSmkConfig() {
        return this.mSmkConfig.getSmkConfig();
    }

    public static String readFromJsonFile(String str, String str2) {
        JsonArray jsonArray = mUpdateArrays;
        if (jsonArray != null && !jsonArray.isJsonNull()) {
            Iterator it = mUpdateArrays.iterator();
            while (it.hasNext()) {
                try {
                    JsonObject asJsonObject = ((JsonElement) it.next()).getAsJsonObject();
                    if (str.equals(asJsonObject.get("name").getAsString())) {
                        return asJsonObject.get(str2).getAsString();
                    }
                } catch (NullPointerException unused) {
                }
            }
        }
        return "";
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, this.mPhoneId, "\nDump of ImsAutoUpdate:");
        this.mEventLog.dump();
    }

    public void resetLoaded() {
        this.mLoaded = false;
    }

    public static class handleSmkConfig {
        private static final String LOG_TAG = "handleSmkConfig";
        private JsonObject mCachedSmkConfig;
        private Context mContext;
        private final File mDownloadedSmkConfig = new File(this.mContext.getFilesDir(), "smkconfig.json");
        private boolean mHasNewSmkConfig = false;

        public handleSmkConfig(Context context) {
            this.mContext = context;
        }

        public void load() {
            try {
                if (this.mDownloadedSmkConfig.exists()) {
                    this.mCachedSmkConfig = new JsonParser().parse(new String(Files.readAllBytes(this.mDownloadedSmkConfig.toPath()))).getAsJsonObject();
                }
            } catch (JsonSyntaxException | IOException e) {
                e.printStackTrace();
            }
        }

        public void saveSmkConfig(JsonObject jsonObject) {
            Log.d(LOG_TAG, "Save downloaded Smk Config");
            try {
                if (this.mDownloadedSmkConfig.exists()) {
                    this.mDownloadedSmkConfig.delete();
                }
                this.mDownloadedSmkConfig.createNewFile();
                Files.write(this.mDownloadedSmkConfig.toPath(), jsonObject.toString().getBytes(), new OpenOption[0]);
                Log.d(LOG_TAG, "Store downloaded Smk Config complete");
                this.mCachedSmkConfig = jsonObject;
                this.mHasNewSmkConfig = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public JsonObject getSmkConfig() {
            return this.mCachedSmkConfig;
        }

        public void clearSmkConfig() {
            Log.d(LOG_TAG, "Clear Smk Config");
            if (this.mCachedSmkConfig != null) {
                try {
                    if (this.mDownloadedSmkConfig.exists()) {
                        this.mDownloadedSmkConfig.delete();
                        disableSmkConfig();
                        Log.d(LOG_TAG, "Clear Smk Config Successfully");
                    }
                } catch (Exception unused) {
                    Log.d(LOG_TAG, "has problem for delete Smk Config");
                }
                this.mCachedSmkConfig = null;
            }
        }

        public void disableSmkConfig() {
            this.mHasNewSmkConfig = false;
        }

        public boolean hasNewSmkConfig() {
            return this.mHasNewSmkConfig;
        }
    }
}
