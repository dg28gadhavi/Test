package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.log.IMSLog;

public class AcceptImSessionParams {
    public Message mCallback;
    public String mChatId;
    public boolean mIsSnF;
    public Object mRawHandle;
    public String mUserAlias;

    public AcceptImSessionParams(String str, String str2, Object obj, boolean z, Message message) {
        this.mChatId = str;
        this.mUserAlias = str2;
        this.mRawHandle = obj;
        this.mIsSnF = z;
        this.mCallback = message;
    }

    public String toString() {
        return "AcceptImSessionParams [mChatId=" + this.mChatId + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mRawHandle=" + this.mRawHandle + ", mIsSnF=" + this.mIsSnF + ", mCallback=" + this.mCallback + "]";
    }
}
