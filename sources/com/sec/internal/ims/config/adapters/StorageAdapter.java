package com.sec.internal.ims.config.adapters;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.log.IMSLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class StorageAdapter implements IStorageAdapter {
    public static final String LOG_TAG = "StorageAdapter";
    public static final int STATE_DEFAULT = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_READY = 1;
    private static final Object mLock = new Object();
    protected int mDBTableMax = 10;
    protected String mIdentity;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    protected SQLiteAdapter mSQLite = null;
    State mState = new IdleState();

    public StorageAdapter() {
        IMSLog.i(LOG_TAG, 0, "Init StorageAdapter");
    }

    public void forceDeleteALL(Context context) {
        new SQLiteAdapter(context, (String) null, this.mDBTableMax).forceDeleteAllConfig();
    }

    public String getIdentity() {
        return this.mIdentity;
    }

    public void setDBTableMax(int i) {
        this.mDBTableMax = i;
    }

    public int getState() {
        int state;
        synchronized (mLock) {
            state = this.mState.getState();
        }
        return state;
    }

    public void open(Context context, String str, int i) {
        synchronized (mLock) {
            this.mState.open(context, str, i);
        }
    }

    public String read(String str) {
        String read;
        synchronized (mLock) {
            read = this.mState.read(str);
        }
        return read;
    }

    public Map<String, String> readAll(String str) {
        Map<String, String> readAll;
        synchronized (mLock) {
            readAll = this.mState.readAll(str);
        }
        return readAll;
    }

    public Map<String, String> readAll() {
        Map<String, String> readAll;
        synchronized (mLock) {
            readAll = this.mState.readAll();
        }
        return readAll;
    }

    public boolean write(String str, String str2) {
        boolean write;
        synchronized (mLock) {
            write = this.mState.write(str, str2);
        }
        return write;
    }

    public boolean writeAll(Map<String, String> map) {
        boolean writeAll;
        synchronized (mLock) {
            writeAll = this.mState.writeAll(map);
        }
        return writeAll;
    }

    public int delete(String str) {
        int delete;
        synchronized (mLock) {
            delete = this.mState.delete(str);
        }
        return delete;
    }

    public boolean deleteAll() {
        boolean deleteAll;
        synchronized (mLock) {
            deleteAll = this.mState.deleteAll();
        }
        return deleteAll;
    }

    public void close() {
        synchronized (mLock) {
            this.mState.close();
        }
    }

    public Cursor query(String[] strArr) {
        Cursor query;
        synchronized (mLock) {
            query = this.mState.query(strArr);
        }
        return query;
    }

    abstract class State {
        protected String LOG_TAG = (StorageAdapter.LOG_TAG + this.mStateName);
        protected String mStateName;

        public void close() {
        }

        public int delete(String str) {
            return 0;
        }

        public boolean deleteAll() {
            return false;
        }

        public int getState() {
            return -1;
        }

        public void open(Context context, String str, int i) {
        }

        public Cursor query(String[] strArr) {
            return null;
        }

        public String read(String str) {
            return null;
        }

        public Map<String, String> readAll() {
            return null;
        }

        public Map<String, String> readAll(String str) {
            return null;
        }

        public boolean write(String str, String str2) {
            return false;
        }

        public boolean writeAll(Map<String, String> map) {
            return false;
        }

        public State(String str) {
            this.mStateName = str;
        }
    }

    class IdleState extends State {
        public int getState() {
            return 0;
        }

        public IdleState() {
            super("IDLE");
        }

        public void open(Context context, String str, int i) {
            String str2 = this.LOG_TAG;
            IMSLog.i(str2, i, "open storage : " + str);
            StorageAdapter.this.mPhoneId = i;
            StorageAdapter storageAdapter = StorageAdapter.this;
            storageAdapter.mIdentity = str;
            StorageAdapter storageAdapter2 = StorageAdapter.this;
            storageAdapter.mSQLite = new SQLiteAdapter(context, str, storageAdapter2.mDBTableMax);
            StorageAdapter storageAdapter3 = StorageAdapter.this;
            storageAdapter3.mState = new ReadyState();
        }
    }

    class ReadyState extends State {
        public int getState() {
            return 1;
        }

        public ReadyState() {
            super("Ready");
        }

        public void close() {
            SQLiteAdapter sQLiteAdapter = StorageAdapter.this.mSQLite;
            if (sQLiteAdapter != null) {
                sQLiteAdapter.close();
            }
            StorageAdapter storageAdapter = StorageAdapter.this;
            storageAdapter.mIdentity = "";
            storageAdapter.mState = new IdleState();
        }

        public String read(String str) {
            Map<String, String> read = StorageAdapter.this.mSQLite.read(str);
            if (read.size() == 1) {
                return read.get(str);
            }
            return null;
        }

        public Map<String, String> readAll(String str) {
            return StorageAdapter.this.mSQLite.read(str);
        }

        public Map<String, String> readAll() {
            return StorageAdapter.this.mSQLite.read();
        }

        public boolean write(String str, String str2) {
            TreeMap treeMap = new TreeMap();
            treeMap.put(str, str2);
            return StorageAdapter.this.mSQLite.write(treeMap);
        }

        public boolean writeAll(Map<String, String> map) {
            return StorageAdapter.this.mSQLite.write(map);
        }

        public int delete(String str) {
            return StorageAdapter.this.mSQLite.delete(str);
        }

        public boolean deleteAll() {
            return StorageAdapter.this.mSQLite.deleteAll();
        }

        public Cursor query(String[] strArr) {
            return StorageAdapter.this.mSQLite.query(strArr);
        }
    }

    protected class SQLiteAdapter extends SQLiteOpenHelper {
        private static final String COLUMN1_NAME = "PATH";
        private static final String COLUMN2_NAME = "VALUE";
        private static final String DB_NAME = "config.db";
        private static final int DB_VERSION = 32;
        private static final String PATH_BACKUP = "backup";
        private static final String PATH_INFO = "info";
        private static final String PATH_METADATA_TIMESTAMP = "metadata/timestamp";
        private static final String PATH_OMADM = "omadm";
        private static final String PATH_ROOT = "root";
        private static final String TIMESTAMP_FORMAT = "EEE, dd MMM yyyy HH:mm:ss ZZZZ";
        private final String[] COLUMNS = {COLUMN1_NAME, COLUMN2_NAME};
        private int DB_TABLE_MAX;
        private Context mContext;
        private String mTableName;

        public SQLiteAdapter(Context context, String str, int i) {
            super(context, DB_NAME, (SQLiteDatabase.CursorFactory) null, 32);
            this.mContext = context;
            this.mTableName = str;
            this.DB_TABLE_MAX = i;
            String str2 = StorageAdapter.LOG_TAG;
            int r4 = StorageAdapter.this.mPhoneId;
            IMSLog.i(str2, r4, "config.db: " + this.mTableName + ", DB_TABLE_MAX: " + this.DB_TABLE_MAX);
        }

        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            String str = StorageAdapter.LOG_TAG;
            int r3 = StorageAdapter.this.mPhoneId;
            IMSLog.i(str, r3, "onCreate: table name: " + this.mTableName);
            if (sQLiteDatabase == null) {
                Log.i(str, "db is null. return.");
                return;
            }
            try {
                sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + this.mTableName + " ( " + COLUMN1_NAME + " TEXT PRIMARY KEY," + COLUMN2_NAME + " TEXT )");
                Calendar instance = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault());
                int r5 = StorageAdapter.this.mPhoneId;
                StringBuilder sb = new StringBuilder();
                sb.append("timestamp:");
                sb.append(simpleDateFormat.format(instance.getTime()));
                IMSLog.i(str, r5, sb.toString());
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN1_NAME, PATH_METADATA_TIMESTAMP);
                contentValues.put(COLUMN2_NAME, String.valueOf(instance.getTimeInMillis()));
                String str2 = this.mTableName;
                if (str2 != null && str2.matches("OMADM_\\w+_\\d")) {
                    String replaceFirst = this.mTableName.replaceFirst("_\\d", "");
                    if (!isTable(sQLiteDatabase, replaceFirst)) {
                        IMSLog.d(str, StorageAdapter.this.mPhoneId, "No old DB exists");
                    } else {
                        IMSLog.i(str, StorageAdapter.this.mPhoneId, String.format("onCreate: Copy the old table [%s] to new one [%s]", new Object[]{replaceFirst, this.mTableName}));
                        sQLiteDatabase.execSQL("INSERT INTO " + this.mTableName + " SELECT * FROM " + replaceFirst);
                    }
                }
                sQLiteDatabase.insertWithOnConflict(this.mTableName, (String) null, contentValues, 5);
            } catch (SQLiteException e) {
                Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                e.printStackTrace();
            }
        }

        public void onOpen(SQLiteDatabase sQLiteDatabase) {
            String str = StorageAdapter.LOG_TAG;
            int r1 = StorageAdapter.this.mPhoneId;
            IMSLog.i(str, r1, "onOpen: table name: " + this.mTableName);
            sQLiteDatabase.beginTransaction();
            try {
                if (!isTable(sQLiteDatabase, this.mTableName)) {
                    int r12 = StorageAdapter.this.mPhoneId;
                    IMSLog.i(str, r12, "onOpen: no table " + this.mTableName + " found. Create.");
                    onCreate(sQLiteDatabase);
                }
                List<String> tables = getTables(sQLiteDatabase);
                if (tables.size() > this.DB_TABLE_MAX) {
                    deleteOldTables(sQLiteDatabase, tables);
                }
                sQLiteDatabase.setTransactionSuccessful();
            } catch (SQLiteCantOpenDatabaseException e) {
                e.printStackTrace();
                IMSLog.i(StorageAdapter.LOG_TAG, StorageAdapter.this.mPhoneId, "unable to open database file");
                onCreate(sQLiteDatabase);
            } catch (Exception e2) {
                e2.printStackTrace();
                IMSLog.i(StorageAdapter.LOG_TAG, StorageAdapter.this.mPhoneId, "delete all tables");
                for (String deleteTable : getTables(sQLiteDatabase)) {
                    deleteTable(sQLiteDatabase, deleteTable);
                }
                onCreate(sQLiteDatabase);
            } catch (Throwable th) {
                endTransaction(sQLiteDatabase);
                throw th;
            }
            endTransaction(sQLiteDatabase);
        }

        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            String str = StorageAdapter.LOG_TAG;
            int r2 = StorageAdapter.this.mPhoneId;
            IMSLog.i(str, r2, "onUpgrade(): [" + i + "] -> [" + i2 + "]");
        }

        /* JADX WARNING: Removed duplicated region for block: B:18:0x0072 A[Catch:{ all -> 0x0093, all -> 0x0098, SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }] */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0078 A[SYNTHETIC, Splitter:B:21:0x0078] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public java.util.Map<java.lang.String, java.lang.String> read(java.lang.String r12) {
            /*
                r11 = this;
                java.util.TreeMap r0 = new java.util.TreeMap
                java.util.Comparator r1 = java.lang.String.CASE_INSENSITIVE_ORDER
                r0.<init>(r1)
                if (r12 != 0) goto L_0x000a
                return r0
            L_0x000a:
                java.util.Locale r1 = java.util.Locale.US
                java.lang.String r12 = r12.toLowerCase(r1)
                android.database.sqlite.SQLiteDatabase r1 = r11.getReadableDatabase()     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                java.lang.String r2 = "root"
                boolean r2 = r12.startsWith(r2)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                if (r2 != 0) goto L_0x003a
                java.lang.String r2 = "info"
                boolean r2 = r12.startsWith(r2)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                if (r2 != 0) goto L_0x003a
                java.lang.String r2 = "backup"
                boolean r2 = r12.startsWith(r2)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                if (r2 != 0) goto L_0x003a
                java.lang.String r2 = "omadm"
                boolean r2 = r12.startsWith(r2)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                if (r2 == 0) goto L_0x0036
                goto L_0x003a
            L_0x0036:
                java.lang.String r2 = "root/"
                goto L_0x003c
            L_0x003a:
                java.lang.String r2 = ""
            L_0x003c:
                java.lang.String r3 = r11.mTableName     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                java.lang.String[] r11 = r11.COLUMNS     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                java.lang.String r4 = "PATH LIKE ?  ESCAPE '\\'"
                r9 = 1
                java.lang.String[] r5 = new java.lang.String[r9]     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                r6.<init>()     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                r6.append(r2)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                java.lang.String r2 = "*"
                java.lang.String r7 = "%"
                java.lang.String r12 = r12.replace(r2, r7)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                java.lang.String r2 = "_"
                java.lang.String r7 = "\\_"
                java.lang.String r12 = r12.replace(r2, r7)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                r6.append(r12)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                java.lang.String r12 = r6.toString()     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                r10 = 0
                r5[r10] = r12     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                r6 = 0
                r7 = 0
                r8 = 0
                r2 = r3
                r3 = r11
                android.database.Cursor r11 = r1.query(r2, r3, r4, r5, r6, r7, r8)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                if (r11 != 0) goto L_0x0078
                if (r11 == 0) goto L_0x0077
                r11.close()     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
            L_0x0077:
                return r0
            L_0x0078:
                boolean r12 = r11.moveToFirst()     // Catch:{ all -> 0x0093 }
                if (r12 == 0) goto L_0x008f
            L_0x007e:
                java.lang.String r12 = r11.getString(r10)     // Catch:{ all -> 0x0093 }
                java.lang.String r1 = r11.getString(r9)     // Catch:{ all -> 0x0093 }
                r0.put(r12, r1)     // Catch:{ all -> 0x0093 }
                boolean r12 = r11.moveToNext()     // Catch:{ all -> 0x0093 }
                if (r12 != 0) goto L_0x007e
            L_0x008f:
                r11.close()     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
                goto L_0x00b0
            L_0x0093:
                r12 = move-exception
                r11.close()     // Catch:{ all -> 0x0098 }
                goto L_0x009c
            L_0x0098:
                r11 = move-exception
                r12.addSuppressed(r11)     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
            L_0x009c:
                throw r12     // Catch:{ SQLiteCantOpenDatabaseException -> 0x00a9, SQLiteException -> 0x009d }
            L_0x009d:
                r11 = move-exception
                java.lang.String r12 = com.sec.internal.ims.config.adapters.StorageAdapter.LOG_TAG
                java.lang.String r1 = "SQLiteException!"
                android.util.Log.i(r12, r1)
                r11.printStackTrace()
                goto L_0x00b0
            L_0x00a9:
                java.lang.String r11 = com.sec.internal.ims.config.adapters.StorageAdapter.LOG_TAG
                java.lang.String r12 = "Can not read DB now!"
                android.util.Log.i(r11, r12)
            L_0x00b0:
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.adapters.StorageAdapter.SQLiteAdapter.read(java.lang.String):java.util.Map");
        }

        public Map<String, String> read() {
            Cursor query;
            TreeMap treeMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            try {
                query = getReadableDatabase().query(this.mTableName, (String[]) null, (String) null, (String[]) null, (String) null, (String) null, (String) null);
                if (query == null) {
                    if (query != null) {
                        query.close();
                    }
                    return treeMap;
                }
                if (query.moveToFirst()) {
                    do {
                        treeMap.put(query.getString(0), query.getString(1));
                    } while (query.moveToNext());
                }
                query.close();
                return treeMap;
            } catch (SQLiteCantOpenDatabaseException unused) {
                Log.e(StorageAdapter.LOG_TAG, "Can not read DB now!");
            } catch (SQLiteException e) {
                Log.e(StorageAdapter.LOG_TAG, "SQLiteException!");
                e.printStackTrace();
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
            throw th;
        }

        public boolean write(Map<String, String> map) {
            if (map == null) {
                Log.i(StorageAdapter.LOG_TAG, "data is null!");
                return false;
            }
            try {
                SQLiteDatabase writableDatabase = getWritableDatabase();
                try {
                    SQLiteStatement compileStatement = writableDatabase.compileStatement("INSERT OR REPLACE INTO " + this.mTableName + " VALUES (?,?);");
                    writableDatabase.beginTransaction();
                    for (Map.Entry next : map.entrySet()) {
                        compileStatement.clearBindings();
                        compileStatement.bindString(1, (String) next.getKey());
                        compileStatement.bindString(2, (String) next.getValue());
                        compileStatement.execute();
                    }
                    writableDatabase.setTransactionSuccessful();
                } catch (SQLiteException e) {
                    Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                    e.printStackTrace();
                } catch (Throwable th) {
                    endTransaction(writableDatabase);
                    throw th;
                }
                endTransaction(writableDatabase);
                for (Map.Entry next2 : map.entrySet()) {
                    if (this.mTableName.startsWith("OMADM")) {
                        ContentResolver contentResolver = this.mContext.getContentResolver();
                        contentResolver.notifyChange(UriUtil.buildUri("content://com.samsung.rcs.dmconfigurationprovider/" + ((String) next2.getKey()), StorageAdapter.this.mPhoneId), (ContentObserver) null);
                    } else {
                        this.mContext.getContentResolver().notifyChange(UriUtil.buildUri(ConfigConstants.CONTENT_URI.buildUpon().appendPath((String) next2.getKey()).build().toString(), StorageAdapter.this.mPhoneId), (ContentObserver) null);
                    }
                }
                return true;
            } catch (SQLiteDiskIOException e2) {
                String str = StorageAdapter.LOG_TAG;
                Log.i(str, "SQLiteDiskIOException : " + e2.toString());
                return false;
            } catch (SQLiteException e3) {
                e3.printStackTrace();
                return false;
            }
        }

        public int delete(String str) {
            String str2 = StorageAdapter.LOG_TAG;
            Log.i(str2, "delete: " + str);
            try {
                return getWritableDatabase().delete(this.mTableName, "PATH = ?", new String[]{str});
            } catch (SQLiteDiskIOException e) {
                String str3 = StorageAdapter.LOG_TAG;
                Log.i(str3, "SQLiteDiskIOException : " + e.toString());
                return 0;
            } catch (SQLiteException e2) {
                Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                e2.printStackTrace();
                return 0;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x004c, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x004d, code lost:
            r0 = com.sec.internal.ims.config.adapters.StorageAdapter.LOG_TAG;
            android.util.Log.i(r0, "SQLiteDiskIOException : " + r4.toString());
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0068, code lost:
            return false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:6:0x003b, code lost:
            r1 = e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:7:0x003c, code lost:
            r0 = null;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Removed duplicated region for block: B:11:0x004c A[ExcHandler: SQLiteDiskIOException (r4v1 'e' android.database.sqlite.SQLiteDiskIOException A[CUSTOM_DECLARE]), Splitter:B:1:0x001e] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean deleteAll() {
            /*
                r4 = this;
                java.lang.String r0 = com.sec.internal.ims.config.adapters.StorageAdapter.LOG_TAG
                com.sec.internal.ims.config.adapters.StorageAdapter r1 = com.sec.internal.ims.config.adapters.StorageAdapter.this
                int r1 = r1.mPhoneId
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = "drop table:"
                r2.append(r3)
                java.lang.String r3 = r4.mTableName
                r2.append(r3)
                java.lang.String r2 = r2.toString()
                com.sec.internal.log.IMSLog.i(r0, r1, r2)
                android.database.sqlite.SQLiteDatabase r0 = r4.getWritableDatabase()     // Catch:{ SQLiteDiskIOException -> 0x004c, SQLiteException -> 0x003b }
                java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ SQLiteDiskIOException -> 0x004c, SQLiteException -> 0x0039 }
                r1.<init>()     // Catch:{ SQLiteDiskIOException -> 0x004c, SQLiteException -> 0x0039 }
                java.lang.String r2 = "DROP TABLE IF EXISTS "
                r1.append(r2)     // Catch:{ SQLiteDiskIOException -> 0x004c, SQLiteException -> 0x0039 }
                java.lang.String r2 = r4.mTableName     // Catch:{ SQLiteDiskIOException -> 0x004c, SQLiteException -> 0x0039 }
                r1.append(r2)     // Catch:{ SQLiteDiskIOException -> 0x004c, SQLiteException -> 0x0039 }
                java.lang.String r1 = r1.toString()     // Catch:{ SQLiteDiskIOException -> 0x004c, SQLiteException -> 0x0039 }
                r0.execSQL(r1)     // Catch:{ SQLiteDiskIOException -> 0x004c, SQLiteException -> 0x0039 }
                goto L_0x0047
            L_0x0039:
                r1 = move-exception
                goto L_0x003d
            L_0x003b:
                r1 = move-exception
                r0 = 0
            L_0x003d:
                java.lang.String r2 = com.sec.internal.ims.config.adapters.StorageAdapter.LOG_TAG
                java.lang.String r3 = "SQLiteException!"
                android.util.Log.i(r2, r3)
                r1.printStackTrace()
            L_0x0047:
                r4.onCreate(r0)
                r4 = 1
                return r4
            L_0x004c:
                r4 = move-exception
                java.lang.String r0 = com.sec.internal.ims.config.adapters.StorageAdapter.LOG_TAG
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                r1.<init>()
                java.lang.String r2 = "SQLiteDiskIOException : "
                r1.append(r2)
                java.lang.String r4 = r4.toString()
                r1.append(r4)
                java.lang.String r4 = r1.toString()
                android.util.Log.i(r0, r4)
                r4 = 0
                return r4
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.adapters.StorageAdapter.SQLiteAdapter.deleteAll():boolean");
        }

        public boolean forceDeleteAllConfig() {
            ArrayList arrayList = new ArrayList();
            try {
                SQLiteDatabase writableDatabase = getWritableDatabase();
                for (String next : getTables(writableDatabase)) {
                    writableDatabase.execSQL("DROP TABLE IF EXISTS " + next);
                    writableDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + next + " ( " + COLUMN1_NAME + " TEXT PRIMARY KEY," + COLUMN2_NAME + " TEXT )");
                    Calendar instance = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault());
                    String str = StorageAdapter.LOG_TAG;
                    int r10 = StorageAdapter.this.mPhoneId;
                    StringBuilder sb = new StringBuilder();
                    sb.append("timestamp:");
                    sb.append(simpleDateFormat.format(instance.getTime()));
                    IMSLog.i(str, r10, sb.toString());
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(COLUMN1_NAME, PATH_METADATA_TIMESTAMP);
                    contentValues.put(COLUMN2_NAME, String.valueOf(instance.getTimeInMillis()));
                    writableDatabase.insertWithOnConflict(this.mTableName, (String) null, contentValues, 5);
                    arrayList.add(next);
                }
                String str2 = StorageAdapter.LOG_TAG;
                Log.i(str2, "forceDeleteAllConfig: removed tables: " + arrayList);
                return true;
            } catch (SQLiteDiskIOException e) {
                String str3 = StorageAdapter.LOG_TAG;
                Log.i(str3, "SQLiteDiskIOException : " + e.toString());
                return false;
            } catch (SQLiteException e2) {
                Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                e2.printStackTrace();
                return false;
            }
        }

        public Cursor query(String[] strArr) {
            StringBuffer stringBuffer = new StringBuffer();
            if (strArr != null) {
                stringBuffer.append("PATH=?");
                for (int i = 1; i < strArr.length; i++) {
                    stringBuffer.append(" OR PATH=?");
                }
            }
            try {
                return getReadableDatabase().query(this.mTableName, this.COLUMNS, stringBuffer.toString(), strArr, (String) null, (String) null, (String) null);
            } catch (SQLiteException e) {
                Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                e.printStackTrace();
                return null;
            }
        }

        private boolean isTable(SQLiteDatabase sQLiteDatabase, String str) {
            Cursor rawQuery;
            boolean z = false;
            try {
                rawQuery = sQLiteDatabase.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type=? AND name=?", new String[]{"table", str});
                if (rawQuery != null) {
                    rawQuery.moveToFirst();
                    if (rawQuery.getInt(0) != 0) {
                        z = true;
                    }
                }
                if (rawQuery != null) {
                    rawQuery.close();
                }
            } catch (SQLiteException e) {
                Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                e.printStackTrace();
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
            return z;
            throw th;
        }

        /* access modifiers changed from: package-private */
        public List<String> getTables(SQLiteDatabase sQLiteDatabase) {
            ArrayList arrayList = new ArrayList();
            Cursor rawQuery = sQLiteDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type=?", new String[]{"table"});
            if (rawQuery != null) {
                try {
                    if (rawQuery.moveToFirst()) {
                        while (rawQuery.moveToNext()) {
                            arrayList.add(rawQuery.getString(0));
                        }
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (rawQuery != null) {
                rawQuery.close();
            }
            return arrayList;
            throw th;
        }

        /* access modifiers changed from: package-private */
        public void deleteTable(SQLiteDatabase sQLiteDatabase, String str) {
            try {
                sQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + str);
            } catch (SQLiteException e) {
                Log.i(StorageAdapter.LOG_TAG, "SQLiteException!");
                e.printStackTrace();
            }
        }

        /* access modifiers changed from: package-private */
        public String readTable(SQLiteDatabase sQLiteDatabase, String str, String str2) {
            Cursor query;
            String str3 = null;
            try {
                query = sQLiteDatabase.query(str, this.COLUMNS, "PATH = ?", new String[]{str2}, (String) null, (String) null, (String) null);
                if (query != null) {
                    if (query.moveToFirst()) {
                        str3 = query.getString(1);
                    }
                }
                if (query != null) {
                    query.close();
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
            return str3;
            throw th;
        }

        /* access modifiers changed from: package-private */
        public void deleteOldTables(SQLiteDatabase sQLiteDatabase, List<String> list) {
            String str = StorageAdapter.LOG_TAG;
            Log.i(str, "over table limit. remove old tables");
            TreeMap treeMap = new TreeMap();
            Log.i(str, "deleteOldTables: current tables: " + list);
            for (String next : list) {
                String readTable = readTable(sQLiteDatabase, next, PATH_METADATA_TIMESTAMP);
                if (readTable != null && !next.startsWith("OMADM")) {
                    treeMap.put(Long.valueOf(readTable), next);
                }
            }
            int size = treeMap.size() - this.DB_TABLE_MAX;
            if (size >= 1) {
                ArrayList arrayList = new ArrayList();
                for (Map.Entry value : treeMap.entrySet()) {
                    String str2 = (String) value.getValue();
                    if (!this.mTableName.equals(str2)) {
                        arrayList.add(str2);
                        deleteTable(sQLiteDatabase, str2);
                        int i = size - 1;
                        if (size <= 0) {
                            break;
                        }
                        size = i;
                    }
                }
                Log.i(StorageAdapter.LOG_TAG, "deleteOldTables: removed tables: " + arrayList);
            }
        }

        /* access modifiers changed from: package-private */
        public void endTransaction(SQLiteDatabase sQLiteDatabase) {
            if (sQLiteDatabase == null) {
                Log.i(StorageAdapter.LOG_TAG, "endTransaction: db is null");
                return;
            }
            try {
                sQLiteDatabase.endTransaction();
            } catch (SQLException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }
}
