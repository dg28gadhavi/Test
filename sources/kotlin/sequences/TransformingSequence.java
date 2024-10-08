package kotlin.sequences;

import java.util.Iterator;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Sequences.kt */
public final class TransformingSequence<T, R> implements Sequence<R> {
    /* access modifiers changed from: private */
    @NotNull
    public final Sequence<T> sequence;
    /* access modifiers changed from: private */
    @NotNull
    public final Function1<T, R> transformer;

    public TransformingSequence(@NotNull Sequence<? extends T> sequence2, @NotNull Function1<? super T, ? extends R> function1) {
        Intrinsics.checkNotNullParameter(sequence2, "sequence");
        Intrinsics.checkNotNullParameter(function1, "transformer");
        this.sequence = sequence2;
        this.transformer = function1;
    }

    @NotNull
    public Iterator<R> iterator() {
        return new TransformingSequence$iterator$1(this);
    }
}
