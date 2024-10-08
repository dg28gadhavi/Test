package okhttp3.internal.platform.android;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: AndroidLog.kt */
public final class AndroidLogHandler extends Handler {
    @NotNull
    public static final AndroidLogHandler INSTANCE = new AndroidLogHandler();

    public void close() {
    }

    public void flush() {
    }

    private AndroidLogHandler() {
    }

    public void publish(@NotNull LogRecord logRecord) {
        Intrinsics.checkNotNullParameter(logRecord, "record");
        AndroidLog androidLog = AndroidLog.INSTANCE;
        String loggerName = logRecord.getLoggerName();
        Intrinsics.checkNotNullExpressionValue(loggerName, "record.loggerName");
        int access$getAndroidLevel = AndroidLogKt.getAndroidLevel(logRecord);
        String message = logRecord.getMessage();
        Intrinsics.checkNotNullExpressionValue(message, "record.message");
        androidLog.androidLog$okhttp(loggerName, access$getAndroidLevel, message, logRecord.getThrown());
    }
}
