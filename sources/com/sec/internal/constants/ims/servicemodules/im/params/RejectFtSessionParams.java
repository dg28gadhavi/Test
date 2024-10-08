package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;

public class RejectFtSessionParams {
    public Message mCallback;
    public String mFileTransferId;
    public String mImdnMessageId;
    public boolean mIsSlmSvcMsg;
    public Object mRawHandle;
    public FtRejectReason mRejectReason;

    public RejectFtSessionParams(Object obj, Message message, FtRejectReason ftRejectReason, String str) {
        this.mRawHandle = obj;
        this.mCallback = message;
        this.mRejectReason = ftRejectReason;
        this.mFileTransferId = str;
    }

    public RejectFtSessionParams(Object obj, Message message, FtRejectReason ftRejectReason, String str, String str2) {
        this(obj, message, ftRejectReason, str);
        this.mImdnMessageId = str2;
    }

    public RejectFtSessionParams(Object obj, Message message, FtRejectReason ftRejectReason, String str, boolean z) {
        this(obj, message, ftRejectReason, str);
        this.mIsSlmSvcMsg = z;
    }

    public String toString() {
        return "RejectFtSessionParams [mRawHandle=" + this.mRawHandle + ", mCallback=" + this.mCallback + ", mRejectReason=" + this.mRejectReason + ", mFileTransferId=" + this.mFileTransferId + "]";
    }
}
