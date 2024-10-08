package com.sec.internal.helper;

import com.sec.internal.helper.NaptrDnsResolver;
import java.util.List;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class NaptrDnsResolver$NaptrRecordAnswerAccumulator$$ExternalSyntheticLambda0 implements Runnable {
    public final /* synthetic */ NaptrDnsResolver.NaptrRecordAnswerAccumulator f$0;
    public final /* synthetic */ List f$1;
    public final /* synthetic */ int f$2;

    public /* synthetic */ NaptrDnsResolver$NaptrRecordAnswerAccumulator$$ExternalSyntheticLambda0(NaptrDnsResolver.NaptrRecordAnswerAccumulator naptrRecordAnswerAccumulator, List list, int i) {
        this.f$0 = naptrRecordAnswerAccumulator;
        this.f$1 = list;
        this.f$2 = i;
    }

    public final void run() {
        this.f$0.lambda$onAnswer$0(this.f$1, this.f$2);
    }
}
