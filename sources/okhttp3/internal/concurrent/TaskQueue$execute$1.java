package okhttp3.internal.concurrent;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/* compiled from: TaskQueue.kt */
public final class TaskQueue$execute$1 extends Task {
    final /* synthetic */ Function0<Unit> $block;
    final /* synthetic */ boolean $cancelable;
    final /* synthetic */ String $name;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public TaskQueue$execute$1(String str, boolean z, Function0<Unit> function0) {
        super(str, z);
        this.$name = str;
        this.$cancelable = z;
        this.$block = function0;
    }

    public long runOnce() {
        this.$block.invoke();
        return -1;
    }
}
