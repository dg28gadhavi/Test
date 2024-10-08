package com.sec.internal.ims.cmstore.mcs.provision.cloudrequest;

import android.os.Bundle;
import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
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

public class RequestOtpSms extends BaseProvisionRequest {
    /* access modifiers changed from: private */
    public String TAG = RequestOtpSms.class.getSimpleName();
    private final JSONObject mDeviceInfo;
    private final String mMsisdn;
    /* access modifiers changed from: private */
    public final int mPhoneId;

    public RequestOtpSms(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, String str, JSONObject jSONObject) {
        super(iAPICallFlowListener, messageStoreClient);
        this.mMsisdn = str;
        this.mDeviceInfo = jSONObject;
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
                String r2 = RequestOtpSms.this.TAG;
                int r3 = RequestOtpSms.this.mPhoneId;
                EventLogHelper.infoLogAndAdd(r2, r3, "resultCode: " + statusCode);
                if (statusCode == 200) {
                    long j = 60;
                    if (!dataString.isEmpty()) {
                        String str = "";
                        try {
                            if (RequestOtpSms.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
                                str = RequestOtpSms.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, false);
                            }
                            long j2 = (TextUtils.isEmpty(str) ? new JSONObject(dataString) : new JSONObject(str)).getLong(CloudMessageProviderContract.BufferDBMMSpdu.EXP) - (System.currentTimeMillis() / 1000);
                            String r10 = RequestOtpSms.this.TAG;
                            int r0 = RequestOtpSms.this.mPhoneId;
                            IMSLog.i(r10, r0, "otpCodeValidity: " + j2);
                            j = j2;
                        } catch (JSONException e) {
                            IMSLog.e(RequestOtpSms.this.TAG, RequestOtpSms.this.mPhoneId, e.getMessage());
                        }
                    }
                    Bundle bundle = new Bundle();
                    bundle.putLong("otpCodeValidity", j);
                    RequestOtpSms.this.goSuccessfulCall(bundle);
                } else if (statusCode != 400) {
                    if (!RequestOtpSms.this.isErrorCodeSupported(statusCode) || RequestOtpSms.this.mStoreClient.getPrerenceManager().getMcsUser() != 1) {
                        RequestOtpSms.this.goFailedCall(statusCode);
                        return;
                    }
                    int checkRetryAfter = RequestOtpSms.this.checkRetryAfter(httpResponseParams, RequestOtpSms.this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(RequestOtpSms.this.mHttpInterface.getClass().getSimpleName()));
                    if (checkRetryAfter > 0) {
                        iAPICallFlowListener.onOverRequest(RequestOtpSms.this.mHttpInterface, String.valueOf(statusCode), checkRetryAfter);
                    }
                }
            }

            public void onFail(IOException iOException) {
                String r0 = RequestOtpSms.this.TAG;
                int r1 = RequestOtpSms.this.mPhoneId;
                IMSLog.e(r0, r1, "Http request onFail: " + iOException.getMessage());
                RequestOtpSms.this.goFailedCall(802);
            }
        });
    }

    private void updateUrl() {
        setUrl(this.mStoreClient.getPrerenceManager().getOasisAuthRoot() + "/oapi/v1/auth/method/sms");
    }

    private String makePostData() {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put(McsConstants.Auth.MDN, this.mMsisdn);
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
