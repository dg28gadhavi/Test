package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestStartVideoEarlymedia extends Table {
    public static RequestStartVideoEarlymedia getRootAsRequestStartVideoEarlymedia(ByteBuffer byteBuffer) {
        return getRootAsRequestStartVideoEarlymedia(byteBuffer, new RequestStartVideoEarlymedia());
    }

    public static RequestStartVideoEarlymedia getRootAsRequestStartVideoEarlymedia(ByteBuffer byteBuffer, RequestStartVideoEarlymedia requestStartVideoEarlymedia) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return requestStartVideoEarlymedia.__assign(byteBuffer.getInt(byteBuffer.position()) + byteBuffer.position(), byteBuffer);
    }

    public void __init(int i, ByteBuffer byteBuffer) {
        this.bb_pos = i;
        this.bb = byteBuffer;
    }

    public RequestStartVideoEarlymedia __assign(int i, ByteBuffer byteBuffer) {
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

    public long session() {
        int __offset = __offset(6);
        if (__offset != 0) {
            return ((long) this.bb.getInt(__offset + this.bb_pos)) & 4294967295L;
        }
        return 0;
    }

    public static int createRequestStartVideoEarlymedia(FlatBufferBuilder flatBufferBuilder, long j, long j2) {
        flatBufferBuilder.startObject(2);
        addSession(flatBufferBuilder, j2);
        addHandle(flatBufferBuilder, j);
        return endRequestStartVideoEarlymedia(flatBufferBuilder);
    }

    public static void startRequestStartVideoEarlymedia(FlatBufferBuilder flatBufferBuilder) {
        flatBufferBuilder.startObject(2);
    }

    public static void addHandle(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(0, (int) j, 0);
    }

    public static void addSession(FlatBufferBuilder flatBufferBuilder, long j) {
        flatBufferBuilder.addInt(1, (int) j, 0);
    }

    public static int endRequestStartVideoEarlymedia(FlatBufferBuilder flatBufferBuilder) {
        return flatBufferBuilder.endObject();
    }
}
