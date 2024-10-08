package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmFolders;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nc.data.NotificationList;
import com.sec.internal.omanetapi.nms.data.BulkResponseList;
import com.sec.internal.omanetapi.nms.data.NmsEventList;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import com.sec.internal.omanetapi.nms.data.Reference;
import java.util.List;
import javax.mail.BodyPart;

public class ParamOMAresponseforBufDB {
    /* access modifiers changed from: private */
    public ActionType mActionType;
    /* access modifiers changed from: private */
    public List<BodyPart> mAllPayloads;
    /* access modifiers changed from: private */
    public BufferDBChangeParam mBufferDbParam;
    /* access modifiers changed from: private */
    public BufferDBChangeParamList mBufferDbParamList;
    /* access modifiers changed from: private */
    public BulkResponseList mBulkResponseList;
    /* access modifiers changed from: private */
    public byte[] mDataString;
    /* access modifiers changed from: private */
    public String mFilePath;
    /* access modifiers changed from: private */
    public boolean mIsFullSync;
    /* access modifiers changed from: private */
    public String mLine;
    /* access modifiers changed from: private */
    public NmsEventList mMcsNmsEventList;
    /* access modifiers changed from: private */
    public NotificationList[] mNotificaitonList;
    /* access modifiers changed from: private */
    public OMASyncEventType mOMASyncEventType;
    /* access modifiers changed from: private */
    public Object mObject;
    /* access modifiers changed from: private */
    public ObjectList mObjectList;
    /* access modifiers changed from: private */
    public String mPayLoadUrl;
    /* access modifiers changed from: private */
    public String mReasonPhrase;
    /* access modifiers changed from: private */
    public Reference mReference;
    /* access modifiers changed from: private */
    public String mSearchCursor;
    /* access modifiers changed from: private */
    public MessageStoreClient mStoreClient;
    /* access modifiers changed from: private */
    public SyncMsgType mType;
    /* access modifiers changed from: private */
    public VvmFolders mVvmFolders;
    /* access modifiers changed from: private */
    public VvmServiceProfile mVvmServiceProfile;

    public enum ActionType {
        INIT_SYNC_COMPLETE,
        INIT_SYNC_SUMMARY_COMPLETE,
        INIT_SYNC_PARTIAL_SYNC_SUMMARY,
        MATCH_DB,
        SYNC_FAILED,
        ONE_MESSAGE_DOWNLOAD,
        ONE_PAYLOAD_DOWNLOAD,
        ALL_PAYLOAD_DOWNLOAD,
        MESSAGE_DOWNLOAD_COMPLETE,
        ONE_MESSAGE_UPLOADED,
        MESSAGE_UPLOAD_COMPLETE,
        NOTIFICATION_OBJECT_DOWNLOADED,
        NOTIFICATION_PAYLOAD_DOWNLOADED,
        NOTIFICATION_ALL_PAYLOAD_DOWNLOADED,
        NOTIFICATION_OBJECTS_DOWNLOAD_COMPLETE,
        MAILBOX_RESET,
        CLOUD_OBJECT_UPDATE,
        RECEIVE_NOTIFICATION,
        OBJECT_FLAG_UPDATED,
        OBJECT_DELETE_UPDATE_FAILED,
        OBJECT_READ_UPDATE_FAILED,
        OBJECT_FLAGS_UPDATE_COMPLETE,
        OBJECT_FLAGS_BULK_UPDATE_COMPLETE,
        OBJECT_NOT_FOUND,
        VVM_FAX_ERROR_WITH_NO_RETRY,
        VVM_PROFILE_DOWNLOADED,
        VVM_QUOTA_INFO,
        BULK_MESSAGES_UPLOADED,
        FALLBACK_MESSAGES_UPLOADED,
        VVM_NORMAL_SYNC_SUMMARY_COMPLETE,
        VVM_NORMAL_SYNC_SUMMARY_PARTIAL,
        ALL_PAYLOAD_NOTIFY,
        NOTIFICATION_IMDN_DOWNLOADED,
        INIT_UPLOAD_STARTED,
        ADHOC_PAYLOAD_DOWNLOAD_FAILED
    }

    private ParamOMAresponseforBufDB() {
    }

    public static class Builder {
        private ParamOMAresponseforBufDB mInstance = new ParamOMAresponseforBufDB();

        public Builder setActionType(ActionType actionType) {
            this.mInstance.mActionType = actionType;
            return this;
        }

        public Builder setLine(String str) {
            this.mInstance.mLine = str;
            return this;
        }

        public Builder setObjectList(ObjectList objectList) {
            this.mInstance.mObjectList = objectList;
            return this;
        }

        public Builder setReference(Reference reference) {
            this.mInstance.mReference = reference;
            return this;
        }

        public Builder setBufferDBChangeParam(BufferDBChangeParam bufferDBChangeParam) {
            this.mInstance.mBufferDbParam = bufferDBChangeParam;
            return this;
        }

