package com.sec.internal.constants.ims;

import java.util.function.BiConsumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class SipMsg$$ExternalSyntheticLambda35 implements BiConsumer {
    public final void accept(Object obj, Object obj2) {
        SipMsg.SERVICE_TO_TAG_LIST.computeIfAbsent((String) obj2, new SipMsg$$ExternalSyntheticLambda29()).add((String) obj);
    }
}
