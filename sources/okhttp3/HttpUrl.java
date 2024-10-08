package okhttp3;

import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.IntProgression;
import kotlin.text.Regex;
import okhttp3.internal.HostnamesKt;
import okhttp3.internal.Util;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: HttpUrl.kt */
public final class HttpUrl {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    /* access modifiers changed from: private */
    @NotNull
    public static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    @Nullable
    private final String fragment;
    @NotNull
    private final String host;
    private final boolean isHttps;
    @NotNull
    private final String password;
    @NotNull
    private final List<String> pathSegments;
    private final int port;
    @Nullable
    private final List<String> queryNamesAndValues;
    @NotNull
    private final String scheme;
    @NotNull
    private final String url;
    @NotNull
    private final String username;

    @Nullable
    public static final HttpUrl get(@NotNull URI uri) {
        return Companion.get(uri);
    }

    @Nullable
    public static final HttpUrl parse(@NotNull String str) {
        return Companion.parse(str);
    }

    public HttpUrl(@NotNull String str, @NotNull String str2, @NotNull String str3, @NotNull String str4, int i, @NotNull List<String> list, @Nullable List<String> list2, @Nullable String str5, @NotNull String str6) {
        Intrinsics.checkNotNullParameter(str, "scheme");
        Intrinsics.checkNotNullParameter(str2, "username");
        Intrinsics.checkNotNullParameter(str3, "password");
        Intrinsics.checkNotNullParameter(str4, "host");
        Intrinsics.checkNotNullParameter(list, "pathSegments");
        Intrinsics.checkNotNullParameter(str6, ImsConstants.FtDlParams.FT_DL_URL);
        this.scheme = str;
        this.username = str2;
        this.password = str3;
        this.host = str4;
        this.port = i;
        this.pathSegments = list;
        this.queryNamesAndValues = list2;
        this.fragment = str5;
        this.url = str6;
        this.isHttps = Intrinsics.areEqual(str, OMAGlobalVariables.HTTPS);
    }

    @NotNull
    public final String scheme() {
        return this.scheme;
    }

    @NotNull
    public final String host() {
        return this.host;
    }

    public final int port() {
        return this.port;
    }

    public final boolean isHttps() {
        return this.isHttps;
    }

    @NotNull
    public final URL url() {
        try {
            return new URL(this.url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public final URI uri() {
        String builder = newBuilder().reencodeForUri$okhttp().toString();
        try {
            return new URI(builder);
        } catch (URISyntaxException e) {
            try {
                URI create = URI.create(new Regex("[\\u0000-\\u001F\\u007F-\\u009F\\p{javaWhitespace}]").replace(builder, ""));
                Intrinsics.checkNotNullExpressionValue(create, "{\n      // Unlikely edge…Unexpected!\n      }\n    }");
                return create;
            } catch (Exception unused) {
                throw new RuntimeException(e);
            }
        }
    }

    @NotNull
    public final String encodedUsername() {
        if (this.username.length() == 0) {
            return "";
        }
        int length = this.scheme.length() + 3;
        String str = this.url;
        String substring = this.url.substring(length, Util.delimiterOffset(str, ":@", length, str.length()));
        Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
        return substring;
    }

    @NotNull
    public final String encodedPassword() {
        if (this.password.length() == 0) {
            return "";
        }
        String substring = this.url.substring(StringsKt__StringsKt.indexOf$default((CharSequence) this.url, ':', this.scheme.length() + 3, false, 4, (Object) null) + 1, StringsKt__StringsKt.indexOf$default((CharSequence) this.url, '@', 0, false, 6, (Object) null));
        Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
        return substring;
    }

    @NotNull
    public final String encodedPath() {
        int indexOf$default = StringsKt__StringsKt.indexOf$default((CharSequence) this.url, '/', this.scheme.length() + 3, false, 4, (Object) null);
        String str = this.url;
        String substring = this.url.substring(indexOf$default, Util.delimiterOffset(str, "?#", indexOf$default, str.length()));
        Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
        return substring;
    }

    @NotNull
    public final List<String> encodedPathSegments() {
        int indexOf$default = StringsKt__StringsKt.indexOf$default((CharSequence) this.url, '/', this.scheme.length() + 3, false, 4, (Object) null);
        String str = this.url;
        int delimiterOffset = Util.delimiterOffset(str, "?#", indexOf$default, str.length());
        ArrayList arrayList = new ArrayList();
        while (indexOf$default < delimiterOffset) {
            int i = indexOf$default + 1;
            int delimiterOffset2 = Util.delimiterOffset(this.url, '/', i, delimiterOffset);
            String substring = this.url.substring(i, delimiterOffset2);
            Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
            arrayList.add(substring);
            indexOf$default = delimiterOffset2;
        }
        return arrayList;
    }

    @Nullable
    public final String encodedQuery() {
        if (this.queryNamesAndValues == null) {
            return null;
        }
        int indexOf$default = StringsKt__StringsKt.indexOf$default((CharSequence) this.url, '?', 0, false, 6, (Object) null) + 1;
        String str = this.url;
        String substring = this.url.substring(indexOf$default, Util.delimiterOffset(str, '#', indexOf$default, str.length()));
        Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
        return substring;
    }

    @Nullable
    public final String query() {
        if (this.queryNamesAndValues == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Companion.toQueryString$okhttp(this.queryNamesAndValues, sb);
        return sb.toString();
    }

    @Nullable
    public final String encodedFragment() {
        if (this.fragment == null) {
            return null;
        }
        String substring = this.url.substring(StringsKt__StringsKt.indexOf$default((CharSequence) this.url, '#', 0, false, 6, (Object) null) + 1);
        Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String).substring(startIndex)");
        return substring;
    }

    @NotNull
    public final String redact() {
        Builder newBuilder = newBuilder("/...");
        Intrinsics.checkNotNull(newBuilder);
        return newBuilder.username("").password("").build().toString();
    }

    @Nullable
    public final HttpUrl resolve(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "link");
        Builder newBuilder = newBuilder(str);
        if (newBuilder == null) {
            return null;
        }
        return newBuilder.build();
    }

    @NotNull
    public final Builder newBuilder() {
        Builder builder = new Builder();
        builder.setScheme$okhttp(this.scheme);
        builder.setEncodedUsername$okhttp(encodedUsername());
        builder.setEncodedPassword$okhttp(encodedPassword());
        builder.setHost$okhttp(this.host);
        builder.setPort$okhttp(this.port != Companion.defaultPort(this.scheme) ? this.port : -1);
        builder.getEncodedPathSegments$okhttp().clear();
        builder.getEncodedPathSegments$okhttp().addAll(encodedPathSegments());
        builder.encodedQuery(encodedQuery());
        builder.setEncodedFragment$okhttp(encodedFragment());
        return builder;
    }

    @Nullable
    public final Builder newBuilder(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "link");
        try {
            return new Builder().parse$okhttp(this, str);
        } catch (IllegalArgumentException unused) {
            return null;
        }
    }

    public boolean equals(@Nullable Object obj) {
        return (obj instanceof HttpUrl) && Intrinsics.areEqual(((HttpUrl) obj).url, this.url);
    }

    public int hashCode() {
        return this.url.hashCode();
    }

    @NotNull
    public String toString() {
        return this.url;
    }

    /* compiled from: HttpUrl.kt */
    public static final class Builder {
        @NotNull
        public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
        @Nullable
        private String encodedFragment;
        @NotNull
        private String encodedPassword = "";
        @NotNull
        private final List<String> encodedPathSegments;
        @Nullable
        private List<String> encodedQueryNamesAndValues;
        @NotNull
        private String encodedUsername = "";
        @Nullable
        private String host;
        private int port = -1;
        @Nullable
        private String scheme;

        public Builder() {
            ArrayList arrayList = new ArrayList();
            this.encodedPathSegments = arrayList;
            arrayList.add("");
        }

        @Nullable
        public final String getScheme$okhttp() {
            return this.scheme;
        }

        public final void setScheme$okhttp(@Nullable String str) {
            this.scheme = str;
        }

        @NotNull
        public final String getEncodedUsername$okhttp() {
            return this.encodedUsername;
        }

        public final void setEncodedUsername$okhttp(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "<set-?>");
            this.encodedUsername = str;
        }

        @NotNull
        public final String getEncodedPassword$okhttp() {
            return this.encodedPassword;
        }

        public final void setEncodedPassword$okhttp(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "<set-?>");
            this.encodedPassword = str;
        }

