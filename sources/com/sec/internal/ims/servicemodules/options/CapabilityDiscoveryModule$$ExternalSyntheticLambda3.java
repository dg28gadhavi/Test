package com.sec.internal.ims.servicemodules.options;

import com.sec.ims.options.Capabilities;
import java.util.List;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class CapabilityDiscoveryModule$$ExternalSyntheticLambda3 implements Runnable {
    public final /* synthetic */ CapabilityDiscoveryModule f$0;
    public final /* synthetic */ int f$1;
    public final /* synthetic */ List f$2;
    public final /* synthetic */ Capabilities f$3;

    public /* synthetic */ CapabilityDiscoveryModule$$ExternalSyntheticLambda3(CapabilityDiscoveryModule capabilityDiscoveryModule, int i, List list, Capabilities capabilities) {
        this.f$0 = capabilityDiscoveryModule;
        this.f$1 = i;
        this.f$2 = list;
        this.f$3 = capabilities;
    }

    public final void run() {
        this.f$0.lambda$notifyCapabilitiesChanged$4(this.f$1, this.f$2, this.f$3);
    }
}
