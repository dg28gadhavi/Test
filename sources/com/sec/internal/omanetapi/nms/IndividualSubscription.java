package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nms.data.NmsSubscriptionUpdate;

public class IndividualSubscription extends BaseNMSRequest {
    public static final String TAG = IndividualSubscription.class.getSimpleName();
    private static final long serialVersionUID = -461469967960054356L;
    private String mSubscriptionId;

    public IndividualSubscription(String str, String str2, String str3, String str4, String str5, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, str4, messageStoreClient);
        this.mSubscriptionId = str5;
        buildAPISpecificURLFromBase();
    }

    public IndividualSubscription(String str, MessageStoreClient messageStoreClient) {
        super(str, messageStoreClient);
    }

    public void initPostRequest(NmsSubscriptionUpdate nmsSubscriptionUpdate, boolean z) {
        HttpPostBody httpPostBody;
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        if (z) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
            OMAApiRequestParam.NmsSubscriptionUpdateRequest nmsSubscriptionUpdateRequest = new OMAApiRequestParam.NmsSubscriptionUpdateRequest();
            nmsSubscriptionUpdateRequest.nmsSubscriptionUpdate = nmsSubscriptionUpdate;
            httpPostBody = new HttpPostBody(new Gson().toJson(nmsSubscriptionUpdateRequest));
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
        buildUpon.appendPath(this.mSubscriptionId);
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, IMSLog.checker(uri));
    }
}
