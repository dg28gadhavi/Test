package okhttp3.internal.platform;

import java.util.List;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Protocol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Jdk9Platform.kt */
public class Jdk9Platform extends Platform {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    /* access modifiers changed from: private */
    public static final boolean isAvailable;

    public void configureTlsExtensions(@NotNull SSLSocket sSLSocket, @Nullable String str, @NotNull List<Protocol> list) {
        Intrinsics.checkNotNullParameter(sSLSocket, "sslSocket");
        Intrinsics.checkNotNullParameter(list, "protocols");
        SSLParameters sSLParameters = sSLSocket.getSSLParameters();
        Object[] array = Platform.Companion.alpnProtocolNames(list).toArray(new String[0]);
        if (array != null) {
            sSLParameters.setApplicationProtocols((String[]) array);
            sSLSocket.setSSLParameters(sSLParameters);
            return;
        }
        throw new NullPointerException("null cannot be cast to non-null type kotlin.Array<T of kotlin.collections.ArraysKt__ArraysJVMKt.toTypedArray>");
    }

    @Nullable
    public String getSelectedProtocol(@NotNull SSLSocket sSLSocket) {
        boolean z;
        Intrinsics.checkNotNullParameter(sSLSocket, "sslSocket");
        try {
            String applicationProtocol = sSLSocket.getApplicationProtocol();
            if (applicationProtocol == null) {
                z = true;
            } else {
                z = Intrinsics.areEqual(applicationProtocol, "");
            }
            if (z) {
                return null;
            }
            return applicationProtocol;
        } catch (UnsupportedOperationException unused) {
            return null;
        }
    }

    /* compiled from: Jdk9Platform.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public final boolean isAvailable() {
            return Jdk9Platform.isAvailable;
        }

        @Nullable
        public final Jdk9Platform buildIfSupported() {
            if (isAvailable()) {
                return new Jdk9Platform();
            }
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x001f, code lost:
        if (r1.intValue() >= 9) goto L_0x002d;
     */
    static {
        /*
            okhttp3.internal.platform.Jdk9Platform$Companion r0 = new okhttp3.internal.platform.Jdk9Platform$Companion
            r1 = 0
            r0.<init>(r1)
            Companion = r0
            java.lang.String r0 = "java.specification.version"
            java.lang.String r0 = java.lang.System.getProperty(r0)
            if (r0 != 0) goto L_0x0011
            goto L_0x0015
        L_0x0011:
            java.lang.Integer r1 = kotlin.text.StringsKt__StringNumberConversionsKt.toIntOrNull(r0)
        L_0x0015:
            r0 = 1
            r2 = 0
            if (r1 == 0) goto L_0x0024
            int r1 = r1.intValue()
            r3 = 9
            if (r1 < r3) goto L_0x0022
            goto L_0x002d
        L_0x0022:
            r0 = r2
            goto L_0x002d
        L_0x0024:
            java.lang.Class<javax.net.ssl.SSLSocket> r1 = javax.net.ssl.SSLSocket.class
            java.lang.String r3 = "getApplicationProtocol"
            java.lang.Class[] r4 = new java.lang.Class[r2]     // Catch:{ NoSuchMethodException -> 0x0022 }
            r1.getMethod(r3, r4)     // Catch:{ NoSuchMethodException -> 0x0022 }
        L_0x002d:
            isAvailable = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.platform.Jdk9Platform.<clinit>():void");
    }
}
