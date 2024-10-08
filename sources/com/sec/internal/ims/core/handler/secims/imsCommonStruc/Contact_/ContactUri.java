package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ContactUri extends Table {
    public static ContactUri getRootAsContactUri(ByteBuffer byteBuffer) {
        return getRootAsContactUri(byteBuffer, new ContactUri());
    }

    public static ContactUri getRootAsContactUri(ByteBuffer byteBuffer, ContactUri contactUri) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return contactUri.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ContactUri __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String uri() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String type() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer typeAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String label() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer labelAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createContactUri(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3) {
        flatBufferBuilder.startObject(3);
        addLabel(flatBufferBuilder, i3);
        addType(flatBufferBuilder, i2);
        addUri(flatBufferBuilder, i);
        return endContactUri(flatBufferBuilder);
    }

    public static void startContactUri(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addLabel(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endContactUri(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
