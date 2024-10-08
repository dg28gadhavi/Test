package com.sec.internal.imsphone;

import android.telephony.ims.DelegateRegistrationState;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipDelegateImpl$$ExternalSyntheticLambda0 implements Consumer {
    public final /* synthetic */ DelegateRegistrationState.Builder f$0;

    public /* synthetic */ SipDelegateImpl$$ExternalSyntheticLambda0(DelegateRegistrationState.Builder builder) {
        this.f$0 = builder;
    }

    public final void accept(Object obj) {
        this.f$0.addDeregisteredFeatureTag((String) obj, 1);
    }
}
