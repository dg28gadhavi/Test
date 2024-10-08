package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.log.IMSLog;
import java.util.UUID;

public class ChangeGroupAliasParams {
    public final String mAlias;
    public final Message mCallback;
    public final Object mRawHandle;
    public final String mReqKey = UUID.randomUUID().toString();

    public ChangeGroupAliasParams(Object obj, String str, Message message) {
        this.mRawHandle = obj;
        this.mAlias = str;
        this.mCallback = message;
    }

    public String toString() {
        return "ChangeGroupAliasParams [mRawHandle=" + this.mRawHandle + ", mAlias=" + IMSLog.checker(this.mAlias) + ", mCallback=" + this.mCallback + ", mReqKey=" + this.mReqKey + "]";
    }
}
