package okhttp3;

import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.httpclient.HttpController;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.Pair;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Headers;
import okhttp3.internal.Util;
import okhttp3.internal.http.HttpMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Request.kt */
public final class Request {
    @Nullable
    private final RequestBody body;
    @NotNull
    private final Headers headers;
    @Nullable
    private CacheControl lazyCacheControl;
    @NotNull
    private final String method;
    @NotNull
    private final Map<Class<?>, Object> tags;
    @NotNull
    private final HttpUrl url;

    public Request(@NotNull HttpUrl httpUrl, @NotNull String str, @NotNull Headers headers2, @Nullable RequestBody requestBody, @NotNull Map<Class<?>, ? extends Object> map) {
        Intrinsics.checkNotNullParameter(httpUrl, ImsConstants.FtDlParams.FT_DL_URL);
        Intrinsics.checkNotNullParameter(str, "method");
        Intrinsics.checkNotNullParameter(headers2, "headers");
        Intrinsics.checkNotNullParameter(map, "tags");
        this.url = httpUrl;
        this.method = str;
        this.headers = headers2;
        this.body = requestBody;
        this.tags = map;
    }

    @NotNull
    public final HttpUrl url() {
        return this.url;
    }

    @NotNull
    public final String method() {
        return this.method;
    }

    @NotNull
    public final Headers headers() {
        return this.headers;
    }

    @Nullable
    public final RequestBody body() {
        return this.body;
    }

    @NotNull
    public final Map<Class<?>, Object> getTags$okhttp() {
        return this.tags;
    }

    public final boolean isHttps() {
        return this.url.isHttps();
    }

