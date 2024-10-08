package okhttp3.internal.cache;

import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import java.io.Closeable;
import java.io.IOException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okhttp3.internal.cache.CacheStrategy;
import okhttp3.internal.connection.RealCall;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: CacheInterceptor.kt */
public final class CacheInterceptor implements Interceptor {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);

    public CacheInterceptor(@Nullable Cache cache) {
    }

    @NotNull
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        Intrinsics.checkNotNullParameter(chain, "chain");
        Call call = chain.call();
        CacheStrategy compute = new CacheStrategy.Factory(System.currentTimeMillis(), chain.request(), (Response) null).compute();
        Request networkRequest = compute.getNetworkRequest();
        Response cacheResponse = compute.getCacheResponse();
        RealCall realCall = call instanceof RealCall ? (RealCall) call : null;
        EventListener eventListener$okhttp = realCall == null ? null : realCall.getEventListener$okhttp();
        if (eventListener$okhttp == null) {
            eventListener$okhttp = EventListener.NONE;
        }
        if (networkRequest == null && cacheResponse == null) {
            Response build = new Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(Id.REQUEST_IM_SENDMSG).message("Unsatisfiable Request (only-if-cached)").body(Util.EMPTY_RESPONSE).sentRequestAtMillis(-1).receivedResponseAtMillis(System.currentTimeMillis()).build();
            eventListener$okhttp.satisfactionFailure(call, build);
            return build;
        } else if (networkRequest == null) {
            Intrinsics.checkNotNull(cacheResponse);
            Response build2 = cacheResponse.newBuilder().cacheResponse(Companion.stripBody(cacheResponse)).build();
            eventListener$okhttp.cacheHit(call, build2);
            return build2;
        } else {
            if (cacheResponse != null) {
                eventListener$okhttp.cacheConditionalHit(call, cacheResponse);
            }
            Response proceed = chain.proceed(networkRequest);
            if (cacheResponse != null) {
                boolean z = false;
                if (proceed != null && proceed.code() == 304) {
                    z = true;
                }
                if (!z) {
                    ResponseBody body = cacheResponse.body();
                    if (body != null) {
                        Util.closeQuietly((Closeable) body);
                    }
                } else {
                    Response.Builder newBuilder = cacheResponse.newBuilder();
                    Companion companion = Companion;
                    newBuilder.headers(companion.combine(cacheResponse.headers(), proceed.headers())).sentRequestAtMillis(proceed.sentRequestAtMillis()).receivedResponseAtMillis(proceed.receivedResponseAtMillis()).cacheResponse(companion.stripBody(cacheResponse)).networkResponse(companion.stripBody(proceed)).build();
                    ResponseBody body2 = proceed.body();
                    Intrinsics.checkNotNull(body2);
                    body2.close();
                    Intrinsics.checkNotNull((Object) null);
                    throw null;
                }
            }
            Intrinsics.checkNotNull(proceed);
            Response.Builder newBuilder2 = proceed.newBuilder();
            Companion companion2 = Companion;
            return newBuilder2.cacheResponse(companion2.stripBody(cacheResponse)).networkResponse(companion2.stripBody(proceed)).build();
        }
    }

    /* compiled from: CacheInterceptor.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        /* access modifiers changed from: private */
        public final Response stripBody(Response response) {
            return (response == null ? null : response.body()) != null ? response.newBuilder().body((ResponseBody) null).build() : response;
        }

        /* access modifiers changed from: private */
        public final Headers combine(Headers headers, Headers headers2) {
            Headers.Builder builder = new Headers.Builder();
            int size = headers.size();
            int i = 0;
            int i2 = 0;
            while (i2 < size) {
                int i3 = i2 + 1;
                String name = headers.name(i2);
                String value = headers.value(i2);
                if ((!StringsKt__StringsJVMKt.equals("Warning", name, true) || !StringsKt__StringsJVMKt.startsWith$default(value, "1", false, 2, (Object) null)) && (isContentSpecificHeader(name) || !isEndToEnd(name) || headers2.get(name) == null)) {
                    builder.addLenient$okhttp(name, value);
                }
                i2 = i3;
            }
            int size2 = headers2.size();
            while (i < size2) {
                int i4 = i + 1;
                String name2 = headers2.name(i);
                if (!isContentSpecificHeader(name2) && isEndToEnd(name2)) {
                    builder.addLenient$okhttp(name2, headers2.value(i));
                }
                i = i4;
            }
            return builder.build();
        }

        private final boolean isEndToEnd(String str) {
            if (StringsKt__StringsJVMKt.equals("Connection", str, true) || StringsKt__StringsJVMKt.equals("Keep-Alive", str, true) || StringsKt__StringsJVMKt.equals("Proxy-Authenticate", str, true) || StringsKt__StringsJVMKt.equals(HttpController.HEADER_PROXY_AUTHORIZATION, str, true) || StringsKt__StringsJVMKt.equals("TE", str, true) || StringsKt__StringsJVMKt.equals("Trailers", str, true) || StringsKt__StringsJVMKt.equals(HttpRequest.HEADER_TRANSFER_ENCODING, str, true) || StringsKt__StringsJVMKt.equals("Upgrade", str, true)) {
                return false;
            }
            return true;
        }

        private final boolean isContentSpecificHeader(String str) {
            if (StringsKt__StringsJVMKt.equals("Content-Length", str, true) || StringsKt__StringsJVMKt.equals("Content-Encoding", str, true) || StringsKt__StringsJVMKt.equals("Content-Type", str, true)) {
                return true;
            }
            return false;
        }
    }
}
