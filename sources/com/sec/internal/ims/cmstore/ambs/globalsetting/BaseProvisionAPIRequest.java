package com.sec.internal.ims.cmstore.ambs.globalsetting;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.PersistentHttp3CookieJar;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class BaseProvisionAPIRequest extends HttpRequestParams implements IHttpAPICommonInterface {
    private static final long serialVersionUID = -3500664057158035738L;
    public String TAG = BaseProvisionAPIRequest.class.getSimpleName();
    protected final transient PersistentHttp3CookieJar mCookieJar;
    protected transient IAPICallFlowListener mFlowListener;
    /* access modifiers changed from: protected */
    public transient MessageStoreClient mStoreClient;

    public HttpRequestParams getRetryInstance(SyncMsgType syncMsgType, IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return this;
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return this;
    }

    public BaseProvisionAPIRequest(Map<String, String> map, IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        super(map);
        this.mFlowListener = iAPICallFlowListener;
        this.mStoreClient = messageStoreClient;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mCookieJar = messageStoreClient.getHttpController().getCookieJar();
        setPhoneId(messageStoreClient.getClientID());
    }

    public BaseProvisionAPIRequest(String str, IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        super(prepareDefaultHeader(str, messageStoreClient));
        this.mFlowListener = iAPICallFlowListener;
        this.mStoreClient = messageStoreClient;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mCookieJar = messageStoreClient.getHttpController().getCookieJar();
        setFollowRedirects(false);
        setPhoneId(messageStoreClient.getClientID());
    }

    public BaseProvisionAPIRequest(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        super(prepareDefaultHeader(messageStoreClient));
        this.mFlowListener = iAPICallFlowListener;
        this.mStoreClient = messageStoreClient;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mCookieJar = messageStoreClient.getHttpController().getCookieJar();
        setFollowRedirects(false);
        setPhoneId(messageStoreClient.getClientID());
    }

    private static Map<String, String> prepareDefaultHeader(MessageStoreClient messageStoreClient) {
        HashMap hashMap = new HashMap();
        hashMap.put("Content-Type", "application/x-www-form-urlencoded");
        return processDefaultHeader(hashMap, messageStoreClient);
    }

    private static Map<String, String> prepareDefaultHeader(String str, MessageStoreClient messageStoreClient) {
        HashMap hashMap = new HashMap();
        hashMap.put("Content-Type", str);
        return processDefaultHeader(hashMap, messageStoreClient);
    }

    private static Map<String, String> processDefaultHeader(Map<String, String> map, MessageStoreClient messageStoreClient) {
        map.put("Connection", "close");
        map.put("x-att-clientVersion", ATTGlobalVariables.VERSION_NAME);
        map.put("x-att-clientId", ATTGlobalVariables.getHttpClientID());
        map.put("x-att-contextInfo", ATTGlobalVariables.BUILD_INFO);
        map.put("x-att-deviceId", messageStoreClient.getPrerenceManager().getDeviceId());
        return map;
    }

    /* access modifiers changed from: protected */
    public void goSuccessfulCall(String str) {
        this.mFlowListener.onSuccessfulCall((IHttpAPICommonInterface) this, str);
    }

    /* access modifiers changed from: protected */
    public void goSuccessfulCall() {
        this.mFlowListener.onSuccessfulCall(this);
    }

    /* access modifiers changed from: protected */
    public void goFailedCall() {
        this.mFlowListener.onFailedCall(this);
    }

    /* access modifiers changed from: protected */
    public void goFailedCall(String str) {
        this.mFlowListener.onFailedCall((IHttpAPICommonInterface) this, str);
    }

    /* access modifiers changed from: protected */
    public boolean checkAndHandleCPSError(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        try {
            JSONObject optJSONObject = new JSONObject(str).optJSONObject("requestError");
            if (optJSONObject == null) {
                return false;
            }
            String string = optJSONObject.getJSONObject("serviceException").getString("messageId");
            if (ATTConstants.ATTErrorNames.CPS_TC_ERROR_1007.equals(string) || ATTConstants.ATTErrorNames.CPS_TC_ERROR_1008.equals(string) || ATTConstants.ATTErrorNames.CPS_PROVISION_SHUTDOWN.equals(string)) {
                String str2 = this.TAG;
                Log.d(str2, "CPS errors: " + string);
                goFailedCall(string);
                return true;
            }
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public int checkRetryAfter(HttpResponseParams httpResponseParams) {
        List list = httpResponseParams.getHeaders().get(HttpRequest.HEADER_RETRY_AFTER);
        if (list == null || list.size() <= 0) {
            return -1;
        }
        String str = (String) list.get(0);
        String str2 = this.TAG;
        Log.d(str2, "retryAfter is " + str + "seconds retryAfterHeader: " + list.toString());
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        this.mStoreClient = messageStoreClient;
        return this;
    }

    public void updateServerRoot(String str) {
        String str2 = this.TAG;
        Log.d(str2, "updateServerRoot" + str);
    }
}
