package com.sec.internal.ims.servicemodules.tapi.service.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.ims.servicemodules.tapi.service.utils.BlockContactItem;

public final class BlockContactPersisit {
    public static final String BLOCKED_CONTACT_TABLE = "blockedContacts";
    private static final String LOG_TAG = "BlockContactPersisit";
    private static final String SELECTION_BLOCKED = "select * from blockedContacts where phone_number=?";
    public static BlockContactPersisit mInstance;
    private SQLiteDatabase db;
    private Context mContext;
    private DatabaseHelper mDatabaseHelper;

    public static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String CREATE_BLOCK_TABLE = "CREATE TABLE blockedContacts(id INTEGER PRIMARY KEY AUTOINCREMENT,phone_number TEXT,key_blocked TEXT, key_blocking_timestamp LONG);";
        public static final String DATABASE_NAME = "blockContact.db";
        private static final int DATABASE_VERSION = 28;

        private void createDb(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL(CREATE_BLOCK_TABLE);
        }

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 28);
        }

        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            createDb(sQLiteDatabase);
        }

        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            onCreate(sQLiteDatabase);
        }
    }

    public static synchronized BlockContactPersisit getInstance(Context context) {
        BlockContactPersisit blockContactPersisit;
        synchronized (BlockContactPersisit.class) {
            if (mInstance == null) {
                mInstance = new BlockContactPersisit(context);
            }
            blockContactPersisit = mInstance;
        }
        return blockContactPersisit;
    }

    private BlockContactPersisit(Context context) {
        this.mContext = context;
        DatabaseHelper databaseHelper = new DatabaseHelper(this.mContext);
        this.mDatabaseHelper = databaseHelper;
        try {
            this.db = databaseHelper.getWritableDatabase();
        } catch (SQLException e) {
            Log.d(LOG_TAG, "got exception when getting writableDatabase");
            e.printStackTrace();
        }
    }

    public long insertBlockContactInfo(ContactInfo contactInfo, boolean z) {
        String str = LOG_TAG;
        Log.d(str, "insertBlockContactInfo");
        long j = -1;
        if (contactInfo == null || contactInfo.getContact() == null) {
            Log.d(str, "ContactInfo phone num is null");
            return -1;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(BlockContactItem.BlockDataItem.PHONE_NUMBER, contactInfo.getContact().toString());
        contentValues.put(BlockContactItem.BlockDataItem.KEY_BLOCKED, contactInfo.getBlockingState().toString());
        contentValues.put(BlockContactItem.BlockDataItem.KEY_BLOCKING_TIMESTAMP, Long.valueOf(contactInfo.getBlockingTimestamp()));
        try {
            j = this.db.insert(BLOCKED_CONTACT_TABLE, (String) null, contentValues);
        } catch (SQLException e) {
            Log.d(LOG_TAG, "got exception when inserting block contact info");
            e.printStackTrace();
        }
        if (z) {
            notifyChanged(contactInfo.getContact().toString());
        }
        return j;
    }

    private void notifyChanged(String str) {
        this.mContext.getContentResolver().notifyChange(Uri.parse("content://com.gsma.services.rcs.provider.blockedcontact").buildUpon().appendPath(str).build(), (ContentObserver) null);
    }

    public Cursor query(String str) {
        try {
            return this.db.rawQuery(SELECTION_BLOCKED, new String[]{str});
        } catch (SQLException e) {
            Log.d(LOG_TAG, "got exception when querying block contact info");
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateBlockContactInfo(ContactInfo contactInfo) {
        int i;
        String str = LOG_TAG;
        Log.e(str, "updateBlockContactInfo");
        if (contactInfo == null || contactInfo.getContact() == null) {
            Log.e(str, "Info or contact is null");
            return false;
        }
        String contactId = contactInfo.getContact().toString();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BlockContactItem.BlockDataItem.PHONE_NUMBER, contactInfo.getContact().toString());
        contentValues.put(BlockContactItem.BlockDataItem.KEY_BLOCKED, contactInfo.getBlockingState().toString());
        contentValues.put(BlockContactItem.BlockDataItem.KEY_BLOCKING_TIMESTAMP, Long.valueOf(contactInfo.getBlockingTimestamp()));
        try {
            i = this.db.update(BLOCKED_CONTACT_TABLE, contentValues, "phone_number=?", new String[]{contactId});
        } catch (SQLException e) {
            Log.d(LOG_TAG, "got exception when updating block contact info");
            e.printStackTrace();
            i = 0;
        }
        if (i > 0) {
            return true;
        }
        return false;
    }

    public static void changeContactInfo(Context context, ContactInfo contactInfo) {
        getInstance(context);
        Cursor query = mInstance.query(contactInfo.getContact().toString());
        if (query != null) {
            try {
                if (query.getCount() == 0) {
                    mInstance.insertBlockContactInfo(contactInfo, false);
                } else {
                    mInstance.updateBlockContactInfo(contactInfo);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (query != null) {
            query.close();
            return;
        }
        return;
        throw th;
    }
}
