package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.GsonBuilder;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.nms.data.GsonInterfaceAdapter;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamObjectUpload;
import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nms.BaseNMSRequest;
import com.sec.internal.omanetapi.nms.data.Attribute;
import com.sec.internal.omanetapi.nms.data.Object;
import java.io.IOException;

public class McsPostGroupSMS extends BaseNMSRequest {
    /* access modifiers changed from: private */
    public String TAG = McsPostGroupSMS.class.getSimpleName();
    private MessageStoreClient mStoreClient;

    public McsPostGroupSMS(final IAPICallFlowListener iAPICallFlowListener, ParamObjectUpload paramObjectUpload, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName(), paramObjectUpload.bufferDbParam.mLine, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mStoreClient = messageStoreClient;
        buildAPISpecificURLFromBase();
        initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        initPostRequest((Object) paramObjectUpload.uploadObjectInfo.first, true);
        final BufferDBChangeParam bufferDBChangeParam = paramObjectUpload.bufferDbParam;
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String r0 = McsPostGroupSMS.this.TAG;
                Log.i(r0, "onComplete status  " + httpResponseParams.getStatusCode());
                if (httpResponseParams.getStatusCode() == 201) {
                    OMAApiResponseParam response = McsPostGroupSMS.this.getResponse(httpResponseParams);
                    if (response == null) {
                        iAPICallFlowListener.onFailedCall(this);
                        return;
                    }
                    iAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.OBJECT_ONE_UPLOAD_COMPLETED.getId(), new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.ONE_MESSAGE_UPLOADED).setObject(response.object).setBufferDBChangeParam(bufferDBChangeParam).build());
                } else if (McsPostGroupSMS.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    iAPICallFlowListener.onMoveOnToNext(McsPostGroupSMS.this, (Object) null);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = McsPostGroupSMS.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                if (iOException.getMessage().equals(EucTestIntent.Extras.TIMEOUT)) {
                    iAPICallFlowListener.onMoveOnToNext(McsPostGroupSMS.this, (Object) null);
                } else {
                    iAPICallFlowListener.onFailedCall(this);
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath("groupSMS").appendPath("objects");
        this.mBaseUrl = buildUpon.build().toString();
    }

    public void initPostRequest(Object object, boolean z) {
        String str;
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        if (z) {
            OMAApiRequestParam.AllObjectRequest allObjectRequest = new OMAApiRequestParam.AllObjectRequest();
            allObjectRequest.object = object;
            GsonBuilder gsonBuilder = new GsonBuilder();
            Class<Attribute> cls = Attribute.class;
            gsonBuilder.registerTypeAdapter(cls, new GsonInterfaceAdapter(cls));
            str = gsonBuilder.disableHtmlEscaping().setPrettyPrinting().create().toJson(allObjectRequest);
        } else {
            str = "";
        }
        this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
        setPostBody(new HttpPostBody(str));
        setHeaders(this.mNMSRequestHeaderMap);
    }
}
