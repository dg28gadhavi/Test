package kotlin.collections;

import java.util.Collection;
import java.util.Iterator;
import kotlin.jvm.internal.ArrayIteratorKt;
import kotlin.jvm.internal.CollectionToArray;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.markers.KMappedMarker;
import org.jetbrains.annotations.NotNull;

/* compiled from: Collections.kt */
final class ArrayAsCollection<T> implements Collection<T>, KMappedMarker {
    private final boolean isVarargs;
    @NotNull
    private final T[] values;

    public boolean add(T t) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    public boolean addAll(Collection<? extends T> collection) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    public void clear() {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    public boolean remove(Object obj) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    public boolean removeAll(Collection<? extends Object> collection) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    public boolean retainAll(Collection<? extends Object> collection) {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    public <T> T[] toArray(T[] tArr) {
        Intrinsics.checkNotNullParameter(tArr, "array");
        return CollectionToArray.toArray(this, tArr);
    }

    public ArrayAsCollection(@NotNull T[] tArr, boolean z) {
        Intrinsics.checkNotNullParameter(tArr, "values");
        this.values = tArr;
        this.isVarargs = z;
    }

    public final /* bridge */ int size() {
        return getSize();
    }

    public int getSize() {
        return this.values.length;
    }

    public boolean isEmpty() {
        return this.values.length == 0;
    }

    public boolean contains(Object obj) {
        return ArraysKt___ArraysKt.contains(this.values, obj);
    }

    @NotNull
    public Iterator<T> iterator() {
        return ArrayIteratorKt.iterator(this.values);
    }

    @NotNull
    public final Object[] toArray() {
        return CollectionsKt__CollectionsJVMKt.copyToArrayOfAny(this.values, this.isVarargs);
    }

    public boolean containsAll(@NotNull Collection<? extends Object> collection) {
        Intrinsics.checkNotNullParameter(collection, "elements");
        if (collection.isEmpty()) {
            return true;
        }
        Iterator<T> it = collection.iterator();
        while (it.hasNext()) {
            if (!contains(it.next())) {
                return false;
            }
        }
        return true;
    }
}
