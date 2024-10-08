package com.sec.internal.ims.cmstore.mcs.provision.cloudrequest;

import android.text.TextUtils;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.log.IMSLog;
import java.io.IOException;

public class RequestGetSD extends BaseProvisionRequest {
    /* access modifiers changed from: private */
    public String TAG = RequestGetSD.class.getSimpleName();
    /* access modifiers changed from: private */
    public final int mPhoneId;

    public RequestGetSD(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, String str) {
        super(iAPICallFlowListener, messageStoreClient);
        this.mHttpInterface = this;
        this.mPhoneId = messageStoreClient.getClientID();
        setMethod(HttpRequestParams.Method.GET);
        updateUrl(str);
        setCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String dataString = httpResponseParams.getDataString();
                int statusCode = httpResponseParams.getStatusCode();
                String r2 = RequestGetSD.this.TAG;
                int r3 = RequestGetSD.this.mPhoneId;
                EventLogHelper.infoLogAndAdd(r2, r3, "resultCode: " + statusCode);
                String r22 = RequestGetSD.this.TAG;
                int r32 = RequestGetSD.this.mPhoneId;
                IMSLog.i(r22, r32, "strbody: " + IMSLog.numberChecker(dataString));
                if (statusCode == 200 && !TextUtils.isEmpty(dataString)) {
                    if (RequestGetSD.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
                        dataString = RequestGetSD.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, true);
                        if (TextUtils.isEmpty(dataString)) {
                            RequestGetSD.this.goFailedCall(statusCode);
                            return;
                        }
                    }
                    RequestGetSD.this.goSuccessfulCall(dataString);
                } else if (RequestGetSD.this.isErrorCodeSupported(statusCode)) {
                    int checkRetryAfter = RequestGetSD.this.checkRetryAfter(httpResponseParams, RequestGetSD.this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(RequestGetSD.this.mHttpInterface.getClass().getSimpleName()));
                    if (checkRetryAfter > 0) {
                        iAPICallFlowListener.onOverRequest(RequestGetSD.this.mHttpInterface, String.valueOf(statusCode), checkRetryAfter);
                    }
                } else {
                    RequestGetSD.this.goFailedCall(statusCode);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = RequestGetSD.this.TAG;
                int r1 = RequestGetSD.this.mPhoneId;
                IMSLog.e(r0, r1, "Http request onFail: " + iOException.getMessage());
                RequestGetSD.this.goFailedCall(802);
            }
        });
    }

    public void updateUrl(String str) {
        setUrl(this.mStoreClient.getPrerenceManager().getOasisServerRoot() + "/oapi/v1/device/" + str);
    }
}
