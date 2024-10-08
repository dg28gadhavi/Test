package com.sec.internal.ims.cmstore.mcs.provision.cloudrequest;

import android.os.Bundle;
import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestGetUser extends BaseProvisionRequest {
    /* access modifiers changed from: private */
    public String TAG = RequestGetUser.class.getSimpleName();
    private final String mMsisdn;
    /* access modifiers changed from: private */
    public final int mPhoneId;

    public RequestGetUser(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, final String str) {
        super(iAPICallFlowListener, messageStoreClient);
        this.mMsisdn = Util.encodeRFC3986(str);
        this.mHttpInterface = this;
        this.mPhoneId = messageStoreClient.getClientID();
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBasic());
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String dataString = httpResponseParams.getDataString();
                int statusCode = httpResponseParams.getStatusCode();
                String r4 = RequestGetUser.this.TAG;
                int r5 = RequestGetUser.this.mPhoneId;
                EventLogHelper.infoLogAndAdd(r4, r5, "resultCode: " + statusCode);
                String r42 = RequestGetUser.this.TAG;
                int r52 = RequestGetUser.this.mPhoneId;
                IMSLog.i(r42, r52, "strbody: " + IMSLog.numberChecker(dataString));
                if (statusCode == 200 && !TextUtils.isEmpty(dataString)) {
                    String str = "";
                    try {
                        if (RequestGetUser.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
                            str = RequestGetUser.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, false);
                        }
                        JSONObject jSONObject = TextUtils.isEmpty(str) ? new JSONObject(dataString) : new JSONObject(str);
                        if (TextUtils.equals(jSONObject.getString(McsConstants.Auth.MDN), str)) {
                            String string = jSONObject.getString(McsConstants.Auth.ROOT_CLIENT_ID);
                            String string2 = jSONObject.getString(McsConstants.Auth.CONSENT_CONTEXT);
                            Bundle bundle = new Bundle();
                            bundle.putString(McsConstants.Auth.ROOT_CLIENT_ID, string);
                            bundle.putString(McsConstants.Auth.CONSENT_CONTEXT, string2);
                            RequestGetUser.this.goSuccessfulCall(bundle);
                            return;
                        }
                    } catch (JSONException e) {
                        IMSLog.e(RequestGetUser.this.TAG, RequestGetUser.this.mPhoneId, e.getMessage());
                    }
                    RequestGetUser.this.goFailedCall(statusCode);
                } else if (statusCode == 404) {
                    RequestGetUser.this.goFailedCall(statusCode);
                } else if (RequestGetUser.this.isErrorCodeSupported(statusCode)) {
                    int checkRetryAfter = RequestGetUser.this.checkRetryAfter(httpResponseParams, RequestGetUser.this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(RequestGetUser.this.mHttpInterface.getClass().getSimpleName()));
                    if (checkRetryAfter > 0) {
                        iAPICallFlowListener.onOverRequest(RequestGetUser.this.mHttpInterface, String.valueOf(statusCode), checkRetryAfter);
                    }
                } else {
                    RequestGetUser.this.goFailedCall(statusCode);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = RequestGetUser.this.TAG;
                int r1 = RequestGetUser.this.mPhoneId;
                IMSLog.e(r0, r1, "Http request onFail: " + iOException.getMessage());
                RequestGetUser.this.goFailedCall(802);
            }
        });
    }

    public void updateUrl() {
        setUrl(this.mStoreClient.getPrerenceManager().getOasisAuthRoot() + "/oapi/v1/auth/user?mdn=" + this.mMsisdn);
    }
}
