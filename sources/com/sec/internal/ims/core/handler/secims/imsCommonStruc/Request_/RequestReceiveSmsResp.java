package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestReceiveSmsResp extends Table {
    public static RequestReceiveSmsResp getRootAsRequestReceiveSmsResp(ByteBuffer byteBuffer) {
        return getRootAsRequestReceiveSmsResp(byteBuffer, new RequestReceiveSmsResp());
    }

    public static RequestReceiveSmsResp getRootAsRequestReceiveSmsResp(ByteBuffer byteBuffer, RequestReceiveSmsResp requestReceiveSmsResp) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestReceiveSmsResp.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestReceiveSmsResp __assign(int i, ByteBuffer byteBuffer) {
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

    public long status() {
        int __offset = __offset(8);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestReceiveSmsResp(FlatBufferBuilder flatBufferBuilder, long j, int i, long j2) {
        flatBufferBuilder.startObject(3);
        addStatus(flatBufferBuilder, j2);
        addCallId(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestReceiveSmsResp(flatBufferBuilder);
    }

    public static void startRequestReceiveSmsResp(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(3);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addCallId(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static void addStatus(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(2, (int) j, 0);
    }

    public static int endRequestReceiveSmsResp(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
