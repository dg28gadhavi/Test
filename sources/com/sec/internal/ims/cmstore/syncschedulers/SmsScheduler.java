package com.sec.internal.ims.cmstore.syncschedulers;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Looper;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.DeviceLegacyUpdateParam;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.DeletedObject;
import java.util.ArrayList;
import java.util.Iterator;

public class SmsScheduler extends BaseMessagingScheduler {
    private String TAG = SmsScheduler.class.getSimpleName();
    protected final SmsQueryBuilder mBufferDbQuery;
    private final MultiLineScheduler mMultiLineScheduler;

    public SmsScheduler(MessageStoreClient messageStoreClient, CloudMessageBufferDBEventSchedulingRule cloudMessageBufferDBEventSchedulingRule, SummaryQueryBuilder summaryQueryBuilder, MultiLineScheduler multiLineScheduler, IDeviceDataChangeListener iDeviceDataChangeListener, IBufferDBEventListener iBufferDBEventListener, Looper looper) {
        super(messageStoreClient, cloudMessageBufferDBEventSchedulingRule, iDeviceDataChangeListener, iBufferDBEventListener, looper, summaryQueryBuilder);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mBufferDbQuery = new SmsQueryBuilder(messageStoreClient, iBufferDBEventListener);
        this.mMultiLineScheduler = multiLineScheduler;
        this.mDbTableContractIndex = 3;
    }

