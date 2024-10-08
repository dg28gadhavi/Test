package com.sec.internal.log;

import com.sec.internal.log.CmcPingTestLogger;
import java.util.List;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class CmcPingTestLogger$$ExternalSyntheticLambda0 implements Runnable {
    public final /* synthetic */ List f$0;
    public final /* synthetic */ CmcPingTestLogger.OnFinishListener f$1;

    public /* synthetic */ CmcPingTestLogger$$ExternalSyntheticLambda0(List list, CmcPingTestLogger.OnFinishListener onFinishListener) {
        this.f$0 = list;
        this.f$1 = onFinishListener;
    }

    public final void run() {
        CmcPingTestLogger.lambda$ping$0(this.f$0, this.f$1);
    }
}
