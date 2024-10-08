package com.sec.internal.ims.cmstore.omanetapi.tmoappapi;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.nms.ObjectsOpSearch;
import com.sec.internal.omanetapi.nms.data.SelectionCriteria;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class CloudMessageObjectsOpSearchForVvmNormalSync extends ObjectsOpSearch {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageObjectsOpSearchForVvmNormalSync.class.getSimpleName();
    private static final long serialVersionUID = 976829149172393071L;
    private final String JSON_CURSOR_TAG = "cursor";
    private final String JSON_OBJECT_LIST_TAG = "objectList";
    private transient CloudMessageBufferDBPersister mBufferDB;
    private final SimpleDateFormat mFormatOfName;
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;

    public CloudMessageObjectsOpSearchForVvmNormalSync(IAPICallFlowListener iAPICallFlowListener, String str, String str2, SyncMsgType syncMsgType, boolean z, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName(), str2, messageStoreClient);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        this.mFormatOfName = simpleDateFormat;
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.mBufferDB = CloudMessageBufferDBPersister.getInstance(messageStoreClient.getContext(), messageStoreClient.getClientID(), false);
        SelectionCriteria selectionCriteria = new SelectionCriteria();
        constructSearchParam(str2, syncMsgType, selectionCriteria, z);
        if (!TextUtils.isEmpty(str)) {
            selectionCriteria.fromCursor = str;
        }
        initCommonRequestHeaders(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(str2));
        initPostRequest(selectionCriteria, true);
        final String str3 = str2;
        final SyncMsgType syncMsgType2 = syncMsgType;
        final String str4 = str;
        final IAPICallFlowListener iAPICallFlowListener2 = iAPICallFlowListener;
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            /* JADX WARNING: Code restructure failed: missing block: B:13:0x0071, code lost:
                r7 = r3.getJSONObject("objectList");
             */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onComplete(com.sec.internal.helper.httpclient.HttpResponseParams r7) {
                /*
                    r6 = this;
                    java.lang.String r0 = "objectList"
                    int r1 = r7.getStatusCode()
                    r2 = 206(0xce, float:2.89E-43)
                    r3 = 200(0xc8, float:2.8E-43)
                    if (r1 != r2) goto L_0x000f
                    r7.setStatusCode(r3)
                L_0x000f:
                    java.lang.String r2 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync.TAG
                    java.lang.StringBuilder r4 = new java.lang.StringBuilder
                    r4.<init>()
                    java.lang.String r5 = "Result code = "
                    r4.append(r5)
                    r4.append(r1)
                    java.lang.String r4 = r4.toString()
                    android.util.Log.d(r2, r4)
                    r2 = 0
                    if (r1 != r3) goto L_0x0126
                    com.google.gson.Gson r1 = new com.google.gson.Gson
                    r1.<init>()
                    java.lang.String r3 = r7.getDataString()     // Catch:{ Exception -> 0x003d }
                    java.lang.Class<com.sec.internal.omanetapi.common.data.OMAApiResponseParam> r4 = com.sec.internal.omanetapi.common.data.OMAApiResponseParam.class
                    java.lang.Object r1 = r1.fromJson(r3, r4)     // Catch:{ Exception -> 0x003d }
                    com.sec.internal.omanetapi.common.data.OMAApiResponseParam r1 = (com.sec.internal.omanetapi.common.data.OMAApiResponseParam) r1     // Catch:{ Exception -> 0x003d }
                    goto L_0x00b1
                L_0x003d:
                    r1 = move-exception
                    java.lang.String r3 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync.TAG
                    java.lang.StringBuilder r4 = new java.lang.StringBuilder
                    r4.<init>()
                    java.lang.String r5 = r1.toString()
                    r4.append(r5)
                    java.lang.String r5 = " "
                    r4.append(r5)
                    java.lang.String r4 = r4.toString()
                    android.util.Log.e(r3, r4)
                    r1.printStackTrace()
                    com.sec.internal.omanetapi.common.data.OMAApiResponseParam r1 = new com.sec.internal.omanetapi.common.data.OMAApiResponseParam
                    r1.<init>()
                    org.json.JSONObject r3 = new org.json.JSONObject     // Catch:{ JSONException -> 0x00ab }
                    java.lang.String r7 = r7.getDataString()     // Catch:{ JSONException -> 0x00ab }
                    r3.<init>(r7)     // Catch:{ JSONException -> 0x00ab }
                    boolean r7 = r3.isNull(r0)     // Catch:{ JSONException -> 0x00ab }
                    if (r7 != 0) goto L_0x007e
                    org.json.JSONObject r7 = r3.getJSONObject(r0)     // Catch:{ JSONException -> 0x00ab }
                    if (r7 == 0) goto L_0x007e
                    java.lang.String r0 = "cursor"
                    java.lang.String r7 = r7.getString(r0)     // Catch:{ JSONException -> 0x00ab }
                    goto L_0x0080
                L_0x007e:
                    java.lang.String r7 = ""
                L_0x0080:
                    java.lang.String r0 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync.TAG     // Catch:{ JSONException -> 0x00ab }
                    java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ JSONException -> 0x00ab }
                    r3.<init>()     // Catch:{ JSONException -> 0x00ab }
                    java.lang.String r4 = "cursor=="
                    r3.append(r4)     // Catch:{ JSONException -> 0x00ab }
                    r3.append(r7)     // Catch:{ JSONException -> 0x00ab }
                    java.lang.String r3 = r3.toString()     // Catch:{ JSONException -> 0x00ab }
                    android.util.Log.e(r0, r3)     // Catch:{ JSONException -> 0x00ab }
                    boolean r0 = android.text.TextUtils.isEmpty(r7)     // Catch:{ JSONException -> 0x00ab }
                    if (r0 == 0) goto L_0x00a1
                    r1.objectList = r2     // Catch:{ JSONException -> 0x00ab }
                    goto L_0x00b1
                L_0x00a1:
                    com.sec.internal.omanetapi.nms.data.ObjectList r0 = new com.sec.internal.omanetapi.nms.data.ObjectList     // Catch:{ JSONException -> 0x00ab }
                    r0.<init>()     // Catch:{ JSONException -> 0x00ab }
                    r1.objectList = r0     // Catch:{ JSONException -> 0x00ab }
                    r0.cursor = r7     // Catch:{ JSONException -> 0x00ab }
                    goto L_0x00b1
                L_0x00ab:
                    r7 = move-exception
                    r7.printStackTrace()
                    r1.objectList = r2
                L_0x00b1:
                    if (r1 == 0) goto L_0x011c
                    com.sec.internal.omanetapi.nms.data.ObjectList r7 = r1.objectList
                    if (r7 == 0) goto L_0x011c
                    java.lang.String r0 = r7.cursor
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r1 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r1.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r7 = r1.setObjectList(r7)
                    java.lang.String r1 = r3
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r7 = r7.setLine(r1)
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r1 = r4
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r7 = r7.setSyncType(r1)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r7 = r7.setCursor(r0)
                    boolean r1 = android.text.TextUtils.isEmpty(r0)
                    if (r1 != 0) goto L_0x00fe
                    java.lang.String r1 = r5
                    boolean r0 = r0.equals(r1)
                    if (r0 != 0) goto L_0x00fe
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r0 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.VVM_NORMAL_SYNC_SUMMARY_PARTIAL
                    r7.setActionType(r0)
                    android.os.Message r0 = new android.os.Message
                    r0.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r7 = r7.build()
                    r0.obj = r7
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r7 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.VVM_NORMAL_SYNC_CONTINUE
                    int r7 = r7.getId()
                    r0.what = r7
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r6 = r6
                    r6.onFixedFlowWithMessage(r0)
                    goto L_0x0168
                L_0x00fe:
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r0 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.VVM_NORMAL_SYNC_SUMMARY_COMPLETE
                    r7.setActionType(r0)
                    android.os.Message r0 = new android.os.Message
                    r0.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r7 = r7.build()
                    r0.obj = r7
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r7 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.VVM_NORMAL_SYNC_SUMMARY_COMPLETE
                    int r7 = r7.getId()
                    r0.what = r7
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r6 = r6
                    r6.onFixedFlowWithMessage(r0)
                    goto L_0x0168
                L_0x011c:
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync r7 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync.this
                    java.lang.String r0 = r3
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r6 = r4
                    r7.onSearchComplete(r0, r6)
                    goto L_0x0168
                L_0x0126:
                    r0 = 204(0xcc, float:2.86E-43)
                    if (r1 != r0) goto L_0x0134
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync r7 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync.this
                    java.lang.String r0 = r3
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r6 = r4
                    r7.onSearchComplete(r0, r6)
                    goto L_0x0168
                L_0x0134:
                    r0 = 401(0x191, float:5.62E-43)
                    if (r1 != r0) goto L_0x0141
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync r0 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync.this
                    boolean r7 = r0.handleUnAuthorized(r7)
                    if (r7 == 0) goto L_0x0141
                    goto L_0x0168
                L_0x0141:
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync r7 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync.this
                    com.sec.internal.ims.cmstore.MessageStoreClient r7 = r7.mStoreClient
                    com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r7 = r7.getCloudMessageStrategyManager()
                    com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r7 = r7.getStrategy()
                    boolean r7 = r7.isRetryRequired(r1)
                    if (r7 == 0) goto L_0x015f
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r7 = r6
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r0 = r7
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r6 = r4
                    r7.onFailedCall(r0, r2, r6, r1)
                    goto L_0x0168
                L_0x015f:
                    com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync r7 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync.this
                    java.lang.String r0 = r3
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r6 = r4
                    r7.onSearchComplete(r0, r6)
                L_0x0168:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync.AnonymousClass1.onComplete(com.sec.internal.helper.httpclient.HttpResponseParams):void");
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageObjectsOpSearchForVvmNormalSync.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                CloudMessageObjectsOpSearchForVvmNormalSync.this.mIAPICallFlowListener.onFailedCall(this);
                CloudMessageObjectsOpSearchForVvmNormalSync.this.onSearchComplete(str3, syncMsgType2);
            }
        });
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00a8, code lost:
        if (r5 == null) goto L_0x00d8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00aa, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00cb, code lost:
        if (r5 != null) goto L_0x00aa;
     */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d2  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void constructSearchParam(java.lang.String r12, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r13, com.sec.internal.omanetapi.nms.data.SelectionCriteria r14, boolean r15) {
        /*
            r11 = this;
            com.sec.internal.ims.cmstore.MessageStoreClient r0 = r11.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r0.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
            int r0 = r0.getMaxSearchEntry()
            r14.maxEntries = r0
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.DEFAULT
            boolean r0 = r0.equals(r13)
            r1 = 0
            java.lang.String r2 = ""
            if (r0 == 0) goto L_0x0042
            boolean r11 = com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.isGcmReplacePolling()
            if (r11 == 0) goto L_0x0041
            if (r15 != 0) goto L_0x0041
            com.sec.internal.omanetapi.nms.data.SearchCriteria r11 = new com.sec.internal.omanetapi.nms.data.SearchCriteria
            r11.<init>()
            com.sec.internal.omanetapi.nms.data.SearchCriterion r12 = new com.sec.internal.omanetapi.nms.data.SearchCriterion
            r12.<init>()
            com.sec.internal.omanetapi.nms.data.SearchCriterion[] r12 = new com.sec.internal.omanetapi.nms.data.SearchCriterion[]{r12}
            r13 = r12[r1]
            java.lang.String r15 = "PresetSearch"
            r13.type = r15
            java.lang.String r15 = "UPOneDotO"
            r13.name = r15
            r13.value = r2
            r11.criterion = r12
            r14.searchCriteria = r11
        L_0x0041:
            return
        L_0x0042:
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r15 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.VM
            boolean r15 = r15.equals(r13)
            java.lang.String r0 = "Date"
            r3 = 0
            if (r15 == 0) goto L_0x00d6
            java.lang.String r15 = com.sec.internal.ims.cmstore.helper.TMOVariables.TmoMessageFolderId.mVVMailInbox
            r4 = 1
            java.lang.String[] r7 = new java.lang.String[r4]     // Catch:{ Exception -> 0x00b0, all -> 0x00ae }
            java.lang.String r5 = "timeStamp"
            r7[r1] = r5     // Catch:{ Exception -> 0x00b0, all -> 0x00ae }
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r5 = r11.mBufferDB     // Catch:{ Exception -> 0x00b0, all -> 0x00ae }
            r6 = 17
            r8 = 0
            r9 = 0
            java.lang.String r10 = "timeStamp DESC"
            android.database.Cursor r5 = r5.queryTable((int) r6, (java.lang.String[]) r7, (java.lang.String) r8, (java.lang.String[]) r9, (java.lang.String) r10)     // Catch:{ Exception -> 0x00b0, all -> 0x00ae }
            if (r5 == 0) goto L_0x00a7
            boolean r6 = r5.moveToFirst()     // Catch:{ Exception -> 0x00a4 }
            if (r6 == 0) goto L_0x00a7
            java.text.SimpleDateFormat r6 = r11.mFormatOfName     // Catch:{ Exception -> 0x00a4 }
            long r7 = r5.getLong(r1)     // Catch:{ Exception -> 0x00a4 }
            java.lang.Long r7 = java.lang.Long.valueOf(r7)     // Catch:{ Exception -> 0x00a4 }
            java.lang.String r6 = r6.format(r7)     // Catch:{ Exception -> 0x00a4 }
            com.sec.internal.omanetapi.nms.data.SearchCriteria r7 = new com.sec.internal.omanetapi.nms.data.SearchCriteria     // Catch:{ Exception -> 0x00a2 }
            r7.<init>()     // Catch:{ Exception -> 0x00a2 }
            com.sec.internal.omanetapi.nms.data.SearchCriterion[] r4 = new com.sec.internal.omanetapi.nms.data.SearchCriterion[r4]     // Catch:{ Exception -> 0x00a2 }
            com.sec.internal.omanetapi.nms.data.SearchCriterion r8 = new com.sec.internal.omanetapi.nms.data.SearchCriterion     // Catch:{ Exception -> 0x00a2 }
            r8.<init>()     // Catch:{ Exception -> 0x00a2 }
            r4[r1] = r8     // Catch:{ Exception -> 0x00a2 }
            r8.type = r0     // Catch:{ Exception -> 0x00a2 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00a2 }
            r9.<init>()     // Catch:{ Exception -> 0x00a2 }
            java.lang.String r10 = "minDate="
            r9.append(r10)     // Catch:{ Exception -> 0x00a2 }
            r9.append(r6)     // Catch:{ Exception -> 0x00a2 }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x00a2 }
            r8.value = r9     // Catch:{ Exception -> 0x00a2 }
            r7.criterion = r4     // Catch:{ Exception -> 0x00a2 }
            r14.searchCriteria = r7     // Catch:{ Exception -> 0x00a2 }
            goto L_0x00a8
        L_0x00a2:
            r4 = move-exception
            goto L_0x00b3
        L_0x00a4:
            r4 = move-exception
            r6 = r2
            goto L_0x00b3
        L_0x00a7:
            r6 = r2
        L_0x00a8:
            if (r5 == 0) goto L_0x00d8
        L_0x00aa:
            r5.close()
            goto L_0x00d8
        L_0x00ae:
            r11 = move-exception
            goto L_0x00d0
        L_0x00b0:
            r4 = move-exception
            r6 = r2
            r5 = r3
        L_0x00b3:
            java.lang.String r7 = TAG     // Catch:{ all -> 0x00ce }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x00ce }
            r8.<init>()     // Catch:{ all -> 0x00ce }
            java.lang.String r4 = r4.getMessage()     // Catch:{ all -> 0x00ce }
            r8.append(r4)     // Catch:{ all -> 0x00ce }
            r8.append(r2)     // Catch:{ all -> 0x00ce }
            java.lang.String r4 = r8.toString()     // Catch:{ all -> 0x00ce }
            android.util.Log.e(r7, r4)     // Catch:{ all -> 0x00ce }
            if (r5 == 0) goto L_0x00d8
            goto L_0x00aa
        L_0x00ce:
            r11 = move-exception
            r3 = r5
        L_0x00d0:
            if (r3 == 0) goto L_0x00d5
            r3.close()
        L_0x00d5:
            throw r11
        L_0x00d6:
            r15 = r2
            r6 = r15
        L_0x00d8:
            com.sec.internal.ims.cmstore.MessageStoreClient r4 = r11.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r4 = r4.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r4 = r4.getStrategy()
            java.lang.String r4 = r4.getProtocol()
            android.net.Uri$Builder r5 = new android.net.Uri$Builder
            r5.<init>()
            android.net.Uri$Builder r4 = r5.scheme(r4)
            com.sec.internal.ims.cmstore.MessageStoreClient r7 = r11.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r7 = r7.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r7 = r7.getStrategy()
            java.lang.String r7 = r7.getNmsHost()
            android.net.Uri$Builder r4 = r4.encodedAuthority(r7)
            java.lang.String r7 = "nms"
            android.net.Uri$Builder r4 = r4.appendPath(r7)
            com.sec.internal.ims.cmstore.MessageStoreClient r7 = r11.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r7 = r7.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r7 = r7.getStrategy()
            java.lang.String r7 = r7.getOMAApiVersion()
            android.net.Uri$Builder r4 = r4.appendPath(r7)
            com.sec.internal.ims.cmstore.MessageStoreClient r11 = r11.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r11 = r11.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r11 = r11.getStrategy()
            java.lang.String r11 = r11.getStoreName()
            android.net.Uri$Builder r11 = r4.appendPath(r11)
            android.net.Uri$Builder r11 = r11.appendPath(r12)
            java.lang.String r12 = "folders"
            android.net.Uri$Builder r11 = r11.appendPath(r12)
            r11.appendPath(r15)
            com.sec.internal.omanetapi.nms.data.Reference r11 = new com.sec.internal.omanetapi.nms.data.Reference     // Catch:{ MalformedURLException -> 0x014f }
            r11.<init>()     // Catch:{ MalformedURLException -> 0x014f }
            java.net.URL r12 = new java.net.URL     // Catch:{ MalformedURLException -> 0x014d }
            android.net.Uri r15 = r5.build()     // Catch:{ MalformedURLException -> 0x014d }
            java.lang.String r15 = r15.toString()     // Catch:{ MalformedURLException -> 0x014d }
            r12.<init>(r15)     // Catch:{ MalformedURLException -> 0x014d }
            r11.resourceURL = r12     // Catch:{ MalformedURLException -> 0x014d }
            goto L_0x016b
        L_0x014d:
            r12 = move-exception
            goto L_0x0151
        L_0x014f:
            r12 = move-exception
            r11 = r3
        L_0x0151:
            java.lang.String r15 = TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r12 = r12.getMessage()
            r4.append(r12)
            r4.append(r2)
            java.lang.String r12 = r4.toString()
            android.util.Log.e(r15, r12)
            r11.resourceURL = r3
        L_0x016b:
            java.lang.String r12 = TAG
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            java.lang.String r2 = "type: "
            r15.append(r2)
            r15.append(r13)
            java.lang.String r13 = ", minDate: "
            r15.append(r13)
            r15.append(r6)
            java.lang.String r13 = r15.toString()
            android.util.Log.i(r12, r13)
            com.sec.internal.omanetapi.nms.data.SortCriteria r12 = new com.sec.internal.omanetapi.nms.data.SortCriteria
            r12.<init>()
            com.sec.internal.omanetapi.nms.data.SortCriterion r13 = new com.sec.internal.omanetapi.nms.data.SortCriterion
            r13.<init>()
            com.sec.internal.omanetapi.nms.data.SortCriterion[] r13 = new com.sec.internal.omanetapi.nms.data.SortCriterion[]{r13}
            r15 = r13[r1]
            r15.type = r0
            java.lang.String r0 = "Ascending"
            r15.order = r0
            r12.criterion = r13
            r14.searchScope = r11
            r14.sortCriteria = r12
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageObjectsOpSearchForVvmNormalSync.constructSearchParam(java.lang.String, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType, com.sec.internal.omanetapi.nms.data.SelectionCriteria, boolean):void");
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener) {
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(getBoxId()));
        return this;
    }

    public void onSearchComplete(String str, SyncMsgType syncMsgType) {
        ParamOMAresponseforBufDB.Builder actionType = new ParamOMAresponseforBufDB.Builder().setLine(str).setSyncType(syncMsgType).setActionType(ParamOMAresponseforBufDB.ActionType.VVM_NORMAL_SYNC_SUMMARY_COMPLETE);
        Message message = new Message();
        message.obj = actionType.build();
        message.what = OMASyncEventType.VVM_NORMAL_SYNC_SUMMARY_COMPLETE.getId();
        this.mIAPICallFlowListener.onFixedFlowWithMessage(message);
    }
}
