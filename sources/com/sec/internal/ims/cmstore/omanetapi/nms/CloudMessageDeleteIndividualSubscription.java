package com.sec.internal.ims.cmstore.omanetapi.nms;

import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.nms.IndividualSubscription;
import java.io.IOException;

public class CloudMessageDeleteIndividualSubscription extends IndividualSubscription {
    private static final long serialVersionUID = 1;

    public CloudMessageDeleteIndividualSubscription(final IAPICallFlowListener iAPICallFlowListener, String str, MessageStoreClient messageStoreClient) {
        super(str, messageStoreClient);
        this.mBaseUrl = Util.replaceUrlPrefix(this.mBaseUrl, messageStoreClient);
        String validTokenByLine = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(this.mStoreClient.getPrerenceManager().getUserTelCtn());
        if (this.isCmsMcsEnabled) {
            initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        } else {
            initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), validTokenByLine);
        }
        initCommonDeleteRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onFail(IOException iOException) {
            }

            public void onComplete(HttpResponseParams httpResponseParams) {
                if (httpResponseParams.getStatusCode() == 401 && CloudMessageDeleteIndividualSubscription.this.handleUnAuthorized(httpResponseParams)) {
                    return;
                }
                if (CloudMessageDeleteIndividualSubscription.this.isCmsMcsEnabled || CloudMessageDeleteIndividualSubscription.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    CloudMessageDeleteIndividualSubscription.this.mStoreClient.getPrerenceManager().saveOMASubscriptionTime(0);
                    CloudMessageDeleteIndividualSubscription.this.mStoreClient.getPrerenceManager().saveOMASubscriptionChannelDuration(0);
                    if (httpResponseParams.getStatusCode() == 200 || httpResponseParams.getStatusCode() == 204) {
                        iAPICallFlowListener.onSuccessfulCall(this);
                    } else {
                        iAPICallFlowListener.onMoveOnToNext(CloudMessageDeleteIndividualSubscription.this, (Object) null);
                    }
                }
            }
        });
    }
}
