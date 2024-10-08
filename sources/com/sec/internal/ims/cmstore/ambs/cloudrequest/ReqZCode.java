package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils;
import com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReqZCode extends BaseProvisionAPIRequest {
    public static final String REQUET_ID_PRIX = "AMBS";
    public static final String ZCODE_SEND_FROM = "74611666";
    public static final String ZCODE_SEND_FROM_FFA = "74611888";
    public static final String ZCODE_SMS_BEGIN = "AT&T FREE MESSAGE - This is an automated message, please ignore.";
    private static volatile String lastRequestKey = null;
    private static final int mMaxRequestIdDigitLength = 4;
    private static final long serialVersionUID = -6914421196386591646L;
    /* access modifiers changed from: private */
    public String TAG = ReqZCode.class.getSimpleName();

    public ReqZCode(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iAPICallFlowListener, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        setMethod(HttpRequestParams.Method.POST);
        setPostParams(makePostData());
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String dataString = httpResponseParams.getDataString();
                String r1 = ReqZCode.this.TAG;
                Log.d(r1, "onComplete StatusCode: " + httpResponseParams.getStatusCode() + " strbody: " + IMSLog.checker(dataString));
                if (httpResponseParams.getStatusCode() == 200) {
                    if (!TextUtils.isEmpty(dataString)) {
                        ReqZCode.this.goFailedCall(AmbsUtils.findErrorCode(dataString, "errorCode=", '&'));
                    }
                } else if (httpResponseParams.getStatusCode() == 503 || httpResponseParams.getStatusCode() == 429) {
                    int access$100 = ReqZCode.this.checkRetryAfter(httpResponseParams);
                    if (access$100 > 0) {
                        iAPICallFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, access$100);
                    }
                } else {
                    ReqZCode.this.goFailedCall();
                }
            }

            public void onFail(IOException iOException) {
                String r0 = ReqZCode.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                ReqZCode.this.goFailedCall();
            }
        });
    }

    private String makeRequestId(int i) {
        return REQUET_ID_PRIX + AmbsUtils.generateRandomString(i - 4, true);
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.ACMS_HOST_NAME + "/commonLogin/nxsATS/AuthZCode");
    }

    private Map<String, String> makePostData() {
        lastRequestKey = makeRequestId(4);
        String str = this.TAG;
        Log.i(str, "ReqZCode() " + lastRequestKey);
        this.mStoreClient.getPrerenceManager().saveZCodeLastRequestId(lastRequestKey);
        HashMap hashMap = new HashMap();
        hashMap.put("TG_OP", "AuthZCode");
        hashMap.put("appID", ATTGlobalVariables.APP_ID);
        hashMap.put("ctnID", this.mStoreClient.getPrerenceManager().getUserCtn());
        hashMap.put("requestID", lastRequestKey);
        return hashMap;
    }

    public static boolean isSmsZCode(String str, String str2) {
        if (!str.startsWith(ZCODE_SMS_BEGIN) || !str.contains(REQUET_ID_PRIX)) {
            return false;
        }
        if (!str2.equals(ZCODE_SEND_FROM) && !str2.equals(ZCODE_SEND_FROM_FFA)) {
            return false;
        }
        return true;
    }

    public static void handleSmsZCode(String str, IAPICallFlowListener iAPICallFlowListener, IRetryStackAdapterHelper iRetryStackAdapterHelper, MessageStoreClient messageStoreClient) {
        String str2;
        if (TextUtils.isEmpty(lastRequestKey)) {
            lastRequestKey = messageStoreClient.getPrerenceManager().getZCodeLastRequestId(lastRequestKey);
            String simpleName = ReqZCode.class.getSimpleName();
            Log.v(simpleName, "read last requestId from preference" + lastRequestKey);
        }
        String simpleName2 = ReqZCode.class.getSimpleName();
        Log.v(simpleName2, "handleSmsZCode() lastReqKey:" + lastRequestKey + " zcodeBody: " + IMSLog.checker(str));
        if (!TextUtils.isEmpty(lastRequestKey) && str.contains(lastRequestKey) && str.length() >= str.indexOf(lastRequestKey) + lastRequestKey.length() + 1) {
            messageStoreClient.getPrerenceManager().saveAuthZCode(str.substring(str.indexOf(lastRequestKey) + lastRequestKey.length() + 1));
            messageStoreClient.getPrerenceManager().removeUserInputNumberCount();
            IHttpAPICommonInterface lastFailedRequest = messageStoreClient.getRetryStackAdapter().getLastFailedRequest();
            if (lastFailedRequest != null && lastFailedRequest.getClass().getSimpleName().equals(ReqZCode.class.getSimpleName())) {
                IHttpAPICommonInterface pop = messageStoreClient.getRetryStackAdapter().pop();
                if (pop == null) {
                    str2 = null;
                } else {
                    str2 = pop.getClass().getSimpleName();
                }
                String simpleName3 = ReqZCode.class.getSimpleName();
                Log.d(simpleName3, "API " + str2 + " Pop from Retry Stack");
            }
            iAPICallFlowListener.onFixedFlow(EnumProvision.ProvisionEventType.REQ_ATS_TOKEN.getId());
        }
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return new ReqZCode(iAPICallFlowListener, messageStoreClient, iCloudMessageManagerHelper);
    }
}
