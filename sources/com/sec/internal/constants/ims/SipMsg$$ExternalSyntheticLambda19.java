package com.sec.internal.constants.ims;

import com.sec.internal.log.IMSLog;
import java.util.Locale;
import java.util.function.Supplier;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipMsg$$ExternalSyntheticLambda19 implements Supplier {
    public final /* synthetic */ String f$0;

    public /* synthetic */ SipMsg$$ExternalSyntheticLambda19(String str) {
        this.f$0 = str;
    }

    public final Object get() {
        return IMSLog.e("SipMsg", String.format(Locale.US, "getAuthenticate: No %s header!", new Object[]{this.f$0}));
    }
}
