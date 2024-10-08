package com.sec.internal.ims.core.sim;

import android.text.TextUtils;
import com.sec.internal.ims.core.sim.CscNetParser;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class CscNetParser$$ExternalSyntheticLambda0 implements Predicate {
    public final /* synthetic */ String f$0;

    public /* synthetic */ CscNetParser$$ExternalSyntheticLambda0(String str) {
        this.f$0 = str;
    }

    public final boolean test(Object obj) {
        return TextUtils.equals(this.f$0, ((CscNetParser.CscNetwork) obj).getNetworkName());
    }
}
