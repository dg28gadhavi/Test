package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
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

public class RequestTC extends BaseProvisionAPIRequest {
    private static final long serialVersionUID = -1949112470222946734L;
    /* access modifiers changed from: private */
    public String TAG = RequestTC.class.getSimpleName();
    /* access modifiers changed from: private */
    public String mTcId = null;
    /* access modifiers changed from: private */
    public String mTcType = ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_URL;

    public RequestTC(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super("application/json", iAPICallFlowListener, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int access$300;
                String dataString = httpResponseParams.getDataString();
                String r1 = RequestTC.this.TAG;
                Log.d(r1, "onComplete StatusCode: " + httpResponseParams.getStatusCode() + " strbodyEmpty:" + TextUtils.isEmpty(dataString) + " strbody: " + IMSLog.checker(dataString));
                if (httpResponseParams.getStatusCode() == 200) {
                    if (!TextUtils.isEmpty(dataString)) {
                        try {
                            RequestTC.this.mTcId = new JSONObject(dataString).getJSONObject("tc").getString("id");
                            RequestTC.this.mStoreClient.getPrerenceManager().saveTermConditionId(RequestTC.this.mTcId);
                            if (!ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_URL.equals(RequestTC.this.mTcType) && !"Text".equals(RequestTC.this.mTcType)) {
                                RequestTC.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
                            }
                            RequestTC.this.goSuccessfulCall();
                            return;
                        } catch (JSONException e) {
                            Log.e(RequestTC.this.TAG, e.getMessage());
                        }
                    }
                } else if ((httpResponseParams.getStatusCode() == 503 || httpResponseParams.getStatusCode() == 429) && (access$300 = RequestTC.this.checkRetryAfter(httpResponseParams)) > 0) {
                    iAPICallFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, access$300);
                    return;
                }
                RequestTC.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
            }

            public void onFail(IOException iOException) {
                String r0 = RequestTC.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                RequestTC.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
            }
        });
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.CPS_HOST_NAME + "/tc/v1/" + ATTGlobalVariables.MSG_STORE_SERVICE_NAME + "?contentType=Url");
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return new RequestTC(iAPICallFlowListener, messageStoreClient, iCloudMessageManagerHelper);
    }
}
