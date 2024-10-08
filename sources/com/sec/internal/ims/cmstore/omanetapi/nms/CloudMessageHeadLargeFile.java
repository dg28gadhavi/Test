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
import java.util.Iterator;

public class CloudMessageHeadLargeFile extends BaseFileRequest {
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageHeadLargeFile.class.getSimpleName();

    public CloudMessageHeadLargeFile(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, String str) {
        super(messageStoreClient.getPrerenceManager().getOasisServerRoot(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        setMethod(HttpRequestParams.Method.HEAD);
        setUrl(str);
        initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        setHeaders(this.mNMSRequestHeaderMap);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String r0 = CloudMessageHeadLargeFile.this.TAG;
                Log.i(r0, "onComplete: status Code " + httpResponseParams.getStatusCode());
                if (httpResponseParams.getStatusCode() == 200 || httpResponseParams.getStatusCode() == 206) {
                    if (httpResponseParams.getHeaders() == null || httpResponseParams.getHeaders().isEmpty()) {
                        Log.i(CloudMessageHeadLargeFile.this.TAG, "onComplete:result.getHeaders().isEmpty()");
                    } else {
                        LargeFileDownloadParams largeFileDownloadParams = new LargeFileDownloadParams();
                        CloudMessageHeadLargeFile.this.parseResponseHeaders(httpResponseParams, largeFileDownloadParams);
                        iAPICallFlowListener.onGoToEvent(OMASyncEventType.DOWNLOAD_FILE_HEAD_COMPLETED.getId(), largeFileDownloadParams);
                        return;
                    }
                }
                if (CloudMessageHeadLargeFile.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    iAPICallFlowListener.onGoToEvent(OMASyncEventType.DOWNLOAD_FILE_API_FAILED.getId(), (Object) null);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageHeadLargeFile.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                iAPICallFlowListener.onFailedCall(this);
            }
        });
    }

    /* access modifiers changed from: private */
    public void parseResponseHeaders(HttpResponseParams httpResponseParams, LargeFileDownloadParams largeFileDownloadParams) {
        Iterator it = httpResponseParams.getHeaders().get("content-type").iterator();
        if (it.hasNext()) {
            largeFileDownloadParams.contentType = (String) it.next();
        }
        Iterator it2 = httpResponseParams.getHeaders().get("content-length").iterator();
        if (it2.hasNext()) {
            largeFileDownloadParams.contentLength = (String) it2.next();
        }
        Iterator it3 = httpResponseParams.getHeaders().get("content-disposition").iterator();
        if (it3.hasNext()) {
            largeFileDownloadParams.contentDisposition = (String) it3.next();
        }
        Iterator it4 = httpResponseParams.getHeaders().get("accept-ranges").iterator();
        if (it4.hasNext()) {
            largeFileDownloadParams.acceptRanges = (String) it4.next();
        }
        String str = this.TAG;
        Log.i(str, "parseResponseHeaders: " + largeFileDownloadParams.toString());
    }
}
