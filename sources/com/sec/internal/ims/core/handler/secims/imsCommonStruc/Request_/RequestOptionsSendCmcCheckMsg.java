package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestOptionsSendCmcCheckMsg extends Table {
    public static RequestOptionsSendCmcCheckMsg getRootAsRequestOptionsSendCmcCheckMsg(ByteBuffer byteBuffer) {
        return getRootAsRequestOptionsSendCmcCheckMsg(byteBuffer, new RequestOptionsSendCmcCheckMsg());
    }

    public static RequestOptionsSendCmcCheckMsg getRootAsRequestOptionsSendCmcCheckMsg(ByteBuffer byteBuffer, RequestOptionsSendCmcCheckMsg requestOptionsSendCmcCheckMsg) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestOptionsSendCmcCheckMsg.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestOptionsSendCmcCheckMsg __assign(int i, ByteBuffer byteBuffer) {
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

    public String uri() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestOptionsSendCmcCheckMsg(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addUri(flatBufferBuilder, i);
        addHandle(flatBufferBuilder, j);
        return endRequestOptionsSendCmcCheckMsg(flatBufferBuilder);
    }

    public static void startRequestOptionsSendCmcCheckMsg(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addUri(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endRequestOptionsSendCmcCheckMsg(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
