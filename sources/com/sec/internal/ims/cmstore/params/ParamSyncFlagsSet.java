package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;

public class ParamSyncFlagsSet {
    public CloudMessageBufferDBConstants.ActionStatusFlag mAction;
    public long mBufferId;
    public CloudMessageBufferDBConstants.DirectionFlag mDirection;
    public boolean mIsChanged = true;

    public ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag directionFlag, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag) {
        this.mDirection = directionFlag;
        this.mAction = actionStatusFlag;
    }

    public void setIsChangedActionAndDirection(boolean z, CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag, CloudMessageBufferDBConstants.DirectionFlag directionFlag) {
        this.mIsChanged = z;
        this.mAction = actionStatusFlag;
        this.mDirection = directionFlag;
    }

    public String toString() {
        return "ParamSyncFlagsSet [mDirection=" + this.mDirection + ", mAction=" + this.mAction + ", mIsChanged=" + this.mIsChanged + "]";
    }
}
