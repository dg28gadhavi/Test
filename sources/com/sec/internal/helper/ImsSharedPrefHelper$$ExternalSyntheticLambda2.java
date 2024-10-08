package com.sec.internal.helper;

import android.content.SharedPreferences;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsSharedPrefHelper$$ExternalSyntheticLambda2 implements Consumer {
    public final void accept(Object obj) {
        ((SharedPreferences) obj).edit().clear().apply();
    }
}
