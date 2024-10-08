package com.sec.internal.constants.ims.servicemodules.im.event;

import com.android.internal.util.Preconditions;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;

public class ImSessionClosedEvent {
    public final String mChatId;
    public final Object mRawHandle;
    public final ImsUri mReferredBy;
    public final Result mResult;

    public ImSessionClosedEvent(Object obj, String str, Result result) {
        this.mRawHandle = obj;
        this.mChatId = str;
        this.mResult = (Result) Preconditions.checkNotNull(result, "%s", new Object[]{"ImSessionClosedEvent: result is null."});
        this.mReferredBy = null;
    }

    public ImSessionClosedEvent(Object obj, String str, Result result, ImsUri imsUri) {
        this.mRawHandle = obj;
        this.mChatId = str;
        this.mResult = (Result) Preconditions.checkNotNull(result, "%s", new Object[]{"ImSessionClosedEvent: result is null."});
        this.mReferredBy = imsUri;
    }

    public String toString() {
        return "ImSessionClosedEvent [mRawHandle= " + this.mRawHandle + ", mChatId=" + this.mChatId + ", mResult=" + this.mResult + "]";
    }
}
