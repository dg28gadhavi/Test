package com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.data.FlagNames;
import com.sec.internal.constants.ims.cmstore.data.OperationEnum;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkUpdate;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualObject;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteObjectFlag;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessagePutObjectFlag;
import com.sec.internal.ims.cmstore.omanetapi.nms.McsPostGroupSMS;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.ParamObjectUpload;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.omanetapi.nms.data.BulkDelete;
import com.sec.internal.omanetapi.nms.data.BulkUpdate;
import com.sec.internal.omanetapi.nms.data.Reference;
import java.util.ArrayList;
import java.util.List;

public class OMAObjectUpdateScheduler extends BaseDeviceDataUpdateHandler {
    private String TAG = OMAObjectUpdateScheduler.class.getSimpleName();
    private ICloudMessageManagerHelper mICloudMessageManagerHelper;

    public OMAObjectUpdateScheduler(Looper looper, MessageStoreClient messageStoreClient, INetAPIEventListener iNetAPIEventListener, String str, SyncMsgType syncMsgType, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper, messageStoreClient, iNetAPIEventListener, str, syncMsgType, iCloudMessageManagerHelper);
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
    }

    /* access modifiers changed from: protected */
    public void setWorkingQueue(BufferDBChangeParam bufferDBChangeParam) {
        String str = this.TAG;
        Log.i(str, "setWorkingQueue param: " + bufferDBChangeParam);
        if (bufferDBChangeParam != null) {
            if (!this.isCmsEnabled || !bufferDBChangeParam.mIsGroupSMSUpload) {
                Pair<String, String> objectIdFlagNamePairFromBufDb = this.mBufferDBTranslation.getObjectIdFlagNamePairFromBufDb(bufferDBChangeParam);
                String str2 = this.TAG;
                Log.i(str2, "setWorkingQueue " + ((String) objectIdFlagNamePairFromBufDb.first) + ((String) objectIdFlagNamePairFromBufDb.second));
                if (!TextUtils.isEmpty((CharSequence) objectIdFlagNamePairFromBufDb.first) && !TextUtils.isEmpty((CharSequence) objectIdFlagNamePairFromBufDb.second)) {
                    if (FlagNames.Seen.equals(objectIdFlagNamePairFromBufDb.second) || FlagNames.Canceled.equals(objectIdFlagNamePairFromBufDb.second) || (FlagNames.Starred.equals(objectIdFlagNamePairFromBufDb.second) && bufferDBChangeParam.mAction == CloudMessageBufferDBConstants.ActionStatusFlag.Starred)) {
                        this.mWorkingQueue.offer(new CloudMessagePutObjectFlag(this, (String) objectIdFlagNamePairFromBufDb.first, (String) objectIdFlagNamePairFromBufDb.second, bufferDBChangeParam, this.mStoreClient));
                    } else if (FlagNames.Deleted.equals(objectIdFlagNamePairFromBufDb.second)) {
                        this.mWorkingQueue.offer(new CloudMessageDeleteIndividualObject(this, (String) objectIdFlagNamePairFromBufDb.first, bufferDBChangeParam, this.mStoreClient));
                    } else if (FlagNames.Flagged.equals(objectIdFlagNamePairFromBufDb.second) || FlagNames.Starred.equals(objectIdFlagNamePairFromBufDb.second)) {
                        this.mWorkingQueue.offer(new CloudMessageDeleteObjectFlag(this, (String) objectIdFlagNamePairFromBufDb.first, FlagNames.Seen, bufferDBChangeParam, this.mStoreClient));
                    }
                }
            } else {
                this.mWorkingQueue.offer(new McsPostGroupSMS(this, new ParamObjectUpload(this.mBufferDBTranslation.getGroupSMSForSteadyUpload(bufferDBChangeParam), bufferDBChangeParam), this.mStoreClient));
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v7, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v0, resolved type: java.lang.String} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setWorkingQueue(com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r21) {
        /*
            r20 = this;
            r7 = r20
            r0 = r21
            if (r0 == 0) goto L_0x0243
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r1 = r0.mChangelst
            if (r1 == 0) goto L_0x0243
            int r1 = r1.size()
            if (r1 != 0) goto L_0x0012
            goto L_0x0243
        L_0x0012:
            java.lang.String r1 = r7.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "setWorkingQueue  isBulkUpdateEnabled: "
            r2.append(r3)
            com.sec.internal.ims.cmstore.MessageStoreClient r3 = r7.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r3 = r3.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r3 = r3.getStrategy()
            boolean r3 = r3.isBulkUpdateEnabled()
            r2.append(r3)
            java.lang.String r3 = "mChangelst size: "
            r2.append(r3)
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r3 = r0.mChangelst
            int r3 = r3.size()
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r1 = r0.mChangelst
            int r1 = r1.size()
            r8 = 1
            if (r1 != r8) goto L_0x005b
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r0 = r0.mChangelst
            r1 = 0
            java.lang.Object r0 = r0.get(r1)
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r0 = (com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r0
            r7.setWorkingQueue((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r0)
            return
        L_0x005b:
            java.util.ArrayList r9 = new java.util.ArrayList
            r9.<init>()
            java.util.ArrayList r10 = new java.util.ArrayList
            r10.<init>()
            java.util.ArrayList r11 = new java.util.ArrayList
            r11.<init>()
            com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r12 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParamList
            r12.<init>()
            com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r13 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParamList
            r13.<init>()
            com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r14 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParamList
            r14.<init>()
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r0 = r0.mChangelst
            java.util.Iterator r15 = r0.iterator()
            java.lang.String r6 = ""
            r0 = r6
        L_0x0082:
            boolean r1 = r15.hasNext()
            if (r1 == 0) goto L_0x023a
            java.lang.Object r1 = r15.next()
            r5 = r1
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r5 = (com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r5
            if (r5 != 0) goto L_0x0092
            goto L_0x0082
        L_0x0092:
            com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation r0 = r7.mBufferDBTranslation
            android.util.Pair r0 = r0.getResourceUrlFlagNamePairFromBufDb(r5)
            java.lang.Object r1 = r0.second
            r4 = r1
            java.lang.String r4 = (java.lang.String) r4
            java.lang.Object r1 = r0.first
            java.lang.CharSequence r1 = (java.lang.CharSequence) r1
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x022d
            boolean r1 = android.text.TextUtils.isEmpty(r4)
            if (r1 != 0) goto L_0x022d
            java.lang.String r1 = "\\Seen"
            boolean r1 = r1.equals(r4)
            r2 = 47
            if (r1 != 0) goto L_0x01bd
            java.lang.String r1 = "\\Canceled"
            boolean r1 = r1.equals(r4)
            if (r1 != 0) goto L_0x01bd
            java.lang.String r1 = "\\Starred"
            boolean r3 = r1.equals(r4)
            if (r3 == 0) goto L_0x00cf
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = r5.mAction
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r8 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Starred
            if (r3 != r8) goto L_0x00cf
            goto L_0x01bd
        L_0x00cf:
            java.lang.String r3 = "\\Deleted"
            boolean r3 = r3.equals(r4)
            if (r3 == 0) goto L_0x013b
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = r7.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r1 = r1.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r1 = r1.getStrategy()
            boolean r1 = r1.isBulkDeleteEnabled()
            if (r1 == 0) goto L_0x011f
            com.sec.internal.omanetapi.nms.data.Reference r1 = new com.sec.internal.omanetapi.nms.data.Reference
            r1.<init>()
            java.net.URL r2 = new java.net.URL     // Catch:{ MalformedURLException -> 0x0101 }
            java.lang.Object r0 = r0.first     // Catch:{ MalformedURLException -> 0x0101 }
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ MalformedURLException -> 0x0101 }
            r2.<init>(r0)     // Catch:{ MalformedURLException -> 0x0101 }
            r1.resourceURL = r2     // Catch:{ MalformedURLException -> 0x0101 }
            r9.add(r1)     // Catch:{ MalformedURLException -> 0x0101 }
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r0 = r12.mChangelst     // Catch:{ MalformedURLException -> 0x0101 }
            r0.add(r5)     // Catch:{ MalformedURLException -> 0x0101 }
            goto L_0x022d
        L_0x0101:
            r0 = move-exception
            java.lang.String r1 = r7.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = r0.getMessage()
            r2.append(r3)
            r2.append(r6)
            java.lang.String r2 = r2.toString()
            android.util.Log.e(r1, r2)
            r0.printStackTrace()
            goto L_0x022d
        L_0x011f:
            java.lang.Object r0 = r0.first
            java.lang.String r0 = (java.lang.String) r0
            int r1 = r0.lastIndexOf(r2)
            r2 = 1
            int r1 = r1 + r2
            java.lang.String r0 = r0.substring(r1)
            java.util.Queue<com.sec.internal.helper.httpclient.HttpRequestParams> r1 = r7.mWorkingQueue
            com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualObject r2 = new com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualObject
            com.sec.internal.ims.cmstore.MessageStoreClient r3 = r7.mStoreClient
            r2.<init>(r7, r0, r5, r3)
            r1.offer(r2)
            goto L_0x022d
        L_0x013b:
            java.lang.String r3 = "\\Flagged"
            boolean r3 = r3.equals(r4)
            if (r3 != 0) goto L_0x0149
            boolean r1 = r1.equals(r4)
            if (r1 == 0) goto L_0x022d
        L_0x0149:
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = r7.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r1 = r1.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r1 = r1.getStrategy()
            boolean r1 = r1.isBulkUpdateEnabled()
            if (r1 == 0) goto L_0x0191
            com.sec.internal.omanetapi.nms.data.Reference r1 = new com.sec.internal.omanetapi.nms.data.Reference
            r1.<init>()
            java.net.URL r2 = new java.net.URL     // Catch:{ MalformedURLException -> 0x0173 }
            java.lang.Object r0 = r0.first     // Catch:{ MalformedURLException -> 0x0173 }
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ MalformedURLException -> 0x0173 }
            r2.<init>(r0)     // Catch:{ MalformedURLException -> 0x0173 }
            r1.resourceURL = r2     // Catch:{ MalformedURLException -> 0x0173 }
            r11.add(r1)     // Catch:{ MalformedURLException -> 0x0173 }
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r0 = r14.mChangelst     // Catch:{ MalformedURLException -> 0x0173 }
            r0.add(r5)     // Catch:{ MalformedURLException -> 0x0173 }
            goto L_0x022d
        L_0x0173:
            r0 = move-exception
            java.lang.String r1 = r7.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = r0.getMessage()
            r2.append(r3)
            r2.append(r6)
            java.lang.String r2 = r2.toString()
            android.util.Log.e(r1, r2)
            r0.printStackTrace()
            goto L_0x022d
        L_0x0191:
            java.lang.Object r0 = r0.first
            java.lang.String r0 = (java.lang.String) r0
            int r1 = r0.lastIndexOf(r2)
            r2 = 1
            int r1 = r1 + r2
            java.lang.String r3 = r0.substring(r1)
            java.util.Queue<com.sec.internal.helper.httpclient.HttpRequestParams> r0 = r7.mWorkingQueue
            com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteObjectFlag r8 = new com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteObjectFlag
            java.lang.String r17 = "\\Seen"
            com.sec.internal.ims.cmstore.MessageStoreClient r2 = r7.mStoreClient
            r1 = r8
            r18 = r2
            r2 = r20
            r19 = r4
            r4 = r17
            r21 = r15
            r15 = r6
            r6 = r18
            r1.<init>(r2, r3, r4, r5, r6)
            r0.offer(r8)
            goto L_0x0232
        L_0x01bd:
            r19 = r4
            r21 = r15
            r15 = r6
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = r7.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r1 = r1.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r1 = r1.getStrategy()
            boolean r1 = r1.isBulkUpdateEnabled()
            if (r1 == 0) goto L_0x0208
            com.sec.internal.omanetapi.nms.data.Reference r1 = new com.sec.internal.omanetapi.nms.data.Reference
            r1.<init>()
            java.net.URL r2 = new java.net.URL     // Catch:{ MalformedURLException -> 0x01eb }
            java.lang.Object r0 = r0.first     // Catch:{ MalformedURLException -> 0x01eb }
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ MalformedURLException -> 0x01eb }
            r2.<init>(r0)     // Catch:{ MalformedURLException -> 0x01eb }
            r1.resourceURL = r2     // Catch:{ MalformedURLException -> 0x01eb }
            r10.add(r1)     // Catch:{ MalformedURLException -> 0x01eb }
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r0 = r13.mChangelst     // Catch:{ MalformedURLException -> 0x01eb }
            r0.add(r5)     // Catch:{ MalformedURLException -> 0x01eb }
            goto L_0x0232
        L_0x01eb:
            r0 = move-exception
            java.lang.String r1 = r7.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = r0.getMessage()
            r2.append(r3)
            r2.append(r15)
            java.lang.String r2 = r2.toString()
            android.util.Log.e(r1, r2)
            r0.printStackTrace()
            goto L_0x0232
        L_0x0208:
            java.lang.Object r0 = r0.first
            java.lang.String r0 = (java.lang.String) r0
            int r1 = r0.lastIndexOf(r2)
            r8 = 1
            int r1 = r1 + r8
            java.lang.String r3 = r0.substring(r1)
            java.util.Queue<com.sec.internal.helper.httpclient.HttpRequestParams> r0 = r7.mWorkingQueue
            com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessagePutObjectFlag r6 = new com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessagePutObjectFlag
            com.sec.internal.ims.cmstore.MessageStoreClient r4 = r7.mStoreClient
            r1 = r6
            r2 = r20
            r16 = r4
            r4 = r19
            r8 = r6
            r6 = r16
            r1.<init>(r2, r3, r4, r5, r6)
            r0.offer(r8)
            goto L_0x0232
        L_0x022d:
            r19 = r4
            r21 = r15
            r15 = r6
        L_0x0232:
            r6 = r15
            r0 = r19
            r8 = 1
            r15 = r21
            goto L_0x0082
        L_0x023a:
            r7.processBulkDelete(r9, r12)
            r7.processBulkSet(r10, r13, r0)
            r7.processBulkUnset(r11, r14, r0)
        L_0x0243:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.OMAObjectUpdateScheduler.setWorkingQueue(com.sec.internal.ims.cmstore.params.BufferDBChangeParamList):void");
    }

    private void processBulkDelete(List<Reference> list, BufferDBChangeParamList bufferDBChangeParamList) {
        int i;
        if (list != null && list.size() >= 1 && this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isBulkDeleteEnabled()) {
            int maxBulkOptionEntry = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMaxBulkOptionEntry();
            String str = this.TAG;
            Log.i(str, "getMaxBulkOptionEntry: " + maxBulkOptionEntry + " listsize: " + list.size());
            if (maxBulkOptionEntry <= 1) {
                maxBulkOptionEntry = 100;
            }
            if (list.size() % maxBulkOptionEntry == 0) {
                i = list.size() / maxBulkOptionEntry;
            } else {
                i = 1 + (list.size() / maxBulkOptionEntry);
            }
            int i2 = 0;
            while (i2 < i) {
                int i3 = i2 * maxBulkOptionEntry;
                i2++;
                int min = Math.min(list.size(), i2 * maxBulkOptionEntry);
                List<Reference> subList = list.subList(i3, min);
                BufferDBChangeParamList bufferDBChangeParamList2 = new BufferDBChangeParamList();
                bufferDBChangeParamList2.mChangelst = new ArrayList<>(bufferDBChangeParamList.mChangelst.subList(i3, min));
                String str2 = this.TAG;
                Log.i(str2, "Start, End: " + i3 + " " + min + " newlistsize: " + subList.size());
                BulkDelete createNewBulkDeleteParam = createNewBulkDeleteParam(subList);
                Reference[] referenceArr = createNewBulkDeleteParam.objects.objectReference;
                if (referenceArr != null && referenceArr.length > 0) {
                    this.mWorkingQueue.offer(new CloudMessageBulkDeletion(this, createNewBulkDeleteParam, this.mLine, this.mSyncMsgType, bufferDBChangeParamList2, this.mStoreClient));
                }
            }
        }
    }

    private void processBulkSet(List<Reference> list, BufferDBChangeParamList bufferDBChangeParamList, String str) {
        int i;
        List<Reference> list2 = list;
        if (list2 != null && list.size() >= 1 && this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isBulkUpdateEnabled()) {
            int maxBulkOptionEntry = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMaxBulkOptionEntry();
            String str2 = this.TAG;
            Log.i(str2, "processBulkSet: " + maxBulkOptionEntry + " listsize: " + list.size());
            if (maxBulkOptionEntry <= 1) {
                maxBulkOptionEntry = 100;
            }
            int i2 = maxBulkOptionEntry;
            if (list.size() % i2 == 0) {
                i = list.size() / i2;
            } else {
                i = (list.size() / i2) + 1;
            }
            int i3 = i;
            int i4 = 0;
            while (i4 < i3) {
                int i5 = i4 * i2;
                int i6 = i4 + 1;
                int min = Math.min(list.size(), i6 * i2);
                List<Reference> subList = list2.subList(i5, min);
                BufferDBChangeParamList bufferDBChangeParamList2 = new BufferDBChangeParamList();
                bufferDBChangeParamList2.mChangelst = new ArrayList<>(bufferDBChangeParamList.mChangelst.subList(i5, min));
                String str3 = this.TAG;
                Log.i(str3, "Start, End: " + i5 + " " + min + " newlistsize: " + subList.size());
                BulkUpdate createNewBulkUpdateParam = createNewBulkUpdateParam(subList, new String[]{str}, OperationEnum.AddFlag);
                Reference[] referenceArr = createNewBulkUpdateParam.objects.objectReference;
                if (referenceArr != null && referenceArr.length > 0) {
                    Log.i(this.TAG, "send bulk update");
                    this.mWorkingQueue.offer(new CloudMessageBulkUpdate(this, createNewBulkUpdateParam, this.mLine, this.mSyncMsgType, bufferDBChangeParamList2, this.mStoreClient));
                }
                i4 = i6;
            }
        }
    }

    private void processBulkUnset(List<Reference> list, BufferDBChangeParamList bufferDBChangeParamList, String str) {
        int i;
        List<Reference> list2 = list;
        if (list2 != null && list.size() >= 1 && this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isBulkUpdateEnabled()) {
            int maxBulkOptionEntry = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMaxBulkOptionEntry();
            String str2 = this.TAG;
            Log.i(str2, "processBulkUnset: " + maxBulkOptionEntry + " listsize: " + list.size());
            if (maxBulkOptionEntry <= 1) {
                maxBulkOptionEntry = 100;
            }
            int i2 = maxBulkOptionEntry;
            if (list.size() % i2 == 0) {
                i = list.size() / i2;
            } else {
                i = (list.size() / i2) + 1;
            }
            int i3 = i;
            int i4 = 0;
            while (i4 < i3) {
                int i5 = i4 * i2;
                int i6 = i4 + 1;
                int min = Math.min(list.size(), i6 * i2);
                List<Reference> subList = list2.subList(i5, min);
                BufferDBChangeParamList bufferDBChangeParamList2 = new BufferDBChangeParamList();
                bufferDBChangeParamList2.mChangelst = new ArrayList<>(bufferDBChangeParamList.mChangelst.subList(i5, min));
                String str3 = this.TAG;
                Log.i(str3, "Start, End: " + i5 + " " + min + " newlistsize: " + subList.size());
                BulkUpdate createNewBulkUpdateParam = createNewBulkUpdateParam(subList, new String[]{str}, OperationEnum.RemoveFlag);
                Reference[] referenceArr = createNewBulkUpdateParam.objects.objectReference;
                if (referenceArr != null && referenceArr.length > 0) {
                    this.mWorkingQueue.offer(new CloudMessageBulkUpdate(this, createNewBulkUpdateParam, this.mLine, this.mSyncMsgType, bufferDBChangeParamList2, this.mStoreClient));
                }
                i4 = i6;
            }
        }
    }
}
