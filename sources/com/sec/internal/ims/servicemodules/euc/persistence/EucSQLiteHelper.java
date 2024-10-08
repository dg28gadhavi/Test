package com.sec.internal.ims.servicemodules.euc.persistence;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;
import com.sec.internal.log.IMSLog;

class EucSQLiteHelper extends SQLiteOpenHelper {
    private static final String CREATE_DIALOG_TABLE = "CREATE TABLE IF NOT EXISTS DIALOG (ID TEXT,LANGUAGE TEXT,SUBJECT TEXT,TEXT TEXT,ACCEPT_BUTTON TEXT,REJECT_BUTTON TEXT,SUBSCRIBER_IDENTITY TEXT, TYPE INTEGER, REMOTE_URI TEXT, PRIMARY KEY(ID,TYPE,LANGUAGE,SUBSCRIBER_IDENTITY,REMOTE_URI), FOREIGN KEY(ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI) REFERENCES EUCRDATA(ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI) ON DELETE CASCADE);";
    private static final String CREATE_EUCR_DATA_TABLE = "CREATE TABLE IF NOT EXISTS EUCRDATA ( ID TEXT,PIN INTEGER,EXTERNAL INTEGER,STATE INTEGER,TYPE INTEGER,REMOTE_URI TEXT,TIMESTAMP INTEGER,TIMEOUT INTEGER,USER_PIN TEXT,SUBSCRIBER_IDENTITY TEXT, PRIMARY KEY (ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI));";
    private static final String DB_NAME = "eucr.db";
    private static final int DB_VERSION = 3;
    static final String DIALOG_COLUMN_ACCEPT_BUTTON = "ACCEPT_BUTTON";
    static final int DIALOG_COLUMN_ACCEPT_BUTTON_INDEX = 4;
    static final String DIALOG_COLUMN_ID = "ID";
    static final int DIALOG_COLUMN_ID_INDEX = 0;
    static final String DIALOG_COLUMN_LANGUAGE = "LANGUAGE";
    static final int DIALOG_COLUMN_LANGUAGE_INDEX = 1;
    static final String DIALOG_COLUMN_REJECT_BUTTON = "REJECT_BUTTON";
    static final int DIALOG_COLUMN_REJECT_BUTTON_INDEX = 5;
    static final String DIALOG_COLUMN_REMOTE_URI = "REMOTE_URI";
    static final int DIALOG_COLUMN_REMOTE_URI_INDEX = 8;
    static final String DIALOG_COLUMN_SUBJECT = "SUBJECT";
    static final int DIALOG_COLUMN_SUBJECT_INDEX = 2;
    static final String DIALOG_COLUMN_SUBSCRIBER_IDENTITY = "SUBSCRIBER_IDENTITY";
    static final int DIALOG_COLUMN_SUBSCRIBER_IDENTITY_INDEX = 6;
    static final String DIALOG_COLUMN_TEXT = "TEXT";
    static final int DIALOG_COLUMN_TEXT_INDEX = 3;
    static final String DIALOG_COLUMN_TYPE = "TYPE";
    static final int DIALOG_COLUMN_TYPE_INDEX = 7;
    static final String DIALOG_TABLE_NAME = "DIALOG";
    private static final String DROP_DIALOG_TABLE = "DROP TABLE IF EXISTS DIALOG;";
    private static final String DROP_EUCR_DATA_TABLE = "DROP TABLE IF EXISTS EUCRDATA;";
    static final String EUCRDATA_COLUMN_EXTERNAL = "EXTERNAL";
    static final int EUCRDATA_COLUMN_EXTERNAL_INDEX = 2;
    static final String EUCRDATA_COLUMN_ID = "ID";
    static final int EUCRDATA_COLUMN_ID_INDEX = 0;
    static final String EUCRDATA_COLUMN_PIN = "PIN";
    static final int EUCRDATA_COLUMN_PIN_INDEX = 1;
    static final String EUCRDATA_COLUMN_REMOTE_URI = "REMOTE_URI";
    static final int EUCRDATA_COLUMN_REMOTE_URI_INDEX = 5;
    static final String EUCRDATA_COLUMN_ROWID = "ROWID";
    static final String EUCRDATA_COLUMN_STATE = "STATE";
    static final int EUCRDATA_COLUMN_STATE_INDEX = 3;
    static final String EUCRDATA_COLUMN_SUBSCRIBER_IDENTITY = "SUBSCRIBER_IDENTITY";
    static final int EUCRDATA_COLUMN_SUBSCRIBER_IDENTITY_INDEX = 9;
    static final String EUCRDATA_COLUMN_TIMEOUT = "TIMEOUT";
    static final int EUCRDATA_COLUMN_TIMEOUT_INDEX = 7;
    static final String EUCRDATA_COLUMN_TIMESTAMP = "TIMESTAMP";
    static final int EUCRDATA_COLUMN_TIMESTAMP_INDEX = 6;
    static final String EUCRDATA_COLUMN_TYPE = "TYPE";
    static final int EUCRDATA_COLUMN_TYPE_INDEX = 4;
    static final String EUCRDATA_COLUMN_USER_PIN = "USER_PIN";
    static final int EUCRDATA_COLUMN_USER_PIN_INDEX = 8;
    static final String EUCRDATA_TABLE_NAME = "EUCRDATA";
    private static final String LOG_TAG = EucSQLiteHelper.class.getSimpleName();
    /* access modifiers changed from: private */
    public static final Object mLock = new Object();
    /* access modifiers changed from: private */
    public static int sOpenCounter;
    private static volatile EucSQLiteHelper sVolatileInstance = null;

