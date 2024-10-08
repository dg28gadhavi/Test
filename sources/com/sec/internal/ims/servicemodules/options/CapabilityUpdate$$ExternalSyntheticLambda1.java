package com.sec.internal.ims.servicemodules.options;

import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import java.util.List;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class CapabilityUpdate$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ CapabilityUpdate f$0;
    public final /* synthetic */ List f$1;
    public final /* synthetic */ int f$2;
    public final /* synthetic */ long f$3;
    public final /* synthetic */ CapabilityConstants.CapExResult f$4;
    public final /* synthetic */ String f$5;
    public final /* synthetic */ int f$6;
    public final /* synthetic */ List f$7;
    public final /* synthetic */ String f$8;

    public /* synthetic */ CapabilityUpdate$$ExternalSyntheticLambda1(CapabilityUpdate capabilityUpdate, List list, int i, long j, CapabilityConstants.CapExResult capExResult, String str, int i2, List list2, String str2) {
        this.f$0 = capabilityUpdate;
        this.f$1 = list;
        this.f$2 = i;
        this.f$3 = j;
        this.f$4 = capExResult;
        this.f$5 = str;
        this.f$6 = i2;
        this.f$7 = list2;
        this.f$8 = str2;
    }

    public final void run() {
        this.f$0.lambda$processUpdateCapabilities$1(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
    }
}
