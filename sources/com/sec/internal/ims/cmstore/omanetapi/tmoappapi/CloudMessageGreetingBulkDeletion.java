package com.sec.internal.ims.cmstore.omanetapi.tmoappapi;

import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.nms.BulkDeletion;

public class CloudMessageGreetingBulkDeletion extends BulkDeletion {
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageGreetingBulkDeletion.class.getSimpleName();
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public CloudMessageGreetingBulkDeletion(com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r8, com.sec.internal.omanetapi.nms.data.BulkDelete r9, java.lang.String r10, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r11, com.sec.internal.ims.cmstore.params.BufferDBChangeParam r12, com.sec.internal.ims.cmstore.MessageStoreClient r13) {
        /*
            r7 = this;
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r13.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
            java.lang.String r2 = r0.getNmsHost()
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r13.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
            java.lang.String r3 = r0.getOMAApiVersion()
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r13.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
            java.lang.String r4 = r0.getStoreName()
            r1 = r7
            r5 = r10
            r6 = r13
            r1.<init>(r2, r3, r4, r5, r6)
            java.lang.Class<com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion> r0 = com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.class
            java.lang.String r0 = r0.getSimpleName()
            r7.TAG = r0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = r7.TAG
            r0.append(r1)
            java.lang.String r1 = "["
            r0.append(r1)
            int r13 = r13.getClientID()
            r0.append(r13)
            java.lang.String r13 = "]"
            r0.append(r13)
            java.lang.String r13 = r0.toString()
            r7.TAG = r13
            r7.mIAPICallFlowListener = r8
            com.sec.internal.ims.cmstore.MessageStoreClient r13 = r7.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r13 = r13.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r13 = r13.getStrategy()
            java.lang.String r13 = r13.getValidTokenByLine(r10)
            com.sec.internal.ims.cmstore.MessageStoreClient r0 = r7.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r0.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
            java.lang.String r0 = r0.getContentType()
            r7.initCommonRequestHeaders(r0, r13)
            r13 = 1
            r7.initDeleteRequest(r9, r13)
            com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion$1 r9 = new com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion$1
            r0 = r9
            r1 = r7
            r2 = r7
            r3 = r12
            r4 = r8
            r6 = r11
            r0.<init>(r2, r3, r4, r5, r6)
            r7.setCallback(r9)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.tmoappapi.CloudMessageGreetingBulkDeletion.<init>(com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener, com.sec.internal.omanetapi.nms.data.BulkDelete, java.lang.String, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType, com.sec.internal.ims.cmstore.params.BufferDBChangeParam, com.sec.internal.ims.cmstore.MessageStoreClient):void");
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        this.mStoreClient = messageStoreClient;
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(getBoxId()));
        return this;
    }
}
