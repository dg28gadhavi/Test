package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class QuantumSecurityStatusEvent extends Table {
    public static QuantumSecurityStatusEvent getRootAsQuantumSecurityStatusEvent(ByteBuffer byteBuffer) {
        return getRootAsQuantumSecurityStatusEvent(byteBuffer, new QuantumSecurityStatusEvent());
    }

    public static QuantumSecurityStatusEvent getRootAsQuantumSecurityStatusEvent(ByteBuffer byteBuffer, QuantumSecurityStatusEvent quantumSecurityStatusEvent) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return quantumSecurityStatusEvent.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public QuantumSecurityStatusEvent __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long session() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public int event() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public String qtSessionId() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer qtSessionIdAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createQuantumSecurityStatusEvent(FlatBufferBuilder flatBufferBuilder, long j, int i, int i2) {
        flatBufferBuilder.startObject(3);
        addQtSessionId(flatBufferBuilder, i2);
        addEvent(flatBufferBuilder, i);
        addSession(flatBufferBuilder, j);
        return endQuantumSecurityStatusEvent(flatBufferBuilder);
    }

    public static void startQuantumSecurityStatusEvent(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addEvent(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static void addQtSessionId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endQuantumSecurityStatusEvent(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
