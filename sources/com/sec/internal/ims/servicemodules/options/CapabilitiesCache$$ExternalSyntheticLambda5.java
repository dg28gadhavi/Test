package com.sec.internal.ims.servicemodules.options;

import com.sec.ims.options.Capabilities;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class CapabilitiesCache$$ExternalSyntheticLambda5 implements Runnable {
    public final /* synthetic */ CapabilitiesCache f$0;
    public final /* synthetic */ Capabilities f$1;
    public final /* synthetic */ boolean f$2;

    public /* synthetic */ CapabilitiesCache$$ExternalSyntheticLambda5(CapabilitiesCache capabilitiesCache, Capabilities capabilities, boolean z) {
        this.f$0 = capabilitiesCache;
        this.f$1 = capabilities;
        this.f$2 = z;
    }

    public final void run() {
        this.f$0.lambda$persistToContactDB$4(this.f$1, this.f$2);
    }
}
