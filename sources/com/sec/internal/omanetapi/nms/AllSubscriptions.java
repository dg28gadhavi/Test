package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nms.data.NmsSubscription;

public class AllSubscriptions extends BaseNMSRequest {
    private static final String TAG = AllSubscriptions.class.getSimpleName();

    public AllSubscriptions(String str, String str2, String str3, String str4, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, str4, true, messageStoreClient);
        buildAPISpecificURLFromBase();
    }

    public void initPostRequest(NmsSubscription nmsSubscription, boolean z) {
        HttpPostBody httpPostBody;
        OMAApiRequestParam.AllSubscriptionRequest allSubscriptionRequest = new OMAApiRequestParam.AllSubscriptionRequest();
        allSubscriptionRequest.nmsSubscription = nmsSubscription;
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        if (z) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
            httpPostBody = new HttpPostBody(new Gson().toJson(allSubscriptionRequest));
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
        buildUpon.appendPath("subscriptions");
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, IMSLog.checker(uri));
    }
}
