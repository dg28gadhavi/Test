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
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestPat extends BaseProvisionAPIRequest {
    private static final long serialVersionUID = 2825360222614488236L;
    /* access modifiers changed from: private */
    public String TAG = RequestPat.class.getSimpleName();

    public RequestPat(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iAPICallFlowListener, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int access$400;
                String dataString = httpResponseParams.getDataString();
                if ((httpResponseParams.getStatusCode() == 200 || httpResponseParams.getStatusCode() == 206) && !TextUtils.isEmpty(dataString)) {
                    try {
                        String string = new JSONObject(dataString).getJSONObject("token").getString("Encrypted");
                        if (!TextUtils.isEmpty(string)) {
                            RequestPat.this.mStoreClient.getPrerenceManager().savePATAndTime(string);
                            RequestPat.this.goSuccessfulCall();
                            return;
                        }
                    } catch (JSONException e) {
                        Log.e(RequestPat.this.TAG, e.getMessage());
                    }
                } else if (httpResponseParams.getStatusCode() == 302 || httpResponseParams.getStatusCode() == 404) {
                    RequestPat.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_SESSION_ID);
                    return;
                } else if (httpResponseParams.getStatusCode() == 400) {
                    RequestPat.this.goFailedCall();
                    return;
                } else if ((httpResponseParams.getStatusCode() == 503 || httpResponseParams.getStatusCode() == 429) && (access$400 = RequestPat.this.checkRetryAfter(httpResponseParams)) > 0) {
                    iAPICallFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, access$400);
                    return;
                }
                RequestPat.this.goFailedCall();
            }

            public void onFail(IOException iOException) {
                String r0 = RequestPat.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                RequestPat.this.goFailedCall();
            }
        });
    }

    public void updateUrl() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("https://");
            sb.append(getMsDomainAndSessionHelper());
            sb.append("/line/token?ValidFor=");
            sb.append(URLEncoder.encode(String.valueOf(ReqConstant.PAT_LIFE_CYCLE), "UTF-8"));
            sb.append("&Revision=");
            sb.append(URLEncoder.encode("1", "UTF-8"));
            sb.append("&ApplicationId=");
            sb.append(URLEncoder.encode(ATTGlobalVariables.APPLICATION_ID, "UTF-8"));
            sb.append("&ContextInfo=");
            sb.append(URLEncoder.encode("version=" + ATTGlobalVariables.VERSION_NAME, "UTF-8"));
            setUrl(sb.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e(this.TAG, e.getMessage());
        }
    }

    private String getMsDomainAndSessionHelper() {
        return this.mStoreClient.getPrerenceManager().getRedirectDomain() + "/handset/session" + this.mStoreClient.getPrerenceManager().getMsgStoreSessionId();
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return new RequestPat(iAPICallFlowListener, messageStoreClient, iCloudMessageManagerHelper);
    }
}
