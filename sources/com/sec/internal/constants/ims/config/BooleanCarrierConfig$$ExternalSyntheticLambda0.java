package com.sec.internal.constants.ims.config;

import android.os.PersistableBundle;
import com.google.gson.JsonElement;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class BooleanCarrierConfig$$ExternalSyntheticLambda0 implements Consumer {
    public final /* synthetic */ BooleanCarrierConfig f$0;
    public final /* synthetic */ PersistableBundle f$1;

    public /* synthetic */ BooleanCarrierConfig$$ExternalSyntheticLambda0(BooleanCarrierConfig booleanCarrierConfig, PersistableBundle persistableBundle) {
        this.f$0 = booleanCarrierConfig;
        this.f$1 = persistableBundle;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$putOverrideConfig$0(this.f$1, (JsonElement) obj);
    }
}
