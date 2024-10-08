package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateCommonConfig extends Table {
    public static RequestUpdateCommonConfig getRootAsRequestUpdateCommonConfig(ByteBuffer byteBuffer) {
        return getRootAsRequestUpdateCommonConfig(byteBuffer, new RequestUpdateCommonConfig());
    }

    public static RequestUpdateCommonConfig getRootAsRequestUpdateCommonConfig(ByteBuffer byteBuffer, RequestUpdateCommonConfig requestUpdateCommonConfig) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUpdateCommonConfig.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUpdateCommonConfig __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long phoneId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public byte configType() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.get(__offset + this.bb_pos);
        }
        return 0;
    }

    public Table config(Table table) {
        int __offset = __offset(8);
        if (__offset != 0) {
            return __union(table, __offset);
        }
        return null;
    }

    public static int createRequestUpdateCommonConfig(FlatBufferBuilder flatBufferBuilder, long j, byte b, int i) {
        flatBufferBuilder.startObject(3);
        addConfig(flatBufferBuilder, i);
        addPhoneId(flatBufferBuilder, j);
        addConfigType(flatBufferBuilder, b);
        return endRequestUpdateCommonConfig(flatBufferBuilder);
    }

    public static void startRequestUpdateCommonConfig(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addPhoneId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addConfigType(FlatBufferBuilder flatBufferBuilder, byte b) {
        flatBufferBuilder.addByte(1, b, 0);
    }

    public static void addConfig(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(2, i, 0);
    }

    public static int endRequestUpdateCommonConfig(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
