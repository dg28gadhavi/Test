package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestStartMedia extends Table {
    public static RequestStartMedia getRootAsRequestStartMedia(ByteBuffer byteBuffer) {
        return getRootAsRequestStartMedia(byteBuffer, new RequestStartMedia());
    }

    public static RequestStartMedia getRootAsRequestStartMedia(ByteBuffer byteBuffer, RequestStartMedia requestStartMedia) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestStartMedia.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestStartMedia __assign(int i, ByteBuffer byteBuffer) {
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

    public static int createRequestStartMedia(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.startObject(1);
        addSessionId(flatBufferBuilder, j);
        return endRequestStartMedia(flatBufferBuilder);
    }

    public static void startRequestStartMedia(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static int endRequestStartMedia(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
