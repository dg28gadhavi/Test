package com.sec.internal.ims.core;

import android.content.SharedPreferences;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class GeolocationController$$ExternalSyntheticLambda4 implements Function {
    public final Object apply(Object obj) {
        return Long.valueOf(((SharedPreferences) obj).getLong("timestamp", 0));
    }
}
