package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Icon extends Table {
    public static Icon getRootAsIcon(ByteBuffer byteBuffer) {
        return getRootAsIcon(byteBuffer, new Icon());
    }

    public static Icon getRootAsIcon(ByteBuffer byteBuffer, Icon icon) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return icon.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public Icon __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public int icontype() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String participant() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer participantAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String timestamp() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer timestampAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String iconLocation() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer iconLocationAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public static int createIcon(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3, int i4) {
        flatBufferBuilder.startObject(4);
        addIconLocation(flatBufferBuilder, i4);
        addTimestamp(flatBufferBuilder, i3);
        addParticipant(flatBufferBuilder, i2);
        addIcontype(flatBufferBuilder, i);
        return endIcon(flatBufferBuilder);
    }

    public static void startIcon(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addIcontype(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(0, i, 0);
    }

    public static void addParticipant(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addTimestamp(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static void addIconLocation(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(3, i, 0);
    }

    public static int endIcon(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
