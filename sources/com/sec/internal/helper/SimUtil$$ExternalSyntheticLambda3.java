package com.sec.internal.helper;

import com.sec.internal.interfaces.ims.core.ISimManager;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SimUtil$$ExternalSyntheticLambda3 implements Function {
    public final /* synthetic */ String f$0;

    public /* synthetic */ SimUtil$$ExternalSyntheticLambda3(String str) {
        this.f$0 = str;
    }

    public final Object apply(Object obj) {
        return ((ISimManager) obj).getMnoFromNetworkPlmn(this.f$0);
    }
}
