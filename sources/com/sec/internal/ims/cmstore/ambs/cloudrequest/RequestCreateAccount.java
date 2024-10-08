package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestCreateAccount extends BaseProvisionAPIRequest {
    private static final long serialVersionUID = -8278931619238563919L;
    /* access modifiers changed from: private */
    public String TAG = RequestCreateAccount.class.getSimpleName();

    public RequestCreateAccount(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super("application/json", iAPICallFlowListener, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        setMethod(HttpRequestParams.Method.POST);
        setPostBody(makePostData());
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String dataString = httpResponseParams.getDataString();
                String r1 = RequestCreateAccount.this.TAG;
                Log.d(r1, "onComplete StatusCode: " + httpResponseParams.getStatusCode() + " strbody: " + IMSLog.checker(dataString));
                if (httpResponseParams.getStatusCode() == 201) {
                    RequestCreateAccount.this.mStoreClient.getPrerenceManager().saveLastApiRequestCreateAccount(true);
                    if (RequestCreateAccount.this.mStoreClient.getPrerenceManager().getUserTbs()) {
                        RequestCreateAccount.this.mStoreClient.getPrerenceManager().saveUserTbsRquired(false);
                    }
                    RequestCreateAccount.this.goSuccessfulCall();
                    return;
                }
                if (httpResponseParams.getStatusCode() == 503 || httpResponseParams.getStatusCode() == 429) {
                    int access$400 = RequestCreateAccount.this.checkRetryAfter(httpResponseParams);
                    if (access$400 > 0) {
                        iAPICallFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, access$400);
                        return;
                    }
                } else if (httpResponseParams.getStatusCode() == 200 && RequestCreateAccount.this.checkAndHandleCPSError(dataString)) {
                    return;
                }
                RequestCreateAccount.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
            }

            public void onFail(IOException iOException) {
                String r0 = RequestCreateAccount.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                RequestCreateAccount.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
            }
        });
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.CPS_HOST_NAME + "/svcaccount/v1/" + ATTGlobalVariables.MSG_STORE_SERVICE_NAME);
    }

    private JSONObject makePostData() {
        String termConditionId = this.mStoreClient.getPrerenceManager().getTermConditionId();
        try {
            JSONObject jSONObject = new JSONObject();
            JSONObject jSONObject2 = new JSONObject();
            JSONObject jSONObject3 = new JSONObject();
            String str = this.TAG;
            Log.d(str, "id: " + termConditionId);
            jSONObject2.put("id", termConditionId);
            jSONObject2.put("action", "Accept");
            jSONObject3.put("tc", jSONObject2);
            jSONObject.put("createServiceAccountRequest", jSONObject3);
            return jSONObject;
        } catch (JSONException e) {
            Log.e(this.TAG, e.getMessage());
            return null;
        }
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return new RequestCreateAccount(iAPICallFlowListener, messageStoreClient, iCloudMessageManagerHelper);
    }
}
