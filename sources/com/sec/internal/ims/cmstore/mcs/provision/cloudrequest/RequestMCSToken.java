package com.sec.internal.ims.cmstore.mcs.provision.cloudrequest;

import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestMCSToken extends BaseProvisionRequest {
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public String TAG = RequestMCSToken.class.getSimpleName();
    private final JSONObject mDeviceInfo;
    private final boolean mIsValidRefreshToken;
    /* access modifiers changed from: private */
    public final int mPhoneId;
    /* access modifiers changed from: private */
    public transient WorkflowMcs mWorkFlow;

    public RequestMCSToken(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, boolean z, JSONObject jSONObject) {
        super(iAPICallFlowListener, messageStoreClient);
        this.mIsValidRefreshToken = z;
        this.mDeviceInfo = jSONObject;
        this.mHttpInterface = this;
        this.mPhoneId = messageStoreClient.getClientID();
        setMethod(HttpRequestParams.Method.POST);
        if (isUpdateUrl()) {
            setCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBasic());
            setPostBody(makePostData());
            this.mWorkFlow = this.mStoreClient.getProvisionWorkFlow();
            setCallback(new HttpRequestParams.HttpRequestCallback() {
                public void onComplete(HttpResponseParams httpResponseParams) {
                    String dataString = httpResponseParams.getDataString();
                    int statusCode = httpResponseParams.getStatusCode();
                    String r11 = RequestMCSToken.this.TAG;
                    int r12 = RequestMCSToken.this.mPhoneId;
                    EventLogHelper.infoLogAndAdd(r11, r12, "resultCode: " + statusCode);
                    String r112 = RequestMCSToken.this.TAG;
                    int r122 = RequestMCSToken.this.mPhoneId;
                    IMSLog.i(r112, r122, "strBody: " + IMSLog.numberChecker(dataString));
                    if (statusCode == 200) {
                        if (!dataString.isEmpty()) {
                            String str = "";
                            try {
                                if (RequestMCSToken.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
                                    str = RequestMCSToken.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, false);
                                }
                                JSONObject jSONObject = TextUtils.isEmpty(str) ? new JSONObject(dataString) : new JSONObject(str);
                                String string = jSONObject.getString("access_token");
                                RequestMCSToken.this.mStoreClient.getPrerenceManager().saveMcsAccessToken(string);
                                long integerPayloadFromToken = Util.getIntegerPayloadFromToken(string, "validity");
                                String str2 = "oasis_server_root";
                                String str3 = "oasis_large_file_server_root";
                                RequestMCSToken.this.mStoreClient.getPrerenceManager().saveMcsAccessTokenExpireTime(Util.getIntegerPayloadFromToken(string, CloudMessageProviderContract.BufferDBMMSpdu.EXP));
                                RequestMCSToken.this.mWorkFlow.setAccessTokenValidityTimer(integerPayloadFromToken);
                                String string2 = jSONObject.getString("refresh_token");
                                RequestMCSToken.this.mStoreClient.getPrerenceManager().saveMcsRefreshToken(string2);
                                long integerPayloadFromToken2 = Util.getIntegerPayloadFromToken(string2, CloudMessageProviderContract.BufferDBMMSpdu.EXP);
                                long integerPayloadFromToken3 = Util.getIntegerPayloadFromToken(string2, "validity");
                                RequestMCSToken.this.mStoreClient.getPrerenceManager().saveMcsRefreshTokenExpireTime(integerPayloadFromToken2);
                                RequestMCSToken.this.mWorkFlow.setRefreshTokenValidityTimer(integerPayloadFromToken3);
                                JSONObject jSONObject2 = jSONObject.getJSONObject(McsConstants.Auth.OASIS_CONFIG);
                                if (jSONObject2.has("fcm_sender_id")) {
                                    RequestMCSToken.this.mStoreClient.getPrerenceManager().saveFcmSenderId(jSONObject2.getString("fcm_sender_id"));
                                }
                                RequestMCSToken.this.mStoreClient.getPrerenceManager().saveCmsDataTtl(RequestMCSToken.this.getInt(jSONObject2.getString("cms_data_ttl")));
                                RequestMCSToken.this.mStoreClient.getPrerenceManager().saveMaxUploadFileSize(RequestMCSToken.this.getInt(jSONObject2.getString("max_upload_file_size")));
                                if (jSONObject2.has("max_small_file_size")) {
                                    RequestMCSToken.this.mStoreClient.getPrerenceManager().saveMaxSmallFileSize(RequestMCSToken.this.getInt(jSONObject2.getString("max_small_file_size")));
                                }
                                if (jSONObject2.has("oasis_small_file_server_root")) {
                                    RequestMCSToken.this.mStoreClient.getPrerenceManager().saveOasisSmallFileServerRoot(jSONObject2.getString("oasis_small_file_server_root"));
                                }
                                String str4 = str3;
                                if (jSONObject2.has(str4)) {
                                    RequestMCSToken.this.mStoreClient.getPrerenceManager().saveOasisLargeFileServerRoot(jSONObject2.getString(str4));
                                }
                                if (jSONObject2.has(str2)) {
                                    String string3 = jSONObject2.getString(str2);
                                    String r4 = RequestMCSToken.this.TAG;
                                    IMSLog.d(r4, "oasis server root: " + string3);
                                    RequestMCSToken.this.mStoreClient.getPrerenceManager().saveOasisServerRoot(string3);
                                }
                                if (jSONObject2.has(McsConstants.Auth.OASIS_SERVER_VERSION)) {
                                    RequestMCSToken.this.mStoreClient.getPrerenceManager().saveOasisServerVersion(jSONObject2.getString(McsConstants.Auth.OASIS_SERVER_VERSION));
                                }
                                JSONArray jSONArray = jSONObject.getJSONObject(McsConstants.Auth.AUTO_CONFIG).getJSONArray(McsConstants.Auth.APPLICATIONS);
                                int i = 0;
                                while (true) {
                                    if (i >= jSONArray.length()) {
                                        break;
                                    }
                                    JSONObject jSONObject3 = jSONArray.getJSONObject(i);
                                    if (TextUtils.equals(jSONObject3.getJSONObject(McsConstants.Auth.TARGET_INFO).getString("type"), McsConstants.Auth.PRIMARY)) {
                                        JSONObject jSONObject4 = jSONObject3.getJSONObject(McsConstants.Auth.XMS_MESSAGE);
                                        RequestMCSToken.this.mStoreClient.getPrerenceManager().saveMmsRevokeTtlSecs(RequestMCSToken.this.getInt(jSONObject4.getString("mms_revoke_ttl_secs")));
                                        RequestMCSToken.this.mStoreClient.getPrerenceManager().saveSmsRevokeTtlSecs(RequestMCSToken.this.getInt(jSONObject4.getString("sms_revoke_ttl_secs")));
                                        break;
                                    }
                                    i++;
                                }
                                RequestMCSToken.this.goSuccessfulCall((Object) null);
                            } catch (JSONException e) {
                                IMSLog.e(RequestMCSToken.this.TAG, RequestMCSToken.this.mPhoneId, e.getMessage());
                                RequestMCSToken.this.goFailedCall(statusCode);
                            }
                        }
                    } else if (!RequestMCSToken.this.isErrorCodeSupported(statusCode) || RequestMCSToken.this.mStoreClient.getPrerenceManager().getMcsUser() != 1) {
                        RequestMCSToken.this.goFailedCall(statusCode);
                    } else {
                        int checkRetryAfter = RequestMCSToken.this.checkRetryAfter(httpResponseParams, RequestMCSToken.this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(RequestMCSToken.this.mHttpInterface.getClass().getSimpleName()));
                        if (checkRetryAfter > 0) {
                            iAPICallFlowListener.onOverRequest(RequestMCSToken.this.mHttpInterface, String.valueOf(statusCode), checkRetryAfter);
                        }
                    }
                }

                public void onFail(IOException iOException) {
                    String r0 = RequestMCSToken.this.TAG;
                    int r1 = RequestMCSToken.this.mPhoneId;
                    IMSLog.e(r0, r1, "Http request onFail: " + iOException.getMessage());
                    RequestMCSToken.this.goFailedCall(802);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public int getInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private boolean isUpdateUrl() {
        if (this.mStoreClient.getPrerenceManager().getOasisServerRoot().isEmpty()) {
            IMSLog.i(this.TAG, this.mPhoneId, "updateUrl: Oasis Server Root is empty");
            goFailedCall(0);
            return false;
        }
        setUrl(this.mStoreClient.getPrerenceManager().getOasisServerRoot() + "/oapi/v1/token");
        return true;
    }

    private JSONObject makePostData() {
        try {
            JSONObject jSONObject = new JSONObject();
            if (this.mIsValidRefreshToken) {
                jSONObject.put("refresh_token", this.mStoreClient.getPrerenceManager().getMcsRefreshToken());
                jSONObject.put(McsConstants.Auth.GRANT_TYPE, "refresh_token");
                jSONObject.put("device_info", this.mDeviceInfo);
            } else {
                jSONObject.put("code", this.mStoreClient.getPrerenceManager().getAuthCode());
                jSONObject.put(McsConstants.Auth.GRANT_TYPE, McsConstants.Auth.AUTHORIZATION_CODE);
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
