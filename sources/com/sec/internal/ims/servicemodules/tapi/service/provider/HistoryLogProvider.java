package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.gsma.services.rcs.chat.ChatLog;
import com.gsma.services.rcs.filetransfer.FileTransferLog;
import com.gsma.services.rcs.history.HistoryLog;
import com.gsma.services.rcs.sharing.image.ImageSharingLog;
import com.gsma.services.rcs.sharing.video.VideoSharingLog;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.tapi.service.api.HistoryLogMember;
import com.sec.internal.ims.servicemodules.tapi.service.api.HistoryLogServiceImpl;
import com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager;
import java.util.Map;

public class HistoryLogProvider extends ContentProvider {
    private static final String AUTHORITY;
    private static final String[] COLUMS = {"provider_id", "id", "mime_type", "direction", ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, "timestamp", "timestamp_sent", "timestamp_delivered", "timestamp_displayed", "status", "reason_code", CloudMessageProviderContract.BufferDBMMSpdu.READ_STATUS, "chat_id", "content", "fileicon", "fileicon_mime_type", "filename", "filesize", "transferred", CloudMessageProviderContract.VVMGreetingColumns.DURATION};
    private static final int HISTORY_ID = 2;
    private static final int HISTORY_PARAMLESS = 3;
    private static final int HISTORY_PARAMLESS_ID = 4;
    private static final int HISTROY = 1;
    private static final String LOG_TAG = HistoryLogProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher;
    private Context mContext;
    private HistoryLogServiceImpl mHistorySvcApi = null;
    private ContentResolver mResolver;

    public int delete(Uri uri, String str, String[] strArr) {
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        return 0;
    }

    static {
        String authority = HistoryLog.CONTENT_URI.getAuthority();
        AUTHORITY = authority;
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI(authority, "history", 1);
        uriMatcher.addURI(authority, "history/*", 2);
        uriMatcher.addURI(authority, "history_paramless", 3);
        uriMatcher.addURI(authority, "history_paramless/#", 4);
    }

    public boolean onCreate() {
        Context context = getContext();
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        String str3 = LOG_TAG;
        Log.d(str3, "Query: uri = " + uri);
        if (this.mHistorySvcApi == null) {
            this.mHistorySvcApi = TapiServiceManager.getHistoryService();
        }
        if (this.mHistorySvcApi == null) {
            Log.d(str3, "Query:  HistoryLogProvider is not available");
            return null;
        }
        int match = sUriMatcher.match(uri);
        if (match != 1) {
            if (match != 2) {
                if (match != 3) {
                    if (match != 4) {
                        return null;
                    }
                }
            }
            String lastPathSegment = uri.getLastPathSegment();
            if (lastPathSegment != null) {
                return mergeAll(strArr, lastPathSegment.split("_"));
            }
            return mergeAll(strArr, (String[]) null);
        }
        return mergeAll(strArr, (String[]) null);
    }

    private MatrixCursor mergeAll(String[] strArr, String[] strArr2) {
        Map<Integer, HistoryLogMember> externalProviderMap = this.mHistorySvcApi.getExternalProviderMap();
        if (strArr == null) {
            Log.d(LOG_TAG, "mergeAll, projection is null");
            strArr = COLUMS;
        }
        MatrixCursor matrixCursor = new MatrixCursor(strArr);
        mergeDefaultProviderCursor(matrixCursor, ChatLog.Message.CONTENT_URI, strArr, 1);
        mergeDefaultProviderCursor(matrixCursor, FileTransferLog.CONTENT_URI, strArr, 2);
        mergeDefaultProviderCursor(matrixCursor, ImageSharingLog.CONTENT_URI, strArr, 3);
        mergeDefaultProviderCursor(matrixCursor, VideoSharingLog.CONTENT_URI, strArr, 4);
        if (strArr2 == null) {
            for (Integer intValue : externalProviderMap.keySet()) {
                mergeAdditionalProviderCursor(matrixCursor, strArr, intValue.intValue());
            }
            return matrixCursor;
        }
        int length = strArr2.length;
        int i = 0;
        while (i < length) {
            try {
                mergeAdditionalProviderCursor(matrixCursor, strArr, Integer.valueOf(strArr2[i]).intValue());
                i++;
            } catch (NumberFormatException unused) {
                Log.e(LOG_TAG, "NumberFormatException, close cursor");
                matrixCursor.close();
                return null;
            }
        }
        return matrixCursor;
    }

    private void mergeDefaultProviderCursor(MatrixCursor matrixCursor, Uri uri, String[] strArr, int i) {
        if (strArr != null) {
            Cursor query = this.mResolver.query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
            if (query == null) {
                try {
                    Log.i(LOG_TAG, "No data exsit in " + uri);
                    if (query != null) {
                        query.close();
                        return;
                    }
                    return;
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            } else {
                Log.d(LOG_TAG, "cursor getCount = " + query.getCount());
                if (query.moveToFirst()) {
                    do {
                        MatrixCursor.RowBuilder newRow = matrixCursor.newRow();
                        for (String str : strArr) {
                            if (str.equals("provider_id")) {
                                newRow.add(Integer.valueOf(i));
                            } else {
                                newRow.add(getColumnValueToInsert(str, i, query));
                            }
                        }
                    } while (query.moveToNext());
                }
                query.close();
                return;
            }
        } else {
            return;
        }
        throw th;
    }

    private String getColumnValueToInsert(String str, int i, Cursor cursor) {
        if (str.equals("id")) {
            if (i == 1) {
                return cursor.getString(cursor.getColumnIndex("msg_id"));
            }
            if (i == 0) {
                return cursor.getString(cursor.getColumnIndex("chat_id"));
            }
            if (i == 2) {
                return cursor.getString(cursor.getColumnIndex("ft_id"));
            }
        } else if (str.equals("content") && i == 2) {
            return cursor.getString(cursor.getColumnIndex("file"));
        }
        int columnIndex = cursor.getColumnIndex(str);
        if (columnIndex < 0) {
            return null;
        }
        return cursor.getString(columnIndex);
    }

    private void mergeAdditionalProviderCursor(MatrixCursor matrixCursor, String[] strArr, int i) {
        HistoryLogMember historyLogMember = this.mHistorySvcApi.getExternalProviderMap().get(Integer.valueOf(i));
        if (historyLogMember == null) {
            Log.i(LOG_TAG, "Not registered provider, id = " + i);
            return;
        }
        Map<String, String> columnMapping = historyLogMember.getColumnMapping();
        Cursor query = this.mResolver.query(historyLogMember.getUri(), (String[]) null, (String) null, (String[]) null, (String) null);
        if (query == null) {
            try {
                Log.i(LOG_TAG, "No data exsit in " + historyLogMember.getUri());
                if (query != null) {
                    query.close();
                    return;
                }
                return;
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else {
            query.moveToFirst();
            while (query.moveToNext()) {
                MatrixCursor.RowBuilder newRow = matrixCursor.newRow();
                for (String str : strArr) {
                    if (str.equals("provider_id")) {
                        newRow.add(Integer.valueOf(i));
                    }
                    String str2 = columnMapping.get(str);
                    if (str2 == null) {
                        newRow.add((Object) null);
                    } else {
                        int columnIndex = query.getColumnIndex(str2);
                        if (columnIndex < 0) {
                            newRow.add((Object) null);
                        } else {
                            newRow.add(query.getString(columnIndex));
                        }
                    }
                }
            }
            query.close();
            return;
        }
        throw th;
    }
}