    public long handleObjectSMSCloudSearch(ParamOMAObject paramOMAObject) {
        String str = this.TAG;
        Log.i(str, "handleObjectSMSCloudSearch: " + paramOMAObject.correlationTag);
        Cursor searchUnSyncedSMSBufferUsingCorrelationTag = this.mBufferDbQuery.searchUnSyncedSMSBufferUsingCorrelationTag(paramOMAObject.correlationTag);
        try {
            handleObjectSMSCloudSearchFromCursor(searchUnSyncedSMSBufferUsingCorrelationTag, paramOMAObject, -1);
            if (searchUnSyncedSMSBufferUsingCorrelationTag != null) {
                searchUnSyncedSMSBufferUsingCorrelationTag.close();
            }
            return -1;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    private void handleObjectSMSCloudSearchFromCursor(Cursor cursor, ParamOMAObject paramOMAObject, long j) {
        ParamSyncFlagsSet paramSyncFlagsSet;
        Cursor cursor2 = cursor;
        ParamOMAObject paramOMAObject2 = paramOMAObject;
        if (cursor2 == null || !cursor.moveToFirst()) {
            ArrayList<Long> insertSMSUsingObject = this.mBufferDbQuery.insertSMSUsingObject(paramOMAObject2, false, 0);
            this.mSummaryDB.insertSummaryDbUsingObjectIfNonExist(paramOMAObject2, 3);
            Iterator<Long> it = insertSMSUsingObject.iterator();
            while (it.hasNext()) {
                long longValue = it.next().longValue();
                if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldSkipMessage(paramOMAObject2)) {
                    deleteMessageFromCloud(3, longValue, paramOMAObject2.mLine, this.mBufferDbQuery);
                } else {
                    handleOutPutParamSyncFlagSet(new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice, CloudMessageBufferDBConstants.ActionStatusFlag.Insert), longValue, 3, false, paramOMAObject2.mIsGoforwardSync, Util.getLineTelUriFromObjUrl(paramOMAObject2.resourceURL.toString()), (BufferDBChangeParamList) null, false);
                }
            }
            return;
        }
        long j2 = (long) cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long j3 = cursor2.getLong(cursor2.getColumnIndexOrThrow("date"));
        CloudMessageBufferDBConstants.ActionStatusFlag valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        String string = cursor2.getString(cursor2.getColumnIndexOrThrow("body"));
        String str = this.TAG;
        Log.d(str, "handleObjectSMSCloudSearch find bufferDB: " + paramOMAObject2.correlationTag + " id: " + j2 + " time: " + j3 + " body:" + string);
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, paramOMAObject2.lastModSeq);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject2.resourceURL.toString()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(paramOMAObject2.parentFolder.toString()));
        contentValues.put("path", Util.decodeUrlFromServer(paramOMAObject2.path));
        ParamSyncFlagsSet paramSyncFlagsSet2 = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.Done, CloudMessageBufferDBConstants.ActionStatusFlag.None);
        paramSyncFlagsSet2.mIsChanged = false;
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag = paramOMAObject2.mFlag;
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
        if (actionStatusFlag.equals(actionStatusFlag2)) {
            paramSyncFlagsSet2.setIsChangedActionAndDirection(true, actionStatusFlag2, CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice);
        } else {
            CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag3 = paramOMAObject2.mFlag;
            CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag4 = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
            if (actionStatusFlag3.equals(actionStatusFlag4)) {
                if (cursor2.getInt(cursor2.getColumnIndexOrThrow("read")) == 0) {
                    contentValues.put("read", 1);
                }
                paramSyncFlagsSet = this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, j2, valueOf2, valueOf, actionStatusFlag4);
            } else {
                paramSyncFlagsSet = this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, j2, valueOf2, valueOf, CloudMessageBufferDBConstants.ActionStatusFlag.Insert);
            }
            paramSyncFlagsSet2 = paramSyncFlagsSet;
        }
        this.mSummaryDB.insertSummaryDbUsingObjectIfNonExist(paramOMAObject2, 3);
        if (paramSyncFlagsSet2.mIsChanged) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(paramSyncFlagsSet2.mAction.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(paramSyncFlagsSet2.mDirection.getId()));
            updateQueryTable(contentValues, j2, this.mBufferDbQuery);
            handleOutPutParamSyncFlagSet(paramSyncFlagsSet2, j2, 3, false, paramOMAObject2.mIsGoforwardSync, paramOMAObject2.mLine, (BufferDBChangeParamList) null, false);
            return;
        }
        updateQueryTable(contentValues, j2, this.mBufferDbQuery);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004f, code lost:
        if (com.sec.internal.constants.ims.cmstore.McsConstants.Protocol.SENDER_SD.equals(r13) == false) goto L_0x0057;
     */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x022d A[SYNTHETIC, Splitter:B:75:0x022d] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x023a A[SYNTHETIC, Splitter:B:83:0x023a] */
    /* JADX WARNING: Removed duplicated region for block: B:99:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleNormalSyncObjectSmsDownload(com.sec.internal.ims.cmstore.params.ParamOMAObject r27) {
        /*
            r26 = this;
            r0 = r26
            r11 = r27
            java.lang.String r1 = "read"
            java.lang.String r2 = "syncdirection"
            java.lang.String r3 = "syncaction"
            java.lang.String r4 = r0.TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "handleNormalSyncObjectSmsDownload: "
            r5.append(r6)
            java.lang.String r6 = r11.correlationTag
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
            java.lang.String r4 = r11.protocol
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            java.lang.String r12 = "standard"
            if (r4 == 0) goto L_0x0032
            r13 = r12
            goto L_0x0035
        L_0x0032:
            java.lang.String r4 = r11.protocol
            r13 = r4
        L_0x0035:
            com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder r4 = r0.mBufferDbQuery     // Catch:{ NullPointerException -> 0x0248 }
            java.lang.String r5 = r11.correlationTag     // Catch:{ NullPointerException -> 0x0248 }
            android.database.Cursor r10 = r4.searchUnSyncedSMSBufferUsingCorrelationTag(r5)     // Catch:{ NullPointerException -> 0x0248 }
            if (r10 == 0) goto L_0x0193
            boolean r6 = r10.moveToFirst()     // Catch:{ all -> 0x018f }
            if (r6 == 0) goto L_0x0193
            boolean r6 = r0.isCmsEnabled     // Catch:{ all -> 0x018f }
            if (r6 == 0) goto L_0x0057
            java.lang.String r6 = "oasis"
            boolean r6 = r6.equals(r13)     // Catch:{ all -> 0x0052 }
            if (r6 != 0) goto L_0x0193
            goto L_0x0057
        L_0x0052:
            r0 = move-exception
            r1 = r0
            r14 = r10
            goto L_0x0236
        L_0x0057:
            java.lang.String r6 = "_bufferdbid"
            int r6 = r10.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x018f }
            int r6 = r10.getInt(r6)     // Catch:{ all -> 0x018f }
            long r6 = (long) r6     // Catch:{ all -> 0x018f }
            java.lang.String r8 = "date"
            int r8 = r10.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x018f }
            long r12 = r10.getLong(r8)     // Catch:{ all -> 0x018f }
            java.lang.String r8 = "_id"
            int r8 = r10.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x018f }
            int r8 = r10.getInt(r8)     // Catch:{ all -> 0x018f }
            long r14 = (long) r8     // Catch:{ all -> 0x018f }
            java.lang.String r8 = "linenum"
            int r8 = r10.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x018f }
            java.lang.String r8 = r10.getString(r8)     // Catch:{ all -> 0x018f }
            int r4 = r10.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x018f }
            int r4 = r10.getInt(r4)     // Catch:{ all -> 0x018f }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r4)     // Catch:{ all -> 0x018f }
            int r5 = r10.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x018f }
            int r5 = r10.getInt(r5)     // Catch:{ all -> 0x018f }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r5)     // Catch:{ all -> 0x018f }
            java.lang.String r9 = "body"
            int r9 = r10.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x018f }
            java.lang.String r9 = r10.getString(r9)     // Catch:{ all -> 0x018f }
            r17 = r4
            java.lang.String r4 = r0.TAG     // Catch:{ all -> 0x018f }
            r18 = r5
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x018f }
            r5.<init>()     // Catch:{ all -> 0x018f }
            r25 = r8
            java.lang.String r8 = "handleObjectSMSCloudSearch find bufferDB: "
            r5.append(r8)     // Catch:{ all -> 0x018f }
            java.lang.String r8 = r11.correlationTag     // Catch:{ all -> 0x018f }
            r5.append(r8)     // Catch:{ all -> 0x018f }
            java.lang.String r8 = " id: "
            r5.append(r8)     // Catch:{ all -> 0x018f }
            r5.append(r6)     // Catch:{ all -> 0x018f }
            java.lang.String r8 = " time: "
            r5.append(r8)     // Catch:{ all -> 0x018f }
            r5.append(r12)     // Catch:{ all -> 0x018f }
            java.lang.String r8 = " body:"
            r5.append(r8)     // Catch:{ all -> 0x018f }
            r5.append(r9)     // Catch:{ all -> 0x018f }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x018f }
            android.util.Log.d(r4, r5)     // Catch:{ all -> 0x018f }
            android.content.ContentValues r4 = new android.content.ContentValues     // Catch:{ all -> 0x018f }
            r4.<init>()     // Catch:{ all -> 0x018f }
            java.lang.String r5 = "lastmodseq"
            java.lang.Long r8 = r11.lastModSeq     // Catch:{ all -> 0x018f }
            r4.put(r5, r8)     // Catch:{ all -> 0x018f }
            java.lang.String r5 = "res_url"
            java.net.URL r8 = r11.resourceURL     // Catch:{ all -> 0x018f }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x018f }
            java.lang.String r8 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r8)     // Catch:{ all -> 0x018f }
            r4.put(r5, r8)     // Catch:{ all -> 0x018f }
            java.lang.String r5 = "parentfolder"
            java.net.URL r8 = r11.parentFolder     // Catch:{ all -> 0x018f }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x018f }
            java.lang.String r8 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r8)     // Catch:{ all -> 0x018f }
            r4.put(r5, r8)     // Catch:{ all -> 0x018f }
            java.lang.String r5 = "path"
            java.lang.String r8 = r11.path     // Catch:{ all -> 0x018f }
            java.lang.String r8 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r8)     // Catch:{ all -> 0x018f }
            r4.put(r5, r8)     // Catch:{ all -> 0x018f }
            int r5 = r10.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x018f }
            int r5 = r10.getInt(r5)     // Catch:{ all -> 0x018f }
            r8 = 1
            if (r5 != r8) goto L_0x0123
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x0052 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r8 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud     // Catch:{ all -> 0x0052 }
            r21 = r5
            r20 = r8
            goto L_0x0127
        L_0x0123:
            r21 = r17
            r20 = r18
        L_0x0127:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x018f }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r8 = r11.mFlag     // Catch:{ all -> 0x018f }
            boolean r5 = r5.equals(r8)     // Catch:{ all -> 0x018f }
            if (r5 == 0) goto L_0x0139
            r9 = 1
            java.lang.Integer r5 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x0052 }
            r4.put(r1, r5)     // Catch:{ all -> 0x0052 }
        L_0x0139:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r1 = r0.mScheduleRule     // Catch:{ all -> 0x018f }
            int r5 = r0.mDbTableContractIndex     // Catch:{ all -> 0x018f }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r8 = r11.mFlag     // Catch:{ all -> 0x018f }
            r16 = r1
            r17 = r5
            r18 = r6
            r22 = r8
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r5 = r16.getSetFlagsForCldOperation(r17, r18, r20, r21, r22)     // Catch:{ all -> 0x018f }
            boolean r1 = r5.mIsChanged     // Catch:{ all -> 0x018f }
            if (r1 == 0) goto L_0x0169
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = r5.mDirection     // Catch:{ all -> 0x0052 }
            int r1 = r1.getId()     // Catch:{ all -> 0x0052 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0052 }
            r4.put(r2, r1)     // Catch:{ all -> 0x0052 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = r5.mAction     // Catch:{ all -> 0x0052 }
            int r1 = r1.getId()     // Catch:{ all -> 0x0052 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0052 }
            r4.put(r3, r1)     // Catch:{ all -> 0x0052 }
        L_0x0169:
            com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder r1 = r0.mBufferDbQuery     // Catch:{ all -> 0x018f }
            r0.updateQueryTable(r4, r6, r1)     // Catch:{ all -> 0x018f }
            r1 = 0
            int r1 = (r14 > r1 ? 1 : (r14 == r1 ? 0 : -1))
            if (r1 <= 0) goto L_0x018a
            r8 = 3
            r9 = 0
            boolean r11 = r11.mIsGoforwardSync     // Catch:{ all -> 0x018f }
            r12 = 0
            r13 = 0
            r1 = r26
            r2 = r5
            r3 = r6
            r5 = r8
            r6 = r9
            r7 = r11
            r8 = r25
            r9 = r12
            r14 = r10
            r10 = r13
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9, r10)     // Catch:{ all -> 0x0234 }
            goto L_0x018b
        L_0x018a:
            r14 = r10
        L_0x018b:
            r23 = -1
            goto L_0x022b
        L_0x018f:
            r0 = move-exception
            r14 = r10
            goto L_0x0235
        L_0x0193:
            r14 = r10
            r9 = 1
            com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder r1 = r0.mBufferDbQuery     // Catch:{ all -> 0x0234 }
            r15 = 0
            r2 = 0
            java.util.ArrayList r1 = r1.insertSMSUsingObject(r11, r15, r2)     // Catch:{ all -> 0x0234 }
            java.util.Iterator r16 = r1.iterator()     // Catch:{ all -> 0x0234 }
            r23 = -1
        L_0x01a4:
            boolean r1 = r16.hasNext()     // Catch:{ all -> 0x0231 }
            if (r1 == 0) goto L_0x022b
            java.lang.Object r1 = r16.next()     // Catch:{ all -> 0x0231 }
            java.lang.Long r1 = (java.lang.Long) r1     // Catch:{ all -> 0x0231 }
            long r17 = r1.longValue()     // Catch:{ all -> 0x0231 }
            java.lang.String r1 = "OUT"
            java.lang.String r2 = r11.DIRECTION     // Catch:{ all -> 0x0231 }
            boolean r1 = r1.equalsIgnoreCase(r2)     // Catch:{ all -> 0x0231 }
            if (r1 == 0) goto L_0x01ca
            boolean r1 = r0.isCmsEnabled     // Catch:{ all -> 0x0231 }
            if (r1 == 0) goto L_0x01c8
            boolean r1 = r12.equals(r13)     // Catch:{ all -> 0x0231 }
            if (r1 != 0) goto L_0x01ca
        L_0x01c8:
            r8 = r9
            goto L_0x01cb
        L_0x01ca:
            r8 = r15
        L_0x01cb:
            java.lang.String r1 = r0.TAG     // Catch:{ all -> 0x0231 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0231 }
            r2.<init>()     // Catch:{ all -> 0x0231 }
            java.lang.String r3 = "handleNormalSyncObjectSmsDownload isInsertOutgoing:"
            r2.append(r3)     // Catch:{ all -> 0x0231 }
            r2.append(r8)     // Catch:{ all -> 0x0231 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0231 }
            com.sec.internal.log.IMSLog.i(r1, r2)     // Catch:{ all -> 0x0231 }
            if (r8 != 0) goto L_0x01f8
            java.lang.String r1 = "IN"
            java.lang.String r2 = r11.DIRECTION     // Catch:{ all -> 0x0231 }
            boolean r1 = r1.equalsIgnoreCase(r2)     // Catch:{ all -> 0x0231 }
            if (r1 == 0) goto L_0x0225
            java.lang.String r1 = r11.DATE     // Catch:{ all -> 0x0231 }
            com.sec.internal.ims.cmstore.MessageStoreClient r2 = r0.mStoreClient     // Catch:{ all -> 0x0231 }
            r3 = 3
            boolean r1 = com.sec.internal.ims.cmstore.utils.Util.isDownloadObject(r1, r2, r3)     // Catch:{ all -> 0x0231 }
            if (r1 == 0) goto L_0x0225
        L_0x01f8:
            boolean r1 = r11.mIsGoforwardSync     // Catch:{ all -> 0x0231 }
            if (r1 != 0) goto L_0x0225
            java.net.URL r1 = r11.resourceURL     // Catch:{ all -> 0x0231 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0231 }
            java.lang.String r8 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r1)     // Catch:{ all -> 0x0231 }
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r2 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x0231 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x0231 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x0231 }
            r2.<init>(r1, r3)     // Catch:{ all -> 0x0231 }
            r5 = 3
            r6 = 0
            boolean r7 = r11.mIsGoforwardSync     // Catch:{ all -> 0x0231 }
            r10 = 0
            r19 = 0
            r1 = r26
            r3 = r17
            r20 = r9
            r9 = r10
            r10 = r19
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9, r10)     // Catch:{ all -> 0x0231 }
            r23 = r17
            goto L_0x0227
        L_0x0225:
            r20 = r9
        L_0x0227:
            r9 = r20
            goto L_0x01a4
        L_0x022b:
            if (r14 == 0) goto L_0x0250
            r14.close()     // Catch:{ NullPointerException -> 0x0244 }
            goto L_0x0250
        L_0x0231:
            r0 = move-exception
            r1 = r0
            goto L_0x0238
        L_0x0234:
            r0 = move-exception
        L_0x0235:
            r1 = r0
        L_0x0236:
            r23 = -1
        L_0x0238:
            if (r14 == 0) goto L_0x0243
            r14.close()     // Catch:{ all -> 0x023e }
            goto L_0x0243
        L_0x023e:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ NullPointerException -> 0x0244 }
        L_0x0243:
            throw r1     // Catch:{ NullPointerException -> 0x0244 }
        L_0x0244:
            r0 = move-exception
            r14 = r23
            goto L_0x024b
        L_0x0248:
            r0 = move-exception
            r14 = -1
        L_0x024b:
            r0.printStackTrace()
            r23 = r14
        L_0x0250:
            return r23
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler.handleNormalSyncObjectSmsDownload(com.sec.internal.ims.cmstore.params.ParamOMAObject):long");
    }

    public void onNmsEventChangedObjSmsBufferDbAvailableUsingCorrTag(Cursor cursor, ChangedObject changedObject, boolean z) {
        onNmsEventChangedObjBufferDbSmsAvailable(cursor, changedObject, z, true);
    }

    public void onNmsEventDeletedObjSmsBufferDbAvailableUsingCorrTag(Cursor cursor, DeletedObject deletedObject, boolean z) {
        onNmsEventDeletedObjBufferDbSmsAvailable(cursor, deletedObject, z, true);
    }

    public void onNmsEventChangedObjBufferDbSmsAvailableUsingUrl(Cursor cursor, ChangedObject changedObject, boolean z) {
        onNmsEventChangedObjBufferDbSmsAvailable(cursor, changedObject, z, false);
    }

    public void onNmsEventDeletedObjBufferDbSmsAvailableUsingUrl(Cursor cursor, DeletedObject deletedObject, boolean z) {
        onNmsEventDeletedObjBufferDbSmsAvailable(cursor, deletedObject, z, false);
    }

    public void onNmsEventChangedObjBufferDbSmsAvailable(Cursor cursor, ChangedObject changedObject, boolean z, boolean z2) {
        Cursor cursor2 = cursor;
        ChangedObject changedObject2 = changedObject;
        long j = (long) cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long j2 = (long) cursor2.getInt(cursor2.getColumnIndexOrThrow("_id"));
        CloudMessageBufferDBConstants.ActionStatusFlag valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        String string = cursor2.getString(cursor2.getColumnIndexOrThrow("linenum"));
        long j3 = cursor2.getLong(cursor2.getColumnIndexOrThrow("date"));
        String string2 = cursor2.getString(cursor2.getColumnIndexOrThrow("body"));
        String str = this.TAG;
        Log.d(str, "onNmsEventChangedObjBufferDbSmsAvailable find bufferDB: " + changedObject2.correlationTag + " id: " + j + " time: " + j3 + " body:" + string2);
        String[] strArr = {String.valueOf(j)};
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, changedObject2.lastModSeq);
        if (z2) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(changedObject2.resourceURL.toString()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(changedObject2.parentFolder.toString()));
        }
        CloudMessageBufferDBConstants.ActionStatusFlag cloudActionPerFlag = this.mBufferDbQuery.getCloudActionPerFlag(changedObject2.flags);
        if (CloudMessageBufferDBConstants.ActionStatusFlag.Update.equals(cloudActionPerFlag)) {
            contentValues.put("read", 1);
        }
        long j4 = j2;
        ContentValues contentValues2 = contentValues;
        String str2 = CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION;
        ParamSyncFlagsSet setFlagsForCldOperationForCms = this.isCmsEnabled ? this.mScheduleRule.getSetFlagsForCldOperationForCms(this.mDbTableContractIndex, j, valueOf2, valueOf, cloudActionPerFlag) : this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, j, valueOf2, valueOf, cloudActionPerFlag);
        if (setFlagsForCldOperationForCms.mIsChanged) {
            contentValues2.put(str2, Integer.valueOf(setFlagsForCldOperationForCms.mDirection.getId()));
            contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(setFlagsForCldOperationForCms.mAction.getId()));
        }
        this.mBufferDbQuery.updateTable(this.mDbTableContractIndex, contentValues2, "_bufferdbid=?", strArr);
        if (j4 > 0) {
            handleOutPutParamSyncFlagSet(setFlagsForCldOperationForCms, j, this.mDbTableContractIndex, false, z, string, (BufferDBChangeParamList) null, false);
        }
    }

    public void onNmsEventDeletedObjBufferDbSmsAvailable(Cursor cursor, DeletedObject deletedObject, boolean z, boolean z2) {
        Cursor cursor2 = cursor;
        DeletedObject deletedObject2 = deletedObject;
        long j = (long) cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long j2 = (long) cursor2.getInt(cursor2.getColumnIndexOrThrow("_id"));
        CloudMessageBufferDBConstants.ActionStatusFlag valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        String string = cursor2.getString(cursor2.getColumnIndexOrThrow("linenum"));
        long j3 = cursor2.getLong(cursor2.getColumnIndexOrThrow("date"));
        String string2 = cursor2.getString(cursor2.getColumnIndexOrThrow("body"));
        String str = this.TAG;
        Log.d(str, "onNmsEventDeletedObjBufferDbSmsAvailable find bufferDB: " + deletedObject2.correlationTag + " id: " + j + " time: " + j3 + " body:" + string2);
        String[] strArr = {String.valueOf(j)};
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, Long.valueOf(deletedObject2.lastModSeq));
        if (z2) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(deletedObject2.resourceURL.toString()));
        }
        long j4 = j;
        long j5 = j;
        ContentValues contentValues2 = contentValues;
        ParamSyncFlagsSet setFlagsForCldOperation = this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, j4, valueOf2, valueOf, CloudMessageBufferDBConstants.ActionStatusFlag.Delete);
        if (setFlagsForCldOperation.mIsChanged) {
            contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(setFlagsForCldOperation.mDirection.getId()));
            contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(setFlagsForCldOperation.mAction.getId()));
        }
        this.mBufferDbQuery.updateTable(this.mDbTableContractIndex, contentValues2, "_bufferdbid=?", strArr);
        if (j2 > 0) {
            handleOutPutParamSyncFlagSet(setFlagsForCldOperation, j5, this.mDbTableContractIndex, false, z, string, (BufferDBChangeParamList) null, false);
        }
    }

    public void handleExistingBufferForGroupUpdate(long j, ParamAppJsonValue paramAppJsonValue, BufferDBChangeParamList bufferDBChangeParamList) {
        Throwable th;
        long j2 = j;
        ParamAppJsonValue paramAppJsonValue2 = paramAppJsonValue;
        String str = this.TAG;
        Log.i(str, "handleExistingBufferForGroupUpdate:  groupId:" + j2);
        Cursor queryNonHiddenSMSwithGroupId = this.mBufferDbQuery.queryNonHiddenSMSwithGroupId(j2);
        if (queryNonHiddenSMSwithGroupId != null) {
            try {
                if (queryNonHiddenSMSwithGroupId.moveToNext()) {
                    int i = queryNonHiddenSMSwithGroupId.getInt(queryNonHiddenSMSwithGroupId.getColumnIndexOrThrow("_id"));
                    String string = queryNonHiddenSMSwithGroupId.getString(queryNonHiddenSMSwithGroupId.getColumnIndexOrThrow("address"));
                    long j3 = queryNonHiddenSMSwithGroupId.getLong(queryNonHiddenSMSwithGroupId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    String str2 = this.TAG;
                    Log.i(str2, "handleExistingBufferForGroupUpdate bufferdb:" + j3 + " appId: " + i + " address: " + IMSLog.numberChecker(string));
                    int i2 = paramAppJsonValue2.mDataContractType;
                    CloudMessageBufferDBConstants.MsgOperationFlag msgOperationFlag = paramAppJsonValue2.mOperation;
                    int i3 = paramAppJsonValue2.mRowId;
                    String str3 = paramAppJsonValue2.mCorrelationTag;
                    String str4 = paramAppJsonValue2.mCorrelationId;
                    DeviceLegacyUpdateParam deviceLegacyUpdateParam = new DeviceLegacyUpdateParam(i2, msgOperationFlag, i3, str3, str4, str4, paramAppJsonValue2.mLine);
                    if (paramAppJsonValue2.mOperation == CloudMessageBufferDBConstants.MsgOperationFlag.Sent) {
                        IMSLog.i(this.TAG, "handleExistingBufferForGroupUpdate nothing to be done on SentMessage update for group sms");
                    } else {
                        handleExistingBufferForDeviceLegacyUpdate(queryNonHiddenSMSwithGroupId, deviceLegacyUpdateParam, false, bufferDBChangeParamList);
                    }
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (queryNonHiddenSMSwithGroupId != null) {
            queryNonHiddenSMSwithGroupId.close();
            return;
        }
        return;
        throw th;
    }

    public void handleExistingBufferForDeviceLegacyUpdate(Cursor cursor, DeviceLegacyUpdateParam deviceLegacyUpdateParam, boolean z, BufferDBChangeParamList bufferDBChangeParamList) {
        Cursor cursor2 = cursor;
        DeviceLegacyUpdateParam deviceLegacyUpdateParam2 = deviceLegacyUpdateParam;
        String str = this.TAG;
        Log.i(str, "handleExistingBufferForDeviceLegacyUpdate: " + deviceLegacyUpdateParam2 + ", mIsGoforwardSync: " + z + ", changelist: " + bufferDBChangeParamList);
        ContentValues contentValues = new ContentValues();
        CloudMessageBufferDBConstants.ActionStatusFlag valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        long j = cursor2.getLong(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        String string = cursor2.getString(cursor2.getColumnIndexOrThrow("linenum"));
        ParamSyncFlagsSet setFlagsForMsgOperation = this.mScheduleRule.getSetFlagsForMsgOperation(this.mDbTableContractIndex, j, valueOf2, valueOf, deviceLegacyUpdateParam2.mOperation);
        if (setFlagsForMsgOperation.mIsChanged) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(setFlagsForMsgOperation.mDirection.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(setFlagsForMsgOperation.mAction.getId()));
        }
        if (CloudMessageBufferDBConstants.MsgOperationFlag.Read.equals(deviceLegacyUpdateParam2.mOperation)) {
            contentValues.put("read", 1);
        }
        if (deviceLegacyUpdateParam2.mTableindex == 3) {
            String[] strArr = {String.valueOf(j)};
            int i = cursor2.getInt(cursor2.getColumnIndexOrThrow("_id"));
            long j2 = deviceLegacyUpdateParam2.mRowId;
            if (j2 != ((long) i)) {
                contentValues.put("_id", Long.valueOf(j2));
            }
            this.mBufferDbQuery.updateTable(deviceLegacyUpdateParam2.mTableindex, contentValues, "_bufferdbid=?", strArr);
        }
        if (setFlagsForMsgOperation.mIsChanged) {
            handleOutPutParamSyncFlagSet(setFlagsForMsgOperation, j, deviceLegacyUpdateParam2.mTableindex, false, z, string, bufferDBChangeParamList, false);
        }
    }

    public void handleNonExistingBufferForDeviceLegacyUpdate(DeviceLegacyUpdateParam deviceLegacyUpdateParam) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
        contentValues.put("linenum", deviceLegacyUpdateParam.mLine);
        if (deviceLegacyUpdateParam.mTableindex == 3) {
            Cursor querySMSUseRowId = this.mBufferDbQuery.querySMSUseRowId(Long.valueOf(deviceLegacyUpdateParam.mRowId).longValue());
            if (querySMSUseRowId != null) {
                try {
                    if (querySMSUseRowId.moveToFirst()) {
                        this.mBufferDbQuery.insertToSMSBufferDB(querySMSUseRowId, contentValues, false, true);
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (querySMSUseRowId != null) {
                querySMSUseRowId.close();
                return;
            }
            return;
        }
        return;
        throw th;
    }

    public void notifyMsgAppFetchBuffer(Cursor cursor, int i) {
        if (i == 3) {
            JsonArray jsonArray = new JsonArray();
            do {
                int i2 = cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", String.valueOf(i2));
                jsonArray.add(jsonObject);
                String str = this.TAG;
                Log.i(str, "jsonArrayRowIdsSMS.size(): " + jsonArray.size() + ", SMS: " + jsonArray.toString());
                if (jsonArray.size() == this.mMaxNumMsgsNotifyAppInIntent) {
                    this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.SMS, jsonArray.toString(), false);
                    jsonArray = new JsonArray();
                }
            } while (cursor.moveToNext());
            if (jsonArray.size() > 0) {
                this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.SMS, jsonArray.toString(), false);
            }
        }
    }

    public Cursor queryToCloudUnsyncedSms() {
        return this.mBufferDbQuery.queryToCloudUnsyncedSms();
    }

    public Cursor queryToDeviceUnsyncedSms() {
        return this.mBufferDbQuery.queryToDeviceUnsyncedSms();
    }

    public Cursor querySMSMessagesToUpload() {
        return this.mBufferDbQuery.querySMSMessagesToUpload();
    }

    public Cursor querySMSBufferDBwithResUrl(String str) {
        return this.mBufferDbQuery.querySMSBufferDBwithResUrl(str);
    }

    public int deleteSMSBufferDBwithResUrl(String str) {
        return this.mBufferDbQuery.deleteSMSBufferDBwithResUrl(str);
    }

    public Cursor searchUnSyncedSMSBufferUsingCorrelationTag(String str) {
        return this.mBufferDbQuery.searchUnSyncedSMSBufferUsingCorrelationTag(str);
    }

    public Cursor querySMSMessagesBySycnDirection(int i, String str) {
        return this.mBufferDbQuery.queryMessageBySyncDirection(i, str);
    }

    public Cursor queryAllSMSfromTelephony() {
        return this.mBufferDbQuery.queryAllSMSfromTelephony();
    }

    public Cursor querySMSfromTelephonyWithIMSI(String str) {
        return this.mBufferDbQuery.querySMSfromTelephonyWithIMSI(str);
    }

    public Cursor querySMSfromTelephonyWoIMSI() {
        return this.mBufferDbQuery.querySMSfromTelephonyWoIMSI();
    }

    public Cursor queryDeltaSMSfromTelephony() {
        return this.mBufferDbQuery.queryDeltaSMSfromTelephony();
    }

    public Cursor queryDeltaSMSfromTelephonyWoImsi() {
        return this.mBufferDbQuery.queryDeltaSMSfromTelephonyWoImsi();
    }

    public void syncReadSmsFromTelephony() {
        Cursor queryReadSMSfromTelephony;
        ArrayList arrayList = new ArrayList();
        try {
            queryReadSMSfromTelephony = this.mBufferDbQuery.queryReadSMSfromTelephony();
            if (queryReadSMSfromTelephony != null) {
                if (queryReadSMSfromTelephony.moveToFirst()) {
                    arrayList.add(queryReadSMSfromTelephony.getString(queryReadSMSfromTelephony.getColumnIndex("_id")));
                }
            }
            if (queryReadSMSfromTelephony != null) {
                queryReadSMSfromTelephony.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        for (int i = 0; i < arrayList.size(); i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
            contentValues.put("read", 1);
            this.mBufferDbQuery.updateTable(this.mDbTableContractIndex, contentValues, "_id=? AND read=? AND syncaction<>? AND syncaction<>?", new String[]{(String) arrayList.get(i), String.valueOf(0), String.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Delete.getId()), String.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId())});
        }
        return;
        throw th;
    }

    public void insertToSMSBufferDB(Cursor cursor, ContentValues contentValues, boolean z) {
        this.mBufferDbQuery.insertToSMSBufferDB(cursor, contentValues, z, false);
    }

    private void handleGroupSMSUpdateParam(ParamAppJsonValue paramAppJsonValue, BufferDBChangeParamList bufferDBChangeParamList) {
        String str = this.TAG;
        Log.i(str, "handleGroupSMSUpdateParam: " + paramAppJsonValue);
        Cursor cursor = null;
        try {
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[paramAppJsonValue.mOperation.ordinal()];
            if (i == 1) {
                cursor = this.mBufferDbQuery.searchSMSBufferUsingRowId((long) paramAppJsonValue.mRowId);
                if (cursor != null) {
                    if (!cursor.moveToNext()) {
                    }
                }
                IMSLog.i(this.TAG, "group message upload");
                bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(paramAppJsonValue.mDataContractType, this.mBufferDbQuery.handleGroupSMSUpload(paramAppJsonValue), false, paramAppJsonValue.mLine, CloudMessageBufferDBConstants.ActionStatusFlag.Update, this.mStoreClient, true));
                if (cursor != null) {
                    cursor.close();
                    return;
                }
                return;
            } else if (i == 2) {
                cursor = this.mBufferDbQuery.searchSMSBufferUsingRowId((long) paramAppJsonValue.mRowId);
            }
            if (cursor == null || !cursor.moveToFirst()) {
                IMSLog.e(this.TAG, "handleGroupSMSUpdateParam Invalid operation");
            } else {
                long j = cursor.getLong(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBSMS.GROUP_ID));
                long j2 = cursor.getLong(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                String str2 = this.TAG;
                IMSLog.i(str2, "handleExistingBufferForGroupUpdate bufferId:" + j2);
                handleExistingBufferForGroupUpdate(j, paramAppJsonValue, bufferDBChangeParamList);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag;

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|4|5|6|7|8|9|10|11|12|(3:13|14|16)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(16:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|16) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag[] r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag = r0
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Sent     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Delete     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Received     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Sending     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Receiving     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Read     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.SendFail     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler.AnonymousClass1.<clinit>():void");
        }
    }

    private void handleDeviceLegacyUpdateParam(DeviceLegacyUpdateParam deviceLegacyUpdateParam, boolean z, BufferDBChangeParamList bufferDBChangeParamList) {
        String str = this.TAG;
        Log.i(str, "handleDeviceLegacyUpdateParam: " + deviceLegacyUpdateParam);
        if (deviceLegacyUpdateParam.mTableindex == 3 && deviceLegacyUpdateParam.mCorrelationTag != null) {
            Cursor cursor = null;
            try {
                switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[deviceLegacyUpdateParam.mOperation.ordinal()]) {
                    case 1:
                        cursor = this.mBufferDbQuery.searchSMSBufferUsingCorrelationTagForEarlierNmsEvent(deviceLegacyUpdateParam.mCorrelationTag, deviceLegacyUpdateParam.mLine);
                        break;
                    case 2:
                        cursor = this.mBufferDbQuery.searchSMSBufferUsingRowId(deviceLegacyUpdateParam.mRowId);
                        break;
                    case 3:
                        cursor = this.mBufferDbQuery.searchSMSBufferUsingCorrelationTagForEarlierNmsEvent(deviceLegacyUpdateParam.mCorrelationTag, deviceLegacyUpdateParam.mLine);
                        break;
                    case 4:
                        cursor = this.mBufferDbQuery.searchSMSBufferUsingRowId(deviceLegacyUpdateParam.mRowId);
                        break;
                    case 5:
                        return;
                    case 6:
                        cursor = this.mBufferDbQuery.searchSMSBufferUsingRowId(deviceLegacyUpdateParam.mRowId);
                        break;
                    case 7:
                        cursor = this.mBufferDbQuery.searchSMSBufferUsingRowId(deviceLegacyUpdateParam.mRowId);
                        break;
                }
                if (cursor == null || !cursor.moveToFirst()) {
                    handleNonExistingBufferForDeviceLegacyUpdate(deviceLegacyUpdateParam);
                } else {
                    handleExistingBufferForDeviceLegacyUpdate(cursor, deviceLegacyUpdateParam, z, bufferDBChangeParamList);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public void onUpdateFromDeviceMsgAppFetch(DeviceMsgAppFetchUpdateParam deviceMsgAppFetchUpdateParam, boolean z) {
        onUpdateFromDeviceMsgAppFetch(deviceMsgAppFetchUpdateParam, z, this.mBufferDbQuery);
    }

    public void onUpdateFromDeviceMsgAppFetchFailed(DeviceMsgAppFetchUpdateParam deviceMsgAppFetchUpdateParam) {
        this.mBufferDbQuery.updateAppFetchingFailed(deviceMsgAppFetchUpdateParam.mTableindex, deviceMsgAppFetchUpdateParam.mBufferRowId);
    }

    public void onCloudUpdateFlagSuccess(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z) {
        onCloudUpdateFlagSuccess(paramOMAresponseforBufDB, z, this.mBufferDbQuery);
    }

    public void onCloudUploadSuccess(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z) {
        if (paramOMAresponseforBufDB.getReference() != null) {
            handleCloudUploadSuccess(paramOMAresponseforBufDB, z, this.mBufferDbQuery, 3);
        }
    }

    public void onGroupSMSUploadSuccess(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        if (paramOMAresponseforBufDB.getBufferDBChangeParam().mIsGroupSMSUpload) {
            handleGroupSMSUploadResponse(paramOMAresponseforBufDB);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x006b  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x007a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleGroupSMSUploadResponse(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r10) {
        /*
            r9 = this;
            java.lang.String r0 = r9.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "handleGroupSMSUploadResponse :"
            r1.append(r2)
            r1.append(r10)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.i(r0, r1)
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r0 = r10.getBufferDBChangeParam()
            com.sec.internal.omanetapi.nms.data.Object r2 = r10.getObject()
            com.sec.internal.ims.cmstore.params.ParamOMAObject r10 = new com.sec.internal.ims.cmstore.params.ParamOMAObject
            r3 = 0
            r4 = 3
            r5 = 0
            com.sec.internal.ims.cmstore.MessageStoreClient r6 = r9.mStoreClient
            r1 = r10
            r1.<init>((com.sec.internal.omanetapi.nms.data.Object) r2, (boolean) r3, (int) r4, (com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper) r5, (com.sec.internal.ims.cmstore.MessageStoreClient) r6)
            com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder r1 = r9.mBufferDbQuery
            int r2 = r9.mDbTableContractIndex
            long r3 = r0.mRowId
            android.database.Cursor r0 = r1.queryTablewithBufferDbId(r2, r3)
            r1 = -1
            if (r0 == 0) goto L_0x0068
            boolean r3 = r0.moveToFirst()     // Catch:{ all -> 0x005e }
            if (r3 == 0) goto L_0x0068
            java.lang.String r3 = "group_id"
            int r3 = r0.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x005e }
            long r3 = r0.getLong(r3)     // Catch:{ all -> 0x005e }
            java.lang.String r5 = r9.TAG     // Catch:{ all -> 0x005e }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x005e }
            r6.<init>()     // Catch:{ all -> 0x005e }
            java.lang.String r7 = "handleGroupSMSUploadResponse groupId:"
            r6.append(r7)     // Catch:{ all -> 0x005e }
            r6.append(r3)     // Catch:{ all -> 0x005e }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x005e }
            com.sec.internal.log.IMSLog.i(r5, r6)     // Catch:{ all -> 0x005e }
            goto L_0x0069
        L_0x005e:
            r9 = move-exception
            r0.close()     // Catch:{ all -> 0x0063 }
            goto L_0x0067
        L_0x0063:
            r10 = move-exception
            r9.addSuppressed(r10)
        L_0x0067:
            throw r9
        L_0x0068:
            r3 = r1
        L_0x0069:
            if (r0 == 0) goto L_0x006e
            r0.close()
        L_0x006e:
            int r0 = (r3 > r1 ? 1 : (r3 == r1 ? 0 : -1))
            if (r0 != 0) goto L_0x007a
            java.lang.String r9 = r9.TAG
            java.lang.String r10 = "invalid groupId, return"
            com.sec.internal.log.IMSLog.e(r9, r10)
            return
        L_0x007a:
            com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder r0 = r9.mBufferDbQuery
            android.database.Cursor r0 = r0.querySMSwithGroupId(r3)
            if (r0 == 0) goto L_0x0104
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x00fa }
            if (r1 == 0) goto L_0x0104
        L_0x0088:
            android.content.ContentValues r1 = new android.content.ContentValues     // Catch:{ all -> 0x00fa }
            r1.<init>()     // Catch:{ all -> 0x00fa }
            java.lang.String r2 = "group_cotag"
            java.lang.String r3 = r10.correlationTag     // Catch:{ all -> 0x00fa }
            r1.put(r2, r3)     // Catch:{ all -> 0x00fa }
            java.lang.String r2 = "_bufferdbid"
            int r2 = r0.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x00fa }
            int r2 = r0.getInt(r2)     // Catch:{ all -> 0x00fa }
            long r2 = (long) r2     // Catch:{ all -> 0x00fa }
            java.lang.String r4 = "hidden"
            int r4 = r0.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x00fa }
            int r4 = r0.getInt(r4)     // Catch:{ all -> 0x00fa }
            if (r4 != 0) goto L_0x00cb
            java.lang.String r4 = "syncaction"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None     // Catch:{ all -> 0x00fa }
            int r5 = r5.getId()     // Catch:{ all -> 0x00fa }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x00fa }
            r1.put(r4, r5)     // Catch:{ all -> 0x00fa }
            java.lang.String r4 = "syncdirection"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done     // Catch:{ all -> 0x00fa }
            int r5 = r5.getId()     // Catch:{ all -> 0x00fa }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x00fa }
            r1.put(r4, r5)     // Catch:{ all -> 0x00fa }
        L_0x00cb:
            java.lang.String r4 = "_bufferdbid=?"
            r5 = 1
            java.lang.String[] r5 = new java.lang.String[r5]     // Catch:{ all -> 0x00fa }
            java.lang.String r6 = java.lang.String.valueOf(r2)     // Catch:{ all -> 0x00fa }
            r7 = 0
            r5[r7] = r6     // Catch:{ all -> 0x00fa }
            java.lang.String r6 = r9.TAG     // Catch:{ all -> 0x00fa }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x00fa }
            r7.<init>()     // Catch:{ all -> 0x00fa }
            java.lang.String r8 = "handleGroupSMSUploadResponse msgbufferdbid:"
            r7.append(r8)     // Catch:{ all -> 0x00fa }
            r7.append(r2)     // Catch:{ all -> 0x00fa }
            java.lang.String r2 = r7.toString()     // Catch:{ all -> 0x00fa }
            com.sec.internal.log.IMSLog.i(r6, r2)     // Catch:{ all -> 0x00fa }
            com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder r2 = r9.mBufferDbQuery     // Catch:{ all -> 0x00fa }
            r3 = 3
            r2.updateTable(r3, r1, r4, r5)     // Catch:{ all -> 0x00fa }
            boolean r1 = r0.moveToNext()     // Catch:{ all -> 0x00fa }
            if (r1 != 0) goto L_0x0088
            goto L_0x0104
        L_0x00fa:
            r9 = move-exception
            r0.close()     // Catch:{ all -> 0x00ff }
            goto L_0x0103
        L_0x00ff:
            r10 = move-exception
            r9.addSuppressed(r10)
        L_0x0103:
            throw r9
        L_0x0104:
            if (r0 == 0) goto L_0x0109
            r0.close()
        L_0x0109:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler.handleGroupSMSUploadResponse(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB):void");
    }

    public void cleanAllBufferDB() {
        this.mBufferDbQuery.cleanAllBufferDB();
    }

    public void onAppOperationReceived(ParamAppJsonValue paramAppJsonValue, BufferDBChangeParamList bufferDBChangeParamList) {
        String str = this.TAG;
        Log.i(str, "onAppOperationReceived: " + paramAppJsonValue);
        if (paramAppJsonValue.mIsGroupSMS) {
            handleGroupSMSUpdateParam(paramAppJsonValue, bufferDBChangeParamList);
            return;
        }
        int i = paramAppJsonValue.mDataContractType;
        CloudMessageBufferDBConstants.MsgOperationFlag msgOperationFlag = paramAppJsonValue.mOperation;
        int i2 = paramAppJsonValue.mRowId;
        String str2 = paramAppJsonValue.mCorrelationTag;
        String str3 = paramAppJsonValue.mCorrelationId;
        handleDeviceLegacyUpdateParam(new DeviceLegacyUpdateParam(i, msgOperationFlag, i2, str2, str3, str3, paramAppJsonValue.mLine), false, bufferDBChangeParamList);
    }

    public boolean handleCrossSearchObj(ParamOMAObject paramOMAObject, String str, boolean z) {
        String str2 = this.TAG;
        Log.i(str2, "handleCrossSearchObj():  line: " + IMSLog.checker(str) + " objt: " + paramOMAObject);
        Cursor querySMSBufferDBwithResUrl = querySMSBufferDBwithResUrl(paramOMAObject.resourceURL.toString());
        if (querySMSBufferDBwithResUrl != null) {
            try {
                if (querySMSBufferDBwithResUrl.moveToFirst()) {
                    onCrossObjectSearchSmsAvailableUsingResUrl(querySMSBufferDBwithResUrl, paramOMAObject, str, z);
                    querySMSBufferDBwithResUrl.close();
                    return true;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (querySMSBufferDBwithResUrl != null) {
            querySMSBufferDBwithResUrl.close();
        }
        Cursor searchUnSyncedSMSBufferUsingCorrelationTag = searchUnSyncedSMSBufferUsingCorrelationTag(paramOMAObject.correlationTag);
        if (searchUnSyncedSMSBufferUsingCorrelationTag != null) {
            try {
                if (searchUnSyncedSMSBufferUsingCorrelationTag.moveToFirst()) {
                    onCrossObjectSearchSmsAvailableUsingCorrTag(searchUnSyncedSMSBufferUsingCorrelationTag, paramOMAObject, str, z);
                    searchUnSyncedSMSBufferUsingCorrelationTag.close();
                    return true;
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (searchUnSyncedSMSBufferUsingCorrelationTag == null) {
            return false;
        }
        searchUnSyncedSMSBufferUsingCorrelationTag.close();
        return false;
        throw th;
        throw th;
    }

    private void onCrossObjectSearchSmsAvailableUsingResUrl(Cursor cursor, ParamOMAObject paramOMAObject, String str, boolean z) {
        onCrossObjectSearchSmsAvailable(cursor, paramOMAObject, str, z, false);
    }

    private void onCrossObjectSearchSmsAvailableUsingCorrTag(Cursor cursor, ParamOMAObject paramOMAObject, String str, boolean z) {
        onCrossObjectSearchSmsAvailable(cursor, paramOMAObject, str, z, true);
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x00e4  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x00fa  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:22:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onCrossObjectSearchSmsAvailable(android.database.Cursor r18, com.sec.internal.ims.cmstore.params.ParamOMAObject r19, java.lang.String r20, boolean r21, boolean r22) {
        /*
            r17 = this;
            r0 = r17
            r1 = r18
            r2 = r19
            java.lang.String r3 = "_bufferdbid"
            int r3 = r1.getColumnIndexOrThrow(r3)
            int r3 = r1.getInt(r3)
            long r11 = (long) r3
            java.lang.String r3 = "_id"
            int r3 = r1.getColumnIndexOrThrow(r3)
            int r3 = r1.getInt(r3)
            long r13 = (long) r3
            java.lang.String r3 = "syncaction"
            int r4 = r1.getColumnIndexOrThrow(r3)
            int r4 = r1.getInt(r4)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r4)
            java.lang.String r15 = "syncdirection"
            int r5 = r1.getColumnIndexOrThrow(r15)
            int r5 = r1.getInt(r5)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r5)
            java.lang.String r6 = "date"
            int r6 = r1.getColumnIndexOrThrow(r6)
            long r6 = r1.getLong(r6)
            java.lang.String r8 = "body"
            int r8 = r1.getColumnIndexOrThrow(r8)
            java.lang.String r8 = r1.getString(r8)
            java.lang.String r9 = r0.TAG
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            r16 = r4
            java.lang.String r4 = "handleCrossSearchObj find bufferDB: "
            r10.append(r4)
            java.lang.String r4 = r2.correlationTag
            r10.append(r4)
            java.lang.String r4 = " id: "
            r10.append(r4)
            r10.append(r11)
            java.lang.String r4 = " time: "
            r10.append(r4)
            r10.append(r6)
            java.lang.String r4 = " body:"
            r10.append(r4)
            r10.append(r8)
            java.lang.String r4 = r10.toString()
            android.util.Log.d(r9, r4)
            android.content.ContentValues r10 = new android.content.ContentValues
            r10.<init>()
            java.lang.String r4 = "lastmodseq"
            java.lang.Long r6 = r2.lastModSeq
            r10.put(r4, r6)
            r4 = 1
            java.lang.String r6 = "read"
            if (r22 == 0) goto L_0x00d1
            java.net.URL r7 = r2.resourceURL
            java.lang.String r7 = r7.toString()
            java.lang.String r7 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r7)
            java.lang.String r8 = "res_url"
            r10.put(r8, r7)
            java.net.URL r7 = r2.parentFolder
            if (r7 == 0) goto L_0x00b3
            java.lang.String r7 = r7.toString()
            java.lang.String r7 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r7)
            java.lang.String r8 = "parentfolder"
            r10.put(r8, r7)
        L_0x00b3:
            java.lang.String r7 = r2.path
            if (r7 == 0) goto L_0x00c0
            java.lang.String r8 = "path"
            java.lang.String r7 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r7)
            r10.put(r8, r7)
        L_0x00c0:
            int r7 = r1.getColumnIndexOrThrow(r6)
            int r1 = r1.getInt(r7)
            if (r1 != r4) goto L_0x00d1
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud
            r9 = r1
            r8 = r5
            goto L_0x00d4
        L_0x00d1:
            r8 = r5
            r9 = r16
        L_0x00d4:
            com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder r1 = r0.mBufferDbQuery
            com.sec.internal.omanetapi.nms.data.FlagList r2 = r2.mFlagList
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = r1.getCloudActionPerFlag(r2)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update
            boolean r2 = r2.equals(r1)
            if (r2 == 0) goto L_0x00eb
            java.lang.Integer r2 = java.lang.Integer.valueOf(r4)
            r10.put(r6, r2)
        L_0x00eb:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r4 = r0.mScheduleRule
            int r5 = r0.mDbTableContractIndex
            r6 = r11
            r2 = r10
            r10 = r1
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r1 = r4.getSetFlagsForCldOperation(r5, r6, r8, r9, r10)
            boolean r4 = r1.mIsChanged
            if (r4 == 0) goto L_0x0114
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r4 = r1.mDirection
            int r4 = r4.getId()
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r2.put(r15, r4)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = r1.mAction
            int r4 = r4.getId()
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r2.put(r3, r4)
        L_0x0114:
            com.sec.internal.ims.cmstore.querybuilders.SmsQueryBuilder r3 = r0.mBufferDbQuery
            r0.updateQueryTable(r2, r11, r3)
            r2 = 0
            int r2 = (r13 > r2 ? 1 : (r13 == r2 ? 0 : -1))
            if (r2 <= 0) goto L_0x012d
            r4 = 3
            r5 = 0
            r8 = 0
            r9 = 0
            r0 = r17
            r2 = r11
            r6 = r21
            r7 = r20
            r0.handleOutPutParamSyncFlagSet(r1, r2, r4, r5, r6, r7, r8, r9)
        L_0x012d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler.onCrossObjectSearchSmsAvailable(android.database.Cursor, com.sec.internal.ims.cmstore.params.ParamOMAObject, java.lang.String, boolean, boolean):void");
    }

    public void updateCorrelationTagObject(ParamOMAObject paramOMAObject) {
        int i;
        String str;
        String str2 = this.TAG;
        Log.i(str2, "updateCorrelationTagObject: " + paramOMAObject);
        try {
            String str3 = paramOMAObject.TEXT_CONTENT;
            if ("IN".equalsIgnoreCase(paramOMAObject.DIRECTION)) {
                str = Util.getMsisdn(paramOMAObject.FROM, Util.getSimCountryCode(this.mContext, this.mStoreClient.getClientID()));
                i = 1;
            } else {
                str = Util.getMsisdn(paramOMAObject.TO.get(0), Util.getSimCountryCode(this.mContext, this.mStoreClient.getClientID()));
                i = 2;
            }
            paramOMAObject.correlationTag = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getSmsHashTagOrCorrelationTag(str, i, str3);
        } catch (Exception e) {
            String str4 = this.TAG;
            Log.e(str4, "updateCorrelationTagObject: " + e.getMessage());
        }
    }

    public void notifyMsgAppDeleteFail(int i, long j, String str) {
        String str2 = this.TAG;
        Log.i(str2, "notifyMsgAppDeleteFail, dbIndex: " + i + " bufferDbId: " + j + " line: " + IMSLog.checker(str));
        if (i == 3) {
            JsonArray jsonArray = new JsonArray();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", String.valueOf(j));
            jsonArray.add(jsonObject);
            this.mCallbackMsgApp.notifyAppCloudDeleteFail(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.SMS, jsonArray.toString());
        }
    }

    public void onUpdateCmsConfig() {
        this.mBufferDbQuery.onUpdateCmsConfigInitSyncDataTtl();
    }
}
