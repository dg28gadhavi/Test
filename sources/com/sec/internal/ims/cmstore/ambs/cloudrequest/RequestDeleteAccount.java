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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestDeleteAccount extends BaseProvisionAPIRequest {
    private static final long serialVersionUID = -6638272236079743088L;
    /* access modifiers changed from: private */
    public String TAG = RequestDeleteAccount.class.getSimpleName();

    public RequestDeleteAccount(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super("application/json", iAPICallFlowListener, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        setMethod(HttpRequestParams.Method.DELETE);
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int access$400;
                String dataString = httpResponseParams.getDataString();
                String r1 = RequestDeleteAccount.this.TAG;
                Log.d(r1, "onComplete StatusCode: " + httpResponseParams.getStatusCode() + " strbody: " + IMSLog.checker(dataString));
                if (httpResponseParams.getStatusCode() != 200) {
                    if ((httpResponseParams.getStatusCode() == 503 || httpResponseParams.getStatusCode() == 429) && (access$400 = RequestDeleteAccount.this.checkRetryAfter(httpResponseParams)) > 0) {
                        iAPICallFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, access$400);
                        return;
                    }
                    if (!RequestDeleteAccount.this.checkAndHandleCPSError(dataString)) {
                        RequestDeleteAccount.this.goFailedCall();
                    }
                } else if (TextUtils.isEmpty(dataString)) {
                    RequestDeleteAccount.this.goFailedCall();
                } else {
                    try {
                        JSONArray jSONArray = new JSONObject(dataString).getJSONObject("deletedServiceAccountList").getJSONArray("serviceAccount");
                        if (jSONArray == null || jSONArray.length() == 0 || jSONArray.getJSONObject(0) == null) {
                            RequestDeleteAccount.this.goFailedCall();
                        } else if (jSONArray.getJSONObject(0).has("serviceId")) {
                            Log.d(RequestDeleteAccount.this.TAG, "deleted successfully");
                            RequestDeleteAccount.this.goSuccessfulCall();
                        } else {
                            RequestDeleteAccount.this.goFailedCall();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            public void onFail(IOException iOException) {
                RequestDeleteAccount.this.goFailedCall();
            }
        });
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.CPS_HOST_NAME + "/svcaccount/v1/msgstoreoemtbs?deleteAll=true");
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return new RequestDeleteAccount(iAPICallFlowListener, messageStoreClient, iCloudMessageManagerHelper);
    }
}
