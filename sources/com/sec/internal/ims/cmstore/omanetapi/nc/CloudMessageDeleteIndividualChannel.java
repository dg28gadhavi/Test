package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.text.TextUtils;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.omanetapi.nc.IndividualNotificationChannel;

public class CloudMessageDeleteIndividualChannel extends IndividualNotificationChannel {
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageDeleteIndividualChannel.class.getSimpleName();
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    private final transient IControllerCommonInterface mIControllerCommonInterface;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public CloudMessageDeleteIndividualChannel(com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r8, com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface r9, java.lang.String r10, boolean r11, com.sec.internal.ims.cmstore.MessageStoreClient r12) {
        /*
            r7 = this;
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r12.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
            java.lang.String r2 = r0.getNcHost()
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r12.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
            java.lang.String r3 = r0.getOMAApiVersion()
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r0 = r12.getPrerenceManager()
            java.lang.String r4 = r0.getUserTelCtn()
            r1 = r7
            r5 = r10
            r6 = r12
            r1.<init>(r2, r3, r4, r5, r6)
            java.lang.Class<com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageDeleteIndividualChannel> r0 = com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageDeleteIndividualChannel.class
            java.lang.String r0 = r0.getSimpleName()
            r7.TAG = r0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = r7.TAG
            r0.append(r1)
            java.lang.String r1 = "["
            r0.append(r1)
            int r12 = r12.getClientID()
            r0.append(r12)
            java.lang.String r12 = "]"
            r0.append(r12)
            java.lang.String r12 = r0.toString()
            r7.TAG = r12
            r7.mIAPICallFlowListener = r8
            r7.mIControllerCommonInterface = r9
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = r7.mStoreClient
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r9 = r9.getPrerenceManager()
            java.lang.String r9 = r9.getUserTelCtn()
            com.sec.internal.ims.cmstore.MessageStoreClient r12 = r7.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r12 = r12.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r12 = r12.getStrategy()
            java.lang.String r9 = r12.getValidTokenByLine(r9)
            com.sec.internal.ims.cmstore.MessageStoreClient r12 = r7.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r12 = r12.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r12 = r12.getStrategy()
            java.lang.String r12 = r12.getContentType()
            r7.initCommonRequestHeaders(r12, r9)
            r7.initDeleteRequest()
            com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageDeleteIndividualChannel$1 r9 = new com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageDeleteIndividualChannel$1
            r0 = r9
            r1 = r7
            r2 = r11
            r3 = r8
            r4 = r7
            r0.<init>(r2, r3, r4, r5)
            r7.setCallback(r9)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageDeleteIndividualChannel.<init>(com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener, com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface, java.lang.String, boolean, com.sec.internal.ims.cmstore.MessageStoreClient):void");
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        String oMAChannelResURL = messageStoreClient.getPrerenceManager().getOMAChannelResURL();
        if (TextUtils.isEmpty(oMAChannelResURL)) {
            return null;
        }
        return new CloudMessageGetIndividualNotificationChannelInfo(this.mIAPICallFlowListener, this.mIControllerCommonInterface, oMAChannelResURL.substring(oMAChannelResURL.lastIndexOf(47) + 1), messageStoreClient);
    }
}
