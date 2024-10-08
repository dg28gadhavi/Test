package kotlin.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.markers.KMappedMarker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Maps.kt */
final class EmptyMap implements Map, Serializable, KMappedMarker {
    @NotNull
    public static final EmptyMap INSTANCE = new EmptyMap();
    private static final long serialVersionUID = 8246714829545688274L;

    public void clear() {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    public boolean containsKey(@Nullable Object obj) {
        return false;
    }

    public boolean containsValue(@NotNull Void voidR) {
        Intrinsics.checkNotNullParameter(voidR, "value");
        return false;
    }

    @Nullable
    public Void get(@Nullable Object obj) {
        return null;
    }

    public int getSize() {
        return 0;
    }

    public int hashCode() {
        return 0;
    }

    public boolean isEmpty() {
        return true;
    }

    public /* bridge */ /* synthetic */ Object put(Object obj, Object obj2) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    public void putAll(Map map) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    public Void remove(Object obj) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    @NotNull
    public String toString() {
        return "{}";
    }

    private EmptyMap() {
    }

    public final /* bridge */ boolean containsValue(Object obj) {
        if (!(obj instanceof Void)) {
            return false;
        }
        return containsValue((Void) obj);
    }

    public final /* bridge */ Set<Map.Entry> entrySet() {
        return getEntries();
    }

    public final /* bridge */ Set<Object> keySet() {
        return getKeys();
    }

    public final /* bridge */ int size() {
        return getSize();
    }

    public final /* bridge */ Collection values() {
        return getValues();
    }

    public boolean equals(@Nullable Object obj) {
        return (obj instanceof Map) && ((Map) obj).isEmpty();
    }

    @NotNull
    public Set<Map.Entry> getEntries() {
        return EmptySet.INSTANCE;
    }

    @NotNull
    public Set<Object> getKeys() {
        return EmptySet.INSTANCE;
    }

    @NotNull
    public Collection getValues() {
        return EmptyList.INSTANCE;
    }

    private final Object readResolve() {
        return INSTANCE;
    }
}
