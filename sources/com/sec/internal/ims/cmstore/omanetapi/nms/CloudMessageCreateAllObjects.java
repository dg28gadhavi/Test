package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import android.util.Pair;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamObjectUpload;
import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nms.AllObjects;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.Reference;
import java.io.IOException;

public class CloudMessageCreateAllObjects extends AllObjects {
    private static final long serialVersionUID = 3193513166884750667L;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageCreateAllObjects.class.getSimpleName();

    public CloudMessageCreateAllObjects(IAPICallFlowListener iAPICallFlowListener, ParamObjectUpload paramObjectUpload, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName(), paramObjectUpload.bufferDbParam.mLine, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        Pair<Object, HttpPostBody> pair = paramObjectUpload.uploadObjectInfo;
        final Object object = (Object) pair.first;
        HttpPostBody httpPostBody = (HttpPostBody) pair.second;
        String validTokenByLine = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(paramObjectUpload.bufferDbParam.mLine);
        if (this.isCmsMcsEnabled) {
            initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        } else {
            initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), validTokenByLine);
        }
        initPostRequest(object, true, httpPostBody);
        final BufferDBChangeParam bufferDBChangeParam = paramObjectUpload.bufferDbParam;
        final IAPICallFlowListener iAPICallFlowListener2 = iAPICallFlowListener;
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                ParamOMAresponseforBufDB paramOMAresponseforBufDB;
                String r0 = CloudMessageCreateAllObjects.this.TAG;
                Log.i(r0, "onComplete status  " + httpResponseParams.getStatusCode());
                if (httpResponseParams.getStatusCode() == 201) {
                    OMAApiResponseParam response = CloudMessageCreateAllObjects.this.getResponse(httpResponseParams);
                    if (response == null || (!CloudMessageCreateAllObjects.this.isCmsMcsEnabled && response.reference == null)) {
                        iAPICallFlowListener2.onFailedCall(this, bufferDBChangeParam);
                        return;
                    }
                    Reference reference = response.reference;
                    if (CloudMessageCreateAllObjects.this.isCmsMcsEnabled && reference == null) {
                        reference = new Reference();
                        Object object = response.object;
                        reference.path = object.path;
                        reference.resourceURL = object.resourceURL;
                    }
                    paramOMAresponseforBufDB = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.ONE_MESSAGE_UPLOADED).setReference(reference).setObject(object).setBufferDBChangeParam(bufferDBChangeParam).build();
                } else if (httpResponseParams.getStatusCode() != 401) {
                    paramOMAresponseforBufDB = null;
                } else if (!CloudMessageCreateAllObjects.this.handleUnAuthorized(httpResponseParams)) {
                    paramOMAresponseforBufDB = new ParamOMAresponseforBufDB.Builder().setBufferDBChangeParam(bufferDBChangeParam).setLine(CloudMessageCreateAllObjects.this.getBoxId()).build();
                } else {
                    return;
                }
                if (CloudMessageCreateAllObjects.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener2, httpResponseParams, paramOMAresponseforBufDB, bufferDBChangeParam, Integer.MIN_VALUE)) {
                    iAPICallFlowListener2.onMoveOnToNext(CloudMessageCreateAllObjects.this, paramOMAresponseforBufDB);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageCreateAllObjects.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                if (iOException.getMessage().equals(EucTestIntent.Extras.TIMEOUT)) {
                    iAPICallFlowListener2.onMoveOnToNext(CloudMessageCreateAllObjects.this, (Object) null);
                } else if (CloudMessageCreateAllObjects.this.isCmsMcsEnabled) {
                    iAPICallFlowListener2.onFailedCall(this);
                } else {
                    iAPICallFlowListener2.onFailedCall(this, bufferDBChangeParam);
                }
            }
        });
    }
}
