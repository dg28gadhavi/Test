package com.sec.internal.ims.cmstore.params;

import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ParamVvmUpdate$VvmGreetingType$$ExternalSyntheticLambda0 implements Predicate {
    public final /* synthetic */ String f$0;

    public /* synthetic */ ParamVvmUpdate$VvmGreetingType$$ExternalSyntheticLambda0(String str) {
        this.f$0 = str;
    }

    public final boolean test(Object obj) {
        return ((ParamVvmUpdate.VvmGreetingType) obj).getName().equalsIgnoreCase(this.f$0);
    }
}
