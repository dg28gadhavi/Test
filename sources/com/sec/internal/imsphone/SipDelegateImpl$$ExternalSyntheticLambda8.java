package com.sec.internal.imsphone;

import android.telephony.ims.DelegateRegistrationState;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipDelegateImpl$$ExternalSyntheticLambda8 implements Consumer {
    public final /* synthetic */ SipDelegateImpl f$0;
    public final /* synthetic */ DelegateRegistrationState.Builder f$1;
    public final /* synthetic */ int f$2;

    public /* synthetic */ SipDelegateImpl$$ExternalSyntheticLambda8(SipDelegateImpl sipDelegateImpl, DelegateRegistrationState.Builder builder, int i) {
        this.f$0 = sipDelegateImpl;
        this.f$1 = builder;
        this.f$2 = i;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$notifyDeRegistering$7(this.f$1, this.f$2, (String) obj);
    }
}
