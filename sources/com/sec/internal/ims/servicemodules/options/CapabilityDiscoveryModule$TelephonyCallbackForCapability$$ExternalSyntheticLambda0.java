package com.sec.internal.ims.servicemodules.options;

import com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class CapabilityDiscoveryModule$TelephonyCallbackForCapability$$ExternalSyntheticLambda0 implements Runnable {
    public final /* synthetic */ CapabilityDiscoveryModule.TelephonyCallbackForCapability f$0;
    public final /* synthetic */ int f$1;
    public final /* synthetic */ int f$2;

    public /* synthetic */ CapabilityDiscoveryModule$TelephonyCallbackForCapability$$ExternalSyntheticLambda0(CapabilityDiscoveryModule.TelephonyCallbackForCapability telephonyCallbackForCapability, int i, int i2) {
        this.f$0 = telephonyCallbackForCapability;
        this.f$1 = i;
        this.f$2 = i2;
    }

    public final void run() {
        this.f$0.lambda$onDataConnectionStateChanged$0(this.f$1, this.f$2);
    }
}
