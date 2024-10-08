package com.sec.internal.ims.core.handler.secims;

import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ResipRegistrationManager$$ExternalSyntheticLambda3 implements Function {
    public final /* synthetic */ int f$0;

    public /* synthetic */ ResipRegistrationManager$$ExternalSyntheticLambda3(int i) {
        this.f$0 = i;
    }

    public final Object apply(Object obj) {
        return ((IImModule) obj).getImConfig(this.f$0);
    }
}
