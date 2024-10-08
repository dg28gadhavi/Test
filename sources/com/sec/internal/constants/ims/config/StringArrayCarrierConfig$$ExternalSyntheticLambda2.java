package com.sec.internal.constants.ims.config;

import android.os.PersistableBundle;
import com.google.gson.JsonElement;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class StringArrayCarrierConfig$$ExternalSyntheticLambda2 implements Consumer {
    public final /* synthetic */ StringArrayCarrierConfig f$0;
    public final /* synthetic */ PersistableBundle f$1;

    public /* synthetic */ StringArrayCarrierConfig$$ExternalSyntheticLambda2(StringArrayCarrierConfig stringArrayCarrierConfig, PersistableBundle persistableBundle) {
        this.f$0 = stringArrayCarrierConfig;
        this.f$1 = persistableBundle;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$putOverrideConfig$1(this.f$1, (JsonElement) obj);
    }
}
