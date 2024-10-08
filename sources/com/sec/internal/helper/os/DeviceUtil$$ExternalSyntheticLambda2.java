package com.sec.internal.helper.os;

import java.util.Locale;
import java.util.function.Function;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class DeviceUtil$$ExternalSyntheticLambda2 implements Function {
    public final Object apply(Object obj) {
        return String.format(Locale.US, ";svn=%02d", new Object[]{(Integer) obj});
    }
}
