package kotlin;

import java.io.Serializable;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: LazyJVM.kt */
final class SynchronizedLazyImpl<T> implements Lazy<T>, Serializable {
    @Nullable
    private volatile Object _value;
    @Nullable
    private Function0<? extends T> initializer;
    @NotNull
    private final Object lock;

    public SynchronizedLazyImpl(@NotNull Function0<? extends T> function0, @Nullable Object obj) {
        Intrinsics.checkNotNullParameter(function0, "initializer");
        this.initializer = function0;
        this._value = UNINITIALIZED_VALUE.INSTANCE;
        this.lock = obj == null ? this : obj;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ SynchronizedLazyImpl(Function0 function0, Object obj, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(function0, (i & 2) != 0 ? null : obj);
    }

    public T getValue() {
        T t;
        T t2 = this._value;
        T t3 = UNINITIALIZED_VALUE.INSTANCE;
        if (t2 != t3) {
            return t2;
        }
        synchronized (this.lock) {
            t = this._value;
            if (t == t3) {
                Function0 function0 = this.initializer;
                Intrinsics.checkNotNull(function0);
                t = function0.invoke();
                this._value = t;
                this.initializer = null;
            }
        }
        return t;
    }

    public boolean isInitialized() {
        return this._value != UNINITIALIZED_VALUE.INSTANCE;
    }

    @NotNull
    public String toString() {
        return isInitialized() ? String.valueOf(getValue()) : "Lazy value not initialized yet.";
    }

    private final Object writeReplace() {
        return new InitializedLazyImpl(getValue());
    }
}
