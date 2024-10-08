package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class IshSessionTerminated extends Table {
    public static IshSessionTerminated getRootAsIshSessionTerminated(ByteBuffer byteBuffer) {
        return getRootAsIshSessionTerminated(byteBuffer, new IshSessionTerminated());
    }

    public static IshSessionTerminated getRootAsIshSessionTerminated(ByteBuffer byteBuffer, IshSessionTerminated ishSessionTerminated) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return ishSessionTerminated.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public IshSessionTerminated __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long sessionId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public int reason() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createIshSessionTerminated(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addReason(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        return endIshSessionTerminated(flatBufferBuilder);
    }

    public static void startIshSessionTerminated(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static int endIshSessionTerminated(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
