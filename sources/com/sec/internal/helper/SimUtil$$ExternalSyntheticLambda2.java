package com.sec.internal.helper;

import com.sec.internal.interfaces.ims.core.ISimManager;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SimUtil$$ExternalSyntheticLambda2 implements Function {
    public final Object apply(Object obj) {
        return Boolean.valueOf(((ISimManager) obj).hasNoSim());
    }
}
