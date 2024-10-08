package com.sec.internal.ims.entitlement.config.persist;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.ims.entitlement.storagehelper.MigrationHelper;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.log.IMSLog;
import java.util.Date;

public class EntitlementConfigProvider extends ContentProvider {
    private static final String CREATE_DEVICE_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS entitlement_config(_id INTEGER PRIMARY KEY AUTOINCREMENT,version TEXT, imsi TEXT NOT NULL, device_config TEXT,backup_version TEXT,validity TEXT,next_config_time TEXT,token TEXT,completed TEXT,tc_popup_user_accept TEXT);";
    private static final String DATABASE_NAME = "entitlement_config.db";
    private static final int DATABASE_VERSION = 1;
    private static final int DEFAULT_SIM_SLOT_IDX = 0;
    private static final String DEVICE_CONFIG_TABLE = "entitlement_config";
    private static final long ENTITLEMENT_FORCE_UPDATE_EXPIRATION_TIME = 300000;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = EntitlementConfigProvider.class.getSimpleName();
    private static final String PROVIDER_NAME = "com.samsung.ims.entitlementconfig.provider";
    private static final UriMatcher sUriMatcher;
    private Date configUpdateDate = null;
    protected Context mContext = null;
    /* access modifiers changed from: private */
    public DatabaseHelper mDatabaseHelper = null;
    protected Messenger mService;
    private ServiceConnection mSvcConn;

