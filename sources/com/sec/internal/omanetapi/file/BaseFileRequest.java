package com.sec.internal.omanetapi.file;

import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseFileRequest extends HttpRequestParams implements IHttpAPICommonInterface {
    private static final long serialVersionUID = 1;
    private String TAG = BaseFileRequest.class.getSimpleName();
    protected boolean isCmsMcsEnabled;
    protected String mBaseUrl;
    protected String mChallenge;
    protected transient Map<String, String> mNMSRequestHeaderMap = new HashMap();
    protected int mPhoneId;
    protected MessageStoreClient mStoreClient;

    public HttpRequestParams getRetryInstance(SyncMsgType syncMsgType, IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return this;
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        return this;
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return this;
    }

    public BaseFileRequest(String str, String str2, MessageStoreClient messageStoreClient) {
        String str3 = this.TAG + "[" + messageStoreClient.getClientID() + "]";
        this.TAG = str3;
        Log.i(str3, "constructor1");
        this.mStoreClient = messageStoreClient;
        this.mPhoneId = messageStoreClient.getClientID();
        this.isCmsMcsEnabled = CmsUtil.isMcsSupported(this.mStoreClient.getContext(), this.mPhoneId);
        buildBaseURL(str, str2);
        setPhoneId(messageStoreClient.getClientID());
    }

    private void buildBaseURL(String str, String str2) {
        Uri.Builder buildUpon = Uri.parse(str).buildUpon();
        buildUpon.appendPath("oapi").appendPath(str2).appendPath("large-file");
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(this.TAG, IMSLog.checker(uri));
    }

    public void initMcsCommonRequestHeaders(String str, String str2) {
        if (str == null || str.isEmpty() || !(str.compareTo("application/json") == 0 || str.compareTo(HttpController.CONTENT_TYPE_XML) == 0)) {
            str = "application/json";
        }
        if (this.mNMSRequestHeaderMap == null) {
            this.mNMSRequestHeaderMap = new HashMap();
        }
        this.mNMSRequestHeaderMap.put("Authorization", str2);
        this.mNMSRequestHeaderMap.put("Content-Type", str);
        this.mNMSRequestHeaderMap.put(McsConstants.CommonHttpHeaders.DEVICE_NAME, Build.MODEL);
        this.mNMSRequestHeaderMap.put(McsConstants.CommonHttpHeaders.DEVICE_ID, this.mStoreClient.getPrerenceManager().getDeviceId());
        this.mNMSRequestHeaderMap.put(McsConstants.CommonHttpHeaders.DEVICE_TYPE, this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getDeviceType());
        this.mNMSRequestHeaderMap.put(McsConstants.CommonHttpHeaders.OS_VERSION, McsConstants.DeviceInfoValue.OS_VERSION);
        this.mNMSRequestHeaderMap.put(McsConstants.CommonHttpHeaders.CLIENT_VERSION, this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getClientVersion());
        this.mNMSRequestHeaderMap.put(McsConstants.CommonHttpHeaders.FIRMWARE_VERSION, Build.VERSION.INCREMENTAL);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
            this.mNMSRequestHeaderMap.put(McsConstants.CommonHttpHeaders.OASIS_ENCRYPT, CloudMessageProviderContract.JsonData.TRUE);
        }
    }

    public void initMcsCommonRequestHeaders(String str, String str2, String str3) {
        initMcsCommonRequestHeaders(str, str2);
        this.mNMSRequestHeaderMap.put("Range", str3);
    }

    /* access modifiers changed from: protected */
    public void initCommonGetRequest() {
        setUrl(this.mBaseUrl);
        setHeaders(this.mNMSRequestHeaderMap);
        setMethod(HttpRequestParams.Method.GET);
        setFollowRedirects(false);
    }

    public boolean shouldCareAfterResponsePreProcess(IAPICallFlowListener iAPICallFlowListener, HttpResponseParams httpResponseParams, Object obj, BufferDBChangeParam bufferDBChangeParam, int i) {
        return this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldCareAfterPreProcess(iAPICallFlowListener, this, httpResponseParams, obj, bufferDBChangeParam, i);
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

    public void updateServerRoot(String str) {
        String replaceHostOfURL = Util.replaceHostOfURL(str, this.mBaseUrl);
        this.mBaseUrl = replaceHostOfURL;
        setUrl(replaceHostOfURL);
    }
}
