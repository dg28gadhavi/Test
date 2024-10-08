package com.sec.internal.ims.cmstore.mcs.provision.cloudrequest;

import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestApproveSd extends BaseProvisionRequest {
    /* access modifiers changed from: private */
    public String TAG = RequestApproveSd.class.getSimpleName();
    /* access modifiers changed from: private */
    public final int mPhoneId;
    private final String mUserCode;

    public RequestApproveSd(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, String str) {
        super(iAPICallFlowListener, messageStoreClient);
        this.mUserCode = str;
        this.mHttpInterface = this;
        this.mPhoneId = messageStoreClient.getClientID();
        setMethod(HttpRequestParams.Method.POST);
        updateUrl();
        setCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        setPostBody(makePostData());
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int statusCode = httpResponseParams.getStatusCode();
                String r0 = RequestApproveSd.this.TAG;
                int r1 = RequestApproveSd.this.mPhoneId;
                EventLogHelper.infoLogAndAdd(r0, r1, "resultCode: " + statusCode);
                if (statusCode == 200) {
                    RequestApproveSd.this.goSuccessfulCall((Object) null);
                } else {
                    RequestApproveSd.this.goFailedCall(statusCode);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = RequestApproveSd.this.TAG;
                int r1 = RequestApproveSd.this.mPhoneId;
                IMSLog.e(r0, r1, "Http request onFail: " + iOException.getMessage());
                RequestApproveSd.this.goFailedCall(802);
            }
        });
    }

    public void updateUrl() {
        setUrl(this.mStoreClient.getPrerenceManager().getOasisServerRoot() + "/oapi/v1/device");
    }

    private JSONObject makePostData() {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put(McsConstants.SdInfo.USER_CODE, this.mUserCode);
            String str = this.TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "json string" + IMSLog.numberChecker(jSONObject.toString()));
            return jSONObject;
        } catch (JSONException e) {
            IMSLog.e(this.TAG, this.mPhoneId, e.getMessage());
            return null;
        }
    }
}
