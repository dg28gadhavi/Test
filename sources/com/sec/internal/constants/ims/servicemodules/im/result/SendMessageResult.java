package com.sec.internal.constants.ims.servicemodules.im.result;

import com.android.internal.util.Preconditions;

public class SendMessageResult {
    public String mAllowedMethods;
    public boolean mIsProvisional;
    public Object mRawHandle;
    public final Result mResult;

    public SendMessageResult(Object obj, Result result) {
        this(obj, result, false);
    }

    public SendMessageResult(Object obj, Result result, boolean z) {
        this.mRawHandle = obj;
        this.mResult = (Result) Preconditions.checkNotNull(result, "%s", new Object[]{"SendMessageResult: result is null."});
        this.mIsProvisional = z;
        this.mAllowedMethods = null;
    }

    public SendMessageResult(Object obj, Result result, String str) {
        this.mRawHandle = obj;
        this.mResult = (Result) Preconditions.checkNotNull(result, "%s", new Object[]{"SendMessageResult: result is null."});
        this.mIsProvisional = false;
        this.mAllowedMethods = str;
    }

    public String toString() {
        return "SendMessageParams [mRawHandle=" + this.mRawHandle + ", mResult=" + this.mResult + ", mIsProvisional=" + this.mIsProvisional + ", mAllowedMethods=" + this.mAllowedMethods + "]";
    }
}
