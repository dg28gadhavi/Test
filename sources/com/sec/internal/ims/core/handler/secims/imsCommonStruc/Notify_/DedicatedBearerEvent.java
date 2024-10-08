package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class DedicatedBearerEvent extends Table {
    public static DedicatedBearerEvent getRootAsDedicatedBearerEvent(ByteBuffer byteBuffer) {
        return getRootAsDedicatedBearerEvent(byteBuffer, new DedicatedBearerEvent());
    }

    public static DedicatedBearerEvent getRootAsDedicatedBearerEvent(ByteBuffer byteBuffer, DedicatedBearerEvent dedicatedBearerEvent) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return dedicatedBearerEvent.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public DedicatedBearerEvent __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long handle() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long session() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long qci() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public int bearerState() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createDedicatedBearerEvent(FlatBufferBuilder flatBufferBuilder, long j, long j2, long j3, int i) {
        flatBufferBuilder.startObject(4);
        addBearerState(flatBufferBuilder, i);
        addQci(flatBufferBuilder, j3);
        addSession(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        return endDedicatedBearerEvent(flatBufferBuilder);
    }

    public static void startDedicatedBearerEvent(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addQci(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static void addBearerState(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(3, i, 0);
    }

    public static int endDedicatedBearerEvent(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
