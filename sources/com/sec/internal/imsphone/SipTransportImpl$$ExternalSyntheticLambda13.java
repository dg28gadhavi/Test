package com.sec.internal.imsphone;

import com.sec.ims.settings.ImsProfile;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipTransportImpl$$ExternalSyntheticLambda13 implements Predicate {
    public final /* synthetic */ ImsProfile f$0;

    public /* synthetic */ SipTransportImpl$$ExternalSyntheticLambda13(ImsProfile imsProfile) {
        this.f$0 = imsProfile;
    }

    public final boolean test(Object obj) {
        return ((SipDelegateImpl) obj).isMatched(this.f$0);
    }
}
