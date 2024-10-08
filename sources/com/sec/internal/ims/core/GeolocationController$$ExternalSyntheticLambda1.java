package com.sec.internal.ims.core;

import com.sec.ims.settings.ImsProfile;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class GeolocationController$$ExternalSyntheticLambda1 implements Predicate {
    public final /* synthetic */ GeolocationController f$0;
    public final /* synthetic */ int f$1;

    public /* synthetic */ GeolocationController$$ExternalSyntheticLambda1(GeolocationController geolocationController, int i) {
        this.f$0 = geolocationController;
        this.f$1 = i;
    }

    public final boolean test(Object obj) {
        return this.f$0.lambda$isNeedRequestLocation$3(this.f$1, (ImsProfile) obj);
    }
}
