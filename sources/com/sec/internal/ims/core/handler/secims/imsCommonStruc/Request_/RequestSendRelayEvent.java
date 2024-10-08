package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendRelayEvent extends Table {
    public static RequestSendRelayEvent getRootAsRequestSendRelayEvent(ByteBuffer byteBuffer) {
        return getRootAsRequestSendRelayEvent(byteBuffer, new RequestSendRelayEvent());
    }

    public static RequestSendRelayEvent getRootAsRequestSendRelayEvent(ByteBuffer byteBuffer, RequestSendRelayEvent requestSendRelayEvent) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSendRelayEvent.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSendRelayEvent __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long streamId() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long event() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestSendRelayEvent(FlatBufferBuilder flatBufferBuilder, long j, long j2) {
        flatBufferBuilder.startObject(2);
        addEvent(flatBufferBuilder, j2);
        addStreamId(flatBufferBuilder, j);
        return endRequestSendRelayEvent(flatBufferBuilder);
    }

    public static void startRequestSendRelayEvent(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addStreamId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addEvent(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static int endRequestSendRelayEvent(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
