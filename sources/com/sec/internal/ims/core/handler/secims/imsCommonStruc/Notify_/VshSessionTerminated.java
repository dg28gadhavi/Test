package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class VshSessionTerminated extends Table {
    public static VshSessionTerminated getRootAsVshSessionTerminated(ByteBuffer byteBuffer) {
        return getRootAsVshSessionTerminated(byteBuffer, new VshSessionTerminated());
    }

    public static VshSessionTerminated getRootAsVshSessionTerminated(ByteBuffer byteBuffer, VshSessionTerminated vshSessionTerminated) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return vshSessionTerminated.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public VshSessionTerminated __assign(int i, ByteBuffer byteBuffer) {
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

    public static int createVshSessionTerminated(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addReason(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        return endVshSessionTerminated(flatBufferBuilder);
    }

    public static void startVshSessionTerminated(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static int endVshSessionTerminated(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
