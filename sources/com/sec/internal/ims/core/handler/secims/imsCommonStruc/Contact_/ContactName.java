package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Contact_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ContactName extends Table {
    public static ContactName getRootAsContactName(ByteBuffer byteBuffer) {
        return getRootAsContactName(byteBuffer, new ContactName());
    }

    public static ContactName getRootAsContactName(ByteBuffer byteBuffer, ContactName contactName) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return contactName.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ContactName __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String title() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer titleAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String givenName() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer givenNameAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String middleName() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer middleNameAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String familyName() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer familyNameAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String generationId() {
        int __offset = __offset(12);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer generationIdAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String displayName() {
        int __offset = __offset(14);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer displayNameAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public static int createContactName(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4, int i5, int i6) {
        flatBufferBuilder.startObject(6);
        addDisplayName(flatBufferBuilder, i6);
        addGenerationId(flatBufferBuilder, i5);
        addFamilyName(flatBufferBuilder, i4);
        addMiddleName(flatBufferBuilder, i3);
        addGivenName(flatBufferBuilder, i2);
        addTitle(flatBufferBuilder, i);
        return endContactName(flatBufferBuilder);
    }

    public static void startContactName(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(6);
    }

    public static void addTitle(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addGivenName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addMiddleName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addFamilyName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static void addGenerationId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(4, i, 0);
    }

    public static void addDisplayName(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(5, i, 0);
    }

    public static int endContactName(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
