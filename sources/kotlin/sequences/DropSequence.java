package kotlin.sequences;

import java.util.Iterator;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Sequences.kt */
public final class DropSequence<T> implements Sequence<T>, DropTakeSequence<T> {
    /* access modifiers changed from: private */
    public final int count;
    /* access modifiers changed from: private */
    @NotNull
    public final Sequence<T> sequence;

    public DropSequence(@NotNull Sequence<? extends T> sequence2, int i) {
        Intrinsics.checkNotNullParameter(sequence2, "sequence");
        this.sequence = sequence2;
        this.count = i;
        if (!(i >= 0)) {
            throw new IllegalArgumentException(("count must be non-negative, but was " + i + '.').toString());
        }
    }

    @NotNull
    public Sequence<T> drop(int i) {
        int i2 = this.count + i;
        return i2 < 0 ? new DropSequence(this, i) : new DropSequence(this.sequence, i2);
    }

    @NotNull
    public Iterator<T> iterator() {
        return new DropSequence$iterator$1(this);
    }
}
