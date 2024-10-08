package com.sec.internal.ims.servicemodules.euc;

import com.sec.internal.ims.servicemodules.euc.locale.IDeviceLocale;
import java.util.Locale;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class EucModule$$ExternalSyntheticLambda2 implements IDeviceLocale.IDeviceLocaleListener {
    public final /* synthetic */ EucModule f$0;

    public /* synthetic */ EucModule$$ExternalSyntheticLambda2(EucModule eucModule) {
        this.f$0 = eucModule;
    }

    public final void onLocaleChanged(Locale locale) {
        this.f$0.lambda$performStartupRegistrations$2(locale);
    }
}
