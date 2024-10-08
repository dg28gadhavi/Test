package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestIshAcceptSession extends Table {
    public static RequestIshAcceptSession getRootAsRequestIshAcceptSession(ByteBuffer byteBuffer) {
        return getRootAsRequestIshAcceptSession(byteBuffer, new RequestIshAcceptSession());
    }

    public static RequestIshAcceptSession getRootAsRequestIshAcceptSession(ByteBuffer byteBuffer, RequestIshAcceptSession requestIshAcceptSession) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestIshAcceptSession.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestIshAcceptSession __assign(int i, ByteBuffer byteBuffer) {
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

    public String filePath() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer filePathAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestIshAcceptSession(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addFilePath(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        return endRequestIshAcceptSession(flatBufferBuilder);
    }

    public static void startRequestIshAcceptSession(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addFilePath(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endRequestIshAcceptSession(FlatBufferBuilder flatBufferBuilder) {
        int endObject = flatBufferBuilder.endObject();
        flatBufferBuilder.required(endObject, 6);
        return endObject;
    }
}
