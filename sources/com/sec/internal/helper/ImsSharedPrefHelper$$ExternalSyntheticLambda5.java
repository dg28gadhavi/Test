package com.sec.internal.helper;

import android.content.SharedPreferences;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsSharedPrefHelper$$ExternalSyntheticLambda5 implements Function {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ int f$1;

    public /* synthetic */ ImsSharedPrefHelper$$ExternalSyntheticLambda5(String str, int i) {
        this.f$0 = str;
        this.f$1 = i;
    }

    public final Object apply(Object obj) {
        return Integer.valueOf(((SharedPreferences) obj).getInt(this.f$0, this.f$1));
    }
}
