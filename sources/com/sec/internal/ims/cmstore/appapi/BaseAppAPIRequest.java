package com.sec.internal.ims.cmstore.appapi;

import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class BaseAppAPIRequest extends HttpRequestParams {
    private String TAG = BaseAppAPIRequest.class.getSimpleName();
    protected transient Map<String, String> mAppRequestHeaderMap = new HashMap();
    protected String mBaseUrl;
    protected MessageStoreClient mStoreClient;

    public BaseAppAPIRequest(MessageStoreClient messageStoreClient, String str) {
        this.mBaseUrl = str;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        setFollowRedirects(false);
        setPhoneId(messageStoreClient.getClientID());
        this.mStoreClient = messageStoreClient;
    }

    /* access modifiers changed from: protected */
    public void initCommonRequestHeaders(String str) {
        setUrl(this.mBaseUrl);
        if (this.mAppRequestHeaderMap == null) {
            this.mAppRequestHeaderMap = new HashMap();
        }
        this.mAppRequestHeaderMap.put("Accept", "application/json");
        this.mAppRequestHeaderMap.put("Content-Type", "application/json");
        this.mAppRequestHeaderMap.put("Authorization", str);
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
            this.mAppRequestHeaderMap.put(McsConstants.CommonHttpHeaders.OASIS_ENCRYPT, CloudMessageProviderContract.JsonData.TRUE);
        }
        setHeaders(this.mAppRequestHeaderMap);
    }

    /* access modifiers changed from: protected */
    public void initMethodAndBody(String str, int i) {
        if (i == 0) {
            setMethod(HttpRequestParams.Method.POST);
            setPostBody(str);
        } else if (i == 1) {
            setMethod(HttpRequestParams.Method.DELETE);
        } else if (i == 2) {
            setMethod(HttpRequestParams.Method.GET);
        } else if (i == 3) {
            setMethod(HttpRequestParams.Method.PUT);
            setPostBody(str);
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
}
