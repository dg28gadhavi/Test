package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.nms.BulkDeletion;
import com.sec.internal.omanetapi.nms.data.BulkDelete;
import java.io.IOException;

public class CloudMessageBulkDeletion extends BulkDeletion {
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageBulkDeletion.class.getSimpleName();
    protected int bulkDeleteRetryCount = 0;
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    protected int responseCode;

    public CloudMessageBulkDeletion(IAPICallFlowListener iAPICallFlowListener, BulkDelete bulkDelete, String str, SyncMsgType syncMsgType, BufferDBChangeParamList bufferDBChangeParamList, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName(), str, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mIAPICallFlowListener = iAPICallFlowListener;
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(str));
        initDeleteRequest(bulkDelete, true);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isPostMethodForBulkDelete()) {
            setMethod(HttpRequestParams.Method.POST);
        }
        final String str2 = str;
        final SyncMsgType syncMsgType2 = syncMsgType;
        final BufferDBChangeParamList bufferDBChangeParamList2 = bufferDBChangeParamList;
        final IAPICallFlowListener iAPICallFlowListener2 = iAPICallFlowListener;
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            /* JADX WARNING: Removed duplicated region for block: B:40:0x00ff  */
            /* JADX WARNING: Removed duplicated region for block: B:42:0x010d  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onComplete(com.sec.internal.helper.httpclient.HttpResponseParams r9) {
                /*
                    r8 = this;
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion r0 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion.this
                    java.lang.String r0 = r0.TAG
                    java.lang.StringBuilder r1 = new java.lang.StringBuilder
                    r1.<init>()
                    java.lang.String r2 = "Result code = "
                    r1.append(r2)
                    int r2 = r9.getStatusCode()
                    r1.append(r2)
                    java.lang.String r1 = r1.toString()
                    android.util.Log.i(r0, r1)
                    int r0 = r9.getStatusCode()
                    r1 = 401(0x191, float:5.62E-43)
                    if (r0 != r1) goto L_0x002f
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion r0 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion.this
                    boolean r0 = r0.handleUnAuthorized(r9)
                    if (r0 == 0) goto L_0x002f
                    return
                L_0x002f:
                    int r0 = r9.getStatusCode()
                    r1 = 206(0xce, float:2.89E-43)
                    r2 = 200(0xc8, float:2.8E-43)
                    if (r0 != r1) goto L_0x003c
                    r9.setStatusCode(r2)
                L_0x003c:
                    int r0 = r9.getStatusCode()
                    r1 = 400(0x190, float:5.6E-43)
                    if (r0 != r1) goto L_0x0047
                    r9.setStatusCode(r2)
                L_0x0047:
                    int r0 = r9.getStatusCode()
                    r1 = 404(0x194, float:5.66E-43)
                    if (r0 != r1) goto L_0x0052
                    r9.setStatusCode(r2)
                L_0x0052:
                    int r0 = r9.getStatusCode()
                    r1 = 405(0x195, float:5.68E-43)
                    if (r0 != r1) goto L_0x005d
                    r9.setStatusCode(r2)
                L_0x005d:
                    int r0 = r9.getStatusCode()
                    r1 = 450(0x1c2, float:6.3E-43)
                    if (r0 != r1) goto L_0x0068
                    r9.setStatusCode(r2)
                L_0x0068:
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion r0 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion.this
                    int r1 = r9.getStatusCode()
                    r0.responseCode = r1
                    int r0 = r9.getStatusCode()
                    r1 = 0
                    if (r0 == r2) goto L_0x009e
                    int r0 = r9.getStatusCode()
                    r3 = 204(0xcc, float:2.86E-43)
                    if (r0 != r3) goto L_0x0080
                    goto L_0x009e
                L_0x0080:
                    int r0 = r9.getStatusCode()
                    r2 = 403(0x193, float:5.65E-43)
                    if (r0 != r2) goto L_0x009c
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r0.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setBulkResponseList(r1)
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r2 = r4
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParamList) r2)
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r0 = r0.build()
                    goto L_0x00d2
                L_0x009c:
                    r5 = r1
                    goto L_0x00d3
                L_0x009e:
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                    r0.<init>()
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r3 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setActionType(r3)
                    java.lang.String r3 = r2
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setLine(r3)
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r3 = r3
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setSyncType(r3)
                    int r3 = r9.getStatusCode()
                    if (r3 != r2) goto L_0x00ce
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion r2 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion.this
                    com.sec.internal.omanetapi.common.data.OMAApiResponseParam r2 = r2.getResponse(r9)
                    if (r2 != 0) goto L_0x00c4
                    return
                L_0x00c4:
                    com.sec.internal.omanetapi.nms.data.BulkResponseList r2 = r2.bulkResponseList
                    r0.setBulkResponseList(r2)
                    com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r2 = r4
                    r0.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParamList) r2)
                L_0x00ce:
                    com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r0 = r0.build()
                L_0x00d2:
                    r5 = r0
                L_0x00d3:
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion r0 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion.this
                    com.sec.internal.ims.cmstore.MessageStoreClient r0 = r0.mStoreClient
                    com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r0.getCloudMessageStrategyManager()
                    com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
                    boolean r0 = r0.isEnableATTHeader()
                    if (r0 != 0) goto L_0x010d
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion r0 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion.this
                    com.sec.internal.ims.cmstore.MessageStoreClient r0 = r0.mStoreClient
                    com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r0.getCloudMessageStrategyManager()
                    com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
                    int r2 = r9.getStatusCode()
                    boolean r0 = r0.isRetryRequired(r2)
                    if (r0 == 0) goto L_0x010d
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r0 = r5
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r2 = r6
                    com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r8 = r3
                    int r9 = r9.getStatusCode()
                    r0.onFailedCall(r2, r1, r8, r9)
                    return
                L_0x010d:
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion r2 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r3 = r5
                    r6 = 0
                    r7 = -2147483648(0xffffffff80000000, float:-0.0)
                    r4 = r9
                    boolean r9 = r2.shouldCareAfterResponsePreProcess(r3, r4, r5, r6, r7)
                    if (r9 != 0) goto L_0x011c
                    return
                L_0x011c:
                    com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion r9 = com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion.this
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r9 = r9.mIAPICallFlowListener
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r8 = r6
                    r9.onFailedCall(r8)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion.AnonymousClass1.onComplete(com.sec.internal.helper.httpclient.HttpResponseParams):void");
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageBulkDeletion.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                CloudMessageBulkDeletion.this.mIAPICallFlowListener.onFailedCall(this);
            }
        });
    }

    public int getRetryCount() {
        return this.bulkDeleteRetryCount;
    }

    public void increaseRetryCount() {
        this.bulkDeleteRetryCount++;
    }

    public int getResponseCode() {
        return this.responseCode;
    }
}
