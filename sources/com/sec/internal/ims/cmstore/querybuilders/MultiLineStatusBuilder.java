package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.log.IMSLog;

public class MultiLineStatusBuilder extends QueryBuilderBase {
    private String TAG = MultiLineStatusBuilder.class.getSimpleName();
    private int mPhoneId;

    public MultiLineStatusBuilder(MessageStoreClient messageStoreClient, IBufferDBEventListener iBufferDBEventListener) {
        super(messageStoreClient, iBufferDBEventListener);
        this.mPhoneId = messageStoreClient.getClientID();
        this.TAG += "[" + this.mPhoneId + "]";
    }

    public int updateLineUploadStatus(String str, SyncMsgType syncMsgType, int i) {
        String str2 = this.TAG;
        Log.i(str2, "updateLineUploadStatus(): " + IMSLog.checker(str) + "  type: " + syncMsgType + " status:  " + OMASyncEventType.valueOf(i));
        String[] strArr = {str, String.valueOf(syncMsgType.getId())};
        ContentValues contentValues = new ContentValues();
        if (this.isCmsEnabled) {
            IMSLog.c(LogClass.MCS_INIT_SYNC_STATUS, this.mPhoneId + "," + BaseSyncHandler.SyncOperation.UPLOAD.ordinal() + "," + i);
        }
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.INITUPLOADSTATUS, Integer.valueOf(i));
        return this.mBufferDB.updateTable(23, contentValues, "linenum=? AND messagetype=?", strArr);
    }

    public int updateLineInitsyncStatus(String str, SyncMsgType syncMsgType, String str2, int i) {
        OMASyncEventType valueOf = OMASyncEventType.valueOf(i);
        String str3 = this.TAG;
        Log.i(str3, "updateLineInitsyncStatus(): " + IMSLog.checker(str) + "  type: " + syncMsgType + " cursor: " + str2 + " " + valueOf);
        StringBuilder sb = new StringBuilder();
        sb.append(this.mPhoneId);
        sb.append(",");
        sb.append(BaseSyncHandler.SyncOperation.DOWNLOAD.ordinal());
        sb.append(",");
        sb.append(valueOf);
        IMSLog.c(LogClass.MCS_INIT_SYNC_STATUS, sb.toString());
        String[] strArr = {str, String.valueOf(syncMsgType.getId())};
        ContentValues contentValues = new ContentValues();
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldClearCursorUponInitSyncDone()) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCCURSOR, str2);
        } else if (TextUtils.isEmpty(str2)) {
            IMSLog.d(this.TAG, "for certain carriers, we should save last cursor with value and not overwrite with empty cursor");
        } else {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCCURSOR, str2);
        }
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCSTATUS, Integer.valueOf(i));
        contentValues.put("sim_imsi", this.IMSI);
        return this.mBufferDB.updateTable(23, contentValues, "linenum=? AND messagetype=?", strArr);
    }

    public Cursor queryUsingLineAndSyncMsgType(String str, SyncMsgType syncMsgType) {
        String str2 = this.TAG;
        Log.i(str2, "queryUsingLineAndSyncMsgType(): " + IMSLog.checker(str) + " type: " + syncMsgType);
        return this.mBufferDB.queryTable(23, (String[]) null, "linenum=? AND messagetype=?", new String[]{str, String.valueOf(syncMsgType.getId())}, (String) null);
    }

    public void insertNewLine(String str, SyncMsgType syncMsgType) {
        String str2 = this.TAG;
        Log.i(str2, "insertNewLine(): " + IMSLog.checker(str) + " type: " + syncMsgType + ", IMSI: " + IMSLog.checker(this.IMSI));
        ContentValues contentValues = new ContentValues();
        contentValues.put("linenum", str);
        contentValues.put("messagetype", Integer.valueOf(syncMsgType.getId()));
        contentValues.put("sim_imsi", this.IMSI);
        this.mBufferDB.insertTable(23, contentValues);
    }

    public void deleteLine(String str, SyncMsgType syncMsgType) {
        String str2 = this.TAG;
        Log.i(str2, "deleteLine(): " + IMSLog.checker(str) + " type: " + syncMsgType);
        String[] strArr = {str, String.valueOf(syncMsgType.getId())};
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCCURSOR, "");
        OMASyncEventType oMASyncEventType = OMASyncEventType.DEFAULT;
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCSTATUS, Integer.valueOf(oMASyncEventType.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.INITUPLOADSTATUS, Integer.valueOf(oMASyncEventType.getId()));
        this.mBufferDB.updateTable(23, contentValues, "linenum=? AND messagetype=?", strArr);
    }
}
