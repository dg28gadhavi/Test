package okhttp3.internal.http2;

import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Header.kt */
public final class Header {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    @NotNull
    public static final ByteString PSEUDO_PREFIX;
    @NotNull
    public static final ByteString RESPONSE_STATUS;
    @NotNull
    public static final ByteString TARGET_AUTHORITY;
    @NotNull
    public static final ByteString TARGET_METHOD;
    @NotNull
    public static final ByteString TARGET_PATH;
    @NotNull
    public static final ByteString TARGET_SCHEME;
    public final int hpackSize;
    @NotNull
    public final ByteString name;
    @NotNull
    public final ByteString value;

    @NotNull
    public final ByteString component1() {
        return this.name;
    }

    @NotNull
    public final ByteString component2() {
        return this.value;
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Header)) {
            return false;
        }
        Header header = (Header) obj;
        return Intrinsics.areEqual(this.name, header.name) && Intrinsics.areEqual(this.value, header.value);
    }

    public int hashCode() {
        return (this.name.hashCode() * 31) + this.value.hashCode();
    }

    public Header(@NotNull ByteString byteString, @NotNull ByteString byteString2) {
        Intrinsics.checkNotNullParameter(byteString, "name");
        Intrinsics.checkNotNullParameter(byteString2, "value");
        this.name = byteString;
        this.value = byteString2;
        this.hpackSize = byteString.size() + 32 + byteString2.size();
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Header(@org.jetbrains.annotations.NotNull java.lang.String r2, @org.jetbrains.annotations.NotNull java.lang.String r3) {
        /*
            r1 = this;
            java.lang.String r0 = "name"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r2, r0)
            java.lang.String r0 = "value"
            kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r3, r0)
            okio.ByteString$Companion r0 = okio.ByteString.Companion
            okio.ByteString r2 = r0.encodeUtf8(r2)
            okio.ByteString r3 = r0.encodeUtf8(r3)
            r1.<init>((okio.ByteString) r2, (okio.ByteString) r3)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.http2.Header.<init>(java.lang.String, java.lang.String):void");
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public Header(@NotNull ByteString byteString, @NotNull String str) {
        this(byteString, ByteString.Companion.encodeUtf8(str));
        Intrinsics.checkNotNullParameter(byteString, "name");
        Intrinsics.checkNotNullParameter(str, "value");
    }

    @NotNull
    public String toString() {
        return this.name.utf8() + ": " + this.value.utf8();
    }

    /* compiled from: Header.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }

    static {
        ByteString.Companion companion = ByteString.Companion;
        PSEUDO_PREFIX = companion.encodeUtf8(":");
        RESPONSE_STATUS = companion.encodeUtf8(":status");
        TARGET_METHOD = companion.encodeUtf8(":method");
        TARGET_PATH = companion.encodeUtf8(":path");
        TARGET_SCHEME = companion.encodeUtf8(":scheme");
        TARGET_AUTHORITY = companion.encodeUtf8(":authority");
    }
}
