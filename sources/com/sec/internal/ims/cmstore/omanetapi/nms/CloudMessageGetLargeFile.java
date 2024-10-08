package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.file.BaseFileRequest;
import com.sec.internal.omanetapi.file.LargeFileDownloadParams;
import java.io.IOException;

public class CloudMessageGetLargeFile extends BaseFileRequest {
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageGetLargeFile.class.getSimpleName();

    public CloudMessageGetLargeFile(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, String str, String str2, String str3) {
        super(messageStoreClient.getPrerenceManager().getOasisServerRoot(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        setMethod(HttpRequestParams.Method.GET);
        setUrl(str2);
        initMcsCommonRequestHeaders(str3, this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer(), str);
        setHeaders(this.mNMSRequestHeaderMap);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int statusCode = httpResponseParams.getStatusCode();
                String r1 = CloudMessageGetLargeFile.this.TAG;
                Log.i(r1, "onComplete: status Code " + statusCode);
                if (statusCode == 200 || statusCode == 206) {
                    String r3 = CloudMessageGetLargeFile.this.TAG;
                    Log.i(r3, "contentType : " + httpResponseParams.getHeaders().get("content-type") + " contentLength: " + httpResponseParams.getHeaders().get("content-length") + " contentRange " + httpResponseParams.getHeaders().get("content-range"));
                    if (httpResponseParams.getDataBinary() != null) {
                        LargeFileDownloadParams largeFileDownloadParams = new LargeFileDownloadParams();
                        largeFileDownloadParams.strbody = httpResponseParams.getDataBinary();
                        iAPICallFlowListener.onGoToEvent(OMASyncEventType.DOWNLOADED_PART.getId(), largeFileDownloadParams);
                        return;
                    }
                }
                if (CloudMessageGetLargeFile.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    iAPICallFlowListener.onGoToEvent(OMASyncEventType.DOWNLOAD_FILE_API_FAILED.getId(), (Object) null);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageGetLargeFile.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                iAPICallFlowListener.onFailedCall(this);
            }
        });
    }
}
