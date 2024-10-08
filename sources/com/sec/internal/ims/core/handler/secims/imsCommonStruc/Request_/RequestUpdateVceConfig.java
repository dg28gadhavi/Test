package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateVceConfig extends Table {
    public static RequestUpdateVceConfig getRootAsRequestUpdateVceConfig(ByteBuffer byteBuffer) {
        return getRootAsRequestUpdateVceConfig(byteBuffer, new RequestUpdateVceConfig());
    }

    public static RequestUpdateVceConfig getRootAsRequestUpdateVceConfig(ByteBuffer byteBuffer, RequestUpdateVceConfig requestUpdateVceConfig) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUpdateVceConfig.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUpdateVceConfig __assign(int i, ByteBuffer byteBuffer) {
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

    public boolean vceConfig() {
        int __offset = __offset(6);
        return (__offset == 0 || this.bb.get(__offset + this.bb_pos) == 0) ? false : true;
    }

    public static int createRequestUpdateVceConfig(FlatBufferBuilder flatBufferBuilder, long j, boolean z) {
        flatBufferBuilder.startObject(2);
        addHandle(flatBufferBuilder, j);
        addVceConfig(flatBufferBuilder, z);
        return endRequestUpdateVceConfig(flatBufferBuilder);
    }

    public static void startRequestUpdateVceConfig(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addVceConfig(FlatBufferBuilder flatBufferBuilder, boolean z) {
        flatBufferBuilder.addBoolean(1, z, false);
    }

    public static int endRequestUpdateVceConfig(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
