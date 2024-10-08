package com.sec.internal.ims.cmstore.mcs.provision.cloudrequest;

import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.log.IMSLog;
import java.io.IOException;

public class RequestRemoveSd extends BaseProvisionRequest {
    /* access modifiers changed from: private */
    public String TAG = RequestRemoveSd.class.getSimpleName();
    /* access modifiers changed from: private */
    public final int mPhoneId;
    private final String mSdClientId;

    public RequestRemoveSd(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, String str) {
        super(iAPICallFlowListener, messageStoreClient);
        this.mSdClientId = str;
        this.mHttpInterface = this;
        this.mPhoneId = messageStoreClient.getClientID();
        setMethod(HttpRequestParams.Method.DELETE);
        updateUrl();
        setCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int statusCode = httpResponseParams.getStatusCode();
                String r1 = RequestRemoveSd.this.TAG;
                int r2 = RequestRemoveSd.this.mPhoneId;
                EventLogHelper.infoLogAndAdd(r1, r2, "resultCode: " + statusCode);
                if (statusCode == 204 || statusCode == 404) {
                    RequestRemoveSd.this.goSuccessfulCall((Object) null);
                } else if (RequestRemoveSd.this.isErrorCodeSupported(statusCode)) {
                    int checkRetryAfter = RequestRemoveSd.this.checkRetryAfter(httpResponseParams, RequestRemoveSd.this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(RequestRemoveSd.this.mHttpInterface.getClass().getSimpleName()));
                    if (checkRetryAfter > 0) {
                        iAPICallFlowListener.onOverRequest(RequestRemoveSd.this.mHttpInterface, String.valueOf(statusCode), checkRetryAfter);
                    }
                } else {
                    RequestRemoveSd.this.goFailedCall(statusCode);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = RequestRemoveSd.this.TAG;
                int r1 = RequestRemoveSd.this.mPhoneId;
                IMSLog.e(r0, r1, "Http request onFail: " + iOException.getMessage());
                RequestRemoveSd.this.goFailedCall(802);
            }
        });
    }

    public void updateUrl() {
        setUrl(this.mStoreClient.getPrerenceManager().getOasisServerRoot() + "/oapi/v1/device/" + this.mSdClientId);
    }
}
