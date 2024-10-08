package com.sec.internal.helper;

import android.content.SharedPreferences;
import java.util.Set;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsSharedPrefHelper$$ExternalSyntheticLambda13 implements Consumer {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ Set f$1;

    public /* synthetic */ ImsSharedPrefHelper$$ExternalSyntheticLambda13(String str, Set set) {
        this.f$0 = str;
        this.f$1 = set;
    }

    public final void accept(Object obj) {
        ((SharedPreferences) obj).edit().putStringSet(this.f$0, this.f$1).apply();
    }
}
