package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestRejectModifyCallType extends Table {
    public static RequestRejectModifyCallType getRootAsRequestRejectModifyCallType(ByteBuffer byteBuffer) {
        return getRootAsRequestRejectModifyCallType(byteBuffer, new RequestRejectModifyCallType());
    }

    public static RequestRejectModifyCallType getRootAsRequestRejectModifyCallType(ByteBuffer byteBuffer, RequestRejectModifyCallType requestRejectModifyCallType) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestRejectModifyCallType.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestRejectModifyCallType __assign(int i, ByteBuffer byteBuffer) {
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

    public int reason() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return this.bb.getInt(__offset + this.bb_pos);
        }
        return 0;
    }

    public static int createRequestRejectModifyCallType(FlatBufferBuilder flatBufferBuilder, long j, int i) {
        flatBufferBuilder.startObject(2);
        addReason(flatBufferBuilder, i);
        addSession(flatBufferBuilder, j);
        return endRequestRejectModifyCallType(flatBufferBuilder);
    }

    public static void startRequestRejectModifyCallType(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addReason(FlatBufferBuilder flatBufferBuilder, int i) {
        flatBufferBuilder.addInt(1, i, 0);
    }

    public static int endRequestRejectModifyCallType(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
