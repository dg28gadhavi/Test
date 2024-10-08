package com.sec.internal.ims.core;

import com.sec.internal.constants.ims.core.PdnFailReason;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationGovernorBase$$ExternalSyntheticLambda4 implements Predicate {
    public final /* synthetic */ PdnFailReason f$0;

    public /* synthetic */ RegistrationGovernorBase$$ExternalSyntheticLambda4(PdnFailReason pdnFailReason) {
        this.f$0 = pdnFailReason;
    }

    public final boolean test(Object obj) {
        return ((String) obj).contains(this.f$0.name());
    }
}
