package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestUpdateNrSaModeOnStart extends Table {
    public static RequestUpdateNrSaModeOnStart getRootAsRequestUpdateNrSaModeOnStart(ByteBuffer byteBuffer) {
        return getRootAsRequestUpdateNrSaModeOnStart(byteBuffer, new RequestUpdateNrSaModeOnStart());
    }

    public static RequestUpdateNrSaModeOnStart getRootAsRequestUpdateNrSaModeOnStart(ByteBuffer byteBuffer, RequestUpdateNrSaModeOnStart requestUpdateNrSaModeOnStart) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestUpdateNrSaModeOnStart.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestUpdateNrSaModeOnStart __assign(int i, ByteBuffer byteBuffer) {
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

    public static int createRequestUpdateNrSaModeOnStart(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.startObject(1);
        addSession(flatBufferBuilder, j);
        return endRequestUpdateNrSaModeOnStart(flatBufferBuilder);
    }

    public static void startRequestUpdateNrSaModeOnStart(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(1);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static int endRequestUpdateNrSaModeOnStart(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
