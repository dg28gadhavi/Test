package com.sec.internal.ims.core;

import android.telephony.PreciseDataConnectionState;
import android.telephony.data.ApnSetting;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class TelephonyCallbackForPdnController$$ExternalSyntheticLambda0 implements Consumer {
    public final /* synthetic */ TelephonyCallbackForPdnController f$0;
    public final /* synthetic */ int f$1;
    public final /* synthetic */ PreciseDataConnectionState f$2;

    public /* synthetic */ TelephonyCallbackForPdnController$$ExternalSyntheticLambda0(TelephonyCallbackForPdnController telephonyCallbackForPdnController, int i, PreciseDataConnectionState preciseDataConnectionState) {
        this.f$0 = telephonyCallbackForPdnController;
        this.f$1 = i;
        this.f$2 = preciseDataConnectionState;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$onPreciseDataConnectionStateChanged$0(this.f$1, this.f$2, (ApnSetting) obj);
    }
}
