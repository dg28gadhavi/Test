package com.sec.internal.ims.cmstore.helper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;

public class RCSDBHelper {
    public static final String TAG = "RCSDBHelper";
    private ContentResolver mResolver = null;

    public RCSDBHelper(Context context) {
        this.mResolver = context.getContentResolver();
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        try {
            return this.mResolver.insert(uri, contentValues);
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when insert: ", e);
            return null;
        }
    }

    public int insertSingleSessionPartsToDB(Uri uri, ContentValues[] contentValuesArr) {
        if (contentValuesArr != null && contentValuesArr.length >= 1) {
            try {
                return this.mResolver.bulkInsert(uri, contentValuesArr);
            } catch (SQLiteException e) {
                Log.e(TAG, "Catch a SQLiteException when insert: ", e);
            }
        }
        return 0;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        String str3 = TAG;
        Log.d(str3, "query uri=" + IMSLog.checker(uri.toString()) + " projections:" + Arrays.toString(strArr) + " whereClaus: " + str + " selectionArgs: " + Arrays.toString(strArr2) + " sortOrder: " + str2);
        try {
            return this.mResolver.query(uri, strArr, str, strArr2, str2);
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when query: ", e);
            return null;
        }
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        try {
            int update = this.mResolver.update(uri, contentValues, str, strArr);
            String str2 = TAG;
            Log.i(str2, "update success rowsupdated: " + update);
            return update;
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when update: ", e);
            return 0;
        }
    }

    public int delete(Uri uri, String str, String[] strArr) {
        try {
            int delete = this.mResolver.delete(uri, str, strArr);
            String str2 = TAG;
            Log.i(str2, "update success rowsupdated: " + delete);
            return delete;
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when delete: ", e);
            return 0;
        }
    }
}
