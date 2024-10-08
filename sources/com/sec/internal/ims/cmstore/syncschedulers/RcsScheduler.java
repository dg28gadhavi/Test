package com.sec.internal.ims.cmstore.syncschedulers;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.data.AttributeNames;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.DeviceIMFTUpdateParam;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.XmlParser;
import com.sec.internal.omanetapi.nms.data.Attribute;
import com.sec.internal.omanetapi.nms.data.GroupState;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.Part;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class RcsScheduler extends RcsSchedulerHelper {
    /* access modifiers changed from: private */
    public String TAG = RcsScheduler.class.getSimpleName();
    private RcsDbSessionObserver mRcsDbSessionObserver = null;
    private final Queue<ContentValues> mSessionQueue;

    private void handleExistingBufferForDeviceIMDNUpdate(Cursor cursor, DeviceIMFTUpdateParam deviceIMFTUpdateParam) {
    }

    private void handleNonExistingBufferForDeviceIMDNUpdate(DeviceIMFTUpdateParam deviceIMFTUpdateParam) {
    }

    public RcsScheduler(MessageStoreClient messageStoreClient, CloudMessageBufferDBEventSchedulingRule cloudMessageBufferDBEventSchedulingRule, SummaryQueryBuilder summaryQueryBuilder, IDeviceDataChangeListener iDeviceDataChangeListener, IBufferDBEventListener iBufferDBEventListener, MmsScheduler mmsScheduler, SmsScheduler smsScheduler, Looper looper) {
        super(messageStoreClient, cloudMessageBufferDBEventSchedulingRule, summaryQueryBuilder, iDeviceDataChangeListener, iBufferDBEventListener, mmsScheduler, smsScheduler, looper);
        LinkedList linkedList = new LinkedList();
        this.mSessionQueue = linkedList;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        registerRcsDbSessionObserver(looper);
        linkedList.clear();
    }

    private void updateSyncFlag(int i, boolean z, String str, long j, ParamSyncFlagsSet paramSyncFlagsSet, ContentValues contentValues, boolean z2) {
        ParamSyncFlagsSet paramSyncFlagsSet2 = paramSyncFlagsSet;
        if (i > 0) {
            if (paramSyncFlagsSet2.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice) || paramSyncFlagsSet2.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                if (paramSyncFlagsSet2.mAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update) || paramSyncFlagsSet2.mAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Cancel)) {
                    this.mBufferDbQuery.updateRCSMessageDb(i, contentValues);
                } else if (paramSyncFlagsSet2.mAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
                    this.mBufferDbQuery.deleteRCSMessageDb(i);
                }
            }
            handleOutPutParamSyncFlagSet(paramSyncFlagsSet, j, 1, z2, z, str, (BufferDBChangeParamList) null, false);
        }
    }

    private void updateSyncDirection(ContentValues contentValues, ParamSyncFlagsSet paramSyncFlagsSet, String str, String str2) {
        if (TextUtils.isEmpty(str2) && Util.isDownloadObject(str, this.mStoreClient, 1)) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
        } else if (paramSyncFlagsSet.mIsChanged) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(paramSyncFlagsSet.mDirection.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(paramSyncFlagsSet.mAction.getId()));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0110 A[Catch:{ all -> 0x0235, all -> 0x023d, ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0113 A[Catch:{ all -> 0x0235, all -> 0x023d, ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x012d A[Catch:{ all -> 0x0235, all -> 0x023d, ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0148 A[Catch:{ all -> 0x0235, all -> 0x023d, ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x014a A[Catch:{ all -> 0x0235, all -> 0x023d, ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x015d A[Catch:{ all -> 0x0235, all -> 0x023d, ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }] */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0172 A[Catch:{ all -> 0x0235, all -> 0x023d, ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x017c A[Catch:{ all -> 0x0235, all -> 0x023d, ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }] */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x01d4 A[Catch:{ all -> 0x0235, all -> 0x023d, ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }] */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0231 A[SYNTHETIC, Splitter:B:68:0x0231] */
    /* JADX WARNING: Removed duplicated region for block: B:87:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleNormalSyncObjectRcsMessageDownload(com.sec.internal.ims.cmstore.params.ParamOMAObject r24, boolean r25) {
        /*
            r23 = this;
            r11 = r23
            r0 = r24
            java.lang.String r1 = "status"
            java.lang.String r2 = r11.TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "handleNormalSyncObjectRcsMessageDownload: "
            r3.append(r4)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            android.util.Log.d(r2, r3)
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r10 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None
            r10.<init>(r2, r3)
            java.net.URL r2 = r0.resourceURL
            java.lang.String r2 = r2.toString()
            java.lang.String r12 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r2)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r11.mBufferDbQuery     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }
            java.lang.String r3 = r0.correlationId     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }
            android.database.Cursor r15 = r2.searchIMFTBufferUsingImdn(r3, r12)     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }
            r9 = 1
            r8 = 0
            if (r15 == 0) goto L_0x0181
            boolean r2 = r15.moveToFirst()     // Catch:{ all -> 0x0235 }
            if (r2 == 0) goto L_0x0181
            java.lang.String r2 = r0.TEXT_CONTENT     // Catch:{ all -> 0x0235 }
            java.lang.String r3 = "_bufferdbid"
            int r3 = r15.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x0235 }
            int r3 = r15.getInt(r3)     // Catch:{ all -> 0x0235 }
            long r5 = (long) r3     // Catch:{ all -> 0x0235 }
            java.lang.String r3 = "_id"
            int r3 = r15.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x0235 }
            int r3 = r15.getInt(r3)     // Catch:{ all -> 0x0235 }
            java.lang.String r4 = "syncaction"
            int r4 = r15.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x0235 }
            int r4 = r15.getInt(r4)     // Catch:{ all -> 0x0235 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r4)     // Catch:{ all -> 0x0235 }
            java.lang.String r7 = "syncdirection"
            int r7 = r15.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x0235 }
            int r7 = r15.getInt(r7)     // Catch:{ all -> 0x0235 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r7)     // Catch:{ all -> 0x0235 }
            java.lang.String r13 = "_bufferdbid=?"
            java.lang.String[] r14 = new java.lang.String[r9]     // Catch:{ all -> 0x0235 }
            java.lang.String r16 = java.lang.String.valueOf(r5)     // Catch:{ all -> 0x0235 }
            r14[r8] = r16     // Catch:{ all -> 0x0235 }
            android.content.ContentValues r8 = new android.content.ContentValues     // Catch:{ all -> 0x0235 }
            r8.<init>()     // Catch:{ all -> 0x0235 }
            java.lang.String r9 = "lastmodseq"
            r16 = r2
            java.lang.Long r2 = r0.lastModSeq     // Catch:{ all -> 0x0235 }
            r8.put(r9, r2)     // Catch:{ all -> 0x0235 }
            java.lang.String r2 = "res_url"
            java.net.URL r9 = r0.resourceURL     // Catch:{ all -> 0x0235 }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x0235 }
            java.lang.String r9 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r9)     // Catch:{ all -> 0x0235 }
            r8.put(r2, r9)     // Catch:{ all -> 0x0235 }
            java.lang.String r2 = "parentfolder"
            java.net.URL r9 = r0.parentFolder     // Catch:{ all -> 0x0235 }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x0235 }
            java.lang.String r9 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r9)     // Catch:{ all -> 0x0235 }
            r8.put(r2, r9)     // Catch:{ all -> 0x0235 }
            java.lang.String r2 = "path"
            java.lang.String r9 = r0.path     // Catch:{ all -> 0x0235 }
            java.lang.String r9 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r9)     // Catch:{ all -> 0x0235 }
            r8.put(r2, r9)     // Catch:{ all -> 0x0235 }
            java.lang.String r2 = "content_type"
            java.lang.String r9 = r0.CONTENT_TYPE     // Catch:{ all -> 0x0235 }
            r8.put(r2, r9)     // Catch:{ all -> 0x0235 }
            int r2 = r15.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0235 }
            int r2 = r15.getInt(r2)     // Catch:{ all -> 0x0235 }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r9 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.READ     // Catch:{ all -> 0x0235 }
            r17 = r4
            int r4 = r9.getId()     // Catch:{ all -> 0x0235 }
            if (r2 == r4) goto L_0x00e8
            java.lang.String r2 = "ft_status"
            int r2 = r15.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x0235 }
            int r2 = r15.getInt(r2)     // Catch:{ all -> 0x0235 }
            int r4 = r9.getId()     // Catch:{ all -> 0x0235 }
            if (r2 != r4) goto L_0x00e3
            goto L_0x00e8
        L_0x00e3:
            r20 = r7
            r21 = r17
            goto L_0x00f0
        L_0x00e8:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x0235 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud     // Catch:{ all -> 0x0235 }
            r21 = r2
            r20 = r4
        L_0x00f0:
            java.lang.String r2 = r0.CONTENT_TYPE     // Catch:{ all -> 0x0235 }
            boolean r2 = android.text.TextUtils.isEmpty(r2)     // Catch:{ all -> 0x0235 }
            if (r2 != 0) goto L_0x0113
            java.lang.String r2 = r0.CONTENT_TYPE     // Catch:{ all -> 0x0235 }
            boolean r2 = com.sec.internal.ims.cmstore.utils.Util.isLocationPushContentType(r2)     // Catch:{ all -> 0x0235 }
            if (r2 != 0) goto L_0x0110
            java.lang.String r2 = r0.CONTENT_TYPE     // Catch:{ all -> 0x0235 }
            boolean r2 = com.sec.internal.ims.cmstore.utils.Util.isBotMessageContentType(r2)     // Catch:{ all -> 0x0235 }
            if (r2 != 0) goto L_0x0110
            java.lang.String r2 = r0.CONTENT_TYPE     // Catch:{ all -> 0x0235 }
            boolean r2 = com.sec.internal.ims.cmstore.utils.Util.isBotResponseMessageContentType(r2)     // Catch:{ all -> 0x0235 }
            if (r2 == 0) goto L_0x0113
        L_0x0110:
            java.lang.String r2 = r0.MESSAGEBODY     // Catch:{ all -> 0x0235 }
            goto L_0x0115
        L_0x0113:
            r2 = r16
        L_0x0115:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r4 = r11.mScheduleRule     // Catch:{ all -> 0x0235 }
            int r7 = r11.mDbTableContractIndex     // Catch:{ all -> 0x0235 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r9 = r0.mFlag     // Catch:{ all -> 0x0235 }
            r16 = r4
            r17 = r7
            r18 = r5
            r22 = r9
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r7 = r16.getSetFlagsForCldOperation(r17, r18, r20, r21, r22)     // Catch:{ all -> 0x0235 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = r7.mAction     // Catch:{ all -> 0x0235 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r9 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Cancel     // Catch:{ all -> 0x0235 }
            if (r4 != r9) goto L_0x013a
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r4 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.CANCELLATION     // Catch:{ all -> 0x0235 }
            int r4 = r4.getId()     // Catch:{ all -> 0x0235 }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x0235 }
            r8.put(r1, r4)     // Catch:{ all -> 0x0235 }
        L_0x013a:
            java.lang.String r1 = r0.DATE     // Catch:{ all -> 0x0235 }
            r11.updateSyncDirection(r8, r7, r1, r2)     // Catch:{ all -> 0x0235 }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r11.mBufferDbQuery     // Catch:{ all -> 0x0235 }
            r1.updateRCSMessageInBufferDBUsingObject(r0, r8, r13, r14)     // Catch:{ all -> 0x0235 }
            com.sec.internal.omanetapi.nms.data.PayloadPartInfo[] r1 = r0.payloadPart     // Catch:{ all -> 0x0235 }
            if (r1 == 0) goto L_0x014a
            r9 = 1
            goto L_0x014b
        L_0x014a:
            r9 = 0
        L_0x014b:
            r1 = r23
            r2 = r3
            r3 = r25
            r4 = r12
            r13 = r5
            r16 = r13
            r13 = 0
            r14 = 1
            r1.updateSyncFlag(r2, r3, r4, r5, r7, r8, r9)     // Catch:{ all -> 0x0235 }
            boolean r1 = r11.isCmsEnabled     // Catch:{ all -> 0x0235 }
            if (r1 == 0) goto L_0x0160
            r11.updateRCSImdnToBufferDBUsingObject(r0, r15)     // Catch:{ all -> 0x0235 }
        L_0x0160:
            java.lang.String r1 = r0.TEXT_CONTENT     // Catch:{ all -> 0x0235 }
            boolean r1 = android.text.TextUtils.isEmpty(r1)     // Catch:{ all -> 0x0235 }
            if (r1 == 0) goto L_0x017c
            java.lang.String r1 = r0.DATE     // Catch:{ all -> 0x0235 }
            com.sec.internal.ims.cmstore.MessageStoreClient r2 = r11.mStoreClient     // Catch:{ all -> 0x0235 }
            boolean r1 = com.sec.internal.ims.cmstore.utils.Util.isDownloadObject(r1, r2, r14)     // Catch:{ all -> 0x0235 }
            if (r1 == 0) goto L_0x017c
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Downloading     // Catch:{ all -> 0x0235 }
            r10.mDirection = r1     // Catch:{ all -> 0x0235 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad     // Catch:{ all -> 0x0235 }
            r10.mAction = r1     // Catch:{ all -> 0x0235 }
            r9 = r14
            goto L_0x017d
        L_0x017c:
            r9 = r13
        L_0x017d:
            r7 = r25
            r8 = r9
            goto L_0x01ca
        L_0x0181:
            r13 = r8
            r14 = r9
            r7 = r25
            int r1 = r11.handleObjectDownloadCrossSearch(r0, r12, r7)     // Catch:{ all -> 0x0235 }
            java.lang.String r2 = r11.TAG     // Catch:{ all -> 0x0235 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0235 }
            r3.<init>()     // Catch:{ all -> 0x0235 }
            java.lang.String r4 = "handleNormalSyncObjectRcsMessageDownload: RCS not foundcontractTypeFromLegacy: "
            r3.append(r4)     // Catch:{ all -> 0x0235 }
            r3.append(r1)     // Catch:{ all -> 0x0235 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0235 }
            android.util.Log.i(r2, r3)     // Catch:{ all -> 0x0235 }
            if (r1 == r14) goto L_0x01a9
            if (r15 == 0) goto L_0x01a6
            r15.close()     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }
        L_0x01a6:
            r1 = -1
            return r1
        L_0x01a9:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r11.mBufferDbQuery     // Catch:{ all -> 0x0235 }
            java.lang.String r1 = r1.searchOrCreateSession(r0)     // Catch:{ all -> 0x0235 }
            if (r1 != 0) goto L_0x01b9
            if (r15 == 0) goto L_0x01b6
            r15.close()     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }
        L_0x01b6:
            r1 = -1
            return r1
        L_0x01b9:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r11.mBufferDbQuery     // Catch:{ all -> 0x0235 }
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r1 = r2.insertRCSMessageToBufferDBUsingObject(r0, r1, r13)     // Catch:{ all -> 0x0235 }
            long r2 = r1.mBufferId     // Catch:{ all -> 0x0235 }
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r4 = r11.mSummaryDB     // Catch:{ all -> 0x0235 }
            r4.insertSummaryDbUsingObjectIfNonExist(r0, r14)     // Catch:{ all -> 0x0235 }
            r10 = r1
            r16 = r2
            r8 = r13
        L_0x01ca:
            java.lang.String r1 = r0.DATE     // Catch:{ all -> 0x0235 }
            com.sec.internal.ims.cmstore.MessageStoreClient r2 = r11.mStoreClient     // Catch:{ all -> 0x0235 }
            boolean r1 = com.sec.internal.ims.cmstore.utils.Util.isDownloadObject(r1, r2, r14)     // Catch:{ all -> 0x0235 }
            if (r1 == 0) goto L_0x022f
            int r1 = r0.mObjectType     // Catch:{ all -> 0x0235 }
            r2 = 12
            if (r1 == r2) goto L_0x01eb
            java.lang.String r2 = r11.getAppTypeString(r1)     // Catch:{ all -> 0x0235 }
            java.lang.String r3 = r11.getMessageTypeString(r14, r13)     // Catch:{ all -> 0x0235 }
            r6 = 0
            r1 = r23
            r4 = r16
            r1.notifyMsgAppCldNotification(r2, r3, r4, r6)     // Catch:{ all -> 0x0235 }
            goto L_0x022f
        L_0x01eb:
            com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r9 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParamList     // Catch:{ all -> 0x0235 }
            r9.<init>()     // Catch:{ all -> 0x0235 }
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r13 = r9.mChangelst     // Catch:{ all -> 0x0235 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r14 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x0235 }
            r1 = 1
            java.lang.String r5 = r0.mLine     // Catch:{ all -> 0x0235 }
            com.sec.internal.ims.cmstore.MessageStoreClient r6 = r11.mStoreClient     // Catch:{ all -> 0x0235 }
            r0 = r14
            r2 = r16
            r4 = r25
            r0.<init>(r1, r2, r4, r5, r6)     // Catch:{ all -> 0x0235 }
            r13.add(r14)     // Catch:{ all -> 0x0235 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r0 = r10.mDirection     // Catch:{ all -> 0x0235 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Downloading     // Catch:{ all -> 0x0235 }
            boolean r0 = r0.equals(r1)     // Catch:{ all -> 0x0235 }
            if (r0 == 0) goto L_0x0220
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r0 = r10.mAction     // Catch:{ all -> 0x0235 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad     // Catch:{ all -> 0x0235 }
            boolean r0 = r0.equals(r1)     // Catch:{ all -> 0x0235 }
            if (r0 == 0) goto L_0x0220
            if (r8 == 0) goto L_0x0220
            com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener r0 = r11.mDeviceDataChangeListener     // Catch:{ all -> 0x0235 }
            r0.sendDeviceNormalSyncDownload(r9)     // Catch:{ all -> 0x0235 }
            goto L_0x022f
        L_0x0220:
            r5 = 1
            r6 = 1
            r0 = 0
            r1 = r23
            r2 = r10
            r3 = r16
            r7 = r25
            r8 = r12
            r10 = r0
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9, r10)     // Catch:{ all -> 0x0235 }
        L_0x022f:
            if (r15 == 0) goto L_0x025e
            r15.close()     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }
            goto L_0x025e
        L_0x0235:
            r0 = move-exception
            r1 = r0
            if (r15 == 0) goto L_0x0242
            r15.close()     // Catch:{ all -> 0x023d }
            goto L_0x0242
        L_0x023d:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }
        L_0x0242:
            throw r1     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x0243 }
        L_0x0243:
            r0 = move-exception
            java.lang.String r1 = r11.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "nullpointer or ArrayIndexOutOfBounds Exception: "
            r2.append(r3)
            java.lang.String r0 = r0.getMessage()
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            android.util.Log.e(r1, r0)
        L_0x025e:
            r1 = -1
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.handleNormalSyncObjectRcsMessageDownload(com.sec.internal.ims.cmstore.params.ParamOMAObject, boolean):long");
    }

    private int handleObjectDownloadCrossSearch(ParamOMAObject paramOMAObject, String str, boolean z) {
        int crossObjectSearchLegacy;
        String str2 = this.TAG;
        Log.d(str2, "handleObjectDownloadCrossSearch: " + paramOMAObject);
        if (!this.mStoreClient.getCloudMessageStrategyManager().getStrategy().requiresInterworkingCrossSearch() || (crossObjectSearchLegacy = crossObjectSearchLegacy(paramOMAObject, str, z)) == 1) {
            return 1;
        }
        this.mSummaryDB.insertSummaryDbUsingObjectIfNonExist(paramOMAObject, crossObjectSearchLegacy);
        return crossObjectSearchLegacy;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x00f5  */
    /* JADX WARNING: Removed duplicated region for block: B:23:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleNormalSyncObjectRcsImdnDownload(com.sec.internal.ims.cmstore.params.ParamOMAObject r8) {
        /*
            r7 = this;
            java.lang.String r0 = r7.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "handleNormalSyncObjectRcsImdnDownload: "
            r1.append(r2)
            r1.append(r8)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
            java.lang.String r0 = r8.DISPOSITION_ORIGINAL_TO
            java.lang.String r0 = com.sec.internal.ims.cmstore.utils.Util.getPhoneNum(r0)
            android.content.Context r1 = r7.mContext
            com.sec.internal.ims.cmstore.MessageStoreClient r2 = r7.mStoreClient
            int r2 = r2.getClientID()
            java.lang.String r1 = com.sec.internal.ims.cmstore.utils.Util.getSimCountryCode(r1, r2)
            java.lang.String r0 = com.sec.internal.ims.cmstore.utils.Util.getTelUri(r0, r1)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r7.mBufferDbQuery
            java.lang.String r2 = r8.DISPOSITION_ORIGINAL_MESSAGEID
            android.database.Cursor r0 = r1.searchBufferNotificationUsingImdnAndTelUri(r2, r0)
            if (r0 == 0) goto L_0x00ee
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x00fb }
            if (r1 == 0) goto L_0x00ee
            java.lang.String r1 = "_bufferdbid"
            int r1 = r0.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x00fb }
            int r1 = r0.getInt(r1)     // Catch:{ all -> 0x00fb }
            long r1 = (long) r1     // Catch:{ all -> 0x00fb }
            java.lang.String r3 = "_bufferdbid=?"
            r4 = 1
            java.lang.String[] r4 = new java.lang.String[r4]     // Catch:{ all -> 0x00fb }
            java.lang.String r1 = java.lang.String.valueOf(r1)     // Catch:{ all -> 0x00fb }
            r2 = 0
            r4[r2] = r1     // Catch:{ all -> 0x00fb }
            android.content.ContentValues r1 = new android.content.ContentValues     // Catch:{ all -> 0x00fb }
            r1.<init>()     // Catch:{ all -> 0x00fb }
            java.lang.String r2 = "lastmodseq"
            java.lang.Long r5 = r8.lastModSeq     // Catch:{ all -> 0x00fb }
            r1.put(r2, r5)     // Catch:{ all -> 0x00fb }
            java.lang.String r2 = "res_url"
            java.net.URL r5 = r8.resourceURL     // Catch:{ all -> 0x00fb }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x00fb }
            java.lang.String r5 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r5)     // Catch:{ all -> 0x00fb }
            r1.put(r2, r5)     // Catch:{ all -> 0x00fb }
            java.lang.String r2 = "parentfolder"
            java.net.URL r5 = r8.parentFolder     // Catch:{ all -> 0x00fb }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x00fb }
            java.lang.String r5 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r5)     // Catch:{ all -> 0x00fb }
            r1.put(r2, r5)     // Catch:{ all -> 0x00fb }
            java.lang.String r2 = "path"
            java.lang.String r5 = r8.path     // Catch:{ all -> 0x00fb }
            java.lang.String r5 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r5)     // Catch:{ all -> 0x00fb }
            r1.put(r2, r5)     // Catch:{ all -> 0x00fb }
            java.lang.String r2 = "imdn_id"
            java.lang.String r5 = r8.DISPOSITION_ORIGINAL_MESSAGEID     // Catch:{ all -> 0x00fb }
            r1.put(r2, r5)     // Catch:{ all -> 0x00fb }
            java.lang.String r2 = "syncaction"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None     // Catch:{ all -> 0x00fb }
            int r5 = r5.getId()     // Catch:{ all -> 0x00fb }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x00fb }
            r1.put(r2, r5)     // Catch:{ all -> 0x00fb }
            java.lang.String r2 = "syncdirection"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done     // Catch:{ all -> 0x00fb }
            int r5 = r5.getId()     // Catch:{ all -> 0x00fb }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x00fb }
            r1.put(r2, r5)     // Catch:{ all -> 0x00fb }
            java.lang.String r2 = "timestamp"
            long r5 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x00fb }
            java.lang.Long r5 = java.lang.Long.valueOf(r5)     // Catch:{ all -> 0x00fb }
            r1.put(r2, r5)     // Catch:{ all -> 0x00fb }
            java.lang.String r2 = "displayed"
            java.lang.String r8 = r8.DISPOSITION_STATUS     // Catch:{ all -> 0x00fb }
            boolean r8 = r2.equalsIgnoreCase(r8)     // Catch:{ all -> 0x00fb }
            java.lang.String r2 = "status"
            if (r8 == 0) goto L_0x00d9
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r8 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED     // Catch:{ all -> 0x00fb }
            int r8 = r8.getId()     // Catch:{ all -> 0x00fb }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x00fb }
            r1.put(r2, r8)     // Catch:{ all -> 0x00fb }
            goto L_0x00e6
        L_0x00d9:
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r8 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DELIVERED     // Catch:{ all -> 0x00fb }
            int r8 = r8.getId()     // Catch:{ all -> 0x00fb }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x00fb }
            r1.put(r2, r8)     // Catch:{ all -> 0x00fb }
        L_0x00e6:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r7 = r7.mBufferDbQuery     // Catch:{ all -> 0x00fb }
            r8 = 13
            r7.updateTable(r8, r1, r3, r4)     // Catch:{ all -> 0x00fb }
            goto L_0x00f3
        L_0x00ee:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r7 = r7.mBufferDbQuery     // Catch:{ all -> 0x00fb }
            r7.insertRCSimdnToBufferDBUsingObject(r8)     // Catch:{ all -> 0x00fb }
        L_0x00f3:
            if (r0 == 0) goto L_0x00f8
            r0.close()
        L_0x00f8:
            r7 = -1
            return r7
        L_0x00fb:
            r7 = move-exception
            if (r0 == 0) goto L_0x0106
            r0.close()     // Catch:{ all -> 0x0102 }
            goto L_0x0106
        L_0x0102:
            r8 = move-exception
            r7.addSuppressed(r8)
        L_0x0106:
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.handleNormalSyncObjectRcsImdnDownload(com.sec.internal.ims.cmstore.params.ParamOMAObject):long");
    }

    /* JADX WARNING: Removed duplicated region for block: B:104:0x02cb A[SYNTHETIC, Splitter:B:104:0x02cb] */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x02e1 A[SYNTHETIC, Splitter:B:115:0x02e1] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x00ea A[Catch:{ all -> 0x018a }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0101 A[Catch:{ all -> 0x018a }] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x013d A[Catch:{ all -> 0x018a }] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0179 A[Catch:{ all -> 0x018a }] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x029e A[Catch:{ all -> 0x02b6, all -> 0x02bc, all -> 0x02cf }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleObjectRCSMessageCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject r23, boolean r24) {
        /*
            r22 = this;
            r11 = r22
            r0 = r23
            java.lang.String r1 = "status"
            java.lang.String r2 = "syncdirection"
            java.lang.String r3 = "syncaction"
            java.lang.String r4 = r11.TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "handleObjectRCSMessageCloudSearch: "
            r5.append(r6)
            r5.append(r0)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
            java.net.URL r4 = r0.resourceURL
            java.lang.String r4 = r4.toString()
            java.lang.String r8 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r4)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r11.mBufferDbQuery     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x02ed }
            java.lang.String r5 = r0.correlationId     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x02ed }
            android.database.Cursor r14 = r4.searchIMFTBufferUsingImdn(r5, r8)     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x02ed }
            r5 = 0
            r6 = 1
            if (r14 == 0) goto L_0x0190
            boolean r7 = r14.moveToFirst()     // Catch:{ all -> 0x018a }
            if (r7 == 0) goto L_0x0190
            java.lang.String r7 = "_bufferdbid"
            int r7 = r14.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x018a }
            int r7 = r14.getInt(r7)     // Catch:{ all -> 0x018a }
            long r7 = (long) r7     // Catch:{ all -> 0x018a }
            int r9 = r14.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x018a }
            int r9 = r14.getInt(r9)     // Catch:{ all -> 0x018a }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r20 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r9)     // Catch:{ all -> 0x018a }
            int r9 = r14.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x018a }
            int r9 = r14.getInt(r9)     // Catch:{ all -> 0x018a }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r19 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r9)     // Catch:{ all -> 0x018a }
            java.lang.String r9 = "_id"
            int r9 = r14.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x018a }
            int r9 = r14.getInt(r9)     // Catch:{ all -> 0x018a }
            java.lang.String r10 = "_bufferdbid=?"
            java.lang.String[] r15 = new java.lang.String[r6]     // Catch:{ all -> 0x018a }
            java.lang.String r16 = java.lang.String.valueOf(r7)     // Catch:{ all -> 0x018a }
            r15[r5] = r16     // Catch:{ all -> 0x018a }
            android.content.ContentValues r12 = new android.content.ContentValues     // Catch:{ all -> 0x018a }
            r12.<init>()     // Catch:{ all -> 0x018a }
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r13 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x018a }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done     // Catch:{ all -> 0x018a }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None     // Catch:{ all -> 0x018a }
            r13.<init>(r4, r6)     // Catch:{ all -> 0x018a }
            r13.mIsChanged = r5     // Catch:{ all -> 0x018a }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x018a }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r6 = r0.mFlag     // Catch:{ all -> 0x018a }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x018a }
            boolean r6 = r6.equals(r5)     // Catch:{ all -> 0x018a }
            if (r6 == 0) goto L_0x00a0
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice     // Catch:{ all -> 0x018a }
            r6 = 1
            r13.setIsChangedActionAndDirection(r6, r5, r1)     // Catch:{ all -> 0x018a }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r11.mBufferDbQuery     // Catch:{ all -> 0x018a }
            r1.deleteRCSMessageDb(r9)     // Catch:{ all -> 0x018a }
            r24 = r4
            goto L_0x00e0
        L_0x00a0:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r6 = r0.mFlag     // Catch:{ all -> 0x018a }
            r24 = r4
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x018a }
            boolean r6 = r6.equals(r4)     // Catch:{ all -> 0x018a }
            if (r6 == 0) goto L_0x00e0
            int r6 = r14.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x018a }
            int r6 = r14.getInt(r6)     // Catch:{ all -> 0x018a }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r16 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.READ     // Catch:{ all -> 0x018a }
            r17 = r4
            int r4 = r16.getId()     // Catch:{ all -> 0x018a }
            if (r6 == r4) goto L_0x00dd
            int r4 = r16.getId()     // Catch:{ all -> 0x018a }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x018a }
            r12.put(r1, r4)     // Catch:{ all -> 0x018a }
            java.lang.String r1 = "disposition_notification_status"
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r4 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED     // Catch:{ all -> 0x018a }
            int r4 = r4.getId()     // Catch:{ all -> 0x018a }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x018a }
            r12.put(r1, r4)     // Catch:{ all -> 0x018a }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r11.mBufferDbQuery     // Catch:{ all -> 0x018a }
            r1.updateRCSMessageDb(r9, r12)     // Catch:{ all -> 0x018a }
        L_0x00dd:
            r21 = r17
            goto L_0x00e2
        L_0x00e0:
            r21 = r24
        L_0x00e2:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = r0.mFlag     // Catch:{ all -> 0x018a }
            boolean r1 = r1.equals(r5)     // Catch:{ all -> 0x018a }
            if (r1 != 0) goto L_0x0101
            boolean r1 = r11.isCmsEnabled     // Catch:{ all -> 0x018a }
            if (r1 == 0) goto L_0x00f1
            r11.updateRCSImdnToBufferDBUsingObject(r0, r14)     // Catch:{ all -> 0x018a }
        L_0x00f1:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r1 = r11.mScheduleRule     // Catch:{ all -> 0x018a }
            int r4 = r11.mDbTableContractIndex     // Catch:{ all -> 0x018a }
            r5 = r15
            r15 = r1
            r16 = r4
            r17 = r7
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r1 = r15.getSetFlagsForCldOperation(r16, r17, r19, r20, r21)     // Catch:{ all -> 0x018a }
            r13 = r1
            goto L_0x0102
        L_0x0101:
            r5 = r15
        L_0x0102:
            java.lang.String r1 = "lastmodseq"
            java.lang.Long r4 = r0.lastModSeq     // Catch:{ all -> 0x018a }
            r12.put(r1, r4)     // Catch:{ all -> 0x018a }
            java.lang.String r1 = "res_url"
            java.net.URL r4 = r0.resourceURL     // Catch:{ all -> 0x018a }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x018a }
            java.lang.String r4 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r4)     // Catch:{ all -> 0x018a }
            r12.put(r1, r4)     // Catch:{ all -> 0x018a }
            java.lang.String r1 = "parentfolder"
            java.net.URL r4 = r0.parentFolder     // Catch:{ all -> 0x018a }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x018a }
            java.lang.String r4 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r4)     // Catch:{ all -> 0x018a }
            r12.put(r1, r4)     // Catch:{ all -> 0x018a }
            java.lang.String r1 = "path"
            java.lang.String r4 = r0.path     // Catch:{ all -> 0x018a }
            java.lang.String r4 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r4)     // Catch:{ all -> 0x018a }
            r12.put(r1, r4)     // Catch:{ all -> 0x018a }
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r1 = r11.mSummaryDB     // Catch:{ all -> 0x018a }
            r4 = 1
            r1.insertSummaryDbUsingObjectIfNonExist(r0, r4)     // Catch:{ all -> 0x018a }
            boolean r1 = r13.mIsChanged     // Catch:{ all -> 0x018a }
            if (r1 == 0) goto L_0x0179
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = r13.mAction     // Catch:{ all -> 0x018a }
            int r1 = r1.getId()     // Catch:{ all -> 0x018a }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x018a }
            r12.put(r3, r1)     // Catch:{ all -> 0x018a }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = r13.mDirection     // Catch:{ all -> 0x018a }
            int r1 = r1.getId()     // Catch:{ all -> 0x018a }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x018a }
            r12.put(r2, r1)     // Catch:{ all -> 0x018a }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r11.mBufferDbQuery     // Catch:{ all -> 0x018a }
            r2 = 1
            r1.updateTable(r2, r12, r10, r5)     // Catch:{ all -> 0x018a }
            int r1 = r0.mObjectType     // Catch:{ all -> 0x018a }
            r2 = 12
            if (r1 != r2) goto L_0x0165
            r6 = 1
            goto L_0x0166
        L_0x0165:
            r6 = 0
        L_0x0166:
            r5 = 1
            boolean r9 = r0.mIsGoforwardSync     // Catch:{ all -> 0x018a }
            java.lang.String r0 = r0.mLine     // Catch:{ all -> 0x018a }
            r10 = 0
            r12 = 0
            r1 = r22
            r2 = r13
            r3 = r7
            r7 = r9
            r8 = r0
            r9 = r10
            r10 = r12
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9, r10)     // Catch:{ all -> 0x018a }
            goto L_0x0186
        L_0x0179:
            java.lang.String r0 = r11.TAG     // Catch:{ all -> 0x018a }
            java.lang.String r1 = "flagsetresult.mIsChanged: false - don't update sync action or direction"
            android.util.Log.d(r0, r1)     // Catch:{ all -> 0x018a }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r11.mBufferDbQuery     // Catch:{ all -> 0x018a }
            r1 = 1
            r0.updateTable(r1, r12, r10, r5)     // Catch:{ all -> 0x018a }
        L_0x0186:
            r12 = -1
            goto L_0x02c9
        L_0x018a:
            r0 = move-exception
            r1 = r0
            r12 = -1
            goto L_0x02df
        L_0x0190:
            r7 = r24
            int r1 = r11.handleObjectDownloadCrossSearch(r0, r8, r7)     // Catch:{ all -> 0x02da }
            java.lang.String r2 = r11.TAG     // Catch:{ all -> 0x02da }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x02da }
            r3.<init>()     // Catch:{ all -> 0x02da }
            java.lang.String r4 = "handleObjectRCSCloudSearch: RCS not found: contractTypeFromLegacy: "
            r3.append(r4)     // Catch:{ all -> 0x02da }
            r3.append(r1)     // Catch:{ all -> 0x02da }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x02da }
            android.util.Log.i(r2, r3)     // Catch:{ all -> 0x02da }
            r2 = 1
            if (r1 == r2) goto L_0x01bd
            if (r14 == 0) goto L_0x01ba
            r14.close()     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x01b5 }
            goto L_0x01ba
        L_0x01b5:
            r0 = move-exception
            r12 = -1
            goto L_0x02f1
        L_0x01ba:
            r1 = -1
            return r1
        L_0x01bd:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r11.mBufferDbQuery     // Catch:{ all -> 0x02da }
            java.lang.String r12 = r1.searchOrCreateSession(r0)     // Catch:{ all -> 0x02da }
            if (r12 != 0) goto L_0x01cd
            if (r14 == 0) goto L_0x01ca
            r14.close()     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x01b5 }
        L_0x01ca:
            r1 = -1
            return r1
        L_0x01cd:
            r1 = -1
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r3 = r11.mBufferDbQuery     // Catch:{ all -> 0x02d8 }
            r4 = 1
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r3 = r3.insertRCSMessageToBufferDBUsingObject(r0, r12, r4)     // Catch:{ all -> 0x02d8 }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r11.mBufferDbQuery     // Catch:{ all -> 0x02d8 }
            com.sec.internal.omanetapi.nms.data.FlagList r5 = r0.mFlagList     // Catch:{ all -> 0x02d8 }
            boolean r4 = r4.getIfCancelUsingFlag(r5)     // Catch:{ all -> 0x02d8 }
            long r9 = r3.mBufferId     // Catch:{ all -> 0x02d8 }
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r1 = r11.mSummaryDB     // Catch:{ all -> 0x02d1 }
            r2 = 1
            r1.insertSummaryDbUsingObjectIfNonExist(r0, r2)     // Catch:{ all -> 0x02d1 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri     // Catch:{ all -> 0x02d1 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = r3.mAction     // Catch:{ all -> 0x02d1 }
            boolean r13 = r1.equals(r5)     // Catch:{ all -> 0x02d1 }
            java.lang.String r1 = r11.TAG     // Catch:{ all -> 0x02d1 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x02d1 }
            r5.<init>()     // Catch:{ all -> 0x02d1 }
            java.lang.String r6 = "result:"
            r5.append(r6)     // Catch:{ all -> 0x02d1 }
            r5.append(r3)     // Catch:{ all -> 0x02d1 }
            java.lang.String r6 = " fetchuri:"
            r5.append(r6)     // Catch:{ all -> 0x02d1 }
            if (r13 != 0) goto L_0x0206
            goto L_0x0207
        L_0x0206:
            r2 = 0
        L_0x0207:
            r5.append(r2)     // Catch:{ all -> 0x02d1 }
            java.lang.String r2 = " text:"
            r5.append(r2)     // Catch:{ all -> 0x02d1 }
            java.lang.String r2 = r0.TEXT_CONTENT     // Catch:{ all -> 0x02d1 }
            boolean r2 = android.text.TextUtils.isEmpty(r2)     // Catch:{ all -> 0x02d1 }
            r5.append(r2)     // Catch:{ all -> 0x02d1 }
            java.lang.String r2 = " isCancelStatus:"
            r5.append(r2)     // Catch:{ all -> 0x02d1 }
            r5.append(r4)     // Catch:{ all -> 0x02d1 }
            java.lang.String r2 = r5.toString()     // Catch:{ all -> 0x02d1 }
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x02d1 }
            r1 = 0
            int r1 = (r9 > r1 ? 1 : (r9 == r1 ? 0 : -1))
            if (r1 <= 0) goto L_0x0285
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice     // Catch:{ all -> 0x02d1 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r2 = r3.mDirection     // Catch:{ all -> 0x02d1 }
            boolean r1 = r1.equals(r2)     // Catch:{ all -> 0x02d1 }
            if (r1 != 0) goto L_0x0247
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x0242 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r2 = r3.mDirection     // Catch:{ all -> 0x0242 }
            boolean r1 = r1.equals(r2)     // Catch:{ all -> 0x0242 }
            if (r1 == 0) goto L_0x0285
            goto L_0x0247
        L_0x0242:
            r0 = move-exception
            r1 = r0
            r12 = r9
            goto L_0x02df
        L_0x0247:
            if (r13 == 0) goto L_0x025b
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = r11.mStoreClient     // Catch:{ all -> 0x0242 }
            android.content.Context r1 = r1.getContext()     // Catch:{ all -> 0x0242 }
            com.sec.internal.ims.cmstore.MessageStoreClient r2 = r11.mStoreClient     // Catch:{ all -> 0x0242 }
            int r2 = r2.getClientID()     // Catch:{ all -> 0x0242 }
            boolean r1 = com.sec.internal.ims.cmstore.utils.CmsUtil.isMcsSupported(r1, r2)     // Catch:{ all -> 0x0242 }
            if (r1 == 0) goto L_0x0285
        L_0x025b:
            int r1 = r0.mObjectType     // Catch:{ all -> 0x02d1 }
            r2 = 12
            if (r1 != r2) goto L_0x0274
            if (r4 != 0) goto L_0x0274
            r5 = 1
            r6 = 1
            r15 = 0
            r1 = r22
            r2 = r3
            r3 = r9
            r7 = r24
            r16 = r9
            r9 = r15
            r10 = r13
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9, r10)     // Catch:{ all -> 0x02cf }
            goto L_0x0287
        L_0x0274:
            r16 = r9
            r5 = 1
            r6 = 0
            r9 = 0
            r10 = 0
            r1 = r22
            r2 = r3
            r3 = r16
            r7 = r24
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9, r10)     // Catch:{ all -> 0x02cf }
            goto L_0x0287
        L_0x0285:
            r16 = r9
        L_0x0287:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r11.mBufferDbQuery     // Catch:{ all -> 0x02cf }
            java.lang.String r2 = r0.correlationId     // Catch:{ all -> 0x02cf }
            r1.queryImdnBufferDBandUpdateRcsMessageBufferDb(r2, r12)     // Catch:{ all -> 0x02cf }
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = r11.mStoreClient     // Catch:{ all -> 0x02cf }
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r1 = r1.getCloudMessageStrategyManager()     // Catch:{ all -> 0x02cf }
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r1 = r1.getStrategy()     // Catch:{ all -> 0x02cf }
            boolean r1 = r1.isStoreImdnEnabled()     // Catch:{ all -> 0x02cf }
            if (r1 == 0) goto L_0x02c7
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r11.mBufferDbQuery     // Catch:{ all -> 0x02cf }
            java.lang.String r2 = r0.correlationId     // Catch:{ all -> 0x02cf }
            android.database.Cursor r1 = r1.queryRcsDBMessageUsingImdnId(r2)     // Catch:{ all -> 0x02cf }
            if (r1 == 0) goto L_0x02c2
            boolean r2 = r1.moveToNext()     // Catch:{ all -> 0x02b6 }
            if (r2 == 0) goto L_0x02c2
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r11.mBufferDbQuery     // Catch:{ all -> 0x02b6 }
            java.lang.String r0 = r0.correlationId     // Catch:{ all -> 0x02b6 }
            r2.queryBufferDbandUpdateRcsMessageDb(r0)     // Catch:{ all -> 0x02b6 }
            goto L_0x02c2
        L_0x02b6:
            r0 = move-exception
            r2 = r0
            r1.close()     // Catch:{ all -> 0x02bc }
            goto L_0x02c1
        L_0x02bc:
            r0 = move-exception
            r1 = r0
            r2.addSuppressed(r1)     // Catch:{ all -> 0x02cf }
        L_0x02c1:
            throw r2     // Catch:{ all -> 0x02cf }
        L_0x02c2:
            if (r1 == 0) goto L_0x02c7
            r1.close()     // Catch:{ all -> 0x02cf }
        L_0x02c7:
            r12 = r16
        L_0x02c9:
            if (r14 == 0) goto L_0x030b
            r14.close()     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x02eb }
            goto L_0x030b
        L_0x02cf:
            r0 = move-exception
            goto L_0x02d4
        L_0x02d1:
            r0 = move-exception
            r16 = r9
        L_0x02d4:
            r1 = r0
            r12 = r16
            goto L_0x02df
        L_0x02d8:
            r0 = move-exception
            goto L_0x02dd
        L_0x02da:
            r0 = move-exception
            r1 = -1
        L_0x02dd:
            r12 = r1
            r1 = r0
        L_0x02df:
            if (r14 == 0) goto L_0x02ea
            r14.close()     // Catch:{ all -> 0x02e5 }
            goto L_0x02ea
        L_0x02e5:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x02eb }
        L_0x02ea:
            throw r1     // Catch:{ ArrayIndexOutOfBoundsException | NullPointerException -> 0x02eb }
        L_0x02eb:
            r0 = move-exception
            goto L_0x02f1
        L_0x02ed:
            r0 = move-exception
            r1 = -1
            r12 = r1
        L_0x02f1:
            java.lang.String r1 = r11.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "nullpointer or ArrayIndexOutOfBoundsException: "
            r2.append(r3)
            java.lang.String r0 = r0.getMessage()
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            android.util.Log.e(r1, r0)
        L_0x030b:
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.handleObjectRCSMessageCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject, boolean):long");
    }

    public void updateRCSImdnToBufferDBUsingObject(ParamOMAObject paramOMAObject, Cursor cursor) {
        Log.d(this.TAG, "updateRCSImdnToBufferDBUsingObject:");
        if (paramOMAObject.mImdns != null && !"IN".equalsIgnoreCase(paramOMAObject.DIRECTION)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject.resourceURL.toString()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(paramOMAObject.parentFolder.toString()));
            if (!TextUtils.isEmpty(paramOMAObject.path)) {
                contentValues.put("path", Util.decodeUrlFromServer(paramOMAObject.path));
            }
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
            updateRCSImdnToBufferDB(paramOMAObject.mImdns, contentValues, cursor);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0140 A[SYNTHETIC, Splitter:B:28:0x0140] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0170 A[Catch:{ all -> 0x019b, all -> 0x01a0, all -> 0x0164, all -> 0x0169, all -> 0x01b0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0183 A[Catch:{ all -> 0x019b, all -> 0x01a0, all -> 0x0164, all -> 0x0169, all -> 0x01b0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x01ac  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleObjectRCSIMDNCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject r13) {
        /*
            r12 = this;
            java.lang.String r0 = r12.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "handleObjectRCSIMDNCloudSearch: "
            r1.append(r2)
            r1.append(r13)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
            java.net.URL r0 = r13.resourceURL
            java.lang.String r0 = r0.toString()
            java.lang.String r0 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)
            java.lang.String r1 = r13.DISPOSITION_ORIGINAL_TO
            java.lang.String r1 = com.sec.internal.ims.cmstore.utils.Util.getPhoneNum(r1)
            android.content.Context r2 = r12.mContext
            com.sec.internal.ims.cmstore.MessageStoreClient r3 = r12.mStoreClient
            int r3 = r3.getClientID()
            java.lang.String r2 = com.sec.internal.ims.cmstore.utils.Util.getSimCountryCode(r2, r3)
            java.lang.String r1 = com.sec.internal.ims.cmstore.utils.Util.getTelUri(r1, r2)
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r12.mBufferDbQuery
            java.lang.String r3 = r13.correlationId
            android.database.Cursor r2 = r2.searchBufferNotificationUsingImdnAndTelUri(r3, r1)
            r3 = 13
            r4 = -1
            if (r2 == 0) goto L_0x012c
            boolean r6 = r2.moveToFirst()     // Catch:{ all -> 0x01b0 }
            if (r6 == 0) goto L_0x012c
            java.lang.String r6 = "delivered"
            java.lang.String r7 = r13.DISPOSITION_STATUS     // Catch:{ all -> 0x01b0 }
            boolean r6 = r6.equalsIgnoreCase(r7)     // Catch:{ all -> 0x01b0 }
            java.lang.String r7 = "status"
            if (r6 == 0) goto L_0x0072
            int r6 = r2.getColumnIndex(r7)     // Catch:{ all -> 0x01b0 }
            int r6 = r2.getInt(r6)     // Catch:{ all -> 0x01b0 }
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r8 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED     // Catch:{ all -> 0x01b0 }
            int r8 = r8.getId()     // Catch:{ all -> 0x01b0 }
            if (r6 != r8) goto L_0x0072
            java.lang.String r12 = r12.TAG     // Catch:{ all -> 0x01b0 }
            java.lang.String r13 = "delivered comes after displayed, shouldn't update"
            android.util.Log.d(r12, r13)     // Catch:{ all -> 0x01b0 }
            r2.close()
            return r4
        L_0x0072:
            java.lang.String r6 = "_bufferdbid"
            int r6 = r2.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x01b0 }
            int r6 = r2.getInt(r6)     // Catch:{ all -> 0x01b0 }
            long r8 = (long) r6     // Catch:{ all -> 0x01b0 }
            java.lang.String r6 = "_bufferdbid=?"
            r10 = 1
            java.lang.String[] r10 = new java.lang.String[r10]     // Catch:{ all -> 0x01b0 }
            java.lang.String r8 = java.lang.String.valueOf(r8)     // Catch:{ all -> 0x01b0 }
            r9 = 0
            r10[r9] = r8     // Catch:{ all -> 0x01b0 }
            android.content.ContentValues r8 = new android.content.ContentValues     // Catch:{ all -> 0x01b0 }
            r8.<init>()     // Catch:{ all -> 0x01b0 }
            java.lang.String r9 = "lastmodseq"
            java.lang.Long r11 = r13.lastModSeq     // Catch:{ all -> 0x01b0 }
            r8.put(r9, r11)     // Catch:{ all -> 0x01b0 }
            java.lang.String r9 = "res_url"
            java.net.URL r11 = r13.resourceURL     // Catch:{ all -> 0x01b0 }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x01b0 }
            java.lang.String r11 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r11)     // Catch:{ all -> 0x01b0 }
            r8.put(r9, r11)     // Catch:{ all -> 0x01b0 }
            java.lang.String r9 = "parentfolder"
            java.net.URL r11 = r13.parentFolder     // Catch:{ all -> 0x01b0 }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x01b0 }
            java.lang.String r11 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r11)     // Catch:{ all -> 0x01b0 }
            r8.put(r9, r11)     // Catch:{ all -> 0x01b0 }
            java.lang.String r9 = "path"
            java.lang.String r11 = r13.path     // Catch:{ all -> 0x01b0 }
            java.lang.String r11 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r11)     // Catch:{ all -> 0x01b0 }
            r8.put(r9, r11)     // Catch:{ all -> 0x01b0 }
            java.lang.String r9 = "syncaction"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r11 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None     // Catch:{ all -> 0x01b0 }
            int r11 = r11.getId()     // Catch:{ all -> 0x01b0 }
            java.lang.Integer r11 = java.lang.Integer.valueOf(r11)     // Catch:{ all -> 0x01b0 }
            r8.put(r9, r11)     // Catch:{ all -> 0x01b0 }
            java.lang.String r9 = "syncdirection"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r11 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done     // Catch:{ all -> 0x01b0 }
            int r11 = r11.getId()     // Catch:{ all -> 0x01b0 }
            java.lang.Integer r11 = java.lang.Integer.valueOf(r11)     // Catch:{ all -> 0x01b0 }
            r8.put(r9, r11)     // Catch:{ all -> 0x01b0 }
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = r12.mStoreClient     // Catch:{ all -> 0x01b0 }
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r9 = r9.getCloudMessageStrategyManager()     // Catch:{ all -> 0x01b0 }
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r9 = r9.getStrategy()     // Catch:{ all -> 0x01b0 }
            boolean r9 = r9.isStoreImdnEnabled()     // Catch:{ all -> 0x01b0 }
            if (r9 == 0) goto L_0x0121
            java.lang.String r9 = "displayed"
            java.lang.String r11 = r13.DISPOSITION_STATUS     // Catch:{ all -> 0x01b0 }
            boolean r9 = r9.equalsIgnoreCase(r11)     // Catch:{ all -> 0x01b0 }
            if (r9 == 0) goto L_0x0107
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r9 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED     // Catch:{ all -> 0x01b0 }
            int r9 = r9.getId()     // Catch:{ all -> 0x01b0 }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x01b0 }
            r8.put(r7, r9)     // Catch:{ all -> 0x01b0 }
            goto L_0x0114
        L_0x0107:
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r9 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DELIVERED     // Catch:{ all -> 0x01b0 }
            int r9 = r9.getId()     // Catch:{ all -> 0x01b0 }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x01b0 }
            r8.put(r7, r9)     // Catch:{ all -> 0x01b0 }
        L_0x0114:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r7 = r12.mBufferDbQuery     // Catch:{ all -> 0x01b0 }
            r7.updateTable(r3, r8, r6, r10)     // Catch:{ all -> 0x01b0 }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r6 = r12.mBufferDbQuery     // Catch:{ all -> 0x01b0 }
            java.lang.String r7 = r13.DISPOSITION_ORIGINAL_MESSAGEID     // Catch:{ all -> 0x01b0 }
            r6.updateRCSNotificationUsingImdnId(r7, r8, r1)     // Catch:{ all -> 0x01b0 }
            goto L_0x0126
        L_0x0121:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r12.mBufferDbQuery     // Catch:{ all -> 0x01b0 }
            r1.updateTable(r3, r8, r6, r10)     // Catch:{ all -> 0x01b0 }
        L_0x0126:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r1 = r12.mSummaryDB     // Catch:{ all -> 0x01b0 }
            r1.insertSummaryDbUsingObjectIfNonExist(r13, r3)     // Catch:{ all -> 0x01b0 }
            goto L_0x0136
        L_0x012c:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r12.mBufferDbQuery     // Catch:{ all -> 0x01b0 }
            r1.insertRCSimdnToBufferDBUsingObject(r13)     // Catch:{ all -> 0x01b0 }
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r1 = r12.mSummaryDB     // Catch:{ all -> 0x01b0 }
            r1.insertSummaryDbUsingObjectIfNonExist(r13, r3)     // Catch:{ all -> 0x01b0 }
        L_0x0136:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r12.mBufferDbQuery     // Catch:{ all -> 0x01b0 }
            java.lang.String r3 = r13.DISPOSITION_ORIGINAL_MESSAGEID     // Catch:{ all -> 0x01b0 }
            android.database.Cursor r0 = r1.searchIMFTBufferUsingImdn(r3, r0)     // Catch:{ all -> 0x01b0 }
            if (r0 == 0) goto L_0x016e
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x0164 }
            if (r1 == 0) goto L_0x016e
            java.lang.String r1 = "not_displayed_counter"
            int r1 = r0.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0164 }
            int r1 = r0.getInt(r1)     // Catch:{ all -> 0x0164 }
            java.lang.String r3 = "disposition_notification_status"
            int r3 = r0.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x0164 }
            int r3 = r0.getInt(r3)     // Catch:{ all -> 0x0164 }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r6 = r12.mBufferDbQuery     // Catch:{ all -> 0x0164 }
            java.lang.String r7 = r13.DISPOSITION_ORIGINAL_MESSAGEID     // Catch:{ all -> 0x0164 }
            java.lang.String r8 = r13.DISPOSITION_STATUS     // Catch:{ all -> 0x0164 }
            r6.updateRcsMessageBufferDbIfNewIMDNReceived(r7, r1, r3, r8)     // Catch:{ all -> 0x0164 }
            goto L_0x016e
        L_0x0164:
            r12 = move-exception
            r0.close()     // Catch:{ all -> 0x0169 }
            goto L_0x016d
        L_0x0169:
            r13 = move-exception
            r12.addSuppressed(r13)     // Catch:{ all -> 0x01b0 }
        L_0x016d:
            throw r12     // Catch:{ all -> 0x01b0 }
        L_0x016e:
            if (r0 == 0) goto L_0x0173
            r0.close()     // Catch:{ all -> 0x01b0 }
        L_0x0173:
            com.sec.internal.ims.cmstore.MessageStoreClient r0 = r12.mStoreClient     // Catch:{ all -> 0x01b0 }
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r0.getCloudMessageStrategyManager()     // Catch:{ all -> 0x01b0 }
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()     // Catch:{ all -> 0x01b0 }
            boolean r0 = r0.isStoreImdnEnabled()     // Catch:{ all -> 0x01b0 }
            if (r0 == 0) goto L_0x01aa
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r12.mBufferDbQuery     // Catch:{ all -> 0x01b0 }
            java.lang.String r1 = r13.DISPOSITION_ORIGINAL_MESSAGEID     // Catch:{ all -> 0x01b0 }
            android.database.Cursor r0 = r0.queryRcsDBMessageUsingImdnId(r1)     // Catch:{ all -> 0x01b0 }
            if (r0 == 0) goto L_0x01a5
            boolean r1 = r0.moveToNext()     // Catch:{ all -> 0x019b }
            if (r1 == 0) goto L_0x01a5
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r12 = r12.mBufferDbQuery     // Catch:{ all -> 0x019b }
            java.lang.String r13 = r13.DISPOSITION_ORIGINAL_MESSAGEID     // Catch:{ all -> 0x019b }
            r12.queryBufferDbandUpdateRcsMessageDb(r13)     // Catch:{ all -> 0x019b }
            goto L_0x01a5
        L_0x019b:
            r12 = move-exception
            r0.close()     // Catch:{ all -> 0x01a0 }
            goto L_0x01a4
        L_0x01a0:
            r13 = move-exception
            r12.addSuppressed(r13)     // Catch:{ all -> 0x01b0 }
        L_0x01a4:
            throw r12     // Catch:{ all -> 0x01b0 }
        L_0x01a5:
            if (r0 == 0) goto L_0x01aa
            r0.close()     // Catch:{ all -> 0x01b0 }
        L_0x01aa:
            if (r2 == 0) goto L_0x01af
            r2.close()
        L_0x01af:
            return r4
        L_0x01b0:
            r12 = move-exception
            if (r2 == 0) goto L_0x01bb
            r2.close()     // Catch:{ all -> 0x01b7 }
            goto L_0x01bb
        L_0x01b7:
            r13 = move-exception
            r12.addSuppressed(r13)
        L_0x01bb:
            throw r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.handleObjectRCSIMDNCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject):long");
    }

    public void handleExistingBufferForDeviceRCSUpdate(Cursor cursor, DeviceIMFTUpdateParam deviceIMFTUpdateParam, boolean z, BufferDBChangeParamList bufferDBChangeParamList) {
        Throwable th;
        Cursor cursor2 = cursor;
        DeviceIMFTUpdateParam deviceIMFTUpdateParam2 = deviceIMFTUpdateParam;
        String str = this.TAG;
        IMSLog.s(str, "handleExistingBufferForDeviceRCSUpdate: " + deviceIMFTUpdateParam2);
        ContentValues contentValues = new ContentValues();
        CloudMessageBufferDBConstants.ActionStatusFlag valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        String string = cursor2.getString(cursor2.getColumnIndexOrThrow("linenum"));
        long j = cursor2.getLong(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        ParamSyncFlagsSet setFlagsForMsgOperation = this.mScheduleRule.getSetFlagsForMsgOperation(this.mDbTableContractIndex, j, valueOf2, valueOf, deviceIMFTUpdateParam2.mOperation);
        int i = cursor2.getInt(cursor2.getColumnIndexOrThrow("status"));
        if (setFlagsForMsgOperation.mIsChanged) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(setFlagsForMsgOperation.mDirection.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(setFlagsForMsgOperation.mAction.getId()));
        }
        long j2 = deviceIMFTUpdateParam2.mRowId;
        if (j2 > 0) {
            contentValues.put("_id", Long.valueOf(j2));
        }
        boolean z2 = cursor2.getInt(cursor2.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER)) == 1;
        String str2 = this.TAG;
        Log.d(str2, "isFt: " + z2 + " , action: " + deviceIMFTUpdateParam2.mUpdateType + " currStatus:" + i);
        if (z2 && CloudMessageBufferDBConstants.ActionStatusFlag.Delete.equals(deviceIMFTUpdateParam2.mUpdateType)) {
            String string2 = cursor2.getString(cursor2.getColumnIndex(ImContract.CsSession.FILE_PATH));
            String string3 = cursor2.getString(cursor2.getColumnIndex(ImContract.CsSession.THUMBNAIL_PATH));
            String str3 = this.TAG;
            Log.d(str3, "filepath: " + string2 + " , thumbpath: " + string3);
            if (!TextUtils.isEmpty(string2)) {
                File file = new File(string2);
                if (file.exists()) {
                    file.delete();
                }
            }
            if (!TextUtils.isEmpty(string3)) {
                File file2 = new File(string3);
                if (file2.exists()) {
                    file2.delete();
                }
            }
        }
        if (CloudMessageBufferDBConstants.MsgOperationFlag.Read.equals(deviceIMFTUpdateParam2.mOperation)) {
            if (i == ImConstants.Status.CANCELLATION_UNREAD.getId()) {
                ImConstants.Status status = ImConstants.Status.CANCELLATION;
                contentValues.put("status", Integer.valueOf(status.getId()));
                contentValues.put(ImContract.CsSession.STATUS, Integer.valueOf(status.getId()));
            } else if (i != ImConstants.Status.CANCELLATION.getId()) {
                ImConstants.Status status2 = ImConstants.Status.READ;
                contentValues.put("status", Integer.valueOf(status2.getId()));
                contentValues.put(ImContract.CsSession.STATUS, Integer.valueOf(status2.getId()));
            }
        } else if (CloudMessageBufferDBConstants.MsgOperationFlag.Cancel.equals(deviceIMFTUpdateParam2.mOperation)) {
            ImConstants.Status status3 = ImConstants.Status.CANCELLATION;
            contentValues.put("status", Integer.valueOf(status3.getId()));
            contentValues.put(ImContract.CsSession.STATUS, Integer.valueOf(status3.getId()));
        }
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isSupportExpiredRule() && CloudMessageBufferDBConstants.MsgOperationFlag.Received.equals(deviceIMFTUpdateParam2.mOperation) && z2) {
            Cursor queryIMFTUsingRowId = this.mBufferDbQuery.queryIMFTUsingRowId(deviceIMFTUpdateParam2.mRowId);
            if (queryIMFTUsingRowId != null) {
                try {
                    if (queryIMFTUsingRowId.moveToFirst()) {
                        contentValues.put(ImContract.CsSession.THUMBNAIL_PATH, queryIMFTUsingRowId.getString(queryIMFTUsingRowId.getColumnIndexOrThrow(ImContract.CsSession.THUMBNAIL_PATH)));
                    }
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            if (queryIMFTUsingRowId != null) {
                queryIMFTUsingRowId.close();
            }
        }
        this.mBufferDbQuery.updateTable(deviceIMFTUpdateParam2.mTableindex, contentValues, "_bufferdbid=?", new String[]{String.valueOf(j)});
        if (setFlagsForMsgOperation.mIsChanged) {
            handleOutPutParamSyncFlagSet(setFlagsForMsgOperation, j, deviceIMFTUpdateParam2.mTableindex, z2, z, string, bufferDBChangeParamList, false);
            return;
        }
        return;
        throw th;
    }

    public void handleNonExistingBufferForDeviceRCSUpdate(DeviceIMFTUpdateParam deviceIMFTUpdateParam) {
        String str = this.TAG;
        IMSLog.s(str, "handleNonExistingBufferForDeviceRCSUpdate: " + deviceIMFTUpdateParam);
        Cursor queryIMFTUsingRowId = this.mBufferDbQuery.queryIMFTUsingRowId(deviceIMFTUpdateParam.mRowId);
        if (queryIMFTUsingRowId != null) {
            try {
                if (queryIMFTUsingRowId.moveToFirst()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
                    if (this.mBufferDbQuery.insertToRCSMessagesBufferDB(queryIMFTUsingRowId, deviceIMFTUpdateParam.mLine, contentValues) < 1) {
                        Log.e(this.TAG, "handleNonExistingBufferForDeviceRCSUpdate: insert RCS Buffer DB error or meet blocked number!");
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryIMFTUsingRowId != null) {
            queryIMFTUsingRowId.close();
            return;
        }
        return;
        throw th;
    }

    public void handleDownLoadMessageResponse(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z) {
        if (!z && ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND.equals(paramOMAresponseforBufDB.getActionType())) {
            this.mBufferDbQuery.setMsgDeleted(paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x00c8  */
    /* JADX WARNING: Removed duplicated region for block: B:58:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onUpdateFromDeviceIMFT(com.sec.internal.ims.cmstore.params.DeviceIMFTUpdateParam r5, boolean r6, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r7) {
        /*
            r4 = this;
            java.lang.String r0 = r4.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onUpdateFromDeviceIMFT: "
            r1.append(r2)
            r1.append(r5)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.s(r0, r1)
            int r0 = r5.mTableindex
            r1 = 1
            if (r0 != r1) goto L_0x00ab
            r0 = 0
            int[] r1 = com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ all -> 0x00a4 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r2 = r5.mOperation     // Catch:{ all -> 0x00a4 }
            int r2 = r2.ordinal()     // Catch:{ all -> 0x00a4 }
            r1 = r1[r2]     // Catch:{ all -> 0x00a4 }
            switch(r1) {
                case 1: goto L_0x0077;
                case 2: goto L_0x0077;
                case 3: goto L_0x005d;
                case 4: goto L_0x005d;
                case 5: goto L_0x005d;
                case 6: goto L_0x0038;
                case 7: goto L_0x0038;
                case 8: goto L_0x0037;
                case 9: goto L_0x0037;
                case 10: goto L_0x0037;
                case 11: goto L_0x002a;
                default: goto L_0x0029;
            }     // Catch:{ all -> 0x00a4 }
        L_0x0029:
            goto L_0x0081
        L_0x002a:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r4.mBufferDbQuery     // Catch:{ all -> 0x00a4 }
            long r2 = r5.mRowId     // Catch:{ all -> 0x00a4 }
            java.lang.String r2 = java.lang.String.valueOf(r2)     // Catch:{ all -> 0x00a4 }
            android.database.Cursor r0 = r1.searchIMFTBufferUsingRowId(r2)     // Catch:{ all -> 0x00a4 }
            goto L_0x0081
        L_0x0037:
            return
        L_0x0038:
            java.lang.String r1 = r5.mChatId     // Catch:{ all -> 0x00a4 }
            if (r1 == 0) goto L_0x0043
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r4.mBufferDbQuery     // Catch:{ all -> 0x00a4 }
            android.database.Cursor r0 = r2.searchIMFTBufferUsingChatId(r1)     // Catch:{ all -> 0x00a4 }
            goto L_0x0081
        L_0x0043:
            java.lang.String r1 = r5.mImdnId     // Catch:{ all -> 0x00a4 }
            if (r1 == 0) goto L_0x0050
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r4.mBufferDbQuery     // Catch:{ all -> 0x00a4 }
            java.lang.String r3 = r5.mLine     // Catch:{ all -> 0x00a4 }
            android.database.Cursor r0 = r2.searchIMFTBufferUsingImdn(r1, r3)     // Catch:{ all -> 0x00a4 }
            goto L_0x0081
        L_0x0050:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r4.mBufferDbQuery     // Catch:{ all -> 0x00a4 }
            long r2 = r5.mRowId     // Catch:{ all -> 0x00a4 }
            java.lang.String r2 = java.lang.String.valueOf(r2)     // Catch:{ all -> 0x00a4 }
            android.database.Cursor r0 = r1.searchIMFTBufferUsingRowId(r2)     // Catch:{ all -> 0x00a4 }
            goto L_0x0081
        L_0x005d:
            java.lang.String r1 = r5.mImdnId     // Catch:{ all -> 0x00a4 }
            if (r1 == 0) goto L_0x006a
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r4.mBufferDbQuery     // Catch:{ all -> 0x00a4 }
            java.lang.String r3 = r5.mLine     // Catch:{ all -> 0x00a4 }
            android.database.Cursor r0 = r2.searchIMFTBufferUsingImdn(r1, r3)     // Catch:{ all -> 0x00a4 }
            goto L_0x0081
        L_0x006a:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r4.mBufferDbQuery     // Catch:{ all -> 0x00a4 }
            long r2 = r5.mRowId     // Catch:{ all -> 0x00a4 }
            java.lang.String r2 = java.lang.String.valueOf(r2)     // Catch:{ all -> 0x00a4 }
            android.database.Cursor r0 = r1.searchIMFTBufferUsingRowId(r2)     // Catch:{ all -> 0x00a4 }
            goto L_0x0081
        L_0x0077:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r1 = r4.mBufferDbQuery     // Catch:{ all -> 0x00a4 }
            java.lang.String r2 = r5.mImdnId     // Catch:{ all -> 0x00a4 }
            java.lang.String r3 = r5.mLine     // Catch:{ all -> 0x00a4 }
            android.database.Cursor r0 = r1.searchIMFTBufferUsingImdn(r2, r3)     // Catch:{ all -> 0x00a4 }
        L_0x0081:
            if (r0 == 0) goto L_0x0093
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x00a4 }
            if (r1 == 0) goto L_0x0093
        L_0x0089:
            r4.handleExistingBufferForDeviceRCSUpdate(r0, r5, r6, r7)     // Catch:{ all -> 0x00a4 }
            boolean r1 = r0.moveToNext()     // Catch:{ all -> 0x00a4 }
            if (r1 != 0) goto L_0x0089
            goto L_0x009e
        L_0x0093:
            long r6 = r5.mRowId     // Catch:{ all -> 0x00a4 }
            r1 = 0
            int r6 = (r6 > r1 ? 1 : (r6 == r1 ? 0 : -1))
            if (r6 <= 0) goto L_0x009e
            r4.handleNonExistingBufferForDeviceRCSUpdate(r5)     // Catch:{ all -> 0x00a4 }
        L_0x009e:
            if (r0 == 0) goto L_0x00d8
            r0.close()
            goto L_0x00d8
        L_0x00a4:
            r4 = move-exception
            if (r0 == 0) goto L_0x00aa
            r0.close()
        L_0x00aa:
            throw r4
        L_0x00ab:
            r6 = 13
            if (r0 != r6) goto L_0x00d8
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r6 = r4.mBufferDbQuery
            java.lang.String r7 = r5.mImdnId
            android.database.Cursor r6 = r6.searchBufferNotificationUsingImdn(r7)
            if (r6 == 0) goto L_0x00c3
            boolean r7 = r6.moveToFirst()     // Catch:{ all -> 0x00cc }
            if (r7 == 0) goto L_0x00c3
            r4.handleExistingBufferForDeviceIMDNUpdate(r6, r5)     // Catch:{ all -> 0x00cc }
            goto L_0x00c6
        L_0x00c3:
            r4.handleNonExistingBufferForDeviceIMDNUpdate(r5)     // Catch:{ all -> 0x00cc }
        L_0x00c6:
            if (r6 == 0) goto L_0x00d8
            r6.close()
            goto L_0x00d8
        L_0x00cc:
            r4 = move-exception
            if (r6 == 0) goto L_0x00d7
            r6.close()     // Catch:{ all -> 0x00d3 }
            goto L_0x00d7
        L_0x00d3:
            r5 = move-exception
            r4.addSuppressed(r5)
        L_0x00d7:
            throw r4
        L_0x00d8:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.onUpdateFromDeviceIMFT(com.sec.internal.ims.cmstore.params.DeviceIMFTUpdateParam, boolean, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList):void");
    }

    /* renamed from: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag;

        /* JADX WARNING: Can't wrap try/catch for region: R(22:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|(3:21|22|24)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(24:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|24) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0078 */
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
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Received     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Sent     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Read     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Cancel     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Starred     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Delete     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.UnStarred     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.SendFail     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Receiving     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Sending     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Download     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.AnonymousClass1.<clinit>():void");
        }
    }

    public void onAppOperationReceived(ParamAppJsonValue paramAppJsonValue, BufferDBChangeParamList bufferDBChangeParamList) {
        String str = this.TAG;
        IMSLog.s(str, "onAppOperationReceived: " + paramAppJsonValue);
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.None;
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[paramAppJsonValue.mOperation.ordinal()];
        if (i != 11) {
            switch (i) {
                case 1:
                case 2:
                case 5:
                    actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
                    break;
                case 3:
                    actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                    break;
                case 4:
                    actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Cancel;
                    break;
                case 6:
                case 7:
                    actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                    break;
            }
        } else {
            actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad;
        }
        DeviceIMFTUpdateParam deviceIMFTUpdateParam = new DeviceIMFTUpdateParam(paramAppJsonValue.mDataContractType, actionStatusFlag, paramAppJsonValue.mOperation, (long) paramAppJsonValue.mRowId, paramAppJsonValue.mChatId, paramAppJsonValue.mCorrelationId, paramAppJsonValue.mLine);
        if (CloudMessageBufferDBConstants.MsgOperationFlag.Download.equals(paramAppJsonValue.mOperation)) {
            onDownloadRequestFromApp(deviceIMFTUpdateParam);
        } else {
            onUpdateFromDeviceIMFT(deviceIMFTUpdateParam, false, bufferDBChangeParamList);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v6, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v7, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v17, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v18, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v38, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v40, resolved type: android.database.Cursor} */
    /* JADX WARNING: type inference failed for: r12v12 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x04e9 A[SYNTHETIC, Splitter:B:181:0x04e9] */
    /* JADX WARNING: Removed duplicated region for block: B:233:0x06a6 A[Catch:{ IOException -> 0x05fb, all -> 0x073b }] */
    /* JADX WARNING: Removed duplicated region for block: B:236:0x06d2 A[Catch:{ IOException -> 0x05fb, all -> 0x073b }] */
    /* JADX WARNING: Removed duplicated region for block: B:254:0x0737  */
    /* JADX WARNING: Removed duplicated region for block: B:260:0x0743 A[SYNTHETIC, Splitter:B:260:0x0743] */
    /* JADX WARNING: Removed duplicated region for block: B:272:0x075d A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x021c A[Catch:{ IOException -> 0x020c, all -> 0x04f9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x021f A[Catch:{ IOException -> 0x020c, all -> 0x04f9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x023b A[Catch:{ IOException -> 0x020c, all -> 0x04f9 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleCloudNotifyConferenceInfo(com.sec.internal.ims.cmstore.params.ParamOMAObject r40, com.sec.internal.omanetapi.nms.data.Object r41, boolean r42) {
        /*
            r39 = this;
            r1 = r39
            r2 = r40
            r0 = r41
            java.lang.String r3 = r1.TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "handleCloudNotifyConferenceInfo, objt is: "
            r4.append(r5)
            r4.append(r0)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r3, r4)
            com.google.gson.Gson r3 = new com.google.gson.Gson
            r3.<init>()
            com.sec.internal.ims.cmstore.MessageStoreClient r4 = r1.mStoreClient
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r4 = r4.getPrerenceManager()
            java.lang.String r4 = r4.getUserTelCtn()
            com.sec.internal.omanetapi.nms.data.AttributeList r0 = r0.attributes
            com.sec.internal.omanetapi.nms.data.Attribute[] r5 = r0.attribute
            int r6 = r5.length
            r0 = 0
            r8 = -1
            r9 = r8
            r10 = 0
            r11 = 0
            r8 = r0
        L_0x0036:
            if (r10 >= r6) goto L_0x076e
            r12 = r5[r10]
            java.lang.String[] r13 = r12.value
            int r14 = r13.length
            r15 = 0
        L_0x003e:
            if (r15 >= r14) goto L_0x0071
            r7 = r13[r15]
            r16 = r0
            java.lang.String r0 = r1.TAG
            r17 = r5
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            r18 = r6
            java.lang.String r6 = "Attribute key: "
            r5.append(r6)
            java.lang.String r6 = r12.name
            r5.append(r6)
            java.lang.String r6 = ", value: "
            r5.append(r6)
            r5.append(r7)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r0, r5)
            int r15 = r15 + 1
            r0 = r16
            r5 = r17
            r6 = r18
            goto L_0x003e
        L_0x0071:
            r16 = r0
            r17 = r5
            r18 = r6
            java.lang.String r0 = "MessageBody"
            java.lang.String r5 = r12.name
            boolean r0 = r0.equalsIgnoreCase(r5)
            if (r0 == 0) goto L_0x074d
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r1.mBufferDbQuery
            java.lang.String r5 = r2.CONVERSATION_ID
            android.database.Cursor r5 = r0.querySessionByConversationId(r5)
            java.lang.String[] r0 = r12.value     // Catch:{ all -> 0x073d }
            r6 = 0
            r0 = r0[r6]     // Catch:{ all -> 0x073d }
            java.lang.Class<com.sec.internal.omanetapi.nms.data.ConferenceInfo> r6 = com.sec.internal.omanetapi.nms.data.ConferenceInfo.class
            java.lang.Object r0 = r3.fromJson(r0, r6)     // Catch:{ all -> 0x073d }
            r6 = r0
            com.sec.internal.omanetapi.nms.data.ConferenceInfo r6 = (com.sec.internal.omanetapi.nms.data.ConferenceInfo) r6     // Catch:{ all -> 0x073d }
            java.util.HashSet r7 = new java.util.HashSet     // Catch:{ all -> 0x073d }
            r7.<init>()     // Catch:{ all -> 0x073d }
            java.util.HashSet r12 = new java.util.HashSet     // Catch:{ all -> 0x073d }
            r12.<init>()     // Catch:{ all -> 0x073d }
            java.lang.String r13 = "sim_imsi"
            java.lang.String r14 = "contribution_id"
            java.lang.String r15 = "chat_type"
            r19 = r3
            java.lang.String r3 = "created_by"
            r0 = r8
            java.lang.String r8 = "invited_by"
            r20 = r0
            java.lang.String r0 = "icon_path"
            r21 = r9
            java.lang.String r9 = "conversation_id"
            r22 = r11
            java.lang.String r11 = "_id"
            r23 = r10
            java.lang.String r10 = "own_sim_imsi"
            r24 = r7
            java.lang.String r7 = "inserted_time_stamp"
            r25 = r10
            java.lang.String r10 = "chat_id"
            r26 = r14
            java.lang.String r14 = "subject"
            r27 = r9
            java.lang.String r9 = "session_uri"
            java.lang.String r2 = "status"
            r28 = r13
            if (r5 == 0) goto L_0x04ff
            boolean r29 = r5.moveToFirst()     // Catch:{ all -> 0x073d }
            if (r29 == 0) goto L_0x04ff
            int r13 = r5.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x04f9 }
            java.lang.String r13 = r5.getString(r13)     // Catch:{ all -> 0x04f9 }
            r30 = r10
            int r10 = r5.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x04f9 }
            java.lang.String r10 = r5.getString(r10)     // Catch:{ all -> 0x04f9 }
            r31 = r15
            java.lang.String r15 = "_bufferdbid"
            int r15 = r5.getColumnIndexOrThrow(r15)     // Catch:{ all -> 0x04f9 }
            int r15 = r5.getInt(r15)     // Catch:{ all -> 0x04f9 }
            boolean r32 = android.text.TextUtils.isEmpty(r10)     // Catch:{ all -> 0x04f9 }
            if (r32 != 0) goto L_0x010b
            java.lang.Long r32 = java.lang.Long.valueOf(r10)     // Catch:{ all -> 0x04f9 }
            long r32 = r32.longValue()     // Catch:{ all -> 0x04f9 }
            goto L_0x010d
        L_0x010b:
            r32 = 0
        L_0x010d:
            boolean r10 = android.text.TextUtils.isEmpty(r10)     // Catch:{ all -> 0x04f9 }
            if (r10 != 0) goto L_0x0137
            java.lang.String r10 = r6.mTimestamp     // Catch:{ all -> 0x04f9 }
            java.lang.Long r10 = java.lang.Long.valueOf(r10)     // Catch:{ all -> 0x04f9 }
            long r34 = r10.longValue()     // Catch:{ all -> 0x04f9 }
            int r10 = (r32 > r34 ? 1 : (r32 == r34 ? 0 : -1))
            if (r10 >= 0) goto L_0x0122
            goto L_0x0137
        L_0x0122:
            r10 = r40
            r36 = r4
            r4 = r12
            r32 = r15
            r0 = r16
            r8 = r20
            r29 = r22
            r15 = r24
            r12 = r5
            r5 = r6
            r6 = r27
            goto L_0x04cf
        L_0x0137:
            int r10 = r5.getColumnIndexOrThrow(r14)     // Catch:{ all -> 0x04f9 }
            java.lang.String r10 = r5.getString(r10)     // Catch:{ all -> 0x04f9 }
            r32 = r15
            android.content.ContentValues r15 = new android.content.ContentValues     // Catch:{ all -> 0x04f9 }
            r15.<init>()     // Catch:{ all -> 0x04f9 }
            boolean r33 = android.text.TextUtils.isEmpty(r10)     // Catch:{ all -> 0x04f9 }
            if (r33 == 0) goto L_0x015e
            java.lang.String r10 = r1.TAG     // Catch:{ all -> 0x04f9 }
            r33 = r12
            java.lang.String r12 = "subject init"
            android.util.Log.d(r10, r12)     // Catch:{ all -> 0x04f9 }
            com.sec.internal.omanetapi.nms.data.ConferenceDescription r10 = r6.mConferenceDescription     // Catch:{ all -> 0x04f9 }
            java.lang.String r10 = r10.mSubject     // Catch:{ all -> 0x04f9 }
            r15.put(r14, r10)     // Catch:{ all -> 0x04f9 }
            goto L_0x0189
        L_0x015e:
            r33 = r12
            boolean r12 = android.text.TextUtils.isEmpty(r10)     // Catch:{ all -> 0x04f9 }
            if (r12 != 0) goto L_0x0189
            com.sec.internal.omanetapi.nms.data.ConferenceDescription r12 = r6.mConferenceDescription     // Catch:{ all -> 0x04f9 }
            java.lang.String r12 = r12.mSubject     // Catch:{ all -> 0x04f9 }
            boolean r12 = android.text.TextUtils.isEmpty(r12)     // Catch:{ all -> 0x04f9 }
            if (r12 != 0) goto L_0x0189
            com.sec.internal.omanetapi.nms.data.ConferenceDescription r12 = r6.mConferenceDescription     // Catch:{ all -> 0x04f9 }
            java.lang.String r12 = r12.mSubject     // Catch:{ all -> 0x04f9 }
            boolean r10 = r10.equals(r12)     // Catch:{ all -> 0x04f9 }
            if (r10 != 0) goto L_0x0189
            java.lang.String r10 = r1.TAG     // Catch:{ all -> 0x04f9 }
            java.lang.String r12 = "subject has been changed, update it"
            android.util.Log.d(r10, r12)     // Catch:{ all -> 0x04f9 }
            com.sec.internal.omanetapi.nms.data.ConferenceDescription r10 = r6.mConferenceDescription     // Catch:{ all -> 0x04f9 }
            java.lang.String r10 = r10.mSubject     // Catch:{ all -> 0x04f9 }
            r15.put(r14, r10)     // Catch:{ all -> 0x04f9 }
        L_0x0189:
            java.lang.String r10 = r6.mTimestamp     // Catch:{ all -> 0x04f9 }
            r15.put(r7, r10)     // Catch:{ all -> 0x04f9 }
            java.lang.String r7 = r6.mCreatedBy     // Catch:{ all -> 0x04f9 }
            boolean r7 = android.text.TextUtils.isEmpty(r7)     // Catch:{ all -> 0x04f9 }
            if (r7 != 0) goto L_0x019b
            java.lang.String r7 = r6.mCreatedBy     // Catch:{ all -> 0x04f9 }
            r15.put(r3, r7)     // Catch:{ all -> 0x04f9 }
        L_0x019b:
            int r3 = r5.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x04f9 }
            java.lang.String r3 = r5.getString(r3)     // Catch:{ all -> 0x04f9 }
            boolean r7 = android.text.TextUtils.isEmpty(r3)     // Catch:{ all -> 0x04f9 }
            if (r7 != 0) goto L_0x01b1
            java.lang.String r7 = r6.mEntity     // Catch:{ all -> 0x04f9 }
            boolean r3 = r3.equals(r7)     // Catch:{ all -> 0x04f9 }
            if (r3 != 0) goto L_0x01b6
        L_0x01b1:
            java.lang.String r3 = r6.mEntity     // Catch:{ all -> 0x04f9 }
            r15.put(r9, r3)     // Catch:{ all -> 0x04f9 }
        L_0x01b6:
            com.sec.internal.omanetapi.nms.data.ConferenceState r3 = r6.mConferenceState     // Catch:{ all -> 0x04f9 }
            boolean r3 = r3.mActivation     // Catch:{ all -> 0x04f9 }
            if (r3 == 0) goto L_0x01c3
            com.sec.internal.constants.ims.servicemodules.im.ChatData$State r3 = com.sec.internal.constants.ims.servicemodules.im.ChatData.State.ACTIVE     // Catch:{ all -> 0x04f9 }
        L_0x01be:
            int r3 = r3.getId()     // Catch:{ all -> 0x04f9 }
            goto L_0x01c6
        L_0x01c3:
            com.sec.internal.constants.ims.servicemodules.im.ChatData$State r3 = com.sec.internal.constants.ims.servicemodules.im.ChatData.State.NONE     // Catch:{ all -> 0x04f9 }
            goto L_0x01be
        L_0x01c6:
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ all -> 0x04f9 }
            r15.put(r2, r3)     // Catch:{ all -> 0x04f9 }
            int r3 = r5.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x04f9 }
            java.lang.String r3 = r5.getString(r3)     // Catch:{ all -> 0x04f9 }
            boolean r3 = android.text.TextUtils.isEmpty(r3)     // Catch:{ all -> 0x04f9 }
            if (r3 == 0) goto L_0x0214
            com.sec.internal.omanetapi.nms.data.ConferenceDescription r3 = r6.mConferenceDescription     // Catch:{ all -> 0x04f9 }
            com.sec.internal.omanetapi.nms.data.ConferenceDescription$Icon r3 = r3.mIcon     // Catch:{ all -> 0x04f9 }
            if (r3 == 0) goto L_0x0214
            com.sec.internal.omanetapi.nms.data.ConferenceDescription$Icon$FileInfo r3 = r3.mFileInfo     // Catch:{ all -> 0x04f9 }
            if (r3 == 0) goto L_0x0214
            java.lang.String r7 = r3.mContentType     // Catch:{ all -> 0x04f9 }
            java.lang.String r3 = r3.mData     // Catch:{ all -> 0x04f9 }
            if (r3 == 0) goto L_0x0214
            r10 = 0
            byte[] r3 = android.util.Base64.decode(r3, r10)     // Catch:{ all -> 0x04f9 }
            java.lang.String r7 = com.sec.internal.helper.translate.FileExtensionTranslator.translate(r7)     // Catch:{ all -> 0x04f9 }
            java.lang.String r7 = com.sec.internal.ims.cmstore.utils.Util.getRandomFileName(r7)     // Catch:{ all -> 0x04f9 }
            android.content.Context r10 = r1.mContext     // Catch:{ IOException -> 0x020c }
            com.sec.internal.ims.cmstore.MessageStoreClient r12 = r1.mStoreClient     // Catch:{ IOException -> 0x020c }
            int r12 = r12.getClientID()     // Catch:{ IOException -> 0x020c }
            r14 = 0
            java.lang.String r10 = com.sec.internal.ims.cmstore.utils.Util.generateUniqueFilePath(r10, r7, r14, r12)     // Catch:{ IOException -> 0x020c }
            com.sec.internal.ims.cmstore.utils.Util.saveFiletoPath(r3, r10)     // Catch:{ IOException -> 0x020c }
            r15.put(r0, r10)     // Catch:{ IOException -> 0x020c }
            goto L_0x0210
        L_0x020c:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x04f9 }
        L_0x0210:
            r0 = r3
            r20 = r7
            goto L_0x0216
        L_0x0214:
            r0 = r16
        L_0x0216:
            com.sec.internal.omanetapi.nms.data.ConferenceState r3 = r6.mConferenceState     // Catch:{ all -> 0x04f9 }
            boolean r7 = r3.mActivation     // Catch:{ all -> 0x04f9 }
            if (r7 != 0) goto L_0x021f
            r21 = 0
            goto L_0x0226
        L_0x021f:
            int r3 = r3.mUserCount     // Catch:{ all -> 0x04f9 }
            r7 = 1
            if (r3 != r7) goto L_0x0226
            r21 = 1
        L_0x0226:
            int r3 = r5.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x04f9 }
            int r3 = r5.getInt(r3)     // Catch:{ all -> 0x04f9 }
            java.util.HashSet r7 = new java.util.HashSet     // Catch:{ all -> 0x04f9 }
            r7.<init>()     // Catch:{ all -> 0x04f9 }
            com.sec.internal.omanetapi.nms.data.Users r10 = r6.mUsers     // Catch:{ all -> 0x04f9 }
            com.sec.internal.omanetapi.nms.data.Users$User[] r10 = r10.mUser     // Catch:{ all -> 0x04f9 }
            int r12 = r10.length     // Catch:{ all -> 0x04f9 }
            r14 = 0
        L_0x0239:
            if (r14 >= r12) goto L_0x02da
            r16 = r0
            r0 = r10[r14]     // Catch:{ all -> 0x04f9 }
            r34 = r10
            java.lang.String r10 = r0.mEntity     // Catch:{ all -> 0x04f9 }
            boolean r10 = r10.equalsIgnoreCase(r4)     // Catch:{ all -> 0x04f9 }
            r35 = r12
            java.lang.String r12 = "connected"
            if (r10 == 0) goto L_0x02b4
            com.sec.internal.omanetapi.nms.data.Users$User$Endpoint[] r10 = r0.mEndpoint     // Catch:{ all -> 0x04f9 }
            if (r10 == 0) goto L_0x026f
            r36 = r4
            java.lang.String r4 = "disconnected"
            r37 = 0
            r10 = r10[r37]     // Catch:{ all -> 0x04f9 }
            java.lang.String r10 = r10.mStatus     // Catch:{ all -> 0x04f9 }
            boolean r4 = r4.equalsIgnoreCase(r10)     // Catch:{ all -> 0x04f9 }
            if (r4 == 0) goto L_0x0271
            com.sec.internal.constants.ims.servicemodules.im.ChatData$State r4 = com.sec.internal.constants.ims.servicemodules.im.ChatData.State.NONE     // Catch:{ all -> 0x04f9 }
            int r4 = r4.getId()     // Catch:{ all -> 0x04f9 }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x04f9 }
            r15.put(r2, r4)     // Catch:{ all -> 0x04f9 }
            goto L_0x0295
        L_0x026f:
            r36 = r4
        L_0x0271:
            com.sec.internal.omanetapi.nms.data.Users$User$Endpoint[] r4 = r0.mEndpoint     // Catch:{ all -> 0x04f9 }
            if (r4 == 0) goto L_0x0295
            r10 = 0
            r4 = r4[r10]     // Catch:{ all -> 0x04f9 }
            java.lang.String r4 = r4.mStatus     // Catch:{ all -> 0x04f9 }
            boolean r4 = r12.equalsIgnoreCase(r4)     // Catch:{ all -> 0x04f9 }
            if (r4 == 0) goto L_0x0295
            com.sec.internal.constants.ims.servicemodules.im.ChatData$State r4 = com.sec.internal.constants.ims.servicemodules.im.ChatData.State.NONE     // Catch:{ all -> 0x04f9 }
            int r4 = r4.getId()     // Catch:{ all -> 0x04f9 }
            if (r3 != r4) goto L_0x0295
            com.sec.internal.constants.ims.servicemodules.im.ChatData$State r4 = com.sec.internal.constants.ims.servicemodules.im.ChatData.State.ACTIVE     // Catch:{ all -> 0x04f9 }
            int r4 = r4.getId()     // Catch:{ all -> 0x04f9 }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x04f9 }
            r15.put(r2, r4)     // Catch:{ all -> 0x04f9 }
        L_0x0295:
            boolean r4 = r0.mOwn     // Catch:{ all -> 0x04f9 }
            if (r4 == 0) goto L_0x02ce
            com.sec.internal.omanetapi.nms.data.Users$User$Endpoint[] r0 = r0.mEndpoint     // Catch:{ all -> 0x04f9 }
            if (r0 == 0) goto L_0x02ce
            r4 = 0
            r0 = r0[r4]     // Catch:{ all -> 0x04f9 }
            com.sec.internal.omanetapi.nms.data.Users$User$Endpoint$JoiningInfo r4 = r0.mJoingingInfo     // Catch:{ all -> 0x04f9 }
            if (r4 == 0) goto L_0x02ce
            java.lang.String r4 = r4.mBy     // Catch:{ all -> 0x04f9 }
            boolean r4 = android.text.TextUtils.isEmpty(r4)     // Catch:{ all -> 0x04f9 }
            if (r4 != 0) goto L_0x02ce
            com.sec.internal.omanetapi.nms.data.Users$User$Endpoint$JoiningInfo r0 = r0.mJoingingInfo     // Catch:{ all -> 0x04f9 }
            java.lang.String r0 = r0.mBy     // Catch:{ all -> 0x04f9 }
            r15.put(r8, r0)     // Catch:{ all -> 0x04f9 }
            goto L_0x02ce
        L_0x02b4:
            r36 = r4
            com.sec.internal.omanetapi.nms.data.Users$User$Endpoint[] r4 = r0.mEndpoint     // Catch:{ all -> 0x04f9 }
            if (r4 == 0) goto L_0x02ce
            r10 = 0
            r4 = r4[r10]     // Catch:{ all -> 0x04f9 }
            java.lang.String r4 = r4.mStatus     // Catch:{ all -> 0x04f9 }
            boolean r4 = r12.equalsIgnoreCase(r4)     // Catch:{ all -> 0x04f9 }
            if (r4 == 0) goto L_0x02ce
            java.lang.String r0 = r0.mEntity     // Catch:{ all -> 0x04f9 }
            com.sec.ims.util.ImsUri r0 = com.sec.ims.util.ImsUri.parse(r0)     // Catch:{ all -> 0x04f9 }
            r7.add(r0)     // Catch:{ all -> 0x04f9 }
        L_0x02ce:
            int r14 = r14 + 1
            r0 = r16
            r10 = r34
            r12 = r35
            r4 = r36
            goto L_0x0239
        L_0x02da:
            r16 = r0
            r36 = r4
            if (r42 != 0) goto L_0x0320
            java.lang.Integer r0 = r15.getAsInteger(r2)     // Catch:{ all -> 0x04f9 }
            if (r0 == 0) goto L_0x0320
            java.lang.Integer r0 = r15.getAsInteger(r2)     // Catch:{ all -> 0x04f9 }
            int r0 = r0.intValue()     // Catch:{ all -> 0x04f9 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData$State r4 = com.sec.internal.constants.ims.servicemodules.im.ChatData.State.NONE     // Catch:{ all -> 0x04f9 }
            int r8 = r4.getId()     // Catch:{ all -> 0x04f9 }
            if (r0 != r8) goto L_0x02fe
            com.sec.internal.constants.ims.servicemodules.im.ChatData$State r0 = com.sec.internal.constants.ims.servicemodules.im.ChatData.State.CLOSED_VOLUNTARILY     // Catch:{ all -> 0x04f9 }
            int r0 = r0.getId()     // Catch:{ all -> 0x04f9 }
            if (r3 == r0) goto L_0x0314
        L_0x02fe:
            java.lang.Integer r0 = r15.getAsInteger(r2)     // Catch:{ all -> 0x04f9 }
            int r0 = r0.intValue()     // Catch:{ all -> 0x04f9 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData$State r8 = com.sec.internal.constants.ims.servicemodules.im.ChatData.State.ACTIVE     // Catch:{ all -> 0x04f9 }
            int r8 = r8.getId()     // Catch:{ all -> 0x04f9 }
            if (r0 != r8) goto L_0x0320
            int r0 = r4.getId()     // Catch:{ all -> 0x04f9 }
            if (r3 != r0) goto L_0x0320
        L_0x0314:
            java.lang.String r0 = r1.TAG     // Catch:{ all -> 0x04f9 }
            java.lang.String r1 = "rejoin or duplicate event with PD left skip"
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x04f9 }
            r5.close()
            return
        L_0x0320:
            int r0 = r15.size()     // Catch:{ all -> 0x04f9 }
            if (r0 <= 0) goto L_0x0332
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r1.mBufferDbQuery     // Catch:{ all -> 0x04f9 }
            r0.updateSessionBufferDb(r13, r15)     // Catch:{ all -> 0x04f9 }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r1.mBufferDbQuery     // Catch:{ all -> 0x04f9 }
            r0.updateRCSSessionDb(r13, r15)     // Catch:{ all -> 0x04f9 }
            r22 = 1
        L_0x0332:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r1.mBufferDbQuery     // Catch:{ all -> 0x04f9 }
            android.database.Cursor r3 = r0.queryParticipantsUsingChatId(r13)     // Catch:{ all -> 0x04f9 }
            if (r3 == 0) goto L_0x03ed
        L_0x033a:
            boolean r0 = r3.moveToNext()     // Catch:{ all -> 0x03e8 }
            if (r0 == 0) goto L_0x03ed
            java.lang.String r0 = "uri"
            int r0 = r3.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x03e8 }
            java.lang.String r0 = r3.getString(r0)     // Catch:{ all -> 0x03e8 }
            android.content.Context r4 = r1.mContext     // Catch:{ all -> 0x03e8 }
            com.sec.internal.ims.cmstore.MessageStoreClient r8 = r1.mStoreClient     // Catch:{ all -> 0x03e8 }
            int r8 = r8.getClientID()     // Catch:{ all -> 0x03e8 }
            java.lang.String r4 = com.sec.internal.ims.cmstore.utils.Util.getSimCountryCode(r4, r8)     // Catch:{ all -> 0x03e8 }
            com.sec.ims.util.ImsUri r4 = com.sec.internal.ims.cmstore.utils.Util.getNormalizedTelUri(r0, r4)     // Catch:{ all -> 0x03e8 }
            java.lang.String r8 = r1.TAG     // Catch:{ all -> 0x03e8 }
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x03e8 }
            r10.<init>()     // Catch:{ all -> 0x03e8 }
            java.lang.String r12 = "participant = "
            r10.append(r12)     // Catch:{ all -> 0x03e8 }
            java.lang.String r12 = com.sec.internal.log.IMSLog.checker(r0)     // Catch:{ all -> 0x03e8 }
            r10.append(r12)     // Catch:{ all -> 0x03e8 }
            java.lang.String r12 = ", telUri = "
            r10.append(r12)     // Catch:{ all -> 0x03e8 }
            java.lang.String r12 = com.sec.internal.log.IMSLog.checker(r4)     // Catch:{ all -> 0x03e8 }
            r10.append(r12)     // Catch:{ all -> 0x03e8 }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x03e8 }
            android.util.Log.e(r8, r10)     // Catch:{ all -> 0x03e8 }
            if (r4 != 0) goto L_0x0384
            goto L_0x033a
        L_0x0384:
            boolean r8 = r7.contains(r4)     // Catch:{ all -> 0x03e8 }
            if (r8 == 0) goto L_0x03c5
            int r0 = r3.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x03e8 }
            int r0 = r3.getInt(r0)     // Catch:{ all -> 0x03e8 }
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r8 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.ACCEPTED     // Catch:{ all -> 0x03e8 }
            int r10 = r8.getId()     // Catch:{ all -> 0x03e8 }
            if (r0 == r10) goto L_0x03bc
            int r0 = r3.getColumnIndexOrThrow(r11)     // Catch:{ all -> 0x03e8 }
            int r0 = r3.getInt(r0)     // Catch:{ all -> 0x03e8 }
            android.content.ContentValues r10 = new android.content.ContentValues     // Catch:{ all -> 0x03e8 }
            r10.<init>()     // Catch:{ all -> 0x03e8 }
            int r8 = r8.getId()     // Catch:{ all -> 0x03e8 }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x03e8 }
            r10.put(r2, r8)     // Catch:{ all -> 0x03e8 }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r8 = r1.mBufferDbQuery     // Catch:{ all -> 0x03e8 }
            r12 = r5
            r34 = r6
            long r5 = (long) r0
            r8.updateRCSParticipantsDb(r5, r10)     // Catch:{ all -> 0x04e4 }
            goto L_0x03bf
        L_0x03bc:
            r12 = r5
            r34 = r6
        L_0x03bf:
            r7.remove(r4)     // Catch:{ all -> 0x04e4 }
            r4 = r33
            goto L_0x03e1
        L_0x03c5:
            r12 = r5
            r34 = r6
            int r4 = r3.getColumnIndexOrThrow(r11)     // Catch:{ all -> 0x04e4 }
            long r4 = r3.getLong(r4)     // Catch:{ all -> 0x04e4 }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r6 = r1.mBufferDbQuery     // Catch:{ all -> 0x04e4 }
            r6.deleteParticipantsFromBufferDb(r0, r13)     // Catch:{ all -> 0x04e4 }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r6 = r1.mBufferDbQuery     // Catch:{ all -> 0x04e4 }
            r6.deleteParticipantsUsingRowId(r4)     // Catch:{ all -> 0x04e4 }
            r4 = r33
            r4.add(r0)     // Catch:{ all -> 0x04e4 }
            r22 = 1
        L_0x03e1:
            r33 = r4
            r5 = r12
            r6 = r34
            goto L_0x033a
        L_0x03e8:
            r0 = move-exception
            r12 = r5
        L_0x03ea:
            r1 = r0
            goto L_0x04e7
        L_0x03ed:
            r12 = r5
            r34 = r6
            r4 = r33
            int r0 = r7.size()     // Catch:{ all -> 0x04e4 }
            if (r0 <= 0) goto L_0x04bc
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r1.mBufferDbQuery     // Catch:{ all -> 0x04e4 }
            java.util.ArrayList r0 = r0.insertNewParticipantToBufferDB(r7, r13)     // Catch:{ all -> 0x04e4 }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r2 = r1.mBufferDbQuery     // Catch:{ all -> 0x04e4 }
            android.database.Cursor r2 = r2.querySessionUsingChatId(r13)     // Catch:{ all -> 0x04e4 }
            int r5 = r2.getCount()     // Catch:{ all -> 0x04ae }
            if (r5 != 0) goto L_0x0466
            r5 = 1
            java.lang.Integer r6 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x04ae }
            r5 = r31
            r15.put(r5, r6)     // Catch:{ all -> 0x04ae }
            com.sec.internal.ims.cmstore.MessageStoreClient r5 = r1.mStoreClient     // Catch:{ all -> 0x04ae }
            java.lang.String r5 = r5.getCurrentIMSI()     // Catch:{ all -> 0x04ae }
            r6 = r28
            r15.put(r6, r5)     // Catch:{ all -> 0x04ae }
            r10 = r40
            java.lang.String r5 = r10.CONVERSATION_ID     // Catch:{ all -> 0x04ae }
            r6 = r27
            r15.put(r6, r5)     // Catch:{ all -> 0x04ae }
            java.lang.String r5 = r10.CONVERSATION_ID     // Catch:{ all -> 0x04ae }
            r8 = r26
            r15.put(r8, r5)     // Catch:{ all -> 0x04ae }
            r5 = r34
            java.lang.String r8 = r5.mEntity     // Catch:{ all -> 0x04ae }
            r15.put(r9, r8)     // Catch:{ all -> 0x04ae }
            com.sec.internal.ims.cmstore.MessageStoreClient r8 = r1.mStoreClient     // Catch:{ all -> 0x04ae }
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r8 = r8.getPrerenceManager()     // Catch:{ all -> 0x04ae }
            java.lang.String r8 = r8.getUserCtn()     // Catch:{ all -> 0x04ae }
            r14 = r25
            r15.put(r14, r8)     // Catch:{ all -> 0x04ae }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r8 = r1.mBufferDbQuery     // Catch:{ all -> 0x04ae }
            int r15 = r8.insertSessionFromBufferDbToRCSDb(r15, r0)     // Catch:{ all -> 0x04ae }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r1.mBufferDbQuery     // Catch:{ all -> 0x04ae }
            r0.updateBufferDbChatIdFromRcsDb(r13, r15)     // Catch:{ all -> 0x04ae }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r0 = r1.mBufferDbQuery     // Catch:{ all -> 0x04ae }
            r0.updateParticipantsIdFromRcsDb(r13)     // Catch:{ all -> 0x04ae }
            java.lang.String r0 = r1.TAG     // Catch:{ all -> 0x04ae }
            java.lang.String r8 = "Make new session for RCSIMFT DB"
            android.util.Log.d(r0, r8)     // Catch:{ all -> 0x04ae }
            if (r42 == 0) goto L_0x0461
            r1.updateMessageReceivedBeforeConfInfo(r13)     // Catch:{ all -> 0x04ae }
        L_0x0461:
            r32 = r15
            r15 = r24
            goto L_0x048d
        L_0x0466:
            r10 = r40
            r6 = r27
            r5 = r34
            java.util.stream.Stream r8 = r7.stream()     // Catch:{ all -> 0x04ae }
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler$$ExternalSyntheticLambda0 r14 = new com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler$$ExternalSyntheticLambda0     // Catch:{ all -> 0x04ae }
            r14.<init>()     // Catch:{ all -> 0x04ae }
            java.util.stream.Stream r8 = r8.map(r14)     // Catch:{ all -> 0x04ae }
            java.util.stream.Collector r14 = java.util.stream.Collectors.toSet()     // Catch:{ all -> 0x04ae }
            java.lang.Object r8 = r8.collect(r14)     // Catch:{ all -> 0x04ae }
            java.util.Collection r8 = (java.util.Collection) r8     // Catch:{ all -> 0x04ae }
            r15 = r24
            r15.addAll(r8)     // Catch:{ all -> 0x04ae }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r8 = r1.mBufferDbQuery     // Catch:{ all -> 0x04ae }
            r8.insertRCSParticipantsDb((java.util.ArrayList<android.content.ContentValues>) r0)     // Catch:{ all -> 0x04ae }
        L_0x048d:
            java.lang.String r0 = r1.TAG     // Catch:{ all -> 0x04ae }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x04ae }
            r8.<init>()     // Catch:{ all -> 0x04ae }
            java.lang.String r14 = "participants = "
            r8.append(r14)     // Catch:{ all -> 0x04ae }
            r8.append(r7)     // Catch:{ all -> 0x04ae }
            java.lang.String r7 = " are added into DB"
            r8.append(r7)     // Catch:{ all -> 0x04ae }
            java.lang.String r7 = r8.toString()     // Catch:{ all -> 0x04ae }
            android.util.Log.d(r0, r7)     // Catch:{ all -> 0x04ae }
            r2.close()     // Catch:{ all -> 0x04e4 }
            r29 = 1
            goto L_0x04c6
        L_0x04ae:
            r0 = move-exception
            r1 = r0
            if (r2 == 0) goto L_0x04bb
            r2.close()     // Catch:{ all -> 0x04b6 }
            goto L_0x04bb
        L_0x04b6:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ all -> 0x04e4 }
        L_0x04bb:
            throw r1     // Catch:{ all -> 0x04e4 }
        L_0x04bc:
            r10 = r40
            r15 = r24
            r6 = r27
            r5 = r34
            r29 = r22
        L_0x04c6:
            if (r3 == 0) goto L_0x04cb
            r3.close()     // Catch:{ all -> 0x04f3 }
        L_0x04cb:
            r0 = r16
            r8 = r20
        L_0x04cf:
            r33 = r4
            r2 = r5
            r25 = r11
            r22 = r12
            r4 = r13
            r24 = r15
            r11 = r29
            r7 = r30
            r5 = 0
            r12 = r6
            r13 = r9
        L_0x04e0:
            r9 = r21
            goto L_0x06d0
        L_0x04e4:
            r0 = move-exception
            goto L_0x03ea
        L_0x04e7:
            if (r3 == 0) goto L_0x04f2
            r3.close()     // Catch:{ all -> 0x04ed }
            goto L_0x04f2
        L_0x04ed:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ all -> 0x04f3 }
        L_0x04f2:
            throw r1     // Catch:{ all -> 0x04f3 }
        L_0x04f3:
            r0 = move-exception
            r1 = r0
            r22 = r12
            goto L_0x0741
        L_0x04f9:
            r0 = move-exception
            r1 = r0
            r22 = r5
            goto L_0x0741
        L_0x04ff:
            r36 = r4
            r22 = r5
            r30 = r10
            r33 = r12
            r5 = r15
            r15 = r25
            r13 = r26
            r12 = r27
            r10 = r40
            r4 = r2
            r2 = r6
            r6 = r28
            r25 = r11
            android.content.ContentValues r11 = new android.content.ContentValues     // Catch:{ all -> 0x073b }
            r11.<init>()     // Catch:{ all -> 0x073b }
            r26 = r3
            java.util.HashSet r3 = new java.util.HashSet     // Catch:{ all -> 0x073b }
            r3.<init>()     // Catch:{ all -> 0x073b }
            r28 = r6
            com.sec.internal.omanetapi.nms.data.Users r6 = r2.mUsers     // Catch:{ all -> 0x073b }
            com.sec.internal.omanetapi.nms.data.Users$User[] r6 = r6.mUser     // Catch:{ all -> 0x073b }
            r27 = r7
            int r7 = r6.length     // Catch:{ all -> 0x073b }
            r31 = r9
            r9 = 0
        L_0x052e:
            if (r9 >= r7) goto L_0x0575
            r32 = r7
            r7 = r6[r9]     // Catch:{ all -> 0x073b }
            r34 = r6
            java.lang.String r6 = r7.mEntity     // Catch:{ all -> 0x073b }
            r35 = r13
            r13 = r36
            boolean r6 = r6.equalsIgnoreCase(r13)     // Catch:{ all -> 0x073b }
            if (r6 == 0) goto L_0x0561
            boolean r6 = r7.mOwn     // Catch:{ all -> 0x073b }
            if (r6 == 0) goto L_0x056a
            com.sec.internal.omanetapi.nms.data.Users$User$Endpoint[] r6 = r7.mEndpoint     // Catch:{ all -> 0x073b }
            if (r6 == 0) goto L_0x056a
            r7 = 0
            r6 = r6[r7]     // Catch:{ all -> 0x073b }
            com.sec.internal.omanetapi.nms.data.Users$User$Endpoint$JoiningInfo r7 = r6.mJoingingInfo     // Catch:{ all -> 0x073b }
            if (r7 == 0) goto L_0x056a
            java.lang.String r7 = r7.mBy     // Catch:{ all -> 0x073b }
            boolean r7 = android.text.TextUtils.isEmpty(r7)     // Catch:{ all -> 0x073b }
            if (r7 != 0) goto L_0x056a
            com.sec.internal.omanetapi.nms.data.Users$User$Endpoint$JoiningInfo r6 = r6.mJoingingInfo     // Catch:{ all -> 0x073b }
            java.lang.String r6 = r6.mBy     // Catch:{ all -> 0x073b }
            r11.put(r8, r6)     // Catch:{ all -> 0x073b }
            goto L_0x056a
        L_0x0561:
            java.lang.String r6 = r7.mEntity     // Catch:{ all -> 0x073b }
            com.sec.ims.util.ImsUri r6 = com.sec.ims.util.ImsUri.parse(r6)     // Catch:{ all -> 0x073b }
            r3.add(r6)     // Catch:{ all -> 0x073b }
        L_0x056a:
            int r9 = r9 + 1
            r36 = r13
            r7 = r32
            r6 = r34
            r13 = r35
            goto L_0x052e
        L_0x0575:
            r35 = r13
            r13 = r36
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r6 = com.sec.internal.constants.ims.servicemodules.im.ChatMode.OFF     // Catch:{ all -> 0x073b }
            int r6 = r6.getId()     // Catch:{ all -> 0x073b }
            r7 = 1
            java.lang.String r6 = com.sec.internal.ims.util.StringIdGenerator.generateChatId(r3, r7, r6)     // Catch:{ all -> 0x073b }
            r7 = r30
            r11.put(r7, r6)     // Catch:{ all -> 0x073b }
            com.sec.internal.ims.cmstore.MessageStoreClient r8 = r1.mStoreClient     // Catch:{ all -> 0x073b }
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r8 = r8.getPrerenceManager()     // Catch:{ all -> 0x073b }
            java.lang.String r8 = r8.getUserCtn()     // Catch:{ all -> 0x073b }
            r11.put(r15, r8)     // Catch:{ all -> 0x073b }
            java.lang.String r8 = "is_group_chat"
            r36 = r13
            r9 = 1
            java.lang.Integer r13 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x073b }
            r11.put(r8, r13)     // Catch:{ all -> 0x073b }
            com.sec.internal.omanetapi.nms.data.ConferenceState r8 = r2.mConferenceState     // Catch:{ all -> 0x073b }
            boolean r8 = r8.mActivation     // Catch:{ all -> 0x073b }
            if (r8 == 0) goto L_0x05af
            com.sec.internal.constants.ims.servicemodules.im.ChatData$State r8 = com.sec.internal.constants.ims.servicemodules.im.ChatData.State.ACTIVE     // Catch:{ all -> 0x073b }
        L_0x05aa:
            int r8 = r8.getId()     // Catch:{ all -> 0x073b }
            goto L_0x05b2
        L_0x05af:
            com.sec.internal.constants.ims.servicemodules.im.ChatData$State r8 = com.sec.internal.constants.ims.servicemodules.im.ChatData.State.NONE     // Catch:{ all -> 0x073b }
            goto L_0x05aa
        L_0x05b2:
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x073b }
            r11.put(r4, r8)     // Catch:{ all -> 0x073b }
            com.sec.internal.omanetapi.nms.data.ConferenceDescription r4 = r2.mConferenceDescription     // Catch:{ all -> 0x073b }
            java.lang.String r4 = r4.mSubject     // Catch:{ all -> 0x073b }
            boolean r4 = android.text.TextUtils.isEmpty(r4)     // Catch:{ all -> 0x073b }
            if (r4 != 0) goto L_0x05ca
            com.sec.internal.omanetapi.nms.data.ConferenceDescription r4 = r2.mConferenceDescription     // Catch:{ all -> 0x073b }
            java.lang.String r4 = r4.mSubject     // Catch:{ all -> 0x073b }
            r11.put(r14, r4)     // Catch:{ all -> 0x073b }
        L_0x05ca:
            com.sec.internal.omanetapi.nms.data.ConferenceDescription r4 = r2.mConferenceDescription     // Catch:{ all -> 0x073b }
            com.sec.internal.omanetapi.nms.data.ConferenceDescription$Icon r4 = r4.mIcon     // Catch:{ all -> 0x073b }
            if (r4 == 0) goto L_0x0601
            com.sec.internal.omanetapi.nms.data.ConferenceDescription$Icon$FileInfo r4 = r4.mFileInfo     // Catch:{ all -> 0x073b }
            if (r4 == 0) goto L_0x0601
            java.lang.String r8 = r4.mContentType     // Catch:{ all -> 0x073b }
            java.lang.String r4 = r4.mData     // Catch:{ all -> 0x073b }
            if (r4 == 0) goto L_0x0601
            r9 = 0
            byte[] r4 = android.util.Base64.decode(r4, r9)     // Catch:{ all -> 0x073b }
            java.lang.String r8 = com.sec.internal.helper.translate.FileExtensionTranslator.translate(r8)     // Catch:{ all -> 0x073b }
            java.lang.String r8 = com.sec.internal.ims.cmstore.utils.Util.getRandomFileName(r8)     // Catch:{ all -> 0x073b }
            android.content.Context r9 = r1.mContext     // Catch:{ IOException -> 0x05fb }
            com.sec.internal.ims.cmstore.MessageStoreClient r13 = r1.mStoreClient     // Catch:{ IOException -> 0x05fb }
            int r13 = r13.getClientID()     // Catch:{ IOException -> 0x05fb }
            r14 = 0
            java.lang.String r9 = com.sec.internal.ims.cmstore.utils.Util.generateUniqueFilePath(r9, r8, r14, r13)     // Catch:{ IOException -> 0x05fb }
            com.sec.internal.ims.cmstore.utils.Util.saveFiletoPath(r4, r9)     // Catch:{ IOException -> 0x05fb }
            r11.put(r0, r9)     // Catch:{ IOException -> 0x05fb }
            goto L_0x05ff
        L_0x05fb:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x073b }
        L_0x05ff:
            r0 = r4
            goto L_0x0605
        L_0x0601:
            r0 = r16
            r8 = r20
        L_0x0605:
            r4 = 1
            java.lang.Integer r9 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x073b }
            r11.put(r5, r9)     // Catch:{ all -> 0x073b }
            java.lang.String r4 = "is_muted"
            r5 = 0
            java.lang.Integer r9 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x073b }
            r11.put(r4, r9)     // Catch:{ all -> 0x073b }
            java.lang.String r4 = "max_participants_count"
            com.sec.internal.omanetapi.nms.data.ConferenceDescription r9 = r2.mConferenceDescription     // Catch:{ all -> 0x073b }
            int r9 = r9.mMaxCount     // Catch:{ all -> 0x073b }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x073b }
            r11.put(r4, r9)     // Catch:{ all -> 0x073b }
            java.lang.String r4 = "imdn_notifications_availability"
            r9 = 1
            java.lang.Integer r13 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x073b }
            r11.put(r4, r13)     // Catch:{ all -> 0x073b }
            java.lang.String r4 = "direction"
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r13 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING     // Catch:{ all -> 0x073b }
            int r13 = r13.getId()     // Catch:{ all -> 0x073b }
            java.lang.Integer r13 = java.lang.Integer.valueOf(r13)     // Catch:{ all -> 0x073b }
            r11.put(r4, r13)     // Catch:{ all -> 0x073b }
            java.lang.String r4 = r10.CONVERSATION_ID     // Catch:{ all -> 0x073b }
            r11.put(r12, r4)     // Catch:{ all -> 0x073b }
            java.lang.String r4 = r10.CONVERSATION_ID     // Catch:{ all -> 0x073b }
            r13 = r35
            r11.put(r13, r4)     // Catch:{ all -> 0x073b }
            java.lang.String r4 = r2.mEntity     // Catch:{ all -> 0x073b }
            r13 = r31
            r11.put(r13, r4)     // Catch:{ all -> 0x073b }
            java.lang.String r4 = r2.mTimestamp     // Catch:{ all -> 0x073b }
            r14 = r27
            r11.put(r14, r4)     // Catch:{ all -> 0x073b }
            java.lang.String r4 = "linenum"
            com.sec.internal.ims.cmstore.MessageStoreClient r14 = r1.mStoreClient     // Catch:{ all -> 0x073b }
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r14 = r14.getPrerenceManager()     // Catch:{ all -> 0x073b }
            java.lang.String r14 = r14.getUserTelCtn()     // Catch:{ all -> 0x073b }
            r11.put(r4, r14)     // Catch:{ all -> 0x073b }
            com.sec.internal.ims.cmstore.MessageStoreClient r4 = r1.mStoreClient     // Catch:{ all -> 0x073b }
            java.lang.String r4 = r4.getCurrentIMSI()     // Catch:{ all -> 0x073b }
            r14 = r28
            r11.put(r14, r4)     // Catch:{ all -> 0x073b }
            com.sec.internal.ims.cmstore.MessageStoreClient r4 = r1.mStoreClient     // Catch:{ all -> 0x073b }
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r4 = r4.getPrerenceManager()     // Catch:{ all -> 0x073b }
            java.lang.String r4 = r4.getUserCtn()     // Catch:{ all -> 0x073b }
            r11.put(r15, r4)     // Catch:{ all -> 0x073b }
            java.lang.String r4 = "syncaction"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r14 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x073b }
            int r14 = r14.getId()     // Catch:{ all -> 0x073b }
            java.lang.Integer r14 = java.lang.Integer.valueOf(r14)     // Catch:{ all -> 0x073b }
            r11.put(r4, r14)     // Catch:{ all -> 0x073b }
            java.lang.String r4 = "syncdirection"
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r14 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice     // Catch:{ all -> 0x073b }
            int r14 = r14.getId()     // Catch:{ all -> 0x073b }
            java.lang.Integer r14 = java.lang.Integer.valueOf(r14)     // Catch:{ all -> 0x073b }
            r11.put(r4, r14)     // Catch:{ all -> 0x073b }
            java.lang.String r4 = r2.mCreatedBy     // Catch:{ all -> 0x073b }
            boolean r4 = android.text.TextUtils.isEmpty(r4)     // Catch:{ all -> 0x073b }
            if (r4 != 0) goto L_0x06ad
            java.lang.String r4 = r2.mCreatedBy     // Catch:{ all -> 0x073b }
            r14 = r26
            r11.put(r14, r4)     // Catch:{ all -> 0x073b }
        L_0x06ad:
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r1.mBufferDbQuery     // Catch:{ all -> 0x073b }
            r14 = 10
            r4.insertTable(r14, r11)     // Catch:{ all -> 0x073b }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r1.mBufferDbQuery     // Catch:{ all -> 0x073b }
            java.util.ArrayList r3 = r4.insertRCSParticipantToBufferDBUsingObject((java.util.Set<com.sec.ims.util.ImsUri>) r3, (java.lang.String) r6)     // Catch:{ all -> 0x073b }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r1.mBufferDbQuery     // Catch:{ all -> 0x073b }
            int r3 = r4.insertSessionFromBufferDbToRCSDb(r11, r3)     // Catch:{ all -> 0x073b }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r4 = r1.mBufferDbQuery     // Catch:{ all -> 0x073b }
            java.lang.String r4 = r4.updateBufferDbChatIdFromRcsDb(r6, r3)     // Catch:{ all -> 0x073b }
            com.sec.internal.ims.cmstore.querybuilders.RcsQueryBuilder r6 = r1.mBufferDbQuery     // Catch:{ all -> 0x073b }
            r6.updateParticipantsIdFromRcsDb(r4)     // Catch:{ all -> 0x073b }
            r32 = r3
            r11 = r9
            goto L_0x04e0
        L_0x06d0:
            if (r11 == 0) goto L_0x0735
            android.content.ContentValues r3 = new android.content.ContentValues     // Catch:{ all -> 0x073b }
            r3.<init>()     // Catch:{ all -> 0x073b }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r32)     // Catch:{ all -> 0x073b }
            r14 = r25
            r3.put(r14, r6)     // Catch:{ all -> 0x073b }
            r3.put(r7, r4)     // Catch:{ all -> 0x073b }
            java.lang.String r2 = r2.mEntity     // Catch:{ all -> 0x073b }
            r3.put(r13, r2)     // Catch:{ all -> 0x073b }
            java.lang.String r2 = r10.CONVERSATION_ID     // Catch:{ all -> 0x073b }
            r3.put(r12, r2)     // Catch:{ all -> 0x073b }
            if (r0 == 0) goto L_0x06f9
            java.lang.String r2 = "icon_name"
            r3.put(r2, r8)     // Catch:{ all -> 0x073b }
            java.lang.String r2 = "icon_data"
            r3.put(r2, r0)     // Catch:{ all -> 0x073b }
        L_0x06f9:
            boolean r2 = r33.isEmpty()     // Catch:{ all -> 0x073b }
            java.lang.String r4 = ","
            if (r2 != 0) goto L_0x070c
            java.lang.String r2 = "participants_del"
            r6 = r33
            java.lang.String r6 = android.text.TextUtils.join(r4, r6)     // Catch:{ all -> 0x073b }
            r3.put(r2, r6)     // Catch:{ all -> 0x073b }
        L_0x070c:
            boolean r2 = r24.isEmpty()     // Catch:{ all -> 0x073b }
            if (r2 != 0) goto L_0x071d
            java.lang.String r2 = "participants_add"
            r6 = r24
            java.lang.String r4 = android.text.TextUtils.join(r4, r6)     // Catch:{ all -> 0x073b }
            r3.put(r2, r4)     // Catch:{ all -> 0x073b }
        L_0x071d:
            if (r9 < 0) goto L_0x0728
            java.lang.String r2 = "closed_reason"
            java.lang.Integer r4 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x073b }
            r3.put(r2, r4)     // Catch:{ all -> 0x073b }
        L_0x0728:
            if (r42 == 0) goto L_0x0730
            java.util.Queue<android.content.ContentValues> r2 = r1.mSessionQueue     // Catch:{ all -> 0x073b }
            r2.add(r3)     // Catch:{ all -> 0x073b }
            goto L_0x0735
        L_0x0730:
            r2 = 10
            r1.notifyMsgAppFetchBuffer((android.content.ContentValues) r3, (int) r2)     // Catch:{ all -> 0x073b }
        L_0x0735:
            if (r22 == 0) goto L_0x075d
            r22.close()
            goto L_0x075d
        L_0x073b:
            r0 = move-exception
            goto L_0x0740
        L_0x073d:
            r0 = move-exception
            r22 = r5
        L_0x0740:
            r1 = r0
        L_0x0741:
            if (r22 == 0) goto L_0x074c
            r22.close()     // Catch:{ all -> 0x0747 }
            goto L_0x074c
        L_0x0747:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)
        L_0x074c:
            throw r1
        L_0x074d:
            r19 = r3
            r36 = r4
            r20 = r8
            r21 = r9
            r23 = r10
            r22 = r11
            r5 = 0
            r10 = r2
            r0 = r16
        L_0x075d:
            int r2 = r23 + 1
            r5 = r17
            r6 = r18
            r3 = r19
            r4 = r36
            r38 = r10
            r10 = r2
            r2 = r38
            goto L_0x0036
        L_0x076e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler.handleCloudNotifyConferenceInfo(com.sec.internal.ims.cmstore.params.ParamOMAObject, com.sec.internal.omanetapi.nms.data.Object, boolean):void");
    }

    /* JADX INFO: finally extract failed */
    public void handleCloudNotifyGSOChangedObj(ParamOMAObject paramOMAObject, Object object) {
        int i;
        int i2;
        String str;
        int i3;
        String[] strArr;
        String str2;
        int i4;
        Attribute[] attributeArr;
        Throwable th;
        String str3;
        String str4;
        Throwable th2;
        int i5;
        int i6;
        long j;
        Part part;
        boolean z;
        Object object2 = object;
        Log.d(this.TAG, "handleCloudNotifyGSOChangedObj(), objt is: " + object2);
        Attribute[] attributeArr2 = object2.attributes.attribute;
        int length = attributeArr2.length;
        int i7 = 0;
        int i8 = 0;
        String str5 = null;
        while (i8 < length) {
            Attribute attribute = attributeArr2[i8];
            String[] strArr2 = attribute.value;
            int length2 = strArr2.length;
            for (int i9 = i7; i9 < length2; i9++) {
                String str6 = strArr2[i9];
                Log.d(this.TAG, "Attribute key: " + attribute.name + ", value: " + str6);
            }
            String str7 = "subject";
            if (str7.equals(attribute.name)) {
                str5 = attribute.value[i7];
            }
            if (AttributeNames.textcontent.equalsIgnoreCase(attribute.name)) {
                String[] strArr3 = attribute.value;
                int length3 = strArr3.length;
                int i10 = i7;
                while (i10 < length3) {
                    GroupState parseGroupState = XmlParser.parseGroupState(strArr3[i10]);
                    Log.i(this.TAG, "GroupState after xmlParser: " + parseGroupState.toString());
                    parseGroupState.subject = str5;
                    ContentValues contentValues = new ContentValues();
                    if ("open".equalsIgnoreCase(parseGroupState.group_type)) {
                        contentValues.put(ImContract.ImSession.CHAT_TYPE, 1);
                    } else {
                        contentValues.put(ImContract.ImSession.CHAT_TYPE, 2);
                    }
                    contentValues.put(str7, parseGroupState.subject);
                    contentValues.put("session_uri", parseGroupState.lastfocussessionid);
                    Cursor queryAllSession = this.mBufferDbQuery.queryAllSession();
                    if (queryAllSession == null) {
                        if (queryAllSession != null) {
                            queryAllSession.close();
                        }
                        attributeArr = attributeArr2;
                        i4 = length;
                        i = i8;
                        str2 = str5;
                    } else {
                        try {
                            String str8 = parseGroupState.lastfocussessionid;
                            int indexOf = str8 != null ? str8.indexOf("@") : -1;
                            if (indexOf > 0) {
                                String substring = parseGroupState.lastfocussessionid.substring(i7, indexOf);
                                while (true) {
                                    str3 = "chat_id";
                                    if (!queryAllSession.moveToNext()) {
                                        attributeArr = attributeArr2;
                                        i4 = length;
                                        str2 = str5;
                                        str4 = null;
                                        break;
                                    }
                                    String string = queryAllSession.getString(queryAllSession.getColumnIndexOrThrow("session_uri"));
                                    attributeArr = attributeArr2;
                                    String str9 = this.TAG;
                                    i4 = length;
                                    StringBuilder sb = new StringBuilder();
                                    str2 = str5;
                                    sb.append("session uri: ");
                                    sb.append(string);
                                    Log.d(str9, sb.toString());
                                    if (string != null && string.toLowerCase().contains(substring.toLowerCase())) {
                                        str4 = queryAllSession.getString(queryAllSession.getColumnIndexOrThrow(str3));
                                        break;
                                    }
                                    attributeArr2 = attributeArr;
                                    length = i4;
                                    str5 = str2;
                                }
                                queryAllSession.close();
                                Log.i(this.TAG, "chat id: " + str4);
                                if (str4 != null) {
                                    this.mBufferDbQuery.updateSessionBufferDb(str4, contentValues);
                                    this.mBufferDbQuery.updateRCSSessionDb(str4, contentValues);
                                    contentValues.put(str3, str4);
                                    notifyMsgAppFetchBuffer(contentValues, 10);
                                    Cursor queryParticipantsUsingChatId = this.mBufferDbQuery.queryParticipantsUsingChatId(str4);
                                    String str10 = "uri";
                                    String str11 = "alias";
                                    if (queryParticipantsUsingChatId != null) {
                                        while (queryParticipantsUsingChatId.moveToNext()) {
                                            try {
                                                Part part2 = new Part();
                                                part2.name = queryParticipantsUsingChatId.getString(queryParticipantsUsingChatId.getColumnIndexOrThrow(str11));
                                                part2.comm_addr = queryParticipantsUsingChatId.getString(queryParticipantsUsingChatId.getColumnIndexOrThrow(str10));
                                                part2.role = queryParticipantsUsingChatId.getString(queryParticipantsUsingChatId.getColumnIndexOrThrow("type"));
                                                String[] strArr4 = strArr3;
                                                String simCountryCode = Util.getSimCountryCode(this.mContext, this.mStoreClient.getClientID());
                                                String telUri = Util.getTelUri(part2.comm_addr, simCountryCode);
                                                int i11 = length3;
                                                String msisdn = Util.getMsisdn(telUri, simCountryCode);
                                                String str12 = str7;
                                                Iterator<Part> it = parseGroupState.participantList.iterator();
                                                while (true) {
                                                    i5 = i8;
                                                    if (!it.hasNext()) {
                                                        i6 = i10;
                                                        j = 0;
                                                        part = null;
                                                        z = false;
                                                        break;
                                                    }
                                                    Iterator<Part> it2 = it;
                                                    part = it.next();
                                                    i6 = i10;
                                                    if (Util.getTelUri(part.comm_addr, simCountryCode).contains(msisdn)) {
                                                        j = queryParticipantsUsingChatId.getLong(queryParticipantsUsingChatId.getColumnIndexOrThrow("_id"));
                                                        parseGroupState.participantList.remove(part);
                                                        z = true;
                                                        break;
                                                    }
                                                    i10 = i6;
                                                    i8 = i5;
                                                    it = it2;
                                                }
                                                long j2 = j;
                                                String str13 = str10;
                                                String str14 = str3;
                                                long j3 = j2;
                                                String str15 = this.TAG;
                                                String str16 = str11;
                                                StringBuilder sb2 = new StringBuilder();
                                                GroupState groupState = parseGroupState;
                                                sb2.append("Participant: ");
                                                sb2.append(part2.toString());
                                                sb2.append(", telLine = ");
                                                sb2.append(IMSLog.checker(telUri));
                                                sb2.append(", line = ");
                                                sb2.append(IMSLog.checker(msisdn));
                                                sb2.append("isExist: ");
                                                sb2.append(z);
                                                sb2.append(", tempPart: ");
                                                sb2.append(part != null ? part.toString() : "");
                                                Log.i(str15, sb2.toString());
                                                if (!z) {
                                                    this.mBufferDbQuery.deleteParticipantsUsingRowId(queryParticipantsUsingChatId.getLong(queryParticipantsUsingChatId.getColumnIndexOrThrow("_id")));
                                                    this.mBufferDbQuery.deleteParticipantsFromBufferDb(part2.comm_addr, str4);
                                                } else if (part != null) {
                                                    ContentValues contentValues2 = new ContentValues();
                                                    String str17 = part.role;
                                                    if (str17 != null && str17.equalsIgnoreCase("Administrator")) {
                                                        String str18 = part.role;
                                                        ImParticipant.Type type = ImParticipant.Type.CHAIRMAN;
                                                        if (!str18.equals(String.valueOf(type.getId()))) {
                                                            contentValues2.put("type", Integer.valueOf(type.getId()));
                                                            this.mBufferDbQuery.updateRCSParticipantsDb(j3, contentValues2);
                                                            this.mBufferDbQuery.updateParticipantsBufferDb(part2.comm_addr, contentValues2);
                                                        }
                                                    }
                                                    contentValues2.put("type", Integer.valueOf(ImParticipant.Type.REGULAR.getId()));
                                                    this.mBufferDbQuery.updateRCSParticipantsDb(j3, contentValues2);
                                                    this.mBufferDbQuery.updateParticipantsBufferDb(part2.comm_addr, contentValues2);
                                                }
                                                strArr3 = strArr4;
                                                length3 = i11;
                                                str7 = str12;
                                                i10 = i6;
                                                i8 = i5;
                                                str10 = str13;
                                                str3 = str14;
                                                str11 = str16;
                                                parseGroupState = groupState;
                                            } catch (Throwable th3) {
                                                th2.addSuppressed(th3);
                                            }
                                        }
                                    }
                                    String str19 = str10;
                                    String str20 = str3;
                                    i = i8;
                                    strArr = strArr3;
                                    i3 = length3;
                                    str = str7;
                                    i2 = i10;
                                    GroupState groupState2 = parseGroupState;
                                    String str21 = str11;
                                    if (queryParticipantsUsingChatId != null) {
                                        queryParticipantsUsingChatId.close();
                                    }
                                    Iterator<Part> it3 = groupState2.participantList.iterator();
                                    while (it3.hasNext()) {
                                        Part next = it3.next();
                                        Log.d(this.TAG, "Insert participant : " + next.toString());
                                        ContentValues contentValues3 = new ContentValues();
                                        String str22 = next.role;
                                        if (str22 == null || !str22.equalsIgnoreCase("Administrator")) {
                                            contentValues3.put("type", Integer.valueOf(ImParticipant.Type.REGULAR.getId()));
                                            contentValues3.put("status", Integer.valueOf(ImParticipant.Status.ACCEPTED.getId()));
                                        } else {
                                            contentValues3.put("type", Integer.valueOf(ImParticipant.Type.CHAIRMAN.getId()));
                                            contentValues3.put("status", Integer.valueOf(ImParticipant.Status.INITIAL.getId()));
                                        }
                                        contentValues3.put(str21, next.name);
                                        contentValues3.put(str20, str4);
                                        contentValues3.put(str19, Util.getTelUri(next.comm_addr, Util.getSimCountryCode(this.mContext, this.mStoreClient.getClientID())));
                                        this.mBufferDbQuery.insertRCSParticipantsDb(contentValues3);
                                        contentValues3.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
                                        this.mBufferDbQuery.insertDeviceMsgToBuffer(2, contentValues3);
                                    }
                                } else {
                                    i = i8;
                                }
                            } else {
                                attributeArr = attributeArr2;
                                i4 = length;
                                i = i8;
                                str2 = str5;
                                strArr = strArr3;
                                i3 = length3;
                                str = str7;
                                i2 = i10;
                                queryAllSession.close();
                            }
                            i10 = i2 + 1;
                            attributeArr2 = attributeArr;
                            length = i4;
                            str5 = str2;
                            strArr3 = strArr;
                            length3 = i3;
                            str7 = str;
                            i8 = i;
                            i7 = 0;
                        } catch (Throwable th4) {
                            th.addSuppressed(th4);
                        }
                    }
                    strArr = strArr3;
                    i3 = length3;
                    str = str7;
                    i2 = i10;
                    i10 = i2 + 1;
                    attributeArr2 = attributeArr;
                    length = i4;
                    str5 = str2;
                    strArr3 = strArr;
                    length3 = i3;
                    str7 = str;
                    i8 = i;
                    i7 = 0;
                }
            }
            i8++;
            attributeArr2 = attributeArr2;
            length = length;
            str5 = str5;
            i7 = 0;
        }
        return;
        throw th;
        throw th2;
    }

    private int crossObjectSearchLegacy(ParamOMAObject paramOMAObject, String str, boolean z) {
        if (paramOMAObject.correlationTag == null && paramOMAObject.TEXT_CONTENT != null) {
            this.mSmsScheduler.updateCorrelationTagObject(paramOMAObject);
        }
        if (paramOMAObject.correlationTag != null && this.mSmsScheduler.handleCrossSearchObj(paramOMAObject, str, z)) {
            return 3;
        }
        if (paramOMAObject.correlationTag != null || paramOMAObject.correlationId == null) {
            return 1;
        }
        String str2 = paramOMAObject.TEXT_CONTENT;
        return ((str2 == null || str2.isEmpty()) && this.mMmsScheduler.handleCrossSearchObj(paramOMAObject, str, z)) ? 4 : 1;
    }

    public boolean isEmptySession() {
        return this.mSessionQueue.isEmpty();
    }

    public void handleNotifySessionToApp() {
        while (!this.mSessionQueue.isEmpty()) {
            notifyMsgAppFetchBuffer(this.mSessionQueue.peek(), 10);
            this.mSessionQueue.poll();
        }
    }

    private class RcsDbSessionObserver extends ContentObserver {
        public RcsDbSessionObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean z, Uri uri) {
            Throwable th;
            Throwable th2;
            Cursor queryBufferDBSessionByChatId;
            Throwable th3;
            String lastPathSegment = uri.getLastPathSegment();
            String r10 = RcsScheduler.this.TAG;
            Log.d(r10, "RcsDbSessionObserver chatId: " + lastPathSegment);
            ContentValues contentValues = new ContentValues();
            Cursor querySessionUsingChatId = RcsScheduler.this.mBufferDbQuery.querySessionUsingChatId(lastPathSegment);
            if (querySessionUsingChatId != null) {
                try {
                    if (querySessionUsingChatId.moveToFirst()) {
                        if (!TextUtils.equals(querySessionUsingChatId.getString(querySessionUsingChatId.getColumnIndexOrThrow("sim_imsi")), RcsScheduler.this.mStoreClient.getCurrentIMSI())) {
                            Log.d(RcsScheduler.this.TAG, "different sim imsi return");
                            querySessionUsingChatId.close();
                            return;
                        }
                        String string = querySessionUsingChatId.getString(querySessionUsingChatId.getColumnIndexOrThrow(ImContract.ImSession.ICON_PATH));
                        String string2 = querySessionUsingChatId.getString(querySessionUsingChatId.getColumnIndexOrThrow("conversation_id"));
                        String string3 = querySessionUsingChatId.getString(querySessionUsingChatId.getColumnIndexOrThrow("contribution_id"));
                        String r15 = RcsScheduler.this.TAG;
                        String str = "status";
                        StringBuilder sb = new StringBuilder();
                        String str2 = "conversation_id";
                        sb.append("onChange iconPath:  ");
                        sb.append(string);
                        Log.d(r15, sb.toString());
                        if (!TextUtils.isEmpty(string)) {
                            String string4 = querySessionUsingChatId.getString(querySessionUsingChatId.getColumnIndexOrThrow(ImContract.ImSession.ICON_PARTICIPANT));
                            String string5 = querySessionUsingChatId.getString(querySessionUsingChatId.getColumnIndexOrThrow(ImContract.ImSession.ICON_TIMESTAMP));
                            contentValues.put(ImContract.ImSession.ICON_PATH, string);
                            contentValues.put(ImContract.ImSession.ICON_PARTICIPANT, string4);
                            contentValues.put(ImContract.ImSession.ICON_TIMESTAMP, string5);
                        }
                        queryBufferDBSessionByChatId = RcsScheduler.this.mBufferDbQuery.queryBufferDBSessionByChatId(lastPathSegment);
                        if (queryBufferDBSessionByChatId != null) {
                            if (queryBufferDBSessionByChatId.moveToFirst()) {
                                String string6 = queryBufferDBSessionByChatId.getString(queryBufferDBSessionByChatId.getColumnIndexOrThrow("session_uri"));
                                String r4 = RcsScheduler.this.TAG;
                                Log.d(r4, "on Change existiongSessionUri: " + string6);
                                if (TextUtils.isEmpty(string6)) {
                                    String string7 = querySessionUsingChatId.getString(querySessionUsingChatId.getColumnIndexOrThrow("session_uri"));
                                    String r42 = RcsScheduler.this.TAG;
                                    Log.d(r42, "onChange sessionUri: " + string7);
                                    if (!TextUtils.isEmpty(string7)) {
                                        contentValues.put("is_group_chat", 1);
                                        contentValues.put("session_uri", string7);
                                    }
                                }
                            }
                        }
                        if (queryBufferDBSessionByChatId != null) {
                            queryBufferDBSessionByChatId.close();
                        }
                        contentValues.put(ImContract.ImSession.INSERTED_TIMESTAMP, Long.valueOf(querySessionUsingChatId.getLong(querySessionUsingChatId.getColumnIndexOrThrow(ImContract.ImSession.INSERTED_TIMESTAMP))));
                        contentValues.put("contribution_id", string3);
                        contentValues.put(str2, string2);
                        String str3 = str;
                        contentValues.put(str3, Integer.valueOf(querySessionUsingChatId.getInt(querySessionUsingChatId.getColumnIndexOrThrow(str3))));
                        RcsScheduler.this.mBufferDbQuery.updateSessionBufferDb(lastPathSegment, contentValues);
                    }
                } catch (Throwable th4) {
                    Throwable th5 = th4;
                    try {
                        querySessionUsingChatId.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                    throw th5;
                }
            }
            if (querySessionUsingChatId != null) {
                querySessionUsingChatId.close();
            }
            HashSet hashSet = new HashSet();
            Cursor queryParticipantsUsingChatId = RcsScheduler.this.mBufferDbQuery.queryParticipantsUsingChatId(lastPathSegment);
            if (queryParticipantsUsingChatId != null) {
                while (queryParticipantsUsingChatId.moveToNext()) {
                    try {
                        hashSet.add(ImsUri.parse(queryParticipantsUsingChatId.getString(queryParticipantsUsingChatId.getColumnIndex("uri"))));
                    } catch (Throwable th7) {
                        th2.addSuppressed(th7);
                    }
                }
            }
            if (queryParticipantsUsingChatId != null) {
                queryParticipantsUsingChatId.close();
            }
            Cursor queryParticipantsFromBufferDb = RcsScheduler.this.mBufferDbQuery.queryParticipantsFromBufferDb(lastPathSegment);
            if (queryParticipantsFromBufferDb != null) {
                while (queryParticipantsFromBufferDb.moveToNext()) {
                    try {
                        String string8 = queryParticipantsFromBufferDb.getString(queryParticipantsFromBufferDb.getColumnIndexOrThrow("uri"));
                        RcsScheduler rcsScheduler = RcsScheduler.this;
                        ImsUri normalizedTelUri = Util.getNormalizedTelUri(string8, Util.getSimCountryCode(rcsScheduler.mContext, rcsScheduler.mStoreClient.getClientID()));
                        String r6 = RcsScheduler.this.TAG;
                        Log.e(r6, "participant = " + IMSLog.checker(string8) + ", telUri = " + IMSLog.checker(normalizedTelUri));
                        if (normalizedTelUri != null) {
                            if (hashSet.contains(normalizedTelUri)) {
                                hashSet.remove(normalizedTelUri);
                            } else {
                                String r5 = RcsScheduler.this.TAG;
                                Log.d(r5, "remove participant" + IMSLog.checker(string8));
                                RcsScheduler.this.mBufferDbQuery.deleteParticipantsFromBufferDb(string8, lastPathSegment);
                            }
                        }
                    } catch (Throwable th8) {
                        th.addSuppressed(th8);
                    }
                }
            }
            if (hashSet.size() > 0) {
                Log.d(RcsScheduler.this.TAG, "insert new participant");
                RcsScheduler.this.mBufferDbQuery.insertNewParticipantToBufferDB(hashSet, lastPathSegment);
            }
            if (queryParticipantsFromBufferDb != null) {
                queryParticipantsFromBufferDb.close();
                return;
            }
            return;
            throw th;
            throw th2;
            throw th3;
        }
    }

    private void registerRcsDbSessionObserver(Looper looper) {
        if (this.mRcsDbSessionObserver == null) {
            this.mRcsDbSessionObserver = new RcsDbSessionObserver(this);
            this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.samsung.rcs.cmstore/chat/"), true, this.mRcsDbSessionObserver);
        }
    }

    public void updateMessageReceivedBeforeConfInfo(String str) {
        Log.d(this.TAG, "updateMessageReceivedBeforeConfInfo");
        Cursor searchIMFTBufferUsingChatId = this.mBufferDbQuery.searchIMFTBufferUsingChatId(str);
        if (searchIMFTBufferUsingChatId != null) {
            try {
                if (searchIMFTBufferUsingChatId.moveToFirst()) {
                    do {
                        String string = searchIMFTBufferUsingChatId.getString(searchIMFTBufferUsingChatId.getColumnIndexOrThrow("imdn_message_id"));
                        this.mBufferDbQuery.queryImdnBufferDBandUpdateRcsMessageBufferDb(string, str);
                        long j = searchIMFTBufferUsingChatId.getLong(searchIMFTBufferUsingChatId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                        int i = searchIMFTBufferUsingChatId.getInt(searchIMFTBufferUsingChatId.getColumnIndexOrThrow("direction"));
                        boolean z = true;
                        if (searchIMFTBufferUsingChatId.getInt(searchIMFTBufferUsingChatId.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER)) != 1) {
                            z = false;
                        }
                        this.mBufferDbQuery.queryBufferDbandUpdateRcsMessageDb(string);
                        String str2 = this.TAG;
                        Log.i(str2, "updateMessageReceivedBeforeConfInfo bufferDbId: " + j + ", direction: " + i);
                        if (i == ImDirection.OUTGOING.getId()) {
                            notifyMsgAppCldNotification(CloudMessageProviderContract.ApplicationTypes.MSGDATA, z ? CloudMessageProviderContract.DataTypes.RCS_IMDN_FT : CloudMessageProviderContract.DataTypes.RCS_IMDN_CHAT, j, false);
                        }
                    } while (searchIMFTBufferUsingChatId.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (searchIMFTBufferUsingChatId != null) {
            searchIMFTBufferUsingChatId.close();
            return;
        }
        return;
        throw th;
    }

    public void onUpdateCmsConfig() {
        this.mBufferDbQuery.onUpdateCmsConfigInitSyncDataTtl();
    }
}
