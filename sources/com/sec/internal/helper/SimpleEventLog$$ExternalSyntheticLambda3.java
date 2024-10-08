package com.sec.internal.helper;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SimpleEventLog$$ExternalSyntheticLambda3 implements Callable {
    public final /* synthetic */ SimpleEventLog f$0;
    public final /* synthetic */ CompletableFuture f$1;
    public final /* synthetic */ List f$2;

    public /* synthetic */ SimpleEventLog$$ExternalSyntheticLambda3(SimpleEventLog simpleEventLog, CompletableFuture completableFuture, List list) {
        this.f$0 = simpleEventLog;
        this.f$1 = completableFuture;
        this.f$2 = list;
    }

    public final Object call() {
        return this.f$0.lambda$flush$0(this.f$1, this.f$2);
    }
}
