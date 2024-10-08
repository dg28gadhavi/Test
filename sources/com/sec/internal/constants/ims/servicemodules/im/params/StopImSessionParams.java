package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;

public class StopImSessionParams {
    public Message mCallback;
    public Object mRawHandle;
    public ImSessionStopReason mSessionStopReason;

    public StopImSessionParams(Object obj, ImSessionStopReason imSessionStopReason, Message message) {
        this.mRawHandle = obj;
        this.mSessionStopReason = imSessionStopReason;
        this.mCallback = message;
    }

    public String toString() {
        return "StopImSessionParams [mRawHandle=" + this.mRawHandle + ", mSessionStopReason=" + this.mSessionStopReason + ", mCallback=" + this.mCallback + " ]";
    }
}
