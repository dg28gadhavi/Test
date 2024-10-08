package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class TextDataEvent extends Table {
    public static TextDataEvent getRootAsTextDataEvent(ByteBuffer byteBuffer) {
        return getRootAsTextDataEvent(byteBuffer, new TextDataEvent());
    }

    public static TextDataEvent getRootAsTextDataEvent(ByteBuffer byteBuffer, TextDataEvent textDataEvent) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return textDataEvent.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public TextDataEvent __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public String text() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer textAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public long len() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createTextDataEvent(FlatBufferBuilder flatBufferBuilder, int i, long j) {
        flatBufferBuilder.startObject(2);
        addLen(flatBufferBuilder, j);
        addText(flatBufferBuilder, i);
        return endTextDataEvent(flatBufferBuilder);
    }

    public static void startTextDataEvent(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addText(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(0, i, 0);
    }

    public static void addLen(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static int endTextDataEvent(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
