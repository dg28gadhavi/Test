package com.sec.internal.ims.aec.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.aec.util.HttpClient;
import com.sec.internal.ims.core.cmc.CmcConstants;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import org.json.JSONObject;

public class HttpStore {
    private String mAppId = null;
    private final Context mContext;
    private String mEapChallenge = null;
    private String mEapChallengeResp = null;
    private String mHostName = null;
    private Map<String, List<String>> mHttpHeader = new HashMap();
    private Map<String, String> mHttpParam = new HashMap();
    private JSONObject mHttpPostData = new JSONObject();
    private HttpClient.Response mHttpResponse = null;
    private String mHttpUrl = null;
    private Queue<String> mHttpUrls = new LinkedList();
    private Map<String, String> mParsedBody = new TreeMap();
    private final int mPhoneId;
    private String mUserAgent = null;

    public HttpStore(Context context, int i) {
        this.mContext = context;
        this.mPhoneId = i;
    }

    public void clearHttpStore() {
        this.mAppId = null;
        this.mEapChallenge = null;
        this.mEapChallengeResp = null;
        this.mHostName = null;
        this.mHttpResponse = null;
        this.mHttpUrl = null;
        this.mUserAgent = null;
        this.mHttpPostData = new JSONObject();
        this.mHttpHeader = new HashMap();
        this.mHttpParam = new HashMap();
        this.mParsedBody = new TreeMap();
        this.mHttpUrls = new LinkedList();
    }

    public String getAppId() {
        return this.mAppId;
    }

    public void setAppId(String str) {
        this.mAppId = str;
    }

    public String getHttpUrl() {
        return this.mHttpUrl;
    }

    public void setHttpUrl(String str) {
        this.mHttpUrl = str;
    }

    public Queue<String> getHttpUrls() {
        return this.mHttpUrls;
    }

    public void setHttpUrls(Queue<String> queue) {
        this.mHttpUrls = queue;
    }

    public String getHostName() {
        return this.mHostName;
    }

    public void setHostName(String str) {
        this.mHostName = str;
    }

    public Map<String, List<String>> getHttpHeaders() {
        return this.mHttpHeader;
    }

    public Map<String, String> getHttpParams() {
        return this.mHttpParam;
    }

    public void setHttpParam(String str, String str2) {
        this.mHttpParam.put(str, str2);
    }

    public JSONObject getHttpPostData() {
        return this.mHttpPostData;
    }

    public HttpClient.Response getHttpResponse() {
        return this.mHttpResponse;
    }

    public void setHttpResponse(HttpClient.Response response) {
        this.mHttpResponse = response;
    }

    public Map<String, String> getParsedBody() {
        return this.mParsedBody;
    }

    public void setParsedBody(Map<String, String> map) {
        this.mParsedBody = map;
    }

    public String getEapChallenge() {
        return this.mEapChallenge;
    }

    public void setEapChallenge(String str) {
        this.mEapChallenge = str;
    }

    public String getEapChallengeResp() {
        return this.mEapChallengeResp;
    }

    public void setEapChallengeResp(String str) {
        this.mEapChallengeResp = str;
    }

    public void setHttpPushParam(String str, String str2) {
        if (!TextUtils.isEmpty(str) && !TextUtils.isEmpty(str2)) {
            setHttpParam("notif_action", str);
            setHttpParam(AECNamespace.PramsName.NOTIF_TOKEN, str2);
        }
    }

    public String getUserAgent() {
        return this.mUserAgent;
    }

    public void setUserAgent(String str) {
        this.mUserAgent = String.format(AECNamespace.Template.USER_AGENT, new Object[]{str, AECNamespace.Build.TERMINAL_VENDOR, AECNamespace.Build.TERMINAL_SW_VERSION, AECNamespace.Build.ANDROID_OS_VERSION});
    }

    public void initHttpGetInfo(int i, String str) throws Exception {
        initHttpHeaders();
        this.mHttpParam = new HashMap();
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        if (telephonyManager == null || telephonyManager.getImei(this.mPhoneId) == null) {
            throw new IOException("initHttpGetInfo: TelephonyManager or imei not ready");
        }
        setHttpParam("terminal_vendor", "SEC");
        setHttpParam("terminal_model", AECNamespace.Build.TERMINAL_MODEL);
        setHttpParam("terminal_sw_version", AECNamespace.Build.TERMINAL_SW_VERSION);
        setHttpParam("entitlement_version", str);
        setHttpParam(AECNamespace.PramsName.TERMINAL_ID, telephonyManager.getImei(this.mPhoneId));
        setHttpParam("vers", Integer.toString(i));
        setHttpParam("app", getAppId());
    }

    public void initHttpPostInfo(String str, List<String> list) throws Exception {
        initHttpHeaders();
        this.mHttpPostData = new JSONObject();
        if (list == null || list.isEmpty()) {
            throw new IOException("initHttpPostInfo: empty cookie");
        }
        this.mHttpHeader.put(HttpController.HEADER_COOKIE, extractCookie(list));
        if (!TextUtils.isEmpty(str)) {
            this.mHttpHeader.put("Content-Type", Collections.singletonList("application/vnd.gsma.eap-relay.v1.0+json"));
            this.mHttpPostData.put("eap-relay-packet", str);
            return;
        }
        throw new IOException("initHttpPostInfo: empty eap challenge response");
    }

    private void initHttpHeaders() {
        Locale locale = Locale.getDefault();
        HashMap hashMap = new HashMap();
        this.mHttpHeader = hashMap;
        hashMap.put(HttpController.HEADER_HOST, Collections.singletonList(getHostName()));
        this.mHttpHeader.put("User-Agent", Collections.singletonList(getUserAgent()));
        this.mHttpHeader.put("Connection", Collections.singletonList("Keep-Alive"));
        this.mHttpHeader.put("Accept", Collections.singletonList("application/vnd.gsma.eap-relay.v1.0+json".concat(", ").concat(AECNamespace.HTTP_CONTENT_TYPE.XML)));
        this.mHttpHeader.put(HttpController.HEADER_CACHE_CONTROL, Collections.singletonList("max-age=0"));
        if (locale != null) {
            this.mHttpHeader.put("Accept-Language", Collections.singletonList(locale.getLanguage().concat(CmcConstants.E_NUM_SLOT_SPLIT).concat(locale.getCountry())));
        }
    }

    private List<String> extractCookie(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String split : list) {
            for (String trim : split.split(";")) {
                String trim2 = trim.trim();
                if (sb.length() != 0) {
                    sb.append("; ");
                }
                sb.append(trim2);
            }
        }
        return Collections.singletonList(sb.toString());
    }
}
