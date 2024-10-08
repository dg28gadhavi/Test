package com.sec.internal.ims.entitlement.softphone;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoftphoneSettingsProvider extends ContentProvider {
    static final int ACCOUNT = 1;
    static final int ACCOUNT_ADDRESS = 10;
    static final int ACCOUNT_FOR_ID = 7;
    static final int ACCOUNT_FOR_ID_MUM = 25;
    static final int ACCOUNT_FOR_IMPI = 6;
    static final int ACCOUNT_LABEL = 16;
    static final int ACCOUNT_LABEL_MUM = 26;
    static final int ACTIVATE_ACCOUNT = 4;
    static final int ACTIVE_ACCOUNT = 2;
    static final int ACTIVE_ACCOUNT_MUM = 23;
    static final int ACTIVE_ADDRESS = 11;
    static final int ADDRESS = 8;
    static final int ADDRESS_ID = 9;
    public static final String AUTHORITY = "com.sec.vsim.attsoftphone.settings";
    public static final String DATABASE_NAME = "softphone.db";
    public static final int DATABASE_VERSION = 3;
    static final int DEACTIVATE_ACCOUNT = 5;
    static final int FULL_FUNCTIONAL_ACCOUNT = 21;
    static final int GET_CURRENT_ADDRESS_BY_ACCOUNT = 17;
    static final int GET_CURRENT_ADDRESS_BY_IMPI = 18;
    static final int GET_DEFAULT_ADDRESS_BY_ACCOUNT = 19;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = SoftphoneSettingsProvider.class.getSimpleName();
    static final int MARK_ADDRESS_DELETED = 24;
    static final int PENDING_ACCOUNT = 3;
    static final int REGISTERED_ACCOUNT = 22;
    static final int REGISTER_ACCOUNT = 20;
    static final int SET_CURRENT_ADDRESS = 12;
    static final int SET_DEFAULT_ADDRESS = 14;
    static final String SQL_WHERE_ACCOUNT_ID = "account_id= ?";
    private static final String SQL_WHERE_ACTIVE_ACCOUNT = "status >= 2";
    private static final String SQL_WHERE_FULL_FUNCTIONAL_ACCOUNT = "status > 3";
    private static final String SQL_WHERE_PENDING_ACCOUNT = "status = 1";
    private static final String SQL_WHERE_REGISTERED_ACCOUNT = "status = 5";
    static final int UNSET_CURRENT_ADDRESS = 13;
    static final int UNSET_DEFAULT_ADDRESS = 15;
    static UriMatcher sUriMatcher;
    Context mContext = null;
    DatabaseHelper mDbHelper;

    public String getType(Uri uri) {
        return null;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/#", 1);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/active_account", 2);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/active_account/#", 23);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/pending_account/#", 3);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/full_functional_account", 21);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/registered_account", 22);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/activate/*", 4);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/deactivate/*", 5);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/register/*", 20);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/impi/*", 6);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/account_id/*", 7);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/account_id/*/#", 25);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/label/*", 16);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/label/*/#", 26);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address", 8);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/#", 9);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/account_address/*", 10);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/active_address/*", 11);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/current_address/set/*/#", 12);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/current_address/unset/*/#", 13);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/default_address/set/*/#", 14);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/default_address/unset/*/#", 15);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/saved_address/delete/*/#", 24);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/get_current_address/by_account/*", 17);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/get_current_address/by_impi/*", 18);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/get_default_address/by_account/*", 19);
    }

    /* access modifiers changed from: package-private */
    public String getAccountId(String str) {
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        sQLiteQueryBuilder.setTables("account");
        sQLiteQueryBuilder.appendWhere("impi=\"" + str + CmcConstants.E_NUM_STR_QUOTE);
        Cursor query = sQLiteQueryBuilder.query(this.mDbHelper.getReadableDatabase(), new String[]{"account_id"}, (String) null, (String[]) null, (String) null, (String) null, (String) null);
        String str2 = null;
        if (query != null) {
            String str3 = LOG_TAG;
            Log.i(str3, "found " + query.getCount() + " active users");
            if (query.getCount() != 0 && query.moveToFirst()) {
                str2 = query.getString(query.getColumnIndex("account_id"));
            }
            query.close();
        }
        return str2;
    }

    /* access modifiers changed from: package-private */
    public int updateAccountStatus(SQLiteDatabase sQLiteDatabase, Uri uri, ContentValues contentValues) {
        return sQLiteDatabase.update("account", contentValues, "account_id= ? AND userid = ?", new String[]{uri.getLastPathSegment(), String.valueOf(Extensions.ActivityManager.getCurrentUser())});
    }

    /* access modifiers changed from: package-private */
    public long insertAccount(SQLiteDatabase sQLiteDatabase, Uri uri, ContentValues contentValues) {
        contentValues.put("account_id", uri.getLastPathSegment());
        return sQLiteDatabase.insert("account", (String) null, contentValues);
    }

    /* access modifiers changed from: package-private */
    public int updateAddressStatus(SQLiteDatabase sQLiteDatabase, Uri uri, int i) {
        List<String> pathSegments = uri.getPathSegments();
        String str = pathSegments.get(pathSegments.size() - 2);
        String lastPathSegment = uri.getLastPathSegment();
        ContentValues contentValues = new ContentValues();
        if ("0".equals(lastPathSegment) || i > 0) {
            contentValues.put("status", 0);
            sQLiteDatabase.update("address", contentValues, "account_id= ? AND status > ?", new String[]{str, String.valueOf(0)});
        }
        contentValues.clear();
        contentValues.put("status", Integer.valueOf(i));
        int update = sQLiteDatabase.update("address", contentValues, "_id = ?", new String[]{String.valueOf(lastPathSegment)});
        if (update == 1) {
            String str2 = LOG_TAG;
            Log.i(str2, "Update address [" + lastPathSegment + "] status successfully: status:" + i);
        }
        return update;
    }

    /* access modifiers changed from: package-private */
    public int updateDefaultAddressStatus(SQLiteDatabase sQLiteDatabase, Uri uri, int i) {
        List<String> pathSegments = uri.getPathSegments();
        String str = pathSegments.get(pathSegments.size() - 2);
        String lastPathSegment = uri.getLastPathSegment();
        ContentValues contentValues = new ContentValues();
        if (i != 0) {
            contentValues.put(SoftphoneContract.AddressColumns.DEFAULT_STATUS, 0);
            sQLiteDatabase.update("address", contentValues, "account_id= ? AND default_status > ?", new String[]{str, String.valueOf(0)});
        }
        contentValues.clear();
        contentValues.put(SoftphoneContract.AddressColumns.DEFAULT_STATUS, Integer.valueOf(i));
        int update = sQLiteDatabase.update("address", contentValues, "_id = ?", new String[]{String.valueOf(lastPathSegment)});
        if (update == 1) {
            String str2 = LOG_TAG;
            Log.i(str2, "Update address [" + lastPathSegment + "] default status successfully: status:" + i);
        }
        return update;
    }

    public boolean onCreate() {
        Log.i(LOG_TAG, "Starting SoftphoneSettingsProvider");
        this.mContext = getContext();
        this.mDbHelper = new DatabaseHelper(this.mContext);
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        String str3;
        Uri uri2 = uri;
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        String str4 = LOG_TAG;
        Log.i(str4, "query Uri: " + uri + " match: " + sUriMatcher.match(uri));
        switch (sUriMatcher.match(uri)) {
            case 1:
                sQLiteQueryBuilder.setTables("account");
                sQLiteQueryBuilder.appendWhere("userid=" + uri.getLastPathSegment());
                break;
            case 2:
                sQLiteQueryBuilder.setTables("account");
                sQLiteQueryBuilder.appendWhere(SQL_WHERE_ACTIVE_ACCOUNT);
                break;
            case 3:
                sQLiteQueryBuilder.setTables("account");
                sQLiteQueryBuilder.appendWhere(SQL_WHERE_PENDING_ACCOUNT);
                sQLiteQueryBuilder.appendWhere(" AND (userid=" + uri.getLastPathSegment() + ")");
                break;
            case 6:
                sQLiteQueryBuilder.setTables("account");
                sQLiteQueryBuilder.appendWhere("impi=\"" + uri.getLastPathSegment() + CmcConstants.E_NUM_STR_QUOTE);
                break;
            case 7:
                sQLiteQueryBuilder.setTables("account");
                sQLiteQueryBuilder.appendWhere("account_id=\"" + uri.getLastPathSegment() + CmcConstants.E_NUM_STR_QUOTE);
                break;
            case 8:
                sQLiteQueryBuilder.setTables("address");
                break;
            case 9:
                sQLiteQueryBuilder.setTables("address");
                sQLiteQueryBuilder.appendWhere("_id=" + uri.getLastPathSegment());
                break;
            case 10:
                sQLiteQueryBuilder.setTables("address");
                sQLiteQueryBuilder.appendWhere("account_id=\"" + uri.getLastPathSegment() + CmcConstants.E_NUM_STR_QUOTE);
                break;
            case 11:
                sQLiteQueryBuilder.setTables("address");
                sQLiteQueryBuilder.appendWhere("account_id=\"" + uri.getLastPathSegment() + CmcConstants.E_NUM_STR_QUOTE);
                sQLiteQueryBuilder.appendWhere(" AND (status>-1)");
                break;
            case 16:
                sQLiteQueryBuilder.setTables("account");
                sQLiteQueryBuilder.appendWhere("account_id=\"" + uri.getLastPathSegment() + CmcConstants.E_NUM_STR_QUOTE);
                break;
            case 17:
                sQLiteQueryBuilder.setTables("address");
                sQLiteQueryBuilder.appendWhere("account_id=\"" + uri.getLastPathSegment() + CmcConstants.E_NUM_STR_QUOTE);
                sQLiteQueryBuilder.appendWhere(" AND (status=1)");
                break;
            case 18:
                String accountId = getAccountId(uri.getLastPathSegment());
                if (accountId != null) {
                    sQLiteQueryBuilder.setTables("address");
                    sQLiteQueryBuilder.appendWhere("account_id=\"" + accountId + CmcConstants.E_NUM_STR_QUOTE);
                    sQLiteQueryBuilder.appendWhere(" AND (status=1)");
                    break;
                } else {
                    return null;
                }
            case 19:
                sQLiteQueryBuilder.setTables("address");
                sQLiteQueryBuilder.appendWhere("account_id=\"" + uri.getLastPathSegment() + CmcConstants.E_NUM_STR_QUOTE);
                sQLiteQueryBuilder.appendWhere(" AND (default_status=2)");
                sQLiteQueryBuilder.appendWhere(" AND (status>-1)");
                break;
            case 21:
                sQLiteQueryBuilder.setTables("account");
                sQLiteQueryBuilder.appendWhere(SQL_WHERE_FULL_FUNCTIONAL_ACCOUNT);
                break;
            case 22:
                sQLiteQueryBuilder.setTables("account");
                sQLiteQueryBuilder.appendWhere(SQL_WHERE_REGISTERED_ACCOUNT);
                break;
            case 23:
                sQLiteQueryBuilder.setTables("account");
                sQLiteQueryBuilder.appendWhere(SQL_WHERE_ACTIVE_ACCOUNT);
                sQLiteQueryBuilder.appendWhere(" AND (userid=" + uri.getLastPathSegment() + ")");
                break;
            case 25:
                sQLiteQueryBuilder.setTables("account");
                List<String> pathSegments = uri.getPathSegments();
                sQLiteQueryBuilder.appendWhere("account_id=\"" + pathSegments.get(pathSegments.size() - 2) + CmcConstants.E_NUM_STR_QUOTE);
                sQLiteQueryBuilder.appendWhere(" AND (userid=" + uri.getLastPathSegment() + ")");
                break;
            case 26:
                sQLiteQueryBuilder.setTables("account");
                List<String> pathSegments2 = uri.getPathSegments();
                sQLiteQueryBuilder.appendWhere("account_id=\"" + pathSegments2.get(pathSegments2.size() - 2) + CmcConstants.E_NUM_STR_QUOTE);
                sQLiteQueryBuilder.appendWhere(" AND (userid=" + uri.getLastPathSegment() + ")");
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        SQLiteDatabase readableDatabase = this.mDbHelper.getReadableDatabase();
        StringBuilder sb = new StringBuilder();
        sb.append("selection: [");
        String str5 = str;
        sb.append(str5);
        sb.append("], selectionArgs: [");
        sb.append(Arrays.toString(strArr2));
        sb.append("], projection: ");
        sb.append(Arrays.toString(strArr));
        IMSLog.s(str4, sb.toString());
        Cursor query = sQLiteQueryBuilder.query(readableDatabase, strArr, str5, strArr2, (String) null, (String) null, str2);
        if (query == null) {
            str3 = "not found";
        } else {
            str3 = "found : " + query.getCount();
        }
        IMSLog.s(str4, str3);
        return query;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        long j;
        SQLiteDatabase writableDatabase = this.mDbHelper.getWritableDatabase();
        String str = LOG_TAG;
        Log.i(str, "insert Uri: " + uri + " match: " + sUriMatcher.match(uri));
        int match = sUriMatcher.match(uri);
        if (match == 1) {
            j = writableDatabase.insert("account", (String) null, contentValues);
        } else if (match == 8) {
            j = writableDatabase.insert("address", (String) null, contentValues);
        } else if (match == 10) {
            contentValues.put("account_id", uri.getLastPathSegment());
            j = writableDatabase.insert("address", (String) null, contentValues);
        } else if (match == 20) {
            contentValues.put("status", 5);
            j = insertAccount(writableDatabase, uri, contentValues);
        } else if (match == 4) {
            contentValues.put("status", 2);
            j = insertAccount(writableDatabase, uri, contentValues);
        } else if (match == 5) {
            contentValues.put("status", 0);
            j = insertAccount(writableDatabase, uri, contentValues);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Context context = this.mContext;
        if (context != null) {
            context.getContentResolver().notifyChange(uri, (ContentObserver) null);
        }
        return Uri.withAppendedPath(uri, Long.toString(j));
    }

    public int delete(Uri uri, String str, String[] strArr) {
        SQLiteDatabase writableDatabase = this.mDbHelper.getWritableDatabase();
        Log.i(LOG_TAG, "delete Uri: " + uri + " match: " + sUriMatcher.match(uri));
        int match = sUriMatcher.match(uri);
        if (match == 1) {
            return writableDatabase.delete("account", str, strArr);
        }
        if (match != 25) {
            switch (match) {
                case 7:
                    String str2 = "account_id=\"" + uri.getLastPathSegment() + CmcConstants.E_NUM_STR_QUOTE;
                    if (str != null) {
                        str2 = str2 + " AND (" + str + ")";
                    }
                    writableDatabase.delete("address", str2, strArr);
                    return writableDatabase.delete("account", str2, strArr);
                case 8:
                    return writableDatabase.delete("address", str, strArr);
                case 9:
                    return writableDatabase.delete("address", "_id=" + uri.getLastPathSegment(), (String[]) null);
                case 10:
                    return writableDatabase.delete("address", "account_id=\"" + uri.getLastPathSegment() + CmcConstants.E_NUM_STR_QUOTE, (String[]) null);
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } else {
            List<String> pathSegments = uri.getPathSegments();
            String str3 = ("account_id=\"" + pathSegments.get(pathSegments.size() - 2) + CmcConstants.E_NUM_STR_QUOTE) + " AND (" + SoftphoneContract.AccountColumns.USERID + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + uri.getLastPathSegment() + ")";
            if (str != null) {
                str3 = str3 + " AND (" + str + ")";
            }
            writableDatabase.delete("address", str3, strArr);
            return writableDatabase.delete("account", str3, strArr);
        }
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        int i;
        SQLiteDatabase writableDatabase = this.mDbHelper.getWritableDatabase();
        Log.i(LOG_TAG, "update Uri: " + uri + " match: " + sUriMatcher.match(uri));
        switch (sUriMatcher.match(uri)) {
            case 1:
                i = writableDatabase.update("account", contentValues, str, strArr);
                break;
            case 2:
                String str2 = SQL_WHERE_ACTIVE_ACCOUNT;
                if (str != null) {
                    str2 = str2 + " AND (" + str + ")";
                }
                i = writableDatabase.update("account", contentValues, str2, strArr);
                break;
            case 4:
                contentValues.put("status", 2);
                i = updateAccountStatus(writableDatabase, uri, contentValues);
                break;
            case 5:
                contentValues.put("status", 0);
                i = updateAccountStatus(writableDatabase, uri, contentValues);
                break;
            case 7:
                String str3 = "account_id=\"" + uri.getLastPathSegment() + CmcConstants.E_NUM_STR_QUOTE;
                if (str != null) {
                    str3 = str3 + " AND (" + str + ")";
                }
                i = writableDatabase.update("account", contentValues, str3, strArr);
                break;
            case 8:
                i = writableDatabase.update("address", contentValues, str, strArr);
                break;
            case 9:
                String str4 = "_id=" + uri.getLastPathSegment();
                if (str != null) {
                    str4 = str4 + " AND (" + str + ")";
                }
                i = writableDatabase.update("address", contentValues, str4, strArr);
                break;
            case 10:
                String str5 = "account_id=\"" + uri.getLastPathSegment() + CmcConstants.E_NUM_STR_QUOTE;
                if (str != null) {
                    str5 = str5 + " AND (" + str + ")";
                }
                i = writableDatabase.update("address", contentValues, str5, strArr);
                break;
            case 12:
                i = updateAddressStatus(writableDatabase, uri, 1);
                break;
            case 13:
                i = updateAddressStatus(writableDatabase, uri, 0);
                break;
            case 14:
                i = updateDefaultAddressStatus(writableDatabase, uri, 2);
                break;
            case 15:
                i = updateDefaultAddressStatus(writableDatabase, uri, 0);
                break;
            case 16:
                String str6 = "account_id=\"" + uri.getLastPathSegment() + CmcConstants.E_NUM_STR_QUOTE;
                if (str != null) {
                    str6 = str6 + " AND (" + str + ")";
                }
                i = writableDatabase.update("account", contentValues, str6, strArr);
                break;
            case 20:
                contentValues.put("status", 5);
                i = updateAccountStatus(writableDatabase, uri, contentValues);
                break;
            case 21:
                String str7 = SQL_WHERE_FULL_FUNCTIONAL_ACCOUNT;
                if (str != null) {
                    str7 = str7 + " AND (" + str + ")";
                }
                i = writableDatabase.update("account", contentValues, str7, strArr);
                break;
            case 24:
                i = updateAddressStatus(writableDatabase, uri, -1);
                break;
            case 25:
            case 26:
                List<String> pathSegments = uri.getPathSegments();
                String str8 = ("account_id=\"" + pathSegments.get(pathSegments.size() - 2) + CmcConstants.E_NUM_STR_QUOTE) + " AND (" + SoftphoneContract.AccountColumns.USERID + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + uri.getLastPathSegment() + ")";
                if (str != null) {
                    str8 = str8 + " AND (" + str + ")";
                }
                i = writableDatabase.update("account", contentValues, str8, strArr);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Context context = this.mContext;
        if (context != null) {
            context.getContentResolver().notifyChange(uri, (ContentObserver) null);
        }
        return i;
    }

    static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String SQL_CREATE_ACCOUNT = "CREATE TABLE account( account_id TEXT, impi TEXT, msisdn TEXT, access_token TEXT, token_type TEXT, secret_key TEXT, label TEXT, status INTEGER, environment INT DEFAULT -1, userid INT DEFAULT 0 );";
        private static final String SQL_CREATE_ADDRESS = "CREATE TABLE address( _id INTEGER PRIMARY KEY AUTOINCREMENT, account_id TEXT, name TEXT, houseNumber TEXT, houseNumExt TEXT, streetDir TEXT, street TEXT, streetNameSuffix TEXT, streetDirSuffix TEXT, city TEXT, state TEXT, zip TEXT, addressAdditional TEXT, formattedAddress TEXT, E911AID TEXT, expire_date TEXT, status INT DEFAULT 0, default_status INT DEFAULT 0 );";

        public DatabaseHelper(Context context) {
            super(context, SoftphoneSettingsProvider.DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 3);
        }

        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            Log.i(SoftphoneSettingsProvider.LOG_TAG, "Creating DB.");
            sQLiteDatabase.execSQL(SQL_CREATE_ACCOUNT);
            sQLiteDatabase.execSQL(SQL_CREATE_ADDRESS);
        }

        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            String r0 = SoftphoneSettingsProvider.LOG_TAG;
            Log.v(r0, "onUpgrade() oldVersion [" + i + "] , newVersion [" + i2 + "]");
            List<ContentValues> migrateTable = migrateTable(sQLiteDatabase, "account");
            List<ContentValues> migrateTable2 = migrateTable(sQLiteDatabase, "address");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS account");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS address");
            onCreate(sQLiteDatabase);
            upgradeTable(sQLiteDatabase, "account", migrateTable);
            upgradeTable(sQLiteDatabase, "address", migrateTable2);
        }

        /* access modifiers changed from: package-private */
        public List<ContentValues> migrateTable(SQLiteDatabase sQLiteDatabase, String str) {
            ArrayList arrayList = new ArrayList();
            Cursor query = sQLiteDatabase.query(str, (String[]) null, (String) null, (String[]) null, (String) null, (String) null, (String) null);
            if (query != null) {
                while (query.moveToNext()) {
                    ContentValues contentValues = new ContentValues();
                    int columnCount = query.getColumnCount();
                    for (int i = 0; i < columnCount; i++) {
                        String columnName = query.getColumnName(i);
                        String string = query.getString(i);
                        if (!(columnName == null || string == null)) {
                            contentValues.put(columnName, string);
                        }
                    }
                    arrayList.add(contentValues);
                }
                query.close();
            }
            return arrayList;
        }

        /* access modifiers changed from: package-private */
        public void upgradeTable(SQLiteDatabase sQLiteDatabase, String str, List<ContentValues> list) {
            sQLiteDatabase.beginTransaction();
            int i = 0;
            while (i < list.size()) {
                try {
                    sQLiteDatabase.insert(str, (String) null, list.get(i));
                    i++;
                } finally {
                    sQLiteDatabase.endTransaction();
                }
            }
            sQLiteDatabase.setTransactionSuccessful();
        }
    }
}
