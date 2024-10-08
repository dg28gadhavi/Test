package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestStopCamera extends Table {
    public static RequestStopCamera getRootAsRequestStopCamera(ByteBuffer byteBuffer) {
        return getRootAsRequestStopCamera(byteBuffer, new RequestStopCamera());
    }

    public static RequestStopCamera getRootAsRequestStopCamera(ByteBuffer byteBuffer, RequestStopCamera requestStopCamera) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestStopCamera.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestStopCamera __assign(int i, ByteBuffer byteBuffer) {
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

    public static int createRequestStopCamera(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.startObject(1);
        addHandle(flatBufferBuilder, j);
        return endRequestStopCamera(flatBufferBuilder);
    }

    public static void startRequestStopCamera(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static int endRequestStopCamera(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
