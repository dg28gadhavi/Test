package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.FlagList;

public class AllFlags extends BaseNMSRequest {
    private static final String TAG = AllFlags.class.getSimpleName();
    private final String mObjectId;

    public AllFlags(String str, String str2, String str3, String str4, String str5, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, str4, messageStoreClient);
        this.mObjectId = str5;
        buildAPISpecificURLFromBase();
    }

    public void initGetRequest() {
        super.initCommonGetRequest();
    }

    public void initPutRequest(FlagList flagList, boolean z) {
        HttpPostBody httpPostBody;
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.PUT);
        setFollowRedirects(false);
        if (z) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
            httpPostBody = new HttpPostBody(new Gson().toJson(flagList));
        } else {
            httpPostBody = null;
        }
        if (httpPostBody != null) {
            setPostBody(httpPostBody);
        }
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath("objects");
        buildUpon.appendPath(this.mObjectId);
        buildUpon.appendPath("flags");
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, IMSLog.checker(uri));
    }
}
