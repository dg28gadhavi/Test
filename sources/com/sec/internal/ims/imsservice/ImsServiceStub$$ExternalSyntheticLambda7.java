package com.sec.internal.ims.imsservice;

import com.sec.ims.settings.ImsProfile;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsServiceStub$$ExternalSyntheticLambda7 implements Predicate {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ int f$1;

    public /* synthetic */ ImsServiceStub$$ExternalSyntheticLambda7(String str, int i) {
        this.f$0 = str;
        this.f$1 = i;
    }

    public final boolean test(Object obj) {
        return ImsServiceStub.lambda$isServiceAvailable$12(this.f$0, this.f$1, (ImsProfile) obj);
    }
}
