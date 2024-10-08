package com.sec.internal.helper;

import android.content.SharedPreferences;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsSharedPrefHelper$$ExternalSyntheticLambda6 implements Consumer {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ long f$1;

    public /* synthetic */ ImsSharedPrefHelper$$ExternalSyntheticLambda6(String str, long j) {
        this.f$0 = str;
        this.f$1 = j;
    }

    public final void accept(Object obj) {
        ((SharedPreferences) obj).edit().putLong(this.f$0, this.f$1).apply();
    }
}
