package com.sec.internal.ims.cmstore.mcs.provision.cloudrequest;

import android.os.Bundle;
import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestUserAuthentication extends BaseProvisionRequest {
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public String TAG = RequestUserAuthentication.class.getSimpleName();
    private final JSONObject mDeviceInfo;
    private final String mMobileIp;
    private final String mMsisdn;
    private final String mOtpCode;
    /* access modifiers changed from: private */
    public final int mPhoneId;
    private String mRequestType;
    /* access modifiers changed from: private */
    public transient WorkflowMcs mWorkFlow;

    public RequestUserAuthentication(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, String str, JSONObject jSONObject, String str2, String str3, String str4, final Boolean bool, final String str5) {
        super(iAPICallFlowListener, messageStoreClient);
        this.mMsisdn = str;
        this.mDeviceInfo = jSONObject;
        this.mOtpCode = str2;
        this.mRequestType = str3;
        this.mMobileIp = str4;
        this.mWorkFlow = this.mStoreClient.getProvisionWorkFlow();
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
                Bundle bundle = new Bundle();
                bundle.putString(McsConstants.Auth.CONSENT_CONTEXT, str5);
                String r4 = RequestUserAuthentication.this.TAG;
                int r5 = RequestUserAuthentication.this.mPhoneId;
                EventLogHelper.infoLogAndAdd(r4, r5, "resultCode: " + statusCode);
                String r42 = RequestUserAuthentication.this.TAG;
                int r52 = RequestUserAuthentication.this.mPhoneId;
                IMSLog.i(r42, r52, "strBody: " + IMSLog.numberChecker(dataString));
                String str = "";
                if (statusCode == 200) {
                    if (!dataString.isEmpty()) {
                        try {
                            if (RequestUserAuthentication.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
                                str = RequestUserAuthentication.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, false);
                            }
                            JSONObject jSONObject = TextUtils.isEmpty(str) ? new JSONObject(dataString) : new JSONObject(str);
                            String string = jSONObject.getString(McsConstants.Auth.AUTHENTICATION_CODE);
                            String r3 = RequestUserAuthentication.this.TAG;
                            int r43 = RequestUserAuthentication.this.mPhoneId;
                            IMSLog.d(r3, r43, "auth_code: " + string);
                            RequestUserAuthentication.this.mStoreClient.getPrerenceManager().saveAuthCode(string);
                            if (jSONObject.has("oasis_server_root")) {
                                String string2 = jSONObject.getString("oasis_server_root");
                                String r0 = RequestUserAuthentication.this.TAG;
                                int r1 = RequestUserAuthentication.this.mPhoneId;
                                IMSLog.d(r0, r1, "oasis server root: " + string2);
                                RequestUserAuthentication.this.mStoreClient.getPrerenceManager().saveOasisServerRoot(string2);
                            }
                            RequestUserAuthentication.this.goSuccessfulCall((Object) null);
                        } catch (JSONException e) {
                            IMSLog.e(RequestUserAuthentication.this.TAG, RequestUserAuthentication.this.mPhoneId, e.getMessage());
                            RequestUserAuthentication.this.goFailedCall(statusCode);
                        }
                    }
                } else if (statusCode == 202) {
                    if (!dataString.isEmpty()) {
                        try {
                            if (RequestUserAuthentication.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
                                str = RequestUserAuthentication.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, false);
                            }
                            String string3 = (TextUtils.isEmpty(str) ? new JSONObject(dataString) : new JSONObject(str)).getString("registration_code");
                            String r02 = RequestUserAuthentication.this.TAG;
                            int r12 = RequestUserAuthentication.this.mPhoneId;
                            IMSLog.d(r02, r12, "registration code: " + string3);
                            if (TextUtils.isEmpty(string3)) {
                                return;
                            }
                            if (RequestUserAuthentication.this.mStoreClient.getPrerenceManager().getMcsUser() == 1 && !bool.booleanValue()) {
                                RequestUserAuthentication.this.goFailedCall(900);
                            } else if (str5 != null) {
                                long integerPayloadFromToken = Util.getIntegerPayloadFromToken(string3, "validity");
                                RequestUserAuthentication.this.mStoreClient.getPrerenceManager().saveRegCode(string3);
                                RequestUserAuthentication.this.mWorkFlow.setRegistrationCodeValidityTimer(integerPayloadFromToken);
                                RequestUserAuthentication.this.goSuccessfulCall(bundle);
                            }
                        } catch (JSONException e2) {
                            IMSLog.e(RequestUserAuthentication.this.TAG, RequestUserAuthentication.this.mPhoneId, e2.getMessage());
                            RequestUserAuthentication.this.goFailedCall(statusCode);
                        }
                    } else {
                        RequestUserAuthentication.this.goFailedCall(statusCode);
                    }
                } else if (!RequestUserAuthentication.this.isErrorCodeSupported(statusCode) || RequestUserAuthentication.this.mStoreClient.getPrerenceManager().getMcsUser() != 1) {
                    RequestUserAuthentication.this.goFailedCall(statusCode);
                } else {
                    int checkRetryAfter = RequestUserAuthentication.this.checkRetryAfter(httpResponseParams, RequestUserAuthentication.this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(RequestUserAuthentication.this.mHttpInterface.getClass().getSimpleName()));
                    if (checkRetryAfter > 0) {
                        iAPICallFlowListener.onOverRequest(RequestUserAuthentication.this.mHttpInterface, String.valueOf(statusCode), checkRetryAfter, bundle);
                    }
                }
            }

            public void onFail(IOException iOException) {
                String r0 = RequestUserAuthentication.this.TAG;
                int r1 = RequestUserAuthentication.this.mPhoneId;
                IMSLog.e(r0, r1, "Http request onFail: " + iOException.getMessage());
                RequestUserAuthentication.this.goFailedCall(802);
            }
        });
    }

    private void updateUrl() {
        setUrl(this.mStoreClient.getPrerenceManager().getOasisAuthRoot() + "/oapi/v1/auth/user");
    }

    private String makePostData() {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("type", this.mRequestType);
            jSONObject.put("id", this.mMsisdn);
            if (this.mRequestType.equals(McsConstants.Auth.TYPE_OTP)) {
                jSONObject.put("password", this.mOtpCode);
            } else if (this.mRequestType.equals(McsConstants.Auth.TYPE_MOBILE_IP)) {
                jSONObject.put(McsConstants.Auth.TYPE_MOBILE_IP, this.mMobileIp);
            }
            jSONObject.put("device_info", this.mDeviceInfo);
            String replaceAll = jSONObject.toString().replaceAll("\\\\", "");
            String str = this.TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "json string: " + IMSLog.numberChecker(replaceAll));
            return replaceAll;
        } catch (JSONException e) {
            IMSLog.e(this.TAG, this.mPhoneId, e.getMessage());
            return "";
        }
    }
}
