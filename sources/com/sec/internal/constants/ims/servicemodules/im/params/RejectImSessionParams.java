package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;

public class RejectImSessionParams {
    public Message mCallback;
    public String mChatId;
    public Object mRawHandle;
    public ImSessionRejectReason mSessionRejectReason;

    public RejectImSessionParams(String str, Object obj) {
        this.mChatId = str;
        this.mRawHandle = obj;
    }

    public RejectImSessionParams(String str, Object obj, ImSessionRejectReason imSessionRejectReason, Message message) {
        this.mChatId = str;
        this.mRawHandle = obj;
        this.mSessionRejectReason = imSessionRejectReason;
        this.mCallback = message;
    }

    public String toString() {
        return "RejectImSessionParams [mChatId=" + this.mChatId + ", mRawHandle=" + this.mRawHandle + ", mSessionStopReason= " + this.mSessionRejectReason + "]";
    }
}
