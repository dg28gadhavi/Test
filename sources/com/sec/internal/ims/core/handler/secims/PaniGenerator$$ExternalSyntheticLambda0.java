package com.sec.internal.ims.core.handler.secims;

import android.net.ConnectivityManager;
import android.net.Network;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class PaniGenerator$$ExternalSyntheticLambda0 implements Function {
    public final /* synthetic */ ConnectivityManager f$0;

    public /* synthetic */ PaniGenerator$$ExternalSyntheticLambda0(ConnectivityManager connectivityManager) {
        this.f$0 = connectivityManager;
    }

    public final Object apply(Object obj) {
        return this.f$0.getNetworkCapabilities((Network) obj);
    }
}
