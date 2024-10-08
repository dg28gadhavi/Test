package com.sec.internal.ims.cmstore.mcs.provision.cloudrequest;

import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.log.IMSLog;
import java.io.IOException;

public class RequestDeleteAccount extends BaseProvisionRequest {
    /* access modifiers changed from: private */
    public String TAG = RequestDeleteAccount.class.getSimpleName();
    String mMsisdn;
    /* access modifiers changed from: private */
    public final int mPhoneId;

    public RequestDeleteAccount(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, String str) {
        super(iAPICallFlowListener, messageStoreClient);
        this.mMsisdn = str;
        this.mHttpInterface = this;
        this.mPhoneId = messageStoreClient.getClientID();
        setMethod(HttpRequestParams.Method.DELETE);
        updateUrl();
        setCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String dataString = httpResponseParams.getDataString();
                int statusCode = httpResponseParams.getStatusCode();
                String r1 = RequestDeleteAccount.this.TAG;
                int r2 = RequestDeleteAccount.this.mPhoneId;
                EventLogHelper.infoLogAndAdd(r1, r2, "resultCode: " + statusCode);
                String r12 = RequestDeleteAccount.this.TAG;
                int r22 = RequestDeleteAccount.this.mPhoneId;
                IMSLog.i(r12, r22, "strBody: " + IMSLog.numberChecker(dataString));
                if (statusCode == 200 || statusCode == 204) {
                    RequestDeleteAccount.this.goSuccessfulCall((Object) null);
                } else {
                    RequestDeleteAccount.this.goFailedCall(statusCode);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = RequestDeleteAccount.this.TAG;
                int r1 = RequestDeleteAccount.this.mPhoneId;
                IMSLog.e(r0, r1, "Http request onFail: " + iOException.getMessage());
                RequestDeleteAccount.this.goFailedCall(802);
            }
        });
    }

    private void updateUrl() {
        setUrl(this.mStoreClient.getPrerenceManager().getOasisServerRoot() + "/oapi/v1/" + this.mMsisdn + "/account");
    }
}
