package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestAcceptSlm extends Table {
    public static RequestAcceptSlm getRootAsRequestAcceptSlm(ByteBuffer byteBuffer) {
        return getRootAsRequestAcceptSlm(byteBuffer, new RequestAcceptSlm());
    }

    public static RequestAcceptSlm getRootAsRequestAcceptSlm(ByteBuffer byteBuffer, RequestAcceptSlm requestAcceptSlm) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestAcceptSlm.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestAcceptSlm __assign(int i, ByteBuffer byteBuffer) {
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

    public String userAlias() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return __string(__offset + this.bb_pos);
        }
        return null;
    }

    public ByteBuffer userAliasAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public static int createRequestAcceptSlm(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addUserAlias(flatBufferBuilder, i);
        addSessionId(flatBufferBuilder, j);
        return endRequestAcceptSlm(flatBufferBuilder);
    }

    public static void startRequestAcceptSlm(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addSessionId(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addUserAlias(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addOffset(1, i, 0);
    }

    public static int endRequestAcceptSlm(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
