package okhttp3;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Challenge.kt */
public final class Challenge {
    @NotNull
    private final Map<String, String> authParams;
    @NotNull
    private final String scheme;

    public Challenge(@NotNull String str, @NotNull Map<String, String> map) {
        String str2;
        Intrinsics.checkNotNullParameter(str, "scheme");
        Intrinsics.checkNotNullParameter(map, "authParams");
        this.scheme = str;
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (Map.Entry next : map.entrySet()) {
            String str3 = (String) next.getKey();
            String str4 = (String) next.getValue();
            if (str3 == null) {
                str2 = null;
            } else {
                Locale locale = Locale.US;
                Intrinsics.checkNotNullExpressionValue(locale, "US");
                str2 = str3.toLowerCase(locale);
                Intrinsics.checkNotNullExpressionValue(str2, "this as java.lang.String).toLowerCase(locale)");
            }
            linkedHashMap.put(str2, str4);
        }
        Map<String, String> unmodifiableMap = Collections.unmodifiableMap(linkedHashMap);
        Intrinsics.checkNotNullExpressionValue(unmodifiableMap, "unmodifiableMap<String?, String>(newAuthParams)");
        this.authParams = unmodifiableMap;
    }

    @NotNull
    public final String scheme() {
        return this.scheme;
    }

    @Nullable
    public final String realm() {
        return this.authParams.get("realm");
    }

    @NotNull
    public final Charset charset() {
        String str = this.authParams.get("charset");
        if (str != null) {
            try {
                Charset forName = Charset.forName(str);
                Intrinsics.checkNotNullExpressionValue(forName, "forName(charset)");
                return forName;
            } catch (Exception unused) {
            }
        }
        Charset charset = StandardCharsets.ISO_8859_1;
        Intrinsics.checkNotNullExpressionValue(charset, "ISO_8859_1");
        return charset;
    }

    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Challenge) {
            Challenge challenge = (Challenge) obj;
            return Intrinsics.areEqual(challenge.scheme, this.scheme) && Intrinsics.areEqual(challenge.authParams, this.authParams);
        }
    }

    public int hashCode() {
        return ((899 + this.scheme.hashCode()) * 31) + this.authParams.hashCode();
    }

    @NotNull
    public String toString() {
        return this.scheme + " authParams=" + this.authParams;
    }
}
