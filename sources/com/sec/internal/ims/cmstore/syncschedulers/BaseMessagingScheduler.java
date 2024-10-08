package com.sec.internal.ims.cmstore.syncschedulers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUriParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.querybuilders.QueryBuilderBase;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.interfaces.ims.cmstore.ITelephonyDBColumns;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.PayloadPartInfo;

public class BaseMessagingScheduler extends Handler {
    private String TAG = BaseMessagingScheduler.class.getSimpleName();
    protected boolean isCmsEnabled = false;
    protected final IBufferDBEventListener mCallbackMsgApp;
    protected final Context mContext;
    protected int mDbTableContractIndex;
    protected final IDeviceDataChangeListener mDeviceDataChangeListener;
    protected int mMaxNumMsgsNotifyAppInIntent = 20;
    protected final CloudMessageBufferDBEventSchedulingRule mScheduleRule;
    protected MessageStoreClient mStoreClient;
    protected SummaryQueryBuilder mSummaryDB;

    public String getAppTypeString(int i) {
        if (i == 1 || i == 14 || i == 3 || i == 4 || i == 11 || i == 12) {
            return CloudMessageProviderContract.ApplicationTypes.MSGDATA;
        }
        switch (i) {
            case 17:
            case 18:
            case 19:
            case 20:
                return "VVMDATA";
            default:
                return null;
        }
    }

    public String getMessageTypeString(int i, boolean z) {
        if (i != 1) {
            if (i == 3) {
                return CloudMessageProviderContract.DataTypes.SMS;
            }
            if (i == 4) {
                return CloudMessageProviderContract.DataTypes.MMS;
            }
            if (i != 11) {
                if (i == 12) {
                    return "FT";
                }
                switch (i) {
                    case 17:
                        return "VVMDATA";
                    case 18:
                        return CloudMessageProviderContract.DataTypes.VVMGREETING;
                    case 19:
                        return CloudMessageProviderContract.DataTypes.VVMPIN;
                    case 20:
                        return CloudMessageProviderContract.DataTypes.VVMPROFILE;
                    default:
                        return null;
                }
            }
        } else if (z) {
            return "FT";
        }
        return CloudMessageProviderContract.DataTypes.CHAT;
    }

    public void wipeOutData(int i, String str) {
    }

    public BaseMessagingScheduler(MessageStoreClient messageStoreClient, CloudMessageBufferDBEventSchedulingRule cloudMessageBufferDBEventSchedulingRule, IDeviceDataChangeListener iDeviceDataChangeListener, IBufferDBEventListener iBufferDBEventListener, Looper looper, SummaryQueryBuilder summaryQueryBuilder) {
        super(looper);
        this.mStoreClient = messageStoreClient;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        Context context = messageStoreClient.getContext();
        this.mContext = context;
        this.mScheduleRule = cloudMessageBufferDBEventSchedulingRule;
        this.mDeviceDataChangeListener = iDeviceDataChangeListener;
        this.mCallbackMsgApp = iBufferDBEventListener;
        this.mSummaryDB = summaryQueryBuilder;
        this.isCmsEnabled = CmsUtil.isMcsSupported(context, messageStoreClient.getClientID());
    }

