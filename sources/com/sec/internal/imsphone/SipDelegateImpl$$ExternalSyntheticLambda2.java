package com.sec.internal.imsphone;

import com.sec.internal.log.IMSLog;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipDelegateImpl$$ExternalSyntheticLambda2 implements Runnable {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ int f$1;

    public /* synthetic */ SipDelegateImpl$$ExternalSyntheticLambda2(String str, int i) {
        this.f$0 = str;
        this.f$1 = i;
    }

    public final void run() {
        IMSLog.i(SipDelegateImpl.LOG_TAG, "notifyMessageReceiveError: viaTransactionId: " + this.f$0 + ", reason: " + this.f$1);
    }
}
