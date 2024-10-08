package com.sec.internal.ims.servicemodules.presence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PresenceStorage {
    private static final String LOG_TAG = "PresenceStorage";
    private static final String[] PRESENCE_PROJECTION = {"_id", CloudMessageProviderContract.BufferDBMMSaddr.CONTACT_ID, "tel_uri", "uri", "timestamp", "phone_id"};
    private PresenceCache mCache;
    private Context mContext;
    private DatabaseHelper mDbHelper;
    private int mPhoneId = 0;

    public static class PresenceTable {
        static final String CONTACT_ID = "contact_id";
        static final String PHONE_ID = "phone_id";
        static final String TABLE_NAME = "presence";
        static final String TEL_URI = "tel_uri";
        static final String TIMESTAMP = "timestamp";
        static final String URI = "uri";
        static final String _ID = "_id";
    }

    public PresenceStorage(Context context, PresenceCache presenceCache, int i) {
        this.mContext = context;
        this.mCache = presenceCache;
        this.mDbHelper = new DatabaseHelper(this.mContext);
        this.mPhoneId = i;
    }

    public void persist() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "persist:");
        List<ImsUri> updatedUriList = this.mCache.getUpdatedUriList();
        List<ImsUri> trashedUriList = this.mCache.getTrashedUriList();
        try {
            SQLiteDatabase writableDatabase = this.mDbHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            if (updatedUriList != null) {
                try {
                    if (updatedUriList.size() > 0) {
                        ArrayList arrayList = new ArrayList();
                        for (ImsUri imsUri : updatedUriList) {
                            PresenceInfo presenceInfo = this.mCache.get(imsUri);
                            if (presenceInfo != null) {
                                arrayList.add(presenceInfo);
                            } else {
                                Log.d(LOG_TAG, "persist: not found in cache.");
                            }
                        }
                        update(writableDatabase, arrayList);
                    }
                } catch (SQLiteFullException e) {
                    Log.e(LOG_TAG, "persist: SQLiteFullException: " + e.toString());
                } catch (SQLiteDiskIOException e2) {
                    Log.e(LOG_TAG, "persist: SQLiteDiskIOException: " + e2.toString());
                } catch (SQLException e3) {
                    Log.e(LOG_TAG, "persist: SQLException: " + e3.toString());
                } catch (Throwable th) {
                    endTransaction(writableDatabase);
                    throw th;
                }
            }
            if (trashedUriList != null && trashedUriList.size() > 0) {
                remove(writableDatabase, trashedUriList);
            }
            writableDatabase.setTransactionSuccessful();
            endTransaction(writableDatabase);
        } catch (SQLiteDiskIOException e4) {
            Log.e(LOG_TAG, "persist: SQLiteDiskIOException: " + e4.toString());
        }
    }

    public void reset() {
        Log.i(LOG_TAG, "reset:");
        try {
            this.mDbHelper.getWritableDatabase().delete(SipMsg.EVENT_PRESENCE, "phone_id = ?", new String[]{String.valueOf(this.mPhoneId)});
        } catch (SQLiteDiskIOException e) {
            Log.e(LOG_TAG, "reset: SQLiteDiskIOException: " + e.toString());
        }
    }

    private void remove(SQLiteDatabase sQLiteDatabase, List<ImsUri> list) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "remove: " + list.size() + " uris");
        for (ImsUri imsUri : list) {
            sQLiteDatabase.delete(SipMsg.EVENT_PRESENCE, "tel_uri = ? AND phone_id = ?", new String[]{imsUri.toString(), String.valueOf(this.mPhoneId)});
        }
    }

    private void update(SQLiteDatabase sQLiteDatabase, Collection<PresenceInfo> collection) {
        ContentValues contentValues = new ContentValues();
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "update: " + collection.size() + " PresenceInfo");
        for (PresenceInfo next : collection) {
            contentValues.clear();
            fillPresenceInfo(contentValues, next);
            if (next.getId() < 0 || next.getPhoneId() != this.mPhoneId) {
                next.setId(sQLiteDatabase.insert(SipMsg.EVENT_PRESENCE, (String) null, contentValues));
            } else {
                sQLiteDatabase.update(SipMsg.EVENT_PRESENCE, contentValues, "_id = ?", new String[]{String.valueOf(next.getId())});
            }
        }
    }

    private void fillPresenceInfo(ContentValues contentValues, PresenceInfo presenceInfo) {
        if (presenceInfo.getContactId() != null) {
            contentValues.put(CloudMessageProviderContract.BufferDBMMSaddr.CONTACT_ID, presenceInfo.getContactId());
        }
        contentValues.put("tel_uri", presenceInfo.getTelUri());
        contentValues.put("uri", presenceInfo.getUri());
        contentValues.put("timestamp", Long.valueOf(presenceInfo.getTimestamp()));
        contentValues.put("phone_id", Integer.valueOf(presenceInfo.getPhoneId()));
    }

    private PresenceInfo fillPresenceInfo(Cursor cursor) {
        PresenceInfo build = new PresenceInfo.Builder().tel_uri(cursor.getString(cursor.getColumnIndex("tel_uri"))).uri(cursor.getString(cursor.getColumnIndex("uri"))).timestamp(cursor.getLong(cursor.getColumnIndex("timestamp"))).phoneId(cursor.getInt(cursor.getColumnIndex("phone_id"))).build();
        build.setId((long) cursor.getInt(cursor.getColumnIndex("_id")));
        return build;
    }

    public PresenceInfo get(ImsUri imsUri) {
        PresenceInfo presenceInfo = null;
        if (imsUri == null) {
            return null;
        }
        int i = this.mPhoneId;
        IMSLog.s(LOG_TAG, i, "get: teluri-" + imsUri);
        String[] strArr = {imsUri.toString(), String.valueOf(this.mPhoneId)};
        try {
            Cursor query = this.mDbHelper.getReadableDatabase().query(SipMsg.EVENT_PRESENCE, PRESENCE_PROJECTION, "tel_uri = ? AND phone_id = ?", strArr, (String) null, (String) null, (String) null);
            try {
                if (query.getCount() > 0) {
                    query.moveToFirst();
                    presenceInfo = fillPresenceInfo(query);
                }
                query.close();
                return presenceInfo;
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } catch (SQLiteDiskIOException e) {
            Log.e(LOG_TAG, "get: SQLiteDiskIOException: " + e.toString());
            return null;
        }
        throw th;
    }

    public Map<ImsUri, PresenceInfo> get(Set<ImsUri> set) {
        if (set == null) {
            return null;
        }
        int size = set.size();
        IMSLog.s(LOG_TAG, this.mPhoneId, "get: querying " + size + " telUris");
        if (size == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("tel_uri");
        sb.append(" in (");
        for (ImsUri imsUri : set) {
            sb.append("'" + imsUri.toString() + "'");
            size += -1;
            if (size > 0) {
                sb.append(",");
            }
        }
        sb.append(")");
        String str = sb.toString() + " AND " + "phone_id" + " = ?";
        String[] strArr = {String.valueOf(this.mPhoneId)};
        IMSLog.s(LOG_TAG, this.mPhoneId, "get: selection = " + str);
        HashMap hashMap = new HashMap();
        try {
            Cursor query = this.mDbHelper.getReadableDatabase().query(SipMsg.EVENT_PRESENCE, PRESENCE_PROJECTION, str, strArr, (String) null, (String) null, (String) null);
            if (query != null) {
                try {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "get: presenceInfo " + query.getCount() + " from DB");
                    if (query.getCount() > 0) {
                        query.moveToFirst();
                        do {
                            PresenceInfo fillPresenceInfo = fillPresenceInfo(query);
                            hashMap.put(ImsUri.parse(fillPresenceInfo.getTelUri()), fillPresenceInfo);
                        } while (query.moveToNext());
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (query != null) {
                query.close();
            }
            return hashMap;
        } catch (SQLiteDiskIOException e) {
            Log.e(LOG_TAG, "get: SQLiteDiskIOException: " + e.toString());
            return hashMap;
        }
        throw th;
    }

    private void endTransaction(SQLiteDatabase sQLiteDatabase) {
        if (sQLiteDatabase == null) {
            Log.e(LOG_TAG, "endTransaction: db is null");
            return;
        }
        try {
            sQLiteDatabase.endTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "presence.db";
        private static final int DATABASE_VERSION = 6;
        private static final String SQL_CREATE_INDEX_TEL_URI = "CREATE INDEX idx_tel_uri ON presence (tel_uri);";
        private static final String SQL_CREATE_PRESENCE_TABLE = "CREATE TABLE presence( _id INTEGER PRIMARY KEY, contact_id TEXT, tel_uri TEXT, uri TEXT, timestamp BIGINT DEFAULT 0, phone_id INT );";

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 6);
        }

        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            Log.i(PresenceStorage.LOG_TAG, "onCreate: Creating DB.");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS presence");
            sQLiteDatabase.execSQL(SQL_CREATE_PRESENCE_TABLE);
            sQLiteDatabase.execSQL(SQL_CREATE_INDEX_TEL_URI);
        }

        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            Log.i(PresenceStorage.LOG_TAG, "onUpgrade: oldVersion [" + i + "] , newVersion [" + i2 + "]");
            if (sQLiteDatabase.getVersion() != i2) {
                List<ContentValues> migrateTable = migrateTable(sQLiteDatabase);
                onCreate(sQLiteDatabase);
                sQLiteDatabase.setVersion(i2);
                upgradeTable(sQLiteDatabase, migrateTable);
            }
            Log.i(PresenceStorage.LOG_TAG, "onUpgrade: done");
        }

        public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            Log.v(PresenceStorage.LOG_TAG, "onDowngrade() oldVersion [" + i + "] , newVersion [" + i2 + "]");
            if (sQLiteDatabase.getVersion() != i2) {
                onCreate(sQLiteDatabase);
                sQLiteDatabase.setVersion(6);
            }
        }

        private List<ContentValues> migrateTable(SQLiteDatabase sQLiteDatabase) {
            Log.i(PresenceStorage.LOG_TAG, "migrateTable");
            ArrayList arrayList = new ArrayList();
            Cursor query = sQLiteDatabase.query(SipMsg.EVENT_PRESENCE, (String[]) null, (String) null, (String[]) null, (String) null, (String) null, (String) null);
            if (query != null) {
                while (query.moveToNext()) {
                    try {
                        ContentValues contentValues = new ContentValues();
                        for (int i = 0; i < query.getColumnCount(); i++) {
                            String columnName = query.getColumnName(i);
                            String string = query.getString(i);
                            if (!(columnName == null || string == null || "hyper".equals(columnName))) {
                                contentValues.put(columnName, string);
                            }
                        }
                        arrayList.add(contentValues);
                    } catch (Throwable th) {
                        th.addSuppressed(th);
                    }
                }
            }
            if (query != null) {
                query.close();
            }
            return arrayList;
            throw th;
        }

        private void upgradeTable(SQLiteDatabase sQLiteDatabase, List<ContentValues> list) {
            Log.i(PresenceStorage.LOG_TAG, "upgradeTable");
            sQLiteDatabase.beginTransaction();
            int i = 0;
            while (i < list.size()) {
                try {
                    sQLiteDatabase.insert(SipMsg.EVENT_PRESENCE, (String) null, list.get(i));
                    i++;
                } catch (SQLException e) {
                    Log.e(PresenceStorage.LOG_TAG, "upgradeTable: SQLException " + e);
                } catch (Throwable th) {
                    sQLiteDatabase.endTransaction();
                    throw th;
                }
            }
            sQLiteDatabase.setTransactionSuccessful();
            sQLiteDatabase.endTransaction();
        }
    }
}
