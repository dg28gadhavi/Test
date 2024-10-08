package com.sec.internal.helper;

import android.content.SharedPreferences;
import java.util.Map;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsSharedPrefHelper$$ExternalSyntheticLambda1 implements Consumer {
    public final /* synthetic */ String[] f$0;
    public final /* synthetic */ Map f$1;

    public /* synthetic */ ImsSharedPrefHelper$$ExternalSyntheticLambda1(String[] strArr, Map map) {
        this.f$0 = strArr;
        this.f$1 = map;
    }

    public final void accept(Object obj) {
        ImsSharedPrefHelper.lambda$getStringArray$13(this.f$0, this.f$1, (SharedPreferences) obj);
    }
}
