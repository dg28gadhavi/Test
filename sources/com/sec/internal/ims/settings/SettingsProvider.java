package com.sec.internal.ims.settings;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.SemSystemProperties;
import android.provider.BaseColumns;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonNull;
import com.samsung.android.cmcsetting.CmcSettingManager;
import com.samsung.android.cmcsetting.CmcSettingManagerConstants;
import com.samsung.android.feature.SemCarrierFeature;
import com.sec.ims.configuration.DATA;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.ImsSettings;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.os.IccCardConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.ims.config.adapters.StorageAdapter;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.core.sim.MnoMapJsonParser;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsProvider extends ContentProvider {
    private static final String ACTION_CARRIER_CHANGED = "com.samsung.carrier.action.CARRIER_CHANGED";
    private static final int CONFIG_DB_RESET = 28;
    private static final int CSC_PROFILE = 7;
    private static final int CSC_PROFILE_ID = 8;
    private static final int CSC_SETTING = 9;
    private static final int CSC_SETTING_ID = 10;
    private static final int DEBUG_CONFIG = 21;
    private static final int DM_ACCESS = 24;
    private static final String DM_CONFIG_URI = "com.samsung.rcs.dmconfigurationprovider";
    private static final int DNS_BLOCK = 20;
    private static final int DOWNLOAD_CONFIG = 29;
    private static final int DT_LOC_USER_CONSENT = 39;
    private static final int EPDG_SYSTEM_SETTINGS = 38;
    private static final String EXTRA_CARRIER_GROUP_CHANGED = "com.samsung.carrier.extra.CARRIER_GROUP_CHANGED";
    private static final String EXTRA_CARRIER_PHONEID = "com.samsung.carrier.extra.CARRIER_PHONE_ID";
    private static final String EXTRA_CARRIER_STATE = "com.samsung.carrier.extra.CARRIER_STATE";
    private static final int GCF_CONFIG_NAME = 19;
    private static final int GCF_INIT_RAT = 35;
    private static final int IDC_CONFIG = 40;
    private static final int IMPU = 17;
    private static final int IMS_GLOBAL = 4;
    private static final int IMS_GLOBAL_ID = 5;
    private static final int IMS_GLOBAL_RESET = 6;
    private static final int IMS_PROFILE = 1;
    private static final int IMS_PROFILE_ID = 2;
    private static final int IMS_PROFILE_RESET = 3;
    private static final int IMS_SMK_SECRET_KEY = 33;
    private static final int IMS_SWITCH = 11;
    private static final int IMS_SWITCH_NAME = 13;
    private static final int IMS_SWITCH_RESET = 12;
    private static final int IMS_USER_SETTING = 36;
    private static final String LOG_DELETE = "Delete";
    private static final String LOG_INSERT = "Insert";
    private static final String LOG_QUERY = "Query";
    public static final String LOG_TAG = "ImsSettingsProvider";
    private static final String LOG_UPDATE = "Update";
    private static final int MNO = 23;
    private static final int NV_LIST = 26;
    private static final int NV_STORAGE = 15;
    private static final int PROFILE_MATCHER = 0;
    private static final int RCS_VER = 31;
    private static final String RCS_VERSION = "6.0.3";
    private static final int READ_ALL_OMADM = 25;
    private static final int RESET_DOWNLOAD_CONFIG = 30;
    private static final int SELF_PROVISIONING = 18;
    private static final int SELF_WIFICALLINGACTIVATION = 22;
    private static final int SIM_DATA = 14;
    private static final int SIM_MOBILITY = 32;
    private static final IntentFilter SIM_STATE_CHANGED_INTENT_FILTER;
    private static final int SMK_UPDATED_INFO = 34;
    private static final int SMS_SETTING = 37;
    private static final int USER_CONFIG = 16;
    private static final UriMatcher sUriMatcher;
    private final BroadcastReceiver mCarrierFeatureReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String stringExtra = intent.getStringExtra(SettingsProvider.EXTRA_CARRIER_STATE);
            boolean booleanExtra = intent.getBooleanExtra(SettingsProvider.EXTRA_CARRIER_GROUP_CHANGED, false);
            int intExtra = intent.getIntExtra(SettingsProvider.EXTRA_CARRIER_PHONEID, 0);
            int carrierId = SemCarrierFeature.getInstance().getCarrierId(intExtra, false);
            IMSLog.i(SettingsProvider.LOG_TAG, intExtra, "intent : action : " + action + " , carrierState : " + stringExtra + ", currentCarrierId : " + carrierId + ", GroupChanged : " + booleanExtra);
            if (SettingsProvider.ACTION_CARRIER_CHANGED.equals(action)) {
                ImsAutoUpdate instance = ImsAutoUpdate.getInstance(SettingsProvider.this.mContext, intExtra);
                if ("UPDATED".equals(stringExtra)) {
                    SettingsProvider.this.doCarrierFeatureUpdate(instance, intExtra, carrierId);
                    if (instance.selectResource(4) != JsonNull.INSTANCE) {
                        ImsRegistry.getWfcEpdgManager().onCarrierUpdate(intent);
                    }
                } else if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(stringExtra)) {
                    if (carrierId != -1) {
                        String currentNWCode = OmcCode.getCurrentNWCode(intExtra);
                        String lastOmcNwCode = OmcCode.getLastOmcNwCode(intExtra);
                        if (lastOmcNwCode.isEmpty()) {
                            lastOmcNwCode = OmcCode.get();
                        }
                        OmcCode.saveLastOmcNwCode(intExtra, currentNWCode);
                        IMSLog.i(SettingsProvider.LOG_TAG, intExtra, "check omcnw changed : currOmcNwCode : " + currentNWCode + " / lastOmcNwCode : " + lastOmcNwCode);
                        if (!currentNWCode.equals(lastOmcNwCode)) {
                            instance.resetLoaded();
                            SettingsProvider.this.resetStoredConfig(true);
                            SettingsProvider.this.mContext.getContentResolver().notifyChange(UriUtil.buildUri("content://com.sec.ims.settings/mnomap_updated", intExtra), (ContentObserver) null);
                        } else {
                            int r0 = SettingsProvider.this.getSavedCarrierId(intExtra);
                            String r1 = SettingsProvider.this.getSavedSwVersion(intExtra);
                            String str = Build.VERSION.INCREMENTAL;
                            boolean isCarrierFeatureChanged = instance.isCarrierFeatureChanged(intExtra);
                            Log.d(SettingsProvider.LOG_TAG, "saved CarrierId : " + r0 + " Current Carrier Id : " + carrierId + " / saved Sw Ver : " + r1 + " current Sw Ver : " + str + " / isCarrierFeatureChanged : " + isCarrierFeatureChanged);
                            if (r0 != carrierId || !str.equals(r1) || isCarrierFeatureChanged) {
                                SettingsProvider.this.doCarrierFeatureUpdate(instance, intExtra, carrierId);
                                if (instance.selectResource(4) != JsonNull.INSTANCE) {
                                    ImsRegistry.getWfcEpdgManager().onCarrierUpdate(intent);
                                }
                            }
                        }
                        SettingsProvider.this.updateOtherVoLTEIconSetting(intExtra);
                    }
                } else if (IccCardConstants.INTENT_VALUE_ICC_READY.equals(stringExtra) && carrierId != -1) {
                    SettingsProvider.this.updateOtherVoLTEIconSetting(intExtra);
                } else if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(stringExtra) || carrierId == -1) {
                    SettingsProvider.this.updateOtherVoLTEIconSetting(intExtra);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext = null;
    private Map<Integer, DeviceConfigManager> mDeviceConfigManager = new ConcurrentHashMap();
    private SimpleEventLog mEventLog;
    private TelephonyManager mTelephonyManager;

    public static final class ImpuRecordTable implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.sec.ims.settings/impu");
        public static final String IMPU = "impu";
        public static final String IMSI = "imsi";
        public static final String TABLE_NAME = "impu";
        public static final String TIMESTAMP = "timestamp";
    }

    public static String getSecretKey() {
        return "3C061A6726A7E3CAF9634D43D93CAC61";
    }

    public String getType(Uri uri) {
        return null;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "match_profile_id", 0);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "profile", 1);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "profile/#", 2);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "profile/reset", 3);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "global", 4);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "global/#", 5);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "global/reset", 6);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "config/reset", 28);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "cscprofile", 7);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "cscprofile/#", 8);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "cscsetting", 9);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "cscsetting/#", 10);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "imsswitch", 11);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "imsswitch/*", 13);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "imsswitchreset", 12);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "simdata", 14);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "nvstorage/*", 15);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "userconfig", 16);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "impu", 17);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "selfprovisioning", 18);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "gcfconfig", 19);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "dnsblock", 20);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "debugconfig/#", 21);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "selfwificallingactivation", 22);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "mno", 23);
        uriMatcher.addURI("com.samsung.rcs.dmconfigurationprovider", "omadm/./3GPP_IMS/*", 24);
        uriMatcher.addURI("com.samsung.rcs.dmconfigurationprovider", NvStorage.ID_OMADM, 24);
        uriMatcher.addURI("com.samsung.rcs.dmconfigurationprovider", SipMsg.EVENT_PRESENCE, 24);
        uriMatcher.addURI("com.samsung.rcs.dmconfigurationprovider", "*", 24);
        uriMatcher.addURI("com.samsung.rcs.dmconfigurationprovider", (String) null, 24);
        uriMatcher.addURI("com.samsung.rcs.dmconfigurationprovider", "omadm/*", 25);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "nvlist", 26);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "ims_info/rcs_ver", 31);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "secretkey", 33);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "downloadconfig", 29);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "resetconfig", 30);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "smkupdatedinfo", 34);
        IntentFilter intentFilter = new IntentFilter();
        SIM_STATE_CHANGED_INTENT_FILTER = intentFilter;
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "simmobility", 32);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "gcfinitrat", 35);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "imsusersetting", 36);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "sms_setting", 37);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "epdgsettings", 38);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "dtlocuserconsent", 39);
        uriMatcher.addURI(ImsConstants.Uris.AUTHORITY, "idcconfig", 40);
    }

    public boolean onCreate() {
        Context context = getContext();
        this.mContext = context;
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 500);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        for (int i = 0; i < this.mTelephonyManager.getPhoneCount(); i++) {
            this.mDeviceConfigManager.put(Integer.valueOf(i), new DeviceConfigManager(this.mContext, i));
        }
        if (this.mTelephonyManager.getPhoneCount() == 0 && isCmcSecondaryDevice()) {
            this.mDeviceConfigManager.put(Integer.valueOf(ImsConstants.Phone.SLOT_1), new DeviceConfigManager(this.mContext, ImsConstants.Phone.SLOT_1));
            Log.d(LOG_TAG, "CMC supported no NOSIM model : DeviceConfigManager");
        }
        this.mContext.registerReceiver(this.mCarrierFeatureReceiver, new IntentFilter(ACTION_CARRIER_CHANGED));
        return true;
    }

    /* access modifiers changed from: private */
    public void doCarrierFeatureUpdate(ImsAutoUpdate imsAutoUpdate, int i, int i2) {
        if (imsAutoUpdate.loadCarrierFeature()) {
            boolean z = (imsAutoUpdate.getMnomap(4, ImsAutoUpdate.TAG_MNOMAP_REMOVE) == JsonNull.INSTANCE && imsAutoUpdate.getMnomap(4, ImsAutoUpdate.TAG_MNOMAP_ADD) == JsonNull.INSTANCE) ? false : true;
            resetStoredConfig(z);
            if (z) {
                this.mContext.getContentResolver().notifyChange(UriUtil.buildUri("content://com.sec.ims.settings/mnomap_updated", i), (ContentObserver) null);
            } else {
                this.mContext.getContentResolver().notifyChange(UriUtil.buildUri("content://com.sec.ims.settings/dynamic_ims_updated", i), (ContentObserver) null);
            }
        }
        saveUpdatedCarrierId(i, i2);
    }

    /* access modifiers changed from: private */
    public int getSavedCarrierId(int i) {
        return ImsSharedPrefHelper.getInt(i, this.mContext, ImsSharedPrefHelper.CARRIER_ID, ImsSharedPrefHelper.CARRIER_ID, -1);
    }

    /* access modifiers changed from: private */
    public String getSavedSwVersion(int i) {
        return ImsSharedPrefHelper.getString(i, this.mContext, ImsSharedPrefHelper.CARRIER_ID, "swversion", "");
    }

    private void saveUpdatedCarrierId(int i, int i2) {
        ImsSharedPrefHelper.save(i, this.mContext, ImsSharedPrefHelper.CARRIER_ID, ImsSharedPrefHelper.CARRIER_ID, i2);
        ImsSharedPrefHelper.save(i, this.mContext, ImsSharedPrefHelper.CARRIER_ID, "swversion", Build.VERSION.INCREMENTAL);
    }

    /* access modifiers changed from: package-private */
    public DeviceConfigManager getDeviceConfigManager(int i) {
        DeviceConfigManager deviceConfigManager = this.mDeviceConfigManager.get(Integer.valueOf(i));
        if (deviceConfigManager == null) {
            IMSLog.d(LOG_TAG, i, "getDeviceConfigManager: Not exist.");
        }
        return deviceConfigManager;
    }

    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> arrayList) throws OperationApplicationException {
        return super.applyBatch(arrayList);
    }

    private ArrayList<String> getAllServiceSwitches() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("volte");
        arrayList.add(DeviceConfigManager.RCS);
        arrayList.add(DeviceConfigManager.IMS);
        arrayList.add(ImsConstants.SystemSettings.VILTE_SLOT1.getName());
        arrayList.add(ImsConstants.SystemSettings.VOLTE_SLOT1.getName());
        arrayList.add(DeviceConfigManager.DEFAULTMSGAPPINUSE);
        arrayList.add("mmtel");
        arrayList.add("mmtel-video");
        arrayList.add("mmtel-call-composer");
        arrayList.add("smsip");
        arrayList.add("ss");
        arrayList.add("cdpn");
        arrayList.add("options");
        arrayList.add(SipMsg.EVENT_PRESENCE);
        arrayList.add("im");
        arrayList.add("ft");
        arrayList.add("ft_http");
        arrayList.add("slm");
        arrayList.add("lastseen");
        arrayList.add("is");
        arrayList.add("vs");
        arrayList.add("euc");
        arrayList.add("gls");
        arrayList.add("profile");
        arrayList.add("ec");
        arrayList.add("cab");
        arrayList.add("cms");
        arrayList.add(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION);
        arrayList.add("datachannel");
        return arrayList;
    }

    private Cursor getSavedImpu(String str) {
        String string = ImsSharedPrefHelper.getString(-1, this.mContext, ImsSharedPrefHelper.SAVED_IMPU, str, "");
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"impu"});
        matrixCursor.addRow(new Object[]{string});
        return matrixCursor;
    }

    private int setSavedImpu(ContentValues contentValues) {
        ImsSharedPrefHelper.save(-1, this.mContext, ImsSharedPrefHelper.SAVED_IMPU, contentValues.getAsString("imsi"), contentValues.getAsString("impu"));
        return 1;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        List<ImsProfile> list;
        String str3;
        Uri uri2 = uri;
        String[] strArr3 = strArr;
        String str4 = str;
        String[] strArr4 = strArr2;
        int simSlotFromUri = UriUtil.getSimSlotFromUri(uri);
        DeviceConfigManager deviceConfigManager = getDeviceConfigManager(simSlotFromUri);
        if (deviceConfigManager == null) {
            return null;
        }
        String str5 = "Uri[" + uri2 + "]";
        String str6 = "#" + uri.getFragment();
        Uri parse = !CollectionUtils.isNullOrEmpty(str6) ? Uri.parse(uri.toString().replace(str6, "")) : uri2;
        if (str4 != null) {
            str5 = str5 + ", sel : " + str4;
        }
        if (strArr3 != null) {
            str5 = str5 + ", pro : " + Arrays.toString(strArr);
        }
        int i = 0;
        if (!IMSLog.isShipBuild()) {
            dumpBinderInfo(LOG_QUERY, false, str5, false);
        }
        int match = sUriMatcher.match(parse);
        if (match == 1) {
            MatrixCursor matrixCursor = new MatrixCursor(new String[]{"profile"});
            if (!TextUtils.isEmpty(str)) {
                String substring = str4.substring(str4.indexOf(AuthenticationHeaders.HEADER_PRARAM_SPERATOR) + 1);
                IMSLog.d(LOG_TAG, simSlotFromUri, "ImsProfile query with  " + substring);
                if (str4.startsWith("mdmn_type")) {
                    list = deviceConfigManager.getProfileCache().getProfileListByMdmnType(substring);
                } else if (str4.startsWith("salescode")) {
                    list = new ArrayList<>();
                } else if (str4.startsWith(MnoMapJsonParser.Param.MCCMNC)) {
                    list = new ArrayList<>();
                } else if (str4.startsWith("mnoname")) {
                    list = deviceConfigManager.getProfileCache().getProfileListByMnoName(substring, deviceConfigManager.getGlobalSettingsRepo().getGlobalGcEnabled());
                } else {
                    list = new ArrayList<>();
                }
            } else {
                list = deviceConfigManager.getProfileCache().getAllProfileList();
            }
            for (ImsProfile json : list) {
                matrixCursor.newRow().add("profile", json.toJson());
            }
            return matrixCursor;
        } else if (match == 2) {
            MatrixCursor matrixCursor2 = new MatrixCursor(new String[]{"profile"});
            ImsProfile profile = deviceConfigManager.getProfileCache().getProfile(Integer.parseInt(parse.getLastPathSegment()));
            if (profile != null) {
                matrixCursor2.newRow().add("profile", profile.toJson());
            }
            return matrixCursor2;
        } else if (match == 4) {
            return deviceConfigManager.getGlobalSettingsRepo().query(strArr3, str4, strArr4);
        } else {
            if (match == 11 || match == 13) {
                return deviceConfigManager.queryImsSwitch(strArr3 == null ? (String[]) getAllServiceSwitches().toArray(new String[0]) : strArr3);
            } else if (match == 19) {
                return deviceConfigManager.queryGcfConfig();
            } else {
                if (match == 21) {
                    return deviceConfigManager.getDebugConfigStorage().query(simSlotFromUri, strArr3);
                }
                if (match == 39) {
                    int i2 = ImsSharedPrefHelper.getSharedPref(-1, this.mContext, "dtlocuserconsent", 0, false).getInt("dtlocation", -1);
                    MatrixCursor matrixCursor3 = new MatrixCursor(new String[]{"dtlocation"});
                    matrixCursor3.newRow().add(Integer.valueOf(i2));
                    return matrixCursor3;
                } else if (match == 40) {
                    return queryIdcConfig();
                } else {
                    switch (match) {
                        case 15:
                            break;
                        case 16:
                            return deviceConfigManager.getUserConfigStorage().query(strArr3);
                        case 17:
                            return getSavedImpu(strArr4[0]);
                        default:
                            switch (match) {
                                case 23:
                                    MatrixCursor matrixCursor4 = new MatrixCursor(new String[]{"mnoname", ISimManager.KEY_MVNO_NAME, ISimManager.KEY_HAS_SIM});
                                    matrixCursor4.newRow().add("mnoname", deviceConfigManager.getMnoName()).add(ISimManager.KEY_MVNO_NAME, deviceConfigManager.getMvnoName()).add(ISimManager.KEY_HAS_SIM, Boolean.valueOf(deviceConfigManager.getHasSim()));
                                    return matrixCursor4;
                                case 24:
                                    break;
                                case 25:
                                    if (parse.getLastPathSegment().equals("*")) {
                                        return deviceConfigManager.queryDm(parse, strArr, str, strArr2, true);
                                    }
                                    break;
                                case 26:
                                    MatrixCursor matrixCursor5 = new MatrixCursor(new String[]{"NVLIST"});
                                    if (deviceConfigManager.getNvList() != null && !deviceConfigManager.getNvList().isEmpty()) {
                                        matrixCursor5.addRow(new String[]{Arrays.toString(deviceConfigManager.getNvList().toArray())});
                                    }
                                    return matrixCursor5;
                                default:
                                    switch (match) {
                                        case 31:
                                            MatrixCursor matrixCursor6 = new MatrixCursor(new String[]{"rcs_ver"});
                                            matrixCursor6.newRow().add(RCS_VERSION);
                                            return matrixCursor6;
                                        case 32:
                                            MatrixCursor matrixCursor7 = new MatrixCursor(new String[]{"simmobility"});
                                            MatrixCursor.RowBuilder newRow = matrixCursor7.newRow();
                                            if (SlotBasedConfig.getInstance(simSlotFromUri).isSimMobilityActivated()) {
                                                i = 1;
                                            }
                                            newRow.add(Integer.valueOf(i));
                                            return matrixCursor7;
                                        case 33:
                                            MatrixCursor matrixCursor8 = new MatrixCursor(new String[]{"secretkey"});
                                            matrixCursor8.newRow().add(getSecretKey());
                                            return matrixCursor8;
                                        case 34:
                                            MatrixCursor matrixCursor9 = new MatrixCursor(new String[]{"smkupdatedinfo"});
                                            try {
                                                str3 = ImsAutoUpdate.getInstance(this.mContext, simSlotFromUri).getSmkConfig().toString();
                                            } catch (Exception e) {
                                                Log.d(LOG_TAG, "failed to get SMK Updated Information : " + e);
                                                str3 = null;
                                            }
                                            if (str3 != null && str3.length() > 0) {
                                                Log.d(LOG_TAG, "updated info return, query to ImsSettings");
                                                matrixCursor9.newRow().add(str3);
                                            }
                                            return matrixCursor9;
                                        case 35:
                                            String string = ImsSharedPrefHelper.getSharedPref(-1, this.mContext, "gcf_init_rat", 0, false).getString("rat", "");
                                            MatrixCursor matrixCursor10 = new MatrixCursor(new String[]{"rat"});
                                            matrixCursor10.newRow().add(string);
                                            return matrixCursor10;
                                        case 36:
                                            return deviceConfigManager.queryImsUserSetting(strArr3);
                                        case 37:
                                            return deviceConfigManager.getSmsSetting().getAsCursor();
                                        default:
                                            throw new IllegalArgumentException("Unknown URI " + uri2);
                                    }
                            }
                    }
                    return deviceConfigManager.queryDm(parse, strArr, str, strArr2, false);
                }
            }
        }
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        long j;
        int simSlotFromUri = UriUtil.getSimSlotFromUri(uri);
        DeviceConfigManager deviceConfigManager = getDeviceConfigManager(simSlotFromUri);
        if (deviceConfigManager == null || contentValues == null) {
            return null;
        }
        dumpBinderInfo(LOG_INSERT, true, "Uri[" + uri + "], value : " + contentValues.toString(), true);
        if (PackageUtils.getProcessNameById(this.mContext, Binder.getCallingPid()).endsWith(":CloudMessageService")) {
            this.mEventLog.logAndAdd(simSlotFromUri, "insert: Ignore the call from CMS process");
            return null;
        }
        String str = "#" + uri.getFragment();
        Uri parse = !CollectionUtils.isNullOrEmpty(str) ? Uri.parse(uri.toString().replace(str, "")) : uri;
        int match = sUriMatcher.match(parse);
        if (match != 1) {
            if (match != 21) {
                if (match != 24) {
                    if (match != 38) {
                        switch (match) {
                            case 15:
                                break;
                            case 16:
                                deviceConfigManager.getUserConfigStorage().insert(contentValues);
                                break;
                            case 17:
                                setSavedImpu(contentValues);
                                break;
                            default:
                                throw new IllegalArgumentException("Unknown URI " + uri);
                        }
                    } else {
                        updateEpdgSystemSettings(contentValues);
                    }
                }
                sendData(simSlotFromUri, contentValues);
                deviceConfigManager.insertDm(parse, contentValues);
            } else {
                deviceConfigManager.getDebugConfigStorage().insert(simSlotFromUri, contentValues);
            }
            j = 0;
        } else {
            j = (long) deviceConfigManager.getProfileCache().insert(new ImsProfile(contentValues));
        }
        getContext().getContentResolver().notifyChange(uri, (ContentObserver) null);
        return Uri.withAppendedPath(uri, Long.toString(j));
    }

    public int delete(Uri uri, String str, String[] strArr) {
        int simSlotFromUri = UriUtil.getSimSlotFromUri(uri);
        DeviceConfigManager deviceConfigManager = getDeviceConfigManager(simSlotFromUri);
        if (deviceConfigManager == null) {
            return 0;
        }
        dumpBinderInfo(LOG_DELETE, true, uri.toString(), true);
        String str2 = "#" + uri.getFragment();
        Uri parse = !CollectionUtils.isNullOrEmpty(str2) ? Uri.parse(uri.toString().replace(str2, "")) : uri;
        int match = sUriMatcher.match(parse);
        if (match == 2) {
            deviceConfigManager.getProfileCache().remove(Integer.valueOf(parse.getLastPathSegment()).intValue());
            return 0;
        } else if (match == 15 || match == 24) {
            return deviceConfigManager.deleteDm(parse);
        } else {
            if (match == 30) {
                ImsAutoUpdate.getInstance(this.mContext, simSlotFromUri).clearSmkConfig();
                resetStoredConfig(false);
                getContext().getContentResolver().notifyChange(uri, (ContentObserver) null);
                return 0;
            }
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        boolean z;
        int i;
        int simSlotFromUri = UriUtil.getSimSlotFromUri(uri);
        DeviceConfigManager deviceConfigManager = getDeviceConfigManager(simSlotFromUri);
        boolean z2 = false;
        if (deviceConfigManager == null) {
            return 0;
        }
        z = true;
        dumpBinderInfo(LOG_UPDATE, true, "Uri[" + uri + "], value : " + contentValues.toString(), true);
        if (PackageUtils.getProcessNameById(this.mContext, Binder.getCallingPid()).endsWith(":CloudMessageService")) {
            this.mEventLog.logAndAdd(simSlotFromUri, "update: Ignore the call from CMS process");
            return 0;
        }
        String str2 = "#" + uri.getFragment();
        Uri parse = !CollectionUtils.isNullOrEmpty(str2) ? Uri.parse(uri.toString().replace(str2, "")) : uri;
        switch (sUriMatcher.match(parse)) {
            case 1:
                IMSLog.e(LOG_TAG, simSlotFromUri, "update: Bulk edit not supported.");
                return 0;
            case 2:
                int intValue = Integer.valueOf(parse.getLastPathSegment()).intValue();
                Mno fromName = Mno.fromName(deviceConfigManager.getGlobalSettingsRepo().getPreviousMno());
                int badEventExpiry = deviceConfigManager.getProfileCache().getProfile(intValue).getBadEventExpiry();
                int extendedPublishTimer = deviceConfigManager.getProfileCache().getProfile(intValue).getExtendedPublishTimer();
                i = deviceConfigManager.getProfileCache().update(intValue, contentValues);
                if (fromName == Mno.ATT && !(badEventExpiry == deviceConfigManager.getProfileCache().getProfile(intValue).getBadEventExpiry() && extendedPublishTimer == deviceConfigManager.getProfileCache().getProfile(intValue).getExtendedPublishTimer())) {
                    IMSLog.d(LOG_TAG, simSlotFromUri, "update : badEventExpiry or badEventExpiry for ATT");
                    return i;
                }
            case 3:
                deviceConfigManager.restoreDefaultImsProfile();
                break;
            case 4:
            case 5:
                deviceConfigManager.getGlobalSettingsRepo().update(contentValues);
                break;
            case 6:
                IMSLog.d(LOG_TAG, simSlotFromUri, "update: reset.");
                deviceConfigManager.getGlobalSettingsRepo().reset();
                deviceConfigManager.getGlobalSettingsRepo().load();
                break;
            case 11:
            case 13:
                try {
                    String asString = contentValues.getAsString("service");
                    boolean booleanValue = contentValues.getAsBoolean("enabled").booleanValue();
                    z = true ^ "ipme".equalsIgnoreCase(asString);
                    deviceConfigManager.enableImsSwitch(asString, booleanValue);
                    break;
                } catch (NullPointerException unused) {
                    IMSLog.d(LOG_TAG, simSlotFromUri, "IMS_SWITCH - NullPointerException");
                    break;
                }
            case 12:
                deviceConfigManager.resetImsSwitch();
                break;
            case 15:
                Log.d(LOG_TAG, "update: not supported in NV_STORAGE. use insert");
                break;
            case 18:
                deviceConfigManager.updateProvisioningProperty(contentValues);
                break;
            case 19:
                deviceConfigManager.updateGcfConfig(contentValues);
                break;
            case 20:
                deviceConfigManager.updateDnsBlock(contentValues);
                break;
            case 22:
                deviceConfigManager.updateWificallingProperty(contentValues);
                break;
            case 23:
                deviceConfigManager.updateMno(contentValues);
                break;
            case 24:
                sendData(simSlotFromUri, contentValues);
                deviceConfigManager.updateDm(parse, contentValues);
                break;
            case 28:
                new StorageAdapter().forceDeleteALL(this.mContext);
                break;
            case 29:
                IMSLog.c(LogClass.SMK_UPDATE, simSlotFromUri + ",SMK UPDATE");
                ImsAutoUpdate instance = ImsAutoUpdate.getInstance(this.mContext, simSlotFromUri);
                instance.updateSmkConfig(contentValues.getAsString(ImsConstants.DOWNLOAD_CONFIG));
                boolean z3 = (instance.getMnomap(0, ImsAutoUpdate.TAG_MNOMAP_REMOVE) == JsonNull.INSTANCE && instance.getMnomap(0, ImsAutoUpdate.TAG_MNOMAP_ADD) == JsonNull.INSTANCE) ? false : true;
                resetStoredConfig(z3);
                if (z3) {
                    getContext().getContentResolver().notifyChange(Uri.parse("content://com.sec.ims.settings/mnomap_updated"), (ContentObserver) null);
                    i = 0;
                    break;
                }
                break;
            case 35:
                deviceConfigManager.updateGcfInitRat(contentValues);
                break;
            case 36:
                try {
                    deviceConfigManager.setImsUserSetting(contentValues.getAsString("name"), contentValues.getAsInteger("value").intValue());
                    break;
                } catch (NullPointerException unused2) {
                    IMSLog.d(LOG_TAG, simSlotFromUri, "IMS_USER_SETTING - NullPointerException");
                    break;
                }
            case 39:
                deviceConfigManager.updateDtLocUserConsent(contentValues);
                break;
            case 40:
                updateIdcConfig(contentValues);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        i = 0;
        z2 = z;
        if (z2) {
            Log.d(LOG_TAG, "notifyChange uri [" + uri + "]");
            getContext().getContentResolver().notifyChange(uri, (ContentObserver) null);
        }
        return i;
    }

    public Bundle call(String str, String str2, Bundle bundle) {
        if ("dump".equals(str)) {
            dump((FileDescriptor) null, (PrintWriter) null, (String[]) null);
        }
        return null;
    }

    private void reset(int i) {
        Optional.ofNullable(getDeviceConfigManager(i)).ifPresent(new SettingsProvider$$ExternalSyntheticLambda0());
        RcsPolicyManager.loadRcsSettings(i, true);
        this.mContext.getContentResolver().notifyChange(UriUtil.buildUri(GlobalSettingsConstants.CONTENT_URI.toString(), i), (ContentObserver) null);
        this.mContext.getContentResolver().notifyChange(UriUtil.buildUri(ImsConstants.Uris.SMS_SETTING.toString(), i), (ContentObserver) null);
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ void lambda$reset$0(DeviceConfigManager deviceConfigManager) {
        deviceConfigManager.restoreDefaultImsProfile();
        deviceConfigManager.updateImsSwitchByDynamicUpdate();
        deviceConfigManager.getGlobalSettingsRepo().reset();
        deviceConfigManager.getGlobalSettingsRepo().loadByDynamicConfig();
        deviceConfigManager.getSmsSetting().init();
    }

    /* access modifiers changed from: private */
    public void resetStoredConfig(boolean z) {
        if (z) {
            for (int i = 0; i < this.mDeviceConfigManager.size(); i++) {
                SharedPreferences sharedPref = ImsSharedPrefHelper.getSharedPref(i, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false);
                if (sharedPref != null) {
                    SharedPreferences.Editor edit = sharedPref.edit();
                    edit.putString("mnoname", "default");
                    edit.apply();
                }
            }
        }
        for (int i2 = 0; i2 < this.mDeviceConfigManager.size(); i2++) {
            reset(i2);
        }
    }

    private void dumpBinderInfo(String str, boolean z, String str2, boolean z2) {
        String str3;
        int callingPid = Binder.getCallingPid();
        String str4 = str + "(" + callingPid;
        if (z2) {
            String processNameById = PackageUtils.getProcessNameById(getContext(), callingPid);
            str3 = str4 + ", " + processNameById.substring(processNameById.lastIndexOf(".") + 1) + ") : ";
        } else {
            str3 = str4 + ") : ";
        }
        if (IMSLog.isShipBuild()) {
            if (str2.contains("impu") || str2.contains("imsi")) {
                str2 = str2.replaceAll("\\d", "x");
            } else {
                str2 = str2.replaceAll("\\d++@", "xxx@");
            }
        }
        String str5 = str3 + str2;
        if (z) {
            this.mEventLog.logAndAdd(str5);
        } else {
            Log.d(LOG_TAG, str5);
        }
    }

    private void sendData(int i, ContentValues contentValues) {
        String str;
        int i2;
        if (!CollectionUtils.isNullOrEmpty(contentValues)) {
            ContentValues contentValues2 = new ContentValues();
            StringBuilder sb = new StringBuilder();
            if (TextUtils.isEmpty(contentValues.getAsString(DmConfigModule.INTERNAL_KEY_PROCESS_NAME))) {
                str = PackageUtils.getProcessNameById(getContext(), Binder.getCallingPid());
            } else {
                str = contentValues.getAsString(DmConfigModule.INTERNAL_KEY_PROCESS_NAME);
            }
            contentValues.remove(DmConfigModule.INTERNAL_KEY_PROCESS_NAME);
            IMSLog.c(LogClass.OMADM_UPDATER_AND_SIZE, i + ",UPD:" + str + "," + contentValues.size());
            for (String next : contentValues.keySet()) {
                if (!next.contains(DeviceConfigManager.NV_INIT_DONE)) {
                    Iterator it = DATA.DM_FIELD_LIST.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            i2 = -1;
                            break;
                        }
                        DATA.DM_FIELD_INFO dm_field_info = (DATA.DM_FIELD_INFO) it.next();
                        if (dm_field_info.getPathName().contains(next)) {
                            i2 = dm_field_info.getIndex();
                            break;
                        }
                    }
                    String asString = contentValues.getAsString(next);
                    if (i2 != -1 || !TextUtils.isEmpty(asString)) {
                        if (i2 >= 0) {
                            sb.append(i2 + ":");
                        } else {
                            Log.e(LOG_TAG, "xNode item: " + next);
                            sb.append("X:");
                        }
                        if (!TextUtils.isEmpty(asString)) {
                            if (asString.contains(":") || asString.contains(".")) {
                                IMSLog.s(LOG_TAG, "Replace sensitive data: " + asString);
                                asString = "HIDE";
                            }
                            sb.append(asString);
                        }
                        sb.append("^");
                        IMSLog.c(LogClass.OMADM_UPDATED_ITEM, i + "," + asString + "," + getShortenKeyForXNode(next));
                    } else {
                        Log.e(LOG_TAG, "Ignore: " + next + ": [" + asString + "]");
                    }
                }
            }
            if (!TextUtils.isEmpty(sb)) {
                sb.deleteCharAt(sb.length() - 1);
                sb.insert(0, contentValues.size() + "^");
                contentValues2.put(DiagnosisConstants.DMUI_KEY_SETTING_TYPE, sb.toString());
                contentValues2.put(DiagnosisConstants.DMUI_KEY_CALLER_INFO, str);
                IMSLog.s(LOG_TAG, "sendData : " + contentValues2);
                ImsLogAgentUtil.sendLogToAgent(i, this.mContext, DiagnosisConstants.FEATURE_DMUI, contentValues2);
            }
        }
    }

    private String getShortenKeyForXNode(String str) {
        String[] split = str.replaceFirst("\\./3GPP_IMS/", "").split("/");
        StringBuilder sb = new StringBuilder();
        int length = split.length;
        if (length >= 2) {
            sb.append(split[length - 2]);
            sb.append("/");
            sb.append(split[length - 1]);
        } else if (length > 0) {
            sb.append(split[0]);
        }
        return sb.toString();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        this.mEventLog.dump();
        this.mDeviceConfigManager.values().forEach(new SettingsProvider$$ExternalSyntheticLambda2());
    }

    private boolean isCmcSecondaryDevice() {
        CmcSettingManager cmcSettingManager = new CmcSettingManager();
        cmcSettingManager.init(this.mContext);
        CmcSettingManagerConstants.DeviceType ownDeviceType = cmcSettingManager.getOwnDeviceType();
        cmcSettingManager.deInit();
        IMSLog.d(LOG_TAG, "onCreate: isCmcSecondaryDevice: api: " + ownDeviceType);
        if (ownDeviceType == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD) {
            return true;
        }
        if (ownDeviceType == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD) {
            return false;
        }
        String str = SemSystemProperties.get(CmcConstants.SystemProperties.CMC_DEVICE_TYPE_PROP, "");
        if (!TextUtils.isEmpty(str)) {
            IMSLog.d(LOG_TAG, "onCreate: isCmcSecondaryDevice: prop: " + str);
            if ("sd".equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    private void updateEpdgSystemSettings(ContentValues contentValues) {
        for (Map.Entry next : contentValues.valueSet()) {
            String str = (String) next.getKey();
            int i = -1;
            int parseInt = str.matches(".+[1-9]$") ? Integer.parseInt(str.substring(str.length() - 1)) : -1;
            Object value = next.getValue();
            if (value instanceof Integer) {
                i = ((Integer) value).intValue();
            } else if (value instanceof String) {
                try {
                    i = Integer.parseInt((String) value);
                } catch (NumberFormatException unused) {
                }
            }
            if (parseInt < 0 || i < 0) {
                Log.e(LOG_TAG, "updateEpdgSystemSettings: Skip wrong input [" + str + ": " + ((String) Optional.ofNullable(value).map(new SettingsProvider$$ExternalSyntheticLambda1()).orElse("null!")) + "] => lastChar [" + parseInt + "], val [" + i + "]");
            } else if (str.replace(VowifiConfig.WIFI_CALL_ENABLE, "").length() == 1) {
                ImsConstants.SystemSettings.setWiFiCallEnabled(this.mContext, parseInt - 1, i);
            } else if (str.replace(VowifiConfig.WIFI_CALL_PREFERRED, "").length() == 1) {
                ImsConstants.SystemSettings.setWiFiCallPreferred(this.mContext, parseInt - 1, i);
            } else if (str.replace(VowifiConfig.WIFI_CALL_WHEN_ROAMING, "").length() == 1) {
                ImsConstants.SystemSettings.setWiFiCallWhenRoaming(this.mContext, parseInt - 1, i);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateOtherVoLTEIconSetting(int i) {
        int i2 = i == 0 ? 1 : 0;
        if (SlotBasedConfig.getInstance(i2).getIconManager() != null) {
            SlotBasedConfig.getInstance(i2).getIconManager().updateRegistrationIcon();
        }
    }

    private Cursor queryIdcConfig() {
        String string = ImsSharedPrefHelper.getString(-1, this.mContext, ImsSharedPrefHelper.SAVED_IDC_PROCESS_MODE, "IDC_APPDATA_PROCESS_MODE_PREF", "file");
        MatrixCursor matrixCursor = new MatrixCursor(ImsSettings.ImsUserSettingTable.PROJECTION);
        matrixCursor.addRow(new Object[]{"IDC_APPDATA_PROCESS_MODE", String.valueOf(string)});
        return matrixCursor;
    }

    private void updateIdcConfig(ContentValues contentValues) {
        if (contentValues != null && contentValues.size() != 0) {
            String asString = contentValues.getAsString("IDC_APPDATA_PROCESS_MODE");
            if (asString == null) {
                IMSLog.d(LOG_TAG, "IDC_APPDATA_PROCESS_MODE is null");
            } else {
                ImsSharedPrefHelper.save(-1, this.mContext, ImsSharedPrefHelper.SAVED_IDC_PROCESS_MODE, "IDC_APPDATA_PROCESS_MODE_PREF", asString);
            }
        }
    }
}
