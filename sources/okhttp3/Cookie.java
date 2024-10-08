package okhttp3;

import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.core.cmc.CmcConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Regex;
import okhttp3.internal.HostnamesKt;
import okhttp3.internal.Util;
import okhttp3.internal.http.DatesKt;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Cookie.kt */
public final class Cookie {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    /* access modifiers changed from: private */
    public static final Pattern DAY_OF_MONTH_PATTERN = Pattern.compile("(\\d{1,2})[^\\d]*");
    /* access modifiers changed from: private */
    public static final Pattern MONTH_PATTERN = Pattern.compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec).*");
    /* access modifiers changed from: private */
    public static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{1,2}):(\\d{1,2})[^\\d]*");
    /* access modifiers changed from: private */
    public static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{2,4})[^\\d]*");
    @NotNull
    private final String domain;
    private final long expiresAt;
    private final boolean hostOnly;
    private final boolean httpOnly;
    @NotNull
    private final String name;
    @NotNull
    private final String path;
    private final boolean persistent;
    private final boolean secure;
    @NotNull
    private final String value;

    public /* synthetic */ Cookie(String str, String str2, long j, String str3, String str4, boolean z, boolean z2, boolean z3, boolean z4, DefaultConstructorMarker defaultConstructorMarker) {
        this(str, str2, j, str3, str4, z, z2, z3, z4);
    }

    private Cookie(String str, String str2, long j, String str3, String str4, boolean z, boolean z2, boolean z3, boolean z4) {
        this.name = str;
        this.value = str2;
        this.expiresAt = j;
        this.domain = str3;
        this.path = str4;
        this.secure = z;
        this.httpOnly = z2;
        this.persistent = z3;
        this.hostOnly = z4;
    }

    @NotNull
    public final String name() {
        return this.name;
    }

    @NotNull
    public final String value() {
        return this.value;
    }

    public final long expiresAt() {
        return this.expiresAt;
    }

    @NotNull
    public final String domain() {
        return this.domain;
    }

    @NotNull
    public final String path() {
        return this.path;
    }

    public final boolean secure() {
        return this.secure;
    }

    public final boolean httpOnly() {
        return this.httpOnly;
    }

    public final boolean persistent() {
        return this.persistent;
    }

    public final boolean hostOnly() {
        return this.hostOnly;
    }

    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Cookie) {
            Cookie cookie = (Cookie) obj;
            return Intrinsics.areEqual(cookie.name, this.name) && Intrinsics.areEqual(cookie.value, this.value) && cookie.expiresAt == this.expiresAt && Intrinsics.areEqual(cookie.domain, this.domain) && Intrinsics.areEqual(cookie.path, this.path) && cookie.secure == this.secure && cookie.httpOnly == this.httpOnly && cookie.persistent == this.persistent && cookie.hostOnly == this.hostOnly;
        }
    }

    @IgnoreJRERequirement
    public int hashCode() {
        return ((((((((((((((((527 + this.name.hashCode()) * 31) + this.value.hashCode()) * 31) + Long.hashCode(this.expiresAt)) * 31) + this.domain.hashCode()) * 31) + this.path.hashCode()) * 31) + Boolean.hashCode(this.secure)) * 31) + Boolean.hashCode(this.httpOnly)) * 31) + Boolean.hashCode(this.persistent)) * 31) + Boolean.hashCode(this.hostOnly);
    }

    @NotNull
    public String toString() {
        return toString$okhttp(false);
    }

    @NotNull
    public final String toString$okhttp(boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append(name());
        sb.append('=');
        sb.append(value());
        if (persistent()) {
            if (expiresAt() == Long.MIN_VALUE) {
                sb.append("; max-age=0");
            } else {
                sb.append("; expires=");
                sb.append(DatesKt.toHttpDateString(new Date(expiresAt())));
            }
        }
        if (!hostOnly()) {
            sb.append("; domain=");
            if (z) {
                sb.append(".");
            }
            sb.append(domain());
        }
        sb.append("; path=");
        sb.append(path());
        if (secure()) {
            sb.append("; secure");
        }
        if (httpOnly()) {
            sb.append("; httponly");
        }
        String sb2 = sb.toString();
        Intrinsics.checkNotNullExpressionValue(sb2, "toString()");
        return sb2;
    }

    /* compiled from: Cookie.kt */
    public static final class Builder {
        @Nullable
        private String domain;
        private long expiresAt = 253402300799999L;
        private boolean hostOnly;
        private boolean httpOnly;
        @Nullable
        private String name;
        @NotNull
        private String path = "/";
        private boolean persistent;
        private boolean secure;
        @Nullable
        private String value;

        @NotNull
        public final Builder name(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "name");
            if (Intrinsics.areEqual(StringsKt__StringsKt.trim(str).toString(), str)) {
                this.name = str;
                return this;
            }
            throw new IllegalArgumentException("name is not trimmed".toString());
        }

        @NotNull
        public final Builder value(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "value");
            if (Intrinsics.areEqual(StringsKt__StringsKt.trim(str).toString(), str)) {
                this.value = str;
                return this;
            }
            throw new IllegalArgumentException("value is not trimmed".toString());
        }

        @NotNull
        public final Builder expiresAt(long j) {
            if (j <= 0) {
                j = Long.MIN_VALUE;
            }
            if (j > 253402300799999L) {
                j = 253402300799999L;
            }
            this.expiresAt = j;
            this.persistent = true;
            return this;
        }

        @NotNull
        public final Builder domain(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "domain");
            return domain(str, false);
        }

        @NotNull
        public final Builder hostOnlyDomain(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "domain");
            return domain(str, true);
        }

        private final Builder domain(String str, boolean z) {
            String canonicalHost = HostnamesKt.toCanonicalHost(str);
            if (canonicalHost != null) {
                this.domain = canonicalHost;
                this.hostOnly = z;
                return this;
            }
            throw new IllegalArgumentException(Intrinsics.stringPlus("unexpected domain: ", str));
        }

        @NotNull
        public final Builder path(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "path");
            if (StringsKt__StringsJVMKt.startsWith$default(str, "/", false, 2, (Object) null)) {
                this.path = str;
                return this;
            }
            throw new IllegalArgumentException("path must start with '/'".toString());
        }

        @NotNull
        public final Builder secure() {
            this.secure = true;
            return this;
        }

        @NotNull
        public final Builder httpOnly() {
            this.httpOnly = true;
            return this;
        }

        @NotNull
        public final Cookie build() {
            String str = this.name;
            if (str != null) {
                String str2 = this.value;
                if (str2 != null) {
                    long j = this.expiresAt;
                    String str3 = this.domain;
                    if (str3 != null) {
                        return new Cookie(str, str2, j, str3, this.path, this.secure, this.httpOnly, this.persistent, this.hostOnly, (DefaultConstructorMarker) null);
                    }
                    throw new NullPointerException("builder.domain == null");
                }
                throw new NullPointerException("builder.value == null");
            }
            throw new NullPointerException("builder.name == null");
        }
    }

    /* compiled from: Cookie.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        private final boolean domainMatch(String str, String str2) {
            if (Intrinsics.areEqual(str, str2)) {
                return true;
            }
            if (!StringsKt__StringsJVMKt.endsWith$default(str, str2, false, 2, (Object) null) || str.charAt((str.length() - str2.length()) - 1) != '.' || Util.canParseAsIpAddress(str)) {
                return false;
            }
            return true;
        }

        @Nullable
        public final Cookie parse(@NotNull HttpUrl httpUrl, @NotNull String str) {
            Intrinsics.checkNotNullParameter(httpUrl, ImsConstants.FtDlParams.FT_DL_URL);
            Intrinsics.checkNotNullParameter(str, "setCookie");
            return parse$okhttp(System.currentTimeMillis(), httpUrl, str);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:56:0x0105, code lost:
            if (r1 > 253402300799999L) goto L_0x010d;
         */
        /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
        /* JADX WARNING: Removed duplicated region for block: B:62:0x0117  */
        /* JADX WARNING: Removed duplicated region for block: B:63:0x011a  */
        /* JADX WARNING: Removed duplicated region for block: B:71:0x013a A[RETURN] */
        /* JADX WARNING: Removed duplicated region for block: B:72:0x013b  */
        @org.jetbrains.annotations.Nullable
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public final okhttp3.Cookie parse$okhttp(long r26, @org.jetbrains.annotations.NotNull okhttp3.HttpUrl r28, @org.jetbrains.annotations.NotNull java.lang.String r29) {
            /*
                r25 = this;
                r0 = r25
                r7 = r29
                java.lang.String r1 = "url"
                r8 = r28
                kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r8, r1)
                java.lang.String r1 = "setCookie"
                kotlin.jvm.internal.Intrinsics.checkNotNullParameter(r7, r1)
                r2 = 59
                r3 = 0
                r4 = 0
                r5 = 6
                r6 = 0
                r1 = r29
                int r9 = okhttp3.internal.Util.delimiterOffset$default(r1, r2, r3, r4, r5, r6)
                r2 = 61
                r5 = 2
                r4 = r9
                int r1 = okhttp3.internal.Util.delimiterOffset$default(r1, r2, r3, r4, r5, r6)
                r2 = 0
                if (r1 != r9) goto L_0x002a
                return r2
            L_0x002a:
                r3 = 0
                r4 = 1
                java.lang.String r11 = okhttp3.internal.Util.trimSubstring$default(r7, r3, r1, r4, r2)
                int r5 = r11.length()
                if (r5 != 0) goto L_0x0038
                r5 = r4
                goto L_0x0039
            L_0x0038:
                r5 = r3
            L_0x0039:
                if (r5 != 0) goto L_0x0172
                int r5 = okhttp3.internal.Util.indexOfControlOrNonAscii(r11)
                r6 = -1
                if (r5 == r6) goto L_0x0044
                goto L_0x0172
            L_0x0044:
                int r1 = r1 + r4
                java.lang.String r12 = okhttp3.internal.Util.trimSubstring(r7, r1, r9)
                int r1 = okhttp3.internal.Util.indexOfControlOrNonAscii(r12)
                if (r1 == r6) goto L_0x0050
                return r2
            L_0x0050:
                int r9 = r9 + r4
                int r1 = r29.length()
                r5 = -1
                r10 = r2
                r22 = r10
                r17 = r3
                r18 = r17
                r19 = r18
                r20 = r4
                r15 = r5
                r23 = 253402300799999(0xe677d21fdbff, double:1.251973714024093E-309)
            L_0x0068:
                if (r9 >= r1) goto L_0x00d9
                r2 = 59
                int r2 = okhttp3.internal.Util.delimiterOffset((java.lang.String) r7, (char) r2, (int) r9, (int) r1)
                r13 = 61
                int r13 = okhttp3.internal.Util.delimiterOffset((java.lang.String) r7, (char) r13, (int) r9, (int) r2)
                java.lang.String r9 = okhttp3.internal.Util.trimSubstring(r7, r9, r13)
                if (r13 >= r2) goto L_0x0083
                int r13 = r13 + 1
                java.lang.String r13 = okhttp3.internal.Util.trimSubstring(r7, r13, r2)
                goto L_0x0085
            L_0x0083:
                java.lang.String r13 = ""
            L_0x0085:
                java.lang.String r14 = "expires"
                boolean r14 = kotlin.text.StringsKt__StringsJVMKt.equals(r9, r14, r4)
                if (r14 == 0) goto L_0x0096
                int r9 = r13.length()     // Catch:{ IllegalArgumentException -> 0x00d5 }
                long r23 = r0.parseExpires(r13, r3, r9)     // Catch:{ IllegalArgumentException -> 0x00d5 }
                goto L_0x00a2
            L_0x0096:
                java.lang.String r14 = "max-age"
                boolean r14 = kotlin.text.StringsKt__StringsJVMKt.equals(r9, r14, r4)
                if (r14 == 0) goto L_0x00a5
                long r15 = r0.parseMaxAge(r13)     // Catch:{  }
            L_0x00a2:
                r19 = r4
                goto L_0x00d5
            L_0x00a5:
                java.lang.String r14 = "domain"
                boolean r14 = kotlin.text.StringsKt__StringsJVMKt.equals(r9, r14, r4)
                if (r14 == 0) goto L_0x00b4
                java.lang.String r10 = r0.parseDomain(r13)     // Catch:{ IllegalArgumentException -> 0x00d5 }
                r20 = r3
                goto L_0x00d5
            L_0x00b4:
                java.lang.String r14 = "path"
                boolean r14 = kotlin.text.StringsKt__StringsJVMKt.equals(r9, r14, r4)
                if (r14 == 0) goto L_0x00bf
                r22 = r13
                goto L_0x00d5
            L_0x00bf:
                java.lang.String r13 = "secure"
                boolean r13 = kotlin.text.StringsKt__StringsJVMKt.equals(r9, r13, r4)
                if (r13 == 0) goto L_0x00cb
                r17 = r4
                goto L_0x00d5
            L_0x00cb:
                java.lang.String r13 = "httponly"
                boolean r9 = kotlin.text.StringsKt__StringsJVMKt.equals(r9, r13, r4)
                if (r9 == 0) goto L_0x00d5
                r18 = r4
            L_0x00d5:
                int r9 = r2 + 1
                r2 = 0
                goto L_0x0068
            L_0x00d9:
                r1 = -9223372036854775808
                int r4 = (r15 > r1 ? 1 : (r15 == r1 ? 0 : -1))
                if (r4 != 0) goto L_0x00e1
            L_0x00df:
                r13 = r1
                goto L_0x0111
            L_0x00e1:
                int r1 = (r15 > r5 ? 1 : (r15 == r5 ? 0 : -1))
                if (r1 == 0) goto L_0x010f
                r1 = 9223372036854775(0x20c49ba5e353f7, double:4.663754807431093E-308)
                int r1 = (r15 > r1 ? 1 : (r15 == r1 ? 0 : -1))
                if (r1 > 0) goto L_0x00f3
                r1 = 1000(0x3e8, float:1.401E-42)
                long r1 = (long) r1
                long r15 = r15 * r1
                goto L_0x00f8
            L_0x00f3:
                r15 = 9223372036854775807(0x7fffffffffffffff, double:NaN)
            L_0x00f8:
                long r1 = r26 + r15
                int r4 = (r1 > r26 ? 1 : (r1 == r26 ? 0 : -1))
                if (r4 < 0) goto L_0x0108
                r4 = 253402300799999(0xe677d21fdbff, double:1.251973714024093E-309)
                int r6 = (r1 > r4 ? 1 : (r1 == r4 ? 0 : -1))
                if (r6 <= 0) goto L_0x00df
                goto L_0x010d
            L_0x0108:
                r4 = 253402300799999(0xe677d21fdbff, double:1.251973714024093E-309)
            L_0x010d:
                r13 = r4
                goto L_0x0111
            L_0x010f:
                r13 = r23
            L_0x0111:
                java.lang.String r1 = r28.host()
                if (r10 != 0) goto L_0x011a
                r15 = r1
                r0 = 0
                goto L_0x0124
            L_0x011a:
                boolean r0 = r0.domainMatch(r1, r10)
                if (r0 != 0) goto L_0x0122
                r0 = 0
                return r0
            L_0x0122:
                r0 = 0
                r15 = r10
            L_0x0124:
                int r1 = r1.length()
                int r2 = r15.length()
                if (r1 == r2) goto L_0x013b
                okhttp3.internal.publicsuffix.PublicSuffixDatabase$Companion r1 = okhttp3.internal.publicsuffix.PublicSuffixDatabase.Companion
                okhttp3.internal.publicsuffix.PublicSuffixDatabase r1 = r1.get()
                java.lang.String r1 = r1.getEffectiveTldPlusOne(r15)
                if (r1 != 0) goto L_0x013b
                return r0
            L_0x013b:
                java.lang.String r1 = "/"
                r2 = r22
                if (r2 == 0) goto L_0x014c
                r4 = 2
                boolean r0 = kotlin.text.StringsKt__StringsJVMKt.startsWith$default(r2, r1, r3, r4, r0)
                if (r0 != 0) goto L_0x0149
                goto L_0x014c
            L_0x0149:
                r16 = r2
                goto L_0x0169
            L_0x014c:
                java.lang.String r0 = r28.encodedPath()
                r5 = 47
                r6 = 0
                r7 = 0
                r8 = 6
                r9 = 0
                r4 = r0
                int r2 = kotlin.text.StringsKt__StringsKt.lastIndexOf$default((java.lang.CharSequence) r4, (char) r5, (int) r6, (boolean) r7, (int) r8, (java.lang.Object) r9)
                if (r2 == 0) goto L_0x0167
                java.lang.String r1 = r0.substring(r3, r2)
                java.lang.String r0 = "this as java.lang.String…ing(startIndex, endIndex)"
                kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r1, r0)
            L_0x0167:
                r16 = r1
            L_0x0169:
                okhttp3.Cookie r0 = new okhttp3.Cookie
                r21 = 0
                r10 = r0
                r10.<init>(r11, r12, r13, r15, r16, r17, r18, r19, r20, r21)
                return r0
            L_0x0172:
                r0 = r2
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: okhttp3.Cookie.Companion.parse$okhttp(long, okhttp3.HttpUrl, java.lang.String):okhttp3.Cookie");
        }

        private final long parseExpires(String str, int i, int i2) {
            String str2 = str;
            int i3 = i2;
            int dateCharacterOffset = dateCharacterOffset(str2, i, i3, false);
            Matcher matcher = Cookie.TIME_PATTERN.matcher(str2);
            int i4 = -1;
            int i5 = -1;
            int i6 = -1;
            int i7 = -1;
            int i8 = -1;
            int i9 = -1;
            while (dateCharacterOffset < i3) {
                int dateCharacterOffset2 = dateCharacterOffset(str2, dateCharacterOffset + 1, i3, true);
                matcher.region(dateCharacterOffset, dateCharacterOffset2);
                if (i5 == -1 && matcher.usePattern(Cookie.TIME_PATTERN).matches()) {
                    String group = matcher.group(1);
                    Intrinsics.checkNotNullExpressionValue(group, "matcher.group(1)");
                    i5 = Integer.parseInt(group);
                    String group2 = matcher.group(2);
                    Intrinsics.checkNotNullExpressionValue(group2, "matcher.group(2)");
                    i8 = Integer.parseInt(group2);
                    String group3 = matcher.group(3);
                    Intrinsics.checkNotNullExpressionValue(group3, "matcher.group(3)");
                    i9 = Integer.parseInt(group3);
                } else if (i6 == -1 && matcher.usePattern(Cookie.DAY_OF_MONTH_PATTERN).matches()) {
                    String group4 = matcher.group(1);
                    Intrinsics.checkNotNullExpressionValue(group4, "matcher.group(1)");
                    i6 = Integer.parseInt(group4);
                } else if (i7 == -1 && matcher.usePattern(Cookie.MONTH_PATTERN).matches()) {
                    String group5 = matcher.group(1);
                    Intrinsics.checkNotNullExpressionValue(group5, "matcher.group(1)");
                    Locale locale = Locale.US;
                    Intrinsics.checkNotNullExpressionValue(locale, "US");
                    String lowerCase = group5.toLowerCase(locale);
                    Intrinsics.checkNotNullExpressionValue(lowerCase, "this as java.lang.String).toLowerCase(locale)");
                    String pattern = Cookie.MONTH_PATTERN.pattern();
                    Intrinsics.checkNotNullExpressionValue(pattern, "MONTH_PATTERN.pattern()");
                    i7 = StringsKt__StringsKt.indexOf$default((CharSequence) pattern, lowerCase, 0, false, 6, (Object) null) / 4;
                } else if (i4 == -1 && matcher.usePattern(Cookie.YEAR_PATTERN).matches()) {
                    String group6 = matcher.group(1);
                    Intrinsics.checkNotNullExpressionValue(group6, "matcher.group(1)");
                    i4 = Integer.parseInt(group6);
                }
                dateCharacterOffset = dateCharacterOffset(str2, dateCharacterOffset2 + 1, i3, false);
            }
            if (70 <= i4 && i4 < 100) {
                i4 += NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_PUSH_TOKEN_GEN_FAILURE;
            }
            if (i4 >= 0 && i4 < 70) {
                i4 += 2000;
            }
            if (i4 >= 1601) {
                if (i7 != -1) {
                    if (1 <= i6 && i6 < 32) {
                        if (i5 >= 0 && i5 < 24) {
                            if (i8 >= 0 && i8 < 60) {
                                if (i9 >= 0 && i9 < 60) {
                                    GregorianCalendar gregorianCalendar = new GregorianCalendar(Util.UTC);
                                    gregorianCalendar.setLenient(false);
                                    gregorianCalendar.set(1, i4);
                                    gregorianCalendar.set(2, i7 - 1);
                                    gregorianCalendar.set(5, i6);
                                    gregorianCalendar.set(11, i5);
                                    gregorianCalendar.set(12, i8);
                                    gregorianCalendar.set(13, i9);
                                    gregorianCalendar.set(14, 0);
                                    return gregorianCalendar.getTimeInMillis();
                                }
                                throw new IllegalArgumentException("Failed requirement.".toString());
                            }
                            throw new IllegalArgumentException("Failed requirement.".toString());
                        }
                        throw new IllegalArgumentException("Failed requirement.".toString());
                    }
                    throw new IllegalArgumentException("Failed requirement.".toString());
                }
                throw new IllegalArgumentException("Failed requirement.".toString());
            }
            throw new IllegalArgumentException("Failed requirement.".toString());
        }

        /* JADX WARNING: Code restructure failed: missing block: B:29:0x003f, code lost:
            if (r0 != ':') goto L_0x0042;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private final int dateCharacterOffset(java.lang.String r5, int r6, int r7, boolean r8) {
            /*
                r4 = this;
            L_0x0000:
                if (r6 >= r7) goto L_0x0049
                int r4 = r6 + 1
                char r0 = r5.charAt(r6)
                r1 = 32
                r2 = 1
                if (r0 >= r1) goto L_0x0011
                r1 = 9
                if (r0 != r1) goto L_0x0041
            L_0x0011:
                r1 = 127(0x7f, float:1.78E-43)
                if (r0 >= r1) goto L_0x0041
                r1 = 57
                r3 = 0
                if (r0 > r1) goto L_0x0020
                r1 = 48
                if (r1 > r0) goto L_0x0020
                r1 = r2
                goto L_0x0021
            L_0x0020:
                r1 = r3
            L_0x0021:
                if (r1 != 0) goto L_0x0041
                r1 = 122(0x7a, float:1.71E-43)
                if (r0 > r1) goto L_0x002d
                r1 = 97
                if (r1 > r0) goto L_0x002d
                r1 = r2
                goto L_0x002e
            L_0x002d:
                r1 = r3
            L_0x002e:
                if (r1 != 0) goto L_0x0041
                r1 = 90
                if (r0 > r1) goto L_0x003a
                r1 = 65
                if (r1 > r0) goto L_0x003a
                r1 = r2
                goto L_0x003b
            L_0x003a:
                r1 = r3
            L_0x003b:
                if (r1 != 0) goto L_0x0041
                r1 = 58
                if (r0 != r1) goto L_0x0042
            L_0x0041:
                r3 = r2
            L_0x0042:
                r0 = r8 ^ 1
                if (r3 != r0) goto L_0x0047
                return r6
            L_0x0047:
                r6 = r4
                goto L_0x0000
            L_0x0049:
                return r7
            */
            throw new UnsupportedOperationException("Method not decompiled: okhttp3.Cookie.Companion.dateCharacterOffset(java.lang.String, int, int, boolean):int");
        }

        private final long parseMaxAge(String str) {
            try {
                long parseLong = Long.parseLong(str);
                if (parseLong <= 0) {
                    return Long.MIN_VALUE;
                }
                return parseLong;
            } catch (NumberFormatException e) {
                if (!new Regex("-?\\d+").matches(str)) {
                    throw e;
                } else if (StringsKt__StringsJVMKt.startsWith$default(str, CmcConstants.E_NUM_SLOT_SPLIT, false, 2, (Object) null)) {
                    return Long.MIN_VALUE;
                } else {
                    return Long.MAX_VALUE;
                }
            }
        }

        private final String parseDomain(String str) {
            if (!StringsKt__StringsJVMKt.endsWith$default(str, ".", false, 2, (Object) null)) {
                String canonicalHost = HostnamesKt.toCanonicalHost(StringsKt__StringsKt.removePrefix(str, "."));
                if (canonicalHost != null) {
                    return canonicalHost;
                }
                throw new IllegalArgumentException();
            }
            throw new IllegalArgumentException("Failed requirement.".toString());
        }

        @NotNull
        public final List<Cookie> parseAll(@NotNull HttpUrl httpUrl, @NotNull Headers headers) {
            Intrinsics.checkNotNullParameter(httpUrl, ImsConstants.FtDlParams.FT_DL_URL);
            Intrinsics.checkNotNullParameter(headers, "headers");
            List<String> values = headers.values(HttpController.HEADER_SET_COOKIE);
            int size = values.size();
            ArrayList arrayList = null;
            int i = 0;
            while (i < size) {
                int i2 = i + 1;
                Cookie parse = parse(httpUrl, values.get(i));
                if (parse != null) {
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                    }
                    arrayList.add(parse);
                }
                i = i2;
            }
            if (arrayList == null) {
                return CollectionsKt__CollectionsKt.emptyList();
            }
            List<Cookie> unmodifiableList = Collections.unmodifiableList(arrayList);
            Intrinsics.checkNotNullExpressionValue(unmodifiableList, "{\n        Collections.un…ableList(cookies)\n      }");
            return unmodifiableList;
        }
    }
}
