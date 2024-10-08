package com.sec.internal.helper;

import android.content.SharedPreferences;
import java.util.Set;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsSharedPrefHelper$$ExternalSyntheticLambda7 implements Function {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ Set f$1;

    public /* synthetic */ ImsSharedPrefHelper$$ExternalSyntheticLambda7(String str, Set set) {
        this.f$0 = str;
        this.f$1 = set;
    }

    public final Object apply(Object obj) {
        return ((SharedPreferences) obj).getStringSet(this.f$0, this.f$1);
    }
}