    public void handleOutPutParamSyncFlagSet(ParamSyncFlagsSet paramSyncFlagsSet, long j, int i, boolean z, boolean z2, String str, BufferDBChangeParamList bufferDBChangeParamList, boolean z3) {
        ParamSyncFlagsSet paramSyncFlagsSet2 = paramSyncFlagsSet;
        int i2 = i;
        boolean z4 = z2;
        BufferDBChangeParamList bufferDBChangeParamList2 = bufferDBChangeParamList;
        String str2 = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("handleOutPutParamSyncFlagSet: ");
        sb.append(paramSyncFlagsSet);
        sb.append(" , mIsGoforwardSync: ");
        sb.append(z4);
        sb.append("changelist: ");
        sb.append(bufferDBChangeParamList2 == null ? "null" : "not null");
        Log.i(str2, sb.toString());
        if ((paramSyncFlagsSet2.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud) || paramSyncFlagsSet2.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud)) && !z4) {
            if (bufferDBChangeParamList2 == null) {
                BufferDBChangeParamList bufferDBChangeParamList3 = new BufferDBChangeParamList();
                bufferDBChangeParamList3.mChangelst.add(new BufferDBChangeParam(i, j, z2, str, paramSyncFlagsSet2.mAction, this.mStoreClient));
                this.mDeviceDataChangeListener.sendDeviceUpdate(bufferDBChangeParamList3);
                return;
            }
            bufferDBChangeParamList2.mChangelst.add(new BufferDBChangeParam(i, j, z2, str, paramSyncFlagsSet2.mAction, this.mStoreClient));
        } else if (paramSyncFlagsSet2.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice) || paramSyncFlagsSet2.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice)) {
            notifyMsgAppCldNotification(getAppTypeString(i2), getMessageTypeString(i2, z), j, z3);
        }
    }

    public void notifyMsgAppCldNotification(String str, String str2, long j, boolean z) {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", String.valueOf(j));
        jsonArray.add(jsonObject);
        this.mCallbackMsgApp.notifyCloudMessageUpdate(str, str2, jsonArray.toString(), z);
    }

    public void notifyInitialSyncStatus(String str, String str2, String str3, CloudMessageBufferDBConstants.InitialSyncStatusFlag initialSyncStatusFlag, boolean z) {
        this.mCallbackMsgApp.notifyAppInitialSyncStatus(str, str2, str3, initialSyncStatusFlag, z);
    }

    public void wipeOutData(int i, String str, QueryBuilderBase queryBuilderBase) {
        queryBuilderBase.deleteAllUsingLineAndTableIndex(i, str);
        this.mSummaryDB.deleteAllUsingLineAndTableIndex(i, str);
        String str2 = this.TAG;
        Log.i(str2, "deleteAllUsingLineAndType: " + i + " , line = " + IMSLog.checker(str));
    }

    public void deleteMessageFromCloud(int i, long j, String str, QueryBuilderBase queryBuilderBase) {
        String str2 = this.TAG;
        Log.i(str2, "deleteMessageFromCloud: bufferID: " + j);
        if (queryBuilderBase != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Delete.getId()));
            queryBuilderBase.updateTable(i, contentValues, "_bufferdbid=?", new String[]{String.valueOf(j)});
            BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
            bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(i, j, false, str, this.mStoreClient));
            this.mDeviceDataChangeListener.sendDeviceUpdate(bufferDBChangeParamList);
        }
    }

    public void msgAppFetchBuffer(Cursor cursor, String str, String str2) {
        JsonArray jsonArray = new JsonArray();
        do {
            int i = cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", String.valueOf(i));
            jsonArray.add(jsonObject);
            if (jsonArray.size() == this.mMaxNumMsgsNotifyAppInIntent) {
                this.mCallbackMsgApp.notifyCloudMessageUpdate(str, str2, jsonArray.toString(), false);
                jsonArray = new JsonArray();
            }
        } while (cursor.moveToNext());
        if (jsonArray.size() > 0) {
            this.mCallbackMsgApp.notifyCloudMessageUpdate(str, str2, jsonArray.toString(), false);
        }
    }

    public void onUpdateFromDeviceFtUriFetch(DeviceMsgAppFetchUriParam deviceMsgAppFetchUriParam, QueryBuilderBase queryBuilderBase) {
        ContentValues contentValues = new ContentValues();
        String str = this.TAG;
        Log.i(str, "onUpdateFromDeviceFtUriFetch param: " + deviceMsgAppFetchUriParam + " pdurid:" + deviceMsgAppFetchUriParam.mBufferRowId + " partid: " + deviceMsgAppFetchUriParam.mImsPartId);
        int i = deviceMsgAppFetchUriParam.mTableindex;
        if (i == 1) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.FILE_URI, "content://im/ft_original/" + deviceMsgAppFetchUriParam.mTelephonyRowId);
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.THUMBNAIL_URI, "content://im/ft_thumbnail/" + deviceMsgAppFetchUriParam.mTelephonyRowId);
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
        } else if (i == 6) {
            contentValues.put("_id", Long.valueOf(deviceMsgAppFetchUriParam.mTelephonyRowId));
            Cursor queryTablewithBufferDbId = queryBuilderBase.queryTablewithBufferDbId(6, deviceMsgAppFetchUriParam.mImsPartId);
            if (queryTablewithBufferDbId != null) {
                try {
                    if (queryTablewithBufferDbId.moveToFirst()) {
                        String string = queryTablewithBufferDbId.getString(queryTablewithBufferDbId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpart.CT));
                        String str2 = this.TAG;
                        Log.i(str2, "MMS contentType: " + string);
                        if (string != null && !string.equalsIgnoreCase(ITelephonyDBColumns.xml_smil_type) && !string.equalsIgnoreCase(MIMEContentType.PLAIN_TEXT) && !string.endsWith(MIMEContentType.JSON) && !string.contains(MIMEContentType.JSON)) {
                            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.FILE_URI, "content://mms/part/" + deviceMsgAppFetchUriParam.mTelephonyRowId);
                        }
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (queryTablewithBufferDbId != null) {
                queryTablewithBufferDbId.close();
            }
        } else if (i == 17 || i == 18) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.FILE_URI, "content://com.samsung.vvm/files/" + deviceMsgAppFetchUriParam.mTelephonyRowId);
        } else {
            Log.e(this.TAG, "Invalid messageType");
            return;
        }
        long j = deviceMsgAppFetchUriParam.mImsPartId;
        if (j == -1) {
            j = deviceMsgAppFetchUriParam.mBufferRowId;
        }
        queryBuilderBase.updateTable(deviceMsgAppFetchUriParam.mTableindex, contentValues, "_bufferdbid=?", new String[]{Long.toString(j)});
        return;
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0119  */
    /* JADX WARNING: Removed duplicated region for block: B:40:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onUpdateFromDeviceMsgAppFetch(com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam r17, boolean r18, com.sec.internal.ims.cmstore.querybuilders.QueryBuilderBase r19) {
        /*
            r16 = this;
            r0 = r16
            r1 = r17
            r2 = r19
            java.lang.String r3 = r0.TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "onUpdateFromDeviceMsgAppFetch: "
            r4.append(r5)
            r4.append(r1)
            java.lang.String r5 = " tableid: "
            r4.append(r5)
            int r5 = r1.mTableindex
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r3, r4)
            int r3 = r1.mTableindex
            long r4 = r1.mBufferRowId
            android.database.Cursor r3 = r2.queryTablewithBufferDbId(r3, r4)
            java.lang.String r4 = "syncaction"
            java.lang.String r5 = "syncdirection"
            if (r3 == 0) goto L_0x0063
            boolean r6 = r3.moveToFirst()     // Catch:{ all -> 0x0057 }
            if (r6 == 0) goto L_0x0063
            int r6 = r3.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x0057 }
            int r6 = r3.getInt(r6)     // Catch:{ all -> 0x0057 }
            int r7 = r3.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x0057 }
            int r7 = r3.getInt(r7)     // Catch:{ all -> 0x0057 }
            java.lang.String r8 = "linenum"
            int r8 = r3.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x0057 }
            java.lang.String r8 = r3.getString(r8)     // Catch:{ all -> 0x0057 }
            goto L_0x0067
        L_0x0057:
            r0 = move-exception
            r1 = r0
            r3.close()     // Catch:{ all -> 0x005d }
            goto L_0x0062
        L_0x005d:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)
        L_0x0062:
            throw r1
        L_0x0063:
            r6 = 0
            r7 = 0
            r8 = r7
            r7 = r6
        L_0x0067:
            if (r3 == 0) goto L_0x006c
            r3.close()
        L_0x006c:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r9 = r0.mScheduleRule
            int r10 = r0.mDbTableContractIndex
            long r11 = r1.mBufferRowId
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r13 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r6)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r14 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r7)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r15 = r1.mUpdateType
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r3 = r9.getSetFlagsForMsgResponse(r10, r11, r13, r14, r15)
            android.content.ContentValues r6 = new android.content.ContentValues
            r6.<init>()
            int r7 = r1.mTableindex
            r9 = 3
            java.lang.String r10 = "_id"
            if (r7 != r9) goto L_0x0096
            long r11 = r1.mTelephonyRowId
            java.lang.Long r7 = java.lang.Long.valueOf(r11)
            r6.put(r10, r7)
            goto L_0x00d4
        L_0x0096:
            r9 = 4
            if (r7 != r9) goto L_0x00a3
            long r11 = r1.mTelephonyRowId
            java.lang.Long r7 = java.lang.Long.valueOf(r11)
            r6.put(r10, r7)
            goto L_0x00d4
        L_0x00a3:
            r9 = 17
            if (r7 != r9) goto L_0x00b1
            long r11 = r1.mTelephonyRowId
            java.lang.Long r7 = java.lang.Long.valueOf(r11)
            r6.put(r10, r7)
            goto L_0x00d4
        L_0x00b1:
            r9 = 18
            if (r7 != r9) goto L_0x00bf
            long r11 = r1.mTelephonyRowId
            java.lang.Long r7 = java.lang.Long.valueOf(r11)
            r6.put(r10, r7)
            goto L_0x00d4
        L_0x00bf:
            r9 = 1
            if (r7 != r9) goto L_0x00c7
            boolean r9 = r3.mIsChanged
            if (r9 != 0) goto L_0x00c7
            return
        L_0x00c7:
            r9 = 10
            if (r7 != r9) goto L_0x00d4
            long r11 = r1.mTelephonyRowId
            java.lang.Long r7 = java.lang.Long.valueOf(r11)
            r6.put(r10, r7)
        L_0x00d4:
            java.lang.String r7 = r0.TAG
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "onUpdateFromDeviceMsgAppFetch "
            r9.append(r10)
            r9.append(r3)
            java.lang.String r9 = r9.toString()
            android.util.Log.i(r7, r9)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r7 = r3.mDirection
            int r7 = r7.getId()
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)
            r6.put(r5, r7)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = r3.mAction
            int r5 = r5.getId()
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)
            r6.put(r4, r5)
            long r4 = r1.mBufferRowId
            java.lang.String r4 = java.lang.Long.toString(r4)
            java.lang.String[] r4 = new java.lang.String[]{r4}
            int r5 = r1.mTableindex
            java.lang.String r7 = "_bufferdbid=?"
            r2.updateTable(r5, r6, r7, r4)
            boolean r2 = r3.mIsChanged
            if (r2 == 0) goto L_0x012c
            long r4 = r1.mBufferRowId
            int r6 = r1.mTableindex
            boolean r7 = r1.mIsFT
            r9 = 0
            r10 = 0
            r1 = r16
            r2 = r3
            r3 = r4
            r5 = r6
            r6 = r7
            r7 = r18
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9, r10)
        L_0x012c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.BaseMessagingScheduler.onUpdateFromDeviceMsgAppFetch(com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam, boolean, com.sec.internal.ims.cmstore.querybuilders.QueryBuilderBase):void");
    }

    public void onCloudUpdateFlagSuccess(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z, QueryBuilderBase queryBuilderBase) {
        Throwable th;
        ParamSyncFlagsSet paramSyncFlagsSet;
        QueryBuilderBase queryBuilderBase2 = queryBuilderBase;
        String str = this.TAG;
        Log.i(str, "onCloudUpdateFlagSuccess: " + paramOMAresponseforBufDB);
        Cursor queryTablewithBufferDbId = queryBuilderBase2.queryTablewithBufferDbId(paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
        if (queryTablewithBufferDbId != null) {
            try {
                if (queryTablewithBufferDbId.moveToFirst()) {
                    CloudMessageBufferDBConstants.ActionStatusFlag valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(queryTablewithBufferDbId.getInt(queryTablewithBufferDbId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
                    CloudMessageBufferDBConstants.DirectionFlag valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(queryTablewithBufferDbId.getInt(queryTablewithBufferDbId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
                    String string = queryTablewithBufferDbId.getString(queryTablewithBufferDbId.getColumnIndexOrThrow("linenum"));
                    if (CloudMessageBufferDBConstants.ActionStatusFlag.Delete.equals(valueOf)) {
                        paramSyncFlagsSet = this.mScheduleRule.getSetFlagsForCldResponse(this.mDbTableContractIndex, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId, valueOf2, valueOf, CloudMessageBufferDBConstants.CloudResponseFlag.SetDelete);
                    } else if (CloudMessageBufferDBConstants.ActionStatusFlag.Update.equals(valueOf)) {
                        paramSyncFlagsSet = this.mScheduleRule.getSetFlagsForCldResponse(this.mDbTableContractIndex, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId, valueOf2, valueOf, CloudMessageBufferDBConstants.CloudResponseFlag.SetRead);
                    } else {
                        paramSyncFlagsSet = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.Done, CloudMessageBufferDBConstants.ActionStatusFlag.None);
                        Log.d(this.TAG, "onCloudUpdateFlagSuccess: something wrong not processed cloud callback");
                    }
                    if (paramSyncFlagsSet.mIsChanged) {
                        String[] strArr = {String.valueOf(paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId)};
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(paramSyncFlagsSet.mDirection.getId()));
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(paramSyncFlagsSet.mAction.getId()));
                        queryBuilderBase2.updateTable(paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex, contentValues, "_bufferdbid=?", strArr);
                    }
                    if (!paramSyncFlagsSet.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                        handleOutPutParamSyncFlagSet(paramSyncFlagsSet, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId, paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex, false, z, string, (BufferDBChangeParamList) null, false);
                    }
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (queryTablewithBufferDbId != null) {
            queryTablewithBufferDbId.close();
            return;
        }
        return;
        throw th;
    }

    public void updateQueryTable(ContentValues contentValues, long j, QueryBuilderBase queryBuilderBase) {
        queryBuilderBase.updateTable(this.mDbTableContractIndex, contentValues, "_bufferdbid=?", new String[]{String.valueOf(j)});
    }

    public void handleCloudUploadSuccess(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z, QueryBuilderBase queryBuilderBase, int i) {
        Throwable th;
        QueryBuilderBase queryBuilderBase2 = queryBuilderBase;
        int i2 = i;
        int i3 = paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex;
        if (paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex == 12) {
            i3 = 1;
        }
        Cursor queryTablewithBufferDbId = queryBuilderBase2.queryTablewithBufferDbId(i3, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
        if (queryTablewithBufferDbId != null) {
            try {
                if (queryTablewithBufferDbId.moveToFirst()) {
                    CloudMessageBufferDBConstants.ActionStatusFlag valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(queryTablewithBufferDbId.getInt(queryTablewithBufferDbId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
                    CloudMessageBufferDBConstants.DirectionFlag valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(queryTablewithBufferDbId.getInt(queryTablewithBufferDbId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
                    String string = queryTablewithBufferDbId.getString(queryTablewithBufferDbId.getColumnIndexOrThrow("linenum"));
                    ParamSyncFlagsSet setFlagsForCldResponse = this.mScheduleRule.getSetFlagsForCldResponse(this.mDbTableContractIndex, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId, valueOf2, valueOf, CloudMessageBufferDBConstants.CloudResponseFlag.Inserted);
                    if (setFlagsForCldResponse.mIsChanged) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(setFlagsForCldResponse.mDirection.getId()));
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(setFlagsForCldResponse.mAction.getId()));
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAresponseforBufDB.getReference().resourceURL.toString()));
                        contentValues.put("path", paramOMAresponseforBufDB.getReference().path);
                        String[] strArr = {String.valueOf(paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId)};
                        if (paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex == 12) {
                            onPayloadUploadSuccess(paramOMAresponseforBufDB, contentValues, queryBuilderBase2);
                        } else {
                            ParamOMAresponseforBufDB paramOMAresponseforBufDB2 = paramOMAresponseforBufDB;
                        }
                        queryBuilderBase2.updateTable(i3, contentValues, "_bufferdbid=?", strArr);
                        if (i2 == 3) {
                            this.mSummaryDB.insertResUrlinSummaryIfNonExist(Util.decodeUrlFromServer(paramOMAresponseforBufDB.getReference().resourceURL.toString()), 3);
                        } else if (i2 == 4) {
                            this.mSummaryDB.insertResUrlinSummaryIfNonExist(Util.decodeUrlFromServer(paramOMAresponseforBufDB.getReference().resourceURL.toString()), 4);
                        } else if (i2 == 1) {
                            this.mSummaryDB.insertResUrlinSummaryIfNonExist(Util.decodeUrlFromServer(paramOMAresponseforBufDB.getReference().resourceURL.toString()), 1);
                        }
                    } else {
                        ParamOMAresponseforBufDB paramOMAresponseforBufDB3 = paramOMAresponseforBufDB;
                    }
                    if (!setFlagsForCldResponse.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.Done)) {
                        handleOutPutParamSyncFlagSet(setFlagsForCldResponse, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId, i3, false, z, string, (BufferDBChangeParamList) null, false);
                    }
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (queryTablewithBufferDbId != null) {
            queryTablewithBufferDbId.close();
            return;
        }
        return;
        throw th;
    }

    public void onPayloadUploadSuccess(ParamOMAresponseforBufDB paramOMAresponseforBufDB, ContentValues contentValues, QueryBuilderBase queryBuilderBase) {
        IMSLog.i(this.TAG, "onPayloadUploadSuccess");
        Cursor queryTablewithBufferDbId = queryBuilderBase.queryTablewithBufferDbId(1, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
        if (queryTablewithBufferDbId != null) {
            try {
                if (queryTablewithBufferDbId.moveToFirst()) {
                    for (PayloadPartInfo payloadPartInfo : paramOMAresponseforBufDB.getObject().payloadPart) {
                        if (payloadPartInfo.contentDisposition.contains("icon")) {
                            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB, payloadPartInfo.href.toString());
                        } else {
                            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL, payloadPartInfo.href.toString());
                        }
                        IMSLog.i(this.TAG, "url: " + payloadPartInfo.href + " size: " + payloadPartInfo.size + " dis:" + payloadPartInfo.contentDisposition);
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryTablewithBufferDbId != null) {
            queryTablewithBufferDbId.close();
            return;
        }
        return;
        throw th;
    }
}
