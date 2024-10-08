package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.log.IMSLog;

public class IndividualFlag extends BaseNMSRequest {
    public static final String TAG = IndividualFlag.class.getSimpleName();
    private static final long serialVersionUID = -1015575143165860338L;
    private final String mFlagName;
    private final String mObjectId;

    public IndividualFlag(String str, String str2, String str3, String str4, String str5, String str6, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, str4, messageStoreClient);
        this.mObjectId = str5;
        this.mFlagName = str6;
        buildAPISpecificURLFromBase();
    }

    public void initPutRequest(boolean z) {
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.PUT);
        setFollowRedirects(false);
        HttpPostBody httpPostBody = new HttpPostBody("");
        if (z) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
        }
        setPostBody(httpPostBody);
    }

    public void initDeleteRequest() {
        super.initCommonDeleteRequest();
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath("objects");
        buildUpon.appendPath(this.mObjectId);
        buildUpon.appendPath("flags");
        buildUpon.appendPath(this.mFlagName);
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, IMSLog.checker(uri));
    }
}
