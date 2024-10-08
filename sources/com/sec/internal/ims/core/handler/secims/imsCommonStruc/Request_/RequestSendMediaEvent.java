package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendMediaEvent extends Table {
    public static RequestSendMediaEvent getRootAsRequestSendMediaEvent(ByteBuffer byteBuffer) {
        return getRootAsRequestSendMediaEvent(byteBuffer, new RequestSendMediaEvent());
    }

    public static RequestSendMediaEvent getRootAsRequestSendMediaEvent(ByteBuffer byteBuffer, RequestSendMediaEvent requestSendMediaEvent) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSendMediaEvent.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSendMediaEvent __assign(int i, ByteBuffer byteBuffer) {
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

    public long target() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long event() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public long eventType() {
        int __offset = __offset(10);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestSendMediaEvent(FlatBufferBuilder flatBufferBuilder, long j, long j2, long j3, long j4) {
        flatBufferBuilder.startObject(4);
        addEventType(flatBufferBuilder, j4);
        addEvent(flatBufferBuilder, j3);
        addTarget(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        return endRequestSendMediaEvent(flatBufferBuilder);
    }

    public static void startRequestSendMediaEvent(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(4);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addTarget(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static void addEvent(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static void addEventType(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(3, (int) j, 0);
    }

    public static int endRequestSendMediaEvent(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
