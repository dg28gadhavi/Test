package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class WarningHdr extends Table {
    public static WarningHdr getRootAsWarningHdr(ByteBuffer byteBuffer) {
        return getRootAsWarningHdr(byteBuffer, new WarningHdr());
    }

    public static WarningHdr getRootAsWarningHdr(ByteBuffer byteBuffer, WarningHdr warningHdr) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return warningHdr.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public WarningHdr __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public int code() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return -1;
    }

    public String host() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer hostAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String text() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer textAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createWarningHdr(FlatBufferBuilder flatBufferBuilder, int i, int i2, int i3) {
        flatBufferBuilder.startObject(3);
        addText(flatBufferBuilder, i3);
        addHost(flatBufferBuilder, i2);
        addCode(flatBufferBuilder, i);
        return endWarningHdr(flatBufferBuilder);
    }

    public static void startWarningHdr(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addCode(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(0, i, -1);
    }

    public static void addHost(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addText(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endWarningHdr(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
