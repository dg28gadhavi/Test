package com.sec.internal.ims.entitlement.nsds.ericssonnsds.persist;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper;
import com.sec.internal.ims.entitlement.storagehelper.MigrationHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;

public class NSDSContentProvider extends ContentProvider {
    private static final String ACCESS_STEERING_TABLE = "access_steering";
    private static final String ACCOUNTS_TABLE = "accounts";
    private static final String CONNECTIVITY_PARAMS_TABLE = "connectivity_parameters";
    private static final String CONNECTIVITY_SERVICE_NAME_TABLE = "connectivity_sevice_names";
    private static final String CREATE_ACCOUNT_TABLE = "CREATE TABLE IF NOT EXISTS accounts(_id INTEGER PRIMARY KEY AUTOINCREMENT,account_id TEXT NOT NULL,device_uid TEXT, email TEXT,access_token TEXT,is_active INTEGER DEFAULT 0,is_temporary INTEGER DEFAULT 0, UNIQUE(account_id));";
    private static final String CREATE_CONNECTIVITY_PARAMS_TABLE = "CREATE TABLE IF NOT EXISTS connectivity_parameters(_id INTEGER PRIMARY KEY AUTOINCREMENT,certificate TEXT,epdg_addresses TEXT);";
    private static final String CREATE_CONNECTIVITY_PARAM_SERVICE_NAME_TABLE = "CREATE TABLE IF NOT EXISTS connectivity_sevice_names(_id INTEGER PRIMARY KEY AUTOINCREMENT,connectivity_id INTEGER REFERENCES connectivity_parameters(_id) NOT NULL, service_name TEXT NOT NULL,client_id TEXT NOT NULL,package_name TEXT NOT NULL,appstore_url TEXT NOT NULL);";
    private static final String CREATE_DEVICES_TABLE = "CREATE TABLE IF NOT EXISTS devices(_id INTEGER PRIMARY KEY AUTOINCREMENT,device_uid TEXT NOT NULL,device_account_id INTEGER REFERENCES accounts(_id) NOT NULL,device_name TEXT NOT NULL,is_primary INTEGER DEFAULT 0,device_type INTEGER DEFAULT 0,is_local INTEGER DEFAULT 0, UNIQUE(device_account_id,device_uid));";
    private static final String CREATE_DEVICE_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS device_config(_id INTEGER PRIMARY KEY AUTOINCREMENT,device_id TEXT NOT NULL, version TEXT, device_config TEXT);";
    private static final String CREATE_GCM_TOKENS_TABLE = "CREATE TABLE IF NOT EXISTS gcm_tokens(_id INTEGER PRIMARY KEY AUTOINCREMENT,sender_id TEXT NOT NULL,gcm_token TEXT NOT NULL,protocol_to_server TEXT, device_uid TEXT, UNIQUE( sender_id));";
    private static final String CREATE_LINES_TABLE = "CREATE TABLE IF NOT EXISTS lines(_id INTEGER PRIMARY KEY AUTOINCREMENT,account_id INTEGER REFERENCES accounts(_id) NOT NULL,msisdn TEXT NOT NULL,friendly_name TEXT NOT NULL,status INTEGER DEFAULT 0,line_res_package TEXT, icon INTEGER,color INTEGER,type TEXT DEFAULT regular,is_owner INTEGER DEFAULT 1,service_attributes TEXT, is_device_default INTEGER DEFAULT 0, location_status INTEGER , tc_status INTEGER , e911_address_id TEXT, e911_aid_expiration TEXT, e911_server_data TEXT, e911_server_url TEXT, cab_status INTEGER DEFAULT 0, reg_status INTEGER DEFAULT 0, ring_tone TEXT, UNIQUE( account_id,msisdn));";
    private static final String CREATE_NSDS_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS nsds_configs(_id INTEGER PRIMARY KEY AUTOINCREMENT,imsi TEXT, pname TEXT NOT NULL,pvalue TEXT);";
    private static final String CREATE_PROVISIONING_PARAMS_TABLE = "CREATE TABLE IF NOT EXISTS provisioning_parameters(_id INTEGER PRIMARY KEY AUTOINCREMENT,apn TEXT NOT NULL,pcscf_address TEXT NOT NULL,sip_uri TEXT NOT NULL,impu TEXT NOT NULL,sip_username TEXT,sip_password TEXT NOT NULL);";
    private static final String CREATE_SERVICES_TABLE = "CREATE TABLE IF NOT EXISTS services(_id INTEGER PRIMARY KEY AUTOINCREMENT,line_id INTEGER REFERENCES lines(_id),device_id INTEGER REFERENCES devices(_id),is_native INTEGER DEFAULT 0,service_name TEXT,service_instance_id TEXT,expiration_time INTEGER DEFAULT 0,service_msisdn TEXT,is_owner INTEGER,msisdn_friendly_name TEXT,service_fingerprint TEXT DEFAULT NULL,service_instance_token TEXT, service_token_expire_time TEXT, provisioning_params_id INTEGER REFERENCES provisioning_parameters(_id),config_parameters TEXT);";
    private static final String CREATE_SIM_SWAP_NSDS_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS sim_swap_nsds_configs(_id INTEGER PRIMARY KEY AUTOINCREMENT,imsi TEXT NOT NULL, pname TEXT NOT NULL,pvalue TEXT);";
    private static final String CREATE_SIM_SWAP_SERVICES_TABLE = "CREATE TABLE IF NOT EXISTS sim_swap_services(_id INTEGER PRIMARY KEY AUTOINCREMENT,line_id INTEGER REFERENCES lines(_id),device_id INTEGER REFERENCES devices(_id),is_native INTEGER ,service_name TEXT,service_instance_id TEXT,expiration_time INTEGER DEFAULT 0,service_msisdn TEXT,is_owner INTEGER,msisdn_friendly_name TEXT,service_fingerprint TEXT DEFAULT NULL,service_instance_token TEXT, service_token_expire_time TEXT, provisioning_params_id INTEGER REFERENCES provisioning_parameters(_id),config_parameters TEXT);";
    private static final String DATABASE_NAME = "ericsson_nsds.db";
    private static final int DATABASE_VERSION = 3;
    private static final String DEVICES_TABLE = "devices";
    private static final String DEVICE_CONFIG_TABLE = "device_config";
    private static final String GCM_TOKENS_TABLE = "gcm_tokens";
    private static final String LINES_TABLE = "lines";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = NSDSContentProvider.class.getSimpleName();
    private static final String NSDS_CONFIG_TABLE = "nsds_configs";
    private static final String PENDING_LINES_TABLE = "pending_lines";
    private static final String PROVIDER_NAME = "com.samsung.ims.nsds.provider";
    private static final String PROVISIONING_PARAMS_TABLE = "provisioning_parameters";
    private static final String REMOVE_ALL_TABLES_AND_INDICES = "PRAGMA writable_schema = 1; DELETE FROM sqlite_master WHERE TYPE IN ('table', 'index'); PRAGMA writable_schema = 0; ";
    private static final String SERVICES_TABLE = "services";
    private static final String SIM_SWAP_NSDS_CONFIG_TABLE = "sim_swap_nsds_configs";
    private static final String SIM_SWAP_SERVICES_TABLE = "sim_swap_services";
    private static final String SQL_WHERE_ACCOUNT_ID = "_id = ?";
    private static final String SQL_WHERE_ACTIVE_ACCOUNT = "is_active = 1";
    private static final String SQL_WHERE_ALL_LINES = " (lines.account_id != 0 OR is_native = 1)";
    private static final String SQL_WHERE_DEVICES_FOR_LINE_ID = "devices._id IN(SELECT services.device_id from lines, devices, services  where device_id = devices._id AND line_id = lines._id AND status = 1 AND line_id = ?)";
    private static final String SQL_WHERE_DEVICE_ID = "_id = ?";
    private static final String SQL_WHERE_LINES_FOR_ACTIVE_ACCOUNT = "services.line_id = lines._id AND services.device_id = devices._id AND lines.account_id != -1 AND devices.device_uid = ? AND  (lines.account_id = 0 OR lines.account_id = (SELECT _id from accounts where is_active = 1))";
    private static final String SQL_WHERE_LINE_ENTITIY_BASE = "line_id = lines._id AND device_id = devices._id";
    private static final String SQL_WHERE_LINE_ENTITIY_ID = "line_id = lines._id AND device_id = devices._id AND lines._id= ?";
    private static final String SQL_WHERE_LINE_ENTITY_ACTIVE_ACCOUNT = "line_id = lines._id AND device_id = devices._id AND services.line_id = lines._id AND services.device_id = devices._id AND lines.account_id != -1 AND devices.device_uid = ? AND  (lines.account_id = 0 OR lines.account_id = (SELECT _id from accounts where is_active = 1))";
    private static final String SQL_WHERE_LINE_ID = "lines._id = ?";
    private static final String SQL_WHERE_LINE_STATUS_ACTIVE = "services.line_id = lines._id AND services.device_id = devices._id AND lines.account_id != -1 AND devices.device_uid = ? AND  (lines.account_id = 0 OR lines.account_id = (SELECT _id from accounts where is_active = 1)) AND status = ?";
    private static final String SQL_WHERE_LOCAL_LINES_WITH_SERVICES = "services.line_id = lines._id AND services.device_id = devices._id AND lines.account_id != -1 AND devices.device_uid = ? AND status = ?";
    private static final String SQL_WHERE_LOCAL_LINES_WITH_SERVICES_BASE = "services.line_id = lines._id AND services.device_id = devices._id AND lines.account_id != -1 AND devices.device_uid = ?";
    public static final String TABLE_JOIN_FOR_ALL_LINES = "lines LEFT OUTER JOIN services on services.line_id = lines._id ";
    public static final String TABLE_JOIN_LINES_SERVICES = "lines, services,devices";
    private static final HashMap<String, String> sLineEntityProjectionMap;
    private static final HashMap<String, String> sLineWithServicesProjectionMap;
    private static final UriMatcher sUriMatcher;
    protected Context mContext = null;
    protected DatabaseHelper mDatabaseHelper = null;
    protected Messenger mNsdsService;
    protected ServiceConnection mNsdsSvcConn;

