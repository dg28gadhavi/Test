package okhttp3.internal.concurrent;

import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Task.kt */
public abstract class Task {
    private final boolean cancelable;
    @NotNull
    private final String name;
    private long nextExecuteNanoTime;
    @Nullable
    private TaskQueue queue;

    public abstract long runOnce();

    public Task(@NotNull String str, boolean z) {
        Intrinsics.checkNotNullParameter(str, "name");
        this.name = str;
        this.cancelable = z;
        this.nextExecuteNanoTime = -1;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ Task(String str, boolean z, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(str, (i & 2) != 0 ? true : z);
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    public final boolean getCancelable() {
        return this.cancelable;
    }

    @Nullable
    public final TaskQueue getQueue$okhttp() {
        return this.queue;
    }

    public final long getNextExecuteNanoTime$okhttp() {
        return this.nextExecuteNanoTime;
    }

    public final void setNextExecuteNanoTime$okhttp(long j) {
        this.nextExecuteNanoTime = j;
    }

    public final void initQueue$okhttp(@NotNull TaskQueue taskQueue) {
        Intrinsics.checkNotNullParameter(taskQueue, "queue");
        TaskQueue taskQueue2 = this.queue;
        if (taskQueue2 != taskQueue) {
            if (taskQueue2 == null) {
                this.queue = taskQueue;
                return;
            }
            throw new IllegalStateException("task is in multiple queues".toString());
        }
    }

    @NotNull
    public String toString() {
        return this.name;
    }
}
