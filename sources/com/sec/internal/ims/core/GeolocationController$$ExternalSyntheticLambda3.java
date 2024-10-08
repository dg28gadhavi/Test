package com.sec.internal.ims.core;

import android.content.SharedPreferences;
import java.util.Date;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class GeolocationController$$ExternalSyntheticLambda3 implements Consumer {
    public final /* synthetic */ String f$0;

    public /* synthetic */ GeolocationController$$ExternalSyntheticLambda3(String str) {
        this.f$0 = str;
    }

    public final void accept(Object obj) {
        ((SharedPreferences.Editor) obj).putLong("timestamp", new Date().getTime()).putString("cc", this.f$0).apply();
    }
}