    public String getType(Uri uri) {
        return null;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "config", 1);
        uriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "config/xpath", 3);
        uriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "config/jansky_config", 2);
        uriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "config/rcs_config", 4);
        uriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "config/force_update", 5);
        uriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "config/entitlement_url", 6);
        uriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "reconnect_db", 7);
        uriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "binding_service", 8);
        uriMatcher.addURI("com.samsung.ims.entitlementconfig.provider", "config/tag", 9);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        }

        public DatabaseHelper(Context context) {
            super(context, EntitlementConfigProvider.DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
        }

        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            IMSLog.i(EntitlementConfigProvider.LOG_TAG, "DatabaseHelper onCreate()");
            sQLiteDatabase.execSQL(EntitlementConfigProvider.CREATE_DEVICE_CONFIG_TABLE);
        }

        public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            String r0 = EntitlementConfigProvider.LOG_TAG;
            IMSLog.i(r0, "db downgrade: oldVersion=" + i + " newVersion=" + i2);
            onCreate(sQLiteDatabase);
            sQLiteDatabase.setVersion(i2);
        }
    }

    public int delete(Uri uri, String str, String[] strArr) {
        String str2 = LOG_TAG;
        IMSLog.i(str2, "delete:" + uri);
        int i = 0;
        if (this.mDatabaseHelper == null || !NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            return 0;
        }
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        try {
            if (sUriMatcher.match(uri) == 1) {
                NotifyDeleteDb();
                i = writableDatabase.delete(DEVICE_CONFIG_TABLE, str, strArr);
            }
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e) {
            String str3 = LOG_TAG;
            IMSLog.s(str3, "Could not delete:" + e.getMessage());
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
        writableDatabase.endTransaction();
        return i;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        String str = LOG_TAG;
        IMSLog.s(str, "insert:" + uri);
        Uri uri2 = null;
        if (this.mDatabaseHelper != null && NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            if (sUriMatcher.match(uri) == 1) {
                uri2 = EntitlementConfigContract.DeviceConfig.buildDeviceConfigUri(insertDeviceConfig(contentValues));
            }
            if (uri2 != null) {
                notifyChange(uri);
            }
        }
        return uri2;
    }

    public void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, (ContentObserver) null);
    }

    private long insertDeviceConfig(ContentValues contentValues) {
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        long j = -1;
        try {
            j = writableDatabase.insert(DEVICE_CONFIG_TABLE, (String) null, contentValues);
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not insert into device_config table:" + e.getMessage());
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
        writableDatabase.endTransaction();
        return j;
    }

    private int updateDeviceConfig(ContentValues contentValues) {
        SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
        writableDatabase.beginTransaction();
        int i = 0;
        try {
            i = writableDatabase.update(DEVICE_CONFIG_TABLE, contentValues, (String) null, (String[]) null);
            writableDatabase.setTransactionSuccessful();
        } catch (SQLiteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not update connectivity_parameters table:" + e.getMessage());
        } catch (Throwable th) {
            writableDatabase.endTransaction();
            throw th;
        }
        writableDatabase.endTransaction();
        return i;
    }

    public void forceConfigUpdate() {
        IMSLog.i(LOG_TAG, "forceConfigUpdate()");
        if (this.configUpdateDate == null || new Date().getTime() - this.configUpdateDate.getTime() > ENTITLEMENT_FORCE_UPDATE_EXPIRATION_TIME) {
            try {
                this.configUpdateDate = new Date(System.currentTimeMillis());
                Message message = new Message();
                message.what = 108;
                message.obj = 0;
                this.mService.send(message);
            } catch (Exception e) {
                String str = LOG_TAG;
                IMSLog.s(str, "Could not force update config" + e.getMessage());
            }
        }
    }

    private void updateEntitlementUrl(Uri uri) {
        String queryParameter = uri.getQueryParameter("entitlement_url");
        try {
            Message message = new Message();
            message.what = 201;
            Bundle bundle = new Bundle();
            bundle.putString("URL", queryParameter);
            message.setData(bundle);
            this.mService.send(message);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "updateEntitlementUrl: failed to request" + e.getMessage());
        }
    }

    private void NotifyDeleteDb() {
        try {
            Message message = new Message();
            message.what = 202;
            this.mService.send(message);
        } catch (RemoteException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "NotifyDeleteDb: failed to request" + e.getMessage());
        }
    }

    public boolean onCreate() {
        this.mContext = getContext().createCredentialProtectedStorageContext();
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        String str3 = LOG_TAG;
        IMSLog.s(str3, "query " + uri);
        if (this.mDatabaseHelper == null || !NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            return null;
        }
        SQLiteDatabase readableDatabase = this.mDatabaseHelper.getReadableDatabase();
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        int match = sUriMatcher.match(uri);
        if (match == 1) {
            sQLiteQueryBuilder.setTables(DEVICE_CONFIG_TABLE);
            return sQLiteQueryBuilder.query(readableDatabase, strArr, str, strArr2, (String) null, (String) null, str2);
        } else if (match == 2) {
            return getJanskyConfigXmlBlock();
        } else {
            if (match == 3) {
                return getNsdsElementsWithXPath(uri);
            }
            if (match == 4) {
                return getRcsConfigXmlBlock();
            }
            if (match != 9) {
                return null;
            }
            return getXmlConfigbyTagUri(uri);
        }
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        String str2 = LOG_TAG;
        IMSLog.s(str2, "update " + uri);
        UriMatcher uriMatcher = sUriMatcher;
        int i = 0;
        if (uriMatcher.match(uri) == 8) {
            IMSLog.i(str2, "Binding to EntitlementConfigService");
            connectToEntitlementConfigService();
            return 0;
        } else if (uriMatcher.match(uri) == 7) {
            IMSLog.e(str2, "Reconnect DB for DatabaseHelper");
            if (this.mDatabaseHelper != null) {
                IMSLog.i(str2, "Reconnect DB after closing the previous DB");
                this.mDatabaseHelper.close();
            }
            this.mDatabaseHelper = new DatabaseHelper(this.mContext);
            return 0;
        } else if (this.mDatabaseHelper == null || !NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            return 0;
        } else {
            SQLiteDatabase writableDatabase = this.mDatabaseHelper.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                int match = uriMatcher.match(uri);
                if (match == 1) {
                    i = updateDeviceConfig(contentValues);
                } else if (match == 5) {
                    forceConfigUpdate();
                } else if (match == 6) {
                    updateEntitlementUrl(uri);
                }
                writableDatabase.setTransactionSuccessful();
            } catch (SQLiteException e) {
                String str3 = LOG_TAG;
                IMSLog.s(str3, "Could not update table:" + e.getMessage());
            } catch (Throwable th) {
                writableDatabase.endTransaction();
                throw th;
            }
            writableDatabase.endTransaction();
            if (i != 0) {
                notifyChange(uri);
            }
            return i;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: java.lang.String} */
    /* JADX WARNING: type inference failed for: r2v0 */
    /* JADX WARNING: type inference failed for: r2v1, types: [android.database.Cursor] */
    /* JADX WARNING: type inference failed for: r2v5 */
    /* JADX WARNING: type inference failed for: r2v6 */
    /* JADX WARNING: type inference failed for: r2v7 */
    /* JADX WARNING: type inference failed for: r2v9 */
    /* JADX WARNING: type inference failed for: r2v10 */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0057, code lost:
        r12 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0058, code lost:
        r3 = r13;
        r13 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005b, code lost:
        r12 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x007c, code lost:
        r13.close();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x005b A[ExcHandler: all (th java.lang.Throwable), Splitter:B:1:0x0008] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x007c  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0092  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00b8  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00c4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.database.Cursor getNsdsElementsWithXPath(android.net.Uri r13) {
        /*
            r12 = this;
            long r0 = android.os.Binder.clearCallingIdentity()
            r2 = 0
            java.lang.String r3 = "tag_name"
            java.lang.String r13 = r13.getQueryParameter(r3)     // Catch:{ Exception -> 0x005d, all -> 0x005b }
            boolean r3 = android.text.TextUtils.isEmpty(r13)     // Catch:{ Exception -> 0x0057, all -> 0x005b }
            if (r3 == 0) goto L_0x001d
            java.lang.String r12 = LOG_TAG     // Catch:{ Exception -> 0x0057, all -> 0x005b }
            java.lang.String r3 = "Empty tag name. Return null"
            com.sec.internal.log.IMSLog.i(r12, r3)     // Catch:{ Exception -> 0x0057, all -> 0x005b }
            android.os.Binder.restoreCallingIdentity(r0)
            return r2
        L_0x001d:
            r3 = 1
            java.lang.String[] r6 = new java.lang.String[r3]     // Catch:{ Exception -> 0x0057, all -> 0x005b }
            java.lang.String r3 = "device_config"
            r10 = 0
            r6[r10] = r3     // Catch:{ Exception -> 0x0057, all -> 0x005b }
            android.content.Context r12 = r12.getContext()     // Catch:{ Exception -> 0x0057, all -> 0x005b }
            android.content.ContentResolver r4 = r12.getContentResolver()     // Catch:{ Exception -> 0x0057, all -> 0x005b }
            android.net.Uri r5 = com.sec.internal.constants.ims.entitilement.EntitlementConfigContract.DeviceConfig.CONTENT_URI     // Catch:{ Exception -> 0x0057, all -> 0x005b }
            r7 = 0
            r8 = 0
            r9 = 0
            android.database.Cursor r12 = r4.query(r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x0057, all -> 0x005b }
            if (r12 == 0) goto L_0x004e
            boolean r3 = r12.moveToFirst()     // Catch:{ Exception -> 0x0048, all -> 0x0043 }
            if (r3 == 0) goto L_0x004e
            java.lang.String r2 = r12.getString(r10)     // Catch:{ Exception -> 0x0048, all -> 0x0043 }
            goto L_0x004e
        L_0x0043:
            r13 = move-exception
            r2 = r12
            r12 = r13
            goto L_0x00c2
        L_0x0048:
            r3 = move-exception
            r11 = r13
            r13 = r12
            r12 = r3
            r3 = r11
            goto L_0x0060
        L_0x004e:
            if (r12 == 0) goto L_0x0053
            r12.close()
        L_0x0053:
            android.os.Binder.restoreCallingIdentity(r0)
            goto L_0x0083
        L_0x0057:
            r12 = move-exception
            r3 = r13
            r13 = r2
            goto L_0x0060
        L_0x005b:
            r12 = move-exception
            goto L_0x00c2
        L_0x005d:
            r12 = move-exception
            r13 = r2
            r3 = r13
        L_0x0060:
            java.lang.String r4 = LOG_TAG     // Catch:{ all -> 0x00c0 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00c0 }
            r5.<init>()     // Catch:{ all -> 0x00c0 }
            java.lang.String r6 = "SQL exception while parseDeviceConfig "
            r5.append(r6)     // Catch:{ all -> 0x00c0 }
            java.lang.String r12 = r12.getMessage()     // Catch:{ all -> 0x00c0 }
            r5.append(r12)     // Catch:{ all -> 0x00c0 }
            java.lang.String r12 = r5.toString()     // Catch:{ all -> 0x00c0 }
            com.sec.internal.log.IMSLog.s(r4, r12)     // Catch:{ all -> 0x00c0 }
            if (r13 == 0) goto L_0x007f
            r13.close()
        L_0x007f:
            android.os.Binder.restoreCallingIdentity(r0)
            r13 = r3
        L_0x0083:
            android.database.MatrixCursor r12 = new android.database.MatrixCursor
            java.lang.String r0 = "element_name"
            java.lang.String r1 = "element_value"
            java.lang.String[] r0 = new java.lang.String[]{r0, r1}
            r12.<init>(r0)
            if (r2 == 0) goto L_0x00b8
            java.util.Map r13 = com.sec.internal.ims.entitlement.util.ConfigElementExtractor.getAllElements(r2, r13)
            java.util.Set r0 = r13.keySet()
            java.util.Iterator r0 = r0.iterator()
        L_0x009e:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x00bf
            java.lang.Object r1 = r0.next()
            java.lang.String r1 = (java.lang.String) r1
            java.lang.Object r2 = r13.get(r1)
            java.lang.String r2 = (java.lang.String) r2
            java.lang.String[] r1 = new java.lang.String[]{r1, r2}
            r12.addRow(r1)
            goto L_0x009e
        L_0x00b8:
            java.lang.String r13 = LOG_TAG
            java.lang.String r0 = "Device Config is null: "
            com.sec.internal.log.IMSLog.e(r13, r0)
        L_0x00bf:
            return r12
        L_0x00c0:
            r12 = move-exception
            r2 = r13
        L_0x00c2:
            if (r2 == 0) goto L_0x00c7
            r2.close()
        L_0x00c7:
            android.os.Binder.restoreCallingIdentity(r0)
            throw r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.config.persist.EntitlementConfigProvider.getNsdsElementsWithXPath(android.net.Uri):android.database.Cursor");
    }

    private Cursor getJanskyConfigXmlBlock() {
        return getXmlConfigByTag("//janskyConfig");
    }

    private Cursor getRcsConfigXmlBlock() {
        return getXmlConfigByTag("//RCSConfig/wap-provisioningdoc|//wap-provisioningdoc");
    }

    private Cursor getXmlConfigbyTagUri(Uri uri) {
        String queryParameter = uri.getQueryParameter("tag_name");
        if (!TextUtils.isEmpty(queryParameter)) {
            return getXmlConfigByTag(queryParameter);
        }
        IMSLog.i(LOG_TAG, "Empty tag name. Return null");
        return null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: java.lang.String} */
    /* JADX WARNING: type inference failed for: r1v0 */
    /* JADX WARNING: type inference failed for: r1v1, types: [android.database.Cursor] */
    /* JADX WARNING: type inference failed for: r1v3 */
    /* JADX WARNING: type inference failed for: r1v4 */
    /* JADX WARNING: type inference failed for: r1v5 */
    /* JADX WARNING: type inference failed for: r1v7 */
    /* JADX WARNING: type inference failed for: r1v8 */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002b, code lost:
        r9.close();
        r1 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004d, code lost:
        if (r9 != null) goto L_0x002b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0050, code lost:
        r9 = new android.database.MatrixCursor(new java.lang.String[]{com.sec.internal.constants.ims.entitilement.EntitlementConfigContract.DeviceConfig.XML_CONFIG});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005c, code lost:
        if (r1 == 0) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005e, code lost:
        r9.addRow(new java.lang.String[]{com.sec.internal.ims.entitlement.util.CompleteXMLBlockExtractor.getXmlBlockForElement(r1, r10)});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x006a, code lost:
        com.sec.internal.log.IMSLog.e(LOG_TAG, "Device Config is null: ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0071, code lost:
        return r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0029, code lost:
        if (r9 != null) goto L_0x002b;
     */
    /* JADX WARNING: Failed to insert additional move for type inference */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0076  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.database.Cursor getXmlConfigByTag(java.lang.String r10) {
        /*
            r9 = this;
            r0 = 1
            r1 = 0
            java.lang.String[] r4 = new java.lang.String[r0]     // Catch:{ Exception -> 0x0031, all -> 0x002f }
            java.lang.String r0 = "device_config"
            r8 = 0
            r4[r8] = r0     // Catch:{ Exception -> 0x0031, all -> 0x002f }
            android.content.Context r9 = r9.getContext()     // Catch:{ Exception -> 0x0031, all -> 0x002f }
            android.content.ContentResolver r2 = r9.getContentResolver()     // Catch:{ Exception -> 0x0031, all -> 0x002f }
            android.net.Uri r3 = com.sec.internal.constants.ims.entitilement.EntitlementConfigContract.DeviceConfig.CONTENT_URI     // Catch:{ Exception -> 0x0031, all -> 0x002f }
            r5 = 0
            r6 = 0
            r7 = 0
            android.database.Cursor r9 = r2.query(r3, r4, r5, r6, r7)     // Catch:{ Exception -> 0x0031, all -> 0x002f }
            if (r9 == 0) goto L_0x0029
            boolean r0 = r9.moveToFirst()     // Catch:{ Exception -> 0x0027 }
            if (r0 == 0) goto L_0x0029
            java.lang.String r1 = r9.getString(r8)     // Catch:{ Exception -> 0x0027 }
            goto L_0x0029
        L_0x0027:
            r0 = move-exception
            goto L_0x0033
        L_0x0029:
            if (r9 == 0) goto L_0x0050
        L_0x002b:
            r9.close()
            goto L_0x0050
        L_0x002f:
            r10 = move-exception
            goto L_0x0074
        L_0x0031:
            r0 = move-exception
            r9 = r1
        L_0x0033:
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x0072 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0072 }
            r3.<init>()     // Catch:{ all -> 0x0072 }
            java.lang.String r4 = "SQL exception while parseDeviceConfig "
            r3.append(r4)     // Catch:{ all -> 0x0072 }
            java.lang.String r0 = r0.toString()     // Catch:{ all -> 0x0072 }
            r3.append(r0)     // Catch:{ all -> 0x0072 }
            java.lang.String r0 = r3.toString()     // Catch:{ all -> 0x0072 }
            com.sec.internal.log.IMSLog.s(r2, r0)     // Catch:{ all -> 0x0072 }
            if (r9 == 0) goto L_0x0050
            goto L_0x002b
        L_0x0050:
            android.database.MatrixCursor r9 = new android.database.MatrixCursor
            java.lang.String r0 = "xml_config"
            java.lang.String[] r0 = new java.lang.String[]{r0}
            r9.<init>(r0)
            if (r1 == 0) goto L_0x006a
            java.lang.String r10 = com.sec.internal.ims.entitlement.util.CompleteXMLBlockExtractor.getXmlBlockForElement(r1, r10)
            java.lang.String[] r10 = new java.lang.String[]{r10}
            r9.addRow(r10)
            goto L_0x0071
        L_0x006a:
            java.lang.String r10 = LOG_TAG
            java.lang.String r0 = "Device Config is null: "
            com.sec.internal.log.IMSLog.e(r10, r0)
        L_0x0071:
            return r9
        L_0x0072:
            r10 = move-exception
            r1 = r9
        L_0x0074:
            if (r1 == 0) goto L_0x0079
            r1.close()
        L_0x0079:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.config.persist.EntitlementConfigProvider.getXmlConfigByTag(java.lang.String):android.database.Cursor");
    }

    private synchronized void connectToEntitlementConfigService() {
        IMSLog.i(LOG_TAG, "connectToEntitlementConfigService()");
        Intent intent = new Intent();
        intent.setClassName("com.sec.imsservice", "com.sec.internal.ims.entitlement.config.EntitlementConfigService");
        this.mSvcConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                IMSLog.i(EntitlementConfigProvider.LOG_TAG, "onServiceConnected: Connected to EntitlementConfigService.");
                if (MigrationHelper.checkMigrateDB(EntitlementConfigProvider.this.mContext)) {
                    IMSLog.i(EntitlementConfigProvider.LOG_TAG, "Connect DB");
                    EntitlementConfigProvider.this.mDatabaseHelper = new DatabaseHelper(EntitlementConfigProvider.this.mContext);
                }
                EntitlementConfigProvider.this.mService = new Messenger(iBinder);
            }

            public void onServiceDisconnected(ComponentName componentName) {
                IMSLog.i(EntitlementConfigProvider.LOG_TAG, "onServiceDisconnected: Disconnected.");
                EntitlementConfigProvider.this.mService = null;
            }
        };
        ContextExt.bindServiceAsUser(getContext(), intent, this.mSvcConn, 1, ContextExt.CURRENT_OR_SELF);
    }
}
