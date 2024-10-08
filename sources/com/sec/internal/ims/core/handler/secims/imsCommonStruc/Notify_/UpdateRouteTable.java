package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class UpdateRouteTable extends Table {
    public static UpdateRouteTable getRootAsUpdateRouteTable(ByteBuffer byteBuffer) {
        return getRootAsUpdateRouteTable(byteBuffer, new UpdateRouteTable());
    }

    public static UpdateRouteTable getRootAsUpdateRouteTable(ByteBuffer byteBuffer, UpdateRouteTable updateRouteTable) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return updateRouteTable.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public UpdateRouteTable __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public int operation() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public long handle() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String address() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer addressAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public static int createUpdateRouteTable(FlatBufferBuilder flatBufferBuilder, int i, long j, int i2) {
        flatBufferBuilder.startObject(3);
        addAddress(flatBufferBuilder, i2);
        addHandle(flatBufferBuilder, j);
        addOperation(flatBufferBuilder, i);
        return endUpdateRouteTable(flatBufferBuilder);
    }

    public static void startUpdateRouteTable(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addOperation(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(0, i, 0);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addAddress(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endUpdateRouteTable(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
