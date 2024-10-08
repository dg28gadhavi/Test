package com.sec.internal.ims.servicemodules.csh;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.ims.servicemodules.csh.event.CshInfo;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;

public class CshProvider extends ContentProvider {
    private static final String LOG_TAG = CshProvider.class.getSimpleName();
    private static final String PROVIDER_NAME = "com.samsung.rcs.cs";
    private static final UriMatcher sUriMatcher;
    private CshCache mCache;
    private final String[] session_columns = {"id", "state", ICshConstants.ShareDatabase.KEY_SHARE_DIRECTION, "type", "size", "path", ICshConstants.ShareDatabase.KEY_PROGRESS, ICshConstants.ShareDatabase.KEY_RESOLUTION_HEIGHT, ICshConstants.ShareDatabase.KEY_RESOLUTION_WIDTH, ICshConstants.ShareDatabase.KEY_TARGET_CONTACT};

    public String getType(Uri uri) {
        return null;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.samsung.rcs.cs", "active_sessions", 5);
    }

    public int delete(Uri uri, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException();
    }

    public boolean onCreate() {
        this.mCache = CshCache.getInstance();
        return false;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        if (sUriMatcher.match(uri) != 5) {
            return null;
        }
        MatrixCursor matrixCursor = new MatrixCursor(this.session_columns);
        for (int i = 0; i < this.mCache.getSize(); i++) {
            CshInfo content = this.mCache.getSessionAt(i).getContent();
            Log.d(LOG_TAG, content.toString());
            matrixCursor.addRow(new String[]{String.valueOf(content.shareId), String.valueOf(content.shareState), String.valueOf(content.shareDirection), String.valueOf(content.shareType), String.valueOf(content.dataSize), String.valueOf(content.dataPath), String.valueOf(content.dataProgress), String.valueOf(content.videoWidth), String.valueOf(content.videoHeight), String.valueOf(content.shareContactUri)});
        }
        return matrixCursor;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }
}
