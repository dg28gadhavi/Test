package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.log.IMSLog;

public class AcceptFtSessionParams {
    public Message mCallback;
    public long mEnd;
    public String mFilePath;
    public int mMessageId;
    public Object mRawHandle;
    public long mStart;
    public String mUserAlias;

    public AcceptFtSessionParams(int i, Object obj, String str, String str2, Message message, long j, long j2) {
        this.mMessageId = i;
        this.mRawHandle = obj;
        this.mFilePath = str;
        this.mUserAlias = str2;
        this.mCallback = message;
        this.mStart = j;
        this.mEnd = j2;
    }

    public String toString() {
        return "AcceptFtSessionParams [mMessageId=" + this.mMessageId + ", mRawHandle=" + this.mRawHandle + ", mStart=" + this.mStart + ", mEnd=" + this.mEnd + ", mFilePath=" + IMSLog.checker(this.mFilePath) + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mCallback=" + this.mCallback + "]";
    }
}
