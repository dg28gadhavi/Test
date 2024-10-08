package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.omanetapi.nms.data.GsonInterfaceAdapter;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.strategy.TMOCmStrategy;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.util.HttpAuthGenerator;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nms.data.Attribute;
import com.sec.internal.omanetapi.nms.data.PathList;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseNMSRequest extends HttpRequestParams implements IHttpAPICommonInterface {
    private static final long serialVersionUID = 8115500554434359994L;
    private final String JSON_MESSAGE_ID_TAG = "messageId";
    private final String JSON_POLICY_EXCEPTION_TAG = "policyException";
    private final String JSON_REQUEST_ERROR_TAG = "requestError";
    private final String JSON_SERVICE_EXCEPTION_TAG = "serviceException";
    private String TAG = BaseNMSRequest.class.getSimpleName();
    /* access modifiers changed from: protected */
    public boolean isCmsMcsEnabled;
    protected String mAuthorization;
    /* access modifiers changed from: protected */
    public String mBaseUrl;
    private String mBoxId;
    protected String mChallenge;
    private final CookieJar mCookieJar;
    protected transient Map<String, String> mNMSRequestHeaderMap = new HashMap();
    protected int mPhoneId;
    /* access modifiers changed from: protected */
    public MessageStoreClient mStoreClient;

    /* access modifiers changed from: protected */
    public abstract void buildAPISpecificURLFromBase();

    public HttpRequestParams getRetryInstance(SyncMsgType syncMsgType, IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return this;
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return this;
    }

    public BaseNMSRequest(String str, String str2, String str3, String str4, MessageStoreClient messageStoreClient) {
        String str5 = this.TAG + "[" + messageStoreClient.getClientID() + "]";
        this.TAG = str5;
        Log.i(str5, "constructor1");
        this.mStoreClient = messageStoreClient;
        this.mCookieJar = messageStoreClient.getHttpController().getCookieJar();
        this.mPhoneId = messageStoreClient.getClientID();
        this.isCmsMcsEnabled = CmsUtil.isMcsSupported(this.mStoreClient.getContext(), this.mPhoneId);
        buildBaseURL(str, str2, str3, str4);
        setPhoneId(messageStoreClient.getClientID());
    }

    public BaseNMSRequest(String str, String str2, String str3, String str4, boolean z, MessageStoreClient messageStoreClient) {
        String str5 = this.TAG + "[" + messageStoreClient.getClientID() + "]";
        this.TAG = str5;
        Log.i(str5, "constructor3");
        this.mStoreClient = messageStoreClient;
        this.mCookieJar = messageStoreClient.getHttpController().getCookieJar();
        this.mPhoneId = messageStoreClient.getClientID();
        this.isCmsMcsEnabled = CmsUtil.isMcsSupported(this.mStoreClient.getContext(), this.mPhoneId);
        if (ATTGlobalVariables.isGcmReplacePolling()) {
            buildBaseURL(str, str2, str3, str4, z);
        } else {
            buildBaseURL(str, str2, str3, str4);
        }
        setPhoneId(messageStoreClient.getClientID());
    }

    public BaseNMSRequest(String str, String str2, String str3, String str4, String str5, String str6, MessageStoreClient messageStoreClient) {
        String str7 = this.TAG + "[" + messageStoreClient.getClientID() + "]";
        this.TAG = str7;
        Log.i(str7, "constructor2");
        this.mStoreClient = messageStoreClient;
        this.mCookieJar = messageStoreClient.getHttpController().getCookieJar();
        if (str2 != null) {
            str = str + ":" + str2;
        }
        if (str3 != null && !str3.trim().isEmpty()) {
            str = str + "/" + str3.trim();
        }
        setPhoneId(messageStoreClient.getClientID());
        buildBaseURL(str, str4, str5, str6);
    }

    public BaseNMSRequest(String str, MessageStoreClient messageStoreClient) {
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mStoreClient = messageStoreClient;
        this.mCookieJar = messageStoreClient.getHttpController().getCookieJar();
        this.mBaseUrl = str;
        this.mPhoneId = messageStoreClient.getClientID();
        this.isCmsMcsEnabled = CmsUtil.isMcsSupported(this.mStoreClient.getContext(), this.mPhoneId);
        setPhoneId(messageStoreClient.getClientID());
    }

    public BaseNMSRequest(String str, String str2, MessageStoreClient messageStoreClient) {
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mStoreClient = messageStoreClient;
        this.mCookieJar = messageStoreClient.getHttpController().getCookieJar();
        try {
            String encode = URLEncoder.encode(str2, "utf-8");
            this.mBaseUrl = str + encode;
            Log.d(this.TAG, "encoded telUri: " + IMSLog.checker(encode) + ", mBaseUrl: " + IMSLog.checker(this.mBaseUrl));
        } catch (UnsupportedEncodingException unused) {
            Log.d(this.TAG, "uri encode failed");
        }
        setPhoneId(messageStoreClient.getClientID());
    }

    private void buildBaseURL(String str, String str2, String str3, String str4) {
        this.mBoxId = str4;
        Uri.Builder builder = new Uri.Builder();
        String protocol = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getProtocol();
        if (CmsUtil.isMcsSupported(this.mStoreClient.getContext(), this.mPhoneId)) {
            builder = Uri.parse(str).buildUpon();
            builder.appendPath("nms").appendPath(str2).appendPath(str3).appendPath(this.mBoxId);
        } else {
            builder.scheme(protocol).encodedAuthority(str).appendPath("nms").appendPath(str2).appendPath(str3).appendPath(this.mBoxId);
        }
        String uri = builder.build().toString();
        this.mBaseUrl = uri;
        Log.i(this.TAG, IMSLog.checker(uri));
    }

    private void buildBaseURL(String str, String str2, String str3, String str4, boolean z) {
        String str5 = this.TAG;
        Log.d(str5, "isNcHost=" + z + ", This constructor is just for subscription");
        this.mBoxId = str4;
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getProtocol()).encodedAuthority(str).appendPath("pubsub").appendPath("oma_b").appendPath("nms").appendPath(str2).appendPath(this.mBoxId);
        String uri = builder.build().toString();
        this.mBaseUrl = uri;
        Log.i(this.TAG, IMSLog.checker(uri));
    }

    public void initCommonRequestHeaders(String str, String str2) {
        if (str == null || str.isEmpty() || !(str.compareTo("application/json") == 0 || str.compareTo(HttpController.CONTENT_TYPE_XML) == 0)) {
            str = "application/json";
        }
        if (this.mNMSRequestHeaderMap == null) {
            this.mNMSRequestHeaderMap = new HashMap();
        }
        this.mNMSRequestHeaderMap.put("Accept", str);
        if (!this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isGbaSupported()) {
            this.mNMSRequestHeaderMap.put("Authorization", str2);
        } else if (!TextUtils.isEmpty(this.mAuthorization)) {
            this.mNMSRequestHeaderMap.put("Authorization", this.mAuthorization);
        }
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEnableATTHeader()) {
            this.mNMSRequestHeaderMap.put("Connection", "Keep-Alive");
            this.mNMSRequestHeaderMap.put("x-att-clientVersion", ATTGlobalVariables.VERSION_NAME);
            this.mNMSRequestHeaderMap.put("x-att-clientId", ATTGlobalVariables.getHttpClientID());
            this.mNMSRequestHeaderMap.put("x-att-contextInfo", ATTGlobalVariables.BUILD_INFO);
            this.mNMSRequestHeaderMap.put("x-att-deviceId", this.mStoreClient.getPrerenceManager().getDeviceId());
        } else if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEnableTMOHeader()) {
            this.mStoreClient.getCloudMessageStrategyManager().getStrategy().updateHTTPHeader();
            if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isGbaSupported()) {
                this.mNMSRequestHeaderMap.put("User-Agent", "3gpp-gba");
            } else {
                this.mNMSRequestHeaderMap.put("User-Agent", TMOCmStrategy.TmoHttpHeaderValues.USER_AGENT_ID_VALUE);
            }
            this.mNMSRequestHeaderMap.put("device_id", TMOCmStrategy.TmoHttpHeaderValues.DEVICE_ID_VALUE);
        }
    }

    public void initMcsCommonRequestHeaders(String str, String str2) {
        if (str == null || str.isEmpty() || !(str.compareTo("application/json") == 0 || str.compareTo(HttpController.CONTENT_TYPE_XML) == 0)) {
            str = "application/json";
        }
        if (this.mNMSRequestHeaderMap == null) {
            this.mNMSRequestHeaderMap = new HashMap();
        }
        this.mNMSRequestHeaderMap.put("Authorization", str2);
        this.mNMSRequestHeaderMap.put(McsConstants.CommonHttpHeaders.DEVICE_NAME, Build.MODEL);
        this.mNMSRequestHeaderMap.put(McsConstants.CommonHttpHeaders.DEVICE_ID, this.mStoreClient.getPrerenceManager().getDeviceId());
        this.mNMSRequestHeaderMap.put(McsConstants.CommonHttpHeaders.DEVICE_TYPE, this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getDeviceType());
        this.mNMSRequestHeaderMap.put(McsConstants.CommonHttpHeaders.OS_VERSION, McsConstants.DeviceInfoValue.OS_VERSION);
        this.mNMSRequestHeaderMap.put(McsConstants.CommonHttpHeaders.CLIENT_VERSION, this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getClientVersion());
        this.mNMSRequestHeaderMap.put(McsConstants.CommonHttpHeaders.FIRMWARE_VERSION, Build.VERSION.INCREMENTAL);
        try {
            String replace = this.mCookieJar.loadForRequest(HttpUrl.get(new URI(this.mBaseUrl))).toString().replace("[", "").replace("]", "");
            if (!TextUtils.isEmpty(replace)) {
                this.mNMSRequestHeaderMap.put(HttpController.HEADER_COOKIE, replace);
            }
        } catch (URISyntaxException e) {
            Log.e(this.TAG, e.getMessage());
        }
        if (TextUtils.isEmpty(this.mNMSRequestHeaderMap.get("Content-Type"))) {
            this.mNMSRequestHeaderMap.put("Content-Type", str);
        }
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
            this.mNMSRequestHeaderMap.put(McsConstants.CommonHttpHeaders.OASIS_ENCRYPT, CloudMessageProviderContract.JsonData.TRUE);
        }
    }

    public void initSubscribeRequestHeaders(String str, String str2) {
        if (str == null || str.isEmpty() || !(str.compareTo("application/json") == 0 || str.compareTo(HttpController.CONTENT_TYPE_XML) == 0)) {
            str = "application/json";
        }
        if (this.mNMSRequestHeaderMap == null) {
            this.mNMSRequestHeaderMap = new HashMap();
        }
        this.mNMSRequestHeaderMap.put("Accept", str);
        this.mNMSRequestHeaderMap.put("Authorization", str2);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEnableATTHeader()) {
            this.mNMSRequestHeaderMap.put("Connection", "close");
            this.mNMSRequestHeaderMap.put("x-att-clientVersion", ATTGlobalVariables.VERSION_NAME);
            this.mNMSRequestHeaderMap.put("x-att-clientId", ATTGlobalVariables.getHttpClientID());
            this.mNMSRequestHeaderMap.put("x-att-contextInfo", ATTGlobalVariables.BUILD_INFO);
            this.mNMSRequestHeaderMap.put("x-att-deviceId", this.mStoreClient.getPrerenceManager().getDeviceId());
        } else if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEnableTMOHeader()) {
            this.mStoreClient.getCloudMessageStrategyManager().getStrategy().updateHTTPHeader();
            this.mNMSRequestHeaderMap.put("User-Agent", TMOCmStrategy.TmoHttpHeaderValues.USER_AGENT_ID_VALUE);
            this.mNMSRequestHeaderMap.put("device_id", TMOCmStrategy.TmoHttpHeaderValues.DEVICE_ID_VALUE);
        }
    }

    /* access modifiers changed from: protected */
    public void initCommonGetRequest() {
        setUrl(this.mBaseUrl);
        setHeaders(this.mNMSRequestHeaderMap);
        setMethod(HttpRequestParams.Method.GET);
        setFollowRedirects(false);
    }

    public void initCommonDeleteRequest() {
        setUrl(this.mBaseUrl);
        setHeaders(this.mNMSRequestHeaderMap);
        setMethod(HttpRequestParams.Method.DELETE);
        setFollowRedirects(false);
    }

    public void addRequestHeader(String str, String str2) {
        if (this.mNMSRequestHeaderMap == null) {
            this.mNMSRequestHeaderMap = new HashMap();
        }
        this.mNMSRequestHeaderMap.put(str, str2);
    }

    public void setMultipleContentType(String str, String[] strArr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strArr.length; i++) {
            sb.append(strArr[i]);
            if (i != strArr.length - 1) {
                sb.append(",");
            }
        }
        if (this.mNMSRequestHeaderMap == null) {
            this.mNMSRequestHeaderMap = new HashMap();
        }
        this.mNMSRequestHeaderMap.put(str, sb.toString());
        setHeaders(this.mNMSRequestHeaderMap);
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        if (this.isCmsMcsEnabled) {
            return this;
        }
        String boxId = getBoxId();
        this.mStoreClient = messageStoreClient;
        if (TextUtils.isEmpty(boxId)) {
            boxId = Util.getLineTelUriFromObjUrl(this.mBaseUrl);
        }
        String str = this.TAG;
        Log.i(str, "getRetryInstance: the box id is " + IMSLog.checker(boxId));
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(boxId));
        return this;
    }

    public OMAApiResponseParam getResponse(HttpResponseParams httpResponseParams) {
        Gson gson;
        if (this.isCmsMcsEnabled) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Class<Attribute> cls = Attribute.class;
            gsonBuilder.registerTypeAdapter(cls, new GsonInterfaceAdapter(cls));
            gson = gsonBuilder.disableHtmlEscaping().create();
        } else {
            gson = new Gson();
        }
        try {
            return (OMAApiResponseParam) gson.fromJson(getDecryptedString(httpResponseParams.getDataString()), OMAApiResponseParam.class);
        } catch (Exception e) {
            Log.e(this.TAG, e.toString());
            return null;
        }
    }

    public String getDecryptedString(String str) {
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
            String decrypt = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(str, true);
            String str2 = this.TAG;
            IMSLog.s(str2, "getDecryptedString: decryptedData: " + decrypt);
            if (!TextUtils.isEmpty(decrypt)) {
                return decrypt;
            }
        }
        return str;
    }

    public String getBoxId() {
        if (TextUtils.isEmpty(this.mBoxId)) {
            this.mBoxId = Util.getLineTelUriFromObjUrl(this.mBaseUrl);
        }
        return this.mBoxId;
    }

    public boolean updateToken() {
        String str;
        if (TextUtils.isEmpty(this.mBoxId)) {
            this.mBoxId = Util.getLineTelUriFromObjUrl(this.mBaseUrl);
        }
        String str2 = this.TAG;
        Log.i(str2, "updateToken: the box id is " + IMSLog.checker(this.mBoxId));
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isGbaSupported()) {
            str = this.mAuthorization;
            if (TextUtils.isEmpty(str)) {
                return true;
            }
        } else if (!this.isCmsMcsEnabled) {
            str = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(this.mBoxId);
            if (TextUtils.isEmpty(str)) {
                return false;
            }
        } else {
            str = null;
        }
        if (this.isCmsMcsEnabled) {
            initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        } else {
            initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), str);
        }
        return true;
    }

    public boolean updateToken(String str) {
        if (this.mBoxId == null) {
            this.mBoxId = str;
        }
        String str2 = this.TAG;
        Log.d(str2, "set box id : " + IMSLog.checker(this.mBoxId));
        return updateToken();
    }

    public void replaceUrlPrefix() {
        String str = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getProtocol() + ":";
        if (!TextUtils.isEmpty(this.mBaseUrl) && !TextUtils.isEmpty(str)) {
            Log.i(this.TAG, "replaceUrlPrefix with: " + str);
            String replaceUrlPrefix = Util.replaceUrlPrefix(this.mBaseUrl, str);
            this.mBaseUrl = replaceUrlPrefix;
            setUrl(replaceUrlPrefix);
        }
    }

    public boolean shouldCareAfterResponsePreProcess(IAPICallFlowListener iAPICallFlowListener, HttpResponseParams httpResponseParams, Object obj, BufferDBChangeParam bufferDBChangeParam, int i) {
        return this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldCareAfterPreProcess(iAPICallFlowListener, this, httpResponseParams, obj, bufferDBChangeParam, i);
    }

    public void updateServerRoot(String str) {
        String replaceHostOfURL = Util.replaceHostOfURL(str, this.mBaseUrl);
        this.mBaseUrl = replaceHostOfURL;
        setUrl(replaceHostOfURL);
    }

    public String getResponseMessageId(String str) {
        JSONObject jSONObject;
        try {
            JSONObject jSONObject2 = new JSONObject(str);
            if (jSONObject2.isNull("requestError")) {
                return null;
            }
            JSONObject jSONObject3 = jSONObject2.getJSONObject("requestError");
            if (!jSONObject3.isNull("serviceException")) {
                JSONObject jSONObject4 = jSONObject3.getJSONObject("serviceException");
                if (jSONObject4 != null) {
                    return jSONObject4.getString("messageId");
                }
                return null;
            } else if (jSONObject3.isNull("policyException") || (jSONObject = jSONObject3.getJSONObject("policyException")) == null) {
                return null;
            } else {
                return jSONObject.getString("messageId");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void initPostRequest(PathList pathList, boolean z) {
        HttpPostBody httpPostBody;
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        if (z) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
            httpPostBody = new HttpPostBody(new Gson().toJson(pathList));
        } else {
            httpPostBody = null;
        }
        if (httpPostBody != null) {
            setPostBody(httpPostBody);
        }
    }

    private String getGbaAuthorization() {
        String str = this.TAG;
        IMSLog.s(str, "uri = " + this.mBaseUrl);
        String str2 = null;
        if (this.mChallenge == null) {
            return null;
        }
        String queryPathFromUrl = Util.queryPathFromUrl(this.mBaseUrl);
        try {
            str2 = HttpAuthGenerator.getGbaResponse(this.mStoreClient.getClientID(), this.mBaseUrl, this.mChallenge, getMethodString(), "TLS_RSA_WITH_AES_128_CBC_SHA");
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            String str3 = this.TAG;
            IMSLog.s(str3, "getGbaResponse failed: " + e.getMessage());
        }
        String str4 = this.TAG;
        IMSLog.s(str4, "authorization = " + str2);
        return Util.replaceUriOfAuth(str2, queryPathFromUrl);
    }

    /* access modifiers changed from: protected */
    public boolean handleUnAuthorized(HttpResponseParams httpResponseParams) {
        if (!this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isGbaSupported()) {
            return false;
        }
        this.mChallenge = Util.getChallengeFromHttpResponse(httpResponseParams);
        this.mAuthorization = getGbaAuthorization();
        updateToken();
        this.mStoreClient.getHttpController().execute(this);
        return true;
    }
}
