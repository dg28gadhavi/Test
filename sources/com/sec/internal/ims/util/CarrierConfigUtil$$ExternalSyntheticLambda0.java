package com.sec.internal.ims.util;

import android.os.PersistableBundle;
import com.google.gson.JsonObject;
import com.sec.internal.interfaces.ims.config.ICarrierConfig;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class CarrierConfigUtil$$ExternalSyntheticLambda0 implements Consumer {
    public final /* synthetic */ PersistableBundle f$0;
    public final /* synthetic */ JsonObject f$1;

    public /* synthetic */ CarrierConfigUtil$$ExternalSyntheticLambda0(PersistableBundle persistableBundle, JsonObject jsonObject) {
        this.f$0 = persistableBundle;
        this.f$1 = jsonObject;
    }

    public final void accept(Object obj) {
        ((ICarrierConfig) obj).putOverrideConfig(this.f$0, this.f$1);
    }
}
