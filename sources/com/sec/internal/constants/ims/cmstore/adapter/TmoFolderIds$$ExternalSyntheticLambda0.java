package com.sec.internal.constants.ims.cmstore.adapter;

import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class TmoFolderIds$$ExternalSyntheticLambda0 implements Predicate {
    public final /* synthetic */ String f$0;

    public /* synthetic */ TmoFolderIds$$ExternalSyntheticLambda0(String str) {
        this.f$0 = str;
    }

    public final boolean test(Object obj) {
        return ((TmoFolderIds) obj).getValue().equals(this.f$0);
    }
}
