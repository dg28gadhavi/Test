package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ContactAddress extends Table {
    public static ContactAddress getRootAsContactAddress(ByteBuffer byteBuffer) {
        return getRootAsContactAddress(byteBuffer, new ContactAddress());
    }

    public static ContactAddress getRootAsContactAddress(ByteBuffer byteBuffer, ContactAddress contactAddress) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return contactAddress.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ContactAddress __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String type() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer typeAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String label() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer labelAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String addrStr() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer addrStrAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String country() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer countryAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String region() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer regionAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String locality() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer localityAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String street() {
        int __offset = __offset(16);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer streetAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String postCode() {
        int __offset = __offset(18);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer postCodeAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public static int createContactAddress(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        flatBufferBuilder.startObject(8);
        addPostCode(flatBufferBuilder, i8);
        addStreet(flatBufferBuilder, i7);
        addLocality(flatBufferBuilder, i6);
        addRegion(flatBufferBuilder, i5);
        addCountry(flatBufferBuilder, i4);
        addAddrStr(flatBufferBuilder, i3);
        addLabel(flatBufferBuilder, i2);
        addType(flatBufferBuilder, i);
        return endContactAddress(flatBufferBuilder);
    }

    public static void startContactAddress(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(8);
    }

    public static void addType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addLabel(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addAddrStr(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addCountry(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addRegion(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addLocality(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static void addStreet(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(6, i, 0);
    }

    public static void addPostCode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(7, i, 0);
    }

    public static int endContactAddress(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
