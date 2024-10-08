package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImsBuffer extends Table {
    public static ImsBuffer getRootAsImsBuffer(ByteBuffer byteBuffer) {
        return getRootAsImsBuffer(byteBuffer, new ImsBuffer());
    }

    public static ImsBuffer getRootAsImsBuffer(ByteBuffer byteBuffer, ImsBuffer imsBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return imsBuffer.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public ImsBuffer __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long trid() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public byte msgType() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.get(__offset + this.bb_pos);
        }
        return 0;
    }

    public Table msg(Table table) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __union(table, __offset);
        }
        return null;
    }

    public static int createImsBuffer(FlatBufferBuilder flatBufferBuilder, long j, byte b, int i) {
        flatBufferBuilder.startObject(3);
        addMsg(flatBufferBuilder, i);
        addTrid(flatBufferBuilder, j);
        addMsgType(flatBufferBuilder, b);
        return endImsBuffer(flatBufferBuilder);
    }

    public static void startImsBuffer(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addTrid(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addMsgType(FlatBufferBuilder flatBufferBuilder, byte b) {
        flatBufferBuilder.addByte(1, b, 0);
    }

    public static void addMsg(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endImsBuffer(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }

    public static void finishImsBufferBuffer(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.finish(i);
    }
}
