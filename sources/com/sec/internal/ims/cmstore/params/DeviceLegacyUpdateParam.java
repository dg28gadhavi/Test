package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.log.IMSLog;

public class DeviceLegacyUpdateParam {
    public String mCorrelationTag;
    public String mLine;
    public String mMId;
    public CloudMessageBufferDBConstants.MsgOperationFlag mOperation;
    public long mRowId;
    public String mTRId;
    public int mTableindex;

    public DeviceLegacyUpdateParam(int i, CloudMessageBufferDBConstants.MsgOperationFlag msgOperationFlag, int i2, String str, String str2, String str3, String str4) {
        this.mTableindex = i;
        this.mOperation = msgOperationFlag;
        this.mRowId = (long) i2;
        this.mCorrelationTag = str;
        this.mMId = str2;
        this.mTRId = str3;
        this.mLine = str4;
    }

    public String toString() {
        return "DeviceLegacyUpdateParam [mTableindex=" + this.mTableindex + ", mOperation=" + this.mOperation + ", mRowId=" + this.mRowId + ", mCorrelationTag=" + this.mCorrelationTag + ", mMId=" + this.mMId + ", mTRId=" + this.mTRId + ", mLine=" + IMSLog.checker(this.mLine) + "]";
    }
}
