package okhttp3.internal.http;

import com.sec.internal.helper.httpclient.HttpController;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: HttpMethod.kt */
public final class HttpMethod {
    @NotNull
    public static final HttpMethod INSTANCE = new HttpMethod();

    private HttpMethod() {
    }

    public static final boolean requiresRequestBody(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "method");
        return Intrinsics.areEqual(str, "POST") || Intrinsics.areEqual(str, "PUT") || Intrinsics.areEqual(str, "PATCH") || Intrinsics.areEqual(str, "PROPPATCH") || Intrinsics.areEqual(str, "REPORT");
    }

    public static final boolean permitsRequestBody(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "method");
        return !Intrinsics.areEqual(str, "GET") && !Intrinsics.areEqual(str, HttpController.METHOD_HEAD);
    }

    public final boolean redirectsWithBody(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "method");
        return Intrinsics.areEqual(str, "PROPFIND");
    }

    public final boolean redirectsToGet(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "method");
        return !Intrinsics.areEqual(str, "PROPFIND");
    }
}
