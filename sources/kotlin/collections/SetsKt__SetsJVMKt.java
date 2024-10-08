package kotlin.collections;

import java.util.Collections;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: SetsJVM.kt */
class SetsKt__SetsJVMKt {
    @NotNull
    public static final <T> Set<T> setOf(T t) {
        Set<T> singleton = Collections.singleton(t);
        Intrinsics.checkNotNullExpressionValue(singleton, "singleton(element)");
        return singleton;
    }
}
