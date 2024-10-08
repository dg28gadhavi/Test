package com.sec.internal.ims.servicemodules.session;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;

public class SharedMultimediaProvider extends ContentProvider {
    private static final String AUTHORITY = "com.samsung.rcs.sharedmultimedia";
    private static final String LOG_TAG = SharedMultimediaProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher;
    private Object mContentType = null;
    private Object mDirection = null;
    private Object mFilePath = null;
    private Object mInsertedTimestamp = null;
    private Object mRemoteUri = null;
    private final String[] session_columns = {"remote_uri", ImContract.CsSession.FILE_PATH, "direction", ImContract.ChatItem.INSERTED_TIMESTAMP, "content_type"};

    public String getType(Uri uri) {
        return null;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.samsung.rcs.sharedmultimedia", "shared_multimedia", 1);
    }

    public int delete(Uri uri, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        if (sUriMatcher.match(uri) != 1) {
            return null;
        }
        String str = LOG_TAG;
        Log.d(str, "insert");
        this.mRemoteUri = contentValues.get("remote_uri");
        this.mFilePath = contentValues.get(ImContract.CsSession.FILE_PATH);
        this.mDirection = contentValues.get("direction");
        this.mInsertedTimestamp = contentValues.get(ImContract.ChatItem.INSERTED_TIMESTAMP);
        this.mContentType = contentValues.get("content_type");
        if (this.mFilePath != null) {
            Log.d(str, "mFilePath : " + this.mFilePath.toString());
        }
        if (this.mDirection != null) {
            Log.d(str, "mDirection : " + this.mDirection.toString());
        }
        if (this.mInsertedTimestamp != null) {
            Log.d(str, "mInsertedTimestamp : " + this.mInsertedTimestamp.toString());
        }
        if (this.mContentType != null) {
            Log.d(str, "mContentType : " + this.mContentType.toString());
        }
        onContentsInserted();
        return uri;
    }

    public boolean onCreate() {
        Log.d(LOG_TAG, "SharedMultimediaProvider : onCreate()");
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        if (sUriMatcher.match(uri) != 1) {
            return null;
        }
        Log.d(LOG_TAG, "query");
        MatrixCursor matrixCursor = new MatrixCursor(this.session_columns);
        matrixCursor.addRow(new Object[]{this.mRemoteUri, this.mFilePath, this.mDirection, this.mInsertedTimestamp, this.mContentType});
        return matrixCursor;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }

    private void onContentsInserted() {
        Intent intent = new Intent();
        intent.addCategory(ISharedMultimediaConstants.CATEGORY_NOTIFICATION);
        intent.setAction(ISharedMultimediaConstants.NOTIFICATION_MULTI_DATA_INSERTION);
        String str = LOG_TAG;
        Log.d(str, "broadcastIntent: " + intent.toString());
        getContext().sendBroadcastAsUser(intent, ContextExt.CURRENT_OR_SELF);
    }
}
