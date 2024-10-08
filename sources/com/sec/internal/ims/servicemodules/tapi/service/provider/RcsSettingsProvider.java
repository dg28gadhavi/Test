package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.gsma.services.rcs.CommonServiceConfiguration;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDefaultConst;
import com.sec.internal.constants.ims.servicemodules.im.ImSettings;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.servicemodules.tapi.service.defaultconst.FileTransferDefaultConst;
import java.util.ArrayList;

public class RcsSettingsProvider extends ContentProvider {
    public static final String DATABASE_NAME = "rcs_settings.db";
    /* access modifiers changed from: private */
    public static final String FALSE = Boolean.toString(false);
    private static final String LOG_TAG = "RcsSettingsProvider";
    private static final int RCSAPI_SETTINGS = 1;
    private static final int RCSAPI_SETTINGS_KEY = 2;
    private static final String TABLE = "settings";
    /* access modifiers changed from: private */
    public static final String TRUE = Boolean.toString(true);
    private static final UriMatcher sUriMatcher;
    private SQLiteOpenHelper mOpenHelper;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.gsma.services.rcs.provider.settings", TABLE, 1);
        uriMatcher.addURI("com.gsma.services.rcs.provider.settings", "settings/*", 2);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 103;

        public DatabaseHelper(Context context) {
            super(context, RcsSettingsProvider.DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 103);
        }

        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE settings (id integer primary key autoincrement,key TEXT,value TEXT);");
            addParameter(sQLiteDatabase, "ServiceActivated", RcsSettingsProvider.FALSE);
            addParameter(sQLiteDatabase, "ConfigurationValidity", RcsSettingsProvider.FALSE);
            addParameter(sQLiteDatabase, "ServiceAvailable", RcsSettingsProvider.FALSE);
            addParameter(sQLiteDatabase, "ModeChangeable", RcsSettingsProvider.TRUE);
            addParameter(sQLiteDatabase, "MinimumBatteryLevel", String.valueOf(CommonServiceConfiguration.MinimumBatteryLevel.NONE.toString()));
            addParameter(sQLiteDatabase, "DefaultMessagingMethod", CommonServiceConfiguration.MessagingMethod.NON_RCS.toString());
            addParameter(sQLiteDatabase, "MessagingMode", CommonServiceConfiguration.MessagingMode.NONE.toString());
            addParameter(sQLiteDatabase, "MyCountryCode", "+1");
            addParameter(sQLiteDatabase, "CountryAreaCode", "0");
            addParameter(sQLiteDatabase, "MyContactId", "");
            addParameter(sQLiteDatabase, "MyDisplayName", "");
            addParameter(sQLiteDatabase, ImSettings.CHAT_RESPOND_TO_DISPLAY_REPORTS, Boolean.toString(ImDefaultConst.DEFAULT_CHAT_RESPOND_TO_DISPLAY_REPORTS.booleanValue()));
            addParameter(sQLiteDatabase, ImSettings.AUTO_ACCEPT_FT_CHANGEABLE, Boolean.toString(false));
            addParameter(sQLiteDatabase, ImSettings.AUTO_ACCEPT_FILE_TRANSFER, Boolean.toString(false));
            addParameter(sQLiteDatabase, ImSettings.AUTO_ACCEPT_FT_IN_ROAMING, Boolean.toString(false));
            addParameter(sQLiteDatabase, ImSettings.KEY_IMAGE_RESIZE_OPTION, FileTransferDefaultConst.DEFALUT_IMAGERESIZEOPTION.toString());
        }

        private void addParameter(SQLiteDatabase sQLiteDatabase, String str, String str2) {
            sQLiteDatabase.execSQL("INSERT INTO settings (key,value) VALUES ('" + str + "','" + str2 + "');");
        }

        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            ArrayList arrayList = new ArrayList();
            Cursor query = sQLiteDatabase.query(RcsSettingsProvider.TABLE, (String[]) null, (String) null, (String[]) null, (String) null, (String) null, (String) null);
            if (query != null) {
                while (query.moveToNext()) {
                    try {
                        int columnIndex = query.getColumnIndex(McsConstants.BundleData.KEY);
                        String str = null;
                        String string = columnIndex != -1 ? query.getString(columnIndex) : null;
                        int columnIndex2 = query.getColumnIndex("value");
                        if (columnIndex2 != -1) {
                            str = query.getString(columnIndex2);
                        }
                        if (!(string == null || str == null)) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(McsConstants.BundleData.KEY, string);
                            contentValues.put("value", str);
                            arrayList.add(contentValues);
                        }
                    } catch (Throwable th) {
                        th.addSuppressed(th);
                    }
                }
            }
            if (query != null) {
                query.close();
            }
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS settings");
            onCreate(sQLiteDatabase);
            for (int i3 = 0; i3 < arrayList.size(); i3++) {
                ContentValues contentValues2 = (ContentValues) arrayList.get(i3);
                sQLiteDatabase.update(RcsSettingsProvider.TABLE, contentValues2, "key = ?", new String[]{CmcConstants.E_NUM_STR_QUOTE + contentValues2.getAsString(McsConstants.BundleData.KEY) + CmcConstants.E_NUM_STR_QUOTE});
            }
            return;
            throw th;
        }

        public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            onUpgrade(sQLiteDatabase, i, i2);
        }
    }

    public boolean onCreate() {
        this.mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        if (match == 1 || match == 2) {
            return uri.toString();
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    private StringBuilder buildKeyedSelection(String str, String str2, String str3) {
        StringBuilder sb = new StringBuilder("(");
        sb.append(str);
        sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
        sb.append(str2);
        sb.append(")");
        if (TextUtils.isEmpty(str3)) {
            return sb;
        }
        sb.append(" AND (");
        sb.append(str3);
        sb.append(")");
        return sb;
    }

    /* JADX WARNING: type inference failed for: r8v0, types: [java.lang.String, android.database.Cursor] */
    /* JADX WARNING: type inference failed for: r8v1, types: [android.database.Cursor] */
    /* JADX WARNING: type inference failed for: r8v3 */
    /* JADX WARNING: type inference failed for: r8v4 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.database.Cursor query(android.net.Uri r10, java.lang.String[] r11, java.lang.String r12, java.lang.String[] r13, java.lang.String r14) {
        /*
            r9 = this;
            android.database.sqlite.SQLiteQueryBuilder r0 = new android.database.sqlite.SQLiteQueryBuilder
            r0.<init>()
            java.lang.String r1 = "settings"
            r0.setTables(r1)
            android.content.UriMatcher r1 = sUriMatcher
            int r1 = r1.match(r10)
            r2 = 1
            r8 = 0
            if (r1 == r2) goto L_0x0052
            r2 = 2
            if (r1 != r2) goto L_0x003b
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            java.lang.String r2 = "'"
            r1.<init>(r2)
            java.lang.String r3 = r10.getLastPathSegment()
            r1.append(r3)
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.String r2 = "key"
            java.lang.StringBuilder r1 = r9.buildKeyedSelection(r2, r1, r8)
            java.lang.String r1 = r1.toString()
            r0.appendWhere(r1)
            goto L_0x0052
        L_0x003b:
            java.lang.IllegalArgumentException r9 = new java.lang.IllegalArgumentException
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r12 = "Unknown URI "
            r11.append(r12)
            r11.append(r10)
            java.lang.String r10 = r11.toString()
            r9.<init>(r10)
            throw r9
        L_0x0052:
            android.database.sqlite.SQLiteOpenHelper r1 = r9.mOpenHelper     // Catch:{ RuntimeException -> 0x0070 }
            android.database.sqlite.SQLiteDatabase r1 = r1.getReadableDatabase()     // Catch:{ RuntimeException -> 0x0070 }
            r5 = 0
            r6 = 0
            r2 = r11
            r3 = r12
            r4 = r13
            r7 = r14
            android.database.Cursor r8 = r0.query(r1, r2, r3, r4, r5, r6, r7)     // Catch:{ RuntimeException -> 0x0070 }
            if (r8 == 0) goto L_0x008c
            android.content.Context r9 = r9.getContext()     // Catch:{ RuntimeException -> 0x0070 }
            android.content.ContentResolver r9 = r9.getContentResolver()     // Catch:{ RuntimeException -> 0x0070 }
            r8.setNotificationUri(r9, r10)     // Catch:{ RuntimeException -> 0x0070 }
            goto L_0x008c
        L_0x0070:
            r9 = move-exception
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r11 = "SQL exception while query: "
            r10.append(r11)
            r10.append(r9)
            java.lang.String r9 = r10.toString()
            java.lang.String r10 = "RcsSettingsProvider"
            android.util.Log.e(r10, r9)
            if (r8 == 0) goto L_0x008c
            r8.close()
        L_0x008c:
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.provider.RcsSettingsProvider.query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String):android.database.Cursor");
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        int i = 0;
        try {
            SQLiteDatabase writableDatabase = this.mOpenHelper.getWritableDatabase();
            int match = sUriMatcher.match(uri);
            if (match == 1) {
                writableDatabase.beginTransaction();
                try {
                    i = writableDatabase.update(TABLE, contentValues, str, strArr);
                    writableDatabase.setTransactionSuccessful();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (Throwable th) {
                    endTransaction(writableDatabase);
                    throw th;
                }
                endTransaction(writableDatabase);
            } else if (match == 2) {
                String sb = buildKeyedSelection(McsConstants.BundleData.KEY, "'" + uri.getLastPathSegment() + "'", str).toString();
                writableDatabase.beginTransaction();
                try {
                    i = writableDatabase.update(TABLE, contentValues, sb, strArr);
                    writableDatabase.setTransactionSuccessful();
                } catch (SQLException e2) {
                    e2.printStackTrace();
                } catch (Throwable th2) {
                    endTransaction(writableDatabase);
                    throw th2;
                }
                endTransaction(writableDatabase);
            } else {
                throw new UnsupportedOperationException("Cannot update URI " + uri);
            }
            if (i != 0) {
                getContext().getContentResolver().notifyChange(uri, (ContentObserver) null);
            }
            return i;
        } catch (SQLException e3) {
            Log.d(LOG_TAG, "update: SQLException: " + e3.toString());
            return 0;
        }
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException("Cannot insert URI " + uri);
    }

    public int delete(Uri uri, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }

    private void endTransaction(SQLiteDatabase sQLiteDatabase) {
        if (sQLiteDatabase == null) {
            Log.e(LOG_TAG, "endTransaction: db is null");
            return;
        }
        try {
            sQLiteDatabase.endTransaction();
        } catch (SQLException e) {
            Log.e(LOG_TAG, "SQLException while endTransaction:" + e);
        }
    }
}