    @Nullable
    public final String header(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "name");
        return this.headers.get(str);
    }

    @NotNull
    public final Builder newBuilder() {
        return new Builder(this);
    }

    @NotNull
    public final CacheControl cacheControl() {
        CacheControl cacheControl = this.lazyCacheControl;
        if (cacheControl != null) {
            return cacheControl;
        }
        CacheControl parse = CacheControl.Companion.parse(this.headers);
        this.lazyCacheControl = parse;
        return parse;
    }

    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Request{method=");
        sb.append(method());
        sb.append(", url=");
        sb.append(url());
        if (headers().size() != 0) {
            sb.append(", headers=[");
            int i = 0;
            for (Object next : headers()) {
                int i2 = i + 1;
                if (i < 0) {
                    CollectionsKt__CollectionsKt.throwIndexOverflow();
                }
                Pair pair = (Pair) next;
                String str = (String) pair.component1();
                String str2 = (String) pair.component2();
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(str);
                sb.append(':');
                sb.append(str2);
                i = i2;
            }
            sb.append(']');
        }
        if (!getTags$okhttp().isEmpty()) {
            sb.append(", tags=");
            sb.append(getTags$okhttp());
        }
        sb.append('}');
        String sb2 = sb.toString();
        Intrinsics.checkNotNullExpressionValue(sb2, "StringBuilder().apply(builderAction).toString()");
        return sb2;
    }

    /* compiled from: Request.kt */
    public static class Builder {
        @Nullable
        private RequestBody body;
        @NotNull
        private Headers.Builder headers;
        @NotNull
        private String method;
        @NotNull
        private Map<Class<?>, Object> tags;
        @Nullable
        private HttpUrl url;

        @NotNull
        public final Builder delete() {
            return delete$default(this, (RequestBody) null, 1, (Object) null);
        }

        public final void setUrl$okhttp(@Nullable HttpUrl httpUrl) {
            this.url = httpUrl;
        }

        public final void setMethod$okhttp(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "<set-?>");
            this.method = str;
        }

        @NotNull
        public final Headers.Builder getHeaders$okhttp() {
            return this.headers;
        }

        public final void setHeaders$okhttp(@NotNull Headers.Builder builder) {
            Intrinsics.checkNotNullParameter(builder, "<set-?>");
            this.headers = builder;
        }

        public final void setBody$okhttp(@Nullable RequestBody requestBody) {
            this.body = requestBody;
        }

        public Builder() {
            this.tags = new LinkedHashMap();
            this.method = "GET";
            this.headers = new Headers.Builder();
        }

        public Builder(@NotNull Request request) {
            Map<Class<?>, Object> map;
            Intrinsics.checkNotNullParameter(request, "request");
            this.tags = new LinkedHashMap();
            this.url = request.url();
            this.method = request.method();
            this.body = request.body();
            if (request.getTags$okhttp().isEmpty()) {
                map = new LinkedHashMap<>();
            } else {
                map = MapsKt__MapsKt.toMutableMap(request.getTags$okhttp());
            }
            this.tags = map;
            this.headers = request.headers().newBuilder();
        }

        @NotNull
        public Builder url(@NotNull HttpUrl httpUrl) {
            Intrinsics.checkNotNullParameter(httpUrl, ImsConstants.FtDlParams.FT_DL_URL);
            setUrl$okhttp(httpUrl);
            return this;
        }

        @NotNull
        public Builder url(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, ImsConstants.FtDlParams.FT_DL_URL);
            if (StringsKt__StringsJVMKt.startsWith(str, "ws:", true)) {
                String substring = str.substring(3);
                Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String).substring(startIndex)");
                str = Intrinsics.stringPlus("http:", substring);
            } else if (StringsKt__StringsJVMKt.startsWith(str, "wss:", true)) {
                String substring2 = str.substring(4);
                Intrinsics.checkNotNullExpressionValue(substring2, "this as java.lang.String).substring(startIndex)");
                str = Intrinsics.stringPlus("https:", substring2);
            }
            return url(HttpUrl.Companion.get(str));
        }

        @NotNull
        public Builder header(@NotNull String str, @NotNull String str2) {
            Intrinsics.checkNotNullParameter(str, "name");
            Intrinsics.checkNotNullParameter(str2, "value");
            getHeaders$okhttp().set(str, str2);
            return this;
        }

        @NotNull
        public Builder removeHeader(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "name");
            getHeaders$okhttp().removeAll(str);
            return this;
        }

        @NotNull
        public Builder headers(@NotNull Headers headers2) {
            Intrinsics.checkNotNullParameter(headers2, "headers");
            setHeaders$okhttp(headers2.newBuilder());
            return this;
        }

        @NotNull
        public Builder head() {
            return method(HttpController.METHOD_HEAD, (RequestBody) null);
        }

        public static /* synthetic */ Builder delete$default(Builder builder, RequestBody requestBody, int i, Object obj) {
            if (obj == null) {
                if ((i & 1) != 0) {
                    requestBody = Util.EMPTY_REQUEST;
                }
                return builder.delete(requestBody);
            }
            throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: delete");
        }

        @NotNull
        public Builder delete(@Nullable RequestBody requestBody) {
            return method(HttpController.METHOD_DELETE, requestBody);
        }

        @NotNull
        public Builder method(@NotNull String str, @Nullable RequestBody requestBody) {
            Intrinsics.checkNotNullParameter(str, "method");
            if (str.length() > 0) {
                if (requestBody == null) {
                    if (!(true ^ HttpMethod.requiresRequestBody(str))) {
                        throw new IllegalArgumentException(("method " + str + " must have a request body.").toString());
                    }
                } else if (!HttpMethod.permitsRequestBody(str)) {
                    throw new IllegalArgumentException(("method " + str + " must not have a request body.").toString());
                }
                setMethod$okhttp(str);
                setBody$okhttp(requestBody);
                return this;
            }
            throw new IllegalArgumentException("method.isEmpty() == true".toString());
        }

        @NotNull
        public Request build() {
            HttpUrl httpUrl = this.url;
            if (httpUrl != null) {
                return new Request(httpUrl, this.method, this.headers.build(), this.body, Util.toImmutableMap(this.tags));
            }
            throw new IllegalStateException("url == null".toString());
        }
    }
}
