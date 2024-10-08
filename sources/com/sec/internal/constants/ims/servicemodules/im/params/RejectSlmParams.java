package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;

public class RejectSlmParams {
    public Message mCallback;
    public String mChatId;
    public String mOwnImsi;
    public Object mRawHandle;
    public ImSessionRejectReason mSessionRejectReason;

    public RejectSlmParams(String str, Object obj, ImSessionRejectReason imSessionRejectReason, Message message, String str2) {
        this.mChatId = str;
        this.mRawHandle = obj;
        this.mSessionRejectReason = imSessionRejectReason;
        this.mCallback = message;
        this.mOwnImsi = str2;
    }

    public String toString() {
        return "RejectSlmParams [mChatId=" + this.mChatId + ", mRawHandle=" + this.mRawHandle + ", mSessionStopReason= " + this.mSessionRejectReason + "]";
    }
}
