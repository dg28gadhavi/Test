package com.sec.internal.constants.ims.cmstore;

public class CloudMessageBufferDBConstants {

    public enum CloudResponseFlag {
        Inserted,
        SetRead,
        SetDelete
    }

    public enum InitialSyncStatusFlag {
        START,
        FINISHED,
        FAIL,
        IGNORED
    }

    public enum MsgOperationFlag {
        Receiving,
        Received,
        Sending,
        Sent,
        Read,
        UnRead,
        Delete,
        Cancel,
        Starred,
        UnStarred,
        SendFail,
        Upload,
        Download,
        StartFullSync,
        StopSync,
        WipeOut,
        StartDeltaSync
    }

    public enum PayloadEncoding {
        None(0),
        Base64(1);
        
        private final int mId;

        private PayloadEncoding(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    public enum ActionStatusFlag {
        None(0),
        Insert(1),
        Update(2),
        Delete(3),
        Deleted(4),
        DownLoad(5),
        FetchUri(6),
        FetchIndividualUri(7),
        UpdatePayload(8),
        Cancel(9),
        Starred(10),
        UnStarred(11),
        FetchForce(12);
        
        private final int mId;

        private ActionStatusFlag(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }

        public static ActionStatusFlag valueOf(int i) {
            for (ActionStatusFlag actionStatusFlag : values()) {
                if (actionStatusFlag.mId == i) {
                    return actionStatusFlag;
                }
            }
            return null;
        }
    }

    public enum DirectionFlag {
        Done(0),
        ToSendCloud(1),
        ToSendDevice(2),
        NmsEvent(3),
        UpdatingCloud(4),
        UpdatingDevice(5),
        Downloading(6),
        FetchingFail(7);
        
        private final int mId;

        private DirectionFlag(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }

        public static DirectionFlag valueOf(int i) {
            for (DirectionFlag directionFlag : values()) {
                if (directionFlag.mId == i) {
                    return directionFlag;
                }
            }
            return null;
        }
    }

    public enum UploadStatusFlag {
        FAILURE(0),
        SUCCESS(1),
        PENDING(2);
        
        private final int mId;

        private UploadStatusFlag(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    public enum FaxDeliveryStatus {
        FAILURE(0),
        DELIVERED(1),
        PENDING(2);
        
        private final int mId;

        private FaxDeliveryStatus(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }
}
