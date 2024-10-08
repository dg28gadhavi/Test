package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;

public class DeviceMsgAppFetchUpdateParam {
    public long mBufferRowId;
    public boolean mIsFT;
    public int mTableindex;
    public long mTelephonyRowId;
    public CloudMessageBufferDBConstants.ActionStatusFlag mUpdateType;

    public DeviceMsgAppFetchUpdateParam(int i, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, long j, long j2, boolean z) {
        this.mTableindex = i;
        this.mUpdateType = actionStatusFlag;
        this.mBufferRowId = j;
        this.mTelephonyRowId = j2;
        this.mIsFT = z;
    }

    public String toString() {
        return "DeviceMsgAppFetchUpdateParam [mTableindex=" + this.mTableindex + ", mUpdateType= " + this.mUpdateType + ", mBufferRowId=" + this.mBufferRowId + ", mTelephonyRowId=" + this.mTelephonyRowId + ", mIsFT=" + this.mIsFT + "]";
    }
}
