package com.sec.internal.ims.aec;

import android.os.Build;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class AECModule$$ExternalSyntheticLambda0 implements Predicate {
    public final boolean test(Object obj) {
        return Build.MODEL.startsWith((String) obj);
    }
}
