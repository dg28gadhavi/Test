package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.GsonBuilder;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nms.data.SelectionCriteria;

public class ObjectsOpSearch extends BaseNMSRequest {
    private static final String TAG = ObjectsOpSearch.class.getSimpleName();
    private static final long serialVersionUID = -6272072039068987383L;

    public ObjectsOpSearch(String str, String str2, String str3, String str4, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, str4, messageStoreClient);
        buildAPISpecificURLFromBase();
    }

    public void initPostRequest(SelectionCriteria selectionCriteria, boolean z) {
        HttpPostBody httpPostBody;
        OMAApiRequestParam.ObjectSearchRequest objectSearchRequest = new OMAApiRequestParam.ObjectSearchRequest();
        objectSearchRequest.selectionCriteria = selectionCriteria;
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        if (z) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
            httpPostBody = new HttpPostBody(new GsonBuilder().disableHtmlEscaping().create().toJson(objectSearchRequest));
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
        buildUpon.appendPath("operations");
        buildUpon.appendPath("search");
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, IMSLog.checker(uri));
    }
}
