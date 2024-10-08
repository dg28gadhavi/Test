package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import java.util.ArrayList;
import java.util.List;

public class ObjectsOpBulkCreation extends BaseNMSRequest {
    private static final String TAG = ObjectsOpBulkCreation.class.getSimpleName();

    public ObjectsOpBulkCreation(String str, String str2, String str3, String str4, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, str4, messageStoreClient);
        buildAPISpecificURLFromBase();
    }

    public void initPostRequest(ObjectList objectList, boolean z, List<HttpPostBody> list) {
        setUrl(this.mBaseUrl);
        this.mNMSRequestHeaderMap.put("Content-Type", HttpPostBody.CONTENT_TYPE_MULTIPART_FORMDATA);
        setHeaders(this.mNMSRequestHeaderMap);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        ArrayList arrayList = new ArrayList();
        if (z) {
            arrayList.add(new HttpPostBody("form-data; name=root-fields", "application/json", new Gson().toJson(objectList)));
        }
        for (HttpPostBody add : list) {
            arrayList.add(add);
        }
        setPostBody(new HttpPostBody((List<HttpPostBody>) arrayList));
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath("objects");
        buildUpon.appendPath("operations");
        buildUpon.appendPath("bulkCreation");
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, IMSLog.checker(uri));
    }
}
