package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.gsma.services.rcs.sharing.image.ImageSharingLog;
import com.sec.internal.ims.servicemodules.csh.CshCache;
import com.sec.internal.ims.servicemodules.csh.ImageShare;
import com.sec.internal.ims.servicemodules.csh.event.CshInfo;
import com.sec.internal.ims.servicemodules.csh.event.IContentShare;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.util.PhoneUtils;

public class IshProvider extends ContentProvider {
    public static final String AUTHORITY;
    private static final String CONTENT_TYPE = "placeholder";
    private static final String LOG_TAG = IshProvider.class.getSimpleName();
    private static final int RCSAPI = 1;
    private static final int RCSAPI_ID = 2;
    private static final UriMatcher sUriMatcher;
    private CshCache mCache;
    private final String[] session_columns = {"_id", "sharingId", ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, "filename", "mime_type", "direction", "filesize", "transferred", "state"};

    public String getType(Uri uri) {
        return null;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        String authority = ImageSharingLog.CONTENT_URI.getAuthority();
        AUTHORITY = authority;
        uriMatcher.addURI(authority, "ish", 1);
        uriMatcher.addURI(authority, "ish/#", 2);
    }

    public int delete(Uri uri, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException();
    }

    public boolean onCreate() {
        this.mCache = CshCache.getInstance();
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        int match = sUriMatcher.match(uri);
        MatrixCursor matrixCursor = new MatrixCursor(this.session_columns);
        Log.d(LOG_TAG, "mCache.getSize() = " + this.mCache.getSize());
        if (match == 1) {
            int i = 0;
            for (int i2 = 0; i2 < this.mCache.getSize(); i2++) {
                IContentShare sessionAt = this.mCache.getSessionAt(i2);
                if (sessionAt instanceof ImageShare) {
                    CshInfo content = sessionAt.getContent();
                    Log.d(LOG_TAG, content.toString());
                    matrixCursor.addRow(new Object[]{Integer.valueOf(i), String.valueOf(content.shareId), PhoneUtils.extractNumberFromUri(content.shareContactUri.toString()), String.valueOf(content.dataPath), Integer.valueOf(content.shareType), Integer.valueOf(content.shareDirection), Long.valueOf(content.dataSize), Long.valueOf(content.dataProgress), Integer.valueOf(content.shareState)});
                    i++;
                }
            }
        } else if (match == 2) {
            String str3 = uri.getPathSegments().get(1);
            int i3 = 0;
            while (true) {
                if (i3 >= this.mCache.getSize()) {
                    break;
                }
                CshInfo content2 = this.mCache.getSessionAt(i3).getContent();
                if (str3 != null && content2 != null && str3.equals(String.valueOf(content2.shareId))) {
                    Log.d(LOG_TAG, content2.toString());
                    matrixCursor.addRow(new Object[]{0, String.valueOf(content2.shareId), PhoneUtils.extractNumberFromUri(content2.shareContactUri.toString()), String.valueOf(content2.dataPath), CONTENT_TYPE, Integer.valueOf(content2.shareType), Integer.valueOf(content2.shareDirection), Long.valueOf(content2.dataSize), Long.valueOf(content2.dataProgress), Integer.valueOf(content2.shareState)});
                    break;
                }
                i3++;
            }
        }
        Log.d(LOG_TAG, "cm.getCount() = " + matrixCursor.getCount());
        return matrixCursor;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }
}
