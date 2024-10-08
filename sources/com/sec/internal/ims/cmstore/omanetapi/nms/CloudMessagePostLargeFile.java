package com.sec.internal.ims.cmstore.omanetapi.nms;

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
import com.sec.internal.omanetapi.common.data.LargeFileResponse;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.file.BaseFileRequest;
import java.io.IOException;

public class CloudMessagePostLargeFile extends BaseFileRequest {
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public String TAG = CloudMessagePostLargeFile.class.getSimpleName();
    private final transient IAPICallFlowListener mIAPICallFlowListener;

    public CloudMessagePostLargeFile(final IAPICallFlowListener iAPICallFlowListener, String str, String str2, Integer num, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getPrerenceManager().getOasisServerRoot(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient);
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        initPostRequest(str, str2, num, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int statusCode = httpResponseParams.getStatusCode();
                String r1 = CloudMessagePostLargeFile.this.TAG;
                Log.i(r1, "onComplete: status Code " + statusCode);
                LargeFileResponse largeFileResponse = null;
                if (statusCode == 201 || statusCode == 200) {
                    try {
                        largeFileResponse = (LargeFileResponse) new Gson().fromJson(CloudMessagePostLargeFile.this.getDecryptedString(httpResponseParams.getDataString()), LargeFileResponse.class);
                    } catch (JsonSyntaxException e) {
                        Log.e(CloudMessagePostLargeFile.this.TAG, e.getMessage());
                    }
                    if (largeFileResponse != null) {
                        iAPICallFlowListener.onGoToEvent(OMASyncEventType.UPLOAD_KEY_ID_RECEIVED.getId(), largeFileResponse);
                        return;
                    }
                    return;
                }
                iAPICallFlowListener.onGoToEvent(OMASyncEventType.FILE_API_FAILED.getId(), (Object) null);
            }

            public void onFail(IOException iOException) {
                String r2 = CloudMessagePostLargeFile.this.TAG;
                Log.e(r2, "Http request onFail: " + iOException.getMessage());
            }
        });
    }

    private void initPostRequest(String str, String str2, Integer num, boolean z) {
        HttpPostBody httpPostBody;
        String str3 = this.TAG;
        Log.d(str3, "initPostRequest contentType " + str + " fileName " + str2 + " totalLength " + num);
        OMAApiRequestParam.LargeFileRequestPost largeFileRequestPost = new OMAApiRequestParam.LargeFileRequestPost();
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        if (z) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
            largeFileRequestPost.contentType = str;
            largeFileRequestPost.fileName = str2;
            largeFileRequestPost.totalLength = num.intValue();
            httpPostBody = new HttpPostBody(new GsonBuilder().disableHtmlEscaping().create().toJson(largeFileRequestPost));
        } else {
            httpPostBody = null;
        }
        if (httpPostBody != null) {
            setPostBody(httpPostBody);
        }
    }
}
