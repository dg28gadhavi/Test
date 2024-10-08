package okhttp3.internal.http;

import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import java.io.IOException;
import java.net.ProtocolException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Protocol;
import org.jetbrains.annotations.NotNull;

/* compiled from: StatusLine.kt */
public final class StatusLine {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    public final int code;
    @NotNull
    public final String message;
    @NotNull
    public final Protocol protocol;

    public StatusLine(@NotNull Protocol protocol2, int i, @NotNull String str) {
        Intrinsics.checkNotNullParameter(protocol2, CloudMessageProviderContract.BufferDBSMS.PROTOCOL);
        Intrinsics.checkNotNullParameter(str, "message");
        this.protocol = protocol2;
        this.code = i;
        this.message = str;
    }

    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.protocol == Protocol.HTTP_1_0) {
            sb.append("HTTP/1.0");
        } else {
            sb.append("HTTP/1.1");
        }
        sb.append(' ');
        sb.append(this.code);
        sb.append(' ');
        sb.append(this.message);
        String sb2 = sb.toString();
        Intrinsics.checkNotNullExpressionValue(sb2, "StringBuilder().apply(builderAction).toString()");
        return sb2;
    }

    /* compiled from: StatusLine.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @NotNull
        public final StatusLine parse(@NotNull String str) throws IOException {
            Protocol protocol;
            int i;
            String str2;
            Intrinsics.checkNotNullParameter(str, "statusLine");
            if (StringsKt__StringsJVMKt.startsWith$default(str, "HTTP/1.", false, 2, (Object) null)) {
                i = 9;
                if (str.length() < 9 || str.charAt(8) != ' ') {
                    throw new ProtocolException(Intrinsics.stringPlus("Unexpected status line: ", str));
                }
                int charAt = str.charAt(7) - '0';
                if (charAt == 0) {
                    protocol = Protocol.HTTP_1_0;
                } else if (charAt == 1) {
                    protocol = Protocol.HTTP_1_1;
                } else {
                    throw new ProtocolException(Intrinsics.stringPlus("Unexpected status line: ", str));
                }
            } else if (StringsKt__StringsJVMKt.startsWith$default(str, "ICY ", false, 2, (Object) null)) {
                protocol = Protocol.HTTP_1_0;
                i = 4;
            } else {
                throw new ProtocolException(Intrinsics.stringPlus("Unexpected status line: ", str));
            }
            int i2 = i + 3;
            if (str.length() >= i2) {
                try {
                    String substring = str.substring(i, i2);
                    Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
                    int parseInt = Integer.parseInt(substring);
                    if (str.length() <= i2) {
                        str2 = "";
                    } else if (str.charAt(i2) == ' ') {
                        str2 = str.substring(i + 4);
                        Intrinsics.checkNotNullExpressionValue(str2, "this as java.lang.String).substring(startIndex)");
                    } else {
                        throw new ProtocolException(Intrinsics.stringPlus("Unexpected status line: ", str));
                    }
                    return new StatusLine(protocol, parseInt, str2);
                } catch (NumberFormatException unused) {
                    throw new ProtocolException(Intrinsics.stringPlus("Unexpected status line: ", str));
                }
            } else {
                throw new ProtocolException(Intrinsics.stringPlus("Unexpected status line: ", str));
            }
        }
    }
}
