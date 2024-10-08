package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ContactOrg extends Table {
    public static ContactOrg getRootAsContactOrg(ByteBuffer byteBuffer) {
        return getRootAsContactOrg(byteBuffer, new ContactOrg());
    }

    public static ContactOrg getRootAsContactOrg(ByteBuffer byteBuffer, ContactOrg contactOrg) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return contactOrg.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ContactOrg __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String displayName() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer displayNameAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String entity() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer entityAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String unit() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer unitAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createContactOrg(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3) {
        flatBufferBuilder.startObject(3);
        addUnit(flatBufferBuilder, i3);
        addEntity(flatBufferBuilder, i2);
        addDisplayName(flatBufferBuilder, i);
        return endContactOrg(flatBufferBuilder);
    }

    public static void startContactOrg(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addDisplayName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addEntity(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addUnit(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endContactOrg(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
