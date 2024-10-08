package com.sec.internal.ims.servicemodules.options;

import com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class CapabilityDiscoveryModule$TelephonyCallbackForCapability$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ CapabilityDiscoveryModule.TelephonyCallbackForCapability f$0;
    public final /* synthetic */ int f$1;

    public /* synthetic */ CapabilityDiscoveryModule$TelephonyCallbackForCapability$$ExternalSyntheticLambda1(CapabilityDiscoveryModule.TelephonyCallbackForCapability telephonyCallbackForCapability, int i) {
        this.f$0 = telephonyCallbackForCapability;
        this.f$1 = i;
    }

    public final void run() {
        this.f$0.lambda$fetchCapabilities$1(this.f$1);
    }
}
