package com.sec.internal.ims.servicemodules.options;

import com.sec.ims.util.ImsUri;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class CapabilityDiscoveryModule$$ExternalSyntheticLambda4 implements Function {
    public final /* synthetic */ ImsUri f$0;

    public /* synthetic */ CapabilityDiscoveryModule$$ExternalSyntheticLambda4(ImsUri imsUri) {
        this.f$0 = imsUri;
    }

    public final Object apply(Object obj) {
        return ((CapabilitiesCache) obj).get(this.f$0);
    }
}
