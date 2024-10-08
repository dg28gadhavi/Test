package com.sec.internal.ims.servicemodules.im.util;

import com.sec.internal.ims.servicemodules.im.util.AsyncFileTask;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class AsyncFileTask$SerialExecutor$$ExternalSyntheticLambda0 implements Runnable {
    public final /* synthetic */ AsyncFileTask.SerialExecutor f$0;
    public final /* synthetic */ Runnable f$1;

    public /* synthetic */ AsyncFileTask$SerialExecutor$$ExternalSyntheticLambda0(AsyncFileTask.SerialExecutor serialExecutor, Runnable runnable) {
        this.f$0 = serialExecutor;
        this.f$1 = runnable;
    }

    public final void run() {
        this.f$0.lambda$execute$0(this.f$1);
    }
}
