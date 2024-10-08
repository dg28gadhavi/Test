package com.sec.internal.ims.servicemodules.csh.event;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;

public class IshIncomingSessionEvent {
    public IshFileTransfer mFt;
    public ImsUri mRemoteUri;
    public int mSessionId;
    public String mUserAlias;

    public IshIncomingSessionEvent(int i, ImsUri imsUri, String str, IshFileTransfer ishFileTransfer) {
        this.mSessionId = i;
        this.mRemoteUri = imsUri;
        this.mUserAlias = str;
        this.mFt = ishFileTransfer;
    }

    public String toString() {
        return "IshIncomingSessionEvent [mSessionId=" + this.mSessionId + ", mRemoteUri=" + IMSLog.checker(this.mRemoteUri) + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mFt=" + this.mFt + "]";
    }
}
