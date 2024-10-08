package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendRpAckResp extends Table {
    public static RequestSendRpAckResp getRootAsRequestSendRpAckResp(ByteBuffer byteBuffer) {
        return getRootAsRequestSendRpAckResp(byteBuffer, new RequestSendRpAckResp());
    }

    public static RequestSendRpAckResp getRootAsRequestSendRpAckResp(ByteBuffer byteBuffer, RequestSendRpAckResp requestSendRpAckResp) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSendRpAckResp.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSendRpAckResp __assign(int i, ByteBuffer byteBuffer) {
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

    public String callId() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer callIdAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestSendRpAckResp(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addCallId(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestSendRpAckResp(flatBufferBuilder);
    }

    public static void startRequestSendRpAckResp(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addCallId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endRequestSendRpAckResp(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
