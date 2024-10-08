package com.sec.internal.helper;

import android.net.DnsResolver;
import com.sec.internal.helper.SrvDnsResolver;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SrvDnsResolver$SrvRecordAnswerAccumulator$$ExternalSyntheticLambda3 implements Runnable {
    public final /* synthetic */ SrvDnsResolver.SrvRecordAnswerAccumulator f$0;
    public final /* synthetic */ DnsResolver.DnsException f$1;

    public /* synthetic */ SrvDnsResolver$SrvRecordAnswerAccumulator$$ExternalSyntheticLambda3(SrvDnsResolver.SrvRecordAnswerAccumulator srvRecordAnswerAccumulator, DnsResolver.DnsException dnsException) {
        this.f$0 = srvRecordAnswerAccumulator;
        this.f$1 = dnsException;
    }

    public final void run() {
        this.f$0.lambda$onError$2(this.f$1);
    }
}
