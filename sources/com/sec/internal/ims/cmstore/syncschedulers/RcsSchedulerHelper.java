package com.sec.internal.ims.cmstore.syncschedulers;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.adapters.CloudMessageTelephonyStorageAdapter;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.DeviceIMFTUpdateParam;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUriParam;
import com.sec.internal.ims.cmstore.params.DeviceSessionPartcptsUpdateParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.DeletedObject;
import com.sec.internal.omanetapi.nms.data.Object;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class RcsSchedulerHelper extends BaseMessagingScheduler {
    private String TAG = RcsSchedulerHelper.class.getSimpleName();
    protected final RcsQueryBuilder mBufferDbQuery;
    protected final MmsScheduler mMmsScheduler;
    protected final SmsScheduler mSmsScheduler;
    private final CloudMessageTelephonyStorageAdapter mTelephonyStorage;

    public RcsSchedulerHelper(MessageStoreClient messageStoreClient, CloudMessageBufferDBEventSchedulingRule cloudMessageBufferDBEventSchedulingRule, SummaryQueryBuilder summaryQueryBuilder, IDeviceDataChangeListener iDeviceDataChangeListener, IBufferDBEventListener iBufferDBEventListener, MmsScheduler mmsScheduler, SmsScheduler smsScheduler, Looper looper) {
        super(messageStoreClient, cloudMessageBufferDBEventSchedulingRule, iDeviceDataChangeListener, iBufferDBEventListener, looper, summaryQueryBuilder);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mSummaryDB = summaryQueryBuilder;
        this.mBufferDbQuery = new RcsQueryBuilder(messageStoreClient, iBufferDBEventListener);
        this.mDbTableContractIndex = 1;
        this.mMmsScheduler = mmsScheduler;
        this.mSmsScheduler = smsScheduler;
        this.mTelephonyStorage = new CloudMessageTelephonyStorageAdapter(messageStoreClient.getContext());
    }

    public void resetImsi() {
        Log.i(this.TAG, "resetImsi");
        this.mBufferDbQuery.resetImsi();
        this.mMmsScheduler.mBufferDbQuery.resetImsi();
        this.mSmsScheduler.mBufferDbQuery.resetImsi();
    }

    public void onNmsEventChangedObjBufferDbRcsAvailableUsingUrl(Cursor cursor, ChangedObject changedObject, boolean z) {
        onNmsEventChangedObjRCSBufferDbAvailable(cursor, changedObject, z);
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x01a4  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x01f5  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x01fe  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0206  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x02a6  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x030a  */
    /* JADX WARNING: Removed duplicated region for block: B:97:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onNmsEventChangedObjRCSBufferDbAvailable(android.database.Cursor r32, com.sec.internal.omanetapi.nms.data.ChangedObject r33, boolean r34) {
        /*
            r31 = this;
            r0 = r31
            r1 = r32
            r2 = r33
            java.lang.String r3 = r0.TAG
            java.lang.String r4 = "onNmsEventChangedObjRCSBufferDbAvailable"
            android.util.Log.i(r3, r4)
            java.lang.String r3 = "_bufferdbid"
            int r3 = r1.getColumnIndexOrThrow(r3)
            int r3 = r1.getInt(r3)
            long r11 = (long) r3
            java.lang.String r3 = "_id"
            int r3 = r1.getColumnIndexOrThrow(r3)
            int r3 = r1.getInt(r3)
            long r13 = (long) r3
            java.lang.String r3 = "is_filetransfer"
            int r3 = r1.getColumnIndexOrThrow(r3)
            int r3 = r1.getInt(r3)
            r10 = 1
            if (r3 != r10) goto L_0x0032
            r3 = r10
            goto L_0x0033
        L_0x0032:
            r3 = 0
        L_0x0033:
            java.lang.String r4 = "direction"
            int r4 = r1.getColumnIndexOrThrow(r4)
            int r9 = r1.getInt(r4)
            java.lang.String r4 = "linenum"
            int r4 = r1.getColumnIndexOrThrow(r4)
            java.lang.String r16 = r1.getString(r4)
            java.lang.String r8 = "syncaction"
            int r4 = r1.getColumnIndexOrThrow(r8)
            int r4 = r1.getInt(r4)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r17 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r4)
            java.lang.String r6 = "syncdirection"
            int r4 = r1.getColumnIndexOrThrow(r6)
            int r4 = r1.getInt(r4)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r18 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r4)
            java.lang.String r7 = "status"
            int r4 = r1.getColumnIndexOrThrow(r7)
            int r5 = r1.getInt(r4)
            android.content.ContentValues r4 = new android.content.ContentValues
            r4.<init>()
            java.lang.Long r10 = r2.lastModSeq
            java.lang.String r15 = "lastmodseq"
            r4.put(r15, r10)
            java.net.URL r10 = r2.resourceURL
            java.lang.String r10 = r10.toString()
            java.lang.String r10 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r10)
            r21 = r6
            java.lang.String r6 = "res_url"
            r4.put(r6, r10)
            java.net.URL r6 = r2.parentFolder
            java.lang.String r6 = r6.toString()
            java.lang.String r6 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r6)
            java.lang.String r10 = "parentfolder"
            r4.put(r10, r6)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r6 = r0.mBufferDbQuery
            com.sec.internal.omanetapi.nms.data.FlagList r10 = r2.flags
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r10 = r6.getCloudActionPerFlag(r10)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update
            boolean r22 = r6.equals(r10)
            if (r22 == 0) goto L_0x00bf
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r22 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.READ
            int r22 = r22.getId()
            r23 = r7
            r25 = r8
            r24 = r15
            r15 = r3
            r3 = r22
            r22 = r4
            goto L_0x010a
        L_0x00bf:
            r22 = r4
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Cancel
            boolean r4 = r4.equals(r10)
            if (r4 == 0) goto L_0x0102
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r0.mBufferDbQuery
            r23 = r7
            com.sec.internal.omanetapi.nms.data.FlagList r7 = r2.flags
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r24 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING
            r25 = r8
            int r8 = r24.getId()
            if (r9 != r8) goto L_0x00dc
            java.lang.String r8 = "IN"
            goto L_0x00de
        L_0x00dc:
            java.lang.String r8 = "OUT"
        L_0x00de:
            int r4 = r4.updateMessageStatus(r7, r8)
            java.lang.String r7 = r0.TAG
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r24 = r15
            java.lang.String r15 = "msgStatus for cancel: "
            r8.append(r15)
            r8.append(r4)
            java.lang.String r8 = r8.toString()
            com.sec.internal.log.IMSLog.i(r7, r8)
            if (r3 == 0) goto L_0x00ff
            r3 = r4
            r15 = 0
            goto L_0x010a
        L_0x00ff:
            r15 = r3
            r3 = r4
            goto L_0x010a
        L_0x0102:
            r23 = r7
            r25 = r8
            r24 = r15
            r15 = r3
            r3 = 0
        L_0x010a:
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r4 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r8 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None
            r4.<init>(r7, r8)
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r26 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.CANCELLATION
            int r7 = r26.getId()
            java.lang.String r8 = "ft_status"
            if (r3 != r7) goto L_0x013f
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r7 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.CANCELLATION_UNREAD
            int r7 = r7.getId()
            if (r5 != r7) goto L_0x013f
            r4.mAction = r6
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice
            r4.mDirection = r6
            r29 = r11
            r27 = r13
            r2 = r21
            r1 = r22
            r13 = r23
            r14 = r25
            r11 = r8
            r12 = r9
            r22 = r15
            r15 = r5
            r5 = 0
            goto L_0x019f
        L_0x013f:
            if (r3 == r5) goto L_0x017b
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r6 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.CANCELLATION_UNREAD
            int r6 = r6.getId()
            if (r3 == r6) goto L_0x014f
            int r6 = r26.getId()
            if (r3 != r6) goto L_0x0158
        L_0x014f:
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r6 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING
            int r6 = r6.getId()
            if (r9 != r6) goto L_0x0158
            goto L_0x017b
        L_0x0158:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r4 = r0.mScheduleRule
            int r6 = r0.mDbTableContractIndex
            r7 = r22
            r22 = r15
            r15 = r5
            r5 = r6
            r1 = r7
            r27 = r13
            r2 = r21
            r13 = r23
            r6 = r11
            r29 = r11
            r14 = r25
            r11 = r8
            r8 = r18
            r12 = r9
            r9 = r17
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r4 = r4.getSetFlagsForCldOperation(r5, r6, r8, r9, r10)
            r10 = r4
            r5 = 0
            goto L_0x01a0
        L_0x017b:
            r29 = r11
            r27 = r13
            r2 = r21
            r1 = r22
            r13 = r23
            r14 = r25
            r11 = r8
            r12 = r9
            r22 = r15
            r15 = r5
            r5 = 0
            r4.mIsChanged = r5
            if (r3 == r15) goto L_0x019f
            java.lang.Integer r6 = java.lang.Integer.valueOf(r3)
            r1.put(r13, r6)
            java.lang.Integer r6 = java.lang.Integer.valueOf(r3)
            r1.put(r11, r6)
        L_0x019f:
            r10 = r4
        L_0x01a0:
            boolean r4 = r10.mIsChanged
            if (r4 == 0) goto L_0x01ea
            java.lang.String r4 = r0.TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "msgStatus: "
            r6.append(r7)
            r6.append(r3)
            java.lang.String r7 = " currStatus:"
            r6.append(r7)
            r6.append(r15)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.i(r4, r6)
            java.lang.Integer r4 = java.lang.Integer.valueOf(r3)
            r1.put(r13, r4)
            java.lang.Integer r4 = java.lang.Integer.valueOf(r3)
            r1.put(r11, r4)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r4 = r10.mDirection
            int r4 = r4.getId()
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r1.put(r2, r4)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = r10.mAction
            int r2 = r2.getId()
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r1.put(r14, r2)
        L_0x01ea:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r0.mBufferDbQuery
            r14 = r29
            r0.updateQueryTable(r1, r14, r2)
            boolean r2 = r10.mIsChanged
            if (r2 == 0) goto L_0x01fe
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r0.mBufferDbQuery
            r8 = r27
            int r4 = (int) r8
            r2.updateRCSMessageDb(r4, r1)
            goto L_0x0200
        L_0x01fe:
            r8 = r27
        L_0x0200:
            boolean r2 = r0.isCmsEnabled
            r17 = 0
            if (r2 == 0) goto L_0x0303
            r2 = r33
            com.sec.internal.omanetapi.nms.data.ImdnList r4 = r2.imdns
            if (r4 == 0) goto L_0x0303
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r4 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING
            int r4 = r4.getId()
            if (r12 != r4) goto L_0x0303
            int r4 = r26.getId()
            if (r3 == r4) goto L_0x0303
            r1.remove(r13)
            r1.remove(r11)
            java.lang.String r3 = "chat_id"
            r4 = r1
            r1 = r32
            int r3 = r1.getColumnIndexOrThrow(r3)
            java.lang.String r3 = r1.getString(r3)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r6 = r0.mBufferDbQuery
            android.database.Cursor r3 = r6.querySessionByChatId(r3)
            if (r3 == 0) goto L_0x0256
            boolean r6 = r3.moveToFirst()     // Catch:{ all -> 0x024a }
            if (r6 == 0) goto L_0x0256
            java.lang.String r6 = "is_group_chat"
            int r6 = r3.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x024a }
            int r6 = r3.getInt(r6)     // Catch:{ all -> 0x024a }
            r7 = 1
            if (r6 != r7) goto L_0x0256
            r5 = r7
            goto L_0x0256
        L_0x024a:
            r0 = move-exception
            r1 = r0
            r3.close()     // Catch:{ all -> 0x0250 }
            goto L_0x0255
        L_0x0250:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)
        L_0x0255:
            throw r1
        L_0x0256:
            if (r3 == 0) goto L_0x025b
            r3.close()
        L_0x025b:
            java.lang.String r3 = r0.TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "onNmsEventChangedObjRCSBufferDbAvailable isGroupChat: "
            r6.append(r7)
            r6.append(r5)
            java.lang.String r6 = r6.toString()
            android.util.Log.i(r3, r6)
            if (r5 == 0) goto L_0x02fa
            java.lang.String r3 = "imdn_message_id"
            int r3 = r1.getColumnIndexOrThrow(r3)
            java.lang.String r1 = r1.getString(r3)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r3 = r0.mBufferDbQuery
            android.database.Cursor r1 = r3.queryLargestLastModSeqRow(r1)
            if (r1 == 0) goto L_0x02a2
            boolean r3 = r1.moveToFirst()     // Catch:{ all -> 0x0296 }
            if (r3 == 0) goto L_0x02a2
            r3 = r24
            int r3 = r1.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x0296 }
            long r3 = r1.getLong(r3)     // Catch:{ all -> 0x0296 }
            goto L_0x02a4
        L_0x0296:
            r0 = move-exception
            r2 = r0
            r1.close()     // Catch:{ all -> 0x029c }
            goto L_0x02a1
        L_0x029c:
            r0 = move-exception
            r1 = r0
            r2.addSuppressed(r1)
        L_0x02a1:
            throw r2
        L_0x02a2:
            r3 = r17
        L_0x02a4:
            if (r1 == 0) goto L_0x02a9
            r1.close()
        L_0x02a9:
            java.lang.String r1 = r0.TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "onNmsEventChangedObjRCSBufferDbAvailable object imdns lastModSeq: "
            r5.append(r6)
            com.sec.internal.omanetapi.nms.data.ImdnList r6 = r2.imdns
            long r6 = r6.lastModSeq
            r5.append(r6)
            java.lang.String r6 = ", notificationDbLastModSeq: "
            r5.append(r6)
            r5.append(r3)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r1, r5)
            com.sec.internal.omanetapi.nms.data.ImdnList r1 = r2.imdns
            long r1 = r1.lastModSeq
            int r5 = (r1 > r17 ? 1 : (r1 == r17 ? 0 : -1))
            if (r5 == 0) goto L_0x0303
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r1 <= 0) goto L_0x0303
            com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r1 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParamList
            r1.<init>()
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r2 = r1.mChangelst
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r3 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam
            r5 = 13
            r11 = 0
            com.sec.internal.ims.cmstore.MessageStoreClient r12 = r0.mStoreClient
            r4 = r3
            r6 = r14
            r19 = r8
            r8 = r11
            r9 = r16
            r11 = r10
            r10 = r12
            r4.<init>(r5, r6, r8, r9, r10)
            r2.add(r3)
            com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener r2 = r0.mDeviceDataChangeListener
            r2.sendDeviceNormalSyncDownload(r1)
            goto L_0x0306
        L_0x02fa:
            r19 = r8
            r11 = r10
            com.sec.internal.omanetapi.nms.data.ImdnList r2 = r2.imdns
            r0.updateRCSImdnToBufferDB(r2, r4, r1)
            goto L_0x0306
        L_0x0303:
            r19 = r8
            r11 = r10
        L_0x0306:
            int r1 = (r19 > r17 ? 1 : (r19 == r17 ? 0 : -1))
            if (r1 <= 0) goto L_0x031a
            r5 = 1
            r9 = 0
            r10 = 0
            r1 = r31
            r2 = r11
            r3 = r14
            r6 = r22
            r7 = r34
            r8 = r16
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9, r10)
        L_0x031a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsSchedulerHelper.onNmsEventChangedObjRCSBufferDbAvailable(android.database.Cursor, com.sec.internal.omanetapi.nms.data.ChangedObject, boolean):void");
    }

    public void onNmsEventChangedObjRcsBufferDbAvailableUsingImdnId(Cursor cursor, ChangedObject changedObject, boolean z) {
        onNmsEventChangedObjRCSBufferDbAvailable(cursor, changedObject, z);
    }

    public void onNmsEventDeletedObjBufferDbRcsAvailableUsingImdnId(Cursor cursor, DeletedObject deletedObject, boolean z) {
        onNmsEventDeletedObjBufferDbRcsAvailable(cursor, deletedObject, z);
    }

    public void onNmsEventDeletedObjBufferDbRcsAvailableUsingUrl(Cursor cursor, DeletedObject deletedObject, boolean z) {
        onNmsEventDeletedObjBufferDbRcsAvailable(cursor, deletedObject, z);
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x00ce  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0120  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0143  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x014c  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0154  */
    /* JADX WARNING: Removed duplicated region for block: B:42:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onNmsEventDeletedObjBufferDbRcsAvailable(android.database.Cursor r22, com.sec.internal.omanetapi.nms.data.DeletedObject r23, boolean r24) {
        /*
            r21 = this;
            r0 = r21
            r1 = r22
            java.lang.String r2 = "_bufferdbid"
            int r2 = r1.getColumnIndexOrThrow(r2)
            int r2 = r1.getInt(r2)
            long r10 = (long) r2
            java.lang.String r2 = "_id"
            int r2 = r1.getColumnIndexOrThrow(r2)
            int r2 = r1.getInt(r2)
            long r12 = (long) r2
            java.lang.String r2 = "is_filetransfer"
            int r2 = r1.getColumnIndexOrThrow(r2)
            int r2 = r1.getInt(r2)
            r4 = 1
            if (r2 != r4) goto L_0x0028
            goto L_0x0029
        L_0x0028:
            r4 = 0
        L_0x0029:
            java.lang.String r2 = "linenum"
            int r2 = r1.getColumnIndexOrThrow(r2)
            java.lang.String r14 = r1.getString(r2)
            java.lang.String r2 = "syncaction"
            int r5 = r1.getColumnIndexOrThrow(r2)
            int r5 = r1.getInt(r5)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r8 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r5)
            java.lang.String r15 = "syncdirection"
            int r5 = r1.getColumnIndexOrThrow(r15)
            int r5 = r1.getInt(r5)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r5)
            java.lang.String r5 = "imdn_message_id"
            int r5 = r1.getColumnIndexOrThrow(r5)
            java.lang.String r5 = r1.getString(r5)
            java.lang.String r6 = "status"
            int r6 = r1.getColumnIndexOrThrow(r6)
            int r6 = r1.getInt(r6)
            java.lang.String r9 = "ft_status"
            int r9 = r1.getColumnIndexOrThrow(r9)
            int r1 = r1.getInt(r9)
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r9 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.CANCELLATION
            int r3 = r9.getId()
            if (r6 == r3) goto L_0x0082
            int r3 = r9.getId()
            if (r1 != r3) goto L_0x007f
            goto L_0x0082
        L_0x007f:
            r16 = r4
            goto L_0x0084
        L_0x0082:
            r16 = 0
        L_0x0084:
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$RevocationStatus r1 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.RevocationStatus.NONE
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r3 = r0.mBufferDbQuery
            android.database.Cursor r3 = r3.queryRcsDBMessageUsingImdnId(r5)
            if (r3 == 0) goto L_0x00cb
            boolean r4 = r3.moveToNext()     // Catch:{ all -> 0x00bf }
            if (r4 == 0) goto L_0x00cb
            java.lang.String r4 = r0.TAG     // Catch:{ all -> 0x00bf }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00bf }
            r5.<init>()     // Catch:{ all -> 0x00bf }
            java.lang.String r6 = "RevokeStatus: "
            r5.append(r6)     // Catch:{ all -> 0x00bf }
            int r6 = r1.getId()     // Catch:{ all -> 0x00bf }
            r5.append(r6)     // Catch:{ all -> 0x00bf }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x00bf }
            com.sec.internal.log.IMSLog.i(r4, r5)     // Catch:{ all -> 0x00bf }
            java.lang.String r4 = "revocation_status"
            int r4 = r3.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x00bf }
            int r4 = r3.getInt(r4)     // Catch:{ all -> 0x00bf }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$RevocationStatus r4 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.RevocationStatus.fromId(r4)     // Catch:{ all -> 0x00bf }
            r9 = r4
            goto L_0x00cc
        L_0x00bf:
            r0 = move-exception
            r1 = r0
            r3.close()     // Catch:{ all -> 0x00c5 }
            goto L_0x00ca
        L_0x00c5:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)
        L_0x00ca:
            throw r1
        L_0x00cb:
            r9 = r1
        L_0x00cc:
            if (r3 == 0) goto L_0x00d1
            r3.close()
        L_0x00d1:
            android.content.ContentValues r5 = new android.content.ContentValues
            r5.<init>()
            r3 = r23
            long r3 = r3.lastModSeq
            java.lang.Long r3 = java.lang.Long.valueOf(r3)
            java.lang.String r4 = "lastmodseq"
            r5.put(r4, r3)
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r3 = r0.mScheduleRule
            int r4 = r0.mDbTableContractIndex
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r17 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete
            r18 = r14
            r14 = r5
            r5 = r10
            r19 = r12
            r12 = r9
            r9 = r17
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r3 = r3.getSetFlagsForCldOperation(r4, r5, r7, r8, r9)
            boolean r4 = r0.isCmsEnabled
            if (r4 == 0) goto L_0x011c
            if (r12 == r1) goto L_0x011c
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done
            int r1 = r1.getId()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r14.put(r15, r1)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted
            int r1 = r1.getId()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r14.put(r2, r1)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r0.mBufferDbQuery
            r0.updateQueryTable(r14, r10, r1)
            goto L_0x0165
        L_0x011c:
            boolean r1 = r3.mIsChanged
            if (r1 == 0) goto L_0x013a
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = r3.mDirection
            int r1 = r1.getId()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r14.put(r15, r1)
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = r3.mAction
            int r1 = r1.getId()
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            r14.put(r2, r1)
        L_0x013a:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r0.mBufferDbQuery
            r0.updateQueryTable(r14, r10, r1)
            boolean r1 = r3.mIsChanged
            if (r1 == 0) goto L_0x014c
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r0.mBufferDbQuery
            r4 = r19
            int r2 = (int) r4
            r1.deleteRCSMessageDb(r2)
            goto L_0x014e
        L_0x014c:
            r4 = r19
        L_0x014e:
            r1 = 0
            int r1 = (r4 > r1 ? 1 : (r4 == r1 ? 0 : -1))
            if (r1 <= 0) goto L_0x0165
            r5 = 1
            r9 = 0
            r12 = 0
            r1 = r21
            r2 = r3
            r3 = r10
            r6 = r16
            r7 = r24
            r8 = r18
            r10 = r12
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9, r10)
        L_0x0165:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsSchedulerHelper.onNmsEventDeletedObjBufferDbRcsAvailable(android.database.Cursor, com.sec.internal.omanetapi.nms.data.DeletedObject, boolean):void");
    }

    public void addRcsDownloadListForUri(BufferDBChangeParamList bufferDBChangeParamList, long j, String str, boolean z) {
        Cursor queryTablewithBufferDbId = this.mBufferDbQuery.queryTablewithBufferDbId(1, j);
        if (queryTablewithBufferDbId != null) {
            try {
                if (queryTablewithBufferDbId.moveToFirst()) {
                    String str2 = this.TAG;
                    Log.i(str2, "addRcsDownloadListForUri " + queryTablewithBufferDbId.getCount());
                    do {
                        long j2 = queryTablewithBufferDbId.getLong(queryTablewithBufferDbId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                        if (!queryTablewithBufferDbId.getString(queryTablewithBufferDbId.getColumnIndexOrThrow("content_type")).toLowerCase().contains("botmessage")) {
                            bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(6, j2, z, str, this.mStoreClient));
                        }
                    } while (queryTablewithBufferDbId.moveToNext());
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

    /* access modifiers changed from: protected */
    public byte[] getFileContentInBytes(String str, CloudMessageBufferDBConstants.PayloadEncoding payloadEncoding) {
        FileInputStream fileInputStream;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                fileInputStream = new FileInputStream(str);
                byte[] bArr = new byte[256];
                int read = fileInputStream.read(bArr);
                while (read >= 0) {
                    byteArrayOutputStream.write(bArr, 0, read);
                    read = fileInputStream.read(bArr);
                }
                String str2 = this.TAG;
                Log.i(str2, "getFileContentInBytes: " + str + " " + payloadEncoding + " bytes " + read + " getRcsFilePayloadFromPath, all bytes: " + byteArrayOutputStream.size());
                if (CloudMessageBufferDBConstants.PayloadEncoding.Base64.equals(payloadEncoding)) {
                    byte[] encode = Base64.encode(byteArrayOutputStream.toByteArray(), 0);
                    fileInputStream.close();
                    byteArrayOutputStream.close();
                    return encode;
                }
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                fileInputStream.close();
                byteArrayOutputStream.close();
                return byteArray;
            } catch (Throwable th) {
                byteArrayOutputStream.close();
                throw th;
            }
            throw th;
        } catch (IOException e) {
            String str3 = this.TAG;
            Log.e(str3, "getFileContentInBytes :: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
    }

    private Pair<Long, byte[]> saveOrCopyFileToAppUri(String str, ParamOMAresponseforBufDB paramOMAresponseforBufDB, String str2, int i, boolean z) {
        byte[] decode;
        long j = 0;
        byte[] bArr = null;
        if (!z) {
            try {
                if (CloudMessageBufferDBConstants.PayloadEncoding.None.getId() == i) {
                    decode = paramOMAresponseforBufDB.getData();
                    if (decode != null) {
                        try {
                            j = (long) decode.length;
                        } catch (IOException e) {
                            e = e;
                            bArr = decode;
                            e.printStackTrace();
                            return new Pair<>(Long.valueOf(j), bArr);
                        }
                    }
                    Util.saveFileToAppUri(this.mContext, decode, str2);
                } else if (CloudMessageBufferDBConstants.PayloadEncoding.Base64.getId() == i) {
                    decode = Base64.decode(paramOMAresponseforBufDB.getData(), 0);
                    if (decode != null) {
                        j = (long) decode.length;
                    }
                    Util.saveFileToAppUri(this.mContext, decode, str2);
                }
                bArr = decode;
            } catch (IOException e2) {
                e = e2;
                e.printStackTrace();
                return new Pair<>(Long.valueOf(j), bArr);
            }
        } else if (CloudMessageBufferDBConstants.PayloadEncoding.None.getId() == i) {
            j = FileUtils.copyFile(this.mContext, str, Uri.parse(str2));
        } else if (CloudMessageBufferDBConstants.PayloadEncoding.Base64.getId() == i) {
            j = FileUtils.copyFile(this.mContext, str, Uri.parse(str2));
        }
        return new Pair<>(Long.valueOf(j), bArr);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v12, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v3, resolved type: byte[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean saveToAppUriOnRcsPayloadDownloaded(android.database.Cursor r17, com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r18, android.content.ContentValues r19, boolean r20) {
        /*
            r16 = this;
            r6 = r16
            r0 = r17
            r7 = r19
            java.lang.String r1 = "content_uri"
            int r1 = r0.getColumnIndexOrThrow(r1)
            java.lang.String r8 = r0.getString(r1)
            java.lang.String r1 = "thumbnail_uri"
            int r1 = r0.getColumnIndexOrThrow(r1)
            java.lang.String r9 = r0.getString(r1)
            java.lang.String r1 = "content_type"
            int r1 = r0.getColumnIndexOrThrow(r1)
            java.lang.String r10 = r0.getString(r1)
            java.lang.String r1 = "payloadencoding"
            int r1 = r0.getColumnIndexOrThrow(r1)
            int r11 = r0.getInt(r1)
            java.lang.String r12 = "thumbnail_path"
            int r1 = r0.getColumnIndexOrThrow(r12)
            java.lang.String r1 = r0.getString(r1)
            r13 = 0
            if (r20 == 0) goto L_0x0043
            java.lang.String r2 = r18.getFilePath()
            r14 = r2
            goto L_0x0044
        L_0x0043:
            r14 = r13
        L_0x0044:
            java.lang.String r2 = r6.TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "large-file case, filePath: "
            r3.append(r4)
            r3.append(r14)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r2, r3)
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r2 = r18.getBufferDBChangeParam()
            boolean r2 = r2.mIsFTThumbnail
            java.lang.String r15 = " encoding method: "
            r5 = 0
            r4 = 1
            if (r2 == 0) goto L_0x00c0
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 == 0) goto L_0x00c0
            java.lang.String r0 = r6.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "app generated thumbnail uri: "
            r1.append(r2)
            r1.append(r9)
            r1.append(r15)
            r1.append(r11)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            if (r9 == 0) goto L_0x009e
            r0 = r16
            r1 = r14
            r2 = r18
            r3 = r9
            r8 = r4
            r4 = r11
            r11 = r5
            r5 = r20
            android.util.Pair r0 = r0.saveOrCopyFileToAppUri(r1, r2, r3, r4, r5)
            java.lang.Object r0 = r0.second
            byte[] r0 = (byte[]) r0
            goto L_0x00a8
        L_0x009e:
            r8 = r4
            r11 = r5
            java.lang.String r0 = r6.TAG
            java.lang.String r1 = "file copy to APP failed. thumbUri=null"
            android.util.Log.e(r0, r1)
            r0 = r13
        L_0x00a8:
            r7.put(r12, r9)
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r1 = r18.getBufferDBChangeParam()
            r1.mIsFTThumbnail = r11
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r1 = r18.getBufferDBChangeParam()
            r1.mFTThumbnailFileName = r13
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r1 = r18.getBufferDBChangeParam()
            r1.mPayloadThumbnailUrl = r13
            r5 = r8
            goto L_0x017c
        L_0x00c0:
            r9 = r4
            r1 = r5
            java.lang.String r2 = "is_filetransfer"
            int r2 = r0.getColumnIndexOrThrow(r2)
            int r0 = r0.getInt(r2)
            if (r0 == r9) goto L_0x00d0
            r5 = r9
            goto L_0x00d1
        L_0x00d0:
            r5 = r1
        L_0x00d1:
            if (r5 == 0) goto L_0x0119
            java.lang.String r0 = "json"
            boolean r0 = r10.endsWith(r0)
            if (r0 != 0) goto L_0x00ed
            java.lang.String r0 = "text/plain"
            boolean r0 = r10.contains(r0)
            if (r0 != 0) goto L_0x00ed
            java.lang.String r0 = "xml"
            boolean r0 = r10.endsWith(r0)
            if (r0 == 0) goto L_0x0119
        L_0x00ed:
            java.lang.String r0 = ""
            if (r20 == 0) goto L_0x0101
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$PayloadEncoding r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.PayloadEncoding.None
            byte[] r13 = r6.getFileContentInBytes(r14, r2)
            if (r13 == 0) goto L_0x0112
            java.lang.String r0 = new java.lang.String
            java.nio.charset.Charset r2 = java.nio.charset.StandardCharsets.UTF_8
            r0.<init>(r13, r2)
            goto L_0x0112
        L_0x0101:
            byte[] r2 = r18.getData()
            if (r2 == 0) goto L_0x0112
            java.lang.String r0 = new java.lang.String
            byte[] r2 = r18.getData()
            java.nio.charset.Charset r3 = java.nio.charset.StandardCharsets.UTF_8
            r0.<init>(r2, r3)
        L_0x0112:
            java.lang.String r2 = "body"
            r7.put(r2, r0)
            r5 = r1
            goto L_0x017b
        L_0x0119:
            if (r8 == 0) goto L_0x0136
            r0 = r16
            r1 = r14
            r2 = r18
            r3 = r8
            r4 = r11
            r5 = r20
            android.util.Pair r0 = r0.saveOrCopyFileToAppUri(r1, r2, r3, r4, r5)
            java.lang.Object r1 = r0.first
            java.lang.Long r1 = (java.lang.Long) r1
            long r1 = r1.longValue()
            java.lang.Object r0 = r0.second
            r13 = r0
            byte[] r13 = (byte[]) r13
            goto L_0x013f
        L_0x0136:
            java.lang.String r0 = r6.TAG
            java.lang.String r1 = "file copy to APP failed. fileUri=null"
            android.util.Log.e(r0, r1)
            r1 = 0
        L_0x013f:
            java.lang.String r0 = r6.TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "app generated file uri: "
            r3.append(r4)
            r3.append(r8)
            r3.append(r15)
            r3.append(r11)
            java.lang.String r4 = " size:"
            r3.append(r4)
            r3.append(r1)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r0, r3)
            java.lang.String r0 = "file_size"
            java.lang.Long r3 = java.lang.Long.valueOf(r1)
            r7.put(r0, r3)
            java.lang.String r0 = "bytes_transf"
            java.lang.Long r1 = java.lang.Long.valueOf(r1)
            r7.put(r0, r1)
            java.lang.String r0 = "file_path"
            r7.put(r0, r8)
            r5 = r9
        L_0x017b:
            r0 = r13
        L_0x017c:
            if (r20 == 0) goto L_0x0182
            com.sec.internal.helper.FileUtils.removeFile(r14)
            goto L_0x01a3
        L_0x0182:
            if (r10 == 0) goto L_0x01a3
            java.lang.String r1 = "application/vnd.gsma.rcspushlocation+xml"
            boolean r1 = r10.equalsIgnoreCase(r1)
            if (r1 == 0) goto L_0x01a3
            if (r0 == 0) goto L_0x01a3
            com.sec.internal.ims.servicemodules.gls.GlsXmlParser r1 = new com.sec.internal.ims.servicemodules.gls.GlsXmlParser
            r1.<init>()
            java.lang.String r2 = new java.lang.String
            java.nio.charset.Charset r3 = java.nio.charset.StandardCharsets.UTF_8
            r2.<init>(r0, r3)
            java.lang.String r0 = r1.getGlsExtInfo(r2)
            java.lang.String r1 = "ext_info"
            r7.put(r1, r0)
        L_0x01a3:
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsSchedulerHelper.saveToAppUriOnRcsPayloadDownloaded(android.database.Cursor, com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB, android.content.ContentValues, boolean):boolean");
    }

    public void onRcsPayloadDownloaded(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z) {
        Throwable th;
        boolean z2;
        Cursor queryTablewithBufferDbId = this.mBufferDbQuery.queryTablewithBufferDbId(paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
        if (queryTablewithBufferDbId != null) {
            try {
                if (queryTablewithBufferDbId.moveToFirst()) {
                    int i = queryTablewithBufferDbId.getInt(queryTablewithBufferDbId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    int i2 = queryTablewithBufferDbId.getInt(queryTablewithBufferDbId.getColumnIndexOrThrow("_id"));
                    String string = queryTablewithBufferDbId.getString(queryTablewithBufferDbId.getColumnIndexOrThrow("linenum"));
                    boolean z3 = queryTablewithBufferDbId.getInt(queryTablewithBufferDbId.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER)) == 1;
                    int i3 = queryTablewithBufferDbId.getInt(queryTablewithBufferDbId.getColumnIndexOrThrow("status"));
                    if (!this.isCmsEnabled || i3 != ImConstants.Status.CANCELLATION.getId()) {
                        ContentValues contentValues = new ContentValues();
                        if (!this.isCmsEnabled || paramOMAresponseforBufDB.getPayloadUrl() == null || !CmsUtil.urlContainsLargeFile(this.mStoreClient, paramOMAresponseforBufDB.getPayloadUrl())) {
                            z2 = saveToAppUriOnRcsPayloadDownloaded(queryTablewithBufferDbId, paramOMAresponseforBufDB, contentValues, false);
                        } else {
                            z2 = saveToAppUriOnRcsPayloadDownloaded(queryTablewithBufferDbId, paramOMAresponseforBufDB, contentValues, true);
                        }
                        if (!z2) {
                            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()));
                            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
                        } else {
                            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()));
                            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.UpdatePayload.getId()));
                        }
                        updateQueryTable(contentValues, (long) i, this.mBufferDbQuery);
                        if (i2 > 0) {
                            this.mBufferDbQuery.updateRCSMessageDb(i2, contentValues);
                        }
                        handleOutPutParamSyncFlagSet(new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice, !z2 ? CloudMessageBufferDBConstants.ActionStatusFlag.Insert : CloudMessageBufferDBConstants.ActionStatusFlag.UpdatePayload), paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId, 1, z3, z, string, (BufferDBChangeParamList) null, false);
                    } else {
                        Log.d(this.TAG, "Message is cancelled:");
                        queryTablewithBufferDbId.close();
                        return;
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

    private void setInlineTextCV(String str, ContentValues contentValues) {
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        contentValues.put(ImContract.ChatItem.IS_FILE_TRANSFER, 0);
        contentValues.put("content_type", MIMEContentType.PLAIN_TEXT);
        contentValues.put("body", str);
    }

    private void getPayloadCV(String str, String str2, String str3, long j, ContentValues contentValues) {
        contentValues.put(ImContract.CsSession.FILE_NAME, str);
        contentValues.put(ImContract.CsSession.FILE_SIZE, Long.valueOf(j));
        contentValues.put(ImContract.CsSession.BYTES_TRANSFERRED, Long.valueOf(j));
        contentValues.put(ImContract.CsSession.FILE_PATH, str2);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
    }

    /* JADX WARNING: Removed duplicated region for block: B:131:0x0314 A[SYNTHETIC, Splitter:B:131:0x0314] */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x0318 A[Catch:{ all -> 0x034e }] */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x032b A[Catch:{ all -> 0x034e }] */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x0365 A[Catch:{ all -> 0x035c, all -> 0x0369 }] */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x036e A[SYNTHETIC, Splitter:B:158:0x036e] */
    /* JADX WARNING: Removed duplicated region for block: B:165:0x037d A[SYNTHETIC, Splitter:B:165:0x037d] */
    /* JADX WARNING: Removed duplicated region for block: B:172:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x0295 A[Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x02a0 A[SYNTHETIC, Splitter:B:95:0x02a0] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onRcsAllPayloadsDownloaded(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r21, boolean r22) {
        /*
            r20 = this;
            r8 = r20
            java.lang.String r1 = ""
            java.lang.String r2 = ";"
            java.lang.String r3 = "onRcsAllPayloadsDownloaded: "
            java.lang.String r4 = "content_type"
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r6 = r8.mBufferDbQuery     // Catch:{ all -> 0x0378 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r7 = r21.getBufferDBChangeParam()     // Catch:{ all -> 0x0378 }
            int r7 = r7.mDBIndex     // Catch:{ all -> 0x0378 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r9 = r21.getBufferDBChangeParam()     // Catch:{ all -> 0x0378 }
            long r9 = r9.mRowId     // Catch:{ all -> 0x0378 }
            android.database.Cursor r11 = r6.queryTablewithBufferDbId(r7, r9)     // Catch:{ all -> 0x0378 }
            if (r11 == 0) goto L_0x0362
            boolean r6 = r11.moveToFirst()     // Catch:{ all -> 0x0355 }
            if (r6 == 0) goto L_0x0362
            java.lang.String r6 = "content_uri"
            int r6 = r11.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x0355 }
            java.lang.String r6 = r11.getString(r6)     // Catch:{ all -> 0x0355 }
            java.lang.String r7 = "_id"
            int r7 = r11.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x0355 }
            int r9 = r11.getInt(r7)     // Catch:{ all -> 0x0355 }
            java.lang.String r7 = "linenum"
            int r7 = r11.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x0355 }
            java.lang.String r10 = r11.getString(r7)     // Catch:{ all -> 0x0355 }
            java.lang.String r7 = "file_name"
            int r7 = r11.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x0355 }
            java.lang.String r7 = r11.getString(r7)     // Catch:{ all -> 0x0355 }
            java.lang.String r12 = "thumbnail_path"
            int r12 = r11.getColumnIndexOrThrow(r12)     // Catch:{ all -> 0x0355 }
            java.lang.String r12 = r11.getString(r12)     // Catch:{ all -> 0x0355 }
            int r13 = r11.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x0355 }
            java.lang.String r13 = r11.getString(r13)     // Catch:{ all -> 0x0355 }
            android.content.ContentValues r14 = new android.content.ContentValues     // Catch:{ all -> 0x0355 }
            r14.<init>()     // Catch:{ all -> 0x0355 }
            r16 = 0
            java.lang.String r5 = r8.TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0304 }
            java.lang.StringBuilder r15 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0304 }
            r15.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0304 }
            r18 = r10
            java.lang.String r10 = "multipart payloads, size: "
            r15.append(r10)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            java.util.List r10 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            int r10 = r10.size()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            r15.append(r10)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            java.lang.String r10 = r15.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            android.util.Log.i(r5, r10)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            java.util.List r5 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            int r5 = r5.size()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            r10 = 1
            if (r5 <= r10) goto L_0x00f5
            java.lang.String r2 = "multipart/related"
            r14.put(r4, r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            javax.mail.internet.MimeMultipart r2 = new javax.mail.internet.MimeMultipart     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            r2.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            r3 = 0
        L_0x009c:
            java.util.List r4 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            int r4 = r4.size()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            if (r3 >= r4) goto L_0x00c7
            java.util.List r4 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            java.lang.Object r4 = r4.get(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            javax.mail.BodyPart r4 = (javax.mail.BodyPart) r4     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            r2.addBodyPart(r4)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            java.util.List r4 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            java.lang.Object r4 = r4.get(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            javax.mail.BodyPart r4 = (javax.mail.BodyPart) r4     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            int r4 = r4.getSize()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            long r4 = (long) r4     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            long r16 = r16 + r4
            int r3 = r3 + 1
            goto L_0x009c
        L_0x00c7:
            java.lang.String r7 = com.sec.internal.ims.cmstore.utils.Util.getRandomFileName(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            android.content.Context r3 = r8.mContext     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            r4 = 0
            java.lang.String r3 = com.sec.internal.ims.cmstore.utils.Util.generateUniqueFilePath(r3, r7, r4)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            java.lang.String r4 = r8.TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            r5.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            java.lang.String r10 = "generated file path: "
            r5.append(r10)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            r5.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            java.lang.String r5 = r5.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            android.util.Log.i(r4, r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            com.sec.internal.ims.cmstore.utils.Util.saveMimeBodyToPath(r2, r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00ef }
            r5 = 0
        L_0x00ec:
            r15 = 0
            goto L_0x0310
        L_0x00ef:
            r0 = move-exception
            r2 = r0
        L_0x00f1:
            r5 = 0
        L_0x00f2:
            r15 = 0
            goto L_0x030d
        L_0x00f5:
            java.util.List r5 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            int r5 = r5.size()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            if (r5 != r10) goto L_0x02fa
            java.lang.String r5 = r8.TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            java.lang.StringBuilder r15 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            r15.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            r15.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            java.util.List r10 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0300 }
            r19 = r1
            r1 = 0
            java.lang.Object r10 = r10.get(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f3 }
            javax.mail.BodyPart r10 = (javax.mail.BodyPart) r10     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            java.lang.String r1 = r10.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            r15.append(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            java.lang.String r1 = r15.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            android.util.Log.i(r5, r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            java.util.List r1 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            r5 = 0
            java.lang.Object r1 = r1.get(r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02ed }
            javax.mail.BodyPart r1 = (javax.mail.BodyPart) r1     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            java.lang.String r1 = r1.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            java.lang.String r5 = "text/plain"
            boolean r1 = r1.contains(r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            if (r1 == 0) goto L_0x0178
            java.util.List r1 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0170 }
            r2 = 0
            java.lang.Object r1 = r1.get(r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0170 }
            javax.mail.BodyPart r1 = (javax.mail.BodyPart) r1     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0170 }
            java.io.InputStream r5 = r1.getInputStream()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0170 }
            java.lang.String r1 = com.sec.internal.ims.cmstore.utils.Util.convertStreamToString(r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x016b }
            java.lang.String r2 = r8.TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0168 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0168 }
            r3.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0168 }
            java.lang.String r4 = "converted inlineTxt: "
            r3.append(r4)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0168 }
            r3.append(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0168 }
            java.lang.String r3 = r3.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0168 }
            android.util.Log.i(r2, r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x0168 }
            r15 = 1
            goto L_0x0310
        L_0x0168:
            r0 = move-exception
            r2 = r0
            goto L_0x0175
        L_0x016b:
            r0 = move-exception
            r2 = r0
            r1 = r19
            goto L_0x0175
        L_0x0170:
            r0 = move-exception
            r2 = r0
            r1 = r19
            r5 = 0
        L_0x0175:
            r15 = 1
            goto L_0x030d
        L_0x0178:
            boolean r1 = android.text.TextUtils.isEmpty(r13)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            if (r1 != 0) goto L_0x01be
            java.util.List r1 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r5 = 0
            java.lang.Object r1 = r1.get(r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            javax.mail.BodyPart r1 = (javax.mail.BodyPart) r1     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String r1 = r1.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            boolean r1 = r1.contains(r13)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            if (r1 == 0) goto L_0x01be
            java.lang.String r1 = r8.TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r2.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r2.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.util.List r3 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r4 = 0
            java.lang.Object r3 = r3.get(r4)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            javax.mail.BodyPart r3 = (javax.mail.BodyPart) r3     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String r3 = r3.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r2.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String r2 = r2.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            android.util.Log.d(r1, r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            goto L_0x028f
        L_0x01b8:
            r0 = move-exception
            r2 = r0
            r1 = r19
            goto L_0x00f1
        L_0x01be:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r8.mBufferDbQuery     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            java.util.List r5 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            r10 = 0
            java.lang.Object r5 = r5.get(r10)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02ea }
            javax.mail.BodyPart r5 = (javax.mail.BodyPart) r5     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            java.lang.String r5 = r5.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            boolean r1 = r1.isContentTypeDefined(r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            if (r1 == 0) goto L_0x020c
            java.lang.String r1 = r8.TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r2.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r2.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.util.List r3 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r5 = 0
            java.lang.Object r3 = r3.get(r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            javax.mail.BodyPart r3 = (javax.mail.BodyPart) r3     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String r3 = r3.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r2.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String r2 = r2.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            android.util.Log.d(r1, r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.util.List r1 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r2 = 0
            java.lang.Object r1 = r1.get(r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            javax.mail.BodyPart r1 = (javax.mail.BodyPart) r1     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String r1 = r1.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r14.put(r4, r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            goto L_0x028f
        L_0x020c:
            java.util.List r1 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            r5 = 0
            java.lang.Object r1 = r1.get(r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02ed }
            javax.mail.BodyPart r1 = (javax.mail.BodyPart) r1     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02ed }
            java.lang.String r1 = r1.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02ed }
            if (r1 == 0) goto L_0x02bf
            java.util.List r1 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.Object r1 = r1.get(r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            javax.mail.BodyPart r1 = (javax.mail.BodyPart) r1     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String r1 = r1.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String[] r1 = r1.split(r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            if (r1 == 0) goto L_0x02bf
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r8.mBufferDbQuery     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.util.List r5 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r10 = 0
            java.lang.Object r5 = r5.get(r10)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            javax.mail.BodyPart r5 = (javax.mail.BodyPart) r5     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String r5 = r5.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String[] r5 = r5.split(r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r5 = r5[r10]     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            boolean r1 = r1.isContentTypeDefined(r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            if (r1 == 0) goto L_0x02bf
            java.lang.String r1 = r8.TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r5.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r5.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.util.List r3 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r10 = 0
            java.lang.Object r3 = r3.get(r10)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            javax.mail.BodyPart r3 = (javax.mail.BodyPart) r3     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String r3 = r3.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String[] r3 = r3.split(r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r3 = r3[r10]     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r5.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String r3 = r5.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            android.util.Log.d(r1, r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.util.List r1 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r3 = 0
            java.lang.Object r1 = r1.get(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            javax.mail.BodyPart r1 = (javax.mail.BodyPart) r1     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String r1 = r1.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String[] r1 = r1.split(r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r1 = r1[r3]     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r14.put(r4, r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
        L_0x028f:
            boolean r1 = android.text.TextUtils.isEmpty(r7)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            if (r1 == 0) goto L_0x02a0
            java.lang.String r1 = r8.TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.lang.String r2 = "onRcsAllPayloadsDownloaded: no file name"
            android.util.Log.e(r1, r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r11.close()     // Catch:{ all -> 0x0378 }
            return
        L_0x02a0:
            java.util.List r1 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            r2 = 0
            java.lang.Object r1 = r1.get(r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            javax.mail.BodyPart r1 = (javax.mail.BodyPart) r1     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            java.io.InputStream r5 = r1.getInputStream()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x01b8 }
            android.content.Context r1 = r8.mContext     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02b9 }
            long r16 = com.sec.internal.ims.cmstore.utils.Util.saveInputStreamtoAppUri(r1, r5, r6)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02b9 }
            r1 = r19
            goto L_0x00ec
        L_0x02b9:
            r0 = move-exception
            r2 = r0
            r1 = r19
            goto L_0x00f2
        L_0x02bf:
            java.lang.String r1 = r8.TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            r2.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            java.lang.String r3 = "onRcsAllPayloadsDownloaded invalid file type for RCS: "
            r2.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            java.util.List r3 = r21.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02f0 }
            r4 = 0
            java.lang.Object r3 = r3.get(r4)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02e8 }
            javax.mail.BodyPart r3 = (javax.mail.BodyPart) r3     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02e8 }
            java.lang.String r3 = r3.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02e8 }
            r2.append(r3)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02e8 }
            java.lang.String r2 = r2.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02e8 }
            android.util.Log.d(r1, r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x02e8 }
            r11.close()     // Catch:{ all -> 0x0378 }
            return
        L_0x02e8:
            r0 = move-exception
            goto L_0x02f5
        L_0x02ea:
            r0 = move-exception
            r4 = r10
            goto L_0x02f5
        L_0x02ed:
            r0 = move-exception
            r4 = r5
            goto L_0x02f5
        L_0x02f0:
            r0 = move-exception
            r4 = 0
            goto L_0x02f5
        L_0x02f3:
            r0 = move-exception
            r4 = r1
        L_0x02f5:
            r2 = r0
            r15 = r4
            r1 = r19
            goto L_0x030c
        L_0x02fa:
            r19 = r1
            r4 = 0
            r15 = r4
            r5 = 0
            goto L_0x0310
        L_0x0300:
            r0 = move-exception
            r19 = r1
            goto L_0x0309
        L_0x0304:
            r0 = move-exception
            r19 = r1
            r18 = r10
        L_0x0309:
            r4 = 0
            r2 = r0
            r15 = r4
        L_0x030c:
            r5 = 0
        L_0x030d:
            r2.printStackTrace()     // Catch:{ all -> 0x0352 }
        L_0x0310:
            r13 = r5
            r2 = r7
            if (r15 == 0) goto L_0x0318
            r8.setInlineTextCV(r1, r14)     // Catch:{ all -> 0x034e }
            goto L_0x0322
        L_0x0318:
            r1 = r20
            r3 = r6
            r4 = r12
            r5 = r16
            r7 = r14
            r1.getPayloadCV(r2, r3, r4, r5, r7)     // Catch:{ all -> 0x034e }
        L_0x0322:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r8.mBufferDbQuery     // Catch:{ all -> 0x034e }
            r2 = 1
            r8.updateQueryTable(r14, r2, r1)     // Catch:{ all -> 0x034e }
            if (r9 <= 0) goto L_0x0330
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r8.mBufferDbQuery     // Catch:{ all -> 0x034e }
            r1.updateRCSMessageDb(r9, r14)     // Catch:{ all -> 0x034e }
        L_0x0330:
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r2 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x034e }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x034e }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x034e }
            r2.<init>(r1, r3)     // Catch:{ all -> 0x034e }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r1 = r21.getBufferDBChangeParam()     // Catch:{ all -> 0x034e }
            long r3 = r1.mRowId     // Catch:{ all -> 0x034e }
            r5 = 1
            r6 = 0
            r9 = 0
            r10 = 0
            r1 = r20
            r7 = r22
            r8 = r18
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9, r10)     // Catch:{ all -> 0x034e }
            r5 = r13
            goto L_0x0363
        L_0x034e:
            r0 = move-exception
            r1 = r0
            r5 = r13
            goto L_0x0358
        L_0x0352:
            r0 = move-exception
            r1 = r0
            goto L_0x0358
        L_0x0355:
            r0 = move-exception
            r1 = r0
            r5 = 0
        L_0x0358:
            r11.close()     // Catch:{ all -> 0x035c }
            goto L_0x0361
        L_0x035c:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ all -> 0x0369 }
        L_0x0361:
            throw r1     // Catch:{ all -> 0x0369 }
        L_0x0362:
            r5 = 0
        L_0x0363:
            if (r11 == 0) goto L_0x036c
            r11.close()     // Catch:{ all -> 0x0369 }
            goto L_0x036c
        L_0x0369:
            r0 = move-exception
            r1 = r0
            goto L_0x037b
        L_0x036c:
            if (r5 == 0) goto L_0x0377
            r5.close()     // Catch:{ IOException -> 0x0372 }
            goto L_0x0377
        L_0x0372:
            r0 = move-exception
            r1 = r0
            r1.printStackTrace()
        L_0x0377:
            return
        L_0x0378:
            r0 = move-exception
            r1 = r0
            r5 = 0
        L_0x037b:
            if (r5 == 0) goto L_0x0386
            r5.close()     // Catch:{ IOException -> 0x0381 }
            goto L_0x0386
        L_0x0381:
            r0 = move-exception
            r2 = r0
            r2.printStackTrace()
        L_0x0386:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsSchedulerHelper.onRcsAllPayloadsDownloaded(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB, boolean):void");
    }

    /* access modifiers changed from: protected */
    public void onDownloadRequestFromApp(DeviceIMFTUpdateParam deviceIMFTUpdateParam) {
        Throwable th;
        DeviceIMFTUpdateParam deviceIMFTUpdateParam2 = deviceIMFTUpdateParam;
        String str = this.TAG;
        Log.i(str, "onDownloadRequestFromApp: " + deviceIMFTUpdateParam2);
        if (deviceIMFTUpdateParam2.mTableindex == 1) {
            Cursor searchIMFTBufferUsingImdn = this.mBufferDbQuery.searchIMFTBufferUsingImdn(String.valueOf(deviceIMFTUpdateParam2.mImdnId), deviceIMFTUpdateParam2.mLine);
            if (searchIMFTBufferUsingImdn != null) {
                if (searchIMFTBufferUsingImdn.moveToFirst()) {
                    boolean z = searchIMFTBufferUsingImdn.getInt(searchIMFTBufferUsingImdn.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER)) == 1;
                    String string = searchIMFTBufferUsingImdn.getString(searchIMFTBufferUsingImdn.getColumnIndexOrThrow(ImContract.CsSession.FILE_PATH));
                    int i = searchIMFTBufferUsingImdn.getInt(searchIMFTBufferUsingImdn.getColumnIndexOrThrow(ImContract.CsSession.BYTES_TRANSFERRED));
                    long j = searchIMFTBufferUsingImdn.getLong(searchIMFTBufferUsingImdn.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    if (!this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isSupportExpiredRule() || !z || TextUtils.isEmpty(string) || i <= 0) {
                        try {
                            CloudMessageBufferDBConstants.ActionStatusFlag valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(searchIMFTBufferUsingImdn.getInt(searchIMFTBufferUsingImdn.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
                            CloudMessageBufferDBConstants.DirectionFlag valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(searchIMFTBufferUsingImdn.getInt(searchIMFTBufferUsingImdn.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
                            if (CloudMessageBufferDBConstants.ActionStatusFlag.None.equals(valueOf) || CloudMessageBufferDBConstants.DirectionFlag.Done.equals(valueOf2)) {
                                ContentValues contentValues = new ContentValues();
                                long ftRowFromTelephonyDb = this.mTelephonyStorage.getFtRowFromTelephonyDb(deviceIMFTUpdateParam2.mImdnId);
                                if (ftRowFromTelephonyDb == -1) {
                                    String str2 = this.TAG;
                                    Log.e(str2, "para.mImdnId not present in DB " + deviceIMFTUpdateParam2.mImdnId);
                                    searchIMFTBufferUsingImdn.close();
                                    return;
                                }
                                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.FILE_URI, "content://im/ft_original/" + ftRowFromTelephonyDb);
                                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.THUMBNAIL_URI, "content://im/ft_thumbnail/" + ftRowFromTelephonyDb);
                                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
                                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
                                updateQueryTable(contentValues, j, this.mBufferDbQuery);
                                BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
                                BufferDBChangeParam bufferDBChangeParam = new BufferDBChangeParam(deviceIMFTUpdateParam2.mTableindex, j, false, deviceIMFTUpdateParam2.mLine, this.mStoreClient);
                                bufferDBChangeParam.mIsDownloadRequestFromApp = true;
                                bufferDBChangeParamList.mChangelst.add(bufferDBChangeParam);
                                this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(bufferDBChangeParamList);
                            } else {
                                Log.i(this.TAG, "duplicate download request!");
                                searchIMFTBufferUsingImdn.close();
                                return;
                            }
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    } else {
                        String str3 = this.TAG;
                        Log.i(str3, "file already downloaded, should not have received another download, notify message app directly: " + string);
                        notifyMsgAppCldNotification(getAppTypeString(deviceIMFTUpdateParam2.mTableindex), getMessageTypeString(deviceIMFTUpdateParam2.mTableindex, true), j, false);
                        searchIMFTBufferUsingImdn.close();
                        return;
                    }
                }
            }
            if (searchIMFTBufferUsingImdn != null) {
                searchIMFTBufferUsingImdn.close();
                return;
            }
            return;
        }
        return;
        throw th;
    }

    /* renamed from: com.sec.internal.ims.cmstore.syncschedulers.RcsSchedulerHelper$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag[] r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag = r0
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsSchedulerHelper.AnonymousClass1.<clinit>():void");
        }
    }

    public void onUpdateFromDeviceSessionPartcpts(DeviceSessionPartcptsUpdateParam deviceSessionPartcptsUpdateParam) {
        String str = this.TAG;
        Log.i(str, "onUpdateFromDeviceSessionPartcpts: " + deviceSessionPartcptsUpdateParam);
        if (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag[deviceSessionPartcptsUpdateParam.mUpdateType.ordinal()] == 1) {
            onNewPartcptsInserted(deviceSessionPartcptsUpdateParam);
        }
    }

    private void onNewPartcptsInserted(DeviceSessionPartcptsUpdateParam deviceSessionPartcptsUpdateParam) {
        Cursor queryParticipantsUsingChatId = this.mBufferDbQuery.queryParticipantsUsingChatId(deviceSessionPartcptsUpdateParam.mChatId);
        if (queryParticipantsUsingChatId != null) {
            try {
                if (queryParticipantsUsingChatId.moveToFirst()) {
                    this.mBufferDbQuery.insertToRCSParticipantsBufferDB(queryParticipantsUsingChatId);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryParticipantsUsingChatId != null) {
            queryParticipantsUsingChatId.close();
            return;
        }
        return;
        throw th;
    }

    public void onUpdateFromDeviceSession(DeviceSessionPartcptsUpdateParam deviceSessionPartcptsUpdateParam) {
        String str = this.TAG;
        Log.i(str, "onUpdateFromDeviceSession: " + deviceSessionPartcptsUpdateParam);
        if (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$ActionStatusFlag[deviceSessionPartcptsUpdateParam.mUpdateType.ordinal()] == 1) {
            onNewSessionInserted(deviceSessionPartcptsUpdateParam);
        }
    }

    private void onNewSessionInserted(DeviceSessionPartcptsUpdateParam deviceSessionPartcptsUpdateParam) {
        Cursor querySessionByChatId = this.mBufferDbQuery.querySessionByChatId(deviceSessionPartcptsUpdateParam.mChatId);
        if (querySessionByChatId != null) {
            try {
                if (querySessionByChatId.moveToFirst()) {
                    Log.d(this.TAG, "session already exists in BufferDb");
                    querySessionByChatId.close();
                    return;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (querySessionByChatId != null) {
            querySessionByChatId.close();
        }
        Cursor querySessionUsingChatId = this.mBufferDbQuery.querySessionUsingChatId(deviceSessionPartcptsUpdateParam.mChatId);
        if (querySessionUsingChatId != null) {
            try {
                if (querySessionUsingChatId.moveToFirst()) {
                    this.mBufferDbQuery.insertSingleSessionToRcsBuffer(querySessionUsingChatId);
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (querySessionUsingChatId != null) {
            querySessionUsingChatId.close();
            return;
        }
        return;
        throw th;
        throw th;
    }

    public void notifyMsgAppFetchBuffer(Cursor cursor, int i) {
        if (i == 1) {
            JsonArray jsonArray = new JsonArray();
            JsonArray jsonArray2 = new JsonArray();
            do {
                int i2 = cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                String string = cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.FILE_PATH));
                String string2 = cursor.getString(cursor.getColumnIndexOrThrow(ImContract.CsSession.THUMBNAIL_PATH));
                if ((string == null || string.length() <= 1) && (string2 == null || string2.length() <= 1)) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("id", String.valueOf(i2));
                    jsonArray.add(jsonObject);
                } else {
                    JsonObject jsonObject2 = new JsonObject();
                    jsonObject2.addProperty("id", String.valueOf(i2));
                    jsonArray2.add(jsonObject2);
                }
                String str = this.TAG;
                Log.d(str, "jsonArrayRowIdsCHAT.size(): " + jsonArray.size() + ",notify message app: CHAT: " + jsonArray.toString() + ", jsonArrayRowIdsFT.size(): " + jsonArray2.size() + "notify message app: FT: " + jsonArray2.toString());
                if (jsonArray.size() == this.mMaxNumMsgsNotifyAppInIntent) {
                    this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.CHAT, jsonArray.toString(), false);
                    jsonArray = new JsonArray();
                }
                if (jsonArray2.size() == this.mMaxNumMsgsNotifyAppInIntent) {
                    this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FT", jsonArray2.toString(), false);
                    jsonArray2 = new JsonArray();
                }
            } while (cursor.moveToNext());
            if (jsonArray.size() > 0) {
                this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.CHAT, jsonArray.toString(), false);
            }
            if (jsonArray2.size() > 0) {
                this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FT", jsonArray2.toString(), false);
            }
        }
    }

    public void notifyMsgAppFetchBuffer(ContentValues contentValues, int i) {
        if (i == 10) {
            String asString = contentValues.getAsString("chat_id");
            String asString2 = contentValues.getAsString("session_uri");
            String asString3 = contentValues.getAsString("conversation_id");
            Integer asInteger = contentValues.getAsInteger("_id");
            if (asInteger != null) {
                JsonArray jsonElements = CmsUtil.getJsonElements(contentValues, asString, asString2, asString3, asInteger.intValue());
                String str = this.TAG;
                Log.i(str, "notifyMsgAppFetchBuffer, chatId : " + asString + ", jsonArrayRowIdsSession: " + jsonElements);
                if (jsonElements.size() > 0) {
                    this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.SESSION, jsonElements.toString(), false);
                }
            }
        }
    }

    public Cursor searchIMFTBufferUsingImdn(String str, String str2) {
        return this.mBufferDbQuery.searchIMFTBufferUsingImdn(str, str2);
    }

    public Cursor queryToDeviceUnDownloadedRcs(String str, int i) {
        return this.mBufferDbQuery.queryToDeviceUnDownloadedRcs(str, i);
    }

    public int queryPendingUrlFetch() {
        Cursor queryMessageBySyncAction = this.mBufferDbQuery.queryMessageBySyncAction(1, CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri.getId());
        if (queryMessageBySyncAction != null) {
            try {
                int count = queryMessageBySyncAction.getCount();
                queryMessageBySyncAction.close();
                return count;
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else if (queryMessageBySyncAction == null) {
            return 0;
        } else {
            queryMessageBySyncAction.close();
            return 0;
        }
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0028  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int queryRCSPDUActionStatus(long r2) {
        /*
            r1 = this;
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r1.mBufferDbQuery
            r0 = 1
            android.database.Cursor r1 = r1.queryTablewithBufferDbId(r0, r2)
            if (r1 == 0) goto L_0x0025
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x001b }
            if (r2 == 0) goto L_0x0025
            java.lang.String r2 = "syncaction"
            int r2 = r1.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x001b }
            int r2 = r1.getInt(r2)     // Catch:{ all -> 0x001b }
            goto L_0x0026
        L_0x001b:
            r2 = move-exception
            r1.close()     // Catch:{ all -> 0x0020 }
            goto L_0x0024
        L_0x0020:
            r1 = move-exception
            r2.addSuppressed(r1)
        L_0x0024:
            throw r2
        L_0x0025:
            r2 = -1
        L_0x0026:
            if (r1 == 0) goto L_0x002b
            r1.close()
        L_0x002b:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsSchedulerHelper.queryRCSPDUActionStatus(long):int");
    }

    public Cursor queryToCloudUnsyncedRcs() {
        return this.mBufferDbQuery.queryToCloudUnsyncedRcs();
    }

    public Cursor queryToDeviceUnsyncedRcs() {
        return this.mBufferDbQuery.queryToDeviceUnsyncedRcs();
    }

    public Cursor queryRCSMessagesToUploadByMessageType(String str) {
        return this.mBufferDbQuery.queryRCSMessagesToUploadByMessageType(str);
    }

    public Cursor queryRCSMessagesToUpload() {
        return this.mBufferDbQuery.queryRCSMessagesToUpload();
    }

    public Cursor queryRCSFtMessagesToUpload(String str) {
        return this.mBufferDbQuery.queryRCSFtMessagesToUpload(str);
    }

    public Cursor queryImdnMessagesToUpload() {
        return this.mBufferDbQuery.queryImdnMessagesToUpload();
    }

    public Cursor queryRCSBufferDBwithResUrl(String str) {
        return this.mBufferDbQuery.queryRCSBufferDBwithResUrl(str);
    }

    public int deleteRCSBufferDBwithResUrl(String str) {
        return this.mBufferDbQuery.deleteRCSBufferDBwithResUrl(str);
    }

    public Cursor queryRCSMessagesBySycnDirection(int i, String str) {
        return this.mBufferDbQuery.queryMessageBySyncDirection(i, str);
    }

    public Cursor queryAllSession() {
        return this.mBufferDbQuery.queryAllSession();
    }

    public Cursor queryGroupSession() {
        return this.mBufferDbQuery.queryGroupSession();
    }

    public Cursor queryOneToOneSession() {
        return this.mBufferDbQuery.queryOneToOneSession();
    }

    public Cursor queryAllSessionWithIMSI(String str) {
        return this.mBufferDbQuery.queryAllSessionWithIMSI(str);
    }

    public Cursor queryAllSessionsFromTelephony(String str) {
        return this.mTelephonyStorage.queryAllSessionsFromTelephony(str);
    }

    public void insertAllSessionToRCSSessionBufferDB(Cursor cursor) {
        this.mBufferDbQuery.insertAllToRCSSessionBufferDB(cursor);
    }

    public void insertSessionFromTPDBToRCSSessionBufferDB(Cursor cursor) {
        this.mBufferDbQuery.insertSessionFromTPDBToRCSSessionBufferDB(cursor);
    }

    public void cleanAllBufferDB() {
        this.mBufferDbQuery.cleanAllBufferDB();
    }

    public void onUpdateFromDeviceFtUriFetch(DeviceMsgAppFetchUriParam deviceMsgAppFetchUriParam) {
        onUpdateFromDeviceFtUriFetch(deviceMsgAppFetchUriParam, this.mBufferDbQuery);
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
        IMSLog.i(this.TAG, "onCloudUploadSuccess ");
        if (paramOMAresponseforBufDB.getReference() != null) {
            handleCloudUploadSuccess(paramOMAresponseforBufDB, z, this.mBufferDbQuery, 1);
        }
    }

    public void notifyMsgAppDeleteFail(int i, long j, String str) {
        String str2 = this.TAG;
        Log.i(str2, "notifyMsgAppDeleteFail, dbIndex: " + i + " bufferDbId: " + j + " line: " + IMSLog.checker(str));
        if (i == 1) {
            this.mCallbackMsgApp.notifyAppCloudDeleteFail(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.CHAT, CmsUtil.getJsonElements(j));
        }
    }

    public void wipeOutData(int i, String str) {
        wipeOutData(i, str, this.mBufferDbQuery);
    }

    public void onRcsChatImdnsDownloaded(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        Cursor queryTablewithBufferDbId = this.mBufferDbQuery.queryTablewithBufferDbId(1, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
        if (queryTablewithBufferDbId != null) {
            try {
                if (queryTablewithBufferDbId.moveToFirst()) {
                    Object object = paramOMAresponseforBufDB.getObject();
                    ContentValues contentValues = new ContentValues();
                    if (!TextUtils.isEmpty(object.imdns.resourceURL)) {
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(object.imdns.resourceURL));
                    }
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
                    updateRCSImdnToBufferDB(object.imdns, contentValues, queryTablewithBufferDbId);
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

    /* JADX WARNING: Removed duplicated region for block: B:89:0x027a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateRCSImdnToBufferDB(com.sec.internal.omanetapi.nms.data.ImdnList r26, android.content.ContentValues r27, android.database.Cursor r28) {
        /*
            r25 = this;
            r0 = r25
            r7 = r26
            r8 = r27
            r1 = r28
            java.lang.String r2 = r0.TAG
            java.lang.String r3 = "updateRCSImdnToBufferDB"
            android.util.Log.i(r2, r3)
            if (r7 == 0) goto L_0x0283
            com.sec.internal.omanetapi.nms.data.ImdnObject[] r2 = r7.imdn
            if (r2 != 0) goto L_0x0018
            goto L_0x0283
        L_0x0018:
            java.lang.String r9 = "status"
            int r2 = r1.getColumnIndexOrThrow(r9)
            int r2 = r1.getInt(r2)
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r3 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.CANCELLATION
            int r3 = r3.getId()
            if (r2 != r3) goto L_0x0033
            java.lang.String r0 = r0.TAG
            java.lang.String r1 = "Imdn Update called for cancelled message, ignore it"
            com.sec.internal.log.IMSLog.i(r0, r1)
            return
        L_0x0033:
            java.lang.String r2 = "imdn_message_id"
            int r2 = r1.getColumnIndexOrThrow(r2)
            java.lang.String r10 = r1.getString(r2)
            java.lang.String r2 = "chat_id"
            int r2 = r1.getColumnIndexOrThrow(r2)
            java.lang.String r11 = r1.getString(r2)
            java.lang.String r12 = "disposition_notification_status"
            int r2 = r1.getColumnIndexOrThrow(r12)
            int r13 = r1.getInt(r2)
            java.lang.String r14 = "not_displayed_counter"
            int r2 = r1.getColumnIndexOrThrow(r14)
            int r15 = r1.getInt(r2)
            java.lang.String r2 = "creator"
            int r2 = r1.getColumnIndexOrThrow(r2)
            java.lang.String r1 = r1.getString(r2)
            boolean r2 = android.text.TextUtils.isEmpty(r1)
            r16 = 0
            if (r2 != 0) goto L_0x0077
            java.lang.String r2 = "SD"
            boolean r1 = r1.equalsIgnoreCase(r2)
            if (r1 == 0) goto L_0x0077
            r5 = 1
            goto L_0x0079
        L_0x0077:
            r5 = r16
        L_0x0079:
            java.lang.String r1 = "imdn_id"
            r8.put(r1, r10)
            long r1 = r7.lastModSeq
            java.lang.Long r1 = java.lang.Long.valueOf(r1)
            java.lang.String r2 = "lastmodseq"
            r8.put(r2, r1)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r0.mBufferDbQuery
            java.util.HashMap r4 = r1.queryRCSNotificationStatus(r10)
            com.sec.internal.omanetapi.nms.data.ImdnObject[] r3 = r7.imdn
            int r2 = r3.length
            r1 = r16
            r7 = r1
        L_0x0095:
            if (r1 >= r2) goto L_0x0148
            r6 = r3[r1]
            r17 = r1
            com.sec.internal.omanetapi.nms.data.ImdnInfo[] r1 = r6.imdnInfo
            if (r1 != 0) goto L_0x00ab
            java.lang.String r1 = r0.TAG
            java.lang.String r6 = "updateRCSImdnToBufferDB imdnInfo is empty"
            android.util.Log.i(r1, r6)
            r18 = r2
            goto L_0x0130
        L_0x00ab:
            android.content.ContentValues r1 = new android.content.ContentValues
            r1.<init>(r8)
            java.lang.String r8 = r6.originalTo
            r18 = r2
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r0.mBufferDbQuery
            r2.setNotificationStatusAndTimestamp(r6, r1)
            java.lang.Integer r6 = r1.getAsInteger(r9)
            if (r6 == 0) goto L_0x0130
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r0.mBufferDbQuery
            int r19 = r6.intValue()
            r20 = r1
            r1 = r2
            r2 = r26
            r21 = r3
            r3 = r20
            r22 = r4
            r4 = r10
            r23 = r9
            r9 = r5
            r5 = r8
            r24 = r15
            r15 = r6
            r6 = r19
            boolean r1 = r1.insertOrUpdateToNotificationDB(r2, r3, r4, r5, r6)
            if (r1 == 0) goto L_0x012d
            if (r9 == 0) goto L_0x0129
            boolean r1 = r22.isEmpty()
            if (r1 != 0) goto L_0x0129
            r6 = r22
            boolean r1 = r6.containsKey(r8)
            if (r1 == 0) goto L_0x012b
            java.lang.Object r1 = r6.get(r8)
            java.lang.Integer r1 = (java.lang.Integer) r1
            java.lang.String r2 = r0.TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "updateRCSImdnToBufferDB rcsNotificationStatus: "
            r3.append(r4)
            r3.append(r1)
            java.lang.String r4 = ", cloudNotificationStatus: "
            r3.append(r4)
            r3.append(r15)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r2, r3)
            if (r1 == 0) goto L_0x012b
            int r1 = r1.intValue()
            int r2 = r15.intValue()
            if (r1 >= r2) goto L_0x012b
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r0.mBufferDbQuery
            r2 = r20
            r1.updateRCSNotificationUsingImdnId(r10, r2, r8)
            goto L_0x012b
        L_0x0129:
            r6 = r22
        L_0x012b:
            r7 = 1
            goto L_0x0138
        L_0x012d:
            r6 = r22
            goto L_0x0138
        L_0x0130:
            r21 = r3
            r6 = r4
            r23 = r9
            r24 = r15
            r9 = r5
        L_0x0138:
            int r1 = r17 + 1
            r8 = r27
            r4 = r6
            r5 = r9
            r2 = r18
            r3 = r21
            r9 = r23
            r15 = r24
            goto L_0x0095
        L_0x0148:
            r6 = r4
            r9 = r5
            r24 = r15
            java.lang.String r1 = r0.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "updateRCSImdnToBufferDB msgDbUpdated: "
            r2.append(r3)
            r2.append(r7)
            java.lang.String r3 = ", isSDMessage: "
            r2.append(r3)
            r2.append(r9)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r0.mBufferDbQuery
            r1.queryImdnBufferDBandUpdateRcsMessageBufferDb(r10, r11)
            if (r7 == 0) goto L_0x027e
            if (r9 == 0) goto L_0x027e
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r0.mBufferDbQuery
            com.sec.internal.ims.cmstore.MessageStoreClient r2 = r0.mStoreClient
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r2 = r2.getPrerenceManager()
            java.lang.String r2 = r2.getUserTelCtn()
            android.database.Cursor r7 = r1.searchIMFTBufferUsingImdn(r10, r2)
            if (r7 == 0) goto L_0x0277
            boolean r1 = r7.moveToFirst()     // Catch:{ all -> 0x026b }
            if (r1 == 0) goto L_0x0277
            int r1 = r7.getColumnIndexOrThrow(r14)     // Catch:{ all -> 0x026b }
            int r1 = r7.getInt(r1)     // Catch:{ all -> 0x026b }
            int r2 = r7.getColumnIndexOrThrow(r12)     // Catch:{ all -> 0x026b }
            int r2 = r7.getInt(r2)     // Catch:{ all -> 0x026b }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r3 = r0.mBufferDbQuery     // Catch:{ all -> 0x026b }
            android.database.Cursor r3 = r3.queryRcsDBMessageUsingImdnId(r10)     // Catch:{ all -> 0x026b }
            if (r3 == 0) goto L_0x01e5
            boolean r4 = r3.moveToNext()     // Catch:{ all -> 0x01d9 }
            if (r4 == 0) goto L_0x01e5
            int r4 = r3.getColumnIndexOrThrow(r14)     // Catch:{ all -> 0x01d9 }
            int r4 = r3.getInt(r4)     // Catch:{ all -> 0x01d9 }
            java.lang.String r5 = r0.TAG     // Catch:{ all -> 0x01d9 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x01d9 }
            r8.<init>()     // Catch:{ all -> 0x01d9 }
            java.lang.String r9 = "updateRCSImdnToBufferDB newNotDisplayedCounter:"
            r8.append(r9)     // Catch:{ all -> 0x01d9 }
            r8.append(r1)     // Catch:{ all -> 0x01d9 }
            java.lang.String r9 = ", rcsDbNotDisplayedCounter: "
            r8.append(r9)     // Catch:{ all -> 0x01d9 }
            r8.append(r4)     // Catch:{ all -> 0x01d9 }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x01d9 }
            android.util.Log.i(r5, r8)     // Catch:{ all -> 0x01d9 }
            if (r4 <= r1) goto L_0x01e5
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r0.mBufferDbQuery     // Catch:{ all -> 0x01d9 }
            r4.queryBufferDbandUpdateRcsMessageDb(r10)     // Catch:{ all -> 0x01d9 }
            goto L_0x01e5
        L_0x01d9:
            r0 = move-exception
            r1 = r0
            r3.close()     // Catch:{ all -> 0x01df }
            goto L_0x01e4
        L_0x01df:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ all -> 0x026b }
        L_0x01e4:
            throw r1     // Catch:{ all -> 0x026b }
        L_0x01e5:
            if (r3 == 0) goto L_0x01ea
            r3.close()     // Catch:{ all -> 0x026b }
        L_0x01ea:
            java.lang.String r3 = r0.TAG     // Catch:{ all -> 0x026b }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x026b }
            r4.<init>()     // Catch:{ all -> 0x026b }
            java.lang.String r5 = "updateRCSImdnToBufferDB oldDispositionStatus: "
            r4.append(r5)     // Catch:{ all -> 0x026b }
            r4.append(r13)     // Catch:{ all -> 0x026b }
            java.lang.String r5 = ", newDispositionStatus: "
            r4.append(r5)     // Catch:{ all -> 0x026b }
            r4.append(r2)     // Catch:{ all -> 0x026b }
            java.lang.String r5 = ", oldNotDisplayedCounter: "
            r4.append(r5)     // Catch:{ all -> 0x026b }
            r5 = r24
            r4.append(r5)     // Catch:{ all -> 0x026b }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x026b }
            android.util.Log.i(r3, r4)     // Catch:{ all -> 0x026b }
            r3 = -1
            if (r1 == r3) goto L_0x0218
            if (r1 < r5) goto L_0x0228
        L_0x0218:
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r1 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.NONE     // Catch:{ all -> 0x026b }
            int r1 = r1.getId()     // Catch:{ all -> 0x026b }
            if (r13 != r1) goto L_0x0277
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r1 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DELIVERED     // Catch:{ all -> 0x026b }
            int r1 = r1.getId()     // Catch:{ all -> 0x026b }
            if (r2 != r1) goto L_0x0277
        L_0x0228:
            java.lang.String r1 = "_bufferdbid"
            int r1 = r7.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x026b }
            long r4 = r7.getLong(r1)     // Catch:{ all -> 0x026b }
            java.lang.String r1 = "is_filetransfer"
            int r1 = r7.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x026b }
            int r1 = r7.getInt(r1)     // Catch:{ all -> 0x026b }
            r2 = 1
            if (r1 != r2) goto L_0x0241
            r16 = r2
        L_0x0241:
            java.lang.String r1 = r0.TAG     // Catch:{ all -> 0x026b }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x026b }
            r2.<init>()     // Catch:{ all -> 0x026b }
            java.lang.String r3 = "updateRCSImdnToBufferDB bufferDbId: "
            r2.append(r3)     // Catch:{ all -> 0x026b }
            r2.append(r4)     // Catch:{ all -> 0x026b }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x026b }
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x026b }
            java.lang.String r2 = "MessageApp"
            if (r16 == 0) goto L_0x025f
            java.lang.String r1 = "RCS_IMDN_FT"
            goto L_0x0261
        L_0x025f:
            java.lang.String r1 = "RCS_IMDN_CHAT"
        L_0x0261:
            r3 = r1
            r8 = 0
            r1 = r25
            r0 = r6
            r6 = r8
            r1.notifyMsgAppCldNotification(r2, r3, r4, r6)     // Catch:{ all -> 0x026b }
            goto L_0x0278
        L_0x026b:
            r0 = move-exception
            r1 = r0
            r7.close()     // Catch:{ all -> 0x0271 }
            goto L_0x0276
        L_0x0271:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)
        L_0x0276:
            throw r1
        L_0x0277:
            r0 = r6
        L_0x0278:
            if (r7 == 0) goto L_0x027f
            r7.close()
            goto L_0x027f
        L_0x027e:
            r0 = r6
        L_0x027f:
            r0.clear()
            return
        L_0x0283:
            java.lang.String r0 = r0.TAG
            java.lang.String r1 = "updateRCSImdnToBufferDB imdn/imdns is empty"
            android.util.Log.i(r0, r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsSchedulerHelper.updateRCSImdnToBufferDB(com.sec.internal.omanetapi.nms.data.ImdnList, android.content.ContentValues, android.database.Cursor):void");
    }
}
