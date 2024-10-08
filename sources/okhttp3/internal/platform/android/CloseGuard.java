package okhttp3.internal.platform.android;

import java.lang.reflect.Method;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: CloseGuard.kt */
public final class CloseGuard {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    @Nullable
    private final Method getMethod;
    @Nullable
    private final Method openMethod;
    @Nullable
    private final Method warnIfOpenMethod;

    public CloseGuard(@Nullable Method method, @Nullable Method method2, @Nullable Method method3) {
        this.getMethod = method;
        this.openMethod = method2;
        this.warnIfOpenMethod = method3;
    }

    @Nullable
    public final Object createAndOpen(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "closer");
        Method method = this.getMethod;
        if (method != null) {
            try {
                Object invoke = method.invoke((Object) null, new Object[0]);
                Method method2 = this.openMethod;
                Intrinsics.checkNotNull(method2);
                method2.invoke(invoke, new Object[]{str});
                return invoke;
            } catch (Exception unused) {
            }
        }
        return null;
    }

    public final boolean warnIfOpen(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        try {
            Method method = this.warnIfOpenMethod;
            Intrinsics.checkNotNull(method);
            method.invoke(obj, new Object[0]);
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    /* compiled from: CloseGuard.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @NotNull
        public final CloseGuard get() {
            Method method;
            Method method2;
            Method method3;
            try {
                Class<?> cls = Class.forName("dalvik.system.CloseGuard");
                method3 = cls.getMethod("get", new Class[0]);
                method2 = cls.getMethod("open", new Class[]{String.class});
                method = cls.getMethod("warnIfOpen", new Class[0]);
            } catch (Exception unused) {
                method3 = null;
                method = null;
                method2 = null;
            }
            return new CloseGuard(method3, method2, method);
        }
    }
}
