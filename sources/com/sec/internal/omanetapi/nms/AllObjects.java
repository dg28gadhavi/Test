package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.nms.data.GsonInterfaceAdapter;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nms.data.Attribute;
import com.sec.internal.omanetapi.nms.data.Object;
import java.util.ArrayList;
import java.util.List;

public class AllObjects extends BaseNMSRequest {
    public static final String TAG = AllObjects.class.getSimpleName();
    private static final long serialVersionUID = -3559371338445770425L;
    private String mUploadUrl = null;

    public AllObjects(String str, String str2, String str3, String str4, MessageStoreClient messageStoreClient) {
        super(str, str2, str3, str4, messageStoreClient);
        this.mUploadUrl = Util.buildUploadURL(str, str2);
        buildAPISpecificURLFromBase();
    }

    public AllObjects(String str, MessageStoreClient messageStoreClient) {
        super(str, messageStoreClient);
    }

    public void initPostRequestWithFile(Object object, HttpPostBody httpPostBody) {
        Log.d(TAG, "initPostRequestWithFile");
        setUrl(this.mUploadUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        this.mNMSRequestHeaderMap.put("Content-Type", HttpPostBody.CONTENT_TYPE_MULTIPART_FORMDATA);
    }

    public void initPostRequest(Object object, boolean z, HttpPostBody httpPostBody) {
        String str;
        Log.d(TAG, "initPostRequest");
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        ArrayList arrayList = new ArrayList();
        if (z) {
            OMAApiRequestParam.AllObjectRequest allObjectRequest = new OMAApiRequestParam.AllObjectRequest();
            allObjectRequest.object = object;
            if (this.isCmsMcsEnabled) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Class<Attribute> cls = Attribute.class;
                gsonBuilder.registerTypeAdapter(cls, new GsonInterfaceAdapter(cls));
                str = gsonBuilder.disableHtmlEscaping().setPrettyPrinting().create().toJson(allObjectRequest);
            } else {
                str = new Gson().toJson(allObjectRequest);
            }
        } else {
            str = "";
        }
        if (httpPostBody != null) {
            this.mNMSRequestHeaderMap.put("Content-Type", HttpPostBody.CONTENT_TYPE_MULTIPART_FORMDATA);
            arrayList.add(new HttpPostBody("form-data; name=\"root-fields\"", "application/json", str));
            arrayList.add(httpPostBody);
            setPostBody(new HttpPostBody((List<HttpPostBody>) arrayList));
        } else {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setPostBody(new HttpPostBody(str));
        }
        setHeaders(this.mNMSRequestHeaderMap);
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath("objects");
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, IMSLog.checker(uri));
    }
}
