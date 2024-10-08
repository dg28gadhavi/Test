package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamObjectUpload;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nms.BaseNMSRequest;
import java.io.IOException;

public class CloudMessageCreateFile extends BaseNMSRequest {
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageCreateFile.class.getSimpleName();
    private String mUploadUrl = null;

    public CloudMessageCreateFile(final IAPICallFlowListener iAPICallFlowListener, ParamObjectUpload paramObjectUpload, final boolean z, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getPrerenceManager().getOasisServerRoot(), messageStoreClient);
        String str = this.TAG + "[" + messageStoreClient.getClientID() + "]";
        this.TAG = str;
        IMSLog.i(str, "CloudMessageCreateFile");
        initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        this.mUploadUrl = Util.buildUploadURL(this.mStoreClient.getPrerenceManager().getOasisSmallFileServerRoot(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion());
        initPostRequestWithFile((HttpPostBody) paramObjectUpload.uploadObjectInfo.second);
        buildAPISpecificURLFromBase();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String r0 = CloudMessageCreateFile.this.TAG;
                Log.i(r0, "onComplete: status Code " + httpResponseParams.getStatusCode());
                if (httpResponseParams.getStatusCode() == 200 || httpResponseParams.getStatusCode() == 201) {
                    OMAApiResponseParam response = CloudMessageCreateFile.this.getResponse(httpResponseParams);
                    if (response != null && response.file != null) {
                        String r02 = CloudMessageCreateFile.this.TAG;
                        Log.i(r02, "onComplete: response " + response.file.size + " " + response.file.href);
                        if (z) {
                            iAPICallFlowListener.onGoToEvent(OMASyncEventType.THUMBNAIL_UPLOADED.getId(), response.file);
                        } else {
                            iAPICallFlowListener.onGoToEvent(OMASyncEventType.FILE_UPLOADED.getId(), response.file);
                        }
                    }
                } else if (CloudMessageCreateFile.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    iAPICallFlowListener.onGoToEvent(OMASyncEventType.FILE_API_FAILED.getId(), (Object) null);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageCreateFile.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                iAPICallFlowListener.onFailedCall(this);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath("oapi").appendPath("v1").appendPath("file");
        this.mBaseUrl = buildUpon.build().toString();
    }

    public void initPostRequestWithFile(HttpPostBody httpPostBody) {
        Log.d(this.TAG, "initPostRequestWithFile");
        setUrl(this.mUploadUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        this.mNMSRequestHeaderMap.put("Content-Type", HttpPostBody.CONTENT_TYPE_MULTIPART_FORMDATA);
        setPostBody(httpPostBody);
        setHeaders(this.mNMSRequestHeaderMap);
    }
}
