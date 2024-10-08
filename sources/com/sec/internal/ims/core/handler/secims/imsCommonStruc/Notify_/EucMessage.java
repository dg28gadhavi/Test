package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class EucMessage extends Table {
    public static EucMessage getRootAsEucMessage(ByteBuffer byteBuffer) {
        return getRootAsEucMessage(byteBuffer, new EucMessage());
    }

    public static EucMessage getRootAsEucMessage(ByteBuffer byteBuffer, EucMessage eucMessage) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return eucMessage.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public EucMessage __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public byte messageType() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return this.bb.get(__offset + this.bb_pos);
        }
        return 0;
    }

    public Table message(Table table) {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __union(table, __offset);
        }
        return null;
    }

    public static int createEucMessage(FlatBufferBuilder flatBufferBuilder, byte b, int i) {
        flatBufferBuilder.startObject(2);
        addMessage(flatBufferBuilder, i);
        addMessageType(flatBufferBuilder, b);
        return endEucMessage(flatBufferBuilder);
    }

    public static void startEucMessage(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addMessageType(FlatBufferBuilder flatBufferBuilder, byte b) {
        flatBufferBuilder.addByte(0, b, 0);
    }

    public static void addMessage(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endEucMessage(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
