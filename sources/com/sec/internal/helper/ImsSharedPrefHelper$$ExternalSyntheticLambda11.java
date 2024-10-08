package com.sec.internal.helper;

import android.content.SharedPreferences;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsSharedPrefHelper$$ExternalSyntheticLambda11 implements Consumer {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ int f$1;

    public /* synthetic */ ImsSharedPrefHelper$$ExternalSyntheticLambda11(String str, int i) {
        this.f$0 = str;
        this.f$1 = i;
    }

    public final void accept(Object obj) {
        ((SharedPreferences) obj).edit().putInt(this.f$0, this.f$1).apply();
    }
}
