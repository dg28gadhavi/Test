package com.sec.internal.ims.servicemodules.im;

import java.util.Comparator;
import java.util.Map;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImSessionDefaultState$$ExternalSyntheticLambda1 implements Comparator {
    public final /* synthetic */ Map f$0;

    public /* synthetic */ ImSessionDefaultState$$ExternalSyntheticLambda1(Map map) {
        this.f$0 = map;
    }

    public final int compare(Object obj, Object obj2) {
        return ((Integer) this.f$0.get((String) obj)).compareTo((Integer) this.f$0.get((String) obj2));
    }
}
