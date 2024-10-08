package com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler;

import android.os.Message;
import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.appapi.McsAppRequest;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamAppResponseObject;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.log.IMSLog;

public class AppRequestHandler implements IAPICallFlowListener {
    private String TAG = AppRequestHandler.class.getSimpleName();
    private final MessageStoreClient mStoreClient;
    private IBufferDBEventListener mcsCallback;

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam) {
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam, SyncMsgType syncMsgType, int i) {
    }

    public void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
    }

    public void onFailedEvent(int i, Object obj) {
    }

    public void onFixedFlow(int i) {
    }

    public void onGoToEvent(int i, Object obj) {
    }

    public void onMoveOnToNext(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i) {
    }

    public void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i, Object obj) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str) {
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface iHttpAPICommonInterface, int i, Object obj) {
    }

    public AppRequestHandler(MessageStoreClient messageStoreClient, IBufferDBEventListener iBufferDBEventListener) {
        this.mStoreClient = messageStoreClient;
        this.mcsCallback = iBufferDBEventListener;
        this.TAG = "[" + messageStoreClient.getClientID() + "]";
    }

    public void processAppRequest(String str, String str2, int i) {
        if (!this.mStoreClient.getProvisionStatus() || !CmsUtil.isMcsSupported(this.mStoreClient.getContext(), this.mStoreClient.getClientID()) || TextUtils.isEmpty(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer())) {
            IMSLog.i(this.TAG, "processAppRequest, provision not completed");
        } else {
            this.mStoreClient.getHttpController().execute(new McsAppRequest(this.mStoreClient, this, str, str2, i));
        }
    }

    public void onFixedFlowWithMessage(Message message) {
        ParamAppResponseObject paramAppResponseObject = (ParamAppResponseObject) message.obj;
        String str = this.TAG;
        IMSLog.i(str, "onFixedFlowWithMessage code:" + paramAppResponseObject.errorCode + " body:" + paramAppResponseObject.jsonResult);
        this.mcsCallback.notifyAppOperationResult(paramAppResponseObject.jsonResult, paramAppResponseObject.errorCode);
    }
}
