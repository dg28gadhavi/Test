package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RrcConnectionEvent extends Table {
    public static RrcConnectionEvent getRootAsRrcConnectionEvent(ByteBuffer byteBuffer) {
        return getRootAsRrcConnectionEvent(byteBuffer, new RrcConnectionEvent());
    }

    public static RrcConnectionEvent getRootAsRrcConnectionEvent(ByteBuffer byteBuffer, RrcConnectionEvent rrcConnectionEvent) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return rrcConnectionEvent.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RrcConnectionEvent __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public int event() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createRrcConnectionEvent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.startObject(1);
        addEvent(flatBufferBuilder, i);
        return endRrcConnectionEvent(flatBufferBuilder);
    }

    public static void startRrcConnectionEvent(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addEvent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(0, i, 0);
    }

    public static int endRrcConnectionEvent(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
