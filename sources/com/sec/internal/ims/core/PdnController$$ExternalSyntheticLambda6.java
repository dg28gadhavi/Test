package com.sec.internal.ims.core;

import com.sec.internal.constants.ims.os.NetworkState;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class PdnController$$ExternalSyntheticLambda6 implements Function {
    public final Object apply(Object obj) {
        return Boolean.valueOf(((NetworkState) obj).isEmergencyEpdgConnected());
    }
}
