package okio;

import com.sec.internal.helper.header.WwwAuthenticateHeader;
import java.security.MessageDigest;
import kotlin.jvm.internal.Intrinsics;
import okio.internal._SegmentedByteStringKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: SegmentedByteString.kt */
public final class SegmentedByteString extends ByteString {
    @NotNull
    private final transient int[] directory;
    @NotNull
    private final transient byte[][] segments;

    @NotNull
    public final byte[][] getSegments$okio() {
        return this.segments;
    }

    @NotNull
    public final int[] getDirectory$okio() {
        return this.directory;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SegmentedByteString(@NotNull byte[][] bArr, @NotNull int[] iArr) {
        super(ByteString.EMPTY.getData$okio());
        Intrinsics.checkNotNullParameter(bArr, "segments");
        Intrinsics.checkNotNullParameter(iArr, "directory");
        this.segments = bArr;
        this.directory = iArr;
    }

    @NotNull
    public String base64() {
        return toByteString().base64();
    }

    @NotNull
    public String hex() {
        return toByteString().hex();
    }

    @NotNull
    public ByteString toAsciiLowercase() {
        return toByteString().toAsciiLowercase();
    }

    @NotNull
    public ByteString digest$okio(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, WwwAuthenticateHeader.HEADER_PARAM_ALGORITHM);
        MessageDigest instance = MessageDigest.getInstance(str);
        int length = getSegments$okio().length;
        int i = 0;
        int i2 = 0;
        while (i < length) {
            int i3 = getDirectory$okio()[length + i];
            int i4 = getDirectory$okio()[i];
            instance.update(getSegments$okio()[i], i3, i4 - i2);
            i++;
            i2 = i4;
        }
        byte[] digest = instance.digest();
        Intrinsics.checkNotNullExpressionValue(digest, "digestBytes");
        return new ByteString(digest);
    }

    public void write$okio(@NotNull Buffer buffer, int i, int i2) {
        int i3;
        Intrinsics.checkNotNullParameter(buffer, "buffer");
        int i4 = i + i2;
        int segment = _SegmentedByteStringKt.segment(this, i);
        while (i < i4) {
            if (segment == 0) {
                i3 = 0;
            } else {
                i3 = getDirectory$okio()[segment - 1];
            }
            int i5 = getDirectory$okio()[getSegments$okio().length + segment];
            int min = Math.min(i4, (getDirectory$okio()[segment] - i3) + i3) - i;
            int i6 = i5 + (i - i3);
            Segment segment2 = new Segment(getSegments$okio()[segment], i6, i6 + min, true, false);
            Segment segment3 = buffer.head;
            if (segment3 == null) {
                segment2.prev = segment2;
                segment2.next = segment2;
                buffer.head = segment2;
            } else {
                Intrinsics.checkNotNull(segment3);
                Segment segment4 = segment3.prev;
                Intrinsics.checkNotNull(segment4);
                segment4.push(segment2);
            }
            i += min;
            segment++;
        }
        buffer.setSize$okio(buffer.size() + ((long) i2));
    }

    private final ByteString toByteString() {
        return new ByteString(toByteArray());
    }

    @NotNull
    public byte[] internalArray$okio() {
        return toByteArray();
    }

    public byte internalGet$okio(int i) {
        int i2;
        _UtilKt.checkOffsetAndCount((long) getDirectory$okio()[getSegments$okio().length - 1], (long) i, 1);
        int segment = _SegmentedByteStringKt.segment(this, i);
        if (segment == 0) {
            i2 = 0;
        } else {
            i2 = getDirectory$okio()[segment - 1];
        }
        return getSegments$okio()[segment][(i - i2) + getDirectory$okio()[getSegments$okio().length + segment]];
    }

    @NotNull
    public String toString() {
        return toByteString().toString();
    }

    private final Object writeReplace() {
        return toByteString();
    }

    public int getSize$okio() {
        return getDirectory$okio()[getSegments$okio().length - 1];
    }

    @NotNull
    public byte[] toByteArray() {
        byte[] bArr = new byte[size()];
        int length = getSegments$okio().length;
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        while (i < length) {
            int i4 = getDirectory$okio()[length + i];
            int i5 = getDirectory$okio()[i];
            int i6 = i5 - i2;
            byte[] unused = ArraysKt___ArraysJvmKt.copyInto(getSegments$okio()[i], bArr, i3, i4, i4 + i6);
            i3 += i6;
            i++;
            i2 = i5;
        }
        return bArr;
    }

    public boolean rangeEquals(int i, @NotNull ByteString byteString, int i2, int i3) {
        int i4;
        Intrinsics.checkNotNullParameter(byteString, "other");
        if (i < 0 || i > size() - i3) {
            return false;
        }
        int i5 = i3 + i;
        int segment = _SegmentedByteStringKt.segment(this, i);
        while (i < i5) {
            if (segment == 0) {
                i4 = 0;
            } else {
                i4 = getDirectory$okio()[segment - 1];
            }
            int i6 = getDirectory$okio()[getSegments$okio().length + segment];
            int min = Math.min(i5, (getDirectory$okio()[segment] - i4) + i4) - i;
            if (!byteString.rangeEquals(i2, getSegments$okio()[segment], i6 + (i - i4), min)) {
                return false;
            }
            i2 += min;
            i += min;
            segment++;
        }
        return true;
    }

    public boolean rangeEquals(int i, @NotNull byte[] bArr, int i2, int i3) {
        int i4;
        Intrinsics.checkNotNullParameter(bArr, "other");
        if (i < 0 || i > size() - i3 || i2 < 0 || i2 > bArr.length - i3) {
            return false;
        }
        int i5 = i3 + i;
        int segment = _SegmentedByteStringKt.segment(this, i);
        while (i < i5) {
            if (segment == 0) {
                i4 = 0;
            } else {
                i4 = getDirectory$okio()[segment - 1];
            }
            int i6 = getDirectory$okio()[getSegments$okio().length + segment];
            int min = Math.min(i5, (getDirectory$okio()[segment] - i4) + i4) - i;
            if (!_UtilKt.arrayRangeEquals(getSegments$okio()[segment], i6 + (i - i4), bArr, i2, min)) {
                return false;
            }
            i2 += min;
            i += min;
            segment++;
        }
        return true;
    }

    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ByteString) {
            ByteString byteString = (ByteString) obj;
            if (byteString.size() == size() && rangeEquals(0, byteString, 0, size())) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        int hashCode$okio = getHashCode$okio();
        if (hashCode$okio != 0) {
            return hashCode$okio;
        }
        int length = getSegments$okio().length;
        int i = 0;
        int i2 = 1;
        int i3 = 0;
        while (i < length) {
            int i4 = getDirectory$okio()[length + i];
            int i5 = getDirectory$okio()[i];
            byte[] bArr = getSegments$okio()[i];
            int i6 = (i5 - i3) + i4;
            while (i4 < i6) {
                i2 = (i2 * 31) + bArr[i4];
                i4++;
            }
            i++;
            i3 = i5;
        }
        setHashCode$okio(i2);
        return i2;
    }
}
