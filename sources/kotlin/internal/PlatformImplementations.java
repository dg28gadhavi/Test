package kotlin.internal;

import java.lang.reflect.Method;
import java.util.regex.MatchResult;
import kotlin.jvm.internal.Intrinsics;
import kotlin.random.FallbackThreadLocalRandom;
import kotlin.random.Random;
import kotlin.text.MatchGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PlatformImplementations.kt */
public class PlatformImplementations {

    /* compiled from: PlatformImplementations.kt */
    private static final class ReflectThrowable {
        @NotNull
        public static final ReflectThrowable INSTANCE = new ReflectThrowable();
        @Nullable
        public static final Method addSuppressed;
        @Nullable
        public static final Method getSuppressed;

        private ReflectThrowable() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:10:0x0040 A[LOOP:0: B:1:0x0016->B:10:0x0040, LOOP_END] */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0044 A[EDGE_INSN: B:21:0x0044->B:12:0x0044 ?: BREAK  , SYNTHETIC] */
        static {
            /*
                kotlin.internal.PlatformImplementations$ReflectThrowable r0 = new kotlin.internal.PlatformImplementations$ReflectThrowable
                r0.<init>()
                INSTANCE = r0
                java.lang.Class<java.lang.Throwable> r0 = java.lang.Throwable.class
                java.lang.reflect.Method[] r1 = r0.getMethods()
                java.lang.String r2 = "throwableMethods"
                kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r1, r2)
                int r2 = r1.length
                r3 = 0
                r4 = r3
            L_0x0016:
                r5 = 0
                if (r4 >= r2) goto L_0x0043
                r6 = r1[r4]
                java.lang.String r7 = r6.getName()
                java.lang.String r8 = "addSuppressed"
                boolean r7 = kotlin.jvm.internal.Intrinsics.areEqual(r7, r8)
                if (r7 == 0) goto L_0x003c
                java.lang.Class[] r7 = r6.getParameterTypes()
                java.lang.String r8 = "it.parameterTypes"
                kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r7, r8)
                java.lang.Object r7 = kotlin.collections.ArraysKt___ArraysKt.singleOrNull(r7)
                boolean r7 = kotlin.jvm.internal.Intrinsics.areEqual(r7, r0)
                if (r7 == 0) goto L_0x003c
                r7 = 1
                goto L_0x003d
            L_0x003c:
                r7 = r3
            L_0x003d:
                if (r7 == 0) goto L_0x0040
                goto L_0x0044
            L_0x0040:
                int r4 = r4 + 1
                goto L_0x0016
            L_0x0043:
                r6 = r5
            L_0x0044:
                addSuppressed = r6
                int r0 = r1.length
            L_0x0047:
                if (r3 >= r0) goto L_0x005c
                r2 = r1[r3]
                java.lang.String r4 = r2.getName()
                java.lang.String r6 = "getSuppressed"
                boolean r4 = kotlin.jvm.internal.Intrinsics.areEqual(r4, r6)
                if (r4 == 0) goto L_0x0059
                r5 = r2
                goto L_0x005c
            L_0x0059:
                int r3 = r3 + 1
                goto L_0x0047
            L_0x005c:
                getSuppressed = r5
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: kotlin.internal.PlatformImplementations.ReflectThrowable.<clinit>():void");
        }
    }

    public void addSuppressed(@NotNull Throwable th, @NotNull Throwable th2) {
        Intrinsics.checkNotNullParameter(th, "cause");
        Intrinsics.checkNotNullParameter(th2, "exception");
        Method method = ReflectThrowable.addSuppressed;
        if (method != null) {
            method.invoke(th, new Object[]{th2});
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0012, code lost:
        r1 = kotlin.collections.ArraysKt.asList((java.lang.Throwable[]) (r1 = r1.invoke(r2, new java.lang.Object[0])));
     */
    @org.jetbrains.annotations.NotNull
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.List<java.lang.Throwable> getSuppressed(@org.jetbrains.annotations.NotNull java.lang.Throwable r2) {
        /*
            r1 = this;
            java.lang.String r1 = "exception"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r2, r1)
            java.lang.reflect.Method r1 = kotlin.internal.PlatformImplementations.ReflectThrowable.getSuppressed
            if (r1 == 0) goto L_0x001a
            r0 = 0
            java.lang.Object[] r0 = new java.lang.Object[r0]
            java.lang.Object r1 = r1.invoke(r2, r0)
            if (r1 == 0) goto L_0x001a
            java.lang.Throwable[] r1 = (java.lang.Throwable[]) r1
            java.util.List r1 = kotlin.collections.ArraysKt___ArraysJvmKt.asList(r1)
            if (r1 != 0) goto L_0x001e
        L_0x001a:
            java.util.List r1 = kotlin.collections.CollectionsKt__CollectionsKt.emptyList()
        L_0x001e:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlin.internal.PlatformImplementations.getSuppressed(java.lang.Throwable):java.util.List");
    }

    @Nullable
    public MatchGroup getMatchResultNamedGroup(@NotNull MatchResult matchResult, @NotNull String str) {
        Intrinsics.checkNotNullParameter(matchResult, "matchResult");
        Intrinsics.checkNotNullParameter(str, "name");
        throw new UnsupportedOperationException("Retrieving groups by name is not supported on this platform.");
    }

    @NotNull
    public Random defaultPlatformRandom() {
        return new FallbackThreadLocalRandom();
    }
}
