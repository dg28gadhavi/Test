package com.sec.internal.ims.servicemodules.options;

import com.sec.ims.ImsRegistration;
import java.util.Map;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class CapabilityRegistration$$ExternalSyntheticLambda0 implements Runnable {
    public final /* synthetic */ CapabilityRegistration f$0;
    public final /* synthetic */ ImsRegistration f$1;
    public final /* synthetic */ Map f$2;

    public /* synthetic */ CapabilityRegistration$$ExternalSyntheticLambda0(CapabilityRegistration capabilityRegistration, ImsRegistration imsRegistration, Map map) {
        this.f$0 = capabilityRegistration;
        this.f$1 = imsRegistration;
        this.f$2 = map;
    }

    public final void run() {
        this.f$0.lambda$processDeregistered$0(this.f$1, this.f$2);
    }
}