    private interface LineEntityQuery {
        public static final String TABLE = "lines, devices, services";
    }

    private interface LinesColumns {
        public static final String ACCOUNT_ID = "lines.account_id";
        public static final String CONCRETE_ID = "lines._id";
        public static final String IS_NATIVE = "services.is_native";
        public static final String IS_OWNER = "lines.is_owner";
    }

    public String getType(Uri uri) {
        return null;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "lines/#/enable_cab", 49);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "lines/#/disable_cab", 50);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", LINES_TABLE, 0);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "lines/#/devices", 43);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/#/lines/#/services", 6);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", DEVICES_TABLE, 2);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/#/lines/#/add_services", 17);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/#/lines/#/remove_services", 18);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/#/lines/#/acitvate_services", 19);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/#/lines/#/deactivate_services", 20);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "accounts/disable_active_account", 48);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "activate_sim_device", 30);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "deactivate_sim_device", 31);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "update_e911_address", 46);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "vowifi_toggle_on", 32);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "vowifi_toggle_off", 33);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "accounts/upload_all_contacts", 23);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "accounts/download_all_contacts", 24);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "accounts/upload_updated_contact/#", 25);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/#/set_primary", 26);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/own_activation_status", 28);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/own_login_status", 41);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/own_ready_status", 60);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/own_nsds_service_status", 61);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/push_token", 67);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "device_config", 39);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "device_config/element", 62);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", NSDS_CONFIG_TABLE, 40);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "nsds_configs/entitlement_url", 73);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "devices/#/services", 42);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "all_lines_in_current_account", 44);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "all_lines", 45);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "all_lines_internal", 77);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "services", 63);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", SIM_SWAP_NSDS_CONFIG_TABLE, 71);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", SIM_SWAP_SERVICES_TABLE, 72);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", GCM_TOKENS_TABLE, 74);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "retrieve_aka_token", 80);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "reconnect_db", 81);
        uriMatcher.addURI("com.samsung.ims.nsds.provider", "binding_service", 82);
        HashMap<String, String> hashMap = new HashMap<>();
        sLineWithServicesProjectionMap = hashMap;
        hashMap.put("_id", LinesColumns.CONCRETE_ID);
        hashMap.put("account_id", LinesColumns.ACCOUNT_ID);
        hashMap.put("msisdn", "msisdn");
        hashMap.put(NSDSContractExt.LineColumns.FRIENDLY_NAME, NSDSContractExt.LineColumns.FRIENDLY_NAME);
        hashMap.put("type", "type");
        hashMap.put("status", "status");
        hashMap.put(NSDSContractExt.LineColumns.LINE_RES_PACKAGE, NSDSContractExt.LineColumns.LINE_RES_PACKAGE);
        hashMap.put("icon", "icon");
        hashMap.put(NSDSContractExt.LineColumns.COLOR, NSDSContractExt.LineColumns.COLOR);
        hashMap.put("is_native", LinesColumns.IS_NATIVE);
        hashMap.put("is_owner", LinesColumns.IS_OWNER);
        hashMap.put("is_native", "is_native");
        hashMap.put(NSDSContractExt.LineColumns.SERVICE_ATTRIBUTES, NSDSContractExt.LineColumns.SERVICE_ATTRIBUTES);
        hashMap.put(NSDSContractExt.LineColumns.IS_DEVICE_DEFAULT, NSDSContractExt.LineColumns.IS_DEVICE_DEFAULT);
        hashMap.put(NSDSContractExt.LineColumns.LOCATION_STATUS, NSDSContractExt.LineColumns.LOCATION_STATUS);
        hashMap.put("tc_status", "tc_status");
        hashMap.put(NSDSContractExt.LineColumns.E911_ADDRESS_ID, NSDSContractExt.LineColumns.E911_ADDRESS_ID);
        hashMap.put("e911_aid_expiration", "e911_aid_expiration");
        hashMap.put(NSDSContractExt.LineColumns.E911_SERVER_DATA, NSDSContractExt.LineColumns.E911_SERVER_DATA);
        hashMap.put(NSDSContractExt.LineColumns.E911_SERVER_URL, NSDSContractExt.LineColumns.E911_SERVER_URL);
        hashMap.put(NSDSContractExt.LineColumns.CAB_STATUS, NSDSContractExt.LineColumns.CAB_STATUS);
        hashMap.put(NSDSContractExt.LineColumns.REG_STATUS, NSDSContractExt.LineColumns.REG_STATUS);
        hashMap.put(NSDSContractExt.LineColumns.RING_TONE, NSDSContractExt.LineColumns.RING_TONE);
        hashMap.put("service_name", "service_name");
        hashMap.put(NSDSContractExt.ServiceColumns.SERVICE_FINGERPRINT, NSDSContractExt.ServiceColumns.SERVICE_FINGERPRINT);
        hashMap.put(NSDSContractExt.ServiceColumns.SERVICE_INSTANCE_ID, NSDSContractExt.ServiceColumns.SERVICE_INSTANCE_ID);
        hashMap.put(NSDSContractExt.ServiceColumns.SERVICE_INSTANCE_TOKEN, NSDSContractExt.ServiceColumns.SERVICE_INSTANCE_TOKEN);
        hashMap.put(NSDSContractExt.ServiceColumns.SERVICE_TOKEN_EXPIRE_TIME, NSDSContractExt.ServiceColumns.SERVICE_TOKEN_EXPIRE_TIME);
        HashMap<String, String> hashMap2 = new HashMap<>();
        sLineEntityProjectionMap = hashMap2;
        hashMap2.put("_id", LinesColumns.CONCRETE_ID);
        hashMap2.put("account_id", LinesColumns.ACCOUNT_ID);
        hashMap2.put("msisdn", "msisdn");
        hashMap2.put(NSDSContractExt.LineColumns.FRIENDLY_NAME, NSDSContractExt.LineColumns.FRIENDLY_NAME);
        hashMap2.put("is_owner", "Lines.is_owner");
        hashMap2.put("is_native", "is_native");
        hashMap2.put("is_native", "is_native");
        hashMap2.put("icon", "icon");
        hashMap2.put(NSDSContractExt.LineColumns.COLOR, NSDSContractExt.LineColumns.COLOR);
        hashMap2.put("device_uid", "device_uid");
        hashMap2.put("device_name", "device_name");
        hashMap2.put("is_primary", "is_primary");
        hashMap2.put(NSDSContractExt.DeviceColumns.DEVICE_TYPE, NSDSContractExt.DeviceColumns.DEVICE_TYPE);
    }

    protected static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, NSDSContentProvider.DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 3);
        }

        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            IMSLog.i(NSDSContentProvider.LOG_TAG, "DatabaseHelper onCreate()");
            sQLiteDatabase.execSQL(NSDSContentProvider.REMOVE_ALL_TABLES_AND_INDICES);
            sQLiteDatabase.execSQL(NSDSContentProvider.CREATE_LINES_TABLE);
            sQLiteDatabase.execSQL(NSDSContentProvider.CREATE_GCM_TOKENS_TABLE);
            sQLiteDatabase.execSQL(NSDSContentProvider.CREATE_DEVICES_TABLE);
            sQLiteDatabase.execSQL(NSDSContentProvider.CREATE_ACCOUNT_TABLE);
            sQLiteDatabase.execSQL(NSDSContentProvider.CREATE_SERVICES_TABLE);
            sQLiteDatabase.execSQL(NSDSContentProvider.CREATE_CONNECTIVITY_PARAMS_TABLE);
            sQLiteDatabase.execSQL(NSDSContentProvider.CREATE_CONNECTIVITY_PARAM_SERVICE_NAME_TABLE);
            sQLiteDatabase.execSQL(NSDSContentProvider.CREATE_PROVISIONING_PARAMS_TABLE);
            sQLiteDatabase.execSQL(NSDSContentProvider.CREATE_DEVICE_CONFIG_TABLE);
            sQLiteDatabase.execSQL(NSDSContentProvider.CREATE_NSDS_CONFIG_TABLE);
            sQLiteDatabase.execSQL(NSDSContentProvider.CREATE_SIM_SWAP_NSDS_CONFIG_TABLE);
            sQLiteDatabase.execSQL(NSDSContentProvider.CREATE_SIM_SWAP_SERVICES_TABLE);
        }

        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            String r2 = NSDSContentProvider.LOG_TAG;
            IMSLog.i(r2, "onUpgrade: oldVersion " + i + " newVersion " + i2);
            if (i2 == 2 && i == 1) {
                NSDSContentProvider.renameDeviceAccountIdColumn(sQLiteDatabase);
            }
            if (i2 == 3) {
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS pending_lines");
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS access_steering");
            }
        }

        public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            String r1 = NSDSContentProvider.LOG_TAG;
            IMSLog.i(r1, "onDowngrade: oldVersion " + i + " newVersion " + i2);
        }
    }

    /* access modifiers changed from: private */
    public static void renameDeviceAccountIdColumn(SQLiteDatabase sQLiteDatabase) {
        IMSLog.i(LOG_TAG, "renameDeviceAccountIdColumn()");
        String str = DEVICES_TABLE + "_temp";
        sQLiteDatabase.execSQL("ALTER TABLE " + DEVICES_TABLE + " RENAME TO " + str);
        sQLiteDatabase.execSQL(CREATE_DEVICES_TABLE);
        sQLiteDatabase.execSQL("INSERT INTO " + DEVICES_TABLE + " select * from " + str);
        StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE ");
        sb.append(str);
        sQLiteDatabase.execSQL(sb.toString());
    }

    public int delete(Uri uri, String str, String[] strArr) {
        int i;
        String str2 = LOG_TAG;
        IMSLog.i(str2, "delete:" + uri);
        int i2 = 0;
        if (this.mDatabaseHelper == null || !NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            return 0;
        }
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        boolean z = true;
        try {
            int match = sUriMatcher.match(uri);
            if (match == 0) {
                i = writableDatabase.delete(LINES_TABLE, str, strArr);
            } else if (match == 2) {
                i = writableDatabase.delete(DEVICES_TABLE, str, strArr);
            } else if (match == 6) {
                i = deleteFromServices(uri.getPathSegments().get(1), uri.getPathSegments().get(3));
            } else if (match == 42) {
                i = writableDatabase.delete("services", "device_id = ?", new String[]{uri.getPathSegments().get(1)});
            } else if (match != 47) {
                if (match == 71) {
                    i = writableDatabase.delete(SIM_SWAP_NSDS_CONFIG_TABLE, str, strArr);
                } else if (match == 74) {
                    i = writableDatabase.delete(GCM_TOKENS_TABLE, str, strArr);
                } else if (match == 39) {
                    i = writableDatabase.delete("device_config", str, strArr);
                } else if (match != 40) {
                    IMSLog.i(str2, "None of the Uri's match for insert:" + uri);
                    writableDatabase.setTransactionSuccessful();
                    writableDatabase.endTransaction();
                    if (i2 > 0 && z) {
                        notifyChange(uri);
                    }
                    return i2;
                } else {
                    try {
                        i = writableDatabase.delete(NSDS_CONFIG_TABLE, str, strArr);
                    } catch (SQLiteException e) {
                        e = e;
                        z = false;
                        try {
                            IMSLog.s(LOG_TAG, "Could not update LINES table:" + e.getMessage());
                            writableDatabase.endTransaction();
                            notifyChange(uri);
                            return i2;
                        } catch (Throwable th) {
                            writableDatabase.endTransaction();
                            throw th;
                        }
                    }
                }
                z = false;
            } else {
                i = writableDatabase.delete(ACCOUNTS_TABLE, str, strArr);
            }
            i2 = i;
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e2) {
            e = e2;
            IMSLog.s(LOG_TAG, "Could not update LINES table:" + e.getMessage());
            writableDatabase.endTransaction();
            notifyChange(uri);
            return i2;
        }
        writableDatabase.endTransaction();
        notifyChange(uri);
        return i2;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        String str = LOG_TAG;
        IMSLog.s(str, "insert: " + uri);
        Uri uri2 = null;
        if (this.mDatabaseHelper != null && NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            int match = sUriMatcher.match(uri);
            if (match == 0) {
                uri2 = NSDSContractExt.Lines.buildLineUri(insertIntoLines(contentValues));
            } else if (match == 2) {
                uri2 = NSDSContractExt.Devices.buildDeviceUri(insertIntoDevices(contentValues));
            } else if (match == 6) {
                long longValue = Long.valueOf(uri.getPathSegments().get(1)).longValue();
                long longValue2 = Long.valueOf(uri.getPathSegments().get(3)).longValue();
                contentValues.put("device_id", Long.valueOf(longValue));
                contentValues.put(NSDSContractExt.ServiceColumns.LINE_ID, Long.valueOf(longValue2));
                uri2 = NSDSContractExt.Services.buildServiceUri(insertIntoServices(contentValues));
            } else if (match == 9) {
                uri2 = NSDSContractExt.Accounts.buildAccountUri(insertIntoAccounts(contentValues));
            } else if (match == 71) {
                uri2 = NSDSContractExt.SimSwapNsdsConfigs.buildNsdsConfigUri(insertIntoSimSwapNsdsConfig(contentValues));
            } else if (match == 74) {
                uri2 = NSDSContractExt.GcmTokens.buildGcmTokensUri(insertIntoGcmTokens(contentValues));
            } else if (match == 39) {
                uri2 = NSDSContractExt.DeviceConfig.buildDeviceConfigUri(insertDeviceConfig(contentValues));
            } else if (match == 40) {
                uri2 = NSDSContractExt.NsdsConfigs.buildNsdsConfigUri(insertIntoNsdsConfig(contentValues));
            }
            if (uri2 != null) {
                notifyChange(uri);
            }
        }
        return uri2;
    }

    public void notifyChange(Uri uri) {
        this.mContext.getContentResolver().notifyChange(uri, (ContentObserver) null);
    }

    private long insertIntoLines(ContentValues contentValues) {
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        long j = -1;
        try {
            String asString = contentValues.getAsString(NSDSContractExt.LineColumns.LINE_RES_PACKAGE);
            Resources resources = this.mContext.getResources();
            if (!TextUtils.isEmpty(asString)) {
                try {
                    resources = this.mContext.getPackageManager().getResourcesForApplication(asString);
                } catch (PackageManager.NameNotFoundException e) {
                    String str = LOG_TAG;
                    IMSLog.s(str, "Lines resource package not found: " + asString + e.getMessage());
                }
            }
            Integer asInteger = contentValues.getAsInteger(NSDSContractExt.LineColumns.COLOR);
            String resourceName = getResourceName(resources, NSDSContractExt.LineColumns.COLOR, asInteger);
            if (resourceName == null) {
                contentValues.remove(NSDSContractExt.LineColumns.COLOR);
                String str2 = LOG_TAG;
                IMSLog.e(str2, "Color resource is null, removing: " + asInteger + " from values");
            }
            Integer asInteger2 = contentValues.getAsInteger("icon");
            String resourceName2 = getResourceName(resources, "drawable", asInteger2);
            if (resourceName2 == null) {
                String str3 = LOG_TAG;
                IMSLog.e(str3, "Icon resource is null, removing: " + asInteger2 + " from values");
                contentValues.remove("icon");
            }
            if (resourceName == null && resourceName2 == null) {
                contentValues.remove(NSDSContractExt.LineColumns.LINE_RES_PACKAGE);
                String str4 = LOG_TAG;
                IMSLog.e(str4, "Both color and icon resource are null, removing: " + asString + " from values");
            }
            j = writableDatabase.insert(LINES_TABLE, (String) null, contentValues);
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e2) {
            String str5 = LOG_TAG;
            IMSLog.s(str5, "Could not insert into LINES:" + e2.getMessage());
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
        writableDatabase.endTransaction();
        return j;
    }

    private long insertIntoDevices(ContentValues contentValues) {
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        long j = -1;
        try {
            j = writableDatabase.insert(DEVICES_TABLE, (String) null, contentValues);
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into DEVICES table:" + e.getMessage());
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
        writableDatabase.endTransaction();
        return j;
    }

    private long insertIntoAccounts(ContentValues contentValues) {
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        long j = -1;
        try {
            j = writableDatabase.insert(ACCOUNTS_TABLE, (String) null, contentValues);
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into DEVICES table:" + e.getMessage());
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
        writableDatabase.endTransaction();
        return j;
    }

    private long insertIntoServices(ContentValues contentValues) {
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        long j = -1;
        try {
            j = writableDatabase.insert("services", (String) null, contentValues);
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into SERVICES table:" + e.getMessage());
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
        writableDatabase.endTransaction();
        return j;
    }

    private long insertIntoGcmTokens(ContentValues contentValues) {
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        long j = -1;
        try {
            j = writableDatabase.insert(GCM_TOKENS_TABLE, (String) null, contentValues);
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into GCM Tokens table:" + e.getMessage());
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
        writableDatabase.endTransaction();
        return j;
    }

    private int deleteFromServices(String str, String str2) {
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        int i = 0;
        try {
            i = writableDatabase.delete("services", "device_id = ? AND line_id = ?", new String[]{str, str2});
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e) {
            String str3 = LOG_TAG;
            IMSLog.s(str3, "Could not delete from Services table:" + e.getMessage());
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
        writableDatabase.endTransaction();
        return i;
    }

    private long insertDeviceConfig(ContentValues contentValues) {
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        long j = -1;
        try {
            j = writableDatabase.insert("device_config", (String) null, contentValues);
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into device_config table:" + e.getMessage());
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
        writableDatabase.endTransaction();
        return j;
    }

    private int updateDeviceConfig(ContentValues contentValues) {
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        int i = 0;
        try {
            i = writableDatabase.update("device_config", contentValues, (String) null, (String[]) null);
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not update connectivity_parameters table:" + e.getMessage());
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
        writableDatabase.endTransaction();
        return i;
    }

    private long insertIntoNsdsConfig(ContentValues contentValues) {
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        long j = -1;
        try {
            j = writableDatabase.insert(NSDS_CONFIG_TABLE, (String) null, contentValues);
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into nsds_configs table:" + e.getMessage());
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
        writableDatabase.endTransaction();
        return j;
    }

    private long insertIntoSimSwapNsdsConfig(ContentValues contentValues) {
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        long j = -1;
        try {
            j = writableDatabase.insert(SIM_SWAP_NSDS_CONFIG_TABLE, (String) null, contentValues);
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into nsds_configs table:" + e.getMessage());
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
        writableDatabase.endTransaction();
        return j;
    }

    public boolean onCreate() {
        this.mContext = getContext().createCredentialProtectedStorageContext();
        return true;
    }

    private synchronized void connectToNSDSMultiSimService() {
        IMSLog.i(LOG_TAG, "connectToNSDSMultiSimService()");
        Intent intent = new Intent();
        intent.setClassName("com.sec.imsservice", "com.sec.internal.ims.entitlement.nsds.NSDSMultiSimService");
        AnonymousClass1 r1 = new ServiceConnection() {
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                IMSLog.i(NSDSContentProvider.LOG_TAG, "onServiceConnected: Connected to NSDSMultiSimService.");
                if (MigrationHelper.checkMigrateDB(NSDSContentProvider.this.mContext)) {
                    IMSLog.i(NSDSContentProvider.LOG_TAG, "Connect DB");
                    NSDSContentProvider.this.mDatabaseHelper = new DatabaseHelper(NSDSContentProvider.this.mContext);
                }
                NSDSContentProvider.this.mNsdsService = new Messenger(iBinder);
            }

            public void onServiceDisconnected(ComponentName componentName) {
                IMSLog.i(NSDSContentProvider.LOG_TAG, "onServiceDisconnected: Disconnected.");
                NSDSContentProvider.this.mNsdsService = null;
            }
        };
        this.mNsdsSvcConn = r1;
        ContextExt.bindServiceAsUser(this.mContext, intent, r1, 1, ContextExt.CURRENT_OR_SELF);
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        Uri uri2 = uri;
        String str3 = str;
        String[] strArr3 = strArr2;
        String str4 = LOG_TAG;
        IMSLog.s(str4, "query " + uri2);
        if (this.mDatabaseHelper == null || !NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            return null;
        }
        SQLiteDatabase readableDatabase = this.mDatabaseHelper.getReadableDatabase();
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        int match = sUriMatcher.match(uri2);
        if (match != 0) {
            if (match == 2) {
                sQLiteQueryBuilder.setTables(DEVICES_TABLE);
                return sQLiteQueryBuilder.query(readableDatabase, strArr, str, strArr2, (String) null, (String) null, str2);
            } else if (match == 29) {
                sQLiteQueryBuilder.setTables(TABLE_JOIN_LINES_SERVICES);
                sQLiteQueryBuilder.appendWhere(SQL_WHERE_LOCAL_LINES_WITH_SERVICES);
                sQLiteQueryBuilder.setProjectionMap(sLineWithServicesProjectionMap);
                return sQLiteQueryBuilder.query(readableDatabase, strArr, str, insertSelectionArg(insertSelectionArg(strArr3, "1"), getDeviceUidFromQueryParam(uri)), (String) null, (String) null, str2);
            } else if (match == 67) {
                return getDevicePushToken(uri);
            } else {
                if (match == 74) {
                    sQLiteQueryBuilder.setTables(GCM_TOKENS_TABLE);
                    return sQLiteQueryBuilder.query(readableDatabase, strArr, str, strArr2, (String) null, (String) null, str2);
                } else if (match == 77) {
                    sQLiteQueryBuilder.setTables(TABLE_JOIN_FOR_ALL_LINES);
                    sQLiteQueryBuilder.setProjectionMap(sLineWithServicesProjectionMap);
                    if (!TextUtils.isEmpty(str) && str3.startsWith("_id")) {
                        str3 = str3.replace("_id", LinesColumns.CONCRETE_ID);
                    }
                    return sQLiteQueryBuilder.query(readableDatabase, strArr, str3, strArr2, (String) null, (String) null, str2);
                } else if (match == 39) {
                    return this.mContext.getContentResolver().query(EntitlementConfigContract.DeviceConfig.CONTENT_URI, strArr, str, strArr2, str2);
                } else {
                    if (match == 40) {
                        sQLiteQueryBuilder.setTables(NSDS_CONFIG_TABLE);
                        return sQLiteQueryBuilder.query(readableDatabase, strArr, str, strArr2, (String) null, (String) null, str2);
                    } else if (match == 62) {
                        return getDeviceConfigElement(uri);
                    } else {
                        if (match != 63) {
                            String str5 = "services";
                            switch (match) {
                                case 6:
                                    sQLiteQueryBuilder.setTables(str5);
                                    sQLiteQueryBuilder.appendWhere("device_id = " + uri.getPathSegments().get(1) + " AND " + NSDSContractExt.ServiceColumns.LINE_ID + " = " + uri.getPathSegments().get(3));
                                    return sQLiteQueryBuilder.query(readableDatabase, strArr, str, strArr2, (String) null, (String) null, str2);
                                case 7:
                                    sQLiteQueryBuilder.setTables(ACCOUNTS_TABLE);
                                    sQLiteQueryBuilder.appendWhere(SQL_WHERE_ACTIVE_ACCOUNT);
                                    return sQLiteQueryBuilder.query(readableDatabase, strArr, str, strArr2, (String) null, (String) null, str2);
                                case 8:
                                    sQLiteQueryBuilder.setTables(TABLE_JOIN_LINES_SERVICES);
                                    sQLiteQueryBuilder.appendWhere(SQL_WHERE_LINE_STATUS_ACTIVE);
                                    return sQLiteQueryBuilder.query(readableDatabase, strArr, (TextUtils.isEmpty(str) || !str3.startsWith("_id")) ? str3 : str3.replace("_id", LinesColumns.CONCRETE_ID), insertSelectionArg(insertSelectionArg(strArr3, "1"), getDeviceUidFromQueryParam(uri)), (String) null, (String) null, str2);
                                case 9:
                                    sQLiteQueryBuilder.setTables(ACCOUNTS_TABLE);
                                    return sQLiteQueryBuilder.query(readableDatabase, strArr, str, strArr2, (String) null, (String) null, str2);
                                default:
                                    switch (match) {
                                        case 43:
                                            sQLiteQueryBuilder.setTables(DEVICES_TABLE);
                                            sQLiteQueryBuilder.appendWhere(SQL_WHERE_DEVICES_FOR_LINE_ID);
                                            return sQLiteQueryBuilder.query(readableDatabase, strArr, str, insertSelectionArg(strArr3, uri.getPathSegments().get(1)), (String) null, (String) null, str2);
                                        case 44:
                                            break;
                                        case 45:
                                            sQLiteQueryBuilder.setTables(TABLE_JOIN_FOR_ALL_LINES);
                                            sQLiteQueryBuilder.appendWhere(SQL_WHERE_ALL_LINES);
                                            sQLiteQueryBuilder.setProjectionMap(sLineWithServicesProjectionMap);
                                            if (!TextUtils.isEmpty(str) && str3.startsWith("_id")) {
                                                str3 = str3.replace("_id", LinesColumns.CONCRETE_ID);
                                            }
                                            return sQLiteQueryBuilder.query(readableDatabase, strArr, str3, strArr2, (String) null, (String) null, str2);
                                        default:
                                            switch (match) {
                                                case 70:
                                                    sQLiteQueryBuilder.setTables(TABLE_JOIN_LINES_SERVICES);
                                                    sQLiteQueryBuilder.appendWhere(SQL_WHERE_LOCAL_LINES_WITH_SERVICES);
                                                    sQLiteQueryBuilder.setProjectionMap(sLineWithServicesProjectionMap);
                                                    return sQLiteQueryBuilder.query(readableDatabase, strArr, str, insertSelectionArg(insertSelectionArg(strArr3, "0"), getDeviceUidFromQueryParam(uri)), (String) null, (String) null, str2);
                                                case 71:
                                                    sQLiteQueryBuilder.setTables(SIM_SWAP_NSDS_CONFIG_TABLE);
                                                    return sQLiteQueryBuilder.query(readableDatabase, strArr, str, strArr2, (String) null, (String) null, str2);
                                                case 72:
                                                    sQLiteQueryBuilder.setTables(SIM_SWAP_SERVICES_TABLE);
                                                    return sQLiteQueryBuilder.query(readableDatabase, strArr, str, strArr2, (String) null, (String) null, str2);
                                                default:
                                                    return queryInternalWithService(readableDatabase, uri2);
                                            }
                                    }
                            }
                        } else {
                            sQLiteQueryBuilder.setTables("services");
                            return sQLiteQueryBuilder.query(readableDatabase, strArr, str, strArr2, (String) null, (String) null, str2);
                        }
                    }
                }
            }
        }
        sQLiteQueryBuilder.setTables(TABLE_JOIN_LINES_SERVICES);
        sQLiteQueryBuilder.appendWhere(SQL_WHERE_LINES_FOR_ACTIVE_ACCOUNT);
        sQLiteQueryBuilder.setProjectionMap(sLineWithServicesProjectionMap);
        return sQLiteQueryBuilder.query(readableDatabase, strArr, (TextUtils.isEmpty(str) || !str3.startsWith("_id")) ? str3 : str3.replace("_id", LinesColumns.CONCRETE_ID), insertSelectionArg(strArr3, getDeviceUidFromQueryParam(uri)), (String) null, (String) null, str2);
    }

    private int getSlotIdFromUri(Uri uri) {
        try {
            return Integer.parseInt(uri.getQueryParameter(NSDSContractExt.QueryParams.SLOT_ID));
        } catch (NullPointerException | NumberFormatException unused) {
            return 0;
        }
    }

    private String getDeviceUidFromQueryParam(Uri uri) {
        String queryParameter = uri.getQueryParameter("device_uid");
        if (TextUtils.isEmpty(queryParameter)) {
            queryParameter = DeviceIdHelper.getDeviceIdIfExists(this.mContext, getSlotIdFromUri(uri));
        }
        return TextUtils.isEmpty(queryParameter) ? "dummy.txt.txt" : queryParameter;
    }

    private Cursor queryInternalWithService(SQLiteDatabase sQLiteDatabase, Uri uri) {
        String str = LOG_TAG;
        IMSLog.s(str, "queryInternalWithService: uri:" + uri);
        if (this.mNsdsService == null) {
            IMSLog.e(str, "query: NSDS service is not connected");
            return null;
        }
        int slotIdFromUri = getSlotIdFromUri(uri);
        int match = sUriMatcher.match(uri);
        if (match == 28) {
            return getDeviceOwnActivationStatus(slotIdFromUri);
        }
        if (match != 60) {
            return null;
        }
        return getDeviceOwnReadyStatus(slotIdFromUri);
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        int update;
        String str2 = LOG_TAG;
        IMSLog.s(str2, "update " + uri);
        UriMatcher uriMatcher = sUriMatcher;
        int i = 0;
        if (uriMatcher.match(uri) == 82) {
            IMSLog.i(str2, "Binding to NSDSMultiSimService");
            connectToNSDSMultiSimService();
            return 0;
        } else if (uriMatcher.match(uri) == 81) {
            IMSLog.e(str2, "Reconnect DB for DatabaseHelper null");
            if (this.mDatabaseHelper != null) {
                IMSLog.i(str2, "Reconnect DB after closing the previous DB");
                this.mDatabaseHelper.close();
            }
            this.mDatabaseHelper = new DatabaseHelper(this.mContext);
            return 0;
        } else if (this.mDatabaseHelper == null || !NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            return 0;
        } else {
            SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
            if (uriMatcher.match(uri) == 0) {
                writableDatabase.beginTransactionNonExclusive();
            } else {
                writableDatabase.beginTransaction();
            }
            try {
                int match = uriMatcher.match(uri);
                if (match != 0) {
                    if (match == 2) {
                        update = writableDatabase.update(DEVICES_TABLE, contentValues, str, strArr);
                    } else if (match == 6) {
                        i = writableDatabase.update("services", contentValues, "device_id = ? AND line_id = ?", new String[]{uri.getPathSegments().get(1), uri.getPathSegments().get(3)});
                        if (i > 0) {
                            IMSLog.i(str2, "Updated Services table for deviceId " + uri.getPathSegments().get(1) + " and lineId :" + uri.getPathSegments().get(3));
                        }
                    } else if (match == 9) {
                        update = writableDatabase.update(ACCOUNTS_TABLE, contentValues, str, strArr);
                    } else if (match == 26) {
                        update = setDevicePrimary(writableDatabase, uri);
                    } else if (match == 74) {
                        update = writableDatabase.update(GCM_TOKENS_TABLE, contentValues, str, strArr);
                    } else if (match != 39) {
                        if (match != 40) {
                            switch (match) {
                                case 47:
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("_id = ?");
                                    i = writableDatabase.update(ACCOUNTS_TABLE, contentValues, appendSelection(sb, str), insertSelectionArg(strArr, uri.getPathSegments().get(1)));
                                    if (i <= 0) {
                                        IMSLog.i(str2, "Updating the account failed");
                                        break;
                                    }
                                    break;
                                case 48:
                                    update = disableActiveAccount(writableDatabase, uri.getQueryParameter("account_id"));
                                    break;
                                case 49:
                                    update = updateCabStatus(writableDatabase, uri, 1);
                                    break;
                                case 50:
                                    update = updateCabStatus(writableDatabase, uri, 0);
                                    break;
                                default:
                                    update = updateInternalWithService(uri);
                                    break;
                            }
                        }
                    } else {
                        update = updateDeviceConfig(contentValues);
                    }
                    i = update;
                } else {
                    i = writableDatabase.update(LINES_TABLE, contentValues, str, strArr);
                    if (i <= 0) {
                        IMSLog.i(str2, "Updating lines failed");
                    }
                }
                writableDatabase.setTransactionSuccessful();
            } catch (SQLiteException e) {
                String str3 = LOG_TAG;
                IMSLog.s(str3, "Could not update table:" + e.getMessage());
            } catch (Throwable th) {
                writableDatabase.endTransaction();
                throw th;
            }
            writableDatabase.endTransaction();
            if (i != 0) {
                notifyChange(uri);
            }
            return i;
        }
    }

    private int updateInternalWithService(Uri uri) {
        String str = LOG_TAG;
        IMSLog.s(str, "updateInternalWithService: uri " + uri);
        if (this.mNsdsService == null) {
            IMSLog.e(str, "update: NSDS service is not connected");
            return 0;
        }
        int slotIdFromUri = getSlotIdFromUri(uri);
        if (NSDSSharedPrefHelper.isSimSwapPending(this.mContext, DeviceIdHelper.getDeviceIdIfExists(this.mContext, slotIdFromUri))) {
            IMSLog.e(str, "SimSwap process is in progress. Ignore operations now");
            return 0;
        }
        int match = sUriMatcher.match(uri);
        if (match == 46) {
            updateE911Address(slotIdFromUri);
            return 0;
        } else if (match == 73) {
            updateEntitlementUrl(uri);
            return 0;
        } else if (match != 80) {
            switch (match) {
                case 17:
                    addServicesToLine(uri);
                    return 0;
                case 18:
                    return removeServicesFromLine(uri);
                case 19:
                    return updateServicesStatusForLine(uri, true);
                case 20:
                    return updateServicesStatusForLine(uri, false);
                default:
                    switch (match) {
                        case 30:
                            activateSimDevice(slotIdFromUri);
                            return 0;
                        case 31:
                            deactivateSimDevice(uri);
                            return 0;
                        case 32:
                            handleVoWiFiToggleOnEvent(slotIdFromUri);
                            return 0;
                        case 33:
                            handleVoWiFiToggleOffEvent(slotIdFromUri);
                            return 0;
                        default:
                            return 0;
                    }
            }
        } else {
            retrieveAkaToken(uri);
            return 0;
        }
    }

    private int disableActiveAccount(SQLiteDatabase sQLiteDatabase, String str) {
        disableLinesAndServices(sQLiteDatabase);
        ContentValues contentValues = new ContentValues();
        contentValues.put(NSDSContractExt.AccountColumns.IS_ACTIVE, 0);
        int update = sQLiteDatabase.update(ACCOUNTS_TABLE, contentValues, "is_active = ?  AND _id = ?", new String[]{"1", str});
        if (update <= 0) {
            IMSLog.i(LOG_TAG, "disabling the account failed");
        }
        return update;
    }

    private void disableLinesAndServices(SQLiteDatabase sQLiteDatabase) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", "0");
        int update = sQLiteDatabase.update(LINES_TABLE, contentValues, (String) null, (String[]) null);
        String str = LOG_TAG;
        IMSLog.i(str, "disableLinesAndServices: de-activated :" + update + " lines for logout");
        if (update > 0) {
            notifyChange(NSDSContractExt.Lines.CONTENT_URI);
        }
        IMSLog.i(str, "disableLinesAndServices: de-activated lines for logout");
        sQLiteDatabase.delete("services", (String) null, (String[]) null);
    }

    private Cursor getDeviceOwnActivationStatus(int i) {
        String str;
        String deviceIdIfExists = DeviceIdHelper.getDeviceIdIfExists(this.mContext, i);
        boolean isSimSwapPending = NSDSSharedPrefHelper.isSimSwapPending(this.mContext, deviceIdIfExists);
        if (isSimSwapPending) {
            str = NSDSNamespaces.NSDSDeviceState.ACTIVATION_IN_PROGRESS;
        } else {
            str = NSDSSharedPrefHelper.get(this.mContext, deviceIdIfExists, NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE);
        }
        String str2 = LOG_TAG;
        IMSLog.i(str2, "getDeviceState: onSimSwapEvt " + isSimSwapPending + " state " + str);
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{NSDSContractExt.Devices.OWN_ACTIVATION_STATUS});
        matrixCursor.newRow().add(str);
        return matrixCursor;
    }

    private Cursor getDeviceOwnReadyStatus(int i) {
        String deviceIdIfExists = DeviceIdHelper.getDeviceIdIfExists(this.mContext, i);
        boolean z = !NSDSSharedPrefHelper.isSimSwapPending(this.mContext, deviceIdIfExists);
        if (!NSDSSharedPrefHelper.isDeviceActivated(this.mContext, deviceIdIfExists) || NSDSSharedPrefHelper.isDeviceInActivationProgress(this.mContext, deviceIdIfExists)) {
            activateSimDevice(i);
            z = false;
        }
        IMSLog.s(LOG_TAG, "own_ready_status:" + z);
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{NSDSContractExt.Devices.OWN_READY_STATUS});
        matrixCursor.newRow().add(Boolean.valueOf(z));
        return matrixCursor;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0077  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x009d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.database.Cursor getDeviceConfigElement(android.net.Uri r10) {
        /*
            r9 = this;
            r0 = 0
            java.lang.String r1 = "tag_name"
            java.lang.String r10 = r10.getQueryParameter(r1)     // Catch:{ Exception -> 0x004c }
            boolean r1 = android.text.TextUtils.isEmpty(r10)     // Catch:{ Exception -> 0x004a }
            if (r1 == 0) goto L_0x0016
            java.lang.String r9 = LOG_TAG     // Catch:{ Exception -> 0x004a }
            java.lang.String r1 = "Empty tag name. Return null"
            com.sec.internal.log.IMSLog.e(r9, r1)     // Catch:{ Exception -> 0x004a }
            return r0
        L_0x0016:
            r1 = 1
            java.lang.String[] r4 = new java.lang.String[r1]     // Catch:{ Exception -> 0x004a }
            java.lang.String r1 = "device_config"
            r8 = 0
            r4[r8] = r1     // Catch:{ Exception -> 0x004a }
            android.content.Context r9 = r9.mContext     // Catch:{ Exception -> 0x004a }
            android.content.ContentResolver r2 = r9.getContentResolver()     // Catch:{ Exception -> 0x004a }
            android.net.Uri r3 = com.sec.internal.constants.ims.entitilement.EntitlementConfigContract.DeviceConfig.CONTENT_URI     // Catch:{ Exception -> 0x004a }
            r5 = 0
            r6 = 0
            r7 = 0
            android.database.Cursor r9 = r2.query(r3, r4, r5, r6, r7)     // Catch:{ Exception -> 0x004a }
            if (r9 == 0) goto L_0x0044
            boolean r1 = r9.moveToFirst()     // Catch:{ all -> 0x003a }
            if (r1 == 0) goto L_0x0044
            java.lang.String r0 = r9.getString(r8)     // Catch:{ all -> 0x003a }
            goto L_0x0044
        L_0x003a:
            r1 = move-exception
            r9.close()     // Catch:{ all -> 0x003f }
            goto L_0x0043
        L_0x003f:
            r9 = move-exception
            r1.addSuppressed(r9)     // Catch:{ Exception -> 0x004a }
        L_0x0043:
            throw r1     // Catch:{ Exception -> 0x004a }
        L_0x0044:
            if (r9 == 0) goto L_0x0068
            r9.close()     // Catch:{ Exception -> 0x004a }
            goto L_0x0068
        L_0x004a:
            r9 = move-exception
            goto L_0x004e
        L_0x004c:
            r9 = move-exception
            r10 = r0
        L_0x004e:
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "SQL exception while parseDeviceConfig "
            r2.append(r3)
            java.lang.String r9 = r9.getMessage()
            r2.append(r9)
            java.lang.String r9 = r2.toString()
            com.sec.internal.log.IMSLog.s(r1, r9)
        L_0x0068:
            android.database.MatrixCursor r9 = new android.database.MatrixCursor
            java.lang.String r1 = "element_name"
            java.lang.String r2 = "element_value"
            java.lang.String[] r1 = new java.lang.String[]{r1, r2}
            r9.<init>(r1)
            if (r0 == 0) goto L_0x009d
            java.util.Map r10 = com.sec.internal.ims.entitlement.util.ConfigElementExtractor.getAllElements(r0, r10)
            java.util.Set r0 = r10.keySet()
            java.util.Iterator r0 = r0.iterator()
        L_0x0083:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x00a4
            java.lang.Object r1 = r0.next()
            java.lang.String r1 = (java.lang.String) r1
            java.lang.Object r2 = r10.get(r1)
            java.lang.String r2 = (java.lang.String) r2
            java.lang.String[] r1 = new java.lang.String[]{r1, r2}
            r9.addRow(r1)
            goto L_0x0083
        L_0x009d:
            java.lang.String r10 = LOG_TAG
            java.lang.String r0 = "Device Config is null: "
            com.sec.internal.log.IMSLog.e(r10, r0)
        L_0x00a4:
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.ericssonnsds.persist.NSDSContentProvider.getDeviceConfigElement(android.net.Uri):android.database.Cursor");
    }

    private void updateE911Address(int i) {
        try {
            Message message = new Message();
            message.what = 19;
            Bundle bundle = new Bundle();
            bundle.putInt("SLOT_ID", i);
            message.setData(bundle);
            this.mNsdsService.send(message);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "openLoginPage: failed to open login page" + e.getMessage());
        }
    }

    private void activateSimDevice(int i) {
        Context context = this.mContext;
        NSDSSharedPrefHelper.save(context, DeviceIdHelper.getDeviceIdIfExists(context, i), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, "completed");
        try {
            Message obtain = Message.obtain();
            obtain.what = 3;
            Bundle bundle = new Bundle();
            bundle.putInt("SLOT_ID", i);
            bundle.putInt("EVENT_TYPE", 11);
            bundle.putInt("RETRY_COUNT", 0);
            obtain.setData(bundle);
            this.mNsdsService.send(obtain);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "activateSIMDevice: failed to activate" + e.getMessage());
        }
    }

    private void deactivateSimDevice(Uri uri) {
        NSDSSharedPrefHelper.save(this.mContext, DeviceIdHelper.getDeviceIdIfExists(this.mContext, getSlotIdFromUri(uri)), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, "completed");
        try {
            String queryParameter = uri.getQueryParameter("imsi");
            Message message = new Message();
            message.what = 4;
            Bundle bundle = new Bundle();
            bundle.putString("IMSI", queryParameter);
            message.setData(bundle);
            this.mNsdsService.send(message);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "openLoginPage: failed to open login page" + e.getMessage());
        }
    }

    private void handleVoWiFiToggleOnEvent(int i) {
        NSDSSharedPrefHelper.save(this.mContext, DeviceIdHelper.getDeviceIdIfExists(this.mContext, i), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, "completed");
        try {
            Message message = new Message();
            message.what = 220;
            Bundle bundle = new Bundle();
            bundle.putInt("SLOT_ID", i);
            message.setData(bundle);
            this.mNsdsService.send(message);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "handleVoWiFiToggleOnEvent: failed to toggle on" + e.getMessage());
        }
    }

    private void handleVoWiFiToggleOffEvent(int i) {
        NSDSSharedPrefHelper.save(this.mContext, DeviceIdHelper.getDeviceIdIfExists(this.mContext, i), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, "completed");
        try {
            Message message = new Message();
            message.what = 221;
            Bundle bundle = new Bundle();
            bundle.putInt("SLOT_ID", i);
            message.setData(bundle);
            this.mNsdsService.send(message);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "handleVoWiFiToggleOffEvent: failed to toggle off" + e.getMessage());
        }
    }

    private int setDevicePrimary(SQLiteDatabase sQLiteDatabase, Uri uri) {
        ContentValues contentValues = new ContentValues();
        String queryParameter = uri.getQueryParameter("is_primary");
        if (TextUtils.isEmpty(queryParameter)) {
            IMSLog.i(LOG_TAG, "Can not update isPrimary since Query parameter:is_primary is null or empty");
        }
        contentValues.put("is_primary", queryParameter);
        int update = sQLiteDatabase.update(DEVICES_TABLE, contentValues, "_id = ?", new String[]{uri.getPathSegments().get(1)});
        if (update == 1) {
            IMSLog.i(LOG_TAG, "setDevicePrimary is successful:");
            broadcastPrimaryDeviceSettingChanged(queryParameter);
        }
        return update;
    }

    private void updateEntitlementUrl(Uri uri) {
        String queryParameter = uri.getQueryParameter("entitlement_url");
        String queryParameter2 = uri.getQueryParameter("imsi");
        try {
            Message message = new Message();
            message.what = 212;
            Bundle bundle = new Bundle();
            bundle.putString("URL", queryParameter);
            if (!TextUtils.isEmpty(queryParameter2)) {
                bundle.putString("IMSI", queryParameter2);
            }
            message.setData(bundle);
            this.mNsdsService.send(message);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "updateEntitlementUrl: failed to request" + e.getMessage());
        }
    }

    private void retrieveAkaToken(Uri uri) {
        try {
            String queryParameter = uri.getQueryParameter("imsi");
            Message obtain = Message.obtain();
            obtain.what = 49;
            Bundle bundle = new Bundle();
            bundle.putString("IMSI", queryParameter);
            bundle.putInt("EVENT_TYPE", 19);
            bundle.putInt("RETRY_COUNT", 0);
            obtain.setData(bundle);
            this.mNsdsService.send(obtain);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "retrieveAkaToken: failed to retrieve aka" + e.getMessage());
        }
    }

    public Cursor getDevicePushToken(Uri uri) {
        String pushToken = PushTokenHelper.getPushToken(getContext(), DeviceIdHelper.getDeviceIdIfExists(this.mContext, getSlotIdFromUri(uri)));
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"device_push_token"});
        matrixCursor.newRow().add(pushToken);
        String str = LOG_TAG;
        IMSLog.s(str, "getDevicePushToken: " + pushToken);
        return matrixCursor;
    }

    private void broadcastPrimaryDeviceSettingChanged(String str) {
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.IS_PRIMARY_ACTIVATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.IS_PRIMARY_DEVICE, str != null && Integer.valueOf(str).intValue() > 0);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    private int updateCabStatus(SQLiteDatabase sQLiteDatabase, Uri uri, int i) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NSDSContractExt.LineColumns.CAB_STATUS, Integer.valueOf(i));
        String str = uri.getPathSegments().get(1);
        int update = sQLiteDatabase.update(LINES_TABLE, contentValues, SQL_WHERE_LINE_ID, new String[]{str});
        if (update > 0) {
            String str2 = LOG_TAG;
            IMSLog.i(str2, "updateCabStatus: cab status successfully updated for lineId :" + str + " to :" + i);
        }
        return update;
    }

    private long addServicesToLine(Uri uri) {
        Long valueOf = Long.valueOf(uri.getPathSegments().get(1));
        Long valueOf2 = Long.valueOf(uri.getPathSegments().get(3));
        String queryParameter = uri.getQueryParameter(NSDSContractExt.Lines.QUERY_PARAM_SERVICE_NAMES);
        long j = 0;
        for (String put : queryParameter.split(",")) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("device_id", valueOf);
            contentValues.put(NSDSContractExt.ServiceColumns.LINE_ID, valueOf2);
            contentValues.put("service_name", put);
            j += insertIntoServices(contentValues);
        }
        if (j == 0) {
            IMSLog.i(LOG_TAG, "Could not add services:" + queryParameter + " to line Id" + valueOf2);
        }
        return j;
    }

    private int removeServicesFromLine(Uri uri) {
        String str = uri.getPathSegments().get(3);
        int deleteFromServices = deleteFromServices(uri.getPathSegments().get(1), str);
        if (deleteFromServices == 0) {
            String str2 = LOG_TAG;
            IMSLog.e(str2, "Could not delete services for device Id" + str);
        }
        return deleteFromServices;
    }

    private int updateServicesStatusForLine(Uri uri, boolean z) {
        String str = uri.getPathSegments().get(1);
        String str2 = uri.getPathSegments().get(3);
        String queryParameter = uri.getQueryParameter(NSDSContractExt.Lines.QUERY_PARAM_SERVICE_IDS);
        int i = 0;
        for (String updateStatusInServices : queryParameter.split(" ")) {
            i += updateStatusInServices(str, str2, updateStatusInServices, z ? 1 : 0);
        }
        if (i == 0) {
            IMSLog.e(LOG_TAG, "Could not add services:" + queryParameter + " to line Id" + str2);
        }
        return i;
    }

    private int updateStatusInServices(String str, String str2, String str3, int i) {
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        int i2 = 0;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(NSDSContractExt.ServiceColumns.SERVICE_STATUS, Integer.valueOf(i));
            i2 = writableDatabase.update("services", contentValues, "device_id= ? AND line_id= ? AND _id = ?", new String[]{str, str2, str3});
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e) {
            String str4 = LOG_TAG;
            IMSLog.s(str4, "updateStatusInServices: Could not update Services table:" + e.getMessage());
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
        writableDatabase.endTransaction();
        return i2;
    }

    private String appendSelection(StringBuilder sb, String str) {
        if (!TextUtils.isEmpty(str)) {
            sb.append(" AND (");
            sb.append(str);
            sb.append(')');
        }
        return sb.toString();
    }

    private String[] insertSelectionArg(String[] strArr, String str) {
        if (strArr == null) {
            return new String[]{str};
        }
        String[] strArr2 = new String[(strArr.length + 1)];
        strArr2[0] = str;
        System.arraycopy(strArr, 0, strArr2, 1, strArr.length);
        return strArr2;
    }

    private String getResourceName(Resources resources, String str, Integer num) {
        if (num != null) {
            try {
                if (num.intValue() != 0) {
                    String resourceEntryName = resources.getResourceEntryName(num.intValue());
                    String resourceTypeName = resources.getResourceTypeName(num.intValue());
                    if (str.equals(resourceTypeName)) {
                        return resourceEntryName;
                    }
                    String str2 = LOG_TAG;
                    IMSLog.e(str2, "Resource " + num + " (" + resourceEntryName + ") is of type " + resourceTypeName + " but " + str + " is required.");
                    return null;
                }
            } catch (Resources.NotFoundException unused) {
            }
        }
        return null;
    }

    /* JADX INFO: finally extract failed */
    public int bulkInsert(Uri uri, ContentValues[] contentValuesArr) {
        String str;
        DatabaseHelper databaseHelper;
        int match = sUriMatcher.match(uri);
        String queryParameter = uri.getQueryParameter("imsi");
        if (match == 40) {
            str = NSDS_CONFIG_TABLE;
        } else if (match != 71) {
            String str2 = LOG_TAG;
            IMSLog.i(str2, "None of the Uri's match for bulkInsert:" + uri);
            str = null;
        } else {
            str = SIM_SWAP_NSDS_CONFIG_TABLE;
        }
        if (!MigrationHelper.checkMigrateDB(this.mContext)) {
            IMSLog.s(LOG_TAG, "ignoring nsds_config inserts since migration is not done yet");
            return -1;
        }
        int i = 0;
        if (!(str == null || (databaseHelper = this.mDatabaseHelper) == null)) {
            SQLiteDatabase writableDatabase = databaseHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                if (SIM_SWAP_NSDS_CONFIG_TABLE.equals(str)) {
                    writableDatabase.execSQL("DELETE FROM sim_swap_nsds_configs");
                    if (TextUtils.isEmpty(queryParameter)) {
                        writableDatabase.execSQL("INSERT INTO " + str + " SELECT * from nsds_configs");
                    } else {
                        writableDatabase.execSQL("INSERT INTO " + str + " SELECT * from nsds_configs WHERE IMSI = '" + queryParameter + "'");
                    }
                    writableDatabase.execSQL("DELETE FROM sim_swap_services");
                    writableDatabase.execSQL("INSERT INTO sim_swap_services SELECT * from services");
                }
                int length = contentValuesArr.length;
                while (i < length) {
                    if (writableDatabase.insertOrThrow(str, (String) null, contentValuesArr[i]) > 0) {
                        i++;
                    } else {
                        throw new SQLException("Failed to insert row into " + uri);
                    }
                }
                writableDatabase.setTransactionSuccessful();
                i = contentValuesArr.length;
                writableDatabase.endTransaction();
                notifyChange(uri);
            } catch (Throwable th) {
                writableDatabase.endTransaction();
                throw th;
            }
        }
        return i;
    }
}
