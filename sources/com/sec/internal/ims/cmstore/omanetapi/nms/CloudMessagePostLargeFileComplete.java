package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.LargeFileResponse;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.file.BaseFileRequest;
import com.sec.internal.omanetapi.file.UploadPartInfos;
import java.io.IOException;

public class CloudMessagePostLargeFileComplete extends BaseFileRequest {
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public String TAG = CloudMessagePostLargeFileComplete.class.getSimpleName();
    private final transient IAPICallFlowListener mIAPICallFlowListener;

    public CloudMessagePostLargeFileComplete(final IAPICallFlowListener iAPICallFlowListener, String str, UploadPartInfos uploadPartInfos, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getPrerenceManager().getOasisServerRoot(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient);
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        buildAPISpecificURLFromBase(str);
        initPostRequest(uploadPartInfos, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int statusCode = httpResponseParams.getStatusCode();
                String r1 = CloudMessagePostLargeFileComplete.this.TAG;
                Log.i(r1, "onComplete: status Code " + statusCode);
                LargeFileResponse largeFileResponse = null;
                if (statusCode == 201 || statusCode == 200) {
                    try {
                        largeFileResponse = (LargeFileResponse) new Gson().fromJson(CloudMessagePostLargeFileComplete.this.getDecryptedString(httpResponseParams.getDataString()), LargeFileResponse.class);
                    } catch (JsonSyntaxException e) {
                        Log.e(CloudMessagePostLargeFileComplete.this.TAG, e.getMessage());
                    }
                    if (largeFileResponse != null) {
                        iAPICallFlowListener.onGoToEvent(OMASyncEventType.LARGE_FILE_UPLOAD_COMPLETED.getId(), largeFileResponse.accessURL);
                        return;
                    }
                    return;
                }
                iAPICallFlowListener.onGoToEvent(OMASyncEventType.FILE_API_FAILED.getId(), (Object) null);
            }

            public void onFail(IOException iOException) {
                String r2 = CloudMessagePostLargeFileComplete.this.TAG;
                Log.e(r2, "Http request onFail: " + iOException.getMessage());
            }
        });
    }

    private void initPostRequest(UploadPartInfos uploadPartInfos, boolean z) {
        HttpPostBody httpPostBody;
        OMAApiRequestParam.LargeFileRequestComplete largeFileRequestComplete = new OMAApiRequestParam.LargeFileRequestComplete();
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        if (z) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
            Gson create = new GsonBuilder().disableHtmlEscaping().create();
            largeFileRequestComplete.uploadPartInfos = uploadPartInfos;
            httpPostBody = new HttpPostBody(create.toJson(uploadPartInfos));
        } else {
            httpPostBody = null;
        }
        if (httpPostBody != null) {
            setPostBody(httpPostBody);
        }
    }

    private void buildAPISpecificURLFromBase(String str) {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath(str);
        buildUpon.appendPath("complete");
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(this.TAG, IMSLog.checker(uri));
    }
}
