package com.sec.internal.ims.core;

import com.sec.internal.constants.ims.os.NetworkEvent;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationManagerHandler$$ExternalSyntheticLambda16 implements Function {
    public final Object apply(Object obj) {
        return Boolean.valueOf(((NetworkEvent) obj).isWifiConnected);
    }
}
