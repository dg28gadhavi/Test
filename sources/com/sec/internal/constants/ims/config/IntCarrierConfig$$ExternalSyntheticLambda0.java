package com.sec.internal.constants.ims.config;

import android.os.PersistableBundle;
import com.google.gson.JsonElement;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class IntCarrierConfig$$ExternalSyntheticLambda0 implements Consumer {
    public final /* synthetic */ IntCarrierConfig f$0;
    public final /* synthetic */ PersistableBundle f$1;

    public /* synthetic */ IntCarrierConfig$$ExternalSyntheticLambda0(IntCarrierConfig intCarrierConfig, PersistableBundle persistableBundle) {
        this.f$0 = intCarrierConfig;
        this.f$1 = persistableBundle;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$putOverrideConfig$0(this.f$1, (JsonElement) obj);
    }
}
