package com.sec.internal.ims.settings;

import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsSimMobilityUpdate$$ExternalSyntheticLambda1 implements Predicate {
    public final /* synthetic */ String f$0;
    public final /* synthetic */ String f$1;

    public /* synthetic */ ImsSimMobilityUpdate$$ExternalSyntheticLambda1(String str, String str2) {
        this.f$0 = str;
        this.f$1 = str2;
    }

    public final boolean test(Object obj) {
        return ImsSimMobilityUpdate.lambda$checkAllowListForSimMobility$1(this.f$0, this.f$1, (String) obj);
    }
}
