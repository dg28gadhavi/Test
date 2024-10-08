package com.sec.internal.ims.servicemodules.csh.event;

public class VshVideoDisplayParams {
    public ICshSuccessCallback mCallback;
    public int mSessionId;
    public VideoDisplay mVideoDisplay;
    public VshViewType mViewType;

    public VshVideoDisplayParams(int i, VshViewType vshViewType, VideoDisplay videoDisplay, ICshSuccessCallback iCshSuccessCallback) {
        this.mSessionId = i;
        this.mViewType = vshViewType;
        this.mVideoDisplay = videoDisplay;
        this.mCallback = iCshSuccessCallback;
    }

    public String toString() {
        return "VshVideoDisplayParams #" + this.mSessionId;
    }
}
