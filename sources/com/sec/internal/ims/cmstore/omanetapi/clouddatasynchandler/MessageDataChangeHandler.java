package com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetAllPayloads;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetIndividualObject;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageGetIndividualPayLoad;
import com.sec.internal.ims.cmstore.omanetapi.nms.McsGetChatImdns;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;
import java.util.List;

public class MessageDataChangeHandler extends BaseDataChangeHandler {
    private String TAG = MessageDataChangeHandler.class.getSimpleName();

    public MessageDataChangeHandler(Looper looper, MessageStoreClient messageStoreClient, INetAPIEventListener iNetAPIEventListener, IUIEventCallback iUIEventCallback, String str, SyncMsgType syncMsgType, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
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
        if (i == 3) {
            return new Pair<>(new CloudMessageGetIndividualObject(this, this.mBufferDBTranslation.getSmsObjectIdFromBufDb(peek), peek, this.mStoreClient), Boolean.FALSE);
        }
        if (i == 4) {
            Pair<String, List<String>> objectIdPartIdFromMmsBufDb = this.mBufferDBTranslation.getObjectIdPartIdFromMmsBufDb(peek);
            if (this.isCmsEnabled) {
                Log.i(this.TAG, "Download MMS All payload");
                return new Pair<>(CloudMessageGetAllPayloads.buildFromPayloadUrl(this, this.mBufferDBTranslation.getPayloadUrlfromMmsPduBufferId(peek), peek, this.mStoreClient), Boolean.FALSE);
            }
            List<String> payloadPartUrlFromMmsBufDb = this.mBufferDBTranslation.getPayloadPartUrlFromMmsBufDb(peek);
            String str2 = (String) objectIdPartIdFromMmsBufDb.first;
            if (payloadPartUrlFromMmsBufDb != null && payloadPartUrlFromMmsBufDb.size() > 0) {
                String str3 = this.TAG;
                Log.i(str3, "setWorkingQueue payloadUrls size: " + payloadPartUrlFromMmsBufDb.size());
                Iterator<String> it = payloadPartUrlFromMmsBufDb.iterator();
                if (it.hasNext()) {
                    return new Pair<>(CloudMessageGetIndividualPayLoad.buildFromPayloadUrl(this, it.next(), peek, this.mStoreClient), Boolean.FALSE);
                }
                return null;
            } else if (!TextUtils.isEmpty(str2)) {
                return new Pair<>(new CloudMessageGetIndividualObject(this, str2, peek, this.mStoreClient), Boolean.FALSE);
            } else {
                return null;
            }
        } else if (i == 6) {
            String payloadPartUrlFromMmsPartUsingPartBufferId = this.mBufferDBTranslation.getPayloadPartUrlFromMmsPartUsingPartBufferId(peek);
            if (!TextUtils.isEmpty(payloadPartUrlFromMmsPartUsingPartBufferId)) {
                return new Pair<>(CloudMessageGetIndividualPayLoad.buildFromPayloadUrl(this, payloadPartUrlFromMmsPartUsingPartBufferId, peek, this.mStoreClient), Boolean.FALSE);
            }
            return null;
        } else if (i == 1) {
            Pair<String, List<String>> objectIdPartIdFromRCSBufDb = this.mBufferDBTranslation.getObjectIdPartIdFromRCSBufDb(peek);
            Pair<String, String> payloadPartandAllPayloadUrlFromRCSBufDb = this.mBufferDBTranslation.getPayloadPartandAllPayloadUrlFromRCSBufDb(peek);
            if (this.isCmsEnabled && !peek.mIsDownloadRequestFromApp) {
                if (this.mIsFTThumbnailDownload) {
                    this.mIsFTThumbnailDownload = false;
                } else {
                    payloadPartandAllPayloadUrlFromRCSBufDb = this.mBufferDBTranslation.getAllPayloadUrlFromRCSBufDb(peek);
                    if (peek.mIsFTThumbnail) {
                        this.mIsFTThumbnailDownload = true;
                    }
                    String str4 = this.TAG;
                    Log.i(str4, "param.mPayloadThumbnailUrl : " + peek.mPayloadThumbnailUrl + ", mIsFTThumbnailDownload = " + this.mIsFTThumbnailDownload);
                }
            }
            String str5 = (String) payloadPartandAllPayloadUrlFromRCSBufDb.first;
            String str6 = (String) payloadPartandAllPayloadUrlFromRCSBufDb.second;
            String str7 = (String) objectIdPartIdFromRCSBufDb.first;
            String str8 = this.TAG;
            Log.d(str8, "payloadpartUrl: " + str5 + " payloadUrl: " + str6 + " objId: " + str7);
            if (this.mIsFTThumbnailDownload) {
                return getPayloadRequestOrDownload(peek, peek.mPayloadThumbnailUrl, true);
            }
            if (!TextUtils.isEmpty(str5)) {
                return getPayloadRequestOrDownload(peek, str5, true);
            }
            if (!TextUtils.isEmpty(str6)) {
                return getPayloadRequestOrDownload(peek, str6, false);
            }
            if (!TextUtils.isEmpty(str7)) {
                return new Pair<>(new CloudMessageGetIndividualObject(this, str7, peek, this.mStoreClient), Boolean.FALSE);
            }
            return null;
        } else if (i == 7) {
            return new Pair<>(new CloudMessageGetIndividualObject(this, this.mBufferDBTranslation.getSummaryObjectIdFromBufDb(peek), peek, this.mStoreClient), Boolean.FALSE);
        } else {
            if (i != 13) {
                return null;
            }
            String imdnResUrl = this.mBufferDBTranslation.getImdnResUrl(peek.mRowId);
            if (!TextUtils.isEmpty(imdnResUrl)) {
                return new Pair<>(new McsGetChatImdns(this, imdnResUrl, peek, this.mStoreClient), Boolean.FALSE);
            }
            IMSLog.e(this.TAG, "resUrl of Imdn is empty!");
            return null;
        }
    }

