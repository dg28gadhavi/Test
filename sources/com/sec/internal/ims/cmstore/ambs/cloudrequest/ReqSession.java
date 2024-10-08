package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.CommonErrorName;
import com.sec.internal.constants.ims.cmstore.ReqConstant;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.CloudMessageIntent;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils;
import com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.servicemodules.gls.GlsIntent;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReqSession extends BaseProvisionAPIRequest {
    private static final long serialVersionUID = 890056112766767377L;
    /* access modifiers changed from: private */
    public String TAG = ReqSession.class.getSimpleName();

    public ReqSession(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, IRetryStackAdapterHelper iRetryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iAPICallFlowListener, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        setMethod(HttpRequestParams.Method.POST);
        setPostParams(makePostData());
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int access$1500;
                int i;
                int i2;
                if (httpResponseParams.getStatusCode() == 302) {
                    List list = httpResponseParams.getHeaders().get(GlsIntent.Extras.EXTRA_LOCATION);
                    String str = (list == null || list.size() <= 0) ? null : (String) list.get(0);
                    Log.d(ReqSession.this.TAG, "location: " + str);
                    if (TextUtils.isEmpty(str)) {
                        ReqSession.this.goFailedCall(CommonErrorName.DEFAULT_ERROR_TYPE);
                        return;
                    }
                    if (str != null) {
                        i2 = str.indexOf(ATTGlobalVariables.ACMS_TARGET_URL);
                        i = str.indexOf("errorCode");
                    } else {
                        i2 = -1;
                        i = -1;
                    }
                    if (i >= 0) {
                        String findErrorCode = AmbsUtils.findErrorCode(str, "errorCode=", '&');
                        Log.d(ReqSession.this.TAG, "errorCode: " + findErrorCode);
                        ReqSession.this.goFailedCall(findErrorCode);
                        return;
                    } else if (i2 >= 0) {
                        IHttpAPICommonInterface lastFailedRequest = ReqSession.this.mStoreClient.getRetryStackAdapter().getLastFailedRequest();
                        if (lastFailedRequest != null) {
                            String simpleName = lastFailedRequest.getClass().getSimpleName();
                            Log.d(ReqSession.this.TAG, "lastFailedApiName: " + simpleName + "SteadyState: " + ReqSession.this.mStoreClient.getPrerenceManager().ifSteadyState());
                            if (ReqSession.this.mStoreClient.getPrerenceManager().ifSteadyState()) {
                                if (simpleName.equals(RequestCreateAccount.class.getSimpleName())) {
                                    ReqSession.this.goSuccessfulCall(ReqConstant.HAPPY_PATH_CREATE_ACCOUNT);
                                    return;
                                } else if (simpleName.equals(RequestAccount.class.getSimpleName())) {
                                    ReqSession.this.goSuccessfulCall(ReqConstant.HAPPY_PATH_GET_SVC_ACCOUNT);
                                    return;
                                } else if (simpleName.equals(RequestDeleteAccount.class.getSimpleName())) {
                                    ReqSession.this.goSuccessfulCall(ReqConstant.HAPPY_PATH_DELETE_ACCOUNT);
                                    return;
                                } else if (simpleName.equals(RequestTC.class.getSimpleName())) {
                                    ReqSession.this.goSuccessfulCall(ReqConstant.HAPPY_PATH_GET_TC);
                                    return;
                                } else {
                                    ReqSession.this.goSuccessfulCall(ReqConstant.HAPPY_PATH_STEADY_STATE_REQ_HUIMSTOKEN);
                                    return;
                                }
                            } else if (simpleName.equals(RequestDeleteAccount.class.getSimpleName())) {
                                ReqSession.this.goSuccessfulCall(ReqConstant.HAPPY_PATH_DELETE_ACCOUNT);
                                return;
                            } else {
                                ReqSession.this.goSuccessfulCall();
                                return;
                            }
                        } else if (ReqSession.this.mStoreClient.getPrerenceManager().ifSteadyState()) {
                            ReqSession.this.goSuccessfulCall(ReqConstant.HAPPY_PATH_STEADY_STATE_REQ_HUIMSTOKEN);
                            return;
                        } else {
                            Log.d(ReqSession.this.TAG, "not steady state");
                            ReqSession.this.goSuccessfulCall();
                            return;
                        }
                    }
                } else if ((httpResponseParams.getStatusCode() == 503 || httpResponseParams.getStatusCode() == 429) && (access$1500 = ReqSession.this.checkRetryAfter(httpResponseParams)) > 0) {
                    iAPICallFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, access$1500);
                    return;
                }
                Log.d(ReqSession.this.TAG, "all other responses");
                ReqSession.this.goFailedCall(CommonErrorName.DEFAULT_ERROR_TYPE);
            }

            public void onFail(IOException iOException) {
                String r0 = ReqSession.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                ReqSession.this.goFailedCall();
            }
        });
    }

    private Map<String, String> makePostData() {
        HashMap hashMap = new HashMap();
        hashMap.put("TG_OP", SoftphoneNamespaces.SoftphoneSettings.TGUARD_MSIP_OPERATION);
        hashMap.put("appID", ATTGlobalVariables.APP_ID);
        String atsToken = this.mStoreClient.getPrerenceManager().getAtsToken();
        if (TextUtils.isEmpty(atsToken)) {
            atsToken = "null";
        }
        hashMap.put("atsToken", atsToken);
        String userCtn = this.mStoreClient.getPrerenceManager().getUserCtn();
        String str = this.TAG;
        Log.d(str, "ctnID: " + IMSLog.checker(userCtn));
        hashMap.put("ctnID", userCtn);
        hashMap.put(CloudMessageIntent.ExtrasAMBSUI.STYLE, ATTGlobalVariables.URL_PARAM_STYLE);
        hashMap.put("targetURL", ATTGlobalVariables.ACMS_TARGET_URL);
        hashMap.put("returnErrorCode", CloudMessageProviderContract.JsonData.TRUE);
        return hashMap;
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.ACMS_HOST_NAME + SoftphoneNamespaces.SoftphoneSettings.MSIP_TOKEN_PATH);
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return new ReqSession(iAPICallFlowListener, messageStoreClient, iRetryStackAdapterHelper, iCloudMessageManagerHelper);
    }
}
