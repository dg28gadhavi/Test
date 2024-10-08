package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.XqMessage_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class XqContent extends Table {
    public static XqContent getRootAsXqContent(ByteBuffer byteBuffer) {
        return getRootAsXqContent(byteBuffer, new XqContent());
    }

    public static XqContent getRootAsXqContent(ByteBuffer byteBuffer, XqContent xqContent) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return xqContent.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public XqContent __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public int type() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public long intVal() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String strVal() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer strValAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createXqContent(FlatBufferBuilder flatBufferBuilder, int i, long j, int i2) {
        flatBufferBuilder.startObject(3);
        addStrVal(flatBufferBuilder, i2);
        addIntVal(flatBufferBuilder, j);
        addType(flatBufferBuilder, i);
        return endXqContent(flatBufferBuilder);
    }

    public static void startXqContent(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addType(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(0, i, 0);
    }

    public static void addIntVal(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addStrVal(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endXqContent(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
