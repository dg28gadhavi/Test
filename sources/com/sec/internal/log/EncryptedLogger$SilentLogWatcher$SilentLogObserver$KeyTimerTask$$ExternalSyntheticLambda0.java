package com.sec.internal.log;

import java.nio.file.Path;
import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class EncryptedLogger$SilentLogWatcher$SilentLogObserver$KeyTimerTask$$ExternalSyntheticLambda0 implements Predicate {
    public final boolean test(Object obj) {
        return ((Path) obj).toString().contains("main");
    }
}
