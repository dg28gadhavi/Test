package com.sec.internal.ims.aec.util;

import com.sec.internal.ims.aec.util.PsDataOffExempt;
import java.util.List;
import okhttp3.Dns;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class PsDataOffExempt$NetworkCallback$$ExternalSyntheticLambda0 implements Dns {
    public final /* synthetic */ PsDataOffExempt.NetworkCallback f$0;

    public /* synthetic */ PsDataOffExempt$NetworkCallback$$ExternalSyntheticLambda0(PsDataOffExempt.NetworkCallback networkCallback) {
        this.f$0 = networkCallback;
    }

    public final List lookup(String str) {
        return this.f$0.lambda$onAvailable$0(str);
    }
}
