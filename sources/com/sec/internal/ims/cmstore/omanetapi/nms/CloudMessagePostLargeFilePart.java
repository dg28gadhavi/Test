package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.LargeFileResponse;
import com.sec.internal.omanetapi.file.BaseFileRequest;
import com.sec.internal.omanetapi.file.UploadPartInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CloudMessagePostLargeFilePart extends BaseFileRequest {
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public String TAG;
    private final transient IAPICallFlowListener mIAPICallFlowListener;

    public CloudMessagePostLargeFilePart(final IAPICallFlowListener iAPICallFlowListener, final String str, final String str2, HttpPostBody httpPostBody, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getPrerenceManager().getOasisServerRoot(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient);
        String simpleName = CloudMessagePostLargeFilePart.class.getSimpleName();
        this.TAG = simpleName;
        Log.i(simpleName, " constructore uploadKeyId " + str + " partNum " + str2);
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        buildAPISpecificURLFromBase(str, str2);
        initPostRequest(httpPostBody);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int statusCode = httpResponseParams.getStatusCode();
                String r1 = CloudMessagePostLargeFilePart.this.TAG;
                Log.i(r1, "onComplete: status Code " + statusCode + " for request partNum " + str2 + " uploadKeyId substring " + str.substring(0, 10));
                LargeFileResponse largeFileResponse = null;
                if (statusCode == 201 || statusCode == 200) {
                    String decryptedString = CloudMessagePostLargeFilePart.this.getDecryptedString(httpResponseParams.getDataString());
                    Gson gson = new Gson();
                    String r12 = CloudMessagePostLargeFilePart.this.TAG;
                    Log.i(r12, " response data string " + decryptedString);
                    try {
                        largeFileResponse = (LargeFileResponse) gson.fromJson(decryptedString, LargeFileResponse.class);
                    } catch (JsonSyntaxException e) {
                        Log.e(CloudMessagePostLargeFilePart.this.TAG, e.getMessage());
                    }
                    if (largeFileResponse != null) {
                        String r7 = CloudMessagePostLargeFilePart.this.TAG;
                        Log.i(r7, "response part Tag " + largeFileResponse.partTag);
                        UploadPartInfo uploadPartInfo = new UploadPartInfo();
                        uploadPartInfo.partNum = Integer.parseInt(str2);
                        uploadPartInfo.partTag = largeFileResponse.partTag;
                        iAPICallFlowListener.onGoToEvent(OMASyncEventType.PART_UPLOADED.getId(), uploadPartInfo);
                        return;
                    }
                    return;
                }
                iAPICallFlowListener.onGoToEvent(OMASyncEventType.FILE_API_FAILED.getId(), (Object) null);
            }

            public void onFail(IOException iOException) {
                String r2 = CloudMessagePostLargeFilePart.this.TAG;
                Log.e(r2, "Http request onFail: " + iOException.getMessage());
            }
        });
    }

    private void initPostRequest(HttpPostBody httpPostBody) {
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        ArrayList arrayList = new ArrayList();
        if (httpPostBody != null) {
            String str = this.TAG;
            Log.i(str, "initPostRequest " + httpPostBody);
            Map<String, String> map = this.mNMSRequestHeaderMap;
            map.put("Content-Type", HttpPostBody.CONTENT_TYPE_MULTIPART + "form-data; name=\"file-part\"");
            arrayList.add(httpPostBody);
            setPostBody(new HttpPostBody((List<HttpPostBody>) arrayList));
        }
        setHeaders(this.mNMSRequestHeaderMap);
    }

    private void buildAPISpecificURLFromBase(String str, String str2) {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath(str);
        buildUpon.appendPath("part");
        buildUpon.appendPath(str2);
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(this.TAG, IMSLog.checker(uri));
    }
}
