package com.sec.internal.helper;

import android.content.SharedPreferences;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsSharedPrefHelper$$ExternalSyntheticLambda9 implements Function {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ String f$1;

    public /* synthetic */ ImsSharedPrefHelper$$ExternalSyntheticLambda9(String str, String str2) {
        this.f$0 = str;
        this.f$1 = str2;
    }

    public final Object apply(Object obj) {
        return ((SharedPreferences) obj).getString(this.f$0, this.f$1);
    }
}
