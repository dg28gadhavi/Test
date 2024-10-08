package com.sec.internal.helper;

import com.sec.internal.helper.SrvDnsResolver;
import java.util.List;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SrvDnsResolver$SrvRecordAnswerAccumulator$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ SrvDnsResolver.SrvRecordAnswerAccumulator f$0;
    public final /* synthetic */ List f$1;
    public final /* synthetic */ int f$2;

    public /* synthetic */ SrvDnsResolver$SrvRecordAnswerAccumulator$$ExternalSyntheticLambda1(SrvDnsResolver.SrvRecordAnswerAccumulator srvRecordAnswerAccumulator, List list, int i) {
        this.f$0 = srvRecordAnswerAccumulator;
        this.f$1 = list;
        this.f$2 = i;
    }

    public final void run() {
        this.f$0.lambda$onAnswer$0(this.f$1, this.f$2);
    }
}