        @Nullable
        public final String getHost$okhttp() {
            return this.host;
        }

        public final void setHost$okhttp(@Nullable String str) {
            this.host = str;
        }

        public final int getPort$okhttp() {
            return this.port;
        }

        public final void setPort$okhttp(int i) {
            this.port = i;
        }

        @NotNull
        public final List<String> getEncodedPathSegments$okhttp() {
            return this.encodedPathSegments;
        }

        @Nullable
        public final List<String> getEncodedQueryNamesAndValues$okhttp() {
            return this.encodedQueryNamesAndValues;
        }

        public final void setEncodedQueryNamesAndValues$okhttp(@Nullable List<String> list) {
            this.encodedQueryNamesAndValues = list;
        }

        @Nullable
        public final String getEncodedFragment$okhttp() {
            return this.encodedFragment;
        }

        public final void setEncodedFragment$okhttp(@Nullable String str) {
            this.encodedFragment = str;
        }

        @NotNull
        public final Builder scheme(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "scheme");
            if (StringsKt__StringsJVMKt.equals(str, OMAGlobalVariables.HTTP, true)) {
                setScheme$okhttp(OMAGlobalVariables.HTTP);
            } else if (StringsKt__StringsJVMKt.equals(str, OMAGlobalVariables.HTTPS, true)) {
                setScheme$okhttp(OMAGlobalVariables.HTTPS);
            } else {
                throw new IllegalArgumentException(Intrinsics.stringPlus("unexpected scheme: ", str));
            }
            return this;
        }

        @NotNull
        public final Builder username(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "username");
            setEncodedUsername$okhttp(Companion.canonicalize$okhttp$default(HttpUrl.Companion, str, 0, 0, " \"':;<=>@[]^`{}|/\\?#", false, false, false, false, (Charset) null, MNO.SPARK, (Object) null));
            return this;
        }

        @NotNull
        public final Builder password(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "password");
            setEncodedPassword$okhttp(Companion.canonicalize$okhttp$default(HttpUrl.Companion, str, 0, 0, " \"':;<=>@[]^`{}|/\\?#", false, false, false, false, (Charset) null, MNO.SPARK, (Object) null));
            return this;
        }

        @NotNull
        public final Builder host(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "host");
            String canonicalHost = HostnamesKt.toCanonicalHost(Companion.percentDecode$okhttp$default(HttpUrl.Companion, str, 0, 0, false, 7, (Object) null));
            if (canonicalHost != null) {
                setHost$okhttp(canonicalHost);
                return this;
            }
            throw new IllegalArgumentException(Intrinsics.stringPlus("unexpected host: ", str));
        }

        @NotNull
        public final Builder port(int i) {
            boolean z = false;
            if (1 <= i && i < 65536) {
                z = true;
            }
            if (z) {
                setPort$okhttp(i);
                return this;
            }
            throw new IllegalArgumentException(Intrinsics.stringPlus("unexpected port: ", Integer.valueOf(i)).toString());
        }

        private final int effectivePort() {
            int i = this.port;
            if (i != -1) {
                return i;
            }
            Companion companion = HttpUrl.Companion;
            String str = this.scheme;
            Intrinsics.checkNotNull(str);
            return companion.defaultPort(str);
        }

        @NotNull
        public final Builder encodedQuery(@Nullable String str) {
            List<String> list = null;
            if (str != null) {
                Companion companion = HttpUrl.Companion;
                String canonicalize$okhttp$default = Companion.canonicalize$okhttp$default(companion, str, 0, 0, " \"'<>#", true, false, true, false, (Charset) null, 211, (Object) null);
                if (canonicalize$okhttp$default != null) {
                    list = companion.toQueryNamesAndValues$okhttp(canonicalize$okhttp$default);
                }
            }
            setEncodedQueryNamesAndValues$okhttp(list);
            return this;
        }

        @NotNull
        public final Builder addQueryParameter(@NotNull String str, @Nullable String str2) {
            Intrinsics.checkNotNullParameter(str, "name");
            if (getEncodedQueryNamesAndValues$okhttp() == null) {
                setEncodedQueryNamesAndValues$okhttp(new ArrayList());
            }
            List<String> encodedQueryNamesAndValues$okhttp = getEncodedQueryNamesAndValues$okhttp();
            Intrinsics.checkNotNull(encodedQueryNamesAndValues$okhttp);
            Companion companion = HttpUrl.Companion;
            encodedQueryNamesAndValues$okhttp.add(Companion.canonicalize$okhttp$default(companion, str, 0, 0, " !\"#$&'(),/:;<=>?@[]\\^`{|}~", false, false, true, false, (Charset) null, 219, (Object) null));
            List<String> encodedQueryNamesAndValues$okhttp2 = getEncodedQueryNamesAndValues$okhttp();
            Intrinsics.checkNotNull(encodedQueryNamesAndValues$okhttp2);
            encodedQueryNamesAndValues$okhttp2.add(str2 == null ? null : Companion.canonicalize$okhttp$default(companion, str2, 0, 0, " !\"#$&'(),/:;<=>?@[]\\^`{|}~", false, false, true, false, (Charset) null, 219, (Object) null));
            return this;
        }

        @NotNull
        public final Builder addEncodedQueryParameter(@NotNull String str, @Nullable String str2) {
            Intrinsics.checkNotNullParameter(str, "encodedName");
            if (getEncodedQueryNamesAndValues$okhttp() == null) {
                setEncodedQueryNamesAndValues$okhttp(new ArrayList());
            }
            List<String> encodedQueryNamesAndValues$okhttp = getEncodedQueryNamesAndValues$okhttp();
            Intrinsics.checkNotNull(encodedQueryNamesAndValues$okhttp);
            Companion companion = HttpUrl.Companion;
            encodedQueryNamesAndValues$okhttp.add(Companion.canonicalize$okhttp$default(companion, str, 0, 0, " \"'<>#&=", true, false, true, false, (Charset) null, 211, (Object) null));
            List<String> encodedQueryNamesAndValues$okhttp2 = getEncodedQueryNamesAndValues$okhttp();
            Intrinsics.checkNotNull(encodedQueryNamesAndValues$okhttp2);
            encodedQueryNamesAndValues$okhttp2.add(str2 == null ? null : Companion.canonicalize$okhttp$default(companion, str2, 0, 0, " \"'<>#&=", true, false, true, false, (Charset) null, 211, (Object) null));
            return this;
        }

        @NotNull
        public final Builder reencodeForUri$okhttp() {
            String host$okhttp = getHost$okhttp();
            String str = null;
            setHost$okhttp(host$okhttp == null ? null : new Regex("[\"<>^`{|}]").replace(host$okhttp, ""));
            int size = getEncodedPathSegments$okhttp().size();
            int i = 0;
            for (int i2 = 0; i2 < size; i2++) {
                getEncodedPathSegments$okhttp().set(i2, Companion.canonicalize$okhttp$default(HttpUrl.Companion, getEncodedPathSegments$okhttp().get(i2), 0, 0, "[]", true, true, false, false, (Charset) null, 227, (Object) null));
            }
            List<String> encodedQueryNamesAndValues$okhttp = getEncodedQueryNamesAndValues$okhttp();
            if (encodedQueryNamesAndValues$okhttp != null) {
                int size2 = encodedQueryNamesAndValues$okhttp.size();
                while (i < size2) {
                    int i3 = i + 1;
                    String str2 = encodedQueryNamesAndValues$okhttp.get(i);
                    encodedQueryNamesAndValues$okhttp.set(i, str2 == null ? null : Companion.canonicalize$okhttp$default(HttpUrl.Companion, str2, 0, 0, "\\^`{|}", true, true, true, false, (Charset) null, MNO.TIGO_COLOMBIA, (Object) null));
                    i = i3;
                }
            }
            String encodedFragment$okhttp = getEncodedFragment$okhttp();
            if (encodedFragment$okhttp != null) {
                str = Companion.canonicalize$okhttp$default(HttpUrl.Companion, encodedFragment$okhttp, 0, 0, " \"#<>\\^`{|}", true, true, false, true, (Charset) null, MNO.NTEL_NIGERIA, (Object) null);
            }
            setEncodedFragment$okhttp(str);
            return this;
        }

        @NotNull
        public final HttpUrl build() {
            ArrayList arrayList;
            String str = this.scheme;
            if (str != null) {
                Companion companion = HttpUrl.Companion;
                String percentDecode$okhttp$default = Companion.percentDecode$okhttp$default(companion, this.encodedUsername, 0, 0, false, 7, (Object) null);
                String percentDecode$okhttp$default2 = Companion.percentDecode$okhttp$default(companion, this.encodedPassword, 0, 0, false, 7, (Object) null);
                String str2 = this.host;
                if (str2 != null) {
                    int effectivePort = effectivePort();
                    List<String> list = this.encodedPathSegments;
                    ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
                    for (String percentDecode$okhttp$default3 : list) {
                        arrayList2.add(Companion.percentDecode$okhttp$default(HttpUrl.Companion, percentDecode$okhttp$default3, 0, 0, false, 7, (Object) null));
                    }
                    List<String> list2 = this.encodedQueryNamesAndValues;
                    String str3 = null;
                    if (list2 == null) {
                        arrayList = null;
                    } else {
                        ArrayList arrayList3 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list2, 10));
                        for (String str4 : list2) {
                            arrayList3.add(str4 == null ? null : Companion.percentDecode$okhttp$default(HttpUrl.Companion, str4, 0, 0, true, 3, (Object) null));
                        }
                        arrayList = arrayList3;
                    }
                    String str5 = this.encodedFragment;
                    if (str5 != null) {
                        str3 = Companion.percentDecode$okhttp$default(HttpUrl.Companion, str5, 0, 0, false, 7, (Object) null);
                    }
                    return new HttpUrl(str, percentDecode$okhttp$default, percentDecode$okhttp$default2, str2, effectivePort, arrayList2, arrayList, str3, toString());
                }
                throw new IllegalStateException("host == null");
            }
            throw new IllegalStateException("scheme == null");
        }

        /* JADX WARNING: Code restructure failed: missing block: B:14:0x003d, code lost:
            if ((getEncodedPassword$okhttp().length() > 0) != false) goto L_0x003f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b5, code lost:
            if (r1 != r2.defaultPort(r3)) goto L_0x00b7;
         */
        @org.jetbrains.annotations.NotNull
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public java.lang.String toString() {
            /*
                r6 = this;
                java.lang.StringBuilder r0 = new java.lang.StringBuilder
                r0.<init>()
                java.lang.String r1 = r6.getScheme$okhttp()
                if (r1 == 0) goto L_0x0018
                java.lang.String r1 = r6.getScheme$okhttp()
                r0.append(r1)
                java.lang.String r1 = "://"
                r0.append(r1)
                goto L_0x001d
            L_0x0018:
                java.lang.String r1 = "//"
                r0.append(r1)
            L_0x001d:
                java.lang.String r1 = r6.getEncodedUsername$okhttp()
                int r1 = r1.length()
                r2 = 1
                r3 = 0
                if (r1 <= 0) goto L_0x002b
                r1 = r2
                goto L_0x002c
            L_0x002b:
                r1 = r3
            L_0x002c:
                r4 = 58
                if (r1 != 0) goto L_0x003f
                java.lang.String r1 = r6.getEncodedPassword$okhttp()
                int r1 = r1.length()
                if (r1 <= 0) goto L_0x003c
                r1 = r2
                goto L_0x003d
            L_0x003c:
                r1 = r3
            L_0x003d:
                if (r1 == 0) goto L_0x0063
            L_0x003f:
                java.lang.String r1 = r6.getEncodedUsername$okhttp()
                r0.append(r1)
                java.lang.String r1 = r6.getEncodedPassword$okhttp()
                int r1 = r1.length()
                if (r1 <= 0) goto L_0x0051
                goto L_0x0052
            L_0x0051:
                r2 = r3
            L_0x0052:
                if (r2 == 0) goto L_0x005e
                r0.append(r4)
                java.lang.String r1 = r6.getEncodedPassword$okhttp()
                r0.append(r1)
            L_0x005e:
                r1 = 64
                r0.append(r1)
            L_0x0063:
                java.lang.String r1 = r6.getHost$okhttp()
                if (r1 == 0) goto L_0x0091
                java.lang.String r1 = r6.getHost$okhttp()
                kotlin.jvm.internal.Intrinsics.checkNotNull(r1)
                r2 = 2
                r5 = 0
                boolean r1 = kotlin.text.StringsKt__StringsKt.contains$default((java.lang.CharSequence) r1, (char) r4, (boolean) r3, (int) r2, (java.lang.Object) r5)
                if (r1 == 0) goto L_0x008a
                r1 = 91
                r0.append(r1)
                java.lang.String r1 = r6.getHost$okhttp()
                r0.append(r1)
                r1 = 93
                r0.append(r1)
                goto L_0x0091
            L_0x008a:
                java.lang.String r1 = r6.getHost$okhttp()
                r0.append(r1)
            L_0x0091:
                int r1 = r6.getPort$okhttp()
                r2 = -1
                if (r1 != r2) goto L_0x009e
                java.lang.String r1 = r6.getScheme$okhttp()
                if (r1 == 0) goto L_0x00bd
            L_0x009e:
                int r1 = r6.effectivePort()
                java.lang.String r2 = r6.getScheme$okhttp()
                if (r2 == 0) goto L_0x00b7
                okhttp3.HttpUrl$Companion r2 = okhttp3.HttpUrl.Companion
                java.lang.String r3 = r6.getScheme$okhttp()
                kotlin.jvm.internal.Intrinsics.checkNotNull(r3)
                int r2 = r2.defaultPort(r3)
                if (r1 == r2) goto L_0x00bd
            L_0x00b7:
                r0.append(r4)
                r0.append(r1)
            L_0x00bd:
                okhttp3.HttpUrl$Companion r1 = okhttp3.HttpUrl.Companion
                java.util.List r2 = r6.getEncodedPathSegments$okhttp()
                r1.toPathString$okhttp(r2, r0)
                java.util.List r2 = r6.getEncodedQueryNamesAndValues$okhttp()
                if (r2 == 0) goto L_0x00db
                r2 = 63
                r0.append(r2)
                java.util.List r2 = r6.getEncodedQueryNamesAndValues$okhttp()
                kotlin.jvm.internal.Intrinsics.checkNotNull(r2)
                r1.toQueryString$okhttp(r2, r0)
            L_0x00db:
                java.lang.String r1 = r6.getEncodedFragment$okhttp()
                if (r1 == 0) goto L_0x00ed
                r1 = 35
                r0.append(r1)
                java.lang.String r6 = r6.getEncodedFragment$okhttp()
                r0.append(r6)
            L_0x00ed:
                java.lang.String r6 = r0.toString()
                java.lang.String r0 = "StringBuilder().apply(builderAction).toString()"
                kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r6, r0)
                return r6
            */
            throw new UnsupportedOperationException("Method not decompiled: okhttp3.HttpUrl.Builder.toString():java.lang.String");
        }

        @NotNull
        public final Builder parse$okhttp(@Nullable HttpUrl httpUrl, @NotNull String str) {
            int i;
            int delimiterOffset;
            int i2;
            String str2;
            int i3;
            boolean z;
            String str3;
            int i4;
            boolean z2;
            String str4 = str;
            Intrinsics.checkNotNullParameter(str4, "input");
            int indexOfFirstNonAsciiWhitespace$default = Util.indexOfFirstNonAsciiWhitespace$default(str4, 0, 0, 3, (Object) null);
            int indexOfLastNonAsciiWhitespace$default = Util.indexOfLastNonAsciiWhitespace$default(str4, indexOfFirstNonAsciiWhitespace$default, 0, 2, (Object) null);
            Companion companion = Companion;
            int access$schemeDelimiterOffset = companion.schemeDelimiterOffset(str4, indexOfFirstNonAsciiWhitespace$default, indexOfLastNonAsciiWhitespace$default);
            String str5 = "this as java.lang.String…ing(startIndex, endIndex)";
            boolean z3 = true;
            char c = 65535;
            if (access$schemeDelimiterOffset != -1) {
                if (StringsKt__StringsJVMKt.startsWith(str4, "https:", indexOfFirstNonAsciiWhitespace$default, true)) {
                    this.scheme = OMAGlobalVariables.HTTPS;
                    indexOfFirstNonAsciiWhitespace$default += 6;
                } else if (StringsKt__StringsJVMKt.startsWith(str4, "http:", indexOfFirstNonAsciiWhitespace$default, true)) {
                    this.scheme = OMAGlobalVariables.HTTP;
                    indexOfFirstNonAsciiWhitespace$default += 5;
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Expected URL scheme 'http' or 'https' but was '");
                    String substring = str4.substring(0, access$schemeDelimiterOffset);
                    Intrinsics.checkNotNullExpressionValue(substring, str5);
                    sb.append(substring);
                    sb.append('\'');
                    throw new IllegalArgumentException(sb.toString());
                }
            } else if (httpUrl != null) {
                this.scheme = httpUrl.scheme();
            } else {
                if (str.length() > 6) {
                    str4 = Intrinsics.stringPlus(StringsKt___StringsKt.take(str4, 6), "...");
                }
                throw new IllegalArgumentException(Intrinsics.stringPlus("Expected URL scheme 'http' or 'https' but no scheme was found for ", str4));
            }
            int access$slashCount = companion.slashCount(str4, indexOfFirstNonAsciiWhitespace$default, indexOfLastNonAsciiWhitespace$default);
            char c2 = '?';
            char c3 = '#';
            if (access$slashCount >= 2 || httpUrl == null || !Intrinsics.areEqual(httpUrl.scheme(), this.scheme)) {
                int i5 = indexOfFirstNonAsciiWhitespace$default + access$slashCount;
                boolean z4 = false;
                boolean z5 = false;
                while (true) {
                    delimiterOffset = Util.delimiterOffset(str4, "@/\\?#", i5, indexOfLastNonAsciiWhitespace$default);
                    char charAt = delimiterOffset != indexOfLastNonAsciiWhitespace$default ? str4.charAt(delimiterOffset) : c;
                    if (charAt == c || charAt == c3 || charAt == '/' || charAt == '\\' || charAt == c2) {
                        boolean z6 = z3;
                        String str6 = str5;
                        i = indexOfLastNonAsciiWhitespace$default;
                        int i6 = delimiterOffset;
                        Companion companion2 = Companion;
                        int access$portColonOffset = companion2.portColonOffset(str4, i5, i6);
                        int i7 = access$portColonOffset + 1;
                    } else if (charAt == '@') {
                        if (!z4) {
                            int delimiterOffset2 = Util.delimiterOffset(str4, ':', i5, delimiterOffset);
                            Companion companion3 = HttpUrl.Companion;
                            String str7 = "%40";
                            int i8 = delimiterOffset;
                            int i9 = delimiterOffset2;
                            z = z3;
                            i3 = indexOfLastNonAsciiWhitespace$default;
                            String str8 = str5;
                            String canonicalize$okhttp$default = Companion.canonicalize$okhttp$default(companion3, str, i5, delimiterOffset2, " \"':;<=>@[]^`{}|/\\?#", true, false, false, false, (Charset) null, 240, (Object) null);
                            if (z5) {
                                canonicalize$okhttp$default = this.encodedUsername + str7 + canonicalize$okhttp$default;
                            }
                            this.encodedUsername = canonicalize$okhttp$default;
                            int i10 = i8;
                            int i11 = i9;
                            if (i11 != i10) {
                                this.encodedPassword = Companion.canonicalize$okhttp$default(companion3, str, i11 + 1, i10, " \"':;<=>@[]^`{}|/\\?#", true, false, false, false, (Charset) null, 240, (Object) null);
                                z2 = z;
                            } else {
                                z2 = z4;
                            }
                            z4 = z2;
                            str3 = str8;
                            z5 = z;
                            i4 = i10;
                        } else {
                            z = z3;
                            i3 = indexOfLastNonAsciiWhitespace$default;
                            String str9 = str5;
                            int i12 = delimiterOffset;
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append(this.encodedPassword);
                            sb2.append("%40");
                            StringBuilder sb3 = sb2;
                            str3 = str9;
                            i4 = i12;
                            sb3.append(Companion.canonicalize$okhttp$default(HttpUrl.Companion, str, i5, i12, " \"':;<=>@[]^`{}|/\\?#", true, false, false, false, (Charset) null, 240, (Object) null));
                            this.encodedPassword = sb3.toString();
                        }
                        i5 = i4 + 1;
                        str5 = str3;
                        z3 = z;
                        indexOfLastNonAsciiWhitespace$default = i3;
                        c3 = '#';
                        c2 = '?';
                        c = 65535;
                    }
                }
                boolean z62 = z3;
                String str62 = str5;
                i = indexOfLastNonAsciiWhitespace$default;
                int i62 = delimiterOffset;
                Companion companion22 = Companion;
                int access$portColonOffset2 = companion22.portColonOffset(str4, i5, i62);
                int i72 = access$portColonOffset2 + 1;
                if (i72 < i62) {
                    i2 = i5;
                    this.host = HostnamesKt.toCanonicalHost(Companion.percentDecode$okhttp$default(HttpUrl.Companion, str, i5, access$portColonOffset2, false, 4, (Object) null));
                    int access$parsePort = companion22.parsePort(str4, i72, i62);
                    this.port = access$parsePort;
                    if (access$parsePort != -1 ? z62 : false) {
                        str2 = str62;
                    } else {
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append("Invalid URL port: \"");
                        String substring2 = str4.substring(i72, i62);
                        Intrinsics.checkNotNullExpressionValue(substring2, str62);
                        sb4.append(substring2);
                        sb4.append('\"');
                        throw new IllegalArgumentException(sb4.toString().toString());
                    }
                } else {
                    i2 = i5;
                    str2 = str62;
                    Companion companion4 = HttpUrl.Companion;
                    this.host = HostnamesKt.toCanonicalHost(Companion.percentDecode$okhttp$default(companion4, str, i2, access$portColonOffset2, false, 4, (Object) null));
                    String str10 = this.scheme;
                    Intrinsics.checkNotNull(str10);
                    this.port = companion4.defaultPort(str10);
                }
                if (this.host != null ? z62 : false) {
                    indexOfFirstNonAsciiWhitespace$default = i62;
                } else {
                    StringBuilder sb5 = new StringBuilder();
                    sb5.append("Invalid URL host: \"");
                    String substring3 = str4.substring(i2, access$portColonOffset2);
                    Intrinsics.checkNotNullExpressionValue(substring3, str2);
                    sb5.append(substring3);
                    sb5.append('\"');
                    throw new IllegalArgumentException(sb5.toString().toString());
                }
            } else {
                this.encodedUsername = httpUrl.encodedUsername();
                this.encodedPassword = httpUrl.encodedPassword();
                this.host = httpUrl.host();
                this.port = httpUrl.port();
                this.encodedPathSegments.clear();
                this.encodedPathSegments.addAll(httpUrl.encodedPathSegments());
                if (indexOfFirstNonAsciiWhitespace$default == indexOfLastNonAsciiWhitespace$default || str4.charAt(indexOfFirstNonAsciiWhitespace$default) == '#') {
                    encodedQuery(httpUrl.encodedQuery());
                }
                i = indexOfLastNonAsciiWhitespace$default;
            }
            int i13 = i;
            int delimiterOffset3 = Util.delimiterOffset(str4, "?#", indexOfFirstNonAsciiWhitespace$default, i13);
            resolvePath(str4, indexOfFirstNonAsciiWhitespace$default, delimiterOffset3);
            if (delimiterOffset3 < i13 && str4.charAt(delimiterOffset3) == '?') {
                int delimiterOffset4 = Util.delimiterOffset(str4, '#', delimiterOffset3, i13);
                Companion companion5 = HttpUrl.Companion;
                this.encodedQueryNamesAndValues = companion5.toQueryNamesAndValues$okhttp(Companion.canonicalize$okhttp$default(companion5, str, delimiterOffset3 + 1, delimiterOffset4, " \"'<>#", true, false, true, false, (Charset) null, 208, (Object) null));
                delimiterOffset3 = delimiterOffset4;
            }
            if (delimiterOffset3 < i13 && str4.charAt(delimiterOffset3) == '#') {
                this.encodedFragment = Companion.canonicalize$okhttp$default(HttpUrl.Companion, str, delimiterOffset3 + 1, i13, "", true, false, false, true, (Charset) null, MNO.ORANGE_SENEGAL, (Object) null);
            }
            return this;
        }

        private final void resolvePath(String str, int i, int i2) {
            if (i != i2) {
                char charAt = str.charAt(i);
                if (charAt == '/' || charAt == '\\') {
                    this.encodedPathSegments.clear();
                    this.encodedPathSegments.add("");
                    i++;
                } else {
                    List<String> list = this.encodedPathSegments;
                    list.set(list.size() - 1, "");
                }
                while (true) {
                    int i3 = i;
                    while (true) {
                        if (i3 < i2) {
                            i = Util.delimiterOffset(str, "/\\", i3, i2);
                            boolean z = i < i2;
                            push(str, i3, i, z, true);
                            if (z) {
                                i3 = i + 1;
                            }
                        } else {
                            return;
                        }
                    }
                }
            }
        }

        private final void push(String str, int i, int i2, boolean z, boolean z2) {
            String canonicalize$okhttp$default = Companion.canonicalize$okhttp$default(HttpUrl.Companion, str, i, i2, " \"<>^`{}|/\\?#", z2, false, false, false, (Charset) null, 240, (Object) null);
            if (!isDot(canonicalize$okhttp$default)) {
                if (isDotDot(canonicalize$okhttp$default)) {
                    pop();
                    return;
                }
                List<String> list = this.encodedPathSegments;
                if (list.get(list.size() - 1).length() == 0) {
                    List<String> list2 = this.encodedPathSegments;
                    list2.set(list2.size() - 1, canonicalize$okhttp$default);
                } else {
                    this.encodedPathSegments.add(canonicalize$okhttp$default);
                }
                if (z) {
                    this.encodedPathSegments.add("");
                }
            }
        }

        private final boolean isDot(String str) {
            return Intrinsics.areEqual(str, ".") || StringsKt__StringsJVMKt.equals(str, "%2e", true);
        }

        private final boolean isDotDot(String str) {
            if (Intrinsics.areEqual(str, "..") || StringsKt__StringsJVMKt.equals(str, "%2e.", true) || StringsKt__StringsJVMKt.equals(str, ".%2e", true) || StringsKt__StringsJVMKt.equals(str, "%2e%2e", true)) {
                return true;
            }
            return false;
        }

        private final void pop() {
            List<String> list = this.encodedPathSegments;
            if (!(list.remove(list.size() - 1).length() == 0) || !(!this.encodedPathSegments.isEmpty())) {
                this.encodedPathSegments.add("");
                return;
            }
            List<String> list2 = this.encodedPathSegments;
            list2.set(list2.size() - 1, "");
        }

        /* compiled from: HttpUrl.kt */
        public static final class Companion {
            public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
                this();
            }

            private Companion() {
            }

            /* access modifiers changed from: private */
            public final int schemeDelimiterOffset(String str, int i, int i2) {
                if (i2 - i < 2) {
                    return -1;
                }
                char charAt = str.charAt(i);
                if ((Intrinsics.compare(charAt, 97) < 0 || Intrinsics.compare(charAt, 122) > 0) && (Intrinsics.compare(charAt, 65) < 0 || Intrinsics.compare(charAt, 90) > 0)) {
                    return -1;
                }
                int i3 = i + 1;
                while (i3 < i2) {
                    int i4 = i3 + 1;
                    char charAt2 = str.charAt(i3);
                    boolean z = false;
                    if (((((('a' <= charAt2 && charAt2 < '{') || ('A' <= charAt2 && charAt2 < '[')) || ('0' <= charAt2 && charAt2 < ':')) || charAt2 == '+') || charAt2 == '-') || charAt2 == '.') {
                        z = true;
                    }
                    if (z) {
                        i3 = i4;
                    } else if (charAt2 == ':') {
                        return i3;
                    } else {
                        return -1;
                    }
                }
                return -1;
            }

            /* access modifiers changed from: private */
            public final int slashCount(String str, int i, int i2) {
                int i3 = 0;
                while (i < i2) {
                    int i4 = i + 1;
                    char charAt = str.charAt(i);
                    if (charAt != '\\' && charAt != '/') {
                        break;
                    }
                    i3++;
                    i = i4;
                }
                return i3;
            }

            /* access modifiers changed from: private */
            public final int portColonOffset(String str, int i, int i2) {
                while (i < i2) {
                    char charAt = str.charAt(i);
                    if (charAt == '[') {
                        do {
                            i++;
                            if (i >= i2) {
                                break;
                            }
                        } while (str.charAt(i) == ']');
                    } else if (charAt == ':') {
                        return i;
                    }
                    i++;
                }
                return i2;
            }

            /* access modifiers changed from: private */
            public final int parsePort(String str, int i, int i2) {
                try {
                    int parseInt = Integer.parseInt(Companion.canonicalize$okhttp$default(HttpUrl.Companion, str, i, i2, "", false, false, false, false, (Charset) null, 248, (Object) null));
                    boolean z = false;
                    if (1 <= parseInt && parseInt < 65536) {
                        z = true;
                    }
                    if (z) {
                        return parseInt;
                    }
                    return -1;
                } catch (NumberFormatException unused) {
                    return -1;
                }
            }
        }
    }

    /* compiled from: HttpUrl.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public final int defaultPort(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "scheme");
            if (Intrinsics.areEqual(str, OMAGlobalVariables.HTTP)) {
                return 80;
            }
            return Intrinsics.areEqual(str, OMAGlobalVariables.HTTPS) ? 443 : -1;
        }

        public final void toPathString$okhttp(@NotNull List<String> list, @NotNull StringBuilder sb) {
            Intrinsics.checkNotNullParameter(list, "<this>");
            Intrinsics.checkNotNullParameter(sb, "out");
            int size = list.size();
            for (int i = 0; i < size; i++) {
                sb.append('/');
                sb.append(list.get(i));
            }
        }

        public final void toQueryString$okhttp(@NotNull List<String> list, @NotNull StringBuilder sb) {
            Intrinsics.checkNotNullParameter(list, "<this>");
            Intrinsics.checkNotNullParameter(sb, "out");
            IntProgression step = RangesKt___RangesKt.step(RangesKt___RangesKt.until(0, list.size()), 2);
            int first = step.getFirst();
            int last = step.getLast();
            int step2 = step.getStep();
            if ((step2 > 0 && first <= last) || (step2 < 0 && last <= first)) {
                while (true) {
                    int i = first + step2;
                    String str = list.get(first);
                    String str2 = list.get(first + 1);
                    if (first > 0) {
                        sb.append('&');
                    }
                    sb.append(str);
                    if (str2 != null) {
                        sb.append('=');
                        sb.append(str2);
                    }
                    if (first != last) {
                        first = i;
                    } else {
                        return;
                    }
                }
            }
        }

        @NotNull
        public final List<String> toQueryNamesAndValues$okhttp(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "<this>");
            ArrayList arrayList = new ArrayList();
            int i = 0;
            while (i <= str.length()) {
                int indexOf$default = StringsKt__StringsKt.indexOf$default((CharSequence) str, '&', i, false, 4, (Object) null);
                if (indexOf$default == -1) {
                    indexOf$default = str.length();
                }
                int i2 = indexOf$default;
                int indexOf$default2 = StringsKt__StringsKt.indexOf$default((CharSequence) str, '=', i, false, 4, (Object) null);
                if (indexOf$default2 == -1 || indexOf$default2 > i2) {
                    String substring = str.substring(i, i2);
                    Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
                    arrayList.add(substring);
                    arrayList.add((Object) null);
                } else {
                    String substring2 = str.substring(i, indexOf$default2);
                    Intrinsics.checkNotNullExpressionValue(substring2, "this as java.lang.String…ing(startIndex, endIndex)");
                    arrayList.add(substring2);
                    String substring3 = str.substring(indexOf$default2 + 1, i2);
                    Intrinsics.checkNotNullExpressionValue(substring3, "this as java.lang.String…ing(startIndex, endIndex)");
                    arrayList.add(substring3);
                }
                i = i2 + 1;
            }
            return arrayList;
        }

        @NotNull
        public final HttpUrl get(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "<this>");
            return new Builder().parse$okhttp((HttpUrl) null, str).build();
        }

        @Nullable
        public final HttpUrl parse(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "<this>");
            try {
                return get(str);
            } catch (IllegalArgumentException unused) {
                return null;
            }
        }

        @Nullable
        public final HttpUrl get(@NotNull URI uri) {
            Intrinsics.checkNotNullParameter(uri, "<this>");
            String uri2 = uri.toString();
            Intrinsics.checkNotNullExpressionValue(uri2, "toString()");
            return parse(uri2);
        }

        public static /* synthetic */ String percentDecode$okhttp$default(Companion companion, String str, int i, int i2, boolean z, int i3, Object obj) {
            if ((i3 & 1) != 0) {
                i = 0;
            }
            if ((i3 & 2) != 0) {
                i2 = str.length();
            }
            if ((i3 & 4) != 0) {
                z = false;
            }
            return companion.percentDecode$okhttp(str, i, i2, z);
        }

        @NotNull
        public final String percentDecode$okhttp(@NotNull String str, int i, int i2, boolean z) {
            Intrinsics.checkNotNullParameter(str, "<this>");
            int i3 = i;
            while (i3 < i2) {
                int i4 = i3 + 1;
                char charAt = str.charAt(i3);
                if (charAt == '%' || (charAt == '+' && z)) {
                    Buffer buffer = new Buffer();
                    buffer.writeUtf8(str, i, i3);
                    writePercentDecoded(buffer, str, i3, i2, z);
                    return buffer.readUtf8();
                }
                i3 = i4;
            }
            String substring = str.substring(i, i2);
            Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
            return substring;
        }

        private final void writePercentDecoded(Buffer buffer, String str, int i, int i2, boolean z) {
            int i3;
            while (i < i2) {
                int codePointAt = str.codePointAt(i);
                if (codePointAt == 37 && (i3 = i + 2) < i2) {
                    int parseHexDigit = Util.parseHexDigit(str.charAt(i + 1));
                    int parseHexDigit2 = Util.parseHexDigit(str.charAt(i3));
                    if (!(parseHexDigit == -1 || parseHexDigit2 == -1)) {
                        buffer.writeByte((parseHexDigit << 4) + parseHexDigit2);
                        i = i3 + Character.charCount(codePointAt);
                    }
                } else if (codePointAt == 43 && z) {
                    buffer.writeByte(32);
                    i++;
                }
                buffer.writeUtf8CodePoint(codePointAt);
                i += Character.charCount(codePointAt);
            }
        }

        private final boolean isPercentEncoded(String str, int i, int i2) {
            int i3 = i + 2;
            if (i3 >= i2 || str.charAt(i) != '%' || Util.parseHexDigit(str.charAt(i + 1)) == -1 || Util.parseHexDigit(str.charAt(i3)) == -1) {
                return false;
            }
            return true;
        }

        public static /* synthetic */ String canonicalize$okhttp$default(Companion companion, String str, int i, int i2, String str2, boolean z, boolean z2, boolean z3, boolean z4, Charset charset, int i3, Object obj) {
            int i4 = i3;
            return companion.canonicalize$okhttp(str, (i4 & 1) != 0 ? 0 : i, (i4 & 2) != 0 ? str.length() : i2, str2, (i4 & 8) != 0 ? false : z, (i4 & 16) != 0 ? false : z2, (i4 & 32) != 0 ? false : z3, (i4 & 64) != 0 ? false : z4, (i4 & 128) != 0 ? null : charset);
        }

        @NotNull
        public final String canonicalize$okhttp(@NotNull String str, int i, int i2, @NotNull String str2, boolean z, boolean z2, boolean z3, boolean z4, @Nullable Charset charset) {
            String str3 = str;
            int i3 = i2;
            String str4 = str2;
            Intrinsics.checkNotNullParameter(str, "<this>");
            Intrinsics.checkNotNullParameter(str4, "encodeSet");
            int i4 = i;
            while (i4 < i3) {
                int codePointAt = str.codePointAt(i4);
                if (codePointAt >= 32 && codePointAt != 127 && ((codePointAt < 128 || z4) && !StringsKt__StringsKt.contains$default((CharSequence) str4, (char) codePointAt, false, 2, (Object) null))) {
                    if (codePointAt == 37) {
                        if (z) {
                            if (z2) {
                                if (!isPercentEncoded(str, i4, i2)) {
                                    Buffer buffer = new Buffer();
                                    int i5 = i;
                                    buffer.writeUtf8(str, i, i4);
                                    writeCanonicalized(buffer, str, i4, i2, str2, z, z2, z3, z4, charset);
                                    return buffer.readUtf8();
                                }
                                if (codePointAt == 43 || !z3) {
                                    i4 += Character.charCount(codePointAt);
                                } else {
                                    Buffer buffer2 = new Buffer();
                                    int i52 = i;
                                    buffer2.writeUtf8(str, i, i4);
                                    writeCanonicalized(buffer2, str, i4, i2, str2, z, z2, z3, z4, charset);
                                    return buffer2.readUtf8();
                                }
                            }
                        }
                    }
                    if (codePointAt == 43) {
                    }
                    i4 += Character.charCount(codePointAt);
                }
                Buffer buffer22 = new Buffer();
                int i522 = i;
                buffer22.writeUtf8(str, i, i4);
                writeCanonicalized(buffer22, str, i4, i2, str2, z, z2, z3, z4, charset);
                return buffer22.readUtf8();
            }
            int i6 = i;
            String substring = str.substring(i, i2);
            Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
            return substring;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:33:0x005f, code lost:
            if (isPercentEncoded(r1, r5, r2) == false) goto L_0x006c;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private final void writeCanonicalized(okio.Buffer r15, java.lang.String r16, int r17, int r18, java.lang.String r19, boolean r20, boolean r21, boolean r22, boolean r23, java.nio.charset.Charset r24) {
            /*
                r14 = this;
                r0 = r15
                r1 = r16
                r2 = r18
                r3 = r24
                r4 = 0
                r5 = r17
                r6 = r4
            L_0x000b:
                if (r5 >= r2) goto L_0x00b9
                int r7 = r1.codePointAt(r5)
                if (r20 == 0) goto L_0x0028
                r8 = 9
                if (r7 == r8) goto L_0x0023
                r8 = 10
                if (r7 == r8) goto L_0x0023
                r8 = 12
                if (r7 == r8) goto L_0x0023
                r8 = 13
                if (r7 != r8) goto L_0x0028
            L_0x0023:
                r8 = r14
                r12 = r19
                goto L_0x00b2
            L_0x0028:
                r8 = 43
                if (r7 != r8) goto L_0x0039
                if (r22 == 0) goto L_0x0039
                if (r20 == 0) goto L_0x0033
                java.lang.String r8 = "+"
                goto L_0x0035
            L_0x0033:
                java.lang.String r8 = "%2B"
            L_0x0035:
                r15.writeUtf8((java.lang.String) r8)
                goto L_0x0023
            L_0x0039:
                r8 = 32
                r9 = 37
                if (r7 < r8) goto L_0x0069
                r8 = 127(0x7f, float:1.78E-43)
                if (r7 == r8) goto L_0x0069
                r8 = 128(0x80, float:1.794E-43)
                if (r7 < r8) goto L_0x0049
                if (r23 == 0) goto L_0x0069
            L_0x0049:
                char r8 = (char) r7
                r10 = 0
                r11 = 2
                r12 = r19
                boolean r8 = kotlin.text.StringsKt__StringsKt.contains$default((java.lang.CharSequence) r12, (char) r8, (boolean) r10, (int) r11, (java.lang.Object) r4)
                if (r8 != 0) goto L_0x0067
                if (r7 != r9) goto L_0x0062
                if (r20 == 0) goto L_0x0067
                if (r21 == 0) goto L_0x0062
                r8 = r14
                boolean r10 = r14.isPercentEncoded(r1, r5, r2)
                if (r10 != 0) goto L_0x0063
                goto L_0x006c
            L_0x0062:
                r8 = r14
            L_0x0063:
                r15.writeUtf8CodePoint(r7)
                goto L_0x00b2
            L_0x0067:
                r8 = r14
                goto L_0x006c
            L_0x0069:
                r8 = r14
                r12 = r19
            L_0x006c:
                if (r6 != 0) goto L_0x0073
                okio.Buffer r6 = new okio.Buffer
                r6.<init>()
            L_0x0073:
                if (r3 == 0) goto L_0x0087
                java.nio.charset.Charset r10 = java.nio.charset.StandardCharsets.UTF_8
                boolean r10 = kotlin.jvm.internal.Intrinsics.areEqual(r3, r10)
                if (r10 == 0) goto L_0x007e
                goto L_0x0087
            L_0x007e:
                int r10 = java.lang.Character.charCount(r7)
                int r10 = r10 + r5
                r6.writeString(r1, r5, r10, r3)
                goto L_0x008a
            L_0x0087:
                r6.writeUtf8CodePoint(r7)
            L_0x008a:
                boolean r10 = r6.exhausted()
                if (r10 != 0) goto L_0x00b2
                byte r10 = r6.readByte()
                r10 = r10 & 255(0xff, float:3.57E-43)
                r15.writeByte((int) r9)
                char[] r11 = okhttp3.HttpUrl.HEX_DIGITS
                int r13 = r10 >> 4
                r13 = r13 & 15
                char r11 = r11[r13]
                r15.writeByte((int) r11)
                char[] r11 = okhttp3.HttpUrl.HEX_DIGITS
                r10 = r10 & 15
                char r10 = r11[r10]
                r15.writeByte((int) r10)
                goto L_0x008a
            L_0x00b2:
                int r7 = java.lang.Character.charCount(r7)
                int r5 = r5 + r7
                goto L_0x000b
            L_0x00b9:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: okhttp3.HttpUrl.Companion.writeCanonicalized(okio.Buffer, java.lang.String, int, int, java.lang.String, boolean, boolean, boolean, boolean, java.nio.charset.Charset):void");
        }
    }
}
