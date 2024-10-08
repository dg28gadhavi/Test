package com.sec.internal.imsphone;

import com.sec.internal.log.IMSLog;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipDelegateImpl$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ String f$0;

    public /* synthetic */ SipDelegateImpl$$ExternalSyntheticLambda1(String str) {
        this.f$0 = str;
    }

    public final void run() {
        IMSLog.i(SipDelegateImpl.LOG_TAG, "notifyMessageReceived: viaTransactionId: " + this.f$0);
    }
}
