package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;

public class DeviceSessionPartcptsUpdateParam {
    public String mChatId;
    public int mTableindex;
    public CloudMessageBufferDBConstants.ActionStatusFlag mUpdateType;

    public DeviceSessionPartcptsUpdateParam(int i, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, String str) {
        this.mTableindex = i;
        this.mUpdateType = actionStatusFlag;
        this.mChatId = str;
    }

    public String toString() {
        return "DeviceSessionPartcptsUpdateParam [mTableindex=" + this.mTableindex + ", mUpdateType= " + this.mUpdateType + ", mChatId=" + this.mChatId + "]";
    }
}
