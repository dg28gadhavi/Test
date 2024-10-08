package com.sec.internal.ims.cmstore.syncschedulers;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Looper;
import android.text.TextUtils;
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
import com.sec.internal.ims.cmstore.params.DeviceLegacyUpdateParam;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUriParam;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.interfaces.ims.cmstore.ITelephonyDBColumns;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.DeletedObject;
import java.net.URL;
import java.util.ArrayList;

public class MmsScheduler extends BaseMessagingScheduler {
    private String TAG = MmsScheduler.class.getSimpleName();
    protected final MmsQueryBuilder mBufferDbQuery;
    private final MultiLineScheduler mMultiLineScheduler;

    public MmsScheduler(MessageStoreClient messageStoreClient, CloudMessageBufferDBEventSchedulingRule cloudMessageBufferDBEventSchedulingRule, SummaryQueryBuilder summaryQueryBuilder, MultiLineScheduler multiLineScheduler, IDeviceDataChangeListener iDeviceDataChangeListener, IBufferDBEventListener iBufferDBEventListener, Looper looper) {
        super(messageStoreClient, cloudMessageBufferDBEventSchedulingRule, iDeviceDataChangeListener, iBufferDBEventListener, looper, summaryQueryBuilder);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mBufferDbQuery = new MmsQueryBuilder(messageStoreClient, iBufferDBEventListener);
        this.mMultiLineScheduler = multiLineScheduler;
        this.mDbTableContractIndex = 4;
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x0149 A[SYNTHETIC, Splitter:B:42:0x0149] */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0151 A[SYNTHETIC, Splitter:B:47:0x0151] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleObjectMMSCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject r20) {
        /*
            r19 = this;
            r0 = r19
            r7 = r20
            java.lang.String r1 = "read"
            java.lang.String r2 = "syncdirection"
            java.lang.String r3 = "syncaction"
            java.lang.String r4 = r0.TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "handleObjectMMSCloudSearch: "
            r5.append(r6)
            java.lang.String r6 = r7.correlationId
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
            r8 = -1
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r4 = r0.mBufferDbQuery     // Catch:{ NullPointerException -> 0x015b }
            java.lang.String r5 = r7.correlationId     // Catch:{ NullPointerException -> 0x015b }
            android.database.Cursor r11 = r4.searchMMsPduBufferUsingCorrelationId(r5)     // Catch:{ NullPointerException -> 0x015b }
            r10 = 4
            if (r11 == 0) goto L_0x0130
            boolean r4 = r11.moveToFirst()     // Catch:{ all -> 0x014d }
            if (r4 == 0) goto L_0x0130
            java.lang.String r4 = "_bufferdbid"
            int r4 = r11.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x014d }
            int r4 = r11.getInt(r4)     // Catch:{ all -> 0x014d }
            long r8 = (long) r4
            int r4 = r11.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x012d }
            int r4 = r11.getInt(r4)     // Catch:{ all -> 0x012d }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r17 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r4)     // Catch:{ all -> 0x012d }
            int r4 = r11.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x012d }
            int r4 = r11.getInt(r4)     // Catch:{ all -> 0x012d }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r16 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r4)     // Catch:{ all -> 0x012d }
            android.content.ContentValues r4 = new android.content.ContentValues     // Catch:{ all -> 0x012d }
            r4.<init>()     // Catch:{ all -> 0x012d }
            java.lang.String r5 = "lastmodseq"
            java.lang.Long r6 = r7.lastModSeq     // Catch:{ all -> 0x012d }
            r4.put(r5, r6)     // Catch:{ all -> 0x012d }
            java.lang.String r5 = "res_url"
            java.net.URL r6 = r7.resourceURL     // Catch:{ all -> 0x012d }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x012d }
            java.lang.String r6 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r6)     // Catch:{ all -> 0x012d }
            r4.put(r5, r6)     // Catch:{ all -> 0x012d }
            java.lang.String r5 = "parentfolder"
            java.net.URL r6 = r7.parentFolder     // Catch:{ all -> 0x012d }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x012d }
            java.lang.String r6 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r6)     // Catch:{ all -> 0x012d }
            r4.put(r5, r6)     // Catch:{ all -> 0x012d }
            java.lang.String r5 = "path"
            java.lang.String r6 = r7.path     // Catch:{ all -> 0x012d }
            java.lang.String r6 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r6)     // Catch:{ all -> 0x012d }
            r4.put(r5, r6)     // Catch:{ all -> 0x012d }
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r5 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x012d }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done     // Catch:{ all -> 0x012d }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r12 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None     // Catch:{ all -> 0x012d }
            r5.<init>(r6, r12)     // Catch:{ all -> 0x012d }
            r6 = 0
            r5.mIsChanged = r6     // Catch:{ all -> 0x012d }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r6 = r7.mFlag     // Catch:{ all -> 0x012d }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r12 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x012d }
            boolean r6 = r6.equals(r12)     // Catch:{ all -> 0x012d }
            r13 = 1
            if (r6 == 0) goto L_0x00ae
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice     // Catch:{ all -> 0x014d }
            r5.setIsChangedActionAndDirection(r13, r12, r1)     // Catch:{ all -> 0x014d }
            goto L_0x00e1
        L_0x00ae:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = r7.mFlag     // Catch:{ all -> 0x012d }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x012d }
            boolean r5 = r5.equals(r6)     // Catch:{ all -> 0x012d }
            if (r5 == 0) goto L_0x00d5
            int r5 = r11.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x014d }
            int r5 = r11.getInt(r5)     // Catch:{ all -> 0x014d }
            if (r5 != 0) goto L_0x00c9
            java.lang.Integer r5 = java.lang.Integer.valueOf(r13)     // Catch:{ all -> 0x014d }
            r4.put(r1, r5)     // Catch:{ all -> 0x014d }
        L_0x00c9:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r12 = r0.mScheduleRule     // Catch:{ all -> 0x014d }
            int r13 = r0.mDbTableContractIndex     // Catch:{ all -> 0x014d }
            r14 = r8
            r18 = r6
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r1 = r12.getSetFlagsForCldOperation(r13, r14, r16, r17, r18)     // Catch:{ all -> 0x014d }
            goto L_0x00e0
        L_0x00d5:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r12 = r0.mScheduleRule     // Catch:{ all -> 0x012d }
            int r13 = r0.mDbTableContractIndex     // Catch:{ all -> 0x012d }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r18 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x012d }
            r14 = r8
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r1 = r12.getSetFlagsForCldOperation(r13, r14, r16, r17, r18)     // Catch:{ all -> 0x012d }
        L_0x00e0:
            r5 = r1
        L_0x00e1:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r1 = r0.mSummaryDB     // Catch:{ all -> 0x012d }
            r1.insertSummaryDbUsingObjectIfNonExist(r7, r10)     // Catch:{ all -> 0x012d }
            boolean r1 = r5.mIsChanged     // Catch:{ all -> 0x012d }
            if (r1 == 0) goto L_0x0121
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = r5.mAction     // Catch:{ all -> 0x012d }
            int r1 = r1.getId()     // Catch:{ all -> 0x012d }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x012d }
            r4.put(r3, r1)     // Catch:{ all -> 0x012d }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = r5.mDirection     // Catch:{ all -> 0x012d }
            int r1 = r1.getId()     // Catch:{ all -> 0x012d }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x012d }
            r4.put(r2, r1)     // Catch:{ all -> 0x012d }
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r0.mBufferDbQuery     // Catch:{ all -> 0x012d }
            r0.updateQueryTable(r4, r8, r1)     // Catch:{ all -> 0x012d }
            r6 = 4
            r10 = 0
            boolean r12 = r7.mIsGoforwardSync     // Catch:{ all -> 0x012d }
            java.lang.String r13 = r7.mLine     // Catch:{ all -> 0x012d }
            r14 = 0
            r15 = 0
            r1 = r19
            r2 = r5
            r3 = r8
            r5 = r6
            r6 = r10
            r7 = r12
            r9 = r8
            r8 = r13
            r12 = r9
            r9 = r14
            r10 = r15
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9, r10)     // Catch:{ all -> 0x0129 }
            goto L_0x0127
        L_0x0121:
            r12 = r8
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r0.mBufferDbQuery     // Catch:{ all -> 0x0129 }
            r0.updateQueryTable(r4, r12, r1)     // Catch:{ all -> 0x0129 }
        L_0x0127:
            r8 = r12
            goto L_0x0147
        L_0x0129:
            r0 = move-exception
            r1 = r0
            r8 = r12
            goto L_0x014f
        L_0x012d:
            r0 = move-exception
            r12 = r8
            goto L_0x014e
        L_0x0130:
            java.lang.String r1 = r0.TAG     // Catch:{ all -> 0x014d }
            java.lang.String r2 = "handleObjectMMSCloudSearch: MMS not found"
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x014d }
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r0.mBufferDbQuery     // Catch:{ all -> 0x014d }
            r3 = 0
            r4 = 0
            r6 = 1
            r2 = r20
            r1.insertMMSUsingObject(r2, r3, r4, r6)     // Catch:{ all -> 0x014d }
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r0.mSummaryDB     // Catch:{ all -> 0x014d }
            r0.insertSummaryDbUsingObjectIfNonExist(r7, r10)     // Catch:{ all -> 0x014d }
        L_0x0147:
            if (r11 == 0) goto L_0x015f
            r11.close()     // Catch:{ NullPointerException -> 0x015b }
            goto L_0x015f
        L_0x014d:
            r0 = move-exception
        L_0x014e:
            r1 = r0
        L_0x014f:
            if (r11 == 0) goto L_0x015a
            r11.close()     // Catch:{ all -> 0x0155 }
            goto L_0x015a
        L_0x0155:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ NullPointerException -> 0x015b }
        L_0x015a:
            throw r1     // Catch:{ NullPointerException -> 0x015b }
        L_0x015b:
            r0 = move-exception
            r0.printStackTrace()
        L_0x015f:
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler.handleObjectMMSCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject):long");
    }

    public long handleNormalSyncObjectMmsDownload(ParamOMAObject paramOMAObject, boolean z) {
        Cursor searchMMsPduBufferUsingCorrelationId;
        Throwable th;
        ParamOMAObject paramOMAObject2 = paramOMAObject;
        String str = this.TAG;
        Log.i(str, "handleNormalSyncObjectMmsDownload: " + paramOMAObject2);
        long j = -1;
        try {
            searchMMsPduBufferUsingCorrelationId = this.mBufferDbQuery.searchMMsPduBufferUsingCorrelationId(paramOMAObject2.correlationId);
            String lineTelUriFromObjUrl = Util.getLineTelUriFromObjUrl(paramOMAObject2.resourceURL.toString());
            if (searchMMsPduBufferUsingCorrelationId == null || !searchMMsPduBufferUsingCorrelationId.moveToFirst()) {
                Log.i(this.TAG, "handleNormalSyncObjectMmsDownload: MMS not found");
                ParamSyncFlagsSet insertMMSUsingObject = this.mBufferDbQuery.insertMMSUsingObject(paramOMAObject, false, 0, false);
                if (("OUT".equalsIgnoreCase(paramOMAObject2.DIRECTION) || ("IN".equalsIgnoreCase(paramOMAObject2.DIRECTION) && Util.isDownloadObject(paramOMAObject2.DATE, this.mStoreClient, 4))) && !CloudMessageBufferDBConstants.ActionStatusFlag.Delete.equals(paramOMAObject2.mFlag) && !paramOMAObject2.mIsGoforwardSync) {
                    BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
                    bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(4, insertMMSUsingObject.mBufferId, z, lineTelUriFromObjUrl, insertMMSUsingObject.mAction, this.mStoreClient));
                    if (insertMMSUsingObject.mBufferId > 0 && insertMMSUsingObject.mDirection.equals(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice)) {
                        if (!this.isCmsEnabled || !insertMMSUsingObject.mAction.equals(CloudMessageBufferDBConstants.ActionStatusFlag.FetchForce)) {
                            handleOutPutParamSyncFlagSet(insertMMSUsingObject, insertMMSUsingObject.mBufferId, 4, false, z, lineTelUriFromObjUrl, bufferDBChangeParamList, false);
                        } else {
                            this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(bufferDBChangeParamList);
                        }
                    }
                }
            } else {
                j = (long) searchMMsPduBufferUsingCorrelationId.getInt(searchMMsPduBufferUsingCorrelationId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                long j2 = (long) searchMMsPduBufferUsingCorrelationId.getInt(searchMMsPduBufferUsingCorrelationId.getColumnIndexOrThrow("_id"));
                CloudMessageBufferDBConstants.ActionStatusFlag valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(searchMMsPduBufferUsingCorrelationId.getInt(searchMMsPduBufferUsingCorrelationId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
                CloudMessageBufferDBConstants.DirectionFlag valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(searchMMsPduBufferUsingCorrelationId.getInt(searchMMsPduBufferUsingCorrelationId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
                ContentValues contentValues = new ContentValues();
                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, paramOMAObject2.lastModSeq);
                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject2.resourceURL.toString()));
                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(paramOMAObject2.parentFolder.toString()));
                contentValues.put("path", Util.decodeUrlFromServer(paramOMAObject2.path));
                if (searchMMsPduBufferUsingCorrelationId.getInt(searchMMsPduBufferUsingCorrelationId.getColumnIndexOrThrow("read")) == 1) {
                    valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                    valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
                }
                CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag = valueOf;
                ParamSyncFlagsSet setFlagsForCldOperation = this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, j, valueOf2, actionStatusFlag, paramOMAObject2.mFlag);
                if (setFlagsForCldOperation.mIsChanged) {
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(setFlagsForCldOperation.mDirection.getId()));
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(setFlagsForCldOperation.mAction.getId()));
                }
                updateQueryTable(contentValues, j, this.mBufferDbQuery);
                if (j2 > 0) {
                    handleOutPutParamSyncFlagSet(setFlagsForCldOperation, j, 4, false, z, lineTelUriFromObjUrl, (BufferDBChangeParamList) null, false);
                }
            }
            if (searchMMsPduBufferUsingCorrelationId != null) {
                searchMMsPduBufferUsingCorrelationId.close();
            }
        } catch (NullPointerException e) {
            Log.e(this.TAG, e.toString());
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
        return j;
        throw th;
    }

    public void addMmsPartDownloadList(BufferDBChangeParamList bufferDBChangeParamList, long j, String str, boolean z) {
        Cursor queryOneMmsUndownloadedParts = queryOneMmsUndownloadedParts(j);
        if (queryOneMmsUndownloadedParts != null) {
            try {
                if (queryOneMmsUndownloadedParts.moveToFirst()) {
                    do {
                        bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(6, queryOneMmsUndownloadedParts.getLong(queryOneMmsUndownloadedParts.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID)), z, str, this.mStoreClient));
                    } while (queryOneMmsUndownloadedParts.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryOneMmsUndownloadedParts != null) {
            queryOneMmsUndownloadedParts.close();
            return;
        }
        return;
        throw th;
    }

    public void addMmsPartDownloadListForFtUri(BufferDBChangeParamList bufferDBChangeParamList, long j, String str, boolean z) {
        Cursor queryOneMmsUndownloadedParts = queryOneMmsUndownloadedParts(j);
        if (queryOneMmsUndownloadedParts != null) {
            try {
                if (queryOneMmsUndownloadedParts.moveToFirst()) {
                    String str2 = this.TAG;
                    Log.i(str2, "addMmsPartDownloadListForFtUri pduid: " + j + " count:" + queryOneMmsUndownloadedParts.getCount());
                    do {
                        queryOneMmsUndownloadedParts.getLong(queryOneMmsUndownloadedParts.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                        String string = queryOneMmsUndownloadedParts.getString(queryOneMmsUndownloadedParts.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpart.CT));
                        if (!ITelephonyDBColumns.xml_smil_type.equalsIgnoreCase(string) && !MIMEContentType.PLAIN_TEXT.equalsIgnoreCase(string)) {
                            bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(4, j, z, str, this.mStoreClient));
                        }
                    } while (queryOneMmsUndownloadedParts.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryOneMmsUndownloadedParts != null) {
            queryOneMmsUndownloadedParts.close();
            return;
        }
        return;
        throw th;
    }

    public void onNmsEventDeletedObjMmsBufferDbAvailableUsingCorrId(Cursor cursor, DeletedObject deletedObject, boolean z) {
        onNmsEventDeletedObjMMSBufferDbAvailable(cursor, deletedObject, z, true);
    }

    public void onNmsEventChangedObjMmsBufferDbAvailableUsingCorrId(Cursor cursor, ChangedObject changedObject, boolean z) {
        onNmsEventChangedObjMMSBufferDbAvailable(cursor, changedObject, z);
    }

    public void onNmsEventDeletedObjBufferDbMmsAvailableUsingUrl(Cursor cursor, DeletedObject deletedObject, boolean z) {
        onNmsEventDeletedObjMMSBufferDbAvailable(cursor, deletedObject, z, false);
    }

    public void onNmsEventChangedObjBufferDbMmsAvailableUsingUrl(Cursor cursor, ChangedObject changedObject, boolean z) {
        onNmsEventChangedObjMMSBufferDbAvailable(cursor, changedObject, z);
    }

    private void onNmsEventChangedObjMMSBufferDbAvailable(Cursor cursor, ChangedObject changedObject, boolean z) {
        Cursor cursor2 = cursor;
        ChangedObject changedObject2 = changedObject;
        long j = (long) cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long j2 = (long) cursor2.getInt(cursor2.getColumnIndexOrThrow("_id"));
        String string = cursor2.getString(cursor2.getColumnIndexOrThrow("linenum"));
        CloudMessageBufferDBConstants.ActionStatusFlag valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, changedObject2.lastModSeq);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(changedObject2.resourceURL.toString()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(changedObject2.parentFolder.toString()));
        CloudMessageBufferDBConstants.ActionStatusFlag cloudActionPerFlag = this.mBufferDbQuery.getCloudActionPerFlag(changedObject2.flags);
        if (CloudMessageBufferDBConstants.ActionStatusFlag.Update.equals(cloudActionPerFlag)) {
            contentValues.put("read", 1);
        }
        String str = string;
        ContentValues contentValues2 = contentValues;
        long j3 = j2;
        String str2 = CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION;
        ParamSyncFlagsSet setFlagsForCldOperationForCms = this.isCmsEnabled ? this.mScheduleRule.getSetFlagsForCldOperationForCms(this.mDbTableContractIndex, j, valueOf2, valueOf, cloudActionPerFlag) : this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, j, valueOf2, valueOf, cloudActionPerFlag);
        if (setFlagsForCldOperationForCms.mIsChanged) {
            contentValues2.put(str2, Integer.valueOf(setFlagsForCldOperationForCms.mDirection.getId()));
            contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(setFlagsForCldOperationForCms.mAction.getId()));
        }
        updateQueryTable(contentValues2, j, this.mBufferDbQuery);
        if (j3 > 0) {
            handleOutPutParamSyncFlagSet(setFlagsForCldOperationForCms, j, 4, false, z, str, (BufferDBChangeParamList) null, false);
        }
    }

    private void onNmsEventDeletedObjMMSBufferDbAvailable(Cursor cursor, DeletedObject deletedObject, boolean z, boolean z2) {
        Cursor cursor2 = cursor;
        DeletedObject deletedObject2 = deletedObject;
        long j = (long) cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long j2 = (long) cursor2.getInt(cursor2.getColumnIndexOrThrow("_id"));
        String string = cursor2.getString(cursor2.getColumnIndexOrThrow("linenum"));
        CloudMessageBufferDBConstants.ActionStatusFlag valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, Long.valueOf(deletedObject2.lastModSeq));
        if (z2) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(deletedObject2.resourceURL.toString()));
        }
        CloudMessageBufferDBEventSchedulingRule cloudMessageBufferDBEventSchedulingRule = this.mScheduleRule;
        int i = this.mDbTableContractIndex;
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
        String str = string;
        String str2 = CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION;
        ParamSyncFlagsSet setFlagsForCldOperation = cloudMessageBufferDBEventSchedulingRule.getSetFlagsForCldOperation(i, j, valueOf2, valueOf, actionStatusFlag);
        if (setFlagsForCldOperation.mIsChanged) {
            contentValues.put(str2, Integer.valueOf(setFlagsForCldOperation.mDirection.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(setFlagsForCldOperation.mAction.getId()));
        }
        updateQueryTable(contentValues, j, this.mBufferDbQuery);
        if (j2 > 0) {
            handleOutPutParamSyncFlagSet(setFlagsForCldOperation, j, 4, false, z, str, (BufferDBChangeParamList) null, false);
        }
    }

    public Cursor queryOneMmsUndownloadedParts(long j) {
        String str = this.TAG;
        Log.i(str, "queryOneMmsUndownloadedParts: " + j);
        return this.mBufferDbQuery.queryUndownloadedPart(j);
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x00dc A[Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onMmsAllPayloadsDownloadFromMcs(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r13) {
        /*
            r12 = this;
            java.lang.String r0 = "Content-ID"
            java.lang.String r1 = ";"
            android.content.ContentValues r2 = new android.content.ContentValues
            r2.<init>()
            java.util.List r3 = r13.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            int r3 = r3.size()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            r4 = 1
            if (r3 < r4) goto L_0x00f9
            r3 = 0
            r5 = r3
        L_0x0016:
            java.util.List r6 = r13.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            int r6 = r6.size()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            if (r5 >= r6) goto L_0x00f9
            java.util.List r6 = r13.getAllPayloads()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.Object r6 = r6.get(r5)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            javax.mail.BodyPart r6 = (javax.mail.BodyPart) r6     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String r7 = r6.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String[] r7 = r7.split(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            r7 = r7[r3]     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String r8 = "multipart/related"
            boolean r8 = r7.equalsIgnoreCase(r8)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            if (r8 != 0) goto L_0x005d
            java.lang.String r8 = "multipart/mixed"
            boolean r8 = r7.equalsIgnoreCase(r8)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            if (r8 == 0) goto L_0x0045
            goto L_0x005d
        L_0x0045:
            java.lang.String r6 = r12.TAG     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            r8.<init>()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String r9 = "Ignore Content type "
            r8.append(r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            r8.append(r7)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String r7 = r8.toString()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            android.util.Log.i(r6, r7)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            goto L_0x00f1
        L_0x005d:
            javax.mail.internet.MimeMultipart r8 = new javax.mail.internet.MimeMultipart     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            javax.mail.util.ByteArrayDataSource r9 = new javax.mail.util.ByteArrayDataSource     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.io.InputStream r6 = r6.getInputStream()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            r9.<init>((java.io.InputStream) r6, (java.lang.String) r7)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            r8.<init>((javax.activation.DataSource) r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            r6 = r3
        L_0x006c:
            int r7 = r8.getCount()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            if (r6 >= r7) goto L_0x00f1
            r2.clear()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            javax.mail.BodyPart r7 = r8.getBodyPart(r6)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String r9 = r7.getContentType()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String[] r9 = r9.split(r1)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            r9 = r9[r3]     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String r10 = "ct"
            r2.put(r10, r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String r10 = "text/plain"
            boolean r10 = r9.contains(r10)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            if (r10 != 0) goto L_0x00b9
            java.lang.String r10 = "application/smil"
            boolean r9 = r9.contains(r10)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            if (r9 == 0) goto L_0x009a
            goto L_0x00b9
        L_0x009a:
            java.lang.String r9 = ""
            java.lang.String r9 = com.sec.internal.ims.cmstore.utils.Util.getRandomFileName(r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            android.content.Context r10 = r12.mContext     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            com.sec.internal.ims.cmstore.MessageStoreClient r11 = r12.mStoreClient     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            int r11 = r11.getClientID()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String r9 = com.sec.internal.ims.cmstore.utils.Util.generateUniqueFilePath(r10, r9, r4, r11)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.io.InputStream r10 = r7.getInputStream()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            com.sec.internal.ims.cmstore.utils.Util.saveInputStreamtoPath(r10, r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String r10 = "_data"
            r2.put(r10, r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            goto L_0x00c7
        L_0x00b9:
            java.io.InputStream r9 = r7.getInputStream()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String r9 = com.sec.internal.ims.cmstore.utils.Util.convertStreamToString(r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String r10 = "text"
            r2.put(r10, r9)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
        L_0x00c7:
            java.lang.String r9 = "mid"
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r10 = r13.getBufferDBChangeParam()     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            long r10 = r10.mRowId     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.Long r10 = java.lang.Long.valueOf(r10)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            r2.put(r9, r10)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String[] r9 = r7.getHeader(r0)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            if (r9 == 0) goto L_0x00e7
            java.lang.String[] r7 = r7.getHeader(r0)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            r7 = r7[r3]     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            java.lang.String r9 = "cid"
            r2.put(r9, r7)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
        L_0x00e7:
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r7 = r12.mBufferDbQuery     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            r9 = 6
            r7.insertTable(r9, r2)     // Catch:{ IOException | NullPointerException | MessagingException -> 0x00f5 }
            int r6 = r6 + 1
            goto L_0x006c
        L_0x00f1:
            int r5 = r5 + 1
            goto L_0x0016
        L_0x00f5:
            r12 = move-exception
            r12.printStackTrace()
        L_0x00f9:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler.onMmsAllPayloadsDownloadFromMcs(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x00e5 A[Catch:{ all -> 0x00fe, all -> 0x0104, IOException -> 0x0110 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onMmsPayloadUpdateWithDB(long r13, java.lang.String r15) {
        /*
            r12 = this;
            java.lang.String r3 = "_data"
            java.lang.String r4 = r12.TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "MMS PayLoad onMmsPayloadUpdateWithDB: "
            r5.append(r6)
            r5.append(r13)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r4, r5)
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r4 = r12.mBufferDbQuery     // Catch:{ IOException -> 0x0110 }
            r5 = 6
            android.database.Cursor r11 = r4.queryTablewithBufferDbId(r5, r13)     // Catch:{ IOException -> 0x0110 }
            if (r11 == 0) goto L_0x010a
            boolean r4 = r11.moveToFirst()     // Catch:{ all -> 0x00fe }
            if (r4 == 0) goto L_0x010a
            java.lang.String r4 = "content_uri"
            int r4 = r11.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x00fe }
            java.lang.String r4 = r11.getString(r4)     // Catch:{ all -> 0x00fe }
            java.lang.String r6 = "ct"
            int r6 = r11.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x00fe }
            java.lang.String r6 = r11.getString(r6)     // Catch:{ all -> 0x00fe }
            java.lang.String r7 = "mid"
            int r7 = r11.getColumnIndexOrThrow(r7)     // Catch:{ all -> 0x00fe }
            int r7 = r11.getInt(r7)     // Catch:{ all -> 0x00fe }
            android.content.ContentValues r8 = new android.content.ContentValues     // Catch:{ all -> 0x00fe }
            r8.<init>()     // Catch:{ all -> 0x00fe }
            java.lang.String r9 = "_bufferdbid= ?"
            r10 = 1
            java.lang.String[] r10 = new java.lang.String[r10]     // Catch:{ all -> 0x00fe }
            java.lang.String r1 = java.lang.String.valueOf(r13)     // Catch:{ all -> 0x00fe }
            r2 = 0
            r10[r2] = r1     // Catch:{ all -> 0x00fe }
            java.lang.String r1 = "application/smil"
            boolean r1 = r1.equalsIgnoreCase(r6)     // Catch:{ all -> 0x00fe }
            if (r1 != 0) goto L_0x00d0
            java.lang.String r1 = "text/plain"
            boolean r1 = r1.equalsIgnoreCase(r6)     // Catch:{ all -> 0x00fe }
            if (r1 != 0) goto L_0x00d0
            java.lang.String r1 = "xml"
            boolean r1 = r6.endsWith(r1)     // Catch:{ all -> 0x00fe }
            if (r1 == 0) goto L_0x0071
            goto L_0x00d0
        L_0x0071:
            java.lang.String r1 = r12.TAG     // Catch:{ all -> 0x00fe }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x00fe }
            r2.<init>()     // Catch:{ all -> 0x00fe }
            java.lang.String r6 = "copy data to app uri "
            r2.append(r6)     // Catch:{ all -> 0x00fe }
            r2.append(r4)     // Catch:{ all -> 0x00fe }
            java.lang.String r6 = " dataSize: "
            r2.append(r6)     // Catch:{ all -> 0x00fe }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x00fe }
            android.util.Log.d(r1, r2)     // Catch:{ all -> 0x00fe }
            int r1 = r11.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x00fe }
            java.lang.String r1 = r11.getString(r1)     // Catch:{ all -> 0x00fe }
            java.io.File r2 = new java.io.File     // Catch:{ all -> 0x00fe }
            r2.<init>(r1)     // Catch:{ all -> 0x00fe }
            boolean r1 = r2.exists()     // Catch:{ all -> 0x00fe }
            if (r1 == 0) goto L_0x00d7
            boolean r1 = r2.canRead()     // Catch:{ all -> 0x00fe }
            if (r1 == 0) goto L_0x00d7
            java.nio.file.Path r1 = r2.toPath()     // Catch:{ all -> 0x00fe }
            byte[] r1 = java.nio.file.Files.readAllBytes(r1)     // Catch:{ all -> 0x00fe }
            android.content.Context r6 = r12.mContext     // Catch:{ all -> 0x00fe }
            com.sec.internal.ims.cmstore.utils.Util.saveFileToAppUri(r6, r1, r4)     // Catch:{ all -> 0x00fe }
            r8.put(r3, r4)     // Catch:{ all -> 0x00fe }
            boolean r1 = r2.delete()     // Catch:{ all -> 0x00fe }
            java.lang.String r2 = r12.TAG     // Catch:{ all -> 0x00fe }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x00fe }
            r3.<init>()     // Catch:{ all -> 0x00fe }
            java.lang.String r4 = "File Deleted: "
            r3.append(r4)     // Catch:{ all -> 0x00fe }
            r3.append(r1)     // Catch:{ all -> 0x00fe }
            java.lang.String r1 = r3.toString()     // Catch:{ all -> 0x00fe }
            com.sec.internal.log.IMSLog.d(r2, r1)     // Catch:{ all -> 0x00fe }
            goto L_0x00d7
        L_0x00d0:
            java.lang.String r1 = r12.TAG     // Catch:{ all -> 0x00fe }
            java.lang.String r2 = "no need update the text data"
            android.util.Log.d(r1, r2)     // Catch:{ all -> 0x00fe }
        L_0x00d7:
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r12.mBufferDbQuery     // Catch:{ all -> 0x00fe }
            r1.updateTable(r5, r8, r9, r10)     // Catch:{ all -> 0x00fe }
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r12.mBufferDbQuery     // Catch:{ all -> 0x00fe }
            long r3 = (long) r7     // Catch:{ all -> 0x00fe }
            boolean r1 = r1.queryIfMmsPartsDownloadComplete(r3)     // Catch:{ all -> 0x00fe }
            if (r1 == 0) goto L_0x010a
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r2 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x00fe }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x00fe }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.UpdatePayload     // Catch:{ all -> 0x00fe }
            r2.<init>(r1, r5)     // Catch:{ all -> 0x00fe }
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r12.mBufferDbQuery     // Catch:{ all -> 0x00fe }
            r1.updateMMSUpdateingDevice(r3)     // Catch:{ all -> 0x00fe }
            r5 = 4
            r6 = 0
            r7 = 0
            r9 = 0
            r10 = 0
            r1 = r12
            r8 = r15
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9, r10)     // Catch:{ all -> 0x00fe }
            goto L_0x010a
        L_0x00fe:
            r0 = move-exception
            r1 = r0
            r11.close()     // Catch:{ all -> 0x0104 }
            goto L_0x0109
        L_0x0104:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ IOException -> 0x0110 }
        L_0x0109:
            throw r1     // Catch:{ IOException -> 0x0110 }
        L_0x010a:
            if (r11 == 0) goto L_0x0114
            r11.close()     // Catch:{ IOException -> 0x0110 }
            goto L_0x0114
        L_0x0110:
            r0 = move-exception
            r0.printStackTrace()
        L_0x0114:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler.onMmsPayloadUpdateWithDB(long, java.lang.String):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x00f3 A[Catch:{ all -> 0x010c, all -> 0x0112, IOException -> 0x011e }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onMmsPayloadDownloaded(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r15, boolean r16) {
        /*
            r14 = this;
            r0 = r14
            java.lang.String r1 = r0.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "MMS PayLoad downloaded: "
            r2.append(r3)
            r3 = r15
            r2.append(r15)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r0.mBufferDbQuery     // Catch:{ IOException -> 0x011e }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r2 = r15.getBufferDBChangeParam()     // Catch:{ IOException -> 0x011e }
            long r4 = r2.mRowId     // Catch:{ IOException -> 0x011e }
            r2 = 6
            android.database.Cursor r11 = r1.queryTablewithBufferDbId(r2, r4)     // Catch:{ IOException -> 0x011e }
            if (r11 == 0) goto L_0x0118
            boolean r1 = r11.moveToFirst()     // Catch:{ all -> 0x010c }
            if (r1 == 0) goto L_0x0118
            java.lang.String r1 = "content_uri"
            int r1 = r11.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x010c }
            java.lang.String r1 = r11.getString(r1)     // Catch:{ all -> 0x010c }
            java.lang.String r4 = "ct"
            int r4 = r11.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x010c }
            java.lang.String r4 = r11.getString(r4)     // Catch:{ all -> 0x010c }
            java.lang.String r5 = "mid"
            int r5 = r11.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x010c }
            int r5 = r11.getInt(r5)     // Catch:{ all -> 0x010c }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r6 = r15.getBufferDBChangeParam()     // Catch:{ all -> 0x010c }
            java.lang.String r8 = r6.mLine     // Catch:{ all -> 0x010c }
            android.content.ContentValues r6 = new android.content.ContentValues     // Catch:{ all -> 0x010c }
            r6.<init>()     // Catch:{ all -> 0x010c }
            java.lang.String r7 = "_bufferdbid= ?"
            r9 = 1
            java.lang.String[] r9 = new java.lang.String[r9]     // Catch:{ all -> 0x010c }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r10 = r15.getBufferDBChangeParam()     // Catch:{ all -> 0x010c }
            long r12 = r10.mRowId     // Catch:{ all -> 0x010c }
            java.lang.String r10 = java.lang.String.valueOf(r12)     // Catch:{ all -> 0x010c }
            r12 = 0
            r9[r12] = r10     // Catch:{ all -> 0x010c }
            java.lang.String r10 = "application/smil"
            boolean r10 = r10.equalsIgnoreCase(r4)     // Catch:{ all -> 0x010c }
            if (r10 != 0) goto L_0x00c1
            java.lang.String r10 = "text/plain"
            boolean r10 = r10.equalsIgnoreCase(r4)     // Catch:{ all -> 0x010c }
            if (r10 != 0) goto L_0x00c1
            java.lang.String r10 = "xml"
            boolean r4 = r4.endsWith(r10)     // Catch:{ all -> 0x010c }
            if (r4 == 0) goto L_0x0083
            goto L_0x00c1
        L_0x0083:
            java.lang.String r4 = r0.TAG     // Catch:{ all -> 0x010c }
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x010c }
            r10.<init>()     // Catch:{ all -> 0x010c }
            java.lang.String r12 = "copy data to app uri "
            r10.append(r12)     // Catch:{ all -> 0x010c }
            r10.append(r1)     // Catch:{ all -> 0x010c }
            java.lang.String r12 = " dataSize: "
            r10.append(r12)     // Catch:{ all -> 0x010c }
            byte[] r12 = r15.getData()     // Catch:{ all -> 0x010c }
            if (r12 == 0) goto L_0x00a7
            byte[] r12 = r15.getData()     // Catch:{ all -> 0x010c }
            int r12 = r12.length     // Catch:{ all -> 0x010c }
            java.lang.Integer r12 = java.lang.Integer.valueOf(r12)     // Catch:{ all -> 0x010c }
            goto L_0x00a8
        L_0x00a7:
            r12 = 0
        L_0x00a8:
            r10.append(r12)     // Catch:{ all -> 0x010c }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x010c }
            android.util.Log.d(r4, r10)     // Catch:{ all -> 0x010c }
            android.content.Context r4 = r0.mContext     // Catch:{ all -> 0x010c }
            byte[] r3 = r15.getData()     // Catch:{ all -> 0x010c }
            com.sec.internal.ims.cmstore.utils.Util.saveFileToAppUri(r4, r3, r1)     // Catch:{ all -> 0x010c }
            java.lang.String r3 = "_data"
            r6.put(r3, r1)     // Catch:{ all -> 0x010c }
            goto L_0x00e5
        L_0x00c1:
            java.lang.String r1 = r0.TAG     // Catch:{ all -> 0x010c }
            java.lang.String r4 = "part UTF8 text data"
            android.util.Log.i(r1, r4)     // Catch:{ all -> 0x010c }
            byte[] r1 = r15.getData()     // Catch:{ all -> 0x010c }
            java.lang.String r4 = "text"
            if (r1 == 0) goto L_0x00e0
            java.lang.String r1 = new java.lang.String     // Catch:{ all -> 0x010c }
            byte[] r3 = r15.getData()     // Catch:{ all -> 0x010c }
            java.nio.charset.Charset r10 = java.nio.charset.StandardCharsets.UTF_8     // Catch:{ all -> 0x010c }
            r1.<init>(r3, r10)     // Catch:{ all -> 0x010c }
            r6.put(r4, r1)     // Catch:{ all -> 0x010c }
            goto L_0x00e5
        L_0x00e0:
            java.lang.String r1 = ""
            r6.put(r4, r1)     // Catch:{ all -> 0x010c }
        L_0x00e5:
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r0.mBufferDbQuery     // Catch:{ all -> 0x010c }
            r1.updateTable(r2, r6, r7, r9)     // Catch:{ all -> 0x010c }
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r0.mBufferDbQuery     // Catch:{ all -> 0x010c }
            long r3 = (long) r5     // Catch:{ all -> 0x010c }
            boolean r1 = r1.queryIfMmsPartsDownloadComplete(r3)     // Catch:{ all -> 0x010c }
            if (r1 == 0) goto L_0x0118
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r2 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x010c }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x010c }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.UpdatePayload     // Catch:{ all -> 0x010c }
            r2.<init>(r1, r5)     // Catch:{ all -> 0x010c }
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r0.mBufferDbQuery     // Catch:{ all -> 0x010c }
            r1.updateMMSUpdateingDevice(r3)     // Catch:{ all -> 0x010c }
            r5 = 4
            r6 = 0
            r9 = 0
            r10 = 0
            r1 = r14
            r7 = r16
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9, r10)     // Catch:{ all -> 0x010c }
            goto L_0x0118
        L_0x010c:
            r0 = move-exception
            r1 = r0
            r11.close()     // Catch:{ all -> 0x0112 }
            goto L_0x0117
        L_0x0112:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ IOException -> 0x011e }
        L_0x0117:
            throw r1     // Catch:{ IOException -> 0x011e }
        L_0x0118:
            if (r11 == 0) goto L_0x0122
            r11.close()     // Catch:{ IOException -> 0x011e }
            goto L_0x0122
        L_0x011e:
            r0 = move-exception
            r0.printStackTrace()
        L_0x0122:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler.onMmsPayloadDownloaded(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB, boolean):void");
    }

    public void handleExistingBufferForDeviceLegacyUpdate(Cursor cursor, DeviceLegacyUpdateParam deviceLegacyUpdateParam, boolean z, BufferDBChangeParamList bufferDBChangeParamList) {
        Cursor cursor2 = cursor;
        DeviceLegacyUpdateParam deviceLegacyUpdateParam2 = deviceLegacyUpdateParam;
        String str = this.TAG;
        Log.i(str, "handleExistingBufferForDeviceLegacyUpdate: " + deviceLegacyUpdateParam2);
        ContentValues contentValues = new ContentValues();
        CloudMessageBufferDBConstants.ActionStatusFlag valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        String string = cursor2.getString(cursor2.getColumnIndexOrThrow("linenum"));
        long j = cursor2.getLong(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        ParamSyncFlagsSet setFlagsForMsgOperation = this.mScheduleRule.getSetFlagsForMsgOperation(this.mDbTableContractIndex, j, valueOf2, valueOf, deviceLegacyUpdateParam2.mOperation);
        if (setFlagsForMsgOperation.mIsChanged) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(setFlagsForMsgOperation.mDirection.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(setFlagsForMsgOperation.mAction.getId()));
        }
        if (CloudMessageBufferDBConstants.MsgOperationFlag.Read.equals(deviceLegacyUpdateParam2.mOperation)) {
            contentValues.put("read", 1);
        }
        String[] strArr = {String.valueOf(j)};
        int i = cursor2.getInt(cursor2.getColumnIndexOrThrow("_id"));
        long j2 = deviceLegacyUpdateParam2.mRowId;
        if (j2 != ((long) i)) {
            contentValues.put("_id", Long.valueOf(j2));
        }
        this.mBufferDbQuery.updateTable(deviceLegacyUpdateParam2.mTableindex, contentValues, "_bufferdbid=?", strArr);
        if (setFlagsForMsgOperation.mIsChanged) {
            handleOutPutParamSyncFlagSet(setFlagsForMsgOperation, j, deviceLegacyUpdateParam2.mTableindex, false, z, string, bufferDBChangeParamList, false);
        }
    }

    public void handleNonExistingBufferForDeviceLegacyUpdate(DeviceLegacyUpdateParam deviceLegacyUpdateParam) {
        String str = this.TAG;
        Log.i(str, "handleNonExistingBufferForDeviceLegacyUpdate: " + deviceLegacyUpdateParam);
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
        contentValues.put("linenum", deviceLegacyUpdateParam.mLine);
        if (deviceLegacyUpdateParam.mTableindex == 4) {
            Cursor queryMMSPduFromTelephonyDbUseID = this.mBufferDbQuery.queryMMSPduFromTelephonyDbUseID(Long.valueOf(deviceLegacyUpdateParam.mRowId).longValue());
            if (queryMMSPduFromTelephonyDbUseID != null) {
                try {
                    if (queryMMSPduFromTelephonyDbUseID.moveToFirst()) {
                        this.mBufferDbQuery.insertToMMSPDUBufferDB(queryMMSPduFromTelephonyDbUseID, contentValues, false, true);
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (queryMMSPduFromTelephonyDbUseID != null) {
                queryMMSPduFromTelephonyDbUseID.close();
                return;
            }
            return;
        }
        return;
        throw th;
    }

    public void notifyMsgAppFetchBuffer(Cursor cursor, int i) {
        String str = this.TAG;
        Log.i(str, "notifyMsgAppFetchBuffer: " + i);
        if (i == 4) {
            JsonArray jsonArray = new JsonArray();
            do {
                int i2 = cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", String.valueOf(i2));
                jsonArray.add(jsonObject);
                if (jsonArray.size() == this.mMaxNumMsgsNotifyAppInIntent) {
                    String str2 = this.TAG;
                    Log.i(str2, "notify message app: MMS: " + jsonArray.toString());
                    this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.MMS, jsonArray.toString(), false);
                    jsonArray = new JsonArray();
                }
            } while (cursor.moveToNext());
            if (jsonArray.size() > 0) {
                String str3 = this.TAG;
                Log.d(str3, "notify message app: MMS: " + jsonArray.toString());
                this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.MMS, jsonArray.toString(), false);
            }
        }
    }

    public Cursor queryToDeviceUnDownloadedMms(String str, int i) {
        return this.mBufferDbQuery.queryToDeviceUnDownloadedMms(str, i);
    }

    public int queryPendingUrlFetch() {
        Cursor queryMessageBySyncAction = this.mBufferDbQuery.queryMessageBySyncAction(4, CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri.getId());
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

    public int queryPendingFetchForce() {
        Cursor queryMessageBySyncAction = this.mBufferDbQuery.queryMessageBySyncAction(4, CloudMessageBufferDBConstants.ActionStatusFlag.FetchForce.getId());
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

    /* JADX WARNING: Removed duplicated region for block: B:14:0x003d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean queryPartType(long r4) {
        /*
            r3 = this;
            java.lang.String r0 = r3.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = " queryPartType partId : "
            r1.append(r2)
            r1.append(r4)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r0 = r3.mBufferDbQuery
            r1 = 6
            android.database.Cursor r4 = r0.queryTablewithBufferDbId(r1, r4)
            if (r4 == 0) goto L_0x003a
            boolean r5 = r4.moveToFirst()     // Catch:{ all -> 0x0030 }
            if (r5 == 0) goto L_0x003a
            java.lang.String r5 = "payloadurl"
            int r5 = r4.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x0030 }
            java.lang.String r5 = r4.getString(r5)     // Catch:{ all -> 0x0030 }
            goto L_0x003b
        L_0x0030:
            r3 = move-exception
            r4.close()     // Catch:{ all -> 0x0035 }
            goto L_0x0039
        L_0x0035:
            r4 = move-exception
            r3.addSuppressed(r4)
        L_0x0039:
            throw r3
        L_0x003a:
            r5 = 0
        L_0x003b:
            if (r4 == 0) goto L_0x0040
            r4.close()
        L_0x0040:
            java.lang.String r3 = r3.TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r0 = " queryPartType "
            r4.append(r0)
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r3, r4)
            boolean r3 = android.text.TextUtils.isEmpty(r5)
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler.queryPartType(long):boolean");
    }

    public boolean queryAllPartIsUpdated(long j) {
        Cursor queryMMSPartRowIdWithoutAppId = this.mBufferDbQuery.queryMMSPartRowIdWithoutAppId(j);
        if (queryMMSPartRowIdWithoutAppId != null) {
            try {
                if (queryMMSPartRowIdWithoutAppId.getCount() == 0) {
                    queryMMSPartRowIdWithoutAppId.close();
                    return true;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryMMSPartRowIdWithoutAppId == null) {
            return false;
        }
        queryMMSPartRowIdWithoutAppId.close();
        return false;
        throw th;
    }

    public Cursor queryToCloudUnsyncedMms() {
        return this.mBufferDbQuery.queryToCloudUnsyncedMms();
    }

    public Cursor queryToDeviceUnsyncedMms() {
        return this.mBufferDbQuery.queryToDeviceUnsyncedMms();
    }

    public Cursor queryMMSMessagesToUpload() {
        return this.mBufferDbQuery.queryMMSMessagesToUpload();
    }

    public Cursor queryMMSBufferDBwithResUrl(String str) {
        return this.mBufferDbQuery.queryMMSBufferDBwithResUrl(str);
    }

    public int deleteMMSBufferDBwithResUrl(String str) {
        return this.mBufferDbQuery.deleteMMSBufferDBwithResUrl(str);
    }

    public Cursor searchMMsPduBufferUsingCorrelationId(String str) {
        return this.mBufferDbQuery.searchMMsPduBufferUsingCorrelationId(str);
    }

    public Cursor queryMMSMessagesBySycnDirection(int i, String str) {
        return this.mBufferDbQuery.queryMessageBySyncDirection(i, str);
    }

    public Cursor queryMMSPduFromTelephonyDbWithIMSI(String str) {
        return this.mBufferDbQuery.queryAllMMSPduFromTelephonyDbWithIMSI(str);
    }

    public Cursor queryMMSPduFromTelephonyDbWoIMSI() {
        return this.mBufferDbQuery.queryMMSPduFromTelephonyDbWoIMSI();
    }

    public Cursor queryDeltaMMSPduFromTelephonyDb() {
        return this.mBufferDbQuery.queryDeltaMMSPduFromTelephonyDb();
    }

    public Cursor queryDeltaMMSPduFromTelephonyDbWoImsi() {
        return this.mBufferDbQuery.queryDeltaMMSPduFromTelephonyDbWoImsi();
    }

    public void syncReadMmsFromTelephony() {
        Cursor queryReadMmsfromTelephony;
        ArrayList arrayList = new ArrayList();
        try {
            queryReadMmsfromTelephony = this.mBufferDbQuery.queryReadMmsfromTelephony();
            if (queryReadMmsfromTelephony != null) {
                if (queryReadMmsfromTelephony.moveToFirst()) {
                    arrayList.add(queryReadMmsfromTelephony.getString(queryReadMmsfromTelephony.getColumnIndex("_id")));
                }
            }
            if (queryReadMmsfromTelephony != null) {
                queryReadMmsfromTelephony.close();
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

    public void insertToMMSPDUBufferDB(Cursor cursor, ContentValues contentValues, boolean z) {
        this.mBufferDbQuery.insertToMMSPDUBufferDB(cursor, contentValues, z, false);
    }

    public Cursor queryMMSPduFromTelephonyDbUseID(long j) {
        return this.mBufferDbQuery.queryMMSPduFromTelephonyDbUseID(j);
    }

    private void handleDeviceLegacyUpdateParam(DeviceLegacyUpdateParam deviceLegacyUpdateParam, boolean z, BufferDBChangeParamList bufferDBChangeParamList) {
        String str = this.TAG;
        Log.i(str, "handleDeviceLegacyUpdateParam: " + deviceLegacyUpdateParam);
        if (deviceLegacyUpdateParam.mTableindex == 4 && !CloudMessageBufferDBConstants.MsgOperationFlag.Sending.equals(deviceLegacyUpdateParam.mOperation) && !CloudMessageBufferDBConstants.MsgOperationFlag.SendFail.equals(deviceLegacyUpdateParam.mOperation) && deviceLegacyUpdateParam.mMId != null) {
            Cursor cursor = null;
            try {
                switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[deviceLegacyUpdateParam.mOperation.ordinal()]) {
                    case 1:
                        cursor = this.mBufferDbQuery.searchMMSPduBufferUsingMidorTrId(deviceLegacyUpdateParam.mMId, deviceLegacyUpdateParam.mTRId);
                        break;
                    case 2:
                        cursor = this.mBufferDbQuery.searchMMSPduBufferUsingMidorTrId(deviceLegacyUpdateParam.mMId, deviceLegacyUpdateParam.mTRId);
                        break;
                    case 3:
                        cursor = this.mBufferDbQuery.searchMMSPduBufferUsingRowId(deviceLegacyUpdateParam.mRowId);
                        break;
                    case 4:
                        cursor = this.mBufferDbQuery.searchMMSPduBufferUsingRowId(deviceLegacyUpdateParam.mRowId);
                        break;
                    case 5:
                    case 6:
                    case 7:
                        return;
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

    /* renamed from: com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler$1  reason: invalid class name */
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
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Delete     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.SendFail     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Receiving     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Sending     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler.AnonymousClass1.<clinit>():void");
        }
    }

    public boolean onUpdateFromDeviceFtUriFetch(DeviceMsgAppFetchUriParam deviceMsgAppFetchUriParam) {
        deviceMsgAppFetchUriParam.mTableindex = 6;
        onUpdateFromDeviceFtUriFetch(deviceMsgAppFetchUriParam, this.mBufferDbQuery);
        boolean queryAllPartIsUpdated = queryAllPartIsUpdated(deviceMsgAppFetchUriParam.mBufferRowId);
        if (queryAllPartIsUpdated) {
            Log.i(this.TAG, "onUpdateFromDeviceFtUriFetch: All parts downloaded, set state as Download");
            ContentValues contentValues = new ContentValues();
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
            updateQueryTable(contentValues, deviceMsgAppFetchUriParam.mBufferRowId, this.mBufferDbQuery);
        }
        return queryAllPartIsUpdated;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0028  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int queryMMSPDUActionStatus(long r2) {
        /*
            r1 = this;
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r1.mBufferDbQuery
            r0 = 4
            android.database.Cursor r1 = r1.queryTablewithBufferDbId(r0, r2)
            if (r1 == 0) goto L_0x0025
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x001b }
            if (r2 == 0) goto L_0x0025
            java.lang.String r2 = "syncaction"
            int r2 = r1.getColumnIndex(r2)     // Catch:{ all -> 0x001b }
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
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler.queryMMSPDUActionStatus(long):int");
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
            handleCloudUploadSuccess(paramOMAresponseforBufDB, z, this.mBufferDbQuery, 4);
        }
    }

    public void cleanAllBufferDB() {
        this.mBufferDbQuery.cleanAllBufferDB();
    }

    public void onAppOperationReceived(ParamAppJsonValue paramAppJsonValue, BufferDBChangeParamList bufferDBChangeParamList) {
        String str = this.TAG;
        Log.i(str, "onAppOperationReceived: " + paramAppJsonValue);
        if (CloudMessageBufferDBConstants.MsgOperationFlag.Delete.equals(paramAppJsonValue.mOperation)) {
            int i = paramAppJsonValue.mDataContractType;
            CloudMessageBufferDBConstants.MsgOperationFlag msgOperationFlag = paramAppJsonValue.mOperation;
            int i2 = paramAppJsonValue.mRowId;
            String str2 = paramAppJsonValue.mCorrelationId;
            handleDeviceLegacyUpdateParam(new DeviceLegacyUpdateParam(i, msgOperationFlag, i2, (String) null, str2, str2, paramAppJsonValue.mLine), false, bufferDBChangeParamList);
            return;
        }
        Cursor queryMMSPduFromTelephonyDbUseID = queryMMSPduFromTelephonyDbUseID((long) paramAppJsonValue.mRowId);
        if (queryMMSPduFromTelephonyDbUseID != null) {
            try {
                if (queryMMSPduFromTelephonyDbUseID.moveToFirst()) {
                    String string = queryMMSPduFromTelephonyDbUseID.getString(queryMMSPduFromTelephonyDbUseID.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.TR_ID));
                    String str3 = paramAppJsonValue.mCorrelationId;
                    if (TextUtils.isEmpty(str3)) {
                        str3 = queryMMSPduFromTelephonyDbUseID.getString(queryMMSPduFromTelephonyDbUseID.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.M_ID));
                    }
                    handleDeviceLegacyUpdateParam(new DeviceLegacyUpdateParam(paramAppJsonValue.mDataContractType, paramAppJsonValue.mOperation, paramAppJsonValue.mRowId, (String) null, str3, string, paramAppJsonValue.mLine), false, bufferDBChangeParamList);
                    Log.d(this.TAG, "onAppOperationReceived: no mms pdu exists");
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryMMSPduFromTelephonyDbUseID != null) {
            queryMMSPduFromTelephonyDbUseID.close();
            return;
        }
        return;
        throw th;
    }

    public boolean handleCrossSearchObj(ParamOMAObject paramOMAObject, String str, boolean z) {
        String str2 = this.TAG;
        Log.i(str2, "handleCrossSearchObj():  line: " + IMSLog.checker(str) + " objt: " + paramOMAObject);
        Cursor searchMMsPduBufferUsingCorrelationId = searchMMsPduBufferUsingCorrelationId(paramOMAObject.correlationId);
        if (searchMMsPduBufferUsingCorrelationId != null) {
            try {
                if (searchMMsPduBufferUsingCorrelationId.moveToFirst()) {
                    onCrossObjectSearchMmsAvailableUsingCorrelationId(searchMMsPduBufferUsingCorrelationId, paramOMAObject, str, z);
                    searchMMsPduBufferUsingCorrelationId.close();
                    return true;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (searchMMsPduBufferUsingCorrelationId == null) {
            return false;
        }
        searchMMsPduBufferUsingCorrelationId.close();
        return false;
        throw th;
    }

    private void onCrossObjectSearchMmsAvailableUsingCorrelationId(Cursor cursor, ParamOMAObject paramOMAObject, String str, boolean z) {
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag;
        CloudMessageBufferDBConstants.DirectionFlag directionFlag;
        Cursor cursor2 = cursor;
        ParamOMAObject paramOMAObject2 = paramOMAObject;
        long j = (long) cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long j2 = cursor2.getLong(cursor2.getColumnIndexOrThrow("date"));
        long j3 = (long) cursor2.getInt(cursor2.getColumnIndexOrThrow("_id"));
        long j4 = cursor2.getLong(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.M_ID));
        String str2 = this.TAG;
        Log.d(str2, "handleCrossSearchObj find bufferDB: " + paramOMAObject2.correlationId + " id: " + j + " time: " + j2 + " m_id: " + j4);
        CloudMessageBufferDBConstants.ActionStatusFlag valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, paramOMAObject2.lastModSeq);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject2.resourceURL.toString()));
        URL url = paramOMAObject2.parentFolder;
        if (url != null) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(url.toString()));
        }
        String str3 = paramOMAObject2.path;
        if (str3 != null) {
            contentValues.put("path", Util.decodeUrlFromServer(str3));
        }
        if (cursor2.getInt(cursor2.getColumnIndexOrThrow("read")) == 1) {
            actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
            directionFlag = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
        } else {
            actionStatusFlag = valueOf;
            directionFlag = valueOf2;
        }
        if (CloudMessageBufferDBConstants.ActionStatusFlag.Update.equals(paramOMAObject2.mFlag)) {
            contentValues.put("read", 1);
        }
        CloudMessageBufferDBEventSchedulingRule cloudMessageBufferDBEventSchedulingRule = this.mScheduleRule;
        int i = this.mDbTableContractIndex;
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2 = paramOMAObject2.mFlag;
        ContentValues contentValues2 = contentValues;
        ParamSyncFlagsSet setFlagsForCldOperation = cloudMessageBufferDBEventSchedulingRule.getSetFlagsForCldOperation(i, j, directionFlag, actionStatusFlag, actionStatusFlag2);
        if (setFlagsForCldOperation.mIsChanged) {
            contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(setFlagsForCldOperation.mDirection.getId()));
            contentValues2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(setFlagsForCldOperation.mAction.getId()));
        }
        updateQueryTable(contentValues2, j, this.mBufferDbQuery);
        if (j3 > 0) {
            handleOutPutParamSyncFlagSet(setFlagsForCldOperation, j, 4, false, z, str, (BufferDBChangeParamList) null, false);
        }
    }

    public void notifyMsgAppDeleteFail(int i, long j, String str) {
        String str2 = this.TAG;
        Log.i(str2, "notifyMsgAppDeleteFail, dbIndex: " + i + " bufferDbId: " + j + " line: " + IMSLog.checker(str));
        if (i == 4) {
            JsonArray jsonArray = new JsonArray();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", String.valueOf(j));
            jsonArray.add(jsonObject);
            this.mCallbackMsgApp.notifyAppCloudDeleteFail(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.MMS, jsonArray.toString());
        }
    }

    public void onUpdateCmsConfig() {
        this.mBufferDbQuery.onUpdateCmsConfigInitSyncDataTtl();
    }
}