        public Builder setBufferDBChangeParam(BufferDBChangeParamList bufferDBChangeParamList) {
            this.mInstance.mBufferDbParamList = bufferDBChangeParamList;
            return this;
        }

        public Builder setNotificationList(NotificationList[] notificationListArr) {
            this.mInstance.mNotificaitonList = notificationListArr;
            return this;
        }

        public Builder setMcsNmsEventList(NmsEventList nmsEventList) {
            this.mInstance.mMcsNmsEventList = nmsEventList;
            return this;
        }

        public Builder setObject(Object object) {
            this.mInstance.mObject = object;
            return this;
        }

        public Builder setVvmServiceProfile(VvmServiceProfile vvmServiceProfile) {
            this.mInstance.mVvmServiceProfile = vvmServiceProfile;
            return this;
        }

        public Builder setVvmFolders(VvmFolders vvmFolders) {
            this.mInstance.mVvmFolders = vvmFolders;
            return this;
        }

        public Builder setByte(byte[] bArr) {
            this.mInstance.mDataString = bArr;
            return this;
        }

        public Builder setPayloadUrl(String str) {
            this.mInstance.mPayLoadUrl = str;
            return this;
        }

        public Builder setCursor(String str) {
            this.mInstance.mSearchCursor = str;
            return this;
        }

        public Builder setAllPayloads(List<BodyPart> list) {
            this.mInstance.mAllPayloads = list;
            return this;
        }

        public Builder setOMASyncEventType(OMASyncEventType oMASyncEventType) {
            this.mInstance.mOMASyncEventType = oMASyncEventType;
            return this;
        }

        public Builder setSyncType(SyncMsgType syncMsgType) {
            this.mInstance.mType = syncMsgType;
            return this;
        }

        public Builder setBulkResponseList(BulkResponseList bulkResponseList) {
            this.mInstance.mBulkResponseList = bulkResponseList;
            return this;
        }

        public Builder setReasonPhrase(String str) {
            this.mInstance.mReasonPhrase = str;
            return this;
        }

        public Builder setMStoreClient(MessageStoreClient messageStoreClient) {
            this.mInstance.mStoreClient = messageStoreClient;
            return this;
        }

        public Builder setFilePath(String str) {
            this.mInstance.mFilePath = str;
            return this;
        }

        public ParamOMAresponseforBufDB build() {
            if (!(this.mInstance.mOMASyncEventType == null || this.mInstance.mStoreClient == null)) {
                this.mInstance.mStoreClient.getPrerenceManager().saveInitialSyncStatus(this.mInstance.mOMASyncEventType.getId());
            }
            return this.mInstance;
        }

        public Builder setIsFullSync(boolean z) {
            this.mInstance.mIsFullSync = z;
            return this;
        }
    }

    public ActionType getActionType() {
        return this.mActionType;
    }

    public String getLine() {
        return this.mLine;
    }

    public ObjectList getObjectList() {
        return this.mObjectList;
    }

    public Reference getReference() {
        return this.mReference;
    }

    public BufferDBChangeParam getBufferDBChangeParam() {
        return this.mBufferDbParam;
    }

    public BufferDBChangeParamList getBufferDBChangeParamList() {
        return this.mBufferDbParamList;
    }

    public Object getObject() {
        return this.mObject;
    }

    public NotificationList[] getNotificationList() {
        return this.mNotificaitonList;
    }

    public NmsEventList getMcsNmsEventList() {
        return this.mMcsNmsEventList;
    }

    public byte[] getData() {
        return this.mDataString;
    }

    public String getSearchCursor() {
        return this.mSearchCursor;
    }

    public OMASyncEventType getOMASyncEventType() {
        return this.mOMASyncEventType;
    }

    public List<BodyPart> getAllPayloads() {
        return this.mAllPayloads;
    }

    public SyncMsgType getSyncMsgType() {
        return this.mType;
    }

    public BulkResponseList getBulkResponseList() {
        return this.mBulkResponseList;
    }

    public VvmServiceProfile getVvmServiceProfile() {
        return this.mVvmServiceProfile;
    }

    public VvmFolders getVvmFolders() {
        return this.mVvmFolders;
    }

    public String getReasonPhrase() {
        return this.mReasonPhrase;
    }

    public boolean getIsFullSync() {
        return this.mIsFullSync;
    }

    public String getFilePath() {
        return this.mFilePath;
    }

    public String getPayloadUrl() {
        return this.mPayLoadUrl;
    }

    public String toString() {
        return " mActionType: " + this.mActionType + " mLine: " + IMSLog.checker(this.mLine) + " mReference: " + this.mReference + " mBufferDbParam: " + this.mBufferDbParam + " mPayLoadUrl: " + IMSLog.checker(this.mPayLoadUrl) + " mSearchCursor: " + this.mSearchCursor + " mOMASyncEventType: " + this.mOMASyncEventType + " mType: " + this.mType + " mIsFullSync: " + this.mIsFullSync;
    }
}
