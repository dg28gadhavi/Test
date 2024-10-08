package com.sec.internal.ims.cmstore.params;

public class DeviceMsgAppFetchUriParam {
    public String mAppType;
    public long mBufferRowId;
    public long mImsPartId;
    public int mTableindex;
    public long mTelephonyRowId;

    public DeviceMsgAppFetchUriParam(int i, long j, long j2, long j3, String str) {
        this.mTableindex = i;
        this.mBufferRowId = j;
        this.mTelephonyRowId = j2;
        this.mImsPartId = j3;
        this.mAppType = str;
    }

    public String toString() {
        return "DeviceMsgAppFetchUpdateParam [mTableindex=" + this.mTableindex + ", mBufferRowId=" + this.mBufferRowId + ", mTelephonyRowId=" + this.mTelephonyRowId + ", mImsPartId= " + this.mImsPartId + ", appType=" + this.mAppType + "]";
    }
}
