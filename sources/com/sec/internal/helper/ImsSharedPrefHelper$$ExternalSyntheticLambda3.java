package com.sec.internal.helper;

import android.content.SharedPreferences;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsSharedPrefHelper$$ExternalSyntheticLambda3 implements Consumer {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ boolean f$1;

    public /* synthetic */ ImsSharedPrefHelper$$ExternalSyntheticLambda3(String str, boolean z) {
        this.f$0 = str;
        this.f$1 = z;
    }

    public final void accept(Object obj) {
        ((SharedPreferences) obj).edit().putBoolean(this.f$0, this.f$1).apply();
    }
}
