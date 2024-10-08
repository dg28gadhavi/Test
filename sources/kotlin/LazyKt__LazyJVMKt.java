package kotlin;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: LazyJVM.kt */
class LazyKt__LazyJVMKt {
    @NotNull
    public static <T> Lazy<T> lazy(@NotNull Function0<? extends T> function0) {
        Intrinsics.checkNotNullParameter(function0, "initializer");
        return new SynchronizedLazyImpl(function0, (Object) null, 2, (DefaultConstructorMarker) null);
    }
}
