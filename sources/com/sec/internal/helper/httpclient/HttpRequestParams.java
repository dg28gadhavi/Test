package com.sec.internal.helper.httpclient;

import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.net.Proxy;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.SocketFactory;
import okhttp3.Dns;
import org.json.JSONArray;
import org.json.JSONObject;

public class HttpRequestParams {
    private String mBsfUrl;
    private HttpRequestCallback mCallback;
    private byte[] mCipherSuite;
    private int mCiphersuite;
    private long mConnectionTimeout;
    private Dns mDns;
    private boolean mFollowRedirects;
    private Map<String, String> mHeaders;
    private boolean mIgnoreServerCert;
    private int mIpVersion;
    private Method mMethod;
    private String mNafUrl;
    private String mPassword;
    private int mPhoneId;
    private HttpPostBody mPostBody;
    private Proxy mProxy;
    private HttpQueryParams mQueryParams;
    private long mReadTimeout;
    private boolean mRetryOnConnectionFailure;
    private boolean mReuseConnection;
    private SocketFactory mSocketFactory;
    private int mToken;
    private boolean mUseForcedProtocol;
    private boolean mUseImei;
    private boolean mUseProxy;
    private boolean mUseTls;
    private String mUserName;
    private long mWriteTimeout;

    public interface HttpRequestCallback {
        void onComplete(HttpResponseParams httpResponseParams);

