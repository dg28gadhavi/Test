package okhttp3.internal.platform.android;

import android.util.Log;
import com.sec.internal.helper.ImsCallUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.OkHttpClient;
import okhttp3.internal.concurrent.TaskRunner;
import okhttp3.internal.http2.Http2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: AndroidLog.kt */
public final class AndroidLog {
    @NotNull
    public static final AndroidLog INSTANCE = new AndroidLog();
    @NotNull
    private static final CopyOnWriteArraySet<Logger> configuredLoggers = new CopyOnWriteArraySet<>();
    @NotNull
    private static final Map<String, String> knownLoggers;

    private AndroidLog() {
    }

    static {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        Package packageR = OkHttpClient.class.getPackage();
        String name = packageR == null ? null : packageR.getName();
        if (name != null) {
            linkedHashMap.put(name, "OkHttp");
        }
        String name2 = OkHttpClient.class.getName();
        Intrinsics.checkNotNullExpressionValue(name2, "OkHttpClient::class.java.name");
        linkedHashMap.put(name2, "okhttp.OkHttpClient");
        String name3 = Http2.class.getName();
        Intrinsics.checkNotNullExpressionValue(name3, "Http2::class.java.name");
        linkedHashMap.put(name3, "okhttp.Http2");
        String name4 = TaskRunner.class.getName();
        Intrinsics.checkNotNullExpressionValue(name4, "TaskRunner::class.java.name");
        linkedHashMap.put(name4, "okhttp.TaskRunner");
        linkedHashMap.put("okhttp3.mockwebserver.MockWebServer", "okhttp.MockWebServer");
        knownLoggers = MapsKt__MapsKt.toMap(linkedHashMap);
    }

    public final void androidLog$okhttp(@NotNull String str, int i, @NotNull String str2, @Nullable Throwable th) {
        int min;
        Intrinsics.checkNotNullParameter(str, "loggerName");
        Intrinsics.checkNotNullParameter(str2, "message");
        String loggerTag = loggerTag(str);
        if (Log.isLoggable(loggerTag, i)) {
            if (th != null) {
                str2 = str2 + 10 + Log.getStackTraceString(th);
            }
            int length = str2.length();
            int i2 = 0;
            while (i2 < length) {
                int indexOf$default = StringsKt__StringsKt.indexOf$default((CharSequence) str2, 10, i2, false, 4, (Object) null);
                if (indexOf$default == -1) {
                    indexOf$default = length;
                }
                while (true) {
                    min = Math.min(indexOf$default, i2 + ImsCallUtil.DEFAULT_LOCATION_ACQUIRE_TIME);
                    String substring = str2.substring(i2, min);
                    Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
                    Log.println(i, loggerTag, substring);
                    if (min >= indexOf$default) {
                        break;
                    }
                    i2 = min;
                }
                i2 = min + 1;
            }
        }
    }

    private final String loggerTag(String str) {
        String str2 = knownLoggers.get(str);
        return str2 == null ? StringsKt___StringsKt.take(str, 23) : str2;
    }

    public final void enable() {
        for (Map.Entry next : knownLoggers.entrySet()) {
            enableLogging((String) next.getKey(), (String) next.getValue());
        }
    }

    private final void enableLogging(String str, String str2) {
        Level level;
        Logger logger = Logger.getLogger(str);
        if (configuredLoggers.add(logger)) {
            logger.setUseParentHandlers(false);
            if (Log.isLoggable(str2, 3)) {
                level = Level.FINE;
            } else if (Log.isLoggable(str2, 4)) {
                level = Level.INFO;
            } else {
                level = Level.WARNING;
            }
            logger.setLevel(level);
            logger.addHandler(AndroidLogHandler.INSTANCE);
        }
    }
}
