package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestVshStopSession extends Table {
    public static RequestVshStopSession getRootAsRequestVshStopSession(ByteBuffer byteBuffer) {
        return getRootAsRequestVshStopSession(byteBuffer, new RequestVshStopSession());
    }

    public static RequestVshStopSession getRootAsRequestVshStopSession(ByteBuffer byteBuffer, RequestVshStopSession requestVshStopSession) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestVshStopSession.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestVshStopSession __assign(int i, ByteBuffer byteBuffer) {
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

    public static int createRequestVshStopSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.startObject(1);
        addSessionId(flatBufferBuilder, j);
        return endRequestVshStopSession(flatBufferBuilder);
    }

    public static void startRequestVshStopSession(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static int endRequestVshStopSession(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
