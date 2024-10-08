package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
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

public class RequestAccountEligibility extends BaseProvisionAPIRequest {
    private static final long serialVersionUID = 6388797514968224882L;
    /* access modifiers changed from: private */
    public String TAG = RequestAccountEligibility.class.getSimpleName();

    public RequestAccountEligibility(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super("application/json", iAPICallFlowListener, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String dataString = httpResponseParams.getDataString();
                String r1 = RequestAccountEligibility.this.TAG;
                Log.d(r1, "onComplete StatusCode: " + httpResponseParams.getStatusCode() + " strbody: " + IMSLog.checker(dataString));
                if (httpResponseParams.getStatusCode() == 503 || httpResponseParams.getStatusCode() == 429) {
                    int access$000 = RequestAccountEligibility.this.checkRetryAfter(httpResponseParams);
                    if (access$000 > 0) {
                        iAPICallFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, access$000);
                        return;
                    }
                } else if (httpResponseParams.getStatusCode() == 200 && !TextUtils.isEmpty(dataString)) {
                    try {
                        boolean z = new JSONObject(dataString).getJSONObject("serviceEligibilityList").getJSONArray("serviceEligibility").getJSONObject(0).getBoolean("isEligible");
                        String r12 = RequestAccountEligibility.this.TAG;
                        Log.d(r12, "account eligible: " + z);
                        if (z) {
                            RequestAccountEligibility.this.goSuccessfulCall();
                            return;
                        } else {
                            RequestAccountEligibility.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_ACCOUNT_NOT_ELIGIBLE);
                            return;
                        }
                    } catch (JSONException e) {
                        Log.e(RequestAccountEligibility.this.TAG, e.getMessage());
                    }
                }
                if (!RequestAccountEligibility.this.checkAndHandleCPSError(dataString)) {
                    RequestAccountEligibility.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = RequestAccountEligibility.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                RequestAccountEligibility.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
            }
        });
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.CPS_HOST_NAME + "/svcaccount/v1/eligibility/" + ATTGlobalVariables.MSG_STORE_SERVICE_NAME);
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return new RequestAccountEligibility(iAPICallFlowListener, messageStoreClient, iCloudMessageManagerHelper);
    }
}
