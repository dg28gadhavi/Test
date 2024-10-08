package okhttp3;

import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import java.io.Closeable;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Headers;
import okhttp3.internal.connection.Exchange;
import okhttp3.internal.http.HttpHeaders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Response.kt */
public final class Response implements Closeable {
    @Nullable
    private final ResponseBody body;
    @Nullable
    private final Response cacheResponse;
    private final int code;
    @Nullable
    private final Exchange exchange;
    @Nullable
    private final Handshake handshake;
    @NotNull
    private final Headers headers;
    @Nullable
    private CacheControl lazyCacheControl;
    @NotNull
    private final String message;
    @Nullable
    private final Response networkResponse;
    @Nullable
    private final Response priorResponse;
    @NotNull
    private final Protocol protocol;
    private final long receivedResponseAtMillis;
    @NotNull
    private final Request request;
    private final long sentRequestAtMillis;

    public Response(@NotNull Request request2, @NotNull Protocol protocol2, @NotNull String str, int i, @Nullable Handshake handshake2, @NotNull Headers headers2, @Nullable ResponseBody responseBody, @Nullable Response response, @Nullable Response response2, @Nullable Response response3, long j, long j2, @Nullable Exchange exchange2) {
        Intrinsics.checkNotNullParameter(request2, "request");
        Intrinsics.checkNotNullParameter(protocol2, CloudMessageProviderContract.BufferDBSMS.PROTOCOL);
        Intrinsics.checkNotNullParameter(str, "message");
        Intrinsics.checkNotNullParameter(headers2, "headers");
        this.request = request2;
        this.protocol = protocol2;
        this.message = str;
        this.code = i;
        this.handshake = handshake2;
        this.headers = headers2;
        this.body = responseBody;
        this.networkResponse = response;
        this.cacheResponse = response2;
        this.priorResponse = response3;
        this.sentRequestAtMillis = j;
        this.receivedResponseAtMillis = j2;
        this.exchange = exchange2;
    }

    @NotNull
    public final Request request() {
        return this.request;
    }

    @NotNull
    public final Protocol protocol() {
        return this.protocol;
    }

    @NotNull
    public final String message() {
        return this.message;
    }

    public final int code() {
        return this.code;
    }

    @Nullable
    public final Handshake handshake() {
        return this.handshake;
    }

    @NotNull
    public final Headers headers() {
        return this.headers;
    }

    @Nullable
    public final ResponseBody body() {
        return this.body;
    }

    @Nullable
    public final Response networkResponse() {
        return this.networkResponse;
    }

    @Nullable
    public final Response cacheResponse() {
        return this.cacheResponse;
    }

    @Nullable
    public final Response priorResponse() {
        return this.priorResponse;
    }

    public final long sentRequestAtMillis() {
        return this.sentRequestAtMillis;
    }

    public final long receivedResponseAtMillis() {
        return this.receivedResponseAtMillis;
    }

    @Nullable
    public final Exchange exchange() {
        return this.exchange;
    }

