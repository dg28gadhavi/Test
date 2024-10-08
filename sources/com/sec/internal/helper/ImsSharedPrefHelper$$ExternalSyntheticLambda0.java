package com.sec.internal.helper;

import android.content.SharedPreferences;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsSharedPrefHelper$$ExternalSyntheticLambda0 implements Function {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ long f$1;

    public /* synthetic */ ImsSharedPrefHelper$$ExternalSyntheticLambda0(String str, long j) {
        this.f$0 = str;
        this.f$1 = j;
    }

    public final Object apply(Object obj) {
        return Long.valueOf(((SharedPreferences) obj).getLong(this.f$0, this.f$1));
    }
}
