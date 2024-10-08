package com.sec.internal.ims.cmstore.mcs.provision.cloudrequest;

import android.os.Bundle;
import android.text.TextUtils;
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

public class RequestGetAccount extends BaseProvisionRequest {
    /* access modifiers changed from: private */
    public String TAG = RequestGetAccount.class.getSimpleName();
    String mMsisdn;
    /* access modifiers changed from: private */
    public final int mPhoneId;

    public RequestGetAccount(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, String str) {
        super(iAPICallFlowListener, messageStoreClient);
        this.mMsisdn = str;
        this.mHttpInterface = this;
        this.mPhoneId = messageStoreClient.getClientID();
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String dataString = httpResponseParams.getDataString();
                int statusCode = httpResponseParams.getStatusCode();
                String r4 = RequestGetAccount.this.TAG;
                int r5 = RequestGetAccount.this.mPhoneId;
                EventLogHelper.infoLogAndAdd(r4, r5, "resultCode: " + statusCode);
                String r42 = RequestGetAccount.this.TAG;
                int r52 = RequestGetAccount.this.mPhoneId;
                IMSLog.i(r42, r52, "strBody: " + IMSLog.numberChecker(dataString));
                if (statusCode == 200 && !dataString.isEmpty()) {
                    try {
                        String str = "";
                        String decrypt = RequestGetAccount.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted() ? RequestGetAccount.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, true) : str;
                        JSONObject jSONObject = (TextUtils.isEmpty(decrypt) ? new JSONObject(dataString) : new JSONObject(decrypt)).getJSONObject("account");
                        String string = jSONObject.getString("account_id");
                        String r53 = RequestGetAccount.this.TAG;
                        int r6 = RequestGetAccount.this.mPhoneId;
                        IMSLog.d(r53, r6, "accountId: " + string);
                        RequestGetAccount.this.mStoreClient.getPrerenceManager().saveMcsAccountId(string);
                        if (jSONObject.has("alias")) {
                            str = jSONObject.getString("alias");
                            String r2 = RequestGetAccount.this.TAG;
                            int r54 = RequestGetAccount.this.mPhoneId;
                            IMSLog.d(r2, r54, "alias: " + str);
                            RequestGetAccount.this.mStoreClient.getPrerenceManager().saveMcsAlias(str);
                        }
                        String string2 = jSONObject.getString(McsConstants.Auth.CONSENT_CONTEXT);
                        String r22 = RequestGetAccount.this.TAG;
                        int r55 = RequestGetAccount.this.mPhoneId;
                        IMSLog.d(r22, r55, "consentContext: " + string2);
                        Bundle bundle = new Bundle();
                        bundle.putString("alias", str);
                        bundle.putString(McsConstants.Auth.CONSENT_CONTEXT, string2);
                        RequestGetAccount.this.goSuccessfulCall(bundle);
                    } catch (JSONException e) {
                        IMSLog.e(RequestGetAccount.this.TAG, RequestGetAccount.this.mPhoneId, e.getMessage());
                        RequestGetAccount.this.goFailedCall(statusCode);
                    }
                } else if (RequestGetAccount.this.isErrorCodeSupported(statusCode)) {
                    int checkRetryAfter = RequestGetAccount.this.checkRetryAfter(httpResponseParams, RequestGetAccount.this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(RequestGetAccount.this.mHttpInterface.getClass().getSimpleName()));
                    if (checkRetryAfter > 0) {
                        iAPICallFlowListener.onOverRequest(RequestGetAccount.this.mHttpInterface, String.valueOf(statusCode), checkRetryAfter);
                    }
                } else {
                    RequestGetAccount.this.goFailedCall(statusCode);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = RequestGetAccount.this.TAG;
                int r1 = RequestGetAccount.this.mPhoneId;
                IMSLog.e(r0, r1, "Http request onFail: " + iOException.getMessage());
                RequestGetAccount.this.goFailedCall(802);
            }
        });
    }

    private void updateUrl() {
        setUrl(this.mStoreClient.getPrerenceManager().getOasisServerRoot() + "/oapi/v1/" + this.mMsisdn + "/account");
    }
}
