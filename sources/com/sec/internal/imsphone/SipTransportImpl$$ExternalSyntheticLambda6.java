package com.sec.internal.imsphone;

import com.sec.internal.constants.ims.SipMsg;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipTransportImpl$$ExternalSyntheticLambda6 implements Consumer {
    public final /* synthetic */ SipMsg f$0;

    public /* synthetic */ SipTransportImpl$$ExternalSyntheticLambda6(SipMsg sipMsg) {
        this.f$0 = sipMsg;
    }

    public final void accept(Object obj) {
        ((SipDelegateImpl) obj).notifySipMessage(this.f$0);
    }
}
