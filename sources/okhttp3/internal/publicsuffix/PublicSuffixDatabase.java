package okhttp3.internal.publicsuffix;

import java.net.IDN;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PublicSuffixDatabase.kt */
public final class PublicSuffixDatabase {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    @NotNull
    private static final List<String> PREVAILING_RULE = CollectionsKt__CollectionsJVMKt.listOf("*");
    @NotNull
    private static final byte[] WILDCARD_LABEL = {42};
    /* access modifiers changed from: private */
    @NotNull
    public static final PublicSuffixDatabase instance = new PublicSuffixDatabase();
    @NotNull
    private final AtomicBoolean listRead = new AtomicBoolean(false);
    private byte[] publicSuffixExceptionListBytes;
    private byte[] publicSuffixListBytes;
    @NotNull
    private final CountDownLatch readCompleteLatch = new CountDownLatch(1);

    @Nullable
    public final String getEffectiveTldPlusOne(@NotNull String str) {
        int i;
        int i2;
        Intrinsics.checkNotNullParameter(str, "domain");
        String unicode = IDN.toUnicode(str);
        Intrinsics.checkNotNullExpressionValue(unicode, "unicodeDomain");
        List<String> splitDomain = splitDomain(unicode);
        List<String> findMatchingRule = findMatchingRule(splitDomain);
        if (splitDomain.size() == findMatchingRule.size() && findMatchingRule.get(0).charAt(0) != '!') {
            return null;
        }
        if (findMatchingRule.get(0).charAt(0) == '!') {
            i2 = splitDomain.size();
            i = findMatchingRule.size();
        } else {
            i2 = splitDomain.size();
            i = findMatchingRule.size() + 1;
        }
        return SequencesKt___SequencesKt.joinToString$default(SequencesKt___SequencesKt.drop(CollectionsKt___CollectionsKt.asSequence(splitDomain(str)), i2 - i), ".", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) null, 62, (Object) null);
    }

    private final List<String> splitDomain(String str) {
        List<String> split$default = StringsKt__StringsKt.split$default(str, new char[]{'.'}, false, 0, 6, (Object) null);
        return Intrinsics.areEqual(CollectionsKt___CollectionsKt.last(split$default), "") ? CollectionsKt___CollectionsKt.dropLast(split$default, 1) : split$default;
    }

    private final List<String> findMatchingRule(List<String> list) {
        List<String> list2;
        String str;
        String str2;
        String str3;
        List<String> list3;
        if (this.listRead.get() || !this.listRead.compareAndSet(false, true)) {
            try {
                this.readCompleteLatch.await();
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
            }
        } else {
            readTheListUninterruptibly();
        }
        if (this.publicSuffixListBytes != null) {
            int size = list.size();
            byte[][] bArr = new byte[size][];
            for (int i = 0; i < size; i++) {
                Charset charset = StandardCharsets.UTF_8;
                Intrinsics.checkNotNullExpressionValue(charset, "UTF_8");
                byte[] bytes = list.get(i).getBytes(charset);
                Intrinsics.checkNotNullExpressionValue(bytes, "this as java.lang.String).getBytes(charset)");
                bArr[i] = bytes;
            }
            int i2 = 0;
            while (true) {
                list2 = null;
                if (i2 >= size) {
                    str = null;
                    break;
                }
                int i3 = i2 + 1;
                Companion companion = Companion;
                byte[] bArr2 = this.publicSuffixListBytes;
                if (bArr2 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("publicSuffixListBytes");
                    bArr2 = null;
                }
                String access$binarySearch = companion.binarySearch(bArr2, bArr, i2);
                if (access$binarySearch != null) {
                    str = access$binarySearch;
                    break;
                }
                i2 = i3;
            }
            if (size > 1) {
                byte[][] bArr3 = (byte[][]) bArr.clone();
                int length = bArr3.length - 1;
                int i4 = 0;
                while (true) {
                    if (i4 >= length) {
                        break;
                    }
                    int i5 = i4 + 1;
                    bArr3[i4] = WILDCARD_LABEL;
                    Companion companion2 = Companion;
                    byte[] bArr4 = this.publicSuffixListBytes;
                    if (bArr4 == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("publicSuffixListBytes");
                        bArr4 = null;
                    }
                    String access$binarySearch2 = companion2.binarySearch(bArr4, bArr3, i4);
                    if (access$binarySearch2 != null) {
                        str2 = access$binarySearch2;
                        break;
                    }
                    i4 = i5;
                }
            }
            str2 = null;
            if (str2 != null) {
                int i6 = size - 1;
                int i7 = 0;
                while (true) {
                    if (i7 >= i6) {
                        break;
                    }
                    int i8 = i7 + 1;
                    Companion companion3 = Companion;
                    byte[] bArr5 = this.publicSuffixExceptionListBytes;
                    if (bArr5 == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("publicSuffixExceptionListBytes");
                        bArr5 = null;
                    }
                    str3 = companion3.binarySearch(bArr5, bArr, i7);
                    if (str3 != null) {
                        break;
                    }
                    i7 = i8;
                }
            }
            str3 = null;
            if (str3 != null) {
                return StringsKt__StringsKt.split$default(Intrinsics.stringPlus("!", str3), new char[]{'.'}, false, 0, 6, (Object) null);
            } else if (str == null && str2 == null) {
                return PREVAILING_RULE;
            } else {
                if (str == null) {
                    list3 = null;
                } else {
                    list3 = StringsKt__StringsKt.split$default(str, new char[]{'.'}, false, 0, 6, (Object) null);
                }
                if (list3 == null) {
                    list3 = CollectionsKt__CollectionsKt.emptyList();
                }
                if (str2 != null) {
                    list2 = StringsKt__StringsKt.split$default(str2, new char[]{'.'}, false, 0, 6, (Object) null);
                }
                if (list2 == null) {
                    list2 = CollectionsKt__CollectionsKt.emptyList();
                }
                return list3.size() > list2.size() ? list3 : list2;
            }
        } else {
            throw new IllegalStateException("Unable to load publicsuffixes.gz resource from the classpath.".toString());
        }
    }

    /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x0027 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void readTheListUninterruptibly() {
        /*
            r4 = this;
            r0 = 0
        L_0x0001:
            r4.readTheList()     // Catch:{ InterruptedIOException -> 0x0027, IOException -> 0x0010 }
            if (r0 == 0) goto L_0x000d
            java.lang.Thread r4 = java.lang.Thread.currentThread()
            r4.interrupt()
        L_0x000d:
            return
        L_0x000e:
            r4 = move-exception
            goto L_0x002c
        L_0x0010:
            r4 = move-exception
            okhttp3.internal.platform.Platform$Companion r1 = okhttp3.internal.platform.Platform.Companion     // Catch:{ all -> 0x000e }
            okhttp3.internal.platform.Platform r1 = r1.get()     // Catch:{ all -> 0x000e }
            java.lang.String r2 = "Failed to read public suffix list"
            r3 = 5
            r1.log(r2, r3, r4)     // Catch:{ all -> 0x000e }
            if (r0 == 0) goto L_0x0026
            java.lang.Thread r4 = java.lang.Thread.currentThread()
            r4.interrupt()
        L_0x0026:
            return
        L_0x0027:
            java.lang.Thread.interrupted()     // Catch:{ all -> 0x000e }
            r0 = 1
            goto L_0x0001
        L_0x002c:
            if (r0 == 0) goto L_0x0035
            java.lang.Thread r0 = java.lang.Thread.currentThread()
            r0.interrupt()
        L_0x0035:
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.publicsuffix.PublicSuffixDatabase.readTheListUninterruptibly():void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0048, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0049, code lost:
        kotlin.io.CloseableKt.closeFinally(r0, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004c, code lost:
        throw r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void readTheList() throws java.io.IOException {
        /*
            r4 = this;
            java.lang.Class<okhttp3.internal.publicsuffix.PublicSuffixDatabase> r0 = okhttp3.internal.publicsuffix.PublicSuffixDatabase.class
            java.lang.String r1 = "publicsuffixes.gz"
            java.io.InputStream r0 = r0.getResourceAsStream(r1)
            if (r0 != 0) goto L_0x000c
            return
        L_0x000c:
            okio.GzipSource r1 = new okio.GzipSource
            okio.Source r0 = okio.Okio.source((java.io.InputStream) r0)
            r1.<init>(r0)
            okio.BufferedSource r0 = okio.Okio.buffer((okio.Source) r1)
            int r1 = r0.readInt()     // Catch:{ all -> 0x0046 }
            long r1 = (long) r1     // Catch:{ all -> 0x0046 }
            byte[] r1 = r0.readByteArray(r1)     // Catch:{ all -> 0x0046 }
            int r2 = r0.readInt()     // Catch:{ all -> 0x0046 }
            long r2 = (long) r2     // Catch:{ all -> 0x0046 }
            byte[] r2 = r0.readByteArray(r2)     // Catch:{ all -> 0x0046 }
            kotlin.Unit r3 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x0046 }
            r3 = 0
            kotlin.io.CloseableKt.closeFinally(r0, r3)
            monitor-enter(r4)
            kotlin.jvm.internal.Intrinsics.checkNotNull(r1)     // Catch:{ all -> 0x0043 }
            r4.publicSuffixListBytes = r1     // Catch:{ all -> 0x0043 }
            kotlin.jvm.internal.Intrinsics.checkNotNull(r2)     // Catch:{ all -> 0x0043 }
            r4.publicSuffixExceptionListBytes = r2     // Catch:{ all -> 0x0043 }
            monitor-exit(r4)
            java.util.concurrent.CountDownLatch r4 = r4.readCompleteLatch
            r4.countDown()
            return
        L_0x0043:
            r0 = move-exception
            monitor-exit(r4)
            throw r0
        L_0x0046:
            r4 = move-exception
            throw r4     // Catch:{ all -> 0x0048 }
        L_0x0048:
            r1 = move-exception
            kotlin.io.CloseableKt.closeFinally(r0, r4)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.publicsuffix.PublicSuffixDatabase.readTheList():void");
    }

    /* compiled from: PublicSuffixDatabase.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @NotNull
        public final PublicSuffixDatabase get() {
            return PublicSuffixDatabase.instance;
        }

        /* access modifiers changed from: private */
        public final String binarySearch(byte[] bArr, byte[][] bArr2, int i) {
            int i2;
            boolean z;
            int i3;
            int and;
            byte[] bArr3 = bArr;
            byte[][] bArr4 = bArr2;
            int length = bArr3.length;
            int i4 = 0;
            while (i4 < length) {
                int i5 = (i4 + length) / 2;
                while (i5 > -1 && bArr3[i5] != 10) {
                    i5--;
                }
                int i6 = i5 + 1;
                int i7 = 1;
                while (true) {
                    i2 = i6 + i7;
                    if (bArr3[i2] == 10) {
                        break;
                    }
                    i7++;
                }
                int i8 = i2 - i6;
                int i9 = i;
                boolean z2 = false;
                int i10 = 0;
                int i11 = 0;
                while (true) {
                    if (z2) {
                        i3 = 46;
                        z = false;
                    } else {
                        z = z2;
                        i3 = Util.and(bArr4[i9][i10], 255);
                    }
                    and = i3 - Util.and(bArr3[i6 + i11], 255);
                    if (and == 0) {
                        i11++;
                        i10++;
                        if (i11 == i8) {
                            break;
                        } else if (bArr4[i9].length != i10) {
                            z2 = z;
                        } else if (i9 == bArr4.length - 1) {
                            break;
                        } else {
                            i9++;
                            i10 = -1;
                            z2 = true;
                        }
                    } else {
                        break;
                    }
                }
                if (and >= 0) {
                    if (and <= 0) {
                        int i12 = i8 - i11;
                        int length2 = bArr4[i9].length - i10;
                        int length3 = bArr4.length;
                        for (int i13 = i9 + 1; i13 < length3; i13++) {
                            length2 += bArr4[i13].length;
                        }
                        if (length2 >= i12) {
                            if (length2 <= i12) {
                                Charset charset = StandardCharsets.UTF_8;
                                Intrinsics.checkNotNullExpressionValue(charset, "UTF_8");
                                return new String(bArr3, i6, i8, charset);
                            }
                        }
                    }
                    i4 = i2 + 1;
                }
                length = i6 - 1;
            }
            return null;
        }
    }
}
