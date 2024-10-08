package com.sec.internal.ims.cmstore.mcs.provision.cloudrequest;

import android.os.Build;
import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.cmstore.ScheduleConstant;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class BaseProvisionRequest extends HttpRequestParams implements IHttpAPICommonInterface {
    private static final long serialVersionUID = 1;
    public String TAG = BaseProvisionRequest.class.getSimpleName();
    protected final transient CookieJar mCookieJar;
    protected transient IAPICallFlowListener mFlowListener;
    protected IHttpAPICommonInterface mHttpInterface;
    protected Map<String, String> mMcsHeaderMap = new LinkedHashMap();
    private final int mPhoneId;
    protected transient MessageStoreClient mStoreClient;

    public HttpRequestParams getRetryInstance(SyncMsgType syncMsgType, IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return null;
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        return null;
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return null;
    }

    public void updateServerRoot(String str) {
    }

    public BaseProvisionRequest(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        this.mFlowListener = iAPICallFlowListener;
        this.mStoreClient = messageStoreClient;
        this.mCookieJar = messageStoreClient.getHttpController().getCookieJar();
        this.mPhoneId = messageStoreClient.getClientID();
        setFollowRedirects(false);
    }

    /* access modifiers changed from: protected */
    public void goSuccessfulCall(Object obj) {
        this.mFlowListener.onSuccessfulCall((IHttpAPICommonInterface) this, obj);
    }

    /* access modifiers changed from: protected */
    public void goFailedCall(int i) {
        this.mFlowListener.onFailedCall((IHttpAPICommonInterface) this, i);
    }

    public void setCommonRequestHeaders(String str, String str2) {
        if (str == null || str.isEmpty()) {
            str = "application/json";
        }
        this.mMcsHeaderMap.put("Authorization", str2);
        try {
            String replace = this.mCookieJar.loadForRequest(HttpUrl.get(new URI(getUrl()))).toString().replace("[", "").replace("]", "");
            if (!TextUtils.isEmpty(replace)) {
                this.mMcsHeaderMap.put(HttpController.HEADER_COOKIE, replace);
            }
        } catch (URISyntaxException e) {
            IMSLog.e(this.TAG, this.mPhoneId, e.getMessage());
        }
        this.mMcsHeaderMap.put("Content-Type", str);
        this.mMcsHeaderMap.put(McsConstants.CommonHttpHeaders.DEVICE_NAME, Build.MODEL);
        this.mMcsHeaderMap.put(McsConstants.CommonHttpHeaders.DEVICE_ID, this.mStoreClient.getPrerenceManager().getDeviceId());
        this.mMcsHeaderMap.put(McsConstants.CommonHttpHeaders.FIRMWARE_VERSION, Build.VERSION.INCREMENTAL);
        Map<String, String> map = this.mMcsHeaderMap;
        map.put(McsConstants.CommonHttpHeaders.CLIENT_VERSION, this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getClientVersion() + "/" + CmsUtil.getSmAppVersion(this.mStoreClient.getContext()));
        this.mMcsHeaderMap.put(McsConstants.CommonHttpHeaders.DEVICE_TYPE, this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getDeviceType());
        this.mMcsHeaderMap.put(McsConstants.CommonHttpHeaders.OS_VERSION, McsConstants.DeviceInfoValue.OS_VERSION);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
            this.mMcsHeaderMap.put(McsConstants.CommonHttpHeaders.OASIS_ENCRYPT, CloudMessageProviderContract.JsonData.TRUE);
        }
        setHeaders(this.mMcsHeaderMap);
    }

    public boolean isErrorCodeSupported(int i) {
        boolean z = i == 429 || i / 100 == 5;
        String str = this.TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "isErrorCodeSupported: " + z);
        return z;
    }

    /* access modifiers changed from: protected */
    public int checkRetryAfter(HttpResponseParams httpResponseParams, int i) {
        List list;
        String str = this.TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "checkRetryAfter retryCount " + i);
        Map<String, List<String>> headers = httpResponseParams.getHeaders();
        if (headers != null && headers.containsKey(HttpRequest.HEADER_RETRY_AFTER) && (list = headers.get(HttpRequest.HEADER_RETRY_AFTER)) != null && list.size() > 0) {
            String str2 = (String) list.get(0);
            String str3 = this.TAG;
            int i3 = this.mPhoneId;
            IMSLog.i(str3, i3, "retryAfter: " + str2 + "seconds");
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
}
