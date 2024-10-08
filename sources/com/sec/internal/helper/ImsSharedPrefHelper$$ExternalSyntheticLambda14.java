package com.sec.internal.helper;

import android.content.SharedPreferences;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsSharedPrefHelper$$ExternalSyntheticLambda14 implements Consumer {
    public final /* synthetic */ String f$0;

    public /* synthetic */ ImsSharedPrefHelper$$ExternalSyntheticLambda14(String str) {
        this.f$0 = str;
    }

    public final void accept(Object obj) {
        ((SharedPreferences) obj).edit().remove(this.f$0).apply();
    }
}
