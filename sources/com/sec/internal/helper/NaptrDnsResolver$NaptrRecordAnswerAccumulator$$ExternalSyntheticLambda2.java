package com.sec.internal.helper;

import android.net.DnsResolver;
import com.sec.internal.helper.NaptrDnsResolver;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class NaptrDnsResolver$NaptrRecordAnswerAccumulator$$ExternalSyntheticLambda2 implements Runnable {
    public final /* synthetic */ NaptrDnsResolver.NaptrRecordAnswerAccumulator f$0;
    public final /* synthetic */ DnsResolver.DnsException f$1;

    public /* synthetic */ NaptrDnsResolver$NaptrRecordAnswerAccumulator$$ExternalSyntheticLambda2(NaptrDnsResolver.NaptrRecordAnswerAccumulator naptrRecordAnswerAccumulator, DnsResolver.DnsException dnsException) {
        this.f$0 = naptrRecordAnswerAccumulator;
        this.f$1 = dnsException;
    }

    public final void run() {
        this.f$0.lambda$onError$2(this.f$1);
    }
}
