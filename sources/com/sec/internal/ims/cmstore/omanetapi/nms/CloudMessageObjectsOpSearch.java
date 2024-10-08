package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.nms.ObjectsOpSearch;
import com.sec.internal.omanetapi.nms.data.SelectionCriteria;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class CloudMessageObjectsOpSearch extends ObjectsOpSearch {
    private static final long serialVersionUID = 513693735609008639L;
    private final String JSON_CURSOR_TAG = "cursor";
    private final String JSON_OBJECT_LIST_TAG = "objectList";
    /* access modifiers changed from: private */
    public String TAG = CloudMessageObjectsOpSearch.class.getSimpleName();
    /* access modifiers changed from: private */
    public boolean isCmsMcsEnabled = false;
    private transient CloudMessageBufferDBPersister mBufferDB;
    private final SimpleDateFormat mFormatOfName;
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final boolean mIsFullSync;

    public CloudMessageObjectsOpSearch(IAPICallFlowListener iAPICallFlowListener, String str, String str2, SyncMsgType syncMsgType, boolean z, MessageStoreClient messageStoreClient, boolean z2) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName(), str2, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        this.mFormatOfName = simpleDateFormat;
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.mIsFullSync = z2;
        this.mBufferDB = CloudMessageBufferDBPersister.getInstance(messageStoreClient.getContext(), messageStoreClient.getClientID(), false);
        SelectionCriteria selectionCriteria = new SelectionCriteria();
        constructSearchParam(str2, syncMsgType, selectionCriteria, z);
        if (!TextUtils.isEmpty(str)) {
            selectionCriteria.fromCursor = str;
        }
        boolean isMcsSupported = CmsUtil.isMcsSupported(messageStoreClient.getContext(), messageStoreClient.getClientID());
        this.isCmsMcsEnabled = isMcsSupported;
        if (isMcsSupported) {
            initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        } else {
            initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(str2));
        }
        initPostRequest(selectionCriteria, true);
        final IAPICallFlowListener iAPICallFlowListener2 = iAPICallFlowListener;
        final String str3 = str2;
        final SyncMsgType syncMsgType2 = syncMsgType;
        final String str4 = str;
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            /* JADX WARNING: Removed duplicated region for block: B:71:0x0234  */
            /* JADX WARNING: Removed duplicated region for block: B:73:0x023e  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onComplete(com.sec.internal.helper.httpclient.HttpResponseParams r13) {
                /*
                    r12 = this;
                    java.lang.String r0 = "objectList"
                    java.lang.String r1 = ""
                    int r2 = r13.getStatusCode()
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r3 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    java.lang.String r3 = r3.TAG
                    java.lang.StringBuilder r4 = new java.lang.StringBuilder
                    r4.<init>()
                    java.lang.String r5 = "Result code = "
                    r4.append(r5)
                    r4.append(r2)
                    java.lang.String r4 = r4.toString()
                    android.util.Log.i(r3, r4)
                    r3 = 401(0x191, float:5.62E-43)
                    if (r2 != r3) goto L_0x002f
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r3 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    boolean r3 = r3.handleUnAuthorized(r13)
                    if (r3 == 0) goto L_0x002f
                    return
                L_0x002f:
                    r3 = 206(0xce, float:2.89E-43)
                    r4 = 200(0xc8, float:2.8E-43)
                    if (r2 != r3) goto L_0x0039
                    r13.setStatusCode(r4)
                    return
                L_0x0039:
                    java.lang.Class<com.sec.internal.omanetapi.common.data.OMAApiResponseParam> r3 = com.sec.internal.omanetapi.common.data.OMAApiResponseParam.class
                    r5 = 0
                    r6 = -2147483648(0xffffffff80000000, float:-0.0)
                    if (r2 != r4) goto L_0x0188
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r4 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    java.lang.String r7 = r13.getDataString()
                    java.lang.String r4 = r4.getDecryptedString(r7)
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r7 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    boolean r7 = r7.isCmsMcsEnabled
                    if (r7 == 0) goto L_0x006a
                    com.google.gson.GsonBuilder r7 = new com.google.gson.GsonBuilder
                    r7.<init>()
                    com.sec.internal.ims.cmstore.omanetapi.nms.data.GsonInterfaceAdapter r8 = new com.sec.internal.ims.cmstore.omanetapi.nms.data.GsonInterfaceAdapter
                    java.lang.Class<com.sec.internal.omanetapi.nms.data.Attribute> r9 = com.sec.internal.omanetapi.nms.data.Attribute.class
                    r8.<init>(r9)
                    r7.registerTypeAdapter(r9, r8)
                    com.google.gson.GsonBuilder r7 = r7.disableHtmlEscaping()
                    com.google.gson.Gson r7 = r7.create()
                    goto L_0x006f
                L_0x006a:
                    com.google.gson.Gson r7 = new com.google.gson.Gson
                    r7.<init>()
                L_0x006f:
                    java.lang.Object r3 = r7.fromJson(r4, r3)     // Catch:{ Exception -> 0x0076 }
                    com.sec.internal.omanetapi.common.data.OMAApiResponseParam r3 = (com.sec.internal.omanetapi.common.data.OMAApiResponseParam) r3     // Catch:{ Exception -> 0x0076 }
                    goto L_0x00cb
                L_0x0076:
                    r3 = move-exception
                    r3.printStackTrace()
                    com.sec.internal.omanetapi.common.data.OMAApiResponseParam r3 = new com.sec.internal.omanetapi.common.data.OMAApiResponseParam
                    r3.<init>()
                    org.json.JSONObject r7 = new org.json.JSONObject     // Catch:{ JSONException -> 0x00c5 }
                    r7.<init>(r4)     // Catch:{ JSONException -> 0x00c5 }
                    boolean r4 = r7.isNull(r0)     // Catch:{ JSONException -> 0x00c5 }
                    if (r4 != 0) goto L_0x0097
                    org.json.JSONObject r0 = r7.getJSONObject(r0)     // Catch:{ JSONException -> 0x00c5 }
                    if (r0 == 0) goto L_0x0097
                    java.lang.String r4 = "cursor"
                    java.lang.String r0 = r0.getString(r4)     // Catch:{ JSONException -> 0x00c5 }
                    goto L_0x0098
                L_0x0097:
                    r0 = r1
                L_0x0098:
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r4 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this     // Catch:{ JSONException -> 0x00c5 }
                    java.lang.String r4 = r4.TAG     // Catch:{ JSONException -> 0x00c5 }
                    java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ JSONException -> 0x00c5 }
                    r7.<init>()     // Catch:{ JSONException -> 0x00c5 }
                    java.lang.String r8 = "cursor=="
                    r7.append(r8)     // Catch:{ JSONException -> 0x00c5 }
                    r7.append(r0)     // Catch:{ JSONException -> 0x00c5 }
                    java.lang.String r7 = r7.toString()     // Catch:{ JSONException -> 0x00c5 }
                    android.util.Log.i(r4, r7)     // Catch:{ JSONException -> 0x00c5 }
                    boolean r4 = android.text.TextUtils.isEmpty(r0)     // Catch:{ JSONException -> 0x00c5 }
                    if (r4 == 0) goto L_0x00bb
                    r3.objectList = r5     // Catch:{ JSONException -> 0x00c5 }
                    goto L_0x00cb
                L_0x00bb:
                    com.sec.internal.omanetapi.nms.data.ObjectList r4 = new com.sec.internal.omanetapi.nms.data.ObjectList     // Catch:{ JSONException -> 0x00c5 }
                    r4.<init>()     // Catch:{ JSONException -> 0x00c5 }
                    r3.objectList = r4     // Catch:{ JSONException -> 0x00c5 }
                    r4.cursor = r0     // Catch:{ JSONException -> 0x00c5 }
                    goto L_0x00cb
                L_0x00c5:
                    r0 = move-exception
                    r0.printStackTrace()
                    r3.objectList = r5
                L_0x00cb:
                    if (r3 != 0) goto L_0x00df
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r13 = r12.makeSearchCompleteResponse()
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r0 = r2
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r12 = r3
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE
                    int r1 = r1.getId()
                    r0.onSuccessfulEvent(r12, r1, r13)
                    return
                L_0x00df:
                    com.sec.internal.omanetapi.nms.data.ObjectList r0 = r3.objectList
                    if (r0 == 0) goto L_0x014a
                    java.lang.String r1 = r0.cursor
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r3 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r3.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r3.setObjectList(r0)
                    java.lang.String r3 = r4
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setLine(r3)
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r3 = r5
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setSyncType(r3)
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r3 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    boolean r3 = r3.mIsFullSync
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setIsFullSync(r3)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setCursor(r1)
                    boolean r3 = android.text.TextUtils.isEmpty(r1)
                    if (r3 != 0) goto L_0x0130
                    java.lang.String r3 = r6
                    boolean r1 = r1.equals(r3)
                    if (r1 != 0) goto L_0x0130
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r1 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.INIT_SYNC_PARTIAL_SYNC_SUMMARY
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r1 = r0.setActionType(r1)
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_CONTINUE_SEARCH
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r1 = r1.setOMASyncEventType(r3)
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r4 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    com.sec.internal.ims.cmstore.MessageStoreClient r4 = r4.mStoreClient
                    r1.setMStoreClient(r4)
                    int r6 = r3.getId()
                    goto L_0x0145
                L_0x0130:
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r1 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.INIT_SYNC_SUMMARY_COMPLETE
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r1 = r0.setActionType(r1)
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r1 = r1.setOMASyncEventType(r3)
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r3 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    com.sec.internal.ims.cmstore.MessageStoreClient r3 = r3.mStoreClient
                    r1.setMStoreClient(r3)
                L_0x0145:
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r0 = r0.build()
                    goto L_0x0190
                L_0x014a:
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r3 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r3.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r4 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.INIT_SYNC_SUMMARY_COMPLETE
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r3 = r3.setActionType(r4)
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r4 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r3 = r3.setOMASyncEventType(r4)
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r4 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    com.sec.internal.ims.cmstore.MessageStoreClient r4 = r4.mStoreClient
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r3 = r3.setMStoreClient(r4)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r3.setObjectList(r0)
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r3 = r5
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setSyncType(r3)
                    java.lang.String r3 = r4
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setLine(r3)
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r3 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    boolean r3 = r3.mIsFullSync
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setIsFullSync(r3)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setCursor(r1)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r0 = r0.build()
                    goto L_0x0190
                L_0x0188:
                    r0 = 204(0xcc, float:2.86E-43)
                    if (r2 != r0) goto L_0x0193
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r0 = r12.makeSearchCompleteResponse()
                L_0x0190:
                    r9 = r0
                    goto L_0x020b
                L_0x0193:
                    r0 = 405(0x195, float:5.68E-43)
                    if (r2 != r0) goto L_0x020a
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r0 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    com.sec.internal.ims.cmstore.MessageStoreClient r0 = r0.mStoreClient
                    com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r0.getCloudMessageStrategyManager()
                    com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
                    boolean r0 = r0.isEnableATTHeader()
                    if (r0 == 0) goto L_0x020a
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r0 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    java.lang.String r1 = r13.getDataString()
                    java.lang.String r0 = r0.getDecryptedString(r1)
                    com.google.gson.Gson r1 = new com.google.gson.Gson
                    r1.<init>()
                    java.lang.Object r1 = r1.fromJson(r0, r3)     // Catch:{ RuntimeException -> 0x01cb }
                    com.sec.internal.omanetapi.common.data.OMAApiResponseParam r1 = (com.sec.internal.omanetapi.common.data.OMAApiResponseParam) r1     // Catch:{ RuntimeException -> 0x01cb }
                    if (r1 == 0) goto L_0x01c9
                    com.sec.internal.omanetapi.common.data.RequestError r1 = r1.requestError     // Catch:{ RuntimeException -> 0x01cb }
                    com.sec.internal.omanetapi.common.data.PolicyException r1 = r1.policyException     // Catch:{ RuntimeException -> 0x01cb }
                    java.lang.String r0 = r1.messageId     // Catch:{ RuntimeException -> 0x01cb }
                    goto L_0x01d5
                L_0x01c9:
                    r0 = r5
                    goto L_0x01d5
                L_0x01cb:
                    r1 = move-exception
                    r1.printStackTrace()
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r1 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    java.lang.String r0 = r1.getResponseMessageId(r0)
                L_0x01d5:
                    if (r0 == 0) goto L_0x020a
                    java.lang.String r1 = "POL2006"
                    boolean r1 = r0.equals(r1)
                    if (r1 == 0) goto L_0x020a
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r13 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    java.lang.String r13 = r13.TAG
                    java.lang.StringBuilder r1 = new java.lang.StringBuilder
                    r1.<init>()
                    java.lang.String r2 = "messageId is "
                    r1.append(r2)
                    r1.append(r0)
                    java.lang.String r0 = ", remove PersetSearch Filter and resend OpSearch HTTP request"
                    r1.append(r0)
                    java.lang.String r0 = r1.toString()
                    android.util.Log.i(r13, r0)
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r12 = r2
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r13 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.REQUEST_OPSEARCH_AFTER_PSF_REMOVED
                    int r13 = r13.getId()
                    r12.onFailedEvent(r13, r5)
                    return
                L_0x020a:
                    r9 = r5
                L_0x020b:
                    r11 = r6
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r0 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    com.sec.internal.ims.cmstore.MessageStoreClient r0 = r0.mStoreClient
                    com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r0.getCloudMessageStrategyManager()
                    com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
                    boolean r0 = r0.isEnableATTHeader()
                    if (r0 != 0) goto L_0x023e
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r0 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    com.sec.internal.ims.cmstore.MessageStoreClient r0 = r0.mStoreClient
                    com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r0.getCloudMessageStrategyManager()
                    com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
                    boolean r0 = r0.isRetryRequired(r2)
                    if (r0 == 0) goto L_0x023e
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r13 = r2
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r0 = r3
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r12 = r5
                    r13.onFailedCall(r0, r5, r12, r2)
                    return
                L_0x023e:
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch r6 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r7 = r2
                    r10 = 0
                    r8 = r13
                    boolean r13 = r6.shouldCareAfterResponsePreProcess(r7, r8, r9, r10, r11)
                    if (r13 != 0) goto L_0x024b
                    return
                L_0x024b:
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r13 = r12.makeSearchCompleteResponse()
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r0 = r2
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r12 = r3
                    com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE
                    int r1 = r1.getId()
                    r0.onSuccessfulEvent(r12, r1, r13)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.AnonymousClass1.onComplete(com.sec.internal.helper.httpclient.HttpResponseParams):void");
            }

            private ParamOMAresponseforBufDB makeSearchCompleteResponse() {
                return new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.INIT_SYNC_SUMMARY_COMPLETE).setOMASyncEventType(OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE).setMStoreClient(CloudMessageObjectsOpSearch.this.mStoreClient).setLine(str3).setSyncType(syncMsgType2).setIsFullSync(CloudMessageObjectsOpSearch.this.mIsFullSync).setCursor("").build();
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageObjectsOpSearch.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                CloudMessageObjectsOpSearch.this.mIAPICallFlowListener.onFailedCall(this);
            }
        });
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x00c5  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00e7  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00ef  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void constructSearchParam(java.lang.String r18, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r19, com.sec.internal.omanetapi.nms.data.SelectionCriteria r20, boolean r21) {
        /*
            r17 = this;
            r1 = r17
            r2 = r19
            r3 = r20
            com.sec.internal.ims.cmstore.MessageStoreClient r0 = r1.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r0.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
            int r0 = r0.getMaxSearchEntry()
            r3.maxEntries = r0
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.DEFAULT
            boolean r0 = r0.equals(r2)
            java.lang.String r4 = "Ascending"
            java.lang.String r5 = "Date"
            java.lang.String r6 = ""
            r7 = 0
            if (r0 == 0) goto L_0x0085
            boolean r0 = com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.isGcmReplacePolling()
            java.lang.String r1 = "PresetSearch"
            if (r0 == 0) goto L_0x004c
            if (r21 != 0) goto L_0x0084
            com.sec.internal.omanetapi.nms.data.SearchCriteria r0 = new com.sec.internal.omanetapi.nms.data.SearchCriteria
            r0.<init>()
            com.sec.internal.omanetapi.nms.data.SearchCriterion r2 = new com.sec.internal.omanetapi.nms.data.SearchCriterion
            r2.<init>()
            com.sec.internal.omanetapi.nms.data.SearchCriterion[] r2 = new com.sec.internal.omanetapi.nms.data.SearchCriterion[]{r2}
            r4 = r2[r7]
            r4.type = r1
            java.lang.String r1 = "UPOneDotO"
            r4.name = r1
            r4.value = r6
            r0.criterion = r2
            r3.searchCriteria = r0
            goto L_0x0084
        L_0x004c:
            if (r21 != 0) goto L_0x006c
            com.sec.internal.omanetapi.nms.data.SearchCriteria r0 = new com.sec.internal.omanetapi.nms.data.SearchCriteria
            r0.<init>()
            com.sec.internal.omanetapi.nms.data.SearchCriterion r2 = new com.sec.internal.omanetapi.nms.data.SearchCriterion
            r2.<init>()
            com.sec.internal.omanetapi.nms.data.SearchCriterion[] r2 = new com.sec.internal.omanetapi.nms.data.SearchCriterion[]{r2}
            r6 = r2[r7]
            r6.type = r1
            java.lang.String r1 = "path"
            r6.name = r1
            java.lang.String r1 = "/main"
            r6.value = r1
            r0.criterion = r2
            r3.searchCriteria = r0
        L_0x006c:
            com.sec.internal.omanetapi.nms.data.SortCriteria r0 = new com.sec.internal.omanetapi.nms.data.SortCriteria
            r0.<init>()
            com.sec.internal.omanetapi.nms.data.SortCriterion r1 = new com.sec.internal.omanetapi.nms.data.SortCriterion
            r1.<init>()
            com.sec.internal.omanetapi.nms.data.SortCriterion[] r1 = new com.sec.internal.omanetapi.nms.data.SortCriterion[]{r1}
            r2 = r1[r7]
            r2.type = r5
            r2.order = r4
            r0.criterion = r1
            r3.sortCriteria = r0
        L_0x0084:
            return
        L_0x0085:
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.VM
            boolean r0 = r0.equals(r2)
            r8 = 0
            if (r0 == 0) goto L_0x00fa
            java.lang.String r9 = com.sec.internal.ims.cmstore.helper.TMOVariables.TmoMessageFolderId.mVVMailInbox
            boolean r0 = r1.mIsFullSync
            if (r0 != 0) goto L_0x00f3
            r0 = 1
            java.lang.String[] r12 = new java.lang.String[r0]     // Catch:{ Exception -> 0x00cb, all -> 0x00c9 }
            java.lang.String r0 = "timeStamp"
            r12[r7] = r0     // Catch:{ Exception -> 0x00cb, all -> 0x00c9 }
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r10 = r1.mBufferDB     // Catch:{ Exception -> 0x00cb, all -> 0x00c9 }
            r11 = 17
            r13 = 0
            r14 = 0
            java.lang.String r15 = "timeStamp DESC"
            android.database.Cursor r10 = r10.queryTable((int) r11, (java.lang.String[]) r12, (java.lang.String) r13, (java.lang.String[]) r14, (java.lang.String) r15)     // Catch:{ Exception -> 0x00cb, all -> 0x00c9 }
            if (r10 == 0) goto L_0x00c2
            boolean r0 = r10.moveToFirst()     // Catch:{ Exception -> 0x00c0 }
            if (r0 == 0) goto L_0x00c2
            java.text.SimpleDateFormat r0 = r1.mFormatOfName     // Catch:{ Exception -> 0x00c0 }
            long r11 = r10.getLong(r7)     // Catch:{ Exception -> 0x00c0 }
            java.lang.Long r11 = java.lang.Long.valueOf(r11)     // Catch:{ Exception -> 0x00c0 }
            java.lang.String r0 = r0.format(r11)     // Catch:{ Exception -> 0x00c0 }
            goto L_0x00c3
        L_0x00c0:
            r0 = move-exception
            goto L_0x00cd
        L_0x00c2:
            r0 = r6
        L_0x00c3:
            if (r10 == 0) goto L_0x00f4
            r10.close()
            goto L_0x00f4
        L_0x00c9:
            r0 = move-exception
            goto L_0x00ed
        L_0x00cb:
            r0 = move-exception
            r10 = r8
        L_0x00cd:
            java.lang.String r11 = r1.TAG     // Catch:{ all -> 0x00eb }
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ all -> 0x00eb }
            r12.<init>()     // Catch:{ all -> 0x00eb }
            java.lang.String r0 = r0.getMessage()     // Catch:{ all -> 0x00eb }
            r12.append(r0)     // Catch:{ all -> 0x00eb }
            r12.append(r6)     // Catch:{ all -> 0x00eb }
            java.lang.String r0 = r12.toString()     // Catch:{ all -> 0x00eb }
            android.util.Log.e(r11, r0)     // Catch:{ all -> 0x00eb }
            if (r10 == 0) goto L_0x00f3
            r10.close()
            goto L_0x00f3
        L_0x00eb:
            r0 = move-exception
            r8 = r10
        L_0x00ed:
            if (r8 == 0) goto L_0x00f2
            r8.close()
        L_0x00f2:
            throw r0
        L_0x00f3:
            r0 = r6
        L_0x00f4:
            r16 = r9
            r9 = r0
            r0 = r16
            goto L_0x0109
        L_0x00fa:
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.VM_GREETINGS
            boolean r0 = r0.equals(r2)
            if (r0 == 0) goto L_0x0107
            java.lang.String r9 = com.sec.internal.ims.cmstore.helper.TMOVariables.TmoMessageFolderId.mVVMailGreeting
            r0 = r9
            r9 = r6
            goto L_0x0109
        L_0x0107:
            r0 = r6
            r9 = r0
        L_0x0109:
            com.sec.internal.ims.cmstore.MessageStoreClient r10 = r1.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r10 = r10.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r10 = r10.getStrategy()
            java.lang.String r10 = r10.getProtocol()
            android.net.Uri$Builder r11 = new android.net.Uri$Builder
            r11.<init>()
            android.net.Uri$Builder r10 = r11.scheme(r10)
            com.sec.internal.ims.cmstore.MessageStoreClient r12 = r1.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r12 = r12.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r12 = r12.getStrategy()
            java.lang.String r12 = r12.getNmsHost()
            android.net.Uri$Builder r10 = r10.encodedAuthority(r12)
            java.lang.String r12 = "nms"
            android.net.Uri$Builder r10 = r10.appendPath(r12)
            com.sec.internal.ims.cmstore.MessageStoreClient r12 = r1.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r12 = r12.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r12 = r12.getStrategy()
            java.lang.String r12 = r12.getOMAApiVersion()
            android.net.Uri$Builder r10 = r10.appendPath(r12)
            com.sec.internal.ims.cmstore.MessageStoreClient r12 = r1.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r12 = r12.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r12 = r12.getStrategy()
            java.lang.String r12 = r12.getStoreName()
            android.net.Uri$Builder r10 = r10.appendPath(r12)
            r12 = r18
            android.net.Uri$Builder r10 = r10.appendPath(r12)
            java.lang.String r12 = "folders"
            android.net.Uri$Builder r10 = r10.appendPath(r12)
            r10.appendPath(r0)
            com.sec.internal.omanetapi.nms.data.Reference r10 = new com.sec.internal.omanetapi.nms.data.Reference     // Catch:{ MalformedURLException -> 0x0182 }
            r10.<init>()     // Catch:{ MalformedURLException -> 0x0182 }
            java.net.URL r0 = new java.net.URL     // Catch:{ MalformedURLException -> 0x0180 }
            android.net.Uri r11 = r11.build()     // Catch:{ MalformedURLException -> 0x0180 }
            java.lang.String r11 = r11.toString()     // Catch:{ MalformedURLException -> 0x0180 }
            r0.<init>(r11)     // Catch:{ MalformedURLException -> 0x0180 }
            r10.resourceURL = r0     // Catch:{ MalformedURLException -> 0x0180 }
            goto L_0x019e
        L_0x0180:
            r0 = move-exception
            goto L_0x0184
        L_0x0182:
            r0 = move-exception
            r10 = r8
        L_0x0184:
            java.lang.String r11 = r1.TAG
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r0 = r0.getMessage()
            r12.append(r0)
            r12.append(r6)
            java.lang.String r0 = r12.toString()
            android.util.Log.e(r11, r0)
            r10.resourceURL = r8
        L_0x019e:
            java.lang.String r0 = r1.TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r8 = "type: "
            r6.append(r8)
            r6.append(r2)
            java.lang.String r2 = ", minDate: "
            r6.append(r2)
            r6.append(r9)
            java.lang.String r2 = ", mIsFullSync: "
            r6.append(r2)
            boolean r2 = r1.mIsFullSync
            r6.append(r2)
            java.lang.String r2 = r6.toString()
            android.util.Log.i(r0, r2)
            boolean r0 = r1.mIsFullSync
            if (r0 != 0) goto L_0x01fa
            boolean r0 = android.text.TextUtils.isEmpty(r9)
            if (r0 != 0) goto L_0x01fa
            com.sec.internal.omanetapi.nms.data.SearchCriteria r0 = new com.sec.internal.omanetapi.nms.data.SearchCriteria
            r0.<init>()
            com.sec.internal.omanetapi.nms.data.SearchCriterion r1 = new com.sec.internal.omanetapi.nms.data.SearchCriterion
            r1.<init>()
            com.sec.internal.omanetapi.nms.data.SearchCriterion[] r1 = new com.sec.internal.omanetapi.nms.data.SearchCriterion[]{r1}
            r2 = r1[r7]
            r2.type = r5
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r8 = "minDate="
            r6.append(r8)
            r6.append(r9)
            java.lang.String r6 = r6.toString()
            r2.value = r6
            r0.criterion = r1
            r3.searchCriteria = r0
        L_0x01fa:
            com.sec.internal.omanetapi.nms.data.SortCriteria r0 = new com.sec.internal.omanetapi.nms.data.SortCriteria
            r0.<init>()
            com.sec.internal.omanetapi.nms.data.SortCriterion r1 = new com.sec.internal.omanetapi.nms.data.SortCriterion
            r1.<init>()
            com.sec.internal.omanetapi.nms.data.SortCriterion[] r1 = new com.sec.internal.omanetapi.nms.data.SortCriterion[]{r1}
            r2 = r1[r7]
            r2.type = r5
            r2.order = r4
            r0.criterion = r1
            r3.searchScope = r10
            r3.sortCriteria = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch.constructSearchParam(java.lang.String, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType, com.sec.internal.omanetapi.nms.data.SelectionCriteria, boolean):void");
    }
}
