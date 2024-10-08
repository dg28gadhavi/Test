package okhttp3;

import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import java.io.IOException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Protocol.kt */
public enum Protocol {
    HTTP_1_0("http/1.0"),
    HTTP_1_1("http/1.1"),
    SPDY_3("spdy/3.1"),
    HTTP_2("h2"),
    H2_PRIOR_KNOWLEDGE("h2_prior_knowledge"),
    QUIC("quic");
    
    @NotNull
    public static final Companion Companion = null;
    /* access modifiers changed from: private */
    @NotNull
    public final String protocol;

    private Protocol(String str) {
        this.protocol = str;
    }

    static {
        Companion = new Companion((DefaultConstructorMarker) null);
    }

    @NotNull
    public String toString() {
        return this.protocol;
    }

    /* compiled from: Protocol.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @NotNull
        public final Protocol get(@NotNull String str) throws IOException {
            Intrinsics.checkNotNullParameter(str, CloudMessageProviderContract.BufferDBSMS.PROTOCOL);
            Protocol protocol = Protocol.HTTP_1_0;
            if (!Intrinsics.areEqual(str, protocol.protocol)) {
                protocol = Protocol.HTTP_1_1;
                if (!Intrinsics.areEqual(str, protocol.protocol)) {
                    protocol = Protocol.H2_PRIOR_KNOWLEDGE;
                    if (!Intrinsics.areEqual(str, protocol.protocol)) {
                        protocol = Protocol.HTTP_2;
                        if (!Intrinsics.areEqual(str, protocol.protocol)) {
                            protocol = Protocol.SPDY_3;
                            if (!Intrinsics.areEqual(str, protocol.protocol)) {
                                protocol = Protocol.QUIC;
                                if (!Intrinsics.areEqual(str, protocol.protocol)) {
                                    throw new IOException(Intrinsics.stringPlus("Unexpected protocol: ", str));
                                }
                            }
                        }
                    }
                }
            }
            return protocol;
        }
    }
}
