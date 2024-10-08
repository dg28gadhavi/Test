package com.sec.internal.ims.cmstore.mcs.provision.cloudrequest;

import android.os.Bundle;
import android.os.Message;
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

public class RequestUpdateAccount extends BaseProvisionRequest {
    /* access modifiers changed from: private */
    public String TAG = RequestUpdateAccount.class.getSimpleName();
    String mConsentContext;
    Boolean mIsChangedAlias;
    Boolean mIsChangedConsent;
    String mMsisdn;
    /* access modifiers changed from: private */
    public final int mPhoneId;
    String mUserAlias;

    public RequestUpdateAccount(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, String str, Message message) {
        super(iAPICallFlowListener, messageStoreClient);
        Boolean bool = Boolean.FALSE;
        this.mIsChangedAlias = bool;
        this.mUserAlias = null;
        this.mConsentContext = null;
        this.mIsChangedConsent = bool;
        this.mMsisdn = str;
        Object obj = message.obj;
        if (obj != null) {
            Bundle bundle = (Bundle) obj;
            this.mUserAlias = bundle.getString("alias");
            this.mIsChangedConsent = Boolean.valueOf(bundle.getBoolean(McsConstants.Auth.IS_CHANGED_CONSENT));
            this.mConsentContext = bundle.getString(McsConstants.Auth.CONSENT_CONTEXT);
            this.mIsChangedAlias = Boolean.valueOf(bundle.getBoolean(McsConstants.Auth.IS_CHANGED_ALIAS));
        }
        this.mHttpInterface = this;
        this.mPhoneId = messageStoreClient.getClientID();
        setMethod(HttpRequestParams.Method.PUT);
        setPostBody(makePostData());
        updateUrl();
        setCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int i;
                String dataString = httpResponseParams.getDataString();
                int statusCode = httpResponseParams.getStatusCode();
                Bundle bundle = new Bundle();
                bundle.putString("alias", RequestUpdateAccount.this.mUserAlias);
                bundle.putString(McsConstants.Auth.CONSENT_CONTEXT, RequestUpdateAccount.this.mConsentContext);
                String r4 = RequestUpdateAccount.this.TAG;
                int r7 = RequestUpdateAccount.this.mPhoneId;
                EventLogHelper.infoLogAndAdd(r4, r7, "resultCode: " + statusCode);
                String r42 = RequestUpdateAccount.this.TAG;
                int r72 = RequestUpdateAccount.this.mPhoneId;
                IMSLog.i(r42, r72, "strBody: " + IMSLog.numberChecker(dataString));
                if (statusCode == 200 && !dataString.isEmpty()) {
                    try {
                        String str = "";
                        String decrypt = RequestUpdateAccount.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted() ? RequestUpdateAccount.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, true) : str;
                        JSONObject jSONObject = (TextUtils.isEmpty(decrypt) ? new JSONObject(dataString) : new JSONObject(decrypt)).getJSONObject("account");
                        String string = jSONObject.getString("account_id");
                        String r73 = RequestUpdateAccount.this.TAG;
                        int r8 = RequestUpdateAccount.this.mPhoneId;
                        IMSLog.d(r73, r8, "accountId: " + string);
                        RequestUpdateAccount.this.mStoreClient.getPrerenceManager().saveMcsAccountId(string);
                        if (jSONObject.has("alias")) {
                            str = jSONObject.getString("alias");
                            String r1 = RequestUpdateAccount.this.TAG;
                            int r74 = RequestUpdateAccount.this.mPhoneId;
                            IMSLog.d(r1, r74, "alias: " + str);
                            RequestUpdateAccount.this.mStoreClient.getPrerenceManager().saveMcsAlias(str);
                        }
                        String string2 = jSONObject.getString(McsConstants.Auth.CONSENT_CONTEXT);
                        String r75 = RequestUpdateAccount.this.TAG;
                        int r82 = RequestUpdateAccount.this.mPhoneId;
                        IMSLog.d(r75, r82, "consentContext: " + string2);
                        if (jSONObject.has(McsConstants.Auth.MCS_ACCOUNT_STATUS)) {
                            i = jSONObject.getInt(McsConstants.Auth.MCS_ACCOUNT_STATUS);
                            String r76 = RequestUpdateAccount.this.TAG;
                            int r83 = RequestUpdateAccount.this.mPhoneId;
                            IMSLog.i(r76, r83, "accountStatus: " + i);
                        } else {
                            i = 0;
                        }
                        bundle.putString("alias", str);
                        bundle.putString(McsConstants.Auth.CONSENT_CONTEXT, string2);
                        bundle.putInt(McsConstants.Auth.MCS_ACCOUNT_STATUS, i);
                        RequestUpdateAccount.this.goSuccessfulCall(bundle);
                    } catch (JSONException e) {
                        IMSLog.e(RequestUpdateAccount.this.TAG, RequestUpdateAccount.this.mPhoneId, e.getMessage());
                        RequestUpdateAccount.this.goFailedCall(statusCode);
                    }
                } else if (RequestUpdateAccount.this.isErrorCodeSupported(statusCode)) {
                    int checkRetryAfter = RequestUpdateAccount.this.checkRetryAfter(httpResponseParams, RequestUpdateAccount.this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(RequestUpdateAccount.this.mHttpInterface.getClass().getSimpleName()));
                    if (checkRetryAfter > 0) {
                        bundle.putBoolean(McsConstants.Auth.IS_CHANGED_ALIAS, RequestUpdateAccount.this.mIsChangedAlias.booleanValue());
                        bundle.putBoolean(McsConstants.Auth.IS_CHANGED_CONSENT, RequestUpdateAccount.this.mIsChangedConsent.booleanValue());
                        iAPICallFlowListener.onOverRequest(RequestUpdateAccount.this.mHttpInterface, String.valueOf(statusCode), checkRetryAfter, bundle);
                    }
                } else {
                    RequestUpdateAccount.this.goFailedCall(statusCode);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = RequestUpdateAccount.this.TAG;
                int r1 = RequestUpdateAccount.this.mPhoneId;
                IMSLog.e(r0, r1, "Http request onFail: " + iOException.getMessage());
                RequestUpdateAccount.this.goFailedCall(802);
            }
        });
    }

    private void updateUrl() {
        setUrl(this.mStoreClient.getPrerenceManager().getOasisServerRoot() + "/oapi/v1/" + this.mMsisdn + "/account");
    }

    private JSONObject makePostData() {
        try {
            JSONObject jSONObject = new JSONObject();
            if (this.mIsChangedAlias.booleanValue()) {
                jSONObject.put("alias", this.mUserAlias);
            }
            if (this.mIsChangedConsent.booleanValue() && !TextUtils.isEmpty(this.mConsentContext)) {
                jSONObject.put(McsConstants.Auth.CONSENT_CONTEXT, this.mConsentContext);
            }
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
