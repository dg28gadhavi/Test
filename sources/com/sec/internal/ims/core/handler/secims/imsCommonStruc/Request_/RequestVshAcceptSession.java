package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestVshAcceptSession extends Table {
    public static RequestVshAcceptSession getRootAsRequestVshAcceptSession(ByteBuffer byteBuffer) {
        return getRootAsRequestVshAcceptSession(byteBuffer, new RequestVshAcceptSession());
    }

    public static RequestVshAcceptSession getRootAsRequestVshAcceptSession(ByteBuffer byteBuffer, RequestVshAcceptSession requestVshAcceptSession) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestVshAcceptSession.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestVshAcceptSession __assign(int i, ByteBuffer byteBuffer) {
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

    public static int createRequestVshAcceptSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.startObject(1);
        addSessionId(flatBufferBuilder, j);
        return endRequestVshAcceptSession(flatBufferBuilder);
    }

    public static void startRequestVshAcceptSession(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static int endRequestVshAcceptSession(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
