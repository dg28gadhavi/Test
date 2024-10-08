package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.DeletedObject;

public class SummaryQueryBuilder extends QueryBuilderBase {
    private String TAG = SummaryQueryBuilder.class.getSimpleName();

    private int convergeRcsMsgTypes(int i) {
        if (11 == i || 12 == i || 14 == i) {
            return 1;
        }
        return i;
    }

    public SummaryQueryBuilder(MessageStoreClient messageStoreClient, IBufferDBEventListener iBufferDBEventListener) {
        super(messageStoreClient, iBufferDBEventListener);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
    }

    public Cursor querySummaryDBwithResUrl(String str) {
        return this.mBufferDB.queryTablewithResUrl(7, str);
    }

    public int deleteSummaryDBwithResUrl(String str) {
        return this.mBufferDB.deleteTablewithResUrl(7, str);
    }

    public Cursor queryAllPendingNmsEventInSummaryDB() {
        Log.d(this.TAG, "queryAllPendingNmsEventInSummaryDB()");
        return this.mBufferDB.querySummaryTable((String[]) null, "messagetype=?", new String[]{String.valueOf(0)}, (String) null);
    }

    public int updateSummaryDbUsingObject(ParamOMAObject paramOMAObject, int i) {
        String str = this.TAG;
        Log.i(str, "updateSummaryDbUsingObject(): " + paramOMAObject);
        String decodeUrlFromServer = Util.decodeUrlFromServer(paramOMAObject.resourceURL.toString());
        String extractObjIdFromResUrl = Util.extractObjIdFromResUrl(paramOMAObject.resourceURL.toString());
        String lineTelUriFromObjUrl = Util.getLineTelUriFromObjUrl(paramOMAObject.resourceURL.toString());
        String[] strArr = {"*" + extractObjIdFromResUrl, lineTelUriFromObjUrl};
        ContentValues contentValues = new ContentValues();
        contentValues.put("messagetype", Integer.valueOf(convergeRcsMsgTypes(i)));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, paramOMAObject.correlationId);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, paramOMAObject.correlationTag);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, decodeUrlFromServer);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, paramOMAObject.lastModSeq);
        contentValues.put("path", paramOMAObject.path);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, paramOMAObject.parentFolder.toString());
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDERPATH, paramOMAObject.parentFolderPath);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(paramOMAObject.mFlag.getId()));
        return this.mBufferDB.updateTable(7, contentValues, "res_url GLOB ? AND linenum=?", strArr);
    }

    public long insertSummaryDbUsingObjectIfNonExist(ParamOMAObject paramOMAObject, int i) {
        String str = this.TAG;
        Log.i(str, "insertSummaryDbUsingObjectIfNonExist(): " + paramOMAObject);
        Cursor queryTablewithResUrl = this.mBufferDB.queryTablewithResUrl(7, paramOMAObject.resourceURL.toString());
        if (queryTablewithResUrl != null) {
            try {
                if (!queryTablewithResUrl.moveToFirst()) {
                    long insertObjectToSummaryDB = insertObjectToSummaryDB(paramOMAObject, i);
                    queryTablewithResUrl.close();
                    return insertObjectToSummaryDB;
                } else if (queryTablewithResUrl.getInt(queryTablewithResUrl.getColumnIndexOrThrow("messagetype")) != i) {
                    long updateSummaryDbUsingObject = (long) updateSummaryDbUsingObject(paramOMAObject, i);
                    queryTablewithResUrl.close();
                    return updateSummaryDbUsingObject;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else {
            Log.e(this.TAG, "insertSummaryDbUsingObjectIfNonExist search error");
        }
        if (queryTablewithResUrl == null) {
            return 0;
        }
        queryTablewithResUrl.close();
        return 0;
        throw th;
    }

    public long updateSummaryDbUsingMessageType(long j, int i) {
        String str = this.TAG;
        Log.i(str, "updateSummaryDbUsingMessageType(): msgtype: " + i);
        ContentValues contentValues = new ContentValues();
        contentValues.put("messagetype", Integer.valueOf(convergeRcsMsgTypes(i)));
        return (long) this.mBufferDB.updateTable(7, contentValues, "_bufferdbid=?", new String[]{String.valueOf(j)});
    }

    public long insertObjectToSummaryDB(ParamOMAObject paramOMAObject, int i) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("linenum", Util.getLineTelUriFromObjUrl(paramOMAObject.resourceURL.toString()));
        contentValues.put("messagetype", Integer.valueOf(convergeRcsMsgTypes(i)));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, paramOMAObject.correlationId);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, paramOMAObject.correlationTag);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject.resourceURL.toString()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, paramOMAObject.lastModSeq);
        contentValues.put("path", paramOMAObject.path);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, paramOMAObject.parentFolder.toString());
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDERPATH, paramOMAObject.parentFolderPath);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
        contentValues.put("sim_imsi", this.IMSI);
        return this.mBufferDB.insertTable(7, contentValues);
    }

    public long insertNmsEventChangedObjToSummaryDB(ChangedObject changedObject, int i) {
        Log.d(this.TAG, "insertNmsEventChangedObjToSummaryDB()");
        ContentValues contentValues = new ContentValues();
        contentValues.put("linenum", Util.getLineTelUriFromObjUrl(changedObject.resourceURL.toString()));
        contentValues.put("messagetype", Integer.valueOf(convergeRcsMsgTypes(i)));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, changedObject.correlationId);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, changedObject.correlationTag);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(changedObject.resourceURL.toString()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, changedObject.lastModSeq);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, changedObject.parentFolder.toString());
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
        contentValues.put("sim_imsi", this.IMSI);
        return this.mBufferDB.insertTable(7, contentValues);
    }

    public long insertNmsEventDeletedObjToSummaryDB(DeletedObject deletedObject, int i) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("linenum", Util.getLineTelUriFromObjUrl(deletedObject.resourceURL.toString()));
        contentValues.put("messagetype", Integer.valueOf(convergeRcsMsgTypes(i)));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, deletedObject.correlationId);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, deletedObject.correlationTag);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(deletedObject.resourceURL.toString()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, Long.valueOf(deletedObject.lastModSeq));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId()));
        contentValues.put("sim_imsi", this.IMSI);
        return this.mBufferDB.insertTable(7, contentValues);
    }

    public void deleteAllUsingLineAndTableIndex(int i, String str) {
        this.mBufferDB.deleteTable(7, "linenum=? AND messagetype=?", new String[]{str, String.valueOf(i)});
    }

    public long insertResUrlinSummaryIfNonExist(String str, int i) {
        String str2 = this.TAG;
        Log.d(str2, "insertResUrlinSummaryIfNonExist(): " + IMSLog.checker(str) + " msgType: " + i);
        Cursor queryTablewithResUrl = this.mBufferDB.queryTablewithResUrl(7, str);
        if (queryTablewithResUrl != null) {
            try {
                if (!queryTablewithResUrl.moveToFirst()) {
                    insertResUrlinSummary(str, i);
                } else if (queryTablewithResUrl.getInt(queryTablewithResUrl.getColumnIndexOrThrow("messagetype")) != i) {
                    long updateSummaryDbUsingMessageType = updateSummaryDbUsingMessageType(queryTablewithResUrl.getLong(queryTablewithResUrl.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID)), i);
                    queryTablewithResUrl.close();
                    return updateSummaryDbUsingMessageType;
                }
            } catch (NullPointerException e) {
                String str3 = this.TAG;
                Log.e(str3, "Nullpointer exception: " + e.getMessage());
            } catch (Throwable th) {
                if (queryTablewithResUrl != null) {
                    try {
                        queryTablewithResUrl.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        } else {
            Log.e(this.TAG, "insertResUrlinSummaryIfNonExist search error");
        }
        if (queryTablewithResUrl == null) {
            return 0;
        }
        queryTablewithResUrl.close();
        return 0;
    }
}
