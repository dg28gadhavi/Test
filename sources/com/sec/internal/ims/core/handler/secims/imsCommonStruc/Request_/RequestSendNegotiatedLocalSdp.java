package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendNegotiatedLocalSdp extends Table {
    public static RequestSendNegotiatedLocalSdp getRootAsRequestSendNegotiatedLocalSdp(ByteBuffer byteBuffer) {
        return getRootAsRequestSendNegotiatedLocalSdp(byteBuffer, new RequestSendNegotiatedLocalSdp());
    }

    public static RequestSendNegotiatedLocalSdp getRootAsRequestSendNegotiatedLocalSdp(ByteBuffer byteBuffer, RequestSendNegotiatedLocalSdp requestSendNegotiatedLocalSdp) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSendNegotiatedLocalSdp.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSendNegotiatedLocalSdp __assign(int i, ByteBuffer byteBuffer) {
        __init(i, byteBuffer);
        return this;
    }

    public long session() {
        int __offset = __offset(4);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public String sdp() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sdpAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestSendNegotiatedLocalSdp(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addSdp(flatBufferBuilder, i);
        addSession(flatBufferBuilder, j);
        return endRequestSendNegotiatedLocalSdp(flatBufferBuilder);
    }

    public static void startRequestSendNegotiatedLocalSdp(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSdp(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endRequestSendNegotiatedLocalSdp(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
