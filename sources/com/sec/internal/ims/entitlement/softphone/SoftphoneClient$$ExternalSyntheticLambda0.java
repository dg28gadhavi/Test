package com.sec.internal.ims.entitlement.softphone;

import android.location.Address;
import com.sec.internal.ims.entitlement.util.GeolocationUpdateFlow;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SoftphoneClient$$ExternalSyntheticLambda0 implements GeolocationUpdateFlow.LocationUpdateListener {
    public final /* synthetic */ SoftphoneClient f$0;

    public /* synthetic */ SoftphoneClient$$ExternalSyntheticLambda0(SoftphoneClient softphoneClient) {
        this.f$0 = softphoneClient;
    }

    public final void onAddressObtained(Address address) {
        this.f$0.lambda$checkAutoRegistrationCondition$3(address);
    }
}
