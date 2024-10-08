package com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetAllPayloads;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetIndividualObject;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.log.IMSLog;

public class VvmDataChangeHandler extends BaseDataChangeHandler {
    private String TAG = VvmDataChangeHandler.class.getSimpleName();

    public VvmDataChangeHandler(Looper looper, MessageStoreClient messageStoreClient, INetAPIEventListener iNetAPIEventListener, IUIEventCallback iUIEventCallback, String str, SyncMsgType syncMsgType, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper, messageStoreClient, iNetAPIEventListener, iUIEventCallback, str, syncMsgType, iCloudMessageManagerHelper);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
    }

    public void setWorkingQueue(BufferDBChangeParam bufferDBChangeParam) {
        this.mWorkingQueue.offer(bufferDBChangeParam);
        String str = this.TAG;
        Log.i(str, "setWorkingQueue: message type: " + bufferDBChangeParam.mDBIndex + ", size : " + this.mWorkingQueue.size());
    }

    /* access modifiers changed from: protected */
    public Pair<HttpRequestParams, Boolean> peekWorkingQueue() {
        BufferDBChangeParam peek = this.mWorkingQueue.peek();
        String str = this.TAG;
        Log.i(str, "peekWorkingQueue: " + peek);
        if (peek == null) {
            return null;
        }
        int i = peek.mDBIndex;
        if (i == 17) {
            String vVMObjectIdFromBufDb = this.mBufferDBTranslation.getVVMObjectIdFromBufDb(peek);
            String vVMpayLoadUrlFromBufDb = this.mBufferDBTranslation.getVVMpayLoadUrlFromBufDb(peek);
            String str2 = this.TAG;
            Log.d(str2, "downloadNextMsgFromQueueObject: objectId: " + vVMObjectIdFromBufDb + " payloadUrl: " + IMSLog.checker(vVMpayLoadUrlFromBufDb));
            if (TextUtils.isEmpty(vVMpayLoadUrlFromBufDb)) {
                return new Pair<>(new CloudMessageGetIndividualObject(this, vVMObjectIdFromBufDb, peek, this.mStoreClient), Boolean.FALSE);
            }
            return new Pair<>(CloudMessageGetAllPayloads.buildFromPayloadUrl(this, vVMpayLoadUrlFromBufDb, peek, this.mStoreClient), Boolean.FALSE);
        } else if (i == 18) {
            return new Pair<>(CloudMessageGetAllPayloads.buildFromPayloadUrl(this, this.mBufferDBTranslation.getVVMGreetingpayLoadUrlFromBufDb(peek), peek, this.mStoreClient), Boolean.FALSE);
        } else {
            if (i == 7) {
                return new Pair<>(new CloudMessageGetIndividualObject(this, this.mBufferDBTranslation.getSummaryObjectIdFromBufDb(peek), peek, this.mStoreClient), Boolean.FALSE);
            }
            return null;
        }
    }
}