    @NotNull
    public final List<String> headers(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "name");
        return this.headers.values(str);
    }

    public static /* synthetic */ String header$default(Response response, String str, String str2, int i, Object obj) {
        if ((i & 2) != 0) {
            str2 = null;
        }
        return response.header(str, str2);
    }

    @Nullable
    public final String header(@NotNull String str, @Nullable String str2) {
        Intrinsics.checkNotNullParameter(str, "name");
        String str3 = this.headers.get(str);
        return str3 == null ? str2 : str3;
    }

    @NotNull
    public final Builder newBuilder() {
        return new Builder(this);
    }

    @NotNull
    public final List<Challenge> challenges() {
        String str;
        Headers headers2 = this.headers;
        int i = this.code;
        if (i == 401) {
            str = "WWW-Authenticate";
        } else if (i != 407) {
            return CollectionsKt__CollectionsKt.emptyList();
        } else {
            str = "Proxy-Authenticate";
        }
        return HttpHeaders.parseChallenges(headers2, str);
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

    public void close() {
        ResponseBody responseBody = this.body;
        if (responseBody != null) {
            responseBody.close();
            return;
        }
        throw new IllegalStateException("response is not eligible for a body and must not be closed".toString());
    }

    @NotNull
    public String toString() {
        return "Response{protocol=" + this.protocol + ", code=" + this.code + ", message=" + this.message + ", url=" + this.request.url() + '}';
    }

    /* compiled from: Response.kt */
    public static class Builder {
        @Nullable
        private ResponseBody body;
        @Nullable
        private Response cacheResponse;
        private int code;
        @Nullable
        private Exchange exchange;
        @Nullable
        private Handshake handshake;
        @NotNull
        private Headers.Builder headers;
        @Nullable
        private String message;
        @Nullable
        private Response networkResponse;
        @Nullable
        private Response priorResponse;
        @Nullable
        private Protocol protocol;
        private long receivedResponseAtMillis;
        @Nullable
        private Request request;
        private long sentRequestAtMillis;

        public final void setRequest$okhttp(@Nullable Request request2) {
            this.request = request2;
        }

        public final void setProtocol$okhttp(@Nullable Protocol protocol2) {
            this.protocol = protocol2;
        }

        public final int getCode$okhttp() {
            return this.code;
        }

        public final void setCode$okhttp(int i) {
            this.code = i;
        }

        public final void setMessage$okhttp(@Nullable String str) {
            this.message = str;
        }

        public final void setHandshake$okhttp(@Nullable Handshake handshake2) {
            this.handshake = handshake2;
        }

        @NotNull
        public final Headers.Builder getHeaders$okhttp() {
            return this.headers;
        }

        public final void setHeaders$okhttp(@NotNull Headers.Builder builder) {
            Intrinsics.checkNotNullParameter(builder, "<set-?>");
            this.headers = builder;
        }

        public final void setBody$okhttp(@Nullable ResponseBody responseBody) {
            this.body = responseBody;
        }

        public final void setNetworkResponse$okhttp(@Nullable Response response) {
            this.networkResponse = response;
        }

        public final void setCacheResponse$okhttp(@Nullable Response response) {
            this.cacheResponse = response;
        }

        public final void setPriorResponse$okhttp(@Nullable Response response) {
            this.priorResponse = response;
        }

        public final void setSentRequestAtMillis$okhttp(long j) {
            this.sentRequestAtMillis = j;
        }

        public final void setReceivedResponseAtMillis$okhttp(long j) {
            this.receivedResponseAtMillis = j;
        }

        public Builder() {
            this.code = -1;
            this.headers = new Headers.Builder();
        }

        public Builder(@NotNull Response response) {
            Intrinsics.checkNotNullParameter(response, "response");
            this.code = -1;
            this.request = response.request();
            this.protocol = response.protocol();
            this.code = response.code();
            this.message = response.message();
            this.handshake = response.handshake();
            this.headers = response.headers().newBuilder();
            this.body = response.body();
            this.networkResponse = response.networkResponse();
            this.cacheResponse = response.cacheResponse();
            this.priorResponse = response.priorResponse();
            this.sentRequestAtMillis = response.sentRequestAtMillis();
            this.receivedResponseAtMillis = response.receivedResponseAtMillis();
            this.exchange = response.exchange();
        }

        @NotNull
        public Builder request(@NotNull Request request2) {
            Intrinsics.checkNotNullParameter(request2, "request");
            setRequest$okhttp(request2);
            return this;
        }

        @NotNull
        public Builder protocol(@NotNull Protocol protocol2) {
            Intrinsics.checkNotNullParameter(protocol2, CloudMessageProviderContract.BufferDBSMS.PROTOCOL);
            setProtocol$okhttp(protocol2);
            return this;
        }

        @NotNull
        public Builder code(int i) {
            setCode$okhttp(i);
            return this;
        }

        @NotNull
        public Builder message(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "message");
            setMessage$okhttp(str);
            return this;
        }

        @NotNull
        public Builder handshake(@Nullable Handshake handshake2) {
            setHandshake$okhttp(handshake2);
            return this;
        }

        @NotNull
        public Builder header(@NotNull String str, @NotNull String str2) {
            Intrinsics.checkNotNullParameter(str, "name");
            Intrinsics.checkNotNullParameter(str2, "value");
            getHeaders$okhttp().set(str, str2);
            return this;
        }

        @NotNull
        public Builder addHeader(@NotNull String str, @NotNull String str2) {
            Intrinsics.checkNotNullParameter(str, "name");
            Intrinsics.checkNotNullParameter(str2, "value");
            getHeaders$okhttp().add(str, str2);
            return this;
        }

        @NotNull
        public Builder headers(@NotNull Headers headers2) {
            Intrinsics.checkNotNullParameter(headers2, "headers");
            setHeaders$okhttp(headers2.newBuilder());
            return this;
        }

        @NotNull
        public Builder body(@Nullable ResponseBody responseBody) {
            setBody$okhttp(responseBody);
            return this;
        }

        @NotNull
        public Builder networkResponse(@Nullable Response response) {
            checkSupportResponse("networkResponse", response);
            setNetworkResponse$okhttp(response);
            return this;
        }

        @NotNull
        public Builder cacheResponse(@Nullable Response response) {
            checkSupportResponse("cacheResponse", response);
            setCacheResponse$okhttp(response);
            return this;
        }

        private final void checkSupportResponse(String str, Response response) {
            if (response != null) {
                boolean z = true;
                if (response.body() == null) {
                    if (response.networkResponse() == null) {
                        if (response.cacheResponse() == null) {
                            if (response.priorResponse() != null) {
                                z = false;
                            }
                            if (!z) {
                                throw new IllegalArgumentException(Intrinsics.stringPlus(str, ".priorResponse != null").toString());
                            }
                            return;
                        }
                        throw new IllegalArgumentException(Intrinsics.stringPlus(str, ".cacheResponse != null").toString());
                    }
                    throw new IllegalArgumentException(Intrinsics.stringPlus(str, ".networkResponse != null").toString());
                }
                throw new IllegalArgumentException(Intrinsics.stringPlus(str, ".body != null").toString());
            }
        }

        @NotNull
        public Builder priorResponse(@Nullable Response response) {
            checkPriorResponse(response);
            setPriorResponse$okhttp(response);
            return this;
        }

        private final void checkPriorResponse(Response response) {
            if (response != null) {
                if (!(response.body() == null)) {
                    throw new IllegalArgumentException("priorResponse.body != null".toString());
                }
            }
        }

        @NotNull
        public Builder sentRequestAtMillis(long j) {
            setSentRequestAtMillis$okhttp(j);
            return this;
        }

        @NotNull
        public Builder receivedResponseAtMillis(long j) {
            setReceivedResponseAtMillis$okhttp(j);
            return this;
        }

        public final void initExchange$okhttp(@NotNull Exchange exchange2) {
            Intrinsics.checkNotNullParameter(exchange2, "deferredTrailers");
            this.exchange = exchange2;
        }

        @NotNull
        public Response build() {
            int i = this.code;
            if (i >= 0) {
                Request request2 = this.request;
                if (request2 != null) {
                    Protocol protocol2 = this.protocol;
                    if (protocol2 != null) {
                        String str = this.message;
                        if (str != null) {
                            return new Response(request2, protocol2, str, i, this.handshake, this.headers.build(), this.body, this.networkResponse, this.cacheResponse, this.priorResponse, this.sentRequestAtMillis, this.receivedResponseAtMillis, this.exchange);
                        }
                        throw new IllegalStateException("message == null".toString());
                    }
                    throw new IllegalStateException("protocol == null".toString());
                }
                throw new IllegalStateException("request == null".toString());
            }
            throw new IllegalStateException(Intrinsics.stringPlus("code < 0: ", Integer.valueOf(getCode$okhttp())).toString());
        }
    }
}
