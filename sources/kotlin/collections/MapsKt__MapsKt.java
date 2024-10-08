package kotlin.collections;

import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Maps.kt */
class MapsKt__MapsKt extends MapsKt__MapsJVMKt {
    @NotNull
    public static <K, V> Map<K, V> emptyMap() {
        return EmptyMap.INSTANCE;
    }

    @NotNull
    public static <K, V> Map<K, V> toMap(@NotNull Map<? extends K, ? extends V> map) {
        Intrinsics.checkNotNullParameter(map, "<this>");
        int size = map.size();
        if (size == 0) {
            return emptyMap();
        }
        if (size != 1) {
            return toMutableMap(map);
        }
        return MapsKt__MapsJVMKt.toSingletonMap(map);
    }

    @NotNull
    public static <K, V> Map<K, V> toMutableMap(@NotNull Map<? extends K, ? extends V> map) {
        Intrinsics.checkNotNullParameter(map, "<this>");
        return new LinkedHashMap(map);
    }
}
