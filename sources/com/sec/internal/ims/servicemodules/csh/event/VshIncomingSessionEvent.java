package com.sec.internal.ims.servicemodules.csh.event;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;

public class VshIncomingSessionEvent {
    public String mContentType;
    public String mFilePath;
    public ImsUri mRemoteUri;
    public int mSessionId;
    public int mSource;

    public VshIncomingSessionEvent(int i, ImsUri imsUri, String str, int i2, String str2) {
        this.mSessionId = i;
        this.mRemoteUri = imsUri;
        this.mContentType = str;
        this.mSource = i2;
        this.mFilePath = str2;
    }

    public String toString() {
        return "VshIncomingSessionEvent [mSessionId=" + this.mSessionId + ", mRemoteUri=" + IMSLog.checker(this.mRemoteUri) + ", mContentType=" + this.mContentType + ", mSource=" + this.mSource + ", mFilePath=" + this.mFilePath + "]";
    }
}
