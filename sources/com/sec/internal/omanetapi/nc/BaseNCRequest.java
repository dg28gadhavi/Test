package com.sec.internal.omanetapi.nc;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.cmstore.ScheduleConstant;
import com.sec.internal.constants.ims.cmstore.mcs.contactsync.McsContactSyncConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public abstract class BaseNCRequest extends HttpRequestParams implements IHttpAPICommonInterface {
    public static final int EXCEPTION_CONNECT = 802;
    private static final long serialVersionUID = 7698970710818917306L;
    private String TAG = BaseNCRequest.class.getSimpleName();
    protected String mBaseUrl;
    protected Context mContext;
    private CookieJar mCookieJar;
    protected transient Map<String, String> mNCRequestHeaderMap = new HashMap();
    /* access modifiers changed from: protected */
    public int mPhoneId;
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

    public BaseNCRequest(String str, String str2, String str3, MessageStoreClient messageStoreClient) {
        this.mPhoneId = messageStoreClient.getClientID();
        String str4 = this.TAG + "[" + this.mPhoneId + "]";
        this.TAG = str4;
        IMSLog.i(str4, "constructor");
        this.mStoreClient = messageStoreClient;
        this.mCookieJar = messageStoreClient.getHttpController().getCookieJar();
        this.mContext = messageStoreClient.getContext();
        buildBaseURL(str, str2, str3);
        setPhoneId(messageStoreClient.getClientID());
    }

    public BaseNCRequest(String str, MessageStoreClient messageStoreClient) {
        this.mPhoneId = messageStoreClient.getClientID();
        String str2 = this.TAG + "[" + this.mPhoneId + "]";
        this.TAG = str2;
        IMSLog.i(str2, "constructor");
        this.mStoreClient = messageStoreClient;
        this.mCookieJar = messageStoreClient.getHttpController().getCookieJar();
        this.mContext = messageStoreClient.getContext();
        this.mBaseUrl = str;
        setPhoneId(messageStoreClient.getClientID());
    }

    private void buildBaseURL(String str, String str2, String str3) {
        Uri.Builder builder = new Uri.Builder();
        String protocol = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getProtocol();
        boolean isMcsSupported = CmsUtil.isMcsSupported(this.mContext, this.mPhoneId);
        String str4 = isMcsSupported ? AuthenticationHeaders.HEADER_PARAM_NONCECOUNT : "notificationchannel";
        String str5 = this.TAG;
        IMSLog.i(str5, "buildBaseURL: isCmsMcsEnabled: " + isMcsSupported);
        if (ATTGlobalVariables.isGcmReplacePolling()) {
            builder.scheme(protocol).authority(str).appendPath("pubsub").appendPath("oma_b").appendPath(str4).appendPath(str2).appendPath(str3);
        } else if (isMcsSupported) {
            builder = Uri.parse(str).buildUpon();
            builder.appendPath(str4).appendPath(str2).appendPath(str3);
        } else {
            builder.scheme(protocol).authority(str).appendPath(str4).appendPath(str2).appendPath(str3);
        }
        String uri = builder.build().toString();
        this.mBaseUrl = uri;
        IMSLog.i(this.TAG, IMSLog.checker(uri));
    }

    public void initCommonRequestHeaders(String str, String str2) {
        if (str == null || str.isEmpty() || !(str.compareTo("application/json") == 0 || str.compareTo(HttpController.CONTENT_TYPE_XML) == 0)) {
            str = "application/json";
        }
        if (this.mNCRequestHeaderMap == null) {
            this.mNCRequestHeaderMap = new HashMap();
        }
        this.mNCRequestHeaderMap.put("Accept", str);
        this.mNCRequestHeaderMap.put("Authorization", str2);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEnableATTHeader()) {
            this.mNCRequestHeaderMap.put("Connection", "close");
            this.mNCRequestHeaderMap.put("x-att-clientVersion", ATTGlobalVariables.VERSION_NAME);
            this.mNCRequestHeaderMap.put("x-att-clientId", ATTGlobalVariables.getHttpClientID());
            this.mNCRequestHeaderMap.put("x-att-contextInfo", ATTGlobalVariables.BUILD_INFO);
            this.mNCRequestHeaderMap.put("x-att-deviceId", this.mStoreClient.getPrerenceManager().getDeviceId());
        }
    }

    public void initMcsCommonRequestHeaders(String str, String str2) {
        if (str == null || str.isEmpty() || !(str.compareTo("application/json") == 0 || str.compareTo(HttpController.CONTENT_TYPE_XML) == 0)) {
            str = "application/json";
        }
        if (this.mNCRequestHeaderMap == null) {
            this.mNCRequestHeaderMap = new HashMap();
        }
        this.mNCRequestHeaderMap.put("Authorization", str2);
        try {
            String replace = this.mCookieJar.loadForRequest(HttpUrl.get(new URI(this.mBaseUrl))).toString().replace("[", "").replace("]", "");
            if (!TextUtils.isEmpty(replace)) {
                this.mNCRequestHeaderMap.put(HttpController.HEADER_COOKIE, replace);
            }
        } catch (URISyntaxException e) {
            Log.e(this.TAG, e.getMessage());
        }
        this.mNCRequestHeaderMap.put("Content-Type", str);
        this.mNCRequestHeaderMap.put(McsConstants.CommonHttpHeaders.DEVICE_NAME, Build.MODEL);
        this.mNCRequestHeaderMap.put(McsConstants.CommonHttpHeaders.DEVICE_ID, this.mStoreClient.getPrerenceManager().getDeviceId());
        this.mNCRequestHeaderMap.put(McsConstants.CommonHttpHeaders.DEVICE_TYPE, this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getDeviceType());
        this.mNCRequestHeaderMap.put(McsConstants.CommonHttpHeaders.OS_VERSION, McsConstants.DeviceInfoValue.OS_VERSION);
        Map<String, String> map = this.mNCRequestHeaderMap;
        map.put(McsConstants.CommonHttpHeaders.CLIENT_VERSION, this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getClientVersion() + "/" + CmsUtil.getSmAppVersion(this.mContext));
        this.mNCRequestHeaderMap.put(McsConstants.CommonHttpHeaders.FIRMWARE_VERSION, Build.VERSION.INCREMENTAL);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
            this.mNCRequestHeaderMap.put(McsConstants.CommonHttpHeaders.OASIS_ENCRYPT, CloudMessageProviderContract.JsonData.TRUE);
        }
    }

    public void initCommonGetRequest() {
        setUrl(this.mBaseUrl);
        setHeaders(this.mNCRequestHeaderMap);
        setMethod(HttpRequestParams.Method.GET);
        setFollowRedirects(false);
    }

    public void initCommonDeleteRequest() {
        setUrl(this.mBaseUrl);
        setHeaders(this.mNCRequestHeaderMap);
        setMethod(HttpRequestParams.Method.DELETE);
        setFollowRedirects(false);
    }

    public void initCommonPutRequest() {
        setUrl(this.mBaseUrl);
        setHeaders(this.mNCRequestHeaderMap);
        setMethod(HttpRequestParams.Method.PUT);
        setFollowRedirects(false);
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        this.mStoreClient = messageStoreClient;
        return this;
    }

    /* access modifiers changed from: protected */
    public int checkRetryAfter(HttpResponseParams httpResponseParams, int i) {
        List list;
        String str = this.TAG;
        Log.d(str, " checkRetryAfter retryCount " + i);
        Map<String, List<String>> headers = httpResponseParams.getHeaders();
        if (headers != null && headers.containsKey(HttpRequest.HEADER_RETRY_AFTER) && (list = headers.get(HttpRequest.HEADER_RETRY_AFTER)) != null && list.size() > 0) {
            String str2 = (String) list.get(0);
            String str3 = this.TAG;
            IMSLog.d(str3, "retryAfter is " + str2 + "seconds retryAfterHeader: " + list);
            try {
                return Integer.parseInt(str2);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return -1;
            }
        } else if (i == 0) {
            return 25000;
        } else {
            if (i == 1) {
                return 125000;
            }
            if (i != 2) {
                return ScheduleConstant.UPDATE_SUBSCRIPTION_DELAY_TIME;
            }
            return 625000;
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

    public boolean isErrorCodeSupported(int i) {
        boolean isErrorCodeSupported = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isErrorCodeSupported(i, this);
        String str = this.TAG;
        IMSLog.i(str, "isErrorCodeSupported: " + isErrorCodeSupported);
        return isErrorCodeSupported;
    }

    public void updateContactSyncHeader() {
        boolean hasPackage = PackageUtils.hasPackage(this.mContext, McsContactSyncConstants.Packages.CS_PACKAGE_NAME);
        String str = this.TAG;
        IMSLog.i(str, "updateContactSyncHeader hasPackage: " + hasPackage);
        if (hasPackage) {
            this.mNCRequestHeaderMap.put("x-mcs-csapp", "INSTALLED");
        } else {
            this.mNCRequestHeaderMap.put("x-mcs-csapp", "UNINSTALLED");
        }
    }
}