    public static EucSQLiteHelper getInstance(Context context) {
        if (sVolatileInstance == null) {
            synchronized (mLock) {
                if (sVolatileInstance == null) {
                    sVolatileInstance = new EucSQLiteHelper(context);
                }
            }
        }
        return sVolatileInstance;
    }

    private EucSQLiteHelper(Context context) {
        super(context, DB_NAME, (SQLiteDatabase.CursorFactory) null, 3);
    }

    private static class EucSQLiteCursor extends SQLiteCursor {
        private static final String LOG_TAG = EucSQLiteCursor.class.getSimpleName();

        EucSQLiteCursor(SQLiteCursorDriver sQLiteCursorDriver, String str, SQLiteQuery sQLiteQuery) {
            super(sQLiteCursorDriver, str, sQLiteQuery);
        }

        public void close() {
            String str = LOG_TAG;
            Log.v(str, "Closing cursor, thread=" + Thread.currentThread().getName());
            super.close();
            close(getDatabase());
        }

        private void close(SQLiteDatabase sQLiteDatabase) {
            String name = Thread.currentThread().getName();
            synchronized (EucSQLiteHelper.mLock) {
                EucSQLiteHelper.sOpenCounter = EucSQLiteHelper.sOpenCounter - 1;
                String str = LOG_TAG;
                Log.v(str, "reference counter=" + EucSQLiteHelper.sOpenCounter + ", thread=" + name);
                if (EucSQLiteHelper.sOpenCounter == 0) {
                    Log.v(str, "Closing database, thread=" + name);
                    if (sQLiteDatabase != null) {
                        sQLiteDatabase.close();
                    } else {
                        Log.e(str, "Database is already closed!, thread=" + name);
                    }
                }
            }
        }
    }

    private static class EucSQLiteCursorFactory implements SQLiteDatabase.CursorFactory {
        private static final String LOG_TAG = EucSQLiteCursorFactory.class.getSimpleName();

        private EucSQLiteCursorFactory() {
        }

