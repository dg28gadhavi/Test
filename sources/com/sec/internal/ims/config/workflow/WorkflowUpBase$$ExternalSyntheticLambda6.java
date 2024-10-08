package com.sec.internal.ims.config.workflow;

import android.net.NetworkCapabilities;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class WorkflowUpBase$$ExternalSyntheticLambda6 implements Predicate {
    public final boolean test(Object obj) {
        return ((NetworkCapabilities) obj).hasTransport(0);
    }
}
