package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import android.util.Pair;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.ParamBulkCreation;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nms.BulkCreation;
import com.sec.internal.omanetapi.nms.data.BulkResponseList;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import java.io.IOException;
import java.util.List;

public class CloudMessageBulkCreation extends BulkCreation {
    private static final long serialVersionUID = 3193513166884750667L;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageBulkCreation.class.getSimpleName();

    public CloudMessageBulkCreation(IAPICallFlowListener iAPICallFlowListener, ParamBulkCreation paramBulkCreation, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName(), paramBulkCreation.mLine, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        Pair<ObjectList, List<HttpPostBody>> pair = paramBulkCreation.uploadObjectInfo;
        final ObjectList objectList = (ObjectList) pair.first;
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(paramBulkCreation.mLine));
        initPostRequest(objectList, true, (List) pair.second);
        setWriteTimeout(300000);
        final BufferDBChangeParamList bufferDBChangeParamList = paramBulkCreation.bufferDbParamList;
        final IAPICallFlowListener iAPICallFlowListener2 = iAPICallFlowListener;
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                ParamOMAresponseforBufDB paramOMAresponseforBufDB;
                if (httpResponseParams.getStatusCode() == 200) {
                    OMAApiResponseParam response = CloudMessageBulkCreation.this.getResponse(httpResponseParams);
                    if (response != null) {
                        paramOMAresponseforBufDB = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.BULK_MESSAGES_UPLOADED).setObjectList(objectList).setBufferDBChangeParam(bufferDBChangeParamList).setBulkResponseList(response.bulkResponseList).build();
                    } else {
                        return;
                    }
                } else if (httpResponseParams.getStatusCode() == 401) {
                    if (!CloudMessageBulkCreation.this.handleUnAuthorized(httpResponseParams)) {
                        paramOMAresponseforBufDB = new ParamOMAresponseforBufDB.Builder().setBufferDBChangeParam(bufferDBChangeParamList).setLine(CloudMessageBulkCreation.this.getBoxId()).build();
                    } else {
                        return;
                    }
                } else if (httpResponseParams.getStatusCode() == 403 || httpResponseParams.getStatusCode() == 503) {
                    iAPICallFlowListener2.onMoveOnToNext(CloudMessageBulkCreation.this, new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.FALLBACK_MESSAGES_UPLOADED).setObjectList(objectList).setBufferDBChangeParam(bufferDBChangeParamList).setBulkResponseList((BulkResponseList) null).build());
                    return;
                } else {
                    paramOMAresponseforBufDB = null;
                }
                if (CloudMessageBulkCreation.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener2, httpResponseParams, paramOMAresponseforBufDB, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    iAPICallFlowListener2.onMoveOnToNext(CloudMessageBulkCreation.this, paramOMAresponseforBufDB);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageBulkCreation.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                if (iOException.getMessage().equals(EucTestIntent.Extras.TIMEOUT)) {
                    iAPICallFlowListener2.onMoveOnToNext(CloudMessageBulkCreation.this, new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.FALLBACK_MESSAGES_UPLOADED).setObjectList(objectList).setBufferDBChangeParam(bufferDBChangeParamList).setBulkResponseList((BulkResponseList) null).build());
                    return;
                }
                iAPICallFlowListener2.onFailedCall(this);
            }
        });
    }
}
