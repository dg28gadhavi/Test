package com.sec.internal.ims.cmstore.helper;

import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.log.IMSLog;

public class SyncParam {
    public String mLine;
    public SyncMsgType mType;

    public SyncParam(String str, SyncMsgType syncMsgType) {
        this.mLine = str;
        this.mType = syncMsgType;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof SyncParam) {
            SyncParam syncParam = (SyncParam) obj;
            return syncParam.mType.equals(this.mType) && syncParam.mLine.equals(this.mLine);
        }
    }

    public int hashCode() {
        return this.mType.hashCode() + this.mLine.hashCode();
    }

    public String toString() {
        return "SyncParam = [ mLine = " + IMSLog.checker(this.mLine) + " ], [ mType = " + this.mType + " ].";
    }
}
