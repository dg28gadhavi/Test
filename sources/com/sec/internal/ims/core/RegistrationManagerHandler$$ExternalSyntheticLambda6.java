package com.sec.internal.ims.core;

import com.sec.internal.ims.util.ConfigUtil;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationManagerHandler$$ExternalSyntheticLambda6 implements Predicate {
    public final boolean test(Object obj) {
        return ConfigUtil.isRcsEur(((RegisterTask) obj).getMno());
    }
}