    private Pair<HttpRequestParams, Boolean> getPayloadRequestOrDownload(BufferDBChangeParam bufferDBChangeParam, String str, boolean z) {
        if (this.isCmsEnabled) {
            if (!bufferDBChangeParam.mIsDownloadRequestFromApp) {
                if (this.mBufferDBTranslation.needToSkipDownloadLargeFileAndUpdateDB(bufferDBChangeParam.mRowId, CloudMessageBufferDBConstants.ActionStatusFlag.None.getId(), CloudMessageBufferDBConstants.DirectionFlag.Done.getId(), str, this.mIsFTThumbnailDownload)) {
                    Log.i(this.TAG, "getPayloadRequestOrDownload large file download payload skipped");
                    return null;
                } else if (CmsUtil.urlContainsLargeFile(this.mStoreClient, str)) {
                    return downloadPayloadFromFileDownloadHandler(bufferDBChangeParam, str);
                }
            } else if (CmsUtil.urlContainsLargeFile(this.mStoreClient, str)) {
                return downloadPayloadFromFileDownloadHandler(bufferDBChangeParam, str);
            }
        }
        return getPayloadRequestBasedOnUrl(bufferDBChangeParam, str, z);
    }

    private Pair<HttpRequestParams, Boolean> getPayloadRequestBasedOnUrl(BufferDBChangeParam bufferDBChangeParam, String str, boolean z) {
        if (z) {
            return new Pair<>(CloudMessageGetIndividualPayLoad.buildFromPayloadUrl(this, str, bufferDBChangeParam, this.mStoreClient), Boolean.FALSE);
        }
        return new Pair<>(CloudMessageGetAllPayloads.buildFromPayloadUrl(this, str, bufferDBChangeParam, this.mStoreClient), Boolean.FALSE);
    }

    private Pair<HttpRequestParams, Boolean> downloadPayloadFromFileDownloadHandler(BufferDBChangeParam bufferDBChangeParam, String str) {
        this.mFileDownloadHandler.start(str, bufferDBChangeParam);
        return new Pair<>((Object) null, Boolean.TRUE);
    }
}