        public Cursor newCursor(SQLiteDatabase sQLiteDatabase, SQLiteCursorDriver sQLiteCursorDriver, String str, SQLiteQuery sQLiteQuery) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "newCursor, thread=" + Thread.currentThread().getName() + ", db=" + sQLiteDatabase.getPath());
            return new EucSQLiteCursor(sQLiteCursorDriver, str, sQLiteQuery);
        }
    }

    public SQLiteDatabase getReadableDatabase() {
        throw new UnsupportedOperationException();
    }

    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase writableDatabase;
        synchronized (mLock) {
            writableDatabase = super.getWritableDatabase();
            sOpenCounter++;
            Log.v(LOG_TAG, "Obtaining database, reference counter=" + sOpenCounter + ", thread=" + Thread.currentThread().getName());
        }
        return writableDatabase;
    }

    public synchronized void close() {
        String name = Thread.currentThread().getName();
        String str = LOG_TAG;
        Log.d(str, "Close(), thread=" + name);
        synchronized (mLock) {
            sOpenCounter--;
            Log.v(str, "reference counter=" + sOpenCounter + ", thread=" + name);
            if (sOpenCounter == 0) {
                Log.v(str, "Closing database, thread=" + name);
                super.close();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public SQLiteDatabase.CursorFactory getCursorFactory() {
        return new EucSQLiteCursorFactory();
    }

    public void onConfigure(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.setForeignKeyConstraintsEnabled(true);
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        String str = LOG_TAG;
        Log.d(str, "DatabaseHelper onCreate() for eucr");
        Log.d(str, "exec SQL:CREATE TABLE IF NOT EXISTS EUCRDATA ( ID TEXT,PIN INTEGER,EXTERNAL INTEGER,STATE INTEGER,TYPE INTEGER,REMOTE_URI TEXT,TIMESTAMP INTEGER,TIMEOUT INTEGER,USER_PIN TEXT,SUBSCRIBER_IDENTITY TEXT, PRIMARY KEY (ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI));");
        sQLiteDatabase.execSQL(CREATE_EUCR_DATA_TABLE);
        Log.d(str, "exec SQL:CREATE TABLE IF NOT EXISTS DIALOG (ID TEXT,LANGUAGE TEXT,SUBJECT TEXT,TEXT TEXT,ACCEPT_BUTTON TEXT,REJECT_BUTTON TEXT,SUBSCRIBER_IDENTITY TEXT, TYPE INTEGER, REMOTE_URI TEXT, PRIMARY KEY(ID,TYPE,LANGUAGE,SUBSCRIBER_IDENTITY,REMOTE_URI), FOREIGN KEY(ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI) REFERENCES EUCRDATA(ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI) ON DELETE CASCADE);");
        sQLiteDatabase.execSQL(CREATE_DIALOG_TABLE);
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        String str = LOG_TAG;
        Log.i(str, "db upgrade: oldVersion=" + i + " newVersion=" + i2);
        if (i == 1) {
            Log.d(str, "exec SQL:CREATE TABLE IF NOT EXISTS DIALOG_new ( ID TEXT,LANGUAGE TEXT,SUBJECT TEXT,TEXT TEXT,ACCEPT_BUTTON TEXT,REJECT_BUTTON INTEGER,SUBSCRIBER_IDENTITY TEXT, TYPE INTEGER, REMOTE_URI TEXT, PRIMARY KEY (ID,TYPE,LANGUAGE,SUBSCRIBER_IDENTITY,REMOTE_URI));");
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS DIALOG_new ( ID TEXT,LANGUAGE TEXT,SUBJECT TEXT,TEXT TEXT,ACCEPT_BUTTON TEXT,REJECT_BUTTON INTEGER,SUBSCRIBER_IDENTITY TEXT, TYPE INTEGER, REMOTE_URI TEXT, PRIMARY KEY (ID,TYPE,LANGUAGE,SUBSCRIBER_IDENTITY,REMOTE_URI));");
            Log.d(str, "exec SQL:INSERT INTO DIALOG_new (ID,LANGUAGE,SUBJECT,TEXT,ACCEPT_BUTTON,REJECT_BUTTON,SUBSCRIBER_IDENTITY,TYPE) SELECT * FROM DIALOG;");
            sQLiteDatabase.execSQL("INSERT INTO DIALOG_new (ID,LANGUAGE,SUBJECT,TEXT,ACCEPT_BUTTON,REJECT_BUTTON,SUBSCRIBER_IDENTITY,TYPE) SELECT * FROM DIALOG;");
            sQLiteDatabase.execSQL(DROP_DIALOG_TABLE);
            sQLiteDatabase.execSQL("ALTER TABLE DIALOG_new RENAME TO DIALOG;");
            Log.d(str, "exec SQL:UPDATE DIALOG SET REMOTE_URI = (SELECT REMOTE_URI FROM EUCRDATA WHERE(ID=DIALOG.ID AND TYPE=DIALOG.TYPE AND SUBSCRIBER_IDENTITY=DIALOG.SUBSCRIBER_IDENTITY));");
            sQLiteDatabase.execSQL("UPDATE DIALOG SET REMOTE_URI = (SELECT REMOTE_URI FROM EUCRDATA WHERE(ID=DIALOG.ID AND TYPE=DIALOG.TYPE AND SUBSCRIBER_IDENTITY=DIALOG.SUBSCRIBER_IDENTITY));");
            Log.d(str, "exec SQL:CREATE TABLE IF NOT EXISTS EUCRDATA_new ( ID TEXT,PIN INTEGER,EXTERNAL INTEGER,STATE INTEGER,TYPE INTEGER,REMOTE_URI TEXT,TIMESTAMP INTEGER,TIMEOUT INTEGER,USER_PIN TEXT,SUBSCRIBER_IDENTITY TEXT, PRIMARY KEY (ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI));");
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS EUCRDATA_new ( ID TEXT,PIN INTEGER,EXTERNAL INTEGER,STATE INTEGER,TYPE INTEGER,REMOTE_URI TEXT,TIMESTAMP INTEGER,TIMEOUT INTEGER,USER_PIN TEXT,SUBSCRIBER_IDENTITY TEXT, PRIMARY KEY (ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI));");
            Log.d(str, "exec SQL:INSERT INTO EUCRDATA_new (ID,PIN,EXTERNAL,STATE,TYPE,REMOTE_URI,TIMESTAMP,TIMEOUT,USER_PIN,SUBSCRIBER_IDENTITY) SELECT * FROM EUCRDATA;");
            sQLiteDatabase.execSQL("INSERT INTO EUCRDATA_new (ID,PIN,EXTERNAL,STATE,TYPE,REMOTE_URI,TIMESTAMP,TIMEOUT,USER_PIN,SUBSCRIBER_IDENTITY) SELECT * FROM EUCRDATA;");
            sQLiteDatabase.execSQL(DROP_EUCR_DATA_TABLE);
            sQLiteDatabase.execSQL("ALTER TABLE EUCRDATA_new RENAME TO EUCRDATA;");
            i = 2;
        }
        if (i == 2) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS DIALOG_new (ID TEXT,LANGUAGE TEXT,SUBJECT TEXT,TEXT TEXT,ACCEPT_BUTTON TEXT,REJECT_BUTTON TEXT,SUBSCRIBER_IDENTITY TEXT, TYPE INTEGER, REMOTE_URI TEXT, PRIMARY KEY(ID,TYPE,LANGUAGE,SUBSCRIBER_IDENTITY,REMOTE_URI), FOREIGN KEY(ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI) REFERENCES EUCRDATA(ID,TYPE,SUBSCRIBER_IDENTITY,REMOTE_URI) ON DELETE CASCADE);");
            sQLiteDatabase.execSQL("INSERT INTO DIALOG_new SELECT * FROM DIALOG;");
            sQLiteDatabase.execSQL(DROP_DIALOG_TABLE);
            sQLiteDatabase.execSQL("ALTER TABLE DIALOG_new RENAME TO DIALOG;");
        }
    }
}
