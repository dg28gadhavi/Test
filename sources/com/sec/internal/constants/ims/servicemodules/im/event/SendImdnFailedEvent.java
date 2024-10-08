package com.sec.internal.constants.ims.servicemodules.im.event;

import com.android.internal.util.Preconditions;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;

public class SendImdnFailedEvent {
    public final String mChatId;
    public final String mImdnId;
    public final Object mRawHandle;
    public final Result mResult;

    public SendImdnFailedEvent(Object obj, String str, String str2, Result result) {
        this.mRawHandle = obj;
        this.mChatId = str;
        this.mImdnId = str2;
        this.mResult = (Result) Preconditions.checkNotNull(result, "%s", new Object[]{"SendImdnFailedEvent: result is null."});
    }

    public String toString() {
        return "SendImdnFailedEvent [mRawHandle=" + this.mRawHandle + ", mChatId=" + this.mChatId + ", mImdnId=" + this.mImdnId + ", mResult=" + this.mResult + "]";
    }
}
