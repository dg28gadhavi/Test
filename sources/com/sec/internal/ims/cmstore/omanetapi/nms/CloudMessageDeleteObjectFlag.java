package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.nms.IndividualFlag;
import java.io.IOException;

public class CloudMessageDeleteObjectFlag extends IndividualFlag {
    private static final long serialVersionUID = 8158555957984259234L;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageDeleteObjectFlag.class.getSimpleName();

    public CloudMessageDeleteObjectFlag(final IAPICallFlowListener iAPICallFlowListener, String str, String str2, final BufferDBChangeParam bufferDBChangeParam, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName(), bufferDBChangeParam.mLine, str, str2, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(bufferDBChangeParam.mLine));
        initDeleteRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                if (httpResponseParams.getStatusCode() != 401) {
                    httpResponseParams.setStatusCode(204);
                } else if (CloudMessageDeleteObjectFlag.this.handleUnAuthorized(httpResponseParams)) {
                    return;
                }
                ParamOMAresponseforBufDB build = httpResponseParams.getStatusCode() == 204 ? new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_UPDATE_COMPLETE).setBufferDBChangeParam(bufferDBChangeParam).build() : null;
                if (CloudMessageDeleteObjectFlag.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEnableATTHeader() || !CloudMessageDeleteObjectFlag.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryRequired(httpResponseParams.getStatusCode())) {
                    if (CloudMessageDeleteObjectFlag.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, build, bufferDBChangeParam, Integer.MIN_VALUE)) {
                        iAPICallFlowListener.onMoveOnToNext(CloudMessageDeleteObjectFlag.this, build);
                        return;
                    }
                    return;
                }
                iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam, SyncMsgType.VM, httpResponseParams.getStatusCode());
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageDeleteObjectFlag.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
            }
        });
    }
}
