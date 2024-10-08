package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.log.IMSLog;

public class AcceptSlmParams {
    public Message mCallback;
    public String mChatId;
    public String mOwnImsi;
    public Object mRawHandle;
    public String mUserAlias;

    public AcceptSlmParams(String str, String str2, Object obj, Message message, String str3) {
        this.mChatId = str;
        this.mUserAlias = str2;
        this.mRawHandle = obj;
        this.mCallback = message;
        this.mOwnImsi = str3;
    }

    public String toString() {
        return "AcceptSlmParams [mChatId=" + this.mChatId + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mRawHandle=" + this.mRawHandle + ", mCallback=" + this.mCallback + "]";
    }
}