        void onFail(IOException iOException);
    }

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE,
        TRACE,
        HEAD,
        OPTIONS
    }

    public HttpRequestParams() {
        this.mMethod = null;
        this.mNafUrl = null;
        this.mBsfUrl = null;
        this.mReuseConnection = false;
        this.mQueryParams = null;
        this.mHeaders = null;
        this.mCallback = null;
        this.mPostBody = null;
        this.mConnectionTimeout = 30000;
        this.mReadTimeout = 30000;
        this.mWriteTimeout = 30000;
        this.mFollowRedirects = true;
        this.mSocketFactory = null;
        this.mPhoneId = 0;
        this.mDns = null;
        this.mUserName = null;
        this.mPassword = null;
        this.mCipherSuite = null;
        this.mUseTls = false;
        this.mIgnoreServerCert = false;
        this.mRetryOnConnectionFailure = true;
        this.mIpVersion = 0;
        this.mProxy = null;
        this.mUseProxy = false;
        this.mUseImei = false;
        this.mToken = 0;
    }

    public HttpRequestParams(Method method, String str, Map<String, String> map, HttpRequestCallback httpRequestCallback) {
        this.mBsfUrl = null;
        this.mReuseConnection = false;
        this.mQueryParams = null;
        this.mPostBody = null;
        this.mConnectionTimeout = 30000;
        this.mReadTimeout = 30000;
        this.mWriteTimeout = 30000;
        this.mFollowRedirects = true;
        this.mSocketFactory = null;
        this.mPhoneId = 0;
        this.mDns = null;
        this.mUserName = null;
        this.mPassword = null;
        this.mCipherSuite = null;
        this.mUseTls = false;
        this.mIgnoreServerCert = false;
        this.mRetryOnConnectionFailure = true;
        this.mIpVersion = 0;
        this.mProxy = null;
        this.mUseProxy = false;
        this.mUseImei = false;
        this.mToken = 0;
        this.mMethod = method;
        this.mNafUrl = str;
        this.mHeaders = map;
        this.mCallback = httpRequestCallback;
    }

    public HttpRequestParams(Method method, String str, String str2, Map<String, String> map, HttpRequestCallback httpRequestCallback) {
        this.mReuseConnection = false;
        this.mQueryParams = null;
        this.mPostBody = null;
        this.mConnectionTimeout = 30000;
        this.mReadTimeout = 30000;
        this.mWriteTimeout = 30000;
        this.mFollowRedirects = true;
        this.mSocketFactory = null;
        this.mPhoneId = 0;
        this.mDns = null;
        this.mUserName = null;
        this.mPassword = null;
        this.mCipherSuite = null;
        this.mUseTls = false;
        this.mIgnoreServerCert = false;
        this.mRetryOnConnectionFailure = true;
        this.mIpVersion = 0;
        this.mProxy = null;
        this.mUseProxy = false;
        this.mUseImei = false;
        this.mToken = 0;
        this.mMethod = method;
        this.mNafUrl = str;
        this.mBsfUrl = str2;
        this.mHeaders = map;
        this.mCallback = httpRequestCallback;
    }

    public HttpRequestParams(Map<String, String> map) {
        this.mMethod = null;
        this.mNafUrl = null;
        this.mBsfUrl = null;
        this.mReuseConnection = false;
        this.mQueryParams = null;
        this.mCallback = null;
        this.mPostBody = null;
        this.mConnectionTimeout = 30000;
        this.mReadTimeout = 30000;
        this.mWriteTimeout = 30000;
        this.mFollowRedirects = true;
        this.mSocketFactory = null;
        this.mPhoneId = 0;
        this.mDns = null;
        this.mUserName = null;
        this.mPassword = null;
        this.mCipherSuite = null;
        this.mUseTls = false;
        this.mIgnoreServerCert = false;
        this.mRetryOnConnectionFailure = true;
        this.mIpVersion = 0;
        this.mProxy = null;
        this.mUseProxy = false;
        this.mUseImei = false;
        this.mToken = 0;
        this.mHeaders = map;
    }

    public Method getMethod() {
        return this.mMethod;
    }

    /* renamed from: com.sec.internal.helper.httpclient.HttpRequestParams$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method;

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|4|5|6|7|8|9|10|11|12|(3:13|14|16)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(16:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|16) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.helper.httpclient.HttpRequestParams$Method[] r0 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method = r0
                com.sec.internal.helper.httpclient.HttpRequestParams$Method r1 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.GET     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.helper.httpclient.HttpRequestParams$Method r1 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.POST     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.helper.httpclient.HttpRequestParams$Method r1 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.PUT     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.helper.httpclient.HttpRequestParams$Method r1 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.DELETE     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.helper.httpclient.HttpRequestParams$Method r1 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.HEAD     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.helper.httpclient.HttpRequestParams$Method r1 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.OPTIONS     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.helper.httpclient.HttpRequestParams$Method r1 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.TRACE     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.httpclient.HttpRequestParams.AnonymousClass1.<clinit>():void");
        }
    }

    public String getMethodString() {
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method[this.mMethod.ordinal()]) {
            case 1:
                return "GET";
            case 2:
                return "POST";
            case 3:
                return "PUT";
            case 4:
                return HttpController.METHOD_DELETE;
            case 5:
                return HttpController.METHOD_HEAD;
            case 6:
                return HttpController.METHOD_OPTIONS;
            case 7:
                return HttpController.METHOD_TRACE;
            default:
                return "";
        }
    }

    public HttpRequestParams setMethod(Method method) {
        this.mMethod = method;
        return this;
    }

    public String getUrl() {
        return this.mNafUrl;
    }

    public String getBsfUrl() {
        return this.mBsfUrl;
    }

    public HttpRequestParams setUrl(String str) {
        this.mNafUrl = str;
        return this;
    }

    public HttpRequestParams setBsfUrl(String str) {
        this.mBsfUrl = str;
        return this;
    }

    public HttpRequestParams setReuseConnection(boolean z) {
        this.mReuseConnection = z;
        return this;
    }

    public boolean isReuseConnection() {
        return this.mReuseConnection;
    }

    public HttpQueryParams getQueryParams() {
        return this.mQueryParams;
    }

    public HttpRequestParams setQueryParams(HttpQueryParams httpQueryParams) {
        this.mQueryParams = httpQueryParams;
        return this;
    }

    public Map<String, String> getHeaders() {
        return this.mHeaders;
    }

    public HttpRequestParams setHeaders(Map<String, String> map) {
        this.mHeaders = map;
        return this;
    }

    public HttpRequestCallback getCallback() {
        return this.mCallback;
    }

    public HttpRequestParams setCallback(HttpRequestCallback httpRequestCallback) {
        this.mCallback = httpRequestCallback;
        return this;
    }

    public HttpPostBody getPostBody() {
        return this.mPostBody;
    }

    public HttpRequestParams setPostBody(HttpPostBody httpPostBody) {
        this.mPostBody = httpPostBody;
        return this;
    }

    public HttpRequestParams setPostBody(String str) {
        this.mPostBody = new HttpPostBody(str);
        return this;
    }

    public HttpRequestParams setPostBody(JSONObject jSONObject) {
        this.mPostBody = new HttpPostBody(jSONObject);
        return this;
    }

    public HttpRequestParams setPostBody(JSONArray jSONArray) {
        this.mPostBody = new HttpPostBody(jSONArray.toString());
        return this;
    }

    public HttpRequestParams setPostBody(byte[] bArr) {
        this.mPostBody = new HttpPostBody(bArr);
        return this;
    }

    public SocketFactory getSocketFactory() {
        return this.mSocketFactory;
    }

    public HttpRequestParams setSocketFactory(SocketFactory socketFactory) {
        this.mSocketFactory = socketFactory;
        return this;
    }

    public long getConnectionTimeout() {
        return this.mConnectionTimeout;
    }

    public HttpRequestParams setConnectionTimeout(long j) {
        this.mConnectionTimeout = j;
        return this;
    }

    public long getReadTimeout() {
        return this.mReadTimeout;
    }

    public HttpRequestParams setReadTimeout(long j) {
        this.mReadTimeout = j;
        return this;
    }

    public long getWriteTimeout() {
        return this.mWriteTimeout;
    }

    public HttpRequestParams setWriteTimeout(long j) {
        this.mWriteTimeout = j;
        return this;
    }

    public HttpRequestParams setPostParams(Map<String, String> map) {
        this.mPostBody = new HttpPostBody(map);
        return this;
    }

    public boolean getFollowRedirects() {
        return this.mFollowRedirects;
    }

    public HttpRequestParams setFollowRedirects(boolean z) {
        this.mFollowRedirects = z;
        return this;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public HttpRequestParams setPhoneId(int i) {
        this.mPhoneId = i;
        return this;
    }

    public Dns getDns() {
        return this.mDns;
    }

    public HttpRequestParams setDns(Dns dns) {
        this.mDns = dns;
        return this;
    }

    public String getUserName() {
        return this.mUserName;
    }

    public HttpRequestParams setUserName(String str) {
        this.mUserName = str;
        return this;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public HttpRequestParams setPassword(String str) {
        this.mPassword = str;
        return this;
    }

    public boolean getUseTls() {
        return this.mUseTls;
    }

    public HttpRequestParams setUseTls(boolean z) {
        this.mUseTls = z;
        return this;
    }

    public boolean getIgnoreServerCert() {
        return this.mIgnoreServerCert;
    }

    public HttpRequestParams setIgnoreServerCert(boolean z) {
        this.mIgnoreServerCert = z;
        return this;
    }

    public boolean getRetryOnConnectionFailure() {
        return this.mRetryOnConnectionFailure;
    }

    public void setProtocol(boolean z) {
        this.mUseForcedProtocol = z;
    }

    public boolean getForcedProtocolVersion() {
        return this.mUseForcedProtocol;
    }

    public int getIpVersion() {
        return this.mIpVersion;
    }

    public HttpRequestParams setIpVersion(int i) {
        this.mIpVersion = i;
        return this;
    }

    public Proxy getProxy() {
        return this.mProxy;
    }

    public HttpRequestParams setProxy(Proxy proxy) {
        this.mProxy = proxy;
        return this;
    }

    public boolean getUseProxy() {
        return this.mUseProxy;
    }

    public HttpRequestParams setUseProxy(boolean z) {
        this.mUseProxy = z;
        return this;
    }

    public void setUseImei(boolean z) {
        this.mUseImei = z;
    }

    public boolean getUseImei() {
        return this.mUseImei;
    }

    public void setCipherSuite(byte[] bArr) {
        this.mCipherSuite = bArr;
    }

    public byte[] getCipherSuite() {
        return this.mCipherSuite;
    }

    public void setToken(int i) {
        this.mToken = i;
    }

    public int getToken() {
        return this.mToken;
    }

    public HttpRequestParams setCipherSuiteType(int i) {
        this.mCiphersuite = i;
        return this;
    }

    public int getCipherSuiteType() {
        return this.mCiphersuite;
    }

    public String toDebugLogs() {
        StringBuffer stringBuffer = new StringBuffer();
        Map<String, String> map = this.mHeaders;
        if (map != null && !map.isEmpty()) {
            for (Map.Entry next : this.mHeaders.entrySet()) {
                if (((String) next.getKey()).contains("x-att-") && !((String) next.getKey()).contains("x-att-deviceId")) {
                    stringBuffer.append("\r\n        " + ((String) next.getKey()) + " : " + ((String) next.getValue()));
                }
            }
        }
        return stringBuffer.toString();
    }

    public String toString() {
        HttpPostBody httpPostBody;
        String checker;
        StringBuffer stringBuffer = new StringBuffer();
        Map<String, String> map = this.mHeaders;
        if (map != null && !map.isEmpty()) {
            for (Map.Entry next : this.mHeaders.entrySet()) {
                stringBuffer.append("\r\n        " + ((String) next.getKey()) + " : ");
                if ("Authorization".equalsIgnoreCase((String) next.getKey())) {
                    String str = (String) next.getValue();
                    Matcher matcher = Pattern.compile("username=\"[^\"]*@[^\"]*\"").matcher((CharSequence) next.getValue());
                    if (matcher.find() && (checker = IMSLog.checker(matcher.group())) != null && !checker.contains("username")) {
                        str = str.replaceAll("username=\"[^\"]*@[^\"]*\"", "username=\"" + checker + CmcConstants.E_NUM_STR_QUOTE);
                    }
                    Matcher matcher2 = Pattern.compile("uri=\"[^\"]*[^\"]\"").matcher((CharSequence) next.getValue());
                    if (matcher2.find()) {
                        str = str.replaceAll("uri=\"[^\"]*[^\"]\"", IMSLog.numberChecker(matcher2.group()));
                    }
                    stringBuffer.append(str);
                } else if ("X-3GPP-Intended-Identity".equalsIgnoreCase((String) next.getKey())) {
                    stringBuffer.append(IMSLog.numberChecker((String) next.getValue()));
                } else {
                    stringBuffer.append((String) next.getValue());
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("HttpRequestParams[\r\n    mMethod: ");
        sb.append(this.mMethod.name());
        sb.append("\r\n    mNafUrl: ");
        sb.append(IMSLog.numberChecker(this.mNafUrl));
        sb.append("\r\n    mBsfUrl: ");
        sb.append(IMSLog.numberChecker(this.mBsfUrl));
        sb.append("\r\n    mQueryParams: ");
        HttpQueryParams httpQueryParams = this.mQueryParams;
        String str2 = "";
        sb.append(httpQueryParams != null ? httpQueryParams.toString() : str2);
        sb.append("\r\n    mHeaders: ");
        CharSequence charSequence = stringBuffer;
        if (IMSLog.isShipBuild()) {
            charSequence = toDebugLogs();
        }
        sb.append(charSequence);
        sb.append("\r\n    mConnectionTimeout: ");
        sb.append(this.mConnectionTimeout);
        sb.append("\r\n    mReadTimeout: ");
        sb.append(this.mReadTimeout);
        sb.append("\r\n    mWriteTimeout: ");
        sb.append(this.mWriteTimeout);
        sb.append("\r\n    mFollowRedirects: ");
        sb.append(this.mFollowRedirects);
        sb.append("\r\n]\r\n    mPostBody: ");
        if (!IMSLog.isShipBuild() && (httpPostBody = this.mPostBody) != null) {
            str2 = IMSLog.numberChecker(httpPostBody.toString());
        }
        sb.append(str2);
        sb.append("\r\n]");
        return sb.toString();
    }
}
