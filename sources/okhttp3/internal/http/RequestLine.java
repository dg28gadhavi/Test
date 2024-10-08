package okhttp3.internal.http;

import com.sec.internal.constants.ims.ImsConstants;
import java.net.Proxy;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;

/* compiled from: RequestLine.kt */
public final class RequestLine {
    @NotNull
    public static final RequestLine INSTANCE = new RequestLine();

    private RequestLine() {
    }

    @NotNull
    public final String get(@NotNull Request request, @NotNull Proxy.Type type) {
        Intrinsics.checkNotNullParameter(request, "request");
        Intrinsics.checkNotNullParameter(type, "proxyType");
        StringBuilder sb = new StringBuilder();
        sb.append(request.method());
        sb.append(' ');
        RequestLine requestLine = INSTANCE;
        if (requestLine.includeAuthorityInRequestLine(request, type)) {
            sb.append(request.url());
        } else {
            sb.append(requestLine.requestPath(request.url()));
        }
        sb.append(" HTTP/1.1");
        String sb2 = sb.toString();
        Intrinsics.checkNotNullExpressionValue(sb2, "StringBuilder().apply(builderAction).toString()");
        return sb2;
    }

    private final boolean includeAuthorityInRequestLine(Request request, Proxy.Type type) {
        return !request.isHttps() && type == Proxy.Type.HTTP;
    }

    @NotNull
    public final String requestPath(@NotNull HttpUrl httpUrl) {
        Intrinsics.checkNotNullParameter(httpUrl, ImsConstants.FtDlParams.FT_DL_URL);
        String encodedPath = httpUrl.encodedPath();
        String encodedQuery = httpUrl.encodedQuery();
        if (encodedQuery == null) {
            return encodedPath;
        }
        return encodedPath + '?' + encodedQuery;
    }
}
