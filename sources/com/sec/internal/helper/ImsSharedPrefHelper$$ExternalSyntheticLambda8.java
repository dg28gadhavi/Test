package com.sec.internal.helper;

import android.content.SharedPreferences;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsSharedPrefHelper$$ExternalSyntheticLambda8 implements Consumer {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ String f$1;

    public /* synthetic */ ImsSharedPrefHelper$$ExternalSyntheticLambda8(String str, String str2) {
        this.f$0 = str;
        this.f$1 = str2;
    }

    public final void accept(Object obj) {
        ((SharedPreferences) obj).edit().putString(this.f$0, this.f$1).apply();
    }
}
