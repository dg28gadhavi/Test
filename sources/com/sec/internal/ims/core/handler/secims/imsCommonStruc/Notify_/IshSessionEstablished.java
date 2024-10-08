package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class IshSessionEstablished extends Table {
    public static IshSessionEstablished getRootAsIshSessionEstablished(ByteBuffer byteBuffer) {
        return getRootAsIshSessionEstablished(byteBuffer, new IshSessionEstablished());
    }

    public static IshSessionEstablished getRootAsIshSessionEstablished(ByteBuffer byteBuffer, IshSessionEstablished ishSessionEstablished) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return ishSessionEstablished.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public IshSessionEstablished __assign(int i, ByteBuffer byteBuffer) {
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

    public int error() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createIshSessionEstablished(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addError(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        return endIshSessionEstablished(flatBufferBuilder);
    }

    public static void startIshSessionEstablished(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addError(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static int endIshSessionEstablished(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
