package com.sec.internal.constants.ims.servicemodules.im.result;

import com.android.internal.util.Preconditions;

public class SendSlmResult {
    public final String mPAssertedIdentity;
    public final Result mResult;

    public SendSlmResult(Result result, String str) {
        this.mResult = (Result) Preconditions.checkNotNull(result, "%s", new Object[]{"SendSlmResult: result is null."});
        this.mPAssertedIdentity = str;
    }

    public String toString() {
        return "SendSlmResult [, mResult=" + this.mResult + ", mPAssertedIdentity=" + this.mPAssertedIdentity + "]";
    }
}
