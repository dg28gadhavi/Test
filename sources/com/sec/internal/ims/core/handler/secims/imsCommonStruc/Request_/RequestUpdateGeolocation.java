package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateGeolocation extends Table {
    public static RequestUpdateGeolocation getRootAsRequestUpdateGeolocation(ByteBuffer byteBuffer) {
        return getRootAsRequestUpdateGeolocation(byteBuffer, new RequestUpdateGeolocation());
    }

    public static RequestUpdateGeolocation getRootAsRequestUpdateGeolocation(ByteBuffer byteBuffer, RequestUpdateGeolocation requestUpdateGeolocation) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUpdateGeolocation.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUpdateGeolocation __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long handle() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String latitude() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer latitudeAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String longitude() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer longitudeAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String altitude() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer altitudeAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String accuracy() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer accuracyAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String verticalaxis() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer verticalaxisAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String orientation() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer orientationAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String providertype() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer providertypeAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public String retentionexpires() {
        int __offset = __offset(20);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer retentionexpiresAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String srsname() {
        int __offset = __offset(22);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer srsnameAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String radiusuom() {
        int __offset = __offset(24);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer radiusuomAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public String os() {
        int __offset = __offset(26);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer osAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public String deviceid() {
        int __offset = __offset(28);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer deviceidAsByteBuffer() {
        return __vector_as_bytebuffer(28, 1);
    }

    public String country() {
        int __offset = __offset(30);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer countryAsByteBuffer() {
        return __vector_as_bytebuffer(30, 1);
    }

    public String a1() {
        int __offset = __offset(32);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer a1AsByteBuffer() {
        return __vector_as_bytebuffer(32, 1);
    }

    public String a3() {
        int __offset = __offset(34);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer a3AsByteBuffer() {
        return __vector_as_bytebuffer(34, 1);
    }

    public String a6() {
        int __offset = __offset(36);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer a6AsByteBuffer() {
        return __vector_as_bytebuffer(36, 1);
    }

    public String hno() {
        int __offset = __offset(38);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer hnoAsByteBuffer() {
        return __vector_as_bytebuffer(38, 1);
    }

    public String pc() {
        int __offset = __offset(40);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer pcAsByteBuffer() {
        return __vector_as_bytebuffer(40, 1);
    }

    public String locationtime() {
        int __offset = __offset(42);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer locationtimeAsByteBuffer() {
        return __vector_as_bytebuffer(42, 1);
    }

    public static int createRequestUpdateGeolocation(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14, int i15, int i16, int i17, int i18, int i19) {
        FlatBufferBuilder flatBufferBuilder2 = flatBufferBuilder;
        flatBufferBuilder.startObject(20);
        addLocationtime(flatBufferBuilder, i19);
        addPc(flatBufferBuilder, i18);
        addHno(flatBufferBuilder, i17);
        addA6(flatBufferBuilder, i16);
        addA3(flatBufferBuilder, i15);
        addA1(flatBufferBuilder, i14);
        addCountry(flatBufferBuilder, i13);
        addDeviceid(flatBufferBuilder, i12);
        int i20 = i11;
        addOs(flatBufferBuilder, i11);
        int i21 = i10;
        addRadiusuom(flatBufferBuilder, i10);
        int i22 = i9;
        addSrsname(flatBufferBuilder, i9);
        int i23 = i8;
        addRetentionexpires(flatBufferBuilder, i8);
        int i24 = i7;
        addProvidertype(flatBufferBuilder, i7);
        int i25 = i6;
        addOrientation(flatBufferBuilder, i6);
        int i26 = i5;
        addVerticalaxis(flatBufferBuilder, i5);
        int i27 = i4;
        addAccuracy(flatBufferBuilder, i4);
        int i28 = i3;
        addAltitude(flatBufferBuilder, i3);
        int i29 = i2;
        addLongitude(flatBufferBuilder, i2);
        int i30 = i;
        addLatitude(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestUpdateGeolocation(flatBufferBuilder);
    }

    public static void startRequestUpdateGeolocation(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(20);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addLatitude(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addLongitude(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addAltitude(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addAccuracy(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addVerticalaxis(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addOrientation(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addProvidertype(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static void addRetentionexpires(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(8, i, 0);
    }

    public static void addSrsname(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(9, i, 0);
    }

    public static void addRadiusuom(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(10, i, 0);
    }

    public static void addOs(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(11, i, 0);
    }

    public static void addDeviceid(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(12, i, 0);
    }

    public static void addCountry(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(13, i, 0);
    }

    public static void addA1(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(14, i, 0);
    }

    public static void addA3(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(15, i, 0);
    }

    public static void addA6(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(16, i, 0);
    }

    public static void addHno(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(17, i, 0);
    }

    public static void addPc(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(18, i, 0);
    }

    public static void addLocationtime(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(19, i, 0);
    }

    public static int endRequestUpdateGeolocation(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
