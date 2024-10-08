package com.sec.internal.helper;

import java.util.concurrent.Callable;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SimpleEventLog$$ExternalSyntheticLambda1 implements Callable {
    public final /* synthetic */ SimpleEventLog f$0;

    public /* synthetic */ SimpleEventLog$$ExternalSyntheticLambda1(SimpleEventLog simpleEventLog) {
        this.f$0 = simpleEventLog;
    }

    public final Object call() {
        return this.f$0.flush();
    }
}
