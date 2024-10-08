package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUADeletion extends Table {
    public static RequestUADeletion getRootAsRequestUADeletion(ByteBuffer byteBuffer) {
        return getRootAsRequestUADeletion(byteBuffer, new RequestUADeletion());
    }

    public static RequestUADeletion getRootAsRequestUADeletion(ByteBuffer byteBuffer, RequestUADeletion requestUADeletion) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUADeletion.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUADeletion __assign(int i, ByteBuffer byteBuffer) {
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

    public static int createRequestUADeletion(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.startObject(1);
        addHandle(flatBufferBuilder, j);
        return endRequestUADeletion(flatBufferBuilder);
    }

    public static void startRequestUADeletion(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static int endRequestUADeletion(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
