package com.sec.internal.helper;

import android.content.SharedPreferences;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsSharedPrefHelper$$ExternalSyntheticLambda12 implements Function {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ boolean f$1;

    public /* synthetic */ ImsSharedPrefHelper$$ExternalSyntheticLambda12(String str, boolean z) {
        this.f$0 = str;
        this.f$1 = z;
    }

    public final Object apply(Object obj) {
        return Boolean.valueOf(((SharedPreferences) obj).getBoolean(this.f$0, this.f$1));
    }
}
