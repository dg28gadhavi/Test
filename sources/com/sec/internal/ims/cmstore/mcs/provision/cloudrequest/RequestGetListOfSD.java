package com.sec.internal.ims.cmstore.mcs.provision.cloudrequest;

import android.text.TextUtils;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.log.IMSLog;
import java.io.IOException;

public class RequestGetListOfSD extends BaseProvisionRequest {
    /* access modifiers changed from: private */
    public String TAG = RequestGetListOfSD.class.getSimpleName();
    /* access modifiers changed from: private */
    public final int mPhoneId;

    public RequestGetListOfSD(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        super(iAPICallFlowListener, messageStoreClient);
        this.mHttpInterface = this;
        this.mPhoneId = messageStoreClient.getClientID();
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String dataString = httpResponseParams.getDataString();
                int statusCode = httpResponseParams.getStatusCode();
                String r1 = RequestGetListOfSD.this.TAG;
                int r2 = RequestGetListOfSD.this.mPhoneId;
                EventLogHelper.infoLogAndAdd(r1, r2, "resultCode: " + statusCode);
                String r12 = RequestGetListOfSD.this.TAG;
                int r22 = RequestGetListOfSD.this.mPhoneId;
                IMSLog.i(r12, r22, "strbody: " + IMSLog.numberChecker(dataString));
                if (statusCode != 200 || TextUtils.isEmpty(dataString)) {
                    RequestGetListOfSD.this.goFailedCall(statusCode);
                    return;
                }
                if (RequestGetListOfSD.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
                    dataString = RequestGetListOfSD.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, true);
                    if (TextUtils.isEmpty(dataString)) {
                        RequestGetListOfSD.this.goFailedCall(statusCode);
                        return;
                    }
                }
                RequestGetListOfSD.this.goSuccessfulCall(dataString);
            }

            public void onFail(IOException iOException) {
                String r0 = RequestGetListOfSD.this.TAG;
                int r1 = RequestGetListOfSD.this.mPhoneId;
                IMSLog.e(r0, r1, "Http request onFail: " + iOException.getMessage());
                RequestGetListOfSD.this.goFailedCall(802);
            }
        });
    }

    public void updateUrl() {
        setUrl(this.mStoreClient.getPrerenceManager().getOasisServerRoot() + "/oapi/v1/device");
    }
}
