package com.sec.internal.imsphone;

import android.telephony.ims.SipMessage;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipDelegateImpl$$ExternalSyntheticLambda7 implements Runnable {
    public final /* synthetic */ SipDelegateImpl f$0;
    public final /* synthetic */ SipMessage f$1;
    public final /* synthetic */ long f$2;

    public /* synthetic */ SipDelegateImpl$$ExternalSyntheticLambda7(SipDelegateImpl sipDelegateImpl, SipMessage sipMessage, long j) {
        this.f$0 = sipDelegateImpl;
        this.f$1 = sipMessage;
        this.f$2 = j;
    }

    public final void run() {
        this.f$0.lambda$sendMessage$0(this.f$1, this.f$2);
    }
}
