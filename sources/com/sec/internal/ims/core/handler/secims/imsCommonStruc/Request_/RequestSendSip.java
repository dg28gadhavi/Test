package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestSendSip extends Table {
    public static RequestSendSip getRootAsRequestSendSip(ByteBuffer byteBuffer) {
        return getRootAsRequestSendSip(byteBuffer, new RequestSendSip());
    }

    public static RequestSendSip getRootAsRequestSendSip(ByteBuffer byteBuffer, RequestSendSip requestSendSip) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestSendSip.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestSendSip __assign(int i, ByteBuffer byteBuffer) {
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

    public String sipMessage() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer sipMessageAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestSendSip(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addSipMessage(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestSendSip(flatBufferBuilder);
    }

    public static void startRequestSendSip(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSipMessage(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endRequestSendSip(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
