package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.ReqConstant;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestAccount extends BaseProvisionAPIRequest {
    private static final String ACCOUNT_STATUS_Active = "Active";
    private static final String ACCOUNT_STATUS_PROVISIONED = "Provisioned";
    private static final long serialVersionUID = -8780447710529534093L;
    /* access modifiers changed from: private */
    public String TAG = RequestAccount.class.getSimpleName();

    public RequestAccount(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        super("application/json", iAPICallFlowListener, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int access$300;
                String dataString = httpResponseParams.getDataString();
                String r1 = RequestAccount.this.TAG;
                Log.d(r1, "onComplete StatusCode: " + httpResponseParams.getStatusCode() + " strbody: " + IMSLog.checker(dataString));
                if (httpResponseParams.getStatusCode() == 200 && !TextUtils.isEmpty(dataString)) {
                    try {
                        JSONArray jSONArray = new JSONObject(dataString).getJSONObject("serviceAccountList").getJSONArray("serviceAccount");
                        if (jSONArray != null) {
                            if (jSONArray.length() != 0) {
                                String string = jSONArray.getJSONObject(0).getString("status");
                                String r2 = RequestAccount.this.TAG;
                                Log.d(r2, "200OK non empty response, status: " + string);
                                if (!RequestAccount.ACCOUNT_STATUS_Active.equals(string)) {
                                    if (!RequestAccount.ACCOUNT_STATUS_PROVISIONED.equals(string)) {
                                        RequestAccount.this.goSuccessfulCall(ReqConstant.HAPPY_PATH_REQACCOUNT_GET_TC);
                                        return;
                                    }
                                }
                                RequestAccount.this.goSuccessfulCall(ReqConstant.HAPPY_PATH_REQACCOUNT_EXIST_USER);
                                return;
                            }
                        }
                        RequestAccount.this.goSuccessfulCall(ReqConstant.HAPPY_PATH_REQACCOUNT_GET_TC);
                        return;
                    } catch (JSONException e) {
                        Log.e(RequestAccount.this.TAG, e.getMessage());
                    }
                } else if ((httpResponseParams.getStatusCode() == 503 || httpResponseParams.getStatusCode() == 429) && (access$300 = RequestAccount.this.checkRetryAfter(httpResponseParams)) > 0) {
                    iAPICallFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, access$300);
                    return;
                }
                if (!RequestAccount.this.checkAndHandleCPSError(dataString)) {
                    RequestAccount.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = RequestAccount.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                RequestAccount.this.goFailedCall();
            }
        });
    }

    public static void handleExternalUserOptIn(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        iAPICallFlowListener.onSuccessfulCall((IHttpAPICommonInterface) new RequestAccount(iAPICallFlowListener, messageStoreClient), ReqConstant.HAPPY_PATH_BINARY_SMS_PROVISIONED);
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.CPS_HOST_NAME + "/svcaccount/v1/" + ATTGlobalVariables.MSG_STORE_SERVICE_NAME);
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return new RequestAccount(iAPICallFlowListener, messageStoreClient);
    }
}
