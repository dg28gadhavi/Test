package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nms.IndividualSubscription;
import com.sec.internal.omanetapi.nms.data.NmsSubscription;
import com.sec.internal.omanetapi.nms.data.NmsSubscriptionUpdate;
import java.io.IOException;

public class CloudMessageUpdateSubscriptionChannel extends IndividualSubscription {
    private static final long serialVersionUID = -4589569264005795758L;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageUpdateSubscriptionChannel.class.getSimpleName();
    /* access modifiers changed from: private */
    public final transient IControllerCommonInterface mIControllerCommonInterface;

    public CloudMessageUpdateSubscriptionChannel(final IAPICallFlowListener iAPICallFlowListener, String str, String str2, IControllerCommonInterface iControllerCommonInterface, MessageStoreClient messageStoreClient) {
        super(str2, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mIControllerCommonInterface = iControllerCommonInterface;
        this.mBaseUrl = Util.replaceUrlPrefix(this.mBaseUrl, messageStoreClient);
        NmsSubscriptionUpdate nmsSubscriptionUpdate = new NmsSubscriptionUpdate();
        nmsSubscriptionUpdate.duration = 86400;
        nmsSubscriptionUpdate.restartToken = str;
        String userTelCtn = this.mStoreClient.getPrerenceManager().getUserTelCtn();
        ICloudMessageStrategy strategy = this.mStoreClient.getCloudMessageStrategyManager().getStrategy();
        if (this.isCmsMcsEnabled) {
            initMcsCommonRequestHeaders(strategy.getContentType(), strategy.getAuthorizationBearer());
        } else {
            initCommonRequestHeaders(strategy.getContentType(), strategy.getValidTokenByLine(userTelCtn));
        }
        initPostRequest(nmsSubscriptionUpdate, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String r0 = CloudMessageUpdateSubscriptionChannel.this.TAG;
                Log.i(r0, "response = " + httpResponseParams.getDataString());
                if (httpResponseParams.getStatusCode() != 401 || !CloudMessageUpdateSubscriptionChannel.this.handleUnAuthorized(httpResponseParams)) {
                    if (httpResponseParams.getStatusCode() == 206) {
                        httpResponseParams.setStatusCode(200);
                    }
                    if (httpResponseParams.getStatusCode() == 200) {
                        OMAApiResponseParam response = CloudMessageUpdateSubscriptionChannel.this.getResponse(httpResponseParams);
                        if (response == null) {
                            iAPICallFlowListener.onFailedCall(this);
                            return;
                        }
                        NmsSubscription nmsSubscription = response.nmsSubscription;
                        if (nmsSubscription != null) {
                            String str = nmsSubscription.restartToken;
                            int intValue = nmsSubscription.duration.intValue();
                            long j = (((long) intValue) * 1000) - 360000;
                            if (ATTGlobalVariables.isGcmReplacePolling()) {
                                CloudMessageUpdateSubscriptionChannel.this.mIControllerCommonInterface.updateDelay(OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING.getId(), j);
                            } else if (CloudMessageUpdateSubscriptionChannel.this.isCmsMcsEnabled) {
                                CloudMessageUpdateSubscriptionChannel.this.mIControllerCommonInterface.updateDelay(OMASyncEventType.CHECK_SUBSCRIPTION_CHANNEL.getId(), j);
                            }
                            CloudMessageUpdateSubscriptionChannel.this.mStoreClient.getPrerenceManager().saveOMASubscriptionIndex(nmsSubscription.index.longValue() - 1);
                            CloudMessageUpdateSubscriptionChannel.this.mStoreClient.getPrerenceManager().saveOMASubscriptionRestartToken(str);
                            CloudMessageUpdateSubscriptionChannel.this.mStoreClient.getPrerenceManager().saveOMASubscriptionTime(System.currentTimeMillis());
                            CloudMessageUpdateSubscriptionChannel.this.mStoreClient.getPrerenceManager().saveOMASubscriptionChannelDuration(intValue);
                            if (ATTGlobalVariables.isGcmReplacePolling()) {
                                Log.i(CloudMessageUpdateSubscriptionChannel.this.TAG, "use gcm, will not send long polling request");
                            } else if (CloudMessageUpdateSubscriptionChannel.this.isCmsMcsEnabled) {
                                iAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.UPDATE_DELAYED_SUBSCRIPTION_COMPLETE.getId(), (Object) null);
                            } else {
                                iAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.SEND_LONG_POLLING_REQUEST.getId(), (Object) null);
                            }
                        } else {
                            iAPICallFlowListener.onFailedCall(this);
                        }
                    } else if (CloudMessageUpdateSubscriptionChannel.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE) && !CloudMessageUpdateSubscriptionChannel.this.isCmsMcsEnabled) {
                        iAPICallFlowListener.onFailedCall(CloudMessageUpdateSubscriptionChannel.this);
                    }
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageUpdateSubscriptionChannel.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                iAPICallFlowListener.onFailedCall(this);
            }
        });
    }
}
