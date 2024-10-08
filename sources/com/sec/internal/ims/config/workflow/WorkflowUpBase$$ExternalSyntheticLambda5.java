package com.sec.internal.ims.config.workflow;

import android.net.ConnectivityManager;
import android.net.Network;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class WorkflowUpBase$$ExternalSyntheticLambda5 implements Function {
    public final /* synthetic */ ConnectivityManager f$0;

    public /* synthetic */ WorkflowUpBase$$ExternalSyntheticLambda5(ConnectivityManager connectivityManager) {
        this.f$0 = connectivityManager;
    }

    public final Object apply(Object obj) {
        return this.f$0.getNetworkCapabilities((Network) obj);
    }
}
