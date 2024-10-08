package com.sec.internal.constants.ims.cmstore.omanetapi;

public enum SyncMsgType {
    DEFAULT(0),
    MESSAGE(1),
    FAX(2),
    VM(3),
    CALLLOG(4),
    VM_GREETINGS(5);
    
    private final int mId;

    private SyncMsgType(int i) {
        this.mId = i;
    }

    public int getId() {
        return this.mId;
    }

    public static SyncMsgType valueOf(int i) {
        for (SyncMsgType syncMsgType : values()) {
            if (syncMsgType.mId == i) {
                return syncMsgType;
            }
        }
        return null;
    }
}
