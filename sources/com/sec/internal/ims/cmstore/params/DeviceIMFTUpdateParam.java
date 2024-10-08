package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.log.IMSLog;

public class DeviceIMFTUpdateParam {
    public String mChatId;
    public String mImdnId;
    public String mLine;
    public CloudMessageBufferDBConstants.MsgOperationFlag mOperation;
    public long mRowId;
    public int mTableindex;
    public CloudMessageBufferDBConstants.ActionStatusFlag mUpdateType;

    public DeviceIMFTUpdateParam(int i, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, CloudMessageBufferDBConstants.MsgOperationFlag msgOperationFlag, long j, String str, String str2, String str3) {
        this.mTableindex = i;
        this.mUpdateType = actionStatusFlag;
        this.mOperation = msgOperationFlag;
        this.mRowId = j;
        this.mChatId = str;
        this.mImdnId = str2;
        this.mLine = str3;
    }

    public String toString() {
        return "DeviceIMFTUpdateParam [mTableindex=" + this.mTableindex + ", mUpdateType= " + this.mUpdateType + ", mOperation=" + this.mOperation + ", mRowId=" + this.mRowId + ", mChatId=" + this.mChatId + ", mImdnId=" + IMSLog.checker(this.mImdnId) + ", mLine=" + IMSLog.checker(this.mLine) + "]";
    }
}
