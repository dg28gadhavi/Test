package com.sec.internal.ims.cmstore.mcs.provision.cloudrequest;

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

public class RequestUserRegistration extends BaseProvisionRequest {
    /* access modifiers changed from: private */
    public String TAG = RequestUserRegistration.class.getSimpleName();
    private String mConsentContext;
    private final String mMsisdn;
    /* access modifiers changed from: private */
    public final int mPhoneId;

    public RequestUserRegistration(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, String str, String str2) {
        super(iAPICallFlowListener, messageStoreClient);
        this.mMsisdn = str;
        this.mConsentContext = str2;
        this.mHttpInterface = this;
        this.mPhoneId = messageStoreClient.getClientID();
        setMethod(HttpRequestParams.Method.POST);
        updateUrl();
        setCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBasic());
        setPostBody(makePostData());
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String dataString = httpResponseParams.getDataString();
                int statusCode = httpResponseParams.getStatusCode();
                String r1 = RequestUserRegistration.this.TAG;
                int r2 = RequestUserRegistration.this.mPhoneId;
                EventLogHelper.infoLogAndAdd(r1, r2, "resultCode: " + statusCode);
                String r12 = RequestUserRegistration.this.TAG;
                int r22 = RequestUserRegistration.this.mPhoneId;
                IMSLog.i(r12, r22, "strBody: " + IMSLog.numberChecker(dataString));
                if (statusCode != 200) {
                    RequestUserRegistration.this.goFailedCall(statusCode);
                } else if (!dataString.isEmpty()) {
                    String str = "";
                    try {
                        if (RequestUserRegistration.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
                            str = RequestUserRegistration.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, false);
                        }
                        JSONObject jSONObject = TextUtils.isEmpty(str) ? new JSONObject(dataString) : new JSONObject(str);
                        String string = jSONObject.getString(McsConstants.Auth.AUTHENTICATION_CODE);
                        String r23 = RequestUserRegistration.this.TAG;
                        int r3 = RequestUserRegistration.this.mPhoneId;
                        IMSLog.d(r23, r3, "auth_code: " + string);
                        RequestUserRegistration.this.mStoreClient.getPrerenceManager().saveAuthCode(string);
                        String string2 = jSONObject.getString("oasis_server_root");
                        String r13 = RequestUserRegistration.this.TAG;
                        int r24 = RequestUserRegistration.this.mPhoneId;
                        IMSLog.d(r13, r24, "oasis server root: " + string2);
                        RequestUserRegistration.this.mStoreClient.getPrerenceManager().saveOasisServerRoot(string2);
                        RequestUserRegistration.this.goSuccessfulCall((Object) null);
                    } catch (JSONException e) {
                        IMSLog.e(RequestUserRegistration.this.TAG, RequestUserRegistration.this.mPhoneId, e.getMessage());
                        RequestUserRegistration.this.goFailedCall(statusCode);
                    }
                }
            }

            public void onFail(IOException iOException) {
                String r0 = RequestUserRegistration.this.TAG;
                int r1 = RequestUserRegistration.this.mPhoneId;
                IMSLog.e(r0, r1, "Http request onFail: " + iOException.getMessage());
                RequestUserRegistration.this.goFailedCall(802);
            }
        });
    }

    private void updateUrl() {
        setUrl(this.mStoreClient.getPrerenceManager().getOasisAuthRoot() + "/oapi/v1/auth/user/registration");
    }

    private JSONObject makePostData() {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("id", this.mMsisdn);
            jSONObject.put("registration_code", this.mStoreClient.getPrerenceManager().getRegCode());
            jSONObject.put(McsConstants.Auth.CONSENT_CONTEXT, this.mConsentContext);
            String str = this.TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "json string: " + IMSLog.numberChecker(jSONObject.toString()));
            return jSONObject;
        } catch (JSONException e) {
            IMSLog.e(this.TAG, this.mPhoneId, e.getMessage());
            return null;
        }
    }
}
