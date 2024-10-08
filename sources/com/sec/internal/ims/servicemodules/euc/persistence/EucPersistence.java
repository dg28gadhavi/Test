package com.sec.internal.ims.servicemodules.euc.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.tapi.UserConsentProviderContract;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.servicemodules.euc.data.AutoconfUserConsentData;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IDialogData;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import com.sec.internal.ims.servicemodules.euc.locale.DeviceLocale;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucFactory;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EucPersistence implements IEucPersistence {
    private static final String LOG_TAG = "EucPersistence";
    private SQLiteDatabase mDb = null;
    private final IEucFactory mEucFactory;
    private final EucSQLiteHelper mEucSQLiteHelper;
    private boolean mIsDbOpened;

    public EucPersistence(Context context, IEucFactory iEucFactory) {
        this.mEucSQLiteHelper = EucSQLiteHelper.getInstance((Context) Preconditions.checkNotNull(context));
        this.mEucFactory = (IEucFactory) Preconditions.checkNotNull(iEucFactory);
    }

    public void updateEuc(EucMessageKey eucMessageKey, EucState eucState, String str) throws EucPersistenceException {
        String str2 = LOG_TAG;
        Log.d(str2, "updateEuc with " + eucMessageKey + " to state=" + eucState + " or PIN=" + str);
        if (this.mDb != null) {
            String str3 = UserConsentProviderContract.UserConsentList.ID + "='" + eucMessageKey.getEucId() + "' AND " + "TYPE" + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + eucMessageKey.getEucType().getId() + " AND " + UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY + "='" + eucMessageKey.getOwnIdentity() + "' AND " + UserConsentProviderContract.UserConsentList.REMOTE_URI + "='" + eucMessageKey.getRemoteUri().encode() + "'";
            IMSLog.s(str2, "update EUCData where " + str3);
            ContentValues contentValues = new ContentValues();
            contentValues.put(UserConsentProviderContract.UserConsentList.STATE, Integer.valueOf(eucState.getId()));
            if (str != null) {
                contentValues.put("USER_PIN", str);
            }
            if (this.mDb.update("EUCRDATA", contentValues, str3, (String[]) null) == 0) {
                throw new EucPersistenceException("No records were updated");
            }
            return;
        }
        throw new EucPersistenceException("db instance is null, no access to EUCR database");
    }

    public void insertEuc(IEucData iEucData) throws EucPersistenceException {
        if (iEucData == null) {
            throw new EucPersistenceException("eucData is null");
        } else if (this.mDb != null) {
            String str = LOG_TAG;
            IMSLog.s(str, "insert EUCData to database for User Identity" + iEucData.getOwnIdentity());
            ContentValues contentValues = new ContentValues();
            contentValues.put(UserConsentProviderContract.UserConsentList.ID, iEucData.getId());
            contentValues.put(CloudMessageProviderContract.DataTypes.VVMPIN, Integer.valueOf(iEucData.getPin() ? 1 : 0));
            contentValues.put("EXTERNAL", Integer.valueOf(iEucData.getExternal() ? 1 : 0));
            contentValues.put(UserConsentProviderContract.UserConsentList.STATE, Integer.valueOf(iEucData.getState().getId()));
            contentValues.put("TYPE", Integer.valueOf(iEucData.getType().getId()));
            contentValues.put(UserConsentProviderContract.UserConsentList.REMOTE_URI, iEucData.getRemoteUri().encode());
            contentValues.put(UserConsentProviderContract.UserConsentList.TIMESTAMP, Long.valueOf(iEucData.getTimestamp()));
            contentValues.put("TIMEOUT", iEucData.getTimeOut());
            contentValues.put(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY, iEucData.getOwnIdentity());
            if (this.mDb.insert("EUCRDATA", (String) null, contentValues) == -1) {
                throw new EucPersistenceException("No records were inserted");
            }
        } else {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        }
    }

    public void insertDialogs(IEucQuery iEucQuery) throws EucPersistenceException {
        Log.d(LOG_TAG, "insert DialogData to database");
        if (iEucQuery != null) {
            SQLiteDatabase sQLiteDatabase = this.mDb;
            if (sQLiteDatabase != null) {
                sQLiteDatabase.beginTransaction();
                try {
                    Iterator it = iEucQuery.iterator();
                    while (it.hasNext()) {
                        IDialogData iDialogData = (IDialogData) it.next();
                        if (iDialogData != null) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(UserConsentProviderContract.UserConsentList.ID, iDialogData.getKey().getEucId());
                            contentValues.put("TYPE", Integer.valueOf(iEucQuery.getEucData().getType().getId()));
                            contentValues.put("LANGUAGE", iDialogData.getLanguage());
                            contentValues.put("TEXT", iDialogData.getText());
                            contentValues.put("SUBJECT", iDialogData.getSubject());
                            contentValues.put("ACCEPT_BUTTON", iDialogData.getAcceptButton());
                            contentValues.put("REJECT_BUTTON", iDialogData.getRejectButton());
                            contentValues.put(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY, iEucQuery.getEucData().getOwnIdentity());
                            contentValues.put(UserConsentProviderContract.UserConsentList.REMOTE_URI, iDialogData.getKey().getRemoteUri().encode());
                            if (this.mDb.insert("DIALOG", (String) null, contentValues) == -1) {
                                throw new EucPersistenceException("No records were inserted");
                            }
                        }
                    }
                    this.mDb.setTransactionSuccessful();
                } finally {
                    this.mDb.endTransaction();
                }
            } else {
                throw new EucPersistenceException("db instance is null, no access to EUCR database");
            }
        } else {
            throw new EucPersistenceException("DialogData is null");
        }
    }

    public void insertAutoconfUserConsent(AutoconfUserConsentData autoconfUserConsentData) throws EucPersistenceException {
        int i;
        Log.d(LOG_TAG, "insertAutoconfUserConsent");
        if (autoconfUserConsentData == null) {
            throw new EucPersistenceException("userConsentData is null");
        } else if (this.mDb != null) {
            String str = "config" + autoconfUserConsentData.getTimestamp();
            ContentValues contentValues = new ContentValues();
            contentValues.put(UserConsentProviderContract.UserConsentList.ID, str);
            if (autoconfUserConsentData.isUserAccept()) {
                i = EucState.ACCEPTED.getId();
            } else {
                i = EucState.REJECTED.getId();
            }
            contentValues.put(UserConsentProviderContract.UserConsentList.STATE, Integer.valueOf(i));
            EucType eucType = EucType.EULA;
            contentValues.put("TYPE", Integer.valueOf(eucType.getId()));
            contentValues.put(UserConsentProviderContract.UserConsentList.TIMESTAMP, Long.valueOf(autoconfUserConsentData.getTimestamp()));
            contentValues.put(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY, autoconfUserConsentData.getOwnIdentity());
            if (this.mDb.insert("EUCRDATA", (String) null, contentValues) != -1) {
                contentValues.clear();
                contentValues.put(UserConsentProviderContract.UserConsentList.ID, str);
                contentValues.put("TYPE", Integer.valueOf(eucType.getId()));
                contentValues.put("LANGUAGE", DeviceLocale.DEFAULT_LANG_VALUE);
                contentValues.put("SUBJECT", autoconfUserConsentData.getConsentMsgTitle());
                contentValues.put("TEXT", autoconfUserConsentData.getConsentMsgMessage());
                contentValues.put(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY, autoconfUserConsentData.getOwnIdentity());
                if (this.mDb.insert("DIALOG", (String) null, contentValues) == -1) {
                    throw new EucPersistenceException("No records were inserted");
                }
                return;
            }
            throw new EucPersistenceException("No records were inserted");
        } else {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        }
    }

    public List<IDialogData> getDialogs(List<String> list, EucType eucType, String str, List<String> list2) throws EucPersistenceException, IllegalArgumentException {
        Cursor query;
        Throwable th;
        String str2 = str;
        String str3 = LOG_TAG;
        IMSLog.s(str3, "getDialogsForId: ids: " + Arrays.toString(list.toArray()) + ", type: " + eucType.getId() + " lang: " + str2 + " ownIdentity: " + Arrays.toString(list2.toArray()));
        if (this.mDb == null) {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        } else if (list.isEmpty() || list2.isEmpty()) {
            throw new EucPersistenceException("eucIds list (size=" + list.size() + ") or ownIdentities list (size =" + list2.size() + ") is empty");
        } else {
            ArrayList arrayList = new ArrayList();
            StringBuilder sb = new StringBuilder();
            Iterator<String> it = list.iterator();
            sb.append("(");
            sb.append(UserConsentProviderContract.UserConsentList.ID);
            sb.append("='");
            sb.append(it.next());
            sb.append("'");
            while (it.hasNext()) {
                sb.append(" OR ");
                sb.append(UserConsentProviderContract.UserConsentList.ID);
                sb.append("='");
                sb.append(it.next());
                sb.append("'");
            }
            sb.append(")");
            sb.append(" AND (");
            sb.append("LANGUAGE");
            sb.append("='");
            sb.append(str2);
            sb.append("' OR ");
            sb.append("LANGUAGE");
            sb.append("='def')");
            sb.append(" AND ");
            sb.append("TYPE");
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(eucType.getId());
            Iterator<String> it2 = list2.iterator();
            sb.append(" AND (");
            sb.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            sb.append("='");
            sb.append(it2.next());
            sb.append("'");
            while (it2.hasNext()) {
                sb.append(" OR ");
                sb.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
                sb.append("='");
                sb.append(it2.next());
                sb.append("'");
            }
            sb.append(")");
            String sb2 = sb.toString();
            String str4 = LOG_TAG;
            IMSLog.s(str4, "select from DIALOG table where " + sb2);
            try {
                query = this.mDb.query("DIALOG", (String[]) null, sb2, (String[]) null, (String) null, (String) null, (String) null, (String) null);
                if (query != null) {
                    while (query.moveToNext()) {
                        arrayList.add(createDialogData(query));
                    }
                }
                if (query != null) {
                    query.close();
                }
                return arrayList;
            } catch (SQLException e) {
                String str5 = LOG_TAG;
                IMSLog.e(str5, "SQL Exception " + e);
                throw new EucPersistenceException("SQL Exception occured!");
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        throw th;
    }

    public List<IDialogData> getDialogsByTypes(EucState eucState, List<EucType> list, String str, String str2) throws EucPersistenceException, IllegalArgumentException {
        Cursor rawQuery;
        String str3 = LOG_TAG;
        IMSLog.s(str3, "getDialogsByTypes: state: " + eucState.getId() + "type: " + Arrays.toString(list.toArray()) + " lang: " + str + " ownIdentity: " + str2);
        if (this.mDb == null) {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        } else if (!list.isEmpty()) {
            StringBuilder sb = new StringBuilder("SELECT * FROM ");
            sb.append("DIALOG");
            sb.append(" JOIN ");
            sb.append("EUCRDATA");
            sb.append(" ON ");
            sb.append("DIALOG");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.ID);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append("EUCRDATA");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.ID);
            sb.append(" AND ");
            sb.append("DIALOG");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append("EUCRDATA");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            sb.append(" AND ");
            sb.append("DIALOG");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.REMOTE_URI);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append("EUCRDATA");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.REMOTE_URI);
            sb.append(" AND ");
            sb.append("DIALOG");
            sb.append(".");
            sb.append("TYPE");
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append("EUCRDATA");
            sb.append(".");
            sb.append("TYPE");
            sb.append(" WHERE ");
            sb.append("EUCRDATA");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.STATE);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(eucState.getId());
            sb.append(" AND ");
            sb.append("DIALOG");
            sb.append(".");
            sb.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            sb.append("='");
            sb.append(str2);
            sb.append("'");
            sb.append(" AND (");
            sb.append("DIALOG");
            sb.append(".");
            sb.append("LANGUAGE");
            sb.append("='");
            sb.append(DeviceLocale.DEFAULT_LANG_VALUE);
            sb.append("'");
            if (!str.equals(DeviceLocale.DEFAULT_LANG_VALUE)) {
                sb.append(" OR ");
                sb.append("DIALOG");
                sb.append(".");
                sb.append("LANGUAGE");
                sb.append("='");
                sb.append(str);
                sb.append("'");
            }
            sb.append(") AND (");
            Iterator<EucType> it = list.iterator();
            if (it.hasNext()) {
                sb.append("DIALOG");
                sb.append(".");
                sb.append("TYPE");
                sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                sb.append(it.next().getId());
                while (it.hasNext()) {
                    sb.append(" OR ");
                    sb.append("DIALOG");
                    sb.append(".");
                    sb.append("TYPE");
                    sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    sb.append(it.next().getId());
                }
            }
            sb.append(");");
            String sb2 = sb.toString();
            String str4 = LOG_TAG;
            IMSLog.s(str4, "getDialogsByTypes query: " + sb2);
            ArrayList arrayList = new ArrayList();
            try {
                rawQuery = this.mDb.rawQuery(sb2, (String[]) null);
                if (rawQuery != null) {
                    while (rawQuery.moveToNext()) {
                        arrayList.add(createDialogData(rawQuery));
                    }
                }
                if (rawQuery != null) {
                    rawQuery.close();
                }
                String str5 = LOG_TAG;
                Log.d(str5, "getDialogsByTypes return list size: " + arrayList.size());
                return arrayList;
            } catch (SQLException e) {
                String str6 = LOG_TAG;
                Log.e(str6, "SQL Exception " + e);
                throw new EucPersistenceException("SQL Exception occured!");
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else {
            throw new IllegalArgumentException("types list is empty");
        }
        throw th;
    }

    public List<IEucData> getAllEucs(EucState eucState, EucType eucType, String str) throws EucPersistenceException {
        String str2 = LOG_TAG;
        IMSLog.s(str2, "getAllEucs: state: " + eucState.getId() + " type: " + eucType.getId() + " ownIdentity: " + str);
        if (this.mDb != null) {
            String str3 = UserConsentProviderContract.UserConsentList.STATE + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + eucState.getId() + " AND " + "TYPE" + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + eucType.getId() + " AND " + UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY + "=\"" + str + CmcConstants.E_NUM_STR_QUOTE;
            IMSLog.s(str2, "getAllEucs where " + str3);
            return queryEucDataUsingSelection(str3);
        }
        throw new EucPersistenceException("db instance is null, no access to EUCR database");
    }

    public List<IEucData> getAllEucs(List<EucState> list, EucType eucType, String str) throws EucPersistenceException, IllegalArgumentException {
        String str2 = LOG_TAG;
        IMSLog.s(str2, "getAllEucs: state: " + list.toString() + " type: " + eucType.getId() + " ownIdentity: " + str);
        if (this.mDb == null) {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        } else if (!list.isEmpty()) {
            StringBuilder sb = new StringBuilder("(");
            Iterator<EucState> it = list.iterator();
            sb.append(UserConsentProviderContract.UserConsentList.STATE);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(it.next().getId());
            while (it.hasNext()) {
                sb.append(" OR ");
                sb.append(UserConsentProviderContract.UserConsentList.STATE);
                sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                sb.append(it.next().getId());
            }
            sb.append(") AND ");
            sb.append("TYPE");
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(eucType.getId());
            sb.append(" AND ");
            sb.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            sb.append("=\"");
            sb.append(str);
            sb.append(CmcConstants.E_NUM_STR_QUOTE);
            String str3 = LOG_TAG;
            IMSLog.s(str3, "getAllEucs where " + sb);
            return queryEucDataUsingSelection(sb.toString());
        } else {
            throw new IllegalArgumentException("states list is empty");
        }
    }

    public List<IEucData> getAllEucs(EucState eucState, List<EucType> list, String str) throws EucPersistenceException, IllegalArgumentException {
        String str2 = LOG_TAG;
        IMSLog.s(str2, "getAllEucs: states: " + eucState.getId() + " types: " + list.toString() + " ownIdentity: " + str);
        if (this.mDb == null) {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        } else if (!list.isEmpty()) {
            StringBuilder sb = new StringBuilder("(");
            Iterator<EucType> it = list.iterator();
            if (it.hasNext()) {
                sb.append("TYPE");
                sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                sb.append(it.next().getId());
                while (it.hasNext()) {
                    sb.append(" OR ");
                    sb.append("TYPE");
                    sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    sb.append(it.next().getId());
                }
            }
            sb.append(") AND ");
            sb.append(UserConsentProviderContract.UserConsentList.STATE);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(eucState.getId());
            sb.append(" AND ");
            sb.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            sb.append("=\"");
            sb.append(str);
            sb.append(CmcConstants.E_NUM_STR_QUOTE);
            String str3 = LOG_TAG;
            IMSLog.s(str3, "getAllEucs where " + sb);
            return queryEucDataUsingSelection(sb.toString());
        } else {
            throw new IllegalArgumentException("types list is empty");
        }
    }

    public List<IEucData> getAllEucs(List<EucState> list, List<EucType> list2, String str) throws EucPersistenceException, IllegalArgumentException {
        String str2 = LOG_TAG;
        IMSLog.s(str2, "getAllEucs: states: " + list.toString() + " types: " + list2.toString() + " ownIdentity: " + str);
        if (this.mDb == null) {
            throw new EucPersistenceException("db instance is null, no access to EUCR database");
        } else if (list2.isEmpty() || list.isEmpty()) {
            throw new EucPersistenceException("types list (size=" + list2.size() + ") or states list (size =" + list.size() + ") is empty");
        } else {
            StringBuilder sb = new StringBuilder("(");
            Iterator<EucType> it = list2.iterator();
            if (it.hasNext()) {
                sb.append("TYPE");
                sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                sb.append(it.next().getId());
                while (it.hasNext()) {
                    sb.append(" OR ");
                    sb.append("TYPE");
                    sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    sb.append(it.next().getId());
                }
            }
            sb.append(") AND (");
            Iterator<EucState> it2 = list.iterator();
            if (it2.hasNext()) {
                sb.append(UserConsentProviderContract.UserConsentList.STATE);
                sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                sb.append(it2.next().getId());
                while (it2.hasNext()) {
                    sb.append(" OR ");
                    sb.append(UserConsentProviderContract.UserConsentList.STATE);
                    sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    sb.append(it2.next().getId());
                }
            }
            sb.append(") AND ");
            sb.append(UserConsentProviderContract.UserConsentList.SUBSCRIBER_IDENTITY);
            sb.append("=\"");
            sb.append(str);
            sb.append(CmcConstants.E_NUM_STR_QUOTE);
            String str3 = LOG_TAG;
            IMSLog.s(str3, "getAllEucs where " + sb);
            return queryEucDataUsingSelection(sb.toString());
        }
    }

    private List<IEucData> queryEucDataUsingSelection(String str) throws EucPersistenceException {
        Cursor query;
        ArrayList arrayList = new ArrayList();
        try {
            query = this.mDb.query("EUCRDATA", (String[]) null, str, (String[]) null, (String) null, (String) null, (String) null, (String) null);
            if (query != null) {
                if (query.moveToFirst()) {
                    while (!query.isAfterLast()) {
                        arrayList.add(createEucData(query));
                        query.moveToNext();
                    }
                }
            }
            if (query != null) {
                query.close();
            }
            return arrayList;
        } catch (SQLException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "SQL Exception " + e);
            throw new EucPersistenceException("SQL Exception occured!");
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x00b5 A[Catch:{ all -> 0x00a8, all -> 0x00ad, SQLException -> 0x00b9 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.euc.data.IEucData getEucByKey(com.sec.internal.ims.servicemodules.euc.data.EucMessageKey r15) throws com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException {
        /*
            r14 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "getEucByKey: eucMessageKey="
            r1.append(r2)
            r1.append(r15)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.s(r0, r1)
            android.database.sqlite.SQLiteDatabase r1 = r14.mDb
            if (r1 == 0) goto L_0x00d8
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            java.lang.String r2 = "ID"
            r1.<init>(r2)
            java.lang.String r2 = "='"
            r1.append(r2)
            java.lang.String r3 = r15.getEucId()
            r1.append(r3)
            java.lang.String r3 = "' AND "
            r1.append(r3)
            java.lang.String r3 = "TYPE"
            r1.append(r3)
            java.lang.String r3 = "="
            r1.append(r3)
            com.sec.internal.ims.servicemodules.euc.data.EucType r3 = r15.getEucType()
            int r3 = r3.getId()
            r1.append(r3)
            java.lang.String r3 = " AND "
            r1.append(r3)
            java.lang.String r4 = "SUBSCRIBER_IDENTITY"
            r1.append(r4)
            r1.append(r2)
            java.lang.String r4 = r15.getOwnIdentity()
            r1.append(r4)
            java.lang.String r4 = "'"
            r1.append(r4)
            r1.append(r3)
            java.lang.String r3 = "REMOTE_URI"
            r1.append(r3)
            r1.append(r2)
            com.sec.ims.util.ImsUri r15 = r15.getRemoteUri()
            r1.append(r15)
            r1.append(r4)
            java.lang.String r8 = r1.toString()
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            java.lang.String r1 = "getEucByKey where "
            r15.append(r1)
            r15.append(r8)
            java.lang.String r15 = r15.toString()
            com.sec.internal.log.IMSLog.s(r0, r15)
            android.database.sqlite.SQLiteDatabase r5 = r14.mDb     // Catch:{ SQLException -> 0x00b9 }
            java.lang.String r6 = "EUCRDATA"
            r7 = 0
            r9 = 0
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            android.database.Cursor r15 = r5.query(r6, r7, r8, r9, r10, r11, r12, r13)     // Catch:{ SQLException -> 0x00b9 }
            if (r15 == 0) goto L_0x00b2
            boolean r0 = r15.moveToFirst()     // Catch:{ all -> 0x00a8 }
            if (r0 == 0) goto L_0x00b2
            com.sec.internal.ims.servicemodules.euc.data.IEucData r14 = r14.createEucData(r15)     // Catch:{ all -> 0x00a8 }
            goto L_0x00b3
        L_0x00a8:
            r14 = move-exception
            r15.close()     // Catch:{ all -> 0x00ad }
            goto L_0x00b1
        L_0x00ad:
            r15 = move-exception
            r14.addSuppressed(r15)     // Catch:{ SQLException -> 0x00b9 }
        L_0x00b1:
            throw r14     // Catch:{ SQLException -> 0x00b9 }
        L_0x00b2:
            r14 = 0
        L_0x00b3:
            if (r15 == 0) goto L_0x00b8
            r15.close()     // Catch:{ SQLException -> 0x00b9 }
        L_0x00b8:
            return r14
        L_0x00b9:
            r14 = move-exception
            java.lang.String r15 = LOG_TAG
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "SQL Exception "
            r0.append(r1)
            r0.append(r14)
            java.lang.String r14 = r0.toString()
            android.util.Log.e(r15, r14)
            com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException r14 = new com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException
            java.lang.String r15 = "SQL Exception occured!"
            r14.<init>(r15)
            throw r14
        L_0x00d8:
            com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException r14 = new com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException
            java.lang.String r15 = "db instance is null, no access to EUCR database"
            r14.<init>(r15)
            throw r14
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.euc.persistence.EucPersistence.getEucByKey(com.sec.internal.ims.servicemodules.euc.data.EucMessageKey):com.sec.internal.ims.servicemodules.euc.data.IEucData");
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0118 A[Catch:{ all -> 0x0109, all -> 0x010f, SQLException -> 0x011c }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.euc.data.IEucData getVolatileEucByMostRecentTimeout(java.util.List<java.lang.String> r20) throws com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException {
        /*
            r19 = this;
            r0 = r19
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "getVolatileEucByMostRecentTimeout for identities: "
            r2.append(r3)
            java.lang.Object[] r3 = r20.toArray()
            java.lang.String r3 = java.util.Arrays.toString(r3)
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.s(r1, r2)
            android.database.sqlite.SQLiteDatabase r1 = r0.mDb
            if (r1 == 0) goto L_0x0143
            boolean r1 = r20.isEmpty()
            if (r1 != 0) goto L_0x013b
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            java.lang.String r2 = "("
            r1.<init>(r2)
            java.lang.String r2 = "STATE"
            r1.append(r2)
            java.lang.String r3 = "="
            r1.append(r3)
            com.sec.internal.ims.servicemodules.euc.data.EucState r4 = com.sec.internal.ims.servicemodules.euc.data.EucState.ACCEPTED_NOT_SENT
            int r4 = r4.getId()
            r1.append(r4)
            java.lang.String r4 = " OR "
            r1.append(r4)
            r1.append(r2)
            r1.append(r3)
            com.sec.internal.ims.servicemodules.euc.data.EucState r5 = com.sec.internal.ims.servicemodules.euc.data.EucState.REJECTED_NOT_SENT
            int r5 = r5.getId()
            r1.append(r5)
            r1.append(r4)
            r1.append(r2)
            r1.append(r3)
            com.sec.internal.ims.servicemodules.euc.data.EucState r2 = com.sec.internal.ims.servicemodules.euc.data.EucState.NONE
            int r2 = r2.getId()
            r1.append(r2)
            java.lang.String r2 = ")"
            r1.append(r2)
            java.lang.String r5 = " AND "
            r1.append(r5)
            java.lang.String r5 = "TYPE"
            r1.append(r5)
            r1.append(r3)
            com.sec.internal.ims.servicemodules.euc.data.EucType r5 = com.sec.internal.ims.servicemodules.euc.data.EucType.VOLATILE
            int r5 = r5.getId()
            r1.append(r5)
            java.util.Iterator r5 = r20.iterator()
            boolean r6 = r5.hasNext()
            if (r6 == 0) goto L_0x00d1
            java.lang.String r6 = " AND ("
            r1.append(r6)
            java.lang.String r6 = "SUBSCRIBER_IDENTITY"
            r1.append(r6)
            r1.append(r3)
            java.lang.String r7 = "=\""
            r1.append(r7)
            java.lang.Object r8 = r5.next()
            java.lang.String r8 = (java.lang.String) r8
            r1.append(r8)
            java.lang.String r8 = "\""
            r1.append(r8)
        L_0x00af:
            boolean r9 = r5.hasNext()
            if (r9 == 0) goto L_0x00ce
            r1.append(r4)
            r1.append(r6)
            r1.append(r3)
            r1.append(r7)
            java.lang.Object r9 = r5.next()
            java.lang.String r9 = (java.lang.String) r9
            r1.append(r9)
            r1.append(r8)
            goto L_0x00af
        L_0x00ce:
            r1.append(r2)
        L_0x00d1:
            java.lang.String r13 = r1.toString()
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "getVolatileEucByMostRecentTimeout where "
            r2.append(r3)
            r2.append(r13)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.s(r1, r2)
            android.database.sqlite.SQLiteDatabase r10 = r0.mDb     // Catch:{ SQLException -> 0x011c }
            java.lang.String r11 = "EUCRDATA"
            r12 = 0
            r14 = 0
            r15 = 0
            r16 = 0
            java.lang.String r17 = "TIMEOUT"
            r18 = 0
            android.database.Cursor r1 = r10.query(r11, r12, r13, r14, r15, r16, r17, r18)     // Catch:{ SQLException -> 0x011c }
            if (r1 == 0) goto L_0x0115
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x0109 }
            if (r2 == 0) goto L_0x0115
            com.sec.internal.ims.servicemodules.euc.data.IEucData r0 = r0.createEucData(r1)     // Catch:{ all -> 0x0109 }
            goto L_0x0116
        L_0x0109:
            r0 = move-exception
            r2 = r0
            r1.close()     // Catch:{ all -> 0x010f }
            goto L_0x0114
        L_0x010f:
            r0 = move-exception
            r1 = r0
            r2.addSuppressed(r1)     // Catch:{ SQLException -> 0x011c }
        L_0x0114:
            throw r2     // Catch:{ SQLException -> 0x011c }
        L_0x0115:
            r0 = 0
        L_0x0116:
            if (r1 == 0) goto L_0x011b
            r1.close()     // Catch:{ SQLException -> 0x011c }
        L_0x011b:
            return r0
        L_0x011c:
            r0 = move-exception
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "SQL Exception "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            android.util.Log.e(r1, r0)
            com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException r0 = new com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException
            java.lang.String r1 = "SQL Exception occured!"
            r0.<init>(r1)
            throw r0
        L_0x013b:
            java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException
            java.lang.String r1 = "identities list is empty"
            r0.<init>(r1)
            throw r0
        L_0x0143:
            com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException r0 = new com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException
            java.lang.String r1 = "db instance is null, no access to EUCR database"
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.euc.persistence.EucPersistence.getVolatileEucByMostRecentTimeout(java.util.List):com.sec.internal.ims.servicemodules.euc.data.IEucData");
    }

    public void open() throws IllegalStateException, EucPersistenceException {
        Log.i(LOG_TAG, "open()");
        Preconditions.checkState(!this.mIsDbOpened, "EucPersistence is already opened!");
        try {
            this.mDb = this.mEucSQLiteHelper.getWritableDatabase();
            this.mIsDbOpened = true;
        } catch (SQLiteException e) {
            throw new EucPersistenceException("Failure, unable to open persistence!", e);
        }
    }

    public void close() throws IllegalStateException {
        Log.i(LOG_TAG, "close()");
        Preconditions.checkState(this.mIsDbOpened, "EucPersistence is already closed!");
        this.mEucSQLiteHelper.close();
        this.mDb = null;
        this.mIsDbOpened = false;
    }

    private IEucData createEucData(Cursor cursor) {
        return this.mEucFactory.createEucData(new EucMessageKey(cursor.getString(0), cursor.getString(9), ((EucType[]) EucType.class.getEnumConstants())[0].getFromId(cursor.getInt(4)), ImsUri.parse(cursor.getString(5))), cursor.getInt(1) == 1, cursor.getString(8), cursor.getInt(2) == 1, ((EucState[]) EucState.class.getEnumConstants())[0].getFromId(cursor.getInt(3)), cursor.getLong(6), Long.valueOf(cursor.getLong(7)));
    }

    private IDialogData createDialogData(Cursor cursor) {
        return this.mEucFactory.createDialogData(new EucMessageKey(cursor.getString(0), cursor.getString(6), ((EucType[]) EucType.class.getEnumConstants())[0].getFromId(cursor.getInt(7)), ImsUri.parse(cursor.getString(8))), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5));
    }
}
